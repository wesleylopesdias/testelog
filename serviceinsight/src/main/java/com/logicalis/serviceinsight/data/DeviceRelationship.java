package com.logicalis.serviceinsight.data;

public class DeviceRelationship {

	private Long deviceId;
	private Long relatedDeviceId;
	private String relationship;
	private Integer specUnits;
	private Integer order;
	
	public DeviceRelationship() {}
	
	public DeviceRelationship(Long deviceId, Long relatedDeviceId, String relationship, Integer specUnits, Integer order) {
		super();
		this.deviceId = deviceId;
		this.relatedDeviceId = relatedDeviceId;
		this.relationship = relationship;
		this.specUnits = specUnits;
		this.order = order;
	}

	public Long getDeviceId() {
		return deviceId;
	}
	
	public void setDeviceId(Long deviceId) {
		this.deviceId = deviceId;
	}
	
	public Long getRelatedDeviceId() {
		return relatedDeviceId;
	}
	
	public void setRelatedDeviceId(Long relatedDeviceId) {
		this.relatedDeviceId = relatedDeviceId;
	}
	
	public String getRelationship() {
		return relationship;
	}
	
	public void setRelationship(String relationship) {
		this.relationship = relationship;
	}
	
	public Integer getSpecUnits() {
		return specUnits;
	}
	
	public void setSpecUnits(Integer specUnits) {
		this.specUnits = specUnits;
	}
	
	public Integer getOrder() {
		return order;
	}
	
	public void setOrder(Integer order) {
		this.order = order;
	}
	
}
