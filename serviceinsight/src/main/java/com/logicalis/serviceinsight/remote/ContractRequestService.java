package com.logicalis.serviceinsight.remote;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.math.BigDecimal;
import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "correlationId",
    "serviceId",
    "contractId",
    "customerId",
    "customerType",
    "quantity",
    "contractGroupId",
    "contractUpdateId",
    "onetimeRevenue",
    "recurringRevenue",
    "startDate",
    "endDate",
    "note",
    "deviceId",
    "deviceName",
    "devicePartNumber",
    "deviceDescription",
    "deviceUnitCount",
    "subscriptionId",
    "subscriptionType",
    "status",
    "locationId"
})
public class ContractRequestService {

    @JsonProperty("correlationId")
    private Long correlationId;
    @JsonProperty("serviceId")
    private Long serviceId;
    @JsonProperty("contractId")
    private Long contractId;
    @JsonProperty("customerId")
    private String customerId;
    @JsonProperty("customerType")
    private String customerType;
    @JsonProperty("quantity")
    private Integer quantity;
    @JsonProperty("contractGroupId")
    private Long contractGroupId;
    @JsonProperty("contractUpdateId")
    private Long contractUpdateId;
    @JsonProperty("onetimeRevenue")
    private BigDecimal onetimeRevenue;
    @JsonProperty("recurringRevenue")
    private BigDecimal recurringRevenue;
    @JsonProperty("startDate")
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="MMddyyyy", timezone="America/New_York")
    private Date startDate;
    @JsonProperty("endDate")
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="MMddyyyy", timezone="America/New_York")
    private Date endDate;
    @JsonProperty("note")
    private String note;
    @JsonProperty("deviceId")
    private Long deviceId;
    @JsonProperty("deviceName")
    private String deviceName;
    @JsonProperty("devicePartNumber")
    private String devicePartNumber;
    @JsonProperty("deviceDescription")
    private String deviceDescription;
    @JsonProperty("deviceUnitCount")
    private Integer deviceUnitCount;
    @JsonProperty("subscriptionId")
    private String subscriptionId;
    @JsonProperty("subscriptionType")
    private String subscriptionType;
    @JsonProperty("status")
    private String status;
    @JsonProperty("locationId")
    private Integer locationId;
    @JsonProperty("relatedServices")
    private List<ContractRequestService> relatedServices;

    public ContractRequestService() {
		super();
	}

    @JsonProperty("correlationId")
    public Long getCorrelationId() {
		return correlationId;
	}
    
	@JsonProperty("correlationId")
	public void setCorrelationId(Long correlationId) {
		this.correlationId = correlationId;
	}

    @JsonProperty("serviceId")
    public Long getServiceId() {
        return serviceId;
    }

	@JsonProperty("serviceId")
    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    @JsonProperty("contractId")
    public Long getContractId() {
        return contractId;
    }

    @JsonProperty("contractId")
    public void setContractId(Long contractId) {
        this.contractId = contractId;
    }

    /**
     * @return the customerId
     */
    public String getCustomerId() {
        return customerId;
    }

    /**
     * @param customerId the customerId to set
     */
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    /**
     * @return the customerType
     */
    public String getCustomerType() {
        return customerType;
    }

    /**
     * @param customerType the customerType to set
     */
    public void setCustomerType(String customerType) {
        this.customerType = customerType;
    }

    @JsonProperty("quantity")
    public Integer getQuantity() {
        return quantity;
    }

    @JsonProperty("quantity")
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    @JsonProperty("contractGroupId")
    public Long getContractGroupId() {
        return contractGroupId;
    }

    @JsonProperty("contractGroupId")
    public void setContractGroupId(Long contractGroupId) {
        this.contractGroupId = contractGroupId;
    }

    @JsonProperty("contractUpdateId")
    public Long getContractUpdateId() {
        return contractUpdateId;
    }

    @JsonProperty("contractUpdateId")
    public void setContractUpdateId(Long contractUpdateId) {
        this.contractUpdateId = contractUpdateId;
    }

    @JsonProperty("onetimeRevenue")
    public BigDecimal getOnetimeRevenue() {
        return onetimeRevenue;
    }

    @JsonProperty("onetimeRevenue")
    public void setOnetimeRevenue(BigDecimal onetimeRevenue) {
        this.onetimeRevenue = onetimeRevenue;
    }

    @JsonProperty("recurringRevenue")
    public BigDecimal getRecurringRevenue() {
        return recurringRevenue;
    }

    @JsonProperty("recurringRevenue")
    public void setRecurringRevenue(BigDecimal recurringRevenue) {
        this.recurringRevenue = recurringRevenue;
    }

    @JsonProperty("startDate")
    public Date getStartDate() {
        return startDate;
    }

    @JsonProperty("startDate")
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    @JsonProperty("endDate")
    public Date getEndDate() {
        return endDate;
    }

    @JsonProperty("endDate")
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    @JsonProperty("note")
    public String getNote() {
        return note;
    }

    @JsonProperty("note")
    public void setNote(String note) {
        this.note = note;
    }

    @JsonProperty("deviceId")
    public Long getDeviceId() {
        return deviceId;
    }

    @JsonProperty("deviceId")
    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    @JsonProperty("deviceName")
    public String getDeviceName() {
        return deviceName;
    }

    @JsonProperty("deviceName")
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    @JsonProperty("devicePartNumber")
    public String getDevicePartNumber() {
        return devicePartNumber;
    }

    @JsonProperty("devicePartNumber")
    public void setDevicePartNumber(String devicePartNumber) {
        this.devicePartNumber = devicePartNumber;
    }

    @JsonProperty("deviceDescription")
    public String getDeviceDescription() {
        return deviceDescription;
    }

    @JsonProperty("deviceDescription")
    public void setDeviceDescription(String deviceDescription) {
        this.deviceDescription = deviceDescription;
    }

    @JsonProperty("deviceUnitCount")
    public Integer getDeviceUnitCount() {
        return deviceUnitCount;
    }

    @JsonProperty("deviceUnitCount")
    public void setDeviceUnitCount(Integer deviceUnitCount) {
        this.deviceUnitCount = deviceUnitCount;
    }

    /**
     * @return the subscriptionId
     */
    public String getSubscriptionId() {
        return subscriptionId;
    }

    /**
     * @param subscriptionId the subscriptionId to set
     */
    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    /**
     * @return the subscriptionType
     */
    public String getSubscriptionType() {
        return subscriptionType;
    }

    /**
     * @param subscriptionType the subscriptionType to set
     */
    public void setSubscriptionType(String subscriptionType) {
        this.subscriptionType = subscriptionType;
    }

    @JsonProperty("status")
    public String getStatus() {
        return status;
    }

    @JsonProperty("status")
    public void setStatus(String status) {
        this.status = status;
    }

    @JsonProperty("locationId")
    public Integer getLocationId() {
        return locationId;
    }

    @JsonProperty("locationId")
    public void setLocationId(Integer locationId) {
        this.locationId = locationId;
    }

	public List<ContractRequestService> getRelatedServices() {
		return relatedServices;
	}

	public void setRelatedServices(List<ContractRequestService> relatedServices) {
		this.relatedServices = relatedServices;
	}

	@Override
	public String toString() {
		return "ContractRequestService [correlationId=" + correlationId + ", serviceId=" + serviceId + ", contractId="
				+ contractId + ", quantity=" + quantity + ", contractGroupId=" + contractGroupId + ", contractUpdateId="
				+ contractUpdateId + ", onetimeRevenue=" + onetimeRevenue + ", recurringRevenue=" + recurringRevenue
				+ ", startDate=" + startDate + ", endDate=" + endDate + ", note=" + note + ", deviceId=" + deviceId
				+ ", deviceName=" + deviceName + ", devicePartNumber=" + devicePartNumber + ", deviceDescription="
				+ deviceDescription + ", deviceUnitCount=" + deviceUnitCount + ", status=" + status + ", locationId="
				+ locationId + ", relatedServices=" + relatedServices + "]";
	}

}
