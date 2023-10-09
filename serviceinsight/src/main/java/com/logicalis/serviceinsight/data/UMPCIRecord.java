package com.logicalis.serviceinsight.data;

import java.math.BigDecimal;

public class UMPCIRecord {

	private String jobNumber;
	private String contractSNSysId;
	private String companyName;
	private String companySNSysId;
	private String ciName;
	private String ciSNSysId;
	private String location;
	private String operatingSystem;
	private Integer cpuCount;
	private BigDecimal memoryGB;
	private BigDecimal storageGB;
	
	public UMPCIRecord(){}
	
	public UMPCIRecord(String jobNumber, String contractSNSysId,
			String companyName, String companySNSysId, String ciName,
			String ciSNSysId, String location, String operatingSystem,
			Integer cpuCount, BigDecimal memoryGB, BigDecimal storageGB) {
		super();
		this.jobNumber = jobNumber;
		this.contractSNSysId = contractSNSysId;
		this.companyName = companyName;
		this.companySNSysId = companySNSysId;
		this.ciName = ciName;
		this.ciSNSysId = ciSNSysId;
		this.location = location;
		this.operatingSystem = operatingSystem;
		this.cpuCount = cpuCount;
		this.memoryGB = memoryGB;
		this.storageGB = storageGB;
	}



	public String getJobNumber() {
		return jobNumber;
	}
	
	public void setJobNumber(String jobNumber) {
		this.jobNumber = jobNumber;
	}
	
	public String getContractSNSysId() {
		return contractSNSysId;
	}
	
	public void setContractSNSysId(String contractSNSysId) {
		this.contractSNSysId = contractSNSysId;
	}
	
	public String getCompanyName() {
		return companyName;
	}
	
	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
	
	public String getCompanySNSysId() {
		return companySNSysId;
	}
	
	public void setCompanySNSysId(String companySNSysId) {
		this.companySNSysId = companySNSysId;
	}
	
	public String getCiName() {
		return ciName;
	}
	
	public void setCiName(String ciName) {
		this.ciName = ciName;
	}
	
	public String getCiSNSysId() {
		return ciSNSysId;
	}
	
	public void setCiSNSysId(String ciSNSysId) {
		this.ciSNSysId = ciSNSysId;
	}
	
	public String getLocation() {
		return location;
	}
	
	public void setLocation(String location) {
		this.location = location;
	}
	
	public String getOperatingSystem() {
		return operatingSystem;
	}
	
	public void setOperatingSystem(String operatingSystem) {
		this.operatingSystem = operatingSystem;
	}
	
	public Integer getCpuCount() {
		return cpuCount;
	}
	
	public void setCpuCount(Integer cpuCount) {
		this.cpuCount = cpuCount;
	}
	
	public BigDecimal getMemoryGB() {
		return memoryGB;
	}
	
	public void setMemoryGB(BigDecimal memoryGB) {
		this.memoryGB = memoryGB;
	}
	
	public BigDecimal getStorageGB() {
		return storageGB;
	}
	
	public void setStorageGB(BigDecimal storageGB) {
		this.storageGB = storageGB;
	}
	
	
	
}
