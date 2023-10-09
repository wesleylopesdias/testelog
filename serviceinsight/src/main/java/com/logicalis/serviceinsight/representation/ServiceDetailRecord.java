package com.logicalis.serviceinsight.representation;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import org.apache.commons.math3.fraction.Fraction;
import org.joda.time.DateTime;

/**
 * Represents the a record of a query to return Contract Service details for the "Service Details" report
 * 
 * @author poneil
 */
public class ServiceDetailRecord {
    
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="MM/dd/yyyy", timezone="America/New_York")
    private DateTime queryMonth;
    private Integer days;
    private String customerName;
    private Long customerId;
    private Long contractId;
    private String contractJobNumber;
    private String contractName;
    private String engagementManager;
    private String serviceName;
    private Long ospId;
    private String deviceDescription;
    private String devicePartNumber;
    private Long deviceId;
    private Integer quantity;
    private Integer unitCount;
    private BigDecimal recurringRevenue;
    private BigDecimal onetimeRevenue;
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="MM/dd/yyyy", timezone="America/New_York")
    private DateTime startDate;
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="MM/dd/yyyy", timezone="America/New_York")
    private DateTime endDate;

    /**
     * default CTOR
     */
    public ServiceDetailRecord() {
    }

    /**
     * All parameters are REQUIRED to have values
     * 
     * @param queryMonth
     * @param customerName
     * @param customerId
     * @param contractJobNumber
     * @param contractName
     * @param serviceName
     * @param ospId
     * @param deviceDescription
     * @param devicePartNumber
     * @param quantity
     * @param onetimeRevenue
     * @param recurringRevenue
     * @param startDate
     * @param endDate 
     */
    public ServiceDetailRecord(DateTime queryMonth, String customerName, Long customerId, Long contractId, String contractJobNumber, String contractName, String engagementManager, String serviceName, Long ospId,
            String deviceDescription, String devicePartNumber, Long deviceId, Integer quantity, Integer unitCount, BigDecimal onetimeRevenue, BigDecimal recurringRevenue, DateTime startDate, DateTime endDate) {
        this.queryMonth = queryMonth;
        this.days = this.queryMonth.dayOfMonth().getMaximumValue();
        this.customerName = customerName;
        this.customerId = customerId;
        this.contractId = contractId;
        this.contractJobNumber = contractJobNumber;
        this.contractName = contractName;
        this.engagementManager = engagementManager;
        this.serviceName = serviceName;
        this.ospId = ospId;
        this.deviceDescription = deviceDescription;
        this.devicePartNumber = devicePartNumber;
        this.deviceId = deviceId;
        this.quantity = quantity;
        this.unitCount = unitCount;
        this.onetimeRevenue = onetimeRevenue;
        this.recurringRevenue = recurringRevenue;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public DateTime getQueryMonth() {
        return queryMonth;
    }

    public void setQueryMonth(DateTime queryMonth) {
        this.queryMonth = queryMonth;
    }
    
    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

	public Long getContractId() {
		return contractId;
	}

	public void setContractId(Long contractId) {
		this.contractId = contractId;
	}

	public String getContractJobNumber() {
        return contractJobNumber;
    }

    public void setContractJobNumber(String contractJobNumber) {
        this.contractJobNumber = contractJobNumber;
    }

    public String getContractName() {
        return contractName;
    }

    public void setContractName(String contractName) {
        this.contractName = contractName;
    }

    public String getEngagementManager() {
		return engagementManager;
	}

	public void setEngagementManager(String engagementManager) {
		this.engagementManager = engagementManager;
	}

	public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Long getOspId() {
        return ospId;
    }

    public void setOspId(Long ospId) {
        this.ospId = ospId;
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

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getUnitCount() {
		return unitCount;
	}

	public void setUnitCount(Integer unitCount) {
		this.unitCount = unitCount;
	}

	public BigDecimal getRecurringRevenue() {
        return recurringRevenue;
    }

    public BigDecimal getAppliedRecurringRevenue() {
        return recurringRevenue
                .multiply(new BigDecimal(getMonthFraction()));
    }

    public void setRecurringRevenue(BigDecimal recurringRevenue) {
        this.recurringRevenue = recurringRevenue;
    }

    public BigDecimal getOnetimeRevenue() {
        return onetimeRevenue;
    }

    public BigDecimal getAppliedOnetimeRevenue() {
        DateTime startDateTime = new DateTime(startDate);
        if (startDateTime.getYear() == queryMonth.getYear()
                && startDateTime.getMonthOfYear() == queryMonth.getMonthOfYear()) {
            return onetimeRevenue;
        }
        return new BigDecimal(0); // onetimeRevenue only applies in the first month
    }

    public void setOnetimeRevenue(BigDecimal onetimeRevenue) {
        this.onetimeRevenue = onetimeRevenue;
    }

    public DateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(DateTime startDate) {
        this.startDate = startDate;
    }

    public DateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(DateTime endDate) {
        this.endDate = endDate;
    }

    public Double getMonthFraction() {
        DateTime leftDate = this.queryMonth; // should be first day of the month...
        if (startDate.isAfter(leftDate)) {
            leftDate = startDate;
        }
        DateTime rightDate = this.queryMonth.dayOfMonth().withMaximumValue();
        if (endDate != null && endDate.isBefore(rightDate)) {
            rightDate = endDate;
        }
        return new Fraction((rightDate.dayOfMonth().get() - leftDate.dayOfMonth().get() + 1), this.days).doubleValue();
    }

}
