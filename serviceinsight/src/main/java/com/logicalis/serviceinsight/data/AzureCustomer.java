package com.logicalis.serviceinsight.data;

public class AzureCustomer {

	private Long id;
	private Long customerId;
	private String name;
	private String azureTenantId;
	private Boolean active = Boolean.TRUE;
	
	public AzureCustomer() {}
	
	public AzureCustomer(Long id, Long customerId, String name, String azureTenantId, Boolean active) {
		super();
		this.id = id;
		this.customerId = customerId;
		this.name = name;
		this.azureTenantId = azureTenantId;
		this.active = active;
	}

	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public Long getCustomerId() {
		return customerId;
	}
	
	public void setCustomerId(Long customerId) {
		this.customerId = customerId;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getAzureTenantId() {
		return azureTenantId;
	}
	
	public void setAzureTenantId(String azureTenantId) {
		this.azureTenantId = azureTenantId;
	}
	
	public Boolean getActive() {
		return active;
	}
	
	public void setActive(Boolean active) {
		this.active = active;
	}

	@Override
	public String toString() {
		return "AzureCustomer [id=" + id + ", customerId=" + customerId + ", name=" + name + ", azureTenantId="
				+ azureTenantId + ", active=" + active + "]";
	}
	
}
