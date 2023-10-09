package com.logicalis.serviceinsight.data;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CostAllocationLineItem {

	private Long id;
	private Long costAllocationId;
	private Long ospId;
	private String serviceName;
	private Long deviceId;
	private String deviceDescription;
	private String devicePartNumber;
	private BigDecimal multiTenantAllocation = new BigDecimal(0);
	private BigDecimal multiTenantAmount = new BigDecimal(0);
	private BigDecimal rentAllocation = new BigDecimal(0);
	private BigDecimal rentAmount = new BigDecimal(0);
	private BigDecimal specificAllocation = new BigDecimal(0);
	private BigDecimal specificAmount = new BigDecimal(0);
	private BigDecimal serviceSpecificTotal = new BigDecimal(0);
	private BigDecimal totalAmount = new BigDecimal(0);
	private String infrastructureNote;
	private Integer units;
	private BigDecimal calculatedUnitCost;
	private BigDecimal costModelPerUnit;
	
	public CostAllocationLineItem() {}
	
	public CostAllocationLineItem(Long id, Long costAllocationId, Long ospId, Long deviceId, String deviceDescription, String devicePartNumber,
			BigDecimal multiTenantAllocation, BigDecimal rentAllocation, BigDecimal specificAllocation, String infrastructureNote, Integer units,
			BigDecimal costModelPerUnit) {
		super();
		this.id = id;
		this.costAllocationId = costAllocationId;
		this.ospId = ospId;
		this.deviceId = deviceId;
		this.deviceDescription = deviceDescription;
		this.devicePartNumber = devicePartNumber;
		this.multiTenantAllocation = multiTenantAllocation;
		this.rentAllocation = rentAllocation;
		this.specificAllocation = specificAllocation;
		this.infrastructureNote = infrastructureNote;
		this.units = units;
		this.costModelPerUnit = costModelPerUnit;
	}

	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public Long getCostAllocationId() {
		return costAllocationId;
	}
	
	public void setCostAllocationId(Long costAllocationId) {
		this.costAllocationId = costAllocationId;
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

	public Long getDeviceId() {
		return deviceId;
	}
	
	public void setDeviceId(Long deviceId) {
		this.deviceId = deviceId;
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

	public BigDecimal getMultiTenantAllocation() {
		return multiTenantAllocation;
	}
	
	public void setMultiTenantAllocation(BigDecimal multiTenantAllocation) {
		this.multiTenantAllocation = multiTenantAllocation;
	}
	
	public BigDecimal getMultiTenantAmount() {
		return multiTenantAmount;
		//return multiTenantTotal.multiply(getMultiTenantAllocation().divide(new BigDecimal(100))).setScale(2, BigDecimal.ROUND_HALF_UP);
	}
	
	public void setMultiTenantAmount(BigDecimal multiTenantAmount) {
		this.multiTenantAmount = multiTenantAmount;
	}
	
	public BigDecimal getRentAllocation() {
		return rentAllocation;
	}
	
	public void setRentAllocation(BigDecimal rentAllocation) {
		this.rentAllocation = rentAllocation;
	}
	
	public BigDecimal getRentAmount() {
		return rentAmount;
	}
	
	public void setRentAmount(BigDecimal rentAmount) {
		this.rentAmount = rentAmount;
	}
	
	public BigDecimal getSpecificAllocation() {
		return specificAllocation;
	}
	
	public void setSpecificAllocation(BigDecimal specificAllocation) {
		this.specificAllocation = specificAllocation;
	}
	
	public BigDecimal getSpecificAmount() {
		return specificAmount;
	}
	
	public void setSpecificAmount(BigDecimal specificAmount) {
		this.specificAmount = specificAmount;
	}
	
	public BigDecimal getServiceSpecificTotal() {
		return serviceSpecificTotal;
	}

	public void setServiceSpecificTotal(BigDecimal serviceSpecificTotal) {
		this.serviceSpecificTotal = serviceSpecificTotal;
	}

	public String getInfrastructureNote() {
		return infrastructureNote;
	}

	public void setInfrastructureNote(String infrastructureNote) {
		this.infrastructureNote = infrastructureNote;
	}

	public Integer getUnits() {
		return units;
	}
	
	public void setUnits(Integer units) {
		this.units = units;
	}
	
	public BigDecimal getTotalAmount() {
		BigDecimal total = getMultiTenantAmount().add(getRentAmount()).add(getSpecificAmount()).setScale(2, RoundingMode.HALF_UP);
		return total;
	}

	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}

	public BigDecimal getCalculatedUnitCost() {
		BigDecimal unitCost = new BigDecimal(0);
		if(getUnits() > 0) {
			unitCost = getTotalAmount().divide(new BigDecimal(getUnits()), 2, RoundingMode.HALF_UP);
		}
		
		return unitCost;
	}
	
	public void setCalculatedUnitCost(BigDecimal calculatedUnitCost) {
		this.calculatedUnitCost = calculatedUnitCost;
	}
	
	public BigDecimal getCostModelPerUnit() {
		return costModelPerUnit;
	}
	
	public void setCostModelPerUnit(BigDecimal costModelPerUnit) {
		this.costModelPerUnit = costModelPerUnit;
	}
	
	public BigDecimal getVariance() {
		BigDecimal variance = new BigDecimal(0);
		BigDecimal costModelPerUnit = new BigDecimal(0);
		BigDecimal calculatedUnitCost = new BigDecimal(0);
		
		if(getCostModelPerUnit() != null) {
			costModelPerUnit = getCostModelPerUnit();
		}
		
		if(getCalculatedUnitCost() != null) {
			calculatedUnitCost = getCalculatedUnitCost();
		}
		
		if(costModelPerUnit.compareTo(BigDecimal.ZERO) > 0) {
			variance = costModelPerUnit.subtract(calculatedUnitCost).divide(costModelPerUnit, 2, RoundingMode.HALF_UP);
		}
		
		return variance;
	}

	@Override
	public String toString() {
		return "CostAllocationLineItem [id=" + id + ", costAllocationId=" + costAllocationId + ", ospId=" + ospId
				+ ", serviceName=" + serviceName + ", deviceId=" + deviceId + ", deviceDescription=" + deviceDescription
				+ ", devicePartNumber=" + devicePartNumber + ", multiTenantAllocation=" + multiTenantAllocation
				+ ", multiTenantAmount=" + multiTenantAmount + ", rentAllocation=" + rentAllocation + ", rentAmount="
				+ rentAmount + ", specificAllocation=" + specificAllocation + ", specificAmount=" + specificAmount
				+ ", totalAmount=" + totalAmount + ", units=" + units + ", calculatedUnitCost=" + calculatedUnitCost
				+ ", costModelPerUnit=" + costModelPerUnit + "]";
	}
	
}
