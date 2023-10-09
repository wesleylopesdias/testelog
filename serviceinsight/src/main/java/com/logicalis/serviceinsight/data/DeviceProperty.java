package com.logicalis.serviceinsight.data;

import java.io.Serializable;

public class DeviceProperty implements Serializable {

	public enum Type {
		os("OS", DataType.string), compute("Compute", DataType.integer), memory("Memory", DataType.integer), storage("Storage", DataType.integer);
		
		private String description;
		private DataType dataType;
		
		public enum DataType {
			string, integer;
		};
		
		Type(String description, DataType dataType) {
			this.description = description;
			this.dataType = dataType;
		}

		public String getDescription() {
			return description;
		}
		
		public DataType getDataType() {
			return dataType;
		}
	}
	
	private Long id;
	private Long deviceId;
	private Type type;
	private Integer unitCount;
	private String strValue;
	private String unitType;
	
	public DeviceProperty() {}
	
	public DeviceProperty(Long id, Long deviceId, Type type, Integer unitCount, String strValue, String unitType) {
		this.id = id;
		this.deviceId = deviceId;
		this.type = type;
		this.unitCount = unitCount;
		this.strValue = strValue;
		this.unitType = unitType;
	}

	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public Long getDeviceId() {
		return deviceId;
	}
	
	public void setDeviceId(Long deviceId) {
		this.deviceId = deviceId;
	}
	
	public Type getType() {
		return type;
	}
	
	public void setType(Type type) {
		this.type = type;
	}
	
	public Integer getUnitCount() {
		return unitCount;
	}
	
	public void setUnitCount(Integer unitCount) {
		this.unitCount = unitCount;
	}
	
	public String getStrValue() {
		return strValue;
	}
	
	public void setStrValue(String strValue) {
		this.strValue = strValue;
	}
	
	public String getUnitType() {
		return unitType;
	}
	
	public void setUnitType(String unitType) {
		this.unitType = unitType;
	}
	
}
