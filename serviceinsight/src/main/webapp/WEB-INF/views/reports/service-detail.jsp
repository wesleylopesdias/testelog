<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<spring:url value="/resources/js/reports-service-detail.js" var="reports_js" />
<script src="${reports_js}" type="text/javascript"></script>

<input type="hidden" id="msg-error-month-required" value="<spring:message code="ui_error_month_required" />" />
<input type="hidden" id="msg-error-service-required" value="<spring:message code="ui_error_service_required" />" />
<input type="hidden" id="msg-error-device-required" value="<spring:message code="ui_error_device_required" />" />

<jsp:include page="sub-nav.jsp" />

<section class="section-wrapper">
    <div class="section-header">
        <div class="section-title"><spring:message code="ui_title_filter_criteria" /></div>
    </div>
    <section class="sow-details filter-criteria">
        <div class="content-msg"></div>
        <div class="row">
            <div class="column-1">
                <div class="field">
                    <input type="radio" name="service_or_device" id="sel-services" value="services" checked="checked"/><label for="sel-services" style="width:70px;">Services</label>
                    <input type="radio" name="service_or_device" id="sel-devices" value="devices"/><label for="sel-devices">Devices</label>
                    <span id="span-services-list" style="color:#000">
                        <label class="top" style="width:100px;"><spring:message code="ui_col_service" /></label>
                        <select id="report-service">
                            <option value="">-- Select --</option>
                            <c:forEach items="${services}" var="service">
                                <c:choose>
                                    <c:when test="${null ne service.businessModel}">
                                        <c:set var="service_practice" value="${service.businessModel}"/>
                                    </c:when>
                                    <c:otherwise>
                                        <c:set var="service_practice" value="Other"/>
                                    </c:otherwise>
                                </c:choose>
                                <option value="${service.ospId}" class="${service_practice}">${service.name}</option>
                            </c:forEach>
                        </select>
                    </span>
                    <span id="span-devices-list" style="color:#000;display:none;">
                        <label class="top" style="width:100px;"><spring:message code="ui_col_device" /></label>
                        <select id="report-device">
                            <option value="">-- Select --</option>
                            <c:forEach items="${devices}" var="device">
                                <c:set var="deviceParts" value="${fn:split(device,'|')}"/>
                                <option value="${deviceParts[0]}">${deviceParts[1]} / ${deviceParts[2]}</option>
                            </c:forEach>
                        </select>
                    </span>
                </div>
            </div>
        </div>
        <div class="row">
            <div class="column-1">
                <div class="field">
                    <label style="width:10%;"><spring:message code="ui_col_date_range" /></label>
                    <select id="report-month" style="width:10%;">
                        <option value=""><spring:message code="ui_reports_value_all_time" /></option>
                    </select>
                </div>
            </div>
        </div>
        <div class="row">
            <div class="button-row"><a href="javascript:;" class="cta-btn" id="run-report-btn"><spring:message code="ui_btn_run_report" /></a></div>
        </div>
    </section>
</section>

<section class="table-wrapper section-spacer report-section hidden">
    <div class="table-header">
        <div class="table-title">
            <spring:message code="ui_title_servicedetails" />
        </div>
        <div class="table-links">
            <a href="#" target="_blank" id="download-excel"><i class="fa fa-download"></i><spring:message code="ui_title_export_excel" /></a>
        </div>
    </div>

    <div id="data-headers">
        <span id="id-data-date"></span><br/>
        <span id="id-data-service"></span>
    </div>
    <table id="report-usage-data-table" class="small-table">
        <thead>
            <tr>
                <th><spring:message code="ui_col_sd_customer" /></th>
                <th><spring:message code="ui_col_sd_jobnumber" /></th>
                <th><spring:message code="ui_col_sd_contractname" /></th>
                <th><spring:message code="ui_col_sd_sdm" /></th>
                <th><spring:message code="ui_col_service" /></th>
                <th><spring:message code="ui_col_sd_devicename" /></th>
                <th><spring:message code="ui_col_sd_devicepartno" /></th>
                <th class="right"><spring:message code="ui_col_sd_quantity" /></th>
                <th class="right"><spring:message code="ui_col_sd_onetime" /></th>
                <th class="right"><spring:message code="ui_col_sd_recurring" /></th>
                <th class="right"><spring:message code="ui_col_sd_revtotal" /></th>
                <th width="6%"><spring:message code="ui_col_sd_startdate" /></th>
                <th width="6%"><spring:message code="ui_col_sd_enddate" /></th>
            </tr>
        </thead>
        <tbody></tbody>
        <tfoot></tfoot>
    </table>
</section>
