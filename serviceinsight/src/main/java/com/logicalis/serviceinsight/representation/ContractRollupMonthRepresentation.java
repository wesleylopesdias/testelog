package com.logicalis.serviceinsight.representation;

import java.util.List;

import com.logicalis.serviceinsight.data.ContractInvoice;

public class ContractRollupMonthRepresentation {

	private List<ContractRollupRecord> contractRollupRecords;
	private ContractInvoice contractInvoice;
	
	public ContractRollupMonthRepresentation(){}
	
	public ContractRollupMonthRepresentation(
			List<ContractRollupRecord> contractRollupRecords,
			ContractInvoice contractInvoice) {
		super();
		this.contractRollupRecords = contractRollupRecords;
		this.contractInvoice = contractInvoice;
	}
	
	public List<ContractRollupRecord> getContractRollupRecords() {
		return contractRollupRecords;
	}
	public void setContractRollupRecords(
			List<ContractRollupRecord> contractRollupRecords) {
		this.contractRollupRecords = contractRollupRecords;
	}
	public ContractInvoice getContractInvoice() {
		return contractInvoice;
	}
	public void setContractInvoice(ContractInvoice contractInvoice) {
		this.contractInvoice = contractInvoice;
	}
}
