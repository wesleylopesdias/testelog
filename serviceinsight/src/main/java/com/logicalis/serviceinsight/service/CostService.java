package com.logicalis.serviceinsight.service;

import com.logicalis.serviceinsight.data.CostFractionRecord;
import com.logicalis.serviceinsight.data.ReportWrapper;
import com.logicalis.serviceinsight.data.StandardCost;
import com.logicalis.serviceinsight.data.UnitCostDetails;
import com.logicalis.serviceinsight.representation.CostItemAnalysisWrapper;
import com.logicalis.serviceinsight.representation.CostItemTypeSubType;
import com.logicalis.serviceinsight.representation.LaborBreakdownRecord;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

/**
 *
 * @author poneil
 */
public interface CostService extends BaseService {

    public Integer serviceTotalDeviceCount(DateTime startDate, DateTime endDate, String businessModel);
    
    public Integer serviceTotalDeviceCount(DateTime startDate, DateTime endDate, String businessModel, Long customerId, Long ospId);
    
    public Integer deviceTotalDeviceCount(DateTime startDate, DateTime endDate, String businessModel);
            
    public Integer deviceTotalDeviceCount(DateTime startDate, DateTime endDate, String businessModel, Long customerId, Long ospId, Long deviceId);
    
    public Integer deviceTotalDeviceCountWithExpenseCategory(DateTime startDate, DateTime endDate, String businessModel, Long customerId, Long ospId, Integer expenseCategoryId, Long deviceId) throws ServiceException;
    
    public List<StandardCost> getStandardCosts();

    public Map<String, Map<String, BigDecimal>> laborRevenueForYear(Long ospId, String year);

    public Map<String, BigDecimal> serviceLaborForMonth(String businessModel, Long ospId, Integer month, String year, Long customerId, Boolean includeChildren, Boolean onboarding);

    public Map<String, BigDecimal> serviceLaborForDates(String businessModel, Long ospId, DateTime startDate, DateTime endDate, Long customerId, Boolean includeChildren, Boolean onboarding);

    /**
     * @deprecated no longer use the service_expense_category table
     * 
     * @param businessModel
     * @param ospId
     * @param startDate
     * @param endDate
     * @return 
     */
    public Map<String, BigDecimal> expenseCategoryLaborForDates(String businessModel, Long ospId, DateTime startDate, DateTime endDate);
    
    /**
     * @deprecated we are not calculating indirect labor using a proportion of total indirect labor / direct labor
     * @param fordate
     * @param businessModel
     * @return 
     */
    public Map<String, BigDecimal> indirectLaborUnitCost(Date fordate, String businessModel);
    
    public Map<String, BigDecimal> indirectLaborForDates(DateTime startDate, DateTime endDate, String businessModel);
    
    public Map<String, BigDecimal> indirectLaborProportionForDates(DateTime startDate, DateTime endDate);
    
    public Map<String, List<CostFractionRecord>> customerDirectCostsForExpenseCategoryAndDateRange(Integer expenseCategoryId, DateTime startDate, DateTime endDate);
    
    public List<CostFractionRecord> customerDirectCostsForExpenseCategory(Integer expenseCategoryId, DateTime appliedDate);
    
    public BigDecimal totalCostForCostCategory(final DateTime costDate, Long customerId, Integer expenseCategoryId, Boolean nonCustomer);
    
    public List<Map<String, Map<String, BigDecimal>>> serviceLaborByServiceForMonth(DateTime month);

    public List<Map<String, Map<String, BigDecimal>>> serviceLaborByServiceForDates(DateTime startDate, DateTime endDate);

    public List<Map<String, Map<String, BigDecimal>>> laborWithoutServiceByCustomerForDates(DateTime startDate, DateTime endDate);

    public List<Map<String, Map<String, BigDecimal>>> laborWithoutServiceByChronosTaskForDates(DateTime startDate, DateTime endDate);
    
    public List<LaborBreakdownRecord> laborBreakdownForMonth(DateTime month);
    
    public ReportWrapper laborHoursReport(Date startDate, Date endDate, Long ospId, Long customerId, Long recordLimit, Boolean includeChildren) throws ServiceException;
    
    public List<UnitCostDetails.LaborDetail> laborCostsForExpenseCategory(Long customerId, Integer expenseCategoryId, DateTime costDate);
    
    public void generateSPLACostsForMonth(Integer month, String year);
    public void runGenerateSPLACosts();
    
    public void runGenerateDeviceCounts();
    public void runGenerateAllDeviceCounts();
    public void generateDeviceCounts(Integer month, String year) throws ServiceException;
    
    public List<CostItemTypeSubType> refCostItemTypeSubTypes();
    public CostItemAnalysisWrapper costItemAnalysis(DateTime costDate) throws ServiceException;
}
