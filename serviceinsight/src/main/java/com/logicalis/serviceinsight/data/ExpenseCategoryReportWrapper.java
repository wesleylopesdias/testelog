package com.logicalis.serviceinsight.data;

import java.util.List;
import java.util.Map;

import com.logicalis.serviceinsight.dao.UnitCost;
import java.math.BigDecimal;

public class ExpenseCategoryReportWrapper {

	private String categoryName;
	private String month;
	private Map<String, UnitCost> previousMonths;
	private Map<String, List<CostFractionRecord>> previousMonthsDirect;
        private List<CostFractionRecord> directUnitCostDetails;
	private UnitCostDetails unitCostDetails;
	private List<Service> associatedServices;
        private List<Device> associatedDevices;
	
	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public String getMonth() {
		return month;
	}

	public void setMonth(String month) {
		this.month = month;
	}

	public Map<String, UnitCost> getPreviousMonths() {
		return previousMonths;
	}
	
	public void setPreviousMonths(Map<String, UnitCost> previousMonths) {
		this.previousMonths = previousMonths;
	}

    public Map<String, List<CostFractionRecord>> getPreviousMonthsDirect() {
        return previousMonthsDirect;
    }

    public void setPreviousMonthsDirect(Map<String, List<CostFractionRecord>> previousMonthsDirect) {
        this.previousMonthsDirect = previousMonthsDirect;
    }

    public List<CostFractionRecord> getDirectUnitCostDetails() {
        return directUnitCostDetails;
    }

    public void setDirectUnitCostDetails(List<CostFractionRecord> directUnitCostDetails) {
        this.directUnitCostDetails = directUnitCostDetails;
    }
	
	public UnitCostDetails getUnitCostDetails() {
		return unitCostDetails;
	}
	
	public void setUnitCostDetails(UnitCostDetails unitCostDetails) {
		this.unitCostDetails = unitCostDetails;
	}

	public List<Service> getAssociatedServices() {
		return associatedServices;
	}

	public void setAssociatedServices(List<Service> associatedServices) {
		this.associatedServices = associatedServices;
	}

    public List<Device> getAssociatedDevices() {
        return associatedDevices;
    }

    public void setAssociatedDevices(List<Device> associatedDevices) {
        this.associatedDevices = associatedDevices;
    }
	
}
