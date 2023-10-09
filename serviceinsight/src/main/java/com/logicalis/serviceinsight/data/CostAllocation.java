package com.logicalis.serviceinsight.data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.logicalis.serviceinsight.dao.CostItem;

public class CostAllocation {

	public enum Status {
		open("Open"), processed("Processed");
		
		private String description;
		
		Status(String description) {
			this.description = description;
		}
		
		public String getDescription() {
			return this.description;
		}
	}
	
	private Long id;
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="MMddyyyy", timezone="America/New_York")
	private Date month;
	private BigDecimal multiTenantTotal = new BigDecimal(0);
	private BigDecimal rentTotal = new BigDecimal(0);
	private BigDecimal specificTotal = new BigDecimal(0);
	private BigDecimal dedicatedTotal = new BigDecimal(0);
	
	private BigDecimal multiTenantAllocationTotal = new BigDecimal(0);
	private BigDecimal multiTenantAllocatedAmountTotal = new BigDecimal(0);
	private BigDecimal rentAllocationTotal = new BigDecimal(0);
	private BigDecimal rentAllocatedAmountTotal = new BigDecimal(0);
	private BigDecimal specificAllocationTotal = new BigDecimal(0);
	private BigDecimal specificAllocatedAmountTotal = new BigDecimal(0);
	private BigDecimal serviceSpecificAllocatedAmountTotal = new BigDecimal(0);
	
	private List<CostAllocationLineItem> lineItems = new ArrayList<CostAllocationLineItem>();
	private List<UnallocatedExpense> unallocatedExpenses = new ArrayList<UnallocatedExpense>();
	private List<CostItem> dedicatedCosts = new ArrayList<CostItem>();
	private Status status;
	
	public CostAllocation() {}
	
	public CostAllocation(Long id, Date month, BigDecimal multiTenantTotal, BigDecimal rentTotal,
			BigDecimal specificTotal, BigDecimal dedicatedTotal, List<CostAllocationLineItem> lineItems,
			Status status) {
		super();
		this.id = id;
		this.month = month;
		this.multiTenantTotal = multiTenantTotal;
		this.rentTotal = rentTotal;
		this.specificTotal = specificTotal;
		this.dedicatedTotal = dedicatedTotal;
		this.lineItems = lineItems;
		this.status = status;
	}

	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public Date getMonth() {
		return month;
	}
	
	public void setMonth(Date month) {
		this.month = month;
	}
	
	public BigDecimal getMultiTenantTotal() {
		return multiTenantTotal;
	}
	
	public void setMultiTenantTotal(BigDecimal multiTenantTotal) {
		this.multiTenantTotal = multiTenantTotal;
	}
	
	public BigDecimal getRentTotal() {
		return rentTotal;
	}
	
	public void setRentTotal(BigDecimal rentTotal) {
		this.rentTotal = rentTotal;
	}
	
	public BigDecimal getSpecificTotal() {
		return specificTotal;
	}
	
	public void setSpecificTotal(BigDecimal specificTotal) {
		this.specificTotal = specificTotal;
	}
	
	public BigDecimal getSpecificTotalFromUnallocatedExpenses() {
		BigDecimal total = new BigDecimal(0);
		
		List<UnallocatedExpense> unallocatedExpenses = getUnallocatedExpenses();
		if(unallocatedExpenses != null && unallocatedExpenses.size() > 0) {
			for(UnallocatedExpense unallocatedExpense: unallocatedExpenses) {
				total = total.add(unallocatedExpense.getAmount());
			}
		}
		
		return total;
	}
	
	public BigDecimal getDedicatedTotalFromCostItems() {
		BigDecimal total = new BigDecimal(0);
		
		List<CostItem> costItems = getDedicatedCosts();
		if(costItems != null && costItems.size() > 0) {
			for(CostItem costItem: costItems) {
				total = total.add(costItem.getAmount());
			}
		}
		
		return total;
	}
	
	public BigDecimal getDedicatedTotal() {
		return dedicatedTotal;
	}
	
	public void setDedicatedTotal(BigDecimal dedicatedTotal) {
		this.dedicatedTotal = dedicatedTotal;
	}
	
	public BigDecimal getMultiTenantAllocationTotal() {
		return multiTenantAllocationTotal;
	}

	public void setMultiTenantAllocationTotal(BigDecimal multiTenantAllocationTotal) {
		this.multiTenantAllocationTotal = multiTenantAllocationTotal;
	}

	public BigDecimal getMultiTenantAllocatedAmountTotal() {
		return multiTenantAllocatedAmountTotal;
	}

	public void setMultiTenantAllocatedAmountTotal(BigDecimal multiTenantAllocatedAmountTotal) {
		this.multiTenantAllocatedAmountTotal = multiTenantAllocatedAmountTotal;
	}

	public BigDecimal getRentAllocationTotal() {
		return rentAllocationTotal;
	}

	public void setRentAllocationTotal(BigDecimal rentAllocationTotal) {
		this.rentAllocationTotal = rentAllocationTotal;
	}

	public BigDecimal getRentAllocatedAmountTotal() {
		return rentAllocatedAmountTotal;
	}

	public void setRentAllocatedAmountTotal(BigDecimal rentAllocatedAmountTotal) {
		this.rentAllocatedAmountTotal = rentAllocatedAmountTotal;
	}

	public BigDecimal getSpecificAllocationTotal() {
		return specificAllocationTotal;
	}

	public void setSpecificAllocationTotal(BigDecimal specificAllocationTotal) {
		this.specificAllocationTotal = specificAllocationTotal;
	}

	public BigDecimal getSpecificAllocatedAmountTotal() {
		return specificAllocatedAmountTotal;
	}

	public void setSpecificAllocatedAmountTotal(BigDecimal specificAllocatedAmountTotal) {
		this.specificAllocatedAmountTotal = specificAllocatedAmountTotal;
	}

	public BigDecimal getServiceSpecificAllocatedAmountTotal() {
		return serviceSpecificAllocatedAmountTotal;
	}

	public void setServiceSpecificAllocatedAmountTotal(BigDecimal serviceSpecificAllocatedAmountTotal) {
		this.serviceSpecificAllocatedAmountTotal = serviceSpecificAllocatedAmountTotal;
	}

	public List<CostAllocationLineItem> getLineItems() {
		return lineItems;
	}
	
	public void setLineItems(List<CostAllocationLineItem> lineItems) {
		this.lineItems = lineItems;
	}
	
	public List<UnallocatedExpense> getUnallocatedExpenses() {
		return unallocatedExpenses;
	}

	public void setUnallocatedExpenses(List<UnallocatedExpense> unallocatedExpenses) {
		this.unallocatedExpenses = unallocatedExpenses;
	}

	public List<CostItem> getDedicatedCosts() {
		return dedicatedCosts;
	}

	public void setDedicatedCosts(List<CostItem> dedicatedCosts) {
		this.dedicatedCosts = dedicatedCosts;
	}

	public Status getStatus() {
		return status;
	}
	
	public void setStatus(Status status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "CostAllocation [id=" + id + ", month=" + month + ", multiTenantTotal=" + multiTenantTotal
				+ ", rentTotal=" + rentTotal + ", specificTotal=" + specificTotal + ", dedicatedTotal=" + dedicatedTotal
				+ ", lineItems=" + lineItems + ", status=" + status + "]";
	}
	
}
