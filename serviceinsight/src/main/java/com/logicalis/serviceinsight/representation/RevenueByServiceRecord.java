package com.logicalis.serviceinsight.representation;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

public class RevenueByServiceRecord implements Serializable, Comparable<RevenueByServiceRecord> {

    String businessModel;
    String serviceName;
    Long ospId;
    BigDecimal revenue;
    Integer deviceCount;
    List<RevenueReportResultRecord> data;

    public String getBusinessModel() {
        return businessModel;
    }

    public void setBusinessModel(String businessModel) {
        this.businessModel = businessModel;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Long getOspId() {
        return ospId;
    }

    public void setOspId(Long ospId) {
        this.ospId = ospId;
    }

    public BigDecimal getRevenue() {
		return revenue;
	}

	public void setRevenue(BigDecimal revenue) {
		this.revenue = revenue;
	}

	public Integer getDeviceCount() {
		return deviceCount;
	}

	public void setDeviceCount(Integer deviceCount) {
		this.deviceCount = deviceCount;
	}

	public List<RevenueReportResultRecord> getData() {
        return data;
    }

    public void setData(List<RevenueReportResultRecord> data) {
        this.data = data;
    }
    
    @Override
    public int compareTo(RevenueByServiceRecord record) {
        int idx = 0;
        if (this.getRevenue() != null && record.getRevenue() != null) {
            idx = this.getRevenue().compareTo(record.getRevenue());
            if (idx != 0) {
                return idx;
            } else {
            	if (this.getServiceName() != null && record.getServiceName() != null) {
                    idx = this.getServiceName().compareTo(record.getServiceName());
                    if (idx != 0) {
                        return idx;
                    }
                }
            }
        } else if (this.getRevenue() != null) {
            return 1;
        } else if (record.getRevenue() != null) {
            return -1;
        }
        
        return 0;
    }
}
