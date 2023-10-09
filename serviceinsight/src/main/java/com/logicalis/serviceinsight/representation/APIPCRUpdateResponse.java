package com.logicalis.serviceinsight.representation;

import java.util.ArrayList;
import java.util.List;

import com.logicalis.serviceinsight.service.BatchResult;

public class APIPCRUpdateResponse {

	public enum Result {
        success, failed
    }
	
	private Long pcrId;
	private Result pcrStatus;
	private String pcrMessage;
	private List<BatchResult> batchResults = new ArrayList<BatchResult>();
	
	public Long getPcrId() {
		return pcrId;
	}
	
	public void setPcrId(Long pcrId) {
		this.pcrId = pcrId;
	}
	
	public Result getPcrStatus() {
		return pcrStatus;
	}
	
	public void setPcrStatus(Result pcrStatus) {
		this.pcrStatus = pcrStatus;
	}
	
	public String getPcrMessage() {
		return pcrMessage;
	}
	
	public void setPcrMessage(String pcrMessage) {
		this.pcrMessage = pcrMessage;
	}
	
	public List<BatchResult> getBatchResults() {
		return batchResults;
	}
	
	public void setBatchResults(List<BatchResult> batchResults) {
		this.batchResults = batchResults;
	}

	@Override
	public String toString() {
		return "APIPCRUpdateResponse [pcrId=" + pcrId + ", pcrStatus=" + pcrStatus + ", pcrMessage=" + pcrMessage
				+ ", batchResults=" + batchResults + "]";
	}
	
}
