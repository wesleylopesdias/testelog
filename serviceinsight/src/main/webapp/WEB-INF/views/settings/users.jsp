<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt_rt"%>

<spring:url value="/resources/images/ajax-loader.gif" var="ajax_loader" />
<spring:url value="/resources/js/users.js" var="users_js" />
<script src="${users_js}" type="text/javascript"></script>

<input type="hidden" id="add-user-msg" value="<spring:message code="ui_ok_new_user" />" />
<input type="hidden" id="update-user-msg" value="<spring:message code="ui_ok_update_user" />" />
<input type="hidden" id="general-error-msg" value="<spring:message code="validation_error_generic" />" />
<input type="hidden" id="username-error-msg" value="<spring:message code="validation_error_user_name" />" />
<input type="hidden" id="password-error-msg" value="<spring:message code="validation_error_user_password" />" />
<input type="hidden" id="profile-error-msg" value="<spring:message code="validation_error_user_profile" />" />
<input type="hidden" id="disable-user-label" value="<spring:message code="ui_col_disable" />" />
<input type="hidden" id="enable-user-label" value="<spring:message code="ui_col_enable" />" />
<input type="hidden" id="delete-user-label" value="<spring:message code="ui_col_delete" />" />
<input type="hidden" id="current-user" value="${currentUser}"/>

<jsp:include page="sub-nav.jsp" />

<section class="table-wrapper" id="users">
  <div class="table-header">
    <div class="table-title">
      <spring:message code="ui_title_users" />
    </div>
    <div class="table-links">
      <a href="javascript:;" class="update-user popup-link" data-dialog="add-user-dialog" data-id="add"><i class="fa fa-plus-square"></i><spring:message code="ui_title_add_user" /></a>
    </div>
  </div>
  
  <section class="tabs">
    <a href="javascript:;" class="selected table-tab" data-view="active"><spring:message code="ui_title_active" /></a>
    <a href="javascript:;" class="table-tab" data-view="disabled"><i class="fa fa-user-times"></i><spring:message code="ui_title_disabled" /></a>
  </section>
  
  <table>
    <thead>
      <tr>
        <th><spring:message code="ui_col_user_name" /></th>
        <th><spring:message code="ui_col_username" /></th>
        <th><spring:message code="ui_col_title" /></th>
        <th><spring:message code="ui_col_roles" /></th>
        <th class="right"><spring:message code="ui_col_options" /></th>
      </tr>
    </thead>
    <tbody></tbody>
  </table>
</section>

<div id="add-user-dialog" class="dialog">
    <div class="message-content"></div>
      
    <div class="dialog-loader-overlay">
        <div class="dialog-loader">
            <img src="${ajax_loader}" />
            <div><spring:message code="ui_user_loading" /></div>
        </div>
    </div>
    <div class="dialog-instruct"><spring:message code="ui_dialog_general_instruct" /></div>
      
    <div class="dialog-content">
    	<div class="field">
          <label><spring:message code="ui_col_user_name" /></label>
          <div class="value">
            <span class="field-symbol"></span><span class="required-ind required"></span><input type="text" id="name" size="40" />
          </div>
        </div>
        <div class="field">
          <label><spring:message code="ui_col_title" /></label>
          <div class="value">
            <span class="field-symbol"></span><span class="required-ind"></span><input type="text" id="title" size="40" />
          </div>
        </div>
        <div class="field">
          <label><spring:message code="ui_col_username" /></label>
          <div class="value">
            <span class="field-symbol"></span><span class="required-ind required"></span><input type="text" id="username" size="40" />
          </div>
        </div>
        <div class="field add">
          <label><spring:message code="ui_col_password" /></label>
          <div class="value">
            <span class="field-symbol"></span><span class="required-ind required"></span><input type="password" id="password" size="40"/>
          </div>
        </div>
        <div class="field">
          <label><spring:message code="ui_col_roles" /></label>
          <div class="value">
            <span class="field-symbol"></span><span class="required-ind required"></span>
            <select class="roles-list" id="roles" multiple="multiple">
            </select>
          </div>
        </div>
    </div>
</div>

<div id="disable-user-dialog" class="dialog">
    <div class="message-content"></div>
      
    <div class="dialog-loader-overlay">
        <div class="dialog-loader">
            <img src="${ajax_loader}" />
            <div><spring:message code="ui_disable_user_loading" /></div>
        </div>
    </div>
    <div class="dialog-instruct"><spring:message code="ui_disable_user_instruct" /></div>
      
    <div class="dialog-content">
      <div class="delete-item"><spring:message code="ui_title_to_be_disabled" /> <span id="disable-username"></span></div>
    </div>
</div>

<div id="enable-user-dialog" class="dialog">
    <div class="message-content"></div>
      
    <div class="dialog-loader-overlay">
        <div class="dialog-loader">
            <img src="${ajax_loader}" />
            <div><spring:message code="ui_enable_user_loading" /></div>
        </div>
    </div>
    <div class="dialog-instruct"><spring:message code="ui_enable_user_instruct" /></div>
      
    <div class="dialog-content">
      <div class="delete-item"><spring:message code="ui_title_to_be_enabled" /> <span id="enable-username"></span></div>
    </div>
</div>

<div id="delete-user-dialog" class="dialog">
    <div class="message-content"></div>
      
    <div class="dialog-loader-overlay">
        <div class="dialog-loader">
            <img src="${ajax_loader}" />
            <div><spring:message code="ui_delete_user_loading" /></div>
        </div>
    </div>
    <div class="dialog-instruct"><spring:message code="ui_delete_user_instruct" /></div>
      
    <div class="dialog-content">
      <div class="delete-msg"><i class="fa fa-warning"></i><spring:message code="ui_delete_user_warning" /></div>
      <div class="delete-item"><spring:message code="ui_title_to_be_deleted" /> <span id="delete-username"></span></div>
    </div>
</div>

<div id="roles-template" style="display:none">
    <select id="roles-template-select">
        <c:forEach items="${roles}" var="role">
            <option id="${role}" value="${role}">${role.description}</option>
        </c:forEach>
    </select>
</div>