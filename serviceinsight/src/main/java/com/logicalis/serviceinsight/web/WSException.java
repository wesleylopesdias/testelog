package com.logicalis.serviceinsight.web;

import java.nio.charset.Charset;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;

/**
 * Custom exception class for storing relevant information stemming from a
 * handled error response from remote APIs
 */
public class WSException extends HttpClientErrorException {

    private static final long serialVersionUID = 1L;
    
    private MediaType contentType;
    private boolean fieldError = false;
    private String fieldMessage;

    public WSException(HttpStatus statusCode) {
        super(statusCode);
    }

    public WSException(HttpStatus statusCode, String statusText) {
        super(statusCode, (statusText == null ? "Status Unknown" : statusText));
    }

    public WSException(HttpStatus statusCode, String statusText, byte[] responseBody,
            Charset responseCharset) {
        super(statusCode, (statusText == null ? "Status Unknown" : statusText), responseBody, responseCharset);
    }
    
    public WSException(HttpStatus statusCode, String statusText, byte[] responseBody,
            Charset responseCharset, MediaType contentType, String fieldMessage) {
        super(statusCode, (statusText == null ? "Status Unknown" : statusText), responseBody, responseCharset);
        this.contentType = contentType;
        this.fieldMessage = fieldMessage;
    }
    
    protected void setFieldError(boolean fieldError) {
        this.fieldError = fieldError;
    }
    
    public boolean getFieldError() {
        return this.fieldError;
    }
    
    public MediaType getContentType() {
        return contentType;
    }

    protected void setContentType(MediaType contentType) {
        this.contentType = contentType;
    }

    public String getFieldMessage() {
        return fieldMessage;
    }

    protected void setFieldMessage(String fieldMessage) {
        this.fieldMessage = fieldMessage;
    }
}
