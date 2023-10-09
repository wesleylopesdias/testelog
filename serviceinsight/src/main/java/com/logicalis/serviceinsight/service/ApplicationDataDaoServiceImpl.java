package com.logicalis.serviceinsight.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logicalis.pcc.util.DecimalMap;
import com.logicalis.serviceinsight.dao.DeviceExpenseCategory;
import com.logicalis.serviceinsight.dao.ExpenseCategory;
import com.logicalis.serviceinsight.dao.Location;
import com.logicalis.serviceinsight.dao.SPLACost;
import com.logicalis.serviceinsight.data.SubscriptionUplift;
import com.logicalis.serviceinsight.data.User;
import com.logicalis.serviceinsight.web.config.CloudBillingSecurityConfig.Role;
import com.logicalis.serviceinsight.data.Device;
import com.logicalis.serviceinsight.data.Device.ParentDevice;
import com.logicalis.serviceinsight.data.DeviceProperty;
import com.logicalis.serviceinsight.data.DeviceRelationship;
import com.logicalis.serviceinsight.data.Personnel;
import com.logicalis.serviceinsight.data.ScheduledTask;
import com.logicalis.serviceinsight.data.Service;
import com.logicalis.serviceinsight.data.ServiceAlign;
import com.microsoft.partnercenter.api.schema.datamodel.AzureMeter;
import com.microsoft.partnercenter.api.schema.datamodel.AzureOfferTerm;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.SerializationUtils;
import org.springframework.util.StopWatch;

/**
 *
 * @author poneil
 */
@org.springframework.stereotype.Service
public class ApplicationDataDaoServiceImpl extends BaseServiceImpl implements ApplicationDataDaoService {

    static final DateTimeFormatter azureZuluDateTimeFormat = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
    static final DateTimeFormatter azureOfferTermDateTimeFormat = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss");
    static final DateTimeFormatter azureUsageDateTimeFormat = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ");
    static final DateTime mysqlBeginTime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").parseDateTime("1000-01-01 00:00:00").withZone(DateTimeZone.UTC);
    
    @Lazy
    @Autowired
    ContractDaoService contractDaoService;
    @Lazy
    @Autowired
    CostDaoService costDaoService;
    @Lazy
    @Autowired
    UserDaoService userDaoService;
    
    @Value("${admin.process.password}")
    private String adminProcessPassword;
    
    @Override
    @Transactional
    public Long saveDevice(Device device) throws ServiceException {
        if (device.getId() != null) {
            updateDevice(device);
            return device.getId();
        }
        if (device.getDescription() == null && device.getPartNumber() == null) {
            throw new ServiceException(messageSource.getMessage("validation_error_device_part_number_or_description", null, LocaleContextHolder.getLocale()));
        }
        if (device.getPartNumber() != null) {
            Integer count = jdbcTemplate.queryForObject("select count(*) from device where part_number = ?", Integer.class, device.getPartNumber());
            /*if (!count.equals(0)) {
             throw new ServiceException(messageSource.getMessage("device_duplicate_for_part_number", new Object[]{device.getPartNumber()}, LocaleContextHolder.getLocale()));
             }*/
        }
        
        if(device.getCostAllocationOption() && (device.getCostMappings() != null && device.getCostMappings().size() > 1)) {
        	throw new ServiceException(messageSource.getMessage("validation_error_device_cost_allocation_mappings_limit", null, LocaleContextHolder.getLocale()));
        }
        
        try {
            SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
            jdbcInsert.withTableName("device").usingGeneratedKeyColumns("id");
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("alt_id", device.getAltId());
            if (device.getPartNumber() != null) {
                params.put("part_number", device.getPartNumber());
            }
            if (device.getDescription() != null) {
                params.put("description", device.getDescription());
            }
            
            String deviceType = null;
            if(device.getDeviceType() != null) deviceType = device.getDeviceType().name();
            
            if(device.getCostAllocationOption()) {
            	Integer allocationCategoryCount = 0;
            	if(device.getCostMappings() != null) {
            		for(DeviceExpenseCategory mapping: device.getCostMappings()) {
            			if(mapping.getAllocationCategory()) {
            				allocationCategoryCount++;
            			}
            		}
            	}
            	
            	if(allocationCategoryCount == 0) {
            		throw new ServiceException(messageSource.getMessage("validation_error_device_cost_allocation_category_required", null, LocaleContextHolder.getLocale()));
            	} else if(allocationCategoryCount > 1) {
            		throw new ServiceException(messageSource.getMessage("validation_error_device_cost_allocation_mappings_limit", null, LocaleContextHolder.getLocale()));
            	}
            }
            
            params.put("device_type", deviceType);
            if (device.getUnits() != null) {
                params.put("units", device.getUnits());
            }
            params.put("archived", device.getArchived());
            params.put("activate_sync_enabled", device.getActivateSyncEnabled());
            params.put("activate_add_business_service", device.getActivateAddBusinessService());
            params.put("is_ci", device.getIsCI());
            params.put("pricing_sheet_enabled", device.getPricingSheetEnabled());
            params.put("default_osp_id", device.getDefaultOspId());
            params.put("product_id", device.getProductId());
            params.put("require_unit_count", device.getRequireUnitCount());
            params.put("cost_allocation_option", device.getCostAllocationOption());
            params.put("pricing_sync_enabled", device.getPricingSyncEnabled());
            params.put("catalog_recurring_cost", device.getCatalogRecurringCost());
            params.put("catalog_recurring_price", device.getCatalogRecurringPrice());
            
            if(device.getTermDuration() != null) params.put("term_duration", device.getTermDuration().name());
            if(device.getBillingPlan() != null) params.put("billing_plan", device.getBillingPlan().name());
            if(device.getSegment() != null) params.put("segment", device.getSegment().name());
            
            params.put("created_by", authenticatedUser());
            Number pk = jdbcInsert.executeAndReturnKey(new MapSqlParameterSource(params));
            Long deviceId = pk.longValue();
            
            if(device.getSplaCosts() != null && device.getSplaCosts().size() > 0) {
            	generateDeviceSPLACosts(device, pk.longValue());
            }
            
            for(DeviceExpenseCategory mapping: device.getCostMappings()) {
    			mapping.setDeviceId(deviceId);
    		}
            
            updateDeviceRelationships(deviceId, device.getRelatedDevices());
            costDaoService.saveOrUpdateDeviceCostMappings(deviceId, device.getCostMappings());
            updateDeviceProperties(deviceId, device.getProperties());
            
            return (Long) pk;
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_device_insert", new Object[]{any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    }
    
    /**
     * Save or Update related devices to a device parent. Replaces existing relationships with whatever
     * is found in relatedDevices list!
     * 
     * @param deviceId device id (parent) to relate to other devices (children)
     * @param relatedDevices barebones info needed: child deviceId and "relationship"
     */
    private void updateDeviceRelationships(Long deviceId, List<Device> relatedDevices) throws ServiceException {
        
        String query = "delete from device_relationship where device_id = ?";
        try {
            jdbcTemplate.update(query, deviceId);
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_device_delete", new Object[]{deviceId, any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
        
        if (relatedDevices == null || relatedDevices.isEmpty()) {
            return;
        }
        List<Object[]> inserts = new ArrayList<Object[]>();
        String update = "insert into device_relationship (device_id, related_device_id, relationship, spec_units, sorder) values (?, ?, ?, ?, ?)";
        List<Long> relatedDeviceIds = new ArrayList<Long>();
        for (Device device : relatedDevices) {
            // validation
            if (device.getId() == null) continue;
            if (deviceId.equals(device.getId())) continue;
            //if (relatedDeviceIds.contains(device.getId())) continue;
            if (device.getSpecUnits() == null || device.getSpecUnits() <= 0) device.setSpecUnits(0);
            if (device.getOrder() == null || device.getOrder() <= 0) device.setOrder(0);
            if (device.getRelationship() == null) continue;
            // end validation
            
            relatedDeviceIds.add(device.getId());
            inserts.add(new Object[]{deviceId, device.getId(),
                device.getRelationship().name(),
                device.getSpecUnits(), device.getOrder()});
        }
        jdbcTemplate.batchUpdate(update, inserts);
    }
    
    /**
     * Save or Update device properties to a device parent. Replaces existing properties with whatever
     * is found in properties list!
     * 
     * @param deviceId device id (parent) to relate to other devices (children)
     * @param properties
     */
    private void updateDeviceProperties(Long deviceId, List<DeviceProperty> properties) throws ServiceException {
        
        String query = "delete from device_property where device_id = ?";
        try {
            jdbcTemplate.update(query, deviceId);
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_device_delete", new Object[]{deviceId, any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
        
        if (properties == null || properties.isEmpty()) {
            return;
        }
        for (DeviceProperty property : properties) {
            // validation
            if(property.getType() == null) {
            	throw new ServiceException(messageSource.getMessage("validation_error_device_property_type", null, LocaleContextHolder.getLocale()));
            }
            
            if(DeviceProperty.Type.DataType.string.equals(property.getType().getDataType())) {
            	if(StringUtils.isEmpty(property.getStrValue())) {
            		throw new ServiceException(messageSource.getMessage("validation_error_device_property_string", null, LocaleContextHolder.getLocale()));
            	}
            }
            
            if(DeviceProperty.Type.DataType.integer.equals(property.getType().getDataType())) {
            	if(property.getUnitCount() == null || property.getUnitCount() == 0) {
            		throw new ServiceException(messageSource.getMessage("validation_error_device_property_unit_count", null, LocaleContextHolder.getLocale()));
            	}
            }
            
            // end validation
            
            SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
            jdbcInsert.withTableName("device_property").usingGeneratedKeyColumns("id");
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("device_id", deviceId);
            params.put("type", property.getType().name());
            if(property.getUnitCount() != null && property.getUnitCount() > 0) {
                params.put("unit_count", property.getUnitCount());
            }
            if(!StringUtils.isEmpty(property.getStrValue())) {
                params.put("str_value", property.getStrValue());
            }
            if(!StringUtils.isEmpty(property.getUnitType())) {
                params.put("unit_type", property.getUnitType());
            }
            
            params.put("created_by", authenticatedUser());
            Number pk = jdbcInsert.executeAndReturnKey(new MapSqlParameterSource(params));
        }
    }

    @Override
    @Transactional
    public void updateDevice(Device device) throws ServiceException {
        if (device.getId() == null) {
            throw new ServiceException(messageSource.getMessage("validation_error_device_id", null, LocaleContextHolder.getLocale()));
        }
        Integer count = jdbcTemplate.queryForObject("select count(*) from device where id = ?", Integer.class, device.getId());
        if (!count.equals(1)) {
            throw new ServiceException(messageSource.getMessage("device_not_found_for_id", new Object[]{device.getId()}, LocaleContextHolder.getLocale()));
        }
        if (device.getDescription() == null && device.getPartNumber() == null) {
            throw new ServiceException(messageSource.getMessage("validation_error_device_part_number_or_description", null, LocaleContextHolder.getLocale()));
        }
        
        if(device.getCostAllocationOption()) {
        	Integer allocationCategoryCount = 0;
        	if(device.getCostMappings() != null) {
        		for(DeviceExpenseCategory mapping: device.getCostMappings()) {
        			if(mapping.getAllocationCategory()) {
        				allocationCategoryCount++;
        			}
        		}
        	}
        	
        	if(allocationCategoryCount == 0) {
        		throw new ServiceException(messageSource.getMessage("validation_error_device_cost_allocation_category_required", null, LocaleContextHolder.getLocale()));
        	} else if(allocationCategoryCount > 1) {
        		throw new ServiceException(messageSource.getMessage("validation_error_device_cost_allocation_mappings_limit", null, LocaleContextHolder.getLocale()));
        	}
        }
        
        String deviceType = null;
        if(device.getDeviceType() != null) deviceType = device.getDeviceType().name();
        String termDuration = null;
        String billingPlan = null;
        String segment = null;
        if(device.getTermDuration() != null) termDuration = device.getTermDuration().name();
        if(device.getBillingPlan() != null) billingPlan = device.getBillingPlan().name();
        if(device.getSegment() != null) segment = device.getSegment().name();
        
        try {
            int updated = jdbcTemplate.update("update device set alt_id = ?, part_number = ?, description = ?, device_type = ?, units = ?, archived = ?, default_osp_id = ?, activate_sync_enabled = ?,"
                    + " is_ci = ?, activate_add_business_service = ?, pricing_sheet_enabled = ?, require_unit_count =?, cost_allocation_option = ?, product_id = ?, "
                    + " pricing_sync_enabled = ?, catalog_recurring_cost = ?, catalog_recurring_price = ?, term_duration = ?, billing_plan = ?, segment = ?, updated = ?, updated_by = ? where id = ?",
                    new Object[]{device.getAltId(), device.getPartNumber(), device.getDescription(), deviceType, device.getUnits(), device.getArchived(), device.getDefaultOspId(), device.getActivateSyncEnabled(),
                device.getIsCI(), device.getActivateAddBusinessService(), device.getPricingSheetEnabled(), device.getRequireUnitCount(), device.getCostAllocationOption(), device.getProductId(),
                device.getPricingSyncEnabled(), device.getCatalogRecurringCost(), device.getCatalogRecurringPrice(), termDuration, billingPlan, segment, new DateTime().withZone(DateTimeZone.forID(TZID)).toDate(), authenticatedUser(), device.getId()});
            log.info("Updated [{}] devices for device ID [{}]", new Object[]{updated, device.getId()});
            
            updateDeviceSPLACosts(device);
            updateDeviceRelationships(device.getId(), device.getRelatedDevices());
            costDaoService.saveOrUpdateDeviceCostMappings(device.getId(), device.getCostMappings());
            updateDeviceProperties(device.getId(), device.getProperties());
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_device_update", new Object[]{device.getId(), any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    }

    @Override
    public void deleteDevice(Long id) throws ServiceException {
        Integer count = jdbcTemplate.queryForObject("select count(*) from device where id = ?", Integer.class, id);
        if (!count.equals(1)) {
            throw new ServiceException(messageSource.getMessage("device_not_found_for_id", new Object[]{id}, LocaleContextHolder.getLocale()));
        }
        
        //delete spla cost associations
        int spla_updated = jdbcTemplate.update("delete from device_spla_cost_catalog where device_id = ?", new Object[]{id});
        
        // note: device child relationships will cascade delete
        
        // note: device properties will cascade delete
        
        //see if this is being used by contract services
        Integer contractServiceCount = jdbcTemplate.queryForObject("select count(*) from contract_service_device where device_id = ?", Integer.class, id);
        if (!contractServiceCount.equals(0)) {
            throw new ServiceException(messageSource.getMessage("validation_error_delete_device_associated", null, LocaleContextHolder.getLocale()));
        }
        
        try {
            int updated = jdbcTemplate.update("delete from device where id = ?", id);
            log.info("Deleted [{}] devices for device ID [{}]", new Object[]{updated, id});
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_device_delete", new Object[]{id, any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    }
    
    private void generateDeviceSPLACosts(Device device, Long pk) throws ServiceException {
        for (SPLACost splaCost : device.getSplaCosts()) {
            try {
                SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
                jdbcInsert.withTableName("device_spla_cost_catalog");
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("spla_cost_catalog_id", splaCost.getId());
                params.put("device_id", pk);
                int updated = jdbcInsert.execute(new MapSqlParameterSource(params));
            } catch (Exception any) {
                throw new ServiceException(messageSource.getMessage("jdbc_error_costitem_fraction_insert", new Object[]{any.getMessage()}, LocaleContextHolder.getLocale()), any);
            }
        }
    }
    
    private void updateDeviceSPLACosts(Device device) throws ServiceException {
        try {
            int updated = jdbcTemplate.update("delete from device_spla_cost_catalog where device_id = ?",
                    new Object[]{device.getId()});
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_costitem_fraction_delete", new Object[]{device.getId(), any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
        generateDeviceSPLACosts(device, device.getId());
    }
    
    @Override
    @Transactional(readOnly = false, rollbackFor = ServiceException.class)
    public void mergeDevice(Long oldDeviceId, Long newDeviceId) throws ServiceException {
    	//get devices
    	Device oldDevice = device(oldDeviceId);
    	Device newDevice = device(newDeviceId);
    	
    	if(oldDevice == null || newDevice == null) {
    		throw new ServiceException(messageSource.getMessage("validation_error_device_merge_missing_devices", null, LocaleContextHolder.getLocale()));
    	}
    	
    	List<Long> pricingSheetConflicts = getPricingSheetProductConflicts(oldDeviceId, newDeviceId);
    	if(pricingSheetConflicts.size() > 0) {
    		throw new ServiceException(messageSource.getMessage("validation_error_device_merge_pricing_sheet_conflict", new Object[]{StringUtils.join(pricingSheetConflicts, ",")}, LocaleContextHolder.getLocale()));
    	}
    	
    	StringBuffer logDetails = new StringBuffer();
    	
    	//update contract service devices
    	List<Long> contractServiceIds = getContractServiceIdsForDevice(oldDeviceId);
    	if(contractServiceIds != null && contractServiceIds.size() > 0) {
	    	try {
	    		int updated = jdbcTemplate.update("update contract_service_device set device_id = ? where device_id = ?", new Object[]{newDeviceId, oldDeviceId});
	            log.info("Updated [{}] contract_service_devices from Device ID [{}] to [{}]", new Object[]{updated, oldDeviceId, newDeviceId});
	        } catch (Exception any) {
	        	any.printStackTrace();
	            throw new ServiceException(messageSource.getMessage("jdbc_error_device_merge_contract_service_update", new Object[]{oldDeviceId, newDeviceId}, LocaleContextHolder.getLocale()), any);
	        }
	    	logDetails.append("contract_service_device updates: ").append(StringUtils.join(contractServiceIds, ",")).append(" \\ ");
    	} else {
    		log.info("No contract_service_device records to update for device merge with Device ID [{}] to [{}]", new Object[]{oldDeviceId, newDeviceId});
    		logDetails.append("No contract_service_device updates. \\ ");
    	}
    	
    	//update pricing sheets
    	List<Long> pricingSheetProductIds = getPricingSheetProductIdsForDevice(oldDeviceId);
    	if(pricingSheetProductIds != null && pricingSheetProductIds.size() > 0) {
	    	try {
	    		int updated = jdbcTemplate.update("update pricing_sheet_product set device_id = ? where device_id = ?", new Object[]{newDeviceId, oldDeviceId});
	            log.info("Updated [{}] contract_service_devices from Device ID [{}] to [{}]", new Object[]{updated, oldDeviceId, newDeviceId});
	        } catch (Exception any) {
	        	any.printStackTrace();
	        	throw new ServiceException(messageSource.getMessage("jdbc_error_device_merge_pricing_sheet_product_update", new Object[]{oldDeviceId, newDeviceId}, LocaleContextHolder.getLocale()), any);
	        }
	    	logDetails.append("pricing_sheet_product updates: ").append(StringUtils.join(pricingSheetProductIds, ",")).append(" \\ ");
    	} else {
    		log.info("No pricing_sheet_product records to update for device merge with Device ID [{}] to [{}]", new Object[]{oldDeviceId, newDeviceId});
    		logDetails.append("No pricing_sheet_product updates. \\ ");
    	}
    	
    	//update contract service azures
    	List<Long> contractServiceAzureIds = getContractServiceAzureIdsForDevice(oldDeviceId);
    	if(contractServiceAzureIds != null && contractServiceAzureIds.size() > 0) {
	    	try {
	    		int updated = jdbcTemplate.update("update contract_service_subscription set device_id = ? where device_id = ?", new Object[]{newDeviceId, oldDeviceId});
	            log.info("Updated [{}] contract_service_azure from Device ID [{}] to [{}]", new Object[]{updated, oldDeviceId, newDeviceId});
	        } catch (Exception any) {
	        	any.printStackTrace();
	        	throw new ServiceException(messageSource.getMessage("jdbc_error_device_merge_contract_service_azure_update", new Object[]{oldDeviceId, newDeviceId}, LocaleContextHolder.getLocale()), any);
	        }
	    	logDetails.append("contract_service_subscription updates: ").append(StringUtils.join(contractServiceAzureIds, ",")).append(" \\ ");
    	} else {
    		log.info("No contract_service_subscription records to update for device merge with Device ID [{}] to [{}]", new Object[]{oldDeviceId, newDeviceId});
    		logDetails.append("No contract_service_subscription updates. \\ ");
    	}
    	
    	//log what is happening
    	try {
            SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
            jdbcInsert.withTableName("device_merge_log").usingGeneratedKeyColumns("id");
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("old_id", oldDeviceId);
            params.put("old_part_number", oldDevice.getPartNumber());
            params.put("old_description", oldDevice.getDescription());
            params.put("new_id", newDeviceId);
            params.put("new_part_number", newDevice.getPartNumber());
            params.put("new_description", newDevice.getDescription());
            params.put("change_details", logDetails.toString());
            params.put("created_by", authenticatedUser());
            Number pk = jdbcInsert.executeAndReturnKey(new MapSqlParameterSource(params));
        } catch (Exception any) {
        	throw new ServiceException(messageSource.getMessage("jdbc_error_device_merge_log_insert", null, LocaleContextHolder.getLocale()), any);
        }
    	
    	//delete device 1
    	deleteDevice(oldDeviceId);
    }
    
    private List<Long> getContractServiceIdsForDevice(Long deviceId) {
    	String query = "select contract_service_id from contract_service_device where device_id = ?";
        return jdbcTemplate.query(query, new Object[]{deviceId}, new RowMapper<Long>() {
            @Override
            public Long mapRow(ResultSet rs, int i) throws SQLException {
                return rs.getLong("contract_service_id");
            }
        });
    }
    
    private List<Long> getPricingSheetProductIdsForDevice(Long deviceId) throws ServiceException {
    	String query = "select id from pricing_sheet_product where device_id = ?";
        return jdbcTemplate.query(query, new Object[]{deviceId}, new RowMapper<Long>() {
            @Override
            public Long mapRow(ResultSet rs, int i) throws SQLException {
                return rs.getLong("id");
            }
        });
    }
    
    private List<Long> getPricingSheetProductConflicts(Long oldDeviceId, Long newDeviceId) {
    	String query = "select ps.contract_id contract_id from pricing_sheet_product psp "
    			    +  "inner join pricing_sheet ps on ps.id = psp.pricing_sheet_id "
    				+  "where psp.device_id = ?";
        List<Long> deviceOneContracts = jdbcTemplate.query(query, new Object[]{oldDeviceId}, 
        		new RowMapper<Long>() {
            @Override
            public Long mapRow(ResultSet rs, int i) throws SQLException {
                return rs.getLong("contract_id");
            }
        });
        
	    List<Long> deviceTwoContracts = jdbcTemplate.query(query, new Object[]{newDeviceId}, 
	    		new RowMapper<Long>() {
	        @Override
	        public Long mapRow(ResultSet rs, int i) throws SQLException {
	            return rs.getLong("contract_id");
	        }
	    });
	    
	    List<Long> matching = new ArrayList<Long>();
	    
	    for(Long contractOneId : deviceOneContracts) {
	    	for(Long contractTwoId : deviceTwoContracts) {
	    		if(contractOneId.equals(contractTwoId)) matching.add(contractOneId); 
	    	}
	    }
	    
	    return matching;
    }
    
    private List<Long> getContractServiceAzureIdsForDevice(Long deviceId) {
    	String query = "select id from contract_service_subscription where device_id = ?";
        return jdbcTemplate.query(query, new Object[]{deviceId}, new RowMapper<Long>() {
            @Override
            public Long mapRow(ResultSet rs, int i) throws SQLException {
                return rs.getLong("id");
            }
        });
    }

    @Override
    public Device device(Long id) throws ServiceException {
        Integer count = jdbcTemplate.queryForObject("select count(*) from device where id = ?", Integer.class, id);
        if (!count.equals(1)) {
            throw new ServiceException(messageSource.getMessage("device_not_found_for_id", new Object[]{id}, LocaleContextHolder.getLocale()));
        }
        String query = "select * from device where id = ? order by part_number, description";
        Device device = jdbcTemplate.queryForObject(query, new Object[]{id},
                new RowMapper<Device>() {
            @Override
            public Device mapRow(ResultSet rs, int i) throws SQLException {
                Device device = new Device();
                device.setId(rs.getLong("id"));
                device.setAltId(rs.getString("alt_id"));
                device.setPartNumber(rs.getString("part_number"));
                device.setDescription(rs.getString("description"));
                device.setArchived(rs.getBoolean("archived"));
                device.setActivateSyncEnabled(rs.getBoolean("activate_sync_enabled"));
                device.setActivateAddBusinessService(rs.getBoolean("activate_add_business_service"));
                device.setIsCI(rs.getBoolean("is_ci"));
                device.setPricingSheetEnabled(rs.getBoolean("pricing_sheet_enabled"));
                device.setRequireUnitCount(rs.getBoolean("require_unit_count"));
                device.setDefaultOspId((rs.getLong("default_osp_id") == 0) ? null : rs.getLong("default_osp_id"));
                device.setDeviceType((rs.getString("device_type") == null) ? null : Device.DeviceType.valueOf(rs.getString("device_type")));
                device.setUnits(rs.getString("units"));
                device.setCostAllocationOption(rs.getBoolean("cost_allocation_option"));
                device.setPricingSyncEnabled(rs.getBoolean("pricing_sync_enabled"));
                device.setCatalogRecurringCost(rs.getBigDecimal("catalog_recurring_cost"));
                device.setCatalogRecurringPrice(rs.getBigDecimal("catalog_recurring_price"));
                device.setTermDuration((rs.getString("term_duration") == null) ? null : Device.TermDuration.valueOf(rs.getString("term_duration")));
                device.setBillingPlan((rs.getString("billing_plan") == null) ? null : Device.BillingPlan.valueOf(rs.getString("billing_plan")));
                device.setSegment((rs.getString("segment") == null) ? null : Device.Segment.valueOf(rs.getString("segment")));
                device.setCreated(rs.getDate("created"));
                device.setCreatedBy(rs.getString("created_by"));
                device.setUpdated(rs.getDate("updated"));
                device.setUpdatedBy(rs.getString("updated_by"));
                return device;
            }
        });
        
        List<SPLACost> splaCosts = findSPLACostsForDevice(device.getId());
    	if(splaCosts != null && splaCosts.size() > 0) {
    		device.setSplaCosts(splaCosts);
    	}
        List<Device> relatedDevices = findRelatedDevices(device.getId());
        if (relatedDevices != null && relatedDevices.size() > 0) {
            device.setRelatedDevices(relatedDevices);
        }
        Integer parentCount = jdbcTemplate.queryForObject("select count(*) from device_relationship where related_device_id = ?", Integer.class, device.getId());
        if (parentCount > 0) {
            device.setHasParent(Boolean.TRUE);
        }
        List<DeviceExpenseCategory> costMappings = findCostMappingsForDevice(device.getId());
        if (costMappings != null && costMappings.size() > 0) {
            device.setCostMappings(costMappings);
        }
        List<DeviceProperty> deviceProperties = findDevicePropertiesForDevice(device.getId());
        if (deviceProperties != null && deviceProperties.size() > 0) {
            device.setProperties(deviceProperties);
        }
    	return device;
    }
    
    @Override
    public Device findDeviceByProductId(Long productId) throws ServiceException {
    	String query = "select * from device where product_id = ?";
        List<Device> devices = jdbcTemplate.query(query, new Object[]{productId}, new DeviceRowMapper());
        if (devices == null || devices.isEmpty()) {
            return null;
        }
        // there should only be one...
        return devices.get(0);
    }
    
    @Override
    public List<String> pipeDelimitedDeviceString() {
        List<String> deviceInfo = new ArrayList<String>();
        List<Map<String, Object>> results = jdbcTemplate.queryForList(
                "select id, part_number, description"
                + " from device"
                + " where archived = 0"
                + " order by part_number, description");
        for (Map<String, Object> result : results) {
            Long deviceId = (Long) result.get("id");
            String partNumber = (String) result.get("part_number");
            String description = (String) result.get("description");
            deviceInfo.add(deviceId + "|" + partNumber + "|" + description);
        }
        return deviceInfo;
    }
    
    @Override
    public List<Device> devices() {
    	return findDevices(null, null, false);
    }
    
    @Override
    public List<Device> devices(Boolean isArchived) {
    	return findDevices(isArchived, null, false);
    }
    
    @Override
    public List<Device> minimumDevices(Boolean isArchived) {
    	return findDevices(isArchived, null, true);
    }
    
    @Override
    public List<Device> findDevicesForOSMSync() throws ServiceException {
    	return findDevices(null, true, false);
    }
    
    @Override
    public List<Device> findDevicesForContract(Long contractId) {
    	String query = "select distinct d.id, d.alt_id, d.part_number, d.description, d.archived, d.activate_sync_enabled, d.pricing_sheet_enabled, d.require_unit_count,  "
    			+ "d.is_ci, d.activate_add_business_service, d.default_osp_id, d.device_type, d.units, d.cost_allocation_option, d.pricing_sync_enabled, d.catalog_recurring_cost, d.catalog_recurring_price, "
    			+ "d.term_duration, d.billing_plan, d.segment, "
    			+ "d.created, d.created_by, d.updated, d.updated_by ";
    		query += "from device d ";
    		query += "inner join contract_service_device csd on csd.device_id = d.id ";
    		query += "inner join contract_service cs on cs.id = csd.contract_service_id ";
    		query += "where cs.contract_id = :contract_id and d.pricing_sheet_enabled = :pricing_sheet_enabled and d.archived = :archived ";
			query += "order by d.description, d.part_number";
	    Map<String, Object> params = new HashMap<String, Object>();
	    params.put("pricing_sheet_enabled", true);
	    params.put("archived", false);
	    params.put("contract_id", contractId);
	    return namedJdbcTemplate.query(query, params, new RowMapper<Device>() {
	        @Override
	        public Device mapRow(ResultSet rs, int i) throws SQLException {
	            Device device = new Device();
	            device.setId(rs.getLong("id"));
	            device.setAltId(rs.getString("alt_id"));
	            device.setPartNumber(rs.getString("part_number"));
	            device.setDescription(rs.getString("description"));
	            device.setArchived(rs.getBoolean("archived"));
	            device.setIsCI(rs.getBoolean("is_ci"));
	            device.setActivateSyncEnabled(rs.getBoolean("activate_sync_enabled"));
	            device.setActivateAddBusinessService(rs.getBoolean("activate_add_business_service"));
	            device.setPricingSheetEnabled(rs.getBoolean("pricing_sheet_enabled"));
	            device.setRequireUnitCount(rs.getBoolean("require_unit_count"));
	            device.setDefaultOspId((rs.getLong("default_osp_id") == 0) ? null : rs.getLong("default_osp_id"));
	            device.setDeviceType((rs.getString("device_type") == null) ? null : Device.DeviceType.valueOf(rs.getString("device_type")));
                device.setUnits(rs.getString("units"));
                device.setTermDuration((rs.getString("term_duration") == null) ? null : Device.TermDuration.valueOf(rs.getString("term_duration")));
                device.setBillingPlan((rs.getString("billing_plan") == null) ? null : Device.BillingPlan.valueOf(rs.getString("billing_plan")));
                device.setSegment((rs.getString("segment") == null) ? null : Device.Segment.valueOf(rs.getString("segment")));
                device.setCostAllocationOption(rs.getBoolean("cost_allocation_option"));
                device.setPricingSyncEnabled(rs.getBoolean("pricing_sync_enabled"));
                device.setCatalogRecurringCost(rs.getBigDecimal("catalog_recurring_cost"));
                device.setCatalogRecurringPrice(rs.getBigDecimal("catalog_recurring_price"));
	            device.setCreated(rs.getDate("created"));
	            device.setCreatedBy(rs.getString("created_by"));
	            device.setUpdated(rs.getDate("updated"));
	            device.setUpdatedBy(rs.getString("updated_by"));
	            return device;
	        }
	    });
    }
    
    private List<Device> findDevices(Boolean isArchived, Boolean isOSMSyncEnabled, Boolean minimumObject) {
    	String query = "select * from device ";
    	if(isArchived != null || isOSMSyncEnabled != null) {
    		query += "where ";
    	}
    	if(isArchived != null) {
			query += "archived = :archived ";
		}
		if(isOSMSyncEnabled != null) {
			query += "osm_sync_enabled = :osm_sync_enabled ";
		}
			query += "order by description, part_number";
	    Map<String, Object> params = new HashMap<String, Object>();
	    if(isArchived != null) params.put("archived", isArchived);
	    if(isOSMSyncEnabled != null) params.put("osm_sync_enabled", isOSMSyncEnabled);
	    
	    StopWatch sw = new StopWatch();
        sw.start();
	    List<Device> devices = namedJdbcTemplate.query(query, params, new DeviceRowMapper());
	    sw.stop();
	    log.debug("**** Initial base query took {} seconds ****", sw.getTotalTimeSeconds());
	    sw.start();
	    
	    List<DeviceRelationship> relationships = findDeviceRelationships();
	    
	    for(Device device : devices) {
	    	
	    	if(!minimumObject) {
		    	List<SPLACost> splaCosts = findSPLACostsForDevice(device.getId());
		    	if(splaCosts != null && splaCosts.size() > 0) {
		    		device.setSplaCosts(splaCosts);
		    	}
	    	}
	    	
	    	/* Querying for all of the related devices for each device was taking a long time. I've swapped this to retrieve all the relationships at once. 
	    	 * Then we loop through those and get the related devices from the device list we already have. 
	    	 * We deep copy the objects and add them to a list that gets added to the parent.
	    	 */
	    	
	    	for(DeviceRelationship relationship: relationships) {
                if(relationship.getDeviceId().equals(device.getId())) {
                    for(Device deviceRef: devices) {
                        if(deviceRef.getId().equals(relationship.getRelatedDeviceId())) {
                            Device relatedDevice = buildRelatedDevice(device.getId(), deviceRef, relationship);
                            for(DeviceRelationship grandChildRelationship: relationships) {
                                if(relationship.getRelatedDeviceId().equals(grandChildRelationship.getDeviceId())) {
                                    for(Device grandChildDeviceRef: devices) {
                                        if(grandChildDeviceRef.getId().equals(grandChildRelationship.getRelatedDeviceId())) {
                                        	Device grandChildDevice = buildRelatedDevice(relatedDevice.getId(), grandChildDeviceRef, grandChildRelationship);
                                        	//making sure the grand child device isn't already mapped
                                        	boolean found = false;
                                        	if(relatedDevice.getRelatedDevices() != null) {
                                        		for(Device existingGrandChildDevice: relatedDevice.getRelatedDevices()) {
                                        			if(grandChildDeviceRef.getId().equals(existingGrandChildDevice.getId())) {
                                        				found = true;
                                        				break;
                                        			}
                                        		}
                                        	}
                                        		
                                        	if(!found) {
                                        		relatedDevice.addRelatedDevice(grandChildDevice);
                                            	break;
                                        	}	
                                        }
                                    }
                                }
                            }
                            device.addRelatedDevice(relatedDevice);
                            break;
                        }
                    }
                }
	    	}
	    	
	    	/*
            List<Device> relatedDevices = findRelatedDevices(device.getId());
            if (relatedDevices != null && relatedDevices.size() > 0) {
                device.setRelatedDevices(relatedDevices);
            }*/          
            String parentQuery = "select d.id, d.description, d.part_number from device d"
                    + " inner join device_relationship dr on dr.device_id = d.id"
                    + " where dr.related_device_id = ?";
            List<ParentDevice> parentDevices = jdbcTemplate.query(parentQuery, new Object[]{device.getId()}, new RowMapper<ParentDevice>() {
                        @Override
                        public ParentDevice mapRow(ResultSet rs, int i) throws SQLException {
                            return new ParentDevice(rs.getLong("id"), rs.getString("description"), rs.getString("part_number"));
                        }
                    });
            if (parentDevices != null && parentDevices.size() > 0) {
                device.setHasParent(Boolean.TRUE);
                device.setParentDevices(parentDevices);
            }
            if(!minimumObject) {
	            List<DeviceExpenseCategory> costMappings = findCostMappingsForDevice(device.getId());
	            if (costMappings != null && costMappings.size() > 0) {
	                device.setCostMappings(costMappings);
	            }
            }
            List<DeviceProperty> deviceProperties = findDevicePropertiesForDevice(device.getId());
            if (deviceProperties != null && deviceProperties.size() > 0) {
                device.setProperties(deviceProperties);
            }
	    }
	    
	    sw.stop();
        log.debug("**** Get Devices took {} seconds ****", sw.getTotalTimeSeconds());
	    return devices;
    }
    
    private Device buildRelatedDevice(Long parentId, Device ref, DeviceRelationship relationship) {
        try {
            Device device = clone(ref);
            device.setRelationship(Device.Relationship.valueOf(relationship.getRelationship()));
            device.setHasParent(Boolean.TRUE);
            device.setParentId(parentId);
            device.setSpecUnits(relationship.getSpecUnits());
            device.setOrder(relationship.getOrder());
            return device;
        } catch (Exception e) {
            log.error("Error occurred setting up related device.", e);
        }
        return null;
    }
    
    private static <T extends Serializable> T clone(T object) {
        return (T) SerializationUtils.deserialize(SerializationUtils.serialize(object));
    }

    @Override
    public List<Device> searchDevicesByDescription(String search) {
        String query = "select * from device where description like :search"
                + " order by part_number, description";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("search", "%" + search + "%");
        return namedJdbcTemplate.query(query, params, new DeviceRowMapper());
    }

    @Override
    public List<Device> searchDevicesByPartNumber(String search) {
        String query = "select * from device where part_number like :search"
                + " order by part_number, description";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("search", "%" + search + "%");
        return namedJdbcTemplate.query(query, params, new DeviceRowMapper());
    }

    @Override
    public Device findDeviceByNameAndPartNumber(String partNumber, String description) {
        String query = "select * from device where part_number = ? and description = ?";
        List<Device> devices = jdbcTemplate.query(query, new Object[]{partNumber, description}, new DeviceRowMapper());
        if (devices == null || devices.isEmpty()) {
            return null;
        }
        // there should only be one...
        return devices.get(0);
    }
    
    @Override
    public List<Device> findDeviceByDeviceType(Device.DeviceType deviceType) throws ServiceException {
    	if(deviceType == null) {
    		throw new ServiceException("Device Type can not be null when searching by device type.");
    	}
        String query = "select * from device where device_type = ?";
        return jdbcTemplate.query(query, new Object[]{deviceType.name()}, new DeviceRowMapper());
    }
    
    @Override
    public List<Device> findDeviceByDefaultOSPId(Long ospId) throws ServiceException {
    	if(ospId == null) {
    		throw new ServiceException("OSP ID can not be null when searching by device type.");
    	}
        String query = "select * from device where default_osp_id = ?";
        return jdbcTemplate.query(query, new Object[]{ospId}, new DeviceRowMapper());
    }
    
    @Override
    public List<Device> findDevicesForCostAllocation(Date month) {
    	DateTime date = new DateTime(month).withDayOfMonth(1).withTimeAtStartOfDay().withZone(DateTimeZone.forID(TZID));
    	String query = "select d.id, d.alt_id, d.part_number, d.description, d.archived, d.is_ci, d.activate_sync_enabled, d.activate_add_business_service, d.pricing_sheet_enabled, "
    			+ " d.require_unit_count, d.default_osp_id, d.device_type, d.units, d.cost_allocation_option, duc.unit_count, d.pricing_sync_enabled, d.catalog_recurring_cost, d.catalog_recurring_price,"
    			+ " d.term_duration, d.billing_plan, d.segment, "
    			+ " d.created, d.created_by, d.updated, d.updated_by "
    			+ " from device d"
    			+ " inner join device_unit_count duc on duc.device_id = d.id"
    			+ " where d.cost_allocation_option = true"
    			+ " and duc.month = :month";
    	Map<String, Object> params = new HashMap<String, Object>();
	    params.put("month", date.toDate());
        return namedJdbcTemplate.query(query, params, new RowMapper<Device>() {
	        @Override
	        public Device mapRow(ResultSet rs, int i) throws SQLException {
	            Device device = new Device();
	            device.setId(rs.getLong("id"));
	            device.setAltId(rs.getString("alt_id"));
	            device.setPartNumber(rs.getString("part_number"));
	            device.setDescription(rs.getString("description"));
	            device.setArchived(rs.getBoolean("archived"));
	            device.setIsCI(rs.getBoolean("is_ci"));
	            device.setActivateSyncEnabled(rs.getBoolean("activate_sync_enabled"));
	            device.setActivateAddBusinessService(rs.getBoolean("activate_add_business_service"));
	            device.setPricingSheetEnabled(rs.getBoolean("pricing_sheet_enabled"));
	            device.setRequireUnitCount(rs.getBoolean("require_unit_count"));
	            device.setDefaultOspId((rs.getLong("default_osp_id") == 0) ? null : rs.getLong("default_osp_id"));
	            device.setDeviceType((rs.getString("device_type") == null) ? null : Device.DeviceType.valueOf(rs.getString("device_type")));
                device.setUnits(rs.getString("units"));
                device.setTermDuration((rs.getString("term_duration") == null) ? null : Device.TermDuration.valueOf(rs.getString("term_duration")));
                device.setBillingPlan((rs.getString("billing_plan") == null) ? null : Device.BillingPlan.valueOf(rs.getString("billing_plan")));
                device.setSegment((rs.getString("segment") == null) ? null : Device.Segment.valueOf(rs.getString("segment")));
                device.setCostAllocationOption(rs.getBoolean("cost_allocation_option"));
                device.setPricingSyncEnabled(rs.getBoolean("pricing_sync_enabled"));
                device.setCatalogRecurringCost(rs.getBigDecimal("catalog_recurring_cost"));
                device.setCatalogRecurringPrice(rs.getBigDecimal("catalog_recurring_price"));
                device.setUnitCount(rs.getInt("unit_count"));
	            device.setCreated(rs.getDate("created"));
	            device.setCreatedBy(rs.getString("created_by"));
	            device.setUpdated(rs.getDate("updated"));
	            device.setUpdatedBy(rs.getString("updated_by"));
	            return device;
	        }
	    });
    }

    @Override
    public Device findDeviceByPartNumber(String partNumber) {
        String query = "select * from device where part_number = ?";
        List<Device> devices = jdbcTemplate.query(query, new Object[]{partNumber}, new DeviceRowMapper());
        if (devices == null || devices.isEmpty()) {
            return null;
        }
        if (devices.size() > 1) {
            log.warn("Ambiguous device results found: multiple records for device part no [{}]", partNumber);
        }
        // there COULD be more than one but we will return 0th
        return devices.get(0);
    }
    
    @Override
    public List<SPLACost> findSPLACostsForDevice(Long deviceId) {
        String query = BASE_SPLA_COST_QUERY
        		+ " inner join device_spla_cost_catalog dscc on dscc.spla_cost_catalog_id = scc.id"
                + " where dscc.device_id = :deviceId";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("deviceId", deviceId);
        return namedJdbcTemplate.query(query, params, new SPLACostCatalogRowMapper());
    }
    
    private List<DeviceRelationship> findDeviceRelationships() {
        String query = "select device_id, related_device_id, relationship, spec_units, sorder from device_relationship order by device_id, sorder";
        List<DeviceRelationship> deviceRelationships = jdbcTemplate.query(query, new RowMapper<DeviceRelationship>() {
	        @Override
	        public DeviceRelationship mapRow(ResultSet rs, int i) throws SQLException {
	        	return new DeviceRelationship(
                        rs.getLong("device_id"),
                        rs.getLong("related_device_id"),
                        rs.getString("relationship"),
                        rs.getInt("spec_units"),
                        rs.getInt("sorder"));
	        }
	    });
        
        return deviceRelationships;
    }
    
    @Override
    public List<Device> findRelatedDevices(Long deviceId) {
        List<Device> relatedDevices = new ArrayList<Device>();
        String query = "select related_device_id, relationship, spec_units, sorder from device_relationship where device_id = ? order by sorder";
        List<Map<String, Object>> relatedDeviceRecords = jdbcTemplate.queryForList(query, deviceId);
        for (Map<String, Object> relatedDeviceRecord : relatedDeviceRecords) {
            try {
                Device relatedDevice = this.device((Long) relatedDeviceRecord.get("related_device_id"));
                relatedDevice.setParentId(deviceId);
                relatedDevice.setRelationship((Device.Relationship) Device.Relationship.valueOf((String) relatedDeviceRecord.get("relationship")));
                relatedDevice.setSpecUnits((Integer) relatedDeviceRecord.get("spec_units"));
                relatedDevice.setOrder((Integer) relatedDeviceRecord.get("sorder"));
                relatedDevice.setHasParent(Boolean.TRUE);
                relatedDevices.add(relatedDevice);
            } catch (ServiceException se) {
                log.warn("exception thrown looking up related device to parent [{}]", deviceId);
            }
        }
        return relatedDevices;
    }

    @Override
    public List<DeviceExpenseCategory> findCostMappingsForDevice(Long deviceId) {
        return jdbcTemplate.query("select * from device_expense_category where device_id = ?",
                new Object[]{deviceId}, new RowMapper<DeviceExpenseCategory>() {
            @Override
            public DeviceExpenseCategory mapRow(ResultSet rs, int i) throws SQLException {
                return new DeviceExpenseCategory(
                        rs.getLong("device_id"),
                        rs.getInt("expense_category_id"),
                        rs.getInt("quantity"),
                        rs.getBoolean("allocation_category"));
            }
        });
    }

    @Override
    public List<DeviceExpenseCategory> findCostMappingsForExpenseCategory(Integer expenseCategoryId) {
        return jdbcTemplate.query("select * from device_expense_category where expense_category_id = ?",
                new Object[]{expenseCategoryId}, new RowMapper<DeviceExpenseCategory>() {
            @Override
            public DeviceExpenseCategory mapRow(ResultSet rs, int i) throws SQLException {
                return new DeviceExpenseCategory(
                        rs.getLong("device_id"),
                        rs.getInt("expense_category_id"),
                        rs.getInt("quantity"),
                        rs.getBoolean("allocation_category"));
            }
        });
    }
    
    @Override
    public List<ExpenseCategory> findExpenseCategories() {
        // this query only sets up single level parent child results
        String query = "select ecp.id as 'pid', ecp.name as 'pname', ecp.description as 'pdescription',"
                + " ecc.id as 'cid', ecc.name as 'cname', ecc.description as 'cdescription', ecc.units as 'cunits'"
                + " from expense_category ecp"
                + " left outer join expense_category ecc on ecp.id = ecc.parent_id"
                + " where ecp.parent_id is null"
                + " order by ecp.name, ecc.name";
        List<ExpenseCategory> expenseCategories = jdbcTemplate.query(query,
                new RowMapper<ExpenseCategory>() {
                    @Override
                    public ExpenseCategory mapRow(ResultSet rs, int i) throws SQLException {
                        ExpenseCategory child;
                        ExpenseCategory parent = new ExpenseCategory(rs.getInt("pid"), rs.getString("pname"), rs.getString("pdescription"));
                        if (rs.getInt("cid") > 0) {
                            child = new ExpenseCategory(rs.getInt("cid"), rs.getString("cname"), rs.getString("cdescription"));
                            child.setUnits(rs.getString("cunits"));
                            child.setParent(parent);
                        } else {
                            child = parent;
                        }
                        return child;
                    }
        });
        List<ExpenseCategory> groupedExpenseCategories = new ArrayList<ExpenseCategory>();
        ExpenseCategory parent = null;
        for (ExpenseCategory member : expenseCategories) {
            if (parent == null || !parent.equals(member.getParent())) {
                if (member.getParent() == null) {
                    parent = member;
                } else {
                    parent = member.getParent();
                }
                groupedExpenseCategories.add(parent);
            }
            if (member.getParent() != null) {
                parent.addSubcategory(member);
            }
        }
        return groupedExpenseCategories;
    }
    
    @Override
    public List<DeviceProperty> findDevicePropertiesForDevice(Long deviceId) {
        String query = "select * from device_property dp"
                + " where dp.device_id = :deviceId";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("deviceId", deviceId);
        return namedJdbcTemplate.query(query, params, new DevicePropertyRowMapper());
    }
    
    private class DevicePropertyRowMapper implements RowMapper {
        public DeviceProperty mapRow(ResultSet rs, int i) throws SQLException {
        	DeviceProperty property = new DeviceProperty(
                    rs.getLong("id"),
                    rs.getLong("device_id"),
                    DeviceProperty.Type.valueOf(rs.getString("type")),
                    (rs.getInt("unit_count") == 0) ? null : rs.getInt("unit_count"),
                    rs.getString("str_value"),
                    rs.getString("unit_type"));
            return property;
        }
    }
    
    @Override
    public List<Device> findDevicesForSPLACost(Long splaCostCatalogId) {
        String query = "select * from device d"
        		+ " inner join device_spla_cost_catalog dscc on d.id = dscc.device_id"
        		+ " where dscc.spla_cost_catalog_id = :splaCostCatalogId"
                + " order by d.part_number, d.description";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("splaCostCatalogId", splaCostCatalogId);
        return namedJdbcTemplate.query(query, params, new DeviceRowMapper());
    }
    
    private class DeviceRowMapper implements RowMapper {
    	public Device mapRow(ResultSet rs, int i) throws SQLException {
            Device device = new Device();
            device.setId(rs.getLong("id"));
            device.setAltId(rs.getString("alt_id"));
            device.setProductId((rs.getLong("product_id") == 0) ? null : rs.getLong("product_id"));
            device.setPartNumber(rs.getString("part_number"));
            device.setDescription(rs.getString("description"));
            device.setArchived(rs.getBoolean("archived"));
            device.setActivateSyncEnabled(rs.getBoolean("activate_sync_enabled"));
            device.setActivateAddBusinessService(rs.getBoolean("activate_add_business_service"));
            device.setIsCI(rs.getBoolean("is_ci"));
            device.setPricingSheetEnabled(rs.getBoolean("pricing_sheet_enabled"));
            device.setRequireUnitCount(rs.getBoolean("require_unit_count"));
            device.setDefaultOspId((rs.getLong("default_osp_id") == 0) ? null : rs.getLong("default_osp_id"));
            device.setDeviceType((rs.getString("device_type") == null) ? null : Device.DeviceType.valueOf(rs.getString("device_type")));
            device.setPricingSyncEnabled(rs.getBoolean("pricing_sync_enabled"));
            device.setCatalogRecurringCost(rs.getBigDecimal("catalog_recurring_cost"));
            device.setCatalogRecurringPrice(rs.getBigDecimal("catalog_recurring_price"));
            device.setUnits(rs.getString("units"));
            device.setTermDuration((rs.getString("term_duration") == null) ? null : Device.TermDuration.valueOf(rs.getString("term_duration")));
            device.setBillingPlan((rs.getString("billing_plan") == null) ? null : Device.BillingPlan.valueOf(rs.getString("billing_plan")));
            device.setSegment((rs.getString("segment") == null) ? null : Device.Segment.valueOf(rs.getString("segment")));
            device.setCostAllocationOption(rs.getBoolean("cost_allocation_option"));
            device.setCreated(rs.getDate("created"));
            device.setCreatedBy(rs.getString("created_by"));
            device.setUpdated(rs.getDate("updated"));
            device.setUpdatedBy(rs.getString("updated_by"));
            return device;
        }
    }

    @Override
    public Service findActiveServiceByName(String name) {
        String query = "select id service_id, code, osp_id, version, name, business_model"
                + " from service where name = ? and active = true";
        List<Service> services = jdbcTemplate.query(query, new Object[]{name},
                new RowMapper<Service>() {
            @Override
            public Service mapRow(ResultSet rs, int i) throws SQLException {
                return new Service(
                        rs.getLong("service_id"),
                        rs.getString("code"),
                        rs.getString("osp_id"),
                        rs.getDouble("version"),
                        rs.getString("name"),
                        rs.getString("business_model"));
            }
        });
        if (services == null || services.isEmpty()) {
            return null;
        }
        if (services.size() > 1) {
            log.warn("Ambiguous results returned for Service name (more than 1 result [{}] for name [{}])", new Object[]{services.size(), name});
            return null;
        }
        return services.get(0);
    }

    @Override
    public Service findAnyServiceByName(String name) {
        String query = "select distinct id service_id, version, code, osp_id, name, business_model"
                + " from service where name = ? order by active desc, version desc";
        List<Service> services = jdbcTemplate.query(query, new Object[]{name},
                new RowMapper<Service>() {
            @Override
            public Service mapRow(ResultSet rs, int i) throws SQLException {
                return new Service(
                        rs.getLong("service_id"),
                        rs.getString("code"),
                        rs.getString("osp_id"),
                        rs.getDouble("version"),
                        rs.getString("name"),
                        rs.getString("business_model"));
            }
        });
        if (services == null || services.isEmpty()) {
            return null;
        }
        return services.get(0);
    }

    @Override
    public Service findActiveServiceByOspId(Long id) {
        String query = "select id service_id, code, osp_id, version, name, business_model"
                + " from service where osp_id = ? and active = true";
        List<Service> services = jdbcTemplate.query(query, new Object[]{id},
                new RowMapper<Service>() {
            @Override
            public Service mapRow(ResultSet rs, int i) throws SQLException {
                return new Service(
                        rs.getLong("service_id"),
                        rs.getString("code"),
                        rs.getString("osp_id"),
                        rs.getDouble("version"),
                        rs.getString("name"),
                        rs.getString("business_model"));
            }
        });
        if (services == null || services.isEmpty()) {
            return null;
        }
        if (services.size() > 1) {
            log.warn("Ambiguous results returned for Service name (more than 1 result [{}])", services.size());
            return null;
        }
        return services.get(0);
    }

    @Override
    public Service findAnyServiceByOspId(Long id) {
        String query = "select id service_id, code, osp_id, version, name, business_model"
                + " from service where osp_id = ? order by active desc, version desc";
        List<Service> services = jdbcTemplate.query(query, new Object[]{id},
                new RowMapper<Service>() {
            @Override
            public Service mapRow(ResultSet rs, int i) throws SQLException {
                return new Service(
                        rs.getLong("service_id"),
                        rs.getString("code"),
                        rs.getString("osp_id"),
                        rs.getDouble("version"),
                        rs.getString("name"),
                        rs.getString("business_model"));
            }
        });
        if (services == null || services.isEmpty()) {
            return null;
        }
        if (services.size() > 1) {
            log.info("Ambiguous results returned for Service name (more than 1 result [{}])", services.size());
        }
        return services.get(0);
    }
    
    @Override
    public List<Personnel> personnel(Boolean active) {
    	List<Personnel> personnel = new ArrayList<Personnel>();
    	List<User> users = userDaoService.users(active);
        
    	for(User user : users) {
    		for(Role role : user.getProfile()) {
    			Personnel.Type type = null;
    			if(Role.ROLE_SDM.equals(role)) {
    				type = Personnel.Type.sdm;
    			} else if(Role.ROLE_AE.equals(role)) {
    				type = Personnel.Type.ae;
    			} else if(Role.ROLE_BSC.equals(role)) {
    				type = Personnel.Type.bsc;
    			} else if(Role.ROLE_EPE.equals(role)) {
    				type = Personnel.Type.epe;
    			}
    			
    			if(type != null) personnel.add(new Personnel(user.getId(), user.getName(), type));
    		}
    	}
        
        return personnel;
    }

    @Override
    public List<Location> locations(Boolean isDisplayedRevenue) {
    	String query = "select * from location";
    	
    	if(isDisplayedRevenue != null) {
    		query += " where is_displayed_revenue = :is_displayed_revenue";
    	}
    	query += " order by name";
    	
    	Map<String, Object> params = new HashMap<String, Object>();
    	if(isDisplayedRevenue != null) {
    		params.put("is_displayed_revenue", isDisplayedRevenue);
    	}
    	
        return namedJdbcTemplate.query(query, params, new RowMapper<Location>() {
            @Override
            public Location mapRow(ResultSet rs, int i) throws SQLException {
                Location location = new Location();
                location.setId(rs.getInt("id"));
                location.setName(rs.getString("name"));
                location.setAltName(rs.getString("alt_name"));
                location.setDescription(rs.getString("description"));
                location.setCreated(rs.getDate("created"));
                location.setCreatedBy(rs.getString("created_by"));
                Integer parentId = rs.getInt("parent_id");
                if (parentId != null && parentId > 0) {
                    try {
                        location.setParent(location(parentId));
                    } catch (ServiceException ignore) {
                    }
                }
                return location;
            }
        });
    }

    @Override
    public Location location(Integer id) throws ServiceException {
        Integer count = jdbcTemplate.queryForObject("select count(*) from location where id = ?", Integer.class, id);
        if (!count.equals(1)) {
            throw new ServiceException(messageSource.getMessage("location_not_found_for_id", new Object[]{id}, LocaleContextHolder.getLocale()));
        }
        String query = "select * from location where id = ? order by name";
        return jdbcTemplate.queryForObject(query, new Object[]{id},
                new RowMapper<Location>() {
            @Override
            public Location mapRow(ResultSet rs, int i) throws SQLException {
                Location location = new Location();
                location.setId(rs.getInt("id"));
                location.setName(rs.getString("name"));
                location.setAltName(rs.getString("alt_name"));
                location.setDescription(rs.getString("description"));
                location.setCreated(rs.getDate("created"));
                location.setCreatedBy(rs.getString("created_by"));
                Integer parentId = rs.getInt("parent_id");
                if (parentId != null && parentId > 0) {
                    try {
                        location.setParent(location(parentId));
                    } catch (ServiceException ignore) {
                    }
                }
                return location;
            }
        });
    }

    @Override
    public Location findLocationByName(String name) throws ServiceException {
        Integer count = jdbcTemplate.queryForObject("select count(*) from location where name = ?", Integer.class, name);
        if (!count.equals(1)) {
            throw new ServiceException(messageSource.getMessage("location_not_found_for_name", new Object[]{name}, LocaleContextHolder.getLocale()));
        }
        String query = "select * from location where name = ?";
        return jdbcTemplate.queryForObject(query, new Object[]{name},
                new RowMapper<Location>() {
            @Override
            public Location mapRow(ResultSet rs, int i) throws SQLException {
                Location location = new Location();
                location.setId(rs.getInt("id"));
                location.setName(rs.getString("name"));
                location.setAltName(rs.getString("alt_name"));
                location.setDescription(rs.getString("description"));
                location.setCreated(rs.getDate("created"));
                location.setCreatedBy(rs.getString("created_by"));
                Integer parentId = rs.getInt("parent_id");
                if (parentId != null && parentId > 0) {
                    try {
                        location.setParent(location(parentId));
                    } catch (ServiceException ignore) {
                    }
                }
                return location;
            }
        });
    }

    @Override
    public ScheduledTask findScheduledTaskByCode(String code) throws ServiceException {
        Integer count = jdbcTemplate.queryForObject("select count(*) from scheduled_task where code = ?", Integer.class, code);
        if (!count.equals(1)) {
            throw new ServiceException(messageSource.getMessage("scheduled_task_not_found_for_code", new Object[]{code}, LocaleContextHolder.getLocale()));
        }
        String query = "select * from scheduled_task where code = ?";
        return jdbcTemplate.queryForObject(query, new Object[]{code},
                new RowMapper<ScheduledTask>() {
            @Override
            public ScheduledTask mapRow(ResultSet rs, int i) throws SQLException {
                return new ScheduledTask(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("code"),
                        rs.getString("description"),
                        rs.getBoolean("enabled"));
            }
        });
    }

    @Override
    public AzureMeter azureMeter(String id) {
        int count = jdbcTemplate.queryForObject("select count(*) from azure_meter where id = ?", Integer.class, id);
        if (count == 0) {
            log.info(messageSource.getMessage("azuremeter_not_found_for_id", new Object[]{id}, LocaleContextHolder.getLocale()));
            return null;
        }
        String query = "select * from azure_meter where id = ?";
        AzureMeter meter = jdbcTemplate.queryForObject(query, new Object[]{id},
                new RowMapper<AzureMeter>() {
            @Override
            public AzureMeter mapRow(ResultSet rs, int i) throws SQLException {
                AzureMeter meter = new AzureMeter();
                meter.setId(rs.getString("id"));
                meter.setName(rs.getString("name"));
                meter.setCategory(rs.getString("category"));
                meter.setSubcategory(rs.getString("subcategory"));
                meter.setRegion(rs.getString("region"));
                meter.setUnit(rs.getString("unit"));
                meter.setIncludedQuantity(rs.getBigDecimal("included_quantity"));
                meter.setEffectiveDate(azureZuluDateTimeFormat.print(new DateTime(rs.getDate("effective_date"))));
                return meter;
            }
        });
        if (meter != null) {
            DecimalMap<String, BigDecimal> rates = new DecimalMap<String, BigDecimal>();
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("meter_id", meter.getId());
            String rateQuery = "select rate_key, value from azure_meter_rate where azure_meter_id = :meter_id";
            SqlRowSet srs = namedJdbcTemplate.queryForRowSet(rateQuery, params);
            while (srs.next()) {
                rates.put(srs.getString("rate_key"), srs.getBigDecimal("value"));
            }
            meter.setRates(rates);
        }
        return meter;
    }

    @Override
    @Transactional
    public void saveAzureMeter(AzureMeter meter, String locale, String currency, Boolean taxIncluded) {
        if (meter.getId() == null) {
            log.warn("meter id null... wth");
        }
        Integer count = jdbcTemplate.queryForObject("select count(*) from azure_meter where id = ?", Integer.class, meter.getId());
        if (count > 0) {
            // for now, ignore
            //updateAzureMeter(meter, locale, currency, taxIncluded);
            return;
        }
        try {
            SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
            jdbcInsert.withTableName("azure_meter");
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("id", meter.getId());
            if (meter.getName() != null) {
                params.put("name", meter.getName());
            }
            if (meter.getCategory() != null) {
                params.put("category", meter.getCategory());
            }
            if (meter.getSubcategory() != null) {
                params.put("subcategory", meter.getSubcategory());
            }
            if (meter.getRegion() != null) {
                params.put("region", meter.getRegion());
            }
            if (meter.getUnit() != null) {
                params.put("unit", meter.getUnit());
            }
            if (meter.getIncludedQuantity() != null) {
                params.put("included_quantity", meter.getIncludedQuantity());
            }
            if (currency != null) {
                params.put("currency", currency);
            }
            if (taxIncluded != null) {
                params.put("tax_included", taxIncluded);
            }
            if (locale != null) {
                params.put("locale", locale);
            }
            if (meter.getEffectiveDate() != null) {
                DateTime effectiveDate = azureZuluDateTimeFormat.parseDateTime(meter.getEffectiveDate());
                if (effectiveDate.isBefore(mysqlBeginTime)) {
                    effectiveDate = azureZuluDateTimeFormat.parseDateTime("2001-01-01T00:00:00Z");
                }
                params.put("effective_date", effectiveDate.toDate());
            }
            jdbcInsert.execute(new MapSqlParameterSource(params));
            
            if (meter.getRates() != null && !meter.getRates().isEmpty()) {
                List<Object[]> inserts = new ArrayList<Object[]>();
                for (Map.Entry<String, BigDecimal> entry : meter.getRates().entrySet()) {
                    inserts.add(new Object[]{meter.getId(), entry.getKey(), entry.getValue()});
                }
                jdbcTemplate.batchUpdate("insert into azure_meter_rate (azure_meter_id, rate_key, value) values (?, ?, ?)", inserts);
            }
        } catch (Exception any) {
            log.error(any.getMessage());
        }
    }

    @Override
    @Transactional
    public void updateAzureMeter(AzureMeter meter, String locale, String currency, Boolean taxIncluded) {
        if (meter.getId() == null) {
            log.warn("meter ID was null... wth");
        }
        DateTime effectiveDate = azureZuluDateTimeFormat.parseDateTime(meter.getEffectiveDate());
        Integer count = jdbcTemplate.queryForObject("select count(*) from device where id = ?", Integer.class, meter.getId());
        if (!count.equals(1)) {
            log.warn("1 <> count of AzureMeter with the id {}", meter.getId());
        }
        
        try {
            int updated = jdbcTemplate.update("update azure_meter set name = ?, category = ?, subcategory = ?, region = ?, unit = ?, included_quantity = ?,"
                    + " currency = ?, tax_included = ?, locale = ?, effective_date = ? where id = ?",
                    new Object[]{meter.getName(), meter.getCategory(), meter.getSubcategory(), meter.getRegion(), meter.getUnit(), meter.getIncludedQuantity(),
                        currency, taxIncluded, locale, effectiveDate.toDate(), meter.getId()});
            log.info("Updated [{}] meter for meter ID [{}]", new Object[]{updated, meter.getId()});
        } catch (Exception any) {
            log.error(any.getMessage());
        }
    }

    @Override
    public BigDecimal azureOfferTermsSum(String locale, String currency, String id, String azureUsageDate) {
        DateTime usageDate = azureUsageDateTimeFormat.parseDateTime(azureUsageDate);
        BigDecimal totalDiscount = BigDecimal.ZERO;
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("locale", locale);
        params.put("currency", currency);
        params.put("usageDate", usageDate.toDate());
        params.put("id", id);
        
        List<DistinctOfferTerm> meterExcluded = namedJdbcTemplate.query("select distinct name, discount, effective_date from azure_offer_term"
                + " where locale = :locale and currency = :currency and azure_meter_id = :id and effective_date < :usageDate", params,
                new RowMapper<DistinctOfferTerm>() {
            @Override
            public DistinctOfferTerm mapRow(ResultSet rs, int i) throws SQLException {
                DistinctOfferTerm term = new DistinctOfferTerm();
                term.setName(rs.getString("name"));
                term.setDiscount(rs.getBigDecimal("discount"));
                term.setEffectiveDate(new DateTime(rs.getDate("effective_date")));
                return term;
            }
        });
        List<DistinctOfferTerm> meterIncluded = namedJdbcTemplate.query("select distinct name, discount, effective_date from azure_offer_term"
                + " where locale = :locale and currency = :currency and azure_meter_id != :id and effective_date < :usageDate", params,
                new RowMapper<DistinctOfferTerm>() {
            @Override
            public DistinctOfferTerm mapRow(ResultSet rs, int i) throws SQLException {
                DistinctOfferTerm term = new DistinctOfferTerm();
                term.setName(rs.getString("name"));
                term.setDiscount(rs.getBigDecimal("discount"));
                term.setEffectiveDate(new DateTime(rs.getDate("effective_date")));
                return term;
            }
        });
        for (DistinctOfferTerm term : meterIncluded) {
            if (!meterExcluded.contains(term)) {
                totalDiscount = totalDiscount.add(term.getDiscount());
            }
        }
        return totalDiscount;
    }

    @Override
    public List<BigDecimal> azureOfferTerms(String locale, String currency, String id, String azureUsageDate) {
        DateTime usageDate = azureUsageDateTimeFormat.parseDateTime(azureUsageDate).withZone(DateTimeZone.UTC);
        return jdbcTemplate.queryForList("select discount from azure_offer_term where locale = ? and currency = ?"
                + " and azure_meter_id != ? and effective_date < ?", BigDecimal.class, new Object[]{locale, currency, id, usageDate.toDate()});
    }
    
    class DistinctOfferTerm {

        private String name;
        private BigDecimal discount;
        private DateTime effectiveDate;
        
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public BigDecimal getDiscount() {
            return discount;
        }

        public void setDiscount(BigDecimal discount) {
            this.discount = discount;
        }

        public DateTime getEffectiveDate() {
            return effectiveDate;
        }

        public void setEffectiveDate(DateTime effectiveDate) {
            this.effectiveDate = effectiveDate;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 67 * hash + Objects.hashCode(this.name);
            hash = 67 * hash + Objects.hashCode(this.discount);
            hash = 67 * hash + Objects.hashCode(this.effectiveDate);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final DistinctOfferTerm other = (DistinctOfferTerm) obj;
            if (!Objects.equals(this.name, other.name)) {
                return false;
            }
            if (!Objects.equals(this.discount, other.discount)) {
                return false;
            }
            if (!Objects.equals(this.effectiveDate, other.effectiveDate)) {
                return false;
            }
            return true;
        }
    }
    
    @Override
    @Transactional
    public void saveAzureOfferTerms(AzureOfferTerm term, String locale, String currency) {
        
        try {
            if (term.getExcludedMeterIds() != null && !term.getExcludedMeterIds().isEmpty()) {
                for (String meterId : term.getExcludedMeterIds()) {
                    saveAzureOfferTerm(locale, currency, term.getName(), term.getDiscount(), term.getEffectiveDate(), meterId);
                }
            } else {
                saveAzureOfferTerm(locale, currency, term.getName(), term.getDiscount(), term.getEffectiveDate(), null);
            }
        } catch (Exception any) {
            log.error(any.getMessage());
        }
    }

    private void saveAzureOfferTerm(String locale, String currency, String name, BigDecimal discount, String effectiveDate, String meterId) {
        try {
            SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
            jdbcInsert.withTableName("azure_offer_term");
            Map<String, Object> params = new HashMap<String, Object>();
            if (locale != null) {
                params.put("locale", locale);
            }
            if (currency != null) {
                params.put("currency", currency);
            }
            if (name != null) {
                params.put("name", name);
            }
            if (discount != null) {
                params.put("discount", discount);
            }
            if (effectiveDate != null) {
                DateTime parsedEffectiveDate = azureOfferTermDateTimeFormat.parseDateTime(effectiveDate);
                if (parsedEffectiveDate.isBefore(mysqlBeginTime)) {
                    parsedEffectiveDate = azureOfferTermDateTimeFormat.parseDateTime("2001-01-01T00:00:00");
                }
                params.put("effective_date", parsedEffectiveDate.toDate());
            }
            if (meterId != null) {
                params.put("azure_meter_id", meterId);
            }
            jdbcInsert.execute(new MapSqlParameterSource(params));
        } catch (Exception any) {
            log.error(any.getMessage());
        }
    }

    @Override
    public void deleteAzureOfferTerms(String locale, String currency) {
        try {
            int updated = jdbcTemplate.update("delete from azure_offer_term where locale = ? and currency = ?", new Object[]{locale, currency});
            log.info("Deleted [{}] AzureOfferTerms for locale [{}] and currency [{}]", new Object[]{updated, locale, currency});
        } catch (Exception any) {
            log.warn(any.getMessage());
        }
    }
    
    @Override
    public SubscriptionUplift subscriptionUplift(Long id) throws ServiceException {
    	Integer count = jdbcTemplate.queryForObject("select count(*) from subscription_uplift where id = ?", Integer.class, id);
        if (!count.equals(1)) {
            throw new ServiceException(messageSource.getMessage("azureuplift_not_found_for_id", new Object[]{id}, LocaleContextHolder.getLocale()));
        }
        String query = "select * from subscription_uplift where id = ?";
        return jdbcTemplate.queryForObject(query, new Object[]{id},
                new RowMapper<SubscriptionUplift>() {
            @Override
            public SubscriptionUplift mapRow(ResultSet rs, int i) throws SQLException {
                return new SubscriptionUplift(
                        rs.getLong("id"),
                        rs.getString("code"),
                        rs.getString("description"),
                        rs.getBigDecimal("uplift"),
                        (rs.getString("uplift_type") == null) ? null : SubscriptionUplift.UpliftType.valueOf(rs.getString("uplift_type")),
                        rs.getBoolean("active"),
                        rs.getDate("start_date"),
                        rs.getDate("end_date"));
            }
        });
    }
    
    @Override
    public SubscriptionUplift subscriptionUpliftByCode(String code) throws ServiceException {
    	Integer count = jdbcTemplate.queryForObject("select count(*) from subscription_uplift where code = ?", Integer.class, code);
        if (!count.equals(1)) {
            throw new ServiceException(messageSource.getMessage("azureuplift_not_found_for_code", new Object[]{code}, LocaleContextHolder.getLocale()));
        }
        String query = "select * from subscription_uplift where code = ? and active = true";
        return jdbcTemplate.queryForObject(query, new Object[]{code},
                new RowMapper<SubscriptionUplift>() {
            @Override
            public SubscriptionUplift mapRow(ResultSet rs, int i) throws SQLException {
            	return new SubscriptionUplift(
                        rs.getLong("id"),
                        rs.getString("code"),
                        rs.getString("description"),
                        rs.getBigDecimal("uplift"),
                        (rs.getString("uplift_type") == null) ? null : SubscriptionUplift.UpliftType.valueOf(rs.getString("uplift_type")),
                        rs.getBoolean("active"),
                        rs.getDate("start_date"),
                        rs.getDate("end_date"));
            }
        });
    }
    
    
    private static final String BASE_SPLA_COST_QUERY = "select scc.id, scc.name, scc.alt_id, scc.cost, scc.vendor, scc.active, scc.type, scc.expense_category_id from spla_cost_catalog scc";
    
    private class SPLACostCatalogRowMapper implements RowMapper {
        public SPLACost mapRow(ResultSet rs, int i) throws SQLException {
            SPLACost splaCost = new SPLACost(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("alt_id"),
                    rs.getBigDecimal("cost"),
                    SPLACost.Vendor.valueOf(rs.getString("vendor")),
                    rs.getBoolean("active"),
                    SPLACost.Type.valueOf(rs.getString("type")),
                    rs.getInt("expense_category_id"));
            return splaCost;
        }
    }
    
    @Override
    public List<SPLACost> splaCosts(Boolean active, Boolean includeDevices) {
        List<SPLACost> records = (active == null ?
                jdbcTemplate.query(BASE_SPLA_COST_QUERY, new SPLACostCatalogRowMapper()) :
                jdbcTemplate.query(BASE_SPLA_COST_QUERY + " where scc.active = ?", new Object[]{active}, new SPLACostCatalogRowMapper()));
        
        if(includeDevices) {
	        for(SPLACost record : records) {
	        	List<Device> devices = findDevicesForSPLACost(record.getId());
	        	if(devices != null && devices.size() > 0) {
	        		record.setDevices(devices);
	        	}
	        }
        }
        
        Collections.sort(records);
        return records;
    }
    
    private void validateSPLACost(SPLACost splaCost) throws ServiceException {
    	if(StringUtils.isEmpty(splaCost.getName())) {
    		throw new ServiceException(messageSource.getMessage("validation_error_spla_name", null, LocaleContextHolder.getLocale()));
    	}
    	
    	if(splaCost.getCost() == null) {
    		throw new ServiceException(messageSource.getMessage("validation_error_spla_cost", null, LocaleContextHolder.getLocale()));
    	}
    	
    	if(splaCost.getType() == null) {
    		throw new ServiceException(messageSource.getMessage("validation_error_spla_type", null, LocaleContextHolder.getLocale()));
    	}
    	
    	if(splaCost.getVendor() == null) {
    		throw new ServiceException(messageSource.getMessage("validation_error_spla_vendor", null, LocaleContextHolder.getLocale()));
    	}
    }
    
    @Override
    public Long saveSPLACost(SPLACost splaCost) throws ServiceException {
    	if (splaCost.getId() != null) {
            updateSPLACost(splaCost);
            return splaCost.getId();
        }
    	
        validateSPLACost(splaCost);
        try {
            SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
            jdbcInsert.withTableName("spla_cost_catalog").usingGeneratedKeyColumns("id");
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("name", splaCost.getName());
            params.put("alt_id", splaCost.getAltId());
            params.put("cost", splaCost.getCost());
            params.put("active", splaCost.getActive());
            params.put("vendor", splaCost.getVendor().name());
            params.put("type", splaCost.getType().name());
            params.put("expense_category_id", splaCost.getExpenseCategoryId());
            params.put("created_by", authenticatedUser());
            
            Number pk = jdbcInsert.executeAndReturnKey(new MapSqlParameterSource(params));
            return (Long) pk;
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_spla_insert", new Object[]{any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    }
    
    @Override
    public void updateSPLACost(SPLACost splaCost) throws ServiceException {
    	if (splaCost.getId() == null) {
            throw new ServiceException(messageSource.getMessage("validation_error_spla_id", null, LocaleContextHolder.getLocale()));
        }
    	validateSPLACost(splaCost);
        try {
            int updated = jdbcTemplate.update("update spla_cost_catalog set name = ?, alt_id = ?,"
                    + " cost = ?, vendor = ?, active = ?, type = ?, expense_category_id = ?,"
                    + " updated = ?, updated_by = ?"
                    + " where id = ?",
                    new Object[]{splaCost.getName(), splaCost.getAltId(), splaCost.getCost(), splaCost.getVendor().name(), splaCost.getActive(), splaCost.getType().name(), splaCost.getExpenseCategoryId(),
                new DateTime().withZone(DateTimeZone.forID(TZID)).toDate(), authenticatedUser(),
                splaCost.getId()});
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_spla_update", new Object[]{splaCost.getId(), any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    }
    
    @Override
    public void deleteSPLACost(Long id) throws ServiceException {
    	SPLACost existing = splaCost(id);
        if(existing == null) {
        	throw new ServiceException("No SPLA Cost record found with the ID [" + id + "] to delete.");
        }
        try {
            int deleted = jdbcTemplate.update("delete from spla_cost_catalog where id = ?", id);
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_spla_delete", new Object[]{id, any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    }
    
    @Override
    public SPLACost splaCost(Long id) throws ServiceException {
    	Integer count = jdbcTemplate.queryForObject("select count(*) from spla_cost_catalog where id = ?", Integer.class, id);
        if (!count.equals(1)) {
            throw new ServiceException(messageSource.getMessage("costitem_not_found_for_id", new Object[]{id}, LocaleContextHolder.getLocale()));
        }
        
    	String query = BASE_SPLA_COST_QUERY
                + " where scc.id = ?";
        List<SPLACost> records = jdbcTemplate.query(query, new Object[]{id}, new SPLACostCatalogRowMapper());
        if (records.size() > 0) {
            return records.get(0);
        }
        return null;
    }
    
    @Transactional(readOnly = false, rollbackFor = ServiceException.class)
    public void serviceAlignment(ServiceAlign serviceAlign) throws ServiceException {
    	Long deviceId = serviceAlign.getDeviceId();
    	Long serviceId = serviceAlign.getServiceId();
    	String password = serviceAlign.getPassword();
    	
    	if(deviceId == null) {
    		throw new ServiceException(messageSource.getMessage("validation_error_device_id", null, LocaleContextHolder.getLocale()));
    	}
    	
    	if(serviceId == null) {
    		throw new ServiceException(messageSource.getMessage("validation_error_service_id", null, LocaleContextHolder.getLocale()));
    	}
    	
    	if(StringUtils.isBlank(password)) {
    		throw new ServiceException(messageSource.getMessage("validation_error_password", null, LocaleContextHolder.getLocale()));
    	}
    	
    	//I just added a hardcoded password here so if someone with admin access happened upon this page, they still couldn't run this job
    	if(!adminProcessPassword.equals(password)) {
    		throw new ServiceException(messageSource.getMessage("validation_error_password_incorrect", null, LocaleContextHolder.getLocale()));
    	}
    	
    	log.info("Looking up contract services");
    	List<Service> services = contractDaoService.contractServicesForDevice(deviceId);
    	log.info("Found " + services.size() +  " contract services");
    	StringBuffer alignLog = new StringBuffer();
    	for(Service service: services) {
    		if(!StringUtils.isBlank(alignLog.toString())) alignLog.append(", ");
    		alignLog.append(service.getId()).append(":").append(service.getServiceId());
    	}
    	log.info("Log record created.");
    	
    	/*
    	for(Service service: services) {
    		service.setServiceId(serviceId);
    		log.info("Saving Record with ID [" + service.getId() + "]");
    		contractDaoService.saveContractService(service, Boolean.FALSE);
    	}*/
    	
    	try {
            int updated = jdbcTemplate.update("update contract_service cs inner join contract_service_device csd on cs.id = csd.contract_service_id"
            		+ " set cs.service_id = ?, cs.updated = ?, cs.updated_by = ?"
                    + " where csd.device_id = ?",
                    new Object[]{serviceAlign.getServiceId(), new Date(), authenticatedUser(),
                serviceAlign.getDeviceId()});
            log.info("Records Updated: " + updated);
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_service_align_update", new Object[]{deviceId, serviceId, any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    	
    	log.info("Saving log entry to DB");
    	try {
            SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
            jdbcInsert.withTableName("service_align_log").usingGeneratedKeyColumns("id");
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("device_id", deviceId);
            params.put("new_service_id", serviceId);
            params.put("change_details", alignLog.toString());
            params.put("created_by", authenticatedUser());
            params.put("created", new Date());
            Number pk = jdbcInsert.executeAndReturnKey(new MapSqlParameterSource(params));
        } catch (Exception any) {
        	any.printStackTrace();
        	throw new ServiceException(messageSource.getMessage("jdbc_error_service_align_log_insert", null, LocaleContextHolder.getLocale()), any);
        }
    	
    }
}
