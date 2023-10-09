package com.logicalis.serviceinsight.dao;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents the many-to-many relationship between Devices and Expense Categories.
 * 
 * @author poneil
 */
public class DeviceExpenseCategory implements Serializable {

    private Long deviceId;
    private Integer expenseCategoryId;
    private Integer quantity;
    private Boolean allocationCategory = Boolean.FALSE;

    /**
     * default CTOR
     */
    public DeviceExpenseCategory() {
    }

    public DeviceExpenseCategory(Long deviceId, Integer expenseCategoryId, Integer quantity, Boolean allocationCategory) {
        this.expenseCategoryId = expenseCategoryId;
        this.deviceId = deviceId;
        this.quantity = quantity;
        this.allocationCategory = allocationCategory;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public Integer getExpenseCategoryId() {
        return expenseCategoryId;
    }

    public void setExpenseCategoryId(Integer expenseCategoryId) {
        this.expenseCategoryId = expenseCategoryId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Boolean getAllocationCategory() {
		return allocationCategory;
	}

	public void setAllocationCategory(Boolean allocationCategory) {
		this.allocationCategory = allocationCategory;
	}

	@Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.deviceId);
        hash = 17 * hash + Objects.hashCode(this.expenseCategoryId);
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
        final DeviceExpenseCategory other = (DeviceExpenseCategory) obj;
        if (!Objects.equals(this.deviceId, other.deviceId)) {
            return false;
        }
        if (!Objects.equals(this.expenseCategoryId, other.expenseCategoryId)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "DeviceExpenseCategory{" + "deviceId=" + deviceId + ", expenseCategoryId=" + expenseCategoryId + ", quantity=" + quantity + '}';
    }
}
