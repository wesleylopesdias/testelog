<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<spring:url value="/resources/js/reports-top-customers.js" var="reports_js" />
<spring:url value="/resources/js/highcharts.js" var="highcharts_js" />
<script src="${highcharts_js}" type="text/javascript"></script>
<script src="${reports_js}" type="text/javascript"></script>

<jsp:include page="sub-nav.jsp" />

<section class="section-wrapper">
    <div class="section-header">
        <div class="section-title"><spring:message code="ui_title_filter_criteria" /></div>
    </div>
    <section class="sow-details filter-criteria">
      <div class="content-msg"></div>
      <div class="row">
        <div class="column-2">
          <div class="field">
            <label><spring:message code="ui_col_date_range" /></label>
            <span>
              <select id="report-date-range">
                <option value=""><spring:message code="ui_reports_value_all_time" /></option>
              </select>
            </span>
          </div>
        </div>
        <div class="column-2">
          <div class="field">
            <label><spring:message code="ui_col_status" /></label>
            <span>
              <select id="report-customer-status">
                <option value=""><spring:message code="ui_reports_all_customers" /></option>
                <option value="false" selected="selected"><spring:message code="ui_reports_all_customers_active" /></option>
              </select>
            </span>
          </div>
        </div>
      </div>
      <div class="row">
        <div class="column-2">
          <div class="field">
            <label><spring:message code="ui_col_revenue_type" /></label>
            <span>
              <select id="report-revenue-status">
                <option value="true"><spring:message code="ui_reports_value_show_invoiced_revenue" /></option>
                <option value=""><spring:message code="ui_reports_value_show_all_revenue" /></option>
              </select>
            </span>
          </div>
        </div>
        <div class="column-2">
          <div class="field">
            <label><spring:message code="ui_col_customer_children" /></label>
            <span>Include
                <input type="radio" name="chldrn" value="true" checked="checked"/>
            </span>
            <span>Exclude
                <input type="radio" name="chldrn" value="false"/>
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
       <div id="chart"></div>
       <div class="report-hint"><spring:message code="ui_reports_labor_cost_note" /></div>
    </section>
</section>
 
<section class="table-wrapper section-spacer report-section hidden">
  <div class="table-header">
    <div class="table-title">
      <spring:message code="ui_title_table_view" />
    </div>
    <div class="table-links">
      <a href="#" target="_blank" id="download-excel"><i class="fa fa-download"></i><spring:message code="ui_title_export_excel" /></a>
    </div>
  </div>
  
  <table id="report-data-table" style="font-size:0.85em;">
    <thead>
      <tr>
        <th class=""><spring:message code="ui_col_customer" /></th>
        <th class=""><spring:message code="ui_col_sd_sdm" /></th>
        <th class="right"><spring:message code="ui_col_device_count" /></th>
        <th class="right column"><spring:message code="ui_col_direct_customer_cost" /></th>
        <th class="right column"><spring:message code="ui_col_service_tools_cost" /></th>
        <th class="right column"><spring:message code="ui_col_labor_tools_cost" /></th>
        <th class="right column"><spring:message code="ui_col_project_labor_cost" /></th>
        <th class="right column"><spring:message code="ui_col_indirect_labor_cost" /></th>
        <th class="right column"><spring:message code="ui_col_direct_labor_cost" /></th>
        <th class="right"><spring:message code="ui_col_revenue" /></th>
        <th class="right"><spring:message code="ui_col_cost_total_cost" /></th>
        <th class="right"><spring:message code="ui_col_margin" /></th>
      </tr>
    </thead>
    <tbody></tbody>
    <tfoot></tfoot>
  </table>
</section>