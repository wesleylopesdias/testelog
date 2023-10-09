<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt_rt" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<spring:url value="/resources/images/ajax-loader.gif" var="ajax_loader" />
<spring:url value="/resources/js/costs.js" var="costs_js" />
<script src="${costs_js}" type="text/javascript"></script>

<spring:url value="/costs/allocation/forlineitem" var="cost_allocation_link"/>
<input type="hidden" id="current-viewing-start-date" value="" />
<input type="hidden" id="current-viewing-end-date" value="" />
<input type="hidden" id="general-error-msg" value="<spring:message code="ui_dialog_general_validation_error" />" />
<input type="hidden" id="error-invalid-date-msg" value="<spring:message code="ui_dialog_validation_invalid_date" />" />
<input type="hidden" id="error-cost-fractions-msg" value="<spring:message code="ui_dialog_validation_cost_fractions" />" />
<input type="hidden" id="error-for-customer-msg" value="<spring:message code="ui_dialog_validation_cost_for_customer" />" />
<input type="hidden" id="error-acquired-disposal-msg" value="<spring:message code="ui_dialog_validation_cost_acquired_disposal_date_range" />" />
<input type="hidden" id="ok-asset-created-msg" value="<spring:message code="ui_dialog_ok_cost_asset_created" />" />
<input type="hidden" id="ok-expense-created-msg" value="<spring:message code="ui_dialog_ok_cost_expense_created" />" />
<input type="hidden" id="ok-asset-updated-msg" value="<spring:message code="ui_dialog_ok_cost_asset_updated" />" />
<input type="hidden" id="ok-expense-updated-msg" value="<spring:message code="ui_dialog_ok_cost_expense_updated" />" />
<input type="hidden" id="ok-cost-deleted-msg" value="<spring:message code="ui_dialog_ok_cost_deleted" />" />
<input type="hidden" id="file-select-file-error-msg" value="<spring:message code="ui_error_select_file" />" />
<input type="hidden" id="file-type-invalid-error-msg" value="<spring:message code="ui_error_invalid_file_type" />" />
<input type="hidden" id="cost-allocation-link" value="${cost_allocation_link}" />

<c:set var="is_admin" value="hasRole('ROLE_ADMIN')" />
<sec:authorize access="${is_admin}">
<input type="hidden" id="is-admin" value="true" />
</sec:authorize>

<jsp:include page="sub-nav.jsp" />

<spring:url value="/reports/expensecategories" var="cost_categories_report" />
<div class="app-plug" style="margin-top:10px;">
  Looking to see what's in each cost category? <a href="${cost_categories_report}">Check the Cost Categories Report<i class="fa fa-arrow-circle-right icon-right"></i></a>
</div>

<section class="section-wrapper">
  <a href="javascript:;" class="popup-link btn-link pull-right" data-dialog="upload-dialog"><i class="fa fa-upload"></i><spring:message code="ui_title_import_excel" /></a>
  <div class="clearer"></div>
</section>

<section class="section-wrapper costs-view-parameters">
  <div class="section-title">View Params</div>
  <div class="row">
    <div class="column-3">
      <div class="field">
        <label>Cost Type</label>
        <span>
          <select id="view-type">
            <option value="all"><spring:message code="ui_val_cost_view_all_costs" /></option>
            <option value="asset"><spring:message code="ui_val_cost_view_all_assets" /></option>
            <option value="expense"><spring:message code="ui_val_cost_view_all_expenses" /></option>
            <option value="unallocated"><spring:message code="ui_val_cost_view_all_service_expense_allocations" /></option>
          </select>
        </span>
      </div>
    </div>
    <div class="column-3 center">
      <div class="field">
        <label>Date View</label>
        <span>
          <select id="view-date-type">
            <option value="all"><spring:message code="ui_val_date_view_all_time" /></option>
            <option value="month" selected="selected"><spring:message code="ui_val_date_view_by_month" /></option>
            <option value="year"><spring:message code="ui_val_date_view_by_year" /></option>
          </select>
        </span>
      </div>
    </div>
    <div class="column-3 right" id="expense-type-filter-column">
      <div class="field">
        <label>Expense Type</label>
        <span>
          <select id="view-cost-type">
          	<option value="">All</option>
          	<c:forEach items="${costTypes}" var="costType">
              <option value="${costType}">${costType.description}</option>
            </c:forEach>
          </select>
        </span>
      </div>
    </div>
  </div>
</section>

<section class="contract-viewing costs-viewing">
  <div class="row">
    <div class="column-3 bottom"><span id="current-view-prev-month"><a href="#">&laquo;May</a></span></div>
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
    <div class="column-3 text-right bottom"><span id="current-view-next-month"><a href="#">Jul&raquo;</a></span></div>
  </div>
</section>

<section class="table-wrapper" id="costs-table">
  <div class="table-header">
    <div class="table-title">
      <spring:message code="ui_title_costs" />
    </div>
    <div class="table-links">
      <a href="javascript:;" class="popup-link cost-popup-link" data-id="add" data-dialog="cost-dialog" id="add-cost-link"><i class="fa fa-plus-square"></i><spring:message code="ui_title_add_cost" /></a>
      <a href="javascript:;" class="popup-link unallocated-popup-link" data-id="" data-dialog="unallocated-dialog" id="add-unallocated-link" style="display:none;"><i class="fa fa-plus-square"></i>Add Service Allocation</a>
    </div>
  </div>
  
  <table>
    <thead>
      <tr>
        <th><spring:message code="ui_col_cost_name" /></th>
        <th><spring:message code="ui_col_asset_expense" /></th>
        <th>Expense Type</th>
        <th class="right"><spring:message code="ui_col_cost_date" /></th>
        <th class="right"><spring:message code="ui_col_cost_amount" /></th>
        <sec:authorize access="${is_admin}"><th class="right"><spring:message code="ui_col_options" /></th></sec:authorize>
      </tr>
    </thead>
    <tbody></tbody>
    <tfoot></tfoot>
  </table>
</section>

<div id="cost-dialog" class="dialog">
    <div class="message-content"></div>
      
    <div class="dialog-loader-overlay">
        <div class="dialog-loader">
            <img src="${ajax_loader}" />
            <div><spring:message code="ui_cost_loading" /></div>
        </div>
    </div>
    <div class="dialog-instruct"><spring:message code="ui_dialog_general_instruct" /></div>
      
    <div class="dialog-content">
        <div class="field">
          <label class="short"><spring:message code="ui_col_asset_expense" /></label>
          <span><span class="field-symbol"></span><span class="required-ind required"></span>
            <select id="cost-group">
              <option value="asset"><spring:message code="ui_val_asset" /></option>
              <option value="expense"><spring:message code="ui_val_expense" /></option>
            </select>
          </span>
        </div>
        <div class="field">
          <label class="short"><spring:message code="ui_col_cost_location" /></label>
          <span><span class="field-symbol"></span><span class="required-ind"></span><select id="cost-location">
              <option value="#"></option>
              <c:forEach items="${locations}" var="location">
              <option value="${location.id}">${location.name}</option>
              </c:forEach>
            </select>
          </span>
        </div>
        <div class="field">
          <label class="short"><spring:message code="ui_col_cost_name" /></label>
          <span><span class="field-symbol"></span><span class="required-ind required"></span><input type="text" size="30" id="cost-name" /></span>
        </div>
        <div class="field">
          <label class="short"><spring:message code="ui_col_cost_description" /></label>
          <span><span class="field-symbol"></span><span class="required-ind"></span><input type="text" size="55" id="cost-description" /></span>
        </div>
        <div class="field expense">
          <label class="short"><spring:message code="ui_col_expense_type" /></label>
          <span><span class="field-symbol"></span><span class="required-ind required"></span>
            <select id="cost-type">
              <c:forEach items="${costTypes}" var="costType">
                <option id="${costType}" value="${costType}">${costType.description}</option>
              </c:forEach>
            </select>
          </span>
        </div>
        <div class="field expense">
          <label class="short"><spring:message code="ui_col_expense_subtype" /></label>
          <span><span class="field-symbol"></span><span class="required-ind"></span>
            <select id="cost-subtype">
              <c:forEach items="${costSubTypes}" var="costSubType">
                <option id="${costSubType}" value="${costSubType}">${costSubType.description}</option>
              </c:forEach>
            </select><span class="field-hint"><spring:message code="ui_col_expense_subtype_hint" /></span>
          </span>
        </div>
        
        <div id="id-ca-app-plug" class="app-plug" style="display:none; margin-bottom:0px;"></div>
        <div class="form-divider"></div>
        
        <div class="field">
          <label class="short"><spring:message code="ui_col_cost_amount" /></label>
          <span><span class="field-symbol">$</span><span class="required-ind required"></span><input type="text" size="10" id="cost-amount" class="currency" /></span>
        </div>
        
        <div class="row asset">
          <div class="column-2">
            <div class="field">
	          <label class="short-column-2"><spring:message code="ui_col_cost_date_acquired" /></label>
	          <span><span class="field-symbol"></span><span class="required-ind required"></span><input type="text" size="10" id="cost-date-acquired" class="datepicker" /></span>
	        </div>
          </div>
          <div class="column-2">
            <div class="field">
              <label><spring:message code="ui_col_cost_disposal_date" /></label>
              <span><span class="field-symbol"></span><span class="required-ind"></span><input type="text" size="10" id="cost-disposal-date" class="datepicker" /></span>
            </div>
          </div>
        </div>
        
        <div class="field asset">
          <label class="short"><spring:message code="ui_col_cost_depreciable_life" /></label>
          <span><span class="field-symbol"></span><span class="required-ind required"></span><input type="text" size="3" id="cost-depreciable-life" class="integer" /><span class="small-hint"> <spring:message code="ui_dialog_depreciable_life_hint" /></span></span>
        </div>
        
        <div class="row asset">
          <div class="column-2">
            <div class="field">
              <label class="short-column-2"><spring:message code="ui_col_cost_asset_number" /></label>
              <span><span class="field-symbol"></span><span class="required-ind"></span><input type="text" size="12" id="cost-asset-number" /></span>
            </div>
          </div>
          <div class="column-2">
            <div class="field">
              <label><spring:message code="ui_col_cost_serial_number" /></label>
              <span><span class="field-symbol"></span><span class="required-ind"></span><input type="text" size="12" id="cost-serial-number" /></span>
            </div>
          </div>
        </div>
        
        <div class="field expense">
          <label class="short"><spring:message code="ui_col_cost_expense_date" /></label>
          <span><span class="field-symbol"></span><span class="required-ind required"></span><input type="text" size="10" id="cost-date" class="datepicker" /></span>
        </div>
        
        <div class="form-divider"></div>
        
        <div class="table-wrapper small-bottom-margin">
          <%-- <div class="table-header">
            <div class="table-title">&nbsp;</div>
            <div class="table-links">
              <a href="javascript:;" id="edit-contract-delete-all" data-action="delete"><i class="fa fa-minus-circle"></i><spring:message code="ui_title_delete_all" /></a>
            </div>
          </div> --%>
	        <table class="edit-table" id="modify-cost-table">
	          <thead>
	            <tr>
	              <th>&nbsp;</th>
	              <th><span><spring:message code="ui_col_cost_category" /></span></th>
	              <th class="asset"><spring:message code="ui_col_quantity" /></th>
	              <th><spring:message code="ui_col_cost_fraction" /></th>
	              <th>&nbsp;</th>
	            </tr>
	          </thead>
	          <tbody>
	            <tr id="clone-row" style="display:none;">
                  <td></td>
                  <td>
                    <span class="required-ind required"></span>
                    <select name="cost-asset-category">
                      <option value="#"></option>
                      <c:forEach items="${expenseCategories}" var="expenseCategory">
                        <option value="${expenseCategory.id}" data-units="${expenseCategory.units}"><c:if test="${expenseCategory.parent ne null}">${expenseCategory.parent.name} - </c:if>${expenseCategory.name}</option>
                      </c:forEach>
                    </select>
                  </td>
                  <td class="asset"><span class="required-ind required"></span><input type="text" size="8" name="cost-units" /> <span class="units-display"></span></td>
                  <td><span class="required-ind required"></span><input type="text" size="4" name="cost-fraction" />&nbsp;%</td>
                  <td><a href="javascript:;" class="remove-row"><i class="fa fa-minus"></i><spring:message code="ui_title_remove_row" /></a></td>
                </tr>
	          </tbody>
	        </table>
	        <div class="footer-link">
	          <a href="javascript:;" id="add-row"><i class="fa fa-plus"></i><spring:message code="ui_title_add_another_row" /></a>
	        </div>
        </div>
        
        <div class="form-divider"></div>
        
        <div class="field-checkbox">
          <label><input type="checkbox" id="cost-for-customer" /><spring:message code="ui_title_purchase_for_customer" /></label>
        </div>
        <div class="row cost-customer-row" style="display:none;">
            <div class="field">
              <label class="short"><spring:message code="ui_col_customer" /></label>
              <span><span class="field-symbol"></span><span class="required-ind required"></span>
                <select id="cost-customer">
                  <option value="#"></option>
                  <c:forEach items="${customers}" var="customer">
                    <option value="${customer.id}">${customer.name}</option>
                  </c:forEach>
                </select>
              </span>
          </div>
          <div class="field">
            <label class="short"><spring:message code="ui_col_contract" /></label>
            <span><span class="field-symbol"></span><span class="required-ind"></span>
              <select id="cost-customer-contract">
                <option value="#"></option>
              </select>
            </span>
          </div>
        </div>
    </div>
</div>

<div id="delete-cost-dialog" class="dialog">
    <div class="message-content"></div>
      
    <div class="dialog-loader-overlay">
        <div class="dialog-loader">
            <img src="${ajax_loader}" />
            <div><spring:message code="ui_dialog_delete_cost_loading" /></div>
        </div>
    </div>
    <div class="dialog-instruct"><spring:message code="ui_dialog_delete_cost_instruct" /></div>
      
    <div class="dialog-content">
      <div class="delete-msg"><i class="fa fa-warning"></i><spring:message code="ui_dialog_delete_cost_warning" /></div>
      <div class="delete-item"><spring:message code="ui_title_to_be_deleted" /> <span id="delete-cost-name"></span></div>
    </div>
</div>

<spring:url value="/expenses/import" var="upload_url" />
<div id="upload-dialog" class="dialog">
    <div class="message-content"></div>
      
    <div class="dialog-loader-overlay">
        <div class="dialog-loader">
            <img src="${ajax_loader}" />
            <div><spring:message code="ui_upload_loading" /></div>
        </div>
    </div>
    <div class="dialog-instruct"><spring:message code="ui_dialog_cost_upload_instruct" /></div>
      
    <div class="dialog-content">
        <iframe id="upload-target" name="upload-target" height="0" width="0" frameborder="0" scrolling="yes"></iframe>
    
        <form id="upload-form" action="${upload_url}" method="post" enctype="multipart/form-data" target="upload-target">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
            <div class="field">
              <label class="short">Cost Type</label>
              <span><span class="field-symbol"></span><span class="required-ind required"></span><select name="importtype" id="import-type">
                  <option value="asset">Asset Upload</option>
                  <option value="cost">Expense Upload</option>
                </select>
              </span>
            </div>
            <div class="field" id="upload-expense-date-container" style="display:none;">
              <label class="short"><spring:message code="ui_col_expense_date" /></label>
              <span><span class="field-symbol"></span><span class="required-ind required"></span><input type="text" size="10" class="month-datepicker" maxlength="10" placeholder="Expense Date" name="uploadexpensedate" id="upload-expense-date" /></span>
            </div>
            <div class="field">
              <label class="short"><spring:message code="ui_col_excel_file" /></label>
              <span><span class="field-symbol"></span><span class="required-ind required"></span><input type="file" name="templatefile" id="template-file" /></span>
            </div>
            <%-- <input type="submit" value="Submit" id="submit-btn"/> --%>
        </form>
    </div>
</div>

<div id="unallocated-dialog" class="dialog">
    <div class="message-content"></div>
      
    <div class="dialog-loader-overlay">
        <div class="dialog-loader">
            <img src="${ajax_loader}" />
            <div><spring:message code="ui_cost_loading" /></div>
        </div>
    </div>
    <div class="dialog-instruct"><spring:message code="ui_dialog_general_instruct" /></div>
      
    <div class="dialog-content">
        <div class="field">
          <label class="short"><spring:message code="ui_col_cost_name" /></label>
          <span><span class="field-symbol"></span><span class="required-ind required"></span><input type="text" size="30" id="unallocated-name" /></span>
        </div>
        <div class="field">
          <label class="short"><spring:message code="ui_col_cost_description" /></label>
          <span><span class="field-symbol"></span><span class="required-ind"></span><input type="text" size="55" id="unallocated-description" /></span>
        </div>
        
        <div class="form-divider"></div>
        
        <div class="field">
          <label class="short"><spring:message code="ui_col_cost_amount" /></label>
          <span><span class="field-symbol">$</span><span class="required-ind required"></span><input type="text" size="10" id="unallocated-amount" class="currency" /></span>
        </div>
        
        <div class="field">
          <label class="short"><spring:message code="ui_col_cost_expense_date" /></label>
          <span><span class="field-symbol"></span><span class="required-ind required"></span><input type="text" size="10" id="unallocated-date" class="datepicker" /></span>
        </div>
        
        <div class="field">
          <label class="short">Service</label>
          <span><span class="field-symbol"></span><span class="required-ind required"></span>
            <select id="unallocated-service">
            	<option value=""></option>
              <c:forEach items="${services}" var="service">
                <option value="${service.ospId}">${service.name}</option>
              </c:forEach>
            </select>
          </span>
        </div>
        
        <div id="unallocated-already-msg" class="notice-msg" style="font-size:0.9em; margin-top:40px;"><i class="fa fa-info-circle"></i><spring:message code="ui_dialog_expense_already_allocated" /></div>
    </div>
</div>