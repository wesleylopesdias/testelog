package com.logicalis.serviceinsight.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PipelineQuoteMin implements Comparable<PipelineQuoteMin> {

    private Long id;
    private String quoteNumber;
    private Long customerId;
    private String customerName;
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="MMddyyyy", timezone="America/New_York")
    private Date closeDate;
    private List<PipelineQuoteLineItemMin> lineItems = new ArrayList<PipelineQuoteLineItemMin>();
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuoteNumber() {
        return quoteNumber;
    }

    public void setQuoteNumber(String quoteNumber) {
        this.quoteNumber = quoteNumber;
    }

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

    public Date getCloseDate() {
        return closeDate;
    }

    public void setCloseDate(Date closeDate) {
        this.closeDate = closeDate;
    }

    public List<PipelineQuoteLineItemMin> getLineItems() {
        return lineItems;
    }

    public void setLineItems(List<PipelineQuoteLineItemMin> lineItems) {
        this.lineItems = lineItems;
    }

    @Override
    public int hashCode() {
        final int prime  = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        PipelineQuoteMin other = (PipelineQuoteMin) obj;
        if (id == null) {
            if (other.id != null) return false;
        } else if (!id.equals(other.id)) return false;
        return true;
    }

    @Override
    public int compareTo(PipelineQuoteMin o) {
        if (o == null) {
            return 1;
        }
        if (getId() != null) {
            if (o.getId() == null) {
                return 1;
            }
            int idx = getId().compareTo(o.getId());
            if (idx != 0) {
                return idx;
            }
        }
        return 0;
    }
    
    public String toString() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("[").append(getId().toString()).append("] ")
            .append(getQuoteNumber()).append(", ")
            .append("[").append(this.getCustomerId()).append("] ")
            .append(getCustomerName()).append(", ")
            .append(getCloseDate().toString()).append(", ")
            .append(this.getLineItems().size()).append(" line items");
        return sbuf.toString();
    }
    
}
