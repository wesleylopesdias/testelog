package com.logicalis.serviceinsight.web.controller;

import com.logicalis.serviceinsight.data.Contract;
import com.logicalis.serviceinsight.data.ContractUpdate;
import com.logicalis.serviceinsight.service.ContractDaoService;
import com.logicalis.serviceinsight.service.ContractRevenueService;
import com.logicalis.serviceinsight.service.DocManagementService;
import com.logicalis.serviceinsight.service.ServiceException;
import com.logicalis.serviceinsight.service.ServiceUsageService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

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
@RequestMapping("/contractupdates")
public class ContractUpdateController extends BaseController {

    @Autowired
    ServiceUsageService serviceUsageService;
    @Autowired
    ContractDaoService contractDaoService;
    @Autowired
    ContractRevenueService contractRevenueService;
    @Autowired
    ConversionService conversionService;
    @Autowired
    DocManagementService docManagementService;

    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public ContractUpdate contractService(@PathVariable("id") Long id) throws ServiceException {

        return contractDaoService.contractUpdate(id);
    }

    @ResponseBody
    @ResponseStatus(CREATED)
    @RequestMapping(method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse save(@RequestBody ContractUpdate contractUpdate) {
        try {
            Long id = contractDaoService.saveContractUpdate(contractUpdate);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_new_contractupdate", new Object[]{id}, LocaleContextHolder.getLocale()));
        } catch (ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_new_contractupdate", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch (Exception e) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_new_contractupdate", null, LocaleContextHolder.getLocale()));
        }
    }

    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(method = RequestMethod.PUT, consumes = APPLICATION_JSON_VALUE, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse update(@RequestBody ContractUpdate contractUpdate) {
        try {
            contractDaoService.updateContractUpdate(contractUpdate);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_update_contractupdate", new Object[]{contractUpdate.getId()}, LocaleContextHolder.getLocale()));
        } catch (ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_update_contractupdate", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch (Exception e) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_update_contractupdate", null, LocaleContextHolder.getLocale()));
        }
    }

    @ResponseBody
    @ResponseStatus(CREATED)
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse delete(@PathVariable("id") Long id) {
        try {
            contractDaoService.deleteContractUpdate(id);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_delete_contractupdate", new Object[]{id}, LocaleContextHolder.getLocale()));
        } catch (ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_delete_contractupdate", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch (Exception e) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_delete_contractupdate", null, LocaleContextHolder.getLocale()));
        }
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String showContractUpdate(Model uiModel, @PathVariable("id") Long id) throws ServiceException {

        uiModel.addAttribute("contractUpdate", contractDaoService.contractUpdate(id));
        return "contractupdates/show";
    }

    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public List<ContractUpdate> contractUpdates(@RequestParam(value = "cid", required = true) Long contractId) {

        return contractRevenueService.contractUpdates(contractId);
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "docs/{id}/upload", method = RequestMethod.POST)
    public String uploadContract(@PathVariable("id") Long contractUpdateId, @RequestParam("contractfile") MultipartFile file) {
    	StringBuffer response = new StringBuffer();
        String status = "error";
        String message = "";
    	if (!file.isEmpty()) {
            try {
            	docManagementService.storeS3ContractUpdate(file, contractUpdateId);
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
    public ResponseEntity<byte[]> downloadContractUpdateFile(@PathVariable("id") Long contractUpdateId) throws ServiceException {
    	ContractUpdate contractUpdate = contractDaoService.contractUpdate(contractUpdateId);
    	if(contractUpdate == null) {
    		throw new ServiceException(messageSource.getMessage("contract_update_not_found_for_id", new Object[]{contractUpdateId}, LocaleContextHolder.getLocale()));
    	}
    	String path = contractUpdate.getFilePath();
    	
        //// get the file
        ResponseEntity<byte[]> response = docManagementService.retrieveS3ContractUpdate(path);
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
    public APIResponse deleteContractFile(@PathVariable("id") Long contractUpdateId) throws ServiceException {
    	try {
    		docManagementService.deleteS3ContractUpdate(contractUpdateId);
    		return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_delete_document", new Object[]{contractUpdateId}, LocaleContextHolder.getLocale()));
        } catch(ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_delete_document", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch(Exception e) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general", null, LocaleContextHolder.getLocale()));
        }
    }
}
