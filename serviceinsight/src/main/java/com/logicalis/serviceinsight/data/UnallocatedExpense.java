package com.logicalis.serviceinsight.data;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

public class UnallocatedExpense {

	private Long id;
	private String altId;
	private String name;
	private String description;
	private BigDecimal amount = new BigDecimal(0);
	private Integer quantity;
	private Long ospId;
	private String serviceName;
	private String vendor;
	private String poNumber;
	private Long costAllocationId;
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="MMddyyyy", timezone="America/New_York")
	private Date month;
	
	public UnallocatedExpense() {}
	
	public UnallocatedExpense(Long id, String altId, String name, String description, BigDecimal amount,
			Integer quantity, Long ospId, String serviceName, String vendor, String poNumber, Long costAllocationId, Date month) {
		super();
		this.id = id;
		this.altId = altId;
		this.name = name;
		this.description = description;
		this.amount = amount;
		this.quantity = quantity;
		this.ospId = ospId;
		this.serviceName = serviceName;
		this.vendor = vendor;
		this.poNumber = poNumber;
		this.costAllocationId = costAllocationId;
		this.month = month;
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
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public BigDecimal getAmount() {
		return amount;
	}
	
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	
	public Integer getQuantity() {
		return quantity;
	}
	
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
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

	public String getVendor() {
		return vendor;
	}
	
	public void setVendor(String vendor) {
		this.vendor = vendor;
	}
	
	public String getPoNumber() {
		return poNumber;
	}
	
	public void setPoNumber(String poNumber) {
		this.poNumber = poNumber;
	}
	
	public Long getCostAllocationId() {
		return costAllocationId;
	}
	
	public void setCostAllocationId(Long costAllocationId) {
		this.costAllocationId = costAllocationId;
	}
	
	public Date getMonth() {
		return month;
	}
	
	public void setMonth(Date month) {
		this.month = month;
	}

	@Override
	public String toString() {
		return "UnallocatedExpense [id=" + id + ", altId=" + altId + ", name=" + name + ", description=" + description
				+ ", amount=" + amount + ", quantity=" + quantity + ", ospId=" + ospId + ", vendor=" + vendor
				+ ", poNumber=" + poNumber + ", costAllocationId=" + costAllocationId + ", month=" + month + "]";
	}
	
	
	
	
}
