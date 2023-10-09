package com.logicalis.serviceinsight.representation;

import com.logicalis.ap.ServiceException;
import java.math.BigDecimal;
import java.util.Objects;

public class CSPOneTimeBillingLineItem {

    private String id;
    private String chargeType;
    private String productName;
    private String subscriptionName;
    private Integer quantity;
    private BigDecimal unitPrice = BigDecimal.ZERO;
    private BigDecimal subscriptionTotal = BigDecimal.ZERO;
    private BigDecimal upliftTotal = BigDecimal.ZERO;
    private String termAndBillingCycle;
    private ServiceException error;
    private Boolean persisted = Boolean.FALSE;

    /**
     * default CTOR
     */
    public CSPOneTimeBillingLineItem() {
    }

    /**
     * minimum CTOR for equality
     * @param id 
     */
    public CSPOneTimeBillingLineItem(String id) {
        super();
        this.id = id;
    }
    
    public CSPOneTimeBillingLineItem(String id, String chargeType, String productName, String subscriptionName, Integer quantity, BigDecimal unitPrice, BigDecimal subscriptionTotal, String termAndBillingCycle) {
        super();
        this.id = id;
        this.chargeType = chargeType;
        this.productName = productName;
        this.subscriptionName = subscriptionName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subscriptionTotal = subscriptionTotal;
        this.termAndBillingCycle = termAndBillingCycle;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getChargeType() {
        return chargeType;
    }

    public void setChargeType(String chargeType) {
        this.chargeType = chargeType;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getSubscriptionName() {
        return subscriptionName;
    }

    public void setSubscriptionName(String subscriptionName) {
        this.subscriptionName = subscriptionName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getSubscriptionTotal() {
        return subscriptionTotal;
    }

    public void setSubscriptionTotal(BigDecimal subscriptionTotal) {
        this.subscriptionTotal = subscriptionTotal;
    }

    public BigDecimal getUpliftTotal() {
        return upliftTotal;
    }

    public void setUpliftTotal(BigDecimal upliftTotal) {
        this.upliftTotal = upliftTotal;
    }

    public void addUpliftAmount(BigDecimal uplift) {
        if (upliftTotal == null) {
            upliftTotal = BigDecimal.ZERO;
        }
        if (uplift != null) {
            upliftTotal = upliftTotal.add(uplift);
        }
    }

    public String getTermAndBillingCycle() {
        return termAndBillingCycle;
    }

    public void setTermAndBillingCycle(String termAndBillingCycle) {
        this.termAndBillingCycle = termAndBillingCycle;
    }

    public ServiceException getError() {
        return error;
    }

    public void setError(ServiceException error) {
        this.error = error;
    }

    public Boolean getPersisted() {
        return persisted;
    }

    public void setPersisted(Boolean persisted) {
        this.persisted = persisted;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + Objects.hashCode(this.id);
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
        final CSPOneTimeBillingLineItem other = (CSPOneTimeBillingLineItem) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "CSPOneTimeBillingLineItem{" + "id=" + id + ", chargeType=" + chargeType + ", productName=" + productName + ", subscriptionName=" + subscriptionName + ", quantity=" + quantity + ", unitPrice=" + unitPrice + ", subscriptionTotal=" + subscriptionTotal + ", upliftTotal=" + upliftTotal + ", termAndBillingCycle=" + termAndBillingCycle + '}';
    }
}
