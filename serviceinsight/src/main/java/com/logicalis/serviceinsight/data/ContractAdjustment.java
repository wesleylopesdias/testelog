package com.logicalis.serviceinsight.data;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.joda.time.DateTime;

/**
 * Encapsulates the concept of a financial adjustment, such as a credit or fee
 * on a Contract, possibly in relation to a Contract Update / PCR
 * 
 * @author poneil
 */
public class ContractAdjustment {
	
    public enum AdjustmentType {
        onetime, recurring
    }
    
    private Long id;
    private Long contractId;
    private Long contractUpdateId;
    private Long contractGroupId;
    private BigDecimal adjustment;
    private String adjustmentType;
    private String note;
    private DateTime month;
    Integer days;
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="MMddyyyy", timezone="America/New_York")
    private Date startDate;
    private DateTime startDateTime;
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="MMddyyyy", timezone="America/New_York")
    private Date endDate;
    private DateTime endDateTime;
    private String operation;
    private Date created;
    private String createdBy;
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="MMddyyyy", timezone="America/New_York")
    private Date updated;
    private String updatedBy;
    private Service.Status status;
    private List<ContractUpdate> contractUpdates = new ArrayList<ContractUpdate>();
    
    /**
     * default CTOR
     */
    public ContractAdjustment() {
    }

    public ContractAdjustment(Long id, Long contractId, Long contractUpdateId, Long contractGroupId, BigDecimal adjustment,
            String adjustmentType, String note, DateTime month, Date startDate, Date endDate, Service.Status status,
            Date created, String createdBy, Date updated, String updatedBy) {
        this.id = id;
        this.contractId = contractId;
        this.contractUpdateId = contractUpdateId;
        this.contractGroupId = contractGroupId;
        this.adjustment = adjustment;
        this.adjustmentType = AdjustmentType.valueOf(adjustmentType).name();
        this.note = note;
        this.month = month;
        this.startDate = startDate;
        if (startDate != null) {
            this.startDateTime = new DateTime(startDate);
        }
        this.endDate = endDate;
        if (endDate != null) {
            this.endDateTime = new DateTime(endDate);
        }
        this.status = status;
        this.created = created;
        this.createdBy = createdBy;
        this.updated = updated;
        this.updatedBy = updatedBy;
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

    public Long getContractUpdateId() {
        return contractUpdateId;
    }

    public void setContractUpdateId(Long contractUpdateId) {
        this.contractUpdateId = contractUpdateId;
    }

    public Long getContractGroupId() {
		return contractGroupId;
	}

	public void setContractGroupId(Long contractGroupId) {
		this.contractGroupId = contractGroupId;
	}

	public BigDecimal getAdjustment() {
        return adjustment;
    }
    
    public BigDecimal getAdjustmentApplied() {
        if (month != null && startDateTime != null) {
            if (startDateTime.getYear() == month.getYear() &&
                    startDateTime.getMonthOfYear() == month.getMonthOfYear()) {
                return adjustment;
            }
            return new BigDecimal(0); // adjustment only applies in it's applied Date
        }
        return adjustment;
    }
    
    public BigDecimal getFormattedAdjustment() {
        if (adjustment != null) {
            return adjustment.setScale(2, RoundingMode.HALF_UP);
        }
        return new BigDecimal(0).setScale(2, RoundingMode.HALF_UP);
    }
    
    public BigDecimal getFormattedAdjustmentApplied() {
        if (adjustment != null) {
            if (month != null && startDateTime != null) {
                if (startDateTime.getYear() == month.getYear() &&
                        startDateTime.getMonthOfYear() == month.getMonthOfYear()) {
                    return adjustment.setScale(2, RoundingMode.HALF_UP);
                }
                return new BigDecimal(0); // adjustment only applies in it's applied Date
            }
            return adjustment.setScale(2, RoundingMode.HALF_UP);
        }
        return new BigDecimal(0).setScale(2, RoundingMode.HALF_UP); // adjustment only applies in the first month
    }

    public void forMonth(DateTime forMonth) {
        this.month = forMonth;
        if (this.month != null) {
            this.days = this.month.dayOfMonth().getMaximumValue();
        }
    }
    
    public void setAdjustment(BigDecimal adjustment) {
        this.adjustment = adjustment;
    }

    public String getAdjustmentType() {
        return adjustmentType;
    }

    public void setAdjustmentType(String adjustmentType) {
        this.adjustmentType = AdjustmentType.valueOf(adjustmentType).name();
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
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

	public Service.Status getStatus() {
		return status;
	}

	public void setStatus(Service.Status status) {
		this.status = status;
	}

	public List<ContractUpdate> getContractUpdates() {
        return contractUpdates;
    }

    public void setContractUpdates(List<ContractUpdate> contractUpdates) {
        this.contractUpdates = contractUpdates;
    }
    
    public void addContractUpdate(ContractUpdate contractUpdate) {
        this.contractUpdates.add(contractUpdate);
    }

    public boolean equalsForRollup(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ContractAdjustment other = (ContractAdjustment) obj;
        if (!Objects.equals(this.contractId, other.contractId)) {
            return false;
        }
        if (!Objects.equals(this.adjustmentType, other.adjustmentType)) {
            return false;
        }
        if (!Objects.equals(this.startDate, other.startDate)) {
            return false;
        }
        if (!Objects.equals(this.endDate, other.endDate)) {
            return false;
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ContractAdjustment other = (ContractAdjustment) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ContractAdjustment{" + "id=" + id + ", contractId=" + contractId + ", contractUpdateId=" + contractUpdateId + ", adjustment=" + adjustment + ", adjustmentType=" + adjustmentType + ", note=" + note + ", month=" + month + ", startDate=" + startDate + ", startDateTime=" + startDateTime + ", endDate=" + endDate + ", endDateTime=" + endDateTime + ", operation=" + operation + ", created=" + created + ", createdBy=" + createdBy + ", updated=" + updated + ", updatedBy=" + updatedBy + ", contractUpdates=" + contractUpdates + '}';
    }
}
