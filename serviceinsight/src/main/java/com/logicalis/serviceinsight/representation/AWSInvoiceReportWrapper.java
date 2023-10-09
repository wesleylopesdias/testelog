package com.logicalis.serviceinsight.representation;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class AWSInvoiceReportWrapper {

    private List<AWSBillingPeriodWrapper> awsBillingPeriods = new ArrayList<AWSBillingPeriodWrapper>();
    private List<CSPBilledContractService> siAWSServices = new ArrayList<CSPBilledContractService>();
    private List<AWSInvoiceReportUsageLineItem> usageLineItems = new ArrayList<AWSInvoiceReportUsageLineItem>();

    @JsonProperty(value = "awsBillingPeriods")
    public List<AWSBillingPeriodWrapper> getAWSBillingPeriods() {
        return awsBillingPeriods;
    }

    public void setAWSBillingPeriods(
            List<AWSBillingPeriodWrapper> awsBillingPeriods) {
        this.awsBillingPeriods = awsBillingPeriods;
    }
    
    public void addAWSBillingPeriod(AWSBillingPeriodWrapper awsBillingPeriod) {
        if (awsBillingPeriods == null) {
            awsBillingPeriods = new ArrayList<AWSBillingPeriodWrapper>();
        }
        awsBillingPeriods.add(awsBillingPeriod);
    }

    @JsonProperty(value = "siAWSServices")
    public List<CSPBilledContractService> getSiAWSServices() {
        return siAWSServices;
    }

    public void setSiAWSServices(List<CSPBilledContractService> siAWSServices) {
        this.siAWSServices = siAWSServices;
    }

    public List<AWSInvoiceReportUsageLineItem> getUsageLineItems() {
        return usageLineItems;
    }

    public void setUsageLineItems(List<AWSInvoiceReportUsageLineItem> usageLineItems) {
        this.usageLineItems = usageLineItems;
    }
}
