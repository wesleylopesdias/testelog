package com.logicalis.serviceinsight.representation;

import java.math.BigDecimal;

public class AzureInvoiceReportUsageLineItem {
	
	private String azureCustomerName;
	private String azureSubscriptionId;
	private String azureSubscriptionName;
	private BigDecimal azureMonthlyCost;
	private String siCustomerName;
	private String siSubscripionName;
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
	
	public BigDecimal getAzureMonthlyCost() {
		return azureMonthlyCost;
	}
	
	public void setAzureMonthlyCost(BigDecimal azureMonthlyCost) {
		this.azureMonthlyCost = azureMonthlyCost;
	}
	
	public String getSiCustomerName() {
		return siCustomerName;
	}
	
	public void setSiCustomerName(String siCustomerName) {
		this.siCustomerName = siCustomerName;
	}
	
	public String getSiSubscripionName() {
		return siSubscripionName;
	}
	
	public void setSiSubscripionName(String siSubscripionName) {
		this.siSubscripionName = siSubscripionName;
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
	
	/*
	public BigDecimal getSiMonthlyRevenue() {
		return getSiOnetimeRevenue().add(getSiRecurringRevenue());
	}*/
	
}
