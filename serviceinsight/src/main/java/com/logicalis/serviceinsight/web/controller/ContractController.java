package com.logicalis.serviceinsight.web.controller;

import com.logicalis.serviceinsight.dao.Location;
import com.logicalis.serviceinsight.data.Contract;
import com.logicalis.serviceinsight.data.ContractGroup;
import com.logicalis.serviceinsight.data.FullContract;
import com.logicalis.serviceinsight.data.Customer;
import com.logicalis.serviceinsight.service.ApplicationDataDaoService;
import com.logicalis.serviceinsight.service.ContractDaoService;
import com.logicalis.serviceinsight.service.ContractRevenueService;
import com.logicalis.serviceinsight.service.DocManagementService;
import com.logicalis.serviceinsight.service.ServiceException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.CREATED;
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
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author poneil
 */
@Controller
@RequestMapping("/contracts")
public class ContractController extends BaseController {

    @Autowired
    ContractDaoService contractDaoService;
    @Autowired
    ContractRevenueService contractRevenueService;
    @Autowired
    ConversionService conversionService;
    @Autowired
    ApplicationDataDaoService applicationDataDaoService;
    @Autowired
    DocManagementService docManagementService;
    
    @Value("${sn.url}")
    private String serviceNowUrl;
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public Contract contract(@PathVariable("id") Long id) throws ServiceException {

        return contractDaoService.contract(id);
    }
    
    @ResponseBody
    @ResponseStatus(CREATED)
    @RequestMapping(method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse save(@RequestBody Contract contract) {
        try {
            Long id = contractDaoService.saveContract(contract);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_new_contract", new Object[]{id}, LocaleContextHolder.getLocale()));
        } catch(ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_new_contract", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch(Exception e) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_new_contract", null, LocaleContextHolder.getLocale()));
        }
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(method = RequestMethod.PUT, consumes = APPLICATION_JSON_VALUE, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse update(@RequestBody Contract contract) {
        try {
            contractDaoService.updateContract(contract);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_update_contract", new Object[]{contract.getId()}, LocaleContextHolder.getLocale()));
        } catch(ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_update_contract", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch(Exception e) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_update_contract", null, LocaleContextHolder.getLocale()));
        }
    }
    
    @ResponseBody
    @ResponseStatus(CREATED)
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse delete(@PathVariable("id") Long id) {
        try {
            contractDaoService.deleteContract(id);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_delete_contract", new Object[]{id}, LocaleContextHolder.getLocale()));
        } catch(ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_delete_contract", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch(Exception e) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_delete_contract", null, LocaleContextHolder.getLocale()));
        }
    }
    
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String showContract(Model uiModel, @PathVariable("id") Long id) throws ServiceException {
    	DateTime now = new DateTime();
    	uiModel.addAttribute("contractservices", contractRevenueService.serviceRevenueRollupForMonthOf(id, null, now.monthOfYear().get(), now.year().getAsString(), null));
    	uiModel.addAttribute("services", contractDaoService.servicesMap());
    	
    	uiModel.addAttribute("fullServices", contractDaoService.services());
    	
    	Contract contract = contractDaoService.contract(id);
        uiModel.addAttribute("contract", contract);
        
        Customer customer = contractDaoService.customer(contract.getCustomerId());
        uiModel.addAttribute("customer", customer);
        
        List<ContractGroup> groups = contractRevenueService.contractGroups(id);
        uiModel.addAttribute("groups", groups);
        
        List<Location> locations = applicationDataDaoService.locations(Boolean.TRUE);
        uiModel.addAttribute("locations", locations);
        
        uiModel.addAttribute("serviceNowUrl", serviceNowUrl);
        uiModel.addAttribute("renewalStatuses", Arrays.asList(Contract.RenewalStatus.values()));
        
        return "contracts/show";
    }
    
    @RequestMapping(params = "jobnum", method = RequestMethod.GET)
    public String showContractByJobAndCustomer(Model uiModel, @RequestParam(value = "cid", required = true) Long customerId, HttpServletRequest request) throws ServiceException {
        String jobnumber = request.getParameter("jobnum");
        if (StringUtils.isBlank(jobnumber)) {
            throw new IllegalArgumentException("job number is a required field");
        }
        Contract contract = contractDaoService.findContractByJobNumberAndCompanyId(jobnumber, customerId);
        if (contract != null) {
            return showContract(uiModel, contract.getId());
        } else {
            throw new ServiceException("Contract not found by Job Number or ID");
        }
    }
    
    @RequestMapping(value = "/{id}/{gid}", method = RequestMethod.GET)
    public String showContractForGroup(Model uiModel, @PathVariable("id") Long id, @PathVariable("gid") Long gid) throws ServiceException {
    	DateTime now = new DateTime();
    	uiModel.addAttribute("contractservices", contractRevenueService.serviceRevenueRollupForMonthOf(id, gid, now.monthOfYear().get(), now.year().getAsString(), null));
    	uiModel.addAttribute("services", contractDaoService.servicesMap());
    	
    	Contract contract = contractDaoService.contract(id);
        uiModel.addAttribute("contract", contract);
        
        Customer customer = contractDaoService.customer(contract.getCustomerId());
        uiModel.addAttribute("customer", customer);
        
        uiModel.addAttribute("serviceNowUrl", serviceNowUrl);
        
        return "contracts/show";
    }

    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public List<Contract> contracts(@RequestParam(value = "cid", required = true) Long customerId, @RequestParam(value = "a", required = false) Boolean archived) {
        return contractRevenueService.contracts(customerId, archived);
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(method = RequestMethod.GET, params = "full", produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public List<Contract> fullContracts(@RequestParam(value = "cid", required = true) Long customerId, @RequestParam(value = "a", required = false) Boolean archived) {
        return contractRevenueService.fullContracts(customerId, archived);
    }

    @RequestMapping(method = RequestMethod.GET)
    public String contracts(Model uiModel) {

        // return ALL customers for a select list
        uiModel.addAttribute("customers", contractDaoService.customers(false, true, false));
        uiModel.addAttribute("renewalStatuses", Arrays.asList(Contract.RenewalStatus.values()));
        return "contracts/index";
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "docs/{id}/upload", method = RequestMethod.POST)
    public String uploadContract(@PathVariable("id") Long contractId, @RequestParam("contractfile") MultipartFile file) {
    	StringBuffer response = new StringBuffer();
        String status = "error";
        String message = "";
    	if (!file.isEmpty()) {
            try {
            	docManagementService.storeS3Contract(file, contractId);
                status = "success";
                message = messageSource.getMessage("ui_ok_file_upload", null, LocaleContextHolder.getLocale());
            } catch (ServiceException ex) {
                log.debug("Showing exception from Contract Upload", ex);
                message = ex.getMessage();
            } catch (Exception ex) {
                log.debug("Showing exception from Contract Upload", ex);
                message = ex.getMessage();
            }
        } else {
            message = messageSource.getMessage("import_error_empty_file", null, LocaleContextHolder.getLocale());
        }
    	//we have to do a custom response here, since we are posting back to an iframe and parse from there
    	response.append("<span id=\"contract-iframe-status\">").append(status).append("</span>").append("<span id=\"contract-iframe-message\">").append(message).append("</span>");
    	return response.toString();
    }
    
    @RequestMapping(value = "docs/{id}/download", method = RequestMethod.GET)
    public ResponseEntity<byte[]> downloadContractFile(@PathVariable("id") Long contractId) throws ServiceException {
    	Contract contract = contractDaoService.contract(contractId);
    	if(contract == null) {
    		throw new ServiceException(messageSource.getMessage("contract_not_found_for_id", new Object[]{contractId}, LocaleContextHolder.getLocale()));
    	}
    	String path = contract.getFilePath();
    	
        //// get the file
        ResponseEntity<byte[]> response = docManagementService.retrieveS3Contract(path);
        MediaType contentType = response.getHeaders().getContentType();
        String sfx = docManagementService.getFileSuffix(contentType.toString());
        path += sfx;
        
        byte[] document = response.getBody();
        HttpHeaders header = new HttpHeaders();
        header.setContentType(contentType);
        header.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + path);
        header.setContentLength(document.length);
        return new ResponseEntity<byte[]>(document, header, OK);
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "docs/{id}", method = RequestMethod.DELETE)
    public APIResponse deleteContractFile(@PathVariable("id") Long contractId) throws ServiceException {
    	try {
    		docManagementService.deleteS3Contract(contractId);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_delete_document", new Object[]{contractId}, LocaleContextHolder.getLocale()));
        } catch(ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_delete_document", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch(Exception e) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general", null, LocaleContextHolder.getLocale()));
        }
    }
}
