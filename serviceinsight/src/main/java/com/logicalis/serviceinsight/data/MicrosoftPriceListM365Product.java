package com.logicalis.serviceinsight.data;

import java.math.BigDecimal;

public class MicrosoftPriceListM365Product extends MicrosoftPriceListProduct {
	
	private String purchase;
	private String secondaryLicenseType;
	private String endCustomerType;
	private String material;
	
	public MicrosoftPriceListM365Product() {}
	
	public MicrosoftPriceListM365Product(Long id, Long microsoftPriceListId, String offerName, String offerId,
			Device.Segment segment, String purchase, String secondaryLicenseType,
			String endCustomerType, BigDecimal unitPrice, BigDecimal erpPrice, String material) {
		super(id, microsoftPriceListId, offerName, offerId, segment, unitPrice, erpPrice, MicrosoftPriceList.MicrosoftPriceListType.M365);
		this.id = id;
		this.microsoftPriceListId = microsoftPriceListId;
		this.offerName = offerName;
		this.offerId = offerId;
		this.purchase = purchase;
		this.secondaryLicenseType = secondaryLicenseType;
		this.endCustomerType = endCustomerType;
		this.unitPrice = unitPrice;
		this.erpPrice = erpPrice;
		this.material = material;
	}
	
	public String getPurchase() {
		return purchase;
	}

	public void setPurchase(String purchase) {
		this.purchase = purchase;
	}

	public String getSecondaryLicenseType() {
		return secondaryLicenseType;
	}
	
	public void setSecondaryLicenseType(String secondaryLicenseType) {
		this.secondaryLicenseType = secondaryLicenseType;
	}
	
	public String getEndCustomerType() {
		return endCustomerType;
	}
	
	public void setEndCustomerType(String endCustomerType) {
		this.endCustomerType = endCustomerType;
	}

	public String getMaterial() {
		return material;
	}

	public void setMaterial(String material) {
		this.material = material;
	}

	@Override
	public String toString() {
		return "MicrosoftPriceListProduct [id=" + id + ", microsoftPriceListId=" + microsoftPriceListId + ", offerName="
				+ offerName + ", offerId=" + offerId + ", segment=" + segment + ", purchase="
				+ purchase + ", secondaryLicenseType=" + secondaryLicenseType + ", endCustomerType=" + endCustomerType
				+ ", unitPrice=" + unitPrice + ", erpPrice=" + erpPrice + ", material=" + material + "]";
	}
	
}
