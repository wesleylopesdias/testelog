package com.logicalis.serviceinsight.web.controller;

import com.logicalis.serviceinsight.dao.AssetItem;
import com.logicalis.serviceinsight.dao.CostItem;
import com.logicalis.serviceinsight.data.Contract;
import com.logicalis.serviceinsight.data.ContractUpdate;
import com.logicalis.serviceinsight.data.Customer;
import com.logicalis.serviceinsight.data.ScheduledTask;
import com.logicalis.serviceinsight.data.Service;
import com.logicalis.serviceinsight.data.ServiceAlign;
import com.logicalis.serviceinsight.scheduled.ChronosScheduled;
import com.logicalis.serviceinsight.scheduled.UMPScheduler;
import com.logicalis.serviceinsight.service.ApplicationDataDaoService;
import com.logicalis.serviceinsight.service.ContractDaoService;
import com.logicalis.serviceinsight.service.CostDaoService;
import com.logicalis.serviceinsight.service.CostService;
import com.logicalis.serviceinsight.service.PricingIntegrationService;
import com.logicalis.serviceinsight.service.PricingSheetService;
import com.logicalis.serviceinsight.service.ServiceException;
import com.logicalis.serviceinsight.service.ServiceNowService;
import com.logicalis.serviceinsight.service.ServiceUsageService;
import com.logicalis.serviceinsight.service.UserDaoService;
import com.logicalis.serviceinsight.util.VersionUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 *
 * @author poneil
 */
@Controller
@RequestMapping("/admin")
public class AdminController extends BaseController {

    @Autowired
    ServiceUsageService serviceUsageService;
    @Autowired
    ContractDaoService contractDaoService;
    @Autowired
    CostDaoService costDaoService;
    @Autowired
    CostService costService;
    @Autowired
    PricingSheetService pricingSheetService;
    @Autowired
    ChronosScheduled chronosScheduled;
    @Autowired
    UMPScheduler umpScheduler;
    @Autowired
    PricingIntegrationService pricingIntegrationService;
    @Autowired
    ServiceNowService serviceNowService;
    @Autowired
    UserDaoService userDaoService;
    @Autowired
    ApplicationDataDaoService applicationDataDaoService;
    private JdbcTemplate jdbcTemplate;
    private static final String SN_CONTRACT_SYNC_TSK = "sn_contract_sync";
    private static final String SN_CONTRACT_CI_SYNC_TSK = "sn_contract_ci_sync";
    

    @Autowired
    void setChronosDataSource(DataSource chronosDataSource, DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @RequestMapping(value = "/importosp", method = RequestMethod.GET)
    public String importosp() {
        serviceUsageService.serviceConnect();
        return "admin/uicontracts";
    }

    @RequestMapping(value = "/onecustomer", method = RequestMethod.GET)
    public String oneCustomer() {
        pricingIntegrationService.syncRemoteCustomers();
        return "redirect:/ok";
    }
    
    @RequestMapping(value = "/usernamegeneration", method = RequestMethod.GET)
    public String userNameGeneration() {
        userDaoService.generateNamesFromEmails();
        return "redirect:/ok";
    }
    
    @RequestMapping(value = "/embeddeddevices", method = RequestMethod.GET)
    public String embeddedDeviceGeneration() {
    	try {
    		contractDaoService.generateEmbeddedServicesForSystem();
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
        return "redirect:/ok";
    }
    
    @RequestMapping(value = "/devicecounts", method = RequestMethod.GET)
    public String generateDeviceCounts() {
    	try {
    		costService.runGenerateAllDeviceCounts();
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
        return "redirect:/ok";
    }
    
    @RequestMapping(value = "/devicecounts/month", method = RequestMethod.GET)
    public String generateDeviceCountsForMonth(@RequestParam(value = "month", required = true) @org.springframework.format.annotation.DateTimeFormat(pattern = "MM/yyyy") Date month) {
    	try {
    		DateTime jobDateTime = new DateTime(month).withZone(DateTimeZone.forID(TZID)).withTime(0,0,0,0).dayOfMonth().withMinimumValue().plusDays(1);
    		Integer runMonth = jobDateTime.getMonthOfYear();
        	String year = String.valueOf(jobDateTime.getYear());
        	
    		costService.generateDeviceCounts(runMonth, year);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
        return "redirect:/ok";
    }
    
    @RequestMapping(value = "/copymappings", method = RequestMethod.GET)
    public String copyCostMappings() {
        contractDaoService.copyCostCategoryServiceMappingsToCostCategoryDevice();
        return "redirect:/ok";
    }
    
    @RequestMapping(value = "/pricing/devices", method = RequestMethod.GET)
    public String syncDevicesFromPricing() {
    	try {
    		pricingIntegrationService.devicePricingSync();
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
        return "redirect:/ok";
    }
    
    @RequestMapping(value = "/logrimmcontracts", method = RequestMethod.GET)
    public String logRimmContracts(@RequestParam(value = "month", required = true) @org.springframework.format.annotation.DateTimeFormat(pattern = "MM/yyyy") Date month) {
    	DateTime startDateTime = new DateTime(month).withZone(DateTimeZone.forID(TZID))
                .withTime(0,0,0,0)
                .dayOfMonth()
                .withMinimumValue();
    	Date startDate = startDateTime.toDate();
    	Date endDate = startDateTime
                .dayOfMonth().withMaximumValue()
                .withTimeAtStartOfDay().plusHours(23).plusMinutes(59).plusSeconds(59).toDate();
    	log.info("About to get contracts for " + startDate + " -- " + endDate);
    	List<Contract> contracts = contractDaoService.contractsForDates(startDate, endDate);
    	List<Long> contractIds = new ArrayList<Long>();
    	for(Contract contract: contracts) {
    		log.info("CID: " + contract.getId() + " -- Alt ID: " + contract.getAltId());
    		contractIds.add(contract.getId());
    	}
    	
    	log.info("Contract IDs: " + contractIds);
    	
    	log.info("About to get contract updates for " + startDate + " -- " + endDate);
    	List<ContractUpdate> contractUpdates = contractDaoService.contractUpdatesForDates(startDate, endDate);
    	List<Long> contractUpdateIds = new ArrayList<Long>();
    	for(ContractUpdate contractUpdate: contractUpdates) {
    		log.info("CID: " + contractUpdate.getContractId() + " -- CUID: " + contractUpdate.getId() + " -- Alt ID: " + contractUpdate.getAltId());
    		contractUpdateIds.add(contractUpdate.getId());
    	}
    	
    	log.info("Contract Update IDs: " + contractUpdateIds);
    	
        return "redirect:/ok";
    }
    
    @RequestMapping(value = "/servicealign", method = RequestMethod.GET)
    public String serviceAlign(Model uiModel) {
    	uiModel.addAttribute("devices", applicationDataDaoService.devices(Boolean.FALSE));
    	uiModel.addAttribute("services", contractDaoService.services());
        return "admin/servicealign";
    }
    
    @ResponseBody
    @ResponseStatus(CREATED)
    @RequestMapping(value="/servicealign", method = RequestMethod.POST, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse serviceAlignment(@RequestBody ServiceAlign serviceAlign) {
    	try {
            applicationDataDaoService.serviceAlignment(serviceAlign);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_new_device", null, LocaleContextHolder.getLocale()));
        } catch (ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_new_device", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch (Exception e) {
            log.error("error saving a Device", e);
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_new_device", null, LocaleContextHolder.getLocale()));
        }
    }
    
    /**
     * Use this method to kick off importing Chronos data since the beginning of time
     * or the last import date.
     * @return 
     */
    @RequestMapping(value = "/importChronosData", method = RequestMethod.GET)
    public String importChronosData() {
        chronosScheduled.importChronosData();
        return "redirect:/ok";
    }

    /**
     * Use this method to kick off calculating indirect Labor Unit Costs
     * either when there are none existing or if the latest month needs to be created.
     * @return 
     */
    @RequestMapping(value = "/indirectLaborUnitCost", method = RequestMethod.GET)
    public String indirectLaborUnitCost() {
        chronosScheduled.indirectLaborUnitCost();
        return "redirect:/ok";
    }

    /**
     * use this method to update all cost and asset items and recalculate unit costs, including labor,
     * from the beginning.
     * 
     * Before calling this method, DELETE ALL UNIT COSTS (unit_cost table). they are re-created.
     * the reason for this is that normally, in the UI, when Assets and Costs are updated, they're previous
     * $ contributions to unit cost are decremented from existing Unit Costs. In this method, below,
     * that is not done on the assumption that the entire table needs to be rebuilt.
     * 
     * @return ok
     */
    @RequestMapping(value = "/recalculateUnitCosts", method = RequestMethod.GET)
    public String recalculateUnitCosts() {
        for (AssetItem assetItem : contractDaoService.assetItems()) {
            try {
                log.debug("UPDATING {}", assetItem.toString());
                contractDaoService.updateAssetItem(assetItem, false);
            } catch (ServiceException ex) {
                Logger.getLogger(AdminController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        for (CostItem costItem : contractDaoService.costItems()) {
            try {
                log.debug("UPDATING {}", costItem.toString());
                contractDaoService.updateCostItem(costItem, false);
            } catch (ServiceException ex) {
                Logger.getLogger(AdminController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return updateLaborUnitCosts();
    }

    /**
     * This method runs through ALL of the existing unit costs and updates the Device
     * unit counts. The service method called is Scheduled to run once a day, but this
     * Controller method can be called to kick it off, too.
     * 
     * @return ok
     */
    @RequestMapping(value = "/updateDeviceUnitCounts", method = RequestMethod.GET)
    public String updateDeviceUnitCounts() {
        try {
            contractDaoService.updateUnitCostServiceTotals();
        } catch (ServiceException ex) {
            Logger.getLogger(AdminController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "redirect:/ok";
    }

    /**
     * Use this method to just update unit costs for labor only. does not require
     * the Unit Cost table to be empty because all that is done is the labor is re-read
     * and the labor total cost is over-written in each unit cost record.
     * 
     * @return ok
     */
    @RequestMapping(value = "/updateLaborUnitCosts", method = RequestMethod.GET)
    public String updateLaborUnitCosts() {
        DateTime startDate = new DateTime(jdbcTemplate.queryForObject("select min(work_date) from grouped_labor_data", Date.class));
        DateTime endDate = new DateTime(jdbcTemplate.queryForObject("select max(work_date) from grouped_labor_data", Date.class));
        while (startDate.isBefore(endDate)) {
            log.debug("applying categorized labor for date {}", DateTimeFormat.forPattern("yyyy-MM-dd").print(startDate));
            chronosScheduled.applyUnitCostsForCategorizedLabor(startDate);
            startDate = startDate.plusMonths(1);
        }
        return "redirect:/ok";
    }

    @RequestMapping(value = "/uicontracts", method = RequestMethod.GET)
    public String uicontracts() {
        return "admin/uicontracts";
    }

    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/customer", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public Customer customer() {

        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("Arcadia");
        customer.setContractCount(2);
        return customer;
    }

    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/sow", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public Contract contract() {

        Date now = new Date();
        Contract contract = new Contract();
        contract.setId(1L);
        contract.setAltId("ABC-123");
        contract.setName("ABC SOW");
        contract.setStartDate(now);
        contract.setEndDate(new DateTime(now).plusMonths(36).toDate());
        contract.setServiceCount(3);
        return contract;
    }

    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/contractservice", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public Service service() {

        Service service = new Service();
        service.setId(1L);
        service.setCode("01-005-01");
        service.setOspId("113");
        service.setVersion(VersionUtil.parseVersion("1.0.1"));
        service.setContractId(1L);
        service.setName("Enterprise Cloud - Virtualization & Cloud - x86");
        service.setLineitemCount(3);
        service.setOnetimeRevenue(new BigDecimal(2000));
        service.setRecurringRevenue(new BigDecimal(1000));
        service.setStartDate(new Date());
        service.setEndDate(new DateTime().plusMonths(36).toDate());
        return service;
    }
    
    /*
    @RequestMapping(value = "/buildpricingsheets", method = RequestMethod.GET)
    public String buildPricingSheetsForExistingContracts() {
        try {
        	List<Contract> contracts = contractDaoService.contracts();
        	for(Contract contract : contracts) {
        		PricingSheet pricingSheet = new PricingSheet();
        		pricingSheet.setContractId(contract.getId());
        		pricingSheet.setOsmSyncEnabled(false);
        		pricingSheetService.savePricingSheet(pricingSheet);
        		log.info("Pricing Sheet Created for Contract ID [" + contract.getId() + "]");
        	}
        } catch (Exception e) {
        	e.printStackTrace();
        }
        return "admin/uiinvoices";
    }*/
    
    @RequestMapping(value = "/osm/contractsync", method = RequestMethod.GET)
    public String syncContractsWithOSM() {
	    try {
	        ScheduledTask st = applicationDataDaoService.findScheduledTaskByCode(SN_CONTRACT_SYNC_TSK);
	        if (st != null && st.getEnabled()) {
	            log.info("Running Task: " + st.getName());
	            serviceNowService.syncContractsFromServiceNow();
	            log.info("Ending Task: " + st.getName());
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return "admin/osm/contractsync";
    }
    
    @RequestMapping(value = "/osm/contractcisync", method = RequestMethod.GET)
    public String syncContractCIsWithOSM() {
	    try {
	        ScheduledTask st = applicationDataDaoService.findScheduledTaskByCode(SN_CONTRACT_CI_SYNC_TSK);
	        if (st != null && st.getEnabled()) {
	            log.info("Running Task: " + st.getName());
	            serviceNowService.syncContractCIsFromServiceNow();
	            log.info("Ending Task: " + st.getName());
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return "admin/osm/contraccitsync";
    }
    
    @RequestMapping(value = "/ump/cisync", method = RequestMethod.GET)
    public String syncCIsWithUMP() {
	    umpScheduler.syncCIsFromUMP();
	    return "redirect:/ok";
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/devicesync", method = RequestMethod.GET)
    public String pricingDeviceSync(@RequestParam(value = "override", required = false) Boolean override) throws ServiceException {
    	if(override == null) {
    		override = false;
    	}
        pricingIntegrationService.deviceSync(override);
        return "admin/devicesync";
    }
}
