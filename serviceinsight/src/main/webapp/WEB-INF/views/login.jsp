<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<spring:url value="/resources/images/ajax-loader.gif" var="ajax_loader" />
<spring:url value="/resources/js/login.js" var="login_js" />
<script src="${login_js}" type="text/javascript"></script>

<input type="hidden" id="error-enter-email-msg" value="<spring:message code="ui_error_reset_password_enter_email" />" />
<input type="hidden" id="error-enter-valid-email-msg" value="<spring:message code="ui_error_reset_password_invalid_email_address" />" />
<input type="hidden" id="ok-email-sent-msg" value="<spring:message code="ui_ok_reset_password" />" />

<c:if test="${pageContext.request.serverName eq 'sitest.logicaliscloud.com'}">
   <div class="notice-msg"><i class="fa fa-warning"></i><spring:message code="login_test_site_msg" /></div>
   <style type="text/css">header { background:#313131 url(/si/resources/images/test.png); }</style>
</c:if>

<div class="login-box">
  <h3><i class="fa fa-lock"></i><spring:message code="authentication_header" /></h3>
  
    <c:if test="${not empty SPRING_SECURITY_LAST_EXCEPTION.message}">
      <div class="error-msg">
        <i class="fa fa-times-circle"></i>
          <spring:message code="security_login_unsuccessful" />
          <c:out value="${SPRING_SECURITY_LAST_EXCEPTION.message}" />
      </div>
    </c:if>
    
    <%-- <spring:message code="security_login_message" /> --%>
    <c:if test="${param.logout != null}">
      <div class="notice-msg">
        <i class="fa fa-info-circle"></i><spring:message code="logout_message"/>
      </div>
    </c:if>
    
    <spring:url value="/login" var="form_url" />
    <form name="f" action="${fn:escapeXml(form_url)}" method="POST">
      <div class="field">
        <label for="j_username">
          <spring:message code="security_login_form_name" />
        </label>
        <span><input id="j_username" type="text" name="username" /></span>
        <spring:message code="security_login_form_name_message" var="name_msg" />
      </div>
      <div class="field">
        <label for="j_password">
          <spring:message code="security_login_form_password" />
        </label>
        <span><input id="j_password" type="password" name="password" /></span>
        <spring:message code="security_login_form_password_message" var="pwd_msg" />
      </div>
      <div class="field-checkbox">
        <label><input type="checkbox" name="_spring_security_remember_me" /><spring:message code="security_login_form_rememberme_message" /></label>
      </div>
      <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
      <div class="submit-row">
        <a href="javascript:;" class="popup-link forgot-password" data-dialog="reset-password-dialog">Forgot Your Password?</a>
        <spring:message code="button_login" var="submit_label" />
        <button class="cta-btn" id="login-btn">${fn:escapeXml(submit_label)}<i class="fa fa-arrow-circle-right"></i></button>
      </div>
    </form>
</div>

<div id="reset-password-dialog" class="dialog">
    <div class="message-content"></div>
      
    <div class="dialog-loader-overlay">
        <div class="dialog-loader">
            <img src="${ajax_loader}" />
            <div><spring:message code="ui_reset_password_loading" /></div>
        </div>
    </div>
    <div class="dialog-instruct"><spring:message code="ui_dialog_reset_password_instruct" /></div>
      
    <div class="dialog-content">
        <div class="field">
          <label>Email Address</label>
          <span><span class="required-ind required"></span><input type="text" size="40" id="reset-password-email" /></span>
        </div>
    </div>
</div>

