package com.logicalis.serviceinsight.data;

import java.math.BigDecimal;

public class ContractServiceDetail {

	private Long contractServiceId;
	private String location;
	private String operatingSystem;
	private Integer cpuCount;
	private BigDecimal memoryGB;
	private BigDecimal storageGB;
	
	public ContractServiceDetail(){}
	
	public ContractServiceDetail(Long contractServiceId, String location, String operatingSystem, Integer cpuCount, BigDecimal memoryGB, BigDecimal storageGB) {
		super();
		this.contractServiceId = contractServiceId;
		this.location = location;
		this.operatingSystem = operatingSystem;
		this.cpuCount = cpuCount;
		this.memoryGB = memoryGB;
		this.storageGB = storageGB;
	}

	public Long getContractServiceId() {
		return contractServiceId;
	}
	
	public void setContractServiceId(Long contractServiceId) {
		this.contractServiceId = contractServiceId;
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
