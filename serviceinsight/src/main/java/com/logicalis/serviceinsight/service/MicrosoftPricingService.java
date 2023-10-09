package com.logicalis.serviceinsight.service;

import java.io.File;
import java.util.Date;
import java.util.List;

import com.logicalis.serviceinsight.data.Microsoft365SubscriptionConfig;
import com.logicalis.serviceinsight.data.MicrosoftPriceList;
import com.logicalis.serviceinsight.data.MicrosoftPriceListM365Product;
import com.logicalis.serviceinsight.data.MicrosoftPriceListProduct;

public interface MicrosoftPricingService extends BaseService {

	public List<MicrosoftPriceList> getMicrosoftPriceLists();
	public MicrosoftPriceList getMicrosoftPriceListForMonthOf(Date month, MicrosoftPriceList.MicrosoftPriceListType type);
	public MicrosoftPriceList getLatestMicrosoftPriceList(MicrosoftPriceList.MicrosoftPriceListType type);
	public List<? extends MicrosoftPriceListProduct> getMicrosoftPriceListProductByPriceListId(Long microsoftPriceListId, MicrosoftPriceList.MicrosoftPriceListType type);
	public MicrosoftPriceListM365Product getMicrosoftPriceListProductByOfferId(Long microsoftPriceListId, String offerId);
	public void importMicrosoftPriceList(File uploaded, Date month, MicrosoftPriceList.MicrosoftPriceListType type) throws ServiceException;
	
	public Microsoft365SubscriptionConfig getMicrosoft365SubscriptionConfig(Long microsoft365SubscriptionConfigId);
	public Microsoft365SubscriptionConfig getMicrosoft365SubscriptionConfigByActiveAndTenantId(String tenantId);
	public List<Microsoft365SubscriptionConfig> getMicrosoft365SubscriptionConfigForContract(Long contractId);
	public Long saveMicrosoft365SubscriptionConfig(Microsoft365SubscriptionConfig config) throws ServiceException;
	public void updateMicrosoft365SubscriptionConfig(Microsoft365SubscriptionConfig config) throws ServiceException;
	public void deleteMicrosoft365SubscriptionConfig(Long configId) throws ServiceException;
}
