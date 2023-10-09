package com.logicalis.serviceinsight.data;

import java.util.List;

public class Personnel {

	public enum Type {
		sdm("Service Delivery Manager"), ae("Account Executive"), epe("Enterprise Program Executive"), bsc("Business Solution Consultant");
		
		private String description;
		
		Type(String description) {
			this.description = description;
		}
		
		public String getDescription() {
			return description;
		}
		
	}
	
	private Long id;
	private Long customerId;
	private Long contractId;
	private Long userId;
	private String userName;
	private Type type;
	
	public Personnel() {}
	
	public Personnel(Long id, Long customerId, Long contractId, Long userId, String userName, Type type) {
		super();
		this.id = id;
		this.customerId = customerId;
		this.contractId = contractId;
		this.userId = userId;
		this.userName = userName;
		this.type = type;
	}
	
	public Personnel(Long userId, String userName, Type type) {
		super();
		this.userId = userId;
		this.userName = userName;
		this.type = type;
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
	
	public Long getContractId() {
		return contractId;
	}
	
	public void setContractId(Long contractId) {
		this.contractId = contractId;
	}
	
	public Long getUserId() {
		return userId;
	}
	
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	
	public String getUserName() {
		return userName;
	}
	
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public Type getType() {
		return type;
	}
	
	public void setType(Type type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "Personnel [id=" + id + ", customerId=" + customerId + ", contractId=" + contractId + ", userId="
				+ userId + ", userName=" + userName + ", type=" + type + "]";
	}
	
	/**
	 * Utility method to convert a List<Personnel> to a comma delimited "username" string
	 * @param persons
	 * @return
	 */
	public static String convertListToUsernamesString(List<Personnel> persons) {
		StringBuilder sbld = new StringBuilder();
		boolean started = false;
		for (Personnel person: persons) {
			if (started) {
				sbld.append(", ");
			}
			sbld.append(person.getUserName());
			started = true;
		}
		return sbld.toString();
	}
	
}
