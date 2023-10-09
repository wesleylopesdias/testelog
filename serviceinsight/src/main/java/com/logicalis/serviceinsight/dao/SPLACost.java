package com.logicalis.serviceinsight.dao;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.logicalis.serviceinsight.data.Device;

public class SPLACost implements Comparable<SPLACost>, Serializable {

	public enum Vendor {
		microsoft("Microsoft"), vmware("VMWare"), citrix("Citrix"), other("Other");
		
		private String description;
		
		Vendor(String description) {
            this.description = description;
        }

        public String getDescription() {
            return this.description;
        }
	}
	
	public enum Type {
		single("Single License"), multi("Multi-Tenant");
		
		private String description;
		
		Type(String description) {
            this.description = description;
        }

        public String getDescription() {
            return this.description;
        }
	}
	
	private Long id;
	private String name;
	private String altId;
	private BigDecimal cost;
	private Vendor vendor;
	private Type type;
	private Integer expenseCategoryId;
	private Boolean active;
	private List<Device> devices = new ArrayList<Device>();
	
	public SPLACost() {}
	
	public SPLACost(Long id, String name, String altId, BigDecimal cost, Vendor vendor, Boolean active, Type type, Integer expenseCategoryId) {
		this.id = id;
		this.name = name;
		this.altId = altId;
		this.cost = cost;
		this.vendor = vendor;
		this.active = active;
		this.type = type;
		this.expenseCategoryId = expenseCategoryId;
	}

	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getAltId() {
		return altId;
	}
	
	public void setAltId(String altId) {
		this.altId = altId;
	}
	
	public BigDecimal getCost() {
		return cost;
	}

	public void setCost(BigDecimal cost) {
		this.cost = cost;
	}

	public Vendor getVendor() {
		return vendor;
	}

	public void setVendor(Vendor vendor) {
		this.vendor = vendor;
	}

	public Boolean getActive() {
		return active;
	}
	
	public void setActive(Boolean active) {
		this.active = active;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Integer getExpenseCategoryId() {
		return expenseCategoryId;
	}

	public void setExpenseCategoryId(Integer expenseCategoryId) {
		this.expenseCategoryId = expenseCategoryId;
	}

	public List<Device> getDevices() {
		return devices;
	}

	public void setDevices(List<Device> devices) {
		this.devices = devices;
	}
	
	@Override
	public String toString() {
		return "SPLACost [id=" + id + ", name=" + name + ", altId=" + altId + ", cost=" + cost + ", vendor=" + vendor
				+ ", type=" + type + ", expenseCategoryId=" + expenseCategoryId + ", active=" + active + ", devices="
				+ devices + "]";
	}

	@Override
	public int compareTo(SPLACost splaCost) {
		return this.name.compareTo(splaCost.name);
	}
	
}
