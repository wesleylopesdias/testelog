<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt_rt" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<spring:url value="/resources/images/ajax-loader.gif" var="ajax_loader" />
<spring:url value="/resources/js/cost-allocations.js" var="cost_allocations_js" />
<script src="${cost_allocations_js}" type="text/javascript"></script>

<style type="text/css">
.container { max-width:none; }
</style>

<jsp:include page="sub-nav.jsp" />

<input type="hidden" id="for-month-of" value="${forMonthOf}" />

<div id="status-msg" class="notice-msg center" style="font-size:0.9em; display:none;">
	<div id="top-loader"><img src="${ajax_loader}" /> <span>Re-calculating Changes</span></div>
	<div id="changed-msg"><spring:message code="ui_msg_cost_allocation_pending_changes" /></div>
</div>

<section class="cost-allocation-totals">
	<div class="row">
		<div class="column-2">
			<div>
				<h3><spring:message code="ui_title_cloud_costs" /></h3>
				<div class="row">
					<label><spring:message code="ui_label_view_date" /></label>
					<span class="value right">
						<select id="allocation-date-range"></select>
					</span>
					<span class="compare right" style="font-weight:600; color:#666;">Allocated</span>
				</div>
				<div class="row col-1">
					<label><span class="cube col-1"></span><spring:message code="ui_label_multi_tenant_depreciation" /></label>
					<span class="value right">$&nbsp;<input type="text" size="10" class="right" id="allocation-multi-tenant-total" /></span>
					<span class="compare right" id="allocation-multi-tenant-allocated-amount"></span>
				</div>
				<div class="row col-2">
					<label><span class="cube col-2"></span><spring:message code="ui_label_datacenter_rent" /></label>
					<span class="value right">$&nbsp;<input type="text" size="10" class="right" id="allocation-rent-total" /></span>
					<span class="compare right" id="allocation-rent-allocated-amount"></span>
				</div>
				<div class="row col-3">
					<label><span class="cube col-3"></span><spring:message code="ui_label_specific_expenses" /> 
					(<a href="javascript:;" class="popup-link" data-dialog="allocation-specific-services-dialog">View Expenses</a>)</label>
					<span class="value right" id="allocation-specific-total"></span>
					<span class="compare right" id="allocation-specific-allocated-amount"></span>
				</div>
				<div class="row col-4" style="display:none;">
					<label><span class="cube col-4"></span><spring:message code="ui_label_dedicated_depreciation" /></label>
					<span class="value right" id="allocation-dedicated-total"></span>
					<span class="compare right"></span>
				</div>
			</div>
		</div>
		<div class="column-2 right">
		  <div class="column-3-2">
			<div class="open-status">
				<a href="javascript:;" class="btn-link pull-right" id="generate-btn" style="margin-left:8px;"><i class="fa fa-file"></i><spring:message code="ui_label_generate_costs" /></a>
				<a href="javascript:;" class="btn-link pull-right" id="save-btn"><i class="fa fa-save"></i><spring:message code="ui_label_save" /></a>
	  			<div class="clearer"></div>
  			</div>
  			<div class="processed-status">
  				<div class="notice-msg center"><i class="fa fa-lock"></i><spring:message code="ui_msg_costallocation_month_already_processed" /></div>
  			</div>
  		  </div>
		</div>
	</div>
</section>

<section class="cost-allocation-table">
	<div class="table-wrapper">
	<a href="javascript:;" class="popup-link pull-right open-status" data-dialog="add-allocation-dialog"><i class="fa fa-plus-square"></i><spring:message code="ui_label_add_row" /></a>
	<a href="javascript:;" class="popup-link pull-right" data-dialog="import-allocation-dialog" id="import-lineitems-btn" style="margin-right:10px;"><i class="fa fa-cloud-download-alt"></i><spring:message code="ui_label_import_rows" /></a>
	<a href="javascript:;" class="popup-link pull-right open-status" data-dialog="allocation-spread-dialog" style="margin-right:10px;"><i class="fa fa-calculator"></i>Spread %</a>
	<table id="cost-allocation-table">
		<thead>
			<tr>
				<th colspan="3"></th>
				<th colspan="2" class="center col-1"><spring:message code="ui_col_multi_tenant" /></th>
				<th colspan="2" class="center col-2"><spring:message code="ui_col_data_center" /></th>
				<th colspan="3" class="center col-3"><spring:message code="ui_col_specific_expenses" /></th>
				<th colspan="6"></th>
			</tr>
			<tr>
				<th><spring:message code="ui_col_service" /></th>
				<th><spring:message code="ui_col_part_number" /></th>
				<th>Infrastructure Note</th>
				<th class="col-1 center"><spring:message code="ui_col_allocation_percent" /></th>
				<th class="col-1 center"><spring:message code="ui_col_amount" /></th>
				<th class="col-2 center"><spring:message code="ui_col_allocation_percent" /></th>
				<th class="col-2 center"><spring:message code="ui_col_amount" /></th>
				<th class="col-3 center"><spring:message code="ui_col_specific_expense" /></th>
				<th class="col-3 center center"><spring:message code="ui_col_allocation_percent" /></th>
				<th class="col-3"><spring:message code="ui_col_amount" /></th>
				<th class="center"><spring:message code="ui_col_cost_total_cost" /></th>
				<th class="center"><spring:message code="ui_col_cost_units" /></th>
				<th class="center"><spring:message code="ui_col_cost_unit_cost" /></th>
				<th>Catalog Cost</th>
				<th>Variance</th>
				<th></th>
			</tr>
		</thead>
		<tbody></tbody>
		<tfoot></tfoot>
	</table>
	</div>
</section>


<div id="add-allocation-dialog" class="dialog">
    <div class="message-content"></div>
      
    <div class="dialog-loader-overlay">
        <div class="dialog-loader">
            <img src="${ajax_loader}" />
            <div>Adding Row...</div>
        </div>
    </div>
    <div class="dialog-instruct"><spring:message code="ui_dialog_general_instruct" /></div>
      
    <div class="dialog-content">
      <div class="field">
        <label>Device</label>
        <span><span class="field-symbol"></span><span class="required-ind required"></span><select id="allocation-device" style="width:400px;">
		</select></span>
      </div>
    </div>
</div>

<div id="import-allocation-dialog" class="dialog">
    <div class="message-content"></div>
      
    <div class="dialog-loader-overlay">
        <div class="dialog-loader">
            <img src="${ajax_loader}" />
            <div><spring:message code="ui_msg_cost_allocation_import_loading" /></div>
        </div>
    </div>
    <div class="dialog-instruct"><spring:message code="ui_dialog_general_instruct" /></div>
      
    <div class="dialog-content">
      <div class="field">
        <label><spring:message code="ui_col_cost_allocation_date" /></label>
        <span><span class="field-symbol"></span><span class="required-ind required"></span><select id="allocation-import-date-range"></select></span>
      </div>
    </div>
</div>

<div id="allocation-specific-services-dialog" class="dialog">
    <div class="message-content"></div>
      
    <div class="dialog-loader-overlay">
        <div class="dialog-loader">
            <img src="${ajax_loader}" />
            <div></div>
        </div>
    </div>
    <div class="dialog-instruct">These are the services that have unallocated specific expenses uploaded.</div>
      
    <div class="dialog-content">
      <div class="field">
        <label>Services</label>
        <div class="value" id="allocation-specific-services-list"></div>
      </div>
    </div>
</div>

<div id="allocation-spread-dialog" class="dialog">
    <div class="message-content"></div>
      
    <div class="dialog-loader-overlay">
        <div class="dialog-loader">
            <img src="${ajax_loader}" />
            <div></div>
        </div>
    </div>
    <div class="dialog-instruct">These are the services that have unallocated specific expenses uploaded.</div>
      
    <div class="dialog-content">
      <div class="field">
        <label>Cost</label>
        <span><span class="field-symbol"></span><span class="required-ind required"></span><select id="allocation-spread-source">
            <option value=""></option>
            <option value="depreciation">Multi-Tenant Monthly Depreciation</option>
            <option value="rent">Datacenter Rent</option>
        </select></span>
      </div>
      <div class="field">
        <label>Spread Type</label>
        <span><span class="field-symbol"></span><span class="required-ind required"></span><select id="allocation-spread-type">
            <option value="percent">Percentage</option>
            <option value="dollar">Dollar Amount</option>
        </select></span>
      </div>
      <div class="field" id="spread-percent-container">
        <label>Spread Percentage</label>
        <span><span class="field-symbol"></span><span class="required-ind required"></span><input size="4" type="text" class="double" id="allocation-spread-percent" />%</span>
      </div>
      <div class="field" style="display:none" id="spread-dollar-container">
        <label>Spread Amount</label>
        <span><span class="field-symbol">$</span><span class="required-ind required"></span><input size="6" type="text" class="currency" id="allocation-spread-dollar" /></span>
      </div>
      <div class="field">
        <label>Spread Across</label>
        <div id="allocation-spread-part-numbers"></div>
      </div>
    </div>
</div>