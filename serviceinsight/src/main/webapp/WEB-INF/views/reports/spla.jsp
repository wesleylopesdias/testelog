<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<spring:url value="/resources/js/reports-spla.js" var="reports_js" />
<script src="${reports_js}" type="text/javascript"></script>

<jsp:include page="sub-nav.jsp" />

<section class="section-wrapper">
    <div class="section-header">
        <div class="section-title"><spring:message code="ui_title_filter_criteria" /></div>
    </div>
    <section class="sow-details filter-criteria">
      <div class="content-msg"></div>
      <div class="row">
        <div class="column-3">
          <div class="field">
            <label><spring:message code="ui_col_date_range" /></label>
            <span>
              <select id="report-date-range">
                <option value=""><spring:message code="ui_reports_value_all_time" /></option>
              </select>
            </span>
          </div>
        </div>
        <div class="column-3">
          <div class="field">
            <label><spring:message code="ui_col_customer" /></label>
            <span>
              <select id="report-customer">
                <option value=""><spring:message code="ui_reports_all_customers" /></option>
                <c:forEach items="${customers}" var="customer">
                <c:if test="${customer.siEnabled eq true}"><option value="${customer.id}">${customer.name}</option></c:if>
                </c:forEach>
              </select>
            </span>
          </div>
        </div>
      </div>
      <div class="row">
        <div class="column-3">
          <div class="field">
            <label><spring:message code="ui_col_vendor" /></label>
            <span>
              <select id="report-vendor">
                <option value=""><spring:message code="ui_reports_all_vendors" /></option>
                <c:forEach items="${vendors}" var="vendor">
                    <option value="${vendor}">${vendor}</option>
                </c:forEach>
              </select>
            </span>
          </div>
        </div>
        <div class="column-3">
          <div class="field">
            <label><spring:message code="ui_col_spla" /></label>
            <span>
              <select id="report-spla-filter">
                <option value=""><spring:message code="ui_reports_all_spla" /></option>
                <c:forEach items="${splas}" var="spla">
                    <option value="${spla.id}">${spla.name}</option>
                </c:forEach>
              </select>
            </span>
          </div>
        </div>
      </div>
      <div class="row">
        <div class="column-2">
          <div class="field">
            <label><spring:message code="ui_col_spla_device" /></label>
            <span>
              <select id="report-spla-device-filter">
                <option value=""><spring:message code="ui_reports_all_spla_device" /></option>
                <c:forEach items="${devices}" var="device">
                    <option value="${device.id}">${device.partNumber} / ${device.description}</option>
                </c:forEach>
              </select>
            </span>
          </div>
        </div>
      </div>
      <div class="row">
        <div class="button-row"><a href="javascript:;" class="cta-btn" id="run-report-btn"><spring:message code="ui_btn_run_report" /></a></div>
      </div>
    </section>
</section>

<section class="table-wrapper section-spacer report-section hidden">
  
  <div id="report-spla-data-div">
  </div>
</section>
