package com.logicalis.serviceinsight.representation;

import java.util.Objects;

/**
 * Makes a representation for passing reference CostItem type and subtype information to the UI
 * 
 * @author poneil
 */
public class CostItemTypeSubType implements Comparable<CostItemTypeSubType> {
    
    private String key;
    private String type;
    private String typeLabel;
    private String subtype;
    private String subtypeLabel;
    
    public CostItemTypeSubType(String type, String typeLabel, String subtype, String subtypeLabel) {
        this.type = type;
        this.typeLabel = typeLabel;
        this.subtype = subtype;
        this.subtypeLabel = subtypeLabel;
        setKey();
    }

    public String getKey() {
        return key;
    }
    
    private void setKey() {
        if (subtype == null) {
            key = type;
        } else {
            key = type + "_" + subtype;
        }
    }

    public String getTypeLabel() {
        return typeLabel;
    }

    public String getSubtypeLabel() {
        return subtypeLabel;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.key);
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
        final CostItemTypeSubType other = (CostItemTypeSubType) obj;
        if (!Objects.equals(this.key, other.key)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(CostItemTypeSubType o) {
        return this.key.compareTo(o.getKey());
    }
}
