package com.logicalis.serviceinsight.data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PricingProductMin implements Comparable<PricingProductMin> {
	
	private Long id;
	private String name;
	private String partNumberCode;
	private String status;
	@JsonProperty(value = "productType")
	private String deviceType;
	private String altId;
	private BigDecimal onetimeCost;
	private BigDecimal onetimePrice;
	private BigDecimal recurringCost;
	private BigDecimal recurringPrice;
	private String termDuration;
	private String billingPlan;
	private String segment;
	private List<ServiceMin> serviceOfferings = new ArrayList<ServiceMin>();
	
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

	public String getPartNumberCode() {
		return partNumberCode;
	}

	public void setPartNumberCode(String partNumberCode) {
		this.partNumberCode = partNumberCode;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	public String getAltId() {
		return altId;
	}

	public void setAltId(String altId) {
		this.altId = altId;
	}

	public BigDecimal getOnetimeCost() {
		return onetimeCost;
	}

	public void setOnetimeCost(BigDecimal onetimeCost) {
		this.onetimeCost = onetimeCost;
	}

	public BigDecimal getOnetimePrice() {
		return onetimePrice;
	}

	public void setOnetimePrice(BigDecimal onetimePrice) {
		this.onetimePrice = onetimePrice;
	}

	public BigDecimal getRecurringCost() {
		return recurringCost;
	}

	public void setRecurringCost(BigDecimal recurringCost) {
		this.recurringCost = recurringCost;
	}

	public BigDecimal getRecurringPrice() {
		return recurringPrice;
	}

	public void setRecurringPrice(BigDecimal recurringPrice) {
		this.recurringPrice = recurringPrice;
	}

	public String getTermDuration() {
		return termDuration;
	}

	public void setTermDuration(String termDuration) {
		this.termDuration = termDuration;
	}

	public String getBillingPlan() {
		return billingPlan;
	}

	public void setBillingPlan(String billingPlan) {
		this.billingPlan = billingPlan;
	}

	public String getSegment() {
		return segment;
	}

	public void setSegment(String segment) {
		this.segment = segment;
	}

	public List<ServiceMin> getServiceOfferings() {
		return serviceOfferings;
	}

	public void setServiceOfferings(List<ServiceMin> serviceOfferings) {
		this.serviceOfferings = serviceOfferings;
	}
	

	@Override
    public int compareTo(PricingProductMin o) {
        if (o == null) {
            return 1;
        }
        if (getId() != null) {
            if (o.getId() == null) {
                return 1;
            }
            int idx = getId().compareTo(o.getId());
            if (idx != 0) {
                return idx;
            }
        }
        return 0;
    }
	
}
