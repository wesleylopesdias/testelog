<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib uri="http://java.sun.com/jstl/fmt_rt" prefix="fmt" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<spring:url value="/resources/js/reports-labor-hours.js" var="reports_js" />
<script src="${reports_js}" type="text/javascript"></script>

<input type="hidden" id="msg-error-start-before-end" value="<spring:message code="ui_error_dates_start_before_end" />" />
<input type="hidden" id="msg-error-dates-required" value="<spring:message code="ui_reports_error_dates_required" />" />

<jsp:include page="sub-nav.jsp" />

<c:set var="is_manager" value="hasRole('ROLE_MANAGER')" />
<sec:authorize access="${is_manager}">
<input type="hidden" id="is-manager" value="true" />
</sec:authorize>

<section class="section-wrapper">
    <div class="section-header">
        <div class="section-title"><spring:message code="ui_title_filter_criteria" /></div>
    </div>
    <section class="sow-details filter-criteria">
      <div class="content-msg"></div>
      <div class="row">
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
        </div>
        <div class="column-2">
          <div class="field">
            <label class="top"><spring:message code="ui_col_service" /></label>
            <span class="value">
              <select id="report-service" class="most-select">
                <option value="">All Services</option>
                <c:forEach items="${services}" var="service">
                <option value="${service.ospId}" class="${service.businessModel}">${service.name}</option>
                </c:forEach>
              </select>
              <span id="multi-select-hint" class="small-hint pull-right" style="display:none;">Hold CTRL to Select Multiple</span>
            </span>
          </div>
        </div>
      </div>
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
      </div>
      <div class="row hidden" id="report-custom-dates">
        <div class="column-2">
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


<section class="table-wrapper section-spacer report-section hidden">
  <div id="results-msg" class="notice-msg" style="font-size:0.9em; display:none;"><spring:message code="ui_reports_labor_hours_results_size" /></div>
  <div class="table-header">
    <div class="table-title">
      <spring:message code="ui_title_table_view" />
    </div>
    <div class="table-links">
      <a href="#" target="_blank" id="download-excel"><i class="fa fa-download"></i><spring:message code="ui_title_export_excel" /></a>
    </div>
  </div>
  
  <table id="report-data-table">
    <thead>
      <tr>
        <th><spring:message code="ui_col_customer" /></th>
        <th><spring:message code="ui_col_worker" /></th>
        <th><spring:message code="ui_col_service_name" /></th>
        <th><spring:message code="ui_col_ticket" /></th>
        <th><spring:message code="ui_col_task_description" /></th>
        <th><spring:message code="ui_col_work_date" /></th>
        <sec:authorize access="${is_manager}">
          <th><spring:message code="ui_col_tier_name" /></th>
          <th><spring:message code="ui_col_tier_code" /></th>
        </sec:authorize>
        <th class="right"><spring:message code="ui_col_hours_worked" /></th>
      </tr>
    </thead>
    <tbody></tbody>
    <tfoot></tfoot>
  </table>
</section>