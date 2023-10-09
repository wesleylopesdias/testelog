package com.logicalis.serviceinsight.data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for carrying revenue of all kinds
 * 
 * @author poneil
 */
public abstract class MonthlyRevenue {
    
    public enum Type {
        SERVICE, LINEITEM
    }
    
    Long id;
    Type type;
    BigDecimal revenue;
    DateTime month;
    Integer quantity = 1; // default!
    Integer days;
    protected final Logger log = LoggerFactory.getLogger(getClass());
    
    public MonthlyRevenue() {
    }
    
    public MonthlyRevenue(Long id, Type type, Date forMonth, BigDecimal revenue) {
        this.id = id;
        this.type = type;
        this.month = new DateTime(forMonth);
        this.days = this.month.dayOfMonth().getMaximumValue();
        this.revenue = revenue;
    }
    
    public MonthlyRevenue(Long id, Type type, Date forMonth, BigDecimal revenue, Integer quantity) {
        this.id = id;
        this.type = type;
        this.month = new DateTime(forMonth);
        this.days = this.month.dayOfMonth().getMaximumValue();
        this.revenue = revenue;
        this.quantity = quantity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getDays() {
        return days;
    }

    public BigDecimal getRevenue() {
        if (revenue == null) {
            return new BigDecimal(0);
        }
        return revenue.multiply(new BigDecimal(quantity))
                .multiply(new BigDecimal(getMonthFraction()));
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }
    
    public BigDecimal getFormattedRevenue() {
        if (revenue != null) {
            return revenue.multiply(new BigDecimal(quantity))
                    .multiply(new BigDecimal(getMonthFraction()))
                    .setScale(2, RoundingMode.HALF_UP);
        }
        return new BigDecimal(0).setScale(2, RoundingMode.HALF_UP);
    }
    
    public Integer getMonthInteger() {
        if (month != null) {
            return month.getMonthOfYear();
        }
        return null;
    }
    
    public String getShortMonthName() {
        if (month != null) {
            return month.toString("MMM");
        }
        return null;
    }
    
    public String getLongMonthName() {
        if (month != null) {
            return month.toString("MMMMM");
        }
        return null;
    }
    
    public Integer getYearInteger() {
        if (month != null) {
            return month.getYear();
        }
        return null;
    }
    
    public String getShortYearName() {
        if (month != null) {
            return month.toString("yy");
        }
        return null;
    }
    
    public String getLongYearName() {
        if (month != null) {
            return month.toString("yyyy");
        }
        return null;
    }
    
    public Double getMonthFraction() {
        return 1.0;
    }
}
