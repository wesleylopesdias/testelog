package com.logicalis.serviceinsight.data;

import java.math.BigDecimal;

/**
 * POJO representing a customer for dashboard reporting
 * 
 * @author jsanchez
 *
 */
public class CustomerStat implements Comparable<CustomerStat> {

    private Long id;
    private String customerName;
    private long contractCount;
    private BigDecimal currentMonthTotalRecurringRevenue;
    
    public CustomerStat() {}

    public CustomerStat(Long id, String customerName) {
        super();
        this.id = id;
        this.customerName = customerName;
        this.contractCount = 0l;
        this.currentMonthTotalRecurringRevenue = BigDecimal.ZERO;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public long getContractCount() {
        return contractCount;
    }

    public void setContractCount(long contractCount) {
        this.contractCount = contractCount;
    }

    public BigDecimal getCurrentMonthTotalRecurringRevenue() {
        return currentMonthTotalRecurringRevenue;
    }

    public void setCurrentMonthTotalRecurringRevenue(BigDecimal currentMonthTotalRecurringRevenue) {
        this.currentMonthTotalRecurringRevenue = currentMonthTotalRecurringRevenue;
    }

    @Override
    public String toString() {
        return "{id:" + id + ", customerName:" + customerName + ", contractCount:" + contractCount
                + ", currentMonthTotalRecurringRevenue:" + currentMonthTotalRecurringRevenue + "}";
    }

    /**
     * Setting natural order to use the revenue amount - ascending amount order
     */
    @Override
    public int compareTo(CustomerStat o) {
        return (this.getCurrentMonthTotalRecurringRevenue().compareTo(o.getCurrentMonthTotalRecurringRevenue()));
    }
    
}
