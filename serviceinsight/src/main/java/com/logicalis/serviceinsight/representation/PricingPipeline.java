package com.logicalis.serviceinsight.representation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PricingPipeline implements Serializable {

    private String pricingBaseURL;
    private List<PricingPipelineService> services;
    private List<PricingPipelineQuoteByCustomer> customerQuotes;
    
    public PricingPipeline() {
        super();
        pricingBaseURL = "";
        this.services = new ArrayList<PricingPipelineService>();
        this.customerQuotes = new ArrayList<PricingPipelineQuoteByCustomer>();
    }
    
    public String getPricingBaseURL() {
        return this.pricingBaseURL;
    }
    
    public void setPricingBaseURL(String url) {
        this.pricingBaseURL = url;
    }
    
    public List<PricingPipelineService> getServices() {
        return services;
    }
    
    public void setServices(List<PricingPipelineService> services) {
        this.services = services;
    }
    
    public void addService(PricingPipelineService service) {
        this.services.add(service);
    }
    
    public List<PricingPipelineQuoteByCustomer> getCustomerQuotes() {
        return customerQuotes;
    }
    
    public void setCustomerQuotes(List<PricingPipelineQuoteByCustomer> customers) {
        this.customerQuotes = customers;
    }
    
    public void addCustomerQuote(PricingPipelineQuoteByCustomer customerQuote) {
        this.customerQuotes.add(customerQuote);
    }
}
