<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles"%>
<!DOCTYPE html>
<html>
    <head>
        <title><tiles:insertAttribute name="title" ignore="true" /></title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta http-equiv="X-UA-Compatible" content="IE=8; IE=9; IE=10; IE=11;">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <meta name="_csrf" content="${_csrf.token}"/>
        <meta name="_csrf_header" content="${_csrf.headerName}"/>

        <spring:url value="/resources/css/core.css" var="css_standard" />
        <spring:url value="/resources/css/fontawesome-all.min.css" var="font_awesome" />
        <spring:url value="/resources/css/jquery-ui.css" var="css_jquery_ui" />
        <spring:url value="/resources/js/jquery.js" var="js_jquery" />
        <spring:url value="/resources/js/jquery-ui.js" var="js_jquery_ui" />
        <spring:url value="/resources/js/moment.min.js" var="moment_js" />
        <spring:url value="/resources/js/html5shiv.js" var="shiv_js" />
        <spring:url value="/resources/js/respond.min.js" var="respond_js" />
        <spring:url value="/resources/js/accounting.js" var="accounting_js" />
        <spring:url value="/resources/js/core.js" var="core_js" />
        
        <!--[if lt IE 9]>
          <script src="${shiv_js}" type="text/javascript"></script>
        <![endif]-->
        
        <link rel="stylesheet" type="text/css" href="${font_awesome}"/>
        <link rel="stylesheet" type="text/css" href="${css_standard}"/>
        <link rel="stylesheet" type="text/css" href="${css_jquery_ui}" />
        
        <script src="${js_jquery}" type="text/javascript"></script>
        <script src="${js_jquery_ui}" type="text/javascript"></script>
        <script src="${moment_js}" type="text/javascript"></script>
        <script src="${accounting_js}" type="text/javascript"></script>
        <script src="${core_js}" type="text/javascript"></script>
        
        <!--[if lt IE 9]>
          <script src="${respond_js}" type="text/javascript"></script>
        <![endif]-->
    </head>
    
    <c:set value="" var="testsite" />
    <c:if test="${pageContext.request.serverName eq 'sitest.logicaliscloud.com'}">
        <c:set value="test-env" var="testsite" />
    </c:if>
    <body class="${testsite}">
        <c:set var="is_admin" value="hasRole('ROLE_ADMIN')" />
        <c:set var="is_manager" value="hasRole('ROLE_MANAGER')" />
        <div class="page-wrapper <tiles:insertAttribute name="title-class" ignore="true" />">
          <tiles:insertAttribute name="header" ignore="true" />
            
          <div class="title-bar">
            <div class="container">
		      <h1><tiles:insertAttribute name="title-display" ignore="true" /></h1>
		      <div class="fa <tiles:insertAttribute name="title-icon" ignore="true" />"></div>
		      <div class="test-env-message"><i class="fa fa-warning"></i><span>This is a Test Site</span><i class="fa fa-warning"></i></div>
		    </div>
		  </div>
            
          <spring:url value="/contracts" var="contracts_link" />
          <spring:url value="/customers" var="customers_link" />
          <spring:url value="/billing" var="billing_link" />
          <spring:url value="/reports" var="reports_link" />
          <spring:url value="/reports/revenue" var="revenue_reports_link" />
          <spring:url value="/costs" var="costs_link" />
          <spring:url value="/dashboard" var="dashboard_link" />
          <sec:authorize access="${is_admin}">
              <spring:url value="/settings/users" var="settings_link"/>
          </sec:authorize>
          <c:set var="current_uri" value="${requestScope['javax.servlet.forward.request_uri']}" />
          
          <div class="main-content container">
            <nav class="main-nav">
              <ul>
                <li <c:if test="${fn:containsIgnoreCase(current_uri,dashboard_link)}">class="active"</c:if>><a href="${dashboard_link}"><span class="icon"><i class="fas fa-tachometer-alt"></i></span>Dashboard</a></li>
                <li <c:if test="${fn:containsIgnoreCase(current_uri,contracts_link)}">class="active"</c:if>><a href="${contracts_link}"><span class="icon"><i class="far fa-file-alt"></i></span>SOWs</a></li>
                <li <c:if test="${fn:containsIgnoreCase(current_uri,billing_link)}">class="active"</c:if>><a href="${billing_link}"><span class="icon"><i class="fas fa-money-check-alt"></i></span>Billing</a></li>
                <li <c:if test="${fn:containsIgnoreCase(current_uri,reports_link)}">class="active"</c:if>><a href="${revenue_reports_link}"><span class="icon"><i class="fas fa-chart-line"></i></span>Reports</a></li>
                <sec:authorize access="${is_manager}">
                <li <c:if test="${fn:containsIgnoreCase(current_uri,costs_link)}">class="active"</c:if>><a href="${costs_link}"><span class="icon"><i class="fas fa-dollar-sign"></i></span>Costs</a></li>
                </sec:authorize>
                <li <c:if test="${fn:containsIgnoreCase(current_uri,customers_link)}">class="active"</c:if>><a href="${customers_link}"><span class="icon"><i class="fas fa-users"></i></span>Customers</a></li>
                <sec:authorize access="${is_admin}">
                    <li <c:if test="${fn:containsIgnoreCase(current_uri,settings_link)}">class="active"</c:if>><a href="${settings_link}"><span class="icon"><i class="fas fa-cog"></i></span>Settings</a></li>
                </sec:authorize>
              </ul>
            </nav>
            <div class="main-body">
              <tiles:insertAttribute name="body" />
            </div>
            <div class="push"></div>
          </div>
        </div>
        <tiles:insertAttribute name="footer" ignore="true" />
    </body>
</html>