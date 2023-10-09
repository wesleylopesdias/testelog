var COST_CAT = {
	cost_category_dialog:"#cost-category-dialog",
	delete_cost_category_dialog:"#delete-cost-category-dialog",
	init:function() {
		COST_CAT.bind_events();
		COST_CAT.load.init();
		COST_CAT.modify.init();
		COST_CAT.delete_item.init();
	},
	bind_events:function() {
		$(COST_CAT.cost_category_dialog).dialog({
			  autoOpen:false,
			  dialogClass:"upload-dialog",	
		      resizable:false,
		      width:720,
		      height:650,
		      modal:true,
		      title: "Add a Category",
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
		        	COST_CAT.modify.submit_form();
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
		
		$(COST_CAT.delete_cost_category_dialog).dialog({
			  autoOpen:false,
			  dialogClass:"upload-dialog",	
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
		        	COST_CAT.delete_item.submit_delete();
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
	load: {
		display_view_class: ".contract-services-tab",
		display_views: { expense_cat:"expenses-categories" },
		current_view: "expenses-categories",
		view_table_class: ".category-table",
		categories:[],
		init:function() {
			COST_CAT.load.bind_events();
			COST_CAT.load.get_categories();
		},
		bind_events:function() {
			$(COST_CAT.load.display_view_class).click(function() {
				var view = $(this).data("view");
				COST_CAT.load.current_view = view;
				
				$(COST_CAT.load.display_view_class).removeClass("selected");
				$(this).addClass("selected");
				
				$(COST_CAT.load.view_table_class).hide();
				$("." + view).show();
				
				//get the category data
				COST_CAT.load.get_categories();
			});
		},
		get_categories:function() {
			var view = COST_CAT.load.current_view;
			var url_view = view.replace("-","/");
			UTIL.add_table_loader(COST_CAT.load.view_table_class + "." + view);
			
			$.ajax ({
				url: PAGE_CONSTANTS.BASE_URL + url_view + ".json",
				type: "GET",
				success:function(data) {
					data = UTIL.prepare_data_for_display(data);
					if(view == COST_CAT.load.display_views.expense_cat) {
						data = COST_CAT.load.sort_category_data(data);
						COST_CAT.load.categories = data;
					}
					COST_CAT.load.build_table(data, view);
				}
			});
		},
		sort_category_data:function(data) {
			var parent_categories = [];
			for(var i=0; i < data.length; i++) {
				var obj = data[i];
				if(obj.parent == "") parent_categories.push(obj);
			}
			
			//loop again to put children with parents
			for(var i=0; i < data.length; i++) {
				var obj = data[i];
				if(obj.parent != "") {
					for(var j=0; j < parent_categories.length; j++) {
						var parent = parent_categories[j];
						if(obj.parent.id == parent.id) {
							if(!parent.children) parent["children"] = [];
							parent.children.push(obj);
						}
					}
				}
			}
			return parent_categories;
		},
		build_table:function(data, view) {
			var $table_body = $(COST_CAT.load.view_table_class + "." + view + " tbody");
			var output = "";
			var is_admin = false;
			if(GLOBAL && GLOBAL.is_admin) is_admin = GLOBAL.is_admin;
			
			if(data.length > 0) {
				if(view == COST_CAT.load.display_views.expense_types) {
					for(var i=0; i < data.length; i++) {
						var row = data[i];
						var id = row.id;
						var name = row.name;
						
						output += "<tr id=\"" + COST_CAT.load.current_view + "-" + id + "\">";
						output += "<td name=\"category-name\">";
						output += "<a href=\"javascript:;\" class=\"popup-link edit-category\" data-id=\"" + id + "\" data-dialog=\"cost-category-dialog\">" + name + "</a>";
						output += "<input type=\"hidden\" name=\"category-description\" value=\"" + row.description + "\" />";
						output += "<input type=\"hidden\" name=\"cost-type-expense-type-ref\" value=\"" + row.expenseTypeRefId + "\" />";
						output += "</td>";
						output += "<td>" + row.description + "</td>";
						if(is_admin) {
						output += "<td class=\"right\"><a href=\"javascript:;\" class=\"popup-link delete-popup-link icon-link\" data-id=\"" + id+ "\" data-name=\"" + name + "\" data-dialog=\"delete-cost-category-dialog\"><i class=\"fa fa-minus-circle\"></i>Delete</a></td>";
						}
						output += "</tr>";
					}
				} else if(view == COST_CAT.load.display_views.expense_cat) {
					for(var i=0; i < data.length; i++) {
						var row = data[i];
						var has_children = false;
						if(row.children) has_children = true;
						output += COST_CAT.load.build_category_row(row, false, has_children, is_admin);
						if(has_children) {
							for(var j=0; j < row.children.length; j++) {
								var child_row = row.children[j];
								output += COST_CAT.load.build_category_row(child_row, true, false, is_admin);
							}
						}
					}
				}
			} else {
				output += "<tr><td colspan=\"5\">No results returned.</td></tr>";
			}
			
			$table_body.html(output);
		},
		build_category_row:function(row, is_child, has_children, is_admin) {
			var output = "";
			
			var id = row.id;
			var name = row.name;
			var parent_id = "#";
			var parent_name = "";
			var style = "";
			var icon = "";
			var labor_split = "";
			if(row.parent) {
				parent_id = row.parent.id;
				//parent_name = row.parent.name;
				//style = " style=\"padding-left:25px;\"";
				icon = "<i class=\"fa fa-angle-right\" style=\"margin-left:10px;\"></i>";
				labor_split = UTIL.convert_decimal_to_percent(row.laborSplit, 1) + "%";
			} else {
				//style = " style=\"font-style:italic;\"";
			}
			
			output += "<tr id=\"" + COST_CAT.load.current_view + "-" + id + "\">";
			//output += "<td>" + parent_name + "</td>";
			output += "<td name=\"category-name\"" + style + ">" + icon;
			output += "<a href=\"javascript:;\" class=\"popup-link edit-category\" data-id=\"" + id + "\" data-dialog=\"cost-category-dialog\">"+ name + "</a>";
			output += "<input type=\"hidden\" name=\"category-parent\" value=\"" + parent_id + "\" />";
			output += "<input type=\"hidden\" name=\"category-description\" value=\"" + row.description + "\" />";
			output += "<input type=\"hidden\" name=\"category-target-utilization\" value=\"" + row.targetUtilization + "\" />";
			output += "<input type=\"hidden\" name=\"category-labor-split\" value=\"" + row.laborSplit + "\" />";
			output += "</td>";
			output += "<td>" + row.description + "</td>";
			output += "<td name=\"category-units\">" + row.units + "</td>";
			output += "<td class=\"right\">" + labor_split + "</td>";
			output += "<td class=\"right\">" + row.targetUtilization * 100 + "%</td>";
			if(is_admin) {
				if(!has_children) {
					output += "<td class=\"right\"><a href=\"javascript:;\" class=\"popup-link delete-popup-link icon-link\" data-id=\"" + id+ "\" data-name=\"" + name + "\" data-dialog=\"delete-cost-category-dialog\"><i class=\"fa fa-minus-circle\"></i>Delete</a></td>";
				} else {
					output += "<td>&nbsp;</td>";
				}
			}
			output += "</tr>";
			
			return output;
		}
	},
	modify: {
		parent_type_field:"#cost-category-parent-type",
		cost_category_name_field:"#cost-category-name",
		cost_category_description_field:"#cost-category-description",
		cost_category_units_field:"#cost-category-units",
		cost_category_target_utilization_field:"#cost-category-target-utilization",
		cost_type_expense_type_ref_field:"#cost-type-expense-type-ref",
		cost_category_labor_split_field:"#cost-category-labor-split",
		edit_category_class:".edit-category",
		edit_category_parent:"input[name=\"category-parent\"]",
		edit_category_description:"input[name=\"category-description\"]",
		edit_category_target_utilization:"input[name=\"category-target-utilization\"]",
		edit_category_labor_split:"input[name=\"category-labor-split\"]",
		edit_category_child_labor_split:"input[name=\"child-category-labor-split\"]",
		edit_category_name:"td[name=\"category-name\"]",
		edit_category_expense_type_ref:"input[name=\"cost-type-expense-type-ref\"]",
		edit_category_units:"td[name=\"category-units\"]",
		cost_category_children:"#cost-category-children-container",
		cost_category_labor_split_container:"#cost-category-labor-split-container",
		cost_category_labor_split_total:"#child-category-labor-split-total",
		disribute_evenly_btn:"#distribute-evenly-btn",
		category_display_class:".category-display",
		type_display_class:".type-display",
		current_category_id:null,
		error_required_fields_msg:"#general-error-msg",
		error_utilization_range_msg:"#error-utilization-range-msg",
		error_related_cost_data_msg:"#error-category-related-data-msg",
		ok_category_created_msg:"#ok-category-created-msg",
		init:function() {
			COST_CAT.modify.bind_events();
		},
		bind_events:function() {
			$(document).on("click", COST_CAT.modify.edit_category_class, function() {
				var id = $(this).data("id");
		    	(id == "add") ? COST_CAT.modify.current_category_id = null : COST_CAT.modify.current_category_id = id;
		    	COST_CAT.modify.reset_fields();
	    		return false;
			});
			
			$(document).on("input", COST_CAT.modify.edit_category_child_labor_split, function() {
				COST_CAT.modify.calculate_child_table_total();
			});
			
			$(COST_CAT.modify.disribute_evenly_btn).click(function() {
				COST_CAT.modify.distribute_costs_evenly();
			});
		},
		reset_fields:function() {
			UTIL.clear_message_in_popup(COST_CAT.cost_category_dialog);
			
			$(COST_CAT.modify.cost_category_type_field).val(COST_CAT.load.current_view);
			$(COST_CAT.modify.cost_category_children + " tbody").html("");
			$(COST_CAT.modify.cost_category_children).hide();
			$(COST_CAT.modify.cost_category_labor_split_container).hide();
			
			var name = "";
			var description = "";
			var target_utilization = 0;
			var units = "";
			var parent_category = "#";
			var expense_type_ref = "";
			var labor_split = 0;

			if(COST_CAT.modify.current_category_id != null) {
				var $row = $("#" + COST_CAT.load.current_view + "-" + COST_CAT.modify.current_category_id);
				parent_category = $row.find(COST_CAT.modify.edit_category_parent).val();
				description = $row.find(COST_CAT.modify.edit_category_description).val();
				target_utilization = $row.find(COST_CAT.modify.edit_category_target_utilization).val() * 100;
				name = $row.find(COST_CAT.modify.edit_category_name).find("a").html();
				units = $row.find(COST_CAT.modify.edit_category_units).html();
				expense_type_ref = $row.find(COST_CAT.modify.edit_category_expense_type_ref).val();
				labor_split = $row.find(COST_CAT.modify.edit_category_labor_split).val();
				labor_split = UTIL.convert_decimal_to_percent(labor_split, 1);
				
				var parent = $.grep(COST_CAT.load.categories, function(n) { return n.id == COST_CAT.modify.current_category_id; });
				if(parent && parent.length > 0) {
					parent = parent[0];
					
					var children = parent.children;
					if(children && children.length > 0) {
						COST_CAT.modify.build_child_table(children);
					}
				} else {
					$(COST_CAT.modify.cost_category_labor_split_container).show();
				}
			}
			
			if (COST_CAT.load.current_view == COST_CAT.load.display_views.expense_cat) {
				$(COST_CAT.modify.parent_type_field).val(parent_category);
			}
			
			$(COST_CAT.modify.cost_category_name_field).val(name);
			$(COST_CAT.modify.cost_category_description_field).val(description);
			$(COST_CAT.modify.cost_category_units_field).val(units);
			$(COST_CAT.modify.cost_category_target_utilization_field).val(target_utilization);
			$(COST_CAT.modify.cost_type_expense_type_ref_field).val(expense_type_ref);
			$(COST_CAT.modify.cost_category_labor_split_field).html(labor_split);
		},
		build_child_table:function(children) {
			var output = "";
			
			if(children && children.length > 0) {
				for(var i=0; i < children.length; i++) {
					var child = children[i];
					output += "<tr>";
					output += "<td>" + child.name + "</td>";
					output += "<td class=\"right\"><input type=\"text\" size=\"4\" name=\"child-category-labor-split\" class=\"single-decimal\" value=\"" + UTIL.convert_decimal_to_percent(child.laborSplit, 1) + "\" data-id=\"" + child.id + "\" /> %</td>";
					output += "</tr>";
				}
			}
			
			$(COST_CAT.modify.cost_category_children + " tbody").html(output);
			COST_CAT.modify.calculate_child_table_total();
			$(COST_CAT.modify.cost_category_children).show();
		},
		calculate_child_table_total:function() {
			var footer_output = "";
			var total = 0;
			
			$(COST_CAT.modify.cost_category_children + " tbody tr").each(function() {
				var $row = $(this);
				var split = parseFloat($row.find(COST_CAT.modify.edit_category_child_labor_split).val());
				total = Math.round((total + split) * 1e12) / 1e12;
			});
			
			footer_output = "<tr class=\"total-row\"><td></td><td class=\"right\"><span id=\"child-category-labor-split-total\">" + total + "</span> %</td></tr>";
			$(COST_CAT.modify.cost_category_children + " tfoot").html(footer_output);
		},
		distribute_costs_evenly:function() {
			var rows = $(COST_CAT.modify.cost_category_children + " tbody tr").length;
			var split = (100 / rows).toFixed(1);
			var remainder = 0;
			if(split * rows != 100) {
				remainder = (100 - (split * (rows - 1))).toFixed(1);
			}
			
			var count = 0;
			$(COST_CAT.modify.cost_category_children + " tbody tr").each(function() {
				$split = $(this).find(COST_CAT.modify.edit_category_child_labor_split);
				if(remainder != 0 && count == 0) {
					$split.val(remainder);
				} else {
					$split.val(split);
				}
				count++;
			});
			COST_CAT.modify.calculate_child_table_total();
		},
		submit_form:function() {
			UTIL.clear_message_in_popup(COST_CAT.cost_category_dialog);
			
			var ajax_type = "POST"; 
			var name = $(COST_CAT.modify.cost_category_name_field).val();
			var description = $(COST_CAT.modify.cost_category_description_field).val();
			var units = $(COST_CAT.modify.cost_category_units_field).val();
			var target_utilization = $(COST_CAT.modify.cost_category_target_utilization_field).val();
			var subcategories = null;
			var labor_split = 0;
			var json = null;
			
			if(!name) {
				UTIL.add_error_message_to_popup(COST_CAT.cost_category_dialog, $(COST_CAT.modify.error_required_fields_msg).val());
				return false;
			} else if(target_utilization > 100) {
				UTIL.add_error_message_to_popup(COST_CAT.cost_category_dialog, $(COST_CAT.modify.error_utilization_range_msg).val());
				return false;
			}
			
			if($(COST_CAT.modify.cost_category_children).is(":visible")) {
				var total = $(COST_CAT.modify.cost_category_labor_split_total).html();
				subcategories = [];
				$(COST_CAT.modify.cost_category_children + " tbody tr").each(function() {
					var $split = $(this).find(COST_CAT.modify.edit_category_child_labor_split);
					var id = $split.data("id");
					var child_labor_split = $split.val();
					child_labor_split = UTIL.convert_percent_to_decimal(child_labor_split);
					subcategories.push({ id:id, laborSplit:child_labor_split });
				});
				
				if(total != 100) {
					UTIL.add_error_message_to_popup(COST_CAT.cost_category_dialog, "The labor distribution must total 100%.");
					return false;
				}
			} else if($(COST_CAT.modify.cost_category_labor_split_container).is(":visible")) {
				labor_split = $(COST_CAT.modify.cost_category_labor_split_field).html();
				labor_split = UTIL.convert_percent_to_decimal(labor_split);
			}
			
			target_utilization = target_utilization / 100;
			
			json = { "name":name, "description":description, "units":units, "targetUtilization":target_utilization, subcategories:subcategories, laborSplit:labor_split };
			
			var parent_category = $(COST_CAT.modify.parent_type_field).val();
			var parent_category_name = $(COST_CAT.modify.parent_type_field + " option:selected").text();
			if(parent_category != "#") {
				json["parent"] = { "id":parent_category, "name":parent_category_name };
			}
			
			if(COST_CAT.modify.current_category_id != null) {
				ajax_type = "PUT";
				json["id"] = COST_CAT.modify.current_category_id;
			}
			
			UTIL.add_dialog_loader(COST_CAT.cost_category_dialog);
			
			$.ajax ({
				url: PAGE_CONSTANTS.BASE_URL + "expenses/categories.json",
				data: JSON.stringify(json),
				type: ajax_type,
				success:function(data) {
					UTIL.remove_dialog_loader(COST_CAT.cost_category_dialog);
					
					if(data.status == PAGE_CONSTANTS.OK_STS) {
						UTIL.add_success_message_to_popup(COST_CAT.cost_category_dialog, $(COST_CAT.modify.ok_category_created_msg).val());
						$(COST_CAT.cost_category_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").hide();
						$(COST_CAT.cost_category_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button:contains('OK')").show();
						
						COST_CAT.load.get_categories();
						COST_CAT.modify.get_expense_categories();
					} else {
						UTIL.add_error_message_to_popup(COST_CAT.cost_category_dialog, data.message);
					}
				}
			});
		},
		get_expense_categories:function() {
			$.ajax ({
				url: PAGE_CONSTANTS.BASE_URL + "expenses/categories.json",
				type: "GET",
				success:function(data) {
					COST_CAT.modify.build_expense_dropdown(data);
				}
			});
		},
		build_expense_dropdown:function(categories) {
			//TODO: Remove?
			var options = "<option value=\"#\"></option>";
			
			if(categories) {
				for(var i=0; i < categories.length; i++) {
					var category = categories[i];
					if(!category.parent) options += "<option value=\"" + category.id + "\">" + category.name + "</option>";
				}
			}
			$(COST_CAT.modify.parent_type_field).html(options);
		}
	},
	delete_item: {
		delete_link:".delete-popup-link",
		current_id:null,
		item_name_display:"#delete-cost-name",
		ok_category_deleted_msg:"#ok-category-deleted-msg",
		error_category_related_data_msg:"#error-category-related-data-msg",
		init:function() {
			COST_CAT.delete_item.bind_events();
		},
		bind_events:function() {
			$(document).on("click", COST_CAT.delete_item.delete_link, function() {
				COST_CAT.delete_item.current_id = $(this).data("id");
				COST_CAT.delete_item.setup_popup($(this).data("name"));
			});
		},
		setup_popup:function(name) {
			UTIL.clear_message_in_popup(COST_CAT.delete_cost_category_dialog);
			$(COST_CAT.delete_item.item_name_display).html(name);
		},
		submit_delete:function() {
			UTIL.clear_message_in_popup(COST_CAT.delete_cost_category_dialog);
			UTIL.add_dialog_loader(COST_CAT.delete_cost_category_dialog);
			
			var view = COST_CAT.load.current_view;
			var url = PAGE_CONSTANTS.BASE_URL + view.replace("-","/") + "/" + COST_CAT.delete_item.current_id;
			
			$.ajax ({
				url: url + ".json",
				type: "DELETE",
				success:function(data) {
					UTIL.remove_dialog_loader(COST_CAT.delete_cost_category_dialog);
					
					if(data.status == PAGE_CONSTANTS.OK_STS) {
						UTIL.add_success_message_to_popup(COST_CAT.delete_cost_category_dialog, $(COST_CAT.delete_item.ok_category_deleted_msg).val());
						$(COST_CAT.delete_cost_category_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").hide();
						$(COST_CAT.delete_cost_category_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button:contains('OK')").show();
						
						COST_CAT.load.get_categories();
					} else if(data.status == PAGE_CONSTANTS.RLTD_DT_ERRR) {
						var message = UTIL.create_message_for_related_data(data.message, $(COST_CAT.delete_item.error_category_related_data_msg).val(), data.data);
						UTIL.add_error_message_to_popup(COST_CAT.delete_cost_category_dialog, message);
					} else {
						UTIL.add_error_message_to_popup(COST_CAT.delete_cost_category_dialog, data.message);
					}
				}
			});
		}
	}
};

$(document).ready(function() {
	COST_CAT.init();
});