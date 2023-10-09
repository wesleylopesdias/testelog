package com.logicalis.serviceinsight.data;

public class ScheduledTask {

	Long id;
	String name;
	String code;
	String description;
	Boolean enabled;
	
	public ScheduledTask(){}

	public ScheduledTask(Long id, String name, String code, String description, Boolean enabled) {
		super();
		this.id = id;
		this.name = name;
		this.code = code;
		this.description = description;
		this.enabled = enabled;
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

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}
	
}
