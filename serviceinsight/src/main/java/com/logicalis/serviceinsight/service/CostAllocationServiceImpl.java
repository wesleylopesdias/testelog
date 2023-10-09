package com.logicalis.serviceinsight.service;

import com.logicalis.serviceinsight.dao.CostItem;
import com.logicalis.serviceinsight.dao.CostItem.CostFraction;
import com.logicalis.serviceinsight.dao.DeviceExpenseCategory;
import com.logicalis.serviceinsight.dao.Expense;
import com.logicalis.serviceinsight.dao.ExpenseCategory;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.transaction.annotation.Transactional;

import com.logicalis.serviceinsight.data.ContractUpdate;
import com.logicalis.serviceinsight.data.CostAllocation;
import com.logicalis.serviceinsight.data.CostAllocationLineItem;
import com.logicalis.serviceinsight.data.Device;
import com.logicalis.serviceinsight.data.Service;
import com.logicalis.serviceinsight.data.UnallocatedExpense;
import com.microsoft.sqlserver.jdbc.StringUtils;
import java.math.RoundingMode;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

@org.springframework.stereotype.Service
@Transactional(readOnly = false, rollbackFor = ServiceException.class)
public class CostAllocationServiceImpl extends BaseServiceImpl implements CostAllocationService {

	@Autowired
	ApplicationDataDaoService applicationDataDaoService;
	@Autowired
	CostDaoService costDaoService;
        @Autowired
        ContractDaoService contractDaoService;
	
	public static final String COST_ALLOCATION_BASE_QUERY = "select ca.id, ca.month, ca.multi_tenant_total, ca.rent_total, ca.dedicated_total, "
			+ "ca.specific_total, ca.status from cost_allocation ca";
	public static final String COST_ALLOCATION_LINEITEM_BASE_QUERY = "select cali.id, cali.cost_allocation_id, cali.osp_id, cali.device_id, d.description, d.part_number, "
			+ " cali.multi_tenant_allocation, cali.rent_allocation, cali.specific_allocation, cali.infrastructure_note, cali.units, cali.cost_model_price from cost_allocation_lineitem cali"
			+ " inner join device d on d.id = cali.device_id";
        
        private DateTimeFormatter monthYearFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        
	@Override
	public CostAllocation costAllocationForLineItem(Long lineItemId) throws ServiceException {
            return costAllocation(jdbcTemplate.queryForObject(
                    "select cost_allocation_id from cost_allocation_lineitem where id = ?", Long.class,
                    new Object[]{lineItemId}));
        }
	
	@Override
	public CostAllocation costAllocation(Long id) throws ServiceException {
		Integer count = jdbcTemplate.queryForObject("select count(*) from cost_allocation where id = ?", Integer.class, id);
        if (!count.equals(1)) {
            throw new ServiceException(messageSource.getMessage("validation_error_costallocation_not_found_for_id", new Object[]{id}, LocaleContextHolder.getLocale()));
        }
        String query = COST_ALLOCATION_BASE_QUERY + " where ca.id = ?";
        CostAllocation allocation = jdbcTemplate.queryForObject(query, new Object[]{id}, new RowMapper<CostAllocation>() {
            @Override
            public CostAllocation mapRow(ResultSet rs, int i) throws SQLException {
                return new CostAllocation(
                        rs.getLong("id"),
                        rs.getDate("month"),
                        rs.getBigDecimal("multi_tenant_total"),
                        rs.getBigDecimal("rent_total"),
                        rs.getBigDecimal("specific_total"),
                        rs.getBigDecimal("dedicated_total"),
                        null, //line items will be populated separately
                        CostAllocation.Status.valueOf(rs.getString("status")));
            }
        });
        
        List<CostAllocationLineItem> lineItems = costAllocationLineItemsForCostAllocation(allocation.getId());
        allocation.setLineItems(lineItems);
        
        Date month = allocation.getMonth();
        List<UnallocatedExpense> unallocatedExpenses = costDaoService.unallocatedExpensesForMonth(month);
        allocation.setUnallocatedExpenses(unallocatedExpenses);
        
        Date leftDate = new DateTime(month).withZone(DateTimeZone.forID(TZID)).withDayOfMonth(1).withTimeAtStartOfDay().toDate();
        Date rightDate = new DateTime(leftDate).withTimeAtStartOfDay().plusHours(23).plusMinutes(59).plusSeconds(59).withZone(DateTimeZone.forID(TZID)).toDate();
        List<CostItem> dedicatedCosts = contractDaoService.findCostItemsByCustomerIdAndTypeAndPeriod(CostItem.CostType.depreciated, CostItem.CostSubType.dedicated, null, leftDate, rightDate);
        allocation.setDedicatedCosts(dedicatedCosts);
        		
        allocation = calculate(allocation);
        
        return allocation;
	}
	
	@Override
	public CostAllocation costAllocationByMonth(Date month) throws ServiceException {
		String query = COST_ALLOCATION_BASE_QUERY + " where ca.month = ?";
        List<CostAllocation> allocations = jdbcTemplate.query(query, new Object[]{month}, new RowMapper<CostAllocation>() {
            @Override
            public CostAllocation mapRow(ResultSet rs, int i) throws SQLException {
                return new CostAllocation(
                        rs.getLong("id"),
                        rs.getDate("month"),
                        rs.getBigDecimal("multi_tenant_total"),
                        rs.getBigDecimal("rent_total"),
                        rs.getBigDecimal("specific_total"),
                        rs.getBigDecimal("dedicated_total"),
                        null, //line items will be populated separately
                        CostAllocation.Status.valueOf(rs.getString("status")));
            }
        });
        
        
        if (allocations == null || allocations.isEmpty()) {
            return null;
        }
        
        // there should only be one...
        CostAllocation allocation = allocations.get(0);
        List<CostAllocationLineItem> lineItems = costAllocationLineItemsForCostAllocation(allocation.getId());
        allocation.setLineItems(lineItems);
        
        List<UnallocatedExpense> unallocatedExpenses = costDaoService.unallocatedExpensesForMonth(month);
        allocation.setUnallocatedExpenses(unallocatedExpenses);
        
        Date leftDate = new DateTime(month).withZone(DateTimeZone.forID(TZID)).withDayOfMonth(1).withTimeAtStartOfDay().toDate();
        Date rightDate = new DateTime(leftDate).withTimeAtStartOfDay().plusHours(23).plusMinutes(59).plusSeconds(59).withZone(DateTimeZone.forID(TZID)).toDate();
        List<CostItem> dedicatedCosts = contractDaoService.findCostItemsByCustomerIdAndTypeAndPeriod(CostItem.CostType.depreciated, CostItem.CostSubType.dedicated, null, leftDate, rightDate);
        allocation.setDedicatedCosts(dedicatedCosts);
        
        allocation = calculate(allocation);
        
        return allocation;
	}
	
	@Override
	public Long saveCostAllocation(CostAllocation costAllocation) throws ServiceException {
		if (costAllocation.getId() != null) {
            updateCostAllocation(costAllocation);
            return costAllocation.getId();
        }
		
		validateCostAllocation(costAllocation);
		
        try {
            SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
            jdbcInsert.withTableName("cost_allocation").usingGeneratedKeyColumns("id");
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("multi_tenant_total", costAllocation.getMultiTenantTotal());
            params.put("rent_total", costAllocation.getRentTotal());
            params.put("dedicated_total", costAllocation.getDedicatedTotal());
            params.put("specific_total", costAllocation.getSpecificTotal());
            params.put("status", costAllocation.getStatus().name());
            params.put("month", costAllocation.getMonth());
            params.put("created", new DateTime().withZone(DateTimeZone.forID(TZID)).toDate());
            params.put("created_by", authenticatedUser());
            Number pk = jdbcInsert.executeAndReturnKey(new MapSqlParameterSource(params));
            
            Long costAllocationId = (Long) pk;
            List<CostAllocationLineItem> lineItems = costAllocation.getLineItems();
            if(lineItems != null && lineItems.size() > 0) {
            	for(CostAllocationLineItem lineItem: lineItems) {
            		lineItem.setCostAllocationId(costAllocationId);
            		saveCostAllocationLineItem(lineItem);
            	}
            }
            
            return (Long) pk;
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_costallocation_insert", new Object[]{any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
	}
	
	private void validateCostAllocation(CostAllocation costAllocation) throws ServiceException {
		if(costAllocation.getMonth() == null) {
			throw new ServiceException(messageSource.getMessage("validation_error_costallocation_month", null, LocaleContextHolder.getLocale()));
		}
		
		if(costAllocation.getStatus() == null) {
			throw new ServiceException(messageSource.getMessage("validation_error_costallocation_status", null, LocaleContextHolder.getLocale()));
		}
	}
	
	@Override
	public void updateCostAllocation(CostAllocation costAllocation) throws ServiceException {
		Long costAllocationId = costAllocation.getId();
		if (costAllocationId == null) {
            throw new ServiceException(messageSource.getMessage("validation_error_cost_allocation_id", null, LocaleContextHolder.getLocale()));
        }
        Integer count = jdbcTemplate.queryForObject("select count(*) from cost_allocation where id = ?", Integer.class, costAllocationId);
        if (!count.equals(1)) {
            throw new ServiceException(messageSource.getMessage("validation_error_costallocation_not_found_for_id", new Object[]{costAllocationId}, LocaleContextHolder.getLocale()));
        }
        
        validateCostAllocation(costAllocation);
        
        try {
            int updated = jdbcTemplate.update("update cost_allocation set multi_tenant_total = ?, rent_total = ?, dedicated_total = ?, specific_total = ?, month = ?, "
                    + " status = ?, updated = ?, updated_by = ? where id = ?",
                    new Object[]{costAllocation.getMultiTenantTotal(), costAllocation.getRentTotal(), costAllocation.getDedicatedTotal(), costAllocation.getSpecificTotal(), 
                    		costAllocation.getMonth(), costAllocation.getStatus().name(), new DateTime().withZone(DateTimeZone.forID(TZID)).toDate(),
                authenticatedUser(), costAllocationId});
            
            int deleted = jdbcTemplate.update("delete from cost_allocation_lineitem where cost_allocation_id = ?", costAllocationId);
            
            List<CostAllocationLineItem> lineItems = costAllocation.getLineItems();
            if(lineItems != null && lineItems.size() > 0) {
            	for(CostAllocationLineItem lineItem: lineItems) {
            		lineItem.setCostAllocationId(costAllocationId);
            		saveCostAllocationLineItem(lineItem);
            	}
            }
            
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_costallocation_update", new Object[]{costAllocationId, any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
	}
	
	@Override
	public void importCostAllocation(CostAllocation costAllocation, Date importMonth) throws ServiceException {
		CostAllocation importAllocation = costAllocationByMonth(importMonth);
		
		if(importMonth == null) {
			throw new ServiceException(messageSource.getMessage("validation_error_costallocation_import_month_required", null, LocaleContextHolder.getLocale()));
		}
		
		if(importAllocation == null) {
			throw new ServiceException(messageSource.getMessage("validation_error_costallocation_import_month_null", null, LocaleContextHolder.getLocale()));
		}
		
		if(importMonth.equals(costAllocation.getMonth())) {
			throw new ServiceException(messageSource.getMessage("validation_error_costallocation_import_date_equal", null, LocaleContextHolder.getLocale()));
		}
		
		CostAllocation newAllocation = costAllocationByMonth(costAllocation.getMonth());
		Long costAllocationId = null;
		if(newAllocation != null) {
			costAllocationId = newAllocation.getId();
		} else {
			costAllocationId = saveCostAllocation(costAllocation);
		}
		
		List<Device> devices = applicationDataDaoService.findDevicesForCostAllocation(costAllocation.getMonth());
		
		for(CostAllocationLineItem lineItem: importAllocation.getLineItems()) {
			CostAllocationLineItem newItem = new CostAllocationLineItem();
			newItem.setCostAllocationId(costAllocationId);
			newItem.setDeviceId(lineItem.getDeviceId());
			newItem.setOspId(lineItem.getOspId());
			newItem.setMultiTenantAllocation(lineItem.getMultiTenantAllocation());
			newItem.setRentAllocation(lineItem.getRentAllocation());
			newItem.setSpecificAllocation(lineItem.getSpecificAllocation());
			newItem.setInfrastructureNote(lineItem.getInfrastructureNote());
			newItem.setCostModelPerUnit(lineItem.getCostModelPerUnit());
			
			Integer unitCount = 0;
			for(Device device: devices) {
				if(device.getId().equals(lineItem.getDeviceId())) {
					unitCount = device.getUnitCount();
					break;
				}
			}
			
			newItem.setUnits(unitCount);
			saveCostAllocationLineItem(newItem);
		}
		
	}
	
	@Override
	public CostAllocation calculate(CostAllocation costAllocation) {
		BigDecimal multiTenantAllocationTotal = new BigDecimal(0);
		BigDecimal multiTenantAllocatedAmountTotal = new BigDecimal(0);
		BigDecimal rentAllocationTotal = new BigDecimal(0);
		BigDecimal rentAllocatedAmountTotal = new BigDecimal(0);
		BigDecimal specificAllocationTotal = new BigDecimal(0);
		BigDecimal specificAllocatedAmountTotal = new BigDecimal(0);
		BigDecimal serviceSpecificAllocatedAmountTotal = new BigDecimal(0);
		
		List<CostAllocationLineItem> lineItems = costAllocation.getLineItems();
		Collections.sort(lineItems, new Comparator<CostAllocationLineItem>() {
		    @Override
		    public int compare(CostAllocationLineItem o1, CostAllocationLineItem o2) {
		        return o1.getOspId().compareTo(o2.getOspId());
		    }
		});
		
		Map<Long, BigDecimal> specificExpenses = new HashMap<Long, BigDecimal>();
		List<UnallocatedExpense> unallocatedExpenses = costAllocation.getUnallocatedExpenses();
		if(unallocatedExpenses != null && unallocatedExpenses.size() > 0) {
			for(UnallocatedExpense unallocatedExpense: unallocatedExpenses) {
				Long ospId = unallocatedExpense.getOspId();
				if(specificExpenses.get(ospId) == null) {
					specificExpenses.put(ospId, new BigDecimal(0));
				}
				specificExpenses.put(ospId, specificExpenses.get(ospId).add(unallocatedExpense.getAmount()));
			}
		}
		
		if(lineItems != null && lineItems.size() > 0) {
			BigDecimal multiTenantTotal = costAllocation.getMultiTenantTotal();
			BigDecimal rentTotal = costAllocation.getRentTotal();
			
			for(CostAllocationLineItem lineItem: lineItems) {
				if (lineItem.getMultiTenantAllocation().compareTo(BigDecimal.ZERO) > 0) {
					BigDecimal multiTenantAllocation = lineItem.getMultiTenantAllocation();
					BigDecimal multiTenantAmount = multiTenantTotal.multiply(multiTenantAllocation.divide(new BigDecimal(100)));
					
					lineItem.setMultiTenantAmount(multiTenantAmount);
					multiTenantAllocatedAmountTotal = multiTenantAllocatedAmountTotal.add(multiTenantAmount);
					multiTenantAllocationTotal = multiTenantAllocationTotal.add(multiTenantAllocation);
				} else {
					lineItem.setMultiTenantAmount(BigDecimal.ZERO);
				}
				
				if (lineItem.getRentAllocation().compareTo(BigDecimal.ZERO) > 0) {
					BigDecimal rentAllocation = lineItem.getRentAllocation();
					BigDecimal rentAmount = rentTotal.multiply(rentAllocation.divide(new BigDecimal(100)));
					
					lineItem.setRentAmount(rentAmount);
					rentAllocatedAmountTotal = rentAllocatedAmountTotal.add(rentAmount);
					rentAllocationTotal = rentAllocationTotal.add(rentAllocation);
				} else {
					lineItem.setRentAmount(BigDecimal.ZERO);
				}
				
				
				BigDecimal serviceSpecificAmount = new BigDecimal(0);
				for(Map.Entry<Long, BigDecimal> specificExpense: specificExpenses.entrySet()) {
					if(specificExpense.getKey().equals(lineItem.getOspId())) {
						serviceSpecificAmount = specificExpense.getValue();
						break;
					}
				}
				lineItem.setServiceSpecificTotal(serviceSpecificAmount);
				//serviceSpecificAllocatedAmountTotal = serviceSpecificAllocatedAmountTotal.add(serviceSpecificAmount);
				
				if (lineItem.getSpecificAllocation().compareTo(BigDecimal.ZERO) > 0) {
					BigDecimal specificAllocation = lineItem.getSpecificAllocation();
					BigDecimal specificAmount = serviceSpecificAmount.multiply(specificAllocation.divide(new BigDecimal(100)));
					
					lineItem.setSpecificAmount(specificAmount);
					specificAllocatedAmountTotal = specificAllocatedAmountTotal.add(specificAmount);
					specificAllocationTotal = specificAllocationTotal.add(specificAllocation);
				} else {
					lineItem.setSpecificAmount(BigDecimal.ZERO);
				}
			}
		}
		
		costAllocation.setLineItems(lineItems);
		costAllocation.setMultiTenantAllocatedAmountTotal(multiTenantAllocatedAmountTotal);
		costAllocation.setMultiTenantAllocationTotal(multiTenantAllocationTotal);
		costAllocation.setRentAllocatedAmountTotal(rentAllocatedAmountTotal);
		costAllocation.setRentAllocationTotal(rentAllocationTotal);
		costAllocation.setSpecificAllocatedAmountTotal(specificAllocatedAmountTotal);
		costAllocation.setSpecificAllocationTotal(specificAllocationTotal);
		costAllocation.setServiceSpecificAllocatedAmountTotal(serviceSpecificAllocatedAmountTotal);
		
		return costAllocation;
	}
	
	@Override
	public List<CostAllocationLineItem> costAllocationLineItemsForCostAllocation(Long costAllocationId) throws ServiceException {
		String query = COST_ALLOCATION_LINEITEM_BASE_QUERY + " where cali.cost_allocation_id = ?";
        List<CostAllocationLineItem> lineItems = jdbcTemplate.query(query, new Object[]{costAllocationId}, new RowMapper<CostAllocationLineItem>() {
            @Override
            public CostAllocationLineItem mapRow(ResultSet rs, int i) throws SQLException {
                return new CostAllocationLineItem(
                        rs.getLong("id"),
                        rs.getLong("cost_allocation_id"),
                        rs.getLong("osp_id"),
                        rs.getLong("device_id"),
                        rs.getString("description"),
                        rs.getString("part_number"),
                        rs.getBigDecimal("multi_tenant_allocation"),
                        rs.getBigDecimal("rent_allocation"),
                        rs.getBigDecimal("specific_allocation"),
                        rs.getString("infrastructure_note"),
                        rs.getInt("units"),
                        rs.getBigDecimal("cost_model_price"));
            }
        });
        
        //map the appropriate service name to each line item
        Map<Long, String> ospServices = new HashMap<Long, String>();
        for(CostAllocationLineItem lineItem: lineItems) {
        	Long ospId = lineItem.getOspId();
        	String serviceName = null;
        	if(ospServices.get(ospId) == null) {
        		Service service = applicationDataDaoService.findAnyServiceByOspId(ospId);
        		if(service != null) {
        			serviceName = service.getName();
        			ospServices.put(ospId, serviceName);
        		}
        	} else {
        		serviceName = ospServices.get(ospId);
        	}
        	lineItem.setServiceName(serviceName);
        }
        
        return lineItems;
	}
	
	@Override
	public Long saveCostAllocationLineItem(CostAllocationLineItem costAllocationLineItem) throws ServiceException {
        
		//validate
		validateCostAllocationLineItem(costAllocationLineItem);
		
        try {
            SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
            jdbcInsert.withTableName("cost_allocation_lineitem").usingGeneratedKeyColumns("id");
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("cost_allocation_id", costAllocationLineItem.getCostAllocationId());
            params.put("osp_id", costAllocationLineItem.getOspId());
            params.put("device_id", costAllocationLineItem.getDeviceId());
            params.put("multi_tenant_allocation", costAllocationLineItem.getMultiTenantAllocation());
            params.put("multi_tenant_amount", costAllocationLineItem.getMultiTenantAmount());
            params.put("rent_allocation", costAllocationLineItem.getRentAllocation());
            params.put("rent_amount", costAllocationLineItem.getRentAmount());
            params.put("specific_allocation", costAllocationLineItem.getSpecificAllocation());
            params.put("specific_amount", costAllocationLineItem.getSpecificAmount());
            params.put("infrastructure_note", costAllocationLineItem.getInfrastructureNote());
            params.put("units", costAllocationLineItem.getUnits());
            params.put("cost_model_price", costAllocationLineItem.getCostModelPerUnit());
            params.put("created", new DateTime().withZone(DateTimeZone.forID(TZID)).toDate());
            params.put("created_by", authenticatedUser());
            Number pk = jdbcInsert.executeAndReturnKey(new MapSqlParameterSource(params));
            return (Long) pk;
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_costallocation_lineitem_insert", new Object[]{any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
	}
	
	private void validateCostAllocationLineItem(CostAllocationLineItem costAllocationLineItem) throws ServiceException {
		if(costAllocationLineItem.getCostAllocationId() == null) {
			throw new ServiceException(messageSource.getMessage("validation_error_cost_allocation_id", null, LocaleContextHolder.getLocale()));
		}
		
		if(costAllocationLineItem.getDeviceId() == null) {
			throw new ServiceException(messageSource.getMessage("validation_error_device_id", null, LocaleContextHolder.getLocale()));
		}
		
		if(costAllocationLineItem.getOspId() == null) {
			throw new ServiceException(messageSource.getMessage("validation_error_service_id", null, LocaleContextHolder.getLocale()));
		}
	}
	
	@Override
	public void updateCostAllocationLineItem(CostAllocationLineItem costAllocationLineItem) throws ServiceException {
		validateCostAllocationLineItem(costAllocationLineItem);
		
		try {
            int updated = jdbcTemplate.update("update cost_allocation_lineitem set cost_allocation_id = ?, osp_id = ?, device_id = ?, multi_tenant_allocation = ?, "
                    + " multi_tenant_amount = ?, rent_allocation = ?, rent_amount = ?, specific_allocation = ?, specific_amount = ?, infrastructure_note = ?, units = ?, "
                    + " cost_model_price = ?, updated = ?, updated_by = ? where id = ?",
                    new Object[]{costAllocationLineItem.getCostAllocationId(), costAllocationLineItem.getOspId(), costAllocationLineItem.getDeviceId(), costAllocationLineItem.getMultiTenantAllocation(),
                    		costAllocationLineItem.getMultiTenantAmount(), costAllocationLineItem.getRentAllocation(), costAllocationLineItem.getRentAmount(), 
                    		costAllocationLineItem.getSpecificAllocation(), costAllocationLineItem.getSpecificAmount(), costAllocationLineItem.getInfrastructureNote(), 
                    		costAllocationLineItem.getUnits(), costAllocationLineItem.getCostModelPerUnit(), new DateTime().withZone(DateTimeZone.forID(TZID)).toDate(), authenticatedUser(), costAllocationLineItem.getId()});            
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_costallocation_lineitem_update", new Object[]{costAllocationLineItem.getCostAllocationId(), any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
	}

    @Override
    public void generateCostItemsFromCostAllocation(Long costAllocationId) throws ServiceException {
        
        CostAllocation ca = validateCostAllocation(costAllocationId);
        
        for (CostAllocationLineItem li : ca.getLineItems()) {
            Device device = applicationDataDaoService.device(li.getDeviceId());
            if (device == null) {
                throw new ServiceException(messageSource.getMessage("device_not_found_for_id", new Object[]{li.getDeviceId()}, LocaleContextHolder.getLocale()));
            }
            if (!device.getCostAllocationOption()) {
                throw new ServiceException(messageSource.getMessage("validation_error_cost_allocation_option", new Object[]{device.getPartNumber(), li.getId()}, LocaleContextHolder.getLocale()));
            }
            /**
             * Devices participating in cost allocation are REQUIRED to have only
             * ONE related expense/cost category...
             */
            List<DeviceExpenseCategory> costMappings = device.getCostMappings();
            if (costMappings == null || costMappings.size() == 0) {
                throw new ServiceException(messageSource.getMessage("validation_error_device_expense_category_missing", new Object[]{device.getPartNumber()}, LocaleContextHolder.getLocale()));
            }
            
            DeviceExpenseCategory allocationCostCategoryRef = null;
            Integer allocationCategoryCount = 0;
    		for(DeviceExpenseCategory mapping: device.getCostMappings()) {
    			if(mapping.getAllocationCategory()) {
    				allocationCostCategoryRef = mapping;
    				allocationCategoryCount++;
    			}
    		}
            
    		if(allocationCategoryCount == 0) {
    			throw new ServiceException(messageSource.getMessage("validation_error_cost_allocation_cost_allocation_category_required", new Object[]{device.getPartNumber()}, LocaleContextHolder.getLocale()));
    		}
    		
            if(allocationCategoryCount > 1) {
                throw new ServiceException(messageSource.getMessage("validation_error_cost_allocation_cost_allocation_category_size", new Object[]{device.getPartNumber()}, LocaleContextHolder.getLocale()));
            }
            
            ExpenseCategory allocationCostCategory = contractDaoService.expenseCategory(allocationCostCategoryRef.getExpenseCategoryId());
            
            if (li.getRentAmount() != null && li.getRentAmount().compareTo(BigDecimal.ZERO) > 0) {
                Expense expense = new Expense();
                expense.setName("Automated: " + device.getDescription() + ", " + li.getServiceName());
                expense.setDescription(device.getPartNumber() + ", " + device.getDescription() + ", " + li.getServiceName());
                expense.setExpenseType(Expense.ExpenseType.cost);
                expense.setAmount(li.getRentAmount());
                expense.setAltId("calloc_"+String.valueOf(li.getId()));
                expense.setQuantity(1);
                
                CostItem costItem = new CostItem();
                costItem.setCostType(CostItem.CostType.depreciated);
                costItem.setCostSubType(CostItem.CostSubType.dc_rent);
                costItem.setExpense(expense);
                costItem.setCostAllocationLineItemIdRef(li.getId());
                costItem.setName(expense.getName());
                costItem.setDescription(expense.getDescription());
                costItem.setAmount(expense.getAmount());
                costItem.setApplied(ca.getMonth());
                CostFraction costFraction = new CostFraction();
                costFraction.setExpenseCategory(allocationCostCategory);
                costFraction.setFraction(new BigDecimal(100));
                costItem.addCostFraction(costFraction);
                try {
                    Long ciid = contractDaoService.saveCostItem(costItem);
                } catch (ServiceException se) {
                    throw new ServiceException(messageSource.getMessage("cost_allocation_lineitem_cost_item_save_error",
                            new Object[]{se.getMessage(), li.getId()}, LocaleContextHolder.getLocale()));
                }
            }
            
            if (li.getMultiTenantAmount() != null && li.getMultiTenantAmount().compareTo(BigDecimal.ZERO) > 0) {
                Expense expense = new Expense();
                expense.setName("Automated: " + device.getDescription() + ", " + li.getServiceName());
                expense.setDescription(device.getPartNumber() + ", " + device.getDescription() + ", " + li.getServiceName());
                expense.setExpenseType(Expense.ExpenseType.cost);
                expense.setAmount(li.getMultiTenantAmount());
                expense.setAltId("calloc_"+String.valueOf(li.getId()));
                expense.setQuantity(1);
                
                CostItem costItem = new CostItem();
                costItem.setCostType(CostItem.CostType.depreciated);
                costItem.setCostSubType(CostItem.CostSubType.multi_tenant);
                costItem.setExpense(expense);
                costItem.setCostAllocationLineItemIdRef(li.getId());
                costItem.setName(expense.getName());
                costItem.setDescription(expense.getDescription());
                costItem.setAmount(expense.getAmount());
                costItem.setApplied(ca.getMonth());
                CostFraction costFraction = new CostFraction();
                costFraction.setExpenseCategory(allocationCostCategory);
                costFraction.setFraction(new BigDecimal(100));
                costItem.addCostFraction(costFraction);
                try {
                    Long ciid = contractDaoService.saveCostItem(costItem);
                } catch (ServiceException se) {
                    throw new ServiceException(messageSource.getMessage("cost_allocation_lineitem_cost_item_save_error",
                            new Object[]{se.getMessage(), li.getId()}, LocaleContextHolder.getLocale()));
                }
            }
            
            if (li.getSpecificAmount() != null && li.getSpecificAmount().compareTo(BigDecimal.ZERO) > 0) {
                Expense expense = new Expense();
                expense.setName("Automated: " + device.getDescription() + ", " + li.getServiceName());
                expense.setDescription(device.getPartNumber() + ", " + device.getDescription() + ", " + li.getServiceName());
                expense.setExpenseType(Expense.ExpenseType.cost);
                expense.setAmount(li.getSpecificAmount());
                expense.setAltId("calloc_"+String.valueOf(li.getId()));
                expense.setQuantity(1);
                
                CostItem costItem = new CostItem();
                costItem.setCostType(CostItem.CostType.depreciated);
                costItem.setCostSubType(CostItem.CostSubType.specific_expense);
                costItem.setExpense(expense);
                costItem.setCostAllocationLineItemIdRef(li.getId());
                costItem.setName(expense.getName());
                costItem.setDescription(expense.getDescription());
                costItem.setAmount(expense.getAmount());
                costItem.setApplied(ca.getMonth());
                CostFraction costFraction = new CostFraction();
                costFraction.setExpenseCategory(allocationCostCategory);
                costFraction.setFraction(new BigDecimal(100));
                costItem.addCostFraction(costFraction);
                try {
                    Long ciid = contractDaoService.saveCostItem(costItem);
                } catch (ServiceException se) {
                    throw new ServiceException(messageSource.getMessage("cost_allocation_lineitem_cost_item_save_error",
                            new Object[]{se.getMessage(), li.getId()}, LocaleContextHolder.getLocale()));
                }
            }
        }
        jdbcTemplate.update("update cost_allocation set status = 'processed' where id = ?", new Object[]{costAllocationId});
    }

    private CostAllocation validateCostAllocation(Long costAllocationId) throws ServiceException {
        
        CostAllocation ca = costAllocation(costAllocationId);
        if (ca == null) {
            throw new ServiceException(messageSource.getMessage("cost_allocation_not_found_for_id", new Object[]{costAllocationId}, LocaleContextHolder.getLocale()));
        }
        if (CostAllocation.Status.processed.equals(ca.getStatus())) {
            throw new ServiceException(messageSource.getMessage("cost_allocation_already_processed_for_id",
                    new Object[]{monthYearFormatter.print(new DateTime(ca.getMonth())), costAllocationId}, LocaleContextHolder.getLocale()));
        }
        
        /**
         * validate the allocations for specific amounts (per Service) above $0.00
         */
        List<Long> ospIds = jdbcTemplate.queryForList("select distinct osp_id from cost_allocation_lineitem where cost_allocation_id = ?", Long.class, new Object[]{ca.getId()});
        if (ospIds == null || ospIds.size() == 0) {
            throw new ServiceException(messageSource.getMessage("validation_error_cost_allocation_services_missing", new Object[]{ca.getId()}, LocaleContextHolder.getLocale()));
        }
        for (Long ospId : ospIds) {
            BigDecimal totalAmount = jdbcTemplate.queryForObject("select sum(amount) from unallocated_expense where month = ? and osp_id = ?", BigDecimal.class, new Object[]{ca.getMonth(), ospId});
            if (totalAmount != null && BigDecimal.ZERO.compareTo(totalAmount) <= 0) {
                BigDecimal totalAllocation = jdbcTemplate.queryForObject("select sum(specific_allocation) from cost_allocation_lineitem where cost_allocation_id = ? and osp_id = ?", BigDecimal.class, new Object[]{ca.getId(), ospId});
                if (totalAllocation == null || BigDecimal.ZERO.compareTo(totalAllocation) >= 0) {
                    String serviceName = jdbcTemplate.queryForObject("select distinct name from service where active = true and osp_id = ?", String.class, new Object[]{ospId});
                    throw new ServiceException(messageSource.getMessage("validation_error_cost_allocation_spa_missing", new Object[]{serviceName}, LocaleContextHolder.getLocale()));
                } else if (totalAllocation.compareTo(new BigDecimal(100)) != 0) {
                    String serviceName = jdbcTemplate.queryForObject("select distinct name from service where active = true and osp_id = ?", String.class, new Object[]{ospId});
                    throw new ServiceException(messageSource.getMessage("validation_error_cost_allocation_spa_not_100", new Object[]{serviceName}, LocaleContextHolder.getLocale()));
                }
            }
        }
        
        /**
         * validate the allocations for multi-tenant amounts (per Service) above $0.00
         */
        BigDecimal totalAmount = ca.getMultiTenantAllocatedAmountTotal();
        if (totalAmount != null && BigDecimal.ZERO.compareTo(totalAmount) <= 0) {
            BigDecimal totalAllocation = jdbcTemplate.queryForObject("select sum(multi_tenant_allocation) from cost_allocation_lineitem where cost_allocation_id = ?", BigDecimal.class, new Object[]{ca.getId()});
            if (totalAllocation == null || BigDecimal.ZERO.compareTo(totalAllocation) >= 0) {
                throw new ServiceException(messageSource.getMessage("validation_error_cost_allocation_mta_missing", new Object[]{}, LocaleContextHolder.getLocale()));
            } else if (totalAllocation.compareTo(new BigDecimal(100)) != 0) {
                throw new ServiceException(messageSource.getMessage("validation_error_cost_allocation_mta_not_100", new Object[]{}, LocaleContextHolder.getLocale()));
            }
        }
        
        /**
         * validate the allocations for rent amounts (per Service) above $0.00
         */
        totalAmount = ca.getRentAllocatedAmountTotal();
        if (totalAmount != null && BigDecimal.ZERO.compareTo(totalAmount) <= 0) {
            BigDecimal totalAllocation = jdbcTemplate.queryForObject("select sum(rent_allocation) from cost_allocation_lineitem where cost_allocation_id = ?", BigDecimal.class, new Object[]{ca.getId()});
            if (totalAllocation == null || BigDecimal.ZERO.compareTo(totalAllocation) >= 0) {
                throw new ServiceException(messageSource.getMessage("validation_error_cost_allocation_ra_missing", new Object[]{}, LocaleContextHolder.getLocale()));
            } else if (totalAllocation.compareTo(new BigDecimal(100)) != 0) {
                throw new ServiceException(messageSource.getMessage("validation_error_cost_allocation_ra_not_100", new Object[]{}, LocaleContextHolder.getLocale()));
            }
        }
        return ca;
    }
}
