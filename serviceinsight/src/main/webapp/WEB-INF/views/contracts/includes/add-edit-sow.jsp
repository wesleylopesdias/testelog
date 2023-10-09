<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<spring:url value="/resources/images/ajax-loader.gif" var="ajax_loader" />

<div id="add-contract-dialog" class="dialog">
    <div class="message-content"></div>
      
    <div class="dialog-loader-overlay">
        <div class="dialog-loader">
            <img src="${ajax_loader}" />
            <div><spring:message code="ui_contract_loading" /></div>
        </div>
    </div>
    <div class="dialog-instruct"><spring:message code="ui_dialog_general_instruct" /></div>
      
    <div class="dialog-content">
        <div class="field">
          <label><spring:message code="ui_col_contract_name" /></label>
          <span><span class="field-symbol"></span><span class="required-ind required"></span><input type="text" id="contract-name" size="40" /></span>
        </div>
        <div class="field">
          <label><spring:message code="ui_col_contract_id" /></label>
          <span><span class="field-symbol"></span><span class="required-ind required"></span><input type="text" id="contract-alt-id" size="15" /><%-- <span class="small-hint">(if entering an existing SOW)</span>--%></span>
        </div>
        <div class="field">
          <label><spring:message code="ui_col_job_number" /></label>
          <span><span class="field-symbol"></span><span class="required-ind required"></span><input type="text" id="contract-job-number" size="15" /></span>
        </div>
        <div class="field">
          <label><spring:message code="ui_col_sdm" /></label>
          <div class="value"><span class="field-symbol"></span><span class="required-ind"></span><input type="text" size="40" id="contract-sdms" />
            <div class="value-second-row"></div>
          </div>
        </div>
        <div class="field">
          <label><spring:message code="ui_col_bsc" /></label>
          <div class="value"><span class="field-symbol"></span><span class="required-ind"></span><input type="text" size="40" id="contract-bscs" />
            <div class="value-second-row"></div>
          </div>
        </div>
        <div class="field">
          <label><spring:message code="ui_col_ae" /></label>
          <div class="value"><span class="field-symbol"></span><span class="required-ind"></span><input type="text" size="40" id="contract-ae" />
          </div>
        </div>
        <div class="field">
          <label><spring:message code="ui_col_epe" /></label>
          <div class="value"><span class="field-symbol"></span><span class="required-ind"></span><input type="text" size="40" id="contract-epe" />
          </div>
        </div>
        <!-- 
        <div class="field">
          <label><spring:message code="ui_col_engagement_manager" /></label>
          <span><span class="field-symbol"></span><span class="required-ind"></span><input type="text" id="contract-engagement-manager" size="40" maxlength="255" /></span>
        </div>
        <div class="field">
          <label><spring:message code="ui_col_account_exec" /></label>
          <span><span class="field-symbol"></span><span class="required-ind"></span><input type="text" id="contract-account-exec" size="40" maxlength="255" /></span>
        </div> -->
        <div class="form-divider"></div>
        <div class="field">
          <label><spring:message code="ui_col_signed_date" /></label>
          <span><span class="field-symbol"></span><span class="required-ind required"></span><input type="text" class="datepicker" id="contract-signed-date" size="11" maxlength="10" placeholder="mm/dd/yyyy" /></span>
        </div>
        <div class="field">
          <label><spring:message code="ui_col_billing_start_date" /></label>
          <span><span class="field-symbol"></span><span class="required-ind required"></span><input type="text" class="datepicker" id="contract-start-date" size="11" maxlength="10" placeholder="mm/dd/yyyy" /> <span class="small-hint">(<spring:message code="ui_title_contract_hint_billing_start_date" />)</span></span>
        </div>
        <div class="field">
          <label><spring:message code="ui_col_service_start_date" /></label>
          <span><span class="field-symbol"></span><span class="required-ind required"></span><input type="text" class="datepicker" id="contract-service-start-date" size="11" maxlength="10" placeholder="mm/dd/yyyy" /></span>
        </div>
        <div class="field">
          <label><spring:message code="ui_col_contract_end_date" />*</label>
          <span><span class="field-symbol"></span><span class="required-ind required"></span><input type="text" class="datepicker" id="contract-end-date" size="11" maxlength="10" placeholder="mm/dd/yyyy" /> <span class="small-hint">(<spring:message code="ui_title_contract_hint_contract_end_date" />)</span></span>
        </div>
        <div class="notice-msg emphasize-msg"><i class="fas fa-exclamation-triangle"></i><spring:message code="ui_dialog_contract_start_end_date_message" /></div>
        <div id="renewal-section">
            <div class="form-divider"></div>
            <div class="field">
	          <label><spring:message code="ui_col_renewal_status" /></label>
	          <span><span class="field-symbol"></span><span class="required-ind"></span><select id="contract-renewal-status">
	           <option value=""></option>
	           <c:forEach items="${renewalStatuses}" var="renewalStatus">
	               <option value="${renewalStatus}">${renewalStatus.description}</option>
	           </c:forEach>
	           </select></span>
	        </div>
	        <div class="field">
	          <label><spring:message code="ui_col_renewal_change" /></label>
	          <span><span class="field-symbol"></span><span class="required-ind"></span><input type="text" class="currency-negative" id="contract-renewal-change" size="6" />% <span class="small-hint">(<spring:message code="ui_title_contract_hint_renewal_percent" />)</span></span>
	        </div>
	        <div class="field">
              <label><spring:message code="ui_col_renewal_notes" /></label>
              <span><span class="field-symbol"></span><span class="required-ind"></span><textarea id="contract-renewal-notes" maxlength="500" /></textarea></span>
            </div>
        </div>
        <div class="form-divider"></div>
        <div class="field">
          <label>&nbsp;</label>
          <span><span class="field-symbol"></span><span class="required-ind"></span><input type="checkbox" id="customer-archived" />&nbsp;&nbsp;<spring:message code="ui_title_archive_contract" /></span>
        </div>
    </div>
</div>