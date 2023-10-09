package com.logicalis.serviceinsight.representation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AzureBillingPeriodWrapper {

	private String subscriptionId;
	private String customerId;
	private String customerAzureName;
	private String subscriptionName;
	private Date startDate;
	private Date endDate;
	private List<AzureBillingPeriodLineItem> lineItems = new ArrayList<AzureBillingPeriodLineItem>();
	private BigDecimal total;
        /** total without uplift */
	private BigDecimal rawTotal;
	
	public AzureBillingPeriodWrapper(){}
	
	public AzureBillingPeriodWrapper(String subscriptionId, String customerId, Date startDate, Date endDate) {
		super();
		this.subscriptionId = subscriptionId;
		this.customerId = customerId;
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public AzureBillingPeriodWrapper(String subscriptionId, String customerId, Date startDate, Date endDate, List<AzureBillingPeriodLineItem> lineItems, BigDecimal total, BigDecimal rawTotal) {
		super();
		this.subscriptionId = subscriptionId;
		this.customerId = customerId;
		this.startDate = startDate;
		this.endDate = endDate;
		this.lineItems = lineItems;
		this.total = total;
                this.rawTotal = rawTotal;
	}

	public String getSubscriptionId() {
		return subscriptionId;
	}
	
	public void setSubscriptionId(String subscriptionId) {
		this.subscriptionId = subscriptionId;
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

	public String getSubscriptionName() {
		return subscriptionName;
	}

	public void setSubscriptionName(String subscriptionName) {
		this.subscriptionName = subscriptionName;
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

	public List<AzureBillingPeriodLineItem> getLineItems() {
		return lineItems;
	}
	
	public void setLineItems(List<AzureBillingPeriodLineItem> lineItems) {
		this.lineItems = lineItems;
	}
        
        public void addLineItem(AzureBillingPeriodLineItem lineItem) {
            if (this.lineItems == null) {
                this.lineItems = new ArrayList<AzureBillingPeriodLineItem>();
            }
            this.lineItems.add(lineItem);
        }
	
	public BigDecimal getTotal() {
		return total;
	}
	
	public void setTotal(BigDecimal total) {
		this.total = total;
	}

    public BigDecimal getRawTotal() {
        return rawTotal;
    }

    public void setRawTotal(BigDecimal rawTotal) {
        this.rawTotal = rawTotal;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((subscriptionId == null) ? 0 : subscriptionId.hashCode());
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
		AzureBillingPeriodWrapper other = (AzureBillingPeriodWrapper) obj;
		if (subscriptionId == null) {
			if (other.subscriptionId != null)
				return false;
		} else if (!subscriptionId.equals(other.subscriptionId))
			return false;
		return true;
	}
    
    
	
}
