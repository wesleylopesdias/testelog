var DEVICES = {
	device_dialog:"#add-device-dialog",
	delete_device_dialog:"#delete-device-dialog",
	general_validation_error:"#general-error-msg",
	init:function() {
		DEVICES.bind_events();
		DEVICES.read.init();
		DEVICES.update.init();
		DEVICES.del.init();
		DEVICES.merge.init();
	},
	bind_events:function() {
		
	},
	read:{
		devices_container:"#devices",
		tab:".table-tab",
		selected_tab:".selected",
		devices:[],
                indentidx:19,
		init:function() {
			DEVICES.read.bind_events();
			DEVICES.read.get_devices();
		},
		bind_events:function() {
			$(DEVICES.read.tab).click(function() {
				$(DEVICES.read.tab).removeClass("selected");
				$(this).addClass("selected");
				
				DEVICES.read.get_devices();
			});
		},
		get_devices:function(target) {
			if(!target) UTIL.add_table_loader($(DEVICES.read.devices_container));
			
			var archived = "?archived=" + $(DEVICES.read.tab + DEVICES.read.selected_tab).data("archived");
			
			$.ajax ({
				url: PAGE_CONSTANTS.BASE_URL + "settings/devices.json" + archived,
				type: "GET",
				success:function(data) {
					DEVICES.read.devices = data;
					if(target == "merge") {
						DEVICES.merge.build_dropdowns(data);
					} else {
						DEVICES.read.build_table(data);
					}
				}
			});
		},
		build_table:function(devices) {
			var output = "";
			for(var i=0; i < devices.length; i++) {
                var device = devices[i];
                output = DEVICES.read.build_table_row(output, device, 0);
			}
			$(DEVICES.read.devices_container + " tbody").html(output);
		},
		build_table_row:function(output, device, nesting, ancestors) {
            if (!ancestors) ancestors = "";
            var part_number = "";
            var description = device.description;
            var device_type = "";
            var activate_sync = device.activateSyncEnabled;
            var add_business_service = device.activateAddBusinessService;
            var pricing_sheet_enabled = device.pricingSheetEnabled;
            var require_unit_count = device.requireUnitCount;
            var default_service_id = device.defaultOspId;
            var is_ci = device.isCI;
            var activate_sync_display = "N";
            var is_ci_display = "N";
            var add_business_service_display = "N";
            var pricing_sheet_enabled_display = "N";
            var require_unit_count_display = "N";
            var open_related = "";
            var open_related_margin = (nesting - 1) * DEVICES.read.indentidx;
            var indent = 0;
            var show_table_row = "display:table-row";
            var row_id = "id" + ancestors + "_" + device.id;
            if (nesting > 0 && device.hasParent) {
                show_table_row = "display:none";
                indent = nesting * DEVICES.read.indentidx;
            }
            if (device.relatedDevices && device.relatedDevices.length > 0) {
                open_related = "<i onclick=\"javascript:DEVICES.read.show_hide_row("+device.id+", '"+row_id+"', this);\" class=\"fa fa-plus-square\" style=\"margin-left:"+open_related_margin+"px;\"></i>";
                if (nesting > 0 && device.hasParent) {
                    indent = 0;
                }
            }
            if(device.partNumber && device.partNumber !== "null") part_number = device.partNumber;
            if(device.deviceType && device.deviceType !== "null") device_type = device.deviceType;
            if(activate_sync) activate_sync_display = "Y";
            if(is_ci) is_ci_display = "Y";
            if(add_business_service) add_business_service_display = "Y";
            if(pricing_sheet_enabled) pricing_sheet_enabled_display = "Y";
            if(require_unit_count) require_unit_count_display = "Y";

            output += "<tr id=\""+row_id+"\" style=\""+show_table_row+";\">";
            output += "<td>"+open_related+"<a href=\"javascript:;\" class=\"popup-link update-device\" data-dialog=\"add-device-dialog\" data-id=\"" + device.id + "\" style=\"margin-left:"+indent+"px;\">" + description + "</a></td>";
            output += "<td>" + part_number + "</td>";
            output += "<td>" + device_type + "</td>";
            output += "<td class=\"center\">" + activate_sync_display + "</td>";
            output += "<td class=\"center\">" + is_ci_display + "</td>";
            output += "<td class=\"center\">" + add_business_service_display + "</td>";
            output += "<td class=\"center\">" + pricing_sheet_enabled_display + "</td>";
            output += "<td class=\"center\">" + require_unit_count_display + "</td>";
            output += "<td><a href=\"javascript:;\" class=\"popup-link delete-device icon-link\" data-dialog=\"delete-device-dialog\" data-id=\"" + device.id + "\"><i class=\"fa fa-minus-circle\"></i>Delete</a></td>";
            //output += "<td>&nbsp;</td>";
            output += "</tr>";

            if (device.relatedDevices && device.relatedDevices.length > 0) {
                for(var i=0; i < device.relatedDevices.length; i++) {
                    output = DEVICES.read.build_table_row(output, device.relatedDevices[i], nesting + 1, ancestors + "_" + device.id);
                }
            }
            return output;
		},
        show_hide_row:function(parent_id, ancestry, fa_obj){
            if (!ancestry) ancestry = "id";
            var device = $.grep(DEVICES.read.devices, function(n) { return n.id == parent_id; });
            device = device[0];
            for(var j=0; j < device.relatedDevices.length; j++) {
                var related_device = device.relatedDevices[j];
                $("#"+ancestry+"_"+related_device.id).toggle();
                if (fa_obj.className === 'fa fa-minus-square') {
                    // when CLOSING, collapse ALL inner relatedDevices
                    if (related_device.relatedDevices && related_device.relatedDevices.length > 0) {
                        // not sure I understand, but the "fa" element appears twice with the first being the one to inspect
                        // if it is minus/open we want to toggle it to plus/closed
                        $.each($("#"+ancestry+"_"+related_device.id).find("i"), function(idx, fa_ch_obj) {
                            if (idx === 0 && fa_ch_obj.className === 'fa fa-minus-square') {
                                DEVICES.read.show_hide_row(related_device.id, ancestry + "_" + related_device.id, fa_ch_obj);
                            }
                        });
                    }
                }
            }
            if (fa_obj.className === 'fa fa-minus-square') {
                fa_obj.className = 'fa fa-plus-square';
            } else {
                fa_obj.className = 'fa fa-minus-square';
            }
        }
	},
	update:{
		device_description_field:"#device-description",
		device_part_number_field:"#device-part-number",
		device_type_field:"#device-type",
		device_archived_field:"#device-archived",
		device_activate_sync_field:"#device-activate-sync",
		device_is_ci_field:"#device-is-ci",
		device_add_business_service:"#device-add-business-service",
		device_pricing_sheet_enabled:"#device-pricing-sheet-enabled",
		device_pricing_sync_enabled:"#device-pricing-sync-enabled",
		device_catalog_recurring_cost_field:"#device-catalog-recurring-cost",
		device_catalog_recurring_price_field:"#device-catalog-recurring-price",
		device_product_id_field:"#device-product-id",
		device_alt_id_field:"#device-alt-id",
		device_require_unit_count:"#device-require-unit-count",
		device_cost_allocation_option:"#device-cost-allocation-option",
		device_default_service:"#device-default-service",
		device_spla_clone:"#device-spla-clone",
		device_spla_field:"select[name=device-spla]",
		device_spla_remove:".remove-spla",
		device_related_clone:"#device-related-clone",
		device_related_field:"select[name=device-related]",
		device_related_remove:".remove-related",
		device_related_relationship_field:"select[name=device-related-relationship]",
		device_related_spec_units_field:"input[name=spec-units]",
		device_related_sort_order_field:"input[name=sort-order]",
		device_cost_category_clone:"#device-cost-category-clone",
		device_cost_category_container:"#cost-category-container",
                device_parent_devices:"#parent-devices",
		device_cost_category_field:"select[name=device-cost-category]",
        row_unit_type:".unit-type",
		device_cost_category_quantity_field:"input[name=quantity]",
		device_cost_category_alloaction_field:"input[name=allocation-category]",
		device_cost_category_remove:".remove-cost-category",
		device_property_clone:"#device-property-clone",
		device_property_container:"#property-container",
		device_property_add_btn:"#device-property-add",
		device_property_remove:".remove-property",
		device_property_type_field:"select[name='device-property-type']",
		device_property_number_container:".device-property-number",
		device_property_string_container:".device-property-string",
		device_property_number_field:"input[name='property-number']",
		device_property_unit_type_field:"input[name='property-unit-type']",
		device_property_string_field:"input[name='property-string']",
		popup_class:".update-device",
		changes_saved_msg:"#device-changes-saved-msg",
		current_id:null,
		init:function() {
			DEVICES.update.bind_events();
		},
		bind_events:function() {
			$(DEVICES.device_dialog).dialog({
				  autoOpen:false,
				  dialogClass:"device-dialog",	
			      resizable:false,
			      width:950,
			      height:650,
			      modal:true,
			      title: "Add/Edit Device",
			      open:function() {
			    	  $('.datepicker').datepicker('enable');
			    	  $(this).closest(".ui-dialog").find(".ui-button:contains('OK')").hide();
			    	  $(this).closest(".ui-dialog").find(".ui-button:contains('Save')").show();
			    	  $(this).closest(".ui-dialog").find(".ui-button:contains('Cancel')").show();
			      },
			      close:function() {
			    	  $('.datepicker').datepicker('disable');
			      },
			      buttons: {
			        "Save":function() {
			        	DEVICES.update.submit_device();
			        },
			        "Cancel":function() {
			            $(this).dialog("close");
			        },
			        "OK":function() {
			            $(this).dialog("close");
			        }
			      },
			      create:function () {
			    	  $(this).closest(".ui-dialog").find(".ui-button:contains('Cancel')").addClass("cancel");
			    	  $(this).closest(".ui-dialog").find(".ui-button:contains('OK')").hide();
			      }
			});
			
			$(document).on("click", DEVICES.update.popup_class, function() {
				var id = $(this).data("id");
				(id == "add") ? DEVICES.update.current_id = null : DEVICES.update.current_id = id;
				DEVICES.update.reset_popup();
			});
			
			$(document).on("click", DEVICES.update.device_spla_remove, function() {
				$(this).parent().remove();
			});
			
			$(document).on("click", DEVICES.update.device_related_remove, function() {
				$(this).parent().remove();
			});
			
			$(document).on("click", DEVICES.update.device_cost_category_remove, function() {
				$(this).parent().remove();
			});
			
			$(document).on("click", DEVICES.update.device_property_remove, function() {
				$(this).parent().remove();
			});
			
			$(DEVICES.update.device_pricing_sync_enabled).click(function() {
				var checked = $(this).prop("checked");
				if(checked) {
					$(DEVICES.update.device_catalog_recurring_cost_field).prop("disabled", true);
					$(DEVICES.update.device_catalog_recurring_price_field).prop("disabled", true);
				} else {
					$(DEVICES.update.device_catalog_recurring_cost_field).prop("disabled", false);
					$(DEVICES.update.device_catalog_recurring_price_field).prop("disabled", false);
				}
			});
			
			$(document).on("change", DEVICES.update.device_cost_category_field, function() {
				var units = $(this).find(":selected").data("units");
				$(this).next(DEVICES.update.row_unit_type).html(units);
			});
			
			$(document).on("change", DEVICES.update.device_property_type_field, function() {
				var type = $(this).find(":selected").data("type");
				
				if(type == "string") {
					$(this).siblings(DEVICES.update.device_property_number_container).hide();
					$(this).siblings(DEVICES.update.device_property_string_container).show();
				} else {
					$(this).siblings(DEVICES.update.device_property_number_container).show();
					$(this).siblings(DEVICES.update.device_property_string_container).hide();
				}
			});
			
			$("#device-spla-add").click(function() {
				DEVICES.update.clone_spla_row();
			});
			
			$("#device-related-add").click(function() {
				DEVICES.update.clone_related_row();
			});
			
			$("#device-cost-category-add").click(function() {
				DEVICES.update.clone_cost_category_row();
			});
			
			$(DEVICES.update.device_property_add_btn).click(function() {
				DEVICES.update.clone_property_row();
			});
		},
		reset_popup:function() {
			UTIL.clear_message_in_popup(DEVICES.device_dialog);
			
			var description = "";
			var part_number = "";
			var device_type = "";
			var archived = false;
			var activate_sync = false;
			var is_ci = false;
			var add_business_service = false;
			var pricing_sheet_enabled = false;
			var require_unit_count = false;
			var cost_allocation_option = false;
			var default_service_id = "";
			var pricing_sync_enabled = false;
			var catalog_recurring_cost = "";
			var catalog_recurring_price = "";
			var product_id = null;
			var alt_id = null;
			
			$("#spla-container").html("");
			$("#related-container").html("");
			$(DEVICES.update.device_cost_category_container).html("");
                        $(DEVICES.update.device_parent_devices).html("");
			$(DEVICES.update.device_property_container).html("");
			
			if(DEVICES.update.current_id) {
				var device = $.grep(DEVICES.read.devices, function(n) { return n.id == DEVICES.update.current_id; });
				device = device[0];
				
				if(device) {
					description = device.description;
					part_number = device.partNumber;
					device_type = device.deviceType;
					archived = device.archived;
					activate_sync = device.activateSyncEnabled;
					add_business_service = device.activateAddBusinessService;
					default_service_id = device.defaultOspId;
					is_ci = device.isCI;
					pricing_sheet_enabled = device.pricingSheetEnabled;
					require_unit_count = device.requireUnitCount;
					cost_allocation_option = device.costAllocationOption;
					pricing_sync_enabled = device.pricingSyncEnabled;
					catalog_recurring_cost = device.catalogRecurringCost;
					catalog_recurring_price = device.catalogRecurringPrice;
					product_id = device.productId;
					alt_id = device.altId;
					
					if(device.splaCosts.length > 0) {
						for(var i=0; i < device.splaCosts.length; i++) {
							var spla = device.splaCosts[i];
							DEVICES.update.clone_spla_row();
							var $span = $("#spla-container" + " span:last-child");
							$span.find(DEVICES.update.device_spla_field).val(spla.id);
						}
					} else {
						DEVICES.update.clone_spla_row();
					}
					
                                        if(device.parentDevices && device.parentDevices.length > 0) {
                                            var output = "";
                                            
                                            for(var i=0; i < device.parentDevices.length; i++) {
                                                var parentDevice = device.parentDevices[i];
                                                output += "<a href=\"javascript:;\" class=\"popup-link update-device\" data-dialog=\"add-device-dialog\" data-id=\""
                                                        + parentDevice.id + "\" style=\"margin-left:0px;\">"
                                                        + parentDevice.description + " (" + parentDevice.partNumber + ")" + "</a><br/>";
                                            }
                                            $(DEVICES.update.device_parent_devices).html(output);
                                        }
					if(device.relatedDevices.length > 0) {
						for(var i=0; i < device.relatedDevices.length; i++) {
							var relatedDevice = device.relatedDevices[i];
							DEVICES.update.clone_related_row();
							var $span = $("#related-container" + " span:last-child");
							$span.find(DEVICES.update.device_related_field).val(relatedDevice.id);
							$span.find(DEVICES.update.device_related_relationship_field).val(relatedDevice.relationship);
							$span.find(DEVICES.update.device_related_spec_units_field).val(relatedDevice.specUnits);
							$span.find(DEVICES.update.device_related_sort_order_field).val(relatedDevice.order);
						}
					} else {
						DEVICES.update.clone_related_row();
					}
					
					if(device.costMappings.length > 0) {
						for(var i=0; i < device.costMappings.length; i++) {
							var costMapping = device.costMappings[i];
							if(costMapping.relationship != "embedded") {
								DEVICES.update.clone_cost_category_row();
								var $span = $(DEVICES.update.device_cost_category_container + " span:last-child");
								$span.find(DEVICES.update.device_cost_category_field).val(costMapping.expenseCategoryId);
								$span.find(DEVICES.update.device_cost_category_quantity_field).val(costMapping.quantity);
								$span.find(DEVICES.update.device_cost_category_alloaction_field).prop("checked", costMapping.allocationCategory);
							}
						}
					} else {
						DEVICES.update.clone_cost_category_row();
					}
					
					if(device.properties.length > 0) {
						for(var i=0; i < device.properties.length; i++) {
							var property = device.properties[i];
							DEVICES.update.clone_property_row();
							var $span = $(DEVICES.update.device_property_container + " span:last-child");
							$span.find(DEVICES.update.device_property_type_field).val(property.type);
							$span.find(DEVICES.update.device_property_number_field).val(property.unitCount);
							$span.find(DEVICES.update.device_property_unit_type_field).val(property.unitType);
							$span.find(DEVICES.update.device_property_string_field).val(property.strValue);
							
							if(property.strValue) {
								$span.find(DEVICES.update.device_property_number_container).hide();
								$span.find(DEVICES.update.device_property_string_container).show();
							} else {
								$span.find(DEVICES.update.device_property_number_container).show();
								$span.find(DEVICES.update.device_property_string_container).hide();
							}
						}
					}
				}
			} else {
				DEVICES.update.clone_spla_row();
				DEVICES.update.clone_related_row();
				DEVICES.update.clone_cost_category_row();
				DEVICES.update.clone_property_row();
			}
			
			if(pricing_sync_enabled) {
				$(DEVICES.update.device_catalog_recurring_cost_field).prop("disabled", true);
				$(DEVICES.update.device_catalog_recurring_price_field).prop("disabled", true);
			} else {
				$(DEVICES.update.device_catalog_recurring_cost_field).prop("disabled", false);
				$(DEVICES.update.device_catalog_recurring_price_field).prop("disabled", false);
			}
			
			$(DEVICES.update.device_description_field).val(description);
			$(DEVICES.update.device_part_number_field).val(part_number);
			$(DEVICES.update.device_type_field).val(device_type);
			$(DEVICES.update.device_default_service).val(default_service_id);
			$(DEVICES.update.device_archived_field).prop("checked", archived);
			$(DEVICES.update.device_is_ci_field).prop("checked", is_ci);
			$(DEVICES.update.device_activate_sync_field).prop("checked", activate_sync);
			$(DEVICES.update.device_add_business_service).prop("checked", add_business_service);
			$(DEVICES.update.device_pricing_sheet_enabled).prop("checked", pricing_sheet_enabled);
			$(DEVICES.update.device_require_unit_count).prop("checked", require_unit_count);
			$(DEVICES.update.device_cost_allocation_option).prop("checked", cost_allocation_option);
			$(DEVICES.update.device_pricing_sync_enabled).prop("checked", pricing_sync_enabled);
			$(DEVICES.update.device_catalog_recurring_cost_field).val(UTIL.convert_decimal_for_ui(catalog_recurring_cost));
			$(DEVICES.update.device_catalog_recurring_price_field).val(UTIL.convert_decimal_for_ui(catalog_recurring_price));
			$(DEVICES.update.device_product_id_field).val(product_id);
			$(DEVICES.update.device_alt_id_field).val(alt_id);
		},
		clone_spla_row:function() {
			var $clone = $(DEVICES.update.device_spla_clone).clone().attr("id","").show();
			$("#spla-container").append($clone);
		},
		clone_related_row:function() {
			var $clone = $(DEVICES.update.device_related_clone).clone().attr("id","").attr("class","cloned-related").show();
			$("#related-container").append($clone);
		},
		clone_cost_category_row:function() {
			var $clone = $(DEVICES.update.device_cost_category_clone).clone().attr("id","").attr("class","cloned-cost-category").show();
			$(DEVICES.update.device_cost_category_container).append($clone);
		},
		clone_property_row:function() {
			var $clone = $(DEVICES.update.device_property_clone).clone().attr("id","").attr("class","cloned-property").show();
			$(DEVICES.update.device_property_container).append($clone);
		},
		submit_device:function() {
			UTIL.clear_message_in_popup(DEVICES.device_dialog);
			
			var part_number = $(DEVICES.update.device_part_number_field).val();
			var description = $(DEVICES.update.device_description_field).val();
			var device_type = $(DEVICES.update.device_type_field).val();
			var archived = $(DEVICES.update.device_archived_field).prop("checked");
			var activate_sync = $(DEVICES.update.device_activate_sync_field).prop("checked");
			var is_ci = $(DEVICES.update.device_is_ci_field).prop("checked");
			var add_business_service = $(DEVICES.update.device_add_business_service).prop("checked");
			var pricing_sheet_enabled = $(DEVICES.update.device_pricing_sheet_enabled).prop("checked");
			var require_unit_count = $(DEVICES.update.device_require_unit_count).prop("checked");
			var cost_allocation_option = $(DEVICES.update.device_cost_allocation_option).prop("checked");
			var pricing_sync_enabled = $(DEVICES.update.device_pricing_sync_enabled).prop("checked");
			var catalog_recurring_cost = $(DEVICES.update.device_catalog_recurring_cost_field).val();
			var catalog_recurring_price = $(DEVICES.update.device_catalog_recurring_price_field).val();
			var default_osp_id = $(DEVICES.update.device_default_service).val();
			var product_id = $(DEVICES.update.device_product_id_field).val();
			var alt_id = $(DEVICES.update.device_alt_id_field).val();
			var ajax_type = "POST";
			
			if(!part_number || !description) {
				UTIL.add_error_message_to_popup(DEVICES.device_dialog, $(DEVICES.general_validation_error).val());
				return false;
			}
			
			part_number = part_number.toUpperCase();
			
			var spla_costs = [];
			$("#spla-container select:visible").each(function() {
				var spla_cost_catalog_id = $(this).val();
				if(spla_cost_catalog_id) spla_costs.push({ id:spla_cost_catalog_id });
			});
                        
			var related_devices = [];
            $(".cloned-related").each(function(idx, inputs) {
                var related_device_id = $(inputs).find(DEVICES.update.device_related_field).val();
                if (related_device_id) {
                    var related_device_relationship = $(inputs).find(DEVICES.update.device_related_relationship_field).val();
                    var related_device_spec_units = $(inputs).find(DEVICES.update.device_related_spec_units_field).val();
                    var related_device_sort_order = $(inputs).find(DEVICES.update.device_related_sort_order_field).val();
                    related_devices.push({"id":related_device_id, "relationship":related_device_relationship, "specUnits":related_device_spec_units, "order":related_device_sort_order });
                }
            });
                        
			var cost_mappings = [];
			var allocation_category_count = 0;
            $(".cloned-cost-category").each(function(idx, inputs) {
                var cost_mapping_id = $(inputs).find(DEVICES.update.device_cost_category_field).val();
                if (cost_mapping_id) {
                    var cost_mapping_quantity = $(inputs).find(DEVICES.update.device_cost_category_quantity_field).val();
                    var allocation_category = $(inputs).find(DEVICES.update.device_cost_category_alloaction_field).prop("checked");
                    if(allocation_category) allocation_category_count++;
                    cost_mappings.push({"deviceId":DEVICES.update.current_id, "expenseCategoryId":cost_mapping_id, "quantity":cost_mapping_quantity, "allocationCategory":allocation_category });
                }
            });
            
            if(allocation_category_count > 1) {
            	UTIL.add_error_message_to_popup(DEVICES.device_dialog, "You may only designate one category as the Allocation Category.");
            	return false;
            }
            
            if(cost_allocation_option && allocation_category_count == 0) {
            	UTIL.add_error_message_to_popup(DEVICES.device_dialog, "You must designate a cost category as the Allocation Category.");
            	return false;
            }

            var properties = [];
            $(DEVICES.update.device_property_container + " .cloned-property").each(function() {
                var $prop = $(this); 
                var property_type = $prop.find(DEVICES.update.device_property_type_field).val();
                var data_type = $prop.find(DEVICES.update.device_property_type_field).find(":selected").data("type");
                var unit_count = $prop.find(DEVICES.update.device_property_number_field).val();
                var unit_type = $prop.find(DEVICES.update.device_property_unit_type_field).val();
                var str_value = $prop.find(DEVICES.update.device_property_string_field).val();
                
                if(property_type) {
                    var property = {};
                    if(data_type == "string") {
                            property = { type:property_type, strValue:str_value };
                    } else {
                            property = { type:property_type, unitCount:unit_count, unitType:unit_type };
                    }
                    properties.push(property);
                }
            });
			
			var json = { "partNumber":part_number, "description":description, archived:archived, activateSyncEnabled:activate_sync, defaultOspId:default_osp_id, deviceType:device_type, isCI:is_ci, activateAddBusinessService:add_business_service, pricingSheetEnabled:pricing_sheet_enabled, requireUnitCount:require_unit_count, costAllocationOption:cost_allocation_option, splaCosts:spla_costs, relatedDevices:related_devices, properties:properties, costMappings:cost_mappings, pricingSyncEnabled:pricing_sync_enabled, catalogRecurringCost:catalog_recurring_cost, catalogRecurringPrice:catalog_recurring_price, productId:product_id, altId:alt_id };
			
			if(DEVICES.update.current_id) {
				json["id"] = DEVICES.update.current_id;
				ajax_type = "PUT";
			}
			
			json = UTIL.remove_null_properties(json);
			UTIL.add_dialog_loader(DEVICES.device_dialog);
			
			$.ajax ({
				url: PAGE_CONSTANTS.BASE_URL + "settings/devices.json",
				data: JSON.stringify(json),
				type: ajax_type,
				success:function(data) {
					UTIL.remove_dialog_loader(DEVICES.device_dialog);
					
					if(data.status == PAGE_CONSTANTS.OK_STS) {
						UTIL.add_success_message_to_popup(DEVICES.device_dialog, $(DEVICES.update.changes_saved_msg).val());
						$(DEVICES.device_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").hide();
						$(DEVICES.device_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button:contains('OK')").show();
						
						//update devices
						DEVICES.read.get_devices();
					} else {
						UTIL.add_error_message_to_popup(DEVICES.device_dialog, data.message);
					}
				}
			});
		}
	},
	del:{
		current_id:null,
		popup_class:".delete-device",
		name_display:"#delete-device-name",
		deleted_msg:"#device-deleted-msg",
		init:function() {
			DEVICES.del.bind_events();
		},
		bind_events:function() {
			$(DEVICES.delete_device_dialog).dialog({
				  autoOpen:false,
				  dialogClass:"device-dialog",	
			      resizable:false,
			      width:720,
			      height:450,
			      modal:true,
			      title: "Delete Device",
			      open:function() {
			    	  $('.datepicker').datepicker('enable');
			    	  $(this).closest(".ui-dialog").find(".ui-button:contains('OK')").hide();
			    	  $(this).closest(".ui-dialog").find(".ui-button:contains('Delete')").show();
			    	  $(this).closest(".ui-dialog").find(".ui-button:contains('Cancel')").show();
			      },
			      close:function() {
			    	  $('.datepicker').datepicker('disable');
			      },
			      buttons: {
			        "Delete":function() {
			        	DEVICES.del.submit_delete();
			        },
			        "Cancel":function() {
			            $(this).dialog("close");
			        },
			        "OK":function() {
			            $(this).dialog("close");
			        }
			      },
			      create:function () {
			    	  $(this).closest(".ui-dialog").find(".ui-button:contains('Cancel')").addClass("cancel");
			    	  $(this).closest(".ui-dialog").find(".ui-button:contains('OK')").hide();
			      }
			});
			
			$(document).on("click", DEVICES.del.popup_class, function() {
				var id = $(this).data("id");
				(id == "add") ? DEVICES.del.current_id = null : DEVICES.del.current_id = id;
				DEVICES.del.reset_popup();
			});
		},
		reset_popup:function() {
			UTIL.clear_message_in_popup(DEVICES.delete_device_dialog);
			
			var description = "";
			var part_number = "";
			
			if(DEVICES.del.current_id) {
				var device = $.grep(DEVICES.read.devices, function(n) { return n.id == DEVICES.del.current_id; });
				device = device[0];
				
				if(device) {
					description = device.description;
					part_number = device.partNumber;
				}
			}
			
			$(DEVICES.del.name_display).html(description + " (" + part_number + ")");
		},
		submit_delete:function() {
			UTIL.add_dialog_loader(DEVICES.delete_device_dialog);
			
			$.ajax ({
				url: PAGE_CONSTANTS.BASE_URL + "settings/devices/" + DEVICES.del.current_id + ".json",
				type: "DELETE",
				success:function(data) {
					UTIL.remove_dialog_loader(DEVICES.delete_device_dialog);
					
					if(data.status == PAGE_CONSTANTS.OK_STS) {
						UTIL.add_success_message_to_popup(DEVICES.delete_device_dialog, $(DEVICES.del.deleted_msg).val());
						$(DEVICES.delete_device_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").hide();
						$(DEVICES.delete_device_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button:contains('OK')").show();
						
						//update devices
						DEVICES.read.get_devices();
					} else {
						UTIL.add_error_message_to_popup(DEVICES.delete_device_dialog, data.message);
					}
				}
			});
		}
	},
	merge:{
		merge_device_dialog:"#merge-device-dialog",
		merge_device_from:"#merge-device-from",
		merge_device_into:"#merge-device-into",
		init:function() {
			DEVICES.merge.bind_events();
		},
		bind_events:function() {
			$(DEVICES.merge.merge_device_dialog).dialog({
				  autoOpen:false,
				  dialogClass:"device-dialog",	
			      resizable:false,
			      width:850,
			      height:450,
			      modal:true,
			      title: "Merge Device",
			      open:function() {
			    	  $('.datepicker').datepicker('enable');
			    	  $(this).closest(".ui-dialog").find(".ui-button:contains('OK')").hide();
			    	  $(this).closest(".ui-dialog").find(".ui-button:contains('Merge')").show();
			    	  $(this).closest(".ui-dialog").find(".ui-button:contains('Cancel')").show();
			    	  DEVICES.merge.reset_popup();
			      },
			      close:function() {
			    	  $('.datepicker').datepicker('disable');
			      },
			      buttons: {
			        "Merge":function() {
			        	DEVICES.merge.submit_merge();
			        },
			        "Cancel":function() {
			            $(this).dialog("close");
			        },
			        "OK":function() {
			            $(this).dialog("close");
			        }
			      },
			      create:function () {
			    	  $(this).closest(".ui-dialog").find(".ui-button:contains('Cancel')").addClass("cancel");
			    	  $(this).closest(".ui-dialog").find(".ui-button:contains('OK')").hide();
			      }
			});
		},
		reset_popup:function() {
			$(DEVICES.merge.merge_device_from).html("");
			$(DEVICES.merge.merge_device_into).html("");
			
			DEVICES.read.get_devices("merge");
		},
		build_dropdowns:function(devices) {
			var output = "<option value=\"\"></option>";
			
			for(var i=0; i < devices.length; i++) {
				var device = devices[i];
				output += "<option value=\"" + device.id + "\">" + device.description + " (" + device.partNumber + ")" + " -- ID: " + device.id + "</option>";
			}
			
			$(DEVICES.merge.merge_device_from).html(output);
			$(DEVICES.merge.merge_device_into).html(output);
		},
		submit_merge:function() {
			UTIL.clear_message_in_popup(DEVICES.merge.merge_device_dialog);
			
			var old_device_id = $(DEVICES.merge.merge_device_from).val();
			var new_device_id = $(DEVICES.merge.merge_device_into).val();
			
			if(!old_device_id || !new_device_id) {
				UTIL.add_error_message_to_popup(DEVICES.merge.merge_device_dialog, $(DEVICES.general_validation_error).val());
				return false;
			}
			
			UTIL.add_dialog_loader(DEVICES.merge.merge_device_dialog);
			
			$.ajax ({
				url: PAGE_CONSTANTS.BASE_URL + "settings/devices/merge.json?odid=" + old_device_id + "&ndid=" + new_device_id,
				type: "POST",
				success:function(data) {
					UTIL.remove_dialog_loader(DEVICES.merge.merge_device_dialog);
					
					if(data.status == PAGE_CONSTANTS.OK_STS) {
						UTIL.add_success_message_to_popup(DEVICES.merge.merge_device_dialog, "Devices successfully merged.");
						$(DEVICES.merge.merge_device_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").hide();
						$(DEVICES.merge.merge_device_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button:contains('OK')").show();
						
						//update devices
						DEVICES.read.get_devices();
					} else {
						UTIL.add_error_message_to_popup(DEVICES.merge.merge_device_dialog, data.message);
					}
				}
			});
		}
	}
};

$(document).ready(function() {
	DEVICES.init();
});