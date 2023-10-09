package com.logicalis.serviceinsight.data;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

public class DataWarehouseLineItem {

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy", timezone = "America/New_York")
	private Date month;
	private String customer;
	private String jobNumber;
	private String sowName;
	private String ospService;
	private Long lineItemId;
	private String lineItemDescription;
	private String lineItemPartNumber;
	private String ciName;
	private String osmSysId;
	private Integer unitCount;
	private BigDecimal nrc;
	private BigDecimal mrc;
        private BigDecimal directCost = BigDecimal.ZERO;
        private BigDecimal serviceToolsCost = BigDecimal.ZERO;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy", timezone = "America/New_York")
	private Date startDate;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy", timezone = "America/New_York")
	private Date endDate;
	private String status;
	
	public DataWarehouseLineItem() {}
	
	public DataWarehouseLineItem(Date month, String customer, String jobNumber, String sowName, String ospService,
			Long lineItemId, String lineItemDescription, String lineItemPartNumber, String ciName, String osmSysId,
			Integer unitCount, BigDecimal nrc, BigDecimal mrc, Date startDate, Date endDate, String status) {
		super();
		this.month = month;
		this.customer = customer;
		this.jobNumber = jobNumber;
		this.sowName = sowName;
		this.ospService = ospService;
		this.lineItemId = lineItemId;
		this.lineItemDescription = lineItemDescription;
		this.lineItemPartNumber = lineItemPartNumber;
		this.ciName = ciName;
		this.osmSysId = osmSysId;
		this.unitCount = unitCount;
		this.nrc = nrc;
		this.mrc = mrc;
		this.startDate = startDate;
		this.endDate = endDate;
		this.status = status;
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
	
	public String getOspService() {
		return ospService;
	}
	
	public void setOspService(String ospService) {
		this.ospService = ospService;
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
	
	public BigDecimal getNrc() {
		return nrc;
	}
	
	public void setNrc(BigDecimal nrc) {
		this.nrc = nrc;
	}
	
	public BigDecimal getMrc() {
		return mrc;
	}
	
	public void setMrc(BigDecimal mrc) {
		this.mrc = mrc;
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

	@Override
	public String toString() {
		return "DataWarehouseLineItem [month=" + month + ", customer=" + customer + ", jobNumber=" + jobNumber
				+ ", sowName=" + sowName + ", ospService=" + ospService + ", lineItemId=" + lineItemId
				+ ", lineItemDescription=" + lineItemDescription + ", lineItemPartNumber=" + lineItemPartNumber
				+ ", ciName=" + ciName + ", osmSysId=" + osmSysId + ", unitCount=" + unitCount + ", nrc=" + nrc
				+ ", mrc=" + mrc + ", startDate=" + startDate + ", endDate=" + endDate + ", status=" + status + "]";
	}
	
}
