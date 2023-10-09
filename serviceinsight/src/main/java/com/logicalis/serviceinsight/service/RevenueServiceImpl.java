package com.logicalis.serviceinsight.service;

import com.logicalis.serviceinsight.dao.DeviceExpenseCategory;
import com.logicalis.serviceinsight.dao.ServiceExpenseCategory;
import com.logicalis.serviceinsight.dao.UnitCost;
import com.logicalis.serviceinsight.data.AssetFractionRecord;
import com.logicalis.serviceinsight.data.Contract;
import com.logicalis.serviceinsight.data.ContractAdjustment;
import com.logicalis.serviceinsight.data.CostFractionRecord;
import com.logicalis.serviceinsight.data.LineItemMonthlyRevenue;
import com.logicalis.serviceinsight.data.Service;
import com.logicalis.serviceinsight.representation.RevenueReportResultRecord;
import com.logicalis.serviceinsight.representation.RevenueReportResultRecord.UniqueRevenueService;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.StopWatch;

/**
 *
 * @author poneil
 */
@org.springframework.stereotype.Service
public class RevenueServiceImpl extends BaseServiceImpl implements RevenueService {

    @Autowired
    CostService costService;
    @Autowired
    CostDaoService costDaoService;
    @Autowired
    ApplicationDataDaoService applicationDataDaoService;
    @Autowired
    ContractRevenueService contractRevenueService;
    
    @Override
    public Map<String, List<Service>> serviceRevenueForYear(Long ospId, String year) {
        Map<String, List<Service>> yearlyRevenue = new TreeMap<String, List<Service>>(new Comparator<String>() {

            @Override
            public int compare(String o1, String o2) {
                return months.valueOf(o1).compareTo(months.valueOf(o2));
            }
        });
        for (int month = 1; month <= 12; month++) {
            yearlyRevenue.put(
                    DateTimeFormat.forPattern("yyyy").parseDateTime(year)
                    .withMonthOfYear(month).monthOfYear().getAsText(),
                    serviceRevenueRollupForMonthOf(ospId, month, year));
        }
        return yearlyRevenue;
    }

    @Override
    public List<Service> serviceRevenueRollupForMonthOf(Long ospId, Integer month, String year) {
        String query = "select svc.osp_id, svc.code, svc.name, sum(csvc.quantity) quantity,"
                + " sum(csvc.quantity * csvc.onetime_revenue) onetime,"
                + " sum(csvc.quantity * csvc.recurring_revenue) revenue,"
                + " csvc.start_date, csvc.end_date, GROUP_CONCAT(distinct csvc.status SEPARATOR ', ') as status"
                + " from service svc"
                + " left join contract_service csvc on csvc.service_id = svc.id"
                + " where svc.osp_id = :ospId"
                + " and (csvc.start_date <= :leftDate or csvc.start_date between :leftDate and :rightDate)"
                + " and (csvc.end_date is null or csvc.end_date between :leftDate and :rightDate or csvc.end_date >= :rightDate)"
                + " group by svc.name, svc.code, csvc.start_date, csvc.end_date"
                + " order by svc.name, svc.code, csvc.start_date, csvc.end_date";
        final DateTime datePointer = DateTimeFormat.forPattern("yyyy").parseDateTime(year)
                .withMonthOfYear(month)
                .withDayOfMonth(1)
                .withTimeAtStartOfDay()
                .withZone(DateTimeZone.forID(TZID));
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("ospId", ospId);
        params.put("leftDate", datePointer.toDate());
        params.put("rightDate", datePointer.dayOfMonth().withMaximumValue().plusHours(23).plusMinutes(59).plusSeconds(59).toDate());
        return namedJdbcTemplate.query(query, params,
                new RowMapper<Service>() {
            @Override
            public Service mapRow(ResultSet rs, int i) throws SQLException {
                return new Service(
                        datePointer,
                        rs.getString("code"),
                        null, // contractId
                        null, // contractGroupId
                        null, // contractUpdateId
                        null, // serviceId
                        rs.getString("osp_id"),
                        null, // version
                        rs.getString("name"),
                        rs.getBigDecimal("onetime"),
                        rs.getBigDecimal("revenue"),
                        rs.getInt("quantity"),
                        rs.getDate("start_date"),
                        rs.getDate("end_date"),
                        null, // device id
                        null, // device name
                        null, // device part number
                        null, // device description
                        null, // device unit_count
                        ((rs.getString("status") != null && rs.getString("status").contains("pending")) ? true : false));
            }
        });
    }

    @Override
    public List<RevenueReportResultRecord> serviceRevenueReport(String businessModel, Long ospId, DateTime startDate, DateTime endDate, Long customerId, Boolean includeChildren, Boolean onlyInvoicedRevenue, Boolean forecastRevenue) {
        StopWatch sw = new StopWatch();
        sw.start();
        
        if(forecastRevenue == null) forecastRevenue = false;
        if(onlyInvoicedRevenue == null) onlyInvoicedRevenue = false;
        List<Contract> contracts = new ArrayList<Contract>();
        if(forecastRevenue) {
        	DateTime contractStartDate = startDate;
        	contractStartDate = contractStartDate.minusMonths(6);
        	
        	//TODO -- add include children flag
        	contracts = contractRevenueService.findContractsForForecastReport(customerId, contractStartDate.toDate(), endDate.toDate());
        	if(contracts != null) {
        		for(Contract contract : contracts) {
        			Map<String, Object> forecastParams = new HashMap<String, Object>();
        	        StringBuilder queryBuilder = new StringBuilder("select cst.id as 'cust_id', cst.name as 'cust_name', svc.osp_id, svc.name,"
        	                + " sum(csvc.quantity) as 'quantity', sum(csvc.quantity * csd.unit_count) as 'unit_count',"
        	                + " sum(case when (csd.unit_count = 0 or csd.unit_count is null) then csvc.quantity else csvc.quantity * csd.unit_count end) as 'blended_unit_count',"
        	                + " d.id as 'device_id', d.part_number,"
        	                + " sum(csvc.quantity * csvc.onetime_revenue) onetime,"
        	                + " sum(csvc.quantity * csvc.recurring_revenue) revenue,"
        	                + " csvc.start_date, csvc.end_date"
        	                + " from contract_service csvc"
        	                + " inner join service svc on svc.id = csvc.service_id"
        	                + " inner join contract ctr on ctr.id = csvc.contract_id"
        	                + " inner join customer cst on cst.id = ctr.customer_id"
        	                + " inner join contract_service_device csd on csd.contract_service_id = csvc.id"
        	                + " inner join device d on d.id = csd.device_id"
        	        		+ " where (csvc.start_date <= :leftDate or csvc.start_date between :leftDate and :rightDate)"
        	                + " and (csvc.end_date is null or csvc.end_date between :leftDate and :rightDate or csvc.end_date >= :rightDate)"
        	        		+ " and ctr.id = :contractId");
        	        queryBuilder.append(" and (csvc.status = 'active' or csvc.status = 'donotbill')"
        	                + " and (d.device_type is null or d.device_type not in ('businessService'))");
        	        if (StringUtils.isNotBlank(businessModel)) {
        	            queryBuilder.append(" and svc.business_model = :businessModel");
        	            forecastParams.put("businessModel", businessModel);
        	        }
        	        if (ospId != null) {
        	            queryBuilder.append(" and svc.osp_id = :ospId");
        	            forecastParams.put("ospId", ospId);
        	        }
        	        forecastParams.put("contractId", contract.getId());
        	        queryBuilder.append(" group by cst.id, cst.name, svc.osp_id, svc.name, d.id, d.part_number, csvc.start_date, csvc.end_date"
        	                + " order by cst.id, svc.osp_id, d.id, csvc.start_date, csvc.end_date");
        	        
        	        final DateTime contractMonthStartDate = new DateTime(contract.getEndDate()).withDayOfMonth(1).withTimeAtStartOfDay().withZone(DateTimeZone.forID(TZID));
        	        DateTime contractMonthEndDate = new DateTime(contract.getEndDate()).dayOfMonth().withMaximumValue().plusHours(23).plusMinutes(59).plusSeconds(59).withZone(DateTimeZone.forID(TZID));
        	        log.info("Contract Query Start Date: " + contractMonthStartDate);
        	        log.info("Contract Query End Date: " + contractMonthEndDate);
        	        forecastParams.put("leftDate", contractMonthStartDate.toDate());
        	        forecastParams.put("rightDate", contractMonthEndDate.toDate());
                    List<Service> services = namedJdbcTemplate.query(queryBuilder.toString(), forecastParams,
                        new RowMapper<Service>() {
                            @Override
                            public Service mapRow(ResultSet rs, int i) throws SQLException {
                                Service service = 
                                        new Service(
                                        		contractMonthStartDate,
                                                null, // code
                                                null, // contractId
                                                null, // contractGroupId
                                                null, // contractUpdateId
                                                null, // serviceId
                                                rs.getString("osp_id"),
                                                null, // version
                                                rs.getString("name"),
                                                rs.getBigDecimal("onetime"),
                                                rs.getBigDecimal("revenue"),
                                                rs.getInt("quantity"),
                                                rs.getDate("start_date"),
                                                rs.getDate("end_date"),
                                                rs.getLong("device_id"), // device id
                                                null, // device name
                                                rs.getString("part_number"), // device part number
                                                null, // device description
                                                rs.getInt("blended_unit_count"), // device unit_count
                                                false);
                                service.setCustomerId(rs.getLong("cust_id"));
                                service.setCustomerName(rs.getString("cust_name"));
                                return service;
                            }
                        });
                    
                    BigDecimal monthTotalRecurringRevenue = new BigDecimal(0);
                    for(Service service : services) {
                    	log.info("Service: " + service);
                    	monthTotalRecurringRevenue = monthTotalRecurringRevenue.add(service.getRecurringRevenue());
                    	log.info("Total: " + monthTotalRecurringRevenue);
                    }
                    if(contract.getRenewalChange() != null) {
                    	monthTotalRecurringRevenue = monthTotalRecurringRevenue.add(monthTotalRecurringRevenue.multiply(contract.getRenewalChange().divide(new BigDecimal(100))));
                    	log.info("Total After Change: " + monthTotalRecurringRevenue);
                    }
                    contract.setMonthTotalRecurringRevenue(monthTotalRecurringRevenue);
        		}
        	}
        	log.info("Contracts with amount: " + contracts);
        	
        	//get the contract services for each and set month total recurring revenue -- filter items down
        }
        
        if (ospId != null) {
            /**
             * I think we should always have the business_model, so we can use it
             * when we need it... It should also match the ospId coming in, of course.
             */
            try {
                businessModel = jdbcTemplate.queryForObject("select business_model from service"
                        + " where active = true and osp_id = ?", new Object[]{ospId}, String.class);
            } catch(Exception ignore) {
            }
        }
        if (startDate == null && endDate == null) {
            startDate = new DateTime().withMonthOfYear(1);
            endDate = new DateTime().monthOfYear().withMaximumValue();
        } else if (endDate == null) {
            throw new IllegalArgumentException("If a start date is provided, an end date must be provided");
        }
        endDate = endDate.dayOfMonth().withMaximumValue().plusHours(23).plusMinutes(59).plusSeconds(59);
        DateTime monthEndDate = null;
        DateTime currentMonth = new DateTime().dayOfMonth().withMaximumValue().withTimeAtStartOfDay().plusHours(23).plusMinutes(59).plusSeconds(59);
        Map<String, Object> baseParams = new HashMap<String, Object>();
        StringBuilder queryBuilder = new StringBuilder("select cst.id as 'cust_id', cst.name as 'cust_name', svc.osp_id, svc.name,"
                + " sum(csvc.quantity) as 'quantity', sum(csvc.quantity * csd.unit_count) as 'unit_count',"
                + " sum(case when (csd.unit_count = 0 or csd.unit_count is null) then csvc.quantity else csvc.quantity * csd.unit_count end) as 'blended_unit_count',"
                + " d.id as 'device_id', d.part_number,"
                + " excat.id as 'exp_cat_id', excat.name as 'exp_cat_name',"
                + " sum(csvc.quantity * csvc.onetime_revenue) onetime,"
                + " sum(csvc.quantity * csvc.recurring_revenue) revenue,"
                + " csvc.start_date, csvc.end_date");
        if(onlyInvoicedRevenue) {
                queryBuilder.append(", ci.id, ci.status");
        }
        queryBuilder.append(" from contract_service csvc"
                + " inner join service svc on svc.id = csvc.service_id"
                + " inner join contract ctr on ctr.id = csvc.contract_id"
                + " inner join customer cst on cst.id = ctr.customer_id"
                + " inner join contract_service_device csd on csd.contract_service_id = csvc.id"
                + " inner join device d on d.id = csd.device_id"
                + " left outer join device_expense_category dexcat on d.id = dexcat.device_id"
                + " left outer join expense_category excat on dexcat.expense_category_id = excat.id");
        if(onlyInvoicedRevenue) {
                queryBuilder.append(" inner join contract_invoice ci on ci.contract_id = ctr.id");
        }
        queryBuilder.append(" where (csvc.start_date <= :leftDate or csvc.start_date between :leftDate and :rightDate)"
                + " and (csvc.end_date is null or csvc.end_date between :leftDate and :rightDate or csvc.end_date >= :rightDate)");
        if(onlyInvoicedRevenue) {
            queryBuilder.append(" and (ci.start_date between :leftDate and :rightDate) and ci.status = 'invoiced'");
        }
        queryBuilder.append(" and (csvc.status = 'active' or csvc.status = 'donotbill')"
                + " and (d.device_type is null or d.device_type not in ('businessService'))");
        if (StringUtils.isNotBlank(businessModel)) {
            queryBuilder.append(" and svc.business_model = :businessModel");
            baseParams.put("businessModel", businessModel);
        }
        if (ospId != null) {
            queryBuilder.append(" and svc.osp_id = :ospId");
            baseParams.put("ospId", ospId);
        }
        if (customerId != null) {
            baseParams.put("customerId", customerId);
            if (includeChildren != null && includeChildren) {
                queryBuilder.append(" and (cst.id = :customerId or cst.parent_id = :customerId)");
            } else {
                queryBuilder.append(" and cst.id = :customerId");
            }
        }
        queryBuilder.append(" group by cst.id, cst.name, svc.osp_id, svc.name, d.id, d.part_number, excat.id, excat.name, csvc.start_date, csvc.end_date"
                + " order by cst.id, svc.osp_id, d.id, excat.id, csvc.start_date, csvc.end_date");
        int monthCounter = 0;
        List<RevenueReportResultRecord> results = new ArrayList<RevenueReportResultRecord>();
        Map<Integer, String> expCatMap = new HashMap<Integer, String>(); // this is for logging names of expense categories efficiently
        while (monthEndDate == null || endDate.isAfter(monthEndDate)) {
            final DateTime monthStartDate = startDate
                    .withDayOfMonth(1)
                    .plusMonths(monthCounter++)
                    .withTimeAtStartOfDay()
                    .withZone(DateTimeZone.forID(TZID));
            monthEndDate = monthStartDate.dayOfMonth().withMaximumValue().plusHours(23).plusMinutes(59).plusSeconds(59);
            Map<String, Object> params = new HashMap<String, Object>();
            params.putAll(baseParams);
            params.put("leftDate", monthStartDate.toDate());
            params.put("rightDate", monthEndDate.toDate());
            List<Service> services = namedJdbcTemplate.query(queryBuilder.toString(), params,
                new RowMapper<Service>() {
                    @Override
                    public Service mapRow(ResultSet rs, int i) throws SQLException {
                        Service service = 
                                new Service(
                                        monthStartDate,
                                        null, // code
                                        null, // contractId
                                        null, // contractGroupId
                                        null, // contractUpdateId
                                        null, // serviceId
                                        rs.getString("osp_id"),
                                        null, // version
                                        rs.getString("name"),
                                        rs.getBigDecimal("onetime"),
                                        rs.getBigDecimal("revenue"),
                                        rs.getInt("quantity"),
                                        rs.getDate("start_date"),
                                        rs.getDate("end_date"),
                                        rs.getLong("device_id"), // device id
                                        null, // device name
                                        rs.getString("part_number"), // device part number
                                        null, // device description
                                        rs.getInt("blended_unit_count"), // device unit_count
                                        false);
                        service.setCustomerId(rs.getLong("cust_id"));
                        service.setCustomerName(rs.getString("cust_name"));
                        Integer expenseCategoryId = rs.getInt("exp_cat_id");
                        if (expenseCategoryId != null && expenseCategoryId > 0) {
                            service.addCostMapping(new DeviceExpenseCategory(service.getDeviceId(), expenseCategoryId, 0, Boolean.FALSE));
                        }
                        return service;
                    }
                });
            
            BigDecimal forecastedRevenue = new BigDecimal(0);
            List<Contract> monthlyForecastedContracts = new ArrayList<Contract>();
            if(forecastRevenue) {
            	log.info("Going to try to forecast revenue");
            	for(Contract contract : contracts) {
            		DateTime contractEndDate = new DateTime(contract.getEndDate()).dayOfMonth().withMaximumValue().plusHours(23).plusMinutes(59).plusSeconds(59);
            		if(monthEndDate.isAfter(contractEndDate)) {
            			forecastedRevenue = forecastedRevenue.add(contract.getMonthTotalRecurringRevenue());
            			monthlyForecastedContracts.add(contract);
            		}
            	}
            }
            

            /**
             * The next loop determines the total cost for each returned expense category per customer
             * and total device count. This will be used to determine the fraction of cost that can be
             * applied to each service revenue record per device.
             * Revenue is totaled also, BUT it must NOT sum duplicates by cost category. Revenue records
             * are unique by customer, service, device, start and end dates
             * Costs are unique by customer, service, device, cost category, start and end dates
             */
            Integer serviceCount = 0;
            Integer deviceCount = 0;
            BigDecimal totalRevenue = BigDecimal.ZERO;
            BigDecimal totalCost = BigDecimal.ZERO;
            BigDecimal serviceCost = BigDecimal.ZERO;
            List<UnitCost> customerUnitCosts = new ArrayList<UnitCost>();
            List<UnitCost> nonCustomerUnitCosts = new ArrayList<UnitCost>();
            List<UniqueRevenueService> uniqueRevenueRecords = new ArrayList<UniqueRevenueService>();
            for (Service service : services) {
                /**
                 * Because we queried records INCLUDING device expense categories and ordered
                 * them accordingly, revenue is duplicated across expense categories returned.
                 * So, we only sum Revenue when a "new" record is read by customerId, ospId, deviceId,
                 * start and end date
                 */
                UniqueRevenueService uniqueRevenueService = new UniqueRevenueService(
                        service.getCustomerId(), service.getOspId(), service.getDeviceId(),
                        service.getDevicePartNumber(), service.getStartDate(), service.getEndDate());
                if (!uniqueRevenueRecords.contains(uniqueRevenueService)) {
                    BigDecimal contributingRevenue = service.getOnetimeRevenue().add(service.getRecurringRevenue());
                    totalRevenue = totalRevenue.add(contributingRevenue);
                    serviceCount += service.getQuantity();
                    uniqueRevenueService.setServiceCount(service.getQuantity());
                    if (service.getDeviceUnitCount() != null) {
                        deviceCount += service.getDeviceUnitCount();
                        uniqueRevenueService.setDeviceCount(service.getDeviceUnitCount());
                    }
                    uniqueRevenueService.setServiceRevenue(contributingRevenue);
                    uniqueRevenueRecords.add(uniqueRevenueService);
                } else {
                    // we replace the "template" version of the record with it's "working" version
                    uniqueRevenueService = uniqueRevenueRecords.get(uniqueRevenueRecords.indexOf(uniqueRevenueService));
                }
                if (!service.getCostMappings().isEmpty()) {
                    DeviceExpenseCategory entry = service.getCostMappings().get(0); // there will only be one in the resultset record!
                    /**
                     * Find customer related costs
                     */
                    UnitCost cuc = new UnitCost(entry.getExpenseCategoryId(), service.getCustomerId(), monthStartDate);
                    int index = customerUnitCosts.indexOf(cuc);
                    if (index < 0) {
                        cuc = costDaoService.unitCostByExpenseCategoryAndDate(service.getCustomerId(), entry.getExpenseCategoryId(), monthStartDate);
                        if (cuc != null) { // there may not be a UnitCost in the database
                            cuc.setCustomerName(service.getCustomerName());
                            customerUnitCosts.add(cuc);
                        } else {
                            // otherwise we're hunting for this same non-functional unitCost over and over again...
                            customerUnitCosts.add(new UnitCost(entry.getExpenseCategoryId(), service.getCustomerId(), monthStartDate));
                        }
                    } else {
                        cuc = customerUnitCosts.get(index);
                    }
                    Integer serviceRecordDeviceCount = (service.getDeviceUnitCount() == null ? 0 : service.getDeviceUnitCount());
                    if (cuc != null && cuc.getDeviceTotalUnits() > 0) {
                        BigDecimal costFraction = new BigDecimal(serviceRecordDeviceCount).divide(new BigDecimal(cuc.getDeviceTotalUnits()), MathContext.DECIMAL32);
                        BigDecimal contributingCost = cuc.getTotalCost().multiply(costFraction);
                        totalCost = totalCost.add(contributingCost);
                        uniqueRevenueService.addDirectCost(contributingCost);
                        cuc.addContributingDeviceCount(serviceRecordDeviceCount);
                        cuc.addContributingCost(contributingCost);
                    }
                    /**
                     * Now find non-customer costs
                     */
                    UnitCost suc = new UnitCost(entry.getExpenseCategoryId(), null, monthStartDate);
                    index = nonCustomerUnitCosts.indexOf(suc);
                    if (index < 0) {
                        suc = costDaoService.unitCostByExpenseCategoryAndDate(null, entry.getExpenseCategoryId(), monthStartDate);
                        if (suc != null) { // there may not be a UnitCost in the database
                            nonCustomerUnitCosts.add(suc);
                        } else {
                            // otherwise we're hunting for this same non-functional unitCost over and over again...
                            nonCustomerUnitCosts.add(new UnitCost(entry.getExpenseCategoryId(), null, monthStartDate));
                        }
                    } else {
                        suc = nonCustomerUnitCosts.get(index);
                    }
                    if (suc != null && suc.getDeviceTotalUnits() > 0) {
                        BigDecimal costFraction = new BigDecimal(serviceRecordDeviceCount).divide(new BigDecimal(suc.getDeviceTotalUnits()), MathContext.DECIMAL32);
                        BigDecimal contributingCost = suc.getTotalCost().multiply(costFraction);
                        serviceCost = serviceCost.add(contributingCost);
                        uniqueRevenueService.addServiceCost(contributingCost);
                        suc.addContributingDeviceCount(serviceRecordDeviceCount);
                        suc.addContributingCost(contributingCost);
                    }
                }
            }
            log.debug("Customer Unit Cost Summary for Month: {}", DateTimeFormat.forPattern("yyyy-MM-dd").print(monthStartDate));
            for (UnitCost unitCost : customerUnitCosts) {
                if (!expCatMap.containsKey(unitCost.getExpenseCategoryId())) {
                    String expCatName = jdbcTemplate.queryForObject("select concat(ecp.name, ' - ', ecc.name) as 'Name'"
                        + " from expense_category ecc"
                        + " inner join expense_category ecp on ecc.parent_id = ecp.id"
                        + " where ecc.id = ?", String.class, new Object[]{unitCost.getExpenseCategoryId()});
                    expCatMap.put(unitCost.getExpenseCategoryId(), expCatName);
                }
                unitCost.setExpenseCategoryName(expCatMap.get(unitCost.getExpenseCategoryId()));
                if (unitCost.getTotalCost().compareTo(BigDecimal.ZERO) > 0) {
                    log.debug("\tCustomer: [{}] / {}, Cost Category: [{}] / {}, Total Cost: ${}, Total Unit Count: {}",
                            new Object[]{unitCost.getCustomerId(), unitCost.getCustomerName(), unitCost.getExpenseCategoryId(),
                            unitCost.getExpenseCategoryName(), unitCost.getTotalCost().setScale(2, RoundingMode.HALF_UP).toPlainString(),
                            unitCost.getDeviceTotalUnits()});
                }
            }
            log.debug("Non-Customer Unit Cost Summary for Month: {}", DateTimeFormat.forPattern("yyyy-MM-dd").print(monthStartDate));
            for (UnitCost unitCost : nonCustomerUnitCosts) {
                if (!expCatMap.containsKey(unitCost.getExpenseCategoryId())) {
                    String expCatName = jdbcTemplate.queryForObject("select concat(ecp.name, ' - ', ecc.name) as 'Name'"
                        + " from expense_category ecc"
                        + " inner join expense_category ecp on ecc.parent_id = ecp.id"
                        + " where ecc.id = ?", String.class, new Object[]{unitCost.getExpenseCategoryId()});
                    expCatMap.put(unitCost.getExpenseCategoryId(), expCatName);
                }
                unitCost.setExpenseCategoryName(expCatMap.get(unitCost.getExpenseCategoryId()));
                if (unitCost.getTotalCost().compareTo(BigDecimal.ZERO) > 0) {
                    log.debug("\tCost Category: [{}] / {}, Total Cost: ${}, Total Unit Count: {}",
                            new Object[]{unitCost.getExpenseCategoryId(), unitCost.getExpenseCategoryName(),
                            unitCost.getTotalCost().setScale(2, RoundingMode.HALF_UP).toPlainString(), unitCost.getDeviceTotalUnits()});
                }
            }
            String key = DateTimeFormat.forPattern("MM/yyyy").print(monthStartDate);
            RevenueReportResultRecord record = new RevenueReportResultRecord();
            record.setDisplayDate(key);
            record.setRevenue(totalRevenue);
            record.setDirectCost(totalCost);
            record.setDirectCostDetails(customerUnitCosts);
            record.setServiceCost(serviceCost);
            record.setServiceCostDetails(nonCustomerUnitCosts);
            record.setServiceCount(serviceCount);
            record.setDeviceCount(deviceCount);
            record.setForecastedRevenue(forecastedRevenue);
            record.setForecastedContracts(monthlyForecastedContracts);
            
            if(monthEndDate.equals(currentMonth) || monthEndDate.isAfter(currentMonth)) {
                record.setLaborCost(new BigDecimal(0));
                record.setAddlLaborCost(new BigDecimal(0));
                record.setOnboardingLaborCost(new BigDecimal(0));
                record.setAddlOnboardingLaborCost(new BigDecimal(0));
                record.setIndirectLaborCost(new BigDecimal(0));
                record.setAddlIndirectLaborCost(new BigDecimal(0));
                record.setIndirectLaborProportion(new BigDecimal(0));
                record.setAddlIndirectLaborProportion(new BigDecimal(0));
            } else {
            	Map<String, BigDecimal> serviceLaborForDatesMap = costService.serviceLaborForDates(businessModel, ospId, monthStartDate, monthEndDate, customerId, includeChildren, Boolean.FALSE);
                record.setLaborCost(serviceLaborForDatesMap.get("labor_total"));
                record.setAddlLaborCost(serviceLaborForDatesMap.get("addl_labor_total"));
            	serviceLaborForDatesMap = costService.serviceLaborForDates(businessModel, ospId, monthStartDate, monthEndDate, customerId, includeChildren, Boolean.TRUE);
                record.setOnboardingLaborCost(serviceLaborForDatesMap.get("labor_total"));
                record.setAddlOnboardingLaborCost(serviceLaborForDatesMap.get("addl_labor_total"));
                Map<String, BigDecimal> indirectLaborCostMap = costService.indirectLaborUnitCost(monthStartDate.toDate(), businessModel);
                record.setIndirectLaborCost(indirectLaborCostMap.get("unit_cost").multiply(new BigDecimal(record.getServiceCount())));
                record.setAddlIndirectLaborCost(indirectLaborCostMap.get("addl_unit_cost").multiply(new BigDecimal(record.getServiceCount())));
                Map<String, BigDecimal> indirectLaborProportionMap = costService.indirectLaborProportionForDates(monthStartDate, monthEndDate);
                record.setIndirectLaborProportion(indirectLaborProportionMap.get("indirect_labor_total_proportion"));
                record.setAddlIndirectLaborProportion(indirectLaborProportionMap.get("addl_indirect_labor_total_proportion"));
            }
            results.add(record);
        }
        sw.stop();
        log.debug("**** Total Revenue + Cost query took {} seconds ****", sw.getTotalTimeSeconds());
        return results;
    }
    
    @Override
    public Map<String, BigDecimal> contractAdjustmentReport(DateTime startDate, DateTime endDate, Long customerId, Boolean onlyInvoicedRevenue) {
    	if(onlyInvoicedRevenue == null) onlyInvoicedRevenue = false;
    	
        if (startDate == null && endDate == null) {
            startDate = new DateTime().withMonthOfYear(1);
            endDate = new DateTime().monthOfYear().withMaximumValue();
        } else if (endDate == null) {
            throw new IllegalArgumentException("If a start date is provided, an end date must be provided");
        }
        endDate = endDate.dayOfMonth().withMaximumValue().plusHours(23).plusMinutes(59).plusSeconds(59);
        DateTime monthEndDate = null;
        Map<String, Object> baseParams = new HashMap<String, Object>();
        StringBuilder queryBuilder = new StringBuilder(
                "select sum(adj.adjustment) adjustment, adj.adjustment_type, adj.start_date, adj.end_date, adj.status adj_status"
                + " from contract_adjustment adj"
                + " left join contract ctr on adj.contract_id = ctr.id"
                + " left join customer cst on ctr.customer_id = cst.id");
        
		        if(onlyInvoicedRevenue) {
					queryBuilder.append(" inner join contract_invoice ci on ci.contract_id = ctr.id");
				}
        
                queryBuilder.append(" where (adj.start_date <= :leftDate or adj.start_date between :leftDate and :rightDate)"
                + " and (adj.end_date is null or adj.end_date between :leftDate and :rightDate or adj.end_date >= :rightDate)");
                
                if(onlyInvoicedRevenue) {
        			queryBuilder.append(" and (ci.start_date between :leftDate and :rightDate) and ci.status = 'invoiced'");
        		}
        if (customerId != null) {
            queryBuilder.append(" and cst.id = :customerId");
            baseParams.put("customerId", customerId);
        }
        queryBuilder.append(" group by adj.contract_id, adj.adjustment_type, adj.start_date, adj.end_date"
                + " order by adj.start_date desc");
        int monthCounter = 0;
        Map<String, BigDecimal> results = new TreeMap<String, BigDecimal>(new Comparator<String>() {

            @Override
            public int compare(String o1, String o2) {
                DateTime d1 = DateTimeFormat.forPattern("MM/yyyy").parseDateTime(o1);
                DateTime d2 = DateTimeFormat.forPattern("MM/yyyy").parseDateTime(o2);
                return d1.compareTo(d2);
            }
        });
        while (monthEndDate == null || endDate.isAfter(monthEndDate)) {
            final DateTime monthStartDate = startDate
                    .withDayOfMonth(1)
                    .plusMonths(monthCounter++)
                    .withTimeAtStartOfDay()
                    .withZone(DateTimeZone.forID(TZID));
            monthEndDate = monthStartDate.dayOfMonth().withMaximumValue().plusHours(23).plusMinutes(59).plusSeconds(59);
            Map<String, Object> params = new HashMap<String, Object>();
            params.putAll(baseParams);
            params.put("leftDate", monthStartDate.toDate());
            params.put("rightDate", monthEndDate.toDate());
            List<ContractAdjustment> contractAdjustments = namedJdbcTemplate.query(queryBuilder.toString(), params,
                    new RowMapper<ContractAdjustment>() {
                @Override
                public ContractAdjustment mapRow(ResultSet rs, int i) throws SQLException {
                    return new ContractAdjustment(
                            null, // no specific contract_adjustment record is queried
                            null, // contract_id
                            null, // contract update id not included
                            null, // contract_group_id
                            rs.getBigDecimal("adjustment"), // summed...
                            rs.getString("adjustment_type"), // grouped by...
                            null, // note not included
                            monthStartDate,
                            rs.getDate("start_date"),
                            rs.getDate("end_date"),
                            Service.Status.valueOf(rs.getString("adj_status")),
                            null, // created not included
                            null, // created_by not included
                            null, // updated not included
                            null); // updated_by not included
                }
            });
            String key = DateTimeFormat.forPattern("MM/yyyy").print(monthStartDate);
            BigDecimal totalRevenue = BigDecimal.ZERO;
            for (ContractAdjustment contractAdjustment : contractAdjustments) {
                totalRevenue = totalRevenue.add(contractAdjustment.getAdjustment());
            }
            results.put(key, totalRevenue);
        }
        return results;
    }

    private List<LineItemMonthlyRevenue> serviceLineItemRevenueForMonthOf(Long contractServiceId, Integer month, String year) {
        if (contractServiceId == null) {
            throw new IllegalArgumentException("A contractServiceId MUST be specified");
        }
        String query = "select clit.id, clit.contract_id, clit.service_id, clit.lineitem_id,"
                + " li.name lineitem, clit.onetime_revenue onetime,"
                + " clit.quantity quantity, clit.recurring_revenue recurring, clit.start_date, clit.end_date"
                + " from contract_lineitem clit"
                + " left join lineitem lir on li.id = clit.lineitem_id"
                + " where clit.contract_service_id = :contractServiceId"
                + " and (clit.start_date <= :leftDate or clit.start_date between :leftDate and :rightDate)"
                + " and (clit.end_date is null or clit.end_date between :leftDate and :rightDate"
                + " or clit.end_date >= :rightDate)";
        final DateTime datePointer = DateTimeFormat.forPattern("yyyy").parseDateTime(year)
                .withMonthOfYear(month)
                .withDayOfMonth(1)
                .withTimeAtStartOfDay()
                .withZone(DateTimeZone.forID(TZID));
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("contractServiceId", contractServiceId);
        params.put("leftDate", datePointer.toDate());
        params.put("rightDate", datePointer.dayOfMonth().withMaximumValue().plusHours(23).plusMinutes(59).plusSeconds(59).toDate());
        return namedJdbcTemplate.query(query, params,
                new RowMapper<LineItemMonthlyRevenue>() {
            @Override
            public LineItemMonthlyRevenue mapRow(ResultSet rs, int i) throws SQLException {
                return new LineItemMonthlyRevenue(
                        rs.getDate("start_date"),
                        rs.getDate("end_date"),
                        datePointer.toDate(),
                        rs.getBigDecimal("recurring"),
                        rs.getLong("id"),
                        rs.getLong("contract_id"),
                        rs.getLong("contract_service_id"),
                        rs.getLong("service_id"),
                        rs.getLong("lineitem_id"),
                        rs.getString("lineitem"),
                        rs.getBigDecimal("onetime"),
                        rs.getInt("quantity"));
            }
        });
    }
}
