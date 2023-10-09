package com.logicalis.serviceinsight.representation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

public class APIPCRUpdateRequest {

	public enum Type {
		rim("RIM"), rimPCR("rimPCR"), rimPCRSign("rimPCRSign"), cloud("Cloud"), cloudPCR("cloudPCR"), prcpOnboard("PRCPOnboard"), prcpSOW("prcpSOW"), 
		textPCR("textPCR"), azureSOW("Azure SOW"), azurePCR("Azure PCR");
		
		private String description;

        Type(String description) {
            this.description = description;
        }

        public String getDescription() {
            return this.description;
        }
	}
	
	public enum SubType {
		extension("Contract Extension");
		
		private String description;

		SubType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return this.description;
        }
	}
	
	private Long pcrId;
	private String pcrName;
	private Type pcrType;
	private SubType pcrSubType;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MMddyyyy", timezone = "America/New_York")
	private Date pcrEffectiveDate;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MMddyyyy", timezone = "America/New_York")
	private Date pcrSignedDate;
	private String pcrJobNumber;
	private String pcrNotes;
	private Long contractId;
	private BigDecimal onetimePrice;
	private BigDecimal recurringPrice;
	private List<APIContractService> contractServices = new ArrayList<APIContractService>();
	private List<APIPricingSheetProduct> pricingSheetProducts = new ArrayList<APIPricingSheetProduct>();
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MMddyyyy", timezone = "America/New_York")
	private Date newContractEndDate;
	private String pcrDocName;
	private String pcrDocContentType;
	private byte[] pcrDocContent; 
	
	public Long getPcrId() {
		return pcrId;
	}
	
	public void setPcrId(Long pcrId) {
		this.pcrId = pcrId;
	}
	
	public String getPcrName() {
		return pcrName;
	}
	
	public void setPcrName(String pcrName) {
		this.pcrName = pcrName;
	}
	
	public Type getPcrType() {
		return pcrType;
	}

	public void setPcrType(Type pcrType) {
		this.pcrType = pcrType;
	}

	public SubType getPcrSubType() {
		return pcrSubType;
	}

	public void setPcrSubType(SubType pcrSubType) {
		this.pcrSubType = pcrSubType;
	}

	public Date getNewContractEndDate() {
		return newContractEndDate;
	}

	public void setNewContractEndDate(Date newContractEndDate) {
		this.newContractEndDate = newContractEndDate;
	}

	public Date getPcrEffectiveDate() {
		return pcrEffectiveDate;
	}
	
	public void setPcrEffectiveDate(Date pcrEffectiveDate) {
		this.pcrEffectiveDate = pcrEffectiveDate;
	}
	
	public Date getPcrSignedDate() {
		return pcrSignedDate;
	}
	
	public void setPcrSignedDate(Date pcrSignedDate) {
		this.pcrSignedDate = pcrSignedDate;
	}
	
	public String getPcrJobNumber() {
		return pcrJobNumber;
	}
	
	public void setPcrJobNumber(String pcrJobNumber) {
		this.pcrJobNumber = pcrJobNumber;
	}
	
	public String getPcrNotes() {
		return pcrNotes;
	}
	
	public void setPcrNotes(String pcrNotes) {
		this.pcrNotes = pcrNotes;
	}
	
	public Long getContractId() {
		return contractId;
	}

	public void setContractId(Long contractId) {
		this.contractId = contractId;
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

	public List<APIContractService> getContractServices() {
		return contractServices;
	}
	
	public void setContractServices(List<APIContractService> contractServices) {
		this.contractServices = contractServices;
	}

	public List<APIPricingSheetProduct> getPricingSheetProducts() {
		return pricingSheetProducts;
	}

	public void setPricingSheetProducts(List<APIPricingSheetProduct> pricingSheetProducts) {
		this.pricingSheetProducts = pricingSheetProducts;
	}

	public String getPcrDocName() {
		return pcrDocName;
	}

	public void setPcrDocName(String pcrDocName) {
		this.pcrDocName = pcrDocName;
	}

	public String getPcrDocContentType() {
		return pcrDocContentType;
	}

	public void setPcrDocContentType(String pcrDocContentType) {
		this.pcrDocContentType = pcrDocContentType;
	}

	public byte[] getPcrDocContent() {
		return pcrDocContent;
	}

	public void setPcrDocContent(byte[] pcrDocContent) {
		this.pcrDocContent = pcrDocContent;
	}

	@Override
	public String toString() {
		return "APIPCRUpdateRequest [pcrId=" + pcrId + ", pcrName=" + pcrName + ", pcrType=" + pcrType + ", pcrSubType="
				+ pcrSubType + ", pcrEffectiveDate=" + pcrEffectiveDate + ", pcrSignedDate=" + pcrSignedDate
				+ ", pcrJobNumber=" + pcrJobNumber + ", pcrNotes=" + pcrNotes + ", contractId=" + contractId
				+ ", onetimePrice=" + onetimePrice + ", recurringPrice=" + recurringPrice + ", contractServices="
				+ contractServices + ", pricingSheetProducts=" + pricingSheetProducts + ", newContractEndDate="
				+ newContractEndDate + ", pcrDocName=" + pcrDocName + ", pcrDocContentType=" + pcrDocContentType + "]";
	}
	
}
