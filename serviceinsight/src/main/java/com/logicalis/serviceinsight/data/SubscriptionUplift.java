package com.logicalis.serviceinsight.data;

import java.math.BigDecimal;
import java.util.Date;

public class SubscriptionUplift {

	public enum UpliftType {
		percentage("percentage"), flat("flat");
		
		private String description;

		UpliftType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return this.description;
        }
	}
	
	private Long id;
	private String code;
	private String description;
	private BigDecimal uplift;
	private UpliftType upliftType;
	private Boolean active;
	private Date startDate;
	private Date endDate;
	private Date created;
    private String createdBy;
    private Date updated;
    private String updatedBy;
	
    public SubscriptionUplift() {}
    
    public SubscriptionUplift(Long id, String code, String description, BigDecimal uplift, UpliftType upliftType, Boolean active, Date startDate, Date endDate) {
		super();
		this.id = id;
		this.code = code;
		this.description = description;
		this.uplift = uplift;
		this.upliftType = upliftType;
		this.active = active;
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getCode() {
		return code;
	}
	
	public void setCode(String code) {
		this.code = code;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public BigDecimal getUplift() {
		return uplift;
	}
	
	public void setUplift(BigDecimal uplift) {
		this.uplift = uplift;
	}
	
	public UpliftType getUpliftType() {
		return upliftType;
	}
	
	public void setUpliftType(UpliftType upliftType) {
		this.upliftType = upliftType;
	}
	
	public Boolean getActive() {
		return active;
	}
	
	public void setActive(Boolean active) {
		this.active = active;
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
		return "SubscriptionUplift [id=" + id + ", code=" + code + ", description="
				+ description + ", uplift=" + uplift + ", upliftType="
				+ upliftType + ", active=" + active + ", startDate="
				+ startDate + ", endDate=" + endDate + ", created=" + created
				+ ", createdBy=" + createdBy + ", updated=" + updated
				+ ", updatedBy=" + updatedBy + "]";
	}
	
}
