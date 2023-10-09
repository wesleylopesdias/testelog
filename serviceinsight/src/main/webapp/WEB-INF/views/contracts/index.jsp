<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<spring:url value="/resources/images/ajax-loader.gif" var="ajax_loader" />
<spring:url value="/resources/js/contracts.js" var="contracts_js" />
<spring:url value="/resources/js/quotes.js" var="quotes_js" />
<spring:url value="/reports/servicedetails" var="service_details_report" />
<script src="${contracts_js}" type="text/javascript"></script>
<script src="${quotes_js}" type="text/javascript"></script>

<input type="hidden" id="contract-update-type" value="add" />
<input type="hidden" id="update-contract-msg" value="<spring:message code="ui_ok_new_contract" />" />
<input type="hidden" id="general-error-msg" value="<spring:message code="validation_error_generic" />" />
<input type="hidden" id="import-quote-success-msg" value="<spring:message code="ui_ok_import_quote_success" />" />
<input type="hidden" id="import-quote-error-customer-quote-required-msg" value="<spring:message code="ui_dialog_import_quote_customer_quote_required" />" />

<div class="app-plug">
	Looking for contracts containing a specific service? <a href="${service_details_report}">Check the Service Details Report<i class="fa fa-arrow-circle-right icon-right"></i></a>
</div>

<div class="breadcrumb">
<fieldset>
  <label><spring:message code="ui_col_customer" /></label>
  <span class="field">
    <select id="customer">
      <option value="#">Select a Customer</option>
      <c:forEach items="${customers}" var="customer">
        <option value="${customer.id}">${customer.name}</option>
      </c:forEach>
    </select>
  </span>
</fieldset>
</div>

<section class="table-wrapper" id="contracts" style="display:none;">
  <div class="table-header">
    <div class="table-title">
      <spring:message code="ui_title_contracts" />
    </div>
    <div class="table-links">
      <a href="javascript:;" class="popup-link" data-dialog="add-contract-dialog"><i class="fa fa-plus-square"></i><spring:message code="ui_title_add_contract" /></a>
    </div>
  </div>
  
  <section class="tabs">
    <a href="javascript:;" class="selected table-tab" data-view="active"><spring:message code="ui_title_active" /></a>
    <a href="javascript:;" class="table-tab" data-view="archived"><i class="fa fa-archive"></i><spring:message code="ui_title_archived" /></a>
    <!--  <a href="javascript:;" class="table-tab" data-view="quotes"><i class="fa fa-tags"></i>Quotes</a> -->
    <a href="javascript:;" class="import-btn small-cta-btn pull-right popup-link" data-dialog="import-quote-dialog"><i class="fa fa-download"></i>Import SOW from Pricing</a>
  </section>
  
  <table>
    <thead>
      <tr>
        <th><spring:message code="ui_col_contract_id" /></th>
        <th><spring:message code="ui_col_contract_name" /></th>
        <th><spring:message code="ui_col_job_number" /></th>
        <th><spring:message code="ui_col_ae" /></th>
        <th><spring:message code="ui_col_epe" /></th>
        <th><spring:message code="ui_col_sdm" /></th>
        <th><spring:message code="ui_col_bsc" /></th>
        <th class="right"><spring:message code="ui_col_signed_date" /></th>
        <th class="right"><spring:message code="ui_col_billing_start_date" /></th>
        <th class="right"><spring:message code="ui_col_service_start_date" /></th>
        <th class="right"><spring:message code="ui_col_contract_end_date" /></th>
        <th class="right"><spring:message code="ui_col_nrc" /> <span id="nrc-header"></span></th>
        <th class="right"><spring:message code="ui_col_mrc" /> <span id="mrc-header"></span></th>
      </tr>
    </thead>
    <tbody>
    </tbody>
    <tfoot>
      <tr class="total-row">
        <td class="right" colspan="11" id="contracts-total-month-label"></td>
        <td class="right" id="contracts-total-month-nrc"></td>
        <td class="right" id="contracts-total-month-mrc"></td>
      </tr>
    </tfoot>
  </table>
</section>

<jsp:include page="/WEB-INF/views/contracts/includes/add-edit-sow.jsp"/>

<div id="import-quote-dialog" class="dialog">
    <div class="message-content"></div>
      
    <div class="dialog-loader-overlay">
        <div class="dialog-loader">
            <img src="${ajax_loader}" />
            <div><spring:message code="ui_dialog_import_quote_importing" /></div>
        </div>
    </div>
    <div class="dialog-instruct"><spring:message code="ui_dialog_import_quote_instructions" /></div>
      
    <div class="dialog-content">
      <div class="step">
        <div class="field">
          <label><spring:message code="ui_col_pricing_customers" /></label>
          <span><span class="field-symbol"></span><span class="required-ind required"></span><select id="quote-customers" class="customer-dropdown-list">
              <option value=""></option>
            </select> <span class="no-results" id="customer-loader"><img src="${ajax_loader}" /><spring:message code="ui_msg_loading" /></span>
          </span>
        </div>
        
        <div class="form-spacer"></div>
        <section class="table-wrapper" id="quotes-table-container" style="display:none;">
        
          <table id="quotes-table">
            <thead>
              <tr>
                <th>&nbsp;</th>
                <th><spring:message code="ui_col_quote_number" /></th>
                <th><spring:message code="ui_col_quote_name" /></th>
                <th class="center"><spring:message code="ui_col_term" /></th>
                <th class="right"><spring:message code="ui_col_nrc" /></th>
                <th class="right"><spring:message code="ui_col_mrc" /></th>
                <th class="right"><spring:message code="ui_col_total_value" /></th>
              </tr>
            </thead>
            <tbody></tbody>
          </table>
        
        </section>
      </div>
      
      <div class="step">
        <div class="field">
          <label><spring:message code="ui_col_contract_name" /></label>
          <span><span class="field-symbol"></span><span class="required-ind required"></span><input type="text" id="import-contract-name" size="40" /></span>
        </div>
        <div class="field">
          <label><spring:message code="ui_col_contract_id" /></label>
          <span><span class="field-symbol"></span><span class="required-ind required"></span><input type="text" id="import-contract-alt-id" size="15" /></span>
        </div>
        <div class="field">
          <label><spring:message code="ui_col_job_number" /></label>
          <span><span class="field-symbol"></span><span class="required-ind"></span><input type="text" id="import-contract-job-number" size="15" /></span>
        </div>
        <div class="field">
          <label><spring:message code="ui_col_sdm" /></label>
          <div class="value"><span class="field-symbol"></span><span class="required-ind"></span><input type="text" size="40" id="import-contract-sdms" />
            <div class="value-second-row"></div>
          </div>
        </div>
        <div class="field">
          <label><spring:message code="ui_col_bsc" /></label>
          <div class="value"><span class="field-symbol"></span><span class="required-ind"></span><input type="text" size="40" id="import-contract-bscs" />
            <div class="value-second-row"></div>
          </div>
        </div>
        <div class="field">
          <label><spring:message code="ui_col_ae" /></label>
          <div class="value"><span class="field-symbol"></span><span class="required-ind"></span><input type="text" size="40" id="import-contract-ae" />
          </div>
        </div>
        <div class="field">
          <label><spring:message code="ui_col_epe" /></label>
          <div class="value"><span class="field-symbol"></span><span class="required-ind"></span><input type="text" size="40" id="import-contract-epe" />
          </div>
        </div>
        <!-- 
        <div class="field">
          <label><spring:message code="ui_col_engagement_manager" /></label>
          <span><span class="field-symbol"></span><span class="required-ind"></span><input type="text" id="import-contract-engagement-manager" size="40" maxlength="255" /></span>
        </div>
        <div class="field">
          <label><spring:message code="ui_col_account_exec" /></label>
          <span><span class="field-symbol"></span><span class="required-ind"></span><input type="text" id="import-contract-account-exec" size="40" maxlength="255" /></span>
        </div> -->
        <div class="form-divider"></div>
        <div class="field">
          <label><spring:message code="ui_col_signed_date" /></label>
          <span><span class="field-symbol"></span><span class="required-ind required"></span><input type="text" class="datepicker" id="import-contract-signed-date" size="11" maxlength="10" placeholder="mm/dd/yyyy" /></span>
        </div>
        <div class="field">
          <label><spring:message code="ui_col_billing_start_date" /></label>
          <span><span class="field-symbol"></span><span class="required-ind required"></span><input type="text" class="datepicker" id="import-contract-start-date" size="11" maxlength="10" placeholder="mm/dd/yyyy" /> <span class="small-hint">(<spring:message code="ui_title_contract_hint_billing_start_date" />)</span></span>
        </div>
        <div class="field">
          <label><spring:message code="ui_col_service_start_date" /></label>
          <span><span class="field-symbol"></span><span class="required-ind required"></span><input type="text" class="datepicker" id="import-contract-service-start-date" size="11" maxlength="10" placeholder="mm/dd/yyyy" /></span>
        </div>
        <div class="field">
          <label><spring:message code="ui_col_contract_end_date" /></label>
          <span><span class="field-symbol"></span><span class="required-ind required"></span><input type="text" class="datepicker" id="import-contract-end-date" size="11" maxlength="10" placeholder="mm/dd/yyyy" /> <span class="small-hint">(<spring:message code="ui_title_contract_hint_contract_end_date" />)</span></span>
        </div>
      </div>
    </div>
</div>
      