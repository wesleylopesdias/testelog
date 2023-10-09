package com.logicalis.serviceinsight.web.view;

import com.logicalis.serviceinsight.dao.Location;
import com.logicalis.serviceinsight.data.Contract;
import com.logicalis.serviceinsight.data.ContractAdjustment;
import com.logicalis.serviceinsight.data.ContractGroup;
import com.logicalis.serviceinsight.data.ContractServiceChangedConsolidatedWrapper;
import com.logicalis.serviceinsight.data.ContractUpdate;
import com.logicalis.serviceinsight.data.Customer;
import com.logicalis.serviceinsight.data.Service;
import com.logicalis.serviceinsight.service.ApplicationDataDaoService;
import com.logicalis.serviceinsight.service.ContractDaoService;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Locale;

import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.springframework.context.MessageSource;

/**
 *
 * @author poneil
 */
public class SDMExportDocumentBuilder extends BaseDocumentBuilder {

    private MessageSource messageSource;
    private Locale locale;
    private XSSFSheet sheet1;
    static final String TITLE_SHEET1 = "SDM Export";
    static final int HEADER_FACTOR = 3 * 256;
    private ContractDaoService contractDaoService;
    private ApplicationDataDaoService applicationDataDaoService;
    
    public SDMExportDocumentBuilder(MessageSource messageSource,
            Locale locale, ContractDaoService contractDaoService, ApplicationDataDaoService applicationDataDaoService) {
        this.messageSource = messageSource;
        this.locale = locale;
        this.contractDaoService = contractDaoService;
        this.applicationDataDaoService = applicationDataDaoService;
        workbook = new XSSFWorkbook();
        sheet1 = workbook.createSheet(TITLE_SHEET1);
        sheet1.setDefaultColumnWidth(30);
    }

    public XSSFWorkbook buildSDMExportSpreadsheet(ContractServiceChangedConsolidatedWrapper wrapper) {
        writeCustomerInfo(wrapper.getCustomer());
        writeContractInfo(wrapper.getContract());
        Integer rowidx = writeContractRollup(11, wrapper);
        writeContractDetails(rowidx+3, wrapper);
        return this.workbook;
    }

    private void writeCustomerInfo(Customer customer) {
        CellStyle style = headerTableHeaderCellStyle();

        XSSFRow header = sheet1.createRow(1);
        header.setHeightInPoints((short) 30);

        String customerTitle = messageSource.getMessage("label_customer_title", null, locale);
        header.createCell(0).setCellValue(customerTitle);
        header.getCell(0).setCellStyle(style);

        header.createCell(1).setCellValue(customer.getName());
        header.getCell(1).setCellStyle(style);
        
        sheet1.setColumnWidth(0, HEADER_FACTOR * customerTitle.length());
        sheet1.setColumnWidth(1, HEADER_FACTOR * customer.getName().length());
    }

    private void writeContractInfo(Contract contract) {
        CellStyle style = headerTableBodyCellStyle();

        // SOW ID
        writeContractInfoRow(style, 2, messageSource.getMessage("ui_col_contract_id", null, locale), contract.getAltId());

        // SOW Signed Date
        writeContractInfoRow(style, 3, messageSource.getMessage("ui_col_signed_date", null, locale),
                DateTimeFormat.forPattern("MM/dd/yyyy").print(new DateTime(contract.getSignedDate())));

        // SOW Start Date
        writeContractInfoRow(style, 4, messageSource.getMessage("ui_col_start_date", null, locale),
                DateTimeFormat.forPattern("MM/dd/yyyy").print(new DateTime(contract.getStartDate())));

        // SOW End Date
        writeContractInfoRow(style, 5, messageSource.getMessage("ui_col_end_date", null, locale),
                DateTimeFormat.forPattern("MM/dd/yyyy").print(new DateTime(contract.getEndDate())));
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
        
        // title row
    	CellStyle titleStyle = titleCellStyle();
        XSSFRow titlerow = sheet1.createRow(rowidx++);
        titlerow.setHeightInPoints((short) 20);
        titlerow.setRowStyle(titleCellStyle());

        String billingTitle = messageSource.getMessage("label_billingperiod_title", null, locale);
        titlerow.createCell(0).setCellValue(billingTitle);
        titlerow.getCell(0).setCellStyle(titleStyle);
        titlerow.createCell(1).setCellValue(wrapper.getBillingPeriod());
        titlerow.getCell(1).setCellStyle(titleStyle);
        
        // header row
        rowidx = writeContractHeader(rowidx, false);
        
        // baseline row
        CellStyle baselineStyle = baselineRowCellStyle();
        CellStyle baselineCurrencyStyle = baselineRowCellCurrencyStyle();
        XSSFRow row = sheet1.createRow(rowidx++);
        row.setHeightInPoints((short) 16);
        row.setRowStyle(baselineStyle);
        row.createCell(0).setCellValue(messageSource.getMessage("label_previous_month_mrc", null, locale));
        row.getCell(0).setCellStyle(baselineStyle);
        row.createCell(6).setCellValue("");
        row.getCell(6).setCellStyle(baselineCurrencyStyle);
        row.createCell(7).setCellValue("");
        row.getCell(7).setCellStyle(baselineCurrencyStyle);
        
        for (Service service : wrapper.getPreviousMonth()) {
            writeContractRow(baselineRowFirstCellStyleLight(), baselineRowCellStyleLight(), baselineRowCellDateStyleLight(), baselineRowCellCurrencyStyleLight(), rowidx++, service, false, false, true);
        }
        for (ContractAdjustment adjustment : wrapper.getPreviousMonthAdjustments()) {
            writeContractRow(baselineRowFirstCellStyleLight(), baselineRowCellStyleLight(), baselineRowCellDateStyleLight(), baselineRowCellCurrencyStyleLight(), rowidx++, adjustment, false, false);
        }
        for (Service service : wrapper.getAdded()) {
            writeContractRow(addedRowCellStyle(), addedRowCellStyle(), addedRowCellDateStyle(), addedRowCellCurrencyStyle(), rowidx++, service, false, false, false);
        }
        for (ContractAdjustment adjustment : wrapper.getAddedAdjustments()) {
            writeContractRow(addedRowCellStyle(), addedRowCellStyle(), addedRowCellDateStyle(), addedRowCellCurrencyStyle(), rowidx++, adjustment, false, false);
        }
        for (Service service : wrapper.getRemoved()) {
            writeContractRow(removedRowCellStyle(), removedRowCellStyle(), removedRowCellDateStyle(), removedRowCellCurrencyStyle(), rowidx++, service, false, true, true);
        }
        for (ContractAdjustment adjustment : wrapper.getRemovedAdjustments()) {
            writeContractRow(removedRowCellStyle(), removedRowCellStyle(), removedRowCellDateStyle(), removedRowCellCurrencyStyle(), rowidx++, adjustment, false, true);
        }
        
        // total row
        CellStyle style = totalCellStyle();
        CellStyle currencyStyle = totalCellCurrencyStyle();
        XSSFRow total = sheet1.createRow(rowidx++);
        total.setHeightInPoints((short) 16);
        total.createCell(0).setCellValue("");
        total.getCell(0).setCellStyle(style);
        total.createCell(1).setCellValue("");
        total.getCell(1).setCellStyle(style);
        total.createCell(2).setCellValue("");
        total.getCell(2).setCellStyle(style);
        total.createCell(3).setCellValue("");
        total.getCell(3).setCellStyle(style);
        total.createCell(4).setCellValue("");
        total.getCell(4).setCellStyle(style);
        total.createCell(5).setCellValue(messageSource.getMessage("ui_col_total", null, locale));
        total.getCell(5).setCellStyle(style);
        total.createCell(6).setCellValue(wrapper.getFormattedTotalOnetime().doubleValue());
        total.getCell(6).setCellStyle(currencyStyle);
        total.createCell(7).setCellValue(wrapper.getFormattedTotalRecurring().doubleValue());
        total.getCell(7).setCellStyle(currencyStyle);
        
        return rowidx;
    }
    
    //used to remove related line items that aren't within the view date of the export
    private boolean serviceWithinViewDates(Service service, Date fromDate, Date toDate) {
    	boolean serviceWithinView = false;
    	
    	Date serviceStartDate = service.getStartDate();
    	Date serviceEndDate = service.getEndDate();
    	
    	if((fromDate.compareTo(serviceStartDate) * serviceStartDate.compareTo(toDate) >= 0) || (serviceStartDate.before(fromDate) && serviceEndDate.after(fromDate)) || (serviceStartDate.before(fromDate) && serviceEndDate.after(fromDate)) &&
    			(fromDate.compareTo(serviceEndDate) * serviceEndDate.compareTo(toDate) >= 0) || (serviceEndDate.after(toDate) || serviceEndDate.equals(fromDate))) {
    		serviceWithinView = true;
    	}
    	
    	return serviceWithinView;
    }

    private Integer writeContractDetails(Integer rowidx, ContractServiceChangedConsolidatedWrapper wrapper) {
        
    	Date fromDate = wrapper.getFromDate().toDate();
    	Date toDate = wrapper.getToDate().toDate();
    	
        // title row
    	CellStyle titleStyle = titleCellStyle();
        XSSFRow titlerow = sheet1.createRow(rowidx++);
        titlerow.setHeightInPoints((short) 20);
        titlerow.setRowStyle(titleCellStyle());

        String detailsTitle = messageSource.getMessage("label_detailview_title", null, locale);
        titlerow.createCell(0).setCellValue(detailsTitle);
        titlerow.getCell(0).setCellStyle(titleStyle);
        
        // header row
        rowidx = writeContractHeader(rowidx, true);
        
        for (Service service : wrapper.getPreviousMonthDetails()) {
            if (service.getParentId() == null) { // only shows children under parents...
                writeContractRow(baselineRowCellStyleLight(), baselineRowCellStyleLight(), baselineRowCellDateStyleLight(), baselineRowCellCurrencyStyleLight(), rowidx++, service, true, false, true);
                if (service.getRelatedLineItems() != null && service.getRelatedLineItems().size() > 0) {
                    for (Service child : service.getRelatedLineItems()) {
                    	if(serviceWithinViewDates(child, fromDate, toDate)) {
    	                    child.setName("child: " + child.getName());
    	                    writeContractRow(baselineRowCellStyleLight(), baselineRowCellStyleLight(), baselineRowCellDateStyleLight(), baselineRowCellCurrencyStyleLight(), rowidx++, child, true, false, true);
    	                    if (child.getRelatedLineItems() != null && child.getRelatedLineItems().size() > 0) {
    	                        for (Service grandchild : child.getRelatedLineItems()) {
    	                        	if(serviceWithinViewDates(grandchild, fromDate, toDate)) {
    	                        		grandchild.setName("grand-child: " + grandchild.getName());
    	                            	writeContractRow(baselineRowCellStyleLight(), baselineRowCellStyleLight(), baselineRowCellDateStyleLight(), baselineRowCellCurrencyStyleLight(), rowidx++, grandchild, true, false, true);
    	                        	}
    	                        }
    	                    }
                    	}
                    }
                }
            }
        }
        for (ContractAdjustment adjustment : wrapper.getPreviousMonthAdjustmentDetails()) {
        	writeContractRow(baselineRowCellStyleLight(), baselineRowCellStyleLight(), baselineRowCellDateStyleLight(), baselineRowCellCurrencyStyleLight(), rowidx++, adjustment, true, false);
        }
        for (Service service : wrapper.getAddedDetails()) {
            if (service.getParentId() == null) { // only shows children under parents...
                writeContractRow(addedRowCellStyle(), addedRowCellStyle(), addedRowCellDateStyle(), addedRowCellCurrencyStyle(), rowidx++, service, true, false, false);
                if (service.getRelatedLineItems() != null && service.getRelatedLineItems().size() > 0) {
                    for (Service child : service.getRelatedLineItems()) {
                        child.setName("child: " + child.getName());
                        writeContractRow(addedRowCellStyle(), addedRowCellStyle(), addedRowCellDateStyle(), addedRowCellCurrencyStyle(), rowidx++, child, true, false, true);
                        if (child.getRelatedLineItems() != null && child.getRelatedLineItems().size() > 0) {
                            for (Service grandchild : child.getRelatedLineItems()) {
                                grandchild.setName("grand-child: " + grandchild.getName());
                                writeContractRow(addedRowCellStyle(), addedRowCellStyle(), addedRowCellDateStyle(), addedRowCellCurrencyStyle(), rowidx++, grandchild, true, false, true);
                            }
                        }
                    }
                }
            }
        }
        for (ContractAdjustment adjustment : wrapper.getAddedAdjustmentDetails()) {
        	writeContractRow(addedRowCellStyle(), addedRowCellStyle(), addedRowCellDateStyle(), addedRowCellCurrencyStyle(), rowidx++, adjustment, true, false);
        }
        
        /*
        for (Service service : wrapper.getRemovedDetails()) {
            writeContractRow(removedRowCellStyle(), removedRowCellStyle(), removedRowCellDateStyle(), removedRowCellCurrencyStyle(), rowidx++, service, true, true, false);
        }
        for (ContractAdjustment adjustment : wrapper.getRemovedAdjustmentDetails()) {
            writeContractRow(removedRowCellStyle(), removedRowCellStyle(), removedRowCellDateStyle(), removedRowCellCurrencyStyle(), rowidx++, adjustment, true, true);
        }*/
        
        // total row
        CellStyle style = totalCellStyle();
        CellStyle currencyStyle = totalCellCurrencyStyle();
        XSSFRow total = sheet1.createRow(rowidx++);
        int colidx = 0;
        total.setHeightInPoints((short) 16);
        total.createCell(colidx).setCellValue("");
        total.getCell(colidx++).setCellStyle(style);
        total.createCell(colidx).setCellValue("");
        total.getCell(colidx++).setCellStyle(style);
        total.createCell(colidx).setCellValue("");
        total.getCell(colidx++).setCellStyle(style);
        total.createCell(colidx).setCellValue("");
        total.getCell(colidx++).setCellStyle(style);
        total.createCell(colidx).setCellValue("");
        total.getCell(colidx++).setCellStyle(style);
        total.createCell(colidx).setCellValue("");
        total.getCell(colidx++).setCellStyle(style);
        total.createCell(colidx).setCellValue("");
        total.getCell(colidx++).setCellStyle(style);
        total.createCell(colidx).setCellValue("");
        total.getCell(colidx++).setCellStyle(style);
        total.createCell(colidx).setCellValue("");
        total.getCell(colidx++).setCellStyle(style);
        total.createCell(colidx).setCellValue("");
        total.getCell(colidx++).setCellStyle(style);
        total.createCell(colidx).setCellValue("");
        total.getCell(colidx++).setCellStyle(style);
        total.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_total", null, locale));
        total.getCell(colidx++).setCellStyle(style);
        total.createCell(colidx).setCellValue(wrapper.getFormattedTotalOnetime().doubleValue());
        total.getCell(colidx++).setCellStyle(currencyStyle);
        total.createCell(colidx).setCellValue(wrapper.getFormattedTotalRecurring().doubleValue());
        total.getCell(colidx).setCellStyle(currencyStyle);
        
        return rowidx;
    }
    
    private Integer writeContractHeader(Integer rowidx, boolean isdetail) {
        
        // header row
        CellStyle style = headerCellStyle();
        XSSFRow header = sheet1.createRow(rowidx++);
        header.setHeightInPoints((short) 16);

        int colidx = 0;
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_service_name", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_part_description", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        if (isdetail) {
            header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_service_code", null, locale));
            header.getCell(colidx++).setCellStyle(style);
        	header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_contract_group", null, locale));
            header.getCell(colidx++).setCellStyle(style);
        }
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_pcr", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        if (isdetail) {
            header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_ci_name", null, locale));
            header.getCell(colidx++).setCellStyle(style);
        }
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_service_quantity", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        if (isdetail) {
        	header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_device_unit_count", null, locale));
        	header.getCell(colidx++).setCellStyle(style);
        	header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_cost_location", null, locale));
        	header.getCell(colidx++).setCellStyle(style);
        	header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_notes", null, locale));
        	header.getCell(colidx++).setCellStyle(style);
        }
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_start_date", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_end_date", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_one_time_cost", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_mrc", null, locale));
        header.getCell(colidx).setCellStyle(style);
        
        return rowidx;
    }
    
    private void writeContractRow(CellStyle firstCellStyle, CellStyle style, CellStyle dateStyle, CellStyle currencyStyle, int rowidx, Service service, boolean isdetail, boolean isRemoved, boolean isPrevMonth) {
    	XSSFRow row = sheet1.createRow(rowidx);
        row.setHeightInPoints((short) 16);
        row.setRowStyle(style);
        
        int colidx = 0;
        String prefix = "";
        if(service.isProRatedAmount()) prefix = "Prorated Adjustment: ";
        String name = service.getName();
        row.createCell(colidx).setCellValue(prefix + name);
        row.getCell(colidx++).setCellStyle(firstCellStyle);
        row.createCell(colidx).setCellValue(service.getDeviceDescription());
        row.getCell(colidx++).setCellStyle(style);
        
        if(isdetail) {
            row.createCell(colidx).setCellValue(service.getDevicePartNumber());
            row.getCell(colidx++).setCellStyle(style);
            String contractGroupName = getContractGroupName(service.getContractGroupId());
            row.createCell(colidx).setCellValue(contractGroupName);
            row.getCell(colidx++).setCellStyle(style);
        }
        
        String pcrs = "";
        for(ContractUpdate pcr : service.getContractUpdates()) {
        	String pcrName = pcr.getAltId();
        	if(!pcrs.contains(pcrName)) {
        		if(!"".equals(pcrs)) pcrs += ", ";
        		pcrs += pcrName;
        	}
        }
        row.createCell(colidx).setCellValue(pcrs);
        row.getCell(colidx++).setCellStyle(style);
        if(isdetail) {
            row.createCell(colidx).setCellValue(service.getDeviceName());
            row.getCell(colidx++).setCellStyle(style);
        }
        row.createCell(colidx).setCellValue(service.getQuantity());
        row.getCell(colidx++).setCellStyle(style);
        if (isdetail) {
        	Integer deviceUnitCount = service.getDeviceUnitCount();
        	if(deviceUnitCount != null) {
        		row.createCell(colidx).setCellValue(deviceUnitCount);
        	} else {
        		row.createCell(colidx).setCellValue("");
        	}
            row.getCell(colidx++).setCellStyle(style);
            
            String location = "";
            if(service.getLocationId() != null) location = getLocationName(service.getLocationId());
            row.createCell(colidx).setCellValue(location);
            row.getCell(colidx++).setCellStyle(style);
            row.createCell(colidx).setCellValue(service.getNote());
            row.getCell(colidx++).setCellStyle(style);
        }
        row.createCell(colidx).setCellValue(service.getStartDate());
        row.getCell(colidx++).setCellStyle(dateStyle);
        row.createCell(colidx).setCellValue(service.getEndDate());
        row.getCell(colidx++).setCellStyle(dateStyle);
        BigDecimal onetimeRevenue = service.getFormattedOnetimeRevenue();
        BigDecimal recurringRevenue = service.getFormattedRecurringRevenue();
        
        if(isPrevMonth) onetimeRevenue = new BigDecimal(0);
        
        if(isRemoved) {
        	onetimeRevenue = onetimeRevenue.multiply(new BigDecimal(-1));
        	recurringRevenue = recurringRevenue.multiply(new BigDecimal(-1));
        }
        		
        row.createCell(colidx).setCellValue(onetimeRevenue.doubleValue());
        row.getCell(colidx++).setCellStyle(currencyStyle);
        row.createCell(colidx).setCellValue(recurringRevenue.doubleValue());
        row.getCell(colidx).setCellStyle(currencyStyle);
    }
    
    private void writeContractRow(CellStyle firstCellStyle, CellStyle style, CellStyle dateStyle, CellStyle currencyStyle, int rowidx, ContractAdjustment adjustment, boolean isdetail, boolean isRemoved) {
    	XSSFRow row = sheet1.createRow(rowidx);
        row.setHeightInPoints((short) 16);
        row.setRowStyle(style);
        
        int colidx = 0;
        row.createCell(colidx).setCellValue("Contract Adjustment");
        row.getCell(colidx++).setCellStyle(firstCellStyle);
        row.createCell(colidx).setCellValue("");
        row.getCell(colidx++).setCellStyle(style);
        
        if (isdetail) {
            row.createCell(colidx).setCellValue("");
            row.getCell(colidx++).setCellStyle(style);
            String contractGroupName = getContractGroupName(adjustment.getContractGroupId());
            row.createCell(colidx).setCellValue(contractGroupName);
            row.getCell(colidx++).setCellStyle(style);
        }
        
        String pcrs = "";
        for(ContractUpdate pcr : adjustment.getContractUpdates()) {
        	String pcrName = pcr.getAltId();
        	if(!pcrs.contains(pcrName)) {
        		if(!"".equals(pcrs)) pcrs += ", ";
        		pcrs += pcrName;
        	}
        }
        row.createCell(colidx).setCellValue(pcrs);
        
        row.getCell(colidx++).setCellStyle(style);
        
        if (isdetail) {
            row.createCell(colidx).setCellValue("");
            row.getCell(colidx++).setCellStyle(style);
        }
        
        row.createCell(colidx).setCellValue("");
        row.getCell(colidx++).setCellStyle(style);
        if (isdetail) {
            row.createCell(colidx).setCellValue("");
            row.getCell(colidx++).setCellStyle(style);
            row.createCell(colidx).setCellValue("");
            row.getCell(colidx++).setCellStyle(style);
            row.createCell(colidx).setCellValue(adjustment.getNote());
            row.getCell(colidx++).setCellStyle(style);
        }
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
        BigDecimal adjustmentAmount = adjustment.getAdjustment();
        if(isRemoved) {
        	adjustmentAmount = adjustmentAmount.multiply(new BigDecimal(-1));
        }
        
        if("onetime".equals(adjustment.getAdjustmentType())) {
        	total = adjustmentAmount.doubleValue();
        }
        row.createCell(colidx).setCellValue(total);
        row.getCell(colidx++).setCellStyle(currencyStyle);
        if("recurring".equals(adjustment.getAdjustmentType())) {
        	total = adjustmentAmount.doubleValue();
        } else {
        	total = 0.00;
        }
        
        row.createCell(colidx).setCellValue(total);
        row.getCell(colidx).setCellStyle(currencyStyle);
    }
    
    private String getContractGroupName(Long contractGroupId) {
    	String contractGroupName = "";
    	if(contractGroupId != null) {
	    	try {
	    		ContractGroup contractGroup = contractDaoService.contractGroup(contractGroupId);
	    		contractGroupName = contractGroup.getName();
	    	} catch (Exception e) {
	    		e.printStackTrace();
	    	}
    	}
    	return contractGroupName;
    }
    
    private String getLocationName(Integer locationId) {
    	String locationName = "";
    	if(locationId != null) {
	    	try {
	    		Location location = applicationDataDaoService.location(locationId);
	    		locationName = location.getName();
	    	} catch (Exception e) {
	    		e.printStackTrace();
	    	}
    	}
    	return locationName;
    }
    
    private CellStyle titleCellStyle() {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName("Arial");
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
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
    
    private CellStyle baselineRowCellStyleLight() {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName("Arial");
        font.setColor(HSSFColor.GREY_50_PERCENT.index);
        font.setFontHeightInPoints((short) 12);
        font.setItalic(true);
        style.setFont(font);
        return style;
    }
    
    private CellStyle baselineRowCellDateStyleLight() {
        CellStyle style = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        Font font = workbook.createFont();
        font.setFontName("Arial");
        font.setColor(HSSFColor.GREY_50_PERCENT.index);
        style.setAlignment(CellStyle.ALIGN_RIGHT);
        style.setDataFormat(createHelper.createDataFormat().getFormat("mm/dd/yyyy"));
        font.setFontHeightInPoints((short) 12);
        font.setItalic(true);
        style.setFont(font);
        return style;
    }
    
    private CellStyle baselineRowCellCurrencyStyleLight() {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName("Arial");
        font.setColor(HSSFColor.GREY_50_PERCENT.index);
        style.setAlignment(CellStyle.ALIGN_RIGHT);
        style.setDataFormat((short) 8);
        font.setFontHeightInPoints((short) 12);
        font.setItalic(true);
        style.setFont(font);
        return style;
    }
    
    private CellStyle baselineRowFirstCellStyleLight() {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName("Arial");
        font.setColor(HSSFColor.GREY_50_PERCENT.index);
        font.setFontHeightInPoints((short) 12);
        font.setItalic(true);
        style.setFont(font);
        style.setIndention((short) 1);
        return style;
    }
    

}
