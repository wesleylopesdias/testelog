package com.logicalis.serviceinsight.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PricingCustomerMin implements Comparable<PricingCustomerMin> {

    private Long id;
    private PricingCustomerMin parent;
    private String altId;
    private String name;
    private String description;
    private String phone;
    private String street1;
    private String street2;
    private String city;
    private String state;
    private String zip;
    private String country;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PricingCustomerMin getParent() {
        return parent;
    }

    public void setParent(PricingCustomerMin parent) {
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAltId() {
        return altId;
    }

    public void setAltId(String altId) {
        this.altId = altId;
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

    @Override
    public int hashCode() {
        int hash = 5;
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
        final PricingCustomerMin other = (PricingCustomerMin) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!Objects.equals(this.parent, other.parent)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(PricingCustomerMin o) {
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
        return "PricingCustomerMin{" + "id=" + id + ", parent=" + parent + ", altId=" + altId + ", name=" + name + ", description=" + description + ", phone=" + phone + ", street1=" + street1 + ", street2=" + street2 + ", city=" + city + ", state=" + state + ", zip=" + zip + ", country=" + country + '}';
    }
}
