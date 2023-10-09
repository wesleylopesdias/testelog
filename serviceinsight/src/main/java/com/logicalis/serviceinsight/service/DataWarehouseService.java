package com.logicalis.serviceinsight.service;

import com.logicalis.serviceinsight.data.DataWarehouseLineItem;
import java.util.Date;
import java.util.List;

public interface DataWarehouseService extends BaseService {

	public List<DataWarehouseLineItem> getCIRecords(Date importDate) throws ServiceException;
	public void getContractRecords(Date importDate) throws ServiceException;
	public void getContractUpdateRecords(Date importDate) throws ServiceException;
	public void getCostRecords(Date importDate) throws ServiceException;
	public void updateDataWarehouseCIs() throws ServiceException;
	public void updateDataWarehouseCIsforMonthOf(Date importDate) throws ServiceException;
	public void updateDataWarehouseContracts() throws ServiceException;
	public void updateDataWarehouseContractsforMonthOf(Date importDate) throws ServiceException;
	public void updateDataWarehouseContractUpdates() throws ServiceException;
	public void updateDataWarehouseContractUpdatesforMonthOf(Date importDate) throws ServiceException;
	public void updateDataWarehouseCosts() throws ServiceException;
	public void updateDataWarehouseCostsforMonthOf(Date importDate) throws ServiceException;
}
