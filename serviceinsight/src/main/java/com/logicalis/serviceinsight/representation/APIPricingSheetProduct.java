package com.logicalis.serviceinsight.representation;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.logicalis.serviceinsight.data.Device.DeviceType;

public class APIPricingSheetProduct {

	public enum ChangeType {
		increase, decrease
	}
	
	private Long id;
	private Long contractId;
	private String operation;
	private BigDecimal onetimePrice;
	private BigDecimal recurringPrice;
	private BigDecimal removalPrice;
	private Long deviceId;
	private String devicePartNumber;
	private String deviceDescription;
	private DeviceType deviceType;
	private Long ospId;
	private String serviceName;
	private String pcrName;
	private Long pcrId;
	private Integer unitCount;
	private Integer unitCountChange;
	private ChangeType changeType;
	private Long correlationId;
	
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
	
	public String getOperation() {
		return operation;
	}
	
	public void setOperation(String operation) {
		this.operation = operation;
	}
	
	public BigDecimal getOnetimePrice() {
		return onetimePrice;
	}
	
	public void setOnetimePrice(BigDecimal onetimePrice) {
		this.onetimePrice = onetimePrice;
	}
	
	public BigDecimal getRecurringPrice() {
		return recurringPrice;
	}
	
	public void setRecurringPrice(BigDecimal recurringPrice) {
		this.recurringPrice = recurringPrice;
	}
	
	public BigDecimal getRemovalPrice() {
		return removalPrice;
	}
	
	public void setRemovalPrice(BigDecimal removalPrice) {
		this.removalPrice = removalPrice;
	}
	
	public Long getDeviceId() {
		return deviceId;
	}
	
	public void setDeviceId(Long deviceId) {
		this.deviceId = deviceId;
	}
	
	public String getDevicePartNumber() {
		return devicePartNumber;
	}
	
	public void setDevicePartNumber(String devicePartNumber) {
		this.devicePartNumber = devicePartNumber;
	}
	
	public String getDeviceDescription() {
		return deviceDescription;
	}
	
	public void setDeviceDescription(String deviceDescription) {
		this.deviceDescription = deviceDescription;
	}
	
	public DeviceType getDeviceType() {
		return deviceType;
	}
	
	public void setDeviceType(DeviceType deviceType) {
		this.deviceType = deviceType;
	}
	
	public Long getOspId() {
		return ospId;
	}
	
	public void setOspId(Long ospId) {
		this.ospId = ospId;
	}
	
	public String getServiceName() {
		return serviceName;
	}
	
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	
	public String getPcrName() {
		return pcrName;
	}
	
	public void setPcrName(String pcrName) {
		this.pcrName = pcrName;
	}
	
	public Long getPcrId() {
		return pcrId;
	}
	
	public void setPcrId(Long pcrId) {
		this.pcrId = pcrId;
	}
	
	public Integer getUnitCount() {
		return unitCount;
	}
	
	public void setUnitCount(Integer unitCount) {
		this.unitCount = unitCount;
	}
	
	public Integer getUnitCountChange() {
		return unitCountChange;
	}

	public void setUnitCountChange(Integer unitCountChange) {
		this.unitCountChange = unitCountChange;
	}

	public ChangeType getChangeType() {
		return changeType;
	}

	public void setChangeType(ChangeType changeType) {
		this.changeType = changeType;
	}

	public Long getCorrelationId() {
		return correlationId;
	}
	
	public void setCorrelationId(Long correlationId) {
		this.correlationId = correlationId;
	}

	@Override
	public String toString() {
		return "APIPricingSheetProduct [id=" + id + ", contractId=" + contractId + ", operation=" + operation
				+ ", onetimePrice=" + onetimePrice + ", recurringPrice=" + recurringPrice + ", removalPrice="
				+ removalPrice + ", deviceId=" + deviceId + ", devicePartNumber=" + devicePartNumber
				+ ", deviceDescription=" + deviceDescription + ", deviceType=" + deviceType + ", ospId=" + ospId
				+ ", serviceName=" + serviceName + ", pcrName=" + pcrName + ", pcrId=" + pcrId + ", unitCount="
				+ unitCount + ", unitCountChange=" + unitCountChange + ", changeType=" + changeType + ", correlationId="
				+ correlationId + "]";
	}
	
	
	
}
