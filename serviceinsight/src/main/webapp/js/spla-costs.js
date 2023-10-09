var SPLA = {
	init:function() {
		SPLA.read.init();
		SPLA.modify.init();
		SPLA.del.init();
	},
	read:{
		spla_container:"#spla",
		spla:[],
		init:function() {
			SPLA.read.bind_events();
			SPLA.read.get_spla();
		},
		bind_events:function() {
			
		},
		get_spla:function() {
			$.ajax ({
				url: PAGE_CONSTANTS.BASE_URL + "settings/spla.json?active=true",
				type: "GET",
				success:function(data) {
					SPLA.read.build_table(data);
					SPLA.read.spla = data;
				}
			});
		},
		build_table:function(data) {
			var output = "";
			
			if(data && data.length > 0) {
				for(var i = 0; i < data.length; i++) {
					var spla = data[i];
					output += "<tr>";
					output += "<td><a href=\"javascript:;\" class=\"popup-link spla-popup-link\" data-id=\"" + spla.id + "\" data-dialog=\"spla-cost-dialog\">" + spla.name + "</a></td>";
					output += "<td>" + spla.altId + "</td>";
					output += "<td>" + spla.vendor + "</td>";
					output += "<td>" + UTIL.convert_currency_for_ui(spla.cost) + "</td>";
					output += "<td></td>";
					output += "</tr>";
				}
			} else {
				output = "<tr><td class=\"no-results\" colspan=\"4\">No SPLA Costs returned.</td></tr>";
			}
			
			$(SPLA.read.spla_container + " tbody").html(output);
		}
	},
	modify:{
		spla_cost_dialog:"#spla-cost-dialog",
		name_field:"#spla-name",
		alt_id_field:"#spla-alt-id",
		cost_field:"#spla-cost",
		vendor_field:"#spla-vendor",
		type_field:"#spla-type",
		cost_category_field:"#spla-cost-category",
		active_field:"#spla-active",
		current_id:null,
		init:function() {
			SPLA.modify.bind_events();
		},
		bind_events:function() {
			$(SPLA.modify.spla_cost_dialog).dialog({
				  autoOpen:false,
				  dialogClass:"spla-dialog",	
			      resizable:false,
			      width:720,
			      height:550,
			      modal:true,
			      title: "Add a SPLA Cost",
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
			        	SPLA.modify.submit_form();
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
			
			$(document).on("click", ".spla-popup-link", function() {
				var id = $(this).data("id");
				(id == "add") ? SPLA.modify.current_id = null : SPLA.modify.current_id = id;
				SPLA.modify.reset_popup();
			});
		},
		reset_popup:function() {
			UTIL.clear_message_in_popup(SPLA.modify.spla_cost_dialog);
			
			var name = "";
			var alt_id = "";
			var cost = "";
			var vendor = "other";
			var active = "true";
			var type = "single";
			var cost_category_id = "";
			
			if(SPLA.modify.current_id != null) {
				var spla = $.grep(SPLA.read.spla, function(n) { return n.id == SPLA.modify.current_id; });
				if(spla && spla.length > 0) {
					spla = spla[0];
					
					name = spla.name;
					alt_id = spla.altId;
					cost = spla.cost;
					vendor = spla.vendor;
					active = spla.active.toString();
					type = spla.type;
					cost_category_id = spla.expenseCategoryId;
				}
			}
			
			$(SPLA.modify.name_field).val(name);
			$(SPLA.modify.alt_id_field).val(alt_id);
			$(SPLA.modify.cost_field).val(cost);
			$(SPLA.modify.vendor_field).val(vendor);
			$(SPLA.modify.active_field).val(active);
			$(SPLA.modify.type_field).val(type);
			$(SPLA.modify.cost_category_field).val(cost_category_id);
			
		},
		submit_form:function() {
			UTIL.clear_message_in_popup(SPLA.modify.spla_cost_dialog);
			
			var ajax_type = "POST"; 
			var name = $(SPLA.modify.name_field).val();
			var alt_id = $(SPLA.modify.alt_id_field).val();
			var cost = $(SPLA.modify.cost_field).val();
			var vendor = $(SPLA.modify.vendor_field).val();
			var active = $(SPLA.modify.active_field).val();
			var type = $(SPLA.modify.type_field).val();
			var cost_category_id = $(SPLA.modify.cost_category_field).val();
			
			if(!name || cost == null || !type || !cost_category_id) {
				UTIL.add_error_message_to_popup(SPLA.modify.spla_cost_dialog, "Please Enter All Required Fields.");
				return false;
			}
			
			cost = UTIL.convert_currency_for_server(cost);
			
			var json = { "name":name, "altId":alt_id, "cost":cost, "vendor":vendor, "active":active, "type":type, "expenseCategoryId":cost_category_id };
			
			if(SPLA.modify.current_id != null) {
				ajax_type = "PUT";
				json["id"] = SPLA.modify.current_id;
			}
			
			UTIL.add_dialog_loader(SPLA.modify.spla_cost_dialog);
			
			$.ajax ({
				url: PAGE_CONSTANTS.BASE_URL + "settings/spla.json",
				data: JSON.stringify(json),
				type: ajax_type,
				success:function(data) {
					UTIL.remove_dialog_loader(SPLA.modify.spla_cost_dialog);
					
					if(data.status == PAGE_CONSTANTS.OK_STS) {
						UTIL.add_success_message_to_popup(SPLA.modify.spla_cost_dialog, "Success!");
						$(SPLA.modify.spla_cost_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").hide();
						$(SPLA.modify.spla_cost_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button:contains('OK')").show();
						
						SPLA.read.get_spla();
					} else {
						UTIL.add_error_message_to_popup(SPLA.modify.spla_cost_dialog, data.message);
					}
				}
			});
		}
	},
	del:{
		init:function() {
			SPLA.del.bind_events();
		},
		bind_events:function() {
			
		}
	}
};

$(document).ready(function() {
	SPLA.init();
});