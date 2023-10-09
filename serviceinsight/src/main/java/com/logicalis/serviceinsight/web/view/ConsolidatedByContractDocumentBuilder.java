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

public class ConsolidatedByContractDocumentBuilder extends BaseDocumentBuilder {

    private MessageSource messageSource;
    private Locale locale;
    private XSSFSheet sheet1;
    static final String TITLE_SHEET1 = "RMO Export";
    static final int HEADER_FACTOR = 2 * 256;

    @Autowired
    ContractDaoService contractDaoService;
    
    public ConsolidatedByContractDocumentBuilder(MessageSource messageSource, Locale locale) {
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
    	String sdms = createSDMs(wrapper.getContract().getServiceDeliveryManagers());
    	String sowJobNumber = contract.getJobNumber();
    	Date endDate = contract.getEndDate();
    	String sowName = contract.getName();
    	Integer deviceCount = 0;
    	
    	//get device count
    	for(Service service : wrapper.getPreviousMonth()) {
    		deviceCount += service.getQuantity();
    	}
    	for(Service service : wrapper.getAdded()) {
    		deviceCount += service.getQuantity();
    	}
    	for(Service service : wrapper.getRemoved()) {
    		deviceCount -= service.getQuantity();
    	}
        
        // baseline row
        CellStyle baselineStyle = baselineRowCellStyle();
        CellStyle baselineCurrencyStyle = baselineRowCellCurrencyStyle();
        CellStyle dateStyle = baselineRowDateStyle();
        XSSFRow row = sheet1.createRow(rowidx++);
        row.setHeightInPoints((short) 16);
        row.setRowStyle(baselineRowCellStyle());
        
        int colidx = 0;
        row.createCell(colidx).setCellValue(customerName);
        row.getCell(colidx++).setCellStyle(baselineStyle);
        row.createCell(colidx).setCellValue(sowName);
        row.getCell(colidx++).setCellStyle(baselineStyle);
        row.createCell(colidx).setCellValue(sdms);
        row.getCell(colidx++).setCellStyle(baselineStyle);
        row.createCell(colidx).setCellValue(sowJobNumber);
        row.getCell(colidx++).setCellStyle(baselineStyle);
        row.createCell(colidx).setCellValue(endDate);
        row.getCell(colidx++).setCellStyle(dateStyle);
        row.createCell(colidx).setCellValue(deviceCount);
        row.getCell(colidx++).setCellStyle(baselineStyle);
        Double oneTimeTotal = wrapper.getFormattedTotalOnetime().doubleValue();
        Double recurringTotal = wrapper.getFormattedTotalRecurring().doubleValue();
        row.createCell(colidx).setCellValue(oneTimeTotal);
        row.getCell(colidx++).setCellStyle(baselineCurrencyStyle);
        row.createCell(colidx).setCellValue(recurringTotal);
        row.getCell(colidx++).setCellStyle(baselineCurrencyStyle);
        Double monthTotal = oneTimeTotal + recurringTotal;
        row.createCell(colidx).setCellValue(monthTotal);
        row.getCell(colidx++).setCellStyle(baselineCurrencyStyle);

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
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_contract_name", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_sdm", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_contract_job_number", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_contract_end_date", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_device_count", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_one_time_cost", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_mrc", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_month_total", null, locale));
        header.getCell(colidx).setCellStyle(style);
        return rowidx;
    }
    
    private String createSDMs(List<Personnel> personnel) {
    	String sdms = "";
    	for(Personnel person : personnel) {
    		if(!"".equals(sdms)) sdms += ", ";
    		sdms += person.getUserName();
    	}
    	return sdms;
    }
    
}
