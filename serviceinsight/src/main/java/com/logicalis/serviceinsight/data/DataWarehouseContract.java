package com.logicalis.serviceinsight.data;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

public class DataWarehouseContract {

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy", timezone = "America/New_York")
	private Date month;
	private String customer;
	private Long id;
	private String jobNumber;
	private String sowName;
	private String altId;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy", timezone = "America/New_York")
	private Date signedDate;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy", timezone = "America/New_York")
	private Date startDate;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy", timezone = "America/New_York")
	private Date serviceStartDate;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy", timezone = "America/New_York")
	private Date endDate;
	private String osmSysId;
	private String sdm;
	private String ae;
	private String type;
	
	public DataWarehouseContract() {}
	
	public DataWarehouseContract(Date month, String customer, Long id, String jobNumber, String sowName, String altId,
			Date signedDate, Date startDate, Date serviceStartDate, Date endDate, String osmSysId, String sdm,
			String ae, String type) {
		super();
		this.month = month;
		this.customer = customer;
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

	public Date getMonth() {
		return month;
	}
	
	public void setMonth(Date month) {
		this.month = month;
	}
	
	public String getCustomer() {
		return customer;
	}
	
	public void setCustomer(String customer) {
		this.customer = customer;
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
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "DataWarehouseContract [month=" + month + ", customer=" + customer + ", id=" + id + ", jobNumber="
				+ jobNumber + ", sowName=" + sowName + ", altId=" + altId + ", signedDate=" + signedDate
				+ ", startDate=" + startDate + ", serviceStartDate=" + serviceStartDate + ", endDate=" + endDate
				+ ", osmSysId=" + osmSysId + ", sdm=" + sdm + ", ae=" + ae + ", type=" + type + "]";
	}
	
	
	
}
