package com.logicalis.serviceinsight.web.controller;

import com.logicalis.serviceinsight.data.ContractAdjustment;
import com.logicalis.serviceinsight.data.ContractInvoice;
import com.logicalis.serviceinsight.data.ContractServiceSubscription;
import com.logicalis.serviceinsight.data.Microsoft365SubscriptionConfig;
import com.logicalis.serviceinsight.data.ContractServiceChangedConsolidatedWrapper;
import com.logicalis.serviceinsight.data.ContractServiceChangedViewWrapper;
import com.logicalis.serviceinsight.data.Service;
import com.logicalis.serviceinsight.representation.ContractRollupMonthRepresentation;
import com.logicalis.serviceinsight.representation.ContractRollupRecord;
import com.logicalis.serviceinsight.service.BatchResult;
import com.logicalis.serviceinsight.service.ContractDaoService;
import com.logicalis.serviceinsight.service.ContractRevenueService;
import com.logicalis.serviceinsight.service.MicrosoftPricingService;
import com.logicalis.serviceinsight.service.ServiceException;
import com.logicalis.serviceinsight.service.ServiceUsageService;
import com.logicalis.serviceinsight.web.view.ExcelView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author poneil
 */
@Controller
@RequestMapping("/contractservices")
public class ContractServiceController extends BaseController {
    
    private static final String FILE_PATH = "/tmp/";
    private static String OS = System.getProperty("os.name").toLowerCase();
    private static String USER_HOME = System.getProperty("user.home").toLowerCase();
    
    @Autowired
    ServiceUsageService serviceUsageService;
    @Autowired
    ContractDaoService contractDaoService;
    @Autowired
    ContractRevenueService contractRevenueService;
    @Autowired
    ConversionService conversionService;
    @Autowired
    MicrosoftPricingService microsoftPricingService;
    
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String show(Model uiModel, @PathVariable("id") Long id) throws ServiceException {

        uiModel.addAttribute("service", contractDaoService.contractService(id));
        return "services/show";
    }

    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public Service contractService(@PathVariable("id") Long id) throws ServiceException {
        return contractDaoService.contractService(id);
    }
    
    @ResponseBody
    @ResponseStatus(CREATED)
    @RequestMapping(method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse save(@RequestBody Service service) {
        try {
            Long id = contractDaoService.saveContractService(service, true);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_new_contractservice", new Object[]{id}, LocaleContextHolder.getLocale()));
        } catch(ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_new_contractservice", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch(Exception e) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_new_contractservice", null, LocaleContextHolder.getLocale()));
        }
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(params = "batch", method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse batch(@RequestBody Service[] services) {
        
    	List<BatchResult> batchResults = new ArrayList<BatchResult>();
    	try {
    		batchResults = contractDaoService.batchContractServices(services);
    		
    		APIResponse.Status outcome = APIResponse.Status.OK;
            for (BatchResult batchResult : batchResults) {
                if (BatchResult.Result.failed.equals(batchResult.getResult())) {
                    outcome = APIResponse.Status.ERROR;
                    break;
                }
            }
            return new APIResponse(outcome, messageSource.getMessage("api_ok_batch_response", null, LocaleContextHolder.getLocale()), batchResults);
    	} catch(ServiceException se) {
    		se.printStackTrace();
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_update_contractservice", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch(Exception e) {
        	e.printStackTrace();
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_update_contractservice", new Object[]{e.getMessage()}, LocaleContextHolder.getLocale()));
        }
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(method = RequestMethod.PUT, consumes = APPLICATION_JSON_VALUE, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse update(@RequestBody Service service) {
        try {
            contractDaoService.updateContractService(service, true);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_update_contractservice", new Object[]{service.getId()}, LocaleContextHolder.getLocale()));
        } catch(ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_update_contractservice", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch(Exception e) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_update_contractservice", null, LocaleContextHolder.getLocale()));
        }
    }
    
    @ResponseBody
    @ResponseStatus(CREATED)
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse delete(@PathVariable("id") Long id) {
        try {
            contractDaoService.deleteContractService(id, true);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_delete_contractservice", new Object[]{id}, LocaleContextHolder.getLocale()));
        } catch(ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_delete_contractservice", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch(Exception e) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_delete_contractservice", null, LocaleContextHolder.getLocale()));
        }
    }

    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(params = "rollup", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public ContractRollupMonthRepresentation alltimeRollup(@RequestParam(value = "cid", required = true) Long contractId,
            @RequestParam(value = "cgid", required = false) Long contractGroupId, @RequestParam(value = "sts", required = true) Service.Status status) {
    	List<Service> contractServices = contractRevenueService.serviceRevenueRollup(contractId, contractGroupId, status);
    	List<ContractAdjustment> contractAdjustments = contractRevenueService.contractAdjustmentsForContract(contractId, contractGroupId, status);
    	List<ContractRollupRecord> rollupRecords =  convertRecordsToRollups(contractServices, contractAdjustments, Boolean.FALSE);
    	return new ContractRollupMonthRepresentation(rollupRecords, null);
    }

    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/{month}/{year}", params = "rollup", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public ContractRollupMonthRepresentation monthlyRollup(@PathVariable("month") Integer month, @PathVariable("year") String year,
            @RequestParam(value = "cid", required = true) Long contractId, @RequestParam(value = "cgid", required = false) Long contractGroupId, @RequestParam(value = "sts", required = true) Service.Status status,
            @RequestParam(value = "civ", required = true) Boolean ciRollupView) {
    	List<Service> contractServices = new ArrayList<Service>(); 
		if(ciRollupView) {
			contractServices = contractRevenueService.serviceRevenueParentRecordsForMonthOf(contractId, contractGroupId, month, year, status, Boolean.FALSE);
		} else {
			contractServices = contractRevenueService.serviceRevenueRollupForMonthOf(contractId, contractGroupId, month, year, status);
		}
    	List<ContractAdjustment> contractAdjustments = new ArrayList<ContractAdjustment>();
    	ContractInvoice contractInvoice = null;
    	try {
    		contractInvoice = contractRevenueService.findContractInvoiceBySearchCriteria(contractId, month, year);
    	} catch (Exception any) {
    		//handle exception
    	}
    	
    	if(!Service.Status.pending.equals(status)) {
    		contractAdjustments = contractRevenueService.contractAdjustmentsForMonthOfWithContractUpdate(contractId, contractGroupId, month, year, status);
    	}
    	
    	List<ContractRollupRecord> rollupRecords = convertRecordsToRollups(contractServices, contractAdjustments, ciRollupView);
        return new ContractRollupMonthRepresentation(rollupRecords, contractInvoice);
    }
    
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/{month}/{year}", params = "summary", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public ContractServiceChangedViewWrapper monthlyRollupData(@PathVariable("month") Integer month, @PathVariable("year") String year,
            @RequestParam(value = "cid", required = true) Long contractId,
            @RequestParam(value = "cgid", required = false) Long contractGroupId, Model uiModel) throws ServiceException {
    	
        ContractServiceChangedConsolidatedWrapper data = contractRevenueService.wrapChangedConsolidatedContractServices(contractId, contractGroupId, month, year, true);
        ContractServiceChangedViewWrapper wrapper = new ContractServiceChangedViewWrapper();
        
        List<ContractRollupRecord> previousMonth = convertRecordsToRollups(data.getPreviousMonth(), data.getPreviousMonthAdjustments(), Boolean.FALSE);
        List<ContractRollupRecord> added = convertRecordsToRollups(data.getAdded(), data.getAddedAdjustments(), Boolean.FALSE);
        List<ContractRollupRecord> removed = convertRecordsToRollups(data.getRemoved(), data.getRemovedAdjustments(), Boolean.FALSE);
        
        wrapper.setPreviousMonth(previousMonth);
        wrapper.setAdded(added);
        wrapper.setRemoved(removed);
        wrapper.setContractInvoice(data.getContractInvoice());
        wrapper.setOnetimeDifference(data.getOnetimeDifference());
        wrapper.setRecurringDifference(data.getRecurringDifference());
        wrapper.setOnetimeTotal(data.getTotalOnetime());
        wrapper.setRecurringTotal(data.getTotalRecurring());
        
        return wrapper;
    }
    
    @RequestMapping(value = "/{month}/{year}", method = RequestMethod.GET)
    public String monthlyRollupView(@PathVariable("month") Integer month, @PathVariable("year") String year,
            @RequestParam(value = "cid", required = true) Long contractId,
            @RequestParam(value = "cgid", required = false) Long contractGroupId, 
            @RequestParam(value = "civ", required = false) Boolean ciRollupView, Model uiModel) throws ServiceException {
        if(ciRollupView != null && ciRollupView) {
        	uiModel.addAttribute(ExcelView.EXPORT_SDM_CUSTOMER_KEY, contractRevenueService.wrapSDMCustomerExport(contractId, contractGroupId, month, year));
        } else {
        	uiModel.addAttribute(ExcelView.EXPORT_SDM_KEY, contractRevenueService.wrapChangedConsolidatedContractServices(contractId, contractGroupId, month, year, true));
        }
    	
        return "contractservices/monthlyrollup";
    }
    
    @RequestMapping(value = "/{month}/{year}", params = "rollupByQueryExport", method = RequestMethod.GET)
    public String monthlyRollupViewBySearchCriteriaExport(@PathVariable("month") Integer month, @PathVariable("year") String year,
            @RequestParam(value = "customer", required = false) String customerName, @RequestParam(value = "manager", required = false) Long sdmId, 
            @RequestParam(value = "showdetail", required = false) Boolean showDetail, @RequestParam(value = "invsts", required = false) String invoiceStatus, Model uiModel) throws ServiceException {
    	if(showDetail == false) {
    		uiModel.addAttribute(ExcelView.EXPORT_CHANGED_CONSOLIDATED_NO_DETAIL_KEY, contractRevenueService.wrapChangedConsolidatedContractServicesBySearch(customerName, sdmId, month, year, false, invoiceStatus));
    	} else {
    		uiModel.addAttribute(ExcelView.EXPORT_CHANGED_CONSOLIDATED_KEY, contractRevenueService.wrapChangedConsolidatedContractServicesBySearch(customerName, sdmId, month, year, false, invoiceStatus));
    	}
    	
        return "contractservices/monthlyrollup";
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/{month}/{year}", params = "rollupByQuery", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public List<ContractServiceChangedConsolidatedWrapper> monthlyRollupViewBySearchCriteria(@PathVariable("month") Integer month, @PathVariable("year") String year,
            @RequestParam(value = "customer", required = false) String customerName, @RequestParam(value = "manager", required = false) Long sdmId, 
            @RequestParam(value = "invsts", required = false) String invoiceStatus, Model uiModel) throws ServiceException {
        return contractRevenueService.wrapChangedConsolidatedContractServicesBySearch(customerName, sdmId, month, year, false, invoiceStatus);
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/{year}", params = "rollup", method = RequestMethod.GET)
    public Map<String, List<Service>> yearlyRollup(@PathVariable("year") String year,
            @RequestParam(value = "cid", required = true) Long contractId,
            @RequestParam(value = "cgid", required = false) Long contractGroupId) {
        return contractRevenueService.serviceRevenueForYear(contractId, contractGroupId, year);
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/{month}/{year}", params = "parents", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public List<Service> getAvailableParents(@PathVariable("month") Integer month, @PathVariable("year") String year,
            @RequestParam(value = "cid", required = true) Long contractId, @RequestParam(value = "did", required = true) Long deviceId) throws ServiceException {
        return contractDaoService.contractServiceParentRecordsForDeviceAndMonthOf(contractId, deviceId, month, year);
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/map", method = RequestMethod.POST, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse getAvailableParents(@RequestParam(value = "cid", required = true) Long childId, @RequestParam(value = "pid", required = true) Long parentId) throws ServiceException {
    	try {
            contractDaoService.mapChildToParent(childId, parentId);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_map_contractservice", null, LocaleContextHolder.getLocale()));
        } catch(ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_update_contractservice", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch(Exception e) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_update_contractservice", null, LocaleContextHolder.getLocale()));
        }
    }

    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public List<Service> recordsForService(@RequestParam(value = "cid", required = true) Long contractId,
            @RequestParam(value = "cgid", required = false) Long contractGroupId,
            @RequestParam(value = "sid", required = true) Long serviceId,
            @RequestParam(value = "did", required = false) Long deviceId,
            @RequestParam(value = "sd", required = true) @DateTimeFormat(pattern = "MMddyyyy") Date startDate,
            @RequestParam(value = "ed", required = true) @DateTimeFormat(pattern = "MMddyyyy") Date endDate,
            @RequestParam(value = "sts", required = true) Service.Status status) {
        return contractRevenueService.serviceRevenueRecordsForFilter(contractId, contractGroupId, serviceId, deviceId,
                startDate, endDate, status);
    }

    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/updated", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public List<Service> servicesForContractUpdate(@RequestParam(value = "cuid", required = true) Long contractUpdateId) {
        return contractRevenueService.contractUpdateServices(contractUpdateId);
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/search", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public List<Service> contractServiceSearch(@RequestParam(value = "cid", required = false) Long contractId, @RequestParam(value = "name", required = true) String name) throws ServiceException {
        return contractDaoService.searchContractServices(name, contractId);
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/import", method = RequestMethod.POST)
    public String importContractServices(@RequestParam("templatefile") MultipartFile file, Model uiModel) {
    	StringBuffer response = new StringBuffer();
        String status = "error";
        String message = "";
    	if (!file.isEmpty()) {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmm").format(new DateTime().toDate());
            try {
                InputStream is = file.getInputStream();
                File uploaded;
                if (OS.indexOf("win") >= 0) {
                    uploaded = new File(USER_HOME + "\\Downloads\\ImportCS_"+timestamp+"_"+file.getOriginalFilename());
                } else {
                    uploaded = new File(FILE_PATH + "ImportCS_"+timestamp+"_"+file.getOriginalFilename());
                }
                FileOutputStream fos = new FileOutputStream(uploaded);
                while (is.available() > 0) {
                    fos.write(is.read());
                }
                is.close();
                fos.close();
                log.debug("uploaded: " + uploaded.getAbsolutePath());
                contractDaoService.importContractServices(uploaded);
                
                status = "success";
                message = messageSource.getMessage("ui_ok_file_upload", null, LocaleContextHolder.getLocale());
            } catch (ServiceException ex) {
                log.debug("Showing exception from Contract Service Import", ex);
                message = ex.getMessage();
            } catch (Exception ex) {
                log.debug("Showing exception from Contract Service Import", ex);
                message = ex.getMessage();
            }
        } else {
            message = messageSource.getMessage("import_error_empty_file", null, LocaleContextHolder.getLocale());
        }
    	//we have to do a custom response here, since we are posting back to an iframe and parse from there
    	response.append("<span id=\"iframe-status\">").append(status).append("</span>").append("<span id=\"iframe-message\">").append(message).append("</span>");
    	return response.toString();
    }
    
    private List<ContractRollupRecord> convertRecordsToRollups(List<Service> contractServices, List<ContractAdjustment> contractAdjustments, Boolean rollupRelatedLineItems) {
    	List<ContractRollupRecord> reps = new ArrayList<ContractRollupRecord>();
    	
    	if(contractServices != null) {
	    	for(Service service : contractServices) {
	    		ContractRollupRecord rep = new ContractRollupRecord(service, rollupRelatedLineItems);
	    		reps.add(rep);
	    	}
    	}
    	
    	if(contractAdjustments != null) {
	    	String displayName = messageSource.getMessage("ui_title_contract_adjustment_row", null, LocaleContextHolder.getLocale());
	    	for(ContractAdjustment adjustment : contractAdjustments) {
	    		ContractRollupRecord rep = new ContractRollupRecord(adjustment, displayName);
	    		reps.add(rep);
	    	}
    	}
    	
    	return reps;
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/subscription", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public List<ContractServiceSubscription> contractServiceSubscriptions(@RequestParam(value = "cid", required = false) Long contractId) {
    	return contractDaoService.contractServiceSubscriptions(contractId, null);
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/subscription/{id}", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public ContractServiceSubscription contractServiceSubscription(@PathVariable("id") Long id) throws ServiceException {
        return contractDaoService.contractServiceSubscription(id);
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/subscription/{month}/{year}", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public List<ContractServiceSubscription> monthlyContractServiceAzures(@PathVariable("month") Integer month, @PathVariable("year") String year, @RequestParam(value = "cid", required = true) Long contractId) {
    	return contractDaoService.contractServiceSubscriptionsForMonthOf(contractId, month, year, null);
    }
    
    @ResponseBody
    @ResponseStatus(CREATED)
    @RequestMapping(value = "/subscription", method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse createContractServiceSubscription(@RequestBody ContractServiceSubscription subscriptionService) {
        try {
            Long id = contractDaoService.saveContractServiceSubscription(subscriptionService, true);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_new_contractservice", new Object[]{id}, LocaleContextHolder.getLocale()));
        } catch(ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_new_contractservice", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch(Exception e) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_new_contractservice", null, LocaleContextHolder.getLocale()));
        }
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/subscription", method = RequestMethod.PUT, consumes = APPLICATION_JSON_VALUE, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse updateContractServiceSubscription(@RequestBody ContractServiceSubscription subscriptionService) {
        try {
            contractDaoService.updateContractServiceSubscription(subscriptionService, true);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_update_contractserviceazure", new Object[]{subscriptionService.getId()}, LocaleContextHolder.getLocale()));
        } catch(ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_update_contractserviceazure", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch(Exception e) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_update_contractserviceazure", null, LocaleContextHolder.getLocale()));
        }
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/subscription/{id}", method = RequestMethod.DELETE, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse deleteContractServiceSubscription(@PathVariable("id") Long id) {
        try {
            contractDaoService.deleteContractServiceSubscription(id, true);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_delete_contractserviceazure", new Object[]{id}, LocaleContextHolder.getLocale()));
        } catch(ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_delete_contractserviceazure", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch(Exception e) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_delete_contractserviceazure", null, LocaleContextHolder.getLocale()));
        }
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/m365subscription", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public List<Microsoft365SubscriptionConfig> m365SubscriptionConfigs(@RequestParam(value = "cid", required = true) Long contractId) {
    	return microsoftPricingService.getMicrosoft365SubscriptionConfigForContract(contractId);
    }
    
    @ResponseBody
    @ResponseStatus(CREATED)
    @RequestMapping(value = "/m365subscription", method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse createM365SubscriptionConfig(@RequestBody Microsoft365SubscriptionConfig subscriptionConfig) {
        try {
            Long id = microsoftPricingService.saveMicrosoft365SubscriptionConfig(subscriptionConfig);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_new_contractservice", new Object[]{id}, LocaleContextHolder.getLocale()));
        } catch(ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_m365_config", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch(Exception e) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_m365_config", null, LocaleContextHolder.getLocale()));
        }
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/m365subscription", method = RequestMethod.PUT, consumes = APPLICATION_JSON_VALUE, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse updateM365SubscriptionConfig(@RequestBody Microsoft365SubscriptionConfig subscriptionConfig) {
        try {
            microsoftPricingService.updateMicrosoft365SubscriptionConfig(subscriptionConfig);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_new_contractservice", null, LocaleContextHolder.getLocale()));
        } catch(ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_m365_config", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch(Exception e) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_m365_config", null, LocaleContextHolder.getLocale()));
        }
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/m365subscription/{id}", method = RequestMethod.DELETE, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse deleteM365SubscriptionConfig(@PathVariable("id") Long id) {
        try {
            microsoftPricingService.deleteMicrosoft365SubscriptionConfig(id);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_new_contractservice", null, LocaleContextHolder.getLocale()));
        } catch(ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_m365_config", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch(Exception e) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_m365_config", null, LocaleContextHolder.getLocale()));
        }
    }
    
}
