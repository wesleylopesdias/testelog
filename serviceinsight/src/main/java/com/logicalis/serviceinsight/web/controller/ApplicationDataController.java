package com.logicalis.serviceinsight.web.controller;

import com.logicalis.serviceinsight.dao.Location;
import com.logicalis.serviceinsight.data.Device;
import com.logicalis.serviceinsight.data.Personnel;
import com.logicalis.serviceinsight.data.Service;
import com.logicalis.serviceinsight.data.StandardCost;
import com.logicalis.serviceinsight.service.ApplicationDataDaoService;
import com.logicalis.serviceinsight.service.ContractDaoService;
import com.logicalis.serviceinsight.service.ContractRevenueService;
import com.logicalis.serviceinsight.service.CostService;
import com.logicalis.serviceinsight.service.RevenueService;
import com.logicalis.serviceinsight.service.ServiceException;
import com.logicalis.serviceinsight.service.ServiceUsageService;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.convert.ConversionService;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author poneil
 */
@RestController
@RequestMapping("/data")
public class ApplicationDataController {

    private final Logger log = LoggerFactory.getLogger(ApplicationDataController.class);
    @Autowired
    CostService costService;
    @Autowired
    ConversionService conversionService;
    @Autowired
    RevenueService revenueService;
    @Autowired
    ContractRevenueService contractRevenueService;
    @Autowired
    ServiceUsageService serviceUsageService;
    @Autowired
    ContractDaoService contractDaoService;
    @Autowired
    ApplicationDataDaoService applicationDataDaoService;
    @Autowired
    MessageSource messageSource;

    @RequestMapping(value = "/standardCosts", method = RequestMethod.GET)
    public List<StandardCost> standardCosts() {
        return costService.getStandardCosts();
    }

    @RequestMapping(value = "/services", method = RequestMethod.GET)
    public Map<String, Long> serviceList() {
        return contractDaoService.servicesMap();
    }
    
    @RequestMapping(value = "/services/list", method = RequestMethod.GET)
    public List<Service> serviceObjectList() {
        return contractDaoService.services();
    }

    @RequestMapping(value = "/services/update", method = RequestMethod.GET)
    public void updateServices() {
        serviceUsageService.serviceConnect();
    }
    
    /*
     * FYI -- ADDITIONAL CRUD METHODS FOR MANAGING DEVICES HAVE BEEN MOVED TO THE SettingsController -- This one left here, so it is accessible by ROLE_USER
     */
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value="/devices", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public List<Device> devices(@RequestParam(value = "archived", required = false) Boolean archived) {
        return applicationDataDaoService.minimumDevices(archived);
    }

    @ResponseStatus(OK)
    @RequestMapping(value = "/locations", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public List<Location> locations(@RequestParam(value = "revenue", required = false) Boolean isDisplayedRevenue) {
        return applicationDataDaoService.locations(isDisplayedRevenue);
    }

    @ResponseStatus(OK)
    @RequestMapping(value = "/devices/search/partno", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public List<Device> searchPartNo(@RequestParam(value = "s", required = true) String search) {
        log.debug("Searching for: [{}]", search);
        return applicationDataDaoService.searchDevicesByPartNumber(search);
    }

    @ResponseStatus(OK)
    @RequestMapping(value = "/devices/search/description", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public List<Device> searchDescr(@RequestParam(value = "s", required = true) String search) {
        log.debug("Searching for: [{}]", search);
        return applicationDataDaoService.searchDevicesByDescription(search);
    }
    
    @ResponseStatus(OK)
    @RequestMapping(value = "/personnel", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public List<Personnel> personnel(@RequestParam(value = "a", required = true) Boolean active) {
        return applicationDataDaoService.personnel(active);
    }
    
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    @ExceptionHandler(ServiceException.class)
    public APIResponse handleGeneralServiceException(ServiceException se, HttpServletRequest httpServletRequest) {
        APIResponse errorResponse = new APIResponse(APIResponse.Status.ERROR, se.getMessage());
        errorResponse.newMeta().addException(se);
        log.info(errorResponse.toString());
        return errorResponse;
    }
    
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    @ExceptionHandler(UnexpectedRollbackException.class)
    public APIResponse handleUnexpectedRollbackException(UnexpectedRollbackException ure, HttpServletRequest httpServletRequest) {
        APIResponse errorResponse = new APIResponse(APIResponse.Status.ERROR, ure.getMessage());
        errorResponse.newMeta().addException(ure);
        log.info(errorResponse.toString());
        return errorResponse;
    }
}
