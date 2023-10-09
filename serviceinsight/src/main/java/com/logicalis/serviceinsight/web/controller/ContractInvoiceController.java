package com.logicalis.serviceinsight.web.controller;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.logicalis.serviceinsight.data.ContractInvoice;
import com.logicalis.serviceinsight.data.Service;
import com.logicalis.serviceinsight.service.BatchResult;
import com.logicalis.serviceinsight.service.ContractDaoService;
import com.logicalis.serviceinsight.service.ServiceException;

@Controller
@RequestMapping("/contractinvoices")
public class ContractInvoiceController extends BaseController {

	@Autowired
    ContractDaoService contractDaoService;
    @Autowired
    ConversionService conversionService;

    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public ContractInvoice contractInvoice(@PathVariable("id") Long id) throws ServiceException {
        return contractDaoService.contractInvoice(id);
    }
    
    @ResponseBody
    @ResponseStatus(CREATED)
    @RequestMapping(method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse save(@RequestBody ContractInvoice contractInvoice) {
        try {
            Long id = contractDaoService.saveContractInvoice(contractInvoice);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_new_contractinvoice", new Object[]{id}, LocaleContextHolder.getLocale()));
        } catch(ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_new_contractinvoice", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch(Exception e) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_new_contractinvoice", null, LocaleContextHolder.getLocale()));
        }
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(method = RequestMethod.PUT, consumes = APPLICATION_JSON_VALUE, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse update(@RequestBody ContractInvoice contractInvoice) {
        try {
            contractDaoService.updateContractInvoice(contractInvoice);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_update_contractinvoice", new Object[]{contractInvoice.getId()}, LocaleContextHolder.getLocale()));
        } catch(ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_update_contractinvoice", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch(Exception e) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_update_contractinvoice", null, LocaleContextHolder.getLocale()));
        }
    }
    
    @ResponseBody
    @ResponseStatus(CREATED)
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse delete(@PathVariable("id") Long id) {
        try {
            contractDaoService.deleteContractInvoice(id);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_delete_contractinvoice", new Object[]{id}, LocaleContextHolder.getLocale()));
        } catch(ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_delete_contractinvoice", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch(Exception e) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_delete_contractinvoice", null, LocaleContextHolder.getLocale()));
        }
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(params = "batch", method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse batch(@RequestBody ContractInvoice[] contractInvoices) {
        
        List<BatchResult> batchResults = contractDaoService.batchContractInvoices(contractInvoices);
        APIResponse.Status outcome = APIResponse.Status.OK;
        for (BatchResult batchResult : batchResults) {
            if (BatchResult.Result.failed.equals(batchResult.getResult())) {
                outcome = APIResponse.Status.ERROR;
                break;
            }
        }
        return new APIResponse(outcome, messageSource.getMessage("api_ok_batch_response", null, LocaleContextHolder.getLocale()), batchResults);
    }
	
}
