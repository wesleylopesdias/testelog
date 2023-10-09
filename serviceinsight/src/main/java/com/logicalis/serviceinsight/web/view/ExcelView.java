package com.logicalis.serviceinsight.web.view;

import com.logicalis.serviceinsight.data.Contract;
import com.logicalis.serviceinsight.data.ContractServiceChangedConsolidatedWrapper;
import com.logicalis.serviceinsight.data.ExpenseCategoryReportWrapper;
import com.logicalis.serviceinsight.data.ReportWrapper;
import com.logicalis.serviceinsight.representation.LaborBreakdownRecord;
import com.logicalis.serviceinsight.representation.LaborHoursRecord;
import com.logicalis.serviceinsight.representation.PricingPipeline;
import com.logicalis.serviceinsight.representation.RevenueByCustomerRecord;
import com.logicalis.serviceinsight.representation.RevenueByServiceRecord;
import com.logicalis.serviceinsight.representation.SDMCustomerExportWrapper;
import com.logicalis.serviceinsight.representation.ServiceDetailRecordWrapper;
import com.logicalis.serviceinsight.service.ApplicationDataDaoService;
import com.logicalis.serviceinsight.service.ContractDaoService;

import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.AbstractView;

/**
 * Provides the Contract Service SDM Spreadsheet View
 *
 * @author poneil
 */
@Component
public class ExcelView extends AbstractView {

    public static final String EXPORT_SDM_KEY = "sdmExport";
    public static final String EXPORT_SDM_CUSTOMER_KEY = "sdmCustomerExport";
    public static final String EXPORT_CHANGED_CONSOLIDATED_KEY = "changedConsolidatedExport";
    public static final String EXPORT_CHANGED_CONSOLIDATED_NO_DETAIL_KEY = "changedConsolidatedNoDetailExport";
    public static final String EXPORT_REVENUE_REPORT_KEY = "revenueReportExport";
    public static final String EXPORT_SERVICE_DETAILS_REPORT_KEY = "serviceDetailsReportExport";
    public static final String EXPORT_REVENUE_REPORT_BY_SERVICE_KEY = "revenueReportByServiceExport";
    public static final String EXPORT_REVENUE_REPORT_BY_CUSTOMER_KEY = "revenueReportByCustomerExport";
    public static final String EXPORT_PRICING_PIPELINE_KEY = "pricingPipeline";
    public static final String LABOR_BY_COST_REPORT_KEY = "laborByCostExport";
    public static final String EXPORT_LABOR_BREAKDOWN_KEY = "laborBreakdown";
    public static final String EXPORT_LABOR_HOURS_KEY = "laborHours";
    public static final String EXPORT_RENEWALS_KEY = "renewals";
    public static final String EXPORT_EXPENSE_CATEGORY_REPORT_KEY = "expenseCategoryReportExport";
    public static final String CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    protected final Logger log = LoggerFactory.getLogger(getClass());
    @Autowired
    MessageSource messageSource;
    @Autowired
    ContractDaoService contractDaoService;
    @Autowired
    ApplicationDataDaoService applicationDataDaoService;

    public ExcelView() {
        setContentType(CONTENT_TYPE);
    }

    @Override
    protected boolean generatesDownloadContent() {
        return true;
    }

    @Override
    protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        //find out which report key we're dealing with
        Object obj = model.get(EXPORT_SDM_KEY);
        String currentKey = EXPORT_SDM_KEY;
        if (obj == null) {
            obj = model.get(EXPORT_REVENUE_REPORT_KEY);
            currentKey = EXPORT_REVENUE_REPORT_KEY;
        }
        if (obj == null) {
            obj = model.get(EXPORT_SERVICE_DETAILS_REPORT_KEY);
            currentKey = EXPORT_SERVICE_DETAILS_REPORT_KEY;
        }
        if (obj == null) {
            obj = model.get(EXPORT_REVENUE_REPORT_BY_SERVICE_KEY);
            currentKey = EXPORT_REVENUE_REPORT_BY_SERVICE_KEY;
        }
        if (obj == null) {
            obj = model.get(EXPORT_REVENUE_REPORT_BY_CUSTOMER_KEY);
            currentKey = EXPORT_REVENUE_REPORT_BY_CUSTOMER_KEY;
        }
        if (obj == null) {
            obj = model.get(EXPORT_CHANGED_CONSOLIDATED_KEY);
            currentKey = EXPORT_CHANGED_CONSOLIDATED_KEY;
        }
        if (obj == null) {
            obj = model.get(EXPORT_CHANGED_CONSOLIDATED_NO_DETAIL_KEY);
            currentKey = EXPORT_CHANGED_CONSOLIDATED_NO_DETAIL_KEY;
        }
        if (obj == null) {
            obj = model.get(EXPORT_PRICING_PIPELINE_KEY);
            currentKey = EXPORT_PRICING_PIPELINE_KEY;
        }
        if (obj == null) {
            obj = model.get(LABOR_BY_COST_REPORT_KEY);
            currentKey = LABOR_BY_COST_REPORT_KEY;
        }
        if (obj == null) {
            obj = model.get(EXPORT_LABOR_BREAKDOWN_KEY);
            currentKey = EXPORT_LABOR_BREAKDOWN_KEY;
        }
        if (obj == null) {
            obj = model.get(EXPORT_LABOR_HOURS_KEY);
            currentKey = EXPORT_LABOR_HOURS_KEY;
        }
        if (obj == null) {
            obj = model.get(EXPORT_EXPENSE_CATEGORY_REPORT_KEY);
            currentKey = EXPORT_EXPENSE_CATEGORY_REPORT_KEY;
        }
        if (obj == null) {
            obj = model.get(EXPORT_SDM_CUSTOMER_KEY);
            currentKey = EXPORT_SDM_CUSTOMER_KEY;
        }
        if (obj == null) {
            obj = model.get(EXPORT_RENEWALS_KEY);
            currentKey = EXPORT_RENEWALS_KEY;
        }
        
        

        if (obj != null && EXPORT_SDM_KEY.equals(currentKey)) {
            SDMExportDocumentBuilder exporter = new SDMExportDocumentBuilder(
                    messageSource, LocaleContextHolder.getLocale(), contractDaoService, applicationDataDaoService);
            XSSFWorkbook workbook = exporter.buildSDMExportSpreadsheet((ContractServiceChangedConsolidatedWrapper) obj);
            response.setContentType(getContentType());
            ServletOutputStream out = response.getOutputStream();
            workbook.write(out);
            out.flush();
        } else if (obj != null && EXPORT_REVENUE_REPORT_KEY.equals(currentKey)) {
            RevenueReportDocumentBuilder exporter = new RevenueReportDocumentBuilder(messageSource, LocaleContextHolder.getLocale(), applicationDataDaoService);
            XSSFWorkbook workbook = exporter.buildRevenueReportExportSpreadsheet((ReportWrapper) obj);
            response.setContentType(getContentType());
            ServletOutputStream out = response.getOutputStream();
            workbook.write(out);
            out.flush();
        } else if (obj != null && EXPORT_SERVICE_DETAILS_REPORT_KEY.equals(currentKey)) {
            ServiceDetailsReportDocumentBuilder exporter = new ServiceDetailsReportDocumentBuilder(messageSource, LocaleContextHolder.getLocale());
            XSSFWorkbook workbook = exporter.buildSpreadsheet((ServiceDetailRecordWrapper) obj);
            response.setContentType(getContentType());
            ServletOutputStream out = response.getOutputStream();
            workbook.write(out);
            out.flush();
        } else if (obj != null && EXPORT_REVENUE_REPORT_BY_SERVICE_KEY.equals(currentKey)) {
            RevenueByServiceReportDocumentBuilder exporter = new RevenueByServiceReportDocumentBuilder(messageSource, LocaleContextHolder.getLocale());
            XSSFWorkbook workbook = exporter.buildRevenueByServiceReportExportSpreadsheet((List<RevenueByServiceRecord>) obj);
            response.setContentType(getContentType());
            ServletOutputStream out = response.getOutputStream();
            workbook.write(out);
            out.flush();
        } else if (obj != null && EXPORT_REVENUE_REPORT_BY_CUSTOMER_KEY.equals(currentKey)) {
            RevenueByCustomerReportDocumentBuilder exporter = new RevenueByCustomerReportDocumentBuilder(messageSource, LocaleContextHolder.getLocale());
            XSSFWorkbook workbook = exporter.buildRevenueByCustomerReportExportSpreadsheet((List<RevenueByCustomerRecord>) obj);
            response.setContentType(getContentType());
            ServletOutputStream out = response.getOutputStream();
            workbook.write(out);
            out.flush();
        } else if (obj != null && EXPORT_PRICING_PIPELINE_KEY.equals(currentKey)) {
            PricingPipelineReportDocumentBuilder exporter = new PricingPipelineReportDocumentBuilder(messageSource, LocaleContextHolder.getLocale());
            XSSFWorkbook workbook = exporter.buildPricingPipelineReportExportSpreadsheet((PricingPipeline) obj);
            response.setContentType(getContentType());
            ServletOutputStream out = response.getOutputStream();
            workbook.write(out);
            out.flush();
        } else if (obj != null && EXPORT_CHANGED_CONSOLIDATED_KEY.equals(currentKey)) {
            ChangedConsolidatedDocumentBuilder exporter = new ChangedConsolidatedDocumentBuilder(messageSource, LocaleContextHolder.getLocale());
            List<ContractServiceChangedConsolidatedWrapper> objs = (List<ContractServiceChangedConsolidatedWrapper>) obj;
            XSSFWorkbook workbook = exporter.buildExportSpreadsheet(objs);
            response.setContentType(getContentType());
            ServletOutputStream out = response.getOutputStream();
            workbook.write(out);
            out.flush();
        } else if (obj != null && EXPORT_CHANGED_CONSOLIDATED_NO_DETAIL_KEY.equals(currentKey)) {
        	ConsolidatedByContractDocumentBuilder exporter = new ConsolidatedByContractDocumentBuilder(messageSource, LocaleContextHolder.getLocale());
            List<ContractServiceChangedConsolidatedWrapper> objs = (List<ContractServiceChangedConsolidatedWrapper>) obj;
            XSSFWorkbook workbook = exporter.buildExportSpreadsheet(objs);
            response.setContentType(getContentType());
            ServletOutputStream out = response.getOutputStream();
            workbook.write(out);
            out.flush();
        } else if (obj != null && LABOR_BY_COST_REPORT_KEY.equals(currentKey)) {
            RevenueReportDocumentBuilder exporter = new RevenueReportDocumentBuilder(messageSource, LocaleContextHolder.getLocale(), applicationDataDaoService);
            XSSFWorkbook workbook = exporter.buildRevenueReportExportSpreadsheet((ReportWrapper) obj);
            response.setContentType(getContentType());
            ServletOutputStream out = response.getOutputStream();
            workbook.write(out);
            out.flush();
        } else if (obj != null && EXPORT_LABOR_BREAKDOWN_KEY.equals(currentKey)) {
        	LaborBreakdownReportDocumentBuilder exporter = new LaborBreakdownReportDocumentBuilder(messageSource, LocaleContextHolder.getLocale());
            XSSFWorkbook workbook = exporter.buildLaborBreakdownExportSpreadsheet((List<LaborBreakdownRecord>) obj);
            response.setContentType(getContentType());
            ServletOutputStream out = response.getOutputStream();
            workbook.write(out);
            out.flush();
        } else if (obj != null && EXPORT_LABOR_HOURS_KEY.equals(currentKey)) {
        	LaborHoursReportDocumentBuilder exporter = new LaborHoursReportDocumentBuilder(messageSource, LocaleContextHolder.getLocale());
            XSSFWorkbook workbook = exporter.buildLaborHoursExportSpreadsheet((List<LaborHoursRecord>) obj);
            response.setContentType(getContentType());
            ServletOutputStream out = response.getOutputStream();
            workbook.write(out);
            out.flush();
        } else if (obj != null && EXPORT_EXPENSE_CATEGORY_REPORT_KEY.equals(currentKey)) {
        	ExpenseCategoryReportDocumentBuilder exporter = new ExpenseCategoryReportDocumentBuilder(messageSource, LocaleContextHolder.getLocale());
            XSSFWorkbook workbook = exporter.buildExpenseCategoryExportSpreadsheet((ExpenseCategoryReportWrapper) obj);
            response.setContentType(getContentType());
            ServletOutputStream out = response.getOutputStream();
            workbook.write(out);
            out.flush();
        } else if (obj != null && EXPORT_SDM_CUSTOMER_KEY.equals(currentKey)) {
        	SDMCustomerExportDocumentBuilder exporter = new SDMCustomerExportDocumentBuilder(messageSource, LocaleContextHolder.getLocale(), contractDaoService, applicationDataDaoService);
            XSSFWorkbook workbook = exporter.buildSDMCustomerExportSpreadsheet((SDMCustomerExportWrapper) obj);
            response.setContentType(getContentType());
            ServletOutputStream out = response.getOutputStream();
            workbook.write(out);
            out.flush();
        } else if (obj != null && EXPORT_RENEWALS_KEY.equals(currentKey)) {
        	RenewalsReportDocumentBuilder exporter = new RenewalsReportDocumentBuilder(messageSource, LocaleContextHolder.getLocale());
            XSSFWorkbook workbook = exporter.buildRenewalsExportSpreadsheet((List<Contract>) obj);
            response.setContentType(getContentType());
            ServletOutputStream out = response.getOutputStream();
            workbook.write(out);
            out.flush();
        }
    }
}
