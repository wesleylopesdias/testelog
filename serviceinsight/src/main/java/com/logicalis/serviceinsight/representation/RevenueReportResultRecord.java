package com.logicalis.serviceinsight.representation;

import com.logicalis.serviceinsight.dao.UnitCost;
import com.logicalis.serviceinsight.data.Contract;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.joda.time.DateTime;

public class RevenueReportResultRecord {

    private DateTime date;
    private String displayDate;
    private BigDecimal revenue;
    private BigDecimal forecastedRevenue;
    private BigDecimal laborCost;
    private BigDecimal addlLaborCost;
    private BigDecimal onboardingLaborCost;
    private BigDecimal addlOnboardingLaborCost;
    private BigDecimal indirectLaborCost;
    private BigDecimal addlIndirectLaborCost;
    private BigDecimal indirectLaborProportion;
    private BigDecimal addlIndirectLaborProportion;
    private BigDecimal serviceCost; // costs spread across all customers
    private List<UnitCost> serviceCostDetails = new ArrayList<UnitCost>();
    private BigDecimal directCost; // customer specific costs
    private List<UnitCost> directCostDetails = new ArrayList<UnitCost>();
    private Integer deviceCount;
    private Integer serviceCount;
    private List<Contract> forecastedContracts = new ArrayList<Contract>();

    public String getDisplayDate() {
        return displayDate;
    }

    public void setDisplayDate(String displayDate) {
        this.displayDate = displayDate;
    }

    public DateTime getDate() {
        return date;
    }

    public void setDate(DateTime date) {
        this.date = date;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }

    public BigDecimal getForecastedRevenue() {
		return forecastedRevenue;
	}

	public void setForecastedRevenue(BigDecimal forecastedRevenue) {
		this.forecastedRevenue = forecastedRevenue;
	}

	public BigDecimal getLaborCost() {
        return laborCost;
    }

    public void setLaborCost(BigDecimal laborCost) {
        this.laborCost = laborCost;
    }

    public BigDecimal getAddlLaborCost() {
        return addlLaborCost;
    }

    public void setAddlLaborCost(BigDecimal addlLaborCost) {
        this.addlLaborCost = addlLaborCost;
    }

    public BigDecimal getOnboardingLaborCost() {
        return onboardingLaborCost;
    }

    public void setOnboardingLaborCost(BigDecimal onboardingLaborCost) {
        this.onboardingLaborCost = onboardingLaborCost;
    }

    public BigDecimal getAddlOnboardingLaborCost() {
        return addlOnboardingLaborCost;
    }

    public void setAddlOnboardingLaborCost(BigDecimal addlOnboardingLaborCost) {
        this.addlOnboardingLaborCost = addlOnboardingLaborCost;
    }

    /**
     * @deprecated use getIndirectLaborProportionCost()
     * @return 
     */
    public BigDecimal getIndirectLaborCost() {
        return indirectLaborCost;
    }

    public void setIndirectLaborCost(BigDecimal indirectLaborCost) {
        this.indirectLaborCost = indirectLaborCost;
    }

    /**
     * @deprecated use getAddlIndirectLaborProportion()
     * @return 
     */
    public BigDecimal getAddlIndirectLaborCost() {
        return addlIndirectLaborCost;
    }

    public void setAddlIndirectLaborCost(BigDecimal addlIndirectLaborCost) {
        this.addlIndirectLaborCost = addlIndirectLaborCost;
    }

    public BigDecimal getIndirectLaborProportion() {
        return indirectLaborProportion;
    }

    public BigDecimal getIndirectLaborProportionCost() {
        if (indirectLaborProportion == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal totalDirectLaborCost = BigDecimal.ZERO;
        if (this.laborCost != null) {
            totalDirectLaborCost = totalDirectLaborCost.add(this.laborCost);
        }
        if (this.onboardingLaborCost != null) {
            totalDirectLaborCost = totalDirectLaborCost.add(this.onboardingLaborCost);
        }
        return indirectLaborProportion.multiply(totalDirectLaborCost).setScale(2, RoundingMode.HALF_UP);
    }

    public void setIndirectLaborProportion(BigDecimal indirectLaborProportion) {
        this.indirectLaborProportion = indirectLaborProportion;
    }

    public BigDecimal getAddlIndirectLaborProportion() {
        return addlIndirectLaborProportion;
    }

    public BigDecimal getAddlIndirectLaborProportionCost() {
        if (this.addlIndirectLaborProportion == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal totalAddlDirectLaborCost = BigDecimal.ZERO;
        if (this.addlLaborCost != null) {
            totalAddlDirectLaborCost = totalAddlDirectLaborCost.add(this.addlLaborCost);
        }
        if (this.addlOnboardingLaborCost != null) {
            totalAddlDirectLaborCost = totalAddlDirectLaborCost.add(this.addlOnboardingLaborCost);
        }
        return this.addlIndirectLaborProportion.multiply(totalAddlDirectLaborCost).setScale(2, RoundingMode.HALF_UP);
    }

    public void setAddlIndirectLaborProportion(BigDecimal addlIndirectLaborProportion) {
        this.addlIndirectLaborProportion = addlIndirectLaborProportion;
    }

    public BigDecimal getServiceCost() {
        return serviceCost;
    }

    public void setServiceCost(BigDecimal serviceCost) {
        this.serviceCost = serviceCost;
    }

    public BigDecimal getDirectCost() {
        return directCost;
    }

    public void setDirectCost(BigDecimal directCost) {
        this.directCost = directCost;
    }

    public List<UnitCost> getServiceCostDetails() {
        return serviceCostDetails;
    }

    public void setServiceCostDetails(List<UnitCost> serviceCostDetails) {
        this.serviceCostDetails = serviceCostDetails;
    }

    public List<UnitCost> getDirectCostDetails() {
        return directCostDetails;
    }

    public void setDirectCostDetails(List<UnitCost> directCostDetails) {
        this.directCostDetails = directCostDetails;
    }
    
    public List<Contract> getForecastedContracts() {
		return forecastedContracts;
	}

	public void setForecastedContracts(List<Contract> forecastedContracts) {
		this.forecastedContracts = forecastedContracts;
	}

	public BigDecimal getPricePerDevice() {
    	BigDecimal average = new BigDecimal(0);
    	BigDecimal totalRevenue = getRevenue();
    	Integer totalDeviceCount = getDeviceCount();
    	if(totalRevenue != null && totalDeviceCount != null && totalDeviceCount > 0) {
    		average = totalRevenue.divide(new BigDecimal(totalDeviceCount), 2, RoundingMode.HALF_UP);
    	}
    	return average;
    }
    
    public BigDecimal getPricePerService() {
    	BigDecimal average = new BigDecimal(0);
    	BigDecimal totalRevenue = getRevenue();
    	Integer totalServiceCount = getServiceCount();
    	if(totalRevenue != null && totalServiceCount != null && totalServiceCount > 0) {
    		average = totalRevenue.divide(new BigDecimal(totalServiceCount), 2, RoundingMode.HALF_UP);
    	}
    	return average;
    }
    
    public BigDecimal getProfitability() {
    	BigDecimal profitability = BigDecimal.ZERO
                .add(revenue);
    	profitability = profitability
                .subtract((laborCost == null ? BigDecimal.ZERO : laborCost))
                .subtract(getIndirectLaborProportionCost())
                .subtract((serviceCost == null ? BigDecimal.ZERO : serviceCost))
                .subtract((directCost == null ? BigDecimal.ZERO : directCost))
                .subtract((onboardingLaborCost == null ? BigDecimal.ZERO : onboardingLaborCost))
                .subtract(getLaborToolsCost());
    	return profitability;
    }
    
    public BigDecimal getLaborToolsCost() {
    	BigDecimal laborToolsCost = BigDecimal.ZERO
                .add((addlLaborCost == null ? BigDecimal.ZERO : addlLaborCost))
                .add(getAddlIndirectLaborProportionCost())
                .add((addlOnboardingLaborCost == null ? BigDecimal.ZERO : addlOnboardingLaborCost));
    	return laborToolsCost;
    }
    
    public BigDecimal getTotalCost() {
    	BigDecimal totalCost = BigDecimal.ZERO
                .add((laborCost == null ? BigDecimal.ZERO : laborCost))
                .add(getIndirectLaborProportionCost())
                .add((serviceCost == null ? BigDecimal.ZERO : serviceCost))
                .add((directCost == null ? BigDecimal.ZERO : directCost))
                .add((onboardingLaborCost == null ? BigDecimal.ZERO : onboardingLaborCost))
                .add(getLaborToolsCost());
    	return totalCost;
    }
    
    public BigDecimal getMargin() {
    	BigDecimal margin = BigDecimal.ZERO;
    	BigDecimal totalCost = getTotalCost();
    	BigDecimal revenue = getRevenue();
    	if(revenue != null && totalCost != null && (revenue.compareTo(BigDecimal.ZERO) > 0) && (totalCost.compareTo(BigDecimal.ZERO) > 0)) {
    		margin = new BigDecimal(1).subtract(totalCost.divide(revenue, 6, RoundingMode.HALF_UP));
    	}
    	
    	return margin;
    }

    public Integer getServiceCount() {
        return serviceCount;
    }

    public void setServiceCount(Integer serviceCount) {
        this.serviceCount = serviceCount;
    }

    public Integer getDeviceCount() {
        return deviceCount;
    }

    public void setDeviceCount(Integer deviceCount) {
        this.deviceCount = deviceCount;
    }
    
    /**
     * Help track a "unique" revenue Service record by:
     * Customer, Service, Device, Start and End Date
     */
    public static class UniqueRevenueService {
        
        private Long customerId;
        private String ospId;
        private Long deviceId;
        private String partNumber;
        private DateTime startDate;
        private DateTime endDate;
        Integer serviceCount = 0;
        Integer deviceCount = 0;
        private BigDecimal serviceRevenue = BigDecimal.ZERO;
        private BigDecimal directCost = BigDecimal.ZERO;
        private BigDecimal serviceCost = BigDecimal.ZERO;

        public UniqueRevenueService(Long customerId, String ospId, Long deviceId, String partNumber, java.util.Date startDate, java.util.Date endDate) {
            this.customerId = customerId;
            this.ospId = ospId;
            this.deviceId = deviceId;
            this.partNumber = partNumber;
            if (startDate != null) {
                this.startDate = new DateTime(startDate);
            }
            if (endDate != null) {
                this.endDate = new DateTime(endDate);
            }
        }
        
        public Integer getServiceCount() {
            return this.serviceCount;
        }
        
        public void setServiceCount(Integer count) {
            this.serviceCount = count;
        }
        
        public void incrementServiceCount(Integer count) {
            this.serviceCount += count;
        }
        
        public Integer getDeviceCount() {
            return this.deviceCount;
        }
        
        public void setDeviceCount(Integer count) {
            this.deviceCount = count;
        }
        
        public void incrementDeviceCount(Integer count) {
            this.deviceCount += count;
        }
        
        public Long getCustomerId() {
            return customerId;
        }

        public void setCustomerId(Long customerId) {
            this.customerId = customerId;
        }

        public String getOspId() {
            return ospId;
        }

        public void setOspId(String ospId) {
            this.ospId = ospId;
        }

        public Long getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(Long deviceId) {
            this.deviceId = deviceId;
        }

        public String getPartNumber() {
            return partNumber;
        }

        public void setPartNumber(String partNumber) {
            this.partNumber = partNumber;
        }

        public DateTime getStartDate() {
            return startDate;
        }

        public void setStartDate(DateTime startDate) {
            this.startDate = startDate;
        }

        public DateTime getEndDate() {
            return endDate;
        }

        public void setEndDate(DateTime endDate) {
            this.endDate = endDate;
        }

        public BigDecimal getDirectCost() {
            return directCost;
        }

        public BigDecimal getDirectCostFormatted() {
            return directCost.setScale(2, RoundingMode.HALF_UP);
        }
        
        public void addDirectCost(BigDecimal amount) {
            if (amount != null) {
                directCost = directCost.add(amount);
            }
        }

        public BigDecimal getServiceCost() {
            return serviceCost;
        }

        public BigDecimal getServiceCostFormatted() {
            return serviceCost.setScale(2, RoundingMode.HALF_UP);
        }
        
        public void addServiceCost(BigDecimal amount) {
            if (amount != null) {
                serviceCost = serviceCost.add(amount);
            }
        }

        public BigDecimal getServiceRevenue() {
            return serviceRevenue;
        }

        public void setServiceRevenue(BigDecimal serviceRevenue) {
            this.serviceRevenue = serviceRevenue;
        }

        /**
         * do not change: used to identify unique Service records in RevenueService
         * 
         * @return 
         */
        @Override
        public int hashCode() {
            int hash = 7;
            hash = 59 * hash + Objects.hashCode(this.customerId);
            hash = 59 * hash + Objects.hashCode(this.ospId);
            hash = 59 * hash + Objects.hashCode(this.deviceId);
            hash = 59 * hash + Objects.hashCode(this.startDate);
            hash = 59 * hash + Objects.hashCode(this.endDate);
            return hash;
        }

        /**
         * do not change: used to identify unique Service records in RevenueService
         * 
         * @param obj
         * @return 
         */
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
            final UniqueRevenueService other = (UniqueRevenueService) obj;
            if (!Objects.equals(this.ospId, other.ospId)) {
                return false;
            }
            if (!Objects.equals(this.customerId, other.customerId)) {
                return false;
            }
            if (!Objects.equals(this.deviceId, other.deviceId)) {
                return false;
            }
            if (!Objects.equals(this.startDate, other.startDate)) {
                return false;
            }
            if (!Objects.equals(this.endDate, other.endDate)) {
                return false;
            }
            return true;
        }
    }

    @Override
    public String toString() {
        return "RevenueReportResultRecord{" + "date=" + date + ", displayDate=" + displayDate + ", revenue=" + revenue + ", forecastedRevenue=" + forecastedRevenue + ", laborCost=" + laborCost + ", addlLaborCost=" + addlLaborCost + ", onboardingLaborCost=" + onboardingLaborCost + ", addlOnboardingLaborCost=" + addlOnboardingLaborCost + ", indirectLaborCost=" + indirectLaborCost + ", addlIndirectLaborCost=" + addlIndirectLaborCost + ", indirectLaborProportion=" + indirectLaborProportion + ", addlIndirectLaborProportion=" + addlIndirectLaborProportion + ", serviceCost=" + serviceCost + ", serviceCostDetails=" + serviceCostDetails + ", directCost=" + directCost + ", directCostDetails=" + directCostDetails + ", deviceCount=" + deviceCount + ", serviceCount=" + serviceCount + '}';
    }
}
