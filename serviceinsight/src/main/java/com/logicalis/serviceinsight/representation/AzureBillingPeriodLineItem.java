package com.logicalis.serviceinsight.representation;

import java.math.BigDecimal;

public class AzureBillingPeriodLineItem {

	private String id;
	private String groupKey;
	private String name;
	private BigDecimal quantity;
	private BigDecimal total;
	
	public AzureBillingPeriodLineItem(){}
	
	public AzureBillingPeriodLineItem(String id, String groupKey, String name, BigDecimal quantity, BigDecimal total) {
		super();
		this.id = id;
		this.groupKey = groupKey;
		this.name = name;
		this.quantity = quantity;
		this.total = total;
	}

	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getGroupKey() {
		return groupKey;
	}

	public void setGroupKey(String groupKey) {
		this.groupKey = groupKey;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public BigDecimal getQuantity() {
		return quantity;
	}
	
	public void setQuantity(BigDecimal quantity) {
		this.quantity = quantity;
	}
	
	public BigDecimal getTotal() {
		return total;
	}
	
	public void setTotal(BigDecimal total) {
		this.total = total;
	}
	
}
