package com.logicalis.serviceinsight.data;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.logicalis.serviceinsight.dao.BaseDao;
import com.logicalis.serviceinsight.data.Device.DeviceType;

public class PricingSheetProduct extends BaseDao {

	public enum Status {
		active("active"), inactive("inactive"), error("error");
        private String description;

        Status(String description) {
            this.description = description;
        }

        public String getDescription() {
            return this.description;
        }
    }
	
	private Long id;
	private Long pricingSheetId;
	private Long deviceId;
	private String devicePartNumber;
	private String deviceDescription;
	private DeviceType deviceType;
	private String deviceAltId;
	private String serviceName;
	private Long serviceId;
	private BigDecimal onetimePrice;
	private BigDecimal recurringPrice;
	private BigDecimal removalPrice;
	private BigDecimal erpPrice;
	private BigDecimal discount;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MMddyyyy", timezone = "America/New_York")
	private Date subscriptionStartDate;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MMddyyyy", timezone = "America/New_York")
	private Date subscriptionEndDate;
	private Integer unitCount = 0;
	private Integer previousUnitCount = 0;
	private Status status;
	private String statusMessage;
	private Boolean manualOverride = Boolean.FALSE;
	private String notes;
	
	public PricingSheetProduct(){}
	
	public PricingSheetProduct(Long id, Long pricingSheetId, Long deviceId, String devicePartNumber, String deviceDescription, DeviceType deviceType, 
			String deviceAltId, Long serviceId, String serviceName, BigDecimal onetimePrice, BigDecimal recurringPrice, BigDecimal removalPrice, BigDecimal erpPrice, 
			BigDecimal discount, Date subscriptionStartDate, Date subscriptionEndDate, Integer unitCount, Integer previousUnitCount, Status status, String statusMessage, Boolean manualOverride) {
		this.id = id;
		this.pricingSheetId = pricingSheetId;
		this.deviceId = deviceId;
		this.devicePartNumber = devicePartNumber;
		this.deviceDescription = deviceDescription;
		this.deviceType = deviceType;
		this.deviceAltId = deviceAltId;
		this.serviceName = serviceName;
		this.serviceId = serviceId;
		this.onetimePrice = onetimePrice;
		this.recurringPrice = recurringPrice;
		this.removalPrice = removalPrice;
		this.erpPrice = erpPrice;
		this.discount = discount;
		this.subscriptionStartDate = subscriptionStartDate;
		this.subscriptionEndDate = subscriptionEndDate;
		this.unitCount = unitCount;
		this.previousUnitCount = previousUnitCount;
		this.status = status;
		this.statusMessage = statusMessage;
		this.manualOverride = manualOverride;
	}

	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public Long getPricingSheetId() {
		return pricingSheetId;
	}
	
	public void setPricingSheetId(Long pricingSheetId) {
		this.pricingSheetId = pricingSheetId;
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

	public String getDeviceAltId() {
		return deviceAltId;
	}

	public void setDeviceAltId(String deviceAltId) {
		this.deviceAltId = deviceAltId;
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
	
	public BigDecimal getOnetimePrice() {
		return onetimePrice.setScale(2, BigDecimal.ROUND_HALF_UP);
	}
	
	public void setOnetimePrice(BigDecimal onetimePrice) {
		this.onetimePrice = onetimePrice;
	}
	
	public BigDecimal getRecurringPrice() {
		return recurringPrice;
	}
	
	public void setRecurringPrice(BigDecimal recurringPrice) {
		this.recurringPrice = recurringPrice.setScale(2, BigDecimal.ROUND_HALF_UP);
	}

	public BigDecimal getRemovalPrice() {
		return removalPrice;
	}

	public void setRemovalPrice(BigDecimal removalPrice) {
		this.removalPrice = removalPrice;
	}

	public BigDecimal getErpPrice() {
		return erpPrice;
	}

	public void setErpPrice(BigDecimal erpPrice) {
		this.erpPrice = erpPrice;
	}

	public BigDecimal getDiscount() {
		return discount;
	}

	public void setDiscount(BigDecimal discount) {
		this.discount = discount;
	}

	public Date getSubscriptionStartDate() {
		return subscriptionStartDate;
	}

	public void setSubscriptionStartDate(Date subscriptionStartDate) {
		this.subscriptionStartDate = subscriptionStartDate;
	}

	public Date getSubscriptionEndDate() {
		return subscriptionEndDate;
	}

	public void setSubscriptionEndDate(Date subscriptionEndDate) {
		this.subscriptionEndDate = subscriptionEndDate;
	}

	public Integer getUnitCount() {
		return unitCount;
	}

	public void setUnitCount(Integer unitCount) {
		this.unitCount = unitCount;
	}

	public Integer getPreviousUnitCount() {
		return previousUnitCount;
	}

	public void setPreviousUnitCount(Integer previousUnitCount) {
		this.previousUnitCount = previousUnitCount;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getStatusMessage() {
		return statusMessage;
	}

	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	}

	public Boolean getManualOverride() {
		return manualOverride;
	}

	public void setManualOverride(Boolean manualOverride) {
		this.manualOverride = manualOverride;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}
	
}
