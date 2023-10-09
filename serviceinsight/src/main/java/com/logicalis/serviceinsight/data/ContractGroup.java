package com.logicalis.serviceinsight.data;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;

/**
 * A group or "sub-level" of a contract, for relating contract services to, for
 * example, a customer's own customers
 *
 * @author poneil
 */
public class ContractGroup implements Comparable<ContractGroup> {

    private Long id;
    private String name;
    private String description;
    private Long contractId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MMddyyyy", timezone = "America/New_York")
    private Date created;
    private String createdBy;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MMddyyyy", timezone = "America/New_York")
    private Date updated;
    private String updatedBy;

    /**
     * default CTOR
     */
    public ContractGroup() {
    }

    public ContractGroup(Long id, Long contractId, String name, String description) {
        this.id = id;
        this.contractId = contractId;
        this.name = name;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getContractId() {
        return contractId;
    }

    public void setContractId(Long contractId) {
        this.contractId = contractId;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    @Override
    public int compareTo(ContractGroup o) {
        if (this.id != null && o.getId() == null) {
            return 1;
        } else if (this.getId() == null && o.getId() != null) {
            return -1;
        } else if (this.id != null && o.getId() != null) {
            return this.id.compareTo(o.getId());
        }
        return 0;
    }
}
