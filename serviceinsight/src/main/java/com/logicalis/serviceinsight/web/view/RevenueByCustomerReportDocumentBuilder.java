package com.logicalis.serviceinsight.web.view;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;

import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.MessageSource;

import com.logicalis.serviceinsight.representation.RevenueByCustomerRecord;
import com.logicalis.serviceinsight.representation.RevenueByServiceRecord;
import com.logicalis.serviceinsight.representation.RevenueReportResultRecord;

public class RevenueByCustomerReportDocumentBuilder extends BaseDocumentBuilder {

    private MessageSource messageSource;
    private Locale locale;
    private XSSFSheet sheet1;
    private BigDecimal total;
    private BigDecimal totalDirectCustomerCost;
    private BigDecimal totalServiceCost;
    private BigDecimal totalLaborToolsCost;
    private BigDecimal totalProjectLaborCost;
    private BigDecimal totalIndirectLaborCost;
    private BigDecimal totalDirectLaborCost;
    private BigDecimal totalCost;
    static final String TITLE_SHEET1 = "SDM Export";
    static final int HEADER_FACTOR = 3 * 256;

    public RevenueByCustomerReportDocumentBuilder(MessageSource messageSource, Locale locale) {
        this.messageSource = messageSource;
        this.locale = locale;
        workbook = new XSSFWorkbook();
        sheet1 = workbook.createSheet(TITLE_SHEET1);
        sheet1.setDefaultColumnWidth(30);
        this.total = new BigDecimal(0);
        this.totalDirectCustomerCost = new BigDecimal(0);
        this.totalServiceCost = new BigDecimal(0);
        this.totalLaborToolsCost = new BigDecimal(0);
        this.totalProjectLaborCost = new BigDecimal(0);
        this.totalIndirectLaborCost = new BigDecimal(0);
        this.totalDirectLaborCost = new BigDecimal(0);
        this.totalCost = new BigDecimal(0);
    }
    
    public XSSFWorkbook buildRevenueByCustomerReportExportSpreadsheet(List<RevenueByCustomerRecord> report) {
        Integer rowidx = 1;
        writeReportData(rowidx, report);
        return this.workbook;
    }
    
    private void writeReportData(Integer rowidx, List<RevenueByCustomerRecord> report) {
        // write out header
        RevenueByCustomerRecord firstRecord = report.get(0);
        rowidx = writeReportDataHeader(rowidx, firstRecord);
        
        for (RevenueByCustomerRecord reportRecord : report) {
            rowidx = writeReportDataRow(rowidx, reportRecord);
        }
        
        rowidx = writeReportDataTotal(rowidx);
    }
    
    private int writeReportDataHeader(Integer rowidx, RevenueByCustomerRecord record) {
        CellStyle style = headerCellStyle();
        
        int colidx = 0;
        XSSFRow header = sheet1.createRow(rowidx);
        header = sheet1.createRow(rowidx++);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_customer_name", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_sd_sdm", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_device_count", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        

        for (RevenueReportResultRecord data : record.getData()) {
            if (data.getDisplayDate() != null) {
                String date = data.getDisplayDate();
                header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_revenue", null, locale) + " - " + date);
                header.getCell(colidx++).setCellStyle(style);
                header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_direct_customer_cost", null, locale) + " - " + date);
                header.getCell(colidx++).setCellStyle(style);
                header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_service_tools_cost", null, locale) + " - " + date);
                header.getCell(colidx++).setCellStyle(style);
                header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_labor_tools_cost", null, locale) + " - " + date);
                header.getCell(colidx++).setCellStyle(style);
                header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_project_labor_cost", null, locale) + " - " + date);
                header.getCell(colidx++).setCellStyle(style);
                header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_indirect_labor_cost", null, locale) + " - " + date);
                header.getCell(colidx++).setCellStyle(style);
                header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_direct_labor_cost", null, locale) + " - " + date);
                header.getCell(colidx++).setCellStyle(style);
                header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_cost_total_cost", null, locale) + " - " + date);
                header.getCell(colidx++).setCellStyle(style);
                header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_margin", null, locale) + " - " + date);
                header.getCell(colidx++).setCellStyle(style);
            }
        }

        return rowidx;
    }

    private int writeReportDataRow(Integer rowidx, RevenueByCustomerRecord record) {
        CellStyle style = baselineRowCellStyle();
        CellStyle currency = baselineRowCellCurrencyStyle();
        CellStyle percent = baselineRowCellPercentStyle();

        XSSFRow header = sheet1.createRow(rowidx);
        header.setHeightInPoints((short) 30);

        header = sheet1.createRow(rowidx++);
        int colidx = 0;
        header.createCell(colidx).setCellValue(record.getCustomerName());
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(record.getServiceDeliveryManager());
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(record.getDeviceCount());
        header.getCell(colidx++).setCellStyle(style);

        for (RevenueReportResultRecord data : record.getData()) {
            BigDecimal revenue = new BigDecimal(0);
            if (data.getRevenue() != null) {
                revenue = data.getRevenue();
                revenue = revenue.setScale(2, RoundingMode.HALF_UP);
                total = total.add(revenue);
            }
            header.createCell(colidx).setCellValue(revenue.doubleValue());
            header.getCell(colidx++).setCellStyle(currency);
            
            /*
            BigDecimal cost = new BigDecimal(0);
            if (data.getLaborCost() != null) {
                cost = data.getLaborCost();
            }
            if (data.getIndirectLaborCost() != null) {
                cost = cost.add(data.getIndirectLaborCost());
            }
            if (data.getLaborToolsCost() != null) {
                cost = cost.add(data.getLaborToolsCost());
            }
            if (data.getServiceCost() != null) {
                cost = cost.add(data.getServiceCost());
            }
            if (data.getOnboardingLaborCost() != null) {
                cost = cost.add(data.getOnboardingLaborCost());
            }*/
            BigDecimal directCustomerCost = data.getDirectCost();
	        totalDirectCustomerCost = totalDirectCustomerCost.add(directCustomerCost);
            directCustomerCost = directCustomerCost.setScale(2, RoundingMode.HALF_UP);
            header.createCell(colidx).setCellValue(directCustomerCost.doubleValue());
            header.getCell(colidx++).setCellStyle(currency);
            
            BigDecimal serviceToolsCost = data.getServiceCost();
            totalServiceCost = totalServiceCost.add(serviceToolsCost);
            serviceToolsCost = serviceToolsCost.setScale(2, RoundingMode.HALF_UP);
            header.createCell(colidx).setCellValue(serviceToolsCost.doubleValue());
            header.getCell(colidx++).setCellStyle(currency);
            
            BigDecimal laborToolsCost = data.getLaborToolsCost();
            totalLaborToolsCost = totalLaborToolsCost.add(laborToolsCost);
            laborToolsCost = laborToolsCost.setScale(2, RoundingMode.HALF_UP);
            header.createCell(colidx).setCellValue(laborToolsCost.doubleValue());
            header.getCell(colidx++).setCellStyle(currency);
            
            BigDecimal projectLaborCost = data.getOnboardingLaborCost();
            totalProjectLaborCost = totalProjectLaborCost.add(projectLaborCost);
            projectLaborCost = projectLaborCost.setScale(2, RoundingMode.HALF_UP);
            header.createCell(colidx).setCellValue(projectLaborCost.doubleValue());
            header.getCell(colidx++).setCellStyle(currency);
            
            BigDecimal indirectLaborCost = data.getIndirectLaborProportionCost();
            totalIndirectLaborCost = totalIndirectLaborCost.add(indirectLaborCost);
            indirectLaborCost = indirectLaborCost.setScale(2, RoundingMode.HALF_UP);
            header.createCell(colidx).setCellValue(indirectLaborCost.doubleValue());
            header.getCell(colidx++).setCellStyle(currency);
            
            BigDecimal directLaborCost = data.getLaborCost();
            totalDirectLaborCost = totalDirectLaborCost.add(directLaborCost);
            directLaborCost = directLaborCost.setScale(2, RoundingMode.HALF_UP);
            header.createCell(colidx).setCellValue(directLaborCost.doubleValue());
            header.getCell(colidx++).setCellStyle(currency);
            
            BigDecimal cost = data.getTotalCost();
            cost = cost.setScale(2, RoundingMode.HALF_UP);
            totalCost = totalCost.add(cost);
            header.createCell(colidx).setCellValue(cost.doubleValue());
            header.getCell(colidx++).setCellStyle(currency);
            
            BigDecimal margin = data.getMargin();
	        header.createCell(colidx).setCellValue(margin.doubleValue());
	        header.getCell(colidx).setCellStyle(percent);
        }

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
        header.createCell(colidx).setCellValue(total.doubleValue());
        header.getCell(colidx++).setCellStyle(currency);
        header.createCell(colidx).setCellValue(totalDirectCustomerCost.doubleValue());
        header.getCell(colidx++).setCellStyle(currency);
        header.createCell(colidx).setCellValue(totalServiceCost.doubleValue());
        header.getCell(colidx++).setCellStyle(currency);
        header.createCell(colidx).setCellValue(totalLaborToolsCost.doubleValue());
        header.getCell(colidx++).setCellStyle(currency);
        header.createCell(colidx).setCellValue(totalProjectLaborCost.doubleValue());
        header.getCell(colidx++).setCellStyle(currency);
        header.createCell(colidx).setCellValue(totalIndirectLaborCost.doubleValue());
        header.getCell(colidx++).setCellStyle(currency);
        header.createCell(colidx).setCellValue(totalDirectLaborCost.doubleValue());
        header.getCell(colidx++).setCellStyle(currency);
        header.createCell(colidx).setCellValue(totalCost.doubleValue());
        header.getCell(colidx++).setCellStyle(currency);
        header.createCell(colidx).setCellValue("");
        header.getCell(colidx++).setCellStyle(currency);
        
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
