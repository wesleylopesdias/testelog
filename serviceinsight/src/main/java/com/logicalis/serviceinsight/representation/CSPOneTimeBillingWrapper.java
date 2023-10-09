package com.logicalis.serviceinsight.representation;

import com.logicalis.ap.APClient;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CSPOneTimeBillingWrapper {

    private APClient apclient;
    private String customerId;
    private String customerAzureName;
    private String customerSIName;
    private Date startDate;
    private Date endDate;
    private List<CSPOneTimeBillingLineItem> lineItems = new ArrayList<CSPOneTimeBillingLineItem>();
    private BigDecimal total = BigDecimal.ZERO;

    /**
     * default CTOR
     */
    public CSPOneTimeBillingWrapper() {
    }

    /**
     * minimum CTOR for equality...
     * @param customerId
     */
    public CSPOneTimeBillingWrapper(String customerId) {
        this.customerId = customerId.toLowerCase();
    }

    public CSPOneTimeBillingWrapper(String customerId, String customerSIName, String customerAzureName) {
        this.customerId = customerId.toLowerCase();
        this.customerSIName = customerSIName;
        this.customerAzureName = customerAzureName;
    }

    public APClient getApclient() {
        return apclient;
    }

    public void setApclient(APClient apclient) {
        this.apclient = apclient;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        if (customerId != null) {
            this.customerId = customerId.toLowerCase();
        }
    }

    public String getCustomerAzureName() {
        return customerAzureName;
    }

    public void setCustomerAzureName(String customerAzureName) {
        this.customerAzureName = customerAzureName;
    }

    public String getCustomerSIName() {
        return customerSIName;
    }

    public void setCustomerSIName(String customerSIName) {
        this.customerSIName = customerSIName;
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

    public List<CSPOneTimeBillingLineItem> getLineItems() {
        return lineItems;
    }

    public void setLineItems(List<CSPOneTimeBillingLineItem> lineItems) {
        this.lineItems = lineItems;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }
    
    public void incrementTotal(BigDecimal increment) {
        if (this.total == null) {
            this.total = BigDecimal.ZERO;
        }
        if (increment != null) {
            this.total = this.total.add(increment);
        }
    }

    /**
     * don't change
     *
     * @return
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((customerId == null) ? 0 : customerId.hashCode());
        return result;
    }

    /**
     * don't change
     *
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CSPOneTimeBillingWrapper other = (CSPOneTimeBillingWrapper) obj;
        if (customerId == null) {
            if (other.customerId != null) {
                return false;
            }
        } else if (!customerId.equals(other.customerId)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "CSPOneTimeBillingWrapper{" + "customerId=" + customerId + ", customerAzureName=" + customerAzureName + ", customerSIName=" + customerSIName + ", startDate=" + startDate + ", endDate=" + endDate + ", lineItems=" + lineItems + ", total=" + total + '}';
    }
}
