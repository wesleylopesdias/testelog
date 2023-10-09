package com.logicalis.serviceinsight.representation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.math3.fraction.Fraction;
import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.logicalis.serviceinsight.data.Device;
import com.logicalis.serviceinsight.data.Device.DeviceType;

public class CSPBilledContractService {

	private Long contractServiceId;
	private Long serviceId;
    private Long contractId;
    private String customerName;
    private String customerAltName;
    private BigDecimal onetimeRevenue = BigDecimal.ZERO;
    private BigDecimal recurringRevenue = BigDecimal.ZERO;
    private Integer quantity;
    DateTime month;
    Integer days;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MMddyyyy", timezone = "America/New_York")
    private Date startDate;
    private DateTime startDateTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MMddyyyy", timezone = "America/New_York")
    private Date endDate;
    private DateTime endDateTime;
    private String devicePartNumber;
    private String deviceDescription;
    private Integer deviceUnitCount;
    private Device.DeviceType deviceType;
    private Long deviceId;
    private int CALC_SCALE = 20;
    private Long contractServiceExternalId;
    private String externalSubscriptionId;
	
    public CSPBilledContractService(){}
    
    public CSPBilledContractService(Long contractServiceId, Long contractId, String customerName, String customerAltName, BigDecimal onetimeRevenue, BigDecimal recurringRevenue, Integer quantity, Date sd,
			Date ed, String devicePartNumber, String deviceDescription, Integer deviceUnitCount, DeviceType deviceType, Long deviceId, Long contractServiceExternalAzureId, String externalSubscriptionId) {
		super();
		this.contractServiceId = contractServiceId;
		this.contractId = contractId;
		this.customerName = customerName;
		this.customerAltName = customerAltName;
		this.onetimeRevenue = (onetimeRevenue == null ? BigDecimal.ZERO : onetimeRevenue);
		this.recurringRevenue = (recurringRevenue == null ?  BigDecimal.ZERO : recurringRevenue);
		this.quantity = quantity;
		this.startDate = sd;
		if (sd != null) {
            this.startDateTime = new DateTime(sd);
        }
        this.endDate = ed;
        if (ed != null) {
            this.endDateTime = new DateTime(ed);
        }
		this.devicePartNumber = devicePartNumber;
		this.deviceDescription = deviceDescription;
		this.deviceUnitCount = deviceUnitCount;
		this.deviceType = deviceType;
		this.deviceId = deviceId;
		this.contractServiceExternalId = contractServiceExternalId;
		this.externalSubscriptionId = externalSubscriptionId;
	}
    
    /*Rollup*/
    public CSPBilledContractService(DateTime month, Long contractId, String customerName, String customerAltName, BigDecimal onetimeRevenue, BigDecimal recurringRevenue, Integer quantity, Date sd,
			Date ed, String devicePartNumber, String deviceDescription, Integer deviceUnitCount, DeviceType deviceType, Long deviceId, Long contractServiceExternalId, String externalSubscriptionId) {
		super();
		this.month = month;
		if (this.month != null) {
            this.days = this.month.dayOfMonth().getMaximumValue();
        }
		this.contractServiceId = null;
		this.contractId = contractId;
		this.customerName = customerName;
		this.customerAltName = customerAltName;
		this.onetimeRevenue = onetimeRevenue;
		this.recurringRevenue = recurringRevenue;
		this.quantity = quantity;
		this.startDate = sd;
		if (sd != null) {
            this.startDateTime = new DateTime(sd);
        }
        this.endDate = ed;
        if (ed != null) {
            this.endDateTime = new DateTime(ed);
        }
		this.devicePartNumber = devicePartNumber;
		this.deviceDescription = deviceDescription;
		this.deviceUnitCount = deviceUnitCount;
		this.deviceType = deviceType;
		this.deviceId = deviceId;
		this.contractServiceExternalId = contractServiceExternalId;
		this.externalSubscriptionId = externalSubscriptionId;
	}

	public Long getContractServiceId() {
		return contractServiceId;
	}
	
	public void setContractServiceId(Long contractServiceId) {
		this.contractServiceId = contractServiceId;
	}
	
	public Long getServiceId() {
		return serviceId;
	}
	
	public void setServiceId(Long serviceId) {
		this.serviceId = serviceId;
	}
	
	public Long getContractId() {
		return contractId;
	}
	
	public void setContractId(Long contractId) {
		this.contractId = contractId;
	}
	
	public String getCustomerName() {
		return customerName;
	}
	
	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}
	
	public String getCustomerAltName() {
		return customerAltName;
	}

	public void setCustomerAltName(String customerAltName) {
		this.customerAltName = customerAltName;
	}

	public Double getMonthFraction() {
        if (month != null) {
            DateTime leftDate = this.month; // should be first day of the month...
            if (startDateTime.isAfter(leftDate)) {
                leftDate = startDateTime;
            }
            DateTime rightDate = this.month.dayOfMonth().withMaximumValue();
            if (endDateTime != null && endDateTime.isBefore(rightDate)) {
                rightDate = endDateTime;
            }
            return new Fraction((rightDate.dayOfMonth().get() - leftDate.dayOfMonth().get() + 1), this.days).doubleValue();
        }
        return null;
    }
	
	public BigDecimal getOnetimeRevenue() {
        if (month != null) {
            if (startDateTime.getYear() == month.getYear()
                    && startDateTime.getMonthOfYear() == month.getMonthOfYear()) {
                return onetimeRevenue;
            }
            return new BigDecimal(0); // onetimeRevenue only applies in the first month
        }
        return onetimeRevenue;
    }
	
	public void setOnetimeRevenue(BigDecimal onetimeRevenue) {
		this.onetimeRevenue = onetimeRevenue;
	}
	
	public BigDecimal getRecurringRevenue() {
        if (recurringRevenue == null) {
            return new BigDecimal(0);
        }
        if (month != null) {
            return recurringRevenue
                    .multiply(new BigDecimal(getMonthFraction()));
        }
        return recurringRevenue;
    }
	
	public void setRecurringRevenue(BigDecimal recurringRevenue) {
		this.recurringRevenue = recurringRevenue;
	}
	
	public Integer getQuantity() {
		return quantity;
	}
	
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
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
	
	public Integer getDeviceUnitCount() {
		return deviceUnitCount;
	}
	
	public void setDeviceUnitCount(Integer deviceUnitCount) {
		this.deviceUnitCount = deviceUnitCount;
	}
	
	public Device.DeviceType getDeviceType() {
		return deviceType;
	}
	
	public void setDeviceType(Device.DeviceType deviceType) {
		this.deviceType = deviceType;
	}
	
	public Long getDeviceId() {
		return deviceId;
	}
	
	public void setDeviceId(Long deviceId) {
		this.deviceId = deviceId;
	}
	
	public Long getContractServiceExternalId() {
		return contractServiceExternalId;
	}
	
	public void setContractServiceExternalId(Long contractServiceExternalId) {
		this.contractServiceExternalId = contractServiceExternalId;
	}
	
	public String getExternalSubscriptionId() {
		return externalSubscriptionId;
	}
	
	public void setExternalSubscriptionId(String externalSubscriptionId) {
		this.externalSubscriptionId = externalSubscriptionId;
	}
	
}
