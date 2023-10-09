<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<spring:message var="title" code="ok_message" htmlEscape="false" />
<div>
  <h3>${fn:escapeXml(title)}</h3>
  <p>
      <spring:message code="ok_message_details" />
  </p>
</div>

