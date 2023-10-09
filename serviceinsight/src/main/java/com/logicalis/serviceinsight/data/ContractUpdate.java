package com.logicalis.serviceinsight.data;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import java.math.BigDecimal;

/**
 * Represents a trackable change to a contract, a PCR concept
 * 
 * @author poneil
 */
public class ContractUpdate implements Comparable<ContractUpdate> {
    private Long id;
    private String altId;
    private String jobNumber;
    private String ticketNumber;
    private String note;
    private Long contractId;
    private BigDecimal onetimePrice;
    private BigDecimal recurringPrice;
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="MMddyyyy", timezone="America/New_York")
    private Date signedDate;
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="MMddyyyy", timezone="America/New_York")
    private Date effectiveDate;
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="MMddyyyy", timezone="America/New_York")
    private Date updated;
    private String updatedBy;
    private String filePath;
    
    /**
     * default CTOR
     */
    public ContractUpdate() {
        
    }

    public ContractUpdate(Long id, Long contractId, String altId, String jobNo, String ticketNo, String note, Date signedDate, Date effectiveDate,  
    		BigDecimal onetimePrice, BigDecimal recurringPrice, Date updated, String updatedBy, String filePath) {
        this.id = id;
        this.contractId = contractId;
        this.altId = altId;
        this.jobNumber = jobNo;
        this.ticketNumber = ticketNo;
        this.onetimePrice = onetimePrice;
        this.recurringPrice = recurringPrice;
        this.note = note;
        this.signedDate = signedDate;
        this.effectiveDate = effectiveDate;
        this.updated = updated;
        this.updatedBy = updatedBy;
        this.filePath = filePath;
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAltId() {
        return altId;
    }

    public void setAltId(String altId) {
        this.altId = altId;
    }

    public String getJobNumber() {
        return jobNumber;
    }

    public void setJobNumber(String jobNumber) {
        this.jobNumber = jobNumber;
    }
    
    public String getTicketNumber() {
		return ticketNumber;
	}

	public void setTicketNumber(String ticketNumber) {
		this.ticketNumber = ticketNumber;
	}

	public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Long getContractId() {
        return contractId;
    }

    public void setContractId(Long contractId) {
        this.contractId = contractId;
    }

	public BigDecimal getOnetimePrice() {
		return onetimePrice;
	}

	public void setOnetimePrice(BigDecimal onetimePrice) {
		this.onetimePrice = onetimePrice;
	}

	public BigDecimal getRecurringPrice() {
		return recurringPrice;
	}

	public void setRecurringPrice(BigDecimal recurringPrice) {
		this.recurringPrice = recurringPrice;
	}
	
	public BigDecimal getTotalPrice() {
		BigDecimal totalPrice = new BigDecimal(0);
		
		if(getOnetimePrice() != null) {
			totalPrice = totalPrice.add(getOnetimePrice());
		}
		
		if(getRecurringPrice() != null) {
			totalPrice = totalPrice.add(getRecurringPrice());
		}
		
		return totalPrice;
	}

	public Date getSignedDate() {
        return signedDate;
    }

    public void setSignedDate(Date signedDate) {
        this.signedDate = signedDate;
    }
    
    public Date getEffectiveDate() {
		return effectiveDate;
	}

	public void setEffectiveDate(Date effectiveDate) {
		this.effectiveDate = effectiveDate;
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

    public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	@Override
    public int compareTo(ContractUpdate o) {
        if (this.id != null && o.getId() == null) {
            return 1;
        } else if (this.getId() == null && o.getId() != null) {
            return -1;
        } else if (this.id != null && o.getId() != null) {
            return this.id.compareTo(o.getId());
        }
        return 0;
    }

    @Override
	public String toString() {
		return "ContractUpdate [id=" + id + ", altId=" + altId + ", jobNumber=" + jobNumber + ", ticketNumber="
				+ ticketNumber + ", note=" + note + ", contractId=" + contractId + ", onetimePrice=" + onetimePrice
				+ ", recurringPrice=" + recurringPrice + ", signedDate=" + signedDate + ", effectiveDate="
				+ effectiveDate + ", updated=" + updated + ", updatedBy=" + updatedBy + ", filePath=" + filePath + "]";
	}
}
