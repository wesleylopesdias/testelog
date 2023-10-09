<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>


<spring:url value="/costs/list" var="costs_url" />
<spring:url value="/costs/categories" var="cost_categories_url" />
<spring:url value="/costs/allocation" var="cost_allocations_url" />
<spring:url value="/costs/analysis" var="cost_analysis_url" />

<c:set var="is_admin" value="hasRole('ROLE_ADMIN')" />
<c:set var="current_uri" value="${requestScope['javax.servlet.forward.request_uri']}" />

<nav class="page-sub-nav">
  <a href="${costs_url}" <c:if test="${fn:containsIgnoreCase(current_uri,costs_url)}">class="active"</c:if>><spring:message code="ui_title_manage_costs" /></a>
  <sec:authorize access="${is_admin}">
  <a href="${cost_categories_url}" <c:if test="${fn:containsIgnoreCase(current_uri,cost_categories_url)}">class="active"</c:if>><spring:message code="ui_title_manage_cost_categories" /></a>
  <a href="${cost_allocations_url}" <c:if test="${fn:containsIgnoreCase(current_uri,cost_allocations_url)}">class="active"</c:if>><spring:message code="ui_title_cost_allocations" /></a>
  <a href="${cost_analysis_url}" <c:if test="${fn:containsIgnoreCase(current_uri,cost_analysis_url)}">class="active"</c:if>><spring:message code="ui_title_cost_analysis" /></a>
  </sec:authorize>
</nav>