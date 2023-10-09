package com.logicalis.serviceinsight.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

public class MicrosoftPriceList {

	public enum MicrosoftPriceListType {
		M365("M365"), M365NC("M365NC");
		
		MicrosoftPriceListType(String description) {
			this.description = description;
		}
		
		private String description;
		
		public String getDescription() {
			return description;
		}
	}
	
	private Long id;
	private MicrosoftPriceListType type;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MMddyyyy", timezone = "America/New_York")
	private Date month;
	private Integer productCount;
	private List<? extends MicrosoftPriceListProduct> products = new ArrayList<>();
	
	public MicrosoftPriceList() {}
	
	public MicrosoftPriceList(Long id, MicrosoftPriceListType type, Date month) {
		super();
		this.id = id;
		this.type = type;
		this.month = month;
	}



	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public MicrosoftPriceListType getType() {
		return type;
	}
	
	public void setType(MicrosoftPriceListType type) {
		this.type = type;
	}
	
	public Date getMonth() {
		return month;
	}
	
	public void setMonth(Date month) {
		this.month = month;
	}

	public Integer getProductCount() {
		return productCount;
	}

	public void setProductCount(Integer productCount) {
		this.productCount = productCount;
	}

	public List<? extends MicrosoftPriceListProduct> getProducts() {
		return products;
	}

	public void setProducts(List<? extends MicrosoftPriceListProduct> products) {
		this.products = products;
	}

	@Override
	public String toString() {
		return "MicrosoftPriceList [id=" + id + ", type=" + type + ", month=" + month + "]";
	}
	
}
