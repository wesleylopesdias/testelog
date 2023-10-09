package com.logicalis.serviceinsight.data;

import java.math.BigDecimal;

public class MicrosoftM365ImportRecord {

	public enum Operation {
		UNC, ADD, DEL, CHG, DEPR
	}
	
	private String operation;
	private String offerName;
	private String offerId;
	private String licenseAgreementType;
	private String purchase;
	private String secondaryLicenseType;
	private String endCustomerType;
	private BigDecimal listPrice;
	private BigDecimal erpPrice;
	private String material;
	
	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
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
	
	public String getLicenseAgreementType() {
		return licenseAgreementType;
	}
	
	public void setLicenseAgreementType(String licenseAgreementType) {
		this.licenseAgreementType = licenseAgreementType;
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
	
	public BigDecimal getListPrice() {
		return listPrice;
	}
	
	public void setListPrice(BigDecimal listPrice) {
		this.listPrice = listPrice;
	}
	
	public BigDecimal getErpPrice() {
		return erpPrice;
	}
	
	public void setErpPrice(BigDecimal erpPrice) {
		this.erpPrice = erpPrice;
	}
	
	public String getMaterial() {
		return material;
	}
	
	public void setMaterial(String material) {
		this.material = material;
	}

	@Override
	public String toString() {
		return "MicrosoftM365ImportRecord [operation=" + operation + ", offerName=" + offerName + ", offerId=" + offerId
				+ ", licenseAgreementType=" + licenseAgreementType + ", purchase=" + purchase
				+ ", secondaryLicenseType=" + secondaryLicenseType + ", endCustomerType=" + endCustomerType
				+ ", listPrice=" + listPrice + ", erpPrice=" + erpPrice + ", material=" + material + "]";
	}
	
}
