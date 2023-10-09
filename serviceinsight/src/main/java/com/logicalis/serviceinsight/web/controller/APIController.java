package com.logicalis.serviceinsight.web.controller;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.logicalis.serviceinsight.data.Contract;
import com.logicalis.serviceinsight.data.ContractServiceSubscription;
import com.logicalis.serviceinsight.data.Customer;
import com.logicalis.serviceinsight.data.Device;
import com.logicalis.serviceinsight.data.PricingSheet;
import com.logicalis.serviceinsight.data.PricingSheetProduct;
import com.logicalis.serviceinsight.data.Service;
import com.logicalis.serviceinsight.remote.ContractRequest;
import com.logicalis.serviceinsight.remote.ContractRequestService;
import com.logicalis.serviceinsight.remote.ContractResponse;
import com.logicalis.serviceinsight.remote.ContractResponseDetails;
import com.logicalis.serviceinsight.remote.ContractResponseLineItems;
import com.logicalis.serviceinsight.remote.GenericResponse;
import com.logicalis.serviceinsight.representation.APIContractService;
import com.logicalis.serviceinsight.representation.APICustomerSubscription;
import com.logicalis.serviceinsight.representation.APIPCRUpdateRequest;
import com.logicalis.serviceinsight.representation.APIPCRUpdateResponse;
import com.logicalis.serviceinsight.representation.APIPricingSheetProduct;
import com.logicalis.serviceinsight.service.ApplicationDataDaoService;
import com.logicalis.serviceinsight.service.BatchResult;
import com.logicalis.serviceinsight.service.ContractDaoService;
import com.logicalis.serviceinsight.service.ContractRevenueService;
import com.logicalis.serviceinsight.service.PricingSheetService;
import com.logicalis.serviceinsight.service.ServiceException;

@Controller
@RequestMapping("/api")
public class APIController extends BaseController {

	@Autowired
	ApplicationDataDaoService applicationDataDaoService;
	@Autowired
	PricingSheetService pricingSheetService;
	@Autowired
	ContractDaoService contractDaoService;
	@Autowired
    ContractRevenueService contractRevenueService;
	
	@ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/customers", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse customers() {
		try {
			log.info("API Action (Customers) Started: Returning Customers");
			List<Customer> customers = contractDaoService.customers(null, Boolean.TRUE, Boolean.FALSE);
			log.info("API Action (Customers) Complete [Success]: Returned " + customers.size() + " Customers");
			return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_osm_general_success", null, LocaleContextHolder.getLocale()), customers, "ArrayList<Customer>");
		} catch (Exception e) {
			log.info("API Action (Customers) Complete [Error]: General Exception Thrown");
			return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_get_customers", new Object[]{e.getMessage()}, LocaleContextHolder.getLocale()));
		}
    }
	
	@ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/devices", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse devices() {
		try {
			log.info("API Action (Devices) Started: Returning Devices");
			List<Device> devices = applicationDataDaoService.findDevicesForOSMSync();
			log.info("API Action (Devices) Complete [Success]: Returned " + devices.size() + " Devices");
			return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_osm_general_success", null, LocaleContextHolder.getLocale()), devices, "ArrayList<Device>");
		} catch(ServiceException se) {
			log.info("API Action (Devices) Complete [Error]: Service Exception Thrown");
			return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_osm_get_devices", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
		} catch (Exception e) {
			log.info("API Action (Devices) Complete [Error]: General Exception Thrown");
			return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_osm_get_devices", new Object[]{e.getMessage()}, LocaleContextHolder.getLocale()));
		}
    }
	
	
	@ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/pricingsheet", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public PricingSheet pricingSheetsForContract(@RequestParam(value = "coid", required = true) Long contractId) {
		PricingSheet sheet = null;
		try {
			log.info("API Action (Pricing Sheets) Started: Returning Pricing Sheet for Contract ID [" + contractId + "]");
			sheet = pricingSheetService.findPricingSheetForContractWithActiveProducts(contractId);
			log.info("API Action (Pricing Sheets) Complete [Success]: Returned Pricing Sheet for Contract ID [" + contractId + "]");
		} catch (Exception e) {
			log.error("caught exception", e);
		}
		return sheet;
    }
	
	@ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/activate/contractservices", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public List<APIContractService> getContractServicesForContract(@RequestParam(value = "coid", required = true) Long contractId) throws ServiceException {
		List<APIContractService> contractServices = new ArrayList<APIContractService>();
		log.info("API Action (Retrieve CIs For Contract ID [" + contractId + "]) Started: Returning CIs");
		contractServices = contractDaoService.apiActiveContractServicesForContract(contractId);
		log.info("API Action (Retrieve CIs for Contract ID [" + contractId + "]) Complete [Success]: Returned " + contractServices.size() + " Devices");
		return contractServices;
    }
	
	@ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/activate/pcr", method = RequestMethod.POST, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIPCRUpdateResponse pcrUpdate(@RequestBody APIPCRUpdateRequest request) throws ServiceException {
		log.info("API Action (Update CIs) Started: Updatings CIs");
		
		try {
			List<APIContractService> contractServices = request.getContractServices();
			for(APIContractService apiContractService : contractServices) {
				log.info("Line Items: " + apiContractService.toString());
			}
			List<APIPricingSheetProduct> pricingSheetProducts = request.getPricingSheetProducts();
			for(APIPricingSheetProduct pricingSheetProduct : pricingSheetProducts) {
				log.info("Pricing Sheet Products: " + pricingSheetProduct);
			}
			//List<BatchResult> batchResults = contractDaoService.batchAPIContractServices(contractServices);
			APIPCRUpdateResponse response = contractDaoService.apiPCRUpdate(request);
			
			List<BatchResult> batchResults = response.getBatchResults();
			log.info("API Action (Update CIs) Complete [Success]: Returned " + batchResults.size() + " Batch Results");
			return response;
		} catch (Exception e) {
			log.error("caught exception", e);
			throw new ServiceException("Error occurred!: " + e.getMessage());
		}
		
		/*try {
			log.info("API Action (Update CIs) Started: Updatings CIs");
			List<APIContractService> contractServices = request.getContractServices();
			for(APIContractService apiContractService : contractServices) {
				log.info("CI: " + apiContractService.toString());
			}
			//List<BatchResult> batchResults = contractDaoService.batchAPIContractServices(contractServices);
			APIPCRUpdateResponse response = contractDaoService.apiPCRUpdate(request);
			APIResponse.Status outcome = APIResponse.Status.OK;
			
			List<BatchResult> batchResults = response.getBatchResults();
			if(APIPCRUpdateResponse.Result.failed.equals(response.getPcrStatus())) {
				outcome = APIResponse.Status.ERROR;
			} else {
				for (BatchResult batchResult : batchResults) {
		            if (BatchResult.Result.failed.equals(batchResult.getResult())) {
		                outcome = APIResponse.Status.ERROR;
		                break;
		            }
		        }
			}
			List<APIPCRUpdateResponse> results = new ArrayList<APIPCRUpdateResponse>();
			results.add(response);
			
	        log.info("API Action (Update CIs) Complete [Success]: Returned " + batchResults.size() + " Batch Results");
			return new APIResponse(outcome, messageSource.getMessage("api_ok_batch_response", null, LocaleContextHolder.getLocale()), results, BatchResult.class.getName());
		} catch (Exception e) {
			log.info("API Action (Update CIs) Complete [Error]: General Exception Thrown");
			return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_osm_get_contractservices", new Object[]{e.getMessage()}, LocaleContextHolder.getLocale()));
		}*/		
    }
	
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/activate/contract", method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public ContractResponse contract(@RequestBody ContractRequest request) throws ServiceException {
        log.info("API Action (Create SOW [{}]) Started", new String[]{request.getContract().getAltId()});
	log.debug(request.toString());
        try {
            Contract contract = Contract.fromContractRequestContract(request.getContract());
            Long contractId = contractDaoService.saveContract(contract);

            List<BatchResult> batchResults = new ArrayList<BatchResult>();
            List<Service> services = new ArrayList<Service>();
            for (ContractRequestService item : request.getLineItems().getServices()) {
                services.add(Service.fromContractRequestService(item));
            }
            if (!services.isEmpty()) {
                for (Service service : services) {
                    service.setContractId(contractId);
                    service.setCustomerId(contract.getCustomerId());
                    service.setOperation(BatchResult.Operation.create.toString());
                    for (Service ch : service.getRelatedLineItems()) {
                        ch.setContractId(contractId);
                        ch.setOperation(BatchResult.Operation.create.toString());
                        for (Service gch : ch.getRelatedLineItems()) {
                            gch.setContractId(contractId);
                            gch.setOperation(BatchResult.Operation.create.toString());
                        } // that's as deep as we go...
                    }
                }
                batchResults = contractDaoService.batchContractServices(services.toArray(new Service[services.size()]));
            }

            List<BatchResult> subBatchResults = new ArrayList<BatchResult>();
            List<Service> subServices = new ArrayList<Service>();
            for (ContractRequestService item : request.getLineItems().getSubscriptionServices()) {
                subServices.add(Service.fromContractRequestService(item));
            }
            if (!subServices.isEmpty()) {
                Customer customer = contractDaoService.customer(contract.getCustomerId());
                for (Service service : subServices) {
                    service.setContractId(contractId);
                    service.setCustomerId(contract.getCustomerId());
                    service.setOperation(BatchResult.Operation.create.toString());
                    ContractServiceSubscription tosave = new ContractServiceSubscription(service.getId(), contractId,
                            service.getDeviceId(), service.getDevicePartNumber(), service.getDeviceDescription(),
                            service.getServiceId(), null, String.valueOf(service.getContractServiceSubscriptionId()), customer.getAzureCustomerId(),
                            service.getStartDate(), service.getEndDate(), service.getName(),
                            ContractServiceSubscription.SubscriptionType.valueOf(service.getSubscriptionType()), null);
                    log.debug(tosave.toString());
                    Long subId = contractDaoService.saveContractServiceSubscription(tosave, true);
                    subBatchResults.add(new BatchResult(subId, null, service.getCorrelationId(),
                            "Contract Service Subscription created", BatchResult.Operation.create,
                            BatchResult.Result.success));
                }
            }
            
            ContractResponse response = new ContractResponse();
            ContractResponseDetails responseDetails = new ContractResponseDetails();
            GenericResponse contractResponse = new GenericResponse(contractId, null, "OK",
                    String.format("Contract created with ID [%s]", new Object[]{contractId}));
            responseDetails.setContract(contractResponse);
            ContractResponseLineItems lineitems = new ContractResponseLineItems();
            for (BatchResult result : batchResults) {
                lineitems.getServices().add(new GenericResponse((Long) result.getId(), result.getCorrelationId(), "OK", result.getMessage()));
            }
            for (BatchResult result : subBatchResults) {
                lineitems.getSubscriptionServices().add(new GenericResponse((Long) result.getId(), result.getCorrelationId(), "OK", result.getMessage()));
            }
            responseDetails.setLineItems(lineitems);
            response.setResponse(responseDetails);
            int count = lineitems.getServices().size() + lineitems.getSubscriptionServices().size();
            log.info("API Action (Create SOW) Complete [Success]: Returned [{}] Batch Results", new Object[]{count});
            log.debug(response.toString());
            return response;
            
        } catch (Exception e) {
                log.error("caught exception", e);
                throw new ServiceException("Error occurred!: " + e.getMessage());
        }
    }
	
	/*Note - Similar methods are also available in the ContractController.
	*These are intended to be used for external APIs only, so we can limit the available of methods to the API
	*/
	
	@ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/contracts/{id}", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse contract(@PathVariable("id") Long id) throws ServiceException {
        try {
			log.info("API Action (Single Contract) Started: Returning Contract for ID[" + id + "]");
			Contract contract = contractDaoService.contract(id);
			List<Contract> contracts = new ArrayList<Contract>();
			contracts.add(contract);
			log.info("API Action (Single Contract) Complete [Success]: Returned " + contracts.size() + " Contract");
			return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_general_success", null, LocaleContextHolder.getLocale()), contracts, "ArrayList<Contract>");
		} catch (Exception e) {
			log.info("API Action (Single Contract) Complete [Error]: General Exception Thrown");
			return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_get_contracts", new Object[]{e.getMessage()}, LocaleContextHolder.getLocale()));
		}
    }
	
	@ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/contracts", method = RequestMethod.POST, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse createContract(@RequestBody Contract contract) throws ServiceException {
        try {
			log.info("API Action (Create Contract) Started: Creating Contract");
			Long contractId = contractDaoService.saveContract(contract);
			contract.setId(contractId);
			List<Contract> contracts = new ArrayList<Contract>();
			contracts.add(contract);
			log.info("API Action (Create Contract) Complete [Success]: Created Contract with ID[" + contractId + "]");
			return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_new_contract", new Object[]{contractId}, LocaleContextHolder.getLocale()), contracts, "ArrayList<Contract>");
		} catch (ServiceException se) {
			log.info("API Action (Create Contract) Complete [Error]: Service Exception Thrown");
			return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_new_contract", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
		} catch (Exception e) {
			log.info("API Action (Create Contract) Complete [Error]: General Exception Thrown");
			return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_new_contract", null, LocaleContextHolder.getLocale()));
		}
    }
	
	@ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/contracts/{id}", method = RequestMethod.PUT, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse updateContract(@PathVariable("id") Long id, @RequestBody Contract contract) throws ServiceException {
        try {
			log.info("API Action (Update Contract) Started: Updating Contract");
			contractDaoService.updateContract(contract);
			
			List<Contract> contracts = new ArrayList<Contract>();
			contracts.add(contract);
			log.info("API Action (Update Contract) Complete [Success]: Updated Contract with ID[" + contract.getId() + "]");
			return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_update_contract", new Object[]{contract.getId()}, LocaleContextHolder.getLocale()), contracts, "ArrayList<Contract>");
		} catch (ServiceException se) {
			log.info("API Action (Update Contract) Complete [Error]: Service Exception Thrown");
			return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_update_contract", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
		} catch (Exception e) {
			log.info("API Action (Update Contract) Complete [Error]: General Exception Thrown");
			return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_update_contract", null, LocaleContextHolder.getLocale()));
		}
    }
	
	@ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/contracts", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse contracts(@RequestParam(value = "cid", required = false) Long customerId, @RequestParam(value = "a", required = false) Boolean archived) {
		try {
			log.info("API Action (Contracts) Started: Returning Contracts for Customer[" + customerId + "] and Archived[" + archived + "]");
			List<Contract> contracts = contractRevenueService.contracts(customerId, archived);
			log.info("API Action (Contracts) Complete [Success]: Returned " + contracts.size() + " Contracts");
			return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_general_success", null, LocaleContextHolder.getLocale()), contracts, "ArrayList<Contract>");
		} catch (Exception e) {
			log.info("API Action (Contracts) Complete [Error]: General Exception Thrown");
			return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_get_contracts", new Object[]{e.getMessage()}, LocaleContextHolder.getLocale()));
		}
    }
	
	@ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/subscription/{id}", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APICustomerSubscription subscriptionInfo(@PathVariable("id") String subscriptionId, @RequestParam(value = "type", required = true) ContractServiceSubscription.SubscriptionType type) throws ServiceException {
		log.info("API Action (subscriptionInfo) Started: Returning APICustomerSubscription for ID[" + subscriptionId + "]");
		APICustomerSubscription subscription = contractDaoService.apiCustomerSubscription(subscriptionId, type);
		log.info("API Action (subscriptionInfo) Complete: Success: Returned " + subscription);
		return subscription;
    }
	
}
