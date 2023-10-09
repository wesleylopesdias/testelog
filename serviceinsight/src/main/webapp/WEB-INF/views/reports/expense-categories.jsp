<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<spring:url value="/resources/js/reports-expense-categories.js" var="reports_js" />
<spring:url value="/resources/js/highcharts.js" var="highcharts_js" />
<spring:url value="/resources/images/ajax-loader.gif" var="ajax_loader" />
<script src="${highcharts_js}" type="text/javascript"></script>
<script src="${reports_js}" type="text/javascript"></script>

<jsp:include page="sub-nav.jsp" />

<input type="hidden" id="push-applied-date" value="${param.ad}" />
<input type="hidden" id="push-id" value="${param.exid}" />
<input type="hidden" id="push-custid" value="${param.custid}" />

<section class="section-wrapper">
    <div class="section-header">
        <div class="section-title"><spring:message code="ui_title_filter_criteria" /></div>
    </div>
    <section class="sow-details filter-criteria">
      <div class="content-msg"></div>
      <div class="row">
        <div class="column-2">
          <div class="field">
            <label><spring:message code="ui_col_cost_category" /></label>
            <span>
              <select id="expense-category">
                <c:forEach items="${categories}" var="category">
                  <option value="${category.id}"><c:if test="${category.parent ne null}">${category.parent.name} - </c:if>${category.name}</option>
                </c:forEach>
              </select>
            </span>
          </div>
          <div class="field">
            <label><spring:message code="ui_col_customer" /></label>
            <span>
              <select id="report-customer">
                <option value=""><spring:message code="ui_reports_non_customer" /></option>
                <c:forEach items="${customers}" var="customer">
                <c:if test="${customer.siEnabled eq true}"><option value="${customer.id}">${customer.name}</option></c:if>
                </c:forEach>
              </select>
            </span>
          </div>
        </div>
        <div class="column-2">
          <div class="field">
            <label><spring:message code="ui_col_date_range" /></label>
            <span>
              <select id="report-date-range">
                <option value=""><spring:message code="ui_reports_value_all_time" /></option>
              </select>
            </span>
          </div>
        </div>
      </div>
      <div class="row">
        <div class="button-row"><a href="javascript:;" class="cta-btn" id="run-report-btn"><spring:message code="ui_btn_run_report" /></a></div>
      </div>
    </section>
</section>

<div id="report-loader" class="no-results" style="display:none;"><img src="${ajax_loader}" style="margin-right:8px;" /><spring:message code="ui_msg_loading_reporting_data" /></div>
<section class="table-wrapper section-spacer report-section" style="display:none;">
    <div class="right" style="margin-bottom:15px; padding-bottom:6px; border-bottom:1px dotted #ccc;"><a href="#" target="_blank" id="download-excel"><i class="fa fa-download"></i><spring:message code="ui_title_export_excel" /></a></div>
    <div class="clearer"></div>
    <div class="row">
        <div class="column-2">
          <div class="report-summary-box">
              <div class="table-header">
	            <div class="table-title">
	              <spring:message code="ui_title_report_cost_summary" />
	            </div>
	          </div>
	          <div class="report-details-box">
	              <div class="field">
                      <label><spring:message code="ui_col_cost_category" /></label>
                      <span class="lined-value" id="current-category"></span>
                  </div>
	              <div class="field">
                      <label><spring:message code="ui_col_customer" /></label>
                      <span class="lined-value" id="current-customer"></span>
                  </div>
                  <div class="field">
                      <label><spring:message code="ui_col_month" /></label>
                      <span class="lined-value" id="current-month"></span>
                  </div>
		          <div class="field">
		              <label><spring:message code="ui_col_total_devices" /></label>
		              <span class="lined-value" id="total-devices"></span>
		          </div>
		          <div class="field">
		              <label><spring:message code="ui_col_average_device_cost" /></label>
		              <span class="lined-value" id="device-unit-cost"></span>
		          </div>
	          </div>
          </div>
          
          <div class="table-wrapper" style="margin-bottom:20px;">
	          <div class="table-header">
	            <div class="table-title">
	              <spring:message code="ui_title_mapped_devices" />
	            </div>
	            <div class="table-links"></div>
	          </div>
	          
	          <table id="report-devices-table">
	            <thead>
	              <tr>
	                <th><spring:message code="ui_col_part_number" /></th>
	                <th><spring:message code="ui_col_description" /></th>
	              </tr>
	            </thead>
	            <tbody></tbody>
	          </table>
          </div>
          
		  <div class="table-header">
		    <div class="table-title">
		      <spring:message code="ui_title_cost_details" />
		    </div>
		    <div class="table-links"></div>
		  </div>
		  
		  <table id="report-data-table">
		    <thead>
		      <tr>
		        <th><spring:message code="ui_col_asset_expense" /></th>
		        <th class="center"><spring:message code="ui_col_cost_date" /></th>
		        <th><spring:message code="ui_col_name" /></th>
		        <th class="right"><spring:message code="ui_col_cost_amount" /></th>
		      </tr>
		    </thead>
		    <tbody></tbody>
		    <tfoot></tfoot>
		  </table>
		</div>
		<div class="column-2">
		  <div class="chart" style="padding-left:2%;"><spring:message code="ui_title_category_history" />
		      <div id="chart-one" style="display:inline-block; width:100%; height:300px;"></div>
		      <div id="chart-two" style="display:inline-block; width:100%; height:300px;"></div>
		  </div>
		</div>
    </div>
</section>