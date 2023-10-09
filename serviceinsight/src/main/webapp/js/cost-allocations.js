var ALLO = {
	current_id:null,
	service:[],
	allocation:null,
        for_month_of:"#for-month-of",
	init:function() {
		ALLO.bind_events();
		ALLO.dates.init();
		ALLO.read.init($(ALLO.for_month_of).val());
		ALLO.create.init();
		ALLO.update.init();
		ALLO.generate.init();
		ALLO.import.init();
		ALLO.spread.init();
	},
	bind_events:function() {
		
	},
	dates:{
		format:"MM/DD/YYYY",
		display_format:"MMM YYYY",
		init:function() {
			ALLO.dates.bind_events();
			ALLO.dates.setup_dates();
		},
		bind_events:function() {
			
		},
		setup_dates:function() {
			var output = "";
			
			//var start_month_moment = moment().subtract(12, "months");
			var start_month_moment = moment("12/01/2016",ALLO.dates.format);
			var end_month_moment = moment();
			while(end_month_moment > start_month_moment) {
				output += "<option value=\"" + end_month_moment.startOf("month").format(ALLO.dates.format) + "\">" + end_month_moment.format(ALLO.dates.display_format); + "</option>";
				end_month_moment.subtract(1, "month");
			}

			$(ALLO.update.date_range_field).html(output);
			$(ALLO.import.import_date_field).html(output);
		}
	},
	read:{
		cost_allocation_table:"#cost-allocation-table",
		multi_tenant_compare_total:"#allocation-multi-tenant-allocated-amount",
		rent_compare_total:"#allocation-rent-allocated-amount",
		specific_compare_total:"#allocation-specific-allocated-amount",
		specific_services_dialog:"#allocation-specific-services-dialog",
		specific_services_list:"#allocation-specific-services-list",
		init:function(forMonthOf) {
			ALLO.read.bind_events();
			ALLO.read.get_services();
			ALLO.read.get_allocation(forMonthOf);
		},
		bind_events:function() {
			$(ALLO.update.date_range_field).change(function() {
				ALLO.read.get_allocation();
				ALLO.create.get_devices();
			});
			
			$(ALLO.read.specific_services_dialog).dialog({
				  autoOpen:false,
				  dialogClass:"device-dialog",	
			      resizable:false,
			      width:720,
			      height:450,
			      modal:true,
			      title: "Specific Expenses",
			      open:function() {
			    	  if(ALLO.allocation) ALLO.read.build_specific_services_list(ALLO.allocation.unallocatedExpenses);
			    	  $('.datepicker').datepicker('enable');
			      },
			      close:function() {
			    	  $('.datepicker').datepicker('disable');
			      },
			      buttons: {
			        "OK":function() {
			            $(this).dialog("close");
			        }
			      },
			      create:function () {
			    	  
			      }
			});
		},
		get_services:function() {
			$.ajax ({
				url: PAGE_CONSTANTS.BASE_URL + "services.json",
				type: "GET",
				success:function(data) {
					//DEVICES.read.devices = data;
					ALLO.services = data;
				}
			});
		},
		get_allocation:function(forMonthOf) {
            var month = null;
            if (forMonthOf && forMonthOf !== '') {
                month = forMonthOf;
                $(ALLO.update.date_range_field + " option[value='"+forMonthOf+"']").prop('selected', true);
            } else {
                month = $(ALLO.update.date_range_field).val();
            }
			ALLO.current_id = null;
			
			$(ALLO.update.status_msg).hide();
			UTIL.add_table_loader(ALLO.read.cost_allocation_table);
			
			$.ajax ({
				url: PAGE_CONSTANTS.BASE_URL + "costs/allocation.json?month=" + month,
				type: "GET",
				success:function(data) {
					ALLO.read.build_cost_allocation(data);
					ALLO.current_id = data.id;
					ALLO.allocation = data;
				}
			});
		},
		build_cost_allocation:function(allocation, update_only) {
			ALLO.read.build_cost_allocation_totals(allocation);
			if(update_only) {
				ALLO.read.update_lineitems(allocation.lineItems);
			} else {
				ALLO.read.build_lineitems(allocation.lineItems, allocation.status);
			}
			ALLO.read.set_status(allocation.status);
			ALLO.read.build_footer(allocation);			
		},
		set_status:function(status) {
			if(status == "processed") {
				$(".open-status").hide();
				$("input[type='text']").prop("disabled", true);
				$(".processed-status").show();
			} else {
				$(".open-status").show();
				$("input[type='text']").prop("disabled", false);
				$(".processed-status").hide();
			}
		},
		build_cost_allocation_totals:function(allocation) {
			$(ALLO.update.multi_tenant_total_field).val(UTIL.convert_decimal_for_ui(allocation.multiTenantTotal));
			$(ALLO.update.rent_total_field).val(UTIL.convert_decimal_for_ui(allocation.rentTotal));
			
			var specific_total = allocation.specificTotal;
			if(!specific_total) {
				specific_total = allocation.specificTotalFromUnallocatedExpenses;
			}
			$(ALLO.update.specific_total_display).html(UTIL.convert_currency_for_ui(specific_total));
			
			/*var dedicated_total = allocation.dedicatedTotal;
			if(!dedicated_total) {
				dedicated_total = allocation.dedicatedTotalFromCostItems;
			}*/
			var dedicated_total = allocation.dedicatedTotalFromCostItems;
			$(ALLO.update.dedicated_total_display).html(UTIL.convert_currency_for_ui(dedicated_total));
			
			//build comparisons
			var multi_tenant_compare = "";
			if(allocation.multiTenantAllocatedAmountTotal == allocation.multiTenantTotal) {
				multi_tenant_compare = "<i class=\"fa fa-check-circle allocation-compare-icon\" title=\"Amount 100% Allocated\"></i> ";
			}
			multi_tenant_compare += UTIL.convert_currency_for_ui(allocation.multiTenantAllocatedAmountTotal);
			
			var rent_compare = "";
			if(allocation.rentAllocatedAmountTotal == allocation.rentTotal) {
				rent_compare = "<i class=\"fa fa-check-circle pull-left allocation-compare-icon\" title=\"Amount 100% Allocated\"></i> ";
			}
			rent_compare += UTIL.convert_currency_for_ui(allocation.rentAllocatedAmountTotal);
			
			var specific_compare = "";
			if(allocation.specificAllocatedAmountTotal == specific_total) {
				specific_compare = "<i class=\"fa fa-check-circle pull-left allocation-compare-icon\" title=\"Amount 100% Allocated\"></i> ";
			}
			specific_compare += UTIL.convert_currency_for_ui(allocation.specificAllocatedAmountTotal);
			
			
			$(ALLO.read.multi_tenant_compare_total).html(multi_tenant_compare);
			$(ALLO.read.rent_compare_total).html(rent_compare);
			$(ALLO.read.specific_compare_total).html(specific_compare);
			
		},
		update_lineitems:function(lineitems) {
			if(lineitems && lineitems.length > 0) {
				for(var i = 0; i < lineitems.length;i++) {
					var lineitem = lineitems[i];
					
					var $row = $("#lineitem-" + lineitem.deviceId);
					$row.find(ALLO.update.lineitem_multi_tenant_allocation_field).val(UTIL.convert_decimal_for_ui(lineitem.multiTenantAllocation));
					$row.find(ALLO.update.lineitem_rent_allocation_field).val(UTIL.convert_decimal_for_ui(lineitem.rentAllocation));
					$row.find(ALLO.update.lineitem_specific_allocation_field).val(UTIL.convert_decimal_for_ui(lineitem.specificAllocation));
					$row.find(ALLO.update.lineitem_multi_tenant_amount_column).html(UTIL.convert_currency_for_ui(lineitem.multiTenantAmount));
					$row.find(ALLO.update.lineitem_rent_amount_column).html(UTIL.convert_currency_for_ui(lineitem.rentAmount));
					$row.find(ALLO.update.lineitem_specific_amount_column).html(UTIL.convert_currency_for_ui(lineitem.specificAmount));
					$row.find(ALLO.update.lineitem_total_cost_column).html(UTIL.convert_currency_for_ui(lineitem.totalAmount));
					$row.find(ALLO.update.lineitem_unit_cost_column).html(UTIL.convert_currency_for_ui(lineitem.calculatedUnitCost));
					
					$row.find(ALLO.update.lineitem_cost_model_per_unit_field).val(UTIL.convert_decimal_for_ui(lineitem.costModelPerUnit));
					$row.find(ALLO.update.lineitem_variance_column).html(UTIL.convert_decimal_to_percent(lineitem.variance, 2)+ "%");
				}
			}
		},
		build_lineitems:function(lineitems, status) {
			var output = "";
			
			if(lineitems && lineitems.length > 0) {
				var previous_id = null;
				var row_class = "even";
				for(var i = 0; i < lineitems.length;i++) {
					var lineitem = lineitems[i];
					var show_specific = false;
					if(previous_id == null || previous_id != lineitem.ospId) {
						show_specific = true;
						previous_id = lineitem.ospId;
						if(row_class == "odd") {
							row_class = "even"
						} else {
							row_class = "odd"
						}
					}
					
					output += ALLO.read.build_lineitem(lineitem, show_specific, row_class);
				}
				
				$(ALLO.import.import_lineitems_btn).hide();
			} else {
				if(status != "processed") {
					$(ALLO.import.import_lineitems_btn).show();
				}
			}
			
			$(ALLO.read.cost_allocation_table + " tbody").html(output);
		},
		build_lineitem:function(lineitem, show_specific, row_class) {
			var output = "";
			
			var specific_amount = "";
			if(show_specific) specific_amount = UTIL.convert_currency_for_ui(lineitem.serviceSpecificTotal);
			var infrastructure_note = "";
			if(lineitem.infrastructureNote) infrastructure_note = lineitem.infrastructureNote;
			
			output += "<tr class=\"" + row_class + "\" id=\"lineitem-" + lineitem.deviceId + "\">";
			output += "<td>";
			output += lineitem.serviceName;
			output += "<input type=\"hidden\" name=\"lineitem-id\" value=\"" + lineitem.id + "\" />";
			output += "<input type=\"hidden\" name=\"lineitem-osp-id\" value=\"" + lineitem.ospId + "\" />";
			output += "<input type=\"hidden\" name=\"lineitem-service-name\" value=\"" + lineitem.serviceName + "\" />";
			output += "<input type=\"hidden\" name=\"lineitem-device-id\" value=\"" + lineitem.deviceId + "\" />";
			output += "<input type=\"hidden\" name=\"lineitem-device-part-number\" value=\"" + lineitem.devicePartNumber + "\" />";
			output += "<input type=\"hidden\" name=\"lineitem-device-description\" value=\"" + lineitem.deviceDescription + "\" />";
			output += "<input type=\"hidden\" name=\"lineitem-units\" value=\"" + lineitem.units + "\" />";
			output += "</td>";
			output += "<td>" + lineitem.deviceDescription + " <div style=\"color:#888;\">" + lineitem.devicePartNumber + "</div></td>"
			output += "<td><input type=\"text\" size=\"15\" name=\"lineitem-infrastructure-note\" value=\"" + infrastructure_note + "\" /></td>";
			output += "<td class=\"right col-1\"><input type=\"text\" size=\"5\" name=\"lineitem-multi-tenant-allocation\" class=\"currency right\" value=\"" + UTIL.convert_decimal_for_ui(lineitem.multiTenantAllocation) + "\" />%</td>";
			output += "<td class=\"right col-1\" name=\"lineitem-multi-tenant-amount\">" + UTIL.convert_currency_for_ui(lineitem.multiTenantAmount) + "</td>";
			output += "<td class=\"right col-2\"><input type=\"text\" size=\"5\" name=\"lineitem-rent-allocation\" class=\"currency right\" value=\"" + UTIL.convert_decimal_for_ui(lineitem.rentAllocation) + "\" />%</td>";
			output += "<td class=\"right col-2\" name=\"lineitem-rent-amount\">" + UTIL.convert_currency_for_ui(lineitem.rentAmount) + "</td>";
			output += "<td class=\"right col-3\">" + specific_amount + "</td>";
			output += "<td class=\"right col-3\"><input type=\"text\" size=\"5\" name=\"lineitem-specific-allocation\" class=\"currency right\" value=\"" + UTIL.convert_decimal_for_ui(lineitem.specificAllocation) + "\" />%</td>";
			output += "<td class=\"right col-3\" name=\"lineitem-specific-amount\">" + UTIL.convert_currency_for_ui(lineitem.specificAmount) + "</td>";
			output += "<td class=\"right\" name=\"lineitem-total-cost\">" + UTIL.convert_currency_for_ui(lineitem.totalAmount) + "</td>";
			output += "<td class=\"right\" name=\"lineitem-units\">" + lineitem.units + "</td>";
			output += "<td class=\"right\" name=\"lineitem-unit-cost\">" + UTIL.convert_currency_for_ui(lineitem.calculatedUnitCost) + "</td>";
			output += "<td class=\"right\" name=\"lineitem-cost-model-per-unit\">" + UTIL.convert_currency_for_ui(lineitem.costModelPerUnit) + "</td>";
			output += "<td class=\"right\" name=\"lineitem-variance\">" + UTIL.convert_decimal_to_percent(lineitem.variance, 2) + "%</td>";
			output += "<td><a href=\"javascript:;\" class=\"delete-lineitem-btn open-status\"><i class=\"fa fa-minus-circle\"></i></a></td>";
			output += "</tr>";
			
			return output;
		},
		build_footer:function(allocation) {
			var output = "";
			
			output += "<tr class=\"total-row\">";
			output += "<td></td>";
			output += "<td></td>";
			output += "<td class=\"right\">TOTAL</td>";
			output += "<td class=\"right col-1\">" + UTIL.convert_decimal_for_ui(allocation.multiTenantAllocationTotal) + "%</td>";
			output += "<td class=\"right col-1\">" + UTIL.convert_currency_for_ui(allocation.multiTenantAllocatedAmountTotal) + "</td>";
			output += "<td class=\"right col-2\">" + UTIL.convert_decimal_for_ui(allocation.rentAllocationTotal) + "%</td>";
			output += "<td class=\"right col-2\">" + UTIL.convert_currency_for_ui(allocation.rentAllocatedAmountTotal) + "</td>";
			output += "<td class=\"right col-3\"></td>";
			output += "<td class=\"right col-3\"></td>";
			output += "<td class=\"right col-3\">" + UTIL.convert_currency_for_ui(allocation.specificAllocatedAmountTotal) + "</td>";
			output += "<td class=\"right\"></td>";
			output += "<td class=\"right\"></td>";
			output += "<td class=\"right\"></td>";
			output += "<td></td>";
			output += "<td></td>";
			output += "<td></td>";
			output += "</tr>";
			
			$(ALLO.read.cost_allocation_table + " tfoot").html(output);
		},
		build_specific_services_list:function(unallocated_expenses) {
			var output = "";
			
			var services = [];
			if(unallocated_expenses != null && unallocated_expenses.length > 0) {
				output = "<ul>"
				for(var i = 0; i < unallocated_expenses.length; i++) {
					var unallocated_expense = unallocated_expenses[i];
					var service = $.grep(ALLO.services, function(n) { return n.ospId == unallocated_expense.ospId; });
					service = service[0];
					service_name = service.name;
					
					var service_cost = $.grep(services, function(n) { return n.ospId == unallocated_expense.ospId; });
					if(service_cost != null && service_cost.length > 0) {	
						service_cost = service_cost[0];
						service_cost.amount = service_cost.amount + unallocated_expense.amount;
					} else {
						services.push({ name:service_name, ospId:unallocated_expense.ospId, amount:unallocated_expense.amount });
					}
					
				}
				
				for(var i = 0; i < services.length; i++) {
					var service = services[i];
					output += "<li>" + service.name + " -- " + UTIL.convert_currency_for_ui(service.amount) + "</li>";
				}
				output += "</ul>";
			} else {
				output += "<span class=\"no-results\">No specific allocations found for this month.</span>";
			}
			
			
			$(ALLO.read.specific_services_list).html(output);
		}
	},
	import:{
		import_allocation_dialog:"#import-allocation-dialog",
		import_date_field:"#allocation-import-date-range",
		import_lineitems_btn:"#import-lineitems-btn",
		init:function() {
			ALLO.import.bind_events();
		},
		bind_events:function() {
			$(ALLO.import.import_allocation_dialog).dialog({
				  autoOpen:false,
				  dialogClass:"device-dialog",	
			      resizable:false,
			      width:720,
			      height:450,
			      modal:true,
			      title: "Add a Device",
			      open:function() {
			    	  ALLO.import.reset_popup();
			    	  $('.datepicker').datepicker('enable');
			    	  $(this).closest(".ui-dialog").find(".ui-button:contains('OK')").hide();
			    	  $(this).closest(".ui-dialog").find(".ui-button:contains('Delete')").show();
			    	  $(this).closest(".ui-dialog").find(".ui-button:contains('Cancel')").show();
			      },
			      close:function() {
			    	  $('.datepicker').datepicker('disable');
			      },
			      buttons: {
			        "Import":function() {
			        	ALLO.import.submit_import();
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
			UTIL.clear_message_in_popup(ALLO.import.import_allocation_dialog);
		},
		submit_import:function() {
			var import_date = $(ALLO.import.import_date_field).val();
			var multi_tenant_total = $(ALLO.update.multi_tenant_total_field).val();
			var rent_total = $(ALLO.update.rent_total_field).val();
			var specific_total = $(ALLO.update.specific_total_display).html();
			var dedicated_total = $(ALLO.update.dedicated_total_display).html();
			var status = "open";
			var month = $(ALLO.update.date_range_field).val();
			
			month = UTIL.convert_dates_for_server(month);
			specific_total = UTIL.convert_currency_for_server(specific_total);
			dedicated_total = UTIL.convert_currency_for_server(dedicated_total);
			
			var json = { multiTenantTotal:multi_tenant_total, rentTotal:rent_total, specificTotal:specific_total, dedicatedTotal:dedicated_total, status:status, month:month };
			
			UTIL.add_dialog_loader(ALLO.import.import_allocation_dialog);
			$.ajax ({
				url: PAGE_CONSTANTS.BASE_URL + "costs/allocation/import.json?month=" + import_date,
				data: JSON.stringify(json),
				type: "POST",
				success:function(data) {
					UTIL.remove_dialog_loader(ALLO.import.import_allocation_dialog);
					
					if(data.status == PAGE_CONSTANTS.OK_STS) {
						UTIL.add_success_message_to_popup(ALLO.import.import_allocation_dialog, "Import complete!");
						$(ALLO.import.import_allocation_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").hide();
						$(ALLO.import.import_allocation_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button:contains('OK')").show();
						ALLO.read.get_allocation();
					} else {
						UTIL.add_error_message_to_popup(ALLO.import.import_allocation_dialog, data.message);
					}
				}
			});
		}
	},
	create:{
		add_allocation_dialog:"#add-allocation-dialog",
		allocation_device_field:"#allocation-device",
		init:function() {
			ALLO.create.bind_events();
			ALLO.create.get_devices();
		},
		bind_events:function() {
			$(ALLO.create.add_allocation_dialog).dialog({
				  autoOpen:false,
				  dialogClass:"device-dialog",	
			      resizable:false,
			      width:720,
			      height:450,
			      modal:true,
			      title: "Add a Device",
			      open:function() {
			    	  ALLO.create.reset_popup();
			    	  $('.datepicker').datepicker('enable');
			    	  //$(this).closest(".ui-dialog").find(".ui-button:contains('OK')").hide();
			    	  $(this).closest(".ui-dialog").find(".ui-button:contains('Delete')").show();
			    	  //$(this).closest(".ui-dialog").find(".ui-button:contains('Cancel')").show();
			      },
			      close:function() {
			    	  $('.datepicker').datepicker('disable');
			      },
			      buttons: {
			    	"Add All Devices":function() {
				    	ALLO.create.add_all_rows();
				    },
			        "Add Device":function() {
			        	ALLO.create.add_new_row();
			        },
			        "OK":function() {
			            $(this).dialog("close");
			        }
			      },
			      create:function () {
			    	  //$(this).closest(".ui-dialog").find(".ui-button:contains('Cancel')").addClass("cancel");
			    	  //$(this).closest(".ui-dialog").find(".ui-button:contains('OK')").hide();
			      }
			});
		},
		get_devices:function() {
			var month = $(ALLO.update.date_range_field).val();
			
			$.ajax ({
				url: PAGE_CONSTANTS.BASE_URL + "costs/allocation/devices.json?month=" + month,
				type: "GET",
				success:function(data) {
					ALLO.create.populate_device_dropdown(data);
				}
			});
		},
		populate_device_dropdown:function(devices) {
			var output = "<option value=\"\"></option>";
			for(var i = 0; i < devices.length; i++) {
				var device = devices[i];
				output += "<option value=\"" + device.id + "\" data-units=\"" + device.unitCount + "\" data-osp-id=\"" + device.defaultOspId + "\" data-description=\"" + device.description + "\" data-part-number=\"" + device.partNumber + "\" data-cost-model-per-unit=\"" + device.catalogRecurringCost + "\">" + device.description + "(" + device.partNumber + ")</option>";
			}
			$(ALLO.create.allocation_device_field).html(output);
		},
		reset_popup:function() {
			UTIL.clear_message_in_popup(ALLO.create.add_allocation_dialog);
			$(ALLO.create.allocation_device_field).val("");
			
		},
		add_all_rows:function() {
			$(ALLO.create.allocation_device_field + " option").each(function() {
				var $selected = $(this);
				if($selected.val() != "") {
					ALLO.create.add_new_row($selected);
				}
			});
		},
		add_new_row:function($selected) {
			UTIL.clear_message_in_popup(ALLO.create.add_allocation_dialog);
			
			if(!$selected) $selected = $(ALLO.create.allocation_device_field + " option:selected");
			var device_id = $selected.val();
			var osp_id = $selected.data("osp-id");
			var part_number = $selected.data("part-number");
			var description = $selected.data("description");
			var cost_model_per_unit = $selected.data("cost-model-per-unit");
			var units = $selected.data("units");
			var service_name = "";
			
			if(ALLO.allocation && ALLO.allocation.lineItems) {
				var existing_lineitems = ALLO.allocation.lineItems;
				for(var i= 0; i < existing_lineitems.length; i++) {
					var existing_lineitem = existing_lineitems[i];
					if(existing_lineitem.deviceId == device_id) {
						UTIL.add_error_message_to_popup(ALLO.create.add_allocation_dialog, "That part number is already in this allocation.");
						return false;
					}
				}
			}
			
			if(!osp_id) {
				UTIL.add_error_message_to_popup(ALLO.create.add_allocation_dialog, "This device must have a default OSP Service assigned to it before it can be added here.");
			}
			
			var service = $.grep(ALLO.services, function(n) { return n.ospId == osp_id; });
			service = service[0];
			service_name = service.name;
			
			var lineitem = { ospId:osp_id, deviceId:device_id, devicePartNumber:part_number, deviceDescription:description, units:units, serviceName:service_name, costModelPerUnit:cost_model_per_unit };
			
			var output = ALLO.read.build_lineitem(lineitem);
			$(ALLO.read.cost_allocation_table + " tbody").append(output);
			
			UTIL.add_success_message_to_popup(ALLO.create.add_allocation_dialog, "Row successfully added");
			
			ALLO.update.submit_cost_allocation(true);
		}
	},
	update:{
		date_range_field:"#allocation-date-range",
		multi_tenant_total_field:"#allocation-multi-tenant-total",
		rent_total_field:"#allocation-rent-total",
		specific_total_display:"#allocation-specific-total",
		dedicated_total_display:"#allocation-dedicated-total",
		lineitem_id_field:"input[name='lineitem-id']",
		lineitem_osp_id_field:"input[name='lineitem-osp-id']",
		lineitem_service_name_field:"input[name='lineitem-service-name']",
		lineitem_device_id_field:"input[name='lineitem-device-id']",
		lineitem_device_part_number_field:"input[name='lineitem-device-part-number']",
		lineitem_device_description_field:"input[name='lineitem-device-description']",
		lineitem_infrastructure_note_field:"input[name='lineitem-infrastructure-note']",
		lineitem_multi_tenant_allocation_field:"input[name='lineitem-multi-tenant-allocation']",
		lineitem_rent_allocation_field:"input[name='lineitem-rent-allocation']",
		lineitem_specific_allocation_field:"input[name='lineitem-specific-allocation']",
		lineitem_units_field:"input[name='lineitem-units']",
		lineitem_cost_model_per_unit_column:"td[name='lineitem-cost-model-per-unit']",
		lineitem_multi_tenant_amount_column:"td[name='lineitem-multi-tenant-amount']",
		lineitem_rent_amount_column:"td[name='lineitem-rent-amount']",
		lineitem_specific_amount_column:"td[name='lineitem-specific-amount']",
		lineitem_total_cost_column:"td[name='lineitem-total-cost']",
		lineitem_units_column:"td[name='lineitem-units']",
		lineitem_unit_cost_column:"td[name='lineitem-unit-cost']",
		lineitem_variance_column:"td[name='lineitem-variance']",
		delete_lineitem_btn:".delete-lineitem-btn",
		add_new_row_btn:"#add-new-row-btn",
		clone_row:"#clone-row",
		device_field:"select[name='device']",
		save_btn:"#save-btn",
		status_msg:"#status-msg",
		loader_container:"#top-loader",
		changed_msg:"#changed-msg",
		init:function() {
			ALLO.update.bind_events();
		},
		bind_events:function() {
			/*
			$(ALLO.update.add_new_row_btn).click(function() {
				ALLO.update.clone_allocation_row();
			});*/
			
			$(document).on("change", ALLO.update.device_field, function() {
				var osp_id = $(this).find("option:selected").data("osp-id");
				var service = $.grep(ALLO.services, function(n) { return n.ospId == osp_id; });
				
				if(service) {
					service = service[0];
					$(this).parent().prev().html(service.name);
				}
			});
			
			$(ALLO.update.save_btn).click(function() {
				ALLO.update.submit_cost_allocation();
			});
			
			
			$(document).on("blur", "input[type='text']", function() {
				ALLO.update.submit_cost_allocation(true, true);
			});
			
			$(document).on("click", ALLO.update.delete_lineitem_btn, function() {
				$(this).parent().parent().remove();
				ALLO.update.submit_cost_allocation(true, false);
			});
		},
		clone_allocation_row:function() {
			var $clone = $(ALLO.update.clone_row).clone().attr("id","").show();
			$(ALLO.read.cost_allocation_table + " tbody").append($clone);
		},
		clean_percentage:function(percent) {
			percent = percent.replace(/[^0-9.]/g, '');
			if(percent > 100) {
				percent = 100;
			} else if(percent < 0) {
				percent = 0;
			}
			return percent;
		},
		submit_cost_allocation:function(calculate, update_only, callback) {
			
			var url = "costs/allocation.json";
			var multi_tenant_total = $(ALLO.update.multi_tenant_total_field).val();
			var rent_total = $(ALLO.update.rent_total_field).val();
			var specific_total = $(ALLO.update.specific_total_display).html();
			var dedicated_total = $(ALLO.update.dedicated_total_display).html();
			var status = "open";
			var month = $(ALLO.update.date_range_field).val();
			var ajax_type = "POST";
			
			month = UTIL.convert_dates_for_server(month);
			specific_total = UTIL.convert_currency_for_server(specific_total);
			dedicated_total = UTIL.convert_currency_for_server(dedicated_total);
			
			var lineitems = [];
			$(ALLO.read.cost_allocation_table + " tbody tr").each(function() {
				var $row = $(this);
				var lineitem_id = $row.find(ALLO.update.lineitem_id_field).val();
				var osp_id = $row.find(ALLO.update.lineitem_osp_id_field).val();
				var service_name = $row.find(ALLO.update.lineitem_service_name_field).val();
				var device_id = $row.find(ALLO.update.lineitem_device_id_field).val();
				var device_part_number = $row.find(ALLO.update.lineitem_device_part_number_field).val();
				var device_description = $row.find(ALLO.update.lineitem_device_description_field).val();
				var infrastructure_note = $row.find(ALLO.update.lineitem_infrastructure_note_field).val();
				var multi_tenant_allocation = $row.find(ALLO.update.lineitem_multi_tenant_allocation_field).val();
				var rent_allocation = $row.find(ALLO.update.lineitem_rent_allocation_field).val();
				var specific_allocation = $row.find(ALLO.update.lineitem_specific_allocation_field).val();
				var multi_tenant_amount = $row.find(ALLO.update.lineitem_multi_tenant_amount_column).html();
				var rent_amount = $row.find(ALLO.update.lineitem_rent_amount_column).html();
				var specific_amount = $row.find(ALLO.update.lineitem_specific_amount_column).html();
				var units = $row.find(ALLO.update.lineitem_units_field).val();
				var cost_model_per_unit = $row.find(ALLO.update.lineitem_cost_model_per_unit_column).html();
				
				multi_tenant_amount = UTIL.convert_currency_for_server(multi_tenant_amount);
				rent_amount = UTIL.convert_currency_for_server(rent_amount);
				specific_amount = UTIL.convert_currency_for_server(specific_amount);
				cost_model_per_unit = UTIL.convert_currency_for_server(cost_model_per_unit);
				
				multi_tenant_allocation = ALLO.update.clean_percentage(multi_tenant_allocation);
				rent_allocation = ALLO.update.clean_percentage(rent_allocation);
				specific_allocation = ALLO.update.clean_percentage(specific_allocation);
				
				if(!lineitem_id || lineitem_id == 'undefined') {
					lineitem_id = null;
				}
				
				var lineitem = { id:lineitem_id, ospId:osp_id, deviceId:device_id, multiTenantAllocation:multi_tenant_allocation, rentAllocation:rent_allocation, specificAllocation:specific_allocation, units:units, serviceName:service_name, devicePartNumber:device_part_number, deviceDescription:device_description, multiTenantAmount:multi_tenant_amount, rentAmount:rent_amount, specificAmount:specific_amount, infrastructureNote:infrastructure_note, costModelPerUnit:cost_model_per_unit };
				lineitem = UTIL.remove_null_properties(lineitem);
				lineitems.push(lineitem);
				
			});
			
			var unallocated_expenses = [];
			if(ALLO.allocation) {
				unallocated_expenses = ALLO.allocation.unallocatedExpenses;
			}
			
			var json = { multiTenantTotal:multi_tenant_total, rentTotal:rent_total, specificTotal:specific_total, dedicatedTotal:dedicated_total, status:status, month:month, lineItems:lineitems, unallocatedExpenses:unallocated_expenses };
			
			if(ALLO.current_id) {
				json["id"] = ALLO.current_id;
				ajax_type = "PUT"
			}
			
			var message = "Saving Changes...";
			if(calculate) {
				ajax_type = "POST";
				url = "costs/allocation/calculate.json";
				message = "Re-calculating...";
			}
			
			$(ALLO.update.loader_container).show();
			$(ALLO.update.loader_container + " span").html(message);
			$(ALLO.update.changed_msg).hide();
			$(ALLO.update.status_msg).removeClass("success-msg").removeClass("error-msg").addClass("notice-msg").show();
			
			json = UTIL.remove_null_properties(json);
			//UTIL.add_dialog_loader(DEVICES.device_dialog);
			
			$.ajax ({
				url: PAGE_CONSTANTS.BASE_URL + url,
				data: JSON.stringify(json),
				type: ajax_type,
				success:function(data) {
					
					$(ALLO.update.loader_container).hide();
					if(calculate) {
						ALLO.read.build_cost_allocation(data, update_only);
						ALLO.allocation = data;
						
						$(ALLO.update.changed_msg).html("Changes have been made but not saved").show();
					} else {
						if(data.status == PAGE_CONSTANTS.OK_STS) {
							if(!ALLO.current_id) {
								if(data.data) {
									ALLO.current_id = data.data[0];
									console.log("Set current ID to: " + ALLO.current_id);
								}
							}
							
							if(callback) {
								callback();
							} else {
								$(ALLO.update.status_msg).removeClass("notice-msg").removeClass("error-msg").addClass("success-msg");
								$(ALLO.update.changed_msg).html("Changes successfully saved").show();
							}
						} else {
							$(ALLO.update.status_msg).removeClass("notice-msg").removeClass("success-msg").addClass("error-msg").show();
							$(ALLO.update.changed_msg).html(data.message).show();
						}
					}
				},
				error:function(jq_xhr, text_status, error_thrown) {
					$(ALLO.update.status_msg).removeClass("notice-msg").removeClass("success-msg").addClass("error-msg").show();
					$(ALLO.update.changed_msg).html(error_thrown).show();
				}
			});
		}
	},
	generate:{
		generate_btn:"#generate-btn",
		init:function() {
			ALLO.generate.bind_events();
		},
		bind_events:function() {
			$(ALLO.generate.generate_btn).click(function() {
				ALLO.generate.generate_cost_items();
			});
		},
		generate_cost_items:function() {
			ALLO.update.submit_cost_allocation(false, false, function() {
				$(ALLO.update.loader_container).show();
				$(ALLO.update.loader_container + " span").html("Generating Cost Allocation Items...");
				$(ALLO.update.changed_msg).hide();
				$(ALLO.update.status_msg).removeClass("success-msg").removeClass("error-msg").addClass("notice-msg").show();
				
				
				$.ajax ({
					url: PAGE_CONSTANTS.BASE_URL + "costs/allocation/generate/" + ALLO.current_id + ".json",
					data: null,
					type: "POST",
					success:function(data) {
						$(ALLO.update.loader_container).hide();
						
						if(data.status == PAGE_CONSTANTS.OK_STS) {
							$(ALLO.update.status_msg).removeClass("notice-msg").addClass("success-msg");
							$(ALLO.update.changed_msg).html("Costs successfully generated").show();
							ALLO.read.set_status("processed");
						} else {
							$(ALLO.update.status_msg).removeClass("notice-msg").addClass("error-msg").show();
							$(ALLO.update.changed_msg).html(data.message).show();
						}
					}
				});
			});
		}
	},
	spread:{
		spread_dialog:"#allocation-spread-dialog",
		spread_source_field:"#allocation-spread-source",
		spread_type_field:"#allocation-spread-type",
		spread_percent_field:"#allocation-spread-percent",
		spread_dollar_field:"#allocation-spread-dollar",
		spread_dollar_container:"#spread-dollar-container",
		spread_percent_container:"#spread-percent-container",
		spread_part_number_container:"#allocation-spread-part-numbers",
		init:function() {
			ALLO.spread.bind_events();
		},
		bind_events:function() {
			$(ALLO.spread.spread_dialog).dialog({
				  autoOpen:false,
				  dialogClass:"device-dialog",	
			      resizable:false,
			      width:720,
			      height:650,
			      modal:true,
			      title: "Spread Across Part Numbers",
			      open:function() {
			    	  ALLO.spread.reset_popup();
			    	  $('.datepicker').datepicker('enable');
			    	  $(this).closest(".ui-dialog").find(".ui-button").show();
			    	  $(this).closest(".ui-dialog").find(".ui-button:contains('OK')").hide();
			      },
			      close:function() {
			    	  $('.datepicker').datepicker('disable');
			      },
			      buttons: {
			        "Spread":function() {
			        	ALLO.spread.generate_spread();
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
			
			$(ALLO.spread.spread_type_field).change(function() {
				var selected = $(this).val();
				if(selected == "dollar") {
					$(ALLO.spread.spread_percent_container).hide();
					$(ALLO.spread.spread_dollar_container).show();
				} else {
					$(ALLO.spread.spread_percent_container).show();
					$(ALLO.spread.spread_dollar_container).hide();
				}
			});
		},
		reset_popup:function() {
			UTIL.clear_message_in_popup(ALLO.spread.spread_dialog);
			
			$(ALLO.spread.spread_source_field).val("");
			$(ALLO.spread.spread_type_field).val("percent");
			$(ALLO.spread.spread_percent_field).val("");
			$(ALLO.spread.spread_dollar_field).val("");
			
			$(ALLO.spread.spread_percent_container).show();
			$(ALLO.spread.spread_dollar_container).hide();
			
			var part_numbers = "";
			
			$(ALLO.read.cost_allocation_table + " tbody tr").each(function() {
				var $row = $(this);
				var id = $row.find(ALLO.update.lineitem_id_field).val();
				var description = $row.find(ALLO.update.lineitem_device_description_field).val();
				var part_number = $row.find(ALLO.update.lineitem_device_part_number_field).val();
				var units = $row.find(ALLO.update.lineitem_units_field).val();
				
				part_numbers += "<div style=\"margin-bottom:5px;\"><input type=\"checkbox\" value=\"" + id + "\" data-units=\"" + units + "\">&nbsp;&nbsp;" + description + " (" + part_number + ")";
				
			});
			
			$(ALLO.spread.spread_part_number_container).html(part_numbers);
		},
		generate_spread:function() {
			UTIL.clear_message_in_popup(ALLO.spread.spread_dialog);
			
			var source = $(ALLO.spread.spread_source_field).val();
			var type = $(ALLO.spread.spread_type_field).val();
			var percent_spread = $(ALLO.spread.spread_percent_field).val();
			var dollar_spread = $(ALLO.spread.spread_dollar_field).val();
			var checked = [];
			var total_units = 0;
			
			$(ALLO.spread.spread_part_number_container).find("input[type='checkbox']:checked").each(function() {
				var $check = $(this);
				checked.push({ id:$check.val(), units: $check.data("units") });
				total_units += $check.data("units");
			});
			
			var percent_to_spread = percent_spread;
			if(type == "dollar") {
				var source_dollar = 0;
				if(source == "depreciation") {
					source_dollar = $(ALLO.update.multi_tenant_total_field).val();
				} else {
					source_dollar = $(ALLO.update.rent_total_field).val();
				}
				percent_to_spread = (dollar_spread / source_dollar) * 100;
			}
			
			if(!source || !type || (type == "dollar" && !dollar_spread) || (type == "percent" && !percent_spread)) {
				UTIL.add_error_message_to_popup(ALLO.spread.spread_dialog, "Please enter all required fields.");
				return false;
			}
			
			if(checked.length == 0) {
				UTIL.add_error_message_to_popup(ALLO.spread.spread_dialog, "You must select at least one part number to spread this to.");
				return false;
			}
			
			for(var i = 0; i < checked.length; i++) {
				var check = checked[i];
				var check_percent = check.units / total_units;
				
				var $row = $("input[name='lineitem-id'][value=" + check.id + "]").parent().parent();
				if(source == "depreciation") {
					$row.find(ALLO.update.lineitem_multi_tenant_allocation_field).val(UTIL.round_with_decimals(percent_to_spread * check_percent, 2));
				} else {
					$row.find(ALLO.update.lineitem_rent_allocation_field).val(UTIL.round_with_decimals(percent_to_spread * check_percent, 2));
				}
				
			}
			ALLO.update.submit_cost_allocation(true);
			
			UTIL.add_success_message_to_popup(ALLO.spread.spread_dialog, "Allocation has been spread across part numbers!");
			$(ALLO.spread.spread_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").hide();
			$(ALLO.spread.spread_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button:contains('OK')").show();
		}
	}
};

$(document).ready(function() {
	ALLO.init();
});