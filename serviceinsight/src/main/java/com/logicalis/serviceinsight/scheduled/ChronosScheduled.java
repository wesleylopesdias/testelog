package com.logicalis.serviceinsight.scheduled;

import com.logicalis.serviceinsight.dao.ExpenseCategory;
import com.logicalis.serviceinsight.dao.UnitCost;
import com.logicalis.serviceinsight.data.ScheduledTask;
import com.logicalis.serviceinsight.data.Service;
import com.logicalis.serviceinsight.service.ApplicationDataDaoService;
import com.logicalis.serviceinsight.service.CostDaoService;
import com.logicalis.serviceinsight.service.CostService;
import com.logicalis.serviceinsight.service.ServiceException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import javax.sql.DataSource;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.math3.fraction.Fraction;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Pulls labor data into the database labor_data table, adding the current labor
 * rate required for labor cost calculations from the labor_rate table.
 * 
 * After the import, a log record is left in labor_import_log indicating the time
 * of the import and the number of records read. The latest labor_import_log record
 * is used to determine from which time to read from the Chronos export table.
 * 
 * @author poneil
 */
@Component
public class ChronosScheduled {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    private ApplicationDataDaoService applicationDataDaoService;
    private CostService costService;
    private CostDaoService costDaoService;
    private JdbcTemplate chronosJdbcTemplate;
    private JdbcTemplate jdbcTemplate;
    private NamedParameterJdbcTemplate namedJdbcTemplate;
    @Value("${application.timezone}")
    protected String TZID;

    @Autowired
    void setApplicationDataDaoService(ApplicationDataDaoService applicationDataDaoService) {
        this.applicationDataDaoService = applicationDataDaoService;
    }

    @Autowired
    void setCostService(CostService costService) {
        this.costService = costService;
    }

    @Autowired
    void setCostDaoService(CostDaoService costDaoService) {
        this.costDaoService = costDaoService;
    }

    @Autowired
    void setChronosDataSource(DataSource chronosDataSource, DataSource dataSource) {
        this.chronosJdbcTemplate = new JdbcTemplate(chronosDataSource);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.namedJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }
    
    @Async
    @Scheduled(cron = "0 45 1 1 * ?") // 1:45am every day 1 of the month...
    public void applyUnitCostsForCategorizedLabor() {
        applyUnitCostsForCategorizedLabor(null);
    }
    
    public void applyUnitCostsForCategorizedLabor(DateTime startDate) {

        if (!taskEnabled("labor_unit_cost")) {
            log.info("Chronos Labor Unit Cost calc. triggered but is not enabled...");
            return;
        }
        if (startDate == null) {
            startDate = new DateTime()
                    .withZone(DateTimeZone.forID(TZID))
                    .withDayOfMonth(1)
                    .minusMonths(1)
                    .withTimeAtStartOfDay();
        }
        DateTime endDate = startDate.plusMonths(1);
        log.info("Applying categorized labor totals to Cost Category Unit Costs for month: {}", DateTimeFormat.forPattern("yyyy-MM-dd").print(startDate));
        String query = "select gd.customer_id as 'CustomerId', ec.id as 'ExId', sum(gd.labor_total) + sum(gd.addl_labor_total) as 'Labor'"
                + " from grouped_labor_data gd"
                + " inner join expense_category ec on ec.id = gd.expense_category_id"
                + " where gd.work_date between :startDate and :endDate and gd.record_type = 'categorized'"
                + " group by ec.id";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("startDate", startDate.toDate());
        params.put("endDate", endDate.toDate());
        List<Map<String, Object>> results = namedJdbcTemplate.queryForList(query, params);
        if (results == null || results.size() == 0) {
            log.info("no categorized labor records to apply to unit costs...");
        }
        for (Map<String, Object> result : results) {
            Long customerId = (Long) result.get("CustomerId");
            Integer expenseCategoryId = (Integer) result.get("ExId");
            BigDecimal amount = (BigDecimal) result.get("Labor");
            UnitCost unitCost = costDaoService.unitCostByExpenseCategoryAndDate((new Long(0).equals(customerId) ? null : customerId), expenseCategoryId, startDate);
            if (unitCost != null) {
                unitCost.setTotalLabor(amount);
                try {
                    costDaoService.updateUnitCost(unitCost);
                } catch (ServiceException ex) {
                    java.util.logging.Logger.getLogger(ChronosScheduled.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                unitCost = new UnitCost();
                unitCost.setCustomerId(customerId);
                unitCost.setAppliedDate(startDate);
                unitCost.setExpenseCategoryId(expenseCategoryId);
                unitCost.setTotalLabor(amount);
                try {
                    Integer deviceTotalCount = costService.deviceTotalDeviceCountWithExpenseCategory(
                            startDate, startDate.plusMonths(1).minusDays(1).withTime(23, 59, 59, 999),
                            null, customerId, null, expenseCategoryId, null);
                    unitCost.setDeviceTotalUnits(deviceTotalCount);
                    costDaoService.saveUnitCost(unitCost);
                } catch (ServiceException ex) {
                    java.util.logging.Logger.getLogger(ChronosScheduled.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    @Async
    @Scheduled(cron = "0 0 0 * * SAT") // SAT night's alright for importing. get a little Chronos in!
    // make a web call to /admin/importChronosData to kick off manually
    // Chronos makes the data available to Service Insight at 6am
    public void importChronosData() {

        if (!taskEnabled("chronos_sync")) {
            log.info("Chronos Labor data Sync triggered but is not enabled...");
            return;
        }

        Boolean fullimport = Boolean.FALSE;
        String query = "select * from Export_Table where Inserted >= ?";
        Long lastRawDataEntry = jdbcTemplate.queryForObject("select max(id) from raw_labor_data", Long.class);
        Date lastImportedDate = jdbcTemplate.queryForObject("select max(inserted_date) from labor_import_log", Date.class);
        if (lastImportedDate == null) {
            fullimport = Boolean.TRUE;
            query = "select * from Export_Table where Date >= ? and Date < ?";
            lastImportedDate = chronosJdbcTemplate.queryForObject("select min(Date) from Export_Table", Date.class);
        }
        DateTime lastImportedDateTime = new DateTime(lastImportedDate).withTimeAtStartOfDay();
        Object[] dateRangeArray = (fullimport ? new Object[]{lastImportedDateTime.toDate(), lastImportedDateTime.plusMonths(1).toDate()}
                : new Object[]{lastImportedDateTime.toDate()});
        List<ChronosRawData> data = chronosJdbcTemplate.query(query, dateRangeArray, new RowMapper<ChronosRawData>() {
            @Override
            public ChronosRawData mapRow(ResultSet rs, int i) throws SQLException {
                return new ChronosRawData(
                        rs.getInt("UID"),
                        rs.getString("Ticket"),
                        rs.getTimestamp("Date"),
                        rs.getString("Customer_ID"),
                        rs.getString("Customer"),
                        rs.getString("CI_ID"),
                        rs.getString("CI_Name"),
                        rs.getInt("Num_CIs"),
                        rs.getString("Service_Name"),
                        rs.getString("Task_Description"),
                        rs.getString("Subtask_Description"),
                        rs.getBigDecimal("Hours"),
                        rs.getString("Name"),
                        rs.getString("Team"),
                        rs.getString("Payrollcode"),
                        rs.getString("Labor_Type"),
                        rs.getTimestamp("Inserted"));
            }
        });
        DateTime importDateToLog = new DateTime().withZone(DateTimeZone.forID(TZID));
        int rawDataCount = 0;
        int unaccounted = 0;
        int norate = 0;
        BigDecimal derivedUnaccounted = new BigDecimal(0);
        while (!data.isEmpty()) {
            log.info("**** importing for records inserted between {} and {} ****", new Object[]{
                DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss").print(lastImportedDateTime),
                (fullimport ? DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss").print(lastImportedDateTime.plusMonths(1)) : "NOW")
            });
            Collections.sort(data);
            Map<String, BigDecimal> tierRates = new HashMap<String, BigDecimal>();
            Map<String, BigDecimal> tierAddlRates = new HashMap<String, BigDecimal>();
            Map<String, Service> services = new HashMap<String, Service>();
            Map<String, Long> customerIds = new HashMap<String, Long>();
            Map<ChronosTaskMapping, Integer> expenseCategoryIds = new HashMap<ChronosTaskMapping, Integer>();
            Map<String, String> rulesMap = mapTaskDescriptionRules();
            log.debug("completing Chronos raw data information for [{}] records...", data.size());
            for (ChronosRawData record : data) {
                BigDecimal recordRate = null;
                BigDecimal recordAddlRate = null;
                String tierKey = record.getTierCode();
                if (tierKey != null && !tierRates.containsKey(tierKey)) {
                    try {
                        recordRate = jdbcTemplate.queryForObject("select rate * rate_factor from labor_rate where code = ?", new Object[]{tierKey}, BigDecimal.class);
                    } catch(IncorrectResultSizeDataAccessException missing) {
                        log.warn(missing.getMessage() + " - [{}]", tierKey);
                        recordRate = BigDecimal.ZERO;
                    }
                    tierRates.put(tierKey, recordRate);
                } else if (tierKey != null) {
                    recordRate = tierRates.get(tierKey);
                }
                if (recordRate != null) {
                    record.setTierRate(recordRate);
                }
                if (tierKey != null && !tierAddlRates.containsKey(tierKey)) {
                    try {
                        recordAddlRate = jdbcTemplate.queryForObject("select addl_rate from labor_rate where code = ?", new Object[]{tierKey}, BigDecimal.class);
                    } catch(IncorrectResultSizeDataAccessException missing) {
                        recordAddlRate = BigDecimal.ZERO;
                    }
                    tierAddlRates.put(tierKey, recordAddlRate);
                } else if (tierKey != null) {
                    recordAddlRate = tierAddlRates.get(tierKey);
                }
                if (recordAddlRate != null) {
                    record.setTierAddlRate(recordAddlRate);
                }

                Service service = null;
                String serviceKey = record.getServiceName();
                if (serviceKey != null && !services.containsKey(serviceKey)) {
                    service = applicationDataDaoService.findAnyServiceByName(serviceKey);
                    services.put(serviceKey, service);
                } else if (serviceKey != null) {
                    service = services.get(serviceKey);
                }
                if (service != null) {
                    record.setServiceId(service.getServiceId());
                    try {
                        // not sure why we coded ospId to be a String in Service...
                        record.setOspId(new Long(service.getOspId()));
                    } catch(Exception ignore) {
                    }
                    record.setBusinessModel(service.getBusinessModel());
                    record.setRecordType("direct");
                }

                Long customerId = null;
                String customerKey = record.getCustomerSysId();
                if (customerKey != null && !customerIds.containsKey(customerKey)) {
                    customerId = customerIdBySysId(customerKey);
                    customerIds.put(customerKey, customerId);
                } else if (customerKey != null) {
                    customerId = customerIds.get(customerKey);
                }
                if (customerId != null) {
                    record.setCustomerId(customerId);
                    if (record.getRecordType() == null) {
                        record.setRecordType("derived");
                    }
                }

                Integer expenseCategoryId = null;
                String taskDescription = record.getTaskDescription();
                String subtaskDescription = record.getSubtaskDescription();
                if (!expenseCategoryIds.containsKey(new ChronosTaskMapping(taskDescription, subtaskDescription))) {
                    expenseCategoryId = mappedExpenseCategoryByTaskSubtask(taskDescription, subtaskDescription);
                    if (expenseCategoryId != null) {
                        expenseCategoryIds.put(new ChronosTaskMapping(taskDescription, subtaskDescription), expenseCategoryId);
                    }
                } else {
                    expenseCategoryId = expenseCategoryIds.get(new ChronosTaskMapping(taskDescription, subtaskDescription));
                }
                if (expenseCategoryId != null) {
                    record.setExpenseCategoryId(expenseCategoryId);
                    record.setRecordType("categorized");
                }
                // "indirect labor" has no specified customer or service
                if (record.getRecordType() == null && serviceKey == null && customerKey == null) {
                    applyTaskDescriptionRules(record, rulesMap);
                }
            }

            // assess success of import
            for (ChronosRawData record : data) {
                if (record.getTierRate() == null || record.getTierRate().equals(BigDecimal.ZERO)) {
                    norate++;
                }
                if (record.getRecordType() == null) {
                    unaccounted++;
                }
            }

            /**
             * process distributed, categorized labor data splitting records for "parent" categories
             */
            List<ChronosRawData> distributedData = new ArrayList<ChronosRawData>();
            Map<Integer, ExpenseCategory> distributedExpenseCategories = distributedExpenseCategoriesByName();
            int childcount = 0;
            for (ChronosRawData record : data) {
                if (record.getExpenseCategoryId() != null &&
                        distributedExpenseCategories.containsKey(record.getExpenseCategoryId())) {
                    ExpenseCategory parent = distributedExpenseCategories.get(record.getExpenseCategoryId());
                    for (ExpenseCategory sub : parent.getSubcategories()) {
                        ChronosRawData split = new ChronosRawData(record);
                        split.setExpenseCategoryId(sub.getId());
                        split.setSubtaskDescription(sub.getName());
                        split.setHours(record.getHours().multiply(sub.getLaborSplit()));
                        distributedData.add(split);
                        childcount++;
                    }
                } else {
                    distributedData.add(record);
                }
            }
            log.info("Raw labor data count increased for distributed expense categories by [{}]", childcount);
            try {
                saveBatch(distributedData);
                rawDataCount += distributedData.size();
            } catch (Exception any) {
                log.error("an error occurred importing Chronos data", any);
                return;
            }

            /**
             * Populate the "grouped" tables...
             * read in the raw data written after the last import, summing tier data
             * for all non null record types
             * note: null record types are DROPPED if we can't figure out if the record
             * is direct, derived, categorized or indirect what are we to do?
             */
            query = "select work_date, sum(hours * tier_rate) as labor, sum(hours * addl_tier_rate) as addl_labor, customer_name, customer_sysid,"
                    + " customer_id, service_name, service_id, osp_id, business_model, labor_type, task_description, subtask_description, expense_category_id,"
                    + " rule, record_type, case when ticket like 'PRJ%' then true else false end as 'onboarding'"
                    + " from raw_labor_data where id > ? and record_type is not null"
                    + " group by work_date, customer_name, customer_sysid, customer_id, service_name, service_id, osp_id, business_model,"
                    + " labor_type, task_description, subtask_description, expense_category_id, rule, record_type, onboarding"
                    + " order by work_date, service_name, customer_name, labor_type, task_description, subtask_description";
            data = jdbcTemplate.query(query, new Object[]{(lastRawDataEntry == null ? 0 : lastRawDataEntry)}, new RowMapper<ChronosRawData>() {
                @Override
                public ChronosRawData mapRow(ResultSet rs, int i) throws SQLException {
                    return new ChronosRawData(
                            rs.getDate("work_date"),
                            rs.getBigDecimal("labor"),
                            rs.getBigDecimal("addl_labor"),
                            rs.getString("customer_name"),
                            rs.getString("customer_sysid"),
                            rs.getLong("customer_id"),
                            rs.getString("service_name"),
                            rs.getLong("service_id"),
                            rs.getLong("osp_id"),
                            rs.getString("business_model"),
                            rs.getString("labor_type"),
                            rs.getString("task_description"),
                            rs.getString("subtask_description"),
                            rs.getInt("expense_category_id"),
                            rs.getString("rule"),
                            rs.getString("record_type"),
                            rs.getBoolean("onboarding"));
                }
            });
            try {
                saveBatchGrouped(data);
            } catch (Exception any) {
                log.error("an error occurred saving 'Grouped' Chronos data", any);
                return;
            }
            List<ChronosRawData> derivedData = new ArrayList<ChronosRawData>();
            for (ChronosRawData record : data) {
                if ("derived".equals(record.getRecordType())) {
                    List<DerivedServiceInfo> derivedServiceInfo = mapServiceDeviceCount(record.getWorkDate(), record.getCustomerId());
                    if (!derivedServiceInfo.isEmpty()) {
                        for (DerivedServiceInfo serviceInfo : derivedServiceInfo) {
                            ChronosRawData splitRecord = new ChronosRawData();
                            splitRecord.setOnboarding(record.getOnboarding());
                            splitRecord.setWorkDate(record.getWorkDate());
                            if (record.getLaborTotal() != null && serviceInfo.getDeviceCountFraction() != null) {
                                splitRecord.setLaborTotal(record.getLaborTotal().multiply(new BigDecimal(serviceInfo.getDeviceCountFraction())));
                            } else {
                                log.warn("derived labor data found with null laborTotal or null device count fraction...");
                            }
                            if (record.getAddlLaborTotal() != null && serviceInfo.getDeviceCountFraction() != null) {
                                splitRecord.setAddlLaborTotal(record.getAddlLaborTotal().multiply(new BigDecimal(serviceInfo.getDeviceCountFraction())));
                            } else {
                                log.warn("derived labor data found with null addlLaborTotal or null device count fraction...");
                            }
                            splitRecord.setCustomerName(record.getCustomerName());
                            splitRecord.setCustomerSysId(record.getCustomerSysId());
                            splitRecord.setCustomerId(record.getCustomerId());
                            splitRecord.setServiceName(serviceInfo.getServiceName());
                            splitRecord.setServiceId(serviceInfo.getServiceId());
                            splitRecord.setOspId(serviceInfo.getOspId());
                            splitRecord.setBusinessModel(serviceInfo.getBusinessModel());
                            splitRecord.setLaborType(record.getLaborType());
                            splitRecord.setTaskDescription(record.getTaskDescription());
                            splitRecord.setSubtaskDescription(record.getSubtaskDescription());
                            derivedData.add(splitRecord);
                        }
                    } else {
                        if (record.getLaborTotal() != null) {
                            derivedUnaccounted = derivedUnaccounted.add(record.getLaborTotal());
                        }
                        if (record.getAddlLaborTotal() != null) {
                            derivedUnaccounted = derivedUnaccounted.add(record.getAddlLaborTotal());
                        }
                    }
                }
            }
            try {
                saveBatchDerived(derivedData);
            } catch (Exception any) {
                log.error("an error occurred saving 'Derived' Chronos data", any);
                return;
            }
            
            if (!fullimport) {
                break;
            }
            lastRawDataEntry = jdbcTemplate.queryForObject("select max(id) from raw_labor_data", Long.class);
            lastImportedDateTime = lastImportedDateTime.plusMonths(1);

            query = "select * from Export_Table where Date >= ? and Date < ?";
            data = chronosJdbcTemplate.query(query, new Object[]{lastImportedDateTime.toDate(), lastImportedDateTime.plusMonths(1).toDate()}, new RowMapper<ChronosRawData>() {
                @Override
                public ChronosRawData mapRow(ResultSet rs, int i) throws SQLException {
                    return new ChronosRawData(
                            rs.getInt("UID"),
                            rs.getString("Ticket"),
                            rs.getTimestamp("Date"),
                            rs.getString("Customer_ID"),
                            rs.getString("Customer"),
                            rs.getString("CI_ID"),
                            rs.getString("CI_Name"),
                            rs.getInt("Num_CIs"),
                            rs.getString("Service_Name"),
                            rs.getString("Task_Description"),
                            rs.getString("Subtask_Description"),
                            rs.getBigDecimal("Hours"),
                            rs.getString("Name"),
                            rs.getString("Team"),
                            rs.getString("Payrollcode"),
                            rs.getString("Labor_Type"),
                            rs.getTimestamp("Inserted"));
                }
            });
        }
        log.info("No record type assigned (likely no match for Service or Customer ID) Chronos record count [{}]", unaccounted);
        log.info("Chronos records without rates count [{}]", norate);
        log.info("Chronos records read as derived without Services to apply to amounted to $$: [{}]", derivedUnaccounted.setScale(2, RoundingMode.HALF_UP).toPlainString());
        
        jdbcTemplate.update("insert into labor_import_log (record_count, inserted_date) values (?, ?)",
                new Object[]{rawDataCount, importDateToLog.toDate()});
        log.debug("*** finished importing data from Chronos ***");
    }
    
    @Async
    @Scheduled(cron = "0 0 2 1 * ?") // 1st of every month at 2am
    // make a web call to /admin/indirectLaborUnitCost to kick off manually
    public void indirectLaborUnitCost() {
        if (!taskEnabled("indirect_labor_unit_cost")) {
            log.info("Chronos Indirect Labor data unit costs triggered but is not enabled...");
            return;
        }
        List<String> businessModels = jdbcTemplate.queryForList("select distinct business_model from service"
                + " where business_model is not null and active = true", String.class);
        try {
            Date lastEntryDate = jdbcTemplate.queryForObject("select max(applied_date) from indirect_labor_unit_cost", Date.class);
            if (lastEntryDate == null) {
                Date initialWorkDate = jdbcTemplate.queryForObject("select min(work_date) from grouped_labor_data", Date.class);
                DateTime initialDate = new DateTime(initialWorkDate).minusMonths(1);
                incrementIndirectLaborCostUnit(initialDate.toDate(), businessModels);
            } else {
                incrementIndirectLaborCostUnit(lastEntryDate, businessModels);
            }
        } catch(Exception any) {
            log.info("exception returned for last applied date lookup...", any);
        }
    }
    
    private void incrementIndirectLaborCostUnit(Date lastAppliedDate, List<String> businessModels) {
            DateTime currentDateTime = new DateTime(lastAppliedDate)
                    .plusMonths(1)
                    .withDayOfMonth(1)
                    .withTimeAtStartOfDay();
            DateTime finalDateTime = new DateTime()
            		.minusMonths(1)
                    .withDayOfMonth(1)
                    .withTimeAtStartOfDay();
            int incr = 0;
            while (currentDateTime.compareTo(finalDateTime) <= 0) {
                Map<String, BigDecimal> unitCostMap = costService.indirectLaborForDates(currentDateTime, currentDateTime.dayOfMonth().withMaximumValue(), null);
                int inserted = jdbcTemplate.update(
                        "insert into indirect_labor_unit_cost (unit_cost, addl_unit_cost, applied_date) values (?, ?, ?)",
                        new Object[]{unitCostMap.get("labor_total"), unitCostMap.get("addl_labor_total"), currentDateTime.toDate()},
                        new int[]{Types.DECIMAL, Types.DECIMAL, Types.DATE});
                for (String businessModel : businessModels) {
                    unitCostMap = costService.indirectLaborForDates(currentDateTime, currentDateTime.dayOfMonth().withMaximumValue(), businessModel);
                    inserted = jdbcTemplate.update(
                            "insert into indirect_labor_unit_cost (business_model, unit_cost, addl_unit_cost, applied_date) values (?, ?, ?, ?)",
                            new Object[]{businessModel, unitCostMap.get("labor_total"), unitCostMap.get("addl_labor_total"), currentDateTime.toDate()},
                            new int[]{Types.VARCHAR, Types.DECIMAL, Types.DECIMAL, Types.DATE});
                }
                log.info("inserted indirect labor unit cost(s) for period {}",
                        DateTimeFormat.forPattern("yyyy-MM-dd").print(currentDateTime));
                currentDateTime = currentDateTime.plusMonths(1);
                incr++;
            }
            if (incr == 0) {
                log.info("no indirect labor unit cost(s) were inserted after last applied date of {}",
                        DateTimeFormat.forPattern("yyyy-MM-dd").print(new DateTime(lastAppliedDate)));
            }
    }
    
    private void applyTaskDescriptionRules(ChronosRawData record, Map<String, String> rulesMap) {
        record.setRecordType("indirect");
        // apply a specific laborType + taskDescription rule
        String rule = rulesMap.get(record.getLaborType()+","+(record.getTaskDescription() == null ? "*" : record.getTaskDescription()));
        if (rule != null) {
            record.setRule(rule);
        } else {
            // apply a general laborType only rule
            rule = rulesMap.get(record.getLaborType()+","+"*");
            if (rule != null) {
                record.setRule(rule);
            }
        }
    }
    
    private Map<String, String> mapTaskDescriptionRules() {
        List<TaskDescriptionRule> rules = lookupChronosTaskRules();
        Map<String, String> rulesMap = new HashMap<String, String>();
        for (TaskDescriptionRule rule : rules) {
            rulesMap.put(rule.getKey(), rule.getRule());
        }
        return rulesMap;
    }
    
    private List<TaskDescriptionRule> lookupChronosTaskRules() {
        return jdbcTemplate.query("select labor_type, task_description, rule from chronos_task_rule order by 1, 2 desc, 3", new RowMapper<TaskDescriptionRule>() {

            @Override
            public TaskDescriptionRule mapRow(ResultSet rs, int i) throws SQLException {
                return new TaskDescriptionRule(
                        rs.getString("labor_type"),
                        rs.getString("task_description"),
                        rs.getString("rule"));
            }
        });
    }
    
    private Long customerIdBySysId(String sysId) {
        try {
            return jdbcTemplate.queryForObject("select id from customer where sn_sys_id = ? order by si_enabled desc, archived asc limit 1", new Object[]{sysId}, Long.class);
        } catch(Exception exc) {
            log.info("Exception thrown looking up a customer by sysId: [{}]", exc.getMessage());
        }
        return null;
    }
    
    /**
     * Will return Expense Category data Mapped by name that are parents with children having distributed labor splits
     * 
     * @param name
     * @return 
     */
    private Map<Integer, ExpenseCategory> distributedExpenseCategoriesByName() {
        String query = "select ec.id as 'id', ec.name as 'name', ecc.id as 'cid', ecc.name as 'cname',"
                + " ecc.labor_split"
                + " from expense_category ec inner join expense_category ecc on ecc.parent_id = ec.id"
                + " where ec.parent_id is null and ecc.labor_split > 0.0"
                + " order by ec.id, ecc.id;";
        List<Map<String, Object>> results = jdbcTemplate.queryForList(query, new Object[]{});
        Map<Integer, ExpenseCategory> parents = new HashMap<Integer, ExpenseCategory>();
        ExpenseCategory pec = null;
        for (Map<String, Object> result : results) {
            Integer id = (Integer) result.get("id");
            if (pec == null || pec.getId() != id) {
                pec = new ExpenseCategory();
                pec.setId(id);
                pec.setName((String) result.get("name"));
                parents.put(pec.getId(), pec);
                continue;
            }
            ExpenseCategory ec = new ExpenseCategory();
            ec.setId((Integer) result.get("cid"));
            ec.setName((String) result.get("cname"));
            ec.setLaborSplit((BigDecimal) result.get("labor_split"));
            pec.getSubcategories().add(ec);
        }
        return parents;
    }
    
    private Integer mappedExpenseCategoryByTaskSubtask(String task, String subtask) {
        try {
            return jdbcTemplate.queryForObject("select expense_category_id from chronos_task_mapping where task_description = ? and subtask_description = ?", new Object[]{task, subtask}, Integer.class);
        } catch(IncorrectResultSizeDataAccessException ignore) {
        } catch(DataAccessException dae) {
            log.warn("DataAccessException thrown looking up an expense category mapping [{}, {}], message [{}]", new Object[]{task, subtask, dae.getMessage()});
        } catch(Exception other) {
            log.warn("Exception thrown looking up an expense category mapping [{}, {}], message [{}]", new Object[]{task, subtask, other.getMessage()});
        }
        return null;
    }
    
    private Boolean taskEnabled(String code) {
        if (StringUtils.isBlank(code)) {
            throw new IllegalArgumentException("the code is needed to check the scheduled task");
        }
        try {
            ScheduledTask st = applicationDataDaoService.findScheduledTaskByCode(code);
            if (st == null) {
                log.warn("Chronos scheduled task for code [{}] not found... returning NOT enabled.", code);
                return Boolean.FALSE;
            }
            return st.getEnabled();
        } catch(Exception any) {
            log.warn("exception thrown looking up scheduled task for Chronos sync [{}]... returning NOT enabled.", any.getMessage());
        }
        return Boolean.FALSE;
    }

    private List<DerivedServiceInfo> mapServiceDeviceCount(Date forWorkDate, Long customerId) {
        DateTime startDate = new DateTime(forWorkDate)
                .withZone(DateTimeZone.forID(TZID))
                .withDayOfMonth(1)
                .withTimeAtStartOfDay();
        DateTime endDate = startDate
                .dayOfMonth()
                .withMaximumValue()
                .plusHours(23).plusMinutes(59).plusSeconds(59);
        
        Map<String, Object> params = new HashMap<String, Object>();
        String query = "select svc.id, svc.osp_id, svc.name, svc.business_model, sum(csvc.quantity) quantity from contract_service csvc"
                + " left join service svc on svc.id = csvc.service_id"
                + " left join contract ctr on ctr.id = csvc.contract_id"
                + " left join customer cst on cst.id = ctr.customer_id"
                + " left join contract_service_device csd on csd.contract_service_id = csvc.id"
                + " left join device d on d.id = csd.device_id"
                + " where (csvc.start_date <= :leftDate or csvc.start_date between :leftDate and :rightDate)"
                + " and (csvc.end_date is null or csvc.end_date between :leftDate and :rightDate"
                + " or csvc.end_date >= :rightDate) and csvc.status != 'pending'"
                + " and cst.id = :customerId"
                + " and (d.device_type is null or d.device_type not in ('businessService'))"
                + " group by svc.id, svc.osp_id, svc.name, svc.business_model";
        params.put("customerId", customerId);
        params.put("leftDate", startDate.toDate());
        params.put("rightDate", endDate.toDate());
        List<DerivedServiceInfo> results = new ArrayList<DerivedServiceInfo>();
        SqlRowSet srs = namedJdbcTemplate.queryForRowSet(query, params);
        Integer totalDeviceCount = 0;
        while(srs.next()) {
            totalDeviceCount += srs.getInt("quantity");
            results.add(new DerivedServiceInfo(
                    srs.getLong("id"),
                    srs.getLong("osp_id"),
                    srs.getString("name"),
                    srs.getString("business_model"),
                    srs.getInt("quantity")
            ));
        }
        for (DerivedServiceInfo member : results) {
            member.setTotalDeviceCount(totalDeviceCount);
        }
        return results;
    }

    private void saveBatch(final List<ChronosRawData> records) {
        log.debug("saving batch (raw) with [{}] records", records.size());
        jdbcTemplate.batchUpdate("insert into raw_labor_data (chronos_id, ticket, work_date,"
                + " hours, customer_name, customer_sysid, customer_id, ci_name, ci_sysid, num_cis,"
                + " service_name, service_id, osp_id, business_model, task_description, subtask_description, labor_type, expense_category_id,"
                + " rule, worker, tier_name, tier_code, tier_rate, addl_tier_rate, chronos_inserted_date, record_type) values (?, ?, ?, ?, ?, ?, ?, ?,"
                + " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                int j = 1;
                ChronosRawData record = records.get(i);
                ps.setInt(j++, record.getChronosId());
                ps.setString(j++, record.getTicket());
                ps.setTimestamp(j++, new java.sql.Timestamp(record.getWorkDate().getTime()));
                ps.setBigDecimal(j++, record.getHours());
                ps.setString(j++, record.getCustomerName());
                ps.setString(j++, record.getCustomerSysId());
                if (record.getCustomerId() == null) {
                    ps.setNull(j++, Types.BIGINT);
                } else {
                    ps.setLong(j++, record.getCustomerId());
                }
                ps.setString(j++, record.getCiName());
                ps.setString(j++, record.getCiSysId());
                if (record.getNumCis() == null) {
                    ps.setNull(j++, Types.INTEGER);
                } else {
                    ps.setInt(j++, record.getNumCis());
                }
                ps.setString(j++, record.getServiceName());
                if (record.getServiceId() == null) {
                    ps.setNull(j++, Types.BIGINT);
                    ps.setNull(j++, Types.BIGINT);
                    ps.setNull(j++, Types.VARCHAR);
                } else {
                    ps.setLong(j++, record.getServiceId());
                    ps.setLong(j++, record.getOspId());
                    ps.setString(j++, record.getBusinessModel());
                }
                ps.setString(j++, record.getTaskDescription());
                ps.setString(j++, record.getSubtaskDescription());
                ps.setString(j++, record.getLaborType());
                if (record.getExpenseCategoryId() == null) {
                    ps.setNull(j++, Types.INTEGER);
                } else {
                    ps.setInt(j++, record.getExpenseCategoryId());
                }
                ps.setString(j++, record.getRule());
                ps.setString(j++, record.getWorker());
                ps.setString(j++, record.getTierName());
                ps.setString(j++, record.getTierCode());
                ps.setBigDecimal(j++, (record.getTierRate() == null ? BigDecimal.ZERO : record.getTierRate()));
                ps.setBigDecimal(j++, (record.getTierAddlRate() == null ? BigDecimal.ZERO : record.getTierAddlRate()));
                ps.setTimestamp(j++, new java.sql.Timestamp(record.getInsertedDate().getTime()));
                ps.setString(j++, record.getRecordType());
            }

            @Override
            public int getBatchSize() {
                return records.size();
            }
        });
    }

    private void saveBatchGrouped(final List<ChronosRawData> records) {
        log.debug("saving batch (grouped) with [{}] records", records.size());
        jdbcTemplate.batchUpdate("insert into grouped_labor_data (work_date, labor_total, addl_labor_total,"
                + " customer_name, customer_sysid, customer_id, service_name, service_id, osp_id, business_model,"
                + " labor_type, task_description, subtask_description, expense_category_id, rule, record_type, onboarding)"
                + " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                int j = 1;
                ChronosRawData record = records.get(i);
                ps.setDate(j++, new java.sql.Date(record.getWorkDate().getTime()));
                ps.setBigDecimal(j++, record.getLaborTotal());
                ps.setBigDecimal(j++, record.getAddlLaborTotal());
                ps.setString(j++, record.getCustomerName());
                ps.setString(j++, record.getCustomerSysId());
                if (record.getCustomerId() == null || record.getCustomerId() == 0) {
                    ps.setNull(j++, Types.BIGINT);
                } else {
                    ps.setLong(j++, record.getCustomerId());
                }
                ps.setString(j++, record.getServiceName());
                if (record.getServiceId() == null || record.getServiceId() == 0) {
                    ps.setNull(j++, Types.BIGINT);
                    ps.setNull(j++, Types.BIGINT);
                    ps.setNull(j++, Types.VARCHAR);
                } else {
                    ps.setLong(j++, record.getServiceId());
                    ps.setLong(j++, record.getOspId());
                    ps.setString(j++, record.getBusinessModel());
                }
                ps.setString(j++, record.getLaborType());
                ps.setString(j++, record.getTaskDescription());
                ps.setString(j++, record.getSubtaskDescription());
                if (record.getExpenseCategoryId() == null || record.getExpenseCategoryId() == 0) {
                    ps.setNull(j++, Types.INTEGER);
                } else {
                    ps.setInt(j++, record.getExpenseCategoryId());
                }
                ps.setString(j++, record.getRule());
                ps.setString(j++, record.getRecordType());
                ps.setBoolean(j++, record.getOnboarding());
            }

            @Override
            public int getBatchSize() {
                return records.size();
            }
        });
    }

    private void saveBatchDerived(final List<ChronosRawData> records) {
        log.debug("saving batch (derived) with [{}] records", records.size());
        jdbcTemplate.batchUpdate("insert into derived_labor_data (work_date, labor_total, addl_labor_total,"
                + " customer_name, customer_sysid, customer_id, service_name, service_id, osp_id, business_model,"
                + " labor_type, task_description, subtask_description, onboarding)"
                + " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                int j = 1;
                ChronosRawData record = records.get(i);
                ps.setDate(j++, new java.sql.Date(record.getWorkDate().getTime()));
                ps.setBigDecimal(j++, record.getLaborTotal());
                ps.setBigDecimal(j++, record.getAddlLaborTotal());
                ps.setString(j++, record.getCustomerName());
                ps.setString(j++, record.getCustomerSysId());
                if (record.getCustomerId() == null || record.getCustomerId() == 0) {
                    ps.setNull(j++, Types.BIGINT);
                } else {
                    ps.setLong(j++, record.getCustomerId());
                }
                ps.setString(j++, record.getServiceName());
                if (record.getServiceId() == null || record.getServiceId() == 0) {
                    ps.setNull(j++, Types.BIGINT);
                    ps.setNull(j++, Types.BIGINT);
                    ps.setNull(j++, Types.VARCHAR);
                } else {
                    ps.setLong(j++, record.getServiceId());
                    ps.setLong(j++, record.getOspId());
                    ps.setString(j++, record.getBusinessModel());
                }
                ps.setString(j++, record.getLaborType());
                ps.setString(j++, record.getTaskDescription());
                ps.setString(j++, record.getSubtaskDescription());
                ps.setBoolean(j++, record.getOnboarding());
            }

            @Override
            public int getBatchSize() {
                return records.size();
            }
        });
    }
    
    public static class DerivedServiceInfo {
        
        private Long serviceId;
        private Long ospId;
        private String serviceName;
        private String businessModel;
        private Integer deviceCount;
        private Integer totalDeviceCount;
        
        public DerivedServiceInfo(Long serviceId, Long ospId, String serviceName, String businessModel, Integer deviceCount) {
            this.serviceId = serviceId;
            this.ospId = ospId;
            this.serviceName = serviceName;
            this.businessModel = businessModel;
            this.deviceCount = deviceCount;
        }

        public Long getServiceId() {
            return serviceId;
        }

        public Long getOspId() {
            return ospId;
        }

        public String getServiceName() {
            return serviceName;
        }

        public String getBusinessModel() {
            return businessModel;
        }

        public Integer getDeviceCount() {
            return deviceCount;
        }

        public void setTotalDeviceCount(Integer totalDeviceCount) {
            this.totalDeviceCount = totalDeviceCount;
        }

        public Integer getTotalDeviceCount() {
            return totalDeviceCount;
        }
        
        public Double getDeviceCountFraction() {
            return new Fraction(this.getDeviceCount(), this.getTotalDeviceCount()).doubleValue();
        }

        @Override
        public String toString() {
            return "DerivedServiceInfo{" + "serviceId=" + serviceId + ", ospId=" + ospId + ", serviceName=" + serviceName + ", businessModel=" + businessModel + ", deviceCount=" + deviceCount + ", totalDeviceCount=" + totalDeviceCount + '}';
        }
    }
    
    public static class TaskDescriptionRule implements Comparable<TaskDescriptionRule> {
        
        private String laborType;
        private String taskDescription;
        private String rule;
        
        public TaskDescriptionRule(String laborType, String taskDescription, String rule) {
            this.laborType = laborType;
            this.taskDescription = taskDescription;
            this.rule = rule;
        }

        public String getLaborType() {
            return laborType;
        }

        public void setLaborType(String laborType) {
            this.laborType = laborType;
        }

        public String getTaskDescription() {
            return taskDescription;
        }

        public void setTaskDescription(String taskDescription) {
            this.taskDescription = taskDescription;
        }

        public String getRule() {
            return rule;
        }

        public void setRule(String rule) {
            this.rule = rule;
        }
        
        public String getKey() {
            return getLaborType() + "," + (getTaskDescription() == null ? "*" : getTaskDescription());
        }

        @Override
        public int compareTo(TaskDescriptionRule o) {
            if (this == o) {
                return 0;
            }
            if (laborType != o.laborType) {
                if (laborType != null && o.laborType != null) {
                    int idx = laborType.compareTo(o.laborType);
                    if (idx != 0) {
                        return idx;
                    }
                }
            }
            if (taskDescription != o.taskDescription) {
                if (taskDescription != null && o.taskDescription != null) {
                    int idx = taskDescription.compareTo(o.taskDescription);
                    if (idx != 0) {
                        return idx;
                    }
                }
            }
            if (rule != o.rule) {
                if (rule != null && o.rule != null) {
                    int idx = rule.compareTo(o.rule);
                    if (idx != 0) {
                        return idx;
                    }
                }
            }
            return 0;
        }

        @Override
        public String toString() {
            return "TaskDescriptionRule{" + "laborType=" + laborType + ", taskDescription=" + taskDescription + ", rule=" + rule + '}';
        }
    }
    
    public static class ChronosTaskMapping implements Comparable<ChronosTaskMapping> {

        private String taskDescription;
        private String subtaskDescription;
        private Integer expenseCategoryId;
        
        public ChronosTaskMapping() {
        }

        public ChronosTaskMapping(String taskDescription, String subtaskDescription) {
            this.taskDescription = taskDescription;
            this.subtaskDescription = subtaskDescription;
        }

        public ChronosTaskMapping(String taskDescription, String subtaskDescription, Integer expenseCategoryId) {
            this.taskDescription = taskDescription;
            this.subtaskDescription = subtaskDescription;
            this.expenseCategoryId = expenseCategoryId;
        }

        public String getTaskDescription() {
            return taskDescription;
        }

        public void setTaskDescription(String taskDescription) {
            this.taskDescription = taskDescription;
        }

        public String getSubtaskDescription() {
            return subtaskDescription;
        }

        public void setSubtaskDescription(String subtaskDescription) {
            this.subtaskDescription = subtaskDescription;
        }

        public Integer getExpenseCategoryId() {
            return expenseCategoryId;
        }

        public void setExpenseCategoryId(Integer expenseCategoryId) {
            this.expenseCategoryId = expenseCategoryId;
        }

        @Override
        public int compareTo(ChronosTaskMapping o) {
            if (this == o) {
                return 0;
            }
            if (taskDescription != o.taskDescription) {
                if (taskDescription != null && o.taskDescription != null) {
                    int idx = taskDescription.compareTo(o.taskDescription);
                    if (idx != 0) {
                        return idx;
                    }
                }
            }
            if (subtaskDescription != o.subtaskDescription) {
                if (subtaskDescription != null && o.subtaskDescription != null) {
                    int idx = subtaskDescription.compareTo(o.subtaskDescription);
                    if (idx != 0) {
                        return idx;
                    }
                }
            }
            return 0;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 97 * hash + Objects.hashCode(this.taskDescription);
            hash = 97 * hash + Objects.hashCode(this.subtaskDescription);
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
            final ChronosTaskMapping other = (ChronosTaskMapping) obj;
            if (!Objects.equals(this.taskDescription, other.taskDescription)) {
                return false;
            }
            if (!Objects.equals(this.subtaskDescription, other.subtaskDescription)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "ChronosTaskMapping{" + "taskDescription=" + taskDescription + ", subtaskDescription=" + subtaskDescription + ", expenseCategoryId=" + expenseCategoryId + '}';
        }
    }
    
    public static class ChronosRawData implements Comparable<ChronosRawData> {

        private Integer chronosId;
        private String ticket;
        private Date workDate;
        private String customerSysId;
        private String customerName;
        private Long customerId;
        private String ciSysId;
        private String ciName;
        private Integer numCis;
        private String serviceName;
        private Long serviceId;
        private Long ospId;
        private String businessModel;
        private String taskDescription;
        private String subtaskDescription;
        private Integer expenseCategoryId;
        private String rule;
        private BigDecimal hours;
        private BigDecimal laborTotal;
        private BigDecimal addlLaborTotal;
        private String worker;
        private String tierName;
        private String tierCode;
        private BigDecimal tierRate;
        private BigDecimal tierAddlRate;
        private String laborType;
        private Date insertedDate;
        private String recordType;
        private Boolean onboarding;

        /**
         * default CTOR
         */
        public ChronosRawData() {
        }

        public ChronosRawData(Integer chronosId, String ticket, Date workDate, String customerSysId,
                String customerName, String ciSysId, String ciName, Integer numCis, String serviceName,
                String taskDescription, String subtaskDescription, BigDecimal hours, String worker,
                String tierName, String tierCode, String laborType, Date insertedDate) {
            this.chronosId = chronosId;
            this.ticket = ticket;
            this.workDate = workDate;
            this.customerSysId = customerSysId;
            this.customerName = customerName;
            this.ciSysId = ciSysId;
            this.ciName = ciName;
            this.numCis = numCis;
            this.serviceName = serviceName;
            this.taskDescription = taskDescription;
            this.subtaskDescription = subtaskDescription;
            this.hours = hours;
            this.worker = worker;
            this.tierName = tierName;
            this.tierCode = tierCode;
            this.laborType = laborType;
            this.insertedDate = insertedDate;
        }

        public ChronosRawData(Date workDate, BigDecimal laborTotal, BigDecimal addlLaborTotal, String customerName, String customerSysId,
                Long customerId, String serviceName, Long serviceId, Long ospId, String businessModel, String laborType,
                String taskDescription, String subtaskDescription, Integer expenseCategoryId, String rule, String recordType, Boolean onboarding) {
            this.workDate = workDate;
            this.laborTotal = laborTotal;
            this.addlLaborTotal = addlLaborTotal;
            this.customerName = customerName;
            this.customerSysId = customerSysId;
            this.customerId = customerId;
            this.serviceName = serviceName;
            this.serviceId = serviceId;
            this.ospId = ospId;
            this.businessModel = businessModel;
            this.laborType = laborType;
            this.taskDescription = taskDescription;
            this.subtaskDescription = subtaskDescription;
            this.expenseCategoryId = expenseCategoryId;
            this.rule = rule;
            this.recordType = recordType;
            this.onboarding = onboarding;
        }
        
        public ChronosRawData(ChronosRawData tocopy) {
            this.chronosId = tocopy.chronosId;
            this.ticket = tocopy.ticket;
            this.workDate = tocopy.workDate;
            this.customerSysId = tocopy.customerSysId;
            this.customerName = tocopy.customerName;
            this.customerId = tocopy.customerId;
            this.ciSysId = tocopy.ciSysId;
            this.ciName = tocopy.ciName;
            this.numCis = tocopy.numCis;
            this.serviceName = tocopy.serviceName;
            this.serviceId = tocopy.serviceId;
            this.ospId = tocopy.ospId;
            this.businessModel = tocopy.businessModel;
            this.taskDescription = tocopy.taskDescription;
            this.subtaskDescription = tocopy.subtaskDescription;
            this.expenseCategoryId = tocopy.expenseCategoryId;
            this.rule = tocopy.rule;
            this.hours = tocopy.hours;
            this.worker = tocopy.worker;
            this.tierName = tocopy.tierName;
            this.tierCode = tocopy.tierCode;
            this.tierRate = tocopy.tierRate;
            this.tierAddlRate = tocopy.tierAddlRate;
            this.laborType = tocopy.laborType;
            this.insertedDate = tocopy.insertedDate;
            this.recordType = tocopy.recordType;
        }

        public Integer getChronosId() {
            return chronosId;
        }

        public void setChronosId(Integer chronosId) {
            this.chronosId = chronosId;
        }

        public String getTicket() {
            return ticket;
        }

        public void setTicket(String ticket) {
            this.ticket = ticket;
        }

        public Date getWorkDate() {
            return workDate;
        }

        public void setWorkDate(Date workDate) {
            this.workDate = workDate;
        }

        public String getCustomerSysId() {
            return customerSysId;
        }

        public void setCustomerSysId(String customerSysId) {
            this.customerSysId = customerSysId;
        }

        public String getCustomerName() {
            return customerName;
        }

        public void setCustomerName(String customerName) {
            this.customerName = customerName;
        }

        public Long getCustomerId() {
            return customerId;
        }

        public void setCustomerId(Long customerId) {
            this.customerId = customerId;
        }

        public String getCiSysId() {
            return ciSysId;
        }

        public void setCiSysId(String ciSysId) {
            this.ciSysId = ciSysId;
        }

        public String getCiName() {
            return ciName;
        }

        public void setCiName(String ciName) {
            this.ciName = ciName;
        }

        public Integer getNumCis() {
            return numCis;
        }

        public void setNumCis(Integer numCis) {
            this.numCis = numCis;
        }

        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        public Long getServiceId() {
            return serviceId;
        }

        public void setServiceId(Long serviceId) {
            this.serviceId = serviceId;
        }

        public Long getOspId() {
            return ospId;
        }

        public void setOspId(Long ospId) {
            this.ospId = ospId;
        }

        public String getBusinessModel() {
            return businessModel;
        }

        public void setBusinessModel(String businessModel) {
            this.businessModel = businessModel;
        }

        public String getTaskDescription() {
            return taskDescription;
        }

        public void setTaskDescription(String taskDescription) {
            this.taskDescription = taskDescription;
        }

        public String getSubtaskDescription() {
            return subtaskDescription;
        }

        public void setSubtaskDescription(String subtaskDescription) {
            this.subtaskDescription = subtaskDescription;
        }

        public Integer getExpenseCategoryId() {
            return expenseCategoryId;
        }

        public void setExpenseCategoryId(Integer expenseCategoryId) {
            this.expenseCategoryId = expenseCategoryId;
        }

        public BigDecimal getHours() {
            return hours;
        }

        public BigDecimal getFormattedHours() {
            if (hours != null) {
                return hours.setScale(2, RoundingMode.HALF_UP);
            }
            return new BigDecimal(0).setScale(2, RoundingMode.HALF_UP);
        }

        public void setHours(BigDecimal hours) {
            this.hours = hours;
        }

        public BigDecimal getLaborTotal() {
            return laborTotal;
        }

        public BigDecimal getFormattedLaborTotal() {
            if (laborTotal != null) {
                return laborTotal.setScale(2, RoundingMode.HALF_UP);
            }
            return new BigDecimal(0).setScale(2, RoundingMode.HALF_UP);
        }

        public void setLaborTotal(BigDecimal laborTotal) {
            this.laborTotal = laborTotal;
        }

        public BigDecimal getAddlLaborTotal() {
            return addlLaborTotal;
        }

        public BigDecimal getFormattedAddlLaborTotal() {
            if (addlLaborTotal != null) {
                return addlLaborTotal.setScale(2, RoundingMode.HALF_UP);
            }
            return new BigDecimal(0).setScale(2, RoundingMode.HALF_UP);
        }

        public void setAddlLaborTotal(BigDecimal addlLaborTotal) {
            this.addlLaborTotal = addlLaborTotal;
        }

        public String getWorker() {
            return worker;
        }

        public void setWorker(String worker) {
            this.worker = worker;
        }

        public String getTierName() {
            return tierName;
        }

        public void setTierName(String tierName) {
            this.tierName = tierName;
        }

        public String getTierCode() {
            return tierCode;
        }

        public void setTierCode(String tierCode) {
            this.tierCode = tierCode;
        }

        public BigDecimal getTierRate() {
            return tierRate;
        }

        public BigDecimal getFormattedTierRate() {
            if (tierRate != null) {
                return tierRate.setScale(2, RoundingMode.HALF_UP);
            }
            return new BigDecimal(0).setScale(2, RoundingMode.HALF_UP);
        }

        public void setTierRate(BigDecimal tierRate) {
            this.tierRate = tierRate;
        }

        public BigDecimal getTierAddlRate() {
            return tierAddlRate;
        }

        public BigDecimal getFormattedTierAddlRate() {
            if (tierAddlRate != null) {
                return tierAddlRate.setScale(2, RoundingMode.HALF_UP);
            }
            return new BigDecimal(0).setScale(2, RoundingMode.HALF_UP);
        }

        public void setTierAddlRate(BigDecimal tierAddlRate) {
            this.tierAddlRate = tierAddlRate;
        }

        public String getLaborType() {
            return laborType;
        }

        public void setLaborType(String laborType) {
            this.laborType = laborType;
        }

        public String getRule() {
            return rule;
        }

        public void setRule(String rule) {
            this.rule = rule;
        }

        public Date getInsertedDate() {
            return insertedDate;
        }

        public void setInsertedDate(Date insertedDate) {
            this.insertedDate = insertedDate;
        }

        public String getRecordType() {
            return recordType;
        }

        public void setRecordType(String recordType) {
            this.recordType = recordType;
        }

        public Boolean getOnboarding() {
            return onboarding;
        }

        public void setOnboarding(Boolean onboarding) {
            this.onboarding = onboarding;
        }

        @Override
        public String toString() {
            return "ChronosRawData{" + "chronosId=" + chronosId + ", ticket=" + ticket + ", workDate=" + workDate + ", customerSysId=" + customerSysId + ", customerName=" + customerName + ", customerId=" + customerId + ", ciSysId=" + ciSysId + ", ciName=" + ciName + ", numCis=" + numCis + ", serviceName=" + serviceName + ", serviceId=" + serviceId + ", ospId=" + ospId + ", businessModel=" + businessModel + ", taskDescription=" + taskDescription + ", subtaskDescription=" + subtaskDescription + ", expenseCategoryId=" + expenseCategoryId + ", rule=" + rule + ", hours=" + hours + ", laborTotal=" + laborTotal + ", addlLaborTotal=" + addlLaborTotal + ", worker=" + worker + ", tierName=" + tierName + ", tierCode=" + getTierCode() + ", tierRate=" + tierRate + ", tierAddlRate=" + tierAddlRate + ", laborType=" + laborType + ", insertedDate=" + insertedDate + ", recordType=" + recordType + ", onboarding=" + onboarding + '}';
        }

        @Override
        public int compareTo(ChronosRawData o) {
            if (this == o) {
                return 0;
            }
            if (workDate != o.workDate) {
                if (workDate != null && o.workDate != null) {
                    int idx = workDate.compareTo(o.workDate);
                    if (idx != 0) {
                        return idx;
                    }
                }
            }
            return 0;
        }
    }
}
