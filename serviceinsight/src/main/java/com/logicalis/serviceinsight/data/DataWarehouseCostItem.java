package com.logicalis.serviceinsight.data;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

public class DataWarehouseCostItem {

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy", timezone = "America/New_York")
	private Date month;
	private String customer;
	private Long id;
	private String jobNumber;
	private String sowName;
	private String costName;
	private Integer quantity;
	private BigDecimal cost;
	private String type;
	
	public DataWarehouseCostItem() {}
	
	public DataWarehouseCostItem(Date month, String customer, Long id, String jobNumber, String sowName,
			String costName, Integer quantity, BigDecimal cost, String type) {
		super();
		this.month = month;
		this.customer = customer;
		this.id = id;
		this.jobNumber = jobNumber;
		this.sowName = sowName;
		this.costName = costName;
		this.quantity = quantity;
		this.cost = cost;
		this.type = type;
	}

	public Date getMonth() {
		return month;
	}
	
	public void setMonth(Date month) {
		this.month = month;
	}
	
	public String getCustomer() {
		return customer;
	}
	
	public void setCustomer(String customer) {
		this.customer = customer;
	}
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getJobNumber() {
		return jobNumber;
	}
	
	public void setJobNumber(String jobNumber) {
		this.jobNumber = jobNumber;
	}
	
	public String getSowName() {
		return sowName;
	}
	
	public void setSowName(String sowName) {
		this.sowName = sowName;
	}
	
	public String getCostName() {
		return costName;
	}
	
	public void setCostName(String costName) {
		this.costName = costName;
	}
	
	public Integer getQuantity() {
		return quantity;
	}
	
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
	
	public BigDecimal getCost() {
		return cost;
	}
	
	public void setCost(BigDecimal cost) {
		this.cost = cost;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "DataWarehouseCostItem [month=" + month + ", customer=" + customer + ", id=" + id + ", jobNumber="
				+ jobNumber + ", sowName=" + sowName + ", costName=" + costName + ", quantity=" + quantity + ", cost="
				+ cost + ", type=" + type + "]";
	}
	
	
}
