<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<spring:url value="/resources/js/reports-azure-invoice.js" var="reports_js" />
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
      <spring:message code="ui_title_onetime_based_lineitems" />
    </div>
    <div class="table-links">
      <%--<a href="#" target="_blank" id="download-excel"><i class="fa fa-download"></i><spring:message code="ui_title_export_excel" /></a> --%>
    </div>
  </div>
  
  <table id="report-onetime-data-table" class="small-table">
    <thead>
      <tr>
        <th width="5%"><spring:message code="ui_col_customer" /></th>
        <th width="27%"><spring:message code="ui_col_azure_offer_name" /></th>
        <th width="5%"><spring:message code="ui_col_quantity" /></th>
        <th width="11%" class="right"><spring:message code="ui_col_azure_onetime_customer_total" /></th>
        <th width="5%">&nbsp;</th>
        <th><spring:message code="ui_col_si_product_name" /></th>
        <th><spring:message code="ui_col_si_part_number" /></th>
        <th><spring:message code="ui_col_si_quantity" /></th>
        <th class="right"><spring:message code="ui_col_invoiced_amount" /></th>
      </tr>
    </thead>
    <tbody></tbody>
    <tfoot></tfoot>
  </table>
</section>

<section class="table-wrapper section-spacer report-section hidden">
  <div class="table-header">
    <div class="table-title">
      <spring:message code="ui_title_license_based_lineitems" />
    </div>
    <div class="table-links">
      <%--<a href="#" target="_blank" id="download-excel"><i class="fa fa-download"></i><spring:message code="ui_title_export_excel" /></a> --%>
    </div>
  </div>
  
  <table id="report-license-data-table" class="small-table">
    <thead>
      <tr>
        <th width="5%"><spring:message code="ui_col_customer" /></th>
        <th width="27%"><spring:message code="ui_col_azure_offer_name" /></th>
        <th width="5%"><spring:message code="ui_col_quantity" /></th>
        <th width="11%" class="right"><spring:message code="ui_col_azure_monthly_cost" /></th>
        <th width="5%">&nbsp;</th>
        <th><spring:message code="ui_col_si_product_name" /></th>
        <th><spring:message code="ui_col_si_part_number" /></th>
        <th><spring:message code="ui_col_si_quantity" /></th>
        <th class="right"><spring:message code="ui_col_invoiced_amount" /></th>
      </tr>
    </thead>
    <tbody></tbody>
    <tfoot></tfoot>
  </table>
</section>

<section class="table-wrapper section-spacer report-section hidden">
  <div class="table-header">
    <div class="table-title">
      <spring:message code="ui_title_usage_based_lineitems" />
    </div>
    <div class="table-links">
      <%--<a href="#" target="_blank" id="download-excel"><i class="fa fa-download"></i><spring:message code="ui_title_export_excel" /></a> --%>
    </div>
  </div>
  
  <table id="report-usage-data-table" class="small-table">
    <thead>
      <tr>
        <th width="1%">&nbsp;</th>
        <th width="20%"><spring:message code="ui_col_azure_customer" /></th>
        <th width="15%"><spring:message code="ui_col_subscription" /></th>
        <th width="10%" class="right"><spring:message code="ui_col_azure_monthly_cost" /></th>
        <th width="5%">&nbsp;</th>
        <th width="34%"><spring:message code="ui_col_si_customer" /></th>
        <th width="10%" class="right"><spring:message code="ui_col_invoiced_amount" /></th>
      </tr>
    </thead>
    <tbody></tbody>
    <tfoot></tfoot>
  </table>
</section>