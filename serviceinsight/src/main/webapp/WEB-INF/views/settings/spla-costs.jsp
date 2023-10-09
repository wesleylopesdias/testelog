<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt_rt"%>

<spring:url value="/resources/images/ajax-loader.gif" var="ajax_loader" />
<spring:url value="/resources/js/spla-costs.js" var="spla_js" />
<script src="${spla_js}" type="text/javascript"></script>

<jsp:include page="sub-nav.jsp" />

<section class="table-wrapper" id="spla">
  <div class="table-header">
    <div class="table-title">
      SPLA
    </div>
    <div class="table-links">
      <a href="javascript:;" class="spla-popup-link popup-link" data-dialog="spla-cost-dialog" data-id="add"><i class="fa fa-plus-square"></i>Add a SPLA Cost</a>
    </div>
  </div>
  
  <section class="tabs">
    <a href="javascript:;" class="selected table-tab" data-view="active"><spring:message code="ui_title_active" /></a>
    <a href="javascript:;" class="table-tab" data-view="disabled"><i class="fa fa-user-times"></i><spring:message code="ui_title_disabled" /></a>
  </section>
  
  <table>
    <thead>
      <tr>
        <th>SPLA License</th>
        <th>Alt ID</th>
        <th>Vendor</th>
        <th>Cost</th>
        <th class="right"><spring:message code="ui_col_options" /></th>
      </tr>
    </thead>
    <tbody></tbody>
  </table>
</section>

<div id="spla-cost-dialog" class="dialog">
    <div class="message-content"></div>
      
    <div class="dialog-loader-overlay">
        <div class="dialog-loader">
            <img src="${ajax_loader}" />
            <div><spring:message code="ui_dialog_cost_category_loading" /></div>
        </div>
    </div>
    <div class="dialog-instruct"><spring:message code="ui_dialog_general_instruct" /></div>
      
    <div class="dialog-content">
      <div class="field">
        <label><spring:message code="ui_col_cost_name" /></label>
        <span><span class="field-symbol"></span><span class="required-ind required"></span><input type="text" size="50" id="spla-name" maxlength="500" /></span>
      </div>
      <div class="field">
        <label>Alt ID</label>
        <span><span class="field-symbol"></span><span class="required-ind"></span><input type="text" size="30" id="spla-alt-id" maxlength="100" /></span>
      </div>
      <div class="field">
        <label>Cost</label>
        <span><span class="field-symbol">$</span><span class="required-ind required"></span><input type="text" class="currency" id="spla-cost" placeholder="(ex. 150.00)" size="12" /> <span class="small-hint">(per license used)</span></span>
      </div>
      <div class="field">
         <label>Vendor</label>
         <span><span class="field-symbol"></span><span class="required-ind required"></span><select id="spla-vendor">
         	<c:forEach items="${vendors}" var="vendor">
           <option id="${vendor}" value="${vendor}">${vendor.description}</option>
       	</c:forEach>
         </select></span>
      </div>
      <div class="field">
         <label>Type</label>
         <span><span class="field-symbol"></span><span class="required-ind required"></span><select id="spla-type">
         	<c:forEach items="${types}" var="type">
           <option id="${type}" value="${type}">${type.description}</option>
       	</c:forEach>
         </select></span>
      </div>
      <div class="field">
      	 <label>Cost Category</label>
         <span><span class="field-symbol"></span><span class="required-ind required"></span><select id="spla-cost-category">
           <option value=""></option>
           <c:forEach items="${expenseCategories}" var="expenseCategory">
             <option value="${expenseCategory.id}"><c:if test="${expenseCategory.parent ne null}">${expenseCategory.parent.name} - </c:if>${expenseCategory.name}</option>
           </c:forEach>
         </select>
      </div>
      <div class="field">
        <label>Active</label>
        <span><span class="field-symbol"></span><span class="required-ind required"></span><select id="spla-active">
          <option value="true">Yes</option>
          <option value="false">No</option>
        </select></span>
      </div>
    </div>
</div>

<div id="delete-spla-cost-dialog" class="dialog">
    <div class="message-content"></div>
      
    <div class="dialog-loader-overlay">
        <div class="dialog-loader">
            <img src="${ajax_loader}" />
            <div><spring:message code="ui_dialog_delete_cost_category_loading" /></div>
        </div>
    </div>
    <div class="dialog-instruct"><spring:message code="ui_dialog_delete_cost_category_instruct" /></div>
      
    <div class="dialog-content">
      <div class="delete-msg"><i class="fa fa-warning"></i><spring:message code="ui_dialog_delete_warning" /></div>
      <div class="delete-item"><spring:message code="ui_title_to_be_deleted" /> <span id="delete-cost-name"></span></div>
    </div>
</div>