package com.logicalis.serviceinsight.representation;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public class AWSInvoiceReportUsageLineItem {

    private String awsCustomerName;
    private String awsSubscriptionId;
    private String awsSubscriptionName;
    private BigDecimal awsMonthlyCost;
    private String siCustomerName;
    private String siSubscripionName;
    private BigDecimal siOnetimeRevenue;
    private BigDecimal siRecurringRevenue;

    @JsonProperty(value = "awsCustomerName")
    public String getAWSCustomerName() {
        return awsCustomerName;
    }

    public void setAWSCustomerName(String awsCustomerName) {
        this.awsCustomerName = awsCustomerName;
    }

    @JsonProperty(value = "awsSubscriptionId")
    public String getAWSSubscriptionId() {
        return awsSubscriptionId;
    }

    public void setAWSSubscriptionId(String awsSubscriptionId) {
        this.awsSubscriptionId = awsSubscriptionId;
    }

    @JsonProperty(value = "awsSubscriptionName")
    public String getAWSSubscriptionName() {
        return awsSubscriptionName;
    }

    public void setAWSSubscriptionName(String awsSubscriptionName) {
        this.awsSubscriptionName = awsSubscriptionName;
    }

    @JsonProperty(value = "awsMonthlyCost")
    public BigDecimal getAWSMonthlyCost() {
        return awsMonthlyCost;
    }

    public void setAWSMonthlyCost(BigDecimal awsMonthlyCost) {
        this.awsMonthlyCost = awsMonthlyCost;
    }

    public String getSiCustomerName() {
        return siCustomerName;
    }

    public void setSiCustomerName(String siCustomerName) {
        this.siCustomerName = siCustomerName;
    }

    public String getSiSubscripionName() {
        return siSubscripionName;
    }

    public void setSiSubscripionName(String siSubscripionName) {
        this.siSubscripionName = siSubscripionName;
    }

    public BigDecimal getSiOnetimeRevenue() {
        return siOnetimeRevenue;
    }

    public void setSiOnetimeRevenue(BigDecimal siOnetimeRevenue) {
        this.siOnetimeRevenue = siOnetimeRevenue;
    }

    public BigDecimal getSiRecurringRevenue() {
        return siRecurringRevenue;
    }

    public void setSiRecurringRevenue(BigDecimal siRecurringRevenue) {
        this.siRecurringRevenue = siRecurringRevenue;
    }

    /*
    public BigDecimal getSiMonthlyRevenue() {
        return getSiOnetimeRevenue().add(getSiRecurringRevenue());
    }
    */
}
