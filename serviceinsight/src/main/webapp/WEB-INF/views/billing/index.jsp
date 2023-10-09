<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib uri="http://java.sun.com/jstl/fmt_rt" prefix="fmt" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<spring:url value="/resources/images/ajax-loader.gif" var="ajax_loader" />
<spring:url value="/resources/js/billing.js" var="billing_js" />
<script src="${billing_js}" type="text/javascript"></script>

<spring:message code="ui_page_general_ajax_error" var="msg_general_ajax_error" />
<spring:message code="msg_validation_checkbox_required" var="msg_validation_checkbox_required" />
<input type="hidden" id="msg-general-ajax-error" value="${msg_general_ajax_error}" />
<input type="hidden" id="msg-validation-checkbox-required" value="${msg_validation_checkbox_required}" />

<c:set var="is_billing" value="hasAnyRole('ROLE_BILLING', 'ROLE_ADMIN')" />
<sec:authorize access="${is_billing}">
<input type="hidden" id="is-billing" value="true" />
</sec:authorize>

<section class="section-wrapper">
  <div class="section-header">
    <div class="section-title"><spring:message code="ui_title_billing_report" /></div>
  </div>
  <div class="search-criteria">
    <div class="row form-inputs">
      <div class="column-3">
        <div class="field">
          <label><spring:message code="ui_col_customer_name" /></label>
          <span><input type="text" id="customer" /></span>
        </div>
      </div>
      <div class="column-3">
        <div class="field">
          <label><spring:message code="ui_col_date_range" /></label>
          <span>
            <select id="billing-search-date"></select>
          </span>
        </div>
      </div>
      <div class="column-3">
        <div class="field">
          <label><spring:message code="ui_col_sdm" /></label>
          <span>
            <select id="engagement-manager">
                <option value=""></option>
                <c:forEach items="${sdms}" var="sdm">
                <option value="${sdm.id}">${sdm.name}</option>
                </c:forEach>
            </select>
          </span>
        </div>
      </div>
    </div>
    <div class="row form-inputs">
      <div class="column-3">
        <div class="field">
          <label>Show Detail</label>
          <span>
            <select id="show-detail">
              <option value="true">Yes</option>
              <option value="false">No</option>
            </select>
          </span>
        </div>
      </div>
      <div class="column-3">
        <div class="field">
          <label><spring:message code="ui_col_report_format" /></label>
          <span>
            <select id="report-format">
              <option value="json"><spring:message code="ui_val_on_screen" /></option>
              <option value="xlsx"><spring:message code="ui_val_excel_export" /></option>
            </select>
          </span>
        </div>
      </div>
      <div class="column-3">
        <div class="field">
	      <label>Invoice Status</label>
	      <span>
	      	<select id="invoice-status">
	      	  <option value="">All</option>
		      <c:forEach items="${invoiceStatuses}" var="status">
		        <option value="${status}">${status.description}</option>
		      </c:forEach>
		      <option value="notInvoiced">Not Invoiced</option>
	      	</select>
	      </span>
      	</div>
      </div>
    </div>
    <div class="btn-row">
      <a href="#" class="btn" id="get-report"><spring:message code="ui_title_get_report" /></a>
    </div>
  </div>
</section>

<div id="billing-error-msg">
  <div class="onscreen-error content-msg"></div>
</div>

<section class="table-wrapper" id="billing-results" style="display:none;">
  <div class="table-header">
    <div class="table-title">
      Results for <span id="date-range"></span>
    </div>
    <div class="table-links">
      <ul class="menu-btn">
        <li>
          <a href="javascript:;" class="cta-btn">Options<i class="fa fa-angle-down"></i></a>
          <ul id="results-options">
            <li><a href="javascript:;" data-status="readyToInvoice"><i class="fa fa-check"></i><spring:message code="ui_title_mark_as_ready" /></a></li>
            <sec:authorize access="${is_billing}">
            <li><a href="javascript:;" data-status="invoiced"><i class="fa fa-lock"></i><spring:message code="ui_title_mark_as_invoiced" /></a></li>
            </sec:authorize>
            <li><a href="javascript:;" data-status="active"><spring:message code="ui_title_clear_status" /></a></li>
          </ul>
        </li>
        <li class="billing-inline-save" id="billing-loader"><img src="${ajax_loader}" /><spring:message code="ui_saving" /></li>
        <li class="billing-inline-save success-text" id="billing-success-msg"><i class="fa fa-check-circle"></i><spring:message code="ui_save_success" /></li>
      </ul>
    </div>
  </div>
  
  <table id="results-table">
    <thead>
      <tr>
        <th><input type="checkbox" id="select-all" /></th>
        <th><spring:message code="ui_col_customer" /></th>
        <th><spring:message code="ui_col_engagement_mgr" /></th>
        <th><spring:message code="ui_col_contract_job_number" /></th>
        <th><spring:message code="ui_col_service_name" /></th>
        <th><spring:message code="ui_col_part_description" /></th>
        <th><spring:message code="ui_col_pcr" /></th>
        <th class="right"><spring:message code="ui_col_qty" /></th>
        <th class="right"><spring:message code="ui_col_start_date" /></th>
        <th class="right"><spring:message code="ui_col_end_date" /></th>
        <th class="right"><spring:message code="ui_col_nrc" /></th>
        <th class="right"><spring:message code="ui_col_mrc" /></th>
        <th class="right"><spring:message code="ui_col_month_total" /></th>
      </tr>
    </thead>
    <tbody></tbody>
  </table>
</section>