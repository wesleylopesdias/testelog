package com.logicalis.serviceinsight.data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.logicalis.serviceinsight.data.Service.ServiceType;

/**
 * POJO representing a contract for the dashboard display
 * 
 * @author jsanchez
 *
 */
public class ContractStat implements Comparable<ContractStat> {

    private Long id;
    private Long customerId;
    private String customerName;
    private String jobNumber;
    private String type;
    @JsonIgnore
    private Set<ServiceType> serviceTypes;
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="MMddyyyy", timezone="America/New_York")
    private Date startDate;
    private BigDecimal currentMonthTotalRecurringRevenue;
    
    public ContractStat() {}

    public ContractStat(Long id, Long customerId, String customerName, String jobNumber, Date startDate) {
        super();
        this.id = id;
        this.customerId = customerId;
        this.customerName = customerName;
        this.jobNumber = jobNumber;
        this.type = StringUtils.EMPTY;
        this.serviceTypes = new HashSet<ServiceType>();
        this.startDate = startDate;
        this.currentMonthTotalRecurringRevenue = BigDecimal.ZERO;
    }
    
    public Long getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getJobNumber() {
        return jobNumber;
    }

    public void setJobNumber(String jobNumber) {
        this.jobNumber = jobNumber;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {        
        StringBuilder sb = new StringBuilder();
        if (serviceTypes.size()>1) {
            sb.append(ServiceType.OTHER.getCode());
        } else {
            for (ServiceType st : getServiceTypes()) {
                sb.append(st.getCode());
            }
        }
        return sb.toString().trim();
    }

    public void setType(String type) {
        // never used - just included for documentation purposes
    }
    
    public Set<ServiceType> getServiceTypes () {
        return this.serviceTypes;
    }
    
    public void setServiceType(ServiceType serviceType) {
        this.serviceTypes.add(serviceType);
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public BigDecimal getCurrentMonthTotalRecurringRevenue() {
        return currentMonthTotalRecurringRevenue;
    }

    public void setCurrentMonthTotalRecurringRevenue(BigDecimal currentMonthTotalRecurringRevenue) {
        this.currentMonthTotalRecurringRevenue = currentMonthTotalRecurringRevenue;
    }

    @Override
    public String toString() {
        return "{customerName:" + customerName + ", jobNumber:" + jobNumber + ", id:" + id
                + ", type:" + type + ", startDate:" + startDate + ", currentMonthTotalRecurringRevenue:"
                + currentMonthTotalRecurringRevenue + "}";
    }

    /**
     * Setting the natural order to use the start date
     */
    @Override
    public int compareTo(ContractStat o) {
        return (this.getStartDate().compareTo(o.getStartDate()));
    }
        
}
