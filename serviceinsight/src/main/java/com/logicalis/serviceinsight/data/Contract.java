package com.logicalis.serviceinsight.data;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.logicalis.serviceinsight.remote.ContractRequestContract;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

/**
 *
 * @author poneil
 */
public class Contract {
	
	public enum RenewalStatus {
		renewed("Renewed"), renewing("Expected to Renew"), likelyRenew("Likely to Renew"), unlikelyRenew("Unlikely to Renew"), notRenewing("Not Expected to Renew");
		
		private String description;
		
		RenewalStatus(String description) {
			this.description = description;
		}
		
		public String getDescription() {
			return description;
		}
		
	}
	
    protected Long customerId;
    protected String customerName;
    protected Long id;
    protected String altId;
    protected String jobNumber;
    protected String name;
    protected String engagementManager;
    protected String accountExec;
    protected Integer serviceCount;
    protected Boolean archived;
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="MMddyyyy", timezone="America/New_York")
    protected Date signedDate; //this is really just informational
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="MMddyyyy", timezone="America/New_York")
    protected Date serviceStartDate; //this is really just informational
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="MMddyyyy", timezone="America/New_York")
    protected Date startDate; //this is known as Billing Start Date in the UI
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="MMddyyyy", timezone="America/New_York")
    protected Date endDate; //this is known as Service End Date in the UI?
    private String serviceNowSysId;
    private Long quoteId; //quote id from pricing, in the event this was created from pricing import
    protected String filePath;
    protected RenewalStatus renewalStatus;
    protected String renewalStatusDisplay;
    protected BigDecimal renewalChange; //can be negative to represent a decrease
    protected String renewalNotes;
    protected Personnel accountExecutive;
    protected Personnel enterpriseProgramExecutive;
    protected List<Personnel> serviceDeliveryManagers = new ArrayList<Personnel>();
    protected List<Personnel> businessSolutionsConsultants = new ArrayList<Personnel>();
    protected BigDecimal monthTotalOnetimeRevenue;
    protected BigDecimal monthTotalRecurringRevenue;
    
    public Contract(){}
    
    public Contract(Long customerId, Long id, String altId, String jobNo, String name, String emgr, String accountExec, Integer serviceCount, Date signed, 
    		Date ssd, Date sd, Date ed, Boolean archived, String serviceNowSysId, String filePath, RenewalStatus renewalStatus, BigDecimal renewalChange, String renewalNotes) {
        this.customerId = customerId;
        this.id = id;
        this.altId = altId;
        this.jobNumber = jobNo;
        this.name = name;
        this.engagementManager = emgr;
        this.accountExec = accountExec;
        this.serviceCount = serviceCount;
        this.signedDate = signed;
        this.serviceStartDate = ssd;
        this.startDate = sd;
        this.endDate = ed;
        this.archived = archived;
        this.serviceNowSysId = serviceNowSysId;
        this.filePath = filePath;
        this.renewalStatus = renewalStatus;
        this.renewalChange = renewalChange;
        this.renewalNotes = renewalNotes;
    }
    
    public Contract(String customerName, Long customerId, Long id, String altId, String jobNo, String name, String emgr, String accountExec, Integer serviceCount, Date signed, 
    		Date ssd, Date sd, Date ed, Boolean archived, String serviceNowSysId, String filePath, RenewalStatus renewalStatus, BigDecimal renewalChange, String renewalNotes) {
        this.customerName = customerName;
    	this.customerId = customerId;
        this.id = id;
        this.altId = altId;
        this.jobNumber = jobNo;
        this.name = name;
        this.engagementManager = emgr;
        this.accountExec = accountExec;
        this.serviceCount = serviceCount;
        this.signedDate = signed;
        this.serviceStartDate = ssd;
        this.startDate = sd;
        this.endDate = ed;
        this.archived = archived;
        this.serviceNowSysId = serviceNowSysId;
        this.filePath = filePath;
        this.renewalStatus = renewalStatus;
        this.renewalChange = renewalChange;
        this.renewalNotes = renewalNotes;
    }
    
    public Contract(Long id, Long customerId, String name, String altId, Date signedDate, Date serviceStartDate, Date startDate, Date endDate, String jobNumber, boolean archived) {
        this.id = id;
        this.customerId = customerId;
        this.name = name;
        this.altId = altId;
        this.signedDate = signedDate;
        this.serviceStartDate = serviceStartDate;
        this.startDate = startDate;
        this.endDate = endDate;
        this.jobNumber = jobNumber;
        this.archived = archived;
    }
    
    public static Contract fromContractRequestContract(ContractRequestContract from) {
        return new Contract(
                from.getId(),
                from.getCustomerId(),
                from.getName(),
                from.getAltId(),
                from.getSignedDate(),
                from.getServiceStartDate(),
                from.getStartDate(),
                from.getEndDate(),
                from.getJobNumber(),
                from.getArchived());
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getServiceCount() {
        return serviceCount;
    }

    public void setServiceCount(Integer serviceCount) {
        this.serviceCount = serviceCount;
    }
    
    public Boolean getArchived() {
		return archived;
	}

	public void setArchived(Boolean archived) {
		this.archived = archived;
	}

	public Date getSignedDate() {
		return signedDate;
	}

	public void setSignedDate(Date signedDate) {
		this.signedDate = signedDate;
	}

	public Date getServiceStartDate() {
		return serviceStartDate;
	}

	public void setServiceStartDate(Date serviceStartDate) {
		this.serviceStartDate = serviceStartDate;
	}

	public Date getStartDate() {
        return startDate;
    }
    
    public String getFormattedStartDate() {
        if (startDate != null) {
            return DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss z").print(new DateTime(startDate));
        }
        return null;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }
    
    public String getFormattedEndDate() {
        if (endDate != null) {
            return DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss z").print(new DateTime(endDate));
        }
        return null;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public String getEngagementManager() {
        return engagementManager;
    }

    public void setEngagementManager(String engagementManager) {
        this.engagementManager = engagementManager;
    }
    
    public String getAccountExec() {
		return accountExec;
	}

	public void setAccountExec(String accountExec) {
		this.accountExec = accountExec;
	}

	public String getServiceNowSysId() {
		return serviceNowSysId;
	}

	public void setServiceNowSysId(String serviceNowSysId) {
		this.serviceNowSysId = serviceNowSysId;
	}

	public Long getQuoteId() {
		return quoteId;
	}

	public void setQuoteId(Long quoteId) {
		this.quoteId = quoteId;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public RenewalStatus getRenewalStatus() {
		return renewalStatus;
	}

	public void setRenewalStatus(RenewalStatus renewalStatus) {
		this.renewalStatus = renewalStatus;
	}

	public BigDecimal getRenewalChange() {
		return renewalChange;
	}

	public void setRenewalChange(BigDecimal renewalChange) {
		this.renewalChange = renewalChange;
	}

	public String getRenewalStatusDisplay() {
		if(getRenewalStatus() != null) {
			renewalStatusDisplay = getRenewalStatus().getDescription();
		}
		return renewalStatusDisplay;
	}

	public String getRenewalNotes() {
		return renewalNotes;
	}

	public void setRenewalNotes(String renewalNotes) {
		this.renewalNotes = renewalNotes;
	}

	public Personnel getAccountExecutive() {
		return accountExecutive;
	}

	public void setAccountExecutive(Personnel accountExecutive) {
		this.accountExecutive = accountExecutive;
	}

	public Personnel getEnterpriseProgramExecutive() {
		return enterpriseProgramExecutive;
	}

	public void setEnterpriseProgramExecutive(Personnel enterpriseProgramExecutive) {
		this.enterpriseProgramExecutive = enterpriseProgramExecutive;
	}

	public List<Personnel> getServiceDeliveryManagers() {
		return serviceDeliveryManagers;
	}

	public void setServiceDeliveryManagers(List<Personnel> serviceDeliveryManagers) {
		this.serviceDeliveryManagers = serviceDeliveryManagers;
	}

	public List<Personnel> getBusinessSolutionsConsultants() {
		return businessSolutionsConsultants;
	}

	public void setBusinessSolutionsConsultants(List<Personnel> businessSolutionsConsultants) {
		this.businessSolutionsConsultants = businessSolutionsConsultants;
	}

	public BigDecimal getMonthTotalOnetimeRevenue() {
		return monthTotalOnetimeRevenue;
	}

	public void setMonthTotalOnetimeRevenue(BigDecimal monthTotalOnetimeRevenue) {
		this.monthTotalOnetimeRevenue = monthTotalOnetimeRevenue;
	}

	public BigDecimal getMonthTotalRecurringRevenue() {
		return monthTotalRecurringRevenue;
	}

	public void setMonthTotalRecurringRevenue(BigDecimal monthTotalRecurringRevenue) {
		this.monthTotalRecurringRevenue = monthTotalRecurringRevenue;
	}

	@Override
	public String toString() {
		return "Contract [customerId=" + customerId + ", customerName=" + customerName + ", id=" + id + ", altId="
				+ altId + ", jobNumber=" + jobNumber + ", name=" + name + ", engagementManager=" + engagementManager
				+ ", accountExec=" + accountExec + ", serviceCount=" + serviceCount + ", archived=" + archived
				+ ", signedDate=" + signedDate + ", serviceStartDate=" + serviceStartDate + ", startDate=" + startDate
				+ ", endDate=" + endDate + ", serviceNowSysId=" + serviceNowSysId + ", quoteId=" + quoteId
				+ ", filePath=" + filePath + ", renewalStatus=" + renewalStatus + ", renewalStatusDisplay="
				+ renewalStatusDisplay + ", renewalChange=" + renewalChange + ", accountExecutive=" + accountExecutive
				+ ", enterpriseProgramExecutive=" + enterpriseProgramExecutive + ", serviceDeliveryManagers="
				+ serviceDeliveryManagers + ", businessSolutionsConsultants=" + businessSolutionsConsultants
				+ ", monthTotalOnetimeRevenue=" + monthTotalOnetimeRevenue + ", monthTotalRecurringRevenue="
				+ monthTotalRecurringRevenue + "]";
	}
	

}
