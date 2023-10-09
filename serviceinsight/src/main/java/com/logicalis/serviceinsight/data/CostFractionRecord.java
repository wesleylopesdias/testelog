package com.logicalis.serviceinsight.data;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.logicalis.serviceinsight.dao.ServiceExpenseCategory;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang.ObjectUtils;
import org.joda.time.DateTime;

/**
 * This class represents a convenient query result Object for computing costs associated
 * with Services and Expense Categories.
 * NOTE: it's equals and hashCode methods are designed to ignore non-unique cost fractions
 * NOTE: it's compareTo method sorts in ascending order similar to how the preferred database
 * ordering occurs
 * 
 * @author poneil
 */
public class CostFractionRecord implements Serializable, Comparable<CostFractionRecord> {
    
    private Long costItemId;
    private Long customerId;
    private String customerName;
    private BigDecimal fraction = BigDecimal.ZERO;
    private Integer expenseCategoryId;
    private List<ServiceExpenseCategory> serviceExpenseCategories = new ArrayList<ServiceExpenseCategory>();
    private List<Long> serviceIds = new ArrayList<Long>();
    private List<String> businessModels = new ArrayList<String>();
    private String expenseName;
    private BigDecimal cost = BigDecimal.ZERO;
    private Integer life = 1;
    private DateTime applied;
    
    public CostFractionRecord() {}
    
    public CostFractionRecord(Long costItemId, Long customerId, String customerName, BigDecimal fraction,
            Integer expenseCategoryId, String expenseName, BigDecimal cost, Integer life, DateTime applied) {
        this.costItemId = costItemId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.fraction = fraction;
        this.expenseCategoryId = expenseCategoryId;
        this.expenseName = expenseName;
        this.cost = cost;
        this.life = (life == null ? 1 : (life < 0 ? 1 : life));
        this.applied = applied;
    }

    public Long getCostItemId() {
        return costItemId;
    }

    public void setCostItemId(Long costItemId) {
        this.costItemId = costItemId;
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

    public BigDecimal getFraction() {
        return fraction;
    }

    public void setFraction(BigDecimal fraction) {
        this.fraction = fraction;
    }

    public Integer getExpenseCategoryId() {
        return expenseCategoryId;
    }

    public void setExpenseCategoryId(Integer expenseCategoryId) {
        this.expenseCategoryId = expenseCategoryId;
    }

    public List<Long> getServiceIds() {
        return serviceIds;
    }
    
    public void setServiceIds(List<Long> serviceIds) {
        this.serviceIds = serviceIds;
    }
    
    public void addServiceId(Long ospId) {
        this.serviceIds.add(ospId);
    }
    
    public List<ServiceExpenseCategory> getServiceExpenseCategories() {
        return serviceExpenseCategories;
    }

    public void setServiceExpenseCategories(List<ServiceExpenseCategory> serviceExpenseCategories) {
        this.serviceExpenseCategories = serviceExpenseCategories;
    }
    
    public void addServiceExpenseCategory(ServiceExpenseCategory value) {
        this.serviceExpenseCategories.add(value);
    }

    public List<String> getBusinessModels() {
        return businessModels;
    }

    public void setBusinessModels(List<String> businessModels) {
        this.businessModels = businessModels;
    }
    
    public void addBusinessModel(String businessModel) {
        this.businessModels.add(businessModel);
    }

    public String getExpenseName() {
        return expenseName;
    }

    public void setExpenseName(String expenseName) {
        this.expenseName = expenseName;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public BigDecimal getAmortizedCost() {
        if (life != null && life > 1) {
            return cost.divide(new BigDecimal(life));
        }
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public Integer getLife() {
        return life;
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "America/New_York")
    public DateTime getApplied() {
        return applied;
    }

    public void setApplied(DateTime applied) {
        this.applied = applied;
    }

    /**
     * Please don't change. Used in specific identity business logic
     */
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + Objects.hashCode(this.costItemId);
        hash = 23 * hash + Objects.hashCode(this.expenseCategoryId);
        return hash;
    }

    /**
     * Please don't change. Used in specific identity business logic
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
        final CostFractionRecord other = (CostFractionRecord) obj;
        if (!Objects.equals(this.costItemId, other.costItemId)) {
            return false;
        }
        if (!Objects.equals(this.expenseCategoryId, other.expenseCategoryId)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "CostFractionRecord{" + "costItemId=" + costItemId + ", customerId=" + customerId + ", customerName=" + customerName
                + ", fraction=" + fraction + ", expenseCategoryId=" + expenseCategoryId + ", serviceExpenseCategories=" + serviceExpenseCategories
                + ", businessModels=" + businessModels + ", expenseName=" + expenseName + ", cost=" + cost + ", life=" + life
                 + ", applied=" + applied + '}';
    }

    @Override
    public int compareTo(CostFractionRecord o) {
        if (o == null)
            return 1;
        int idx = ObjectUtils.compare(getExpenseCategoryId(), o.getExpenseCategoryId());
        if (idx != 0)
            return -1 * idx;
        idx = ObjectUtils.compare(getFraction(), o.getFraction());
        if (idx != 0)
            return -1 * idx;
        idx = ObjectUtils.compare(getCustomerId(), o.getCustomerId());
        if (idx != 0)
            return idx;
        idx = ObjectUtils.compare(getCostItemId(), o.getCostItemId());
        if (idx != 0)
            return -1 * idx;
        idx = ObjectUtils.compare(getApplied(), o.getApplied());
        if (idx != 0)
            return idx;
        return 0;
    }
}
