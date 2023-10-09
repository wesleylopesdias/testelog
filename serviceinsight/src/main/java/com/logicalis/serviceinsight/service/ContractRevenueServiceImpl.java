package com.logicalis.serviceinsight.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import com.logicalis.serviceinsight.data.Contract;
import com.logicalis.serviceinsight.data.ContractAdjustment;
import com.logicalis.serviceinsight.data.ContractAdjustment.AdjustmentType;
import com.logicalis.serviceinsight.data.ContractGroup;
import com.logicalis.serviceinsight.data.ContractInvoice;
import com.logicalis.serviceinsight.data.ContractServiceChangedConsolidatedWrapper;
import com.logicalis.serviceinsight.data.ContractServiceWrapper;
import com.logicalis.serviceinsight.data.ContractUpdate;
import com.logicalis.serviceinsight.data.Device;
import com.logicalis.serviceinsight.data.LineItemMonthlyRevenue;
import com.logicalis.serviceinsight.data.Service;
import com.logicalis.serviceinsight.representation.CSPBilledContractService;
import com.logicalis.serviceinsight.representation.SDMCustomerExportWrapper;
import com.logicalis.serviceinsight.representation.SPLARevenue;
import com.logicalis.serviceinsight.representation.ServiceDetailRecord;
import com.logicalis.serviceinsight.representation.ServiceDetailRecordWrapper;

/**
 *
 * @author poneil
 */
@org.springframework.stereotype.Service
public class ContractRevenueServiceImpl extends BaseServiceImpl implements ContractRevenueService {

    @Autowired
    ContractDaoService contractDaoService;
    @Autowired
    ApplicationDataDaoService dataService;

    @Override
    public Map<String, List<Service>> serviceRevenueForYear(Long contractId, Long contractGroupId, String year) {
        Map<String, List<Service>> yearlyRevenue = new TreeMap<String, List<Service>>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return months.valueOf(o1).compareTo(months.valueOf(o2));
            }
        });
        for (int month = 1; month <= 12; month++) {
            yearlyRevenue.put(
                    DateTimeFormat.forPattern("yyyy").parseDateTime(year)
                    .withMonthOfYear(month).monthOfYear().getAsText(),
                    serviceRevenueRollupForMonthOf(contractId, contractGroupId, month, year, Service.Status.active));
        }
        return yearlyRevenue;
    }

    @Override
    public ContractServiceWrapper wrapContractServices(Long contractId, Long contractGroupId, Integer month, String year) throws ServiceException {
        DateTime currentDate = DateTimeFormat.forPattern("yyyy").parseDateTime(year)
                .withMonthOfYear(month)
                .withDayOfMonth(1)
                .withTimeAtStartOfDay()
                .withZone(DateTimeZone.forID(TZID));
        DateTime priorDate = currentDate.minusMonths(1);
        List<Service> currentRollup = serviceRevenueRollupForMonthOf(contractId, contractGroupId, month, year, Service.Status.active);
        List<Service> priorRollup = serviceRevenueRollupForMonthOf(contractId, contractGroupId, priorDate.getMonthOfYear(),
                DateTimeFormat.forPattern("yyyy").print(priorDate), Service.Status.active);
        Contract contract = contractDaoService.contract(contractId);
        ContractServiceWrapper wrapper = new ContractServiceWrapper(
                contractDaoService.customer(contract.getCustomerId()), contract, month, year, TZID);
        for (Service service : currentRollup) {
            if (priorRollup.contains(service)) {
                wrapper.addBaseline(service);
            } else {
                wrapper.addAdded(service);
            }
        }
        for (Service service : priorRollup) {
            if (!currentRollup.contains(service)) {
                wrapper.addRemoved(service);
            }
        }
        List<ContractAdjustment> currentAdjustmentRollup = contractAdjustmentRollupForMonthOf(contractId, contractGroupId, month, year, Service.Status.active);
        List<ContractAdjustment> priorAdjustmentRollup = contractAdjustmentRollupForMonthOf(contractId, contractGroupId, priorDate.getMonthOfYear(),
                DateTimeFormat.forPattern("yyyy").print(priorDate), Service.Status.active);
        for (ContractAdjustment curradj : currentAdjustmentRollup) {
            boolean contained = false;
            for (ContractAdjustment priadj : priorAdjustmentRollup) {
                if (priadj.equalsForRollup(curradj)) {
                    wrapper.addBaselineAdjustment(curradj);
                    contained = true;
                }
            }
            if (!contained) {
                wrapper.addAddedAdjustment(curradj);
            }
        }
        for (ContractAdjustment priadj : priorAdjustmentRollup) {
            boolean contained = false;
            for (ContractAdjustment curradj : currentAdjustmentRollup) {
                if (curradj.equalsForRollup(priadj)) {
                    contained = true;
                }
            }
            if (!contained) {
                wrapper.addRemovedAdjustment(priadj);
            }
        }

        List<Service> currentDetails = new ArrayList<Service>();
        for (Service service : currentRollup) {
            List<Service> serviceDetails = serviceRevenueRecordsForFilter(service.getContractId(), service.getContractGroupId(), service.getServiceId(),
                    service.getDeviceId(), service.getStartDate(), service.getEndDate(), null);
            for (Service serviceDetail : serviceDetails) {
                serviceDetail.forMonth(currentDate);
            }
            currentDetails.addAll(serviceDetails);
        }
        List<Service> priorDetails = new ArrayList<Service>();
        for (Service service : priorRollup) {
            List<Service> serviceDetails = serviceRevenueRecordsForFilter(service.getContractId(), service.getContractGroupId(), service.getServiceId(),
                    service.getDeviceId(), service.getStartDate(), service.getEndDate(), null);
            for (Service serviceDetail : serviceDetails) {
                serviceDetail.forMonth(priorDate);
            }
            priorDetails.addAll(serviceDetails);
        }
        for (Service service : currentDetails) {
            if (priorDetails.contains(service)) {
                wrapper.addBaselineDetails(service);
            } else {
                wrapper.addAddedDetails(service);
            }
        }
        for (Service service : priorDetails) {
            if (!currentDetails.contains(service)) {
                wrapper.addRemovedDetails(service);
            }
        }
        return wrapper;
    }

    @Override
    public List<ContractServiceChangedConsolidatedWrapper> wrapChangedConsolidatedContractServicesBySearch(String customerName, Long sdmId, Integer month, String year, boolean includeDetails, String invoiceStatus) throws ServiceException {
        List<ContractServiceChangedConsolidatedWrapper> wrappers = new ArrayList<ContractServiceChangedConsolidatedWrapper>();
        try {
            List<Contract> contracts = findContractsBySearchCriteria(customerName, sdmId, month, year);
            
            //loop through contracts and build wrappers
            for (Contract contract : contracts) {
                /**
                 * null here is the contractGroupId: is a different search
                 * method desired that takes in a contractGroupId?
                 */
            	ContractServiceChangedConsolidatedWrapper wrapper = wrapChangedConsolidatedContractServices(contract.getId(), null, month, year, includeDetails);
            	if(invoiceStatus == null) {
            		wrappers.add(wrapper);
            	} else {
            		if(((wrapper.getContractInvoice() != null && "notInvoiced".equals(invoiceStatus) && !wrapper.getContractInvoice().getStatus().equals(ContractInvoice.Status.invoiced)) || 
            			(wrapper.getContractInvoice() == null && "notInvoiced".equals(invoiceStatus))) ||
            			(wrapper.getContractInvoice() != null && !"notInvoiced".equals(invoiceStatus) && wrapper.getContractInvoice().getStatus().equals(ContractInvoice.Status.valueOf(invoiceStatus)))) {
            			wrappers.add(wrapper);
            		}
            	}
            }
            
        } catch (Exception e) {
            /* TODO: I think we need to fix:
             * 1) we report errors to logging, not printStackTrace
             * 2) need to understand why we are just catching this exception and swallowing it.
             */
            e.printStackTrace();
        }
        return wrappers;
    }

    @Override
    public ContractServiceChangedConsolidatedWrapper wrapChangedConsolidatedContractServices(Long contractId, Long contractGroupId, Integer month, String year, boolean includeDetails) throws ServiceException {
        DateTime currentDate = DateTimeFormat.forPattern("yyyy").parseDateTime(year)
                .withMonthOfYear(month)
                .withDayOfMonth(1)
                .withTimeAtStartOfDay()
                .withZone(DateTimeZone.forID(TZID));
        DateTime priorDate = currentDate.minusMonths(1);
        BigDecimal currentOnetimeTotal = new BigDecimal(0);
        BigDecimal currentRecurringTotal = new BigDecimal(0);
        BigDecimal previousOnetimeTotal = new BigDecimal(0);
        BigDecimal previousRecurringTotal = new BigDecimal(0);
        List<Service> currentRollup = serviceRevenueRollupForMonthOf(contractId, contractGroupId, month, year, Service.Status.active);
        List<Service> priorRollup = serviceRevenueRollupForMonthOf(contractId, contractGroupId, priorDate.getMonthOfYear(),
                DateTimeFormat.forPattern("yyyy").print(priorDate), Service.Status.active);
        Contract contract = contractDaoService.contract(contractId);
        ContractServiceChangedConsolidatedWrapper wrapper = new ContractServiceChangedConsolidatedWrapper(
                contractDaoService.customer(contract.getCustomerId()), contract, month, year, TZID);
        for (Service service : currentRollup) {
            if (!priorRollup.contains(service)) {
                wrapper.addAdded(service);
            }
            currentOnetimeTotal = currentOnetimeTotal.add(service.getOnetimeRevenue());
            currentRecurringTotal = currentRecurringTotal.add(service.getRecurringRevenue());
        }
        
        

        //proration adjustment for services that start after the 1st of the month
        for (Service service : priorRollup) {
            if (!currentRollup.contains(service)) {
                wrapper.addRemoved(service);
            }
            wrapper.addPreviousMonth(service);
            
            previousOnetimeTotal = previousOnetimeTotal.add(service.getOnetimeRevenue());
            previousRecurringTotal = previousRecurringTotal.add(service.getRecurringRevenue());

            DateTime serviceStartDate = new DateTime(service.getStartDate());
            //see if the previous month was pro-rated
            if (serviceStartDate.isAfter(priorDate)) {
                Service currentServiceRecord = null;
                for (Service currentService : currentRollup) {
                    if (currentService.equals(service)) {
                        currentServiceRecord = currentService;
                    }
                }

                if (currentServiceRecord != null) {
                    BigDecimal recurringRevenueDifference = currentServiceRecord.getFullMonthRecurringRevenue().subtract(service.getRecurringRevenue());
                    Service prorateAdjustment = new Service(service.getId(), "", service.getParentId(), service.getContractId(), service.getContractGroupId(), service.getContractUpdateId(), service.getServiceId(), service.getOspId(),
                            service.getVersion(), "Prorated Adjustment: " + service.getName(), new BigDecimal(0), recurringRevenueDifference, service.getQuantity(),
                            service.getNote(), service.getLineitemCount(), service.getStartDate(), service.getEndDate(), service.getDeviceId(), service.getDeviceName(),
                            service.getDevicePartNumber(), service.getDeviceDescription(), service.getDeviceUnitCount(), Service.Status.active, service.getContractServiceSubscriptionId(), service.getSubscriptionType(), service.getMicrosoft365SubscriptionConfigId(), service.getLocationId());
                    prorateAdjustment.setProRatedAmount(true);
                    wrapper.addAdded(prorateAdjustment);
                }
            }
        }

        //proration adjustment for services that end before the last day of the month
        for (Service service : currentRollup) {

        	DateTime serviceStartDate = new DateTime(service.getStartDate());
            DateTime serviceEndDate = new DateTime(service.getEndDate());
            DateTime currentStartDate = currentDate.dayOfMonth().withMinimumValue();
            DateTime currentEndDate = currentDate.dayOfMonth().withMaximumValue();
            //see if the previous month was pro-rated
            if (serviceEndDate.isBefore(currentEndDate)) {
            	//check to make sure we aren't in the first month of the contract
            	DateTime contractStartDate = new DateTime(contract.getStartDate());
            	DateTime contractStartDateMonthEnd = new DateTime(contract.getStartDate()).dayOfMonth().withMaximumValue();
            	if(!serviceEndDate.isEqual(contractStartDate) && !(serviceEndDate.isAfter(contractStartDate) && serviceEndDate.isBefore(contractStartDateMonthEnd)) && !serviceStartDate.isAfter(currentStartDate)
            			&& !((serviceStartDate.isEqual(currentStartDate) || serviceStartDate.isAfter(currentStartDate)) && serviceEndDate.isBefore(currentEndDate))) {
	                Service currentServiceRecord = null;
	                for (Service currentService : currentRollup) {
	                    if (currentService.equals(service)) {
	                        currentServiceRecord = currentService;
	                    }
	                }
	
	                if (currentServiceRecord != null) {
	                    //BigDecimal recurringRevenueDifference = service.getUnitPriceRecurringRevenue().multiply(new BigDecimal(service.getQuantity())).subtract(currentServiceRecord.getRecurringRevenue());
	                	BigDecimal recurringRevenueDifference = service.getFullMonthRecurringRevenue().subtract(currentServiceRecord.getRecurringRevenue());
	                    Service prorateAdjustment = new Service(service.getId(), "", service.getParentId(), service.getContractId(), service.getContractGroupId(), service.getContractUpdateId(), service.getServiceId(), service.getOspId(),
	                            service.getVersion(), "Prorated Adjustment: " + service.getName(), new BigDecimal(0), recurringRevenueDifference, service.getQuantity(),
	                            service.getNote(), service.getLineitemCount(), service.getStartDate(), service.getEndDate(), service.getDeviceId(), service.getDeviceName(),
	                            service.getDevicePartNumber(), service.getDeviceDescription(), service.getDeviceUnitCount(), Service.Status.active, service.getContractServiceSubscriptionId(), service.getSubscriptionType(), service.getMicrosoft365SubscriptionConfigId(), service.getLocationId());
	                    prorateAdjustment.setProRatedAmount(true);
	                    wrapper.addRemoved(prorateAdjustment);
	                }
            	}
            }
        }
        List<ContractAdjustment> currentAdjustmentRollup = contractAdjustmentsForMonthOfWithContractUpdate(contractId, contractGroupId, month, year, Service.Status.active);
        List<ContractAdjustment> priorAdjustmentRollup = contractAdjustmentsForMonthOfWithContractUpdate(contractId, contractGroupId, priorDate.getMonthOfYear(),
                DateTimeFormat.forPattern("yyyy").print(priorDate), Service.Status.active);
        for (ContractAdjustment curradj : currentAdjustmentRollup) {
            boolean contained = false;
            for (ContractAdjustment priadj : priorAdjustmentRollup) {
                if (priadj.equalsForRollup(curradj)) {
                    contained = true;
                }
            }
            if (!contained) {
                wrapper.addAddedAdjustment(curradj);
            }
            
            if(curradj.getAdjustmentType().equals(ContractAdjustment.AdjustmentType.onetime.name())) {
            	currentOnetimeTotal = currentOnetimeTotal.add(curradj.getAdjustment());
            } else if(curradj.getAdjustmentType().equals(ContractAdjustment.AdjustmentType.recurring.name())) {
            	currentRecurringTotal = currentRecurringTotal.add(curradj.getAdjustment());
            }
        }
        for (ContractAdjustment priadj : priorAdjustmentRollup) {
            boolean contained = false;
            for (ContractAdjustment curradj : currentAdjustmentRollup) {
                if (curradj.equalsForRollup(priadj)) {
                    contained = true;
                }
            }
            if (!contained) {
                wrapper.addRemovedAdjustment(priadj);
            }
            wrapper.addPreviousMonthAdjustment(priadj);
            
            if(priadj.getAdjustmentType().equals(ContractAdjustment.AdjustmentType.onetime.name())) {
            	previousOnetimeTotal = previousOnetimeTotal.add(priadj.getAdjustment());
            } else if(priadj.getAdjustmentType().equals(ContractAdjustment.AdjustmentType.recurring.name())) {
            	previousRecurringTotal = previousRecurringTotal.add(priadj.getAdjustment());
            }
        }
        
        BigDecimal onetimeDifference = currentOnetimeTotal.subtract(previousOnetimeTotal);
        BigDecimal recurringDifference = currentRecurringTotal.subtract(previousRecurringTotal);
        wrapper.setOnetimeDifference(onetimeDifference);
        wrapper.setRecurringDifference(recurringDifference);

        if (includeDetails) {
            List<Service> currentDetails = new ArrayList<Service>();
            for (Service service : currentRollup) {
                List<Service> serviceDetails = serviceRevenueRecordsForFilter(service.getContractId(), service.getContractGroupId(), service.getServiceId(),
                        service.getDeviceId(), service.getStartDate(), service.getEndDate(), Service.Status.active);
                for (Service serviceDetail : serviceDetails) {
                    serviceDetail.forMonth(currentDate);
                }
                currentDetails.addAll(serviceDetails);
            }
            //service details
            List<Service> priorDetails = new ArrayList<Service>();
            for (Service service : priorRollup) {
                List<Service> serviceDetails = serviceRevenueRecordsForFilter(service.getContractId(), service.getContractGroupId(), service.getServiceId(),
                        service.getDeviceId(), service.getStartDate(), service.getEndDate(), Service.Status.active);
                for (Service serviceDetail : serviceDetails) {
                    serviceDetail.forMonth(priorDate);
                }
                priorDetails.addAll(serviceDetails);
            }
            for (Service service : currentDetails) {
                if (priorDetails.contains(service)) {
                    wrapper.addPreviousMonthDetails(service);
                } else {
                    wrapper.addAddedDetails(service);
                }
            }
            for (Service service : priorDetails) {
                if (!currentDetails.contains(service)) {
                    wrapper.addRemovedDetails(service);
                }
            }

            //adjustment details -- per request, we actually made the adjustment rollups more of a detailed view
            List<ContractAdjustment> currentAdjustmentDetails = currentAdjustmentRollup;
            List<ContractAdjustment> priorAdjustmentDetails = priorAdjustmentRollup;
            for (ContractAdjustment adjustment : currentAdjustmentDetails) {
                if (priorAdjustmentDetails.contains(adjustment)) {
                    wrapper.addPreviousMonthAdjustmentDetails(adjustment);
                } else {
                    wrapper.addAddedAdjustmentDetails(adjustment);
                }
            }
            for (ContractAdjustment adjustment : priorAdjustmentDetails) {
                if (!currentAdjustmentDetails.contains(adjustment)) {
                    wrapper.addRemovedAdjustmentDetails(adjustment);
                }
            }
        }

        //contract invoice
        wrapper.setContractInvoice(findContractInvoiceBySearchCriteria(contractId, month, year));
        return wrapper;
    }
    
    @Override
    public SDMCustomerExportWrapper wrapSDMCustomerExport(Long contractId, Long contractGroupId, Integer month, String year) throws ServiceException {
    	Contract contract = contractDaoService.contract(contractId);
    	SDMCustomerExportWrapper wrapper = new SDMCustomerExportWrapper(contractDaoService.customer(contract.getCustomerId()), contract, month, year, TZID);
    	
    	List<Service> services = serviceRevenueParentRecordsForMonthOf(contractId, contractGroupId, month, year, Service.Status.active, Boolean.TRUE);
    	wrapper.setServices(services);
    	
    	List<ContractAdjustment> adjustments = contractAdjustmentsForMonthOfWithContractUpdate(contractId, contractGroupId, month, year, Service.Status.active);
    	wrapper.setAdjustments(adjustments);
    	
    	return wrapper;
    }

    private List<Contract> findContractsBySearchCriteria(String customerName, Long sdmId, Integer month, String year) {
        List<Contract> contracts = null;
        try {
            String query = "select contract.customer_id, contract.id, contract.alt_id, contract.job_number, contract.name,"
                    + " contract.emgr, contract.sda, count(csvc.service_id) services, contract.signed_date, contract.service_start_date, contract.start_date, contract.end_date, contract.archived archived, contract.sn_sys_id," 
            		+ " contract.file_path, contract.renewal_status, contract.renewal_change, contract.renewal_notes"
                    + " from contract contract"
                    + " inner join customer cust on contract.customer_id = cust.id";
            
		            if(sdmId != null && sdmId.intValue() > 0) {
		            	query += " inner join contract_personnel cp on cp.contract_id = contract.id";
		            }
		            
                    query += " left outer join contract_service csvc on csvc.contract_id = contract.id"
                    + " left outer join contract_adjustment adj on adj.contract_id = contract.id"
                    + " where (((csvc.start_date <= :leftDate or csvc.start_date between :leftDate and :rightDate)"
                    + " and (csvc.end_date is null or csvc.end_date between :leftDate and :rightDate"
                    + " or csvc.end_date >= :rightDate))"
                    + " or ((adj.start_date <= :leftDate or adj.start_date between :leftDate and :rightDate)"
                    + " and (adj.end_date is null or adj.end_date between :leftDate and :rightDate"
                    + " or adj.end_date >= :rightDate)))";
            if (customerName != null && !"".equals(customerName)) {
                query += " and cust.name like :customerName";
            }
            
            if(sdmId != null && sdmId.intValue() > 0) {
                query += " and cp.user_id = :sdmId and cp.type = 'sdm'";
            }

            query += " group by contract.customer_id, contract.id, contract.alt_id, contract.job_number, contract.name,"
                    + " contract.emgr, contract.signed_date, contract.start_date, contract.end_date"
                    + " order by cust.name";

            String queryCustomerName = customerName.toLowerCase().trim() + "%";
            final DateTime datePointer = DateTimeFormat.forPattern("yyyy").parseDateTime(year)
                    .withMonthOfYear(month)
                    .withDayOfMonth(1)
                    .withTimeAtStartOfDay()
                    .withZone(DateTimeZone.forID(TZID));

            Map<String, Object> params = new HashMap<String, Object>();
            params.put("leftDate", datePointer.toDate());
            params.put("rightDate", datePointer.dayOfMonth().withMaximumValue().plusHours(23).plusMinutes(59).plusSeconds(59).toDate());
            params.put("customerName", queryCustomerName);
            if(sdmId != null && sdmId.intValue() > 0) {
            	params.put("sdmId", sdmId);
            }
            contracts = namedJdbcTemplate.query(query, params,
                    new RowMapper<Contract>() {
                @Override
                public Contract mapRow(ResultSet rs, int i) throws SQLException {
                    return new Contract(
                            rs.getLong("customer_id"),
                            rs.getLong("id"),
                            rs.getString("alt_id"),
                            rs.getString("job_number"),
                            rs.getString("name"),
                            rs.getString("emgr"),
                            rs.getString("sda"),
                            rs.getInt("services"),
                            rs.getDate("signed_date"),
                            rs.getDate("service_start_date"),
                            rs.getDate("start_date"),
                            rs.getDate("end_date"),
                            rs.getBoolean("archived"),
                            rs.getString("sn_sys_id"),
                            rs.getString("file_path"),
                            ((rs.getString("renewal_status") == null) ? null : Contract.RenewalStatus.valueOf(rs.getString("renewal_status"))),
                            rs.getBigDecimal("renewal_change"),
                            rs.getString("renewal_notes"));
                }
            });
            
            for(Contract contract: contracts) {
            	contractDaoService.mapPersonnelToContract(contract);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return contracts;
    }

    @Override
    public List<ContractAdjustment> contractAdjustmentsForContract(Long contractId, Long contractGroupId, Service.Status status) {
        String query = "select adj.id, adj.contract_id, adj.adjustment, adj.adjustment_type,"
                + " adj.start_date, adj.end_date, adj.note, adj.status adj_status,";
        if (contractGroupId != null && contractGroupId > 0) {
            query += " adj.contract_group_id,";
        } else {
            query += " 0 as contract_group_id,"; // allows us to still group by and use one object mapper
        }
        query += " cu.id cu_id, cu.alt_id cu_alt_id, cu.job_number cu_job_number, cu.ticket_number cu_ticket_number, cu.note cu_note,"
                + " cu.signed_date cu_signed_date, cu.effective_date cu_effective_date, cu.onetime_price cu_onetime_price, cu.recurring_price cu_recurring_price, cu.updated cu_updated, cu.updated_by cu_updated_by, cu.file_path cu_file_path"
                + " from contract_adjustment adj"
                + " left join contract c on adj.contract_id = c.id"
                + " left outer join contract_update cu on cu.id = adj.contract_update_id"
                + " where c.id = :contractId and adj.status = :status";
        if (contractGroupId != null && contractGroupId > 0) {
            query += " and adj.contract_group_id = :contractGroupId";
        }
        query += " order by adj.start_date desc";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("contractId", contractId);
        params.put("status", status.name());
        if (contractGroupId != null && contractGroupId > 0) {
            params.put("contractGroupId", contractGroupId);
        }
        List<ContractAdjustment> contractAdjustments = namedJdbcTemplate.query(query, params,
                new RowMapper<ContractAdjustment>() {
            @Override
            public ContractAdjustment mapRow(ResultSet rs, int i) throws SQLException {
                ContractAdjustment rec = new ContractAdjustment(
                        rs.getLong("id"),
                        rs.getLong("contract_id"),
                        null, // contract update id not included
                        (rs.getLong("contract_group_id") == 0 ? null : rs.getLong("contract_group_id")),
                        rs.getBigDecimal("adjustment"), // summed...
                        rs.getString("adjustment_type"), // grouped by...
                        rs.getString("note"),
                        null,
                        rs.getDate("start_date"),
                        rs.getDate("end_date"),
                        Service.Status.valueOf(rs.getString("adj_status")),
                        null, // created not included
                        null, // created_by not included
                        null, // updated not included
                        null); // updated_by not included
                if (rs.getLong("cu_id") > 0) {
                    rec.addContractUpdate(new ContractUpdate(
                            rs.getLong("cu_id"),
                            rs.getLong("contract_id"),
                            rs.getString("cu_alt_id"),
                            rs.getString("cu_job_number"),
                            rs.getString("cu_ticket_number"),
                            rs.getString("cu_note"),
                            rs.getDate("cu_signed_date"),
                            rs.getDate("cu_effective_date"),
                            rs.getBigDecimal("cu_onetime_price"),
                            rs.getBigDecimal("cu_recurring_price"),
                            rs.getDate("cu_updated"),
                            rs.getString("cu_updated_by"),
                            rs.getString("cu_file_path")));
                }
                return rec;
            }
        });

        return contractAdjustments;
    }

    @Override
    public ContractInvoice findContractInvoiceBySearchCriteria(Long contractId, Integer month, String year) throws ServiceException {
        /*Integer count = jdbcTemplate.queryForObject("select count(*) from contract_invoice where id = ?", Integer.class, contractId);
         if (!count.equals(1)) {
         throw new ServiceException(messageSource.getMessage("contract_invoice_not_found_for_id", new Object[]{id}, LocaleContextHolder.getLocale()));
         }*/
        Map<String, Object> params = new HashMap<String, Object>();
        final DateTime datePointer = DateTimeFormat.forPattern("yyyy").parseDateTime(year)
                .withMonthOfYear(month)
                .withDayOfMonth(1)
                .withTimeAtStartOfDay()
                .withZone(DateTimeZone.forID(TZID));
        params.put("leftDate", datePointer.toDate());
        params.put("rightDate", datePointer.dayOfMonth().withMaximumValue().plusHours(23).plusMinutes(59).plusSeconds(59).toDate());
        params.put("contractId", contractId);

        String countQuery = "select count(*) from contract_invoice where (start_date between :leftDate and :rightDate) and contract_id = :contractId";
        Integer count = namedJdbcTemplate.queryForObject(countQuery, params, Integer.class);
        if (!count.equals(1)) {
            log.debug("An issue occurred getting the contract invoice for Contract ID: " + contractId);
            return null;
        }

        ContractInvoice invoice = null;
        try {
            String query = "select ci.id, ci.contract_id, ci.status, ci.start_date, ci.end_date, ci.created, ci.created_by, ci.updated, ci.updated_by"
                    + " from contract_invoice ci"
                    + " where (ci.start_date between :leftDate and :rightDate)"
                    + " and ci.contract_id = :contractId";

            invoice = namedJdbcTemplate.queryForObject(query, params,
                    new RowMapper<ContractInvoice>() {
                @Override
                public ContractInvoice mapRow(ResultSet rs, int i) throws SQLException {
                    ContractInvoice ci = new ContractInvoice(
                            rs.getLong("id"),
                            rs.getLong("contract_id"),
                            ContractInvoice.Status.valueOf(rs.getString("status")),
                            rs.getDate("start_date"),
                            rs.getDate("end_date"));

                    ci.setCreated(rs.getDate("created"));
                    ci.setCreatedBy(rs.getString("created_by"));
                    ci.setUpdated(rs.getDate("updated"));
                    ci.setUpdatedBy(rs.getString("updated_by"));
                    return ci;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return invoice;
    }

    @Override
    public List<ContractAdjustment> contractAdjustmentRollup(Long contractId, Long contractGroupId, Service.Status status) {
        String query = "select adj.id, adj.contract_id, sum(adj.adjustment) adjustment, adj.adjustment_type,"
                + " adj.start_date, adj.end_date, adj.status adj_status,";
        if (contractGroupId != null && contractGroupId > 0) {
            query += " adj.contract_group_id,";
        } else {
            query += " 0 as contract_group_id,"; // allows us to still group by and use one object mapper
        }
        query += " cu.id cu_id, cu.alt_id cu_alt_id, cu.job_number cu_job_number, cu.ticket_number cu_ticket_number, cu.note cu_note,"
                + " cu.signed_date cu_signed_date, cu.effective_date cu_effective_date, cu.onetime_price cu_onetime_price, cu.recurring_price cu_recurring_price, cu.updated cu_updated, cu.updated_by cu_updated_by, cu.file_path cu_file_path"
                + " from contract_adjustment adj"
                + " left join contract c on adj.contract_id = c.id"
                + " left outer join contract_update cu on cu.id = adj.contract_update_id"
                + " where c.id = :contractId and adj.status = :status";
        if (contractGroupId != null && contractGroupId > 0) {
            query += " and adj.contract_group_id = :contractGroupId";
        }
        query += " group by adj.id, adj.contract_id, ";
        if (contractGroupId != null && contractGroupId > 0) {
            query += " adj.contract_group_id,";
        }
        query += "adj.adjustment_type, adj.start_date, adj.end_date, cu.id"
                + " order by adj.start_date desc";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("contractId", contractId);
        params.put("status", status.name());
        if (contractGroupId != null && contractGroupId > 0) {
            params.put("contractGroupId", contractGroupId);
        }
        List<ContractAdjustment> contractAdjustments = namedJdbcTemplate.query(query, params,
                new RowMapper<ContractAdjustment>() {
            @Override
            public ContractAdjustment mapRow(ResultSet rs, int i) throws SQLException {
                ContractAdjustment rec = new ContractAdjustment(
                        null, // no specific contract_adjustment record is queried
                        rs.getLong("contract_id"),
                        null, // contract update id not included
                        (rs.getLong("contract_group_id") == 0 ? null : rs.getLong("contract_group_id")),
                        rs.getBigDecimal("adjustment"), // summed...
                        rs.getString("adjustment_type"), // grouped by...
                        null, // note not included
                        null,
                        rs.getDate("start_date"),
                        rs.getDate("end_date"),
                        Service.Status.valueOf(rs.getString("adj_status")),
                        null, // created not included
                        null, // created_by not included
                        null, // updated not included
                        null); // updated_by not included
                if (rs.getLong("cu_id") > 0) {
                    rec.addContractUpdate(new ContractUpdate(
                            rs.getLong("cu_id"),
                            rs.getLong("contract_id"),
                            rs.getString("cu_alt_id"),
                            rs.getString("cu_job_number"),
                            rs.getString("cu_ticket_number"),
                            rs.getString("cu_note"),
                            rs.getDate("cu_signed_date"),
                            rs.getDate("cu_effective_date"),
                            rs.getBigDecimal("cu_onetime_price"),
                            rs.getBigDecimal("cu_recurring_price"),
                            rs.getDate("cu_updated"),
                            rs.getString("cu_updated_by"),
                            rs.getString("cu_file_path")));
                }
                return rec;
            }
        });

        return contractAdjustments;
    }

    @Override
    public List<ContractAdjustment> contractAdjustmentRollupForMonthOf(Long contractId, Long contractGroupId, Integer month, String year, Service.Status status) {
        String query = "select adj.contract_id, sum(adj.adjustment) adjustment, adj.adjustment_type,"
                + " adj.start_date, adj.end_date, adj.status adj_status,";
        if (contractGroupId != null && contractGroupId > 0) {
            query += " adj.contract_group_id";
        } else {
            query += " 0 as contract_group_id"; // allows us to still group by and use one object mapper
        }
        query += " from contract_adjustment adj"
                + " left join contract c on adj.contract_id = c.id"
                + " where c.id = :contractId and adj.status = :status";
        if (contractGroupId != null && contractGroupId > 0) {
            query += " and adj.contract_group_id = :contractGroupId";
        }
        query += " and (adj.start_date <= :leftDate or adj.start_date between :leftDate and :rightDate)"
                + " and (adj.end_date is null or adj.end_date between :leftDate and :rightDate"
                + " or adj.end_date >= :rightDate)"
                + " group by adj.contract_id, adj.adjustment_type,";
        if (contractGroupId != null && contractGroupId > 0) {
            query += " adj.contract_group_id,";
        }
        query += " adj.start_date, adj.end_date"
                + " order by adj.start_date desc";
        final DateTime datePointer = DateTimeFormat.forPattern("yyyy").parseDateTime(year)
                .withMonthOfYear(month)
                .withDayOfMonth(1)
                .withTimeAtStartOfDay()
                .withZone(DateTimeZone.forID(TZID));
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("contractId", contractId);
        params.put("status", status.name());
        if (contractGroupId != null && contractGroupId > 0) {
            params.put("contractGroupId", contractGroupId);
        }
        params.put("leftDate", datePointer.toDate());
        params.put("rightDate", datePointer.dayOfMonth().withMaximumValue().plusHours(23).plusMinutes(59).plusSeconds(59).toDate());
        List<ContractAdjustment> contractAdjustments = namedJdbcTemplate.query(query, params,
                new RowMapper<ContractAdjustment>() {
            @Override
            public ContractAdjustment mapRow(ResultSet rs, int i) throws SQLException {
                return new ContractAdjustment(
                        null, // no specific contract_adjustment record is queried
                        rs.getLong("contract_id"),
                        null, // contract update id not included
                        (rs.getLong("contract_group_id") == 0 ? null : rs.getLong("contract_group_id")),
                        rs.getBigDecimal("adjustment"), // summed...
                        rs.getString("adjustment_type"), // grouped by...
                        null, // note not included
                        datePointer,
                        rs.getDate("start_date"),
                        rs.getDate("end_date"),
                        Service.Status.valueOf(rs.getString("adj_status")),
                        null, // created not included
                        null, // created_by not included
                        null, // updated not included
                        null); // updated_by not included
            }
        });

        if (contractAdjustments == null || contractAdjustments.isEmpty()) {
            return contractAdjustments;
        }
        // next we run a query that provides contract contractAdjustment UPDATE(s) so that the updates
        // can be added to the rollup we just created
        List<ContractAdjustment> contractAdjustmentUpdates = contractAdjustmentRollupForMonthOfWithContractUpdate(contractId, contractGroupId, month, year, status);
        for (ContractAdjustment contractAdjustment : contractAdjustments) {
            log.debug("going over contractAdjustment from rollup: [{}]", contractAdjustment.toString());
            for (ContractAdjustment contractAdjustmentWithUpdate : contractAdjustmentUpdates) {
                log.debug("seeking over contractAdjustmentWithUpdate record: [{}]", contractAdjustmentWithUpdate.toString());
                if (contractAdjustment.equalsForRollup(contractAdjustmentWithUpdate)) {
                    log.debug("found a match to add contractAdjustment updates...");
                    if (contractAdjustmentWithUpdate.getContractUpdates() != null
                            && !contractAdjustmentWithUpdate.getContractUpdates().isEmpty()) {
                        for (ContractUpdate upd : contractAdjustmentWithUpdate.getContractUpdates()) {
                            log.debug("contract update being added: [{}]", upd.toString());
                        }
                    }
                    contractAdjustment.getContractUpdates().addAll(contractAdjustmentWithUpdate.getContractUpdates());
                }
            }
        }
        return contractAdjustments;
    }

    /**
     * This is an internal method to requery the rollup, including grouping by
     * Contract Updates to a Contract Adjustment.
     *
     * @param contractId
     * @param month
     * @param year
     * @return
     */
    private List<ContractAdjustment> contractAdjustmentRollupForMonthOfWithContractUpdate(Long contractId, Long contractGroupId, Integer month, String year, Service.Status status) {
        String query = "select adj.id, adj.contract_id, sum(adj.adjustment) adjustment, adj.adjustment_type,"
                + " adj.start_date, adj.end_date, adj.status adj_status,";
        if (contractGroupId != null && contractGroupId > 0) {
            query += " adj.contract_group_id,";
        } else {
            query += " 0 as contract_group_id,"; // allows us to still group by and use one object mapper
        }
        query += " cu.id cu_id, cu.alt_id cu_alt_id, cu.job_number cu_job_number, cu.ticket_number cu_ticket_number, cu.note cu_note,"
                + " cu.signed_date cu_signed_date, cu.effective_date cu_effective_date, cu.onetime_price cu_onetime_price, cu.recurring_price cu_recurring_price, cu.updated cu_updated, cu.updated_by cu_updated_by, cu.file_path cu_file_path"
                + " from contract_adjustment adj"
                + " left join contract c on adj.contract_id = c.id"
                + " left outer join contract_update cu on cu.id = adj.contract_update_id"
                + " where c.id = :contractId and adj.status = :status";
        if (contractGroupId != null && contractGroupId > 0) {
            query += " and adj.contract_group_id = :contractGroupId";
        }
        query += " and (adj.start_date <= :leftDate or adj.start_date between :leftDate and :rightDate)"
                + " and (adj.end_date is null or adj.end_date between :leftDate and :rightDate"
                + " or adj.end_date >= :rightDate)"
                + " group by adj.id, adj.contract_id,";
        if (contractGroupId != null && contractGroupId > 0) {
            query += " adj.contract_group_id,";
        }
        query += " adj.adjustment_type, adj.start_date, adj.end_date, cu.id"
                + " order by adj.start_date desc";
        final DateTime datePointer = DateTimeFormat.forPattern("yyyy").parseDateTime(year)
                .withMonthOfYear(month)
                .withDayOfMonth(1)
                .withTimeAtStartOfDay()
                .withZone(DateTimeZone.forID(TZID));
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("contractId", contractId);
        params.put("status", status.name());
        if (contractGroupId != null && contractGroupId > 0) {
            params.put("contractGroupId", contractGroupId);
        }
        params.put("leftDate", datePointer.toDate());
        params.put("rightDate", datePointer.dayOfMonth().withMaximumValue().plusHours(23).plusMinutes(59).plusSeconds(59).toDate());
        return namedJdbcTemplate.query(query, params,
                new RowMapper<ContractAdjustment>() {
            @Override
            public ContractAdjustment mapRow(ResultSet rs, int i) throws SQLException {
                ContractAdjustment rec = new ContractAdjustment(
                        null, // no specific contract_adjustment record is queried
                        rs.getLong("contract_id"),
                        null, // contract update id not included
                        (rs.getLong("contract_group_id") == 0 ? null : rs.getLong("contract_group_id")),
                        rs.getBigDecimal("adjustment"), // summed...
                        rs.getString("adjustment_type"), // grouped by...
                        null, // note not included
                        datePointer,
                        rs.getDate("start_date"),
                        rs.getDate("end_date"),
                        Service.Status.valueOf(rs.getString("adj_status")),
                        null, // created not included
                        null, // created_by not included
                        null, // updated not included
                        null); // updated_by not included
                if (rs.getLong("cu_id") > 0) {
                    rec.addContractUpdate(new ContractUpdate(
                            rs.getLong("cu_id"),
                            rs.getLong("contract_id"),
                            rs.getString("cu_alt_id"),
                            rs.getString("cu_job_number"),
                            rs.getString("cu_ticket_number"),
                            rs.getString("cu_note"),
                            rs.getDate("cu_signed_date"),
                            rs.getDate("cu_effective_date"),
                            rs.getBigDecimal("cu_onetime_price"),
                            rs.getBigDecimal("cu_recurring_price"),
                            rs.getDate("cu_updated"),
                            rs.getString("cu_updated_by"),
                            rs.getString("cu_file_path")));
                }
                return rec;
            }
        });
    }

    public List<ContractAdjustment> contractAdjustmentsForMonthOfWithContractUpdate(Long contractId, Long contractGroupId, Integer month, String year, Service.Status status) {
        String query = "select adj.id adj_id, adj.contract_id, adj.adjustment, adj.adjustment_type,"
                + " adj.start_date, adj.end_date, adj.status adj_status,";
        if (contractGroupId != null && contractGroupId > 0) {
            query += " adj.contract_group_id,";
        } else {
            query += " 0 as contract_group_id,"; // allows us to still group by and use one object mapper
        }
        query += " cu.id cu_id, cu.alt_id cu_alt_id, cu.job_number cu_job_number, cu.ticket_number cu_ticket_number, cu.note cu_note,"
                + " cu.signed_date cu_signed_date, cu.effective_date cu_effective_date, cu.onetime_price cu_onetime_price, cu.recurring_price cu_recurring_price, cu.updated cu_updated, cu.updated_by cu_updated_by, cu.file_path cu_file_path"
                + " from contract_adjustment adj"
                + " left join contract c on adj.contract_id = c.id"
                + " left outer join contract_update cu on cu.id = adj.contract_update_id"
                + " where c.id = :contractId and adj.status = :status";
        if (contractGroupId != null && contractGroupId > 0) {
            query += " and adj.contract_group_id = :contractGroupId";
        }
        query += " and (adj.start_date <= :leftDate or adj.start_date between :leftDate and :rightDate)"
                + " and (adj.end_date is null or adj.end_date between :leftDate and :rightDate"
                + " or adj.end_date >= :rightDate)"
                + " order by adj.start_date desc";
        final DateTime datePointer = DateTimeFormat.forPattern("yyyy").parseDateTime(year)
                .withMonthOfYear(month)
                .withDayOfMonth(1)
                .withTimeAtStartOfDay()
                .withZone(DateTimeZone.forID(TZID));
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("contractId", contractId);
        params.put("status", status.name());
        if (contractGroupId != null && contractGroupId > 0) {
            params.put("contractGroupId", contractGroupId);
        }
        params.put("leftDate", datePointer.toDate());
        params.put("rightDate", datePointer.dayOfMonth().withMaximumValue().plusHours(23).plusMinutes(59).plusSeconds(59).toDate());
        return namedJdbcTemplate.query(query, params,
                new RowMapper<ContractAdjustment>() {
            @Override
            public ContractAdjustment mapRow(ResultSet rs, int i) throws SQLException {
                ContractAdjustment rec = new ContractAdjustment(
                        rs.getLong("adj_id"), // no specific contract_adjustment record is queried
                        rs.getLong("contract_id"),
                        null, // contract update id not included
                        (rs.getLong("contract_group_id") == 0 ? null : rs.getLong("contract_group_id")),
                        rs.getBigDecimal("adjustment"), // summed...
                        rs.getString("adjustment_type"), // grouped by...
                        null, // note not included
                        datePointer,
                        rs.getDate("start_date"),
                        rs.getDate("end_date"),
                        Service.Status.valueOf(rs.getString("adj_status")),
                        null, // created not included
                        null, // created_by not included
                        null, // updated not included
                        null); // updated_by not included
                if (rs.getLong("cu_id") > 0) {
                    rec.addContractUpdate(new ContractUpdate(
                            rs.getLong("cu_id"),
                            rs.getLong("contract_id"),
                            rs.getString("cu_alt_id"),
                            rs.getString("cu_job_number"),
                            rs.getString("cu_ticket_number"),
                            rs.getString("cu_note"),
                            rs.getDate("cu_signed_date"),
                            rs.getDate("cu_effective_date"),
                            rs.getBigDecimal("cu_onetime_price"),
                            rs.getBigDecimal("cu_recurring_price"),
                            rs.getDate("cu_updated"),
                            rs.getString("cu_updated_by"),
                            rs.getString("cu_file_path")));
                }
                return rec;
            }
        });
    }

    @Override
    public List<Service> serviceRevenueRollupForMonthOf(Long contractId, Long contractGroupId, Integer month, String year, Service.Status status) {
        String query = "select csvc.service_id, csvc.contract_id,";
        if (contractGroupId != null && contractGroupId > 0) {
            query += " csvc.contract_group_id,";
        } else {
            query += " 0 as contract_group_id,"; // allows us to still group by and use one object mapper
        }
        query += " svc.code, svc.osp_id, svc.name, dev.id devid, dev.part_number, dev.description ddescr,"
                + " sum(csd.unit_count) dcount, sum(csvc.quantity) quantity, csvc.start_date,"
                + " csvc.end_date, GROUP_CONCAT(distinct csvc.status SEPARATOR ', ') as status, csvc.status, sum(csvc.quantity * csvc.onetime_revenue) onetime,"
                + " sum(csvc.quantity * csvc.recurring_revenue) revenue"
                + " from service svc"
                + " left join contract_service csvc on csvc.service_id = svc.id"
                + " left outer join contract_service_device csd on csd.contract_service_id = csvc.id"
                + " left outer join device dev on dev.id = csd.device_id"
                + " where csvc.contract_id = :contractId and csvc.hidden = false";
        if (contractGroupId != null && contractGroupId > 0) {
            query += " and csvc.contract_group_id = :contractGroupId";
        }
        if (status != null) {
            query += " and csvc.status = :status";
        }
        query += " and (csvc.start_date <= :leftDate or csvc.start_date between :leftDate and :rightDate)"
                + " and (csvc.end_date is null or csvc.end_date between :leftDate and :rightDate"
                + " or csvc.end_date >= :rightDate)";
        query += " group by csvc.service_id, csvc.contract_id,";
        if (contractGroupId != null && contractGroupId > 0) {
            query += " csvc.contract_group_id,";
        }
        query += " csvc.start_date, csvc.end_date, dev.part_number, dev.description"
                + " order by svc.name, csvc.start_date, dev.part_number, dev.description";

        final DateTime datePointer = DateTimeFormat.forPattern("yyyy").parseDateTime(year)
                .withMonthOfYear(month)
                .withDayOfMonth(1)
                .withTimeAtStartOfDay()
                .withZone(DateTimeZone.forID(TZID));
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("contractId", contractId);
        if (contractGroupId != null && contractGroupId > 0) {
            params.put("contractGroupId", contractGroupId);
        }
        if (status != null) {
            params.put("status", status.getDescription());
        }
        params.put("leftDate", datePointer.toDate());
        params.put("rightDate", datePointer.dayOfMonth().withMaximumValue().plusHours(23).plusMinutes(59).plusSeconds(59).toDate());
        List<Service> contractServices = namedJdbcTemplate.query(query, params,
                new RowMapper<Service>() {
            @Override
            public Service mapRow(ResultSet rs, int i) throws SQLException {
                return new Service(
                        datePointer,
                        rs.getString("code"),
                        rs.getLong("contract_id"),
                        (rs.getLong("contract_group_id") == 0 ? null : rs.getLong("contract_group_id")),
                        null, // contractUpdateId
                        rs.getLong("service_id"),
                        rs.getString("osp_id"),
                        null, // version
                        rs.getString("name"),
                        rs.getBigDecimal("onetime"),
                        rs.getBigDecimal("revenue"),
                        rs.getInt("quantity"),
                        rs.getDate("start_date"),
                        rs.getDate("end_date"),
                        (rs.getLong("devid") == 0 ? null : rs.getLong("devid")),
                        null, // for a rollup, we don't want to differentiate ind device instances
                        rs.getString("part_number"),
                        rs.getString("ddescr"),
                        rs.getInt("dcount"),
                        ((rs.getString("status") != null && rs.getString("status").contains("pending")) ? true : false));
            }
        });

        if (contractServices == null || contractServices.isEmpty()) {
            return contractServices;
        }
        // next we run a query that provides contract service UPDATE(s) so that the updates
        // can be added to the rollup we just created
        List<Service> contractServiceUpdates = serviceRevenueRollupForMonthOfWithContractUpdate(contractId, contractGroupId, month, year);
        for (Service service : contractServices) {
            for (Service serviceWithUpdate : contractServiceUpdates) {
                if (service.rollupEquals(serviceWithUpdate)) {
                    service.getContractUpdates().addAll(serviceWithUpdate.getContractUpdates());
                }
            }
        }

        return contractServices;
    }
    
    @Override
    public List<Service> serviceRevenueParentRecordsForMonthOf(Long contractId, Long contractGroupId, Integer month, String year, Service.Status status, Boolean includeHiddenRecords) {
    	String query = "select csvc.id, csvc.service_id, csvc.parent_id, csvc.contract_id, csvc.contract_group_id, svc.code, svc.osp_id, svc.name, svc.version,"
                + " csvc.onetime_revenue, csvc.recurring_revenue, csvc.note, csvc.quantity, "
                + " csvc.start_date, csvc.end_date, csvc.status, dev.id devid, csd.name dname, csd.unit_count dcount, csd.location_id dlocation, dev.part_number, dev.description ddescr, csvc.contract_service_subscription_id, css.subscription_type, csvc.microsoft_365_subscription_config_id "
                + " from contract_service csvc"
                + " inner join service svc on svc.id = csvc.service_id"
                + " inner join contract_service_device csd on csd.contract_service_id = csvc.id"
                + " inner join device dev on dev.id = csd.device_id"
                + " left outer join contract_service_subscription css on css.id = csvc.contract_service_subscription_id"
                + " where csvc.contract_id = :contractId"
                + " and csvc.parent_id is null";
    	if(!includeHiddenRecords) {
    		query += " and csvc.hidden = false";
    	}
    	
        if (contractGroupId != null && contractGroupId > 0) {
            query += " and csvc.contract_group_id = :contractGroupId";
        }
        if (status != null) {
            query += " and csvc.status = :status";
        }
        
        query += " and (csvc.start_date <= :leftDate or csvc.start_date between :leftDate and :rightDate)"
                + " and (csvc.end_date is null or csvc.end_date between :leftDate and :rightDate"
                + " or csvc.end_date >= :rightDate)"
                + " order by svc.name, csvc.start_date, dev.part_number, dev.description";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("contractId", contractId);
        if (contractGroupId != null && contractGroupId > 0) {
            params.put("contractGroupId", contractGroupId);
        }
        if (status != null) {
            params.put("status", status.getDescription());
        }
        final DateTime datePointer = DateTimeFormat.forPattern("yyyy").parseDateTime(year)
                .withMonthOfYear(month)
                .withDayOfMonth(1)
                .withTimeAtStartOfDay()
                .withZone(DateTimeZone.forID(TZID));
        Date leftDate = datePointer.toDate();
        Date rightDate = datePointer.dayOfMonth().withMaximumValue().plusHours(23).plusMinutes(59).plusSeconds(59).toDate();
        params.put("leftDate", leftDate);
        params.put("rightDate", rightDate);
        
        List<Service> contractServices = namedJdbcTemplate.query(query, params,
                new RowMapper<Service>() {
            @Override
            public Service mapRow(ResultSet rs, int i) throws SQLException {
                return new Service(
                        rs.getLong("id"),
                        rs.getString("code"),
                        (rs.getLong("parent_id") == 0 ? null : rs.getLong("parent_id")),
                        rs.getLong("contract_id"),
                        (rs.getLong("contract_group_id") == 0 ? null : rs.getLong("contract_group_id")),
                        null, // contractUpdateId
                        rs.getLong("service_id"),
                        rs.getString("osp_id"),
                        rs.getDouble("version"),
                        rs.getString("name"),
                        rs.getBigDecimal("onetime_revenue"),
                        rs.getBigDecimal("recurring_revenue"),
                        rs.getInt("quantity"),
                        rs.getString("note"),
                        0,
                        rs.getDate("start_date"),
                        rs.getDate("end_date"),
                        (rs.getLong("devid") == 0 ? null : rs.getLong("devid")),
                        rs.getString("dname"),
                        rs.getString("part_number"),
                        rs.getString("ddescr"),
                        (rs.getInt("dcount") == 0 ? null : rs.getInt("dcount")),
                        Service.Status.valueOf(rs.getString("status")),
                        (rs.getLong("contract_service_subscription_id") == 0 ? null : rs.getLong("contract_service_subscription_id")),
                        rs.getString("subscription_type"),
                        (rs.getLong("microsoft_365_subscription_config_id") == 0 ? null : rs.getLong("microsoft_365_subscription_config_id")),
                        (rs.getInt("dlocation") == 0 ? null : rs.getInt("dlocation")));
            }
        });
        for (Service cs : contractServices) {
        	cs.setMonth(datePointer);
            // attach the contract service - contract updates
            cs.setContractUpdates(jdbcTemplate.query("select cu.id, cu.alt_id, cu.job_number, cu.ticket_number, cu.note,"
                    + " cu.contract_id, cu.signed_date, cu.effective_date, cu.onetime_price onetime_price, cu.recurring_price recurring_price, cu.updated, cu.updated_by, cu.file_path cu_file_path, cucs.note,"
                    + " cucs.operation"
                    + " from contract_update_contract_service cucs"
                    + " left join contract_service csvc on cucs.contract_service_id = csvc.id"
                    + " left join contract_update cu on cucs.contract_update_id = cu.id"
                    + " where csvc.id = ?"
                    + " order by cucs.created desc", new Object[]{cs.getId()}, new RowMapper<ContractUpdate>() {
                @Override
                public ContractUpdate mapRow(ResultSet rs, int i) throws SQLException {
                    return new ContractUpdate(
                            rs.getLong("id"),
                            rs.getLong("contract_id"),
                            rs.getString("alt_id"),
                            rs.getString("job_number"),
                            rs.getString("ticket_number"),
                            rs.getString("note"),
                            rs.getDate("signed_date"),
                            rs.getDate("effective_date"),
                            rs.getBigDecimal("onetime_price"),
                            rs.getBigDecimal("recurring_price"),
                            rs.getDate("updated"),
                            rs.getString("updated_by"),
                            rs.getString("cu_file_path"));
                }
            }));
            // attach possible related lineitems... note: these records are already a part of this
            // list, but now they can be nested under a parent as well for preferred processing
            List<Service> relatedLineItems = serviceRevenueRelatedRecordsForParent(cs.getId(), leftDate, rightDate);
            for(Service relatedLineItem: relatedLineItems) {
            	relatedLineItem.setMonth(datePointer);
            	
            	List<Service> grandChildLineItems = serviceRevenueRelatedRecordsForParent(relatedLineItem.getId(), leftDate, rightDate);
            	for(Service grandChildLineItem: grandChildLineItems) {
            		grandChildLineItem.setMonth(datePointer);
            	}
            	relatedLineItem.setRelatedLineItems(grandChildLineItems);
            }
            cs.setRelatedLineItems(relatedLineItems);
        }
        return contractServices;
    }

    /**
     * This is an internal method to requery the rollup, including grouping by
     * Contract Updates to a Contract Service.
     *
     * @param contractId
     * @param contractGroupId
     * @param month
     * @param year
     * @return
     */
    private List<Service> serviceRevenueRollupForMonthOfWithContractUpdate(Long contractId, Long contractGroupId, Integer month, String year) {
        String query = "select csvc.service_id, csvc.contract_id,";
        if (contractGroupId != null && contractGroupId > 0) {
            query += " csvc.contract_group_id,";
        } else {
            query += " 0 as contract_group_id,"; // allows us to still group by and use one object mapper
        }
        query += " svc.code, svc.osp_id, svc.name, dev.id devid, dev.part_number, dev.description ddescr,"
                + " csd.unit_count dcount, sum(csvc.quantity) quantity, csvc.start_date,"
                + " csvc.end_date, GROUP_CONCAT(distinct csvc.status SEPARATOR ', ') as status, sum(csvc.quantity * csvc.onetime_revenue) onetime,"
                + " sum(csvc.quantity * csvc.recurring_revenue) revenue,"
                + " cu.id cu_id, cu.alt_id cu_alt_id, cu.job_number cu_job_number, cu.ticket_number cu_ticket_number, cu.note cu_note,"
                + " cu.signed_date cu_signed_date, cu.effective_date cu_effective_date, cu.onetime_price cu_onetime_price, cu.recurring_price cu_recurring_price, cu.updated cu_updated, cu.updated_by cu_updated_by, cu.file_path cu_file_path"
                + " from service svc"
                + " left join contract_service csvc on csvc.service_id = svc.id"
                + " left outer join contract_service_device csd on csd.contract_service_id = csvc.id"
                + " left outer join device dev on dev.id = csd.device_id"
                + " left outer join contract_update_contract_service cucs on cucs.contract_service_id = csvc.id"
                + " left outer join contract_update cu on cu.id = cucs.contract_update_id"
                + " where csvc.contract_id = :contractId";
        if (contractGroupId != null && contractGroupId > 0) {
            query += " and csvc.contract_group_id = :contractGroupId";
        }
        query += " and (csvc.start_date <= :leftDate or csvc.start_date between :leftDate and :rightDate)"
                + " and (csvc.end_date is null or csvc.end_date between :leftDate and :rightDate"
                + " or csvc.end_date >= :rightDate)"
                + " group by csvc.service_id, csvc.contract_id,";
        if (contractGroupId != null && contractGroupId > 0) {
            query += " csvc.contract_group_id,";
        }
        query += " csvc.start_date, csvc.end_date, dev.part_number, dev.description, cu.id"
                + " order by svc.name, csvc.start_date, dev.part_number, dev.description";

        final DateTime datePointer = DateTimeFormat.forPattern("yyyy").parseDateTime(year)
                .withMonthOfYear(month)
                .withDayOfMonth(1)
                .withTimeAtStartOfDay()
                .withZone(DateTimeZone.forID(TZID));
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("contractId", contractId);
        if (contractGroupId != null && contractGroupId > 0) {
            params.put("contractGroupId", contractGroupId);
        }
        params.put("leftDate", datePointer.toDate());
        params.put("rightDate", datePointer.dayOfMonth().withMaximumValue().plusHours(23).plusMinutes(59).plusSeconds(59).toDate());
        return namedJdbcTemplate.query(query, params,
                new RowMapper<Service>() {
            @Override
            public Service mapRow(ResultSet rs, int i) throws SQLException {
                Service rec = new Service(
                        datePointer,
                        rs.getString("code"),
                        rs.getLong("contract_id"),
                        (rs.getLong("contract_group_id") == 0 ? null : rs.getLong("contract_group_id")),
                        null, // contractUpdateId
                        rs.getLong("service_id"),
                        rs.getString("osp_id"),
                        null, // version
                        rs.getString("name"),
                        rs.getBigDecimal("onetime"),
                        rs.getBigDecimal("revenue"),
                        rs.getInt("quantity"),
                        rs.getDate("start_date"),
                        rs.getDate("end_date"),
                        (rs.getLong("devid") == 0 ? null : rs.getLong("devid")),
                        null, // for a rollup, we don't want to differentiate ind device instances
                        rs.getString("part_number"),
                        rs.getString("ddescr"),
                        rs.getInt("dcount"),
                        ((rs.getString("status") != null && rs.getString("status").contains("pending")) ? true : false));

                if (rs.getLong("cu_id") > 0) {
                    rec.addContractUpdate(new ContractUpdate(
                            rs.getLong("cu_id"),
                            rs.getLong("contract_id"),
                            rs.getString("cu_alt_id"),
                            rs.getString("cu_job_number"),
                            rs.getString("cu_ticket_number"),
                            rs.getString("cu_note"),
                            rs.getDate("cu_signed_date"),
                            rs.getDate("cu_effective_date"),
                            rs.getBigDecimal("cu_onetime_price"),
                            rs.getBigDecimal("cu_recurring_price"),
                            rs.getDate("cu_updated"),
                            rs.getString("cu_updated_by"),
                            rs.getString("cu_file_path")));
                }
                return rec;
            }
        });
    }

    @Override
    public List<Service> serviceRevenueRollup(Long contractId, Long contractGroupId, Service.Status status) {
        String query = "select csvc.service_id, csvc.contract_id,";
        if (contractGroupId != null && contractGroupId > 0) {
            query += " csvc.contract_group_id,";
        } else {
            query += " 0 as contract_group_id,"; // allows us to still group by and use one object mapper
        }
        query += " svc.code, svc.osp_id, svc.name, dev.id devid, dev.part_number, dev.description ddescr,"
                + " sum(csd.unit_count) dcount, sum(csvc.quantity) quantity, csvc.start_date,"
                + " csvc.end_date, csvc.status, GROUP_CONCAT(distinct csvc.status SEPARATOR ', ') as status, sum(csvc.quantity * csvc.onetime_revenue) onetime,"
                + " sum(csvc.quantity * csvc.recurring_revenue) revenue,"
                + " cu.id cu_id, cu.alt_id cu_alt_id, cu.job_number cu_job_number, cu.ticket_number cu_ticket_number, cu.note cu_note,"
                + " cu.signed_date cu_signed_date, cu.effective_date cu_effective_date, cu.onetime_price cu_onetime_price, cu.recurring_price cu_recurring_price, cu.updated cu_updated, cu.updated_by cu_updated_by, cu.file_path cu_file_path"
                + " from service svc"
                + " left join contract_service csvc on csvc.service_id = svc.id"
                + " left outer join contract_service_device csd on csd.contract_service_id = csvc.id"
                + " left outer join device dev on dev.id = csd.device_id"
                + " left outer join contract_update_contract_service cucs on cucs.contract_service_id = csvc.id"
                + " left outer join contract_update cu on cu.id = cucs.contract_update_id"
                + " where csvc.contract_id = :contractId and csvc.hidden = false";
        if (contractGroupId != null && contractGroupId > 0) {
            query += " and csvc.contract_group_id = :contractGroupId";
        }
        if (status != null) {
            query += " and csvc.status = :status";
        }
        query += " group by csvc.service_id, csvc.contract_id,";
        if (contractGroupId != null && contractGroupId > 0) {
            query += " csvc.contract_group_id,";
        }
        query += " csvc.start_date, csvc.end_date, dev.part_number, dev.description, cu.id"
                + " order by svc.name, csvc.start_date, dev.part_number, dev.description";

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("contractId", contractId);
        if (contractGroupId != null && contractGroupId > 0) {
            params.put("contractGroupId", contractGroupId);
        }
        if (status != null) {
            params.put("status", status.getDescription());
        }
        return namedJdbcTemplate.query(query, params,
                new RowMapper<Service>() {
            @Override
            public Service mapRow(ResultSet rs, int i) throws SQLException {
                Service rec = new Service(
                        null,
                        rs.getString("code"),
                        rs.getLong("contract_id"),
                        (rs.getLong("contract_group_id") == 0 ? null : rs.getLong("contract_group_id")),
                        null, // contractUpdateId
                        rs.getLong("service_id"),
                        rs.getString("osp_id"),
                        null, // version
                        rs.getString("name"),
                        rs.getBigDecimal("onetime"),
                        rs.getBigDecimal("revenue"),
                        rs.getInt("quantity"),
                        rs.getDate("start_date"),
                        rs.getDate("end_date"),
                        (rs.getLong("devid") == 0 ? null : rs.getLong("devid")),
                        null, // for a rollup, we don't want to differentiate ind device instances
                        rs.getString("part_number"),
                        rs.getString("ddescr"),
                        rs.getInt("dcount"),
                        ((rs.getString("status") != null && rs.getString("status").contains("pending")) ? true : false));

                if (rs.getLong("cu_id") > 0) {
                    rec.addContractUpdate(new ContractUpdate(
                            rs.getLong("cu_id"),
                            rs.getLong("contract_id"),
                            rs.getString("cu_alt_id"),
                            rs.getString("cu_job_number"),
                            rs.getString("cu_ticket_number"),
                            rs.getString("cu_note"),
                            rs.getDate("cu_signed_date"),
                            rs.getDate("cu_effective_date"),
                            rs.getBigDecimal("cu_onetime_price"),
                            rs.getBigDecimal("cu_recurring_price"),
                            rs.getDate("cu_updated"),
                            rs.getString("cu_updated_by"),
                            rs.getString("cu_file_path")));
                }
                return rec;
            }
        });
    }

    @Override
    public ServiceDetailRecordWrapper serviceDetailsForFilter(Long ospId, Long deviceId, final Date svcDate) {
        if(svcDate == null) {
            throw new IllegalArgumentException("Service Date is a required field");
        }
        if(ospId == null && deviceId == null) {
            throw new IllegalArgumentException("OSP ID or Device ID is a required field");
        }
        if ((ospId != null && ospId > 0) && (deviceId != null && deviceId > 0)) {
            throw new IllegalArgumentException("OSP ID and Device ID cannot both be used as input");
        }
        Service service = null;
        Device device = null;
        if (ospId != null && ospId > 0) {
            service = dataService.findAnyServiceByOspId(ospId);
            if (service == null) {
                throw new IllegalArgumentException("Error: Service not found for OSP ID!");
            }
        } else if (deviceId != null && deviceId > 0) {
            try {
                device = dataService.device(deviceId);
            } catch (ServiceException se) {
                log.warn("issue looking up device: {}", se.getMessage());
            }
            if (device == null) {
                throw new IllegalArgumentException("Error: Device not found for Device ID!");
            }
        }
        DateTime leftDate = new DateTime(svcDate)
                .withDayOfMonth(1)
                .withTimeAtStartOfDay()
                .withZone(DateTimeZone.forID(TZID));
        DateTime rightDate = leftDate
                .dayOfMonth()
                .withMaximumValue()
                .plusHours(23).plusMinutes(59).plusSeconds(59);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("leftDate", leftDate.toDate());
        params.put("rightDate", rightDate.toDate());
        String query = "select cst.name as 'Customer', cst.id as 'customer_id', ctr.id as 'contract_id', ctr.job_number, ctr.name as 'Contract', ctr.emgr, svc.osp_id, svc.name as 'Service',"
                + " d.description as 'Device', d.id as 'device_id', d.part_number, sum(csvc.quantity) quantity, sum(csvc.quantity * csvc.onetime_revenue) onetime,"
                + " sum(csvc.quantity * csvc.recurring_revenue) revenue, csvc.start_date, csvc.end_date, sum(csd.unit_count) as 'unit_count'"
                + " from contract_service csvc"
                + " inner join service svc on svc.id = csvc.service_id"
                + " inner join contract ctr on ctr.id = csvc.contract_id"
                + " inner join customer cst on cst.id = ctr.customer_id"
                + " inner join contract_service_device csd on csd.contract_service_id = csvc.id"
                + " inner join device d on d.id = csd.device_id"
                + " where (csvc.start_date <= :leftDate or csvc.start_date between :leftDate and :rightDate)"
                + " and (csvc.end_date is null or csvc.end_date between :leftDate and :rightDate or csvc.end_date >= :rightDate)"
                + " and (csvc.status = 'active' or csvc.status = 'donotbill')";
        if (service != null) {
            query += " and svc.osp_id = :ospId";
            params.put("ospId", ospId);
        } else if (device != null) {
            query += " and d.id = :deviceId";
            params.put("deviceId", deviceId);
        }
        query += " and csvc.hidden = false"
                + " group by cst.name, cst.id, ctr.job_number, ctr.name, svc.osp_id, svc.name, d.description, d.part_number, d.id, csvc.start_date, csvc.end_date"
                + " order by cst.name, csvc.start_date, csvc.end_date";
        
        ServiceDetailRecordWrapper wrapper = new ServiceDetailRecordWrapper();
        wrapper.setOspId(ospId);
        wrapper.setDeviceId(deviceId);
        wrapper.setServiceDate(new DateTime(svcDate));
        if (service != null) {
            wrapper.setServiceName(service.getName());
        }
        if (device != null) {
            wrapper.setDevicePartNumber(device.getPartNumber());
        }
        List<ServiceDetailRecord> records = namedJdbcTemplate.query(query, params, new RowMapper<ServiceDetailRecord>() {
            @Override
            public ServiceDetailRecord mapRow(ResultSet rs, int i) throws SQLException {
                return new ServiceDetailRecord(
                        new DateTime(svcDate),
                        rs.getString("Customer"),
                        rs.getLong("customer_id"),
                        rs.getLong("contract_id"),
                        rs.getString("job_number"),
                        rs.getString("Contract"),
                        rs.getString("emgr"),
                        rs.getString("Service"),
                        rs.getLong("osp_id"),
                        rs.getString("Device"),
                        rs.getString("part_number"),
                        rs.getLong("device_id"),
                        rs.getInt("quantity"),
                        rs.getInt("unit_count"),
                        rs.getBigDecimal("onetime"),
                        rs.getBigDecimal("revenue"),
                        new DateTime(rs.getDate("start_date")),
                        new DateTime(rs.getDate("end_date"))
                );
            }
        });
        
        for (ServiceDetailRecord record : records) {
        	contractDaoService.mapEngagementManagerForServiceContractDetail(record);
        }
        
        wrapper.setData(records);
        return wrapper;
    }
    
    @Override
    public List<Service> serviceRevenueRecordsForFilter(Long contractId, Long contractGroupId, Long serviceId, Long deviceId, final Date startDate, Date endDate, Service.Status status) {
        String query = "select csvc.id, csvc.service_id, csvc.parent_id, csvc.contract_id, csvc.contract_group_id, service.code, service.osp_id, service.name, service.version,"
                + " csvc.onetime_revenue, csvc.recurring_revenue, csvc.note, csvc.quantity, count(clit.lineitem_id) lineitems,"
                + " csvc.start_date, csvc.end_date, csvc.status, dev.id devid, csd.name dname, csd.unit_count dcount, csd.location_id dlocation, dev.part_number, dev.description ddescr, csvc.contract_service_subscription_id, css.subscription_type, csvc.microsoft_365_subscription_config_id "
                + " from contract_service csvc"
                + " left join service service on service.id = csvc.service_id"
                + " left outer join contract_lineitem clit on clit.contract_service_id = csvc.id"
                + " left outer join contract_service_device csd on csd.contract_service_id = csvc.id"
                + " left outer join device dev on dev.id = csd.device_id"
                + " left outer join contract_service_subscription css on css.id = csvc.contract_service_subscription_id"
                + " where csvc.contract_id = :contractId and csvc.hidden = false";
        if (contractGroupId != null && contractGroupId > 0) {
            query += " and csvc.contract_group_id = :contractGroupId";
        }
        if (status != null) {
            query += " and csvc.status = :status";
        }
        if (serviceId != null) {
        	query += " and csvc.service_id = :serviceId";
        }
        if (deviceId != null) {
            query += " and dev.id = :deviceId";
        }
        query += " and DATE_FORMAT(csvc.start_date,'%m%d%Y') = :startDate and DATE_FORMAT(csvc.end_date,'%m%d%Y') = :endDate"
                + " group by csvc.id, csvc.service_id, csvc.parent_id, csvc.contract_id,";
        if (contractGroupId != null && contractGroupId > 0) {
            query += " csvc.contract_group_id,";
        }
        query += " service.code, service.osp_id, service.name, service.version,"
                + " csvc.onetime_revenue, csvc.recurring_revenue, csvc.note, csvc.quantity, csvc.start_date, csvc.end_date,"
                + " dev.id, csd.name, csd.unit_count, dev.part_number, dev.description";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("contractId", contractId);
        if (contractGroupId != null && contractGroupId > 0) {
            params.put("contractGroupId", contractGroupId);
        }
        if (status != null) {
            params.put("status", status.getDescription());
        }
        if (serviceId != null) {
        	params.put("serviceId", serviceId);
        }
        if (deviceId != null) {
            params.put("deviceId", deviceId);
        }
        params.put("startDate", new SimpleDateFormat("MMddyyyy").format(startDate));
        params.put("endDate", new SimpleDateFormat("MMddyyyy").format(endDate));
        List<Service> contractServices = namedJdbcTemplate.query(query, params,
                new RowMapper<Service>() {
            @Override
            public Service mapRow(ResultSet rs, int i) throws SQLException {
                return new Service(
                        rs.getLong("id"),
                        rs.getString("code"),
                        (rs.getLong("parent_id") == 0 ? null : rs.getLong("parent_id")),
                        rs.getLong("contract_id"),
                        (rs.getLong("contract_group_id") == 0 ? null : rs.getLong("contract_group_id")),
                        null, // contractUpdateId
                        rs.getLong("service_id"),
                        rs.getString("osp_id"),
                        rs.getDouble("version"),
                        rs.getString("name"),
                        rs.getBigDecimal("onetime_revenue"),
                        rs.getBigDecimal("recurring_revenue"),
                        rs.getInt("quantity"),
                        rs.getString("note"),
                        rs.getInt("lineitems"),
                        rs.getDate("start_date"),
                        rs.getDate("end_date"),
                        (rs.getLong("devid") == 0 ? null : rs.getLong("devid")),
                        rs.getString("dname"),
                        rs.getString("part_number"),
                        rs.getString("ddescr"),
                        (rs.getInt("dcount") == 0 ? null : rs.getInt("dcount")),
                        Service.Status.valueOf(rs.getString("status")),
                        (rs.getLong("contract_service_subscription_id") == 0 ? null : rs.getLong("contract_service_subscription_id")),
                        rs.getString("subscription_type"),
                        (rs.getLong("microsoft_365_subscription_config_id") == 0 ? null : rs.getLong("microsoft_365_subscription_config_id")),
                        (rs.getInt("dlocation") == 0 ? null : rs.getInt("dlocation")));
            }
        });
        for (Service cs : contractServices) {
            // attach the contract service - contract updates
            cs.setContractUpdates(jdbcTemplate.query("select cu.id, cu.alt_id, cu.job_number, cu.ticket_number, cu.note,"
                    + " cu.contract_id, cu.signed_date, cu.effective_date, cu.onetime_price onetime_price, cu.recurring_price recurring_price, cu.updated, cu.updated_by, cu.file_path cu_file_path, cucs.note,"
                    + " cucs.operation"
                    + " from contract_update_contract_service cucs"
                    + " left join contract_service csvc on cucs.contract_service_id = csvc.id"
                    + " left join contract_update cu on cucs.contract_update_id = cu.id"
                    + " where csvc.id = ?"
                    + " order by cucs.created desc", new Object[]{cs.getId()}, new RowMapper<ContractUpdate>() {
                @Override
                public ContractUpdate mapRow(ResultSet rs, int i) throws SQLException {
                    return new ContractUpdate(
                            rs.getLong("id"),
                            rs.getLong("contract_id"),
                            rs.getString("alt_id"),
                            rs.getString("job_number"),
                            rs.getString("ticket_number"),
                            rs.getString("note"),
                            rs.getDate("signed_date"),
                            rs.getDate("effective_date"),
                            rs.getBigDecimal("onetime_price"),
                            rs.getBigDecimal("recurring_price"),
                            rs.getDate("updated"),
                            rs.getString("updated_by"),
                            rs.getString("cu_file_path"));
                }
            }));
            // attach possible related lineitems... note: these records are already a part of this
            // list, but now they can be nested under a parent as well for preferred processing
            List<Service> relatedLineItems = serviceRevenueRelatedRecordsForParent(cs.getId(), startDate, endDate);
            for(Service relatedLineItem: relatedLineItems) {
            	
            	List<Service> grandChildLineItems = serviceRevenueRelatedRecordsForParent(relatedLineItem.getId(), startDate, endDate);
//            	for(Service grandChildLineItem: grandChildLineItems) {
//            	}
            	relatedLineItem.setRelatedLineItems(grandChildLineItems);
            }
            cs.setRelatedLineItems(relatedLineItems);
        }
        return contractServices;
    }
    
    private List<Service> serviceRevenueRelatedRecordsForParent(Long parentContractServiceId, Date startDate, Date endDate) {
        String query = "select csvc.id, csvc.service_id, csvc.parent_id, csvc.contract_id, csvc.contract_group_id, service.code, service.osp_id, service.name, service.version,"
                + " csvc.onetime_revenue, csvc.recurring_revenue, csvc.note, csvc.quantity, count(clit.lineitem_id) lineitems,"
                + " csvc.start_date, csvc.end_date, csvc.status, dev.id devid, csd.name dname, csd.unit_count dcount, csd.location_id dlocation, dev.part_number, dev.description ddescr, csvc.contract_service_subscription_id, css.subscription_type, csvc.microsoft_365_subscription_config_id "
                + " from contract_service csvc"
                + " left join service service on service.id = csvc.service_id"
                + " left outer join contract_lineitem clit on clit.contract_service_id = csvc.id"
                + " left outer join contract_service_device csd on csd.contract_service_id = csvc.id"
                + " left outer join device dev on dev.id = csd.device_id"
                + " left outer join contract_service_subscription css on css.id = csvc.contract_service_subscription_id"
                + " where csvc.parent_id = :parentContractServiceId and csvc.hidden = false";
        
        if(startDate != null && endDate != null) {
        	query += " and (csvc.start_date <= :leftDate or csvc.start_date between :leftDate and :rightDate)"
                    + " and (csvc.end_date is null or csvc.end_date between :leftDate and :rightDate"
                    + " or csvc.end_date >= :rightDate)";
        }
        
        query += " group by csvc.id, csvc.service_id, csvc.parent_id, csvc.contract_id,"
                + " service.code, service.osp_id, service.name, service.version,"
                + " csvc.onetime_revenue, csvc.recurring_revenue, csvc.note, csvc.quantity, csvc.start_date, csvc.end_date,"
                + " dev.id, csd.name, csd.unit_count, dev.part_number, dev.description";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("parentContractServiceId", parentContractServiceId);
        
        if(startDate != null && endDate != null) {
        	params.put("leftDate", startDate);
        	params.put("rightDate", endDate);
        }
        
        List<Service> contractServices = namedJdbcTemplate.query(query, params,
                new RowMapper<Service>() {
            @Override
            public Service mapRow(ResultSet rs, int i) throws SQLException {
                return new Service(
                        rs.getLong("id"),
                        rs.getString("code"),
                        (rs.getLong("parent_id") == 0 ? null : rs.getLong("parent_id")),
                        rs.getLong("contract_id"),
                        (rs.getLong("contract_group_id") == 0 ? null : rs.getLong("contract_group_id")),
                        null, // contractUpdateId
                        rs.getLong("service_id"),
                        rs.getString("osp_id"),
                        rs.getDouble("version"),
                        rs.getString("name"),
                        rs.getBigDecimal("onetime_revenue"),
                        rs.getBigDecimal("recurring_revenue"),
                        rs.getInt("quantity"),
                        rs.getString("note"),
                        rs.getInt("lineitems"),
                        rs.getDate("start_date"),
                        rs.getDate("end_date"),
                        (rs.getLong("devid") == 0 ? null : rs.getLong("devid")),
                        rs.getString("dname"),
                        rs.getString("part_number"),
                        rs.getString("ddescr"),
                        (rs.getInt("dcount") == 0 ? null : rs.getInt("dcount")),
                        Service.Status.valueOf(rs.getString("status")),
                        (rs.getLong("contract_service_subscription_id") == 0 ? null : rs.getLong("contract_service_subscription_id")),
                        rs.getString("subscription_type"),
                        (rs.getLong("microsoft_365_subscription_config_id") == 0 ? null : rs.getLong("microsoft_365_subscription_config_id")),
                        (rs.getInt("dlocation") == 0 ? null : rs.getInt("dlocation")));
            }
        });
        // attach the contract service - contract updates
        for (Service cs : contractServices) {
            cs.setContractUpdates(jdbcTemplate.query("select cu.id, cu.alt_id, cu.job_number, cu.ticket_number, cu.note,"
                    + " cu.contract_id, cu.signed_date, cu.effective_date, cu.onetime_price cu_onetime_price, cu.recurring_price cu_recurring_price, cu.updated, cu.updated_by, cu.file_path cu_file_path, cucs.note,"
                    + " cucs.operation"
                    + " from contract_update_contract_service cucs"
                    + " left join contract_service csvc on cucs.contract_service_id = csvc.id"
                    + " left join contract_update cu on cucs.contract_update_id = cu.id"
                    + " where csvc.id = ?"
                    + " order by cucs.created desc", new Object[]{cs.getId()}, new RowMapper<ContractUpdate>() {
                @Override
                public ContractUpdate mapRow(ResultSet rs, int i) throws SQLException {
                    return new ContractUpdate(
                            rs.getLong("id"),
                            rs.getLong("contract_id"),
                            rs.getString("alt_id"),
                            rs.getString("job_number"),
                            rs.getString("ticket_number"),
                            rs.getString("note"),
                            rs.getDate("signed_date"),
                            rs.getDate("effective_date"),
                            rs.getBigDecimal("cu_onetime_price"),
                            rs.getBigDecimal("cu_recurring_price"),
                            rs.getDate("updated"),
                            rs.getString("updated_by"),
                            rs.getString("cu_file_path"));
                }
            }));
        }
        return contractServices;
    }
    
    @Override
    public List<Service> serviceRevenueRecordsForDateRange(Long contractId, Date startDate, Date endDate, Service.Status status) {
        String query = "select csvc.id, csvc.service_id, csvc.parent_id, csvc.contract_id, csvc.contract_group_id, service.code, service.osp_id, service.name, service.version,"
                + " csvc.onetime_revenue, csvc.recurring_revenue, csvc.note, csvc.quantity,"
                + " csvc.start_date, csvc.end_date, csvc.status, dev.id devid, csd.name dname, csd.unit_count dcount, csd.location_id dlocation, dev.part_number, dev.description ddescr, csvc.contract_service_subscription_id, css.subscription_type, csvc.microsoft_365_subscription_config_id "
                + " from contract_service csvc"
                + " left join service service on service.id = csvc.service_id"
                + " left outer join contract_service_device csd on csd.contract_service_id = csvc.id"
                + " left outer join device dev on dev.id = csd.device_id"
                + " left outer join contract_service_subscription css on css.id = csvc.contract_service_subscription_id"
                + " where csvc.contract_id = :contractId";

        if (status != null) {
            query += " and csvc.status = :status";
        }
       
        query += " and (csvc.start_date <= :leftDate or csvc.start_date between :leftDate and :rightDate)" 
        		+ " and (csvc.end_date is null or csvc.end_date between :leftDate and :rightDate or csvc.end_date >= :rightDate)"
                + " group by csvc.id, csvc.service_id, csvc.parent_id, csvc.contract_id,";
        query += " service.code, service.osp_id, service.name, service.version,"
                + " csvc.onetime_revenue, csvc.recurring_revenue, csvc.note, csvc.quantity, csvc.start_date, csvc.end_date,"
                + " dev.id, csd.name, csd.unit_count, dev.part_number, dev.description";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("contractId", contractId);
        
        if (status != null) {
            params.put("status", status.getDescription());
        }
        
        params.put("leftDate", startDate);
        params.put("rightDate", endDate);
        List<Service> contractServices = namedJdbcTemplate.query(query, params,
                new RowMapper<Service>() {
            @Override
            public Service mapRow(ResultSet rs, int i) throws SQLException {
                return new Service(
                        rs.getLong("id"),
                        rs.getString("code"),
                        (rs.getLong("parent_id") == 0 ? null : rs.getLong("parent_id")),
                        rs.getLong("contract_id"),
                        (rs.getLong("contract_group_id") == 0 ? null : rs.getLong("contract_group_id")),
                        null, // contractUpdateId
                        rs.getLong("service_id"),
                        rs.getString("osp_id"),
                        rs.getDouble("version"),
                        rs.getString("name"),
                        rs.getBigDecimal("onetime_revenue"),
                        rs.getBigDecimal("recurring_revenue"),
                        rs.getInt("quantity"),
                        rs.getString("note"),
                        null,
                        rs.getDate("start_date"),
                        rs.getDate("end_date"),
                        (rs.getLong("devid") == 0 ? null : rs.getLong("devid")),
                        rs.getString("dname"),
                        rs.getString("part_number"),
                        rs.getString("ddescr"),
                        (rs.getInt("dcount") == 0 ? null : rs.getInt("dcount")),
                        Service.Status.valueOf(rs.getString("status")),
                        (rs.getLong("contract_service_subscription_id") == 0 ? null : rs.getLong("contract_service_subscription_id")),
                        rs.getString("subscription_type"),
                        (rs.getLong("microsoft_365_subscription_config_id") == 0 ? null : rs.getLong("microsoft_365_subscription_config_id")),
                        (rs.getInt("dlocation") == 0 ? null : rs.getInt("dlocation")));
            }
        });
        
        for (Service cs : contractServices) {
            // attach the contract service - contract updates
            cs.setContractUpdates(jdbcTemplate.query("select cu.id, cu.alt_id, cu.job_number, cu.ticket_number, cu.note,"
                    + " cu.contract_id, cu.signed_date, cu.effective_date, cu.onetime_price cu_onetime_price, cu.recurring_price cu_recurring_price, cu.updated, cu.updated_by, cu.file_path cu_file_path, cucs.note,"
                    + " cucs.operation"
                    + " from contract_update_contract_service cucs"
                    + " left join contract_service csvc on cucs.contract_service_id = csvc.id"
                    + " left join contract_update cu on cucs.contract_update_id = cu.id"
                    + " where csvc.id = ?"
                    + " order by cucs.created desc", new Object[]{cs.getId()}, new RowMapper<ContractUpdate>() {
                @Override
                public ContractUpdate mapRow(ResultSet rs, int i) throws SQLException {
                    return new ContractUpdate(
                            rs.getLong("id"),
                            rs.getLong("contract_id"),
                            rs.getString("alt_id"),
                            rs.getString("job_number"),
                            rs.getString("ticket_number"),
                            rs.getString("note"),
                            rs.getDate("signed_date"),
                            rs.getDate("effective_date"),
                            rs.getBigDecimal("cu_onetime_price"),
                            rs.getBigDecimal("cu_recurring_price"),
                            rs.getDate("updated"),
                            rs.getString("updated_by"),
                            rs.getString("cu_file_path"));
                }
            }));
            // attach possible related lineitems... note: these records are already a part of this
            // list, but now they can be nested under a parent as well for preferred processing
            List<Service> relatedLineItems = serviceRevenueRelatedRecordsForParent(cs.getId(), startDate, endDate);
            for(Service relatedLineItem: relatedLineItems) {
            	
            	List<Service> grandChildLineItems = serviceRevenueRelatedRecordsForParent(relatedLineItem.getId(), startDate, endDate);
//            	for(Service grandChildLineItem: grandChildLineItems) {
//            	}
            	relatedLineItem.setRelatedLineItems(grandChildLineItems);
            }
            cs.setRelatedLineItems(relatedLineItems);
        }
        return contractServices;
    }

    @Override
    public List<ContractAdjustment> contractAdjustmentRecordsForFilter(Long contractId, String type, Date startDate, Date endDate, Service.Status status) {
        String query = "select adj.id, adj.contract_id, adj.contract_update_id, adj.contract_group_id, adj.adjustment, adj.adjustment_type,"
                + " adj.start_date, adj.end_date, adj.note, adj.status adj_status"
                + " from contract_adjustment adj"
                + " where adj.contract_id = :contractId"
                + " and adj.adjustment_type = :type"
                + " and adj.status = :status"
                + " and DATE_FORMAT(adj.start_date,'%m%d%Y') = :startDate and DATE_FORMAT(adj.end_date,'%m%d%Y') = :endDate"
                + " order by adj.start_date desc";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("contractId", contractId);
        params.put("type", type);
        params.put("status", status.name());
        params.put("startDate", new SimpleDateFormat("MMddyyyy").format(startDate));
        params.put("endDate", new SimpleDateFormat("MMddyyyy").format(endDate));
        return namedJdbcTemplate.query(query, params,
                new RowMapper<ContractAdjustment>() {
            @Override
            public ContractAdjustment mapRow(ResultSet rs, int i) throws SQLException {
                return new ContractAdjustment(
                        rs.getLong("id"),
                        rs.getLong("contract_id"),
                        rs.getLong("contract_update_id"), // contract update id not included
                        (rs.getLong("contract_group_id") == 0 ? null : rs.getLong("contract_group_id")),
                        rs.getBigDecimal("adjustment"), // summed...
                        rs.getString("adjustment_type"), // grouped by...
                        rs.getString("note"),
                        null,
                        rs.getDate("start_date"),
                        rs.getDate("end_date"),
                        Service.Status.valueOf(rs.getString("adj_status")),
                        null, // created not included
                        null, // created_by not included
                        null, // updated not included
                        null); // updated_by not included
            }
        });
    }

    @SuppressWarnings("unused")
	private List<LineItemMonthlyRevenue> lineItemRevenueForMonthOf(Long contractId, Integer month, String year) {
        String query = "select clit.id, clit.contract_id, clit.lineitem_id, lir.name lineitem,"
                + " clit.onetime_revenue onetime, clit.quantity quantity, clit.recurring_revenue recurring,"
                + " clit.start_date, clit.end_date"
                + " from contract_lineitem clit"
                + " left join lineitem lir on lir.id = clit.lineitem_id"
                + " where clit.contract_id = :contractId and clit.service_id is null"
                + " and (clit.start_date <= :leftDate or clit.start_date between :leftDate and :rightDate)"
                + " and (clit.end_date is null or clit.end_date between :leftDate and :rightDate"
                + " or clit.end_date >= :rightDate)";
        final DateTime datePointer = DateTimeFormat.forPattern("yyyy").parseDateTime(year)
                .withMonthOfYear(month)
                .withDayOfMonth(1)
                .withTimeAtStartOfDay()
                .withZone(DateTimeZone.forID(TZID));
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("contractId", contractId);
        params.put("leftDate", datePointer.toDate());
        params.put("rightDate", datePointer.dayOfMonth().withMaximumValue().plusHours(23).plusMinutes(59).plusSeconds(59).toDate());
        return namedJdbcTemplate.query(query, params,
                new RowMapper<LineItemMonthlyRevenue>() {
            @Override
            public LineItemMonthlyRevenue mapRow(ResultSet rs, int i) throws SQLException {
                return new LineItemMonthlyRevenue(
                        rs.getDate("start_date"),
                        rs.getDate("end_date"),
                        datePointer.toDate(),
                        rs.getBigDecimal("recurring"),
                        rs.getLong("id"),
                        rs.getLong("contract_id"),
                        null,
                        null,
                        rs.getLong("lineitem_id"),
                        rs.getString("lineitem"),
                        rs.getBigDecimal("onetime"),
                        rs.getInt("quantity"));
            }
        });
    }

    @SuppressWarnings("unused")
	private List<LineItemMonthlyRevenue> serviceLineItemRevenueForMonthOf(Long contractServiceId, Integer month, String year) {
        if (contractServiceId == null) {
            throw new IllegalArgumentException("A contractServiceId MUST be specified");
        }
        String query = "select clit.id, clit.contract_id, clit.contract_service_id, , clit.service_id, clit.lineitem_id,"
                + " lir.name lineitem, clit.onetime_revenue onetime,"
                + " clit.quantity quantity, clit.recurring_revenue recurring, clit.start_date, clit.end_date"
                + " from contract_lineitem clit"
                + " left join lineitem lir on lir.id = clit.lineitem_id"
                + " where clit.contract_service_id = :contractServiceId"
                + " and (clit.start_date <= :leftDate or clit.start_date between :leftDate and :rightDate)"
                + " and (clit.end_date is null or clit.end_date between :leftDate and :rightDate"
                + " or clit.end_date >= :rightDate)";
        final DateTime datePointer = DateTimeFormat.forPattern("yyyy").parseDateTime(year)
                .withMonthOfYear(month)
                .withDayOfMonth(1)
                .withTimeAtStartOfDay()
                .withZone(DateTimeZone.forID(TZID));
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("contractId", contractServiceId);
        params.put("leftDate", datePointer.toDate());
        params.put("rightDate", datePointer.dayOfMonth().withMaximumValue().plusHours(23).plusMinutes(59).plusSeconds(59).toDate());
        return namedJdbcTemplate.query(query, params,
                new RowMapper<LineItemMonthlyRevenue>() {
            @Override
            public LineItemMonthlyRevenue mapRow(ResultSet rs, int i) throws SQLException {
                return new LineItemMonthlyRevenue(
                        rs.getDate("start_date"),
                        rs.getDate("end_date"),
                        datePointer.toDate(),
                        rs.getBigDecimal("recurring"),
                        rs.getLong("id"),
                        rs.getLong("contract_id"),
                        rs.getLong("contract_service_id"),
                        rs.getLong("service_id"),
                        rs.getLong("lineitem_id"),
                        rs.getString("lineitem"),
                        rs.getBigDecimal("onetime"),
                        rs.getInt("quantity"));
            }
        });
    }

    @Override
    public List<Contract> contracts(Long customerId, Boolean archived) {
        String query = "select contract.customer_id, contract.id, contract.alt_id, contract.job_number, contract.name,"
                + " contract.emgr, contract.sda, count(csvc.service_id) services, contract.signed_date, contract.service_start_date, contract.start_date, contract.end_date, contract.archived,"
        		+ " contract.sn_sys_id, contract.file_path, contract.renewal_status, contract.renewal_change, contract.renewal_notes"
                + " from contract contract"
                + " left outer join contract_service csvc on csvc.contract_id = contract.id";
        if(archived != null || customerId != null) {
        	query += " where";
        }
        if(customerId != null) {
            query += " contract.customer_id = :customerId";
        }
        if (archived != null) {
            query += " and contract.archived = :archived";
        }
        query += " group by contract.customer_id, contract.id, contract.alt_id, contract.job_number, contract.name,"
                + " contract.emgr, contract.signed_date, contract.start_date, contract.end_date, contract.archived";
        
        Map<String, Object> params = new HashMap<String, Object>();
        if(customerId != null) {
        	params.put("customerId", customerId);
        }
        if (archived != null) {
            params.put("archived", archived);
        }
        List<Contract> contracts = namedJdbcTemplate.query(query, params,
                new RowMapper<Contract>() {
            @Override
            public Contract mapRow(ResultSet rs, int i) throws SQLException {
                return new Contract(
                        rs.getLong("customer_id"),
                        rs.getLong("id"),
                        rs.getString("alt_id"),
                        rs.getString("job_number"),
                        rs.getString("name"),
                        rs.getString("emgr"),
                        rs.getString("sda"),
                        rs.getInt("services"),
                        rs.getDate("signed_date"),
                        rs.getDate("service_start_date"),
                        rs.getDate("start_date"),
                        rs.getDate("end_date"),
                        rs.getBoolean("archived"),
                        rs.getString("sn_sys_id"),
                        rs.getString("file_path"),
                        ((rs.getString("renewal_status") == null) ? null : Contract.RenewalStatus.valueOf(rs.getString("renewal_status"))),
                        rs.getBigDecimal("renewal_change"),
                        rs.getString("renewal_notes"));
            }
        });
        
        for(Contract contract: contracts) {
        	log.info("mapping personnel");
        	contractDaoService.mapPersonnelToContract(contract);
        }
        
        return contracts;
    }
    
    @Override
    public List<Contract> contractsForRenewal(Long customerId, Date renewalDate, List<Contract.RenewalStatus> inStatus, Boolean includeRevenue) {
        List<Contract> contracts = contractsForRenewalReport(customerId, renewalDate, inStatus);

        if(includeRevenue) {
	        for (Contract contract : contracts) {
	            Long contractId = contract.getId();
	            List<ContractAdjustment> adjustments = contractAdjustments(contractId);
	            List<Service> services = services(contractId, null);  
	            contract.setMonthTotalOnetimeRevenue(getCurrentMonthTotalOnetimeRevenue(services, adjustments));
	            contract.setMonthTotalRecurringRevenue(getCurrentMonthTotalRecurringRevenue(services, adjustments));
	        }
        }

        return contracts;
    }
    
    private BigDecimal getCurrentMonthTotalRecurringRevenue(List<Service> contractServices, List<ContractAdjustment> contractAdjustments) {
		BigDecimal currentTotalRecurring = new BigDecimal(0);
		DateTime currentMonthStart = new DateTime().withDayOfMonth(1).withTimeAtStartOfDay().withZone(DateTimeZone.forID(TZID));
		DateTime currentMonthEnd = new DateTime().dayOfMonth().withMaximumValue().withTimeAtStartOfDay().plusHours(23).plusMinutes(59).plusSeconds(59).withZone(DateTimeZone.forID(TZID));
		
		boolean svcNotActive = false;
		for(Service service : contractServices) {
		    svcNotActive = ((service.getStatus()!=null) && (!service.getStatus().equals(Service.Status.active)));
		    if (!svcNotActive) {
                DateTime serviceStartDate = new DateTime(service.getStartDate());
                DateTime serviceEndDate = new DateTime(service.getEndDate());
                
                service.forMonth(currentMonthStart);
                if((serviceStartDate.isBefore(currentMonthStart) || serviceStartDate.isEqual(currentMonthStart) || (serviceStartDate.isAfter(currentMonthStart) && serviceStartDate.isBefore(currentMonthEnd))) &&
                    (serviceEndDate.isAfter(currentMonthStart) && (serviceEndDate.isBefore(currentMonthEnd) || serviceEndDate.isEqual(currentMonthEnd)) || serviceEndDate.isAfter(currentMonthEnd) || serviceEndDate.isEqual(currentMonthStart))) {
                    currentTotalRecurring = currentTotalRecurring.add(service.getRecurringRevenue());
                }
		    }
		}
		
		boolean adjNotActive = false;
		for(ContractAdjustment adjustment : contractAdjustments) {
			adjNotActive = ((adjustment.getStatus()!=null) && (!adjustment.getStatus().equals(Service.Status.active)));
			if(!adjNotActive) {
				DateTime adjustmentStartDate = new DateTime(adjustment.getStartDate());
				DateTime adjustmentEndDate = new DateTime(adjustment.getEndDate());
				
				adjustment.forMonth(currentMonthStart);
				if((adjustmentStartDate.isBefore(currentMonthStart) || adjustmentStartDate.isEqual(currentMonthStart) || (adjustmentStartDate.isAfter(currentMonthStart) && adjustmentStartDate.isBefore(currentMonthEnd))) &&
					(adjustmentEndDate.isAfter(currentMonthStart) && (adjustmentEndDate.isBefore(currentMonthEnd) || adjustmentEndDate.isEqual(currentMonthEnd)) || adjustmentEndDate.isAfter(currentMonthEnd))) {
					if(AdjustmentType.recurring.equals(AdjustmentType.valueOf(adjustment.getAdjustmentType()))) {
						currentTotalRecurring = currentTotalRecurring.add(adjustment.getAdjustment());
					}
				}
			}
		}
		
		return currentTotalRecurring;
	}

	private BigDecimal getCurrentMonthTotalOnetimeRevenue(List<Service> contractServices, List<ContractAdjustment> contractAdjustments) {
		BigDecimal currentTotalOnetime = new BigDecimal(0);
		DateTime currentMonthStart = new DateTime().withDayOfMonth(1).withTimeAtStartOfDay().withZone(DateTimeZone.forID(TZID));
		DateTime currentMonthEnd = new DateTime().dayOfMonth().withMaximumValue().withTimeAtStartOfDay().plusHours(23).plusMinutes(59).plusSeconds(59).withZone(DateTimeZone.forID(TZID));
		
		boolean svcNotActive = false;
		for(Service service : contractServices) {
			svcNotActive = ((service.getStatus()!=null) && (!service.getStatus().equals(Service.Status.active)));
		    if (!svcNotActive) {
				DateTime serviceStartDate = new DateTime(service.getStartDate());
				
				if(serviceStartDate.equals(currentMonthStart) || (serviceStartDate.isAfter(currentMonthStart) && serviceStartDate.isBefore(currentMonthEnd))) {
					currentTotalOnetime = currentTotalOnetime.add(service.getOnetimeRevenue());
				}
		    }
		}
		
		boolean adjNotActive = false;
		for(ContractAdjustment adjustment : contractAdjustments) {
			adjNotActive = ((adjustment.getStatus()!=null) && (!adjustment.getStatus().equals(Service.Status.active)));
			if(!adjNotActive) {
				DateTime adjustmentStartDate = new DateTime(adjustment.getStartDate());
				
				if(adjustmentStartDate.equals(currentMonthStart) || (adjustmentStartDate.isAfter(currentMonthStart) && adjustmentStartDate.isBefore(currentMonthEnd))) {
					if(AdjustmentType.onetime.equals(AdjustmentType.valueOf(adjustment.getAdjustmentType()))) {
						currentTotalOnetime = currentTotalOnetime.add(adjustment.getAdjustment());
					}
				}
			}
		}
		
		return currentTotalOnetime;
	}
    
    
    private List<Contract> contractsForRenewalReport(Long customerId, Date renewalDate, List<Contract.RenewalStatus> inStatus) {
        String query = "select contract.customer_id, contract.id, contract.alt_id, contract.job_number, contract.name,"
                + " contract.emgr, contract.sda, contract.signed_date, contract.service_start_date, contract.start_date, contract.end_date, contract.archived,"
        		+ " contract.sn_sys_id, contract.file_path, contract.renewal_status, contract.renewal_change, contract.renewal_notes, cu.name customer_name"
                + " from contract contract"
                + " inner join customer cu on cu.id = contract.customer_id"
        		+ " where contract.end_date between :now and :renewalDate and contract.archived = false";
        if(customerId != null) {
            query += " and contract.customer_id = :customerId";
        }
        
        if(inStatus != null && !inStatus.isEmpty()) {
            query += " and contract.renewal_status in (:statuses)";
        }
        
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("now", new Date());
        params.addValue("renewalDate", renewalDate);
        if(customerId != null) {
        	params.addValue("customerId", customerId);
        }
        if (inStatus != null && !inStatus.isEmpty()) {
        	log.info("setting statuses");
        	Set<String> statuses = new HashSet<String>();
        	for(Contract.RenewalStatus status: inStatus) {
        		statuses.add(status.name());
        	}
        	params.addValue("statuses", statuses);
        }
        List<Contract> contracts = namedJdbcTemplate.query(query, params,
                new RowMapper<Contract>() {
            @Override
            public Contract mapRow(ResultSet rs, int i) throws SQLException {
                return new Contract(
                		rs.getString("customer_name"),
                        rs.getLong("customer_id"),
                        rs.getLong("id"),
                        rs.getString("alt_id"),
                        rs.getString("job_number"),
                        rs.getString("name"),
                        rs.getString("emgr"),
                        rs.getString("sda"),
                        0,
                        rs.getDate("signed_date"),
                        rs.getDate("service_start_date"),
                        rs.getDate("start_date"),
                        rs.getDate("end_date"),
                        rs.getBoolean("archived"),
                        rs.getString("sn_sys_id"),
                        rs.getString("file_path"),
                        ((rs.getString("renewal_status") == null) ? null : Contract.RenewalStatus.valueOf(rs.getString("renewal_status"))),
                        rs.getBigDecimal("renewal_change"),
                        rs.getString("renewal_notes"));
            }
        });
        
        for(Contract contract: contracts) {
        	contractDaoService.mapPersonnelToContract(contract);
        }
        
        return contracts;
    }
    
    @Override
    public List<Contract> findContractsForForecastReport(Long customerId, Date startDate, Date endDate) {
        String query = "select contract.customer_id, contract.id, contract.alt_id, contract.job_number, contract.name,"
                + " contract.emgr, contract.sda, contract.signed_date, contract.service_start_date, contract.start_date, contract.end_date, contract.archived,"
        		+ " contract.sn_sys_id, contract.file_path, contract.renewal_status, contract.renewal_change, contract.renewal_notes, cu.name customer_name"
                + " from contract contract"
                + " inner join customer cu on cu.id = contract.customer_id"
        		+ " where contract.end_date between :startDate and :endDate and contract.archived = false"
                + " and contract.renewal_status in (:statuses)";
        if(customerId != null) {
            query += " and contract.customer_id = :customerId";
        }
        
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("startDate", startDate);
        params.addValue("endDate", endDate);
        if(customerId != null) {
        	params.addValue("customerId", customerId);
        }
        
        	Set<String> statuses = new HashSet<String>();
        	statuses.add(Contract.RenewalStatus.likelyRenew.name());
        	statuses.add(Contract.RenewalStatus.renewing.name());
        	params.addValue("statuses", statuses);
        List<Contract> contracts = namedJdbcTemplate.query(query, params,
                new RowMapper<Contract>() {
            @Override
            public Contract mapRow(ResultSet rs, int i) throws SQLException {
                return new Contract(
                		rs.getString("customer_name"),
                        rs.getLong("customer_id"),
                        rs.getLong("id"),
                        rs.getString("alt_id"),
                        rs.getString("job_number"),
                        rs.getString("name"),
                        rs.getString("emgr"),
                        rs.getString("sda"),
                        0,
                        rs.getDate("signed_date"),
                        rs.getDate("service_start_date"),
                        rs.getDate("start_date"),
                        rs.getDate("end_date"),
                        rs.getBoolean("archived"),
                        rs.getString("sn_sys_id"),
                        rs.getString("file_path"),
                        ((rs.getString("renewal_status") == null) ? null : Contract.RenewalStatus.valueOf(rs.getString("renewal_status"))),
                        rs.getBigDecimal("renewal_change"),
                        rs.getString("renewal_notes"));
            }
        });

        for(Contract contract: contracts) {
        	contractDaoService.mapPersonnelToContract(contract);
        }

        return contracts;
    }
    
    @Override
    public List<Contract> fullContracts(Long customerId, Boolean archived) {
        List<Contract> contracts = contracts(customerId, archived);

        for (Contract contract : contracts) {
//            FullContract rep = new FullContract(contract);
            Long contractId = contract.getId();
            List<ContractAdjustment> adjustments = contractAdjustments(contractId);
            List<Service> services = services(contractId, null);  
            contract.setMonthTotalOnetimeRevenue(getCurrentMonthTotalOnetimeRevenue(services, adjustments));
            contract.setMonthTotalRecurringRevenue(getCurrentMonthTotalRecurringRevenue(services, adjustments));
        }

        return contracts;
    }

    /*
    @Override
    public List<FullContract> fullContracts(Long customerId, Boolean archived) {
        List<FullContract> fullContracts = new ArrayList<FullContract>();
        List<Contract> contracts = contracts(customerId, archived);

        for (Contract contract : contracts) {
            FullContract rep = new FullContract(contract);
            Long contractId = contract.getId();
            rep.setContractAdjustments(contractAdjustments(contractId));
            rep.setContractServices(services(contractId, null));
            fullContracts.add(rep);
        }

        return fullContracts;
    }*/

    @Override
    public List<ContractUpdate> contractUpdates(Long contractId) {
        String query = "select * from contract_update where contract_id = :contractId";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("contractId", contractId);
        return namedJdbcTemplate.query(query, params,
                new RowMapper<ContractUpdate>() {
            @Override
            public ContractUpdate mapRow(ResultSet rs, int i) throws SQLException {
                return new ContractUpdate(
                        rs.getLong("id"),
                        rs.getLong("contract_id"),
                        rs.getString("alt_id"),
                        rs.getString("job_number"),
                        rs.getString("ticket_number"),
                        rs.getString("note"),
                        rs.getDate("signed_date"),
                        rs.getDate("effective_date"),
                        rs.getBigDecimal("onetime_price"),
                        rs.getBigDecimal("recurring_price"),
                        rs.getDate("updated"),
                        rs.getString("updated_by"),
                        rs.getString("file_path"));
            }
        });
    }

    @Override
    public List<ContractGroup> contractGroups(Long contractId) {
        String query = "select * from contract_group where contract_id = :contractId";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("contractId", contractId);
        return namedJdbcTemplate.query(query, params,
                new RowMapper<ContractGroup>() {
            @Override
            public ContractGroup mapRow(ResultSet rs, int i) throws SQLException {
                ContractGroup cg = new ContractGroup(
                        rs.getLong("id"),
                        rs.getLong("contract_id"),
                        rs.getString("name"),
                        rs.getString("description"));

                cg.setCreated(rs.getDate("created"));
                cg.setCreatedBy(rs.getString("created_by"));
                cg.setUpdated(rs.getDate("updated"));
                cg.setUpdatedBy(rs.getString("updated_by"));
                return cg;
            }
        });
    }

    @Override
    public List<ContractAdjustment> contractAdjustments(Long contractId) {
        String query = "select * from contract_adjustment where contract_id = :contractId";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("contractId", contractId);
        return namedJdbcTemplate.query(query, params,
                new RowMapper<ContractAdjustment>() {
            @Override
            public ContractAdjustment mapRow(ResultSet rs, int i) throws SQLException {
                return new ContractAdjustment(
                        rs.getLong("id"),
                        rs.getLong("contract_id"),
                        rs.getLong("contract_update_id"),
                        (rs.getLong("contract_group_id") == 0 ? null : rs.getLong("contract_group_id")),
                        rs.getBigDecimal("adjustment"),
                        rs.getString("adjustment_type"),
                        rs.getString("note"),
                        null, // month
                        rs.getDate("start_date"),
                        rs.getDate("end_date"),
                        Service.Status.valueOf(rs.getString("status")),
                        rs.getDate("created"),
                        rs.getString("created_by"),
                        rs.getDate("updated"),
                        rs.getString("updated_by"));
            }
        });
    }

    @Override
    public List<Service> services(Long contractId, Long contractGroupId) {
        String query = "select csvc.id, csvc.service_id, csvc.parent_id, csvc.contract_id, csvc.contract_group_id, service.code, service.osp_id, service.name, service.version,"
                + " csvc.onetime_revenue, csvc.recurring_revenue, csvc.note, csvc.quantity, count(clit.lineitem_id) lineitems,"
                + " csvc.start_date, csvc.end_date, csvc.status, dev.id devid, csd.name dname, csd.unit_count dcount, csd.location_id dlocation, dev.part_number, dev.description ddescr, csvc.contract_service_subscription_id, css.subscription_type, csvc.microsoft_365_subscription_config_id "
                + " from contract_service csvc"
                + " left join service service on service.id = csvc.service_id"
                + " left outer join contract_lineitem clit on clit.contract_service_id = csvc.id"
                + " left outer join contract_service_device csd on csd.contract_service_id = csvc.id"
                + " left outer join device dev on dev.id = csd.device_id"
                + " left outer join contract_service_subscription css on css.id = csvc.contract_service_subscription_id"
                + " where csvc.contract_id = :contractId";
        if (contractGroupId != null && contractGroupId > 0) {
            query += " and csvc.contract_group_id = :contractGroupId";
        }
        query += " group by csvc.id, csvc.service_id, csvc.parent_id, csvc.contract_id,";
        if (contractGroupId != null && contractGroupId > 0) {
            query += " csvc.contract_group_id,";
        }
        query += " service.code, service.osp_id, service.name, service.version,"
                + " csvc.onetime_revenue, csvc.recurring_revenue, csvc.note, csvc.quantity, csvc.start_date, csvc.end_date,"
                + " dev.id, csd.name, csd.unit_count, dev.part_number, dev.description";
//        DateTime now = new DateTime(); // return only contracts that are not ended
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("contractId", contractId);
        if (contractGroupId != null && contractGroupId > 0) {
            params.put("contractGroupId", contractGroupId);
        }
        List<Service> contractServices = namedJdbcTemplate.query(query, params,
                new RowMapper<Service>() {
            @Override
            public Service mapRow(ResultSet rs, int i) throws SQLException {
                return new Service(
                        rs.getLong("id"),
                        rs.getString("code"),
                        (rs.getLong("parent_id") == 0 ? null : rs.getLong("parent_id")),
                        rs.getLong("contract_id"),
                        (rs.getLong("contract_group_id") == 0 ? null : rs.getLong("contract_group_id")),
                        null, // contractUpdateId
                        rs.getLong("service_id"),
                        rs.getString("osp_id"),
                        rs.getDouble("version"),
                        rs.getString("name"),
                        rs.getBigDecimal("onetime_revenue"),
                        rs.getBigDecimal("recurring_revenue"),
                        rs.getInt("quantity"),
                        rs.getString("note"),
                        rs.getInt("lineitems"),
                        rs.getDate("start_date"),
                        rs.getDate("end_date"),
                        (rs.getLong("devid") == 0 ? null : rs.getLong("devid")),
                        rs.getString("dname"),
                        rs.getString("part_number"),
                        rs.getString("ddescr"),
                        rs.getInt("dcount"),
                        Service.Status.valueOf(rs.getString("status")),
                        (rs.getLong("contract_service_subscription_id") == 0 ? null : rs.getLong("contract_service_subscription_id")),
                        rs.getString("subscription_type"),
                        (rs.getLong("microsoft_365_subscription_config_id") == 0 ? null : rs.getLong("microsoft_365_subscription_config_id")),
                        (rs.getInt("dlocation") == 0 ? null : rs.getInt("dlocation")));
            }
        });
        for (Service cs : contractServices) {
            // attach the contract service - contract updates
            cs.setContractUpdates(jdbcTemplate.query("select cu.id, cu.alt_id, cu.job_number, cu.ticket_number, cu.note,"
                    + " cu.contract_id, cu.signed_date, cu.effective_date, cu.onetime_price cu_onetime_price, cu.recurring_price cu_recurring_price, cu.updated, cu.updated_by, cu.file_path cu_file_path, cucs.note,"
                    + " cucs.operation"
                    + " from contract_update_contract_service cucs"
                    + " left join contract_service csvc on cucs.contract_service_id = csvc.id"
                    + " left join contract_update cu on cucs.contract_update_id = cu.id"
                    + " where csvc.id = ?"
                    + " order by cucs.created desc", new Object[]{cs.getId()}, new RowMapper<ContractUpdate>() {
                @Override
                public ContractUpdate mapRow(ResultSet rs, int i) throws SQLException {
                    return new ContractUpdate(
                            rs.getLong("id"),
                            rs.getLong("contract_id"),
                            rs.getString("alt_id"),
                            rs.getString("job_number"),
                            rs.getString("ticket_number"),
                            rs.getString("note"),
                            rs.getDate("signed_date"),
                            rs.getDate("effective_date"),
                            rs.getBigDecimal("cu_onetime_price"),
                            rs.getBigDecimal("cu_recurring_price"),
                            rs.getDate("updated"),
                            rs.getString("updated_by"),
                            rs.getString("cu_file_path"));
                }
            }));
            // attach possible related lineitems... note: these records are already a part of this
            // list, but now they can be nested under a parent as well for preferred processing
            cs.setRelatedLineItems(serviceRevenueRelatedRecordsForParent(cs.getId(), null, null));
        }
        return contractServices;
    }

    @Override
    public List<Service> contractUpdateServices(Long contractUpdateId) {
        String query = "select csvc.id, csvc.service_id, csvc.parent_id, csvc.contract_id, csvc.contract_group_id, service.code, service.osp_id, service.name, service.version,"
                + " csvc.onetime_revenue, csvc.recurring_revenue, csvc.note, csvc.quantity, count(clit.lineitem_id) lineitems,"
                + " csvc.start_date, csvc.end_date, csvc.status, dev.id devid, csd.name dname, csd.unit_count dcount, csd.location_id dlocation, dev.part_number, dev.description ddescr, cucs.contract_update_id, csvc.contract_service_subscription_id, css.subscription_type, csvc.microsoft_365_subscription_config_id "
                + " from contract_service csvc"
                + " left join service service on service.id = csvc.service_id"
                + " left join contract_update_contract_service cucs on cucs.contract_service_id = csvc.id"
                + " left outer join contract_lineitem clit on clit.contract_service_id = csvc.id"
                + " left outer join contract_service_device csd on csd.contract_service_id = csvc.id"
                + " left outer join device dev on dev.id = csd.device_id"
                + " left outer join contract_service_subscription css on css.id = csvc.contract_service_subscription_id"
                + " where cucs.contract_update_id = :contractUpdateId"
                + " group by csvc.id, csvc.service_id, csvc.contract_id, csvc.contract_group_id, service.code, service.osp_id, service.name, service.version,"
                + " csvc.onetime_revenue, csvc.recurring_revenue, csvc.note, csvc.quantity, csvc.start_date, csvc.end_date,"
                + " dev.id, csd.name, csd.unit_count, dev.part_number, dev.description, cucs.contract_update_id";
//        DateTime now = new DateTime(); // return only contracts that are not ended
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("contractUpdateId", contractUpdateId);
        List<Service> contractServices = namedJdbcTemplate.query(query, params,
                new RowMapper<Service>() {
            @Override
            public Service mapRow(ResultSet rs, int i) throws SQLException {
                return new Service(
                        rs.getLong("id"),
                        rs.getString("code"),
                        (rs.getLong("parent_id") == 0 ? null : rs.getLong("parent_id")),
                        rs.getLong("contract_id"),
                        (rs.getLong("contract_group_id") == 0 ? null : rs.getLong("contract_group_id")),
                        rs.getLong("contract_update_id"),
                        rs.getLong("service_id"),
                        rs.getString("osp_id"),
                        rs.getDouble("version"),
                        rs.getString("name"),
                        rs.getBigDecimal("onetime_revenue"),
                        rs.getBigDecimal("recurring_revenue"),
                        rs.getInt("quantity"),
                        rs.getString("note"),
                        rs.getInt("lineitems"),
                        rs.getDate("start_date"),
                        rs.getDate("end_date"),
                        (rs.getLong("devid") == 0 ? null : rs.getLong("devid")),
                        rs.getString("dname"),
                        rs.getString("part_number"),
                        rs.getString("ddescr"),
                        rs.getInt("dcount"),
                        Service.Status.valueOf(rs.getString("status")),
                        (rs.getLong("contract_service_subscription_id") == 0 ? null : rs.getLong("contract_service_subscription_id")),
                        rs.getString("subscription_type"),
                        (rs.getLong("microsoft_365_subscription_config_id") == 0 ? null : rs.getLong("microsoft_365_subscription_config_id")),
                        (rs.getInt("dlocation") == 0 ? null : rs.getInt("dlocation")));
            }
        });
        for (Service cs : contractServices) {
            // attach the contract service - contract updates
            cs.setContractUpdates(jdbcTemplate.query("select cu.id, cu.alt_id, cu.job_number, cu.ticket_number, cu.note,"
                    + " cu.contract_id, cu.signed_date, cu.effective_date, cu.onetime_price cu_onetime_price, cu.recurring_price cu_recurring_price, cu.updated, cu.updated_by, cu.file_path cu_file_path, cucs.note,"
                    + " cucs.operation"
                    + " from contract_update_contract_service cucs"
                    + " left join contract_service csvc on cucs.contract_service_id = csvc.id"
                    + " left join contract_update cu on cucs.contract_update_id = cu.id"
                    + " where csvc.id = ?"
                    + " order by cucs.created desc", new Object[]{cs.getId()}, new RowMapper<ContractUpdate>() {
                @Override
                public ContractUpdate mapRow(ResultSet rs, int i) throws SQLException {
                    return new ContractUpdate(
                            rs.getLong("id"),
                            rs.getLong("contract_id"),
                            rs.getString("alt_id"),
                            rs.getString("job_number"),
                            rs.getString("ticket_number"),
                            rs.getString("note"),
                            rs.getDate("signed_date"),
                            rs.getDate("effective_date"),
                            rs.getBigDecimal("cu_onetime_price"),
                            rs.getBigDecimal("cu_recurring_price"),
                            rs.getDate("updated"),
                            rs.getString("updated_by"),
                            rs.getString("cu_file_path"));
                }
            }));
            // attach possible related lineitems... note: these records are already a part of this
            // list, but now they can be nested under a parent as well for preferred processing
            cs.setRelatedLineItems(serviceRevenueRelatedRecordsForParent(cs.getId(), null, null));
        }
        return contractServices;
    }
    
    @Override
    public List<CSPBilledContractService> serviceRevenueRollupForMonthOfByCustomer(Device.DeviceType deviceType, Integer month, String year) {
    	String query = "select csvc.service_id, csvc.contract_id,";
        query += " svc.code, svc.osp_id, svc.name, dev.id devid, dev.part_number, dev.description ddescr, dev.device_type,"
                + " sum(csd.unit_count) dcount, sum(csvc.quantity) quantity, csvc.start_date,"
                + " csvc.end_date, GROUP_CONCAT(distinct csvc.status SEPARATOR ', ') as status, csvc.status, sum(csvc.quantity * csvc.onetime_revenue) onetime,"
                + " sum(csvc.quantity * csvc.recurring_revenue) revenue, cu.name customer_name, cu.alt_name customer_alt_name"
                + " from service svc"
                + " left join contract_service csvc on csvc.service_id = svc.id"
                + " left outer join contract_service_device csd on csd.contract_service_id = csvc.id"
                + " left outer join device dev on dev.id = csd.device_id"
                + " inner join contract co on co.id = csvc.contract_id"
                + " inner join customer cu on cu.id = co.customer_id"
                + " where dev.device_type = :deviceType"
                + " and csvc.status != 'pending'"
                + " and (csvc.start_date <= :leftDate or csvc.start_date between :leftDate and :rightDate)"
                + " and (csvc.end_date is null or csvc.end_date between :leftDate and :rightDate"
                + " or csvc.end_date >= :rightDate)"
        		+ " group by csvc.service_id, csvc.contract_id,"
        		+ " csvc.start_date, csvc.end_date, dev.part_number, dev.description"
                + " order by svc.name, csvc.start_date, dev.part_number, dev.description";

        final DateTime datePointer = DateTimeFormat.forPattern("yyyy").parseDateTime(year)
                .withMonthOfYear(month)
                .withDayOfMonth(1)
                .withTimeAtStartOfDay()
                .withZone(DateTimeZone.forID(TZID));
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("deviceType", deviceType.name());
        params.put("leftDate", datePointer.toDate());
        params.put("rightDate", datePointer.dayOfMonth().withMaximumValue().plusHours(23).plusMinutes(59).plusSeconds(59).toDate());
        List<CSPBilledContractService> contractServices = namedJdbcTemplate.query(query, params,
                new RowMapper<CSPBilledContractService>() {
            @Override
            public CSPBilledContractService mapRow(ResultSet rs, int i) throws SQLException {
                return new CSPBilledContractService(
                		datePointer,
                		rs.getLong("contract_id"),
                		rs.getString("customer_name"),
                		rs.getString("customer_alt_name"),
                		rs.getBigDecimal("onetime"),
                        rs.getBigDecimal("revenue"),
                        rs.getInt("quantity"),
                        rs.getDate("start_date"),
                        rs.getDate("end_date"),
                        rs.getString("part_number"),
                        rs.getString("ddescr"),
                        rs.getInt("dcount"),
                        (rs.getString("device_type") != null ? Device.DeviceType.valueOf(rs.getString("device_type")) : null),
                		(rs.getLong("devid") == 0 ? null : rs.getLong("devid")),
                		null,
                		null);
            }
        });
        
        return contractServices;
    }
    
    @Override
    public List<CSPBilledContractService> serviceRevenueCSPForMonthOf(Device.DeviceType deviceType, Integer month, String year) {
    	String query = "select csvc.service_id, csvc.contract_id,";
        query += " svc.code, svc.osp_id, svc.name, dev.id devid, dev.part_number, dev.description ddescr, dev.device_type,"
                + " csd.unit_count dcount, csvc.quantity quantity, csvc.start_date,"
                + " csvc.end_date, csvc.status, csvc.onetime_revenue onetime,"
                + " csvc.recurring_revenue revenue, cu.name customer_name, cu.alt_name customer_alt_name, csa.id csa_id, csa.subscription_id, csa.name csa_name"
                + " from service svc"
                + " left join contract_service csvc on csvc.service_id = svc.id"
                + " left outer join contract_service_device csd on csd.contract_service_id = csvc.id"
                + " left outer join device dev on dev.id = csd.device_id"
                + " left outer join contract_service_subscription csa on csa.id = csvc.contract_service_subscription_id"
                + " inner join contract co on co.id = csvc.contract_id"
                + " inner join customer cu on cu.id = co.customer_id"
                + " where dev.device_type = :deviceType"
                + " and csvc.status = 'active'"
                + " and (csvc.start_date <= :leftDate or csvc.start_date between :leftDate and :rightDate)"
                + " and (csvc.end_date is null or csvc.end_date between :leftDate and :rightDate"
                + " or csvc.end_date >= :rightDate)"
                + " order by cu.name, csvc.start_date, dev.part_number, dev.description";

        final DateTime datePointer = DateTimeFormat.forPattern("yyyy").parseDateTime(year)
                .withMonthOfYear(month)
                .withDayOfMonth(1)
                .withTimeAtStartOfDay()
                .withZone(DateTimeZone.forID(TZID));
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("deviceType", deviceType.name());
        params.put("leftDate", datePointer.toDate());
        params.put("rightDate", datePointer.dayOfMonth().withMaximumValue().plusHours(23).plusMinutes(59).plusSeconds(59).toDate());
        List<CSPBilledContractService> contractServices = namedJdbcTemplate.query(query, params,
                new RowMapper<CSPBilledContractService>() {
            @Override
            public CSPBilledContractService mapRow(ResultSet rs, int i) throws SQLException {
                return new CSPBilledContractService(
                		rs.getLong("service_id"),
                		rs.getLong("contract_id"),
                		rs.getString("customer_name"),
                		rs.getString("customer_alt_name"),
                		rs.getBigDecimal("onetime"),
                        rs.getBigDecimal("revenue"),
                        rs.getInt("quantity"),
                        rs.getDate("start_date"),
                        rs.getDate("end_date"),
                        rs.getString("part_number"),
                        rs.getString("ddescr"),
                        rs.getInt("dcount"),
                        (rs.getString("device_type") != null ? Device.DeviceType.valueOf(rs.getString("device_type")) : null),
                		(rs.getLong("devid") == 0 ? null : rs.getLong("devid")),
                		rs.getLong("csa_id"),
                		rs.getString("subscription_id"));
            }
        });
        
        return contractServices;
    }
    
   @Override
   public List<SPLARevenue> splaRevenueReport(final DateTime monthof, Long customerId, Long deviceId, Long splaId, String vendor) {
        
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("leftDate", monthof.toDate());
        params.put("rightDate", monthof.dayOfMonth().withMaximumValue().plusHours(23).plusMinutes(59).plusSeconds(59).toDate());
        
        String query = "select cust.id as 'custId', cust.name as 'customer', cn.id as 'contractId', cn.name as 'contract',"
                + " svc.osp_id as 'ospId', svc.name as 'service',"
                + " sum(csvc.quantity) as 'quantity', sum(csvc.quantity * csvc.onetime_revenue) as 'onetime',"
                + " sum(csvc.quantity * csvc.recurring_revenue) as 'recurring', csvc.start_date, csvc.end_date,"
                + " dev.id as 'deviceId', dev.part_number, dev.description as 'device', sum(csd.unit_count) as 'unit_count'"
                + " from contract cn"
                + " inner join contract_service csvc on cn.id = csvc.contract_id"
                + " inner join customer cust on cn.customer_id = cust.id"
                + " inner join service svc on svc.id = csvc.service_id"
                + " inner join contract_service_device csd on csd.contract_service_id = csvc.id"
                + " inner join device dev on dev.id = csd.device_id"
                + " inner join device_spla_cost_catalog dcat on dcat.device_id = csd.device_id"
                + " inner join spla_cost_catalog spla on dcat.spla_cost_catalog_id = spla.id"
                + " where (csvc.start_date <= :leftDate or csvc.start_date between :leftDate and :rightDate)"
                + " and (csvc.end_date is null or csvc.end_date between :leftDate and :rightDate"
                + " or csvc.end_date >= :rightDate)"
                + " and csvc.status = 'active'";
        if (customerId != null && customerId > 0) {
            query += " and cust.id = :custId";
            params.put("custId", customerId);
        }
        if (deviceId != null && deviceId > 0) {
            query += " and dev.id = :devId";
            params.put("devId", deviceId);
        }
        if (splaId != null && splaId > 0) {
            query += " and spla.id = :splaId";
            params.put("splaId", splaId);
        }
        if (StringUtils.isNotBlank(vendor)) {
            query += " and spla.vendor = :vendor";
            params.put("vendor", vendor);
        }
        query += " group by cust.id, cust.name, cn.id, cn.name, svc.osp_id, svc.name, csvc.start_date, csvc.end_date, dev.id"
                + " order by cust.name, cn.id, svc.name, csvc.start_date, csvc.end_date";
        
        List<SPLARevenue> splaRevenue = namedJdbcTemplate.query(query, params,
                new RowMapper<SPLARevenue>() {
            @Override
            public SPLARevenue mapRow(ResultSet rs, int i) throws SQLException {
                return new SPLARevenue(
                        rs.getLong("custId"),
                        rs.getString("customer"),
                        rs.getLong("contractId"),
                        rs.getString("contract"),
                        rs.getLong("ospId"),
                        rs.getString("service"),
                        rs.getInt("quantity"),
                        rs.getInt("unit_count"),
                        rs.getBigDecimal("onetime"),
                        rs.getBigDecimal("recurring"),
                        monthof,
                        new DateTime(rs.getDate("start_date")),
                        new DateTime(rs.getDate("end_date")),
                        (rs.getLong("deviceId") == 0 ? null : rs.getLong("deviceId")),
                        rs.getString("device"),
                        rs.getString("part_number")
                );
            }
        });
        return splaRevenue;
   }
}
