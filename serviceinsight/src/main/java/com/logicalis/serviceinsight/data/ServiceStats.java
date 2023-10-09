package com.logicalis.serviceinsight.data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * POJO representing service stats on the Dashboard
 * 
 * @author jsanchez
 *
 */
public class ServiceStats {
    private ChartData chartData;
    private List<ServiceStat> topManagedServices;
    private List<ServiceStat> topCloudServices;
    private BigDecimal msCurrentMonthRevenue;
    private BigDecimal cloudCurrentMonthRevenue;
    
    public ServiceStats() {
        this.chartData = new ChartData();
        this.topManagedServices = new ArrayList<ServiceStat>();
        this.topCloudServices = new ArrayList<ServiceStat>();
        this.msCurrentMonthRevenue = BigDecimal.ZERO;
        this.cloudCurrentMonthRevenue = BigDecimal.ZERO;
    }

    public ServiceStats(ChartData chartData, List<ServiceStat> topManagedServices, List<ServiceStat> topCloudServices,
                        BigDecimal msCurrentMonthRevenue, BigDecimal cloudCurrentMonthRevenue) {
        this.chartData = chartData;
        this.topManagedServices = topManagedServices;
        this.topCloudServices = topCloudServices;
        this.msCurrentMonthRevenue = msCurrentMonthRevenue;
        this.cloudCurrentMonthRevenue = cloudCurrentMonthRevenue;
    }

    public ChartData getChartData() {
        return chartData;
    }

    public void setChartData(ChartData chartData) {
        this.chartData = chartData;
    }

    public List<ServiceStat> getTopManagedServices() {
        return topManagedServices;
    }

    public void setTopManagedServices(List<ServiceStat> topManagedServices) {
        this.topManagedServices = topManagedServices;
    }

    public List<ServiceStat> getTopCloudServices() {
        return topCloudServices;
    }

    public void setTopCloudServices(List<ServiceStat> topCloudServices) {
        this.topCloudServices = topCloudServices;
    }

    public BigDecimal getMsCurrentMonthRevenue() {
        return msCurrentMonthRevenue;
    }

    public void setMsCurrentMonthRevenue(BigDecimal msCurrentMonthRevenue) {
        this.msCurrentMonthRevenue = msCurrentMonthRevenue;
    }

    public BigDecimal getCloudCurrentMonthRevenue() {
        return cloudCurrentMonthRevenue;
    }

    public void setCloudCurrentMonthRevenue(BigDecimal cloudCurrentMonthRevenue) {
        this.cloudCurrentMonthRevenue = cloudCurrentMonthRevenue;
    }

    @Override
    public String toString() {
        return "{chartData:" + chartData + ", topManagedServices:" + topManagedServices
                + ", topCloudServices:" + topCloudServices + ", msCurrentMonthRevenue:" + msCurrentMonthRevenue
                + ", cloudCurrentMonthRevenue:" + cloudCurrentMonthRevenue + "}";
    }
    
}
