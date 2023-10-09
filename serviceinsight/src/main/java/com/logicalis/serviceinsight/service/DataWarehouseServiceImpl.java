package com.logicalis.serviceinsight.service;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.logicalis.serviceinsight.dao.CostItem;
import com.logicalis.serviceinsight.dao.CostItem.CostType;
import com.logicalis.serviceinsight.dao.UnitCost;
import com.logicalis.serviceinsight.data.Contract;
import com.logicalis.serviceinsight.data.DataWarehouseContract;
import com.logicalis.serviceinsight.data.DataWarehouseContractUpdate;
import com.logicalis.serviceinsight.data.DataWarehouseCostItem;
import com.logicalis.serviceinsight.data.DataWarehouseLineItem;
import com.logicalis.serviceinsight.data.SIDataWarehouseContract;
import com.logicalis.serviceinsight.data.SIDataWarehouseLineItem;
import com.logicalis.serviceinsight.data.ScheduledTask;
import com.logicalis.serviceinsight.data.SIDataWarehouseContract.Type;
import com.logicalis.serviceinsight.data.SIDataWarehouseContractUpdate;
import com.logicalis.serviceinsight.data.SIDataWarehouseCostItem;
import com.logicalis.serviceinsight.scheduled.ChronosScheduled.ChronosRawData;
import java.math.MathContext;
import java.math.RoundingMode;
import org.joda.time.format.DateTimeFormat;
import org.springframework.util.StopWatch;

@Service
@Transactional(readOnly = false, rollbackFor = ServiceException.class)
public class DataWarehouseServiceImpl extends BaseServiceImpl implements DataWarehouseService {

	protected final Logger log = LoggerFactory.getLogger(getClass());
	private JdbcTemplate azureJdbcTemplate;
	private static final String DATA_WAREHOUSE_CI_SYNC_TSK = "data_warehouse_ci_sync";
	private static final String DATA_WAREHOUSE_CONTRACT_SYNC_TSK = "data_warehouse_contract_sync";
	private static final String DATA_WAREHOUSE_PCR_SYNC_TSK = "data_warehouse_pcr_sync";
	private static final String DATA_WAREHOUSE_COST_SYNC_TSK = "data_warehouse_cost_sync";
	
	@Value("${application.timezone}")
    protected String TZID;
	
	@Autowired
    ApplicationDataDaoService applicationDataDaoService;
        @Autowired
        CostService costService;
        @Autowired
        CostDaoService costDaoService;
	
	@Autowired
    void setAzureDataSource(DataSource azureDataSource, DataSource dataSource) {
        this.azureJdbcTemplate = new JdbcTemplate(azureDataSource);
        //this.jdbcTemplate = new JdbcTemplate(dataSource);
        //this.namedJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }
	
        @Override
	public List<DataWarehouseLineItem> getCIRecords(Date importDate) throws ServiceException {
		String query = "select * from SI_CI_LIST where Month = ?";
		DateTime queryDate = new DateTime(importDate).withZone(DateTimeZone.forID(TZID)).withDayOfMonth(1).withTimeAtStartOfDay();
        List<DataWarehouseLineItem> data = azureJdbcTemplate.query(query, new Object[] {queryDate.toDate()}, new RowMapper<DataWarehouseLineItem>() {
        //List<DataWarehouseLineItem> data = azureJdbcTemplate.query(query, new RowMapper<DataWarehouseLineItem>() {
            @Override
            public DataWarehouseLineItem mapRow(ResultSet rs, int i) throws SQLException {
                return new DataWarehouseLineItem(
                        rs.getDate("Month"),
                        rs.getString("Customer"),
                        rs.getString("Job_Number"),
                        rs.getString("SOW_Name"),
                        rs.getString("OSP_Service"),
                        rs.getLong("ID"),
                        rs.getString("Line_Item_Descr"),
                        rs.getString("Line_ItemPart_Number"),
                        rs.getString("CI_Name"),
                        rs.getString("OSM_Sys_ID"),
                        rs.getInt("Unit_Count"),
                        rs.getBigDecimal("NRC"),
                        rs.getBigDecimal("MRC"),
                        rs.getDate("Start_Date"),
                        rs.getDate("End_Date"),
                        rs.getString("Status"));
            }
        });
        /*
        for(DataWarehouseLineItem item: data) {
        	log.info("Item: " + item.toString());
        }*/
        return data;
	}
	
	@Async
	public void getContractRecords(Date importDate) throws ServiceException {
		String query = "select * from SI_SOW where Month = ?";
		DateTime queryDate = new DateTime(importDate).withZone(DateTimeZone.forID(TZID)).withDayOfMonth(1).withTimeAtStartOfDay();
        log.info("Date - " + queryDate.toDate());
        List<DataWarehouseContract> data = azureJdbcTemplate.query(query, new Object[] {queryDate.toDate()}, new RowMapper<DataWarehouseContract>() {
            @Override
            public DataWarehouseContract mapRow(ResultSet rs, int i) throws SQLException {
                return new DataWarehouseContract(
                        rs.getDate("Month"),
                        rs.getString("Customer"),
                        rs.getLong("ID"),
                        rs.getString("Job_Number"),
                        rs.getString("SOW_Name"),
                        rs.getString("Alt_ID"),
                        rs.getDate("Signed_Date"),
                        rs.getDate("Start_Date"),
                        rs.getDate("Service_Start_Date"),
                        rs.getDate("End_Date"),
                        rs.getString("OSM_Sys_ID"),
                        rs.getString("SDM"),
                        rs.getString("AE"),
                        rs.getString("Type"));
            }
        });
        
        log.info("Total Results: " + data.size());
	}
	
	@Async
	public void getContractUpdateRecords(Date importDate) throws ServiceException {
		String query = "select * from SI_PCR where Month = ?";
		DateTime queryDate = new DateTime(importDate).withZone(DateTimeZone.forID(TZID)).withDayOfMonth(1).withTimeAtStartOfDay();
        log.info("Date - " + queryDate.toDate());
        List<DataWarehouseContractUpdate> data = azureJdbcTemplate.query(query, new Object[] {queryDate.toDate()}, new RowMapper<DataWarehouseContractUpdate>() {
            @Override
            public DataWarehouseContractUpdate mapRow(ResultSet rs, int i) throws SQLException {
                return new DataWarehouseContractUpdate(
                        rs.getDate("Month"),
                        rs.getString("Customer"),
                        rs.getLong("SOW_ID"),
                        rs.getString("Job_Number"),
                        rs.getString("SOW_Name"),
                        rs.getString("SOW_Alt_ID"),
                        rs.getLong("ID"),
                        rs.getString("PCR_Alt_ID"),
                        rs.getDate("Signed_Date"),
                        rs.getDate("Effective_Date"),
                        rs.getString("Note"),
                        rs.getString("Type"));
            }
        });
        
        log.info("Total Results: " + data.size());
	}
	
	@Async
	public void getCostRecords(Date importDate) throws ServiceException {
		String query = "select * from SI_COST where Month = ?";
		DateTime queryDate = new DateTime(importDate).withZone(DateTimeZone.forID(TZID)).withDayOfMonth(1).withTimeAtStartOfDay();
        log.info("Date - " + queryDate.toDate());
        List<DataWarehouseCostItem> data = azureJdbcTemplate.query(query, new Object[] {queryDate.toDate()}, new RowMapper<DataWarehouseCostItem>() {
            @Override
            public DataWarehouseCostItem mapRow(ResultSet rs, int i) throws SQLException {
                return new DataWarehouseCostItem(
                        rs.getDate("Month"),
                        rs.getString("Customer"),
                        rs.getLong("ID"),
                        rs.getString("Job_Number"),
                        rs.getString("SOW_Name"),
                        rs.getString("Cost_Name"),
                        rs.getInt("Quantity"),
                        rs.getBigDecimal("Cost"),
                        rs.getString("Type"));
            }
        });
        
        log.info("Total Results: " + data.size());
	}
	
	public void updateDataWarehouseCIs() throws ServiceException {
		updateDataWarehouseCIsforMonthOf(null);
	}
	
	public void updateDataWarehouseContracts() throws ServiceException {
		updateDataWarehouseContractsforMonthOf(null);
	}
	
	public void updateDataWarehouseContractUpdates() throws ServiceException {
		updateDataWarehouseContractUpdatesforMonthOf(null);
	}
	
	public void updateDataWarehouseCosts() throws ServiceException {
		updateDataWarehouseCostsforMonthOf(null);
	}
	
	@Async
	public void updateDataWarehouseCIsforMonthOf(Date importDate) throws ServiceException {
		try {
            ScheduledTask st = applicationDataDaoService.findScheduledTaskByCode(DATA_WAREHOUSE_CI_SYNC_TSK);
            if (st != null && st.getEnabled()) {
                log.info("Running Task: " + st.getName());
                
                if (importDate == null) {
                	importDate = new Date();
                	log.info("Import Date was null, so setting it to current date.");
                }
               
                DateTime queryStartDate = new DateTime(importDate).withZone(DateTimeZone.forID(TZID)).withDayOfMonth(1).withTimeAtStartOfDay();
                DateTime queryEndDate = new DateTime(importDate).dayOfMonth().withMaximumValue().withTimeAtStartOfDay().plusHours(23).plusMinutes(59).plusSeconds(59);
                
                log.info("Deleting records for Month of: " + queryStartDate.toDate());
                try {
                    int deleted = azureJdbcTemplate.update("delete from SI_CI_LIST where Month = ?", queryStartDate.toDate());
                } catch (Exception any) {
                    throw new ServiceException(messageSource.getMessage("jdbc_error_contract_adjustment_delete", new Object[]{queryStartDate.toDate(), any.getMessage()}, LocaleContextHolder.getLocale()), any);
                }
                
                String getQuery = "select cst.name cust_name, ctr.job_number job_number, ctr.name sow_name, svc.name service_name, csvc.id lineitem_id,"
                        + " d.description lineitem_descr, d.part_number lineitem_part_number, csd.name ci_name, snci.sn_sys_id osm_sys_id,"
                        + " (case when (csd.unit_count = 0 or csd.unit_count is null) then csvc.quantity else csvc.quantity * csd.unit_count end) as 'blended_unit_count',"
                        + " csvc.onetime_revenue onetime, csvc.recurring_revenue recurring, csvc.start_date start_date,"
                        + " csvc.end_date end_date, csvc.status status"
                		+ " from contract_service csvc" 
                		+ " inner join service svc on svc.id = csvc.service_id"
                		+ " inner join contract ctr on ctr.id = csvc.contract_id"
                		+ " inner join customer cst on cst.id = ctr.customer_id" 
                		+ " inner join contract_service_device csd on csd.contract_service_id = csvc.id"
                		+ " inner join device d on d.id = csd.device_id"
                		+ " left join service_now_ci snci on snci.contract_service_id = csvc.id"
                		+ " where (csvc.start_date <= :leftDate or csvc.start_date between :leftDate and :rightDate)"
                		+ " and (csvc.end_date is null or csvc.end_date between :leftDate and :rightDate or csvc.end_date >= :rightDate)"
                		+ " and csvc.parent_id is null"
                		+ " order by cst.name, ctr.job_number, d.description, csvc.start_date, csvc.end_date";
                
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("leftDate", queryStartDate.toDate());
                params.put("rightDate", queryEndDate.toDate());
                
                List<SIDataWarehouseLineItem> lineItems = namedJdbcTemplate.query(getQuery, params, new RowMapper<SIDataWarehouseLineItem>() {
                    @Override
                    public SIDataWarehouseLineItem mapRow(ResultSet rs, int i) throws SQLException {
                        return new SIDataWarehouseLineItem(
                                rs.getString("cust_name"),
                                rs.getString("job_number"),
                                rs.getString("sow_name"),
                                rs.getString("service_name"),
                                rs.getLong("lineitem_id"),
                                rs.getString("lineitem_descr"),
                                rs.getString("lineitem_part_number"),
                                rs.getString("ci_name"),
                                rs.getString("osm_sys_id"),
                                rs.getInt("blended_unit_count"),
                                rs.getBigDecimal("onetime"),
                                rs.getBigDecimal("recurring"),
                                rs.getDate("start_date"),
                                rs.getDate("end_date"),
                                rs.getString("status"));
                    }
                });
                
                List<SIDataWarehouseLineItem> lineItemsToAdd = new ArrayList<SIDataWarehouseLineItem>();
                //get all the child line items
                for(SIDataWarehouseLineItem lineItem: lineItems) {
                	lineItemsToAdd.add(lineItem);
                	List<SIDataWarehouseLineItem> childLineItems = getChildLineItems(lineItem.getLineItemId(), queryStartDate, queryEndDate);
                	for(SIDataWarehouseLineItem childLineItem: childLineItems) {
                		childLineItem.setCiName(lineItem.getCiName());
                		childLineItem.setOsmSysId(lineItem.getOsmSysId());
                		
                		lineItemsToAdd.add(childLineItem);
                		List<SIDataWarehouseLineItem> grandChildLineItems = getChildLineItems(childLineItem.getLineItemId(), queryStartDate, queryEndDate);
                		for(SIDataWarehouseLineItem grandChildLineItem: grandChildLineItems) {
                			grandChildLineItem.setCiName(lineItem.getCiName());
                			grandChildLineItem.setOsmSysId(lineItem.getOsmSysId());
                    		lineItemsToAdd.add(grandChildLineItem);
                		}
                	}
                }
                
                /**
                 * we compute two sets of cost, now:
                 * - standard costs across all costtype/subtypes using database unit_cost entries
                 * - "custom" costs excluding the following set of costtype/subtypes, calculated on the fly.
                 */
                List<Map<String, String>> excludeCostTypes = new ArrayList<Map<String, String>>();
                Map<String, String> excludeCostType = new HashMap<String, String>();
                excludeCostType.put("CostType", "depreciated");
                excludeCostType.put("CostSubType", "dc_rent");
                excludeCostTypes.add(excludeCostType);
                excludeCostType = new HashMap<String, String>();
                excludeCostType.put("CostType", "depreciated");
                excludeCostType.put("CostSubType", "dedicated");
                excludeCostTypes.add(excludeCostType);
                excludeCostType = new HashMap<String, String>();
                excludeCostType.put("CostType", "depreciated");
                excludeCostType.put("CostSubType", "multi_tenant");
                excludeCostTypes.add(excludeCostType);
                
                computeDataWarehouseLineItemCost(lineItemsToAdd, queryStartDate, null);
                computeDataWarehouseLineItemCost(lineItemsToAdd, queryStartDate, excludeCostTypes);
                log.info("Starting batch save");
                saveBatchLineItems(lineItemsToAdd, queryStartDate.toDate());
                log.info("Ending batch save");
                
                log.info("Ending Task: " + st.getName());
            } else {
            	log.info("Data Warehouse Sync is not enabled.");
            }
        } catch (Exception e) {
            log.error("an error occurred processing DataWarehouse CI import", e);
            throw new ServiceException("An exception occurred ... rolling back");
        }
	}
        
        private void computeDataWarehouseLineItemCost(List<SIDataWarehouseLineItem> lineitems, DateTime importDateTime, List<Map<String, String>> excludeCostTypes) {
            if (lineitems == null || lineitems.size() == 0) {
                log.warn("No records were passed to compute \"Unit Costs\" for the import date [{}]",
                        DateTimeFormat.forPattern("yyyy-MM-dd").print(importDateTime));
                return;
            }
            StopWatch sw = new StopWatch();
            sw.start();
            log.info("Computing \"Unit Costs\" for [{}] DataWarehouse records for the import date [{}]",
                    new Object[]{lineitems.size(), DateTimeFormat.forPattern("yyyy-MM-dd").print(importDateTime)});
            List<UnitCost> customerUnitCosts = new ArrayList<UnitCost>();
            List<UnitCost> nonCustomerUnitCosts = new ArrayList<UnitCost>();
            Map<Integer, String> expCatMap = new HashMap<Integer, String>(); // this is for logging names of expense categories efficiently
            for (SIDataWarehouseLineItem lineitem : lineitems) {
                Long csvcId = lineitem.getLineItemId();
                List<Map<String, Object>> records = jdbcTemplate.queryForList("select ctr.customer_id, dexcat.expense_category_id"
                        + " from contract ctr"
                        + " inner join contract_service csvc on ctr.id = csvc.contract_id"
                        + " inner join contract_service_device csd on csvc.id = csd.contract_service_id"
                        + " inner join device_expense_category dexcat on csd.device_id = dexcat.device_id"
                        + " where csvc.id = ?", new Object[]{csvcId});
                for (Map<String, Object> record : records) {
                    /**
                     * Find customer related costs
                     */
                    Integer expCatId = (Integer) record.get("expense_category_id");
                    Long customerId = (Long) record.get("customer_id");
                    UnitCost cuc = new UnitCost(expCatId, customerId, importDateTime);
                    int index = customerUnitCosts.indexOf(cuc);
                    if (index < 0) {
                        if (excludeCostTypes == null) {
                            cuc = costDaoService.unitCostByExpenseCategoryAndDate(customerId, expCatId, importDateTime);
                        } else {
                            cuc = costDaoService.customCostByExpenseCategoryAndDate(customerId, expCatId, importDateTime, excludeCostTypes);
                        }
                        if (cuc != null) { // there may not be a UnitCost in the database
                            cuc.setCustomerName(lineitem.getCustomerName());
                            customerUnitCosts.add(cuc);
                        } else {
                            // otherwise we're hunting for this same non-functional unitCost over and over again...
                            customerUnitCosts.add(new UnitCost(expCatId, customerId, importDateTime));
                        }
                    } else {
                        cuc = customerUnitCosts.get(index);
                    }
                    if (cuc != null && cuc.getDeviceTotalUnits() > 0) {
                        BigDecimal costFraction = new BigDecimal(lineitem.getUnitCount()).divide(new BigDecimal(cuc.getDeviceTotalUnits()), MathContext.DECIMAL32);
                        BigDecimal contributingCost = cuc.getTotalCost().multiply(costFraction);
                        if (excludeCostTypes == null) {
                            lineitem.addDirectCost(contributingCost);
                        } else {
                            lineitem.addNonDepreciatedDirectCost(contributingCost);
                        }
                    }
                    /**
                     * Now find non-customer costs
                     */
                    UnitCost suc = new UnitCost(expCatId, null, importDateTime);
                    index = nonCustomerUnitCosts.indexOf(suc);
                    if (index < 0) {
                        if (excludeCostTypes == null) {
                            suc = costDaoService.unitCostByExpenseCategoryAndDate(null, expCatId, importDateTime);
                        } else {
                            suc = costDaoService.customCostByExpenseCategoryAndDate(null, expCatId, importDateTime, excludeCostTypes);
                        }
                        if (suc != null) { // there may not be a UnitCost in the database
                            nonCustomerUnitCosts.add(suc);
                        } else {
                            // otherwise we're hunting for this same non-functional unitCost over and over again...
                            nonCustomerUnitCosts.add(new UnitCost(expCatId, null, importDateTime));
                        }
                    } else {
                        suc = nonCustomerUnitCosts.get(index);
                    }
                    if (suc != null && suc.getDeviceTotalUnits() > 0) {
                        BigDecimal costFraction = new BigDecimal(lineitem.getUnitCount()).divide(new BigDecimal(suc.getDeviceTotalUnits()), MathContext.DECIMAL32);
                        BigDecimal contributingCost = suc.getTotalCost().multiply(costFraction);
                        if (excludeCostTypes == null) {
                            lineitem.addServiceToolsCost(contributingCost);
                        } else {
                            lineitem.addNonDepreciatedServiceToolsCost(contributingCost);
                        }
                    }
                }
            }
            /**
             * some logging of cost results
             */
            log.info("Customer Unit Cost Summary for Month: {}", DateTimeFormat.forPattern("yyyy-MM-dd").print(importDateTime));
            for (UnitCost unitCost : customerUnitCosts) {
                if (!expCatMap.containsKey(unitCost.getExpenseCategoryId())) {
                    String expCatName = jdbcTemplate.queryForObject("select concat(ecp.name, ' - ', ecc.name) as 'Name'"
                        + " from expense_category ecc"
                        + " inner join expense_category ecp on ecc.parent_id = ecp.id"
                        + " where ecc.id = ?", String.class, new Object[]{unitCost.getExpenseCategoryId()});
                    expCatMap.put(unitCost.getExpenseCategoryId(), expCatName);
                }
                unitCost.setExpenseCategoryName(expCatMap.get(unitCost.getExpenseCategoryId()));
                if (unitCost.getTotalCost().compareTo(BigDecimal.ZERO) > 0) {
                    log.info("\tCustomer: [{}] / {}, Cost Category: [{}] / {}, Total Cost: ${}, Total Unit Count: {}",
                            new Object[]{unitCost.getCustomerId(), unitCost.getCustomerName(), unitCost.getExpenseCategoryId(),
                            unitCost.getExpenseCategoryName(), unitCost.getTotalCost().setScale(2, RoundingMode.HALF_UP).toPlainString(),
                            unitCost.getDeviceTotalUnits()});
                }
            }
            log.info("Non-Customer Unit Cost Summary for Month: {}", DateTimeFormat.forPattern("yyyy-MM-dd").print(importDateTime));
            for (UnitCost unitCost : nonCustomerUnitCosts) {
                if (!expCatMap.containsKey(unitCost.getExpenseCategoryId())) {
                    String expCatName = jdbcTemplate.queryForObject("select concat(ecp.name, ' - ', ecc.name) as 'Name'"
                        + " from expense_category ecc"
                        + " inner join expense_category ecp on ecc.parent_id = ecp.id"
                        + " where ecc.id = ?", String.class, new Object[]{unitCost.getExpenseCategoryId()});
                    expCatMap.put(unitCost.getExpenseCategoryId(), expCatName);
                }
                unitCost.setExpenseCategoryName(expCatMap.get(unitCost.getExpenseCategoryId()));
                if (unitCost.getTotalCost().compareTo(BigDecimal.ZERO) > 0) {
                    log.info("\tCost Category: [{}] / {}, Total Cost: ${}, Total Unit Count: {}",
                            new Object[]{unitCost.getExpenseCategoryId(), unitCost.getExpenseCategoryName(),
                            unitCost.getTotalCost().setScale(2, RoundingMode.HALF_UP).toPlainString(), unitCost.getDeviceTotalUnits()});
                }
            }
            sw.stop();
            log.debug("**** DWH Cost compute took {} seconds ****", sw.getTotalTimeSeconds());
        }
	
	private List<SIDataWarehouseLineItem> getChildLineItems(Long parentId, DateTime startDate, DateTime endDate) {
		String getQuery = "select cst.name cust_name, ctr.job_number job_number, ctr.name sow_name, svc.name service_name, csvc.id lineitem_id,"
                        + " d.description lineitem_descr, d.part_number lineitem_part_number, csd.name ci_name, snci.sn_sys_id osm_sys_id,"
                        + " (case when (csd.unit_count = 0 or csd.unit_count is null) then csvc.quantity else csvc.quantity * csd.unit_count end) as 'blended_unit_count',"
                        + " csvc.onetime_revenue onetime, csvc.recurring_revenue recurring, csvc.start_date start_date,"
                        + " csvc.end_date end_date, csvc.status status"
		        		+ " from contract_service csvc" 
		        		+ " inner join service svc on svc.id = csvc.service_id"
		        		+ " inner join contract ctr on ctr.id = csvc.contract_id"
		        		+ " inner join customer cst on cst.id = ctr.customer_id" 
		        		+ " inner join contract_service_device csd on csd.contract_service_id = csvc.id"
		        		+ " inner join device d on d.id = csd.device_id"
		        		+ " left join service_now_ci snci on snci.contract_service_id = csvc.id"
		        		+ " where (csvc.start_date <= :leftDate or csvc.start_date between :leftDate and :rightDate)" 
		        		+ " and (csvc.end_date is null or csvc.end_date between :leftDate and :rightDate or csvc.end_date >= :rightDate)"
		        		+ " and csvc.parent_id = :parentId"
		        		+ " order by cst.name, ctr.job_number, d.description, csvc.start_date, csvc.end_date";
		
		Map<String, Object> params = new HashMap<String, Object>();
        params.put("leftDate", startDate.toDate());
        params.put("rightDate", endDate.toDate());
        params.put("parentId", parentId);
		
		List<SIDataWarehouseLineItem> lineItems = namedJdbcTemplate.query(getQuery, params, new RowMapper<SIDataWarehouseLineItem>() {
            @Override
            public SIDataWarehouseLineItem mapRow(ResultSet rs, int i) throws SQLException {
                return new SIDataWarehouseLineItem(
                        rs.getString("cust_name"),
                        rs.getString("job_number"),
                        rs.getString("sow_name"),
                        rs.getString("service_name"),
                        rs.getLong("lineitem_id"),
                        rs.getString("lineitem_descr"),
                        rs.getString("lineitem_part_number"),
                        rs.getString("ci_name"),
                        rs.getString("osm_sys_id"),
                        rs.getInt("blended_unit_count"),
                        rs.getBigDecimal("onetime"),
                        rs.getBigDecimal("recurring"),
                        rs.getDate("start_date"),
                        rs.getDate("end_date"),
                        rs.getString("status"));
            }
        });
		
		return lineItems;
	}
	
	private void saveBatchLineItems(final List<SIDataWarehouseLineItem> lineItems, final Date month) {
		log.info("saving batch  with [{}] records", lineItems.size());
		int[] results = azureJdbcTemplate.batchUpdate("insert into SI_CI_LIST (Month, Customer, Job_Number,"
                + " SOW_Name, OSP_Service, ID, Line_Item_Descr, Line_ItemPart_Number, CI_Name, OSM_Sys_ID,"
                + " Unit_Count, NRC, MRC, Cost, Cost_With_Depr, Start_Date, End_Date, Status)"
                + " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                int j = 1;
                SIDataWarehouseLineItem record = lineItems.get(i);
                ps.setDate(j++, new java.sql.Date(month.getTime()));
                ps.setString(j++, record.getCustomerName());
                ps.setString(j++, record.getJobNumber());
                ps.setString(j++, record.getSowName());
                ps.setString(j++, record.getOspServiceName());
                ps.setInt(j++, record.getLineItemId().intValue());
                ps.setString(j++, record.getLineItemDescription());
                ps.setString(j++, record.getLineItemPartNumber());
                ps.setString(j++, record.getCiName());
                ps.setString(j++, record.getOsmSysId());
                if (record.getUnitCount() == null) {
                    ps.setNull(j++, Types.INTEGER);
                } else {
                    ps.setInt(j++, record.getUnitCount());
                }
                ps.setBigDecimal(j++, record.getOnetimeRevenue());
                ps.setBigDecimal(j++, record.getRecurringRevenue());
                ps.setBigDecimal(j++, record.getNonDepreciatedTotalCost());
                ps.setBigDecimal(j++, record.getTotalCost());
                ps.setDate(j++, new java.sql.Date(record.getStartDate().getTime()));
                ps.setDate(j++, new java.sql.Date(record.getEndDate().getTime()));
                ps.setString(j++, record.getStatus());
                //ps.setTimestamp(j++, new java.sql.Timestamp(record.getWorkDate().getTime()));
            }

            @Override
            public int getBatchSize() {
                return lineItems.size();
            }
        });
		
		/*
		int count = 1;
        log.info("About to loop through results");
        for(int result : results) {
        	log.info(count + " - Batch Update Result is: [" + result + "]");
        }*/
	}
	
	@Async
	public void updateDataWarehouseContractsforMonthOf(Date importDate) throws ServiceException {
		try {
            ScheduledTask st = applicationDataDaoService.findScheduledTaskByCode(DATA_WAREHOUSE_CONTRACT_SYNC_TSK);
            if (st != null && st.getEnabled()) {
                log.info("Running Task: " + st.getName());
                
                if (importDate == null) {
                	importDate = new Date();
                	log.info("Import Date was null, so setting it to current date.");
                }
               
                Date queryStartDate = new DateTime(importDate).withZone(DateTimeZone.forID(TZID)).withDayOfMonth(1).withTimeAtStartOfDay().toDate();
                Date queryEndDate = new DateTime(importDate).dayOfMonth().withMaximumValue().withTimeAtStartOfDay().plusHours(23).plusMinutes(59).plusSeconds(59).toDate();
                
                log.info("Deleting records for Month of: " + queryStartDate);
                try {
                    int deleted = azureJdbcTemplate.update("delete from SI_SOW where Month = ?", queryStartDate);
                } catch (Exception any) {
                    throw new ServiceException(messageSource.getMessage("jdbc_error_contract_adjustment_delete", new Object[]{queryStartDate, any.getMessage()}, LocaleContextHolder.getLocale()), any);
                }
                
                
                String getQuery = "select cst.name customer_name, co.customer_id, co.id, co.alt_id, co.job_number, co.name,"
                        + " co.emgr, co.sda, co.signed_date, co.service_start_date, co.start_date, co.end_date, co.archived, co.sn_sys_id"
                        + " from contract co"
                        + " inner join customer cst on cst.id = co.id"
                        + " where co.archived = :archived"
                        + " and co.start_date between :startDate and :endDate";
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("archived", Boolean.FALSE);
                params.put("startDate", queryStartDate);
                params.put("endDate", queryEndDate);
                List<SIDataWarehouseContract> contracts = namedJdbcTemplate.query(getQuery, params, new RowMapper<SIDataWarehouseContract>() {
                    @Override
                    public SIDataWarehouseContract mapRow(ResultSet rs, int i) throws SQLException {
                        return new SIDataWarehouseContract(
                                rs.getString("customer_name"),
                                rs.getLong("id"),
                                rs.getString("job_number"),
                                rs.getString("name"),
                                rs.getString("alt_id"),
                                rs.getDate("signed_date"),
                                rs.getDate("start_date"),
                                rs.getDate("service_start_date"),
                                rs.getDate("end_date"),
                                rs.getString("sn_sys_id"),
                                rs.getString("emgr"),
                                rs.getString("sda"),
                                null);
                    }
                });
                
                String childQuery = "select csvc.service_id, s.business_model"
                        + " from contract_service csvc inner join service s on s.id = csvc.service_id"
                		+ " where csvc.contract_id = :contractId"
                        + " group by csvc.service_id";
                for(SIDataWarehouseContract contract: contracts) {
                	Map<String, Object> childParams = new HashMap<String, Object>();
                	childParams.put("contractId", contract.getId());
                    
                    List<String> businessModels = namedJdbcTemplate.query(childQuery, childParams, new RowMapper<String>() {
                        @Override
                        public String mapRow(ResultSet rs, int i) throws SQLException {
                            return new String(rs.getString("business_model"));
                        }
                    });
                    
                    SIDataWarehouseContract.Type type = getTypeFromBusinessModels(businessModels);
                    contract.setType(type);
                }
                
                log.info("Starting batch save");
                saveBatchContracts(contracts, queryStartDate);
                log.info("Ending batch save");
                
                log.info("Ending Task: " + st.getName());
            } else {
            	log.info("Data Warehouse Sync is not enabled.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException("An exception occurred ... rolling back");
        }
	}
	
	private void saveBatchContracts(final List<SIDataWarehouseContract> contracts, final Date month) {
		log.info("saving batch contracts with [{}] records", contracts.size());
		int[] results = azureJdbcTemplate.batchUpdate("insert into SI_SOW (Month, Customer, ID, Job_Number,"
                + " SOW_Name, Alt_ID, Signed_Date, Start_Date, Service_Start_Date, End_Date, OSM_Sys_ID,"
                + " SDM, AE, Type)"
                + " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                int j = 1;
                SIDataWarehouseContract record = contracts.get(i);
                ps.setDate(j++, new java.sql.Date(month.getTime()));
                ps.setString(j++, record.getCustomerName());
                ps.setInt(j++, record.getId().intValue());
                ps.setString(j++, record.getJobNumber());
                ps.setString(j++, record.getSowName());
                ps.setString(j++, record.getAltId());
                ps.setDate(j++, new java.sql.Date(record.getSignedDate().getTime()));
                ps.setDate(j++, new java.sql.Date(record.getStartDate().getTime()));
                ps.setDate(j++, new java.sql.Date(record.getServiceStartDate().getTime()));
                ps.setDate(j++, new java.sql.Date(record.getEndDate().getTime()));
                ps.setString(j++, record.getOsmSysId());
                ps.setString(j++, record.getSdm());
                ps.setString(j++, record.getAe());
                
                String type = null;
                if(record.getType() != null) {
                	type = record.getType().getDescription();
                }
                ps.setString(j++, type);
                //ps.setTimestamp(j++, new java.sql.Timestamp(record.getWorkDate().getTime()));
            }

            @Override
            public int getBatchSize() {
                return contracts.size();
            }
        });
		
		/*
		int count = 1;
        log.info("About to loop through results");
        for(int result : results) {
        	log.info(count + " - Batch Update Result is: [" + result + "]");
        }*/
	}
	
	@Async
	public void updateDataWarehouseContractUpdatesforMonthOf(Date importDate) throws ServiceException {
		try {
            ScheduledTask st = applicationDataDaoService.findScheduledTaskByCode(DATA_WAREHOUSE_PCR_SYNC_TSK);
            if (st != null && st.getEnabled()) {
                log.info("Running Task: " + st.getName());
                
                if (importDate == null) {
                	importDate = new Date();
                	log.info("Import Date was null, so setting it to current date.");
                }
               
                Date queryStartDate = new DateTime(importDate).withZone(DateTimeZone.forID(TZID)).withDayOfMonth(1).withTimeAtStartOfDay().toDate();
                Date queryEndDate = new DateTime(importDate).dayOfMonth().withMaximumValue().withTimeAtStartOfDay().plusHours(23).plusMinutes(59).plusSeconds(59).toDate();
                
                log.info("Deleting records for Month of: " + queryStartDate);
                try {
                    int deleted = azureJdbcTemplate.update("delete from SI_PCR where Month = ?", queryStartDate);
                } catch (Exception any) {
                    throw new ServiceException(messageSource.getMessage("jdbc_error_contract_adjustment_delete", new Object[]{queryStartDate, any.getMessage()}, LocaleContextHolder.getLocale()), any);
                }
                
                String getQuery = "select cst.name customer_name, co.id contract_id, co.job_number, co.name contract_name, co.alt_id contract_alt_id, cu.id pcr_id, cu.alt_id pcr_alt_id, cu.signed_date, cu.effective_date, cu.note"
                		+ " from contract_update cu"
                		+ " inner join contract co on co.id = cu.contract_id"
                		+ " inner join customer cst on cst.id = co.customer_id"
                		+ " where cu.signed_date between :startDate and :endDate";
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("startDate", queryStartDate);
                params.put("endDate", queryEndDate);
                List<SIDataWarehouseContractUpdate> contractUpdates = namedJdbcTemplate.query(getQuery, params, new RowMapper<SIDataWarehouseContractUpdate>() {
                    @Override
                    public SIDataWarehouseContractUpdate mapRow(ResultSet rs, int i) throws SQLException {
                        return new SIDataWarehouseContractUpdate(
                                rs.getString("customer_name"),
                                rs.getLong("contract_id"),
                                rs.getString("job_number"),
                                rs.getString("contract_name"),
                                rs.getString("contract_alt_id"),
                                rs.getLong("pcr_id"),
                                rs.getString("pcr_alt_id"),
                                rs.getDate("signed_date"),
                                rs.getDate("effective_date"),
                                rs.getString("note"),
                                null);
                    }
                });
                
                
                String childQuery = "select csvc.service_id, s.business_model"
                        + " from contract_service csvc "
                		+ " inner join service s on s.id = csvc.service_id"
                        + " inner join contract_update_contract_service cucs on cucs.contract_service_id = csvc.id"
                		+ " where cucs.contract_update_id = :contractUpdateId"
                        + " group by csvc.service_id";
                for(SIDataWarehouseContractUpdate contractUpdate: contractUpdates) {
                	Map<String, Object> childParams = new HashMap<String, Object>();
                	childParams.put("contractUpdateId", contractUpdate.getId());
                    
                    List<String> businessModels = namedJdbcTemplate.query(childQuery, childParams, new RowMapper<String>() {
                        @Override
                        public String mapRow(ResultSet rs, int i) throws SQLException {
                            return new String(rs.getString("business_model"));
                        }
                    });
                    
                    SIDataWarehouseContract.Type type = getTypeFromBusinessModels(businessModels);
                    contractUpdate.setType(type);
                }
                
                log.info("Starting batch save");
                saveBatchContractUpdates(contractUpdates, queryStartDate);
                log.info("Ending batch save");
                
                log.info("Ending Task: " + st.getName());
            } else {
            	log.info("Data Warehouse Sync is not enabled.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException("An exception occurred ... rolling back");
        }
	}
	
	private void saveBatchContractUpdates(final List<SIDataWarehouseContractUpdate> contracts, final Date month) {
		log.info("saving batch contracts with [{}] records", contracts.size());
		int[] results = azureJdbcTemplate.batchUpdate("insert into SI_PCR (Month, Customer, SOW_ID, Job_Number,"
                + " SOW_Name, SOW_Alt_ID, ID, PCR_Alt_ID, Signed_Date, Effective_Date, Note, Type)"
                + " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                int j = 1;
                SIDataWarehouseContractUpdate record = contracts.get(i);
                ps.setDate(j++, new java.sql.Date(month.getTime()));
                ps.setString(j++, record.getCustomerName());
                ps.setInt(j++, record.getSowId().intValue());
                ps.setString(j++, record.getJobNumber());
                ps.setString(j++, record.getSowName());
                ps.setString(j++, record.getSowAltId());
                ps.setInt(j++, record.getId().intValue());
                ps.setString(j++, record.getAltId());
                ps.setDate(j++, new java.sql.Date(record.getSignedDate().getTime()));
                ps.setDate(j++, new java.sql.Date(record.getEffectiveDate().getTime()));
                ps.setString(j++, record.getNote());
                
                String type = null;
                if(record.getType() != null) {
                	type = record.getType().getDescription();
                }
                ps.setString(j++, type);
                //ps.setTimestamp(j++, new java.sql.Timestamp(record.getWorkDate().getTime()));
            }

            @Override
            public int getBatchSize() {
                return contracts.size();
            }
        });
		
		/*
		int count = 1;
        log.info("About to loop through results");
        for(int result : results) {
        	log.info(count + " - Batch Update Result is: [" + result + "]");
        }*/
	}
	
	private SIDataWarehouseContract.Type getTypeFromBusinessModels(List<String> businessModels) {
		SIDataWarehouseContract.Type type = null;
		
		boolean containsCloud = false;
        boolean containsOther = false;
        boolean containsManaged = false;
        boolean containsCSP = false;
        for(String model: businessModels) {
        	if(model.equals("Cloud")) {
        		containsCloud = true;
        	} else if(model.equals("Managed")) {
        		containsManaged = true;
        	} else if(model.equals("Other")) {
        		containsOther = true;
        	} else if(model.equals("CSP")) {
        		containsCSP = true;
        	}
        }
        
        if(containsManaged && containsCloud && containsOther && containsCSP) {
        	type = SIDataWarehouseContract.Type.rimmCloudCSPAndOther;
        } else if (containsManaged && containsOther && containsCSP) {
        	type = SIDataWarehouseContract.Type.rimmCSPAndOther;
        } else if (containsCloud && containsOther && containsCSP) {
        	type = SIDataWarehouseContract.Type.cloudCSPAndOther;
        } else if (containsManaged && containsOther) {
        	type = SIDataWarehouseContract.Type.rimmAndOther;
        } else if (containsManaged && containsCSP) {
        	type = SIDataWarehouseContract.Type.rimmAndCSP;
        } else if (containsManaged && containsCloud) {
        	type = SIDataWarehouseContract.Type.rimmAndCloud;
        } else if (containsCloud && containsOther) {
        	type = SIDataWarehouseContract.Type.cloudAndOther;
        } else if (containsCloud && containsCSP) {
        	type = SIDataWarehouseContract.Type.cloudAndCSP;
        } else if (containsManaged) {
        	type = SIDataWarehouseContract.Type.rimm;
        } else if (containsOther) {
        	type = SIDataWarehouseContract.Type.other;
        } else if (containsCSP) {
        	type = SIDataWarehouseContract.Type.csp;
        } else if (containsCloud) {
        	type = SIDataWarehouseContract.Type.cloud;
        }
		
		return type;
	}
	
	
	
	@Async
	public void updateDataWarehouseCostsforMonthOf(Date importDate) throws ServiceException {
		try {
            ScheduledTask st = applicationDataDaoService.findScheduledTaskByCode(DATA_WAREHOUSE_COST_SYNC_TSK);
            if (st != null && st.getEnabled()) {
                log.info("Running Task: " + st.getName());
                
                if (importDate == null) {
                	importDate = new DateTime().minusMonths(1).toDate();
                	log.info("Import Date was null, so setting it to last month.");
                }
               
                Date queryStartDate = new DateTime(importDate).withZone(DateTimeZone.forID(TZID)).withDayOfMonth(1).withTimeAtStartOfDay().toDate();
                Date queryEndDate = new DateTime(importDate).dayOfMonth().withMaximumValue().withTimeAtStartOfDay().plusHours(23).plusMinutes(59).plusSeconds(59).toDate();
                
                log.info("Deleting records for Month of: " + queryStartDate);
                try {
                    int deleted = azureJdbcTemplate.update("delete from SI_COST where Month = ?", queryStartDate);
                } catch (Exception any) {
                    throw new ServiceException(messageSource.getMessage("jdbc_error_contract_adjustment_delete", new Object[]{queryStartDate, any.getMessage()}, LocaleContextHolder.getLocale()), any);
                }
                
                String getQuery = "select costItem.id, costItem.name, costItem.description,"
                        + " costItem.amount, costItem.quantity, costItem.part_number, costItem.sku,"
                        + " costItem.applied, costItem.customer_id, cust.name as 'customerName', costItem.contract_id, ctr.name as 'contractName', ctr.job_number as 'jobNumber',"
                        + " costItem.location_id, costItem.cost_type, costItem.azure_customer_name, costItem.azure_invoice_id, costItem.azure_subscription_id,"
                        + " costItem.aws_subscription_id, costItem.device_id, costItem.spla_cost_catalog_id,"
                        + " costItem.created, costItem.created_by, costItem.updated, costItem.updated_by,"
                        + " exp.id exp_id, exp.name exp_name, exp.expense_type expense_type, exp.alt_id exp_alt_id, exp.quantity exp_quantity"
                        + " from cost_item costItem"
                        + " left outer join customer cust on costItem.customer_id = cust.id"
                        + " left outer join contract ctr on costItem.contract_id = ctr.id"
                        + " left outer join expense exp on costItem.expense_id_ref = exp.id"
                        + " left outer join spla_cost_catalog splaCat on costItem.spla_cost_catalog_id = splaCat.id"
                        + " where costItem.applied between :startDate and :endDate";
                
                MapSqlParameterSource params = new MapSqlParameterSource();
                params.addValue("startDate", queryStartDate);
                params.addValue("endDate", queryEndDate);
                
                /*
                Set<String> costTypes = new HashSet<String>();
                costTypes.add(CostItem.CostType.aws.name());
                costTypes.add(CostItem.CostType.azure.name());
                costTypes.add(CostItem.CostType.o365.name());
                costTypes.add(CostItem.CostType.spla.name());
                params.addValue("costTypes", costTypes);*/
                
                List<SIDataWarehouseCostItem> costItems = namedJdbcTemplate.query(getQuery, params, new RowMapper<SIDataWarehouseCostItem>() {
                    @Override
                    public SIDataWarehouseCostItem mapRow(ResultSet rs, int i) throws SQLException {
                        return new SIDataWarehouseCostItem(
                        		rs.getLong("id"),
                        		rs.getString("name"),
                        		rs.getBigDecimal("amount"),
                        		rs.getInt("quantity"),
                        		rs.getDate("applied"),
                        		rs.getLong("customer_id"),
                        		rs.getString("customerName"),
                        		rs.getLong("contract_id"),
                        		rs.getString("contractName"),
                                rs.getString("jobNumber"),
                                CostItem.CostType.valueOf(rs.getString("cost_type")));
                    }
                });
                
                log.info("Found " + costItems.size() + " Cost Items in SI");
                
                log.info("Starting batch save");
                saveBatchCosts(costItems, queryStartDate);
                log.info("Ending batch save");
                
                log.info("Ending Task: " + st.getName());
            } else {
            	log.info("Data Warehouse Sync is not enabled.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException("An exception occurred ... rolling back");
        }
	}
	
	private void saveBatchCosts(final List<SIDataWarehouseCostItem> costs, final Date month) {
		log.info("saving batch costs with [{}] records", costs.size());
		int[] results = azureJdbcTemplate.batchUpdate("insert into SI_COST (Month, Customer, ID, Job_Number,"
                + " SOW_Name, Cost_Name, Quantity, Cost, Type)"
                + " values (?, ?, ?, ?, ?, ?, ?, ?, ?)", new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                int j = 1;
                SIDataWarehouseCostItem record = costs.get(i);
                ps.setDate(j++, new java.sql.Date(month.getTime()));
                ps.setString(j++, record.getCustomerName());
                ps.setInt(j++, record.getId().intValue());
                ps.setString(j++, record.getJobNumber());
                ps.setString(j++, record.getContractName());
                ps.setString(j++, record.getName());
                ps.setInt(j++, record.getQuantity());
                ps.setBigDecimal(j++, record.getAmount());
                
                String type = null;
                if(record.getCostType() != null) {
                	type = record.getCostType().getDescription();
                }
                ps.setString(j++, type);
                //ps.setTimestamp(j++, new java.sql.Timestamp(record.getWorkDate().getTime()));
            }

            @Override
            public int getBatchSize() {
                return costs.size();
            }
        });
		
		/*
		int count = 1;
        log.info("About to loop through results");
        for(int result : results) {
        	log.info(count + " - Batch Update Result is: [" + result + "]");
        }*/
	}
	
}
