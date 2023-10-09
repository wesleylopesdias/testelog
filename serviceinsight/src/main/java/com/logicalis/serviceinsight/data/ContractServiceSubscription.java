package com.logicalis.serviceinsight.data;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

public class ContractServiceSubscription {
	
	public enum SubscriptionType {
		cspazure("CSPAzure"), cspazureplan("CSPAzurePlan"), aws("AWS");
        private String description;

        SubscriptionType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return this.description;
        }
    }
	
	public enum CustomerType {
		netnew("New Customer"), existing("Existing Customer");
        private String description;

        CustomerType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return this.description;
        }
    }
	
	private Long id;
	private Long contractId;
	private Long deviceId;
	private String devicePartNumber;
	private String deviceDescription;
	private Long serviceId;
	private String serviceName;
	private String subscriptionId;
	private String customerId;
	private String name;
	private SubscriptionType subscriptionType;
	private CustomerType customerType;
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="MMddyyyy", timezone="America/New_York")
    private Date startDate;
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="MMddyyyy", timezone="America/New_York")
    private Date endDate;
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="MMddyyyy", timezone="America/New_York")
	private Date created;
    private String createdBy;
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="MMddyyyy", timezone="America/New_York")
    private Date updated;
    private String updatedBy;
    
    public ContractServiceSubscription(){}
    
	public ContractServiceSubscription(Long id, Long contractId, Long deviceId, String devicePartNumber, String deviceDescription, Long serviceId, String serviceName, String subscriptionId, String customerId, Date startDate, Date endDate, String name, SubscriptionType subscriptionType, CustomerType customerType) {
		super();
		this.id = id;
		this.contractId = contractId;
		this.deviceId = deviceId;
		this.devicePartNumber = devicePartNumber;
		this.deviceDescription = deviceDescription;
		this.serviceId = serviceId;
		this.serviceName = serviceName;
		this.subscriptionId = subscriptionId;
		this.customerId = customerId;
		this.startDate = startDate;
		this.endDate = endDate;
		this.name = name;
		this.subscriptionType = subscriptionType;
		this.customerType = customerType;
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

	public Long getServiceId() {
		return serviceId;
	}
	
	public void setServiceId(Long serviceId) {
		this.serviceId = serviceId;
	}
	
	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getSubscriptionId() {
		return subscriptionId;
	}
	
	public void setSubscriptionId(String subscriptionId) {
		this.subscriptionId = subscriptionId;
	}

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
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
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public SubscriptionType getSubscriptionType() {
		return subscriptionType;
	}

	public void setSubscriptionType(SubscriptionType subscriptionType) {
		this.subscriptionType = subscriptionType;
	}

	public CustomerType getCustomerType() {
		return customerType;
	}

	public void setCustomerType(CustomerType customerType) {
		this.customerType = customerType;
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
        return "ContractServiceSubscription{" + "id=" + id + ", contractId=" + contractId + ", deviceId=" + deviceId + ", devicePartNumber=" + devicePartNumber + ", deviceDescription=" + deviceDescription + ", serviceId=" + serviceId + ", serviceName=" + serviceName + ", subscriptionId=" + subscriptionId + ", customerId=" + customerId + ", name=" + name + ", subscriptionType=" + subscriptionType + ", customerType=" + customerType + ", startDate=" + startDate + ", endDate=" + endDate + ", created=" + created + ", createdBy=" + createdBy + ", updated=" + updated + ", updatedBy=" + updatedBy + '}';
    }

}
