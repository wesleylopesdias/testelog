package com.logicalis.serviceinsight.web.controller;

import com.logicalis.pcc.PCClient;
import com.logicalis.pcc.PCPath;
import com.logicalis.serviceinsight.dao.ExpenseCategory;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

import com.logicalis.serviceinsight.dao.SPLACost;
import com.logicalis.serviceinsight.data.Device;
import com.logicalis.serviceinsight.data.DeviceProperty;
import com.logicalis.serviceinsight.data.MicrosoftPriceList;
import com.logicalis.serviceinsight.data.User;
import com.logicalis.serviceinsight.service.ApplicationDataDaoService;
import com.logicalis.serviceinsight.service.ContractDaoService;
import com.logicalis.serviceinsight.service.MicrosoftPricingService;
import com.logicalis.serviceinsight.service.ServiceException;
import com.logicalis.serviceinsight.service.UserDaoService;
import com.logicalis.serviceinsight.web.config.CloudBillingSecurityConfig.Role;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;

/**
 * Manage CRUD operations for the Users of the system
 * 
 * @author jsanchez
 *
 */
@Controller
@RequestMapping("/settings")
public class SettingsController extends BaseController {

    @Autowired
    UserDaoService userDaoService;
    @Autowired
    ApplicationDataDaoService applicationDataDaoService;
    @Autowired
    ContractDaoService contractDaoService;
    @Autowired
    MicrosoftPricingService microsoftPricingService;

    /**
     * 
     * @param uiModel
     * @return
     */
    @RequestMapping(value = "/users", method = RequestMethod.GET)
    public String show(Model uiModel) {
        uiModel.addAttribute("roles", Arrays.asList(Role.values()));
        uiModel.addAttribute("currentUser",authenticatedUser());
        return "settings/userslisting";
    }
    
    @RequestMapping(value = "/m365", method = RequestMethod.GET)
    public String m365(Model uiModel, @RequestParam(value = "message", required = false) String message) {
        if (message != null) {
            uiModel.addAttribute("reported_message", message);
        }
        return "settings/m365";
    }
    
    /**
     * Retrieve user for given user name
     * 
     * @param username
     * @return  user or user not found exception
     * @throws ServiceException
     */
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/users/{id}", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public User user(@PathVariable("id") Long id) throws ServiceException {
        return userDaoService.user(id);
    }
    
    /** 
     * Retrieve the list of users that are "enabled" or "not enabled"
     * 
     * @param enabled
     * @return  List of users
     */
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value="/users", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public List<User> users(@RequestParam(value = "enabled", required = true) Boolean enabled) {
        return userDaoService.users(enabled);
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/users", method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse create(@RequestBody User user) {
        try {
            userDaoService.saveUser(user);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_new_user", new Object[]{user.getUsername()}, LocaleContextHolder.getLocale()));
        } catch(ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_new_user", new Object[]{user.getUsername(), se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch (Exception e) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_new_user", new Object[]{user.getUsername(), e.getMessage()}, LocaleContextHolder.getLocale()));
        }
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/users", method = RequestMethod.PUT, consumes = APPLICATION_JSON_VALUE, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse update(@RequestBody User user) {
        try {
            userDaoService.updateUser(user);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_update_user", new Object[]{user.getUsername()}, LocaleContextHolder.getLocale()));
        } catch(ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_update_user", new Object[]{user.getUsername(), se.getMessage()}, LocaleContextHolder.getLocale()));            
        } catch(Exception e) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_update_user", new Object[]{user.getUsername(), e.getMessage()}, LocaleContextHolder.getLocale()));
        }
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/users/{id}", method = RequestMethod.DELETE, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse delete(@PathVariable("id") Long id) {
        try {
            userDaoService.deleteUser(id);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_delete_user", new Object[]{id}, LocaleContextHolder.getLocale()));
        } catch(ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_delete_user", new Object[]{id, se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch(Exception e) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_delete_user", new Object[]{id, e.getMessage()}, LocaleContextHolder.getLocale()));
        }
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/users/disable/{id}", method = RequestMethod.PUT, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse disable(@PathVariable("id") Long id) {
        try {
            userDaoService.disableUser(id);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_disable_user", new Object[]{id}, LocaleContextHolder.getLocale()));
        } catch(ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_disable_user", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch(Exception e) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_disable_user", new Object[]{e.getMessage()}, LocaleContextHolder.getLocale()));
        }
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/users/enable/{id}", method = RequestMethod.PUT, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse enable(@PathVariable("id") Long id) {
        try {
            userDaoService.enableUser(id);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_enable_user", new Object[]{id}, LocaleContextHolder.getLocale()));
        } catch(ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_enable_user", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch(Exception e) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_enable_user", new Object[]{e.getMessage()}, LocaleContextHolder.getLocale()));
        }
    }
    
    @RequestMapping(value = "/devices", method = RequestMethod.GET)
    public String showDevices(Model uiModel) {
    	uiModel.addAttribute("services", contractDaoService.services());
    	uiModel.addAttribute("deviceTypes", Arrays.asList(Device.DeviceType.values()));
    	uiModel.addAttribute("splas", applicationDataDaoService.splaCosts(Boolean.TRUE, Boolean.FALSE));
        uiModel.addAttribute("deviceSelect", devices(Boolean.FALSE));
        uiModel.addAttribute("deviceRelationships", Arrays.asList(Device.Relationship.values()));
        uiModel.addAttribute("costCategorySelect", applicationDataDaoService.findExpenseCategories());
        uiModel.addAttribute("devicePropertyTypes", Arrays.asList(DeviceProperty.Type.values()));
        return "settings/devices";
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value="/devices", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public List<Device> devices(@RequestParam(value = "archived", required = false) Boolean archived) {
        return applicationDataDaoService.devices(archived);
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value="/devices/{id}", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse device(@PathVariable("id") Long deviceId) {
    	try {
    		Device device =  applicationDataDaoService.device(deviceId);
    		return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_get_device", new Object[]{deviceId}, LocaleContextHolder.getLocale()));
    	} catch(ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_get_device", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch(Exception e) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_get_device", new Object[]{e.getMessage()}, LocaleContextHolder.getLocale()));
        }
    }
    
    @ResponseBody
    @ResponseStatus(CREATED)
    @RequestMapping(value="/devices", method = RequestMethod.POST, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse createDevice(@RequestBody Device device) {
    	try {
            Long id = applicationDataDaoService.saveDevice(device);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_new_device", new Object[]{id}, LocaleContextHolder.getLocale()));
        } catch (ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_new_device", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch (Exception e) {
            log.error("error saving a Device", e);
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_new_device", null, LocaleContextHolder.getLocale()));
        }
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value="/devices", method = RequestMethod.PUT, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse updateDevice(@RequestBody Device device) {
    	try {
            applicationDataDaoService.updateDevice(device);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_update_device", new Object[]{device.getId()}, LocaleContextHolder.getLocale()));
        } catch (ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_update_device", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch (Exception e) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_update_device", null, LocaleContextHolder.getLocale()));
        }
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value="/devices/{id}", method = RequestMethod.DELETE, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse deleteDevice(@PathVariable("id") Long deviceId) {
    	try {
            applicationDataDaoService.deleteDevice(deviceId);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_delete_device", new Object[]{deviceId}, LocaleContextHolder.getLocale()));
        } catch (ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_delete_device", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch (Exception e) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_delete_device", null, LocaleContextHolder.getLocale()));
        }
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value="/devices/merge", method = RequestMethod.POST, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse mergeDevice(@RequestParam(value = "odid", required = true) Long oldDeviceId, @RequestParam(value = "ndid", required = true) Long newDeviceId) {
    	try {
            applicationDataDaoService.mergeDevice(oldDeviceId, newDeviceId);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_update_device", new Object[]{newDeviceId}, LocaleContextHolder.getLocale()));
        } catch (ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_update_device", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch (Exception e) {
        	e.printStackTrace();
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_update_device", null, LocaleContextHolder.getLocale()));
        }
    }
    
    @RequestMapping(value = "/spla", method = RequestMethod.GET)
    public String showSPLA(Model uiModel) {
        uiModel.addAttribute("currentUser",authenticatedUser());
        uiModel.addAttribute("vendors", Arrays.asList(SPLACost.Vendor.values()));
        uiModel.addAttribute("types", Arrays.asList(SPLACost.Type.values()));
    	List<ExpenseCategory> categories = contractDaoService.expenseCategories();
    	Collections.sort(categories, ExpenseCategory.ExpenseCategoryNameComparator);
        uiModel.addAttribute("expenseCategories", categories);
        return "settings/spla";
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value="/spla", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public List<SPLACost> splaCosts(@RequestParam(value = "active", required = false) Boolean active) {
        return applicationDataDaoService.splaCosts(active, Boolean.FALSE);
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value="/spla/{id}", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse splaCost(@PathVariable("id") Long splaId) {
    	try {
    		SPLACost splaCost =  applicationDataDaoService.splaCost(splaId);
    		return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_get_device", new Object[]{splaId}, LocaleContextHolder.getLocale()));
    	} catch(ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_get_device", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch(Exception e) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_get_device", new Object[]{e.getMessage()}, LocaleContextHolder.getLocale()));
        }
    }
    
    @ResponseBody
    @ResponseStatus(CREATED)
    @RequestMapping(value="/spla", method = RequestMethod.POST, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse createSPLACost(@RequestBody SPLACost splaCost) {
    	try {
            Long id = applicationDataDaoService.saveSPLACost(splaCost);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_new_device", new Object[]{id}, LocaleContextHolder.getLocale()));
        } catch (ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_new_device", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch (Exception e) {
            log.error("error saving a Device", e);
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_new_device", null, LocaleContextHolder.getLocale()));
        }
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value="/spla", method = RequestMethod.PUT, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse updateSPLACost(@RequestBody SPLACost splaCost) {
    	try {
            applicationDataDaoService.updateSPLACost(splaCost);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_update_device", new Object[]{splaCost.getId()}, LocaleContextHolder.getLocale()));
        } catch (ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_update_device", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch (Exception e) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_update_device", null, LocaleContextHolder.getLocale()));
        }
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value="/spla/{id}", method = RequestMethod.DELETE, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse deleteSPLACost(@PathVariable("id") Long splaId) {
    	try {
            applicationDataDaoService.deleteSPLACost(splaId);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_delete_device", new Object[]{splaId}, LocaleContextHolder.getLocale()));
        } catch (ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_delete_device", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch (Exception e) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_delete_device", null, LocaleContextHolder.getLocale()));
        }
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value="/microsoftpricing", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public List<MicrosoftPriceList> microsoftPricing() {
        return microsoftPricingService.getMicrosoftPriceLists();
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value="/microsoftpricing/import", method = RequestMethod.POST)
    public String importM365Products(@RequestParam("templatefile") MultipartFile file, @RequestParam(value = "date", required = false) @DateTimeFormat(pattern = "MM/yyyy") Date date, 
    		@RequestParam("type") MicrosoftPriceList.MicrosoftPriceListType type, Model uiModel) {
    	StringBuffer response = new StringBuffer();
        String status = "error";
        String message = "";
    	if (!file.isEmpty()) {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmm").format(new DateTime().toDate());
            try {
                InputStream is = file.getInputStream();
                File uploaded = new File("/tmp/" + File.separator + "m365Upload_"+timestamp+"_"+file.getOriginalFilename());
                FileOutputStream fos = new FileOutputStream(uploaded);
                while (is.available() > 0) {
                    fos.write(is.read());
                }
                is.close();
                fos.close();
                log.debug("uploaded: " + uploaded.getAbsolutePath());
                
                microsoftPricingService.importMicrosoftPriceList(uploaded, date, type);
                
                status = "success";
                //message = messageSource.getMessage("ui_ok_file_upload", null, LocaleContextHolder.getLocale());
                message = "File successfully uploaded!";
            } catch (ServiceException ex) {
                log.debug("Showing exception from Expense Import", ex);
                message = ex.getMessage();
            } catch (Exception ex) {
                log.debug("Showing exception from Expense Import", ex);
                message = ex.getMessage();
            }
        } else {
            message = messageSource.getMessage("import_error_empty_file", null, LocaleContextHolder.getLocale());
        }
    	//we have to do a custom response here, since we are posting back to an iframe and parse from there
    	response.append("<span id=\"iframe-status\">").append(status).append("</span>").append("<span id=\"iframe-message\">").append(message).append("</span>");
    	return response.toString();
    }
    
    /**
     * Requires MFA
     * PCPath.pricing().getAppUser() == TRUE
     * 
     * @param model
     * @param httpServletRequest
     * @return 
     */
    @RequestMapping(value="/microsoftncepricing/import", method = RequestMethod.POST)
    public String pricesheet(Model model, HttpServletRequest httpServletRequest) {
        PCClient client = (PCClient) httpServletRequest.getSession().getAttribute("com.logicalis.pcc.PCClient");
        if (client == null) {
            return "redirect:" + this.mfa("/settings/authredirect", httpServletRequest) + "&state=pricesheet";
        }
        return providePricesheet(httpServletRequest, model, client);
    }
    
    /**
     * PCPath.pricing()
     * 
     * @param model must not be null. should be pulled from the session or created
     * @param client
     * @return 
     */
    private String providePricesheet(HttpServletRequest httpServletRequest, Model uiModel, PCClient client) {
        if (client == null) {
            throw new IllegalArgumentException("the client must be provided by the calling method");
        }
        client.setRedirectURI(azureRedirectHost + "/settings/authredirect"); // required by endpoint app services
        PCPath wrapperPath = PCPath.pricing("US", "updatedlicensebased");
        File pricesheet = client.getFile(wrapperPath, null); // returns ZIP
        
        log.debug("Fetched {} file from PC API", pricesheet.getAbsolutePath());
        String message = messageSource.getMessage("pccapi_ok_imported_products", null, LocaleContextHolder.getLocale());
        try {
            microsoftPricingService.importMicrosoftPriceList(pricesheet, null, MicrosoftPriceList.MicrosoftPriceListType.M365NC);
        } catch (Exception any) {
            log.error("caught exception processing M365NC Products", any);
            message = messageSource.getMessage("pccapi_error_imported_products", new Object[]{any.getMessage()}, LocaleContextHolder.getLocale());
        }
        return "redirect:/settings/m365?message=" + encodeQueryParameter(message, httpServletRequest);
    }
    
    /**
     * Called by Microsoft, providing the authorization code after MFA login
     * 
     * @param model
     * @param httpServletRequest
     * @return 
     */
    @RequestMapping(value = "/authredirect", method = RequestMethod.POST)
    public String authredirect(Model model, HttpServletRequest httpServletRequest) throws ServiceException {
        
        String authorizationCode = httpServletRequest.getParameter("code"); // authorization code for your web app to accept from the Azure AD login call
        String state = httpServletRequest.getParameter("state");
        log.debug("received authredirect params: authorization_code [{}], state [{}]", new Object[]{authorizationCode, state});
        
        PCClient client = new PCClient(this.partnerCenterDomain, this.partnerTenantId,
                this.ccaaWebAppId, this.ccaaWebAppSecret, this.pcWebAppId, this.pcWebAppSecret, authorizationCode);
        httpServletRequest.getSession().setAttribute("com.logicalis.pcc.PCClient", client);
        
        switch(state) {
            case "pricesheet":
                return providePricesheet(httpServletRequest, model, client);
            default:
                throw new ServiceException("unexpected authredirect STATE found: " + state);
        }
    }
}
