package com.logicalis.serviceinsight.web.view;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.MessageSource;

import com.logicalis.serviceinsight.dao.AssetItem;
import com.logicalis.serviceinsight.dao.UnitCost;
import com.logicalis.serviceinsight.data.Device;
import com.logicalis.serviceinsight.data.ExpenseCategoryReportWrapper;
import com.logicalis.serviceinsight.data.Service;
import com.logicalis.serviceinsight.data.UnitCostDetails;
import com.logicalis.serviceinsight.data.UnitCostDetails.AssetDetail;
import com.logicalis.serviceinsight.data.UnitCostDetails.CostDetail;
import com.logicalis.serviceinsight.data.UnitCostDetails.LaborDetail;
import com.logicalis.serviceinsight.representation.LaborBreakdownRecord;
import com.logicalis.serviceinsight.representation.RevenueByServiceRecord;
import com.logicalis.serviceinsight.representation.RevenueReportResultRecord;

public class ExpenseCategoryReportDocumentBuilder extends BaseDocumentBuilder {

    private MessageSource messageSource;
    private Locale locale;
    private XSSFSheet sheet1;
    private BigDecimal expenseTotal;
    static final String TITLE_SHEET1 = "Expense Category Export";
    static final int HEADER_FACTOR = 3 * 256;

    public ExpenseCategoryReportDocumentBuilder(MessageSource messageSource, Locale locale) {
        this.messageSource = messageSource;
        this.locale = locale;
        workbook = new XSSFWorkbook();
        sheet1 = workbook.createSheet(TITLE_SHEET1);
        sheet1.setDefaultColumnWidth(30);
        this.expenseTotal = new BigDecimal(0);
    }

    public XSSFWorkbook buildExpenseCategoryExportSpreadsheet(ExpenseCategoryReportWrapper report) {
        Integer rowidx = 1;
        writeReportData(rowidx, report);
        return this.workbook;
    }
    
    private void writeReportData(Integer rowidx, ExpenseCategoryReportWrapper report) {
    	UnitCostDetails details = report.getUnitCostDetails();
    	
    	//write summary information
    	rowidx = createSectionTitle(rowidx, messageSource.getMessage("ui_title_report_cost_summary", null, locale));
    	rowidx = writeSummaryData(rowidx, report);
    	rowidx++;
    	rowidx++;
    	
    	//write service details table
    	rowidx = createSectionTitle(rowidx, messageSource.getMessage("ui_title_mapped_devices", null, locale));
    	rowidx = writeMappedDeviceData(rowidx, report.getAssociatedDevices());
    	rowidx++;
    	rowidx++;
    	
    	//write expense details table
    	rowidx = createSectionTitle(rowidx, messageSource.getMessage("ui_title_cost_details", null, locale));
    	rowidx = writeExpenseDetailData(rowidx, details);
    	rowidx++;
    	rowidx++;
    	
    	rowidx = createSectionTitle(rowidx, messageSource.getMessage("ui_title_category_history", null, locale));
    	rowidx = writeDeviceCountHistoryData(rowidx, report.getPreviousMonths());
    }
    
    private Integer writeSummaryData(Integer rowidx, ExpenseCategoryReportWrapper report) {
    	CellStyle headerStyle = headerCellStyle();
    	CellStyle style = baselineRowCellStyle();
        CellStyle currency = baselineRowCellCurrencyStyle();
    	UnitCostDetails details = report.getUnitCostDetails();
    	
    	int colidx = 0;
        XSSFRow header = sheet1.createRow(rowidx);
        header = sheet1.createRow(rowidx++);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_cost_category", null, locale));
        header.getCell(colidx++).setCellStyle(headerStyle);
        String category = report.getCategoryName();
        header.createCell(colidx).setCellValue(category);
        header.getCell(colidx++).setCellStyle(style);
        
        colidx = 0;
        header = sheet1.createRow(rowidx);
        header = sheet1.createRow(rowidx++);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_month", null, locale));
        header.getCell(colidx++).setCellStyle(headerStyle);
        String month = report.getMonth();
        header.createCell(colidx).setCellValue(month);
        header.getCell(colidx++).setCellStyle(style);
    	
        colidx = 0;
        header = sheet1.createRow(rowidx);
        header = sheet1.createRow(rowidx++);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_total_devices", null, locale));
        header.getCell(colidx++).setCellStyle(headerStyle);
        Integer units = 0;
        if(details.getUnitCost() != null && details.getUnitCost().getDeviceTotalUnits() != null) {
        	units = details.getUnitCost().getDeviceTotalUnits();
        }
        header.createCell(colidx).setCellValue(units);
        header.getCell(colidx++).setCellStyle(style);
        
        colidx = 0;
        header = sheet1.createRow(rowidx++);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_average_device_cost", null, locale));
        header.getCell(colidx++).setCellStyle(headerStyle);
        
        BigDecimal unitCost = new BigDecimal(0);
        UnitCost unitCostRec = details.getUnitCost();
    	if(!units.equals(BigDecimal.ZERO) && unitCostRec != null) {
    		unitCost = unitCostRec.getTotalCost().add(unitCostRec.getTotalLabor());
    		unitCost = unitCost.divide(new BigDecimal(units), 2, RoundingMode.HALF_UP);
    	}
        header.createCell(colidx).setCellValue(unitCost.doubleValue());
        header.getCell(colidx++).setCellStyle(currency);
    	
    	return rowidx;
    }

    private Integer writeExpenseDetailData(Integer rowidx, UnitCostDetails details) {
        //write out header
        rowidx = writeExpenseDetailHeader(rowidx);

        List<GenericExpense> expenses = new ArrayList<GenericExpense>();
        for(AssetDetail asset : details.getAssets()) {
        	expenses.add(new GenericExpense("Asset", asset.getAcquiredDate().toDate(), asset.getDescription(), asset.getDepreciation()));
        }
        for(CostDetail cost : details.getCosts()) {
        	expenses.add(new GenericExpense("Expense", cost.getAppliedDate().toDate(), cost.getDescription(), cost.getAmount()));
        }
        for(LaborDetail labor : details.getLabor()) {
        	expenses.add(new GenericExpense("Labor", labor.getWorkDate().toDate(), labor.getWorker(), labor.getLaborFormatted()));
        }
        
        for (GenericExpense reportRecord : expenses) {
            rowidx = writeExpenseDetailRow(rowidx, reportRecord);
        }
        
        rowidx = writeExpenseDetailTotal(rowidx);
        
        return rowidx;
    }

    private int writeExpenseDetailHeader(Integer rowidx) {
        CellStyle style = headerCellStyle();

        int colidx = 0;
        XSSFRow header = sheet1.createRow(rowidx);
        header = sheet1.createRow(rowidx++);
        //header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_business_model", null, locale));
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_asset_expense", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_cost_date", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_name", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_cost_amount", null, locale));
        header.getCell(colidx++).setCellStyle(style);

        return rowidx;
    }

    private int writeExpenseDetailRow(Integer rowidx, GenericExpense record) {
        CellStyle style = baselineRowCellStyle();
        CellStyle dateStyle = baselineRowDateStyle();
        CellStyle currency = baselineRowCellCurrencyStyle();

        XSSFRow header = sheet1.createRow(rowidx);
        header.setHeightInPoints((short) 30);

        header = sheet1.createRow(rowidx++);
        int colidx = 0;
        header.createCell(colidx).setCellValue(record.getType());
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(record.getDate());
        header.getCell(colidx++).setCellStyle(dateStyle);
        header.createCell(colidx).setCellValue(record.getName());
        header.getCell(colidx++).setCellStyle(currency);
        header.createCell(colidx).setCellValue(record.getAmount().doubleValue());
        header.getCell(colidx).setCellStyle(currency);

        expenseTotal = expenseTotal.add(record.getAmount());
        
        return rowidx;
    }

    private int writeExpenseDetailTotal(Integer rowidx) {
    	CellStyle style = totalCellStyle();
        CellStyle currency = totalCellCurrencyStyle();
        
    	int colidx = 0;
        XSSFRow header = sheet1.createRow(rowidx);
        header = sheet1.createRow(rowidx++);
        header.createCell(colidx).setCellValue("");
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue("");
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_cost_total_cost", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(expenseTotal.doubleValue());
        header.getCell(colidx).setCellStyle(currency);
        
    	return rowidx;
    }
    
    private Integer writeDeviceCountHistoryData(Integer rowidx, Map<String, UnitCost> previousMonths) {
    	CellStyle headerStyle = headerCellStyle();
    	CellStyle style = baselineRowCellStyle();
        CellStyle currency = baselineRowCellCurrencyStyle();
    	
        int colidx = 0;
        XSSFRow header = sheet1.createRow(rowidx);
        header = sheet1.createRow(rowidx++);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_date", null, locale));
        header.getCell(colidx++).setCellStyle(headerStyle);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_cost_units", null, locale));
        header.getCell(colidx++).setCellStyle(headerStyle);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_cost_unit_cost", null, locale));
        header.getCell(colidx++).setCellStyle(headerStyle);
        Map<String, UnitCost> months = previousMonths;
        
        Iterator it = months.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            colidx = 0;
            header = sheet1.createRow(rowidx++);
            header.createCell(colidx).setCellValue(pair.getKey().toString());
            header.getCell(colidx++).setCellStyle(style);
            
            Integer units = 0;
            BigDecimal unitCost = new BigDecimal(0);
            if(pair.getValue() != null) {
            	UnitCost unitCostRec = (UnitCost) pair.getValue();
            	units = unitCostRec.getDeviceTotalUnits();
            	if(!units.equals(BigDecimal.ZERO) && unitCostRec != null) {
            		unitCost = unitCostRec.getTotalCost().add(unitCostRec.getTotalLabor());
            		unitCost = unitCost.divide(new BigDecimal(units), 2, RoundingMode.HALF_UP);
            	}
            }
            header.createCell(colidx).setCellValue(units);
            header.getCell(colidx++).setCellStyle(style);
            header.createCell(colidx).setCellValue(unitCost.doubleValue());
            header.getCell(colidx++).setCellStyle(currency);
            it.remove(); // avoids a ConcurrentModificationException
        }
    	
    	return rowidx;
    }
    
    private Integer writeMappedDeviceData(Integer rowidx, List<Device> devices) {
    	CellStyle headerStyle = headerCellStyle();
    	CellStyle style = baselineRowCellStyle();
    	
        int colidx = 0;
        XSSFRow header = sheet1.createRow(rowidx);
        header = sheet1.createRow(rowidx++);
        header.createCell(colidx).setCellValue("Part Number");
        header.getCell(colidx++).setCellStyle(headerStyle);
        header.createCell(colidx).setCellValue("Description");
        header.getCell(colidx++).setCellStyle(headerStyle);
        
        for (Device device : devices) {
            colidx = 0;
            header = sheet1.createRow(rowidx++);
            header.createCell(colidx).setCellValue(device.getPartNumber());
            header.getCell(colidx++).setCellStyle(style);
            header.createCell(colidx).setCellValue(device.getDescription());
            header.getCell(colidx++).setCellStyle(style);
        }
    	
    	return rowidx;
    }
    
    private Integer createSectionTitle(Integer rowidx, String title) {
    	CellStyle style = titleCellStyle();
    	
        int colidx = 0;
        XSSFRow header = sheet1.createRow(rowidx);
        header = sheet1.createRow(rowidx++);
        header.createCell(colidx).setCellValue(title);
        header.getCell(colidx++).setCellStyle(style);
    	
    	return rowidx;
    }
    
    private CellStyle titleCellStyle() {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName("Arial");
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
    
    private class GenericExpense {
    	private String type;
    	private Date date;
    	private String name;
    	private BigDecimal amount;
    	
    	public GenericExpense(){}
    	
		public GenericExpense(String type, Date date, String name, BigDecimal amount) {
			super();
			this.type = type;
			this.date = date;
			this.name = name;
			this.amount = amount;
		}

		public String getType() {
			return type;
		}
		
		public void setType(String type) {
			this.type = type;
		}
		
		public Date getDate() {
			return date;
		}
		
		public void setDate(Date date) {
			this.date = date;
		}
		
		public String getName() {
			return name;
		}
		
		public void setName(String name) {
			this.name = name;
		}
		
		public BigDecimal getAmount() {
			return amount;
		}
		
		public void setAmount(BigDecimal amount) {
			this.amount = amount;
		}
    	
    	
    }
}
