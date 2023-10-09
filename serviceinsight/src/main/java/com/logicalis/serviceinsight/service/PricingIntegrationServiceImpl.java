package com.logicalis.serviceinsight.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import com.logicalis.serviceinsight.dao.CostItem;
import com.logicalis.serviceinsight.dao.DeviceExpenseCategory;
import com.logicalis.serviceinsight.dao.ExpenseCategory;
import com.logicalis.serviceinsight.data.Contract;
import com.logicalis.serviceinsight.data.ContractServiceSubscription;
import com.logicalis.serviceinsight.data.Customer;
import com.logicalis.serviceinsight.data.Device;
import com.logicalis.serviceinsight.data.Device.DeviceType;
import com.logicalis.serviceinsight.data.MicrosoftPriceList;
import com.logicalis.serviceinsight.data.MicrosoftPriceList.MicrosoftPriceListType;
import com.logicalis.serviceinsight.data.MicrosoftPriceListM365Product;
import com.logicalis.serviceinsight.data.MicrosoftPriceListProduct;
import com.logicalis.serviceinsight.data.PipelineQuoteMin;
import com.logicalis.serviceinsight.data.PricingCustomerMin;
import com.logicalis.serviceinsight.data.PricingProductMin;
import com.logicalis.serviceinsight.data.PricingSheet;
import com.logicalis.serviceinsight.data.PricingSheetProduct;
import com.logicalis.serviceinsight.data.QuoteLineItemMin;
import com.logicalis.serviceinsight.data.QuoteMin;
import com.logicalis.serviceinsight.data.SIDataWarehouseCostItem;
import com.logicalis.serviceinsight.data.ScheduledTask;
import com.logicalis.serviceinsight.data.Service;
import com.logicalis.serviceinsight.data.ServiceMin;
import com.logicalis.serviceinsight.web.RestClient;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;

@org.springframework.stereotype.Service
public class PricingIntegrationServiceImpl extends BaseServiceImpl implements PricingIntegrationService {

    SimpleDateFormat sqlDateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter pricingPipelineFormatter = DateTimeFormat.forPattern("MMddyyyy");
    @Value("${pricing.api.url}")
    private String pricingAPIUrl; // todo: add params to URL?
    @Value("${pricing.api.username}")
    private String pricingAPIUser;
    @Value("${pricing.api.password}")
    private String pricingAPIPassword;
    @Autowired
    ContractDaoService contractDaoService;
    @Autowired
    ApplicationDataDaoService applicationDataDaoService;
    @Autowired
    CostDaoService costDaoService;
    @Autowired
    PricingSheetService pricingSheetService;
    @Autowired
    MicrosoftPricingService microsoftPricingService;
    
    //CONSTANTS
    private static final Long OSP_SERVICE_ID_UNDEFINED = 90001L;
    private static final Long OSP_SERVICE_ID_NON_PORTFOLIO = 90002L;
    private static final Long OSP_SERVICE_ID_CSP_O365 = 90003L;
    private static final String PRICING_PRODUCT_TYPE_OSP = "osp";
    private static final String PRICING_PRODUCT_TYPE_NONOSP = "nonOsp";
    private static final String PRICING_PRODUCT_TYPE_SERVER = "server";
    private static final String PRICING_PRODUCT_TYPE_SSDM = "ssdm";
    private static final String PRICING_PRODUCT_TYPE_O365 = "O365";
    private static final String PRICING_PRODUCT_TYPE_M365 = "M365";
    private static final String PRICING_PRODUCT_TYPE_CAB = "CAB";
    private static final String PRICING_PRODUCT_TYPE_AZURE = "cspazure";
    private static final String PRICING_PRODUCT_TYPE_AWS = "aws";
    private static final String PRICING_PRODUCT_AWS_NEW_CUST = "netnew_cust";
    private static final String PRICING_PRODUCT_AWS_EXSTNG_CUST = "exstng_cust";
    private static final String PRICING_PRODUCT_IMPORT_SPLIT_RULE_CODE = "imprt_splt";
    private static final String PRICING_QUOTE_API_PATH = "/quoteapi";
    private static final String PRICING_PRODUCT_API_PATH = "/productapi";
    private static final String PRICING_QUOTES_PATH = "/quotes/";
    private static final String ONE_CUSTOMER_SYNC_TSK = "one_customer_sync";
    private static final String PRICING_DEVICE_SYNC_TSK = "pricing_device_sync";
    private static final String PRICING_DEVICE_M365_SYNC_TSK = "pricing_m365_sync";
    private static final String PRICING_DEVICE_O365_SYNC_TSK = "pricing_o365_sync";
    private static final String PRICING_DEVICE_M365NC_SYNC_TSK = "pricing_m365nc_sync";
    static final String PUBLIC_CLOUD_EXPENSE_CATEGORY_NAME = "Public Cloud";
    static final String AZURE_OFFICE_EXPENSE_CATEGORY_NAME = "Office 365";

    private String buildPricingAPIUrl(String endpoint) {
        return pricingAPIUrl + endpoint;
    }

    @Override
    public List<PipelineQuoteMin> findQuotesForPipeline(DateTime startDate, DateTime endDate) throws ServiceException {
        if (startDate==null) {
            startDate = new DateTime().withMonthOfYear(1);
        } else {
            startDate = startDate.withDayOfMonth(1).withTimeAtStartOfDay();
        }
        if (endDate==null) {
            endDate = new DateTime().monthOfYear().withMaximumValue();
        } else {
            endDate = endDate.dayOfMonth().withMaximumValue().plusHours(23).plusMinutes(59).plusSeconds(59);
        }
        ResponseEntity<List<PipelineQuoteMin>> response = RestClient.getListOfQuotes(pricingAPIUser, pricingAPIPassword,
                buildPricingAPIUrl(
                PRICING_QUOTE_API_PATH + "/list?rollup=false"
                + String.format("&sd=%s", pricingPipelineFormatter.print(startDate))
                + String.format("&ed=%s", pricingPipelineFormatter.print(endDate))));
        return response.getBody();
    }
    
    @Override
    public String getPricingQuoteUrl() {
        return (buildPricingAPIUrl(PRICING_QUOTES_PATH));
    }
    
    @Override
    public List<QuoteMin> findQuotesForCustomer(Long customerId) throws ServiceException {
        ResponseEntity<List<QuoteMin>> response = RestClient.getListOfQuotesForCustomer(pricingAPIUser, pricingAPIPassword, buildPricingAPIUrl(PRICING_QUOTE_API_PATH + "/list?cid=" + customerId));
        return response.getBody();
    }

    @Override
    public QuoteMin quote(Long quoteId) throws ServiceException {
        ResponseEntity<QuoteMin> response = RestClient.getQuoteById(pricingAPIUser, pricingAPIPassword, buildPricingAPIUrl(PRICING_QUOTE_API_PATH + "/one/" + quoteId));
        return response.getBody();
    }

    @Override
    public QuoteMin markQuoteAsWon(Long quoteId) throws ServiceException {
        ResponseEntity<QuoteMin> response = RestClient.markQuoteAsWon(pricingAPIUser, pricingAPIPassword, buildPricingAPIUrl(PRICING_QUOTE_API_PATH + "/won/" + quoteId));
        return response.getBody();
    }
    
    @Override
    public List<PricingProductMin> findPricingToolProducts() throws ServiceException {
        ResponseEntity<List<PricingProductMin>> response = RestClient.getListOfPricingProducts(pricingAPIUser, pricingAPIPassword, buildPricingAPIUrl(PRICING_PRODUCT_API_PATH + "/list"));
        return response.getBody();
    }
    
    @Override
    public List<PricingProductMin> findM365Products() throws ServiceException {
        ResponseEntity<List<PricingProductMin>> response = RestClient.getListOfM365Products(pricingAPIUser, pricingAPIPassword, buildPricingAPIUrl(PRICING_PRODUCT_API_PATH + "/m365"));
        return response.getBody();
    }
    
    @Override
    public List<PricingProductMin> findM365NCProducts() throws ServiceException {
        ResponseEntity<List<PricingProductMin>> response = RestClient.getListOfM365Products(pricingAPIUser, pricingAPIPassword, buildPricingAPIUrl(PRICING_PRODUCT_API_PATH + "/m365nc"));
        return response.getBody();
    }
    
    @Override
    public List<PricingProductMin> findO365Products() throws ServiceException {
    	String urlPath = PRICING_PRODUCT_API_PATH + "/list?type&ptype=O365&status=active";
        ResponseEntity<List<PricingProductMin>> response = RestClient.getListOfProductsByType(pricingAPIUser, pricingAPIPassword, buildPricingAPIUrl(urlPath));
        return response.getBody();
    }
    
    @Override
    @Transactional(readOnly = false, rollbackFor = ServiceException.class)
    public void deviceSync(Boolean override) throws ServiceException {
    	List<PricingProductMin> products = findPricingToolProducts();
    	
    	for(PricingProductMin product : products) {
    		try {
	    		Device device = applicationDataDaoService.findDeviceByNameAndPartNumber(product.getPartNumberCode(), product.getName());
	    		if(device == null) {
	    			//create device
	    			device = new Device(null, product.getPartNumberCode(), product.getName(), product.getId(), null, false, false, false, false, false, false, null, false, true, null, null, null, null, new Date(), authenticatedUser());
	    			
	    			//set the service offering 
	    			if(product.getServiceOfferings().size() == 1) {
	        			ServiceMin serviceOffering = product.getServiceOfferings().get(0);
	        			device.setDefaultOspId(serviceOffering.getId());
	        			log.info("Device Sync: Set Default OSP ID[" + serviceOffering.getId() + "] while adding a device.");
	        		}
	    			Long deviceId = applicationDataDaoService.saveDevice(device);
	    			log.info("Device Sync: Saved New Device with ID[" + deviceId + "]");
	    		} else {
	    			boolean changed = false;
	    			if(override == true || device.getProductId() == null) {
	    				if(!product.getId().equals(device.getProductId())) {
	    					device.setProductId(product.getId());
	    					changed = true;
	    				}
	    			}
	    			
	    			//we only set the default service for products that have one service associated
	        		if(product.getServiceOfferings().size() == 1 && (device.getDefaultOspId() == null || override == true)) {
	        			ServiceMin serviceOffering = product.getServiceOfferings().get(0);
	        			if(!serviceOffering.getId().equals(device.getDefaultOspId())) {
	        				device.setDefaultOspId(serviceOffering.getId());
	        				log.info("Device Sync: Set Default OSP ID[" + serviceOffering.getId() + "] while updating a device.");
	        				changed = true;
	        			}
	        		}
	        		
	        		if(changed) {
	        			device.setUpdated(new Date());
	        			device.setUpdatedBy(authenticatedUser());
	        			applicationDataDaoService.updateDevice(device);
	        			log.info("Device Sync: Updated Existing Device with ID[" + device.getId() + "]");
	        		}
	    		}
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    	}
    }
    
    @Override
    @Scheduled(cron = "0 0 4 * * *") //4:00am
    @Transactional(readOnly = false, rollbackFor = ServiceException.class)
    public void devicePricingSync() throws ServiceException {
    	try {
            ScheduledTask st = applicationDataDaoService.findScheduledTaskByCode(PRICING_DEVICE_SYNC_TSK);
            if (st != null && st.getEnabled()) {
                log.info("Running Task: " + st.getName());
                
                List<PricingProductMin> products = findPricingToolProducts();
                List<Device> devices = applicationDataDaoService.devices(Boolean.FALSE);
                for(Device device: devices) {
                	if(device.getPricingSyncEnabled()) {
                		PricingProductMin pricingProduct = null;
                		if(device.getProductId() != null) {
                			for(PricingProductMin product: products) {
                				if(product.getId().equals(device.getProductId())) {
                					pricingProduct = product;
                					break;
                				}
                			}
                		} else {
                			for(PricingProductMin product: products) {
                				if(product.getPartNumberCode().equals(device.getPartNumber())) {
                					pricingProduct = product;
                					device.setProductId(product.getId());                					
                					break;
                				}
                			}
                		}
                		
                		if(pricingProduct != null) {
                			log.info("Updating Device ID [" + device.getId() + "] ({ " + device.getPartNumber() + "}) with Product ID [{" + pricingProduct.getId() + "}]");
	                		device.setCatalogRecurringCost(pricingProduct.getRecurringCost());
	                		device.setCatalogRecurringPrice(pricingProduct.getRecurringPrice());
	                		applicationDataDaoService.saveDevice(device);
                		}
                	}
                }
                
                log.info("Ending Task: " + st.getName());
            } else {
            	log.info("Pricing Device Sync is not enabled.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException("An exception occurred ... rolling back");
        }
    }

    @Override
    @Async
    @Scheduled(cron = "0 5 21 * * *") //9:05pm
    @Transactional(readOnly = false)
    public void m365Sync() throws ServiceException {
    	try {
            ScheduledTask st = applicationDataDaoService.findScheduledTaskByCode(PRICING_DEVICE_M365_SYNC_TSK);
            if (st != null && st.getEnabled()) {
                log.info("Running Task: " + st.getName());
                
                List<PricingProductMin> products = findM365Products();
                List<Device> devices = applicationDataDaoService.devices(null);
                
                for(PricingProductMin product: products) {
                	addOrUpdateDevice(product, devices);
                }
                
                log.info("Ending Task: " + st.getName());
            } else {
            	log.info("M365 Device Sync is not enabled.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException("An exception occurred ... rolling back");
        }
    }
    
    @Override
    @Async
    @Scheduled(cron = "0 15 21 * * *") //9:15pm
    @Transactional(readOnly = false)
    public void m365NCSync() throws ServiceException {
    	try {
            ScheduledTask st = applicationDataDaoService.findScheduledTaskByCode(PRICING_DEVICE_M365NC_SYNC_TSK);
            if (st != null && st.getEnabled()) {
                log.info("Running Task: " + st.getName());
                
                List<PricingProductMin> products = findM365NCProducts();
                List<Device> devices = applicationDataDaoService.devices(null);
                
                for(PricingProductMin product: products) {
                	addOrUpdateDevice(product, devices);
                }
                
                log.info("Ending Task: " + st.getName());
            } else {
            	log.info("M365NC Device Sync is not enabled.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException("An exception occurred ... rolling back");
        }
    }
    
    @Override
    @Async
    @Scheduled(cron = "0 25 22 * * *") //10:25pm
    @Transactional(readOnly = false)
    public void o365Sync() throws ServiceException {
    	try {
            ScheduledTask st = applicationDataDaoService.findScheduledTaskByCode(PRICING_DEVICE_O365_SYNC_TSK);
            if (st != null && st.getEnabled()) {
                log.info("Running Task: " + st.getName());
                
                List<PricingProductMin> products = findO365Products();
                List<Device> devices = applicationDataDaoService.devices(null);
                
                for(PricingProductMin product: products) {
                	addOrUpdateDevice(product, devices);
                }
                
                log.info("Ending Task: " + st.getName());
            } else {
            	log.info("M365 Device Sync is not enabled.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException("An exception occurred ... rolling back");
        }
    }
    
    private void addOrUpdateDevice(PricingProductMin product, List<Device> devices) {
    	try {
        	//log.info("Checking Product: " + product.getName());
        	Device existingDevice = null;
        	Boolean deviceChanged = false;
        	for(Device device: devices) {
        		DeviceType deviceType = null;
        		if(PRICING_PRODUCT_TYPE_O365.equals(product.getDeviceType())) {
        			deviceType = Device.DeviceType.cspO365;
        		} else {
        			deviceType = Device.DeviceType.valueOf(product.getDeviceType());
        		}
        		
        		if(product.getId().equals(device.getProductId())) {
        			log.info("Found Product by Product ID");
        			if(!product.getName().equals(device.getDescription()) || !product.getRecurringCost().equals(device.getCatalogRecurringCost()) || !product.getRecurringPrice().equals(device.getCatalogRecurringPrice())
        					|| !product.getAltId().equals(device.getAltId()) || !device.getDeviceType().equals(deviceType)) {
	        			device.setDescription(product.getName());
	        			device.setCatalogRecurringCost(product.getRecurringCost());
	        			device.setCatalogRecurringPrice(product.getRecurringPrice());
	        			device.setAltId(product.getAltId());
	        			device.setActivateSyncEnabled(Boolean.TRUE);
	            		device.setActivateAddBusinessService(Boolean.TRUE);
	            		if(!Device.DeviceType.M365Setup.equals(deviceType) && !Device.DeviceType.M365Support.equals(deviceType)) {
	            			device.setPricingSheetEnabled(Boolean.TRUE);
	            		}
	            		device.setDeviceType(deviceType);
	            		
	            		Device.TermDuration termDuration = null;
	            		Device.BillingPlan billingPlan = null;
	            		Device.Segment segment = null;
	            		
	            		if(product.getTermDuration() != null) termDuration = Device.TermDuration.valueOf(product.getTermDuration());
	            		if(product.getBillingPlan() != null) billingPlan = Device.BillingPlan.valueOf(product.getBillingPlan());
	            		if(product.getSegment() != null) segment = Device.Segment.valueOf(product.getSegment());
	            		
	            		device.setTermDuration(termDuration);
	            		device.setBillingPlan(billingPlan);
	            		device.setSegment(segment);
	            		
	            		deviceChanged = true;
        			}
            		existingDevice = device;
					break;
				} else if(product.getPartNumberCode().equals(device.getPartNumber())) {
					log.info("Found Product by Part Number");
					if(!product.getName().equals(device.getDescription()) || !product.getRecurringCost().equals(device.getCatalogRecurringCost()) || !product.getRecurringPrice().equals(device.getCatalogRecurringPrice())
        					|| !product.getAltId().equals(device.getAltId()) || !product.getId().equals(device.getProductId()) || !device.getDeviceType().equals(deviceType)) {
						device.setDescription(product.getName());
						device.setProductId(product.getId());
						device.setCatalogRecurringCost(product.getRecurringCost());
	        			device.setCatalogRecurringPrice(product.getRecurringPrice());
						device.setAltId(product.getAltId());
						device.setActivateSyncEnabled(Boolean.TRUE);
	            		device.setActivateAddBusinessService(Boolean.TRUE);
	            		if(!Device.DeviceType.M365Setup.equals(deviceType) && !Device.DeviceType.M365Support.equals(deviceType)) {
	            			device.setPricingSheetEnabled(Boolean.TRUE);
	            		}
	            		device.setDeviceType(deviceType);
	            		
	            		Device.TermDuration termDuration = null;
	            		Device.BillingPlan billingPlan = null;
	            		Device.Segment segment = null;
	            		
	            		if(product.getTermDuration() != null) termDuration = Device.TermDuration.valueOf(product.getTermDuration());
	            		if(product.getBillingPlan() != null) billingPlan = Device.BillingPlan.valueOf(product.getBillingPlan());
	            		if(product.getSegment() != null) segment = Device.Segment.valueOf(product.getSegment());
	            		
	            		device.setTermDuration(termDuration);
	            		device.setBillingPlan(billingPlan);
	            		device.setSegment(segment);
	            		
	            		deviceChanged = true;
					}
					existingDevice = device;
					break;
				}
        	}
        	
        	if(existingDevice == null) {
        		log.info("Device doesn't exist. Creating new one.");
        		deviceChanged = true;
        		Device newDevice = new Device();
        		newDevice.setDescription(product.getName());
        		newDevice.setProductId(product.getId());
        		newDevice.setPartNumber(product.getPartNumberCode());
        		newDevice.setDefaultOspId(OSP_SERVICE_ID_CSP_O365);
        		newDevice.setCatalogRecurringCost(product.getRecurringCost());
        		newDevice.setCatalogRecurringPrice(product.getRecurringPrice());
        		newDevice.setRequireUnitCount(Boolean.TRUE);
        		newDevice.setActivateSyncEnabled(Boolean.TRUE);
        		newDevice.setActivateAddBusinessService(Boolean.TRUE);
        		newDevice.setPricingSheetEnabled(Boolean.TRUE);
        		log.info("Device Type: " + product.getDeviceType());
        		DeviceType deviceType = null;
        		if("O365".equals(product.getDeviceType())) {
        			deviceType = Device.DeviceType.cspO365;
        		} else {
        			deviceType = Device.DeviceType.valueOf(product.getDeviceType());
        		}
        		newDevice.setDeviceType(deviceType);
        		newDevice.setAltId(product.getAltId());
        		
        		Device.TermDuration termDuration = null;
        		Device.BillingPlan billingPlan = null;
        		Device.Segment segment = null;
        		
        		if(product.getTermDuration() != null) termDuration = Device.TermDuration.valueOf(product.getTermDuration());
        		if(product.getBillingPlan() != null) billingPlan = Device.BillingPlan.valueOf(product.getBillingPlan());
        		if(product.getSegment() != null) segment = Device.Segment.valueOf(product.getSegment());
        		
        		newDevice.setTermDuration(termDuration);
        		newDevice.setBillingPlan(billingPlan);
        		
        		//add mapping for M365
        		List<DeviceExpenseCategory> costMappings = new ArrayList<DeviceExpenseCategory>();
        		ExpenseCategory expenseCategory = costDaoService.expenseCategoryByName(AZURE_OFFICE_EXPENSE_CATEGORY_NAME, PUBLIC_CLOUD_EXPENSE_CATEGORY_NAME);
        		DeviceExpenseCategory category = new DeviceExpenseCategory();
        		category.setExpenseCategoryId(expenseCategory.getId());
        		costMappings.add(category);
        		newDevice.setCostMappings(costMappings);
        		existingDevice = newDevice;
        	}
        	if(deviceChanged) applicationDataDaoService.saveDevice(existingDevice);
    	} catch (Exception e) {
    		log.error(e.getMessage());
    		e.printStackTrace();
    	}
    }
    
    @Override
    @Transactional(readOnly = false, rollbackFor = ServiceException.class)
    public void importQuote(Long quoteId, Contract contract) throws ServiceException {
        Long contractId = contractDaoService.saveContract(contract);
        //validate contract?
        contract.setId(contractId);

        QuoteMin quote = quote(quoteId);

        if (quote == null) {
            throw new ServiceException(messageSource.getMessage("validation_error_import_quote_not_found", new Object[]{quoteId}, LocaleContextHolder.getLocale()));
        }

        if (quote.getLineItems() != null && quote.getLineItems().size() > 0) {
            for (QuoteLineItemMin lineItem : quote.getLineItems()) {
            	if(lineItem.getParentId() == null) {
            		Long parentId = saveQuoteLineItemAsContractService(lineItem, contract, null);
            		
            		List<QuoteLineItemMin> relatedQuoteLineItems = lineItem.getRelatedQuoteLineItems();
            		if(relatedQuoteLineItems != null && relatedQuoteLineItems.size() > 0) {
            			for (QuoteLineItemMin relatedLineItem : relatedQuoteLineItems) {
            				Long childId = saveQuoteLineItemAsContractService(relatedLineItem, contract, parentId);
            				
            				List<QuoteLineItemMin> grandChildQuoteLineItems = relatedLineItem.getRelatedQuoteLineItems();
                    		if(grandChildQuoteLineItems != null && grandChildQuoteLineItems.size() > 0) {
                    			for (QuoteLineItemMin grandChildQuoteLineItem : grandChildQuoteLineItems) {
                    				Long grandChildId = saveQuoteLineItemAsContractService(grandChildQuoteLineItem, contract, childId);
                    				
                    				List<QuoteLineItemMin> greatGrandChildQuoteLineItems = grandChildQuoteLineItem.getRelatedQuoteLineItems();
                            		if(greatGrandChildQuoteLineItems != null && greatGrandChildQuoteLineItems.size() > 0) {
                            			for (QuoteLineItemMin greatGrandChildQuoteLineItem : greatGrandChildQuoteLineItems) {
                            				Long greatGrandChildId = saveQuoteLineItemAsContractService(greatGrandChildQuoteLineItem, contract, grandChildId);
                            			}
                            		}
                    				
                    			}
                    		}
            			}
            		}
            	}
            }
        } else {
            throw new ServiceException(messageSource.getMessage("validation_error_import_quote_no_lineitems", null, LocaleContextHolder.getLocale()));
        }
    }
    
    private Long saveQuoteLineItemAsContractService(QuoteLineItemMin lineItem, Contract contract, Long parentId) throws ServiceException {
    	Long contractServiceId = null;
    	
    	//get service offering
        Long serviceOfferingId = lineItem.getServiceOfferingId();
        if (serviceOfferingId == null) {
            serviceOfferingId = OSP_SERVICE_ID_UNDEFINED;
        }

        String productName = lineItem.getName();
        String productType = lineItem.getProductType();
        String partNumberCode = lineItem.getPartNumberCode();
        String ruleCode = lineItem.getRuleCode();
        
        if(PRICING_PRODUCT_TYPE_SERVER.equals(productType) || PRICING_PRODUCT_IMPORT_SPLIT_RULE_CODE.equals(ruleCode)) {
			parentId = null;
		}
        
        //if the product is CAB or nonOsp, we'll use the Non-Portfolio Products Service
        if (PRICING_PRODUCT_TYPE_NONOSP.equals(productType) || PRICING_PRODUCT_TYPE_CAB.equals(productType) || PRICING_PRODUCT_TYPE_SSDM.equals(productType) || PRICING_PRODUCT_TYPE_AZURE.equals(productType) || PRICING_PRODUCT_TYPE_AWS.equals(productType) || PRICING_PRODUCT_TYPE_O365.equals(productType) || PRICING_PRODUCT_TYPE_M365.equals(productType)) {
            serviceOfferingId = OSP_SERVICE_ID_NON_PORTFOLIO;

            //As of now, we are giving all CAB Items the same name to prevent Service Insight from creating fake products.
            if (PRICING_PRODUCT_TYPE_CAB.equals(productType)) {
                productName = "CAB Item";
                if(partNumberCode != null && partNumberCode.contains("PV-CAB")) {
                	partNumberCode = "PV-CAB";
                } else {
                	partNumberCode = "MS-CAB";
                }
            }
        }
        
        if(PRICING_PRODUCT_TYPE_O365.equals(productType) || PRICING_PRODUCT_TYPE_M365.equals(productType)) {
        	serviceOfferingId = OSP_SERVICE_ID_CSP_O365;
        }

        Service serviceOffering = applicationDataDaoService.findActiveServiceByOspId(serviceOfferingId);
        if(serviceOffering == null) {
        	//we'll set it to undefined if it can't find the service
        	serviceOffering = applicationDataDaoService.findActiveServiceByOspId(OSP_SERVICE_ID_UNDEFINED);
        }

        //get or create device
        Device device = null;
        //check by product id 
        device = applicationDataDaoService.findDeviceByProductId(lineItem.getProductId());
        
        //otherwise check by part number and name
        if(device == null) {
        	device = applicationDataDaoService.findDeviceByNameAndPartNumber(partNumberCode, productName);
        }
        if(PRICING_PRODUCT_TYPE_AWS.equals(productType)) {
        	//because there are multiple AWS products in the pricing tool, we need to find the single SI device these map to, so we'll get it by device type
                List<Device> devices = applicationDataDaoService.findDeviceByDeviceType(Device.DeviceType.aws);
                if (devices != null && devices.size() == 1) {
                    device = devices.get(0);
                } else {
                    log.warn("Found more than one Device with DeviceType {}", Device.DeviceType.aws);
                }
        }
        
        if (device == null) {
            device = new Device();
            device.setPartNumber(partNumberCode);
            device.setDescription(productName);
            device.setProductId(lineItem.getProductId());
            device.setRequireUnitCount(lineItem.getImportAsUnits());
            device.setCreated(new Date());
            Long deviceId = applicationDataDaoService.saveDevice(device);
            device.setId(deviceId);
        }

        
        if(PRICING_PRODUCT_TYPE_AZURE.equals(productType) || PRICING_PRODUCT_TYPE_AWS.equals(productType)) {
        	saveContractServiceSubscriptionFromImport(lineItem, contract, serviceOffering.getServiceId(), device.getId(), productType, ruleCode);
        } else if(PRICING_PRODUCT_TYPE_O365.equals(productType) || PRICING_PRODUCT_TYPE_M365.equals(productType)) {
        	savePricingSheetProductFromImport(lineItem, contract, serviceOffering.getServiceId(), device.getId(), productType);
        } else {
        	contractServiceId = saveContractServiceFromImport(lineItem, contract, serviceOffering.getServiceId(), device.getId(), productType, parentId);
        }
        
        return contractServiceId;
    }

    private void saveContractServiceSubscriptionFromImport(QuoteLineItemMin lineItem, Contract contract, Long serviceId, Long deviceId, String productType, String ruleCode) throws ServiceException {

        for (int i = 0; i < lineItem.getQuantity(); i++) {
        	ContractServiceSubscription subscription = new ContractServiceSubscription();
        	subscription.setContractId(contract.getId());
        	subscription.setStartDate(contract.getStartDate());
        	subscription.setEndDate(contract.getEndDate());
        	subscription.setServiceId(serviceId);
        	subscription.setDeviceId(deviceId);
        	
        	if(PRICING_PRODUCT_TYPE_AZURE.equals(productType)) {
        		subscription.setSubscriptionType(ContractServiceSubscription.SubscriptionType.cspazure);
        	} else if(PRICING_PRODUCT_TYPE_AWS.equals(productType)) {
        		subscription.setSubscriptionType(ContractServiceSubscription.SubscriptionType.aws);
        		//need to set customer type
        		if(PRICING_PRODUCT_AWS_NEW_CUST.equals(ruleCode)) {
        			subscription.setCustomerType(ContractServiceSubscription.CustomerType.netnew);
        		} else {
        			subscription.setCustomerType(ContractServiceSubscription.CustomerType.existing);
        		}
        	}

            contractDaoService.saveContractServiceSubscription(subscription, false);
        }
    }
    
    private void savePricingSheetProductFromImport(QuoteLineItemMin lineItem, Contract contract, Long serviceId, Long deviceId, String productType) throws ServiceException {
        PricingSheetProduct pricingSheetProduct = new PricingSheetProduct();
        
        PricingSheet sheet = pricingSheetService.findPricingSheetForContractWithActiveProducts(contract.getId());
        for(PricingSheetProduct existingProduct : sheet.getProducts()) {
        	if(existingProduct.getDeviceId().equals(deviceId)) {
        		pricingSheetProduct = existingProduct;
        		break;
        	}
        }
        
        BigDecimal recurringPrice = lineItem.getNetRecurringPrice();
        String altId = lineItem.getAltId();
        
        pricingSheetProduct.setPricingSheetId(sheet.getId());
        pricingSheetProduct.setDeviceId(deviceId);
        pricingSheetProduct.setServiceId(serviceId);
        pricingSheetProduct.setDeviceAltId(altId);
        pricingSheetProduct.setRemovalPrice(new BigDecimal(0));
        pricingSheetProduct.setStatus(PricingSheetProduct.Status.active);
        pricingSheetProduct.setUnitCount(lineItem.getQuantity());
        pricingSheetProduct.setOnetimePrice(lineItem.getNetOnetimePrice());
        pricingSheetProduct.setRecurringPrice(recurringPrice);
        
        MicrosoftPriceList priceList = microsoftPricingService.getLatestMicrosoftPriceList(MicrosoftPriceListType.M365);
        if(priceList == null) {
        	//throw exception
        }
        
        MicrosoftPriceListM365Product priceListProduct = microsoftPricingService.getMicrosoftPriceListProductByOfferId(priceList.getId(), altId);
        if(priceListProduct == null) {
        	//throw exception
        }
        
        pricingSheetProduct.setErpPrice(priceListProduct.getErpPrice());
        
        BigDecimal discount = recurringPrice.divide(priceListProduct.getErpPrice(), 2, RoundingMode.HALF_UP).multiply(new BigDecimal(100));
		discount = new BigDecimal(100).subtract(discount);
		pricingSheetProduct.setDiscount(discount);
		
		if(pricingSheetProduct.getId() == null) {
			pricingSheetService.savePricingSheetProduct(pricingSheetProduct);
		} else {
			pricingSheetService.updatePricingSheetProduct(pricingSheetProduct);
		}
    }
    
    private Long saveContractServiceFromImport(QuoteLineItemMin lineItem, Contract contract, Long serviceId, Long deviceId, String productType, Long parentId) throws ServiceException {
        Integer units = 0;
        if (lineItem.getImportAsUnits()) {
            units = lineItem.getQuantity();
            lineItem.setQuantity(1);
        }
        
        if(lineItem.getQuantity() > 200) {
        	throw new ServiceException(messageSource.getMessage("validation_error_import_quote_quantity_max", null, LocaleContextHolder.getLocale()));
        }
        
        Long contractServiceId = null;
        
        for (int i = 0; i < lineItem.getQuantity(); i++) {
            Service contractService = new Service();
            contractService.setContractId(contract.getId());
            contractService.setStartDate(contract.getStartDate());
            contractService.setEndDate(contract.getEndDate());
            contractService.setServiceId(serviceId);
            contractService.setDeviceId(deviceId);
            contractService.setQuoteLineItemId(lineItem.getId());
            if(parentId != null) {
            	contractService.setParentId(parentId);
            }
            BigDecimal onetimePrice = lineItem.getNetOnetimePrice();
            BigDecimal recurringPrice = lineItem.getNetRecurringPrice();

            //if the lineitem is something like Storage or Memory, we don't want to create 1000s of records, so we'll stick the quantity in the units field and calculate the total price for them
            if (lineItem.getImportAsUnits()) {
                contractService.setDeviceUnitCount(units);
                //update pricing
                onetimePrice = onetimePrice.multiply(new BigDecimal(units));
                recurringPrice = recurringPrice.multiply(new BigDecimal(units));
            }
            contractService.setOnetimeRevenue(onetimePrice);
            contractService.setRecurringRevenue(recurringPrice);

            //since we are changing the CAB item name to just "CAB Item", we will stick the name and description from the pricing tool into the notes field.
            if (PRICING_PRODUCT_TYPE_CAB.equals(productType)) {
                String note = lineItem.getName();
                String description = lineItem.getDescription();
                if (description != null && !"".equals(description)) {
                    note = note + " (" + description + ")";
                }
                contractService.setNote(note);
            }

            contractServiceId = contractDaoService.saveContractService(contractService, false);
        }
        
        return contractServiceId;
    }

    @Scheduled(cron = "0 0 6,18 * * *") //6:00am & 6:00pm
//    @Scheduled(initialDelay = 1000 * 60, fixedRate = 1000 * 60 * 60)
    public void syncRemoteCustomers() {
    	try {
	    	ScheduledTask st = applicationDataDaoService.findScheduledTaskByCode(ONE_CUSTOMER_SYNC_TSK);
	        if (st != null && st.getEnabled()) {
	        	log.info("Running Task: " + st.getName());
		        log.info("There can be only one... Customer");
		        System.out.println("         `                `                                       `      ");
		        System.out.println("                           ```  `` ````                    `             ");
		        System.out.println("                  `     `  `` ``,````` ```                               ");
		        System.out.println("                  ` `   ````` ``.`..``   ````                            ");
		        System.out.println("       `        `        `` ```.....,::,,....`                           ");
		        System.out.println("               ```       ```..``..`..,.`.......`                         ");
		        System.out.println("              ``   `   ```````....`..,   `  ``.,                         ");
		        System.out.println("                   `  ````````.`.``..,,.` ````.`.                     `  ");
		        System.out.println("                    ` ` ````.....,:,:,,,,....`..`.                       ");
		        System.out.println("                     `.,.,,,.,,,,.,,:,,.,:..,,..`,`                 `    ");
		        System.out.println("                     ..,:,,:,:,::,,..;;,;;,,.,,`,..                      ");
		        System.out.println("                   `.,...,,::;;:;::,..;'':,:,,,.....                     ");
		        System.out.println("                  `.,::;:..:;;;:;::';,,:;,:;,:,...,,,`                   ");
		        System.out.println("               `...,,:;';;,.:';;:++'+;:,::;:::,;,.,.,,,.                 ");
		        System.out.println("             `,::,;;;;''';;::::;'++'';,;:,;:;:';;::,,`.,,                ");
		        System.out.println("            `':,:;#+''''#++'''';;+++'';;:::,,:,:;:,:,.``.:`              ");
		        System.out.println(".```       `,::;;+'''''++#++'+#+'';++'+;::,,:,,::;,,:,,`...`            `");
		        System.out.println(":::::,`    ,:;;;#''''''+##+++++'+++;;++''';:,,..,,:;,:::`.``.            ");
		        System.out.println("::::::,`  .:'''++'''++++##+++#''++'+';'++'++'';,...;:::,,`.``.           ");
		        System.out.println(":::::,..``;+'''+'''''++++##+#'+#++++'+':'+'++++++;,,:,:::...`.`          ");
		        System.out.println(";::,.,,..,'';:;'''+'+'++++++++++++++'''';;++++'+##+,::,:';... .`         ");
		        System.out.println(":::,:,.`.:+'::;'++++++'##+++'+++'+++'+'+':,;''+++++';,;:,',.....    ` `  ");
		        System.out.println("::,.,.`.:;;;::,;''++'++++++++++'++''''''::,.,:;'''+++;:;:,,..,,. `       ");
		        System.out.println(":::,:.,:;':,,,'''++#+++'++'+++''++';;;::..`..,:;'+'''';:;;::.::.         ");
		        System.out.println(":,,,.`::;::,:+#+###+#+++++++''';';;::,,.`````..,:'''++';:':::';          ");
		        System.out.println(".,,.`.::;::;+++####+++++++++;';::,...`````````..,'''+'';;;';;''.         ");
		        System.out.println(",....:,::;'+#++##++++++++'''::,,,.....`````````..,'+++''''';'';:         ");
		        System.out.println(":.,,,:,:+++++++#++++++++'';:.,,......```````````..:;++++++++++';`        ");
		        System.out.println(",,,...'+++++'+++++''''++'::,..,.....````...``````.:''+++++++++++:        ");
		        System.out.println(",,...'++'+++''++'';';'';;:,..,.,.....``````.```.`.,;''++++++++++;       `");
		        System.out.println(":..,';+++##+'';;:::::;;::,...,.........```........,:'++#+++++''++        ");
		        System.out.println(",.;+;#+'+#@':;:,,,,,,,,,.,.,..........````........,;'+'+++++++''+`    `` ");
		        System.out.println(".;+;#+'++##:::,,,:,,.,.................```....`...,;''+++++++++++:       ");
		        System.out.println(":+'+::+++#+:,,,,,,,,,,,..,..............```........:''+++++''++''+` `    ");
		        System.out.println("'+;,,;+++##::,,.,,,,,,,,,,,.,......,......`....`..,:''+'++++'''+'+; `` ` ");
		        System.out.println("+,,::;'++#+;:,,,,,,,,:,,,,,,,,,,,,,,,,............,:';+'++++++'';;',`.   ");
		        System.out.println(":,,,::'+'#+;:,:,,,,,,,,,,,,,,,,,,:,:,,,.,,,,.,....,,''+'++++++++':::.,   ");
		        System.out.println(",,,.:''++##';:::,:,,,,,,:,,,,,,,,,:::,,,,,,,.,....,;''+++++++#+++'::.,   ");
		        System.out.println(",,,,:'+'##+';::::,:,,,,:::,::,,,::::::::,,.,,.....,;''++'+#+++###'';::   ");
		        System.out.println(",::,;'+++#+';;:::;:,,::,:,:,::::::::::,:,,..,,....,;'++'+++++#+###++',   ");
		        System.out.println(",:::;;##+@#+';;::::::::,:,:,::::::,:::,,,,,,,,.....,;++'#'+##+##+#++'.   ");
		        System.out.println(":::;;;+#+##+'';;;;;;::::::,::::,:,,,,,,,,,,,,.......,'+'#++++++##+#+.`   ");
		        System.out.println(",::::'##+##++'''';;;:;::,,::::,,,,,,,,,,.,.,,.....``.:+'+###+'++#+++,    ");
		        System.out.println(",,::''#++##+'''''';;;;;:,,:,:::,,,,,,,,.,,,,,........,'''#####+'';;;     ");
		        System.out.println(",,;;''#'+###'''''';;;:;:,,:,,:,,,,,,,,,..............,;+++######+'':     ");
		        System.out.println(",:':;++'+###+''''';:::::,,,,::,,...,,,,,.,......`.....,'++#####+##+;``  `");
		        System.out.println(":';;'++++###+';';;:::,::,,,,,,,,...,,,,....``.`.`......:+##+#######+:`   ");
		        System.out.println(",':;'+''+###+';;;;;::,,,,,..,,,,,..,.........`.........:'##+##++###+,    ");
		        System.out.println(":';;;'''+####+;;::;::,,,,,.,,,,,,......,,..,,,,::,,,..`:;+#+##+++##+`    ");
		        System.out.println(";'::'''+'#@##+';;:;;:,:,,,,,,,,:::,,,,,::;;''';;;::,..`;,'#+#++++++'     ");
		        System.out.println(";;:;';'+'#@#@+';;::::,,:,:,,::::;;;;;''''';''';;;;:,..:.,'#+#+++++':     ");
		        System.out.println("':;'''++'#@#@+'';;:,:,:::::::;;;;''++++'++##+##';::,,,,`.'##+#'+++':.... ");
		        System.out.println("+:'''+++;###@#';;:,:,,::;;;;:::;'''+++'+#+#+'';;:,,,:,.`.+##+#''++''.:::,");
		        System.out.println("+;';'+++'###@#';::::::;;''';:,,:;'++++';'++;;:,,,,,,,..``+##+'';++':.::::");
		        System.out.println("''+'''+'+####@++''''''+++++;,..,:+++#++''';::,,,,.,,,..``+++'';;+'';.::::");
		        System.out.println(",'+'''+'+####@#+'''++#@##+#',...,'+'''';;::::,,,...,.....;+':':''++;,,:::");
		        System.out.println(".''+;'''+#+##@++++#+'#+###@+....,;''';';;:;:,,,..,.,.....:':;;'++++',,:::");
		        System.out.println(",;'';''++##@#@#'++#'::++++#+,...,;:;::::::::,,,,,,,,,,...,;;;:;'+#++;::::");
		        System.out.println(":'''+'+###@@#@@''+++'''''''+,`..,:::,,,:,::,,.,,,,,,,,,..,;;:;;++#+#'::::");
		        System.out.println(";;;;'+##+#@##@@+'''+''';;;;':.``.,,,.,,,,,,,,,,,,,,,,,,,,,;:;''#++###:,::");
		        System.out.println("';:'+++++##@#@@+''''''';;;;':.``.........,,,,,,,,,,,,,:,,:::''##+++@@:,::");
		        System.out.println(";:''+'+++##@@#@#'';;;;;;;;;;:,```..........,,,,,,,,,,,,.,;:,;;@#'+#@@::::");
		        System.out.println(":'''++''+##@##@@';;;;;;;;;;;:,.```.........,,,,,:,,,,,,,,::,;;@+'+##+::::");
		        System.out.println(";:''+''+++#@@@@@+';;;;;:;;;;:..`````..,,....,,,,,,,,,,,,::,,:;@+''+#::::;");
		        System.out.println(":;'''''++##@@@@@@';;;;;::::;,.`````...::,,,,,,,,,,,::,,:::,.::@+':+::::::");
		        System.out.println(":''''+;++###@@@@@+';;;::::;;:..```````,:,,,,.,..,,,,:,,:::,..:#+':#;.,:::");
		        System.out.println(":;'+'+';'###@@@@@#'';;::::;;:,.````````;:::,,..,,.,,:,,:,:...,+';;@+:,:::");
		        System.out.println(";''''+'''+###@#@@@+'';;::::::,.````````,:::,,,.,..,:::::;,,...''+'+':;.,:");
		        System.out.println(";;;;;'''++###@#@@@#''';:::::,,,...,,,..,,,:,,,,,...,::::;,,,`.'''++''''+,");
		        System.out.println(";;:'''''++##@@#@@@#+'';::::;;:,,.,,;+';:..,,:,,,,,,,::;::,,..,'''##+'::::");
		        System.out.println(";:;'+'++++##@##@@@##'';;:::;';:::::+++;,....,:::,,:::':::,,..:;;'@#+':,::");
		        System.out.println("':;'+++++++#@##@@###+'';::::'+''';'';;,.,,,,,:::,,,,+;,,:,:..;;;#@#';,:';");
		        System.out.println("';'++'+++++####@@####'';;;::;+++++';::,,,,,,,,,,::,'':,,,:,,,';:#@#''+##'");
		        System.out.println("'''++;+#+++####@##+##+';;:::;;''''';:,,,,,,:,::,,,:':,::,::::+':###+++#++");
		        System.out.println("'''#';++#+++#@#@##+###';;::::;;;;;';,,,,,::::;::,:':::::::::;+;:#++###+';");
		        System.out.println(";;'+'''+#+++#@@###+##@+';;;::;;:::::,::,::;'';::,:;:,::::;::;';:++++''++'");
		        System.out.println(";;++';'+++++#@####+##@#';;;;;;::;;:;;;''+##+'';::;:,::::;:;;;'::'',,';+'+");
		        System.out.println(";'+';;++'+++######+##@@+';;;;;'';'';;'+#'';::;';':,,:::;;;';'',;'';''';''");
		        System.out.println(";;'';'+''+++##@##++#@#@#'';;++++++++#+;,,::,::;';;::;::;;'+++'''+++##+;''");
		        System.out.println(";'';''''+++######+###+@@';;''''''';;:,.,,:,,:;:;;;:;::;;'+#;;:'+'####+':;");
		        System.out.println("'+'';''++++#########'#@##;;;;;:::,,,,,,:,:::;;;;;;;;::;'''+;;'''';##++'::");
		        System.out.println("+;:;''+'++####@####++####+;;;:;::::,:::;;;;';;;;':;;;;'+++'''+'++'##+++';");
		        System.out.println(";;:;'++++#####@####+#+#++#'';;;''';;'+++';';;;;:;::;;:+###+#+'++++#++''''");
		        System.out.println(";';;;++'+####@@#####+#'++##+''''++++++'';;;::::::::;''+#####+###+++++++;:");
		        System.out.println(":,:::+''######@##@@#+;'#####+''''''';;::;;::,:::;;;''##+++#+###+++#+':..:");
		        System.out.println(":,:::+'++#####@##@@#;,+#####+'';;::::;;;:,,,::::;''++##+++#+##++''++;,,,,");
		        System.out.println(",,:::+++########@@@#+########+';:::;;:::::::::;''+++##++''+###+';'':,,:::");
		        System.out.println(",:::;+++########@@@###########;;;;;:::::::::;;'++++###++++'''+'';:,;::,;;");
		        System.out.println(",::;;'+++######@@@###########@+'';;;;;;:;;'''+++######++++'+''::.,:;,,;:'");
		        System.out.println(":::;;'+++######@##############@+''''';'''+++###++#####++++';':,:.::',;':'");
		        System.out.println(":,:;''++++##@###################''+++'++######@#+#####+++;;';:::,:;'::;+;");
		        System.out.println(",,::'+'+++#@##################+++'+++++###@@@@@#####++++''';,:;:,:;';;;+;");
		        System.out.println(":,,:'''++##@@@###############++++''+##'##@@@@@+++++++#';'+';:;'::;:++'++'");
		        System.out.println("::.;';'+#@#@@@@#######+#####+++'+++'''+##@@@#+';:;;';;;'';:;:;'',:;+++++'");
		
		        String updateQuery = "update customer set alt_id = ?, sn_sys_id = ?, name = ?, description = ?,"
		                + " phone = ?, street_1 = ?, street_2 = ?, city = ?, state = ?, zip = ?, country = ?,"
		                + " updated = ?, updated_by = ? ";
		        ResponseEntity<List<PricingCustomerMin>> response = RestClient.getListOfPricingCustomers(
		                pricingAPIUser, pricingAPIPassword, buildPricingAPIUrl(PRICING_QUOTE_API_PATH + "/customers.json"));
		        List<PricingCustomerMin> pcmResults = response.getBody();
		        for (PricingCustomerMin pcm : pcmResults) {
		            try {
		                Long id = jdbcTemplate.queryForObject("select id from customer where alt_id = ?", Long.class, pcm.getId());
		                try {
		                    int updated = jdbcTemplate.update(updateQuery + "where id = ?", new Object[]{pcm.getId(),
		                        pcm.getAltId(), pcm.getName(), pcm.getDescription(), pcm.getPhone(), pcm.getStreet1(),
		                        pcm.getStreet2(), pcm.getCity(), pcm.getState(), pcm.getZip(), pcm.getCountry(),
		                        new DateTime().toDate(), "onecustomer", id});
		//                    log.debug("existing customer updated for remote PK Id with data: {}", pcm.toString());
		                } catch (Exception any) {
		                    log.warn("failed to update ServiceInsight customer for remote id [{}], message: [{}]",
		                            new Object[]{pcm.getId(), any.getMessage()});
		                }
		                continue;
		            } catch(DataAccessException dae) {
		                // ignoreable: we proceed to finding other ways
		            }
		            /**
		             * look for the Customer by SN SysId
		             */
		            if (StringUtils.isNotBlank(pcm.getAltId())) {
		                Integer count = jdbcTemplate.queryForObject("select count(*) from customer where sn_sys_id = ?", Integer.class, pcm.getAltId());
		                if (count > 1) { // very unlikely...
		                    log.warn("PROBLEM: more than one Customer was found with the same SN sys id for remote customer: ", pcm.toString());
		                    continue;
		                } else if (count == 1) {
		                    try {
		                        int updated = jdbcTemplate.update(updateQuery + "where sn_sys_id = ?", new Object[]{pcm.getId(),
		                            pcm.getAltId(), pcm.getName(), pcm.getDescription(), pcm.getPhone(), pcm.getStreet1(),
		                            pcm.getStreet2(), pcm.getCity(), pcm.getState(), pcm.getZip(), pcm.getCountry(),
		                            new DateTime().toDate(), "onecustomer", pcm.getAltId()});
		//                        log.debug("existing customer updated for remote SN sysId with data: {}", pcm.toString());
		                    } catch (Exception any) {
		                        log.warn("failed to update ServiceInsight customer for remote Sys ID [{}], message: [{}]",
		                                new Object[]{pcm.getAltId(), any.getMessage()});
		                    }
		                    continue;
		                }
		            }
		            /**
		             * look for the Customer by Name BUT only on the
		             * condition that this is a OSM customer with a SysId
		             */
		            if (StringUtils.isNotBlank(pcm.getAltId()) && StringUtils.isNotBlank(pcm.getName())) {
		                Integer count = jdbcTemplate.queryForObject("select count(*) from customer where name = ?", Integer.class, pcm.getName());
		                if (count > 1) { // possible... name isn't unique
		                    log.warn("PROBLEM: more than one Customer was found with the same SN sys id for remote customer: ", pcm.toString());
		                    int counter = 1;
		                    for (Customer customer : contractDaoService.findCustomerByName(pcm.getName())) {
		                        log.info("\t{}) ", new Object[]{counter++, customer.toString()});
		                    }
		                } else if (count == 1) {
		                    try {
		                        int updated = jdbcTemplate.update(updateQuery + "where name = ?", new Object[]{pcm.getId(),
		                            pcm.getAltId(), pcm.getName(), pcm.getDescription(), pcm.getPhone(), pcm.getStreet1(),
		                            pcm.getStreet2(), pcm.getCity(), pcm.getState(), pcm.getZip(), pcm.getCountry(),
		                            new DateTime().toDate(), "onecustomer", pcm.getName()});
		//                        log.debug("existing customer updated for remote Name with data: {}", pcm.toString());
		                    } catch (Exception any) {
		                        log.warn("failed to update ServiceInsight customer for remote Name [{}], message: [{}]",
		                                new Object[]{pcm.getName(), any.getMessage()});
		                    }
		                    continue;
		                }
		                Customer customer = new Customer();
		                customer.setAltId(pcm.getId());
		                customer.setServiceNowSysId(pcm.getAltId());
		                customer.setName(pcm.getName());
		                customer.setDescription(pcm.getDescription());
		                customer.setPhone(pcm.getPhone());
		                customer.setStreet1(pcm.getStreet1());
		                customer.setStreet2(pcm.getStreet2());
		                customer.setCity(pcm.getCity());
		                customer.setState(pcm.getState());
		                customer.setZip(pcm.getZip());
		                customer.setCountry(pcm.getCountry());
		                customer.setArchived(Boolean.FALSE);
		                customer.setSiEnabled(Boolean.FALSE);
		                
		                try {
		                    Long id = saveCustomer(customer);
		//                    log.debug("NEW customer created for remote data: {}", pcm.toString());
		                } catch (ServiceException ex) {
		                    Logger.getLogger(PricingIntegrationServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
		                }
		            }
		        } // end remote customer loop
		        log.info("Done with initial Customer sync... checking parent relationships");
		        
		        // re-loop to check, update parents
		        for (PricingCustomerMin pcm : pcmResults) {
		            PricingCustomerMin parent = pcm.getParent();
		            if (parent != null) {
		                try {
		                    // both the parent and the child *should* exist, at this point, by remote PK Id (alt_id)
		                    Long parentId = jdbcTemplate.queryForObject("select id from customer where alt_id = ?", Long.class, parent.getId());
		                    Long id = jdbcTemplate.queryForObject("select id from customer where alt_id = ?", Long.class, pcm.getId());
		                    int updated = jdbcTemplate.update("update customer set parent_id = ?, updated = ?,"
		                            + " updated_by = ? where id = ?", new Object[]{parentId, new DateTime().toDate(),
		                                "onecustomer", id});
		//                    log.debug("updated Customer with ID [{}] for parent with ID [{}]", new Object[]{id, parentId});
		                } catch(DataAccessException dae) {
		                    log.info("DataAccessException for parent with ID [{}] and child with ID [{}], message: [{}]",
		                            new Object[]{parent.getId(), pcm.getId(), dae.getMessage()});
		                }
		            }
		        }
		        log.info("Done with Customer parent updates. END OF CUSTOMER SYNC.");
		        log.info("Ending Task: " + st.getName());
	        }
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }

    Long saveCustomer(Customer customer) throws ServiceException {
        if (customer.getId() != null) {
            updateCustomer(customer);
            return customer.getId();
        }
        if (customer.getName() == null) {
            throw new ServiceException(messageSource.getMessage("validation_error_customer_name", null, LocaleContextHolder.getLocale()));
        }
        try {
            SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
            jdbcInsert.withTableName("customer").usingGeneratedKeyColumns("id");
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("alt_id", customer.getAltId());
            params.put("name", customer.getName());
            params.put("description", customer.getDescription());
            params.put("phone", customer.getPhone());
            params.put("street_1", customer.getStreet1());
            params.put("street_2", customer.getStreet2());
            params.put("city", customer.getCity());
            params.put("state", customer.getState());
            params.put("zip", customer.getZip());
            params.put("country", customer.getCountry());
            params.put("sn_sys_id", customer.getServiceNowSysId());
            params.put("archived", customer.getArchived());
            params.put("si_enabled", customer.getSiEnabled());
            params.put("created_by", "onecustomer");
            Number pk = jdbcInsert.executeAndReturnKey(new MapSqlParameterSource(params));
            return (Long) pk;
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_customer_insert", new Object[]{any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    }

    void updateCustomer(Customer customer) throws ServiceException {
        if (customer.getId() == null) {
            throw new ServiceException(messageSource.getMessage("validation_error_customer_id", null, LocaleContextHolder.getLocale()));
        }
        Integer count = jdbcTemplate.queryForObject("select count(*) from customer where id = ?", Integer.class, customer.getId());
        if (!count.equals(1)) {
            throw new ServiceException(messageSource.getMessage("customer_not_found_for_id", new Object[]{customer.getId()}, LocaleContextHolder.getLocale()));
        }
        if (customer.getName() == null) {
            throw new ServiceException(messageSource.getMessage("validation_error_customer_name", null, LocaleContextHolder.getLocale()));
        }
        try {
            int updated = jdbcTemplate.update("update customer set alt_id = ?, name = ?, description = ?, phone = ?, street_1 = ?,"
                    + " street_2 = ?, city = ?, state = ?, zip = ?, country = ?, archived = ?, sn_sys_id = ?,"
                    + " updated = ?, updated_by = ? where id = ?",
                    new Object[]{customer.getAltId(), customer.getName(), customer.getDescription(), customer.getPhone(),
                customer.getStreet1(), customer.getStreet2(), customer.getCity(), customer.getState(), customer.getZip(),
                customer.getCountry(), customer.getArchived(), customer.getServiceNowSysId(), new DateTime().withZone(DateTimeZone.forID(TZID)).toDate(),
                "onecustomer", customer.getId()});
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_customer_update", new Object[]{customer.getId(), any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    }

    void deleteCustomer(Long id) throws ServiceException {
        Integer count = jdbcTemplate.queryForObject("select count(*) from customer where id = ?", Integer.class, id);
        if (!count.equals(1)) {
            throw new ServiceException(messageSource.getMessage("customer_not_found_for_id", new Object[]{id}, LocaleContextHolder.getLocale()));
        }
        try {
            int updated = jdbcTemplate.update("delete from customer where id = ?", id);
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_customer_update", new Object[]{id, any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    }
}
