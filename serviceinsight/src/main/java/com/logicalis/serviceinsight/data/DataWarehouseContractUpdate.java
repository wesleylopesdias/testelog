package com.logicalis.serviceinsight.data;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

public class DataWarehouseContractUpdate {

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy", timezone = "America/New_York")
	private Date month;
	private String customer;
	private Long sowId;
	private String jobNumber;
	private String sowName;
	private String sowAltId;
	private Long id;
	private String pcrAltId;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy", timezone = "America/New_York")
	private Date signedDate;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy", timezone = "America/New_York")
	private Date effectiveDate;
	private String note;
	private String type;
	
	public DataWarehouseContractUpdate() {}
	
	public DataWarehouseContractUpdate(Date month, String customer, Long sowId, String jobNumber, String sowName,
			String sowAltId, Long id, String pcrAltId, Date signedDate, Date effectiveDate, String note, String type) {
		super();
		this.month = month;
		this.customer = customer;
		this.sowId = sowId;
		this.jobNumber = jobNumber;
		this.sowName = sowName;
		this.sowAltId = sowAltId;
		this.id = id;
		this.pcrAltId = pcrAltId;
		this.signedDate = signedDate;
		this.effectiveDate = effectiveDate;
		this.note = note;
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
	
	public Long getSowId() {
		return sowId;
	}
	
	public void setSowId(Long sowId) {
		this.sowId = sowId;
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
	
	public String getSowAltId() {
		return sowAltId;
	}
	
	public void setSowAltId(String sowAltId) {
		this.sowAltId = sowAltId;
	}
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getPcrAltId() {
		return pcrAltId;
	}
	
	public void setPcrAltId(String pcrAltId) {
		this.pcrAltId = pcrAltId;
	}
	
	public Date getSignedDate() {
		return signedDate;
	}
	
	public void setSignedDate(Date signedDate) {
		this.signedDate = signedDate;
	}
	
	public Date getEffectiveDate() {
		return effectiveDate;
	}
	
	public void setEffectiveDate(Date effectiveDate) {
		this.effectiveDate = effectiveDate;
	}
	
	public String getNote() {
		return note;
	}
	
	public void setNote(String note) {
		this.note = note;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "DataWarehouseContractUpdate [month=" + month + ", customer=" + customer + ", sowId=" + sowId
				+ ", jobNumber=" + jobNumber + ", sowName=" + sowName + ", sowAltId=" + sowAltId + ", id=" + id
				+ ", pcrAltId=" + pcrAltId + ", signedDate=" + signedDate + ", effectiveDate=" + effectiveDate
				+ ", note=" + note + ", type=" + type + "]";
	}
	
}
