package com.logicalis.serviceinsight.web.view;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.springframework.context.MessageSource;

import com.logicalis.serviceinsight.data.ReportWrapper;
import com.logicalis.serviceinsight.data.ReportWrapper.ReportType;
import com.logicalis.serviceinsight.data.Service;
import com.logicalis.serviceinsight.representation.RevenueReportResultRecord;
import com.logicalis.serviceinsight.service.ApplicationDataDaoService;
import com.logicalis.serviceinsight.service.ContractDaoService;

public class RevenueReportDocumentBuilder extends BaseDocumentBuilder {

	private MessageSource messageSource;
    private Locale locale;
    private XSSFSheet sheet1;
    private BigDecimal total;
    private BigDecimal totalForecastedRevenue;
    private BigDecimal totalCost;
    private BigDecimal totalDirectCustomerCost;
    private BigDecimal totalServiceCost;
    private BigDecimal totalLaborToolsCost;
    private BigDecimal totalProjectLaborCost;
    private BigDecimal totalIndirectLaborCost;
    private BigDecimal totalDirectLaborCost;
    private BigDecimal serviceProfit;
    static final String TITLE_SHEET1 = "SDM Export";
    static final int HEADER_FACTOR = 3 * 256;
    private ApplicationDataDaoService applicationDataDaoService;
    
    public RevenueReportDocumentBuilder(MessageSource messageSource, Locale locale, ApplicationDataDaoService applicationDataDaoService) {
        this.messageSource = messageSource;
        this.locale = locale;
        workbook = new XSSFWorkbook();
        sheet1 = workbook.createSheet(TITLE_SHEET1);
        sheet1.setDefaultColumnWidth(30);
        this.total = new BigDecimal(0);
        this.totalForecastedRevenue = new BigDecimal(0);
        this.totalCost = new BigDecimal(0);
        this.serviceProfit = new BigDecimal(0);
        this.totalDirectCustomerCost = new BigDecimal(0);
        this.totalServiceCost = new BigDecimal(0);
        this.totalLaborToolsCost = new BigDecimal(0);
        this.totalProjectLaborCost = new BigDecimal(0);
        this.totalIndirectLaborCost = new BigDecimal(0);
        this.totalDirectLaborCost = new BigDecimal(0);
        this.applicationDataDaoService = applicationDataDaoService;
    }

    public XSSFWorkbook buildRevenueReportExportSpreadsheet(ReportWrapper report) {
    	Integer rowidx = writeReportHeader(report);
        writeReportData(rowidx, report);
        return this.workbook;
    }
    
    private int writeReportHeader(ReportWrapper report) {
    	CellStyle headerStyle = headerTableHeaderCellStyle();
        CellStyle style = headerTableBodyCellStyle();
        
        int rowidx = 0;
        int colidx = 0;
        XSSFRow header = sheet1.createRow(rowidx);
        header.setHeightInPoints((short) 30);

        header = sheet1.createRow(rowidx++);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_title_profitability_report", null, locale));
        header.getCell(colidx++).setCellStyle(headerStyle);
        header.createCell(colidx).setCellValue("");
        header.getCell(colidx).setCellStyle(headerStyle);
        
        colidx = 0;
        header = sheet1.createRow(rowidx++);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_practice", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        String practice = report.getBusinessModel();
        if(practice == null) practice = messageSource.getMessage("ui_reports_value_all_practices", null, locale);
        header.createCell(colidx).setCellValue(practice);
        header.getCell(colidx).setCellStyle(style);
        
        colidx = 0;
        header = sheet1.createRow(rowidx++);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_start_date", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        Date startDate = report.getStartDate();
        String startDateDisplay = "";
        if(startDate != null) {
        	startDateDisplay = DateTimeFormat.forPattern("MM/dd/yyyy").print(new DateTime(startDate));
        }
        header.createCell(colidx).setCellValue(startDateDisplay);
        header.getCell(colidx).setCellStyle(style);
        
        colidx = 0;
        header = sheet1.createRow(rowidx++);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_end_date", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        Date endDate = report.getEndDate();
        String endDateDisplay = "";
        if(endDate != null) {
        	endDateDisplay = DateTimeFormat.forPattern("MM/dd/yyyy").print(new DateTime(endDate).dayOfMonth().withMaximumValue());
        }
        header.createCell(colidx).setCellValue(endDateDisplay);
        header.getCell(colidx).setCellStyle(style);
        
        colidx = 0;
        header = sheet1.createRow(rowidx++);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_service", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        
        String service = report.getServiceName();
        header.createCell(colidx).setCellValue(service);
        header.getCell(colidx).setCellStyle(style);
        
        colidx = 0;
        header = sheet1.createRow(rowidx++);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_customer", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        Long customerId = report.getCustomerId();
        String customer = "";
        if(customerId == null) {
        	customer = messageSource.getMessage("ui_reports_all_customers", null, locale);
        } else {
        	customer = report.getCustomerName();
        }
        header.createCell(colidx).setCellValue(customer);
        header.getCell(colidx).setCellStyle(style);
        
        return rowidx;
    }
    
    private void writeReportData(Integer rowidx, ReportWrapper report) {
    	//write out header
    	rowidx = writeReportDataHeader(rowidx, report);
    	
    	ReportType reportType = report.getType();
    	Iterator it = report.getData().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            Long serviceId = (Long) pair.getKey();
            serviceProfit = new BigDecimal(0);
            if(ReportType.revenueProfit.equals(reportType)) {
            	rowidx = writeServiceSubHeader(rowidx, serviceId);
        	}
        
            List<RevenueReportResultRecord> records = (List<RevenueReportResultRecord>) pair.getValue();
            for(RevenueReportResultRecord reportRecord : records) {
	    		rowidx = writeReportDataRow(rowidx, reportRecord, reportType);
	    	}
            
            if(ReportType.revenueProfit.equals(reportType)) {
            	rowidx = writeServiceTotal(rowidx, serviceProfit);
        	}
            it.remove();
        }
    	
    	rowidx = writeReportDataTotal(rowidx, reportType);
    }
    
    private int writeServiceSubHeader(Integer rowidx, Long serviceId) {
    	CellStyle style = headerTableBodyCellStyle();
        
    	int colidx = 0;
        XSSFRow header = sheet1.createRow(rowidx);
        header = sheet1.createRow(rowidx++);
        colidx = 0;
        String serviceName = "";
        if(new Long(0).equals(serviceId)) {
        	serviceName = messageSource.getMessage("ui_reports_all_services", null, locale);
    	} else if(new Long(99999).equals(serviceId)) {
    		serviceName = messageSource.getMessage("ui_reports_all_services_without", null, locale);
        } else {
        	Service service = applicationDataDaoService.findActiveServiceByOspId(serviceId);
        	if(service != null) {
        		serviceName = service.getName();
        	} else {
        		serviceName = serviceId.toString();
        	}
        }
        header.createCell(colidx).setCellValue(serviceName);
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue("");
        header.getCell(colidx++).setCellStyle(style);
    	header.createCell(colidx).setCellValue("");
        header.getCell(colidx).setCellStyle(style);
        
    	return rowidx;
    }
    
    private int writeServiceTotal(Integer rowidx, BigDecimal serviceTotal) {
    	CellStyle style = baselineRowCellStyle();
        CellStyle currency = baselineRowCellCurrencyStyle();
        
    	int colidx = 0;
        XSSFRow header = sheet1.createRow(rowidx);
        header = sheet1.createRow(rowidx++);
        colidx = 0;
        header.createCell(colidx).setCellValue("");
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue("Service Total");
        header.getCell(colidx++).setCellStyle(style);
    	header.createCell(colidx).setCellValue(serviceTotal.doubleValue());
        header.getCell(colidx).setCellStyle(currency);
        
    	return rowidx;
    }
    
    private int writeReportDataHeader(Integer rowidx, ReportWrapper report) {
    	CellStyle style = headerCellStyle();
        
    	int colidx = 0;
        XSSFRow header = sheet1.createRow(rowidx);
        header = sheet1.createRow(rowidx++);
        header.createCell(colidx++).setCellValue("");
        header.createCell(colidx).setCellValue("");
        
        header = sheet1.createRow(rowidx++);
        colidx = 0;
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_month", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_device_count", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        if(ReportType.revenue.equals(report.getType())) {
        	header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_revenue", null, locale));
            header.getCell(colidx++).setCellStyle(style);
            header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_forecasted_revenue", null, locale));
            header.getCell(colidx++).setCellStyle(style);
            header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_direct_customer_cost", null, locale));
            header.getCell(colidx++).setCellStyle(style);
            header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_service_tools_cost", null, locale));
            header.getCell(colidx++).setCellStyle(style);
            header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_labor_tools_cost", null, locale));
            header.getCell(colidx++).setCellStyle(style);
            header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_project_labor_cost", null, locale));
            header.getCell(colidx++).setCellStyle(style);
            header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_indirect_labor_cost", null, locale));
            header.getCell(colidx++).setCellStyle(style);
            header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_direct_labor_cost", null, locale));
            header.getCell(colidx++).setCellStyle(style);
            header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_cost_total_cost", null, locale));
            header.getCell(colidx++).setCellStyle(style);
            header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_margin", null, locale));
            header.getCell(colidx++).setCellStyle(style);
        } else if(ReportType.revenueProfit.equals(report.getType())) {
        	header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_profit", null, locale));
            header.getCell(colidx).setCellStyle(style);
        }
        
        
    	return rowidx;
    }
    
    private int writeReportDataRow(Integer rowidx, RevenueReportResultRecord data, ReportType reportType) {
    	CellStyle style = baselineRowCellStyle();
        CellStyle currency = baselineRowCellCurrencyStyle();
        CellStyle percent = baselineRowCellPercentStyle();
    	
        XSSFRow header = sheet1.createRow(rowidx);
        header.setHeightInPoints((short) 30);

        header = sheet1.createRow(rowidx++);
        int colidx = 0;
        String date = "";
        if(data.getDisplayDate() != null) date = data.getDisplayDate();
        header.createCell(colidx).setCellValue(date);
        header.getCell(colidx++).setCellStyle(style);
        Integer deviceCount = 0;
        if(data.getDeviceCount() != null) deviceCount = data.getDeviceCount();
        header.createCell(colidx).setCellValue(deviceCount);
        header.getCell(colidx++).setCellStyle(style);
        
        if(ReportType.revenue.equals(reportType)) {
	        BigDecimal revenue = new BigDecimal(0);
	        if(data.getRevenue() != null) {
	        	revenue = data.getRevenue();
	        	//revenue = new BigDecimal(revenueString);
	        	total = total.add(revenue);
	        }
	        revenue = revenue.setScale(2, RoundingMode.HALF_UP);
	        header.createCell(colidx).setCellValue(revenue.doubleValue());
	        header.getCell(colidx++).setCellStyle(currency);
	        
	        BigDecimal forecastedRevenue = data.getForecastedRevenue();
	        totalForecastedRevenue = totalForecastedRevenue.add(forecastedRevenue);
	        header.createCell(colidx).setCellValue(forecastedRevenue.doubleValue());
	        header.getCell(colidx++).setCellStyle(currency);
	        /*BigDecimal cost = new BigDecimal(0);
	        if(data.getLaborCost() != null) {
	        	cost = data.getLaborCost();
	        }
	        if(data.getIndirectLaborCost() != null) {
	        	cost = cost.add(data.getIndirectLaborCost());
	        }
	        if(data.getLaborToolsCost() != null) {
	        	cost = cost.add(data.getLaborToolsCost());
	        }
	        if(data.getServiceCost() != null) {
	        	cost = cost.add(data.getServiceCost());
	        }
	        if(data.getOnboardingLaborCost() != null) {
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
	        totalCost = totalCost.add(cost);
	        cost = cost.setScale(2, RoundingMode.HALF_UP);
	        header.createCell(colidx).setCellValue(cost.doubleValue());
	        header.getCell(colidx++).setCellStyle(currency);
	        
	        BigDecimal margin = data.getMargin();
	        header.createCell(colidx).setCellValue(margin.doubleValue());
	        header.getCell(colidx).setCellStyle(percent);
        } else if(ReportType.revenueProfit.equals(reportType)) {
        	BigDecimal profit = new BigDecimal(0);
	        if(data.getProfitability() != null) {
	        	profit = data.getProfitability();
	        	total = total.add(profit);
	        	serviceProfit = serviceProfit.add(profit);
	        }
	        profit = profit.setScale(2, RoundingMode.HALF_UP);
	        header.createCell(colidx).setCellValue(profit.doubleValue());
	        header.getCell(colidx++).setCellStyle(currency);
        }
    	
    	return rowidx;
    }

    private int writeReportDataTotal(Integer rowidx, ReportType reportType) {
    	CellStyle style = totalCellStyle();
        CellStyle currency = totalCellCurrencyStyle();
        
    	int colidx = 0;
        XSSFRow header = sheet1.createRow(rowidx);
        header = sheet1.createRow(rowidx++);
        header.createCell(colidx).setCellValue("");
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue("Total");
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(total.doubleValue());
        header.getCell(colidx++).setCellStyle(currency);
        header.createCell(colidx).setCellValue(totalForecastedRevenue.doubleValue());
        header.getCell(colidx++).setCellStyle(currency);
        if(ReportType.revenue.equals(reportType)) {
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
