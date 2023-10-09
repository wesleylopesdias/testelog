package com.logicalis.serviceinsight.util.servicenow;

import java.util.ArrayList;
import java.util.List;

import com.logicalis.serviceinsight.data.SNRecord;

public class SNContract {
	
	String sysId;
	String name;
	String jobNumber;
	List<SNContract> childContracts = new ArrayList<SNContract>();
	
	public SNContract(){}
	
	public SNContract(SNRecord snRecord) {
		this.sysId = snRecord.getSysId();
		this.jobNumber = snRecord.getAttribute("number");
		this.name = snRecord.getAttribute("short_description");
	}
	
	public String getSysId() {
		return sysId;
	}
	
	public void setSysId(String sysId) {
		this.sysId = sysId;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getJobNumber() {
		return jobNumber;
	}
	
	public void setJobNumber(String jobNumber) {
		this.jobNumber = jobNumber;
	}

	public List<SNContract> getChildContracts() {
		return childContracts;
	}

	public void setChildContracts(List<SNContract> childContracts) {
		this.childContracts = childContracts;
	}
	
}
