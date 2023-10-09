package com.logicalis.serviceinsight.scheduled;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.logicalis.serviceinsight.dao.Location;
import com.logicalis.serviceinsight.data.ContractServiceDetail;
import com.logicalis.serviceinsight.data.ScheduledTask;
import com.logicalis.serviceinsight.data.Service;
import com.logicalis.serviceinsight.data.UMPCIRecord;
import com.logicalis.serviceinsight.scheduled.ChronosScheduled.ChronosRawData;
import com.logicalis.serviceinsight.service.ApplicationDataDaoService;
import com.logicalis.serviceinsight.service.ContractDaoService;
import com.logicalis.serviceinsight.service.CostDaoService;
import com.logicalis.serviceinsight.service.CostService;
import com.logicalis.serviceinsight.service.ServiceException;

@Component
public class UMPScheduler extends BaseScheduler {

	protected final Logger log = LoggerFactory.getLogger(getClass());
    private JdbcTemplate chronosJdbcTemplate;
    private JdbcTemplate jdbcTemplate;
    private NamedParameterJdbcTemplate namedJdbcTemplate;
    private static final String UMP_CI_SYNC_TSK = "ump_ci_sync";
    private static final String AZURE_LOCATION_ALT_NAME = "Azure";
    
    @Value("${application.timezone}")
    protected String TZID;
    
    @Autowired
	ApplicationDataDaoService applicationDataDaoService;
    @Autowired
    ContractDaoService contractDaoService;

    @Autowired
    void setChronosDataSource(DataSource chronosDataSource, DataSource dataSource) {
        this.chronosJdbcTemplate = new JdbcTemplate(chronosDataSource);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.namedJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }
    
    @Async
    @Scheduled(cron = "0 30 4 * * *") //4:30am
    public void syncCIsFromUMP() {
    	try {
            ScheduledTask st = applicationDataDaoService.findScheduledTaskByCode(UMP_CI_SYNC_TSK);
            if (st != null && st.getEnabled()) {
                log.info("Running Task: " + st.getName());
                List<UMPCIRecord> cis = getCIsFromUMP();
                mergeCIs(cis);
                log.info("Ending Task: " + st.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private List<UMPCIRecord> getCIsFromUMP() {
    	log.info("Retreiving CIs from CI_Export Table");
    	String query = "select * from CI_Export";
	    List<UMPCIRecord> data = chronosJdbcTemplate.query(query, new RowMapper<UMPCIRecord>() {
	    	@Override
	    	public UMPCIRecord mapRow(ResultSet rs, int i) throws SQLException {
	    		return new UMPCIRecord(
                      rs.getString("Contract"),
                      rs.getString("Contract_ID"),
                      rs.getString("Company"),
                      rs.getString("Company_ID"),
                      rs.getString("CI_Name"),
                      rs.getString("CI_ID"),
                      rs.getString("Location"),
                      rs.getString("OpSys"),
                      rs.getInt("NumCPUs"),
                      rs.getBigDecimal("Memory_GB"),
                      rs.getBigDecimal("Storage_GB"));
	    	}
	    });
	    
	    if(data != null && !data.isEmpty()) {
	    	log.info("Successfully Retreived {} CIs from CI_Export Table", data.size());
	    } else {
	    	log.info("Zero CIs found in CI_Export Table");
	    }
	    
	    return data;
    }
    
    private void mergeCIs(List<UMPCIRecord> cis) {
    	List<Service> matchedServices = new ArrayList<Service>();
    	List<Location> locations =  applicationDataDaoService.locations(null);
    	
    	log.info("Syncing Details For {} CIs from the UMP Data", new Object[]{cis.size()});
    	for(UMPCIRecord ci : cis) {
    		try {
    			Service contractService = contractDaoService.contractServiceBySNSysId(ci.getCiSNSysId());
    			if(contractService != null) {
    				if(ci.getLocation() != null) {
	    				Location location = getLocationFromUMPLocations(locations, ci.getLocation());
	    				if(location != null) contractService.setLocationId(location.getId());
    				}
    				
    				mergeCI(ci, contractService);
    				matchedServices.add(contractService);
    			} else {
    				//log.info("No Match for CI {}", new Object[]{ci.getCiName()});
    			}
        	} catch (Exception e) {
        		e.printStackTrace();
        	}
		}
    	
    	//Delete Contract Service Detail for records that are no longer in our matched list
    	log.info("About to Remove Details for Records that No Longer Match.");
    	List<ContractServiceDetail> existingDetails = contractDaoService.contractServiceDetails();
    	for(ContractServiceDetail detail : existingDetails) {
    		boolean matched = false;
    		for(Service service : matchedServices) {
    			if(service.getId().equals(detail.getContractServiceId())) {
    				matched = true;
    				break;
    			}
    		}
    		
    		if(!matched) {
    			try {
    				log.info("Deleting Detail for :" + detail.getContractServiceId());
    				contractDaoService.deleteContractServiceDetail(detail.getContractServiceId());
    			} catch (Exception e) {
    				e.printStackTrace();
    			}
    		}
    	}
    }
    
    private void mergeCI(UMPCIRecord ci, Service contractService) throws ServiceException {
    	ContractServiceDetail detail = new ContractServiceDetail(contractService.getId(), ci.getLocation(), ci.getOperatingSystem(), ci.getCpuCount(), ci.getMemoryGB(), ci.getStorageGB());
    	contractService.setDetail(detail);
    	contractDaoService.updateContractService(contractService, Boolean.FALSE);
    }
    
    private Location getLocationFromUMPLocations(List<Location> locations, String locationName) {
    	Location matchedLocation = null;
    	
    	if(locationName.contains(AZURE_LOCATION_ALT_NAME)) locationName = AZURE_LOCATION_ALT_NAME;
    	
    	for(Location location : locations) {
    		if(!"".equals(locationName) && locationName != null && locationName.equals(location.getAltName())) {
    			matchedLocation = location;
    			break;
    		}
    	}
    	
    	return matchedLocation;
    }
	
}
