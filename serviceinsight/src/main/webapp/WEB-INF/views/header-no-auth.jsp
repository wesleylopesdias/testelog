<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<spring:url value="/resources/images/service-insight-logo.png" var="img_head" />

<spring:url value="/" var="home_link" />
<spring:url value="/logout" var="logout_link" />

<header>
  <div class="container">
    <a href="${home_link}" class="logo"><img src="${img_head}" width="201" height="30" /></a>
    <div class="clearer"></div>
  </div>
</header>
