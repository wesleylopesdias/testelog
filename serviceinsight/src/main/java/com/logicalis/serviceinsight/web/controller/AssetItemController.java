package com.logicalis.serviceinsight.web.controller;

import com.logicalis.serviceinsight.dao.AssetItem;
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
@RequestMapping("/assetitems")
public class AssetItemController extends BaseController {

    @Autowired
    ServiceUsageService serviceUsageService;
    @Autowired
    ContractDaoService contractDaoService;
    @Autowired
    ConversionService conversionService;

    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public AssetItem assetItem(@PathVariable("id") Long id) throws ServiceException {

        return contractDaoService.assetItem(id);
    }

    @ResponseBody
    @ResponseStatus(CREATED)
    @RequestMapping(method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse saveAssetItem(@RequestBody AssetItem assetItem) {
        try {
            Long id = contractDaoService.saveAssetItem(assetItem);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_new_assetitem", new Object[]{id}, LocaleContextHolder.getLocale()));
        } catch (ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_new_assetitem", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch (Exception e) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_new_assetitem", null, LocaleContextHolder.getLocale()));
        }
    }

    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(method = RequestMethod.PUT, consumes = APPLICATION_JSON_VALUE, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse updateAssetItem(@RequestBody AssetItem assetItem) {
        try {
            contractDaoService.updateAssetItem(assetItem, true);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_update_assetitem", new Object[]{assetItem.getId()}, LocaleContextHolder.getLocale()));
        } catch (ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_update_assetitem", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch (Exception e) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_update_assetitem", null, LocaleContextHolder.getLocale()));
        }
    }

    @ResponseBody
    @ResponseStatus(CREATED)
    @Secured({"ROLE_ADMIN"})
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse deleteAssetItem(@PathVariable("id") Long id) {
        try {
            contractDaoService.deleteAssetItem(id);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_delete_assetitem", new Object[]{id}, LocaleContextHolder.getLocale()));
        } catch (ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_delete_assetitem", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch (Exception e) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_delete_assetitem", null, LocaleContextHolder.getLocale()));
        }
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String showAssetItem(Model uiModel, @PathVariable("id") Long id) throws ServiceException {

        uiModel.addAttribute("assetItem", contractDaoService.assetItem(id));
        return "assetItems/show";
    }

    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public List<AssetItem> assetItems() {
        return contractDaoService.assetItems();
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(params = "cid", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public List<AssetItem> findAssetItemsForCustomer(@RequestParam(value = "cid", required = true) Long customerId) {
        return contractDaoService.findAssetItemsByCustomer(customerId);
    }

    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(params = "filter", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public List<AssetItem> findAssetItemsForCustomerAndPeriod(@RequestParam(value = "cid", required = false) Long customerId,
            @RequestParam(value = "sd", required = false) @DateTimeFormat(pattern = "MMddyyyy") Date startDate,
            @RequestParam(value = "ed", required = false) @DateTimeFormat(pattern = "MMddyyyy") Date endDate) {
    	if(startDate != null && endDate != null) {
    		return contractDaoService.findAssetItemsByCustomerIdAndPeriod(customerId, startDate, endDate);
    	} else {
    		return contractDaoService.assetItems();
    	}
        
    }

    @RequestMapping(method = RequestMethod.GET)
    public String assetItems(Model uiModel) {

        uiModel.addAttribute("assetItems", contractDaoService.assetItems());
        return "assetItems/index";
    }
}
