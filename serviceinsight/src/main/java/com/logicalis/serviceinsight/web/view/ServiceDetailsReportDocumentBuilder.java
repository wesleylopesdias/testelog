package com.logicalis.serviceinsight.web.view;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.MessageSource;

import com.logicalis.serviceinsight.representation.ServiceDetailRecord;
import com.logicalis.serviceinsight.representation.ServiceDetailRecordWrapper;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Font;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class ServiceDetailsReportDocumentBuilder extends BaseDocumentBuilder {

    private MessageSource messageSource;
    private Locale locale;
    static final String TITLE_SHEET1 = "Service Detail Export";
    private XSSFSheet sheet1;
    DateTimeFormatter dateformatter = DateTimeFormat.forPattern("MM/dd/yyyy");

    public ServiceDetailsReportDocumentBuilder(MessageSource messageSource, Locale locale) {
        this.messageSource = messageSource;
        this.locale = locale;
        workbook = new XSSFWorkbook();
        sheet1 = workbook.createSheet(TITLE_SHEET1);
        sheet1.setDefaultColumnWidth(30);
    }

    public XSSFWorkbook buildSpreadsheet(ServiceDetailRecordWrapper report) {
        Integer rowidx = writeReportHeader(report);
        rowidx = writeReportData(rowidx, report);
        writeReportDataTotal(rowidx, report);
        return this.workbook;
    }

    private int writeReportHeader(ServiceDetailRecordWrapper report) {
        CellStyle headerStyle = headerTableHeaderCellStyle();
        CellStyle style = headerTableBodyCellStyle();

        int rowidx = 0;
        int colidx = 0;
        XSSFRow header = sheet1.createRow(rowidx);
        header.setHeightInPoints((short) 30);

        header = sheet1.createRow(rowidx++);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_title_service_details", null, locale));
        header.getCell(colidx++).setCellStyle(headerStyle);
        header.createCell(colidx).setCellValue("");
        header.getCell(colidx).setCellStyle(headerStyle);

        colidx = 0;
        header = sheet1.createRow(rowidx++);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_date", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        String dateval = dateformatter.print(report.getServiceDate());
        header.createCell(colidx).setCellValue(dateval);
        header.getCell(colidx).setCellStyle(style);

        colidx = 0;
        header = sheet1.createRow(rowidx++);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_service", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        String service = report.getServiceName();
        header.createCell(colidx).setCellValue(service);
        header.getCell(colidx).setCellStyle(style);
        return rowidx;
    }

    private int writeReportData(Integer rowidx, ServiceDetailRecordWrapper report) {
        //write out header
        rowidx = writeReportDataHeader(rowidx);
        for (ServiceDetailRecord record : report.getData()) {
            rowidx = writeReportDataRow(rowidx, record);
        }
        return rowidx;
    }

    private int writeReportDataHeader(Integer rowidx) {
        CellStyle style = headerCellStyle();

        int colidx = 0;
        XSSFRow header = sheet1.createRow(rowidx);
        header = sheet1.createRow(rowidx++);
        header.createCell(colidx++).setCellValue("");
        header.createCell(colidx).setCellValue("");

        header = sheet1.createRow(rowidx++);
        colidx = 0;
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_sd_customer", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_sd_jobnumber", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_sd_contractname", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_sd_sdm", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_service", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_sd_devicename", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_sd_devicepartno", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_sd_quantity", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_sd_unit_count", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_sd_onetime", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_sd_recurring", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_sd_revtotal", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_sd_startdate", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_sd_enddate", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        return rowidx;
    }

    private int writeReportDataRow(Integer rowidx, ServiceDetailRecord data) {
        CellStyle style = baselineRowCellStyle();
        CellStyle currency = baselineRowCellCurrencyStyle();

        XSSFRow header = sheet1.createRow(rowidx);
        header.setHeightInPoints((short) 30);

        header = sheet1.createRow(rowidx++);
        int colidx = 0;
        header.createCell(colidx).setCellValue(data.getCustomerName());
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(data.getContractJobNumber());
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(data.getContractName());
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(data.getEngagementManager());
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(data.getServiceName());
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(data.getDeviceDescription());
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(data.getDevicePartNumber());
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(data.getQuantity());
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(data.getUnitCount());
        header.getCell(colidx++).setCellStyle(style);
        BigDecimal onetime = data.getAppliedOnetimeRevenue();
        onetime.setScale(2, RoundingMode.HALF_UP);
        header.createCell(colidx).setCellValue(onetime.doubleValue());
        header.getCell(colidx++).setCellStyle(currency);
        BigDecimal recurring = data.getAppliedRecurringRevenue();
        recurring.setScale(2, RoundingMode.HALF_UP);
        header.createCell(colidx).setCellValue(recurring.doubleValue());
        header.getCell(colidx++).setCellStyle(currency);
        BigDecimal revtotal = onetime.add(recurring);
        revtotal.setScale(2, RoundingMode.HALF_UP);
        header.createCell(colidx).setCellValue(revtotal.doubleValue());
        header.getCell(colidx++).setCellStyle(currency);
        header.createCell(colidx).setCellValue(dateformatter.print(data.getStartDate()));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(dateformatter.print(data.getEndDate()));
        header.getCell(colidx++).setCellStyle(style);
        return rowidx;
    }

    private int writeReportDataTotal(Integer rowidx, ServiceDetailRecordWrapper report) {
    	CellStyle style = totalCellStyle();
        CellStyle currency = totalCellCurrencyStyle();
        
    	int colidx = 0;
        XSSFRow header = sheet1.createRow(rowidx);
        header = sheet1.createRow(rowidx++);
        for (int i=0; i<6; i++) {
            header.createCell(colidx).setCellValue("");
            header.getCell(colidx++).setCellStyle(style);
        }
        BigDecimal totalOnetime = BigDecimal.ZERO;
        BigDecimal totalRecurring = BigDecimal.ZERO;
        for (ServiceDetailRecord record : report.getData()) {
            totalOnetime = totalOnetime.add(record.getAppliedOnetimeRevenue());
            totalRecurring = totalRecurring.add(record.getAppliedRecurringRevenue());
        }
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_total", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(totalOnetime.doubleValue());
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(totalRecurring.doubleValue());
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(totalOnetime.add(totalRecurring).doubleValue());
        header.getCell(colidx++).setCellStyle(style);
        for (int i=0; i<2; i++) {
            header.createCell(colidx).setCellValue("");
            header.getCell(colidx++).setCellStyle(style);
        }
    	return rowidx;
    }
    
    private CellStyle totalCellStyle() {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName("Arial");
        style.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        style.setAlignment(CellStyle.ALIGN_RIGHT);
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        font.setColor(HSSFColor.BLACK.index);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        return style;
    }

    private CellStyle totalCellCurrencyStyle() {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName("Arial");
        style.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
        style.setDataFormat((short) 8);
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        style.setAlignment(CellStyle.ALIGN_RIGHT);
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        font.setColor(HSSFColor.BLACK.index);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        return style;
    }
}