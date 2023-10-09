package com.logicalis.serviceinsight.web.config;

import java.util.Properties;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.DatabasePopulator;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.ui.velocity.VelocityEngineFactoryBean;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;

import com.logicalis.serviceinsight.util.servicenow.SNObjectAdapter;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Java config for services and ORM!
 *
 * @author poneil
 */
@Configuration
@EnableSpringConfigured
@EnableAsync
@EnableScheduling
@EnableTransactionManagement(mode = AdviceMode.ASPECTJ)
@ComponentScan(basePackages = {"com.logicalis.serviceinsight"}, excludeFilters = {
    @ComponentScan.Filter(Controller.class)})
public class CloudBillingServiceModelConfiguration {

    private static final Logger log = LoggerFactory.getLogger(CloudBillingServiceModelConfiguration.class);
    @Autowired
    Environment env;
    @Value("classpath:db-schema.sql")
    private Resource schemaScript;
    @Value("classpath:db-schema-security.sql")
    private Resource securitySchemaScript;
    @Value("classpath:db-views.sql")
    private Resource viewsScript;
    @Value("classpath:db-data-security.sql")
    private Resource securityDataScript;
    @Value("classpath:db-data-costs.sql")
    private Resource dataScript1;
    @Value("classpath:db-data-devices.sql")
    private Resource dataScript2;
    @Value("classpath:db-data-arcadia.sql")
    private Resource dataScript3;
    @Value("classpath:db-data-anixter.sql")
    private Resource dataScript4;
    @Value("classpath:db-data-ewscripps.sql")
    private Resource dataScript5;
    @Value("classpath:db-data-scheduled-task.sql")
    private Resource dataScript6;
    @Value("classpath:db-data-armstrong.sql")
    private Resource dataScript7;
    @Value("${database.driverClassName}")
    String databaseDriver;
    @Value("${database.url}")
    String databaseUrl;
    @Value("${database.username}")
    String databaseUsername;
    @Value("${database.password}")
    String databasePassword;
    @Value("${chronos.database.driverClassName}")
    String chronosDatabaseDriver;
    @Value("${chronos.database.url}")
    String chronosDatabaseUrl;
    @Value("${chronos.database.username}")
    String chronosDatabaseUsername;
    @Value("${chronos.database.password}")
    String chronosDatabasePassword;
    @Value("${azure.database.driverClassName}")
    String azureDatabaseDriver;
    @Value("${azure.database.url}")
    String azureDatabaseUrl;
    @Value("${azure.database.username}")
    String azureDatabaseUsername;
    @Value("${azure.database.password}")
    String azureDatabasePassword;
    @Value("${mail.protocol}")
    String mailProtocol;
    @Value("${mail.encoding}")
    String mailEncoding;
    @Value("${mail.host}")
    String mailHost;
    @Value("${mail.username}")
    String mailUsername;
    @Value("${mail.password}")
    String mailPassword;
    @Value("${mail.port}")
    Integer mailPort;
    @Value("${mail.debug}")
    String mailDebug;
    @Value("${sn.url}")
	private String serviceNowUrl;
	@Value("${sn.login}")
	private String serviceNowLogin;
	@Value("${sn.password}")
	private String serviceNowPassword;

    @Bean
    public DataSource dataSource() {
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(databaseDriver);
        ds.setUrl(databaseUrl);
        ds.setUsername(databaseUsername);
        ds.setPassword(databasePassword);
        ds.setTestOnBorrow(true);
        ds.setTestOnReturn(true);
        ds.setTestWhileIdle(true);
        ds.setTimeBetweenEvictionRunsMillis(1800000);
        ds.setNumTestsPerEvictionRun(3);
        ds.setMinEvictableIdleTimeMillis(1800000);
        ds.setValidationQuery("SELECT 1");
        //ds.setMinIdle(minIdle);
        return ds;
    }

    @Bean
    public DataSource chronosDataSource() {
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(chronosDatabaseDriver);
        ds.setUrl(chronosDatabaseUrl);
        ds.setUsername(chronosDatabaseUsername);
        ds.setPassword(chronosDatabasePassword);
        ds.setTestOnBorrow(true);
        ds.setTestOnReturn(true);
        ds.setTestWhileIdle(true);
        ds.setTimeBetweenEvictionRunsMillis(1800000);
        ds.setNumTestsPerEvictionRun(3);
        ds.setMinEvictableIdleTimeMillis(1800000);
        ds.setValidationQuery("SELECT 1");
        //ds.setMinIdle(minIdle);
        return ds;
    }
    
    @Bean
    public DataSource azureDataSource() {
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(azureDatabaseDriver);
        ds.setUrl(azureDatabaseUrl);
        ds.setUsername(azureDatabaseUsername);
        ds.setPassword(azureDatabasePassword);
        ds.setTestOnBorrow(true);
        ds.setTestOnReturn(true);
        ds.setTestWhileIdle(true);
        ds.setTimeBetweenEvictionRunsMillis(1800000);
        ds.setNumTestsPerEvictionRun(3);
        ds.setMinEvictableIdleTimeMillis(1800000);
        ds.setValidationQuery("SELECT 1");
        //ds.setMinIdle(minIdle);
        return ds;
    }

    @Bean
    @Profile({"notused"})
    public DataSourceInitializer dataSourceInitializer() {
        final DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(dataSource());
        initializer.setDatabasePopulator(databasePopulator());
        return initializer;
    }

    private DatabasePopulator databasePopulator() {
        final ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(schemaScript);
        populator.addScript(securitySchemaScript);
        populator.addScript(securityDataScript);
        /**
         * Commenting out while new cost queries are developed
         *
         * populator.addScript(viewsScript);
         */
        populator.addScript(dataScript1);
        populator.addScript(dataScript2);
        populator.addScript(dataScript3);
        populator.addScript(dataScript4);
        populator.addScript(dataScript5);
        populator.addScript(dataScript6);
        populator.addScript(dataScript7);
        return populator;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManager() {
        LocalContainerEntityManagerFactoryBean entityManagerFactory = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactory.setDataSource(dataSource());
        entityManagerFactory.setPersistenceUnitName("persistenceUnit");
        entityManagerFactory.setPackagesToScan("com.logicalis.serviceinsight.model");
        return entityManagerFactory;
    }

    /**
     * We're NOT using JPA in this app (although some configuration exists), so
     * we use the datasource txManager
     *
     * @return
     */
//    @Bean
    public PlatformTransactionManager jpaTransactionManager() {
        JpaTransactionManager jpaTxManager = new JpaTransactionManager();
        jpaTxManager.setEntityManagerFactory(entityManager().getObject());
        return jpaTxManager;
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        DataSourceTransactionManager dsTxMgr = new DataSourceTransactionManager();
        dsTxMgr.setDataSource(dataSource());
        return dsTxMgr;
    }

    @Bean
    public JavaMailSender mailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setProtocol(mailProtocol);
        mailSender.setDefaultEncoding(mailEncoding);
        mailSender.setHost(mailHost);
        mailSender.setUsername(mailUsername);
        mailSender.setPassword(mailPassword);
        mailSender.setPort(mailPort);
        return mailSender;
    }

    @Bean
    public VelocityEngineFactoryBean velocityEngine() {
        VelocityEngineFactoryBean velocityEngineFactoryBean = new VelocityEngineFactoryBean();
        Properties velocityProperties = new Properties();
        velocityProperties.setProperty("resource.loader", "class");
        velocityProperties.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        velocityEngineFactoryBean.setVelocityProperties(velocityProperties);
        return velocityEngineFactoryBean;
    }

    @Bean
    public SaajSoapMessageFactory soapMessageFactory() {
        SaajSoapMessageFactory messageFactory = new SaajSoapMessageFactory();
        messageFactory.setSoapVersion(SoapVersion.SOAP_11);
        messageFactory.afterPropertiesSet();
        return messageFactory;
    }
    
    @Bean
    public SNObjectAdapter snObjectAdapter() {
    	SNObjectAdapter sno = new SNObjectAdapter(serviceNowUrl, serviceNowLogin, serviceNowPassword);
    	return sno;
    }
}
