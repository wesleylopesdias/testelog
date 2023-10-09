package com.logicalis.serviceinsight.data;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

public class ContractInvoice {

	public enum Status {
        active("Active"), readyToInvoice("Ready To Invoice"), invoiced("Invoiced");
        private String description;

        Status(String description) {
            this.description = description;
        }

        public String getDescription() {
            return this.description;
        }
    }
	
	private Long id;
	private Long contractId;
	private Status status;
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="MMddyyyy", timezone="America/New_York")
    private Date startDate;
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="MMddyyyy", timezone="America/New_York")
    private Date endDate;
    private String operation;
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="MMddyyyy", timezone="America/New_York")
    private Date created;
    private String createdBy;
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="MMddyyyy", timezone="America/New_York")
    private Date updated;
    private String updatedBy;
    
    
    public ContractInvoice() {}
    
	public ContractInvoice(Long id, Long contractId, Status status, Date startDate, Date endDate) {
		this.id = id;
		this.contractId = contractId;
		this.status = status;
		this.startDate = startDate;
		this.endDate = endDate;
	}
	

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getContractId() {
		return contractId;
	}

	public void setContractId(Long contractId) {
		this.contractId = contractId;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
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
	
	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Date getUpdated() {
		return updated;
	}

	public void setUpdated(Date updated) {
		this.updated = updated;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	@Override
	public String toString() {
		return "ContractInvoice [id=" + id + ", contractId=" + contractId
				+ ", status=" + status + ", startDate=" + startDate
				+ ", endDate=" + endDate + "]";
	}
    
}
