package com.logicalis.serviceinsight.data;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.util.Date;

/**
 * A simple representation of AWS Account Cost and Usage information.
 *
 * @author poneil
 */
public class AWSAccountCostAndUsage {

    private String accountId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-yyyy")
    private Date period;
    private BigDecimal blendedCost = BigDecimal.ZERO;
    private BigDecimal usageQuantity = BigDecimal.ZERO;
    private BigDecimal unblendedCost = BigDecimal.ZERO;

    /**
     * default CTOR
     */
    public AWSAccountCostAndUsage() {
    }

    public AWSAccountCostAndUsage(String accountId, Date period) {
        this.accountId = accountId;
        this.period = period;
    }

    public AWSAccountCostAndUsage(String accountId, Date period, BigDecimal blendedCost, BigDecimal usageQuantity,
            BigDecimal unblendedCost) {
        this.accountId = accountId;
        this.period = period;
        this.blendedCost = blendedCost;
        this.usageQuantity = usageQuantity;
        this.unblendedCost = unblendedCost;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public Date getPeriod() {
        return period;
    }

    public void setPeriod(Date period) {
        this.period = period;
    }

    public BigDecimal getBlendedCost() {
        return blendedCost;
    }

    public void setBlendedCost(BigDecimal blendedCost) {
        this.blendedCost = blendedCost;
    }

    public BigDecimal getUsageQuantity() {
        return usageQuantity;
    }

    public void setUsageQuantity(BigDecimal usageQuantity) {
        this.usageQuantity = usageQuantity;
    }

    public BigDecimal getUnblendedCost() {
        return unblendedCost;
    }

    public void setUnblendedCost(BigDecimal unblendedCost) {
        this.unblendedCost = unblendedCost;
    }

    @Override
    public String toString() {
        return "AWSAccountCostAndUsage{" + "accountId=" + accountId + ", period=" + period + ", blendedCost=" + blendedCost + ", usageQuantity=" + usageQuantity + ", unblendedCost=" + unblendedCost + '}';
    }
}
