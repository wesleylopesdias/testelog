<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt_rt" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<spring:url value="/resources/images/ajax-loader.gif" var="ajax_loader" />
<spring:url value="/resources/js/contracts.js" var="contracts_js" />
<spring:url value="/resources/js/contract.js?v=1" var="contract_js" />
<spring:url value="/resources/js/pricing-sheet.js" var="pricing_sheet_js" />
<script src="${contracts_js}" type="text/javascript"></script>
<script src="${contract_js}" type="text/javascript"></script>
<script src="${pricing_sheet_js}" type="text/javascript"></script>

<fmt:formatDate value="${contract.startDate}" pattern="MM/dd/yyyy" var="contractStartDate"/>
<fmt:formatDate value="${contract.signedDate}" pattern="MM/dd/yyyy" var="contractSignedDate"/>
<fmt:formatDate value="${contract.serviceStartDate}" pattern="MM/dd/yyyy" var="contractServiceStartDate"/>
<fmt:formatDate value="${contract.endDate}" pattern="MM/dd/yyyy" var="contractEndDate"/>

<c:set var="is_billing" value="hasAnyRole('ROLE_BILLING', 'ROLE_ADMIN')" />
<sec:authorize access="${is_billing}">
<input type="hidden" id="is-billing" value="true" />
</sec:authorize>

<c:set var="snSysId" value="${contract.serviceNowSysId}" />
<input type="hidden" id="contract-id" value="${contract.id}" />
<input type="hidden" id="contract-update-type" value="edit" />
<input type="hidden" id="customer" value="${contract.customerId}" />
<input type="hidden" id="contract-id" value="${contract.id}" />
<input type="hidden" id="contract-service-now-sys-id" value="${snSysId}" />
<input type="hidden" id="osm-url" value="${serviceNowUrl}" />
<input type="hidden" id="update-contract-msg" value="<spring:message code="ui_ok_update_contract" />" />
<input type="hidden" id="new-contract-service-msg" value="<spring:message code="ui_ok_new_contractservice" />" />
<input type="hidden" id="update-contract-service-msg" value="<spring:message code="ui_ok_update_contractservice" />" />
<input type="hidden" id="new-pcr-msg" value="<spring:message code="ui_ok_new_contractupdate" />" />
<input type="hidden" id="update-pcr-msg" value="<spring:message code="ui_ok_update_contractupdate" />" />
<input type="hidden" id="general-error-msg" value="<spring:message code="ui_dialog_general_validation_error" />" />
<input type="hidden" id="no-contract-services-msg" value="<spring:message code="ui_title_no_results_contract_services" />" />
<input type="hidden" id="device-identifer-required-msg" value="<spring:message code="ui_error_part_identifier_required" />" />
<input type="hidden" id="no-pcrs-msg" value="<spring:message code="ui_title_no_results_pcrs" />" />
<input type="hidden" id="delete-msg" value="<spring:message code="ui_title_delete" />" />
<input type="hidden" id="delete-all-msg" value="<spring:message code="ui_title_delete_all" />" />
<input type="hidden" id="contract-service-contract-dates-msg" value="<spring:message code="ui_validation_error_contractservice_contract_dates" />" />
<input type="hidden" id="contract-service-device-msg" value="<spring:message code="ui_validation_error_contractservice_device" />" />
<input type="hidden" id="contract-service-start-date-msg" value="<spring:message code="ui_dialog_validation_invalid_date" />" />
<input type="hidden" id="contract-service-end-before-start-msg" value="<spring:message code="ui_validation_error_contractservice_end_before_start_dates" />" />
<input type="hidden" id="contract-adjustment-contract-dates-msg" value="<spring:message code="ui_validation_error_contract_adjustment_contract_dates" />" />
<input type="hidden" id="undo-msg" value="<spring:message code="ui_title_undo" />" />
<input type="hidden" id="undo-delete-all-msg" value="<spring:message code="ui_title_undo_delete_all" />" />
<input type="hidden" id="file-select-file-error-msg" value="<spring:message code="ui_error_select_file" />" />
<input type="hidden" id="file-type-invalid-error-msg" value="<spring:message code="ui_error_invalid_file_type" />" />
<input type="hidden" id="contract-file-type-invalid-error-msg" value="<spring:message code="ui_error_contract_invalid_file_type" />" />
<input type="hidden" id="add-contract-adjustment-success-msg" value="<spring:message code="ui_ok_new_contract_adjustment" />" />
<input type="hidden" id="update-contract-adjustment-success-msg" value="<spring:message code="ui_ok_update_contract_adjustment" />" />
<input type="hidden" id="add-contract-group-success-msg" value="<spring:message code="ui_ok_new_contract_group" />" />
<input type="hidden" id="update-contract-group-success-msg" value="<spring:message code="ui_ok_update_contract_group" />" />
<input type="hidden" id="delete-contract-group-success-msg" value="<spring:message code="ui_ok_delete_contract_group" />" />
<input type="hidden" id="contract-hidden-start-date" value="${contractStartDate}" />
<input type="hidden" id="contract-hidden-end-date" value="${contractEndDate}" />
<input type="hidden" id="current-viewing-start-date" value="" />
<input type="hidden" id="current-viewing-end-date" value="" />
<input type="hidden" id="search-no-results-msg" value="<spring:message code="ui_ok_no_cis_found" />" />
<input type="hidden" id="no-cis-found-msg" value="<spring:message code="ui_msg_no_cis_found" />" />
<input type="hidden" id="sn-integration-failed-msg" value="<spring:message code="ui_msg_sn_integration_failed" />" />
<input type="hidden" id="sn-loading-cis-msg" value="<spring:message code="ui_msg_loading_cis" />" />
<input type="hidden" id="sn-syncing-cis-msg" value="<spring:message code="ui_msg_syncing_cis" />" />
<input type="hidden" id="add-pricing-sheet-product-success-msg" value="<spring:message code="ui_dialog_ok_pricing_sheet_product_created" />" />
<input type="hidden" id="update-pricing-sheet-product-success-msg" value="<spring:message code="ui_dialog_ok_pricing_sheet_product_updated" />" />
<input type="hidden" id="delete-pricing-sheet-product-success-msg" value="<spring:message code="ui_dialog_ok_pricing_sheet_product_deleted" />" />
<input type="hidden" id="validate-pricing-sheet-product-already-exists-msg" value="<spring:message code="ui_dialog_validation_pricing_sheet_product_exists" />" />
<input type="hidden" id="save-contract-service-azure-success-msg" value="<spring:message code="ui_dialog_ok_contractserviceazure_saved" />" />
<input type="hidden" id="delete-contract-service-azure-success-msg" value="<spring:message code="ui_dialog_ok_contractserviceazure_deleted" />" />
<input type="hidden" id="validation-contract-service-quantity-max-msg" value="<spring:message code="ui_validation_error_contractservice_quantity_max" />" />

<spring:url value="/contracts?cid=" var="all_contracts" />

<spring:message code="ui_title_search_by_ci" var="search_by_ci" />

<%-- 
<div id="device-loader" class="overlay-screen-loader">
	<div class="loading-box">
		<div><img src="${ajax_loader}" /></div>Loading Contract Details...
	</div>
</div>
--%>

<section class="section-wrapper" style="margin-bottom:30px;">
  <div class="row">
    <div class="column-3"><a href="${all_contracts}${customer.id}" class="back-to-link pull-left"><i class="fa fa-arrow-circle-left"></i><spring:message code="ui_title_all_contracts" /> ${customer.name}</a></div>
    <div class="column-3 search">
      <input type="text" size="30" id="ci-search" placeholder="${search_by_ci}" /><i class="fa fa-search"></i>
      <div id="no-search-results" class="no-results"></div>
    </div>
    <div class="column-3">
        <jsp:useBean id="now" class="java.util.Date" />
        <fmt:formatDate var="current_year" value="${now}" pattern="yyyy" />
        <spring:url value="/reports/revenue/?auto=true&cid=${customer.id}&year=${current_year}&invoiced=true" var="profitability_url" />
        <a href="${profitability_url}" class="pull-right"><i class="fa fa-chart-line"></i><spring:message code="ui_title_view_profitability" /></a>
        <div class="clearer"></div>
    </div>
  </div>
  <div class="row" style="padding-top:10px;">
      <a href="javascript:;" class="popup-link btn-link pull-right" data-dialog="upload-dialog"><i class="fa fa-upload"></i><spring:message code="ui_title_import_excel" /></a>
      <div class="clearer"></div>
  </div>
</section>

<div class="notice-msg border-msg center archived" <c:if test="${contract.archived eq false}">style="display:none;"</c:if>><i class="fa fa-archive"></i><spring:message code="ui_title_contract_is_archived" /></div>

<section class="section-wrapper">
    <div class="section-header">
        <div class="section-title"><spring:message code="ui_title_contract_details" /></div>
        <div class="section-links">
            <a href="javascript:;" class="popup-link" data-dialog="add-contract-dialog"><i class="fa fa-edit"></i>Edit Details</a>
        </div>
    </div>
	<section class="sow-details">
	  <div class="row">
	    <div class="column-3">
	      <div class="field">
            <label><spring:message code="ui_col_contract_name" /></label>
            <span id="contract-display-name">${contract.name}</span>
          </div>
          <div class="field">
            <label><spring:message code="ui_col_sow_file" /></label>
            <span id="contract-display-file-path"></span>
          </div>
	      <div class="field">
	        <label><spring:message code="ui_col_contract_id" /></label>
	        <span id="contract-display-alt-id">${contract.altId}</span>
	      </div>
	      <div class="field">
            <label><spring:message code="ui_col_job_number" /></label>
            <span id="contract-display-job-number">${contract.jobNumber}</span> <c:if test="${snSysId ne null}"><span id="contract-sn-link">(<a href="${serviceNowUrl}/nav_to.do?uri=ast_service.do?sys_id=${snSysId}" target="_blank">OSM<i class="fa fa-external-link-square-alt icon-right"></i></a>)</span></c:if>
          </div>
	    </div>
	    <div class="column-3">
          <div class="field">
            <label><spring:message code="ui_col_signed_date" /></label>
            <span id="contract-display-signed-date">${contractSignedDate}</span>
          </div>
          <div class="field">
            <label><spring:message code="ui_col_billing_start_date" /></label>
            <span id="contract-display-start-date">${contractStartDate}</span>
          </div>
	      <div class="field">
            <label><spring:message code="ui_col_service_start_date" /></label>
            <span id="contract-display-service-start-date">${contractServiceStartDate}</span>
          </div>
	      <div class="field">
            <label><spring:message code="ui_col_contract_end_date" /></label>
            <span id="contract-display-end-date">${contractEndDate}</span>
          </div>
	    </div>
	    <div class="column-3">
	      <div class="field">
            <label><spring:message code="ui_col_ae" /></label>
            <span id="contract-display-account-exec">${contract.accountExecutive}</span>
          </div>     
          <div class="field">
            <label><spring:message code="ui_col_sdm" /></label>
            <span id="contract-display-engagement-manager">${contract.engagementManager}</span>
          </div>
          <div class="field">
            <label><spring:message code="ui_col_epe" /></label>
            <span id="contract-display-epe"></span>
          </div>
          <div class="field">
            <label><spring:message code="ui_col_bsc" /></label>
            <span id="contract-display-bsc"></span>
          </div>
	    </div>
	  </div>
	  
	  <div id="renewal-display-msg" style="display:none;" class="notice-msg border-msg"><i class="fa fa-history"></i>This contract is up for renewal soon. Please tell us how likely it is to renew. <a href="javascript:;" class="popup-link" data-dialog="add-contract-dialog">Click Here</a> and fill out the renewal details at the bottom.</div>
	  <section id="renewal-display-section" class="sow-subdetails" style="display:none;">
		  <div class="row">
	        <div class="column-3">
	          <div class="field">
	            <label><spring:message code="ui_col_renewal_status" /></label>
	            <span id="contract-display-renewal-status"></span>
	          </div>
	        </div>
	        <div class="column-3">
              <div class="field">
                <label><spring:message code="ui_col_renewal_change" /></label>
                <span id="contract-display-renewal-change"></span>
              </div>
            </div>
	      </div>
	      <div class="row">
            <div class="column">
              <div class="field">
                <label><spring:message code="ui_col_renewal_notes" /></label>
                <span id="contract-display-renewal-notes"></span>
              </div>
            </div>
          </div>
	  </section>
	</section>
</section>

<section class="contract-viewing">
  <div class="row">
    <div class="column-2">
      <div class="sub-header"><i class="fa fa-calendar"></i><spring:message code="ui_title_filter_by_date" /></div>
	  <div class="row">
	    <div class="column-3 bottom"><span id="current-view-prev-month"></span></div>
	    <div class="column-3 center current-viewing-date">
	        <span>
	          <select id="current-viewing-date">
	            <option value="#"><spring:message code="ui_date_range_all_time" /></option>
	          </select>
	        </span>
	        <div class="current-viewing-date-range">
	          <span id="current-viewing-display-start-date"></span> - <span id="current-viewing-display-end-date"></span>
	        </div>
	    </div>
	    <div class="column-3 text-right bottom"><span id="current-view-next-month"></span></div>
	  </div>
    </div>
    <div class="column-divider">&nbsp;</div>
    <div class="column-2">
      <div class="sub-header"><i class="fa fa-tags"></i><spring:message code="ui_title_filter_by_group" /> <a href="javascript:;" class="pull-right popup-link" data-dialog="manage-groups-dialog"><spring:message code="ui_title_contract_group_manage_groups" /></a></div>
      <div class="row current-viewing-date current-viewing-group">
        <label><spring:message code="ui_title_contract_group" /></label>
        <select id="current-viewing-group">
          <option value="#"><spring:message code="ui_val_all_groups" /></option>
          <c:forEach items="${groups}" var="group">
          <option value="${group.id}">${group.name}</option>
          </c:forEach>
        </select>
      </div>
    </div>
  </div>
</section>

<section class="table-wrapper" id="contract-services">
  <div class="table-header">
    <div class="table-title">
      <spring:message code="ui_title_contract_services" />
    </div>
    <div class="table-links">
      <a href="javascript:;" class="popup-link azure-popup-link" data-dialog="azure-dialog"><i class="fa fa-plus-square"></i>Add Subscription Service</a>
      <a href="javascript:;" class="popup-link" data-dialog="add-contract-adjustment-dialog"><i class="fa fa-plus-square"></i><spring:message code="ui_title_add_contract_adjustment" /></a>
      <a href="javascript:;" class="popup-link" data-dialog="add-contract-service-dialog"><i class="fa fa-plus-square"></i><spring:message code="ui_title_add_contract_service" /></a>
       <span class="inline-btn-msg" id="locked-msg" style="display:none;"><i class="fa fa-info-circle"></i>This month has been invoiced. Navigate to another month to add items to it.</span>
    </div>
  </div>
  
  <section style="background-color:#e8e8e8; border-radius:4px; margin-top:10px; padding:8px;" id="contract-services-azure">
    <div style="border-bottom:1px dotted #ccc; margin-bottom:5px; padding-bottom:3px;"><i class="fa fa-history"></i><spring:message code="ui_title_contractserviceazure" /></div>
    <div id="azure-container" style="margin-bottom:20px;">
        <h5>Azure Subscriptions</h5>
	    <table id="azure-table" style="font-size:0.9em;">
		  <thead>
		    <tr>
		      <th><spring:message code="ui_col_service_name" /></th>
		      <th><spring:message code="ui_col_name" /></th>
		      <th><spring:message code="ui_col_service_code" /></th>
		      <th><spring:message code="ui_col_part_description" /></th>
		      <th><spring:message code="ui_col_azure_customer_id" /></th>
		      <th><spring:message code="ui_col_subscription_id" /></th>
		      <th class="center"><spring:message code="ui_col_start_date" /></th>
		      <th class="center"><spring:message code="ui_col_end_date" /></th>
		      <th class="center"><spring:message code="ui_col_options" /></th>
		    </tr>
		  </thead>
		  <tbody></tbody>
		</table>
	</div>
	
	<div id="m365-container">
	   <h5>M365 Subscriptions</h5>
		<table id="m365-table" style="font-size:0.9em;">
	      <thead>
	        <tr>
	          <th><spring:message code="ui_col_service_name" /></th>
	          <th>Type</th>
	          <th><spring:message code="ui_col_service_code" /></th>
	          <th><spring:message code="ui_col_part_description" /></th>
	          <th><spring:message code="ui_col_azure_customer_id" /></th>
	          <th>Active</th>
	          <th class="center"><spring:message code="ui_col_options" /></th>
	        </tr>
	      </thead>
	      <tbody></tbody>
	    </table>
    </div>
  </section>
  
  <section class="tabs">
    <span class="table-tab-placeholder no-results temporary-device-loader">Loading CI View...</span>
  	<a href="javascript:;" class="contract-services-tab table-tab requires-devices" data-view="ci"><spring:message code="ui_title_tab_ci_view" /></a>
    <a href="javascript:;" class="selected contract-services-tab table-tab" data-view="expanded"><spring:message code="ui_title_tab_expanded" /></a>
    <a href="javascript:;" class="contract-services-tab table-tab" data-view="changed"><spring:message code="ui_title_tab_changed" /></a>
    <a href="javascript:;" class="contract-services-tab table-tab" data-view="pending"><spring:message code="ui_title_tab_pending" /></a>
    <a href="javascript:;" class="contract-services-tab table-tab" data-view="donotbill">Do Not Bill</a>
    <a href="#" class="side-tab-link" id="download-excel"><i class="fa fa-download"></i><spring:message code="ui_title_standard_view_excel" /></a>
    <a href="#" class="side-tab-link" id="download-excel-ci-view"><i class="fa fa-download"></i><spring:message code="ui_title_ci_view_excel" /></a>
    <span class="invoice-status" id="contract-invoice-status">
      <span id="current-status"></span>
      <span id="status-loader" class="no-results" style="display:none;"><img src="${ajax_loader}" />&nbsp;Saving...</span>
    </span>
  </section>
  
  <table class="contract-services-table" id="contract-services-table">
    <thead>
      <tr>
      	<th id="ci-name-column" style="display:none;"><spring:message code="ui_col_ci_name" /></th>
        <th><spring:message code="ui_col_service_name" /></th>
        <th><spring:message code="ui_col_service_code" /></th>
        <th><spring:message code="ui_col_part_description" /></th>
        <th><spring:message code="ui_col_pcr_id" /></th>
        <th class="center"><spring:message code="ui_col_quantity" /></th>
        <th class="right"><spring:message code="ui_col_start_date" /></th>
        <th class="right"><spring:message code="ui_col_end_date" /></th>
        <th class="right"><spring:message code="ui_col_one_time_cost" /></th>
        <th class="right"><spring:message code="ui_col_mrc" /></th>
      </tr>
    </thead>
    <tbody></tbody>
    <tfoot></tfoot>
  </table>
</section>

<section class="table-wrapper" id="pcrs">
  <div class="table-header">
    <div class="table-title">
      <spring:message code="ui_title_pcrs" />
    </div>
    <div class="table-links">
      <a href="javascript:;" class="popup-link update-pcr" data-id="add" data-dialog="pcr-dialog"><i class="fa fa-plus-square"></i><spring:message code="ui_title_add_pcr" /></a>
    </div>
  </div>
  
  <table>
    <thead>
      <tr>
        <th><spring:message code="ui_col_pcr_id" /></th>
        <th><spring:message code="ui_col_pcr_file" /></th>
        <th><spring:message code="ui_col_job_number" /></th>
        <th><spring:message code="ui_col_ticket_number" /></th>
        <th><spring:message code="ui_col_notes" /></th>
        <th class="right"><spring:message code="ui_col_signed_date" /></th>
        <th class="right"><spring:message code="ui_col_effective_date" /></th>
        <th class="center"><spring:message code="ui_col_one_time_cost" /></th>
        <th class="center"><spring:message code="ui_col_mrc" /></th>
        <th class="center"><spring:message code="ui_col_options" /></th>
      </tr>
    </thead>
    <tbody></tbody>
  </table>
</section>


<section class="table-wrapper" id="pricing-sheet">
  <div class="table-header">
    <div class="table-title">
      <spring:message code="ui_label_activation_pricing_sheet" />
    </div>
    <div class="table-links">
      <a href="javascript:;" id="generate-sheet" ><i class="fa fa-plus-square"></i><spring:message code="ui_label_generate_pricing_sheet" /></a>
      <a href="javascript:;" class="popup-link add-m365-product" data-id="add" data-dialog="add-m365-pricing-product-dialog"><i class="fa fa-plus-square"></i>Add an M365 Product</a>
      <a href="javascript:;" class="popup-link add-m365nc-product" data-id="add" data-dialog="add-m365nc-pricing-product-dialog"><i class="fa fa-plus-square"></i>Add an M365 NC Product</a>
      <a href="javascript:;" class="popup-link update-pricing-products" data-id="add" data-dialog="pricing-product-dialog"><i class="fa fa-plus-square"></i>Add an O365 Product</a>
    </div>
  </div>
  
  <section class="tabs">
    <a href="javascript:;" class="selected table-tab pricing-sheet-tab" data-type="standard"><spring:message code="ui_label_pricing_sheet" /><span id="pricing-sheet-standard-count"></span></a>
    <a href="javascript:;" class="table-tab pricing-sheet-tab" data-type="O365">O365 Licenses<span id="pricing-sheet-o365-count"></span></a>
    <a href="javascript:;" class="table-tab pricing-sheet-tab" data-type="M365">M365 Licenses<span id="pricing-sheet-m365-count"></span></a>
    <a href="javascript:;" class="table-tab pricing-sheet-tab" data-type="M365NC">M365 New Commerce Licenses<span id="pricing-sheet-m365nc-count"></span></a>
    <span class="invoice-status" id="pricing-sheet-status">
      <span id="current-pricing-status"></span>
      <span id="pricing-status-loader" class="no-results" style="display:none;"><img src="${ajax_loader}" />&nbsp;Saving...</span>
    </span>
  </section>
  
  <table>
    <thead>
      <tr>
        <th><spring:message code="ui_col_service_name" /></th>
        <th><spring:message code="ui_col_service_code" /></th>
        <th><spring:message code="ui_col_part_description" /></th>
        <th class="right"><spring:message code="ui_col_one_time_cost" /></th>
        <th class="right"><spring:message code="ui_col_mrc" /></th>
        <th class="right"><spring:message code="ui_col_removal_cost" /></th>
        <th class="center"><spring:message code="ui_col_options" /></th>
      </tr>
    </thead>
    <tbody></tbody>
  </table>
</section>


<section class="table-wrapper" id="sn-cis">
  <div class="table-header">
    <div class="table-title">
      <spring:message code="ui_title_sn_cis" />
    </div>
    
    <c:if test="${snSysId ne null}">
    <div class="table-links">
      <a href="javascript:;" id="osm-sync"><i class="fa fa-cloud-download-alt"></i><spring:message code="ui_title_sync_with_osm" /></a>
    </div>
    </c:if>
  </div>
  
  <div id="sn-ci-list"></div>
  <div class="legend">
    <span><i class="fa fa-check-circle"></i><spring:message code="ui_title_legend_ci_found" /></span>
    <span><i class="fa fa-question-circle"></i><spring:message code="ui_title_legend_ci_not_found" /></span>
    <span><i class="fa fa-external-link-square-alt"></i><spring:message code="ui_title_legend_open_in_osm" /></span>
  </div>
</section>

<jsp:include page="/WEB-INF/views/contracts/includes/add-edit-sow.jsp"/>

<div id="add-contract-service-dialog" class="dialog">
    <div class="message-content"></div>
      
    <div class="dialog-loader-overlay">
        <div class="dialog-loader">
            <img src="${ajax_loader}" />
            <div><spring:message code="ui_contract_loading" /></div>
        </div>
    </div>
    <div class="dialog-instruct"><spring:message code="ui_dialog_general_instruct" /></div>
      
    <div class="dialog-content" style="position:relative;">
    	<div class="temporary-device-loader part-number-loader"><img src="${ajax_loader}" /><h3>Part Numbers are still loading. Autocomplete won't work until this is complete.</h3></div>
        <div class="field">
          <label><spring:message code="ui_col_service_code" /></label>
          <span><span class="field-symbol"></span><span class="required-ind required"></span><input type="text" class="autocomplete" id="add-contract-service-part-number" size="30" />
          </span>
        </div>
        <div class="field">
          <label><spring:message code="ui_col_part_description" /></label>
          <span><span class="field-symbol"></span><span class="required-ind required"></span><input type="text" class="autocomplete contract-service-input-long" size="70" id="add-contract-service-part-description" />
          </span>
        </div>
        <div class="field">
          <label><spring:message code="ui_col_service_name" /></label>
          <span><span class="field-symbol"></span><span class="required-ind required"></span><select id="add-contract-service" class="service-dropdown contract-service-input-long">
              <option value=""></option>
              <c:forEach items="${fullServices}" var="fullService">
                <option value="${fullService.serviceId}" class="osp-id-${fullService.ospId}">${fullService.name}</option>
              </c:forEach>
            </select>
          </span>
        </div>
        <div class="notice-msg" style="font-size:0.85em; margin-top:16px;" id="add-related-devices-msg"><i class="fa fa-info-circle"></i>This part number has related line items. Some of those may be required. Scroll to the bottom of this window to provide the pricing and quantities for those and any optional line items you'd like to include.</div>
        
        <div class="form-divider contract-group" style="display:none;"></div>
        <div class="field contract-group" style="display:none;">
          <label><spring:message code="ui_title_contract_group" /></label>
          <span><span class="field-symbol"></span><span class="required-ind"></span><select class="contract-group-list" id="add-contract-service-group">
              <option value="#"></option>
              <c:forEach items="${groups}" var="group">
              <option value="${group.id}" data-name="${group.name}" data-description="${group.description}">${group.name}</option>
              </c:forEach>
            </select>
          </span>
        </div>
        
        <div class="form-divider"></div>
        
        <div class="field">
          <label><spring:message code="ui_col_pcr" /></label>
          <span><span class="field-symbol"></span><span class="required-ind"></span><select id="add-contract-services-pcr" class="pcr-list"></select>
            <span id="add-contract-services-no-pcrs" class="no-results" style="display:none;"><spring:message code="ui_title_no_results_pcrs" /></span>
          </span>
        </div>
        <div class="field">
          <label><spring:message code="ui_col_cost_location" /></label>
          <span><span class="field-symbol"></span><span class="required-ind"></span><select class="location-list" id="add-contract-service-location">
              <option value=""></option>
              <c:forEach items="${locations}" var="location">
              <option value="${location.id}">${location.name}</option>
              </c:forEach>
            </select>
          </span>
        </div>
        
        <div class="form-divider"></div>
        
        <div class="add-unit-price-view" style="display:none;">
	      <div class="field">
	        <label>One-Time Unit Cost</label>
	        <span><span class="field-symbol">$</span><span class="required-ind required"></span><input type="text" class="currency-negative add-unit-cost-calculator-field" id="add-unit-contract-service-one-time-cost" placeholder="(ex. 150.00)" size="12" /> <span class="small-hint">(per unit)</span><span class="total-cost-display">Total: <span id="total-nrc-cost-display"></span></span></span>
	      </div>
	      <div class="field">
	        <label>Recurring Unit Cost</label>
	        <span><span class="field-symbol">$</span><span class="required-ind required"></span><input type="text" class="currency-negative add-unit-cost-calculator-field" id="add-unit-contract-service-recurring-cost" placeholder="(ex. 265.39)" size="12" /> <span class="small-hint">(per unit)</span><span class="total-cost-display">Total: <span id="total-mrc-cost-display"></span></span></span>
	      </div>
	      <div class="field">
            <label><spring:message code="ui_col_device_unit_count" /></label>
            <span><span class="field-symbol"></span><span class="required-ind"></span><input type="text" class="integer add-unit-count-calculator-field" id="add-contract-service-unit-count" size="5" /> <span class="small-hint">(<spring:message code="ui_title_unit_count_hint" />)</span></span>
          </div>
        </div>
        <div class="add-total-price-view" style="display:none;">
	      <div class="field">
	        <label><spring:message code="ui_col_one_time_cost" /></label>
	        <span><span class="field-symbol">$</span><span class="required-ind required"></span><input type="text" class="add-contract-service-calculate currency-negative" id="add-contract-service-one-time-cost" placeholder="(ex. 150.00)" size="12" /> <span class="small-hint">(<spring:message code="ui_title_per_unit" />)</span><span class="total-cost-display">Total: <span id="add-contract-service-one-time-cost-total">$0.00</span> <span class="small-hint">(billed in <span id="add-contract-one-time-cost-bill-date">04/01/2015</span>)</span></span></span>
	      </div>
	      <div class="field">
	        <label><spring:message code="ui_col_recurring_cost" /></label>
	        <span><span class="field-symbol">$</span><span class="required-ind required"></span><input type="text" class="add-contract-service-calculate currency-negative" id="add-contract-service-recurring-cost" placeholder="(ex. 265.39)" size="12" /> <span class="small-hint">(<spring:message code="ui_title_per_unit" />)</span><span class="total-cost-display">Total: <span id="add-contract-service-recurring-cost-total">$0.00</span> <span class="small-hint">(per month)</span></span></span>
	      </div>
	      <div class="field">
            <label><spring:message code="ui_col_service_quantity" /></label>
            <span><span class="field-symbol"></span><span class="required-ind required"></span><input type="text" class="add-contract-service-calculate" id="add-contract-service-quantity" size="3" /> <span class="small-hint">(<spring:message code="ui_title_service_quantity_hint" />)</span></span>
          </div>
        </div>
        
        <div class="field">
          <label><spring:message code="ui_col_start_date" /></label>
          <span><span class="field-symbol"></span><span class="required-ind required"></span><input type="text" id="add-contract-service-start-date" class="datepicker" placeholder="mm/dd/yyyy" size="10" /></span>
        </div>
        <div class="field">
          <label><spring:message code="ui_col_end_date" /></label>
          <span><span class="field-symbol"></span><span class="required-ind required"></span><input type="text" id="add-contract-service-end-date" class="datepicker" placeholder="mm/dd/yyyy" size="10" /></span>
        </div>
        <div class="field">
          <label><spring:message code="ui_col_status" /></label>
          <span><span class="field-symbol"></span><span class="required-ind"></span>
            <select id="add-contract-service-status">
              <option value="active"><spring:message code="ui_value_active" /></option>
              <option value="pending"><spring:message code="ui_value_pending" /></option>
              <option value="donotbill"><spring:message code="ui_value_do_not_bill" /></option>
            </select>
          </span>
        </div>
        
        <div class="form-divider"></div>
        <div class="field" style="display:none;" id="ci-name-clone">
          <label><spring:message code="ui_col_ci_name" /></label>
          <span><span class="field-symbol"></span><span class="required-ind"></span><input type="text" name="add-contract-service-name" size="15" /></span>
        </div>
        <div id="ci-names-container">
        
        </div>
        
        <%--
        <div class="form-divider"></div>
        <div class="field">
          <label><spring:message code="ui_col_total_one_time_cost" /></label>
          <span><span class="field-symbol"></span><span class="required-ind"></span><span id="add-contract-service-one-time-cost-total">$0.00</span> <span class="small-hint">(billed in <span id="add-contract-one-time-cost-bill-date">04/01/2015</span>)</span></span>
        </div>
        <div class="field">
          <label><spring:message code="ui_col_total_recurring_cost" /></label>
          <span><span class="field-symbol"></span><span class="required-ind"></span><span id="add-contract-service-recurring-cost-total">$0.00</span> <span class="small-hint">(per month)</span></span>
        </div>
         --%>
         
        <div class="form-divider"></div>
        
        <div class="field">
          <label><spring:message code="ui_col_notes" /></label>
          <span><textarea id="add-contract-service-notes" maxlength="500"></textarea></span>
        </div>
        
        <div id="add-related-devices-container">
        	<div class="form-divider"></div>
        	<div>
        	<section class="table-wrapper">
			  <div class="table-header">
			    <div class="table-title">
			      <i class="fas fa-link"></i>Related Line Items
			    </div>
			  </div>
       		  <table id="add-related-devices">
       		    <thead>
       		      <tr>
       		      	<th>&nbsp;</th>
       		        <th>Device</th>
       		        <th>Quantity</th>
       		        <th>Unit Count</th>
       		        <th>&nbsp;</th>
       		        <th>NRC</th>
       		        <th>MRC</th>
       		      </tr>
       		    </thead>
       		    <tbody></tbody>
       		  </table>
        	</section>
        	</div>
        </div>
    </div>
</div>

<div id="edit-contract-service-dialog" class="dialog">
    <div class="message-content"></div>
      
    <div class="dialog-loader-overlay">
        <div class="dialog-loader">
            <img src="${ajax_loader}" />
            <div><spring:message code="ui_contract_loading" /></div>
        </div>
    </div>
    <div class="dialog-instruct"><spring:message code="ui_dialog_general_instruct" /></div>
      
    <div class="dialog-content">
        <div class="field">
          <label><spring:message code="ui_col_service_code" /></label>
          <span><span class="field-symbol"></span><span class="required-ind required"></span><input type="text" class="autocomplete edit-contract-field" id="edit-contract-service-part-number" size="30" />
            <span class="locked-display-value"></span>
          </span>
        </div>
        <div class="field">
          <label><spring:message code="ui_col_part_description" /></label>
          <span><span class="field-symbol"></span><span class="required-ind required"></span><input type="text" class="autocomplete edit-contract-field contract-service-input-long" size="70" id="edit-contract-service-part-description" />
            <span class="locked-display-value"></span>
          </span>
        </div>
        <div class="field">
          <label><spring:message code="ui_col_service_name" /></label>
          <span><span class="required-ind required"></span><select id="edit-contract-service" class="edit-contract-field service-dropdown contract-service-input-long">
              <c:forEach items="${fullServices}" var="fullService">
                <option value="${fullService.serviceId}" class="osp-id-${fullService.ospId}">${fullService.name}</option>
              </c:forEach>
            </select>
            <span class="locked-display-value"></span> 
          </span>
          <%-- <span id="edit-contract-service" autofocus></span> --%>
        </div>
        
        <div class="form-divider"></div>
        
        <div class="field">
          <label><spring:message code="ui_col_pcr" /></label>
          <span>
            <select id="edit-contract-services-pcr" class="pcr-list"></select>
            <span id="edit-contract-services-no-pcrs" class="no-results" style="display:none;"><spring:message code="ui_title_no_results_pcrs" /></span>
            <span class="locked-display-value"></span>
          </span>
        </div>
        
        <div class="form-divider"></div>
        <div class="table-wrapper">
          <div class="table-header">
            <div class="table-title">&nbsp;</div>
		    <div class="table-links">
		      <a href="javascript:;" id="edit-contract-delete-all" data-action="delete"><i class="fa fa-minus-circle"></i><spring:message code="ui_title_delete_all" /></a>
		    </div>
          </div>
        <table class="edit-table" id="edit-contract-service-table">
          <thead>
            <tr>
              <th>&nbsp;</th>
              <th><spring:message code="ui_col_ci_name" /></th>
              <th><spring:message code="ui_col_start_date" /></th>
              <th><spring:message code="ui_col_end_date" /></th>
              <th><spring:message code="ui_col_device_unit_count" /></th>
              <th><spring:message code="ui_col_one_time_cost" /></th>
              <th><spring:message code="ui_col_mrc" /></th>
              <th>&nbsp;</th>
            </tr>
          </thead>
          <tbody></tbody>
        </table>
        </div>
    </div>
</div>

<div id="pcr-dialog" class="dialog">
    <div class="message-content"></div>
      
    <div class="dialog-loader-overlay">
        <div class="dialog-loader">
            <img src="${ajax_loader}" />
            <div><spring:message code="ui_contract_loading" /></div>
        </div>
    </div>
    <div class="dialog-instruct"><spring:message code="ui_dialog_general_instruct" /></div>
      
    <div class="dialog-content">
        <div class="field">
          <label><spring:message code="ui_col_pcr_id" /></label>
          <span><span class="field-symbol"></span><span class="required-ind required"></span><input type="text" id="pcr-alt-id" size="15" maxlength="20" /></span>
        </div>
        <div class="field">
          <label><spring:message code="ui_col_job_number" /></label>
          <span><span class="field-symbol"></span><span class="required-ind"></span><input type="text" id="pcr-job-number" size="15" maxlength="20" /> <span class="small-hint">(Not all PCRs have a Job #)</span></span>
        </div>
        <div class="field">
          <label><spring:message code="ui_col_ticket_number" /></label>
          <span><span class="field-symbol"></span><span class="required-ind"></span><input type="text" id="pcr-ticket-number" size="15" maxlength="20" /></span>
        </div>
        <div class="field">
          <label><spring:message code="ui_col_signed_date" /></label>
          <span><span class="field-symbol"></span><span class="required-ind required"></span><input type="text" class="datepicker" id="pcr-signed-date" size="11" /></span>
        </div>
        <div class="field">
          <label><spring:message code="ui_col_effective_date" /></label>
          <span><span class="field-symbol"></span><span class="required-ind"></span><input type="text" class="datepicker" id="pcr-effective-date" size="11" /></span>
        </div>
        <div class="field">
          <label><spring:message code="ui_col_one_time_cost" /></label>
          <span><span class="field-symbol">$</span><span class="required-ind"></span><input type="text" class="currency-negative" id="pcr-onetime-price" size="10" maxlength="20" /></span>
        </div>
        <div class="field">
          <label><spring:message code="ui_col_mrc" /></label>
          <span><span class="field-symbol">$</span><span class="required-ind"></span><input type="text" class="currency-negative" id="pcr-recurring-price" size="10" maxlength="20" /> <span class="small-hint">(This can be a negative amount)</span></span>
        </div>
        <div class="table-wrapper" id="pcr-related-contract-services" style="display:none;">
          <div class="form-divider"></div>
          <table>
            <thead>
              <tr>
                <th><spring:message code="ui_col_service_name" /></th>
                <th><spring:message code="ui_col_start_date" /></th>
                <th><spring:message code="ui_col_end_date" /></th>
                <%-- <th>&nbsp;</th> --%>
              </tr>
            </thead>
            <tbody></tbody>
          </table>
        </div>
        <div class="form-divider"></div>
        <div class="field">
          <label><spring:message code="ui_col_notes" /></label>
          <span><textarea id="pcr-notes" maxlength="500"></textarea></span>
        </div>
    </div>
</div>

<div id="add-contract-adjustment-dialog" class="dialog">
    <div class="message-content"></div>
      
    <div class="dialog-loader-overlay">
        <div class="dialog-loader">
            <img src="${ajax_loader}" />
            <div><spring:message code="ui_contract_loading" /></div>
        </div>
    </div>
    <div class="dialog-instruct"><spring:message code="ui_dialog_adjustment_instruct" /></div>
      
    <div class="dialog-content">
        <div class="field">
          <label><spring:message code="ui_col_type" /></label>
          <span><span class="field-symbol"></span><span class="required-ind required"></span><select id="add-contract-adjustment-type">
              <option value="onetime"><spring:message code="ui_value_onetime" /></option>
              <option value="recurring"><spring:message code="ui_value_recurring" /></option>
            </select>
          </span>
        </div>
        <div class="field">
          <label><spring:message code="ui_col_credit_debit_amt" /></label>
          <span><span class="field-symbol">$</span><span class="required-ind required"></span><input type="text" class="currency-negative" id="add-contract-adjustment-amount" placeholder="(ex. 150.00)" size="12" />&nbsp;<span class="small-hint">(<spring:message code="ui_title_amt_hint" />)</span></span>
        </div>

        <div class="field">
          <label><spring:message code="ui_col_start_date" /></label>
          <span><span class="field-symbol"></span><span class="required-ind required"></span><input type="text" class="datepicker" id="add-contract-adjustment-start-date" size="11" /></span>
        </div>
        <div class="field" style="display:none;" id="add-contract-adjustment-end-date-container">
          <label><spring:message code="ui_col_end_date" /></label>
          <span><span class="field-symbol"></span><span class="required-ind required"></span><input type="text" class="datepicker" id="add-contract-adjustment-end-date" size="11" /></span>
        </div>
        <div class="form-divider contract-group" style="display:none;"></div>
        <div class="field contract-group" style="display:none;">
          <label><spring:message code="ui_title_contract_group" /></label>
          <span><span class="field-symbol"></span><span class="required-ind"></span><select class="contract-group-list" id="add-contract-adjustment-group">
              <option value="#"></option>
              <c:forEach items="${groups}" var="group">
              <option value="${group.id}" data-name="${group.name}" data-description="${group.description}">${group.name}</option>
              </c:forEach>
            </select>
          </span>
        </div>
        <div class="form-divider"></div>
        <div class="field">
          <label><spring:message code="ui_col_pcr" /></label>
          <span><span class="field-symbol"></span><span class="required-ind"></span>
            <select id="add-contract-adjustment-pcr" class="pcr-list"></select>
            <span id="add-contract-adjustment-no-pcrs" class="no-results" style="display:none;"><spring:message code="ui_title_no_results_pcrs" /></span>
          </span>
        </div>
        <div class="form-divider"></div>
        <div class="field">
          <label><spring:message code="ui_col_status" /></label>
          <span><span class="field-symbol"></span><span class="required-ind"></span>
            <select id="add-contract-adjustment-status">
              <option value="active"><spring:message code="ui_value_active" /></option>
              <option value="donotbill"><spring:message code="ui_value_do_not_bill" /></option>
            </select>
          </span>
        </div>
        <div class="form-divider"></div>
        <div class="field">
          <label><spring:message code="ui_col_notes" /></label>
          <span><textarea id="add-contract-adjustment-notes" maxlength="500"></textarea></span>
        </div>
    </div>
</div>

<div id="edit-contract-adjustment-dialog" class="dialog">
    <div class="message-content"></div>
      
    <div class="dialog-loader-overlay">
        <div class="dialog-loader">
            <img src="${ajax_loader}" />
            <div><spring:message code="ui_contract_loading" /></div>
        </div>
    </div>
    <div class="dialog-instruct"><spring:message code="ui_dialog_adjustment_instruct" /></div>
      
    <div class="dialog-content">
        <div class="table-wrapper">
          <div class="table-header">
            <div class="table-title">&nbsp;</div>
            <div class="table-links">
              <a href="javascript:;" id="edit-contract-delete-all" data-action="delete"><i class="fa fa-minus-circle"></i><spring:message code="ui_title_delete_all" /></a>
            </div>
          </div>
        <table class="edit-table" id="edit-contract-adjustment-table">
          <thead>
            <tr>
              <th>&nbsp;</th>
              <th><spring:message code="ui_col_type" /></th>
              <th><spring:message code="ui_col_credit_debit_amt" /></th>
              <th><spring:message code="ui_col_start_date" /></th>
              <th><spring:message code="ui_col_end_date" /></th>
              <th>&nbsp;</th>
            </tr>
          </thead>
          <tbody></tbody>
        </table>
        </div>
    </div>
</div>

<div id="manage-groups-dialog" class="dialog">
    <div class="message-content"></div>
      
    <div class="dialog-loader-overlay">
        <div class="dialog-loader">
            <img src="${ajax_loader}" />
            <div><spring:message code="ui_contract_loading" /></div>
        </div>
    </div>
    <div class="dialog-instruct"><spring:message code="ui_dialog_general_instruct" /></div>
      
    <div class="dialog-content">
        <div class="field">
          <label><spring:message code="ui_title_choose_action" /></label>
          <span><span class="required-ind required"></span><select id="manage-groups-action">
              <option value="#"></option>
              <option value="add"><spring:message code="ui_val_add_group" /></option>
              <option value="edit"><spring:message code="ui_val_edit_group" /></option>
              <option value="delete"><spring:message code="ui_val_delete_group" /></option>
            </select>
          </span>
        </div>
        <div class="field" id="manage-groups-group-container" style="display:none;">
          <label><spring:message code="ui_title_contract_group" /></label>
          <span><span class="required-ind required"></span><select class="contract-group-list" id="manage-groups-group">
              <option value="#"></option>
              <c:forEach items="${groups}" var="group">
              <option value="${group.id}" data-name="${group.name}" data-description="${group.description}">${group.name}</option>
              </c:forEach>
            </select>
          </span>
        </div>
        
        <div class="form-divider" id="manage-groups-form-divider" style="display:none;"></div>
        
        <div id="manage-groups-modify-container" style="display:none;">
	        <div class="field">
	          <label><spring:message code="ui_col_name" /></label>
	          <span><span class="required-ind required"></span><input type="text" size="40" id="manage-groups-name" /></span>
	        </div>
	        <div class="field">
	          <label><spring:message code="ui_col_description" /></label>
	          <span><span class="required-ind"></span><input type="text" size="50" id="manage-groups-description" /></span>
	        </div>
        </div>
        
        <div class="notice-msg" id="manage-groups-delete-msg" style="display:none;"><i class="fas fa-exclamation-triangle"></i><spring:message code="ui_title_contract_group_delete_warning" /></div>
    </div>
</div>

<div id="azure-dialog" class="dialog">
    <div class="message-content"></div>
      
    <div class="dialog-loader-overlay">
        <div class="dialog-loader">
            <img src="${ajax_loader}" />
            <div><spring:message code="ui_contract_loading" /></div>
        </div>
    </div>
    <div class="dialog-instruct"><spring:message code="ui_dialog_general_instruct" /></div>
      
    <div class="dialog-content">
        <div class="field">
          <label><spring:message code="ui_col_azure_product" /></label>
          <span><span class="field-symbol"></span><span class="required-ind required"></span><select id="azure-device" class="edit-contract-field">
            <option value=""></option>
            </select>
            <span class="locked-display-value"></span>
          </span>
        </div>
        <div class="field">
          <label><spring:message code="ui_col_service_name" /></label>
          <span><span class="field-symbol"></span><span class="required-ind required"></span><select id="azure-service" class="edit-contract-field service-dropdown contract-service-input-long">
              <option value=""></option>
              <c:forEach items="${fullServices}" var="fullService">
                <option value="${fullService.serviceId}" class="osp-id-${fullService.ospId}">${fullService.name}</option>
              </c:forEach>
            </select>
            <span class="locked-display-value"></span>
          </span>
        </div>
        
        <div class="form-divider"></div>
        <div class="notice-msg" style="font-size:0.8em;"><i class="fa fa-info-circle"></i>Note: These products are "auto-billed" meaning Service Insight will automatically insert a contract service once a month using the start/end date provided below. Service Insight will pull the appropriate amount to bill from Azure.</div>
        <div class="field azure-field m365-field">
          <label><spring:message code="ui_col_azure_customer_id" /></label>
          <span><span class="field-symbol"></span><span class="required-ind required"></span><input type="text" id="azure-customer-id" size="50" class="edit-contract-field" /><span class="locked-display-value"></span></span>
        </div>
        <div class="field azure-field aws-field">
          <label class="azure-field"><spring:message code="ui_col_subscription_id" /></label><label class="aws-field" style="display:none;">Account ID</label>
          <span><span class="field-symbol"></span><span class="required-ind required"></span><input type="text" id="azure-subscription-id" size="50" class="edit-contract-field" /><span class="locked-display-value"></span></span>
        </div>
        <div class="field azure-field">
          <label><spring:message code="ui_col_start_date" /></label>
          <span><span class="field-symbol"></span><span class="required-ind required"></span><input type="text" class="datepicker edit-contract-field" id="azure-start-date" size="11" /><span class="locked-display-value"></span></span>
        </div>
        <div class="field azure-field">
          <label><spring:message code="ui_col_end_date" /></label>
          <span><span class="field-symbol"></span><span class="required-ind required"></span><input type="text" class="datepicker edit-contract-field" id="azure-end-date" size="11" /><span class="locked-display-value"></span></span>
        </div>
        <div class="field aws-field">
          <label>Customer Type</label>
          <span><span class="field-symbol"></span><span class="required-ind required"></span><select id="azure-customer-type" class="edit-contract-field">
              <option value=""></option>
              <option value="netnew">New AWS Customer</option>
              <option value="existing">Existing AWS Customer</option>
          </select><span class="locked-display-value"></span><span class="hint-text small-hint block-hint">(This affects the pricing, and should be given to you along with the account ID. If you're not sure, please ask someone on the Ops team.)</span></span>
        </div>
        <div class="field azure-field">
          <label>CI Name</label>
          <span><span class="field-symbol"></span><span class="required-ind"></span><input type="text" id="azure-ci-name" size="25" class="edit-contract-field" /><span class="locked-display-value"></span></span>
        </div>
        <div class="field m365-field" style="display:none;">
          <label>Subscription Type</label>
          <span><span class="field-symbol"></span><span class="required-ind required"></span><select id="azure-m365-type" class="edit-contract-field">
            <option value="M365">M365</option>
            <option value="O365">O365</option>
          </select><span class="locked-display-value"></span></span>
        </div>
        <div class="field m365-field azure-m365-type-m365" style="display:none;">
          <label>Support Type</label>
          <span><span class="field-symbol"></span><span class="required-ind required"></span><select id="azure-m365-support-type" class="edit-contract-field">
            <option value="flat">Flat Fee</option>
            <option value="percent">Percentage of MR Price</option>
          </select><span class="locked-display-value"></span></span>
        </div>
        <div class="field m365-field azure-m365-type-m365" style="display:none;">
          <label>Support Amount</label>
          <span><span class="field-symbol"><span class="azure-m365-support-flat">$</span></span><span class="required-ind required"></span><input type="text" id="azure-m365-support-amount" class="currency edit-contract-field" size="11" /><span class="locked-display-value"></span><span class="azure-m365-support-percent" style="display:none">%</span></span>
        </div>
        <div class="field m365-field" style="display:none;">
          <label>Active</label>
          <span><span class="field-symbol"></span><span class="required-ind required"></span><select id="azure-m365-active" class="edit-contract-field">
            <option value="true">Yes</option>
            <option value="false">No</option>
          </select><span class="locked-display-value"></span></span>
        </div>
        
        <%--
        <div class="form-divider"></div>
        <div class="field">
          <label><spring:message code="ui_col_notes" /></label>
          <span><textarea id="pcr-notes" maxlength="500"></textarea></span>
        </div>
        --%>
    </div>
</div>

<div id="delete-azure-dialog" class="dialog">
    <div class="message-content"></div>
      
    <div class="dialog-loader-overlay">
        <div class="dialog-loader">
            <img src="${ajax_loader}" />
            <div><spring:message code="ui_dialog_delete_contractserviceazure_loading" /></div>
        </div>
    </div>
    <div class="dialog-instruct"><spring:message code="ui_dialog_delete_contractserviceazure_instruct" /></div>
      
    <div class="dialog-content">
      <div class="delete-msg"><i class="fa fa-warning"></i><spring:message code="ui_dialog_delete_contractserviceazure_warning" /></div>
      <div class="delete-item"><spring:message code="ui_dialog_delete_contractserviceazure_delete_item" /> <span id="delete-azure-subscription"></span></div>
    </div>
</div>

<div id="map-contract-service-to-parent-dialog" class="dialog">
    <div class="message-content"></div>
      
    <div class="dialog-loader-overlay">
        <div class="dialog-loader">
            <img src="${ajax_loader}" />
            <div>Mapping Child Line Item to Parent</div>
        </div>
    </div>
    <div class="dialog-instruct">Select the parent line item you would like map this to.</div>
      
    <div class="dialog-content">
      <div class="field">
         <label>Parent Line Item</label>
         <span><span class="field-symbol"></span><span class="required-ind required"></span><select style="width:420px;" id="map-contract-service-parent"></select></span>
       </div>
    </div>
</div>

<select class="contract-group-list" id="contract-group-clone" style="display:none;">
  <option value="#"></option>
  <c:forEach items="${groups}" var="group">
  <option value="${group.id}">${group.name}</option>
  </c:forEach>
</select>

<spring:url value="/contractservices/import" var="upload_url" />
<div id="upload-dialog" class="dialog">
    <div class="message-content"></div>
      
    <div class="dialog-loader-overlay">
        <div class="dialog-loader">
            <img src="${ajax_loader}" />
            <div><spring:message code="ui_upload_loading" /></div>
        </div>
    </div>
    <div class="dialog-instruct"><spring:message code="ui_dialog_upload_instruct" /></div>
      
    <div class="dialog-content">
	    <iframe id="upload-target" name="upload-target" height="0" width="0" frameborder="0" scrolling="yes"></iframe>
	
	    <form id="upload-form" action="${upload_url}" method="post" enctype="multipart/form-data" target="upload-target">
	        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
	        <div class="field">
	          <label class="short"><spring:message code="ui_col_excel_file" /></label>
	          <span><span class="field-symbol"></span><span class="required-ind required"></span><input type="file" name="templatefile" id="template-file" /></span>
	        </div>
	        <%-- <input type="submit" value="Submit" id="submit-btn"/> --%>
	    </form>
	</div>
</div>

<spring:url value="/contracts/docs/${contract.id}/upload" var="contract_upload_url" />
<div id="contract-upload-dialog" class="dialog">
    <div class="message-content"></div>
      
    <div class="dialog-loader-overlay">
        <div class="dialog-loader">
            <img src="${ajax_loader}" />
            <div><spring:message code="ui_upload_loading" /></div>
        </div>
    </div>
    <div class="dialog-instruct"><spring:message code="ui_dialog_doc_upload_instruct" /></div>
      
    <div class="dialog-content">
        <iframe id="contract-upload-target" name="contract-upload-target" height="0" width="0" frameborder="0" scrolling="yes"></iframe>
    
        <form id="contract-upload-form" action="${contract_upload_url}" method="post" enctype="multipart/form-data" target="contract-upload-target">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
            <div class="field">
              <label class="short">File</label>
              <span><span class="field-symbol"></span><span class="required-ind required"></span><input type="file" name="contractfile" id="contract-file" /></span>
            </div>
            <%-- <input type="submit" value="Submit" id="submit-btn"/> --%>
        </form>
    </div>
</div>

<div id="delete-contract-doc-dialog" class="dialog">
    <div class="message-content"></div>
      
    <div class="dialog-loader-overlay">
        <div class="dialog-loader">
            <img src="${ajax_loader}" />
            <div>Deleting Document...</div>
        </div>
    </div>
    <div class="dialog-instruct"><spring:message code="ui_dialog_doc_delete_instruct" /></div>
      
    <div class="dialog-content">
      <div class="delete-msg"><spring:message code="ui_dialog_doc_delete_warning" /></div>
    </div>
</div>

<div id="delete-pcr-dialog" class="dialog">
    <div class="message-content"></div>
      
    <div class="dialog-loader-overlay">
        <div class="dialog-loader">
            <img src="${ajax_loader}" />
            <div><spring:message code="ui_dialog_delete_contractupdate_loading" /></div>
        </div>
    </div>
    <div class="dialog-instruct"><spring:message code="ui_dialog_delete_contractupdate_instruct" /></div>
      
    <div class="dialog-content">
      <div class="delete-msg"><i class="fa fa-warning"></i><spring:message code="ui_dialog_delete_contractupdate_warning" /></div>
      <div class="delete-item"><spring:message code="ui_title_to_be_deleted" /> <span id="delete-pcr-name"></span></div>
    </div>
</div>

<div id="pricing-product-dialog" class="dialog">
    <div class="message-content"></div>
      
    <div class="dialog-loader-overlay">
        <div class="dialog-loader">
            <img src="${ajax_loader}" />
            <div><spring:message code="ui_pricing_sheet_product_saving" /></div>
        </div>
    </div>
    <div class="dialog-instruct"><spring:message code="ui_dialog_general_instruct" /></div>
      
    <div class="dialog-content">
        <div class="field">
          <label><spring:message code="ui_col_service_code" /></label>
          <span><span class="field-symbol"></span><span class="required-ind required"></span><input type="text" class="autocomplete" id="pricing-product-part-number" size="30" />
          </span>
        </div>
        <div class="field">
          <label><spring:message code="ui_col_part_description" /></label>
          <span><span class="field-symbol"></span><span class="required-ind required"></span><input type="text" class="autocomplete contract-service-input-long" size="70" id="pricing-product-description" />
          </span>
        </div>
        <input type="hidden" id="pricing-product-device" />
        <%--
        <div class="field">
          <label class="short"><spring:message code="ui_col_part_description" /></label>
          <span><span class="field-symbol"></span><span class="required-ind"></span><select id="pricing-product-device" style="max-width:600px;"></select>
          	<span id="pricing-product-device-display"></span>
          	<input type="hidden" id="pricing-product-device" />
          </span>
        </div> --%>
        <%--
        <div class="field">
          <label class="short"><spring:message code="ui_col_service_name" /></label>
          <span><span class="field-symbol"></span><span class="required-ind required"></span><select id="pricing-product-service" class="service-dropdown">
              <option value=""></option>
                <c:forEach items="${fullServices}" var="fullService">
                <option value="${fullService.serviceId}" class="osp-id-${fullService.ospId}">${fullService.name}</option>
                </c:forEach>
            </select>
          </span>
        </div>
         --%>
        <div class="form-divider"></div>
        
        <div class="field">
          <label class="short"><spring:message code="ui_label_auto_update" /></label>
          <span><span class="field-symbol"></span><span class="required-ind required"></span><select id="pricing-product-manual-override">
          	<option value="true">Yes</option>
          	<option value="false">No</option>
          </select> <span class="small-hint">(<spring:message code="ui_msg_pricing_sheet_auto_update_hint" />)</span></span>
        </div>
        
        <div class="field">
          <label class="short"><spring:message code="ui_col_one_time_cost" /></label>
          <span><span class="field-symbol">$</span><span class="required-ind required"></span><input type="text" class="add-contract-service-calculate currency" id="pricing-product-onetime-price" placeholder="(ex. 150.00)" size="12" /> <span class="small-hint">(<spring:message code="ui_title_per_unit" />)</span></span>
        </div>
        <div class="field">
          <label class="short"><spring:message code="ui_col_recurring_cost" /></label>
          <span><span class="field-symbol">$</span><span class="required-ind required"></span><input type="text" class="add-contract-service-calculate currency" id="pricing-product-recurring-price" placeholder="(ex. 265.39)" size="12" /> <span class="small-hint">(<spring:message code="ui_title_per_unit" />)</span></span>
        </div>
        <div class="field">
          <label class="short"><spring:message code="ui_col_removal_cost" /></label>
          <span><span class="field-symbol">$</span><span class="required-ind required"></span><input type="text" class="add-contract-service-calculate currency" id="pricing-product-removal-price" placeholder="(ex. 50.00)" size="12" /> <span class="small-hint">(<spring:message code="ui_title_per_unit" />)</span></span>
        </div>
        
        <div class="notice-msg" id="pricing-sheet-status-message"></div>
    </div>
</div>


<div id="add-m365-pricing-product-dialog" class="dialog">
    <div class="message-content"></div>
      
    <div class="dialog-loader-overlay">
        <div class="dialog-loader">
            <img src="${ajax_loader}" />
            <div><spring:message code="ui_pricing_sheet_product_saving" /></div>
        </div>
    </div>
    <div class="dialog-instruct"><spring:message code="ui_dialog_general_instruct" /></div>
      
    <div class="dialog-content">
        <div class="field">
          <label><spring:message code="ui_col_service_code" /></label>
          <span><span class="field-symbol"></span><span class="required-ind required"></span><input type="text" class="autocomplete" id="m365-pricing-product-part-number" size="30" />
          </span>
        </div>
        <div class="field">
          <label><spring:message code="ui_col_part_description" /></label>
          <span><span class="field-symbol"></span><span class="required-ind required"></span><input type="text" class="autocomplete contract-service-input-long" size="70" id="m365-pricing-product-description" />
          </span>
        </div>
        <input type="hidden" id="pricing-product-unit-count" />
        <div class="form-divider"></div>
        <div class="field">
          <label>Discount %</label>
          <span><span class="field-symbol"></span><span class="required-ind required"></span><input type="text" class="currency" id="pricing-product-discount" placeholder="8.50%" size="12" /> %</span>
        </div>
        <div class="field">
          <label>List Price</label>
          <span><span class="field-symbol"></span><span class="required-ind"></span><span id="pricing-product-erp-price"></span></span>
        </div>
        <div class="field">
          <label>Customer Price</label>
          <span><span class="field-symbol"></span><span class="required-ind"></span><span id="pricing-product-discounted-price"></span></span>
        </div>
    </div>
</div>

<div id="add-m365nc-pricing-product-dialog" class="dialog">
    <div class="message-content"></div>
      
    <div class="dialog-loader-overlay">
        <div class="dialog-loader">
            <img src="${ajax_loader}" />
            <div><spring:message code="ui_pricing_sheet_product_saving" /></div>
        </div>
    </div>
    <div class="dialog-instruct"><spring:message code="ui_dialog_general_instruct" /></div>
      
    <div class="dialog-content">
        <div class="field">
          <label><spring:message code="ui_col_service_code" /></label>
          <span><span class="field-symbol"></span><span class="required-ind required"></span><input type="text" class="autocomplete" id="m365nc-pricing-product-part-number" size="30" />
          </span>
        </div>
        <div class="field">
          <label><spring:message code="ui_col_part_description" /></label>
          <span><span class="field-symbol"></span><span class="required-ind required"></span><input type="text" class="autocomplete contract-service-input-long" size="70" id="m365nc-pricing-product-description" />
          </span>
        </div>
        <input type="hidden" id="pricing-product-unit-count" />
        <div class="form-divider"></div>
        <div class="field">
          <label>Discount %</label>
          <span><span class="field-symbol"></span><span class="required-ind required"></span><input type="text" class="currency" id="m365nc-pricing-product-discount" placeholder="8.50%" size="12" /> %</span>
        </div>
        <div class="field">
          <label>List Price</label>
          <span><span class="field-symbol"></span><span class="required-ind"></span><span id="m365nc-pricing-product-erp-price"></span></span>
        </div>
        <div class="field">
          <label>Customer Price</label>
          <span><span class="field-symbol"></span><span class="required-ind"></span><span id="m365nc-pricing-product-discounted-price"></span></span>
        </div>
        <div class="field">
          <label>Term Duration</label>
          <span><span class="field-symbol"></span><span class="required-ind"></span><span id="m365nc-pricing-product-term-duration"></span></span>
        </div>
        <div class="field">
          <label>Billing Plan</label>
          <span><span class="field-symbol"></span><span class="required-ind"></span><span id="m365nc-pricing-product-billing-plan"></span></span>
        </div>
        <div class="field">
          <label>Segment</label>
          <span><span class="field-symbol"></span><span class="required-ind"></span><span id="m365nc-pricing-product-segment"></span></span>
        </div>
    </div>
</div>

<div id="delete-pricing-product-dialog" class="dialog">
    <div class="message-content"></div>
      
    <div class="dialog-loader-overlay">
        <div class="dialog-loader">
            <img src="${ajax_loader}" />
            <div><spring:message code="ui_pricing_sheet_product_deleting" /></div>
        </div>
    </div>
    <div class="dialog-instruct"><spring:message code="ui_dialog_pricing_sheet_product_delete_instruct" /></div>
      
    <div class="dialog-content">
      <div class="delete-msg"><i class="fa fa-warning"></i><spring:message code="ui_pricing_sheet_product_delete_warning" /></div>
      <div class="delete-item"><spring:message code="ui_title_to_be_deleted" /> <span id="delete-pricing-product-name"></span></div>
    </div>
</div>