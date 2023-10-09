package com.logicalis.serviceinsight.representation;

import java.math.BigDecimal;

public class AzureInvoiceReportOneTimeLineItem {

	private String azureCustomerName;
	private String azureSubscriptionId;
	private String azureSubscriptionName;
	private String azureOfferName;
	private Integer azureQuantity;
	private BigDecimal azureUnitPrice;
	private BigDecimal customerTotal;
        private String termAndBillingCycle;
	private String siCustomerName;
	private Integer siQuantity;
	private String siDeviceDescription;
	private String siDevicePartNumber;
	private Integer siDeviceUnitCount;
	private BigDecimal siOnetimeRevenue;
	private BigDecimal siRecurringRevenue;
	
	public String getAzureCustomerName() {
		return azureCustomerName;
	}
	
	public void setAzureCustomerName(String azureCustomerName) {
		this.azureCustomerName = azureCustomerName;
	}
	
	public String getAzureSubscriptionId() {
		return azureSubscriptionId;
	}
	
	public void setAzureSubscriptionId(String azureSubscriptionId) {
		this.azureSubscriptionId = azureSubscriptionId;
	}
	
	public String getAzureSubscriptionName() {
		return azureSubscriptionName;
	}
	
	public void setAzureSubscriptionName(String azureSubscriptionName) {
		this.azureSubscriptionName = azureSubscriptionName;
	}
	
	public String getAzureOfferName() {
		return azureOfferName;
	}

	public void setAzureOfferName(String azureOfferName) {
		this.azureOfferName = azureOfferName;
	}

	public Integer getAzureQuantity() {
		return azureQuantity;
	}
	
	public void setAzureQuantity(Integer azureQuantity) {
		this.azureQuantity = azureQuantity;
	}
	
	public BigDecimal getAzureUnitPrice() {
		return azureUnitPrice;
	}
	
	public void setAzureUnitPrice(BigDecimal azureUnitPrice) {
		this.azureUnitPrice = azureUnitPrice;
	}
	
	public BigDecimal getCustomerTotal() {
		return customerTotal;
	}
	
	public void setCustomerTotal(BigDecimal customerTotal) {
		this.customerTotal = customerTotal;
	}

    public String getTermAndBillingCycle() {
        return termAndBillingCycle;
    }

    public void setTermAndBillingCycle(String termAndBillingCycle) {
        this.termAndBillingCycle = termAndBillingCycle;
    }
	
	public String getSiCustomerName() {
		return siCustomerName;
	}
	
	public void setSiCustomerName(String siCustomerName) {
		this.siCustomerName = siCustomerName;
	}
	
	public Integer getSiQuantity() {
		return siQuantity;
	}
	
	public void setSiQuantity(Integer siQuantity) {
		this.siQuantity = siQuantity;
	}
	
	public String getSiDeviceDescription() {
		return siDeviceDescription;
	}

	public void setSiDeviceDescription(String siDeviceDescription) {
		this.siDeviceDescription = siDeviceDescription;
	}

	public String getSiDevicePartNumber() {
		return siDevicePartNumber;
	}
	
	public void setSiDevicePartNumber(String siDevicePartNumber) {
		this.siDevicePartNumber = siDevicePartNumber;
	}

	public Integer getSiDeviceUnitCount() {
		return siDeviceUnitCount;
	}

	public void setSiDeviceUnitCount(Integer siDeviceUnitCount) {
		this.siDeviceUnitCount = siDeviceUnitCount;
	}

	public BigDecimal getSiOnetimeRevenue() {
		return siOnetimeRevenue;
	}
	
	public void setSiOnetimeRevenue(BigDecimal siOnetimeRevenue) {
		this.siOnetimeRevenue = siOnetimeRevenue;
	}
	
	public BigDecimal getSiRecurringRevenue() {
		return siRecurringRevenue;
	}
	
	public void setSiRecurringRevenue(BigDecimal siRecurringRevenue) {
		this.siRecurringRevenue = siRecurringRevenue;
	}

    @Override
    public String toString() {
        return "AzureInvoiceReportOneTimeLineItem{" + "azureCustomerName=" + azureCustomerName + ", azureSubscriptionId=" + azureSubscriptionId + ", azureSubscriptionName=" + azureSubscriptionName + ", azureOfferName=" + azureOfferName + ", azureQuantity=" + azureQuantity + ", azureUnitPrice=" + azureUnitPrice + ", customerTotal=" + customerTotal + ", termAndBillingCycle=" + termAndBillingCycle + ", siCustomerName=" + siCustomerName + ", siQuantity=" + siQuantity + ", siDeviceDescription=" + siDeviceDescription + ", siDevicePartNumber=" + siDevicePartNumber + ", siDeviceUnitCount=" + siDeviceUnitCount + ", siOnetimeRevenue=" + siOnetimeRevenue + ", siRecurringRevenue=" + siRecurringRevenue + '}';
    }
	
	
	
}
