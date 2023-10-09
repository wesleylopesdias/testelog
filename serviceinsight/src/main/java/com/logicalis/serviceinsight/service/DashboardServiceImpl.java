package com.logicalis.serviceinsight.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;

import com.logicalis.serviceinsight.data.ChartSeriesEntry;
import com.logicalis.serviceinsight.data.ContractAdjustment;
import com.logicalis.serviceinsight.data.ContractAdjustment.AdjustmentType;
import com.logicalis.serviceinsight.data.ContractServiceDashboardItem;
import com.logicalis.serviceinsight.data.ContractStat;
import com.logicalis.serviceinsight.data.ContractStats;
import com.logicalis.serviceinsight.data.CustomerStat;
import com.logicalis.serviceinsight.data.CustomerStats;
import com.logicalis.serviceinsight.data.Dashboard;
import com.logicalis.serviceinsight.data.Device.DeviceType;
import com.logicalis.serviceinsight.data.Service;
import com.logicalis.serviceinsight.data.Service.ServiceType;
import com.logicalis.serviceinsight.data.ServiceStat;
import com.logicalis.serviceinsight.data.ServiceStats;

/**
 *
 * @author jsanchez
 *
 */
@org.springframework.stereotype.Service
public class DashboardServiceImpl extends BaseServiceImpl implements DashboardService {

    @Autowired
    RevenueService revenueService;

    @Override
    public Dashboard generateRevenueDashboard(DateTime startDate, DateTime endDate) {
        log.info("Entering generateRevenueDashboard");
        Dashboard dashboard = new Dashboard();
        log.info("... retrieving data");
        Map<String, List<ContractServiceDashboardItem>> data = retrieveServiceRevenueData(startDate, endDate);
        log.info("... retrieving adjustments");
        Map<String, List<ContractServiceDashboardItem>> adjustments = retrieveContractAdjustments(startDate, endDate);
        log.info("... processing data/adjustments");
        String currentMonth = DateTimeFormat.forPattern("MM/yyyy").print(endDate);
        BigDecimal adjustment;
        for (Map.Entry<String, List<ContractServiceDashboardItem>> entry : data.entrySet()) {
            // sum adjustments
            adjustment = sumAdjustments(adjustments.get(entry.getKey()));
            // add chartData for this month to dashboard's serviceStats
            addChartData(dashboard, entry.getKey(), entry.getValue(), adjustment, (currentMonth.equals(entry.getKey())));
            if (currentMonth.equals(entry.getKey())) {
                // top services // contracts // customers
                processCurrentMonth(dashboard, entry.getValue(), adjustments.get(entry.getKey()));
            }
        }
        log.info("Exiting generateRevenueDashboard");
        return dashboard;
    }

    /**
     * Sum the contract adjustments
     *
     * @param adjustments list of ContractAdjustment
     * @return sum of adjustments
     */
    private BigDecimal sumAdjustments(List<ContractServiceDashboardItem> adjustments) {
        BigDecimal sumAdjustments = BigDecimal.ZERO;
        for (ContractServiceDashboardItem adj : adjustments) {
            sumAdjustments = sumAdjustments.add(adj.getContractTotalAdjustment());
        }
        return sumAdjustments;
    }

    private void addChartData(Dashboard dashboard, String key, List<ContractServiceDashboardItem> items, BigDecimal adj, boolean currentMonth) {
        if (dashboard.getServiceStats() == null) {
            dashboard.setServiceStats(new ServiceStats());
        }
        // add month to chart data's date range
        dashboard.getServiceStats().getChartData().getDateRange().add(getMonth(key));
        // sum revenue + add to chart data's series
        BigDecimal managed = BigDecimal.ZERO;
        BigDecimal cloud = BigDecimal.ZERO;
        BigDecimal csp = BigDecimal.ZERO;
        BigDecimal other = BigDecimal.ZERO;
        for (ContractServiceDashboardItem item : items) {
            managed = managed.add(item.getMsTotalRevenue());
            cloud = cloud.add(item.getCloudTotalRevenue());
            csp = csp.add(item.getCspTotalRevenue());
            other = other.add(item.getOtherTotalRevenue());
        }
        // adding adjustment to OTHER
        other = other.add(adj);
        for (ChartSeriesEntry seriesEntry : dashboard.getServiceStats().getChartData().getSeries()) {
            if (seriesEntry.getName().equals(ServiceType.MANAGED.getDescription())) {
                seriesEntry.getData().add(managed.setScale(2, BigDecimal.ROUND_UP));
            } else if (seriesEntry.getName().equals(ServiceType.CLOUD.getDescription())) {
                seriesEntry.getData().add(cloud.setScale(2, BigDecimal.ROUND_UP));
            } else if (seriesEntry.getName().equals(ServiceType.CSP.getDescription())) {
                seriesEntry.getData().add(csp.setScale(2, BigDecimal.ROUND_UP));
            } else {
                seriesEntry.getData().add(other.setScale(2, BigDecimal.ROUND_UP));
            }
        }
        if (currentMonth) {
            dashboard.getServiceStats().setMsCurrentMonthRevenue(managed.setScale(2, BigDecimal.ROUND_UP));
            dashboard.getServiceStats().setCloudCurrentMonthRevenue(cloud.setScale(2, BigDecimal.ROUND_UP));
        }
    }

    private void processCurrentMonth(Dashboard dashboard, List<ContractServiceDashboardItem> items, List<ContractServiceDashboardItem> adjustments) {
        // Top Services
        Map<Long, ServiceStat> topManagedServicesMap = new HashMap<Long, ServiceStat>();
        Map<Long, ServiceStat> topCloudServicesMap = new HashMap<Long, ServiceStat>();
        // Contracts - tiles counting by Type + recent SOWs
        Map<Long, ContractStat> contractMap = new HashMap<Long, ContractStat>();

        ServiceStat curSvc; // ptr to current service for revenue tally
        ContractStat curCtr; // ptr to current contract for revenue tally

        // Services
        for (ContractServiceDashboardItem item : items) {
            if (!contractMap.containsKey(item.getContractId())) {
                contractMap.put(item.getContractId(), new ContractStat(item.getContractId(), item.getCustomerId(), item.getCustomerName(), item.getJobNumber(), item.getContractStart()));
            }
            curCtr = contractMap.get(item.getContractId());

            switch (item.getServiceType()) {
                case MANAGED:
                    if (!topManagedServicesMap.containsKey(item.getOspId())) {
                        topManagedServicesMap.put(item.getOspId(), new ServiceStat(item.getOspId(), item.getServiceName()));
                    }
                    curSvc = topManagedServicesMap.get(item.getOspId());
                    curSvc.setCurrentMonthTotalRecurringRevenue(curSvc.getCurrentMonthTotalRecurringRevenue().add(item.getMsRecurringRevenue()));
                    curCtr.setCurrentMonthTotalRecurringRevenue(curCtr.getCurrentMonthTotalRecurringRevenue().add(item.getMsRecurringRevenue()));
                    curCtr.getServiceTypes().add(item.getServiceType());
                    break;
                case CLOUD:
                    if (!topCloudServicesMap.containsKey(item.getOspId())) {
                        topCloudServicesMap.put(item.getOspId(), new ServiceStat(item.getOspId(), item.getServiceName()));
                    }
                    curSvc = topCloudServicesMap.get(item.getOspId());
                    curSvc.setCurrentMonthTotalRecurringRevenue(curSvc.getCurrentMonthTotalRecurringRevenue().add(item.getCloudRecurringRevenue()));
                    curCtr.setCurrentMonthTotalRecurringRevenue(curCtr.getCurrentMonthTotalRecurringRevenue().add(item.getCloudRecurringRevenue()));
                    curCtr.getServiceTypes().add(item.getServiceType());
                    break;
                case CSP:
                    curCtr.setCurrentMonthTotalRecurringRevenue(curCtr.getCurrentMonthTotalRecurringRevenue().add(item.getCspRecurringRevenue()));
                    curCtr.getServiceTypes().add(item.getServiceType());
                    break;
                case OTHER:
                    curCtr.setCurrentMonthTotalRecurringRevenue(curCtr.getCurrentMonthTotalRecurringRevenue().add(item.getOtherRecurringRevenue()));
                    curCtr.getServiceTypes().add(item.getServiceType());
                    break;
                default:
                    break;
            }
        }

        // Adjustments
        for (ContractServiceDashboardItem adj : adjustments) {
            if (!contractMap.containsKey(adj.getContractId())) {
                contractMap.put(adj.getContractId(), new ContractStat(adj.getContractId(), adj.getCustomerId(), adj.getCustomerName(), adj.getJobNumber(), adj.getContractStart()));
            }
            curCtr = contractMap.get(adj.getContractId());
            curCtr.setCurrentMonthTotalRecurringRevenue(curCtr.getCurrentMonthTotalRecurringRevenue().add(adj.getContractRecurringAdjustment()));
        }

        // Top Services
        addTopServices(dashboard,
                (new ArrayList<ServiceStat>(topManagedServicesMap.values())),
                (new ArrayList<ServiceStat>(topCloudServicesMap.values())));

        // Customers
        addCustomers(dashboard, (new ArrayList<ContractStat>(contractMap.values())));

        // Contracts
        addContracts(dashboard, (new ArrayList<ContractStat>(contractMap.values())));

    }

    private void addTopServices(Dashboard dashboard, List<ServiceStat> topManagedServices, List<ServiceStat> topCloudServices) {
        if (dashboard.getServiceStats() == null) {
            dashboard.setServiceStats(new ServiceStats());
        }
        if (topManagedServices.size() > 0) {
            Collections.sort(topManagedServices, Collections.reverseOrder());
            if (topManagedServices.size() > 5) {
                topManagedServices.subList(5, topManagedServices.size()).clear();
            }
            dashboard.getServiceStats().setTopManagedServices(topManagedServices);
        }
        if (topCloudServices.size() > 0) {
            Collections.sort(topCloudServices, Collections.reverseOrder());
            if (topCloudServices.size() > 5) {
                topCloudServices.subList(5, topCloudServices.size()).clear();
            }
            dashboard.getServiceStats().setTopCloudServices(topCloudServices);
        }
    }

    private void addCustomers(Dashboard dashboard, List<ContractStat> contracts) {
        if (dashboard.getCustomerStats() == null) {
            dashboard.setCustomerStats(new CustomerStats());
        }
        if (contracts.size() > 0) {
            Map<Long, CustomerStat> mapCustomers = new HashMap<Long, CustomerStat>();
            CustomerStat cur;
            for (ContractStat cstat : contracts) {
                if (!mapCustomers.containsKey(cstat.getCustomerId())) {
                    mapCustomers.put(cstat.getCustomerId(), new CustomerStat(cstat.getCustomerId(), cstat.getCustomerName()));
                }
                cur = mapCustomers.get(cstat.getCustomerId());
                cur.setContractCount(cur.getContractCount() + 1);
                cur.setCurrentMonthTotalRecurringRevenue(cur.getCurrentMonthTotalRecurringRevenue().add(cstat.getCurrentMonthTotalRecurringRevenue()));
            }
            List<CustomerStat> topCustomers = (new ArrayList<CustomerStat>(mapCustomers.values()));
            if (topCustomers.size() > 0) {
                Collections.sort(topCustomers, Collections.reverseOrder());
                if (topCustomers.size() > 5) {
                    topCustomers.subList(5, topCustomers.size()).clear();
                }
            }
            dashboard.getCustomerStats().setCustomers(topCustomers);
        }
    }

    private void addContracts(Dashboard dashboard, List<ContractStat> recent) {
        if (dashboard.getContractStats() == null) {
            dashboard.setContractStats(new ContractStats());
        }
        if (recent.size() > 0) {
            Collections.sort(recent, Collections.reverseOrder());
            long msCtr = 0L;
            long cloudCtr = 0L;
            long otherCtr = 0L;
            for (ContractStat cstat : recent) {
                if (cstat.getServiceTypes().size() == 1) {
                    for (ServiceType statType : cstat.getServiceTypes()) {
                        switch (statType) {
                            case MANAGED:
                                msCtr++;
                                break;
                            case CLOUD:
                                cloudCtr++;
                                break;
                            case CSP:
                                otherCtr++; // note: not breaking out a cspCtr
                                break;
                            case OTHER:
                                otherCtr++;
                                break;
                            default:
                                otherCtr++;
                                break;
                        }
                    }
                } else {
                    otherCtr++;
                }
            }
            dashboard.getContractStats().setMsContractCount(new Long(msCtr));
            dashboard.getContractStats().setCloudContractCount(new Long(cloudCtr));
            dashboard.getContractStats().setMsCloudContractCount(new Long(otherCtr));
            if (recent.size() > 5) {
                recent.subList(5, recent.size()).clear();
            }
            dashboard.getContractStats().setContracts(recent);
        }
    }

    private String getMonth(String key) {
        return (DateTimeFormat.forPattern("MM/yyyy").parseDateTime(key).monthOfYear().getAsShortText());
    }

    @Override
    public Map<String, List<ContractServiceDashboardItem>> retrieveServiceRevenueData(DateTime startDate, DateTime endDate) {
        log.info("Entering retrieveServiceRevenueDate");
        if (endDate == null) {
            endDate = DateTime.now();
        }
        if (startDate == null) {
            startDate = endDate.minusMonths(5).withDayOfMonth(1).withTime(0, 0, 0, 0);
        }

        String currentMonth = DateTimeFormat.forPattern("MM/yyyy").print(endDate);

        DateTime monthEndDate = null;

        StringBuilder queryBuilder = new StringBuilder("select dev.device_type, svc.business_model, csvc.contract_id, cst.id customer_id,"
                + " cst.name customer_name, ctr.name,"
                + " sum(csvc.quantity) quantity, sum(csvc.quantity * csvc.onetime_revenue) onetime,"
                + " sum(csvc.quantity * csvc.recurring_revenue) revenue,"
                + " csvc.start_date, csvc.end_date, ctr.start_date contract_start, ctr.job_number,"
                + " svc.osp_id, svc.name service_name"
                + " from contract_service csvc"
                + "    left join service svc on svc.id = csvc.service_id"
                + "    left join contract ctr on ctr.id = csvc.contract_id"
                + "    left join customer cst on cst.id = ctr.customer_id"
                + "    left join contract_service_device csvcd on csvc.id = csvcd.contract_service_id"
                + "    left join device dev on csvcd.device_id = dev.id"
                + " where (csvc.start_date <= :leftDate or csvc.start_date between :leftDate and :rightDate) and "
                + "       (csvc.end_date is null or csvc.end_date between :leftDate and :rightDate or csvc.end_date >= :rightDate)"
                + "          and (csvc.status = 'active' or csvc.status = 'donotbill')"
                + " group by csvc.contract_id, dev.device_type, svc.business_model, svc.name, svc.code, csvc.start_date, csvc.end_date"
                + " order by csvc.contract_id, dev.device_type, svc.business_model, svc.name, svc.code, csvc.start_date, csvc.end_date");

        int monthCounter = 0;

        Map<String, List<ContractServiceDashboardItem>> results = new TreeMap<String, List<ContractServiceDashboardItem>>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                DateTime d1 = DateTimeFormat.forPattern("MM/yyyy").parseDateTime(o1);
                DateTime d2 = DateTimeFormat.forPattern("MM/yyyy").parseDateTime(o2);
                return d1.compareTo(d2);
            }
        });


        while (monthEndDate == null || monthEndDate.isBefore(endDate)) {
            log.info("... ... query executing");
            final DateTime monthStartDate = startDate
                    .withDayOfMonth(1)
                    .plusMonths(monthCounter++)
                    .withTimeAtStartOfDay()
                    .withZone(DateTimeZone.forID(TZID));
            monthEndDate = monthStartDate.dayOfMonth().withMaximumValue().plusHours(23).plusMinutes(59).plusSeconds(59);

            Map<String, Object> params = new HashMap<String, Object>();
            params.put("leftDate", monthStartDate.toDate());
            params.put("rightDate", monthEndDate.toDate());
            List<ContractServiceDashboardItem> dashboardData = namedJdbcTemplate.query(queryBuilder.toString(), params,
                    new RowMapper<ContractServiceDashboardItem>() {
                @Override
                public ContractServiceDashboardItem mapRow(ResultSet rs, int i) throws SQLException {
                    Service service = new Service(
                            monthStartDate,
                            null, // code
                            null, // contractId
                            null, // contractGroupId
                            null, // contractUpdateId
                            null, // serviceId
                            null, // ospId
                            null, // version
                            null, // name
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
                            false);
                    BigDecimal msRecurring = BigDecimal.ZERO;
                    BigDecimal msRevenue = BigDecimal.ZERO;
                    BigDecimal cloudRecurring = BigDecimal.ZERO;
                    BigDecimal cloudRevenue = BigDecimal.ZERO;
                    BigDecimal cspRecurring = BigDecimal.ZERO;
                    BigDecimal cspRevenue = BigDecimal.ZERO;
                    BigDecimal otherRecurring = BigDecimal.ZERO;
                    BigDecimal otherRevenue = BigDecimal.ZERO;
                    String businessModel = rs.getString("business_model");
                    String deviceType = rs.getString("device_type");
                    ServiceType serviceType;
                    if (businessModel != null) {
                        if (businessModel.toUpperCase().equalsIgnoreCase(ServiceType.MANAGED.name())) {
                            msRecurring = service.getRecurringRevenue();
                            msRevenue = msRevenue.add(service.getOnetimeRevenue()).add(service.getRecurringRevenue());
                            serviceType = ServiceType.MANAGED;
                        } else if (businessModel.toUpperCase().equalsIgnoreCase(ServiceType.CLOUD.name())) {
                            cloudRecurring = service.getRecurringRevenue();
                            cloudRevenue = cloudRevenue.add(service.getOnetimeRevenue()).add(service.getRecurringRevenue());
                            serviceType = ServiceType.CLOUD;
                        } else if (businessModel.toUpperCase().equalsIgnoreCase(ServiceType.CSP.name())) {
                            cspRecurring = service.getRecurringRevenue();
                            cspRevenue = cspRevenue.add(service.getOnetimeRevenue()).add(service.getRecurringRevenue());
                            serviceType = ServiceType.CSP;
                        } else {
                            otherRecurring = service.getRecurringRevenue();
                            otherRevenue = otherRevenue.add(service.getOnetimeRevenue()).add(service.getRecurringRevenue());
                            serviceType = ServiceType.OTHER;
                        }
                    } else {
                        otherRecurring = service.getRecurringRevenue();
                        otherRevenue = otherRevenue.add(service.getOnetimeRevenue()).add(service.getRecurringRevenue());
                        serviceType = ServiceType.OTHER;
                    }
                    ContractServiceDashboardItem row = new ContractServiceDashboardItem(
                            rs.getLong("contract_id"), // contractId
                            rs.getString("name"), // contractName
                            rs.getString("job_number"), // jobNumber
                            rs.getDate("contract_start"), // contractStart
                            rs.getLong("customer_id"), // customerId
                            rs.getString("customer_name"), // customerName
                            rs.getLong("osp_id"), // ospId
                            rs.getString("service_name"), // serviceName
                            msRecurring, // msRecurringRevenue
                            msRevenue, // msTotalRevenue - Managed Services
                            cloudRecurring, // cloudRecurringRevenue
                            cloudRevenue, // cloudTotalRevenue - Cloud
                            cspRecurring, // cspRecurringRevenue
                            cspRevenue, // cspTotalRevenue - Cloud Service Provider
                            otherRecurring, // otherRecurringRevenue
                            otherRevenue, // otherTotalRevenue - Other
                            serviceType,
                            deviceType); // device device_type
                    return row;
                }
            });

            String key = DateTimeFormat.forPattern("MM/yyyy").print(monthStartDate);
            if (currentMonth.equals(key)) {
                log.info("... ... query completed - rolling up rows " + key);
                results.put(key, rollupServiceRevenueDashboardItems(dashboardData));
                log.info("... ... rollup completed " + key);
            } else {
                results.put(key, dashboardData);
                log.info("... ... query completed " + key);
            }

        }

        log.info("Exiting retrieveServiceRevenueDate");
        return results;
    }

    @Override
    public Map<String, List<ContractServiceDashboardItem>> retrieveContractAdjustments(DateTime startDate, DateTime endDate) {
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
                "select adj.contract_id, ctr.name contract_name, ctr.job_number, ctr.start_date contract_start,"
                + "     cst.id customer_id, cst.name customer_name, sum(adj.adjustment) adjustment,"
                + "     adj.adjustment_type, adj.start_date, adj.end_date, adj.status adj_status"
                + " from contract_adjustment adj"
                + " left join contract ctr on adj.contract_id = ctr.id"
                + " left join customer cst on cst.id = ctr.customer_id"
                + " where (adj.start_date <= :leftDate or adj.start_date between :leftDate and :rightDate)"
                + " and (adj.end_date is null or adj.end_date between :leftDate and :rightDate"
                + " or adj.end_date >= :rightDate)"
                + " group by adj.contract_id, adj.adjustment_type, adj.start_date, adj.end_date"
                + " order by adj.start_date desc");

        int monthCounter = 0;

        Map<String, List<ContractServiceDashboardItem>> results = new TreeMap<String, List<ContractServiceDashboardItem>>(new Comparator<String>() {
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
            List<ContractServiceDashboardItem> contractAdjustments = namedJdbcTemplate.query(queryBuilder.toString(), params,
                    new RowMapper<ContractServiceDashboardItem>() {
                @Override
                public ContractServiceDashboardItem mapRow(ResultSet rs, int i) throws SQLException {
                    ContractAdjustment contractAdjustment = new ContractAdjustment(
                            null, // no specific contract_adjustment record is queried
                            rs.getLong("contract_id"), // contract_id
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
                    BigDecimal recurringAdjustment = BigDecimal.ZERO;
                    BigDecimal totalAdjustment = BigDecimal.ZERO;
                    if (contractAdjustment.getAdjustmentType().equalsIgnoreCase(AdjustmentType.onetime.toString())) {
                        totalAdjustment = totalAdjustment.add(contractAdjustment.getAdjustmentApplied());
                    } else {
                        recurringAdjustment = recurringAdjustment.add(contractAdjustment.getAdjustment());
                        totalAdjustment = totalAdjustment.add(contractAdjustment.getAdjustment());
                    }
                    ContractServiceDashboardItem row = new ContractServiceDashboardItem(
                            rs.getLong("contract_id"), // contractId
                            rs.getString("contract_name"), // contractName
                            rs.getString("job_number"), // jobNumber
                            rs.getDate("contract_start"), // contractStart
                            rs.getLong("customer_id"), // customerId
                            rs.getString("customer_name"), // customerName
                            recurringAdjustment, // recurring adjustment
                            totalAdjustment);   // total adjustment
                    return row;
                }
            });
            String key = DateTimeFormat.forPattern("MM/yyyy").print(monthStartDate);
            results.put(key, contractAdjustments);
        }
        return results;
    }

    /**
     * Query returns rows that are grouped by contract, service and service
     * dates - after determining revenue amounts, there are rows which hold the
     * same services on a contract - this rolls them into a single item and
     * accumulates their revenue for dash-board reporting
     *
     * @param data raw query results
     * @return rolled up list of services with revenue amounts
     */
    private List<ContractServiceDashboardItem> rollupServiceRevenueDashboardItems(List<ContractServiceDashboardItem> data) {
        Map<String, ContractServiceDashboardItem> dataMap = new HashMap<String, ContractServiceDashboardItem>();
        String key;
        for (ContractServiceDashboardItem item : data) {
            key = item.getContractId() + ":" + item.getOspId();
            if (dataMap.containsKey(key)) {
                switch (item.getServiceType()) {
                    case CLOUD:
                        dataMap.get(key).setCloudRecurringRevenue(dataMap.get(key).getCloudRecurringRevenue().add(item.getCloudRecurringRevenue()));
                        dataMap.get(key).setCloudTotalRevenue(dataMap.get(key).getCloudTotalRevenue().add(item.getCloudTotalRevenue()));
                        break;
                    case MANAGED:
                        dataMap.get(key).setMsRecurringRevenue(dataMap.get(key).getMsRecurringRevenue().add(item.getMsRecurringRevenue()));
                        dataMap.get(key).setMsTotalRevenue(dataMap.get(key).getMsTotalRevenue().add(item.getMsTotalRevenue()));
                        break;
                    case CSP:
                        dataMap.get(key).setCspRecurringRevenue(dataMap.get(key).getCspRecurringRevenue().add(item.getCspRecurringRevenue()));
                        dataMap.get(key).setCspTotalRevenue(dataMap.get(key).getCspTotalRevenue().add(item.getCspTotalRevenue()));
                        break;
                    case OTHER:
                        dataMap.get(key).setOtherRecurringRevenue(dataMap.get(key).getOtherRecurringRevenue().add(item.getOtherRecurringRevenue()));
                        dataMap.get(key).setOtherTotalRevenue(dataMap.get(key).getOtherTotalRevenue().add(item.getOtherTotalRevenue()));
                        break;
                    default:
                        break;
                }
            } else {
                dataMap.put(key, item);
            }
        }
        return (new ArrayList<ContractServiceDashboardItem>(dataMap.values()));
    }
}
