package com.logicalis.serviceinsight.service;

import com.logicalis.ap.APClient;
import com.logicalis.ap.APPath;
import com.logicalis.ap.data.Column;
import com.logicalis.ap.data.Properties;
import com.logicalis.ap.data.SubscriptionCostRequest;
import com.logicalis.ap.data.SubscriptionCostResponse;
import com.logicalis.ap.data.TimePeriod;
import com.logicalis.pcc.PCClient;
import com.logicalis.pcc.PCPath;

import static com.logicalis.pcc.PCPath.customerSubscriptionById;
import static com.logicalis.pcc.PCPath.customerSubscriptionUtilizations;
import static com.logicalis.pcc.PCPath.invoiceLineitems;
import static com.logicalis.pcc.PCPath.invoices;
import static com.logicalis.pcc.PCPath.nextPath;
import static com.logicalis.pcc.PCPath.rateCards;

import com.logicalis.pcc.comparators.AzureUtilizationRecordComparator;
import com.logicalis.pcc.comparators.LicenseBasedLineItemComparator;
import com.logicalis.pcc.comparators.OneTimeBasedLineItemComparator;
import com.logicalis.pcc.comparators.UsageBasedLineItemComparator;
import com.logicalis.serviceinsight.dao.CostItem;
import com.logicalis.serviceinsight.dao.CostItem.CostFraction;
import com.logicalis.serviceinsight.dao.Expense;
import com.logicalis.serviceinsight.data.AWSAccountCostAndUsage;
import com.logicalis.serviceinsight.data.SubscriptionUplift;
import com.logicalis.serviceinsight.data.Contract;
import com.logicalis.serviceinsight.data.ContractInvoice;
import com.logicalis.serviceinsight.data.ContractServiceSubscription;
import com.logicalis.serviceinsight.data.Customer;
import com.logicalis.serviceinsight.data.Device;
import com.logicalis.serviceinsight.data.Microsoft365SubscriptionConfig;
import com.logicalis.serviceinsight.data.MicrosoftPriceList;
import com.logicalis.serviceinsight.data.MicrosoftPriceListProduct;
import com.logicalis.serviceinsight.data.MonthlyBillingInfo;
import com.logicalis.serviceinsight.data.PricingSheet;
import com.logicalis.serviceinsight.data.PricingSheetProduct;
import com.logicalis.serviceinsight.data.ScheduledTask;
import com.logicalis.serviceinsight.data.Service;
import com.logicalis.serviceinsight.data.ServiceMin;
import com.logicalis.serviceinsight.representation.AWSBillingPeriodLineItem;
import com.logicalis.serviceinsight.representation.AWSBillingPeriodWrapper;
import com.logicalis.serviceinsight.representation.AWSInvoiceReportWrapper;
import com.logicalis.serviceinsight.representation.AzureBillingPeriodLineItem;
import com.logicalis.serviceinsight.representation.AzureBillingPeriodWrapper;
import com.logicalis.serviceinsight.representation.AzureInvoiceReportWrapper;
import com.logicalis.serviceinsight.representation.CSPLicenseBillingLineItem;
import com.logicalis.serviceinsight.representation.CSPLicenseBillingWrapper;
import com.logicalis.serviceinsight.representation.CSPOneTimeBillingLineItem;
import com.logicalis.serviceinsight.representation.CSPOneTimeBillingWrapper;
import com.logicalis.serviceinsight.util.VersionUtil;
import com.logicalis.serviceinsight.web.RestClient;
import com.microsoft.partnercenter.api.schema.datamodel.AzureMeter;
import com.microsoft.partnercenter.api.schema.datamodel.AzureOfferTerm;
import com.microsoft.partnercenter.api.schema.datamodel.AzureRateCard;
import com.microsoft.partnercenter.api.schema.datamodel.AzureResource;
import com.microsoft.partnercenter.api.schema.datamodel.AzureUtilizationRecord;
import com.microsoft.partnercenter.api.schema.datamodel.AzureUtilizationRecordWrapper;
import com.microsoft.partnercenter.api.schema.datamodel.Invoice;
import com.microsoft.partnercenter.api.schema.datamodel.InvoiceDetail;
import com.microsoft.partnercenter.api.schema.datamodel.InvoiceWrapper;
import com.microsoft.partnercenter.api.schema.datamodel.KeyValuePair;
import com.microsoft.partnercenter.api.schema.datamodel.LicenseBasedLineItem;
import com.microsoft.partnercenter.api.schema.datamodel.LicenseBasedLineItemWrapper;
import com.microsoft.partnercenter.api.schema.datamodel.Link;
import com.microsoft.partnercenter.api.schema.datamodel.OneTimeBasedLineItem;
import com.microsoft.partnercenter.api.schema.datamodel.OneTimeBasedLineItemWrapper;
import com.microsoft.partnercenter.api.schema.datamodel.Subscription;
import com.microsoft.partnercenter.api.schema.datamodel.UsageBasedLineItem;
import com.microsoft.partnercenter.api.schema.datamodel.UsageBasedLineItemWrapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.internet.MimeMessage;
import org.apache.commons.lang.StringUtils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.ui.velocity.VelocityEngineUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 *
 * @author poneil
 */
@org.springframework.stereotype.Service
public class ServiceUsageServiceImpl extends BaseServiceImpl implements ServiceUsageService {

    private static final DateTimeFormatter loggingFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter azureZuluDateTimeFormat = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private static final DateTimeFormatter azureZulu7DigitsDateTimeFormat = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'"); // .9999999Z
    private static final DateTimeFormatter azureOfferTermDateTimeFormat = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final DateTimeFormatter azureUsageDateTimeFormat = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ");
    private static SimpleDateFormat javaAWSDateFmt = new SimpleDateFormat("MM-yyyy");
    private static DateTimeFormatter jodaAWSDateFmt = DateTimeFormat.forPattern("MM-yyyy");
    SimpleDateFormat sqlDateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Value("${osp.api.minlist}")
    private String ospAPIMinlist; // todo: add params to URL?
    @Value("${osp.api.username}")
    private String ospAPIUser;
    @Value("${osp.api.password}")
    private String ospAPIPassword;
    @Value("${partnercenter.username}")
    private String pcUser;
    @Value("${partnercenter.password}")
    private String pcPassword;
    @Value("${partnercenter.domain}")
    private String pcDomain;
    @Value("${partnercenter.tenant.id}")
    private String pcTenantId;
    @Value("${partnercenter.webapp.id}")
    private String pcWebappId;
    @Value("${partnercenter.webapp.key}")
    private String pcWebappKey;
    @Value("${partnercenter.nativeapp.id}")
    private String pcNativeappId;
    @Value("${azure.default.currency}")
    private String azureDefaultCurrency;
    @Value("${azure.default.region}")
    private String azureDefaultRegion;
    @Value("${azure.default.locale}")
    private String azureDefaultLocale;
    @Value("${azure.email.alert.list}")
    private String azureEmailAlertList;
    @Value("${azure.api.version}")
    private String azureAPIVersion;
    @Value("${azure.customer_cost_access_application.webapp.id}")
    private String ccaaWebAppId;
    @Value("${azure.customer_cost_access_application.webapp.secret}")
    private String ccaaWebAppSecret;
    @Value("${awsapis.user}")
    private String awsclientUser;
    @Value("${awsapis.password}")
    private String awsclientPassword;
    @Value("${awsapis.endpoint}")
    private String awsclientEndpoint;

    @Autowired
    ApplicationDataDaoService applicationDataDaoService;
    @Autowired
    ContractDaoService contractDaoService;
    @Autowired
    CostDaoService costDaoService;
    @Autowired
    PricingSheetService pricingSheetService;
    @Autowired
    MicrosoftPricingService microsoftPricingService;
    @Autowired
    ContractRevenueService contractRevenueService;

    private static final Long OSP_SERVICE_ID_CSP_O365 = 90003L;
    private static final String OSP_SERVICE_SYNC_TSK = "osp_service_sync";
    private static final String AZURE_RATE_CARD_SYNC_TSK = "azure_rate_card_sync";
    private static final String AZURE_MONTHLY_COST_IMPORT = "azure_monthly_cost_import";
    private static final String AZURE_MONTHLY_BILLING_SYNC_TSK = "azure_monthly_billing_sync";
    private static final String M365_MONTHLY_BILLING_SYNC_TSK = "azure_m365_sync";
    private static final String AZURE_UPLIFT_PRODUCT_MARGIN = "prdct_uplft";
    private static final String AZURE_UPLIFT_SUPPORT_MARGIN = "spprt_uplft";
    private static final String AWS_MONTHLY_BILLING_SYNC_TSK = "aws_monthly_billing_sync";
    private static final String AWS_UPLIFT_NEW_CUST_MARGIN = "aws_new_cust_prdct_uplft";
    private static final String AWS_UPLIFT_EXSTNG_CUST_MARGIN = "aws_exstng_cust_prdct_uplft";
    private static final String AZURE_CHARGE_TYPE_CANCEL_FEE = "Cancel fee";
    private static final String AZURE_CHARGE_TYPE_CYCLE_FEE = "Cycle fee";
    private static final String AZURE_CHARGE_TYPE_CYCLE_PRORATE = "Cycle instance prorate";
    private static final String AZURE_CHARGE_TYPE_PRORATE_WHEN_PURCHASE = "Prorate fees when purchase";
    private static final String AZURE_CHARGE_TYPE_PRORATE_WHEN_CANCEL = "Prorate fees when cancel";
    private static final String AZURE_CHARGE_TYPE_PRORATE_WHEN_RENEW = "Prorate fee when renew";
    static final String PUBLIC_CLOUD_EXPENSE_CATEGORY_NAME = "Public Cloud";
    static final String AZURE_EXPENSE_CATEGORY_NAME = "Azure";
    static final String AWS_EXPENSE_CATEGORY_NAME = "AWS";
    static final String AZURE_OFFICE_EXPENSE_CATEGORY_NAME = "Office 365";

    @Override
    public List<MonthlyBillingInfo> monthlyBilling(Long customerId, Integer month, Integer year) {
        DateTime startDate = new DateTime()
                .withDayOfMonth(1)
                .withYear(year)
                .withMonthOfYear(month);
        DateTime endDate = startDate.withDayOfMonth(
                startDate.dayOfMonth().getMaximumValue());

        String query = "select distinct p.name, su.start_date, su.units, cp.pricing"
                + " from service_usage su"
                + " left join contract c on c.id = su.contract_id"
                + " left join contract_pricing cp on cp.contract_id = su.contract_id"
                + " left join product p on p.id = su.product_id"
                + " left join service s on s.id = su.service_id"
                + " where c.customer_id = :custId"
                + " and p.id = cp.product_id"
                + " and s.id = cp.service_id"
                + " and su.start_date between :startDate and :endDate"
                + " and c.start_date < :endDate"
                + " and (su.stop_date is null or su.stop_date >= :startDate)";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("custId", customerId);
        params.put("startDate", startDate.toDate());
        params.put("endDate", endDate.toDate());
        return namedJdbcTemplate.query(query,
                params,
                new RowMapper<MonthlyBillingInfo>() {

            @Override
            public MonthlyBillingInfo mapRow(ResultSet rs, int i) throws SQLException {
                return new MonthlyBillingInfo(
                        rs.getString("name"),
                        rs.getDate("start_date"),
                        rs.getBigDecimal("units"),
                        rs.getBigDecimal("pricing"));
            }
        });
    }

    @Async
    @Override
    @Scheduled(cron = "0 0 0 * * *") // every day at midnight
    public void updateRateCard() {
    	try {
	    	ScheduledTask st = applicationDataDaoService.findScheduledTaskByCode(AZURE_RATE_CARD_SYNC_TSK);
	        if (st != null && st.getEnabled()) {
	            log.info("Running Task: " + st.getName());
		        PCClient client = new PCClient(pcDomain, pcTenantId, ccaaWebAppId, ccaaWebAppSecret, pcWebappId, pcWebappKey, null);
		        AzureRateCard rateCard = client.get(rateCards(), AzureRateCard.class, new String[]{
		            String.format("currency=%s", azureDefaultCurrency),
		            String.format("region=%s", azureDefaultRegion)});
		        //log.debug("AzureRateCard meters...");
		        for (AzureMeter meter : rateCard.getMeters()) {
		            // i think we just save all new meters... why would a meter update?
		            applicationDataDaoService.saveAzureMeter(meter, rateCard.getLocale(), rateCard.getCurrency(), rateCard.isIsTaxIncluded());
		        }
		        //log.debug("AzureRateCard terms...");
		        applicationDataDaoService.deleteAzureOfferTerms(rateCard.getLocale(), rateCard.getCurrency());
		        for (AzureOfferTerm term : rateCard.getOfferTerms()) {
		            applicationDataDaoService.saveAzureOfferTerms(term, rateCard.getLocale(), rateCard.getCurrency());
		        }
		        log.info("Ending Task: " + st.getName());
	        }
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    @Override
    @Scheduled(cron = "0 0 8 26 * *") // how about eigth hour of 26th day of the month
    public void syncAzureCustomerCosts() {
        syncAzureCustomerCosts(null);
    }
    
    @Async
    @Override
    public void syncAzureCustomerCosts(DateTime billingInvoiceDate) {
        try {
            ScheduledTask st = applicationDataDaoService.findScheduledTaskByCode(AZURE_MONTHLY_COST_IMPORT);
            if (st == null || !st.getEnabled()) {
                log.info("Scheduled task for Azure Cost Import is either undefined or disabled");
                return;
            }
        } catch (ServiceException se) {
            log.info("failed to run scheduled task for Azure Cost Import: {}", se.getMessage());
            return;
        }
        if (billingInvoiceDate == null) {
            billingInvoiceDate = new DateTime()
                    .withZone(DateTimeZone.UTC)
                    .withTime(0,0,0,0)
                    .dayOfMonth()
                    .withMinimumValue()
                    .plusDays(22);
        }
        log.info("importing Azure monthly invoiced Customer costs for date {}", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ssZ").print(billingInvoiceDate));
        List<AzureInvoiceReportWrapper> reportWrappers = null;
        try {
            reportWrappers = getAzureInvoiceReportData(billingInvoiceDate);
        } catch (Exception any) {
            log.error("exception thrown sync'ing Azure Customer cost data.. exiting", any);
            return;
        }
        /**
         * set the Cost Applied Date to the beginning of the month within the billing cycle, local time
         */
        Date costAppliedDate = billingInvoiceDate
                .withZone(DateTimeZone.forID(TZID))
                .dayOfMonth()
                .withMinimumValue()
                .withTimeAtStartOfDay()
                .toDate();
        Set<String> azureCustomers = new HashSet<String>();
        /**
         * Import Usage based Azure costs
         */
        log.info("importing Usage based azure costs");
        for (AzureInvoiceReportWrapper reportWrapper : reportWrappers) {
            log.info("\tInvoice: {}", reportWrapper.getInvoiceId());
            for (AzureBillingPeriodWrapper record : reportWrapper.getAzureBillingPeriods()) {
                /**
                 * don't insert duplicate costs identified by the invoice and subscription id
                 * ALSO: do not enter ZERO dollar costs
                 */
                Integer count = jdbcTemplate.queryForObject("select count(*) from cost_item"
                        + " where applied = ? and azure_invoice_id = ? and azure_subscription_id = ?", Integer.class,
                        new Object[]{costAppliedDate, reportWrapper.getInvoiceId(), record.getSubscriptionId()});
                if (count == 0 && notZero(record.getRawTotal(), 2)) {
                    try {
                    	//log.info("Attempting to Add Azure Cost of: " + record.getRawTotal() + " for " + record.getCustomerAzureName());
                        Customer customer = null;
                        List<Customer> customers = contractDaoService.findCustomerByName(record.getCustomerAzureName());
                        if (customers != null && customers.size() == 1) {
                            customer = customers.get(0);
                        }
                        if (customer == null) {
                            log.info("\tskipping Usage cost item imports for Azure customer [{}]. No matching SI Customer", record.getCustomerAzureName());
                            azureCustomers.add(record.getCustomerAzureName());
                            continue;
                        }
                        String expenseRecordName = "Automated Azure Expense: " + record.getSubscriptionName();
                        String expenseRecordDescription = "for Azure Customer: " + record.getCustomerAzureName();
                        Expense expenseRecord = new Expense(null, null, Expense.ExpenseType.cost, expenseRecordName, null, record.getRawTotal(),
                                1, (customer == null ? null : customer.getId()), null, null);
                        expenseRecord.setDescription(expenseRecordDescription);
                        expenseRecord.setCreatedBy("system");
                        CostItem azureCostItem = new CostItem();
                        azureCostItem.setExpense(expenseRecord);
                        azureCostItem.setAzureCustomerName(record.getCustomerAzureName());
                        azureCostItem.setAzureInvoiceNo(reportWrapper.getInvoiceId());
                        azureCostItem.setAzureSubscriptionNo(record.getSubscriptionId());
                        azureCostItem.setAmount(record.getRawTotal());
                        azureCostItem.setQuantity(1);
                        azureCostItem.setApplied(costAppliedDate);
                        azureCostItem.setContractId(null);
                        azureCostItem.setCustomerId((customer == null ? null : customer.getId()));
                        azureCostItem.setCreated(billingInvoiceDate.withZone(DateTimeZone.forID(TZID)).toDate());
                        azureCostItem.setCostType(CostItem.CostType.azure);
                        String azureCostItemName = "Automated Azure Cost: " + record.getSubscriptionName();
                        azureCostItem.setName(azureCostItemName);
                        CostFraction azureCostFraction = new CostFraction();
                        azureCostFraction.setExpenseCategory(costDaoService.expenseCategoryByName(AZURE_EXPENSE_CATEGORY_NAME, PUBLIC_CLOUD_EXPENSE_CATEGORY_NAME));
                        azureCostFraction.setFraction(new BigDecimal(100));
                        azureCostItem.addCostFraction(azureCostFraction);
                        azureCostItem.setCreatedBy("system");
                        Long azureCostItemId = contractDaoService.saveCostItem(azureCostItem);
                    } catch (Exception any) {
                        log.warn("Failed to create a cost for Azure Expense", any);
                    }
                }
            }
        }
        /**
         * Import License based Azure costs
         */
        log.debug("importing License based azure costs");
        for (AzureInvoiceReportWrapper reportWrapper : reportWrappers) {
            log.info("\tInvoice: {}", reportWrapper.getInvoiceId());
            for (CSPLicenseBillingWrapper record : reportWrapper.getCspLicenses()) { // outer loop is Customer based
                Customer customer = null;
                List<Customer> customers = contractDaoService.findCustomerByName(record.getCustomerAzureName());
                if (customers != null && customers.size() == 1) {
                    customer = customers.get(0);
                }
                if (customer == null) {
                    log.info("\tskipping License cost item imports for Azure customer [{}]. No matching SI Customer", record.getCustomerAzureName());
                    azureCustomers.add(record.getCustomerAzureName());
                    continue;
                }
                for (CSPLicenseBillingLineItem lineitem :record.getLineItems()) { // inner loop is lineitem subscription based
                    /**
                     * don't insert duplicate costs identified by the invoice and subscription id
                     * ALSO: do not enter ZERO dollar costs
                     */
                    Integer count = jdbcTemplate.queryForObject("select count(*) from cost_item"
                            + " where applied = ? and azure_invoice_id = ? and azure_subscription_id = ?", Integer.class,
                            new Object[]{costAppliedDate, reportWrapper.getInvoiceId(), lineitem.getId()});
                    if (count == 0 && notZero(lineitem.getTotal(), 2)) {
                        try {
                            String expenseRecordName = "Automated Azure License Expense: " + lineitem.getName();
                            String expenseRecordDescription = "for Azure Customer: " + record.getCustomerAzureName();
                            Integer quantity = 1;
                            if (lineitem.getQuantity() != null && lineitem.getQuantity() > 0) {
                                quantity = lineitem.getQuantity();
                            }
                            Expense expenseRecord = new Expense(null, null, Expense.ExpenseType.cost, expenseRecordName, null, lineitem.getTotal(),
                                    quantity, (customer == null ? null : customer.getId()), null, null);
                            expenseRecord.setDescription(expenseRecordDescription);
                            expenseRecord.setCreatedBy("system");
                            CostItem azureCostItem = new CostItem();
                            azureCostItem.setExpense(expenseRecord);
                            azureCostItem.setAzureCustomerName(record.getCustomerAzureName());
                            azureCostItem.setAzureInvoiceNo(reportWrapper.getInvoiceId());
                            azureCostItem.setAzureSubscriptionNo(lineitem.getId());
                            azureCostItem.setAmount(lineitem.getTotal());
                            azureCostItem.setQuantity(quantity);
                            azureCostItem.setApplied(costAppliedDate);
                            azureCostItem.setContractId(null);
                            azureCostItem.setCustomerId((customer == null ? null : customer.getId()));
                            azureCostItem.setCreated(billingInvoiceDate.withZone(DateTimeZone.forID(TZID)).toDate());
                            azureCostItem.setCostType(CostItem.CostType.o365);
                            String azureCostItemName = "Automated Azure License Cost: " + lineitem.getName();
                            azureCostItem.setName(azureCostItemName);
                            CostFraction azureCostFraction = new CostFraction();
                            azureCostFraction.setExpenseCategory(costDaoService.expenseCategoryByName(AZURE_OFFICE_EXPENSE_CATEGORY_NAME, PUBLIC_CLOUD_EXPENSE_CATEGORY_NAME));
                            azureCostFraction.setFraction(new BigDecimal(100));
                            azureCostItem.addCostFraction(azureCostFraction);
                            azureCostItem.setCreatedBy("system");
                            Long azureCostItemId = contractDaoService.saveCostItem(azureCostItem);
                        } catch (Exception any) {
                            log.warn("Failed to create a cost for Azure Office Expense", any);
                        }
                    }
                }
            }
        }
        /**
         * Import OneTime based Azure costs
         */
        log.debug("importing OneTime based azure costs");
        for (AzureInvoiceReportWrapper reportWrapper : reportWrappers) {
            log.info("\tInvoice: {}", reportWrapper.getInvoiceId());
            for (CSPOneTimeBillingWrapper record : reportWrapper.getCspOnetimes()) { // outer loop is Customer based
                Customer customer = null;
                List<Customer> customers = contractDaoService.findCustomerByName(record.getCustomerAzureName());
                if (customers != null && customers.size() == 1) {
                    customer = customers.get(0);
                }
                if (customer == null) {
                    log.info("\tskipping OneTime cost item imports for Azure customer [{}]. No matching SI Customer", record.getCustomerAzureName());
                    azureCustomers.add(record.getCustomerAzureName());
                    continue;
                }
                for (CSPOneTimeBillingLineItem lineitem :record.getLineItems()) { // inner loop is lineitem subscription based
                    /**
                     * don't insert duplicate costs identified by the invoice and subscription id
                     * ALSO: do not enter ZERO dollar costs
                     */
                    Integer count = jdbcTemplate.queryForObject("select count(*) from cost_item"
                            + " where applied = ? and azure_invoice_id = ? and azure_subscription_id = ?", Integer.class,
                            new Object[]{costAppliedDate, reportWrapper.getInvoiceId(), lineitem.getId()});
                    if (count == 0 && notZero(lineitem.getSubscriptionTotal(), 2)) {
                        try {
                            String expenseRecordName = "Automated Azure OneTime Expense: " + lineitem.getSubscriptionName();
                            String expenseRecordDescription = "for Azure Customer: " + record.getCustomerAzureName();
                            Integer quantity = 1;
                            if (lineitem.getQuantity() != null && lineitem.getQuantity() > 0) {
                                quantity = lineitem.getQuantity();
                            }
                            Expense expenseRecord = new Expense(null, null, Expense.ExpenseType.cost, expenseRecordName, null, lineitem.getSubscriptionTotal(),
                                    quantity, (customer == null ? null : customer.getId()), null, null);
                            expenseRecord.setDescription(expenseRecordDescription);
                            expenseRecord.setCreatedBy("system");
                            CostItem azureCostItem = new CostItem();
                            azureCostItem.setExpense(expenseRecord);
                            azureCostItem.setAzureCustomerName(record.getCustomerAzureName());
                            azureCostItem.setAzureInvoiceNo(reportWrapper.getInvoiceId());
                            azureCostItem.setAzureSubscriptionNo(lineitem.getId());
                            azureCostItem.setAmount(lineitem.getSubscriptionTotal());
                            azureCostItem.setQuantity(quantity);
                            azureCostItem.setApplied(costAppliedDate);
                            azureCostItem.setContractId(null);
                            azureCostItem.setCustomerId((customer == null ? null : customer.getId()));
                            azureCostItem.setCreated(billingInvoiceDate.withZone(DateTimeZone.forID(TZID)).toDate());
                            azureCostItem.setCostType(CostItem.CostType.azure);
                            String azureCostItemName = "Automated Azure OneTime Cost: " + lineitem.getSubscriptionName();
                            azureCostItem.setName(azureCostItemName);
                            CostFraction azureCostFraction = new CostFraction();
                            azureCostFraction.setExpenseCategory(costDaoService.expenseCategoryByName(AZURE_EXPENSE_CATEGORY_NAME, PUBLIC_CLOUD_EXPENSE_CATEGORY_NAME));
                            azureCostFraction.setFraction(new BigDecimal(100));
                            azureCostItem.addCostFraction(azureCostFraction);
                            azureCostItem.setCreatedBy("system");
                            Long azureCostItemId = contractDaoService.saveCostItem(azureCostItem);
                        } catch (Exception any) {
                            log.warn("Failed to create a cost for Azure OneTime Expense", any);
                        }
                    }
                }
            }
        }
        if (azureCustomers.size() > 0) {
            String dateString = DateTimeFormat.forPattern("MM/yyyy").print(billingInvoiceDate);
            try {
                List<String> results = new ArrayList<String>();
                results.addAll(azureCustomers);
                Collections.sort(results);
                reportAzureOnlyCustomerNames(azureEmailAlertList, results, dateString);
            } catch (ServiceException ex) {
                Logger.getLogger(ServiceUsageServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private boolean notZero(BigDecimal amount, int scale) {
        if (amount != null && amount.setScale(scale, RoundingMode.HALF_UP).compareTo(BigDecimal.ZERO) != 0) {
            return true;
        }
        return false;
    }

    @Async
    @Override
    @Scheduled(cron = "0 0 7 26 * *") // how about eigth hour of 26th day of the month
    public void syncOffice365Invoices() throws ServiceException {
    	getOffice365InvoiceReportData(null);
    }
    
    @Async
    @Override
    public void syncOffice365InvoicesForMonth(DateTime billingInvoiceDate) throws ServiceException {
    	getOffice365InvoiceReportData(billingInvoiceDate);
    }
    
    private void getOffice365InvoiceReportData(DateTime billingInvoiceDate) throws ServiceException {
    	try {
            ScheduledTask st = applicationDataDaoService.findScheduledTaskByCode(M365_MONTHLY_BILLING_SYNC_TSK);
            if (st == null || !st.getEnabled()) {
                log.info("Scheduled task for M365 Invoice Billing Sync is either undefined or disabled");
                return;
            }
        } catch (ServiceException se) {
            log.info("failed to run scheduled task for M365 Invoice Billing Sync: {}", se.getMessage());
            return;
        }
    	
    	if (billingInvoiceDate == null) {
            billingInvoiceDate = new DateTime()
                    .withZone(DateTimeZone.UTC)
                    .withTime(0,0,0,0)
                    .dayOfMonth()
                    .withMinimumValue()
                    .plusDays(22);
        }
    	
    	log.info("importing Azure monthly O365 charges for date {}", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ssZ").print(billingInvoiceDate));
        
    	Map<CSPLicenseBillingWrapper, List<String>> resultsMap = new HashMap<CSPLicenseBillingWrapper, List<String>>();
    	
        try {
            List<CSPLicenseBillingWrapper> billingWrappers = getInvoicedLicenseDetails(billingInvoiceDate);
            
        	log.debug("importing License based azure Usage to Contracts");
            for (CSPLicenseBillingWrapper record : billingWrappers) { // outer loop is Customer based
            	try {
            		List<String> resultMessages = new ArrayList<String>();
            		log.info("Processing: " + record.getCustomerAzureName());
	            	Microsoft365SubscriptionConfig subscriptionConfig = microsoftPricingService.getMicrosoft365SubscriptionConfigByActiveAndTenantId(record.getCustomerId());
	            	if(subscriptionConfig == null) {
	            		resultMessages.add("No active M365 Subscription Config found for this subscription.");
	            		resultsMap.put(record, resultMessages);
	            		continue;
	            	}
	            	log.info("Found Config in Contract: " + subscriptionConfig.getContractId());
	            	
	                Contract contract = contractDaoService.contract(subscriptionConfig.getContractId());
	                if (contract == null) {
	            		resultMessages.add("Contract not found for ID [" + subscriptionConfig.getContractId() + "] that is associated with the M365 Subscription Config.");
	            		resultsMap.put(record, resultMessages);
	                	continue;
	                }
	                Long contractId = contract.getId();
	                
	                PricingSheet pricingSheet = pricingSheetService.findPricingSheetForContract(contractId);
	                if(pricingSheet == null) {
	            		resultMessages.add("Pricing Sheet not found for Contract ID [" + contractId + "] that is associated with the M365 Subscription Config.");
	            		resultsMap.put(record, resultMessages);
	                	continue;
	                }
	                
	                billingInvoiceDate = billingInvoiceDate.withZone(DateTimeZone.forID(TZID));
	                Integer month = billingInvoiceDate.getMonthOfYear() + 1;
	                Date serviceStartDate = billingInvoiceDate.withTime(0,0,0,0).withMonthOfYear(month).dayOfMonth().withMinimumValue().toDate();
	                Date serviceEndDate = billingInvoiceDate.withMonthOfYear(month).dayOfMonth().withMaximumValue().withTime(23,59,59,999).toDate();
	                BigDecimal monthlyTotal = new BigDecimal(0);
	                
	                //group all the licenses together, so we can deal with prorating and the other oddities that microsoft has
	                Map<String, List<CSPLicenseBillingLineItem>> map = new HashMap<String, List<CSPLicenseBillingLineItem>>();
	                for (CSPLicenseBillingLineItem cspLineItem :record.getLineItems()) {
	                	String offerId = cspLineItem.getDurableOfferId();
	                	if(map.get(offerId) == null) {
	                		map.put(offerId, new ArrayList<CSPLicenseBillingLineItem>());
	                	}
	                	
	                	if(AZURE_CHARGE_TYPE_CYCLE_FEE.equals(cspLineItem.getChargeType()) && !map.get(offerId).isEmpty()) {
	                		//log.debug("type is cycle fee: " + cspLineItem.getOfferName());
	                		boolean found = false;
	                		for(CSPLicenseBillingLineItem mapLineItem : map.get(offerId)) {
	                			//log.debug("checking existing line items");
	                			if(AZURE_CHARGE_TYPE_CYCLE_FEE.equals(mapLineItem.getChargeType()) && mapLineItem.getSubscriptionStartDate().equals(cspLineItem.getSubscriptionStartDate()) && mapLineItem.getQuantity() == cspLineItem.getQuantity()) {
	                				//log.debug("Found line item to merge: " + cspLineItem.getOfferName());
	                				mapLineItem.setUnitPrice(mapLineItem.getUnitPrice().add(cspLineItem.getUnitPrice()));
	                				found = true;
	                				break;
	                			}
	                		}
	                		if(found) continue;
	                	}
	                	
	                	map.get(offerId).add(cspLineItem);
	                }
	                
	                Boolean error = Boolean.FALSE;
	                for (Map.Entry<String, List<CSPLicenseBillingLineItem>> entry : map.entrySet()) {
	                    String durableOfferId = entry.getKey();
	                    List<CSPLicenseBillingLineItem> cspLineItems = entry.getValue();
	                    
	                    PricingSheetProduct pricingSheetProduct = null;
	                	for(PricingSheetProduct product : pricingSheet.getProducts()) {
	                		if(durableOfferId.equals(product.getDeviceAltId())) {
	                			if((Microsoft365SubscriptionConfig.Type.M365.equals(subscriptionConfig.getType()) && Device.DeviceType.M365.equals(product.getDeviceType())) || (Microsoft365SubscriptionConfig.Type.O365.equals(subscriptionConfig.getType()) && Device.DeviceType.cspO365.equals(product.getDeviceType()))) {
	                				log.debug("Found mathcing Pricing Sheet Product for: " + product.getDeviceDescription() + ". " + cspLineItems.size() + " Items to loop through.");
	                				pricingSheetProduct = product;
	                				break;
	                			} else {
	                				log.info("Found Pricing Sheet Product buy Offer Id, but It doesn't match the M365 Subscription Config Type");
	                			}
	                		}
	                	}
	                	
	                	if(pricingSheetProduct == null) {
		            		String offerName = "";
		            		if(cspLineItems != null && !cspLineItems.isEmpty()) offerName = cspLineItems.get(0).getOfferName();
		            		resultMessages.add("Pricing Sheet Product not found for Offer ID [" + durableOfferId + ", " + offerName + "].");
		            		log.info("Pricing Sheet Product not found for Offer ID [" + durableOfferId + ", " + offerName + "].");
		            		error = Boolean.TRUE;
	                		continue;
	                	}
	                    
	                	Integer quantity = 0;
	                    BigDecimal totalPrice = new BigDecimal(0);
	                    String note = "This record was automatically created as part of the automated M365 billing for the Tenant [" + record.getCustomerAzureName() + "].";
	                    Device.DeviceType deviceType = pricingSheetProduct.getDeviceType();
	                    
	                    Date lineItemSubscriptionStartDate = new Date();
	                    Date lineItemSubscriptionEndDate = new Date();
	                    log.info("About to loop through");
	                    for (CSPLicenseBillingLineItem cspLineItem : cspLineItems) {
	                    	log.info("LineItem: " + cspLineItem);
		                	//pull pricing info from pricing list
	                    	String chargeType = cspLineItem.getChargeType();
		                	DateTime subscriptionStartDate = new DateTime(cspLineItem.getSubscriptionStartDate()).dayOfMonth().withMinimumValue().withTime(0,0,0,0).withZone(DateTimeZone.forID(TZID));
		                	lineItemSubscriptionStartDate = cspLineItem.getSubscriptionStartDate();
		                	lineItemSubscriptionEndDate = cspLineItem.getSubscriptionEndDate();
		                	MicrosoftPriceList priceList = microsoftPricingService.getMicrosoftPriceListForMonthOf(subscriptionStartDate.toDate(), MicrosoftPriceList.MicrosoftPriceListType.M365);
		                	
		                	if(priceList == null) {
			            		resultMessages.add("Microsoft Pricing List not found for Offer ID [" + cspLineItem.getOfferName() + " with subscription date " + subscriptionStartDate + "].");
			            		log.info("Microsoft Pricing List not found for Offer ID [" + cspLineItem.getOfferName() + " with subscription date " + subscriptionStartDate + "].");
		                		error = Boolean.TRUE;
		                		continue;
		                	}
		                	
		                	MicrosoftPriceListProduct priceListProduct = microsoftPricingService.getMicrosoftPriceListProductByOfferId(priceList.getId(), durableOfferId);
		                	if(priceListProduct == null) {
			            		resultMessages.add("Microsoft Pricing List Product not found for Offer ID [" + cspLineItem.getOfferName() + ", " + durableOfferId + "].");
			            		log.info("Microsoft Pricing List Product not found for Offer ID [" + cspLineItem.getOfferName() + ", " + durableOfferId + "].");
		                		error = Boolean.TRUE;
		                		continue;
		                	}
		                	
		                	BigDecimal lineItemPrice = new BigDecimal(0);
		                	Integer lineItemQuantity = cspLineItem.getQuantity();
		                	if(AZURE_CHARGE_TYPE_CYCLE_FEE.equals(chargeType) || AZURE_CHARGE_TYPE_PRORATE_WHEN_RENEW.equals(chargeType)) {
		                		quantity += lineItemQuantity;
		                		lineItemSubscriptionStartDate = cspLineItem.getSubscriptionStartDate();
			                	lineItemSubscriptionEndDate = cspLineItem.getSubscriptionEndDate();
		                	}
		                	
		                	BigDecimal discountPercentRate = new BigDecimal(1);
		                	BigDecimal erpPrice = priceListProduct.getErpPrice();
		                	log.info("ERP Price: " + erpPrice);
		                	
		                	if(Device.DeviceType.M365.equals(deviceType)) {
		                		//note that we've already calculated it as M365 above
		                		BigDecimal discount = pricingSheetProduct.getDiscount();
		                		if(discount == null) discount = new BigDecimal(0);
		                		discountPercentRate = (new BigDecimal(100).subtract(discount)).divide(new BigDecimal(100));
		                		log.debug("About to do [" + erpPrice + "] x " + discountPercentRate + "%");
	                    	} else if(Device.DeviceType.cspO365.equals(deviceType)) {
	                    		//for O365 products, we just use the MR Price they've specified in the pricing sheet. we also don't discount, so we'll set that to 1 to negate it.
	                    		erpPrice = pricingSheetProduct.getRecurringPrice();
	                    		discountPercentRate = new BigDecimal(1);
	                    	}
		                	
		                	BigDecimal listPrice = priceListProduct.getUnitPrice();
		                	if(AZURE_CHARGE_TYPE_PRORATE_WHEN_PURCHASE.equals(chargeType) || AZURE_CHARGE_TYPE_PRORATE_WHEN_RENEW.equals(chargeType) || AZURE_CHARGE_TYPE_PRORATE_WHEN_CANCEL.equals(chargeType) || AZURE_CHARGE_TYPE_CYCLE_PRORATE.equals(chargeType)) {
		                		listPrice = cspLineItem.getUnitPrice().divide(listPrice, 10, RoundingMode.HALF_UP);
		                		log.debug("prorated list price: " + listPrice);
		                		erpPrice = erpPrice.multiply(listPrice);
		                		log.debug("ERP Price of license changed due to proration. Now set at: " + erpPrice);
		                	}
		                	
		                	BigDecimal lineItemDiscountedPrice = erpPrice.multiply(discountPercentRate);
		                	lineItemPrice = lineItemDiscountedPrice.multiply(new BigDecimal(lineItemQuantity));	                    			
		                	totalPrice = totalPrice.add(lineItemPrice);
		                	log.info("Total Price: " + totalPrice);
		                	
		                	//handle cancel fee
		                	
		                	//add detail to note
		                	note += "\nFee Breakdown - Line Item Cost: $" + cspLineItem.getUnitPrice() + " Line Item Price: $" + lineItemDiscountedPrice.setScale(2, RoundingMode.HALF_UP) + " x " + lineItemQuantity + " Licenses";
		                }
	                    
	                    if(error) {
	                    	log.info("Error occurred: " + resultMessages);
	                    	resultsMap.put(record, resultMessages);
	                    	break;
	                    } else {
	                    	log.info("no error");
	                    }
		                
		                //save service
	                    try {
	                    	log.info("Setting Service");
	                        Service service = new Service();
	                        service.setServiceId(pricingSheetProduct.getServiceId());
	                        service.setContractId(contractId);
	                        service.setDeviceId(pricingSheetProduct.getDeviceId());
	                        service.setStartDate(serviceStartDate);
	                        service.setDeviceUnitCount(quantity);
	                        service.setEndDate(serviceEndDate);
	                        service.setOnetimeRevenue(new BigDecimal(0));
	                        service.setRecurringRevenue(totalPrice);
	                        service.setStatus(Service.Status.active);
	                        service.setMicrosoft365SubscriptionConfigId(subscriptionConfig.getId());
	                        service.setNote(note);
	
	                        if(Device.DeviceType.M365.equals(pricingSheetProduct.getDeviceType())) {
	                        	monthlyTotal = monthlyTotal.add(totalPrice);
	                        }
	                        
	                        try {
	                        	//make sure it's not already in the contract
	                            Calendar c = Calendar.getInstance();
	                            c.setTime(serviceStartDate);
	                        	Integer monthInt = c.get(Calendar.MONTH) + 1;
	                        	String year = String.valueOf(c.get(Calendar.YEAR));
	                        	boolean exists = false;
	                        	List<Service> existingServices = contractRevenueService.serviceRevenueParentRecordsForMonthOf(contractId, null, monthInt, year, Service.Status.active, Boolean.FALSE);
	                        	for(Service existingService : existingServices) {
	                        		if(service.getDeviceId().equals(existingService.getDeviceId()) && service.getMicrosoft365SubscriptionConfigId().equals(existingService.getMicrosoft365SubscriptionConfigId())) {
	                        			log.info("License [" + pricingSheetProduct.getDeviceDescription() + "] already exists in contract [" + contractId + "]. Not adding again");
	                        			exists = true;
	                        			break;
	                        		}
	                        	}
	                        	
	                        	//check for invoiced
	    	                    List<ContractInvoice> contractInvoices = contractDaoService.contractInvoicesForContract(contractId);
	                        	boolean invoiceConflictExists = contractDaoService.contractInvoiceConflictExistsForContractService(contractInvoices, service, BatchResult.Operation.create);
	                        	
	                        	if(invoiceConflictExists) {
	                        		error = true;
	                        		resultMessages.add("Contract is already invoiced for Contract ID: " + contractId);
	                        		log.info("Won't insert service. Month has already been invoiced.");
	                        		break;
	                        	}
	                        	
	                            if(!exists) {
	                            	log.info("About to insert Service: " + service);
	                            	contractDaoService.saveContractService(service, Boolean.FALSE);
	                            	resultMessages.add("Added Contract Service for [" + pricingSheetProduct.getDeviceDescription() + "] for: $" + totalPrice);
	                            }
	                            
	                            
	                            //save details down to the pricing sheet product
	                            if(!exists) {
		    	                    pricingSheetProduct.setPreviousUnitCount(pricingSheetProduct.getUnitCount());
		    	                    pricingSheetProduct.setUnitCount(quantity);
		    	                    pricingSheetProduct.setSubscriptionStartDate(lineItemSubscriptionStartDate);
		    	                    pricingSheetProduct.setSubscriptionEndDate(lineItemSubscriptionEndDate);
		    	                    
		    	                    pricingSheetService.updatePricingSheetProduct(pricingSheetProduct);
	                            }
	                        } catch (ServiceException ex) {
	                            Logger.getLogger(ServiceUsageServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
	                            resultMessages.add("Error occurred: " + ex);
		                        resultsMap.put(record, resultMessages);
	                        }
	                        
	                        
	                    } catch (Exception any) {
	                        log.warn("Failed to create a cost for Azure Office Expense", any);
	                        resultMessages.add("Error occurred: " + any);
	                        resultsMap.put(record, resultMessages);
	                    }
	                }
	                
	                if(error) {
	                	log.info("outer error: " + resultMessages);
	                	resultsMap.put(record, resultMessages);
	                	continue;
	                }
	                
	                //calculate support costs
	                if(Microsoft365SubscriptionConfig.Type.M365.equals(subscriptionConfig.getType())) {
		                try {
		                	Service ospService = contractDaoService.serviceByOspIdAndActive(OSP_SERVICE_ID_CSP_O365);
		                	if(ospService == null) {
		                		//error
		                	}
		                	log.debug("Service: " + ospService);
		                	
		                	BigDecimal supportPrice = new BigDecimal(0);
		                	if(Microsoft365SubscriptionConfig.SupportType.flat.equals(subscriptionConfig.getSupportType())) {
		                		supportPrice = subscriptionConfig.getFlatFee();
		                	} else {
		                		supportPrice = monthlyTotal.multiply(subscriptionConfig.getPercent().divide(new BigDecimal(100)));
		                	}
		                	
		                	List<Device> supportDevices = applicationDataDaoService.findDeviceByDeviceType(Device.DeviceType.M365Support); 
		                	Device supportDevice = null;
		                	for(Device device : supportDevices) {
		                		if(!device.getArchived()) {
		                			supportDevice = device;
		                			break;
		                		}
		                	}
		                	
		                    Service service = new Service();
		                    service.setContractId(contractId);
		                    service.setDeviceId(supportDevice.getId());
		                    service.setServiceId(ospService.getServiceId());
		                    service.setStartDate(serviceStartDate);
		                    service.setQuantity(1);
		                    service.setEndDate(serviceEndDate);
		                    service.setOnetimeRevenue(new BigDecimal(0));
		                    service.setRecurringRevenue(supportPrice);
		                    service.setStatus(Service.Status.active);
		                    service.setMicrosoft365SubscriptionConfigId(subscriptionConfig.getId());
		                    service.setNote("CSP Support Fee for M365 Licenses for [" + record.getCustomerAzureName() + "].");
		                    
		                    try {
		                    	//make sure it's not already in the contract
		                        Calendar c = Calendar.getInstance();
		                        c.setTime(serviceStartDate);
		                    	Integer monthInt = c.get(Calendar.MONTH) + 1;
		                    	String year = String.valueOf(c.get(Calendar.YEAR));
		                    	boolean exists = false;
		                    	List<Service> existingServices = contractRevenueService.serviceRevenueParentRecordsForMonthOf(contractId, null, monthInt, year, Service.Status.active, Boolean.FALSE);
		                    	for(Service existingService : existingServices) {
		                    		if(service.getDeviceId().equals(existingService.getDeviceId()) && service.getMicrosoft365SubscriptionConfigId().equals(existingService.getMicrosoft365SubscriptionConfigId())) {
		                    			exists = true;
		                    			break;
		                    		}
		                    	}
		                    	
		                        if(!exists) {
		                        	contractDaoService.saveContractService(service, Boolean.FALSE);
		                        	resultMessages.add("Added Contract Service for [" + supportDevice.getDescription() + "] for: $" + supportPrice);
		                        }
		                        
		                        resultsMap.put(record, resultMessages);
		                    } catch (ServiceException ex) {
		                        Logger.getLogger(ServiceUsageServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
		                        resultMessages.add("Error occurred: " + ex);
		                        resultsMap.put(record, resultMessages);
		                    }
		                } catch (Exception e) {
		                	e.printStackTrace();
		                	log.warn("Failed to create a support fee for M365 Licenses", e);
		                	resultMessages.add("Error occurred: " + e);
	                        resultsMap.put(record, resultMessages);
		                }
	                }
	        	} catch (Exception e) {
	        		
	        	}
          }
            
          //email errors
          Map<String, String> results = new TreeMap<String, String>();
          for (Map.Entry<CSPLicenseBillingWrapper, List<String>> entry : resultsMap.entrySet()) {
        	  CSPLicenseBillingWrapper record = entry.getKey();
              List<String> errors = entry.getValue();
              
              String result = "<b>[" + record.getCustomerAzureName() + "]</b><br/><ul>";
              for(String error : errors) {
            	  result += "<li>" + error + "</li>";
              }
              result += "</ul><br/><br/>";
        	  results.put(record.getCustomerAzureName(), result);
          }
          
          sendMonthlyM365BillingSummary(azureEmailAlertList, results, billingInvoiceDate);
            
            
        } catch (Exception any) {
            log.error("exception thrown sync'ing Azure Customer cost data.. exiting", any);
            return;
        }
        
    }
    
    @Override
    @Scheduled(cron = "0 30 1 8 * *") // how about 1:30AM on the 8th of every month (Azure billing is on the 6th)
    public void syncAzureCustomerInvoices() {
    	syncAzureCustomerInvoices(null);
    }
    
    /**
     * Auto-generates the contract service records for Azure invoiced billing.
     * 
     * @param billingInvoiceDate Azure invoices bill on the 6th for the previous months "period" of charges.
     * When we create contract service records, the start and end dates represent the INVOICE month, not the
     * period of the charges, example: a billingInvoiceDate of 11/2020 queries for the 10/1-10/31 period of
     * charges and contract service records will be generated with start and end dates of 11/1-11/31
     * 
     */
    @Async
    @Override
    public void syncAzureCustomerInvoices(DateTime billingInvoiceDate) {
    	try {
            ScheduledTask st = applicationDataDaoService.findScheduledTaskByCode(AZURE_MONTHLY_BILLING_SYNC_TSK);
            if (st != null && st.getEnabled()) {
                log.info("Running Task: " + st.getName());

                /**
                 * an even more newer way to play with the dates!
                 * ensure the billingInvoiceDate is a "correct" value
                 * for the month
                 */
                billingInvoiceDate = (
                        billingInvoiceDate == null ?
                        new DateTime().dayOfMonth().withMinimumValue().withTime(0,0,0,0).plusDays(5) :
                        billingInvoiceDate.withZoneRetainFields(DateTimeZone.forID(TZID)).dayOfMonth().withMinimumValue().withTime(0,0,0,0).plusDays(5)
                        );
                
                // now let's create a date for checking the SI contract subscription start and end dates
                DateTime siStartDateTime = billingInvoiceDate.minusMonths(1).dayOfMonth().withMinimumValue();

                List<ContractServiceSubscription> csas = contractDaoService.contractServiceSubscriptions(null, ContractServiceSubscription.SubscriptionType.cspazure);
                List<ContractServiceSubscription> activeCsas = new ArrayList<ContractServiceSubscription>();
                for(ContractServiceSubscription csa : csas) {
                    if (siStartDateTime.compareTo(new DateTime(csa.getStartDate())) >= 0 &&
                            siStartDateTime.compareTo(new DateTime(csa.getEndDate())) <= 0) {
                        activeCsas.add(csa);
                    }
                }

                String noresults = "";
                Map<String, String> results = new TreeMap<String, String>();
                List<AzureBillingPeriodWrapper> azureInvoices = getInvoicesForAzureSubscription(activeCsas, billingInvoiceDate, Boolean.TRUE);
                if (azureInvoices != null && !azureInvoices.isEmpty()) {
                    for (AzureBillingPeriodWrapper invoice : azureInvoices) {
                        results.put(invoice.getSubscriptionId(), "Service succesfully added for customer in the amount of: " + invoice.getTotal() + " for Subscription: [" + invoice.getSubscriptionName() + " -- " + invoice.getSubscriptionId() + "]");
                    }
                } else {
                    noresults += "No invoices were returned from Azure legacy Usage-based subscriptions.";
                }

                String templateMsg = "<p>Contract Service successfully added for Customer: [%s], Subscription: [%s], ID: [%s], Amount: $%s</p>";
                List<CSPOneTimeBillingWrapper> cspInvoices = getInvoicesForAzureOneTime(activeCsas, billingInvoiceDate, Boolean.TRUE);
                if (cspInvoices != null && !cspInvoices.isEmpty()) {
                    boolean onetimeResultsAdded = false;
                    for (CSPOneTimeBillingWrapper invoice : cspInvoices) {
                        for (CSPOneTimeBillingLineItem lineItem : invoice.getLineItems()) {
                            if (lineItem.getError() != null) {
                                com.logicalis.ap.ServiceException apse = lineItem.getError();
                                String errorMessage = String.format("statusCode [%s], statusText [%s], message [%s], fieldMessage [%s]",
                                        new Object[]{apse.getStatusCode().name(), apse.getStatusText(), apse.getMessage(), apse.getFieldMessage()});
                                results.put(invoice.getCustomerId()+"|"+lineItem.getId(), "ERROR adding contract service record for Azure Customer: ["
                                        + (invoice.getCustomerAzureName() == null ? invoice.getCustomerSIName() : invoice.getCustomerAzureName())
                                        + "], Subscription: [" + lineItem.getSubscriptionName() + "], ID: [" + lineItem.getId() + "], Error: [" + errorMessage);
                                onetimeResultsAdded = true;
                            } else if (lineItem.getPersisted() && lineItem.getSubscriptionTotal().compareTo(BigDecimal.ZERO) > 0) {
                                String serviceTotal = lineItem.getSubscriptionTotal().add(lineItem.getUpliftTotal()).setScale(2, RoundingMode.HALF_UP).toPlainString();
                                results.put(invoice.getCustomerId()+"|"+lineItem.getId(), String.format(templateMsg, new Object[]{
                                    (invoice.getCustomerAzureName() == null ? invoice.getCustomerSIName() : invoice.getCustomerAzureName()),
                                    lineItem.getSubscriptionName(), lineItem.getId(), serviceTotal}));
                                onetimeResultsAdded = true;
                            }
                        }
                    }
                    if (!onetimeResultsAdded) {
                        noresults += "<br/>No Contract Services were added for Azure OneTime (Azure Plan) subscriptions.";
                    }
                } else {
                    noresults += "<br/>No invoices were returned for Azure OneTime (Azure Plan) subscriptions.";
                }
                if (noresults.length() > 0) {
                    results.put("", noresults);
                }
                //email matt with a billing status
                sendMonthlyAzureBillingSummary(azureEmailAlertList, results, billingInvoiceDate);

                log.info("Ending Task: " + st.getName());
            }
    	} catch (Exception e) {
            e.printStackTrace();
    	}
    }
    
    @Override
    public List<AzureBillingPeriodWrapper> getInvoicesForAzureSubscription(List<ContractServiceSubscription> csas, DateTime billingInvoiceDate, Boolean persistContractService) throws ServiceException {
    	List<AzureBillingPeriodWrapper> invoices = new ArrayList<AzureBillingPeriodWrapper>();
        PCClient client = new PCClient(pcDomain, pcTenantId, ccaaWebAppId, ccaaWebAppSecret, pcWebappId, pcWebappKey, null);
    	try {
            InvoiceWrapper wrapper = client.get(invoices(), InvoiceWrapper.class, new String[]{});
            
            for (Invoice invoice : wrapper.getItems()) {
                DateTime invoiceDate;
                try {
                    if (invoice.getInvoiceDate().indexOf(".") < 0) {
                        invoiceDate = azureZuluDateTimeFormat.withZone(DateTimeZone.UTC).parseDateTime(invoice.getInvoiceDate());
                    } else {
                        invoiceDate = azureZulu7DigitsDateTimeFormat.withZone(DateTimeZone.UTC).parseDateTime(invoice.getInvoiceDate());
                    }
                } catch (IllegalArgumentException iae) {
                    log.warn(iae.getMessage());
                    continue;
                }
                
                /**
                 * note: the billingInvoiceDate is a month after the accrued charges
                 */
                if (billingInvoiceDate.getYear() == invoiceDate.getYear() &&
                        billingInvoiceDate.getMonthOfYear() == invoiceDate.getMonthOfYear()) {
                    if (invoice.getTotalCharges() == null) {
                        log.debug("getting Usage details for INVOICE {}, date: {}, total charges [null]", invoice.getId(),
                                loggingFormatter.print(invoiceDate));
                    } else {
                        log.debug("getting Usage details for INVOICE {}, date: {}, total charges ${}", new Object[]{invoice.getId(),
                            loggingFormatter.print(invoiceDate), invoice.getTotalCharges().setScale(2, RoundingMode.HALF_UP)});
                    }
                    for (InvoiceDetail detail : invoice.getInvoiceDetails()) {
                        log.debug("billingProvider: {}, invoiceLineitemType: {}", new Object[]{detail.getBillingProvider(), detail.getInvoiceLineItemType()});
                        if ("azure".equals(detail.getBillingProvider()) && "billing_line_items".equals(detail.getInvoiceLineItemType())) {
                            try {
                                invoices = getInvoiceLineItems(client, invoice.getId(), "Azure", "BillingLineItems", csas, persistContractService, billingInvoiceDate);
                            } catch(Exception any) {
                                log.info("caught exception looking up Usage-based invoice line items: [{}]", any.getMessage());
                            }
                        } else if ("office".equals(detail.getBillingProvider()) && "billing_line_items".equals(detail.getInvoiceLineItemType())) {
                            log.info("ignoring OFFICE billing provider...");
                        } else if ("one_time".equals(detail.getBillingProvider()) && "billing_line_items".equals(detail.getInvoiceLineItemType())) {
                            log.info("ignoring OneTime billing provider...");
                        }
                        // what about this one: billingProvider: marketplace, invoiceLineitemType: usage_line_items
                    }
                }
            }
        } catch(Exception any) {
            log.info("caught exception getting contract and customer info for an Azure subscription...", any.getMessage());
        }
        return invoices;
    }
    
    @Override
    public List<CSPOneTimeBillingWrapper> getInvoicesForAzureOneTime(List<ContractServiceSubscription> csas, DateTime billingInvoiceDate, Boolean persistContractService) throws ServiceException {
    	List<CSPOneTimeBillingWrapper> invoices = new ArrayList<CSPOneTimeBillingWrapper>();
        
        DateTime billingInvoiceStartDate = (
                billingInvoiceDate == null ?
                new DateTime().dayOfMonth().withMinimumValue().withTime(0,0,0,0) :
                billingInvoiceDate.withZoneRetainFields(DateTimeZone.forID(TZID)).dayOfMonth().withMinimumValue().withTime(0,0,0,0)
                );
        DateTime billingInvoiceEndDate = billingInvoiceStartDate
                .dayOfMonth().withMaximumValue().withTime(23,59,59,999); // how about that time??
        
        /**
         * this is very important: the billingInvoiceDate is a month ahead of the queried
         * charges. Also, the Azure timezone is UTC, so we convert to UTC before calling the API
         */
        DateTime subscriptionRequestStartDateTime = billingInvoiceDate
                .withZoneRetainFields(DateTimeZone.UTC)
                .minusMonths(1).dayOfMonth().withMinimumValue();
        DateTime subscriptionRequestEndDateTime = subscriptionRequestStartDateTime
                .dayOfMonth().withMaximumValue().withTime(23,59,59,999);
        
    	for (ContractServiceSubscription csa : csas) {
            try {
                Contract contract = contractDaoService.contract(csa.getContractId());
                Customer customer = contractDaoService.customer(contract.getCustomerId());
                log.debug("Customer: {}, Contract Id {}, Contract Name {}", new Object[]{customer.getName(), contract.getId(), contract.getName()});
                CSPOneTimeBillingWrapper invoice;
                int index = invoices.indexOf(new CSPOneTimeBillingWrapper(csa.getCustomerId().toLowerCase()));
                if (index > -1) {
                    invoice = invoices.get(index);
                } else {
                    APClient apclient = new APClient(azureAPIVersion, csa.getCustomerId().toLowerCase(), this.ccaaWebAppId, this.ccaaWebAppSecret);
                    invoice = new CSPOneTimeBillingWrapper(csa.getCustomerId().toLowerCase(), customer.getName(), customer.getAltName());
                    invoice.setApclient(apclient);
                    invoices.add(invoice);
                }
                log.debug("\trequesting subscription cost from Azure Cost Management API for Customer [{}], subscription [{}], dates: [{}] to [{}]",
                        new Object[]{(invoice.getCustomerAzureName() == null ? invoice.getCustomerSIName() : invoice.getCustomerAzureName()), csa.getSubscriptionId(),
                            azureUsageDateTimeFormat.print(subscriptionRequestStartDateTime), azureUsageDateTimeFormat.print(subscriptionRequestEndDateTime)});
                CSPOneTimeBillingLineItem lineitem = new CSPOneTimeBillingLineItem(csa.getSubscriptionId());
                lineitem.setSubscriptionName(csa.getName()); // should match the "distinct" Azure Subscription name
                invoice.getLineItems().add(lineitem);
                SubscriptionCostRequest request = new SubscriptionCostRequest(
                        new TimePeriod(azureUsageDateTimeFormat.print(subscriptionRequestStartDateTime),
                                azureUsageDateTimeFormat.print(subscriptionRequestEndDateTime)));
                try {
                    SubscriptionCostResponse response = invoice.getApclient().save(APPath.subscriptionCost(lineitem.getId()), request, SubscriptionCostResponse.class);
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
                        log.debug("\tAccumulated Subscription Cost: ${}", subscriptionCost.setScale(2, RoundingMode.HALF_UP).toPlainString());
                        lineitem.setSubscriptionTotal(subscriptionCost);
                        invoice.incrementTotal(lineitem.getSubscriptionTotal());
                        
                        //Create Service
                        Service existingService = contractDaoService.contractServiceByContractServiceSubscriptionAndStartDate(csa.getId(), billingInvoiceStartDate.toDate());
                        Service subscriptionService = null;
                        //we only persist if requested too, as this method can also be used just to read the azure monthly results
                        if(persistContractService) {
                            //we only want to create the record if there is not already one for that month
                            if(existingService == null) {
                                //we also only want to create a record if it has a dollar amount?
                                if (lineitem.getSubscriptionTotal().compareTo(BigDecimal.ZERO) > 0) {
                                    BigDecimal serviceTotal = lineitem.getSubscriptionTotal().add(lineitem.getUpliftTotal());
                                    log.debug("\tgenerating a monthly Contract Service object for subscription [{}], with latest total: [${}]",
                                            new Object[]{lineitem.getSubscriptionName(), serviceTotal.setScale(2, RoundingMode.HALF_UP)});
                                    subscriptionService = new Service();
                                    subscriptionService.setContractServiceSubscriptionId(csa.getId());
                                    subscriptionService.setServiceId(csa.getServiceId());
                                    subscriptionService.setContractId(csa.getContractId());
                                    subscriptionService.setDeviceId(csa.getDeviceId());
                                    subscriptionService.setStartDate(billingInvoiceStartDate.toDate());
                                    //we set the end date to a time with zero, so it matches contract end dates created through the system
                                    billingInvoiceEndDate = billingInvoiceEndDate.withTime(0, 0, 0, 0);
                                    subscriptionService.setEndDate(billingInvoiceEndDate.toDate());
                                    subscriptionService.setOnetimeRevenue(BigDecimal.ZERO);
                                    subscriptionService.setRecurringRevenue(serviceTotal);
                                    subscriptionService.setStatus(Service.Status.active);
                                    subscriptionService.setName(lineitem.getSubscriptionName()); // isn't persisted... there is no "name" field
                                    
                                    String note = "Automated Azure billing for the tenant ["
                                            + (invoice.getCustomerAzureName() == null ? invoice.getCustomerSIName() : invoice.getCustomerAzureName()) + "]"
                                            + " subscription [" + lineitem.getSubscriptionName() + "]";
                                    subscriptionService.setNote(note);

                                    try {
                                        contractDaoService.saveContractService(subscriptionService, Boolean.FALSE);
                                        lineitem.setPersisted(Boolean.TRUE);
                                    } catch (ServiceException ex) {
                                        Logger.getLogger(ServiceUsageServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                } else {
                                    log.debug("\tskipping subscription [{}] with $0 total for month", new Object[]{lineitem.getSubscriptionName()});
                                }
                            } else {
                                log.debug("\tContract Service Record Already Exists for this month & subcription ID");
                            }
                        }
                    }
                } catch (com.logicalis.ap.ServiceException apse) {
                    log.info("exception thrown by APClient - statusCode [{}], statusText [{}], message [{}], fieldMessage [{}]",
                            new Object[]{apse.getStatusCode().name(), apse.getStatusText(), apse.getMessage(), apse.getFieldMessage()});
                    lineitem.setError(apse);
                }
            } catch(Exception any) {
                log.info("caught exception getting contract and customer info for an Azure subscription...", any.getMessage());
                continue;
            }
        }
        return invoices;
    }
    
    private List<AzureBillingPeriodWrapper> getInvoiceLineItems(PCClient client, String invoiceId, String billingProvider, String invoiceLineitemType, List<ContractServiceSubscription> csas, Boolean persistContractService, DateTime billingInvoiceDate) throws ServiceException {
        PCPath wrapperPath = invoiceLineitems(invoiceId, billingProvider, invoiceLineitemType);
        UsageBasedLineItemWrapper wrapper = client.get(wrapperPath, UsageBasedLineItemWrapper.class, new String[]{"size=1000", "offset=0"});
        //assertNotNull(wrapper);
        List<UsageBasedLineItem> usageLineItems = wrapper.getItems();
        while (wrapper.getLinks() != null && wrapper.getLinks().getNext() != null) {
            Link next = wrapper.getLinks().getNext();
            String continuationToken = null;
            for (KeyValuePair header : next.getHeaders()) {
                if ("MS-ContinuationToken".equals(header.getKey())) {
                    continuationToken = header.getValue();
                }
            }
            wrapperPath = nextPath(next.getUri(), continuationToken, wrapperPath);
            wrapper = client.get(wrapperPath, UsageBasedLineItemWrapper.class, null);
            usageLineItems.addAll(wrapper.getItems());
        }
        Collections.sort(usageLineItems, new UsageBasedLineItemComparator());
        
        List<AzureBillingPeriodWrapper> azureInvoices = new ArrayList<AzureBillingPeriodWrapper>();
        for(ContractServiceSubscription csa : csas) {
            String csaSubscriptionId = csa.getSubscriptionId();
            if(csaSubscriptionId == null) { // not sure why this would happen, but ok...
                continue;
            }
            int counter = 0;
            BigDecimal totalUsage = BigDecimal.ZERO;
            List<AzureBillingPeriodLineItem> lineItems = new ArrayList<AzureBillingPeriodLineItem>();
            AzureBillingPeriodWrapper azureInvoice = new AzureBillingPeriodWrapper();
            for (UsageBasedLineItem record : usageLineItems) {
                if (record.getSubscriptionId() != null && csaSubscriptionId.toLowerCase().equals(record.getSubscriptionId().toLowerCase())) {
                    azureInvoice.setSubscriptionName(record.getSubscriptionName());
                    String key = record.getCustomerCompanyName()+"|"+record.getSubscriptionName()+"|"+record.getServiceName()+"|"+record.getResourceName();

                    boolean found = false;
                    for(AzureBillingPeriodLineItem lineItem : lineItems) {
                        if(lineItem.getGroupKey().equals(key)) {
                            lineItem.setTotal(lineItem.getTotal().add(record.getPostTaxTotal()));
                            found = true;
                            break;
                        }
                    }
                    if(!found) {
                        lineItems.add(new AzureBillingPeriodLineItem(record.getResourceGuid(), key, record.getResourceName(), record.getConsumedQuantity(), record.getPostTaxTotal()));
                    }
                    totalUsage = totalUsage.add(record.getPostTaxTotal());
                    counter ++;
                }
            }

            //Add Product Margins
            BigDecimal totalPrice = totalUsage;
            // there's no reason to do any of this if the totalPrice is == 0, right??
            if (totalPrice.compareTo(BigDecimal.ZERO) > 0) {
                log.debug("{} records... calculated a total for Subscription and subscription ${}", new Object[]{counter, totalUsage.setScale(2, RoundingMode.HALF_UP).toPlainString()});
                try {
                    SubscriptionUplift productUplift = applicationDataDaoService.subscriptionUpliftByCode(AZURE_UPLIFT_PRODUCT_MARGIN);
                    if(productUplift != null) {
                        if(SubscriptionUplift.UpliftType.percentage.equals(productUplift.getUpliftType())) {
                            BigDecimal productPriceUplift = totalUsage.multiply(productUplift.getUplift());
                            totalPrice = productPriceUplift.add(totalPrice);
                            log.debug("Adding [{}%] product uplift [${}], new total is: [${}]", new Object[]{new BigDecimal(100).multiply(productUplift.getUplift()), productPriceUplift.setScale(4, RoundingMode.HALF_UP), totalPrice.setScale(4, RoundingMode.HALF_UP)});
                            AzureBillingPeriodLineItem lineItem = new AzureBillingPeriodLineItem(AZURE_UPLIFT_PRODUCT_MARGIN, null, messageSource.getMessage("ui_label_product_margin", null, LocaleContextHolder.getLocale()), new BigDecimal(1), productPriceUplift);
                            lineItems.add(lineItem);
                        }
                    }

                    SubscriptionUplift supportUplift = applicationDataDaoService.subscriptionUpliftByCode(AZURE_UPLIFT_SUPPORT_MARGIN);
                    if(supportUplift != null) {
                        if(SubscriptionUplift.UpliftType.percentage.equals(supportUplift.getUpliftType())) {
                            BigDecimal supportPriceUplift = totalUsage.multiply(supportUplift.getUplift());
                            totalPrice = supportPriceUplift.add(totalPrice);
                            log.debug("Adding [{}%] support uplift [${}], new total is: [${}]", new Object[]{new BigDecimal(100).multiply(supportUplift.getUplift()), supportPriceUplift.setScale(4, RoundingMode.HALF_UP), totalPrice.setScale(4, RoundingMode.HALF_UP)});
                            AzureBillingPeriodLineItem lineItem = new AzureBillingPeriodLineItem(AZURE_UPLIFT_SUPPORT_MARGIN, null, messageSource.getMessage("ui_label_support_margin", null, LocaleContextHolder.getLocale()), new BigDecimal(1), supportPriceUplift);
                            lineItems.add(lineItem);
                        }
                    }
                } catch (Exception e) {
                    log.info("Margins not added because no active ones were found.");
                }

                azureInvoice.setLineItems(lineItems);
                azureInvoice.setSubscriptionId(csaSubscriptionId);
                azureInvoice.setTotal(totalPrice);
                azureInvoices.add(azureInvoice);

                //Create Service
                Date serviceStartDate = billingInvoiceDate.dayOfMonth().withMinimumValue().toDate();
                Date serviceEndDate = billingInvoiceDate.dayOfMonth().withMaximumValue().withTime(23,59,59,999).toDate();
                Service existingService = contractDaoService.contractServiceByContractServiceSubscriptionAndStartDate(csa.getId(), serviceStartDate);
                Service subscriptionService = null;

                //we only persist if requested too, as this method can also be used just to read the azure monthly results
                if(persistContractService) {
                    //we only want to create the record if there is not already one for that month
                    if(existingService == null) {
                        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
                        log.debug("generating a monthly Contract Service object for subscription [{}], with latest total: [${}]",
                                new Object[]{azureInvoice.getSubscriptionName(), totalPrice.setScale(2, RoundingMode.HALF_UP)});
                        subscriptionService = new Service();
                        subscriptionService.setContractServiceSubscriptionId(csa.getId());
                        subscriptionService.setServiceId(csa.getServiceId());
                        subscriptionService.setContractId(csa.getContractId());
                        subscriptionService.setDeviceId(csa.getDeviceId());
                        subscriptionService.setStartDate(serviceStartDate);
                        subscriptionService.setEndDate(serviceEndDate);
                        subscriptionService.setOnetimeRevenue(totalPrice);
                        subscriptionService.setStatus(Service.Status.active);
                        subscriptionService.setName(csa.getName());

                        String note = "This record was automatically created as part of the automated Azure billing. This is for the environment " + azureInvoice.getSubscriptionName() + " and the service period of " + sdf.format(billingInvoiceDate.minusMonths(1).toDate()) + " to " + sdf.format(billingInvoiceDate.minusDays(1).toDate()) + ".";
                        subscriptionService.setNote(note);

                        try {
                            contractDaoService.saveContractService(subscriptionService, Boolean.FALSE);
                        } catch (ServiceException ex) {
                            Logger.getLogger(ServiceUsageServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else {
                        log.info("Contract Service Record Already Exists for this month & subcription ID");
                    }
                }
            } // conditional on totalPrice > $0
        }
        return azureInvoices;
    }
    
    private List<CSPLicenseBillingWrapper> getInvoicedLicenseDetails(DateTime billingInvoiceDate) throws ServiceException {
    	PCClient client = new PCClient(pcDomain, pcTenantId, ccaaWebAppId, ccaaWebAppSecret, pcWebappId, pcWebappKey, null);
    	List<CSPLicenseBillingWrapper> cspLicenseBillingPeriods = new ArrayList<CSPLicenseBillingWrapper>();
    	try {
            InvoiceWrapper wrapper = client.get(invoices(), InvoiceWrapper.class, new String[]{});
            
            for (Invoice invoice : wrapper.getItems()) {
                DateTime invoiceDate;
                try {
                    if (invoice.getInvoiceDate().indexOf(".") < 0) {
                        invoiceDate = azureZuluDateTimeFormat.withZone(DateTimeZone.UTC).parseDateTime(invoice.getInvoiceDate());
                    } else {
                        invoiceDate = azureZulu7DigitsDateTimeFormat.withZone(DateTimeZone.UTC).parseDateTime(invoice.getInvoiceDate());
                    }
                } catch (IllegalArgumentException iae) {
                    log.warn(iae.getMessage());
                    continue;
                }
                if (billingInvoiceDate.getYear() == invoiceDate.getYear() &&
                        billingInvoiceDate.getMonthOfYear() == invoiceDate.getMonthOfYear()) {
                    if (invoice.getTotalCharges() == null) {
                        log.debug("getting Report Usage/License/OneTime based details for INVOICE {}, date: {}, total charges [null]", invoice.getId(),
                                loggingFormatter.print(invoiceDate));
                    } else {
                        log.debug("getting Report Usage/License/OneTime based details for INVOICE {}, date: {}, total charges ${}", new Object[]{invoice.getId(),
                            loggingFormatter.print(invoiceDate), invoice.getTotalCharges().setScale(2, RoundingMode.HALF_UP)});
                    }
                    for (InvoiceDetail detail : invoice.getInvoiceDetails()) {
                        log.debug("billingProvider: {}, invoiceLineitemType: {}", new Object[]{detail.getBillingProvider(), detail.getInvoiceLineItemType()});
                        if ("office".equals(detail.getBillingProvider()) && "billing_line_items".equals(detail.getInvoiceLineItemType())) {
                            try {
                                cspLicenseBillingPeriods.addAll(getReportInvoiceLicenseLineItems(client, invoice.getId(), "Office", "BillingLineItems", true));
                            } catch(Exception any) {
                                log.info("caught exception looking up License-based invoice line items: [{}]", any.getMessage());
                            }
                        } else {
                        	log.debug("Skipping this one because it has a billing Provider of: " + detail.getBillingProvider());
                        }
                    }
                }
            }
        } catch(Exception any) {
            log.info("caught exception getting contract and customer info for an Azure subscription... {}", any.getMessage());
        }
    	return cspLicenseBillingPeriods;
    }
    
    @Override
    public List<AzureInvoiceReportWrapper> getAzureInvoiceReportData(DateTime billingInvoiceDate) throws ServiceException {
    	List<AzureInvoiceReportWrapper> reportWrappers = new ArrayList<AzureInvoiceReportWrapper>();
        PCClient client = new PCClient(pcDomain, pcTenantId, ccaaWebAppId, ccaaWebAppSecret, pcWebappId, pcWebappKey, null);
    	try {
            InvoiceWrapper wrapper = client.get(invoices(), InvoiceWrapper.class, new String[]{});
            
            for (Invoice invoice : wrapper.getItems()) {
                DateTime invoiceDate;
                try {
                    if (invoice.getInvoiceDate().indexOf(".") < 0) {
                        invoiceDate = azureZuluDateTimeFormat.withZone(DateTimeZone.UTC).parseDateTime(invoice.getInvoiceDate());
                    } else {
                        invoiceDate = azureZulu7DigitsDateTimeFormat.withZone(DateTimeZone.UTC).parseDateTime(invoice.getInvoiceDate());
                    }
                } catch (IllegalArgumentException iae) {
                    log.warn(iae.getMessage());
                    continue;
                }
                if (billingInvoiceDate.getYear() == invoiceDate.getYear() &&
                        billingInvoiceDate.getMonthOfYear() == invoiceDate.getMonthOfYear()) {
                    AzureInvoiceReportWrapper reportWrapper = new AzureInvoiceReportWrapper();
                    reportWrappers.add(reportWrapper);
                    if (invoice.getTotalCharges() == null) {
                        log.debug("getting Report Usage/License/OneTime based details for INVOICE {}, date: {}, total charges [null]", invoice.getId(),
                                loggingFormatter.print(invoiceDate));
                    } else {
                        log.debug("getting Report Usage/License/OneTime based details for INVOICE {}, date: {}, total charges ${}", new Object[]{invoice.getId(),
                            loggingFormatter.print(invoiceDate), invoice.getTotalCharges().setScale(2, RoundingMode.HALF_UP)});
                    }
                    reportWrapper.setInvoiceId(invoice.getId());
                    for (InvoiceDetail detail : invoice.getInvoiceDetails()) {
                        log.debug("billingProvider: {}, invoiceLineitemType: {}", new Object[]{detail.getBillingProvider(), detail.getInvoiceLineItemType()});
                        if ("azure".equals(detail.getBillingProvider()) && "billing_line_items".equals(detail.getInvoiceLineItemType())) {
                            try {
                                List<AzureBillingPeriodWrapper> azureBillingPeriods = getReportInvoiceLineItems(client, invoice.getId(), "Azure", "BillingLineItems");
                                reportWrapper.setAzureBillingPeriods(azureBillingPeriods);
                            } catch(Exception any) {
                                log.info("caught exception looking up Usage-based invoice line items: [{}]", any.getMessage());
                            }
                        } else if ("office".equals(detail.getBillingProvider()) && "billing_line_items".equals(detail.getInvoiceLineItemType())) {
                            try {
                                List<CSPLicenseBillingWrapper> cspLicenseBillingPeriods = getReportInvoiceLicenseLineItems(client, invoice.getId(), "Office", "BillingLineItems", false);
                                reportWrapper.setCspLicenses(cspLicenseBillingPeriods);
                            } catch(Exception any) {
                                log.info("caught exception looking up License-based invoice line items: [{}]", any.getMessage());
                            }
                        } else if ("one_time".equals(detail.getBillingProvider()) && "billing_line_items".equals(detail.getInvoiceLineItemType())) {
                            try {
                                List<CSPOneTimeBillingWrapper> cspOneTimeBillingPeriods = getReportInvoiceOneTimeLineItems(client, invoice.getId(), "OneTime", "BillingLineItems");
                                reportWrapper.setCspOnetimes(cspOneTimeBillingPeriods);
                            } catch(Exception any) {
                                log.info("caught exception looking up OneTime-based invoice line items: [{}]", any.getMessage());
                            }
                        } else {
                        	log.debug("Skipping this one because it has a billing Provider of: " + detail.getBillingProvider());
                        }
                    }
                }
            }
        } catch(Exception any) {
            log.info("caught exception getting contract and customer info for an Azure subscription... {}", any.getMessage());
        }
        return reportWrappers;
    }
    
    @Override
    public AWSInvoiceReportWrapper getAWSInvoiceReportData(DateTime billingInvoiceDate, Boolean includeBillingDetails) throws ServiceException {
        
        if (billingInvoiceDate == null) {
            throw new IllegalArgumentException("billing invoice date must not be null");
        }
        
        /**
         * Look up the linked accounts we have in this year. It's the only way to lookup AWS accounts
         * in the CostExplorer API
         */
    	AWSInvoiceReportWrapper reportWrapper = new AWSInvoiceReportWrapper();
        String pathAndParams = "/costexplorer/linkedAccounts?year={year}";
        RestTemplate awsclient = RestClient.authenticatingRestTemplate(awsclientUser, awsclientPassword);
        Map<String, String> accounts = awsclient.getForObject(awsclientEndpoint + pathAndParams,
                Map.class, new Object[]{billingInvoiceDate.year().getAsString()});
        for (Map.Entry<String, String> entry : accounts.entrySet()) {
            String accountName = entry.getKey();
            String accountId = entry.getValue();
            
            /**
             * Now, take each linked account and grab the various cost values
             */
            pathAndParams = "/costexplorer/costAndUsage?acct={acct}&month={month}";
            AWSAccountCostAndUsage invoice = awsclient.getForObject(awsclientEndpoint + pathAndParams, AWSAccountCostAndUsage.class,
                    new Object[]{accountId, jodaAWSDateFmt.print(billingInvoiceDate)});

            if(invoice != null) {
                AWSBillingPeriodWrapper wrapper = new AWSBillingPeriodWrapper();
                wrapper.setAccountId(accountId);
                wrapper.setCustomerAWSName(accountName);
                wrapper.setSubscriptionName("Amazon Web Services"); // this is static... nothing coming from AWS to indicate a Subscription name
                BigDecimal rawTotal = invoice.getBlendedCost();
	    	if(rawTotal.compareTo(BigDecimal.ZERO) > 0 && includeBillingDetails) {
                    
                    /**
                     * And for each account grab the list of cost details
                     */
                    String detailsPathAndParams = "/costexplorer/monthlyBillingDetails?acct={acct}&month={month}";
                    Map invoiceLineItems = awsclient.getForObject(awsclientEndpoint + detailsPathAndParams, Map.class, new Object[]{accountId, jodaAWSDateFmt.print(billingInvoiceDate)});
                    Iterator it = invoiceLineItems.entrySet().iterator();
                    List<AWSBillingPeriodLineItem> lineItems = new ArrayList<AWSBillingPeriodLineItem>();
                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry)it.next();
                        BigDecimal amount = new BigDecimal((double) pair.getValue());
                        if(amount.compareTo(BigDecimal.ZERO) > 0) {
                                AWSBillingPeriodLineItem invoiceLineItem = new AWSBillingPeriodLineItem((String) pair.getKey(), amount);
                                lineItems.add(invoiceLineItem);
                        }
                        it.remove();
                    }
                    wrapper.setLineItems(lineItems);
                }
                Date serviceStartDate = billingInvoiceDate.dayOfMonth().withMinimumValue().toDate();
                Date serviceEndDate = billingInvoiceDate.dayOfMonth().withMaximumValue().toDate();
                wrapper.setStartDate(serviceStartDate);
                wrapper.setEndDate(serviceEndDate);
                wrapper.setRawTotal(rawTotal);
                reportWrapper.addAWSBillingPeriod(wrapper);
            }
        }
        return reportWrapper;
    }
    
    private List<AzureBillingPeriodWrapper> getReportInvoiceLineItems(PCClient client, String invoiceId, String billingProvider, String invoiceLineitemType) throws ServiceException {
        PCPath wrapperPath = invoiceLineitems(invoiceId, billingProvider, invoiceLineitemType);
        UsageBasedLineItemWrapper wrapper = client.get(wrapperPath, UsageBasedLineItemWrapper.class, new String[]{"size=1000", "offset=0"});
        //assertNotNull(wrapper);
        List<UsageBasedLineItem> usageLineItems = wrapper.getItems();
        while (wrapper.getLinks() != null && wrapper.getLinks().getNext() != null) {
            Link next = wrapper.getLinks().getNext();
            String continuationToken = null;
            for (KeyValuePair header : next.getHeaders()) {
                if ("MS-ContinuationToken".equals(header.getKey())) {
                    continuationToken = header.getValue();
                }
            }
            wrapperPath = nextPath(next.getUri(), continuationToken, wrapperPath);
            wrapper = client.get(wrapperPath, UsageBasedLineItemWrapper.class, null);
            usageLineItems.addAll(wrapper.getItems());
        }
        Collections.sort(usageLineItems, new UsageBasedLineItemComparator());
        
        List<AzureBillingPeriodWrapper> azureInvoices = new ArrayList<AzureBillingPeriodWrapper>();
    	int counter = 0;
        BigDecimal totalUsage = BigDecimal.ZERO;
        
        for (UsageBasedLineItem record : usageLineItems) {
            AzureBillingPeriodWrapper azureInvoice = new AzureBillingPeriodWrapper();
            azureInvoice.setSubscriptionId(record.getSubscriptionId());
            int index = azureInvoices.indexOf(azureInvoice);
            if (index > -1) {
                azureInvoice = azureInvoices.get(index);
            } else {
            	azureInvoice.setSubscriptionName(record.getSubscriptionName());
            	azureInvoice.setCustomerAzureName(record.getCustomerCompanyName());
                azureInvoices.add(azureInvoice);
            }
            boolean found = false;
            String key = record.getCustomerCompanyName()+"|"+record.getSubscriptionName()+"|"+record.getServiceName()+"|"+record.getResourceName();
            
            for(AzureBillingPeriodLineItem lineItem : azureInvoice.getLineItems()) {
            	if(lineItem.getGroupKey().equals(key)) {
            		lineItem.setTotal(lineItem.getTotal().add(record.getPostTaxTotal()));
            		found = true;
            		break;
            	}
            }
            
            if(!found) {
                azureInvoice.addLineItem(new AzureBillingPeriodLineItem(record.getResourceGuid(), key, record.getResourceName(), record.getConsumedQuantity(), record.getPostTaxTotal()));
            }
            if(azureInvoice.getRawTotal() == null) {
            	azureInvoice.setRawTotal(record.getPostTaxTotal());
            } else {
            	azureInvoice.setRawTotal(azureInvoice.getRawTotal().add(record.getPostTaxTotal()));
            }
            counter++;
        }
        log.debug("{} records... calculated a total for Subscription and subscription ${}", new Object[]{counter, totalUsage.setScale(2, RoundingMode.HALF_UP).toPlainString()});
        
        return azureInvoices;
    }
    
    private List<CSPLicenseBillingWrapper> getReportInvoiceLicenseLineItems(PCClient client, String invoiceId, String billingProvider, String invoiceLineitemType, Boolean includeDetail) throws ServiceException {
        PCPath wrapperPath = invoiceLineitems(invoiceId, billingProvider, invoiceLineitemType);
        LicenseBasedLineItemWrapper wrapper = client.get(wrapperPath, LicenseBasedLineItemWrapper.class, new String[]{"size=1000", "offset=0"});
        //assertNotNull(wrapper);
        List<LicenseBasedLineItem> licenseLineItems = wrapper.getItems();
        while (wrapper.getLinks() != null && wrapper.getLinks().getNext() != null) {
            Link next = wrapper.getLinks().getNext();
            String continuationToken = null;
            for (KeyValuePair header : next.getHeaders()) {
                if ("MS-ContinuationToken".equals(header.getKey())) {
                    continuationToken = header.getValue();
                }
            }
            wrapperPath = nextPath(next.getUri(), continuationToken, wrapperPath);
            wrapper = client.get(wrapperPath, LicenseBasedLineItemWrapper.class, null);
            licenseLineItems.addAll(wrapper.getItems());
        }
        Collections.sort(licenseLineItems, new LicenseBasedLineItemComparator());
        
        List<CSPLicenseBillingWrapper> cspInvoices = new ArrayList<CSPLicenseBillingWrapper>();
        
        for (LicenseBasedLineItem record : licenseLineItems) {
            try {
                if (record.getCustomerId() == null) {
                    log.info("skipping invoice record with chargeType [{}], customerName [{}]: has NULL customerId",
                            new Object[]{record.getChargeType(), record.getCustomerName()});
                    continue;
                }
                CSPLicenseBillingWrapper cspInvoice = new CSPLicenseBillingWrapper(record.getCustomerId(), record.getCustomerName());
                if (cspInvoices.contains(cspInvoice)) {
                    for(CSPLicenseBillingWrapper invoice : cspInvoices) {
                        if (invoice.equals(cspInvoice)) {
                            cspInvoice = invoice;
                            break;
                        }
                    }
                } else {
                    cspInvoices.add(cspInvoice);
                }
                cspInvoice.setTotal(cspInvoice.getTotal().add(record.getAmount())); // we have this property but weren't using it...
                Integer quantity = record.getQuantity();
                //we set the quantity to zero in case it's the first record through the loop
                if(!AZURE_CHARGE_TYPE_CYCLE_FEE.equalsIgnoreCase(record.getChargeType()) && !includeDetail) {
                	quantity = 0;
                }
                Date subscriptionStartDate = azureZuluDateTimeFormat.withZone(DateTimeZone.UTC).parseDateTime(record.getSubscriptionStartDate()).toDate();
                Date subscriptionEndDate = azureZuluDateTimeFormat.withZone(DateTimeZone.UTC).parseDateTime(record.getSubscriptionEndDate()).toDate();
                Date chargeStartDate = azureZuluDateTimeFormat.withZone(DateTimeZone.UTC).parseDateTime(record.getChargeStartDate()).toDate();
                Date chargeEndDate = azureZuluDateTimeFormat.withZone(DateTimeZone.UTC).parseDateTime(record.getChargeEndDate()).toDate();
                CSPLicenseBillingLineItem newLineItem = new CSPLicenseBillingLineItem(record.getSyndicationPartnerSubscriptionNumber(), record.getChargeType(), record.getOfferName(), quantity, record.getUnitPrice(), record.getAmount(), record.getSubscriptionId(), record.getDurableOfferId(), record.getOfferName(), record.getBillingCycleType(), record.getTax(), subscriptionStartDate, subscriptionEndDate, chargeStartDate, chargeEndDate);
                
                if(includeDetail) {
                	cspInvoice.getLineItems().add(newLineItem);
                } else {
                	if (cspInvoice.getLineItems().contains(newLineItem)) {
                        for (CSPLicenseBillingLineItem lineItem : cspInvoice.getLineItems()) {
                            if (lineItem.equals(newLineItem)) {
                                if(AZURE_CHARGE_TYPE_CYCLE_FEE.equalsIgnoreCase(record.getChargeType())) {
                                    lineItem.setQuantity(lineItem.getQuantity() + newLineItem.getQuantity());
                                }
                                lineItem.setTotal(lineItem.getTotal().add(newLineItem.getTotal()));
                                break;
                            }
                        }
                    } else {
                        cspInvoice.getLineItems().add(newLineItem);
                    }
                }
                
            } catch (Exception any) {
                log.error("Exception inside Azure line item loop, skipping record", any);
            }
        }
        
        return cspInvoices;
    }
    
    private List<CSPOneTimeBillingWrapper> getReportInvoiceOneTimeLineItems(PCClient client, String invoiceId, String billingProvider, String invoiceLineitemType) throws ServiceException {
        PCPath wrapperPath = invoiceLineitems(invoiceId, billingProvider, invoiceLineitemType);
        OneTimeBasedLineItemWrapper wrapper = client.get(wrapperPath, OneTimeBasedLineItemWrapper.class, new String[]{"size=1000", "offset=0"});
        //assertNotNull(wrapper);
        List<OneTimeBasedLineItem> onetimeLineItems = wrapper.getItems();
        while (wrapper.getLinks() != null && wrapper.getLinks().getNext() != null) {
            Link next = wrapper.getLinks().getNext();
            String continuationToken = null;
            for (KeyValuePair header : next.getHeaders()) {
                if ("MS-ContinuationToken".equals(header.getKey())) {
                    continuationToken = header.getValue();
                }
            }
            wrapperPath = nextPath(next.getUri(), continuationToken, wrapperPath);
            wrapper = client.get(wrapperPath, OneTimeBasedLineItemWrapper.class, null);
            onetimeLineItems.addAll(wrapper.getItems());
        }
        Collections.sort(onetimeLineItems, new OneTimeBasedLineItemComparator());
        
        List<CSPOneTimeBillingWrapper> cspInvoices = new ArrayList<CSPOneTimeBillingWrapper>();
        for (OneTimeBasedLineItem record : onetimeLineItems) {
            try {
                if (record.getCustomerId() == null || record.getSubscriptionId() == null) {
                    continue;
                }
                CSPOneTimeBillingWrapper cspInvoice = new CSPOneTimeBillingWrapper(record.getCustomerId(), null, record.getCustomerName());
                int index = cspInvoices.indexOf(cspInvoice);
                if (index > -1) {
                    cspInvoice = cspInvoices.get(index);
                } else {
                    cspInvoices.add(cspInvoice);
                }
                /** I think we should just ignore records with zero totals...
                 * they don't show up in the invoice summary
                 */
                if (record.getTotalForCustomer() != null && record.getTotalForCustomer().compareTo(BigDecimal.ZERO) > 0) {
                    cspInvoice.setTotal(cspInvoice.getTotal().add(record.getTotalForCustomer())); // we have this property but weren't using it...
                    CSPOneTimeBillingLineItem newLineItem = new CSPOneTimeBillingLineItem(record.getSubscriptionId(), record.getChargeType(), record.getProductName(), record.getSubscriptionDescription(), record.getQuantity(), record.getUnitPrice(), record.getTotalForCustomer(), record.getTermAndBillingCycle());
                    index = cspInvoice.getLineItems().indexOf(newLineItem);
                    if (index > -1) {
                        CSPOneTimeBillingLineItem lineItem = cspInvoice.getLineItems().get(index);
                        lineItem.setQuantity(lineItem.getQuantity() + newLineItem.getQuantity());
                        lineItem.setSubscriptionTotal(lineItem.getSubscriptionTotal().add(newLineItem.getSubscriptionTotal()));
                    } else {
                        cspInvoice.getLineItems().add(newLineItem);
                    }
                }
            } catch (Exception any) {
                log.error("Exception inside Azure line item loop, skipping record", any);
            }
        }
        return cspInvoices;
    }
    
    /*
    @Override
    //@Scheduled(cron = "0 0 3 1 * *") // how about third hour of first day of the month
    public void syncCustomerSubscriptions() {
    	syncCustomerSubscriptions(null, null);
    }
    
    @Async
    @Override
    public void syncCustomerSubscriptions(DateTime startDate, DateTime endDate) {
    	try {
	    	ScheduledTask st = applicationDataDaoService.findScheduledTaskByCode(AZURE_MONTHLY_BILLING_SYNC_TSK);
	        if (st != null && st.getEnabled()) {
	            log.info("Running Task: " + st.getName());
    	
		    	if (startDate == null) {
		            startDate = new DateTime()
		                    .withZone(DateTimeZone.forID(TZID))
		                    .withDayOfMonth(1)
		                    .minusMonths(1)
		                    .withTimeAtStartOfDay();
		        }
		        if (endDate == null) {
		            endDate = startDate
		            		.plusMonths(1);
		                    //.dayOfMonth()
		                    //.withMaximumValue()
		                    //.plusHours(23).plusMinutes(59).plusSeconds(59);
		        }
		        
		        log.info("sync-ing Customer Azure subscriptions for period {} to {}..", new Object[]{DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ssZ").print(startDate), DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ssZ").print(endDate)});
		        
		        Map<Long, String> results = new HashMap<Long, String>();
		        List<ContractServiceAzure> csas = contractDaoService.contractServiceAzures(null);
		        if (csas != null && !csas.isEmpty()) {
                            BigDecimal totalRawSum = BigDecimal.ZERO;
                            BigDecimal totalSum = BigDecimal.ZERO;
		            for (ContractServiceAzure csa : csas) {
		            	try {
		            		AzureBillingPeriodWrapper invoice = generateMonthlyBillingForAzureSubscription(csa, startDate, endDate, Boolean.TRUE);
		            		if(invoice != null) {
		            			results.put(csa.getId(), "Service succesfully added for customer in the amount of: " + invoice.getTotal() + " with CSA ID [" + csa.getId() + "]");
                                                totalRawSum = totalRawSum.add(invoice.getRawTotal());
                                                totalSum = totalSum.add(invoice.getTotal());
		            		}
		            	} catch (Exception e) {
		                	e.printStackTrace();
		                	results.put(csa.getId(), "Service threw exception for customer with CSA ID [" + csa.getId() + "]");
		                }
		            }
                            log.info("Raw Total: ${}", totalRawSum.setScale(4, RoundingMode.HALF_UP).toPlainString());
                            log.info("Total w/uplift: ${}", totalSum.setScale(4, RoundingMode.HALF_UP).toPlainString());
		        } else {
		            log.info("no subscriptions found to sync :(");
		        }
		        
		        //send email
		        sendMonthlyAzureBillingSummary(azureEmailAlertList, results);
		        log.info("Ending Task: " + st.getName());
	        }
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    public AzureBillingPeriodWrapper generateMonthlyBillingForAzureSubscription(ContractServiceAzure csa, DateTime startDate, DateTime endDate, Boolean persistContractService) throws ServiceException {
    	AzureBillingPeriodWrapper invoice = null;
    	
    	if(csa.getSubscriptionId() != null && csa.getCustomerId() != null) {
    		PCClient client = new PCClient(pcContext, pcDomain, pcTenantId, pcWebappId, pcWebappKey, null);
	    	try {
	            Contract contract = contractDaoService.contract(csa.getContractId());
	            Customer customer = contractDaoService.customer(contract.getCustomerId());
	            log.info("Customer: {}, Contract Id {}, Contract Name {}", new Object[]{customer.getName(), contract.getId(), contract.getName()});
	        } catch(Exception any) {
	            log.info("caught exception getting contract and customer info for an Azure subscription...", any.getMessage());
	        }
	    	
	        Subscription subscription = client.get(customerSubscriptionById(csa.getCustomerId(), csa.getSubscriptionId()), Subscription.class, new String[]{});
	        if (subscription.getBillingType() != null && "usage".equals(subscription.getBillingType())) {
	        	invoice = computeAzureResourceMonthlyUtilization(client, csa, subscription, startDate, endDate, persistContractService);
	        }
    	}
    	
        return invoice;
    }

    private AzureBillingPeriodWrapper computeAzureResourceMonthlyUtilization(PCClient client, ContractServiceAzure csa, Subscription subscription, DateTime startDate, DateTime endDate, Boolean persistContractService) throws ServiceException {
        AzureBillingPeriodWrapper invoice = new AzureBillingPeriodWrapper(subscription.getId(), csa.getCustomerId(), startDate.toDate(), endDate.toDate());
        
        PCPath wrapperPath = customerSubscriptionUtilizations(csa.getCustomerId(), subscription.getId());
    	AzureUtilizationRecordWrapper wrapper = client.get(wrapperPath, AzureUtilizationRecordWrapper.class,
                new String[]{"start_time=" + azureUsageDateTimeFormat.print(startDate), "end_time=" + azureUsageDateTimeFormat.print(endDate), "granularity=daily", "show_details=true", "size=1000"});
        int counter = 1;
        List<AzureUtilizationRecord> utilizationRecords = wrapper.getItems();
        while (wrapper.getLinks() != null && wrapper.getLinks().getNext() != null) {
            Link next = wrapper.getLinks().getNext();
            String continuationToken = null;
            for (KeyValuePair header : next.getHeaders()) {
                if ("MS-ContinuationToken".equals(header.getKey())) {
                    continuationToken = header.getValue();
                }
            }
            wrapperPath = nextPath(next.getUri(), continuationToken, wrapperPath);
            wrapper = client.get(wrapperPath, AzureUtilizationRecordWrapper.class, null);
            utilizationRecords.addAll(wrapper.getItems());
        }
        Collections.sort(utilizationRecords, new AzureUtilizationRecordComparator());
    	
        Boolean discounting = Boolean.FALSE;
        BigDecimal total = BigDecimal.ZERO;
        Map<String, Map<String, Object>> meters = new HashMap<String, Map<String, Object>>();
        Map<String, BigDecimal> lineItemTotalAmounts = new HashMap<String, BigDecimal>();
        Map<String, BigDecimal> lineItemTotalQuantities = new HashMap<String, BigDecimal>();
        
        for (AzureUtilizationRecord record : utilizationRecords) {
            if (record.getQuantity().compareTo(BigDecimal.ZERO) > 0) {
                Map<String, Object> meterMap = null;
                AzureResource resource = record.getResource();
                log.debug("{}, id: {}, quantity: {}, units: {}, usage start: {}", new Object[]{resource.getName(), resource.getId(), record.getQuantity(), record.getUnit(), record.getUsageStartTime()});
                if (meters.containsKey(resource.getId())) {
                    meterMap = meters.get(resource.getId());
                } else {
                    AzureMeter meter = applicationDataDaoService.azureMeter(resource.getId());
                    meterMap = new HashMap<String, Object>();
                    Set<String> keys = meter.getRates().keySet();
                    // have to xform the rate map into a sorted map for rate logic to work
                    Map<BigDecimal, BigDecimal> sortedRates = new TreeMap<BigDecimal, BigDecimal>();
                    for (String key : keys) {
                        sortedRates.put(new BigDecimal(key), meter.getRates().get(key));
                    }
                    meterMap.put("meter", meter);
                    meterMap.put("rates", sortedRates);
                    meters.put(resource.getId(), meterMap);
                }
                if (meterMap != null) {
                    AzureMeter meter = (AzureMeter) meterMap.get("meter");
                    BigDecimal adjustedQuantity = record.getQuantity().subtract(meter.getIncludedQuantity());
                    if (adjustedQuantity.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal appliedRate = BigDecimal.ZERO;
                        Map<BigDecimal, BigDecimal> sortedRates = (Map<BigDecimal, BigDecimal>) meterMap.get("rates");
                        for (Map.Entry<BigDecimal, BigDecimal> entry : sortedRates.entrySet()) {
                            log.debug("\t[{}, ${}]", new Object[]{entry.getKey(), entry.getValue().setScale(4, RoundingMode.HALF_UP).toPlainString()});
                            if (adjustedQuantity.compareTo(entry.getKey()) > 0) {
                                appliedRate = entry.getValue();
                            }
                        }
                        log.debug("\tusing adjusted quantity {} and applied rate ${}", new Object[]{adjustedQuantity.setScale(4, RoundingMode.HALF_UP).toPlainString(), appliedRate.setScale(4, RoundingMode.HALF_UP).toPlainString()});
                        if (discounting) {
                            BigDecimal totalDiscount = applicationDataDaoService.azureOfferTermsSum(
                                    azureDefaultLocale, azureDefaultCurrency, meter.getId(), record.getUsageStartTime());
                            if (totalDiscount != null && totalDiscount.compareTo(BigDecimal.ZERO) > 0) {
                                log.debug("\tusing discount {}%", new Object[]{totalDiscount.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP).toPlainString()});
                                log.debug("\ttotal incremented by ${}", new Object[]{appliedRate.multiply(adjustedQuantity).multiply(BigDecimal.ONE.subtract(totalDiscount)).setScale(4, RoundingMode.HALF_UP).toPlainString()});
                                total = total.add(appliedRate.multiply(adjustedQuantity).multiply(BigDecimal.ONE.subtract(totalDiscount)));
                            } else {
                                log.debug("\ttotal incremented by ${}", new Object[]{appliedRate.multiply(adjustedQuantity).setScale(4, RoundingMode.HALF_UP).toPlainString()});
                                total = total.add(appliedRate.multiply(adjustedQuantity));
                            }
                        } else {
                            total = total.add(appliedRate.multiply(adjustedQuantity));
                        }
                        
                        //Summing up lineitem totals
                        String key = meter.getId();
                        BigDecimal newTotal = new BigDecimal(0);
                        BigDecimal newTotalQuantity = new BigDecimal(0);
                        log.info("Key: " + lineItemTotalAmounts.get(key));
                        if(lineItemTotalAmounts.containsKey(key)) {
                        	newTotal = lineItemTotalAmounts.get(key);
                        	newTotal = newTotal.add(appliedRate.multiply(adjustedQuantity));
                        	lineItemTotalAmounts.put(key, newTotal);
                        } else {
                        	newTotal = newTotal.add(appliedRate.multiply(adjustedQuantity));
                        	lineItemTotalAmounts.put(key, newTotal);
                        }
                        
                        if(lineItemTotalQuantities.containsKey(key)) {
                        	newTotalQuantity = lineItemTotalQuantities.get(key);
                        	newTotalQuantity = newTotalQuantity.add(adjustedQuantity);
                        	lineItemTotalQuantities.put(key, newTotalQuantity);
                        } else {
                        	newTotalQuantity = newTotalQuantity.add(adjustedQuantity);
                        	lineItemTotalQuantities.put(key, newTotalQuantity);
                        }
                        log.debug("\tadding to map {} and with amount ${}", new Object[]{key, newTotal.setScale(4, RoundingMode.HALF_UP).toPlainString()});
                    } else {
                        log.debug("\tadjusted quantity <= 0...");
                    }
                } else {
                    log.warn("meter not found for id {}", new Object[]{resource.getId()});
                }
            }
        }
        
        //for debug only -- can remove this or comment it out
        List<AzureBillingPeriodLineItem> lineItems = new ArrayList<AzureBillingPeriodLineItem>();
        Iterator it = lineItemTotalAmounts.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            
            String id = (String) pair.getKey();
            BigDecimal lineItemTotal = (BigDecimal) pair.getValue();
            String name = "";
            for (AzureUtilizationRecord record : utilizationRecords) {
                    AzureResource resource = record.getResource();
                    if(resource.getId().equals(pair.getKey())) name = resource.getName();
            }
            BigDecimal lineItemTotalQuantity = lineItemTotalQuantities.get(pair.getKey());
            log.debug(name + " [" + id + " ], Total $$$: " + lineItemTotal + "], Total Quantity: " + lineItemTotalQuantity);
            AzureBillingPeriodLineItem lineItem = new AzureBillingPeriodLineItem(id, name, lineItemTotalQuantity, lineItemTotal);
            lineItems.add(lineItem);
            
            it.remove();
        }
        log.debug("generating a monthly Contract Service object for subscription [{}], with latest total: [${}]",
                new Object[]{subscription.getFriendlyName(), total.setScale(4, RoundingMode.HALF_UP)});
        
        //Add Product Margins
        BigDecimal totalPrice = total;
        try {
	        AzureUplift productUplift = applicationDataDaoService.azureUpliftByCode(AZURE_UPLIFT_PRODUCT_MARGIN);
	        if(productUplift != null) {
	        	if(AzureUplift.UpliftType.percentage.equals(productUplift.getUpliftType())) {
	        		BigDecimal productPriceUplift = total.multiply(productUplift.getUplift());
	        		totalPrice = productPriceUplift.add(totalPrice);
		        	log.debug("Adding [{}%] product uplift [${}], new total is: [${}]", new Object[]{new BigDecimal(100).multiply(productUplift.getUplift()), productPriceUplift.setScale(4, RoundingMode.HALF_UP), totalPrice.setScale(4, RoundingMode.HALF_UP)});
		        	AzureBillingPeriodLineItem lineItem = new AzureBillingPeriodLineItem(AZURE_UPLIFT_PRODUCT_MARGIN, messageSource.getMessage("ui_label_product_margin", null, LocaleContextHolder.getLocale()), new BigDecimal(1), productPriceUplift);
		            lineItems.add(lineItem);
	        	}
	        }
	        
	        AzureUplift supportUplift = applicationDataDaoService.azureUpliftByCode(AZURE_UPLIFT_SUPPORT_MARGIN);
	        if(supportUplift != null) {
	        	if(AzureUplift.UpliftType.percentage.equals(supportUplift.getUpliftType())) {
	        		BigDecimal supportPriceUplift = total.multiply(supportUplift.getUplift());
	        		totalPrice = supportPriceUplift.add(totalPrice);
	        		log.debug("Adding [{}%] support uplift [${}], new total is: [${}]", new Object[]{new BigDecimal(100).multiply(supportUplift.getUplift()), supportPriceUplift.setScale(4, RoundingMode.HALF_UP), totalPrice.setScale(4, RoundingMode.HALF_UP)});
	        		AzureBillingPeriodLineItem lineItem = new AzureBillingPeriodLineItem(AZURE_UPLIFT_SUPPORT_MARGIN, messageSource.getMessage("ui_label_support_margin", null, LocaleContextHolder.getLocale()), new BigDecimal(1), supportPriceUplift);
		            lineItems.add(lineItem);
	        	}
	        }
        } catch (Exception e) {
        	log.info("Margins not added because no active ones were found.");
        }
        
        //Create Service
        Service existingService = contractDaoService.contractServiceByContractServiceAzureAndStartDate(csa.getId(), startDate.toDate());
        Service subscriptionService = null;
        
        //we only persist if requested too, as this method can also be used just to read the azure monthly results
        if(persistContractService) {
	        //we only want to create the record if there is not already one for that month
	        if(existingService == null) {
	        	//we also only want to create a record if it has a dollar amount?
		        if (totalPrice.compareTo(BigDecimal.ZERO) > 0) {
		            log.debug("generating a monthly Contract Service object for subscription [{}], with latest total: [${}]",
		                    new Object[]{subscription.getFriendlyName(), total.setScale(2, RoundingMode.HALF_UP)});
		            subscriptionService = new Service();
		            subscriptionService.setContractServiceAzureId(csa.getId());
		            subscriptionService.setServiceId(csa.getServiceId());
		            subscriptionService.setContractId(csa.getContractId());
		            subscriptionService.setDeviceId(csa.getDeviceId());
		            subscriptionService.setStartDate(startDate.toDate());
		            subscriptionService.setEndDate(endDate.toDate());
		            subscriptionService.setOnetimeRevenue(totalPrice);
		            subscriptionService.setStatus(Service.Status.active);
		            subscriptionService.setName(csa.getName());
		            
		            String note = "This record was automatically created as part of the automated Azure billing. This is for the service period of " + startDate.toString() + " to " + endDate.toString() + ".";
		            subscriptionService.setNote(note);
		            
		            try {
		                contractDaoService.saveContractService(subscriptionService, Boolean.FALSE);
		            } catch (ServiceException ex) {
		                Logger.getLogger(ServiceUsageServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
		            }
		        } else {
		            log.debug("skipping subscription [{}] with $0 total for month", new Object[]{subscription.getFriendlyName()});
		        }
	        } else {
	        	log.info("Contract Service Record Already Exists for this month & subcription ID");
	        }
        }
        
        invoice.setLineItems(lineItems);
        invoice.setRawTotal(total);
        invoice.setTotal(totalPrice);
        
        return invoice;
    }
    */
    
    private void sendMonthlyM365BillingSummary(final String email, Map<String, String> results, final DateTime billingDate) throws ServiceException {
        try {
            final String templateLocation = getTemplateLocation("m365_billing_summary", LocaleContextHolder.getLocale());
            final Map<String, String> jobResults = results;
            MimeMessagePreparator preparator = new MimeMessagePreparator() {
                public void prepare(MimeMessage mimeMessage) throws Exception {
                    MimeMessageHelper message = new MimeMessageHelper(mimeMessage, Boolean.TRUE);
                    message.setTo(email);
                    message.setFrom(noReplyEmail, "Service Insight");
                    message.setReplyTo(noReplyEmail, "Service Insight");
                    message.setSubject("Monthly Service Insight Auto-Billing Summary");
                    Map model = new HashMap();
                    model.put("billingDate", DateTimeFormat.forPattern("yyyy-MM-dd").print(billingDate));
                    model.put("results", jobResults);
                    String text = VelocityEngineUtils.mergeTemplateIntoString(
                            velocityEngine, templateLocation, mailEncoding, model);
                    message.setText(text, true);
                    message.addInline("logo", new ClassPathResource(String.format("%s/%s", baseTemplateLocation, emailLogo)));
                }
            };
            mailSender.send(preparator);
        } catch (Exception ex) {
            log.warn("Exception thrown sending email", ex);
            throw new ServiceException("Error sending monthly Azure billing email", ex);
        }
    }
    
    private void sendMonthlyAzureBillingSummary(final String email, Map<String, String> results, final DateTime billingDate) throws ServiceException {
        try {
            final String templateLocation = getTemplateLocation("monthly_billing_summary", LocaleContextHolder.getLocale());
            final Map<String, String> jobResults = results;
            MimeMessagePreparator preparator = new MimeMessagePreparator() {
                public void prepare(MimeMessage mimeMessage) throws Exception {
                    MimeMessageHelper message = new MimeMessageHelper(mimeMessage, Boolean.TRUE);
                    message.setTo(email);
                    message.setFrom(noReplyEmail, "Service Insight");
                    message.setReplyTo(noReplyEmail, "Service Insight");
                    message.setSubject("Monthly Service Insight Auto-Billing Summary");
                    Map model = new HashMap();
                    model.put("billingDate", DateTimeFormat.forPattern("yyyy-MM-dd").print(billingDate));
                    model.put("results", jobResults);
                    String text = VelocityEngineUtils.mergeTemplateIntoString(
                            velocityEngine, templateLocation, mailEncoding, model);
                    message.setText(text, true);
                    message.addInline("logo", new ClassPathResource(String.format("%s/%s", baseTemplateLocation, emailLogo)));
                }
            };
            mailSender.send(preparator);
        } catch (Exception ex) {
            log.warn("Exception thrown sending email", ex);
            throw new ServiceException("Error sending monthly Azure billing email", ex);
        }
    }
    
    private void reportAzureOnlyCustomerNames(final String email, final List<String> azureCustomers, final String dateString) throws ServiceException {
        try {
            final String templateLocation = getTemplateLocation("azure_only_customers", LocaleContextHolder.getLocale());
            MimeMessagePreparator preparator = new MimeMessagePreparator() {
                public void prepare(MimeMessage mimeMessage) throws Exception {
                    MimeMessageHelper message = new MimeMessageHelper(mimeMessage, Boolean.TRUE);
                    message.setTo(email);
                    message.setFrom(noReplyEmail, "Service Insight");
                    message.setReplyTo(noReplyEmail, "Service Insight");
                    message.setSubject("Unmatched Azure Customer Names for " + dateString);
                    Map model = new HashMap();
                    model.put("results", azureCustomers);
                    String rerunUrl = UriComponentsBuilder.fromHttpUrl(httpHost)
                            .path("/services/azure/costs/import")
                            .queryParam("invd", dateString)
                            .build().toUriString();
                    model.put("rerunUrl", rerunUrl);
                    model.put("dateString", dateString);
                    String text = VelocityEngineUtils.mergeTemplateIntoString(
                            velocityEngine, templateLocation, mailEncoding, model);
                    message.setText(text, true);
                    message.addInline("logo", new ClassPathResource(String.format("%s/%s", baseTemplateLocation, emailLogo)));
                }
            };
            mailSender.send(preparator);
        } catch (UsernameNotFoundException unfe) {
            throw new ServiceException(messageSource.getMessage("security_username_not_found", new Object[]{email}, LocaleContextHolder.getLocale()), unfe);
        } catch (Exception ex) {
            log.error("whoops... general error", ex);
            throw new ServiceException(messageSource.getMessage("password_reset_exception", new Object[]{email}, LocaleContextHolder.getLocale()), ex);
        }
    }
    
//    @Scheduled(initialDelay = 60*1000, fixedRate = 5*60*1000) // for testing...
    @Scheduled(cron = "0 0 * * * *") // run once, every hour, on the hour
    public void serviceConnect() {
        try {
            ScheduledTask st = applicationDataDaoService.findScheduledTaskByCode(OSP_SERVICE_SYNC_TSK);
            if (st != null && st.getEnabled()) {
                log.info("Running Task: " + st.getName());
                DateTime batchTime = new DateTime().withZone(DateTimeZone.forID(TZID));
                ResponseEntity<List<ServiceMin>> response = RestClient.getListOfOspServices(
                        ospAPIUser, ospAPIPassword, ospAPIMinlist);
                List<Object[]> inserts = new ArrayList<Object[]>();
                List<Object[]> svcIdsForUpdate = new ArrayList<Object[]>();
                for (ServiceMin item : response.getBody()) {
                	if(StringUtils.isEmpty(item.getBusinessModelIdentifier()) || item.getBusinessModelIdentifier() == null) {
                		log.info("Service: " + item);
                	}
                    /**
                     * item is a "current version" Service that is published in
                     * the region passed in
                     */
                    if (item.getDocId() != null) {
                        Map<String, Object> params = new HashMap<String, Object>();
                        params.put("ospId", item.getId());
                        List<Map<String, Object>> results = namedJdbcTemplate.queryForList("select * from service where osp_id = :ospId", params);
                        boolean versionFound = false;
                        boolean currentDisabledValue = false;
                        for (Map<String, Object> svc : results) {
                            //log.debug("found SI services matching OSP Service with ID: [{}]", item.getId());
                            /**
                             * svc is a Service that matched the osp id - if it
                             * is the same version, it's 'active' field needs to
                             * be TRUE - if it is not the same version, that
                             * version 'active' field needs to be set to FALSE
                             */
                            Double svcVersion = ((BigDecimal) svc.get("version")).doubleValue();
                            if (item.getVersionId().equals(VersionUtil.formatVersion(svcVersion))) {
                                versionFound = true;
                                if (!((Boolean) svc.get("active"))) {
                                    svcIdsForUpdate.add(new Object[]{Boolean.TRUE, batchTime.toDate(), "system", svc.get("id")});
                                }
                            } else if ((Boolean) svc.get("active")) {
                                currentDisabledValue = (Boolean) svc.get("disabled"); // used below to OVERRIDE the incoming default disabled value
                                svcIdsForUpdate.add(new Object[]{Boolean.FALSE, batchTime.toDate(), "system", svc.get("id")});
                            }
                        }
                        // at this point, if svc and version(s) existed for an id, IF the versionId was found,
                        // then active state has been properly set for all svc version(s)
                        if (!versionFound) {
                            // either svc didn't even exist or the version was not found
                            inserts.add(new Object[]{item.getId(), VersionUtil.parseVersion(item.getVersionId()), item.getName(),"description unavailable",
                                item.getSIBusinessModel(), Boolean.TRUE, (results.isEmpty()? item.getSIDisabled() : currentDisabledValue), batchTime.toDate()});
                        }
                    }
                }
                List<Map<String, Object>> results = jdbcTemplate.queryForList("select * from service"
                        + " where osp_id < 90000 and active = true order by osp_id, version, id"); // note: 90k range is for special SI only services...
                for (Map<String, Object> svc : results) {
                    Long svcId = (Long) svc.get("osp_id");
                    Boolean svcFound = false;
                    for (ServiceMin item : response.getBody()) {
                        if (item.getId().equals(svcId)) {
                            svcFound = true;
                            break;
                        }
                    }
                    if (!svcFound) {
                        // this service exists in Service Insight but wasn't passed by OSP
                        log.debug("found an SI Service with id [{}] that was not in the OSP list at all...", svcId);
                        svcIdsForUpdate.add(new Object[]{Boolean.FALSE, batchTime.toDate(), "system", svc.get("id")});
                    }
                }
                if (!inserts.isEmpty()) {
                    log.debug("batch inserting [{}] service/version(s) to reflect correct data from Service Portfolio", inserts.size());
                    jdbcTemplate.batchUpdate("insert into service (osp_id, version, name, description, business_model, active, disabled, created) values (?, ?, ?, ?, ?, ?, ?, ?)", inserts);
                }
                if (!svcIdsForUpdate.isEmpty()) {
                    log.debug("batch updating [{}] services to reflect correct status from Service Portfolio", svcIdsForUpdate.size());
                    jdbcTemplate.batchUpdate("update service set active = ?, updated = ?, updated_by = ? where id = ?", svcIdsForUpdate);
                }
                log.info("Ending Task: " + st.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    @Scheduled(cron = "0 0 7 2 * *") // how about seventh hour of 2nd day of the month
    public void syncAWSCustomerInvoices() {
    	syncAWSCustomerInvoices(null);
    }
    
    @Async
    @Override
    public void syncAWSCustomerInvoices(DateTime billingInvoiceDate) {
    	try {
	    	ScheduledTask st = applicationDataDaoService.findScheduledTaskByCode(AWS_MONTHLY_BILLING_SYNC_TSK);
	        if (st != null && st.getEnabled()) {
	            log.info("Running Task: " + st.getName());
    	
		    	if (billingInvoiceDate == null) {
		    		billingInvoiceDate = new DateTime()
				            .withZone(DateTimeZone.UTC)
			                .withTime(0,0,0,0)
			                .dayOfMonth()
			                .withMinimumValue()
			                .plusDays(1).minusMonths(1);
		        }
		    	
		    	List<ContractServiceSubscription> csas = contractDaoService.contractServiceSubscriptions(null, ContractServiceSubscription.SubscriptionType.aws);
		    	List<ContractServiceSubscription> activeCsas = new ArrayList<ContractServiceSubscription>();
		    	for(ContractServiceSubscription csa : csas) {
		    		DateTime startDate = new DateTime(csa.getStartDate()).withZone(DateTimeZone.UTC).withTime(0,0,0,0);
		    		DateTime endDate = new DateTime(csa.getEndDate()).withZone(DateTimeZone.UTC).withTime(0,0,0,0);
		    		if((startDate.isBefore(billingInvoiceDate) || startDate.isEqual(billingInvoiceDate)) && (endDate.isAfter(billingInvoiceDate) || endDate.isEqual(billingInvoiceDate))) {
		    			activeCsas.add(csa);
		    		}
		    	}
		    	List<AWSBillingPeriodWrapper> invoices = getInvoicesForAWSSubscription(activeCsas, billingInvoiceDate, Boolean.TRUE, Boolean.FALSE);
		    	Map<String, String> results = new HashMap<String, String>();
		        if (invoices != null && !invoices.isEmpty()) {
		            for (AWSBillingPeriodWrapper invoice : invoices) {
		            	results.put(invoice.getAccountId(), "AWS Service succesfully added for customer in the amount of: " + invoice.getTotal() + " for Account: [" + invoice.getAccountId() + "]");
		            }
		        } else {
		        	results.put("", "No invoices were returned from AWS.");
		        }
		    	
		    	//email matt with a billing status
		    	sendMonthlyAzureBillingSummary(azureEmailAlertList, results, billingInvoiceDate);
		    	
		    	log.info("Ending Task: " + st.getName());
	        }
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    @Override
    public List<AWSBillingPeriodWrapper> getInvoicesForAWSSubscription(List<ContractServiceSubscription> subs, DateTime billingInvoiceDate, Boolean persistContractService, Boolean includeLineItems) throws ServiceException {
    	List<AWSBillingPeriodWrapper> invoices = new ArrayList<AWSBillingPeriodWrapper>();
    	
    	try {
            RestTemplate awsclient = RestClient.authenticatingRestTemplate(awsclientUser, awsclientPassword);
    		for(ContractServiceSubscription sub : subs) {
    			AWSBillingPeriodWrapper invoice = getInvoiceForAWS(awsclient, sub, billingInvoiceDate, persistContractService, includeLineItems);
    			invoices.add(invoice);
    		}
        } catch(Exception any) {
            log.info("caught exception getting contract and customer info for an AWS subscription... {}", any.getMessage());
        }
    	
        return invoices;
    }
    
    private AWSBillingPeriodWrapper getInvoiceForAWS(RestTemplate awsclient, ContractServiceSubscription sub, DateTime billingInvoiceDate, Boolean persistContractService, Boolean includeLineItems) throws ServiceException {
    	AWSBillingPeriodWrapper wrapper = new AWSBillingPeriodWrapper();
        
    	String pathAndParams = "/costexplorer/costAndUsage?acct={acct}&month={month}";
    	AWSAccountCostAndUsage invoice = awsclient.getForObject(awsclientEndpoint + pathAndParams, AWSAccountCostAndUsage.class, new Object[]{sub.getSubscriptionId(), jodaAWSDateFmt.print(billingInvoiceDate)});
    	
    	List<AWSBillingPeriodLineItem> lineItems = new ArrayList<AWSBillingPeriodLineItem>();
    	
    	if(invoice != null) {
	    	BigDecimal rawTotal = invoice.getBlendedCost();
	    	
	    	if(rawTotal.compareTo(BigDecimal.ZERO) > 0) {
		    	BigDecimal totalPrice = rawTotal;
		    	
		    	if(includeLineItems) {
		    		String detailsPathAndParams = "/costexplorer/monthlyBillingDetails?acct={acct}&month={month}";
		        	Map invoiceLineItems = awsclient.getForObject(awsclientEndpoint + detailsPathAndParams, Map.class, new Object[]{sub.getSubscriptionId(), jodaAWSDateFmt.print(billingInvoiceDate)});
		        	Iterator it = invoiceLineItems.entrySet().iterator();
		            while (it.hasNext()) {
		                Map.Entry pair = (Map.Entry)it.next();
		                BigDecimal amount = new BigDecimal((double) pair.getValue());
		                if(amount.compareTo(BigDecimal.ZERO) > 0) {
		                	AWSBillingPeriodLineItem invoiceLineItem = new AWSBillingPeriodLineItem((String) pair.getKey(), amount);
		                	lineItems.add(invoiceLineItem);
		                }
		                it.remove();
		            }
		    	}
		        
		    	//Add Product Margins
                        String upliftLabel = messageSource.getMessage("ui_label_product_margin", null, LocaleContextHolder.getLocale());
		        try {
			        SubscriptionUplift awsUplift = null;
			        if(ContractServiceSubscription.CustomerType.netnew.equals(sub.getCustomerType())) {
			        	awsUplift = applicationDataDaoService.subscriptionUpliftByCode(AWS_UPLIFT_NEW_CUST_MARGIN);
			        } else if(ContractServiceSubscription.CustomerType.existing.equals(sub.getCustomerType())) {
			        	awsUplift = applicationDataDaoService.subscriptionUpliftByCode(AWS_UPLIFT_EXSTNG_CUST_MARGIN);
			        }
			        
			        if(awsUplift != null) {
			        	if(SubscriptionUplift.UpliftType.percentage.equals(awsUplift.getUpliftType())) {
			        		totalPrice = rawTotal.divide(new BigDecimal(1).subtract(awsUplift.getUplift()), 4, RoundingMode.HALF_UP);
			        		BigDecimal awsPriceUplift = totalPrice.subtract(rawTotal);
				        	log.debug("Adding [{}%] product uplift [${}], new total is: [${}]", new Object[]{new BigDecimal(100).multiply(awsUplift.getUplift()), awsPriceUplift.setScale(4, RoundingMode.HALF_UP), totalPrice.setScale(4, RoundingMode.HALF_UP)});
				        	AWSBillingPeriodLineItem upliftLineItem = new AWSBillingPeriodLineItem(upliftLabel, awsPriceUplift);
				        	lineItems.add(upliftLineItem);
			        	}
			        }
		        } catch (ServiceException e) {
		        	log.info("Margins not added because no active ones were found.");
		        } catch (Exception any) {
		        	log.info("An error occurred trying to add the margins: {}", any.getMessage());
		        }
		        
		    	//Create Service
		        billingInvoiceDate = billingInvoiceDate.withZone(DateTimeZone.forID(TZID));
		        Date serviceStartDate = billingInvoiceDate.dayOfMonth().withMinimumValue().toDate();
		        Date serviceEndDate = billingInvoiceDate.dayOfMonth().withMaximumValue().toDate();
		        Service existingService = contractDaoService.contractServiceByContractServiceSubscriptionAndStartDate(sub.getId(), serviceStartDate);
		        Service subscriptionService = null;
		        
		        //we only persist if requested too, as this method can also be used just to read the azure monthly results
		        if(persistContractService) {
			        //we only want to create the record if there is not already one for that month
			        if(existingService == null) {
			        	//we also only want to create a record if it has a dollar amount?
				        if (totalPrice.compareTo(BigDecimal.ZERO) > 0) {
				        	SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
				            log.debug("generating a monthly Contract Service object for subscription [{}], with latest total: [${}]",
				                    new Object[]{sub.getSubscriptionId(), totalPrice.setScale(2, RoundingMode.HALF_UP)});
				            subscriptionService = new Service();
				            subscriptionService.setContractServiceSubscriptionId(sub.getId());
				            subscriptionService.setServiceId(sub.getServiceId());
				            subscriptionService.setContractId(sub.getContractId());
				            subscriptionService.setDeviceId(sub.getDeviceId());
				            subscriptionService.setStartDate(serviceStartDate);
				            subscriptionService.setEndDate(serviceEndDate);
				            subscriptionService.setOnetimeRevenue(totalPrice);
				            subscriptionService.setStatus(Service.Status.active);
				            subscriptionService.setName(sub.getName());
				            
				            String note = "This record was automatically created as part of the automated AWS billing. This is for the account " + sub.getSubscriptionId() + " and the service period of " + sdf.format(serviceStartDate) + " to " + sdf.format(serviceEndDate) + ".";
				            subscriptionService.setNote(note);
				            
				            try {
				                contractDaoService.saveContractService(subscriptionService, Boolean.FALSE);
				            } catch (ServiceException ex) {
				                Logger.getLogger(ServiceUsageServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
				            }
				        } else {
				            log.debug("skipping subscription [{}] with $0 total for month", new Object[]{sub.getSubscriptionId()});
				        }
			        } else {
			        	log.info("Contract Service Record Already Exists for this month & subcription ID");
			        }
		        }
		        
		        wrapper.setAccountId(sub.getSubscriptionId());
	        	wrapper.setRawTotal(rawTotal);
	        	wrapper.setTotal(totalPrice);
	        	wrapper.setStartDate(serviceStartDate);
	        	wrapper.setEndDate(serviceEndDate);
		        if(includeLineItems) {
		        	wrapper.setLineItems(lineItems);
		        }
                        if (persistContractService && existingService == null &&
                                rawTotal.compareTo(BigDecimal.ZERO) > 0) {
                            Map<String, BigDecimal> costLineItems = new HashMap<String, BigDecimal>();
                            BigDecimal auditTotal = BigDecimal.ZERO;
                            /**
                             * disabled: creating "fine-grained" AWS Cost Items
                             * instead, we're just going to lump them all into one Cost Item
                             */
                            if (false) {
                                for (AWSBillingPeriodLineItem record : lineItems) {
                                    if (record.getTotal().compareTo(BigDecimal.ZERO) > 0) {
                                        auditTotal = auditTotal.add(record.getTotal());
                                        String key = "Unspecified Cost";
                                        if (StringUtils.isNotBlank(record.getName())) {
                                            if (record.getName().indexOf("/") > 0) {
                                                String[] parts = record.getName().split("/");
                                                key = parts[0];
                                            } else {
                                                key = record.getName();
                                            }
                                        }
                                        if (!upliftLabel.equals(key)) {
                                            costLineItems.put(key, (costLineItems.get(key) == null
                                                    ? record.getTotal()
                                                    : costLineItems.get(key).add(record.getTotal())
                                                    ));
                                        }
                                    }
                                }
                                log.debug("Creating Cost Items for AWS service charges (wrapper total: ${}, audit total: ${}):",
                                        new Object[]{rawTotal.setScale(2, RoundingMode.HALF_UP).toPlainString(), auditTotal.setScale(2, RoundingMode.HALF_UP).toPlainString()});
                            } else {
                                log.debug("Creating SINGLE Combined Cost Item for ALL AWS service charges (wrapper total: ${}):",
                                        new Object[]{rawTotal.setScale(2, RoundingMode.HALF_UP).toPlainString()});
                                String awsCostTitle = messageSource.getMessage("azure_combined_cost_title", null, LocaleContextHolder.getLocale());
                                costLineItems.put(awsCostTitle, rawTotal);
                            }
                            for (Map.Entry<String, BigDecimal> entry : costLineItems.entrySet()) {
                                try {
                                    Contract contract = contractDaoService.contract(subscriptionService.getContractId());
                                    Customer customer = contractDaoService.customer(contract.getCustomerId());
                                    String expenseRecordName = "Automated AWS Expense: " + entry.getKey();
                                    String expenseRecordDescription = "for Azure Customer: " + customer.getName();
                                    Expense expenseRecord = new Expense(null, null, Expense.ExpenseType.cost, expenseRecordName, null, entry.getValue(),
                                            1, customer.getId(), null, null);
                                    expenseRecord.setDescription(expenseRecordDescription);
                                    expenseRecord.setCreatedBy("system");
                                    CostItem awsCostItem = new CostItem();
                                    awsCostItem.setExpense(expenseRecord);
                                    awsCostItem.setAwsSubscriptionNo(sub.getSubscriptionId());
                                    awsCostItem.setAmount(entry.getValue());
                                    awsCostItem.setQuantity(1);
                                    awsCostItem.setApplied(serviceStartDate);
                                    awsCostItem.setContractId(contract.getId());
                                    awsCostItem.setCustomerId(customer.getId());
                                    awsCostItem.setCostType(CostItem.CostType.aws);
                                    awsCostItem.setCreated(billingInvoiceDate.withZone(DateTimeZone.forID(TZID)).toDate());
                                    String awsCostItemName = "Automated AWS Cost: " + entry.getKey();
                                    awsCostItem.setName(awsCostItemName);
                                    CostFraction awsCostFraction = new CostFraction();
                                    awsCostFraction.setExpenseCategory(costDaoService.expenseCategoryByName(AWS_EXPENSE_CATEGORY_NAME, PUBLIC_CLOUD_EXPENSE_CATEGORY_NAME));
                                    awsCostFraction.setFraction(new BigDecimal(100));
                                    awsCostItem.addCostFraction(awsCostFraction);
                                    awsCostItem.setCreatedBy("system");
                                    Long awsCostItemId = contractDaoService.saveCostItem(awsCostItem);
                                } catch (Exception any) {
                                    log.warn("Failed to create a cost for AWS Expense", any);
                                }
                            }
                        }
	    	}
        }
    	
    	return wrapper;
    }
}
