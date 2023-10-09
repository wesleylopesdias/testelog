package com.logicalis.serviceinsight.scheduled;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.logicalis.serviceinsight.service.DataWarehouseService;
import com.logicalis.serviceinsight.service.ServiceException;

@Component
public class DataWarehouseScheduler extends BaseScheduler {

	@Autowired
	DataWarehouseService dataWarehouseService;
	
	//we sync once a month after the month to make sure last month's data is complete
	@Async
	@Scheduled(cron = "0 45 1 8 * ?") // 1:45am every day 8 of the month...
	public void updateDataWarehouse(){
		try {
			dataWarehouseService.updateDataWarehouseCIs();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//daily sync to provide up-to-date data
	@Async
	@Scheduled(cron = "0 39 23 * * ?") // 11:39pm every day 
	public void updateDataWarehouseDaily(){
		try {
			dataWarehouseService.updateDataWarehouseCIsforMonthOf(new Date());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Async
	@Scheduled(cron = "0 52 23 * * ?") // 11:52pm every day...
	public void updateDataWarehouseContracts(){
		try {
			dataWarehouseService.updateDataWarehouseContracts();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Async
	@Scheduled(cron = "0 55 23 * * ?") // 11:55pm every day...
	public void updateDataWarehouseContractUpdates(){
		try {
			dataWarehouseService.updateDataWarehouseContractUpdates();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Async
	@Scheduled(cron = "0 55 23 3 * ?") // 11:55pm every day 3 of the month...
	public void updateDataWarehouseCosts(){
		try {
			dataWarehouseService.updateDataWarehouseCosts();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
