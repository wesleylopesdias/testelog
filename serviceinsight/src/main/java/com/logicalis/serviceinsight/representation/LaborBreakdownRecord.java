package com.logicalis.serviceinsight.representation;

import java.math.BigDecimal;

public class LaborBreakdownRecord {

    private String tierName;
    private String tierCode;
    private BigDecimal tierRate;
    private BigDecimal tierAddlRate;
    private BigDecimal tierTotal;
    private BigDecimal tierAddlTotal;

    public LaborBreakdownRecord() {
    }

    public LaborBreakdownRecord(String tierName, String tierCode, BigDecimal tierRate, BigDecimal tierAddlRate, BigDecimal tierTotal, BigDecimal tierAddlTotal) {
        this.tierName = tierName;
        this.tierCode = tierCode;
        this.tierRate = tierRate;
        this.tierAddlRate = tierAddlRate;
        this.tierTotal = tierTotal;
        this.tierAddlTotal = tierAddlTotal;
    }

    public String getTierName() {
        return tierName;
    }

    public void setTierName(String tierName) {
        this.tierName = tierName;
    }

    public String getTierCode() {
        return tierCode;
    }

    public void setTierCode(String tierCode) {
        this.tierCode = tierCode;
    }

    public BigDecimal getTierRate() {
        return tierRate;
    }

    public void setTierRate(BigDecimal tierRate) {
        this.tierRate = tierRate;
    }

    public BigDecimal getTierAddlRate() {
        return tierAddlRate;
    }

    public void setTierAddlRate(BigDecimal tierAddlRate) {
        this.tierAddlRate = tierAddlRate;
    }

    public BigDecimal getTierTotal() {
        return tierTotal;
    }

    public void setTierTotal(BigDecimal tierTotal) {
        this.tierTotal = tierTotal;
    }

    public BigDecimal getTierAddlTotal() {
        return tierAddlTotal;
    }

    public void setTierAddlTotal(BigDecimal tierAddlTotal) {
        this.tierAddlTotal = tierAddlTotal;
    }
}
