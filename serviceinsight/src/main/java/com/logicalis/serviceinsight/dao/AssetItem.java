package com.logicalis.serviceinsight.dao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Comparator;

/**
 * Discreet assets contributing to costs, including fields for amortizing,
 * "life", etc...
 *
 * @author poneil
 */
public class AssetItem extends BaseDao {

    private Long id;
    private String name;
    private String description;
    private BigDecimal amount;
    private Integer quantity;
    private Integer life;
    private String partNumber;
    private String sku;
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="MMddyyyy", timezone="America/New_York")
    private Date acquired;
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="MMddyyyy", timezone="America/New_York")
    private Date disposal;
    private Long customerId;
    private Long contractId;
    private Integer locationId;
    private Expense expense;
    private List<AssetCostFraction> assetCostFractions = new ArrayList<AssetCostFraction>();

    public AssetItem() {
    }

    public AssetItem(String name, String description, BigDecimal amount,
            Integer quantity, Integer life, String partNumber, String sku, Date acquired, Date disposal,
            Long customerId, Long contractId, Integer locationId) {
        this.name = name;
        this.description = description;
        this.amount = amount;
        this.quantity = quantity;
        this.life = life;
        this.partNumber = partNumber;
        this.sku = sku;
        this.acquired = acquired;
        this.disposal = disposal;
        this.customerId = customerId;
        this.contractId = contractId;
        this.locationId = locationId;
    }

    public AssetItem(Long id, String name, String description, BigDecimal amount,
            Integer quantity, Integer life, String partNumber, String sku, Date acquired, Date disposal,
            Long customerId, Long contractId, Integer locationId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.amount = amount;
        this.quantity = quantity;
        this.life = life;
        this.partNumber = partNumber;
        this.sku = sku;
        this.acquired = acquired;
        this.disposal = disposal;
        this.customerId = customerId;
        this.contractId = contractId;
        this.locationId = locationId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getLife() {
        return life;
    }

    public void setLife(Integer life) {
        this.life = life;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
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

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getContractId() {
        return contractId;
    }

    public void setContractId(Long contractId) {
        this.contractId = contractId;
    }

    public Integer getLocationId() {
        return locationId;
    }

    public void setLocationId(Integer locationId) {
        this.locationId = locationId;
    }

    public Expense getExpense() {
        return expense;
    }

    public void setExpense(Expense expense) {
        this.expense = expense;
    }

    public List<AssetCostFraction> getAssetCostFractions() {
        return assetCostFractions;
    }

    public void setAssetCostFractions(List<AssetCostFraction> assetCostFractions) {
        this.assetCostFractions = assetCostFractions;
    }
    
    public void addAssetCostFraction(AssetCostFraction assetCostFraction) {
        this.assetCostFractions.add(assetCostFraction);
    }
    
    /**
     * Allows for the assignment of an asset item to fractional asset categories,
     * such as an X86 Server Item fractionally split between assets X86 RAM and X86 CPU.
     * 
     * @author poneil
     */
    public static class AssetCostFraction {
        private ExpenseCategory expenseCategory;
        private BigDecimal fraction;
        private BigDecimal targetUtilization;
        private Integer quantity;

        public ExpenseCategory getExpenseCategory() {
			return expenseCategory;
		}

		public void setExpenseCategory(ExpenseCategory expenseCategory) {
			this.expenseCategory = expenseCategory;
		}

		public BigDecimal getFraction() {
            return fraction;
        }

        public void setFraction(BigDecimal fraction) {
            this.fraction = fraction;
        }

        public BigDecimal getTargetUtilization() {
            return targetUtilization;
        }

        public void setTargetUtilization(BigDecimal targetUtilization) {
            this.targetUtilization = targetUtilization;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        @Override
        public String toString() {
            return "AssetCostFraction{" + "expenseCategory=" + expenseCategory + ", fraction=" + fraction + ", targetUtilization=" + targetUtilization + ", quantity=" + quantity + '}';
        }
    }

    @Override
    public String toString() {
        return "AssetItem{" + "id=" + id + ", name=" + name + ", description=" + description + ", amount=" + amount + ", quantity=" + quantity + ", life=" + life + ", partNumber=" + partNumber + ", sku=" + sku + ", acquired=" + acquired + ", disposal=" + disposal + ", customerId=" + customerId + ", contractId=" + contractId + ", locationId=" + locationId + ", expense=" + expense + ", assetCostFractions=" + assetCostFractions + '}';
    }
    
    public static class AssetItemBasicComparator implements Comparator<AssetItem> {

        @Override
        /**
         * satisfies the previous comparison made via SQL.
         * no fields should require null checks.
         */
        public int compare(AssetItem o1, AssetItem o2) {
            int result = o1.getName().compareTo(o2.getName());
            if (result != 0) {
                return result;
            } else {
                return o1.getCreated().compareTo(o2.getCreated());
            }
        }
    }
}
