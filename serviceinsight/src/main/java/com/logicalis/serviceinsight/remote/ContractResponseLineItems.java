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
public class ContractResponseLineItems {

    @JsonProperty("services")
    private List<GenericResponse> services = new ArrayList<GenericResponse>();
    @JsonProperty("subscriptionServices")
    private List<GenericResponse> subscriptionServices = new ArrayList<GenericResponse>();

    public ContractResponseLineItems() {
		super();
	}

	@JsonProperty("services")
    public List<GenericResponse> getServices() {
        return services;
    }

    @JsonProperty("services")
    public void setServices(List<GenericResponse> services) {
        this.services = services;
    }

    @JsonProperty("subscriptionServices")
    public List<GenericResponse> getSubscriptionServices() {
        return subscriptionServices;
    }

    @JsonProperty("subscriptionServices")
    public void setSubscriptionServices(List<GenericResponse> subscriptionServices) {
        this.subscriptionServices = subscriptionServices;
    }

	@Override
	public String toString() {
		return "ContractResponseLineItems [services=" + services + ", subscriptionServices=" + subscriptionServices
				+ "]";
	}
    
    

}
