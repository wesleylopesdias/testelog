package com.logicalis.serviceinsight.data;

import java.math.BigDecimal;

public class Microsoft365SubscriptionConfig {

	public enum Type {
		M365("M365"), O365("O365");
		
		Type(String description) {
			this.description = description;
		}
		
		private String description;
		
		public String getDescription() {
			return description;
		}
	}
	
	public enum SupportType {
		flat("Flat Fee"), percent("Percentage of MRC");
		
		SupportType(String description) {
			this.description = description;
		}
		
		private String description;
		
		public String getDescription() {
			return description;
		}
	}
	
	private Long id;
	private Long contractId;
	private Long deviceId;
	private String deviceDescription;
	private String devicePartNumber;
	private Long serviceId;
	private String serviceName;
	private String tenantId;
	private Type type;
	private SupportType supportType;
	private BigDecimal flatFee;
	private BigDecimal percent;
	private Boolean active = Boolean.TRUE;
	
	public Microsoft365SubscriptionConfig() {}
	
	public Microsoft365SubscriptionConfig(Long id, Long contractId, Long deviceId, String deviceDescription, String devicePartNumber, Long serviceId,
			String serviceName, String tenantId, Type type, SupportType supportType,
			BigDecimal flatFee, BigDecimal percent, Boolean active) {
		super();
		this.id = id;
		this.contractId = contractId;
		this.deviceId = deviceId;
		this.deviceDescription = deviceDescription;
		this.devicePartNumber = devicePartNumber;
		this.serviceId = serviceId;
		this.serviceName = serviceName;
		this.tenantId = tenantId;
		this.type = type;
		this.supportType = supportType;
		this.flatFee = flatFee;
		this.percent = percent;
		this.active = active;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getContractId() {
		return contractId;
	}

	public void setContractId(Long contractId) {
		this.contractId = contractId;
	}

	public Long getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(Long deviceId) {
		this.deviceId = deviceId;
	}

	public String getDeviceDescription() {
		return deviceDescription;
	}

	public void setDeviceDescription(String deviceDescription) {
		this.deviceDescription = deviceDescription;
	}

	public String getDevicePartNumber() {
		return devicePartNumber;
	}

	public void setDevicePartNumber(String devicePartNumber) {
		this.devicePartNumber = devicePartNumber;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public Long getServiceId() {
		return serviceId;
	}

	public void setServiceId(Long serviceId) {
		this.serviceId = serviceId;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public SupportType getSupportType() {
		return supportType;
	}

	public void setSupportType(SupportType supportType) {
		this.supportType = supportType;
	}

	public BigDecimal getFlatFee() {
		return flatFee;
	}

	public void setFlatFee(BigDecimal flatFee) {
		this.flatFee = flatFee;
	}

	public BigDecimal getPercent() {
		return percent;
	}

	public void setPercent(BigDecimal percent) {
		this.percent = percent;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	@Override
	public String toString() {
		return "Microsoft365SubscriptionConfig [id=" + id + ", contractId=" + contractId + ", deviceId=" + deviceId
				+ ", deviceDescription=" + deviceDescription + ", devicePartNumber=" + devicePartNumber + ", serviceId="
				+ serviceId + ", serviceName=" + serviceName + ", tenantId=" + tenantId + ", type=" + type
				+ ", supportType=" + supportType + ", flatFee=" + flatFee + ", percent=" + percent + ", active="
				+ active + "]";
	}
	
}
