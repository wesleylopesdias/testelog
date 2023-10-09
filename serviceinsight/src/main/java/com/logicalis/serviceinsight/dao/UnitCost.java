package com.logicalis.serviceinsight.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import org.apache.commons.lang.ObjectUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

/**
 *
 * @author poneil
 */
public class UnitCost implements Comparable<UnitCost> {
    
    private Long id;
    private Long customerId;
    private String customerName;
    private Integer expenseCategoryId;
    private String expenseCategoryName;
    private Integer deviceTotalUnits = 0;
    private Integer contributingDeviceCount = 0;
    private BigDecimal totalCost = new BigDecimal(0);
    private BigDecimal contributingCost = BigDecimal.ZERO;
    private BigDecimal totalLabor = new BigDecimal(0);
    private DateTime appliedDate;

    /**
     * default CTOR
     */
    public UnitCost() {
    }

    /**
     * this CTOR reflects the uniqueness of a UnitCost record/object
     * 
     * @param expenseCategoryId
     * @param customerId
     * @param appliedDate 
     */
    public UnitCost(Integer expenseCategoryId, Long customerId, DateTime appliedDate) {
        this.expenseCategoryId = expenseCategoryId;
        this.customerId = customerId;
        this.appliedDate = appliedDate;
    }

    public UnitCost(Integer expenseCategoryId, Long customerId, Integer deviceTotalUnits, BigDecimal totalCost) {
        this.expenseCategoryId = expenseCategoryId;
        this.customerId = customerId;
        this.deviceTotalUnits = deviceTotalUnits;
        this.totalCost = totalCost;
    }

    public UnitCost(Integer expenseCategoryId, Long customerId, Integer deviceTotalUnits, BigDecimal totalCost, DateTime appliedDate) {
        this.expenseCategoryId = expenseCategoryId;
        this.customerId = customerId;
        this.deviceTotalUnits = deviceTotalUnits;
        this.totalCost = totalCost;
        this.appliedDate = appliedDate;
    }
    
    public UnitCost(Long id, Long customerId, Integer expenseCategoryId, BigDecimal totalCost, BigDecimal totalLabor, Integer deviceTotalUnits, DateTime appliedDate) {
        this.id = id;
        this.customerId = customerId;
        this.expenseCategoryId = expenseCategoryId;
        this.totalCost = totalCost;
        this.totalLabor = totalLabor;
        this.deviceTotalUnits = deviceTotalUnits;
        this.appliedDate = appliedDate;
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Integer getExpenseCategoryId() {
        return expenseCategoryId;
    }
    
    public void setExpenseCategoryId(Integer expenseCategoryId) {
        this.expenseCategoryId = expenseCategoryId;
    }

    public String getExpenseCategoryName() {
        return expenseCategoryName;
    }

    public void setExpenseCategoryName(String expenseCategoryName) {
        this.expenseCategoryName = expenseCategoryName;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }
    
    public void incrementTotalCost(BigDecimal increment) {
        if (this.totalCost != null) {
            this.totalCost = totalCost.add(increment);
        } else {
            this.totalCost = increment;
        }
    }
    
    public void decrementTotalCost(BigDecimal increment) {
        if (this.totalCost != null) {
            this.totalCost = totalCost.subtract(increment);
        } else {
            this.totalCost = increment.negate();
        }
    }

    public BigDecimal getTotalLabor() {
        return totalLabor;
    }

    public void setTotalLabor(BigDecimal totalLabor) {
        this.totalLabor = totalLabor;
    }
    
    public void incrementTotalLabor(BigDecimal increment) {
        if (this.totalLabor != null) {
            this.totalLabor = totalLabor.add(increment);
        } else {
            this.totalLabor = increment;
        }
    }
    
    public void decrementTotalLabor(BigDecimal increment) {
        if (this.totalLabor != null) {
            this.totalLabor = totalLabor.subtract(increment);
        } else {
            this.totalLabor = increment.negate();
        }
    }

    public Integer getDeviceTotalUnits() {
        return deviceTotalUnits;
    }

    public void setDeviceTotalUnits(Integer deviceTotalUnits) {
        this.deviceTotalUnits = deviceTotalUnits;
    }
    
    public void incrementDeviceTotalUnits(Integer increment) {
        if (this.deviceTotalUnits != null) {
            this.deviceTotalUnits += increment;
        } else {
            this.deviceTotalUnits = increment;
        }
    }
    
    public void decrementDeviceTotalUnits(Integer increment) {
        if (this.deviceTotalUnits != null) {
            this.deviceTotalUnits -= increment;
        } else {
            this.deviceTotalUnits = -1 * increment;
        }
    }

    public Integer getContributingDeviceCount() {
        return contributingDeviceCount;
    }

    public void setContributingDeviceCount(Integer contributingDeviceCount) {
        this.contributingDeviceCount = contributingDeviceCount;
    }
    
    public void addContributingDeviceCount(Integer quantity) {
        if (quantity != null) {
            this.contributingDeviceCount += quantity;
        }
    }

    public BigDecimal getContributingCost() {
        return (contributingCost == null ? BigDecimal.ZERO : contributingCost);
    }

    public BigDecimal getFormattedContributingCost() {
        return (contributingCost == null ? BigDecimal.ZERO : contributingCost.setScale(2, RoundingMode.HALF_UP));
    }

    public void setContributingCost(BigDecimal contributingCost) {
        this.contributingCost = contributingCost;
    }
    
    public void addContributingCost(BigDecimal amount) {
        if (amount != null) {
            this.contributingCost = this.contributingCost.add(amount);
        }
    }

    @JsonIgnore
    public DateTime getAppliedDate() {
        return appliedDate;
    }

    public void setAppliedDate(DateTime appliedDate) {
        this.appliedDate = appliedDate;
    }
    
    public String getAppliedDateShort() {
        if (this.appliedDate != null) {
            return DateTimeFormat.forPattern("yyyy-MM-dd").print(appliedDate);
        }
        return null;
    }
    
    public String getAppliedDateLong() {
        if (this.appliedDate != null) {
            return DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ssZ").print(appliedDate);
        }
        return null;
    }
    
    public UnitCost copyToNextUnitCost() {
        UnitCost uc = new UnitCost();
        uc.setCustomerId(customerId);
        uc.setAppliedDate(appliedDate.plusMonths(1));
        uc.setExpenseCategoryId(expenseCategoryId);
        uc.setTotalCost(totalCost);
        uc.setTotalLabor(totalLabor);
        uc.setDeviceTotalUnits(deviceTotalUnits);
        return uc;
    }

    /**
     * ID is not used to determine equality because logic in the application
     * relies on object properties to locate UnitCost members in Lists for fast
     * access.
     * 
     * @return 
     */
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + Objects.hashCode(this.customerId);
        hash = 89 * hash + Objects.hashCode(this.expenseCategoryId);
        hash = 89 * hash + Objects.hashCode(this.appliedDate);
        return hash;
    }

    /**
     * ID is not used to determine equality because logic in the application
     * relies on object properties to locate UnitCost members in Lists for fast
     * access.
     * 
     * @param obj
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
        final UnitCost other = (UnitCost) obj;
        if (!Objects.equals(this.customerId, other.customerId)) {
            return false;
        }
        if (!Objects.equals(this.expenseCategoryId, other.expenseCategoryId)) {
            return false;
        }
        if (!Objects.equals(this.appliedDate, other.appliedDate)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "UnitCost{" + "id=" + id + ", customerId=" + customerId + ", customerName=" + customerName + ", expenseCategoryId=" + expenseCategoryId + ", expenseCategoryName=" + expenseCategoryName + ", deviceTotalUnits=" + deviceTotalUnits + ", contributingDeviceCount=" + contributingDeviceCount + ", totalCost=" + totalCost + ", contributingCost=" + contributingCost + ", totalLabor=" + totalLabor + ", appliedDate=" + appliedDate + '}';
    }

    @Override
    public int compareTo(UnitCost o) {
        Integer result = ObjectUtils.compare(this.customerId, o.getCustomerId());
        if (result != 0) {
            return result;
        }
        result = ObjectUtils.compare(this.expenseCategoryId, o.getExpenseCategoryId());
        if (result != 0) {
            return result;
        }
        return ObjectUtils.compare(this.appliedDate, o.getAppliedDate());
    }
}
