<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<spring:url value="/settings/users" var="users_link" />
<spring:url value="/settings/devices" var="devices_link" />
<spring:url value="/settings/spla" var="spla_link" />
<spring:url value="/settings/m365" var="m365_link" />
<c:set var="current_uri" value="${requestScope['javax.servlet.forward.request_uri']}" />

<nav class="page-sub-nav">
  <a href="${users_link}" <c:if test="${fn:containsIgnoreCase(current_uri,users_link)}">class="active"</c:if>>Users</a>
  <a href="${devices_link}" <c:if test="${fn:containsIgnoreCase(current_uri,devices_link)}">class="active"</c:if>>Devices</a>
  <a href="${spla_link}" <c:if test="${fn:containsIgnoreCase(current_uri,spla_link)}">class="active"</c:if>>SPLA</a>
  <a href="${m365_link}" <c:if test="${fn:containsIgnoreCase(current_uri,m365_link)}">class="active"</c:if>>M365</a>
</nav>