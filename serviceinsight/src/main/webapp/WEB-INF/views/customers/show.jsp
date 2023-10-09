<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<!DOCTYPE html>
<div>
    <span>Customer: <b>${customerName}</b></span>
    <p />
    <div style="margin-left: 10px;">
        <span><b><fmt:formatDate pattern="MMMM" value="${theMonth}"/> Service Usage</b></span>
        <table style="border: 1px solid black;">
            <thead>
                <tr>
                    <th>Name</th><th>Start Date</th><th>Units</th><th>Pricing</th><th>Billing</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${serviceUsage}" var="su">
                    <tr>
                    <td style="border:1px solid green;">${su.product}</td>
                    <td style="border:1px solid green;"><fmt:formatDate type="both" value="${su.startDate}"/></td>
                    <td style="border:1px solid green;"><fmt:formatNumber type="number" value="${su.units}"/></td>
                    <td style="border:1px solid green;"><fmt:formatNumber type="currency" value="${su.pricing}"/></td>
                    <td style="border:1px solid green;"><fmt:formatNumber type="currency" value="${su.billable}"/></td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </div>
</div>
