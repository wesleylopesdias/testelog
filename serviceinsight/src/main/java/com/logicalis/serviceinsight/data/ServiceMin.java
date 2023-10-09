package com.logicalis.serviceinsight.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * this is the minimal Service information data that comes across from the OSP
 * "minlist" REST web-service
 * 
 * @author poneil
 */
public class ServiceMin implements Comparable<ServiceMin> {
    
    private Long id;
    private String name;
    private String versionId;
    @JsonProperty(value = "currentVersionDocId")
    private String docId;
    private String businessModelIdentifier;
    private String status;

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the versionId
     */
    public String getVersionId() {
        return versionId;
    }

    /**
     * @param versionId the versionId to set
     */
    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    /**
     * @return the docId
     */
    public String getDocId() {
        return docId;
    }

    /**
     * @param docId the docId to set
     */
    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getBusinessModelIdentifier() {
        return businessModelIdentifier;
    }

    public void setBusinessModelIdentifier(String businessModelIdentifier) {
        this.businessModelIdentifier = businessModelIdentifier;
    }
    
    public String getSIBusinessModel() {
    	if(this.businessModelIdentifier == null) {
    		return "Other";
    	}
    	
        switch(this.businessModelIdentifier) {
            case "lifecycle": return "Other";
            case "managed": return "Managed";
            case "monitoring": return "Managed";
            case "provisioned": return "Cloud";
            case "professional": return "Other";
            default: return "Other";
        }
    }
    
    public Boolean getSIDisabled() {
    	if(this.businessModelIdentifier == null) {
    		return Boolean.FALSE;
    	}
    	
        switch(this.businessModelIdentifier) {
            case "lifecycle": return Boolean.FALSE;
            case "managed": return Boolean.FALSE;
            case "monitoring": return Boolean.FALSE;
            case "provisioned": return Boolean.FALSE;
            case "professional": return Boolean.TRUE;
            default: return Boolean.FALSE;
        }
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.id);
        hash = 67 * hash + Objects.hashCode(this.versionId);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ServiceMin other = (ServiceMin) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!Objects.equals(this.versionId, other.versionId)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(ServiceMin o) {
        if (o == null) {
            return 1;
        }
        if (getName() != null) {
            if (o.getName() == null) {
                return 1;
            }
            int idx = getName().compareTo(o.getName());
            if (idx != 0) {
                return idx;
            }
        }
        if (getId() != null) {
            if (o.getId() == null) {
                return 1;
            }
            int idx = getId().compareTo(o.getId());
            if (idx != 0) {
                return idx;
            }
        }
        if (getVersionId() != null) {
            if (o.getVersionId() == null) {
                return 1;
            }
            int idx = getVersionId().compareTo(o.getVersionId());
            if (idx != 0) {
                return idx;
            }
        }
        return 0;
    }

    @Override
    public String toString() {
        return "ServiceMin{" + "id=" + id + ", name=" + name + ", versionId=" + versionId + ", docId=" + docId + ", businessModelIdentifier=" + businessModelIdentifier + ", status=" + status + '}';
    }
}
