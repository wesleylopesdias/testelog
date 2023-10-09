package com.logicalis.serviceinsight.data;

import java.math.BigDecimal;

/**
 *
 * @author poneil
 */
public class GeneralCost {
    
    public enum Allocation {
        SP, MS, BL, CL, Equip
    }
    
    private Long id;
    private String name;
    private String description;
    private Allocation allocation;
    private BigDecimal sixMonthAverage;
    private String notes;

    /**
     * default CTOR
     */
    public GeneralCost() {
    }

    /**
     * populate props CTOR
     */
    public GeneralCost(Long id, String name, String description, Allocation allocation, BigDecimal sixMonthAverage, String notes) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.allocation = allocation;
        this.sixMonthAverage = sixMonthAverage;
        this.notes = notes;
    }

    /**
     * populate props CTOR
     */
    public GeneralCost(Long id, String name, String description, String allocationName, BigDecimal sixMonthAverage, String notes) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.allocation = Allocation.valueOf(allocationName);
        this.sixMonthAverage = sixMonthAverage;
        this.notes = notes;
    }
    
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
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the allocation
     */
    public Allocation getAllocation() {
        return allocation;
    }

    /**
     * @param allocation the allocation to set
     */
    public void setAllocation(Allocation allocation) {
        this.allocation = allocation;
    }

    /**
     * @return the sixMonthAverage
     */
    public BigDecimal getSixMonthAverage() {
        return sixMonthAverage;
    }

    /**
     * @param sixMonthAverage the sixMonthAverage to set
     */
    public void setSixMonthAverage(BigDecimal sixMonthAverage) {
        this.sixMonthAverage = sixMonthAverage;
    }

    /**
     * @return the notes
     */
    public String getNotes() {
        return notes;
    }

    /**
     * @param notes the notes to set
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }
}
