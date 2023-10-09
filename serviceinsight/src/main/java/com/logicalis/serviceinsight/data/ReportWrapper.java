package com.logicalis.serviceinsight.data;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.logicalis.serviceinsight.representation.RevenueReportResultRecord;

import net.sf.json.JSONArray;

public class ReportWrapper {
	
	public enum ReportType {
		revenue, revenueProfit, laborByCost, laborHours
	}
	
	private ReportType type;
	private Date startDate;
	private Date endDate;
	private Long customerId;
	private String customerName;
	private Long serviceId;
	private String serviceName;
	private String businessModel;
	private Boolean excludeContractAdjustments;
	private Map<Long, List<RevenueReportResultRecord>> data;
	private Object genericData;
	private Long resultCount;
	
	public ReportWrapper(){};
	
	public ReportWrapper(ReportType type, Date startDate, Date endDate, Long customerId, String customerName, Long serviceId, String serviceName, String businessModel, Boolean excludeContractAdjustments, Map<Long, List<RevenueReportResultRecord>> data) {
		this.type = type;
		this.startDate = startDate;
		this.endDate = endDate;
		this.customerId = customerId;
		this.customerName = customerName;
		this.serviceId = serviceId;
		this.serviceName = serviceName;
		this.businessModel = businessModel;
		this.excludeContractAdjustments = excludeContractAdjustments;
		this.data = data;
	}

	public ReportType getType() {
		return type;
	}
	
	public void setType(ReportType type) {
		this.type = type;
	}
	
	public String getBusinessModel() {
		return businessModel;
	}
	
	public void setBusinessModel(String businessModel) {
		this.businessModel = businessModel;
	}
	
	public Date getStartDate() {
		return startDate;
	}
	
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	
	public Date getEndDate() {
		return endDate;
	}
	
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	
	public Long getCustomerId() {
		return customerId;
	}
	
	public void setCustomerId(Long customerId) {
		this.customerId = customerId;
	}
	
	public Long getServiceId() {
		return serviceId;
	}
	
	public void setServiceId(Long serviceId) {
		this.serviceId = serviceId;
	}
	
	public Boolean getExcludeContractAdjustments() {
		return excludeContractAdjustments;
	}

	public void setExcludeContractAdjustments(Boolean excludeContractAdjustments) {
		this.excludeContractAdjustments = excludeContractAdjustments;
	}

	public Map<Long, List<RevenueReportResultRecord>> getData() {
		return data;
	}

	public void setData(Map<Long, List<RevenueReportResultRecord>> data) {
		this.data = data;
	}

	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public Object getGenericData() {
		return genericData;
	}

	public void setGenericData(Object genericData) {
		this.genericData = genericData;
	}

	public Long getResultCount() {
		return resultCount;
	}

	public void setResultCount(Long resultCount) {
		this.resultCount = resultCount;
	}
	
}
