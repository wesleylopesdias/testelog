package com.logicalis.serviceinsight.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.logicalis.serviceinsight.util.VersionUtil;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import org.apache.commons.math3.fraction.Fraction;
import org.joda.time.DateTime;

/**
 *
 * @author poneil
 */
public class ServiceMonthlyRevenue extends MonthlyRevenue {
    
    private Long contractId;
    private Long serviceId;
    private String code;
    private DateTime startDate;
    private DateTime endDate;
    private String service;
    private Double version;
    private BigDecimal onetimeRevenue;
    private List<LineItemMonthlyRevenue> lineItems = new ArrayList<LineItemMonthlyRevenue>();
    
    public ServiceMonthlyRevenue() {
        super();
    }
    
    public ServiceMonthlyRevenue(Date startDate, Date endDate, Date forMonth, BigDecimal revenue, Long id,
            Long contractId, Long serviceId, String code, String service, Double version, BigDecimal onetimeRevenue,
            Integer quantity) {
        super(id, Type.SERVICE, forMonth, revenue, quantity);
        this.contractId = contractId;
        this.serviceId = serviceId;
        this.code = code;
        this.startDate = new DateTime(startDate);
        if (endDate != null) {
            this.endDate = new DateTime(endDate);
        }
        this.service = service;
        this.version = version;
        this.onetimeRevenue = onetimeRevenue;
    }

    public Long getContractId() {
        return contractId;
    }

    public void setContractId(Long contractId) {
        this.contractId = contractId;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public BigDecimal getOnetimeRevenue() {
        
        if (getStartDate().getYear() == month.getYear() &&
                getStartDate().getMonthOfYear() == month.getMonthOfYear()) {
            return onetimeRevenue;
        }
        return new BigDecimal(0); // onetimeRevenue only applies in the first month
    }

    public void setOnetimeRevenue(BigDecimal onetimeRevenue) {
        this.onetimeRevenue = onetimeRevenue;
    }
    
    public BigDecimal getFormattedOnetimeRevenue() {
        if (onetimeRevenue != null) {
            if (getStartDate().getYear() == month.getYear() &&
                getStartDate().getMonthOfYear() == month.getMonthOfYear()) {
                return onetimeRevenue.setScale(2, RoundingMode.HALF_UP);
            }
        }
        return new BigDecimal(0).setScale(2, RoundingMode.HALF_UP); // onetimeRevenue only applies in the first month
    }

    public List<LineItemMonthlyRevenue> getLineItems() {
        return lineItems;
    }

    public void setLineItems(List<LineItemMonthlyRevenue> lineItems) {
        this.lineItems = lineItems;
    }
    
    public void addLineItem(LineItemMonthlyRevenue lineItem) {
        lineItems.add(lineItem);
    }

    public Double getVersion() {
        return version;
    }

    public void setVersion(Double version) {
        this.version = version;
    }
    
    /**
     * should format a Double into a "version" looking string, like 1.11 becomes 1.1.1
     * 
     * @param version
     * @return 
     */
    public String getFormattedVersion() {
        return VersionUtil.formatVersion(getVersion());
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
        int hash = 7;
        hash = 23 * hash + Objects.hashCode(this.id);
        hash = 23 * hash + Objects.hashCode(this.type);
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
        final ServiceMonthlyRevenue other = (ServiceMonthlyRevenue) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (this.type != other.type) {
            return false;
        }
        return true;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
