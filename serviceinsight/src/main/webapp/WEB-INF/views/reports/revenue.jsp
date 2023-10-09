<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib uri="http://java.sun.com/jstl/fmt_rt" prefix="fmt" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<spring:url value="/resources/js/reports-revenue.js" var="reports_js" />
<spring:url value="/resources/js/highcharts.js" var="highcharts_js" />
<spring:url value="/reports/expensecategories" var="expense_category_link"/>

<script src="${highcharts_js}" type="text/javascript"></script>
<script src="${reports_js}" type="text/javascript"></script>

<input type="hidden" id="msg-error-start-before-end" value="<spring:message code="ui_error_dates_start_before_end" />" />
<input type="hidden" id="msg-error-dates-required" value="<spring:message code="ui_reports_error_dates_required" />" />
<input type="hidden" id="expense-category-link" value="${expense_category_link}" />

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
            <label>Chart Type</label>
            <span>
              <select id="report-chart-type">
                <option value="column">Revenue vs. Cost (Column)</option>
                <option value="line">Profit (Line)</option>
              </select>
            </span>
          </div>
        </div>
        <div class="column-2">
          <div class="field">
            <label><spring:message code="ui_col_revenue_type" /></label>
            <span>
              <select id="report-revenue-status">
                <option value="invoiced"><spring:message code="ui_reports_value_show_invoiced_revenue" /></option>
                <option value="all"><spring:message code="ui_reports_value_show_all_revenue" /></option>
                <option value="forecast">Show Forecasted Revenue</option>
              </select>
            </span>
          </div>
        </div>
      </div>
      <div class="row">
        <div class="column-3">
          <div class="field">
            <label><spring:message code="ui_col_practice" /></label>
            <span>
              <select id="report-group">
                <option value=""><spring:message code="ui_reports_value_all_practices" /></option>
                <option value="Cloud"><spring:message code="ui_reports_value_cloud" /></option>
                <option value="Managed"><spring:message code="ui_reports_value_ms" /></option>
                <option value="CSP"><spring:message code="ui_reports_value_csp" /></option>
                <option value="Other"><spring:message code="ui_reports_value_other" /></option>
              </select>
            </span>
          </div>
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
            <label><spring:message code="ui_col_customer" /></label>
            <span>
              <select id="report-customer">
                <option value=""><spring:message code="ui_reports_all_customers" /></option>
                <c:forEach items="${customers}" var="customer">
                    <c:if test="${customer.siEnabled eq true}">
                        <option value="${customer.id}" data-parent="">${customer.name}</option>
                        <c:if test="${fn:length(customer.children) gt 0}">
                            <option value="${customer.id}" data-parent="true">${customer.name} (including child companies)</option>
                        </c:if>
                    </c:if>
                </c:forEach>
              </select>
            </span>
          </div>
          <div class="field">
            <label class="top"><spring:message code="ui_col_service" /></label>
            <span class="value">
              <select id="report-service" class="most-select">
                <option value="0"><spring:message code="ui_reports_all_services_with" /></option>
                <option value="99999"><spring:message code="ui_reports_all_services_without" /></option>
                <c:forEach items="${services}" var="service">
                    <c:choose>
                        <c:when test="${null ne service.businessModel}">
                            <c:set var="service_practice" value="${service.businessModel}"/>
                        </c:when>
                        <c:otherwise>
                            <c:set var="service_practice" value="Other"/>
                        </c:otherwise>
                    </c:choose>
                    <option value="${service.ospId}" class="${service_practice}">${service.name}</option>
                </c:forEach>
              </select>
              <span id="multi-select-hint" class="small-hint pull-right" style="display:none;">Hold CTRL to Select Multiple</span>
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
        <th class=""><spring:message code="ui_col_month" /></th>
        <th class="right"><spring:message code="ui_col_device_count" /></th>
        <th class="right column"><spring:message code="ui_col_revenue" /></th>
        <th class="right column"><spring:message code="ui_col_forecasted_revenue" /></th>
        <th class="right column"><spring:message code="ui_col_direct_customer_cost" /></th>
        <th class="right column"><spring:message code="ui_col_service_tools_cost" /></th>
        <th class="right column"><spring:message code="ui_col_labor_tools_cost" /></th>
        <th class="right column"><spring:message code="ui_col_project_labor_cost" /></th>
        <th class="right column"><spring:message code="ui_col_indirect_labor_cost" /></th>
        <th class="right column"><spring:message code="ui_col_direct_labor_cost" /></th>
        <th class="right column"><spring:message code="ui_col_cost_total_cost" /></th>
        <th class="right column"><spring:message code="ui_col_margin" /></th>
        <th class="right line" style="display:none;"><spring:message code="ui_col_profit" /></th>
      </tr>
    </thead>
    <tbody></tbody>
    <tfoot></tfoot>
  </table>
</section>

<section class="table-wrapper section-spacer report-section hidden">
    <section class="tabs">
        <a href="javascript:;" class="table-tab" data-view="service-tools" id="service-tools-tab">Service Tools Cost Details</a>
        <a href="javascript:;" class="table-tab" data-view="direct-customer" id="direct-customer-tab">Direct Customer Cost Details</a>
        <a href="javascript:;" class="table-tab" data-view="forecast" id="forecast-tab">Forecasted Contracts</a>
    </section>
    
    <div class="report-tab-section" id="cost-logging-tables" style="display:none;"></div>
    <div class="report-tab-section" id="customer-cost-logging-tables" style="display:none;"></div>
    <div class="report-tab-section" id="forecasted-contracts-tables" style="display:none;"></div>
</section>