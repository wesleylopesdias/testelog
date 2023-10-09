package com.logicalis.serviceinsight.data;

import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;

/**
 * POJO representing a service for the dashboard display
 * 
 * @author jsanchez
 *
 */
public class ServiceStat implements Comparable<ServiceStat> {

    private Long serviceId;
    private String serviceName;
    private BigDecimal currentMonthTotalRecurringRevenue;
    
    public ServiceStat() {
        serviceId = new Long(-1l);
        currentMonthTotalRecurringRevenue = BigDecimal.ZERO;
        serviceName = StringUtils.EMPTY;
    }

    public ServiceStat(Long serviceId, String serviceName) {
        super();
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.currentMonthTotalRecurringRevenue = BigDecimal.ZERO;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public BigDecimal getCurrentMonthTotalRecurringRevenue() {
        return currentMonthTotalRecurringRevenue;
    }

    public void setCurrentMonthTotalRecurringRevenue(BigDecimal currentMonthTotalRevenue) {
        this.currentMonthTotalRecurringRevenue = currentMonthTotalRevenue;
    }

    @Override
    public String toString() {
        return "{serviceId:" + serviceId + ", serviceName:" + serviceName + ", currentMonthTotalRevenue:"
                + currentMonthTotalRecurringRevenue + "}";
    }

    /**
     * Setting natural order to use the revenue amount - ascending amount order
     */
    @Override
    public int compareTo(ServiceStat o) {        
        return (this.getCurrentMonthTotalRecurringRevenue().compareTo(o.getCurrentMonthTotalRecurringRevenue()));
    }
    
}
