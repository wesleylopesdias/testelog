package com.logicalis.serviceinsight.dao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Typing of costs, such as "rent", "utilities", "licenses", "SPLA".
 *
 * @author poneil
 */
public class ExpenseCategory extends BaseDao {

    private Integer id;
    private ExpenseCategory parent;
    private String name;
    private String description;
    private BigDecimal targetUtilization;
    private String units;
    private BigDecimal laborSplit;
    private List<ExpenseCategory> subcategories = new ArrayList<ExpenseCategory>();

    public ExpenseCategory() {
    }

    public ExpenseCategory(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public ExpenseCategory(Integer id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public ExpenseCategory(Integer id, String name, String description, BigDecimal targetUtilization, String units, BigDecimal laborSplit) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.targetUtilization = targetUtilization;
        this.units = units;
        this.laborSplit = laborSplit;
    }

    public ExpenseCategory(Integer id, ExpenseCategory parent, String name, String description, BigDecimal targetUtilization, String units, BigDecimal laborSplit) {
        this.id = id;
        this.parent = parent;
        this.name = name;
        this.description = description;
        this.targetUtilization = targetUtilization;
        this.units = units;
        this.laborSplit = laborSplit;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public ExpenseCategory getParent() {
        return parent;
    }

    public void setParent(ExpenseCategory parent) {
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

    public BigDecimal getTargetUtilization() {
        return targetUtilization;
    }

    public void setTargetUtilization(BigDecimal targetUtilization) {
        this.targetUtilization = targetUtilization;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public BigDecimal getLaborSplit() {
        return laborSplit;
    }

    public void setLaborSplit(BigDecimal laborSplit) {
        this.laborSplit = laborSplit;
    }

    public List<ExpenseCategory> getSubcategories() {
        return subcategories;
    }

    public void setSubcategories(List<ExpenseCategory> subcategories) {
        this.subcategories = subcategories;
    }
    
    public void addSubcategory(ExpenseCategory subcategory) {
        if (this.subcategories == null) {
            this.subcategories = new ArrayList<ExpenseCategory>();
        }
        this.subcategories.add(subcategory);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ExpenseCategory other = (ExpenseCategory) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ExpenseCategory{" + "id=" + id + ", parent=" + parent + ", name=" + name + ", description=" + description + ", targetUtilization=" + targetUtilization + ", units=" + units + ", laborSplit=" + laborSplit + '}';
    }
    
    public static Comparator<ExpenseCategory> ExpenseCategoryNameComparator = new Comparator<ExpenseCategory>() {
		public int compare(ExpenseCategory categoryOne, ExpenseCategory categoryTwo) {
			String categoryNameOne = categoryOne.getName().toUpperCase();
			String categoryNameTwo = categoryTwo.getName().toUpperCase();
			
			if(categoryOne.getParent() != null) {
				categoryNameOne = categoryOne.getParent().getName().toUpperCase() + categoryNameOne;
			}
			if(categoryTwo.getParent() != null) {
				categoryNameTwo = categoryTwo.getParent().getName().toUpperCase() + categoryNameTwo;
			}
			
			//ascending order
			return categoryNameOne.compareTo(categoryNameTwo);
		}
	};
}
