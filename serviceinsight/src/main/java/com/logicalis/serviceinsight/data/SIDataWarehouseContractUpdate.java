package com.logicalis.serviceinsight.data;

import java.util.Date;

import com.logicalis.serviceinsight.data.SIDataWarehouseContract.Type;

public class SIDataWarehouseContractUpdate {

	private String customerName;
	private Long sowId;
	private String jobNumber;
	private String sowName;
	private String sowAltId;
	private Long id;
	private String altId;
	private Date signedDate;
	private Date effectiveDate;
	private String note;
	private Type type;
	
	public SIDataWarehouseContractUpdate() {}
	
	public SIDataWarehouseContractUpdate(String customerName, Long sowId, String jobNumber, String sowName,
			String sowAltId, Long id, String altId, Date signedDate, Date effectiveDate, String note, Type type) {
		super();
		this.customerName = customerName;
		this.sowId = sowId;
		this.jobNumber = jobNumber;
		this.sowName = sowName;
		this.sowAltId = sowAltId;
		this.id = id;
		this.altId = altId;
		this.signedDate = signedDate;
		this.effectiveDate = effectiveDate;
		this.note = note;
		this.type = type;
	}

	public String getCustomerName() {
		return customerName;
	}
	
	public void setCustomerName(String customerName) {
		this.customerName = customerName;
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
	
	public Type getType() {
		return type;
	}
	
	public void setType(Type type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "SIDataWarehouseContractUpdate [customerName=" + customerName + ", sowId=" + sowId + ", jobNumber="
				+ jobNumber + ", sowName=" + sowName + ", sowAltId=" + sowAltId + ", id=" + id + ", altId=" + altId
				+ ", signedDate=" + signedDate + ", effectiveDate=" + effectiveDate + ", note=" + note + ", type="
				+ type + "]";
	}
	
	
	
}
