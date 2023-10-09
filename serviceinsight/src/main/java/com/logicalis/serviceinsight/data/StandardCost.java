package com.logicalis.serviceinsight.data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 *
 * @author poneil
 */
public class StandardCost {
    private String category;
    private String name;
    private BigDecimal monthlyCost;
    private BigDecimal units;
    private BigDecimal baseStandard;
    private double targetUtilization;
    private double genEquipBurdenRate;
    private double cloudOverheadBurdenRate;
    private BigDecimal standardCost;

    /**
     * default CTOR
     */
    public StandardCost() {
        
    }

    /**
     * populate attributes CTOR
     */
    public StandardCost(String category, String name, BigDecimal monthlyCost, BigDecimal units,
            BigDecimal baseStandard, double targetUtilization, double genEquipBurdenRate,
            double cloudOverheadBurdenRate) {
        this.category = category;
        this.name = name;
        if (monthlyCost != null) {
            this.monthlyCost = monthlyCost.setScale(2, RoundingMode.HALF_UP);
        }
        if (units != null) {
            this.units = units.setScale(2, RoundingMode.HALF_UP);
        }
        if (baseStandard != null) {
            this.baseStandard = baseStandard.setScale(2, RoundingMode.HALF_UP);
        }
        this.targetUtilization = targetUtilization;
        this.genEquipBurdenRate = genEquipBurdenRate;
        this.cloudOverheadBurdenRate = cloudOverheadBurdenRate;
    }

    /**
     * @return the category
     */
    public String getCategory() {
        return category;
    }

    /**
     * @param category the category to set
     */
    public void setCategory(String category) {
        this.category = category;
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
     * @return the monthlyCost
     */
    public BigDecimal getMonthlyCost() {
        return monthlyCost;
    }

    /**
     * @param monthlyCost the monthlyCost to set
     */
    public void setMonthlyCost(BigDecimal monthlyCost) {
        this.monthlyCost = monthlyCost;
    }

    /**
     * @return the units
     */
    public BigDecimal getUnits() {
        return units;
    }

    /**
     * @param units the units to set
     */
    public void setUnits(BigDecimal units) {
        this.units = units;
    }

    /**
     * @return the baseStandard
     */
    public BigDecimal getBaseStandard() {
        return baseStandard;
    }

    /**
     * @param baseStandard the baseStandard to set
     */
    public void setBaseStandard(BigDecimal baseStandard) {
        this.baseStandard = baseStandard;
    }

    /**
     * @return the targetUtilization
     */
    public double getTargetUtilization() {
        return targetUtilization;
    }

    /**
     * @param targetUtilization the targetUtilization to set
     */
    public void setTargetUtilization(double targetUtilization) {
        this.targetUtilization = targetUtilization;
    }

    /**
     * @return the genEquipBurdenRate
     */
    public double getGenEquipBurdenRate() {
        return genEquipBurdenRate;
    }

    /**
     * @param genEquipBurdenRate the genEquipBurdenRate to set
     */
    public void setGenEquipBurdenRate(double genEquipBurdenRate) {
        this.genEquipBurdenRate = genEquipBurdenRate;
    }

    /**
     * @return the cloudOverheadBurdenRate
     */
    public double getCloudOverheadBurdenRate() {
        return cloudOverheadBurdenRate;
    }

    /**
     * @param cloudOverheadBurdenRate the cloudOverheadBurdenRate to set
     */
    public void setCloudOverheadBurdenRate(double cloudOverheadBurdenRate) {
        this.cloudOverheadBurdenRate = cloudOverheadBurdenRate;
    }

    /**
     * @return the standardCost
     */
    public BigDecimal getStandardCost() {
        standardCost = baseStandard.divide(new BigDecimal(targetUtilization), 2, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(1+this.genEquipBurdenRate+this.cloudOverheadBurdenRate));
        return standardCost.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + Objects.hashCode(this.category);
        hash = 37 * hash + Objects.hashCode(this.name);
        hash = 37 * hash + Objects.hashCode(this.monthlyCost);
        hash = 37 * hash + Objects.hashCode(this.units);
        hash = 37 * hash + Objects.hashCode(this.baseStandard);
        hash = 37 * hash + (int) (Double.doubleToLongBits(this.targetUtilization) ^ (Double.doubleToLongBits(this.targetUtilization) >>> 32));
        hash = 37 * hash + (int) (Double.doubleToLongBits(this.genEquipBurdenRate) ^ (Double.doubleToLongBits(this.genEquipBurdenRate) >>> 32));
        hash = 37 * hash + (int) (Double.doubleToLongBits(this.cloudOverheadBurdenRate) ^ (Double.doubleToLongBits(this.cloudOverheadBurdenRate) >>> 32));
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
        final StandardCost other = (StandardCost) obj;
        if (!Objects.equals(this.category, other.category)) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.monthlyCost, other.monthlyCost)) {
            return false;
        }
        if (!Objects.equals(this.units, other.units)) {
            return false;
        }
        if (!Objects.equals(this.baseStandard, other.baseStandard)) {
            return false;
        }
        if (Double.doubleToLongBits(this.targetUtilization) != Double.doubleToLongBits(other.targetUtilization)) {
            return false;
        }
        if (Double.doubleToLongBits(this.genEquipBurdenRate) != Double.doubleToLongBits(other.genEquipBurdenRate)) {
            return false;
        }
        if (Double.doubleToLongBits(this.cloudOverheadBurdenRate) != Double.doubleToLongBits(other.cloudOverheadBurdenRate)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "StandardCost{" + "category=" + category + ", name=" + name + ", monthlyCost=" + monthlyCost + ", units=" + units + ", baseStandard=" + baseStandard + ", targetUtilization=" + targetUtilization + ", genEquipBurdenRate=" + genEquipBurdenRate + ", cloudOverheadBurdenRate=" + cloudOverheadBurdenRate + ", standardCost=" + standardCost + '}';
    }
}
