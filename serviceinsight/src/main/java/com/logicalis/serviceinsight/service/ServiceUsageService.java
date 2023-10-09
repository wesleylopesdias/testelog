package com.logicalis.serviceinsight.service;

import com.logicalis.serviceinsight.data.ContractServiceSubscription;
import com.logicalis.serviceinsight.data.MonthlyBillingInfo;
import com.logicalis.serviceinsight.representation.AWSBillingPeriodWrapper;
import com.logicalis.serviceinsight.representation.AWSInvoiceReportWrapper;
import com.logicalis.serviceinsight.representation.AzureBillingPeriodWrapper;
import com.logicalis.serviceinsight.representation.AzureInvoiceReportWrapper;
import com.logicalis.serviceinsight.representation.CSPOneTimeBillingWrapper;

import java.util.List;

import org.joda.time.DateTime;

/**
 * encapsulates usage oriented methods for services
 * 
 * @author poneil
 */
public interface ServiceUsageService extends BaseService {
    public List<MonthlyBillingInfo> monthlyBilling(Long customerId, Integer month, Integer year);
    //public AzureBillingPeriodWrapper generateMonthlyBillingForAzureSubscription(ContractServiceAzure csa, DateTime startDate, DateTime endDate, Boolean persistContractService) throws ServiceException;
    public void updateRateCard();
    //public void syncCustomerSubscriptions();
    //public void syncCustomerSubscriptions(DateTime startDate, DateTime endDate);
    public void serviceConnect();
    
    public void syncAzureCustomerInvoices();
    public void syncAzureCustomerInvoices(DateTime billingInvoiceDate);
    public List<AzureBillingPeriodWrapper> getInvoicesForAzureSubscription(List<ContractServiceSubscription> csas, DateTime billingInvoiceDate, Boolean persistContractService) throws ServiceException;
    public List<CSPOneTimeBillingWrapper> getInvoicesForAzureOneTime(List<ContractServiceSubscription> csas, DateTime billingInvoiceDate, Boolean persistContractService) throws ServiceException;
    public void syncOffice365Invoices() throws ServiceException;
    public void syncOffice365InvoicesForMonth(DateTime billingInvoiceDate) throws ServiceException;
    
    public List<AzureInvoiceReportWrapper> getAzureInvoiceReportData(DateTime billingInvoiceDate) throws ServiceException;
    public AWSInvoiceReportWrapper getAWSInvoiceReportData(DateTime billingInvoiceDate, Boolean includeBillingDetails) throws ServiceException;
    
    public void syncAzureCustomerCosts();
    public void syncAzureCustomerCosts(DateTime billingInvoiceDate);
    public void syncAWSCustomerInvoices();
    public void syncAWSCustomerInvoices(DateTime billingInvoiceDate);
    public List<AWSBillingPeriodWrapper> getInvoicesForAWSSubscription(List<ContractServiceSubscription> csas, DateTime billingInvoiceDate, Boolean persistContractService, Boolean includeLineItems) throws ServiceException;
}
