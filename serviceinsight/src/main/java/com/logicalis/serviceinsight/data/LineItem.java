package com.logicalis.serviceinsight.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

/**
 *
 * @author poneil
 */
public class LineItem {
    private Long id;
    private Long contractId;
    private Long contractServiceId;
    private Long serviceId;
    private String name;
    private String units;
    private String mode;
    private Integer quantity = 1;
    private BigDecimal onetimeRevenue;
    private BigDecimal recurringRevenue;
    private BigDecimal breakpoint;
    private DateTime startDate;
    private DateTime endDate;
    
    public LineItem(){}
    
    public LineItem(Long id, Long contractId, Long contractServiceId, Long serviceId, String name, String units, String mode,
            Integer quantity, BigDecimal onetimeRevenue, BigDecimal recurringRevenue, BigDecimal breakpoint,
            Date sd, Date ed) {
        this.id = id;
        this.contractId = contractId;
        this.contractServiceId = contractServiceId;
        this.serviceId = serviceId;
        this.name = name;
        this.units = units;
        this.mode = mode;
        this.quantity = quantity;
        this.onetimeRevenue = onetimeRevenue;
        this.recurringRevenue = recurringRevenue;
        this.breakpoint = breakpoint;
        if (sd != null) {
            startDate = new DateTime(sd);
        }
        if (ed != null) {
            endDate = new DateTime(ed);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    @JsonIgnore
    public DateTime getStartDate() {
        return startDate;
    }
    
    public String getFormattedStartDate() {
        if (startDate != null) {
            return DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss z").print(startDate);
        }
        return null;
    }

    public void setStartDate(DateTime startDate) {
        this.startDate = startDate;
    }

    @JsonIgnore
    public DateTime getEndDate() {
        return endDate;
    }
    
    public String getFormattedEndDate() {
        if (endDate != null) {
            return DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss z").print(endDate);
        }
        return null;
    }

    public void setEndDate(DateTime endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getOnetimeRevenue() {
        return onetimeRevenue;
    }
    
    public BigDecimal getFormattedOnetimeRevenue() {
        if (onetimeRevenue != null) {
            return onetimeRevenue.setScale(2, RoundingMode.HALF_UP);
        }
        return new BigDecimal(0).setScale(2, RoundingMode.HALF_UP);
    }

    public void setOnetimeRevenue(BigDecimal onetimeRevenue) {
        this.onetimeRevenue = onetimeRevenue;
    }

    public BigDecimal getRecurringRevenue() {
        return recurringRevenue;
    }
    
    public BigDecimal getFormattedRecurringRevenue() {
        if (recurringRevenue != null) {
            return recurringRevenue.setScale(2, RoundingMode.HALF_UP);
        }
        return new BigDecimal(0).setScale(2, RoundingMode.HALF_UP);
    }

    public void setRecurringRevenue(BigDecimal recurringRevenue) {
        this.recurringRevenue = recurringRevenue;
    }

    public BigDecimal getBreakpoint() {
        return breakpoint;
    }
    
    public BigDecimal getFormattedBreakpoint() {
        if (breakpoint != null) {
            return breakpoint.setScale(2, RoundingMode.HALF_UP);
        }
        return new BigDecimal(0).setScale(2, RoundingMode.HALF_UP);
    }

    public void setBreakpoint(BigDecimal breakpoint) {
        this.breakpoint = breakpoint;
    }
}
