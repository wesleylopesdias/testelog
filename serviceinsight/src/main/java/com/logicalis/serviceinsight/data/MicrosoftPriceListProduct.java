package com.logicalis.serviceinsight.data;

import java.math.BigDecimal;

public class MicrosoftPriceListProduct {
	
	protected Long id;
	protected Long microsoftPriceListId;
	protected String offerName;
	protected String offerId;
	protected Device.Segment segment;
	protected BigDecimal unitPrice;
	protected BigDecimal erpPrice;
	protected MicrosoftPriceList.MicrosoftPriceListType productType;
	
	public MicrosoftPriceListProduct() {}
	
	public MicrosoftPriceListProduct(Long id, Long microsoftPriceListId, String offerName, String offerId,
			Device.Segment segment, BigDecimal unitPrice, BigDecimal erpPrice, MicrosoftPriceList.MicrosoftPriceListType productType) {
		super();
		this.id = id;
		this.microsoftPriceListId = microsoftPriceListId;
		this.offerName = offerName;
		this.offerId = offerId;
		this.segment = segment;
		this.unitPrice = unitPrice;
		this.erpPrice = erpPrice;
		this.productType = productType;
	}

	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public Long getMicrosoftPriceListId() {
		return microsoftPriceListId;
	}
	
	public void setMicrosoftPriceListId(Long microsoftPriceListId) {
		this.microsoftPriceListId = microsoftPriceListId;
	}
	
	public String getOfferName() {
		return offerName;
	}
	
	public void setOfferName(String offerName) {
		this.offerName = offerName;
	}
	
	public String getOfferId() {
		return offerId;
	}
	
	public void setOfferId(String offerId) {
		this.offerId = offerId;
	}
	
	public Device.Segment getSegment() {
		return segment;
	}

	public void setSegment(Device.Segment segment) {
		this.segment = segment;
	}

	public BigDecimal getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(BigDecimal unitPrice) {
		this.unitPrice = unitPrice;
	}

	public BigDecimal getErpPrice() {
		return erpPrice;
	}
	
	public void setErpPrice(BigDecimal erpPrice) {
		this.erpPrice = erpPrice;
	}

	public MicrosoftPriceList.MicrosoftPriceListType getProductType() {
		return productType;
	}

	public void setProductType(MicrosoftPriceList.MicrosoftPriceListType productType) {
		this.productType = productType;
	}

	@Override
	public String toString() {
		return "MicrosoftPriceListProduct [id=" + id + ", microsoftPriceListId=" + microsoftPriceListId + ", offerName="
				+ offerName + ", offerId=" + offerId + ", segment=" + segment + ", unitPrice=" + unitPrice
				+ ", erpPrice=" + erpPrice + ", productType=" + productType + "]";
	}
	
}
