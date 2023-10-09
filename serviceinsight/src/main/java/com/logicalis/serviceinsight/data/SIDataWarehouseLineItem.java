package com.logicalis.serviceinsight.data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SIDataWarehouseLineItem {

	private String customerName;
	private String jobNumber;
	private String sowName;
	private String ospServiceName;
	private Long lineItemId;
	private String lineItemDescription;
	private String lineItemPartNumber;
	private String ciName;
	private String osmSysId;
	private Integer unitCount;
	private BigDecimal onetimeRevenue;
	private BigDecimal recurringRevenue;
	private Date startDate;
	private Date endDate;
	private String status;
	private BigDecimal unitCost;
        private BigDecimal directCost = BigDecimal.ZERO;
        private BigDecimal serviceToolsCost = BigDecimal.ZERO;
        private BigDecimal nonDepreciatedDirectCost = BigDecimal.ZERO;
        private BigDecimal nonDepreciatedServiceToolsCost = BigDecimal.ZERO;
	private List<SIDataWarehouseLineItem> relatedLineItems =  new ArrayList<SIDataWarehouseLineItem>();
	
	public SIDataWarehouseLineItem() {}
	
	public SIDataWarehouseLineItem(String customerName, String jobNumber, String sowName, String ospServiceName,
			Long lineItemId, String lineItemDescription, String lineItemPartNumber, String ciName, String osmSysId,
			Integer unitCount, BigDecimal onetimeRevenue, BigDecimal recurringRevenue, Date startDate, Date endDate,
			String status) {
		super();
		this.customerName = customerName;
		this.jobNumber = jobNumber;
		this.sowName = sowName;
		this.ospServiceName = ospServiceName;
		this.lineItemId = lineItemId;
		this.lineItemDescription = lineItemDescription;
		this.lineItemPartNumber = lineItemPartNumber;
		this.ciName = ciName;
		this.osmSysId = osmSysId;
		this.unitCount = unitCount;
		this.onetimeRevenue = onetimeRevenue;
		this.recurringRevenue = recurringRevenue;
		this.startDate = startDate;
		this.endDate = endDate;
		this.status = status;
	}

	public String getCustomerName() {
		return customerName;
	}
	
	public void setCustomerName(String customerName) {
		this.customerName = customerName;
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
	
	public String getOspServiceName() {
		return ospServiceName;
	}
	
	public void setOspServiceName(String ospServiceName) {
		this.ospServiceName = ospServiceName;
	}
	
	public Long getLineItemId() {
		return lineItemId;
	}
	
	public void setLineItemId(Long lineItemId) {
		this.lineItemId = lineItemId;
	}
	
	public String getLineItemDescription() {
		return lineItemDescription;
	}
	
	public void setLineItemDescription(String lineItemDescription) {
		this.lineItemDescription = lineItemDescription;
	}
	
	public String getLineItemPartNumber() {
		return lineItemPartNumber;
	}
	
	public void setLineItemPartNumber(String lineItemPartNumber) {
		this.lineItemPartNumber = lineItemPartNumber;
	}
	
	public String getCiName() {
		return ciName;
	}
	
	public void setCiName(String ciName) {
		this.ciName = ciName;
	}
	
	public String getOsmSysId() {
		return osmSysId;
	}
	
	public void setOsmSysId(String osmSysId) {
		this.osmSysId = osmSysId;
	}
	
	public Integer getUnitCount() {
		return unitCount;
	}
	
	public void setUnitCount(Integer unitCount) {
		this.unitCount = unitCount;
	}
	
	public BigDecimal getOnetimeRevenue() {
		return onetimeRevenue;
	}
	
	public void setOnetimeRevenue(BigDecimal onetimeRevenue) {
		this.onetimeRevenue = onetimeRevenue;
	}
	
	public BigDecimal getRecurringRevenue() {
		return recurringRevenue;
	}
	
	public void setRecurringRevenue(BigDecimal recurringRevenue) {
		this.recurringRevenue = recurringRevenue;
	}
	
	public Date getStartDate() {
		return startDate;
	}
	
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	
	public Date getEndDate() {
		return endDate;
	}
	
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public List<SIDataWarehouseLineItem> getRelatedLineItems() {
		return relatedLineItems;
	}

	public void setRelatedLineItems(List<SIDataWarehouseLineItem> relatedLineItems) {
		this.relatedLineItems = relatedLineItems;
	}

	public BigDecimal getUnitCost() {
		return unitCost;
	}

	public void setUnitCost(BigDecimal unitCost) {
		this.unitCost = unitCost;
	}

    public BigDecimal getDirectCost() {
        return directCost;
    }

    public void setDirectCost(BigDecimal directCost) {
        this.directCost = directCost;
    }
    
    public void addDirectCost(BigDecimal incrementalCost) {
        if (incrementalCost == null) {
            return;
        }
        this.directCost = (this.directCost == null ? BigDecimal.ZERO : this.directCost).add(incrementalCost);
    }

    public BigDecimal getServiceToolsCost() {
        return serviceToolsCost;
    }

    public void setServiceToolsCost(BigDecimal serviceToolsCost) {
        this.serviceToolsCost = serviceToolsCost;
    }
    
    public void addServiceToolsCost(BigDecimal incrementalCost) {
        if (incrementalCost == null) {
            return;
        }
        this.serviceToolsCost = (this.serviceToolsCost == null ? BigDecimal.ZERO : this.serviceToolsCost).add(incrementalCost);
    }
    
    public BigDecimal getTotalCost() {
        return BigDecimal.ZERO
                .add((this.directCost == null ? BigDecimal.ZERO : this.directCost))
                .add((this.serviceToolsCost == null ? BigDecimal.ZERO : this.serviceToolsCost));
    }

    public BigDecimal getNonDepreciatedDirectCost() {
        return nonDepreciatedDirectCost;
    }

    public void setNonDepreciatedDirectCost(BigDecimal nonDepreciatedDirectCost) {
        this.nonDepreciatedDirectCost = nonDepreciatedDirectCost;
    }
    
    public void addNonDepreciatedDirectCost(BigDecimal incrementalCost) {
        if (incrementalCost == null) {
            return;
        }
        this.nonDepreciatedDirectCost = (this.nonDepreciatedDirectCost == null ? BigDecimal.ZERO : this.nonDepreciatedDirectCost).add(incrementalCost);
    }

    public BigDecimal getNonDepreciatedServiceToolsCost() {
        return nonDepreciatedServiceToolsCost;
    }

    public void setNonDepreciatedServiceToolsCost(BigDecimal nonDepreciatedServiceToolsCost) {
        this.nonDepreciatedServiceToolsCost = nonDepreciatedServiceToolsCost;
    }
    
    public void addNonDepreciatedServiceToolsCost(BigDecimal incrementalCost) {
        if (incrementalCost == null) {
            return;
        }
        this.nonDepreciatedServiceToolsCost = (this.nonDepreciatedServiceToolsCost == null ? BigDecimal.ZERO : this.nonDepreciatedServiceToolsCost).add(incrementalCost);
    }
    
    public BigDecimal getNonDepreciatedTotalCost() {
        return BigDecimal.ZERO
                .add((this.nonDepreciatedDirectCost == null ? BigDecimal.ZERO : this.nonDepreciatedDirectCost))
                .add((this.nonDepreciatedServiceToolsCost == null ? BigDecimal.ZERO : this.nonDepreciatedServiceToolsCost));
    }

	@Override
	public String toString() {
		return "SIDataWarehouseLineItem [customerName=" + customerName + ", jobNumber=" + jobNumber + ", sowName="
				+ sowName + ", ospServiceName=" + ospServiceName + ", lineItemId=" + lineItemId
				+ ", lineItemDescription=" + lineItemDescription + ", lineItemPartNumber=" + lineItemPartNumber
				+ ", ciName=" + ciName + ", osmSysId=" + osmSysId + ", unitCount=" + unitCount + ", onetimeRevenue="
				+ onetimeRevenue + ", recurringRevenue=" + recurringRevenue + ", startDate=" + startDate + ", endDate="
				+ endDate + ", status=" + status + ", unitCost=" + unitCost
                                + ", directCost=" + directCost + ", serviceToolsCost=" + serviceToolsCost + ", totalCost=" + getTotalCost()
                                + ", nonDepreciatedDirectCost=" + nonDepreciatedDirectCost + ", nonDepreciatedServiceToolsCost=" + nonDepreciatedServiceToolsCost + ", nonDepreciatedTotalCost=" + getNonDepreciatedTotalCost()
                                + ", relatedLineItems=" + relatedLineItems
				+ "]";
	}
	
}
