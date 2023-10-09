package com.logicalis.serviceinsight.dao;

import java.util.Objects;

/**
 *
 * @author poneil
 */
public class ServiceExpenseCategory {
    
    private Integer expenseCategoryId;
    private Long ospId;
    private String businessModel;
    private Long serviceOfferingId;
    private Integer quantity;

    /**
     * default CTOR
     */
    public ServiceExpenseCategory() {
    }
    
    public ServiceExpenseCategory(Integer expenseCategoryId, Long ospId, String businessModel, Long serviceOfferingId, Integer quantity) {
        this.expenseCategoryId = expenseCategoryId;
        this.ospId = ospId;
        this.businessModel = businessModel;
        this.serviceOfferingId = serviceOfferingId;
        this.quantity = quantity;
    }
    
    public Integer getExpenseCategoryId() {
        return expenseCategoryId;
    }

    public void setExpenseCategoryId(Integer expenseCategoryId) {
        this.expenseCategoryId = expenseCategoryId;
    }

    public Long getOspId() {
        return ospId;
    }

    public void setOspId(Long ospId) {
        this.ospId = ospId;
    }

    public String getBusinessModel() {
        return businessModel;
    }

    public void setBusinessModel(String businessModel) {
        this.businessModel = businessModel;
    }

    public Long getServiceOfferingId() {
        return serviceOfferingId;
    }

    public void setServiceOfferingId(Long serviceOfferingId) {
        this.serviceOfferingId = serviceOfferingId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 73 * hash + Objects.hashCode(this.expenseCategoryId);
        hash = 73 * hash + Objects.hashCode(this.ospId);
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
        final ServiceExpenseCategory other = (ServiceExpenseCategory) obj;
        if (!Objects.equals(this.expenseCategoryId, other.expenseCategoryId)) {
            return false;
        }
        if (!Objects.equals(this.ospId, other.ospId)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ServiceExpenseCategory{" + "expenseCategoryId=" + expenseCategoryId + ", ospId=" + ospId + ", businessModel=" + businessModel + ", serviceOfferingId=" + serviceOfferingId + ", quantity=" + quantity + '}';
    }
}
