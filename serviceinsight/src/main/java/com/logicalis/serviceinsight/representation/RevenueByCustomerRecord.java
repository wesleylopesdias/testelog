package com.logicalis.serviceinsight.representation;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public class RevenueByCustomerRecord implements Serializable, Comparable<RevenueByCustomerRecord> {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = -2697918464670563356L;
	
	String customerName;
    String serviceDeliveryManager;
    BigDecimal revenue;
    Integer deviceCount;
    List<RevenueReportResultRecord> data;

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getServiceDeliveryManager() {
		return serviceDeliveryManager;
	}

	public void setServiceDeliveryManager(String serviceDeliveryManager) {
		this.serviceDeliveryManager = serviceDeliveryManager;
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
    public int compareTo(RevenueByCustomerRecord record) {
        int idx = 0;
        if (this.getRevenue() != null && record.getRevenue() != null) {
            idx = this.getRevenue().compareTo(record.getRevenue());
            if (idx != 0) {
                return idx;
            } else {
                if (this.getCustomerName() != null && record.getCustomerName() != null) {
                    idx = this.getCustomerName().compareTo(record.getCustomerName());
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

    @Override
    public String toString() {
        return "RevenueByCustomerRecord{" + "customerName=" + customerName + ", revenue=" + revenue + ", deviceCount=" + deviceCount + ", data=" + data + '}';
    }

}
