package com.logicalis.serviceinsight.representation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CSPLicenseBillingWrapper {

	private String customerId;
	private String customerAzureName;
	private Date startDate;
	private Date endDate;
	private List<CSPLicenseBillingLineItem> lineItems = new ArrayList<CSPLicenseBillingLineItem>();
	private BigDecimal total = BigDecimal.ZERO;
	
    /**
     * default CTOR
     */
    public CSPLicenseBillingWrapper() {
    }
    
    public CSPLicenseBillingWrapper(String customerId, String customerAzureName) {
        this.customerId = customerId;
        this.customerAzureName = customerAzureName;
    }
        
	public String getCustomerId() {
		return customerId;
	}
	
	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}
	
	public String getCustomerAzureName() {
		return customerAzureName;
	}
	
	public void setCustomerAzureName(String customerAzureName) {
		this.customerAzureName = customerAzureName;
	}
	
	public Date getStartDate() {
		return startDate;
	}
	
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	
	public Date getEndDate() {
		return endDate;
	}
	
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	
	public List<CSPLicenseBillingLineItem> getLineItems() {
		return lineItems;
	}
	
	public void setLineItems(List<CSPLicenseBillingLineItem> lineItems) {
		this.lineItems = lineItems;
	}
	
	public BigDecimal getTotal() {
		return total;
	}
	
	public void setTotal(BigDecimal total) {
		this.total = total;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((customerId == null) ? 0 : customerId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CSPLicenseBillingWrapper other = (CSPLicenseBillingWrapper) obj;
		if (customerId == null) {
			if (other.customerId != null)
				return false;
		} else if (!customerId.equals(other.customerId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CSPLicenseBillingWrapper [customerId=" + customerId + ", customerAzureName=" + customerAzureName
				+ ", startDate=" + startDate + ", endDate=" + endDate + ", lineItems=" + lineItems + ", total=" + total
				+ "]";
	}
	
}
