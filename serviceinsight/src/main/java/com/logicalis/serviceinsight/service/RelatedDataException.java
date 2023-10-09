package com.logicalis.serviceinsight.service;

import java.util.Collection;

/**
 * @author poneil
 */
public class RelatedDataException extends Exception {

    Collection<? extends Object> relatedData;
    String relatedDataType;
    
    public RelatedDataException(String message) {

       super(message);
    }

    public RelatedDataException(String message, Collection relatedData, String relatedDataType) {

       super(message);
       this.relatedData = relatedData;
       this.relatedDataType = relatedDataType;
    }
    
    public Collection<? extends Object> getRelatedData() {
        return relatedData;
    }
    
    public String getRelatedDataType() {
        return relatedDataType;
    }
}
