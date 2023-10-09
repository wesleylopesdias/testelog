package com.logicalis.serviceinsight.data;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.logicalis.serviceinsight.dao.CostItem.CostType;

public class SIDataWarehouseCostItem {

	private Long id;
    private String name;
    private BigDecimal amount;
    private Integer quantity;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy", timezone = "America/New_York")
    private Date applied;
    private Long customerId;
    private String customerName;
    private Long contractId;
    private String contractName;
    private String jobNumber;
    private CostType costType;
    
    public SIDataWarehouseCostItem() {}
    
	public SIDataWarehouseCostItem(Long id, String name, BigDecimal amount, Integer quantity, Date applied,
			Long customerId, String customerName, Long contractId, String contractName, String jobNumber,
			CostType costType) {
		super();
		this.id = id;
		this.name = name;
		this.amount = amount;
		this.quantity = quantity;
		this.applied = applied;
		this.customerId = customerId;
		this.customerName = customerName;
		this.contractId = contractId;
		this.contractName = contractName;
		this.jobNumber = jobNumber;
		this.costType = costType;
	}

	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
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
	
	public Date getApplied() {
		return applied;
	}
	
	public void setApplied(Date applied) {
		this.applied = applied;
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
	
	public CostType getCostType() {
		return costType;
	}
	
	public void setCostType(CostType costType) {
		this.costType = costType;
	}

	@Override
	public String toString() {
		return "SIDataWarehouseCostItem [id=" + id + ", name=" + name + ", amount=" + amount + ", quantity=" + quantity
				+ ", applied=" + applied + ", customerId=" + customerId + ", customerName=" + customerName
				+ ", contractId=" + contractId + ", contractName=" + contractName + ", jobNumber=" + jobNumber
				+ ", costType=" + costType + "]";
	}
	
}
