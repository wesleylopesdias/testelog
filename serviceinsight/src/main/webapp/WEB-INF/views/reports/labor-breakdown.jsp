<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<spring:url value="/resources/js/reports-labor-breakdown.js" var="reports_js" />
<script src="${reports_js}" type="text/javascript"></script>

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
      <div class="row">
        <div class="button-row"><a href="javascript:;" class="cta-btn" id="run-report-btn"><spring:message code="ui_btn_run_report" /></a></div>
      </div>
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
  
  <table id="report-data-table">
    <thead>
      <tr>
        <th><spring:message code="ui_col_tier_code" /></th>
        <th class="right"><spring:message code="ui_col_base_rate" /></th>
        <th class="right"><spring:message code="ui_col_addl_rate" /></th>
        <th class="right"><spring:message code="ui_col_base_rate_monthly_total" /></th>
        <th class="right"><spring:message code="ui_col_addl_rate_monthly_total" /></th>
        <th class="right"><spring:message code="ui_col_overall_monthly_total" /></th>
      </tr>
    </thead>
    <tbody></tbody>
    <tfoot></tfoot>
  </table>
</section>