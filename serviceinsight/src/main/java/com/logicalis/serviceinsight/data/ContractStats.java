package com.logicalis.serviceinsight.data;

import java.util.ArrayList;
import java.util.List;

/**
 * POJO representing contract stats on the Dashboard
 * 
 * @author jsanchez
 *
 */
public class ContractStats {
    private List<ContractStat> contracts;
    private Long msContractCount;
    private Long cloudContractCount;
    private Long msCloudContractCount;
        
    public ContractStats(){
        contracts = new ArrayList<ContractStat>();
        msContractCount = new Long(0l);
        cloudContractCount = new Long(0l);
        msCloudContractCount = new Long(0l);
    }

    public ContractStats(List<ContractStat> contracts, Long msContractCount, Long cloudContractCount, Long msCloudContractCount) {
        this.contracts = contracts;
        this.msContractCount = msContractCount;
        this.cloudContractCount = cloudContractCount;
        this.msCloudContractCount = msCloudContractCount;
    }

    public List<ContractStat> getContracts() {
        return contracts;
    }

    public void setContracts(List<ContractStat> contracts) {
        this.contracts = contracts;
    }

    public Long getMsContractCount() {
        return msContractCount;
    }

    public void setMsContractCount(Long msContractCount) {
        this.msContractCount = msContractCount;
    }

    public Long getCloudContractCount() {
        return cloudContractCount;
    }

    public void setCloudContractCount(Long cloudContractCount) {
        this.cloudContractCount = cloudContractCount;
    }

    public Long getMsCloudContractCount() {
        return msCloudContractCount;
    }

    public void setMsCloudContractCount(Long msCloudContractCount) {
        this.msCloudContractCount = msCloudContractCount;
    }

    @Override
    public String toString() {
        return "{contracts:" + contracts + ", msContractCount:" + msContractCount
                + ", cloudContractCount:" + cloudContractCount + ", msCloudContractCount:" + msCloudContractCount + "}";
    }

}
