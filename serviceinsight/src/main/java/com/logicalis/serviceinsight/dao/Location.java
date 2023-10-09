package com.logicalis.serviceinsight.dao;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;

/**
 *
 * @author poneil
 */
public class Location {
    private Integer id;
    private Location parent;
    private String name;
    private String description;
    private String altName;
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="MMddyyyy", timezone="America/New_York")
    private Date created;
    private String createdBy;

    /**
     * default CTOR
     */
    public Location() {
        
    }

    public Location(Integer id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public Location(Integer id, String name, String altName, String description, Date created, String createdBy) {
        this.id = id;
        this.name = name;
        this.altName = altName;
        this.description = description;
        this.created = created;
        this.createdBy = createdBy;
    }
    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Location getParent() {
        return parent;
    }

    public void setParent(Location parent) {
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

    public String getAltName() {
		return altName;
	}

	public void setAltName(String altName) {
		this.altName = altName;
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
}
