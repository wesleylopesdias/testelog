package com.logicalis.serviceinsight.representation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.logicalis.serviceinsight.data.Device.DeviceType;

public class APIContractService {
	
	private Long id;
	private String customerSNSysId;
	private String contractSNSysId;
	private Long contractId;
	private String ciSNSysId;
	private Long pricingSheetProductId;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MMddyyyy", timezone = "America/New_York")
	private Date startDate;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MMddyyyy", timezone = "America/New_York")
	private Date endDate;
	private String ciName;
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
	private Long correlationId;
	private Long parentId;
	private List<APIContractService> relatedLineItems = new ArrayList<APIContractService>();
	
	public APIContractService() {}
	
	public APIContractService(Long id, String customerSNSysId, String contractSNSysId, Long contractId, String ciSNSysId, String ciName, BigDecimal onetimePrice, BigDecimal recurringPrice, BigDecimal removalPrice,
			Long deviceId, String devicePartNumber, String deviceDescription, DeviceType deviceType, Long ospId, String serviceName, Date startDate, Date endDate, Integer unitCount,
			Long parentId) {
		this.id = id;
		this.customerSNSysId = customerSNSysId;
		this.contractSNSysId = contractSNSysId;
		this.contractId = contractId;
		this.ciSNSysId = ciSNSysId;
		this.ciName = ciName;
		this.onetimePrice = onetimePrice;
		this.recurringPrice = recurringPrice;
		this.removalPrice = removalPrice;
		this.deviceId = deviceId;
		this.devicePartNumber = devicePartNumber;
		this.deviceDescription = deviceDescription;
		this.deviceType = deviceType;
		this.ospId = ospId;
		this.serviceName = serviceName;
		this.startDate = startDate;
		this.endDate = endDate;
		this.unitCount = unitCount;
		this.parentId = parentId;
	}

	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getCustomerSNSysId() {
		return customerSNSysId;
	}
	
	public void setCustomerSNSysId(String customerSNSysId) {
		this.customerSNSysId = customerSNSysId;
	}
	
	public String getContractSNSysId() {
		return contractSNSysId;
	}
	
	public void setContractSNSysId(String contractSNSysId) {
		this.contractSNSysId = contractSNSysId;
	}
	
	public Long getContractId() {
		return contractId;
	}

	public void setContractId(Long contractId) {
		this.contractId = contractId;
	}

	public String getCiSNSysId() {
		return ciSNSysId;
	}
	
	public void setCiSNSysId(String ciSNSysId) {
		this.ciSNSysId = ciSNSysId;
	}
	
	public Long getPricingSheetProductId() {
		return pricingSheetProductId;
	}
	
	public void setPricingSheetProductId(Long pricingSheetProductId) {
		this.pricingSheetProductId = pricingSheetProductId;
	}
	
	public Date getStartDate() {
		return startDate;
	}
	
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	
	public Date getEndDate() {
		return endDate;
	}
	
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	
	public String getCiName() {
		return ciName;
	}
	
	public void setCiName(String ciName) {
		this.ciName = ciName;
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

	public Long getCorrelationId() {
		return correlationId;
	}

	public void setCorrelationId(Long correlationId) {
		this.correlationId = correlationId;
	}

	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

	public List<APIContractService> getRelatedLineItems() {
		return relatedLineItems;
	}

	public void setRelatedLineItems(List<APIContractService> relatedLineItems) {
		this.relatedLineItems = relatedLineItems;
	}

	@Override
	public String toString() {
		return "APIContractService [id=" + id + ", customerSNSysId=" + customerSNSysId + ", contractSNSysId="
				+ contractSNSysId + ", contractId=" + contractId + ", ciSNSysId=" + ciSNSysId
				+ ", pricingSheetProductId=" + pricingSheetProductId + ", startDate=" + startDate + ", endDate="
				+ endDate + ", ciName=" + ciName + ", operation=" + operation + ", onetimePrice=" + onetimePrice
				+ ", recurringPrice=" + recurringPrice + ", removalPrice=" + removalPrice + ", deviceId=" + deviceId
				+ ", devicePartNumber=" + devicePartNumber + ", deviceDescription=" + deviceDescription
				+ ", deviceType=" + deviceType + ", ospId=" + ospId + ", serviceName=" + serviceName + ", pcrName="
				+ pcrName + ", pcrId=" + pcrId + ", unitCount=" + unitCount + ", correlationId=" + correlationId
				+ ", parentId=" + parentId + ", relatedLineItems=" + relatedLineItems + "]";
	}

}
