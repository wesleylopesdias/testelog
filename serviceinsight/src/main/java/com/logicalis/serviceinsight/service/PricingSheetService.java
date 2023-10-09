package com.logicalis.serviceinsight.service;

import java.util.List;

import com.logicalis.serviceinsight.data.PricingSheet;
import com.logicalis.serviceinsight.data.PricingSheetProduct;

public interface PricingSheetService extends BaseService {

	public PricingSheet pricingSheet(Long id) throws ServiceException;
	public PricingSheet findPricingSheetForContract(Long contractId) throws ServiceException;
	public PricingSheet findPricingSheetForContractWithActiveProducts(Long contractId) throws ServiceException;
	public Long savePricingSheet(PricingSheet pricingSheet) throws ServiceException;
	public void updatePricingSheet(PricingSheet pricingSheet) throws ServiceException;
	public void deletePricingSheet(Long pricingSheetId) throws ServiceException;
	public List<PricingSheet> pricingSheets() throws ServiceException;
	public PricingSheetProduct pricingSheetProductByDeviceAndContractId(Long contractId, Long deviceId) throws ServiceException;
	public void generatePricingSheetForContract(Long contractId) throws ServiceException;
	public void generatePricingSheetsForAllCustomers();
	
	public PricingSheetProduct pricingSheetProduct(Long id) throws ServiceException;
	public Long savePricingSheetProduct(PricingSheetProduct pricingSheetProduct) throws ServiceException;
	public void updatePricingSheetProduct(PricingSheetProduct pricingSheetProduct) throws ServiceException;
	public void updatePricingSheetProductBits(PricingSheetProduct pricingSheetProduct) throws ServiceException;
	public void deletePricingSheetProduct(Long pricingSheetProductId) throws ServiceException;
	
}
