package com.logicalis.serviceinsight.remote;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "contract",
    "lineItems"
})
public class ContractResponseDetails {

    @JsonProperty("contract")
    private GenericResponse contract;
    @JsonProperty("lineItems")
    private ContractResponseLineItems lineItems = new ContractResponseLineItems();

    public ContractResponseDetails() {
		super();
	}

	@JsonProperty("contract")
    public GenericResponse getContract() {
        return contract;
    }

    @JsonProperty("contract")
    public void setContract(GenericResponse contract) {
        this.contract = contract;
    }

    @JsonProperty("lineItems")
    public ContractResponseLineItems getLineItems() {
        return lineItems;
    }

    @JsonProperty("lineItems")
    public void setLineItems(ContractResponseLineItems lineItems) {
        this.lineItems = lineItems;
    }

	@Override
	public String toString() {
		return "ContractResponseDetails [contract=" + contract + ", lineItems=" + lineItems + "]";
	}

}
