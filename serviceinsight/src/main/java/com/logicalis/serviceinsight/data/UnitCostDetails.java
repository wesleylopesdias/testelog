package com.logicalis.serviceinsight.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.logicalis.serviceinsight.dao.UnitCost;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

/**
 *
 * @author poneil
 */
public class UnitCostDetails {
    
    private UnitCost unitCost;
    private List<AssetDetail> assets = new ArrayList<AssetDetail>();
    private List<CostDetail> costs = new ArrayList<CostDetail>();
    private List<LaborDetail> labor = new ArrayList<LaborDetail>();

    public UnitCost getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(UnitCost unitCost) {
        this.unitCost = unitCost;
    }

    public List<AssetDetail> getAssets() {
        return assets;
    }

    public void setAssets(List<AssetDetail> assets) {
        this.assets = assets;
    }
    
    public void addAssetDetail(AssetDetail detail) {
        if (this.assets == null) {
            this.assets = new ArrayList<AssetDetail>();
        }
        this.assets.add(detail);
    }

    public List<CostDetail> getCosts() {
        return costs;
    }

    public void setCosts(List<CostDetail> costs) {
        this.costs = costs;
    }
    
    public void addCostDetail(CostDetail detail) {
        if (this.costs == null) {
            this.costs = new ArrayList<CostDetail>();
        }
        this.costs.add(detail);
    }

    public List<LaborDetail> getLabor() {
        return labor;
    }

    public void setLabor(List<LaborDetail> labor) {
        this.labor = labor;
    }
    
    public void addLaborDetail(LaborDetail detail) {
        if (this.labor == null) {
            this.labor = new ArrayList<LaborDetail>();
        }
        this.labor.add(detail);
    }

    public static class AssetDetail implements Comparable<AssetDetail> {

        private String customer;
        private String description;
        private DateTime acquiredDate;
        private BigDecimal depreciation;

        public String getCustomer() {
            return customer;
        }

        public void setCustomer(String customer) {
            this.customer = customer;
        }
        
        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        @JsonIgnore
        public DateTime getAcquiredDate() {
            return acquiredDate;
        }
    
        public String getAcquiredDateShort() {
            if (this.acquiredDate != null) {
                return DateTimeFormat.forPattern("yyyy-MM-dd").print(acquiredDate);
            }
            return null;
        }

        public void setAcquiredDate(DateTime acquiredDate) {
            this.acquiredDate = acquiredDate;
        }

        public BigDecimal getDepreciation() {
            return depreciation;
        }

        public BigDecimal getDepreciationFormatted() {
            return (depreciation == null ? BigDecimal.ZERO : depreciation.setScale(2, RoundingMode.HALF_UP));
        }

        public void setDepreciation(BigDecimal depreciation) {
            this.depreciation = depreciation;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 97 * hash + Objects.hashCode(this.customer);
            hash = 97 * hash + Objects.hashCode(this.description);
            hash = 97 * hash + Objects.hashCode(this.acquiredDate);
            hash = 97 * hash + Objects.hashCode(this.depreciation);
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
            final AssetDetail other = (AssetDetail) obj;
            if (!Objects.equals(this.customer, other.customer)) {
                return false;
            }
            if (!Objects.equals(this.description, other.description)) {
                return false;
            }
            if (!Objects.equals(this.acquiredDate, other.acquiredDate)) {
                return false;
            }
            if (!Objects.equals(this.depreciation, other.depreciation)) {
                return false;
            }
            return true;
        }

        @Override
        public int compareTo(AssetDetail o) {
            if (this == o) {
                return 0;
            }
            if (acquiredDate != o.acquiredDate) {
                if (acquiredDate != null && o.acquiredDate != null) {
                    int idx = acquiredDate.compareTo(o.acquiredDate);
                    if (idx != 0) {
                        return idx;
                    }
                }
            }
            if (customer != o.customer) {
                if (customer != null && o.customer != null) {
                    int idx = customer.compareTo(o.customer);
                    if (idx != 0) {
                        return idx;
                    }
                }
            }
            if (description != o.description) {
                if (description != null && o.description != null) {
                    int idx = description.compareTo(o.description);
                    if (idx != 0) {
                        return idx;
                    }
                }
            }
            if (depreciation != o.depreciation) {
                if (depreciation != null && o.depreciation != null) {
                    int idx = depreciation.compareTo(o.depreciation);
                    if (idx != 0) {
                        return idx;
                    }
                }
            }
            return 0;
        }
    }
    
    public static class CostDetail implements Comparable<CostDetail> {

        private String customer;
        private String description;
        private DateTime appliedDate;
        private BigDecimal amount;

        public String getCustomer() {
            return customer;
        }

        public void setCustomer(String customer) {
            this.customer = customer;
        }
        
        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        @JsonIgnore
        public DateTime getAppliedDate() {
            return appliedDate;
        }
    
        public String getAppliedDateShort() {
            if (this.appliedDate != null) {
                return DateTimeFormat.forPattern("yyyy-MM-dd").print(appliedDate);
            }
            return null;
        }

        public void setAppliedDate(DateTime appliedDate) {
            this.appliedDate = appliedDate;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public BigDecimal getAmountFormatted() {
            return (amount == null ? BigDecimal.ZERO : amount.setScale(2, RoundingMode.HALF_UP));
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 97 * hash + Objects.hashCode(this.customer);
            hash = 97 * hash + Objects.hashCode(this.description);
            hash = 97 * hash + Objects.hashCode(this.appliedDate);
            hash = 97 * hash + Objects.hashCode(this.amount);
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
            final CostDetail other = (CostDetail) obj;
            if (!Objects.equals(this.customer, other.customer)) {
                return false;
            }
            if (!Objects.equals(this.description, other.description)) {
                return false;
            }
            if (!Objects.equals(this.appliedDate, other.appliedDate)) {
                return false;
            }
            if (!Objects.equals(this.amount, other.amount)) {
                return false;
            }
            return true;
        }

        @Override
        public int compareTo(CostDetail o) {
            if (this == o) {
                return 0;
            }
            if (appliedDate != o.appliedDate) {
                if (appliedDate != null && o.appliedDate != null) {
                    int idx = appliedDate.compareTo(o.appliedDate);
                    if (idx != 0) {
                        return idx;
                    }
                }
            }
            if (customer != o.customer) {
                if (customer != null && o.customer != null) {
                    int idx = customer.compareTo(o.customer);
                    if (idx != 0) {
                        return idx;
                    }
                }
            }
            if (description != o.description) {
                if (description != null && o.description != null) {
                    int idx = description.compareTo(o.description);
                    if (idx != 0) {
                        return idx;
                    }
                }
            }
            if (amount != o.amount) {
                if (amount != null && o.amount != null) {
                    int idx = amount.compareTo(o.amount);
                    if (idx != 0) {
                        return idx;
                    }
                }
            }
            return 0;
        }
    }
    
    public static class LaborDetail implements Comparable<LaborDetail> {

        private String worker;
        private DateTime workDate;
        private BigDecimal hours;
        private BigDecimal labor;
        
        public String getWorker() {
            return (worker == null ? "Not Specified" : worker);
        }

        public void setWorker(String worker) {
            this.worker = worker;
        }

        @JsonIgnore
        public DateTime getWorkDate() {
            return workDate;
        }
    
        public String getWorkDateShort() {
            if (this.workDate != null) {
                return DateTimeFormat.forPattern("yyyy-MM-dd").print(workDate);
            }
            return null;
        }

        public void setWorkDate(DateTime workDate) {
            this.workDate = workDate;
        }

        public BigDecimal getHours() {
            return hours;
        }

        public void setHours(BigDecimal hours) {
            this.hours = hours;
        }

        @JsonIgnore
        public BigDecimal getLabor() {
            return labor;
        }

        public BigDecimal getLaborFormatted() {
            return (labor == null ? BigDecimal.ZERO : labor.setScale(2, RoundingMode.HALF_UP));
        }

        public void setLabor(BigDecimal labor) {
            this.labor = labor;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 97 * hash + Objects.hashCode(this.worker);
            hash = 97 * hash + Objects.hashCode(this.workDate);
            hash = 97 * hash + Objects.hashCode(this.hours);
            hash = 97 * hash + Objects.hashCode(this.labor);
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
            final LaborDetail other = (LaborDetail) obj;
            if (!Objects.equals(this.worker, other.worker)) {
                return false;
            }
            if (!Objects.equals(this.workDate, other.workDate)) {
                return false;
            }
            if (!Objects.equals(this.hours, other.hours)) {
                return false;
            }
            if (!Objects.equals(this.labor, other.labor)) {
                return false;
            }
            return true;
        }

        @Override
        public int compareTo(LaborDetail o) {
            if (this == o) {
                return 0;
            }
            if (workDate != o.workDate) {
                if (workDate != null && o.workDate != null) {
                    int idx = workDate.compareTo(o.workDate);
                    if (idx != 0) {
                        return idx;
                    }
                }
            }
            if (worker != o.worker) {
                if (worker != null && o.worker != null) {
                    int idx = worker.compareTo(o.worker);
                    if (idx != 0) {
                        return idx;
                    }
                }
            }
            if (labor != o.labor) {
                if (labor != null && o.labor != null) {
                    int idx = labor.compareTo(o.labor);
                    if (idx != 0) {
                        return idx;
                    }
                }
            }
            return 0;
        }
    }
}