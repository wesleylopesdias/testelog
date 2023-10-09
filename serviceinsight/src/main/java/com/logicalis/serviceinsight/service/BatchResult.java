package com.logicalis.serviceinsight.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author poneil
 */
public class BatchResult {
    
    public enum Operation {
        create, update, delete, unmap
    }
    
    public enum Result {
        success, failed
    }
    
    private Object id;
    private Object parentId;
    private String snSysId;
    private Long relatedObjectId;
    private Long correlationId;
    private String message;
    private Operation operation; // what was this operation?
    private Result result;
    private Metadata meta;
    
    public BatchResult(String message) {
        this.message = message;
    }
    
    public BatchResult(Object id, String message) {
        this.id = id;
        this.message = message;
    }
    
    public BatchResult(Object id, String message, Operation operation, Result result) {
        this.id = id;
        this.message = message;
        this.operation = operation;
        this.result = result;
    }
    
    public BatchResult(Object id, Object parentId, Long correlationId, String message, Operation operation, Result result) {
        this.id = id;
        this.parentId = parentId;
        this.correlationId = correlationId;
        this.message = message;
        this.operation = operation;
        this.result = result;
    }
    
    public BatchResult(Object id, Long relatedObjectId, Long correlationId, String message, Operation operation, Result result) {
        this.id = id;
        this.relatedObjectId = relatedObjectId;
        this.correlationId = correlationId;
        this.message = message;
        this.operation = operation;
        this.result = result;
    }

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    /**
     * @return the parentId
     */
    public Object getParentId() {
        return parentId;
    }

    /**
     * @param parentId the parentId to set
     */
    public void setParentId(Object parentId) {
        this.parentId = parentId;
    }

    public String getSnSysId() {
		return snSysId;
	}

	public void setSnSysId(String snSysId) {
		this.snSysId = snSysId;
	}

	public Long getRelatedObjectId() {
		return relatedObjectId;
	}

	public void setRelatedObjectId(Long relatedObjectId) {
		this.relatedObjectId = relatedObjectId;
	}

	public Long getCorrelationId() {
		return correlationId;
	}

	public void setCorrelationId(Long correlationId) {
		this.correlationId = correlationId;
	}

	public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
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
        return "BatchResult{" + "id=" + id + ", parentId=" + parentId + ", snSysId=" + snSysId + ", relatedObjectId=" + relatedObjectId + ", correlationId=" + correlationId + ", message=" + message + ", operation=" + operation + ", result=" + result + '}';
    }
}
