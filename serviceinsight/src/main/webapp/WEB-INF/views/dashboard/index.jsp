<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<style type="text/css">.selectBox-options li a { font-size:0.9em; }</style>

<spring:url value="/resources/images/ajax-loader.gif" var="ajax_loader" />
<spring:url value="/resources/js/highcharts.js" var="highcharts_js" />
<spring:url value="/resources/js/dashboard.js" var="dashboard_js" />
<script src="${highcharts_js}" type="text/javascript"></script>
<script src="${dashboard_js}" type="text/javascript"></script>

<section class="dashboard-filters">
  <label><spring:message code="ui_col_viewing" /></label>
  <select id="currently-viewing"></select>
</section>

<section class="column-60">
  <section id="contract-stats">
	  <div class="stat-box">
	    <div class="stat-top">
	      <span class="stat-icon"><i class="far fa-file-alt"></i></span>
	      <span class="stat-label"><spring:message code="ui_title_ms_contracts" /></span>
	    </div>
	    <div class="stat-bottom" id="tile-ms-contract-count">--</div>
	  </div>
	  <div class="stat-box">
	    <div class="stat-top">
	      <span class="stat-icon"><i class="far fa-file-alt"></i></span>
	      <span class="stat-label"><spring:message code="ui_title_cloud_contracts" /></span>
	    </div>
	    <div class="stat-bottom" id="tile-cloud-contract-count">--</div>
	  </div>
	  <div class="stat-box">
	    <div class="stat-top">
	      <span class="stat-icon"><i class="far fa-file-alt"></i></span>
	      <span class="stat-label"><spring:message code="ui_title_ms_cloud_contracts" /></span>
	    </div>
	    <div class="stat-bottom" id="tile-ms-cloud-contract-count">--</div>
	  </div>
  </section>
  <div class="section-spacer"></div>
  <section class="table-wrapper" id="recent-sows">
    <div class="table-header">
      <div class="table-title">
        <i class="far fa-file-alt"></i><spring:message code="ui_title_recent_contracts" />
      </div>
      <div class="table-links"></div>
    </div>
  
    <table>
      <thead>
        <tr>
          <th><spring:message code="ui_col_customer" /></th>
          <th class="center"><spring:message code="ui_col_job_number" /></th>
          <th class="center"><spring:message code="ui_col_type" /></th>
          <th class="right"><spring:message code="ui_col_start_date" /></th>
          <th class="right"><span class="current-month"></span> <spring:message code="ui_col_mrc" /></th>
        </tr>
      </thead>
      <tbody></tbody>
    </table>
  </section>
  
  <section class="table-wrapper" id="top-customers">
    <div class="table-header">
      <div class="table-title">
        <i class="fa fa-users"></i><spring:message code="ui_title_top_customers" />
      </div>
      <div class="table-links"></div>
    </div>
  
    <table>
      <thead>
        <tr>
          <th><spring:message code="ui_col_customer" /></th>
          <th class="center"><spring:message code="ui_col_sows" /></th>
          <th class="right"><span class="current-month"></span> <spring:message code="ui_col_mrc" /></th>
        </tr>
      </thead>
      <tbody></tbody>
    </table>
  </section>
</section><section class="column-40">
  <section id="revenue-stats">
	  <div class="stat-box">
	    <div class="stat-top">
	      <span class="stat-icon"><i class="fas fa-dollar-sign"></i></span>
	      <span class="stat-label"><spring:message code="ui_title_ms" /> <span class="current-month"></span> <spring:message code="ui_title_revenue" /></span>
	    </div>
	    <div class="stat-bottom" id="tile-ms-revenue">--</div>
	  </div>
	  <div class="stat-box">
	    <div class="stat-top">
	      <span class="stat-icon"><i class="fas fa-dollar-sign"></i></span>
	      <span class="stat-label"><spring:message code="ui_title_cloud" /> <span class="current-month"></span> <spring:message code="ui_title_revenue" /></span>
	    </div>
	    <div class="stat-bottom" id="tile-cloud-revenue">--</div>
	  </div>
  </section>
  <div class="section-spacer"></div>
  
  <section id="chart"></section>
  
  <div class="section-spacer"></div>
  
  <section class="table-wrapper" id="top-ms-services">
    <div class="table-header">
      <div class="table-title full">
        <i class="fas fa-dollar-sign"></i><spring:message code="ui_title_top_ms" />
      </div>
    </div>
  
    <table>
      <thead>
        <tr>
          <th>Service</th>
          <th class="right"><span class="current-month"></span> <spring:message code="ui_col_mrc" /></th>
        </tr>
      </thead>
      <tbody></tbody>
    </table>
  </section>
  
  <section class="table-wrapper" id="top-cloud-services">
    <div class="table-header">
      <div class="table-title full">
        <i class="fas fa-dollar-sign"></i><spring:message code="ui_title_top_cloud" />
      </div>
    </div>
  
    <table>
      <thead>
        <tr>
          <th>Service</th>
          <th class="right"><span class="current-month"></span> <spring:message code="ui_col_mrc" /></th>
        </tr>
      </thead>
      <tbody></tbody>
    </table>
  </section>
</section>