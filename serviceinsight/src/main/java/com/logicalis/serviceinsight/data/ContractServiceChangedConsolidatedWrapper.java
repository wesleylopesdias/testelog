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
 * @author mfoster
 */

public class ContractServiceChangedConsolidatedWrapper {

	private Integer month;
    private String year;
    private String tzid;
    private Customer customer;
    private Contract contract;
    private ContractInvoice contractInvoice;
    private List<Service> previousMonth = new ArrayList<Service>();
    private List<Service> removed = new ArrayList<Service>();
    private List<Service> added = new ArrayList<Service>();
    private List<Service> previousMonthDetails = new ArrayList<Service>();
    private List<Service> removedDetails = new ArrayList<Service>();
    private List<Service> addedDetails = new ArrayList<Service>();
    private List<ContractAdjustment> previousMonthAdjustments = new ArrayList<ContractAdjustment>();
    private List<ContractAdjustment> removedAdjustments = new ArrayList<ContractAdjustment>();
    private List<ContractAdjustment> addedAdjustments = new ArrayList<ContractAdjustment>();
    private List<ContractAdjustment> previousMonthAdjustmentDetails = new ArrayList<ContractAdjustment>();
    private List<ContractAdjustment> removedAdjustmentDetails = new ArrayList<ContractAdjustment>();
    private List<ContractAdjustment> addedAdjustmentDetails = new ArrayList<ContractAdjustment>();
    private BigDecimal onetimeDifference;
    private BigDecimal recurringDifference;
    private DateTime fromDate;
    private DateTime toDate;
    
    /**
     * default CTOR
     */
    public ContractServiceChangedConsolidatedWrapper() {
    }
    
    public ContractServiceChangedConsolidatedWrapper(Customer customer, Contract contract, Integer month, String year, String tzid) {
        this.customer = customer;
        this.contract = contract;
        this.month = month;
        this.year = year;
        this.tzid = tzid;
        
        this.fromDate = DateTimeFormat.forPattern("yyyy").parseDateTime(year).withMonthOfYear(month).withDayOfMonth(1).withTimeAtStartOfDay().withZone(DateTimeZone.forID(tzid));
        this.toDate = fromDate.dayOfMonth().withMaximumValue().plusHours(23).plusMinutes(59).plusSeconds(59);
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
    
    public ContractInvoice getContractInvoice() {
		return contractInvoice;
	}

	public void setContractInvoice(ContractInvoice contractInvoice) {
		this.contractInvoice = contractInvoice;
	}

	public BigDecimal getPreviousMonthOnetime() {
        BigDecimal result = new BigDecimal(0);
        for (Service service : previousMonth) {
            result = result.add(service.getOnetimeRevenue());
        }
        return result;
    }
    
    public BigDecimal getFormattedPreviousMonthOnetime() {
        return getPreviousMonthOnetime().setScale(2, RoundingMode.HALF_UP);
    }
    
    public BigDecimal getPreviousMonthAdjustmentsOnetime() {
        BigDecimal result = new BigDecimal(0);
        for (ContractAdjustment adjustment : previousMonthAdjustments) {
            if (AdjustmentType.onetime.equals(AdjustmentType.valueOf(adjustment.getAdjustmentType()))) {
                result = result.add(adjustment.getAdjustment());
            }
        }
        return result;
    }
    
    public BigDecimal getFormattedPreviousMonthAdjustmentsOnetime() {
        return getPreviousMonthAdjustmentsOnetime().setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal  getPreviousMonthTotalOnetime() {
    	return getPreviousMonthOnetime().add(getPreviousMonthAdjustmentsOnetime());
    }
    
    public BigDecimal getFormmattedPreviousMonthTotalOnetime() {
    	return getPreviousMonthTotalOnetime().setScale(2, RoundingMode.HALF_UP);
    }
    
    public BigDecimal getTotalOnetime() {
    	/*I don't think we need these since one-time totals don't carry over the same way, but I didn't want to
    	 * full remove the code in case we need to put it back in.
    	 */
        /*BigDecimal result = getPreviousMonthOnetime()
                .add(getPreviousMonthAdjustmentsOnetime());*/
    	BigDecimal result = new BigDecimal(0);
        for (Service service : added) {
            result = result.add(service.getOnetimeRevenue());
        }
        for (ContractAdjustment adjustment : addedAdjustments) {
            if (AdjustmentType.onetime.equals(AdjustmentType.valueOf(adjustment.getAdjustmentType()))) {
                result = result.add(adjustment.getAdjustment());
            }
        }
        /*
        for (Service service : removed) {
            result = result.subtract(service.getOnetimeRevenue());
        }
        for (ContractAdjustment adjustment : removedAdjustments) {
            if (AdjustmentType.onetime.equals(AdjustmentType.valueOf(adjustment.getAdjustmentType()))) {
                result = result.subtract(adjustment.getAdjustment());
            }
        }*/
        return result;
    }
    
    public BigDecimal getFormattedTotalOnetime() {
        return getTotalOnetime().setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getPreviousMonthRecurring() {
        BigDecimal result = new BigDecimal(0);
        for (Service service : previousMonth) {
            result = result.add(service.getRecurringRevenue());
        }
        return result;
    }
    
    public BigDecimal getFormattedPreviousMonthRecurring() {
        return getPreviousMonthRecurring().setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getPreviousMonthAdjustmentsRecurring() {
        BigDecimal result = new BigDecimal(0);
        for (ContractAdjustment adjustment : previousMonthAdjustments) {
            if (AdjustmentType.recurring.equals(AdjustmentType.valueOf(adjustment.getAdjustmentType()))) {
                result = result.add(adjustment.getAdjustment());
            }
        }
        return result;
    }
    
    public BigDecimal getFormattedPreviousMonthAdjustmentsRecurring() {
        return getPreviousMonthAdjustmentsRecurring().setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getPreviousMonthTotalRecurring() {
    	return getPreviousMonthRecurring().add(getPreviousMonthAdjustmentsRecurring());
    }
    
    public BigDecimal getFormattedPreviousMonthTotalRecurring() {
    	return getPreviousMonthTotalRecurring().setScale(2, RoundingMode.HALF_UP);
    }
    
    public BigDecimal getTotalRecurring() {
        BigDecimal result = getPreviousMonthRecurring()
                .add(getPreviousMonthAdjustmentsRecurring());
        for (Service service : added) {
            result = result.add(service.getRecurringRevenue());
        }
        for (ContractAdjustment adjustment : addedAdjustments) {
            if (AdjustmentType.recurring.equals(AdjustmentType.valueOf(adjustment.getAdjustmentType()))) {
                result = result.add(adjustment.getAdjustment());
            }
        }
        for (Service service : removed) {
        	result = result.subtract(service.getRecurringRevenue());
        }
        for (ContractAdjustment adjustment : removedAdjustments) {
            if (AdjustmentType.recurring.equals(AdjustmentType.valueOf(adjustment.getAdjustmentType()))) {
                result = result.subtract(adjustment.getAdjustment());
            }
        }
        return result;
    }
    
    public BigDecimal getFormattedTotalRecurring() {
        return getTotalRecurring().setScale(2, RoundingMode.HALF_UP);
    }

    public List<Service> getPreviousMonth() {
        return previousMonth;
    }

    public void setPreviousMonth(List<Service> services) {
        this.previousMonth = services;
    }
    
    public void addPreviousMonth(Service service) {
        this.previousMonth.add(service);
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

    public List<Service> getAdded() {
        return added;
    }

    public void setAdded(List<Service> services) {
        this.added = services;
    }
    
    public void addAdded(Service service) {
        this.added.add(service);
    }
	
    public List<Service> getPreviousMonthDetails() {
        return previousMonthDetails;
    }

    public void setPreviousMonthDetails(List<Service> services) {
        this.previousMonthDetails = services;
    }
    
    public void addPreviousMonthDetails(Service service) {
        this.previousMonthDetails.add(service);
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
    
    public List<Service> getAddedDetails() {
        return addedDetails;
    }

    public void setAddedDetails(List<Service> services) {
        this.addedDetails = services;
    }
    
    public void addAddedDetails(Service service) {
        this.addedDetails.add(service);
    }

    public List<ContractAdjustment> getPreviousMonthAdjustments() {
        return previousMonthAdjustments;
    }

    public void setPreviousMonthAdjustments(List<ContractAdjustment> previousMonthAdjustments) {
        this.previousMonthAdjustments = previousMonthAdjustments;
    }
    
    public void addPreviousMonthAdjustment(ContractAdjustment contractAdjustment) {
        this.previousMonthAdjustments.add(contractAdjustment);
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

	public List<ContractAdjustment> getPreviousMonthAdjustmentDetails() {
		return previousMonthAdjustmentDetails;
	}

	public void setPreviousMonthAdjustmentDetails(
			List<ContractAdjustment> previousMonthAdjustmentDetails) {
		this.previousMonthAdjustmentDetails = previousMonthAdjustmentDetails;
	}

	public void addPreviousMonthAdjustmentDetails(ContractAdjustment contractAdjustment) {
        this.previousMonthAdjustmentDetails.add(contractAdjustment);
    }
	
	public List<ContractAdjustment> getRemovedAdjustmentDetails() {
		return removedAdjustmentDetails;
	}

	public void setRemovedAdjustmentDetails(
			List<ContractAdjustment> removedAdjustmentDetails) {
		this.removedAdjustmentDetails = removedAdjustmentDetails;
	}
	
	public void addRemovedAdjustmentDetails(ContractAdjustment contractAdjustment) {
        this.removedAdjustmentDetails.add(contractAdjustment);
    }

	public List<ContractAdjustment> getAddedAdjustmentDetails() {
		return addedAdjustmentDetails;
	}

	public void setAddedAdjustmentDetails(
			List<ContractAdjustment> addedAdjustmentDetails) {
		this.addedAdjustmentDetails = addedAdjustmentDetails;
	}
	
	public void addAddedAdjustmentDetails(ContractAdjustment contractAdjustment) {
        this.addedAdjustmentDetails.add(contractAdjustment);
    }

	public BigDecimal getOnetimeDifference() {
		return onetimeDifference;
	}

	public void setOnetimeDifference(BigDecimal onetimeDifference) {
		this.onetimeDifference = onetimeDifference;
	}

	public BigDecimal getRecurringDifference() {
		return recurringDifference;
	}

	public void setRecurringDifference(BigDecimal recurringDifference) {
		this.recurringDifference = recurringDifference;
	}

	//set in constructor
	public DateTime getFromDate() {
		return fromDate;
	}

	public DateTime getToDate() {
		return toDate;
	}
    
    
}
