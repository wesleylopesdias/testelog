package com.logicalis.serviceinsight.data;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

public class ServiceNowCI {

	private Long id;
	private String serviceNowSysId;
	private Long contractId;
	private String contractServiceNowSysId;
	private Long contractServiceId;
	private String name;
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="MMddyyyy", timezone="America/New_York")
    private Date created;
    private String createdBy;
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="MMddyyyy", timezone="America/New_York")
    private Date updated;
    private String updatedBy;
    
    public ServiceNowCI(){}
    
	public ServiceNowCI(Long id, String serviceNowSysId, String name, Long contractId, Long contractServiceId, String contractServiceNowSysId) {
		super();
		this.id = id;
		this.serviceNowSysId = serviceNowSysId;
		this.name = name;
		this.contractId = contractId;
		this.contractServiceId = contractServiceId;
		this.contractServiceNowSysId = contractServiceNowSysId;
	}

	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getServiceNowSysId() {
		return serviceNowSysId;
	}
	
	public void setServiceNowSysId(String serviceNowSysId) {
		this.serviceNowSysId = serviceNowSysId;
	}
	
	public Long getContractId() {
		return contractId;
	}

	public void setContractId(Long contractId) {
		this.contractId = contractId;
	}
	
	public String getContractServiceNowSysId() {
		return contractServiceNowSysId;
	}

	public void setContractServiceNowSysId(String contractServiceNowSysId) {
		this.contractServiceNowSysId = contractServiceNowSysId;
	}

	public Long getContractServiceId() {
		return contractServiceId;
	}

	public void setContractServiceId(Long contractServiceId) {
		this.contractServiceId = contractServiceId;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Date getCreated() {
		return created;
	}
	
	public void setCreated(Date created) {
		this.created = created;
	}
	
	public String getCreatedBy() {
		return createdBy;
	}
	
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	
	public Date getUpdated() {
		return updated;
	}
	
	public void setUpdated(Date updated) {
		this.updated = updated;
	}
	
	public String getUpdatedBy() {
		return updatedBy;
	}
	
	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	@Override
	public String toString() {
		return "ServiceNowCI [id=" + id + ", serviceNowSysId=" + serviceNowSysId + ", contractId=" + contractId
				+ ", contractServiceNowSysId=" + contractServiceNowSysId + ", contractServiceId=" + contractServiceId
				+ ", name=" + name + ", created=" + created + ", createdBy=" + createdBy + ", updated=" + updated
				+ ", updatedBy=" + updatedBy + "]";
	}
    
}
