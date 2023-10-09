<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<spring:url value="/resources/js/reports-labor-by-service.js" var="reports_js" />
<spring:url value="/resources/js/highcharts.js" var="highcharts_js" />
<script src="${highcharts_js}" type="text/javascript"></script>
<script src="${reports_js}" type="text/javascript"></script>

<input type="hidden" id="msg-error-start-before-end" value="<spring:message code="ui_error_dates_start_before_end" />" />
<input type="hidden" id="msg-error-dates-required" value="<spring:message code="ui_reports_error_dates_required" />" />

<jsp:include page="sub-nav.jsp" />

<section class="section-wrapper">
    <div class="section-header">
        <div class="section-title"><spring:message code="ui_title_filter_criteria" /></div>
    </div>
    <section class="sow-details filter-criteria">
      <div class="content-msg"></div>
      <div class="row">
        <div class="column-3">
          <div class="field">
            <label><spring:message code="ui_col_date_range" /></label>
            <span>
              <select id="report-date-range">
                <option value=""><spring:message code="ui_reports_value_all_time" /></option>
              </select>
            </span>
          </div>
        </div>
      </div>
      <div class="row hidden" id="report-custom-dates">
        <div class="column-3">
          <div class="field">
            <label>&nbsp;</label>
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

<section class="section-wrapper section-spacer report-section hidden">
    <div class="section-header">
        <div class="section-title"><spring:message code="ui_title_graph_view" /></div>
    </div>
    <section>
       <div id="chart" style="height:500px;"></div>
       <div id="sub-chart-link" class="center" style="padding-top:40px; display:none;">
        <a href="javascript:;">Show Uncategorized Breakdown</a>
       </div>
       <div id="sub-chart-container" style="display:none;">
         <div id="sub-chart-left" style="height:400px; margin-top:50px; text-align:center;"></div><div id="sub-chart-right" style="height:400px; margin-top:50px; text-align:center;"></div>
       </div>
    </section>
</section>

<section class="table-wrapper section-spacer report-section hidden">
  <div class="table-header">
    <div class="table-title">
      <spring:message code="ui_title_table_view" />
    </div>
    <div class="table-links">
      <%--<a href="#" target="_blank" id="download-excel"><i class="fa fa-download"></i><spring:message code="ui_title_export_excel" /></a>  --%>
    </div>
  </div>
  
  <section class="tabs">
    <a href="javascript:;" class="selected table-tab" data-view="report-data-table">Labor Cost by Service</a>
    <span id="sub-table-container" style="display:none;">
    <a href="javascript:;" class="table-tab" data-view="sub-table-left">Uncategorized Labor By Customer</a>
    <a href="javascript:;" class="table-tab" data-view="sub-table-right">Uncategorized Labor By Task</a>
    </span>
  </section>
  
  <table id="report-data-table" class="data-table">
    <thead>
      <tr>
        <th>Service Name</th>
        <th>&nbsp;</th>
        <th class="right">Percentage</th>
        <th class="right"><spring:message code="ui_col_labor_cost" /></th>
      </tr>
    </thead>
    <tbody></tbody>
    <tfoot></tfoot>
  </table>
  
  <table id="sub-table-left" style="display:none;" class="data-table">
    <thead>
      <tr>
        <th>Service Name</th>
        <th>&nbsp;</th>
        <th class="right">Percentage</th>
        <th class="right"><spring:message code="ui_col_labor_cost" /></th>
      </tr>
    </thead>
    <tbody></tbody>
    <tfoot></tfoot>
  </table>
  
  <table id="sub-table-right" style="display:none;" class="data-table">
    <thead>
      <tr>
        <th>Service Name</th>
        <th>&nbsp;</th>
        <th class="right">Percentage</th>
        <th class="right"><spring:message code="ui_col_labor_cost" /></th>
      </tr>
    </thead>
    <tbody></tbody>
    <tfoot></tfoot>
  </table>
  </div>
</section>