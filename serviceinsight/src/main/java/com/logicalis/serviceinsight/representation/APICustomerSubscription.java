package com.logicalis.serviceinsight.representation;

import com.logicalis.serviceinsight.data.Contract;
import com.logicalis.serviceinsight.data.ContractServiceSubscription;
import com.logicalis.serviceinsight.data.Customer;

public class APICustomerSubscription {

	private Customer customer;
	private Contract contract;
	private ContractServiceSubscription subscription;
	
	public Customer getCustomer() {
		return customer;
	}
	
	public void setCustomer(Customer customer) {
		this.customer = customer;
	}
	
	public Contract getContract() {
		return contract;
	}
	
	public void setContract(Contract contract) {
		this.contract = contract;
	}
	
	public ContractServiceSubscription getSubscription() {
		return subscription;
	}
	
	public void setSubscription(ContractServiceSubscription subscription) {
		this.subscription = subscription;
	}

	@Override
	public String toString() {
		return "APICustomerSubscription [customer=" + customer + ", contract=" + contract + ", subscription="
				+ subscription + "]";
	}
	
}
