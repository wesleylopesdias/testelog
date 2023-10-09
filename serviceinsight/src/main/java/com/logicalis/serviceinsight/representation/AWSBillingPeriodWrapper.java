package com.logicalis.serviceinsight.representation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AWSBillingPeriodWrapper {

	private String accountId;
        private String customerId;
	private String customerAWSName;
	private String subscriptionName;
	private Date startDate;
	private Date endDate;
	private List<AWSBillingPeriodLineItem> lineItems = new ArrayList<AWSBillingPeriodLineItem>();
	private BigDecimal total;
    /** total without uplift */
	private BigDecimal rawTotal;
	
	public AWSBillingPeriodWrapper(){}
        
	public AWSBillingPeriodWrapper(String accountId, Date startDate, Date endDate, List<AWSBillingPeriodLineItem> lineItems, BigDecimal total, BigDecimal rawTotal) {
		super();
		this.accountId = accountId;
		this.startDate = startDate;
		this.endDate = endDate;
		this.lineItems = lineItems;
		this.total = total;
		this.rawTotal = rawTotal;
	}

	public String getAccountId() {
		return accountId;
	}
	
	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerAWSName() {
        return customerAWSName;
    }

    public void setCustomerAWSName(String customerAWSName) {
        this.customerAWSName = customerAWSName;
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
	
	public List<AWSBillingPeriodLineItem> getLineItems() {
		return lineItems;
	}

	public void setLineItems(List<AWSBillingPeriodLineItem> lineItems) {
		this.lineItems = lineItems;
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
	
}
