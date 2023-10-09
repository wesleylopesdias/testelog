var CUSTOMERS = {
	add_customer_popup:"#add-customer-dialog",
	customers_table:"#customers",
	customer_field:"#customer-list",
	customers:null,
	init:function() {
		CUSTOMERS.bind_events();
		CUSTOMERS.read.init();
		CUSTOMERS.update.init();
	},
	bind_events:function() {
		$(CUSTOMERS.add_customer_popup).dialog({
			  autoOpen:false,
			  dialogClass:"customer-dialog",	
		      resizable:false,
		      width:740,
		      height:650,
		      modal:true,
		      title: "Customers",
		      open:function() {
		    	  $(this).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").show();
		    	  $(this).closest(".ui-dialog").find(".ui-button:contains('OK')").hide();
		      },
		      buttons: {
		        "Save":function() {
		        	CUSTOMERS.update.submit_customer();
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
		
		$(document).on("click", ".update-customer", function() {
	    	var id = $(this).data("id");
	    	(id == "add") ? CUSTOMERS.update.current_customer_id = null : CUSTOMERS.update.current_customer_id = id;
	    	CUSTOMERS.update.reset_fields();
    		return false;
    	});
	},
	read:{
		tab:".table-tab",
		init:function() {
			CUSTOMERS.read.bind_events();
			CUSTOMERS.read.load_customer_list();
			CUSTOMERS.read.get_personnel();
		},
		bind_events:function() {
			$(CUSTOMERS.read.tab).click(function() {
				$(CUSTOMERS.read.tab).removeClass("selected");
				$(this).addClass("selected");
				var type = $(this).data("view");
				CUSTOMERS.update.current_tab = type;
				CUSTOMERS.read.load_customer_list(type);
			});
		},
		load_customer_list:function(type) {
			CUSTOMERS.read.get_customers(type);
		},
		get_personnel:function() {
			$.ajax ({
				url: PAGE_CONSTANTS.BASE_URL + "data/personnel.json?a=true",
				type: "GET",
				success:function(data) {
					CUSTOMERS.update.personnel = data;
					CUSTOMERS.update.setup_autocompletes(data);
				}
			});
		},
		get_customers:function(type) {
			UTIL.add_table_loader(CUSTOMERS.customers_table);
			
			var archived = "?a=false";
			if(type == "archived") archived = "?a=true";
			
			$.ajax ({
				url: PAGE_CONSTANTS.BASE_URL + "customers.json" + archived,
				type: "GET",
				success:function(data) {
					CUSTOMERS.customers = data;
					CUSTOMERS.read.build_customers(data);
					if(type != "archived") CUSTOMERS.read.build_customer_dropdown(data);
				}
			});
		},
		build_customers:function(customers) {
			var output = "";
			var url = PAGE_CONSTANTS.BASE_URL + "contracts?cid=";
			if(customers && customers.length > 0) {
				var count = 0;
				for(var i=0;i<customers.length;i++) {
					var customer = customers[i];
					if(customer.siEnabled) {
						count++;
						
						var id = customer.id;
						var csp_name = customer.altName;
						if(!csp_name) csp_name = "";
						var azure_id = customer.azureCustomerId;
						if(!azure_id) azure_id = "";
						var ae = "", epe = "", sdm = "", bsc = "";
						if(customer.accountExecutive) ae = customer.accountExecutive.userName;
						if(customer.enterpriseProgramExecutive) epe = customer.enterpriseProgramExecutive.userName;
						var sdms = customer.serviceDeliveryManagers;
						var bscs = customer.businessSolutionsConsultants;
						if(sdms != null && sdms.length > 0) {
							for(var k = 0; k < sdms.length; k++) {
								if(k > 0) sdm += ", ";
								sdm += sdms[k].userName;
							}
						}
						if(bscs != null && bscs.length > 0) {
							for(var k = 0; k < bscs.length; k++) {
								if(k > 0) bsc += ", ";
								bsc += bscs[k].userName;
							}
						}
						
						
						output += "<tr>";
						output += "<td id=\"customer-" + id + "\">" + customer.name + "</td>";
						output += "<td>" + csp_name + "</td>";
						output += "<td>" + azure_id + "</td>";
						output += "<td>" + ae + "</td>";
						output += "<td>" + epe + "</td>";
						output += "<td>" + sdm + "</td>";
						output += "<td>" + bsc + "</td>";
						output += "<td class=\"right\"><a href=\"" + url + id + "\">" + customer.contractCount + "</a></td>";
						output += "<td class=\"center\"><a href=\"javascript:;\" class=\"update-customer popup-link\" data-dialog=\"add-customer-dialog\" data-id=\"" + id + "\">Edit</a></td>";
					    output += "</tr>";
					}
				}
				
				if(count == 0) {
					output = "<tr><td colspan=\"4\" class=\"no-results\">No customers found.</td></tr>";
				}
			} else {
				output = "<tr><td colspan=\"4\" class=\"no-results\">No customers found.</td></tr>";
			}
			$(CUSTOMERS.customers_table).find("tbody").html(output);
			$(CUSTOMERS.customers_table).show();
		},
		build_customer_dropdown:function(customers) {
			var output = "<option value=\"\"></option>";
			for(var i=0; i<customers.length; i++) {
				var customer = customers[i];
				if(!customer.siEnabled) output += "<option value=\"" + customer.id + "\">" + customer.name + "</option>";
			}
			$(CUSTOMERS.customer_field).html(output);
		}
	},
	update: {
		current_customer_id: null,
		customer_name_field:"#customer-name",
		customer_alt_name_field:"#customer-alt-name",
		customer_azure_id_field:"#customer-azure-id",
		customer_archived_field:"#customer-archived",
		customer_sdms_field:"#customer-sdms",
		customer_bscs_field:"#customer-bscs",
		customer_ae_field:"#customer-ae",
		customer_epe_field:"#customer-epe",
		autocomplete_value:"input[name='autocomplete-value']",
		multiselect_display:".value-second-row",
		add_customer_msg:"#add-customer-msg",
		update_customer_msg:"#update-customer-msg",
		general_error_msg:"#general-error-msg",
		add_person_btn:".add-person-btn",
		bscs: [],
		sdms: [],
		personnel: [],
		personnel_types:{ ae:"ae", bsc:"bsc", epe:"epe", sdm:"sdm" },
		current_tab:"active",
		add_class:".add",
		edit_class:".edit",
		init:function() {
			CUSTOMERS.update.bind_events();
		},
		bind_events:function() {
			$(CUSTOMERS.update.add_person_btn).click(function() {
				var label = $(QUOTES.create.presales_rep).val();
				CUSTOMERS.update.add_multiselect(label, null);
			});
			
			$(".dialog-content").on("click", ".multiselect-display a", function() {
				var $parent = $(this).parent();
				CUSTOMERS.update.remove_multiselect($parent.data("name"), $parent.data("id"));
				$parent.remove();
			});
		},
		setup_autocompletes:function(personnel) {
			var bscs =[];
			var sdms = [];
			var aes = [];
			var epes = [];
			
			for(var i = 0; i < personnel.length; i++) {
				var person = personnel[i];
				var type = person.type;
				if(type == CUSTOMERS.update.personnel_types.ae) {
					aes.push({id:person.userId, value:person.userName, type:CUSTOMERS.update.personnel_types.ae});
				} else if(type == CUSTOMERS.update.personnel_types.bsc) {
					bscs.push({id:person.userId, value:person.userName, type: CUSTOMERS.update.personnel_types.bsc});
				} else if(type == CUSTOMERS.update.personnel_types.sdm) {
					sdms.push({id:person.userId, value:person.userName, type:CUSTOMERS.update.personnel_types.sdm});
				} else if(type == CUSTOMERS.update.personnel_types.epe) {
					epes.push({id:person.userId, value:person.userName, type:CUSTOMERS.update.personnel_types.epe});
				}
			}
			
			CUSTOMERS.update.setup_autocomplete(CUSTOMERS.update.customer_ae_field, aes);
			CUSTOMERS.update.setup_autocomplete(CUSTOMERS.update.customer_epe_field, epes);
			CUSTOMERS.update.setup_multi_autocomplete(CUSTOMERS.update.customer_sdms_field, sdms);
			CUSTOMERS.update.setup_multi_autocomplete(CUSTOMERS.update.customer_bscs_field, bscs);
		},
		setup_multi_autocomplete:function(field, source) {
			$(field).autocomplete({
			      minLength: 0,
			      source: source,
			      focus: function(event, ui) {
			    	$(field).val(ui.item.label);
			        return false;
			      },
			      select: function( event, ui ) {
			    	CUSTOMERS.update.add_multiselect(field, ui.item.label, ui.item.value);
			        return false;
			      }
			 });
		},
		setup_autocomplete:function(field, source) {
			if($(field).autocomplete()) $(field).autocomplete("destroy");
			
			$(field).autocomplete({
			      minLength: 0,
			      source: source,
			      focus: function(event, ui) {
			        $(field).val(ui.item.label);
			        return false;
			      },
			      select: function(event, ui) {
			        $(field).val(ui.item.label);
			        $(field).siblings(CUSTOMERS.update.autocomplete_value).val(ui.item.value);
			        return false;
			      }
			 });
			
			$(field).change(function() {
				var val = $(this).val();
				var results = $.grep(source, function(n) { return n.label == val; });
				if(results.length == 0) $(field).siblings(CUSTOMERS.update.autocomplete_value).val("");
			});
		},
		add_multiselect:function(field, label, value, type) {
			if(value != "") {
				var output = "<span class=\"multiselect-display\" data-id=\"" + value + "\" data-name=\"" +label + "\" data-type=\"" + type + "\">";
				output += label;
				output += "<a href=\"javascript:;\"><i class=\"fa fa-times\"></i></a>";
				output += "</span>";
				$(field).siblings(CUSTOMERS.update.multiselect_display).append(output);
				
				//add to array
				var record = { id:value, name:label };
				//QUOTES.create.presales_reps.push(record);
				
				//clear field
				$(field).val("");
			}
		},
		remove_multiselect:function(name, id, type) {
			if(type == CUSTOMERS.update.personnel_types.bsc) {
				//remove by id, otherwise, remove by the name itself if the id doesn't exist
				if(id) {
					CUSTOMERS.update.bscs = $.grep(CUSTOMERS.update.bscs, function(n) { return n.userId != id; });
				} else {
					CUSTOMERS.update.bscs = $.grep(CUSTOMERS.update.bscs, function(n) { return n.name != name; });
				}
			} else if(type == CUSTOMERS.update.personnel_types.sdm) {
				//remove by id, otherwise, remove by the name itself if the id doesn't exist
				if(id) {
					CUSTOMERS.update.sdms = $.grep(CUSTOMERS.update.sdms, function(n) { return n.userId != id; });
				} else {
					CUSTOMERS.update.sdms = $.grep(CUSTOMERS.update.sdms, function(n) { return n.name != name; });
				}
			}
		},
		reset_fields:function() {
			var name = "";
			var alt_name = "";
			var azure_id = "";
			var archived = false;
			var pcr_enabled = false;
			var ae_name = "";
			var epe_name = "";
			
			$(CUSTOMERS.update.customer_bscs_field).siblings(CUSTOMERS.update.multiselect_display).html("");
			$(CUSTOMERS.update.customer_sdms_field).siblings(CUSTOMERS.update.multiselect_display).html("");
			
			if(CUSTOMERS.update.current_customer_id != null) {
				var customer = $.grep(CUSTOMERS.customers, function(n) { return n.id == CUSTOMERS.update.current_customer_id; });
				if(customer != null && customer.length > 0) {
					customer = customer[0];
					name = customer.name;
					alt_name = customer.altName;
					archived = customer.archived;
					azure_id = customer.azureCustomerId;
					if(customer.accountExecutive != null) ae_name = customer.accountExecutive.userName;
					if(customer.enterpriseProgramExecutive != null) epe_name = customer.enterpriseProgramExecutive.userName;
					
					var bscs = customer.businessSolutionsConsultants;
					if(bscs != null && bscs.length > 0) {
						for(var i = 0; i < bscs.length; i++) {
							var bsc = bscs[i];
							CUSTOMERS.update.add_multiselect(CUSTOMERS.update.customer_bscs_field, bsc.userName, bsc.userId, bsc.type);
						}
					}
					
					var sdms = customer.serviceDeliveryManagers;
					if(sdms != null && sdms.length > 0) {
						for(var i = 0; i < sdms.length; i++) {
							var sdm = sdms[i];
							CUSTOMERS.update.add_multiselect(CUSTOMERS.update.customer_sdms_field, sdm.userName, sdm.userId, sdm.type);
						}
					}
					
					$(CUSTOMERS.update.customer_name_field).html(name);
				}
				
				$(PAGE_CONSTANTS.REQUIRED_CLASS.SELECTOR).hide();
				$(CUSTOMERS.update.add_class).hide();
				$(CUSTOMERS.update.edit_class).show();
			} else {
				$(PAGE_CONSTANTS.REQUIRED_CLASS.SELECTOR).show();
				$(CUSTOMERS.update.add_class).show();
				$(CUSTOMERS.update.edit_class).hide();
			}
			
			$(CUSTOMERS.update.customer_alt_name_field).val(alt_name);
			$(CUSTOMERS.update.customer_azure_id_field).val(azure_id);
			$(CUSTOMERS.update.customer_archived_field).prop("checked", archived);
			$(CUSTOMERS.update.customer_ae_field).val(ae_name);
			$(CUSTOMERS.update.customer_epe_field).val(epe_name);
			UTIL.clear_message_in_popup(CUSTOMERS.add_customer_popup);
		},
		build_personnel_for_save:function(user_name, customer_id, type) {
			var personnel = null;
			var person = $.grep(CUSTOMERS.update.personnel, function(n) { return n.userName == user_name; });
			if(person && person.length > 0) {
				person = person[0];
				personnel = { userId:person.userId, customerId:customer_id, type:type };
			}
			return personnel;
			
		},
		submit_customer:function() {
			UTIL.clear_message_in_popup(CUSTOMERS.add_customer_popup);
			
			var id = $(CUSTOMERS.customer_field).val();
			var ajax_type = "PUT";
			var alt_name = $(CUSTOMERS.update.customer_alt_name_field).val();
			var azure_id = $(CUSTOMERS.update.customer_azure_id_field).val();
			var archived = $(CUSTOMERS.update.customer_archived_field).prop("checked");
			var ae_name = $(CUSTOMERS.update.customer_ae_field).val();
			var epe_name = $(CUSTOMERS.update.customer_epe_field).val();
			var $bscs = $(CUSTOMERS.update.customer_bscs_field).siblings(CUSTOMERS.update.multiselect_display).find(".multiselect-display");
			var $sdms = $(CUSTOMERS.update.customer_sdms_field).siblings(CUSTOMERS.update.multiselect_display).find(".multiselect-display");
			var bscs = [];
			var sdms = [];
			var ae = null;
			var epe = null;
			
			
			//validate
			if(CUSTOMERS.update.current_customer_id == null && !id) {
				UTIL.add_error_message_to_popup(CUSTOMERS.add_customer_popup, $(CUSTOMERS.update.general_error_msg).val());
				return false;
			}
			
			if(CUSTOMERS.update.current_customer_id != null) {
				id = CUSTOMERS.update.current_customer_id;
			}
			
			if(ae_name) {
				ae = CUSTOMERS.update.build_personnel_for_save(ae_name, id, CUSTOMERS.update.personnel_types.ae);
				if(ae == null) {
					UTIL.add_error_message_to_popup(CUSTOMERS.add_customer_popup, "No AE found in the database with the name " + ae_name);
					return false;
				}
			}
			
			if(epe_name) {
				epe = CUSTOMERS.update.build_personnel_for_save(epe_name, id, CUSTOMERS.update.personnel_types.epe);
				if(epe == null) {
					UTIL.add_error_message_to_popup(CUSTOMERS.add_customer_popup, "No EPE found in the database with the name " + ae_name);
					return false;
				}
			}
			
			var bsc_names = "";
			$bscs.each(function() {
				var bsc_name = $(this).data("name");
				var bsc = CUSTOMERS.update.build_personnel_for_save(bsc_name, id, CUSTOMERS.update.personnel_types.bsc);
				if(bsc == null) {
					bsc_names += bsc_name + " ";
				} else {
					bscs.push(bsc);
				}
			});
			
			if(bsc_names) {
				UTIL.add_error_message_to_popup(CUSTOMERS.add_customer_popup, "No BSC found in the database with the name(s) " + bsc_names);
				return false;
			}
			
			var sdm_names = "";
			$sdms.each(function() {
				var sdm_name = $(this).data("name");
				var sdm = CUSTOMERS.update.build_personnel_for_save(sdm_name, id, CUSTOMERS.update.personnel_types.sdm);
				if(sdm == null) {
					sdm_names += sdm_name + " ";
				} else {
					sdms.push(sdm);
				}
			});
			
			if(sdm_names) {
				UTIL.add_error_message_to_popup(CUSTOMERS.add_customer_popup, "No SDM found in the database with the name(s) " + sdm_names);
				return false;
			}
			
			var json = { id:id, archived:archived, siEnabled:true, altName:alt_name, azureCustomerId:azure_id, accountExecutive:ae, enterpriseProgramExecutive:epe, businessSolutionsConsultants:bscs, serviceDeliveryManagers:sdms };
			
			UTIL.add_dialog_loader(CUSTOMERS.add_customer_popup);
			
			$.ajax ({
				url: PAGE_CONSTANTS.BASE_URL + "customers.json",
				data: JSON.stringify(json),
				type: ajax_type,
				success:function(data) {
					UTIL.remove_dialog_loader(CUSTOMERS.add_customer_popup);
					if(data.status == PAGE_CONSTANTS.OK_STS) {
						
						//update the popup
						var success_msg = $(CUSTOMERS.update.add_customer_msg).val();
						if(CUSTOMERS.update.current_customer_id != null) success_msg = $(CUSTOMERS.update.update_customer_msg).val();
						UTIL.add_success_message_to_popup(CUSTOMERS.add_customer_popup, success_msg);
						$(CUSTOMERS.add_customer_popup).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").hide();
						$(CUSTOMERS.add_customer_popup).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button:contains('OK')").show();
						
						var name = $(CUSTOMERS.customer_field + " option:selected").text();
						$(CUSTOMERS.update.customer_name_field).html(name);
						$(PAGE_CONSTANTS.REQUIRED_CLASS.SELECTOR).hide();
						$(CUSTOMERS.update.add_class).hide();
						$(CUSTOMERS.update.edit_class).show();
						
						CUSTOMERS.read.load_customer_list(CUSTOMERS.update.current_tab);
					}
				}
			});
		}
	}
};

$(document).ready(function() {
	CUSTOMERS.init();
});