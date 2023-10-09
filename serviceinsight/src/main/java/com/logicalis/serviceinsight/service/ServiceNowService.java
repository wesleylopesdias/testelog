package com.logicalis.serviceinsight.service;

import java.util.List;

import com.logicalis.serviceinsight.data.ServiceNowCI;
import com.logicalis.serviceinsight.util.servicenow.SNCI;
import com.logicalis.serviceinsight.util.servicenow.SNContract;
import com.logicalis.serviceinsight.util.servicenow.SNContractCI;

public interface ServiceNowService {

	public SNContract findContractBySysId(String contractSysId) throws ServiceException;
	public SNContract findContractByCustomerSysIdAndJobNumber(String customerSysId, String jobNumber) throws ServiceException;

	public SNContractCI findContractCIBySysId(String sysId) throws ServiceException;
	public List<SNContractCI> findContractCIsForContract(String contractSysId) throws ServiceException;
	
	public SNCI findCIBySysId(String sysId) throws ServiceException;
	public List<SNCI> findCIsForContract(String contractSysId) throws ServiceException;
	
	
	public ServiceNowCI serviceNowCI(Long id) throws ServiceException;
	public Long saveServiceNowCI(ServiceNowCI serviceNowCI) throws ServiceException;
	public void updateServiceNowCI(ServiceNowCI serviceNowCI) throws ServiceException;
	public void deleteServiceNowCI(Long id) throws ServiceException;
	public ServiceNowCI serviceNowCIBySysId(String snSysId) throws ServiceException;
	public List<ServiceNowCI> serviceNowCIsForContract(Long contractId);
	public List<ServiceNowCI> serviceNowCIsForSNContract(Long contractId, String contractSysId);
	public List<ServiceNowCI> serviceNowCIs();
	
	public void syncContractsFromServiceNow() throws ServiceException;
	public void syncContractCIsFromServiceNow() throws ServiceException;
	public void syncContractCIsForContractFromServiceNow(String contractSysId, Long contractId) throws ServiceException;
}
