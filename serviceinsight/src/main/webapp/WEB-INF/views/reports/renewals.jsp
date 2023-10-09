<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib uri="http://java.sun.com/jstl/fmt_rt" prefix="fmt" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<spring:url value="/resources/js/reports-renewals.js" var="reports_js" />
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
            <label class="top"><spring:message code="ui_col_customer" /></label>
            <span class="value">
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
      </div>
      <div class="row">
        <div class="column-2">
          <div class="field">
            <label class="top">Renewal Status</label>
            <span class="value">
              <select id="report-status">
                <option value="">All Statuses</option>
                <c:forEach items="${statuses}" var="status">
                    <option value="${status}" data-parent="">${status.description}</option>
                </c:forEach>
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


<section class="table-wrapper section-spacer report-section hidden" style="font-size:0.85em;">
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
        <th><spring:message code="ui_col_contract_id" /></th>
        <th><spring:message code="ui_col_contract_name" /></th>
        <th><spring:message code="ui_col_job_number" /></th>
        <th><spring:message code="ui_col_ae" /></th>
        <th><spring:message code="ui_col_epe" /></th>
        <th><spring:message code="ui_col_sdm" /></th>
        <th><spring:message code="ui_col_bsc" /></th>
        <th><spring:message code="ui_col_service_start_date" /></th>
        <th><spring:message code="ui_col_contract_end_date" /></th>
        <th><spring:message code="ui_col_renewal_status" /></th>
        <th><spring:message code="ui_col_renewal_change" /></th>
        <th class="right"><spring:message code="ui_col_nrc" /> <span id="nrc-header"></span></th>
        <th class="right"><spring:message code="ui_col_mrc" /> <span id="mrc-header"></span></th>
      </tr>
    </thead>
    <tbody></tbody>
    <tfoot></tfoot>
  </table>
</section>