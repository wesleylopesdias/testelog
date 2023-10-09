package com.logicalis.serviceinsight.data;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author poneil
 */
public class Customer implements Comparable<Customer> {

    private Long id;
    private Long altId;
    private Customer parent;
    private String name;
    private String description;
    private String phone;
    private String street1;
    private String street2;
    private String city;
    private String state;
    private String zip;
    private String country;
    private Integer contractCount;
    private Boolean archived = Boolean.FALSE;
    private Boolean siEnabled = Boolean.FALSE;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MMddyyyy", timezone = "America/New_York")
    private Date created;
    private String createdBy;
    private Date updated;
    private String updatedBy;
    private String serviceNowSysId;
    private String altName;
    private String azureCustomerId;
    private Personnel accountExecutive;
    private Personnel enterpriseProgramExecutive;
    private List<Personnel> serviceDeliveryManagers = new ArrayList<Personnel>();
    private List<Personnel> businessSolutionsConsultants = new ArrayList<Personnel>();
    private List<Customer> children = new ArrayList<Customer>();

    /**
     * default CTOR
     */
    public Customer() {
    }

    public Customer(Long id, Long altId, String name, Integer contractCount, Boolean archived, String serviceNowSysId, Boolean siEnabled, String altName, String azureCustomerId,
            Date created, String createdBy, Date updated, String updatedBy) {
        this.id = id;
        this.altId = altId;
        this.name = name;
        this.contractCount = contractCount;
        this.archived = archived;
        this.serviceNowSysId = serviceNowSysId;
        this.altName = altName;
        this.azureCustomerId = azureCustomerId;
        this.created = created;
        this.createdBy = createdBy;
        this.updated = updated;
        this.updatedBy = updatedBy;
        this.siEnabled = siEnabled;
    }
    
    public Customer(Long id, Long altId, String name, String street1, String street2, String city, String state, String zip, String country, String phone, Integer contractCount, Boolean archived, String serviceNowSysId, Boolean siEnabled, 
            String altName, String azureCustomerId, Date created, String createdBy, Date updated, String updatedBy) {
        this.id = id;
        this.altId = altId;
        this.name = name;
        this.street1 = street1;
        this.street2 = street2;
        this.city = city;
        this.state = state;
        this.zip = zip;
        this.country = country;
        this.phone = phone;
        this.contractCount = contractCount;
        this.archived = archived;
        this.serviceNowSysId = serviceNowSysId;
        this.altName = altName;
        this.azureCustomerId = azureCustomerId;
        this.created = created;
        this.createdBy = createdBy;
        this.updated = updated;
        this.updatedBy = updatedBy;
        this.siEnabled = siEnabled;
    }

    public Customer(Long id, String name, String description, Boolean archived, String serviceNowSysId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.archived = archived;
        this.serviceNowSysId = serviceNowSysId;
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAltId() {
        return altId;
    }

    public void setAltId(Long altId) {
        this.altId = altId;
    }

    public Customer getParent() {
        return parent;
    }

    public void setParent(Customer parent) {
        this.parent = parent;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getStreet1() {
        return street1;
    }

    public void setStreet1(String street1) {
        this.street1 = street1;
    }

    public String getStreet2() {
        return street2;
    }

    public void setStreet2(String street2) {
        this.street2 = street2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Integer getContractCount() {
        return contractCount;
    }

    public void setContractCount(Integer contractCount) {
        this.contractCount = contractCount;
    }

    public Boolean getArchived() {
        return archived;
    }

    public void setArchived(Boolean archived) {
        this.archived = archived;
    }

    public Boolean getSiEnabled() {
        return siEnabled;
    }

    public void setSiEnabled(Boolean siEnabled) {
        this.siEnabled = siEnabled;
    }

	public String getServiceNowSysId() {
        return serviceNowSysId;
    }

    public void setServiceNowSysId(String serviceNowSysId) {
        this.serviceNowSysId = serviceNowSysId;
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

    public String getAltName() {
		return altName;
	}

	public void setAltName(String altName) {
		this.altName = altName;
	}

	public String getAzureCustomerId() {
		return azureCustomerId;
	}

	public void setAzureCustomerId(String azureCustomerId) {
		this.azureCustomerId = azureCustomerId;
	}

	public Personnel getAccountExecutive() {
		return accountExecutive;
	}

	public void setAccountExecutive(Personnel accountExecutive) {
		this.accountExecutive = accountExecutive;
	}

	public Personnel getEnterpriseProgramExecutive() {
		return enterpriseProgramExecutive;
	}

	public void setEnterpriseProgramExecutive(Personnel enterpriseProgramExecutive) {
		this.enterpriseProgramExecutive = enterpriseProgramExecutive;
	}

	public List<Personnel> getServiceDeliveryManagers() {
		return serviceDeliveryManagers;
	}

	public void setServiceDeliveryManagers(List<Personnel> serviceDeliveryManagers) {
		this.serviceDeliveryManagers = serviceDeliveryManagers;
	}

	public List<Personnel> getBusinessSolutionsConsultants() {
		return businessSolutionsConsultants;
	}

	public void setBusinessSolutionsConsultants(List<Personnel> businessSolutionsConsultants) {
		this.businessSolutionsConsultants = businessSolutionsConsultants;
	}

	public List<Customer> getChildren() {
		return children;
	}

	public void setChildren(List<Customer> children) {
		this.children = children;
	}

	@Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.id);
        hash = 17 * hash + Objects.hashCode(this.parent);
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
        final Customer other = (Customer) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!Objects.equals(this.parent, other.parent)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(Customer o) {
        if(getParent() != null && (o != null))
            return getParent().compareTo(o.getParent());
        else if(getParent() != null) {
            return 1;
        }
        if(getName() != null && (o != null))
            return getName().compareTo(o.getName());
        else if(getName() != null) {
            return 1;
        }
        if (getId() != null && o != null) {
            if (o.getId() == null) {
                return 1;
            }
            return getId().compareTo(o.getId());
        } else if (o.getId() != null) {
            return -1;
        }
        if(o != null) {
            return -1;
        }
        return 0;
    }

    @Override
	public String toString() {
		return "Customer [id=" + id + ", altId=" + altId + ", parent=" + parent + ", name=" + name + ", description="
				+ description + ", phone=" + phone + ", street1=" + street1 + ", street2=" + street2 + ", city=" + city
				+ ", state=" + state + ", zip=" + zip + ", country=" + country + ", contractCount=" + contractCount
				+ ", archived=" + archived + ", siEnabled=" + siEnabled + ", created=" + created + ", createdBy="
				+ createdBy + ", updated=" + updated + ", updatedBy=" + updatedBy + ", serviceNowSysId="
				+ serviceNowSysId + ", altName=" + altName + ", azureCustomerId=" + azureCustomerId + ", children="
				+ children + "]";
	}
}
