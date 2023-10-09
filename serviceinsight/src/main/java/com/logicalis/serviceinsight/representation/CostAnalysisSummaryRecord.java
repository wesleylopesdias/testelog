package com.logicalis.serviceinsight.representation;

import java.math.BigDecimal;
import java.util.Objects;

/**
 *
 * @author poneil
 */
public class CostAnalysisSummaryRecord implements Comparable<CostAnalysisSummaryRecord> {
    
    private CostItemTypeSubType costItemCostSubType;
    private BigDecimal previous = BigDecimal.ZERO;
    private BigDecimal current = BigDecimal.ZERO;
    private BigDecimal next = BigDecimal.ZERO;

    public CostAnalysisSummaryRecord(CostItemTypeSubType costItemCostSubType) {
        this.costItemCostSubType = costItemCostSubType;
    }
    
    public CostItemTypeSubType getCostItemCostSubType() {
        return costItemCostSubType;
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
        hash = 67 * hash + Objects.hashCode(this.costItemCostSubType);
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
        final CostAnalysisSummaryRecord other = (CostAnalysisSummaryRecord) obj;
        if (!Objects.equals(this.costItemCostSubType, other.costItemCostSubType)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(CostAnalysisSummaryRecord o) {
        return this.costItemCostSubType.compareTo(o.getCostItemCostSubType());
    }
}
