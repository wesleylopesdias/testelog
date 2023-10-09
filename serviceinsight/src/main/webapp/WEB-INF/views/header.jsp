<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<spring:url value="/resources/images/service-insight-logo.png" var="img_head" />
<spring:url value="/resources/images/activate-logo.png" var="activate_logo" />
<spring:url value="/resources/images/osp-logo.png" var="osp_logo" />
<spring:url value="/resources/images/pricing-logo.png" var="pricing_logo" />

<spring:url value="/" var="home_link" />
<spring:url value="/myaccount/updatepassword" var="change_password_link" />
<spring:url value="/logout" var="logout_link" />

<c:set value="https://osptest.logicaliscloud.com" var="osp_url" />
<c:set value="https://pricingtest.logicaliscloud.com" var="pricing_url" />
<c:set value="https://activatetest.logicaliscloud.com" var="activate_url" />  
  
<c:if test="${pageContext.request.serverName eq 'si.logicaliscloud.com'}">
  <c:set value="https://osp.logicaliscloud.com" var="osp_url" />
  <c:set value="https://pricing.logicaliscloud.com" var="pricing_url" />
  <c:set value="https://activate.logicaliscloud.com" var="activate_url" />
</c:if>

<header>
  <div class="container">
    <a href="${home_link}" class="logo"><img src="${img_head}" width="201" height="30" /></a>
    <a class="logicalis-links-arrow" href="javascript:;"><i class="fa fa-chevron-circle-down"></i></a>
    <nav class="logicalis-app-nav">
      <div class="dropdown-menu">
      	<div class="logicalis-app-nav-title">Other Logicalis Apps</div>
      	<a class="dropdown-item" href="${activate_url}" target="_blank"><img src="${activate_logo}" style="height:30px; width:229px;" /><i class="fas fa-external-link-square-alt"></i><div class="clearer"></div></a>
      	<a class="dropdown-item" href="${osp_url}" target="_blank"><img src="${osp_logo}" style="height:30px; width:218px;" /><i class="fas fa-external-link-square-alt"></i><div class="clearer"></div></a>
      	<a class="dropdown-item" href="${pricing_url}" target="_blank"><img src="${pricing_logo}" style="height:30px; width:201px;" /><i class="fas fa-external-link-square-alt"></i><div class="clearer"></div></a>
      </div>
    </nav>
    
    <nav class="auth-nav">
      <ul>
        <li><a href="${logout_link}">Log Out</a></li>
        <li>
          <a href="javascript:;"><i class="fa fa-user"></i>My Account<i class="fa fa-caret-down"></i></a>
          <ul class="sub-menu">
            <li><a href="${change_password_link}">Change Password</a></li>
          </ul>
        </li>
      </ul>
    </nav>
    <div class="clearer"></div>
  </div>
</header>
