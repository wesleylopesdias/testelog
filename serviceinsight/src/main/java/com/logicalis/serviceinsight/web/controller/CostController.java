package com.logicalis.serviceinsight.web.controller;

import com.logicalis.serviceinsight.dao.CostItem;
import com.logicalis.serviceinsight.dao.ExpenseCategory;
import com.logicalis.serviceinsight.dao.UnitCost;
import com.logicalis.serviceinsight.data.CostAllocation;
import com.logicalis.serviceinsight.data.CostFractionRecord;
import com.logicalis.serviceinsight.data.Device;
import com.logicalis.serviceinsight.data.UnallocatedExpense;
import com.logicalis.serviceinsight.data.UnitCostDetails;
import com.logicalis.serviceinsight.representation.CostAnalysisCustomerRecord;
import com.logicalis.serviceinsight.representation.CostAnalysisExpenseCategoryRecord;
import com.logicalis.serviceinsight.representation.CostAnalysisSummaryRecord;
import com.logicalis.serviceinsight.representation.CostItemAnalysisWrapper;
import com.logicalis.serviceinsight.representation.CostItemTypeSubType;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.logicalis.serviceinsight.service.ApplicationDataDaoService;
import com.logicalis.serviceinsight.service.ContractDaoService;
import com.logicalis.serviceinsight.service.CostAllocationService;
import com.logicalis.serviceinsight.service.CostDaoService;
import com.logicalis.serviceinsight.service.CostService;
import com.logicalis.serviceinsight.service.ServiceException;
import com.logicalis.serviceinsight.service.ServiceUsageService;
import com.logicalis.serviceinsight.web.config.CloudBillingSecurityConfig.Role;
import com.logicalis.serviceinsight.service.RelatedDataException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

import java.util.List;
import java.util.Map;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.convert.ConversionService;
import org.springframework.format.annotation.DateTimeFormat;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping("/costs")
public class CostController extends BaseController {

    @Autowired
    ServiceUsageService serviceUsageService;
    @Autowired
    ContractDaoService contractDaoService;
    @Autowired
    ConversionService conversionService;
    @Autowired
    ApplicationDataDaoService applicationDataService;
    @Autowired
    CostService costService;
    @Autowired
    CostDaoService costDaoService;
    @Autowired
    CostAllocationService costAllocationService;
    static final SimpleDateFormat costAllocationDateFormatter = new SimpleDateFormat("MM/dd/yyyy");

    @RequestMapping(method = RequestMethod.GET)
    public String redirectToCosts(Model uiModel) throws ServiceException {
    	return "redirect:/costs/list";
    }
    
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String showCosts(Model uiModel) throws ServiceException {
        //uiModel.addAttribute("assets", contractDaoService.assetItems());
    	List<ExpenseCategory> categories = contractDaoService.expenseCategories();
    	Collections.sort(categories, ExpenseCategory.ExpenseCategoryNameComparator);
        uiModel.addAttribute("expenseCategories", categories);
        uiModel.addAttribute("locations", applicationDataService.locations(null));
        //uiModel.addAttribute("businessUnits",contractDaoService.businessUnits());
        uiModel.addAttribute("customers", contractDaoService.customers(null, true, false));
        uiModel.addAttribute("costTypes", Arrays.asList(CostItem.CostType.values()));
        uiModel.addAttribute("costSubTypes", Arrays.asList(CostItem.CostSubType.values()));
        uiModel.addAttribute("services", contractDaoService.services());
        return "costs/index";
    }

    @RequestMapping(value = "/categories", method = RequestMethod.GET)
    public String showCostCategories(Model uiModel) throws ServiceException {
    	List<ExpenseCategory> categories = contractDaoService.expenseCategories();
    	Collections.sort(categories, ExpenseCategory.ExpenseCategoryNameComparator);
        uiModel.addAttribute("expenseCategories", categories);
        return "costs/categories/index";
    }
    
    @RequestMapping(value = "/allocation", method = RequestMethod.GET)
    public String showCostAllocations(Model uiModel) throws ServiceException {
        uiModel.addAttribute("devices", applicationDataService.findDevicesForCostAllocation(new Date()));
        return "costs/allocations";
    }
    
    @RequestMapping(value = "/analysis", method = RequestMethod.GET)
    public String costAnalysis(Model uiModel) throws ServiceException {
        return "costs/analysis";
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value="/analysisData",method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public Map<String, Object> costAnalysis(@RequestParam(value = "month", required = true) @DateTimeFormat(pattern = "MM/yyyy") Date month) throws ServiceException {
        
        Map<String, Object> data = new HashMap<String, Object>();
        
        /** we need data for cost tables in the following three formats:
         *
         * summary:
         * Type / Subtype / prev month $ / current month $ / next month $
         *
         * by customer:
         * Type / Subtype / Customer / prev month $ / current month $ / next month $
         *
         * by expense category:
         * Type / Subtype / Expense Category / prev month $ / current month $ / next month $
         */
        List<CostItemTypeSubType> costItemTypeSubTypes = costService.refCostItemTypeSubTypes();
        costItemTypeSubTypes.add(new CostItemTypeSubType("direct", "Direct Labor", null, null));
        costItemTypeSubTypes.add(new CostItemTypeSubType("indirect", "Indirect Labor", null, null));
        
        CostItemAnalysisWrapper previousWrapper = costService.costItemAnalysis(new DateTime(month).minusMonths(1));
        CostItemAnalysisWrapper currentWrapper = costService.costItemAnalysis(new DateTime(month));
        CostItemAnalysisWrapper nextWrapper = costService.costItemAnalysis(new DateTime(month).plusMonths(1));
        
        /**
         * merge previous, current and next for SUMMMARY data
         */
        List<CostAnalysisSummaryRecord> summaryRecords = new ArrayList<CostAnalysisSummaryRecord>();
        for (CostItemTypeSubType item : costItemTypeSubTypes) {
            CostAnalysisSummaryRecord summaryRecord = new CostAnalysisSummaryRecord(item);
            for (CostItemAnalysisWrapper.SummaryRecord cost : previousWrapper.getCostsByTypeSubType()) {
                if (item.getKey().equals(cost.getKey())) {
                    summaryRecord.setPrevious(cost.getCost());
                }
            }
            for (CostItemAnalysisWrapper.SummaryRecord cost : currentWrapper.getCostsByTypeSubType()) {
                if (item.getKey().equals(cost.getKey())) {
                    summaryRecord.setCurrent(cost.getCost());
                }
            }
            for (CostItemAnalysisWrapper.SummaryRecord cost : nextWrapper.getCostsByTypeSubType()) {
                if (item.getKey().equals(cost.getKey())) {
                    summaryRecord.setNext(cost.getCost());
                }
            }
            summaryRecords.add(summaryRecord);
        }
        data.put("summaryRecords", summaryRecords);
        
        /**
         * merge previous, current and next for CUSTOMER data
         */
        List<CostAnalysisCustomerRecord> customerRecords = new ArrayList<CostAnalysisCustomerRecord>();
        for (CostItemTypeSubType item : costItemTypeSubTypes) {
            boolean created = false;
            for (CostItemAnalysisWrapper.SummaryRecordByCustomer cost : previousWrapper.getCostsByTypeSubTypeAndCustomer()) {
                if (item.getKey().equals(cost.getKey())) {
                    CostAnalysisCustomerRecord customerRecord = new CostAnalysisCustomerRecord(item, cost.getCustomer());
                    customerRecord.setPrevious(cost.getCost());
                    customerRecords.add(customerRecord);
                    created = true;
                }
            }
            for (CostItemAnalysisWrapper.SummaryRecordByCustomer cost : currentWrapper.getCostsByTypeSubTypeAndCustomer()) {
                if (item.getKey().equals(cost.getKey())) {
                    CostAnalysisCustomerRecord customerRecord = new CostAnalysisCustomerRecord(item, cost.getCustomer());
                    int index = customerRecords.indexOf(customerRecord);
                    if (index > -1) {
                        customerRecord = customerRecords.get(index);
                    } else {
                        customerRecords.add(customerRecord);
                    }
                    customerRecord.setCurrent(cost.getCost());
                    created = true;
                }
            }
            for (CostItemAnalysisWrapper.SummaryRecordByCustomer cost : nextWrapper.getCostsByTypeSubTypeAndCustomer()) {
                if (item.getKey().equals(cost.getKey())) {
                    CostAnalysisCustomerRecord customerRecord = new CostAnalysisCustomerRecord(item, cost.getCustomer());
                    int index = customerRecords.indexOf(customerRecord);
                    if (index > -1) {
                        customerRecord = customerRecords.get(index);
                    } else {
                        customerRecords.add(customerRecord);
                    }
                    customerRecord.setNext(cost.getCost());
                    created = true;
                }
            }
            if (!created && !"direct".equals(item.getKey()) && !"indirect".equals(item.getKey())) {
                customerRecords.add(new CostAnalysisCustomerRecord(item, null));
            }
        }
        data.put("customerRecords", customerRecords);
        
        /**
         * merge previous, current and next for EXPENSE CATEGORY data
         */
        List<CostAnalysisExpenseCategoryRecord> expenseCategoryRecords = new ArrayList<CostAnalysisExpenseCategoryRecord>();
        for (CostItemTypeSubType item : costItemTypeSubTypes) {
            boolean created = false;
            for (CostItemAnalysisWrapper.SummaryRecordByCostCategory cost : previousWrapper.getCostsByTypeSubTypeAndCostCategory()) {
                if (item.getKey().equals(cost.getKey())) {
                    CostAnalysisExpenseCategoryRecord expenseCategoryRecord = new CostAnalysisExpenseCategoryRecord(item, cost.getParentCostCategory(), cost.getCostCategory());
                    expenseCategoryRecord.setPrevious(cost.getCost());
                    expenseCategoryRecords.add(expenseCategoryRecord);
                    created = true;
                }
            }
            for (CostItemAnalysisWrapper.SummaryRecordByCostCategory cost : currentWrapper.getCostsByTypeSubTypeAndCostCategory()) {
                if (item.getKey().equals(cost.getKey())) {
                    CostAnalysisExpenseCategoryRecord expenseCategoryRecord = new CostAnalysisExpenseCategoryRecord(item, cost.getParentCostCategory(), cost.getCostCategory());
                    int index = expenseCategoryRecords.indexOf(expenseCategoryRecord);
                    if (index > -1) {
                        expenseCategoryRecord = expenseCategoryRecords.get(index);
                    } else {
                        expenseCategoryRecords.add(expenseCategoryRecord);
                    }
                    expenseCategoryRecord.setCurrent(cost.getCost());
                    created = true;
                }
            }
            for (CostItemAnalysisWrapper.SummaryRecordByCostCategory cost : nextWrapper.getCostsByTypeSubTypeAndCostCategory()) {
                if (item.getKey().equals(cost.getKey())) {
                    CostAnalysisExpenseCategoryRecord expenseCategoryRecord = new CostAnalysisExpenseCategoryRecord(item, cost.getParentCostCategory(), cost.getCostCategory());
                    int index = expenseCategoryRecords.indexOf(expenseCategoryRecord);
                    if (index > -1) {
                        expenseCategoryRecord = expenseCategoryRecords.get(index);
                    } else {
                        expenseCategoryRecords.add(expenseCategoryRecord);
                    }
                    expenseCategoryRecord.setNext(cost.getCost());
                    created = true;
                }
            }
            if (!created && !"direct".equals(item.getKey()) && !"indirect".equals(item.getKey())) {
                expenseCategoryRecords.add(new CostAnalysisExpenseCategoryRecord(item, null, null));
            }
        }
        data.put("expenseCategoryRecords", expenseCategoryRecords);
        
        return data;
    }
    
    /**
     * Primes the cost allocation view with a Month related to a specific Cost Allocation
     * looked up by the ID of a member CostAllocationLineItem
     * 
     * @param uiModel
     * @param liid
     * @return
     * @throws ServiceException 
     */
    @RequestMapping(value = "/allocation/forlineitem/{liid}", method = RequestMethod.GET)
    public String showCostAllocationsForMonth(Model uiModel, @PathVariable("liid") Long liid) throws ServiceException {
        
        Date forMonthOf = null;
        try {
            forMonthOf = jdbcTemplate.queryForObject(
                    "select ca.month from" +
                    " cost_allocation_lineitem cali" +
                    " inner join cost_allocation ca on cali.cost_allocation_id = ca.id" +
                    " where cali.id = ?", Date.class, new Object[]{liid});
        } catch (Exception any) {
            log.warn(any.getMessage());
        }
        if (forMonthOf != null) {
            uiModel.addAttribute("forMonthOf", costAllocationDateFormatter.format(forMonthOf));
            uiModel.addAttribute("devices", applicationDataService.findDevicesForCostAllocation(forMonthOf));
        } else {
            uiModel.addAttribute("devices", applicationDataService.findDevicesForCostAllocation(new Date()));
        }
        return "costs/allocations";
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value="/allocation/devices", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public List<Device> allocationDevices(@RequestParam(value = "month", required = true) @DateTimeFormat(pattern = "MM/dd/yyyy") Date month) {
        return applicationDataService.findDevicesForCostAllocation(month);
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value="/allocation/{id}", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public CostAllocation costAllocation(@PathVariable("id") Long id) throws ServiceException {
        return costAllocationService.costAllocation(id);
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value="/allocation", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public CostAllocation costAllocation(@RequestParam(value = "month", required = true) @DateTimeFormat(pattern = "MM/dd/yyyy") Date month) throws ServiceException {
    	CostAllocation allocation = null;
    	allocation = costAllocationService.costAllocationByMonth(month);
    	
    	//we'll return an empty one for the UI to use to see if this fixes the bug in test
    	if(allocation == null) {
    		allocation = new CostAllocation();
    		allocation.setMonth(month);
        	allocation.setStatus(CostAllocation.Status.open);
        	
        	List<UnallocatedExpense> unallocatedExpenses = costDaoService.unallocatedExpensesForMonth(month);
            allocation.setUnallocatedExpenses(unallocatedExpenses);
            
            Date leftDate = new DateTime(month).withZone(DateTimeZone.forID(TZID)).withDayOfMonth(1).withTimeAtStartOfDay().toDate();
            Date rightDate = new DateTime(leftDate).withTimeAtStartOfDay().plusHours(23).plusMinutes(59).plusSeconds(59).withZone(DateTimeZone.forID(TZID)).toDate();
            List<CostItem> dedicatedCosts = contractDaoService.findCostItemsByCustomerIdAndTypeAndPeriod(CostItem.CostType.depreciated, CostItem.CostSubType.dedicated, null, leftDate, rightDate);
            allocation.setDedicatedCosts(dedicatedCosts);
        	
        	allocation = costAllocationService.calculate(allocation);
    	}
    	
    	return allocation;
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value="/allocation/calculate", method = RequestMethod.POST, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public CostAllocation costAllocation(@RequestBody CostAllocation costAllocation) throws ServiceException {
        return costAllocationService.calculate(costAllocation);
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value="/allocation/import", method = RequestMethod.POST, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse importCostAllocation(@RequestBody CostAllocation costAllocation, @RequestParam(value = "month", required = true) @DateTimeFormat(pattern = "MM/dd/yyyy") Date month) throws ServiceException {
    	try {
        	costAllocationService.importCostAllocation(costAllocation, month);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_new_costallocation", null, LocaleContextHolder.getLocale()));
        } catch(ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_new_costallocation", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch(Exception e) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_costallocation", null, LocaleContextHolder.getLocale()));
        }
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value="/allocation", method = RequestMethod.POST, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse saveCostAllocation(@RequestBody CostAllocation costAllocation) throws ServiceException {
        try {
        	Long id = costAllocationService.saveCostAllocation(costAllocation);
        	List<Long> ids = new ArrayList<Long>();
        	ids.add(id);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_new_costallocation", new Object[]{id}, LocaleContextHolder.getLocale()), ids, Long.class.getName());
        } catch(ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_new_costallocation", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch(Exception e) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_costallocation", null, LocaleContextHolder.getLocale()));
        }
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value="/allocation", method = RequestMethod.PUT, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse updateCostAllocation(@RequestBody CostAllocation costAllocation) throws ServiceException {
        try {
        	costAllocationService.updateCostAllocation(costAllocation);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_update_costallocation", new Object[]{costAllocation.getId()}, LocaleContextHolder.getLocale()));
        } catch(ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_update_costallocation", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch(Exception e) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_costallocation", null, LocaleContextHolder.getLocale()));
        }
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value="/allocation/generate/{id}", method = RequestMethod.POST, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse generateCostAllocationCosts(@PathVariable("id") Long id) throws ServiceException {
        try {
            costAllocationService.generateCostItemsFromCostAllocation(id);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_generate_costallocation", new Object[]{id}, LocaleContextHolder.getLocale()));
        } catch(ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_generate_costallocation", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch(Exception e) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_costallocation", null, LocaleContextHolder.getLocale()));
        }
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value="/unitCosts", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public Map<String, UnitCost> unitCosts(@RequestParam(value = "custid", required = false) Long custId,
            @RequestParam(value = "exid", required = true) Integer id,
            @RequestParam(value = "sd", required = true) @DateTimeFormat(pattern = "MM/yyyy") Date startDate,
            @RequestParam(value = "ed", required = true) @DateTimeFormat(pattern = "MM/yyyy") Date endDate) {
        return costDaoService.unitCostByExpenseCategoryAndDateRange(custId, id, new DateTime(startDate), new DateTime(endDate));
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value="/unitCostDetails", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public UnitCostDetails unitCostDetails(@RequestParam(value = "custid", required = false) Long custId,
            @RequestParam(value = "exid", required = true) Integer id,
            @RequestParam(value = "ad", required = true) @DateTimeFormat(pattern = "MM/yyyy") Date appliedDate) {
        return contractDaoService.unitCostDetailsForAppliedDate(custId, id, new DateTime(appliedDate));
    }
}
