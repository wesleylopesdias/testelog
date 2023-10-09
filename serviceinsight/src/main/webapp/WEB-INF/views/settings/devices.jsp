<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt_rt"%>

<spring:url value="/resources/images/ajax-loader.gif" var="ajax_loader" />
<spring:url value="/resources/js/devices.js" var="devices_js" />
<script src="${devices_js}" type="text/javascript"></script>

<input type="hidden" id="general-error-msg" value="<spring:message code="validation_error_generic" />" />
<input type="hidden" id="device-changes-saved-msg" value="<spring:message code="ui_dialog_ok_devices_saved" />" />
<input type="hidden" id="device-deleted-msg" value="<spring:message code="ui_dialog_ok_devices_deleted" />" />

<jsp:include page="sub-nav.jsp" />

<section class="table-wrapper" id="devices">
  <div class="table-header">
    <div class="table-title">
      Devices
    </div>
    <div class="table-links">
      <a href="javascript:;" class="popup-link" data-dialog="merge-device-dialog"><i class="fa fa-compress"></i><spring:message code="ui_label_merge_devices" /></a>
      <a href="javascript:;" class="update-device popup-link" data-dialog="add-device-dialog" data-id="add"><i class="fa fa-plus-square"></i><spring:message code="ui_label_add_device" /></a>
    </div>
  </div>
  
  <section class="tabs">
    <%-- <a href="javascript:;" class="selected table-tab" data-view="active">Search</a> --%>
    <a href="javascript:;" class="selected table-tab" data-archived="false"><spring:message code="ui_label_tab_active_devices" /></a>
    <a href="javascript:;" class="table-tab" data-archived="true"><i class="fa fa-archive"></i><spring:message code="ui_label_tab_archived_devices" /></a>
  </section>
  
  <table>
    <thead>
      <tr>
        <th><spring:message code="ui_col_part_description" /></th>
        <th><spring:message code="ui_col_service_code" /></th>
        <th><spring:message code="ui_col_device_type" /></th>
        <th class="center">Activate Sync</th>
        <th class="center">Is CI</th>
        <th class="center">Business Service</th>
        <th class="center">Pricing Sheet</th>
        <th class="center">Unit Count</th>
        <th class="right"><spring:message code="ui_col_options" /></th>
      </tr>
    </thead>
    <tbody></tbody>
  </table>
</section>

<div id="add-device-dialog" class="dialog">
    <div class="message-content"></div>
      
    <div class="dialog-loader-overlay">
        <div class="dialog-loader">
            <img src="${ajax_loader}" />
            <div><spring:message code="ui_dialog_devices_save_loading" /></div>
        </div>
    </div>
    <div class="dialog-instruct"><spring:message code="ui_dialog_general_instruct" /></div>
      
    <div class="dialog-content">
        <div class="field">
          <label><spring:message code="ui_col_part_description" /></label>
          <div class="value">
            <span class="field-symbol"></span><span class="required-ind required"></span><input type="text" id="device-description" size="50" />
          </div>
        </div>
        <div class="field add">
          <label><spring:message code="ui_col_service_code" /></label>
          <div class="value">
            <span class="field-symbol"></span><span class="required-ind required"></span><input type="text" id="device-part-number" size="30"  class="field-part-number" />
          </div>
        </div>
        <div class="field add">
          <label><spring:message code="ui_col_default_service" /></label>
          <div class="value">
            <span class="field-symbol"></span><span class="required-ind"></span><select id="device-default-service" class="service-dropdown">
                <option value=""></option>
                <c:forEach items="${services}" var="service">
                <option value="${service.ospId}">${service.name}</option>
                </c:forEach>
            </select>
          </div>
        </div>
        <div class="field add">
          <label><spring:message code="ui_col_device_type" /></label>
          <div class="value">
            <span class="field-symbol"></span><span class="required-ind"></span><select id="device-type">
              <option value=""></option>
              <c:forEach items="${deviceTypes}" var="deviceType">
                <option value="${deviceType}">${deviceType.description}</option>
                </c:forEach>
            </select>
          </div>
        </div>
        <input type="hidden" id="device-alt-id" />
        <input type="hidden" id="device-product-id" />
        <section>
	        <div class="field">
	          <label>Pricing Tool Sync</label>
	          <span class="checkbox">
	            <span class="field-symbol"></span><span class="required-ind"></span><label><input type="checkbox" id="device-pricing-sync-enabled" />&nbsp;&nbsp;Enable Pricing Tool Sync</label>
	          </span>
	        </div>
	        <div class="field">
	          <label>Catalog Pricing</label>
	          <span>
	            <span class="field-symbol">$</span><span class="required-ind"></span><input type="text" size="5" class="currency" id="device-catalog-recurring-cost" />&nbsp;MRC
	            <span style="margin-left:40px;"><span class="field-symbol">$</span><span class="required-ind"></span><input type="text" size="5" class="currency" id="device-catalog-recurring-price" />&nbsp;MRP</span>
	          </span>
	        </div>
        </section>
        <div class="field add">
          <span id="device-spla-clone" style="display:none;">
            <select name="device-spla" style="margin-bottom:5px; width:430px;">
              <option value=""></option>
              <c:forEach items="${splas}" var="spla">
                <option value="${spla.id}">${spla.name}</option>
              </c:forEach>
            </select>
            <a href="javascript:;" class="remove-spla"><i class="fas fa-minus-circle"></i></a>
          </span>
          <label>SPLA License Associated</label>
          <div class="value">
            <div id="spla-container" style="padding-left:17px;"></div>
          </div>
        </div>
        <div style="text-align:right;"><a href="javascript:;" id="device-spla-add"><i class="fas fa-plus-square"></i>Add Another SPLA</a></div>

        <div class="field"><label>Parent Devices</label>
            <div id="parent-devices"></div>
        </div>

        <div class="field"><label>Related Devices</label></div>
        <div class="field add" style="margin-top: 10px;">
          <span id="device-related-clone" style="display:none;">
            <select name="device-related" style="margin-bottom:5px; width:420px;">
              <option value=""></option>
              <c:forEach items="${deviceSelect}" var="entry">
                <option value="${entry.id}">${entry.description} (${entry.partNumber})</option>
              </c:forEach>
            </select>
            <select name="device-related-relationship" style="margin-left: 5px; width:100px;">
              <c:forEach items="${deviceRelationships}" var="deviceRelationship">
                <option value="${deviceRelationship}">${deviceRelationship.description}</option>
                </c:forEach>
            </select>
            <span class="required-ind required" style="margin-left: 5px;"></span>
            <input type="text" size="2" name="spec-units" value="0" style="height: 9px;"/>
            <span>(# units)</span>
            <input type="text" size="2" name="sort-order" value="0" style="height: 9px;"/>
            <span>(order)</span>
            <a href="javascript:;" class="remove-related"><i class="fas fa-minus-circle"></i></a>
          </span>
          <div class="value" style="width: 90%;">
            <div id="related-container" style="margin-left: 10px;"></div>
          </div>
        </div>
        <div style="text-align:right;"><a href="javascript:;" id="device-related-add"><i class="fas fa-plus-square"></i>Add Related Device</a></div>
        
        <div class="field"><label>Cost Category Mappings</label></div>
        <div class="field add" style="margin-top: 10px;">
          <span id="device-cost-category-clone" style="display:none;">
            <select name="device-cost-category" style="margin-bottom:5px; width:420px;">
              <option value=""></option>
              <c:forEach items="${costCategorySelect}" var="entrygroup">
                <optgroup label="${entrygroup.name}">
                  <c:forEach items="${entrygroup.subcategories}" var="entry">
                    <option value="${entry.id}" data-units="${entry.units}">${entry.name} (${entry.description})</option>
                  </c:forEach>
                </optgroup>
              </c:forEach>
            </select>
            <span class="unit-type"></span>
            <span class="required-ind required" style="margin-left: 5px;"></span>
            <input type="text" size="2" name="quantity" value="0" style="height: 9px;"/>
            <span>(Quantity)</span>
            <input type="checkbox" name="allocation-category" style="margin-left:8px;" />
            <span>(Allocation Category)</span>
            <a href="javascript:;" class="remove-cost-category"><i class="fas fa-minus-circle"></i></a>
          </span>
          <div class="value" style="width: 90%;">
            <div id="cost-category-container" style="margin-left: 10px;"></div>
          </div>
        </div>
        <div style="text-align:right;"><a href="javascript:;" id="device-cost-category-add"><i class="fas fa-plus-square"></i>Add Cost Category</a></div>
        
        <section>
          <div class="field"><label>Device Properties</label></div>
          <div class="field" style="margin-top: 10px;">
            <span id="device-property-clone" style="display:none;">
              <select name="device-property-type" style="margin-bottom:5px; width:420px;">
                <option value=""></option>
                <c:forEach items="${devicePropertyTypes}" var="propertyType">
                  <option value="${propertyType}" data-type="${propertyType.dataType}">${propertyType.description}</option>
                </c:forEach>
              </select>
              <span class="device-property-number">
                <span class="required-ind required" style="margin-left: 5px;"></span>
                <input type="text" size="3" name="property-number" value="0" style="height: 9px;"/>
                <span>(# units)</span>
                <input type="text" size="5" name="property-unit-type" value="" style="height: 9px;"/>
                <span>(unit type)</span>
              </span>
              <span class="device-property-string" style="display:none;">
                <span class="required-ind required" style="margin-left: 5px;"></span>
                <input type="text" size="30" name="property-string" value="" style="height: 9px;"/>
              </span>
              <a href="javascript:;" class="remove-property"><i class="fas fa-minus-circle"></i></a>
            </span>
            <div class="value" style="width: 90%;">
              <div id="property-container" style="margin-left: 10px;"></div>
            </div>
          </div>
          <div style="text-align:right;"><a href="javascript:;" id="device-property-add"><i class="fas fa-plus-square"></i>Add Device Property</a></div>
        </section>
        
        <div class="form-spacer"></div>
        <div class="field">
          <label></label>
          <span class="checkbox">
            <span class="field-symbol"></span><span class="required-ind"></span><label><input type="checkbox" id="device-archived" />&nbsp;&nbsp;<spring:message code="ui_label_archive_device" /></label>
          </span>
        </div>
        <div class="field">
          <label></label>
          <span class="checkbox">
            <span class="field-symbol"></span><span class="required-ind"></span><label><input type="checkbox" id="device-activate-sync" />&nbsp;&nbsp;<spring:message code="ui_label_activate_sync" /></label>
          </span>
        </div>
        <div class="field">
          <label></label>
          <span class="checkbox">
            <span class="field-symbol"></span><span class="required-ind"></span><label><input type="checkbox" id="device-is-ci" />&nbsp;&nbsp;<spring:message code="ui_label_device_is_ci" /></label>
          </span>
        </div>
        <div class="field">
          <label></label>
          <span class="checkbox">
            <span class="field-symbol"></span><span class="required-ind"></span><label style="width:auto;"><input type="checkbox" id="device-add-business-service" />&nbsp;&nbsp;<spring:message code="ui_label_device_add_business_service" /></label>
          </span>
        </div>
        <div class="field">
          <label></label>
          <span class="checkbox">
            <span class="field-symbol"></span><span class="required-ind"></span><label><input type="checkbox" id="device-pricing-sheet-enabled" />&nbsp;&nbsp;Enabled for Pricing Sheets</label>
          </span>
        </div>
        <div class="field">
          <label></label>
          <span class="checkbox">
            <span class="field-symbol"></span><span class="required-ind"></span><label><input type="checkbox" id="device-require-unit-count" />&nbsp;&nbsp;Require Unit Count</label>
          </span>
        </div>
        <div class="field">
          <label></label>
          <span class="checkbox">
            <span class="field-symbol"></span><span class="required-ind"></span><label><input type="checkbox" id="device-cost-allocation-option" />&nbsp;&nbsp;Cost Allocation Option</label>
          </span>
        </div>
    </div>
</div>

<div id="delete-device-dialog" class="dialog">
    <div class="message-content"></div>
      
    <div class="dialog-loader-overlay">
        <div class="dialog-loader">
            <img src="${ajax_loader}" />
            <div><spring:message code="ui_dialog_delete_devices_loading" /></div>
        </div>
    </div>
    <div class="dialog-instruct"><spring:message code="ui_dialog_delete_devices_instruct" /></div>
      
    <div class="dialog-content">
      <div class="delete-msg"><i class="fa fa-warning"></i><spring:message code="ui_dialog_delete_warning" /></div>
      <div class="delete-item"><spring:message code="ui_title_to_be_deleted" /> <span id="delete-device-name"></span></div>
    </div>
</div>

<div id="merge-device-dialog" class="dialog">
    <div class="message-content"></div>
      
    <div class="dialog-loader-overlay">
        <div class="dialog-loader">
            <img src="${ajax_loader}" />
            <div><spring:message code="ui_dialog_device_merge_saving" /></div>
        </div>
    </div>
    <div class="dialog-instruct"><spring:message code="ui_dialog_device_merge_instruct" /></div>
      
    <div class="delete-msg" style="font-size:0.8em;"><i class="fa fa-warning"></i><spring:message code="ui_dialog_device_merge_warning" /></div>
      
    <div class="dialog-content">
        <div class="field">
          <label class="short"><spring:message code="ui_col_device_one" /></label>
          <div class="value">
            <span class="field-symbol"></span><span class="required-ind required"></span><select id="merge-device-from" style="width:550px;">
                <option value=""></option>
            </select>
          </div>
        </div>
        <div class="field">
          <label class="short"></label>
          <div class="value">
          <span class="field-symbol"></span><span class="required-ind"></span><spring:message code="ui_label_will_be_merged" />
          </div>
        </div>
        <div class="field">
          <label class="short"><spring:message code="ui_col_device_two" /></label>
          <div class="value">
            <span class="field-symbol"></span><span class="required-ind required"></span><select id="merge-device-into" style="width:550px;">
                <option value=""></option>
            </select>
          </div>
        </div>
    </div>
</div>