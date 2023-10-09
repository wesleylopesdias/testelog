package com.logicalis.serviceinsight.data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class QuoteLineItemMin {

	private Long id;
	private String name;
    private String partNumberCode;
    private BigDecimal onetimeCost;
    private BigDecimal onetimePrice;
    private BigDecimal netOnetimePrice;
    private BigDecimal recurringCost;
    private BigDecimal recurringPrice;
    private BigDecimal netRecurringPrice;
    private Long serviceOfferingId;
    private Integer quantity;
    private Long productId;
    private String productType;
    private String altId;
    private String ruleCode;
    private Boolean importAsUnits;
    private String description;
    private Long parentId;
    private List<QuoteLineItemMin> relatedQuoteLineItems = new ArrayList<QuoteLineItemMin>();
    
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
	
	public BigDecimal getNetOnetimePrice() {
		return netOnetimePrice;
	}
	
	public void setNetOnetimePrice(BigDecimal netOnetimePrice) {
		this.netOnetimePrice = netOnetimePrice;
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
	
	public BigDecimal getNetRecurringPrice() {
		return netRecurringPrice;
	}
	
	public void setNetRecurringPrice(BigDecimal netRecurringPrice) {
		this.netRecurringPrice = netRecurringPrice;
	}
	
	public Long getServiceOfferingId() {
		return serviceOfferingId;
	}
	
	public void setServiceOfferingId(Long serviceOfferingId) {
		this.serviceOfferingId = serviceOfferingId;
	}
	
	public Integer getQuantity() {
		return quantity;
	}
	
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
	
	public Long getProductId() {
		return productId;
	}
	
	public void setProductId(Long productId) {
		this.productId = productId;
	}
	
	public String getProductType() {
		return productType;
	}
	
	public void setProductType(String productType) {
		this.productType = productType;
	}
	
	public String getAltId() {
		return altId;
	}

	public void setAltId(String altId) {
		this.altId = altId;
	}

	public String getRuleCode() {
		return ruleCode;
	}
	
	public void setRuleCode(String ruleCode) {
		this.ruleCode = ruleCode;
	}
	
	public Boolean getImportAsUnits() {
		return importAsUnits;
	}
	
	public void setImportAsUnits(Boolean importAsUnits) {
		this.importAsUnits = importAsUnits;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

	public List<QuoteLineItemMin> getRelatedQuoteLineItems() {
		return relatedQuoteLineItems;
	}

	public void setRelatedQuoteLineItems(List<QuoteLineItemMin> relatedQuoteLineItems) {
		this.relatedQuoteLineItems = relatedQuoteLineItems;
	}

	@Override
	public String toString() {
		return "QuoteLineItemMin [id=" + id + ", name=" + name + ", partNumberCode=" + partNumberCode + ", onetimeCost="
				+ onetimeCost + ", onetimePrice=" + onetimePrice + ", netOnetimePrice=" + netOnetimePrice
				+ ", recurringCost=" + recurringCost + ", recurringPrice=" + recurringPrice + ", netRecurringPrice="
				+ netRecurringPrice + ", serviceOfferingId=" + serviceOfferingId + ", quantity=" + quantity
				+ ", productId=" + productId + ", productType=" + productType + ", altId=" + altId + ", ruleCode="
				+ ruleCode + ", importAsUnits=" + importAsUnits + ", description=" + description + ", parentId="
				+ parentId + ", relatedQuoteLineItems=" + relatedQuoteLineItems + "]";
	}
	
	
    
}
