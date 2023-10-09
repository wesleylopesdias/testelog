var QUOTES = {
	init:function() {
		QUOTES.bind_events();
		QUOTES.read.init();
		QUOTES.import.init();
		QUOTES.customers.init();
	},
	bind_events:function() {
		
	},
	read:{
		quotes_table_container:"#quotes-table-container",
		quotes_table:"#quotes-table",
		quote_to_import_field:"input[name='quote-to-import']",
		init:function() {
			QUOTES.read.bind_events();
			QUOTES.read.get_personnel();
		},
		bind_events:function() {
			
		},
		build_quotes:function(quotes) {
			var output = "";
			
			if(quotes.length > 0) {
				for(var i=0; i < quotes.length; i++) {
					var quote = quotes[i];
					var id = quote.id;
					var quote_name = "";
					if(quote.quoteName) quote_name = quote.quoteName;
					
					output += "<tr>";
					output += "<td><input type=\"radio\" name=\"quote-to-import\" value=\"" + id + "\" /></td>";
					output += "<td>" + quote.quoteNumber + "</td>";
					output += "<td>" + quote_name + "</td>";
					//output += "<td class=\"center\">" + UTIL.convert_dates_for_ui_alt(quote.closeDate) + "</td>";
					output += "<td class=\"center\">" + quote.termMonths + " Months</td>";
					output += "<td class=\"right\">" + UTIL.convert_currency_for_ui(quote.onetimePrice) + "</td>";
					output += "<td class=\"right\">" + UTIL.convert_currency_for_ui(quote.recurringPrice) + "</td>";
					output += "<td class=\"right\">" + UTIL.convert_currency_for_ui(quote.total) + "</td>";
	        		output += "</tr>";
				}
			} else {
				output = "<tr><td colspan=\"5\" class=\"no-results\">No Quotes Were Returned...</td></tr>";
			}
			
			$(QUOTES.read.quotes_table + " tbody").html(output);
		},
		get_quotes:function(customer_id) {
			$(QUOTES.read.quotes_table_container).show();
			UTIL.add_table_loader(QUOTES.read.quotes_table, "Loading Quotes from Pricing...");
			
			$.ajax ({
				url: PAGE_CONSTANTS.BASE_URL + "pricing/quotes/list.json?cid=" + customer_id,
				type: "GET",
				success:function(data) {
					QUOTES.read.build_quotes(data);
				}
			});
		},
		get_personnel:function() {
			$.ajax ({
				url: PAGE_CONSTANTS.BASE_URL + "data/personnel.json?a=true",
				type: "GET",
				success:function(data) {
					QUOTES.import.personnel = data;
					QUOTES.import.setup_autocompletes(data);
				}
			});
		}
	},
	customers:{
		customers:null,
		customer_field:"#quote-customers",
		loader:"#customer-loader",
		init:function() {
			QUOTES.customers.bind_events();
		},
		bind_events:function() {
			$(QUOTES.customers.customer_field).change(function() {
				var customer_id = $(this).val();
				if(customer_id) {
					QUOTES.read.get_quotes(customer_id);
				} else {
					$(QUOTES.read.quotes_table_container).hide();
				}
				
			});
		},
		build_dropdown:function(customers) {
			var output = "<option value=\"\"></option>";
			if(customers && customers.length > 0) {
				for(var i=0; i < customers.length; i++) {
					var customer = customers[i];
					var alt_id = customer.altId;
					if(alt_id) output += "<option value=\"" + alt_id + "\">" + customer.name + "</option>"; 
				}
			}
			$(QUOTES.customers.customer_field).html(output);
		},
		get_customers:function() {
			$.ajax ({
				url: PAGE_CONSTANTS.BASE_URL + "customers.json",
				type: "GET",
				success:function(data) {
					QUOTES.customers.customers = data;
					QUOTES.customers.build_dropdown(data);
					$(QUOTES.customers.loader).hide();
				}
			});
		},
		get_customer:function() {
			var customer_id = $(QUOTES.import.customer_field).val();
			if(customer_id) {
				$.ajax ({
					url: PAGE_CONSTANTS.BASE_URL + "customers/" + customer_id + ".json",
					type: "GET",
					success:function(data) {
						QUOTES.import.customer = data;
						QUOTES.import.preset_personnel();
					}
				});
			}
		},
	},
	import:{
		import_dialog:"#import-quote-dialog",
		step_class:".step",
		current_quote_id:null,
		customer_field:"#customer",
		contract_name_field:"#import-contract-name",
		alt_id_field:"#import-contract-alt-id",
		job_number_field:"#import-contract-job-number",
		engagement_manager_field:"#import-contract-engagement-manager",
		account_exec_field:"#import-contract-account-exec",
		signed_date_field:"#import-contract-signed-date",
		service_start_date_field:"#import-contract-service-start-date",
		start_date_field:"#import-contract-start-date",
		end_date_field:"#import-contract-end-date",
		contract_sdms_field:"#import-contract-sdms",
		contract_bscs_field:"#import-contract-bscs",
		contract_ae_field:"#import-contract-ae",
		contract_epe_field:"#import-contract-epe",
		autocomplete_value:"input[name='autocomplete-value']",
		multiselect_display:".value-second-row",
		add_person_btn:".add-person-btn",
		customer:null,
		bscs: [],
		sdms: [],
		personnel: [],
		personnel_types:{ ae:"ae", bsc:"bsc", epe:"epe", sdm:"sdm" },
		general_error_msg:"#general-error-msg",
		import_success_msg:"#import-quote-success-msg",
		customer_quote_required_msg:"#import-quote-error-customer-quote-required-msg",
		init:function() {
			QUOTES.import.bind_events();
		},
		bind_events:function() {
			$(QUOTES.import.import_dialog).dialog({
				  autoOpen:false,
				  dialogClass:"import-dialog",	
			      resizable:false,
			      width:800,
			      height:670,
			      modal:true,
			      title: "Import an SOW",
			      open:function() {
			    	  $('.datepicker').datepicker('enable');
			    	  QUOTES.import.reset_form();
			      },
			      close:function() {
			    	  $('.datepicker').datepicker('disable');
			      },
			      buttons: {
			        "Prev":function() {
			        	QUOTES.import.move_step(false);
			        },
			        "Next":function() {
			        	QUOTES.import.move_step(true);
			        },
			        "Import":function() {
			        	QUOTES.import.submit_import();
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
			
			$(QUOTES.import.add_person_btn).click(function() {
				var label = $(this).siblings("input[type='text']").val();
				QUOTES.import.add_multiselect(label, null);
			});
			
			$(".dialog-content").on("click", ".multiselect-display a", function() {
				var $parent = $(this).parent();
				QUOTES.import.remove_multiselect($parent.data("name"), $parent.data("id"));
				$parent.remove();
			});
		},
		move_step:function(forward) {
			var $dialog = $(QUOTES.import.import_dialog);
			UTIL.clear_message_in_popup($dialog);
			
			if(forward) {
				//validate
				var customer_id = $(QUOTES.customers.customer_field).val();
				var quote_id = $(QUOTES.read.quote_to_import_field + ":checked").val();
				
				if(!customer_id || !quote_id) {
					UTIL.add_error_message_to_popup($dialog, $(QUOTES.import.customer_quote_required_msg).val());
					return false;
				}
				
				QUOTES.import.current_quote_id = quote_id;
				
				$dialog.find(QUOTES.import.step_class).hide();
				$dialog.find(QUOTES.import.step_class + ":last-child").fadeIn("fast");
				$dialog.closest(".ui-dialog").find(".ui-button:contains('Prev')").show();
				$dialog.closest(".ui-dialog").find(".ui-button:contains('Import')").show();
				$dialog.closest(".ui-dialog").find(".ui-button:contains('Next')").hide();
			} else {
				$dialog.find(QUOTES.import.step_class).hide();
				$dialog.find(QUOTES.import.step_class + ":first-child").fadeIn("fast");
				$dialog.closest(".ui-dialog").find(".ui-button:contains('Prev')").hide();
				$dialog.closest(".ui-dialog").find(".ui-button:contains('Import')").hide();
				$dialog.closest(".ui-dialog").find(".ui-button:contains('Next')").show();
			}
		},
		reset_form:function() {
			var $dialog = $(QUOTES.import.import_dialog);
			UTIL.clear_message_in_popup(QUOTES.import.import_dialog);
			$dialog.closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").hide();
			$dialog.closest(".ui-dialog").find(".ui-button:contains('Next')").show();
			$dialog.closest(".ui-dialog").find(".ui-button:contains('Cancel')").show();
			
			$dialog.find(QUOTES.import.step_class).hide();
			$dialog.find(QUOTES.import.step_class + ":first-child").show();
			
			QUOTES.import.current_quote_id = null;
			QUOTES.import.customer = null;
			
			if(!QUOTES.read.customers) {
				QUOTES.customers.get_customers();
			}
			
			QUOTES.customers.get_customer();
			
			$(QUOTES.import.contract_bscs_field).siblings(QUOTES.import.multiselect_display).html("");
			$(QUOTES.import.contract_sdms_field).siblings(QUOTES.import.multiselect_display).html("");
			
			$(QUOTES.customers.customer_field).val("");
			$(QUOTES.import.contract_name_field).val("");
			$(QUOTES.import.alt_id_field).val("");
			$(QUOTES.import.job_number_field).val("");
			$(QUOTES.import.signed_date_field).val("");
			$(QUOTES.import.service_start_date_field).val("");
			$(QUOTES.import.start_date_field).val("");
			$(QUOTES.import.end_date_field).val("");
			$(QUOTES.import.contract_sdms_field).val("");
			$(QUOTES.import.contract_bscs_field).val("");
			$(QUOTES.import.contract_ae_field).val("");
			$(QUOTES.import.contract_epe_field).val("");
			
			$(QUOTES.read.quotes_table_container).hide();
		},
		preset_personnel:function() {
			var ae_name = "";
			var epe_name = "";
			var bscs = null;
			var sdms = null;
			
			var customer = QUOTES.import.customer;
			if(customer != null) {
				if(customer.accountExecutive != null) ae_name = customer.accountExecutive.userName;
				if(customer.enterpriseProgramExecutive != null) epe_name = customer.enterpriseProgramExecutive.userName;
				bscs = customer.businessSolutionsConsultants;
				sdms = customer.serviceDeliveryManagers;
			}
			
			if(bscs != null && bscs.length > 0) {
				bscs.forEach(bsc => {
					QUOTES.import.add_multiselect(QUOTES.import.contract_bscs_field, bsc.userName, bsc.userId, bsc.type);
				});
			}
			
			if(sdms != null && sdms.length > 0) {
				sdms.forEach(sdm => {
					QUOTES.import.add_multiselect(QUOTES.import.contract_sdms_field, sdm.userName, sdm.userId, sdm.type);
				});
			}
			
			$(QUOTES.import.contract_ae_field).val(ae_name);
			$(QUOTES.import.contract_epe_field).val(epe_name);
		},
		mark_quote_as_won:function() {
			$.ajax ({
				url: PAGE_CONSTANTS.BASE_URL + "pricing/quotes/won/" + QUOTES.import.current_quote_id + ".json",
				type: "PUT",
				success:function(data) {
				}
			});
		},
		setup_autocompletes:function(personnel) {
			var bscs =[];
			var sdms = [];
			var aes = [];
			var epes = [];
			
			personnel.forEach(person => {
				var type = person.type;
				if(type == QUOTES.import.personnel_types.ae) {
					aes.push({id:person.userId, value:person.userName, type:QUOTES.import.personnel_types.ae});
				} else if(type == QUOTES.import.personnel_types.bsc) {
					bscs.push({id:person.userId, value:person.userName, type:QUOTES.import.personnel_types.bsc});
				} else if(type == QUOTES.import.personnel_types.sdm) {
					sdms.push({id:person.userId, value:person.userName, type:QUOTES.import.personnel_types.sdm});
				} else if(type == QUOTES.import.personnel_types.epe) {
					epes.push({id:person.userId, value:person.userName, type:QUOTES.import.personnel_types.epe});
				}
			});
			
			QUOTES.import.setup_autocomplete(QUOTES.import.contract_ae_field, aes);
			QUOTES.import.setup_autocomplete(QUOTES.import.contract_epe_field, epes);
			QUOTES.import.setup_multi_autocomplete(QUOTES.import.contract_sdms_field, sdms);
			QUOTES.import.setup_multi_autocomplete(QUOTES.import.contract_bscs_field, bscs);
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
			    	  CONTRACTS.update.add_multiselect(field, ui.item.label, ui.item.value);
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
			        $(field).siblings(QUOTES.import.autocomplete_value).val(ui.item.value);
			        return false;
			      }
			 });
			
			$(field).change(function() {
				var val = $(this).val();
				var results = $.grep(source, function(n) { return n.label == val; });
				if(results.length == 0) $(field).siblings(QUOTES.import.autocomplete_value).val("");
			});
		},
		add_multiselect:function(field, label, value, type) {
			if(value != "") {
				var output = "<span class=\"multiselect-display\" data-id=\"" + value + "\" data-name=\"" +label + "\" data-type=\"" + type + "\">";
				output += label;
				output += "<a href=\"javascript:;\"><i class=\"fa fa-times\"></i></a>";
				output += "</span>";
				$(field).siblings(QUOTES.import.multiselect_display).append(output);
				
				//add to array
				var record = { id:value, name:label };
				//QUOTES.create.presales_reps.push(record);
				
				//clear field
				$(field).val("");
			}
		},
		remove_multiselect:function(name, id, type) {
			if(type == QUOTES.import.personnel_types.bsc) {
				//remove by id, otherwise, remove by the name itself if the id doesn't exist
				if(id) {
					QUOTES.import.bscs = $.grep(QUOTES.import.bscs, function(n) { return n.userId != id; });
				} else {
					QUOTES.import.bscs = $.grep(QUOTES.import.bscs, function(n) { return n.name != name; });
				}
			} else if(type == QUOTES.import.personnel_types.sdm) {
				//remove by id, otherwise, remove by the name itself if the id doesn't exist
				if(id) {
					QUOTES.import.sdms = $.grep(QUOTES.import.sdms, function(n) { return n.userId != id; });
				} else {
					QUOTES.import.sdms = $.grep(QUOTES.import.sdms, function(n) { return n.name != name; });
				}
			}
		},
		build_personnel_for_save:function(user_name, contract_id, type) {
			var personnel = null;
			var person = $.grep(QUOTES.import.personnel, function(n) { return n.userName == user_name; });
			if(person && person.length > 0) {
				person = person[0];
				personnel = { userId:person.userId, contractId:contract_id, type:type };
			}
			return personnel;
			
		},
		submit_import:function() {
			var $dialog = $(QUOTES.import.import_dialog);
			UTIL.clear_message_in_popup(QUOTES.import.import_dialog);
			
			var customer_id = $(QUOTES.import.customer_field).val();
			var name = $(QUOTES.import.contract_name_field).val();
			var alt_id = $(QUOTES.import.alt_id_field).val();
			var job_number = $(QUOTES.import.job_number_field).val();
			var engagement_manager = $(QUOTES.import.engagement_manager_field).val();
			var account_exec = $(QUOTES.import.account_exec_field).val();
			var signed_date = $(QUOTES.import.signed_date_field).val();
			var service_start_date = $(QUOTES.import.service_start_date_field).val();
			var start_date = $(QUOTES.import.start_date_field).val();
			var end_date = $(QUOTES.import.end_date_field).val();
			var renewal_status = null;
			var renewal_change = 0;
			
			var server_signed_date = UTIL.convert_dates_for_server(signed_date);
			var server_service_start_date = UTIL.convert_dates_for_server(service_start_date);
			var server_start_date = UTIL.convert_dates_for_server(start_date);
			var server_end_date = UTIL.convert_dates_for_server(end_date);
			
			var ae_name = $(QUOTES.import.contract_ae_field).val();
			var epe_name = $(QUOTES.import.contract_epe_field).val();
			var $bscs = $(QUOTES.import.contract_bscs_field).siblings(QUOTES.import.multiselect_display).find(".multiselect-display");
			var $sdms = $(QUOTES.import.contract_sdms_field).siblings(QUOTES.import.multiselect_display).find(".multiselect-display");
			var bscs = [];
			var sdms = [];
			var ae = null;
			var epe = null;
			
			//validate
			if(!name || !alt_id || !server_start_date || !server_end_date || !server_signed_date || !server_service_start_date) {
				UTIL.add_error_message_to_popup(QUOTES.import.import_dialog, $(QUOTES.import.general_error_msg).val());
				return false;
			}
			
			if(ae_name) {
				ae = QUOTES.import.build_personnel_for_save(ae_name, null, QUOTES.import.personnel_types.ae);
				if(ae == null) {
					UTIL.add_error_message_to_popup(QUOTES.import.import_dialog, "No AE found in the database with the name " + ae_name);
					return false;
				}
			}
			
			if(epe_name) {
				epe = QUOTES.import.build_personnel_for_save(epe_name, null, QUOTES.import.personnel_types.epe);
				if(epe == null) {
					UTIL.add_error_message_to_popup(QUOTES.import.import_dialog, "No EPE found in the database with the name " + ae_name);
					return false;
				}
			}
			
			var bsc_names = "";
			$bscs.each(function() {
				var bsc_name = $(this).data("name");
				var bsc = QUOTES.import.build_personnel_for_save(bsc_name, null, QUOTES.import.personnel_types.bsc);
				if(bsc == null) {
					bsc_names += bsc_name + " ";
				} else {
					bscs.push(bsc);
				}
			});
			
			if(bsc_names) {
				UTIL.add_error_message_to_popup(QUOTES.import.import_dialog, "No BSC found in the database with the name(s) " + bsc_names);
				return false;
			}
			
			var sdm_names = "";
			$sdms.each(function() {
				var sdm_name = $(this).data("name");
				var sdm = QUOTES.import.build_personnel_for_save(sdm_name, null, QUOTES.import.personnel_types.sdm);
				if(sdm == null) {
					sdm_names += sdm_name + " ";
				} else {
					sdms.push(sdm);
				}
			});
			
			if(sdm_names) {
				UTIL.add_error_message_to_popup(QUOTES.import.import_dialog, "No SDM found in the database with the name(s) " + sdm_names);
				return false;
			}
			
			var json = { "customerId":customer_id, "name":name, "altId":alt_id, "signedDate":server_signed_date, "serviceStartDate":server_service_start_date, "startDate":server_start_date, "endDate":server_end_date, "jobNumber":job_number, "engagementManager":engagement_manager, "accountExecutive":account_exec, "archived":"false", quoteId:QUOTES.import.current_quote_id, renewalStatus:renewal_status, renewalChange:renewal_change, accountExecutive:ae, enterpriseProgramExecutive:epe, businessSolutionsConsultants:bscs, serviceDeliveryManagers:sdms };
			
			json = UTIL.remove_null_properties(json);
			
			UTIL.add_dialog_loader(QUOTES.import.import_dialog);
			
			$.ajax ({
				url: PAGE_CONSTANTS.BASE_URL + "pricing/quotes/import/" + QUOTES.import.current_quote_id + ".json",
				data: JSON.stringify(json),
				type: "POST",
				success:function(data) {
					UTIL.remove_dialog_loader(QUOTES.import.import_dialog);
					if(data.status == PAGE_CONSTANTS.OK_STS) {
						QUOTES.import.mark_quote_as_won();
						
						//update the popup
						var success_msg = $(QUOTES.import.import_success_msg).val();
						UTIL.add_success_message_to_popup(QUOTES.import.import_dialog, success_msg);
						$(QUOTES.import.import_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").hide();
						$(QUOTES.import.import_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button:contains('OK')").show();
						
						//in order for this to reload, it's depending on the contracts.js being included -- it won't throw an error if it's not present though
						if(CONTRACTS) CONTRACTS.read.get_contracts(customer_id);
						
					} else if(data.status == PAGE_CONSTANTS.ERRR_STS) {
						UTIL.add_error_message_to_popup(QUOTES.import.import_dialog, data.message);
					}
				}
			});
		}
	}
};

$(document).ready(function() {
    QUOTES.init();
});
