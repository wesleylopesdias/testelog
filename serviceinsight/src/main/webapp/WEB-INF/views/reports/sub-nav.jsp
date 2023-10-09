<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<spring:url value="/reports/revenue" var="revenue_link" />
<spring:url value="/reports/topservices" var="top_services_link" />
<spring:url value="/reports/servicedetails" var="service_details_link" />
<spring:url value="/reports/laborbyservice" var="labor_by_service_link" />
<spring:url value="/reports/topcustomers" var="top_customers_link" />
<spring:url value="/reports/pricingpipeline" var="pricing_pipeline_link" />
<spring:url value="/reports/laborbreakdown" var="labor_breakdown_link" />
<spring:url value="/reports/laborhours" var="labor_hours_link" />
<spring:url value="/reports/expensecategories" var="expense_categories_link" />
<spring:url value="/reports/azureinvoice" var="azure_invoice_link" />
<spring:url value="/reports/awsinvoice" var="aws_invoice_link" />
<spring:url value="/reports/spla" var="spla_link" />
<spring:url value="/reports/renewals" var="renewals_link" />

<c:set var="is_manager" value="hasRole('ROLE_MANAGER')" />
<c:set var="current_uri" value="${requestScope['javax.servlet.forward.request_uri']}" />

<nav class="page-sub-nav">
  <a href="${revenue_link}" <c:if test="${fn:containsIgnoreCase(current_uri,revenue_link)}">class="active"</c:if>><spring:message code="ui_title_profitability" /></a>
  <a href="${top_services_link}" <c:if test="${fn:containsIgnoreCase(current_uri,top_services_link)}">class="active"</c:if>>Top Services</a>
  <a href="${service_details_link}" <c:if test="${fn:containsIgnoreCase(current_uri,service_details_link)}">class="active"</c:if>>Service Details</a>
  <a href="${top_customers_link }" <c:if test="${fn:containsIgnoreCase(current_uri,top_customers_link)}">class="active"</c:if>>Top Customers</a>
  <a href="${pricing_pipeline_link}" <c:if test="${fn:containsIgnoreCase(current_uri,pricing_pipeline_link)}">class="active"</c:if>>Pricing Pipeline</a>
  <a href="${labor_hours_link}" <c:if test="${fn:containsIgnoreCase(current_uri,labor_hours_link)}">class="active"</c:if>>Labor Hours</a>
  <a href="${expense_categories_link}" <c:if test="${fn:containsIgnoreCase(current_uri,expense_categories_link)}">class="active"</c:if>>Cost Categories</a>
  <sec:authorize access="${is_manager}"><a href="${labor_breakdown_link}" <c:if test="${fn:containsIgnoreCase(current_uri,labor_breakdown_link)}">class="active"</c:if>>Labor Breakdown</a></sec:authorize>
  <div class="sub-nav-divider"></div>
  <a href="${azure_invoice_link}" <c:if test="${fn:containsIgnoreCase(current_uri,azure_invoice_link)}">class="active"</c:if>>Azure Invoice</a>
  <a href="${aws_invoice_link}" <c:if test="${fn:containsIgnoreCase(current_uri,aws_invoice_link)}">class="active"</c:if>>AWS Invoice</a>
  <a href="${spla_link}" <c:if test="${fn:containsIgnoreCase(current_uri,spla_link)}">class="active"</c:if>>SPLA</a>
  <a href="${renewals_link}" <c:if test="${fn:containsIgnoreCase(current_uri,renewals_link)}">class="active"</c:if>>Renewals</a>
</nav>
