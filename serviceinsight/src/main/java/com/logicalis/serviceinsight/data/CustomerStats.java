package com.logicalis.serviceinsight.data;

import java.util.List;

/**
 * POJO representing customer stats on the Dashboard
 * 
 * @author jsanchez
 *
 */
public class CustomerStats {
    private List<CustomerStat> customers;
    
    public CustomerStats(){}

    public CustomerStats(List<CustomerStat> customers) {
        this.customers = customers;
    }

    public List<CustomerStat> getCustomers() {
        return customers;
    }

    public void setCustomers(List<CustomerStat> customers) {
        this.customers = customers;
    }

    @Override
    public String toString() {
        return "{customers:" + customers + "}";
    }

}
