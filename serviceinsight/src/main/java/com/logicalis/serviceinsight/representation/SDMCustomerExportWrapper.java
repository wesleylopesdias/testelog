package com.logicalis.serviceinsight.representation;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;

import com.logicalis.serviceinsight.data.Contract;
import com.logicalis.serviceinsight.data.ContractAdjustment;
import com.logicalis.serviceinsight.data.Customer;
import com.logicalis.serviceinsight.data.Service;

public class SDMCustomerExportWrapper {

	private Integer month;
    private String year;
    private String tzid;
	private Customer customer;
	private Contract contract;
	private List<Service> services = new ArrayList<Service>();
    private List<ContractAdjustment> adjustments = new ArrayList<ContractAdjustment>();
    
    public SDMCustomerExportWrapper() {}
    
    public SDMCustomerExportWrapper(Customer customer, Contract contract, Integer month, String year, String tzid) {
		super();
		this.month = month;
		this.year = year;
		this.tzid = tzid;
		this.customer = customer;
		this.contract = contract;
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

	public Integer getMonth() {
		return month;
	}

	public void setMonth(Integer month) {
		this.month = month;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public String getTzid() {
		return tzid;
	}

	public void setTzid(String tzid) {
		this.tzid = tzid;
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

	public List<Service> getServices() {
		return services;
	}

	public void setServices(List<Service> services) {
		this.services = services;
	}

	public List<ContractAdjustment> getAdjustments() {
		return adjustments;
	}

	public void setAdjustments(List<ContractAdjustment> adjustments) {
		this.adjustments = adjustments;
	}
	
	
	
}
