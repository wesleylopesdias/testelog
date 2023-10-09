package com.logicalis.serviceinsight.web.controller;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.List;

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

import com.logicalis.serviceinsight.data.Contract;
import com.logicalis.serviceinsight.data.QuoteMin;
import com.logicalis.serviceinsight.service.PricingIntegrationService;
import com.logicalis.serviceinsight.service.ServiceException;

@Controller
@RequestMapping("/pricing")
public class PricingController extends BaseController {

	@Autowired
	PricingIntegrationService pricingIntegrationService;
	
	@ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/quotes/{id}", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public QuoteMin quote(@PathVariable("id") Long id) throws ServiceException {
        return pricingIntegrationService.quote(id);
    }
	
	@ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/quotes/import/{id}", method = RequestMethod.POST, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse importQuote(@PathVariable("id") Long id, @RequestBody Contract contract) throws ServiceException {
		try {
            //Long id = contractDaoService.saveContract(contract);
			pricingIntegrationService.importQuote(id, contract);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_import_quote", new Object[]{id}, LocaleContextHolder.getLocale()));
        } catch(ServiceException se) {
        	se.printStackTrace();
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_import_quote", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch(Exception e) {
        	e.printStackTrace();
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_import_quote", null, LocaleContextHolder.getLocale()));
        }
    }
	
	@ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/quotes/won/{id}", method = RequestMethod.PUT, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse markQuoteAsWon(@PathVariable("id") Long id) throws ServiceException {
		try {
			pricingIntegrationService.markQuoteAsWon(id);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_update_pricing_status", new Object[]{id}, LocaleContextHolder.getLocale()));
        } catch(ServiceException se) {
        	se.printStackTrace();
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_update_pricing_status", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch(Exception e) {
        	e.printStackTrace();
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_update_pricing_status", null, LocaleContextHolder.getLocale()));
        }
    }
	
	@ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/quotes/list", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public List<QuoteMin> quotes(@RequestParam(value = "cid", required = true) Long customerId) throws ServiceException {
        return pricingIntegrationService.findQuotesForCustomer(customerId);
    }
	
}
