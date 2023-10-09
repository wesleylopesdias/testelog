<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt_rt" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<spring:url value="/resources/images/ajax-loader.gif" var="ajax_loader" />
<spring:url value="/resources/js/cost-category.js" var="cost_category_js" />
<script src="${cost_category_js}" type="text/javascript"></script>

<input type="hidden" id="general-error-msg" value="<spring:message code="ui_dialog_general_validation_error" />" />
<input type="hidden" id="error-utilization-range-msg" value="<spring:message code="ui_dialog_validation_utilization_range" />" />
<input type="hidden" id="ok-category-created-msg" value="<spring:message code="ui_dialog_ok_category_created" />" />
<input type="hidden" id="ok-category-deleted-msg" value="<spring:message code="ui_dialog_ok_category_deleted" />" />
<input type="hidden" id="error-category-related-data-msg" value="<spring:message code="ui_dialog_error_related_cost_data" />" />
<c:set var="is_admin" value="hasRole('ROLE_ADMIN')" />
<sec:authorize access="${is_admin}">
<input type="hidden" id="is-admin" value="true" />
</sec:authorize>

<jsp:include page="../sub-nav.jsp" />

<section class="table-wrapper top-margin" id="costs-categories-table">
  <div class="table-header">
    <div class="table-title">
      <spring:message code="ui_title_header_cost_categories" />
    </div>
    <div class="table-links">
      <a href="javascript:;" class="popup-link edit-category" data-id="add" data-dialog="cost-category-dialog"><i class="fa fa-plus-square"></i><spring:message code="ui_title_add_cost_category" /></a>
    </div>
  </div>
  
  <section class="tabs">
    <a href="javascript:;" class="contract-services-tab table-tab selected" data-view="expenses-categories"><spring:message code="ui_title_cost_categories" /></a>
  </section>
  
  <table class="category-table expenses-categories">
    <thead>
      <tr>
        <th style="width:23%;"><spring:message code="ui_col_cost_category_name" /></th>
        <th><spring:message code="ui_col_cost_asset_description" /></th>
        <th><spring:message code="ui_col_cost_category_units" /></th>
        <th class="right"><spring:message code="ui_col_labor_split" /></th>
        <th class="right"><spring:message code="ui_col_cost_category_target_utilization" /></th>
        <sec:authorize access="${is_admin}"><th class="right"><spring:message code="ui_col_options" /></th></sec:authorize>
      </tr>
    </thead>
    <tbody></tbody>
  </table>
</section>

<div id="cost-category-dialog" class="dialog">
    <div class="message-content"></div>
      
    <div class="dialog-loader-overlay">
        <div class="dialog-loader">
            <img src="${ajax_loader}" />
            <div><spring:message code="ui_dialog_cost_category_loading" /></div>
        </div>
    </div>
    <div class="dialog-instruct"><spring:message code="ui_dialog_general_instruct" /></div>
      
    <div class="dialog-content">
      <div class="field category-display">
        <label><spring:message code="ui_col_cost_category_parent_category" /></label>
        <span><span class="field-symbol"></span><span class="required-ind"></span><select id="cost-category-parent-type">
            <option value="#"></option>
            <c:forEach items="${expenseCategories}" var="expenseCategory">
              <c:if test="${expenseCategory.parent eq null}">
              <option value="${expenseCategory.id}">${expenseCategory.name}</option>
              </c:if>
            </c:forEach>
          </select>
        </span>
      </div>
      <div class="field">
        <label><spring:message code="ui_col_cost_name" /></label>
        <span><span class="field-symbol"></span><span class="required-ind required"></span><input type="text" size="30" id="cost-category-name" maxlength="100" /></span>
      </div>
      <div class="field">
        <label><spring:message code="ui_col_cost_description" /></label>
        <span><span class="field-symbol"></span><span class="required-ind"></span><input type="text" size="50" id="cost-category-description" maxlength="200" /></span>
      </div>
      <div class="field category-display">
        <label><spring:message code="ui_col_cost_category_unit" /></label>
        <span><span class="field-symbol"></span><span class="required-ind"></span><input type="text" size="15" id="cost-category-units" maxlength="25" />&nbsp;<span class="small-hint">(ex: GHz, GB, etc)</span></span>
      </div>
      <div class="field category-display">
        <label><spring:message code="ui_col_cost_category_target_utilization" /></label>
        <span><span class="field-symbol"></span><span class="required-ind"></span><input type="text" size="3" maxlength="3" value="0" id="cost-category-target-utilization" class="decimal" />&nbsp;%</span>
      </div>
      <div class="field category-display" id="cost-category-labor-split-container">
        <label>Labor Split</label>
        <span><span class="field-symbol"></span><span class="required-ind"></span><span id="cost-category-labor-split"></span>&nbsp;%</span>
      </div>
      <div id="cost-category-children-container" class="table-wrapper">
          <div class="form-divider"></div>
          <div class="report-hint left"><spring:message code="ui_msg_labor_split_instructions" /></div>
          <div class="right" style="font-size:0.8em;"><a href="javascript:;" id="distribute-evenly-btn"><i class="fa fa-calculator"></i><spring:message code="ui_title_distribute_evenly" /></a></div>
          <table>
              <thead>
                  <tr>
                      <th><spring:message code="ui_col_child_category_name" /></th>
                      <th class="right"><spring:message code="ui_col_labor_split" /></th>
                  </tr>
              </thead>
              <tbody></tbody>
              <tfoot></tfoot>
          </table>
      </div>
    </div>
</div>

<div id="delete-cost-category-dialog" class="dialog">
    <div class="message-content"></div>
      
    <div class="dialog-loader-overlay">
        <div class="dialog-loader">
            <img src="${ajax_loader}" />
            <div><spring:message code="ui_dialog_delete_cost_category_loading" /></div>
        </div>
    </div>
    <div class="dialog-instruct"><spring:message code="ui_dialog_delete_cost_category_instruct" /></div>
      
    <div class="dialog-content">
      <div class="delete-msg"><i class="fa fa-warning"></i><spring:message code="ui_dialog_delete_warning" /></div>
      <div class="delete-item"><spring:message code="ui_title_to_be_deleted" /> <span id="delete-cost-name"></span></div>
    </div>
</div>