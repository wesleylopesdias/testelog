package com.logicalis.serviceinsight.data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.MessageSource;

import com.logicalis.serviceinsight.dao.AssetItem.AssetCostFraction;
import com.logicalis.serviceinsight.dao.CostItem.CostFraction;
import com.logicalis.serviceinsight.dao.ExpenseType;
import com.logicalis.serviceinsight.dao.Location;
import com.logicalis.serviceinsight.service.ServiceException;

public class CostImportRecordHolder {
	
    private String name;
    private String description;
    private BigDecimal amount;
    private Date expenseDate;
    private String customerName;
    private Customer customer;
    private String contractJobNumber;
    private Contract contract;
    private String locationName;
    private Location location;
    private String costFractionCategoryOne;
    private BigDecimal costFractionPercentOne;
    private String costFractionCategoryTwo;
    private BigDecimal costFractionPercentTwo;
    private String costFractionCategoryThree;
    private BigDecimal costFractionPercentThree;
    private String costFractionCategoryFour;
    private BigDecimal costFractionPercentFour;
    private String costFractionCategoryFive;
    private BigDecimal costFractionPercentFive;
    private List<CostFraction> costFractions = new ArrayList<CostFraction>();
    
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	public Date getExpenseDate() {
		return expenseDate;
	}
	public void setExpenseDate(Date expenseDate) {
		this.expenseDate = expenseDate;
	}
	public String getCustomerName() {
		return customerName;
	}
	public void setCustomerName(String customerName) {
		this.customerName = customerName;
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
	public Contract getContract() {
		return contract;
	}
	public void setContract(Contract contract) {
		this.contract = contract;
	}
	public String getLocationName() {
		return locationName;
	}
	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}
	public Location getLocation() {
		return location;
	}
	public void setLocation(Location location) {
		this.location = location;
	}
	public String getCostFractionCategoryOne() {
		return costFractionCategoryOne;
	}
	public void setCostFractionCategoryOne(String costFractionCategoryOne) {
		this.costFractionCategoryOne = costFractionCategoryOne;
	}
	public BigDecimal getCostFractionPercentOne() {
		return costFractionPercentOne;
	}
	public void setCostFractionPercentOne(BigDecimal costFractionPercentOne) {
		this.costFractionPercentOne = costFractionPercentOne;
	}
	public String getCostFractionCategoryTwo() {
		return costFractionCategoryTwo;
	}
	public void setCostFractionCategoryTwo(String costFractionCategoryTwo) {
		this.costFractionCategoryTwo = costFractionCategoryTwo;
	}
	public BigDecimal getCostFractionPercentTwo() {
		return costFractionPercentTwo;
	}
	public void setCostFractionPercentTwo(BigDecimal costFractionPercentTwo) {
		this.costFractionPercentTwo = costFractionPercentTwo;
	}
	public String getCostFractionCategoryThree() {
		return costFractionCategoryThree;
	}
	public void setCostFractionCategoryThree(String costFractionCategoryThree) {
		this.costFractionCategoryThree = costFractionCategoryThree;
	}
	public BigDecimal getCostFractionPercentThree() {
		return costFractionPercentThree;
	}
	public void setCostFractionPercentThree(BigDecimal costFractionPercentThree) {
		this.costFractionPercentThree = costFractionPercentThree;
	}
	public String getCostFractionCategoryFour() {
		return costFractionCategoryFour;
	}
	public void setCostFractionCategoryFour(String costFractionCategoryFour) {
		this.costFractionCategoryFour = costFractionCategoryFour;
	}
	public BigDecimal getCostFractionPercentFour() {
		return costFractionPercentFour;
	}
	public void setCostFractionPercentFour(BigDecimal costFractionPercentFour) {
		this.costFractionPercentFour = costFractionPercentFour;
	}
	public String getCostFractionCategoryFive() {
		return costFractionCategoryFive;
	}
	public void setCostFractionCategoryFive(String costFractionCategoryFive) {
		this.costFractionCategoryFive = costFractionCategoryFive;
	}
	public BigDecimal getCostFractionPercentFive() {
		return costFractionPercentFive;
	}
	public void setCostFractionPercentFive(BigDecimal costFractionPercentFive) {
		this.costFractionPercentFive = costFractionPercentFive;
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
	
	
	public void validate(Integer record, MessageSource messageSource, Locale locale) throws ServiceException {
        if (StringUtils.isBlank(name)) {
            throw new ServiceException(messageSource.getMessage("import_validation_error_cost_name", new Object[]{record}, locale));
        } else if (name.length() > 255) {
            throw new ServiceException(messageSource.getMessage("import_validation_error_cost_name_length", new Object[]{record}, locale));
        }
        if (amount == null) {
            throw new ServiceException(messageSource.getMessage("import_validation_error_amount", new Object[]{record}, locale));
        }
        if (expenseDate == null) {
            throw new ServiceException(messageSource.getMessage("import_validation_error_cost_date", new Object[]{record}, locale));
        }
        if (StringUtils.isBlank(costFractionCategoryOne)) {
            throw new ServiceException(messageSource.getMessage("import_validation_error_cost_category_one", new Object[]{record}, locale));
        }
        if (costFractionPercentOne == null) {
            throw new ServiceException(messageSource.getMessage("import_validation_error_cost_percent_one", new Object[]{record}, locale));
        }
        /*
        if (note != null && note.length() > 500) {
            throw new ServiceException(messageSource.getMessage("import_validation_error_note_length", new Object[]{record}, locale));
        }*/
    }
	
	public void validateCostFraction(CostFraction costFraction, Integer row, MessageSource messageSource, Locale locale) throws ServiceException {
		//this validation assumes the Expense category has already been provided and found -- otherwise, we don't get to this validation level
		String name = "";
		if(costFraction.getExpenseCategory().getName() != null) name = costFraction.getExpenseCategory().getName();
		if(costFraction.getFraction() == null || costFraction.getFraction().setScale(2, RoundingMode.HALF_UP) == new BigDecimal(0).setScale(2, RoundingMode.HALF_UP)) {
			throw new ServiceException(messageSource.getMessage("import_validation_error_cost_fraction_percent", new Object[]{name, row}, locale));
		}
	}
	
	
	@Override
	public String toString() {
		return "CostImportRecordHolder [name=" + name + ", description="
				+ description + ", amount=" + amount + ", expenseDate="
				+ expenseDate + ", customerName=" + customerName
				+ ", customer=" + customer + ", contractJobNumber="
				+ contractJobNumber + ", contract=" + contract
				+ ", locationName=" + locationName + ", location=" + location
				+ ", costFractionCategoryOne=" + costFractionCategoryOne
				+ ", costFractionPercentOne=" + costFractionPercentOne
				+ ", costFractionCategoryTwo=" + costFractionCategoryTwo
				+ ", costFractionPercentTwo=" + costFractionPercentTwo
				+ ", costFractionCategoryThree=" + costFractionCategoryThree
				+ ", costFractionPercentThree=" + costFractionPercentThree
				+ ", costFractionCategoryFour=" + costFractionCategoryFour
				+ ", costFractionPercentFour=" + costFractionPercentFour
				+ ", costFractionCategoryFive=" + costFractionCategoryFive
				+ ", costFractionPercentFive=" + costFractionPercentFive
				+ ", costFractions=" + costFractions + "]";
	}
	
}
