package com.logicalis.serviceinsight.data;

import java.math.BigDecimal;
import java.util.Date;

/**
 *
 * @author poneil
 */
public class MonthlyBillingInfo implements Comparable<MonthlyBillingInfo> {
    private String product;
    private Date startDate;
    private BigDecimal units;
    private BigDecimal pricing;
    private BigDecimal billable;

    public MonthlyBillingInfo() {
        
    }
    
    public MonthlyBillingInfo(String product, Date startDate, BigDecimal units, BigDecimal pricing) {
        this.product = product;
        this.startDate = startDate;
        this.units = units;
        this.pricing = pricing;
        this.setBillable(units.multiply(pricing));
    }
    
    /**
     * @return the product
     */
    public String getProduct() {
        return product;
    }

    /**
     * @param product the product to set
     */
    public void setProduct(String product) {
        this.product = product;
    }

    /**
     * @return the startDate
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * @param startDate the startDate to set
     */
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    /**
     * @return the units
     */
    public BigDecimal getUnits() {
        return units;
    }

    /**
     * @param units the units to set
     */
    public void setUnits(BigDecimal units) {
        this.units = units;
    }

    /**
     * @return the pricing
     */
    public BigDecimal getPricing() {
        return pricing;
    }

    /**
     * @param pricing the pricing to set
     */
    public void setPricing(BigDecimal pricing) {
        this.pricing = pricing;
    }

    /**
     * @return the billable
     */
    public BigDecimal getBillable() {
        return billable;
    }

    /**
     * @param billable the billable to set
     */
    public void setBillable(BigDecimal billable) {
        this.billable = billable;
    }

    @Override
    public int compareTo(MonthlyBillingInfo o) {
        int result = getProduct().compareTo(o.getProduct());
        if (result != 0) {
            return result;
        }
        if (this.startDate != null) {
            result = this.startDate.compareTo(o.startDate);
            if (result != 0) {
                return result;
            }
        } else if (o.startDate != null) {
            return -1;
        }
        return 0;
    }
}
