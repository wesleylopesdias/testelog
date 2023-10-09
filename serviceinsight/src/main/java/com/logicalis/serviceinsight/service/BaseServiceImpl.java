package com.logicalis.serviceinsight.service;

import com.google.common.base.Optional;

import static com.google.common.base.Optional.fromNullable;
import static java.lang.String.format;

import java.util.Locale;

import javax.sql.DataSource;

import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

/**
 * Provides basic infrastructure or service methods
 *
 * @author poneil
 */
public abstract class BaseServiceImpl implements BaseService {

    protected NamedParameterJdbcTemplate namedJdbcTemplate;
    protected JdbcTemplate jdbcTemplate;
    protected final Logger log = LoggerFactory.getLogger(getClass());
    @Autowired
    protected MessageSource messageSource;
    @Value("${application.timezone}")
    protected String TZID;
    @Value("${admin.email}")
    protected String siAdmin;
    @Value("${noreply.email}")
    protected String noReplyEmail;
    @Value("${templates.location}")
    protected String baseTemplateLocation;
    @Value("${email.image.logo}")
    protected String emailLogo;
    @Value("${http.host}")
    protected String httpHost;
    @Value("${mail.encoding}")
    String mailEncoding;
    
    @Autowired
    protected JavaMailSender mailSender;
    @Autowired
    protected VelocityEngine velocityEngine;

    protected enum months {

        January, February, March, April, May, June, July, August, September, October, November, December
    }

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.namedJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    protected String authenticatedUser() {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            Object userPrincipal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            if (userPrincipal instanceof User) {
                return ((User) userPrincipal).getUsername();
            }
        }
        return "unknown";
    }

    /**
     * Search for the locale-specific vm template, else fallback to default
     * template.
     *
     * @throws IllegalArgumentException If the template resource is not found
     * @param baseTemplateName
     */
    protected String getTemplateLocation(String baseTemplateName, Locale locale) {
        String templateLocation = buildTemplateLocation(fromNullable(locale), baseTemplateName);
        ClassPathResource resource = new ClassPathResource(templateLocation);
        if (!resource.exists()) {
            // use the default template
            templateLocation = buildTemplateLocation(Optional.<Locale>absent(), baseTemplateName);
        }
        resource = new ClassPathResource(templateLocation);
        if (!resource.exists()) {
            throw (new IllegalArgumentException("Template location not found: " + templateLocation));
        }
        return templateLocation;
    }

    protected String buildTemplateLocation(Optional<Locale> locale, String baseTemplateName) {
        String templateName = baseTemplateName + ".vm";
        if (locale.isPresent()) {
            templateName = format("%s_%s.vm", baseTemplateName, locale.get().toString());
        }
        String templateLocation = format("%s/%s", baseTemplateLocation, templateName);
        //log.debug("Template candidate location: {}", templateLocation);
        return templateLocation;
    }
}
