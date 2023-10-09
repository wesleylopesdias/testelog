<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<spring:message var="title" code="security_accessdenied_title" htmlEscape="false" />
<div>
  <h3>${fn:escapeXml(title)}</h3>
  <p>
      <spring:message code="security_accessdenied_message" arguments="${url}" />
      <br />
      <spring:message code="security_returntologin_message" />
  </p>
    <c:if test="${not empty exception}">
        <div style="margin-top: 10px;">
        <h4><spring:message code="exception_details" /></h4>
        <spring:message var="message" code="exception_message" htmlEscape="false" />
        <div>
          <span style="color: red;"><c:out value="${exception.localizedMessage}" /></span>
          <c:if test="${exception.getClass().name eq 'com.logicalis.ap.ServiceException'}">
              <div><span><c:out value="${exception.fieldMessage}" /></span></div>
          </c:if>
        </div>
      </div>
    </c:if>
  <p />
</div>

