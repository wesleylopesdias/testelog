package com.logicalis.serviceinsight.service;

import com.logicalis.serviceinsight.dao.DeviceExpenseCategory;
import java.io.File;

import com.logicalis.serviceinsight.dao.ExpenseCategory;
import com.logicalis.serviceinsight.dao.ServiceExpenseCategory;
import com.logicalis.serviceinsight.dao.UnitCost;
import com.logicalis.serviceinsight.data.Device;
import com.logicalis.serviceinsight.data.Service;
import com.logicalis.serviceinsight.data.UnallocatedExpense;
import com.logicalis.serviceinsight.service.CostDaoServiceImpl.ModelExpenseType;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

public interface CostDaoService extends BaseService {

    //Expense Categories
    public ExpenseCategory expenseCategoryByName(String name, String parentName) throws ServiceException;

    //Imports
    public void importExpenses(File excel, ModelExpenseType expenseType, Date expenseDate) throws ServiceException;

    public List<UnitCost> unitCosts();
    public Long saveUnitCost(UnitCost uc) throws ServiceException;
    public void updateUnitCost(UnitCost uc) throws ServiceException;
    public UnitCost unitCostByExpenseCategoryAndDate(Long customerId, Integer expenseCategoryId, DateTime appliedDate);
    public Map<String, UnitCost> unitCostByExpenseCategoryAndDateRange(Long customerId, Integer expenseCategoryId, DateTime startDate, DateTime endDate);
    public List<UnitCost> unitCostByExpenseCategory(Long customerId, Integer expenseCategoryId);
    public List<UnitCost> allUnitCostByExpenseCategory(Integer expenseCategoryId);

    /**
     * @deprecated no longer use the service_expense_category table
     * @param ospId
     * @return 
     */
    public List<ServiceExpenseCategory> serviceExpenseCategories(Long ospId);
    public void saveOrUpdateDeviceCostMappings(Long deviceId, List<DeviceExpenseCategory> costMappings) throws ServiceException;
    public List<Service> servicesForExpenseCategory(Integer expenseCategoryId) throws ServiceException;
    public List<Device> devicesForExpenseCategory(Integer expenseCategoryId) throws ServiceException;
    
    public UnallocatedExpense unallocatedExpense(Long id) throws ServiceException;
    public List<UnallocatedExpense> unallocatedExpenses() throws ServiceException;
    public List<UnallocatedExpense> unallocatedExpensesForMonth(Date month) throws ServiceException;
    public List<UnallocatedExpense> unallocatedExpensesForCostAllocation(Long costAllocationId) throws ServiceException;
    public List<UnallocatedExpense> unallocatedExpensesForPeriod(Date startDate, Date endDate) throws ServiceException;
    public Long saveUnallocatedExpense(UnallocatedExpense ue) throws ServiceException;
    public void updateUnallocatedExpense(UnallocatedExpense ue) throws ServiceException;
    public void deleteUnallocatedExpense(Long id) throws ServiceException;
    
    /**
     * Produces a "custom", UNPERSISTED UnitCost Object that is based on querying costs that EXCLUDE
     * a specific list of CostType / CostSubType (map keys) combinations. This list can be empty. The customerId can be empty
     * 
     * @param customerId (nullable)
     * @param expenseCategoryId (required)
     * @param appliedDate (required)
     * @param costTypes (nullable)
     * @return
     * @throws ServiceException 
     */
    public UnitCost customCostByExpenseCategoryAndDate(Long customerId, Integer expenseCategoryId, DateTime appliedDate, List<Map<String, String>> costTypes);
}
