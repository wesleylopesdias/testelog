package com.logicalis.serviceinsight.representation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author poneil
 */
public class SPLAReport {
    
    List<SPLAReportCustomer> customers = new ArrayList<SPLAReportCustomer>();
    List<SPLASummary> splaSummaries = new ArrayList<SPLASummary>();

    public List<SPLAReportCustomer> getCustomers() {
        return customers;
    }
    
    public void addCustomer(SPLAReportCustomer customer) {
        this.customers.add(customer);
    }

    public List<SPLASummary> getSPLASummaries() {
        return splaSummaries;
    }
    
    public void addSPLASummary(SPLASummary splaSummary) {
        this.splaSummaries.add(splaSummary);
    }
        
    public BigDecimal getTotalCost() {
        BigDecimal total = BigDecimal.ZERO;
        for (SPLAReportCustomer record : this.customers) {
            total = total.add(record.getTotalCost());
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getTotalRevenue() {
        BigDecimal total = BigDecimal.ZERO;
        for (SPLAReportCustomer record : this.customers) {
            total = total.add(record.getTotalRevenue());
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }
    
    public static class SPLAReportCustomer  implements Comparable<SPLAReportCustomer> {

        String customer = "";
        List<SPLAReportContract> contracts = new ArrayList<SPLAReportContract>();
        
        public SPLAReportCustomer(String customer) {
            if (StringUtils.isNotBlank(customer)) {
                this.customer = customer;
            }
        }
        
        public String getCustomer() {
            return this.customer;
        }
        
        public List<SPLAReportContract> getContracts() {
            return contracts;
        }
 
        public void addContract(SPLAReportContract record) {
            this.contracts.add(record);
        }
        
        public BigDecimal getTotalCost() {
            BigDecimal total = BigDecimal.ZERO;
            for (SPLAReportContract record : this.contracts) {
                total = total.add(record.getTotalCost());
            }
            return total.setScale(2, RoundingMode.HALF_UP);
        }
        
        public BigDecimal getTotalRevenue() {
            BigDecimal total = BigDecimal.ZERO;
            for (SPLAReportContract record : this.contracts) {
                total = total.add(record.getTotalRevenue());
            }
            return total.setScale(2, RoundingMode.HALF_UP);
        }
        
        @Override
        public int compareTo(SPLAReportCustomer o) {
            return ObjectUtils.compare(this.customer, o.customer);
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 31 * hash + Objects.hashCode(this.customer);
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
            final SPLAReportCustomer other = (SPLAReportCustomer) obj;
            if (!Objects.equals(this.customer, other.customer)) {
                return false;
            }
            return true;
        }
    }
    
    public static class SPLAReportContract implements Comparable<SPLAReportContract> {
        
        Long contractId;
        String contractName;
        List<SPLAReportCostRecord> costs = new ArrayList<SPLAReportCostRecord>();
        List<SPLARevenue> revenues = new ArrayList<SPLARevenue>();
        
        public SPLAReportContract(Long contractId, String contractName) {
            this.contractId = contractId;
            this.contractName = contractName;
        }

        public Long getContractId() {
            return contractId;
        }

        public String getContractName() {
            return contractName;
        }

        public List<SPLAReportCostRecord> getCosts() {
            return costs;
        }
        
        public void addCost(SPLAReportCostRecord cost) {
            this.costs.add(cost);
        }
        
        public BigDecimal getTotalCost() {
            BigDecimal total = BigDecimal.ZERO;
            for (SPLAReportCostRecord record : this.costs) {
                total = total.add(record.getAmount());
            }
            return total.setScale(2, RoundingMode.HALF_UP);
        }

        public List<SPLARevenue> getRevenues() {
            return revenues;
        }
        
        public void addRevenue(SPLARevenue revenue) {
            this.revenues.add(revenue);
        }
        
        public BigDecimal getTotalRevenue() {
            BigDecimal total = BigDecimal.ZERO;
            for (SPLARevenue record : this.revenues) {
                total = total.add(record.getTotalRevenue());
            }
            return total.setScale(2, RoundingMode.HALF_UP);
        }

        @Override
        public int compareTo(SPLAReportContract o) {
            return ObjectUtils.compare(this.contractId, o.getContractId());
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 53 * hash + Objects.hashCode(this.contractId);
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
            final SPLAReportContract other = (SPLAReportContract) obj;
            if (!Objects.equals(this.contractId, other.contractId)) {
                return false;
            }
            return true;
        }
    }
    
    public static class SPLAReportCostRecord implements Comparable<SPLAReportCostRecord> {
        
        Long deviceId;
        Long splaId;
        String spla;
        String partNumber;
        private Integer quantity;
        BigDecimal amount = BigDecimal.ZERO;

        public SPLAReportCostRecord(String spla, Long splaId, String partNumber, Long deviceId, Integer quantity, BigDecimal amount) {
            this.spla = spla;
            this.splaId = splaId;
            this.deviceId = deviceId;
            this.partNumber = partNumber;
            this.quantity = quantity;
            this.amount = amount;
        }
        
        public String getSpla() {
            return spla;
        }

        public Long getDeviceId() {
            return deviceId;
        }

        public Long getSplaId() {
            return splaId;
        }

        public String getPartNumber() {
            return partNumber;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        @Override
        public int compareTo(SPLAReportCostRecord o) {
            int result = ObjectUtils.compare(this.deviceId, o.deviceId);
            if (result != 0) return result;
            result = ObjectUtils.compare(this.splaId, o.splaId);
            if (result != 0) return result;
            result = ObjectUtils.compare(this.spla, o.spla);
            if (result != 0) return result;
            return ObjectUtils.compare(this.amount, o.amount);
        }
    }
    
    public static class SPLAReportRevenueRecord implements Comparable<SPLAReportRevenueRecord> {
        
        String id;
        String service;
        BigDecimal revenue;
        
        public SPLAReportRevenueRecord(String id, String service, BigDecimal revenue) {
            this.id = id;
            this.service = service;
            this.revenue = revenue;
        }

        public String getId() {
            return id;
        }

        public String getService() {
            return service;
        }

        public BigDecimal getRevenue() {
            return revenue;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 37 * hash + Objects.hashCode(this.id);
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
            final SPLAReportRevenueRecord other = (SPLAReportRevenueRecord) obj;
            if (!Objects.equals(this.id, other.id)) {
                return false;
            }
            return true;
        }

        @Override
        public int compareTo(SPLAReportRevenueRecord o) {
            return ObjectUtils.compare(this.getService(), o.getService());
        }
    }
    
    public static class SPLASummary {

        private Long id;
        private String name;
        private Integer quantity = 0;
        private BigDecimal cost = BigDecimal.ZERO;
        
        public SPLASummary(Long id, String name, Integer quantity, BigDecimal cost) {
            this.id = id;
            this.name = name;
            this.quantity = quantity;
            this.cost = cost;
        }
        
        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public Integer getQuantity() {
            return quantity;
        }
        
        public void addQuantity(Integer addQuantity) {
            this.quantity += addQuantity;
        }

        public BigDecimal getCost() {
            return cost;
        }
        
        public void addCost(BigDecimal costtoadd) {
            this.cost = this.cost.add(costtoadd);
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 17 * hash + Objects.hashCode(this.id);
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
            final SPLASummary other = (SPLASummary) obj;
            if (!Objects.equals(this.id, other.id)) {
                return false;
            }
            return true;
        }
    }
}
