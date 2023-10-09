package com.logicalis.serviceinsight.data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.logicalis.serviceinsight.dao.SPLACost;
import com.logicalis.serviceinsight.dao.DeviceExpenseCategory;

/**
 * Represents a "Supported Device/Item" that is associated with a Service on a Contract.
 * 
 * @author poneil
 */
public class Device implements Serializable {
	
	public enum DeviceType {
		cspazure("CSPAzure"), cspazureplan("CSPAzurePlan"), network("Network"), server("Server"), cspO365("CSPO365"), aws("AWS"), spla("SPLA"), cspreserved("CSPReserved"), 
		businessService("Business Service"), compute("Compute"), storage("Storage"), memory("Memory"), backupStorage("Backup Storage"), vaultingStorage("Vaulting Storage"),
		backup("Backup"), vaulting("Vaulting"), management("Management"), software("Software"), dr("Disaster Recovery"), vdc("VDC"), drCompute("Disaster Recovery Compute"),
		drMemory("Disaster Recovery Memory"), drStorage("Disaster Recovery Storage"), prcp("PRC Device"), M365Subscription("M365 Subscription"), M365("M365"), M365Support("M365 Support"), M365Setup("M365Setup"),
		M365NC("M365NewCommerce");
        
		private String description;

        DeviceType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return this.description;
        }
    }
	
	public enum TermDuration {
        P1Y("1 Year"), P1M("1 Month"), P3Y("3 Year");
        private String description;

        TermDuration(String description) {
            this.description = description;
        }

        public String getDescription() {
            return this.description;
        }
    }
    
    public enum BillingPlan {
        none("None"), annual("Annually"), monthly("Monthly"), triennial("Triennial");
        private String description;

        BillingPlan(String description) {
            this.description = description;
        }

        public String getDescription() {
            return this.description;
        }
    }
    
    public enum Segment {
        commercial("Commercial"), government("Government"), charity("Charity"), academic("Academic"), corporate("Corporate");
        private String description;

        Segment(String description) {
            this.description = description;
        }

        public String getDescription() {
            return this.description;
        }
    }
	
	public enum Relationship {
		optional("Optional"), required("Required"), embedded("Embedded");
		
        private String description;

        Relationship(String description) {
            this.description = description;
        }

        public String getDescription() {
            return this.description;
        }
    }
    
    /**
     * Used to easily store retrievable data about
     * a devices parents for UI display
     */
    public static class ParentDevice implements Serializable {

        private Long id;
        private String description;
        private String partNumber;
        
        public ParentDevice(Long id, String description, String partNumber) {
            this.id = id;
            this.description = description;
            this.partNumber = partNumber;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getPartNumber() {
            return partNumber;
        }

        public void setPartNumber(String partNumber) {
            this.partNumber = partNumber;
        }
    }
	
	/* NOTE About Adding New Properties! 
	 * If you add a new property that is an Object, please make sure that Object implements Serializable. We do a Deep Copy when retrieving devices
	 * and if the object being referenced is not Serializable it can cause issues.
	 * */
	
    private Long id;
    private Long parentId;
    private List<ParentDevice> parentDevices;
    private Boolean hasParent = Boolean.FALSE;
    private String altId;
    private String partNumber;
    private String description;
    private Long productId; //product id from pricing, in the event this was created from pricing import
    private Boolean archived = false;
    private Long defaultOspId;
    private Boolean activateSyncEnabled = Boolean.FALSE;
    private Boolean isCI = Boolean.FALSE;
    private Boolean activateAddBusinessService = Boolean.FALSE;
    private Boolean pricingSheetEnabled = Boolean.FALSE;
    private Boolean requireUnitCount = Boolean.FALSE;
    private List<SPLACost> splaCosts = new ArrayList<SPLACost>();
    private List<Device> relatedDevices = new ArrayList<Device>();
    private Relationship relationship;
    private Integer specUnits;
    private Integer order;
    private List<DeviceProperty> properties = new ArrayList<DeviceProperty>();
    private List<DeviceExpenseCategory> costMappings = new ArrayList<DeviceExpenseCategory>();
    private Boolean costAllocationOption = Boolean.FALSE;
    private Boolean pricingSyncEnabled = Boolean.FALSE;
    private BigDecimal catalogRecurringCost;
    private BigDecimal catalogRecurringPrice;
    private Date created;
    private String createdBy;
    private Date updated;
    private String updatedBy;
    private Integer unitCount;
    private TermDuration termDuration;
    private BillingPlan billingPlan;
    private Segment segment;
    
    // on a contract we'll use other fields
    private Long contractServiceId;
    private String name;
    private String note;
    
    private DeviceType deviceType;
    private String units;
    
    /**
     * default CTOR...
     */
    public Device() {
    }
    
    /**
     * Minimal CTOR.
     */
    public Device(String partNumber, String description) {
        this.partNumber = partNumber;
        this.description = description;
    }
    
    /**
     * Another Minimal CTOR?
     */
    public Device(Long id, String partNumber, String description) {
        this.id = id;
        this.partNumber = partNumber;
        this.description = description;
    }
    
    /**
     * A CTOR for creating a Device (device table).
     */
    public Device(String altId, String partNumber, String description, Date created, String createdBy) {
        this.altId = altId;
        this.partNumber = partNumber;
        this.description = description;
        this.created = created;
        this.createdBy = createdBy;
    }

    public Device(String altId, String partNumber, String description, Long productId, Long defaultOspId, Boolean archived, Boolean activateSyncEnabled, Boolean isCI, Boolean activateAddBusinessService, Boolean pricingSheetEnabled, Boolean requireUnitCount, DeviceType deviceType, Boolean costAllocationOption, Boolean pricingSyncEnabled, String units, TermDuration termDuration, BillingPlan billingPlan, Segment segment, Date created, String createdBy) {
        this.altId = altId;
        this.partNumber = partNumber;
        this.description = description;
        this.productId = productId;
        this.defaultOspId = defaultOspId;
        this.archived = archived;
        this.isCI = isCI;
        this.activateSyncEnabled = activateSyncEnabled;
        this.activateAddBusinessService = activateAddBusinessService;
        this.pricingSheetEnabled = pricingSheetEnabled;
        this.requireUnitCount = requireUnitCount;
        this.costAllocationOption = costAllocationOption;
        this.pricingSyncEnabled = pricingSyncEnabled;
        this.deviceType = deviceType;
        this.units = units;
        this.termDuration = termDuration;
        this.billingPlan = billingPlan;
        this.segment = segment;
        this.created = created;
        this.createdBy = createdBy;
    }
    
    /**
     * A CTOR suited for returning data from contract_service_device table.
     */
    
    public Device(Long id, Long contractServiceId, String altId, String partNumber, String description,
            String name, String note) {
        this.id = id;
        this.contractServiceId = contractServiceId;
        this.altId = altId;
        this.partNumber = partNumber;
        this.description = description;
        this.name = name;
        this.note = note;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAltId() {
        return altId;
    }

    public void setAltId(String altId) {
        this.altId = altId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public List<ParentDevice> getParentDevices() {
        return parentDevices;
    }

    public void setParentDevices(List<ParentDevice> parentDevices) {
        this.parentDevices = parentDevices;
    }

    public Boolean getHasParent() {
        return hasParent;
    }

    public void setHasParent(Boolean hasParent) {
        this.hasParent = hasParent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public Long getProductId() {
		return productId;
	}

	public void setProductId(Long productId) {
		this.productId = productId;
	}

	public Boolean getArchived() {
		return archived;
	}

	public void setArchived(Boolean archived) {
		this.archived = archived;
	}

	public Long getDefaultOspId() {
		return defaultOspId;
	}

	public void setDefaultOspId(Long defaultOspId) {
		this.defaultOspId = defaultOspId;
	}

	public Boolean getActivateSyncEnabled() {
		return activateSyncEnabled;
	}

	public void setActivateSyncEnabled(Boolean activateSyncEnabled) {
		this.activateSyncEnabled = activateSyncEnabled;
	}

	public Boolean getPricingSheetEnabled() {
		return pricingSheetEnabled;
	}

	public void setPricingSheetEnabled(Boolean pricingSheetEnabled) {
		this.pricingSheetEnabled = pricingSheetEnabled;
	}

	public Boolean getIsCI() {
		return isCI;
	}

	public void setIsCI(Boolean isCI) {
		this.isCI = isCI;
	}

	public Boolean getActivateAddBusinessService() {
		return activateAddBusinessService;
	}

	public void setActivateAddBusinessService(Boolean activateAddBusinessService) {
		this.activateAddBusinessService = activateAddBusinessService;
	}

	public Boolean getRequireUnitCount() {
		return requireUnitCount;
	}

	public void setRequireUnitCount(Boolean requireUnitCount) {
		this.requireUnitCount = requireUnitCount;
	}

	public List<SPLACost> getSplaCosts() {
		return splaCosts;
	}

	public void setSplaCosts(List<SPLACost> splaCosts) {
		this.splaCosts = splaCosts;
	}

	public Date getCreated() {
        return created;
    }

    public List<Device> getRelatedDevices() {
        return relatedDevices;
    }

    public void setRelatedDevices(List<Device> relatedDevices) {
        this.relatedDevices = relatedDevices;
    }
    
    public void addRelatedDevice(Device device) {
        if (relatedDevices == null) {
            relatedDevices = new ArrayList<Device>();
        }
        relatedDevices.add(device);
    }

    public Relationship getRelationship() {
        return relationship;
    }

    public void setRelationship(Relationship relationship) {
        this.relationship = relationship;
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

    public Long getContractServiceId() {
        return contractServiceId;
    }

    public void setContractServiceId(Long contractServiceId) {
        this.contractServiceId = contractServiceId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

	public DeviceType getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(DeviceType deviceType) {
		this.deviceType = deviceType;
	}

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public Integer getSpecUnits() {
        return specUnits;
    }

    public void setSpecUnits(Integer specUnits) {
        this.specUnits = specUnits;
    }

	public Integer getOrder() {
		return order;
	}

	public void setOrder(Integer order) {
		this.order = order;
	}

	public List<DeviceProperty> getProperties() {
		return properties;
	}

	public void setProperties(List<DeviceProperty> properties) {
		this.properties = properties;
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
        costMappings.add(costMapping);
    }

	public Boolean getCostAllocationOption() {
		return costAllocationOption;
	}

	public void setCostAllocationOption(Boolean costAllocationOption) {
		this.costAllocationOption = costAllocationOption;
	}

	public Boolean getPricingSyncEnabled() {
		return pricingSyncEnabled;
	}

	public void setPricingSyncEnabled(Boolean pricingSyncEnabled) {
		this.pricingSyncEnabled = pricingSyncEnabled;
	}

	public BigDecimal getCatalogRecurringCost() {
		return catalogRecurringCost;
	}

	public void setCatalogRecurringCost(BigDecimal catalogRecurringCost) {
		this.catalogRecurringCost = catalogRecurringCost;
	}

	public BigDecimal getCatalogRecurringPrice() {
		return catalogRecurringPrice;
	}

	public void setCatalogRecurringPrice(BigDecimal catalogRecurringPrice) {
		this.catalogRecurringPrice = catalogRecurringPrice;
	}

	public Integer getUnitCount() {
		return unitCount;
	}

	public void setUnitCount(Integer unitCount) {
		this.unitCount = unitCount;
	}

	public TermDuration getTermDuration() {
		return termDuration;
	}

	public void setTermDuration(TermDuration termDuration) {
		this.termDuration = termDuration;
	}

	public BillingPlan getBillingPlan() {
		return billingPlan;
	}

	public void setBillingPlan(BillingPlan billingPlan) {
		this.billingPlan = billingPlan;
	}

	public Segment getSegment() {
		return segment;
	}

	public void setSegment(Segment segment) {
		this.segment = segment;
	}
    
}
