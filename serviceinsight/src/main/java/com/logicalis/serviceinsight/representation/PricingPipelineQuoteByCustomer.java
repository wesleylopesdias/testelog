package com.logicalis.serviceinsight.representation;

import java.io.Serializable;
import java.util.Date;

public class PricingPipelineQuoteByCustomer implements Serializable, Comparable<PricingPipelineQuoteByCustomer> {
    
    private Long customerId;
    private String customerName;
    private Long quoteId;
    private String quoteNumber;
    private String closeDate;
    private String services;
    private Long totalItems;
    
    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Long getQuoteId() {
        return quoteId;
    }

    public void setQuoteId(Long quoteId) {
        this.quoteId = quoteId;
    }

    public String getQuoteNumber() {
        return quoteNumber;
    }

    public void setQuoteNumber(String quoteNumber) {
        this.quoteNumber = quoteNumber;
    }

    public String getCloseDate() {
        return closeDate;
    }

    public void setCloseDate(String closeDate) {
        this.closeDate = closeDate;
    }

    public String getServices() {
        return services;
    }

    public void setServices(String services) {
        this.services = services;
    }

    public Long getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(Long totalItems) {
        this.totalItems = totalItems;
    }
    
    @Override
    public int compareTo(PricingPipelineQuoteByCustomer rec) {
        int idx = 0;
        if (this.getCustomerName() != null && rec.getCustomerName() != null) {
            idx = (this.getCustomerName().compareTo(rec.getCustomerName()));
            if (idx != 0) {
                return idx;
            } else {
                if (this.getQuoteId() != null && rec.getQuoteId() != null) {
                    idx = this.getQuoteId().compareTo(rec.getQuoteId());
                    if (idx!=0) {
                        return idx;
                    } 
                } else if (this.getQuoteId() != null) {
                    return 1;
                } else if (rec.getQuoteId() != null) {
                    return -1;
                }
            }
        } else if (this.getCustomerName() != null) {
            return 1;
        } else if (rec.getCustomerName() != null) {
            return -1;
        }
        return idx;
    }

}
