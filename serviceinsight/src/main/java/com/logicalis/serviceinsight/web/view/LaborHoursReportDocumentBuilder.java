package com.logicalis.serviceinsight.web.view;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;

import net.sf.json.JSONObject;

import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.logicalis.serviceinsight.representation.LaborBreakdownRecord;
import com.logicalis.serviceinsight.representation.LaborHoursRecord;
import com.logicalis.serviceinsight.representation.RevenueByServiceRecord;
import com.logicalis.serviceinsight.representation.RevenueReportResultRecord;

public class LaborHoursReportDocumentBuilder extends BaseDocumentBuilder {

    private MessageSource messageSource;
    private Locale locale;
    private XSSFSheet sheet1;
    private BigDecimal total;
    private Boolean isManager = Boolean.FALSE;
    static final String TITLE_SHEET1 = "Labor Hours Export";
    static final int HEADER_FACTOR = 3 * 256;

    public LaborHoursReportDocumentBuilder(MessageSource messageSource, Locale locale) {
        this.messageSource = messageSource;
        this.locale = locale;
        workbook = new XSSFWorkbook();
        sheet1 = workbook.createSheet(TITLE_SHEET1);
        sheet1.setDefaultColumnWidth(30);
        this.total = new BigDecimal(0);
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MANAGER")) || authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))){
        	this.isManager = Boolean.TRUE;
        }
    }

    public XSSFWorkbook buildLaborHoursExportSpreadsheet(List<LaborHoursRecord> report) {
        Integer rowidx = 1;
        writeReportData(rowidx, report);
        return this.workbook;
    }

    private void writeReportData(Integer rowidx, List<LaborHoursRecord> report) {
        //write out header
        rowidx = writeReportDataHeader(rowidx);

        for (LaborHoursRecord reportRecord : report) {
            rowidx = writeReportDataRow(rowidx, reportRecord);
        }
        
        rowidx = writeReportDataTotal(rowidx);
    }

    private int writeReportDataHeader(Integer rowidx) {
        CellStyle style = headerCellStyle();

        int colidx = 0;
        XSSFRow header = sheet1.createRow(rowidx);
        header = sheet1.createRow(rowidx++);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_customer", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_worker", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_service_name", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_ticket", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_task_description", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_work_date", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        if(this.isManager) {
        	header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_tier_name", null, locale));
            header.getCell(colidx++).setCellStyle(style);
            header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_tier_code", null, locale));
            header.getCell(colidx++).setCellStyle(style);
        }
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_hours_worked", null, locale));
        header.getCell(colidx++).setCellStyle(style);

        return rowidx;
    }

    private int writeReportDataRow(Integer rowidx, LaborHoursRecord record) {
        CellStyle style = baselineRowCellStyle();
        CellStyle dateStyle = baselineRowDateStyle();

        XSSFRow header = sheet1.createRow(rowidx);
        header.setHeightInPoints((short) 30);

        header = sheet1.createRow(rowidx++);
        int colidx = 0;
        header.createCell(colidx).setCellValue(record.getCustomerName());
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(record.getWorker());
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(record.getServiceName());
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(record.getTicket());
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(record.getTaskDescription());
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(record.getWorkDate());
        header.getCell(colidx++).setCellStyle(dateStyle);
        if(this.isManager) {
        	header.createCell(colidx).setCellValue(record.getTierName());
            header.getCell(colidx++).setCellStyle(style);
            header.createCell(colidx).setCellValue(record.getTierCode());
            header.getCell(colidx++).setCellStyle(dateStyle);
        }
        header.createCell(colidx).setCellValue(record.getHours().doubleValue());
        header.getCell(colidx++).setCellStyle(style);

        total = total.add(record.getHours());
        
        return rowidx;
    }

    private int writeReportDataTotal(Integer rowidx) {
    	CellStyle style = totalCellStyle();
        CellStyle currency = totalCellCurrencyStyle();
        
    	int colidx = 0;
        XSSFRow header = sheet1.createRow(rowidx);
        header = sheet1.createRow(rowidx++);
        header.createCell(colidx).setCellValue("");
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue("");
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue("");
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue("");
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue("");
        header.getCell(colidx++).setCellStyle(style);
        if(this.isManager) {
        	header.createCell(colidx).setCellValue("");
            header.getCell(colidx++).setCellStyle(style);
            header.createCell(colidx).setCellValue("");
            header.getCell(colidx++).setCellStyle(style);
        }
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_total", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(total.doubleValue());
        header.getCell(colidx++).setCellStyle(style);
        
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
