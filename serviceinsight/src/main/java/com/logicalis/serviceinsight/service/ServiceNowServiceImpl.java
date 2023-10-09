package com.logicalis.serviceinsight.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.transaction.annotation.Transactional;

import com.logicalis.serviceinsight.data.Contract;
import com.logicalis.serviceinsight.data.Customer;
import com.logicalis.serviceinsight.data.SNRecord;
import com.logicalis.serviceinsight.data.Service;
import com.logicalis.serviceinsight.data.ServiceNowCI;
import com.logicalis.serviceinsight.util.servicenow.SNCI;
import com.logicalis.serviceinsight.util.servicenow.SNContract;
import com.logicalis.serviceinsight.util.servicenow.SNContractCI;
import com.logicalis.serviceinsight.util.servicenow.SNObjectAdapter;

@Transactional(readOnly = false, rollbackFor = ServiceException.class)
@org.springframework.stereotype.Service
public class ServiceNowServiceImpl extends BaseServiceImpl implements ServiceNowService {

	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	@Autowired
	SNObjectAdapter snObjectAdapter;
	
	@Autowired
	ContractDaoService contractDaoService;
	

	//Service Now Tables
	private static String CONTRACT_SN_URL ="ast_service"; 
	private static String CONTRACT_CI_SN_URL = "contract_rel_ci";
	private static String CI_SN_URL = "cmdb_ci";

	@Override
	public SNContract findContractByCustomerSysIdAndJobNumber(String customerSysId, String jobNumber) throws ServiceException {
		SNContract contract = null;

		HashMap<String, String> criteria = new HashMap<String, String>();
		criteria.put("vendor", customerSysId);
		criteria.put("number", jobNumber);

		ArrayList<SNRecord> SNRecords = (ArrayList<SNRecord>) snObjectAdapter.getSNRecords(CONTRACT_SN_URL, criteria);

		if(SNRecords.size() == 1) {
			SNRecord r = SNRecords.get(0);
			contract = new SNContract(r);
		}
		
		//we'll retrieve the child contracts if we got the parent
		if(contract != null) {
			criteria = new HashMap<String, String>();
			criteria.put("parent", contract.getSysId());
			
			SNRecords = (ArrayList<SNRecord>) snObjectAdapter.getSNRecords(CONTRACT_SN_URL, criteria);

			if(SNRecords.size() > 0) {
				List<SNContract> childContracts = new ArrayList<SNContract>();
				for(SNRecord record : SNRecords) {
					SNContract childContract = new SNContract(record);
					childContracts.add(childContract);
				}
				contract.setChildContracts(childContracts);
			}
		}

		return contract;
	}
	
	@Override
	public SNContract findContractBySysId(String contractSysId) throws ServiceException {
		SNContract contract = null;

		SNRecord r = snObjectAdapter.get(CONTRACT_SN_URL, contractSysId);

		if(r != null) {
			contract = new SNContract(r);
		}
		
		//we'll retrieve the child contracts if we got the parent
		if(contract != null) {
			HashMap<String, String> criteria = new HashMap<String, String>();
			criteria.put("parent", contract.getSysId());
			
			ArrayList<SNRecord> SNRecords = (ArrayList<SNRecord>) snObjectAdapter.getSNRecords(CONTRACT_SN_URL, criteria);

			if(SNRecords.size() > 0) {
				List<SNContract> childContracts = new ArrayList<SNContract>();
				for(SNRecord record : SNRecords) {
					SNContract childContract = new SNContract(record);
					childContracts.add(childContract);
				}
				contract.setChildContracts(childContracts);
			}
		}

		return contract;
	}

	@Override
	public SNContractCI findContractCIBySysId(String sysId) throws ServiceException {
		SNContractCI contractCI = null;
		SNRecord r = snObjectAdapter.get(CONTRACT_CI_SN_URL, sysId);
		if(r != null) {
			contractCI = new SNContractCI(r);
		}

		return contractCI;
	}

	@Override
	public List<SNContractCI> findContractCIsForContract(String contractSysId) throws ServiceException {
		List<SNContractCI> contractCIs = new ArrayList<SNContractCI>();
		HashMap<String, String> criteria = new HashMap<String, String>();
		criteria.put("contract", contractSysId);

		ArrayList<SNRecord> SNRecords = (ArrayList<SNRecord>) snObjectAdapter.getSNRecords(CONTRACT_CI_SN_URL, criteria);
		if(SNRecords.size() > 0) {
			for(SNRecord r : SNRecords) {
				contractCIs.add(new SNContractCI(r));
			}
		}

		return contractCIs;
	}
	
	@Override
	public SNCI findCIBySysId(String sysId) throws ServiceException {
		SNCI ci = null;
		SNRecord r = snObjectAdapter.get(CI_SN_URL, sysId);
		if(r != null) {
			ci = new SNCI(r);
		}

		return ci;
	}
	
	@Override
	public List<SNCI> findCIsForContract(String contractSysId) throws ServiceException {
		List<SNCI> cis = new ArrayList<SNCI>();
		List<SNContractCI> contractCIs = findContractCIsForContract(contractSysId);
		
		for(SNContractCI contractCI : contractCIs) {
			SNCI ci = findCIBySysId(contractCI.getCiItemSysId());
			if(ci != null) {
				cis.add(ci);
			}
		}
		
		return cis;
	}
	
	@Override
	public void syncContractsFromServiceNow() throws ServiceException {
		//we only want the active contracts -- no archived contracts
		List<Contract> contracts = contractDaoService.contracts(Boolean.FALSE);
		for(Contract contract : contracts) {
			try {
				Customer customer = contractDaoService.customer(contract.getCustomerId());
				String customerSysId = customer.getServiceNowSysId();
				if(customerSysId != null) {
					SNContract snContract = findContractByCustomerSysIdAndJobNumber(customerSysId, contract.getJobNumber());
					if(snContract != null) {
						String snSysId = snContract.getSysId();
						if(snSysId != null && !snSysId.equals(contract.getServiceNowSysId())) {
							contract.setServiceNowSysId(snSysId);
							contractDaoService.saveContract(contract);
						}
					} else {
						//no contract returned -- we'll see if there are any child companies and check those for contracts
						if(customer.getChildren() != null && customer.getChildren().size() > 0) {
							log.debug("ISV -- Checking for Child Customer Contracts for Parent [" + customer.getName() + "]");
							for(Customer child : customer.getChildren()) {
								String childSysId = child.getServiceNowSysId();
								snContract = findContractByCustomerSysIdAndJobNumber(childSysId, contract.getJobNumber());
								if(snContract != null) {
									String snSysId = snContract.getSysId();
									if(snSysId != null && !snSysId.equals(contract.getServiceNowSysId())) {
										log.debug("Found Contract for child [" + child.getName() + "]");
										contract.setServiceNowSysId(snSysId);
										contractDaoService.saveContract(contract);
										break;
									}
								} else {
                                                                        // note: this log statement generates a lot of logging, so disabling
									//log.debug("Not Found for child [" + child.getName() + "]");
								}
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void syncContractCIsFromServiceNow() throws ServiceException {
		//we only want the active contracts -- no archived contracts
		List<Contract> contracts = contractDaoService.contracts(Boolean.FALSE);
		for(Contract contract : contracts) {
			syncContractCIsForContractFromServiceNow(contract.getServiceNowSysId(), contract.getId());
		}
	}
	
	@Override
	public void syncContractCIsForContractFromServiceNow(String contractSysId, Long contractId) throws ServiceException {
		if(contractSysId != null) {
			//pull CIs for the base level contract
			updateCIsForContract(contractSysId, contractId);
			
			//pull CIs for child contracts in SN
			SNContract snContract = findContractBySysId(contractSysId);
			
			if(snContract != null) {
				List<SNContract> childContracts = snContract.getChildContracts();
				if(childContracts != null && childContracts.size() > 0) {
					for(SNContract childContract : childContracts) {
						updateCIsForContract(childContract.getSysId(), contractId);
					}
				}
			}
			
		}
	}
	
	private void updateCIsForContract(String contractSysId, Long contractId) throws ServiceException {
		List<SNCI> cis = findCIsForContract(contractSysId);

		//add or update and CIs in the database
		for(SNCI ci : cis) {
			boolean save = false;
			String snSysId = ci.getSysId();
			String ciName = ci.getName();
			log.info("Checking CI: " + ciName + " -- Sys ID: " + snSysId);
			ServiceNowCI serviceNowCI = serviceNowCIBySysId(snSysId);
			log.info("ServiceNowCI: " + serviceNowCI);
			if(serviceNowCI == null) {
				save = true;
				serviceNowCI = new ServiceNowCI();
				serviceNowCI.setServiceNowSysId(snSysId);
			}
			
			Long contractServiceId = null;
			Service matchingContractService = contractDaoService.findContractServiceByNameAndContractId(ciName, contractId);
			if(matchingContractService != null) {
				contractServiceId = matchingContractService.getId();
			}
			
			Long existingContractServiceId = null;
			if(serviceNowCI.getContractServiceId() != null && serviceNowCI.getContractServiceId() != 0) existingContractServiceId = serviceNowCI.getContractServiceId();
			if(!ciName.equals(serviceNowCI.getName()) || (contractServiceId != null && !contractServiceId.equals(existingContractServiceId)) || (contractServiceId == null && existingContractServiceId != null)) {
				save = true;
			} else {
				log.info("failed a condition to save");
				log.info("Name Equals: " + !ciName.equals(serviceNowCI.getName()));
				log.info("Contract Service Id 1: " + (contractServiceId != null && !contractServiceId.equals(existingContractServiceId)));
				log.info("Contract Service Id 2: " + (contractServiceId == null && existingContractServiceId != null));
			}
			
			serviceNowCI.setContractId(contractId);
			serviceNowCI.setName(ciName);
			serviceNowCI.setContractServiceId(contractServiceId);
			serviceNowCI.setContractServiceNowSysId(contractSysId);
			
			if(save) {
				if(!"7".equals(ci.getStatus())) {
					log.info("Saving CI: " + ciName);
					saveServiceNowCI(serviceNowCI);
				} else {
					log.info("Skipping adding CI [" + ciName + "] because it is retired.");
				}
			}
		}
		
		//delete any cis that aren't in SN anymore
		List<ServiceNowCI> serviceNowCIs = serviceNowCIsForSNContract(contractId, contractSysId);
		for(ServiceNowCI serviceNowCI : serviceNowCIs) {
			boolean exists = false;
			for(SNCI ci : cis) {
				if(ci.getSysId().equals(serviceNowCI.getServiceNowSysId())) {
					exists = true;
					
					//remove any CI with a status of retired
					
					if("7".equals(ci.getStatus())) {
						log.info("Found CI with status of [" + ci.getStatus() + "] (Retired) to delete: " + ci.getName());
						exists = false;
					}
				}
			}
			if(!exists) {
				log.info("Deleting CI: " + serviceNowCI.getName());
				deleteServiceNowCI(serviceNowCI.getId());
			}
		}
	}
	
	@Override
    public List<ServiceNowCI> serviceNowCIs() {
        String query = "select * from service_now_ci ci";
        return jdbcTemplate.query(query, new RowMapper<ServiceNowCI>() {
            @Override
            public ServiceNowCI mapRow(ResultSet rs, int i) throws SQLException {
            	ServiceNowCI ci =  new ServiceNowCI(
                        rs.getLong("id"),
                        rs.getString("sn_sys_id"),
                        rs.getString("name"),
                        rs.getLong("contract_id"),
                        rs.getLong("contract_service_id"),
                        rs.getString("contract_sn_sys_id"));
                
            	ci.setCreated(rs.getDate("created"));
            	ci.setCreatedBy(rs.getString("created_by"));
            	ci.setUpdated(rs.getDate("updated"));
            	ci.setUpdatedBy(rs.getString("updated_by"));
                return ci;
            }
        });
    }
	
	@Override
    public List<ServiceNowCI> serviceNowCIsForContract(Long contractId) {
        String query = "select * from service_now_ci ci where contract_id = ? order by LOWER(name)";
        return jdbcTemplate.query(query, new Object[]{contractId}, new RowMapper<ServiceNowCI>() {
            @Override
            public ServiceNowCI mapRow(ResultSet rs, int i) throws SQLException {
            	ServiceNowCI ci =  new ServiceNowCI(
                        rs.getLong("id"),
                        rs.getString("sn_sys_id"),
                        rs.getString("name"),
                        rs.getLong("contract_id"),
                        rs.getLong("contract_service_id"),
                        rs.getString("contract_sn_sys_id"));
                
            	ci.setCreated(rs.getDate("created"));
            	ci.setCreatedBy(rs.getString("created_by"));
            	ci.setUpdated(rs.getDate("updated"));
            	ci.setUpdatedBy(rs.getString("updated_by"));
                return ci;
            }
        });
    }
	
	@Override
    public List<ServiceNowCI> serviceNowCIsForSNContract(Long contractId, String contractSysId) {
        String query = "select * from service_now_ci ci where contract_id = :contract_id and contract_sn_sys_id = :contract_sys_id";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("contract_id", contractId);
        params.put("contract_sys_id", contractSysId);
        return namedJdbcTemplate.query(query, params, new RowMapper<ServiceNowCI>() {
            @Override
            public ServiceNowCI mapRow(ResultSet rs, int i) throws SQLException {
            	ServiceNowCI ci =  new ServiceNowCI(
                        rs.getLong("id"),
                        rs.getString("sn_sys_id"),
                        rs.getString("name"),
                        rs.getLong("contract_id"),
                        rs.getLong("contract_service_id"),
                        rs.getString("contract_sn_sys_id"));
                
            	ci.setCreated(rs.getDate("created"));
            	ci.setCreatedBy(rs.getString("created_by"));
            	ci.setUpdated(rs.getDate("updated"));
            	ci.setUpdatedBy(rs.getString("updated_by"));
                return ci;
            }
        });
    }
	
	@Override
    public ServiceNowCI serviceNowCI(Long id) throws ServiceException {
        String query = "select * from service_now_ci ci where ci.id = ?";
        
        return jdbcTemplate.queryForObject(query, new Object[]{id}, new RowMapper<ServiceNowCI>() {
            @Override
            public ServiceNowCI mapRow(ResultSet rs, int i) throws SQLException {
            	ServiceNowCI ci =  new ServiceNowCI(
                        rs.getLong("id"),
                        rs.getString("sn_sys_id"),
                        rs.getString("name"),
                        rs.getLong("contract_id"),
                        rs.getLong("contract_service_id"),
                        rs.getString("contract_sn_sys_id"));
                
            	ci.setCreated(rs.getDate("created"));
            	ci.setCreatedBy(rs.getString("created_by"));
            	ci.setUpdated(rs.getDate("updated"));
            	ci.setUpdatedBy(rs.getString("updated_by"));
                return ci;
            }
        });
    }
	
	@Override
    public ServiceNowCI serviceNowCIBySysId(String snSysId) throws ServiceException {
		Integer count = jdbcTemplate.queryForObject("select count(*) from service_now_ci ci where ci.sn_sys_id = ?", Integer.class, snSysId);
        if (!count.equals(1)) return null;
		
        String query = "select * from service_now_ci ci where ci.sn_sys_id = ?";
        
        return jdbcTemplate.queryForObject(query, new Object[]{snSysId}, new RowMapper<ServiceNowCI>() {
            @Override
            public ServiceNowCI mapRow(ResultSet rs, int i) throws SQLException {
            	ServiceNowCI ci =  new ServiceNowCI(
                        rs.getLong("id"),
                        rs.getString("sn_sys_id"),
                        rs.getString("name"),
                        rs.getLong("contract_id"),
                        rs.getLong("contract_service_id"),
                        rs.getString("contract_sn_sys_id"));
                
            	ci.setCreated(rs.getDate("created"));
            	ci.setCreatedBy(rs.getString("created_by"));
            	ci.setUpdated(rs.getDate("updated"));
            	ci.setUpdatedBy(rs.getString("updated_by"));
                return ci;
            }
        });
    }
  

    @Override
    public Long saveServiceNowCI(ServiceNowCI serviceNowCI) throws ServiceException {
        if (serviceNowCI.getId() != null) {
            updateServiceNowCI(serviceNowCI);
            return serviceNowCI.getId();
        }
        if (serviceNowCI.getContractId() == null) {
            throw new ServiceException(messageSource.getMessage("validation_error_servicenow_ci_contract", null, LocaleContextHolder.getLocale()));
        } 
        
        if (serviceNowCI.getServiceNowSysId() == null) {
            throw new ServiceException(messageSource.getMessage("validation_error_servicenow_ci_sys_id", null, LocaleContextHolder.getLocale()));
        }
        
        try {
            SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
            jdbcInsert.withTableName("service_now_ci").usingGeneratedKeyColumns("id");
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("sn_sys_id", serviceNowCI.getServiceNowSysId());
            params.put("contract_id", serviceNowCI.getContractId());
            params.put("contract_service_id", serviceNowCI.getContractServiceId());
            params.put("contract_sn_sys_id", serviceNowCI.getContractServiceNowSysId());
            params.put("name", serviceNowCI.getName());
            params.put("created_by", authenticatedUser());
            Number pk = jdbcInsert.executeAndReturnKey(new MapSqlParameterSource(params));
            
            Long serviceNowCIId = (Long) pk;
            
            return serviceNowCIId;
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_servicenow_ci_insert", new Object[]{any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    }
    
    @Override
    public void updateServiceNowCI(ServiceNowCI serviceNowCI) throws ServiceException {
        Integer count = jdbcTemplate.queryForObject("select count(*) from service_now_ci where id = ?", Integer.class, serviceNowCI.getId());
        if (!count.equals(1)) {
            throw new ServiceException(messageSource.getMessage("servicenow_ci_not_found_for_id", new Object[]{serviceNowCI.getId()}, LocaleContextHolder.getLocale()));
        }
        if (serviceNowCI.getContractId() == null) {
            throw new ServiceException(messageSource.getMessage("validation_error_servicenow_ci_contract", null, LocaleContextHolder.getLocale()));
        } 
        if (serviceNowCI.getServiceNowSysId() == null) {
            throw new ServiceException(messageSource.getMessage("validation_error_servicenow_ci_sys_id", null, LocaleContextHolder.getLocale()));
        }
        
        try {
            int updated = jdbcTemplate.update("update service_now_ci set sn_sys_id = ?, name = ?, contract_id = ?, contract_service_id = ?, contract_sn_sys_id = ?, updated = ?, updated_by = ? where id = ?",
                    new Object[]{serviceNowCI.getServiceNowSysId(), serviceNowCI.getName(), serviceNowCI.getContractId(), serviceNowCI.getContractServiceId(), serviceNowCI.getContractServiceNowSysId(), 
            		new DateTime().withZone(DateTimeZone.forID(TZID)).toDate(), authenticatedUser(), serviceNowCI.getId()});
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_servicenow_ci_update", new Object[]{serviceNowCI.getId(), any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    }

    @Override
    public void deleteServiceNowCI(Long id) throws ServiceException {
        Integer count = jdbcTemplate.queryForObject("select count(*) from service_now_ci where id = ?", Integer.class, id);
        if (!count.equals(1)) {
            throw new ServiceException(messageSource.getMessage("servicenow_ci_not_found_for_id", new Object[]{id}, LocaleContextHolder.getLocale()));
        }
        try {
            int updated = jdbcTemplate.update("delete from service_now_ci where id = ?", id);
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_servicenow_ci_delete", new Object[]{id, any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    }
}
