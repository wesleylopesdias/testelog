package com.logicalis.serviceinsight.data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import com.logicalis.serviceinsight.dao.Expense;
import com.logicalis.serviceinsight.dao.AssetItem.AssetCostFraction;
import com.logicalis.serviceinsight.dao.ExpenseType;
import com.logicalis.serviceinsight.dao.Location;
import com.logicalis.serviceinsight.service.ServiceException;

public class AssetImportRecordHolder {
	
    private String name;
    private String description;
    private BigDecimal amount;
    private Integer life;
    private String assetNumber;
    private String sku;
    private Date acquired;
    private Date disposal;
    private String customerName;
    private Customer customer;
    private String contractJobNumber;
    private Contract contract;
    private String locationName;
    private Location location;
    private String costFractionCategoryOne;
    private Integer costFractionUnitOne;
    private BigDecimal costFractionPercentOne;
    private String costFractionCategoryTwo;
    private Integer costFractionUnitTwo;
    private BigDecimal costFractionPercentTwo;
    private String costFractionCategoryThree;
    private Integer costFractionUnitThree;
    private BigDecimal costFractionPercentThree;
    private String costFractionCategoryFour;
    private Integer costFractionUnitFour;
    private BigDecimal costFractionPercentFour;
    private String costFractionCategoryFive;
    private Integer costFractionUnitFive;
    private BigDecimal costFractionPercentFive;
    private List<AssetCostFraction> costFractions = new ArrayList<AssetCostFraction>();
    
    public AssetImportRecordHolder() {
    }
    
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
	public Integer getLife() {
		return life;
	}
	public void setLife(Integer life) {
		this.life = life;
	}
	public String getAssetNumber() {
		return assetNumber;
	}
	public void setAssetNumber(String assetNumber) {
		this.assetNumber = assetNumber;
	}
	public String getSku() {
		return sku;
	}
	public void setSku(String sku) {
		this.sku = sku;
	}
	public Date getAcquired() {
		return acquired;
	}
	public void setAcquired(Date acquired) {
		this.acquired = acquired;
	}
	public Date getDisposal() {
		return disposal;
	}
	public void setDisposal(Date disposal) {
		this.disposal = disposal;
	}
	public String getCustomerName() {
		return customerName;
	}
	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}
	public String getContractJobNumber() {
		return contractJobNumber;
	}
	public void setContractJobNumber(String contractJobNumber) {
		this.contractJobNumber = contractJobNumber;
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
	public String getCostFractionCategoryOne() {
		return costFractionCategoryOne;
	}

	public void setCostFractionCategoryOne(String costFractionCategoryOne) {
		this.costFractionCategoryOne = costFractionCategoryOne;
	}

	public Integer getCostFractionUnitOne() {
		return costFractionUnitOne;
	}

	public void setCostFractionUnitOne(Integer costFractionUnitOne) {
		this.costFractionUnitOne = costFractionUnitOne;
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

	public Integer getCostFractionUnitTwo() {
		return costFractionUnitTwo;
	}

	public void setCostFractionUnitTwo(Integer costFractionUnitTwo) {
		this.costFractionUnitTwo = costFractionUnitTwo;
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

	public Integer getCostFractionUnitThree() {
		return costFractionUnitThree;
	}

	public void setCostFractionUnitThree(Integer costFractionUnitThree) {
		this.costFractionUnitThree = costFractionUnitThree;
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

	public Integer getCostFractionUnitFour() {
		return costFractionUnitFour;
	}

	public void setCostFractionUnitFour(Integer costFractionUnitFour) {
		this.costFractionUnitFour = costFractionUnitFour;
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

	public Integer getCostFractionUnitFive() {
		return costFractionUnitFive;
	}

	public void setCostFractionUnitFive(Integer costFractionUnitFive) {
		this.costFractionUnitFive = costFractionUnitFive;
	}

	public BigDecimal getCostFractionPercentFive() {
		return costFractionPercentFive;
	}

	public void setCostFractionPercentFive(BigDecimal costFractionPercentFive) {
		this.costFractionPercentFive = costFractionPercentFive;
	}

	public List<AssetCostFraction> getCostFractions() {
		return costFractions;
	}
	public void setCostFractions(List<AssetCostFraction> costFractions) {
		this.costFractions = costFractions;
	}
	public void addCostFraction(AssetCostFraction costFraction) {
        this.costFractions.add(costFraction);
    }
	
	
	
	public void validate(Integer record, MessageSource messageSource, Locale locale) throws ServiceException {
        if (StringUtils.isBlank(name)) {
            throw new ServiceException(messageSource.getMessage("import_validation_error_asset_name", new Object[]{record}, locale));
        } else if (name.length() > 255) {
            throw new ServiceException(messageSource.getMessage("import_validation_error_asset_name_length", new Object[]{record}, locale));
        }
        if (amount == null) {
            throw new ServiceException(messageSource.getMessage("import_validation_error_amount", new Object[]{record}, locale));
        }
        if (acquired == null) {
            throw new ServiceException(messageSource.getMessage("import_validation_error_date_acquired", new Object[]{record}, locale));
        }
        if (StringUtils.isBlank(costFractionCategoryOne)) {
            throw new ServiceException(messageSource.getMessage("import_validation_error_cost_category_one", new Object[]{record}, locale));
        }
        if (costFractionUnitOne == null) {
            throw new ServiceException(messageSource.getMessage("import_validation_error_cost_unit_one", new Object[]{record}, locale));
        }
        if (costFractionPercentOne == null) {
            throw new ServiceException(messageSource.getMessage("import_validation_error_cost_percent_one", new Object[]{record}, locale));
        }
        /*
        if (note != null && note.length() > 500) {
            throw new ServiceException(messageSource.getMessage("import_validation_error_note_length", new Object[]{record}, locale));
        }*/
    }
	
	public void validateCostFraction(AssetCostFraction costFraction, Integer row, MessageSource messageSource, Locale locale) throws ServiceException {
		//this validation assumes the Expense category has already been provided and found -- otherwise, we don't get to this validation level
		String name = "";
		if(costFraction.getExpenseCategory().getName() != null) name = costFraction.getExpenseCategory().getName();
		if(costFraction.getQuantity() == null || costFraction.getQuantity() == 0) {
			throw new ServiceException(messageSource.getMessage("import_validation_error_cost_fraction_units", new Object[]{name, row}, locale));
			//throw new ServiceException("Cost Category Units for the Category: " + name + " in Row: " + row + " is required and must be greater than zero.");
		}
		if(costFraction.getFraction() == null || costFraction.getFraction().setScale(2, RoundingMode.HALF_UP) == new BigDecimal(0).setScale(2, RoundingMode.HALF_UP)) {
			throw new ServiceException(messageSource.getMessage("import_validation_error_cost_fraction_percent", new Object[]{name, row}, locale));
			//throw new ServiceException("Cost Category Percent for the Category: " + name + " in Row: " + row + " is required and must be greater than zero.");
		}
	}
	
    @Override
    public String toString() {
        return "AssetImportRecordHolder{" + "name=" + name + ", description=" + description + ", amount=" + amount + ", life=" + life + ", assetNumber=" + assetNumber + ", sku=" + sku + ", acquired=" + acquired + ", disposal=" + disposal + ", customerName=" + customerName + ", contractName=" + contractJobNumber + ", location=" + locationName
        		+ ", costFractionCategoryOne=" + costFractionCategoryOne + ", costFractionUnitOne=" + costFractionUnitOne +  ", costFractionPercentOne=" + costFractionPercentOne
        		+ ", costFractionCategoryTwo=" + costFractionCategoryTwo + ", costFractionUnitTwo=" + costFractionUnitTwo +  ", costFractionPercentTwo=" + costFractionPercentTwo
        		+ ", costFractionCategoryThree=" + costFractionCategoryThree + ", costFractionUnitThree=" + costFractionUnitThree +  ", costFractionPercentThree=" + costFractionPercentThree
        		+ ", costFractionCategoryFour=" + costFractionCategoryFour + ", costFractionUnitFour=" + costFractionUnitFour +  ", costFractionPercentFour=" + costFractionPercentFour
        		+ ", costFractionCategoryFive=" + costFractionCategoryFive + ", costFractionUnitFive=" + costFractionUnitFive +  ", costFractionPercentFive=" + costFractionPercentFive + '}';
    }
	
}
