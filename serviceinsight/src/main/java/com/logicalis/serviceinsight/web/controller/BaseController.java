package com.logicalis.serviceinsight.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logicalis.serviceinsight.service.ServiceException;
import com.logicalis.serviceinsight.web.WSException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.logging.Level;


import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

/**
 * a base class for providing common Controller infrastructure
 *
 * @author poneil
 */
public abstract class BaseController {

    final Logger log = LoggerFactory.getLogger(getClass());
    @Autowired
    MessageSource messageSource;
    protected JdbcTemplate jdbcTemplate; // because why the hell not?!
    @Value("${application.timezone}")
    protected String TZID;
    @Value("${partnercenter.domain}")
    protected String partnerCenterDomain;
    @Value("${partnercenter.tenant.id}")
    protected String partnerTenantId;
    @Value("${azure.api.version}")
    protected String azureAPIVersion;
    @Value("${azure.mfa.auth_redirect}")
    protected String azureMFARedirect;
    @Value("${azure.mfa.auth_redirect.params}")
    protected String azureMFARedirectParams;
    @Value("${azure.application.redirecthost}")
    protected String azureRedirectHost;
    @Value("${azure.customer_cost_access_application.webapp.id}")
    protected String ccaaWebAppId;
    @Value("${azure.customer_cost_access_application.webapp.secret}")
    protected String ccaaWebAppSecret;
    @Value("${partnercenter.webapp.id}")
    protected String pcWebAppId;
    @Value("${partnercenter.webapp.key}")
    protected String pcWebAppSecret;
    @Value("${azure.dev_application.webapp.id}")
    protected String devWebAppId;
    @Value("${azure.dev_application.webapp.secret}")
    protected String devWebAppSecret;
    @Value("${pc.dev_application.webapp.id}")
    protected String devPcWebAppId;
    @Value("${pc.dev_application.webapp.secret}")
    protected String devPcWebAppSecret;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    protected String authenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            Object userPrincipal = auth.getPrincipal();
            if (userPrincipal != null && userPrincipal instanceof User) {
                return ((User) userPrincipal).getUsername();
            } else if (userPrincipal != null && userPrincipal instanceof String) {
                log.info("userPrincipal String: [{}]", userPrincipal.toString());
                // this scenario isn't a valid application "authenticated user"
                return null;
            }
        }
        return null;
    }
    
    protected String mfa(String localPath, HttpServletRequest httpServletRequest) {
        String authURL = String.format(azureMFARedirect, new Object[]{ccaaWebAppId}) + azureMFARedirectParams;
        String redirectURIParam = "&redirect_uri=" + generalEncoding(azureRedirectHost + localPath, httpServletRequest);
        log.debug("constructed mfa auth redirect URL as: {}", authURL + redirectURIParam);
        return authURL + redirectURIParam;
    }

    @ResponseBody
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    @ExceptionHandler(WSException.class)
    public APIResponse handleWebServiceException(WSException wse, HttpServletRequest httpServletRequest) {
        APIResponse errorResponse = new APIResponse(APIResponse.Status.ERROR,
                (wse.getFieldMessage() != null ? wse.getFieldMessage() : wse.getMessage()));
        errorResponse.newMeta().addException(wse);
        log.info(errorResponse.toString());
        return errorResponse;
    }

    @ResponseBody
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    @ExceptionHandler(ResourceAccessException.class)
    public APIResponse handleResourceAccessException(ResourceAccessException rae, HttpServletRequest httpServletRequest) {
        APIResponse errorResponse = new APIResponse(APIResponse.Status.ERROR, rae.getMessage());
        errorResponse.newMeta().addException(rae);
        log.info(errorResponse.toString());
        return errorResponse;
    }

    @ResponseBody
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    @ExceptionHandler(ServiceException.class)
    public APIResponse handleGeneralServiceException(ServiceException se, HttpServletRequest httpServletRequest) {
        log.info("SI ServiceException thrown!", se);
        APIResponse errorResponse = new APIResponse(APIResponse.Status.ERROR, se.getMessage());
        errorResponse.newMeta().addException(se);
        log.info(errorResponse.toString());
        return errorResponse;
    }

    @ResponseBody
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    @ExceptionHandler(com.logicalis.pcc.ServiceException.class)
    public ModelAndView handlePCCServiceException(com.logicalis.ap.ServiceException se, HttpServletRequest httpServletRequest) {
        ModelAndView mav = new ModelAndView("unauthorized");
        mav.addObject("exception", se);
        mav.addObject("url", httpServletRequest.getRequestURL());
        return mav;
    }

    @ResponseBody
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    @ExceptionHandler(com.logicalis.ap.ServiceException.class)
    public ModelAndView handleAPServiceException(com.logicalis.ap.ServiceException se, HttpServletRequest httpServletRequest) {
        ModelAndView mav = new ModelAndView("unauthorized");
        mav.addObject("exception", se);
        mav.addObject("url", httpServletRequest.getRequestURL());
        return mav;
    }

    @ResponseBody
    @ResponseStatus(BAD_GATEWAY)
    @ExceptionHandler(HttpServerErrorException.class)
    public APIResponse handleBadGatewayException(HttpServerErrorException se, HttpServletRequest httpServletRequest) {
        APIResponse errorResponse = new APIResponse(APIResponse.Status.ERROR, se.getMessage());
        errorResponse.newMeta().addException(se);
        log.info(logObject(errorResponse));
        return errorResponse;
    }

    @ResponseBody
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    @ExceptionHandler(UnexpectedRollbackException.class)
    public APIResponse handleUnexpectedRollbackException(UnexpectedRollbackException ure, HttpServletRequest httpServletRequest) {
        APIResponse errorResponse = new APIResponse(APIResponse.Status.ERROR, ure.getMessage());
        errorResponse.newMeta().addException(ure);
        log.info(errorResponse.toString());
        return errorResponse;
    }

    @ResponseStatus(UNAUTHORIZED)
    @ExceptionHandler(AccessDeniedException.class)
    public ModelAndView handleAccessDeniedException(AccessDeniedException ade, HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("unauthorized");
        mav.addObject("exception", ade);
        mav.addObject("url", request.getRequestURL());
        return mav;
    }
    
    private String logObject(Object obj) {
        try {
            return "\n"+objectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException ex) {
            java.util.logging.Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public String generalEncoding(String value, HttpServletRequest httpServletRequest) {
        String enc = httpServletRequest.getCharacterEncoding();
        if (enc == null) {
            enc = WebUtils.DEFAULT_CHARACTER_ENCODING;
        }
        try {
            return URLEncoder.encode((value == null ? "unspecified" : value), enc);
        } catch (UnsupportedEncodingException ex) {
            java.util.logging.Logger.getLogger(BaseController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return value;
    }

    String encodeQueryParameter(String queryParam, HttpServletRequest httpServletRequest) {
        String enc = httpServletRequest.getCharacterEncoding();
        if (enc == null) {
            enc = WebUtils.DEFAULT_CHARACTER_ENCODING;
        }
        try {
            queryParam = UriUtils.encodeQueryParam(queryParam, enc);
        } catch (UnsupportedEncodingException uee) {
        }
        return queryParam;
    }
    
    private ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
