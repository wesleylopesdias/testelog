package com.logicalis.serviceinsight.representation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AzureInvoiceReportWrapper {

    private String invoiceId;
	private List<AzureBillingPeriodWrapper> azureBillingPeriods = new ArrayList<AzureBillingPeriodWrapper>();
	private List<CSPLicenseBillingWrapper> cspLicenses = new ArrayList<CSPLicenseBillingWrapper>();
	private List<CSPOneTimeBillingWrapper> cspOnetimes = new ArrayList<CSPOneTimeBillingWrapper>();
	private List<CSPBilledContractService> siLicenses = new ArrayList<CSPBilledContractService>();
	private List<CSPBilledContractService> siAzureServices = new ArrayList<CSPBilledContractService>();
	private List<AzureInvoiceReportUsageLineItem> usageLineItems = new ArrayList<AzureInvoiceReportUsageLineItem>();
	private Map<String, List<AzureInvoiceReportLicenseLineItem>> licenseLineItems = new HashMap<String, List<AzureInvoiceReportLicenseLineItem>>();
	private Map<String, List<AzureInvoiceReportOneTimeLineItem>> onetimeLineItems = new HashMap<String, List<AzureInvoiceReportOneTimeLineItem>>();

    public String getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }
	
	public List<AzureBillingPeriodWrapper> getAzureBillingPeriods() {
		return azureBillingPeriods;
	}
	
	public void setAzureBillingPeriods(
			List<AzureBillingPeriodWrapper> azureBillingPeriods) {
		this.azureBillingPeriods = azureBillingPeriods;
	}
	
	public List<CSPLicenseBillingWrapper> getCspLicenses() {
		return cspLicenses;
	}
	
	public void setCspLicenses(List<CSPLicenseBillingWrapper> cspLicenses) {
		this.cspLicenses = cspLicenses;
	}

    public List<CSPOneTimeBillingWrapper> getCspOnetimes() {
        return cspOnetimes;
    }

    public void setCspOnetimes(List<CSPOneTimeBillingWrapper> cspOnetimes) {
        this.cspOnetimes = cspOnetimes;
    }

	public List<CSPBilledContractService> getSiAzureServices() {
		return siAzureServices;
	}

	public List<CSPBilledContractService> getSiLicenses() {
		return siLicenses;
	}

	public void setSiLicenses(List<CSPBilledContractService> siLicenses) {
		this.siLicenses = siLicenses;
	}

	public void setSiAzureServices(List<CSPBilledContractService> siAzureServices) {
		this.siAzureServices = siAzureServices;
	}

	public List<AzureInvoiceReportUsageLineItem> getUsageLineItems() {
		return usageLineItems;
	}

	public void setUsageLineItems(List<AzureInvoiceReportUsageLineItem> usageLineItems) {
		this.usageLineItems = usageLineItems;
	}

	public Map<String, List<AzureInvoiceReportLicenseLineItem>> getLicenseLineItems() {
		return licenseLineItems;
	}

	public void setLicenseLineItems(
			Map<String, List<AzureInvoiceReportLicenseLineItem>> licenseLineItems) {
		this.licenseLineItems = licenseLineItems;
	}

    public Map<String, List<AzureInvoiceReportOneTimeLineItem>> getOnetimeLineItems() {
        return onetimeLineItems;
    }

    public void setOnetimeLineItems(Map<String, List<AzureInvoiceReportOneTimeLineItem>> onetimeLineItems) {
        this.onetimeLineItems = onetimeLineItems;
    }
	
}
