package com.logicalis.serviceinsight.service;

import com.logicalis.serviceinsight.data.Service;
import com.logicalis.serviceinsight.representation.RevenueReportResultRecord;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

/**
 * The idea of this interface is to provide methods for returning revenue for
 * data OUTSIDE of contracts, ala revenue for a service across ALL contracts.
 * 
 * @author poneil
 */
public interface RevenueService extends BaseService {
    
    /**
     * Returns a Map of revenue for a service where the key represents the month name
     * and the value represents a list of Service revenue (including service "lineitem"
     * revenue) for that month.
     * 
     * @param serviceId
     * @param year
     * @return 
     */
    public Map<String, List<Service>> serviceRevenueForYear(Long serviceId, String year);
    
    /**
     * Returns revenue for a service in a particular month (including service "lineitem"
     * revenue).
     * 
     * @param serviceId
     * @param month
     * @param year
     * @return 
     */
    public List<Service> serviceRevenueRollupForMonthOf(Long serviceId, Integer month, String year);
    
    /**
     * Returns a Map representing records of month (MM/yyyy) and revenue ($.$$) for the provided input
     * 
     * @param businessModel
     * @param ospId
     * @param startDate
     * @param endDate
     * @param customerId
     * @throws IllegalArgumentException if the startDate is provided but the endDate is null
     * @return 
     */
    public List<RevenueReportResultRecord> serviceRevenueReport(String businessModel, Long ospId, DateTime startDate, DateTime endDate, Long customerId, Boolean includeChildren, Boolean onlyInvoicedRevenue, Boolean forecastRevenue);
    
    /**
     * Returns a Map representing records of month (MM/yyyy) and revenue ($.$$) for the provided input
     * 
     * @param startDate
     * @param endDate
     * @param customerId
     * @return 
     */
    public Map<String, BigDecimal> contractAdjustmentReport(DateTime startDate, DateTime endDate, Long customerId, Boolean onlyInvoicedRevenue);
}
