package com.logicalis.serviceinsight.web.controller;

import com.logicalis.ap.APClient;
import com.logicalis.ap.APPath;
import com.logicalis.ap.data.Column;
import com.logicalis.ap.data.Properties;
import com.logicalis.ap.data.SubscriptionCostRequest;
import com.logicalis.ap.data.SubscriptionCostResponse;
import com.logicalis.ap.data.TimePeriod;
import com.logicalis.pcc.PCClient;
import com.logicalis.pcc.PCPath;
import static com.logicalis.pcc.PCPath.customerSubscriptions;
import static com.logicalis.pcc.PCPath.nextPath;
import com.logicalis.serviceinsight.data.Customer;
import com.logicalis.serviceinsight.service.ContractDaoService;
import com.logicalis.serviceinsight.service.ServiceException;
import com.logicalis.serviceinsight.service.ServiceNowService;
import com.logicalis.serviceinsight.service.ServiceUsageService;
import com.microsoft.partnercenter.api.schema.datamodel.CustomerWrapper;
import com.microsoft.partnercenter.api.schema.datamodel.KeyValuePair;
import com.microsoft.partnercenter.api.schema.datamodel.Link;
import com.microsoft.partnercenter.api.schema.datamodel.RelationshipRequest;
import com.microsoft.partnercenter.api.schema.datamodel.Subscription;
import com.microsoft.partnercenter.api.schema.datamodel.SubscriptionWrapper;
import java.math.BigDecimal;
import java.math.RoundingMode;


import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.convert.ConversionService;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

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
@RequestMapping("/customers")
public class CustomerController extends BaseController {

    @Autowired
    ServiceUsageService serviceUsageService;
    @Autowired
    ContractDaoService contractDaoService;
    @Autowired
    ConversionService conversionService;

    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public Customer customer(@PathVariable("id") Long id) throws ServiceException {

        return contractDaoService.customer(id);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String showCustomer(Model uiModel, @PathVariable("id") Long id) throws ServiceException {

        uiModel.addAttribute("customer", contractDaoService.customer(id));
        return "customers/show";
    }

    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public List<Customer> customers(@RequestParam(value = "a", required = false) Boolean archived, @RequestParam(value = "si", required = false) Boolean siEnabled) {
    	return contractDaoService.customers(archived, siEnabled, false);
    }

    @RequestMapping(method = RequestMethod.GET)
    public String customers(Model uiModel, HttpServletRequest httpServletRequest) {
        // this is the basic login link for MSFT
        String azureMfaLink = String.format(azureMFARedirect, new Object[]{ccaaWebAppId}) + azureMFARedirectParams;
        // the redirect_uri must match a configured Azure application redirect URI
        String redirectURIParam = "&redirect_uri=" + generalEncoding(azureRedirectHost + "/customers/authredirect", httpServletRequest);
        log.debug("azure redirect_uri: [{}]", redirectURIParam);
        uiModel.addAttribute("azureMfaLink", azureMfaLink + redirectURIParam);
        
        redirectURIParam = "&redirect_uri=" + generalEncoding(azureRedirectHost + "/customers/customersync", httpServletRequest);
        log.debug("azure redirect_uri: [{}]", redirectURIParam);
        uiModel.addAttribute("customerSyncLink", azureMfaLink + redirectURIParam);
        
        azureMfaLink = String.format(azureMFARedirect, new Object[]{devWebAppId}) + azureMFARedirectParams;
        // the redirect_uri must match a configured Azure application redirect URI
        redirectURIParam = "&redirect_uri=" + generalEncoding(azureRedirectHost + "/customers/relationshipRequest", httpServletRequest);
        log.debug("azure redirect_uri: [{}]", redirectURIParam);
        uiModel.addAttribute("azureRelationshipRequestLink", azureMfaLink + redirectURIParam);
        
        return "customers/index";
    }

    /* Not used from within this app, but Service Activation will be able to push customers here as long as they exist in OSM */
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse saveCustomer(@RequestBody Customer customer) {
        try {
            Long customerId = contractDaoService.saveCustomer(customer);
            List<Long> customers = new ArrayList<Long>();
            customers.add(customerId);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_new_customer", new Object[]{customerId}, LocaleContextHolder.getLocale()), customers, Long.class.getName());
        } catch (ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_new_customer", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch (Exception e) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_new_customer", null, LocaleContextHolder.getLocale()));
        }
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(method = RequestMethod.PUT, consumes = APPLICATION_JSON_VALUE, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse updateCustomerBits(@RequestBody Customer customer) {
        try {
            contractDaoService.updateCustomerBits(customer);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_update_customer", new Object[]{customer.getId()}, LocaleContextHolder.getLocale()));
        } catch (ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_update_customer", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch (Exception e) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_update_customer", null, LocaleContextHolder.getLocale()));
        }
    }
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/search", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public List<Customer> search(@RequestParam(value = "s", required = true) String search) {
        log.debug("Searching for: [{}]", search);
        return contractDaoService.searchCustomers(search);
    }
    
    /**
     * This method is a "callback" from Microsoft's login process. If authentication
     * was successful, the request contains an authorization token that is used to create
     * the PartnerCenter and Azure Portal REST client objects for additional API calls
     * THAT REQUIRE user authentication (App + User), such as the PCPath.customers() API call.
     * 
     * This redirect endpoint is defined by the Azure application used to initiate the Microsoft
     * user login (see the CustomerController.customers(Model uiModel) method above for how the
     * link is constructed for the Microsoft login that redirects to this endpoint.
     * 
     * Enjoy reading Microsoft's "Enable the Secure Application Model" if you want to know more
     * of the gory details... https://docs.microsoft.com/en-us/partner-center/develop/enable-secure-app-model
     * 
     * @param httpServletRequest
     * @return 
     */
    @ResponseStatus(OK)
    @RequestMapping(value = "/authredirect", method = RequestMethod.POST)
    public String authredirect(HttpServletRequest httpServletRequest) {
        String referer = httpServletRequest.getHeader("Referer");
        if (StringUtils.isNotBlank(referer)) {
            log.debug("authredirect called by: [{}]", referer);
        }
        String authorizationCode = httpServletRequest.getParameter("code"); // authorization code for your web app to accept from the Azure AD login call
        String token = httpServletRequest.getParameter("id_token");
        
        /**
         * would like to inspect the id_token, but not sure where the signing key comes from
        Claims claims = Jwts
                .parser()
                .setSigningKey("signingkey")
                .parseClaimsJws(token)
                .getBody();
        String audience = claims.get("aud", String.class);
        log.debug("audience: ["+audience+"]");
         */
        
        // we got a redirect, but what for? we don't really know what API call required MFA
        // for now, let's just run through the PC customers() method and see if it works.
        
        // make the "refresh" authentication call, using the authorization code
        PCClient client = new PCClient(partnerCenterDomain, partnerTenantId,
                this.ccaaWebAppId, this.ccaaWebAppSecret, this.pcWebAppId, this.pcWebAppSecret, authorizationCode);
        
        /**
         * this is weird... when a redirect_uri is specified for the authorization token,
         * it needs to be included, again, when the PCClient requests an access token
         * for the API call, which has nothing at all to do with redirects.
         * this does not seem to be documented anywhere
         */
        String redirectURI = azureRedirectHost + "/customers/authredirect";
        client.setRedirectURI(redirectURI);
        PCPath wrapperPath = PCPath.customers();
        CustomerWrapper customerWrapper = client.get(wrapperPath, CustomerWrapper.class, null);
        List<com.microsoft.partnercenter.api.schema.datamodel.Customer> customers = customerWrapper.getItems();
        while (customerWrapper.getLinks() != null && customerWrapper.getLinks().getNext() != null) {
            Link next = customerWrapper.getLinks().getNext();
            String continuationToken = null;
            for (KeyValuePair header : next.getHeaders()) {
                if ("MS-ContinuationToken".equals(header.getKey())) {
                    continuationToken = header.getValue();
                }
            }
            wrapperPath = PCPath.nextPath(next.getUri(), continuationToken, wrapperPath);
            customerWrapper = client.get(wrapperPath, CustomerWrapper.class, null);
            customers.addAll(customerWrapper.getItems());
        }
        
        for (com.microsoft.partnercenter.api.schema.datamodel.Customer customer : customers) {
            log.info("Customer: ID [{}], Name [{}]", new Object[]{customer.getId(), customer.getCompanyProfile().getCompanyName()});
        }
        if (true) {
            return "redirect:/customers";
        }
        // note: skipping below "test" call with APClient
        for (com.microsoft.partnercenter.api.schema.datamodel.Customer customer : customers) {
            log.info("Customer: ID [{}], Name [{}]", new Object[]{customer.getId(), customer.getCompanyProfile().getCompanyName()});
            
            /**
             * since we're not really doing anything, here, this is just an example, let's just do some
             * other work, use the new Azure Portal client (APClient) to call the Azure Cost Mgmt API
             * for last month's invoiced costs for one customer's (JBT) subscriptions.
             */
            if ("91c22079-02ce-47e8-bb5b-c93a3d5f1a78".equalsIgnoreCase(customer.getId())) {
                wrapperPath = customerSubscriptions(customer.getId());
                SubscriptionWrapper subscriptionWrapper = client.get(wrapperPath, SubscriptionWrapper.class, null);
                List<Subscription> subscriptions = subscriptionWrapper.getItems();
                while (subscriptionWrapper.getLinks() != null && subscriptionWrapper.getLinks().getNext() != null) {
                    Link next = subscriptionWrapper.getLinks().getNext();
                    String continuationToken = null;
                    for (KeyValuePair header : next.getHeaders()) {
                        if ("MS-ContinuationToken".equals(header.getKey())) {
                            continuationToken = header.getValue();
                        }
                    }
                    wrapperPath = nextPath(next.getUri(), continuationToken, wrapperPath);
                    subscriptionWrapper = client.get(wrapperPath, SubscriptionWrapper.class, null);
                    subscriptions.addAll(subscriptionWrapper.getItems());
                }
                APClient appclient = null;
                BigDecimal totalCustomerCost = BigDecimal.ZERO;
                boolean foundAzurePlans = false;
                for (Subscription subscription : subscriptions) {
                    log.info("\tSubscription: ID [{}], Name [{}], Offer Name [{}]",
                            new Object[]{subscription.getId(), subscription.getFriendlyName(), subscription.getOfferName(), subscription});
                    if ("Microsoft Azure".equals(subscription.getOfferName())) {
                        foundAzurePlans = true;
                        log.info("\t\tfetching Azure Plan Costs");
                        if (appclient == null) {
                            appclient = new APClient(azureAPIVersion, customer.getId(), this.ccaaWebAppId, this.ccaaWebAppSecret);
                        }
                        SubscriptionCostRequest request = new SubscriptionCostRequest(new TimePeriod("2020-09-01T00:00:00+00:00", "2020-09-30T23:59:59+00:00"));
                        SubscriptionCostResponse response = appclient.save(APPath.subscriptionCost(subscription.getId()), request, SubscriptionCostResponse.class);
                        if (response != null) {
                            Properties props = response.getProperties();
                            List<Column> columns = props.getColumns();
                            BigDecimal subscriptionCost = BigDecimal.ZERO;
                            for (Object[] obj : props.getRows()) {
                                for (int i=0; i<obj.length; i++) {
                                    Column column = columns.get(i);
                                    if ("Cost".equals(column.getName()) && "Number".equals(column.getType())) {
                                        subscriptionCost = subscriptionCost.add(new BigDecimal((double) obj[i]));
                                    }
                                }
                            }
                            log.debug("\t\tAccumulated Subscription Cost: ${}", subscriptionCost.setScale(2, RoundingMode.HALF_UP).toPlainString());
                            totalCustomerCost = totalCustomerCost.add(subscriptionCost);
                        }
                    }
                }
                if (foundAzurePlans) {
                    log.debug("\tTotal Customer Cost: ${}", totalCustomerCost.setScale(2, RoundingMode.HALF_UP).toPlainString());
                }
            }
        }
        return "redirect:/customers";
    }
    
    /**
     * This method is a "callback" from Microsoft's login process. If authentication
    * was successful, the request contains an authorization token that is used to create
     * the PartnerCenter and Azure Portal REST client objects for additional API calls
     * THAT REQUIRE user authentication (App + User), such as the PCPath.customers() API call.
     * 
     * This redirect endpoint is defined by the Azure application used to initiate the Microsoft
     * user login (see the CustomerController.customers(Model uiModel) method above for how the
     * link is constructed for the Microsoft login that redirects to this endpoint.
     * 
     * Enjoy reading Microsoft's "Enable the Secure Application Model" if you want to know more
     * of the gory details... https://docs.microsoft.com/en-us/partner-center/develop/enable-secure-app-model
     * 
     * @param httpServletRequest
     * @param uiModel
     * @return 
     */
    @ResponseStatus(OK)
    @RequestMapping(value = "/relationshipRequest", method = RequestMethod.POST)
    public String relationshipRequest(Model uiModel, HttpServletRequest httpServletRequest) {
        
        String authorizationCode = httpServletRequest.getParameter("code"); // authorization code for your web app to accept from the Azure AD login call
        String token = httpServletRequest.getParameter("id_token");
        
        // make the "refresh" authentication call, using the authorization code
        PCClient client = new PCClient(partnerCenterDomain, partnerTenantId,
                this.devWebAppId, this.devWebAppSecret, this.devPcWebAppId, this.devPcWebAppSecret, authorizationCode);
        
        String redirectURI = azureRedirectHost + "/customers/relationshipRequest";
        client.setRedirectURI(redirectURI);
        PCPath wrapperPath = PCPath.relationshipRequest();
        RelationshipRequest rr = client.get(wrapperPath, RelationshipRequest.class, null);
        log.info("Relationship Request URL: " + rr.getUrl());
        uiModel.addAttribute("requestURL", rr.getUrl());
        return "customers/index";
    }
    
    /**
     * This method is a "callback" from Microsoft's login process. If authentication
     * was successful, the request contains an authorization token that is used to create
     * the PartnerCenter and Azure Portal REST client objects for additional API calls
     * THAT REQUIRE user authentication (App + User), such as the PCPath.customers() API call.
     * 
     * This redirect endpoint is defined by the Azure application used to initiate the Microsoft
     * user login (see the CustomerController.customers(Model uiModel) method above for how the
     * link is constructed for the Microsoft login that redirects to this endpoint.
     * 
     * Enjoy reading Microsoft's "Enable the Secure Application Model" if you want to know more
     * of the gory details... https://docs.microsoft.com/en-us/partner-center/develop/enable-secure-app-model
     * 
     * @param httpServletRequest
     * @return 
     */
    @ResponseStatus(OK)
    @RequestMapping(value = "/customersync", method = RequestMethod.POST)
    public String customerSync(HttpServletRequest httpServletRequest) {
        String referer = httpServletRequest.getHeader("Referer");
        if (StringUtils.isNotBlank(referer)) {
            log.debug("authredirect called by: [{}]", referer);
        }
        String authorizationCode = httpServletRequest.getParameter("code"); // authorization code for your web app to accept from the Azure AD login call
        String token = httpServletRequest.getParameter("id_token");
        
        /**
         * would like to inspect the id_token, but not sure where the signing key comes from
        Claims claims = Jwts
                .parser()
                .setSigningKey("signingkey")
                .parseClaimsJws(token)
                .getBody();
        String audience = claims.get("aud", String.class);
        log.debug("audience: ["+audience+"]");
         */
        
        // we got a redirect, but what for? we don't really know what API call required MFA
        // for now, let's just run through the PC customers() method and see if it works.
        
        // make the "refresh" authentication call, using the authorization code
        PCClient client = new PCClient(partnerCenterDomain, partnerTenantId,
                this.ccaaWebAppId, this.ccaaWebAppSecret, this.pcWebAppId, this.pcWebAppSecret, authorizationCode);
        
        /**
         * this is weird... when a redirect_uri is specified for the authorization token,
         * it needs to be included, again, when the PCClient requests an access token
         * for the API call, which has nothing at all to do with redirects.
         * this does not seem to be documented anywhere
         */
        String redirectURI = azureRedirectHost + "/customers/customersync";
        client.setRedirectURI(redirectURI);
        PCPath wrapperPath = PCPath.customers();
        CustomerWrapper customerWrapper = client.get(wrapperPath, CustomerWrapper.class, null);
        List<com.microsoft.partnercenter.api.schema.datamodel.Customer> customers = customerWrapper.getItems();
        while (customerWrapper.getLinks() != null && customerWrapper.getLinks().getNext() != null) {
            Link next = customerWrapper.getLinks().getNext();
            String continuationToken = null;
            for (KeyValuePair header : next.getHeaders()) {
                if ("MS-ContinuationToken".equals(header.getKey())) {
                    continuationToken = header.getValue();
                }
            }
            wrapperPath = PCPath.nextPath(next.getUri(), continuationToken, wrapperPath);
            customerWrapper = client.get(wrapperPath, CustomerWrapper.class, null);
            customers.addAll(customerWrapper.getItems());
        }
        
        for (com.microsoft.partnercenter.api.schema.datamodel.Customer customer : customers) {
        	log.info("Customer: ID [{}], Name [{}]", new Object[]{customer.getId(), customer.getCompanyProfile().getCompanyName()});
        	
        	List<Customer> matchedCustomers = contractDaoService.findCustomerByName(customer.getCompanyProfile().getCompanyName());
        	if(matchedCustomers != null && !matchedCustomers.isEmpty()) {
        		Customer matchedCustomer = matchedCustomers.get(0);
        		if(matchedCustomer != null) {
	        		log.info("Found Matching Customer: " + matchedCustomer.getName() + " ID: " + matchedCustomer.getId());
	        		matchedCustomer.setAzureCustomerId(customer.getId());
	        		try {
	        			contractDaoService.updateCustomerBits(matchedCustomer);
	        		} catch (Exception e) {
	        			log.error(e.getMessage());
	        			e.printStackTrace();
	        		}
        		}
        	}
        	
        }
        
        
        return "redirect:/customers";
    }
    
    
    
    @Autowired ServiceNowService serviceNowService;
    @RequestMapping(value = "/sns", method = RequestMethod.GET)
    public String serviceNowTest(Model uiModel) {
    	try {
    		serviceNowService.findContractByCustomerSysIdAndJobNumber("2a00443834ccf8c81259236c43b96884", "0174115-1");
    		//serviceNowService.findCustomerByName("EW Scripps");
    		//serviceNowService.findContractCIBySysId("0ccccb55370d5680f6209c9953990e5e");
    		//serviceNowService.findContractCIBySysId("063c85492dc13404c3c7550114d5836d");
    		//serviceNowService.findContractCIsForContract("f2a714eb37f05ec0ffa92b2943990e72");
    		//serviceNowService.findCIsForContract("f2a714eb37f05ec0ffa92b2943990e72");
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
        uiModel.addAttribute("customers", contractDaoService.customers());
        return "customers/index";
    }
}
