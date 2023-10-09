package com.logicalis.serviceinsight.representation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang.ObjectUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

/**
 * Consolidates query results for CostItems joined with other related
 * data, such as ExpenseCategories
 * 
 * @author poneil
 */
public class CostItemAnalysisWrapper {
    
    private DateTime month;
    private List<SummaryRecord> costsByTypeSubType = new ArrayList<SummaryRecord>();
    private List<SummaryRecordByCustomer> costsByTypeSubTypeAndCustomer = new ArrayList<SummaryRecordByCustomer>();
    private List<SummaryRecordByCostCategory> costsByTypeSubTypeAndCostCategory = new ArrayList<SummaryRecordByCostCategory>();

    public DateTime getMonth() {
        return month;
    }
    
    public String getMonthFormatted() {
        return (month == null ? null : DateTimeFormat.forPattern("MM/yyyy").print(this.month));
    }

    public void setMonth(DateTime month) {
        this.month = month;
    }

    public List<SummaryRecord> getCostsByTypeSubType() {
        return costsByTypeSubType;
    }

    public void setCostsByTypeSubType(List<SummaryRecord> costsByTypeSubType) {
        this.costsByTypeSubType = costsByTypeSubType;
    }
    
    public void addSummaryRecord(SummaryRecord record) {
        costsByTypeSubType.add(record);
    }

    public List<SummaryRecordByCustomer> getCostsByTypeSubTypeAndCustomer() {
        return costsByTypeSubTypeAndCustomer;
    }

    public void setCostsByTypeSubTypeAndCustomer(List<SummaryRecordByCustomer> costsByTypeSubTypeAndCustomer) {
        this.costsByTypeSubTypeAndCustomer = costsByTypeSubTypeAndCustomer;
    }

    public List<SummaryRecordByCostCategory> getCostsByTypeSubTypeAndCostCategory() {
        return costsByTypeSubTypeAndCostCategory;
    }

    public void setCostsByTypeSubTypeAndCostCategory(List<SummaryRecordByCostCategory> costsByTypeSubTypeAndCostCategory) {
        this.costsByTypeSubTypeAndCostCategory = costsByTypeSubTypeAndCostCategory;
    }
    
    public static class SummaryRecord {
        
        private BigDecimal cost = BigDecimal.ZERO;
        private String costType;
        private String costSubType;
        
        public SummaryRecord(BigDecimal cost, String costType, String costSubType) {
            this.cost = cost;
            this.costType = costType;
            this.costSubType = costSubType;
        }

        public BigDecimal getCost() {
            return cost;
        }
        
        public String getFormattedCost() {
            return (cost == null ? "0.00" : cost.setScale(2, RoundingMode.HALF_UP).toPlainString());
        }

        public void setCost(BigDecimal cost) {
            this.cost = cost;
        }
        
        public String getKey() {
            if (costSubType == null) {
                return costType;
            } else {
                return costType + "_" + costSubType;
            }
        }

        public String getCostType() {
            return costType;
        }

        public void setCostType(String costType) {
            this.costType = costType;
        }

        public String getCostSubType() {
            return costSubType;
        }

        public void setCostSubType(String costSubType) {
            this.costSubType = costSubType;
        }
    }
    
    public static class SummaryRecordByCustomer implements Comparable<SummaryRecordByCustomer> {
        
        private Long customerId;
        private String customer;
        private BigDecimal cost = BigDecimal.ZERO;
        private String costType;
        private String costSubType;
        
        public SummaryRecordByCustomer(Long customerId, String customer, BigDecimal cost, String costType, String costSubType) {
            
            this.customerId = customerId;
            this.customer = customer;
            this.cost = cost;
            this.costType = costType;
            this.costSubType = costSubType;
        }

        public Long getCustomerId() {
            return customerId;
        }

        public void setCustomerId(Long customerId) {
            this.customerId = customerId;
        }

        public String getCustomer() {
            return customer;
        }

        public void setCustomer(String customer) {
            this.customer = customer;
        }

        public BigDecimal getCost() {
            return cost;
        }
        
        public String getFormattedCost() {
            return (cost == null ? "0.00" : cost.setScale(2, RoundingMode.HALF_UP).toPlainString());
        }

        public void setCost(BigDecimal cost) {
            this.cost = cost;
        }
        
        public String getKey() {
            if (costSubType == null) {
                return costType;
            } else {
                return costType + "_" + costSubType;
            }
        }

        public String getCostType() {
            return costType;
        }

        public void setCostType(String costType) {
            this.costType = costType;
        }

        public String getCostSubType() {
            return costSubType;
        }

        public void setCostSubType(String costSubType) {
            this.costSubType = costSubType;
        }

        @Override
        public int compareTo(SummaryRecordByCustomer o) {
            int idx = ObjectUtils.compare(this.costType, o.getCostType());
            if (idx != 0) {
                return idx;
            }
            idx = ObjectUtils.compare(this.costSubType, o.getCostSubType());
            if (idx != 0) {
                return idx;
            }
            return ObjectUtils.compare(this.customer, o.getCustomer());
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 59 * hash + Objects.hashCode(this.customerId);
            hash = 59 * hash + Objects.hashCode(this.costType);
            hash = 59 * hash + Objects.hashCode(this.costSubType);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final SummaryRecordByCustomer other = (SummaryRecordByCustomer) obj;
            if (!Objects.equals(this.costType, other.costType)) {
                return false;
            }
            if (!Objects.equals(this.costSubType, other.costSubType)) {
                return false;
            }
            if (!Objects.equals(this.customerId, other.customerId)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "SummaryRecordByCustomer{" + "customerId=" + customerId + ", customer=" + customer + ", cost=" + cost + ", costType=" + costType + ", costSubType=" + costSubType + '}';
        }
    }
    
    public static class SummaryRecordByCostCategory {
        
        private String parentCostCategory;
        private String costCategory;
        private Long costCategoryId;
        private BigDecimal cost = BigDecimal.ZERO;
        private String costType;
        private String costSubType;
        
        public SummaryRecordByCostCategory(String parentCostCategory, String costCategory, Long costCategoryId, BigDecimal cost, String costType, String costSubType) {
            
            this.parentCostCategory = parentCostCategory;
            this.costCategory = costCategory;
            this.costCategoryId = costCategoryId;
            this.cost = cost;
            this.costType = costType;
            this.costSubType = costSubType;
        }

        public String getParentCostCategory() {
            return parentCostCategory;
        }

        public void setParentCostCategory(String parentCostCategory) {
            this.parentCostCategory = parentCostCategory;
        }

        public String getCostCategory() {
            return costCategory;
        }

        public void setCostCategory(String costCategory) {
            this.costCategory = costCategory;
        }

        public Long getCostCategoryId() {
            return costCategoryId;
        }

        public void setCostCategoryId(Long costCategoryId) {
            this.costCategoryId = costCategoryId;
        }

        public BigDecimal getCost() {
            return cost;
        }
        
        public String getFormattedCost() {
            return (cost == null ? "0.00" : cost.setScale(2, RoundingMode.HALF_UP).toPlainString());
        }

        public void setCost(BigDecimal cost) {
            this.cost = cost;
        }
        
        public String getKey() {
            if (costSubType == null) {
                return costType;
            } else {
                return costType + "_" + costSubType;
            }
        }

        public String getCostType() {
            return costType;
        }

        public void setCostType(String costType) {
            this.costType = costType;
        }

        public String getCostSubType() {
            return costSubType;
        }

        public void setCostSubType(String costSubType) {
            this.costSubType = costSubType;
        }
    }
}
