package com.logicalis.serviceinsight.web.converter;

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.ConverterRegistry;
import org.springframework.format.support.FormattingConversionServiceFactoryBean;

/**
 *
 * @author poneil
 */
public class CloudBillingConversionServiceFactoryBean extends FormattingConversionServiceFactoryBean {

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        ConversionService conversionService = getObject();
        ConverterRegistry registry = (ConverterRegistry) conversionService;
        // register converters that need a nested conversion service
    }
}
