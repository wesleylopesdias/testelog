package com.logicalis.serviceinsight.web;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.codec.binary.Base64;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.Assert;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * A convenient Spring RestTemplate client, supporting setup for ServiceNow
 * RESTful web-service calls
 */
public class SNWSClient {

    private RestTemplate restTemplate;
    private String target;

    final Logger log = LoggerFactory.getLogger(SNWSClient.class);
    
    public enum SNPath {
        changeRequest ("/api/now/v1/table/change_request"),
        incidents ("/api/now/v1/table/incident");
        
        private final String path;
        
        SNPath(String path) {
            this.path = path;
        }
        
        public String getPath() { return this.path; }
    }

    public SNWSClient(String target, String username, String password) {
        Assert.notNull(target, "SN target cannot be null");
        this.target = target;
        Base64 encoder = new Base64();
        String authString = new StringBuilder(username)
                .append(":")
                .append(password)
                .toString();
        String encoded = new String(encoder.encode(authString.getBytes()));
        restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(new SNWSClientHttpRequestFactory(new SimpleClientHttpRequestFactory(), encoded));
        restTemplate.setErrorHandler(new ErrorHandler());
    }

    private class SNWSClientHttpRequestFactory implements ClientHttpRequestFactory {
        private ClientHttpRequestFactory delegate;
        private String auth;
        public SNWSClientHttpRequestFactory(ClientHttpRequestFactory delegate, String suth) {
            this.delegate = delegate;
            this.auth = suth;
        }

        @Override
        public ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod) throws IOException {
            ClientHttpRequest request = delegate.createRequest(uri, httpMethod);
            request.getHeaders().add("Authorization", "Basic "+auth);
            request.getHeaders().add("Content-Type", "application/json");
            request.getHeaders().add("Accept", "application/json");
            return request;
        }
    }
    
    private class ErrorHandler extends DefaultResponseErrorHandler {
        @Override
        public void handleError(ClientHttpResponse response) throws IOException {
            HttpStatus statusCode = response.getStatusCode();
            MediaType contentType = response.getHeaders().getContentType();
            switch (statusCode.series()) {
                case CLIENT_ERROR:
                    WSException exception;
                    String responseBody = null;
                    try {
                        if(contentType != null) {
                            log.info("errorHandler information: processing type/subtype [{}/{}]",
                                    new Object[]{contentType.getType(), contentType.getSubtype()});

                            if(MediaType.APPLICATION_JSON.getType().equals(contentType.getType()) &&
                                    MediaType.APPLICATION_JSON.getSubtype().equals(contentType.getSubtype())) {
                                responseBody = IOUtils.toString(response.getBody());
                                exception = new WSException(statusCode, response.getStatusText());
                                exception.setContentType(contentType);
                                exception.setFieldMessage(responseBody);
                                exception.setFieldError(true);
                            } else {
                                exception = new WSException(statusCode, response.getStatusText(),
                                        IOUtils.toByteArray(response.getBody()), null);
                                exception.setContentType(contentType);
                            }
                        } else
                            exception = new WSException(statusCode, response.getStatusText(),
                                    IOUtils.toByteArray(response.getBody()), null);
                        throw exception;
                    } catch (IOException ioe) {
                        log.debug("An IOException inspecting the response", ioe);
                        throw new WSException(statusCode, response.getStatusText());
                    }
                case SERVER_ERROR:
                    throw new HttpServerErrorException(statusCode, response.getStatusText());
                default:
                    throw new RestClientException("Unknown status code [" + statusCode + "]");
            }
        }
    }
    
    public <T> List<T> getListOf(String path, Class<T> entityType) throws WSException {
        List ids = restTemplate.getForObject(target+path, List.class);
        List<T> entities = new ArrayList<T>();
        for(Object obj : ids)
            entities.add(getSingle(path, entityType, (String) obj));
        return entities;
    }

    public <T> T getSingle(String path, Class<T> singleType, String id) throws WSException {
        return restTemplate.getForObject(target+path+"/{id}", singleType, id);
    }
    
    public String save(String path, Object obj) throws WSException {
        String result = restTemplate.postForObject(target+path+"/", obj, String.class);
        if(result != null && 2 < result.length())
            // strip quotes off return value
            return result.substring(1, result.length()-1);
        return result;
    }
    
    public void update(String path, Object obj) throws WSException {
        restTemplate.put(target+path+"/", obj);
    }
    
    public void delete(String path, String id) throws WSException {
        restTemplate.delete(target+path+"/{id}", id);
    }
}
