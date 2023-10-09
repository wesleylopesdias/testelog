var MAP = {
	init:function() {
		MAP.mappings.init();
	},
	bind_events:function() {
		
	},
	mappings:{
		mappings_table:"#cost-mappings-table",
		mappings_dialog:"#cost-mappings-dialog",
		popup_class:".mapping-popup-link",
		init:function() {
			MAP.mappings.bind_events();
			MAP.mappings.modify.init();
			MAP.mappings.load.init();
		},
		bind_events:function() {
			
		},
		load:{
			init:function() {
				MAP.mappings.load.get_services();
				MAP.mappings.load.sort_categories();
			},
			bind_events:function() {
				
			},
			sort_categories:function() {
				var options = $(MAP.mappings.modify.clone_data_row).find(MAP.mappings.modify.expense_category_field + " option");
				var arr = options.map(function(_, o) { return { t: $(o).text(), v: o.value, u:$(o).data("units"), c:$(o).data("unit-cost") }; }).get();
				arr.sort(function(o1, o2) { 
					var t1 = o1.t.toLowerCase(), t2 = o2.t.toLowerCase();
					return t1 > t2 ? 1 : t1 < t2 ? -1 : 0;
				});
				options.each(function(i, o) {
				  o.value = arr[i].v;
				  $(o).text(arr[i].t);
				  $(o).attr("data-units", arr[i].u);
				  $(o).attr("data-unit-cost", arr[i].c);
				});
			},
			get_services:function() {
				UTIL.add_table_loader(MAP.mappings.mappings_table);
				
				$.ajax ({
					url: PAGE_CONSTANTS.BASE_URL + "services.json",
					type: "GET",
					success:function(data) {
						MAP.mappings.load.build_table(data);
					}
				});
			},
			build_table:function(services) {
				var output = "";
				if(services && services.length > 0) {
					for(var i=0; i < services.length; i++) {
						var service = services[i];
						output += "<tr>";
						output += "<td><a href=\"javascript:;\" class=\"popup-link mapping-popup-link\" data-dialog=\"cost-mappings-dialog\" data-service-id=\"" + service.serviceId + "\" data-osp-id=\"" + service.ospId + "\" data-service-name=\"" + service.name + "\">" + service.name + "</a></td>";
						output += "<td class=\"right\">" + service.costMappingsCount + "</td>";
						output += "</tr>";
					}
				} else {
					output = "<tr><td colspan=\"2\">No services were returned.</td></tr>";
				}
				
				$(MAP.mappings.mappings_table + " tbody").html(output);
			}
		},
		modify:{
			service_name_display:"#cost-mappings-service-name",
			mappings_table_container:"#mappings-table-container",
			modify_mappings_table:"#modify-mappings-table",
			expense_category_field:"select[name='cost-asset-category']",
			unit_quantity_field:"input[name='unit-quantity']",
			row_unit_type:".unit-type",
			row_unit_cost:".unit-cost",
			clone_data_row:"#clone-row",
			add_row:"#add-row",
			remove_row:".remove-row",
			loader:"#table-loader",
			current_service_id:null,
			current_osp_id:null,
			mapping_saved_msg:"#ok-cost-mapping-saved-msg",
			quantity_zero_msg:"#error-cost-mapping-quantity-zero",
			general_required_fields_msg:"#general-error-msg",
			init:function() {
				MAP.mappings.modify.bind_events();
			},
			bind_events:function() {
				$(MAP.mappings.mappings_dialog).dialog({
					  autoOpen:false,
					  dialogClass:"mappings-dialog",	
				      resizable:false,
				      width:780,
				      height:700,
				      modal:true,
				      title: "Map Cost Categories To A Service",
				      open:function() {
				    	  $('.datepicker').datepicker('enable');
				    	  $(this).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").show();
						  $(this).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button:contains('OK')").hide();
						  $(MAP.mappings.modify.loader).show();
						  $(MAP.mappings.modify.mappings_table_container).hide();
				      },
				      close:function() {
				    	  $('.datepicker').datepicker('disable');
				      },
				      buttons: {
				        "Save":function() {
				        	MAP.mappings.modify.submit_mappings();
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
				
				$(document).on("click", MAP.mappings.popup_class, function() {
					var name = $(this).data("service-name");
					MAP.mappings.modify.current_service_id = $(this).data("service-id");
					MAP.mappings.modify.current_osp_id = $(this).data("osp-id");
					MAP.mappings.modify.get_mappings();
					MAP.mappings.modify.reset_popup(name);
				});
				
				$(MAP.mappings.modify.add_row).click(function() {
					MAP.mappings.modify.clone_row();
				});
				
				$(document).on("click", MAP.mappings.modify.remove_row,function() {
					//remove the row
					$(this).parent().parent().remove();
					
					var rows = $(MAP.mappings.modify.modify_mappings_table + " tr").length;
					if(rows == 2) {
						//add a clean row if it's the last one
						MAP.mappings.modify.clone_row();
					}
				});
				
				$(document).on("click", MAP.mappings.modify.expense_category_field, function() {
					//remove the row
					var $row = $(this).parents("tr");
					var $option = $(MAP.mappings.modify.expense_category_field + " option:selected");
					
					var $option = $row.find(MAP.mappings.modify.expense_category_field + " option:selected");
					$row.find(MAP.mappings.modify.row_unit_type).html($option.data("units"));
					var cost = $option.data("unit-cost");
					if(cost) cost = UTIL.convert_currency_for_ui(cost);
					$row.find(MAP.mappings.modify.row_unit_cost).html(cost);
				});
			},
			get_mappings:function() {
				$.ajax ({
					url: PAGE_CONSTANTS.BASE_URL + "services/costmappings/" + MAP.mappings.modify.current_osp_id + ".json",
					type: "GET",
					success:function(data) {
						MAP.mappings.modify.build_table(data);
						$(MAP.mappings.modify.loader).hide();
					}
				});
			},
			build_table:function(mappings) {
				MAP.mappings.modify.reset_table();
				
				if(mappings && mappings.length > 0) {
					for(var i=0; i < mappings.length; i++) {
						if(i > 0) MAP.mappings.modify.clone_row();
						var mapping = mappings[i];
						var $row = $(MAP.mappings.modify.modify_mappings_table + " tr:last-child");
						$row.find(MAP.mappings.modify.expense_category_field).val(mapping.expenseCategoryId);
						$row.find(MAP.mappings.modify.unit_quantity_field).val(mapping.quantity);
						
						var $option = $row.find(MAP.mappings.modify.expense_category_field + " option:selected");
						$row.find(MAP.mappings.modify.row_unit_type).html($option.data("units"));
						var cost = $option.data("unit-cost");
						if(cost) cost = UTIL.convert_currency_for_ui(cost);
						$row.find(MAP.mappings.modify.row_unit_cost).html(cost);
					}
				}
				
				$(MAP.mappings.modify.mappings_table_container).show();
			},
			reset_popup:function(name) {
				UTIL.clear_message_in_popup(MAP.mappings.mappings_dialog);
				
				$(MAP.mappings.modify.service_name_display).html(name);
			},
			reset_table:function() {
				$(MAP.mappings.modify.modify_mappings_table + " tbody tr").each(function() {
					var $row = $(this);
					if($row.attr("id") != "clone-row") $row.remove();
				});
				MAP.mappings.modify.clone_row();
			},
			clone_row:function() {
				var $clone = $(MAP.mappings.modify.clone_data_row).clone().attr("id","").show();
				$(MAP.mappings.modify.modify_mappings_table + " tbody").append($clone);
			},
			submit_mappings:function() {
				UTIL.clear_message_in_popup(MAP.mappings.mappings_dialog);
				
				var json = [];
				
				var is_valid = true;
				var message = "";
				$(MAP.mappings.modify.modify_mappings_table + " tbody tr:visible").each(function() {
					var $row = $(this);
					
					var expense_category_id = $row.find(MAP.mappings.modify.expense_category_field).val();
					var quantity = $row.find(MAP.mappings.modify.unit_quantity_field).val();
					
					//validate!
					if (expense_category_id && (!quantity || quantity == 0)) {
						is_valid = false;
						message = $(MAP.mappings.modify.quantity_zero_msg).val();
					}
					
					var record = { "expenseCategoryId":expense_category_id, "ospId":MAP.mappings.modify.current_osp_id, "serviceOfferingId":MAP.mappings.modify.current_service_id, "quantity":quantity };
					
					//prepare data before submit
					record = UTIL.remove_null_properties(record);
					json.push(record);
				});
				
				if(!is_valid) {
					UTIL.add_error_message_to_popup(MAP.mappings.mappings_dialog, message);
					return false;
				}
				
				UTIL.add_dialog_loader(MAP.mappings.mappings_dialog);
				
				$.ajax ({
					url: PAGE_CONSTANTS.BASE_URL + "services/mapcosts.json",
					data: JSON.stringify(json),
					type: "POST",
					success:function(data) {
						UTIL.remove_dialog_loader(MAP.mappings.mappings_dialog);
						
						var success_msg = $(MAP.mappings.modify.mapping_saved_msg).val();
						UTIL.add_success_message_to_popup(MAP.mappings.mappings_dialog, success_msg);
						$(MAP.mappings.mappings_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").hide();
						$(MAP.mappings.mappings_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button:contains('OK')").show();
						MAP.mappings.load.get_services();
					}
				});
			}
		}
	}
};

$(document).ready(function() {
	MAP.init();
});