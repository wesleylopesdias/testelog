<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt_rt"%>

<spring:url value="/resources/images/ajax-loader.gif" var="ajax_loader" />
<spring:url value="/resources/js/m365-pricing-list.js" var="m365_js" />
<script src="${m365_js}" type="text/javascript"></script>

<jsp:include page="sub-nav.jsp" />

<c:if test="${not empty reported_message}">
    <div id="id-imported-msg" style="margin-top: 20px;">
        <span class="notice-msg"><c:out value="${reported_message}" /></span>
    </div>
</c:if>

<section class="table-wrapper" id="m365-pricing">
  <div class="table-header">
    <div class="table-title">
      M365
    </div>
    <div class="table-links">
      <a href="javascript:;" class="upload-popup-link popup-link" data-dialog="upload-dialog" data-id="add"><i class="fa fa-plus-square"></i>Upload M365 Pricesheet</a>
      <a href="javascript:;" class="upload-popup-link popup-link" data-dialog="import-dialog" data-id="add"><i class="fa fa-plus-square"></i>Import M365 NCE Pricesheet</a>
    </div>
  </div>
  
  <table>
    <thead>
      <tr>
        <th>Date</th>
        <th>Type</th>
        <th>Products</th>
        <th class="right"><spring:message code="ui_col_options" /></th>
      </tr>
    </thead>
    <tbody></tbody>
  </table>
</section>

<spring:url value="/settings/microsoftpricing/import" var="upload_url" />
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
            <input type="hidden" name="type" value="M365" />
            <div class="field">
              <label class="short"><spring:message code="ui_col_date" /></label>
              <span><span class="field-symbol"></span><span class="required-ind required"></span><input type="text" size="10" class="month-datepicker" maxlength="10" placeholder="Date" name="date" id="upload-date" /></span>
            </div>
            <div class="field">
              <label class="short"><spring:message code="ui_col_excel_file" /></label>
              <span><span class="field-symbol"></span><span class="required-ind required"></span><input type="file" name="templatefile" id="template-file" /></span>
            </div>
            <%-- <input type="submit" value="Submit" id="submit-btn"/> --%>
        </form>
    </div>
</div>
<spring:url value="/settings/microsoftncepricing/import" var="import_url" />
<div id="import-dialog" class="dialog">
    <div class="message-content"></div>
    <div class="dialog-instruct"><spring:message code="ui_dialog_pricing_import_instruct" /></div>
    <div class="dialog-content">
        <form id="import-form" action="${import_url}" method="post">
        </form>
    </div>
</div>
