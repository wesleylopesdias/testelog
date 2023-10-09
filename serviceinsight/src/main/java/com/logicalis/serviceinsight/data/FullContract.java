package com.logicalis.serviceinsight.data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.logicalis.serviceinsight.data.ContractAdjustment.AdjustmentType;
import com.logicalis.serviceinsight.data.ContractInvoice.Status;

public class FullContract extends Contract {

	BigDecimal currentMonthTotalRecurringRevenue;
	BigDecimal currentMonthTotalOnetimeRevenue;
	private List<Service> contractServices = new ArrayList<Service>();
	private List<ContractAdjustment> contractAdjustments = new ArrayList<ContractAdjustment>();
	private static final String TZID = "America/New_York";
	
	public FullContract(){}
    
    public FullContract(Contract contract) {
    	this.customerName = contract.getCustomerName();
        this.customerId = contract.getCustomerId();
        this.id = contract.getId();
        this.altId = contract.getAltId();
        this.jobNumber = contract.getJobNumber();
        this.name = contract.getName();
        this.engagementManager = contract.getEngagementManager();
        this.accountExec = contract.getAccountExec();
        this.accountExecutive = contract.getAccountExecutive();
        this.serviceDeliveryManagers = contract.getServiceDeliveryManagers();
        this.enterpriseProgramExecutive = contract.getEnterpriseProgramExecutive();
        this.businessSolutionsConsultants = contract.getBusinessSolutionsConsultants();
        this.serviceCount = contract.getServiceCount();
        this.signedDate = contract.getSignedDate();
        this.serviceStartDate = contract.getServiceStartDate();
        this.startDate = contract.getStartDate();
        this.endDate = contract.getEndDate();
        this.archived = contract.getArchived();
        this.renewalStatus = contract.getRenewalStatus();
        this.renewalChange = contract.getRenewalChange();
    }
	
	public BigDecimal getCurrentMonthTotalRecurringRevenue() {
		BigDecimal currentTotalRecurring = new BigDecimal(0);
		DateTime currentMonthStart = new DateTime().withDayOfMonth(1).withTimeAtStartOfDay().withZone(DateTimeZone.forID(TZID));
		DateTime currentMonthEnd = new DateTime().dayOfMonth().withMaximumValue().withTimeAtStartOfDay().plusHours(23).plusMinutes(59).plusSeconds(59).withZone(DateTimeZone.forID(TZID));
		
		boolean svcNotActive = false;
		for(Service service : contractServices) {
		    svcNotActive = ((service.getStatus()!=null) && (!service.getStatus().equals(Service.Status.active)));
		    if (!svcNotActive) {
                DateTime serviceStartDate = new DateTime(service.getStartDate());
                DateTime serviceEndDate = new DateTime(service.getEndDate());
                
                service.forMonth(currentMonthStart);
                if((serviceStartDate.isBefore(currentMonthStart) || serviceStartDate.isEqual(currentMonthStart) || (serviceStartDate.isAfter(currentMonthStart) && serviceStartDate.isBefore(currentMonthEnd))) &&
                    (serviceEndDate.isAfter(currentMonthStart) && (serviceEndDate.isBefore(currentMonthEnd) || serviceEndDate.isEqual(currentMonthEnd)) || serviceEndDate.isAfter(currentMonthEnd) || serviceEndDate.isEqual(currentMonthStart))) {
                    currentTotalRecurring = currentTotalRecurring.add(service.getRecurringRevenue());
                }
		    }
		}
		
		boolean adjNotActive = false;
		for(ContractAdjustment adjustment : contractAdjustments) {
			adjNotActive = ((adjustment.getStatus()!=null) && (!adjustment.getStatus().equals(Service.Status.active)));
			if(!adjNotActive) {
				DateTime adjustmentStartDate = new DateTime(adjustment.getStartDate());
				DateTime adjustmentEndDate = new DateTime(adjustment.getEndDate());
				
				adjustment.forMonth(currentMonthStart);
				if((adjustmentStartDate.isBefore(currentMonthStart) || adjustmentStartDate.isEqual(currentMonthStart) || (adjustmentStartDate.isAfter(currentMonthStart) && adjustmentStartDate.isBefore(currentMonthEnd))) &&
					(adjustmentEndDate.isAfter(currentMonthStart) && (adjustmentEndDate.isBefore(currentMonthEnd) || adjustmentEndDate.isEqual(currentMonthEnd)) || adjustmentEndDate.isAfter(currentMonthEnd))) {
					if(AdjustmentType.recurring.equals(AdjustmentType.valueOf(adjustment.getAdjustmentType()))) {
						currentTotalRecurring = currentTotalRecurring.add(adjustment.getAdjustment());
					}
				}
			}
		}
		
		return currentTotalRecurring;
	}

	public BigDecimal getCurrentMonthTotalOnetimeRevenue() {
		BigDecimal currentTotalOnetime = new BigDecimal(0);
		DateTime currentMonthStart = new DateTime().withDayOfMonth(1).withTimeAtStartOfDay().withZone(DateTimeZone.forID(TZID));
		DateTime currentMonthEnd = new DateTime().dayOfMonth().withMaximumValue().withTimeAtStartOfDay().plusHours(23).plusMinutes(59).plusSeconds(59).withZone(DateTimeZone.forID(TZID));
		
		boolean svcNotActive = false;
		for(Service service : contractServices) {
			svcNotActive = ((service.getStatus()!=null) && (!service.getStatus().equals(Service.Status.active)));
		    if (!svcNotActive) {
				DateTime serviceStartDate = new DateTime(service.getStartDate());
				
				if(serviceStartDate.equals(currentMonthStart) || (serviceStartDate.isAfter(currentMonthStart) && serviceStartDate.isBefore(currentMonthEnd))) {
					currentTotalOnetime = currentTotalOnetime.add(service.getOnetimeRevenue());
				}
		    }
		}
		
		boolean adjNotActive = false;
		for(ContractAdjustment adjustment : contractAdjustments) {
			adjNotActive = ((adjustment.getStatus()!=null) && (!adjustment.getStatus().equals(Service.Status.active)));
			if(!adjNotActive) {
				DateTime adjustmentStartDate = new DateTime(adjustment.getStartDate());
				
				if(adjustmentStartDate.equals(currentMonthStart) || (adjustmentStartDate.isAfter(currentMonthStart) && adjustmentStartDate.isBefore(currentMonthEnd))) {
					if(AdjustmentType.onetime.equals(AdjustmentType.valueOf(adjustment.getAdjustmentType()))) {
						currentTotalOnetime = currentTotalOnetime.add(adjustment.getAdjustment());
					}
				}
			}
		}
		
		return currentTotalOnetime;
	}
	
	public void addContractService(Service contractService) {
        this.contractServices.add(contractService);
    }

	public List<Service> getContractServices() {
		return contractServices;
	}

	public void setContractServices(List<Service> contractServices) {
		this.contractServices = contractServices;
	}

	public List<ContractAdjustment> getContractAdjustments() {
        return contractAdjustments;
    }

    public void setContractAdjustments(List<ContractAdjustment> contractAdjustments) {
        this.contractAdjustments = contractAdjustments;
    }
    
    public void addContractAdjustment(ContractAdjustment contractAdjustment) {
        this.contractAdjustments.add(contractAdjustment);
    }
	
}
