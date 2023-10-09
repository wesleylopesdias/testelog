package com.logicalis.serviceinsight.data;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

import com.logicalis.serviceinsight.data.Service.ServiceType;

public class ContractServiceDashboardItem {

    private static final String BLANK_JOBNUMBER = "N/A";

    // Customer
    private Long customerId;
    private String customerName;

    // Contract
    private Long contractId;
    private String contractName;
    private String jobNumber;
    private Date contractStart;
    
    // Service
    private Long ospId;
    private String serviceName;
    private ServiceType serviceType;
    private String deviceType;
    private BigDecimal msRecurringRevenue;
    private BigDecimal msTotalRevenue;
    private BigDecimal cloudRecurringRevenue;
    private BigDecimal cloudTotalRevenue;
    private BigDecimal cspRecurringRevenue;
    private BigDecimal cspTotalRevenue;
    private BigDecimal otherRecurringRevenue;
    private BigDecimal otherTotalRevenue;
    
    // Adjustment
    private BigDecimal contractRecurringAdjustment;
    private BigDecimal contractTotalAdjustment;

    public ContractServiceDashboardItem(){}

    public ContractServiceDashboardItem(Long contractId, String contractName, String jobNumber, Date contractStart,
                                        Long customerId, String customerName, Long ospId, String serviceName,BigDecimal msRecurringRevenue,  
                                        BigDecimal msTotalRevenue, BigDecimal cloudRecurringRevenue, BigDecimal cloudTotalRevenue,
                                        BigDecimal cspRecurringRevenue, BigDecimal cspTotalRevenue,
                                        BigDecimal otherRecurringRevenue, BigDecimal otherTotalRevenue,
                                        ServiceType serviceType, String deviceType) {
        super();
        this.customerId = customerId;
        this.customerName = customerName;
        this.contractId = contractId;
        this.contractName = contractName;
        this.jobNumber = (jobNumber==null ? BLANK_JOBNUMBER : jobNumber);
        this.contractStart = contractStart;
        this.ospId = ospId;
        this.serviceName = serviceName;
        this.serviceType = serviceType;
        this.deviceType = deviceType;
        this.msRecurringRevenue = msRecurringRevenue;
        this.msTotalRevenue = msTotalRevenue;
        this.cloudRecurringRevenue = cloudRecurringRevenue;
        this.cloudTotalRevenue = cloudTotalRevenue;
        this.cspRecurringRevenue = cspRecurringRevenue;
        this.cspTotalRevenue = cspTotalRevenue;
        this.otherRecurringRevenue = otherRecurringRevenue;
        this.otherTotalRevenue = otherTotalRevenue;
        this.contractRecurringAdjustment = BigDecimal.ZERO;
        this.contractTotalAdjustment = BigDecimal.ZERO;
    }
    
    public ContractServiceDashboardItem(Long contractId, String contractName, String jobNumber,
                                        Date contractStart, Long customerId, String customerName, 
                                        BigDecimal contractRecurringAdjustment, BigDecimal contractTotalAdjustment) {
        super();
        this.customerId = customerId;
        this.customerName = customerName;
        this.contractId = contractId;
        this.contractName = contractName;
        this.jobNumber = (jobNumber==null ? BLANK_JOBNUMBER : jobNumber);
        this.contractStart = contractStart;
        this.contractRecurringAdjustment = BigDecimal.ZERO;
        this.contractTotalAdjustment = BigDecimal.ZERO;
        this.ospId = null;
        this.serviceName = StringUtils.EMPTY;
        this.serviceType = ServiceType.OTHER;
        this.deviceType = null;
        this.msRecurringRevenue = BigDecimal.ZERO;
        this.msTotalRevenue = BigDecimal.ZERO;
        this.cloudRecurringRevenue = BigDecimal.ZERO;
        this.cloudTotalRevenue = BigDecimal.ZERO;
        this.cspRecurringRevenue = BigDecimal.ZERO;
        this.cspTotalRevenue = BigDecimal.ZERO;
        this.otherRecurringRevenue = BigDecimal.ZERO;
        this.otherTotalRevenue = BigDecimal.ZERO;
        this.contractRecurringAdjustment = contractRecurringAdjustment;
        this.contractTotalAdjustment = contractTotalAdjustment;
    }
    
    public Long getContractId() {
        return contractId;
    }

    public void setContractId(Long contractId) {
        this.contractId = contractId;
    }

    public String getContractName() {
        return contractName;
    }

    public void setContractName(String contractName) {
        this.contractName = contractName;
    }

    public String getJobNumber() {
        return jobNumber;
    }

    public void setJobNumber(String jobNumber) {
        this.jobNumber = jobNumber;
    }

    public Date getContractStart() {
        return contractStart;
    }

    public void setContractStart(Date contractStart) {
        this.contractStart = contractStart;
    }
    
    public BigDecimal getContractRecurringAdjustment() {
        return contractRecurringAdjustment;
    }
    
    public void setContractRecurringAdjustment(BigDecimal contractRecurringAdjustment) {
        this.contractRecurringAdjustment = contractRecurringAdjustment;
    }
    
    public BigDecimal getContractTotalAdjustment() {
        return contractTotalAdjustment;
    }
    
    public void setContractTotalAdjustment(BigDecimal contractTotalAdjustment) {
        this.contractTotalAdjustment = contractTotalAdjustment;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
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

    public ServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public BigDecimal getMsRecurringRevenue() {
        return msRecurringRevenue;
    }

    public void setMsRecurringRevenue(BigDecimal msRecurringRevenue) {
        this.msRecurringRevenue = msRecurringRevenue;
    }

    public BigDecimal getMsTotalRevenue() {
        return msTotalRevenue;
    }

    public void setMsTotalRevenue(BigDecimal msTotalRevenue) {
        this.msTotalRevenue = msTotalRevenue;
    }

    public BigDecimal getCloudRecurringRevenue() {
        return cloudRecurringRevenue;
    }

    public void setCloudRecurringRevenue(BigDecimal cloudRecurringRevenue) {
        this.cloudRecurringRevenue = cloudRecurringRevenue;
    }

    public BigDecimal getCloudTotalRevenue() {
        return cloudTotalRevenue;
    }

    public void setCloudTotalRevenue(BigDecimal cloudTotalRevenue) {
        this.cloudTotalRevenue = cloudTotalRevenue;
    }

    public BigDecimal getCspRecurringRevenue() {
        return cspRecurringRevenue;
    }

    public void setCspRecurringRevenue(BigDecimal cspRecurringRevenue) {
        this.cspRecurringRevenue = cspRecurringRevenue;
    }

    public BigDecimal getCspTotalRevenue() {
        return cspTotalRevenue;
    }

    public void setCspTotalRevenue(BigDecimal cspTotalRevenue) {
        this.cspTotalRevenue = cspTotalRevenue;
    }

    public BigDecimal getOtherRecurringRevenue() {
        return otherRecurringRevenue;
    }

    public void setOtherRecurringRevenue(BigDecimal otherRecurringRevenue) {
        this.otherRecurringRevenue = otherRecurringRevenue;
    }

    public BigDecimal getOtherTotalRevenue() {
        return otherTotalRevenue;
    }

    public void setOtherTotalRevenue(BigDecimal otherTotalRevenue) {
        this.otherTotalRevenue = otherTotalRevenue;
    }

    @Override
    public String toString() {
        return "[contractId=" + contractId + ", contractName=" + contractName
                + ", jobNumber=" + jobNumber + ", contractStart=" + contractStart + ", customerId=" + customerId
                + ", customerName=" + customerName + ", ospId=" + ospId + ", serviceName=" + serviceName
                + ", serviceType=" + serviceType + ", deviceType=" + deviceType
                + ", msRecurringRevenue=" + msRecurringRevenue + ", msTotalRevenue=" + msTotalRevenue
                + ", cloudRecurringRevenue=" + cloudRecurringRevenue + ", cloudTotalRevenue=" + cloudTotalRevenue
                + ", cspRecurringRevenue=" + cspRecurringRevenue + ", cspTotalRevenue=" + cspTotalRevenue
                + ", otherRecurringRevenue=" + otherRecurringRevenue + ", otherTotalRevenue=" + otherTotalRevenue + "]";
    }

}
