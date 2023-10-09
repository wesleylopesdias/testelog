package com.logicalis.serviceinsight.data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.logicalis.serviceinsight.data.Device.BillingPlan;
import com.logicalis.serviceinsight.data.Device.TermDuration;

public class MicrosoftPriceListM365NCProduct extends MicrosoftPriceListProduct {

	private String productTitle;
	private String publisher;
	private String skuDescription;
	private Device.TermDuration termDuration;
	private Device.BillingPlan billingPlan;
	private String tags;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MMddyyyy", timezone = "America/New_York")
	private Date effectiveStartDate;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MMddyyyy", timezone = "America/New_York")
	private Date effectiveEndDate;
	
	public MicrosoftPriceListM365NCProduct() {}
	
	public MicrosoftPriceListM365NCProduct(Long id, Long microsoftPriceListId, String offerName, String offerId, 
			Device.Segment segment, BigDecimal unitPrice, BigDecimal erpPrice, String productTitle, String publisher, String skuDescription,
			TermDuration termDuration, BillingPlan billingPlan, String tags, Date effectiveStartDate,
			Date effectiveEndDate) {
		super(id, microsoftPriceListId, offerName, offerId, segment, unitPrice, erpPrice, MicrosoftPriceList.MicrosoftPriceListType.M365NC);
		this.productTitle = productTitle;
		this.skuDescription = skuDescription;
		this.publisher = publisher;
		this.termDuration = termDuration;
		this.billingPlan = billingPlan;
		this.tags = tags;
		this.effectiveStartDate = effectiveStartDate;
		this.effectiveEndDate = effectiveEndDate;
	}
	
	public String getProductTitle() {
		return productTitle;
	}
	
	public void setProductTitle(String productTitle) {
		this.productTitle = productTitle;
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

	public Device.TermDuration getTermDuration() {
		return termDuration;
	}
	
	public void setTermDuration(Device.TermDuration termDuration) {
		this.termDuration = termDuration;
	}
	
	public Device.BillingPlan getBillingPlan() {
		return billingPlan;
	}
	
	public void setBillingPlan(Device.BillingPlan billingPlan) {
		this.billingPlan = billingPlan;
	}
	
	public Device.Segment getSegment() {
		return segment;
	}
	
	public void setSegment(Device.Segment segment) {
		this.segment = segment;
	}
	
	public BigDecimal getErpPrice() {
		return erpPrice;
	}
	
	public void setErpPrice(BigDecimal erpPrice) {
		this.erpPrice = erpPrice;
	}
	
	public String getTags() {
		return tags;
	}
	
	public void setTags(String tags) {
		this.tags = tags;
	}
	
	public Date getEffectiveStartDate() {
		return effectiveStartDate;
	}
	
	public void setEffectiveStartDate(Date effectiveStartDate) {
		this.effectiveStartDate = effectiveStartDate;
	}
	
	public Date getEffectiveEndDate() {
		return effectiveEndDate;
	}
	
	public void setEffectiveEndDate(Date effectiveEndDate) {
		this.effectiveEndDate = effectiveEndDate;
	}
	
	public BigDecimal getMonthlyUnitPrice() {
		BigDecimal price = getUnitPrice();
		
		if(Device.BillingPlan.monthly.equals(getBillingPlan()) && Device.TermDuration.P1Y.equals(getTermDuration())) {
			price = getUnitPrice().divide(new BigDecimal(12), RoundingMode.HALF_UP);
		}
		
		return price;
	}
	
	public BigDecimal getMonthlyErpPrice() {
		BigDecimal price = getErpPrice();
		
		if(Device.BillingPlan.monthly.equals(getBillingPlan()) && Device.TermDuration.P1Y.equals(getTermDuration())) {
			price = getErpPrice().divide(new BigDecimal(12), RoundingMode.HALF_UP);
		}
		
		return price;
	}

	@Override
	public String toString() {
		return "MicrosoftPriceListM365NCProduct [productTitle=" + productTitle + ", publisher=" + publisher
				+ ", skuDescription=" + skuDescription + ", termDuration=" + termDuration + ", billingPlan="
				+ billingPlan + ", tags=" + tags + ", effectiveStartDate=" + effectiveStartDate + ", effectiveEndDate="
				+ effectiveEndDate + "]";
	}
	
}