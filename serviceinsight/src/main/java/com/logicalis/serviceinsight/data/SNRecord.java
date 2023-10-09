package com.logicalis.serviceinsight.data;

import java.util.HashMap;

public class SNRecord {
	
	private String sysId;
	private String objType;
	private HashMap attributes;
	
	public String getSysId() {
		return sysId;
	}

	public String getObjType() {
		return objType;
	}
	
	public void setObjType(String objType) {
		this.objType = objType;
	}
	
	public HashMap getAttributes() {
		return attributes;
	}
	
	public String getAttribute(String attributeName) {
		String retVal = null;
		retVal = (String) attributes.get(attributeName);
		return retVal;
	}
	
	public void setAttributes(HashMap attributes) {
		this.attributes = attributes;
		this.sysId = (String) attributes.get("sys_id");
	}
	
}
