package com.logicalis.serviceinsight.service;

import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import com.logicalis.serviceinsight.data.ContractServiceDashboardItem;
import com.logicalis.serviceinsight.data.Dashboard;

public interface DashboardService extends BaseService {

    /**
     * Generates a revenue based dashboard for the specified date range
     *
     * @param startDate
     * @param endDate
     * @return
     */
    public Dashboard generateRevenueDashboard(DateTime startDate, DateTime endDate);

    /**
     * Returns a Map representing records of month (MM/yyyy) and contract
     * service data for Dashboard reporting
     *
     * @param startDate
     * @param endDate
     * @return
     */
    public Map<String, List<ContractServiceDashboardItem>> retrieveServiceRevenueData(DateTime startDate, DateTime endDate);

    /**
     *
     * @param startDate
     * @param endDate
     */
    public Map<String, List<ContractServiceDashboardItem>> retrieveContractAdjustments(DateTime startDate, DateTime endDate);
}
