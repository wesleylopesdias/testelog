package com.logicalis.serviceinsight.data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class QuoteMin implements Comparable<QuoteMin> {

	private Long id;
	private String quoteNumber;
	private String quoteName;
	private BigDecimal onetimePrice;
	private BigDecimal recurringPrice;
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="MMddyyyy", timezone="America/New_York")
	private Date closeDate;
	private Integer termMonths;
	private BigDecimal total;
	private List<QuoteLineItemMin> lineItems = new ArrayList<QuoteLineItemMin>();
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getQuoteNumber() {
		return quoteNumber;
	}
	
	public void setQuoteNumber(String quoteNumber) {
		this.quoteNumber = quoteNumber;
	}
	
	public String getQuoteName() {
		return quoteName;
	}
	
	public void setQuoteName(String quoteName) {
		this.quoteName = quoteName;
	}
	
	public BigDecimal getOnetimePrice() {
		return onetimePrice;
	}
	
	public void setOnetimePrice(BigDecimal onetimePrice) {
		this.onetimePrice = onetimePrice;
	}
	
	public BigDecimal getRecurringPrice() {
		return recurringPrice;
	}
	
	public void setRecurringPrice(BigDecimal recurringPrice) {
		this.recurringPrice = recurringPrice;
	}
	
	public Date getCloseDate() {
		return closeDate;
	}
	
	public void setCloseDate(Date closeDate) {
		this.closeDate = closeDate;
	}
	
	public Integer getTermMonths() {
		return termMonths;
	}
	
	public void setTermMonths(Integer termMonths) {
		this.termMonths = termMonths;
	}
	
	public BigDecimal getTotal() {
		return total;
	}
	
	public void setTotal(BigDecimal total) {
		this.total = total;
	}
	
	public List<QuoteLineItemMin> getLineItems() {
		return lineItems;
	}
	
	public void setLineItems(List<QuoteLineItemMin> lineItems) {
		this.lineItems = lineItems;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		QuoteMin other = (QuoteMin) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	@Override
    public int compareTo(QuoteMin o) {
        if (o == null) {
            return 1;
        }
        if (getId() != null) {
            if (o.getId() == null) {
                return 1;
            }
            int idx = getId().compareTo(o.getId());
            if (idx != 0) {
                return idx;
            }
        }
        return 0;
    }

	@Override
	public String toString() {
		return "QuoteMin [id=" + id + ", quoteNumber=" + quoteNumber + ", quoteName=" + quoteName + ", onetimePrice="
				+ onetimePrice + ", recurringPrice=" + recurringPrice + ", closeDate=" + closeDate + ", termMonths="
				+ termMonths + ", total=" + total + ", lineItems=" + lineItems + "]";
	}
	
	
	
}
