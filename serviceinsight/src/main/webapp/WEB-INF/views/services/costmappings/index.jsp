<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt_rt" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<spring:url value="/resources/images/ajax-loader.gif" var="ajax_loader" />
<spring:url value="/resources/js/cost-mappings.js" var="costs_mappings_js" />
<script src="${costs_mappings_js}" type="text/javascript"></script>

<input type="hidden" id="general-error-msg" value="<spring:message code="ui_dialog_general_validation_error" />" />
<input type="hidden" id="ok-cost-mapping-saved-msg" value="<spring:message code="ui_dialog_ok_mapping_saved" />" />
<input type="hidden" id="error-cost-mapping-quantity-zero" value="<spring:message code="ui_dialog_error_quantity_not_zero" />" />

<spring:url value="/services/costmappings" var="cost_mappings_url" />
<nav class="page-sub-nav">
  <a href="${cost_mappings_url}" class="active"><spring:message code="ui_title_cost_mappings" /></a>
</nav>

<section class="table-wrapper" id="cost-mappings-table">
  <div class="table-header">
    <div class="table-title"><spring:message code="ui_title_cost_mappings" /></div>
    <div class="table-links"></div>
  </div>
  
  <table>
    <thead>
      <tr>
        <th><spring:message code="ui_col_service_name" /></th>
        <th class="right"><spring:message code="ui_col_cost_categories" /></th>
      </tr>
    </thead>
    <tbody></tbody>
    <tfoot></tfoot>
  </table>
</section>

<div id="cost-mappings-dialog" class="dialog">
    <div class="message-content"></div>
      
    <div class="dialog-loader-overlay">
        <div class="dialog-loader">
            <img src="${ajax_loader}" />
            <div><spring:message code="ui_dialog_cost_mappings_save_loading" /></div>
        </div>
    </div>
    <div class="dialog-instruct"><spring:message code="ui_dialog_cost_mappings_instruct" /></div>
      
    <div class="dialog-content">
      <div class="field">
          <label class="short"><spring:message code="ui_col_service_name" /></label>
          <span><span class="field-symbol"></span><span class="required-ind"></span><span id="cost-mappings-service-name"></span></span>
      </div>
      
      <div class="form-divider"></div>
      
      <div class="inline-loader no-results" id="table-loader"><img src="${ajax_loader}" /> <spring:message code="ui_dialog_loading_mappings_message" /></div>
      
      <div class="table-wrapper small-bottom-margin" id="mappings-table-container">
            
            <table class="edit-table" id="modify-mappings-table">
              <thead>
                <tr>
                  <th><spring:message code="ui_col_cost_category" /></th>
                  <th><spring:message code="ui_col_cost_unit_type" /></th>
                  <th><spring:message code="ui_col_quantity" /></th>
                  <th><spring:message code="ui_col_cost_unit_cost" /></th>
                  <th>&nbsp;</th>
                </tr>
              </thead>
              <tbody>
                <tr id="clone-row" style="display:none;">
                  <td>
                    <span class="required-ind required"></span>
                    <select name="cost-asset-category">
                      <option value="" data-units="" data-unit-cost=""></option>
                      <c:forEach items="${expenseCategories}" var="expenseCategory">
                        <option value="${expenseCategory.id}" data-units="${expenseCategory.units}" data-unit-cost="${expenseCategory.unitCost}"><c:if test="${expenseCategory.parent ne null}">${expenseCategory.parent.name} - </c:if>${expenseCategory.name}</option>
                      </c:forEach>
                    </select>
                  </td>
                  <td class="unit-type"></td>
                  <td><span class="required-ind required"></span><input type="text" size="4" name="unit-quantity" /></td>
                  <td class="unit-cost"></td>
                  <td><a href="javascript:;" class="remove-row"><i class="fa fa-minus"></i><spring:message code="ui_title_remove_row" /></a></td>
                </tr>
              </tbody>
            </table>
            <div class="footer-link">
              <a href="javascript:;" id="add-row"><i class="fa fa-plus"></i><spring:message code="ui_title_add_another_row" /></a>
            </div>
        </div>
      
    </div>
</div>