package com.logicalis.serviceinsight.dao;

import java.math.BigDecimal;

/**
 * Represents an expense in the system
 * 
 * @author poneil
 */
public class Expense extends BaseDao {
	
	public enum ExpenseType {
		asset("Asset"), cost("Expense");
		
		private String description;

		ExpenseType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return this.description;
        }
	}
	
    private Long id;
    private String altId;
    private String name;
    private String description;
    private BigDecimal amount;
    private Integer quantity;
    private Long customerId;
    private Long contractId;
    private Integer locationId;
    private ExpenseType expenseType;
    
    public Expense() {
        
    }
    
    public Expense(Long id, String altId, ExpenseType expenseType, String name, String description,
            BigDecimal amount, Integer quantity, Long customerId, Long contractId, Integer locationId) {
        this.id = id;
        this.altId = altId;
        this.expenseType = expenseType;
        this.name = name;
        this.description = description;
        this.amount = amount;
        this.quantity = quantity;
        this.customerId = customerId;
        this.contractId = contractId;
        this.locationId = locationId;
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

    public Integer getLocationId() {
        return locationId;
    }

    public void setLocationId(Integer locationId) {
        this.locationId = locationId;
    }

    public ExpenseType getExpenseType() {
            return expenseType;
    }

    public void setExpenseType(ExpenseType expenseType) {
            this.expenseType = expenseType;
    }

    @Override
    public String toString() {
        return "Expense{" + "id=" + id + ", altId=" + altId + ", name=" + name + ", description=" + description + ", amount=" + amount + ", quantity=" + quantity + ", customerId=" + customerId + ", contractId=" + contractId + ", locationId=" + locationId + ", expenseType=" + expenseType + '}';
    }
}
