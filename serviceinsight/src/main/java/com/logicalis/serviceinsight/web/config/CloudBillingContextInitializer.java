package com.logicalis.serviceinsight.web.config;

import javax.servlet.Filter;
import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletRegistration;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

/**
 * Web Application init class for Spring configuration
 *
 * @author poneil
 */
public class CloudBillingContextInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    private static String OS = System.getProperty("os.name").toLowerCase();
    private static String USER_HOME = System.getProperty("user.home").toLowerCase();
    
    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class<?>[]{CloudBillingServiceModelConfiguration.class};
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class[]{CloudBillingWebConfig.class, CloudBillingSecurityConfig.class};
    }

    @Override
    protected String[] getServletMappings() {
        return new String[]{"/"};
    }

    @Override
    protected Filter[] getServletFilters() {
        return new Filter[]{new HiddenHttpMethodFilter(), new OpenEntityManagerInViewFilter()};
    }

    @Override
    protected void customizeRegistration(ServletRegistration.Dynamic registration) {
        String location = "/tmp";
        if (OS.indexOf("win") >= 0) {
            location = USER_HOME + "\\Downloads";
        }
        MultipartConfigElement multipartConfigElement =
                new MultipartConfigElement(location, 1024 * 1024 * 5, 1024 * 1024 * 5 * 5, 1024 * 1024);
        registration.setMultipartConfig(multipartConfigElement);
    }
}
