var COSTS = {
	add_cost_dialog:"#cost-dialog",
	delete_cost_dialog:"#delete-cost-dialog",
	upload_dialog:"#upload-dialog",
	moment_format:"MM/DD/YYYY",
	asset_value:"asset",
	expense_value:"expense",
	init:function() {
		COSTS.set_variables();
		COSTS.bind_events();
		COSTS.view.init();
		COSTS.load.init();
		COSTS.modify.init();
		COSTS.file_upload.init();
		COSTS.delete_item.init();
		COSTS.modify_unallocated.init();
	},
	set_variables:function() {
		
	},
	bind_events:function() {
		$(COSTS.add_cost_dialog).dialog({
			  autoOpen:false,
			  dialogClass:"costs-dialog",	
		      resizable:false,
		      width:750,
		      height:770,
		      modal:true,
		      title: "Add a Cost",
		      open:function() {
		    	  $('.datepicker').datepicker('enable');
		    	  $(this).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").show();
		    	  $(this).closest(".ui-dialog").find(".ui-button:contains('OK')").hide();
		      },
		      close:function() {
		    	  $('.datepicker').datepicker('disable');
		      },
		      buttons: {
		        "Save":function() {
		        	COSTS.modify.submit_cost();
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
		
		$(COSTS.delete_cost_dialog).dialog({
			  autoOpen:false,
			  dialogClass:"delete-dialog",	
		      resizable:false,
		      width:720,
		      height:400,
		      modal:true,
		      title: "Delete a Category",
		      open:function() {
		    	  $('.datepicker').datepicker('enable');
		    	  $(this).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").show();
			      $(this).closest(".ui-dialog").find(".ui-button:contains('OK')").hide();
		      },
		      close:function() {
		    	  $('.datepicker').datepicker('disable');
		      },
		      buttons: {
		        "Delete":function() {
		        	COSTS.delete_item.submit_delete();
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
		
		$(COSTS.upload_dialog).dialog({
			  autoOpen:false,
			  dialogClass:"upload-dialog",	
		      resizable:false,
		      width:720,
		      height:400,
		      modal:true,
		      title: "Upload a Spreadsheet",
		      open:function() {
		    	  $('.datepicker').datepicker('enable');
		    	  COSTS.file_upload.reset_popup();
		      },
		      close:function() {
		    	  $('.datepicker').datepicker('disable');
		      },
		      buttons: {
		        "Upload":function() {
		        	COSTS.file_upload.submit_file();
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
	view: {
		view_type_field:"#view-type",
		view_cost_type_field:"#view-cost-type",
		view_date_type_field:"#view-date-type",
		view_date_min:"01/01/2012",
		view_date_max:"12/31/2024",
		date_dropdown:"#current-viewing-date",
		stylized_dropdown:null,
		date_viewing_section:".costs-viewing",
		current_viewing_start_date:"#current-viewing-start-date",
		current_viewing_end_date:"#current-viewing-end-date",
		current_viewing_display_start_date:"#current-viewing-display-start-date",
		current_viewing_display_end_date:"#current-viewing-display-end-date",
		current_view_prev_month:"#current-view-prev-month",
		current_view_next_month:"#current-view-next-month",
		switch_date_class:"switch-date",
		expense_type_filter_column:"#expense-type-filter-column",
		current_view_type:null,
		init:function() {
			COSTS.view.bind_events();
			COSTS.view.build_dropdown();
			COSTS.view.build_date_links();
		},
		bind_events:function() {
			$(COSTS.view.view_type_field).change(function() {
				var selected = $(this).val();
				if(selected == "unallocated") {
					$(COSTS.modify.add_unallocated_link).show();
					$(COSTS.modify.add_cost_link).hide();
					$(COSTS.view.expense_type_filter_column).hide();
				} else {
					$(COSTS.modify.add_unallocated_link).hide();
					$(COSTS.modify.add_cost_link).show();
					$(COSTS.view.expense_type_filter_column).show();
				}
				COSTS.load.get_costs();
			});
			
			$(COSTS.view.view_cost_type_field).change(function() {
				COSTS.load.get_costs();
			});
			
			$(COSTS.view.date_dropdown).change(function() {
				var value = $(this).val();
				var $option = $(this).find(":selected");
				
				COSTS.view.switch_date($option, value);
			});
			
			$(COSTS.view.view_date_type_field).change(function() {
				var selected = $(this).val();
				if(selected == "all") {
					$(COSTS.view.date_viewing_section).hide();
					$(COSTS.view.current_viewing_start_date).val("");
					$(COSTS.view.current_viewing_end_date).val("");
				} else {
					COSTS.view.build_dropdown(selected);
					COSTS.view.build_date_links();
					$(COSTS.view.date_viewing_section).show();
				}
				
				COSTS.load.get_costs();
			});
			
			$(document).on("click", "." + COSTS.view.switch_date_class, function() {
				var $option = $(this);
				$(COSTS.view.date_dropdown).selectBox('value',$option.data("value"));
				COSTS.view.switch_date($option, "");
			});
		},
		switch_date:function($option, value) {
			var start_date = $option.data("start");
			var end_date = $option.data("end");
			
			$(COSTS.view.current_viewing_start_date).val(start_date);
			$(COSTS.view.current_viewing_end_date).val(end_date);
			COSTS.load.get_costs();
			
			/*
			if(value == "#") {
				start_date = $(CONTRACT.date_selectors.contract_start_date).val();
				end_date = $(CONTRACT.date_selectors.contract_end_date).val();
			}
			*/
			
			$(COSTS.view.current_viewing_display_start_date).html(start_date);
			$(COSTS.view.current_viewing_display_end_date).html(end_date);
			
			COSTS.view.build_date_links();
		},
		build_date_links:function() {
			var $option = $(COSTS.view.date_dropdown + " :selected");
			var $prev_option = $option.prev();
			var $next_option = $option.next();
			var prev_start_name = $prev_option.html();
			var prev_start_date = $prev_option.data("start");
			var prev_end_date = $prev_option.data("end");
			var prev_value = $prev_option.val();
			var next_start_name = $next_option.html();
			var next_start_date = $next_option.data("start");
			var next_end_date = $next_option.data("end");
			var next_value = $next_option.val();
			
			var prev_link = "";
			var next_link = "";
			if(prev_start_name != "All Time" && prev_start_name != undefined) prev_link = COSTS.view.build_date_link(prev_start_name, prev_start_date, prev_end_date, prev_value, true);
			if(next_start_name != "All Time" && next_start_name != undefined) next_link = COSTS.view.build_date_link(next_start_name, next_start_date, next_end_date, next_value, false);
			$(COSTS.view.current_view_prev_month).html(prev_link);
			$(COSTS.view.current_view_next_month).html(next_link);
		},
		build_date_link:function(month_name, start_date, end_date, value, prev) {
			var prev_icon = "<i class=\"fa fa-angle-double-left\"></i>";
			var next_icon = "<i class=\"fa fa-angle-double-right\"></i>";
			var output = "<a class=\"" + COSTS.view.switch_date_class + "\" href=\"javascript:;\" data-value=\"" + value + "\" data-start=\"" + start_date + "\" data-end=\"" + end_date + "\">";
			(prev) ? output += prev_icon + month_name : output += month_name + next_icon;
			output += "</a>";
			return output;
		},
		build_dropdown:function(type) {
			var output = "";
			if(!type) type = "month";
			var display_format = "MMM YYYY";
			if(type == "year") display_format = "YYYY";
			var start_moment = moment(COSTS.view.view_date_min, COSTS.moment_format).startOf(type);
			var end_moment = moment(COSTS.view.view_date_max, COSTS.moment_format).endOf(type);
			var now = moment().startOf(type);
			while(start_moment < end_moment) {
				var selected = "";
				var month_start = start_moment.startOf(type).format(COSTS.moment_format);
				var month_end = start_moment.endOf(type).format(COSTS.moment_format);
				//set the month to the current month, as that is the page load default
				if(month_start == now.format(COSTS.moment_format)) {
					selected = " selected=\"selected\"";
					$(COSTS.view.current_viewing_start_date).val(month_start);
					$(COSTS.view.current_viewing_end_date).val(month_end);
					$(COSTS.view.current_viewing_display_start_date).html(month_start);
					$(COSTS.view.current_viewing_display_end_date).html(month_end);
				}
				output += "<option data-start=\"" + month_start + "\" data-end=\"" + month_end + "\"" + selected + ">" + start_moment.format(display_format) + "</option>";
				start_moment.add(1, type).startOf(type);
			}
			$(COSTS.view.date_dropdown).html(output);
			
			//destroy a previous version if it exists
			//if(COSTS.view.stylized_dropdown) COSTS.view.stylized_dropdown.destroy();
			$(COSTS.view.date_dropdown).selectBox("destroy");
			
			//initiate the fancy looking dropdown
			$(COSTS.view.date_dropdown).selectBox();
		}
	},
	load: {
		costs_table:"#costs-table",
		cost_data:[],
		asset_data:null,
		expense_data:null,
		unallocated_data:null,
		asset_data_cache:{},
		expense_data_cache:{},
		asset_data_loaded:false,
		expense_data_loaded:false,
		init:function() {
			//default date??
			
			COSTS.load.get_costs();
		},
		get_costs:function() {
			COSTS.load.asset_data = null;
			COSTS.load.expense_data = null;
			COSTS.load.unallocated_data = null;
			COSTS.load.asset_data_cache = {};
			COSTS.load.expense_data_cache = {};
			COSTS.load.asset_data_loaded = false;
			COSTS.load.expense_data_loaded = false;
			COSTS.load.cost_data = [];
			
			var view_type = $(COSTS.view.view_type_field).val();
			COSTS.view.current_view_type = view_type;
			
			var params = "?filter";
			var start_date = $(COSTS.view.current_viewing_start_date).val();
			var end_date = $(COSTS.view.current_viewing_end_date).val();
			if($(COSTS.view.view_date_type_field).val() != "all") params += "&sd=" + UTIL.convert_dates_for_server(start_date) + "&ed=" + UTIL.convert_dates_for_server(end_date);
			
			UTIL.add_table_loader(COSTS.load.costs_table);
			$(COSTS.load.costs_table + " tfoot").html("");
			
			if(view_type == "unallocated") {
				$.ajax ({
					url: PAGE_CONSTANTS.BASE_URL + "unallocatedexpense.json" + params,
					type: "GET",
					success:function(data) {
						COSTS.load.unallocated_data = data;
						COSTS.load.build_costs_table(data, true);
					}
				});
			} else {
				if(view_type == "all" || view_type == "asset") {
					$.ajax ({
						url: PAGE_CONSTANTS.BASE_URL + "assetitems.json" + params,
						type: "GET",
						success:function(data) {
							COSTS.load.asset_data = data;
							COSTS.load.asset_data_loaded = true;
						}
					});
				}
				
				var cost_type = $(COSTS.view.view_cost_type_field).val();
				if(cost_type) {
					params += "&ctype=" + cost_type; 
				}
				
				if(view_type == "all" || view_type == "expense") {
					$.ajax ({
						url: PAGE_CONSTANTS.BASE_URL + "costitems.json" + params,
						type: "GET",
						success:function(data) {
							COSTS.load.expense_data = data;
							COSTS.load.expense_data_loaded = true;
						}
					});
				}
				
				COSTS.load.merge_data();
			}
			
			
			
		},
		merge_data:function() {
			if(((!COSTS.load.asset_data_loaded || !COSTS.load.expense_data_loaded) && COSTS.view.current_view_type == "all") || (!COSTS.load.expense_data_loaded && COSTS.view.current_view_type == "expense") || (!COSTS.load.asset_data_loaded && COSTS.view.current_view_type == "asset")) {
				//we want to loop through the method again until all calls are done
				setTimeout("COSTS.load.merge_data();",250);
				return false;
			}
			
			var asset_data = COSTS.load.asset_data;
			var expense_data = COSTS.load.expense_data;
			
			//merge the data into a representation for the table display
			if(asset_data != null) {
				for(var i=0; i < asset_data.length; i++) {
					var record = asset_data[i];
					COSTS.load.asset_data_cache[record.id] = record;
					var rep = { "id":record.id, "costGroup":COSTS.asset_value, "name":record.name, "costDate":record.acquired, "amount":record.amount, "expense":record.expense, "costType":"" };
					COSTS.load.cost_data.push(rep);
				}
			}
			if(expense_data != null) {
				for(var k=0; k < expense_data.length; k++) {
					var record = expense_data[k];
					COSTS.load.expense_data_cache[record.id] = record;
					var rep = { "id":record.id, "costGroup":COSTS.expense_value, "name":record.name, "costDate":record.applied, "amount":record.amount, "expense":record.expense, "costType":record.costType };
					COSTS.load.cost_data.push(rep);
				}
			}
			
			COSTS.load.build_costs_table(COSTS.load.cost_data, false);
		},
		build_costs_table:function(costs, is_unallocated) {
			var output = "";
			var footer_output = "";
			var total_amount = 0;
			var is_admin = false;
			if(GLOBAL && GLOBAL.is_admin) is_admin = GLOBAL.is_admin;
			
			if(costs.length > 0) {
				for(var i=0; i < costs.length; i++) {
					if(is_unallocated) {
						output += COSTS.load.build_unallocated_row(costs[i], is_admin);
					} else {
						output += COSTS.load.build_cost_row(costs[i], is_admin);
					}
					
					total_amount += costs[i].amount;
				}
				
				footer_output = "<tr class=\"total-row\">";
				footer_output += "<td colspan=\"3\"></td>";
				footer_output += "<td class=\"right\">TOTAL</td>";
				footer_output += "<td class=\"right\">" + accounting.formatMoney(total_amount) + "</td>";
				if(is_admin) {
					footer_output += "<td>&nbsp;</td>";
				}
				footer_output += "</tr>";
			} else {
				output += "<tr><td colspan=\"5\" class=\"no-results\">There are no costs for this time frame.</td></tr>";
			}
			
			$(COSTS.load.costs_table + " tbody").html(output);
			$(COSTS.load.costs_table + " tfoot").html(footer_output);
		},
		build_cost_row:function(cost, is_admin) {
			var type = "assetitems";
			if(cost.costGroup == "expense") {
				type = "costitems";
			}
			
			var expense_id = null;
			if(cost.expense && cost.expense.id) expense_id = cost.expense.id;
			
			var output = "<tr>";
			output += "<td><a href=\"javascript:;\" class=\"popup-link cost-popup-link\" data-id=\"" + cost.id + "\"  data-expense-id=\"" + expense_id + "\" data-type=\"" + cost.costGroup + "\" data-dialog=\"cost-dialog\">" + cost.name + "</a></td>";
			output += "<td class=\"capitalize\">" + cost.costGroup + "</td>";
			output += "<td>" + cost.costType + "</td>";
			output += "<td class=\"right\">" + UTIL.convert_dates_for_ui(cost.costDate) + "</td>";
			output += "<td class=\"right\">" + accounting.formatMoney(cost.amount) + "</td>";
			if(is_admin) {
				output += "<td class=\"right\"><a href=\"javascript:;\" class=\"popup-link delete-popup-link\" data-id=\"" + cost.id + "\" data-type=\"" + type + "\" data-name=\"" + cost.name + "\" data-dialog=\"delete-cost-dialog\"><i class=\"fa fa-minus-circle\"></i>Delete</a></td>";
			}
			output += "</tr>";
			
			return output;
		},
		build_unallocated_row:function(unallocated_expense, is_admin) {
			var output = "<tr>";
			output += "<td><a href=\"javascript:;\" class=\"popup-link unallocated-popup-link\" data-id=\"" + unallocated_expense.id + "\" data-dialog=\"unallocated-dialog\">" + unallocated_expense.name + "</a></td>";
			output += "<td class=\"capitalize\">Unallocated</td>";
			output += "<td>" + unallocated_expense.serviceName + "</td>";
			output += "<td class=\"right\">" + UTIL.convert_dates_for_ui(unallocated_expense.month) + "</td>";
			output += "<td class=\"right\">" + accounting.formatMoney(unallocated_expense.amount) + "</td>";
			if(is_admin) {
				if(unallocated_expense.costAllocationId) {
					output += "<td></td>";
				} else {
					output += "<td class=\"right\"><a href=\"javascript:;\" class=\"popup-link delete-popup-link\" data-id=\"" + unallocated_expense.id + "\" data-type=\"unallocatedexpense\" data-name=\"" + unallocated_expense.name + "\" data-dialog=\"delete-cost-dialog\"><i class=\"fa fa-minus-circle\"></i>Delete</a></td>";
				}
			}
			output += "</tr>";
			
			return output;
		}
	},
	modify: {
		cost_link_class:".cost-popup-link",
		cost_group_field:"#cost-group",
		cost_type_field:"#cost-asset-type",
		cost_expense_category_field:"#cost-expense-category",
		cost_location_field:"#cost-location",
		cost_name_field:"#cost-name",
		cost_description_field:"#cost-description",
		cost_amount_field:"#cost-amount",
		cost_date_acquired_field:"#cost-date-acquired",
		cost_disposal_date_field:"#cost-disposal-date",
		depreciable_life_field:"#cost-depreciable-life",
		cost_date_field:"#cost-date",
		cost_asset_number_field:"#cost-asset-number",
		cost_serial_number_field:"#cost-serial-number",
		cost_for_customer_field:"#cost-for-customer",
		cost_customer_field:"#cost-customer",
		cost_type_field:"#cost-type",
		cost_subtype_field:"#cost-subtype",
        ca_app_plug:"#id-ca-app-plug",
		cost_customer_contract_field:"#cost-customer-contract",
		modify_cost_table:"#modify-cost-table",
		fraction_asset_category:"select[name='cost-asset-category']",
		fraction_cost_units:"input[name='cost-units']",
		fraction_cost_fraction:"input[name='cost-fraction']",
		units_display:".units-display",
		clone_data_row:"#clone-row",
		add_row:"#add-row",
		remove_row:".remove-row",
		asset_class_display:".asset",
		expense_class_display:".expense",
		for_customer_display:".cost-customer-row",
		error_general_msg:"#general-error-msg",
		error_cost_fractions_msg:"#error-cost-fractions-msg",
		error_invalid_date_msg:"#error-invalid-date-msg",
		error_acquired_disposal_msg:"#error-acquired-disposal-msg",
		error_for_customer_msg:"#error-for-customer-msg",
		ok_asset_created_msg:"#ok-asset-created-msg",
		ok_expense_created_msg:"#ok-expense-created-msg",
		ok_asset_updated_msg:"#ok-asset-updated-msg",
		ok_expense_updated_msg:"#ok-expense-updated-msg",
        cost_allocation_link:"#cost-allocation-link",
        add_cost_link:"#add-cost-link",
        add_unallocated_link:"#add-unallocated-link",
		current_id:null,
		current_expense_id:null,
		current_type:null,
		init:function() {
			COSTS.modify.bind_events();
			COSTS.modify.sort_categories();
		},
		bind_events:function() {
			$(document).on("click", COSTS.modify.cost_link_class, function() {
				var id = $(this).data("id");
				var expense_id = $(this).data("expense-id");
				var type = $(this).data("type");
				
				if (id == "add") {
					COSTS.modify.current_id = null;
					COSTS.modify.current_expense_id = null;
					COSTS.modify.current_type = COSTS.asset_value;
				} else {
					COSTS.modify.current_id = id;
					COSTS.modify.current_expense_id = expense_id;
					COSTS.modify.current_type = type;
				}
				
				COSTS.modify.reset_popup();
			});
			
			$(COSTS.modify.cost_group_field).change(function() {
				var selected = $(this).val();
				COSTS.modify.set_form_for_cost_group(selected);
			});
			
			$(COSTS.modify.cost_for_customer_field).click(function() {
				if(this.checked) {
					$(COSTS.modify.for_customer_display).show();
					$(COSTS.modify.cost_customer_field).focus();
				} else {
					$(COSTS.modify.for_customer_display).hide();
				}
			});
			
			$(COSTS.modify.cost_customer_field).change(function() {
				var selected = $(this).val();
				if(selected == "#") {
					$(COSTS.modify.cost_customer_contract_field).html("<option value=\"#\"></option>");
				} else {
					COSTS.modify.set_contracts_for_customer(selected);
				}
			});
			
			$(COSTS.modify.add_row).click(function() {
				COSTS.modify.clone_row();
			});
			
			$(document).on("click",COSTS.modify.remove_row,function() {
				//remove the row
				$(this).parent().parent().remove();
				
				var rows = $(COSTS.modify.modify_cost_table + " tr").length;
				if(rows == 2) {
					//add a clean row if it's the last one
					COSTS.modify.clone_row();
				}
			});
			
			$(document).on("change",COSTS.modify.fraction_asset_category, function() {
				var units = $(this).find("option:selected").data("units");
				if(!units) units = "";
				$(this).parents("tr").find(COSTS.modify.units_display).html(units);
			});
		},
		sort_categories:function() {
			var options = $(COSTS.modify.clone_data_row).find(COSTS.modify.fraction_asset_category + " option");
			var arr = options.map(function(_, o) { return { t: $(o).text(), v: o.value, u:$(o).data("units") }; }).get();
			arr.sort(function(o1, o2) { 
				var t1 = o1.t.toLowerCase(), t2 = o2.t.toLowerCase();
				return t1 > t2 ? 1 : t1 < t2 ? -1 : 0;
			});
			options.each(function(i, o) {
			  o.value = arr[i].v;
			  $(o).text(arr[i].t);
			  $(o).attr("data-units", arr[i].u);
			});
		},
		reset_popup:function() {
			UTIL.clear_message_in_popup(COSTS.add_cost_dialog);
			
			var record = null;
			var cost_group = COSTS.asset_value;
			var location = "#";
			var name = "";
			var description = "";
			var amount = "";
			var date_acquired = "";
			var disposal_date = "";
			var cost_date = "";
			var life = "";
			var asset_number = "";
			var serial_number = "";
			var cost_type = "general";
            var cost_subtype = "";
            var cost_allocation_id = null;
			
			//edit
			if(COSTS.modify.current_id != null) {
				cost_group = COSTS.modify.current_type;
				
				if(cost_group == COSTS.asset_value) {
					record = COSTS.load.asset_data_cache[COSTS.modify.current_id];
					date_acquired = UTIL.convert_dates_for_ui(record.acquired);
					disposal_date = UTIL.convert_dates_for_ui(record.disposal);
					life = record.life;
					asset_number = record.partNumber;
					serial_number = record.sku;
				} else if(cost_group == COSTS.expense_value) {
					record = COSTS.load.expense_data_cache[COSTS.modify.current_id];
					cost_date = UTIL.convert_dates_for_ui(record.applied);
					cost_type = record.costType;
					cost_subtype = record.costSubType;
                    cost_allocation_id = record.costAllocationLineItemIdRef;
				}
				
				if(record) {
					name = record.name;
					description = record.description;
					amount = accounting.formatMoney(record.amount);
					amount = amount.replace("$","");
					location = record.locationId;
				}
				
			}
			
	    	//header fields
	    	$(COSTS.modify.cost_group_field).val(cost_group);
			COSTS.modify.set_form_for_cost_group(cost_group);
			$(COSTS.modify.cost_location_field).val(location);
			$(COSTS.modify.cost_name_field).val(name);
			$(COSTS.modify.cost_description_field).val(description);
			$(COSTS.modify.cost_amount_field).val(amount);
			$(COSTS.modify.cost_date_acquired_field).val(date_acquired);
			$(COSTS.modify.cost_disposal_date_field).val(disposal_date);
			$(COSTS.modify.depreciable_life_field).val(life);
			$(COSTS.modify.cost_date_field).val(cost_date);
			$(COSTS.modify.cost_asset_number_field).val(asset_number);
			$(COSTS.modify.cost_serial_number_field).val(serial_number);
			$(COSTS.modify.cost_type_field).val(cost_type);
			$(COSTS.modify.cost_subtype_field).val(cost_subtype);
            if(cost_type == "depreciated" && cost_allocation_id) {
                $(COSTS.modify.ca_app_plug).html("View the <a target=\"_blank\" href=\""+$(COSTS.modify.cost_allocation_link).val()+"/"+cost_allocation_id+"\">Cost Allocation records</a> related to this Cost item");
                $(COSTS.modify.ca_app_plug).show();
            } else {
                $(COSTS.modify.ca_app_plug).html("");
                $(COSTS.modify.ca_app_plug).hide();
            }
	    	
	    	//table section
			$(COSTS.modify.modify_cost_table + " tbody tr:visible").remove();
			if(COSTS.modify.current_id != null) {
				if(cost_group == COSTS.asset_value) {
					var fractions = record.assetCostFractions;
					for(var i=0; i < fractions.length; i++) {
						var fraction = fractions[i];
						COSTS.modify.clone_row();
						var $row = $(COSTS.modify.modify_cost_table + " tbody tr:last-child");
						$row.find(COSTS.modify.fraction_asset_category).val(fraction.expenseCategory.id);
						$row.find(COSTS.modify.fraction_cost_units).val(fraction.quantity);
						var units = $row.find(COSTS.modify.fraction_asset_category).find("option:selected").data("units");
						if(!units) units = "";
						$row.find(COSTS.modify.units_display).html(units);
						$row.find(COSTS.modify.fraction_cost_fraction).val(fraction.fraction);
					}
				} else if(cost_group == COSTS.expense_value) {
					var fractions = record.costFractions;
					for(var i=0; i < fractions.length; i++) {
						var fraction = fractions[i];
						COSTS.modify.clone_row();
						var $row = $(COSTS.modify.modify_cost_table + " tbody tr:last-child");
						$row.find(COSTS.modify.fraction_asset_category).val(fraction.expenseCategory.id);
						$row.find(COSTS.modify.fraction_cost_fraction).val(fraction.fraction);
					}
				}
			} else {
				COSTS.modify.clone_row();
			}
			
			//for customer section
			var for_customer = false;
			var customer_id = "#";
			$(COSTS.modify.cost_customer_contract_field).html("<option value=\"#\"></option>");
			
			if(COSTS.modify.current_id != null) {
				if(record.customerId) {
					var contract_id = record.contractId;
					for_customer = true;
					customer_id = record.customerId;
					$(COSTS.modify.for_customer_display).show();
					COSTS.modify.set_contracts_for_customer(customer_id, contract_id);
				} else {
					$(COSTS.modify.for_customer_display).hide();
				}
			} else {
				$(COSTS.modify.for_customer_display).hide();
			}
			$(COSTS.modify.cost_for_customer_field).prop("checked",for_customer);
			$(COSTS.modify.cost_customer_field).val(customer_id);
		},
		clone_row:function() {
			var $clone = $(COSTS.modify.clone_data_row).clone().attr("id","").show();
			$(COSTS.modify.modify_cost_table + " tbody").append($clone);
		},
		set_form_for_cost_group:function(cost_group) {
			if(cost_group == COSTS.asset_value) {
				$(COSTS.modify.asset_class_display).show();
				$(COSTS.modify.expense_class_display).hide();
			} else {
				$(COSTS.modify.asset_class_display).hide();
				$(COSTS.modify.expense_class_display).show();
			}
		},
		set_contracts_for_customer:function(customer_id, contract_id) {
			$.ajax ({
				url: PAGE_CONSTANTS.BASE_URL + "contracts.json?cid=" + customer_id,
				type: "GET",
				success:function(data) {
					var options = "<option value=\"#\"></option>";
					for(var i=0; i<data.length; i++) {
						options += "<option value=\"" + data[i].id + "\">" + data[i].altId + "</option>";
					}
					$(COSTS.modify.cost_customer_contract_field).html(options);
					if(contract_id) $(COSTS.modify.cost_customer_contract_field).val(contract_id);
				}
			});
		},
		submit_cost:function() {
			UTIL.clear_message_in_popup(COSTS.add_cost_dialog);
			
			var cost_group = $(COSTS.modify.cost_group_field).val();
			var ajax_type = "POST";
			var url = PAGE_CONSTANTS.BASE_URL;
			var name = $(COSTS.modify.cost_name_field).val();
			var description = $(COSTS.modify.cost_description_field).val();
			var amount = $(COSTS.modify.cost_amount_field).val();
			var location = $(COSTS.modify.cost_location_field).val();
			var for_customer = $(COSTS.modify.cost_for_customer_field).prop("checked");
			var cost_type = $(COSTS.modify.cost_type_field).val();
			var cost_subtype = $(COSTS.modify.cost_subtype_field).val();
			var rows_valid = true;
			var cost_fraction_total = 0;
			var json = {};
			var fractions = [];
			var success_msg = "Success!";
			var expense_record = null;
			
			if(location == "#") location = null;
			amount = UTIL.convert_currency_for_server(amount);
			
			if(cost_group == "asset") {
				url += "assetitems.json";
				
				var life = $(COSTS.modify.depreciable_life_field).val();
				var acquired = $(COSTS.modify.cost_date_acquired_field).val();
				var disposal = $(COSTS.modify.cost_disposal_date_field).val();
				var asset_number = $(COSTS.modify.cost_asset_number_field).val();
				var serial_number = $(COSTS.modify.cost_serial_number_field).val();
				
				//validate
				if(!name || !amount || !acquired || !life) {
					UTIL.add_error_message_to_popup(COSTS.add_cost_dialog, $(COSTS.modify.error_general_msg).val());
					return false;
				} else if(!moment(acquired, COSTS.moment_format).isValid() || (disposal && !moment(disposal, COSTS.moment_format).isValid())) {
					UTIL.add_error_message_to_popup(COSTS.add_cost_dialog, $(COSTS.modify.error_invalid_date_msg).val());
					return false;
				}
				
				acquired = UTIL.convert_dates_for_server(acquired);
				disposal = UTIL.convert_dates_for_server(disposal);
				
				expense_record = { "expenseType":"asset", "name":name, "description":description, "amount":amount, "quantity":1, "locationId":location };
				
				$(COSTS.modify.modify_cost_table).find("tbody tr").each(function() {
					var $row = $(this);
					if("#" + $row.attr("id") != COSTS.modify.clone_data_row) {
						var expense_category = $row.find(COSTS.modify.fraction_asset_category).val();
						var units = $row.find(COSTS.modify.fraction_cost_units).val();
						var cost_fraction = $row.find(COSTS.modify.fraction_cost_fraction).val();
						
						//validate
						if(expense_category == "#" || !cost_fraction) {
							rows_valid = false;
							return false;
						}
						
						fractions.push({ "expenseCategory":{ "id":expense_category }, "quantity":units, "fraction":cost_fraction });
						cost_fraction_total += parseFloat(cost_fraction);
					}
				});
				
				json = { "name":name, "description":description, "locationId":location, "amount":amount, "life":life, "acquired":acquired, "disposal":disposal, "assetCostFractions":fractions, partNumber:asset_number, sku:serial_number };
				
				success_msg = $(COSTS.modify.ok_asset_created_msg).val();
			} else if(cost_group == "expense") {
				url += "costitems.json";
				
				var cost_date = $(COSTS.modify.cost_date_field).val();
				cost_date = UTIL.convert_dates_for_server(cost_date);
				
				//validate
				if(!name || !amount || !cost_date) {
					UTIL.add_error_message_to_popup(COSTS.add_cost_dialog, $(COSTS.modify.error_general_msg).val());
					return false;
				} else if(!moment(cost_date, COSTS.moment_format).isValid()) {
					UTIL.add_error_message_to_popup(COSTS.add_cost_dialog, $(COSTS.modify.error_invalid_date_msg).val());
					return false;
				}
				
				expense_record = { "expenseType":"cost", "name":name, "description":description, "amount":amount, "quantity":1, "locationId":location };
				
				$(COSTS.modify.modify_cost_table).find("tbody tr").each(function() {
					var $row = $(this);
					if("#" + $row.attr("id") != COSTS.modify.clone_data_row) {
						var expense_category = $row.find(COSTS.modify.fraction_asset_category).val();
						var cost_fraction = $row.find(COSTS.modify.fraction_cost_fraction).val();
						
						//validate
						if(expense_category == "#" || !cost_fraction) {
							rows_valid = false;
							return false;
						}
						
						fractions.push({ "expenseCategory":{ "id":expense_category }, "fraction":cost_fraction });
						cost_fraction_total += parseFloat(cost_fraction);
					}
				});
				
				json = { "name":name, "description":description, "locationId":location, "amount":amount, "applied":cost_date, costFractions:fractions, "costType":cost_type, "costSubType":cost_subtype };
				
				success_msg = $(COSTS.modify.ok_expense_created_msg).val();
			}
			
			if(!rows_valid) {
				UTIL.add_error_message_to_popup(COSTS.add_cost_dialog, $(COSTS.modify.error_general_msg).val());
				return false;
			}
			
			if(cost_fraction_total != 100) {
				UTIL.add_error_message_to_popup(COSTS.add_cost_dialog, $(COSTS.modify.error_cost_fractions_msg).val());
				return false;
			}
			
			//add in for customer attributes
			if(for_customer) {
				var customer_id = $(COSTS.modify.cost_customer_field).val();
				var contract_id = $(COSTS.modify.cost_customer_contract_field).val();
				//validate
				if(customer_id == "#") {
					UTIL.add_error_message_to_popup(COSTS.add_cost_dialog, $(COSTS.modify.error_for_customer_msg).val());
					return false;
				}
				json["customerId"] = customer_id;
				expense_record["customerId"] = customer_id;
				if(contract_id != "#") {
					json["contractId"] = contract_id;
					expense_record["contractId"] = contract_id;
				}
			}
			
			//is an update
			if(COSTS.modify.current_id != null) {
				ajax_type = "PUT";
				json["id"] = COSTS.modify.current_id;
				
				if(cost_group == "asset") {
					success_msg = $(COSTS.modify.ok_asset_updated_msg).val();
				} else if(cost_group == "expense") {
					success_msg = $(COSTS.modify.ok_expense_updated_msg).val();
				}
			}
			
			UTIL.add_dialog_loader(COSTS.add_cost_dialog);
			
			if(COSTS.modify.current_expense_id != null) {
				expense_record["id"] = COSTS.modify.current_expense_id; 
			}
			
			//we only add the expense record if this record is being added for the first time, or if it has an existing expense record id
			if(COSTS.modify.current_id == null || (COSTS.modify.current_id != null && COSTS.modify.current_expense_id != null)) {
				UTIL.remove_null_properties(expense_record);
				json["expense"] = expense_record;
			}
			
			UTIL.remove_null_properties(json);
			
			$.ajax ({
				url: url,
				data: JSON.stringify(json),
				type: ajax_type,
				success:function(data) {
					UTIL.remove_dialog_loader(COSTS.add_cost_dialog);
					
					if(data.status == PAGE_CONSTANTS.OK_STS) {
						UTIL.add_success_message_to_popup(COSTS.add_cost_dialog, success_msg);
						$(COSTS.add_cost_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").hide();
						$(COSTS.add_cost_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button:contains('OK')").show();
						
						COSTS.load.get_costs();
					} else {
						UTIL.add_error_message_to_popup(COSTS.add_cost_dialog, data.message);
					}
				}
			});
		}
	},
	modify_unallocated:{
		unallocated_popup_link:".unallocated-popup-link",
		unallocated_dialog:"#unallocated-dialog",
		name_field:"#unallocated-name",
		description_field:"#unallocated-description",
		amount_field:"#unallocated-amount",
		date_field:"#unallocated-date",
		service_field:"#unallocated-service",
		unallocated_already_msg:"#unallocated-already-msg",
		current_id:null,
		init:function() {
			COSTS.modify_unallocated.bind_events();
		},
		bind_events:function() {
			$(COSTS.modify_unallocated.unallocated_dialog).dialog({
				  autoOpen:false,
				  dialogClass:"costs-dialog",	
			      resizable:false,
			      width:850,
			      height:530,
			      modal:true,
			      title: "Add a Cost",
			      open:function() {
			    	  $('.datepicker').datepicker('enable');
			    	  $(this).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").show();
			    	  $(this).closest(".ui-dialog").find(".ui-button:contains('OK')").hide();
			      },
			      close:function() {
			    	  $('.datepicker').datepicker('disable');
			      },
			      buttons: {
			        "Save":function() {
			        	COSTS.modify_unallocated.submit_unallocated();
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
			
			$(document).on("click", COSTS.modify_unallocated.unallocated_popup_link, function() {
				var id = $(this).data("id");
				COSTS.modify_unallocated.current_id = id;
				COSTS.modify_unallocated.reset_popup();
			});
		},
		reset_popup:function() {
			UTIL.clear_message_in_popup(COSTS.modify_unallocated.unallocated_dialog);
			
			var name = "";
			var description = "";
			var date = "";
			var amount = "";
			var service = "";
			
			if(COSTS.modify_unallocated.current_id) {
				var ue = $.grep(COSTS.load.unallocated_data, function(n) { return n.id == COSTS.modify_unallocated.current_id; });
				if(ue && ue.length > 0) {
					ue = ue[0];
					
					name = ue.name;
					description = ue.description;
					date = UTIL.convert_dates_for_ui(ue.month);
					amount = UTIL.convert_decimal_for_ui(ue.amount);
					service = ue.ospId;
					
					if(ue.costAllocationId) {
						$(COSTS.modify_unallocated.unallocated_already_msg).show();
						$(COSTS.modify_unallocated.unallocated_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").hide();
						$(COSTS.modify_unallocated.unallocated_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button:contains('OK')").show();
					} else {
						$(COSTS.modify_unallocated.unallocated_already_msg).hide();
					}
				}
			}
			
			$(COSTS.modify_unallocated.name_field).val(name);
			$(COSTS.modify_unallocated.description_field).val(description);
			$(COSTS.modify_unallocated.date_field).val(date);
			$(COSTS.modify_unallocated.amount_field).val(amount);
			$(COSTS.modify_unallocated.service_field).val(service);
		},
		submit_unallocated:function() {
			UTIL.clear_message_in_popup(COSTS.modify_unallocated.unallocated_dialog);
			
			var name = $(COSTS.modify_unallocated.name_field).val();
			var description = $(COSTS.modify_unallocated.description_field).val();
			var date = $(COSTS.modify_unallocated.date_field).val();
			var amount = $(COSTS.modify_unallocated.amount_field).val();
			var service_id = $(COSTS.modify_unallocated.service_field).val();
			var ajax_type = "POST";
			
			amount = UTIL.convert_currency_for_server(amount);
			date = UTIL.convert_dates_for_server(date);
			
			var json = { name:name, description:description, month:date, amount:amount, ospId:service_id };
			
			if(COSTS.modify_unallocated.current_id) {
				json["id"] = COSTS.modify_unallocated.current_id;
				ajax_type = "PUT";
			}
			
			UTIL.remove_null_properties(json);
			
			$.ajax ({
				url: PAGE_CONSTANTS.BASE_URL + "unallocatedexpense.json",
				data: JSON.stringify(json),
				type: ajax_type,
				success:function(data) {
					UTIL.remove_dialog_loader(COSTS.modify_unallocated.unallocated_dialog);
					
					if(data.status == PAGE_CONSTANTS.OK_STS) {
						UTIL.add_success_message_to_popup(COSTS.modify_unallocated.unallocated_dialog, "Successfully saved!");
						$(COSTS.modify_unallocated.unallocated_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").hide();
						$(COSTS.modify_unallocated.unallocated_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button:contains('OK')").show();
						
						COSTS.load.get_costs();
					} else {
						UTIL.add_error_message_to_popup(COSTS.modify_unallocated.unallocated_dialog, data.message);
					}
				}
			});
		}
	},
	delete_item: {
		delete_link:".delete-popup-link",
		current_id:null,
		current_type:null,
		item_name_display:"#delete-cost-name",
		ok_cost_deleted_msg:"#ok-cost-deleted-msg",
		init:function() {
			COSTS.delete_item.bind_events();
		},
		bind_events:function() {
			$(document).on("click", COSTS.delete_item.delete_link, function() {
				COSTS.delete_item.current_id = $(this).data("id");
				COSTS.delete_item.current_type = $(this).data("type");
				COSTS.delete_item.setup_popup($(this).data("name"));
			});
		},
		setup_popup:function(name) {
			UTIL.clear_message_in_popup(COSTS.delete_cost_dialog);
			$(COSTS.delete_item.item_name_display).html(name);
		},
		submit_delete:function() {
			UTIL.clear_message_in_popup(COSTS.delete_cost_dialog);
			UTIL.add_dialog_loader(COSTS.delete_cost_dialog);
			
			var url = PAGE_CONSTANTS.BASE_URL + COSTS.delete_item.current_type + "/" + COSTS.delete_item.current_id;
			
			$.ajax ({
				url: url + ".json",
				type: "DELETE",
				success:function(data) {
					UTIL.remove_dialog_loader(COSTS.delete_cost_dialog);
					
					if(data.status == PAGE_CONSTANTS.OK_STS) {
						UTIL.add_success_message_to_popup(COSTS.delete_cost_dialog, $(COSTS.delete_item.ok_cost_deleted_msg).val());
						$(COSTS.delete_cost_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").hide();
						$(COSTS.delete_cost_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button:contains('OK')").show();
						
						COSTS.load.get_costs();
					} else {
						UTIL.add_error_message_to_popup(COSTS.delete_cost_dialog, data.message);
					}
				}
			});
		}
	},
	file_upload: {
		form:"#upload-form",
		form_target:"#upload-target",
		import_type:"#import-type",
		file_field: "#template-file",
		upload_expense_date_container:"#upload-expense-date-container",
		upload_expense_date_field:"#upload-expense-date",
		file_select_error_msg: "#file-select-file-error-msg",
		file_type_invalid_error_msg: "#file-type-invalid-error-msg",
		iframe_status:"#iframe-status",
		iframe_message:"#iframe-message",
		init:function() {
			COSTS.file_upload.bind_events();
		},
		bind_events:function() {
			$(COSTS.file_upload.form_target).load(function(){ 
				var iframe = $(COSTS.file_upload.form_target);
				var status = $(COSTS.file_upload.iframe_status, iframe.contents()).html();
				var message = $(COSTS.file_upload.iframe_message, iframe.contents()).html();
				COSTS.file_upload.complete_submit(status, message);
			});
			
			$(COSTS.file_upload.import_type).change(function() {
				var selected = $(this).val();
				if(selected == "asset") {
					$(COSTS.file_upload.upload_expense_date_container).hide();
				} else {
					$(COSTS.file_upload.upload_expense_date_container).show();
				}
			});
		},
		reset_popup:function() {
			$(COSTS.upload_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").show();
	    	$(COSTS.upload_dialog).closest(".ui-dialog").find(".ui-button:contains('OK')").hide();
	    	UTIL.clear_message_in_popup(COSTS.upload_dialog);
	    	$(COSTS.file_upload.import_type).val("asset");
	    	$(COSTS.file_upload.file_field).val("");
	    	$(COSTS.file_upload.upload_expense_date_field).val("");
	    	$(COSTS.file_upload.upload_expense_date_container).hide();
		},
		submit_file:function() {
			UTIL.clear_message_in_popup(COSTS.upload_dialog);
			
			var file = $(COSTS.file_upload.file_field).val();
			var import_type = $(COSTS.file_upload.import_type).val();
			var expense_date = $(COSTS.file_upload.upload_expense_date_field).val();
			
			if(file == "") {
				var message = $(COSTS.file_upload.file_select_error_msg).val();
				UTIL.add_error_message_to_popup(COSTS.upload_dialog, message);
				return false;
			}
			
			if(import_type == "cost" && !expense_date) {
				var message = "Expense Date is required for the expense upload.";
				UTIL.add_error_message_to_popup(COSTS.upload_dialog, message);
				return false;
			}
			
			if(!(/\.(xlsx)$/i).test(file)) {
				var message = $(COSTS.file_upload.file_type_invalid_error_msg).val();
				UTIL.add_error_message_to_popup(COSTS.upload_dialog, message);
				return false;
			}
			
			UTIL.add_dialog_loader(COSTS.upload_dialog);
			$(COSTS.file_upload.form).submit();
		},
		complete_submit:function(status, message) {
			UTIL.remove_dialog_loader(COSTS.upload_dialog);
			if(status == "success") {
				UTIL.add_success_message_to_popup(COSTS.upload_dialog, message);
				$(COSTS.upload_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").hide();
		    	$(COSTS.upload_dialog).closest(".ui-dialog").find(".ui-button:contains('OK')").show();
		    	COSTS.load.get_costs();
			} else {
				UTIL.add_error_message_to_popup(COSTS.upload_dialog, message);
			}
		}
	},
};

$(document).ready(function() {
	COSTS.init();
});