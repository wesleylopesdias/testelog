package com.logicalis.serviceinsight.remote;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "services",
    "subscriptionServices"
})
public class ContractRequestLineItems {

    @JsonProperty("services")
    private List<ContractRequestService> services = new ArrayList<ContractRequestService>();
    @JsonProperty("subscriptionServices")
    private List<ContractRequestService> subscriptionServices = new ArrayList<ContractRequestService>();

    public ContractRequestLineItems() {
		super();
	}

	@JsonProperty("services")
    public List<ContractRequestService> getServices() {
        return services;
    }

    @JsonProperty("services")
    public void setServices(List<ContractRequestService> services) {
        this.services = services;
    }

    @JsonProperty("subscriptionServices")
    public List<ContractRequestService> getSubscriptionServices() {
        return subscriptionServices;
    }

    @JsonProperty("subscriptionServices")
    public void setSubscriptionServices(List<ContractRequestService> subscriptionServices) {
        this.subscriptionServices = subscriptionServices;
    }

	@Override
	public String toString() {
		return "ContractRequestLineItems [services=" + services + ", subscriptionServices=" + subscriptionServices
				+ "]";
	}

}
