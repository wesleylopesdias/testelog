package com.logicalis.serviceinsight.remote;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "customerId",
    "name",
    "altId",
    "signedDate",
    "serviceStartDate",
    "startDate",
    "endDate",
    "jobNumber",
    "engagementManager",
    "accountExecutive",
    "archived",
    "serviceNowSysId"
})
public class ContractRequestContract {

    @JsonProperty("id")
    private Long id;
    @JsonProperty("customerId")
    private Long customerId;
    @JsonProperty("name")
    private String name;
    @JsonProperty("altId")
    private String altId;
    @JsonProperty("signedDate")
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="MMddyyyy", timezone="America/New_York")
    private Date signedDate;
    @JsonProperty("serviceStartDate")
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="MMddyyyy", timezone="America/New_York")
    private Date serviceStartDate;
    @JsonProperty("startDate")
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="MMddyyyy", timezone="America/New_York")
    private Date startDate;
    @JsonProperty("endDate")
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="MMddyyyy", timezone="America/New_York")
    private Date endDate;
    @JsonProperty("jobNumber")
    private String jobNumber;
    @JsonProperty("engagementManager")
    private String engagementManager;
    @JsonProperty("accountExecutive")
    private String accountExecutive;
    @JsonProperty("archived")
    private Boolean archived;
    @JsonProperty("serviceNowSysId")
    private String serviceNowSysId;

    public ContractRequestContract() {
		super();
	}

    @JsonProperty("id")
    public Long getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(Long id) {
        this.id = id;
    }

	@JsonProperty("customerId")
    public Long getCustomerId() {
        return customerId;
    }

    @JsonProperty("customerId")
    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("altId")
    public String getAltId() {
        return altId;
    }

    @JsonProperty("altId")
    public void setAltId(String altId) {
        this.altId = altId;
    }

    @JsonProperty("signedDate")
    public Date getSignedDate() {
        return signedDate;
    }

    @JsonProperty("signedDate")
    public void setSignedDate(Date signedDate) {
        this.signedDate = signedDate;
    }

    @JsonProperty("serviceStartDate")
    public Date getServiceStartDate() {
        return serviceStartDate;
    }

    @JsonProperty("serviceStartDate")
    public void setServiceStartDate(Date serviceStartDate) {
        this.serviceStartDate = serviceStartDate;
    }

    @JsonProperty("startDate")
    public Date getStartDate() {
        return startDate;
    }

    @JsonProperty("startDate")
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    @JsonProperty("endDate")
    public Date getEndDate() {
        return endDate;
    }

    @JsonProperty("endDate")
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    @JsonProperty("jobNumber")
    public String getJobNumber() {
        return jobNumber;
    }

    @JsonProperty("jobNumber")
    public void setJobNumber(String jobNumber) {
        this.jobNumber = jobNumber;
    }

    @JsonProperty("engagementManager")
    public String getEngagementManager() {
        return engagementManager;
    }

    @JsonProperty("engagementManager")
    public void setEngagementManager(String engagementManager) {
        this.engagementManager = engagementManager;
    }

    @JsonProperty("accountExecutive")
    public String getAccountExecutive() {
        return accountExecutive;
    }

    @JsonProperty("accountExecutive")
    public void setAccountExecutive(String accountExecutive) {
        this.accountExecutive = accountExecutive;
    }

    @JsonProperty("archived")
    public Boolean getArchived() {
        return archived;
    }

    @JsonProperty("archived")
    public void setArchived(Boolean archived) {
        this.archived = archived;
    }

    @JsonProperty("serviceNowSysId")
    public String getServiceNowSysId() {
        return serviceNowSysId;
    }

    @JsonProperty("serviceNowSysId")
    public void setServiceNowSysId(String serviceNowSysId) {
        this.serviceNowSysId = serviceNowSysId;
    }

	@Override
	public String toString() {
		return "ContractRequestContract [id=" + id + ", customerId=" + customerId + ", name=" + name + ", altId="
				+ altId + ", signedDate=" + signedDate + ", serviceStartDate=" + serviceStartDate + ", startDate="
				+ startDate + ", endDate=" + endDate + ", jobNumber=" + jobNumber + ", engagementManager="
				+ engagementManager + ", accountExecutive=" + accountExecutive + ", archived=" + archived
				+ ", serviceNowSysId=" + serviceNowSysId + "]";
	}

}
