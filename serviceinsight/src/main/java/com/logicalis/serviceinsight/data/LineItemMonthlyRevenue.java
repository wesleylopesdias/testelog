package com.logicalis.serviceinsight.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.Objects;
import org.apache.commons.math3.fraction.Fraction;
import org.joda.time.DateTime;

/**
 *
 * @author poneil
 */
public class LineItemMonthlyRevenue extends MonthlyRevenue {
    
    private Long contractId;
    private Long contractServiceId;
    private Long serviceId;
    private Long lineItemId;
    private DateTime startDate;
    private DateTime endDate;
    private String lineItem;
    private BigDecimal onetimeRevenue;
    
    public LineItemMonthlyRevenue() {
        super();
    }
    
    public LineItemMonthlyRevenue(Date startDate, Date endDate, Date forMonth, BigDecimal revenue, Long id,
            Long contractId, Long contractServiceId, Long serviceId, Long lineItemId, String lineItem, BigDecimal onetimeRevenue,
            Integer quantity) {
        super(id, Type.LINEITEM, forMonth, revenue, quantity);
        this.contractId = contractId;
        this.contractServiceId = contractServiceId;
        this.serviceId = serviceId;
        this.lineItemId = lineItemId;
        this.startDate = new DateTime(startDate);
        if (endDate != null) {
            this.endDate = new DateTime(endDate);
        }
        this.lineItem = lineItem;
        this.onetimeRevenue = onetimeRevenue;
    }

    public Long getContractId() {
        return contractId;
    }

    public void setContractId(Long contractId) {
        this.contractId = contractId;
    }

    public Long getContractServiceId() {
        return contractServiceId;
    }

    public void setContractServiceId(Long contractServiceId) {
        this.contractServiceId = contractServiceId;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public Long getLineItemId() {
        return lineItemId;
    }

    public void setLineItemId(Long lineItemId) {
        this.lineItemId = lineItemId;
    }

    public String getLineItem() {
        return lineItem;
    }

    public void setLineItem(String lineItem) {
        this.lineItem = lineItem;
    }

    public BigDecimal getOnetimeRevenue() {
        
        if (startDate.getYear() == month.getYear() &&
                startDate.getMonthOfYear() == month.getMonthOfYear()) {
            return onetimeRevenue;
        }
        return new BigDecimal(0); // onetimeRevenue only applies in the first month
    }

    public void setOnetimeRevenue(BigDecimal onetimeRevenue) {
        this.onetimeRevenue = onetimeRevenue;
    }
    
    public BigDecimal getFormattedOnetimeRevenue() {
        if (onetimeRevenue != null) {
            if (startDate.getYear() == month.getYear() &&
                startDate.getMonthOfYear() == month.getMonthOfYear()) {
                return onetimeRevenue.setScale(2, RoundingMode.HALF_UP);
            }
        }
        return new BigDecimal(0).setScale(2, RoundingMode.HALF_UP); // onetimeRevenue only applies in the first month
    }

    @JsonIgnore
    public DateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(DateTime startDate) {
        this.startDate = startDate;
    }

    @JsonIgnore
    public DateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(DateTime endDate) {
        this.endDate = endDate;
    }

    @Override
    public Double getMonthFraction() {
        DateTime leftDate = this.month; // should be first day of the month...
        if (startDate.isAfter(leftDate)) {
            leftDate = startDate;
        }
        DateTime rightDate = this.month.dayOfMonth().withMaximumValue();
        if (endDate != null && endDate.isBefore(rightDate)) {
            rightDate = endDate;
        }
        return new Fraction((rightDate.dayOfMonth().get() - leftDate.dayOfMonth().get()+1), this.days).doubleValue();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + Objects.hashCode(this.id);
        hash = 67 * hash + Objects.hashCode(this.type);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LineItemMonthlyRevenue other = (LineItemMonthlyRevenue) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (this.type != other.type) {
            return false;
        }
        return true;
    }
}
