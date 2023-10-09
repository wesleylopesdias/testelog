<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<spring:url value="/resources/images/ajax-loader.gif" var="ajax_loader" />
<spring:url value="/resources/js/passy.js" var="passy_js" />
<spring:url value="/resources/js/reset-password.js" var="reset_js" />
<script src="${passy_js}" type="text/javascript"></script>
<script src="${reset_js}" type="text/javascript"></script>

<input type="hidden" id="general-error-msg" value="<spring:message code="ui_dialog_general_validation_error" />" />
<input type="hidden" id="too-short-error-msg" value="<spring:message code="ui_error_reset_password_too_short" />" />
<input type="hidden" id="no-match-error-msg" value="<spring:message code="ui_error_reset_password_doesnt_match_confirm" />" />

<c:choose>
  <c:when test="${error ne null}">
    <div class="error-msg"><i class="fa fa-times-circle"></i>${error.message}</div>
  </c:when>
  <c:otherwise>
	<div class="half-box">
	  <div class="content-box" id="reset-password-box">
	    <h1>Reset Application Password</h1>
	    <div class="content-msg"></div>
	    <section class="content-instruct"><spring:message code="ui_dialog_external_reset_instruct" /></section>
	    <section class="content-body">
		    <form name="f" action="${fn:escapeXml(form_url)}" method="POST">
		      <div class="field">
	            <label for="new_password"><spring:message code="ui_form_new_password" /></label>
	            <span><input id="new-password" type="password" name="password" /><span class="password-strength"></span></span>
	          </div>
		      <div class="field">
		        <label for="new_password"><spring:message code="ui_form_confirm_password" /></label>
		        <span><input id="confirm-password" type="password" name="password" /></span>
		      </div>
		      <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
		      <br />
		      <div class="submit-row">
		        <spring:message code="ui_form_reset_password" var="submit_label" />
		        <span class="inline-loader"><img src="${ajax_loader}" /></span><button class="cta-btn" id="reset-password-btn">${fn:escapeXml(submit_label)}</button>
		      </div>
		    </form>
	    </section>
	  </div>  
	</div>
  </c:otherwise>
</c:choose>