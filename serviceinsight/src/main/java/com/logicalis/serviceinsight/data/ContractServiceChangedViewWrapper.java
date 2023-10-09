package com.logicalis.serviceinsight.data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.logicalis.serviceinsight.representation.ContractRollupRecord;

public class ContractServiceChangedViewWrapper {

	private List<ContractRollupRecord> previousMonth = new ArrayList<ContractRollupRecord>();
	private List<ContractRollupRecord> added = new ArrayList<ContractRollupRecord>();
	private List<ContractRollupRecord> removed = new ArrayList<ContractRollupRecord>();
	private ContractInvoice contractInvoice;
	private BigDecimal onetimeTotal;
	private BigDecimal recurringTotal;
	private BigDecimal onetimeDifference;
	private BigDecimal recurringDifference;
	
	
	public List<ContractRollupRecord> getPreviousMonth() {
		return previousMonth;
	}
	
	public void setPreviousMonth(List<ContractRollupRecord> previousMonth) {
		this.previousMonth = previousMonth;
	}
	
	public List<ContractRollupRecord> getAdded() {
		return added;
	}
	
	public void setAdded(List<ContractRollupRecord> added) {
		this.added = added;
	}
	
	public List<ContractRollupRecord> getRemoved() {
		return removed;
	}
	
	public void setRemoved(List<ContractRollupRecord> removed) {
		this.removed = removed;
	}

	public ContractInvoice getContractInvoice() {
		return contractInvoice;
	}

	public void setContractInvoice(ContractInvoice contractInvoice) {
		this.contractInvoice = contractInvoice;
	}

	public BigDecimal getOnetimeTotal() {
		return onetimeTotal;
	}

	public void setOnetimeTotal(BigDecimal onetimeTotal) {
		this.onetimeTotal = onetimeTotal;
	}

	public BigDecimal getRecurringTotal() {
		return recurringTotal;
	}

	public void setRecurringTotal(BigDecimal recurringTotal) {
		this.recurringTotal = recurringTotal;
	}

	public BigDecimal getOnetimeDifference() {
		return onetimeDifference;
	}

	public void setOnetimeDifference(BigDecimal onetimeDifference) {
		this.onetimeDifference = onetimeDifference;
	}

	public BigDecimal getRecurringDifference() {
		return recurringDifference;
	}

	public void setRecurringDifference(BigDecimal recurringDifference) {
		this.recurringDifference = recurringDifference;
	}
	
}
