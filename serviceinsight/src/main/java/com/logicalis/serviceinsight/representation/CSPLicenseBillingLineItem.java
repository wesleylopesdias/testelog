package com.logicalis.serviceinsight.representation;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

public class CSPLicenseBillingLineItem {

	private String id;
	private String chargeType;
	private String name;
	private Integer quantity;
	private BigDecimal unitPrice;
	private BigDecimal total;
	private String subscriptionId;
	private String durableOfferId;
	private String offerName;
	private String billingCycle;
	private BigDecimal tax;
	private Date subscriptionStartDate;
	private Date subscriptionEndDate;
	private Date chargeStartDate;
	private Date chargeEndDate;
	
	public CSPLicenseBillingLineItem(){}
        
	public CSPLicenseBillingLineItem(String id, String chargeType, String name, Integer quantity, BigDecimal unitPrice, BigDecimal total,
			String subscriptionId, String durableOfferId, String offerName, String billingCycle, BigDecimal tax, Date subscriptionStartDate,
			Date subscriptionEndDate, Date chargeStartDate, Date chargeEndDate) {
		super();
		this.id = id;
		this.chargeType = chargeType;
		this.name = name;
		this.quantity = quantity;
		this.unitPrice = unitPrice;
		this.total = total;
		this.subscriptionId = subscriptionId;
		this.durableOfferId = durableOfferId;
		this.offerName = offerName;
		this.billingCycle = billingCycle;
		this.tax = tax;
		this.subscriptionStartDate = subscriptionStartDate;
		this.subscriptionEndDate = subscriptionEndDate;
		this.chargeStartDate = chargeStartDate;
		this.chargeEndDate = chargeEndDate;
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

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
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

	public BigDecimal getTotal() {
		return total;
	}
	
	public void setTotal(BigDecimal total) {
		this.total = total;
	}

    public String getSubscriptionId() {
		return subscriptionId;
	}

	public void setSubscriptionId(String subscriptionId) {
		this.subscriptionId = subscriptionId;
	}

	public String getDurableOfferId() {
		return durableOfferId;
	}

	public void setDurableOfferId(String durableOfferId) {
		this.durableOfferId = durableOfferId;
	}

	public String getOfferName() {
		return offerName;
	}

	public void setOfferName(String offerName) {
		this.offerName = offerName;
	}

	public String getBillingCycle() {
		return billingCycle;
	}

	public void setBillingCycle(String billingCycle) {
		this.billingCycle = billingCycle;
	}

	public BigDecimal getTax() {
		return tax;
	}

	public void setTax(BigDecimal tax) {
		this.tax = tax;
	}

	public Date getSubscriptionStartDate() {
		return subscriptionStartDate;
	}

	public void setSubscriptionStartDate(Date subscriptionStartDate) {
		this.subscriptionStartDate = subscriptionStartDate;
	}

	public Date getSubscriptionEndDate() {
		return subscriptionEndDate;
	}

	public void setSubscriptionEndDate(Date subscriptionEndDate) {
		this.subscriptionEndDate = subscriptionEndDate;
	}

	public Date getChargeStartDate() {
		return chargeStartDate;
	}

	public void setChargeStartDate(Date chargeStartDate) {
		this.chargeStartDate = chargeStartDate;
	}

	public Date getChargeEndDate() {
		return chargeEndDate;
	}

	public void setChargeEndDate(Date chargeEndDate) {
		this.chargeEndDate = chargeEndDate;
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
        final CSPLicenseBillingLineItem other = (CSPLicenseBillingLineItem) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

	@Override
	public String toString() {
		return "CSPLicenseBillingLineItem [id=" + id + ", chargeType=" + chargeType + ", name=" + name + ", quantity="
				+ quantity + ", unitPrice=" + unitPrice + ", total=" + total + ", subscriptionId=" + subscriptionId
				+ ", durableOfferId=" + durableOfferId + ", offerName=" + offerName + ", billingCycle=" + billingCycle
				+ ", tax=" + tax + ", subscriptionStartDate=" + subscriptionStartDate + ", subscriptionEndDate="
				+ subscriptionEndDate + ", chargeStartDate=" + chargeStartDate + ", chargeEndDate=" + chargeEndDate
				+ "]";
	}
    
    
	
}
