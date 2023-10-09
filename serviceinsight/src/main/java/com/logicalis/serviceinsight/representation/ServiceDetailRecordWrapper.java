package com.logicalis.serviceinsight.representation;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.List;
import org.joda.time.DateTime;

/**
 * Holds the overall report data for the Service Detail report
 * 
 * @author poneil
 */
public class ServiceDetailRecordWrapper {

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="MM/dd/yyyy", timezone="America/New_York")
    private DateTime serviceDate;
    private String serviceName;
    private Long ospId;
    private String devicePartNumber;
    private Long deviceId;
    private List<ServiceDetailRecord> data;
    
    public DateTime getServiceDate() {
        return serviceDate;
    }

    public void setServiceDate(DateTime serviceDate) {
        this.serviceDate = serviceDate;
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

    public String getDevicePartNumber() {
        return devicePartNumber;
    }

    public void setDevicePartNumber(String devicePartNumber) {
        this.devicePartNumber = devicePartNumber;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public List<ServiceDetailRecord> getData() {
        return data;
    }

    public void setData(List<ServiceDetailRecord> data) {
        this.data = data;
    }
}