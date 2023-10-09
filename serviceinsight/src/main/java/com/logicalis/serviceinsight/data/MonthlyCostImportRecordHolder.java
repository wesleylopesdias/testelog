package com.logicalis.serviceinsight.data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.springframework.context.MessageSource;

import com.logicalis.serviceinsight.dao.ExpenseCategory;
import com.logicalis.serviceinsight.dao.CostItem.CostFraction;
import com.logicalis.serviceinsight.service.ServiceException;

public class MonthlyCostImportRecordHolder {

	public static final String CATEGORY_TYPE_MS_GENERAL = "MS-General";
	public static final String CATEGORY_TYPE_MS_CLIENT_SPECIFIC = "MS-Client Specific";
	public static final String CATEGORY_TYPE_CLOUD_GENERAL = "Cloud";
	public static final String CATEGORY_TYPE_CLOUD_CLIENT_SPECIFIC = "Cloud-Customer Specific";
	
	private String thirdParty;
    private String cc;
    private String approver;
    private BigDecimal amount;
    private String invoiceNumber;
    private String poNumber;
    private String amountType;
    private String description;
    private String category;
    private String customerColumn;
    private String siCategoryColumn;
    private String ospServiceColumn;
    private String notes;
    private Customer customer;
    private String contractJobNumber;
    private Contract contract;
    private Boolean unallocatedExpense = Boolean.FALSE;
    private String costSubType;
    private List<CostFraction> costFractions = new ArrayList<CostFraction>();
    private List<ServiceFraction> serviceFractions = new ArrayList<ServiceFraction>();
    
    public MonthlyCostImportRecordHolder(){}
    
    //used to clone the object in case there is more than one customer provided
	public MonthlyCostImportRecordHolder(MonthlyCostImportRecordHolder monthlyCostImportRecordHolder) {
		this.thirdParty = monthlyCostImportRecordHolder.getThirdParty();
		this.cc = monthlyCostImportRecordHolder.getCc();
		this.approver = monthlyCostImportRecordHolder.getApprover();
		this.amount = monthlyCostImportRecordHolder.getAmount();
		this.invoiceNumber = monthlyCostImportRecordHolder.getInvoiceNumber();
		this.poNumber = monthlyCostImportRecordHolder.getPoNumber();
		this.amountType = monthlyCostImportRecordHolder.getAmountType();
		this.description = monthlyCostImportRecordHolder.getDescription();
		this.category = monthlyCostImportRecordHolder.getCategory();
		this.customerColumn = monthlyCostImportRecordHolder.getCustomerColumn();
		this.siCategoryColumn = monthlyCostImportRecordHolder.getSiCategoryColumn();
		this.ospServiceColumn = monthlyCostImportRecordHolder.getOspServiceColumn();
		this.notes = monthlyCostImportRecordHolder.getNotes();
		this.customer = monthlyCostImportRecordHolder.getCustomer();
		this.contractJobNumber = monthlyCostImportRecordHolder.getContractJobNumber();
		this.contract = monthlyCostImportRecordHolder.getContract();
		this.unallocatedExpense = monthlyCostImportRecordHolder.getUnallocatedExpense();
		this.costFractions = monthlyCostImportRecordHolder.getCostFractions();
		this.serviceFractions = monthlyCostImportRecordHolder.getServiceFractions();
		this.costSubType = monthlyCostImportRecordHolder.getCostSubType();
	}

	public String getThirdParty() {
		return thirdParty;
	}
	
	public void setThirdParty(String thirdParty) {
		this.thirdParty = thirdParty;
	}
	
	public String getCc() {
		return cc;
	}
	
	public void setCc(String cc) {
		this.cc = cc;
	}
	
	public String getApprover() {
		return approver;
	}
	
	public void setApprover(String approver) {
		this.approver = approver;
	}
	
	public BigDecimal getAmount() {
		return amount;
	}
	
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	
	public String getInvoiceNumber() {
		return invoiceNumber;
	}
	
	public void setInvoiceNumber(String invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}
	
	public String getPoNumber() {
		return poNumber;
	}
	
	public void setPoNumber(String poNumber) {
		this.poNumber = poNumber;
	}
	
	public String getAmountType() {
		return amountType;
	}
	
	public void setAmountType(String amountType) {
		this.amountType = amountType;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getCategory() {
		return category;
	}
	
	public void setCategory(String category) {
		this.category = category;
	}
	
	public String getCustomerColumn() {
		return customerColumn;
	}
	
	public void setCustomerColumn(String customerColumn) {
		this.customerColumn = customerColumn;
	}
	
	public String getSiCategoryColumn() {
		return siCategoryColumn;
	}
	
	public void setSiCategoryColumn(String siCategoryColumn) {
		this.siCategoryColumn = siCategoryColumn;
	}
	
	public String getOspServiceColumn() {
		return ospServiceColumn;
	}

	public void setOspServiceColumn(String ospServiceColumn) {
		this.ospServiceColumn = ospServiceColumn;
	}

	public String getNotes() {
		return notes;
	}
	
	public void setNotes(String notes) {
		this.notes = notes;
	}
	
	public Customer getCustomer() {
		return customer;
	}
	
	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public String getContractJobNumber() {
		return contractJobNumber;
	}

	public void setContractJobNumber(String contractJobNumber) {
		this.contractJobNumber = contractJobNumber;
	}

	public Boolean getUnallocatedExpense() {
		return unallocatedExpense;
	}

	public void setUnallocatedExpense(Boolean unallocatedExpense) {
		this.unallocatedExpense = unallocatedExpense;
	}

	public String getCostSubType() {
		return costSubType;
	}

	public void setCostSubType(String costSubType) {
		this.costSubType = costSubType;
	}

	public Contract getContract() {
		return contract;
	}

	public void setContract(Contract contract) {
		this.contract = contract;
	}

	public List<CostFraction> getCostFractions() {
		return costFractions;
	}

	public void setCostFractions(List<CostFraction> costFractions) {
		this.costFractions = costFractions;
	}
	
	public void addCostFraction(CostFraction costFraction) {
        this.costFractions.add(costFraction);
    }
	
	public List<ServiceFraction> getServiceFractions() {
		return serviceFractions;
	}

	public void setServiceFractions(List<ServiceFraction> serviceFractions) {
		this.serviceFractions = serviceFractions;
	}
	
	public void addServiceFraction(ServiceFraction serviceFraction) {
        this.serviceFractions.add(serviceFraction);
    }

	public void validateCostFraction(CostFraction costFraction, Integer row, MessageSource messageSource, Locale locale) throws ServiceException {
		//this validation assumes the Expense category has already been provided and found -- otherwise, we don't get to this validation level
		String name = "";
		if(costFraction.getExpenseCategory().getName() != null) name = costFraction.getExpenseCategory().getName();
		if(costFraction.getFraction() == null || costFraction.getFraction().setScale(2, RoundingMode.HALF_UP) == new BigDecimal(0).setScale(2, RoundingMode.HALF_UP)) {
			throw new ServiceException(messageSource.getMessage("import_validation_error_cost_fraction_percent", new Object[]{name, row}, locale));
		}
	}
	
	public void validateServiceFraction(ServiceFraction serviceFraction, String serviceName, Integer row, MessageSource messageSource, Locale locale) throws ServiceException {
		//this validation assumes the Service has already been provided and found -- otherwise, we don't get to this validation level
		if(serviceFraction.getFraction() == null || serviceFraction.getFraction().setScale(2, RoundingMode.HALF_UP) == new BigDecimal(0).setScale(2, RoundingMode.HALF_UP)) {
			throw new ServiceException(messageSource.getMessage("import_validation_error_cost_fraction_percent", new Object[]{serviceName, row}, locale));
		}
	}
	
	public void validateCostFractions(List<CostFraction> costFractions, Integer row, MessageSource messageSource, Locale locale) throws ServiceException {
		//this validation assumes the Expense category has already been provided and found -- otherwise, we don't get to this validation level
		BigDecimal total = new BigDecimal(0);
		for(CostFraction fraction : costFractions) {
			total = total.add(fraction.getFraction());
		}
		
		//add up to 100%?
		if(!total.setScale(2, RoundingMode.HALF_UP).equals(new BigDecimal(100).setScale(2, RoundingMode.HALF_UP))) {
			throw new ServiceException(messageSource.getMessage("import_validation_error_cost_fraction_percent_total", new Object[]{row}, locale));
		}
	}

	@Override
	public String toString() {
		return "MonthlyCostImportRecordHolder [thirdParty=" + thirdParty
				+ ", cc=" + cc + ", approver=" + approver + ", amount="
				+ amount + ", invoiceNumber=" + invoiceNumber + ", poNumber="
				+ poNumber + ", amountType=" + amountType + ", description="
				+ description + ", category=" + category + ", customerColumn="
				+ customerColumn + ", siCategoryColumn=" + siCategoryColumn
				+ ", notes=" + notes + ", customer=" + customer
				+ ", contractJobNumber=" + contractJobNumber + ", contract="
				+ contract + ", costFractions=" + costFractions + "]";
	}
	
	public static class ServiceFraction {
    	private Long ospId;
        private BigDecimal fraction;

        public BigDecimal getFraction() {
            return fraction;
        }

        public void setFraction(BigDecimal fraction) {
            this.fraction = fraction;
        }

        public Long getOspId() {
			return ospId;
		}

		public void setOspId(Long ospId) {
			this.ospId = ospId;
		}

		@Override
        public String toString() {
            return "CostFraction{" + "ospId=" + ospId + ", fraction=" + fraction + '}';
        }
    }
    
}
