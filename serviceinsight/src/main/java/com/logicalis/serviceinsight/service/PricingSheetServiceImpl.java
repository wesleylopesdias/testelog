package com.logicalis.serviceinsight.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import com.logicalis.serviceinsight.dao.ExpenseCategory;
import com.logicalis.serviceinsight.data.Contract;
import com.logicalis.serviceinsight.data.ContractUpdate;
import com.logicalis.serviceinsight.data.Device;
import com.logicalis.serviceinsight.data.PricingSheet;
import com.logicalis.serviceinsight.data.PricingSheetProduct;
import com.logicalis.serviceinsight.data.ScheduledTask;
import com.logicalis.serviceinsight.data.Service;

@org.springframework.stereotype.Service
@Transactional(readOnly = false, rollbackFor = ServiceException.class)
public class PricingSheetServiceImpl extends BaseServiceImpl implements PricingSheetService {

	@Autowired
	ApplicationDataDaoService applicationDataDaoService;
	
	@Autowired
	ContractRevenueService contractRevenueService;
	
	@Autowired
	ContractDaoService contractDaoService;
	
	private static final Long OSP_SERVICE_ID_UNDEFINED = 90001L;
	private static final String GENERATE_PRICING_SHEETS_TASK = "generate_pricing_sheets_sync";
	
	//gets the basic details of a pricing sheet
	private static final String BASE_PRICING_SHEET_QUERY = "select ps.id, ps.contract_id, ps.active,"
            + " ps.created ps_created, ps.created_by ps_created_by, ps.updated ps_updated, ps.updated_by ps_updated_by"
            + " from pricing_sheet ps";
	//gets the basic details of a pricing sheet
	private static final String BASE_OSM_PRICING_SHEET_QUERY = "select ps.id, ps.contract_id, ps.active, co.sn_sys_id contract_sys_id, co.job_number contract_job_number, co.name contract_name, cu.id customer_id, cu.sn_sys_id customer_sys_id, "
	            + " ps.created ps_created, ps.created_by ps_created_by, ps.updated ps_updated, ps.updated_by ps_updated_by"
	            + " from pricing_sheet ps"
	            + " inner join contract co on ps.contract_id = co.id"
	            + " inner join customer cu on co.customer_id = cu.id";
	//gets the products from a pricing sheet with device and service details
	private static final String BASE_PRICING_SHEET_PRODUCT_QUERY = "select psp.id, psp.pricing_sheet_id, psp.device_id, psp.service_id, psp.onetime_revenue, psp.recurring_revenue, psp.removal_revenue,"
			+ " psp.status, psp.status_message, psp.manual_override, psp.erp_price, psp.discount, psp.subscription_start_date, psp.subscription_end_date, psp.unit_count, psp.previous_unit_count, "
			+ " d.id device_id, d.description device_description, d.part_number device_part_number, d.device_type device_type, d.alt_id device_alt_id, s.id service_id, s.name service_name,"
			+ " psp.created psp_created, psp.created_by psp_created_by, psp.updated psp_updated, psp.updated_by psp_updated_by"
            + " from pricing_sheet_product psp"
            + " inner join device d on psp.device_id = d.id"
            + " inner join service s on psp.service_id = s.id";
	//gets the products from a pricing sheet with device and service and contract details
		private static final String BASE_PRICING_SHEET_PRODUCT_WITH_CONTRACT_QUERY = "select psp.id, psp.pricing_sheet_id, psp.device_id, psp.service_id, psp.onetime_revenue, psp.recurring_revenue, psp.removal_revenue,"
				+ " psp.status, psp.status_message, psp.manual_override, psp.erp_price, psp.discount, psp.subscription_start_date, psp.subscription_end_date, psp.unit_count, psp.previous_unit_count, "
				+ " d.id device_id, d.description device_description, d.part_number device_part_number, d.device_type device_type, d.alt_id device_alt_id, s.id service_id, s.name service_name, ps.id, co.id,"
				+ " psp.created psp_created, psp.created_by psp_created_by, psp.updated psp_updated, psp.updated_by psp_updated_by"
	            + " from pricing_sheet_product psp"
	            + " inner join device d on psp.device_id = d.id"
	            + " inner join service s on psp.service_id = s.id"
	            + " inner join pricing_sheet ps on psp.pricing_sheet_id = ps.id"
	            + " inner join contract co on ps.contract_id = co.id";
	
	
	@Override
	public PricingSheet pricingSheet(Long id) throws ServiceException {
		Integer count = jdbcTemplate.queryForObject("select count(*) from pricing_sheet where id = ?", Integer.class, id);
        if (!count.equals(1)) {
            throw new ServiceException(messageSource.getMessage("validation_error_pricing_sheet_not_found", new Object[]{id}, LocaleContextHolder.getLocale()));
        }
        String query = BASE_PRICING_SHEET_QUERY
                + " where ps.id = ?";
        return jdbcTemplate.queryForObject(query, new Object[]{id},
                new RowMapper<PricingSheet>() {
            @Override
            public PricingSheet mapRow(ResultSet rs, int i) throws SQLException {
            	PricingSheet pricingSheet = new PricingSheet(
                        rs.getLong("id"),
                        rs.getLong("contract_id"),
                        rs.getBoolean("active"));
            	pricingSheet.setCreated(rs.getTimestamp("ps_created"));
            	pricingSheet.setCreatedBy(rs.getString("ps_created_by"));
            	pricingSheet.setUpdated(rs.getTimestamp("ps_updated"));
            	pricingSheet.setUpdatedBy(rs.getString("ps_updated_by"));
            	
            	pricingSheet.setProducts(jdbcTemplate.query(BASE_PRICING_SHEET_PRODUCT_QUERY
                        + " where psp.pricing_sheet_id = ?"
                        + " order by psp.created desc", new Object[]{pricingSheet.getId()}, new RowMapper<PricingSheetProduct>() {
                    @Override
                    public PricingSheetProduct mapRow(ResultSet rs, int i) throws SQLException {
                        return new PricingSheetProduct(
                                rs.getLong("id"),
                                rs.getLong("pricing_sheet_id"),
                                rs.getLong("device_id"),
                                rs.getString("device_part_number"),
                                rs.getString("device_description"),
                                (rs.getString("device_type") == null) ? null : Device.DeviceType.valueOf(rs.getString("device_type")),
                                rs.getString("device_alt_id"),
                                rs.getLong("service_id"),
                                rs.getString("service_name"),
                                rs.getBigDecimal("onetime_revenue"),
                                rs.getBigDecimal("recurring_revenue"),
                                rs.getBigDecimal("removal_revenue"), 
                                rs.getBigDecimal("erp_price"),
                                rs.getBigDecimal("discount"),
                                rs.getDate("subscription_start_date"),
                                rs.getDate("subscription_end_date"),
                                rs.getInt("unit_count"),
                                rs.getInt("previous_unit_count"),
                                (rs.getString("status") == null) ? null : PricingSheetProduct.Status.valueOf(rs.getString("status")),
                                rs.getString("status_message"),
                                rs.getBoolean("manual_override"));
                    }
                }));
            	
                return pricingSheet;
            }
        });
	}
	
	@Override
	public List<PricingSheet> pricingSheets() throws ServiceException {
		String query = BASE_PRICING_SHEET_QUERY;
        List<PricingSheet> pricingSheets = namedJdbcTemplate.query(query,  
                new RowMapper<PricingSheet>() {
            @Override
            public PricingSheet mapRow(ResultSet rs, int i) throws SQLException {
            	PricingSheet pricingSheet = new PricingSheet(
                        rs.getLong("id"),
                        rs.getLong("contract_id"),
                        rs.getBoolean("active"));
            	pricingSheet.setCreated(rs.getTimestamp("ps_created"));
            	pricingSheet.setCreatedBy(rs.getString("ps_created_by"));
            	pricingSheet.setUpdated(rs.getTimestamp("ps_updated"));
            	pricingSheet.setUpdatedBy(rs.getString("ps_updated_by"));
            	
            	pricingSheet.setProducts(jdbcTemplate.query(BASE_PRICING_SHEET_PRODUCT_QUERY
                        + " where psp.pricing_sheet_id = ?"
                        + " order by psp.created desc", new Object[]{pricingSheet.getId()}, new RowMapper<PricingSheetProduct>() {
                    @Override
                    public PricingSheetProduct mapRow(ResultSet rs, int i) throws SQLException {
                        return new PricingSheetProduct(
                                rs.getLong("id"),
                                rs.getLong("pricing_sheet_id"),
                                rs.getLong("device_id"),
                                rs.getString("device_part_number"),
                                rs.getString("device_description"),
                                (rs.getString("device_type") == null) ? null : Device.DeviceType.valueOf(rs.getString("device_type")),
                                rs.getString("device_alt_id"),
                                rs.getLong("service_id"),
                                rs.getString("service_name"),
                                rs.getBigDecimal("onetime_revenue"),
                                rs.getBigDecimal("recurring_revenue"),
                                rs.getBigDecimal("removal_revenue"), 
                                rs.getBigDecimal("erp_price"),
                                rs.getBigDecimal("discount"),
                                rs.getDate("subscription_start_date"),
                                rs.getDate("subscription_end_date"),
                                rs.getInt("unit_count"),
                                rs.getInt("previous_unit_count"),
                                (rs.getString("status") == null) ? null : PricingSheetProduct.Status.valueOf(rs.getString("status")),
                                rs.getString("status_message"),
                                rs.getBoolean("manual_override"));
                    }
                }));
            	
                return pricingSheet;
            }
        });
        
        return pricingSheets;
	}
	
	@Override
	@Async
	@Scheduled(cron = "0 10 22 * * *") //10:10pm
	public void generatePricingSheetsForAllCustomers() {
		try {
			ScheduledTask st = applicationDataDaoService.findScheduledTaskByCode(GENERATE_PRICING_SHEETS_TASK);
	        if (st != null && st.getEnabled()) {
	            log.info("Running Task: " + st.getName());
	            
	            List<Contract> contracts = contractDaoService.contracts(Boolean.FALSE);
	            log.info("About to process " + contracts.size() + " contracts.");
	            int count = 1;
	    		for(Contract contract : contracts) {
	    			try {
	    				log.info("(" + count + " of " + contracts.size() + ") == Processing Contract ID [" + contract.getId() + "] - [" + contract.getJobNumber() + " - " + contract.getName() +  "] for Customer ID [" + contract.getCustomerId() + "]");
	    				generatePricingSheetForContract(contract.getId());
	    				count++;
	    			} catch (ServiceException se) {
	    				se.printStackTrace();
	    			} catch (Exception e) {
	    				e.printStackTrace();
	    			}
	    		}
	            
	            log.info("Ending Task: " + st.getName());
	        } else {
	        	log.info("Scheduled Task for Pricing Sheets is not enabled.");
	        }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void generatePricingSheetForContract(Long contractId) throws ServiceException {
		PricingSheet pricingSheet = findPricingSheetForContract(contractId);
		
		if(pricingSheet == null) {
			//throw new ServiceException("No Pricing Sheet Exists for this Contract.");
			log.warn("No Pricing Sheet Exists for this Contract ID [" + contractId + "]");
			return;
		}
		
		if(pricingSheet.getActive()) {
			List<Service> services = contractRevenueService.services(contractId, null);
			
			Map<Long, List<Service>> groupedServices = new HashMap<Long, List<Service>>();
			
			for(Service service : services) {
				//skip any services that have already ended (for now)
				if(service.getEndDate().before(new Date())) {
					//log.info("Skipping Service [" + service.getDeviceDescription() + "] because it ended [" + service.getEndDate() + "]");
					continue;
				}
				
				Long deviceId = service.getDeviceId();
				Device device = applicationDataDaoService.device(deviceId);
				if(device.getPricingSheetEnabled() && !Device.DeviceType.M365.equals(device.getDeviceType()) && !Device.DeviceType.cspO365.equals(device.getDeviceType()) && !Device.DeviceType.M365NC.equals(device.getDeviceType())) {
					List<Service> existingServices = groupedServices.get(deviceId);
					if(existingServices == null) {
						List<Service> newServices = new ArrayList<Service>();
						newServices.add(service);
						groupedServices.put(deviceId, newServices);
						log.info("Added new service list for Device: [" + service.getDeviceDescription() + "]");
					} else {
						List<Service> updatedServicesList = new ArrayList<Service>(); 
						for(Service existingService : existingServices) {
							if(!existingService.getOnetimeUnitPrice().equals(service.getOnetimeUnitPrice()) || !existingService.getRecurringUnitPrice().equals(service.getRecurringUnitPrice())) {
                                                                // note: this log statement generates a HUGE amount of logging
								//log.info("Prices DON'T match for Service : [" + existingService.getId() + ", " + existingService.getOnetimeUnitPrice() + ", " + existingService.getRecurringUnitPrice() + "] and Service : [" + service.getId() + ", " + service.getOnetimeUnitPrice() + ", " + service.getRecurringUnitPrice() + "]");
								boolean contains = false;
								for(Service updatedService : updatedServicesList) {
									if(updatedService.getId().equals(service.getId())) {
										contains = true;
										break;
									}
								}
								
								if(!contains) {
									updatedServicesList.add(service);
								}
							} else {
                                                                // note: this log statement generates a HUGE amount of logging
								//log.info("Prices DO match for Service : [" + existingService.getId() + ", " + existingService.getOnetimeUnitPrice() + ", " + existingService.getRecurringUnitPrice() + "] and Service : [" + service.getId() + ", " + service.getOnetimeUnitPrice() + ", " + service.getRecurringUnitPrice() + "]");
								continue;
							}
						}
						updatedServicesList.addAll(existingServices);
						groupedServices.put(deviceId, updatedServicesList);	
					}
				} else {
					//log.info("Skipping Service [" + service.getDeviceDescription() + "] because it is not enabled for Pricing.");
				}
			}
			
			List<PricingSheetProduct> productsToUpdate = new ArrayList<PricingSheetProduct>();
			Iterator it = groupedServices.entrySet().iterator();
			while(it.hasNext()) {
				Map.Entry<Long, List<Service>> pair = (Map.Entry<Long, List<Service>>) it.next();
				List<Service> productsToCreate = pair.getValue();
				if(productsToCreate.size() == 1) {
					Service okProduct = productsToCreate.get(0);
					//create product
					
					PricingSheetProduct pricingSheetProduct = new PricingSheetProduct();
					pricingSheetProduct.setDeviceId(okProduct.getDeviceId());
					pricingSheetProduct.setOnetimePrice(okProduct.getOnetimeUnitPrice());
					pricingSheetProduct.setRecurringPrice(okProduct.getRecurringUnitPrice());
					pricingSheetProduct.setRemovalPrice(new BigDecimal(0));
					pricingSheetProduct.setPricingSheetId(pricingSheet.getId());
					pricingSheetProduct.setStatus(PricingSheetProduct.Status.active);
					productsToUpdate.add(pricingSheetProduct);
					log.info("Saving Pricing Sheet Product! [" + okProduct.getDeviceDescription() + "]");
				} else {
					Service errorProduct = productsToCreate.get(0);
					log.info("Pricing Mis-match in Product: [" + errorProduct.getDeviceDescription() + "]");
					
					PricingSheetProduct pricingSheetProduct = new PricingSheetProduct();
					pricingSheetProduct.setDeviceId(errorProduct.getDeviceId());
					pricingSheetProduct.setOnetimePrice(new BigDecimal(0));
					pricingSheetProduct.setRecurringPrice(new BigDecimal(0));
					pricingSheetProduct.setRemovalPrice(new BigDecimal(0));
					pricingSheetProduct.setPricingSheetId(pricingSheet.getId());
					pricingSheetProduct.setStatus(PricingSheetProduct.Status.error);
					StringBuffer errorMessage = new StringBuffer();
					errorMessage.append("Multiple products with the same part number have different prices this month. They are as follows: ");
					for(Service productToCreate : productsToCreate) {
						errorMessage.append("<br/>");
						errorMessage.append("<b>CI Name: ").append(productToCreate.getDeviceName()).append("</b>");
						errorMessage.append("<b>NRC: $").append(productToCreate.getFormattedOnetimeRevenue()).append("</b>");
						errorMessage.append("<b>MRC: $").append(productToCreate.getFormattedRecurringRevenue()).append("</b>");
					}
					pricingSheetProduct.setStatusMessage(errorMessage.toString());
					log.info("Status: " + errorMessage.toString());
					productsToUpdate.add(pricingSheetProduct);
					
				}
				
				it.remove();
			}
			
			for(PricingSheetProduct productToUpdate : productsToUpdate) {
				boolean needsUpdate = false;
				for(PricingSheetProduct existingProduct : pricingSheet.getProducts()) {
					if(existingProduct.getDeviceId().equals(productToUpdate.getDeviceId())) {
						productToUpdate.setId(existingProduct.getId());
						
						//see if anything has changed to update the product
						if(!existingProduct.getOnetimePrice().equals(productToUpdate.getOnetimePrice()) || !existingProduct.getRecurringPrice().equals(productToUpdate.getRecurringPrice()) || !existingProduct.getStatus().equals(productToUpdate.getStatus()) || (existingProduct.getStatusMessage() != null && !existingProduct.getStatusMessage().equals(productToUpdate.getStatusMessage()))) {
							needsUpdate = true;
						}
						
						//if the manual override is set, we don't want to update
						if(existingProduct.getManualOverride()) {
							needsUpdate = false;
						}
						
						break;
					}
				}
				
				if(productToUpdate.getId() == null) {
					savePricingSheetProduct(productToUpdate);
				} else if(needsUpdate) {
					updatePricingSheetProduct(productToUpdate);
				}
			}
			
			//delete any that shouldn't be in there
			for(PricingSheetProduct existingProduct : pricingSheet.getProducts()) {
				Device device = applicationDataDaoService.device(existingProduct.getDeviceId());
				
				if(!device.getPricingSheetEnabled() && !existingProduct.getManualOverride() && !Device.DeviceType.M365.equals(device.getDeviceType())) {
					log.info("Deleted Pricing Sheet Product with ID [" + existingProduct.getId() + "] as Device ID [" + existingProduct.getDeviceId() + "] is no longer enabled for Pricing Sheets.");
					deletePricingSheetProduct(existingProduct.getId());
				}
			}
		}
		
	}
	
	@Override
	public PricingSheet findPricingSheetForContract(Long contractId) throws ServiceException {
        String query = BASE_PRICING_SHEET_QUERY
                + " where ps.contract_id = ?";
        return jdbcTemplate.queryForObject(query, new Object[]{contractId},
                new RowMapper<PricingSheet>() {
            @Override
            public PricingSheet mapRow(ResultSet rs, int i) throws SQLException {
            	PricingSheet pricingSheet = new PricingSheet(
                        rs.getLong("id"),
                        rs.getLong("contract_id"),
                        rs.getBoolean("active"));
            	pricingSheet.setCreated(rs.getTimestamp("ps_created"));
            	pricingSheet.setCreatedBy(rs.getString("ps_created_by"));
            	pricingSheet.setUpdated(rs.getTimestamp("ps_updated"));
            	pricingSheet.setUpdatedBy(rs.getString("ps_updated_by"));
            	
        		pricingSheet.setProducts(jdbcTemplate.query(BASE_PRICING_SHEET_PRODUCT_QUERY
                        + " where psp.pricing_sheet_id = ?"
                        + " order by psp.created desc", new Object[]{pricingSheet.getId()}, new RowMapper<PricingSheetProduct>() {
                    @Override
                    public PricingSheetProduct mapRow(ResultSet rs, int i) throws SQLException {
                        return new PricingSheetProduct(
                                rs.getLong("id"),
                                rs.getLong("pricing_sheet_id"),
                                rs.getLong("device_id"),
                                rs.getString("device_part_number"),
                                rs.getString("device_description"),
                                (rs.getString("device_type") == null) ? null : Device.DeviceType.valueOf(rs.getString("device_type")),
                                rs.getString("device_alt_id"),
                                rs.getLong("service_id"),
                                rs.getString("service_name"),
                                rs.getBigDecimal("onetime_revenue"),
                                rs.getBigDecimal("recurring_revenue"),
                                rs.getBigDecimal("removal_revenue"), 
                                rs.getBigDecimal("erp_price"),
                                rs.getBigDecimal("discount"),
                                rs.getDate("subscription_start_date"),
                                rs.getDate("subscription_end_date"),
                                rs.getInt("unit_count"),
                                rs.getInt("previous_unit_count"),
                                (rs.getString("status") == null) ? null : PricingSheetProduct.Status.valueOf(rs.getString("status")),
                                rs.getString("status_message"),
                                rs.getBoolean("manual_override"));
                    }
                }));
            	
                return pricingSheet;
            }
        });
	}
	
	@Override
	public PricingSheet findPricingSheetForContractWithActiveProducts(Long contractId) throws ServiceException {
		PricingSheet pricingSheet = findPricingSheetForContract(contractId);
		List<PricingSheetProduct> activeProducts = new ArrayList<PricingSheetProduct>();
		for(PricingSheetProduct product : pricingSheet.getProducts()) {
			if(PricingSheetProduct.Status.active.equals(product.getStatus())) {
				activeProducts.add(product);
			}
		}
		pricingSheet.setProducts(activeProducts);	
		return pricingSheet;
	}
	
	@Override
    public Long savePricingSheet(PricingSheet pricingSheet) throws ServiceException {
		if(pricingSheet.getContractId() == null) {
			throw new ServiceException(messageSource.getMessage("validation_error_pricing_sheet_contract", null, LocaleContextHolder.getLocale()));
		}
        try {
            SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
            jdbcInsert.withTableName("pricing_sheet").usingGeneratedKeyColumns("id");
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("contract_id", pricingSheet.getContractId());
            params.put("active", pricingSheet.getActive());
            params.put("created_by", authenticatedUser());
            params.put("created", new Date());
            Number pk = jdbcInsert.executeAndReturnKey(new MapSqlParameterSource(params));
            return (Long) pk;
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_pricing_sheet_insert", new Object[]{any.getMessage()}, LocaleContextHolder.getLocale()));
        }
    }
	
	@Override
    public void updatePricingSheet(PricingSheet pricingSheet) throws ServiceException {
		Long pricingSheetId = pricingSheet.getId();
		if(pricingSheetId == null) {
			throw new ServiceException(messageSource.getMessage("validation_error_pricing_sheet_not_found", new Object[]{pricingSheetId}, LocaleContextHolder.getLocale()));
		}
		
		Integer count = jdbcTemplate.queryForObject("select count(*) from pricing_sheet where id = ?", Integer.class, pricingSheetId);
        if (!count.equals(1)) {
            throw new ServiceException(messageSource.getMessage("validation_error_pricing_sheet_not_found", new Object[]{pricingSheetId}, LocaleContextHolder.getLocale()));
        }
        
		if(pricingSheet.getContractId() == null) {
			throw new ServiceException(messageSource.getMessage("validation_error_pricing_sheet_contract", null, LocaleContextHolder.getLocale()));
		}
        
        try {
            int updated = jdbcTemplate.update("update pricing_sheet set contract_id = ?, active = ?, updated = ?, updated_by = ?"
                    + " where id = ?",
                    new Object[]{pricingSheet.getContractId(), pricingSheet.getActive(), new DateTime().withZone(DateTimeZone.forID(TZID)).toDate(), authenticatedUser(),
                    		pricingSheetId});
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_pricing_sheet_update", new Object[]{pricingSheet.getId(), any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    }
	
	@Override
    public void deletePricingSheet(Long pricingSheetId) throws ServiceException {
		if(pricingSheetId == null) {
			throw new ServiceException(messageSource.getMessage("validation_error_pricing_sheet_not_found", new Object[]{pricingSheetId}, LocaleContextHolder.getLocale()));
		}
		
		Integer count = jdbcTemplate.queryForObject("select count(*) from pricing_sheet where id = ?", Integer.class, pricingSheetId);
        if (!count.equals(1)) {
            throw new ServiceException(messageSource.getMessage("validation_error_pricing_sheet_not_found", new Object[]{pricingSheetId}, LocaleContextHolder.getLocale()));
        }
        
        try {
        	int deleted = jdbcTemplate.update("delete from pricing_sheet where id = ?", pricingSheetId);
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_pricing_sheet_delete", new Object[]{pricingSheetId, any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    }
	
	@Override
	public PricingSheetProduct pricingSheetProduct(Long id) throws ServiceException {
		String query = BASE_PRICING_SHEET_PRODUCT_QUERY
                + " where psp.id = ?";
        return jdbcTemplate.queryForObject(query, new Object[]{id},
                new RowMapper<PricingSheetProduct>() {
            @Override
            public PricingSheetProduct mapRow(ResultSet rs, int i) throws SQLException {
            	PricingSheetProduct pricingSheetProduct = new PricingSheetProduct(
                        rs.getLong("id"),
                        rs.getLong("pricing_sheet_id"),
                        rs.getLong("device_id"),
                        rs.getString("device_part_number"),
                        rs.getString("device_description"),
                        (rs.getString("device_type") == null) ? null : Device.DeviceType.valueOf(rs.getString("device_type")),
                        rs.getString("device_alt_id"),
                        rs.getLong("service_id"),
                        rs.getString("service_name"),
                        rs.getBigDecimal("onetime_revenue"),
                        rs.getBigDecimal("recurring_revenue"),
                        rs.getBigDecimal("removal_revenue"), 
                        rs.getBigDecimal("erp_price"),
                        rs.getBigDecimal("discount"),
                        rs.getDate("subscription_start_date"),
                        rs.getDate("subscription_end_date"),
                        rs.getInt("unit_count"),
                        rs.getInt("previous_unit_count"),
                        (rs.getString("status") == null) ? null : PricingSheetProduct.Status.valueOf(rs.getString("status")),
                        rs.getString("status_message"),
                        rs.getBoolean("manual_override"));
            	pricingSheetProduct.setCreated(rs.getTimestamp("psp_created"));
            	pricingSheetProduct.setCreatedBy(rs.getString("psp_created_by"));
            	pricingSheetProduct.setUpdated(rs.getTimestamp("psp_updated"));
            	pricingSheetProduct.setUpdatedBy(rs.getString("psp_updated_by"));
            	
                return pricingSheetProduct;
            }
        });
	}
	
	private PricingSheetProduct pricingSheetProductByDeviceInPricingSheet(Long pricingSheetId, Long deviceId) throws ServiceException {
		String query = BASE_PRICING_SHEET_PRODUCT_QUERY
                + " where psp.pricing_sheet_id = ? and psp.device_id = ?";
        return jdbcTemplate.queryForObject(query, new Object[]{pricingSheetId, deviceId},
                new RowMapper<PricingSheetProduct>() {
            @Override
            public PricingSheetProduct mapRow(ResultSet rs, int i) throws SQLException {
            	PricingSheetProduct pricingSheetProduct = new PricingSheetProduct(
                        rs.getLong("id"),
                        rs.getLong("pricing_sheet_id"),
                        rs.getLong("device_id"),
                        rs.getString("device_part_number"),
                        rs.getString("device_description"),
                        (rs.getString("device_type") == null) ? null : Device.DeviceType.valueOf(rs.getString("device_type")),
                        rs.getString("device_alt_id"),
                        rs.getLong("service_id"),
                        rs.getString("service_name"),
                        rs.getBigDecimal("onetime_revenue"),
                        rs.getBigDecimal("recurring_revenue"),
                        rs.getBigDecimal("removal_revenue"), 
                        rs.getBigDecimal("erp_price"),
                        rs.getBigDecimal("discount"),
                        rs.getDate("subscription_start_date"),
                        rs.getDate("subscription_end_date"),
                        rs.getInt("unit_count"),
                        rs.getInt("previous_unit_count"),
                        (rs.getString("status") == null) ? null : PricingSheetProduct.Status.valueOf(rs.getString("status")),
                        rs.getString("status_message"),
                        rs.getBoolean("manual_override"));
            	pricingSheetProduct.setCreated(rs.getTimestamp("psp_created"));
            	pricingSheetProduct.setCreatedBy(rs.getString("psp_created_by"));
            	pricingSheetProduct.setUpdated(rs.getTimestamp("psp_updated"));
            	pricingSheetProduct.setUpdatedBy(rs.getString("psp_updated_by"));
            	
                return pricingSheetProduct;
            }
        });
	}
	
	@Override
	public PricingSheetProduct pricingSheetProductByDeviceAndContractId(Long contractId, Long deviceId) throws ServiceException {
		String query = BASE_PRICING_SHEET_PRODUCT_WITH_CONTRACT_QUERY
                + " where co.id = ? and psp.device_id = ?";
        List<PricingSheetProduct> results = jdbcTemplate.query(query, new Object[]{contractId, deviceId},
        		new RowMapper<PricingSheetProduct>() {
            @Override
            public PricingSheetProduct mapRow(ResultSet rs, int i) throws SQLException {
                return new PricingSheetProduct(
                        rs.getLong("id"),
                        rs.getLong("pricing_sheet_id"),
                        rs.getLong("device_id"),
                        rs.getString("device_part_number"),
                        rs.getString("device_description"),
                        (rs.getString("device_type") == null) ? null : Device.DeviceType.valueOf(rs.getString("device_type")),
                        rs.getString("device_alt_id"),
                        rs.getLong("service_id"),
                        rs.getString("service_name"),
                        rs.getBigDecimal("onetime_revenue"),
                        rs.getBigDecimal("recurring_revenue"),
                        rs.getBigDecimal("removal_revenue"), 
                        rs.getBigDecimal("erp_price"),
                        rs.getBigDecimal("discount"),
                        rs.getDate("subscription_start_date"),
                        rs.getDate("subscription_end_date"),
                        rs.getInt("unit_count"),
                        rs.getInt("previous_unit_count"),
                        (rs.getString("status") == null) ? null : PricingSheetProduct.Status.valueOf(rs.getString("status")),
                        rs.getString("status_message"),
                        rs.getBoolean("manual_override"));
            }
        });
        
        if(results != null && results.size() > 0) {
        	return results.get(0);
        } else {
        	return null;
        }
	}
	
	@Override
    public Long savePricingSheetProduct(PricingSheetProduct pricingSheetProduct) throws ServiceException {
		validatePricingSheetProduct(pricingSheetProduct);
		
        try {
            SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
            jdbcInsert.withTableName("pricing_sheet_product").usingGeneratedKeyColumns("id");
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("pricing_sheet_id", pricingSheetProduct.getPricingSheetId());
            params.put("device_id", pricingSheetProduct.getDeviceId());
            params.put("onetime_revenue", pricingSheetProduct.getOnetimePrice());
            params.put("recurring_revenue", pricingSheetProduct.getRecurringPrice());
            params.put("removal_revenue", pricingSheetProduct.getRemovalPrice());
            params.put("erp_price", pricingSheetProduct.getErpPrice());
            params.put("discount", pricingSheetProduct.getDiscount());
            params.put("status", pricingSheetProduct.getStatus().name());
            params.put("status_message", pricingSheetProduct.getStatusMessage());
            params.put("unit_count", pricingSheetProduct.getUnitCount());
            params.put("previous_unit_count", pricingSheetProduct.getPreviousUnitCount());
            params.put("manual_override", pricingSheetProduct.getManualOverride());
            params.put("created_by", authenticatedUser());
            params.put("created", new Date());
            
            Long serviceId = getServiceIdForDevice(pricingSheetProduct.getDeviceId());
            params.put("service_id", serviceId);
            
            Number pk = jdbcInsert.executeAndReturnKey(new MapSqlParameterSource(params));
            return (Long) pk;
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_pricing_sheet_product_insert", new Object[]{any.getMessage()}, LocaleContextHolder.getLocale()));
        }
    }
	
	@Override
    public void updatePricingSheetProduct(PricingSheetProduct pricingSheetProduct) throws ServiceException {
		validatePricingSheetProduct(pricingSheetProduct);
		
		Long serviceId = getServiceIdForDevice(pricingSheetProduct.getDeviceId());
		
		Long pricingSheetProductId = pricingSheetProduct.getId();
		if(pricingSheetProductId == null) {
			throw new ServiceException(messageSource.getMessage("validation_error_pricing_sheet_product_not_found", null, LocaleContextHolder.getLocale()));
		}
		Integer count = jdbcTemplate.queryForObject("select count(*) from pricing_sheet_product where id = ?", Integer.class, pricingSheetProductId);
        if (!count.equals(1)) {
            throw new ServiceException(messageSource.getMessage("validation_error_pricing_sheet_product_not_found", new Object[]{pricingSheetProductId}, LocaleContextHolder.getLocale()));
        }
		
		try {
            int updated = jdbcTemplate.update("update pricing_sheet_product set pricing_sheet_id = ?, device_id = ?, service_id = ?, onetime_revenue = ?, recurring_revenue = ?, removal_revenue = ?,"
            		+ " status = ?, status_message = ?, manual_override = ?, erp_price = ?, discount = ?, subscription_start_date = ?, subscription_end_date = ?, unit_count = ?, previous_unit_count = ?, "
            		+ " updated = ?, updated_by = ?"
                    + " where id = ?",
                    new Object[]{pricingSheetProduct.getPricingSheetId(), pricingSheetProduct.getDeviceId(), serviceId, pricingSheetProduct.getOnetimePrice(), 
            				pricingSheetProduct.getRecurringPrice(), pricingSheetProduct.getRemovalPrice(), pricingSheetProduct.getStatus().name(), pricingSheetProduct.getStatusMessage(), pricingSheetProduct.getManualOverride(),
            				pricingSheetProduct.getErpPrice(), pricingSheetProduct.getDiscount(), pricingSheetProduct.getSubscriptionStartDate(), pricingSheetProduct.getSubscriptionEndDate(), pricingSheetProduct.getUnitCount(), pricingSheetProduct.getPreviousUnitCount(),
            				new DateTime().withZone(DateTimeZone.forID(TZID)).toDate(), authenticatedUser(), pricingSheetProductId});
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_pricing_sheet_product_update", new Object[]{pricingSheetProduct.getId(), any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    }
	
	//these are the things we update from the Microsoft bill or a Service Activation PCR, so we separate out this method
	@Override
    public void updatePricingSheetProductBits(PricingSheetProduct pricingSheetProduct) throws ServiceException {
		Long pricingSheetProductId = pricingSheetProduct.getId();
		if(pricingSheetProductId == null) {
			throw new ServiceException(messageSource.getMessage("validation_error_pricing_sheet_product_not_found", null, LocaleContextHolder.getLocale()));
		}
		Integer count = jdbcTemplate.queryForObject("select count(*) from pricing_sheet_product where id = ?", Integer.class, pricingSheetProductId);
        if (!count.equals(1)) {
            throw new ServiceException(messageSource.getMessage("validation_error_pricing_sheet_product_not_found", new Object[]{pricingSheetProductId}, LocaleContextHolder.getLocale()));
        }
		
		try {
            int updated = jdbcTemplate.update("update pricing_sheet_product set subscription_start_date = ?, subscription_end_date = ?, unit_count = ?, previous_unit_count = ?, "
            		+ " updated = ?, updated_by = ?"
                    + " where id = ?",
                    new Object[]{pricingSheetProduct.getSubscriptionStartDate(), pricingSheetProduct.getSubscriptionEndDate(), pricingSheetProduct.getUnitCount(), pricingSheetProduct.getPreviousUnitCount(), 
            				new DateTime().withZone(DateTimeZone.forID(TZID)).toDate(), authenticatedUser(), pricingSheetProductId});
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_pricing_sheet_product_update", new Object[]{pricingSheetProduct.getId(), any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    }
	
	private Long getServiceIdForDevice(Long deviceId) {
		Long serviceId = null;
		try {
			Device device = applicationDataDaoService.device(deviceId);
			
			if(device != null && device.getDefaultOspId() != null) {
				com.logicalis.serviceinsight.data.Service serviceOffering = applicationDataDaoService.findActiveServiceByOspId(device.getDefaultOspId());
		        if(serviceOffering != null) {
		        	serviceId = serviceOffering.getServiceId();
		        } else {
		        	serviceOffering = applicationDataDaoService.findActiveServiceByOspId(OSP_SERVICE_ID_UNDEFINED);
			        if(serviceOffering != null) {
			        	serviceId = serviceOffering.getServiceId();
			        }
		        }
			} else {
				com.logicalis.serviceinsight.data.Service serviceOffering = applicationDataDaoService.findActiveServiceByOspId(OSP_SERVICE_ID_UNDEFINED);
		        if(serviceOffering != null) {
		        	serviceId = serviceOffering.getServiceId();
		        }
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return serviceId;
	}
	
	private void validatePricingSheetProduct(PricingSheetProduct pricingSheetProduct) throws ServiceException {
		Long pricingSheetId = pricingSheetProduct.getPricingSheetId();
		if(pricingSheetId == null) {
			throw new ServiceException(messageSource.getMessage("validation_error_pricing_sheet_product_pricing_sheet", new Object[]{pricingSheetId}, LocaleContextHolder.getLocale()));
		}
		Integer pricingSheetCount = jdbcTemplate.queryForObject("select count(*) from pricing_sheet where id = ?", Integer.class, pricingSheetId);
        if (!pricingSheetCount.equals(1)) {
            throw new ServiceException(messageSource.getMessage("validation_error_pricing_sheet_not_found", new Object[]{pricingSheetId}, LocaleContextHolder.getLocale()));
        }
        
        Long deviceId = pricingSheetProduct.getDeviceId();
        if(deviceId == null) {
			throw new ServiceException(messageSource.getMessage("validation_error_pricing_sheet_product_device", null, LocaleContextHolder.getLocale()));
		}
        Integer deviceCount = jdbcTemplate.queryForObject("select count(*) from device where id = ?", Integer.class, deviceId);
        if (!deviceCount.equals(1)) {
            throw new ServiceException(messageSource.getMessage("validation_error_pricing_sheet_product_device_not_found", new Object[]{deviceId}, LocaleContextHolder.getLocale()));
        }
        
        /*
        Long serviceId = pricingSheetProduct.getServiceId();
        if(serviceId == null) {
			throw new ServiceException(messageSource.getMessage("validation_error_pricing_sheet_product_service", null, LocaleContextHolder.getLocale()));
		}
        Integer serviceCount = jdbcTemplate.queryForObject("select count(*) from service where id = ?", Integer.class, serviceId);
        if (!serviceCount.equals(1)) {
            throw new ServiceException(messageSource.getMessage("validation_error_pricing_sheet_product_service_not_found", new Object[]{serviceId}, LocaleContextHolder.getLocale()));
        }*/
	}
	
	@Override
    public void deletePricingSheetProduct(Long pricingSheetProductId) throws ServiceException {
		if(pricingSheetProductId == null) {
			throw new ServiceException(messageSource.getMessage("validation_error_pricing_sheet_not_found", new Object[]{pricingSheetProductId}, LocaleContextHolder.getLocale()));
		}
		
		Integer count = jdbcTemplate.queryForObject("select count(*) from pricing_sheet_product where id = ?", Integer.class, pricingSheetProductId);
        if (!count.equals(1)) {
            throw new ServiceException(messageSource.getMessage("validation_error_pricing_sheet_product_not_found", new Object[]{pricingSheetProductId}, LocaleContextHolder.getLocale()));
        }
        
        try {
        	int deleted = jdbcTemplate.update("delete from pricing_sheet_product where id = ?", pricingSheetProductId);
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_pricing_sheet_product_delete", new Object[]{pricingSheetProductId, any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    }
	
}
