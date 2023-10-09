package com.logicalis.serviceinsight.remote;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "contract",
    "lineItems"
})
public class ContractRequest {

    @JsonProperty("contract")
    private ContractRequestContract contract;
    @JsonProperty("lineItems")
    private ContractRequestLineItems lineItems;

    public ContractRequest() {
		super();
	}

	@JsonProperty("contract")
    public ContractRequestContract getContract() {
        return contract;
    }

    @JsonProperty("contract")
    public void setContract(ContractRequestContract contract) {
        this.contract = contract;
    }

    @JsonProperty("lineItems")
    public ContractRequestLineItems getLineItems() {
        return lineItems;
    }

    @JsonProperty("lineItems")
    public void setLineItems(ContractRequestLineItems lineItems) {
        this.lineItems = lineItems;
    }

	@Override
	public String toString() {
		return "ContractRequest [contract=" + contract + ", lineItems=" + lineItems + "]";
	}

}