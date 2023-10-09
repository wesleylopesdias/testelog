package com.logicalis.serviceinsight.dao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Comparator;

/**
 * Discreet cost items, providing for fields like an applied / billing date
 * for something like a utility bill.
 *
 * @author poneil
 */
public class CostItem extends BaseDao {

    public enum CostType {
        
        general("General"), aws("AWS"), azure("Azure"), o365("O365"), spla("SPLA"), depreciated("Depreciated");
        private String description;
        
        CostType(String description){
            this.description = description;
        }
        public String getDescription() {
            return this.description;
        }
    }

    public enum CostSubType {
        
        dc_rent("DC Rent"), multi_tenant("Multi Tenant"), specific_expense("Specific Expense"), dedicated("Dedicated");
        private String description;
        
        CostSubType(String description){
            this.description = description;
        }
        public String getDescription() {
            return this.description;
        }
    }
	
    private Long id;
    private String name;
    private String description;
    private BigDecimal amount;
    private Integer quantity;
    private String partNumber;
    private Long deviceId;
    private Long splaId;
    private String sku;
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="MMddyyyy", timezone="America/New_York")
    private Date applied;
    private Long customerId;
    private String customerName;
    private Long contractId;
    private String contractName;
    private Integer locationId;
    private Expense expense;
    private Long costAllocationLineItemIdRef;
    private String azureCustomerName;
    private String azureInvoiceNo;
    private String azureSubscriptionNo;
    private String awsSubscriptionNo;
    private CostType costType;
    private CostSubType costSubType;
    private List<CostFraction> costFractions = new ArrayList<CostFraction>();

    public CostItem() {
    }

    public CostItem(String name, String description, BigDecimal amount,
            Integer quantity, String partNumber, String sku, Date applied, Long customerId, Long contractId,
            String contractName, Integer locationId, CostType costType) {
        this.name = name;
        this.description = description;
        this.amount = amount;
        this.quantity = quantity;
        this.partNumber = partNumber;
        this.sku = sku;
        this.applied = applied;
        this.customerId = customerId;
        this.contractId = contractId;
        this.contractName = contractName;
        this.locationId = locationId;
        this.costType = costType;
    }

    public CostItem(Long id, String name, String description, BigDecimal amount,
            Integer quantity, String partNumber, String sku, Date applied, Long customerId, String customerName, Long contractId,
            String contractName, Integer locationId, CostType costType) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.amount = amount;
        this.quantity = quantity;
        this.partNumber = partNumber;
        this.sku = sku;
        this.applied = applied;
        this.customerId = customerId;
        this.customerName = customerName;
        this.contractId = contractId;
        this.contractName = contractName;
        this.locationId = locationId;
        this.costType = costType;
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

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public Long getSplaId() {
        return splaId;
    }

    public void setSplaId(Long splaId) {
        this.splaId = splaId;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public Date getApplied() {
        return applied;
    }

    public void setApplied(Date applied) {
        this.applied = applied;
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

    public Long getContractId() {
        return contractId;
    }

    public void setContractId(Long contractId) {
        this.contractId = contractId;
    }

    public String getContractName() {
        return contractName;
    }

    public void setContractName(String contractName) {
        this.contractName = contractName;
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

    public Long getCostAllocationLineItemIdRef() {
        return costAllocationLineItemIdRef;
    }

    public void setCostAllocationLineItemIdRef(Long costAllocationLineItemIdRef) {
        this.costAllocationLineItemIdRef = costAllocationLineItemIdRef;
    }

    public String getAzureCustomerName() {
        return azureCustomerName;
    }

    public void setAzureCustomerName(String azureCustomerName) {
        this.azureCustomerName = azureCustomerName;
    }

    public String getAzureInvoiceNo() {
        return azureInvoiceNo;
    }

    public void setAzureInvoiceNo(String azureInvoiceNo) {
        this.azureInvoiceNo = azureInvoiceNo;
    }

    public String getAzureSubscriptionNo() {
        return azureSubscriptionNo;
    }

    public void setAzureSubscriptionNo(String azureSubscriptionNo) {
        this.azureSubscriptionNo = azureSubscriptionNo;
    }

    public String getAwsSubscriptionNo() {
        return awsSubscriptionNo;
    }

    public void setAwsSubscriptionNo(String awsSubscriptionNo) {
        this.awsSubscriptionNo = awsSubscriptionNo;
    }

    public CostType getCostType() {
        return costType;
    }

    public void setCostType(CostType costType) {
        this.costType = costType;
    }

    public CostSubType getCostSubType() {
        return costSubType;
    }

    public void setCostSubType(CostSubType costSubType) {
        this.costSubType = costSubType;
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

    @Override
    public String toString() {
        return "CostItem{" + "id=" + id + ", name=" + name + ", description=" + description + ", amount=" + amount + ", quantity=" + quantity + ", partNumber=" + partNumber + ", deviceId=" + deviceId + ", splaId=" + splaId + ", sku=" + sku + ", applied=" + applied + ", customerId=" + customerId + ", customerName=" + customerName + ", contractId=" + contractId + ", contractName=" + contractName + ", locationId=" + locationId + ", expense=" + expense + ", costAllocationLineItemIdRef=" + costAllocationLineItemIdRef + ", azureCustomerName=" + azureCustomerName + ", azureInvoiceNo=" + azureInvoiceNo + ", azureSubscriptionNo=" + azureSubscriptionNo + ", awsSubscriptionNo=" + awsSubscriptionNo + ", costType=" + costType + ", costSubType=" + costSubType + ", costFractions=" + costFractions + '}';
    }
    
    /**
     * Allows for the assignment of a cost item to fractional business unit users,
     * such as Cloud and Managed Services.
     * 
     * @author poneil
     */
    public static class CostFraction {
    	private ExpenseCategory expenseCategory;
        private BigDecimal fraction;

        public BigDecimal getFraction() {
            return fraction;
        }

        public void setFraction(BigDecimal fraction) {
            this.fraction = fraction;
        }

        public ExpenseCategory getExpenseCategory() {
                return expenseCategory;
        }

        public void setExpenseCategory(ExpenseCategory expenseCategory) {
                this.expenseCategory = expenseCategory;
        }

        @Override
        public String toString() {
            return "CostFraction{" + "expenseCategory=" + expenseCategory + ", fraction=" + fraction + '}';
        }
    }
    
    public static class CostItemBasicComparator implements Comparator<CostItem> {

        @Override
        /**
         * satisfies the previous comparison made via SQL.
         * no fields should require null checks.
         */
        public int compare(CostItem o1, CostItem o2) {
            int result = o1.getName().compareTo(o2.getName());
            if (result != 0) {
                return result;
            } else {
                return o1.getCreated().compareTo(o2.getCreated());
            }
        }
    }
}
