package com.logicalis.serviceinsight.data;

import com.logicalis.serviceinsight.data.ContractAdjustment.AdjustmentType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;

/**
 * Encapsulates the Contract Service data, facilitating data xfer or producing a UI view
 * 
 * @author poneil
 */
public class ContractServiceWrapper {
    
    private Integer month;
    private String year;
    private String tzid;
    private Customer customer;
    private Contract contract;
    private List<Service> baseline = new ArrayList<Service>();
    private List<Service> removed = new ArrayList<Service>();
    private List<Service> added = new ArrayList<Service>();
    private List<Service> baselineDetails = new ArrayList<Service>();
    private List<Service> removedDetails = new ArrayList<Service>();
    private List<Service> addedDetails = new ArrayList<Service>();
    private List<ContractAdjustment> baselineAdjustments = new ArrayList<ContractAdjustment>();
    private List<ContractAdjustment> removedAdjustments = new ArrayList<ContractAdjustment>();
    private List<ContractAdjustment> addedAdjustments = new ArrayList<ContractAdjustment>();

    /**
     * default CTOR
     */
    public ContractServiceWrapper() {
    }
    
    public ContractServiceWrapper(Customer customer, Contract contract, Integer month, String year, String tzid) {
        this.customer = customer;
        this.contract = contract;
        this.month = month;
        this.year = year;
        this.tzid = tzid;
    }
    
    public String getBillingPeriod() {
        DateTime fromDate = DateTimeFormat.forPattern("yyyy").parseDateTime(year)
                .withMonthOfYear(month)
                .withDayOfMonth(1)
                .withTimeAtStartOfDay()
                .withZone(DateTimeZone.forID(tzid));
        DateTime toDate = fromDate.dayOfMonth().withMaximumValue().plusHours(23).plusMinutes(59).plusSeconds(59);
        return DateTimeFormat.forPattern("MM/dd/yyyy").print(fromDate) + " - " +
                DateTimeFormat.forPattern("MM/dd/yyyy").print(toDate);
    }
    
    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Contract getContract() {
        return contract;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
    }

    public BigDecimal getBaselineOnetime() {
        BigDecimal result = new BigDecimal(0);
        for (Service service : baseline) {
            result = result.add(service.getOnetimeRevenue());
        }
        return result;
    }
    
    public BigDecimal getFormattedBaselineOnetime() {
        return getBaselineOnetime().setScale(2, RoundingMode.HALF_UP);
    }
    
    public BigDecimal getBaselineAdjustmentsOnetime() {
        BigDecimal result = new BigDecimal(0);
        for (ContractAdjustment adjustment : baselineAdjustments) {
            if (AdjustmentType.onetime.equals(AdjustmentType.valueOf(adjustment.getAdjustmentType()))) {
                result = result.add(adjustment.getAdjustment());
            }
        }
        return result;
    }
    
    public BigDecimal getFormattedBaselineAdjustmentsOnetime() {
        return getBaselineAdjustmentsOnetime().setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getTotalOnetime() {
        BigDecimal result = getBaselineOnetime()
                .add(getBaselineAdjustmentsOnetime());
        for (Service service : added) {
            result = result.add(service.getOnetimeRevenue());
        }
        for (ContractAdjustment adjustment : addedAdjustments) {
            if (AdjustmentType.onetime.equals(AdjustmentType.valueOf(adjustment.getAdjustmentType()))) {
                result = result.add(adjustment.getAdjustment());
            }
        }
        return result;
    }
    
    public BigDecimal getFormattedTotalOnetime() {
        return getTotalOnetime().setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getBaselineRecurring() {
        BigDecimal result = new BigDecimal(0);
        for (Service service : baseline) {
            result = result.add(service.getRecurringRevenue());
        }
        return result;
    }
    
    public BigDecimal getFormattedBaselineRecurring() {
        return getBaselineRecurring().setScale(2, RoundingMode.HALF_UP);
    }
    
    public BigDecimal getBaselineAdjustmentsRecurring() {
        BigDecimal result = new BigDecimal(0);
        for (ContractAdjustment adjustment : baselineAdjustments) {
            if (AdjustmentType.recurring.equals(AdjustmentType.valueOf(adjustment.getAdjustmentType()))) {
                result = result.add(adjustment.getAdjustment());
            }
        }
        return result;
    }
    
    public BigDecimal getFormattedBaselineAdjustmentsRecurring() {
        return getBaselineAdjustmentsRecurring().setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getTotalRecurring() {
        BigDecimal result = getBaselineRecurring()
                .add(getBaselineAdjustmentsRecurring());
        for (Service service : added) {
            result = result.add(service.getRecurringRevenue());
        }
        for (ContractAdjustment adjustment : addedAdjustments) {
            if (AdjustmentType.recurring.equals(AdjustmentType.valueOf(adjustment.getAdjustmentType()))) {
                result = result.add(adjustment.getAdjustment());
            }
        }
        return result;
    }
    
    public BigDecimal getFormattedTotalRecurring() {
        return getTotalRecurring().setScale(2, RoundingMode.HALF_UP);
    }

    public List<Service> getBaseline() {
        return baseline;
    }

    public void setBaseline(List<Service> services) {
        this.baseline = services;
    }
    
    public void addBaseline(Service service) {
        this.baseline.add(service);
    }

    public List<Service> getBaselineDetails() {
        return baselineDetails;
    }

    public void setBaselineDetails(List<Service> services) {
        this.baselineDetails = services;
    }
    
    public void addBaselineDetails(Service service) {
        this.baselineDetails.add(service);
    }

    public List<Service> getRemoved() {
        return removed;
    }

    public void setRemoved(List<Service> service) {
        this.removed = service;
    }
    
    public void addRemoved(Service service) {
        this.removed.add(service);
    }

    public List<Service> getRemovedDetails() {
        return removedDetails;
    }

    public void setRemovedDetails(List<Service> services) {
        this.removedDetails = services;
    }
    
    public void addRemovedDetails(Service service) {
        this.removedDetails.add(service);
    }

    public List<Service> getAdded() {
        return added;
    }

    public void setAdded(List<Service> services) {
        this.added = services;
    }
    
    public void addAdded(Service service) {
        this.added.add(service);
    }

    public List<Service> getAddedDetails() {
        return addedDetails;
    }

    public void setAddedDetails(List<Service> services) {
        this.addedDetails = services;
    }
    
    public void addAddedDetails(Service service) {
        this.addedDetails.add(service);
    }

    public List<ContractAdjustment> getBaselineAdjustments() {
        return baselineAdjustments;
    }

    public void setBaselineAdjustments(List<ContractAdjustment> baselineAdjustments) {
        this.baselineAdjustments = baselineAdjustments;
    }
    
    public void addBaselineAdjustment(ContractAdjustment contractAdjustment) {
        this.baselineAdjustments.add(contractAdjustment);
    }

    public List<ContractAdjustment> getRemovedAdjustments() {
        return removedAdjustments;
    }

    public void setRemovedAdjustments(List<ContractAdjustment> removedAdjustments) {
        this.removedAdjustments = removedAdjustments;
    }
    
    public void addRemovedAdjustment(ContractAdjustment contractAdjustment) {
        this.removedAdjustments.add(contractAdjustment);
    }

    public List<ContractAdjustment> getAddedAdjustments() {
        return addedAdjustments;
    }

    public void setAddedAdjustments(List<ContractAdjustment> addedAdjustments) {
        this.addedAdjustments = addedAdjustments;
    }
    
    public void addAddedAdjustment(ContractAdjustment contractAdjustment) {
        this.addedAdjustments.add(contractAdjustment);
    }
}
