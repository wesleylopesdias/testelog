package com.logicalis.serviceinsight.util.servicenow;

import com.logicalis.serviceinsight.data.SNRecord;

public class SNContractCI {

	String sysId;
	String name;
	String ciItemSysId;

	public SNContractCI(){}

	public SNContractCI(SNRecord snRecord) {
		this.sysId = snRecord.getSysId();
		this.name = snRecord.getAttribute("name");
		this.ciItemSysId = snRecord.getAttribute("ci_item");
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

	public String getCiItemSysId() {
		return ciItemSysId;
	}

	public void setCiItemSysId(String ciItemSysId) {
		this.ciItemSysId = ciItemSysId;
	}

}
