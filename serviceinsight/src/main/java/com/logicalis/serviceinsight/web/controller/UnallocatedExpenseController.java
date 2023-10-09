package com.logicalis.serviceinsight.web.controller;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.logicalis.serviceinsight.dao.CostItem;
import com.logicalis.serviceinsight.data.UnallocatedExpense;
import com.logicalis.serviceinsight.service.ContractDaoService;
import com.logicalis.serviceinsight.service.CostDaoService;
import com.logicalis.serviceinsight.service.ServiceException;

@Controller
@RequestMapping("/unallocatedexpense")
public class UnallocatedExpenseController extends BaseController {

	
	@Autowired
	CostDaoService costDaoService;
	
	@ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public UnallocatedExpense unallocatedExpense(@PathVariable("id") Long id) throws ServiceException {
        return costDaoService.unallocatedExpense(id);
    }

    @ResponseBody
    @ResponseStatus(CREATED)
    @RequestMapping(method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse saveUnallocatedExpense(@RequestBody UnallocatedExpense unallocatedExpense) {
        try {
            Long id = costDaoService.saveUnallocatedExpense(unallocatedExpense);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_new_unallocated_expense", new Object[]{id}, LocaleContextHolder.getLocale()));
        } catch (ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_new_unallocated_expense", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch (Exception e) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_unallocated_expense", null, LocaleContextHolder.getLocale()));
        }
    }

    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(method = RequestMethod.PUT, consumes = APPLICATION_JSON_VALUE, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse updateUnallocatedExpense(@RequestBody UnallocatedExpense unallocatedExpense) {
        try {
        	costDaoService.updateUnallocatedExpense(unallocatedExpense);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_update_unallocated_expense", new Object[]{unallocatedExpense.getId()}, LocaleContextHolder.getLocale()));
        } catch (ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_update_unallocated_expense", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch (Exception e) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_unallocated_expense", null, LocaleContextHolder.getLocale()));
        }
    }

    @ResponseBody
    @ResponseStatus(CREATED)
    @Secured({"ROLE_ADMIN"})
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse deleteCostItem(@PathVariable("id") Long id) {
        try {
            costDaoService.deleteUnallocatedExpense(id);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_delete_unallocated_expense", new Object[]{id}, LocaleContextHolder.getLocale()));
        } catch (ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_delete_unallocated_expense", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch (Exception e) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_unallocated_expense", null, LocaleContextHolder.getLocale()));
        }
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(params = "filter", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public List<UnallocatedExpense> unallocatedExpenses(
            @RequestParam(value = "sd", required = false) @DateTimeFormat(pattern = "MMddyyyy") Date startDate,
            @RequestParam(value = "ed", required = false) @DateTimeFormat(pattern = "MMddyyyy") Date endDate) throws ServiceException {
    	if(startDate != null && endDate != null) {
    		return costDaoService.unallocatedExpensesForPeriod(startDate, endDate);
    	} else {
    		return costDaoService.unallocatedExpenses();
    	}
        
    }
	
}
