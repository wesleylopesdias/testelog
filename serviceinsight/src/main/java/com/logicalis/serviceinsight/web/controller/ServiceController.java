package com.logicalis.serviceinsight.web.controller;

import com.logicalis.serviceinsight.dao.ExpenseCategory;
import com.logicalis.serviceinsight.dao.ServiceExpenseCategory;
import com.logicalis.serviceinsight.data.ContractServiceSubscription;
import com.logicalis.serviceinsight.data.Service;
import com.logicalis.serviceinsight.representation.AWSBillingPeriodWrapper;
import com.logicalis.serviceinsight.representation.AzureBillingPeriodWrapper;
import com.logicalis.serviceinsight.service.ContractDaoService;
import com.logicalis.serviceinsight.service.CostDaoService;
import com.logicalis.serviceinsight.service.CostService;
import com.logicalis.serviceinsight.service.DataWarehouseService;
import com.logicalis.serviceinsight.service.PricingIntegrationService;
import com.logicalis.serviceinsight.service.RevenueService;
import com.logicalis.serviceinsight.service.ServiceException;
import com.logicalis.serviceinsight.service.ServiceUsageService;

import java.util.ArrayList;
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

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
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
@RequestMapping("/services")
public class ServiceController extends BaseController {

    @Autowired
    ServiceUsageService serviceUsageService;
    @Autowired
    RevenueService revenueService;
    @Autowired
    ConversionService conversionService;
    @Autowired
    ContractDaoService contractDaoService;
    @Autowired
    CostDaoService costDaoService;
    @Autowired
    CostService costService;
    @Autowired
    DataWarehouseService dataWarehouseService;
    @Autowired
    PricingIntegrationService pricingIntegrationService;
    
    @RequestMapping(method = RequestMethod.GET)
    public String showServices(Model uiModel) throws ServiceException {
        //right now we default to the cost mappings section
    	return showCostMappings(uiModel);
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public List<Service> services() {
        return contractDaoService.servicesAndExpenseCategoryCount();
    }
    
    @RequestMapping(value = "/costmappings", method = RequestMethod.GET)
    public String showCostMappings(Model uiModel) throws ServiceException {
    	List<ExpenseCategory> categories = contractDaoService.expenseCategories();
    	Collections.sort(categories, ExpenseCategory.ExpenseCategoryNameComparator);
        uiModel.addAttribute("expenseCategories", categories);
        return "services/costmappings/index";
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/revenue/{ospId}/{month}/{year}", method = RequestMethod.GET)
    public List<Service> monthlyServiceRevenue(
            @PathVariable("ospId") Long ospId, @PathVariable("month") Integer month,
            @PathVariable("year") String year) {
        return revenueService.serviceRevenueRollupForMonthOf(ospId, month, year);
    }

    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/revenue/{ospId}/{year}", method = RequestMethod.GET)
    public Map<String, List<Service>> yearlyServiceRevenue(
            @PathVariable("ospId") Long ospId, @PathVariable("year") String year) {
        return revenueService.serviceRevenueForYear(ospId, year);
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/costmappings/{ospId}", method = RequestMethod.GET)
    public List<ServiceExpenseCategory> showCostMappings(@PathVariable("ospId") Long ospId) {
        return costDaoService.serviceExpenseCategories(ospId);
    }

    //used to kick off all monthly CSAs
    /*
    @RequestMapping(value = "/azure/csa", method = RequestMethod.GET)
    public String pctest(@RequestParam(value = "sd", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Date startDate,
            @RequestParam(value = "ed", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Date endDate) {
        serviceUsageService.syncCustomerSubscriptions(new DateTime(startDate).withZone(DateTimeZone.forID(TZID)),
                new DateTime(endDate).withZone(DateTimeZone.forID(TZID)));
        return "redirect:/ok";
    }*/
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/azure/csa/{id}", method = RequestMethod.GET)
    public List<AzureBillingPeriodWrapper> getInvoice(@PathVariable("id") Long contractServiceAzureId, @RequestParam(value = "invd", required = true) @DateTimeFormat(pattern = "MMddyyyy") Date invoiceDate) throws ServiceException {
    	ContractServiceSubscription csa = contractDaoService.contractServiceSubscription(contractServiceAzureId);
    	List<ContractServiceSubscription> csas = new ArrayList<ContractServiceSubscription>();
    	csas.add(csa);
    	DateTime invoiceDateTime = new DateTime(invoiceDate).withZone(DateTimeZone.UTC)
                .withTime(0,0,0,0)
                .dayOfMonth()
                .withMinimumValue()
                .plusDays(22);
    	return serviceUsageService.getInvoicesForAzureSubscription(csas, invoiceDateTime, Boolean.FALSE);
    }
    
    @RequestMapping(value = "/azure/costs/import", method = RequestMethod.GET)
    public String importAzureCosts(@RequestParam(value = "invd", required = true) @DateTimeFormat(pattern = "MM/yyyy") Date invoiceDate) throws ServiceException {
    	//we'll automatically get whatever date they pass to the correct format
    	DateTime invoiceDateTime = new DateTime(invoiceDate).withZone(DateTimeZone.UTC).withTime(0,0,0,0)
                .dayOfMonth()
                .withMinimumValue()
                .plusDays(22);
    	serviceUsageService.syncAzureCustomerCosts(invoiceDateTime);
        return "redirect:/ok";
    }
    
    @RequestMapping(value = "/azure/csa/one", method = RequestMethod.GET)
    public String syncAzureInvoice(@RequestParam(value = "invd", required = true) @DateTimeFormat(pattern = "MM/yyyy") Date invoiceDate) throws ServiceException {
    	//we'll automatically get whatever date they pass to the correct format
    	DateTime invoiceDateTime = new DateTime(invoiceDate).withZone(DateTimeZone.UTC).withTime(0,0,0,0)
                .dayOfMonth()
                .withMinimumValue()
                .plusDays(5); // Azure invoices on the 6th
    	serviceUsageService.syncAzureCustomerInvoices(invoiceDateTime);
        return "redirect:/ok";
    }
    
    @RequestMapping(value = "/azure/csa", method = RequestMethod.GET)
    public String syncAzureInvoices() {
    	serviceUsageService.syncAzureCustomerInvoices();
        return "redirect:/ok";
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/aws/sub/{id}", method = RequestMethod.GET)
    public List<AWSBillingPeriodWrapper> getAWSInvoice(@PathVariable("id") Long contractServiceAzureId, @RequestParam(value = "invd", required = true) @DateTimeFormat(pattern = "MMddyyyy") Date invoiceDate) throws ServiceException {
    	ContractServiceSubscription csa = contractDaoService.contractServiceSubscription(contractServiceAzureId);
    	List<ContractServiceSubscription> csas = new ArrayList<ContractServiceSubscription>();
    	csas.add(csa);
    	log.info("Date: " + invoiceDate);
    	DateTime invoiceDateTime = new DateTime(invoiceDate).withZone(DateTimeZone.UTC)
                .withTime(0,0,0,0)
                .dayOfMonth()
                .withMinimumValue()
                .plusDays(1);
    	log.info("DateTime: " + invoiceDateTime);
    	return serviceUsageService.getInvoicesForAWSSubscription(csas, invoiceDateTime, Boolean.FALSE, Boolean.TRUE);
    }
    
    @RequestMapping(value = "/aws/sub/one", method = RequestMethod.GET)
    public String syncAWSInvoice(@RequestParam(value = "invd", required = true) @DateTimeFormat(pattern = "MMyyyy") Date invoiceDate) throws ServiceException {
    	DateTime invoiceDateTime = new DateTime(invoiceDate).withZone(DateTimeZone.UTC).withTime(0,0,0,0)
                .dayOfMonth()
                .withMinimumValue().plusDays(1);
    	serviceUsageService.syncAWSCustomerInvoices(invoiceDateTime);
        return "redirect:/ok";
    }
    
    @RequestMapping(value = "/aws/sub", method = RequestMethod.GET)
    public String syncAWSInvoices() {
    	serviceUsageService.syncAWSCustomerInvoices();
        return "redirect:/ok";
    }
    
    /*
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/azure/csa/{id}", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public AzureBillingPeriodWrapper getAzureMonthlyBillingForContractServiceAzure(@PathVariable("id") Long contractServiceAzureId, 
    		@RequestParam(value = "sd", required = true) @DateTimeFormat(pattern = "MMddyyyy") Date startDate,
            @RequestParam(value = "ed", required = true) @DateTimeFormat(pattern = "MMddyyyy") Date endDate) throws ServiceException {
    	ContractServiceAzure csa = contractDaoService.contractServiceAzure(contractServiceAzureId);
    	DateTime startDateTime = new DateTime(startDate).withTimeAtStartOfDay().withZone(DateTimeZone.forID(TZID));
    	DateTime endDateTime = new DateTime(endDate).plusHours(23).plusMinutes(59).plusSeconds(59).withZone(DateTimeZone.forID(TZID));
    	return serviceUsageService.generateMonthlyBillingForAzureSubscription(csa, startDateTime, endDateTime, Boolean.FALSE);
    }
    
    //used to kick off a specific monthly CSAs
    @RequestMapping(value = "/azure/csa/{id}", method = RequestMethod.GET)
    public String generateAzureMonthlyBillingForContractServiceAzure(@PathVariable("id") Long contractServiceAzureId, 
    		@RequestParam(value = "sd", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Date startDate,
            @RequestParam(value = "ed", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Date endDate) throws ServiceException {
    	ContractServiceAzure csa = contractDaoService.contractServiceAzure(contractServiceAzureId);
        serviceUsageService.generateMonthlyBillingForAzureSubscription(csa, new DateTime(startDate).withZone(DateTimeZone.forID(TZID)),
                new DateTime(endDate).withZone(DateTimeZone.forID(TZID)), Boolean.TRUE);
        return "redirect:/ok";
    }

    @RequestMapping(value = "/azure/pctest", method = RequestMethod.GET)
    public String pctest() {
    	serviceUsageService.syncCustomerSubscriptions();
        return "redirect:/ok";
    }*/
    
    @RequestMapping(value = "/azure/rctest", method = RequestMethod.GET)
    public String rctest() {
        serviceUsageService.updateRateCard();
        return "redirect:/ok";
    }
    
    @RequestMapping(value = "/spla/costs/import", method = RequestMethod.GET)
    public String importSPLACosts(@RequestParam(value = "invd", required = true) @DateTimeFormat(pattern = "MMyyyy") Date invoiceDate) throws ServiceException {
    	//we'll automatically get whatever date they pass to the correct format
    	DateTime invoiceDateTime = new DateTime(invoiceDate).withZone(DateTimeZone.UTC).withTime(0,0,0,0)
                .dayOfMonth()
                .withMinimumValue().plusDays(1);
    	Integer month = invoiceDateTime.getMonthOfYear();
    	String year = String.valueOf(invoiceDateTime.getYear());
    	log.info("Month: " + month + " --- Year " + year);
    	costService.generateSPLACostsForMonth(month, year);
        return "redirect:/ok";
    }
    
    @RequestMapping(value = "/azure/warehouse/ci/get", method = RequestMethod.GET)
    public String warehousetest(@RequestParam(value = "invd", required = true) @DateTimeFormat(pattern = "MMyyyy") Date invoiceDate) {
        try {
        	dataWarehouseService.getCIRecords(invoiceDate);
        } catch (ServiceException se) {
        	se.printStackTrace();
        } catch(Exception e) {
        	e.printStackTrace();
        }
        return "redirect:/ok";
    }
    
    @RequestMapping(value = "/azure/warehouse/ci/importytd", method = RequestMethod.GET)
    public String warehouseImportTest() {
        DateTime runDate = new DateTime().withMonthOfYear(1);
        DateTime endDate = new DateTime();
        while(runDate.getMonthOfYear() < endDate.getMonthOfYear()) {
            try {
                    log.debug("running DWH CI import for {}", org.joda.time.format.DateTimeFormat.forPattern("yyyy-MM").print(runDate));
                    dataWarehouseService.updateDataWarehouseCIsforMonthOf(runDate.toDate());
            } catch (ServiceException se) {
                    se.printStackTrace();
            } catch (Exception e) {
                    e.printStackTrace();
            }
            runDate = runDate.plusMonths(1);
        }
        return "redirect:/ok";
    }
    
    @RequestMapping(value = "/azure/warehouse/ci/import", method = RequestMethod.GET)
    public String warehouseImportTest(@RequestParam(value = "invd", required = true) @DateTimeFormat(pattern = "MMyyyy") Date invoiceDate) {
    	try {
    		dataWarehouseService.updateDataWarehouseCIsforMonthOf(invoiceDate);
    	} catch (ServiceException se) {
    		se.printStackTrace();
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
        return "redirect:/ok";
    }
    
    @RequestMapping(value = "/azure/warehouse/contract/get", method = RequestMethod.GET)
    public String warehouseContractGet(@RequestParam(value = "invd", required = true) @DateTimeFormat(pattern = "MMyyyy") Date invoiceDate) {
        try {
        	dataWarehouseService.getContractRecords(invoiceDate);
        } catch (ServiceException se) {
        	se.printStackTrace();
        } catch(Exception e) {
        	e.printStackTrace();
        }
        return "redirect:/ok";
    }
    
    @RequestMapping(value = "/azure/warehouse/contract/import", method = RequestMethod.GET)
    public String warehouseContractImport(@RequestParam(value = "invd", required = true) @DateTimeFormat(pattern = "MMyyyy") Date invoiceDate) {
    	try {
    		dataWarehouseService.updateDataWarehouseContractsforMonthOf(invoiceDate);
    	} catch (ServiceException se) {
    		se.printStackTrace();
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
        return "redirect:/ok";
    }
    
    @RequestMapping(value = "/azure/warehouse/contractupdate/get", method = RequestMethod.GET)
    public String warehouseContractUpdateGet(@RequestParam(value = "invd", required = true) @DateTimeFormat(pattern = "MMyyyy") Date invoiceDate) {
        try {
        	dataWarehouseService.getContractUpdateRecords(invoiceDate);
        } catch (ServiceException se) {
        	se.printStackTrace();
        } catch(Exception e) {
        	e.printStackTrace();
        }
        return "redirect:/ok";
    }
    
    @RequestMapping(value = "/azure/warehouse/contractupdate/import", method = RequestMethod.GET)
    public String warehouseContractUpdateImport(@RequestParam(value = "invd", required = true) @DateTimeFormat(pattern = "MMyyyy") Date invoiceDate) {
    	try {
    		dataWarehouseService.updateDataWarehouseContractUpdatesforMonthOf(invoiceDate);
    	} catch (ServiceException se) {
    		se.printStackTrace();
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
        return "redirect:/ok";
    }
    
    @RequestMapping(value = "/azure/warehouse/costs/get", method = RequestMethod.GET)
    public String warehouseCostGet(@RequestParam(value = "invd", required = true) @DateTimeFormat(pattern = "MMyyyy") Date invoiceDate) {
        try {
        	dataWarehouseService.getCostRecords(invoiceDate);
        } catch (ServiceException se) {
        	se.printStackTrace();
        } catch(Exception e) {
        	e.printStackTrace();
        }
        return "redirect:/ok";
    }
    
    @RequestMapping(value = "/azure/warehouse/costs/import", method = RequestMethod.GET)
    public String warehouseCostImport(@RequestParam(value = "invd", required = true) @DateTimeFormat(pattern = "MMyyyy") Date invoiceDate) {
    	try {
    		dataWarehouseService.updateDataWarehouseCostsforMonthOf(invoiceDate);
    	} catch (ServiceException se) {
    		se.printStackTrace();
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
        return "redirect:/ok";
    }
    
    @RequestMapping(value = "/pricing/m365", method = RequestMethod.GET)
    public String m365Sync() {
    	try {
    		pricingIntegrationService.m365Sync();
    	} catch (ServiceException se) {
    		se.printStackTrace();
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
        return "redirect:/ok";
    }
    
    @RequestMapping(value = "/pricing/m365nc", method = RequestMethod.GET)
    public String m365NCSync() {
    	try {
    		pricingIntegrationService.m365NCSync();
    	} catch (ServiceException se) {
    		se.printStackTrace();
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
        return "redirect:/ok";
    }
    
    @RequestMapping(value = "/pricing/o365", method = RequestMethod.GET)
    public String o365Sync() {
    	try {
    		pricingIntegrationService.o365Sync();
    	} catch (ServiceException se) {
    		se.printStackTrace();
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
        return "redirect:/ok";
    }
    
    @RequestMapping(value = "/azure/m365", method = RequestMethod.GET)
    public String m365TestSync(@RequestParam(value = "invd", required = true) @DateTimeFormat(pattern = "MM/yyyy") Date invoiceDate) {
    	try {
    		DateTime invoiceDateTime = new DateTime(invoiceDate).withZone(DateTimeZone.UTC).withTime(0,0,0,0)
                    .dayOfMonth()
                    .withMinimumValue()
                    .plusDays(22);
    		serviceUsageService.syncOffice365InvoicesForMonth(invoiceDateTime);
    	} catch (ServiceException se) {
    		se.printStackTrace();
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
        return "redirect:/ok";
    }
}
