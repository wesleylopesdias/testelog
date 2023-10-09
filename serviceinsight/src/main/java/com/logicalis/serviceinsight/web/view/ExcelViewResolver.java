package com.logicalis.serviceinsight.web.view;

import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

/**
 * Provides the resolver for the applications Excel spreadsheet view builder
 *
 * @author poneil
 */
public class ExcelViewResolver implements ViewResolver {

    @Autowired
    ExcelView excelView;

    @Override
    public View resolveViewName(String string, Locale locale) throws Exception {
        return excelView;
    }
}
