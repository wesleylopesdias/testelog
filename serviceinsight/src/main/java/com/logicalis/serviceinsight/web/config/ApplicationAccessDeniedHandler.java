package com.logicalis.serviceinsight.web.config;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

/**
 * Provides a place for business logic and error page configuration for an
 * access denied exception condition.
 * 
 * @author poneil
 */
public class ApplicationAccessDeniedHandler implements AccessDeniedHandler {

    private String errorPage;

    public ApplicationAccessDeniedHandler() {
    }

    public ApplicationAccessDeniedHandler(String errorPage) {
        this.errorPage = errorPage;
    }

    public String getErrorPage() {
        return errorPage;
    }

    public void setErrorPage(String errorPage) {
        this.errorPage = errorPage;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException ade) throws IOException, ServletException {
        request.setAttribute("exception", ade);
        request.setAttribute("url", request.getRequestURL());
        request.getRequestDispatcher(errorPage).forward(request, response);
    }
}
