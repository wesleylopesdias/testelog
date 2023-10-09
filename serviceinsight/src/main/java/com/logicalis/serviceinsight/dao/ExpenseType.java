package com.logicalis.serviceinsight.dao;

/**
 * Asset types allow an asset item to be classified before it is
 * entered into the system.
 * 
 * example: an asset item "X86 Server" is a "Server" type
 * 
 * @author poneil
 */
public class ExpenseType extends BaseDao {
	
    private Integer id;
    private String name;
    private String description;
    private Long expenseTypeRefId;

    public ExpenseType() {
    }
    
    public ExpenseType(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    public ExpenseType(Integer id, String name, String description, Long expenseTypeRefId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.expenseTypeRefId = expenseTypeRefId;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

	public Long getExpenseTypeRefId() {
		return expenseTypeRefId;
	}

	public void setExpenseTypeRefId(Long expenseTypeRefId) {
		this.expenseTypeRefId = expenseTypeRefId;
	}
    
}
