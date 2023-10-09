<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt_rt" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<spring:url value="/resources/images/ajax-loader.gif" var="ajax_loader" />
<spring:url value="/resources/js/highcharts.js" var="highcharts_js" />
<spring:url value="/resources/js/cost-analysis.js" var="costs_js" />
<script src="${highcharts_js}" type="text/javascript"></script>
<script src="${costs_js}" type="text/javascript"></script>

<input type="hidden" id="current-viewing-input-date" value="" />

<c:set var="is_admin" value="hasRole('ROLE_ADMIN')" />
<sec:authorize access="${is_admin}">
    <input type="hidden" id="is-admin" value="true" />
</sec:authorize>

<jsp:include page="sub-nav.jsp" />

<section class="contract-viewing costs-viewing">
    <div class="row">
        <div class="column-3 bottom"><span id="current-view-prev-month"><a href="#">&laquo;May</a></span></div>
        <div class="column-3 center current-viewing-date">
            <span>
                <select id="current-viewing-date">
                    <option value="#"><spring:message code="ui_date_range_all_time" /></option>
                </select>
            </span>
            <div class="current-viewing-date-range"><!-- not needed --></div>
        </div>
        <div class="column-3 text-right bottom"><span id="current-view-next-month"><a href="#">Jul&raquo;</a></span></div>
    </div>
</section>

<section id="costs-table" class="table-wrapper">
    <div class="row-spaced">
        <div class="table-header">
            <div class="table-title" style="width:100%;">
                <a id="cost-summary-link" href="javascript:;" style="background-color:#ccc;"><spring:message code="ui_title_cost_analysis_summary" /></a>
                <span>/</span>
                <a id="cost-customer-link" href="javascript:;"><spring:message code="ui_title_cost_analysis_by_customer" /></a>
                <span>/</span>
                <a id="cost-expense-category-link" href="javascript:;"><spring:message code="ui_title_cost_analysis_by_expense_category" /></a>
            </div>
            <div class="table-links"></div>
        </div>
        <div id="cost-summary-table-div">
            <table>
                <thead></thead>
                <tbody></tbody>
                <tfoot></tfoot>
            </table>
        </div>
        <div id="cost-customer-table-div" style="display:none;">
            <table>
                <thead><tr><th>Customer</th></tr></thead>
                <tbody></tbody>
                <tfoot></tfoot>
            </table>
        </div>
        <div id="cost-expense-category-table-div" style="display:none;">
            <table>
                <thead><tr><th>Exp Cat</th></tr></thead>
                <tbody></tbody>
                <tfoot></tfoot>
            </table>
        </div>
    </div>
    <div class="row-spaced">
        <div>
            <section id="chart" style="height: 500px;"></section>
        </div>
    </div>
</section>

<!-- popup dialog for cost details ("tabs" or radio button for filter on Customer and filter on cost category) -->
<div id="cost-dialog" class="dialog">
    <div class="dialog-content">
    </div>
</div>
<!-- popup dialog for labor-cost details (filter on Customer) -->
<div id="labor-cost-dialog" class="dialog">
    <div class="dialog-content">
    </div>
</div>
