package com.logicalis.serviceinsight.web.view;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import com.logicalis.serviceinsight.data.Contract;
import com.logicalis.serviceinsight.data.ContractAdjustment;
import com.logicalis.serviceinsight.data.ContractServiceChangedConsolidatedWrapper;
import com.logicalis.serviceinsight.data.ContractUpdate;
import com.logicalis.serviceinsight.data.Personnel;
import com.logicalis.serviceinsight.data.Service;
import com.logicalis.serviceinsight.service.ContractDaoService;

public class ChangedConsolidatedDocumentBuilder extends BaseDocumentBuilder {

    private MessageSource messageSource;
    private Locale locale;
    private XSSFSheet sheet1;
    static final String TITLE_SHEET1 = "RMO Export";
    static final int HEADER_FACTOR = 2 * 256;

    @Autowired
    ContractDaoService contractDaoService;
    
    public ChangedConsolidatedDocumentBuilder(MessageSource messageSource, Locale locale) {
        this.messageSource = messageSource;
        this.locale = locale;
        workbook = new XSSFWorkbook();
        sheet1 = workbook.createSheet(TITLE_SHEET1);
        sheet1.setDefaultColumnWidth(30);
    }

    public XSSFWorkbook buildExportSpreadsheet(List<ContractServiceChangedConsolidatedWrapper> wrappers) {
    	Integer rowidx = 6;
    	if(wrappers.size() > 0) {
    		writeReportTitle();
    		writeReportInfo(wrappers.get(0).getBillingPeriod());
    		
    		// header row
            rowidx = writeContractHeader(rowidx);
        	for(ContractServiceChangedConsolidatedWrapper wrapper : wrappers) {
        		rowidx = writeContractRollup(rowidx, wrapper);
        	}
    	}
        return this.workbook;
    }

    private void writeReportTitle() {
        CellStyle style = headerTableHeaderCellStyle();

        XSSFRow header = sheet1.createRow(1);
        header.setHeightInPoints((short) 30);

        String reportTitle = messageSource.getMessage("label_billing_report_title", null, locale);
        header.createCell(0).setCellValue(reportTitle);
        header.getCell(0).setCellStyle(style);

        header.createCell(1).setCellValue("");
        header.getCell(1).setCellStyle(style);
        
        sheet1.setColumnWidth(0, HEADER_FACTOR * reportTitle.length());
        //sheet1.setColumnWidth(1, HEADER_FACTOR * );
    }

    private void writeReportInfo(String billingPeriod) {
        CellStyle style = headerTableBodyCellStyle();

        // billing period
        writeContractInfoRow(style, 2, messageSource.getMessage("label_billingperiod_title", null, locale), billingPeriod);

        // report run date
        writeContractInfoRow(style, 3, messageSource.getMessage("label_report_run_title", null, locale),
                DateTimeFormat.forPattern("MM/dd/yyyy").print(new DateTime()));
    }
    
    private void writeContractInfoRow(CellStyle style, int rowidx, String label, String value) {
        XSSFRow row = sheet1.createRow(rowidx);
        row.setHeightInPoints((short) 16);
        
        row.createCell(0).setCellValue(label);
        row.getCell(0).setCellStyle(style);
        row.createCell(1).setCellValue(value);
        row.getCell(1).setCellStyle(style);
    }

    private Integer writeContractRollup(Integer rowidx, ContractServiceChangedConsolidatedWrapper wrapper) {
        String customerName = wrapper.getCustomer().getName();
    	Contract contract = wrapper.getContract();
    	String sdms = createSDMs(contract.getServiceDeliveryManagers());
    	String sowJobNumber = contract.getJobNumber();
        
        // baseline row
        CellStyle baselineStyle = baselineRowCellStyle();
        CellStyle baselineCurrencyStyle = baselineRowCellCurrencyStyle();
        XSSFRow row = sheet1.createRow(rowidx++);
        row.setHeightInPoints((short) 16);
        row.setRowStyle(baselineRowCellStyle());
        row.createCell(0).setCellValue(customerName);
        row.getCell(0).setCellStyle(baselineStyle);
        row.createCell(1).setCellValue(sdms);
        row.getCell(1).setCellStyle(baselineStyle);
        row.createCell(2).setCellValue(sowJobNumber);
        row.getCell(2).setCellStyle(baselineStyle);
        row.createCell(3).setCellValue(messageSource.getMessage("label_previous_month_mrc", null, locale));
        row.getCell(3).setCellStyle(baselineStyle);
        row.createCell(4).setCellValue(messageSource.getMessage("label_previous_month_description", null, locale));
        row.getCell(4).setCellStyle(baselineStyle);
        row.createCell(10).setCellValue(wrapper.getFormmattedPreviousMonthTotalOnetime().doubleValue());
        row.getCell(10).setCellStyle(baselineCurrencyStyle);
        row.createCell(11).setCellValue(wrapper.getFormattedPreviousMonthTotalRecurring().doubleValue());
        row.getCell(11).setCellStyle(baselineCurrencyStyle);
        
        for (Service service : wrapper.getAdded()) {
            writeContractRow(addedRowCellStyle(), addedRowCellDateStyle(), addedRowCellCurrencyStyle(), rowidx++, customerName, contract, service);
        }
        for (ContractAdjustment adjustment : wrapper.getAddedAdjustments()) {
            writeContractRow(addedRowCellStyle(), addedRowCellDateStyle(), addedRowCellCurrencyStyle(), rowidx++, customerName, contract, adjustment);
        }
        for (Service service : wrapper.getRemoved()) {
            writeContractRow(removedRowCellStyle(), removedRowCellDateStyle(), removedRowCellCurrencyStyle(), rowidx++, customerName, contract, service);
        }
        for (ContractAdjustment adjustment : wrapper.getRemovedAdjustments()) {
            writeContractRow(removedRowCellStyle(), removedRowCellDateStyle(), removedRowCellCurrencyStyle(), rowidx++, customerName, contract, adjustment);
        }
        
        // total row
        CellStyle style = totalCellStyle();
        CellStyle currencyStyle = totalCellCurrencyStyle();
        CellStyle leftStyle = totalCellLeftStyle();
        CellStyle grandTotalStyle = grandTotalCellCurrencyStyle();
        XSSFRow total = sheet1.createRow(rowidx++);
        total.setHeightInPoints((short) 16);
        total.createCell(0).setCellValue(customerName);
        total.getCell(0).setCellStyle(leftStyle);
        total.createCell(1).setCellValue(sdms);
        total.getCell(1).setCellStyle(leftStyle);
        total.createCell(2).setCellValue(sowJobNumber);
        total.getCell(2).setCellStyle(leftStyle);
        total.createCell(3).setCellValue("");
        total.getCell(3).setCellStyle(style);
        total.createCell(4).setCellValue("");
        total.getCell(4).setCellStyle(style);
        total.createCell(5).setCellValue("");
        total.getCell(5).setCellStyle(style);
        total.createCell(6).setCellValue("");
        total.getCell(6).setCellStyle(style);
        total.createCell(7).setCellValue("");
        total.getCell(7).setCellStyle(style);
        total.createCell(8).setCellValue("");
        total.getCell(8).setCellStyle(style);
        total.createCell(9).setCellValue(messageSource.getMessage("ui_col_total", null, locale));
        total.getCell(9).setCellStyle(style);
        
        Double oneTimeTotal = wrapper.getFormattedTotalOnetime().doubleValue();
        Double recurringTotal = wrapper.getFormattedTotalRecurring().doubleValue();
        total.createCell(10).setCellValue(oneTimeTotal);
        total.getCell(10).setCellStyle(currencyStyle);
        total.createCell(11).setCellValue(recurringTotal);
        total.getCell(11).setCellStyle(currencyStyle);
        
        Double monthTotal = oneTimeTotal + recurringTotal;
        total.createCell(12).setCellValue(monthTotal);
        total.getCell(12).setCellStyle(grandTotalStyle);
        
        //add a space after the total row
        XSSFRow spacer = sheet1.createRow(rowidx++);
        
        return rowidx;
    }
    
    private Integer writeContractHeader(Integer rowidx) {
        
        // header row
        CellStyle style = headerCellStyle();
        XSSFRow header = sheet1.createRow(rowidx++);
        header.setHeightInPoints((short) 16);

        int colidx = 0;
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_customer_name", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_sdm", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_contract_job_number", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_service_name", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_part_description", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_pcr", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_contract_update_job_number", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_quantity", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_start_date", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_end_date", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_one_time_cost", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_mrc", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_month_total", null, locale));
        header.getCell(colidx).setCellStyle(style);
        return rowidx;
    }
    
    private void writeContractRow(CellStyle style, CellStyle dateStyle, CellStyle currencyStyle, int rowidx, String customerName, Contract contract, Service service) {
    	XSSFRow row = sheet1.createRow(rowidx);
        row.setHeightInPoints((short) 16);
        row.setRowStyle(style);
        
        String sdms = createSDMs(contract.getServiceDeliveryManagers());
        
        int colidx = 0;
        row.createCell(colidx).setCellValue(customerName);
        row.getCell(colidx++).setCellStyle(style);
        row.createCell(colidx).setCellValue(sdms);
        row.getCell(colidx++).setCellStyle(style);
        row.createCell(colidx).setCellValue(contract.getJobNumber());
        row.getCell(colidx++).setCellStyle(style);
        
        String prefix = "";
        if(service.isProRatedAmount()) prefix = "Prorated Adjustment: ";
        String name = service.getName();
        row.createCell(colidx).setCellValue(prefix + name);
        row.getCell(colidx++).setCellStyle(style);
        row.createCell(colidx).setCellValue(service.getDeviceDescription());
        row.getCell(colidx++).setCellStyle(style);
        
        String pcrs = "";
        String pcrJobNumbers = "";
        for(ContractUpdate pcr : service.getContractUpdates()) {
        	String pcrName = pcr.getAltId();
        	if(!pcrs.contains(pcrName)) {
        		if(!"".equals(pcrs)) pcrs += ", ";
        		pcrs += pcrName;
        		
        		if(!"".equals(pcrJobNumbers)) pcrJobNumbers += ", ";
        		pcrJobNumbers += pcr.getJobNumber();
        	}
        }
        row.createCell(colidx).setCellValue(pcrs);
        row.getCell(colidx++).setCellStyle(style);
        
        row.createCell(colidx).setCellValue(pcrJobNumbers);
        row.getCell(colidx++).setCellStyle(style);
        
        row.createCell(colidx).setCellValue(service.getQuantity());
        row.getCell(colidx++).setCellStyle(style);
        row.createCell(colidx).setCellValue(service.getStartDate());
        row.getCell(colidx++).setCellStyle(dateStyle);
        row.createCell(colidx).setCellValue(service.getEndDate());
        row.getCell(colidx++).setCellStyle(dateStyle);
        row.createCell(colidx).setCellValue(service.getFormattedOnetimeRevenue().doubleValue());
        row.getCell(colidx++).setCellStyle(currencyStyle);
        row.createCell(colidx).setCellValue(service.getFormattedRecurringRevenue().doubleValue());
        row.getCell(colidx++).setCellStyle(currencyStyle);
        row.createCell(colidx).setCellValue("");
        row.getCell(colidx).setCellStyle(currencyStyle);
    }
    
    private void writeContractRow(CellStyle style, CellStyle dateStyle, CellStyle currencyStyle, int rowidx, String customerName, Contract contract, ContractAdjustment adjustment) {
    	XSSFRow row = sheet1.createRow(rowidx);
        row.setHeightInPoints((short) 16);
        row.setRowStyle(style);
        
        String sdms = createSDMs(contract.getServiceDeliveryManagers());
        
        int colidx = 0;
        row.createCell(colidx).setCellValue(customerName);
        row.getCell(colidx++).setCellStyle(style);
        row.createCell(colidx).setCellValue(sdms);
        row.getCell(colidx++).setCellStyle(style);
        row.createCell(colidx).setCellValue(contract.getJobNumber());
        row.getCell(colidx++).setCellStyle(style);
        row.createCell(colidx).setCellValue("Contract Adjustment");
        row.getCell(colidx++).setCellStyle(style);
        row.createCell(colidx).setCellValue("");
        row.getCell(colidx++).setCellStyle(style);
        
        String pcrs = "";
        String pcrJobNumbers = "";
        for(ContractUpdate pcr : adjustment.getContractUpdates()) {
        	String pcrName = pcr.getAltId();
        	String pcrJobNumber = pcr.getJobNumber();
        	if(!pcrs.contains(pcrName)) {
        		if(!"".equals(pcrs)) pcrs += ", ";
        		pcrs += pcrName;
        		if(!"".equals(pcrJobNumbers)) pcrJobNumbers += ", ";
        		if(pcrJobNumber != null) pcrJobNumbers += pcrJobNumber;
        	}
        }
        row.createCell(colidx).setCellValue(pcrs);
        row.getCell(colidx++).setCellStyle(style);
        
        row.createCell(colidx).setCellValue(pcrJobNumbers);
        row.getCell(colidx++).setCellStyle(style);
        
        row.createCell(colidx).setCellValue("");
        row.getCell(colidx++).setCellStyle(style);
        row.createCell(colidx).setCellValue(adjustment.getStartDate());
        row.getCell(colidx++).setCellStyle(dateStyle);
        
        Date endDate = adjustment.getEndDate();
        if(endDate == null) {
        	row.createCell(colidx).setCellValue("");
        } else {
        	row.createCell(colidx).setCellValue(endDate);
        }
        row.getCell(colidx++).setCellStyle(dateStyle);
        double total = 0.00;
        if("onetime".equals(adjustment.getAdjustmentType())) {
        	total = adjustment.getAdjustment().doubleValue();
        }
        row.createCell(colidx).setCellValue(total);
        row.getCell(colidx++).setCellStyle(currencyStyle);
        if("recurring".equals(adjustment.getAdjustmentType())) {
        	total = adjustment.getAdjustment().doubleValue();
        } else {
        	total = 0.00;
        }
        row.createCell(colidx).setCellValue(total);
        row.getCell(colidx++).setCellStyle(currencyStyle);
        row.createCell(colidx).setCellValue("");
        row.getCell(colidx).setCellStyle(currencyStyle);
    }
    
    private String createSDMs(List<Personnel> personnel) {
    	String sdms = "";
    	for(Personnel person : personnel) {
    		if(!"".equals(sdms)) sdms += ", ";
    		sdms += person.getUserName();
    	}
    	return sdms;
    }
    
    private CellStyle totalCellLeftStyle() {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName("Arial");
        style.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        style.setAlignment(CellStyle.ALIGN_LEFT);
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        font.setColor(HSSFColor.BLACK.index);
        font.setFontHeightInPoints((short) 16);
        style.setFont(font);
        return style;
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
        font.setFontHeightInPoints((short) 16);
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
        font.setFontHeightInPoints((short) 16);
        style.setFont(font);
        return style;
    }
    
    private CellStyle grandTotalCellCurrencyStyle() {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName("Arial");
        style.setFillForegroundColor(HSSFColor.GREY_80_PERCENT.index);
        style.setDataFormat((short) 8);
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        style.setAlignment(CellStyle.ALIGN_RIGHT);
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        font.setColor(HSSFColor.WHITE.index);
        font.setFontHeightInPoints((short) 16);
        style.setFont(font);
        return style;
    }
    
}
