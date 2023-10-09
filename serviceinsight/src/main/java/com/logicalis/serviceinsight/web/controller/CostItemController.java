package com.logicalis.serviceinsight.web.controller;

import com.logicalis.serviceinsight.dao.CostItem;
import com.logicalis.serviceinsight.service.ContractDaoService;
import com.logicalis.serviceinsight.service.ServiceException;
import com.logicalis.serviceinsight.service.ServiceUsageService;
import java.util.Date;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.convert.ConversionService;
import org.springframework.format.annotation.DateTimeFormat;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import org.springframework.security.access.annotation.Secured;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.ui.Model;
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
@RequestMapping("/costitems")
public class CostItemController extends BaseController {

    @Autowired
    ServiceUsageService serviceUsageService;
    @Autowired
    ContractDaoService contractDaoService;
    @Autowired
    ConversionService conversionService;

    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public CostItem costItem(@PathVariable("id") Long id) throws ServiceException {

        return contractDaoService.costItem(id);
    }

    @ResponseBody
    @ResponseStatus(CREATED)
    @RequestMapping(method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse saveCostItem(@RequestBody CostItem costItem) {
        try {
            Long id = contractDaoService.saveCostItem(costItem);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_new_costitem", new Object[]{id}, LocaleContextHolder.getLocale()));
        } catch (ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_new_costitem", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch (Exception e) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_new_costitem", null, LocaleContextHolder.getLocale()));
        }
    }

    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(method = RequestMethod.PUT, consumes = APPLICATION_JSON_VALUE, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse updateCostItem(@RequestBody CostItem costItem) {
        try {
            contractDaoService.updateCostItem(costItem, true);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_update_costitem", new Object[]{costItem.getId()}, LocaleContextHolder.getLocale()));
        } catch (ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_update_costitem", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch (Exception e) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_update_costitem", null, LocaleContextHolder.getLocale()));
        }
    }

    @ResponseBody
    @ResponseStatus(CREATED)
    @Secured({"ROLE_ADMIN"})
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse deleteCostItem(@PathVariable("id") Long id) {
        try {
            contractDaoService.deleteCostItem(id);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_delete_costitem", new Object[]{id}, LocaleContextHolder.getLocale()));
        } catch (ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_delete_costitem", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch (Exception e) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_delete_costitem", null, LocaleContextHolder.getLocale()));
        }
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String showCostItem(Model uiModel, @PathVariable("id") Long id) throws ServiceException {

        uiModel.addAttribute("costItem", contractDaoService.costItem(id));
        return "costItems/show";
    }

    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public List<CostItem> costItems() {
        return contractDaoService.costItems();
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(params = "cid", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public List<CostItem> findCostItemsForCustomer(@RequestParam(value = "cid", required = true) Long customerId) {
        return contractDaoService.findCostItemsByCustomer(customerId);
    }

    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(params = "filter", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public List<CostItem> findCostItemsForCustomerAndPeriod(@RequestParam(value = "cid", required = false) Long customerId,
            @RequestParam(value = "sd", required = false) @DateTimeFormat(pattern = "MMddyyyy") Date startDate,
            @RequestParam(value = "ed", required = false) @DateTimeFormat(pattern = "MMddyyyy") Date endDate, 
            @RequestParam(value = "ctype", required = false) CostItem.CostType costType,
            @RequestParam(value = "cstype", required = false) CostItem.CostSubType costSubType) {
    	if(startDate != null && endDate != null) {
    		return contractDaoService.findCostItemsByCustomerIdAndTypeAndPeriod(costType, costSubType, customerId, startDate, endDate);
    	} else {
    		return contractDaoService.findCostItemsByType(costType, costSubType);
    	}
        
    }

    @RequestMapping(method = RequestMethod.GET)
    public String costItems(Model uiModel) {

        uiModel.addAttribute("costItems", contractDaoService.costItems());
        return "costItems/index";
    }
}
