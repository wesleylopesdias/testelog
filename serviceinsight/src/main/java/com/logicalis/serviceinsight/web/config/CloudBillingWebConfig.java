package com.logicalis.serviceinsight.web.config;

import com.logicalis.serviceinsight.web.converter.CloudBillingConversionServiceFactoryBean;
import com.logicalis.serviceinsight.web.view.ExcelViewResolver;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import org.springframework.web.servlet.view.tiles3.TilesConfigurer;
import org.springframework.web.servlet.view.tiles3.TilesView;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;

/**
 * Magically configures Spring MVC! - extend WebMvcConfigurerAdapter or even
 * WebMvcConfigurationSupport to customize...
 *
 * @see
 * org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport
 * @author poneil
 */
@Configuration
@ComponentScan(basePackages = {"com.logicalis.serviceinsight.web.controller"})
public class CloudBillingWebConfig extends WebMvcConfigurationSupport {

    private static final Logger log = LoggerFactory.getLogger(CloudBillingWebConfig.class);

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        // Simple strategy: only path extension is taken into account
        configurer.favorPathExtension(true)
                .ignoreAcceptHeader(true)
                .useJaf(false)
                .defaultContentType(MediaType.TEXT_HTML);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/resources/**").addResourceLocations("/images", "/js", "/css", "/fonts", "/xlsx");
    }

    @Bean
    public ReloadableResourceBundleMessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasenames("WEB-INF/i18n/messages");
        return messageSource;
    }

    @Bean
    public CookieLocaleResolver localeResolver() {
        CookieLocaleResolver localeResolver = new CookieLocaleResolver();
        localeResolver.setDefaultLocale(new Locale("en", "US"));
        localeResolver.setCookieName("locale");
        return localeResolver;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
        localeChangeInterceptor.setParamName("lang");
        return localeChangeInterceptor;
    }

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

    @Override
    public FormattingConversionService mvcConversionService() {
        return conversionService().getObject();
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/login").setViewName("login");
        //registry.addViewController("/billing").setViewName("billing/index");
        registry.addViewController("/unauthorized").setViewName("unauthorized");
        registry.addViewController("/ok").setViewName("ok");
    }

    @Bean
    public CloudBillingConversionServiceFactoryBean conversionService() {
        return new CloudBillingConversionServiceFactoryBean();
    }

    @Bean
    public ViewResolver jspViewResolver() {
        UrlBasedViewResolver resolver = new UrlBasedViewResolver();
        resolver.setViewClass(TilesView.class);
        return resolver;
    }

    @Bean
    public ViewResolver excelViewResolver() {
        return new ExcelViewResolver();
    }

    @Bean(name = "multipartResolver")
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }

    @Bean
    public TilesConfigurer tilesConfigurer() {
        TilesConfigurer tilesConfigurer = new TilesConfigurer();
        tilesConfigurer.setDefinitions(new String[]{
            "/WEB-INF/layouts/tiles.xml",
            "/WEB-INF/views/**/tiles.xml"
        });
        tilesConfigurer.setCheckRefresh(true);
        return tilesConfigurer;
    }

    @Bean
    public SaajSoapMessageFactory messageFactory() {
        SaajSoapMessageFactory messageFactory = new SaajSoapMessageFactory();
        messageFactory.setSoapVersion(SoapVersion.SOAP_11);
        return messageFactory;
    }

    @Profile("local")
    static class LocalPropertiesConfig {

        @Bean(name = "siProps")
        public static PropertySourcesPlaceholderConfigurer siProps() {
            Resource[] locations = {
                new PathMatchingResourcePatternResolver().getResource("classpath:si_common.properties"),
                new PathMatchingResourcePatternResolver().getResource("classpath:META-INF/env/local/si.properties"),
                new PathMatchingResourcePatternResolver().getResource("classpath:META-INF/env/local/database.properties"),
                new PathMatchingResourcePatternResolver().getResource("classpath:META-INF/env/local/mail.properties")
            };
            PropertySourcesPlaceholderConfigurer bean = new PropertySourcesPlaceholderConfigurer();
            bean.setIgnoreUnresolvablePlaceholders(Boolean.TRUE);
            bean.setLocations(locations);
            log.info("Created LOCAL environment PropertySourcesPlaceholderConfigurer in Controllers");
            return bean;
        }
    }

    @Profile("dev")
    static class DevPropertiesConfig {

        @Bean(name = "siProps")
        public static PropertySourcesPlaceholderConfigurer siProps() {
            Resource[] locations = {
                new PathMatchingResourcePatternResolver().getResource("classpath:si_common.properties"),
                new PathMatchingResourcePatternResolver().getResource("classpath:META-INF/env/dev/si.properties"),
                new PathMatchingResourcePatternResolver().getResource("classpath:META-INF/env/dev/database.properties"),
                new PathMatchingResourcePatternResolver().getResource("classpath:META-INF/env/dev/mail.properties")
            };
            PropertySourcesPlaceholderConfigurer bean = new PropertySourcesPlaceholderConfigurer();
            bean.setIgnoreUnresolvablePlaceholders(Boolean.TRUE);
            bean.setLocations(locations);
            log.info("Created DEV environment PropertySourcesPlaceholderConfigurer in Controllers");
            return bean;
        }
    }

    @Profile("test")
    static class TestPropertiesConfig {

        @Bean(name = "siProps")
        public static PropertySourcesPlaceholderConfigurer siProps() {
            Resource[] locations = {
                new PathMatchingResourcePatternResolver().getResource("classpath:si_common.properties"),
                new PathMatchingResourcePatternResolver().getResource("classpath:META-INF/env/test/si.properties"),
                new PathMatchingResourcePatternResolver().getResource("classpath:META-INF/env/test/database.properties"),
                new PathMatchingResourcePatternResolver().getResource("classpath:META-INF/env/test/mail.properties")
            };
            PropertySourcesPlaceholderConfigurer bean = new PropertySourcesPlaceholderConfigurer();
            bean.setIgnoreUnresolvablePlaceholders(Boolean.TRUE);
            bean.setLocations(locations);
            log.info("Created TEST environment PropertySourcesPlaceholderConfigurer in Controllers");
            return bean;
        }
    }

    @Profile("prod")
    static class ProdPropertiesConfig {

        @Bean(name = "siProps")
        public static PropertySourcesPlaceholderConfigurer siProps() {
            Resource[] locations = {
                new PathMatchingResourcePatternResolver().getResource("classpath:si_common.properties"),
                new PathMatchingResourcePatternResolver().getResource("classpath:META-INF/env/prod/si.properties"),
                new PathMatchingResourcePatternResolver().getResource("classpath:META-INF/env/prod/database.properties"),
                new PathMatchingResourcePatternResolver().getResource("classpath:META-INF/env/prod/mail.properties")
            };
            PropertySourcesPlaceholderConfigurer bean = new PropertySourcesPlaceholderConfigurer();
            bean.setIgnoreUnresolvablePlaceholders(Boolean.TRUE);
            bean.setLocations(locations);
            log.info("Created PROD environment PropertySourcesPlaceholderConfigurer in Controllers");
            return bean;
        }
    }
}
