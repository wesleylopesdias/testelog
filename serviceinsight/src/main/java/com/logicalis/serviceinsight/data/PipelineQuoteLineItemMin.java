package com.logicalis.serviceinsight.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PipelineQuoteLineItemMin {

    private Long id;    
    private String name;
    private Integer quantity;
    private String unitLabel;
    private String serviceOfferingName;
    private String productType;
    private String businessModel;
    private String technology;
    
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
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public String getUnitLabel() {
        return unitLabel;
    }
    
    public void setUnitLabel(String unitLabel) {
        this.unitLabel = unitLabel;
    }
    
    public String getServiceOfferingName() {
        return serviceOfferingName;
    }
    
    public void setServiceOfferingName(String serviceOfferingName) {
        this.serviceOfferingName = serviceOfferingName;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getBusinessModel() {
        return businessModel;
    }

    public void setBusinessModel(String businessModel) {
        this.businessModel = businessModel;
    }

    public String getTechnology() {
        return technology;
    }

    public void setTechnology(String technology) {
        this.technology = technology;
    }
    
}
