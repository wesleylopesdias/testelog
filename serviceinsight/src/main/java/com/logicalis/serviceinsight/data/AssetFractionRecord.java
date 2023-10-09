package com.logicalis.serviceinsight.data;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang.ObjectUtils;
import org.joda.time.DateTime;

/**
 * This class represents a convenient query result Object for computing assets associated
 * with Services and Expense Categories.
 * NOTE: it's equals and hashCode methods are designed to ignore non-unique asset fractions
 * NOTE: it's compareTo method sorts in ascending order similar to how the preferred database
 * ordering occurs
 * 
 * @author poneil
 */
public class AssetFractionRecord implements Serializable, Comparable<AssetFractionRecord> {
    
    private Long assetItemId;
    private Long customerId;
    private BigDecimal fraction = BigDecimal.ZERO;
    private Integer expenseCategoryId;
    private List<Long> serviceIds = new ArrayList<Long>();
    private List<String> businessModels = new ArrayList<String>();
    private String expenseName;
    private BigDecimal cost = BigDecimal.ZERO;
    private Integer quantity;
    private DateTime acquired;
    private Integer life;
    
    public AssetFractionRecord() {}
    
    public AssetFractionRecord(Long assetItemId, Long customerId, BigDecimal fraction,
            Integer expenseCategoryId, String expenseName, BigDecimal cost, Integer quantity,
            DateTime acquired, Integer life) {
        this.assetItemId = assetItemId;
        this.customerId = customerId;
        this.fraction = fraction;
        this.expenseCategoryId = expenseCategoryId;
        this.expenseName = expenseName;
        this.cost = cost;
        this.quantity = quantity;
        this.acquired = acquired;
        this.life = life;
    }

    public Long getAssetItemId() {
        return assetItemId;
    }

    public void setAssetItemId(Long assetItemId) {
        this.assetItemId = assetItemId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
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

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "America/New_York")
    public DateTime getAcquired() {
        return acquired;
    }

    public void setAcquired(DateTime acquired) {
        this.acquired = acquired;
    }

    public Integer getLife() {
        return life;
    }

    public void setLife(Integer life) {
        this.life = life;
    }

    /**
     * Please don't change. Used in specific identity business logic
     */
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + Objects.hashCode(this.assetItemId);
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
        final AssetFractionRecord other = (AssetFractionRecord) obj;
        if (!Objects.equals(this.assetItemId, other.assetItemId)) {
            return false;
        }
        if (!Objects.equals(this.expenseCategoryId, other.expenseCategoryId)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "AssetFractionRecord{" + "assetItemId=" + assetItemId + ", customerId=" + customerId + ", fraction=" + fraction
                + ", expenseCategoryId=" + expenseCategoryId + ", serviceIds=" + serviceIds + ", businessModels=" + businessModels
                + ", expenseName=" + expenseName + ", cost=" + cost + ", quantity=" + quantity + ", acquired=" + acquired + ", life=" + life + '}';
    }

    @Override
    public int compareTo(AssetFractionRecord o) {
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
        idx = ObjectUtils.compare(getAssetItemId(), o.getAssetItemId());
        if (idx != 0)
            return -1 * idx;
        idx = ObjectUtils.compare(getAcquired(), o.getAcquired());
        if (idx != 0)
            return idx;
        return 0;
    }
}
