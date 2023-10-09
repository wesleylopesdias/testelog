package com.logicalis.serviceinsight.remote;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "response"
})
public class ContractResponse {

    @JsonProperty("response")
    private ContractResponseDetails response;

    public ContractResponse() {
		super();
	}

	@JsonProperty("response")
    public ContractResponseDetails getResponse() {
        return response;
    }

    @JsonProperty("response")
    public void setResponse(ContractResponseDetails response) {
        this.response = response;
    }

	@Override
	public String toString() {
		return "ContractResponse [response=" + response + "]";
	}

}
