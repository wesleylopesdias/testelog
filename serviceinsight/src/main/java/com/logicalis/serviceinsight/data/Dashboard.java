package com.logicalis.serviceinsight.data;

/**
 * 
 * @author jsanchez
 *
 */
public class Dashboard {
    private ContractStats contractStats;
    private ServiceStats serviceStats;
    private CustomerStats customerStats;
    
    public Dashboard(){}

    public Dashboard(ContractStats contractStats, ServiceStats serviceStats, CustomerStats customerStats) {
        this.contractStats = contractStats;
        this.serviceStats = serviceStats;
        this.customerStats = customerStats;
    }

    public ContractStats getContractStats() {
        return contractStats;
    }

    public void setContractStats(ContractStats contractStats) {
        this.contractStats = contractStats;
    }

    public ServiceStats getServiceStats() {
        return serviceStats;
    }

    public void setServiceStats(ServiceStats serviceStats) {
        this.serviceStats = serviceStats;
    }

    public CustomerStats getCustomerStats() {
        return customerStats;
    }

    public void setCustomerStats(CustomerStats customerStats) {
        this.customerStats = customerStats;
    }

    @Override
    public String toString() {
        return "Data {contractStats:" + contractStats + ", serviceStats:" + serviceStats + ", customerStats:"
                + customerStats + "}";
    }
    
}
