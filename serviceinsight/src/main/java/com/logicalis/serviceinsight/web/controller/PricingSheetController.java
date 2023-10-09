package com.logicalis.serviceinsight.web.controller;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.logicalis.serviceinsight.data.Device;
import com.logicalis.serviceinsight.data.MicrosoftPriceList;
import com.logicalis.serviceinsight.data.PricingSheet;
import com.logicalis.serviceinsight.data.PricingSheetProduct;
import com.logicalis.serviceinsight.data.Service;
import com.logicalis.serviceinsight.service.ApplicationDataDaoService;
import com.logicalis.serviceinsight.service.MicrosoftPricingService;
import com.logicalis.serviceinsight.service.PricingSheetService;
import com.logicalis.serviceinsight.service.ServiceException;

@Controller
@RequestMapping("/pricingsheets")
public class PricingSheetController extends BaseController {

	@Autowired
	PricingSheetService pricingSheetService;
	@Autowired
	ApplicationDataDaoService applicationDataDaoService;
	@Autowired
	MicrosoftPricingService microsoftPricingService;
	
	@ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public List<PricingSheet> pricingSheets(@PathVariable("id") Long id) throws ServiceException {
        return new ArrayList<PricingSheet>();
    }
	
	@ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public PricingSheet pricingSheet(@PathVariable("id") Long id) throws ServiceException {
		return pricingSheetService.pricingSheet(id);
    }
	
	@ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/contract/{id}", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public PricingSheet pricingSheetForContract(@PathVariable("id") Long contractId) throws ServiceException {
		return pricingSheetService.findPricingSheetForContract(contractId);
    }
	
	@ResponseBody
    @ResponseStatus(CREATED)
    @RequestMapping(method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse savePricingSheet(@RequestBody PricingSheet pricingSheet) {
        try {
            Long id = pricingSheetService.savePricingSheet(pricingSheet);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_new_pricing_sheet", new Object[]{id}, LocaleContextHolder.getLocale()));
        } catch(ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_new_pricing_sheet", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch(Exception e) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_new_pricing_sheet", null, LocaleContextHolder.getLocale()));
        }
    }
	
	@ResponseBody
	@ResponseStatus(OK)
    @RequestMapping(method = RequestMethod.PUT, consumes = APPLICATION_JSON_VALUE, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse updatePricingSheet(@RequestBody PricingSheet pricingSheet) {
        try {
        	pricingSheetService.updatePricingSheet(pricingSheet);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_update_pricing_sheet", new Object[]{pricingSheet.getId()}, LocaleContextHolder.getLocale()));
        } catch(ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_update_pricing_sheet", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch(Exception e) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_update_pricing_sheet", null, LocaleContextHolder.getLocale()));
        }
    }
	
	@ResponseBody
	@ResponseStatus(OK)
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse deletePricingSheet(@PathVariable("id") Long id) {
        try {
        	pricingSheetService.deletePricingSheet(id);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_delete_pricing_sheet", new Object[]{id}, LocaleContextHolder.getLocale()));
        } catch(ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_delete_pricing_sheet", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch(Exception e) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_delete_pricing_sheet", null, LocaleContextHolder.getLocale()));
        }
    }
	
	
	// -- PRODUCTS
	
	@ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/products/{id}", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public PricingSheet pricingSheetProduct(@PathVariable("id") Long id) throws ServiceException {
		return pricingSheetService.pricingSheet(id);
    }
	
	@ResponseBody
    @ResponseStatus(CREATED)
    @RequestMapping(value = "/products", method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse savePricingSheetProduct(@RequestBody PricingSheetProduct pricingSheetProduct) {
        try {
            Long id = pricingSheetService.savePricingSheetProduct(pricingSheetProduct);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_new_pricing_sheet_product", new Object[]{id}, LocaleContextHolder.getLocale()));
        } catch(ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_new_pricing_sheet_product", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch(Exception e) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_new_pricing_sheet_product", null, LocaleContextHolder.getLocale()));
        }
    }
	
	@ResponseBody
	@ResponseStatus(OK)
    @RequestMapping(value = "/products", method = RequestMethod.PUT, consumes = APPLICATION_JSON_VALUE, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse updatePricingSheetProduct(@RequestBody PricingSheetProduct pricingSheetProduct) {
        try {
        	pricingSheetService.updatePricingSheetProduct(pricingSheetProduct);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_update_pricing_sheet_product", new Object[]{pricingSheetProduct.getId()}, LocaleContextHolder.getLocale()));
        } catch(ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_update_pricing_sheet_product", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch(Exception e) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_update_pricing_sheet_product", null, LocaleContextHolder.getLocale()));
        }
    }
	
	@ResponseBody
	@ResponseStatus(OK)
    @RequestMapping(value = "/products/{id}", method = RequestMethod.DELETE, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse deletePricingSheetProduct(@PathVariable("id") Long id) {
        try {
        	pricingSheetService.deletePricingSheetProduct(id);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_delete_pricing_sheet_product", new Object[]{id}, LocaleContextHolder.getLocale()));
        } catch(ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_delete_pricing_sheet_product", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch(Exception e) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_delete_pricing_sheet_product", null, LocaleContextHolder.getLocale()));
        }
    }
	
	@ResponseBody
    @ResponseStatus(CREATED)
    @RequestMapping(value = "/products/generate/{id}", method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse savePricingSheetProduct(@PathVariable("id") Long contractId) {
        try {
            pricingSheetService.generatePricingSheetForContract(contractId);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_new_pricing_sheet_product", new Object[]{contractId}, LocaleContextHolder.getLocale()));
        } catch(ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_new_pricing_sheet_product", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch(Exception e) {
        	e.printStackTrace();
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_new_pricing_sheet_product", null, LocaleContextHolder.getLocale()));
        }
    }
	
	@RequestMapping(value = "/products/generate/all", method = RequestMethod.GET)
    public String syncAllPricingSheets() {
    	pricingSheetService.generatePricingSheetsForAllCustomers();
        return "redirect:/ok";
    }
	
	@ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/devices/{id}", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public List<Device> pricingSheetDevices(@PathVariable("id") Long contractId) throws ServiceException {
		return applicationDataDaoService.findDevicesForContract(contractId);
    }
	
	@ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/microsoft/latest", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public MicrosoftPriceList getMicrosoftPriceListForMonth(@RequestParam("type") MicrosoftPriceList.MicrosoftPriceListType type) throws ServiceException {
		return microsoftPricingService.getLatestMicrosoftPriceList(type);
    }
	
}
