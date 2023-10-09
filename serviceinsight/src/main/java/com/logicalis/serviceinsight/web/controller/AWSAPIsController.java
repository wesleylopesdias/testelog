package com.logicalis.serviceinsight.web.controller;

import com.logicalis.serviceinsight.data.AWSAccountCostAndUsage;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.logicalis.serviceinsight.web.RestClient;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

@Controller
@RequestMapping("/awsapis")
public class AWSAPIsController extends BaseController {

    @Value("${awsapis.user}")
    private String awsclientUser;
    @Value("${awsapis.password}")
    private String awsclientPassword;
    @Value("${awsapis.endpoint}")
    private String awsclientEndpoint;

    private static SimpleDateFormat datefmt = new SimpleDateFormat("MM-yyyy");

    /**
     * Returns JSON of the "linked" accounts in our AWS Organization. (use
     * /linkedAccounts.json to invoke the JSON MessageConverter).
     *
     * This could be called to grab a list of accounts to show in the UI
     *
     * request parameters: - year (yyyy)
     */
    @RequestMapping(value = "/linkedAccounts", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody
    Map<String, String> linkedAccounts(
            @RequestParam(value = "year", required = true) Integer year) {
        String pathAndParams = "/costexplorer/linkedAccounts?year={year}";
        RestTemplate awsclient = RestClient.authenticatingRestTemplate(awsclientUser, awsclientPassword);
        return awsclient.getForObject(awsclientEndpoint + pathAndParams,
                Map.class, new Object[]{year});
    }

    /**
     * Returns JSON of the AWS Services. (use /services.json to invoke the JSON
     * MessageConverter)
     *
     * Could be used to show Services in the UI for other queries...
     *
     * request parameters: - year (yyyy)
     */
    @RequestMapping(value = "/services", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody
    String[] services(
            @RequestParam(value = "year", required = true) Integer year) {
        String pathAndParams = "/costexplorer/services?year={year}";
        RestTemplate awsclient = RestClient.authenticatingRestTemplate(awsclientUser, awsclientPassword);
        return awsclient.getForObject(awsclientEndpoint + pathAndParams,
                String[].class, new Object[]{year});
    }

    /**
     * Returns JSON of the AWS "Instance Types". (use /instanceTypes.json to
     * invoke the JSON MessageConverter)
     *
     * Could be used to show AWS Instance Types for a period, used for other
     * queries, perhaps...
     *
     * request parameters: - year (yyyy)
     */
    @RequestMapping(value = "/instanceTypes", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody
    String[] instanceTypes(
            @RequestParam(value = "year", required = true) Integer year) {
        String pathAndParams = "/costexplorer/instanceTypes?year={year}";
        RestTemplate awsclient = RestClient.authenticatingRestTemplate(awsclientUser, awsclientPassword);
        return awsclient.getForObject(awsclientEndpoint + pathAndParams,
                String[].class, new Object[]{year});
    }

    /**
     * Returns JSON of the AWS sub-account Cost and Usage mapped to the
     * AWSAccountCostAndUsage Object as a single result or in an Array. (use
     * /costAndUsage.json to invoke the JSON MessageConverter)
     *
     * Could be used to show AWS Instance Types for a period, used for other
     * queries, perhaps...
     *
     * request parameters: - acct a specific account id to query - year (yyyy)
     * OR month (MM-yyyy)
     */
    @RequestMapping(value = "/costAndUsage", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody
    Object costAndUsage(
            @RequestParam(value = "acct", required = true) String acct,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "month", required = false) @DateTimeFormat(pattern = "MM-yyyy") Date month) {

        // custom validation
        if ((month == null && year == null) || (month != null && year != null)) {
            throw new IllegalArgumentException("one OR the other of parameters: month (pattern MM-yyyy), year (integer yyyy) must be used");
        }
        if (month != null) {
            String pathAndParams = "/costexplorer/costAndUsage?acct={acct}&month={month}";
            RestTemplate awsclient = RestClient.authenticatingRestTemplate(awsclientUser, awsclientPassword);
            return awsclient.getForObject(awsclientEndpoint + pathAndParams,
                    AWSAccountCostAndUsage.class, new Object[]{acct, datefmt.format(month)});
        } else {
            String pathAndParams = "/costexplorer/costAndUsage?acct={acct}&year={year}";
            RestTemplate awsclient = RestClient.authenticatingRestTemplate(awsclientUser, awsclientPassword);
            return awsclient.getForObject(awsclientEndpoint + pathAndParams,
                    List.class, new Object[]{acct, year});
        }
    }

    /**
     * Returns a JSON summary "map" of the specified AWS account's INVOICED
     * usage for each distinct service / operation, such as:
     * 
     * "AWSCostExplorer/USE1-APIRequest" : 132.63
     * "AmazonEC2/BoxUsage:m4.xlarge":4617.00
     *
     * Can be used to show details of a months invoiced activity
     * 
     * note: the sum of a month's invoiced details will add up to the "costAndUsage" total
     *
     * request parameters: - acct a specific account id to query amd month (MM-yyyy)
     */
    @RequestMapping(value = "/invoicedDetails", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody
    Object invoicedDetails(
            @RequestParam(value = "acct", required = true) String acct,
            @RequestParam(value = "month", required = true) @DateTimeFormat(pattern = "MM-yyyy") Date month) {

        String pathAndParams = "/costexplorer/monthlyBillingDetails?acct={acct}&month={month}";
        RestTemplate awsclient = RestClient.authenticatingRestTemplate(awsclientUser, awsclientPassword);
        return awsclient.getForObject(awsclientEndpoint + pathAndParams,
                Map.class, new Object[]{acct, datefmt.format(month)});
    }
}
