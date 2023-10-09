package com.logicalis.serviceinsight.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import javax.mail.internet.MimeMessage;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.velocity.VelocityEngineUtils;
import org.springframework.util.StopWatch;

import com.logicalis.serviceinsight.dao.AssetItem;
import com.logicalis.serviceinsight.dao.CostItem;
import com.logicalis.serviceinsight.dao.DeviceExpenseCategory;
import com.logicalis.serviceinsight.dao.Expense;
import com.logicalis.serviceinsight.dao.ExpenseCategory;
import com.logicalis.serviceinsight.dao.ServiceExpenseCategory;
import com.logicalis.serviceinsight.dao.UnitCost;
import com.logicalis.serviceinsight.data.Contract;
import com.logicalis.serviceinsight.data.ContractAdjustment;
import com.logicalis.serviceinsight.data.ContractGroup;
import com.logicalis.serviceinsight.data.ContractInvoice;
import com.logicalis.serviceinsight.data.ContractServiceDetail;
import com.logicalis.serviceinsight.data.ContractServiceSubscription;
import com.logicalis.serviceinsight.data.ContractUpdate;
import com.logicalis.serviceinsight.data.Customer;
import com.logicalis.serviceinsight.data.Device;
import com.logicalis.serviceinsight.data.MicrosoftPriceList;
import com.logicalis.serviceinsight.data.MicrosoftPriceListM365Product;
import com.logicalis.serviceinsight.data.Personnel;
import com.logicalis.serviceinsight.data.PricingSheet;
import com.logicalis.serviceinsight.data.PricingSheetProduct;
import com.logicalis.serviceinsight.data.ScheduledTask;
import com.logicalis.serviceinsight.data.Service;
import com.logicalis.serviceinsight.data.UnitCostDetails;
import com.logicalis.serviceinsight.representation.APIContractService;
import com.logicalis.serviceinsight.representation.APICustomerSubscription;
import com.logicalis.serviceinsight.representation.APIPCRUpdateRequest;
import com.logicalis.serviceinsight.representation.APIPCRUpdateResponse;
import com.logicalis.serviceinsight.representation.APIPricingSheetProduct;
import com.logicalis.serviceinsight.representation.ServiceDetailRecord;
import java.util.Comparator;

/**
 *
 * @author poneil
 */
@Transactional(readOnly = false, rollbackFor = ServiceException.class)
@org.springframework.stereotype.Service
@SuppressWarnings({ "rawtypes", "unchecked", "unused" })
public class ContractDaoServiceImpl extends BaseServiceImpl implements ContractDaoService {

    @Autowired
    ApplicationDataDaoService applicationDataDaoService;
    @Lazy
    @Autowired
    CostDaoService costDaoService;
    @Lazy
    @Autowired
    CostService costService;
    @Lazy
    @Autowired
    PricingSheetService pricingSheetService;
    @Lazy
    @Autowired
    ContractRevenueService contractRevenueService;
    @Lazy
    @Autowired
    MicrosoftPricingService microsoftPricingService;
    @Lazy
    @Autowired
    DocManagementService docManagementService;
    
    @Value("${azure.email.alert.list}")
    private String azureEmailAlertList;
    
    private static final Long OSP_SERVICE_ID_UNDEFINED = 90001L;
    
    private String BASIC_ASSET_ITEM_QUERY = "select assetItem.id, assetItem.name, assetItem.description,"
            + " assetItem.amount, assetItem.quantity, assetItem.part_number, assetItem.sku, assetItem.life,"
            + " assetItem.acquired, assetItem.disposal, assetItem.customer_id, assetItem.contract_id, assetItem.location_id,"
            + " assetItem.created, assetItem.created_by, assetItem.updated, assetItem.updated_by,"
            + " exp.id exp_id, exp.name exp_name, exp.expense_type expense_type, exp.alt_id exp_alt_id, exp.quantity exp_quantity,"
            + " expc.id expc_id, expc.name expc_name, "
            + " aif.cost_fraction, aif.target_utilization, aif.quantity aif_quantity"
            + " from asset_item assetItem"
            + " left outer join expense exp on assetItem.expense_id_ref = exp.id"
            + " left outer join asset_item_fraction aif on aif.asset_item_id = assetItem.id"
            + " left outer join expense_category expc on aif.expense_category_id = expc.id";
    private String BASIC_COST_ITEM_QUERY = "select costItem.id, costItem.name, costItem.description,"
            + " costItem.amount, costItem.quantity, costItem.part_number, costItem.sku,"
            + " costItem.applied, costItem.customer_id, cust.name as 'customerName', costItem.contract_id, ctr.name as 'contractName',"
            + " costItem.location_id, costItem.cost_type, costItem.cost_subtype, costItem.azure_customer_name, costItem.azure_invoice_id, costItem.azure_subscription_id,"
            + " costItem.aws_subscription_id, costItem.device_id, costItem.cost_allocation_lineitem_id_ref, costItem.spla_cost_catalog_id,"
            + " costItem.created, costItem.created_by, costItem.updated, costItem.updated_by,"
            + " exp.id exp_id, exp.name exp_name, exp.expense_type expense_type, exp.alt_id exp_alt_id, exp.quantity exp_quantity,"
            + " expc.id expc_id, expc.name expc_name, "
            + " cif.cost_fraction"
            + " from cost_item costItem"
            + " left outer join customer cust on costItem.customer_id = cust.id"
            + " left outer join contract ctr on costItem.contract_id = ctr.id"
            + " left outer join expense exp on costItem.expense_id_ref = exp.id"
            + " left outer join cost_item_fraction cif on cif.cost_item_id = costItem.id"
            + " left outer join expense_category expc on cif.expense_category_id = expc.id"
            + " left outer join spla_cost_catalog splaCat on costItem.spla_cost_catalog_id = splaCat.id";
    
    private String BASIC_CONTRACT_QUERY = "select contract.customer_id, contract.id, contract.alt_id, contract.job_number, contract.name,"
            + " contract.emgr, contract.sda, count(csvc.service_id) services, contract.signed_date, contract.service_start_date, contract.start_date,"
            + " contract.end_date, contract.archived, contract.sn_sys_id, contract.file_path, contract.renewal_status, contract.renewal_change, contract.renewal_notes"
            + " from contract contract"
            + " left outer join contract_service csvc on csvc.contract_id = contract.id";
    
    @Override
    public Integer saveExpenseCategory(ExpenseCategory expenseCategory) throws ServiceException {
        if (expenseCategory.getId() != null) {
            updateExpenseCategory(expenseCategory);
            return expenseCategory.getId();
        }
        validateExpenseCategory(expenseCategory);
        try {
            SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
            jdbcInsert.withTableName("expense_category").usingGeneratedKeyColumns("id");
            Map<String, Object> params = new HashMap<String, Object>();
            if (expenseCategory.getParent() != null) {
                params.put("parent_id", expenseCategory.getParent().getId());
            }
            params.put("name", expenseCategory.getName());
            params.put("description", expenseCategory.getDescription());
            params.put("created_by", authenticatedUser());
            params.put("target_utilization", expenseCategory.getTargetUtilization());
            params.put("units", expenseCategory.getUnits());
            params.put("labor_split", BigDecimal.ZERO);
            Number pk = jdbcInsert.executeAndReturnKey(new MapSqlParameterSource(params));
            return ((Long) pk).intValue();
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_expensecategory_insert", new Object[]{any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    }

    @Override
    public void updateExpenseCategory(ExpenseCategory expenseCategory) throws ServiceException {
        if (expenseCategory.getId() == null) {
            throw new ServiceException(messageSource.getMessage("validation_error_expensecategory_id", null, LocaleContextHolder.getLocale()));
        }
        Integer count = jdbcTemplate.queryForObject("select count(*) from expense_category where id = ?", Integer.class, expenseCategory.getId());
        if (!count.equals(1)) {
            throw new ServiceException(messageSource.getMessage("expensecategory_not_found_for_id", new Object[]{expenseCategory.getId()}, LocaleContextHolder.getLocale()));
        }
        validateExpenseCategory(expenseCategory);
        
        List<ExpenseCategory> subcategories = expenseCategory.getSubcategories();
        if(subcategories != null && subcategories.size() > 0) {
	        for(ExpenseCategory subcategory : subcategories) {
	        	ExpenseCategory existingCategory = expenseCategory(subcategory.getId());
	        	existingCategory.setLaborSplit(subcategory.getLaborSplit());
	        	updateExpenseCategory(existingCategory);
	        }
        }
        
        try {
            int updated = jdbcTemplate.update("update expense_category set parent_id = ?, name = ?, description = ?, target_utilization = ?, units = ?, labor_split = ?,"
                    + " updated = ?, updated_by = ?"
                    + " where id = ?",
                    new Object[]{(expenseCategory.getParent() == null ? null : expenseCategory.getParent().getId()),
                expenseCategory.getName(), expenseCategory.getDescription(), expenseCategory.getTargetUtilization(), expenseCategory.getUnits(), (expenseCategory.getLaborSplit() == null) ? BigDecimal.ZERO : expenseCategory.getLaborSplit(),
                new DateTime().withZone(DateTimeZone.forID(TZID)).toDate(), authenticatedUser(),
                expenseCategory.getId()});
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_expensecategory_update", new Object[]{expenseCategory.getId(), any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    }

	@Override
    public void deleteExpenseCategory(Integer id) throws ServiceException, RelatedDataException {
        Integer count = jdbcTemplate.queryForObject("select count(*) from expense_category where id = ?", Integer.class, id);
        if (!count.equals(1)) {
            throw new ServiceException(messageSource.getMessage("expensecategory_not_found_for_id", new Object[]{id}, LocaleContextHolder.getLocale()));
        }
        
        ExpenseCategory expenseCategory = expenseCategory(id);
        if(expenseCategory.getLaborSplit().compareTo(BigDecimal.ZERO) > 0) {
        	BigDecimal splitDisplay = expenseCategory.getLaborSplit().multiply(new BigDecimal(100)).setScale(2);
        	throw new ServiceException(messageSource.getMessage("expensecategory_has_labor_split", new Object[]{splitDisplay}, LocaleContextHolder.getLocale()));
        }

        List<CostItem> relatedCostItems = costItemsForExpenseCategory(id);
        List<AssetItem> relatedAssetItems = assetItemsForExpenseCategory(id);
        if ((relatedCostItems != null && !relatedCostItems.isEmpty()) || (relatedAssetItems != null && !relatedAssetItems.isEmpty())) {
            List related = new ArrayList<>();
            String type = "";
            if (relatedAssetItems != null && !relatedAssetItems.isEmpty()) {
                for (AssetItem assetItem : relatedAssetItems) {
                    related.add(assetItem);
                }
                type = AssetItem.class.getSimpleName();
            }
            if (relatedCostItems != null && !relatedCostItems.isEmpty()) {
                for (CostItem costItem : relatedCostItems) {
                    related.add(costItem);
                }
                type = CostItem.class.getSimpleName();
            }
            throw new RelatedDataException(messageSource.getMessage("cannot_delete_related_data_found_for_id", new Object[]{id}, LocaleContextHolder.getLocale()), related, type);
        }
        List<DeviceExpenseCategory> costMappings = applicationDataDaoService.findCostMappingsForExpenseCategory(id);
        if (costMappings != null && costMappings.size() > 0) {
            throw new RelatedDataException(messageSource.getMessage("cannot_delete_related_data_found_for_id",
                    new Object[]{id}, LocaleContextHolder.getLocale()), costMappings, DeviceExpenseCategory.class.getSimpleName());
        }
        try {
            int updated = jdbcTemplate.update("delete from expense_category where id = ?", id);
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_expensecategory_delete", new Object[]{id, any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    }

    @Override
    public ExpenseCategory expenseCategory(Integer id) throws ServiceException {
        Integer count = jdbcTemplate.queryForObject("select count(*) from expense_category where id = ?", Integer.class, id);
        if (!count.equals(1)) {
            throw new ServiceException(messageSource.getMessage("expensecategory_not_found_for_id", new Object[]{id}, LocaleContextHolder.getLocale()));
        }
        String query = "select ec.id, ec.name, ec.description, ec.target_utilization, ec.units, ec.labor_split, ec.created, ec.created_by,"
                + " ec.updated, ec.updated_by, ecp.id p_id, ecp.name p_name, ecp.description p_description, ecp.target_utilization p_target_utilization, ecp.units p_units,"
                + " ecp.labor_split p_labor_split, ecp.created p_created, ecp.created_by p_created_by, ecp.updated p_updated, ecp.updated_by p_updated_by"
                + " from expense_category ec"
                + " left outer join expense_category ecp on ec.parent_id = ecp.id"
                + " where ec.id = ?";
        return jdbcTemplate.queryForObject(query, new Object[]{id},
                new RowMapper<ExpenseCategory>() {
            @Override
            public ExpenseCategory mapRow(ResultSet rs, int i) throws SQLException {
                ExpenseCategory expenseCategory = new ExpenseCategory(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getBigDecimal("target_utilization"),
                        rs.getString("units"),
                        rs.getBigDecimal("labor_split"));

                Integer parentId = rs.getInt("p_id");
                if (parentId > 0) {
                    ExpenseCategory parent = new ExpenseCategory(
                            rs.getInt("p_id"),
                            rs.getString("p_name"),
                            rs.getString("p_description"),
                            rs.getBigDecimal("p_target_utilization"),
                            rs.getString("p_units"),
                            rs.getBigDecimal("p_labor_split"));
                    expenseCategory.setParent(parent);
                }
                expenseCategory.setCreated(rs.getTimestamp("created"));
                expenseCategory.setCreatedBy(rs.getString("created_by"));
                expenseCategory.setUpdated(rs.getTimestamp("updated"));
                expenseCategory.setUpdatedBy(rs.getString("updated_by"));
                return expenseCategory;
            }
        });
    }

    @Override
    public List<ExpenseCategory> expenseCategories() {
        String query = "select ec.id, ec.name, ec.description, ec.target_utilization, ec.units, ec.labor_split, ec.created, ec.created_by,"
                + " ec.updated, ec.updated_by, ecp.id p_id, ecp.name p_name, ecp.description p_description, ecp.target_utilization p_target_utilization, ecp.units p_units,"
                + " ecp.labor_split p_labor_split, ecp.created p_created, ecp.created_by p_created_by, ecp.updated p_updated, ecp.updated_by p_updated_by"
                + " from expense_category ec"
                + " left outer join expense_category ecp on ec.parent_id = ecp.id";
        return jdbcTemplate.query(query, new RowMapper<ExpenseCategory>() {
            @Override
            public ExpenseCategory mapRow(ResultSet rs, int i) throws SQLException {
                ExpenseCategory expenseCategory = new ExpenseCategory(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getBigDecimal("target_utilization"),
                        rs.getString("units"),
                        rs.getBigDecimal("labor_split"));

                Integer parentId = rs.getInt("p_id");
                if (parentId > 0) {
                    ExpenseCategory parent = new ExpenseCategory(
                            rs.getInt("p_id"),
                            rs.getString("p_name"),
                            rs.getString("p_description"),
                            rs.getBigDecimal("p_target_utilization"),
                            rs.getString("p_units"),
                            rs.getBigDecimal("p_labor_split"));
                    expenseCategory.setParent(parent);
                }
                expenseCategory.setCreated(rs.getTimestamp("created"));
                expenseCategory.setCreatedBy(rs.getString("created_by"));
                expenseCategory.setUpdated(rs.getTimestamp("updated"));
                expenseCategory.setUpdatedBy(rs.getString("updated_by"));
                return expenseCategory;
            }
        });
    }

    private void validateExpenseCategory(ExpenseCategory expenseCategory) throws ServiceException {
        if (expenseCategory.getName() == null) {
            throw new ServiceException(messageSource.getMessage("validation_error_expensecategory_name", null, LocaleContextHolder.getLocale()));
        }
        if (expenseCategory.getParent() != null && expenseCategory.getParent().getId() != null && expenseCategory.getParent().getId() > 0) {
            Integer count = jdbcTemplate.queryForObject("select count(*) from expense_category where id = ?", Integer.class, expenseCategory.getParent().getId());
            if (count < 1) {
                throw new ServiceException(messageSource.getMessage("expensecategory_not_found_for_id", new Object[]{expenseCategory.getParent().getId()}, LocaleContextHolder.getLocale()));
            }
            
            count = jdbcTemplate.queryForObject("select count(*) from expense_category where name = ? and parent_id = ?", Integer.class, new Object[]{expenseCategory.getName(), expenseCategory.getParent().getId()});
            if(count > 0) {
	            Integer id = jdbcTemplate.queryForObject("select id from expense_category where name = ? and parent_id = ?", Integer.class, new Object[]{expenseCategory.getName(), expenseCategory.getParent().getId()});
	            if(id != null && !id.equals(expenseCategory.getId())) {
	            	throw new ServiceException(messageSource.getMessage("validation_error_expensecategory_unique_level_name", new Object[]{expenseCategory.getName(), expenseCategory.getParent().getName()}, LocaleContextHolder.getLocale()));
	            }
            }
        }
        if (expenseCategory.getParent() == null) {
            Integer count = jdbcTemplate.queryForObject("select count(*) from expense_category where name = ? and parent_id is null and id != ?", Integer.class, new Object[]{expenseCategory.getName(), expenseCategory.getId()});
            if (count > 0) {
                throw new ServiceException(messageSource.getMessage("validation_error_expensecategory_unique_name", null, LocaleContextHolder.getLocale()));
            }
        }
    }

    @Override
    public Long saveCostItem(CostItem costItem) throws ServiceException {
        if (costItem.getId() != null) {
            updateCostItem(costItem, true);
            return costItem.getId();
        }
        Long expenseId = null;
        if (costItem.getExpense() != null) {
        	costItem.getExpense().setExpenseType(Expense.ExpenseType.cost);
            expenseId = saveExpense(costItem.getExpense());
        }
        validateCostItem(costItem);
        try {
            SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
            jdbcInsert.withTableName("cost_item").usingGeneratedKeyColumns("id");
            Map<String, Object> params = new HashMap<String, Object>();
            if (expenseId != null) {
                params.put("expense_id_ref", expenseId);
            }
            if (costItem.getCostAllocationLineItemIdRef() != null) {
                params.put("cost_allocation_lineitem_id_ref", costItem.getCostAllocationLineItemIdRef());
            }
            params.put("name", costItem.getName());
            params.put("description", costItem.getDescription());
            params.put("amount", costItem.getAmount());
            if (costItem.getQuantity() != null) {
                params.put("quantity", costItem.getQuantity());
            }
            params.put("part_number", costItem.getPartNumber());
            if (costItem.getDeviceId() != null) {
                params.put("device_id", costItem.getDeviceId());
            }
            if (costItem.getSplaId() != null) {
                params.put("spla_cost_catalog_id", costItem.getSplaId());
            }
            params.put("sku", costItem.getSku());
            params.put("applied", costItem.getApplied());
            if(costItem.getCostType() != null) {
            	params.put("cost_type", costItem.getCostType().name());
                if (CostItem.CostType.depreciated.equals(costItem.getCostType())) {
                    params.put("cost_subtype", costItem.getCostSubType().name()); // it was validated not null...
                }
            } else {
            	params.put("cost_type", CostItem.CostType.general);
            }
            if (costItem.getCustomerId() != null) {
                params.put("customer_id", costItem.getCustomerId());
            }
            if (costItem.getContractId() != null) {
                params.put("contract_id", costItem.getContractId());
            }
            if (costItem.getLocationId() != null) {
                params.put("location_id", costItem.getLocationId());
            }
            if (costItem.getAzureCustomerName() != null) {
                params.put("azure_customer_name", costItem.getAzureCustomerName());
            }
            if (costItem.getAzureInvoiceNo() != null) {
                params.put("azure_invoice_id", costItem.getAzureInvoiceNo());
            }
            if (costItem.getAzureSubscriptionNo() != null) {
                params.put("azure_subscription_id", costItem.getAzureSubscriptionNo());
            }
            if (costItem.getAwsSubscriptionNo() != null) {
                params.put("aws_subscription_id", costItem.getAwsSubscriptionNo());
            }
            params.put("created_by", (costItem.getCreatedBy() == null ? authenticatedUser() : costItem.getCreatedBy()));
            Number pk = jdbcInsert.executeAndReturnKey(new MapSqlParameterSource(params));
            if (!costItem.getCostFractions().isEmpty()) {
                generateCostItemFractions(costItem, (Long) pk);
            }
            return (Long) pk;
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_costitem_insert", new Object[]{any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    }

    private void generateCostItemFractions(CostItem costItem, Long pk) throws ServiceException {

        DateTime appliedDate = new DateTime(costItem.getApplied())
                .withZone(DateTimeZone.forID(TZID))
                .withDayOfMonth(1)
                .withTimeAtStartOfDay();
        DateTime endDate = appliedDate
                .plusMonths(1)
                .minusDays(1)
                .withTime(23, 59, 59, 999);

        for (CostItem.CostFraction fraction : costItem.getCostFractions()) {
            try {
                SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
                jdbcInsert.withTableName("cost_item_fraction");
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("expense_category_id", fraction.getExpenseCategory().getId());
                params.put("cost_item_id", pk);
                params.put("cost_fraction", fraction.getFraction());
                int updated = jdbcInsert.execute(new MapSqlParameterSource(params));

                UnitCost uc = costDaoService.unitCostByExpenseCategoryAndDate(costItem.getCustomerId(), fraction.getExpenseCategory().getId(), appliedDate);
                if (uc == null) {
                    uc = new UnitCost();
                    uc.setAppliedDate(appliedDate);
                    uc.setCustomerId(costItem.getCustomerId());
                    uc.setExpenseCategoryId(fraction.getExpenseCategory().getId());
                    Integer deviceTotalCount = costService.deviceTotalDeviceCountWithExpenseCategory(appliedDate, endDate, null, costItem.getCustomerId(), null, uc.getExpenseCategoryId(), null);
                    uc.setDeviceTotalUnits(deviceTotalCount);
                }
                uc.incrementTotalCost(costItem.getAmount().multiply(fraction.getFraction())
                        .divide(new BigDecimal(100)));
                costDaoService.saveUnitCost(uc);
            } catch (Exception any) {
                throw new ServiceException(messageSource.getMessage("jdbc_error_costitem_fraction_insert", new Object[]{any.getMessage()}, LocaleContextHolder.getLocale()), any);
            }
        }
    }

    private void decrementUnitCost(CostItem costItem) throws ServiceException {
        if (costItem.getId() == null) {
            throw new IllegalArgumentException("cost unit costs cannot be subtracted from a NEW cost item");
        }
        DateTime appliedDate = new DateTime(costItem.getApplied())
                .withZone(DateTimeZone.forID(TZID))
                .withDayOfMonth(1)
                .withTimeAtStartOfDay();
        for (CostItem.CostFraction fraction : costItem.getCostFractions()) {
            UnitCost uc = costDaoService.unitCostByExpenseCategoryAndDate(costItem.getCustomerId(), fraction.getExpenseCategory().getId(), appliedDate);
            if (uc != null) {
                uc.decrementTotalCost(costItem.getAmount().multiply(fraction.getFraction())
                        .divide(new BigDecimal(100)));
                costDaoService.saveUnitCost(uc);
            }
        }
    }

    @Override
    public void updateCostItem(CostItem costItem, boolean decrement) throws ServiceException {
        if (costItem.getId() == null) {
            throw new ServiceException(messageSource.getMessage("validation_error_costitem_id", null, LocaleContextHolder.getLocale()));
        }
        CostItem existing = costItem(costItem.getId());
        if (costItem.getExpense() != null) {
        	costItem.getExpense().setExpenseType(Expense.ExpenseType.cost);
            updateExpense(costItem.getExpense());
        }
        validateCostItem(costItem);
        try {
            // if unit costs were corrupted, manually deleted, we'd want to skip decrementing
            if (decrement) {
                decrementUnitCost(existing);
            }
            String costSubType = null;
            if (CostItem.CostType.depreciated.equals(costItem.getCostType())) {
                costSubType = costItem.getCostSubType().name();
            }
            int updated = jdbcTemplate.update("update cost_item set name = ?, description = ?,"
                    + " amount = ?, quantity = ?, part_number = ?, sku = ?, applied = ?, customer_id = ?,"
                    + " contract_id = ?, location_id = ?, cost_type = ?, cost_subtype = ?,"
                    + " updated = ?, updated_by = ?"
                    + " where id = ?",
                    new Object[]{costItem.getName(), costItem.getDescription(),
                costItem.getAmount(), costItem.getQuantity(), costItem.getPartNumber(), costItem.getSku(),
                costItem.getApplied(), costItem.getCustomerId(), costItem.getContractId(), costItem.getLocationId(), costItem.getCostType().name(),
                costSubType, new DateTime().withZone(DateTimeZone.forID(TZID)).toDate(), authenticatedUser(),
                costItem.getId()});
            updateCostItemFractions(costItem);
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_costitem_update", new Object[]{costItem.getId(), any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    }

    private void updateCostItemFractions(CostItem costItem) throws ServiceException {
        try {
            int updated = jdbcTemplate.update("delete from cost_item_fraction where cost_item_id = ?",
                    new Object[]{costItem.getId()});
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_costitem_fraction_delete", new Object[]{costItem.getId(), any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
        generateCostItemFractions(costItem, costItem.getId());
    }

    @Override
    public void deleteCostItem(Long id) throws ServiceException {
        CostItem existing = costItem(id);
        Long expenseId = jdbcTemplate.queryForObject("select expense_id_ref from cost_item where id = ?", Long.class, id);
        try {
            decrementUnitCost(existing);
            int updated = jdbcTemplate.update("delete from cost_item where id = ?", id);
            if (expenseId != null) {
                deleteExpense(expenseId);
            }
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_costitem_delete", new Object[]{id, any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    }

    @Override
    public CostItem costItem(Long id) throws ServiceException {
        Integer count = jdbcTemplate.queryForObject("select count(*) from cost_item where id = ?", Integer.class, id);
        if (!count.equals(1)) {
            throw new ServiceException(messageSource.getMessage("costitem_not_found_for_id", new Object[]{id}, LocaleContextHolder.getLocale()));
        }
        String query = BASIC_COST_ITEM_QUERY
                + " where costItem.id = ?"
                + " order by costItem.id, expc.id";
        List<CostItem> records = jdbcTemplate.query(query, new Object[]{id}, new CostItemRowMapper());
        List<CostItem> costItems = getCostFractionsForRecords(records);
        if (costItems.size() > 0) {
            return costItems.get(0);
        }
        return null;
    }

    @Override
    public List<CostItem> costItems() {
        String query = BASIC_COST_ITEM_QUERY
                + " order by costItem.id, expc.id";
        List<CostItem> records = jdbcTemplate.query(query, new CostItemRowMapper());
        List<CostItem> costItems = getCostFractionsForRecords(records);
        Collections.sort(records, new CostItem.CostItemBasicComparator());
        return costItems;
    }

    @Override
    public List<CostItem> costItemsForExpenseCategory(Integer expenseCategoryId) {
        String query = BASIC_COST_ITEM_QUERY
                + " where cif.expense_category_id = ?"
                + " order by costItem.id, expc.id";
        List<CostItem> records = jdbcTemplate.query(query, new Object[]{expenseCategoryId}, new CostItemRowMapper());
        List<CostItem> costItems = getCostFractionsForRecords(records);
        Collections.sort(records, new CostItem.CostItemBasicComparator());
        return costItems;
    }

    @Override
    public List<CostItem> costItemsForCostTypeAndAppliedDate(CostItem.CostType costType, DateTime costDate) {
        String query = BASIC_COST_ITEM_QUERY
                + " where costItem.cost_type = ?"
                + " and costItem.applied = ?"
                + " order by cust.name, costItem.contract_id,"
                + " costItem.device_id, costItem.spla_cost_catalog_id";
        DateTime appliedDate = costDate
                .withZone(DateTimeZone.forID(TZID))
                .withDayOfMonth(1)
                .withTimeAtStartOfDay();
        List<CostItem> records = jdbcTemplate.query(query, new Object[]{costType.name(), appliedDate.toDate()}, new CostItemRowMapper());
        List<CostItem> costItems = getCostFractionsForRecords(records);
        return costItems;
    }

    @Override
    public List<CostItem> findSPLACosts(DateTime costDate, Long customerId, Long deviceId, Long splaId, String vendor) {
        
        DateTime appliedDate = costDate
                .withZone(DateTimeZone.forID(TZID))
                .withDayOfMonth(1)
                .withTimeAtStartOfDay();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("applied", appliedDate.toDate());
        
        String query = BASIC_COST_ITEM_QUERY
                + " where costItem.cost_type = 'spla'"
                + " and costItem.applied = :applied";
        if (customerId != null && customerId > 0) {
            query += " and cust.id = :custId";
            params.put("custId", customerId);
        }
        if (deviceId != null && deviceId > 0) {
            query += " and costItem.device_id = :devId";
            params.put("devId", deviceId);
        }
        if (splaId != null && splaId > 0) {
            query += " and costItem.spla_cost_catalog_id = :splaId";
            params.put("splaId", splaId);
        }
        if (StringUtils.isNotBlank(vendor)) {
            query += " and splaCat.vendor = :vendor";
            params.put("vendor", vendor);
        }
        query += " order by cust.name, costItem.contract_id,"
                + " costItem.device_id, costItem.spla_cost_catalog_id";
        List<CostItem> records = namedJdbcTemplate.query(query, params, new CostItemRowMapper());
        List<CostItem> costItems = getCostFractionsForRecords(records);
        return costItems;
    }
    
    @Override
    public UnitCostDetails unitCostDetailsForAppliedDate(Long customerId, Integer expenseCategoryId, DateTime costDate) {
        
        UnitCostDetails unitCostDetails = new UnitCostDetails();
        unitCostDetails.setUnitCost(costDaoService.unitCostByExpenseCategoryAndDate(customerId, expenseCategoryId, costDate));
        unitCostDetails.setCosts(costDetailsForDate(customerId, expenseCategoryId, costDate));
        unitCostDetails.setAssets(assetDetailsForDate(customerId, expenseCategoryId, costDate));
        unitCostDetails.setLabor(costService.laborCostsForExpenseCategory(customerId, expenseCategoryId, costDate));
        return unitCostDetails;
    }
    
    @Override
    public List<UnitCostDetails.CostDetail> costDetailsForDate(Long customerId, Integer expenseCategoryId, DateTime costDate) {
        DateTime startDate = costDate
                .withZone(DateTimeZone.forID(TZID))
                .withDayOfMonth(1) // default is beginning of year...
                .withTimeAtStartOfDay();
        DateTime endDate = startDate.dayOfMonth().withMaximumValue().plusHours(23).plusMinutes(59).plusSeconds(59);
        List<CostItem> costItems = findCostItemsForExpenseCategoryAndPeriod(customerId, expenseCategoryId, startDate.toDate(), endDate.toDate());
        List<UnitCostDetails.CostDetail> costItemDetails = new ArrayList<UnitCostDetails.CostDetail>();
        for (CostItem costItem : costItems) {
            UnitCostDetails.CostDetail costDetail = new UnitCostDetails.CostDetail();
            if (costItem.getCostFractions() != null && costItem.getCostFractions().size() == 1) {
                costDetail.setAmount(
                        costItem.getAmount()
                                .multiply(costItem.getCostFractions().get(0).getFraction())
                                .divide(new BigDecimal(100.))); // fractions were stored as %...
            } else {
                log.warn("expected 1 and only 1 cost fraction for cost item for a queried expense category [{}] and cost date {}!",
                        new Object[]{expenseCategoryId, DateTimeFormat.forPattern("yyyy-MM-dd").print(costDate)});
                costDetail.setAmount(BigDecimal.ZERO);
            }
            if (costItem.getCustomerId() != null) {
                try {
                    Customer customer = customer(costItem.getCustomerId());
                    if (customer != null) {
                        costDetail.setCustomer(customer.getName());
                    }
                } catch (Exception ignore) {}
            }
            costDetail.setAppliedDate(new DateTime(costItem.getApplied()));
            costDetail.setDescription(costItem.getName());
            costItemDetails.add(costDetail);
        }
        Collections.sort(costItemDetails);
        return costItemDetails;
    }
    
    @Override
    public List<UnitCostDetails.AssetDetail> assetDetailsForDate(Long customerId, Integer expenseCategoryId, DateTime costDate) {
        DateTime appliedDate = costDate
                .withZone(DateTimeZone.forID(TZID))
                .withDayOfMonth(1) // default is beginning of year...
                .withTimeAtStartOfDay();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("expenseCategoryId", expenseCategoryId);
        params.put("appliedDate", appliedDate.toDate());
        String queryOfTheYear = "select ai.acquired as 'Acquired', ai.name as 'Asset', ai.customer_id as 'CustomerId',"
                + " round(ai.amount * aif.cost_fraction / 100. / ai.life, 2) as 'Depreciation'"
                + " from asset_item ai"
                + " inner join asset_item_fraction aif on aif.asset_item_id = ai.id"
                + " inner join expense_category ec on ec.id = aif.expense_category_id"
                + " inner join unit_cost uc on uc.expense_category_id = aif.expense_category_id"
                + " where ec.id = :expenseCategoryId and uc.applied_date between subdate(ai.acquired, (day(ai.acquired)-1))"
                + " and DATE_ADD(subdate(ai.acquired, (day(ai.acquired)-1)), INTERVAL ai.life MONTH)"
                + " and uc.applied_date = :appliedDate";
        if (customerId != null && customerId > 0) {
            queryOfTheYear += " and ai.customer_id = :custId";
            params.put("custId", customerId);
        } else {
            queryOfTheYear += " and ai.customer_id is null";
        }
        queryOfTheYear += " order by ec.id, uc.applied_date";
        
        List<Map<String, Object>> results = namedJdbcTemplate.queryForList(queryOfTheYear, params);
        List<UnitCostDetails.AssetDetail> assetDetails = new ArrayList<UnitCostDetails.AssetDetail>();
        for (Map<String, Object> result : results) {
            UnitCostDetails.AssetDetail assetDetail = new UnitCostDetails.AssetDetail();
            if (customerId != null && customerId > 0) {
                try {
                    Customer customer = customer(customerId);
                    if (customer != null) {
                        assetDetail.setCustomer(customer.getName());
                    }
                } catch (Exception ignore) {}
            }
            assetDetail.setAcquiredDate(new DateTime((Date) result.get("Acquired")));
            assetDetail.setDescription((String) result.get("Asset"));
            assetDetail.setDepreciation((BigDecimal) result.get("Depreciation"));
            assetDetails.add(assetDetail);
        }
        Collections.sort(assetDetails);
        return assetDetails;
    }

    @Override
    public List<CostItem> findCostItemsForExpenseCategoryAndPeriod(Long customerId, Integer expenseCategoryId, Date leftDate, Date rightDate) {
        if (leftDate == null || rightDate == null) {
            throw new IllegalArgumentException(messageSource.getMessage("missing_arguments",
                    new Object[]{"left and/or right date(s)"}, LocaleContextHolder.getLocale()));
        }
        DateTime leftDateTime = new DateTime(leftDate)
                .withTimeAtStartOfDay()
                .withZone(DateTimeZone.forID(TZID));
        DateTime rightDateTime = new DateTime(rightDate)
                .withTimeAtStartOfDay()
                .plusHours(23).plusMinutes(59).plusSeconds(59)
                .withZone(DateTimeZone.forID(TZID));
        
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("expenseCategoryId", expenseCategoryId);
        params.put("leftDate", leftDateTime.toDate());
        params.put("rightDate", rightDateTime.toDate());
        
        String query = BASIC_COST_ITEM_QUERY
                + " where cif.expense_category_id = :expenseCategoryId"
                + " and costItem.applied between :leftDate and :rightDate";
        if (customerId != null && customerId > 0) {
            query += " and cust.id = :custId";
            params.put("custId", customerId);
        } else {
            query += " and cust.id is null";
        }
        query += " order by costItem.id, expc.id";
        
        List<CostItem> records = namedJdbcTemplate.query(query, params, new CostItemRowMapper());
        List<CostItem> costItems = getCostFractionsForRecords(records);
        Collections.sort(records, new CostItem.CostItemBasicComparator());
        return costItems;
    }

    @Override
    public List<CostItem> findCostItemsByCustomer(Long customerId) {
        String query = BASIC_COST_ITEM_QUERY
                + " where costItem.customer_id = ?"
                + " order by costItem.id, expc.id";
        List<CostItem> records = jdbcTemplate.query(query, new Object[]{customerId}, new CostItemRowMapper());
        List<CostItem> costItems = getCostFractionsForRecords(records);
        Collections.sort(records, new CostItem.CostItemBasicComparator());
        return costItems;
    }

    @Override
    public List<CostItem> findCostItemsByType(CostItem.CostType costType, CostItem.CostSubType costSubType) {
        String query = BASIC_COST_ITEM_QUERY;
        Map<String, Object> params = new HashMap<String, Object>();
        
        if (costType != null) {
            query += " where costItem.cost_type = :costType";
            params.put("costType", costType.name());
        }
        
        if (costSubType != null) {
        	if (costType != null) {
        		query += " and";
        	} else {
        		query += " where";
        	}
            query += " costItem.cost_subtype = :costSubType";
            params.put("costSubType", costSubType.name());
        }
        
        query += " order by costItem.id, expc.id";
        List<CostItem> records = namedJdbcTemplate.query(query, params, new CostItemRowMapper());
        List<CostItem> costItems = getCostFractionsForRecords(records);
        Collections.sort(records, new CostItem.CostItemBasicComparator());
        return costItems;
    }
    
    @Override
    public List<CostItem> findCostItemsByCustomerIdAndTypeAndPeriod(CostItem.CostType costType, CostItem.CostSubType costSubType, Long customerId, Date leftDate, Date rightDate) {
        if (leftDate == null || rightDate == null) {
            throw new IllegalArgumentException(messageSource.getMessage("missing_arguments",
                    new Object[]{"left and/or right date(s)"}, LocaleContextHolder.getLocale()));
        }
        DateTime leftDateTime = new DateTime(leftDate)
                .withTimeAtStartOfDay()
                .withZone(DateTimeZone.forID(TZID));
        DateTime rightDateTime = new DateTime(rightDate)
                .withTimeAtStartOfDay()
                .plusHours(23).plusMinutes(59).plusSeconds(59)
                .withZone(DateTimeZone.forID(TZID));
        String query = BASIC_COST_ITEM_QUERY
                + " where costItem.applied between :leftDate and :rightDate";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("leftDate", leftDateTime.toDate());
        params.put("rightDate", rightDateTime.toDate());
        if (customerId != null && customerId > 0) {
            query += " and costItem.customer_id = :customerId";
            params.put("customerId", customerId);
        }
        if (costType != null) {
            query += " and costItem.cost_type = :costType";
            params.put("costType", costType.name());
        }
        if (costSubType != null) {
            query += " and costItem.cost_subtype = :costSubType";
            params.put("costSubType", costSubType.name());
        }
        query += " order by costItem.id, expc.id";
        List<CostItem> records = namedJdbcTemplate.query(query, params, new CostItemRowMapper());
        List<CostItem> costItems = getCostFractionsForRecords(records);
        Collections.sort(records, new CostItem.CostItemBasicComparator());
        return costItems;
    }

    private class CostItemRowMapper implements RowMapper<CostItem> {

        public CostItem mapRow(ResultSet rs, int i) throws SQLException {
            CostItem costItem = new CostItem(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getBigDecimal("amount"),
                    (rs.getInt("quantity") == 0 ? null : rs.getInt("quantity")),
                    rs.getString("part_number"),
                    rs.getString("sku"),
                    rs.getDate("applied"),
                    (rs.getLong("customer_id") == 0 ? null : rs.getLong("customer_id")),
                    rs.getString("customerName"),
                    (rs.getLong("contract_id") == 0 ? null : rs.getLong("contract_id")),
                    rs.getString("contractName"),
                    (rs.getInt("location_id") == 0 ? null : rs.getInt("location_id")),
            		CostItem.CostType.valueOf(rs.getString("cost_type")));

            // new fields...
            costItem.setAzureCustomerName(rs.getString("azure_customer_name"));
            costItem.setAzureInvoiceNo(rs.getString("azure_invoice_id"));
            costItem.setAzureSubscriptionNo(rs.getString("azure_subscription_id"));
            costItem.setAwsSubscriptionNo(rs.getString("aws_subscription_id"));
            costItem.setDeviceId((rs.getLong("device_id") == 0 ? null : rs.getLong("device_id")));
            String costSubTypeValue = rs.getString("cost_subtype");
            if (costSubTypeValue != null) {
                costItem.setCostSubType(CostItem.CostSubType.valueOf(costSubTypeValue));
            }
            costItem.setCostAllocationLineItemIdRef((rs.getLong("cost_allocation_lineitem_id_ref") == 0 ? null : rs.getLong("cost_allocation_lineitem_id_ref")));
            costItem.setSplaId((rs.getLong("spla_cost_catalog_id") == 0 ? null : rs.getLong("spla_cost_catalog_id")));
            
            costItem.setCreated(rs.getTimestamp("created"));
            costItem.setCreatedBy(rs.getString("created_by"));
            costItem.setUpdated(rs.getTimestamp("updated"));
            costItem.setUpdatedBy(rs.getString("updated_by"));

            ExpenseCategory expenseCategory = new ExpenseCategory();
            expenseCategory.setId(rs.getInt("expc_id"));
            expenseCategory.setName(rs.getString("expc_name"));

            CostItem.CostFraction fraction = new CostItem.CostFraction();
            fraction.setExpenseCategory(expenseCategory);
            fraction.setFraction(rs.getBigDecimal("cost_fraction"));
            costItem.addCostFraction(fraction);

            if (rs.getLong("exp_id") > 0) {
                Expense expense = new Expense();
                expense.setId(rs.getLong("exp_id"));
                expense.setExpenseType(Expense.ExpenseType.valueOf(rs.getString("expense_type")));
                expense.setAltId(rs.getString("exp_alt_id"));
                expense.setName(rs.getString("exp_name"));
                expense.setQuantity(rs.getInt("exp_quantity"));
                costItem.setExpense(expense);
            }
            return costItem;
        }
    }

    private List<CostItem> getCostFractionsForRecords(List<CostItem> records) {
        List<CostItem> costItems = new ArrayList<CostItem>();
        if (records != null && !records.isEmpty()) {
            int counter = 0;
            CostItem entry = records.get(counter++);
            costItems.add(entry);
            while (records.size() > counter) {
                CostItem record = records.get(counter++);
                if (entry.getId().equals(record.getId())) {
                    entry.getCostFractions().addAll(record.getCostFractions());
                } else {
                    entry = record;
                    costItems.add(entry);
                }
            }
        }
        return costItems;
    }

    private void validateCostItem(CostItem costItem) throws ServiceException {
        if (costItem.getName() == null) {
            throw new ServiceException(messageSource.getMessage("validation_error_costitem_name", null, LocaleContextHolder.getLocale()));
        }
        if (costItem.getApplied() == null) {
            throw new ServiceException(messageSource.getMessage("validation_error_costitem_applied", new Object[]{costItem.getApplied()}, LocaleContextHolder.getLocale()));
        }
        if (costItem.getQuantity() != null && costItem.getQuantity() < 1) {
            throw new ServiceException(messageSource.getMessage("validation_error_costitem_quantity", null, LocaleContextHolder.getLocale()));
        }
        if (costItem.getCustomerId() != null) {
            Integer count = jdbcTemplate.queryForObject("select count(*) from customer where id = ?", Integer.class, costItem.getCustomerId());
            if (!count.equals(1)) {
                throw new ServiceException(messageSource.getMessage("customer_not_found_for_id", new Object[]{costItem.getCustomerId()}, LocaleContextHolder.getLocale()));
            }
        }
        if (costItem.getContractId() != null) {
            Integer count = jdbcTemplate.queryForObject("select count(*) from contract where id = ?", Integer.class, costItem.getContractId());
            if (!count.equals(1)) {
                throw new ServiceException(messageSource.getMessage("contract_not_found_for_id", new Object[]{costItem.getContractId()}, LocaleContextHolder.getLocale()));
            }
        }
        if (costItem.getLocationId() != null) {
            Integer count = jdbcTemplate.queryForObject("select count(*) from location where id = ?", Integer.class, costItem.getLocationId());
            if (!count.equals(1)) {
                throw new ServiceException(messageSource.getMessage("location_not_found_for_id", new Object[]{costItem.getLocationId()}, LocaleContextHolder.getLocale()));
            }
        }
        if (CostItem.CostType.depreciated.equals(costItem.getCostType())) {
            if (costItem.getCostSubType() == null) {
                throw new ServiceException(messageSource.getMessage("validation_error_costtype_missing_subtype", null, LocaleContextHolder.getLocale()));
            }
        }
    }

    @Override
    public Long saveAssetItem(AssetItem assetItem) throws ServiceException {
        if (assetItem.getId() != null) {
            updateAssetItem(assetItem, true);
            return assetItem.getId();
        }
        Long expenseId = null;
        if (assetItem.getExpense() != null) {
        	assetItem.getExpense().setExpenseType(Expense.ExpenseType.asset);
            expenseId = saveExpense(assetItem.getExpense());
        }
        validateAssetItem(assetItem);
        try {
            SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
            jdbcInsert.withTableName("asset_item").usingGeneratedKeyColumns("id");
            Map<String, Object> params = new HashMap<String, Object>();
            if (expenseId != null) {
                params.put("expense_id_ref", expenseId);
            }
            params.put("name", assetItem.getName());
            params.put("description", assetItem.getDescription());
            params.put("amount", assetItem.getAmount());
            if (assetItem.getQuantity() != null) {
                params.put("quantity", assetItem.getQuantity());
            }
            params.put("part_number", assetItem.getPartNumber());
            params.put("sku", assetItem.getSku());
            params.put("life", assetItem.getLife());
            params.put("acquired", assetItem.getAcquired());
            params.put("disposal", assetItem.getDisposal());
            if (assetItem.getCustomerId() != null) {
                params.put("customer_id", assetItem.getCustomerId());
            }
            if (assetItem.getContractId() != null) {
                params.put("contract_id", assetItem.getContractId());
            }
            if (assetItem.getLocationId() != null) {
                params.put("location_id", assetItem.getLocationId());
            }
            params.put("created_by", authenticatedUser());
            Number pk = jdbcInsert.executeAndReturnKey(new MapSqlParameterSource(params));
            if (!assetItem.getAssetCostFractions().isEmpty()) {
                generateAssetItemCostFractions(assetItem, (Long) pk);
            }
            return (Long) pk;
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_assetitem_insert", new Object[]{any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    }

    private void generateAssetItemCostFractions(AssetItem assetItem, Long pk) throws ServiceException {
        for (AssetItem.AssetCostFraction fraction : assetItem.getAssetCostFractions()) {
            try {
                SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
                jdbcInsert.withTableName("asset_item_fraction");
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("expense_category_id", fraction.getExpenseCategory().getId());
                params.put("asset_item_id", pk);
                params.put("cost_fraction", fraction.getFraction());
                params.put("target_utilization", fraction.getTargetUtilization());
                params.put("quantity", fraction.getQuantity());
                int updated = jdbcInsert.execute(new MapSqlParameterSource(params));

                DateTime appliedDate = new DateTime(assetItem.getAcquired())
                        .withZone(DateTimeZone.forID(TZID))
                        .withDayOfMonth(1)
                        .withTimeAtStartOfDay();
                if (assetItem.getLife() != null) {
                    DateTime depreciationDate = appliedDate
                            .plusMonths(assetItem.getLife() - 1);
                    while (appliedDate.compareTo(depreciationDate) <= 0) {
                        createOrUpdateAssetFractionUnitCost(assetItem, fraction, appliedDate);
                        appliedDate = appliedDate.plusMonths(1);
                    }
                } else {
                    createOrUpdateAssetFractionUnitCost(assetItem, fraction, appliedDate);
                }
            } catch (Exception any) {
                throw new ServiceException(messageSource.getMessage("jdbc_error_assetitem_fraction_insert", new Object[]{any.getMessage()}, LocaleContextHolder.getLocale()), any);
            }
        }
    }
    
    private void createOrUpdateAssetFractionUnitCost(AssetItem assetItem, AssetItem.AssetCostFraction fraction, DateTime appliedDate) throws ServiceException {
        UnitCost uc = costDaoService.unitCostByExpenseCategoryAndDate(assetItem.getCustomerId(), fraction.getExpenseCategory().getId(), appliedDate);
        if (uc == null) {
            DateTime endDate = appliedDate
                    .plusMonths(1)
                    .minusDays(1)
                    .withTime(23, 59, 59, 999);
            uc = new UnitCost();
            uc.setAppliedDate(appliedDate);
            uc.setCustomerId(assetItem.getCustomerId());
            uc.setExpenseCategoryId(fraction.getExpenseCategory().getId());
            Integer deviceTotalCount = costService.deviceTotalDeviceCountWithExpenseCategory(appliedDate, endDate, null, uc.getCustomerId(), null, uc.getExpenseCategoryId(), null);
            uc.setDeviceTotalUnits(deviceTotalCount);
        }
        uc.incrementTotalCost(assetItem.getAmount().multiply(fraction.getFraction())
                .divide(new BigDecimal(100)).divide(new BigDecimal(assetItem.getLife()), MathContext.DECIMAL32));
        costDaoService.saveUnitCost(uc);
    }

    private void decrementUnitCost(AssetItem assetItem) throws ServiceException {
        if (assetItem.getId() == null) {
            throw new IllegalArgumentException("asset unit costs cannot be subtracted from a NEW asset item");
        }
        if (assetItem.getLife() != null) {
            for (AssetItem.AssetCostFraction fraction : assetItem.getAssetCostFractions()) {
                DateTime appliedDate = new DateTime(assetItem.getAcquired())
                        .withZone(DateTimeZone.forID(TZID))
                        .plusMonths(1)
                        .withDayOfMonth(1)
                        .withTimeAtStartOfDay();
                DateTime depreciationDate = appliedDate
                        .plusMonths(assetItem.getLife() - 1);
                while (appliedDate.compareTo(depreciationDate) <= 0) {
                    UnitCost uc = costDaoService.unitCostByExpenseCategoryAndDate(assetItem.getCustomerId(), fraction.getExpenseCategory().getId(), appliedDate);
                    if (uc != null) {
                        uc.decrementTotalCost(assetItem.getAmount().multiply(fraction.getFraction())
                                .divide(new BigDecimal(100)).divide(new BigDecimal(assetItem.getLife()), MathContext.DECIMAL32));
                        costDaoService.saveUnitCost(uc);
                    }
                    appliedDate = appliedDate.plusMonths(1);
                }
            }
        }
    }

    @Override
    public void updateAssetItem(AssetItem assetItem, boolean decrement) throws ServiceException {
        if (assetItem.getId() == null) {
            throw new ServiceException(messageSource.getMessage("validation_error_assetitem_id", null, LocaleContextHolder.getLocale()));
        }
        AssetItem existing = assetItem(assetItem.getId());
        if (assetItem.getExpense() != null) {
        	assetItem.getExpense().setExpenseType(Expense.ExpenseType.asset);
            updateExpense(assetItem.getExpense());
        }
        validateAssetItem(assetItem);
        try {
            // if unit costs were corrupted, manually deleted, we'd want to skip decrementing
            if (decrement) {
                decrementUnitCost(existing);
            }
            int updated = jdbcTemplate.update("update asset_item set name = ?, description = ?,"
                    + " amount = ?, quantity = ?, part_number = ?, sku = ?, life = ?, acquired = ?, disposal = ?,"
                    + " customer_id = ?, contract_id= ?, location_id = ?, updated = ?, updated_by = ?"
                    + " where id = ?",
                    new Object[]{assetItem.getName(), assetItem.getDescription(),
                assetItem.getAmount(), assetItem.getQuantity(), assetItem.getPartNumber(), assetItem.getSku(),
                assetItem.getLife(), assetItem.getAcquired(), assetItem.getDisposal(), assetItem.getCustomerId(),
                assetItem.getContractId(), assetItem.getLocationId(),
                new DateTime().withZone(DateTimeZone.forID(TZID)).toDate(), authenticatedUser(),
                assetItem.getId()});
            updateAssetItemCostFractions(assetItem);
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_assetitem_update", new Object[]{assetItem.getId(), any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    }

    private void updateAssetItemCostFractions(AssetItem assetItem) throws ServiceException {
        try {
            int updated = jdbcTemplate.update("delete from asset_item_fraction where asset_item_id = ?",
                    new Object[]{assetItem.getId()});
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_assetitem_fraction_delete", new Object[]{assetItem.getId(), any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
        generateAssetItemCostFractions(assetItem, assetItem.getId());
    }

    @Override
    public void deleteAssetItem(Long id) throws ServiceException {
        AssetItem existing = assetItem(id);
        Long expenseId = jdbcTemplate.queryForObject("select expense_id_ref from asset_item where id = ?", Long.class, id);
        try {
            decrementUnitCost(existing);
            int updated = jdbcTemplate.update("delete from asset_item where id = ?", id);
            if (expenseId != null) {
                deleteExpense(expenseId);
            }
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_assetitem_delete", new Object[]{id, any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    }

    @Override
    public AssetItem assetItem(Long id) throws ServiceException {
        Integer count = jdbcTemplate.queryForObject("select count(*) from asset_item where id = ?", Integer.class, id);
        if (!count.equals(1)) {
            throw new ServiceException(messageSource.getMessage("assetitem_not_found_for_id", new Object[]{id}, LocaleContextHolder.getLocale()));
        }
        String query = BASIC_ASSET_ITEM_QUERY
                + " where assetItem.id = ?"
                + " order by assetItem.id";
        List<AssetItem> records = jdbcTemplate.query(query, new Object[]{id}, new AssetItemRowMapper());
        List<AssetItem> assetItems = getAssetCostFractionsForRecords(records);
        if (assetItems.size() > 0) {
            return assetItems.get(0);
        }
        return null;
    }

    @Override
    public List<AssetItem> assetItems() {
        String query = BASIC_ASSET_ITEM_QUERY
                + " order by assetItem.id";
        List<AssetItem> records = jdbcTemplate.query(query, new AssetItemRowMapper());
        List<AssetItem> assetItems = getAssetCostFractionsForRecords(records);
        Collections.sort(records, new AssetItem.AssetItemBasicComparator());
        return assetItems;
    }

    @Override
    public List<AssetItem> assetItemsForExpenseCategory(Integer expenseCategoryId) {
        String query = BASIC_ASSET_ITEM_QUERY
                + " where aif.expense_category_id = ?"
                + " order by assetItem.id, expc.id";
        List<AssetItem> records = jdbcTemplate.query(query, new Object[]{expenseCategoryId}, new AssetItemRowMapper());
        List<AssetItem> assetItems = getAssetCostFractionsForRecords(records);
        Collections.sort(records, new AssetItem.AssetItemBasicComparator());
        return assetItems;
    }

    @Override
    public List<AssetItem> assetItemsForType(Integer typeId) {
        String query = BASIC_ASSET_ITEM_QUERY
                + " where assetItem.expense_type_id = ?"
                + " order by assetItem.id";
        List<AssetItem> records = jdbcTemplate.query(query, new Object[]{typeId}, new AssetItemRowMapper());
        List<AssetItem> assetItems = getAssetCostFractionsForRecords(records);
        Collections.sort(records, new AssetItem.AssetItemBasicComparator());
        return assetItems;
    }

    @Override
    public List<AssetItem> findAssetItemsByCustomer(Long customerId) {
        String query = BASIC_ASSET_ITEM_QUERY
                + " where assetItem.customer_id = ?"
                + " order by assetItem.id";
        List<AssetItem> records = jdbcTemplate.query(query, new Object[]{customerId}, new AssetItemRowMapper());
        List<AssetItem> assetItems = getAssetCostFractionsForRecords(records);
        Collections.sort(records, new AssetItem.AssetItemBasicComparator());
        return assetItems;
    }

    @Override
    public List<AssetItem> findAssetItemsByCustomerIdAndPeriod(Long customerId, Date leftDate, Date rightDate) {
        if (leftDate == null || rightDate == null) {
            throw new IllegalArgumentException(messageSource.getMessage("missing_arguments",
                    new Object[]{"left and/or right date(s)"}, LocaleContextHolder.getLocale()));
        }
        DateTime leftDateTime = new DateTime(leftDate)
                .withTimeAtStartOfDay()
                .withZone(DateTimeZone.forID(TZID));
        DateTime rightDateTime = new DateTime(rightDate)
                .withTimeAtStartOfDay()
                .plusHours(23).plusMinutes(59).plusSeconds(59)
                .withZone(DateTimeZone.forID(TZID));
        String query = BASIC_ASSET_ITEM_QUERY
        		+ " where assetItem.acquired between :leftDate and :rightDate" 
        		+ " or DATE_ADD(assetItem.acquired, INTERVAL assetItem.life MONTH) between :leftDate and :rightDate"
        		+ " or (assetItem.acquired <= :leftDate and DATE_ADD(assetItem.acquired, INTERVAL assetItem.life MONTH) >= :rightDate)";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("leftDate", leftDateTime.toDate());
        params.put("rightDate", rightDateTime.toDate());
        if (customerId != null && customerId > 0) {
            query += " and assetItem.customer_id = :customerId";
            params.put("customerId", customerId);
        }
        query += " order by assetItem.id";
        List<AssetItem> records = namedJdbcTemplate.query(query, params, new AssetItemRowMapper());
        List<AssetItem> assetItems = getAssetCostFractionsForRecords(records);
        Collections.sort(records, new AssetItem.AssetItemBasicComparator());
        return assetItems;
    }
    
    private class AssetItemRowMapper implements RowMapper<AssetItem> {

        public AssetItem mapRow(ResultSet rs, int i) throws SQLException {
            AssetItem assetItem = new AssetItem(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getBigDecimal("amount"),
                    (rs.getInt("quantity") == 0 ? null : rs.getInt("quantity")),
                    rs.getInt("life"),
                    rs.getString("part_number"),
                    rs.getString("sku"),
                    rs.getDate("acquired"),
                    rs.getDate("disposal"),
                    (rs.getLong("customer_id") == 0 ? null : rs.getLong("customer_id")),
                    (rs.getLong("contract_id") == 0 ? null : rs.getLong("contract_id")),
                    (rs.getInt("location_id") == 0 ? null : rs.getInt("location_id")));

            assetItem.setCreated(rs.getTimestamp("created"));
            assetItem.setCreatedBy(rs.getString("created_by"));
            assetItem.setUpdated(rs.getTimestamp("updated"));
            assetItem.setUpdatedBy(rs.getString("updated_by"));

            ExpenseCategory expenseCategory = new ExpenseCategory();
            expenseCategory.setId(rs.getInt("expc_id"));
            expenseCategory.setName(rs.getString("expc_name"));

            AssetItem.AssetCostFraction fraction = new AssetItem.AssetCostFraction();
            fraction.setExpenseCategory(expenseCategory);
            fraction.setFraction(rs.getBigDecimal("cost_fraction"));
            fraction.setTargetUtilization(rs.getBigDecimal("target_utilization"));
            fraction.setQuantity(rs.getInt("aif_quantity"));
            assetItem.addAssetCostFraction(fraction);

            if (rs.getLong("exp_id") > 0) {
                Expense expense = new Expense();
                expense.setId(rs.getLong("exp_id"));
                expense.setExpenseType(Expense.ExpenseType.valueOf(rs.getString("expense_type")));
                expense.setAltId(rs.getString("exp_alt_id"));
                expense.setName(rs.getString("exp_name"));
                expense.setQuantity(rs.getInt("exp_quantity"));
                assetItem.setExpense(expense);
            }
            return assetItem;
        }
    }

    private List<AssetItem> getAssetCostFractionsForRecords(List<AssetItem> records) {
        List<AssetItem> assetItems = new ArrayList<AssetItem>();
        if (records != null && !records.isEmpty()) {
            int counter = 0;
            AssetItem entry = records.get(counter++);
            assetItems.add(entry);
            while (records.size() > counter) {
                AssetItem record = records.get(counter++);
                if (entry.getId().equals(record.getId())) {
                    entry.getAssetCostFractions().addAll(record.getAssetCostFractions());
                } else {
                    entry = record;
                    assetItems.add(entry);
                }
            }
        }
        return assetItems;
    }

    private void validateAssetItem(AssetItem assetItem) throws ServiceException {
        if (assetItem.getName() == null) {
            throw new ServiceException(messageSource.getMessage("validation_error_assetitem_name", null, LocaleContextHolder.getLocale()));
        }
        if (assetItem.getAcquired() == null) {
            throw new ServiceException(messageSource.getMessage("validation_error_assetitem_acquired", new Object[]{assetItem.getAcquired()}, LocaleContextHolder.getLocale()));
        }
        if (assetItem.getQuantity() != null && assetItem.getQuantity() < 1) {
            throw new ServiceException(messageSource.getMessage("validation_error_assetitem_quantity", null, LocaleContextHolder.getLocale()));
        }
        if (assetItem.getCustomerId() != null) {
            Integer count = jdbcTemplate.queryForObject("select count(*) from customer where id = ?", Integer.class, assetItem.getCustomerId());
            if (!count.equals(1)) {
                throw new ServiceException(messageSource.getMessage("customer_not_found_for_id", new Object[]{assetItem.getCustomerId()}, LocaleContextHolder.getLocale()));
            }
        }
        if (assetItem.getContractId() != null) {
            Integer count = jdbcTemplate.queryForObject("select count(*) from contract where id = ?", Integer.class, assetItem.getContractId());
            if (!count.equals(1)) {
                throw new ServiceException(messageSource.getMessage("contract_not_found_for_id", new Object[]{assetItem.getContractId()}, LocaleContextHolder.getLocale()));
            }
        }
        if (assetItem.getLocationId() != null) {
            Integer count = jdbcTemplate.queryForObject("select count(*) from location where id = ?", Integer.class, assetItem.getLocationId());
            if (!count.equals(1)) {
                throw new ServiceException(messageSource.getMessage("location_not_found_for_id", new Object[]{assetItem.getLocationId()}, LocaleContextHolder.getLocale()));
            }
        }
    }

    @Override
    public Long saveExpense(Expense expense) throws ServiceException {
        if (expense.getId() != null) {
            updateExpense(expense);
            return expense.getId();
        }
        validateExpense(expense);
        try {
            SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
            jdbcInsert.withTableName("expense").usingGeneratedKeyColumns("id");
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("alt_id", expense.getAltId());
            params.put("expense_type", expense.getExpenseType());
            params.put("name", expense.getName());
            params.put("description", expense.getDescription());
            params.put("amount", expense.getAmount());
            if (expense.getQuantity() != null) {
                params.put("quantity", expense.getQuantity());
            }
            if (expense.getCustomerId() != null) {
                params.put("customer_id", expense.getCustomerId());
            }
            if (expense.getContractId() != null) {
                params.put("contract_id", expense.getContractId());
            }
            if (expense.getLocationId() != null) {
                params.put("location_id", expense.getLocationId());
            }
            params.put("created_by", (expense.getCreatedBy() == null ? authenticatedUser() : expense.getCreatedBy()));
            Number pk = jdbcInsert.executeAndReturnKey(new MapSqlParameterSource(params));
            return (Long) pk;
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_expense_insert", new Object[]{any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    }

    @Override
    public void updateExpense(Expense expense) throws ServiceException {
        if (expense.getId() == null) {
            throw new ServiceException(messageSource.getMessage("validation_error_expense_id", null, LocaleContextHolder.getLocale()));
        }
        Integer count = jdbcTemplate.queryForObject("select count(*) from expense where id = ?", Integer.class, expense.getId());
        if (!count.equals(1)) {
            throw new ServiceException(messageSource.getMessage("expense_not_found_for_id", new Object[]{expense.getId()}, LocaleContextHolder.getLocale()));
        }
        validateExpense(expense);
        try {
            int updated = jdbcTemplate.update("update expense set alt_id = ?, expense_type = ?, name = ?,"
                    + " description = ?, amount = ?, quantity = ?, customer_id = ?, contract_id = ?, location_id = ?,"
                    + " updated = ?, updated_by = ?"
                    + " where id = ?",
                    new Object[]{expense.getAltId(), expense.getExpenseType().name(), expense.getName(), expense.getDescription(),
                expense.getAmount(), expense.getQuantity(), expense.getCustomerId(), expense.getContractId(),
                expense.getLocationId(), new DateTime().withZone(DateTimeZone.forID(TZID)).toDate(),
                authenticatedUser(), expense.getId()});
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_expense_update", new Object[]{expense.getId(), any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    }

    @Override
    public void deleteExpense(Long id) throws ServiceException {
        Integer count = jdbcTemplate.queryForObject("select count(*) from expense where id = ?", Integer.class, id);
        if (!count.equals(1)) {
            throw new ServiceException(messageSource.getMessage("expense_not_found_for_id", new Object[]{id}, LocaleContextHolder.getLocale()));
        }
        try {
            int updated = jdbcTemplate.update("delete from expense where id = ?", id);
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_expense_delete", new Object[]{id, any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    }

    @Override
    public Expense expense(Long id) throws ServiceException {
        Integer count = jdbcTemplate.queryForObject("select count(*) from expense where id = ?", Integer.class, id);
        if (!count.equals(1)) {
            throw new ServiceException(messageSource.getMessage("expense_not_found_for_id", new Object[]{id}, LocaleContextHolder.getLocale()));
        }
        String query = "select ex.id, ex.alt_id, ex.expense_type expense_type, ex.name, ex.description, ex.amount,"
                + " ex.quantity, ex.customer_id, ex.contract_id, ex.location_id,"
                + " ex.created, ex.created_by, ex.updated, ex.updated_by"
                + " from expense ex"
                + " where ex.id = ?"
                + " order by ex.created desc";
        return jdbcTemplate.queryForObject(query, new Object[]{id},
                new RowMapper<Expense>() {
            @Override
            public Expense mapRow(ResultSet rs, int i) throws SQLException {
                Expense expense = new Expense(
                        rs.getLong("id"),
                        rs.getString("alt_id"),
                        Expense.ExpenseType.valueOf(rs.getString("expense_type")),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getBigDecimal("amount"),
                        rs.getInt("quantity"),
                        rs.getLong("customer_id"),
                        rs.getLong("contract_id"),
                        rs.getInt("location_id"));

                expense.setCreated(rs.getTimestamp("created"));
                expense.setCreatedBy(rs.getString("created_by"));
                expense.setUpdated(rs.getTimestamp("updated"));
                expense.setUpdatedBy(rs.getString("updated_by"));
                return expense;
            }
        });
    }

    @Override
    public List<Expense> expenses() {
        String query = "select ex.id, ex.alt_id, ex.expense_type expense_type, ex.name, ex.description, ex.amount,"
                + " ex.quantity, ex.customer_id, ex.contract_id, ex.location_id,"
                + " ex.created, ex.created_by, ex.updated, ex.updated_by"
                + " from expense ex"
                + " order by ex.customer_id, ex.name, ex.created desc";
        return jdbcTemplate.query(query, new RowMapper<Expense>() {
            @Override
            public Expense mapRow(ResultSet rs, int i) throws SQLException {
                Expense expense = new Expense(
                        rs.getLong("id"),
                        rs.getString("alt_id"),
                        Expense.ExpenseType.valueOf(rs.getString("expense_type")),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getBigDecimal("amount"),
                        rs.getInt("quantity"),
                        rs.getLong("customer_id"),
                        rs.getLong("contract_id"),
                        rs.getInt("location_id"));

                expense.setCreated(rs.getTimestamp("created"));
                expense.setCreatedBy(rs.getString("created_by"));
                expense.setUpdated(rs.getTimestamp("updated"));
                expense.setUpdatedBy(rs.getString("updated_by"));
                return expense;
            }
        });
    }

    @Override
    public List<Expense> findExpensesByCustomer(Long customerId) {
        String query = "select ex.id, ex.alt_id, ex.expense_type expense_type, ex.name, ex.description, ex.amount,"
                + " ex.quantity, ex.customer_id, ex.contract_id, ex.location_id,"
                + " ex.created, ex.created_by, ex.updated, ex.updated_by"
                + " from expense ex"
                + " where ex.customer_id = ?"
                + " order by ex.name, ex.created desc";
        return jdbcTemplate.query(query, new Object[]{customerId}, new RowMapper<Expense>() {
            @Override
            public Expense mapRow(ResultSet rs, int i) throws SQLException {
                Expense expense = new Expense(
                        rs.getLong("id"),
                        rs.getString("alt_id"),
                        Expense.ExpenseType.valueOf(rs.getString("expense_type")),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getBigDecimal("amount"),
                        rs.getInt("quantity"),
                        rs.getLong("customer_id"),
                        rs.getLong("contract_id"),
                        rs.getInt("location_id"));

                expense.setCreated(rs.getTimestamp("created"));
                expense.setCreatedBy(rs.getString("created_by"));
                expense.setUpdated(rs.getTimestamp("updated"));
                expense.setUpdatedBy(rs.getString("updated_by"));
                return expense;
            }
        });
    }

    @Override
    public List<Expense> findExpensesByCustomerIdAndPeriod(Long customerId, Date leftDate, Date rightDate) {
        Object[] params;
        if (leftDate == null || rightDate == null) {
            throw new IllegalArgumentException(messageSource.getMessage("missing_arguments",
                    new Object[]{"left and/or right date(s)"}, LocaleContextHolder.getLocale()));
        }
        DateTime leftDateTime = new DateTime(leftDate)
                .withTimeAtStartOfDay()
                .withZone(DateTimeZone.forID(TZID));
        DateTime rightDateTime = new DateTime(rightDate)
                .withTimeAtStartOfDay()
                .plusHours(23).plusMinutes(59).plusSeconds(59)
                .withZone(DateTimeZone.forID(TZID));
        String query = "select ex.id, ex.alt_id, ex.expense_type expense_type, ex.name, ex.description, ex.amount,"
                + " ex.quantity, ex.customer_id, ex.contract_id, ex.location_id,"
                + " ex.created, ex.created_by, ex.updated, ex.updated_by"
                + " from expense ex"
                + " where ex.created between ? and ?";
        if (customerId != null && customerId > 0) {
            query += " and ex.customer_id = ?";
            params = new Object[]{leftDateTime.toDate(), rightDateTime.toDate(), customerId};
        } else {
            params = new Object[]{leftDateTime.toDate(), rightDateTime.toDate()};
        }
        query += " order by ex.customer_id, ext.name, ex.name, ex.created desc";
        return jdbcTemplate.query(query, params, new RowMapper<Expense>() {
            @Override
            public Expense mapRow(ResultSet rs, int i) throws SQLException {
                Expense expense = new Expense(
                        rs.getLong("id"),
                        rs.getString("alt_id"),
                        Expense.ExpenseType.valueOf(rs.getString("expense_type")),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getBigDecimal("amount"),
                        rs.getInt("quantity"),
                        rs.getLong("customer_id"),
                        rs.getLong("contract_id"),
                        rs.getInt("location_id"));

                expense.setCreated(rs.getTimestamp("created"));
                expense.setCreatedBy(rs.getString("created_by"));
                expense.setUpdated(rs.getTimestamp("updated"));
                expense.setUpdatedBy(rs.getString("updated_by"));
                return expense;
            }
        });
    }

    private void validateExpense(Expense expense) throws ServiceException {
        if (expense.getName() == null) {
            throw new ServiceException(messageSource.getMessage("validation_error_expense_name", null, LocaleContextHolder.getLocale()));
        }
        if (expense.getQuantity() != null && expense.getQuantity() < 1) {
            throw new ServiceException(messageSource.getMessage("validation_error_expense_quantity", null, LocaleContextHolder.getLocale()));
        }
        if (expense.getCustomerId() != null) {
            Integer count = jdbcTemplate.queryForObject("select count(*) from customer where id = ?", Integer.class, expense.getCustomerId());
            if (!count.equals(1)) {
                throw new ServiceException(messageSource.getMessage("customer_not_found_for_id", new Object[]{expense.getCustomerId()}, LocaleContextHolder.getLocale()));
            }
        }
        if (expense.getContractId() != null) {
            Integer count = jdbcTemplate.queryForObject("select count(*) from contract where id = ?", Integer.class, expense.getContractId());
            if (!count.equals(1)) {
                throw new ServiceException(messageSource.getMessage("contract_not_found_for_id", new Object[]{expense.getContractId()}, LocaleContextHolder.getLocale()));
            }
        }
        if (expense.getLocationId() != null) {
            Integer count = jdbcTemplate.queryForObject("select count(*) from location where id = ?", Integer.class, expense.getLocationId());
            if (!count.equals(1)) {
                throw new ServiceException(messageSource.getMessage("location_not_found_for_id", new Object[]{expense.getLocationId()}, LocaleContextHolder.getLocale()));
            }
        }
        if (expense.getExpenseType() == null) {
            throw new ServiceException(messageSource.getMessage("validation_error_expense_type_missing", null, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public Customer customer(Long id) throws ServiceException {
        Integer count = jdbcTemplate.queryForObject("select count(*) from customer where id = ?", Integer.class, id);
        if (!count.equals(1)) {
            throw new ServiceException(messageSource.getMessage("customer_not_found_for_id", new Object[]{id}, LocaleContextHolder.getLocale()));
        }
        String query = "select customer.id, customer.alt_id, customer.name, count(contract.id) contracts, customer.archived,"
        		+ " customer.street_1, customer.street_2, customer.city, customer.state, customer.zip, customer.country, customer.phone,"
                + " customer.sn_sys_id, customer.si_enabled, customer.alt_name, customer.azure_customer_id, customer.created, customer.created_by, customer.updated, customer.updated_by"
                + " from customer customer"
                + " left outer join contract contract on contract.customer_id = customer.id"
                + " where customer.id = ?"
                + " group by customer.id, customer.name"
                + " order by customer.name";
        Customer customer = jdbcTemplate.queryForObject(query, new Object[]{id},
                new RowMapper<Customer>() {
            @Override
            public Customer mapRow(ResultSet rs, int i) throws SQLException {
                return new Customer(
                        rs.getLong("id"),
                        rs.getLong("alt_id"),
                        rs.getString("name"),
                        rs.getString("street_1"),
                        rs.getString("street_2"),
                        rs.getString("city"),
                        rs.getString("state"),
                        rs.getString("zip"),
                        rs.getString("country"),
                        rs.getString("phone"),
                        rs.getInt("contracts"),
                        rs.getBoolean("archived"),
                        rs.getString("sn_sys_id"),
                        rs.getBoolean("si_enabled"),
                        rs.getString("alt_name"),
                        rs.getString("azure_customer_id"),
                        rs.getDate("created"),
                        rs.getString("created_by"),
                        rs.getDate("updated"),
                        rs.getString("updated_by"));
            }
        });
        
        String childQuery = "select customer.id, customer.alt_id, customer.name, customer.archived,"
        		+ " customer.street_1, customer.street_2, customer.city, customer.state, customer.zip, customer.country, customer.phone,"
                + " customer.sn_sys_id, customer.si_enabled, customer.alt_name, customer.azure_customer_id, customer.created, customer.created_by, customer.updated, customer.updated_by"
                + " from customer customer"
                + " where customer.parent_id = :parent_id"
        		+ " order by customer.name";
    	Map<String, Object> childParams = new HashMap<String, Object>();
    	childParams.put("parent_id", id);
    	List<Customer> childCustomers = namedJdbcTemplate.query(childQuery, childParams,
                new RowMapper<Customer>() {
            @Override
            public Customer mapRow(ResultSet rs, int i) throws SQLException {
                return new Customer(
                        rs.getLong("id"),
                        rs.getLong("alt_id"),
                        rs.getString("name"),
                        rs.getString("street_1"),
                        rs.getString("street_2"),
                        rs.getString("city"),
                        rs.getString("state"),
                        rs.getString("zip"),
                        rs.getString("country"),
                        rs.getString("phone"),
                        0, //are not returning this for children now
                        rs.getBoolean("archived"),
                        rs.getString("sn_sys_id"),
                        rs.getBoolean("si_enabled"),
                        rs.getString("alt_name"),
                        rs.getString("azure_customer_id"),
                        rs.getDate("created"),
                        rs.getString("created_by"),
                        rs.getDate("updated"),
                        rs.getString("updated_by"));
            }
        });
    	
    	if(childCustomers != null && childCustomers.size() > 0) {
    		for(Customer childCustomer: childCustomers) {
        		mapPersonnelToCustomer(childCustomer);
        	}
    		
    		customer.setChildren(childCustomers);
    	}
    	
    	mapPersonnelToCustomer(customer);
    	
    	return customer;
    }
    
    private void mapPersonnelToCustomer(Customer customer) {
    	List<Personnel> personnel = findCustomerPersonnel(customer.getId());
    	
    	for(Personnel person : personnel) {
    		Personnel.Type type = person.getType();
    		if(Personnel.Type.ae.equals(type)) {
    			customer.setAccountExecutive(person);
    		} else if(Personnel.Type.epe.equals(type)) {
    			customer.setEnterpriseProgramExecutive(person);
    		} else if(Personnel.Type.bsc.equals(type)) {
    			customer.getBusinessSolutionsConsultants().add(person);
    		} else if(Personnel.Type.sdm.equals(type)) {
    			customer.getServiceDeliveryManagers().add(person);
    		}
    	}
    }
    
    @Override
    public void mapPersonnelToContract(Contract contract) {
    	List<Personnel> personnel = findContractPersonnel(contract.getId());
    	int sdmCnt = 0;
    	StringBuilder sdms = new StringBuilder();
    	
    	for(Personnel person : personnel) {
    		Personnel.Type type = person.getType();
    		if(Personnel.Type.ae.equals(type)) {
    			contract.setAccountExecutive(person);
    		} else if(Personnel.Type.epe.equals(type)) {
    			contract.setEnterpriseProgramExecutive(person);
    		} else if(Personnel.Type.bsc.equals(type)) {
    			contract.getBusinessSolutionsConsultants().add(person);
    		} else if(Personnel.Type.sdm.equals(type)) {
    			contract.getServiceDeliveryManagers().add(person);
    			if (sdmCnt>0) sdms.append(", ");
    			sdms.append(person.getUserName());
    			sdmCnt++;
    		}
    	}
    	
    	if ((contract.getEngagementManager()==null) || ((contract.getEngagementManager()!=null) && (contract.getEngagementManager().trim().length()==0)) ) {
    		contract.setEngagementManager(sdms.toString());
    	}
    }
    
    public void mapEngagementManagerForServiceContractDetail(ServiceDetailRecord record) {
    	if ((record.getEngagementManager()==null) || ((record.getEngagementManager()!=null) && (record.getEngagementManager().trim().length()==0)) ) {
        	List<Personnel> personnel = findContractPersonnel(record.getContractId());
        	int sdmCnt = 0;
        	StringBuilder sdms = new StringBuilder();
        	
        	for (Personnel person : personnel) {
        		if (Personnel.Type.sdm.equals(person.getType())) {
        			if (sdmCnt>0) sdms.append(", ");
        			sdms.append(person.getUserName());
        			sdmCnt++;
        		}
        	}
        	
        	record.setEngagementManager(sdms.toString());    		
    	}
    }
    
    
    @Override
    public List<Personnel> findCustomerPersonnel(Long customerId) {
    	String query = "select cp.id, cp.customer_id, cp.user_id, cp.type, u.name user_name from customer_personnel cp"
    			+ " inner join users u on u.id = cp.user_id"
    			+ " where customer_id = :customer_id";
    	Map<String, Object> params = new HashMap<String, Object>();
    	params.put("customer_id", customerId);
    	List<Personnel> personnel = namedJdbcTemplate.query(query, params,
                new RowMapper<Personnel>() {
            @Override
            public Personnel mapRow(ResultSet rs, int i) throws SQLException {
            	return new Personnel(
                        rs.getLong("id"),
                        rs.getLong("customer_id"),
                        null,
                        rs.getLong("user_id"),
                        rs.getString("user_name"),
                        Personnel.Type.valueOf(rs.getString("type")));
            }
            
        });
    	
    	return personnel;
    }
    
    @Override
    public List<Personnel> findContractPersonnel(Long contractId) {
    	String query = "select cp.id, cp.contract_id, cp.user_id, cp.type, u.name user_name from contract_personnel cp"
    			+ " inner join users u on u.id = cp.user_id"
    			+ " where contract_id = :contract_id";
    	Map<String, Object> params = new HashMap<String, Object>();
    	params.put("contract_id", contractId);
    	List<Personnel> personnel = namedJdbcTemplate.query(query, params,
                new RowMapper<Personnel>() {
            @Override
            public Personnel mapRow(ResultSet rs, int i) throws SQLException {
            	return new Personnel(
                        rs.getLong("id"),
                        null,
                        rs.getLong("contract_id"),
                        rs.getLong("user_id"),
                        rs.getString("user_name"),
                        Personnel.Type.valueOf(rs.getString("type")));
            }
            
        });
    	
    	return personnel;
    }

    @Override
    public List<Customer> findCustomerByName(String name) {
        String query = "select customer.id, customer.alt_id, customer.name, count(contract.id) contracts, customer.archived,"
        		+ " customer.street_1, customer.street_2, customer.city, customer.state, customer.zip, customer.country, customer.phone,"
                + " customer.sn_sys_id, customer.si_enabled, customer.alt_name, customer.azure_customer_id, customer.created, customer.created_by, customer.updated, customer.updated_by"
                + " from customer customer"
                + " left outer join contract contract on contract.customer_id = customer.id"
                + " where customer.name = ? or customer.alt_name = ?";
        try {
            return jdbcTemplate.query(query, new Object[]{name, name},
                    new RowMapper<Customer>() {
                @Override
                public Customer mapRow(ResultSet rs, int i) throws SQLException {
                    if (rs.getLong("id") == 0) {
                        return null;
                    } // otherwise a bogus Customer is returned due to the outer join
                    return new Customer(
                            rs.getLong("id"),
                            rs.getLong("alt_id"),
                            rs.getString("name"),
                            rs.getString("street_1"),
                            rs.getString("street_2"),
                            rs.getString("city"),
                            rs.getString("state"),
                            rs.getString("zip"),
                            rs.getString("country"),
                            rs.getString("phone"),
                            rs.getInt("contracts"),
                            rs.getBoolean("archived"),
                            rs.getString("sn_sys_id"),
                            rs.getBoolean("si_enabled"),
                            rs.getString("alt_name"),
                            rs.getString("azure_customer_id"),
                            rs.getDate("created"),
                            rs.getString("created_by"),
                            rs.getDate("updated"),
                            rs.getString("updated_by"));
                }
            });
        } catch (IncorrectResultSizeDataAccessException ex) {
            log.info("customer not found [{}]", ex.getMessage());
            return null;
        }
    }

    @Override
    public List<Customer> customers(Boolean archived, Boolean siEnabled, Boolean includeChildren) {
        String query = "select customer.id, customer.alt_id, customer.name, count(contract.id) contracts, customer.archived,"
        		+ " customer.street_1, customer.street_2, customer.city, customer.state, customer.zip, customer.country, customer.phone,"
                + " customer.sn_sys_id, customer.si_enabled, customer.alt_name, customer.azure_customer_id, customer.created, customer.created_by, customer.updated, customer.updated_by, customer.parent_id"
                + " from customer customer"
                + " left outer join contract contract on contract.customer_id = customer.id";
        if (archived != null) {
            query += " where customer.archived = :archived";
        }
        if (siEnabled != null) {
            if (archived != null) {
                query += " and";
            } else {
                query += " where";
            }
            query += " customer.si_enabled = :si_enabled";
        }
        query += " group by customer.id, customer.name"
                + " order by customer.name";
        Map<String, Object> params = new HashMap<String, Object>();
        if (archived != null) {
            params.put("archived", archived);
        }
        if (siEnabled != null) {
            params.put("si_enabled", siEnabled);
        }
        List<Customer> customers = namedJdbcTemplate.query(query, params,
                new RowMapper<Customer>() {
            @Override
            public Customer mapRow(ResultSet rs, int i) throws SQLException {
                Customer cust =  new Customer(
                        rs.getLong("id"),
                        rs.getLong("alt_id"),
                        rs.getString("name"),
                        rs.getString("street_1"),
                        rs.getString("street_2"),
                        rs.getString("city"),
                        rs.getString("state"),
                        rs.getString("zip"),
                        rs.getString("country"),
                        rs.getString("phone"),
                        rs.getInt("contracts"),
                        rs.getBoolean("archived"),
                        rs.getString("sn_sys_id"),
                        rs.getBoolean("si_enabled"),
                        rs.getString("alt_name"),
                        rs.getString("azure_customer_id"),
                        rs.getDate("created"),
                        rs.getString("created_by"),
                        rs.getDate("updated"),
                        rs.getString("updated_by"));
                
                if(rs.getLong("parent_id") > 0) {
                	Customer parent = new Customer();
                	parent.setId(rs.getLong("parent_id"));
                	cust.setParent(parent);
                }
                
                return cust;
            }
            
        });
        
        if(includeChildren != null && includeChildren) {
        	String childQuery = "select customer.id, customer.alt_id, customer.name, customer.archived,"
        			+ " customer.street_1, customer.street_2, customer.city, customer.state, customer.zip, customer.country, customer.phone,"
                    + " customer.sn_sys_id, customer.si_enabled, customer.alt_name, customer.azure_customer_id, customer.created, customer.created_by, customer.updated, customer.updated_by"
                    + " from customer customer"
                    + " where customer.parent_id = :parent_id"
            		+ " order by customer.name";
        	
        	for(Customer customer : customers) {
	        	Map<String, Object> childParams = new HashMap<String, Object>();
	        	childParams.put("parent_id", customer.getId());
	        	List<Customer> childCustomers = namedJdbcTemplate.query(childQuery, childParams,
	                    new RowMapper<Customer>() {
	                @Override
	                public Customer mapRow(ResultSet rs, int i) throws SQLException {
	                    return new Customer(
	                            rs.getLong("id"),
	                            rs.getLong("alt_id"),
	                            rs.getString("name"),
	                            rs.getString("street_1"),
	                            rs.getString("street_2"),
	                            rs.getString("city"),
	                            rs.getString("state"),
	                            rs.getString("zip"),
	                            rs.getString("country"),
	                            rs.getString("phone"),
	                            0, //are not returning this for children now
	                            rs.getBoolean("archived"),
	                            rs.getString("sn_sys_id"),
	                            rs.getBoolean("si_enabled"),
	                            rs.getString("alt_name"),
	                            rs.getString("azure_customer_id"),
	                            rs.getDate("created"),
	                            rs.getString("created_by"),
	                            rs.getDate("updated"),
	                            rs.getString("updated_by"));
	                }
	            });
	        	
	        	mapPersonnelToCustomer(customer);
	        	
	        	if(childCustomers != null && childCustomers.size() > 0) {
	        		for(Customer childCustomer: childCustomers) {
	            		mapPersonnelToCustomer(childCustomer);
	            	}
	        		
	        		customer.setChildren(childCustomers);
	        	}
        	}
        } else {
        	for(Customer customer : customers) {
	        	mapPersonnelToCustomer(customer);
        	}
        }
        
        
        
        return customers;
    }

    @Override
    public List<Customer> customers() {
        return customers(null, null, Boolean.FALSE);
    }

    @Override
    public Long saveCustomer(Customer customer) throws ServiceException {
        validateCustomer(customer);
        
        Integer count = jdbcTemplate.queryForObject("select count(*) from customer where name = ?", Integer.class, customer.getName());
        if (count > 0) {
            throw new ServiceException(messageSource.getMessage("api_customer_exists_for_name", new Object[]{customer.getName()}, LocaleContextHolder.getLocale()));
        }
        
        try {
            SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
            jdbcInsert.withTableName("customer").usingGeneratedKeyColumns("id");
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("alt_id", customer.getAltId());
            params.put("name", customer.getName());
            params.put("phone", customer.getPhone());
            params.put("street_1", customer.getStreet1());
            params.put("street_2", customer.getStreet2());
            params.put("city", customer.getCity());
            params.put("state", customer.getState());
            params.put("zip", customer.getZip());
            params.put("country", customer.getCountry());
            params.put("archived", customer.getArchived());
            params.put("si_enabled", customer.getSiEnabled());
            params.put("sn_sys_id", customer.getServiceNowSysId());
            params.put("azure_customer_id", customer.getAzureCustomerId());
            params.put("archived", Boolean.FALSE);
            params.put("alt_name", customer.getAltName());
            
            if (customer.getParent() != null) {
            	Customer parent = customer.getParent();
            	count = jdbcTemplate.queryForObject("select count(*) from customer where id = ?", Integer.class, parent.getId());
            	if (!count.equals(1)) {
                    throw new ServiceException(messageSource.getMessage("customer_not_found_for_id", new Object[]{customer.getId()}, LocaleContextHolder.getLocale()));
                }
                params.put("parent_id", parent.getId());
            }
            params.put("created_by", authenticatedUser());
            Number pk = jdbcInsert.executeAndReturnKey(new MapSqlParameterSource(params));
            return (Long) pk;
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_customer_insert", new Object[]{any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    }
    
    private void validateCustomer(Customer customer) throws ServiceException {
    	if(StringUtils.isBlank(customer.getName())) {
    		throw new ServiceException(messageSource.getMessage("validation_error_customer_name", null, LocaleContextHolder.getLocale()));
    	}
    }
    
    @Override
    public void updateCustomerBits(Customer customer) throws ServiceException {
        if (customer.getId() == null) {
            throw new ServiceException(messageSource.getMessage("validation_error_customer_id", null, LocaleContextHolder.getLocale()));
        }
        Integer count = jdbcTemplate.queryForObject("select count(*) from customer where id = ?", Integer.class, customer.getId());
        if (!count.equals(1)) {
            throw new ServiceException(messageSource.getMessage("customer_not_found_for_id", new Object[]{customer.getId()}, LocaleContextHolder.getLocale()));
        }
        try {
            int updated = jdbcTemplate.update("update customer set archived = ?, si_enabled = ?, alt_name = ?, azure_customer_id = ?,"
                    + " updated = ?, updated_by = ? where id = ?",
                    new Object[]{customer.getArchived(), customer.getSiEnabled(), customer.getAltName(), customer.getAzureCustomerId(),
                new DateTime().withZone(DateTimeZone.forID(TZID)).toDate(), authenticatedUser(), customer.getId()});
            if (StringUtils.isNotBlank(customer.getAltName())) {
                updateCostCustomerAltName(customer);
            }
            
            //update personnel relationships
            List<Personnel> personnel = new ArrayList<Personnel>();
            if(customer.getAccountExecutive() != null) personnel.add(customer.getAccountExecutive());
            if(customer.getEnterpriseProgramExecutive() != null) personnel.add(customer.getEnterpriseProgramExecutive());
            if(customer.getBusinessSolutionsConsultants() != null && !customer.getBusinessSolutionsConsultants().isEmpty()) personnel.addAll(customer.getBusinessSolutionsConsultants());
            if(customer.getServiceDeliveryManagers() != null && !customer.getServiceDeliveryManagers().isEmpty()) personnel.addAll(customer.getServiceDeliveryManagers());
            
            updatePersonnelForCustomer(personnel, customer.getId());
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_customer_update", new Object[]{customer.getId(), any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    }
    
    private void updatePersonnelForCustomer(List<Personnel> personnel, Long customerId) throws ServiceException {
    	int deleted = jdbcTemplate.update("delete from customer_personnel where customer_id = ?", customerId);
    	
    	for(Personnel person: personnel) {
    		saveCustomerPersonnel(person);
    	}
    }
    
    public void saveCustomerPersonnel(Personnel personnel) throws ServiceException {
    	//validate 
    	
    	try {
	    	SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
	        jdbcInsert.withTableName("customer_personnel").usingGeneratedKeyColumns("id");
	        Map<String, Object> params = new HashMap<String, Object>();
	        params.put("customer_id", personnel.getCustomerId());
	        params.put("user_id", personnel.getUserId());
	        params.put("type", personnel.getType().name());
	        params.put("created", new Date());
	        params.put("created_by", authenticatedUser());
	        Number pk = jdbcInsert.executeAndReturnKey(new MapSqlParameterSource(params));
    	} catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_customer_personnel_insert", new Object[]{any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    }
    
    private void updatePersonnelForContract(List<Personnel> personnel, Long contractId) throws ServiceException {
    	int deleted = jdbcTemplate.update("delete from contract_personnel where contract_id = ?", contractId);
    	
    	for(Personnel person: personnel) {
    		person.setContractId(contractId);
    		saveContractPersonnel(person);
    	}
    }
    
    public void saveContractPersonnel(Personnel personnel) throws ServiceException {
    	//validate 
    	
    	try {
	    	SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
	        jdbcInsert.withTableName("contract_personnel").usingGeneratedKeyColumns("id");
	        Map<String, Object> params = new HashMap<String, Object>();
	        params.put("contract_id", personnel.getContractId());
	        params.put("user_id", personnel.getUserId());
	        params.put("type", personnel.getType().name());
	        params.put("created", new Date());
	        params.put("created_by", authenticatedUser());
	        Number pk = jdbcInsert.executeAndReturnKey(new MapSqlParameterSource(params));
    	} catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_customer_personnel_insert", new Object[]{any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    }
    
    
    /**
     * Azure invoices records are imported as SI Costs. When a Customer in SI can't be
     * matched with the Azure customer by name, the cost is still created and can be updated
     * to fix the missing SI Customer id later on.
     * 
     * @param customer 
     */
    private void updateCostCustomerAltName(Customer customer) throws ServiceException {
        if (customer.getId() == null) {
            throw new ServiceException(messageSource.getMessage("validation_error_customer_id", null, LocaleContextHolder.getLocale()));
        }
        if (StringUtils.isBlank(customer.getAltName())) {
            log.info("Azure Cost record cannot be updated for Customer: missing Customer alt name");
            return;
        }
        try {
            int updated = jdbcTemplate.update("update cost_item set customer_id = ? where customer_id is null and azure_customer_name = ?",
                    new Object[]{customer.getId(), customer.getAltName()});
        } catch (Exception any) {
            log.warn("Could not update Azure Cost record for Customer: {}", any.getMessage());
        }
    }

    @Override
    public List<Customer> searchCustomers(String search) {
        String query = "select customer.id, customer.alt_id, customer.name, count(contract.id) contracts, customer.archived,"
        		+ " customer.street_1, customer.street_2, customer.city, customer.state, customer.zip, customer.country, customer.phone,"
                + " customer.sn_sys_id, customer.si_enabled, customer.alt_name, customer.azure_customer_id, customer.created, customer.created_by, customer.updated, customer.updated_by"
                + " from customer customer"
                + " left outer join contract contract on contract.customer_id = customer.id"
                + " where customer.name like :search"
                + " group by customer.id, customer.name"
                + " order by customer.name";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("search", "%" + search + "%");
        return namedJdbcTemplate.query(query, params,
                new RowMapper<Customer>() {
            @Override
            public Customer mapRow(ResultSet rs, int i) throws SQLException {
                return new Customer(
                        rs.getLong("id"),
                        rs.getLong("alt_id"),
                        rs.getString("name"),
                        rs.getString("street_1"),
                        rs.getString("street_2"),
                        rs.getString("city"),
                        rs.getString("state"),
                        rs.getString("zip"),
                        rs.getString("country"),
                        rs.getString("phone"),
                        rs.getInt("contracts"),
                        rs.getBoolean("archived"),
                        rs.getString("sn_sys_id"),
                        rs.getBoolean("si_enabled"),
                        rs.getString("alt_name"),
                        rs.getString("azure_customer_id"),
                        rs.getDate("created"),
                        rs.getString("created_by"),
                        rs.getDate("updated"),
                        rs.getString("updated_by"));
            }
        });
    }

    @Override
    public Contract contract(Long id) throws ServiceException {
        Integer count = jdbcTemplate.queryForObject("select count(*) from contract where id = ?", Integer.class, id);
        if (!count.equals(1)) {
            throw new ServiceException(messageSource.getMessage("contract_not_found_for_id", new Object[]{id}, LocaleContextHolder.getLocale()));
        }
        String query = BASIC_CONTRACT_QUERY
                + " where contract.id = ?"
                + " group by contract.customer_id, contract.id, contract.alt_id, contract.name";
        Contract contract = jdbcTemplate.queryForObject(query, new Object[]{id},
                new RowMapper<Contract>() {
            @Override
            public Contract mapRow(ResultSet rs, int i) throws SQLException {
                return new Contract(
                        rs.getLong("customer_id"),
                        rs.getLong("id"),
                        rs.getString("alt_id"),
                        rs.getString("job_number"),
                        rs.getString("name"),
                        rs.getString("emgr"),
                        rs.getString("sda"),
                        rs.getInt("services"),
                        rs.getDate("signed_date"),
                        rs.getDate("service_start_date"),
                        rs.getDate("start_date"),
                        rs.getDate("end_date"),
                        rs.getBoolean("archived"),
                        rs.getString("sn_sys_id"),
                        rs.getString("file_path"),
                        ((rs.getString("renewal_status") == null) ? null : Contract.RenewalStatus.valueOf(rs.getString("renewal_status"))),
                        rs.getBigDecimal("renewal_change"),
                        rs.getString("renewal_notes"));
            }
        });
        
        if(contract != null) mapPersonnelToContract(contract);
        return contract;
    }

    @Override
    public Contract findContractByJobNumberAndCompanyId(String jobNumber, Long customerId) throws ServiceException {
        String query = BASIC_CONTRACT_QUERY
                + " where contract.job_number = ? and contract.customer_id = ?";
        List<Contract> contracts = jdbcTemplate.query(query, new Object[]{jobNumber, customerId},
                new RowMapper<Contract>() {
            @Override
            public Contract mapRow(ResultSet rs, int i) throws SQLException {
                if (rs.getLong("customer_id") == 0) {
                    return null;
                } // otherwise a bogus Contract is returned due to the outer join
                return new Contract(
                        rs.getLong("customer_id"),
                        rs.getLong("id"),
                        rs.getString("alt_id"),
                        rs.getString("job_number"),
                        rs.getString("name"),
                        rs.getString("emgr"),
                        rs.getString("sda"),
                        rs.getInt("services"),
                        rs.getDate("signed_date"),
                        rs.getDate("service_start_date"),
                        rs.getDate("start_date"),
                        rs.getDate("end_date"),
                        rs.getBoolean("archived"),
                        rs.getString("sn_sys_id"),
                        rs.getString("file_path"),
                        ((rs.getString("renewal_status") == null) ? null : Contract.RenewalStatus.valueOf(rs.getString("renewal_status"))),
                        rs.getBigDecimal("renewal_change"),
                        rs.getString("renewal_notes"));
            }
        });
        if (contracts == null || contracts.isEmpty()) {
            return null;
        }
        // there should only be one...
        Contract contract = contracts.get(0);
        if (contract != null) { // so weird... collection still ends up with a null contract!
            mapPersonnelToContract(contract);
        }
        return contract;
    }
    
    @Override
    public Contract findContractByActiveM365ConfigTenantId(String tenantId) throws ServiceException {
    	String query = BASIC_CONTRACT_QUERY
                + " inner join microsoft_365_subscription_config msc on msc.contract_id = contract.id"
                + " where msc.tenant_id = ?";
        List<Contract> contracts = jdbcTemplate.query(query, new Object[]{tenantId},
                new RowMapper<Contract>() {
            @Override
            public Contract mapRow(ResultSet rs, int i) throws SQLException {
                if (rs.getLong("customer_id") == 0) {
                    return null;
                } // otherwise a bogus Contract is returned due to the outer join
                return new Contract(
                        rs.getLong("customer_id"),
                        rs.getLong("id"),
                        rs.getString("alt_id"),
                        rs.getString("job_number"),
                        rs.getString("name"),
                        rs.getString("emgr"),
                        rs.getString("sda"),
                        rs.getInt("services"),
                        rs.getDate("signed_date"),
                        rs.getDate("service_start_date"),
                        rs.getDate("start_date"),
                        rs.getDate("end_date"),
                        rs.getBoolean("archived"),
                        rs.getString("sn_sys_id"),
                        rs.getString("file_path"),
                        ((rs.getString("renewal_status") == null) ? null : Contract.RenewalStatus.valueOf(rs.getString("renewal_status"))),
                        rs.getBigDecimal("renewal_change"),
                        rs.getString("renewal_notes"));
            }
        });
        if (contracts == null || contracts.isEmpty()) {
            return null;
        }
        // there should only be one...
        Contract contract = contracts.get(0);
        mapPersonnelToContract(contract);
        return contract;
    }
    
    @Override
    public List<Contract> findContractsByCustomerId(Long customerId, Boolean archived) throws ServiceException {
        String query = BASIC_CONTRACT_QUERY
                + " where contract.customer_id = :customerId";
        if (archived != null) {
            query += " and archived = :archived";
        }
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("customerId", customerId);
        if (archived != null) {
            params.put("archived", archived);
        }
        List<Contract> contracts = namedJdbcTemplate.query(query, params, new RowMapper<Contract>() {
            @Override
            public Contract mapRow(ResultSet rs, int i) throws SQLException {
                return new Contract(
                        rs.getLong("customer_id"),
                        rs.getLong("id"),
                        rs.getString("alt_id"),
                        rs.getString("job_number"),
                        rs.getString("name"),
                        rs.getString("emgr"),
                        rs.getString("sda"),
                        0,
                        rs.getDate("signed_date"),
                        rs.getDate("service_start_date"),
                        rs.getDate("start_date"),
                        rs.getDate("end_date"),
                        rs.getBoolean("archived"),
                        rs.getString("sn_sys_id"),
                        rs.getString("file_path"),
                        ((rs.getString("renewal_status") == null) ? null : Contract.RenewalStatus.valueOf(rs.getString("renewal_status"))),
                        rs.getBigDecimal("renewal_change"),
                        rs.getString("renewal_notes"));
            }
        });
        
        for(Contract contract: contracts) {
        	mapPersonnelToContract(contract);
        }
        
        return contracts;
    }

    @Override
    public List<Contract> contracts(Boolean archived) {
        String query = BASIC_CONTRACT_QUERY;
        if (archived != null) {
            query += " where archived = :archived";
        }
        query += " group by contract.customer_id, contract.id, contract.alt_id, contract.name";
        Map<String, Object> params = new HashMap<String, Object>();
        if (archived != null) {
            params.put("archived", archived);
        }
        List<Contract> contracts = namedJdbcTemplate.query(query, params, new RowMapper<Contract>() {
            @Override
            public Contract mapRow(ResultSet rs, int i) throws SQLException {
                return new Contract(
                        rs.getLong("customer_id"),
                        rs.getLong("id"),
                        rs.getString("alt_id"),
                        rs.getString("job_number"),
                        rs.getString("name"),
                        rs.getString("emgr"),
                        rs.getString("sda"),
                        rs.getInt("services"),
                        rs.getDate("signed_date"),
                        rs.getDate("service_start_date"),
                        rs.getDate("start_date"),
                        rs.getDate("end_date"),
                        rs.getBoolean("archived"),
                        rs.getString("sn_sys_id"),
                        rs.getString("file_path"),
                        ((rs.getString("renewal_status") == null) ? null : Contract.RenewalStatus.valueOf(rs.getString("renewal_status"))),
                        rs.getBigDecimal("renewal_change"),
                        rs.getString("renewal_notes"));
            }
        });
        
        for(Contract contract: contracts) {
        	mapPersonnelToContract(contract);
        }
        
        return contracts;
    }

    @Override
    public List<Contract> contracts() {
        return contracts(null);
    }

    @Override
    public Long saveContract(Contract contract) throws ServiceException {
        Date startDate = contract.getStartDate();
        Date endDate = contract.getEndDate();
        if (contract.getId() != null) {
            updateContract(contract);
            return contract.getId();
        }
        validateContract(contract);
        
        try {
            SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
            jdbcInsert.withTableName("contract").usingGeneratedKeyColumns("id");
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("alt_id", contract.getAltId());
            params.put("job_number", contract.getJobNumber());
            params.put("name", contract.getName());
            params.put("emgr", contract.getEngagementManager());
            params.put("sda", contract.getAccountExec());
            params.put("customer_id", contract.getCustomerId());
            params.put("signed_date", contract.getSignedDate());
            params.put("service_start_date", contract.getServiceStartDate());
            params.put("start_date", startDate);
            params.put("end_date", endDate);
            params.put("created_by", authenticatedUser());
            params.put("archived", contract.getArchived());
            params.put("sn_sys_id", contract.getServiceNowSysId());
            params.put("quote_id", contract.getQuoteId());
            params.put("file_path", contract.getFilePath());
            params.put("renewal_status", null);
            params.put("renewal_change", new BigDecimal(0));
            params.put("renewal_notes", null);
            Number pk = jdbcInsert.executeAndReturnKey(new MapSqlParameterSource(params));

            //add contract invoices for dates
            Long contractId = (Long) pk;
            addOrDeleteContractInvoicesForContract(contractId, startDate, endDate);

            //create pricing sheet record
            PricingSheet pricingSheet = new PricingSheet();
            pricingSheet.setContractId(contractId);
            pricingSheet.setActive(Boolean.TRUE);
            pricingSheetService.savePricingSheet(pricingSheet);
            
            //update personnel relationships
            List<Personnel> personnel = new ArrayList<Personnel>();
            if(contract.getAccountExecutive() != null) personnel.add(contract.getAccountExecutive());
            if(contract.getEnterpriseProgramExecutive() != null) personnel.add(contract.getEnterpriseProgramExecutive());
            if(contract.getBusinessSolutionsConsultants() != null && !contract.getBusinessSolutionsConsultants().isEmpty()) personnel.addAll(contract.getBusinessSolutionsConsultants());
            if(contract.getServiceDeliveryManagers() != null && !contract.getServiceDeliveryManagers().isEmpty()) personnel.addAll(contract.getServiceDeliveryManagers());
            
            updatePersonnelForContract(personnel, contractId);
            
            return contractId;
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_contract_insert", new Object[]{any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    }

    /**
     * IF no invoice conflicts exist...
     * 
     * For END DATES
     * new contract endDate < prior contract endDate:
     * - update contract service line items with end dates > new contract endDate
     * new contract endDate > prior contract endDate:
     * - update contract service line items with end dates = prior endDate
     * 
     * For START DATES
     * new contract startDate > prior contract startDate
     * - update contract service line items with start dates < new contract startDate
     * new contract startDate < prior contract startDate
     * - update contract service line items with start dates = prior startDate
     */
    private void checkAndModifyContractServices(Long contractId, Date startDate, Date endDate) throws ServiceException {
        if (contractId == null) {
            throw new IllegalArgumentException("Contract ID field cannot be null");
        }
        if (startDate == null) {
            throw new IllegalArgumentException("Contract Start Date field cannot be null");
        }
        if (endDate == null) {
            throw new IllegalArgumentException("Contract End Date field cannot be null");
        }
        Contract existingContract = contract(contractId);
        
        DateTime startDateTime = new DateTime(startDate);
        DateTime priorStartDateTime = new DateTime(existingContract.getStartDate());
        DateTime endDateTime = new DateTime(endDate);
        DateTime priorEndDateTime = new DateTime(existingContract.getEndDate());
        if (!startDateTime.equals(priorStartDateTime) || !endDateTime.equals(priorEndDateTime)) {
            List<ContractInvoice> contractInvoices = contractInvoicesForContract(contractId);
            for (Service record : contractRevenueService.services(contractId, null)) {
                boolean updating = false;
                DateTime priorContractServiceStartDateTime = new DateTime(record.getStartDate());
                DateTime priorContractServiceEndDateTime = new DateTime(record.getEndDate());
                if (startDateTime.isAfter(priorContractServiceStartDateTime) ||
                        (priorStartDateTime.isAfter(startDateTime) && priorStartDateTime.equals(priorContractServiceStartDateTime))) {
                    updating = true;
                    record.setStartDate(startDate);
                }
                if (endDateTime.isBefore(priorContractServiceEndDateTime) ||
                        (priorEndDateTime.isBefore(endDateTime) && priorEndDateTime.equals(priorContractServiceEndDateTime))) {
                    updating = true;
                    record.setEndDate(endDate);
                }
                if ( updating && !contractInvoiceConflictExistsForContractService(contractInvoices, record, BatchResult.Operation.update)) {
                    log.debug("auto-update Contract Service startDate [from -> to]: [{} -> {}] and endDate [from -> to]: [{} -> {}]",
                            new Object[]{DateTimeFormat.forPattern("MM/dd/yyyy").print(priorStartDateTime),
                                DateTimeFormat.forPattern("MM/dd/yyyy").print(startDateTime),
                                DateTimeFormat.forPattern("MM/dd/yyyy").print(priorEndDateTime),
                                DateTimeFormat.forPattern("MM/dd/yyyy").print(endDateTime)
                            });
                    int updated = jdbcTemplate.update("update contract_service set start_date = ?, end_date = ? where id = ?",
                            new Object[]{record.getStartDate(), record.getEndDate(), record.getId()});
                } else if (updating) {
                    log.debug("CONFLICT on auto-update for Contract Service with ID [{}], startDate: [{}], endDate: [{}]",
                            new Object[]{record.getId(), DateTimeFormat.forPattern("MM/dd/yyyy").print(priorContractServiceStartDateTime),
                            DateTimeFormat.forPattern("MM/dd/yyyy").print(priorContractServiceEndDateTime)});
                }
            }
        }
    }

    /**
     * IF no invoice conflicts exist...
     * 
     * For END DATES
     * new contract endDate < prior contract endDate:
     * - update contract adjustment line items with end dates > new contract endDate
     * new contract endDate > prior contract endDate:
     * - update contract adjustment line items with end dates = prior endDate
     * 
     * For START DATES
     * new contract startDate > prior contract startDate
     * - update contract adjustment line items with start dates < new contract startDate
     * new contract startDate < prior contract startDate
     * - update contract adjustment line items with start dates = prior startDate
     */
    private void checkAndModifyContractAdjustments(Long contractId, Date startDate, Date endDate) throws ServiceException {
        if (contractId == null) {
            throw new IllegalArgumentException("Contract ID field cannot be null");
        }
        if (startDate == null) {
            throw new IllegalArgumentException("Contract Start Date field cannot be null");
        }
        if (endDate == null) {
            throw new IllegalArgumentException("Contract End Date field cannot be null");
        }
        Contract existingContract = contract(contractId);
        
        DateTime startDateTime = new DateTime(startDate);
        DateTime priorStartDateTime = new DateTime(existingContract.getStartDate());
        DateTime endDateTime = new DateTime(endDate);
        DateTime priorEndDateTime = new DateTime(existingContract.getEndDate());
        if (!startDateTime.equals(priorStartDateTime) || !endDateTime.equals(priorEndDateTime)) {
            List<ContractInvoice> contractInvoices = contractInvoicesForContract(contractId);
            for (ContractAdjustment record : contractRevenueService.contractAdjustments(contractId)) {
                boolean updating = false;
                DateTime priorContractServiceStartDateTime = new DateTime(record.getStartDate());
                DateTime priorContractServiceEndDateTime = new DateTime(record.getEndDate());
                if (startDateTime.isAfter(priorContractServiceStartDateTime) ||
                        (priorStartDateTime.isAfter(startDateTime) && priorStartDateTime.equals(priorContractServiceStartDateTime))) {
                    updating = true;
                    record.setStartDate(startDate);
                }
                if (endDateTime.isBefore(priorContractServiceEndDateTime) ||
                        (priorEndDateTime.isBefore(endDateTime) && priorEndDateTime.equals(priorContractServiceEndDateTime))) {
                    updating = true;
                    record.setEndDate(endDate);
                }
                if ( updating && !contractInvoiceConflictExistsForContractAdjustment(contractInvoices, record, BatchResult.Operation.update)) {
                    log.debug("auto-update Contract Adjustment startDate [from -> to]: [{} -> {}] and endDate [from -> to]: [{} -> {}]",
                            new Object[]{DateTimeFormat.forPattern("MM/dd/yyyy").print(priorStartDateTime),
                                DateTimeFormat.forPattern("MM/dd/yyyy").print(startDateTime),
                                DateTimeFormat.forPattern("MM/dd/yyyy").print(priorEndDateTime),
                                DateTimeFormat.forPattern("MM/dd/yyyy").print(endDateTime)
                            });
                    int updated = jdbcTemplate.update("update contract_adjustment set start_date = ?, end_date = ? where id = ?",
                            new Object[]{record.getStartDate(), record.getEndDate(), record.getId()});
                } else if (updating) {
                    log.debug("CONFLICT on auto-update for Contract Service with ID [{}], startDate: [{}], endDate: [{}]",
                            new Object[]{record.getId(), DateTimeFormat.forPattern("MM/dd/yyyy").print(priorContractServiceStartDateTime),
                            DateTimeFormat.forPattern("MM/dd/yyyy").print(priorContractServiceEndDateTime)});
                }
            }
        }
    }
    
    @Override
    public void addOrDeleteContractInvoicesForContract(Long contractId, Date startDate, Date endDate) throws ServiceException {
        DateTime startDateTime = new DateTime(startDate);
        DateTime endDateTime = new DateTime(endDate);
        try {

            //if the contract already exists, we need to check the start and end month against the invoices in the event they changed the day, but not month of the start/end date
            Contract existingContract = contract(contractId);
            List<ContractInvoice> existingInvoices = contractInvoicesForContract(contractId);
            if (existingContract != null) {
                DateTime existingContractStartDate = new DateTime(existingContract.getStartDate());
                DateTime existingContractEndDate = new DateTime(existingContract.getEndDate());

                //only need to do this if the start/end months match, but the days don't -- otherwise the loops after this will take care of it
                if ((startDateTime.getMonthOfYear() == existingContractStartDate.getMonthOfYear() && startDateTime.getDayOfMonth() != existingContractStartDate.getDayOfMonth())
                        || (endDateTime.getMonthOfYear() == existingContractEndDate.getMonthOfYear() && endDateTime.getDayOfMonth() != existingContractEndDate.getDayOfMonth())) {
                    for (ContractInvoice existingInvoice : existingInvoices) {
                        DateTime existingStartDate = new DateTime(existingInvoice.getStartDate());
                        DateTime existingEndDate = new DateTime(existingInvoice.getEndDate());
                        if (existingStartDate.equals(existingContractStartDate)) {
                            if (!ContractInvoice.Status.invoiced.equals(existingInvoice.getStatus())) {
                                existingInvoice.setStartDate(startDate);
                                updateContractInvoice(existingInvoice);
                            } else {
                                throw new ServiceException(messageSource.getMessage("validation_error_contract_invoice_contract_date_invoiced", null, LocaleContextHolder.getLocale()));
                            }
                        }

                        if (existingEndDate.equals(existingContractEndDate)) {
                            if (!ContractInvoice.Status.invoiced.equals(existingInvoice.getStatus())) {
                                existingInvoice.setEndDate(endDate);
                                updateContractInvoice(existingInvoice);
                            } else {
                                throw new ServiceException(messageSource.getMessage("validation_error_contract_invoice_contract_date_invoiced", null, LocaleContextHolder.getLocale()));
                            }
                        }
                    }
                }
            }

            //delete contract invoices that are outside of the date range now
            for (ContractInvoice existingInvoice : existingInvoices) {
                DateTime existingStartDate = new DateTime(existingInvoice.getStartDate());
                DateTime existingEndDate = new DateTime(existingInvoice.getEndDate());
                if (existingStartDate.isBefore(startDateTime) || existingEndDate.isAfter(endDateTime)) {
                    if (!ContractInvoice.Status.invoiced.equals(existingInvoice.getStatus())) {
                        deleteContractInvoice(existingInvoice.getId());
                    } else {
                        throw new ServiceException(messageSource.getMessage("validation_error_contract_invoice_contract_date_invoiced", null, LocaleContextHolder.getLocale()));
                    }
                }
            }

            //add in new contract invoices
            for (DateTime date = startDateTime; date.isBefore(endDateTime); date = date.plusMonths(1)) {
                DateTime periodStart = date;
                DateTime periodEnd = periodStart.dayOfMonth().withMaximumValue();
                if (periodStart.isAfter(startDateTime)) {
                    periodStart = periodStart.dayOfMonth().withMinimumValue();
                }
                if (periodEnd.isAfter(endDateTime)) {
                    periodEnd = endDateTime;
                }

                ContractInvoice contractInvoice = new ContractInvoice();
                contractInvoice.setContractId(contractId);
                contractInvoice.setStatus(ContractInvoice.Status.active);
                contractInvoice.setStartDate(periodStart.toDate());
                contractInvoice.setEndDate(periodEnd.toDate());

                //we only want to add it if it doesn't exist yet
                boolean exists = false;
                for (ContractInvoice existingInvoice : existingInvoices) {
                    if (existingInvoice.getStartDate().equals(periodStart.toDate()) && existingInvoice.getEndDate().equals(periodEnd.toDate())) {
                        exists = true;
                    }
                }

                if (!exists) {
                    saveContractInvoice(contractInvoice);
                }
            }
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_contract_invoice_with_contract_insert", new Object[]{any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    }
    
    private void validateContract(Contract contract) throws ServiceException {
    	Date startDate = contract.getStartDate();
        Date endDate = contract.getEndDate();
        
        if (contract.getCustomerId() == null) {
            throw new ServiceException(messageSource.getMessage("validation_error_contract_customer", null, LocaleContextHolder.getLocale()));
        }
        if (contract.getName() == null) {
            throw new ServiceException(messageSource.getMessage("validation_error_contract_name", null, LocaleContextHolder.getLocale()));
        }
        if (startDate == null) {
            throw new ServiceException(messageSource.getMessage("validation_error_contract_startdate", null, LocaleContextHolder.getLocale()));
        }
        if (endDate == null) {
            throw new ServiceException(messageSource.getMessage("validation_error_contract_enddate", null, LocaleContextHolder.getLocale()));
        }
        if(endDate.before(startDate)) {
        	throw new ServiceException(messageSource.getMessage("validation_error_contract_enddate_before_start_date", null, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public void updateContract(Contract contract) throws ServiceException {
        Long contractId = contract.getId();
        Date startDate = contract.getStartDate();
        Date endDate = contract.getEndDate();

        if (contractId == null) {
            throw new ServiceException(messageSource.getMessage("validation_error_contract_id", null, LocaleContextHolder.getLocale()));
        }
        Integer count = jdbcTemplate.queryForObject("select count(*) from contract where id = ?", Integer.class, contract.getId());
        if (!count.equals(1)) {
            throw new ServiceException(messageSource.getMessage("contract_not_found_for_id", new Object[]{contract.getId()}, LocaleContextHolder.getLocale()));
        }
        validateContract(contract);
        
        addOrDeleteContractInvoicesForContract(contractId, startDate, endDate);

        checkAndModifyContractServices(contractId, startDate, endDate);
        
        checkAndModifyContractAdjustments(contractId, startDate, endDate);
        
        String renewalStatus = null;
        if(contract.getRenewalStatus() != null) renewalStatus = contract.getRenewalStatus().name();
        
        try {
        	log.info("about to update contract");
            int updated = jdbcTemplate.update("update contract set alt_id = ?, job_number = ?, name = ?, emgr = ?, sda = ?, customer_id = ?,"
                    + " signed_date = ?, service_start_date = ?, start_date = ?, end_date = ?, archived = ?, sn_sys_id = ?, file_path = ?, renewal_status = ?, renewal_change = ?, renewal_notes = ?, updated = ?, updated_by = ? where id = ?",
                    new Object[]{contract.getAltId(), contract.getJobNumber(), contract.getName(), contract.getEngagementManager(), contract.getAccountExec(), contract.getCustomerId(), contract.getSignedDate(),
                contract.getServiceStartDate(), contract.getStartDate(), contract.getEndDate(), contract.getArchived(), contract.getServiceNowSysId(), contract.getFilePath(), renewalStatus, (contract.getRenewalChange() == null ? BigDecimal.ZERO : contract.getRenewalChange()), contract.getRenewalNotes(), 
                new DateTime().withZone(DateTimeZone.forID(TZID)).toDate(), authenticatedUser(), contract.getId()});
            
            log.info("about to build personnel");
            //update personnel relationships
            List<Personnel> personnel = new ArrayList<Personnel>();
            if(contract.getAccountExecutive() != null) personnel.add(contract.getAccountExecutive());
            if(contract.getEnterpriseProgramExecutive() != null) personnel.add(contract.getEnterpriseProgramExecutive());
            if(contract.getBusinessSolutionsConsultants() != null && !contract.getBusinessSolutionsConsultants().isEmpty()) personnel.addAll(contract.getBusinessSolutionsConsultants());
            if(contract.getServiceDeliveryManagers() != null && !contract.getServiceDeliveryManagers().isEmpty()) personnel.addAll(contract.getServiceDeliveryManagers());
            
            log.info("Updating personnel");
            
            updatePersonnelForContract(personnel, contractId);
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_contract_update", new Object[]{contract.getId(), any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    }

    @Override
    public void deleteContract(Long id) throws ServiceException {
        Integer count = jdbcTemplate.queryForObject("select count(*) from contract where id = ?", Integer.class, id);
        if (!count.equals(1)) {
            throw new ServiceException(messageSource.getMessage("contract_not_found_for_id", new Object[]{id}, LocaleContextHolder.getLocale()));
        }
        try {
            int updated = jdbcTemplate.update("delete from contract where id = ?", id);
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_contract_delete", new Object[]{id, any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    }

    @Override
    public Long saveContractInvoice(ContractInvoice contractInvoice) throws ServiceException {
        if (contractInvoice.getId() != null) {
            updateContractInvoice(contractInvoice);
            return contractInvoice.getId();
        }
        if (contractInvoice.getContractId() == null) {
            throw new ServiceException(messageSource.getMessage("validation_error_contract_invoice_contract", null, LocaleContextHolder.getLocale()));
        }
        if (contractInvoice.getStartDate() == null || contractInvoice.getEndDate() == null) {
            throw new ServiceException(messageSource.getMessage("validation_error_contract_invoice_date", null, LocaleContextHolder.getLocale()));
        }
        try {
            SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
            jdbcInsert.withTableName("contract_invoice").usingGeneratedKeyColumns("id");
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("contract_id", contractInvoice.getContractId());
            params.put("status", contractInvoice.getStatus());
            params.put("start_date", contractInvoice.getStartDate());
            params.put("end_date", contractInvoice.getEndDate());
            params.put("created", new DateTime().withZone(DateTimeZone.forID(TZID)).toDate());
            params.put("created_by", authenticatedUser());
            Number pk = jdbcInsert.executeAndReturnKey(new MapSqlParameterSource(params));
            return (Long) pk;
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_contract_invoice_insert", new Object[]{any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    }

    @Override
    public void updateContractInvoice(ContractInvoice contractInvoice) throws ServiceException {
        if (contractInvoice.getId() == null) {
            throw new ServiceException(messageSource.getMessage("validation_error_contract_invoice_id", null, LocaleContextHolder.getLocale()));
        }
        Integer count = jdbcTemplate.queryForObject("select count(*) from contract_invoice where id = ?", Integer.class, contractInvoice.getId());
        if (!count.equals(1)) {
            throw new ServiceException(messageSource.getMessage("contract_invoice_not_found_for_id", new Object[]{contractInvoice.getId()}, LocaleContextHolder.getLocale()));
        }
        if (contractInvoice.getContractId() == null) {
            throw new ServiceException(messageSource.getMessage("validation_error_contract_invoice_contract", null, LocaleContextHolder.getLocale()));
        }
        try {
            int updated = jdbcTemplate.update("update contract_invoice set contract_id = ?, status = ?, start_date = ?, end_date = ?, "
                    + " updated = ?, updated_by = ? where id = ?",
                    new Object[]{contractInvoice.getContractId(), contractInvoice.getStatus().name(), contractInvoice.getStartDate(), contractInvoice.getEndDate(),
                new DateTime().withZone(DateTimeZone.forID(TZID)).toDate(),
                authenticatedUser(), contractInvoice.getId()});
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_contract_invoice_update", new Object[]{contractInvoice.getId(), any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    }

    @Override
    public void deleteContractInvoice(Long id) throws ServiceException {
        Integer count = jdbcTemplate.queryForObject("select count(*) from contract_invoice where id = ?", Integer.class, id);
        if (!count.equals(1)) {
            throw new ServiceException(messageSource.getMessage("contract_invoice_not_found_for_id", new Object[]{id}, LocaleContextHolder.getLocale()));
        }
        try {
            int updated = jdbcTemplate.update("delete from contract_invoice where id = ?", id);
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_contract_invoice_delete", new Object[]{id, any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    }

    @Override
    public ContractInvoice contractInvoice(Long id) throws ServiceException {
        Integer count = jdbcTemplate.queryForObject("select count(*) from contract_invoice where id = ?", Integer.class, id);
        if (!count.equals(1)) {
            throw new ServiceException(messageSource.getMessage("contract_invoice_not_found_for_id", new Object[]{id}, LocaleContextHolder.getLocale()));
        }
        String query = "select * from contract_invoice ci where ci.id = ?";
        return jdbcTemplate.queryForObject(query, new Object[]{id},
                new RowMapper<ContractInvoice>() {
            @Override
            public ContractInvoice mapRow(ResultSet rs, int i) throws SQLException {
                ContractInvoice ci = new ContractInvoice(
                        rs.getLong("id"),
                        rs.getLong("contract_id"),
                        ContractInvoice.Status.valueOf(rs.getString("status")),
                        rs.getDate("start_date"),
                        rs.getDate("end_date"));

                ci.setCreated(rs.getDate("created"));
                ci.setCreatedBy(rs.getString("created_by"));
                ci.setUpdated(rs.getDate("updated"));
                ci.setUpdatedBy(rs.getString("updated_by"));
                return ci;
            }
        });
    }

    @Override
    public List<ContractInvoice> contractInvoicesForContract(Long contractId) throws ServiceException {
        String query = "select * from contract_invoice ci where ci.contract_id = ?";

        return jdbcTemplate.query(query, new Object[]{contractId}, new RowMapper<ContractInvoice>() {
            @Override
            public ContractInvoice mapRow(ResultSet rs, int i) throws SQLException {
                ContractInvoice ci = new ContractInvoice(
                        rs.getLong("id"),
                        rs.getLong("contract_id"),
                        ContractInvoice.Status.valueOf(rs.getString("status")),
                        rs.getDate("start_date"),
                        rs.getDate("end_date"));

                ci.setCreated(rs.getDate("created"));
                ci.setCreatedBy(rs.getString("created_by"));
                ci.setUpdated(rs.getDate("updated"));
                ci.setUpdatedBy(rs.getString("updated_by"));
                return ci;
            }
        });
    }

    @Override
    public List<ContractInvoice> contractInvoices() {
        String query = "select * from contract_invoice";
        return jdbcTemplate.query(query, new RowMapper<ContractInvoice>() {
            @Override
            public ContractInvoice mapRow(ResultSet rs, int i) throws SQLException {
                ContractInvoice ci = new ContractInvoice(
                        rs.getLong("id"),
                        rs.getLong("contract_id"),
                        ContractInvoice.Status.valueOf(rs.getString("status")),
                        rs.getDate("start_date"),
                        rs.getDate("end_date"));

                ci.setCreated(rs.getDate("created"));
                ci.setCreatedBy(rs.getString("created_by"));
                ci.setUpdated(rs.getDate("updated"));
                ci.setUpdatedBy(rs.getString("updated_by"));
                return ci;
            }
        });
    }

    @Override
    public List<BatchResult> batchContractInvoices(ContractInvoice[] contractInvoices) {

        List<BatchResult> results = new ArrayList<BatchResult>();
        for (ContractInvoice contractInvoice : contractInvoices) {
            if (contractInvoice.getOperation() != null) {
                BatchResult.Operation op = null;
                try {
                    op = BatchResult.Operation.valueOf(contractInvoice.getOperation());
                } catch (IllegalArgumentException iae) {
                    results.add(new BatchResult(contractInvoice.getId(), messageSource.getMessage("batch_error_contract_invoice_bad_operation", new Object[]{contractInvoice.getId()}, LocaleContextHolder.getLocale()), null, BatchResult.Result.failed));
                    continue;
                }
                if (op.equals(BatchResult.Operation.delete)) {
                    try {
                        deleteContractInvoice(contractInvoice.getId());
                        results.add(new BatchResult(contractInvoice.getId(), messageSource.getMessage("api_ok_batch_response_single", new Object[]{op.toString(), contractInvoice.getId()}, LocaleContextHolder.getLocale()), op, BatchResult.Result.success));
                    } catch (ServiceException se) {
                        results.add(new BatchResult(contractInvoice.getId(), se.getMessage(), op, BatchResult.Result.failed));
                    } catch (Exception other) {
                        BatchResult result = new BatchResult(contractInvoice.getId(), messageSource.getMessage("batch_error_contract_invoice_delete_exception", new Object[]{contractInvoice.getId()}, LocaleContextHolder.getLocale()), op, BatchResult.Result.failed);
                        result.newMeta().addException(other);
                        results.add(result);
                    }
                } else if (op.equals(BatchResult.Operation.create)) {
                    try {
                        saveContractInvoice(contractInvoice);
                        results.add(new BatchResult(contractInvoice.getId(), messageSource.getMessage("api_ok_batch_response_single", new Object[]{op.toString(), contractInvoice.getId()}, LocaleContextHolder.getLocale()), op, BatchResult.Result.success));
                    } catch (ServiceException se) {
                        results.add(new BatchResult(contractInvoice.getContractId(), se.getMessage(), op, BatchResult.Result.failed));
                    } catch (Exception other) {
                        BatchResult result = new BatchResult(contractInvoice.getContractId(), messageSource.getMessage("batch_error_contract_invoice_save_exception", new Object[]{contractInvoice.getContractId()}, LocaleContextHolder.getLocale()), op, BatchResult.Result.failed);
                        result.newMeta().addException(other);
                        results.add(result);
                    }
                } else if (op.equals(BatchResult.Operation.update)) {
                    try {
                        updateContractInvoice(contractInvoice);
                        results.add(new BatchResult(contractInvoice.getId(), messageSource.getMessage("api_ok_batch_response_single", new Object[]{op.toString(), contractInvoice.getId()}, LocaleContextHolder.getLocale()), op, BatchResult.Result.success));
                    } catch (ServiceException se) {
                        results.add(new BatchResult(contractInvoice.getId(), se.getMessage(), op, BatchResult.Result.failed));
                    } catch (Exception other) {
                        BatchResult result = new BatchResult(contractInvoice.getId(), messageSource.getMessage("batch_error_contract_invoice_update_exception", new Object[]{contractInvoice.getId()}, LocaleContextHolder.getLocale()), op, BatchResult.Result.failed);
                        result.newMeta().addException(other);
                        results.add(result);
                    }
                }
            } else {
                results.add(new BatchResult(contractInvoice.getId(), messageSource.getMessage("batch_error_contract_invoice_no_operation", new Object[]{contractInvoice.getId()}, LocaleContextHolder.getLocale())));
            }
        }
        return results;
    }

    @Override
    public ContractUpdate contractUpdate(Long id) throws ServiceException {
        Integer count = jdbcTemplate.queryForObject("select count(*) from contract_update where id = ?", Integer.class, id);
        if (!count.equals(1)) {
            throw new ServiceException(messageSource.getMessage("contract_update_not_found_for_id", new Object[]{id}, LocaleContextHolder.getLocale()));
        }
        String query = "select * from contract_update cu where cu.id = ?";
        return jdbcTemplate.queryForObject(query, new Object[]{id},
                new RowMapper<ContractUpdate>() {
            @Override
            public ContractUpdate mapRow(ResultSet rs, int i) throws SQLException {
                return new ContractUpdate(
                        rs.getLong("id"),
                        rs.getLong("contract_id"),
                        rs.getString("alt_id"),
                        rs.getString("job_number"),
                        rs.getString("ticket_number"),
                        rs.getString("note"),
                        rs.getDate("signed_date"),
                        rs.getDate("signed_date"),
                        rs.getBigDecimal("onetime_price"),
                        rs.getBigDecimal("recurring_price"),
                        rs.getDate("updated"),
                        rs.getString("updated_by"),
                        rs.getString("file_path"));
            }
        });
    }
    
    @Override
    public ContractUpdate findContractUpdateByContractIdAndAltId(Long contractId, String altId) throws ServiceException {
        String query = "select * from contract_update cu where cu.contract_id = ? and cu.alt_id = ?";
        List<ContractUpdate> updates = jdbcTemplate.query(query, new Object[]{contractId, altId}, new RowMapper<ContractUpdate>() {
            @Override
            public ContractUpdate mapRow(ResultSet rs, int i) throws SQLException {
                return new ContractUpdate(
                        rs.getLong("id"),
                        rs.getLong("contract_id"),
                        rs.getString("alt_id"),
                        rs.getString("job_number"),
                        rs.getString("ticket_number"),
                        rs.getString("note"),
                        rs.getDate("signed_date"),
                        rs.getDate("effective_date"),
                        rs.getBigDecimal("onetime_price"),
                        rs.getBigDecimal("recurring_price"),
                        rs.getDate("updated"),
                        rs.getString("updated_by"),
                        rs.getString("file_path"));
            }
        });
        
        if (updates == null || updates.isEmpty()) {
            return null;
        }
        // there should only be one...
        return updates.get(0);
    }

    @Override
    public List<ContractUpdate> contractUpdates() {
        String query = "select * from contract_update";
        return jdbcTemplate.query(query, new RowMapper<ContractUpdate>() {
            @Override
            public ContractUpdate mapRow(ResultSet rs, int i) throws SQLException {
                return new ContractUpdate(
                        rs.getLong("id"),
                        rs.getLong("contract_id"),
                        rs.getString("alt_id"),
                        rs.getString("job_number"),
                        rs.getString("ticket_number"),
                        rs.getString("note"),
                        rs.getDate("signed_date"),
                        rs.getDate("effective_date"),
                        rs.getBigDecimal("onetime_price"),
                        rs.getBigDecimal("recurring_price"),
                        rs.getDate("updated"),
                        rs.getString("updated_by"),
                        rs.getString("file_path"));
            }
        });
    }

    @Override
    public Long saveContractUpdate(ContractUpdate contractUpdate) throws ServiceException {
        if (contractUpdate.getId() != null) {
            updateContractUpdate(contractUpdate);
            return contractUpdate.getId();
        }
        if (contractUpdate.getContractId() == null) {
            throw new ServiceException(messageSource.getMessage("validation_error_contract_update_contract", null, LocaleContextHolder.getLocale()));
        }
        try {
            SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
            jdbcInsert.withTableName("contract_update").usingGeneratedKeyColumns("id");
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("alt_id", contractUpdate.getAltId());
            params.put("job_number", contractUpdate.getJobNumber());
            params.put("ticket_number", contractUpdate.getTicketNumber());
            params.put("contract_id", contractUpdate.getContractId());
            params.put("onetime_price", contractUpdate.getOnetimePrice());
            params.put("recurring_price", contractUpdate.getRecurringPrice());
            params.put("note", contractUpdate.getNote());
            params.put("signed_date", contractUpdate.getSignedDate());
            params.put("effective_date", contractUpdate.getEffectiveDate());
            params.put("updated_by", authenticatedUser());
            Number pk = jdbcInsert.executeAndReturnKey(new MapSqlParameterSource(params));
            return (Long) pk;
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_contract_update_insert", new Object[]{any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    }

    @Override
    public void updateContractUpdate(ContractUpdate contractUpdate) throws ServiceException {
        if (contractUpdate.getId() == null) {
            throw new ServiceException(messageSource.getMessage("validation_error_contract_update_id", null, LocaleContextHolder.getLocale()));
        }
        Integer count = jdbcTemplate.queryForObject("select count(*) from contract_update where id = ?", Integer.class, contractUpdate.getId());
        if (!count.equals(1)) {
            throw new ServiceException(messageSource.getMessage("contract_update_not_found_for_id", new Object[]{contractUpdate.getId()}, LocaleContextHolder.getLocale()));
        }
        if (contractUpdate.getContractId() == null) {
            throw new ServiceException(messageSource.getMessage("validation_error_contract_update_contract", null, LocaleContextHolder.getLocale()));
        }
        try {
            int updated = jdbcTemplate.update("update contract_update set alt_id = ?, job_number = ?, ticket_number = ?, note = ?, contract_id = ?,"
                    + " signed_date = ?, effective_date = ?, onetime_price = ?, recurring_price = ?, updated = ?, updated_by = ?, file_path = ? where id = ?",
                    new Object[]{contractUpdate.getAltId(), contractUpdate.getJobNumber(), contractUpdate.getTicketNumber(), contractUpdate.getNote(), contractUpdate.getContractId(),
                contractUpdate.getSignedDate(), contractUpdate.getEffectiveDate(), contractUpdate.getOnetimePrice(), contractUpdate.getRecurringPrice(), new DateTime().withZone(DateTimeZone.forID(TZID)).toDate(),
                authenticatedUser(), contractUpdate.getFilePath(), contractUpdate.getId()});
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_contract_update_update", new Object[]{contractUpdate.getId(), any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    }

    @Override
    public void deleteContractUpdate(Long id) throws ServiceException {
        Integer count = jdbcTemplate.queryForObject("select count(*) from contract_update where id = ?", Integer.class, id);
        if (!count.equals(1)) {
            throw new ServiceException(messageSource.getMessage("contract_update_not_found_for_id", new Object[]{id}, LocaleContextHolder.getLocale()));
        }
        try {
            int updated = jdbcTemplate.update("delete from contract_update where id = ?", id);
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_contract_update_delete", new Object[]{id, any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    }

    @Override
    public Long saveContractGroup(ContractGroup contractGroup) throws ServiceException {
        if (contractGroup.getId() != null) {
            updateContractGroup(contractGroup);
            return contractGroup.getId();
        }
        if (contractGroup.getContractId() == null) {
            throw new ServiceException(messageSource.getMessage("validation_error_contract_group_contract", null, LocaleContextHolder.getLocale()));
        }
        try {
            SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
            jdbcInsert.withTableName("contract_group").usingGeneratedKeyColumns("id");
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("name", contractGroup.getName());
            params.put("description", contractGroup.getDescription());
            params.put("contract_id", contractGroup.getContractId());
            params.put("created_by", authenticatedUser());
            Number pk = jdbcInsert.executeAndReturnKey(new MapSqlParameterSource(params));
            return (Long) pk;
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_contract_group_insert", new Object[]{any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    }

    @Override
    public void updateContractGroup(ContractGroup contractGroup) throws ServiceException {
        if (contractGroup.getId() == null) {
            throw new ServiceException(messageSource.getMessage("validation_error_contract_group_id", null, LocaleContextHolder.getLocale()));
        }
        Integer count = jdbcTemplate.queryForObject("select count(*) from contract_group where id = ?", Integer.class, contractGroup.getId());
        if (!count.equals(1)) {
            throw new ServiceException(messageSource.getMessage("contract_group_not_found_for_id", new Object[]{contractGroup.getId()}, LocaleContextHolder.getLocale()));
        }
        if (contractGroup.getContractId() == null) {
            throw new ServiceException(messageSource.getMessage("validation_error_contract_group_contract", null, LocaleContextHolder.getLocale()));
        }
        try {
            int updated = jdbcTemplate.update("update contract_group set name = ?, description = ?, contract_id = ?,"
                    + " updated = ?, updated_by = ? where id = ?",
                    new Object[]{contractGroup.getName(), contractGroup.getDescription(), contractGroup.getContractId(),
                new DateTime().withZone(DateTimeZone.forID(TZID)).toDate(),
                authenticatedUser(), contractGroup.getId()});
            log.info("Updated [{}] contract groups for contract ID [{}]", new Object[]{updated, contractGroup.getId()});
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_contract_group_update", new Object[]{contractGroup.getId(), any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    }

    @Override
    public void deleteContractGroup(Long id) throws ServiceException {
        Integer count = jdbcTemplate.queryForObject("select count(*) from contract_group where id = ?", Integer.class, id);
        if (!count.equals(1)) {
            throw new ServiceException(messageSource.getMessage("contract_group_not_found_for_id", new Object[]{id}, LocaleContextHolder.getLocale()));
        }
        try {
        	//we first need to remove the association from the contract_service and contract_adjustment records
        	int cs_updated = jdbcTemplate.update("update contract_service set contract_group_id = null, updated = ?, updated_by = ? where contract_group_id = ?",
                    new Object[]{new DateTime().withZone(DateTimeZone.forID(TZID)).toDate(), authenticatedUser(), id});
        	log.info("Removed [{}] contract groups associations from contract services with group [{}]", new Object[]{cs_updated, id});
        	
        	int ca_updated = jdbcTemplate.update("update contract_adjustment set contract_group_id = null, updated = ?, updated_by = ? where contract_group_id = ?",
                    new Object[]{new DateTime().withZone(DateTimeZone.forID(TZID)).toDate(), authenticatedUser(), id});
        	log.info("Removed [{}] contract groups associations from contract adjustments with group [{}]", new Object[]{ca_updated, id});
        	
            int deleted = jdbcTemplate.update("delete from contract_group where id = ?", id);
            log.info("Deleted [{}] contract groups for contract ID [{}]", new Object[]{deleted, id});
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_contract_group_delete", new Object[]{id, any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    }

    @Override
    public ContractGroup contractGroup(Long id) throws ServiceException {
        Integer count = jdbcTemplate.queryForObject("select count(*) from contract_group where id = ?", Integer.class, id);
        if (!count.equals(1)) {
            throw new ServiceException(messageSource.getMessage("contract_group_not_found_for_id", new Object[]{id}, LocaleContextHolder.getLocale()));
        }
        String query = "select * from contract_group cg where cg.id = ?";
        return jdbcTemplate.queryForObject(query, new Object[]{id},
                new RowMapper<ContractGroup>() {
            @Override
            public ContractGroup mapRow(ResultSet rs, int i) throws SQLException {
                ContractGroup cg = new ContractGroup(
                        rs.getLong("id"),
                        rs.getLong("contract_id"),
                        rs.getString("name"),
                        rs.getString("description"));

                cg.setCreated(rs.getDate("created"));
                cg.setCreatedBy(rs.getString("created_by"));
                cg.setUpdated(rs.getDate("updated"));
                cg.setUpdatedBy(rs.getString("updated_by"));
                return cg;
            }
        });
    }

    @Override
    public ContractGroup findContractGroupByNameAndContractId(String name, Long contractId) throws ServiceException {
        String query = "select * from contract_group"
                + " where contract_id = ? and name = ?";
        List<ContractGroup> contractGroups = jdbcTemplate.query(query, new Object[]{contractId, name},
                new RowMapper<ContractGroup>() {
            @Override
            public ContractGroup mapRow(ResultSet rs, int i) throws SQLException {
                ContractGroup cg = new ContractGroup(
                        rs.getLong("id"),
                        rs.getLong("contract_id"),
                        rs.getString("name"),
                        rs.getString("description"));

                cg.setCreated(rs.getDate("created"));
                cg.setCreatedBy(rs.getString("created_by"));
                cg.setUpdated(rs.getDate("updated"));
                cg.setUpdatedBy(rs.getString("updated_by"));
                return cg;
            }
        });
        if (contractGroups == null || contractGroups.isEmpty()) {
            return null;
        }
        // there should only be one...
        return contractGroups.get(0);
    }

    @Override
    public List<ContractGroup> contractGroups() {
        String query = "select * from contract_group";
        return jdbcTemplate.query(query, new RowMapper<ContractGroup>() {
            @Override
            public ContractGroup mapRow(ResultSet rs, int i) throws SQLException {
                ContractGroup cg = new ContractGroup(
                        rs.getLong("id"),
                        rs.getLong("contract_id"),
                        rs.getString("name"),
                        rs.getString("description"));

                cg.setCreated(rs.getDate("created"));
                cg.setCreatedBy(rs.getString("created_by"));
                cg.setUpdated(rs.getDate("updated"));
                cg.setUpdatedBy(rs.getString("updated_by"));
                return cg;
            }
        });
    }

    @Override
    public List<Long> serviceOSPIds() {
        return jdbcTemplate.queryForList("select distinct osp_id from service where active = true and disabled = false order by name", Long.class);
    }

    @Override
    public Map<String, Long> servicesMap() {
        Map<String, Long> services = new TreeMap<String, Long>();
        String query = "select distinct id, name from service where active = true and disabled = false order by name";
        List<Map<String, Object>> results = jdbcTemplate.queryForList(query);
        for (Map<String, Object> result : results) {
            services.put((String) result.get("name"), (Long) result.get("id"));
        }
        return services;
    }

    @Override
    public List<Service> servicesIncludingContractServices() {
        List<Service> services = services();
        List<Service> servicesInContracts = servicesInContracts();
        for (Service serviceInContracts : servicesInContracts) {
            if (!services.contains(serviceInContracts)) {
                services.add(serviceInContracts);
            }
        }
        Collections.sort(services, new Comparator<Service>() {
            @Override
            public int compare(Service o1, Service o2) {
                int idx = 0;
                if (o1.getName() != null) {
                    if (o2.getName() == null) {
                        return 1;
                    }
                    idx = o1.getName().compareTo(o2.getName());
                    if (idx != 0) {
                        return idx;
                    }
                } else if (o2.getName() != null) {
                    return -1;
                }
                return idx;
            }
        });
        return services;
    }

    @Override
    public List<Service> services() {
        String query = "select distinct id service_id, code, osp_id, version, name, business_model"
                + " from service where active = true and disabled = false order by name";
        return jdbcTemplate.query(query, new RowMapper<Service>() {
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
    }

    @Override
    public List<Service> servicesInContracts() {
        StopWatch sw = new StopWatch();
        sw.start();
        List<Service> services = new ArrayList<Service>();
        String query = "select distinct svc.osp_id from contract_service csvc"
                + " left join service svc on svc.id = csvc.service_id order by svc.osp_id";
        List<Long> results = jdbcTemplate.queryForList(query, Long.class);
        for (Long result : results) {
            query = "select id service_id, code, osp_id, version, name, business_model from service"
                    + " where osp_id = ? and version = (select max(version) from service where osp_id = ?)";
            Service service = jdbcTemplate.queryForObject(query, new Object[] {result, result}, new RowMapper<Service>() {
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
            services.add(service);
        }
        sw.stop();
        log.debug("**** OSP Services in Contracts query took {} seconds ****", sw.getTotalTimeSeconds());
        return services;
    }
    
    @Override
    public Service serviceByOspIdAndActive(Long ospId) {
        String query = "select distinct id service_id, code, osp_id, version, name, business_model"
                + " from service where active = true and osp_id = ?";
        List<Service> services = jdbcTemplate.query(query, new Object[] {ospId}, new RowMapper<Service>() {
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
        
        if(services != null && !services.isEmpty()) {
        	return services.get(0);
        }
        
        return null;
    }

    /**
     * We need this method primarily because it joins the service table on the
     * service_expense_category table, which references the osp_id, NOT the PK
     * of the table. If other than 'active' Services are referenced, the count
     * of related expense categories will be a cartesian product.
     *
     * @deprecated no longer use the service_expense_category table
     * @return
     */
    @Override
    public List<Service> servicesAndExpenseCategoryCount() {
        Map<String, Object> params = new HashMap<String, Object>();
        String query = "select svc.id service_id, svc.code, svc.osp_id, svc.version, svc.name, svc.business_model,"
                + " count(sec.expense_category_id) as cost_mapping_count from service svc"
                + " left outer join service_expense_category sec on svc.osp_id = sec.osp_id"
                + " where svc.active = true and svc.disabled = false group by svc.id order by svc.name";
        return namedJdbcTemplate.query(query, params, new RowMapper<Service>() {
            @Override
            public Service mapRow(ResultSet rs, int i) throws SQLException {
                return new Service(
                        rs.getLong("service_id"),
                        rs.getString("code"),
                        rs.getString("osp_id"),
                        rs.getDouble("version"),
                        rs.getString("name"),
                        rs.getString("business_model"),
                        rs.getInt("cost_mapping_count"));
            }
        });
    }

    @Override
    public List<Service> servicesForBusinessModel(String businessModel) {
        Map<String, Object> params = new HashMap<String, Object>();
        String query = "select osp_id, name, business_model from service"
                + " where active = true and disabled = false";
        if (businessModel != null) {
            query += " and business_model = :business_model";
        }
        query += " group by osp_id";
        query += " order by business_model, name";
        if (businessModel != null) {
            params.put("business_model", businessModel);
        }
        return namedJdbcTemplate.query(query, params, new RowMapper<Service>() {
            @Override
            public Service mapRow(ResultSet rs, int i) throws SQLException {
                return new Service(
                        null, // no id
                        null, // no code
                        rs.getString("osp_id"),
                        null, // no version
                        rs.getString("name"),
                        rs.getString("business_model"));
            }
        });
    }

    @Override
    public Long saveContractAdjustment(ContractAdjustment contractAdjustment, boolean validateInvoiceConflict) throws ServiceException {
        if (contractAdjustment.getId() != null) {
            updateContractAdjustment(contractAdjustment, validateInvoiceConflict);
        }
        validateContractAdjustment(contractAdjustment, BatchResult.Operation.create, validateInvoiceConflict);

        try {
            SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
            jdbcInsert.withTableName("contract_adjustment").usingGeneratedKeyColumns("id");
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("contract_id", contractAdjustment.getContractId());
            if (contractAdjustment.getContractUpdateId() != null) {
                params.put("contract_update_id", contractAdjustment.getContractUpdateId());
            }
            if (contractAdjustment.getContractGroupId() != null) {
                params.put("contract_group_id", contractAdjustment.getContractGroupId());
            }
            params.put("adjustment", contractAdjustment.getAdjustment());
            params.put("adjustment_type", contractAdjustment.getAdjustmentType());
            params.put("note", contractAdjustment.getNote());
            params.put("start_date", contractAdjustment.getStartDate());
            params.put("end_date", contractAdjustment.getEndDate());
            params.put("status", contractAdjustment.getStatus());
            params.put("created_by", authenticatedUser());
            Number pk = jdbcInsert.executeAndReturnKey(new MapSqlParameterSource(params));
            return (Long) pk;
        } catch (Exception any) {
            String message = messageSource.getMessage("jdbc_error_contract_adjustment_insert", new Object[]{any.getMessage()}, LocaleContextHolder.getLocale());
            log.error(message, any);
            throw new ServiceException(message, any);
        }
    }

    @Override
    public void updateContractAdjustment(ContractAdjustment contractAdjustment, boolean validateInvoiceConflict) throws ServiceException {
        if (contractAdjustment.getId() == null) {
            throw new ServiceException(messageSource.getMessage("validation_error_contract_adjustment_id", null, LocaleContextHolder.getLocale()));
        }
        Integer count = jdbcTemplate.queryForObject("select count(*) from contract_adjustment where id = ?", Integer.class, contractAdjustment.getId());
        if (!count.equals(1)) {
            throw new ServiceException(messageSource.getMessage("contract_adjustment_not_found_for_id", new Object[]{contractAdjustment.getId()}, LocaleContextHolder.getLocale()));
        }
        validateContractAdjustment(contractAdjustment, BatchResult.Operation.update, validateInvoiceConflict);

        try {
            int updated = jdbcTemplate.update("update contract_adjustment set contract_id = ?,"
                    + " contract_update_id = ?, contract_group_id = ?, adjustment = ?, adjustment_type = ?, note = ?,"
                    + " start_date = ?, end_date = ?, status = ?, updated_by = ?, updated = ?"
                    + " where id = ?",
                    new Object[]{contractAdjustment.getContractId(), contractAdjustment.getContractUpdateId(), contractAdjustment.getContractGroupId(),
                contractAdjustment.getAdjustment(), contractAdjustment.getAdjustmentType(),
                contractAdjustment.getNote(), contractAdjustment.getStartDate(),
                contractAdjustment.getEndDate(), contractAdjustment.getStatus().name(), authenticatedUser(), new DateTime().withZone(DateTimeZone.forID(TZID)).toDate(),
                contractAdjustment.getId()});
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_contract_adjustment_update", new Object[]{contractAdjustment.getId(), any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    }

    @Override
    public ContractAdjustment contractAdjustment(Long id) throws ServiceException {
        Integer count = jdbcTemplate.queryForObject("select count(*) from contract_adjustment where id = ?", Integer.class, id);
        if (!count.equals(1)) {
            throw new ServiceException(messageSource.getMessage("contract_adjustment_not_found_for_id", new Object[]{id}, LocaleContextHolder.getLocale()));
        }

        String query = "select * from contract_adjustment ca where ca.id = ?";
        return jdbcTemplate.queryForObject(query, new Object[]{id},
                new RowMapper<ContractAdjustment>() {
            @Override
            public ContractAdjustment mapRow(ResultSet rs, int i) throws SQLException {
                return new ContractAdjustment(
                        rs.getLong("id"),
                        rs.getLong("contract_id"),
                        rs.getLong("contract_update_id"),
                        (rs.getLong("contract_group_id") == 0 ? null : rs.getLong("contract_group_id")),
                        rs.getBigDecimal("adjustment"),
                        rs.getString("adjustment_type"),
                        rs.getString("note"),
                        null, // month
                        rs.getDate("start_date"),
                        rs.getDate("end_date"),
                        Service.Status.valueOf(rs.getString("status")),
                        rs.getDate("created"),
                        rs.getString("created_by"),
                        rs.getDate("updated"),
                        rs.getString("updated_by"));
            }
        });
    }

    @Override
    public List<ContractAdjustment> contractAdjustments() {
        String query = "select * from contract_adjustment";
        return jdbcTemplate.query(query, new RowMapper<ContractAdjustment>() {
            @Override
            public ContractAdjustment mapRow(ResultSet rs, int i) throws SQLException {
                return new ContractAdjustment(
                        rs.getLong("id"),
                        rs.getLong("contract_id"),
                        rs.getLong("contract_update_id"),
                        (rs.getLong("contract_group_id") == 0 ? null : rs.getLong("contract_group_id")),
                        rs.getBigDecimal("adjustment"),
                        rs.getString("adjustment_type"),
                        rs.getString("note"),
                        null, // month
                        rs.getDate("start_date"),
                        rs.getDate("end_date"),
                        Service.Status.valueOf(rs.getString("status")),
                        rs.getDate("created"),
                        rs.getString("created_by"),
                        rs.getDate("updated"),
                        rs.getString("updated_by"));
            }
        });
    }

    @Override
    public void deleteContractAdjustment(Long id, boolean validateInvoiceConflict) throws ServiceException {
        Integer count = jdbcTemplate.queryForObject("select count(*) from contract_adjustment where id = ?", Integer.class, id);
        if (!count.equals(1)) {
            throw new ServiceException(messageSource.getMessage("contract_adjustment_not_found_for_id", new Object[]{id}, LocaleContextHolder.getLocale()));
        }

        if (validateInvoiceConflict) {
            ContractAdjustment contractAdjustment = contractAdjustment(id);
            Long contractId = contractAdjustment.getContractId();
            List<ContractInvoice> contractInvoices = contractInvoicesForContract(contractId);
            boolean invoiceConflictExists = contractInvoiceConflictExistsForContractAdjustment(contractInvoices, contractAdjustment, BatchResult.Operation.delete);
            if (invoiceConflictExists) {
                throw new ServiceException(messageSource.getMessage("batch_error_delete_adjustment_contract_invoice_conflict", null, LocaleContextHolder.getLocale()));
            }
        }

        try {
            int updated = jdbcTemplate.update("delete from contract_adjustment where id = ?", id);
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_contract_adjustment_delete", new Object[]{id, any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    }

    private void validateContractAdjustment(ContractAdjustment contractAdjustment, BatchResult.Operation op, boolean validateInvoiceConflict) throws ServiceException {
        if (contractAdjustment.getContractId() == null) {
            throw new ServiceException(messageSource.getMessage("validation_error_contract_reference", null, LocaleContextHolder.getLocale()));
        }
        Integer count = jdbcTemplate.queryForObject("select count(*) from contract where id = ?", Integer.class, contractAdjustment.getContractId());
        if (!count.equals(1)) {
            throw new ServiceException(messageSource.getMessage("contract_not_found_for_id", new Object[]{contractAdjustment.getContractId()}, LocaleContextHolder.getLocale()));
        }
        if (contractAdjustment.getContractUpdateId() != null) {
            count = jdbcTemplate.queryForObject("select count(*) from contract_update where id = ?", Integer.class, contractAdjustment.getContractUpdateId());
            if (!count.equals(1)) {
                throw new ServiceException(messageSource.getMessage("contract_update_not_found_for_id", new Object[]{contractAdjustment.getContractUpdateId()}, LocaleContextHolder.getLocale()));
            }
        }
        if (contractAdjustment.getStartDate() == null) {
            throw new ServiceException(messageSource.getMessage("validation_error_startdate", null, LocaleContextHolder.getLocale()));
        }
        
        if (contractAdjustment.getStatus() == null) {
            throw new ServiceException(messageSource.getMessage("validation_error_status", null, LocaleContextHolder.getLocale()));
        }

        if (validateInvoiceConflict) {
            Long contractId = contractAdjustment.getContractId();
            List<ContractInvoice> contractInvoices = contractInvoicesForContract(contractId);
            boolean invoiceConflictExists = contractInvoiceConflictExistsForContractAdjustment(contractInvoices, contractAdjustment, op);
            if (invoiceConflictExists) {
                if (op.equals(BatchResult.Operation.create)) {
                    throw new ServiceException(messageSource.getMessage("batch_error_create_adjustment_contract_invoice_conflict", null, LocaleContextHolder.getLocale()));
                } else {
                    throw new ServiceException(messageSource.getMessage("batch_error_update_adjustment_contract_invoice_conflict", null, LocaleContextHolder.getLocale()));
                }
            }
        }
    }

    @Override
    public Long saveContractService(Service service, boolean validateInvoiceConflict) throws ServiceException {
        if (service.getId() != null) {
            updateContractService(service, validateInvoiceConflict);
            return service.getId();
        }
        
        validateContractService(service, BatchResult.Operation.create, validateInvoiceConflict);
        
        Long pk = null;
        try {
            pk = dbSaveContractService(service);
            if (service.getRelatedLineItems() != null) {
                for (Service relatedService : service.getRelatedLineItems()) {
                	relatedService.setParentId(pk);
                    Long cpk = dbSaveContractService(relatedService);
                    
                    if(relatedService.getRelatedLineItems() != null) {
                    	for(Service grandChildService : relatedService.getRelatedLineItems()) {
                    		grandChildService.setParentId(cpk);
                    		dbSaveContractService(grandChildService);
                    	}
                    }
                }
            }
            return pk;
        } catch (Exception any) {
            String message = messageSource.getMessage("jdbc_error_contractservice_insert", new Object[]{any.getMessage()}, LocaleContextHolder.getLocale());
            log.error(message, any);
            any.printStackTrace();
            throw new ServiceException(message, any);
        }
    }
    
    private Long dbSaveContractService(Service service) throws ServiceException {
        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
        jdbcInsert.withTableName("contract_service").usingGeneratedKeyColumns("id");
        Map<String, Object> params = new HashMap<String, Object>();
        if(service.getParentId() != null) {
        	params.put("parent_id", service.getParentId());
        }
        params.put("contract_id", service.getContractId());
        params.put("service_id", service.getServiceId());
        if (service.getQuantity() != null) {
            params.put("quantity", service.getQuantity());
        }
        if (service.getContractGroupId() != null) {
            params.put("contract_group_id", service.getContractGroupId());
        }
        params.put("onetime_revenue", service.getOnetimeRevenue());
        params.put("recurring_revenue", service.getRecurringRevenue());
        params.put("note", service.getNote());
        params.put("start_date", service.getStartDate());
        params.put("end_date", service.getEndDate());
        params.put("quote_line_item_id", service.getQuoteLineItemId());
        params.put("contract_service_subscription_id", service.getContractServiceSubscriptionId());
        params.put("microsoft_365_subscription_config_id", service.getMicrosoft365SubscriptionConfigId());
        params.put("created_by", authenticatedUser());
        
        Boolean hidden = Boolean.FALSE;
        if(service.getHidden() != null) {
        	hidden = service.getHidden();
        	params.put("reason", service.getReason());
        }
        params.put("hidden", hidden);

        Service.Status status = service.getStatus();
        if (status == null) {
            status = Service.Status.active;
            service.setStatus(status);
        }
        params.put("status", status);

        Number pk = jdbcInsert.executeAndReturnKey(new MapSqlParameterSource(params));
        Long contractServiceId = (Long) pk;
        service.setId(contractServiceId); // why was this never done? what harm?
        
        if (service.getContractUpdateId() != null) {
            Integer count = jdbcTemplate.queryForObject("select count(*) from contract_update where id = ?", Integer.class, service.getContractUpdateId());
            if (!count.equals(1)) {
                throw new ServiceException(messageSource.getMessage("contract_update_not_found_for_id", new Object[]{service.getContractUpdateId()}, LocaleContextHolder.getLocale()));
            }
            generateContractUpdateContractService(service.getContractUpdateId(), (Long) pk, null, null);
        }
        if (service.getDeviceId() == null) {
            throw new ServiceException(messageSource.getMessage("validation_error_device_id", null, LocaleContextHolder.getLocale()));
        }
        
        generateOrUpdateContractServiceDevice((Long) pk, service.getDeviceId(), service.getDeviceName(), null, service.getDeviceUnitCount(), service.getLocationId());
        
        generateOrUpdateEmbeddedContractServices(contractServiceId, service);
        return (Long) pk;
    }
    
    private void generateOrUpdateEmbeddedContractServices(Long contractServiceId, Service service) throws ServiceException {
    	Long deviceId = service.getDeviceId();
    	
    	Device device = applicationDataDaoService.device(deviceId);
    	if(device == null) {
            log.warn("applicationDataDaoService.device(deviceId) returned null!");
    		//throw service exception
    	}
    	
    	//first we want to delete any existing embedded contract services. we will add them back below with updated info
    	List<Service> existingEmbeddedServices = embeddedContractServicesByParentId(contractServiceId);
    	if(existingEmbeddedServices != null) {
    		log.info("Found Existing Services: " + existingEmbeddedServices.size());
    		for(Service existingEmbeddedService: existingEmbeddedServices) {
    			deleteContractService(existingEmbeddedService.getId(), Boolean.FALSE);
    		}
    	}
    	
    	List<Device> relatedDevices = device.getRelatedDevices();
    	if(relatedDevices != null) {
    		for(Device relatedDevice: relatedDevices) {
    			if(Device.Relationship.embedded.equals(relatedDevice.getRelationship())) {
    				Service embeddedService = new Service();
    				
    				embeddedService.setParentId(contractServiceId);
    				embeddedService.setContractId(service.getContractId());
    				
    				if (service.getContractGroupId() != null) {
    					embeddedService.setContractGroupId(service.getContractGroupId());
    		        }
    				
    				embeddedService.setStartDate(service.getStartDate());
    				embeddedService.setEndDate(service.getEndDate());
    				embeddedService.setOnetimeRevenue(new BigDecimal(0));
    				embeddedService.setRecurringRevenue(new BigDecimal(0));
    				embeddedService.setDeviceId(relatedDevice.getId());
    				embeddedService.setDeviceUnitCount(relatedDevice.getSpecUnits());
    				embeddedService.setStatus(service.getStatus());
    				embeddedService.setHidden(Boolean.TRUE);
    				embeddedService.setReason(Service.Reason.embedded);
    				
    				Service serviceOffering = null;
    				if(relatedDevice.getDefaultOspId() != null) {
    					serviceOffering = applicationDataDaoService.findActiveServiceByOspId(relatedDevice.getDefaultOspId());
    				}
    				
    				if(serviceOffering == null) {
                    	//we'll set it to undefined if it can't find the service
                    	serviceOffering = applicationDataDaoService.findActiveServiceByOspId(OSP_SERVICE_ID_UNDEFINED);
                    }
    				embeddedService.setServiceId(serviceOffering.getServiceId());
    				
    				saveContractService(embeddedService, Boolean.FALSE);
    			}
    		}
    	}
    }
    
    @Async
    @Override
    public void generateEmbeddedServicesForSystem() throws ServiceException {
    	List<Device> devices = applicationDataDaoService.devices();
    	
    	for(Device device: devices) {
    		List<Device> relatedDevices = device.getRelatedDevices();
    		if(relatedDevices != null && relatedDevices.size() > 0) {
    			Boolean embeddedDevice = Boolean.FALSE;
    			for(Device relatedDevice: relatedDevices) {
    				if(Device.Relationship.embedded.equals(relatedDevice.getRelationship())) {
    					embeddedDevice = Boolean.TRUE;
    					break;
    				}
    			}
    			
    			if(embeddedDevice) {
    				Long deviceId = device.getId();
    				log.info("Embedded Device Found for: ID[" + deviceId + "]" + "  -- " + device.getDescription() + " -- " + device.getPartNumber());
    				
    				List<Service> services = contractServicesForDevice(deviceId);
    				for(Service service: services) {
    					log.info("Creating Embedded Devices for Contract Service ID [" + service.getId() + "] -- Contract ID [" + service.getContractId() + "]");
    					try {
    						generateOrUpdateEmbeddedContractServices(service.getId(), service);
    					} catch (Exception e) {
    						e.printStackTrace();
    					}
    				}
    				
    			}
    		}
    	}	
    }
    
    @Async
    @Override
    public void copyCostCategoryServiceMappingsToCostCategoryDevice() {
    	String query = "select sec.osp_id, sec.expense_category_id, sec.quantity"
                + " from service_expense_category sec";
    	List<ServiceExpenseCategory> serviceExpenseCategories = namedJdbcTemplate.query(query, new RowMapper<ServiceExpenseCategory>() {
            @Override
            public ServiceExpenseCategory mapRow(ResultSet rs, int i) throws SQLException {
                return new ServiceExpenseCategory(
                        rs.getInt("expense_category_id"),
                        rs.getLong("osp_id"),
                        null,
                        null,
                        rs.getInt("quantity"));
            }
        });
    	
    	Map<Long, List<ServiceExpenseCategory>> serviceExpenseMappings = new HashMap<Long, List<ServiceExpenseCategory>>();
    	for(ServiceExpenseCategory serviceExpenseCategory: serviceExpenseCategories) {
    		Long ospId = serviceExpenseCategory.getOspId();
    		
    		if(serviceExpenseMappings.get(ospId) == null) {
    			serviceExpenseMappings.put(ospId, new ArrayList<ServiceExpenseCategory>());
    		} 
    		serviceExpenseMappings.get(ospId).add(serviceExpenseCategory);
    	}
    	
    	Iterator it = serviceExpenseMappings.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            
            Long ospId = (Long) pair.getKey();
            List<ServiceExpenseCategory> serviceCostMappings = (List<ServiceExpenseCategory>) pair.getValue();
            try {
            	log.info("Reviewing Mappings for OSP ID [" + ospId + "]");
    			List<Device> devices = applicationDataDaoService.findDeviceByDefaultOSPId(ospId);
    			for(Device device: devices) {
    				log.info("Reviewing Mappings for Device [" + device.getDescription() + " -- " + device.getPartNumber() + "]");
    				List<DeviceExpenseCategory> costMappings = new ArrayList<DeviceExpenseCategory>();
    				for(ServiceExpenseCategory serviceCostMapping: serviceCostMappings) {
    					log.info("Creating Mappings for Expense Category [" + serviceCostMapping.getExpenseCategoryId() + "]");
    					DeviceExpenseCategory deviceMapping = new DeviceExpenseCategory(device.getId(), serviceCostMapping.getExpenseCategoryId(), serviceCostMapping.getQuantity(), Boolean.FALSE);
    					costMappings.add(deviceMapping);
    				}
    				
    				if(costMappings.size() > 0) {
    					costDaoService.saveOrUpdateDeviceCostMappings(device.getId(), costMappings);
    				}
    			}
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
            
            it.remove(); // avoids a ConcurrentModificationException
        }
    }
    
    private List<Service> embeddedContractServicesByParentId(Long parentId) throws ServiceException {
        log.debug("parentId [{}]", parentId);
    	String query = "select csvc.id, csvc.service_id, csvc.parent_id, csvc.contract_id, csvc.contract_group_id, service.code, service.osp_id, service.name, service.version,"
                + " csvc.onetime_revenue, csvc.recurring_revenue, csvc.note, csvc.quantity,"
                + " csvc.start_date, csvc.end_date, csvc.status, dev.id devid, csd.name dname, csd.unit_count dcount, csd.location_id dlocation, dev.part_number, dev.description ddescr, csvc.contract_service_subscription_id, csvc.microsoft_365_subscription_config_id "
                + " from contract_service csvc"
                + " inner join service service on service.id = csvc.service_id"
                + " inner join contract_service_device csd on csd.contract_service_id = csvc.id"
                + " inner join device dev on dev.id = csd.device_id"
                + " where csvc.parent_id = :parentId and csvc.hidden = true and csvc.reason = 'embedded'";
    	
    	Map<String, Object> params = new HashMap<String, Object>();
        params.put("parentId", parentId);
        
        List<Service> contractServices = namedJdbcTemplate.query(query, params, new RowMapper<Service>() {
            @Override
            public Service mapRow(ResultSet rs, int i) throws SQLException {
                return new Service(
                        rs.getLong("id"),
                        rs.getString("code"),
                        rs.getLong("parent_id"),
                        rs.getLong("contract_id"),
                        rs.getLong("contract_group_id"),
                        null,
                        rs.getLong("service_id"),
                        rs.getString("osp_id"),
                        rs.getDouble("version"),
                        rs.getString("name"),
                        rs.getBigDecimal("onetime_revenue"),
                        rs.getBigDecimal("recurring_revenue"),
                        rs.getInt("quantity"),
                        rs.getString("note"),
                        null,
                        rs.getDate("start_date"),
                        rs.getDate("end_date"),
                        rs.getLong("devid"),
                        rs.getString("dname"),
                        rs.getString("part_number"),
                        rs.getString("ddescr"),
                        rs.getInt("dcount"),
                        Service.Status.valueOf(rs.getString("status")),
                        (rs.getLong("contract_service_subscription_id") == 0 ? null : rs.getLong("contract_service_subscription_id")),
                        null,
                        (rs.getLong("microsoft_365_subscription_config_id") == 0 ? null : rs.getLong("microsoft_365_subscription_config_id")),
                        (rs.getInt("dlocation") == 0 ? null : rs.getInt("dlocation")));
            }
        });
        
        //expecting just one result
        if(contractServices.isEmpty()) {
            log.debug("embeddedContractServicesByParentId query returned empty list");
            return null;
        } else {
        	return contractServices;
        }
    }

    /**
     * update ALL unit cost Service total counts
     * 
     * @param serviceId
     * @throws ServiceException 
     */
    @Async
    @Override
    @Scheduled(cron = "0 10 23 * * *") // how about 11:10pm every day?
    public void updateUnitCostServiceTotals() throws ServiceException {
        if (!taskEnabled("update_unit_cost_totals")) {
            log.info("[updateUnitCostServiceTotals] triggered but is not enabled...");
            return;
        }
        log.info("Scheduled task: updateUnitCostServiceTotals starting");
        StopWatch sw = new StopWatch();
        sw.start();
        List<UnitCost> unitCosts = costDaoService.unitCosts();
        for (UnitCost uc : unitCosts) {
            Integer deviceTotalCount = costService.deviceTotalDeviceCountWithExpenseCategory(
                    uc.getAppliedDate(), uc.getAppliedDate().plusMonths(1).minusDays(1).withTime(23, 59, 59, 999), null, uc.getCustomerId(), null, uc.getExpenseCategoryId(), null);
            uc.setDeviceTotalUnits(deviceTotalCount);
            costDaoService.updateUnitCost(uc);
        }
        sw.stop();
        log.info("**** updateUnitCostServiceTotals took {} seconds ****", sw.getTotalTimeSeconds());
    }
    
    /**
     * A caller can insert a contract_service_device relationship record when -
     * device part number and device description are present - device part
     * number only is present - device description only is present
     *
     * The method looks up a device OR creates a new one, if necessary for the
     * incoming information.
     *
     * @param contractServiceId
     * @param devicePartNumber
     * @param deviceDescription
     * @param deviceName
     * @param note
     * @throws ServiceException
     */
    private void generateOrUpdateContractServiceDevice(Long contractServiceId, Long deviceId, String deviceName, String note, Integer unitCount, Integer locationId) throws ServiceException {
        String query = "select count(*) from contract_service_device where contract_service_id = ?";
        Integer count = jdbcTemplate.queryForObject(query, Integer.class, new Object[]{contractServiceId});

        if (count == 0) {
            SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                    .withTableName("contract_service_device");
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("contract_service_id", contractServiceId);
            params.put("device_id", deviceId);
            params.put("created_by", authenticatedUser());
            if (deviceName != null) {
                params.put("name", deviceName);
            }
            if (note != null) {
                params.put("note", note);
            }
            if (unitCount != null) {
                params.put("unit_count", unitCount);
            }
            if (locationId != null) {
                params.put("location_id", locationId);
            }
            jdbcInsert.execute(params);
        } else if (count == 1) {
            jdbcTemplate.update("update contract_service_device set name = ?, note = ?, unit_count = ?, device_id = ?, location_id = ? where contract_service_id = ?",
                    new Object[]{deviceName, note, unitCount, deviceId, locationId, contractServiceId});
        } else {
            log.warn(String.format("Unsupported condition exists for contract_service_device: a contract service [id = %s] may only be associated with one device", contractServiceId));
        }
    }

    private void generateContractUpdateContractService(Long contractUpdateId, Long contractServiceId, String note, String operation) {
        String query = "select count(*) from contract_update_contract_service where contract_service_id = ? and contract_update_id = ?";
        Integer count = jdbcTemplate.queryForObject(query, Integer.class, new Object[]{contractServiceId, contractUpdateId});

        if (count == 0) {
            SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                    .withTableName("contract_update_contract_service");
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("contract_update_id", contractUpdateId);
            params.put("contract_service_id", contractServiceId);
            params.put("created_by", authenticatedUser());
            if (note != null) {
                params.put("note", note);
            }
            if (operation != null) {
                params.put("operation", operation);
            }
            jdbcInsert.execute(params);
        }
    }

    private void validateContractService(Service service, BatchResult.Operation op, boolean validateInvoiceConflict) throws ServiceException {
        Date serviceStartDate = service.getStartDate();
        Date serviceEndDate = service.getEndDate();
        
        if (serviceStartDate == null) {
            throw new ServiceException(messageSource.getMessage("validation_error_startdate", null, LocaleContextHolder.getLocale()));
        }
        if (serviceEndDate == null) {
            throw new ServiceException(messageSource.getMessage("validation_error_enddate", null, LocaleContextHolder.getLocale()));
        }
        
    	if (service.getContractId() == null) {
            throw new ServiceException(messageSource.getMessage("validation_error_contract_reference", null, LocaleContextHolder.getLocale()));
        }
        Integer count = jdbcTemplate.queryForObject("select count(*) from contract where id = ?", Integer.class, service.getContractId());
        if (!count.equals(1)) {
            throw new ServiceException(messageSource.getMessage("contract_not_found_for_id", new Object[]{service.getContractId()}, LocaleContextHolder.getLocale()));
        } else {
        	Contract contract = contract(service.getContractId());
        	Date contractStartDate = contract.getStartDate();
        	Date contractEndDate = contract.getEndDate();
        	
        	if(!(serviceStartDate.equals(contractStartDate) || serviceStartDate.after(contractStartDate)) || !(serviceStartDate.equals(contractEndDate) || serviceStartDate.before(contractEndDate))) {
        		throw new ServiceException(messageSource.getMessage("validation_error_startdate_not_in_contract_date", new Object[]{service.getContractId()}, LocaleContextHolder.getLocale()));
        	}
        	
        	if(!(serviceEndDate.equals(contractStartDate) || serviceEndDate.after(contractStartDate)) || !(serviceEndDate.equals(contractEndDate) || serviceEndDate.before(contractEndDate))) {
        		throw new ServiceException(messageSource.getMessage("validation_error_enddate_not_in_contract_date", new Object[]{service.getContractId()}, LocaleContextHolder.getLocale()));
        	}
        }

        if (service.getServiceId() == null) {
            throw new ServiceException(messageSource.getMessage("validation_error_service_reference", null, LocaleContextHolder.getLocale()));
        }
        count = jdbcTemplate.queryForObject("select count(*) from service where id = ?", Integer.class, service.getServiceId());
        if (!count.equals(1)) {
            throw new ServiceException(messageSource.getMessage("service_not_found_for_id", new Object[]{service.getServiceId()}, LocaleContextHolder.getLocale()));
        }
        
        if (service.getContractUpdateId() != null) {
            count = jdbcTemplate.queryForObject("select count(*) from contract_update where id = ?", Integer.class, service.getContractUpdateId());
            if (!count.equals(1)) {
                throw new ServiceException(messageSource.getMessage("contractupdate_not_found_for_id", new Object[]{service.getContractUpdateId()}, LocaleContextHolder.getLocale()));
            }
        }
        if (service.getQuantity() != null && service.getQuantity().compareTo(0) < 0) {
            throw new ServiceException(messageSource.getMessage("validation_service_quantity_less_than_zero", null, LocaleContextHolder.getLocale()));
        }
        
        /*
        String devicePartNumber = service.getDevicePartNumber();
        String deviceDescription = service.getDeviceDescription();
        String deviceName = service.getDeviceName();
        if (StringUtils.isNotBlank(deviceName) && StringUtils.isBlank(devicePartNumber) && StringUtils.isBlank(deviceDescription)) {
            throw new ServiceException(messageSource.getMessage("validation_error_device_info", null, LocaleContextHolder.getLocale()));
        }*/
        
        if(service.getDeviceId() == null) {
        	throw new ServiceException(messageSource.getMessage("validation_error_device_id", null, LocaleContextHolder.getLocale()));
        }
        
        Long contractId = service.getContractId();
        List<ContractInvoice> contractInvoices = contractInvoicesForContract(contractId);
        
        if (service.getRelatedLineItems() != null && service.getRelatedLineItems().size() > 0) {
            for (Service relatedService : service.getRelatedLineItems()) {
                if (relatedService.getDeviceId() == null) {
                    throw new ServiceException(messageSource.getMessage("validation_error_related_device_id", null, LocaleContextHolder.getLocale()));
                }
                count = jdbcTemplate.queryForObject("select count(*) from device_relationship"
                        + " where device_id = ? and related_device_id = ?", Integer.class,
                        new Object[]{service.getDeviceId(), relatedService.getDeviceId()});
                
                if (!count.equals(1) && !BatchResult.Operation.update.equals(op)) {
                    throw new ServiceException(messageSource.getMessage("validation_error_missing_device_relationship", null, LocaleContextHolder.getLocale()));
                } else if (!count.equals(1)) { // an update
                    log.info(String.format("update operation: device with id [%s] is not a parent to [%s]. the relationship might have been removed...",
                            new Object[]{service.getDeviceId(), relatedService.getDeviceId()}));
                }
                if (relatedService.getStartDate() == null) {
                    throw new ServiceException(messageSource.getMessage("validation_error_related_startdate", null, LocaleContextHolder.getLocale()));
                }
                if (relatedService.getEndDate() == null) {
                    throw new ServiceException(messageSource.getMessage("validation_error_related_enddate", null, LocaleContextHolder.getLocale()));
                }
                if(relatedService.getStartDate().before(service.getStartDate()) || relatedService.getStartDate().after(service.getEndDate()) || relatedService.getEndDate().after(service.getEndDate()) || relatedService.getEndDate().before(service.getStartDate())) {
                	throw new ServiceException(messageSource.getMessage("validation_error_related_device_outside_dates", null, LocaleContextHolder.getLocale()));
                }
                
                //if the service ID is null, we want to pull it from the default service id
                Long relatedServiceId = getServiceIdForService(relatedService);
                relatedService.setServiceId(relatedServiceId);
                /*
                if (relatedService.getServiceId() == null) {
                	Long defaultOspId = OSP_SERVICE_ID_UNDEFINED;
                	if(relatedService.getDefaultOspId() != null) {
                		defaultOspId = relatedService.getDefaultOspId();
                	}
                	
                	Service serviceOffering = applicationDataDaoService.findActiveServiceByOspId(defaultOspId);
                    if(serviceOffering == null) {
                    	//we'll set it to undefined if it can't find the service
                    	serviceOffering = applicationDataDaoService.findActiveServiceByOspId(OSP_SERVICE_ID_UNDEFINED);
                    }
                    
                    relatedService.setServiceId(serviceOffering.getServiceId());
                }*/
                
                if (validateInvoiceConflict && op.equals(BatchResult.Operation.update)) {
                	BatchResult.Operation childOp = BatchResult.Operation.valueOf(relatedService.getOperation());
                    boolean invoiceConflictExists = contractInvoiceConflictExistsForContractService(contractInvoices, relatedService, childOp);
                    if (invoiceConflictExists) {
                        throw new ServiceException(messageSource.getMessage("batch_error_update_related_service_contract_invoice_conflict", null, LocaleContextHolder.getLocale()));
                    }
                }
                
                if (relatedService.getRelatedLineItems() != null && relatedService.getRelatedLineItems().size() > 0) {
	                for(Service grandChildService: relatedService.getRelatedLineItems()) {
	                	if (grandChildService.getDeviceId() == null) {
	                        throw new ServiceException(messageSource.getMessage("validation_error_related_device_id", null, LocaleContextHolder.getLocale()));
	                    }
	                    count = jdbcTemplate.queryForObject("select count(*) from device_relationship"
	                            + " where device_id = ? and related_device_id = ?", Integer.class,
	                            new Object[]{relatedService.getDeviceId(), grandChildService.getDeviceId()});
	                    if (!count.equals(1) && !BatchResult.Operation.update.equals(op)) {
	                        throw new ServiceException(messageSource.getMessage("validation_error_missing_device_relationship", null, LocaleContextHolder.getLocale()));
                            } else if (!count.equals(1)) { // an update
                                log.info(String.format("update operation: child device with id [%s] is not a parent to [%s]. the relationship might have been removed...",
                                        new Object[]{relatedService.getDeviceId(), grandChildService.getDeviceId()}));
	                    }
	                    if (grandChildService.getStartDate() == null) {
	                        throw new ServiceException(messageSource.getMessage("validation_error_related_startdate", null, LocaleContextHolder.getLocale()));
	                    }
	                    if (grandChildService.getEndDate() == null) {
	                        throw new ServiceException(messageSource.getMessage("validation_error_related_enddate", null, LocaleContextHolder.getLocale()));
	                    }
	                    if(grandChildService.getStartDate().before(relatedService.getStartDate()) || grandChildService.getStartDate().after(relatedService.getEndDate()) || grandChildService.getEndDate().after(relatedService.getEndDate()) || grandChildService.getEndDate().before(relatedService.getStartDate())) {
	                    	throw new ServiceException(messageSource.getMessage("validation_error_related_device_outside_dates", null, LocaleContextHolder.getLocale()));
	                    }
	                    
	                    //if the service ID is null, we want to pull it from the default service id
	                    Long grandChildServiceId = getServiceIdForService(grandChildService);
	                    grandChildService.setServiceId(grandChildServiceId);
	                    
	                    if (validateInvoiceConflict && op.equals(BatchResult.Operation.update)) {
	                    	BatchResult.Operation childOp = BatchResult.Operation.valueOf(grandChildService.getOperation());
	                        boolean invoiceConflictExists = contractInvoiceConflictExistsForContractService(contractInvoices, grandChildService, childOp);
	                        if (invoiceConflictExists) {
	                            throw new ServiceException(messageSource.getMessage("batch_error_update_related_service_contract_invoice_conflict", null, LocaleContextHolder.getLocale()));
	                        }
	                    }
	                }
                }
            }
        }

        if (validateInvoiceConflict) {
            boolean invoiceConflictExists = contractInvoiceConflictExistsForContractService(contractInvoices, service, op);
            if (invoiceConflictExists) {
                if (op.equals(BatchResult.Operation.create)) {
                    throw new ServiceException(messageSource.getMessage("batch_error_create_service_contract_invoice_conflict", null, LocaleContextHolder.getLocale()));
                } else {
                    throw new ServiceException(messageSource.getMessage("batch_error_update_service_contract_invoice_conflict", null, LocaleContextHolder.getLocale()));
                }
            }
        }
        
        if(service.getParentId() != null) {
        	Service parent = contractService(service.getParentId());
        	if(service.getStartDate().before(parent.getStartDate()) || service.getStartDate().after(parent.getEndDate()) || service.getEndDate().after(parent.getEndDate()) || service.getEndDate().before(parent.getStartDate())) {
            	throw new ServiceException(messageSource.getMessage("validation_error_related_device_outside_dates", null, LocaleContextHolder.getLocale()));
            }
        }
    }
    
    private Long getServiceIdForService(Service service) {
    	//if the service ID is null, we want to pull it from the default service id
    	Long serviceId = null;
        if (service.getServiceId() == null) {
        	Long defaultOspId = OSP_SERVICE_ID_UNDEFINED;
        	if(service.getDefaultOspId() != null) {
        		defaultOspId = service.getDefaultOspId();
        	}
        	
        	Service serviceOffering = applicationDataDaoService.findActiveServiceByOspId(defaultOspId);
            if(serviceOffering == null) {
            	//we'll set it to undefined if it can't find the service
            	serviceOffering = applicationDataDaoService.findActiveServiceByOspId(OSP_SERVICE_ID_UNDEFINED);
            }
            
            //relatedService.setServiceId(serviceOffering.getServiceId());
            serviceId = serviceOffering.getServiceId();
        } else {
        	serviceId = service.getServiceId();
        }
        return serviceId;
    }

    @Override
    public void updateContractService(Service service, boolean validateInvoiceConflict) throws ServiceException {
        if (service.getId() == null) {
            throw new ServiceException(messageSource.getMessage("validation_error_contractservice_id", null, LocaleContextHolder.getLocale()));
        }
        Integer count = jdbcTemplate.queryForObject("select count(*) from contract_service where id = ?", Integer.class, service.getId());
        if (!count.equals(1)) {
            throw new ServiceException(messageSource.getMessage("contractservice_not_found_for_id", new Object[]{service.getId()}, LocaleContextHolder.getLocale()));
        }
        
        /*
        if (service.getRelatedLineItems() != null) {
            for (Service relatedService : service.getRelatedLineItems()) {
                if (relatedService.getId() == null) {
                    throw new ServiceException(messageSource.getMessage("validation_error_contractservice_id", null, LocaleContextHolder.getLocale()));
                }
                count = jdbcTemplate.queryForObject("select count(*) from contract_service where id = ?", Integer.class, relatedService.getId());
                if (!count.equals(1)) {
                    throw new ServiceException(messageSource.getMessage("contractservice_not_found_for_id", new Object[]{relatedService.getId()}, LocaleContextHolder.getLocale()));
                }
            }
        }*/
        
        validateContractService(service, BatchResult.Operation.update, validateInvoiceConflict);
        try {
            dbUpdateContractService(service);
            if(service.getDetail() != null) {
            	createOrUpdateContractServiceDetail(service.getId(), service.getDetail());
            }
            if (service.getRelatedLineItems() != null) {
                log.warn("currently NOT creating or updating contract service details for contract service related line items!");
                for (Service relatedService : service.getRelatedLineItems()) {
                	BatchResult.Operation childOp = null;
                	if(!StringUtils.isEmpty(relatedService.getOperation())) {
                		try {
                			childOp = BatchResult.Operation.valueOf(relatedService.getOperation());
                		} catch (Exception e) {
                			//ignore
                		}
                	}
                	
                	Long relatedServiceId = relatedService.getId();
                	if (relatedServiceId == null) {
                		relatedServiceId = dbSaveContractService(relatedService);
                		relatedService.setId(relatedServiceId);
                	} else if(BatchResult.Operation.delete.equals(childOp)) {
                		deleteContractService(relatedServiceId, Boolean.TRUE);
                	} else if(BatchResult.Operation.unmap.equals(childOp)) {
                		relatedService.setParentId(null);
                		dbUpdateContractService(relatedService);
                	} else {
                		dbUpdateContractService(relatedService);
                	}
                    
                	//grandchildren
                    if(relatedService.getRelatedLineItems() != null) {
                    	for(Service grandChildService : relatedService.getRelatedLineItems()) {
                    		BatchResult.Operation grandChildOp = null;
                    		try {
                    			grandChildOp = BatchResult.Operation.valueOf(grandChildService.getOperation());
                    		} catch (Exception e) {
                    			//ignore
                    		}
                    		
                    		grandChildService.setParentId(relatedServiceId);
                    		if (grandChildService.getId() == null) {
                        		dbSaveContractService(grandChildService);
                        	} else if(BatchResult.Operation.delete.equals(grandChildOp)) {
                        		deleteContractService(grandChildService.getId(), Boolean.TRUE);
                        	} else if(BatchResult.Operation.unmap.equals(grandChildOp)) {
                        		grandChildService.setParentId(null);
                        		dbUpdateContractService(grandChildService);
                        	} else {
                        		dbUpdateContractService(grandChildService);
                        	}
                    	}
                    }
                }
            }
        } catch (Exception any) {
            any.printStackTrace();
            throw new ServiceException(messageSource.getMessage("jdbc_error_contractservice_update", new Object[]{service.getId(), any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    }
    
    private int dbUpdateContractService(Service service) throws ServiceException {
        int updated = jdbcTemplate.update("update contract_service set parent_id = ?, contract_id = ?, service_id = ?, contract_group_id = ?, quantity = ?,"
                + " onetime_revenue = ?, recurring_revenue = ?, note = ?, start_date = ?, end_date = ?, status = ?, contract_service_subscription_id = ?, microsoft_365_subscription_config_id = ?, updated_by = ?,"
                + " updated = ? where id = ?",
                new Object[]{service.getParentId(), service.getContractId(), service.getServiceId(), service.getContractGroupId(), service.getQuantity(),
            service.getOnetimeRevenue(), service.getRecurringRevenue(), service.getNote(), service.getStartDate(),
            service.getEndDate(), service.getStatus().name(), service.getContractServiceSubscriptionId(), service.getMicrosoft365SubscriptionConfigId(), authenticatedUser(), new DateTime().withZone(DateTimeZone.forID(TZID)).toDate(), service.getId()});
        if (service.getContractUpdateId() != null) {
            Integer cucount = jdbcTemplate.queryForObject("select count(*) from contract_update where id = ?", Integer.class, service.getContractUpdateId());
            if (!cucount.equals(1)) {
                throw new ServiceException(messageSource.getMessage("contract_update_not_found_for_id", new Object[]{service.getContractUpdateId()}, LocaleContextHolder.getLocale()));
            }
            generateContractUpdateContractService(service.getContractUpdateId(), service.getId(), null, null);
        }
        if (service.getDeviceId() == null) {
            throw new ServiceException(messageSource.getMessage("validation_error_device_id", null, LocaleContextHolder.getLocale()));
        }
        generateOrUpdateContractServiceDevice(service.getId(), service.getDeviceId(), service.getDeviceName(), null, service.getDeviceUnitCount(), service.getLocationId());
        generateOrUpdateEmbeddedContractServices(service.getId(), service);
        return updated;
    }
    
    @Override
    public List<ContractServiceDetail> contractServiceDetails() {
    	String query = "select csdl.contract_service_id, csdl.location, csdl.operating_system, csdl.cpu_count, csdl.memory_gb, csdl.storage_gb"
                + " from contract_service_detail csdl";
        List<ContractServiceDetail> contractServiceDetails = jdbcTemplate.query(query, new RowMapper<ContractServiceDetail>() {
            @Override
            public ContractServiceDetail mapRow(ResultSet rs, int i) throws SQLException {
                return new ContractServiceDetail(
                        rs.getLong("contract_service_id"),
                        rs.getString("location"),
                        rs.getString("operating_system"),
                        rs.getInt("cpu_count"),
                        rs.getBigDecimal("memory_gb"),
                        rs.getBigDecimal("storage_gb"));
            }
        });
        
        return contractServiceDetails;
    }
    
    @Override
    public void deleteContractServiceDetail(Long contractServiceId) throws ServiceException {
    	Integer count = jdbcTemplate.queryForObject("select count(*) from contract_service_detail where contract_service_id = ?", Integer.class, contractServiceId);
        if (!count.equals(1)) {
            throw new ServiceException(messageSource.getMessage("contractservicedetail_not_found_for_id", new Object[]{contractServiceId}, LocaleContextHolder.getLocale()));
        }

        try {
            int updated = jdbcTemplate.update("delete from contract_service_detail where contract_service_id = ?", contractServiceId);
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_contractservicedetail_delete", new Object[]{contractServiceId, any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    }
    
    private void createOrUpdateContractServiceDetail(Long contractServiceId, ContractServiceDetail detail) {
    	String query = "select count(*) from contract_service_detail where contract_service_id = ?";
        Integer count = jdbcTemplate.queryForObject(query, Integer.class, new Object[]{contractServiceId});

        if (count == 0) {
            SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                    .withTableName("contract_service_detail");
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("contract_service_id", contractServiceId);
            params.put("created_by", authenticatedUser());
            if (detail.getLocation() != null) {
                params.put("location", detail.getLocation());
            }
            if (detail.getCpuCount() != null) {
                params.put("cpu_count", detail.getCpuCount());
            }
            if (detail.getMemoryGB() != null) {
                params.put("memory_gb", detail.getMemoryGB());
            }
            if (detail.getStorageGB() != null) {
                params.put("storage_gb", detail.getStorageGB());
            }
            if (detail.getOperatingSystem() != null) {
                params.put("operating_system", detail.getOperatingSystem());
            }
            jdbcInsert.execute(params);
        } else if (count == 1) {
            jdbcTemplate.update("update contract_service_detail set location = ?, cpu_count = ?, memory_gb = ?, storage_gb = ?, operating_system = ? where contract_service_id = ?",
                    new Object[]{detail.getLocation(), detail.getCpuCount(), detail.getMemoryGB(), detail.getStorageGB(), detail.getOperatingSystem(), contractServiceId});
        } else {
            log.warn(String.format("Unsupported condition exists for contract_service_detail: a contract service [id = %s] may only be associated with one set of detail", contractServiceId));
        }
    }

    @Override
    public void deleteContractService(Long id, boolean validateInvoiceConflict) throws ServiceException {
        Integer count = jdbcTemplate.queryForObject("select count(*) from contract_service where id = ?", Integer.class, id);
        if (!count.equals(1)) {
            throw new ServiceException(messageSource.getMessage("contractservice_not_found_for_id", new Object[]{id}, LocaleContextHolder.getLocale()));
        }

        if (validateInvoiceConflict) {
            Service service = contractService(id);
            Long contractId = service.getContractId();
            List<ContractInvoice> contractInvoices = contractInvoicesForContract(contractId);
            boolean invoiceConflictExists = contractInvoiceConflictExistsForContractService(contractInvoices, service, BatchResult.Operation.delete);
            if (invoiceConflictExists) {
                throw new ServiceException(messageSource.getMessage("batch_error_delete_service_contract_invoice_conflict", null, LocaleContextHolder.getLocale()));
            }
        }

        try {
            Long serviceId = jdbcTemplate.queryForObject("select service_id from contract_service where id = ?", Long.class, id);
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_contractservice_update", new Object[]{id, any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }

        try {
            int updated = jdbcTemplate.update("delete from contract_service where id = ?", id);
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_contractservice_update", new Object[]{id, any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    }

    @Override
    public Service contractService(Long id) throws ServiceException {
        Integer count = jdbcTemplate.queryForObject("select count(*) from contract_service where id = ?", Integer.class, id);
        if (!count.equals(1)) {
            throw new ServiceException(messageSource.getMessage("contractservice_not_found_for_id", new Object[]{id}, LocaleContextHolder.getLocale()));
        }
        
        Service service = jdbcTemplate.queryForObject(ContractServiceRowMapper.QUERY + " where csvc.id = ?" + ContractServiceRowMapper.POST_QUERY, new Object[]{id}, new ContractServiceRowMapper());
        List<Service> relatedServices = jdbcTemplate.query(ContractServiceRowMapper.QUERY + " where csvc.parent_id = ? and csvc.hidden = false" + ContractServiceRowMapper.POST_QUERY, new Object[]{id}, new ContractServiceRowMapper());
        for(Service relatedService: relatedServices) {	
        	List<Service> grandChildLineItems = jdbcTemplate.query(ContractServiceRowMapper.QUERY + " where csvc.parent_id = ? and csvc.hidden = false" + ContractServiceRowMapper.POST_QUERY, new Object[]{relatedService.getId()}, new ContractServiceRowMapper());
        	relatedService.setRelatedLineItems(grandChildLineItems);
        }
        service.setRelatedLineItems(relatedServices);
        return service;
    }
    
    @Override
    public Service contractServiceBySNSysId(String snSysId) throws ServiceException {
    	try {
    		Long id = jdbcTemplate.queryForObject("select contract_service_id from service_now_ci where sn_sys_id = ?", Long.class, snSysId);
    		
        	if(id == null || id == 0L) {
        		return null;
        	}
        	
        	//log.info("Found Contract Service from SNSysId [ " + snSysId + " ] with ID [ " + id + " ]");
        	return contractService(id);
    	} catch (Exception e) {
    		//couldn't find a match -- we'll return null
    	}
    	
    	return null;
    }
    
    @Override
    public Service contractServiceByContractServiceSubscriptionAndStartDate(Long contractServiceSubscriptionId, Date startDate) throws ServiceException {
    	String query = "select csvc.id, csvc.service_id, csvc.parent_id, csvc.contract_id, csvc.contract_group_id, service.code, service.osp_id, service.name, service.version,"
                + " csvc.onetime_revenue, csvc.recurring_revenue, csvc.note, csvc.quantity,"
                + " csvc.start_date, csvc.end_date, csvc.status, dev.id devid, csd.name dname, csd.unit_count dcount, csd.location_id dlocation, dev.part_number, dev.description ddescr, csvc.contract_service_subscription_id, css.subscription_type, csvc.microsoft_365_subscription_config_id "
                + " from contract_service csvc"
                + " left join service service on service.id = csvc.service_id"
                + " left outer join contract_service_device csd on csd.contract_service_id = csvc.id"
                + " left outer join device dev on dev.id = csd.device_id"
                + " left outer join contract_service_subscription css on css.id = csvc.contract_service_subscription_id"
                + " where csvc.contract_service_subscription_id = :contractServiceSubscriptionId"
                + " and (csvc.start_date = :startDate or :startDate between csvc.start_date and csvc.end_date)";
    	
    	Map<String, Object> params = new HashMap<String, Object>();
        params.put("contractServiceSubscriptionId", contractServiceSubscriptionId);
        
        params.put("startDate", startDate);
        
        List<Service> contractServices = namedJdbcTemplate.query(query, params, new RowMapper<Service>() {
            @Override
            public Service mapRow(ResultSet rs, int i) throws SQLException {
                return new Service(
                        rs.getLong("id"),
                        rs.getString("code"),
                        rs.getLong("parent_id"),
                        rs.getLong("contract_id"),
                        rs.getLong("contract_group_id"),
                        null,
                        rs.getLong("service_id"),
                        rs.getString("osp_id"),
                        rs.getDouble("version"),
                        rs.getString("name"),
                        rs.getBigDecimal("onetime_revenue"),
                        rs.getBigDecimal("recurring_revenue"),
                        rs.getInt("quantity"),
                        rs.getString("note"),
                        null,
                        rs.getDate("start_date"),
                        rs.getDate("end_date"),
                        rs.getLong("devid"),
                        rs.getString("dname"),
                        rs.getString("part_number"),
                        rs.getString("ddescr"),
                        rs.getInt("dcount"),
                        Service.Status.valueOf(rs.getString("status")),
                        (rs.getLong("contract_service_subscription_id") == 0 ? null : rs.getLong("contract_service_subscription_id")),
                        rs.getString("subscription_type"),
                        (rs.getLong("microsoft_365_subscription_config_id") == 0 ? null : rs.getLong("microsoft_365_subscription_config_id")),
                        (rs.getInt("dlocation") == 0 ? null : rs.getInt("dlocation")));
            }
        });
        
        //expecting just one result
        if(contractServices.isEmpty()) {
        	return null;
        } else {
        	return contractServices.get(0);
        }
    }
    
    @Override
    public List<Service> contractServices() {
        List<Service> contractServices = jdbcTemplate.query(
                ContractServiceRowMapper.QUERY + ContractServiceRowMapper.POST_QUERY, new ContractServiceRowMapper());
        for (Service cs : contractServices) {
            // attach the contract service - contract updates
            List<ContractUpdate> contractUpdates = jdbcTemplate.query(
                    ContractServiceContractUpdateRowMapper.QUERY + " where csvc.id = ?" + ContractServiceContractUpdateRowMapper.POST_QUERY,
                    new Object[]{cs.getId()}, new ContractServiceContractUpdateRowMapper());
            cs.setContractUpdates(contractUpdates);
            // attach the nested/child contract services to the parents
            List<Service> nestedContractServices = jdbcTemplate.query(ContractServiceRowMapper.QUERY + " where csvc.parent_id = ?" + ContractServiceRowMapper.POST_QUERY,
                    new Object[]{cs.getId()}, new ContractServiceRowMapper());
            cs.setRelatedLineItems(nestedContractServices);
        }
        return contractServices;
    }

    private class ContractServiceContractUpdateRowMapper implements RowMapper<ContractUpdate> {
        
        static final String QUERY = "select cu.id, cu.alt_id, cu.job_number, cu.ticket_number, cu.note,"
                    + " cu.contract_id, cu.signed_date, cu.effective_date, cu.onetime_price, cu.recurring_price, cu.updated, cu.updated_by, cu.file_path, cucs.note,"
                    + " cucs.operation"
                    + " from contract_update_contract_service cucs"
                    + " left join contract_service csvc on cucs.contract_service_id = csvc.id"
                    + " left join contract_update cu on cucs.contract_update_id = cu.id";
        static final String POST_QUERY = " order by cucs.created desc";
        
        @Override
        public ContractUpdate mapRow(ResultSet rs, int i) throws SQLException {
            return new ContractUpdate(
                    rs.getLong("id"),
                    rs.getLong("contract_id"),
                    rs.getString("alt_id"),
                    rs.getString("job_number"),
                    rs.getString("ticket_number"),
                    rs.getString("note"),
                    rs.getDate("signed_date"),
                    rs.getDate("effective_date"),
                    rs.getBigDecimal("onetime_price"),
                    rs.getBigDecimal("recurring_price"),
                    rs.getDate("updated"),
                    rs.getString("updated_by"),
                    rs.getString("file_path"));
        }
    }
    
    private class ContractServiceRowMapper implements RowMapper<Service> {
        
        static final String QUERY = "select csvc.id, csvc.service_id, csvc.parent_id, csvc.contract_id, csvc.contract_group_id, service.code, service.osp_id, service.name, service.version,"
                + " csvc.onetime_revenue, csvc.recurring_revenue, csvc.note, csvc.quantity, count(clit.lineitem_id) lineitems,"
                + " csvc.start_date, csvc.end_date, csvc.status, dev.id devid, csd.name dname, csd.unit_count dcount, csd.location_id dlocation, dev.part_number, dev.description ddescr, csvc.contract_service_subscription_id, css.subscription_type, csvc.microsoft_365_subscription_config_id"
                + " from contract_service csvc"
                + " left join service service on service.id = csvc.service_id"
                + " left outer join contract_lineitem clit on clit.contract_service_id = csvc.id"
                + " left outer join contract_service_device csd on csd.contract_service_id = csvc.id"
                + " left outer join device dev on dev.id = csd.device_id"
                + " left outer join contract_service_subscription css on css.id = csvc.contract_service_subscription_id";
        static final String POST_QUERY =  " group by csvc.id, csvc.service_id, csvc.parent_id, csvc.contract_id, csvc.contract_group_id, service.code, service.osp_id, service.name, service.version,"
                + " csvc.onetime_revenue, csvc.recurring_revenue, csvc.note, csvc.quantity, csvc.start_date, csvc.end_date,"
                + " dev.id, csd.name, csd.unit_count, dev.part_number, dev.description";
        
        @Override
        public Service mapRow(ResultSet rs, int i) throws SQLException {
            return new Service(
                    rs.getLong("id"),
                    rs.getString("code"),
                    rs.getLong("parent_id"),
                    rs.getLong("contract_id"),
                    (rs.getLong("contract_group_id") == 0 ? null : rs.getLong("contract_group_id")),
                    null,
                    rs.getLong("service_id"),
                    rs.getString("osp_id"),
                    rs.getDouble("version"),
                    rs.getString("name"),
                    rs.getBigDecimal("onetime_revenue"),
                    rs.getBigDecimal("recurring_revenue"),
                    rs.getInt("quantity"),
                    rs.getString("note"),
                    rs.getInt("lineitems"),
                    rs.getDate("start_date"),
                    rs.getDate("end_date"),
                    rs.getLong("devid"),
                    rs.getString("dname"),
                    rs.getString("part_number"),
                    rs.getString("ddescr"),
                    rs.getInt("dcount"),
                    Service.Status.valueOf(rs.getString("status")),
                    (rs.getLong("contract_service_subscription_id") == 0 ? null : rs.getLong("contract_service_subscription_id")),
                    rs.getString("subscription_type"),
                    (rs.getLong("microsoft_365_subscription_config_id") == 0 ? null : rs.getLong("microsoft_365_subscription_config_id")),
                    (rs.getInt("dlocation") == 0 ? null : rs.getInt("dlocation")));
        }
    }
    
    @Override
    public List<Service> contractServicesForDevice(Long deviceId) {
        String query = "select csvc.id, csvc.service_id, csvc.parent_id, csvc.contract_id, csvc.contract_group_id, service.code, service.osp_id, service.name, service.version,"
                + " csvc.onetime_revenue, csvc.recurring_revenue, csvc.note, csvc.quantity,"
                + " csvc.start_date, csvc.end_date, csvc.status, dev.id devid, csd.name dname, csd.unit_count dcount, csd.location_id dlocation, dev.part_number, dev.description ddescr, csvc.contract_service_subscription_id, csvc.microsoft_365_subscription_config_id "
                + " from contract_service csvc"
                + " inner join service service on service.id = csvc.service_id"
                + " inner join contract_service_device csd on csd.contract_service_id = csvc.id"
                + " inner join device dev on dev.id = csd.device_id"
                + " where dev.id = ?";
        List<Service> contractServices = jdbcTemplate.query(query, new Object[] {deviceId}, new RowMapper<Service>() {
            @Override
            public Service mapRow(ResultSet rs, int i) throws SQLException {
                return new Service(
                        rs.getLong("id"),
                        rs.getString("code"),
                        rs.getLong("parent_id"),
                        rs.getLong("contract_id"),
                        (rs.getLong("contract_group_id") == 0 ? null : rs.getLong("contract_group_id")),
                        null,
                        rs.getLong("service_id"),
                        rs.getString("osp_id"),
                        rs.getDouble("version"),
                        rs.getString("name"),
                        rs.getBigDecimal("onetime_revenue"),
                        rs.getBigDecimal("recurring_revenue"),
                        rs.getInt("quantity"),
                        rs.getString("note"),
                        null,
                        rs.getDate("start_date"),
                        rs.getDate("end_date"),
                        rs.getLong("devid"),
                        rs.getString("dname"),
                        rs.getString("part_number"),
                        rs.getString("ddescr"),
                        rs.getInt("dcount"),
                        Service.Status.valueOf(rs.getString("status")),
                        (rs.getLong("contract_service_subscription_id") == 0 ? null : rs.getLong("contract_service_subscription_id")),
                        null,
                        (rs.getLong("microsoft_365_subscription_config_id") == 0 ? null : rs.getLong("microsoft_365_subscription_config_id")),
                        (rs.getInt("dlocation") == 0 ? null : rs.getInt("dlocation")));
            }
        });
        for (Service cs : contractServices) {
            // attach the nested/child contract services to the parents
            List<Service> nestedContractServices = jdbcTemplate.query(ContractServiceRowMapper.QUERY + " where csvc.parent_id = ?" + ContractServiceRowMapper.POST_QUERY,
                    new Object[]{cs.getId()}, new ContractServiceRowMapper());
            cs.setRelatedLineItems(nestedContractServices);
        }
        
        return contractServices;
    }

    @Override
    public boolean contractInvoiceConflictExistsForContractService(List<ContractInvoice> contractInvoices, Service service, BatchResult.Operation op) {
        boolean conflictExists = false;
        try {
            if (contractInvoices != null && contractInvoices.size() > 0) {
                DateTime serviceStartDate = new DateTime(service.getStartDate());
                DateTime serviceEndDate = new DateTime(service.getEndDate());

                if (op == null) {
                    return false;
                }

                //make sure no invoices exist within the services date range -- if so, we can't create, update or delete from that period
                conflictExists = contractInvoiceConflictForDateRange(contractInvoices, serviceStartDate, serviceEndDate);

                if (op.equals(BatchResult.Operation.update)) {
                    Service existingService = contractService(service.getId());

                    if (existingService != null) {
                        //if (existingService status is active) &&                          
                        //if there's not already a conflict, we need to check the existing date range to make sure there's not a conflict 
                        // ... reason: if dates are changed on an active service then only allow if no conflicts with previous dates
                        if (!conflictExists && (existingService.getStatus().equals(Service.Status.active))) {
                            DateTime existingServiceStartDate = new DateTime(existingService.getStartDate());
                            DateTime existingServiceEndDate = new DateTime(existingService.getEndDate());
                            conflictExists = contractInvoiceConflictForDateRange(contractInvoices, existingServiceStartDate, existingServiceEndDate);
                        }

                        if (conflictExists) {
                            //if the core fields haven't changed, then allow them to save it. -- we don't care if they change ci name, notes, etc
                            if (existingService.getStartDate().equals(service.getStartDate()) && existingService.getEndDate().equals(service.getEndDate())
                                    && existingService.getOnetimeRevenue().equals(service.getOnetimeRevenue()) && existingService.getRecurringRevenue().equals(service.getRecurringRevenue())
                                    && existingService.getStatus().equals(service.getStatus())) {
                                conflictExists = false;
                            } else if (existingService.getStartDate().equals(service.getStartDate()) && !existingService.getEndDate().equals(service.getEndDate())
                                    && existingService.getOnetimeRevenue().equals(service.getOnetimeRevenue()) && existingService.getRecurringRevenue().equals(service.getRecurringRevenue())
                                    && existingService.getStatus().equals(service.getStatus())) {
                                conflictExists = contractInvoiceConflictForEndDateChange(contractInvoices, new DateTime(service.getEndDate()));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
        	e.printStackTrace();
        }
        return conflictExists;
    }

    private boolean contractInvoiceConflictExistsForContractAdjustment(List<ContractInvoice> contractInvoices, ContractAdjustment adjustment, BatchResult.Operation op) {
        boolean conflictExists = false;
        try {
            if (contractInvoices != null && contractInvoices.size() > 0) {
                DateTime adjustmentStartDate = new DateTime(adjustment.getStartDate());
                DateTime adjustmentEndDate = new DateTime(adjustment.getEndDate());

                if (op == null) {
                    return false;
                }

                //make sure no invoices exist within the services date range -- if so, we can't create or delete from that period
                conflictExists = contractInvoiceConflictForDateRange(contractInvoices, adjustmentStartDate, adjustmentEndDate);

                if (op.equals(BatchResult.Operation.update)) {
                    ContractAdjustment existingAdjustment = contractAdjustment(adjustment.getId());

                    if (existingAdjustment != null) {
                        if (!conflictExists) {
                            DateTime existingAdjustmentStartDate = new DateTime(existingAdjustment.getStartDate());
                            DateTime existingAdjustmentEndDate = new DateTime(existingAdjustment.getEndDate());
                            conflictExists = contractInvoiceConflictForDateRange(contractInvoices, existingAdjustmentStartDate, existingAdjustmentEndDate);
                        }

                        if (conflictExists) {
                            //if the core fields haven't changed, then allow them to save it. -- we don't care if they change ci name, notes, etc
                            if (existingAdjustment.getStartDate().equals(adjustment.getStartDate()) && existingAdjustment.getEndDate().equals(adjustment.getEndDate())
                                    && existingAdjustment.getAdjustment().equals(adjustment.getAdjustment()) && existingAdjustment.getAdjustmentType().equals(adjustment.getAdjustmentType())) {
                                conflictExists = false;
                            } else if (existingAdjustment.getStartDate().equals(adjustment.getStartDate()) && !existingAdjustment.getEndDate().equals(adjustment.getEndDate())
                                    && existingAdjustment.getAdjustment().equals(adjustment.getAdjustment()) && existingAdjustment.getAdjustmentType().equals(adjustment.getAdjustmentType())) {
                                //only end date has changed -- we need to see if it conflicts with any other invoices
                                conflictExists = contractInvoiceConflictForEndDateChange(contractInvoices, new DateTime(adjustment.getEndDate()));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conflictExists;
    }

    private boolean contractInvoiceConflictForEndDateChange(List<ContractInvoice> contractInvoices, DateTime endDate) {
        boolean conflictExists = false;
        ContractInvoice latestInvoicedMonth = null;
        for (ContractInvoice contractInvoice : contractInvoices) {
            if (ContractInvoice.Status.invoiced.equals(contractInvoice.getStatus())) {
                DateTime invoiceStartDate = new DateTime(contractInvoice.getStartDate());
                DateTime invoiceEndDate = new DateTime(contractInvoice.getEndDate());

                if (endDate.isBefore(invoiceStartDate) || endDate.isEqual(invoiceStartDate) || endDate.isEqual(invoiceEndDate) || (endDate.isAfter(invoiceStartDate) && endDate.isBefore(invoiceEndDate))) {
                    //add if/else logic to see if end date is the last day of the last invoiced month
                    conflictExists = true;
                    //break;
                }

                //we find out the last invoiced month to later check if they want to switch the end date to the last day of that month
                if (latestInvoicedMonth == null) {
                    latestInvoicedMonth = contractInvoice;
                } else if (invoiceStartDate.isAfter(new DateTime(latestInvoicedMonth.getStartDate()))) {
                    latestInvoicedMonth = contractInvoice;
                }
            }
        }

        if (conflictExists && endDate.isEqual(new DateTime(latestInvoicedMonth.getEndDate()))) {
            conflictExists = false;
        }

        return conflictExists;
    }

    private boolean contractInvoiceConflictForDateRange(List<ContractInvoice> contractInvoices, DateTime startDate, DateTime endDate) {
        boolean conflictExists = false;
        for (ContractInvoice contractInvoice : contractInvoices) {
            if (ContractInvoice.Status.invoiced.equals(contractInvoice.getStatus())) {
                DateTime invoiceStartDate = new DateTime(contractInvoice.getStartDate());
                DateTime invoiceEndDate = new DateTime(contractInvoice.getEndDate());

                if (startDate.isEqual(invoiceStartDate) || endDate.isEqual(invoiceEndDate)
                        || (startDate.isBefore(invoiceStartDate) && endDate.isAfter(invoiceStartDate))
                        || (startDate.isBefore(invoiceEndDate) && endDate.isAfter(invoiceEndDate))) {
                    conflictExists = true;
                    break;
                }
            }
        }
        return conflictExists;
    }
    
    private boolean contractInvoiceConflictExistsForContractServiceSubscription(List<ContractInvoice> contractInvoices, ContractServiceSubscription subscription, BatchResult.Operation op) {
        boolean conflictExists = false;
        try {
            if (contractInvoices != null && contractInvoices.size() > 0) {
                DateTime subscriptionStartDate = new DateTime(subscription.getStartDate());
                DateTime subscriptionEndDate = new DateTime(subscription.getEndDate());

                if (op == null) {
                    return false;
                }

                //make sure no invoices exist within the services date range -- if so, we can't create, update or delete from that period
                conflictExists = contractInvoiceConflictForDateRange(contractInvoices, subscriptionStartDate, subscriptionEndDate);

                if (op.equals(BatchResult.Operation.update)) {
                    ContractServiceSubscription existingSubscription = contractServiceSubscription(subscription.getId());

                    if (existingSubscription != null) {                          
                        //if there's not already a conflict, we need to check the existing date range to make sure there's not a conflict 
                        // ... reason: if dates are changed on an active service then only allow if no conflicts with previous dates
                        if (!conflictExists) {
                            DateTime existingSubscriptionStartDate = new DateTime(existingSubscription.getStartDate());
                            DateTime existingSubscriptionEndDate = new DateTime(existingSubscription.getEndDate());
                            conflictExists = contractInvoiceConflictForDateRange(contractInvoices, existingSubscriptionStartDate, existingSubscriptionEndDate);
                        }

                        if (conflictExists) {
                            //if the core fields haven't changed, then allow them to save it. -- we don't care if they change ci name, etc
                            if (existingSubscription.getStartDate().equals(subscription.getStartDate()) && existingSubscription.getEndDate().equals(subscription.getEndDate()) && ((existingSubscription.getSubscriptionId() == null && subscription.getSubscriptionId() != null)|| existingSubscription.getSubscriptionId().equals(subscription.getSubscriptionId()))) {
                                conflictExists = false;
                            } else if (existingSubscription.getStartDate().equals(subscription.getStartDate()) && !existingSubscription.getEndDate().equals(subscription.getEndDate()) && ((existingSubscription.getSubscriptionId() == null && subscription.getSubscriptionId() != null)|| existingSubscription.getSubscriptionId().equals(subscription.getSubscriptionId()))) {
                                conflictExists = contractInvoiceConflictForEndDateChange(contractInvoices, subscriptionEndDate);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conflictExists;
    }

    @Override
    public List<BatchResult> batchContractServices(Service[] services) throws ServiceException {

        List<BatchResult> results = new ArrayList<BatchResult>();
        Long contractId = null;
        List<ContractInvoice> contractInvoices = null;
        try {
            for (Service service : services) {
                contractId = service.getContractId();
                break;
            }
            contractInvoices = contractInvoicesForContract(contractId);
        } catch (Exception e) {
            //general exception
        	e.printStackTrace();
        }
        
        for (Service service : services) {
            if (service.getOperation() != null) {
                BatchResult.Operation op = null;
                try {
                    op = BatchResult.Operation.valueOf(service.getOperation());
                } catch (IllegalArgumentException iae) {
                    results.add(new BatchResult(service.getId(), service.getParentId(), service.getCorrelationId(), messageSource.getMessage("batch_error_bad_operation", new Object[]{service.getId(), service.getName()}, LocaleContextHolder.getLocale()), null, BatchResult.Result.failed));
                    continue;
                }

                if (op.equals(BatchResult.Operation.delete)) {
                	//we're going to get the existing service from the DB here to prevent users from changing values then marking it for "delete", 
                	//which can bypass some of the validation
                	service = contractService(service.getId());
                }
                
                boolean invoiceConflictExists = contractInvoiceConflictExistsForContractService(contractInvoices, service, op);
                
                if (op.equals(BatchResult.Operation.delete)) {                	
                    if (!invoiceConflictExists) {
                        try {
                            deleteContractService(service.getId(), false);
                            results.add(new BatchResult(service.getId(), service.getParentId(), service.getCorrelationId(), messageSource.getMessage("api_ok_batch_response_single", new Object[]{op.toString(), service.getId()}, LocaleContextHolder.getLocale()), op, BatchResult.Result.success));
                        } catch (ServiceException se) {
                            results.add(new BatchResult(service.getId(), service.getCorrelationId(), service.getCorrelationId(), se.getMessage(), op, BatchResult.Result.failed));
                        } catch (Exception other) {
                            BatchResult result = new BatchResult(service.getId(), service.getParentId(), service.getCorrelationId(), messageSource.getMessage("batch_error_delete_exception", new Object[]{service.getId(), service.getName()}, LocaleContextHolder.getLocale()), op, BatchResult.Result.failed);
                            result.newMeta().addException(other);
                            results.add(result);
                        }
                    } else {
                        BatchResult result = new BatchResult(service.getId(), service.getParentId(), service.getCorrelationId(), messageSource.getMessage("batch_error_delete_service_contract_invoice_conflict", null, LocaleContextHolder.getLocale()), op, BatchResult.Result.failed);
                        results.add(result);
                        break;
                    }
                } else if (op.equals(BatchResult.Operation.create)) {
                    if (!invoiceConflictExists) {
                        try {
                            saveContractService(service, false);
                            results.add(new BatchResult(service.getId(), service.getParentId(), service.getCorrelationId(), messageSource.getMessage("api_ok_batch_response_single", new Object[]{op.toString(), service.getId()}, LocaleContextHolder.getLocale()), op, BatchResult.Result.success));
                            for (Service ch : service.getRelatedLineItems()) {
                                results.add(new BatchResult(ch.getId(), ch.getParentId(), ch.getCorrelationId(), messageSource.getMessage("api_ok_batch_response_single", new Object[]{op.toString(), ch.getId()}, LocaleContextHolder.getLocale()), op, BatchResult.Result.success));
                                for (Service gch : ch.getRelatedLineItems()) {
                                    results.add(new BatchResult(gch.getId(), gch.getParentId(), gch.getCorrelationId(), messageSource.getMessage("api_ok_batch_response_single", new Object[]{op.toString(), service.getId()}, LocaleContextHolder.getLocale()), op, BatchResult.Result.success));
                                }
                            }
                        } catch (ServiceException se) {
                            results.add(new BatchResult(service.getId(), service.getParentId(), service.getCorrelationId(), se.getMessage(), op, BatchResult.Result.failed));
                        } catch (Exception other) {
                            BatchResult result = new BatchResult(service.getId(), service.getParentId(), service.getCorrelationId(),messageSource.getMessage("batch_error_save_exception", new Object[]{service.getServiceId(), service.getName()}, LocaleContextHolder.getLocale()), op, BatchResult.Result.failed);
                            result.newMeta().addException(other);
                            results.add(result);
                        }
                    } else {
                        BatchResult result = new BatchResult(service.getId(), service.getParentId(),service.getCorrelationId(),messageSource.getMessage("batch_error_create_service_contract_invoice_conflict", null, LocaleContextHolder.getLocale()),op,BatchResult.Result.failed);
                        results.add(result);
                        break;
                    }
                } else if (op.equals(BatchResult.Operation.update)) {
                    if (!invoiceConflictExists) {
                        try {
                            updateContractService(service, true);
                            results.add(new BatchResult(service.getId(), service.getParentId(), service.getCorrelationId(), messageSource.getMessage("api_ok_batch_response_single", new Object[]{op.toString(), service.getId()}, LocaleContextHolder.getLocale()), op, BatchResult.Result.success));
                            for (Service ch : service.getRelatedLineItems()) {
                                results.add(new BatchResult(ch.getId(), ch.getParentId(), ch.getCorrelationId(), messageSource.getMessage("api_ok_batch_response_single", new Object[]{op.toString(), ch.getId()}, LocaleContextHolder.getLocale()), op, BatchResult.Result.success));
                                for (Service gch : ch.getRelatedLineItems()) {
                                    results.add(new BatchResult(gch.getId(), gch.getParentId(), gch.getCorrelationId(), messageSource.getMessage("api_ok_batch_response_single", new Object[]{op.toString(), service.getId()}, LocaleContextHolder.getLocale()), op, BatchResult.Result.success));
                                }
                            }
                        } catch (ServiceException se) {
                            se.printStackTrace();
                            results.add(new BatchResult(service.getId(), service.getParentId(), service.getCorrelationId(), se.getMessage(), op, BatchResult.Result.failed));
                            throw new ServiceException(se.getMessage());
                        } catch (Exception other) {
                            other.printStackTrace();
                            BatchResult result = new BatchResult(service.getId(), service.getParentId(), service.getCorrelationId(), messageSource.getMessage("batch_error_update_exception", new Object[]{service.getId(), service.getName()}, LocaleContextHolder.getLocale()), op, BatchResult.Result.failed);
                            result.newMeta().addException(other);
                            results.add(result);
                        }
                    } else {
                        BatchResult result = new BatchResult(service.getId(), service.getParentId(), service.getCorrelationId(), messageSource.getMessage("batch_error_update_service_contract_invoice_conflict", null, LocaleContextHolder.getLocale()), op, BatchResult.Result.failed);
                        results.add(result);
                        break;
                    }
                }
            } else {
                results.add(new BatchResult(service.getId(), service.getParentId(), service.getCorrelationId(), messageSource.getMessage("batch_error_no_operation", new Object[]{service.getId(), service.getName()}, LocaleContextHolder.getLocale()), null, BatchResult.Result.failed));
            }
        }
        
        return results;
    }

    @Override
    public List<BatchResult> batchContractAdjustments(ContractAdjustment[] contractAdjustments) {

        List<BatchResult> results = new ArrayList<BatchResult>();
        Long contractId = null;
        List<ContractInvoice> contractInvoices = null;
        try {
            for (ContractAdjustment adjustment : contractAdjustments) {
                contractId = adjustment.getContractId();
                break;
            }
            contractInvoices = contractInvoicesForContract(contractId);
        } catch (Exception e) {
            //general exception
        }
        for (ContractAdjustment adjustment : contractAdjustments) {
            if (adjustment.getOperation() != null) {
                BatchResult.Operation op = null;
                try {
                    op = BatchResult.Operation.valueOf(adjustment.getOperation());
                } catch (IllegalArgumentException iae) {
                    results.add(new BatchResult(adjustment.getId(), messageSource.getMessage("batch_error_bad_operation", new Object[]{adjustment.getId()}, LocaleContextHolder.getLocale()), null, BatchResult.Result.failed));
                    continue;
                }

                boolean invoiceConflictExists = contractInvoiceConflictExistsForContractAdjustment(contractInvoices, adjustment, op);
                if (op.equals(BatchResult.Operation.delete)) {
                    if (!invoiceConflictExists) {
                        try {
                            deleteContractAdjustment(adjustment.getId(), false);
                            results.add(new BatchResult(adjustment.getId(), messageSource.getMessage("api_ok_batch_response_single", new Object[]{op.toString(), adjustment.getId()}, LocaleContextHolder.getLocale()), op, BatchResult.Result.success));
                        } catch (ServiceException se) {
                            results.add(new BatchResult(adjustment.getId(), se.getMessage(), op, BatchResult.Result.failed));
                        } catch (Exception other) {
                            BatchResult result = new BatchResult(adjustment.getId(), messageSource.getMessage("batch_error_delete_exception", new Object[]{adjustment.getId()}, LocaleContextHolder.getLocale()), op, BatchResult.Result.failed);
                            result.newMeta().addException(other);
                            results.add(result);
                        }
                    } else {
                        BatchResult result = new BatchResult(adjustment.getId(), messageSource.getMessage("batch_error_delete_adjustment_contract_invoice_conflict", null, LocaleContextHolder.getLocale()), op, BatchResult.Result.failed);
                        results.add(result);
                        break;
                    }
                    //maybe take this create out    
                } else if (op.equals(BatchResult.Operation.create)) {
                    if (!invoiceConflictExists) {
                        try {
                            saveContractAdjustment(adjustment, false);
                            results.add(new BatchResult(adjustment.getId(), messageSource.getMessage("api_ok_batch_response_single", new Object[]{op.toString(), adjustment.getId()}, LocaleContextHolder.getLocale()), op, BatchResult.Result.success));
                        } catch (ServiceException se) {
                            results.add(new BatchResult(adjustment.getId(), se.getMessage(), op, BatchResult.Result.failed));
                        } catch (Exception other) {
                            BatchResult result = new BatchResult(adjustment.getId(), messageSource.getMessage("batch_error_save_exception", new Object[]{adjustment.getId(), null}, LocaleContextHolder.getLocale()), op, BatchResult.Result.failed);
                            result.newMeta().addException(other);
                            results.add(result);
                        }
                    } else {
                        BatchResult result = new BatchResult(adjustment.getId(), messageSource.getMessage("batch_error_create_adjustment_contract_invoice_conflict", null, LocaleContextHolder.getLocale()), op, BatchResult.Result.failed);
                        results.add(result);
                        break;
                    }
                } else if (op.equals(BatchResult.Operation.update)) {
                    if (!invoiceConflictExists) {
                        try {
                            updateContractAdjustment(adjustment, false);
                            results.add(new BatchResult(adjustment.getId(), messageSource.getMessage("api_ok_batch_response_single", new Object[]{op.toString(), adjustment.getId()}, LocaleContextHolder.getLocale()), op, BatchResult.Result.success));
                        } catch (ServiceException se) {
                            results.add(new BatchResult(adjustment.getId(), se.getMessage(), op, BatchResult.Result.failed));
                        } catch (Exception other) {
                            BatchResult result = new BatchResult(adjustment.getId(), messageSource.getMessage("batch_error_update_exception", new Object[]{adjustment.getId()}, LocaleContextHolder.getLocale()), op, BatchResult.Result.failed);
                            result.newMeta().addException(other);
                            results.add(result);
                        }
                    } else {
                        BatchResult result = new BatchResult(adjustment.getId(), messageSource.getMessage("batch_error_update_adjustment_contract_invoice_conflict", null, LocaleContextHolder.getLocale()), op, BatchResult.Result.failed);
                        results.add(result);
                        break;
                    }
                }
            } else {
                results.add(new BatchResult(adjustment.getId(), messageSource.getMessage("batch_error_no_operation", new Object[]{adjustment.getId()}, LocaleContextHolder.getLocale())));
            }
        }
        return results;
    }

    @Override
    public List<Service> searchContractServices(String name, Long contractId) {
        String query = "select csvc.id, csvc.service_id, csvc.parent_id, csvc.contract_id, csvc.contract_group_id, service.code, service.osp_id, service.name, service.version,"
                + " csvc.onetime_revenue, csvc.recurring_revenue, csvc.note, csvc.quantity, count(clit.lineitem_id) lineitems,"
                + " csvc.start_date, csvc.end_date, csvc.status, dev.id devid, csd.name dname, csd.unit_count dcount, csd.location_id dlocation, dev.part_number, dev.description ddescr, csvc.contract_service_subscription_id, css.subscription_type, csvc.microsoft_365_subscription_config_id "
                + " from contract_service csvc"
                + " left join service service on service.id = csvc.service_id"
                + " left outer join contract_lineitem clit on clit.contract_service_id = csvc.id"
                + " left outer join contract_service_device csd on csd.contract_service_id = csvc.id"
                + " left outer join device dev on dev.id = csd.device_id"
                + " left outer join contract_service_subscription css on css.id = csvc.contract_service_subscription_id"
                + " where csd.name like :name";
        if (contractId != null) {
            query += " and csvc.contract_id = :contractId";
        }
        query += " group by csvc.id, csvc.service_id, csvc.parent_id, csvc.contract_id, csvc.contract_group_id, service.code, service.osp_id, service.name, service.version,"
                + " csvc.onetime_revenue, csvc.recurring_revenue, csvc.note, csvc.quantity, csvc.start_date, csvc.end_date,"
                + " dev.id, csd.name, csd.unit_count, dev.part_number, dev.description";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", "%" + name + "%");
        if (contractId != null) {
            params.put("contractId", contractId);
        }
        List<Service> contractServices = namedJdbcTemplate.query(query, params,
                new RowMapper<Service>() {
            @Override
            public Service mapRow(ResultSet rs, int i) throws SQLException {
                return new Service(
                        rs.getLong("id"),
                        rs.getString("code"),
                        rs.getLong("parent_id"),
                        rs.getLong("contract_id"),
                        rs.getLong("contract_group_id"),
                        null,
                        rs.getLong("service_id"),
                        rs.getString("osp_id"),
                        rs.getDouble("version"),
                        rs.getString("name"),
                        rs.getBigDecimal("onetime_revenue"),
                        rs.getBigDecimal("recurring_revenue"),
                        rs.getInt("quantity"),
                        rs.getString("note"),
                        rs.getInt("lineitems"),
                        rs.getDate("start_date"),
                        rs.getDate("end_date"),
                        rs.getLong("devid"),
                        rs.getString("dname"),
                        rs.getString("part_number"),
                        rs.getString("ddescr"),
                        rs.getInt("dcount"),
                        Service.Status.valueOf(rs.getString("status")),
                        (rs.getLong("contract_service_subscription_id") == 0 ? null : rs.getLong("contract_service_subscription_id")),
                        rs.getString("subscription_type"),
                        (rs.getLong("microsoft_365_subscription_config_id") == 0 ? null : rs.getLong("microsoft_365_subscription_config_id")),
                        (rs.getInt("dlocation") == 0 ? null : rs.getInt("dlocation")));
            }
        });
        for (Service cs : contractServices) {
            // attach the nested/child contract services to the parents
            List<Service> nestedContractServices = jdbcTemplate.query(ContractServiceRowMapper.QUERY + " where csvc.parent_id = ?" + ContractServiceRowMapper.POST_QUERY,
                    new Object[]{cs.getId()}, new ContractServiceRowMapper());
            cs.setRelatedLineItems(nestedContractServices);
        }
        return contractServices;
    }

    @Override
    public Service findContractServiceByNameAndContractId(String name, Long contractId) {
        String query = "select csvc.id, csvc.service_id, csvc.parent_id, csvc.contract_id, csvc.contract_group_id, service.code, service.osp_id, service.name, service.version,"
                + " csvc.onetime_revenue, csvc.recurring_revenue, csvc.note, csvc.quantity, count(clit.lineitem_id) lineitems,"
                + " csvc.start_date, csvc.end_date, csvc.status, dev.id devid, csd.name dname, csd.unit_count dcount, csd.location_id dlocation, dev.part_number, dev.description ddescr, csvc.contract_service_subscription_id, css.subscription_type, csvc.microsoft_365_subscription_config_id "
                + " from contract_service csvc"
                + " left join service service on service.id = csvc.service_id"
                + " left outer join contract_lineitem clit on clit.contract_service_id = csvc.id"
                + " left outer join contract_service_device csd on csd.contract_service_id = csvc.id"
                + " left outer join device dev on dev.id = csd.device_id"
                + " left outer join contract_service_subscription css on css.id = csvc.contract_service_subscription_id"
                + " where csd.name = :name";
        if (contractId != null) {
            query += " and csvc.contract_id = :contractId";
        }
        query += " group by csvc.id, csvc.service_id, csvc.parent_id, csvc.contract_id, csvc.contract_group_id, service.code, service.osp_id, service.name, service.version,"
                + " csvc.onetime_revenue, csvc.recurring_revenue, csvc.note, csvc.quantity, csvc.start_date, csvc.end_date,"
                + " dev.id, csd.name, csd.unit_count, dev.part_number, dev.description order by csvc.end_date desc";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", name);
        if (contractId != null) {
            params.put("contractId", contractId);
        }
        List<Service> contractServices = namedJdbcTemplate.query(query, params,
                new RowMapper<Service>() {
            @Override
            public Service mapRow(ResultSet rs, int i) throws SQLException {
                return new Service(
                        rs.getLong("id"),
                        rs.getString("code"),
                        rs.getLong("parent_id"),
                        rs.getLong("contract_id"),
                        rs.getLong("contract_group_id"),
                        null,
                        rs.getLong("service_id"),
                        rs.getString("osp_id"),
                        rs.getDouble("version"),
                        rs.getString("name"),
                        rs.getBigDecimal("onetime_revenue"),
                        rs.getBigDecimal("recurring_revenue"),
                        rs.getInt("quantity"),
                        rs.getString("note"),
                        rs.getInt("lineitems"),
                        rs.getDate("start_date"),
                        rs.getDate("end_date"),
                        rs.getLong("devid"),
                        rs.getString("dname"),
                        rs.getString("part_number"),
                        rs.getString("ddescr"),
                        rs.getInt("dcount"),
                        Service.Status.valueOf(rs.getString("status")),
                        (rs.getLong("contract_service_subscription_id") == 0 ? null : rs.getLong("contract_service_subscription_id")),
                        rs.getString("subscription_type"),
                        (rs.getLong("microsoft_365_subscription_config_id") == 0 ? null : rs.getLong("microsoft_365_subscription_config_id")),
                        (rs.getInt("dlocation") == 0 ? null : rs.getInt("dlocation")));
            }
        });

        if (contractServices == null || contractServices.isEmpty()) {
            return null;
        }
        for (Service cs : contractServices) {
            // attach the nested/child contract services to the parents
            List<Service> nestedContractServices = jdbcTemplate.query(ContractServiceRowMapper.QUERY + " where csvc.parent_id = ?" + ContractServiceRowMapper.POST_QUERY,
                    new Object[]{cs.getId()}, new ContractServiceRowMapper());
            cs.setRelatedLineItems(nestedContractServices);
        }
        
        //if there's more than one, we'll get the most recent one
        Service matchedContractService = contractServices.get(0);
        
        /*
        //Leaving this in in case we want more strict logic for sorting out which is the correct "current" CI in the future
        if(contractServices.size() > 1) {
        	for(Service contractService : contractServices) {
        		if(contractService.getEndDate().compareTo(matchedContractService.getEndDate()) > 0) {
        			matchedContractService = contractService;
        		}
        	}
        }*/
        
        return matchedContractService;
    }

    @SuppressWarnings("resource")
	@Override
    public void importContractServices(File excel) throws ServiceException {
        try {
            FileInputStream bis = new FileInputStream(excel);
            Workbook workbook;
            if (excel.getName().endsWith("xls")) {
                workbook = new HSSFWorkbook(bis);
            } else if (excel.getName().endsWith("xlsx")) {
                workbook = new XSSFWorkbook(bis);
            } else {
            	bis.close();
                throw new IllegalArgumentException("Received file does not have a standard excel extension.");
            }

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.rowIterator();
            int counter = 0;
            int parentCounter = 0;
            int chparentCounter = 0;
            boolean emptyRow = false;
            DataFormatter poiFmt = new DataFormatter();
            List<ContractServiceImportRecordHolder> toinsert = new ArrayList<ContractServiceImportRecordHolder>();
            boolean childFlag = false;
            boolean gchildFlag = false;
            ContractServiceImportRecordHolder parent = null;
            ContractServiceImportRecordHolder chparent = null;
            ContractServiceImportRecordHolder lastRecord = null;
            while (rowIterator.hasNext()) {
                ContractServiceImportRecordHolder rec = null;
                Row row = rowIterator.next();
                switch (row.getRowNum()) {
                    case 0:
                        counter++;
                        // header
                        break;
                    default:
                        emptyRow = true;
                        for (Cell c : row) {
                            if (c.getCellType() != Cell.CELL_TYPE_BLANK) {
                                emptyRow = false;
                            }
                        }
                        if (!emptyRow) {
                            // read contract service records
                            counter++;
                            rec = new ContractServiceImportRecordHolder();
                            Iterator<Cell> rowColumns = row.cellIterator();
                            while (rowColumns.hasNext()) {
                                Cell rowCell = rowColumns.next();
                                switch (rowCell.getColumnIndex()) {
                                    case 0:
                                        if ("Y".equalsIgnoreCase(rowCell.getStringCellValue())) {
                                            childFlag = true;
                                            gchildFlag = false;
                                        } else if ("YY".equalsIgnoreCase(rowCell.getStringCellValue())) {
                                            gchildFlag = true;
                                            childFlag = false;
                                        } else {
                                            childFlag = false;
                                            gchildFlag = false;
                                        }
                                        break;
                                    case 1:
                                        rec.setJobNumber(rowCell.getStringCellValue());
                                        break;
                                    case 2:
                                        rec.setStartDate(rowCell.getDateCellValue());
                                        break;
                                    case 3:
                                        rec.setEndDate(rowCell.getDateCellValue());
                                        break;
                                    case 4:
                                        rec.setCustomerName(rowCell.getStringCellValue());
                                        break;
                                    case 5:
                                        rec.setContractGroupName(rowCell.getStringCellValue());
                                        break;
                                    case 6:
                                        rec.setCIName(rowCell.getStringCellValue());
                                        break;
                                    case 7:
                                        rec.setServiceName(rowCell.getStringCellValue());
                                        break;
                                    case 8:
                                        rec.setPartDescription(rowCell.getStringCellValue());
                                        break;
                                    case 9:
                                        rec.setPartNumber(rowCell.getStringCellValue());
                                        break;
                                    case 10:
                                        rowCell.setCellType(Cell.CELL_TYPE_STRING); // force String input for expected integer value
                                        if (StringUtils.isNotBlank(rowCell.getStringCellValue())) {
                                            rec.setUnitCount(Integer.parseInt(rowCell.getStringCellValue()));
                                        }
                                        break;
                                    case 11:
                                        rec.setOnetime(new BigDecimal(rowCell.getNumericCellValue()));
                                        break;
                                    case 12:
                                        rec.setRecurring(new BigDecimal(rowCell.getNumericCellValue()));
                                        break;
                                    case 13:
                                        String status = rowCell.getStringCellValue();
                                        if (StringUtils.isNotBlank(status)) {
                                            status = status.toLowerCase();
                                            if ("pending".equals(status)) {
                                                rec.setStatus(Service.Status.valueOf(status));
                                            } else {
                                                rec.setStatus(Service.Status.active);
                                            }
                                        } else {
                                            rec.setStatus(Service.Status.active);
                                        }
                                        break;
                                    case 14:
                                        rec.setNote(rowCell.getStringCellValue());
                                        break;
                                    default:
                                    // foo
                                }
                            }
                            if (counter == 1 && (childFlag || gchildFlag)) {
                                throw new ServiceException(messageSource.getMessage("import_validation_error_child_first_record",
                                        new Object[]{counter}, LocaleContextHolder.getLocale()));
                            } else if (gchildFlag && parent == null) {
                                throw new ServiceException(messageSource.getMessage("import_validation_error_gchild_before_child",
                                        new Object[]{counter}, LocaleContextHolder.getLocale()));
                            } else if (gchildFlag && chparent == null) {
                                chparent = lastRecord;
                                chparentCounter = counter - 1;
                            } else if (childFlag && parent == null) {
                                parent = lastRecord;
                                parentCounter = counter - 1;
                            } else if (!childFlag && !gchildFlag) {
                                if (parent != null && parent.getDevice() != null) {
                                    validateChildDevicesOnImport(parent, parentCounter);
                                }
                                // no more children, unset parent...
                                parent = null;
                                chparent = null;
                            } else if (!gchildFlag) {
                                if (chparent != null && chparent.getDevice() != null) {
                                    validateChildDevicesOnImport(chparent, chparentCounter);
                                }
                                // no more gchildren, unset chparent...
                                chparent = null;
                                if (childFlag && parent != null) { // did the lastRecord (child) require gchildren?
                                    validateChildDevicesOnImport(lastRecord, counter - 1);
                                }
                            }
                            /**
                            log.debug("counter: [{}], part: [{}], childFlag: [{}], parent part: [{}]",
                                    new Object[]{counter, rec.getPartNumber(), childFlag, (parent == null ? "null" : parent.getPartNumber())});
                            */
                            rec.validate((chparent != null ? chparent : parent), counter, LocaleContextHolder.getLocale());
                            Customer customer = null;
                            if (parent != null) {
                                customer = parent.getCustomer();
                            } else {
                                List<Customer> customers = findCustomerByName(rec.getCustomerName());
                                if (customers == null || customers.size() < 1) {
                                    throw new ServiceException(messageSource.getMessage("import_customer_not_found_for_name",
                                            new Object[]{rec.getCustomerName(), counter}, LocaleContextHolder.getLocale()));
                                } else if (customers.size() > 1) {
                                    throw new ServiceException(messageSource.getMessage("import_duplicate_customer_found_for_name",
                                            new Object[]{rec.getCustomerName(), counter}, LocaleContextHolder.getLocale()));
                                } else {
                                    customer = customers.get(0);
                                }
                            }
                            rec.setCustomer(customer);
                            Contract contract;
                            if (parent != null) {
                                contract = parent.getContract();
                            } else {
                                contract = findContractByJobNumberAndCompanyId(rec.getJobNumber(), customer.getId());
                                if (contract == null) {
                                    throw new ServiceException(messageSource.getMessage("import_contract_not_found_for_job_number_customer",
                                            new Object[]{customer.getId(), rec.getJobNumber(), counter}, LocaleContextHolder.getLocale()));
                                }
                            }
                            rec.setContract(contract);

                            if (parent == null) {
                                //validate that contract service dates fall within contract dates
                                DateTime startDate = new DateTime(rec.getStartDate());
                                DateTime endDate = new DateTime(rec.getEndDate());
                                DateTime contractStartDate = new DateTime(contract.getStartDate());
                                DateTime contractEndDate = new DateTime(contract.getEndDate());
                                if (startDate.isBefore(contractStartDate) || endDate.isAfter(contractEndDate)) {
                                    throw new ServiceException(messageSource.getMessage("import_validation_error_service_dates_contract_dates", new Object[]{counter}, LocaleContextHolder.getLocale()));
                                }
                                
                                if (StringUtils.isNotBlank(rec.getContractGroupName())) {
                                    ContractGroup contractGroup = findContractGroupByNameAndContractId(rec.getContractGroupName(), contract.getId());
                                    if (contractGroup == null) {
                                        // let's be nice and make a new one...
                                        contractGroup = new ContractGroup();
                                        contractGroup.setName(rec.getContractGroupName());
                                        contractGroup.setContractId(contract.getId());
                                        contractGroup.setContractId(saveContractGroup(contractGroup));
                                    }
                                    rec.setContractGroup(contractGroup);
                                }
                                Service service = applicationDataDaoService.findActiveServiceByName(rec.getServiceName());
                                if (service == null) {
                                    throw new ServiceException(messageSource.getMessage("import_service_not_found_for_name",
                                            new Object[]{rec.getServiceName(), counter}, LocaleContextHolder.getLocale()));
                                } else {
                                    rec.setService(service);
                                }
                            } else {
                                rec.setJobNumber(parent.getJobNumber());
                                rec.setStartDate(parent.getStartDate());
                                rec.setEndDate(parent.getEndDate());
                                rec.setContractGroup(parent.getContractGroup());
                                if (chparent != null) {
                                    rec.setService(chparent.getService());
                                } else {
                                    rec.setService(parent.getService());
                                }
                            }
                            Device device = null;
                            if (StringUtils.isNotBlank(rec.getPartNumber())
                                    && StringUtils.isNotBlank(rec.getPartDescription())) {
                                device = applicationDataDaoService.findDeviceByNameAndPartNumber(rec.getPartNumber(), rec.getPartDescription());
                                if (device == null && StringUtils.isNotBlank(rec.getPartNumber())) {
                                    // assumption here is that the description is in error...
                                    device = applicationDataDaoService.findDeviceByPartNumber(rec.getPartNumber());
                                }

                                if (device == null) {
                                    throw new ServiceException(messageSource.getMessage("import_validation_error_device_not_found", new Object[]{counter}, LocaleContextHolder.getLocale()));
                                }
                            } else if (StringUtils.isNotBlank(rec.getPartNumber())) {
                                device = applicationDataDaoService.findDeviceByPartNumber(rec.getPartNumber());
                                if (device == null) {
                                    throw new ServiceException(messageSource.getMessage("import_device_not_found_for_part_number",
                                            new Object[]{counter}, LocaleContextHolder.getLocale()));
                                }
                            }
                            
                            if(device != null) {
                            	Integer unitCount = rec.getUnitCount();
                            	if(device.getRequireUnitCount()) {
                            		if(unitCount == null || unitCount == 0) {
                            			throw new ServiceException(messageSource.getMessage("import_unit_count_required_for_part_number",
                                                new Object[]{counter}, LocaleContextHolder.getLocale()));
                            		}
                            	} else {
                            		if(unitCount != null) {
                            			throw new ServiceException(messageSource.getMessage("import_unit_count_not_allowed_for_part_number",
                                                new Object[]{counter}, LocaleContextHolder.getLocale()));
                            		}
                            	}
                                /**
                                 * this will override previously set Service
                                 * note: a child will either have this Service or the parent's Service
                                 */
                                if (device.getDefaultOspId() != null) {
                                    Service service = applicationDataDaoService.findActiveServiceByOspId(device.getDefaultOspId());
                                    if (service != null) {
                                        rec.setService(service);
                                    }
                                }
                            }
                            
                            rec.setDevice(device); // may be null
                            if (chparent != null) {
                                chparent.addRelatedLineItem(rec);
                            } else if (parent != null) {
                                parent.addRelatedLineItem(rec);
                            } else {
                                toinsert.add(rec);
                            }
                        } else {
                            log.debug("empty row...");
                        }
                }
                if (emptyRow) {
                    // check for missed gchildren AND children on last parent/chparent records
                    validateChildDevicesOnImport(lastRecord, counter);
                    if (parent != null) {
                        validateChildDevicesOnImport(parent, parentCounter);
                    }
                    if (chparent != null) {
                        validateChildDevicesOnImport(chparent, chparentCounter);
                    }
                    break; // get out of row loop
                }
                lastRecord = rec;
            }
            int idx = 1;
            for (ContractServiceImportRecordHolder rec : toinsert) {
                Service service = convertImportRecordToService(rec);
                try {
                    for (ContractServiceImportRecordHolder relatedRec : rec.getRelatedLineItems()) {
                        Service chservice = convertImportRecordToService(relatedRec);
                        service.addRelatedLineItem(chservice);
                        for (ContractServiceImportRecordHolder chRelatedRec : relatedRec.getRelatedLineItems()) {
                            chservice.addRelatedLineItem(convertImportRecordToService(chRelatedRec));
                        }
                    }
                    Long cid = saveContractService(service, true);
                } catch (ServiceException se) {
                    throw new ServiceException(messageSource.getMessage("import_contract_service_save_failed",
                            new Object[]{se.getMessage(), idx}, LocaleContextHolder.getLocale()));
                }
                idx++;
            }
        } catch (FileNotFoundException fnfe) {
            log.error("failed to import service definition", fnfe);
            throw new ServiceException("Error - file to import not found", fnfe);
        } catch (IOException ioe) {
            log.error("failed to import service definition", ioe);
            throw new ServiceException("IO Error - reading service definition", ioe);
        }
    }
    
    private void validateChildDevicesOnImport(ContractServiceImportRecordHolder parent, Integer counter) throws ServiceException {
        List<Device> relatedDevices = applicationDataDaoService.findRelatedDevices(parent.getDevice().getId());
        for (Device relatedDevice : relatedDevices) {
            if ("required".equals(relatedDevice.getRelationship().getDescription())) {
                boolean childImported = false;
                for (ContractServiceImportRecordHolder parentRecChild : parent.getRelatedLineItems()) {
                    if (parentRecChild.getDevice() != null && parentRecChild.getDevice().getId()
                            .equals(relatedDevice.getId())) {
                        childImported = true;
                    }
                }
                if (!childImported) {
                    throw new ServiceException(messageSource.getMessage("import_validation_error_missing_required_child",
                            new Object[]{counter, relatedDevice.getPartNumber()}, LocaleContextHolder.getLocale()));
                }
            }
        }
    }
    
    private Service convertImportRecordToService(ContractServiceImportRecordHolder rec) {
        Service service = new Service();
        service.setServiceId(rec.getService().getServiceId());
        service.setContractId(rec.getContract().getId());
        if (rec.getDevice() != null) {
            service.setDeviceId(rec.getDevice().getId());
            service.setDevicePartNumber(rec.getDevice().getPartNumber());
            service.setDeviceDescription(rec.getDevice().getDescription());
            service.setDeviceName(rec.getCIName());
            service.setDeviceUnitCount(rec.getUnitCount());
        }
        if (rec.getContractGroup() != null) {
            service.setContractGroupId(rec.getContractGroup().getId());
        }
        service.setStartDate(rec.getStartDate());
        service.setEndDate(rec.getEndDate());
        BigDecimal onetime = rec.getOnetime();
        if (onetime == null) {
            onetime = new BigDecimal(0);
        }
        service.setOnetimeRevenue(onetime);
        BigDecimal recurring = rec.getRecurring();
        if (recurring == null) {
            recurring = new BigDecimal(0);
        }
        service.setRecurringRevenue(recurring);
        service.setStatus(rec.getStatus());
        service.setNote(rec.getNote());
        return service;
    }

    class ContractServiceImportRecordHolder {

        private String jobNumber;
        private Contract contract;
        private String contractGroupName;
        private ContractGroup contractGroup;
        private Date startDate;
        private Date endDate;
        private String customerName;
        private Customer customer;
        private String ciName;
        private String serviceName;
        private Service service;
        private String partNumber;
        private String partDescription;
        private Integer unitCount;
        private Device device;
        private BigDecimal onetime;
        private BigDecimal recurring;
        private Service.Status status;
        private String note;
        private ContractServiceImportRecordHolder parent;
        private List<ContractServiceImportRecordHolder> relatedLineItems = new ArrayList<ContractServiceImportRecordHolder>();

        public String getJobNumber() {
            return jobNumber;
        }

        public void setJobNumber(String jobNumber) {
            this.jobNumber = jobNumber;
        }

        public Contract getContract() {
            return contract;
        }

        public void setContract(Contract contract) {
            this.contract = contract;
        }

        public String getContractGroupName() {
            return contractGroupName;
        }

        public void setContractGroupName(String contractGroupName) {
            this.contractGroupName = contractGroupName;
        }

        public ContractGroup getContractGroup() {
            return contractGroup;
        }

        public void setContractGroup(ContractGroup contractGroup) {
            this.contractGroup = contractGroup;
        }

        public Date getStartDate() {
            return startDate;
        }

        public void setStartDate(Date startDate) {
            this.startDate = startDate;
        }

        public Date getEndDate() {
            return endDate;
        }

        public void setEndDate(Date endDate) {
            this.endDate = endDate;
        }

        public String getCustomerName() {
            return customerName;
        }

        public void setCustomerName(String customerName) {
            this.customerName = customerName;
        }

        public Customer getCustomer() {
            return customer;
        }

        public void setCustomer(Customer customer) {
            this.customer = customer;
        }

        public String getCIName() {
            return ciName;
        }

        public void setCIName(String ciName) {
            this.ciName = ciName;
        }

        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        public Service getService() {
            return service;
        }

        public void setService(Service service) {
            this.service = service;
        }

        public String getPartNumber() {
            return partNumber;
        }

        public void setPartNumber(String partNumber) {
            this.partNumber = partNumber;
        }

        public String getPartDescription() {
            return partDescription;
        }

        public void setPartDescription(String partDescription) {
            this.partDescription = partDescription;
        }

        public Integer getUnitCount() {
            return unitCount;
        }

        public void setUnitCount(Integer unitCount) {
            this.unitCount = unitCount;
        }

        public Device getDevice() {
            return device;
        }

        public void setDevice(Device device) {
            this.device = device;
        }

        public BigDecimal getOnetime() {
            return onetime;
        }

        public void setOnetime(BigDecimal onetime) {
            this.onetime = onetime;
        }

        public BigDecimal getRecurring() {
            return recurring;
        }

        public void setRecurring(BigDecimal recurring) {
            this.recurring = recurring;
        }

        public Service.Status getStatus() {
            return status;
        }

        public void setStatus(Service.Status status) {
            this.status = status;
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }

        public List<ContractServiceImportRecordHolder> getRelatedLineItems() {
            return relatedLineItems;
        }

        public void setRelatedLineItems(List<ContractServiceImportRecordHolder> relatedLineItems) {
            this.relatedLineItems = relatedLineItems;
        }
        
        public void addRelatedLineItem(ContractServiceImportRecordHolder lineItem) {
            if (this.relatedLineItems == null) {
                this.relatedLineItems = new ArrayList<ContractServiceImportRecordHolder>();
            }
            this.relatedLineItems.add(lineItem);
        }

        public void validate(ContractServiceImportRecordHolder parent, Integer record, Locale locale) throws ServiceException {
            if (parent == null) {
                if (StringUtils.isBlank(getCustomerName())) {
                    throw new ServiceException(messageSource.getMessage("import_validation_error_customer_name", new Object[]{record}, locale));
                } else if (getCustomerName().length() > 255) {
                    throw new ServiceException(messageSource.getMessage("import_validation_error_customer_name_length", new Object[]{record}, locale));
                }
                if (StringUtils.isBlank(jobNumber)) {
                    throw new ServiceException(messageSource.getMessage("import_validation_error_job_number", new Object[]{record}, locale));
                } else if (jobNumber.length() > 20) {
                    throw new ServiceException(messageSource.getMessage("import_validation_error_job_number_length", new Object[]{record}, locale));
                }
                if (StringUtils.isBlank(this.getServiceName())) {
                    throw new ServiceException(messageSource.getMessage("import_validation_error_service_name", new Object[]{record}, locale));
                } else if (getServiceName().length() > 255) {
                    throw new ServiceException(messageSource.getMessage("import_validation_error_service_name_length", new Object[]{record}, locale));
                }
                if (startDate == null) {
                    throw new ServiceException(messageSource.getMessage("import_validation_error_startdate", new Object[]{record}, locale));
                }
                if (endDate == null) {
                    throw new ServiceException(messageSource.getMessage("import_validation_error_enddate", new Object[]{record}, locale));
                }
            } else {
                if (StringUtils.isBlank(parent.getPartNumber())) {
                    throw new ServiceException(messageSource.getMessage("import_validation_error_parent_part_number_required", new Object[]{record}, locale));
                }
                if (StringUtils.isBlank(partNumber)) {
                    throw new ServiceException(messageSource.getMessage("import_validation_error_child_part_number_required", new Object[]{record}, locale));
                }
                log.debug("Validating Device parent [{}] and Device Child [{}]", new Object[]{(parent == null ? "empty" : parent.getPartNumber()), getPartNumber()});
                Integer related = jdbcTemplate.queryForObject("select count(*) from device dp"
                        + " inner join device_relationship dr on dp.id = dr.device_id"
                        + " inner join device dc on dr.related_device_id = dc.id"
                        + " where dp.part_number = ? and dc.part_number = ?", Integer.class,
                        new Object[]{parent.getPartNumber(), partNumber});
                if (related != 1) {
                    throw new ServiceException(messageSource.getMessage("import_validation_error_related_device_not_related", new Object[]{record}, locale));
                }
            }
            if (ciName != null && ciName.length() > 255) {
                throw new ServiceException(messageSource.getMessage("import_validation_error_ci_name_length", new Object[]{record}, locale));
            }
            if (partNumber != null && partNumber.length() > 50) {
                throw new ServiceException(messageSource.getMessage("import_validation_error_part_number_length", new Object[]{record}, locale));
            }
            if (partDescription != null && partDescription.length() > 255) {
                throw new ServiceException(messageSource.getMessage("import_validation_error_part_description_length", new Object[]{record}, locale));
            }
            if (note != null && note.length() > 500) {
                throw new ServiceException(messageSource.getMessage("import_validation_error_note_length", new Object[]{record}, locale));
            }
        }

        @Override
        public String toString() {
            return "ContractServiceImportRecordHolder{" + "jobNumber=" + jobNumber + ", contract=" + contract + ", startDate=" + startDate + ", endDate=" + endDate + ", customerName=" + customerName + ", customer=" + customer + ", ciName=" + ciName + ", serviceName=" + serviceName + ", service=" + service + ", partNumber=" + partNumber + ", partDescription=" + partDescription + ", device=" + device + ", onetime=" + onetime + ", recurring=" + recurring + ", note=" + note + '}';
        }
    }
    
    private static final String BASE_API_CONTRACT_SERVICES_QUERY = "select csvc.id, service.osp_id, csvc.contract_id, service.name servicename, cust.name, cust.sn_sys_id custsnsysid, "
            + " csvc.onetime_revenue, csvc.recurring_revenue, csvc.quantity, csvc.parent_id, snci.sn_sys_id snsysid, snci.contract_sn_sys_id contractsnsysid,"
            + " csvc.start_date, csvc.end_date, dev.id devid, csd.name dname, csd.unit_count dunit_count, dev.part_number, dev.description ddescr, dev.device_type, psp.removal_revenue"
            + " from contract_service csvc"
            + " inner join service service on service.id = csvc.service_id"
            + " left outer join service_now_ci snci on snci.contract_service_id = csvc.id"
            + " inner join contract_service_device csd on csd.contract_service_id = csvc.id"
            + " inner join device dev on dev.id = csd.device_id"
            + " inner join contract co on co.id = csvc.contract_id"
            + " inner join customer cust on cust.id = co.customer_id"
            + " inner join pricing_sheet ps on ps.contract_id = co.id"
            + " left outer join pricing_sheet_product psp on psp.pricing_sheet_id = ps.id and psp.device_id = csd.device_id";
        
    @Override
    public List<APIContractService> apiActiveContractServicesForContract(Long contractId) throws ServiceException {
        String query = BASE_API_CONTRACT_SERVICES_QUERY
                + " where co.id = :contractId and csvc.end_date > :endDate and csvc.hidden = false";
        
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("contractId", contractId);
        params.put("endDate", new Date());
        
        List<APIContractService> contractServices = namedJdbcTemplate.query(query, params, new RowMapper<APIContractService>() {
            @Override
            public APIContractService mapRow(ResultSet rs, int i) throws SQLException {
                return new APIContractService(
                        rs.getLong("id"),
                        rs.getString("custsnsysid"),
                        rs.getString("contractsnsysid"),
                        rs.getLong("contract_id"),
                        rs.getString("snsysid"),
                        rs.getString("dname"),
                        rs.getBigDecimal("onetime_revenue"),
                        rs.getBigDecimal("recurring_revenue"),
                        (rs.getBigDecimal("removal_revenue") == null) ? new BigDecimal(0) : rs.getBigDecimal("removal_revenue"),
                        rs.getLong("devid"),
                        rs.getString("part_number"),
                        rs.getString("ddescr"),
                        (rs.getString("device_type") == null) ? null : Device.DeviceType.valueOf(rs.getString("device_type")),
                        rs.getLong("osp_id"),
                        rs.getString("servicename"),
                        rs.getDate("start_date"),
                        rs.getDate("end_date"),
                        rs.getInt("dunit_count"),
                        (rs.getLong("parent_id") == 0) ? null : rs.getLong("parent_id"));
            }
        });
        
        
        query = BASE_API_CONTRACT_SERVICES_QUERY + " where csvc.parent_id = :parentId and csvc.end_date > :endDate and csvc.hidden = false";
        params = new HashMap<String, Object>();
        params.put("endDate", new Date());
        for(APIContractService contractService: contractServices) {
        	params.put("parentId", contractService.getId());
        	
        	List<APIContractService> childContractServices = namedJdbcTemplate.query(query, params, new RowMapper<APIContractService>() {
                @Override
                public APIContractService mapRow(ResultSet rs, int i) throws SQLException {
                    return new APIContractService(
                            rs.getLong("id"),
                            rs.getString("custsnsysid"),
                            rs.getString("contractsnsysid"),
                            rs.getLong("contract_id"),
                            rs.getString("snsysid"),
                            rs.getString("dname"),
                            rs.getBigDecimal("onetime_revenue"),
                            rs.getBigDecimal("recurring_revenue"),
                            (rs.getBigDecimal("removal_revenue") == null) ? new BigDecimal(0) : rs.getBigDecimal("removal_revenue"),
                            rs.getLong("devid"),
                            rs.getString("part_number"),
                            rs.getString("ddescr"),
                            (rs.getString("device_type") == null) ? null : Device.DeviceType.valueOf(rs.getString("device_type")),
                            rs.getLong("osp_id"),
                            rs.getString("servicename"),
                            rs.getDate("start_date"),
                            rs.getDate("end_date"),
                            rs.getInt("dunit_count"),
                            (rs.getLong("parent_id") == 0) ? null : rs.getLong("parent_id"));
                }
            });
        	
        	//grand child
        	for(APIContractService childContractService: childContractServices) {
            	params.put("parentId", childContractService.getId());
            	
            	List<APIContractService> grandChildContractServices = namedJdbcTemplate.query(query, params, new RowMapper<APIContractService>() {
                    @Override
                    public APIContractService mapRow(ResultSet rs, int i) throws SQLException {
                        return new APIContractService(
                                rs.getLong("id"),
                                rs.getString("custsnsysid"),
                                rs.getString("contractsnsysid"),
                                rs.getLong("contract_id"),
                                rs.getString("snsysid"),
                                rs.getString("dname"),
                                rs.getBigDecimal("onetime_revenue"),
                                rs.getBigDecimal("recurring_revenue"),
                                (rs.getBigDecimal("removal_revenue") == null) ? new BigDecimal(0) : rs.getBigDecimal("removal_revenue"),
                                rs.getLong("devid"),
                                rs.getString("part_number"),
                                rs.getString("ddescr"),
                                (rs.getString("device_type") == null) ? null : Device.DeviceType.valueOf(rs.getString("device_type")),
                                rs.getLong("osp_id"),
                                rs.getString("servicename"),
                                rs.getDate("start_date"),
                                rs.getDate("end_date"),
                                rs.getInt("dunit_count"),
                                (rs.getLong("parent_id") == 0) ? null : rs.getLong("parent_id"));
                    }
                });
            	
            	childContractService.setRelatedLineItems(grandChildContractServices);
        	}
        	
        	contractService.setRelatedLineItems(childContractServices);
        }
        
        return contractServices;
    }
    
    @Override
    public APIPCRUpdateResponse apiPCRUpdate(APIPCRUpdateRequest pcrUpdate) {
    	APIPCRUpdateResponse response = new APIPCRUpdateResponse();
    	
    	try {
    		ContractUpdate pcr = addOrGetContractUpdateforPCRUpdate(pcrUpdate);
    		response.setPcrStatus(APIPCRUpdateResponse.Result.success);
    		response.setPcrId(pcr.getId());
    		
    		if(pcrUpdate.getPcrType() != null && APIPCRUpdateRequest.Type.textPCR.equals(pcrUpdate.getPcrType())) {
    			//check what type it is
    			APIPCRUpdateRequest.SubType subType = pcrUpdate.getPcrSubType();
    			
    			if(subType != null) {
    				if(APIPCRUpdateRequest.SubType.extension.equals(subType)) {
    					extendContract(pcrUpdate.getContractId(), pcrUpdate.getNewContractEndDate());
    				}
    			}
    		} else {
    			List<BatchResult> batchResults = new ArrayList<BatchResult>();
    			if(!pcrUpdate.getContractServices().isEmpty()) {
            		APIContractService[] contractServices = pcrUpdate.getContractServices().toArray(new APIContractService[pcrUpdate.getContractServices().size()]);
            		List<BatchResult> contractServiceBatchResults = batchAPIContractServices(contractServices, pcr.getId());
                	batchResults.addAll(contractServiceBatchResults);
            	}
    			
    			if(!pcrUpdate.getPricingSheetProducts().isEmpty()) {
            		APIPricingSheetProduct[] pricingSheetProducts = pcrUpdate.getPricingSheetProducts().toArray(new APIPricingSheetProduct[pcrUpdate.getPricingSheetProducts().size()]);
                	List<BatchResult> pricingSheetBatchResults = batchAPIPricingSheetProducts(pricingSheetProducts, pcr.getId());
                	batchResults.addAll(pricingSheetBatchResults);
            	}
    			response.setBatchResults(batchResults);
    		}
    	} catch (Exception e) {
    		e.printStackTrace();
    		response.setPcrStatus(APIPCRUpdateResponse.Result.failed);
    		response.setPcrMessage(e.getMessage());
    	}
    	
    	return response;
    }
    
    private void extendContract(Long contractId, Date newContractEndDate) throws ServiceException {
    	Contract contract = contract(contractId);
    	
    	if(newContractEndDate != null && newContractEndDate.after(contract.getEndDate())) {
    		contract.setEndDate(newContractEndDate);
    		updateContract(contract);
    	}
    	
    }
    
    @Override
    public List<BatchResult> batchAPIContractServices(APIContractService[] apiContractServices, Long contractUpdateId) {

        List<BatchResult> results = new ArrayList<BatchResult>();
        List<APIContractService> removedContractServices = new ArrayList<APIContractService>();
        log.info("Processing Contract Services...");
        for (APIContractService apiContractService : apiContractServices) {
        	log.info("Looping...");
        	log.info("CS: " + apiContractService);
            if (apiContractService.getOperation() != null) {
                BatchResult.Operation op = null;
                try {
                    op = BatchResult.Operation.valueOf(apiContractService.getOperation());
                } catch (IllegalArgumentException iae) {
                    results.add(new BatchResult(apiContractService.getId(), null, apiContractService.getCorrelationId(), messageSource.getMessage("batch_error_bad_operation", new Object[]{apiContractService.getCiSNSysId()}, LocaleContextHolder.getLocale()), null, BatchResult.Result.failed));
                    continue;
                }

                if (op.equals(BatchResult.Operation.delete)) {
                	//we don't support delete, so just send back an error
                	BatchResult result = new BatchResult(apiContractService.getId(), null, apiContractService.getCorrelationId(), messageSource.getMessage("batch_error_delete_osm_contract_service_not_available", new Object[]{apiContractService.getCiSNSysId()}, LocaleContextHolder.getLocale()), op, BatchResult.Result.failed);
                    results.add(result);
                    break;  
                } else if (op.equals(BatchResult.Operation.create)) {
                	log.info("Creating...");
                	try {
                		//validate fields
                		Date serviceStartDate = apiContractService.getStartDate();
                		if(serviceStartDate == null) {
                			results.add(new BatchResult(apiContractService.getId(), null, apiContractService.getCorrelationId(), messageSource.getMessage("validation_error_osm_start_date_required", null, LocaleContextHolder.getLocale()), op, BatchResult.Result.failed));
                    		continue;
                		}
                		
                		Long contractId = apiContractService.getContractId();
                		if(contractId == null) {
                			results.add(new BatchResult(apiContractService.getId(), null, apiContractService.getCorrelationId(), messageSource.getMessage("validation_error_osm_contract_id_required", null, LocaleContextHolder.getLocale()), op, BatchResult.Result.failed));
                    		continue;
                		}
                		
                		//add or get pcr
                		//ContractUpdate pcr = addOrGetContractUpdateforAPIContractService(apiContractService, contractId);
                        ContractUpdate pcr = contractUpdate(contractUpdateId);
                        log.info("PCR: " + pcr);
                        if(pcr == null) {
                        	results.add(new BatchResult(apiContractService.getId(), null, apiContractService.getCorrelationId(), messageSource.getMessage("contract_update_not_found_for_id", new Object[] {contractUpdateId}, LocaleContextHolder.getLocale()), op, BatchResult.Result.failed));
                    		continue;
                        }
                        //service.setContractUpdateId(pcr.getId());
                		
                		//validate
                    	Contract contract = contract(contractId);
                    	log.info("Contract: " + contract);
                    	if(contract == null) {
                    		results.add(new BatchResult(apiContractService.getId(), pcr.getId(), apiContractService.getCorrelationId(), messageSource.getMessage("batch_error_update_osm_service_contract_not_found", new Object[]{apiContractService.getCiSNSysId()}, LocaleContextHolder.getLocale()), op, BatchResult.Result.failed));
                    		continue;
                    	}
                    	
                     	Date contractStartDate = contract.getStartDate();
                     	Date contractEndDate = contract.getEndDate();
                     	if(!(serviceStartDate.equals(contractStartDate) || serviceStartDate.after(contractStartDate)) || !(serviceStartDate.equals(contractEndDate) || serviceStartDate.before(contractEndDate))) {
                     		results.add(new BatchResult(apiContractService.getId(), pcr.getId(), apiContractService.getCorrelationId(), messageSource.getMessage("validation_error_startdate_not_in_contract_date", null, LocaleContextHolder.getLocale()), op, BatchResult.Result.failed));
                    		continue;
                     	}
                     	log.info("Dates valid");
                    	
                    	//we set the newly added CI end date to the contract end date automatically
                     	Service service = mapAPIContractServiceToCreateService(apiContractService, contractId, serviceStartDate, contractEndDate);
                     	service.setContractUpdateId(pcr.getId());
                     	
                     	Long parentId = apiContractService.getParentId();
                     	if(parentId != null && parentId > 0) {
                     		service.setParentId(parentId);
                     	}
                     	log.info("Service Mapped: " + service);
                		//make sure the month hasn't been billed already
                    	List<ContractInvoice> contractInvoices = contractInvoicesForContract(contractId);
                    	boolean invoiceConflictExists = contractInvoiceConflictExistsForContractService(contractInvoices, service, op);
                    	log.info("Invoice Conflicts: " + invoiceConflictExists);
                        if (!invoiceConflictExists) {
                        	try {
                        		log.info("About to save");
                        		Long contractServiceId = saveContractService(service, false);
                        		log.info("saved");
                        		results.add(new BatchResult(contractServiceId, pcr.getId(), apiContractService.getCorrelationId(), messageSource.getMessage("api_ok_batch_response_single", new Object[]{op.toString(), service.getId()}, LocaleContextHolder.getLocale()), op, BatchResult.Result.success));
                        		continue;
                        	} catch (Exception e) {
                        		e.printStackTrace();
                        		results.add(new BatchResult(apiContractService.getId(), pcr.getId(), apiContractService.getCorrelationId(), messageSource.getMessage("batch_error_save_exception", new Object[]{op.toString(), apiContractService.getId()}, LocaleContextHolder.getLocale()), op, BatchResult.Result.failed));
                        		continue;
                        	}
                        } else {
                            BatchResult result = new BatchResult(apiContractService.getId(), pcr.getId(), apiContractService.getCorrelationId(), messageSource.getMessage("batch_error_create_service_contract_invoice_conflict", null, LocaleContextHolder.getLocale()), op, BatchResult.Result.failed);
                            results.add(result);
                            continue;
                        }
                    } catch (ServiceException se) {
                        results.add(new BatchResult(apiContractService.getId(), null, apiContractService.getCorrelationId(), se.getMessage(), op, BatchResult.Result.failed));
                        se.printStackTrace();
                    } catch (Exception other) {
                        BatchResult result = new BatchResult(apiContractService.getId(), null, apiContractService.getCorrelationId(), messageSource.getMessage("batch_error_save_exception", new Object[]{apiContractService.getCiSNSysId(), null}, LocaleContextHolder.getLocale()), op, BatchResult.Result.failed);
                        result.newMeta().addException(other);
                        results.add(result);
                        other.printStackTrace();
                    }
                } else if (op.equals(BatchResult.Operation.update)) {
                	log.info("Updating...");
                		Service service = null;
                        try {
                        	//validate fields
                        	Date serviceEndDate = apiContractService.getEndDate();
                    		if(serviceEndDate == null) {
                    			results.add(new BatchResult(apiContractService.getId(), null, apiContractService.getCorrelationId(), messageSource.getMessage("validation_error_osm_end_date_required", null, LocaleContextHolder.getLocale()), op, BatchResult.Result.failed));
                        		continue;
                    		}
                        	
                        	service = contractService(apiContractService.getId());
                        	if(service == null) {
                        		results.add(new BatchResult(apiContractService.getId(), null, apiContractService.getCorrelationId(), messageSource.getMessage("batch_error_update_osm_service_not_found", new Object[]{apiContractService.getCiSNSysId()}, LocaleContextHolder.getLocale()), op, BatchResult.Result.failed));
                        		continue;
                        	}
                        	
                        	log.info("Service: " + service);
                        	/*
                        	if(service.getParentId() != null && service.getParentId().compareTo(0L) <= 0) {
                        		service.setParentId(null);
                        	}*/
                        	
                        	//validate that service belongs to the customer and contract
                        	Long contractId = service.getContractId();
                        	Contract contract = contract(contractId);
                        	if(contract == null) {
                        		results.add(new BatchResult(apiContractService.getId(), null, apiContractService.getCorrelationId(), messageSource.getMessage("batch_error_update_osm_service_contract_not_found", new Object[]{apiContractService.getCiSNSysId()}, LocaleContextHolder.getLocale()), op, BatchResult.Result.failed));
                        		continue;
                        	}
                        	log.info("Contract: " + contract);
                        	
                        	Date contractStartDate = contract.getStartDate();
                         	Date contractEndDate = contract.getEndDate();
                        	if(!(serviceEndDate.equals(contractStartDate) || serviceEndDate.after(contractStartDate)) || !(serviceEndDate.equals(contractEndDate) || serviceEndDate.before(contractEndDate))) {
                         		results.add(new BatchResult(apiContractService.getId(), null, apiContractService.getCorrelationId(), messageSource.getMessage("validation_error_enddate_not_in_contract_date", null, LocaleContextHolder.getLocale()), op, BatchResult.Result.failed));
                        		continue;
                         	}
                        	log.info("Dates Valid!");
                        	
                        	//set the new end date -- it's the only thing we're changing
                        	/*
                        	service.setEndDate(apiContractService.getEndDate());
                        	String note = service.getNote();
                        	service.setNote(note + " This record was automatically updated through Service Activation on: " + new Date().toString() + ". The end date was changed to: " + apiContractService.getEndDate());
                        	*/
                        	
                        	//we'll map the service and it's related line items now that we've validated the parent in the contract
                        	service = mapAPIContractServiceToUpdateService(apiContractService, contractId, serviceEndDate);
                        	log.info("Service Mapped: " + service);
                        	List<ContractInvoice> contractInvoices = contractInvoicesForContract(contractId);
                        	boolean invoiceConflictExists = contractInvoiceConflictExistsForContractService(contractInvoices, service, op);
                        	log.info("Invoice Conflicts: " + invoiceConflictExists);
                            if (!invoiceConflictExists) {
                            	//add or get pcr
                            	
                            	ContractUpdate pcr = contractUpdate(contractUpdateId);
                                if(pcr == null) {
                                	results.add(new BatchResult(apiContractService.getId(), null, apiContractService.getCorrelationId(), messageSource.getMessage("contract_update_not_found_for_id", new Object[] {contractUpdateId}, LocaleContextHolder.getLocale()), op, BatchResult.Result.failed));
                            		continue;
                                }
                                service.setContractUpdateId(pcr.getId());
                                log.info("PCR: " + pcr);
                        		//add a contract adjustment for removal fee if one exists
                        		//List<APIContractService> fullContractServices = apiContractServices(new String[]{apiContractService.getCiSNSysId()});
                        		/*
                        		List<APIContractService> fullContractServices = new ArrayList<APIContractService>();
                        		if(fullContractServices.size() > 0) {
                        			APIContractService fullContractService = fullContractServices.get(0);
                        			BigDecimal removalPrice = fullContractService.getRemovalPrice();
                        			if(removalPrice != null && removalPrice.doubleValue() > new BigDecimal(0).doubleValue()) {
                        				ContractAdjustment adjustment = new ContractAdjustment();
                        				adjustment.setContractId(service.getContractId());
                        		        adjustment.setContractUpdateId(pcr.getId());
                        		        adjustment.setAdjustment(removalPrice);
                        		        adjustment.setAdjustmentType(ContractAdjustment.AdjustmentType.onetime.toString());
                        		        adjustment.setNote("This is the removal fee for the CI: " + service.getDeviceName());
                        		        DateTime startDateTime = new DateTime(apiContractService.getEndDate());
                        		        startDateTime = startDateTime.dayOfMonth().withMinimumValue();
                        		        adjustment.setStartDate(startDateTime.toDate());
                        		        DateTime endDateTime = startDateTime.dayOfMonth().withMaximumValue();
                        		        adjustment.setEndDate(endDateTime.toDate());
                        		        adjustment.setCreated(new Date());
                        		        adjustment.setCreatedBy(authenticatedUser());
                        		        saveContractAdjustment(adjustment, false);
                        			}
                        		}*/
                            	
                                try {
                                	log.info("Updating Service... with Contract Group ID [" + service.getContractGroupId() + "]");
                                	updateContractService(service, false);
                                	log.info("Updated");
                                	results.add(new BatchResult(apiContractService.getId(), pcr.getId(), apiContractService.getCorrelationId(), messageSource.getMessage("api_ok_batch_response_single", new Object[]{op.toString(), apiContractService.getId()}, LocaleContextHolder.getLocale()), op, BatchResult.Result.success));
                                	removedContractServices.add(apiContractService);
                                	continue;
                                } catch (Exception e) {
                            		results.add(new BatchResult(apiContractService.getId(), pcr.getId(), apiContractService.getCorrelationId(), messageSource.getMessage("batch_error_update_exception", new Object[]{op.toString(), apiContractService.getId()}, LocaleContextHolder.getLocale()), op, BatchResult.Result.failed));
                            		continue;
                            	}
                            } else {
                                BatchResult result = new BatchResult(apiContractService.getId(), null, apiContractService.getCorrelationId(), messageSource.getMessage("batch_error_update_service_contract_invoice_conflict", null, LocaleContextHolder.getLocale()), op, BatchResult.Result.failed);
                                results.add(result);
                                continue;
                            }
                        } catch (ServiceException se) {
                        	se.printStackTrace();
                            results.add(new BatchResult(apiContractService.getId(), null, apiContractService.getCorrelationId(), se.getMessage(), op, BatchResult.Result.failed));
                        } catch (Exception other) {
                        	other.printStackTrace();
                            BatchResult result = new BatchResult(apiContractService.getId(), null, apiContractService.getCorrelationId(), messageSource.getMessage("batch_error_update_exception", new Object[]{apiContractService.getCiSNSysId()}, LocaleContextHolder.getLocale()), op, BatchResult.Result.failed);
                            result.newMeta().addException(other);
                            results.add(result);
                        }
                }
            } else {
                results.add(new BatchResult(apiContractService.getCiSNSysId(), messageSource.getMessage("batch_error_no_operation", new Object[]{apiContractService.getCiSNSysId()}, LocaleContextHolder.getLocale())));
            }
        }
        
        try {
        	BigDecimal removalPrice = new BigDecimal(0);
        	StringBuffer note = new StringBuffer("This is the removal fee for the following CIs: ");
        	Date endDate = null;
        	Long contractId = null;
        	
        	for(APIContractService removedContractService: removedContractServices) {
        		if(endDate == null) {
        			endDate = removedContractService.getEndDate();
        		}
        		if(contractId == null) {
        			contractId = removedContractService.getContractId();
        		}
        		BigDecimal lineItemRemovalPrice = removedContractService.getRemovalPrice();
        		if(lineItemRemovalPrice != null && lineItemRemovalPrice.doubleValue() > new BigDecimal(0).doubleValue()) {
	        		removalPrice = removalPrice.add(lineItemRemovalPrice);
	        		
	        		String ciName = removedContractService.getCiName();
	        		if(StringUtils.isBlank(ciName)) ciName = "No Name Assigned: ID[" + removedContractService.getId() + "]";
	        		note.append(ciName).append(" ($").append(lineItemRemovalPrice.setScale(2, BigDecimal.ROUND_HALF_UP)).append("), ");
        		}
        	}
        	
        	if(removalPrice.doubleValue() > new BigDecimal(0).doubleValue() && contractId != null && endDate != null) {
        		ContractAdjustment adjustment = new ContractAdjustment();
				adjustment.setContractId(contractId);
		        adjustment.setContractUpdateId(contractUpdateId);
		        adjustment.setAdjustment(removalPrice);
		        adjustment.setAdjustmentType(ContractAdjustment.AdjustmentType.onetime.toString());
		        adjustment.setNote(note.toString());
		        DateTime startDateTime = new DateTime(endDate);
		        startDateTime = startDateTime.dayOfMonth().withMinimumValue();
		        adjustment.setStartDate(startDateTime.toDate());
		        DateTime endDateTime = startDateTime.dayOfMonth().withMaximumValue();
		        adjustment.setEndDate(endDateTime.toDate());
		        adjustment.setStatus(Service.Status.active);
		        adjustment.setCreated(new Date());
		        adjustment.setCreatedBy(authenticatedUser());
		        saveContractAdjustment(adjustment, false);
        	}
        	
        } catch (Exception e) {
        	log.error("Error creating debit for removing line items.");
        	e.printStackTrace();
        }
        
        return results;
    }
    
    @Override
    public List<BatchResult> batchAPIPricingSheetProducts(APIPricingSheetProduct[] apiPricingSheetProducts, Long contractUpdateId) {

        List<BatchResult> results = new ArrayList<BatchResult>();
        List<APIContractService> removedContractServices = new ArrayList<APIContractService>();
        log.info("Processing Pricing Sheet Products...");
        for (APIPricingSheetProduct apiPricingSheetProduct : apiPricingSheetProducts) {
        	log.info("Looping...");
        	log.info("CS: " + apiPricingSheetProduct);
            if (apiPricingSheetProduct.getOperation() != null) {
                BatchResult.Operation op = null;
                try {
                    op = BatchResult.Operation.valueOf(apiPricingSheetProduct.getOperation());
                } catch (IllegalArgumentException iae) {
                    results.add(new BatchResult(apiPricingSheetProduct.getId(), null, apiPricingSheetProduct.getCorrelationId(), messageSource.getMessage("batch_error_bad_operation", new Object[]{apiPricingSheetProduct.getDeviceId()}, LocaleContextHolder.getLocale()), null, BatchResult.Result.failed));
                    continue;
                }

                if (op.equals(BatchResult.Operation.delete)) {
                	//we don't support delete, so just send back an error
                	BatchResult result = new BatchResult(apiPricingSheetProduct.getId(), null, apiPricingSheetProduct.getCorrelationId(), messageSource.getMessage("batch_error_delete_osm_contract_service_not_available", new Object[]{apiPricingSheetProduct.getDeviceId()}, LocaleContextHolder.getLocale()), op, BatchResult.Result.failed);
                    results.add(result);
                    break;  
                } else if (op.equals(BatchResult.Operation.create)) {
                	log.info("Creating...");
                	try {
                		Long contractId = apiPricingSheetProduct.getContractId();
                		if(contractId == null) {
                			results.add(new BatchResult(apiPricingSheetProduct.getId(), null, apiPricingSheetProduct.getCorrelationId(), messageSource.getMessage("validation_error_osm_contract_id_required", null, LocaleContextHolder.getLocale()), op, BatchResult.Result.failed));
                    		continue;
                		}
                		log.info("Contract: " + contractId);
                		
                		PricingSheet pricingSheet = pricingSheetService.findPricingSheetForContractWithActiveProducts(contractId);                				
                		if(pricingSheet == null) {
                			results.add(new BatchResult(apiPricingSheetProduct.getId(), null, apiPricingSheetProduct.getCorrelationId(), messageSource.getMessage("validation_error_osm_pricing_sheet_required", null, LocaleContextHolder.getLocale()), op, BatchResult.Result.failed));
                			continue;
                		}
                		log.info("Pricing Sheet: " + pricingSheet);
                		
                		Long deviceId = apiPricingSheetProduct.getDeviceId();
                		Device device = applicationDataDaoService.device(deviceId);
                		if(device == null) {
                			results.add(new BatchResult(apiPricingSheetProduct.getId(), null, apiPricingSheetProduct.getCorrelationId(), messageSource.getMessage("batch_error_update_osm_device_not_found", new Object[]{apiPricingSheetProduct.getDeviceId()}, LocaleContextHolder.getLocale()), op, BatchResult.Result.failed));
                			continue;
                		}
                		log.info("Device: " + device);
                		
                		PricingSheetProduct existingProduct = null;
                		for(PricingSheetProduct pricingSheetProduct: pricingSheet.getProducts()) {
                			if(pricingSheetProduct.getDeviceId().equals(deviceId)) {
                				existingProduct = pricingSheetProduct;
                				break;
                			}
                		}
                		log.info("existing product: " + existingProduct);
                		
                		PricingSheetProduct pricingSheetProduct = new PricingSheetProduct();
                		Integer unitCount = apiPricingSheetProduct.getUnitCount();
                		Integer newUnitCount = apiPricingSheetProduct.getUnitCount();
                		BigDecimal onetimePrice = apiPricingSheetProduct.getOnetimePrice();
                		BigDecimal recurringPrice = apiPricingSheetProduct.getRecurringPrice();
                		log.info("Unit Count: " + unitCount);
                		if(existingProduct != null) {
                			pricingSheetProduct = existingProduct;
                			Integer unitCountChange = 0;
                			if(apiPricingSheetProduct.getUnitCountChange() != null) unitCountChange = apiPricingSheetProduct.getUnitCountChange(); 
                			newUnitCount = unitCount + unitCountChange;
                		}
                		
                		pricingSheetProduct.setPricingSheetId(pricingSheet.getId());                		
                		pricingSheetProduct.setDeviceId(deviceId);
                		pricingSheetProduct.setServiceId(device.getDefaultOspId());
                		pricingSheetProduct.setOnetimePrice(onetimePrice);
                		pricingSheetProduct.setRecurringPrice(recurringPrice);
                		pricingSheetProduct.setRemovalPrice(apiPricingSheetProduct.getRemovalPrice());
                		pricingSheetProduct.setStatus(PricingSheetProduct.Status.active);
                		pricingSheetProduct.setUnitCount(newUnitCount);
                		
                		log.info("Product Pre Discount: " + pricingSheetProduct);
                		
                		BigDecimal discount = new BigDecimal(0);
                		if(Device.DeviceType.M365.equals(device.getDeviceType())) {
                			log.info("IS M365 Device");
                			//get the price list
                			MicrosoftPriceList priceList = microsoftPricingService.getLatestMicrosoftPriceList(MicrosoftPriceList.MicrosoftPriceListType.M365);
                			if(priceList == null) {
                				results.add(new BatchResult(apiPricingSheetProduct.getId(), null, apiPricingSheetProduct.getCorrelationId(), messageSource.getMessage("validation_error_osm_microsoft_price_list_not_found", null, LocaleContextHolder.getLocale()), op, BatchResult.Result.failed));
                				continue;
                			}
                			
                			if(StringUtils.isEmpty(device.getAltId())) {
                				results.add(new BatchResult(apiPricingSheetProduct.getId(), null, apiPricingSheetProduct.getCorrelationId(), messageSource.getMessage("validation_error_osm_microsoft_price_list_offer_id_not_found", new Object[] {device.getAltId()}, LocaleContextHolder.getLocale()), op, BatchResult.Result.failed));
                				log.info("No Offer ID for for Alt ID: " + device.getAltId());
                				continue;
                			}
                			MicrosoftPriceListM365Product priceListProduct = microsoftPricingService.getMicrosoftPriceListProductByOfferId(priceList.getId(), device.getAltId());
                			log.info("Price List Product is: " + priceListProduct);
                			
                			pricingSheetProduct.setErpPrice(priceListProduct.getErpPrice());
                			
                			discount = recurringPrice.divide(priceListProduct.getErpPrice(), 2, RoundingMode.HALF_UP).multiply(new BigDecimal(100));
                			discount = new BigDecimal(100).subtract(discount);
                			log.info("Discount is: " + discount);
                		}
                		pricingSheetProduct.setDiscount(discount);
                		
                		try {
                    		log.info("About to save: " + pricingSheetProduct);
                    		Long id = null;
                    		if(pricingSheetProduct.getId() == null) {
                    			id = pricingSheetService.savePricingSheetProduct(pricingSheetProduct);
                    		} else {
                    			pricingSheetService.updatePricingSheetProduct(pricingSheetProduct);
                    			id = pricingSheetProduct.getId();
                    		}
                    		results.add(new BatchResult(id, null, apiPricingSheetProduct.getCorrelationId(), messageSource.getMessage("api_ok_batch_response_single", new Object[]{op.toString(), apiPricingSheetProduct.getCorrelationId()}, LocaleContextHolder.getLocale()), op, BatchResult.Result.success));
                    		continue;
                    	} catch (Exception e) {
                    		e.printStackTrace();
                    		results.add(new BatchResult(pricingSheetProduct.getId(), null, apiPricingSheetProduct.getCorrelationId(), messageSource.getMessage("batch_error_save_exception", new Object[]{op.toString(), apiPricingSheetProduct.getCorrelationId()}, LocaleContextHolder.getLocale()), op, BatchResult.Result.failed));
                    		continue;
                    	}
                		
                    } catch (ServiceException se) {
                        results.add(new BatchResult(apiPricingSheetProduct.getId(), null, apiPricingSheetProduct.getCorrelationId(), se.getMessage(), op, BatchResult.Result.failed));
                        se.printStackTrace();
                    } catch (Exception other) {
                        BatchResult result = new BatchResult(apiPricingSheetProduct.getId(), null, apiPricingSheetProduct.getCorrelationId(), messageSource.getMessage("batch_error_save_exception", new Object[]{apiPricingSheetProduct.getDeviceId(), null}, LocaleContextHolder.getLocale()), op, BatchResult.Result.failed);
                        result.newMeta().addException(other);
                        results.add(result);
                        other.printStackTrace();
                    }
                } else if (op.equals(BatchResult.Operation.update)) {
                	log.info("Updating...");
                		Service service = null;
                        try {
                        	Long contractId = apiPricingSheetProduct.getContractId();
                    		if(contractId == null) {
                    			results.add(new BatchResult(apiPricingSheetProduct.getId(), null, apiPricingSheetProduct.getCorrelationId(), messageSource.getMessage("validation_error_osm_contract_id_required", null, LocaleContextHolder.getLocale()), op, BatchResult.Result.failed));
                        		continue;
                    		}
                    		log.info("Contract Id: " + contractId);
                    		
                    		PricingSheet pricingSheet = pricingSheetService.findPricingSheetForContractWithActiveProducts(contractId);                				
                    		if(pricingSheet == null) {
                    			results.add(new BatchResult(apiPricingSheetProduct.getId(), null, apiPricingSheetProduct.getCorrelationId(), messageSource.getMessage("validation_error_osm_pricing_sheet_required", null, LocaleContextHolder.getLocale()), op, BatchResult.Result.failed));
                    			continue;
                    		}
                    		log.info("Pricing Sheet: " + pricingSheet);
                    		
                    		PricingSheetProduct pricingSheetProduct = pricingSheetService.pricingSheetProduct(apiPricingSheetProduct.getId());
                    		log.info("Pricing Sheet Product: " + pricingSheetProduct);
                    		
                    		if(pricingSheetProduct == null) {
                    			results.add(new BatchResult(apiPricingSheetProduct.getId(), null, apiPricingSheetProduct.getCorrelationId(), messageSource.getMessage("batch_error_update_osm_pricing_sheet_product_not_found", new Object[] {apiPricingSheetProduct.getId()}, LocaleContextHolder.getLocale()), op, BatchResult.Result.failed));
                    			continue;
                    		}
                    		
                    		Long deviceId = pricingSheetProduct.getDeviceId();
                    		Device device = applicationDataDaoService.device(deviceId);
                    		log.info("Device: " + device);
                    		if(device == null) {
                    			results.add(new BatchResult(apiPricingSheetProduct.getId(), null, apiPricingSheetProduct.getCorrelationId(), messageSource.getMessage("batch_error_update_osm_device_not_found", new Object[]{apiPricingSheetProduct.getDeviceId()}, LocaleContextHolder.getLocale()), op, BatchResult.Result.failed));
                    			continue;
                    		}
                    		
                    		BigDecimal onetimePrice = apiPricingSheetProduct.getOnetimePrice();
                    		BigDecimal recurringPrice = apiPricingSheetProduct.getRecurringPrice();
                    		Integer unitCount = apiPricingSheetProduct.getUnitCount();
                    		Integer newUnitCount = apiPricingSheetProduct.getUnitCount();
                    		log.info("Unit Count: " + unitCount);
                    		if(APIPricingSheetProduct.ChangeType.increase.equals(apiPricingSheetProduct.getChangeType())) {
                    			newUnitCount = unitCount + apiPricingSheetProduct.getUnitCountChange();
                    		} else if(APIPricingSheetProduct.ChangeType.decrease.equals(apiPricingSheetProduct.getChangeType())) {
                    			newUnitCount = unitCount - apiPricingSheetProduct.getUnitCountChange();
                    		} else {
                    			//it's a removal
                    			newUnitCount = unitCount - apiPricingSheetProduct.getUnitCountChange();
                    		}
                    		
                    		pricingSheetProduct.setOnetimePrice(onetimePrice);
                    		pricingSheetProduct.setRecurringPrice(recurringPrice);
                    		pricingSheetProduct.setRemovalPrice(apiPricingSheetProduct.getRemovalPrice());
                    		pricingSheetProduct.setUnitCount(newUnitCount);
                    		
                    		BigDecimal discount = new BigDecimal(0);
                    		if(Device.DeviceType.M365.equals(device.getDeviceType())) {
                    			//get the price list
                    			MicrosoftPriceList priceList = microsoftPricingService.getLatestMicrosoftPriceList(MicrosoftPriceList.MicrosoftPriceListType.M365);
                    			if(priceList == null) {
                    				results.add(new BatchResult(apiPricingSheetProduct.getId(), null, apiPricingSheetProduct.getCorrelationId(), messageSource.getMessage("validation_error_osm_microsoft_price_list_not_found", null, LocaleContextHolder.getLocale()), op, BatchResult.Result.failed));
                    				continue;
                    			}
                    			
                    			if(StringUtils.isEmpty(device.getAltId())) {
                    				results.add(new BatchResult(apiPricingSheetProduct.getId(), null, apiPricingSheetProduct.getCorrelationId(), messageSource.getMessage("validation_error_osm_microsoft_price_list_offer_id_not_found", new Object[] {device.getAltId()}, LocaleContextHolder.getLocale()), op, BatchResult.Result.failed));
                    				log.info("No Offer ID for for Alt ID: " + device.getAltId());
                    				continue;
                    			}
                    			MicrosoftPriceListM365Product priceListProduct = microsoftPricingService.getMicrosoftPriceListProductByOfferId(priceList.getId(), device.getAltId());
                    			
                    			discount = recurringPrice.divide(priceListProduct.getErpPrice(), 2, RoundingMode.HALF_UP).multiply(new BigDecimal(100));
                    			discount = new BigDecimal(100).subtract(discount);
                    		}
                    		
                    		log.info("Discount: " + discount);
                    		pricingSheetProduct.setDiscount(discount);
                        	
                            try {
                            	log.info("Updating: " + pricingSheetProduct);
                            	pricingSheetService.updatePricingSheetProduct(pricingSheetProduct);
                            	results.add(new BatchResult(pricingSheetProduct.getId(), null, apiPricingSheetProduct.getCorrelationId(), messageSource.getMessage("api_ok_batch_response_single", new Object[]{op.toString(), apiPricingSheetProduct.getId()}, LocaleContextHolder.getLocale()), op, BatchResult.Result.success));
                            	continue;
                            } catch (Exception e) {
                        		results.add(new BatchResult(pricingSheetProduct.getId(), null, apiPricingSheetProduct.getCorrelationId(), messageSource.getMessage("batch_error_update_exception", new Object[]{op.toString(), apiPricingSheetProduct.getId()}, LocaleContextHolder.getLocale()), op, BatchResult.Result.failed));
                        		continue;
                        	}
                        } catch (ServiceException se) {
                        	se.printStackTrace();
                            results.add(new BatchResult(apiPricingSheetProduct.getId(), null, apiPricingSheetProduct.getCorrelationId(), se.getMessage(), op, BatchResult.Result.failed));
                        } catch (Exception other) {
                        	other.printStackTrace();
                        	BatchResult result = new BatchResult(apiPricingSheetProduct.getId(), null, apiPricingSheetProduct.getCorrelationId(), messageSource.getMessage("batch_error_save_exception", new Object[]{apiPricingSheetProduct.getDeviceId(), null}, LocaleContextHolder.getLocale()), op, BatchResult.Result.failed);
                            result.newMeta().addException(other);
                            results.add(result);
                        }
                }
            } else {
                results.add(new BatchResult(apiPricingSheetProduct.getCorrelationId(), messageSource.getMessage("batch_error_no_operation", new Object[]{apiPricingSheetProduct.getCorrelationId()}, LocaleContextHolder.getLocale())));
            }
        }
        
        
        return results;
    }
    
    private Service mapAPIContractServiceToCreateService(APIContractService apiContractService, Long contractId, Date serviceStartDate, Date serviceEndDate) {
    	Service service = new Service();
		service.setOnetimeRevenue(apiContractService.getOnetimePrice());
		service.setRecurringRevenue(apiContractService.getRecurringPrice());
		service.setStartDate(serviceStartDate);
		service.setEndDate(serviceEndDate);
		service.setStatus(Service.Status.active);
		service.setContractId(contractId);
		service.setDeviceId(apiContractService.getDeviceId());
		service.setDeviceName(apiContractService.getCiName());
		service.setDeviceUnitCount(apiContractService.getUnitCount());
		service.setQuantity(new Integer(1));
		service.setNote("This record was automatically created through Service Activation on: " + new Date().toString() + ".");
		
		if(service.getParentId() != null && service.getParentId().compareTo(0L) <= 0) {
    		service.setParentId(null);
    	}
		
		Service serviceOffering = applicationDataDaoService.findActiveServiceByOspId(apiContractService.getOspId());
        if(serviceOffering == null) {
        	//we'll set it to undefined if it can't find the service
        	serviceOffering = applicationDataDaoService.findActiveServiceByOspId(OSP_SERVICE_ID_UNDEFINED);
        }
        log.info("Service Offering is: " + serviceOffering);
        service.setServiceId(serviceOffering.getServiceId());
        log.info("Setting Service ID to: " + serviceOffering.getServiceId());
        
        if(apiContractService.getRelatedLineItems() != null && apiContractService.getRelatedLineItems().size() > 0) {
        	List<Service> relatedContractServices = new ArrayList<Service>();
        	for(APIContractService relatedAPIContractService: apiContractService.getRelatedLineItems()) {
        		Service relatedContractService = mapAPIContractServiceToCreateService(relatedAPIContractService, contractId, serviceStartDate, serviceEndDate);
        		relatedContractServices.add(relatedContractService);
        	}
        	service.setRelatedLineItems(relatedContractServices);
        }
        
        return service;
    }
    
    private Service mapAPIContractServiceToUpdateService(APIContractService apiContractService, Long contractId, Date serviceEndDate) throws ServiceException {
    	Service service = contractService(apiContractService.getId());
    	if(service == null) {
    		throw new ServiceException("Cannot find Line Item with ID: " + apiContractService.getId());
    	}
    	
    	if(service.getParentId() != null && service.getParentId().compareTo(0L) <= 0) {
    		service.setParentId(null);
    	}
    	
    	//set the new end date -- it's the only thing we're changing
    	service.setEndDate(serviceEndDate);
    	String note = service.getNote();
    	service.setNote(note + " This record was automatically updated through Service Activation on: " + new Date().toString() + ". The end date was changed to: " + apiContractService.getEndDate());
        
        if(apiContractService.getRelatedLineItems() != null && apiContractService.getRelatedLineItems().size() > 0) {
        	List<Service> relatedContractServices = new ArrayList<Service>();
        	for(APIContractService relatedAPIContractService: apiContractService.getRelatedLineItems()) {
        		Service relatedContractService = mapAPIContractServiceToUpdateService(relatedAPIContractService, contractId, serviceEndDate);
        		relatedContractServices.add(relatedContractService);
        	}
        	service.setRelatedLineItems(relatedContractServices);
        }
        
        return service;
    }
    
    private ContractUpdate addOrGetContractUpdateforAPIContractService(APIContractService apiContractService, Long contractId) throws ServiceException {
		ContractUpdate pcr = null;
		String pcrName = apiContractService.getPcrName();
		if(pcrName == null) {
			throw new ServiceException(messageSource.getMessage("validation_error_osm_pcr_name_required", null, LocaleContextHolder.getLocale()));
		}
		
		//see if the contract update already exists
		pcr = findContractUpdateByContractIdAndAltId(contractId, pcrName);
		
		if(pcr == null) {
			//if it doesn't exist, we create a new one
			pcr = new ContractUpdate();
			pcr.setContractId(contractId);
			pcr.setAltId(pcrName);
			pcr.setTicketNumber(pcrName);
			pcr.setSignedDate(new Date());
			pcr.setNote("This PCR was automatically created by OSM.");
			Long pcrId = saveContractUpdate(pcr);
			pcr.setId(pcrId);
		} else {
			//String note = pcr.getNote();
		}
		
		//add a note to add the CI to the notes?
		
		return pcr;
    }
    
    private ContractUpdate addOrGetContractUpdateforPCRUpdate(APIPCRUpdateRequest pcrRequest) throws ServiceException {
		ContractUpdate pcr = null;
		Long pcrId = pcrRequest.getPcrId();
		
		if(pcrId != null) {
			pcr = contractUpdate(pcrId);
			if(pcr == null) {
				throw new ServiceException(messageSource.getMessage("contract_update_not_found_for_id", new Object[] {pcrId}, LocaleContextHolder.getLocale()));
			}
			return pcr;
		}
		
		String pcrName = pcrRequest.getPcrName();
		if(pcrName == null) {
			throw new ServiceException(messageSource.getMessage("validation_error_osm_pcr_name_required", null, LocaleContextHolder.getLocale()));
		}
		
		//see if the contract update already exists
		Long contractId = pcrRequest.getContractId();
		pcr = findContractUpdateByContractIdAndAltId(contractId, pcrName);
		
		if(pcr == null) {
			BigDecimal onetimePrice = new BigDecimal(0);
			BigDecimal recurringPrice = new BigDecimal(0);
			
			if(pcrRequest.getOnetimePrice() != null) {
				onetimePrice = pcrRequest.getOnetimePrice();
			}
			
			if(pcrRequest.getRecurringPrice() != null) {
				recurringPrice = pcrRequest.getRecurringPrice();
			}
			
			//if it doesn't exist, we create a new one
			String notes = "This PCR was automatically created by Service Activation.";
			if(!StringUtils.isEmpty(pcrRequest.getPcrNotes())) {
				notes = pcrRequest.getPcrNotes();
			}
			
			pcr = new ContractUpdate();
			pcr.setContractId(contractId);
			pcr.setAltId(pcrName);
			pcr.setJobNumber(pcrRequest.getPcrJobNumber());
			pcr.setSignedDate(pcrRequest.getPcrSignedDate());
			pcr.setEffectiveDate(pcrRequest.getPcrEffectiveDate());
			pcr.setNote(notes);
			pcr.setOnetimePrice(onetimePrice);
			pcr.setRecurringPrice(recurringPrice);
			Long newPcrId = saveContractUpdate(pcr);
			pcr.setId(newPcrId);
			
			//add the doc to S3
			if(pcrRequest.getPcrDocContent() != null) {
				try {
					docManagementService.storeS3ContractUpdate(pcrRequest.getPcrDocName(), pcrRequest.getPcrDocContentType(), pcrRequest.getPcrDocContent(), newPcrId);
				} catch(Exception e) {
					log.error("Failed to upload doc to S3: " + e.getMessage());
				}
			}
			
		} else {
			//String note = pcr.getNote();
		}
		
		//add a note to add the CI to the notes?
		
		return pcr;
    }
    
    @Override
    public List<ContractServiceSubscription> contractServiceSubscriptions(Long contractId, ContractServiceSubscription.SubscriptionType type) {
    	Map<String, Object> params = new HashMap<String, Object>();
    	String query = "select csa.id, csa.service_id, csa.contract_id, csa.device_id, csa.start_date, csa.end_date, csa.subscription_id, csa.customer_id, csa.name, csa.subscription_type, csa.customer_type, s.name servname, d.description devdesc, d.part_number devpartnum"
                + " from contract_service_subscription csa"
        		+ " inner join service s on csa.service_id = s.id"
        		+ " inner join device d on csa.device_id = d.id";
    	if(contractId != null || type != null) {
    		query += " where";
	        if(contractId != null) {
	        	query += " csa.contract_id = :contract_id";
	        	params.put("contract_id", contractId);
	        }
	        if(type != null) {
	        	if(contractId != null) query += " and";
	        	query += " csa.subscription_type = :subscription_type";
	        	params.put("subscription_type", type.name());
	        }
    	}
        
        return namedJdbcTemplate.query(query, params, new RowMapper<ContractServiceSubscription>() {
            @Override
            public ContractServiceSubscription mapRow(ResultSet rs, int i) throws SQLException {
                return new ContractServiceSubscription(
                		rs.getLong("id"),
                        rs.getLong("contract_id"),
                        rs.getLong("device_id"),
                        rs.getString("devpartnum"),
                        rs.getString("devdesc"),
                        rs.getLong("service_id"),
                        rs.getString("servname"),
                        rs.getString("subscription_id"),
                        rs.getString("customer_id"),
                        rs.getDate("start_date"),
                        rs.getDate("end_date"),
                        rs.getString("name"),
                        ContractServiceSubscription.SubscriptionType.valueOf(rs.getString("subscription_type")),
                        (rs.getString("customer_type") == null ? null : ContractServiceSubscription.CustomerType.valueOf(rs.getString("customer_type"))));
            }
        });
    }
    
    @Override
    public ContractServiceSubscription contractServiceSubscription(Long id) throws ServiceException {
        Integer count = jdbcTemplate.queryForObject("select count(*) from contract_service_subscription where id = ?", Integer.class, id);
        if (!count.equals(1)) {
            throw new ServiceException(messageSource.getMessage("contractserviceazure_not_found_for_id", new Object[]{id}, LocaleContextHolder.getLocale()));
        }
        String query = "select csa.id, csa.service_id, csa.contract_id, csa.device_id, csa.start_date, csa.end_date, csa.subscription_id, csa.customer_id, csa.name, csa.subscription_type, csa.customer_type, s.name servname, d.description devdesc, d.part_number devpartnum"
                + " from contract_service_subscription csa"
        		+ " inner join service s on csa.service_id = s.id"
        		+ " inner join device d on csa.device_id = d.id"
                + " where csa.id = ?";
        return jdbcTemplate.queryForObject(query, new Object[]{id},
                new RowMapper<ContractServiceSubscription>() {
            @Override
            public ContractServiceSubscription mapRow(ResultSet rs, int i) throws SQLException {
                return new ContractServiceSubscription(
                        rs.getLong("id"),
                        rs.getLong("contract_id"),
                        rs.getLong("device_id"),
                        rs.getString("devpartnum"),
                        rs.getString("devdesc"),
                        rs.getLong("service_id"),
                        rs.getString("servname"),
                        rs.getString("subscription_id"),
                        rs.getString("customer_id"),
                        rs.getDate("start_date"),
                        rs.getDate("end_date"),
                        rs.getString("name"),
                        ContractServiceSubscription.SubscriptionType.valueOf(rs.getString("subscription_type")),
                        (rs.getString("customer_type") == null ? null : ContractServiceSubscription.CustomerType.valueOf(rs.getString("customer_type"))));
            }
        });
    }
    
    @Override
    public Long saveContractServiceSubscription(ContractServiceSubscription subscription, boolean validateInvoiceConflict) throws ServiceException {
        if (subscription.getId() != null) {
            updateContractServiceSubscription(subscription, validateInvoiceConflict);
            return subscription.getId();
        }
        validateContractServiceSubscription(subscription, BatchResult.Operation.create, validateInvoiceConflict);
        
        Number pk = null;
        try {
            
            SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
            jdbcInsert.withTableName("contract_service_subscription").usingGeneratedKeyColumns("id");
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("contract_id", subscription.getContractId());
            params.put("service_id", subscription.getServiceId());
            params.put("device_id", subscription.getDeviceId());
            params.put("subscription_id", subscription.getSubscriptionId());
            params.put("customer_id", subscription.getCustomerId());
            params.put("start_date", subscription.getStartDate());
            params.put("end_date", subscription.getEndDate());
            params.put("name", subscription.getName());
            params.put("subscription_type", subscription.getSubscriptionType());
            params.put("customer_type", subscription.getCustomerType());
            params.put("created_by", authenticatedUser());

            pk = jdbcInsert.executeAndReturnKey(new MapSqlParameterSource(params));
            
        } catch (Exception any) {
            String message = messageSource.getMessage("jdbc_error_contractserviceazure_insert", new Object[]{any.getMessage()}, LocaleContextHolder.getLocale());
            log.error(message, any);
            throw new ServiceException(message, any);
        }
        
        Contract contract = contract(subscription.getContractId());
        Customer customer = customer(contract.getCustomerId());
        try {
            final String templateLocation = getTemplateLocation("generic_messages", LocaleContextHolder.getLocale());
            final String messageSubject = messageSource.getMessage("azure_subscription_notification_subject", null, LocaleContextHolder.getLocale());
            final String messageHeader = messageSource.getMessage("azure_new_subscription_message_header", null, LocaleContextHolder.getLocale());
            final List<String> messages = new ArrayList<String>();
            messages.add(messageSource.getMessage("azure_new_subscription_message_customer", new Object[]{customer.getName(), subscription.getCustomerId()}, LocaleContextHolder.getLocale()));
            messages.add(messageSource.getMessage("azure_new_subscription_message_subscription", new Object[]{subscription.getSubscriptionId()}, LocaleContextHolder.getLocale()));
            messages.add(messageSource.getMessage("azure_new_subscription_message_instructions_1", null, LocaleContextHolder.getLocale()));
            messages.add(messageSource.getMessage("azure_new_subscription_message_instructions_2", null, LocaleContextHolder.getLocale()));
            MimeMessagePreparator preparator = new MimeMessagePreparator() {
                @Override
                public void prepare(MimeMessage mimeMessage) throws Exception {
                    MimeMessageHelper message = new MimeMessageHelper(mimeMessage, Boolean.TRUE);
                    message.setTo(azureEmailAlertList);
                    message.setFrom(noReplyEmail, "Service Insight");
                    message.setReplyTo(noReplyEmail, "Service Insight");
                    message.setSubject(messageSubject);
                    Map model = new HashMap();
                    model.put("messageDate", DateTimeFormat.forPattern("yyyy-MM-dd").print(new DateTime()));
                    model.put("messageHeader", messageHeader);
                    model.put("messages", messages);
                    String text = VelocityEngineUtils.mergeTemplateIntoString(
                            velocityEngine, templateLocation, mailEncoding, model);
                    message.setText(text, true);
                    message.addInline("logo", new ClassPathResource(String.format("%s/%s", baseTemplateLocation, emailLogo)));
                }
            };
            mailSender.send(preparator);
        } catch (Exception ex) {
            log.warn("Exception thrown sending email", ex);
        }
        return (Long) pk;
    }
    
    @Override
    public void updateContractServiceSubscription(ContractServiceSubscription subscription, boolean validateInvoiceConflict) throws ServiceException {
        if (subscription.getId() == null) {
            throw new ServiceException(messageSource.getMessage("validation_error_contractserviceazure_id", null, LocaleContextHolder.getLocale()));
        }
        Integer count = jdbcTemplate.queryForObject("select count(*) from contract_service_subscription where id = ?", Integer.class, subscription.getId());
        if (!count.equals(1)) {
            throw new ServiceException(messageSource.getMessage("contractserviceazure_not_found_for_id", new Object[]{subscription.getId()}, LocaleContextHolder.getLocale()));
        }
        validateContractServiceSubscription(subscription, BatchResult.Operation.update, validateInvoiceConflict);

        String subscriptionType = null;
        String customerType = null;
		if(subscription.getSubscriptionType() != null) {
			subscriptionType = subscription.getSubscriptionType().name();
		}
		if(subscription.getCustomerType() != null) {
			customerType = subscription.getCustomerType().name();
		}
        try {
            int updated = jdbcTemplate.update("update contract_service_subscription set contract_id = ?, service_id = ?, device_id = ?, subscription_id = ?, customer_id = ?, subscription_type = ?,"
                    + " customer_type = ?, name = ?, start_date = ?, end_date = ?, updated_by = ?, updated = ? where id = ?",
                    new Object[]{subscription.getContractId(), subscription.getServiceId(), subscription.getDeviceId(), subscription.getSubscriptionId(), subscription.getCustomerId(), 
                    		subscriptionType, customerType, subscription.getName(), subscription.getStartDate(), subscription.getEndDate(), 
                authenticatedUser(), new DateTime().withZone(DateTimeZone.forID(TZID)).toDate(), subscription.getId()});
        } catch (Exception any) {
            any.printStackTrace();
            throw new ServiceException(messageSource.getMessage("jdbc_error_contractserviceazure_update", new Object[]{subscription.getId(), any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    }

    private void validateContractServiceSubscription(ContractServiceSubscription subscription, BatchResult.Operation op, boolean validateInvoiceConflict) throws ServiceException {
        Date subscriptionStartDate = subscription.getStartDate();
        Date subscriptionEndDate = subscription.getEndDate();
        Long contractId = subscription.getContractId();
        Long serviceId = subscription.getServiceId();
        
        if (subscriptionStartDate == null) {
            throw new ServiceException(messageSource.getMessage("validation_error_startdate", null, LocaleContextHolder.getLocale()));
        }
        if (subscriptionEndDate == null) {
            throw new ServiceException(messageSource.getMessage("validation_error_enddate", null, LocaleContextHolder.getLocale()));
        }
        
    	if (contractId == null) {
            throw new ServiceException(messageSource.getMessage("validation_error_contract_reference", null, LocaleContextHolder.getLocale()));
        }
        Integer count = jdbcTemplate.queryForObject("select count(*) from contract where id = ?", Integer.class, contractId);
        if (!count.equals(1)) {
            throw new ServiceException(messageSource.getMessage("contract_not_found_for_id", new Object[]{contractId}, LocaleContextHolder.getLocale()));
        } else {
        	Contract contract = contract(contractId);
        	Date contractStartDate = contract.getStartDate();
        	Date contractEndDate = contract.getEndDate();
        	
        	if(!(subscriptionStartDate.equals(contractStartDate) || subscriptionStartDate.after(contractStartDate)) || !(subscriptionStartDate.equals(contractEndDate) || subscriptionStartDate.before(contractEndDate))) {
        		throw new ServiceException(messageSource.getMessage("validation_error_startdate_not_in_contract_date", new Object[]{contractId}, LocaleContextHolder.getLocale()));
        	}
        	
        	if(!(subscriptionEndDate.equals(contractStartDate) || subscriptionEndDate.after(contractStartDate)) || !(subscriptionEndDate.equals(contractEndDate) || subscriptionEndDate.before(contractEndDate))) {
        		throw new ServiceException(messageSource.getMessage("validation_error_enddate_not_in_contract_date", new Object[]{contractId}, LocaleContextHolder.getLocale()));
        	}
        }
        if (serviceId == null) {
            throw new ServiceException(messageSource.getMessage("validation_error_service_reference", null, LocaleContextHolder.getLocale()));
        }
        count = jdbcTemplate.queryForObject("select count(*) from service where id = ?", Integer.class, serviceId);
        if (!count.equals(1)) {
            throw new ServiceException(messageSource.getMessage("service_not_found_for_id", new Object[]{serviceId}, LocaleContextHolder.getLocale()));
        }
        
        if (subscription.getDeviceId() == null) {
            throw new ServiceException(messageSource.getMessage("validation_error_device_reference", null, LocaleContextHolder.getLocale()));
        }
        
        if(!StringUtils.isEmpty(subscription.getSubscriptionType().name())) {
        	if(ContractServiceSubscription.SubscriptionType.cspazureplan.equals(subscription.getSubscriptionType())) {
        		subscription.setSubscriptionType(ContractServiceSubscription.SubscriptionType.cspazure);
        	}
        }

        if (validateInvoiceConflict) {
            List<ContractInvoice> contractInvoices = contractInvoicesForContract(contractId);
            boolean invoiceConflictExists = contractInvoiceConflictExistsForContractServiceSubscription(contractInvoices, subscription, op);
            if (invoiceConflictExists) {
                if (op.equals(BatchResult.Operation.create)) {
                    throw new ServiceException(messageSource.getMessage("batch_error_create_contractserviceazure_contract_invoice_conflict", null, LocaleContextHolder.getLocale()));
                } else {
                    throw new ServiceException(messageSource.getMessage("batch_error_update_contractserviceazure_contract_invoice_conflict", null, LocaleContextHolder.getLocale()));
                }
            }
        }
    }
    
    @Override
    public void deleteContractServiceSubscription(Long id, boolean validateInvoiceConflict) throws ServiceException {
        Integer count = jdbcTemplate.queryForObject("select count(*) from contract_service_subscription where id = ?", Integer.class, id);
        if (!count.equals(1)) {
            throw new ServiceException(messageSource.getMessage("contractserviceazure_not_found_for_id", new Object[]{id}, LocaleContextHolder.getLocale()));
        }

        if (validateInvoiceConflict) {
        	ContractServiceSubscription subscription = contractServiceSubscription(id);
            List<ContractInvoice> contractInvoices = contractInvoicesForContract(subscription.getContractId());
            boolean invoiceConflictExists = contractInvoiceConflictExistsForContractServiceSubscription(contractInvoices, subscription, BatchResult.Operation.delete);
            if (invoiceConflictExists) {
                throw new ServiceException(messageSource.getMessage("batch_error_delete_contractserviceazure_contract_invoice_conflict", null, LocaleContextHolder.getLocale()));
            }
        }

        try {
            int updated = jdbcTemplate.update("delete from contract_service_subscription where id = ?", id);
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_contractserviceazure_delete", new Object[]{id, any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    }
    
    @Override
    public List<ContractServiceSubscription> contractServiceSubscriptionsForMonthOf(Long contractId, Integer month, String year, ContractServiceSubscription.SubscriptionType type) {
    	String query = "select csa.id, csa.service_id, csa.contract_id, csa.device_id, csa.start_date, csa.end_date, csa.subscription_id, csa.customer_id, csa.name, csa.subscription_type, csa.customer_type, s.name servname, d.description devdesc, d.part_number devpartnum"
                + " from contract_service_subscription csa"
        		+ " inner join service s on csa.service_id = s.id"
        		+ " inner join device d on csa.device_id = d.id"
                + " where (csa.start_date <= :leftDate or csa.start_date between :leftDate and :rightDate)"
                + " and (csa.end_date is null or csa.end_date between :leftDate and :rightDate"
                + " or csa.end_date >= :rightDate)";
        if (contractId != null) {
            query += " and csa.contract_id = :contractId";
        }
        if (type != null) {
            query += " and csa.subscription_type = :subscription_type";
        }
        
        final DateTime datePointer = DateTimeFormat.forPattern("yyyy").parseDateTime(year)
                .withMonthOfYear(month)
                .withDayOfMonth(1)
                .withTimeAtStartOfDay()
                .withZone(DateTimeZone.forID(TZID));
        Map<String, Object> params = new HashMap<String, Object>();
        if (contractId != null) {
            params.put("contractId", contractId);
        }
        if (type != null) {
            params.put("subscription_type", type.name());
        }
        params.put("leftDate", datePointer.toDate());
        params.put("rightDate", datePointer.dayOfMonth().withMaximumValue().plusHours(23).plusMinutes(59).plusSeconds(59).toDate());
        
        List<ContractServiceSubscription> contractServiceSubscriptions = namedJdbcTemplate.query(query, params,
                new RowMapper<ContractServiceSubscription>() {
            @Override
            public ContractServiceSubscription mapRow(ResultSet rs, int i) throws SQLException {
                return new ContractServiceSubscription(
                		rs.getLong("id"),
                        rs.getLong("contract_id"),
                        rs.getLong("device_id"),
                        rs.getString("devpartnum"),
                        rs.getString("devdesc"),
                        rs.getLong("service_id"),
                        rs.getString("servname"),
                        rs.getString("subscription_id"),
                        rs.getString("customer_id"),
                        rs.getDate("start_date"),
                        rs.getDate("end_date"),
                        rs.getString("name"),
                        ContractServiceSubscription.SubscriptionType.valueOf(rs.getString("subscription_type")),
                        (rs.getString("customer_type") == null ? null : ContractServiceSubscription.CustomerType.valueOf(rs.getString("customer_type"))));
            }
        });

        return contractServiceSubscriptions;
    }
    
    public APICustomerSubscription apiCustomerSubscription(String subscriptionId, ContractServiceSubscription.SubscriptionType type) throws ServiceException {
    	APICustomerSubscription apiCustomerSubscription = null;
    	
    	String query = "select csa.id, csa.service_id, csa.contract_id, csa.device_id, csa.start_date, csa.end_date, csa.subscription_id, csa.customer_id, csa.name, csa.subscription_type, csa.customer_type, s.name servname, d.description devdesc, d.part_number devpartnum"
                + " from contract_service_subscription csa"
        		+ " inner join service s on csa.service_id = s.id"
        		+ " inner join device d on csa.device_id = d.id"
                + " where csa.subscription_id = :subscription_id" 
        		+ " and csa.subscription_type = :subscription_type"
                + " order by csa.end_date desc";
        
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("subscription_id", subscriptionId);
        params.put("subscription_type", type.name());
        
        List<ContractServiceSubscription> contractServiceSubscriptions = namedJdbcTemplate.query(query, params,
                new RowMapper<ContractServiceSubscription>() {
            @Override
            public ContractServiceSubscription mapRow(ResultSet rs, int i) throws SQLException {
                return new ContractServiceSubscription(
                		rs.getLong("id"),
                        rs.getLong("contract_id"),
                        rs.getLong("device_id"),
                        rs.getString("devpartnum"),
                        rs.getString("devdesc"),
                        rs.getLong("service_id"),
                        rs.getString("servname"),
                        rs.getString("subscription_id"),
                        rs.getString("customer_id"),
                        rs.getDate("start_date"),
                        rs.getDate("end_date"),
                        rs.getString("name"),
                        ContractServiceSubscription.SubscriptionType.valueOf(rs.getString("subscription_type")),
                        (rs.getString("customer_type") == null ? null : ContractServiceSubscription.CustomerType.valueOf(rs.getString("customer_type"))));
            }
        });
    
        if(!contractServiceSubscriptions.isEmpty()) {
        	ContractServiceSubscription subscription = contractServiceSubscriptions.get(0);
        	
        	apiCustomerSubscription = new APICustomerSubscription();
        	apiCustomerSubscription.setSubscription(subscription);
        	
        	Contract contract = contract(subscription.getContractId());
        	
        	//don't think this will ever happen
        	if(contract == null) {
        		throw new ServiceException("A subscription was found but there is no contract associated with it.");
        	}
        	
        	apiCustomerSubscription.setContract(contract);
        	
        	Customer customer = customer(contract.getCustomerId());
        	
        	//don't think this will ever happen
        	if(customer == null) {
        		throw new ServiceException("A subscription was found but there is no customer associated with the contract.");
        	}
        	
        	apiCustomerSubscription.setCustomer(customer);
        }
        
        return apiCustomerSubscription;
    }
    
    /*This method is a utility method for finding contracts that are only "Managed" for a report*/
    @Override
    public List<Contract> contractsForDates(Date startDate, Date endDate) {
        String query = BASIC_CONTRACT_QUERY
                + " where contract.archived = :archived"
                + " and contract.start_date between :startDate and :endDate";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("archived", Boolean.FALSE);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        List<Contract> contracts = namedJdbcTemplate.query(query, params, new RowMapper<Contract>() {
            @Override
            public Contract mapRow(ResultSet rs, int i) throws SQLException {
                return new Contract(
                        rs.getLong("customer_id"),
                        rs.getLong("id"),
                        rs.getString("alt_id"),
                        rs.getString("job_number"),
                        rs.getString("name"),
                        rs.getString("emgr"),
                        rs.getString("sda"),
                        0,
                        rs.getDate("signed_date"),
                        rs.getDate("service_start_date"),
                        rs.getDate("start_date"),
                        rs.getDate("end_date"),
                        rs.getBoolean("archived"),
                        rs.getString("sn_sys_id"),
                        rs.getString("file_path"),
                        ((rs.getString("renewal_status") == null) ? null : Contract.RenewalStatus.valueOf(rs.getString("renewal_status"))),
                        rs.getBigDecimal("renewal_change"),
                        rs.getString("renewal_notes"));
            }
        });
        
        List<Contract> finalContracts = new ArrayList<Contract>();
        String childQuery = "select csvc.service_id, s.business_model"
                + " from contract_service csvc inner join service s on s.id = csvc.service_id"
        		+ " where csvc.contract_id = :contractId"
                + " group by csvc.service_id";
        for(Contract contract: contracts) {
        	Map<String, Object> childParams = new HashMap<String, Object>();
        	childParams.put("contractId", contract.getId());
            
            List<String> businessModels = namedJdbcTemplate.query(childQuery, childParams, new RowMapper<String>() {
                @Override
                public String mapRow(ResultSet rs, int i) throws SQLException {
                    return new String(rs.getString("business_model"));
                }
            });
            
            boolean containsCloud = false;
            boolean containsOther = false;
            boolean containsManaged = false;
            boolean containsCSP = false;
            for(String model: businessModels) {
            	if(model.equals("Cloud")) {
            		containsCloud = true;
            		break;
            	} else if(model.equals("Managed")) {
            		containsManaged = true;
            	} else if(model.equals("Other")) {
            		containsOther = true;
            	} else if(model.equals("CSP")) {
            		containsCSP = true;
            	}
            }
            
            if(containsManaged && !containsCloud) {
            	finalContracts.add(contract);
            }
        }
        
        return finalContracts;
    }
    
    /*This method is a utility method for finding contract updates that are only "Managed" for a report*/
    @Override
    public List<ContractUpdate> contractUpdatesForDates(Date startDate, Date endDate) {
    	String query = "select * from contract_update where signed_date between :startDate and :endDate";
    	
    	Map<String, Object> params = new HashMap<String, Object>();
        params.put("startDate", startDate);
        params.put("endDate", endDate);
    	
        List<ContractUpdate> contractUpdates = namedJdbcTemplate.query(query, params, new RowMapper<ContractUpdate>() {
            @Override
            public ContractUpdate mapRow(ResultSet rs, int i) throws SQLException {
                return new ContractUpdate(
                        rs.getLong("id"),
                        rs.getLong("contract_id"),
                        rs.getString("alt_id"),
                        rs.getString("job_number"),
                        rs.getString("ticket_number"),
                        rs.getString("note"),
                        rs.getDate("signed_date"),
                        rs.getDate("effective_date"),
                        rs.getBigDecimal("onetime_price"),
                        rs.getBigDecimal("recurring_price"),
                        rs.getDate("updated"),
                        rs.getString("updated_by"),
                        rs.getString("file_path"));
            }
        });
        
        List<ContractUpdate> finalContractUpdates = new ArrayList<ContractUpdate>();
        
        String childQuery = "select csvc.service_id, s.business_model"
                + " from contract_service csvc "
        		+ " inner join service s on s.id = csvc.service_id"
                + " inner join contract_update_contract_service cucs on cucs.contract_service_id = csvc.id"
        		+ " where cucs.contract_update_id = :contractUpdateId"
                + " group by csvc.service_id";
        for(ContractUpdate contractUpdate: contractUpdates) {
        	Map<String, Object> childParams = new HashMap<String, Object>();
        	childParams.put("contractUpdateId", contractUpdate.getId());
            
            List<String> businessModels = namedJdbcTemplate.query(childQuery, childParams, new RowMapper<String>() {
                @Override
                public String mapRow(ResultSet rs, int i) throws SQLException {
                    return new String(rs.getString("business_model"));
                }
            });
            
            boolean containsCloud = false;
            boolean containsOther = false;
            boolean containsManaged = false;
            boolean containsCSP = false;
            for(String model: businessModels) {
            	if(model.equals("Cloud")) {
            		containsCloud = true;
            		break;
            	} else if(model.equals("Managed")) {
            		containsManaged = true;
            	} else if(model.equals("Other")) {
            		containsOther = true;
            	} else if(model.equals("CSP")) {
            		containsCSP = true;
            	}
            }
            
            if(containsManaged && !containsCloud) {
            	finalContractUpdates.add(contractUpdate);
            }
        }
        
        return finalContractUpdates;
        
    }
    
    @Override
    public List<Service> contractServiceParentRecordsForDeviceAndMonthOf(Long contractId, Long deviceId, Integer month, String year) {
    	String query = "select csvc.id, csvc.service_id, csvc.parent_id, csvc.contract_id, csvc.contract_group_id, svc.code, svc.osp_id, svc.name, svc.version,"
                + " csvc.onetime_revenue, csvc.recurring_revenue, csvc.note, csvc.quantity, "
                + " csvc.start_date, csvc.end_date, csvc.status, dev.id devid, csd.name dname, csd.unit_count dcount, csd.location_id dlocation, dev.part_number, dev.description ddescr, csvc.contract_service_subscription_id, css.subscription_type, csvc.microsoft_365_subscription_config_id "
                + " from contract_service csvc"
                + " inner join service svc on svc.id = csvc.service_id"
                + " inner join contract_service_device csd on csd.contract_service_id = csvc.id"
                + " inner join device dev on dev.id = csd.device_id"
                + " inner join device_relationship devrl on devrl.device_id = csd.device_id"
                + " left outer join contract_service_subscription css on css.id = csvc.contract_service_subscription_id"
                + " where csvc.contract_id = :contractId"
                + " and devrl.related_device_id = :deviceId"
                + " and csvc.parent_id is null"
    			+ " and (csvc.start_date <= :leftDate or csvc.start_date between :leftDate and :rightDate)"
                + " and (csvc.end_date is null or csvc.end_date between :leftDate and :rightDate"
                + " or csvc.end_date >= :rightDate)"
                + " group by csvc.id"
                + " order by csd.name, dev.part_number, dev.description";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("contractId", contractId);
        params.put("deviceId", deviceId);
        
        final DateTime datePointer = DateTimeFormat.forPattern("yyyy").parseDateTime(year)
                .withMonthOfYear(month)
                .withDayOfMonth(1)
                .withTimeAtStartOfDay()
                .withZone(DateTimeZone.forID(TZID));
        Date leftDate = datePointer.toDate();
        Date rightDate = datePointer.dayOfMonth().withMaximumValue().plusHours(23).plusMinutes(59).plusSeconds(59).toDate();
        params.put("leftDate", leftDate);
        params.put("rightDate", rightDate);
        
        List<Service> contractServices = namedJdbcTemplate.query(query, params,
                new RowMapper<Service>() {
            @Override
            public Service mapRow(ResultSet rs, int i) throws SQLException {
                return new Service(
                        rs.getLong("id"),
                        rs.getString("code"),
                        (rs.getLong("parent_id") == 0 ? null : rs.getLong("parent_id")),
                        rs.getLong("contract_id"),
                        (rs.getLong("contract_group_id") == 0 ? null : rs.getLong("contract_group_id")),
                        null, // contractUpdateId
                        rs.getLong("service_id"),
                        rs.getString("osp_id"),
                        rs.getDouble("version"),
                        rs.getString("name"),
                        rs.getBigDecimal("onetime_revenue"),
                        rs.getBigDecimal("recurring_revenue"),
                        rs.getInt("quantity"),
                        rs.getString("note"),
                        0,
                        rs.getDate("start_date"),
                        rs.getDate("end_date"),
                        (rs.getLong("devid") == 0 ? null : rs.getLong("devid")),
                        rs.getString("dname"),
                        rs.getString("part_number"),
                        rs.getString("ddescr"),
                        (rs.getInt("dcount") == 0 ? null : rs.getInt("dcount")),
                        Service.Status.valueOf(rs.getString("status")),
                        (rs.getLong("contract_service_subscription_id") == 0 ? null : rs.getLong("contract_service_subscription_id")),
                        rs.getString("subscription_type"),
                        (rs.getLong("microsoft_365_subscription_config_id") == 0 ? null : rs.getLong("microsoft_365_subscription_config_id")),
                        (rs.getInt("dlocation") == 0 ? null : rs.getInt("dlocation")));
            }
        });
        return contractServices;
    }
    
    public void mapChildToParent(Long childId, Long parentId) throws ServiceException {
    	Service contractService = contractService(childId);
    	
    	log.info("CS: " + contractService);
    	
    	if(contractService == null) {
    		throw new ServiceException("Contract Service not found for ID: [" + childId + "]");
    	}
    	
    	contractService.setParentId(parentId);
    	updateContractService(contractService, Boolean.FALSE);
    	
    }
    
    private Boolean taskEnabled(String code) {
        if (StringUtils.isBlank(code)) {
            throw new IllegalArgumentException("the code is needed to check the scheduled task");
        }
        try {
            ScheduledTask st = applicationDataDaoService.findScheduledTaskByCode(code);
            if (st == null) {
                log.warn("scheduled task for code [{}] not found... returning NOT enabled.", code);
                return Boolean.FALSE;
            }
            return st.getEnabled();
        } catch(Exception any) {
            log.warn("exception thrown looking up scheduled task for [{}]... returning NOT enabled.", any.getMessage());
        }
        return Boolean.FALSE;
    }
    
}
