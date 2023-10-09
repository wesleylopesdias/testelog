package com.logicalis.serviceinsight.dao;

/**
 *
 * @author poneil
 */
public class ExpenseTypeRef extends BaseDao {
    
    public enum ExpenseTypeRefValue {
        asset, cost
    }
    
    private Integer id;
    private String name;
    private String displayName;
    private String description;

    public ExpenseTypeRef() {
        
    }
    
    public ExpenseTypeRef(String name, String displayName, String description) {
        this.name = ExpenseTypeRefValue.valueOf(name).name();
        this.displayName = displayName;
        this.description = description;
    }
    
    public ExpenseTypeRef(Integer id, String name, String displayName, String description) {
        this.id = id;
        this.name = ExpenseTypeRefValue.valueOf(name).name();
        this.displayName = displayName;
        this.description = description;
    }
    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
