package com.logicalis.serviceinsight.util.servicenow;

import com.logicalis.serviceinsight.data.SNRecord;

public class SNCI {

	String sysId;
	String name;
	String status;

	public SNCI(){}

	public SNCI(SNRecord snRecord) {
		this.sysId = snRecord.getSysId();
		this.name = snRecord.getAttribute("name");
		this.status = snRecord.getAttribute("install_status");
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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
}
