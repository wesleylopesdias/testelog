package com.logicalis.serviceinsight.service;

import java.util.Date;
import java.util.List;

import com.logicalis.serviceinsight.data.CostAllocation;
import com.logicalis.serviceinsight.data.CostAllocationLineItem;

public interface CostAllocationService {

	public CostAllocation costAllocation(Long id) throws ServiceException;
	public CostAllocation costAllocationForLineItem(Long lineItemId) throws ServiceException;
	public CostAllocation costAllocationByMonth(Date month) throws ServiceException;
	public Long saveCostAllocation(CostAllocation costAllocation) throws ServiceException;
	public void updateCostAllocation(CostAllocation costAllocation) throws ServiceException;
	public void importCostAllocation(CostAllocation costAllocation, Date importMonth) throws ServiceException;
	public CostAllocation calculate(CostAllocation costAllocation);
	
	public List<CostAllocationLineItem> costAllocationLineItemsForCostAllocation(Long costAllocationId) throws ServiceException;
	public Long saveCostAllocationLineItem(CostAllocationLineItem costAllocationLineItem) throws ServiceException;
	public void updateCostAllocationLineItem(CostAllocationLineItem costAllocationLineItem) throws ServiceException;
	public void generateCostItemsFromCostAllocation(Long costAllocationId) throws ServiceException;
	
}
