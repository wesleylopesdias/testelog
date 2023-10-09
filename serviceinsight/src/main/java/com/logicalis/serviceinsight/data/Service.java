package com.logicalis.serviceinsight.data;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.logicalis.serviceinsight.dao.DeviceExpenseCategory;
import com.logicalis.serviceinsight.dao.ServiceExpenseCategory;
import com.logicalis.serviceinsight.remote.ContractRequestService;
import com.logicalis.serviceinsight.util.VersionUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.apache.commons.math3.fraction.Fraction;
import org.joda.time.DateTime;

/**
 *
 * @author poneil
 */
public class Service {
    
	public enum Status {
		active("Active"), inactive("Inactive"), pending("Pending"), donotbill("DoNotBill");
		
		private String description;

        Status(String description) {
            this.description = description;
        }

        public String getDescription() {
            return this.description;
        }
	}
	
	public enum ServiceType {
	    MANAGED("Managed Services","MS"), CLOUD("Cloud","Cloud"), CSP("Annuity Product","CSP"), OTHER("Other","MS & Cloud");
	    
	    private String description;
	    private String code;
	    
	    ServiceType(String description, String code) {
	        this.description = description;
	        this.code = code;
	    }
	    
	    public String getDescription() {
	        return this.description;
	    }
	    
	    public String getCode() {
	        return this.code;
	    }
	}
	
	public enum Reason {
		embedded("Embedded");
		
		private String description;

		Reason(String description) {
            this.description = description;
        }

        public String getDescription() {
            return this.description;
        }
	}
	
    private Long id;
    private Long correlationId;
    private String code;
    private Long customerId;
    private String customerName;
    private String customerType;
    private Long serviceId;
    private Long parentId;
    private Long contractId;
    private Long contractGroupId;
    private Long contractUpdateId;
    private String ospId;
    private String businessModel;
    private Double version;
    private String name;
    private BigDecimal onetimeRevenue;
    private BigDecimal recurringRevenue;
    private BigDecimal unitPriceRecurringRevenue;
    private String note;
    private Integer quantity = 1;
    private Integer lineitemCount;
    private Status status;
    private Boolean hasPendingRecord;
    DateTime month;
    Integer days;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MMddyyyy", timezone = "America/New_York")
    private Date startDate;
    private DateTime startDateTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MMddyyyy", timezone = "America/New_York")
    private Date endDate;
    private DateTime endDateTime;
    private String operation;
    private String devicePartNumber;
    private String deviceDescription;
    private String deviceName;
    private Integer deviceUnitCount;
    private Long deviceId;
    private List<ContractUpdate> contractUpdates = new ArrayList<ContractUpdate>();
    private Integer costMappingsCount = 0;
    private List<DeviceExpenseCategory> costMappings = new ArrayList<DeviceExpenseCategory>();
    private List<ServiceExpenseCategory> serviceCostMappings = new ArrayList<ServiceExpenseCategory>();
    private List<Service> relatedLineItems = new ArrayList<Service>();
    private boolean isProRatedAmount = false;
    private int CALC_SCALE = 20;
    private Long quoteLineItemId;
    private Long contractServiceSubscriptionId;
    private Long microsoft365SubscriptionConfigId;
    private String subscriptionType;
    private Integer locationId;
    private ContractServiceDetail detail;
    private BigDecimal fullMonthRecurringRevenue;
    private Long defaultOspId;
    private Boolean hidden;
    private Reason reason;
    private BigDecimal recurringUnitPrice;
    private BigDecimal onetimeUnitPrice;

    public Service() {
    }

    /**
     * minimal CTOR for a Service outside of a contract
     */
    public Service(Long id, String code, String ospId, Double version, String name, String businessModel) {
        this.serviceId = id;
        this.code = code;
        this.ospId = ospId;
        this.version = version;
        this.name = name;
        this.businessModel = businessModel;
    }

    /**
     * minimal CTOR for a Service outside of a contract
     * - includes costMappingsCount
     */
    public Service(Long id, String code, String ospId, Double version, String name, String businessModel, Integer costMappingsCount) {
        this.serviceId = id;
        this.code = code;
        this.ospId = ospId;
        this.version = version;
        this.name = name;
        this.businessModel = businessModel;
        this.costMappingsCount = costMappingsCount;
    }

    /**
     * basic CTOR with ID
     */
    public Service(Long id, String code, Long parentId, Long contractId, Long contractGroupId, Long contractUpdateId, Long serviceId, String ospId,
            Double version, String name, BigDecimal onetimeRevenue, BigDecimal recurringRevenue, Integer quantity,
            String note, Integer lineitemCount, Date sd, Date ed, Long deviceId, String deviceName,
            String devicePartNumber, String deviceDescription, Integer deviceUnitCount, Status status, Long contractServiceSubscriptionId, String subscriptionType, Long microsoft365SubscriptionConfigId,
            Integer locationId) {
        this.id = id;
        this.code = code;
        this.parentId = parentId;
        this.contractId = contractId;
        this.contractGroupId = contractGroupId;
        this.contractUpdateId = contractUpdateId;
        this.serviceId = serviceId;
        this.ospId = ospId;
        this.version = version;
        this.name = name;
        this.onetimeRevenue = onetimeRevenue;
        this.recurringRevenue = recurringRevenue;
        this.fullMonthRecurringRevenue = recurringRevenue;
        this.note = note;
        this.quantity = quantity;
        this.unitPriceRecurringRevenue = recurringRevenue;
        this.lineitemCount = lineitemCount;
        this.startDate = sd;
        if (sd != null) {
            this.startDateTime = new DateTime(sd);
        }
        this.endDate = ed;
        if (ed != null) {
            this.endDateTime = new DateTime(ed);
        }

        this.month = null;
        this.days = null;
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.devicePartNumber = devicePartNumber;
        this.deviceDescription = deviceDescription;
        this.deviceUnitCount = deviceUnitCount;
        this.status = status;
        this.contractServiceSubscriptionId = contractServiceSubscriptionId;
        this.microsoft365SubscriptionConfigId = microsoft365SubscriptionConfigId;
        this.subscriptionType = subscriptionType;
        this.locationId = locationId;
    }

    /**
     * CTOR for Service rollup (no ID, has month property)
     */
    public Service(DateTime forMonth, String code, Long contractId, Long contractGroupId, Long contractUpdateId, Long serviceId, String ospId,
            Double version, String name, BigDecimal onetimeRevenue, BigDecimal recurringRevenue, Integer quantity,
            Date sd, Date ed, Long deviceId, String deviceName, String devicePartNumber, String deviceDescription,
            Integer deviceUnitCount, Boolean hasPendingRecord) {
        this.id = null;
        this.month = forMonth;
        if (this.month != null) {
            this.days = this.month.dayOfMonth().getMaximumValue();
        }
        this.code = code;
        this.contractId = contractId;
        this.contractGroupId = contractGroupId;
        this.contractUpdateId = contractUpdateId;
        this.serviceId = serviceId;
        this.ospId = ospId;
        this.version = version;
        this.name = name;
        this.onetimeRevenue = onetimeRevenue;
        this.recurringRevenue = recurringRevenue;
        this.fullMonthRecurringRevenue = recurringRevenue;
        this.quantity = quantity;
        this.unitPriceRecurringRevenue = recurringRevenue;
        this.lineitemCount = null;
        this.startDate = sd;
        if (sd != null) {
            this.startDateTime = new DateTime(sd);
        }
        this.endDate = ed;
        if (ed != null) {
            this.endDateTime = new DateTime(ed);
        }
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.devicePartNumber = devicePartNumber;
        this.deviceDescription = deviceDescription;
        this.deviceUnitCount = deviceUnitCount;
        this.hasPendingRecord = hasPendingRecord;
    }
    
    /**
     * CTOR with ID for CI View Rollup
     */
    public Service(DateTime forMonth, Long id, String code, Long parentId, Long contractId, Long contractGroupId, Long contractUpdateId, Long serviceId, String ospId,
            Double version, String name, BigDecimal onetimeRevenue, BigDecimal recurringRevenue, Integer quantity,
            String note, Integer lineitemCount, Date sd, Date ed, Long deviceId, String deviceName,
            String devicePartNumber, String deviceDescription, Integer deviceUnitCount, Status status, Long contractServiceSubscriptionId, String subscriptionType, Integer locationId) {
    	this.month = forMonth;
        if (this.month != null) {
            this.days = this.month.dayOfMonth().getMaximumValue();
        }
    	this.id = id;
        this.code = code;
        this.parentId = parentId;
        this.contractId = contractId;
        this.contractGroupId = contractGroupId;
        this.contractUpdateId = contractUpdateId;
        this.serviceId = serviceId;
        this.ospId = ospId;
        this.version = version;
        this.name = name;
        this.onetimeRevenue = onetimeRevenue;
        this.recurringRevenue = recurringRevenue;
        this.fullMonthRecurringRevenue = recurringRevenue;
        this.note = note;
        this.quantity = quantity;
        this.unitPriceRecurringRevenue = recurringRevenue;
        this.lineitemCount = lineitemCount;
        this.startDate = sd;
        if (sd != null) {
            this.startDateTime = new DateTime(sd);
        }
        this.endDate = ed;
        if (ed != null) {
            this.endDateTime = new DateTime(ed);
        }

        this.month = null;
        this.days = null;
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.devicePartNumber = devicePartNumber;
        this.deviceDescription = deviceDescription;
        this.deviceUnitCount = deviceUnitCount;
        this.status = status;
        this.contractServiceSubscriptionId = contractServiceSubscriptionId;
        this.subscriptionType = subscriptionType;
        this.locationId = locationId;
    }
    
    public Service(Long correlationId, Long serviceId, Long contractId, String customerId, String customerType,
            Integer quantity, Long contractGroupId, Long contractUpdateId, BigDecimal onetimeRevenue,
            BigDecimal recurringRevenue, Date startDate, Date endDate, String note, Long deviceId,
            String deviceName, String devicePartNumber, String deviceDescription, Integer deviceUnitCount,
            String subscriptionId, String subscriptionType, String status, Integer locationId) {
        this.correlationId = correlationId;
        this.serviceId = serviceId;
        this.contractId = contractId;
        if (customerId != null) {
            this.customerId = Long.valueOf(customerId);
        }
        this.customerType = customerType;
        this.quantity = quantity;
        this.contractGroupId = contractGroupId;
        this.contractUpdateId = contractUpdateId;
        this.onetimeRevenue = onetimeRevenue;
        this.recurringRevenue = recurringRevenue;
        this.startDate = startDate;
        this.endDate = endDate;
        this.note = note;
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.devicePartNumber = devicePartNumber;
        this.deviceDescription = deviceDescription;
        this.deviceUnitCount = deviceUnitCount;
        if (subscriptionId != null) {
            this.contractServiceSubscriptionId = Long.valueOf(subscriptionId);
        }
        this.subscriptionType = subscriptionType;
        if (status != null) {
            this.status = Status.valueOf(status);
        }
        this.locationId = locationId;
    }
    
    public static Service fromContractRequestService(ContractRequestService from) {
        Service to = new Service(from.getCorrelationId(), from.getServiceId(), from.getContractId(),
        from.getCustomerId(), from.getCustomerType(), from.getQuantity(), from.getContractGroupId(),
        from.getContractUpdateId(), from.getOnetimeRevenue(), from.getRecurringRevenue(),
        from.getStartDate(), from.getEndDate(), from.getNote(), from.getDeviceId(), from.getDeviceName(),
        from.getDevicePartNumber(), from.getDeviceDescription(), from.getDeviceUnitCount(),
        from.getSubscriptionId(), from.getSubscriptionType(), from.getStatus(), from.getLocationId());
        if (from.getRelatedServices() != null) {
            for (ContractRequestService child : from.getRelatedServices()) {
                to.addRelatedLineItem(fromContractRequestService(child));
            }
        }
        return to;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(Long correlationId) {
        this.correlationId = correlationId;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
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

    public Long getContractId() {
        return contractId;
    }

    public void setContractId(Long contractId) {
        this.contractId = contractId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Long getContractGroupId() {
        return contractGroupId;
    }

    public void setContractGroupId(Long contractGroupId) {
        this.contractGroupId = contractGroupId;
    }

    public Long getContractUpdateId() {
        return contractUpdateId;
    }

    public void setContractUpdateId(Long contractUpdateId) {
        this.contractUpdateId = contractUpdateId;
    }

    public String getOspId() {
        return ospId;
    }

    public void setOspId(String ospId) {
        this.ospId = ospId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getLineitemCount() {
        return lineitemCount;
    }

    public void setLineitemCount(Integer lineitemCount) {
        this.lineitemCount = lineitemCount;
    }

    public Double getVersion() {
        return version;
    }

    public void setVersion(Double version) {
        this.version = version;
    }

    public DateTime getMonth() {
		return month;
	}

	public void setMonth(DateTime month) {
		this.month = month;
		if (this.month != null) {
            this.days = this.month.dayOfMonth().getMaximumValue();
        }
	}

	/**
     * should format a Double into a "version" looking string, like 1.11 becomes
     * 1.1.1
     *
     * @param version
     * @return
     */
    public String getFormattedVersion() {
        return VersionUtil.formatVersion(getVersion());
    }

    public Integer getMonthInteger() {
        if (month != null) {
            return month.getMonthOfYear();
        }
        return null;
    }

    public String getShortMonthName() {
        if (month != null) {
            return month.toString("MMM");
        }
        return null;
    }

    public String getLongMonthName() {
        if (month != null) {
            return month.toString("MMMMM");
        }
        return null;
    }

    public Integer getYearInteger() {
        if (month != null) {
            return month.getYear();
        }
        return null;
    }

    public String getShortYearName() {
        if (month != null) {
            return month.toString("yy");
        }
        return null;
    }

    public String getLongYearName() {
        if (month != null) {
            return month.toString("yyyy");
        }
        return null;
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

    public void forMonth(DateTime forMonth) {
        this.month = forMonth;
        if (this.month != null) {
            this.days = this.month.dayOfMonth().getMaximumValue();
        }
    }

    public Integer getDays() {
        return days;
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
    public Long getQuoteLineItemId() {
		return quoteLineItemId;
	}

	public void setQuoteLineItemId(Long quoteLineItemId) {
		this.quoteLineItemId = quoteLineItemId;
	}

	public void setRecurringUnitPrice(BigDecimal recurringUnitPrice) {
		this.recurringUnitPrice = recurringUnitPrice;
	}

	public void setOnetimeUnitPrice(BigDecimal onetimeUnitPrice) {
		this.onetimeUnitPrice = onetimeUnitPrice;
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

    public BigDecimal getFormattedOnetimeRevenue() {
        if (onetimeRevenue != null) {
            if (month != null) {
                if (startDateTime.getYear() == month.getYear()
                        && startDateTime.getMonthOfYear() == month.getMonthOfYear()) {
                    return onetimeRevenue.setScale(2, RoundingMode.HALF_UP);
                }
                return new BigDecimal(0); // onetimeRevenue only applies in the first month
            }
            return onetimeRevenue.setScale(2, RoundingMode.HALF_UP);
        }
        return new BigDecimal(0).setScale(2, RoundingMode.HALF_UP); // onetimeRevenue only applies in the first month
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
    
    public void setUnitPriceRecurringRevenue(BigDecimal unitPriceRecurringRevenue) {
        this.unitPriceRecurringRevenue = unitPriceRecurringRevenue;
    }
    
    //returns a non-prorated revenue for a single unit for UI calculations
    public BigDecimal getUnitPriceRecurringRevenue() {
        if (recurringRevenue == null || isZero(recurringRevenue)) {
            return new BigDecimal(0);
        }
        if(quantity == null || quantity == 0) {
        	return new BigDecimal(0);
        }
            return recurringRevenue.divide(new BigDecimal(quantity), CALC_SCALE, RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP);
    }
    
    //generate unit price based on unit count
    public BigDecimal getOnetimeUnitPrice() {
    	BigDecimal onetimeUnitPrice = new BigDecimal(0);
    	if (onetimeRevenue == null || isZero(onetimeRevenue)) {
            return onetimeUnitPrice;
        }
    	
    	if(deviceUnitCount != null && deviceUnitCount > 0) {
    		onetimeUnitPrice = onetimeRevenue.divide(new BigDecimal(deviceUnitCount), CALC_SCALE, RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP);
    	} else {
    		onetimeUnitPrice = onetimeRevenue;
    	}
    	
    	return onetimeUnitPrice;
    }
    
    //generate unit price based on unit count
    public BigDecimal getRecurringUnitPrice() {
    	BigDecimal recurringUnitPrice = new BigDecimal(0);
    	if (recurringRevenue == null || isZero(recurringRevenue)) {
            return recurringUnitPrice;
        }
    	
    	if(deviceUnitCount != null && deviceUnitCount > 0) {
    		recurringUnitPrice = recurringRevenue.divide(new BigDecimal(deviceUnitCount), CALC_SCALE, RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP);
    	} else {
    		recurringUnitPrice = recurringRevenue;
    	}
    	
    	return recurringUnitPrice;
    }

    public BigDecimal getFormattedRecurringRevenue() {
        if (recurringRevenue != null) {
            if (month != null) {
                return recurringRevenue
                        .multiply(new BigDecimal(getMonthFraction()))
                        .setScale(2, RoundingMode.HALF_UP);
            }
            return recurringRevenue.setScale(2, RoundingMode.HALF_UP);
        }
        return new BigDecimal(0).setScale(2, RoundingMode.HALF_UP);
    }

    public void setRecurringRevenue(BigDecimal recurringRevenue) {
        this.recurringRevenue = recurringRevenue;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
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

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public Integer getDeviceUnitCount() {
        return deviceUnitCount;
    }

    public void setDeviceUnitCount(Integer deviceUnitCount) {
        this.deviceUnitCount = deviceUnitCount;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }
    
    public Boolean getHasPendingRecord() {
		return hasPendingRecord;
	}

	public void setHasPendingRecord(Boolean hasPendingRecord) {
		this.hasPendingRecord = hasPendingRecord;
	}

	public List<ContractUpdate> getContractUpdates() {
        return contractUpdates;
    }

    public void setContractUpdates(List<ContractUpdate> contractUpdates) {
        this.contractUpdates = contractUpdates;
    }

    public void addContractUpdate(ContractUpdate contractUpdate) {
        this.contractUpdates.add(contractUpdate);
    }

    public Integer getCostMappingsCount() {
        return costMappingsCount;
    }

    public void setCostMappingsCount(Integer costMappingsCount) {
        this.costMappingsCount = costMappingsCount;
    }

    public List<DeviceExpenseCategory> getCostMappings() {
        return costMappings;
    }

    public void setCostMappings(List<DeviceExpenseCategory> costMappings) {
        this.costMappings = costMappings;
    }
    
    public void addCostMapping(DeviceExpenseCategory costMapping) {
        if (costMappings == null) {
            costMappings = new ArrayList<DeviceExpenseCategory>();
        }
        if (!costMappings.contains(costMapping)) {
            costMappings.add(costMapping);
        }
    }

    public List<ServiceExpenseCategory> getServiceCostMappings() {
        return serviceCostMappings;
    }

    public void setServiceCostMappings(List<ServiceExpenseCategory> serviceCostMappings) {
        this.serviceCostMappings = serviceCostMappings;
    }

    public List<Service> getRelatedLineItems() {
        return relatedLineItems;
    }

    public void setRelatedLineItems(List<Service> relatedLineItems) {
        this.relatedLineItems = relatedLineItems;
    }
    
    public void addRelatedLineItem(Service relatedLineItem) {
        if (relatedLineItems == null) {
            relatedLineItems = new ArrayList<Service>();
        }
        relatedLineItems.add(relatedLineItem);
    }

    public boolean isProRatedAmount() {
        return isProRatedAmount;
    }

    public void setProRatedAmount(boolean isProRatedAmount) {
        this.isProRatedAmount = isProRatedAmount;
    }

    public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getBusinessModel() {
		return businessModel;
	}

	public void setBusinessModel(String businessModel) {
		this.businessModel = businessModel;
	}

	public Long getContractServiceSubscriptionId() {
		return contractServiceSubscriptionId;
	}

	public void setContractServiceSubscriptionId(Long contractServiceSubscriptionId) {
		this.contractServiceSubscriptionId = contractServiceSubscriptionId;
	}

	public String getSubscriptionType() {
		return subscriptionType;
	}

	public void setSubscriptionType(String subscriptionType) {
		this.subscriptionType = subscriptionType;
	}

	public Long getMicrosoft365SubscriptionConfigId() {
		return microsoft365SubscriptionConfigId;
	}

	public void setMicrosoft365SubscriptionConfigId(Long microsoft365SubscriptionConfigId) {
		this.microsoft365SubscriptionConfigId = microsoft365SubscriptionConfigId;
	}

	public ContractServiceDetail getDetail() {
		return detail;
	}

	public void setDetail(ContractServiceDetail detail) {
		this.detail = detail;
	}

	public Integer getLocationId() {
		return locationId;
	}

	public void setLocationId(Integer locationId) {
		this.locationId = locationId;
	}

	public BigDecimal getFullMonthRecurringRevenue() {
		return fullMonthRecurringRevenue;
	}

	public void setFullMonthRecurringRevenue(BigDecimal fullMonthRecurringRevenue) {
		this.fullMonthRecurringRevenue = fullMonthRecurringRevenue;
	}

	public Long getDefaultOspId() {
		return defaultOspId;
	}

	public void setDefaultOspId(Long defaultOspId) {
		this.defaultOspId = defaultOspId;
	}

	public Boolean getHidden() {
		return hidden;
	}

	public void setHidden(Boolean hidden) {
		this.hidden = hidden;
	}

	public Reason getReason() {
		return reason;
	}

	public void setReason(Reason reason) {
		this.reason = reason;
	}

	private boolean isZero(BigDecimal number) {
    	BigDecimal zero = new BigDecimal(0);
    	BigDecimal zeroDecimal = new BigDecimal(0).setScale(2, RoundingMode.HALF_UP);
    	number = number.setScale(2, RoundingMode.HALF_UP);
    	if(zero.equals(number) || zeroDecimal.equals(number)) {
    		return true;
    	}
    	
    	return false;
    }
    
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.contractId);
        hash = 97 * hash + Objects.hashCode(this.startDate);
        hash = 97 * hash + Objects.hashCode(this.endDate);
        hash = 97 * hash + Objects.hashCode(this.devicePartNumber);
        hash = 97 * hash + Objects.hashCode(this.deviceDescription);
        hash = 97 * hash + Objects.hashCode(this.deviceUnitCount);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Service other = (Service) obj;
        if (!Objects.equals(this.serviceId, other.serviceId)) {
            return false;
        }
        if (!Objects.equals(this.contractId, other.contractId)) {
            return false;
        }
        if (!Objects.equals(this.startDate, other.startDate)) {
            return false;
        }
        if (!Objects.equals(this.endDate, other.endDate)) {
            return false;
        }
        if (!Objects.equals(this.devicePartNumber, other.devicePartNumber)) {
            return false;
        }
        if (!Objects.equals(this.deviceDescription, other.deviceDescription)) {
            return false;
        }
        if (!Objects.equals(this.deviceUnitCount, other.deviceUnitCount)) {
            return false;
        }
        return true;
    }
    
    public boolean rollupEquals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Service other = (Service) obj;
        if (!Objects.equals(this.serviceId, other.serviceId)) {
            return false;
        }
        if (!Objects.equals(this.contractId, other.contractId)) {
            return false;
        }
        if (!Objects.equals(this.startDate, other.startDate)) {
            return false;
        }
        if (!Objects.equals(this.endDate, other.endDate)) {
            return false;
        }
        if (!Objects.equals(this.devicePartNumber, other.devicePartNumber)) {
            return false;
        }
        if (!Objects.equals(this.deviceDescription, other.deviceDescription)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Service{" + "id=" + id + ", correlationId=" + correlationId + ", code=" + code + ", customerId=" + customerId + ", customerName=" + customerName + ", customerType=" + customerType + ", serviceId=" + serviceId + ", parentId=" + parentId + ", contractId=" + contractId + ", contractGroupId=" + contractGroupId + ", contractUpdateId=" + contractUpdateId + ", ospId=" + ospId + ", businessModel=" + businessModel + ", version=" + version + ", name=" + name + ", onetimeRevenue=" + onetimeRevenue + ", recurringRevenue=" + recurringRevenue + ", unitPriceRecurringRevenue=" + unitPriceRecurringRevenue + ", note=" + note + ", quantity=" + quantity + ", lineitemCount=" + lineitemCount + ", status=" + status + ", hasPendingRecord=" + hasPendingRecord + ", month=" + month + ", days=" + days + ", startDate=" + startDate + ", startDateTime=" + startDateTime + ", endDate=" + endDate + ", endDateTime=" + endDateTime + ", operation=" + operation + ", devicePartNumber=" + devicePartNumber + ", deviceDescription=" + deviceDescription + ", deviceName=" + deviceName + ", deviceUnitCount=" + deviceUnitCount + ", deviceId=" + deviceId + ", contractUpdates=" + contractUpdates + ", costMappingsCount=" + costMappingsCount + ", costMappings=" + costMappings + ", serviceCostMappings=" + serviceCostMappings + ", relatedLineItems=" + relatedLineItems + ", isProRatedAmount=" + isProRatedAmount + ", CALC_SCALE=" + CALC_SCALE + ", quoteLineItemId=" + quoteLineItemId + ", contractServiceSubscriptionId=" + contractServiceSubscriptionId + ", microsoft365SubscriptionConfigId=" + microsoft365SubscriptionConfigId + ", subscriptionType=" + subscriptionType + ", locationId=" + locationId + ", detail=" + detail + ", fullMonthRecurringRevenue=" + fullMonthRecurringRevenue + ", defaultOspId=" + defaultOspId + ", hidden=" + hidden + ", reason=" + reason + ", recurringUnitPrice=" + recurringUnitPrice + ", onetimeUnitPrice=" + onetimeUnitPrice + '}';
    }
}
