package com.logicalis.serviceinsight.data;

import java.math.BigDecimal;
import java.util.Date;

public class SIDataWarehouseContract {

	public enum Type {
		rimm("RIMM"), cloud("Cloud"), csp("CSP"), other("Other"), rimmAndCloud("RIMM & Cloud"), rimmAndCSP("RIMM & CSP"), rimmAndOther("RIMM & Other"), 
		rimmCSPAndOther("RIMM, CSP, & Other"), rimmCloudCSPAndOther("RIMM, Cloud, CSP & Other"), cloudAndCSP("Cloud & CSP"), cloudAndOther("Cloud & Other"), 
		cloudCSPAndOther("Cloud, CSP & Other"), cspAndOther("CSP & Other");
		
		private String description;
		
		Type(String description) {
			this.description = description;
		}
		
		public String getDescription() {
			return this.description;
		}
	}
	
	private String customerName;
	private Long id;
	private String jobNumber;
	private String sowName;
	private String altId;
	private Date signedDate;
	private Date startDate;
	private Date serviceStartDate;
	private Date endDate;
	private String osmSysId;
	private String sdm;
	private String ae;
	private Type type;
	
	public SIDataWarehouseContract() {}
	
	public SIDataWarehouseContract(String customerName, Long id, String jobNumber, String sowName, String altId,
			Date signedDate, Date startDate, Date serviceStartDate, Date endDate, String osmSysId, String sdm,
			String ae, Type type) {
		super();
		this.customerName = customerName;
		this.id = id;
		this.jobNumber = jobNumber;
		this.sowName = sowName;
		this.altId = altId;
		this.signedDate = signedDate;
		this.startDate = startDate;
		this.serviceStartDate = serviceStartDate;
		this.endDate = endDate;
		this.osmSysId = osmSysId;
		this.sdm = sdm;
		this.ae = ae;
		this.type = type;
	}

	public String getCustomerName() {
		return customerName;
	}
	
	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getJobNumber() {
		return jobNumber;
	}
	
	public void setJobNumber(String jobNumber) {
		this.jobNumber = jobNumber;
	}
	
	public String getSowName() {
		return sowName;
	}
	
	public void setSowName(String sowName) {
		this.sowName = sowName;
	}
	
	public String getAltId() {
		return altId;
	}
	
	public void setAltId(String altId) {
		this.altId = altId;
	}
	
	public Date getSignedDate() {
		return signedDate;
	}
	
	public void setSignedDate(Date signedDate) {
		this.signedDate = signedDate;
	}
	
	public Date getStartDate() {
		return startDate;
	}
	
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	
	public Date getServiceStartDate() {
		return serviceStartDate;
	}
	
	public void setServiceStartDate(Date serviceStartDate) {
		this.serviceStartDate = serviceStartDate;
	}
	
	public Date getEndDate() {
		return endDate;
	}
	
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	
	public String getOsmSysId() {
		return osmSysId;
	}
	
	public void setOsmSysId(String osmSysId) {
		this.osmSysId = osmSysId;
	}
	
	public String getSdm() {
		return sdm;
	}
	
	public void setSdm(String sdm) {
		this.sdm = sdm;
	}
	
	public String getAe() {
		return ae;
	}
	
	public void setAe(String ae) {
		this.ae = ae;
	}
	
	public Type getType() {
		return type;
	}
	
	public void setType(Type type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "SIDataWarehouseContract [customerName=" + customerName + ", id=" + id + ", jobNumber=" + jobNumber
				+ ", sowName=" + sowName + ", altId=" + altId + ", signedDate=" + signedDate + ", startDate="
				+ startDate + ", serviceStartDate=" + serviceStartDate + ", endDate=" + endDate + ", osmSysId="
				+ osmSysId + ", sdm=" + sdm + ", ae=" + ae + ", type=" + type + "]";
	}
}
