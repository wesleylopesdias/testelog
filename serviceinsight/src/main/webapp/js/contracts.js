var CONTRACTS = {
	add_contract_dialog: "#add-contract-dialog",
	customers: "#customer",
	contracts: "#contracts",
	update_type_field: "#contract-update-type",
	update_type_is_add: true,
	date_format:"MM/DD/YYYY",
	init:function() {
		CONTRACTS.set_variables();
		CONTRACTS.bind_events();
		CONTRACTS.check_for_load();
		CONTRACTS.read.init();
		CONTRACTS.update.init();
	},
	set_variables:function() {
		if($(CONTRACTS.update_type_field).val() == "edit") CONTRACTS.update_type_is_add = false;
	},
	bind_events:function() {
		var popup_title = "Add an SOW";
		if(!CONTRACTS.update_type_is_add) popup_title = "Edit an SOW";
		$(CONTRACTS.add_contract_dialog).dialog({
			  autoOpen:false,
			  dialogClass:"contract-dialog",	
		      resizable:false,
		      width:700,
		      height:670,
		      modal:true,
		      title: popup_title,
		      open:function() {
		    	  $('.datepicker').datepicker('enable');
		    	  CONTRACTS.update.reset_fields();
		    	  $(this).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").show();
		    	  $(this).closest(".ui-dialog").find(".ui-button:contains('OK')").hide();
		      },
		      close:function() {
		    	  $('.datepicker').datepicker('disable');
		      },
		      buttons: {
		        "Save":function() {
		        	CONTRACTS.update.submit_contract();
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
		
		$(CONTRACTS.customers).change(function() {
			var customer_id = $(this).val();
			if(customer_id != "#") {
				CONTRACTS.update.current_tab = "active";
				$(CONTRACTS.read.tab).removeClass("selected");
				$(CONTRACTS.read.tab + ":first-child").addClass("selected");
				CONTRACTS.read.get_contracts(customer_id);
			} else {
				$(CONTRACTS.contracts).hide();
			}
		});
	},
	check_for_load:function() {
		var customer_id = UTIL.get_param_by_name("cid");
		if(customer_id) {
			$(CONTRACTS.customers).val(customer_id);
			CONTRACTS.read.get_contracts(customer_id, CONTRACTS.update.current_tab);
		}
	},
	read: {
		tab:".table-tab",
		nrc_header:"#nrc-header",
		mrc_header:"#mrc-header",
		nrc_total:"#contracts-total-month-nrc",
		mrc_total:"#contracts-total-month-mrc",
		total_label:"#contracts-total-month-label",
		init:function() {
			CONTRACTS.read.bind_events();
			CONTRACTS.read.set_headers();
			CONTRACTS.read.get_personnel();
			
			if(!CONTRACTS.update_type_is_add) {
				CONTRACTS.read.get_contract();
			}
		},
		bind_events:function() {
			$(CONTRACTS.read.tab).click(function() {
				$(CONTRACTS.read.tab).removeClass("selected");
				$(this).addClass("selected");
				
				var type = $(this).data("view");
				var customer_id = $(CONTRACTS.customers).val();
				
				CONTRACTS.update.current_tab = type;
				CONTRACTS.read.get_contracts(customer_id, type);
			});
		},
		set_headers:function() {
			var moment_month = moment().format("MMM 'YY");
			
			$(CONTRACTS.read.nrc_header + ", " + CONTRACTS.read.mrc_header).html("(" + moment_month + ")");
			$(CONTRACTS.read.total_label).html(moment_month + " Totals");
		},
		get_contracts:function(customer_id, type) {
			UTIL.add_table_loader(CONTRACTS.contracts);
			$(CONTRACTS.contracts).find("tfoot").hide();
			
			var archived = "&a=false";
			if(type == "archived") archived = "&a=true";
			
			$.ajax ({
				url: PAGE_CONSTANTS.BASE_URL + "contracts.json?full&cid=" + customer_id + archived,
				type: "GET",
				success:function(data) {
					data = UTIL.prepare_data_for_display(data);
					CONTRACTS.read.build_contracts(data);
				}
			});
			
			if(CONTRACTS.update_type_is_add) {
				CONTRACTS.read.get_customer();
			}
		},
		get_personnel:function() {
			$.ajax ({
				url: PAGE_CONSTANTS.BASE_URL + "data/personnel.json?a=true",
				type: "GET",
				success:function(data) {
					CONTRACTS.update.personnel = data;
					CONTRACTS.update.setup_autocompletes(data);
				}
			});
		},
		get_contract:function() {
			var contract_id = $(CONTRACTS.update.contract_id).val();
			$.ajax ({
				url: PAGE_CONSTANTS.BASE_URL + "contracts/" + contract_id + ".json",
				type: "GET",
				success:function(data) {
					CONTRACTS.read.build_contract_details(data);
					CONTRACTS.update.current_contract = data;
				}
			});
		},
		get_customer:function() {
			var customer_id = $(CONTRACTS.customers).val();
			if(customer_id) {
				$.ajax ({
					url: PAGE_CONSTANTS.BASE_URL + "customers/" + customer_id + ".json",
					type: "GET",
					success:function(data) {
						CONTRACTS.update.customer = data;
					}
				});
			}
		},
		build_contract_details:function(contract) {
			var ae = "", epe = "", sdm = "", bsc = "";
			if(contract.accountExecutive) ae = contract.accountExecutive.userName;
			if(contract.enterpriseProgramExecutive) epe = contract.enterpriseProgramExecutive.userName;
			var sdms = contract.serviceDeliveryManagers;
			var bscs = contract.businessSolutionsConsultants;
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
			
			$(CONTRACTS.update.display_alt_id).html(contract.altId);
			$(CONTRACTS.update.display_job_number).html(contract.jobNumber);
			$(CONTRACTS.update.display_engagement_manager).html(sdm);
			$(CONTRACTS.update.display_account_exec).html(ae);
			$(CONTRACTS.update.display_epe).html(epe);
			$(CONTRACTS.update.display_bsc).html(bsc);
			$(CONTRACTS.update.display_name).html(contract.name);
			$(CONTRACTS.update.display_signed_date).html(UTIL.convert_dates_for_ui(contract.signedDate));
			$(CONTRACTS.update.display_service_start_date).html(UTIL.convert_dates_for_ui(contract.serviceStartDate));
			$(CONTRACTS.update.display_start_date).html(UTIL.convert_dates_for_ui(contract.startDate));
			$(CONTRACTS.update.display_end_date).html(UTIL.convert_dates_for_ui(contract.endDate));
			
			var file_path = "";
			if(contract.filePath) {
				file_path = "<a href=\"javascript:;\" class=\"contract-download-link\"\" id=\"download-contract-link\" data-type=\"contract\">Download SOW PDF<i class=\"fa fa-download icon-right\"></i></a> <span style=\"margin:0 7px;\">|</span><span style=\"font-size:0.85em; font-style: italic;\"><a href=\"javascript:;\" class=\"popup-link contract-delete-link\"\" data-type=\"contract\" data-dialog=\"delete-contract-doc-dialog\"><i class=\"fa fa-minus-circle\"></i>Delete PDF</a></span>";
			} else {
				//file_path = "<a href=\"javascript:;\" class=\"popup-link contract-upload-link\" data-dialog=\"contract-upload-dialog\" id=\"contract-upload-btn\" data-type=\"contract\">Upload a SOW<i class=\"fa fa-upload icon-right\"></i></a>";
				file_path = "";
			}
			
			$(CONTRACTS.update.display_file_path).html(file_path);
			
			$(CONTRACTS.update.display_renewal_status).html(contract.renewalStatusDisplay);
			
			if(contract.renewalStatus) {
				var change = contract.renewalChange;
				var change_display = "";
				if(change < 0) {
					change_display = Math.abs(change) + "% MRC Decrease";
				} else if(change > 0) {
					change_display = change + "% MRC Increase";
				} else {
					change_display = "No change";
				}
				
				$(CONTRACTS.update.display_renewal_change).html(change_display);
				$(CONTRACTS.update.display_renewal_notes).html(contract.renewalNotes);
				$(CONTRACTS.update.renewal_display_section).show();
				$(CONTRACTS.update.renewal_display_msg).hide();
			} else {
				$(CONTRACTS.update.renewal_display_section).hide();
				
				//check to show message
				var now = moment();
				var contract_end_date_moment = moment(UTIL.convert_dates_for_ui(contract.endDate), CONTRACT.date_selectors.moment_format).subtract(12, 'months');
				if(now > contract_end_date_moment && !contract.archived) {
					$(CONTRACTS.update.renewal_display_msg).show();
				}
			}
			
			if(contract.archived) {
				$(CONTRACTS.update.archived_label).show();
			} else {
				$(CONTRACTS.update.archived_label).hide();
			}
		},
		build_contracts:function(contracts) {
			var output = "";
			var url = PAGE_CONSTANTS.BASE_URL + "contracts/";
			var nrc_total = 0;
			var mrc_total = 0;
			
			if(contracts.length > 0) {
				for(var i=0; i<contracts.length; i++) {
					var contract = contracts[i];
					var ae = "", epe = "", sdm = "", bsc = "";
					if(contract.accountExecutive) ae = contract.accountExecutive.userName;
					if(contract.enterpriseProgramExecutive) epe = contract.enterpriseProgramExecutive.userName;
					var sdms = contract.serviceDeliveryManagers;
					var bscs = contract.businessSolutionsConsultants;
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
					output += "<td><a href=\"" + url + contract.id + "\">" + contract.altId + "</a></td>";
					output += "<td>" + contract.name + "</td>";
					output += "<td>" + contract.jobNumber + "</td>";
					output += "<td>" + ae + "</td>";
					output += "<td>" + epe + "</td>";
					output += "<td>" + sdm + "</td>";
					output += "<td>" + bsc + "</td>";
					output += "<td class=\"right\">" + UTIL.convert_dates_for_ui(contract.signedDate) + "</td>";
					output += "<td class=\"right\">" + UTIL.convert_dates_for_ui(contract.startDate) + "</td>";
					output += "<td class=\"right\">" + UTIL.convert_dates_for_ui(contract.serviceStartDate) + "</td>";
					output += "<td class=\"right\">" + UTIL.convert_dates_for_ui(contract.endDate) + "</td>";
					output += "<td class=\"right\">" + UTIL.convert_currency_for_ui(contract.monthTotalOnetimeRevenue) + "</td>";
					output += "<td class=\"right\">" + UTIL.convert_currency_for_ui(contract.monthTotalRecurringRevenue) + "</td>";
				    output += "</tr>";
				    
				    nrc_total += contract.monthTotalOnetimeRevenue;
				    mrc_total += contract.monthTotalRecurringRevenue;
				}
				
				$(CONTRACTS.read.nrc_total).html(UTIL.convert_currency_for_ui(nrc_total));
				$(CONTRACTS.read.mrc_total).html(UTIL.convert_currency_for_ui(mrc_total));
				$(CONTRACTS.contracts).find("tfoot").show();
			} else {
				output = "<tr><td colspan=\"8\" class=\"no-results\">No contracts returned for this customer.</td></tr>";
			}
			
			
			$(CONTRACTS.contracts).find("tbody").html(output);
			$(CONTRACTS.contracts).show();
		}
	},
	update: {
		contract_name_field: "#contract-name",
		alt_id_field: "#contract-alt-id",
		job_number_field: "#contract-job-number",
		engagement_manager_field: "#contract-engagement-manager",
		account_exec_field:"#contract-account-exec",
		contract_sdms_field:"#contract-sdms",
		contract_bscs_field:"#contract-bscs",
		contract_ae_field:"#contract-ae",
		contract_epe_field:"#contract-epe",
		autocomplete_value:"input[name='autocomplete-value']",
		multiselect_display:".value-second-row",
		signed_date_field: "#contract-signed-date",
		service_start_date_field: "#contract-service-start-date",
		start_date_field: "#contract-start-date",
		end_date_field: "#contract-end-date",
		archived_field:"#customer-archived",
		display_alt_id: "#contract-display-alt-id",
		file_path_field: "#file-path-id",
		renewal_status_field:"#contract-renewal-status",
		renewal_change_field:"#contract-renewal-change",
		renewal_notes_field:"#contract-renewal-notes",
		display_name: "#contract-display-name",
		display_job_number: "#contract-display-job-number",
		display_engagement_manager: "#contract-display-engagement-manager",
		display_account_exec: "#contract-display-account-exec",
		display_epe: "#contract-display-epe",
		display_bsc: "#contract-display-bsc",
		display_signed_date: "#contract-display-signed-date",
		display_service_start_date:"#contract-display-service-start-date",
		display_start_date: "#contract-display-start-date",
		display_end_date: "#contract-display-end-date",
		display_file_path:"#contract-display-file-path",
		display_renewal_status:"#contract-display-renewal-status",
		display_renewal_change:"#contract-display-renewal-change",
		display_renewal_notes:"#contract-display-renewal-notes",
		contract_hidden_start_date: "#contract-hidden-start-date",
		contract_hidden_end_date: "#contract-hidden-end-date",
		contract_id: "#contract-id",
		sn_sys_id:"#contract-service-now-sys-id",
		sn_link_container:"#contract-sn-link",
		add_person_btn:".add-person-btn",
		customer:null,
		bscs: [],
		sdms: [],
		personnel: [],
		personnel_types:{ ae:"ae", bsc:"bsc", epe:"epe", sdm:"sdm" },
		renewal_field_section:"#renewal-section",
		renewal_display_section:"#renewal-display-section",
		renewal_display_msg:"#renewal-display-msg",
		contract_update_msg: "#update-contract-msg",
		general_error_msg: "#general-error-msg",
		archived_label:".archived",
		current_tab:"active",
		current_contract:null,
		init:function() {
			CONTRACTS.update.bind_events();
		},
		bind_events:function() {
			$(".dialog-content").on("click", ".multiselect-display a", function() {
				var $parent = $(this).parent();
				CONTRACTS.update.remove_multiselect($parent.data("name"), $parent.data("id"));
				$parent.remove();
			});
		},
		reset_fields:function() {
			var name = "", alt_id = "", signed_date = "", service_start_date = "", start_date = "", end_date = "", job_number = "", engagement_manager = "", account_exec = "", archived = false, renewal_status = "", renewal_change = "", renewal_notes = "";
			var ae_name = "";
			var epe_name = "";
			var bscs = null;
			var sdms = null;
			
			$(CONTRACTS.update.contract_bscs_field).siblings(CONTRACTS.update.multiselect_display).html("");
			$(CONTRACTS.update.contract_sdms_field).siblings(CONTRACTS.update.multiselect_display).html("");
			
			if(!CONTRACTS.update_type_is_add) {
				var contract = CONTRACTS.update.current_contract;
				
	    		//populate fields
				alt_id = $(CONTRACTS.update.display_alt_id).html();
				job_number = $(CONTRACTS.update.display_job_number).html();
				engagement_manager = $(CONTRACTS.update.display_engagement_manager).html();
				account_exec = $(CONTRACTS.update.display_account_exec).html();
				name = $(CONTRACTS.update.display_name).html();
				signed_date = $(CONTRACTS.update.display_signed_date).html();
				service_start_date = $(CONTRACTS.update.display_service_start_date).html();
				start_date = $(CONTRACTS.update.display_start_date).html();
				end_date = $(CONTRACTS.update.display_end_date).html();
				renewal_status = contract.renewalStatus;
				renewal_change = contract.renewalChange;
				renewal_notes = contract.renewalNotes;
				$(CONTRACTS.update.renewal_field_section).show();
				if($(CONTRACTS.update.archived_label).is(":visible")) archived = true;
				
				if(contract.accountExecutive != null) ae_name = contract.accountExecutive.userName;
				if(contract.enterpriseProgramExecutive != null) epe_name = contract.enterpriseProgramExecutive.userName;
				
				bscs = contract.businessSolutionsConsultants;
				sdms = contract.serviceDeliveryManagers;
			} else {
				$(CONTRACTS.update.renewal_field_section).hide();
				var customer = CONTRACTS.update.customer;
				if(customer != null) {
					if(customer.accountExecutive != null) ae_name = customer.accountExecutive.userName;
					if(customer.enterpriseProgramExecutive != null) epe_name = customer.enterpriseProgramExecutive.userName;
					bscs = customer.businessSolutionsConsultants;
					sdms = customer.serviceDeliveryManagers;
				}
			}
			
			if(bscs != null && bscs.length > 0) {
				for(var i = 0; i < bscs.length; i++) {
					var bsc = bscs[i];
					CONTRACTS.update.add_multiselect(CONTRACTS.update.contract_bscs_field, bsc.userName, bsc.userId, bsc.type);
				}
			}
			
			if(sdms != null && sdms.length > 0) {
				for(var i = 0; i < sdms.length; i++) {
					var sdm = sdms[i];
					CONTRACTS.update.add_multiselect(CONTRACTS.update.contract_sdms_field, sdm.userName, sdm.userId, sdm.type);
				}
			}
			
			UTIL.clear_message_in_popup(CONTRACTS.add_contract_dialog);
			$(CONTRACTS.update.contract_name_field).val(name);
			$(CONTRACTS.update.job_number_field).val(job_number);
			$(CONTRACTS.update.engagement_manager_field).val(engagement_manager);
			$(CONTRACTS.update.account_exec_field).val(account_exec);
			$(CONTRACTS.update.contract_ae_field).val(ae_name);
			$(CONTRACTS.update.contract_epe_field).val(epe_name);
			$(CONTRACTS.update.alt_id_field).val(alt_id);
			$(CONTRACTS.update.signed_date_field).val(signed_date);
			$(CONTRACTS.update.service_start_date_field).val(service_start_date);
			$(CONTRACTS.update.start_date_field).val(start_date);
			$(CONTRACTS.update.end_date_field).val(end_date);
			$(CONTRACTS.update.renewal_status_field).val(renewal_status);
			$(CONTRACTS.update.renewal_change_field).val(renewal_change);
			$(CONTRACTS.update.renewal_notes_field).val(renewal_notes);
			$(CONTRACTS.update.archived_field).prop("checked", archived);
		},
		setup_autocompletes:function(personnel) {
			var bscs =[];
			var sdms = [];
			var aes = [];
			var epes = [];
			
			if(personnel != null && personnel.length > 0) {
				for(var i = 0; i < personnel.length; i++) {
					var person = personnel[i];
					var type = person.type;
					if(type == CONTRACTS.update.personnel_types.ae) {
						aes.push({id:person.userId, value:person.userName, type:CONTRACTS.update.personnel_types.ae});
					} else if(type == CONTRACTS.update.personnel_types.bsc) {
						bscs.push({id:person.userId, value:person.userName, type: CONTRACTS.update.personnel_types.bsc});
					} else if(type == CONTRACTS.update.personnel_types.sdm) {
						sdms.push({id:person.userId, value:person.userName, type:CONTRACTS.update.personnel_types.sdm});
					} else if(type == CONTRACTS.update.personnel_types.epe) {
						epes.push({id:person.userId, value:person.userName, type:CONTRACTS.update.personnel_types.epe});
					}
				}
			}
			
			CONTRACTS.update.setup_autocomplete(CONTRACTS.update.contract_ae_field, aes);
			CONTRACTS.update.setup_autocomplete(CONTRACTS.update.contract_epe_field, epes);
			CONTRACTS.update.setup_multi_autocomplete(CONTRACTS.update.contract_sdms_field, sdms);
			CONTRACTS.update.setup_multi_autocomplete(CONTRACTS.update.contract_bscs_field, bscs);
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
			        $(field).siblings(CONTRACTS.update.autocomplete_value).val(ui.item.value);
			        return false;
			      }
			 });
			
			$(field).change(function() {
				var val = $(this).val();
				var results = $.grep(source, function(n) { return n.label == val; });
				if(results.length == 0) $(field).siblings(CONTRACTS.update.autocomplete_value).val("");
			});
		},
		add_multiselect:function(field, label, value, type) {
			if(value != "") {
				var output = "<span class=\"multiselect-display\" data-id=\"" + value + "\" data-name=\"" +label + "\" data-type=\"" + type + "\">";
				output += label;
				output += "<a href=\"javascript:;\"><i class=\"fa fa-times\"></i></a>";
				output += "</span>";
				$(field).siblings(CONTRACTS.update.multiselect_display).append(output);
				
				//add to array
				var record = { id:value, name:label };
				//QUOTES.create.presales_reps.push(record);
				
				//clear field
				$(field).val("");
			}
		},
		remove_multiselect:function(name, id, type) {
			if(type == CONTRACTS.update.personnel_types.bsc) {
				//remove by id, otherwise, remove by the name itself if the id doesn't exist
				if(id) {
					CONTRACTS.update.bscs = $.grep(CONTRACTS.update.bscs, function(n) { return n.userId != id; });
				} else {
					CONTRACTS.update.bscs = $.grep(CONTRACTS.update.bscs, function(n) { return n.name != name; });
				}
			} else if(type == CONTRACTS.update.personnel_types.sdm) {
				//remove by id, otherwise, remove by the name itself if the id doesn't exist
				if(id) {
					CONTRACTS.update.sdms = $.grep(CONTRACTS.update.sdms, function(n) { return n.userId != id; });
				} else {
					CONTRACTS.update.sdms = $.grep(CONTRACTS.update.sdms, function(n) { return n.name != name; });
				}
			}
		},
		build_personnel_for_save:function(user_name, contract_id, type) {
			var personnel = null;
			var person = $.grep(CONTRACTS.update.personnel, function(n) { return n.userName == user_name; });
			if(person && person.length > 0) {
				person = person[0];
				personnel = { userId:person.userId, contractId:contract_id, type:type };
			}
			return personnel;
			
		},
		submit_contract:function() {
			UTIL.clear_message_in_popup(CONTRACTS.add_contract_dialog);
			
			var contract_id = $(CONTRACTS.update.contract_id).val();
			var customer_id = $(CONTRACTS.customers).val();
			var name = $(CONTRACTS.update.contract_name_field).val();
			var alt_id = $(CONTRACTS.update.alt_id_field).val();
			var job_number = $(CONTRACTS.update.job_number_field).val();
			var engagement_manager = $(CONTRACTS.update.engagement_manager_field).val();
			var account_exec = $(CONTRACTS.update.account_exec_field).val();
			var signed_date = $(CONTRACTS.update.signed_date_field).val();
			var service_start_date = $(CONTRACTS.update.service_start_date_field).val();
			var start_date = $(CONTRACTS.update.start_date_field).val();
			var end_date = $(CONTRACTS.update.end_date_field).val();
			var archived = $(CONTRACTS.update.archived_field).prop("checked");
			var existing_job_number = $(CONTRACTS.update.display_job_number).html();
			var sn_sys_id = $(CONTRACTS.update.sn_sys_id).val();
			var renewal_status = $(CONTRACTS.update.renewal_status_field).val();
			var renewal_change = $(CONTRACTS.update.renewal_change_field).val();
			var renewal_notes = $(CONTRACTS.update.renewal_notes_field).val();
			archived = archived.toString();
			var ajax_type = "POST";
			
			var server_signed_date = UTIL.convert_dates_for_server(signed_date);
			var server_service_start_date = UTIL.convert_dates_for_server(service_start_date);
			var server_start_date = UTIL.convert_dates_for_server(start_date);
			var server_end_date = UTIL.convert_dates_for_server(end_date);
			
			var ae_name = $(CONTRACTS.update.contract_ae_field).val();
			var epe_name = $(CONTRACTS.update.contract_epe_field).val();
			var $bscs = $(CONTRACTS.update.contract_bscs_field).siblings(CONTRACTS.update.multiselect_display).find(".multiselect-display");
			var $sdms = $(CONTRACTS.update.contract_sdms_field).siblings(CONTRACTS.update.multiselect_display).find(".multiselect-display");
			var bscs = [];
			var sdms = [];
			var ae = null;
			var epe = null;
			
			//validate
			if(!name || !alt_id || !server_start_date || !server_end_date || !server_signed_date || !server_service_start_date) {
				UTIL.add_error_message_to_popup(CONTRACTS.add_contract_dialog, $(CONTRACTS.update.general_error_msg).val());
				return false;
			}
			
			//clear sn sys id if job number changes
			if(existing_job_number != job_number || !sn_sys_id) {
				sn_sys_id = null;
			}
			
			if(!renewal_status) renewal_status = null;
			if(!renewal_change) renewal_change = "0";
			
			if(ae_name) {
				ae = CONTRACTS.update.build_personnel_for_save(ae_name, contract_id, CONTRACTS.update.personnel_types.ae);
				if(ae == null) {
					UTIL.add_error_message_to_popup(CONTRACTS.add_contract_dialog, "No AE found in the database with the name " + ae_name);
					return false;
				}
			}
			
			if(epe_name) {
				epe = CONTRACTS.update.build_personnel_for_save(epe_name, contract_id, CONTRACTS.update.personnel_types.epe);
				if(epe == null) {
					UTIL.add_error_message_to_popup(CONTRACTS.add_contract_dialog, "No EPE found in the database with the name " + ae_name);
					return false;
				}
			}
			
			var bsc_names = "";
			$bscs.each(function() {
				var bsc_name = $(this).data("name");
				var bsc = CONTRACTS.update.build_personnel_for_save(bsc_name, contract_id, CONTRACTS.update.personnel_types.bsc);
				if(bsc == null) {
					bsc_names += bsc_name + " ";
				} else {
					bscs.push(bsc);
				}
			});
			
			if(bsc_names) {
				UTIL.add_error_message_to_popup(CONTRACTS.add_contract_dialog, "No BSC found in the database with the name(s) " + bsc_names);
				return false;
			}
			
			var sdm_names = "";
			$sdms.each(function() {
				var sdm_name = $(this).data("name");
				var sdm = CONTRACTS.update.build_personnel_for_save(sdm_name, contract_id, CONTRACTS.update.personnel_types.sdm);
				if(sdm == null) {
					sdm_names += sdm_name + " ";
				} else {
					sdms.push(sdm);
				}
			});
			
			if(sdm_names) {
				UTIL.add_error_message_to_popup(CONTRACTS.add_contract_dialog, "No SDM found in the database with the name(s) " + sdm_names);
				return false;
			}
			
			var json = { "customerId":customer_id, "name":name, "altId":alt_id, "signedDate":server_signed_date, "serviceStartDate":server_service_start_date, "startDate":server_start_date, "endDate":server_end_date, "jobNumber":job_number, "engagementManager":engagement_manager, "accountExecutive":account_exec, "archived":archived, "serviceNowSysId":sn_sys_id, renewalStatus:renewal_status, renewalChange:renewal_change, renewalNotes:renewal_notes, accountExecutive:ae, enterpriseProgramExecutive:epe, businessSolutionsConsultants:bscs, serviceDeliveryManagers:sdms };
			
			if(!CONTRACTS.update_type_is_add) {
				ajax_type = "PUT";
				json["id"] = contract_id;
			}
			
			json = UTIL.remove_null_properties(json);
			
			UTIL.add_dialog_loader(CONTRACTS.add_contract_dialog);
			
			$.ajax ({
				url: PAGE_CONSTANTS.BASE_URL + "contracts.json",
				data: JSON.stringify(json),
				type: ajax_type,
				success:function(data) {
					UTIL.remove_dialog_loader(CONTRACTS.add_contract_dialog);
					if(data.status == PAGE_CONSTANTS.OK_STS) {
						
						//update the popup
						var success_msg = $(CONTRACTS.update.contract_update_msg).val();
						UTIL.add_success_message_to_popup(CONTRACTS.add_contract_dialog, success_msg);
						$(CONTRACTS.add_contract_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").hide();
						$(CONTRACTS.add_contract_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button:contains('OK')").show();
						
						if(!CONTRACTS.update_type_is_add) {
							/*
							$(CONTRACTS.update.display_alt_id).html(alt_id);
							$(CONTRACTS.update.display_name).html(name);
							$(CONTRACTS.update.display_job_number).html(job_number);
							$(CONTRACTS.update.display_engagement_manager).html(engagement_manager);
							$(CONTRACTS.update.display_account_exec).html(account_exec);
							$(CONTRACTS.update.display_signed_date).html(signed_date);
							$(CONTRACTS.update.display_service_start_date).html(service_start_date);
							$(CONTRACTS.update.display_start_date).html(start_date);
							$(CONTRACTS.update.display_end_date).html(end_date);
							*/
							
							CONTRACTS.read.get_contract();
							
							$(CONTRACTS.update.contract_hidden_start_date).val(start_date);
							$(CONTRACTS.update.contract_hidden_end_date).val(end_date);
							
							if(existing_job_number != job_number) {
								$(CONTRACTS.update.sn_link_container).hide();
							}
                            if (CONTRACT) {
                                CONTRACT.contract_services.get_contract_services(contract_id);
                            } else {
                                console.log('CONTRACT is empty!');
                            }
						} else {
							var customer_id = $(CONTRACTS.customers).val();
							CONTRACTS.read.get_contracts(customer_id, CONTRACTS.update.current_tab);
						}
					} else if(data.status == PAGE_CONSTANTS.ERRR_STS) {
						UTIL.add_error_message_to_popup(CONTRACTS.add_contract_dialog, data.message);
					}
					//refresh list or redirect to edit page
				}
			});
		}
	}
};

$(document).ready(function() {
    CONTRACTS.init();
});