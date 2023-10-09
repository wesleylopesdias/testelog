package com.logicalis.serviceinsight.representation;

import java.io.Serializable;

public class PricingPipelineService implements Serializable, Comparable<PricingPipelineService> {

    private String serviceOfferingName;
    private String serviceName;
    private Long totalItems;
    private String unitLabel;
    
    public String getServiceOfferingName() {
        return serviceOfferingName;
    }
    
    public void setServiceOfferingName(String serviceOfferingName) {
        this.serviceOfferingName = serviceOfferingName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    
    public Long getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(Long totalItems) {
        this.totalItems = totalItems;
    }
    
    public String getUnitLabel() {
        return unitLabel;
    }
    
    public void setUnitLabel(String unitLabel) {
        this.unitLabel = unitLabel;
    }

    @Override
    public int compareTo(PricingPipelineService rec) {
        int idx = 0;
        if (this.getServiceOfferingName() != null && rec.getServiceOfferingName() != null) {
            idx = (this.getServiceOfferingName().compareTo(rec.getServiceOfferingName()));
            if (idx != 0) {
                return idx;
            } else {
                if (this.getServiceName() != null && rec.getServiceName() != null) {
                    return (this.getServiceName().compareTo(rec.getServiceName()));
                } else if (this.getServiceName() != null) {
                    return 1;
                } else if (rec.getServiceName() != null) {
                    return -1;
                }
            }
        } else if (this.getServiceOfferingName() != null) {
            return 1;
        } else if (rec.getServiceOfferingName() != null) {
            return -1;
        }
        return idx;
    }
    
}
