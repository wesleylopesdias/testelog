package com.logicalis.serviceinsight.data;

import java.util.ArrayList;
import java.util.List;

import com.logicalis.serviceinsight.data.Service.ServiceType;

/**
 * POJO representing chart data (see Dashboard)
 * 
 * @author jsanchez
 *
 */
public class ChartData {
    private List<String> dateRange;
    private List<ChartSeriesEntry> series;
    
    public ChartData() {
        this.dateRange = new ArrayList<String>();
        this.series = new ArrayList<ChartSeriesEntry>();
        for (ServiceType type : ServiceType.values()) {
            this.series.add(new ChartSeriesEntry(type.getDescription()));
        }
    }

    public ChartData(List<String> dateRange, List<ChartSeriesEntry> series) {
        this.dateRange = dateRange;
        this.series = series;
    }

    public List<String> getDateRange() {
        return dateRange;
    }

    public void setDateRange(List<String> dateRange) {
        this.dateRange = dateRange;
    }

    public List<ChartSeriesEntry> getSeries() {
        return series;
    }

    public void setSeries(List<ChartSeriesEntry> series) {
        this.series = series;
    }

    @Override
    public String toString() {
        return "{dateRange:" + dateRange + ", series:" + series + "}";
    }
        
}
