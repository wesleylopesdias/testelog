package com.logicalis.serviceinsight.service;

import com.logicalis.serviceinsight.dao.DeviceExpenseCategory;
import com.logicalis.serviceinsight.dao.ExpenseCategory;
import com.logicalis.serviceinsight.dao.Location;
import com.logicalis.serviceinsight.dao.SPLACost;
import com.logicalis.serviceinsight.data.SubscriptionUplift;
import com.logicalis.serviceinsight.data.Device;
import com.logicalis.serviceinsight.data.DeviceProperty;
import com.logicalis.serviceinsight.data.DeviceRelationship;
import com.logicalis.serviceinsight.data.Personnel;
import com.logicalis.serviceinsight.data.ScheduledTask;
import com.logicalis.serviceinsight.data.Service;
import com.logicalis.serviceinsight.data.ServiceAlign;
import com.microsoft.partnercenter.api.schema.datamodel.AzureMeter;
import com.microsoft.partnercenter.api.schema.datamodel.AzureOfferTerm;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;

/**
 *
 * @author poneil
 */
public interface ApplicationDataDaoService {

    public Long saveDevice(Device device) throws ServiceException;
    public void updateDevice(Device device) throws ServiceException;
    public void deleteDevice(Long id) throws ServiceException;
    public Device device(Long id) throws ServiceException;
    public Device findDeviceByProductId(Long productId) throws ServiceException;
    /**
     * Returns List of: id|part_number|description where archived is false
     * @return 
     */
    public List<String> pipeDelimitedDeviceString();
    public void mergeDevice(Long oldDeviceId, Long newDeviceId) throws ServiceException;
    
    public List<Device> devices();
    public List<Device> devices(Boolean isArchived);
    public List<Device> minimumDevices(Boolean isArchived);
    public List<Device> findDevicesForOSMSync() throws ServiceException;
    public List<Device> findDevicesForContract(Long contractId);
    public List<Device> findDeviceByDefaultOSPId(Long ospId) throws ServiceException;
    public List<Device> findDevicesForCostAllocation(Date month);
    
    public List<Device> searchDevicesByDescription(String search);

    public List<Device> searchDevicesByPartNumber(String search);

    public Device findDeviceByNameAndPartNumber(String partNumber, String description);

    public Device findDeviceByPartNumber(String partNumber);
    
    public List<Device> findDeviceByDeviceType(Device.DeviceType deviceType) throws ServiceException;
    public List<Device> findDevicesForSPLACost(Long splaCostCatalogId);
    
    public Service findAnyServiceByName(String name);
    
    public Service findActiveServiceByName(String name);

    public Service findActiveServiceByOspId(Long id);

    public Service findAnyServiceByOspId(Long id);
    
    public List<Personnel> personnel(Boolean active);
    public Location location(Integer id) throws ServiceException;

    public Location findLocationByName(String name) throws ServiceException;

    public List<Location> locations(Boolean isDisplayedRevenue);

    public ScheduledTask findScheduledTaskByCode(String code) throws ServiceException;
    
    public AzureMeter azureMeter(String id);
    
    public void saveAzureMeter(AzureMeter meter, String locale, String currency, Boolean taxIncluded);
    
    public void updateAzureMeter(AzureMeter meter, String locale, String currency, Boolean taxIncluded);
    
    public BigDecimal azureOfferTermsSum(String locale, String currency, String id, String azureUsageDate);
    
    public List<BigDecimal> azureOfferTerms(String locale, String currency, String id, String azureUsageDate);
    
    public void saveAzureOfferTerms(AzureOfferTerm term, String locale, String currency);
    
    public void deleteAzureOfferTerms(String locale, String currency);
    
    public SubscriptionUplift subscriptionUplift(Long id) throws ServiceException;
    public SubscriptionUplift subscriptionUpliftByCode(String id) throws ServiceException;
    
    public List<SPLACost> splaCosts(Boolean active, Boolean includeDevices);
    public Long saveSPLACost(SPLACost splaCost) throws ServiceException;
    public void updateSPLACost(SPLACost splaCost) throws ServiceException;
    public void deleteSPLACost(Long id) throws ServiceException;
    public SPLACost splaCost(Long id) throws ServiceException;
    public List<SPLACost> findSPLACostsForDevice(Long deviceId);
    public List<Device> findRelatedDevices(Long deviceId) throws ServiceException;
    public List<DeviceExpenseCategory> findCostMappingsForDevice(Long deviceId);
    public List<DeviceExpenseCategory> findCostMappingsForExpenseCategory(Integer expenseCategoryId);
    public List<ExpenseCategory> findExpenseCategories();
    public List<DeviceProperty> findDevicePropertiesForDevice(Long deviceId);
    
    public void serviceAlignment(ServiceAlign serviceAlign) throws ServiceException;
    
}
