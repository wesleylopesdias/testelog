package com.logicalis.serviceinsight.representation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.logicalis.serviceinsight.data.ContractAdjustment;
import com.logicalis.serviceinsight.data.ContractUpdate;
import com.logicalis.serviceinsight.data.Service;

public class ContractRollupRecord {

	private enum RowType {
		ContractService, ContractAdjustment
	}
	
	private Long id;
	private RowType rowType;
    private Long serviceId;
    private Long contractId;
    private Long contractGroupId;
    private String ospId;
    private String name;
    private BigDecimal onetimeRevenue;
    private BigDecimal recurringRevenue;
    private BigDecimal unitPriceRecurringRevenue;
    private BigDecimal fullMonthRecurringRevenue;
    private String note;
    private Integer quantity;
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="MMddyyyy", timezone="America/New_York")
    private Date startDate;
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="MMddyyyy", timezone="America/New_York")
    private Date endDate;
    private String deviceName;
    private String devicePartNumber;
    private String deviceDescription;
    private Long deviceId;
    private boolean hasPendingRecord;
    private Integer unitCount;
    private Boolean isProRatedAmount = Boolean.FALSE;
    private List<ContractUpdate> contractUpdates = new ArrayList<ContractUpdate>();
    
    public ContractRollupRecord(){}

    public ContractRollupRecord(Service source, Boolean rollupRelatedLineItems){
    	this.id = source.getId();
    	this.rowType = RowType.ContractService;
    	this.serviceId = source.getServiceId();
    	this.contractId = source.getContractId();
        this.contractGroupId = source.getContractGroupId();
    	this.ospId = source.getOspId();
    	this.name = source.getName();
    	this.fullMonthRecurringRevenue = source.getFullMonthRecurringRevenue();
    	this.note = source.getNote();
    	this.quantity = source.getQuantity();
    	this.startDate = source.getStartDate();
    	this.endDate = source.getEndDate();
    	this.deviceName = source.getDeviceName();
    	this.devicePartNumber = source.getDevicePartNumber();
    	this.deviceDescription = source.getDeviceDescription();
    	this.deviceId = source.getDeviceId();
    	this.contractUpdates = source.getContractUpdates();
    	this.unitPriceRecurringRevenue = source.getUnitPriceRecurringRevenue();
    	this.hasPendingRecord = (source.getHasPendingRecord() != null) ? source.getHasPendingRecord() : Boolean.FALSE;
    	this.unitCount = source.getDeviceUnitCount();
    	this.isProRatedAmount = source.isProRatedAmount();
    	
    	//adding the child service dollar amounts into the parents
		BigDecimal onetimeRevenue = source.getOnetimeRevenue();
    	BigDecimal recurringRevenue = source.getRecurringRevenue();
    	
    	if(rollupRelatedLineItems) {
	    	List<Service> relatedLineItems = source.getRelatedLineItems();
	    	if(relatedLineItems != null && !relatedLineItems.isEmpty()) {
	    		for(Service relatedLineItem: relatedLineItems) {
	    			onetimeRevenue = onetimeRevenue.add(relatedLineItem.getOnetimeRevenue());
	    			recurringRevenue = recurringRevenue.add(relatedLineItem.getRecurringRevenue());
	    			
	    			List<Service> grandChildLineItems = relatedLineItem.getRelatedLineItems();
	    			if(grandChildLineItems != null && !grandChildLineItems.isEmpty()) {
		    			for(Service grandChildLineItem: grandChildLineItems) {
			    			onetimeRevenue = onetimeRevenue.add(grandChildLineItem.getOnetimeRevenue());
			    			recurringRevenue = recurringRevenue.add(grandChildLineItem.getRecurringRevenue());
			    		}
	    			}
	    		}
	    	}
    	}
    	
    	this.setOnetimeRevenue(onetimeRevenue);
    	this.setRecurringRevenue(recurringRevenue);
    }
    
    public ContractRollupRecord(ContractAdjustment source, String name){
    	this.id = source.getId();
    	this.rowType = RowType.ContractAdjustment;
    	this.contractId = source.getContractId();
    	this.name = name;
    	
    	if("onetime".equals(source.getAdjustmentType())) {
    		this.onetimeRevenue = source.getAdjustment();
    	} else {
    		this.recurringRevenue = source.getAdjustment();
    	}
    	
    	this.contractUpdates = source.getContractUpdates();
    	this.note = source.getNote();
    	this.startDate = source.getStartDate();
    	this.endDate = source.getEndDate();
    	this.hasPendingRecord = Boolean.FALSE;
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    public RowType getRowType() {
		return rowType;
	}

	public void setRowType(RowType rowType) {
		this.rowType = rowType;
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

    public Long getContractGroupId() {
        return contractGroupId;
    }

    public void setContractGroupId(Long contractGroupId) {
        this.contractGroupId = contractGroupId;
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

    public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public BigDecimal getOnetimeRevenue() {
        return onetimeRevenue;
    }

    public void setOnetimeRevenue(BigDecimal onetimeRevenue) {
        this.onetimeRevenue = onetimeRevenue;
    }

    public BigDecimal getRecurringRevenue() {
        return recurringRevenue;
    }

    public void setRecurringRevenue(BigDecimal recurringRevenue) {
        this.recurringRevenue = recurringRevenue;
    }

    public BigDecimal getUnitPriceRecurringRevenue() {
		return unitPriceRecurringRevenue;
	}

	public void setUnitPriceRecurringRevenue(BigDecimal unitPriceRecurringRevenue) {
		this.unitPriceRecurringRevenue = unitPriceRecurringRevenue;
	}

	public BigDecimal getFullMonthRecurringRevenue() {
		return fullMonthRecurringRevenue;
	}

	public void setFullMonthRecurringRevenue(BigDecimal fullMonthRecurringRevenue) {
		this.fullMonthRecurringRevenue = fullMonthRecurringRevenue;
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

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public boolean isHasPendingRecord() {
		return hasPendingRecord;
	}

	public void setHasPendingRecord(boolean hasPendingRecord) {
		this.hasPendingRecord = hasPendingRecord;
	}

	public Integer getUnitCount() {
		return unitCount;
	}

	public void setUnitCount(Integer unitCount) {
		this.unitCount = unitCount;
	}

	public List<ContractUpdate> getContractUpdates() {
        return contractUpdates;
    }

	public Boolean getIsProRatedAmount() {
		return isProRatedAmount;
	}

	public void setIsProRatedAmount(Boolean isProRatedAmount) {
		this.isProRatedAmount = isProRatedAmount;
	}

	public void setContractUpdates(List<ContractUpdate> contractUpdates) {
        this.contractUpdates = contractUpdates;
    }
    
    public void addContractUpdate(ContractUpdate contractUpdate) {
        this.contractUpdates.add(contractUpdate);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.serviceId);
        hash = 67 * hash + Objects.hashCode(this.contractId);
        hash = 67 * hash + Objects.hashCode(this.startDate);
        hash = 67 * hash + Objects.hashCode(this.endDate);
        hash = 67 * hash + Objects.hashCode(this.devicePartNumber);
        hash = 67 * hash + Objects.hashCode(this.deviceDescription);
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
        final ContractRollupRecord other = (ContractRollupRecord) obj;
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
    
}
