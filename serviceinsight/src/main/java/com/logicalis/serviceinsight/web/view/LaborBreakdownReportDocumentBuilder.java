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

import com.logicalis.serviceinsight.representation.LaborBreakdownRecord;
import com.logicalis.serviceinsight.representation.RevenueByServiceRecord;
import com.logicalis.serviceinsight.representation.RevenueReportResultRecord;

public class LaborBreakdownReportDocumentBuilder extends BaseDocumentBuilder {

    private MessageSource messageSource;
    private Locale locale;
    private XSSFSheet sheet1;
    private BigDecimal baseTotal;
    private BigDecimal addlTotal;
    private BigDecimal total;
    static final String TITLE_SHEET1 = "SDM Export";
    static final int HEADER_FACTOR = 3 * 256;

    public LaborBreakdownReportDocumentBuilder(MessageSource messageSource, Locale locale) {
        this.messageSource = messageSource;
        this.locale = locale;
        workbook = new XSSFWorkbook();
        sheet1 = workbook.createSheet(TITLE_SHEET1);
        sheet1.setDefaultColumnWidth(30);
        this.baseTotal = new BigDecimal(0);
        this.addlTotal = new BigDecimal(0);
        this.total = new BigDecimal(0);
    }

    public XSSFWorkbook buildLaborBreakdownExportSpreadsheet(List<LaborBreakdownRecord> report) {
        Integer rowidx = 1;
        writeReportData(rowidx, report);
        return this.workbook;
    }

    private void writeReportData(Integer rowidx, List<LaborBreakdownRecord> report) {
        //write out header
        rowidx = writeReportDataHeader(rowidx);

        for (LaborBreakdownRecord reportRecord : report) {
            rowidx = writeReportDataRow(rowidx, reportRecord);
        }
        
        rowidx = writeReportDataTotal(rowidx);
    }

    private int writeReportDataHeader(Integer rowidx) {
        CellStyle style = headerCellStyle();

        int colidx = 0;
        XSSFRow header = sheet1.createRow(rowidx);
        header = sheet1.createRow(rowidx++);
        //header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_business_model", null, locale));
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_tier_code", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_base_rate", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_addl_rate", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_base_rate_monthly_total", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_addl_rate_monthly_total", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_overall_monthly_total", null, locale));
        header.getCell(colidx++).setCellStyle(style);

        return rowidx;
    }

    private int writeReportDataRow(Integer rowidx, LaborBreakdownRecord record) {
        CellStyle style = baselineRowCellStyle();
        CellStyle currency = baselineRowCellCurrencyStyle();

        XSSFRow header = sheet1.createRow(rowidx);
        header.setHeightInPoints((short) 30);

        header = sheet1.createRow(rowidx++);
        int colidx = 0;
        header.createCell(colidx).setCellValue(record.getTierCode());
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(record.getTierRate().doubleValue());
        header.getCell(colidx++).setCellStyle(currency);
        header.createCell(colidx).setCellValue(record.getTierAddlRate().doubleValue());
        header.getCell(colidx++).setCellStyle(currency);
        header.createCell(colidx).setCellValue(record.getTierTotal().doubleValue());
        header.getCell(colidx++).setCellStyle(currency);
        header.createCell(colidx).setCellValue(record.getTierAddlTotal().doubleValue());
        header.getCell(colidx++).setCellStyle(currency);
        BigDecimal overallTotal = record.getTierTotal().add(record.getTierAddlTotal());
        header.createCell(colidx).setCellValue(overallTotal.doubleValue());
        header.getCell(colidx).setCellStyle(currency);

        baseTotal = baseTotal.add(record.getTierTotal());
        addlTotal = addlTotal.add(record.getTierAddlTotal());
        total = total.add(overallTotal);
        
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
        header.createCell(colidx).setCellValue("Total");
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(baseTotal.doubleValue());
        header.getCell(colidx++).setCellStyle(currency);
        header.createCell(colidx).setCellValue(addlTotal.doubleValue());
        header.getCell(colidx++).setCellStyle(currency);
        header.createCell(colidx).setCellValue(total.doubleValue());
        header.getCell(colidx).setCellStyle(currency);
        
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
