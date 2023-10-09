<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt_rt"%>

<spring:url value="/resources/images/ajax-loader.gif" var="ajax_loader" />
<spring:url value="/resources/js/admin-service-align.js" var="align_js" />
<script src="${align_js}" type="text/javascript"></script>

<input type="hidden" id="general-error-msg" value="<spring:message code="validation_error_generic" />" />


<a href="javascript:;" class="popup-link" data-dialog="service-align-dialog">Re-Align a Service</a>


<div id="service-align-dialog" class="dialog">
    <div class="message-content"></div>
      
    <div class="dialog-loader-overlay">
        <div class="dialog-loader">
            <img src="${ajax_loader}" />
            <div>Saving...</div>
        </div>
    </div>
    <div class="dialog-instruct"><spring:message code="ui_dialog_general_instruct" /></div>
      
    <div class="delete-msg" style="font-size:0.8em;"><i class="fa fa-warning"></i><spring:message code="ui_dialog_service_align_warning" /></div>
      
    <div class="dialog-content">
        <div class="field">
          <label class="short">Device</label>
            <span class="field-symbol"></span><span class="required-ind required"></span><select id="align-device" style="width:500px;">
                <option value=""></option>
                <c:forEach items="${devices}" var="device">
                <option value="${device.id}">${device.description} (${device.partNumber})</option>
                </c:forEach>
            </select>
        </div>
        <div class="field">
          <label class="short"></label>
          <div class="value" style="font-size:0.9em;">
          <span class="field-symbol"></span><span class="required-ind"></span> <spring:message code="ui_dialog_service_align_hint" />
          </div>
        </div>
        <div class="field">
          <label class="short">New Service</label>
            <span class="field-symbol"></span><span class="required-ind required"></span><select id="align-service" style="width:500px;">
                <option value=""></option>
                <c:forEach items="${services}" var="service">
                <option value="${service.serviceId}">${service.name}</option>
                </c:forEach>
            </select>
        </div>
        <div class="field">
          <label class="short">Password</label>
            <span class="field-symbol"></span><span class="required-ind required"></span><input type="password" id="align-password" />
        </div>
        
        <div class="notice-msg" style="font-size:0.9em; font-weight:600; margin-top:40px;"><spring:message code="ui_dialog_service_align_notice" /></div>
    </div>
</div>