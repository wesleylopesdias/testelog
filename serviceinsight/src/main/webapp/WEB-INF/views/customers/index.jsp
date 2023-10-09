<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt_rt"%>

<spring:url value="/resources/images/ajax-loader.gif" var="ajax_loader" />
<spring:url value="/resources/js/customers.js" var="customers_js" />
<script src="${customers_js}" type="text/javascript"></script>

<input type="hidden" id="add-customer-msg" value="<spring:message code="ui_ok_new_customer" />" />
<input type="hidden" id="update-customer-msg" value="<spring:message code="ui_ok_update_customer" />" />
<input type="hidden" id="general-error-msg" value="<spring:message code="validation_error_generic" />" />

<section class="table-wrapper" id="customers">
  <div class="table-header">
    <div class="table-title">
      <spring:message code="ui_title_customers" />
    </div>
    <div class="table-links">
      <a href="javascript:;" class="update-customer popup-link" data-dialog="add-customer-dialog" data-username="add"><i class="fa fa-plus-square"></i><spring:message code="ui_title_add_customer" /></a>
      <sec:authorize access="hasRole('ROLE_ADMIN')">
        <p/>
        <a href="${customerSyncLink}" class="btn-link pull-right" id="btn-mfa-auth" style="margin-top: 5px;"><i class="fa fa-sync"></i><spring:message code="ui_label_sync_csp_tenant_ids" /></a>
        <div class="clearer"></div>
        <p/>
        <a href="${azureMfaLink}" class="btn-link pull-right" id="btn-mfa-auth" style="margin-top: 5px;"><i class="fa fa-sync"></i><spring:message code="ui_label_sync_azure_customers" /></a>
        <div class="clearer"></div>
        <p/>
        <a href="${azureRelationshipRequestLink}" class="btn-link pull-right" id="btn-mfa-auth" style="margin-top: 5px;"><i class="fa fa-sync"></i><spring:message code="ui_label_sync_azure_relationship_request" /></a>
        <c:if test="${not empty requestURL}">
            <div id="rrdeiv">
                <span><c:out value="${requestURL}"/></span>
            </div>
        </c:if>
        <div class="clearer"></div>
        <span style="font-size:smaller;">(tests API functionality, only)</span>
      </sec:authorize>
    </div>
  </div>
  
  <section class="tabs">
    <a href="javascript:;" class="selected table-tab" data-view="active"><spring:message code="ui_title_active" /></a>
    <a href="javascript:;" class="table-tab" data-view="archived"><i class="fa fa-archive"></i><spring:message code="ui_title_archived" /></a>
  </section>
  
  <spring:url value="/contracts?cid=" var="contract" />
  <table>
    <thead>
      <tr>
        <th><spring:message code="ui_col_customer_name" /></th>
        <th><spring:message code="ui_col_csp_customer_name" /></th>
        <th><spring:message code="ui_col_azure_customer_id" /></th>
        <th><spring:message code="ui_col_ae" /></th>
        <th><spring:message code="ui_col_epe" /></th>
        <th><spring:message code="ui_col_sdm" /></th>
        <th><spring:message code="ui_col_bsc" /></th>
        <th class="right"><spring:message code="ui_col_contracts" /></th>
        <th class="center"><spring:message code="ui_col_options" /></th>
      </tr>
    </thead>
    <tbody></tbody>
  </table>
</section>


<div id="add-customer-dialog" class="dialog">
    <div class="message-content"></div>
      
    <div class="dialog-loader-overlay">
        <div class="dialog-loader">
            <img src="${ajax_loader}" />
            <div><spring:message code="ui_customer_loading" /></div>
        </div>
    </div>
    <div class="dialog-instruct"><spring:message code="ui_dialog_general_instruct" /></div>
      
    <div class="dialog-content">
        <div class="field">
          <label><spring:message code="ui_col_customer_name" /></label>
          <div class="value"><span class="field-symbol"></span><span class="required-ind required"></span><select id="customer-list" class="add customer-dropdown-list"></select>
            <div class="small-hint indent add">(<spring:message code="ui_msg_cant_find_customer_hint" />)</div>
            <span class="edit" id="customer-name" style="display:none;"></span>
          </div>
        </div>
        <div class="field">
          <label><spring:message code="ui_col_sdm" /></label>
          <div class="value"><span class="field-symbol"></span><span class="required-ind"></span><input type="text" size="40" id="customer-sdms" />
            <div class="value-second-row"></div>
          </div>
        </div>
        <div class="field">
          <label><spring:message code="ui_col_bsc" /></label>
          <div class="value"><span class="field-symbol"></span><span class="required-ind"></span><input type="text" size="40" id="customer-bscs" />
            <div class="value-second-row"></div>
          </div>
        </div>
        <div class="field">
          <label><spring:message code="ui_col_ae" /></label>
          <div class="value"><span class="field-symbol"></span><span class="required-ind"></span><input type="text" size="40" id="customer-ae" />
          </div>
        </div>
        <div class="field">
          <label><spring:message code="ui_col_epe" /></label>
          <div class="value"><span class="field-symbol"></span><span class="required-ind"></span><input type="text" size="40" id="customer-epe" />
          </div>
        </div>
        <div class="field">
          <label><spring:message code="ui_col_csp_customer_name" /></label>
          <span><span class="field-symbol"></span><span class="required-ind"></span><input type="text" id="customer-alt-name" size="40" /></span>
        </div>
        <div class="field">
          <label><spring:message code="ui_col_azure_customer_id" /></label>
          <span><span class="field-symbol"></span><span class="required-ind"></span><input type="text" id="customer-azure-id" size="40" /></span>
        </div>
        <div class="field">
          <label>&nbsp;</label>
          <span><span class="field-symbol"></span><span class="required-ind"></span><input type="checkbox" id="customer-archived" />&nbsp;&nbsp;<spring:message code="ui_title_archive_customer" /></span>
        </div>
    </div>
</div>