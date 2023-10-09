package com.logicalis.serviceinsight.representation;

import java.math.BigDecimal;
import java.util.Objects;
import org.apache.commons.lang.ObjectUtils;

/**
 *
 * @author poneil
 */
public class CostAnalysisCustomerRecord implements Comparable<CostAnalysisCustomerRecord> {
    
    private CostItemTypeSubType costItemCostSubType;
    private String customer;
    private BigDecimal previous = BigDecimal.ZERO;
    private BigDecimal current = BigDecimal.ZERO;
    private BigDecimal next = BigDecimal.ZERO;

    public CostAnalysisCustomerRecord(CostItemTypeSubType costItemCostSubType, String customer) {
        this.costItemCostSubType = costItemCostSubType;
        this.customer = customer;
    }
    
    public CostItemTypeSubType getCostItemCostSubType() {
        return costItemCostSubType;
    }

    public String getCustomer() {
        return customer;
    }

    public BigDecimal getPrevious() {
        return previous;
    }

    public void setPrevious(BigDecimal previous) {
        this.previous = previous;
    }

    public BigDecimal getCurrent() {
        return current;
    }

    public void setCurrent(BigDecimal current) {
        this.current = current;
    }

    public BigDecimal getNext() {
        return next;
    }

    public void setNext(BigDecimal next) {
        this.next = next;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.costItemCostSubType);
        hash = 29 * hash + Objects.hashCode(this.customer);
        return hash;
    }

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
        final CostAnalysisCustomerRecord other = (CostAnalysisCustomerRecord) obj;
        if (!Objects.equals(this.customer, other.customer)) {
            return false;
        }
        if (!Objects.equals(this.costItemCostSubType, other.costItemCostSubType)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(CostAnalysisCustomerRecord o) {
        int idx = this.costItemCostSubType.compareTo(o.getCostItemCostSubType());
        if (idx != 0) {
            return idx;
        }
        return ObjectUtils.compare(this.customer, o.getCustomer());
    }
}
