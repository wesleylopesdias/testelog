package com.logicalis.serviceinsight.data;

import java.math.BigDecimal;

public class MicrosoftM365NCImportRecord {

	private Long id;
	private Long microsoftPriceListId;
	private String productTitle;
	private String productId;
	private String skuId;
	private String skuTitle;
	private String publisher;
	private String skuDescription;
	private String unitOfMeasure;
	private String termDuration;
	private String billingPlan;
	private String market;
	private String currency;
	private BigDecimal unitPrice;
	private String effectiveStartDate;
	private String effectiveEndDate;
	private String tags;
	private BigDecimal erpPrice;
	private String segment;
	
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
	
	public String getProductTitle() {
		return productTitle;
	}
	
	public void setProductTitle(String productTitle) {
		this.productTitle = productTitle;
	}
	
	public String getProductId() {
		return productId;
	}
	
	public void setProductId(String productId) {
		this.productId = productId;
	}
	
	public String getSkuId() {
		return skuId;
	}
	
	public void setSkuId(String skuId) {
		this.skuId = skuId;
	}
	
	public String getSkuTitle() {
		return skuTitle;
	}
	
	public void setSkuTitle(String skuTitle) {
		this.skuTitle = skuTitle;
	}
	
	public String getPublisher() {
		return publisher;
	}
	
	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}
	
	public String getSkuDescription() {
		return skuDescription;
	}
	
	public void setSkuDescription(String skuDescription) {
		this.skuDescription = skuDescription;
	}
	
	public String getUnitOfMeasure() {
		return unitOfMeasure;
	}
	
	public void setUnitOfMeasure(String unitOfMeasure) {
		this.unitOfMeasure = unitOfMeasure;
	}
	
	public String getTermDuration() {
		return termDuration;
	}
	
	public void setTermDuration(String termDuration) {
		this.termDuration = termDuration;
	}
	
	public String getBillingPlan() {
		return billingPlan;
	}
	
	public void setBillingPlan(String billingPlan) {
		this.billingPlan = billingPlan;
	}
	
	public String getMarket() {
		return market;
	}
	
	public void setMarket(String market) {
		this.market = market;
	}
	
	public String getCurrency() {
		return currency;
	}
	
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	
	public BigDecimal getUnitPrice() {
		return unitPrice;
	}
	
	public void setUnitPrice(BigDecimal unitPrice) {
		this.unitPrice = unitPrice;
	}
	
	public String getEffectiveStartDate() {
		return effectiveStartDate;
	}
	
	public void setEffectiveStartDate(String effectiveStartDate) {
		this.effectiveStartDate = effectiveStartDate;
	}
	
	public String getEffectiveEndDate() {
		return effectiveEndDate;
	}

	public void setEffectiveEndDate(String effectiveEndDate) {
		this.effectiveEndDate = effectiveEndDate;
	}

	public String getTags() {
		return tags;
	}
	
	public void setTags(String tags) {
		this.tags = tags;
	}
	
	public BigDecimal getErpPrice() {
		return erpPrice;
	}
	
	public void setErpPrice(BigDecimal erpPrice) {
		this.erpPrice = erpPrice;
	}
	
	public String getSegment() {
		return segment;
	}
	
	public void setSegment(String segment) {
		this.segment = segment;
	}

	@Override
	public String toString() {
		return "MicrosoftM365NCImportRecord [id=" + id + ", microsoftPriceListId=" + microsoftPriceListId
				+ ", productTitle=" + productTitle + ", productId=" + productId + ", skuId=" + skuId + ", skuTitle="
				+ skuTitle + ", publisher=" + publisher + ", skuDescription=" + skuDescription + ", unitOfMeasure="
				+ unitOfMeasure + ", termDuration=" + termDuration + ", billingPlan=" + billingPlan + ", market="
				+ market + ", currency=" + currency + ", unitPrice=" + unitPrice + ", effectiveStartDate="
				+ effectiveStartDate + ", effectiveEndDate=" + effectiveEndDate + ", tags=" + tags + ", erpPrice=" + erpPrice
				+ ", segment=" + segment + "]";
	}
	
}
