package com.logicalis.serviceinsight.service;

import java.util.List;

import org.joda.time.DateTime;

import com.logicalis.serviceinsight.data.Contract;
import com.logicalis.serviceinsight.data.PipelineQuoteMin;
import com.logicalis.serviceinsight.data.PricingProductMin;
import com.logicalis.serviceinsight.data.QuoteMin;

public interface PricingIntegrationService extends BaseService {
	
    public List<PipelineQuoteMin> findQuotesForPipeline(DateTime startDate, DateTime endDate) throws ServiceException;
    public String getPricingQuoteUrl();
	public List<QuoteMin> findQuotesForCustomer(Long customerId) throws ServiceException;
	public QuoteMin quote(Long quoteId) throws ServiceException;
	public void importQuote(Long quoteId, Contract contract) throws ServiceException;
	public QuoteMin markQuoteAsWon(Long quoteId) throws ServiceException;
	
	public List<PricingProductMin> findPricingToolProducts() throws ServiceException;
	public List<PricingProductMin> findM365Products() throws ServiceException;
	public List<PricingProductMin> findO365Products() throws ServiceException;
	public List<PricingProductMin> findM365NCProducts() throws ServiceException;
	public void deviceSync(Boolean override) throws ServiceException;
	public void devicePricingSync() throws ServiceException;
	public void m365Sync() throws ServiceException;
	public void o365Sync() throws ServiceException;
	public void m365NCSync() throws ServiceException;
	
	public void syncRemoteCustomers();
	
}
