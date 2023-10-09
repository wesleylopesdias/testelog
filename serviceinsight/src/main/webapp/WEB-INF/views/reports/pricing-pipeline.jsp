<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib uri="http://java.sun.com/jstl/fmt_rt" prefix="fmt" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<spring:url value="/resources/js/reports-pricing-pipeline.js" var="reports_js" />
<script src="${reports_js}" type="text/javascript"></script>

<spring:message code="ui_reports_pricing_pipeline_services" var="lbl_services"/>
<spring:message code="ui_reports_pricing_pipeline_customer_quotes" var="lbl_customer_quotes" />

<input type="hidden" id="msg-error-start-before-end" value="<spring:message code="ui_error_dates_start_before_end" />" />
<input type="hidden" id="msg-error-dates-required" value="<spring:message code="ui_reports_error_dates_required" />" />
<input type="hidden" id="msg-no-services-data" value="<spring:message code="ui_reports_no_data" arguments="${lbl_services }"/>"/>
<input type="hidden" id="msg-no-customer-quotes-data" value="<spring:message code="ui_reports_no_data" arguments="${lbl_customer_quotes }"/>"/>

<jsp:include page="sub-nav.jsp" />

<section class="section-wrapper">
    <div class="section-header">
        <div class="section-title"><spring:message code="ui_title_filter_criteria" /></div>
    </div>
    <section class="sow-details filter-criteria">
      <div class="content-msg"></div>
      <div class="row" id="report-custom-dates">
        <div class="column-3">
          <div class="field">
            <label><spring:message code="ui_col_date_range" /></label>
            <span>
              <span class="field-symbol"></span><span class="required-ind required"></span><input type="text" size="10" class="month-datepicker" maxlength="10" placeholder="Start Date" id="report-start-date" /> - <span class="field-symbol"></span><span class="required-ind required"></span><input type="text" size="10" class="month-datepicker" maxlength="10" placeholder="End Date" id="report-end-date" /> 
            </span>
          </div>
        </div>
      </div>
      <div class="row">
        <div class="button-row"><a href="javascript:;" class="cta-btn" id="run-report-btn"><spring:message code="ui_btn_run_report" /></a></div>
      </div>
    </section>
</section>

<section class="table-wrapper section-spacer report-section hidden">
  <div class="table-header">
    <div class="table-title">
      <spring:message code="ui_reports_pricing_pipeline_services" />
    </div>
    <div class="table-links">
      <a href="#" target="_blank" id="download-excel-services"><i class="fa fa-download"></i><spring:message code="ui_title_export_excel" /></a>
    </div>
  </div>
  
  <table id="report-data-table-services">
    <thead>
      <tr>
        <th class=""><spring:message code="ui_col_service" /></th>
        <th class=""><spring:message code="ui_col_offering"></spring:message></th>
        <th class="right"><spring:message code="ui_col_total_quantity" /></th>
        <th class=""><spring:message code="ui_col_unit_label" /></th>
      </tr>
    </thead>
    <tbody></tbody>
    <tfoot></tfoot>
  </table>
</section>

<section class="table-wrapper section-spacer report-section hidden">
  <div class="table-header">
    <div class="table-title">
      <spring:message code="ui_reports_pricing_pipeline_customer_quotes" />
    </div>
    <div class="table-links">
      <a href="#" target="_blank" id="download-excel-customers"><i class="fa fa-download"></i><spring:message code="ui_title_export_excel" /></a>
    </div>
  </div>
  
  <table id="report-data-table-customers">
    <thead>
      <tr>
        <th class=""><spring:message code="ui_col_quote_number" /></th>
        <th class=""><spring:message code="ui_col_customer" /></th>
        <th class=""><spring:message code="ui_col_close_date" /></th>
        <th class=""><spring:message code="ui_col_services" /></th>
        <th class="right"><spring:message code="ui_col_total_quantity" /></th>
      </tr>
    </thead>
    <tbody></tbody>
    <tfoot></tfoot>
  </table>
</section>
