package com.logicalis.serviceinsight.web.controller;

import com.logicalis.serviceinsight.dao.CostItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.logicalis.serviceinsight.dao.ExpenseCategory;
import com.logicalis.serviceinsight.dao.UnitCost;
import com.logicalis.serviceinsight.data.Contract;
import com.logicalis.serviceinsight.data.Customer;
import com.logicalis.serviceinsight.data.Device;
import com.logicalis.serviceinsight.data.ExpenseCategoryReportWrapper;
import com.logicalis.serviceinsight.data.FullContract;
import com.logicalis.serviceinsight.data.Personnel;
import com.logicalis.serviceinsight.data.PipelineQuoteLineItemMin;
import com.logicalis.serviceinsight.data.PipelineQuoteMin;
import com.logicalis.serviceinsight.data.ReportWrapper;
import com.logicalis.serviceinsight.data.ReportWrapper.ReportType;
import com.logicalis.serviceinsight.data.Service;
import com.logicalis.serviceinsight.data.UnitCostDetails;
import com.logicalis.serviceinsight.representation.AWSBillingPeriodWrapper;
import com.logicalis.serviceinsight.representation.AWSInvoiceReportUsageLineItem;
import com.logicalis.serviceinsight.representation.AWSInvoiceReportWrapper;
import com.logicalis.serviceinsight.representation.AzureBillingPeriodWrapper;
import com.logicalis.serviceinsight.representation.AzureInvoiceReportLicenseLineItem;
import com.logicalis.serviceinsight.representation.AzureInvoiceReportOneTimeLineItem;
import com.logicalis.serviceinsight.representation.AzureInvoiceReportUsageLineItem;
import com.logicalis.serviceinsight.representation.AzureInvoiceReportWrapper;
import com.logicalis.serviceinsight.representation.CSPBilledContractService;
import com.logicalis.serviceinsight.representation.CSPLicenseBillingLineItem;
import com.logicalis.serviceinsight.representation.CSPLicenseBillingWrapper;
import com.logicalis.serviceinsight.representation.CSPOneTimeBillingLineItem;
import com.logicalis.serviceinsight.representation.CSPOneTimeBillingWrapper;
import com.logicalis.serviceinsight.representation.LaborBreakdownRecord;
import com.logicalis.serviceinsight.representation.LaborHoursRecord;
import com.logicalis.serviceinsight.representation.PricingPipeline;
import com.logicalis.serviceinsight.representation.PricingPipelineQuoteByCustomer;
import com.logicalis.serviceinsight.representation.PricingPipelineService;
import com.logicalis.serviceinsight.representation.RevenueByCustomerRecord;
import com.logicalis.serviceinsight.representation.RevenueByServiceRecord;
import com.logicalis.serviceinsight.representation.RevenueReportResultRecord;
import com.logicalis.serviceinsight.representation.SPLAReport;
import com.logicalis.serviceinsight.representation.SPLARevenue;
import com.logicalis.serviceinsight.representation.ServiceDetailRecordWrapper;
import com.logicalis.serviceinsight.service.ApplicationDataDaoService;
import com.logicalis.serviceinsight.service.ContractDaoService;
import com.logicalis.serviceinsight.service.ContractRevenueService;
import com.logicalis.serviceinsight.service.CostDaoService;
import com.logicalis.serviceinsight.service.CostService;
import com.logicalis.serviceinsight.service.PricingIntegrationService;
import com.logicalis.serviceinsight.service.RevenueService;
import com.logicalis.serviceinsight.service.ServiceException;
import com.logicalis.serviceinsight.service.ServiceUsageService;
import com.logicalis.serviceinsight.web.view.ExcelView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.apache.commons.lang.StringUtils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.HttpStatus.OK;
import org.springframework.http.ResponseEntity;

@Controller
@RequestMapping("/reports")
public class ReportController extends BaseController {

    @Autowired
    ContractDaoService contractDaoService;
    @Autowired
    RevenueService revenueService;
    @Autowired
    ContractRevenueService contractRevenueService;
    @Autowired
    ApplicationDataDaoService applicationDataDaoService;
    @Autowired
    CostService costService;
    @Autowired
    CostDaoService costDaoService;
    @Autowired
    PricingIntegrationService pricingIntegrationService;
    @Autowired
    ServiceUsageService serviceUsageService;
    
    private static final Long ALL_SERVICES_CODE = 0L;
    private static final Long ALL_SERVICES_NO_ADJUSTMENTS_CODE = 99999L;

    @RequestMapping(method = RequestMethod.GET)
    public String reports(Model uiModel) {
        return "forward:/reports/revenue";
    }

    @RequestMapping(value = "/revenue", method = RequestMethod.GET)
    public String revenueReports(Model uiModel) {
        uiModel.addAttribute("services", contractDaoService.servicesIncludingContractServices());
        uiModel.addAttribute("customers", contractDaoService.customers(null, null, true));
        return "reports/revenue";
    }
    
    @RequestMapping(value = "/topservices", method = RequestMethod.GET)
    public String serviceReports(Model uiModel) {
        //uiModel.addAttribute("services", contractDaoService.services(Boolean.TRUE));
        //uiModel.addAttribute("customers", contractDaoService.customers());
        return "reports/topservices";
    }
    
    @RequestMapping(value = "/laborbyservice", method = RequestMethod.GET)
    public String laborByService(Model uiModel) {
        return "reports/laborbyservice";
    }
    
    @RequestMapping(value = "/topcustomers", method = RequestMethod.GET)
    public String topCustomers(Model uiModel) {
        return "reports/topcustomers";
    }
    
    @RequestMapping(value = "/pricingpipeline", method = RequestMethod.GET)
    public String pricingPipeline(Model uiModel) {
        return "reports/pricingpipeline";
    }
    
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/laborbreakdown", method = RequestMethod.GET)
    public String laborBreakdown(Model uiModel) {
        return "reports/laborbreakdown";
    }
    
    @RequestMapping(value = "/laborhours", method = RequestMethod.GET)
    public String laborHours(Model uiModel) {
    	uiModel.addAttribute("services", contractDaoService.servicesIncludingContractServices());
        uiModel.addAttribute("customers", contractDaoService.customers(false, true, true));
        return "reports/laborhours";
    }
    
    @RequestMapping(value = "/expensecategories", method = RequestMethod.GET)
    public String expenseCategories(Model uiModel) {
    	List<ExpenseCategory> categories = contractDaoService.expenseCategories();
    	Collections.sort(categories, ExpenseCategory.ExpenseCategoryNameComparator);
    	uiModel.addAttribute("categories", categories);
        uiModel.addAttribute("customers", contractDaoService.customers());
        return "reports/expensecategories";
    }
    
    @RequestMapping(value = "/azureinvoice", method = RequestMethod.GET)
    public String azureInvoice(Model uiModel) {
        return "reports/azureinvoice";
    }
    
    @RequestMapping(value = "/renewals", method = RequestMethod.GET)
    public String renewalsReport(Model uiModel) {
    	uiModel.addAttribute("customers", contractDaoService.customers(false, true, true));
    	uiModel.addAttribute("statuses", Arrays.asList(Contract.RenewalStatus.values()));
        return "reports/renewals";
    }
    
    @RequestMapping(value = "/spla", method = RequestMethod.GET)
    public String spla(Model uiModel) throws ServiceException {
        try {
            uiModel.addAttribute("customers", contractDaoService.customers());
            uiModel.addAttribute("vendors", jdbcTemplate.queryForList("select distinct vendor from spla_cost_catalog", String.class));
            uiModel.addAttribute("splas", applicationDataDaoService.splaCosts(null, Boolean.FALSE));
            uiModel.addAttribute("devices", applicationDataDaoService.findDeviceByDeviceType(Device.DeviceType.spla));
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("general_service_exception", new Object[]{any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
        return "reports/spla";
    }
    
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/awsinvoice", method = RequestMethod.GET)
    public String awsInvoice(Model uiModel) {
        return "reports/awsinvoice";
    }
    
    @RequestMapping(value = "/servicedetails", method = RequestMethod.GET)
    public String serviceRevenueDetails(Model uiModel) {
        uiModel.addAttribute("services", contractDaoService.servicesIncludingContractServices());
        uiModel.addAttribute("devices", applicationDataDaoService.pipeDelimitedDeviceString());
        return "reports/servicedetails";
    }

    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/laborbyservice", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public List<Map<String, Map<String, BigDecimal>>> queryForLaborByService(@RequestParam(value = "sd", required = true) @DateTimeFormat(pattern = "MM/yyyy") Date startDate, 
    		@RequestParam(value = "ed", required = true) @DateTimeFormat(pattern = "MM/yyyy") Date endDate,
    		@RequestParam(value = "type", required = false) String reportType) {
    	List<Map<String, Map<String, BigDecimal>>> results = null;
    	if(reportType == null) {
    		results = costService.serviceLaborByServiceForDates(new DateTime(startDate), new DateTime(endDate));
    	} else if ("customer".equals(reportType)) {
    		results = costService.laborWithoutServiceByCustomerForDates(new DateTime(startDate), new DateTime(endDate));
    	} else if ("task".equals(reportType)) {
    		results = costService.laborWithoutServiceByChronosTaskForDates(new DateTime(startDate), new DateTime(endDate));
    	}
    	return results;
    }
    
    @RequestMapping(value = "/laborbyservice", params = "export", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public String queryForLaborByServiceExport(Model uiModel, @RequestParam(value = "sd", required = true) @DateTimeFormat(pattern = "MM/yyyy") Date startDate, @RequestParam(value = "ed", required = true) @DateTimeFormat(pattern = "MM/yyyy") Date endDate) {
        costService.serviceLaborByServiceForDates(new DateTime(startDate), new DateTime(endDate));
        uiModel.addAttribute(ExcelView.LABOR_BY_COST_REPORT_KEY, null);
        return "report/revenueexport";
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/revenue", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public Map<Long, List<RevenueReportResultRecord>> queryForRevenueData(@RequestParam(value = "bm", required = false) String businessModel,
            @RequestParam(value = "ospId", required = false) Long[] ospIds,
            @RequestParam(value = "sd", required = false) @DateTimeFormat(pattern = "MM/yyyy") Date startDate,
            @RequestParam(value = "ed", required = false) @DateTimeFormat(pattern = "MM/yyyy") Date endDate,
            @RequestParam(value = "cid", required = false) Long customerId,
            @RequestParam(value = "chldrn", required = false) Boolean includeChildren,
            @RequestParam(value = "invoiced", required = false) Boolean invoicedRevenueOnly,
            @RequestParam(value = "fc", required = false) Boolean forecastRevenue) {
    	Map<Long, List<RevenueReportResultRecord>> results = new HashMap<Long, List<RevenueReportResultRecord>>();
    	
    	if(ospIds == null) {
    		List<RevenueReportResultRecord> serviceResults = getRevenueReportData(businessModel, null, startDate, endDate, customerId, includeChildren, false, invoicedRevenueOnly, forecastRevenue);
    		results.put(new Long(0), serviceResults);
    	} else {
	    	for(Long ospId : ospIds) {
	    		List<RevenueReportResultRecord> serviceResults = null;
	    		if(ALL_SERVICES_CODE.equals(ospId)) {
	    			serviceResults = getRevenueReportData(businessModel, null, startDate, endDate, customerId, includeChildren, false, invoicedRevenueOnly, forecastRevenue);
	    		} else if(ALL_SERVICES_NO_ADJUSTMENTS_CODE.equals(ospId)) {
	    			serviceResults = getRevenueReportData(businessModel, null, startDate, endDate, customerId, includeChildren, true, invoicedRevenueOnly, forecastRevenue);
	    		} else {
	    			serviceResults = getRevenueReportData(businessModel, ospId, startDate, endDate, customerId, includeChildren, true, invoicedRevenueOnly, forecastRevenue);
	    		}
	    		results.put(ospId, serviceResults);
	    	}
    	}
        return results;
    }

    @RequestMapping(value = "/revenue", params = "export", method = RequestMethod.GET)
    public String revenueExport(@RequestParam(value = "bm", required = false) String businessModel,
            @RequestParam(value = "ospId", required = false) Long[] ospIds,
            @RequestParam(value = "sd", required = false) @DateTimeFormat(pattern = "MM/yyyy") Date startDate,
            @RequestParam(value = "ed", required = false) @DateTimeFormat(pattern = "MM/yyyy") Date endDate,
            @RequestParam(value = "cid", required = false) Long customerId,
            @RequestParam(value = "chldrn", required = false) Boolean includeChildren,
            @RequestParam(value = "profit", required = false) Boolean isProfitReport, 
            @RequestParam(value = "invoiced", required = false) Boolean invoicedRevenueOnly, 
            @RequestParam(value = "fc", required = false) Boolean forecastRevenue, Model uiModel) throws ServiceException {
        String customerName = "";
        String serviceName = "";
        if (customerId != null) {
            Customer customer = contractDaoService.customer(customerId);
            if (customer != null) {
                customerName = customer.getName();
            }
        }
        
        ReportType reportType = ReportType.revenue;
        if(isProfitReport != null && isProfitReport) reportType = ReportType.revenueProfit;
        
        Map<Long, List<RevenueReportResultRecord>> results = new HashMap<Long, List<RevenueReportResultRecord>>();
    	
    	if(ospIds == null) {
    		List<RevenueReportResultRecord> serviceResults = getRevenueReportData(businessModel, null, startDate, endDate, customerId, includeChildren, false, invoicedRevenueOnly, forecastRevenue);
    		results.put(new Long(0), serviceResults);
    	} else {
	    	for(Long ospId : ospIds) {
	    		List<RevenueReportResultRecord> serviceResults = null;
	    		if(ALL_SERVICES_CODE.equals(ospId)) {
	    			serviceResults = getRevenueReportData(businessModel, null, startDate, endDate, customerId, includeChildren, false, invoicedRevenueOnly, forecastRevenue);
	    			if(!"".equals(serviceName)) serviceName += ", ";
	    			serviceName += messageSource.getMessage("ui_reports_all_services", null, LocaleContextHolder.getLocale());
	    		} else if(ALL_SERVICES_NO_ADJUSTMENTS_CODE.equals(ospId)) {
	    			serviceResults = getRevenueReportData(businessModel, null, startDate, endDate, customerId, includeChildren, true, invoicedRevenueOnly, forecastRevenue);
	    			if(!"".equals(serviceName)) serviceName += ", ";
	    			serviceName += messageSource.getMessage("ui_reports_all_services_without", null, LocaleContextHolder.getLocale());
	    		} else {
	    			serviceResults = getRevenueReportData(businessModel, ospId, startDate, endDate, customerId, includeChildren, true, invoicedRevenueOnly, forecastRevenue);
	    			Service service = applicationDataDaoService.findActiveServiceByOspId(ospId);
                            if (service != null) {
                                    if(!"".equals(serviceName)) serviceName += ", ";
                                serviceName += service.getName();
                            }
	    		}
	    		results.put(ospId, serviceResults);
	    	}
    	}
        
        ReportWrapper report = new ReportWrapper(reportType, startDate, endDate, customerId, customerName, null, serviceName, businessModel, false, results);
        uiModel.addAttribute(ExcelView.EXPORT_REVENUE_REPORT_KEY, report);
        return "report/revenueexport";
    }
    
    private List<RevenueReportResultRecord> getRevenueReportData(String businessModel, Long ospId, Date startDate, Date endDate,
            Long customerId, Boolean includeChildren, Boolean excludeContractAdjustments, Boolean invoicedRevenueOnly, Boolean forecastRevenue) {
        List<RevenueReportResultRecord> report = revenueService.serviceRevenueReport(businessModel, ospId,
                (startDate != null ? new DateTime(startDate) : null),
                (endDate != null ? new DateTime(endDate) : null), customerId, includeChildren, invoicedRevenueOnly, forecastRevenue);
        
        Map<String, BigDecimal> contractAdjustmentReport = null;
        if (excludeContractAdjustments == null || !excludeContractAdjustments) {
            contractAdjustmentReport = revenueService.contractAdjustmentReport(
                    (startDate != null ? new DateTime(startDate) : null),
                    (endDate != null ? new DateTime(endDate) : null), customerId, invoicedRevenueOnly);
        }

        for (RevenueReportResultRecord record : report) {
            BigDecimal revenue = record.getRevenue();
            if (excludeContractAdjustments == null || (!excludeContractAdjustments && contractAdjustmentReport != null)) {
                revenue = revenue.add(contractAdjustmentReport.get(record.getDisplayDate()));
            }
            revenue = revenue.setScale(2, RoundingMode.HALF_UP);
            record.setRevenue(revenue);
        }
        return report;
    }

    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/topservices", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public List<RevenueByServiceRecord> queryForTopServicesData(@RequestParam(value = "bm", required = false) String businessModel,
            @RequestParam(value = "month", required = false) @DateTimeFormat(pattern = "MM/yyyy") Date month,
            @RequestParam(value = "invoiced", required = false) Boolean invoicedRevenueOnly) {
        return getTopServicesReportData(businessModel, month, invoicedRevenueOnly);
    }
    
    private List<RevenueByServiceRecord> getTopServicesReportData(String businessModel, Date month, Boolean invoicedRevenueOnly) {
    	List<RevenueByServiceRecord> records = new ArrayList<RevenueByServiceRecord>();
    	try {
    		List<Service> services = contractDaoService.servicesForBusinessModel(businessModel);
            for (Service service : services) {
                Long ospId = Long.valueOf(service.getOspId());
                List<RevenueReportResultRecord> report = getRevenueReportData(businessModel, ospId, month, month, null, null, true, invoicedRevenueOnly, false);
                RevenueByServiceRecord record = new RevenueByServiceRecord();
                record.setBusinessModel(service.getBusinessModel());
                record.setOspId(ospId);
                record.setServiceName(service.getName());
                
                //expecting just one record in this result set
                record.setRevenue(report.get(0).getRevenue());
                record.setDeviceCount(report.get(0).getDeviceCount());
                record.setData(report);
                records.add(record);
            }
            Collections.sort(records, Collections.reverseOrder());
        } catch (Exception e) {
            e.printStackTrace();
        }
    	
        return records;
    }
    
    @RequestMapping(value = "/topservices", params = "export", method = RequestMethod.GET)
    public String queryForRevenueByServiceData(@RequestParam(value = "bm", required = false) String businessModel,
            @RequestParam(value = "month", required = false) @DateTimeFormat(pattern = "MM/yyyy") Date month,
            @RequestParam(value = "invoiced", required = false) Boolean invoicedRevenueOnly, Model uiModel) {
        try {
        	List<RevenueByServiceRecord> records = getTopServicesReportData(businessModel, month, invoicedRevenueOnly);
            uiModel.addAttribute(ExcelView.EXPORT_REVENUE_REPORT_BY_SERVICE_KEY, records);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "report/revenuebyserviceexport";
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/servicedetails", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public ServiceDetailRecordWrapper getServiceDetails(@RequestParam(value = "svcdt", required = true) @DateTimeFormat(pattern = "MM/yyyy") Date serviceDate,
            @RequestParam(value = "ospId", required = false) Long ospId, @RequestParam(value = "deviceId", required = false) Long deviceId) throws ServiceException {
        try {
            return contractRevenueService.serviceDetailsForFilter(ospId, deviceId, serviceDate);
        } catch(Exception any) {
            throw new ServiceException(messageSource.getMessage("general_service_exception", new Object[]{any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    }
    
    @RequestMapping(value = "/servicedetails", params = "export", method = RequestMethod.GET)
    public String getServiceDetailsExport(@RequestParam(value = "svcdt", required = true) @DateTimeFormat(pattern = "MM/yyyy") Date serviceDate,
            @RequestParam(value = "ospId", required = false) Long ospId, @RequestParam(value = "deviceId", required = false) Long deviceId, Model uiModel) throws ServiceException {
        try {
            ServiceDetailRecordWrapper results = contractRevenueService.serviceDetailsForFilter(ospId, deviceId, serviceDate);
            uiModel.addAttribute(ExcelView.EXPORT_SERVICE_DETAILS_REPORT_KEY, results);
        } catch(Exception any) {
            throw new ServiceException(messageSource.getMessage("general_service_exception", new Object[]{any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
        return "report/revenuebyserviceexport";
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/topcustomers", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public List<RevenueByCustomerRecord> queryForTopCustomersData(@RequestParam(value = "month", required = false) @DateTimeFormat(pattern = "MM/yyyy") Date month,
            @RequestParam(value = "archived", required = false) Boolean archived,
            @RequestParam(value = "chldrn", required = false) Boolean includeChildren,
            @RequestParam(value = "invoiced", required = false) Boolean invoicedRevenueOnly) {
        return getTopCustomersReportData(month, archived, includeChildren, invoicedRevenueOnly);
    }
    
    private String fmtmoney(BigDecimal money) {
        if (money != null) {
            return money.setScale(2, RoundingMode.HALF_UP).toPlainString();
        } else {
            return "[null]";
        }
    }
    
    private List<RevenueByCustomerRecord> getTopCustomersReportData(Date month, Boolean active, Boolean includeChildren, Boolean invoicedRevenueOnly) {
        List<RevenueByCustomerRecord> records = new ArrayList<RevenueByCustomerRecord>();
        try {
            List<Customer> customers = contractDaoService.customers(active, true, false);
            log.debug(" Total Customers: " + customers.size());
            for (Customer customer : customers) {
                Long customerId = customer.getId();
                List<RevenueReportResultRecord> report = getRevenueReportData(null, null, month, month, customerId, includeChildren, false, invoicedRevenueOnly, false);
                RevenueByCustomerRecord record = new RevenueByCustomerRecord();
                record.setCustomerName(customer.getName());
                record.setServiceDeliveryManager(Personnel.convertListToUsernamesString(customer.getServiceDeliveryManagers()));
                
                record.setRevenue(report.get(0).getRevenue());
                record.setDeviceCount(report.get(0).getDeviceCount());
                record.setData(report);
                records.add(record);
            }
            Collections.sort(records, Collections.reverseOrder());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return records;
    }
    
    @RequestMapping(value = "/topcustomers", params = "export", method = RequestMethod.GET)
    public String queryForRevenueByCustomerData(@RequestParam(value = "month", required = false) @DateTimeFormat(pattern = "MM/yyyy") Date month,
            @RequestParam(value = "archived", required = false) Boolean archived, @RequestParam(value = "chldrn", required = false) Boolean includeChildren,
            @RequestParam(value = "invoiced", required = false) Boolean invoicedRevenueOnly, Model uiModel) {
        try {
            List<RevenueByCustomerRecord> records = getTopCustomersReportData(month, archived, includeChildren, invoicedRevenueOnly);
            uiModel.addAttribute(ExcelView.EXPORT_REVENUE_REPORT_BY_CUSTOMER_KEY, records);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "report/revenuebycustomerexport";
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/pricingpipeline", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public PricingPipeline queryForPricingPipelineData(@RequestParam(value = "sd", required = false) @DateTimeFormat(pattern = "MM/yyyy") Date startDate,
            @RequestParam(value = "ed", required = false) @DateTimeFormat(pattern = "MM/yyyy") Date endDate) {
        return getPricingPipeline(startDate, endDate);
    }
    
    private PricingPipeline getPricingPipeline(Date startDate, Date endDate) {
        PricingPipeline pipeline = new PricingPipeline();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        try {
            pipeline.setPricingBaseURL(pricingIntegrationService.getPricingQuoteUrl());
            List<PipelineQuoteMin> pipelineItems = pricingIntegrationService.findQuotesForPipeline(new DateTime(startDate), new DateTime(endDate));
            PricingPipelineQuoteByCustomer customerQuote;
            PricingPipelineService service;
            Map<String,PricingPipelineService> serviceMap = new HashMap<String,PricingPipelineService>();
            Set<String> quoteServices;
            String servicesKey;
            long qty = 0l;
            for (PipelineQuoteMin item : pipelineItems) {
                quoteServices = new HashSet<String>();
                qty = 0l;
                for (PipelineQuoteLineItemMin li : item.getLineItems()) {
                    servicesKey = calculateServicesKey(li);
                    qty += li.getQuantity().intValue();
                    quoteServices.add(li.getName()); 
                    if (serviceMap.containsKey(servicesKey)) {
                        service = serviceMap.get(servicesKey);
                    } else {
                        service = new PricingPipelineService();
                        if (li.getServiceOfferingName()!=null) {
                            service.setServiceOfferingName(li.getServiceOfferingName());
                        } else {
                            service.setServiceOfferingName("undefined");
                        }
                        service.setServiceName(li.getName());
                        service.setTotalItems(new Long(0l));
                        if (li.getUnitLabel()!=null) {
                            service.setUnitLabel(li.getUnitLabel());
                            log.debug("..... unit label: [" + li.getUnitLabel() + "] .....");
                        } else {
                            service.setUnitLabel("");
                        }
                    }
                    service.setTotalItems(service.getTotalItems() + li.getQuantity().intValue());
                    serviceMap.put(servicesKey, service);
                }
                customerQuote = new PricingPipelineQuoteByCustomer();
                customerQuote.setCustomerId(item.getCustomerId());
                customerQuote.setCustomerName(item.getCustomerName());
                customerQuote.setQuoteId(item.getId());
                customerQuote.setQuoteNumber(item.getQuoteNumber());
                customerQuote.setCloseDate(sdf.format(item.getCloseDate()));
                customerQuote.setTotalItems(new Long(qty));
                customerQuote.setServices(((quoteServices.toString().replace("[","")).replace("]","")));
                pipeline.addCustomerQuote(customerQuote);
            }
            pipeline.setServices(new ArrayList<PricingPipelineService>(serviceMap.values()));
            // sort both
            Collections.sort(pipeline.getServices());
            Collections.sort(pipeline.getCustomerQuotes());
        } catch (ServiceException e) {
            e.printStackTrace();
        }   
        return pipeline;
    }
    
    /**
     * Calculate the Pricing Pipeline report services key : product type | <calculateServiceGroup> | offering name
     * 
     * @param item
     * @return String key
     */
    private String calculateServicesKey(PipelineQuoteLineItemMin item) {
        StringBuilder sbuf = new StringBuilder();
        if ((item.getServiceOfferingName()!=null) && (item.getServiceOfferingName().trim().length()>0)) {
            sbuf.append(item.getServiceOfferingName());
        } else {
            sbuf.append("undefined");
        }
        sbuf.append(" - ");
        if (item.getName()!=null) {
            sbuf.append(item.getName());
        } else {
            sbuf.append("undefined");
        }
        return sbuf.toString();
    }
    
    @RequestMapping(value = "/pricingpipeline", params = "export", method = RequestMethod.GET)
    public String queryForPricelinePipelineExport(@RequestParam(value = "sd", required = false) @DateTimeFormat(pattern = "MM/yyyy") Date startDate,
            @RequestParam(value = "ed", required = false) @DateTimeFormat(pattern = "MM/yyyy") Date endDate, Model uiModel) {
        PricingPipeline report = getPricingPipeline(startDate, endDate);
        uiModel.addAttribute(ExcelView.EXPORT_PRICING_PIPELINE_KEY, report);
        return "report/pricingpipelineexport";
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/laborbreakdown", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public List<LaborBreakdownRecord> queryForLaborBreakdownData(@RequestParam(value = "month", required = true) @DateTimeFormat(pattern = "MM/yyyy") Date month) {
        return costService.laborBreakdownForMonth(new DateTime(month));
    }
    
    @RequestMapping(value = "/laborbreakdown", params = "export", method = RequestMethod.GET)
    public String queryForLaborBreakdownDataExport(@RequestParam(value = "month", required = true) @DateTimeFormat(pattern = "MM/yyyy") Date month, Model uiModel) {
    	DateTime monthDateTime = new DateTime(month);
    	List<LaborBreakdownRecord> reportData = costService.laborBreakdownForMonth(monthDateTime);
        uiModel.addAttribute(ExcelView.EXPORT_LABOR_BREAKDOWN_KEY, reportData);
        return "report/laborbreakdownexport";
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/laborhours", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public ReportWrapper queryForLaborHoursData(@RequestParam(value = "ospId", required = false) Long ospId,
            @RequestParam(value = "sd", required = true) @DateTimeFormat(pattern = "MM/yyyy") Date startDate,
            @RequestParam(value = "ed", required = true) @DateTimeFormat(pattern = "MM/yyyy") Date endDate,
            @RequestParam(value = "cid", required = false) Long customerId, @RequestParam(value = "lim", required = false) Long recordLimit, @RequestParam(value = "chldrn", required = false) Boolean includeChildren) throws ServiceException {
    	ReportWrapper results = costService.laborHoursReport(startDate, endDate, ospId, customerId, recordLimit, includeChildren);
        return results;
    }
    
    @RequestMapping(value = "/laborhours", params = "export", method = RequestMethod.GET)
    public String queryForLaborHoursDataExport(@RequestParam(value = "ospId", required = false) Long ospId,
            @RequestParam(value = "sd", required = true) @DateTimeFormat(pattern = "MM/yyyy") Date startDate,
            @RequestParam(value = "ed", required = true) @DateTimeFormat(pattern = "MM/yyyy") Date endDate,
            @RequestParam(value = "cid", required = false) Long customerId, @RequestParam(value = "chldrn", required = false) Boolean includeChildren, Model uiModel) throws ServiceException {
    	ReportWrapper results = costService.laborHoursReport(startDate, endDate, ospId, customerId, null, includeChildren);
    	List<LaborHoursRecord> reportData = (List<LaborHoursRecord>) results.getGenericData();
    	
    	uiModel.addAttribute(ExcelView.EXPORT_LABOR_HOURS_KEY, reportData);
        return "report/laborbreakdownexport";
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value="/expensecategories", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public ExpenseCategoryReportWrapper unitCosts(@RequestParam(value = "custid", required = false) Long custId, @RequestParam(value = "exid", required = true) Integer id,
            @RequestParam(value = "ad", required = true) @DateTimeFormat(pattern = "MM/yyyy") Date appliedDate) throws ServiceException {
    	ExpenseCategoryReportWrapper wrapper = new ExpenseCategoryReportWrapper();
    	Map<String, UnitCost> previousMonths = costDaoService.unitCostByExpenseCategoryAndDateRange(custId, id, new DateTime(appliedDate).minusMonths(5), new DateTime(appliedDate));
    	UnitCostDetails unitCostDetails = contractDaoService.unitCostDetailsForAppliedDate(custId, id, new DateTime(appliedDate));
    	List<Device> devices = costDaoService.devicesForExpenseCategory(id);
    	wrapper.setPreviousMonths(previousMonths);
    	wrapper.setUnitCostDetails(unitCostDetails);
    	wrapper.setAssociatedDevices(devices);
    	return wrapper;
    }

    @RequestMapping(value = "/expensecategories", params = "export", method = RequestMethod.GET)
    public String unitCostsExport(@RequestParam(value = "custid", required = false) Long custId, @RequestParam(value = "exid", required = true) Integer id,
            @RequestParam(value = "ad", required = true) @DateTimeFormat(pattern = "MM/yyyy") Date appliedDate, @RequestParam(value = "exn", required = false) String categoryName, Model uiModel) throws ServiceException {
    	ExpenseCategoryReportWrapper wrapper = new ExpenseCategoryReportWrapper();
    	Map<String, UnitCost> previousMonths = costDaoService.unitCostByExpenseCategoryAndDateRange(custId, id, new DateTime(appliedDate).minusMonths(5), new DateTime(appliedDate));
    	UnitCostDetails unitCostDetails = contractDaoService.unitCostDetailsForAppliedDate(custId, id, new DateTime(appliedDate));
    	List<Device> devices = costDaoService.devicesForExpenseCategory(id);
    	SimpleDateFormat sdfDate = new SimpleDateFormat("MM/yyyy");
        String strDate = sdfDate.format(appliedDate);
    	wrapper.setCategoryName(categoryName);
    	wrapper.setMonth(strDate);
    	wrapper.setPreviousMonths(previousMonths);
    	wrapper.setUnitCostDetails(unitCostDetails);
    	wrapper.setAssociatedDevices(devices);
    	
    	uiModel.addAttribute(ExcelView.EXPORT_EXPENSE_CATEGORY_REPORT_KEY, wrapper);
        return "report/expensecategoriesexport";
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/azureinvoice", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public AzureInvoiceReportWrapper getAzureInvoice(@RequestParam(value = "invd", required = true) @DateTimeFormat(pattern = "MM/yyyy") Date invoiceDate) throws ServiceException {
    	DateTime invoiceDateTime = new DateTime(invoiceDate).withZone(DateTimeZone.UTC)
                .withTime(0,0,0,0)
                .dayOfMonth()
                .withMinimumValue()
                .plusDays(22);
    	Integer month = invoiceDateTime.getMonthOfYear();
    	String year = String.valueOf(invoiceDateTime.getYear());
    	
        /**
         * Even though we get a list of invoiced wrappers, we don't use the invoice id for this
         * report so we can just combine the Azure results and do the same SI merge routine
         */
    	List<AzureInvoiceReportWrapper> azureWrappers =  serviceUsageService.getAzureInvoiceReportData(invoiceDateTime);
        AzureInvoiceReportWrapper azureWrapper = new AzureInvoiceReportWrapper();
        for (AzureInvoiceReportWrapper azureInvoiceWrapper : azureWrappers) {
            azureWrapper.getAzureBillingPeriods().addAll(azureInvoiceWrapper.getAzureBillingPeriods());
            azureWrapper.getCspLicenses().addAll(azureInvoiceWrapper.getCspLicenses());
            azureWrapper.getCspOnetimes().addAll(azureInvoiceWrapper.getCspOnetimes());
        }
    	List<CSPBilledContractService> siLicenses = contractRevenueService.serviceRevenueRollupForMonthOfByCustomer(Device.DeviceType.cspO365, month, year);
    	List<CSPBilledContractService> siAzureReserved = contractRevenueService.serviceRevenueRollupForMonthOfByCustomer(Device.DeviceType.cspreserved, month, year);
        siAzureReserved.addAll(contractRevenueService.serviceRevenueRollupForMonthOfByCustomer(Device.DeviceType.cspazureplan, month, year));
        siAzureReserved.addAll(contractRevenueService.serviceRevenueCSPForMonthOf(Device.DeviceType.cspazure, month, year));
        List<AzureInvoiceReportUsageLineItem> usageLineItems = mergeUsageLineitems(azureWrapper.getAzureBillingPeriods(), new ArrayList<CSPBilledContractService>());
        Map<String, List<AzureInvoiceReportLicenseLineItem>> licenseLineItems = mergeLicenseLineitems(azureWrapper.getCspLicenses(), siLicenses);
        Map<String, List<AzureInvoiceReportOneTimeLineItem>> onetimeLineItems = mergeOneTimeLineitems(azureWrapper.getCspOnetimes(), siAzureReserved);

        azureWrapper.setUsageLineItems(usageLineItems);
        azureWrapper.setLicenseLineItems(licenseLineItems);
        azureWrapper.setOnetimeLineItems(onetimeLineItems);
        azureWrapper.setSiLicenses(siLicenses);
    	return azureWrapper;
    }
    
    private List<AzureInvoiceReportUsageLineItem> mergeUsageLineitems(List<AzureBillingPeriodWrapper> azureBillingPeriods, List<CSPBilledContractService> siAzureBillingPeriods) {
    	List<AzureInvoiceReportUsageLineItem> results = new ArrayList<AzureInvoiceReportUsageLineItem>();
    	
    	for(AzureBillingPeriodWrapper azurePeriod : azureBillingPeriods) {
    		AzureInvoiceReportUsageLineItem lineitem = new AzureInvoiceReportUsageLineItem();
    		lineitem.setAzureCustomerName(azurePeriod.getCustomerAzureName());
    		lineitem.setAzureSubscriptionId(azurePeriod.getSubscriptionId());
    		lineitem.setAzureSubscriptionName(azurePeriod.getSubscriptionName());
    		lineitem.setAzureMonthlyCost(azurePeriod.getRawTotal());
    		
    		for (Iterator<CSPBilledContractService> iterator = siAzureBillingPeriods.iterator(); iterator.hasNext();) {
    			CSPBilledContractService siPeriod = iterator.next();
    		    if(azurePeriod.getSubscriptionId() != null && siPeriod.getExternalSubscriptionId() != null && azurePeriod.getSubscriptionId().toLowerCase().equals(siPeriod.getExternalSubscriptionId().toLowerCase())) {
    				lineitem.setSiCustomerName(siPeriod.getCustomerName());
    				lineitem.setSiOnetimeRevenue(siPeriod.getOnetimeRevenue());
    				lineitem.setSiRecurringRevenue(siPeriod.getRecurringRevenue());
    				iterator.remove();
    			}
    		}
    		results.add(lineitem);
    	}
    	
    	for(CSPBilledContractService siPeriod : siAzureBillingPeriods) {
    		AzureInvoiceReportUsageLineItem lineitem = new AzureInvoiceReportUsageLineItem();
    		lineitem.setSiCustomerName(siPeriod.getCustomerName());
			lineitem.setSiOnetimeRevenue(siPeriod.getOnetimeRevenue());
			lineitem.setSiRecurringRevenue(siPeriod.getRecurringRevenue());
			results.add(lineitem);
    	}
    	
    	return results;
    }
    
    private Map<String, List<AzureInvoiceReportLicenseLineItem>> mergeLicenseLineitems(List<CSPLicenseBillingWrapper> azureLicenses, List<CSPBilledContractService> siLicenses) {
    	List<AzureInvoiceReportLicenseLineItem> lineitems = new ArrayList<AzureInvoiceReportLicenseLineItem>();
    	Map<String, List<AzureInvoiceReportLicenseLineItem>> results = new HashMap<String, List<AzureInvoiceReportLicenseLineItem>>();
    	
    	List<CSPBilledContractService> siLicensesMerged = new ArrayList<CSPBilledContractService>();
    	for(CSPBilledContractService siLicense : siLicenses) {
    		boolean contains = false;
    		for(CSPBilledContractService mergedLicense : siLicensesMerged) {
    			if(mergedLicense.getContractId().equals(siLicense.getContractId()) && mergedLicense.getDeviceId().equals(siLicense.getDeviceId())) {
    				contains = true;
    				Integer newUnitCount = null;
    				Integer mergedLicenseUnitCount = mergedLicense.getDeviceUnitCount();
    				Integer siLicenseUnitCount = siLicense.getDeviceUnitCount();
    				if(mergedLicenseUnitCount != null && siLicenseUnitCount != null) {
    					newUnitCount = mergedLicenseUnitCount + siLicenseUnitCount;
    				}
    				Integer newQuantity = mergedLicense.getQuantity() + siLicense.getQuantity();
    				BigDecimal newOnetime = mergedLicense.getOnetimeRevenue().add(siLicense.getOnetimeRevenue());
    				BigDecimal newRecurring = mergedLicense.getRecurringRevenue().add(siLicense.getRecurringRevenue());
    				
    				mergedLicense.setDeviceUnitCount(newUnitCount);
    				mergedLicense.setQuantity(newQuantity);
    				mergedLicense.setOnetimeRevenue(newOnetime);
    				mergedLicense.setRecurringRevenue(newRecurring);
    				
    				break;
    			}
    		}
    		if(!contains) {
    			siLicensesMerged.add(siLicense);
    		}
    	}
    	
    	for(CSPLicenseBillingWrapper azureLicense : azureLicenses) {
    		String customerAzureName = azureLicense.getCustomerAzureName();
    		boolean customerMatch = false;
    		
    		for(CSPBilledContractService siLicense : siLicensesMerged) {
    			if(siLicense.getCustomerName().toUpperCase().equals(customerAzureName.toUpperCase()) || (siLicense.getCustomerAltName() != null && siLicense.getCustomerAltName().toUpperCase().equals(customerAzureName.toUpperCase()))) {
    				customerMatch = true;
    				break;
    			}
    		}
    		
    		for(CSPLicenseBillingLineItem azureLicenseLineItem : azureLicense.getLineItems()) {
    			String offerName = azureLicenseLineItem.getName();
    			AzureInvoiceReportLicenseLineItem lineitem = new AzureInvoiceReportLicenseLineItem();
        		lineitem.setAzureCustomerName(customerAzureName);
        		lineitem.setAzureSubscriptionId(azureLicenseLineItem.getId());
        		lineitem.setAzureOfferName(offerName);
        		lineitem.setAzureQuantity(azureLicenseLineItem.getQuantity());
        		lineitem.setAzureUnitPrice(azureLicenseLineItem.getUnitPrice());
        		lineitem.setAzureMonthlyCost(azureLicenseLineItem.getTotal());
        		
        		if(customerMatch) {
        			//match the device description to the offer name
	        		for (Iterator<CSPBilledContractService> iterator = siLicensesMerged.iterator(); iterator.hasNext();) {
	        			CSPBilledContractService siLicenseIt = iterator.next();
	        			String deviceDescription = siLicenseIt.getDeviceDescription();
	        		    if((siLicenseIt.getCustomerName().toUpperCase().equals(customerAzureName.toUpperCase()) || (siLicenseIt.getCustomerAltName() != null && siLicenseIt.getCustomerAltName().toUpperCase().equals(customerAzureName.toUpperCase())))
                                            && deviceDescription != null && deviceDescription.toUpperCase().equals(offerName.toUpperCase())) {
	        				lineitem.setSiCustomerName(siLicenseIt.getCustomerName());
	        				lineitem.setSiOnetimeRevenue(siLicenseIt.getOnetimeRevenue());
	        				lineitem.setSiRecurringRevenue(siLicenseIt.getRecurringRevenue());
	        				lineitem.setSiQuantity(siLicenseIt.getQuantity());
	        				lineitem.setSiDevicePartNumber(siLicenseIt.getDevicePartNumber());
	        				lineitem.setSiDeviceDescription(siLicenseIt.getDeviceDescription());
	        				lineitem.setSiDeviceUnitCount(siLicenseIt.getDeviceUnitCount());
	        				iterator.remove();
	        			}
	        		}
	        		lineitems.add(lineitem);
        		} else {
        			lineitems.add(lineitem);
        		}
    		}
    		
    		if(customerMatch) {
        		//otherwise add the item as an SI item only
        		for (Iterator<CSPBilledContractService> iterator = siLicensesMerged.iterator(); iterator.hasNext();) {
        			CSPBilledContractService siLicenseIt = iterator.next();
        		    if(siLicenseIt.getCustomerName().toUpperCase().equals(customerAzureName.toUpperCase()) || (siLicenseIt.getCustomerAltName() != null && siLicenseIt.getCustomerAltName().toUpperCase().equals(customerAzureName.toUpperCase()))) {
        		    	AzureInvoiceReportLicenseLineItem newLineitem = new AzureInvoiceReportLicenseLineItem();
        		    	newLineitem.setAzureCustomerName(customerAzureName);
        		    	newLineitem.setAzureSubscriptionId(""); // we don't use this and it isn't valid in the wrapper
        		    	newLineitem.setSiCustomerName(siLicenseIt.getCustomerName());
        		    	newLineitem.setSiOnetimeRevenue(siLicenseIt.getOnetimeRevenue());
        		    	newLineitem.setSiRecurringRevenue(siLicenseIt.getRecurringRevenue());
        		    	newLineitem.setSiQuantity(siLicenseIt.getQuantity());
        		    	newLineitem.setSiDevicePartNumber(siLicenseIt.getDevicePartNumber());
        		    	newLineitem.setSiDeviceDescription(siLicenseIt.getDeviceDescription());
        		    	newLineitem.setSiDeviceUnitCount(siLicenseIt.getDeviceUnitCount());
        				lineitems.add(newLineitem);
        				iterator.remove();
        			}
        		}
    		}
    	}
    	
    	for(CSPBilledContractService siLicense : siLicensesMerged) {
    		AzureInvoiceReportLicenseLineItem lineitem = new AzureInvoiceReportLicenseLineItem();
    		lineitem.setSiCustomerName(siLicense.getCustomerName());
			lineitem.setSiOnetimeRevenue(siLicense.getOnetimeRevenue());
			lineitem.setSiRecurringRevenue(siLicense.getRecurringRevenue());
			lineitem.setSiQuantity(siLicense.getQuantity());
			lineitem.setSiDevicePartNumber(siLicense.getDevicePartNumber());
			lineitem.setSiDeviceDescription(siLicense.getDeviceDescription());
			lineitem.setSiDeviceUnitCount(siLicense.getDeviceUnitCount());
			lineitems.add(lineitem);
    	}
    	
        for(AzureInvoiceReportLicenseLineItem lineitem : lineitems) {
            String key = lineitem.getAzureCustomerName();
            if(key == null) {
                key = lineitem.getSiCustomerName();
            }
            if(key == null) {
                log.warn("NULL key resulting from {}", lineitem.toString());
            } else {
                if(results.containsKey(key)) {
                        results.get(key).add(lineitem);
                } else {
                    List<AzureInvoiceReportLicenseLineItem> list = new ArrayList<AzureInvoiceReportLicenseLineItem>();
                    list.add(lineitem);
                    results.put(key, list);
                }
            }
        }
    	
    	return new TreeMap<String, List<AzureInvoiceReportLicenseLineItem>>(results);
    }
    
    private Map<String, List<AzureInvoiceReportOneTimeLineItem>> mergeOneTimeLineitems(List<CSPOneTimeBillingWrapper> azureOneTimes, List<CSPBilledContractService> siOneTimes) {
    	List<AzureInvoiceReportOneTimeLineItem> lineitems = new ArrayList<AzureInvoiceReportOneTimeLineItem>();
    	Map<String, List<AzureInvoiceReportOneTimeLineItem>> results = new HashMap<String, List<AzureInvoiceReportOneTimeLineItem>>();
    	
    	List<CSPBilledContractService> siOneTimesMerged = new ArrayList<CSPBilledContractService>();
    	for(CSPBilledContractService siOneTime : siOneTimes) {
    		boolean contains = false;
    		for(CSPBilledContractService mergedOneTime : siOneTimesMerged) {
    			if(mergedOneTime.getContractId().equals(siOneTime.getContractId()) && mergedOneTime.getDeviceId().equals(siOneTime.getDeviceId())) {
    				contains = true;
    				Integer newUnitCount = null;
    				Integer mergedOneTimeUnitCount = mergedOneTime.getDeviceUnitCount();
    				Integer siOneTimeUnitCount = siOneTime.getDeviceUnitCount();
    				if(mergedOneTimeUnitCount != null && siOneTimeUnitCount != null) {
    					newUnitCount = mergedOneTimeUnitCount + siOneTimeUnitCount;
    				}
    				Integer newQuantity = mergedOneTime.getQuantity() + siOneTime.getQuantity();
    				BigDecimal newOnetime = mergedOneTime.getOnetimeRevenue().add(siOneTime.getOnetimeRevenue());
    				BigDecimal newRecurring = mergedOneTime.getRecurringRevenue().add(siOneTime.getRecurringRevenue());
    				
    				mergedOneTime.setDeviceUnitCount(newUnitCount);
    				mergedOneTime.setQuantity(newQuantity);
    				mergedOneTime.setOnetimeRevenue(newOnetime);
    				mergedOneTime.setRecurringRevenue(newRecurring);
    				
    				break;
    			}
    		}
    		if(!contains) {
    			siOneTimesMerged.add(siOneTime);
    		}
    	}
    	
    	for(CSPOneTimeBillingWrapper azureOneTime : azureOneTimes) {
    		String customerAzureName = azureOneTime.getCustomerAzureName();
    		boolean customerMatch = false;
    		
    		for(CSPBilledContractService siOneTime : siOneTimesMerged) {
    			if(siOneTime.getCustomerName().toUpperCase().equals(customerAzureName.toUpperCase()) || (siOneTime.getCustomerAltName() != null && siOneTime.getCustomerAltName().toUpperCase().equals(customerAzureName.toUpperCase()))) {
    				customerMatch = true;
    				break;
    			}
    		}
    		
    		for(CSPOneTimeBillingLineItem azureOneTimeLineItem : azureOneTime.getLineItems()) {
    			String offerName = (StringUtils.isNotBlank(azureOneTimeLineItem.getSubscriptionName()) ? azureOneTimeLineItem.getSubscriptionName() : null);
                        String productName = (StringUtils.isNotBlank(azureOneTimeLineItem.getProductName()) ? azureOneTimeLineItem.getProductName() : null);
    			AzureInvoiceReportOneTimeLineItem lineitem = new AzureInvoiceReportOneTimeLineItem();
        		lineitem.setAzureCustomerName(customerAzureName);
        		lineitem.setAzureSubscriptionId(azureOneTimeLineItem.getId());
        		lineitem.setAzureOfferName(offerName == null ? productName : offerName);
        		lineitem.setAzureQuantity(azureOneTimeLineItem.getQuantity());
        		lineitem.setAzureUnitPrice(azureOneTimeLineItem.getUnitPrice());
        		lineitem.setCustomerTotal(azureOneTimeLineItem.getSubscriptionTotal());
                        lineitem.setTermAndBillingCycle(azureOneTimeLineItem.getTermAndBillingCycle());
        		
        		if(customerMatch) {
        			//match the device description to the offer name
	        		for (Iterator<CSPBilledContractService> iterator = siOneTimesMerged.iterator(); iterator.hasNext();) {
	        			CSPBilledContractService siOneTimeIt = iterator.next();
	        			String deviceDescription = siOneTimeIt.getDeviceDescription();
	        		    if((siOneTimeIt.getCustomerName().toUpperCase().equals(customerAzureName.toUpperCase()) ||
                                            (siOneTimeIt.getCustomerAltName() != null && siOneTimeIt.getCustomerAltName().toUpperCase().equals(customerAzureName.toUpperCase())))
                                            && deviceDescription != null && deviceDescription.toUpperCase().equals(lineitem.getAzureOfferName().toUpperCase())) {
                                        // this is rarely going to happen... the "offerName" (subscriptionName or productName) is just not going to match the device description, I think
	        				lineitem.setSiCustomerName(siOneTimeIt.getCustomerName());
	        				lineitem.setSiOnetimeRevenue(siOneTimeIt.getOnetimeRevenue());
	        				lineitem.setSiRecurringRevenue(siOneTimeIt.getRecurringRevenue());
	        				lineitem.setSiQuantity(siOneTimeIt.getQuantity());
	        				lineitem.setSiDevicePartNumber(siOneTimeIt.getDevicePartNumber());
	        				lineitem.setSiDeviceDescription(siOneTimeIt.getDeviceDescription());
	        				lineitem.setSiDeviceUnitCount(siOneTimeIt.getDeviceUnitCount());
	        				iterator.remove();
	        			}
	        		}
	        		lineitems.add(lineitem);
        		} else {
        			lineitems.add(lineitem);
        		}
    		}
    		
    		if(customerMatch) {
        		//otherwise add the item as an SI item only
        		for (Iterator<CSPBilledContractService> iterator = siOneTimesMerged.iterator(); iterator.hasNext();) {
        			CSPBilledContractService siOneTimeIt = iterator.next();
        		    if(siOneTimeIt.getCustomerName().toUpperCase().equals(customerAzureName.toUpperCase()) || (siOneTimeIt.getCustomerAltName() != null && siOneTimeIt.getCustomerAltName().toUpperCase().equals(customerAzureName.toUpperCase()))) {
        		    	AzureInvoiceReportOneTimeLineItem newLineitem = new AzureInvoiceReportOneTimeLineItem();
        		    	newLineitem.setAzureCustomerName(customerAzureName);
        		    	newLineitem.setAzureSubscriptionId(""); // we don't use this and it isn't valid in the wrapper
        		    	newLineitem.setSiCustomerName(siOneTimeIt.getCustomerName());
        		    	newLineitem.setSiOnetimeRevenue(siOneTimeIt.getOnetimeRevenue());
        		    	newLineitem.setSiRecurringRevenue(siOneTimeIt.getRecurringRevenue());
        		    	newLineitem.setSiQuantity(siOneTimeIt.getQuantity());
        		    	newLineitem.setSiDevicePartNumber(siOneTimeIt.getDevicePartNumber());
        		    	newLineitem.setSiDeviceDescription(siOneTimeIt.getDeviceDescription());
        		    	newLineitem.setSiDeviceUnitCount(siOneTimeIt.getDeviceUnitCount());
        				lineitems.add(newLineitem);
        				iterator.remove();
        			}
        		}
    		}
    	}
    	
    	for(CSPBilledContractService siOneTime : siOneTimesMerged) {
    		AzureInvoiceReportOneTimeLineItem lineitem = new AzureInvoiceReportOneTimeLineItem();
    		lineitem.setSiCustomerName(siOneTime.getCustomerName());
			lineitem.setSiOnetimeRevenue(siOneTime.getOnetimeRevenue());
			lineitem.setSiRecurringRevenue(siOneTime.getRecurringRevenue());
			lineitem.setSiQuantity(siOneTime.getQuantity());
			lineitem.setSiDevicePartNumber(siOneTime.getDevicePartNumber());
			lineitem.setSiDeviceDescription(siOneTime.getDeviceDescription());
			lineitem.setSiDeviceUnitCount(siOneTime.getDeviceUnitCount());
			lineitems.add(lineitem);
    	}
    	
        for(AzureInvoiceReportOneTimeLineItem lineitem : lineitems) {
            String key = lineitem.getAzureCustomerName();
            if(key == null) {
                key = lineitem.getSiCustomerName();
            }
            if(key == null) {
                log.warn("NULL key resulting from {}", lineitem.toString());
            } else {
                if(results.containsKey(key)) {
                        results.get(key).add(lineitem);
                } else {
                    List<AzureInvoiceReportOneTimeLineItem> list = new ArrayList<AzureInvoiceReportOneTimeLineItem>();
                    list.add(lineitem);
                    results.put(key, list);
                }
            }
        }
    	
    	return new TreeMap<String, List<AzureInvoiceReportOneTimeLineItem>>(results);
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/awsinvoice", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public AWSInvoiceReportWrapper getAWSInvoice(@RequestParam(value = "invd", required = true) @DateTimeFormat(pattern = "MM/yyyy") Date invoiceDate,
            @RequestParam(value = "detailed", required = false) Boolean includeBillingDetails) throws ServiceException {
    	DateTime invoiceDateTime = new DateTime(invoiceDate).withZone(DateTimeZone.forID(TZID))
                .withTime(0,0,0,0)
                .dayOfMonth()
                .withMinimumValue();
    	Integer month = invoiceDateTime.getMonthOfYear();
    	String year = String.valueOf(invoiceDateTime.getYear());
    	
    	AWSInvoiceReportWrapper awsWrapper =  serviceUsageService.getAWSInvoiceReportData(invoiceDateTime, (includeBillingDetails == null ? Boolean.FALSE : includeBillingDetails));
    	List<CSPBilledContractService> siAWSServices = contractRevenueService.serviceRevenueCSPForMonthOf(Device.DeviceType.aws, month, year);
    	List<AWSInvoiceReportUsageLineItem> usageLineItems = mergeAWSUsageLineitems(awsWrapper.getAWSBillingPeriods(), siAWSServices);
    	
    	awsWrapper.setUsageLineItems(usageLineItems);
    	awsWrapper.setSiAWSServices(siAWSServices);
    	return awsWrapper;
    }
    
    private List<AWSInvoiceReportUsageLineItem> mergeAWSUsageLineitems(List<AWSBillingPeriodWrapper> awsBillingPeriods, List<CSPBilledContractService> siAWSBillingPeriods) {
    	List<AWSInvoiceReportUsageLineItem> results = new ArrayList<AWSInvoiceReportUsageLineItem>();
    	
    	for(AWSBillingPeriodWrapper awsPeriod : awsBillingPeriods) {
    		AWSInvoiceReportUsageLineItem lineitem = new AWSInvoiceReportUsageLineItem();
    		lineitem.setAWSCustomerName(awsPeriod.getCustomerAWSName());
    		lineitem.setAWSSubscriptionId(awsPeriod.getAccountId());
    		lineitem.setAWSSubscriptionName(awsPeriod.getSubscriptionName());
    		lineitem.setAWSMonthlyCost(awsPeriod.getRawTotal());
    		
    		for (Iterator<CSPBilledContractService> iterator = siAWSBillingPeriods.iterator(); iterator.hasNext();) {
    			CSPBilledContractService siPeriod = iterator.next();
    		    if(awsPeriod.getAccountId().equals(siPeriod.getExternalSubscriptionId())) {
    				lineitem.setSiCustomerName(siPeriod.getCustomerName());
    				lineitem.setSiOnetimeRevenue(siPeriod.getOnetimeRevenue());
    				lineitem.setSiRecurringRevenue(siPeriod.getRecurringRevenue());
    				iterator.remove();
    			}
    		}
    		results.add(lineitem);
    	}
    	
    	for(CSPBilledContractService siPeriod : siAWSBillingPeriods) {
    		AWSInvoiceReportUsageLineItem lineitem = new AWSInvoiceReportUsageLineItem();
    		lineitem.setSiCustomerName(siPeriod.getCustomerName());
			lineitem.setSiOnetimeRevenue(siPeriod.getOnetimeRevenue());
			lineitem.setSiRecurringRevenue(siPeriod.getRecurringRevenue());
			results.add(lineitem);
    	}
    	
    	return results;
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/splareport", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public ResponseEntity<SPLAReport> splaReport(@RequestParam(value = "monthof", required = true) @DateTimeFormat(pattern = "MM/yyyy") Date monthof,
            @RequestParam(value = "cid", required = false) Long customerId,
            @RequestParam(value = "vendor", required = false) String vendor,
            @RequestParam(value = "splaid", required = false) Long splaId,
            @RequestParam(value = "spladevid", required = false) Long deviceId) throws ServiceException {
        DateTime applied = new DateTime(monthof);
        List<SPLARevenue> splaRevenues = contractRevenueService.splaRevenueReport(applied, customerId, deviceId, splaId, vendor);
        List<CostItem> splaCosts = contractDaoService.findSPLACosts(applied, customerId, deviceId, splaId, vendor);
        
        SPLAReport report = new SPLAReport();
        Map<Long, SPLAReport.SPLASummary> splaSummaryMap = new HashMap<Long, SPLAReport.SPLASummary>();
        for (CostItem splaCost : splaCosts) {
            SPLAReport.SPLAReportCustomer customer = new SPLAReport.SPLAReportCustomer(splaCost.getCustomerName());
            if (!report.getCustomers().contains(customer)) {
                report.getCustomers().add(customer);
            } else {
                for (SPLAReport.SPLAReportCustomer member : report.getCustomers()) {
                    if (member.getCustomer().equals(customer.getCustomer())) {
                        customer = member;
                        break;
                    }
                }
            }
            SPLAReport.SPLAReportContract contract = new SPLAReport.SPLAReportContract(splaCost.getContractId(), splaCost.getContractName());
            if (!customer.getContracts().contains(contract)) {
                customer.getContracts().add(contract);
            } else {
                for (SPLAReport.SPLAReportContract member : customer.getContracts()) {
                    if (member.getContractId().equals(contract.getContractId())) {
                        contract = member;
                        break;
                    }
                }
            }
            BigDecimal formattedAmount = splaCost.getAmount().setScale(2, RoundingMode.HALF_UP);
            contract.getCosts().add(new SPLAReport.SPLAReportCostRecord(splaCost.getName(), splaCost.getSplaId(), splaCost.getPartNumber(), splaCost.getDeviceId(), splaCost.getQuantity(), formattedAmount));
            SPLAReport.SPLASummary splaSummary = splaSummaryMap.get(splaCost.getSplaId());
            if (splaSummary == null) {
                splaSummaryMap.put(splaCost.getSplaId(), new SPLAReport.SPLASummary(splaCost.getSplaId(), splaCost.getName(), splaCost.getQuantity(), formattedAmount));
            } else {
                splaSummary.addQuantity(splaCost.getQuantity());
                splaSummary.addCost(formattedAmount);
            }
        }
        for (SPLAReport.SPLASummary splaSummary : splaSummaryMap.values()) {
            report.addSPLASummary(splaSummary);
        }
        for (SPLARevenue splaRevenue : splaRevenues) {
            SPLAReport.SPLAReportCustomer customer = new SPLAReport.SPLAReportCustomer(splaRevenue.getCustomer());
            if (!report.getCustomers().contains(customer)) {
                report.getCustomers().add(customer);
            } else {
                for (SPLAReport.SPLAReportCustomer member : report.getCustomers()) {
                    if (member.getCustomer().equals(customer.getCustomer())) {
                        customer = member;
                        break;
                    }
                }
            }
            SPLAReport.SPLAReportContract contract = new SPLAReport.SPLAReportContract(splaRevenue.getContractId(), splaRevenue.getContract());
            if (!customer.getContracts().contains(contract)) {
                customer.getContracts().add(contract);
            } else {
                for (SPLAReport.SPLAReportContract member : customer.getContracts()) {
                    if (member.getContractId().equals(contract.getContractId())) {
                        contract = member;
                        break;
                    }
                }
            }
            contract.getRevenues().add(splaRevenue);
        }
        return new ResponseEntity<SPLAReport>(report, OK);
     }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/renewals", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public List<Contract> queryForRenewalsData(@RequestParam(value = "rd", required = true) @DateTimeFormat(pattern = "MM/dd/yyyy") Date renewalDate,
            @RequestParam(value = "cid", required = false) Long customerId, @RequestParam(value = "sts", required = false) Contract.RenewalStatus status) throws ServiceException {
    	List<Contract.RenewalStatus> inStatus = new ArrayList<Contract.RenewalStatus>();
    	if(status != null) {
    		inStatus.add(status);
    	}
    	return contractRevenueService.contractsForRenewal(customerId, renewalDate, inStatus, Boolean.TRUE);
    }
    
    @RequestMapping(value = "/renewals", params = "export", method = RequestMethod.GET)
    public String queryForRenewalsDataExport(@RequestParam(value = "rd", required = true) @DateTimeFormat(pattern = "MM/dd/yyyy") Date renewalDate,
            @RequestParam(value = "cid", required = false) Long customerId, @RequestParam(value = "sts", required = false) Contract.RenewalStatus status, Model uiModel) throws ServiceException {
    	List<Contract.RenewalStatus> inStatus = new ArrayList<Contract.RenewalStatus>();
    	if(status != null) {
    		inStatus.add(status);
    	}
    	List<Contract> reportData = contractRevenueService.contractsForRenewal(customerId, renewalDate, inStatus, Boolean.TRUE);
    	
    	uiModel.addAttribute(ExcelView.EXPORT_RENEWALS_KEY, reportData);
        return "report/renewalsexport";
    }
}
