package com.logicalis.serviceinsight.service;

import com.logicalis.serviceinsight.dao.CostItem;
import com.logicalis.serviceinsight.dao.Expense;
import com.logicalis.serviceinsight.dao.ExpenseCategory;
import com.logicalis.serviceinsight.dao.SPLACost;
import com.logicalis.serviceinsight.dao.ServiceExpenseCategory;
import com.logicalis.serviceinsight.dao.UnitCost;
import com.logicalis.serviceinsight.dao.CostItem.CostFraction;
import com.logicalis.serviceinsight.data.AssetFractionRecord;
import com.logicalis.serviceinsight.data.Contract;
import com.logicalis.serviceinsight.data.CostAllocationLineItem;
import com.logicalis.serviceinsight.data.CostFractionRecord;
import com.logicalis.serviceinsight.data.Customer;
import com.logicalis.serviceinsight.data.Device;
import com.logicalis.serviceinsight.data.GeneralCost;
import com.logicalis.serviceinsight.data.GeneralCost.Allocation;
import com.logicalis.serviceinsight.data.ReportWrapper;
import com.logicalis.serviceinsight.data.ScheduledTask;
import com.logicalis.serviceinsight.data.Service;
import com.logicalis.serviceinsight.data.StandardCost;
import com.logicalis.serviceinsight.data.UnitCostDetails;
import com.logicalis.serviceinsight.representation.CostItemAnalysisWrapper;
import com.logicalis.serviceinsight.representation.CostItemTypeSubType;
import com.logicalis.serviceinsight.representation.LaborBreakdownRecord;
import com.logicalis.serviceinsight.representation.LaborHoursRecord;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.Months;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.StopWatch;

/**
 *
 * @author poneil
 */
@org.springframework.stereotype.Service
public class CostServiceImpl extends BaseServiceImpl implements CostService {

	@Autowired
	ApplicationDataDaoService applicationDataDaoService;
	
	@Autowired
	ContractDaoService contractDaoService;
	
	@Autowired
	ContractRevenueService contractRevenueService;
	
	@Autowired
	CostDaoService costDaoService;
	
    @Value("${standard.cost.target.utilization}")
    private double targetUtilization;
    
    public static final String SPLA_MONTHLY_COST_GENERATION_TSK = "spla_monthly_cost_generation";
    public static final String DEVICE_UNIT_COUNT_SYNC = "update_device_unit_count";

    private Double getGeneralEquipmentBurdenRate() {
        // fetch sums to calculate general equipment burden rate
        String query = "select category, sum(cost_contribution) category_monthly_cost"
                + " from view_asset_detail_cost group by category";
        List<Map<String, Object>> results = jdbcTemplate.queryForList(query);
        BigDecimal equipCost = null;
        BigDecimal otherCosts = null;
        for (Map<String, Object> result : results) {
            String category = (String) result.get("category");
            BigDecimal categoryMonthlyCost = (BigDecimal) result.get("category_monthly_cost");
            if ("Equipment".equals(category)) {
                equipCost = categoryMonthlyCost;
            } else {
                if (otherCosts == null) {
                    otherCosts = categoryMonthlyCost;
                } else {
                    otherCosts = otherCosts.add(categoryMonthlyCost);
                }
            }
        }
        return equipCost.divide(otherCosts, 2, RoundingMode.HALF_UP).doubleValue();
    }

    private Double getCloudEquipmentBurdenRate() {
        // fetch sums to calculate for cloud equipment burden rate
        String query = "select distinct id, name, description, allocation, six_month_avg, notes"
                + " from cost_general";
        List<GeneralCost> gcs = jdbcTemplate.query(query, new RowMapper<GeneralCost>() {
            @Override
            public GeneralCost mapRow(ResultSet rs, int i) throws SQLException {
                return new GeneralCost(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("allocation"),
                        rs.getBigDecimal("six_month_avg"),
                        rs.getString("notes"));
            }
        });
        BigDecimal cloudOverheadTotal = new BigDecimal(0);
        for (GeneralCost gc : gcs) {
            if (Allocation.SP.equals(gc.getAllocation())) {
                cloudOverheadTotal = cloudOverheadTotal.add(gc.getSixMonthAverage().divide(new BigDecimal(2)));
            } else if (Allocation.CL.equals(gc.getAllocation())) {
                cloudOverheadTotal = cloudOverheadTotal.add(gc.getSixMonthAverage());
            }
        }

        query = "select category, sum(cost_contribution) category_monthly_cost"
                + " from view_asset_detail_cost"
                + " where category != 'Equipment'"
                + " group by category";
        List<Map<String, Object>> results = jdbcTemplate.queryForList(query);
        BigDecimal otherCosts = null;
        for (Map<String, Object> result : results) {
            String category = (String) result.get("category");
            BigDecimal categoryMonthlyCost = (BigDecimal) result.get("category_monthly_cost");
            if (otherCosts == null) {
                otherCosts = categoryMonthlyCost;
            } else {
                otherCosts = otherCosts.add(categoryMonthlyCost);
            }
        }
        return cloudOverheadTotal.divide(otherCosts, 2, RoundingMode.HALF_UP).doubleValue();
    }

    @Override
    public List<StandardCost> getStandardCosts() {
        final double generalEquipmentBurdenRate = getGeneralEquipmentBurdenRate();
        final double cloudEquipmentBurdenRate = getCloudEquipmentBurdenRate();
        log.debug("reporting generalEquipmentBurdenRate: [{}], cloudEquipmentBurdenRate as [{}]",
                new Object[]{generalEquipmentBurdenRate, cloudEquipmentBurdenRate});

        String query = "select category, detail, sum(cost_contribution) sum_cost_contribution,"
                + " sum(capacity) sum_capacity, sum(cost_contribution)/sum(capacity) base_standard"
                + " from view_asset_detail_cost"
                + " where category != 'Equipment'"
                + " group by category, detail order by category, detail";
        return jdbcTemplate.query(query, new RowMapper<StandardCost>() {
            @Override
            public StandardCost mapRow(ResultSet rs, int i) throws SQLException {
                return new StandardCost(
                        rs.getString("category"),
                        rs.getString("detail"),
                        rs.getBigDecimal("sum_cost_contribution"),
                        rs.getBigDecimal("sum_capacity"),
                        rs.getBigDecimal("base_standard"),
                        targetUtilization,
                        generalEquipmentBurdenRate,
                        cloudEquipmentBurdenRate);
            }
        });
    }

    @Override
    public Map<String, Map<String, BigDecimal>> laborRevenueForYear(Long ospId, String year) {
        Map<String, Map<String, BigDecimal>> yearlyLabor = new TreeMap<String, Map<String, BigDecimal>>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return months.valueOf(o1).compareTo(months.valueOf(o2));
            }
        });
        for (int month = 1; month <= 12; month++) {
            yearlyLabor.put(
                    DateTimeFormat.forPattern("yyyy").parseDateTime(year)
                    .withMonthOfYear(month).monthOfYear().getAsText(),
                    serviceLaborForMonth(null, ospId, month, year, null, Boolean.FALSE, Boolean.FALSE));
        }
        return yearlyLabor;
    }

    @Override
    public Map<String, BigDecimal> serviceLaborForMonth(String businessModel, Long ospId, Integer month, String year, Long customerId, Boolean includeChildren, Boolean onboarding) {
        if (month == null || StringUtils.isBlank(year)) {
            throw new IllegalArgumentException("month and year are required fields for labor cost search");
        }
        DateTime startDate = DateTimeFormat.forPattern("yyyy").parseDateTime(year)
                .withMonthOfYear(month)
                .withDayOfMonth(1)
                .withTimeAtStartOfDay()
                .withZone(DateTimeZone.forID(TZID));
        DateTime endDate = startDate.dayOfMonth().withMaximumValue().plusHours(23).plusMinutes(59).plusSeconds(59);
        return serviceLaborForDates(businessModel, ospId, startDate, endDate, customerId, includeChildren, onboarding);
    }

    @Override
    public Map<String, BigDecimal> serviceLaborForDates(String businessModel, Long ospId, DateTime startDate, DateTime endDate, Long customerId, Boolean includeChildren, Boolean onboarding) {
        StopWatch sw = new StopWatch();
        sw.start();
        if (startDate == null || endDate == null) { // don't mess with one of these being null... override
            startDate = new DateTime()
                    .withZone(DateTimeZone.forID(TZID))
                    .withMonthOfYear(1)
                    .withDayOfMonth(1) // default is beginning of year...
                    .withTimeAtStartOfDay();
            endDate = startDate
                    .monthOfYear()
                    .withMaximumValue() // default is end of year...
                    .plusHours(23).plusMinutes(59).plusSeconds(59);
        }
        String query = "select sum(ld.labor_total) as labor_total, sum(ld.addl_labor_total) as addl_labor_total"
                + " from grouped_labor_data ld";
        if(customerId != null && customerId > 0 && includeChildren != null && includeChildren) {
                query += " inner join customer cu on cu.id = ld.customer_id";
        }
        if (ospId != null || StringUtils.isNotBlank(businessModel)) {
            query += " where ld.work_date between :leftDate and :rightDate and onboarding = :onboarding";
            if (ospId != null) {
                query += " and ld.osp_id = :ospId";
            } else if (businessModel != null) {
                query += " and ld.business_model = :businessModel";
            }
        } else {
            query += " where ld.work_date between :leftDate and :rightDate and onboarding = :onboarding";
        }
        if (customerId != null && customerId > 0) {
            if (includeChildren != null && includeChildren) {
                query += " and (ld.customer_id = :customerId or cu.parent_id = :customerId)";
            } else {
                query += " and ld.customer_id = :customerId";
            }
        }
        if (ospId == null && businessModel == null) {
            query += " and ld.record_type in ('direct', 'derived')";
        } else {
            query += " and ld.record_type = 'direct'";
        }
        Map<String, Object> params = new HashMap<String, Object>();
        if (ospId != null) {
            params.put("ospId", ospId);
        } else if (StringUtils.isNotBlank(businessModel)) {
            params.put("businessModel", businessModel);
        }
        params.put("leftDate", startDate.toDate());
        params.put("rightDate", endDate.toDate());
        params.put("onboarding", onboarding);
        if (customerId != null && customerId > 0) {
            params.put("customerId", customerId);
        }
        Map<String, Object> laborTotalMap = namedJdbcTemplate.queryForMap(query, params);
        sw.stop();
        Map<String, BigDecimal> resultTotalMap = convertLaborMap(laborTotalMap);
        
        log.info("Direct labor query took {} seconds", new Object[]{sw.getTotalTimeSeconds()});
        if (ospId != null || businessModel != null) {
            Map<String, BigDecimal> derivedLaborTotalMap = derivedServiceLaborForDates(businessModel, ospId, startDate, endDate, customerId, includeChildren, onboarding);
            resultTotalMap.put("labor_total", resultTotalMap.get("labor_total").add(derivedLaborTotalMap.get("labor_total")));
            resultTotalMap.put("addl_labor_total", resultTotalMap.get("addl_labor_total").add(derivedLaborTotalMap.get("addl_labor_total")));
        }
        return resultTotalMap;
    }

    /**
     * @deprecated no longer use the service_expense_category table
     * @param businessModel
     * @param ospId
     * @param startDate
     * @param endDate
     * @return 
     */
    @Override
    public Map<String, BigDecimal> expenseCategoryLaborForDates(String businessModel, Long ospId, DateTime startDate, DateTime endDate) {
        StopWatch sw = new StopWatch();
        sw.start();
        if (startDate == null || endDate == null) { // don't mess with one of these being null... override
            startDate = new DateTime()
                    .withZone(DateTimeZone.forID(TZID))
                    .withMonthOfYear(1)
                    .withDayOfMonth(1) // default is beginning of year...
                    .withTimeAtStartOfDay();
            endDate = startDate
                    .monthOfYear()
                    .withMaximumValue() // default is end of year...
                    .plusHours(23).plusMinutes(59).plusSeconds(59);
        }
        String query = "select sum(ld.labor_total) as labor_total, sum(ld.addl_labor_total) as addl_labor_total"
                + " from grouped_labor_data ld"
                + " inner join service_expense_category sec on ld.expense_category_id = sec.expense_category_id"
                + " where ld.work_date between :leftDate and :rightDate";
        if (ospId != null || StringUtils.isNotBlank(businessModel)) {
            if (ospId != null) {
                query += " and ld.osp_id = sec.osp_id and ld.osp_id = :ospId";
            } else if (businessModel != null) {
                query += " and ld.business_model = :businessModel";
            }
        }
        query += " and ld.record_type = 'categorized'";
        Map<String, Object> params = new HashMap<String, Object>();
        if (ospId != null) {
            params.put("ospId", ospId);
        } else if (StringUtils.isNotBlank(businessModel)) {
            params.put("businessModel", businessModel);
        }
        params.put("leftDate", startDate.toDate());
        params.put("rightDate", endDate.toDate());
        
        return queryForLaborMap(query, params, sw);
    }
    
    /**
     * Labor Data may consist of hours where a customer was specified but not a Service... so, that work
     * has been split up equally amongst the Services that customer had for that month period and put into
     * the "derived_labor_data" table.
     * 
     * @param businessModel
     * @param ospId
     * @param startDate
     * @param endDate
     * @param customerId
     * @return 
     */
    private Map<String, BigDecimal> derivedServiceLaborForDates(String businessModel, Long ospId, DateTime startDate, DateTime endDate, Long customerId, Boolean includeChildren, Boolean onboarding) {
        
        if (businessModel == null && ospId == null) {
            throw new IllegalArgumentException("Derived labor requires customerId and one of either OSP Id OR businessModel");
        }
        StopWatch sw = new StopWatch();
        sw.start();
        if (startDate == null || endDate == null) { // don't mess with one of these being null... override
            startDate = new DateTime()
                    .withZone(DateTimeZone.forID(TZID))
                    .withMonthOfYear(1)
                    .withDayOfMonth(1) // default is beginning of year...
                    .withTimeAtStartOfDay();
            endDate = startDate
                    .monthOfYear()
                    .withMaximumValue() // default is end of year...
                    .plusHours(23).plusMinutes(59).plusSeconds(59);
        }
        String query = "select sum(ld.labor_total) as labor_total, sum(ld.addl_labor_total) as addl_labor_total"
                + " from derived_labor_data ld";
        if(customerId != null && customerId > 0 && includeChildren != null && includeChildren) {
                query += " inner join customer cu on cu.id = ld.customer_id";
        }
        query += " where ld.work_date between :leftDate and :rightDate and onboarding = :onboarding";
        if (ospId != null) {
            query += " and ld.osp_id = :ospId";
        } else if (businessModel != null) {
            query += " and ld.business_model = :businessModel";
        }
        if (customerId != null && customerId > 0) {
            if (includeChildren != null && includeChildren) {
                query += " and (ld.customer_id = :customerId or cu.parent_id = :customerId)";
            } else {
                query += " and ld.customer_id = :customerId";
            }
        }
        Map<String, Object> params = new HashMap<String, Object>();
        if (ospId != null) {
            params.put("ospId", ospId);
        } else if (StringUtils.isNotBlank(businessModel)) {
            params.put("businessModel", businessModel);
        }
        params.put("leftDate", startDate.toDate());
        params.put("rightDate", endDate.toDate());
        params.put("onboarding", onboarding);
        if (customerId != null && customerId > 0) {
            params.put("customerId", customerId);
        }
        return queryForLaborMap(query, params, sw);
    }
    
    /**
     * convenience method that returns a standard labor map for other methods to use with keys:
     * - labor_total
     * - addl_labor_total
     * 
     * @param query
     * @param params
     * @param sw
     * @return 
     */
    private Map<String, BigDecimal> queryForLaborMap(String query, Map<String, Object> params, StopWatch sw) {
        Map<String, Object> laborTotalMap = namedJdbcTemplate.queryForMap(query, params);
        if (sw != null) {
            sw.stop();
        }
        Map<String, BigDecimal> resultTotalMap = convertLaborMap(laborTotalMap);
        if (sw != null) {
            log.info("labor query took {} seconds", new Object[]{sw.getTotalTimeSeconds()});
        }
        return resultTotalMap;
    }
    
    /**
     * convenience method that takes a generic query Map and converts to the labor map with keys:
     * - labor_total
     * - addl_labor_total
     * 
     * @param laborTotalMap
     * @return 
     */
    private Map<String, BigDecimal> convertLaborMap(Map<String, Object> laborTotalMap) {
        if (laborTotalMap == null) {
            return new HashMap<String, BigDecimal>();
        }
        BigDecimal laborTotal = (laborTotalMap.get("labor_total") == null ? BigDecimal.ZERO : (BigDecimal) laborTotalMap.get("labor_total"));
        BigDecimal addlLaborTotal = (laborTotalMap.get("addl_labor_total") == null ? BigDecimal.ZERO : (BigDecimal) laborTotalMap.get("addl_labor_total"));
        Map<String, BigDecimal> resultTotalMap = new HashMap<String, BigDecimal>();
        resultTotalMap.put("labor_total", laborTotal.setScale(2, RoundingMode.HALF_UP));
        resultTotalMap.put("addl_labor_total", addlLaborTotal.setScale(2, RoundingMode.HALF_UP));
        return resultTotalMap;
    }
    
    /**
     * @deprecated we are not calculating indirect labor using a proportion of total indirect labor / direct labor
     * @param fordate
     * @param businessModel
     * @return 
     */
    @Override
    public Map<String, BigDecimal> indirectLaborUnitCost(Date fordate, String businessModel) {
        StopWatch sw = new StopWatch();
        sw.start();
        if (fordate == null) {
            throw new IllegalArgumentException("A date must be provided to lookup unit cost");
        }
        DateTime unitCostDateTime = new DateTime(fordate)
                .withDayOfMonth(1)
                .withTimeAtStartOfDay();
        Map<String, Object> unitCostMap = null;
        if (StringUtils.isBlank(businessModel)) {
            try {
                unitCostMap = jdbcTemplate.queryForMap("select unit_cost, addl_unit_cost from indirect_labor_unit_cost"
                        + " where business_model = 'blended' and applied_date = ?", new Object[]{unitCostDateTime.toDate()});
            } catch(EmptyResultDataAccessException ignore) {
                unitCostMap = new HashMap<String, Object>();
            }
        } else {
            try {
                unitCostMap = jdbcTemplate.queryForMap("select unit_cost, addl_unit_cost from indirect_labor_unit_cost"
                        + " where business_model = ? and applied_date = ?", new Object[]{businessModel, unitCostDateTime.toDate()});
            } catch(EmptyResultDataAccessException ignore) {
                unitCostMap = new HashMap<String, Object>();
            }
        }
        sw.stop();
        log.info("Indirect labor took {} seconds", new Object[]{sw.getTotalTimeSeconds()});
        
        BigDecimal unitCost = (unitCostMap.get("unit_cost") == null ? BigDecimal.ZERO : (BigDecimal) unitCostMap.get("unit_cost"));
        BigDecimal addlUnitCost = (unitCostMap.get("addl_unit_cost") == null ? BigDecimal.ZERO : (BigDecimal) unitCostMap.get("addl_unit_cost"));
        Map<String, BigDecimal> resultTotalMap = new HashMap<String, BigDecimal>();
        resultTotalMap.put("unit_cost", unitCost.setScale(2, RoundingMode.HALF_UP));
        resultTotalMap.put("addl_unit_cost", addlUnitCost.setScale(2, RoundingMode.HALF_UP));
        return resultTotalMap;
    }
    
    @Override
    public Map<String, BigDecimal> indirectLaborProportionForDates(DateTime startDate, DateTime endDate) {
        if (startDate == null || endDate == null) { // don't mess with one of these being null... override
            startDate = new DateTime()
                    .withZone(DateTimeZone.forID(TZID))
                    .withMonthOfYear(1)
                    .withDayOfMonth(1) // default is beginning of year...
                    .withTimeAtStartOfDay();
            endDate = startDate
                    .monthOfYear()
                    .withMaximumValue() // default is end of year...
                    .plusHours(23).plusMinutes(59).plusSeconds(59);
        }
        String query = "select sum(ld.labor_total) as labor_total, sum(ld.addl_labor_total) as addl_labor_total"
                + " from grouped_labor_data ld"
                + " where ld.record_type = 'indirect'"
                + " and ld.work_date between :leftDate and :rightDate";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("leftDate", startDate.toDate());
        params.put("rightDate", endDate.toDate());
        Map<String, Object> indirectLaborTotalMap = namedJdbcTemplate.queryForMap(query, params);
        
        query = "select sum(ld.labor_total) as labor_total, sum(ld.addl_labor_total) as addl_labor_total"
                + " from grouped_labor_data ld"
                + " where ld.record_type in ('direct', 'derived')"
                + " and ld.work_date between :leftDate and :rightDate";
        Map<String, Object> directLaborTotalMap = namedJdbcTemplate.queryForMap(query, params);
        
        Map<String, BigDecimal> indirectLaborProportionMap = new HashMap<String, BigDecimal>();
        indirectLaborProportionMap.put("indirect_labor_total_proportion", BigDecimal.ZERO);
        indirectLaborProportionMap.put("addl_indirect_labor_total_proportion", BigDecimal.ZERO);
        BigDecimal directLaborTotal = (BigDecimal) directLaborTotalMap.get("labor_total");
        BigDecimal addlDirectLaborTotal = (BigDecimal) directLaborTotalMap.get("addl_labor_total");
        BigDecimal indirectLaborTotal = (BigDecimal) indirectLaborTotalMap.get("labor_total");
        BigDecimal addlIndirectLaborTotal = (BigDecimal) indirectLaborTotalMap.get("addl_labor_total");
        if (directLaborTotal != null && directLaborTotal.compareTo(BigDecimal.ZERO) > 0 && indirectLaborTotal != null) {
            indirectLaborProportionMap.put("indirect_labor_total_proportion", indirectLaborTotal.divide(directLaborTotal, MathContext.DECIMAL32));
        }
        if (addlDirectLaborTotal != null && addlDirectLaborTotal.compareTo(BigDecimal.ZERO) > 0 && addlIndirectLaborTotal != null) {
            indirectLaborProportionMap.put("addl_indirect_labor_total_proportion", addlIndirectLaborTotal.divide(addlDirectLaborTotal, MathContext.DECIMAL32));
        }
        return indirectLaborProportionMap;
    }

    @Override
    public Map<String, BigDecimal> indirectLaborForDates(DateTime startDate, DateTime endDate, String businessModel) {
        Map<String, BigDecimal> laborCostMap = indirectLaborForRuleAndDates(startDate, endDate, "SPREAD_ALL", false);
        BigDecimal laborCost = laborCostMap.get("labor_total");
        BigDecimal addLaborCost = laborCostMap.get("addl_labor_total");
        Integer deviceCount = serviceTotalDeviceCount(startDate, endDate, null);
        BigDecimal unitLaborCost = new BigDecimal(0);
        BigDecimal unitAddlLaborCost = new BigDecimal(0);
        if (deviceCount != null && deviceCount > 0) {
            unitLaborCost = laborCost.divide(new BigDecimal(deviceCount), MathContext.DECIMAL32);
            unitAddlLaborCost = addLaborCost.divide(new BigDecimal(deviceCount), MathContext.DECIMAL32);
        }
        // add businessModel specific labor cost
        if (StringUtils.isNotBlank(businessModel)) {
            laborCostMap = indirectLaborForRuleAndDates(startDate, endDate, businessModel, false);
            laborCost = laborCostMap.get("labor_total");
            addLaborCost = laborCostMap.get("addl_labor_total");
            if (laborCost.compareTo(new BigDecimal(0)) > 0) {
                deviceCount = serviceTotalDeviceCount(startDate, endDate, businessModel);
                if (deviceCount != null && deviceCount > 0) {
                    unitLaborCost = unitLaborCost.add(laborCost.divide(new BigDecimal(deviceCount), MathContext.DECIMAL32));
                    if (addLaborCost.compareTo(new BigDecimal(0)) > 0) {
                        unitAddlLaborCost = unitAddlLaborCost.add(addLaborCost.divide(new BigDecimal(deviceCount), MathContext.DECIMAL32));
                    }
                }
            }
        } else {
            // in this case generate a blended unit cost
            laborCostMap = indirectLaborForRuleAndDates(startDate, endDate, "SPREAD_ALL", true);
            laborCost = laborCost.add(laborCostMap.get("labor_total"));
            addLaborCost = addLaborCost.add(laborCostMap.get("addl_labor_total"));
            if (deviceCount != null && deviceCount > 0) {
                unitLaborCost = laborCost.divide(new BigDecimal(deviceCount), MathContext.DECIMAL32);
                unitAddlLaborCost = addLaborCost.divide(new BigDecimal(deviceCount), MathContext.DECIMAL32);
            }
        }
        Map<String, BigDecimal> resultTotalMap = new HashMap<String, BigDecimal>();
        resultTotalMap.put("labor_total", unitLaborCost.setScale(2, RoundingMode.HALF_UP));
        resultTotalMap.put("addl_labor_total", unitAddlLaborCost.setScale(2, RoundingMode.HALF_UP));
        return resultTotalMap;
    }
    
    /**
     * Provides a Map of labor costs keyed for "labor_total" and "addl_labor_total"
     * 
     * @param startDate
     * @param endDate
     * @param rule
     * @param exclude
     * @return 
     */
    private Map<String, BigDecimal> indirectLaborForRuleAndDates(DateTime startDate, DateTime endDate, String rule, Boolean exclude) {
        StopWatch sw = new StopWatch();
        sw.start();
        if (StringUtils.isBlank(rule)) {
            throw new IllegalArgumentException("A rule must be provided");
        }
        if (startDate == null || endDate == null) { // don't mess with one of these being null... override
            startDate = new DateTime()
                    .withZone(DateTimeZone.forID(TZID))
                    .withMonthOfYear(1)
                    .withDayOfMonth(1) // default is beginning of year...
                    .withTimeAtStartOfDay();
            endDate = startDate
                    .monthOfYear()
                    .withMaximumValue() // default is end of year...
                    .plusHours(23).plusMinutes(59).plusSeconds(59);
        }
        String query = "select sum(ld.labor_total) as labor_total, sum(ld.addl_labor_total) as addl_labor_total"
                + " from grouped_labor_data ld"
                + " where ld.record_type = 'indirect' and ld.rule is not null"
                + " and ld.work_date between :leftDate and :rightDate";
        if (exclude) {
            query+= " and ld.rule != :theRule";
        } else {
            query+= " and ld.rule = :theRule";
        }
        
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("leftDate", startDate.toDate());
        params.put("rightDate", endDate.toDate());
        params.put("theRule", rule);
        Map<String, Object> laborTotalMap = namedJdbcTemplate.queryForMap(query, params);
        sw.stop();
        double t1 = sw.getTotalTimeSeconds();
        log.info("Indirect labor {} [exclude: {}] {} seconds",
                new Object[]{rule, exclude, t1});
        
        return convertLaborMap(laborTotalMap);
    }

    @Override
    public Integer deviceTotalDeviceCount(DateTime startDate, DateTime endDate, String businessModel) {
        return deviceTotalDeviceCount(startDate, endDate, businessModel, null, null, null);
    }

    @Override
    public Integer deviceTotalDeviceCount(DateTime startDate, DateTime endDate, String businessModel, Long customerId, Long ospId, Long deviceId) {
        Map<String, Object> params = new HashMap<String, Object>();
        String query = "select sum(case when (csd.unit_count = 0 or csd.unit_count is null) then csvc.quantity else csvc.quantity * csd.unit_count end) as 'unit_count'"
                + " from contract_service csvc"
                + " inner join contract_service_device csd on csd.contract_service_id = csvc.id"
                + " inner join device dev on csd.device_id = dev.id"
                + " inner join contract ctr on ctr.id = csvc.contract_id"
                + " inner join customer cst on cst.id = ctr.customer_id";
        if (ospId != null || StringUtils.isNotBlank(businessModel)) {
            query += " inner join service svc on csvc.service_id = svc.id";
        }
        query += " where (csvc.start_date <= :leftDate or csvc.start_date between :leftDate and :rightDate)"
                + " and (csvc.end_date is null or csvc.end_date between :leftDate and :rightDate"
                + " or csvc.end_date >= :rightDate)"
                + " and (csvc.status = 'active' or csvc.status = 'donotbill')"
                + " and (dev.device_type is null or dev.device_type not in ('businessService'))";
        if (StringUtils.isNotBlank(businessModel)) {
            query += " and svc.business_model = :businessModel";
            params.put("businessModel", businessModel);
        }
        if (ospId != null) {
            query += " and svc.osp_id = :ospId";
            params.put("ospId", ospId);
        }
        if (deviceId != null) {
            query += " and dev.id = :deviceId";
            params.put("deviceId", deviceId);
        }
        if (customerId != null && customerId > 0) {
            query += " and cst.id = :customerId";
            params.put("customerId", customerId);
        }
        params.put("leftDate", startDate.toDate());
        params.put("rightDate", endDate.toDate());
        Integer dtdc =  namedJdbcTemplate.queryForObject(query, params, Integer.class);
        if (dtdc == null) {
            return 0;
        }
        return dtdc;
    }

    @Override
    public Integer deviceTotalDeviceCountWithExpenseCategory(DateTime startDate, DateTime endDate, String businessModel, Long customerId, Long ospId, Integer expenseCategoryId, Long deviceId) throws ServiceException {
        Map<String, Object> params = new HashMap<String, Object>();
        String query = "select sum(case when (csd.unit_count = 0 or csd.unit_count is null) then csvc.quantity else csvc.quantity * csd.unit_count end) as 'unit_count'"
                + " from contract_service csvc"
                + " inner join contract_service_device csd on csd.contract_service_id = csvc.id"
                + " inner join device dev on csd.device_id = dev.id"
                + " inner join contract ctr on ctr.id = csvc.contract_id"
                + " inner join customer cst on cst.id = ctr.customer_id";
        if (ospId != null || StringUtils.isNotBlank(businessModel)) {
            query += " inner join service svc on csvc.service_id = svc.id";
        }
        query += " inner join device_expense_category devcat on csd.device_id = devcat.device_id"
                + " where (csvc.start_date <= :leftDate or csvc.start_date between :leftDate and :rightDate)"
                + " and (csvc.end_date is null or csvc.end_date between :leftDate and :rightDate"
                + " or csvc.end_date >= :rightDate)"
                + " and (csvc.status = 'active' or csvc.status = 'donotbill')"
                + " and (dev.device_type is null or dev.device_type not in ('businessService'))";
        if (StringUtils.isNotBlank(businessModel)) {
            query += " and svc.business_model = :businessModel";
            params.put("businessModel", businessModel);
        }
        if (ospId != null) {
            query += " and svc.osp_id = :ospId";
            params.put("ospId", ospId);
        }
        query += " and devcat.expense_category_id = :expenseCategoryId";
        params.put("expenseCategoryId", expenseCategoryId);
        if (deviceId != null) {
            query += " and dev.id = :deviceId";
            params.put("deviceId", deviceId);
        }
        if (customerId != null && customerId > 0) {
            query += " and cst.id = :customerId";
            params.put("customerId", customerId);
        }
        params.put("leftDate", startDate.toDate());
        params.put("rightDate", endDate.toDate());
        Integer dtdc =  namedJdbcTemplate.queryForObject(query, params, Integer.class);
        if (dtdc == null) {
            return 0;
        }
        return dtdc;
    }

    @Override
    public Integer serviceTotalDeviceCount(DateTime startDate, DateTime endDate, String businessModel) {
        return serviceTotalDeviceCount(startDate, endDate, businessModel, null, null);
    }
    
    @Override
    public Integer serviceTotalDeviceCount(DateTime startDate, DateTime endDate, String businessModel, Long customerId, Long ospId) {
        Map<String, Object> params = new HashMap<String, Object>();
        String query = "select sum(csvc.quantity) quantity from contract_service csvc"
                + " left join service svc on svc.id = csvc.service_id"
                + " left join contract ctr on ctr.id = csvc.contract_id"
                + " left join customer cst on cst.id = ctr.customer_id"
                + " left join contract_service_device csd on csd.contract_service_id = csvc.id"
                + " left join device d on d.id = csd.device_id";
        query += " where (csvc.start_date <= :leftDate or csvc.start_date between :leftDate and :rightDate)"
                + " and (csvc.end_date is null or csvc.end_date between :leftDate and :rightDate"
                + " or csvc.end_date >= :rightDate) and csvc.status != 'pending'"
                + " and (d.device_type is null or d.device_type not in ('businessService'))"
                + " and (csd.unit_count is null or csd.unit_count = 0)";
        if (StringUtils.isNotBlank(businessModel)) {
            query += " and svc.business_model = :businessModel";
            params.put("businessModel", businessModel);
        }
        if (ospId != null) {
            query += " and svc.osp_id = :ospId";
            params.put("ospId", ospId);
        }
        if (customerId != null && customerId > 0) {
            query += " and cst.id = :customerId";
            params.put("customerId", customerId);
        }
        params.put("leftDate", startDate.toDate());
        params.put("rightDate", endDate.toDate());
        Integer stdc =  namedJdbcTemplate.queryForObject(query, params, Integer.class);
        if (stdc == null) {
            return 0;
        }
        return stdc;
    }
    
    @Override
    public Map<String, List<CostFractionRecord>> customerDirectCostsForExpenseCategoryAndDateRange(Integer expenseCategoryId, DateTime startDate, DateTime endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("start date and end date are required");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("start date must be before end date");
        }
        Map<String, List<CostFractionRecord>> results = new TreeMap<String, List<CostFractionRecord>>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                DateTime d1 = DateTimeFormat.forPattern("MM/yyyy").parseDateTime(o1);
                DateTime d2 = DateTimeFormat.forPattern("MM/yyyy").parseDateTime(o2);
                return d1.compareTo(d2);
            }
        });
        while (Months.monthsBetween(startDate, endDate).getMonths() >= 0) {
            String key = DateTimeFormat.forPattern("MM/yyyy").print(startDate);
            List<CostFractionRecord> cfrs = customerDirectCostsForExpenseCategory(expenseCategoryId, startDate);
            if (cfrs != null && !cfrs.isEmpty()) {
                results.put(key, cfrs);
            }
            startDate = startDate.plusMonths(1);
        }
        return results;
    }
    
    @Override
    public List<CostFractionRecord> customerDirectCostsForExpenseCategory(Integer expenseCategoryId, final DateTime appliedDate) {
        if (expenseCategoryId == null || appliedDate == null) {
            throw new IllegalArgumentException("Expense ID and Applied Date must not be null");
        }
        DateTime startDate = appliedDate
                .withZone(DateTimeZone.forID(TZID))
                .withDayOfMonth(1) // default is beginning of year...
                .withTimeAtStartOfDay();
        DateTime endDate = startDate.dayOfMonth().withMaximumValue()
                .plusHours(23).plusMinutes(59).plusSeconds(59);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("startDate", startDate.toDate());
        params.put("endDate", endDate.toDate());
        params.put("expense_id", expenseCategoryId);
        String query = "select cu.id as 'cust_id', cu.name as 'customer', exp.id as 'exp_id', exp.name as 'expense',"
                + " sum(ci.amount * cif.cost_fraction / 100) as 'cost'"
                + " from cost_item ci"
                + " inner join cost_item_fraction cif on ci.id = cif.cost_item_id"
                + " inner join expense_category exp on exp.id = cif.expense_category_id"
                + " inner join customer cu on ci.customer_id = cu.id"
                + " where exp.id = :expense_id"
                + " and ci.applied between :startDate and :endDate"
                + " and ci.customer_id is not null"
                + " group by cu.id, cu.name, exp.id, exp.name"
                + " order by cu.name";
        List<CostFractionRecord> cfrs = namedJdbcTemplate.query(query, params, new RowMapper<CostFractionRecord>() {
            @Override
            public CostFractionRecord mapRow(ResultSet rs, int i) throws SQLException {
                return new CostFractionRecord(
                    null,
                    rs.getLong("cust_id"),
                    rs.getString("customer"),
                    null,
                    rs.getInt("exp_id"),
                    rs.getString("expense"),
                    rs.getBigDecimal("cost"),
                    1,
                    appliedDate);
            }
        });
        return cfrs;
    }
    
    @Override
    public BigDecimal totalCostForCostCategory(final DateTime costDate, Long customerId, Integer expenseCategoryId, Boolean nonCustomer) {
        if (costDate == null) {
            throw new IllegalArgumentException("Applied Date must not be null");
        }
        if (expenseCategoryId == null) {
            throw new IllegalArgumentException("ExpenseCategory ID must not be null");
        }
        DateTime startDate = costDate
                .withZone(DateTimeZone.forID(TZID))
                .withDayOfMonth(1) // default is beginning of year...
                .withTimeAtStartOfDay();
        DateTime endDate = startDate.dayOfMonth().withMaximumValue()
                .plusHours(23).plusMinutes(59).plusSeconds(59);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("startDate", startDate.toDate());
        params.put("endDate", endDate.toDate());
        params.put("expenseCategoryId", expenseCategoryId);
        String query = "select sum(ci.amount * cif.cost_fraction / 100) as 'total_cost'"
                + " from cost_item ci"
                + " inner join cost_item_fraction cif on ci.id = cif.cost_item_id"
                + " where ci.applied between :startDate and :endDate";
        if (customerId != null && customerId > 0) {
            query += " and ci.customer_id = :customerId";
            params.put("customerId", customerId);
        } else if (nonCustomer) {
            query += " and ci.customer_id is null";
        } else {
            query += " and ci.customer_id is not null";
        }
        query += " and cif.expense_category_id = :expenseCategoryId";
        BigDecimal totalCost = namedJdbcTemplate.queryForObject(query, params, BigDecimal.class);
        if (totalCost == null) {
            totalCost = BigDecimal.ZERO;
        }
        return totalCost;
    }

    @Override
    public List<Map<String, Map<String, BigDecimal>>> serviceLaborByServiceForMonth(DateTime month) {
        if (month == null) {
            throw new IllegalArgumentException("month are required fields for labor cost search");
        }
        DateTime startDate = month
                .withDayOfMonth(1)
                .withTimeAtStartOfDay()
                .withZone(DateTimeZone.forID(TZID));
        DateTime endDate = startDate.dayOfMonth().withMaximumValue().plusHours(23).plusMinutes(59).plusSeconds(59);
        return serviceLaborByServiceForDates(startDate, endDate);
    }

    @Override
    public List<Map<String, Map<String, BigDecimal>>> serviceLaborByServiceForDates(DateTime startDate, DateTime endDate) {
        if (startDate == null || endDate == null) { // don't mess with one of these being null... override
            startDate = new DateTime();
            endDate = new DateTime();
        }

        startDate = startDate
                .withZone(DateTimeZone.forID(TZID))
                .withDayOfMonth(1)
                .withTimeAtStartOfDay();
        endDate = endDate
                .withZone(DateTimeZone.forID(TZID))
                .dayOfMonth().withMaximumValue()
                .withTimeAtStartOfDay().plusHours(23).plusMinutes(59).plusSeconds(59);

        String query = "select ld.service_name name, sum(ld.labor_total) labor_total, sum(ld.addl_labor_total) addl_labor_total"
                + " from grouped_labor_data ld"
                + " where ld.record_type = 'direct'"
                + " and ld.work_date between :leftDate and :rightDate"
                + " group by ld.osp_id";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("leftDate", startDate.toDate());
        params.put("rightDate", endDate.toDate());


        List<Map<String, Map<String, BigDecimal>>> results = namedJdbcTemplate.query(query, params,
                new RowMapper<Map<String, Map<String, BigDecimal>>>() {
            @Override
            public Map<String, Map<String, BigDecimal>> mapRow(ResultSet rs, int i) throws SQLException {
                HashMap<String, Map<String, BigDecimal>> mapRet = new HashMap<String, Map<String, BigDecimal>>();
                String name = rs.getString("name");
                if (name == null) {
                    name = "Uncategorized";
                }
                Map<String, BigDecimal> laborMap = new HashMap<String, BigDecimal>();
                laborMap.put("labor_total", rs.getBigDecimal("labor_total"));
                laborMap.put("addl_labor_total", rs.getBigDecimal("addl_labor_total"));
                mapRet.put(name, laborMap);
                return mapRet;
            }
        });

        return results;
    }

    @Override
    public List<Map<String, Map<String, BigDecimal>>> laborWithoutServiceByCustomerForDates(DateTime startDate, DateTime endDate) {
        if (startDate == null || endDate == null) { // don't mess with one of these being null... override
            startDate = new DateTime();
            endDate = new DateTime();
        }

        startDate = startDate
                .withZone(DateTimeZone.forID(TZID))
                .withDayOfMonth(1)
                .withTimeAtStartOfDay();
        endDate = endDate
                .withZone(DateTimeZone.forID(TZID))
                .dayOfMonth().withMaximumValue()
                .withTimeAtStartOfDay().plusHours(23).plusMinutes(59).plusSeconds(59);

        String query = "select ld.customer_name customer_name, sum(ld.labor_total) labor_total, sum(ld.addl_labor_total) addl_labor_total"
                + " from grouped_labor_data ld"
                + " where ld.record_type = 'derived'"
                + " and ld.work_date between :leftDate and :rightDate"
                + " group by ld.customer_name";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("leftDate", startDate.toDate());
        params.put("rightDate", endDate.toDate());


        List<Map<String, Map<String, BigDecimal>>> results = namedJdbcTemplate.query(query, params,
                new RowMapper<Map<String, Map<String, BigDecimal>>>() {
            @Override
            public Map<String, Map<String, BigDecimal>> mapRow(ResultSet rs, int i) throws SQLException {
                HashMap<String, Map<String, BigDecimal>> mapRet = new HashMap<String, Map<String, BigDecimal>>();
                String name = rs.getString("customer_name");
                if (name == null) {
                    name = "No Customer";
                }
                Map<String, BigDecimal> laborMap = new HashMap<String, BigDecimal>();
                laborMap.put("labor_total", rs.getBigDecimal("labor_total"));
                laborMap.put("addl_labor_total", rs.getBigDecimal("addl_labor_total"));
                mapRet.put(name, laborMap);
                return mapRet;
            }
        });

        return results;
    }

    @Override
    public List<Map<String, Map<String, BigDecimal>>> laborWithoutServiceByChronosTaskForDates(DateTime startDate, DateTime endDate) {
        if (startDate == null || endDate == null) { // don't mess with one of these being null... override
            startDate = new DateTime();
            endDate = new DateTime();
        }

        startDate = startDate
                .withZone(DateTimeZone.forID(TZID))
                .withDayOfMonth(1)
                .withTimeAtStartOfDay();
        endDate = endDate
                .withZone(DateTimeZone.forID(TZID))
                .dayOfMonth().withMaximumValue()
                .withTimeAtStartOfDay().plusHours(23).plusMinutes(59).plusSeconds(59);

        String query = "select ld.task_description task, sum(ld.labor_total) labor_total, sum(ld.addl_labor_total) addl_labor_total"
                + " from grouped_labor_data ld"
                + " where ld.record_type = 'indirect'"
                + " and ld.work_date between :leftDate and :rightDate"
                + " group by ld.task_description";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("leftDate", startDate.toDate());
        params.put("rightDate", endDate.toDate());

        List<Map<String, Map<String, BigDecimal>>> results = namedJdbcTemplate.query(query, params,
                new RowMapper<Map<String, Map<String, BigDecimal>>>() {
            @Override
            public Map<String, Map<String, BigDecimal>> mapRow(ResultSet rs, int i) throws SQLException {
                HashMap<String, Map<String, BigDecimal>> mapRet = new HashMap<String, Map<String, BigDecimal>>();
                String name = rs.getString("task");
                if (name == null) {
                    name = "No Task";
                }
                Map<String, BigDecimal> laborMap = new HashMap<String, BigDecimal>();
                laborMap.put("labor_total", rs.getBigDecimal("labor_total"));
                laborMap.put("addl_labor_total", rs.getBigDecimal("addl_labor_total"));
                mapRet.put(name, laborMap);
                return mapRet;
            }
        });

        return results;
    }
    
    @Override
    public List<LaborBreakdownRecord> laborBreakdownForMonth(DateTime month) {
        if (month == null) {
            throw new IllegalArgumentException("month are required fields for labor cost search");
        }
        DateTime startDate = month
                .withDayOfMonth(1)
                .withTimeAtStartOfDay()
                .withZone(DateTimeZone.forID(TZID));
        DateTime endDate = startDate.dayOfMonth().withMaximumValue().plusHours(23).plusMinutes(59).plusSeconds(59);
        
        String query = "select tier_code, tier_rate, sum(hours*tier_rate) as tier_total,"
                + " addl_tier_rate, sum(hours*addl_tier_rate) as addl_tier_total"
                + " from raw_labor_data"
                + " where tier_rate > 0."
                + " and work_date between :leftDate and :rightDate"
                + " and record_type is not null"
                + " group by tier_code, tier_rate, addl_tier_rate"
                + " order by tier_code";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("leftDate", startDate.toDate());
        params.put("rightDate", endDate.toDate());
        
        List<LaborBreakdownRecord> results = namedJdbcTemplate.query(query, params,
                new RowMapper<LaborBreakdownRecord>() {
            @Override
            public LaborBreakdownRecord mapRow(ResultSet rs, int i) throws SQLException {
                return new LaborBreakdownRecord(
            		rs.getString("tier_code"), // eh, just copy code over name, which we're not using
                        rs.getString("tier_code"),
            		rs.getBigDecimal("tier_rate"),
            		rs.getBigDecimal("addl_tier_rate"),
            		rs.getBigDecimal("tier_total"),
            		rs.getBigDecimal("addl_tier_total")
                );
            }
        });

        return results;
    }
    
    @Override
    public ReportWrapper laborHoursReport(Date startDate, Date endDate, Long ospId, Long customerId, Long recordLimit, Boolean includeChildren) throws ServiceException {
    	ReportWrapper wrapper = new ReportWrapper();
    	Long count = 0L;
        DateTime startDateParam = new DateTime(startDate)
                .withDayOfMonth(1)
                .withTimeAtStartOfDay()
                .withZone(DateTimeZone.forID(TZID));
        DateTime endDateParam = new DateTime(endDate).dayOfMonth().withMaximumValue().plusHours(23).plusMinutes(59).plusSeconds(59);
        
        /*
        String query = "select id, osp_id, service_name, customer_id, customer_name, worker, task_description, DATE_FORMAT(work_date, '%Y/%m'), work_date as period, sum(hours) as hours_worked"
                + " from raw_labor_data"
        		+ " where work_date between :leftDate and :rightDate";
        		if(ospId != null) query += " and osp_id = :ospId";
        		if(customerId != null) query += " and customer_id = :customerId";
        		query += " group by DATE_FORMAT(work_date, '%Y/%m'), customer_name, service_name, worker, task_description";
        		query += " order by DATE_FORMAT(work_date, '%Y/%m'), customer_name, service_name, worker, task_description";
        */		
        String query = "select rld.id, rld.osp_id, rld.service_name, rld.customer_id, rld.customer_name, rld.worker, rld.task_description, rld.work_date as period, rld.hours as hours_worked, rld.ticket,"
        		+ " rld.tier_name, rld.tier_code"
                + " from raw_labor_data rld";
        		if(includeChildren != null && includeChildren) {
        			query += " inner join customer cu on cu.id = rld.customer_id";
        		}
        		query += " where rld.work_date between :leftDate and :rightDate";
        		if(ospId != null) query += " and rld.osp_id = :ospId";
        		if(includeChildren != null && includeChildren) {
        			query += " and (rld.customer_id = :customerId or cu.parent_id = :customerId)";
        		} else {
        			if(customerId != null) query += " and rld.customer_id = :customerId";
        		}
        		query += " order by rld.work_date, rld.customer_name, rld.worker, rld.service_name, rld.task_description";
        
		Map<String, Object> params = new HashMap<String, Object>();
        params.put("leftDate", startDateParam.toDate());
        params.put("rightDate", endDateParam.toDate());
        if(ospId != null) params.put("ospId", ospId);
        if(customerId != null) params.put("customerId", customerId);
        		
        List<LaborHoursRecord> results = namedJdbcTemplate.query(query, params,
                new RowMapper<LaborHoursRecord>() {
            @Override
            public LaborHoursRecord mapRow(ResultSet rs, int i) throws SQLException {
                return new LaborHoursRecord(
                	rs.getLong("id"),
            		rs.getLong("osp_id"),
            		rs.getString("service_name"),
            		rs.getLong("customer_id"),
            		rs.getString("customer_name"),
            		rs.getString("worker"),
            		rs.getDate("period"),
            		rs.getBigDecimal("hours_worked"),
            		rs.getString("task_description"),
            		rs.getString("ticket"),
            		rs.getString("tier_name"),
            		rs.getString("tier_code")
                );
            }
        });
        
        count = new Long(results.size());
        wrapper.setResultCount(count);
        
        if((recordLimit != null && recordLimit.compareTo(count) >= 0) || recordLimit == null) {
        	wrapper.setGenericData(results);
        }
        
        //if the results set is larger than 80k, we throw an error for safety
        if(count.compareTo(new Long(80000)) > 0) {
        	throw new ServiceException(messageSource.getMessage("ui_reports_labor_hours_results_too_large_safety", null, LocaleContextHolder.getLocale()));
        }
        
        return wrapper;
    }
    
    @Override
    public List<UnitCostDetails.LaborDetail> laborCostsForExpenseCategory(Long customerId, Integer expenseCategoryId, DateTime costDate) {
        
        DateTime startDate = costDate
                .withZone(DateTimeZone.forID(TZID))
                .withDayOfMonth(1) // default is beginning of year...
                .withTimeAtStartOfDay();
        DateTime endDate = startDate.dayOfMonth().withMaximumValue().plusHours(23).plusMinutes(59).plusSeconds(59);
        
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("expenseCategoryId", expenseCategoryId);
        params.put("startDate", startDate.toDate());
        params.put("endDate", endDate.toDate());
        
        String query = "select worker 'Worker', work_date 'Date', sum(hours) 'Hours',"
                + " sum(hours * tier_rate) + sum(hours * addl_tier_rate) 'Amount' from raw_labor_data"
                + " where work_date between :startDate and :endDate and expense_category_id = :expenseCategoryId"
                + " and record_type = 'categorized'";
        if (customerId != null && customerId > 0) {
            query += " and customer_id = :custId";
            params.put("custId", customerId);
        } else {
            query += " and customer_id is null";
        }
        query += " group by worker, work_date"
                + " order by work_date asc, worker asc";
        return namedJdbcTemplate.query(query, params, new RowMapper<UnitCostDetails.LaborDetail>() {
            @Override
            public UnitCostDetails.LaborDetail mapRow(ResultSet rs, int i) throws SQLException {
                UnitCostDetails.LaborDetail laborDetail = new UnitCostDetails.LaborDetail();
                laborDetail.setWorker(rs.getString("Worker"));
                laborDetail.setWorkDate(new DateTime(rs.getString("Date")));
                laborDetail.setHours(rs.getBigDecimal("Hours"));
                laborDetail.setLabor(rs.getBigDecimal("Amount"));
                return laborDetail;
            }
        });
    }
    
    
    @Override
    @Scheduled(cron = "0 0 7 28 * *")
    public void runGenerateSPLACosts() {
    	DateTime jobDateTime = new DateTime().withZone(DateTimeZone.UTC).withTime(0,0,0,0).dayOfMonth().withMinimumValue().plusDays(1);
    	Integer month = jobDateTime.getMonthOfYear();
    	String year = String.valueOf(jobDateTime.getYear());
    	generateSPLACostsForMonth(month, year);
    }
    
    public void generateSPLACostsForMonth(Integer month, String year) {
    	try {
	    	ScheduledTask st = applicationDataDaoService.findScheduledTaskByCode(SPLA_MONTHLY_COST_GENERATION_TSK);
	        if (st != null && st.getEnabled()) {
	            log.info("Running Task: " + st.getName());
		    	List<Contract> contracts = contractDaoService.contracts(Boolean.FALSE); 
		    	Date startDate = DateTimeFormat.forPattern("yyyy").parseDateTime(year)
		                .withMonthOfYear(month)
		                .withDayOfMonth(1)
		                .withTimeAtStartOfDay()
		                .withZone(DateTimeZone.forID(TZID)).toDate();
		        Date endDate = DateTimeFormat.forPattern("yyyy").parseDateTime(year).withMonthOfYear(month).dayOfMonth().withMaximumValue().plusHours(23).plusMinutes(59).plusSeconds(59).toDate();
		    	
		    	for(Contract contract : contracts) {
                            List<Service> services = contractRevenueService.serviceRevenueRecordsForDateRange(contract.getId(), startDate, endDate, Service.Status.active);
                            List<DeviceSPLACost> deviceSPLACosts = new ArrayList<DeviceSPLACost>();
                            //log.info("Found Services [" + services.size() + "]");
                            for(Service service : services) {
                                Integer serviceCount = service.getQuantity();
                                if(service.getDeviceUnitCount() != null) {
                                    serviceCount = service.getDeviceUnitCount();
                                }
                                try {
                                    Device device = applicationDataDaoService.device(service.getDeviceId());
                                    for(SPLACost deviceSPLA : device.getSplaCosts()) {
                                        DeviceSPLACost entry = new DeviceSPLACost(device.getId(), device.getPartNumber(), deviceSPLA.getId(), deviceSPLA.getExpenseCategoryId());
                                        if (!deviceSPLACosts.contains(entry)) {
                                            entry.setSplaName(deviceSPLA.getName());
                                            entry.addCount(serviceCount);
                                            entry.addCost(deviceSPLA.getCost().multiply(new BigDecimal(serviceCount)));
                                            deviceSPLACosts.add(entry);
                                            continue;
                                        }
                                        for (DeviceSPLACost deviceSPLACost : deviceSPLACosts) {
                                            if (deviceSPLACost.equals(entry)) {
                                                deviceSPLACost.addCost(deviceSPLA.getCost().multiply(new BigDecimal(serviceCount)));
                                                deviceSPLACost.addCount(serviceCount);
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    log.info("Device not found for SPLA Generation with ID [" + service.getDeviceId() + "]");
                                    continue;
                                }	
                            }

                            Customer customer = null;
                            try {
                                customer = contractDaoService.customer(contract.getCustomerId());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            //create the cost records
                            for (DeviceSPLACost deviceSPLACost : deviceSPLACosts) {
                                try {
                                    String expenseRecordName = "Automated SPLA Cost: " + deviceSPLACost.getSplaName();
                                    String expenseRecordDescription = "for Customer: " + customer.getName();
                                    String costItemDescription = "SPLA Cost for " + deviceSPLACost.getCount() + " " + deviceSPLACost.getSplaName() + " Licenses.";
                                    Expense expenseRecord = new Expense(null, null, Expense.ExpenseType.cost, expenseRecordName, null, deviceSPLACost.getCost(), 1, customer.getId(), contract.getId(), null);
                                    expenseRecord.setDescription(expenseRecordDescription);
                                    expenseRecord.setCreatedBy("system");
                                    CostItem splaCostItem = new CostItem();
                                    splaCostItem.setExpense(expenseRecord);
                                    splaCostItem.setAmount(deviceSPLACost.getCost());
                                    splaCostItem.setQuantity(deviceSPLACost.getCount());
                                    splaCostItem.setApplied(startDate);
                                    splaCostItem.setContractId(contract.getId());
                                    splaCostItem.setCustomerId(customer.getId());
                                    splaCostItem.setCreated(new Date());
                                    splaCostItem.setName(expenseRecordName);
                                    splaCostItem.setDescription(costItemDescription);
                                    splaCostItem.setCostType(CostItem.CostType.spla);
                                    splaCostItem.setSplaId(deviceSPLACost.getSplaId());
                                    splaCostItem.setDeviceId(deviceSPLACost.getDeviceId());
                                    splaCostItem.setPartNumber(deviceSPLACost.getPartNumber());
                                    CostFraction awsCostFraction = new CostFraction();
                                    ExpenseCategory expenseCategory = contractDaoService.expenseCategory(deviceSPLACost.getExpenseCategoryId());
                                    awsCostFraction.setExpenseCategory(expenseCategory);
                                    awsCostFraction.setFraction(new BigDecimal(100));
                                    splaCostItem.addCostFraction(awsCostFraction);
                                    splaCostItem.setCreatedBy("system");
                                    Long costItemId = contractDaoService.saveCostItem(splaCostItem);
                                } catch (Exception e) {
                                        e.printStackTrace();
                                }
                            }
		    	}
		    	log.info("Ending Task: " + st.getName());
	        } else {
	        	log.info("SPLA Cost Generation Task is not enabled.");
	        }
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }

    @Override
    public List<CostItemTypeSubType> refCostItemTypeSubTypes() {
        List<CostItemTypeSubType> costItemTypeSubTypes = new ArrayList<CostItemTypeSubType>();
        List<Map<String, Object>> results = jdbcTemplate.queryForList("select distinct cost_type, cost_subtype from cost_item order by 1, 2");
        for (Map<String, Object> result : results) {
            String typeString = (String) result.get("cost_type"); // better not be null...
            CostItem.CostType typeEnum = convertCostItemCostType(typeString);
            String subtypeString = (String) result.get("cost_subtype");
            if (subtypeString != null) {
                CostItem.CostSubType subtypeEnum = convertCostItemCostSubType(subtypeString);
                costItemTypeSubTypes.add(new CostItemTypeSubType(
                        typeString,
                        (typeEnum != null ? typeEnum.getDescription() : typeString),
                        subtypeString,
                        (subtypeEnum != null ? subtypeEnum.getDescription() : subtypeString)
                ));
            } else {
                costItemTypeSubTypes.add(new CostItemTypeSubType(
                        typeString,
                        (typeEnum != null ? typeEnum.getDescription() : typeString),
                        subtypeString,
                        null
                ));
            }
        }
        return costItemTypeSubTypes;
    }
    
    @Override
    public CostItemAnalysisWrapper costItemAnalysis(DateTime costDate) throws ServiceException {
        
        CostItemAnalysisWrapper wrapper = new CostItemAnalysisWrapper();
        wrapper.setMonth(costDate);
        
        Map<String, Object> params = new HashMap<String, Object>();
        DateTime startDate = costDate
                .withDayOfMonth(1)
                .withTimeAtStartOfDay()
                .withZone(DateTimeZone.forID(TZID));
        params.put("startDate", startDate.toDate());
        DateTime endDate = startDate.dayOfMonth().withMaximumValue().plusHours(23).plusMinutes(59).plusSeconds(59);
        params.put("endDate", endDate.toDate());
        
        /**
         * returns a summary by CostItem CostType / CostSubType:
         * Cost | Cost Type | Cost Subtype
         */
        String query = "select sum(ci.amount * cif.cost_fraction / 100) as 'Cost', ci.cost_type as 'Cost Type', ci.cost_subtype as 'Cost SubType'"
                + " from cost_item ci"
                + " inner join cost_item_fraction cif on ci.id = cif.cost_item_id"
                + " where ci.applied between :startDate and :endDate"
                + " group by ci.cost_type, ci.cost_subtype order by ci.cost_type, ci.cost_subtype";
        
        List<CostItemAnalysisWrapper.SummaryRecord> costsByTypeSubType = namedJdbcTemplate.query(query, params, new RowMapper<CostItemAnalysisWrapper.SummaryRecord>(){
            @Override
            public CostItemAnalysisWrapper.SummaryRecord mapRow(ResultSet rs, int i) throws SQLException {
                return new CostItemAnalysisWrapper.SummaryRecord(
                        rs.getBigDecimal("Cost"),
                        rs.getString("Cost Type"),
                        rs.getString("Cost SubType"));
            }
        });
        wrapper.setCostsByTypeSubType(costsByTypeSubType);
        
        /**
         * returns cost details by CostItem CostType / CostSubType AND Customer
         * Customer | Cost | Cost Type | Cost Subtype
         * * excludes null Customer costs, which are ADDED later, spread across Customers by cost category contract service counts
         */
        query = "select cu.id as 'Customer Id', cu.name as 'Customer', sum(ci.amount * cif.cost_fraction / 100) as 'Cost', ci.cost_type as 'Cost Type', ci.cost_subtype as 'Cost Subtype'"
                + " from cost_item ci"
                + " inner join cost_item_fraction cif on ci.id = cif.cost_item_id"
                + " inner join customer cu on ci.customer_id = cu.id"
                + " where ci.applied between :startDate and :endDate"
                + " group by cu.id, cu.name, ci.cost_type, ci.cost_subtype"
                + " order by ci.cost_type, ci.cost_subtype, cu.name";
        
        List<CostItemAnalysisWrapper.SummaryRecordByCustomer> costsByTypeSubTypeAndCustomer = namedJdbcTemplate.query(query, params, new RowMapper<CostItemAnalysisWrapper.SummaryRecordByCustomer>(){
            @Override
            public CostItemAnalysisWrapper.SummaryRecordByCustomer mapRow(ResultSet rs, int i) throws SQLException {
                return new CostItemAnalysisWrapper.SummaryRecordByCustomer(
                        rs.getLong("Customer Id"),
                        rs.getString("Customer"),
                        rs.getBigDecimal("Cost"),
                        rs.getString("Cost Type"),
                        rs.getString("Cost SubType"));
            }
        });
        wrapper.setCostsByTypeSubTypeAndCustomer(costsByTypeSubTypeAndCustomer);
        
        /**
         * returns cost details by CostItem CostType / CostSubType AND Parent+Cost Category
         * Parent Cost Category | Cost Category | Cost | Cost Type | Cost Subtype
         */
        query = "select pexp.name as 'Parent Cost Category', exp.name as 'Cost Category',"
                + " exp.id as 'Cost Category Id', sum(ci.amount * cif.cost_fraction / 100) as 'Cost',"
                + " ci.cost_type as 'Cost Type', ci.cost_subtype as 'Cost Subtype'"
                + " from cost_item ci"
                + " inner join cost_item_fraction cif on ci.id = cif.cost_item_id"
                + " inner join expense_category exp on exp.id = cif.expense_category_id"
                + " left outer join expense_category pexp on exp.parent_id = pexp.id"
                + " where ci.applied between :startDate and :endDate"
                + " group by pexp.name, exp.name, exp.id, ci.cost_type, ci.cost_subtype"
                + " order by ci.cost_type, ci.cost_subtype, pexp.name, exp.name";
        
        List<CostItemAnalysisWrapper.SummaryRecordByCostCategory> costsByTypeSubTypeAndCostCategory = namedJdbcTemplate.query(query, params, new RowMapper<CostItemAnalysisWrapper.SummaryRecordByCostCategory>(){
            @Override
            public CostItemAnalysisWrapper.SummaryRecordByCostCategory mapRow(ResultSet rs, int i) throws SQLException {
                return new CostItemAnalysisWrapper.SummaryRecordByCostCategory(
                        rs.getString("Parent Cost Category"),
                        rs.getString("Cost Category"),
                        rs.getLong("Cost Category Id"),
                        rs.getBigDecimal("Cost"),
                        rs.getString("Cost Type"),
                        rs.getString("Cost SubType"));
            }
        });
        wrapper.setCostsByTypeSubTypeAndCostCategory(costsByTypeSubTypeAndCostCategory);
        
        /**
         * We need to go through a couple step process to find cost type/subtype costs by category NOT assigned to any Customers
         * and then spread those amounts across Customers in the cost type/subtype (CostItemAnalysisWrapper.SummaryRecordByCustomer) list.
         */
        query = "select ci.cost_type as 'Cost Type', ci.cost_subtype as 'Cost Subtype', pexp.name as 'Parent Cost Category', exp.name as 'Cost Category',"
                + " exp.id as 'Cost Category Id', sum(ci.amount * cif.cost_fraction / 100) as 'Cost'"
                + " from cost_item ci"
                + " inner join cost_item_fraction cif on ci.id = cif.cost_item_id"
                + " inner join expense_category exp on exp.id = cif.expense_category_id"
                + " left outer join expense_category pexp on exp.parent_id = pexp.id"
                + " where ci.applied between :startDate and :endDate"
                + " and ci.customer_id is null"
                + " group by ci.cost_type, ci.cost_subtype, pexp.name, exp.name, exp.id"
                + " order by exp.id, ci.cost_type, ci.cost_subtype";
        
        List<CostItemAnalysisWrapper.SummaryRecordByCostCategory> nonCustomerCostsByTypeSubTypeAndCostCategory = namedJdbcTemplate.query(query, params, new RowMapper<CostItemAnalysisWrapper.SummaryRecordByCostCategory>(){
            @Override
            public CostItemAnalysisWrapper.SummaryRecordByCostCategory mapRow(ResultSet rs, int i) throws SQLException {
                return new CostItemAnalysisWrapper.SummaryRecordByCostCategory(
                        rs.getString("Parent Cost Category"),
                        rs.getString("Cost Category"),
                        rs.getLong("Cost Category Id"),
                        rs.getBigDecimal("Cost"),
                        rs.getString("Cost Type"),
                        rs.getString("Cost SubType"));
            }
        });
        
        String[] deviceCountQuery = {"select", " cst.id, cst.name,", " sum(case when (csd.unit_count = 0 or csd.unit_count is null) then csvc.quantity"
                + " else csvc.quantity * csd.unit_count end) as 'unit_count'"
                + " from contract_service csvc"
                + " inner join contract_service_device csd on csd.contract_service_id = csvc.id"
                + " inner join device dev on csd.device_id = dev.id"
                + " inner join contract ctr on ctr.id = csvc.contract_id"
                + " inner join customer cst on cst.id = ctr.customer_id"
                + " inner join device_expense_category devcat on csd.device_id = devcat.device_id"
                + " where (csvc.start_date <= :startDate or csvc.start_date between :startDate and :endDate)"
                + " and (csvc.end_date is null or csvc.end_date between :startDate and :endDate or csvc.end_date >= :endDate)"
                + " and (csvc.status = 'active' or csvc.status = 'donotbill')"
                + " and (dev.device_type is null or dev.device_type not in ('businessService'))"
                + " and devcat.expense_category_id = :costCatId", " group by cst.id, cst.name"
                + " order by cst.name"};
        Long costCatId = -1L;
        List<Map<String, Object>> customerDeviceCounts = new ArrayList<Map<String, Object>>();
        List<CostItemAnalysisWrapper.SummaryRecordByCustomer> costsByTypeSubTypeAndNoCustomer = new ArrayList<CostItemAnalysisWrapper.SummaryRecordByCustomer>();
        for (CostItemAnalysisWrapper.SummaryRecordByCostCategory cost_record : nonCustomerCostsByTypeSubTypeAndCostCategory) {
            if (!costCatId.equals(cost_record.getCostCategoryId())) {
                costCatId = cost_record.getCostCategoryId();
                params.put("costCatId", costCatId);
                Integer deviceTotalCount = namedJdbcTemplate.queryForObject(deviceCountQuery[0] + deviceCountQuery[2], params, Integer.class);
                customerDeviceCounts = namedJdbcTemplate.queryForList(deviceCountQuery[0] + deviceCountQuery[1] + deviceCountQuery[2] + deviceCountQuery[3], params);
                for (Map<String, Object> cust_record : customerDeviceCounts) {
                    int unit_count = ((BigDecimal) cust_record.get("unit_count")).intValue();
                    double fraction_calc = unit_count / (double) deviceTotalCount;
                    cust_record.put("fraction", fraction_calc);
                }
            }
            if (customerDeviceCounts == null || customerDeviceCounts.isEmpty()) {
                CostItemAnalysisWrapper.SummaryRecordByCustomer nonCustomerCost = new CostItemAnalysisWrapper.SummaryRecordByCustomer(
                            0L, // SQL query returning null results in 0...
                            null,
                            cost_record.getCost(),
                            cost_record.getCostType(),
                            cost_record.getCostSubType());
                int index = costsByTypeSubTypeAndNoCustomer.indexOf(nonCustomerCost);
                if (index > -1) {
                    nonCustomerCost = costsByTypeSubTypeAndNoCustomer.get(index);
                    nonCustomerCost.setCost(nonCustomerCost.getCost().add(cost_record.getCost())); // incrementing the non-spread, null customer amount
                } else {
                    costsByTypeSubTypeAndNoCustomer.add(nonCustomerCost);
                }
            } else {
                for (Map<String, Object> cust_record : customerDeviceCounts) {
                    BigDecimal fraction_amount = new BigDecimal((double) cust_record.get("fraction")).multiply(cost_record.getCost());
                    CostItemAnalysisWrapper.SummaryRecordByCustomer incr_customer_cost = new CostItemAnalysisWrapper.SummaryRecordByCustomer(
                                (Long) cust_record.get("id"),
                                (String) cust_record.get("name"),
                                fraction_amount,
                                cost_record.getCostType(),
                                cost_record.getCostSubType());
                    int index = costsByTypeSubTypeAndCustomer.indexOf(incr_customer_cost);
                    if (index > -1) {
                        incr_customer_cost = costsByTypeSubTypeAndCustomer.get(index);
                        incr_customer_cost.setCost(incr_customer_cost.getCost().add(fraction_amount));
                    } else {
                        costsByTypeSubTypeAndCustomer.add(incr_customer_cost);
                    }
                }
            }
        }
        costsByTypeSubTypeAndCustomer.addAll(costsByTypeSubTypeAndNoCustomer);
        Collections.sort(costsByTypeSubTypeAndCustomer);
        
        /**
         * get the summarized labor totals
         */
        query = "select sum(ld.labor_total) as 'Labor Total', sum(ld.addl_labor_total) as 'Additional Labor Total',"
                + " case when ld.record_type = 'derived' then 'direct' else ld.record_type end as 'Record Type'"
                + " from grouped_labor_data ld"
                + " where ld.work_date between :startDate and :endDate"
                + " and ld.record_type in ('direct', 'derived', 'indirect')"
                + " group by 3 order by 3";
        List<Map<String, Object>> results = namedJdbcTemplate.queryForList(query, params);
        for (Map<String, Object> record : results) {
            String recordType = (String) record.get("Record Type");
            BigDecimal laborTotal = (BigDecimal) record.get("Labor Total");
            BigDecimal additionalLaborTotal = (BigDecimal) record.get("Additional Labor Total");
            wrapper.getCostsByTypeSubType().add(new CostItemAnalysisWrapper.SummaryRecord(laborTotal.add(additionalLaborTotal), recordType, null));
        }
        return wrapper;
    }
    
    private CostItem.CostType convertCostItemCostType(String name) {
        if (name == null) {
            return null;
        }
        try {
            return CostItem.CostType.valueOf(name);
        } catch (IllegalArgumentException iae) {
            log.info("No ENUM CostItem.CostType found for name {}", new Object[]{name});
            return null;
        }
    }
    
    private CostItem.CostSubType convertCostItemCostSubType(String name) {
        if (name == null) {
            return null;
        }
        try {
            return CostItem.CostSubType.valueOf(name);
        } catch (IllegalArgumentException iae) {
            log.info("No ENUM CostItem.CostSubType found for name {}", new Object[]{name});
            return null;
        }
    }
    
    static class DeviceSPLACost {

        private Long deviceId;
        private String partNumber;
        private Long splaId;
        private Integer expenseCategoryId;
        private String splaName;
        private Integer count = 0;
        private BigDecimal cost = BigDecimal.ZERO;

        /**
         * default CTOR
         */
        DeviceSPLACost() {
        }

        DeviceSPLACost(Long deviceId, String partNumber, Long splaId, Integer expenseCategoryId) {
            this.deviceId = deviceId;
            this.partNumber = partNumber;
            this.splaId = splaId;
            this.expenseCategoryId = expenseCategoryId;
        }
        
        public Long getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(Long deviceId) {
            this.deviceId = deviceId;
        }

        public String getPartNumber() {
            return partNumber;
        }

        public void setPartNumber(String partNumber) {
            this.partNumber = partNumber;
        }

        public Long getSplaId() {
            return splaId;
        }

        public void setSplaId(Long splaId) {
            this.splaId = splaId;
        }

        public Integer getExpenseCategoryId() {
            return expenseCategoryId;
        }

        public void setExpenseCategoryId(Integer expenseCategoryId) {
            this.expenseCategoryId = expenseCategoryId;
        }

        public String getSplaName() {
            return splaName;
        }

        public void setSplaName(String splaName) {
            this.splaName = splaName;
        }

        public Integer getCount() {
            return count;
        }
        
        public void addCount(Integer addCount) {
            if (count == null) {
                count = 0;
            }
            count = count + addCount;
        }

        public BigDecimal getCost() {
            return cost;
        }
        
        public void addCost(BigDecimal addCost) {
            if (cost == null) {
                cost = BigDecimal.ZERO;
            }
            cost = cost.add(addCost);
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 53 * hash + Objects.hashCode(this.deviceId);
            hash = 53 * hash + Objects.hashCode(this.splaId);
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
            final DeviceSPLACost other = (DeviceSPLACost) obj;
            if (!Objects.equals(this.deviceId, other.deviceId)) {
                return false;
            }
            if (!Objects.equals(this.splaId, other.splaId)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "DeviceSPLACost{" + "deviceId=" + deviceId + ", partNumber=" + partNumber + ", splaId=" + splaId + ", expenseCategoryId=" + expenseCategoryId + ", splaName=" + splaName + ", count=" + count + ", cost=" + cost + '}';
        }
    }
    
    @Override
    @Scheduled(cron = "0 2 1 * * *") //1:02am
    public void runGenerateDeviceCounts() {
    	DateTime jobDateTime = new DateTime().withZone(DateTimeZone.UTC).withTime(0,0,0,0).dayOfMonth().withMinimumValue().plusDays(1);
    	Integer month = jobDateTime.getMonthOfYear();
    	String year = String.valueOf(jobDateTime.getYear());
    	
    	try {
    		generateDeviceCounts(month, year);
    	} catch (Exception e) {
    		log.error(e.getMessage());
    	}
    }
    
    @Async
    @Override
    public void runGenerateAllDeviceCounts() {
    	DateTime startDate = DateTimeFormat.forPattern("yyyy").parseDateTime("2017")
                .withMonthOfYear(1)
                .withDayOfMonth(1)
                .withTimeAtStartOfDay()
                .withZone(DateTimeZone.forID(TZID));
    	DateTime endDate = new DateTime().withDayOfMonth(1).withTimeAtStartOfDay().withZone(DateTimeZone.forID(TZID));
    	
    	while(!startDate.isAfter(endDate)) {
    		DateTime jobDateTime = startDate.withZone(DateTimeZone.UTC).withTime(0,0,0,0).dayOfMonth().withMinimumValue().plusDays(1);
	    	Integer month = jobDateTime.getMonthOfYear();
	    	String year = String.valueOf(jobDateTime.getYear());
	    	
	    	try {
	    		log.info("Running Unit Count for: " + jobDateTime);
	    		
	    		generateDeviceCounts(month, year);
	    	} catch (Exception e) {
	    		log.error(e.getMessage());
	    	}
	    	startDate = startDate.plusMonths(1);
    	}
    }
    
    @Override
    public void generateDeviceCounts(Integer month, String year) throws ServiceException {
    	try {
            ScheduledTask st = applicationDataDaoService.findScheduledTaskByCode(DEVICE_UNIT_COUNT_SYNC);
            if (st != null && st.getEnabled()) {
                log.info("Running Task: " + st.getName());
                List<Device> devices = applicationDataDaoService.devices(Boolean.FALSE);
            	
            	DateTime startDate = DateTimeFormat.forPattern("yyyy").parseDateTime(year)
                        .withMonthOfYear(month)
                        .withDayOfMonth(1)
                        .withTimeAtStartOfDay()
                        .withZone(DateTimeZone.forID(TZID));
                DateTime endDate = DateTimeFormat.forPattern("yyyy").parseDateTime(year).withMonthOfYear(month).dayOfMonth().withMaximumValue().plusHours(23).plusMinutes(59).plusSeconds(59);
            	
            	if(month == null || year == null) {
            		startDate = new DateTime().withDayOfMonth(1).withTimeAtStartOfDay().withZone(DateTimeZone.forID(TZID));
                	endDate = startDate.dayOfMonth().withMaximumValue().plusHours(23).plusMinutes(59).plusSeconds(59);
            	}
            	
            	try {
                    int updated = jdbcTemplate.update("delete from device_unit_count where month = ?", startDate.toDate());
                    log.info("Deleted [{}] device unit counts for Month [{}]", new Object[]{startDate.toDate()});
                } catch (Exception any) {
                    throw new ServiceException(messageSource.getMessage("jdbc_error_device_delete", new Object[]{startDate.toDate(), any.getMessage()}, LocaleContextHolder.getLocale()), any);
                }
            	
            	for(Device device: devices) {
            		try {
            			Long deviceId = device.getId();
            			Integer deviceCount = deviceTotalDeviceCount(startDate, endDate, null, null, null, deviceId);
            			
            			saveDeviceUnitCount(deviceId, deviceCount, startDate.toDate());
            		} catch(Exception e) {
            			log.error(e.getMessage());
            		}
            	}
                log.info("Ending Task: " + st.getName());
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
    
    private void saveDeviceUnitCount(Long deviceId, Integer count, Date month) throws ServiceException {
    	try {
            SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
            jdbcInsert.withTableName("device_unit_count").usingGeneratedKeyColumns("id");
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("device_id", deviceId);
            params.put("unit_count", count);
            params.put("month", month);
            params.put("created", new DateTime().withZone(DateTimeZone.forID(TZID)).toDate());
            params.put("created_by", authenticatedUser());
            Number pk = jdbcInsert.executeAndReturnKey(new MapSqlParameterSource(params));
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_contract_invoice_insert", new Object[]{any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    }
    
}