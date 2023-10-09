package com.logicalis.serviceinsight.web.controller;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.logicalis.serviceinsight.service.BatchResult;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author poneil
 */
public class APIResponse {
    
    public enum Status {
        OK, ERROR, RELATED_DATA_FOUND
    }
    
    private String message;
    private Status status;
    private List<BatchResult> batchResults = new ArrayList<BatchResult>();
    private Collection<? extends Object> data;
    private String dataType;
    private Metadata meta;
    
    public APIResponse(Status status, String message) {
        this.message = message;
        this.status = status;
    }
    
    public APIResponse(Status status, String message, List<BatchResult> batchResults) {
        this.message = message;
        this.status = status;
        addBatchResults(batchResults);
    }
    
    public APIResponse(Status status, String message, Collection data, String dataType) {
        this.message = message;
        this.status = status;
        this.data = data;
        this.dataType = dataType;
    }
    
    public APIResponse(Status status, String message, Exception e) {
        this.message = message;
        this.status = status;
        newMeta().addException(e);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<BatchResult> getBatchResults() {
        return batchResults;
    }
    
    private void addBatchResults(List<BatchResult> batchResults) {
        this.batchResults.addAll(batchResults);
    }
    
    public Collection<? extends Object> getData() {
        return data;
    }
    
    public String getDataType() {
        return dataType;
    }
    
    public Metadata getMeta() {
        return meta;
    }

    public void setMeta(Metadata meta) {
        this.meta = meta;
    }

    @JsonIgnore
    public Metadata newMeta() {
        meta = new Metadata();
        return meta;
    }
    
    public static class Metadata {

        private String errorMessage;
        private List<String> stackTrace = new ArrayList<String>();

        public Metadata() {
            super();
        }

        public List<String> getStackTrace() {
            return stackTrace;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setStackTrace(List<String> stackTrace) {
            this.stackTrace = stackTrace;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        @JsonIgnore
        public void addException(Exception e) {
            errorMessage = e.getMessage();
            for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                addStackTrace(stackTraceElement);
            }
        }

        @JsonIgnore
        public void addStackTrace(StackTraceElement stackTraceElement) {
            stackTrace.add(stackTraceElement.toString());
        }
    }

    @Override
    public String toString() {
        return "APIResponse{" + "message=" + message + ", status=" + status + ", batchResults=" + batchResults + ", meta=" + meta + '}';
    }
}
