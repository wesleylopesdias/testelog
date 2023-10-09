package com.logicalis.serviceinsight.web.config;

import javax.servlet.ServletContext;
import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;
import org.springframework.web.multipart.support.MultipartFilter;

/**
 *
 * @author poneil
 */
public class CloudBillingSecurityWebApplicationInitializer extends AbstractSecurityWebApplicationInitializer {
    // default behavior...

    @Override
    protected void beforeSpringSecurityFilterChain(ServletContext servletContext) {
        insertFilters(servletContext, new MultipartFilter());
    }
}
