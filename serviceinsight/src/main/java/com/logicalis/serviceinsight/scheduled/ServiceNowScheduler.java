package com.logicalis.serviceinsight.scheduled;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import com.logicalis.serviceinsight.data.ScheduledTask;
import com.logicalis.serviceinsight.service.ApplicationDataDaoService;
import com.logicalis.serviceinsight.service.ContractDaoService;
import com.logicalis.serviceinsight.service.ServiceNowService;

@Transactional
@org.springframework.stereotype.Service
public class ServiceNowScheduler extends BaseScheduler {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    @Autowired
    ServiceNowService serviceNowService;
    @Autowired
    ContractDaoService contractDaoService;
    @Autowired
    ApplicationDataDaoService applicationDataDaoService;
    private static final String SN_CONTRACT_SYNC_TSK = "sn_contract_sync";
    private static final String SN_CONTRACT_CI_SYNC_TSK = "sn_contract_ci_sync";

    @Scheduled(cron = "0 10 1 * * *") //1:10am
    //@Scheduled(fixedRate = 600000, initialDelay = 60000)
    public void contractSysIdSync() {
        try {
            ScheduledTask st = applicationDataDaoService.findScheduledTaskByCode(SN_CONTRACT_SYNC_TSK);
            if (st != null && st.getEnabled()) {
                log.info("Running Task: " + st.getName());
                serviceNowService.syncContractsFromServiceNow();
                log.info("Ending Task: " + st.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Scheduled(cron = "0 30 1 * * *") //1:30am
    //@Scheduled(fixedRate = 600000, initialDelay = 90000)
    public void contractCISync() {
        try {
            ScheduledTask st = applicationDataDaoService.findScheduledTaskByCode(SN_CONTRACT_CI_SYNC_TSK);
            if (st != null && st.getEnabled()) {
                log.info("Running Task: " + st.getName());
                serviceNowService.syncContractCIsFromServiceNow();
                log.info("Ending Task: " + st.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
