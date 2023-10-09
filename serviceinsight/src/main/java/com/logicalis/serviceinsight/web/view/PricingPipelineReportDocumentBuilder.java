package com.logicalis.serviceinsight.web.view;

import java.util.Locale;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.MessageSource;

import com.logicalis.serviceinsight.representation.PricingPipeline;
import com.logicalis.serviceinsight.representation.PricingPipelineQuoteByCustomer;
import com.logicalis.serviceinsight.representation.PricingPipelineService;

public class PricingPipelineReportDocumentBuilder extends BaseDocumentBuilder {

    private MessageSource messageSource;
    private Locale locale;
    private XSSFSheet sheet1;
    private XSSFSheet sheet2;
    static final String TITLE_SHEET1 = "Services";
    static final String TITLE_SHEET2 = "Customer Quotes";
    static final int HEADER_FACTOR = 3 * 256;
    
    public PricingPipelineReportDocumentBuilder(MessageSource messageSource, Locale locale) {
        this.messageSource = messageSource;
        this.locale = locale;
        workbook = new XSSFWorkbook();
        sheet1 = workbook.createSheet(TITLE_SHEET1);
        sheet1.setDefaultColumnWidth(40);
        sheet2 = workbook.createSheet(TITLE_SHEET2);
        sheet2.setDefaultColumnWidth(40);
    }

    public XSSFWorkbook buildPricingPipelineReportExportSpreadsheet(PricingPipeline report) {
        writeReportData(report);
        return this.workbook;
    }
    
    private void writeReportData(PricingPipeline report) {
        // write out header to each sheet
        writeReportDataHeaders();
        int rowidx = 2;
        
        for (PricingPipelineService service : report.getServices()) {
            rowidx = writeServicesReportDataRow(rowidx, service);
        }
        
        rowidx = 2;
        
        for (PricingPipelineQuoteByCustomer customerQuote : report.getCustomerQuotes()) {
            rowidx = writeCustomersReportDataRow(rowidx, customerQuote);
        }
        
    }
    
    private void writeReportDataHeaders() {
        CellStyle style = headerCellStyle();
        
        int rowidx = 1;
        int colidx = 0;
        
        // Services
        XSSFRow header = sheet1.createRow(rowidx);
        header.createCell(colidx++).setCellValue("");
        header.createCell(colidx).setCellValue("");
        
        header = sheet1.createRow(rowidx++);
        colidx = 0;
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_service", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_offering", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_total_quantity", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_unit_label", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        
        // Customers
        rowidx = 1;
        colidx = 0;
        
        header = sheet2.createRow(rowidx);
        header.createCell(colidx++).setCellValue("");
        header.createCell(colidx).setCellValue("");
        
        header = sheet2.createRow(rowidx++);
        colidx = 0;
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_quote_number", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_customer", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_close_date", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_services", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_total_quantity", null, locale));
        header.getCell(colidx++).setCellStyle(style);
    }
    
    private int writeServicesReportDataRow(Integer rowidx, PricingPipelineService data) {
        CellStyle style = baselineRowCellStyle();
        
        XSSFRow row = sheet1.createRow(rowidx);
        row.setHeightInPoints((short) 30);

        row = sheet1.createRow(rowidx++);
        int colidx = 0;
        row.createCell(colidx).setCellValue(data.getServiceOfferingName());
        row.getCell(colidx++).setCellStyle(style);
        row.createCell(colidx).setCellValue(data.getServiceName());
        row.getCell(colidx++).setCellStyle(style);
        row.createCell(colidx).setCellValue(data.getTotalItems());
        row.getCell(colidx++).setCellStyle(style);
        row.createCell(colidx).setCellValue(data.getUnitLabel());
        row.getCell(colidx).setCellStyle(style);
        
        return rowidx;
    }
    
    private int writeCustomersReportDataRow(Integer rowidx, PricingPipelineQuoteByCustomer data) {
        CellStyle style = baselineRowCellStyle();
        
        XSSFRow row = sheet2.createRow(rowidx);
        row.setHeightInPoints((short) 30);

        row = sheet2.createRow(rowidx++);
        int colidx = 0;
        row.createCell(colidx).setCellValue(data.getQuoteNumber().toString());
        row.getCell(colidx++).setCellStyle(style);
        row.createCell(colidx).setCellValue(data.getCustomerName());
        row.getCell(colidx++).setCellStyle(style);
        row.createCell(colidx).setCellValue(data.getCloseDate());
        row.getCell(colidx++).setCellStyle(style);
        row.createCell(colidx).setCellValue(data.getServices());
        row.getCell(colidx++).setCellStyle(style);
        row.createCell(colidx).setCellValue(data.getTotalItems());
        row.getCell(colidx++).setCellStyle(style);
        
        return rowidx;
    }
    
}
