package com.logicalis.serviceinsight.representation;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.math3.fraction.Fraction;
import org.joda.time.DateTime;

/**
 *
 * @author poneil
 */
public class SPLARevenue implements Comparable<SPLARevenue> {

    private Long customerId;
    private String customer;
    private Long contractId;
    private String contract;
    private Long ospId;
    private String service;
    private Integer quantity;
    private Integer unitCount;
    private BigDecimal onetimeRevenue;
    private BigDecimal recurringRevenue;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "America/New_York")
    private DateTime monthof;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "America/New_York")
    private DateTime startDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "America/New_York")
    private DateTime endDate;
    private Long deviceId;
    private String device;
    private String partNumber;
    private Long splaId;
    private String spla;
    private Integer splaExpenseCategoryId;

    /**
     * default CTOR
     */
    public SPLARevenue() {
    }

    /**
     * Basic CTOR without Device, SPLA info...
     */
    public SPLARevenue(Long customerId, String customer, Long contractId, String contract, Long ospId,
            String service, Integer quantity, Integer unitCount, BigDecimal onetimeRevenue, BigDecimal recurringRevenue, DateTime monthof,
            DateTime startDate, DateTime endDate) {
        this.customerId = customerId;
        this.customer = customer;
        this.contractId = contractId;
        this.contract = contract;
        this.ospId = ospId;
        this.service = service;
        this.quantity = quantity;
        this.unitCount = unitCount;
        this.onetimeRevenue = onetimeRevenue;
        this.recurringRevenue = recurringRevenue;
        this.monthof = monthof;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    /**
     * CTOR with Device info
     */
    public SPLARevenue(Long customerId, String customer, Long contractId, String contract, Long ospId,
            String service, Integer quantity, Integer unitCount, BigDecimal onetimeRevenue, BigDecimal recurringRevenue, DateTime monthof,
            DateTime startDate, DateTime endDate, Long deviceId, String device, String partNumber) {
        this.customerId = customerId;
        this.customer = customer;
        this.contractId = contractId;
        this.contract = contract;
        this.ospId = ospId;
        this.service = service;
        this.quantity = quantity;
        this.unitCount = unitCount;
        this.onetimeRevenue = onetimeRevenue;
        this.recurringRevenue = recurringRevenue;
        this.monthof = monthof;
        this.startDate = startDate;
        this.endDate = endDate;
        this.deviceId = deviceId;
        this.device = device;
        this.partNumber = partNumber;
    }

    /**
     * CTOR with Device, SPLA info
     */
    public SPLARevenue(Long customerId, String customer, Long contractId, String contract, Long ospId,
            String service, Integer quantity, Integer unitCount, BigDecimal onetimeRevenue, BigDecimal recurringRevenue, DateTime monthof,
            DateTime startDate, DateTime endDate, Long deviceId, String device, String partNumber, Long splaId, String spla,
            Integer splaExpenseCategoryId) {
        this.customerId = customerId;
        this.customer = customer;
        this.contractId = contractId;
        this.contract = contract;
        this.ospId = ospId;
        this.service = service;
        this.quantity = quantity;
        this.unitCount = unitCount;
        this.onetimeRevenue = onetimeRevenue;
        this.recurringRevenue = recurringRevenue;
        this.monthof = monthof;
        this.startDate = startDate;
        this.endDate = endDate;
        this.deviceId = deviceId;
        this.device = device;
        this.partNumber = partNumber;
        this.splaId = splaId;
        this.spla = spla;
        this.splaExpenseCategoryId = splaExpenseCategoryId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public Long getContractId() {
        return contractId;
    }

    public void setContractId(Long contractId) {
        this.contractId = contractId;
    }

    public String getContract() {
        return contract;
    }

    public void setContract(String contract) {
        this.contract = contract;
    }

    public Long getOspId() {
        return ospId;
    }

    public void setOspId(Long ospId) {
        this.ospId = ospId;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getUnitCount() {
        return unitCount;
    }

    public void setUnitCount(Integer unitCount) {
        this.unitCount = unitCount;
    }

    public BigDecimal getOnetimeRevenue() {
        if (monthof != null) {
            if (startDate.getYear() == monthof.getYear()
                    && startDate.getMonthOfYear() == monthof.getMonthOfYear()) {
                return onetimeRevenue;
            }
            return new BigDecimal(0); // onetimeRevenue only applies in the first month
        }
        return onetimeRevenue;
    }

    public BigDecimal getFormattedOnetimeRevenue() {
        if (onetimeRevenue != null) {
            if (monthof != null) {
                if (startDate.getYear() == monthof.getYear()
                        && startDate.getMonthOfYear() == monthof.getMonthOfYear()) {
                    return onetimeRevenue.setScale(2, RoundingMode.HALF_UP);
                }
                return new BigDecimal(0); // onetimeRevenue only applies in the first month
            }
            return onetimeRevenue.setScale(2, RoundingMode.HALF_UP);
        }
        return new BigDecimal(0).setScale(2, RoundingMode.HALF_UP); // onetimeRevenue only applies in the first month
    }

    public void setOnetimeRevenue(BigDecimal onetimeRevenue) {
        this.onetimeRevenue = onetimeRevenue;
    }

    public BigDecimal getRecurringRevenue() {
        if (recurringRevenue == null) {
            return new BigDecimal(0);
        }
        if (monthof != null) {
            return recurringRevenue
                    .multiply(new BigDecimal(getMonthFraction()));
        }
        return recurringRevenue;
    }

    public BigDecimal getFormattedRecurringRevenue() {
        if (recurringRevenue != null) {
            if (monthof != null) {
                return recurringRevenue
                        .multiply(new BigDecimal(getMonthFraction()))
                        .setScale(2, RoundingMode.HALF_UP);
            }
            return recurringRevenue.setScale(2, RoundingMode.HALF_UP);
        }
        return new BigDecimal(0).setScale(2, RoundingMode.HALF_UP);
    }

    public void setRecurringRevenue(BigDecimal recurringRevenue) {
        this.recurringRevenue = recurringRevenue;
    }
    
    public BigDecimal getTotalRevenue() {
        return getOnetimeRevenue().add(getRecurringRevenue());
    }
    
    public BigDecimal getFormattedTotalRevenue() {
        return getOnetimeRevenue().add(getRecurringRevenue()).setScale(2, RoundingMode.HALF_UP);
    }

    public DateTime getMonthof() {
        return monthof;
    }

    public void setMonthof(DateTime monthof) {
        this.monthof = monthof;
    }

    public Double getMonthFraction() {
        if (monthof != null) {
            DateTime leftDate = this.monthof; // should be first day of the month...
            if (startDate.isAfter(leftDate)) {
                leftDate = startDate;
            }
            DateTime rightDate = this.monthof.dayOfMonth().withMaximumValue();
            if (endDate != null && endDate.isBefore(rightDate)) {
                rightDate = endDate;
            }
            return new Fraction((rightDate.dayOfMonth().get() - leftDate.dayOfMonth().get() + 1), monthof.dayOfMonth().getMaximumValue()).doubleValue();
        }
        return null;
    }

    public DateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(DateTime startDate) {
        this.startDate = startDate;
    }

    public DateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(DateTime endDate) {
        this.endDate = endDate;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public Long getSplaId() {
        return splaId;
    }

    public void setSplaId(Long splaId) {
        this.splaId = splaId;
    }

    public String getSpla() {
        return spla;
    }

    public void setSpla(String spla) {
        this.spla = spla;
    }

    public Integer getSplaExpenseCategoryId() {
        return splaExpenseCategoryId;
    }

    public void setSplaExpenseCategoryId(Integer splaExpenseCategoryId) {
        this.splaExpenseCategoryId = splaExpenseCategoryId;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + Objects.hashCode(this.customerId);
        hash = 43 * hash + Objects.hashCode(this.contractId);
        hash = 43 * hash + Objects.hashCode(this.ospId);
        hash = 43 * hash + Objects.hashCode(this.startDate);
        hash = 43 * hash + Objects.hashCode(this.endDate);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SPLARevenue other = (SPLARevenue) obj;
        if (!Objects.equals(this.customerId, other.customerId)) {
            return false;
        }
        if (!Objects.equals(this.contractId, other.contractId)) {
            return false;
        }
        if (!Objects.equals(this.ospId, other.ospId)) {
            return false;
        }
        if (!Objects.equals(this.startDate, other.startDate)) {
            return false;
        }
        if (!Objects.equals(this.endDate, other.endDate)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "SPLARevenue{" + "customerId=" + customerId + ", customer=" + customer + ", contractId=" + contractId + ", contract=" + contract + ", ospId=" + ospId + ", service=" + service + ", quantity=" + quantity + ", unitCount=" + unitCount + ", onetimeRevenue=" + onetimeRevenue + ", recurringRevenue=" + recurringRevenue + ", monthof=" + monthof + ", startDate=" + startDate + ", endDate=" + endDate + ", deviceId=" + deviceId + ", device=" + device + ", partNumber=" + partNumber + ", splaId=" + splaId + ", spla=" + spla + ", splaExpenseCategoryId=" + splaExpenseCategoryId + '}';
    }

    @Override
    public int compareTo(SPLARevenue o) {
        int result = ObjectUtils.compare(this.customerId, o.getCustomerId());
        if (result != 0) {
            return result;
        }
        result = ObjectUtils.compare(this.contractId, o.getContractId());
        if (result != 0) {
            return result;
        }
        result = ObjectUtils.compare(this.ospId, o.getOspId());
        if (result != 0) {
            return result;
        }
        result = ObjectUtils.compare(this.monthof, o.getMonthof());
        if (result != 0) {
            return result;
        }
        result = ObjectUtils.compare(this.deviceId, o.getDeviceId());
        if (result != 0) {
            return result;
        }
        return ObjectUtils.compare(this.splaId, o.getSplaId());
    }
}
