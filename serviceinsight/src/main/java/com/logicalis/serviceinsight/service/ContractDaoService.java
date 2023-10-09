package com.logicalis.serviceinsight.service;

import com.logicalis.serviceinsight.dao.AssetItem;
import com.logicalis.serviceinsight.dao.ExpenseType;
import com.logicalis.serviceinsight.dao.ExpenseCategory;
import com.logicalis.serviceinsight.dao.CostItem;
import com.logicalis.serviceinsight.dao.Expense;
import com.logicalis.serviceinsight.dao.ExpenseTypeRef;
import com.logicalis.serviceinsight.data.Contract;
import com.logicalis.serviceinsight.data.ContractAdjustment;
import com.logicalis.serviceinsight.data.ContractGroup;
import com.logicalis.serviceinsight.data.ContractInvoice;
import com.logicalis.serviceinsight.data.ContractServiceSubscription;
import com.logicalis.serviceinsight.data.ContractServiceDetail;
import com.logicalis.serviceinsight.data.ContractUpdate;
import com.logicalis.serviceinsight.data.Customer;
import com.logicalis.serviceinsight.data.Personnel;
import com.logicalis.serviceinsight.data.Service;
import com.logicalis.serviceinsight.data.UnitCostDetails;
import com.logicalis.serviceinsight.representation.APIContractService;
import com.logicalis.serviceinsight.representation.APICustomerSubscription;
import com.logicalis.serviceinsight.representation.APIPCRUpdateRequest;
import com.logicalis.serviceinsight.representation.APIPCRUpdateResponse;
import com.logicalis.serviceinsight.representation.APIPricingSheetProduct;
import com.logicalis.serviceinsight.representation.ServiceDetailRecord;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

/**
 * Data Access Operations (CRUD) should be here, ONLY - getById, getListOf,
 * create, update, delete
 *
 * Ok, well, maybe a few other operations like search and basic lists
 *
 * @author poneil
 */
public interface ContractDaoService extends BaseService {

    public Integer saveExpenseCategory(ExpenseCategory expenseCategory) throws ServiceException;

    public void updateExpenseCategory(ExpenseCategory expenseCategory) throws ServiceException;

    public void deleteExpenseCategory(Integer id) throws ServiceException, RelatedDataException;

    public ExpenseCategory expenseCategory(Integer id) throws ServiceException;

    public List<ExpenseCategory> expenseCategories();

    public Long saveCostItem(CostItem cost) throws ServiceException;

    public void updateCostItem(CostItem cost, boolean decrement) throws ServiceException;

    public void deleteCostItem(Long id) throws ServiceException;

    public CostItem costItem(Long id) throws ServiceException;

    public List<CostItem> costItems();

    public List<CostItem> costItemsForExpenseCategory(Integer expenseCategoryId);
    
    public List<CostItem> costItemsForCostTypeAndAppliedDate(CostItem.CostType costType, DateTime costDate);
    
    public List<CostItem> findSPLACosts(DateTime costDate, Long customerId, Long deviceId, Long splaId, String vendor);
    
    public UnitCostDetails unitCostDetailsForAppliedDate(Long customerId, Integer expenseCategoryId, DateTime costDate);
    
    public List<UnitCostDetails.CostDetail> costDetailsForDate(Long customerId, Integer expenseCategoryId, DateTime costDate);
    
    public List<UnitCostDetails.AssetDetail> assetDetailsForDate(Long customerId, Integer expenseCategoryId, DateTime costDate);
    
    public List<CostItem> findCostItemsForExpenseCategoryAndPeriod(Long customerId, Integer expenseCategoryId, Date leftDate, Date rightDate);

    public List<CostItem> findCostItemsByCustomer(Long customerId);

    public List<CostItem> findCostItemsByType(CostItem.CostType costType, CostItem.CostSubType costSubType);
    public List<CostItem> findCostItemsByCustomerIdAndTypeAndPeriod(CostItem.CostType costType, CostItem.CostSubType costSubType, Long customerId, Date leftDate, Date rightDate);

    public Long saveAssetItem(AssetItem asset) throws ServiceException;

    public void updateAssetItem(AssetItem asset, boolean decrement) throws ServiceException;

    public void deleteAssetItem(Long id) throws ServiceException;

    public AssetItem assetItem(Long id) throws ServiceException;

    public List<AssetItem> assetItems();

    public List<AssetItem> assetItemsForExpenseCategory(Integer expenseCategoryId);

    public List<AssetItem> assetItemsForType(Integer typeId);

    public List<AssetItem> findAssetItemsByCustomer(Long customerId);

    public List<AssetItem> findAssetItemsByCustomerIdAndPeriod(Long customerId, Date leftDate, Date rightDate);

    public Long saveExpense(Expense expense) throws ServiceException;

    public void updateExpense(Expense expense) throws ServiceException;

    public void deleteExpense(Long id) throws ServiceException;

    public Expense expense(Long id) throws ServiceException;

    public List<Expense> expenses();

    public List<Expense> findExpensesByCustomer(Long customerId);

    public List<Expense> findExpensesByCustomerIdAndPeriod(Long customerId, Date leftDate, Date rightDate);

    public Customer customer(Long id) throws ServiceException;

    public List<Customer> customers(Boolean archived, Boolean siEnabled, Boolean includeChildren);

    public List<Customer> customers();
    
    public List<ContractServiceDetail> contractServiceDetails();
    public void deleteContractServiceDetail(Long contractServiceId) throws ServiceException;
    
    /* This is not intended to be used within Service Insight's UI, but is part of the API in the event we want to push a customer to the DB
     * If you do, that customer should already exist in OSM and be passed with the SysID, so it can continue to sync in the future
     * */
    public Long saveCustomer(Customer customer) throws ServiceException;
    
    /**
     * We only allow a Service Insight specific fields to be controlled by the
     * user / application. Other Customer data is imported from a system of
     * record...
     *
     * @param customer
     * @throws ServiceException
     */
    public void updateCustomerBits(Customer customer) throws ServiceException;

    public Long saveContract(Contract contract) throws ServiceException;

    public void updateContract(Contract contract) throws ServiceException;

    public void deleteContract(Long id) throws ServiceException;

    public Contract contract(Long id) throws ServiceException;

    public Contract findContractByJobNumberAndCompanyId(String jobNumber, Long customerId) throws ServiceException;
    public Contract findContractByActiveM365ConfigTenantId(String tenantId) throws ServiceException;
    public List<Contract> findContractsByCustomerId(Long customerId, Boolean archived) throws ServiceException;
    

    public List<Contract> contracts();

    public List<Contract> contracts(Boolean archived);

    public Long saveContractInvoice(ContractInvoice contractInvoice) throws ServiceException;

    public void updateContractInvoice(ContractInvoice contractInvoice) throws ServiceException;

    public void deleteContractInvoice(Long id) throws ServiceException;

    public ContractInvoice contractInvoice(Long id) throws ServiceException;

    public List<ContractInvoice> contractInvoicesForContract(Long contractId) throws ServiceException;

    public List<ContractInvoice> contractInvoices();

    public List<BatchResult> batchContractInvoices(ContractInvoice[] contractInvoices);

    public void addOrDeleteContractInvoicesForContract(Long contractId, Date startDate, Date endDate) throws ServiceException;
    public boolean contractInvoiceConflictExistsForContractService(List<ContractInvoice> contractInvoices, Service service, BatchResult.Operation op);

    public Long saveContractGroup(ContractGroup contractGroup) throws ServiceException;

    public void updateContractGroup(ContractGroup contractGroup) throws ServiceException;

    public void deleteContractGroup(Long id) throws ServiceException;

    public ContractGroup contractGroup(Long id) throws ServiceException;

    public ContractGroup findContractGroupByNameAndContractId(String name, Long contractId) throws ServiceException;

    public List<ContractGroup> contractGroups();

    public Long saveContractUpdate(ContractUpdate contractUpdate) throws ServiceException;

    public void updateContractUpdate(ContractUpdate contractUpdate) throws ServiceException;

    public void deleteContractUpdate(Long id) throws ServiceException;

    public ContractUpdate contractUpdate(Long id) throws ServiceException;

    public List<ContractUpdate> contractUpdates();

    public Long saveContractAdjustment(ContractAdjustment contractAdjustment, boolean validateInvoiceConflict) throws ServiceException;

    public void updateContractAdjustment(ContractAdjustment contractAdjustment, boolean validateInvoiceConflict) throws ServiceException;

    public void deleteContractAdjustment(Long id, boolean validateInvoiceConflict) throws ServiceException;

    public ContractAdjustment contractAdjustment(Long id) throws ServiceException;

    public List<ContractAdjustment> contractAdjustments();

    public Long saveContractService(Service service, boolean validateInvoiceConflict) throws ServiceException;

    public void updateContractService(Service service, boolean validateInvoiceConflict) throws ServiceException;

    public void deleteContractService(Long id, boolean validateInvoiceConflict) throws ServiceException;

    public Service contractService(Long id) throws ServiceException;

    public List<Service> contractServices();
    
    public List<Service> contractServicesForDevice(Long deviceId);
    
    public void updateUnitCostServiceTotals() throws ServiceException;
    
    public List<BatchResult> batchContractServices(Service[] services) throws ServiceException;

    public List<BatchResult> batchContractAdjustments(ContractAdjustment[] contractAdjustments);

    public List<Service> searchContractServices(String name, Long contractId);

    public Service findContractServiceByNameAndContractId(String name, Long contractId);
    
    public ContractServiceSubscription contractServiceSubscription(Long id) throws ServiceException;
    public Long saveContractServiceSubscription(ContractServiceSubscription subscription, boolean validateInvoiceConflict) throws ServiceException;
    public void updateContractServiceSubscription(ContractServiceSubscription subscription, boolean validateInvoiceConflict) throws ServiceException;
    public void deleteContractServiceSubscription(Long subscriptionId, boolean validateInvoiceConflict) throws ServiceException;
    public List<ContractServiceSubscription> contractServiceSubscriptions(Long contractId, ContractServiceSubscription.SubscriptionType type);
    public List<ContractServiceSubscription> contractServiceSubscriptionsForMonthOf(Long contractId, Integer month, String year, ContractServiceSubscription.SubscriptionType type);
    public Service contractServiceByContractServiceSubscriptionAndStartDate(Long contractServiceSubscriptionId, Date startDate) throws ServiceException;
    public APICustomerSubscription apiCustomerSubscription(String subscriptionId, ContractServiceSubscription.SubscriptionType type) throws ServiceException;
    public void generateEmbeddedServicesForSystem() throws ServiceException; //admin method to generate embedded items for all contract services in the system. 
    public void copyCostCategoryServiceMappingsToCostCategoryDevice(); //admin method to copy mappings from cost_category_service to cost_category_device
    
    public List<Personnel> findCustomerPersonnel(Long customerId);
    public List<Personnel> findContractPersonnel(Long contractId);
    public void mapPersonnelToContract(Contract contract);
    public void mapEngagementManagerForServiceContractDetail(ServiceDetailRecord record);
    
    /**
     * Returns Service &lt;NAME, SERVICE ID&gt; (NOT OSP ID) Map for Enabled Services.
     * This method is designed for fast response, returning just the Service name
     * and OSP ID for say, use in a selection list.
     *
     * @return
     */
    public Map<String, Long> servicesMap();

    /**
     * Combines Services from the standard Service lookup and
     * and Services that were present in Contracts
     * 
     * @return 
     */
    public List<Service> servicesIncludingContractServices();
    
    /**
     * Returns Service object List ospId by NAME for Enabled Services.
     * This method is designed for fast response, returning just the Service name
     * and OSP ID for say, use in a selection list.
     *
     * @return
     */
    public List<Service> services();
    
    /**
     * Returns Services found from joining the contract_service table
     * with the Service table.
     * 
     * @return 
     */
    public List<Service> servicesInContracts();
    
    public Service serviceByOspIdAndActive(Long ospId);

    /**
     * @deprecated no longer use the service_expense_category table
     * @return 
     */
    public List<Service> servicesAndExpenseCategoryCount();

    /**
     * Returns OSP Services for SI-defined business model
     * 
     * @return
     */
    public List<Service> servicesForBusinessModel(String businessModel);

    public List<Long> serviceOSPIds();

    /**
     * Search customer with name strings
     *
     * @param search
     * @return
     */
    public List<Customer> searchCustomers(String search);

    /**
     * Returns a list of customers for a name. It's possible that more than one
     * customer exists for a name
     *
     * @param name
     * @return
     * @throws ServiceException
     */
    public List<Customer> findCustomerByName(String name);

    /**
     * Read an excel workbook file conforming to a specific format for importing
     * contract service records.
     *
     * A workbook contains a header record and contract service records
     *
     * record format: SOW (Job #)	Start	End	Company	CI Name	Service Name	Service
     * Description	Service Code/Part # One Time Cost	MRC	Quantity	Note
     *
     * @param file
     * @throws ServiceException
     */
    public void importContractServices(File excel) throws ServiceException;
    public ContractUpdate findContractUpdateByContractIdAndAltId(Long contractId, String altId) throws ServiceException;
    public Service contractServiceBySNSysId(String snSysId) throws ServiceException;
    
    
    public List<APIContractService> apiActiveContractServicesForContract(Long contractId) throws ServiceException;
    public List<BatchResult> batchAPIContractServices(APIContractService[] apiContractServices, Long contractUpdateId);
    public List<BatchResult> batchAPIPricingSheetProducts(APIPricingSheetProduct[] apiContractServices, Long contractUpdateId);
    public APIPCRUpdateResponse apiPCRUpdate(APIPCRUpdateRequest pcrUpdate);
    
    public List<Contract> contractsForDates(Date startDate, Date endDate);
    public List<ContractUpdate> contractUpdatesForDates(Date startDate, Date endDate);
    
    public List<Service> contractServiceParentRecordsForDeviceAndMonthOf(Long contractId, Long deviceId, Integer month, String year);
    public void mapChildToParent(Long childId, Long parentId) throws ServiceException;
}
