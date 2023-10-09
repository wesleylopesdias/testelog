package com.logicalis.serviceinsight.data;

import java.util.ArrayList;
import java.util.List;

import com.logicalis.serviceinsight.dao.BaseDao;

public class PricingSheet extends BaseDao {

	private Long id;
	private Long customerId;
	private String customerSNSysId;
	private Long contractId;
	private String contractSNSysId;
	private String contractName;
	private String jobNumber;
	private Boolean active = Boolean.TRUE;
	private List<PricingSheetProduct> products = new ArrayList<PricingSheetProduct>();
	
	public PricingSheet() {}
	
	/*CTOR -- Used for General Pricing Sheet Retrieval in the App*/
	public PricingSheet(Long id, Long contractId, Boolean active) {
		this.id = id;
		this.contractId = contractId;
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
	
	public String getCustomerSNSysId() {
		return customerSNSysId;
	}
	
	public void setCustomerSNSysId(String customerSNSysId) {
		this.customerSNSysId = customerSNSysId;
	}
	
	public Long getContractId() {
		return contractId;
	}
	
	public void setContractId(Long contractId) {
		this.contractId = contractId;
	}
	
	public String getContractSNSysId() {
		return contractSNSysId;
	}
	
	public void setContractSNSysId(String contractSNSysId) {
		this.contractSNSysId = contractSNSysId;
	}
	
	public String getContractName() {
		return contractName;
	}
	
	public void setContractName(String contractName) {
		this.contractName = contractName;
	}
	
	public String getJobNumber() {
		return jobNumber;
	}
	
	public void setJobNumber(String jobNumber) {
		this.jobNumber = jobNumber;
	}
	
	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public List<PricingSheetProduct> getProducts() {
		return products;
	}
	
	public void setProducts(List<PricingSheetProduct> products) {
		this.products = products;
	}
	
}
