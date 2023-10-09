package com.logicalis.serviceinsight.representation;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

public class LaborHoursRecord {

	private Long id;
	private Long ospId;
	private String serviceName;
	private Long customerId;
	private String customerName;
	private String worker;
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="MMddyyyy", timezone="America/New_York")
	private Date workDate;
	private BigDecimal hours;
	private String taskDescription;
	private String ticket;
	private String tierName;
	private String tierCode;
	
	public LaborHoursRecord(){}
	
	public LaborHoursRecord(Long id, Long ospId, String serviceName, Long customerId, String customerName, String worker, Date workDate, 
			BigDecimal hours, String taskDescription, String ticket, String tierName, String tierCode) {
		super();
		this.id = id;
		this.ospId = ospId;
		this.serviceName = serviceName;
		this.customerId = customerId;
		this.customerName = customerName;
		this.worker = worker;
		this.workDate = workDate;
		this.hours = hours;
		this.taskDescription = taskDescription;
		this.ticket = ticket;
		this.tierName = tierName;
		this.tierCode = tierCode;
	}

	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public Long getOspId() {
		return ospId;
	}
	
	public void setOspId(Long ospId) {
		this.ospId = ospId;
	}
	
	public String getServiceName() {
		return serviceName;
	}
	
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
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
	
	public String getWorker() {
		return worker;
	}
	
	public void setWorker(String worker) {
		this.worker = worker;
	}
	
	public Date getWorkDate() {
		return workDate;
	}
	
	public void setWorkDate(Date workDate) {
		this.workDate = workDate;
	}
	
	public BigDecimal getHours() {
		return hours;
	}
	
	public void setHours(BigDecimal hours) {
		this.hours = hours;
	}

	public String getTaskDescription() {
		return taskDescription;
	}

	public void setTaskDescription(String taskDescription) {
		this.taskDescription = taskDescription;
	}

	public String getTicket() {
		return ticket;
	}

	public void setTicket(String ticket) {
		this.ticket = ticket;
	}

	public String getTierName() {
		return tierName;
	}

	public void setTierName(String tierName) {
		this.tierName = tierName;
	}

	public String getTierCode() {
		return tierCode;
	}

	public void setTierCode(String tierCode) {
		this.tierCode = tierCode;
	}
	
}
