package com.logicalis.serviceinsight.data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * POJO representing one point in a chart series (see Dashboard)
 * 
 * @author jsanchez
 *
 */
public class ChartSeriesEntry {
    private String name;
    private List<BigDecimal> data;
    
    public ChartSeriesEntry(){}

    public ChartSeriesEntry(String name) {
        this.name = name;
        this.data = new ArrayList<BigDecimal>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<BigDecimal> getData() {
        return data;
    }

    public void setData(List<BigDecimal> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "{name:" + name + ", data:" + data + "}";
    }

}
