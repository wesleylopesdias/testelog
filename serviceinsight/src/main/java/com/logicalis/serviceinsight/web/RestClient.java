package com.logicalis.serviceinsight.web;

import com.logicalis.serviceinsight.data.PipelineQuoteMin;
import com.logicalis.serviceinsight.data.PricingCustomerMin;
import com.logicalis.serviceinsight.data.PricingProductMin;
import com.logicalis.serviceinsight.data.QuoteMin;
import com.logicalis.serviceinsight.data.ServiceMin;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * A convenient Spring RestTemplate client, supporting setup for any
 * RESTful web-service calls
 */
public class RestClient {

    static final Logger log = LoggerFactory.getLogger(RestClient.class);

    private static class RestClientHttpRequestFactory implements ClientHttpRequestFactory {
        private ClientHttpRequestFactory delegate;
        private String auth;
        public RestClientHttpRequestFactory(ClientHttpRequestFactory delegate, String suth) {
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
    
    private static class ErrorHandler extends DefaultResponseErrorHandler {
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
                            log.info("errorHandler information: processing type/subtype [{}/{}],"
                                    + " status code {}, status text {}",
                                    new Object[]{contentType.getType(), contentType.getSubtype(),
                                    response.getStatusCode(), response.getStatusText()});

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
    
    public static ResponseEntity<List<ServiceMin>> getListOfOspServices(String username, String password, String url) throws WSException {
        return authenticatingRestTemplate(username, password)
                .exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<ServiceMin>>() {});
    }
    
    public static ResponseEntity<QuoteMin> getQuoteById(String username, String password, String url) throws WSException {
        return authenticatingRestTemplate(username, password)
                .exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<QuoteMin>() {});
    }
    
    public static ResponseEntity<QuoteMin> markQuoteAsWon(String username, String password, String url) throws WSException {
        return authenticatingRestTemplate(username, password)
                .exchange(url, HttpMethod.PUT, null, new ParameterizedTypeReference<QuoteMin>() {});
    }
    
    public static ResponseEntity<List<QuoteMin>> getListOfQuotesForCustomer(String username, String password, String url) throws WSException {
        return authenticatingRestTemplate(username, password)
                .exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<QuoteMin>>() {});
    }
    
    public static ResponseEntity<List<PipelineQuoteMin>> getListOfQuotes(String username, String password, String url) throws WSException {
        return authenticatingRestTemplate(username, password)
                .exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<PipelineQuoteMin>>() {});
    }
    
    public static ResponseEntity<List<PricingCustomerMin>> getListOfPricingCustomers(String username, String password, String url) throws WSException {
        return authenticatingRestTemplate(username, password)
                .exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<PricingCustomerMin>>() {});
    }
    
    public static ResponseEntity<List<PricingProductMin>> getListOfPricingProducts(String username, String password, String url) throws WSException {
        return authenticatingRestTemplate(username, password)
                .exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<PricingProductMin>>() {});
    }
    
    public static ResponseEntity<List<PricingProductMin>> getListOfM365Products(String username, String password, String url) throws WSException {
        return authenticatingRestTemplate(username, password)
                .exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<PricingProductMin>>() {});
    }
    
    public static ResponseEntity<List<PricingProductMin>> getListOfProductsByType(String username, String password, String url) throws WSException {
        return authenticatingRestTemplate(username, password)
                .exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<PricingProductMin>>() {});
    }
    
    public static RestTemplate authenticatingRestTemplate(String username, String password) {
        RestTemplate rt = new RestTemplate();
        rt.setRequestFactory(new RestClientHttpRequestFactory(new SimpleClientHttpRequestFactory(),
                getEncodedAuth(username, password)));
        rt.setErrorHandler(new ErrorHandler());
        return rt;
    }
    
    private static String getEncodedAuth(String username, String password) {
        Base64 encoder = new Base64();
        String authString = new StringBuilder(username)
                .append(":")
                .append(password)
                .toString();
        return new String(encoder.encode(authString.getBytes()));
    }

    public static <T> T getSingleForId(String username, String password, String url, Class<T> singleType, String id) throws WSException {
        return authenticatingRestTemplate(username, password)
                .getForObject(url+"/{id}", singleType, id);
    }

    public static <T> T getSingle(String username, String password, String url, Class<T> singleType) throws WSException {
        return authenticatingRestTemplate(username, password)
                .getForObject(url, singleType);
    }
    
    public static ResponseEntity<Object[]> getArrayOf(String username, String password, String url) throws WSException {
        return authenticatingRestTemplate(username, password)
                .getForEntity(url, Object[].class);
    }
    
    public static String save(String username, String password, String url, Object obj) throws WSException {
        String result = authenticatingRestTemplate(username, password).postForObject(url+"/", obj, String.class);
        if(result != null && 2 < result.length())
            // strip quotes off return value
            return result.substring(1, result.length()-1);
        return result;
    }
    
    public static void update(String username, String password, String url, Object obj) throws WSException {
        authenticatingRestTemplate(username, password).put(url+"/", obj);
    }
    
    public static void delete(String username, String password, String url, String id) throws WSException {
        authenticatingRestTemplate(username, password).delete(url+"/{id}", id);
    }
}
