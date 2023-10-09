package com.logicalis.serviceinsight.web.view;

import java.math.BigDecimal;
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
import org.springframework.util.StringUtils;

import com.logicalis.serviceinsight.data.Contract;
import com.logicalis.serviceinsight.data.Personnel;
import com.logicalis.serviceinsight.representation.LaborHoursRecord;

public class RenewalsReportDocumentBuilder extends BaseDocumentBuilder {

	private MessageSource messageSource;
    private Locale locale;
    private XSSFSheet sheet1;
    private BigDecimal total;
    private Boolean isManager = Boolean.FALSE;
    static final String TITLE_SHEET1 = "Renewals Export";
    static final int HEADER_FACTOR = 3 * 256;

    public RenewalsReportDocumentBuilder(MessageSource messageSource, Locale locale) {
        this.messageSource = messageSource;
        this.locale = locale;
        workbook = new XSSFWorkbook();
        sheet1 = workbook.createSheet(TITLE_SHEET1);
        sheet1.setDefaultColumnWidth(30);
        this.total = new BigDecimal(0);
    }

    public XSSFWorkbook buildRenewalsExportSpreadsheet(List<Contract> report) {
        Integer rowidx = 1;
        writeReportData(rowidx, report);
        return this.workbook;
    }

    private void writeReportData(Integer rowidx, List<Contract> report) {
        //write out header
        rowidx = writeReportDataHeader(rowidx);

        for (Contract reportRecord : report) {
            rowidx = writeReportDataRow(rowidx, reportRecord);
        }
    }

    private int writeReportDataHeader(Integer rowidx) {
        CellStyle style = headerCellStyle();

        int colidx = 0;
        XSSFRow header = sheet1.createRow(rowidx);
        header = sheet1.createRow(rowidx++);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_customer", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_contract_id", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_contract_name", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_job_number", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_ae", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_epe", null, locale));
        header.getCell(colidx++).setCellStyle(style);
    	header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_sdm", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_bsc", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_service_start_date", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_contract_end_date", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_renewal_status", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_renewal_change", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_renewal_notes", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_nrc", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_mrc", null, locale));
        header.getCell(colidx++).setCellStyle(style);

        return rowidx;
    }

    private int writeReportDataRow(Integer rowidx, Contract record) {
        CellStyle style = baselineRowCellStyle();
        CellStyle dateStyle = baselineRowDateStyle();

        XSSFRow header = sheet1.createRow(rowidx);
        header.setHeightInPoints((short) 30);

        header = sheet1.createRow(rowidx++);
        int colidx = 0;
        header.createCell(colidx).setCellValue(record.getCustomerName());
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(record.getAltId());
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(record.getName());
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(record.getJobNumber());
        header.getCell(colidx++).setCellStyle(style);
        String aeName = "";
        String epeName = "";
        String sdmName = "";
        String bscName = "";
        if(record.getAccountExecutive() != null) aeName = record.getAccountExecutive().getUserName();
        if(record.getEnterpriseProgramExecutive() != null) epeName = record.getEnterpriseProgramExecutive().getUserName();
        List<Personnel> sdms = record.getServiceDeliveryManagers();
        if(sdms != null && !sdms.isEmpty()) {
        	int count = 0;
        	for(Personnel sdm : sdms) {
        		if(count > 0) sdmName += ", ";
        		sdmName = sdm.getUserName();
        	}
        }
        List<Personnel> bscs = record.getBusinessSolutionsConsultants();
        if(bscs != null && !bscs.isEmpty()) {
        	int count = 0;
        	for(Personnel bsc : bscs) {
        		if(count > 0) bscName += ", ";
        		bscName = bsc.getUserName();
        	}
        }
        header.createCell(colidx).setCellValue(aeName);
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(epeName);
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(sdmName);
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(bscName);
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(record.getServiceStartDate());
        header.getCell(colidx++).setCellStyle(dateStyle);
        header.createCell(colidx).setCellValue(record.getEndDate());
        header.getCell(colidx++).setCellStyle(dateStyle);
        header.createCell(colidx).setCellValue(record.getRenewalStatusDisplay());
        header.getCell(colidx++).setCellStyle(style);
        
        String change = "No change";
        if(record.getRenewalChange().compareTo(new BigDecimal(0)) > 0) {
        	change = record.getRenewalChange().toString() + "% Increase";
        } else if(record.getRenewalChange().compareTo(new BigDecimal(0)) < 0) {
        	change = Math.abs(record.getRenewalChange().doubleValue()) + "% Decrease";
        }
        header.createCell(colidx).setCellValue(change);
        header.getCell(colidx++).setCellStyle(style);
        String renewalNotes = "";
        log.info("NOtes: " + record.getRenewalNotes());
        if(StringUtils.hasLength(record.getRenewalNotes())) {
        	renewalNotes = record.getRenewalNotes().replaceAll("\\n", " -- ");
        }
        log.info("Notes: " + renewalNotes);
        header.createCell(colidx).setCellValue(renewalNotes);
        header.getCell(colidx++).setCellStyle(style);
        
        header.createCell(colidx).setCellValue(record.getMonthTotalOnetimeRevenue().doubleValue());
        header.getCell(colidx++).setCellStyle(baselineRowCellCurrencyStyle());
        header.createCell(colidx).setCellValue(record.getMonthTotalRecurringRevenue().doubleValue());
        header.getCell(colidx++).setCellStyle(baselineRowCellCurrencyStyle());
        
        return rowidx;
    }
	
}
