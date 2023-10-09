package com.logicalis.serviceinsight.representation;

import java.math.BigDecimal;

public class AWSBillingPeriodLineItem {

	private String name;
	private BigDecimal total;
	
	public AWSBillingPeriodLineItem(){}
	
	public AWSBillingPeriodLineItem(String name, BigDecimal total) {
		super();
		this.name = name;
		this.total = total;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public BigDecimal getTotal() {
		return total;
	}
	public void setTotal(BigDecimal total) {
		this.total = total;
	}
	
}
