package com.logicalis.serviceinsight.web.controller;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.logicalis.serviceinsight.data.Contract;
import com.logicalis.serviceinsight.data.ServiceNowCI;
import com.logicalis.serviceinsight.service.ServiceException;
import com.logicalis.serviceinsight.service.ServiceNowService;

@Controller
@RequestMapping("/servicenowcis")
public class ServiceNowCIController extends BaseController  {

	@Autowired
	ServiceNowService serviceNowService;
	
	@ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public List<ServiceNowCI> serviceNowCIs(@RequestParam(value = "cid", required = true) Long contractId) {
        return serviceNowService.serviceNowCIsForContract(contractId);
    }
	
	@ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(method = RequestMethod.GET, params="sync",  produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public List<ServiceNowCI> serviceNowCISync(@RequestParam(value = "cid", required = true) Long contractId, @RequestParam(value = "sid", required = true) String contractSysId) {
		/*try {
			serviceNowService.syncContractCIsForContractFromServiceNow(contractSysId, contractId);
			List<ServiceNowCI> cis = serviceNowService.serviceNowCIsForContract(contractId);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_delete_contract", cis, LocaleContextHolder.getLocale()));
        } catch(ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_delete_contract", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch(Exception e) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_delete_contract", null, LocaleContextHolder.getLocale()));
        }*/
		
		List<ServiceNowCI> cis = new ArrayList<ServiceNowCI>();
		try {
			serviceNowService.syncContractCIsForContractFromServiceNow(contractSysId, contractId);
			cis = serviceNowService.serviceNowCIsForContract(contractId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return cis;
    }
	
}
