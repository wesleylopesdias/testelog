package com.logicalis.serviceinsight.representation;

import java.math.BigDecimal;
import java.util.Objects;
import org.apache.commons.lang.ObjectUtils;

/**
 *
 * @author poneil
 */
public class CostAnalysisExpenseCategoryRecord implements Comparable<CostAnalysisExpenseCategoryRecord> {
    
    private CostItemTypeSubType costItemCostSubType;
    private String parentExpenseCategory;
    private String expenseCategory;
    private BigDecimal previous = BigDecimal.ZERO;
    private BigDecimal current = BigDecimal.ZERO;
    private BigDecimal next = BigDecimal.ZERO;

    public CostAnalysisExpenseCategoryRecord(CostItemTypeSubType costItemCostSubType, String parentExpenseCategory, String expenseCategory) {
        this.costItemCostSubType = costItemCostSubType;
        this.parentExpenseCategory = parentExpenseCategory;
        this.expenseCategory = expenseCategory;
    }
    
    public CostItemTypeSubType getCostItemCostSubType() {
        return costItemCostSubType;
    }

    public String getParentExpenseCategory() {
        return parentExpenseCategory;
    }
    
    public String getExpenseCategoryName() {
        return (parentExpenseCategory == null ? expenseCategory : parentExpenseCategory + " - " + expenseCategory);
    }

    public String getExpenseCategory() {
        return expenseCategory;
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
        hash = 53 * hash + Objects.hashCode(this.costItemCostSubType);
        hash = 53 * hash + Objects.hashCode(this.parentExpenseCategory);
        hash = 53 * hash + Objects.hashCode(this.expenseCategory);
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
        final CostAnalysisExpenseCategoryRecord other = (CostAnalysisExpenseCategoryRecord) obj;
        if (!Objects.equals(this.parentExpenseCategory, other.parentExpenseCategory)) {
            return false;
        }
        if (!Objects.equals(this.expenseCategory, other.expenseCategory)) {
            return false;
        }
        if (!Objects.equals(this.costItemCostSubType, other.costItemCostSubType)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(CostAnalysisExpenseCategoryRecord o) {
        int idx = this.costItemCostSubType.compareTo(o.getCostItemCostSubType());
        if (idx != 0) {
            return idx;
        }
        idx = ObjectUtils.compare(this.parentExpenseCategory, o.getParentExpenseCategory());
        if (idx != 0) {
            return idx;
        }
        return ObjectUtils.compare(this.expenseCategory, o.getExpenseCategory());
    }
}
