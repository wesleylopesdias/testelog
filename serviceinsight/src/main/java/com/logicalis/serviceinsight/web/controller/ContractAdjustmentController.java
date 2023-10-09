package com.logicalis.serviceinsight.web.controller;

import com.logicalis.serviceinsight.data.ContractAdjustment;
import com.logicalis.serviceinsight.data.Service;
import com.logicalis.serviceinsight.service.BatchResult;
import com.logicalis.serviceinsight.service.ContractDaoService;
import com.logicalis.serviceinsight.service.ContractRevenueService;
import com.logicalis.serviceinsight.service.ServiceException;
import com.logicalis.serviceinsight.service.ServiceUsageService;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.convert.ConversionService;
import org.springframework.format.annotation.DateTimeFormat;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 *
 * @author poneil
 */
@Controller
@RequestMapping("/contractadjustments")
public class ContractAdjustmentController {

    private final Logger log = LoggerFactory.getLogger(ContractAdjustmentController.class);
    @Autowired
    ServiceUsageService serviceUsageService;
    @Autowired
    ContractDaoService contractDaoService;
    @Autowired
    ContractRevenueService contractRevenueService;
    @Autowired
    ConversionService conversionService;
    @Autowired
    MessageSource messageSource;

    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public ContractAdjustment contractService(@PathVariable("id") Long id) throws ServiceException {

        return contractDaoService.contractAdjustment(id);
    }

    @ResponseBody
    @ResponseStatus(CREATED)
    @RequestMapping(method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse save(@RequestBody ContractAdjustment contractAdjustment) {
        try {
            Long id = contractDaoService.saveContractAdjustment(contractAdjustment, true);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_new_contract_adjustment", new Object[]{id}, LocaleContextHolder.getLocale()));
        } catch (ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_new_contract_adjustment", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch (Exception e) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_new_contract_adjustment", null, LocaleContextHolder.getLocale()));
        }
    }

    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(method = RequestMethod.PUT, consumes = APPLICATION_JSON_VALUE, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse update(@RequestBody ContractAdjustment contractAdjustment) {
        try {
            contractDaoService.updateContractAdjustment(contractAdjustment, true);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_adjustment_contract_adjustment", new Object[]{contractAdjustment.getId()}, LocaleContextHolder.getLocale()));
        } catch (ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_adjustment_contract_adjustment", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch (Exception e) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_adjustment_contract_adjustment", null, LocaleContextHolder.getLocale()));
        }
    }

    @ResponseBody
    @ResponseStatus(CREATED)
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse delete(@PathVariable("id") Long id) {
        try {
            contractDaoService.deleteContractAdjustment(id, true);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_delete_contract_adjustment", new Object[]{id}, LocaleContextHolder.getLocale()));
        } catch (ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_delete_contract_adjustment", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch (Exception e) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_delete_contract_adjustment", null, LocaleContextHolder.getLocale()));
        }
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String showContractAdjustment(Model uiModel, @PathVariable("id") Long id) throws ServiceException {

        uiModel.addAttribute("contractAdjustment", contractDaoService.contractAdjustment(id));
        return "contract_adjustments/show";
    }

    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(method = RequestMethod.GET, params = "rollup", produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public List<ContractAdjustment> contractAdjustments(@RequestParam(value = "cid", required = true) Long contractId) {
        return contractRevenueService.contractAdjustments(contractId);
    }

    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(params = "batch", method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse batch(@RequestBody ContractAdjustment[] contractAdjustments) {
        
        List<BatchResult> batchResults = contractDaoService.batchContractAdjustments(contractAdjustments);
        APIResponse.Status outcome = APIResponse.Status.OK;
        for (BatchResult batchResult : batchResults) {
            if (BatchResult.Result.failed.equals(batchResult.getResult())) {
                outcome = APIResponse.Status.ERROR;
                break;
            }
        }
        return new APIResponse(outcome, messageSource.getMessage("api_ok_batch_response", null, LocaleContextHolder.getLocale()), batchResults);
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/{month}/{year}", params = "rollup", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public List<ContractAdjustment> monthlyAdjustmentRollup(@PathVariable("month") Integer month, @PathVariable("year") String year,
            @RequestParam(value = "cid", required = true) Long contractId, @RequestParam(value = "cgid", required = false) Long contractGroupId, @RequestParam(value = "sts", required = true) Service.Status status) {
        return contractRevenueService.contractAdjustmentRollupForMonthOf(contractId, contractGroupId, month, year, status);
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public List<ContractAdjustment> recordsForContractAdjustment(@RequestParam(value = "cid", required = true) Long contractId,
            @RequestParam(value = "type", required = true) String adjustmentType,
            @RequestParam(value = "sd", required = true) @DateTimeFormat(pattern = "MMddyyyy") Date startDate,
            @RequestParam(value = "ed", required = true) @DateTimeFormat(pattern = "MMddyyyy") Date endDate, @RequestParam(value = "sts", required = true) Service.Status status) {
        return contractRevenueService.contractAdjustmentRecordsForFilter(contractId, adjustmentType, startDate, endDate, status);
    }

    @ResponseBody
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    @ExceptionHandler(ServiceException.class)
    public APIResponse handleGeneralServiceException(ServiceException se, HttpServletRequest httpServletRequest) {
        APIResponse errorResponse = new APIResponse(APIResponse.Status.ERROR, se.getMessage());
        errorResponse.newMeta().addException(se);
        log.info(errorResponse.toString());
        return errorResponse;
    }
    
    @ResponseBody
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    @ExceptionHandler(UnexpectedRollbackException.class)
    public APIResponse handleUnexpectedRollbackException(UnexpectedRollbackException ure, HttpServletRequest httpServletRequest) {
        APIResponse errorResponse = new APIResponse(APIResponse.Status.ERROR, ure.getMessage());
        errorResponse.newMeta().addException(ure);
        log.info(errorResponse.toString());
        return errorResponse;
    }
}
