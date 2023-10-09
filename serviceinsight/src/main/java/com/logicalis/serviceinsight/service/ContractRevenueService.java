package com.logicalis.serviceinsight.service;

import com.logicalis.serviceinsight.data.Contract;
import com.logicalis.serviceinsight.data.ContractAdjustment;
import com.logicalis.serviceinsight.data.ContractGroup;
import com.logicalis.serviceinsight.data.ContractInvoice;
import com.logicalis.serviceinsight.data.ContractServiceChangedConsolidatedWrapper;
import com.logicalis.serviceinsight.data.ContractUpdate;
import com.logicalis.serviceinsight.data.ContractServiceWrapper;
import com.logicalis.serviceinsight.data.Device;
import com.logicalis.serviceinsight.data.FullContract;
import com.logicalis.serviceinsight.data.Service;
import com.logicalis.serviceinsight.representation.CSPBilledContractService;
import com.logicalis.serviceinsight.representation.SDMCustomerExportWrapper;
import com.logicalis.serviceinsight.representation.SPLARevenue;
import com.logicalis.serviceinsight.representation.ServiceDetailRecordWrapper;

import java.util.Date;
import java.util.List;
import java.util.Map;
import org.joda.time.DateTime;

/**
 * This interface is for coding operations that query the system for contract
 * related revenue, taking in parameters and querying with joins on relevant
 * tables, etc to return contract revenue data in a form useful to the caller.
 *
 * @author poneil
 */
public interface ContractRevenueService extends BaseService {

    /**
     * Returns a Map of customer contract revenue where the key represents the
     * month name and the value represents a list of Service revenue (including
     * service "lineitem" revenue) for that month.
     *
     * @param contractId
     * @param year
     * @return
     */
    public Map<String, List<Service>> serviceRevenueForYear(Long contractId, Long contractGroupId, String year);

    /**
     * For the given parameters, returns an object that encapsulates contract
     * service data, which provides the rollup and summary data for a contract,
     * for a month.
     *
     * @param contractId
     * @param month
     * @param year
     * @return
     * @throws ServiceException
     */
    public ContractServiceWrapper wrapContractServices(Long contractId, Long contractGroupId, Integer month, String year) throws ServiceException;

    /**
     * For the given parameters, returns an object that encapsulates contract
     * service data, which provides the rollup and summary data for a contract,
     * for a month. This rollup shows the previous month's price instead of a
     * baseline of the current month
     *
     * @param contractId
     * @param month
     * @param year
     * @param includeDetails
     * @return
     * @throws ServiceException
     */
    public ContractServiceChangedConsolidatedWrapper wrapChangedConsolidatedContractServices(Long contractId, Long contractGroupId, Integer month, String year, boolean includeDetails) throws ServiceException;

    /**
     * For the given parameters, returns an object that encapsulates contract
     * service data, which provides the CI View and summary data for a contract,
     * for a month. 
     *
     * @param contractId
     * @param contractGroupId
     * @param month
     * @param year
     * @return
     * @throws ServiceException
     */
    public SDMCustomerExportWrapper wrapSDMCustomerExport(Long contractId, Long contractGroupId, Integer month, String year) throws ServiceException;
    
    /**
     * For the given parameters, returns an object that encapsulates contract
     * service data, which provides the rollup and summary data for a wildcard
     * customer name, engagement manager name, for a month
     *
     * @param customerName
     * @param sdmId
     * @param month
     * @param year
     * @param includeDetails
     * @return
     * @throws ServiceException
     */
    public List<ContractServiceChangedConsolidatedWrapper> wrapChangedConsolidatedContractServicesBySearch(String customerName, Long sdmId, Integer month, String year, boolean includeDetails, String invoiceStatus) throws ServiceException;

    /**
     * Returns customer contract revenue for services (including service
     * "lineitem" revenue) in a "rollup", meaning that contract Service records
     * with identical serviceId, contractId, startDate and endDate are SUMMED
     * (quantities and revenues).
     *
     * @param contractId
     * @param contractGroupId
     * @return
     */
    public List<Service> serviceRevenueRollup(Long contractId, Long contractGroupId, Service.Status status);

    /**
     * Returns customer contract revenue for services in a particular month
     * (including service "lineitem" revenue) in a "rollup", meaning that
     * contract Service records with identical serviceId, contractId, startDate
     * and endDate are SUMMED (quantities and revenues).
     *
     * @param contractId
     * @param contractGroupId
     * @param month
     * @param year
     * @return
     */
    public List<Service> serviceRevenueRollupForMonthOf(Long contractId, Long contractGroupId, Integer month, String year, Service.Status status);

    /**
     * Provides rollup of contract adjustments, including credits and fees for
     * any service on a contract or credits and fees set on just the contract
     * for the life of the contract.
     *
     * A rollup indicates that "equivalent" contract adjustments are summed.
     * Equivalent contract adjustments have the same contract id, service id,
     * adjustment type, start and end date.
     *
     * @param contractId
     * @param contractGroupId
     * @return
     */
    public List<ContractAdjustment> contractAdjustmentRollup(Long contractId, Long contractGroupId, Service.Status status);

    /**
     * Provides rollup of contract adjustments, including credits and fees for
     * any service on a contract or credits and fees set on just the contract
     * for the period specified.
     *
     * A rollup indicates that "equivalent" contract adjustments are summed.
     * Equivalent contract adjustments have the same contract id, service id,
     * adjustment type, start and end date.
     *
     * @param contractId
     * @param contractGroupId
     * @param month
     * @param year
     * @return
     */
    public List<ContractAdjustment> contractAdjustmentRollupForMonthOf(Long contractId, Long contractGroupId, Integer month, String year, Service.Status status);

    public List<ContractAdjustment> contractAdjustmentsForContract(Long contractId, Long contractGroupId, Service.Status status);

    public List<ContractAdjustment> contractAdjustmentsForMonthOfWithContractUpdate(Long contractId, Long contractGroupId, Integer month, String year, Service.Status status);

    /**
     * For the required input params, returns detailed Contract Service records for a specific report
     * 
     * @param ospId required if deviceId is null
     * @param deviceId required if ospId is null
     * @param svcDate required
     * @return 
     */
    public ServiceDetailRecordWrapper serviceDetailsForFilter(Long ospId, Long deviceId, Date svcDate);
    
    /**
     * For the method parameter filters, return what should be distinct contract
     * service records
     *
     * @param contractId
     * @param serviceId
     * @param startDate
     * @param endDate
     * @return
     */
    public List<Service> serviceRevenueRecordsForFilter(Long contractId, Long contractGroupId, Long serviceId, Long deviceId, Date startDate, Date endDate, Service.Status status);

    public List<Service> serviceRevenueRecordsForDateRange(Long contractId, Date startDate, Date endDate, Service.Status status);
    public List<Service> serviceRevenueParentRecordsForMonthOf(Long contractId, Long contractGroupId, Integer month, String year, Service.Status status, Boolean includeHiddenRecords);
    
    /**
     * For the method parameter filters, return what should be distinct contract
     * service records
     *
     * @param contractId
     * @param serviceId
     * @param startDate
     * @param endDate
     * @return
     */
    public List<ContractAdjustment> contractAdjustmentRecordsForFilter(Long contractId, String type, Date startDate, Date endDate, Service.Status status);

    /**
     * return contracts for a customer id
     *
     * @param customerId
     * @param archived
     * @return
     */
    public List<Contract> contracts(Long customerId, Boolean archived);

    /**
     * return contracts for a customer id
     *
     * @param customerId
     * @param archived
     * @return
     */
    //public List<FullContract> fullContracts(Long customerId, Boolean archived);
    public List<Contract> fullContracts(Long customerId, Boolean archived);

    /**
     * return contract updates for a contract id
     *
     * @param contractId
     * @return
     */
    public List<ContractUpdate> contractUpdates(Long contractId);

    /**
     * returns contract groups for a contract
     *
     * @param contractId
     * @return
     */
    public List<ContractGroup> contractGroups(Long contractId);

    /**
     * Returns a list of Contract Adjustments for a contract id
     *
     * @param contractId
     * @return
     */
    public List<ContractAdjustment> contractAdjustments(Long contractId);

    /**
     * return services for a contract id
     *
     * @param contractId
     * @return
     */
    public List<Service> services(Long contractId, Long contractGroupId);

    /**
     * return services for a contract update id
     *
     * @param contractId
     * @return
     */
    public List<Service> contractUpdateServices(Long contractUpdateId);

    /**
     * return a contract invoice based on search criteria
     *
     * @param contractId
     * @param month
     * @param year
     * @return
     */
    public ContractInvoice findContractInvoiceBySearchCriteria(Long contractId, Integer month, String year) throws ServiceException;
    
    public List<Contract> contractsForRenewal(Long customerId, Date renewalDate, List<Contract.RenewalStatus> inStatus, Boolean includeRevenue);
    public List<Contract> findContractsForForecastReport(Long customerId, Date startDate, Date endDate);
    
    public List<CSPBilledContractService> serviceRevenueRollupForMonthOfByCustomer(Device.DeviceType deviceType, Integer month, String year);
    public List<CSPBilledContractService> serviceRevenueCSPForMonthOf(Device.DeviceType deviceType, Integer month, String year);
    public List<SPLARevenue> splaRevenueReport(DateTime monthof, Long customerId, Long deviceId, Long splaId, String vendor);
}
