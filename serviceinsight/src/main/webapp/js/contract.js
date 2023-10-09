var CONTRACT = {
	contract_id: null,
	sn_sys_id: null,
	contract_id_field: "#contract-id",
	sn_sys_id_field:"#contract-service-now-sys-id",
	add_contract_service_dialog:"#add-contract-service-dialog",
	edit_contract_service_dialog:"#edit-contract-service-dialog",
	pcr_dialog:"#pcr-dialog",
	upload_dialog:"#upload-dialog",
	azure_dialog:"#azure-dialog",
	manage_groups_dialog:"#manage-groups-dialog",
	add_contract_adjustment_dialog:"#add-contract-adjustment-dialog",
	edit_contract_adjustment_dialog:"#edit-contract-adjustment-dialog",
	contract_start_date_validation_msg: "#contract-service-start-date-msg",
	contract_end_before_start_date_validation_msg: "#contract-service-end-before-start-msg",
	general_validation_error:"#general-error-msg",
	init:function() {
		CONTRACT.set_variables();
		CONTRACT.bind_events();
		CONTRACT.date_selectors.init();
		CONTRACT.contract_services.init();
		CONTRACT.pcr.init();
		CONTRACT.file_upload.init();
		CONTRACT.contract_adjustment.init();
		CONTRACT.contract_groups.init();
		CONTRACT.search.init();
		CONTRACT.contract_invoice.init();
		CONTRACT.sn_ci.init();
		CONTRACT.azure.init();
		CONTRACT.sow_upload.init();
	},
	set_variables:function() {
		CONTRACT.contract_id = $(CONTRACT.contract_id_field).val();
		CONTRACT.sn_sys_id = $(CONTRACT.sn_sys_id_field).val();
	},
	bind_events:function() {
		$(CONTRACT.add_contract_service_dialog).dialog({
			  autoOpen:false,
			  dialogClass:"contract-dialog",	
		      resizable:false,
		      width:1000,
		      height:700,
		      modal:true,
		      title: "Add a Service",
		      open:function() {
		    	  $('.datepicker').datepicker('enable');
		    	  CONTRACT.contract_services.add.reset_popup();
		    	  $(this).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").show();
		    	  $(this).closest(".ui-dialog").find(".ui-button:contains('Add Another')").hide();
		    	  $(this).closest(".ui-dialog").find(".ui-button:contains('OK')").hide();
		      },
		      close:function() {
		    	  $('.datepicker').datepicker('disable');
		      },
		      buttons: {
		        "Save":function() {
		        	CONTRACT.contract_services.add.submit_services();
		        },
		        "Cancel":function() {
		            $(this).dialog("close");
		        },
		        "Add Another":function() {
		            CONTRACT.contract_services.add.reset_popup();
		        },
		        "OK":function() {
		            $(this).dialog("close");
		        }
		      },
		      create:function () {
		    	  $(this).closest(".ui-dialog").find(".ui-button:contains('Cancel')").addClass("cancel");
		    	  $(this).closest(".ui-dialog").find(".ui-button:contains('OK')").hide();
		    	  $(this).closest(".ui-dialog").find(".ui-button:contains('Add Another')").hide();
		      }
		});
		
		$(CONTRACT.edit_contract_service_dialog).dialog({
			  autoOpen:false,
			  dialogClass:"contract-dialog",	
		      resizable:false,
		      width:1100,
		      height:700,
		      modal:true,
		      title: "Edit a Service",
		      open:function() {
		    	  $('.datepicker').datepicker('enable');
		    	  CONTRACT.contract_services.edit.load_dialog();
		    	  $(this).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").show();
		    	  $(this).closest(".ui-dialog").find(".ui-button:contains('OK')").hide();
		      },
		      close:function() {
		    	  $('.datepicker').datepicker('disable');
		      },
		      buttons: {
		        "Save":function() {
		        	CONTRACT.contract_services.edit.submit_services();
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
		
		$(CONTRACT.pcr_dialog).dialog({
			  autoOpen:false,
			  dialogClass:"pcr-dialog",	
		      resizable:false,
		      width:720,
		      height:530,
		      modal:true,
		      title: "Add a PCR",
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
		        	CONTRACT.pcr.submit_pcr();
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
		
		$(CONTRACT.upload_dialog).dialog({
			  autoOpen:false,
			  dialogClass:"upload-dialog",	
		      resizable:false,
		      width:720,
		      height:400,
		      modal:true,
		      title: "Upload a Spreadsheet",
		      open:function() {
		    	  $('.datepicker').datepicker('enable');
		    	  CONTRACT.file_upload.reset_popup();
		      },
		      close:function() {
		    	  $('.datepicker').datepicker('disable');
		      },
		      buttons: {
		        "Upload":function() {
		        	CONTRACT.file_upload.submit_file();
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
		
		$(CONTRACT.add_contract_adjustment_dialog).dialog({
			  autoOpen:false,
			  dialogClass:"upload-dialog",	
		      resizable:false,
		      width:720,
		      height:680,
		      modal:true,
		      title: "Add Credit/Debit",
		      open:function() {
		    	  $('.datepicker').datepicker('enable');
		    	  CONTRACT.contract_adjustment.add.reset_popup();
		      },
		      close:function() {
		    	  $('.datepicker').datepicker('disable');
		      },
		      buttons: {
		        "Save":function() {
		        	CONTRACT.contract_adjustment.add.submit_contract_adjustment();
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
		
		$(CONTRACT.edit_contract_adjustment_dialog).dialog({
			  autoOpen:false,
			  dialogClass:"upload-dialog",	
		      resizable:false,
		      width:760,
		      height:660,
		      modal:true,
		      title: "Edit Credit/Debit",
		      open:function() {
		    	  $('.datepicker').datepicker('enable');
		    	  CONTRACT.contract_adjustment.edit.reset_popup();
		      },
		      close:function() {
		    	  $('.datepicker').datepicker('disable');
		      },
		      buttons: {
		        "Save":function() {
		        	CONTRACT.contract_adjustment.edit.submit_contract_adjustment();
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
		
		$(CONTRACT.manage_groups_dialog).dialog({
			  autoOpen:false,
			  dialogClass:"manage-groups-dialog",	
		      resizable:false,
		      width:720,
		      height:530,
		      modal:true,
		      title: "Manage Groups",
		      open:function() {
		    	  $('.datepicker').datepicker('enable');
		    	  $(this).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").show();
		    	  $(this).closest(".ui-dialog").find(".ui-button:contains('OK')").hide();
		    	  CONTRACT.contract_groups.reset_popup();
		      },
		      close:function() {
		    	  $('.datepicker').datepicker('disable');
		      },
		      buttons: {
		        "Save":function() {
		        	CONTRACT.contract_groups.submit_form();
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
		
		$(CONTRACT.azure_dialog).dialog({
			  autoOpen:false,
			  dialogClass:"contract-dialog",	
		      resizable:false,
		      width:850,
		      height:600,
		      modal:true,
		      title: "Add a Subscription Service",
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
		        	CONTRACT.azure.submit_items();
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
	sow_upload:{
		upload_dialog:"#contract-upload-dialog",
		delete_dialog:"#delete-contract-doc-dialog",
		form:"#contract-upload-form",
		form_target:"#contract-upload-target",
		file_field: "#contract-file",
		file_select_error_msg: "#file-select-file-error-msg",
		file_type_invalid_error_msg: "#file-type-invalid-error-msg",
		iframe_status:"#contract-iframe-status",
		iframe_message:"#contract-iframe-message",
		file_upload_link:".contract-upload-link",
		file_download_link:".contract-download-link",
		file_delete_link:".contract-delete-link",
		current_type:null,
		contract_update_id:null,
		types:{ contract:"contract", contract_update:"contractupdate" },
		file_type_invalid_error_msg:"#contract-file-type-invalid-error-msg",
		init:function() {
			CONTRACT.sow_upload.bind_events();
		},
		bind_events:function() {
			$(CONTRACT.sow_upload.upload_dialog).dialog({
				  autoOpen:false,
				  dialogClass:"upload-dialog",	
			      resizable:false,
			      width:720,
			      height:400,
			      modal:true,
			      title: "Upload a SOW",
			      open:function() {
			    	  $('.datepicker').datepicker('enable');
			    	  CONTRACT.sow_upload.reset_popup();
			      },
			      close:function() {
			    	  $('.datepicker').datepicker('disable');
			      },
			      buttons: {
			        "Upload":function() {
			        	CONTRACT.sow_upload.submit_file();
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
			
			$(CONTRACT.sow_upload.delete_dialog).dialog({
				  autoOpen:false,
				  dialogClass:"upload-dialog",	
			      resizable:false,
			      width:720,
			      height:400,
			      modal:true,
			      title: "Upload a SOW",
			      open:function() {
			    	  $('.datepicker').datepicker('enable');
			    	  CONTRACT.sow_upload.reset_delete_popup();
			      },
			      close:function() {
			    	  $('.datepicker').datepicker('disable');
			      },
			      buttons: {
			        "Delete":function() {
			        	CONTRACT.sow_upload.delete_file();
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
			
			$(CONTRACT.sow_upload.form_target).load(function(){ 
				var iframe = $(CONTRACT.sow_upload.form_target);
				var status = $(CONTRACT.sow_upload.iframe_status, iframe.contents()).html();
				var message = $(CONTRACT.sow_upload.iframe_message, iframe.contents()).html();
				CONTRACT.sow_upload.complete_submit(status, message);
			});
			
			$(document).on("click", CONTRACT.sow_upload.file_download_link, function() {
				var type = $(this).data("type");
				console.log('type: ' + type);
				CONTRACT.sow_upload.current_type = type;
				if(type == CONTRACT.sow_upload.types.contract_update) {
					CONTRACT.sow_upload.contract_update_id = $(this).data("id");
				} else {
					CONTRACT.sow_upload.contract_update_id = null;
				}
				
				CONTRACT.sow_upload.download_file();
			});
			
			$(document).on("click", CONTRACT.sow_upload.file_upload_link, function() {
				var type = $(this).data("type");
				console.log('type: ' + type);
				CONTRACT.sow_upload.current_type = type;
				if(type == CONTRACT.sow_upload.types.contract_update) {
					CONTRACT.sow_upload.contract_update_id = $(this).data("id");
				} else {
					CONTRACT.sow_upload.contract_update_id = null;
				}
			});
			
			$(document).on("click", CONTRACT.sow_upload.file_delete_link, function() {
				var type = $(this).data("type");
				console.log('type: ' + type);
				CONTRACT.sow_upload.current_type = type;
				if(type == CONTRACT.sow_upload.types.contract_update) {
					CONTRACT.sow_upload.contract_update_id = $(this).data("id");
				} else {
					CONTRACT.sow_upload.contract_update_id = null;
				}
			});
		},
		reset_popup:function() {
			$(CONTRACT.sow_upload.upload_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").show();
	    	$(CONTRACT.sow_upload.upload_dialog).closest(".ui-dialog").find(".ui-button:contains('OK')").hide();
	    	UTIL.clear_message_in_popup(CONTRACT.sow_upload.upload_dialog);
	    	$(CONTRACT.sow_upload.file_field).val("");
		},
		download_file:function() {
			var url = PAGE_CONSTANTS.BASE_URL + "contracts/docs/" + CONTRACT.contract_id + "/download.json";
			var fileName = $(CONTRACTS.update.display_alt_id).html();
			if(CONTRACT.sow_upload.current_type == CONTRACT.sow_upload.types.contract_update) {
				url = PAGE_CONSTANTS.BASE_URL + "contractupdates/docs/" + CONTRACT.sow_upload.contract_update_id + "/download.json"
				fileName = $("#pcr-" + CONTRACT.sow_upload.contract_update_id + " td:first-child a").html();
			}
			
			var req = new XMLHttpRequest();
		     req.open("GET", url, true);
		     req.responseType = "blob";
		     req.onload = function (event) {
		         var blob = req.response;
		         var link=document.createElement("a");
		         link.href=window.URL.createObjectURL(blob);
		         link.download=fileName;
		         link.click();
		     };

		     req.send();
		},
		reset_delete_popup:function() {
			UTIL.clear_message_in_popup(CONTRACT.sow_upload.delete_dialog);
			$(CONTRACT.sow_upload.delete_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").show();
	    	$(CONTRACT.sow_upload.delete_dialog).closest(".ui-dialog").find(".ui-button:contains('OK')").hide();
		},
		delete_file:function() {
			UTIL.clear_message_in_popup(CONTRACT.sow_upload.delete_dialog);
			UTIL.add_dialog_loader(CONTRACT.sow_upload.delete_dialog);
			
			var url = PAGE_CONSTANTS.BASE_URL + "contracts/docs/" + CONTRACT.contract_id + ".json";
			if(CONTRACT.sow_upload.current_type == CONTRACT.sow_upload.types.contract_update) {
				url = PAGE_CONSTANTS.BASE_URL + "contractupdates/docs/" + CONTRACT.sow_upload.contract_update_id + ".json";
			}
			
			$.ajax ({
				url: url,
				type: "DELETE",
				success:function(data) {
					UTIL.remove_dialog_loader(CONTRACT.sow_upload.delete_dialog);
					
					if(data.status == PAGE_CONSTANTS.OK_STS) {
						UTIL.add_success_message_to_popup(CONTRACT.sow_upload.delete_dialog, "File successfully deleted");
						$(CONTRACT.sow_upload.delete_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").hide();
						$(CONTRACT.sow_upload.delete_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button:contains('OK')").show();
						if(CONTRACT.sow_upload.current_type == CONTRACT.sow_upload.types.contract_update) {
							CONTRACT.pcr.get_pcrs();
						} else {
							CONTRACTS.read.get_contract();
						}
					} else {
						UTIL.add_error_message_to_popup(CONTRACT.sow_upload.delete_dialog, data.message);
					}
				}
			});
		},
		submit_file:function() {
			UTIL.clear_message_in_popup(CONTRACT.sow_upload.upload_dialog);
			
			var file = $(CONTRACT.sow_upload.file_field).val();
			
			if(file == "") {
				var message = $(CONTRACT.file_upload.file_select_error_msg).val();
				UTIL.add_error_message_to_popup(CONTRACT.sow_upload.upload_dialog, message);
				return false;
			}
			
			if(!(/\.(pdf)$/i).test(file)) {
				var message = $(CONTRACT.sow_upload.file_type_invalid_error_msg).val();
				UTIL.add_error_message_to_popup(CONTRACT.sow_upload.upload_dialog, message);
				return false;
			}
			
			UTIL.add_dialog_loader(CONTRACT.sow_upload.upload_dialog);
			var url = PAGE_CONSTANTS.BASE_URL + "contracts/docs/" + CONTRACT.contract_id + "/upload";
			
			if(CONTRACT.sow_upload.current_type == CONTRACT.sow_upload.types.contract_update) {
				url = PAGE_CONSTANTS.BASE_URL + "contractupdates/docs/" + CONTRACT.sow_upload.contract_update_id + "/upload";
			}
			console.log("Action: " + url);
			$(CONTRACT.sow_upload.form).prop("action", url);
			$(CONTRACT.sow_upload.form).submit();
		},
		complete_submit:function(status, message) {
			UTIL.remove_dialog_loader(CONTRACT.sow_upload.upload_dialog);
			if(status == "success") {
				UTIL.add_success_message_to_popup(CONTRACT.sow_upload.upload_dialog, message);
				$(CONTRACT.sow_upload.upload_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").hide();
		    	$(CONTRACT.sow_upload.upload_dialog).closest(".ui-dialog").find(".ui-button:contains('OK')").show();
		    	if(CONTRACT.sow_upload.current_type == CONTRACT.sow_upload.types.contract_update) {
					CONTRACT.pcr.get_pcrs();
				} else {
					CONTRACTS.read.get_contract();
				}
			} else {
				UTIL.add_error_message_to_popup(CONTRACT.sow_upload.upload_dialog, message);
			}
		}
	},
	search:{
		ci_search:"#ci-search",
		no_results:"#no-search-results",
		no_results_msg:"#search-no-results-msg",
		init:function() {
			CONTRACT.search.bind_events();
		},
		bind_events:function() {
			$(CONTRACT.search.ci_search).autocomplete({
				source: function(request, response) {
	                $.ajax({
	                    url: PAGE_CONSTANTS.BASE_URL + "contractservices/search.json",
	                    data: {
	                        name: request.term,
	                        cid: CONTRACT.contract_id
	                    },
	                    dataType: "json",
	                    type: "GET",
	                    success: function(data) {
	                    	var options = [];
	                    	if(data.length > 0) {
	                    		$(CONTRACT.search.no_results).html("");
		                    	for(var i=0; i<data.length; i++) {
		                    		var obj = data[i];
		                    		var start_date = obj.startDate;
		                    		var end_date = obj.endDate;
		                    		start_date = UTIL.convert_dates_for_ui(start_date);
		                    		end_date = UTIL.convert_dates_for_ui(end_date);
		                    		options.push({ id:obj.id, value:obj.deviceName, desc:obj.deviceDescription, sid:obj.serviceId, sd:start_date, ed:end_date, did:obj.deviceId });
		                    	}
	                    	} else {
	                    		$(CONTRACT.search.no_results).html($(CONTRACT.search.no_results_msg).val());
	                    	}
	                    	response(options);
	                    }/*,
	                    error: function() {
	                        // added an error handler for the sake of the example
	                        response($.ui.autocomplete.filter(
	                            ["opt1","opt2"]
	                            , extractLast(request.term)));
	                    }*/
	                });
	            },
				minLength: 2,
				select: function(event, ui) {
					var selected = ui.item;
					CONTRACT.search.load_service(selected.id, selected.sid, selected.did, selected.sd, selected.ed);
					$(this).val("");
					return false;
		        }
		    }).data("uiAutocomplete")._renderItem = function( ul, item ) {
				return $("<li>").append("<a>" + item.value + "<span>" + item.desc + "</span></a>").appendTo(ul);
			};
		},
		load_service:function(id, service_id, device_id, start_date, end_date) {
			CONTRACT.contract_services.edit.current_service_id = service_id;
			CONTRACT.contract_services.edit.current_device_id = device_id;
			CONTRACT.contract_services.edit.current_start_date = start_date;
			CONTRACT.contract_services.edit.current_end_date = end_date;
			CONTRACT.contract_services.edit.search_id = id;
			
			//open popup
			UTIL.open_dialog(CONTRACT.edit_contract_service_dialog);
		},
		highligh_row:function() {
			var $input = $(CONTRACT.contract_services.edit.contract_service_table).find(".row-count").find("input[value='" + CONTRACT.contract_services.edit.search_id + "']");
			var $row = $input.parents(".edit-row");
			$row.find("td").css("background-color","#fcfbd1");
			$row.next().find("td").css("background-color","#fcfbd1");
			$row.next().next().find("td").css("background-color","#fcfbd1");
			$row.next().next().find(".required-ind").css("border-color","#fcfbd1");
			$row.next().next().find("select").focus();
			$row.find("input[type=text]:first").focus();
			CONTRACT.contract_services.edit.search_id = null;
		}
	},
	azure:{
		azure_container:"#contract-services-azure",
		azure_table:"#azure-table",
		azure_device_field:"#azure-device",
		azure_service_field:"#azure-service",
		azure_customer_id_field:"#azure-customer-id",
		azure_subcription_id_field:"#azure-subscription-id",
		azure_start_date_field:"#azure-start-date",
		azure_end_date_field:"#azure-end-date",
		azure_ci_name_field:"#azure-ci-name",
		azure_customer_type_field:"#azure-customer-type",
		azure_m365_type_field:"#azure-m365-type",
		azure_m365_support_type_field:"#azure-m365-support-type",
		azure_m365_support_amount_field:"#azure-m365-support-amount",
		azure_m365_active_field:"#azure-m365-active",
		azure_services_container:"#contract-services-azure",
		azure_popup_link:".azure-popup-link",
		azure_delete_link:".azure-delete-popup-link",
		azure_lineitems_link:".azure-lineitems",
		azure_lineitem_container:".azure-lineitems-container",
		msg_saved_successfully:"#save-contract-service-azure-success-msg",
		azure_row_prefix:"#azure-row-",
		azure_field_class:".azure-field",
		aws_field_class:".aws-field",
		m365_field_class:".m365-field",
		m365_type_class:".azure-m365-type-m365",
		m365_support_type_flat_class:".azure-m365-support-flat",
		m365_support_type_percent_class:".azure-m365-support-percent",
		m365_table:"#m365-table",
		subscription_types:{ azure:"cspazure", azureplan:"cspazureplan", aws:"aws", M365Subscription:"M365Subscription" },
		azure_services:null,
		m365_services:null,
		services_interval:null,
		current_id:null,
		current_type:null,
		init:function() {
			CONTRACT.azure.bind_events();
			CONTRACT.azure.get_azure_services();
			CONTRACT.azure.delete_item.init();
		},
		bind_events:function() {
			$(CONTRACT.azure.azure_device_field).change(function() {
				var default_osp_id = $(this).find("option:selected").data("default-osp-id");
				if(!default_osp_id) default_osp_id = "";
				var id = $(CONTRACT.azure.azure_service_field).find(".osp-id-" + default_osp_id).val();
				$(CONTRACT.azure.azure_service_field).val(id);
				if($(CONTRACT.azure.azure_service_field).val()) {
					$(CONTRACT.azure.azure_service_field).prop("disabled", true);
				} else {
					$(CONTRACT.azure.azure_service_field).prop("disabled", false);
				}
				
				var device_type = $(this).find("option:selected").data("device-type");
				if(device_type == CONTRACT.azure.subscription_types.aws) {
					$(CONTRACT.azure.m365_field_class).hide();
					$(CONTRACT.azure.azure_field_class).hide();
					$(CONTRACT.azure.aws_field_class).show();
				} else if(device_type == CONTRACT.azure.subscription_types.azure || device_type == CONTRACT.azure.subscription_types.azureplan) {
					$(CONTRACT.azure.aws_field_class).hide();
					$(CONTRACT.azure.m365_field_class).hide();
					$(CONTRACT.azure.azure_field_class).show();
				} else if(device_type == CONTRACT.azure.subscription_types.M365Subscription) {
					$(CONTRACT.azure.aws_field_class).hide();
					$(CONTRACT.azure.azure_field_class).hide();
					$(CONTRACT.azure.m365_field_class).show();
				}
			});
			
			$(CONTRACT.azure.azure_m365_type_field).change(function() {
				var type = $(this).val();
				if(type == "M365") {
					$(CONTRACT.azure.m365_type_class).show();
				} else {
					$(CONTRACT.azure.m365_type_class).hide();
				}
			});
			
			$(CONTRACT.azure.azure_m365_support_type_field).change(function() {
				var type = $(this).val();
				if(type == "flat") {
					$(CONTRACT.azure.m365_support_type_flat_class).show();
					$(CONTRACT.azure.m365_support_type_percent_class).hide();
				} else {
					$(CONTRACT.azure.m365_support_type_flat_class).hide();
					$(CONTRACT.azure.m365_support_type_percent_class).show();
				}
			});
			
			$(document).on("click", CONTRACT.azure.azure_popup_link, function() {
				var id = $(this).data("id");
				var type = $(this).data("type");
				(id == null) ? CONTRACT.azure.current_id = null : CONTRACT.azure.current_id = id;
				(type == null) ? CONTRACT.azure.current_type = null : CONTRACT.azure.current_type = type;
				CONTRACT.azure.reset_popup();
			});
			
			$(document).on("click", CONTRACT.azure.azure_lineitems_link, function() {
				var id = $(this).data("azure-id");
				var start_date = $(this).data("start-date");
				var end_date = $(this).data("end-date");
				var type = $(this).data("type");
				
				if(type == "cspazure") {
					CONTRACT.azure.get_azure_lineitems(id, start_date, end_date);
				} else {
					CONTRACT.azure.get_aws_lineitems(id, start_date, end_date);
				}
			});
		},
		reset_popup:function() {
			UTIL.clear_message_in_popup(CONTRACT.azure_dialog);
			$(CONTRACT.azure.azure_service_field).prop("disabled", false);
			
			var device_id = "";
			var service_id = "";
			var azure_customer_id = "";
			var subscription_id = "";
			var start_date = "";
			var end_date = "";
			var name = "";
			var customer_type = "";
			var m365_type = "M365";
			var m365_support_type = "flat";
			var m365_support_amount = "";
			var m365_active = "true";
			
			if(CONTRACT.azure.current_id) {
				if(CONTRACT.azure.current_type == "azure") {
					var service = $.grep(CONTRACT.azure.azure_services, function(n) { return n.id == CONTRACT.azure.current_id; });
					service = service[0];
					
					device_id = service.deviceId;
					service_id = service.serviceId;
					azure_customer_id = service.customerId;
					subscription_id = service.subscriptionId;
					start_date = UTIL.convert_dates_for_ui(service.startDate);
					end_date = UTIL.convert_dates_for_ui(service.endDate);
					name = service.name;
					customer_type = service.customerType;
					
					var subscription_type = service.subscriptionType;
					if(subscription_type == CONTRACT.azure.subscription_types.aws) {
						$(CONTRACT.azure.m365_field_class).hide();
						$(CONTRACT.azure.azure_field_class).hide();
						$(CONTRACT.azure.aws_field_class).show();
					} else if(subscription_type == CONTRACT.azure.subscription_types.azure || subscription_type == CONTRACT.azure.subscription_types.azureplan) {
						$(CONTRACT.azure.m365_field_class).hide();
						$(CONTRACT.azure.aws_field_class).hide();
						$(CONTRACT.azure.azure_field_class).show();
					}
				} else if(CONTRACT.azure.current_type == "M365") {
					var service = $.grep(CONTRACT.azure.m365_services, function(n) { return n.id == CONTRACT.azure.current_id; });
					console.log(service);
					service = service[0];
					
					device_id = service.deviceId;
					service_id = service.serviceId;
					azure_customer_id = service.tenantId;
					m365_type = service.type;
					m365_support_type = service.supportType;
					if(m365_support_type == "flat") {
						m365_support_amount = service.flatFee;
					} else {
						m365_support_amount = service.percent;
					}
					m365_active = service.active.toString();
					
					
					$(CONTRACT.azure.azure_field_class).hide();
					$(CONTRACT.azure.aws_field_class).hide();
					$(CONTRACT.azure.m365_field_class).show();
					
					if(m365_type == "M365") {
						$(CONTRACT.azure.m365_type_class).show();
						
						if(m365_support_type == "flat") {
							$(CONTRACT.azure.m365_support_type_flat_class).show();
							$(CONTRACT.azure.m365_support_type_percent_class).hide();
						} else {
							$(CONTRACT.azure.m365_support_type_flat_class).hide();
							$(CONTRACT.azure.m365_support_type_percent_class).show();
						}
					} else {
						$(CONTRACT.azure.m365_type_class).hide();
					}
					
				}
			} else {
				//populate dates
				start_date = $(CONTRACT.date_selectors.current_viewing_start_date).val();
				end_date = $(CONTRACT.date_selectors.contract_end_date).val();
				
				$(CONTRACT.azure.m365_field_class).hide();
				$(CONTRACT.azure.aws_field_class).hide();
				$(CONTRACT.azure.azure_field_class).show();
			}
			
			$(CONTRACT.azure.azure_device_field).val(device_id);
			$(CONTRACT.azure.azure_service_field).val(service_id);
			$(CONTRACT.azure.azure_customer_id_field).val(azure_customer_id);
			$(CONTRACT.azure.azure_subcription_id_field).val(subscription_id);
			$(CONTRACT.azure.azure_start_date_field).val(start_date);
			$(CONTRACT.azure.azure_end_date_field).val(end_date);
			$(CONTRACT.azure.azure_ci_name_field).val(name);
			$(CONTRACT.azure.azure_customer_type_field).val(customer_type);
			$(CONTRACT.azure.azure_m365_type_field).val(m365_type);
			$(CONTRACT.azure.azure_m365_support_type_field).val(m365_support_type);
			$(CONTRACT.azure.azure_m365_support_amount_field).val(m365_support_amount);
			$(CONTRACT.azure.azure_m365_active_field).val(m365_active);
			
			if($(CONTRACT.azure.azure_service_field).val()) {
				$(CONTRACT.azure.azure_service_field).prop("disabled", true);
			} else {
				$(CONTRACT.azure.azure_service_field).prop("disabled", false);
			}
			
			//locked display values
			$(CONTRACT.azure.azure_device_field).siblings(CONTRACT.contract_services.edit.locked_display_value_class).html($(CONTRACT.azure.azure_device_field + " option:selected").text());
			$(CONTRACT.azure.azure_service_field).siblings(CONTRACT.contract_services.edit.locked_display_value_class).html($(CONTRACT.azure.azure_service_field + " option:selected").text());
			$(CONTRACT.azure.azure_customer_id_field).siblings(CONTRACT.contract_services.edit.locked_display_value_class).html(azure_customer_id);
			$(CONTRACT.azure.azure_subcription_id_field).siblings(CONTRACT.contract_services.edit.locked_display_value_class).html(subscription_id);
			$(CONTRACT.azure.azure_start_date_field).siblings(CONTRACT.contract_services.edit.locked_display_value_class).html(start_date);
			$(CONTRACT.azure.azure_end_date_field).siblings(CONTRACT.contract_services.edit.locked_display_value_class).html(end_date);
			$(CONTRACT.azure.azure_ci_name_field).siblings(CONTRACT.contract_services.edit.locked_display_value_class).html(name);
			$(CONTRACT.azure.azure_customer_type_field).siblings(CONTRACT.contract_services.edit.locked_display_value_class).html($(CONTRACT.azure.azure_customer_type_field + " option:selected").text());
			
			CONTRACT.contract_services.edit.toggle_dialog_lock($(CONTRACT.azure_dialog));
		},
		populate_azure_device_field:function(devices) {
			var output = "<option value=\"\"></option>";
			for(var i=0; i < devices.length; i++) {
				var device = devices[i];
				var device_type = device.deviceType;
				if(device_type == "cspazure" || device_type == "cspazureplan" || device_type == "aws" || device_type == "M365Subscription") output += "<option value=\"" + device.id + "\" data-default-osp-id=\"" + device.defaultOspId + "\" data-device-type=\"" + device_type + "\">" + device.description + " (" + device.partNumber + ")</option>";
			}
			$(CONTRACT.azure.azure_device_field).html(output);
		},
		get_azure_services:function() {
			CONTRACT.azure.azure_services = null;
			CONTRACT.azure.m365_services = null;
			clearInterval(CONTRACT.azure.services_interval);
			
			var start_date = $(CONTRACT.date_selectors.current_viewing_start_date).val();
			var view_month_url = "";
			if(start_date) view_month_url = "/" + moment(start_date, CONTRACT.date_selectors.moment_format).format("MM/YYYY");
			
			UTIL.add_table_loader(CONTRACT.azure.azure_table);
			
			//get azure
			$.ajax ({
				url: PAGE_CONSTANTS.BASE_URL + "contractservices/subscription" + view_month_url + ".json?cid=" + CONTRACT.contract_id,
				type: "GET",
				success:function(data) {
					CONTRACT.azure.azure_services = data;
					
				}
			});
			
			//get m365
			$.ajax ({
				url: PAGE_CONSTANTS.BASE_URL + "contractservices/m365subscription.json?cid=" + CONTRACT.contract_id,
				type: "GET",
				success:function(data) {
					CONTRACT.azure.m365_services = data;
				}
			});
			
			CONTRACT.azure.services_interval = setInterval("CONTRACT.azure.build_subscription_services()", 500);
		},
		build_subscription_services:function() {
			if(CONTRACT.azure.azure_services && CONTRACT.azure.m365_services) {
				clearInterval(CONTRACT.azure.services_interval);
				CONTRACT.azure.build_azure_services();
			}
		},
		get_azure_lineitems:function(id, start_date, end_date) {
			start_date = UTIL.convert_dates_for_server(start_date);
			end_date = UTIL.convert_dates_for_server(end_date);
			
			UTIL.add_table_loader(CONTRACT.azure.azure_row_prefix + id + " " + CONTRACT.azure.azure_lineitem_container, "Retreiving Data from Azure...");
			
			$.ajax ({
				url: PAGE_CONSTANTS.BASE_URL + "services/azure/csa/" + id + ".json?invd=" + start_date,
				type: "GET",
				success:function(data) {
					CONTRACT.azure.build_lineitem_table(data[0].lineItems, id);
				}
			});
		},
		get_aws_lineitems:function(id, start_date, end_date) {
			start_date = UTIL.convert_dates_for_server(start_date);
			end_date = UTIL.convert_dates_for_server(end_date);
			
			UTIL.add_table_loader(CONTRACT.azure.azure_row_prefix + id + " " + CONTRACT.azure.azure_lineitem_container, "Retreiving Data from AWS...");
			
			$.ajax ({
				url: PAGE_CONSTANTS.BASE_URL + "services/aws/sub/" + id + ".json?invd=" + start_date,
				type: "GET",
				success:function(data) {
					CONTRACT.azure.build_aws_lineitem_table(data[0].lineItems, id);
				}
			});
		},
		build_aws_lineitem_table:function(lineitems, csa_id) {
			var output = "";
			if(lineitems && lineitems.length > 0) {
				var total = 0;
				for(var i=0; i < lineitems.length; i++) {
					var lineitem = lineitems[i];
					output += "<tr><td>" + lineitem.name + "</td><td class=\"right\">" + UTIL.convert_currency_for_ui(lineitem.total) + "</td></tr>";
					total += lineitem.total;
				}
				output += "<tr class=\"total-row\"><td>TOTAL</td><td class=\"right\">" + UTIL.convert_currency_for_ui(total) + "</td></tr>";
			} else {
				output = "<tr><td class=\"no-results\">No lineitems returned.</td></tr>";
			}
			$(CONTRACT.azure.azure_row_prefix + csa_id + " " + CONTRACT.azure.azure_lineitem_container + " tbody").html(output);
		},
		build_lineitem_table:function(lineitems, csa_id) {
			var output = "";
			if(lineitems && lineitems.length > 0) {
				var total = 0;
				for(var i=0; i < lineitems.length; i++) {
					var lineitem = lineitems[i];
					output += "<tr><td>" + lineitem.name + "</td><td class=\"right\">" + UTIL.convert_currency_for_ui(lineitem.total) + "</td></tr>";
					total += lineitem.total;
				}
				output += "<tr class=\"total-row\"><td>TOTAL</td><td class=\"right\">" + UTIL.convert_currency_for_ui(total) + "</td></tr>";
			} else {
				output = "<tr><td class=\"no-results\">No lineitems returned.</td></tr>";
			}
			$(CONTRACT.azure.azure_row_prefix + csa_id + " " + CONTRACT.azure.azure_lineitem_container + " tbody").html(output);
		},
		build_azure_services:function() {
			var services = CONTRACT.azure.azure_services;
			var m365 = CONTRACT.azure.m365_services;
			if((services && services.length > 0) || (m365 && m365.length > 0)) {
				if(services && services.length > 0) {
					var output = "";
					for(var i=0; i < services.length; i++) {
						var service = services[i];
						var service_name = service.serviceName;
						var subscription_id = service.subscriptionId;
						var customer_id = service.customerId;
						var customer_display = "";
						var subscription_type = service.subscriptionType;
						var subscription_display = subscription_id;
	
						if(!subscription_id) {
							subscription_display = "<i class=\"fas fa-exclamation-triangle\"></i>Subscription ID is Missing";
						}
						if(!customer_id && (subscription_type == CONTRACT.azure.subscription_types.azure || subscription_type == CONTRACT.azure.subscription_types.azureplan)) {
							customer_display = "<i class=\"fas fa-exclamation-triangle\"></i>Customer ID is Missing";
						} else if(customer_id) {
							customer_display = customer_id;
						}
						
						if(!subscription_id || (!customer_id && (subscription_type == CONTRACT.azure.subscription_types.azure || subscription_type == CONTRACT.azure.subscription_types.azureplan))) {
							service_name = "<i class=\"fas fa-exclamation-triangle\"></i>" + service_name;
						}
						
						output += "<tr>";
						output += "<td><a href=\"javascript:;\" class=\"popup-link azure-popup-link\" data-dialog=\"azure-dialog\" data-id=\"" + service.id + "\" data-type=\"azure\">" + service_name + "</a></td>";
						output += "<td>" + service.name + "</td>";
						output += "<td>" + service.devicePartNumber + "</td>";
						output += "<td>" + service.deviceDescription + "</td>";
						output += "<td>" + customer_display + "</td>";
						output += "<td>" + subscription_display + "</td>";
						output += "<td class=\"center\">" + UTIL.convert_dates_for_ui(service.startDate) + "</td>";
						output += "<td class=\"center\">" + UTIL.convert_dates_for_ui(service.endDate) + "</td>";
						output += "<td class=\"center\"><a href=\"javascript:;\" class=\"popup-link azure-delete-popup-link\" data-dialog=\"delete-azure-dialog\" data-id=\"" + service.id + "\" data-type=\"azure\" data-subscription-id=\"" + subscription_id + "\"><i class=\"fa fa-minus-circle\"></i>Delete</a></td>";
						output += "</tr>";
					}
					$(CONTRACT.azure.azure_table + " tbody").html(output);
					$(CONTRACT.azure.azure_table).show();
				} else {
					$(CONTRACT.azure.azure_table).hide();
				}
				
				if(m365 && m365.length > 0) {
					var output = "";
					for(var i=0; i < m365.length; i++) {
						var m365_service = m365[i];
						var service_name = m365_service.serviceName;
						var customer_id = m365_service.tenantId;
						var customer_display = "";
						var active_display = "Yes";
	
						if(!customer_id && (subscription_type == CONTRACT.azure.subscription_types.azure || subscription_type == CONTRACT.azure.subscription_types.azureplan)) {
							customer_display = "<i class=\"fas fa-exclamation-triangle\"></i>Customer ID is Missing";
						} else if(customer_id) {
							customer_display = customer_id;
						}
						
						if(!m365_service.active) {
							active_display = "No";
						}
						
						output += "<tr>";
						output += "<td><a href=\"javascript:;\" class=\"popup-link azure-popup-link\" data-dialog=\"azure-dialog\" data-id=\"" + m365_service.id + "\" data-type=\"M365\">" + service_name + "</a></td>";
						output += "<td>" + m365_service.type + "</td>";
						output += "<td>" + m365_service.devicePartNumber + "</td>";
						output += "<td>" + m365_service.deviceDescription + "</td>";
						output += "<td>" + customer_display + "</td>";
						output += "<td>" + active_display + "</td>";
						output += "<td class=\"center\"><a href=\"javascript:;\" class=\"popup-link azure-delete-popup-link\" data-dialog=\"delete-azure-dialog\" data-id=\"" + m365_service.id + "\" data-type=\"M365\" data-subscription-id=\"" + customer_id + "\"><i class=\"fa fa-minus-circle\"></i>Delete</a></td>";
						output += "</tr>";
					}
					$(CONTRACT.azure.m365_table + " tbody").html(output);
					$(CONTRACT.azure.m365_table).show();
				} else {
					$(CONTRACT.azure.m365_table).hide();
				}
				$(CONTRACT.azure.azure_container).show();
			} else {
				$(CONTRACT.azure.azure_container).hide();
			}
		},
		submit_items:function() {
			UTIL.clear_message_in_popup(CONTRACT.azure_dialog);
			
			var device_id = $(CONTRACT.azure.azure_device_field).val();
			var device_type = $(CONTRACT.azure.azure_device_field + " option:selected").data("device-type");
			var service_id = $(CONTRACT.azure.azure_service_field).val();
			var azure_customer_id = $(CONTRACT.azure.azure_customer_id_field).val();
			var subscription_id = $(CONTRACT.azure.azure_subcription_id_field).val();
			var customer_type = $(CONTRACT.azure.azure_customer_type_field).val();
			var m365_type = $(CONTRACT.azure.azure_m365_type_field).val();
			var m365_support_type = $(CONTRACT.azure.azure_m365_support_type_field).val();
			var m365_support_amount = $(CONTRACT.azure.azure_m365_support_amount_field).val();
			var m365_active = $(CONTRACT.azure.azure_m365_active_field).val();
			var start_date = $(CONTRACT.azure.azure_start_date_field).val();
			var end_date = $(CONTRACT.azure.azure_end_date_field).val();
			var name = $(CONTRACT.azure.azure_ci_name_field).val();
			var ajax_type = "POST";
			
			if(device_type != CONTRACT.azure.subscription_types.M365Subscription) {
				if(!device_id || !service_id || !subscription_id || !start_date || !end_date) {
					UTIL.add_error_message_to_popup(CONTRACT.azure_dialog, $(CONTRACT.general_validation_error).val());
					return false;
				}
			} else {
				if(!device_id || !service_id || !azure_customer_id || !m365_type) {
					UTIL.add_error_message_to_popup(CONTRACT.azure_dialog, $(CONTRACT.general_validation_error).val());
					return false;
				}
				
				if(m365_type == "M365" && (!m365_support_type || !m365_support_amount)) {
					UTIL.add_error_message_to_popup(CONTRACT.azure_dialog, $(CONTRACT.general_validation_error).val());
					return false;
				}
			}
			
			if(device_type == CONTRACT.azure.subscription_types.aws) {
				if(!customer_type) {
					UTIL.add_error_message_to_popup(CONTRACT.azure_dialog, $(CONTRACT.general_validation_error).val());
					return false;
				}
				azure_customer_id = "";
			}
			
			if(!azure_customer_id && (device_type == CONTRACT.azure.subscription_types.azure || device_type == CONTRACT.azure.subscription_types.azureplan)) {
				UTIL.add_error_message_to_popup(CONTRACT.azure_dialog, $(CONTRACT.general_validation_error).val());
				return false;
			}
			
			start_date = UTIL.convert_dates_for_server(start_date);
			end_date = UTIL.convert_dates_for_server(end_date);
			
			var json = { serviceId:service_id, deviceId:device_id, contractId:CONTRACT.contract_id, startDate:start_date, endDate:end_date, name:name, subscriptionId:subscription_id, customerId:azure_customer_id, subscriptionType:device_type, customerType:customer_type };
			var url = PAGE_CONSTANTS.BASE_URL + "contractservices/subscription.json";
			
			if(device_type == CONTRACT.azure.subscription_types.M365Subscription) {
				var percent = "0";
				var flat_fee = "0";
				if(m365_type != "O365") {
					if(m365_support_type == "flat") {
						flat_fee = m365_support_amount;
					} else if(m365_support_type == "percent") {
						percent = m365_support_amount;
					}
				}
				
				json = { serviceId:service_id, deviceId:device_id, contractId:CONTRACT.contract_id, tenantId:azure_customer_id, type:m365_type, supportType:m365_support_type, flatFee:flat_fee, percent:percent, active:m365_active };
				url = PAGE_CONSTANTS.BASE_URL + "contractservices/m365subscription.json";
			}
			
			if(CONTRACT.azure.current_id) {
				json["id"] = CONTRACT.azure.current_id;
				ajax_type = "PUT";
			}
			
			json = UTIL.remove_null_properties(json);
			UTIL.add_dialog_loader(CONTRACT.azure_dialog);
			
			$.ajax ({
				url: url,
				data: JSON.stringify(json),
				type: ajax_type,
				success:function(data) {
					UTIL.remove_dialog_loader(CONTRACT.azure_dialog);
					
					if(data.status == PAGE_CONSTANTS.OK_STS) {
						UTIL.add_success_message_to_popup(CONTRACT.azure_dialog, $(CONTRACT.azure.msg_saved_successfully).val());
						$(CONTRACT.azure_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").hide();
						$(CONTRACT.azure_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button:contains('OK')").show();
						
						CONTRACT.azure.get_azure_services();
						CONTRACT.azure.get_m365_subscriptions();
					} else {
						UTIL.add_error_message_to_popup(CONTRACT.azure_dialog, data.message);
					}
				}
			});
		},
		delete_item:{
			azure_delete_dialog:"#delete-azure-dialog",
			azure_subscription_id_field:"#delete-azure-subscription",
			msg_deleted_successfully:"#delete-contract-service-azure-success-msg",
			init:function() {
				CONTRACT.azure.delete_item.bind_events();
			},
			bind_events:function() {
				$(CONTRACT.azure.delete_item.azure_delete_dialog).dialog({
					  autoOpen:false,
					  dialogClass:"contract-dialog",	
				      resizable:false,
				      width:700,
				      height:500,
				      modal:true,
				      title: "Delete an Azure Service",
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
				        	CONTRACT.azure.delete_item.submit_delete();
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
				
				$(document).on("click", CONTRACT.azure.azure_delete_link, function() {
					var id = $(this).data("id");
					var type = $(this).data("type");
					var subscription_id = $(this).data("subscription-id");
					(id == null) ? CONTRACT.azure.current_id = null : CONTRACT.azure.current_id = id;
					(type == null) ? CONTRACT.azure.current_type = null : CONTRACT.azure.current_type = type;
					$(CONTRACT.azure.delete_item.azure_subscription_id_field).html(subscription_id);
				});
			},
			submit_delete:function() {
				UTIL.clear_message_in_popup(CONTRACT.azure.delete_item.azure_delete_dialog);
				
				UTIL.add_dialog_loader(CONTRACT.azure.delete_item.azure_delete_dialog);
				
				var url = PAGE_CONSTANTS.BASE_URL + "contractservices/subscription/" + CONTRACT.azure.current_id + ".json";
				if(CONTRACT.azure.current_type == "M365") {
					url = PAGE_CONSTANTS.BASE_URL + "contractservices/m365subscription/" + CONTRACT.azure.current_id + ".json";
				}
				
				$.ajax ({
					url: url,
					type: "DELETE",
					success:function(data) {
						UTIL.remove_dialog_loader(CONTRACT.azure.delete_item.azure_delete_dialog);
						
						if(data.status == PAGE_CONSTANTS.OK_STS) {
							UTIL.add_success_message_to_popup(CONTRACT.azure.delete_item.azure_delete_dialog, $(CONTRACT.azure.delete_item.msg_deleted_successfully).val());
							$(CONTRACT.azure.delete_item.azure_delete_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").hide();
							$(CONTRACT.azure.delete_item.azure_delete_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button:contains('OK')").show();
							
							CONTRACT.azure.get_azure_services();
							CONTRACT.azure.get_m365_subscriptions();
						} else {
							UTIL.add_error_message_to_popup(CONTRACT.azure.delete_item.azure_delete_dialog, data.message);
						}
					}
				});
			}
		}
	},
	contract_groups: {
		contract_group_field:"#current-viewing-group",
		contract_group_action_field:"#manage-groups-action",
		contract_group_clone_field:"#contract-group-clone",
		group_field:"#manage-groups-group",
		group_name_field:"#manage-groups-name",
		group_description_field:"#manage-groups-description",
		group_container:"#manage-groups-group-container",
		group_form_divider_container:"#manage-groups-form-divider",
		group_modify_container:"#manage-groups-modify-container",
		group_delete_msg:"#manage-groups-delete-msg",
		add_contract_group_success_msg:"#add-contract-group-success-msg",
		update_contract_group_success_msg:"#update-contract-group-success-msg",
		delete_contract_group_success_msg:"#delete-contract-group-success-msg",
		contract_group_dropdowns:".contract-group-list",
		current_view_id:null,
		current_edit_id:null,
		init:function() {
			CONTRACT.contract_groups.bind_events();
		},
		bind_events:function() {
			$(CONTRACT.contract_groups.contract_group_field).selectBox();
			
			$(CONTRACT.contract_groups.contract_group_field).change(function() {
				var value = $(this).val();
				(value == "#") ? CONTRACT.contract_groups.current_view_id = null : CONTRACT.contract_groups.current_view_id = value;
				CONTRACT.contract_services.get_contract_services(CONTRACT.contract_id);
			});
			
			$(CONTRACT.contract_groups.contract_group_action_field).change(function() {
				var action = $(this).val();
				if(action == "add") {
					$(CONTRACT.contract_groups.group_form_divider_container).show();
					$(CONTRACT.contract_groups.group_modify_container).show();
					$(CONTRACT.contract_groups.group_container).hide();
				} else if(action == "edit") {
					$(CONTRACT.contract_groups.group_form_divider_container).hide();
					$(CONTRACT.contract_groups.group_modify_container).hide();
					$(CONTRACT.contract_groups.group_container).show();
				} else if(action == "delete") {
					$(CONTRACT.contract_groups.group_form_divider_container).hide();
					$(CONTRACT.contract_groups.group_modify_container).hide();
					$(CONTRACT.contract_groups.group_container).show();
				}
				$(CONTRACT.contract_groups.group_delete_msg).hide();
				$(CONTRACT.contract_groups.group_field).val("#");
			});
			
			$(CONTRACT.contract_groups.group_field).change(function() {
				var action = $(CONTRACT.contract_groups.contract_group_action_field).val();
				var selected = $(this).val();
				if(selected == "#") {
					CONTRACT.contract_groups.current_edit_id = null;
				} else {
					CONTRACT.contract_groups.current_edit_id = selected;
				}
				
				if(action == "edit") {
					var name = $(this).find("option:selected").data("name");
					var description = $(this).find("option:selected").data("description");
					
					$(CONTRACT.contract_groups.group_name_field).val(name);
					$(CONTRACT.contract_groups.group_description_field).val(description);
					$(CONTRACT.contract_groups.group_modify_container).show();
					$(CONTRACT.contract_groups.group_delete_msg).hide();
				} else if(action == "delete") {
					$(CONTRACT.contract_groups.group_modify_container).hide();
					$(CONTRACT.contract_groups.group_delete_msg).show();
				}
				
				$(CONTRACT.contract_groups.group_form_divider_container).show();
			});
		},
		reset_popup:function() {
			UTIL.clear_message_in_popup(CONTRACT.manage_groups_dialog);
			
			$(CONTRACT.contract_groups.contract_group_action_field).val("#");
			$(CONTRACT.contract_groups.group_field).val("#");
			$(CONTRACT.contract_groups.group_name_field).val("");
			$(CONTRACT.contract_groups.group_description_field).val("");
			
			$(CONTRACT.contract_groups.group_container).hide();
			$(CONTRACT.contract_groups.group_modify_container).hide();
			$(CONTRACT.contract_groups.group_form_divider_container).hide();
			$(CONTRACT.contract_groups.group_delete_msg).hide();
		},
		reload_contract_groups:function() {
			$.ajax ({
				url: PAGE_CONSTANTS.BASE_URL + "contractgroups.json?cid=" + CONTRACT.contract_id,
				type: "GET",
				success:function(data) {
					CONTRACT.contract_groups.set_dropdowns(data);
				}
			});
		},
		set_dropdowns:function(options) {
			var dialog_options = "<option value=\"#\"></option>"; 
			var filter_options = "<option value=\"#\">All Groups</option>";
			var clone_options = "<option value=\"#\"></option>";
			var current_selection = CONTRACT.contract_groups.current_view_id;
			var id_found = false;
			
			for(var i=0; i < options.length; i++) {
				var option = options[i];
				dialog_options += "<option value=\"" + option.id + "\" data-name=\"" + option.name + "\" data-description=\"" + option.description + "\">" + option.name + "</option>";
				filter_options += "<option value=\"" + option.id + "\">" + option.name + "</option>";
				clone_options += "<option value=\"" + option.id + "\">" + option.name + "</option>";
				if(current_selection && option.id == current_selection) id_found = true;
			}
			
			$(CONTRACT.contract_groups.group_field).html(dialog_options);
			$(CONTRACT.contract_groups.contract_group_dropdowns).html(clone_options);
			
			$(CONTRACT.contract_groups.contract_group_field).selectBox("destroy");
			$(CONTRACT.contract_groups.contract_group_field).html(filter_options);
			
			if(!current_selection || !id_found) current_selection = "#";
			$(CONTRACT.contract_groups.contract_group_field).val(current_selection);
			$(CONTRACT.contract_groups.contract_group_field).selectBox();
			if(!id_found) {
				CONTRACT.contract_groups.current_view_id = null;
				CONTRACT.contract_services.get_contract_services(CONTRACT.contract_id);
			}
		},
		submit_form:function() {
			UTIL.clear_message_in_popup(CONTRACT.manage_groups_dialog);
			
			var action = $(CONTRACT.contract_groups.contract_group_action_field).val();
			var group = $(CONTRACT.contract_groups.group_field).val();
			var name = $(CONTRACT.contract_groups.group_name_field).val();
			var description = $(CONTRACT.contract_groups.group_description_field).val();
			var ajax_type = "POST";
			var json = { "name":name, "description":description, "contractId":CONTRACT.contract_id };
			var is_delete = "";
			var success_msg = "";
			
			if(action == "add") {
				if(!name) {
					UTIL.add_error_message_to_popup(CONTRACT.manage_groups_dialog, $(CONTRACT.general_validation_error).val());
					return false;
				}
				success_msg = $(CONTRACT.contract_groups.add_contract_group_success_msg).val();
			} else if(action == "edit") {
				if(!group || !name) {
					UTIL.add_error_message_to_popup(CONTRACT.manage_groups_dialog, $(CONTRACT.general_validation_error).val());
					return false;
				}
				
				ajax_type = "PUT";
				json["id"] = group;
				success_msg = $(CONTRACT.contract_groups.update_contract_group_success_msg).val();
			} else if(action == "delete") {
				if(!group) {
					UTIL.add_error_message_to_popup(CONTRACT.manage_groups_dialog, $(CONTRACT.general_validation_error).val());
					return false;
				}
				
				ajax_type = "DELETE";
				json = {};
				is_delete = "/" + group;
				success_msg = $(CONTRACT.contract_groups.delete_contract_group_success_msg).val();
			} else {
				UTIL.add_error_message_to_popup(CONTRACT.manage_groups_dialog, $(CONTRACT.general_validation_error).val());
				return false;
			}
			
			$.ajax ({
				url: PAGE_CONSTANTS.BASE_URL + "contractgroups" + is_delete + ".json",
				data: JSON.stringify(json),
				type: ajax_type,
				success:function(data) {
					UTIL.remove_dialog_loader(CONTRACT.manage_groups_dialog);
					
					if(data.status == PAGE_CONSTANTS.OK_STS) {
						UTIL.add_success_message_to_popup(CONTRACT.manage_groups_dialog, success_msg);
						$(CONTRACT.manage_groups_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").hide();
						$(CONTRACT.manage_groups_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button:contains('OK')").show();
						
						//update dropdowns
						CONTRACT.contract_groups.reload_contract_groups();
					} else {
						UTIL.add_error_message_to_popup(CONTRACT.manage_groups_dialog, data.message);
					}
				}
			});
		}
	},
	contract_adjustment: {
		recurring:"recurring",
		onetime:"onetime",
		edit_contract_adjustments_link:".edit-contract-adjustment-popup-link",
		init:function() {
			CONTRACT.contract_adjustment.bind_events();
		},
		bind_events:function() {
			$(CONTRACT.contract_adjustment.add.contract_adjustment_type_field).change(function() {
				var selected = $(this).val();
				if(selected == CONTRACT.contract_adjustment.onetime) {
					$(CONTRACT.contract_adjustment.add.contract_adjustment_end_date_container).hide();
				} else {
					$(CONTRACT.contract_adjustment.add.contract_adjustment_end_date_container).show();
				}
			});
			$(document).on("click", CONTRACT.contract_adjustment.edit_contract_adjustments_link, function() {
				var dialog = $(this).data("dialog");
				CONTRACT.contract_adjustment.edit.current_id = $(this).data("id");
				
				UTIL.open_dialog("#" + dialog);
			});
		},
		edit: {
			contract_adjustment_table:"#edit-contract-adjustment-table",
			contract_adjustment_id_field: "input[name='edit-contract-adjustment-id']",
			contract_adjustment_type_field: "select[name='edit-contract-adjustment-type']",
			contract_adjustment_amount_field: "input[name='edit-contract-adjustment-amount']",
			contract_adjustment_start_date_field: "input[name='edit-contract-adjustment-start-date']",
			contract_adjustment_end_date_field: "input[name='edit-contract-adjustment-end-date']",
			contract_adjustment_notes_field: "textarea[name='edit-contract-adjustment-notes']",
			contract_adjustment_pcr_field:"select[name='edit-contract-adjustment-pcr']",
			contract_adjustment_group_field:"select[name='edit-contract-adjustment-group']",
			contract_adjustment_status_field:"select[name='edit-contract-adjustment-status']",
			contract_adjustment_update_success_msg: "#update-contract-adjustment-success-msg",
			current_start_date: null,
			current_end_date: null,
			current_type: null,
			current_id:null,
			reset_popup:function() {
				UTIL.clear_message_in_popup(CONTRACT.edit_contract_adjustment_dialog);
				$(CONTRACT.edit_contract_adjustment_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").show();
				$(CONTRACT.edit_contract_adjustment_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button:contains('OK')").hide();
				
				CONTRACT.contract_adjustment.edit.get_contract_adjustments();
			},
			get_contract_adjustments:function() {
				var contract_adjustment_id = CONTRACT.contract_adjustment.edit.current_id;
				
				$.ajax ({
					url: PAGE_CONSTANTS.BASE_URL + "contractadjustments/" + contract_adjustment_id + ".json",
					type: "GET",
					success:function(data) {
						CONTRACT.contract_adjustment.edit.populate_dialog(data);
					}
				});
			},
			populate_dialog:function(contract_adjustments) {
				var type = CONTRACT.contract_adjustment.edit.build_edit_adjustment_table(contract_adjustments);
				
				//select proper dropdown option
				$(CONTRACT.edit_contract_adjustment_dialog).find(CONTRACT.contract_adjustment.edit.contract_adjustment_type_field).val(type);
				
				var $dialog = $(CONTRACT.edit_contract_adjustment_dialog);
				CONTRACT.contract_services.edit.toggle_dialog_lock($dialog);
			},
			build_edit_adjustment_table:function(contract_adjustment) {
				var output = "";
				var type = "";
				//for(var i=0; i<contract_adjustments.length; i++) {
					var row_class="";
					var obj = contract_adjustment;
					var start_date = UTIL.convert_dates_for_ui(obj.startDate);
					var end_date = UTIL.convert_dates_for_ui(obj.endDate);
					var amount = UTIL.convert_currency_for_server(obj.adjustment);
					var pcr_id = obj.contractUpdateId;
					var contract_group_id = obj.contractGroupId;
					var status = obj.status;
					var notes = obj.note;
					type = obj.adjustmentType;
					if(notes == null) notes = "";
					
					amount = amount.replace("$","");
					
					//if(i % 2 != 0) row_class="odd";
					
					var options = $(CONTRACT.contract_adjustment.add.contract_adjustment_type_field).html();
					var select_text = "";
					$(CONTRACT.contract_adjustment.add.contract_adjustment_type_field + " option").each(function() {
						if($(this).val() == type) select_text = $(this).text();
					});
					
					var $pcr_options = $(CONTRACT.contract_adjustment.add.contract_adjustment_pcr_field + " option");
					var pcr_select = "<select name=\"edit-contract-adjustment-pcr\" class=\"edit-contract-field\">";
					var pcr_select_text = "";
					$pcr_options.each(function() {
						var selected = "";
						var val = $(this).val();
						var text = $(this).text();
						
						if(val == pcr_id) {
							selected = " selected=\"selected\"";
							pcr_select_text = text;
						}
						
						pcr_select += "<option value=\"" + val + "\"" + selected + ">" + text + "</option>";
					});
					pcr_select += "</select>";
					
					var $status_options = $(CONTRACT.contract_adjustment.add.contract_adjustment_status_field + " option");
					var status_select = "<select name=\"edit-contract-adjustment-status\" class=\"edit-contract-field\">";
					var status_select_text = "";
					$status_options.each(function() {
						var selected = "";
						var val = $(this).val();
						var text = $(this).text();
						
						if(val == status) {
							selected = " selected=\"selected\"";
							status_select_text = text;
						}
						
						status_select += "<option value=\"" + val + "\"" + selected + ">" + text + "</option>";
					});
					status_select += "</select>";
					
					var $group_options = $(CONTRACT.contract_groups.contract_group_clone_field + " option");
					var group_select = "<select name=\"edit-contract-adjustment-group\" class=\"edit-contract-field\">";
					var group_select_text = "";
					$group_options.each(function() {
						var selected = "";
						var val = $(this).val();
						var text = $(this).text();
						
						if(val == contract_group_id) {
							selected = " selected=\"selected\"";
							group_select_text = text;
						}
						
						group_select += "<option value=\"" + val + "\"" + selected + ">" + text + "</option>";
					});
					group_select += "</select>";
					
					output += "<tr data-operation=\"update\" class=\"edit-row " + row_class + "\">";
					output += "<td class=\"row-count\">" + 1 + "<input type=\"hidden\" name=\"edit-contract-adjustment-id\" value=\"" + obj.id + "\" /></td>";
					output += "<td><span class=\"field-symbol\"></span><span class=\"required-ind required\"></span><select name=\"edit-contract-adjustment-type\" class=\"edit-contract-field\">" + options + "</select><div class=\"edit-contract-deleted-value\">" + select_text + "</div></td>";
					output += "<td><span class=\"field-symbol\">$</span><span class=\"required-ind required\"></span><input type=\"text\" name=\"edit-contract-adjustment-amount\" class=\"edit-contract-field currency-negative\" placeholder=\"(ex. 265.39)\" size=\"9\" value=\"" + amount + "\" /><div class=\"edit-contract-deleted-value\">" + amount + "</div></td>";
					output += "<td><span class=\"field-symbol\"></span><span class=\"required-ind required\"></span><input type=\"text\" name=\"edit-contract-adjustment-start-date\" class=\"edit-contract-field datepicker\" placeholder=\"mm/dd/yyyy\" size=\"10\" value=\"" + start_date + "\" /><div class=\"edit-contract-deleted-value\">" + start_date + "</div></td>";
					output += "<td><span class=\"field-symbol\"></span><span class=\"required-ind required\"></span><input type=\"text\" name=\"edit-contract-adjustment-end-date\" class=\"edit-contract-field datepicker\" placeholder=\"mm/dd/yyyy\" value=\"" + end_date + "\" size=\"10\" /><div class=\"edit-contract-deleted-value\">" + end_date + "</div></td>";
					output += "<td><a href=\"javascript:;\" class=\"edit-contract-delete\"><i class=\"fa fa-minus-circle\"></i>" + $(CONTRACT.contract_services.edit.delete_msg).val() + "</a></td>";
					output += "</tr>";
					
					output += "<tr class=\"delete-row middle-row " + row_class + "\">";
					output += "<td>&nbsp;</td>";
					output += "<td colspan=\"4\"><textarea name=\"edit-contract-adjustment-notes\" class=\"edit-contract-field\" placeholder=\"Enter notes here...\">" + notes + "</textarea><div class=\"edit-contract-deleted-value\">" + notes + "</div></td>";
					output += "<td>&nbsp;</td>";
					output += "</tr>";
					
					output += "<tr class=\"pcr-row " + row_class + "\">";
					output += "<td>&nbsp;</td>";
					output += "<td colspan=\"2\"><span class=\"field-symbol\"></span><span class=\"required-ind\"></span><label>Group</label>" + group_select + "<span class=\"edit-contract-deleted-value\">" + group_select_text + "</span></td>";
					output += "<td><span class=\"field-symbol\"></span><span class=\"required-ind\"></span><label>Status</label>" + status_select + "<span class=\"edit-contract-deleted-value\">" + status_select_text + "</span></td>";
					output += "<td><span class=\"field-symbol\"></span><span class=\"required-ind\"></span><label>PCR</label>" + pcr_select + "<span class=\"edit-contract-deleted-value\">" + pcr_select_text + "</span></td>";
					output += "<td>&nbsp;</td>";
					output += "</tr>";
				//}
				$(CONTRACT.contract_adjustment.edit.contract_adjustment_table).find("tbody").html(output);
				return type;
			},
			submit_contract_adjustment:function() {
				UTIL.clear_message_in_popup(CONTRACT.edit_contract_adjustment_dialog);
				
				var adjustments = [];
				var contract_id = CONTRACT.contract_id;
				var $table = $(CONTRACT.contract_adjustment.edit.contract_adjustment_table);
				var contract_start_moment = moment($(CONTRACT.date_selectors.contract_start_date).val(), CONTRACT.date_selectors.moment_format);
				var contract_end_moment = moment($(CONTRACT.date_selectors.contract_end_date).val(), CONTRACT.date_selectors.moment_format);
				var error_msg = "";
				var is_valid = true;
				
				$table.find("tbody").find("tr.edit-row").each(function() {
					var $row = $(this);
					var id = $row.find(CONTRACT.contract_adjustment.edit.contract_adjustment_id_field).val();
					var start_date = $row.find(CONTRACT.contract_adjustment.edit.contract_adjustment_start_date_field).val();
					var end_date = $row.find(CONTRACT.contract_adjustment.edit.contract_adjustment_end_date_field).val();
					var amount = $row.find(CONTRACT.contract_adjustment.edit.contract_adjustment_amount_field).val();
					var adjustment_type = $row.find(CONTRACT.contract_adjustment.edit.contract_adjustment_type_field).val();
					var operation = $row.data("operation");
					var notes = $row.next().find("textarea").val();
					var contract_group_id = $row.next().next().find(CONTRACT.contract_adjustment.edit.contract_adjustment_group_field).val();
					var status = $row.next().next().find(CONTRACT.contract_adjustment.edit.contract_adjustment_status_field).val();
					var pcr_id = $row.next().next().find(CONTRACT.contract_adjustment.edit.contract_adjustment_pcr_field).val();
					var changed = true;
					
					//validate
					if(!amount || !start_date || !end_date) {
						error_msg = $(CONTRACT.general_validation_error).val();
						is_valid = true;
						return false;
					}
					
					// validate adjustment dates
					var start_moment = moment(start_date, CONTRACT.date_selectors.moment_format);
					var end_moment = moment(end_date, CONTRACT.date_selectors.moment_format);

					// 1. validate start & end date & if end date is valid then validate it is not before the start date
					if ((!start_moment.isValid()) || ((end_date) && (!end_moment.isValid()))){
						error_msg = $(CONTRACT.contract_start_date_validation_msg).val();
						is_valid = false;
						return false;
					} else if ((end_moment.isValid()) && (end_moment.isBefore(start_moment))) {
						error_msg = $(CONTRACT.contract_end_before_start_date_validation_msg).val();
						is_valid = false;
						return false;
					}
					
					// 2. validate that adjustment dates fall within contract date range
					if(start_moment.isBefore(contract_start_moment) || start_moment.isAfter(contract_end_moment) || (adjustment_type == CONTRACT.contract_adjustment.recurring && end_moment.isAfter(contract_end_moment))) {
						error_msg = $(CONTRACT.contract_adjustment.add.contract_dates_validation_msg).val();
						is_valid = false;
						return false;
					}
					
					start_date = UTIL.convert_dates_for_server(start_date);
					end_date = UTIL.convert_dates_for_server(end_date);
					
					var json = {};
					if(operation == "delete") {
						json = {"id":id, "contractId":contract_id, "adjustmentType":adjustment_type, "startDate":start_date, "endDate":end_date, "status":status, "operation":operation};
						adjustments.push(json);
					} else if(changed) {
						json = {"id":id, "contractId":contract_id, "operation":operation, "adjustment":amount, "adjustmentType":adjustment_type, "startDate":start_date, "endDate":end_date, "status":status, "note":notes};
						if(pcr_id && pcr_id != "#") json["contractUpdateId"] = pcr_id;
						if(contract_group_id && contract_group_id != "#") json["contractGroupId"] = contract_group_id;
						adjustments.push(json);
					}
					
				});
				
				if(!is_valid) {
					UTIL.add_error_message_to_popup(CONTRACT.edit_contract_adjustment_dialog, error_msg);
					return false;
				}
				
				UTIL.add_dialog_loader(CONTRACT.edit_contract_adjustment_dialog);
				
				$.ajax ({
					url: PAGE_CONSTANTS.BASE_URL + "contractadjustments.json?batch",
					data: JSON.stringify(adjustments),
					type: "POST",
					success:function(data) {
						UTIL.remove_dialog_loader(CONTRACT.edit_contract_adjustment_dialog);
						
						var status = data.status;
						if(status == PAGE_CONSTANTS.OK_STS) {
							//we may want to loop through the individual responses and see if any errors occurred in adding the objects
							var success_msg = $(CONTRACT.contract_adjustment.edit.contract_adjustment_update_success_msg).val();
							UTIL.add_success_message_to_popup(CONTRACT.edit_contract_adjustment_dialog, success_msg);
							$(CONTRACT.edit_contract_adjustment_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").hide();
							$(CONTRACT.edit_contract_adjustment_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button:contains('OK')").show();
							
							CONTRACT.contract_services.get_contract_services(CONTRACT.contract_id);
							
							$("." + CONTRACT.contract_services.edit.undo_delete_row_class).remove();
							$(CONTRACT.contract_services.edit.edit_contract_delete_all).hide();
						} else if (status == PAGE_CONSTANTS.ERRR_STS) {
							var error_msg = data.message;
							if(data.batchResults && data.batchResults.length > 0) {
								error_msg = data.batchResults[0].message;
							}
							UTIL.add_error_message_to_popup(CONTRACT.edit_contract_adjustment_dialog, error_msg);
						}
					}
				});
			}
		},
		add: {
			contract_adjustment_type_field:"#add-contract-adjustment-type",
			contract_adjustment_amount_field:"#add-contract-adjustment-amount",
			contract_adjustment_start_date_field:"#add-contract-adjustment-start-date",
			contract_adjustment_end_date_field:"#add-contract-adjustment-end-date",
			contract_adjustment_notes_field:"#add-contract-adjustment-notes",
			contract_adjustment_pcr_field:"#add-contract-adjustment-pcr",
			contract_adjustment_group_field:"#add-contract-adjustment-group",
			contract_adjustment_status_field:"#add-contract-adjustment-status",
			contract_adjustment_end_date_container: "#add-contract-adjustment-end-date-container",
			contract_adjustment_create_success_msg:"#add-contract-adjustment-success-msg",
			contract_dates_validation_msg:"#contract-adjustment-contract-dates-msg",
			reset_popup:function() {
				UTIL.clear_message_in_popup(CONTRACT.add_contract_adjustment_dialog);
				$(CONTRACT.add_contract_adjustment_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").show();
				$(CONTRACT.add_contract_adjustment_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button:contains('OK')").hide();
				
				$(CONTRACT.contract_adjustment.add.contract_adjustment_pcr_field).val("#");
				$(CONTRACT.contract_adjustment.add.contract_adjustment_type_field).val(CONTRACT.contract_adjustment.onetime);
				$(CONTRACT.contract_adjustment.add.contract_adjustment_end_date_container).hide();
				$(CONTRACT.contract_adjustment.add.contract_adjustment_amount_field).val("");
				$(CONTRACT.contract_adjustment.add.contract_adjustment_status_field).val("active");
				var start_date = moment($(CONTRACT.date_selectors.current_viewing_start_date).val(), CONTRACT.date_selectors.moment_format);
				if (!start_date.isValid()) {
					start_date = "";
				} else {
					start_date = $(CONTRACT.date_selectors.current_viewing_start_date).val()
				}
				$(CONTRACT.contract_adjustment.add.contract_adjustment_start_date_field).val(start_date);
				$(CONTRACT.contract_adjustment.add.contract_adjustment_end_date_field).val("");
				$(CONTRACT.contract_adjustment.add.contract_adjustment_notes_field).val("");
				
				var $contract_group = $(CONTRACT.contract_adjustment.add.contract_adjustment_group_field);
				$contract_group.val("#");
				if($contract_group.find("option").length > 1) {
					$(CONTRACT.contract_services.add.contract_group_class).show();
				} else {
					$(CONTRACT.contract_services.add.contract_group_class).hide();
				}
			},
			submit_contract_adjustment:function() {
				UTIL.clear_message_in_popup(CONTRACT.add_contract_adjustment_dialog);
				
				var adjustment_type = $(CONTRACT.contract_adjustment.add.contract_adjustment_type_field).val();
				var adjustment = $(CONTRACT.contract_adjustment.add.contract_adjustment_amount_field).val();
				var start_date = $(CONTRACT.contract_adjustment.add.contract_adjustment_start_date_field).val();
				var end_date = $(CONTRACT.contract_adjustment.add.contract_adjustment_end_date_field).val();
				var notes = $(CONTRACT.contract_adjustment.add.contract_adjustment_notes_field).val();
				var pcr_id = $(CONTRACT.contract_adjustment.add.contract_adjustment_pcr_field).val();
				var contract_group_id = $(CONTRACT.contract_adjustment.add.contract_adjustment_group_field).val();
				var status = $(CONTRACT.contract_adjustment.add.contract_adjustment_status_field).val();
				
				if(!adjustment || !start_date || (adjustment_type == CONTRACT.contract_adjustment.recurring && !end_date)) {
					UTIL.add_error_message_to_popup(CONTRACT.add_contract_adjustment_dialog, $(CONTRACT.general_validation_error).val());
					return false;
				}
				
				var contract_start_moment = moment($(CONTRACT.date_selectors.contract_start_date).val(), CONTRACT.date_selectors.moment_format);
				var contract_end_moment = moment($(CONTRACT.date_selectors.contract_end_date).val(), CONTRACT.date_selectors.moment_format);

				// validate adjustment dates
				var start_moment = moment(start_date, CONTRACT.date_selectors.moment_format);
				var end_moment = moment(end_date, CONTRACT.date_selectors.moment_format);

				// 1. validate start date & if end date is valid then validate it is not before the start date
				if ((!start_moment.isValid()) || ((end_date) && (!end_moment.isValid()))) {
					UTIL.add_error_message_to_popup(CONTRACT.add_contract_adjustment_dialog,$(CONTRACT.contract_start_date_validation_msg).val());
					return false;
				} else if ((end_moment.isValid()) && (end_moment.isBefore(start_moment))) {
					UTIL.add_error_message_to_popup(CONTRACT.add_contract_adjustment_dialog,$(CONTRACT.contract_end_before_start_date_validation_msg).val());
					return false;
				}

				// 2. validate that adjustment dates fall within contract date range
				if(start_moment.isBefore(contract_start_moment) || start_moment.isAfter(contract_end_moment)  || end_moment.isAfter(contract_end_moment)) {
					UTIL.add_error_message_to_popup(CONTRACT.add_contract_adjustment_dialog, $(CONTRACT.contract_adjustment.add.contract_dates_validation_msg).val());
					return false;
				}
				
				start_date = UTIL.convert_dates_for_server(start_date);
				if(adjustment_type == CONTRACT.contract_adjustment.onetime) {
					end_date = moment(start_date, CONTRACT.date_selectors.moment_format).endOf("month").format(CONTRACT.date_selectors.moment_format);
				}
				end_date = UTIL.convert_dates_for_server(end_date);
				
				var json = { "contractId":CONTRACT.contract_id, "adjustmentType":adjustment_type, "adjustment":adjustment, "startDate":start_date, "endDate":end_date, "status":status, "note":notes };
				if(pcr_id != "#") json["contractUpdateId"] = pcr_id;
				if(contract_group_id != "#") json["contractGroupId"] = contract_group_id;
				json = UTIL.remove_null_properties(json);
				
				UTIL.add_dialog_loader(CONTRACT.add_contract_adjustment_dialog);
				
				$.ajax ({
					url: PAGE_CONSTANTS.BASE_URL + "contractadjustments.json",
					data: JSON.stringify(json),
					type: "POST",
					success:function(data) {
						UTIL.remove_dialog_loader(CONTRACT.add_contract_adjustment_dialog);
						
						var status = data.status;
						if(status == PAGE_CONSTANTS.OK_STS) {
							var success_msg = $(CONTRACT.contract_adjustment.add.contract_adjustment_create_success_msg).val();
							UTIL.add_success_message_to_popup(CONTRACT.add_contract_adjustment_dialog, success_msg);
							$(CONTRACT.add_contract_adjustment_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").hide();
							$(CONTRACT.add_contract_adjustment_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button:contains('OK')").show();
							CONTRACT.contract_services.get_contract_services(CONTRACT.contract_id);
						} else if (status == PAGE_CONSTANTS.ERRR_STS) {
							var error_msg = data.message;
							if(error_msg.indexOf("[") != -1) {
								var msg_parts = error_msg.split("[");
								error_msg = msg_parts[1];
								error_msg = error_msg.replace("]","");
							}
							UTIL.add_error_message_to_popup(CONTRACT.add_contract_adjustment_dialog, error_msg);
						}
					}
				});
			}
		}
	},
	file_upload: {
		form:"#upload-form",
		form_target:"#upload-target",
		file_field: "#template-file",
		file_select_error_msg: "#file-select-file-error-msg",
		file_type_invalid_error_msg: "#file-type-invalid-error-msg",
		iframe_status:"#iframe-status",
		iframe_message:"#iframe-message",
		init:function() {
			CONTRACT.file_upload.bind_events();
		},
		bind_events:function() {
			$(CONTRACT.file_upload.form_target).load(function(){ 
				var iframe = $(CONTRACT.file_upload.form_target);
				var status = $(CONTRACT.file_upload.iframe_status, iframe.contents()).html();
				var message = $(CONTRACT.file_upload.iframe_message, iframe.contents()).html();
				CONTRACT.file_upload.complete_submit(status, message);
			});
		},
		reset_popup:function() {
			$(CONTRACT.upload_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").show();
	    	$(CONTRACT.upload_dialog).closest(".ui-dialog").find(".ui-button:contains('OK')").hide();
	    	UTIL.clear_message_in_popup(CONTRACT.upload_dialog);
	    	$(CONTRACT.file_upload.file_field).val("");
		},
		submit_file:function() {
			UTIL.clear_message_in_popup(CONTRACT.upload_dialog);
			
			var file = $(CONTRACT.file_upload.file_field).val();
			
			if(file == "") {
				var message = $(CONTRACT.file_upload.file_select_error_msg).val();
				UTIL.add_error_message_to_popup(CONTRACT.upload_dialog, message);
				return false;
			}
			
			if(!(/\.(xlsx)$/i).test(file)) {
				var message = $(CONTRACT.file_upload.file_type_invalid_error_msg).val();
				UTIL.add_error_message_to_popup(CONTRACT.upload_dialog, message);
				return false;
			}
			
			UTIL.add_dialog_loader(CONTRACT.upload_dialog);
			$(CONTRACT.file_upload.form).submit();
		},
		complete_submit:function(status, message) {
			UTIL.remove_dialog_loader(CONTRACT.upload_dialog);
			if(status == "success") {
				UTIL.add_success_message_to_popup(CONTRACT.upload_dialog, message);
				$(CONTRACT.upload_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").hide();
		    	$(CONTRACT.upload_dialog).closest(".ui-dialog").find(".ui-button:contains('OK')").show();
		    	CONTRACT.contract_services.get_contract_services(CONTRACT.contract_id);
			} else {
				UTIL.add_error_message_to_popup(CONTRACT.upload_dialog, message);
			}
		}
	},
	date_selectors: {
		moment_format: "MM/DD/YYYY",
		contract_start_date: "#contract-hidden-start-date",
		contract_end_date: "#contract-hidden-end-date",
		date_dropdown: "#current-viewing-date",
		current_viewing_start_date: "#current-viewing-start-date",
		current_viewing_end_date: "#current-viewing-end-date",
		current_viewing_display_start_date: "#current-viewing-display-start-date",
		current_viewing_display_end_date: "#current-viewing-display-end-date",
		current_view_prev_month: "#current-view-prev-month",
		current_view_next_month: "#current-view-next-month",
		switch_month_class: "switch-month-view",
		init:function() {
			CONTRACT.date_selectors.bind_events();
			CONTRACT.date_selectors.build_dropdown();
			CONTRACT.date_selectors.build_date_links();
		},
		bind_events:function() {
			$(CONTRACT.date_selectors.date_dropdown).change(function() {
				var value = $(this).val();
				var $option = $(this).find(":selected");
				
				CONTRACT.date_selectors.switch_month($option, value);
			});
			
			$(document).on("click", "." + CONTRACT.date_selectors.switch_month_class, function() {
				var $option = $(this);
				//$(CONTRACT.date_selectors.date_dropdown).val($option.data("value"));
				$(CONTRACT.date_selectors.date_dropdown).selectBox('value',$option.data("value"));
				CONTRACT.date_selectors.switch_month($option, "");
			});
		},
		switch_month:function($option, value) {
			var start_date = $option.data("start");
			var end_date = $option.data("end");
			
			$(CONTRACT.date_selectors.current_viewing_start_date).val(start_date);
			$(CONTRACT.date_selectors.current_viewing_end_date).val(end_date);
			CONTRACT.contract_services.get_contract_services(CONTRACT.contract_id);
			CONTRACT.azure.get_azure_services();
			
			if(value == "#") {
				start_date = $(CONTRACT.date_selectors.contract_start_date).val();
				end_date = $(CONTRACT.date_selectors.contract_end_date).val();
				
				$(CONTRACT.contract_services.download_excel_link).hide();
			} else {
				$(CONTRACT.contract_services.download_excel_link).show();
			}
			
			$(CONTRACT.date_selectors.current_viewing_display_start_date).html(start_date);
			$(CONTRACT.date_selectors.current_viewing_display_end_date).html(end_date);
			
			CONTRACT.date_selectors.build_date_links();
		},
		build_date_links:function() {
			var $option = $(CONTRACT.date_selectors.date_dropdown + " :selected");
			var $prev_option = $option.prev();
			var $next_option = $option.next();
			var prev_month_name = $prev_option.html();
			var prev_start_date = $prev_option.data("start");
			var prev_end_date = $prev_option.data("end");
			var prev_value = $prev_option.val();
			var next_month_name = $next_option.html();
			var next_start_date = $next_option.data("start");
			var next_end_date = $next_option.data("end");
			var next_value = $next_option.val();
			
			var prev_link = "";
			var next_link = "";
			if(prev_month_name != "All Time" && prev_month_name != undefined) prev_link = CONTRACT.date_selectors.build_date_link(prev_month_name, prev_start_date, prev_end_date, prev_value, true);
			if(next_month_name != "All Time" && next_month_name != undefined) next_link = CONTRACT.date_selectors.build_date_link(next_month_name, next_start_date, next_end_date, next_value, false);
			$(CONTRACT.date_selectors.current_view_prev_month).html(prev_link);
			$(CONTRACT.date_selectors.current_view_next_month).html(next_link);
		},
		build_date_link:function(month_name, start_date, end_date, value, prev) {
			var prev_icon = "<i class=\"fa fa-angle-double-left\"></i>";
			var next_icon = "<i class=\"fa fa-angle-double-right\"></i>";
			var output = "<a class=\"" + CONTRACT.date_selectors.switch_month_class + "\" href=\"javascript:;\" data-value=\"" + value + "\" data-start=\"" + start_date + "\" data-end=\"" + end_date + "\">";
			(prev) ? output += prev_icon + month_name : output += month_name + next_icon;
			output += "</a>";
			return output;
		},
		build_dropdown:function() {
			var output = "";
			var start_moment = moment($(CONTRACT.date_selectors.contract_start_date).val(), CONTRACT.date_selectors.moment_format).startOf("month");
			var end_moment = moment($(CONTRACT.date_selectors.contract_end_date).val(), CONTRACT.date_selectors.moment_format).endOf("month");
			var now = moment().startOf("month");
			while(start_moment < end_moment) {
				var selected = "";
				var month_start = start_moment.startOf("month").format(CONTRACT.date_selectors.moment_format);
				var month_end = start_moment.endOf("month").format(CONTRACT.date_selectors.moment_format);
				//set the month to the current month, as that is the page load default
				if(month_start == now.format(CONTRACT.date_selectors.moment_format)) {
					selected = " selected=\"selected\"";
					$(CONTRACT.date_selectors.current_viewing_start_date).val(month_start);
					$(CONTRACT.date_selectors.current_viewing_end_date).val(month_end);
					$(CONTRACT.date_selectors.current_viewing_display_start_date).html(month_start);
					$(CONTRACT.date_selectors.current_viewing_display_end_date).html(month_end);
				}
				output += "<option data-start=\"" + month_start + "\" data-end=\"" + month_end + "\"" + selected + ">" + start_moment.format("MMM YYYY") + "</option>";
				start_moment.add(1, 'months').startOf("month");
			}
			$(CONTRACT.date_selectors.date_dropdown).append(output);
			
			//initiate the fancy looking dropdown
			$(CONTRACT.date_selectors.date_dropdown).selectBox();
		}
	},
	contract_services: {
		display_table: "#contract-services",
		contract_services_table:"#contract-services-table",
		current_view: "expanded",
		display_view_class: ".contract-services-tab",
		display_views: { expanded:"expanded", changed:"changed", consolidated:"consolidated", pending:"pending", do_not_bill:"donotbill", ci:"ci" },
		changed_collapsed_rows: null,
		unchanged_activity_class: "unchanged-activity",
		current_row_class: "",
		contract_services_no_results:"#no-contract-services-msg",
		download_excel_link: "#download-excel",
		download_excel_ci_view_link: "#download-excel-ci-view",
		one_time_total:"#one-time-total",
		recurring_total:"#recurring-total",
		ci_name_column:"#ci-name-column",
		device_loader:"#device-loader",
		temporary_device_loader_class:".temporary-device-loader",
		requires_devices_class:".requires-devices",
		current_month_one_time_cost: 0,
		current_month_recurring_cost: 0,
		changed_month_one_time_cost: 0,
		changed_month_recurring_cost: 0,
		prev_month_cache: null,
		current_month_cache:null,
		difference_timer: null,
		load_completed:false,
		devices:[],
		init:function() {
			CONTRACT.contract_services.bind_events();
			CONTRACT.contract_services.get_contract_services(CONTRACT.contract_id);
			CONTRACT.contract_services.setup_autocompletes();
			CONTRACT.contract_services.add.init();
			CONTRACT.contract_services.edit.init();
			CONTRACT.contract_services.parent_map.init();
		},
		bind_events:function() {
			$(CONTRACT.contract_services.display_view_class).click(function() {
				var view = $(this).data("view");
				CONTRACT.contract_services.current_view = view;
				
				$(CONTRACT.contract_services.display_view_class).removeClass("selected");
				$(this).addClass("selected");
				
				CONTRACT.contract_services.get_contract_services(CONTRACT.contract_id);
			});
			$(CONTRACT.contract_services.add.contract_start_date_field).change(function() {
				var start_date = $(this).val();
				$(CONTRACT.contract_services.add.contract_one_time_bill_date_field).html(CONTRACT.contract_services.add.convert_start_date_to_bill_date(start_date));
			});
			$(CONTRACT.contract_services.add.contract_calculate_class).keyup(function() {
				CONTRACT.contract_services.add.calculate_cost();
			});
			$(document).on("click", CONTRACT.contract_services.edit.contract_service_edit_class, function() {
				CONTRACT.contract_services.edit.current_service_name = $(this).data("service-name");
				CONTRACT.contract_services.edit.current_service_id = $(this).data("service-id");
				CONTRACT.contract_services.edit.current_device_id = $(this).data("device-id");
				CONTRACT.contract_services.edit.current_start_date = $(this).data("start-date");
				CONTRACT.contract_services.edit.current_end_date = $(this).data("end-date");
				CONTRACT.contract_services.edit.current_id = $(this).data("id");
				
				var id = $(this).data("dialog");
	    		UTIL.open_dialog("#" + id);
			});
			$(document).on("click", "." + CONTRACT.contract_services.edit.delete_row_class, function() {
				CONTRACT.contract_services.edit.delete_row($(this));
			});
			$(document).on("click", "." + CONTRACT.contract_services.edit.undo_delete_row_class, function() {
				CONTRACT.contract_services.edit.undo_delete_row($(this));
			});
			
			$(document).on("click", CONTRACT.contract_services.edit.edit_contract_delete_all, function() {
				var action = $(this).data("action");
				if(action == "delete") {
					$("." + CONTRACT.contract_services.edit.delete_row_class).each(function() {
						CONTRACT.contract_services.edit.delete_row($(this));
					});
					$(this).html($(CONTRACT.contract_services.edit.undo_delete_all_msg).val()).data("action","undelete");
				} else {
					$("." + CONTRACT.contract_services.edit.undo_delete_row_class).each(function() {
						CONTRACT.contract_services.edit.undo_delete_row($(this));
					});
					$(this).html("<i class=\"fa fa-minus-circle\"></i>" + $(CONTRACT.contract_services.edit.delete_all_msg).val()).data("action","delete");
				}
			});
			
			$(document).on("click", "." + CONTRACT.contract_services.unchanged_activity_class, function() {
				//$(".changed-collapsed").toggle();
				var $icon = $(this).find("i");
				$icon.toggleClass("fa-chevron-circle-right fa-chevron-circle-down");
				
				if($icon.hasClass("fa-chevron-circle-down")) {
					$(this).parent().parent().after(CONTRACT.contract_services.changed_collapsed_rows);
				} else {
					$(".changed-collapsed").remove();
				}
			});
			
			$(CONTRACT.contract_services.download_excel_link).click(function() {
				var url = CONTRACT.contract_services.build_excel_download_link(false);
				$(this).attr("href",url);
				return true;
			});
			
			$(CONTRACT.contract_services.download_excel_ci_view_link).click(function() {
				var url = CONTRACT.contract_services.build_excel_download_link(true);
				$(this).attr("href",url);
				return true;
			});
			
			//modify autocomplete object
			/*
			$.ui.autocomplete.prototype._renderItem = function(ul, item) {
			    return $("<li>").append("<a>" + item.value + "<span>" + item.desc + "</span></a>").appendTo(ul);
			};*/
		},
		build_excel_download_link:function(ci_view) {
			var link = "";
			var start_date = $(CONTRACT.date_selectors.current_viewing_start_date).val();
			var view_month = moment(start_date).month() + 1;
			var view_year = moment(start_date).year();
			var contract_group = "";
			if(CONTRACT.contract_groups.current_view_id) contract_group = "&cgid=" + CONTRACT.contract_groups.current_view_id;
			link = PAGE_CONSTANTS.BASE_URL + "contractservices/" + view_month + "/" + view_year + ".xlsx?cid=" + CONTRACT.contract_id + contract_group;
			if(ci_view) link += "&civ=true";
			return link;
		},
		setup_autocompletes:function() {
			$(CONTRACT.contract_services.requires_devices_class).hide();
			
			$.ajax ({
				url: PAGE_CONSTANTS.BASE_URL + "data/devices.json?archived=false",
				type: "GET",
				success:function(data) {
					//remove loader
					//$(CONTRACT.contract_services.device_loader).remove();
					$(CONTRACT.contract_services.requires_devices_class).show();
					$(CONTRACT.contract_services.temporary_device_loader_class).hide();
					
					
					//set azure products
					CONTRACT.azure.populate_azure_device_field(data);
					
					//set autocompletes
					CONTRACT.contract_services.devices = data;
					var codes = [], descriptions = [];
					for(var i=0; i<data.length; i++) {
						var code = data[i].partNumber;
						var desc = data[i].description;
						var ospId = data[i].defaultOspId;
						var require_unit_count = data[i].requireUnitCount;
						var related_devices = data[i].relatedDevices;
						codes.push({ value:code, desc:desc, service:ospId, requireUnitCount:require_unit_count, relatedDevices:related_devices });
						descriptions.push({ value:desc, desc:code, service:ospId, requireUnitCount:require_unit_count, relatedDevices:related_devices });
					}
					//setup autocomplete fields
					CONTRACT.contract_services.bind_autocompletes(CONTRACT.contract_services.add.contract_part_number_field, codes, CONTRACT.contract_services.add.contract_part_description_field, CONTRACT.contract_services.add.contract_service_item_field, CONTRACT.contract_services.add.contract_quantity_field, CONTRACT.contract_services.add.contract_device_unit_count_field, "add");
					CONTRACT.contract_services.bind_autocompletes(CONTRACT.contract_services.edit.contract_part_number_field, codes, CONTRACT.contract_services.edit.contract_part_description_field, CONTRACT.contract_services.edit.contract_service_item_field, null, CONTRACT.contract_services.edit.contract_device_unit_count_field, "edit");
					CONTRACT.contract_services.bind_autocompletes(CONTRACT.contract_services.add.contract_part_description_field, descriptions, CONTRACT.contract_services.add.contract_part_number_field, CONTRACT.contract_services.add.contract_service_item_field, CONTRACT.contract_services.add.contract_quantity_field, CONTRACT.contract_services.add.contract_device_unit_count_field, "add");
					CONTRACT.contract_services.bind_autocompletes(CONTRACT.contract_services.edit.contract_part_description_field, descriptions, CONTRACT.contract_services.edit.contract_part_number_field, CONTRACT.contract_services.edit.contract_service_item_field, null, CONTRACT.contract_services.edit.contract_device_unit_count_field, "edit");
				}
			});
		},
		filter_embedded_device:function(device) {
			if(device.relatedDevices && device.relatedDevices.length> 0) {
				device.relatedDevices = device.relatedDevices.filter(function (el) {
					return el.relationship != "embedded";
				});
				
				for(var i = 0; i < device.relatedDevices.length; i++) {
					if(device.relatedDevices[i].relatedDevices && device.relatedDevices[i].relatedDevices.length > 0) {
						device.relatedDevices[i] = CONTRACT.contract_services.filter_embedded_device(device.relatedDevices[i]);
					}
				}
			}
			
			return device;
		},
		bind_autocompletes:function(obj_id, codes, chained_field_id, service_field_id, quantity_field_id, unit_count_field_id, type) {
			$(obj_id).autocomplete({ 
				source: codes,
				focus: function( event, ui ) {
			        $(obj_id).val(ui.item.value);
			        return false;
			      },
			      select: function( event, ui ) {
			        $(obj_id).val(ui.item.value);
			        $(chained_field_id).val(ui.item.desc);
			        var id = $(service_field_id).find(".osp-id-" + ui.item.service).val();
			        $(service_field_id).val(id);
			        if($(service_field_id).val()) {
			        	$(service_field_id).prop("disabled", true);
			        } else {
			        	$(service_field_id).prop("disabled", false);
			        }
			        
			        var $parent = $(service_field_id).parents(".dialog-content");
			        if(ui.item.requireUnitCount) {
			        	$parent.find(unit_count_field_id).siblings(".required-ind").addClass("required");
			        	$parent.find(unit_count_field_id).prop("disabled", false);
			        	$parent.find(quantity_field_id).prop("disabled", true).val(1);
			        	$parent.find(CONTRACT.contract_services.add.add_unit_price_view_class).show();
			        	$parent.find(CONTRACT.contract_services.add.add_total_price_view_class).hide();
			        } else {
			        	$parent.find(unit_count_field_id).siblings(".required-ind").removeClass("required");
			        	$parent.find(unit_count_field_id).prop("disabled", true).val("");
			        	$parent.find(quantity_field_id).prop("disabled", false);
			        	$parent.find(CONTRACT.contract_services.add.add_unit_price_view_class).hide();
			        	$parent.find(CONTRACT.contract_services.add.add_total_price_view_class).show();
			        }
			        
			        if(type == "add") {
			        	CONTRACT.contract_services.add.setup_ci_name_fields();
			        	var filtered_device = ui.item;
			        	filtered_device = CONTRACT.contract_services.filter_embedded_device(filtered_device);
			        	CONTRACT.contract_services.add.build_related_add_lineitems(filtered_device.relatedDevices);
			        }
			        
			        return false;
			      }
			}).data("uiAutocomplete")._renderItem = function( ul, item ) {
				return $("<li>").append("<a>" + item.value + "<span>" + item.desc + "</span></a>").appendTo(ul);
			};
			
		},
		get_device_id:function(part_number, description) {
			var device_id = null;
			
			var devices = $.grep(CONTRACT.contract_services.devices, function(n) { return n.partNumber == part_number; });
			if(devices.length > 0) {
				for(var i=0; i < devices.length; i++) {
					var device = devices[i];
					if(device.description == description) {
						device_id = device.id;
						break;
					}
				}
			}
			
			return device_id;
		},
		get_device_default_osp_id:function(device_id) {
			var default_osp_id = null;
			
			var devices = $.grep(CONTRACT.contract_services.devices, function(n) { return n.id == device_id; });
			if(devices.length > 0) {
				var device = devices[0];
				default_osp_id = device.defaultOspId;
			}
			
			return default_osp_id;
		},
		get_device_require_unit_count:function(device_id) {
			var require_unit_count = false;
			
			var devices = $.grep(CONTRACT.contract_services.devices, function(n) { return n.id == device_id; });
			if(devices.length > 0) {
				var device = devices[0];
				require_unit_count = device.requireUnitCount;
			}
			
			return require_unit_count;
		},
		get_device_has_parent:function(device_id) {
			var has_parent = false;
			
			var devices = $.grep(CONTRACT.contract_services.devices, function(n) { return n.id == device_id; });
			if(devices.length > 0) {
				var device = devices[0];
				has_parent = device.hasParent;
			}
			
			return has_parent;
		},
		get_device_has_children:function(device_id) {
			var has_children = false;
			
			var devices = $.grep(CONTRACT.contract_services.devices, function(n) { return n.id == device_id; });
			if(devices.length > 0) {
				var device = devices[0];
				
				//make sure it doesn't count embedded children
				if(device.relatedDevices && device.relatedDevices.length > 0) {
					var related_devices = [];
					for(var i = 0; i < device.relatedDevices.length; i++) {
						if(device.relatedDevices[i].relationship != "embedded") {
							related_devices.push(device.relatedDevices[i]);
						}
					}
					
					if(related_devices && related_devices.length > 0) {
						has_children = true;
					}
				}
			}
			
			return has_children;
		},
		get_device_type:function(device_id) {
			var device_type = null;
			
			var devices = $.grep(CONTRACT.contract_services.devices, function(n) { return n.id == device_id; });
			if(devices.length > 0) {
				var device = devices[0];
				device_type = device.deviceType
			}
			
			return device_type;
		},
		get_device:function(device_id) {
			var devices = $.grep(CONTRACT.contract_services.devices, function(n) { return n.id == device_id; });
			if(devices.length > 0) {
				var device = devices[0];
				return device;
			}
			
			return null;
		},
		get_device_has_parent_in_contract:function(device_id) {
			var has_parent = false;
			
			var devices = $.grep(CONTRACT.contract_services.devices, function(n) { return n.id == device_id; });
			if(devices.length > 0) {
				var device = devices[0];
				
				/*
				var related_devices = $.grep(CONTRACT.contract_services.devices, function(n) { return n.relatedDevices.id == device_id; });
				if(related_devices.length > 0) {
					console.log(related_devices);
				}*/
				if(device.hasParent) {
					//if it has a parent, we check all of the objects to see if any of those parents exist in the contract
					for(var i = 0; i < CONTRACT.contract_services.devices.length; i++) {
						var dev = CONTRACT.contract_services.devices[i];
						if(dev.relatedDevices) {
							var related_devices = $.grep(dev.relatedDevices, function(n) { return n.id == device_id; });
							if(related_devices.length > 0) {
								for(var j = 0; j < related_devices.length; j++) {
									var rel_dev = related_devices[j];
									var parent_id = rel_dev.parentId;
									
									var services = $.grep(CONTRACT.contract_services.current_month_cache, function(n) { return n.deviceId == parent_id; });
									if(services.length > 0) {
										has_parent = true;
									}
								}
							}
						}
					}
				}				
			}
			
			return has_parent;
		},
		edit: {
			current_service_id: null,
			current_device_id: null,
			current_service_name: null,
			current_start_date: null,
			current_end_date: null,
			search_id:null,
			current_id:null,
			contract_service_edit_class: ".edit-contract-services-popup-link",
			contract_service_item_field: "#edit-contract-service",
			contract_service_table: "#edit-contract-service-table",
			contract_service_id_field: "input[name='edit-contract-service-id']",
			contract_service_parent_id_field:"input[name='edit-contract-service-parent-id']",
			contract_device_unit_count_field:"input[name='edit-contract-service-device-unit-count']",
			contract_one_time_cost_field: "input[name='edit-contract-service-one-time-cost']",
			contract_recurring_cost_field: "input[name='edit-contract-service-recurring-cost']",
			contract_ci_name_field: "input[name='edit-contract-service-ci-name']",
			contract_start_date_field: "input[name='edit-contract-service-start-date']",
			contract_end_date_field: "input[name='edit-contract-service-end-date']",
			contract_notes_field: "textarea[name='edit-contract-service-notes']",
			contract_group_field:"select[name=\"edit-contract-service-group\"]",
			contract_status_field:"select[name=\"edit-contract-service-status\"]",
			contract_location_field:"select[name=\"edit-contract-service-location\"]",
			contract_azure_id_field: "input[name='edit-contract-service-azure-id']",
			contract_part_number_field: "#edit-contract-service-part-number",
			contract_part_description_field: "#edit-contract-service-part-description",
			contract_pcr_field:"#edit-contract-services-pcr",
			contract_no_pcr:"#edit-contract-services-no-pcrs",
			delete_row_class: "edit-contract-delete",
			undo_delete_row_class: "edit-contract-delete-undo",
			delete_row_tr_class:".delete-row",
			related_row_tr_class:".related-row",
			edit_row_tr_class:".edit-row",
			pcr_row_class:"pcr-row",
			azure_row_class:"azure-row",
			related_row_class:"related-row",
			server_stat_row_class:"server-stat-row",
			add_related_line_item_btn:".add-related-line-item-btn",
			related_lineitems_table:".edit-related-devices",
			date_match_related_line_item_btn:".related-lineitems-match-date-btn",
			remove_related_line_item_btn:".related-line-item-remove-btn",
			delete_related_line_item_btn:".related-line-item-delete-btn",
			unmap_related_line_item_btn:".related-line-item-unmap-btn",
			undo_unmap_related_line_item_btn:".undo-related-line-item-unmap-btn",
			undo_delete_related_line_item_btn:".undo-related-line-item-delete-btn",
			delete_child_row_class: "related-line-item-delete-btn",
			undo_delete_child_row_class: "undo-related-line-item-delete-btn",
			undo_unmap_child_row_class: "undo-related-line-item-unmap-btn",
			unmap_child_row_class: "related-line-item-unmap-btn",
			add_related_line_item_device_field:".add-related-lineitem-device-select",
			related_lineitem_id:"input[name='child-id']",
			related_lineitem_start_date:"input[name='child-start-date']",
			related_lineitem_end_date:"input[name='child-end-date']",
			edit_contract_field_class: ".edit-contract-field",
			edit_contract_display_class: ".edit-contract-deleted-value",
			contract_update_success_msg: "#update-contract-service-msg",
			part_identifier_required_msg: "#device-identifer-required-msg",
			edit_contract_delete_all: "#edit-contract-delete-all",
			delete_msg: "#delete-msg",
			delete_all_msg: "#delete-all-msg",
			undo_msg: "#undo-msg",
			undo_delete_all_msg: "#undo-delete-all-msg",
			required_class:".required-ind",
			locked_display_value_class:".locked-display-value",
			cached_services:null,
			load_dialog:function() {
				CONTRACT.contract_services.edit.get_services();
			},
			init:function() {
				CONTRACT.contract_services.edit.bind_events();
			},
			bind_events:function() {
				$(document).on("click", CONTRACT.contract_services.edit.add_related_line_item_btn, function() {
					var device_id = $(this).data("device-id");
					var row = CONTRACT.contract_services.edit.build_new_related_line_item_row(device_id);
					$(this).siblings("table").find("tbody").first().append(row);
					return false;
				});
				
				$(document).on("click", CONTRACT.contract_services.edit.remove_related_line_item_btn, function() {
					$(this).parent().parent().remove(); //the row
					return false;
				});
				
				$(document).on("click", CONTRACT.contract_services.edit.delete_related_line_item_btn, function() {
					$link = $(this);
					CONTRACT.contract_services.edit.delete_child_row($link);
					return false;
				});
				
				$(document).on("click", CONTRACT.contract_services.edit.undo_delete_related_line_item_btn, function() {
					$link = $(this);
					CONTRACT.contract_services.edit.undo_delete_child_row($link);
					return false;
				});
				
				$(document).on("click", CONTRACT.contract_services.edit.unmap_related_line_item_btn, function() {
					$link = $(this);
					CONTRACT.contract_services.edit.unmap_row($link);
					return false;
				});
				
				$(document).on("click", CONTRACT.contract_services.edit.undo_unmap_related_line_item_btn, function() {
					$link = $(this);
					CONTRACT.contract_services.edit.undo_unmap_row($link);
					return false;
				});
				
				$(document).on("change", CONTRACT.contract_services.edit.add_related_line_item_device_field, function() {
					var device_id = $(this).val();
					var require_unit_count = CONTRACT.contract_services.get_device_require_unit_count(device_id);
					
					var $row = $(this).parent().parent();
					if(require_unit_count) {
						$row.find(CONTRACT.contract_services.add.related_lineitem_unit_count_field).prop("disabled", false).addClass("unit-count-calculator-field");
						$row.find(CONTRACT.contract_services.add.related_lineitem_unit_count_field).siblings(".required-ind").addClass("required");
						$row.find(CONTRACT.contract_services.add.unit_price_calculator_btn).show();
						$row.find(CONTRACT.contract_services.add.unit_price_view_class).show();
						$row.find(CONTRACT.contract_services.add.total_price_view_class).hide();
					} else {
						$row.find(CONTRACT.contract_services.add.related_lineitem_unit_count_field).val("").prop("disabled", true).removeClass("unit-count-calculator-field");
						$row.find(CONTRACT.contract_services.add.related_lineitem_unit_count_field).siblings(".required-ind").removeClass("required");
						$row.find(CONTRACT.contract_services.add.total_price_view_class).show();
						$row.find(CONTRACT.contract_services.add.unit_price_view_class).hide();
						$row.find(CONTRACT.contract_services.add.unit_price_calculator_btn).data("view", "unit-cost").hide();
						$row.find(CONTRACT.contract_services.add.unit_price_view_class).hide();
						$row.find(CONTRACT.contract_services.add.total_price_view_class).show();
					}
					
					//grandchild devices
					CONTRACT.contract_services.edit.manage_new_grandchild_lineitem_section($(this), device_id);
				});
				
				$(document).on("click", CONTRACT.contract_services.edit.date_match_related_line_item_btn, function() {
					var $link = $(this);
					var $related_row = $link.parents(CONTRACT.contract_services.edit.related_row_tr_class);
					var row_count = $link.data("row");
					var $edit_row = $related_row.siblings(CONTRACT.contract_services.edit.edit_row_tr_class + ".row-" + row_count);
					
					var date_type = $link.data("date");
					if(date_type == "start") {
						var start_date = $edit_row.find(CONTRACT.contract_services.edit.contract_start_date_field).val();
						$related_row.find(CONTRACT.contract_services.edit.related_lineitem_start_date).val(start_date);
					} else {
						var end_date = $edit_row.find(CONTRACT.contract_services.edit.contract_end_date_field).val();
						$related_row.find(CONTRACT.contract_services.edit.related_lineitem_end_date).val(end_date);
					}
				});

			},
			get_services:function(cb) {
				var contract_id = CONTRACT.contract_id;
				var service_id = CONTRACT.contract_services.edit.current_service_id;
				var device_id = CONTRACT.contract_services.edit.current_device_id;
				var current_id = CONTRACT.contract_services.edit.current_id;
				var start_date = UTIL.convert_dates_for_server(CONTRACT.contract_services.edit.current_start_date);
				var end_date = UTIL.convert_dates_for_server(CONTRACT.contract_services.edit.current_end_date);
				var device_url = "";
				if(device_id != null || device_id == "null") device_url = "&did=" + device_id;
				var status = "&sts=active";
				if(CONTRACT.contract_services.current_view == CONTRACT.contract_services.display_views.pending) {
					status = "&sts=pending";
				} else if(CONTRACT.contract_services.current_view == CONTRACT.contract_services.display_views.do_not_bill) {
					status = "&sts=donotbill";
				}
				
				UTIL.add_table_loader(CONTRACT.contract_services.edit.contract_service_table);
				
				var url = PAGE_CONSTANTS.BASE_URL + "contractservices.json?cid=" + contract_id + "&sid=" + service_id + "&sd=" + start_date + "&ed=" + end_date + device_url + status;
				
				if(current_id != null && current_id != "null" && current_id) {
					url = PAGE_CONSTANTS.BASE_URL + "contractservices/" + current_id + ".json";
				}
				
				$.ajax ({
					url: url,
					type: "GET",
					success:function(data) {
						if(current_id != null && current_id != "null" && current_id) {
							var results = [];
							results.push(data);
							data = results;
						}
						UTIL.remove_dialog_loader(CONTRACT.edit_contract_service_dialog);
						CONTRACT.contract_services.edit.populate_dialog(data);
						CONTRACT.contract_services.edit.cached_services = data;
						
						if(CONTRACT.contract_services.edit.search_id) {
							CONTRACT.search.highligh_row();
						}
					}
				});
			},
			populate_dialog:function(contract_services) {
				UTIL.clear_message_in_popup(CONTRACT.edit_contract_service_dialog);
				var $dialog = $(CONTRACT.edit_contract_service_dialog);
				
				$(CONTRACT.contract_services.edit.edit_contract_delete_all).html("<i class=\"fa fa-minus-circle\"></i>" + $(CONTRACT.contract_services.edit.delete_all_msg).val()).data("action","delete").show();
				
				$(CONTRACT.contract_services.edit.contract_service_item_field).prop("disabled", false);
				var serviceIdVal = $(CONTRACT.contract_services.edit.contract_service_item_field + " .osp-id-" + contract_services[0].ospId).val();
				$(CONTRACT.contract_services.edit.contract_service_item_field).val(serviceIdVal);
				
				$(CONTRACT.contract_services.edit.contract_service_item_field).siblings(CONTRACT.contract_services.edit.locked_display_value_class).html(CONTRACT.contract_services.edit.current_service_name);
				CONTRACT.contract_services.edit.build_edit_service_table(contract_services);
				$(CONTRACT.contract_services.edit.contract_pcr_field).val("#");
				if($(CONTRACT.contract_services.edit.contract_pcr_field + " option").length > 1) {
					$(CONTRACT.contract_services.edit.contract_pcr_field).show();
					$(CONTRACT.contract_services.edit.contract_no_pcr).hide();
				} else {
					$(CONTRACT.contract_services.edit.contract_pcr_field).hide();
					$(CONTRACT.contract_services.edit.contract_no_pcr).show();
				}
				
				var notes = contract_services[0].note;
				var device_id = contract_services[0].deviceId;
				var part_number = contract_services[0].devicePartNumber;
				var part_description = contract_services[0].deviceDescription;
				$(CONTRACT.contract_services.edit.contract_notes_field).val(notes);
				$(CONTRACT.contract_services.edit.contract_part_number_field).val(part_number);
				$(CONTRACT.contract_services.edit.contract_part_number_field).siblings(CONTRACT.contract_services.edit.locked_display_value_class).html(part_number);
				$(CONTRACT.contract_services.edit.contract_part_description_field).val(part_description);
				$(CONTRACT.contract_services.edit.contract_part_description_field).siblings(CONTRACT.contract_services.edit.locked_display_value_class).html(part_description);
				
				//check the part number for a default ID and change it to that service
				var default_osp_id = CONTRACT.contract_services.get_device_default_osp_id(contract_services[0].deviceId);
				if(default_osp_id) {
					default_osp_id = $(CONTRACT.contract_services.edit.contract_service_item_field + " .osp-id-" + default_osp_id).val();
					$(CONTRACT.contract_services.edit.contract_service_item_field).val(default_osp_id);
					
					if($(CONTRACT.contract_services.edit.contract_service_item_field).val()){
						$(CONTRACT.contract_services.edit.contract_service_item_field).prop("disabled", true);
					}
				}
				
				$(CONTRACT.contract_services.edit.contract_part_number_field).parents(".field").find(".field-symbol").hide();
				$(CONTRACT.contract_services.edit.contract_part_description_field).parents(".field").find(".field-symbol").hide();
				
				var has_children = CONTRACT.contract_services.get_device_has_children(device_id);
				if(has_children) {
					$(CONTRACT.contract_services.edit.contract_part_number_field).prop("disabled", true);
					$(CONTRACT.contract_services.edit.contract_part_description_field).prop("disabled", true);
				} else {
					$(CONTRACT.contract_services.edit.contract_part_number_field).prop("disabled", false);
					$(CONTRACT.contract_services.edit.contract_part_description_field).prop("disabled", false);
				}
				
				CONTRACT.contract_services.edit.toggle_dialog_lock($dialog);
			},
			toggle_dialog_lock:function($dialog) {
				if(CONTRACT.contract_invoice.current_status == CONTRACT.contract_invoice.statuses.invoiced) {
					$dialog.find(CONTRACT.contract_services.edit.edit_contract_field_class).hide();
					$dialog.find(CONTRACT.contract_services.edit.required_class).hide();
					$dialog.find(CONTRACT.contract_services.edit.edit_contract_display_class).css("display","inline-block").css("text-decoration","none");
					$dialog.find("." + CONTRACT.contract_services.edit.delete_row_class).hide();
					$dialog.find(CONTRACT.contract_services.edit.edit_contract_delete_all).hide();
					$dialog.find(CONTRACT.contract_services.edit.delete_related_line_item_btn).hide();
					$dialog.find(CONTRACT.contract_services.edit.add_related_line_item_btn).hide();
					
					$dialog.find(CONTRACT.contract_services.edit.delete_row_tr_class).each(function() {
						var $this = $(this);
						if($this.find("textarea").html() == "") $this.hide();
					});
					
					if($(CONTRACT.contract_services.edit.contract_pcr_field + " option").length > 1) {
						$(CONTRACT.contract_services.edit.contract_pcr_field).hide();
					}
					
					$dialog.find(CONTRACT.contract_services.edit.locked_display_value_class).show();
					
					$dialog.siblings(".ui-dialog-buttonpane").find(".notice-msg").remove();
					var msg = "<div class=\"notice-msg\"><i class=\"fa fa-lock\"></i> This month has already been invoiced, so you can not change records in here. You must go to another month to edit the records.</div>";
					$dialog.siblings(".ui-dialog-buttonpane").prepend(msg);
					$dialog.closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").hide();
					$dialog.closest(".ui-dialog").find(".ui-button:contains('OK')").show();
				} else {
					$dialog.find(CONTRACT.contract_services.edit.edit_contract_field_class).show();
					$dialog.find(CONTRACT.contract_services.edit.required_class).show();
					$dialog.find(CONTRACT.contract_services.edit.edit_contract_display_class).css("display","none").css("text-decoration","");
					$dialog.find("." + CONTRACT.contract_services.edit.delete_row_class).show();
					$dialog.find(CONTRACT.contract_services.edit.edit_contract_delete_all).show();
					$dialog.find(CONTRACT.contract_services.edit.delete_row_tr_class).show();
					$dialog.find(CONTRACT.contract_services.edit.locked_display_value_class).hide();
					$dialog.find(CONTRACT.contract_services.edit.delete_related_line_item_btn).show();
					$dialog.find(CONTRACT.contract_services.edit.add_related_line_item_btn).show();
					
					$dialog.siblings(".ui-dialog-buttonpane").find(".notice-msg").remove();
				}
			},
			build_edit_service_table:function(contract_services) {
				var output = "";
				for(var i=0; i<contract_services.length; i++) {
					var row_class="";
					var obj = contract_services[i];
					var start_date = UTIL.convert_dates_for_ui(obj.startDate);
					var end_date = UTIL.convert_dates_for_ui(obj.endDate);
					var ci_name = obj.deviceName;
					var parent_id = obj.parentId;
					if(!parent_id) parent_id = "";
					var one_time_cost = UTIL.convert_currency_for_server(obj.onetimeRevenue);
					var recurring_cost = UTIL.convert_currency_for_server(obj.recurringRevenue);
					var notes = obj.note;
					var contract_group_id = obj.contractGroupId;
					var device_unit_count = obj.deviceUnitCount;
					var contract_service_subscription_id = obj.contractServiceSubscriptionId;
					var location_id = obj.locationId;
					var device_id = obj.deviceId;
					if(notes == null) notes = "";
					if(ci_name == null) ci_name = "";
					if(!device_unit_count) device_unit_count = "";
					if(contract_service_subscription_id == null) contract_service_subscription_id = "";
					var subscription_type = obj.subscriptionType;
					
					one_time_cost = one_time_cost.replace("$","");
					recurring_cost = recurring_cost.replace("$","");
					
					if(i % 2 != 0) row_class="odd";
					
					var $group_options = $(CONTRACT.contract_groups.contract_group_clone_field + " option");
					var group_select = "<select name=\"edit-contract-service-group\" class=\"edit-contract-field\">";
					var group_select_text = "";
					$group_options.each(function() {
						var selected = "";
						var val = $(this).val();
						var text = $(this).text();
						
						if(val == contract_group_id) {
							selected = " selected=\"selected\"";
							group_select_text = text;
						}
						
						group_select += "<option value=\"" + val + "\"" + selected + ">" + text + "</option>";
					});
					group_select += "</select>";
					
					var status_select = "<select name=\"edit-contract-service-status\" class=\"edit-contract-field\">";
					var status_select_text = "";
					var status_selected = " selected=\"selected\"";
					status_select += "<option value=\"active\""; 
					if(obj.status == "active") {
						status_select_text = obj.status;
						status_select += status_selected;
					}
					status_select += ">Active</option>";
					status_select += "<option value=\"pending\"";				
					if(obj.status == "pending") {
						status_select_text = obj.status;
						status_select += status_selected;
					}
					status_select += ">Pending</option>";
					status_select += "<option value=\"donotbill\"";	
					if(obj.status == "donotbill") {
						status_select_text = obj.status;
						status_select += status_selected;
					}
					status_select += ">Do Not Bill</option>";
					status_select += "</select>";
					
					var $location_options = $(CONTRACT.contract_services.add.contract_location_field + " option");
					var location_select = "<select name=\"edit-contract-service-location\" class=\"edit-contract-field\">";
					var location_select_text = "";
					$location_options.each(function() {
						var selected = "";
						var val = $(this).val();
						var text = $(this).text();
						
						if(val == location_id) {
							selected = " selected=\"selected\"";
							location_select_text = text;
						}
						
						location_select += "<option value=\"" + val + "\"" + selected + ">" + text + "</option>";
					});
					location_select += "</select>";
					
					var require_unit_count = CONTRACT.contract_services.get_device_require_unit_count(contract_services[0].deviceId);
					var unit_count_required_class = "required-ind";
					var unit_count_disabled = " disabled=\"disabled\" ";
					if(require_unit_count) {
						unit_count_required_class = "required-ind required";
						unit_count_disabled = "";
					}
					
					var row_count = (i + 1);
					
					var disabled = "";
					if(contract_service_subscription_id) disabled = " disabled=\"disabled\"";
					
					output += "<tr data-operation=\"update\" class=\"edit-row row-" + row_count + " " + row_class + "\">";
					output += "<td class=\"row-count\">" + row_count + "<input type=\"hidden\" name=\"edit-contract-service-id\" value=\"" + obj.id + "\" /><input type=\"hidden\" name=\"edit-contract-service-azure-id\" value=\"" + contract_service_subscription_id + "\" /><input type=\"hidden\" name=\"edit-contract-service-parent-id\" value=\"" + parent_id + "\" /></td>";
					output += "<td><span class=\"field-symbol\"></span><span class=\"required-ind\"></span><input type=\"text\" name=\"edit-contract-service-ci-name\" class=\"edit-contract-field\" placeholder=\"(ex. arc-tlc-td01)\" size=\"12\" value=\"" + ci_name + "\" /><div class=\"edit-contract-deleted-value\">" + ci_name + "</div></td>";
					output += "<td><span class=\"field-symbol\"></span><span class=\"required-ind required\"></span><input type=\"text\" name=\"edit-contract-service-start-date\" class=\"edit-contract-field datepicker\" placeholder=\"mm/dd/yyyy\" size=\"10\" value=\"" + start_date + "\" " + disabled + "/><div class=\"edit-contract-deleted-value\">" + start_date + "</div></td>";
					output += "<td><span class=\"field-symbol\"></span><span class=\"required-ind required\"></span><input type=\"text\" name=\"edit-contract-service-end-date\" class=\"edit-contract-field datepicker\" placeholder=\"mm/dd/yyyy\" value=\"" + end_date + "\" size=\"10\" " + disabled + "/><div class=\"edit-contract-deleted-value\">" + end_date + "</div></td>";
					output += "<td><span class=\"field-symbol\"></span><span class=\"" + unit_count_required_class + "\"></span><input type=\"text\" name=\"edit-contract-service-device-unit-count\" class=\"edit-contract-field\" value=\"" + device_unit_count + "\" size=\"5\" " + unit_count_disabled + " /><div class=\"edit-contract-deleted-value\">" + device_unit_count + "</div></td>";
					output += "<td><span class=\"field-symbol\">$</span><span class=\"required-ind required\"></span><input type=\"text\" name=\"edit-contract-service-one-time-cost\" class=\"edit-contract-field currency-negative\" placeholder=\"(ex. 150.00)\" size=\"9\" value=\"" + one_time_cost + "\" " + disabled + "/><div class=\"edit-contract-deleted-value\">" + one_time_cost + "</div></td>";
					output += "<td><span class=\"field-symbol\">$</span><span class=\"required-ind required\"></span><input type=\"text\" name=\"edit-contract-service-recurring-cost\" class=\"edit-contract-field currency-negative\" placeholder=\"(ex. 265.39)\" size=\"9\" value=\"" + recurring_cost + "\" " + disabled + "/><div class=\"edit-contract-deleted-value\">" + recurring_cost + "</div></td>";
					output += "<td><a href=\"javascript:;\" class=\"edit-contract-delete\"><i class=\"fa fa-minus-circle\"></i>" + $(CONTRACT.contract_services.edit.delete_msg).val() + "</a></td>";
					output += "</tr>";
					
					output += "<tr class=\"delete-row " + row_class + "\">";
					output += "<td>&nbsp;</td>";
					output += "<td colspan=\"6\"><textarea class=\"edit-contract-field\" placeholder=\"Enter notes here...\" maxlength=\"500\">" + notes + "</textarea><div class=\"edit-contract-deleted-value\">" + notes + "</div></td>";
					output += "<td>&nbsp;</td>";
					output += "</tr>";
					
					output += "<tr class=\"pcr-row " + row_class + "\">";
					output += "<td>&nbsp;</td>";
					output += "<td colspan=\"2\"><span class=\"field-symbol\"></span><span class=\"required-ind\"></span><label>Group</label>" + group_select + "<span class=\"edit-contract-deleted-value\">" + group_select_text + "</span></td>";
					output += "<td colspan=\"2\"><span class=\"field-symbol\"></span><span class=\"required-ind\"></span><label>Status</label>" + status_select + "<span class=\"edit-contract-deleted-value\">" + status_select_text + "</span></td>";
					output += "<td colspan=\"2\"><span class=\"field-symbol\"></span><span class=\"required-ind\"></span><label>Location</label>" + location_select + "<span class=\"edit-contract-deleted-value\">" + location_select_text + "</span></td>";
					output += "<td>&nbsp;</td>";
					output += "</tr>";
					
					var related_lineitems = obj.relatedLineItems;
					related_lineitems = CONTRACT.contract_services.edit.filter_related_lineitems(related_lineitems);
					var related_devices = null;
					
					var devices = $.grep(CONTRACT.contract_services.devices, function(n) { return n.id == device_id; });
					if(devices.length > 0) {
						var device = devices[0];
						
						//device = CONTRACT.contract_services.filter_embedded_device(device);
						var related = device.relatedDevices;
						if(related && related.length > 0) {
							related_devices = related;
						}
					}
					
					var device = CONTRACT.contract_services.get_device(device_id);
					if(device != null && device.deviceType == "server") {
						output += "<tr class=\"server-stat-row " + row_class + "\">";
						output += "<td>&nbsp;</td>";
						output += "<td colspan=\"7\">";
						output += CONTRACT.contract_services.edit.build_server_stats(device, related_lineitems);
						output += "</td>";
						output += "</tr>";
					}
					
					if((related_lineitems && related_lineitems.length > 0) || (related_devices && related_devices.length > 0)) {
						output += "<tr class=\"related-row " + row_class + "\">";
						output += "<td>&nbsp;</td>";
						output += "<td colspan=\"7\">";
						output += CONTRACT.contract_services.edit.build_related_edit_lineitems(related_lineitems, device_id, row_count);
						output += "</td>";
						output += "</tr>";
					}
					
					if(contract_service_subscription_id) {
						var sub_link_text = "<i class=\"fab fa-windows\"></i>Get Azure Details";
						if(subscription_type == "aws") sub_link_text = "<i class=\"fab fa-aws\"></i>Get AWS Details";
						output += "<tr class=\"azure-row " + row_class + "\" id=\"azure-row-" + contract_service_subscription_id + "\">";
						output += "<td>&nbsp;</td>";
						output += "<td colspan=\"6\"><a href=\"javascript:;\" class=\"azure-lineitems\" data-azure-id=\"" + contract_service_subscription_id + "\" data-start-date=\"" + start_date + "\" data-end-date=\"" + end_date + "\" data-type=\"" + subscription_type + "\">" + sub_link_text + "</a><div class=\"azure-lineitems-container nested-table\" style=\"display:none;\"><table><thead><th>LineItem</th><th class=\"right\">Billing Period Cost</th></thead><tbody></tbody></table></div></td>";
						output += "<td>&nbsp;</td>";
						output += "</tr>";
					}
				}
				$(CONTRACT.contract_services.edit.contract_service_table).find("tbody").html(output);
			},
			build_server_stats:function(device, related_lineitems) {
				var output = "";
				var os = "Unknown";
				var compute = 0;
				var memory = 0;
				var storage = 0;
				var backup_storage = 0;
				var vaulting_storage = 0;
				
				if(related_lineitems && related_lineitems.length > 0) {
					for(var j = 0; j < related_lineitems.length; j++) {
						var related_lineitem = related_lineitems[j];
						var device_type = CONTRACT.contract_services.get_device_type(related_lineitem.deviceId);
						
						var unit_count = related_lineitem.deviceUnitCount;
						if(device_type == "compute"){
							compute += unit_count;
						} else if(device_type == "memory") {
							memory += unit_count;
						} else if(device_type == "storage") {
							storage += unit_count;
						} else if(device_type == "backupStorage") {
							backup_storage += unit_count;
						} else if(device_type == "vaultingStorage") {
							vaulting_storage += unit_count;
						}
						
						var grandchild_lineitems = related_lineitem.relatedLineItems;
						if(grandchild_lineitems && grandchild_lineitems.length > 0) {
							for(var k = 0; k < grandchild_lineitems.length; k++) {
								var grandchild_lineitem = grandchild_lineitems[k];
								var gdevice_type = CONTRACT.contract_services.get_device_type(grandchild_lineitem.deviceId);
								
								var gunit_count = grandchild_lineitem.deviceUnitCount;
								if(gdevice_type == "compute"){
									compute += gunit_count;
								} else if(gdevice_type == "memory") {
									memory += gunit_count;
								} else if(gdevice_type == "storage") {
									storage += gunit_count;
								} else if(gdevice_type == "backupStorage") {
									backup_storage += gunit_count;
								} else if(gdevice_type == "vaultingStorage") {
									vaulting_storage += gunit_count;
								}
							}
						}
					}
				}
				
				var embedded_devices = $.grep(device.relatedDevices, function(n) { return n.relationship == "embedded"; });
				if(embedded_devices && embedded_devices.length > 0) {
					for(var j = 0; j < embedded_devices.length; j++) {
						var embedded_device = embedded_devices[j];
						var type = embedded_device.deviceType;
						
						var unit_count = embedded_device.specUnits;
						if(type == "compute"){
							compute += unit_count;
						} else if(type == "memory") {
							memory += unit_count;
						} else if(type == "storage") {
							storage += unit_count;
						} else if(type == "os") {
							os = property.strValue;
						} else if(type == "backupStorage") {
							backup_storage += unit_count;
						} else if(type == "vaultingStorage") {
							vaulting_storage += unit_count;
						}
					}
				}
				
				var properties = device.properties;
				if(properties && properties.length > 0) {
					for(var j = 0; j < properties.length; j++) {
						var property = properties[j];
						var type = property.type
						
						if(type == "os") {
							os = property.strValue;
						}
						/*
						var unit_count = property.unitCount;
						if(type == "compute"){
							compute += unit_count;
						} else if(type == "memory") {
							memory += unit_count;
						} else if(type == "storage") {
							storage += unit_count;
						} else  else if(type == "backupStorage") {
							backup_storage += unit_count;
						} else if(type == "vaultingStorage") {
							vaulting_storage += unit_count;
						}
						*/
					}
				}
				
				output += "<section class=\"section-wrapper\" style=\"margin-bottom:-15px; border-top:1px solid #e8e8e8; padding-top:15px;\">";
				output += "<div class=\"section-title\" style=\"font-size:1.05em;\">";
				output += "<i class=\"fas fa-server\"></i>Server Stats";
				output += "</div>";
				output += "<div>";
				
				output += "<div class=\"column-4\">";
				output += "<strong>OS: </strong>";
				output += os;
				output += "</div>";
				
				output += "<div class=\"column-4\">";
				output += "<strong>Compute: </strong>";
				output += compute;
				output += "</div>";
				
				output += "<div class=\"column-4\">";
				output += "<strong>Memory: </strong>";
				output += memory + " GB";
				output += "</div>";
				
				output += "<div class=\"column-4\">";
				output += "<strong>Storage: </strong>";
				output += storage + " GB";
				output += "</div>";
				
				output += "</div>";
				
				output += "<div>";
				
				output += "<div class=\"column-4\">";
				output += "</div>";
				
				output += "<div class=\"column-4\">";
				output += "</div>";
				
				output += "<div class=\"column-4\">";
				output += "<strong>Backup Storage: </strong>";
				output += backup_storage + " GB";
				output += "</div>";
				
				output += "<div class=\"column-4\">";
				output += "<strong>Vaulting Storage: </strong>";
				output += vaulting_storage + " GB";
				output += "</div>";
				
				output += "</div>";
				output += "</section>";
				
       		  	return output;
			},
			filter_related_lineitems:function(lineitems) {
				var filtered_lineitems = [];
				for(var i = 0; i < lineitems.length; i++) {
					var lineitem = lineitems[i];
					var start_date = UTIL.convert_dates_for_ui(lineitem.startDate);
					var end_date = UTIL.convert_dates_for_ui(lineitem.endDate);
					var start_moment = moment(start_date, CONTRACT.date_selectors.moment_format);
					var end_moment = moment(end_date, CONTRACT.date_selectors.moment_format);
					
					if(CONTRACT.contract_services.edit.filter_lineitem_by_date(start_moment, end_moment, lineitem.deviceDescription)) {
						var filtered_grandchild_lineitems = [];
						var grandchild_lineitems = lineitem.relatedLineItems;
						for(var j = 0; j < grandchild_lineitems.length; j++) {
							var grandchild_lineitem = grandchild_lineitems[j];
							var grandchild_start_date = UTIL.convert_dates_for_ui(grandchild_lineitem.startDate);
							var grandchild_end_date = UTIL.convert_dates_for_ui(grandchild_lineitem.endDate);
							var grandchild_start_moment = moment(grandchild_start_date, CONTRACT.date_selectors.moment_format);
							var grandchild_end_moment = moment(grandchild_end_date, CONTRACT.date_selectors.moment_format);
							if(CONTRACT.contract_services.edit.filter_lineitem_by_date(grandchild_start_moment, grandchild_end_moment, grandchild_lineitem.deviceDescription)) {
								filtered_grandchild_lineitems.push(grandchild_lineitem);
							}
						}
						
						lineitem.relatedLineItems = filtered_grandchild_lineitems;
						
						filtered_lineitems.push(lineitem);
					}
				}
				
				return filtered_lineitems;
			},
			filter_lineitem_by_date:function(start_moment, end_moment, description) {
				var view_start_date = $(CONTRACT.date_selectors.current_viewing_start_date).val();
				var view_end_date = $(CONTRACT.date_selectors.current_viewing_end_date).val();
				var view_start_moment = moment(view_start_date, CONTRACT.date_selectors.moment_format);
				var view_end_moment = moment(view_end_date, CONTRACT.date_selectors.moment_format);
				
				if((start_moment.isBetween(view_start_moment, view_end_moment) || start_moment.isSame(view_start_moment) || (start_moment.isBefore(view_start_moment) && end_moment.isAfter(view_start_moment))) &&
						(end_moment.isBetween(view_start_moment, view_end_moment) || end_moment.isSame(view_end_moment) || (end_moment.isAfter(view_end_moment))) || end_moment.isSame(view_start_moment)) {
					return true;
				}
				return false;
			},
			build_related_edit_lineitems:function(lineitems, parent_device_id, row_count) {
				var output = "";
				output += "<div class=\"edit-related-devices-container\" style=\"font-size:0.8em;\">";
				output += "<div class=\"form-divider\"></div>";
				output += "<div>";
				output += "<section class=\"table-wrapper\">";
				output += "<div class=\"table-header\">";
			    output += "<div class=\"table-title\">";
			    output += "<i class=\"fas fa-link\"></i>Related Line Items";
			    output += "</div>";
			    output += "</div>";
       		  	output += "<table class=\"edit-related-devices\">";
       		    output += "<thead>";
       		    output += "<tr>";
       		    output += "<th>Device</th>";
       		    output += "<th>Start Date (<a href=\"javascript:;\" class=\"related-lineitems-match-date-btn\" data-date=\"start\" data-row=\"" + row_count + "\" style=\"font-size:0.9em;\">Match to Parent</a>)</th>";
       		 	output += "<th>End Date (<a href=\"javascript:;\" class=\"related-lineitems-match-date-btn\" data-date=\"end\" data-row=\"" + row_count + "\" style=\"font-size:0.9em;\">Match to Parent</a>)</th>";
       		    output += "<th>Unit Count</th>";
       		    output += "<th>&nbsp;</th>";
       		    output += "<th>NRC</th>";
       		    output += "<th>MRC</th>";
       		    output += "<th></th>";
       		    output += "</tr>";
       		    output += "</thead>";
       		    output += "<tbody>";
				
       		    if(lineitems && lineitems.length > 0) {
					for(var i = 0; i < lineitems.length; i++) {
						var lineitem = lineitems[i];
						
						var start_date = UTIL.convert_dates_for_ui(lineitem.startDate);
						var end_date = UTIL.convert_dates_for_ui(lineitem.endDate);
						var onetime_cost = UTIL.convert_currency_for_server(lineitem.onetimeRevenue);
						var recurring_cost = UTIL.convert_currency_for_server(lineitem.recurringRevenue);
						var device_id = lineitem.deviceId;
						var device_description = lineitem.deviceDescription;
						var device_part_number = lineitem.devicePartNumber;
						var device_unit_count = lineitem.deviceUnitCount;
						var nrc_unit_cost = 0;
						var mrc_unit_cost = 0;
						
						if(device_unit_count) {
							nrc_unit_cost = onetime_cost / device_unit_count;
							mrc_unit_cost = recurring_cost / device_unit_count;
						}
						
						var row_class = "";
						var requires = CONTRACT.contract_services.get_device_require_unit_count(lineitem.deviceId);
						if(i % 2 == 0) {
							row_class = " class=\"odd\"";
						}
						var checked = "";
						var unit_displayed = " style=\"display:none;\"";
						var total_displayed = " style=\"display:none;\"";
						if(requires) {
							checked = " checked=\"checked\" disabled=\"disabled\"";
							unit_displayed = " style=\"display:inline-block;\"";
						} else {
							total_displayed = " style=\"display:inline-block;\"";
						}
						
						output += "<tr data-operation=\"update\" " + row_class + ">";
						output += "<td>" + device_description + "<div style=\"font-size:0.8em;\">" + device_part_number + "</span>";
						output += "<input type=\"hidden\" name=\"child-id\" value=\"" + lineitem.id + "\" />";
						output += "<input type=\"hidden\" name=\"child-device-id\" value=\"" + device_id + "\" />";
						output += "<input type=\"hidden\" name=\"child-device-part-number\" value=\"" + device_part_number + "\" />";
						output += "<input type=\"hidden\" name=\"child-device-description\" value=\"" + device_description + "\" />";
						output += "<input type=\"hidden\" name=\"child-service-id\" value=\"" + lineitem.serviceId + "\" />";
						output += "<input type=\"hidden\" name=\"child-default-osp-id\" value=\"" + lineitem.ospId + "\" />";
						output += "</td>";
						output += "<td><span class=\"field-symbol\"></span><span class=\"required-ind required\"></span><input type=\"text\" class=\"edit-contract-field datepicker\" name=\"child-start-date\" placeholder=\"mm/dd/yyyy\" size=\"10\" value=\"" + start_date + "\" /><div class=\"edit-contract-deleted-value\">" + start_date + "</div></td>";
						output += "<td><span class=\"field-symbol\"></span><span class=\"required-ind required\"></span><input type=\"text\" class=\"edit-contract-field datepicker\" name=\"child-end-date\" placeholder=\"mm/dd/yyyy\" size=\"10\" value=\"" + end_date + "\" /><div class=\"edit-contract-deleted-value\">" + end_date + "</div></td>";
						if(requires) {
							output += "<td><span class=\"field-symbol\"></span><span class=\"required-ind required\"></span><input type=\"text\" class=\"edit-contract-field unit-count-calculator-field\" size=\"3\" name=\"child-unit-count\" value=\"" + device_unit_count + "\" /><div class=\"edit-contract-deleted-value\">" + device_unit_count + "</div></td>";
							output += "<td><a href=\"javascript:;\" data-view=\"unit-cost\" alt=\"Click to toggle between unit cost and total cost.\" title=\"Click to toggle between unit cost and total cost.\" class=\"uc-calculator-btn\"><i class=\"fa fa-calculator\"></i>Total</a></td>";
						} else {
							output += "<td><span class=\"field-symbol\"></span><span class=\"required-ind\"></span><input type=\"text\" size=\"3\" class=\"edit-contract-field\" disabled=\"disabled\" name=\"child-unit-count\" class=\"keep-disabled\" /></td>";
							output += "<td>&nbsp;</td>";
						}
						
						output += "<td>";
						output += "<div class=\"total-price-view\"" + total_displayed + "><span class=\"field-symbol\">$</span><span class=\"required-ind required\"></span><input type=\"text\" size=\"8\" class=\"edit-contract-field currency\" name=\"child-onetime-cost\"" + " value=\"" + onetime_cost + "\" /><div class=\"edit-contract-deleted-value\">" + onetime_cost + "</div></div>";
						output += "<div class=\"unit-price-view\"" + unit_displayed + "><label>Unit Price</label><div><span class=\"field-symbol\">$</span><span class=\"required-ind required\"></span><input type=\"text\" size=\"8\" class=\"currency edit-contract-field unit-cost-calculator-field\" name=\"child-onetime-unit-cost\" value=\"" + UTIL.convert_currency_for_server(nrc_unit_cost) + "\" /><div class=\"edit-contract-deleted-value\">" + UTIL.convert_currency_for_server(nrc_unit_cost) + "</div></div><div class=\"unit-price-total-row\">Total: <span class=\"unit-cost-total\">" + UTIL.convert_currency_for_ui(onetime_cost) + "</span></div></div>";
						output += "</td>";
						output += "<td>";
						output += "<div class=\"total-price-view\"" + total_displayed + "><span class=\"field-symbol\">$</span><span class=\"required-ind required\"></span><input type=\"text\" size=\"8\" class=\"edit-contract-field currency\" name=\"child-recurring-cost\"" + " value=\"" + recurring_cost + "\" /><div class=\"edit-contract-deleted-value\">" + recurring_cost + "</div></div>";
						output += "<div class=\"unit-price-view\"" + unit_displayed + "><label>Unit Price</label><div><span class=\"field-symbol\">$</span><span class=\"required-ind required\"></span><input type=\"text\" size=\"8\" class=\"currency edit-contract-field unit-cost-calculator-field\" name=\"child-recurring-unit-cost\" value=\"" + UTIL.convert_currency_for_server(mrc_unit_cost) + "\" /><div class=\"edit-contract-deleted-value\">" + UTIL.convert_currency_for_server(mrc_unit_cost) + "</div></div><div class=\"unit-price-total-row\">Total: <span class=\"unit-cost-total\">" + UTIL.convert_currency_for_ui(recurring_cost) + "</span></div></div>";
						output += "</td>";
						output += "<td>";
						output += "<a href=\"javascript:;\" class=\"related-line-item-delete-btn\"><i class=\"fa fa-minus-circle\"></i>Delete</a>, ";
						output += "<a href=\"javascript:;\" class=\"related-line-item-unmap-btn\"><i class=\"fa fa-unlink\"></i>Un-Map</a>";
						output += "</td>";
						output += "</tr>";
						
						//granchild lineitems
						var related_lineitems = lineitem.relatedLineItems;
						if(related_lineitems != null && related_lineitems.length > 0) {
							output += "<tr class=\"child-row\"><td colspan=\"8\">";
							output += "<section class=\"table-wrapper\" style=\"padding-left:25px;\">";
							output += "<div class=\"table-header\">";
							output += "<div class=\"table-title\" style=\"font-size:0.9em;\"><i class=\"fas fa-link\"></i>Related Line Items</div>";
							output += "</div>";
							output += "<table class=\"grandchild-lineitems-table\">";
							output += "<thead><tr><th>Device</th><th>Start Date</th><th>End Date</th><th>Unit Count</th><th>&nbsp;</th><th>NRC</th><th>MRC</th><th>&nbsp;</th></tr></thead>";
							output += "<tbody>";
							
							for(var j= 0; j < related_lineitems.length; j++) {
								var related_lineitem = related_lineitems[j];
								
								var gstart_date = UTIL.convert_dates_for_ui(related_lineitem.startDate);
								var gend_date = UTIL.convert_dates_for_ui(related_lineitem.endDate);
								var gonetime_cost = UTIL.convert_currency_for_server(related_lineitem.onetimeRevenue);
								var grecurring_cost = UTIL.convert_currency_for_server(related_lineitem.recurringRevenue);
								var gdevice_id = related_lineitem.deviceId;
								var gdevice_description = related_lineitem.deviceDescription;
								var gdevice_part_number = related_lineitem.devicePartNumber;
								var gdevice_unit_count = related_lineitem.deviceUnitCount;
								var gnrc_unit_cost = 0;
								var gmrc_unit_cost = 0;
								
								if(gdevice_unit_count) {
									gnrc_unit_cost = gonetime_cost / gdevice_unit_count;
									gmrc_unit_cost = grecurring_cost / gdevice_unit_count;
								}
								
								var grequires = CONTRACT.contract_services.get_device_require_unit_count(related_lineitem.deviceId);
								
								var grow_class = "";
								if(j % 2 == 0) {
									grow_class = " class=\"odd\"";
								}
								var gchecked = "";
								var gunit_displayed = " style=\"display:none;\"";
								var gtotal_displayed = " style=\"display:none;\"";
								if(grequires) {
									gchecked = " checked=\"checked\" disabled=\"disabled\"";
									gunit_displayed = " style=\"display:inline-block;\"";
								} else {
									gtotal_displayed = " style=\"display:inline-block;\"";
								}
								
								output += "<tr data-operation=\"update\" " + grow_class + ">";
								output += "<td>" + gdevice_description + "<div style=\"font-size:0.8em;\">" + gdevice_part_number + "</span>";
								output += "<input type=\"hidden\" name=\"child-id\" value=\"" + related_lineitem.id + "\" />";
								output += "<input type=\"hidden\" name=\"child-device-id\" value=\"" + gdevice_id + "\" />";
								output += "<input type=\"hidden\" name=\"child-device-part-number\" value=\"" + gdevice_part_number + "\" />";
								output += "<input type=\"hidden\" name=\"child-device-description\" value=\"" + gdevice_description + "\" />";
								output += "<input type=\"hidden\" name=\"child-service-id\" value=\"" + related_lineitem.serviceId + "\" />";
								output += "<input type=\"hidden\" name=\"child-default-osp-id\" value=\"" + related_lineitem.ospId + "\" />";
								output += "</td>";
								output += "<td><span class=\"field-symbol\"></span><span class=\"required-ind required\"></span><input type=\"text\" class=\"edit-contract-field datepicker\" name=\"child-start-date\" placeholder=\"mm/dd/yyyy\" size=\"10\" value=\"" + gstart_date + "\" /><div class=\"edit-contract-deleted-value\">" + gstart_date + "</div></td>";
								output += "<td><span class=\"field-symbol\"></span><span class=\"required-ind required\"></span><input type=\"text\" class=\"edit-contract-field datepicker\" name=\"child-end-date\" placeholder=\"mm/dd/yyyy\" size=\"10\" value=\"" + gend_date + "\" /><div class=\"edit-contract-deleted-value\">" + gend_date + "</div></td>";
								if(grequires) {
									output += "<td><span class=\"field-symbol\"></span><span class=\"required-ind required\"></span><input type=\"text\" class=\"edit-contract-field unit-count-calculator-field\" size=\"3\" name=\"child-unit-count\" value=\"" + gdevice_unit_count + "\" /><div class=\"edit-contract-deleted-value\">" + gdevice_unit_count + "</div></td>";
									output += "<td><a href=\"javascript:;\" data-view=\"unit-cost\" alt=\"Click to toggle between unit cost and total cost.\" title=\"Click to toggle between unit cost and total cost.\" class=\"uc-calculator-btn\"><i class=\"fa fa-calculator\"></i>Total</a></td>";
								} else {
									output += "<td><span class=\"field-symbol\"></span><span class=\"required-ind\"></span><input type=\"text\" size=\"3\" class=\"edit-contract-field\" disabled=\"disabled\" name=\"child-unit-count\" class=\"keep-disabled\" /></td>";
									output += "<td>&nbsp;</td>";
								}
								
								output += "<td>";
								output += "<div class=\"total-price-view\"" + gtotal_displayed + "><span class=\"field-symbol\">$</span><span class=\"required-ind required\"></span><input type=\"text\" size=\"8\" class=\"edit-contract-field currency\" name=\"child-onetime-cost\"" + " value=\"" + gonetime_cost + "\" /><div class=\"edit-contract-deleted-value\">" + gonetime_cost + "</div></div>";
								output += "<div class=\"unit-price-view\"" + gunit_displayed + "><label>Unit Price</label><div><span class=\"field-symbol\">$</span><span class=\"required-ind required\"></span><input type=\"text\" size=\"8\" class=\"currency edit-contract-field unit-cost-calculator-field\" name=\"child-onetime-unit-cost\" value=\"" + UTIL.convert_currency_for_server(gnrc_unit_cost) + "\" /><div class=\"edit-contract-deleted-value\">" + UTIL.convert_currency_for_server(gnrc_unit_cost) + "</div></div><div class=\"unit-price-total-row\">Total: <span class=\"unit-cost-total\">" + UTIL.convert_currency_for_ui(gonetime_cost) + "</span></div></div>";
								output += "</td>";
								output += "<td>";
								output += "<div class=\"total-price-view\"" + gtotal_displayed + "><span class=\"field-symbol\">$</span><span class=\"required-ind required\"></span><input type=\"text\" size=\"8\" class=\"edit-contract-field currency\" name=\"child-recurring-cost\"" + " value=\"" + grecurring_cost + "\" /><div class=\"edit-contract-deleted-value\">" + grecurring_cost + "</div></div>";
								output += "<div class=\"unit-price-view\"" + gunit_displayed + "><label>Unit Price</label><div><span class=\"field-symbol\">$</span><span class=\"required-ind required\"></span><input type=\"text\" size=\"8\" class=\"currency edit-contract-field unit-cost-calculator-field\" name=\"child-recurring-unit-cost\" value=\"" + UTIL.convert_currency_for_server(gmrc_unit_cost) + "\" /><div class=\"edit-contract-deleted-value\">" + UTIL.convert_currency_for_server(gmrc_unit_cost) + "</div></div><div class=\"unit-price-total-row\">Total: <span class=\"unit-cost-total\">" + UTIL.convert_currency_for_ui(grecurring_cost) + "</span></div></div>";
								output += "</td>";
								output += "<td>";
								output += "<a href=\"javascript:;\" class=\"related-line-item-delete-btn\"><i class=\"fa fa-minus-circle\"></i>Delete</a>, ";
								output += "<a href=\"javascript:;\" class=\"related-line-item-unmap-btn\"><i class=\"fa fa-unlink\"></i>Un-Map</a>";
								output += "</td>";
								output += "</tr>";
							}
							
							output += "</tbody></table>";
							output += "<a href=\"javascript:;\" class=\"pull-right add-related-line-item-btn\" data-device-id=\"" + device_id + "\"><i class=\"fa fa-plus-circle\"></i>Add a Related Line Item</a>";
							output += "</section>";
							output += "</td></tr>";
						}
					}
	       		} else {
	       			//output += "<tr><td colspan=\"6\" class=\"no-results\">No child line items.</td></tr>";
	       		}
				
				output += "</tbody>";
				output += "</table>";
				output += "<a href=\"javascript:;\" class=\"pull-right add-related-line-item-btn\" data-device-id=\"" + parent_device_id + "\"><i class=\"fa fa-plus-circle\"></i>Add a Related Line Item</a>";
				output += "</section>";
				output += "</div>";
				output += "</div>";
				
				return output;
			},
			build_new_related_line_item_row:function(device_id) {
				var output = "";
				
				var device_select = "";
				var devices = $.grep(CONTRACT.contract_services.devices, function(n) { return n.id == device_id; });
				if(devices.length > 0) {
					var device = devices[0];
					
					device = CONTRACT.contract_services.filter_embedded_device(device);
					var related_devices = device.relatedDevices;
					if(related_devices && related_devices.length > 0) {
						device_select = "<select class=\"add-related-lineitem-device-select\" style=\"width:300px;\">";
						device_select += "<option value=\"\"></option>";
						for(var i=0; i < related_devices.length; i++) {
							var related_device = related_devices[i];
							device_select += "<option value=\"" + related_device.id + "\" data-default-osp-id=\"" + related_device.defaultOspId + "\" data-device-description=\"" + related_device.description + "\">" + related_device.description + " (" + related_device.partNumber +  ")</option>";
						}
						device_select += "</select>";
					}
				}
				
				var unit_displayed = " style=\"display:none;\"";
				var hidden = " style=\"display:none;\"";
				var total_displayed = " style=\"display:inline-block;\"";
				
				
				output += "<tr data-operation=\"create\">";
				output += "<td>";
				output += device_select;
				output += "</td>";
				output += "<td><span class=\"field-symbol\"></span><span class=\"required-ind required\"></span><input type=\"text\" class=\"edit-contract-field datepicker\" name=\"child-start-date\" placeholder=\"mm/dd/yyyy\" size=\"10\" value=\"\" /></td>";
				output += "<td><span class=\"field-symbol\"></span><span class=\"required-ind required\"></span><input type=\"text\" class=\"edit-contract-field datepicker\" name=\"child-end-date\" placeholder=\"mm/dd/yyyy\" size=\"10\" value=\"\" /></td>";
				if(false) {
					output += "<td><span class=\"field-symbol\"></span><span class=\"required-ind required\"></span><input type=\"text\" class=\"edit-contract-field unit-count-calculator-field\" size=\"3\" name=\"child-unit-count\" value=\"\" /></td>";
					output += "<td><a href=\"javascript:;\" data-view=\"unit-cost\" alt=\"Click to toggle between unit cost and total cost.\" title=\"Click to toggle between unit cost and total cost.\" class=\"uc-calculator-btn\"" + hidden + "><i class=\"fa fa-calculator\"></i>Total</a></td>";
				} else {
					output += "<td><span class=\"field-symbol\"></span><span class=\"required-ind\"></span><input type=\"text\" size=\"3\" class=\"edit-contract-field\" disabled=\"disabled\" name=\"child-unit-count\" class=\"keep-disabled\" /></td>";
					output += "<td><a href=\"javascript:;\" data-view=\"unit-cost\" alt=\"Click to toggle between unit cost and total cost.\" title=\"Click to toggle between unit cost and total cost.\" class=\"uc-calculator-btn\"" + hidden + "><i class=\"fa fa-calculator\"></i>Total</a></td>";
				}
				
				output += "<td>";
				output += "<div class=\"total-price-view\"" + total_displayed + "><span class=\"field-symbol\">$</span><span class=\"required-ind required\"></span><input type=\"text\" size=\"8\" class=\"edit-contract-field currency\" name=\"child-onetime-cost\"" + " value=\"\" /></div>";
				output += "<div class=\"unit-price-view\"" + unit_displayed + "><label>Unit Price</label><div><span class=\"field-symbol\">$</span><span class=\"required-ind required\"></span><input type=\"text\" size=\"8\" class=\"currency edit-contract-field unit-cost-calculator-field\" name=\"child-onetime-unit-cost\" /></div><div class=\"unit-price-total-row\">Total: <span class=\"unit-cost-total\"></span></div></div>";
				output += "</td>";
				output += "<td>";
				output += "<div class=\"total-price-view\"" + total_displayed + "><span class=\"field-symbol\">$</span><span class=\"required-ind required\"></span><input type=\"text\" size=\"8\" class=\"edit-contract-field currency\" name=\"child-recurring-cost\"" + " value=\"\" /></div>";
				output += "<div class=\"unit-price-view\"" + unit_displayed + "><label>Unit Price</label><div><span class=\"field-symbol\">$</span><span class=\"required-ind required\"></span><input type=\"text\" size=\"8\" class=\"currency edit-contract-field unit-cost-calculator-field\" name=\"child-recurring-unit-cost\" /></div><div class=\"unit-price-total-row\">Total: <span class=\"unit-cost-total\"></span></div></div>";
				output += "</td>";
				output += "<td><a href=\"javascript:;\" class=\"related-line-item-remove-btn\"><i class=\"fa fa-minus-circle\"></i>Remove</a></td>";
				output += "</tr>";
				
				return output;
			},
			manage_new_grandchild_lineitem_section:function($select, device_id) {
				var output = "";
				
				var related_devices = null;
				var devices = $.grep(CONTRACT.contract_services.devices, function(n) { return n.id == device_id; });
				if(devices.length > 0) {
					var device = devices[0];
					
					if(device.relatedDevices && device.relatedDevices.length > 0) {
						related_devices = [];
						for(var i = 0; i < device.relatedDevices.length; i++) {
							var related_device = device.relatedDevices[i];
							if(related_device.relationship != "embedded") {
								related_devices.push(related_device);
							}
						}
					}
				}
				
				var $row = $select.parent().parent();
				if(related_devices && related_devices.length > 0) {
					output += "<tr class=\"child-row\"><td colspan=\"7\">";
					output += "<section class=\"table-wrapper\" style=\"padding-left:25px;\">";
					output += "<div class=\"table-header\">";
					output += "<div class=\"table-title\" style=\"font-size:0.9em;\"><i class=\"fas fa-link\"></i>Related Line Items</div>";
					output += "</div>";
					output += "<table class=\"grandchild-lineitems-table\">";
					output += "<thead><tr><th>Device</th><th>Start Date</th><th>End Date</th><th>Unit Count</th><th></th><th>NRC</th><th>MRC</th><th>&nbsp;</th></tr></thead>";
					output += "<tbody>";
					output += CONTRACT.contract_services.edit.build_new_related_line_item_row(device_id);
					output += "</tbody></table>";
					output += "<a href=\"javascript:;\" class=\"pull-right add-related-line-item-btn\" data-device-id=\"" + device_id + "\"><i class=\"fa fa-plus-circle\"></i>Add a Related Line Item</a>";
					output += "</section>";
					output += "</td></tr>";
					$row.after(output);
				} else {
					//check if you need to remove the row
					if($row.next().hasClass(CONTRACT.contract_services.add.child_row_class)) {
						$row.next().remove();
					}
				}
			},
			unmap_row:function($obj) {
				$obj.removeClass(CONTRACT.contract_services.edit.unmap_child_row_class).addClass(CONTRACT.contract_services.edit.undo_unmap_child_row_class).html($(CONTRACT.contract_services.edit.undo_msg).val());
				$obj.siblings(CONTRACT.contract_services.edit.delete_related_line_item_btn).hide();
				var $tr = $obj.parent().parent();
				$tr.data("operation","unmap");
			},
			undo_unmap_row:function($obj) {
				$obj.removeClass(CONTRACT.contract_services.edit.undo_unmap_child_row_class).addClass(CONTRACT.contract_services.edit.unmap_child_row_class).html("<i class=\"fa fa-unlink\"></i>" + "Un-Map");
				$obj.siblings(CONTRACT.contract_services.edit.delete_related_line_item_btn).show();
				var $tr = $obj.parent().parent();
				$tr.data("operation","");
			},
			delete_row:function($obj) {
				$obj.removeClass(CONTRACT.contract_services.edit.delete_row_class).addClass(CONTRACT.contract_services.edit.undo_delete_row_class).html($(CONTRACT.contract_services.edit.undo_msg).val());
				var $tr = $obj.parent().parent();
				$tr.data("operation","delete");
				$tr.find(CONTRACT.contract_services.edit.edit_contract_field_class).hide();
				$tr.find(CONTRACT.contract_services.edit.required_class).hide();
				$tr.find(CONTRACT.contract_services.edit.edit_contract_display_class).css("display","inline-block");
				$tr.next().find(CONTRACT.contract_services.edit.edit_contract_field_class).hide();
				$tr.next().find(CONTRACT.contract_services.edit.edit_contract_display_class).css("display","inline-block");
				var $third_row = $tr.next().next();
				if($third_row.hasClass(CONTRACT.contract_services.edit.pcr_row_class)) {
					$third_row.find(CONTRACT.contract_services.edit.edit_contract_field_class).hide();
					$third_row.find(CONTRACT.contract_services.edit.edit_contract_display_class).css("display","inline-block");
				}
				var $fourth_row = $third_row.next();
				if($fourth_row.hasClass(CONTRACT.contract_services.edit.server_stat_row_class)) {
					$fourth_row = $fourth_row.next();
				}
				if($fourth_row.hasClass(CONTRACT.contract_services.edit.azure_row_class) || $fourth_row.hasClass(CONTRACT.contract_services.edit.related_row_class)) {
					$fourth_row.find(CONTRACT.contract_services.edit.edit_contract_field_class).hide();
					$fourth_row.find(CONTRACT.contract_services.edit.edit_contract_display_class).css("display","inline-block");
					$fourth_row.find(CONTRACT.contract_services.edit.delete_related_line_item_btn).hide();
					$fourth_row.find(CONTRACT.contract_services.edit.required_class).hide();
					$fourth_row.find(CONTRACT.contract_services.edit.add_related_line_item_btn).hide();
					$fourth_row.find(CONTRACT.contract_services.add.unit_price_calculator_btn).hide();
				}
				var $fifth_row = $fourth_row.next();
				if($fifth_row.hasClass(CONTRACT.contract_services.edit.azure_row_class)) {
					$fifth_row.find(CONTRACT.contract_services.edit.edit_contract_field_class).show();
					$fifth_row.find(CONTRACT.contract_services.edit.edit_contract_display_class).css("display","none");
				}
			},
			undo_delete_row:function($obj) {
				$obj.removeClass(CONTRACT.contract_services.edit.undo_delete_row_class).addClass(CONTRACT.contract_services.edit.delete_row_class).html("<i class=\"fa fa-minus-circle\"></i>" + $(CONTRACT.contract_services.edit.delete_msg).val());
				var $tr = $obj.parent().parent();
				$tr.data("operation","");
				$tr.find(CONTRACT.contract_services.edit.edit_contract_field_class).show();
				$tr.find(CONTRACT.contract_services.edit.required_class).show();
				$tr.find(CONTRACT.contract_services.edit.edit_contract_display_class).css("display","none");
				$tr.next().find(CONTRACT.contract_services.edit.edit_contract_field_class).show();
				$tr.next().find(CONTRACT.contract_services.edit.edit_contract_display_class).css("display","none");
				var $third_row = $tr.next().next();
				if($third_row.hasClass(CONTRACT.contract_services.edit.pcr_row_class)) {
					$third_row.find(CONTRACT.contract_services.edit.edit_contract_field_class).show();
					$third_row.find(CONTRACT.contract_services.edit.edit_contract_display_class).css("display","none");
				}
				var $fourth_row = $third_row.next();
				if($fourth_row.hasClass(CONTRACT.contract_services.edit.server_stat_row_class)) {
					$fourth_row = $fourth_row.next();
				}
				if($fourth_row.hasClass(CONTRACT.contract_services.edit.azure_row_class) || $fourth_row.hasClass(CONTRACT.contract_services.edit.related_row_class)) {
					$fourth_row.find(CONTRACT.contract_services.edit.edit_contract_field_class).show();
					$fourth_row.find(CONTRACT.contract_services.edit.edit_contract_display_class).css("display","none");
					$fourth_row.find(CONTRACT.contract_services.edit.delete_related_line_item_btn).show();
					$fourth_row.find(CONTRACT.contract_services.edit.required_class).show();
					$fourth_row.find(CONTRACT.contract_services.edit.add_related_line_item_btn).show();
					
				}
				var $fifth_row = $fourth_row.next();
				if($fifth_row.hasClass(CONTRACT.contract_services.edit.azure_row_class)) {
					$fifth_row.find(CONTRACT.contract_services.edit.edit_contract_field_class).show();
					$fifth_row.find(CONTRACT.contract_services.edit.edit_contract_display_class).css("display","none");
				}
			},
			delete_child_row:function($obj) {
				$obj.removeClass(CONTRACT.contract_services.edit.delete_child_row_class).addClass(CONTRACT.contract_services.edit.undo_delete_child_row_class).html($(CONTRACT.contract_services.edit.undo_msg).val());
				$obj.siblings(CONTRACT.contract_services.edit.unmap_related_line_item_btn).hide();
				var $tr = $obj.parent().parent();
				$tr.data("operation","delete");
				$tr.find(CONTRACT.contract_services.edit.edit_contract_field_class).hide();
				$tr.find(CONTRACT.contract_services.edit.required_class).hide();
				$tr.find(CONTRACT.contract_services.edit.edit_contract_display_class).css("display","inline-block");
				$tr.find(CONTRACT.contract_services.add.unit_price_calculator_btn).hide();
				
				var $grandchild_tr = $tr.next();
				if($grandchild_tr.hasClass(CONTRACT.contract_services.add.child_row_class)) {
					$grandchild_tr.data("operation","delete");
					$grandchild_tr.find(CONTRACT.contract_services.edit.edit_contract_field_class).hide();
					$grandchild_tr.find(CONTRACT.contract_services.edit.required_class).hide();
					$grandchild_tr.find(CONTRACT.contract_services.edit.edit_contract_display_class).css("display","inline-block");
					$grandchild_tr.find(CONTRACT.contract_services.edit.delete_related_line_item_btn).hide();
					$grandchild_tr.find(CONTRACT.contract_services.edit.add_related_line_item_btn).hide();
					$grandchild_tr.find(CONTRACT.contract_services.add.unit_price_calculator_btn).hide();
				}
			},
			undo_delete_child_row:function($obj) {
				$obj.removeClass(CONTRACT.contract_services.edit.undo_delete_child_row_class).addClass(CONTRACT.contract_services.edit.delete_child_row_class).html("<i class=\"fa fa-minus-circle\"></i>" + $(CONTRACT.contract_services.edit.delete_msg).val());
				$obj.siblings(CONTRACT.contract_services.edit.unmap_related_line_item_btn).show();
				var $tr = $obj.parent().parent();
				$tr.data("operation","");
				$tr.find(CONTRACT.contract_services.edit.edit_contract_field_class).show();
				$tr.find(CONTRACT.contract_services.edit.required_class).show();
				$tr.find(CONTRACT.contract_services.edit.edit_contract_display_class).css("display","none");
				$tr.find(CONTRACT.contract_services.add.unit_price_calculator_btn).show();
				
				var $grandchild_tr = $tr.next();
				if($grandchild_tr.hasClass(CONTRACT.contract_services.add.child_row_class)) {
					$grandchild_tr.data("operation","");
					$grandchild_tr.find(CONTRACT.contract_services.edit.edit_contract_field_class).show();
					$grandchild_tr.find(CONTRACT.contract_services.edit.required_class).show();
					$grandchild_tr.find(CONTRACT.contract_services.edit.edit_contract_display_class).css("display","none");
					$grandchild_tr.find(CONTRACT.contract_services.edit.delete_related_line_item_btn).show();
					$grandchild_tr.find(CONTRACT.contract_services.edit.add_related_line_item_btn).show();
					$grandchild_tr.find(CONTRACT.contract_services.add.unit_price_calculator_btn).show();
				}
			},
			submit_services:function() {
				UTIL.clear_message_in_popup(CONTRACT.edit_contract_service_dialog);
				
				//var service_id = CONTRACT.contract_services.edit.current_service_id;
				var service_id = $(CONTRACT.contract_services.edit.contract_service_item_field).val();
				var services = [];
				var $table = $(CONTRACT.contract_services.edit.contract_service_table);
				var pcr_id = $(CONTRACT.contract_services.edit.contract_pcr_field).val();
				var part_number = $(CONTRACT.contract_services.edit.contract_part_number_field).val();
				var part_description = $(CONTRACT.contract_services.edit.contract_part_description_field).val();
				if(pcr_id == "#") pcr_id = "";
				
				if(!part_number && !part_description) {
					UTIL.add_error_message_to_popup(CONTRACT.edit_contract_service_dialog, $(CONTRACT.contract_services.edit.part_identifier_required_msg).val());
					return false;
				}
				
				//validate the device exists
				var device_id = CONTRACT.contract_services.get_device_id(part_number, part_description);
				if(!device_id) {
					UTIL.add_error_message_to_popup(CONTRACT.edit_contract_service_dialog, $(CONTRACT.contract_services.add.contract_device_validation_msg).val());
					return false;
				}
				
				var contract_start_moment = moment($(CONTRACT.date_selectors.contract_start_date).val(), CONTRACT.date_selectors.moment_format);
				var contract_end_moment = moment($(CONTRACT.date_selectors.contract_end_date).val(), CONTRACT.date_selectors.moment_format);
				
				var require_unit_count = CONTRACT.contract_services.get_device_require_unit_count(device_id);
				
				//loop through rows
				var is_valid = true;
				var error_msg = "";
				$table.find("tbody").find("tr.edit-row").each(function() {
					var $row = $(this);
					var id = $row.find(CONTRACT.contract_services.edit.contract_service_id_field).val();
					var parent_id = $row.find(CONTRACT.contract_services.edit.contract_service_parent_id_field).val();
					var ci_name = $row.find(CONTRACT.contract_services.edit.contract_ci_name_field).val();
					var start_date = $row.find(CONTRACT.contract_services.edit.contract_start_date_field).val();
					var end_date = $row.find(CONTRACT.contract_services.edit.contract_end_date_field).val();
					var device_unit_count = $row.find(CONTRACT.contract_services.edit.contract_device_unit_count_field).val();
					var one_time_cost = $row.find(CONTRACT.contract_services.edit.contract_one_time_cost_field).val();
					var recurring_cost = $row.find(CONTRACT.contract_services.edit.contract_recurring_cost_field).val();
					var operation = $row.data("operation");
					//second row
					var notes = $row.next().find("textarea").val();
					//third row
					var contract_group_id = $row.next().next().find(CONTRACT.contract_services.edit.contract_group_field).val();
					var status = $row.next().next().find(CONTRACT.contract_services.edit.contract_status_field).val();
					var location_id = $row.next().next().find(CONTRACT.contract_services.edit.contract_location_field).val();
					//fourth row
					var contract_service_azure_id = $row.find(CONTRACT.contract_services.edit.contract_azure_id_field).val();
					if(contract_group_id == "#") contract_group_id = "";
					
					var existing_services = CONTRACT.contract_services.edit.cached_services;
					
					var related_lineitems = null;
					var $fourth_row = $row.next().next().next();
					if($fourth_row.hasClass(CONTRACT.contract_services.edit.server_stat_row_class)) {
						$fourth_row = $fourth_row.next();
					}
					if($fourth_row.hasClass(CONTRACT.contract_services.edit.related_row_class)) {
						related_lineitems = [];
						
						var any_children_changed = false;
						var child_validation_error_msg = "";
						var child_validation_error = false;
						$fourth_row.find(CONTRACT.contract_services.edit.related_lineitems_table + " > tbody > tr").each(function() {
							var $related_row = $(this);
							
							var grandchild_json = null;
							var child_changed = false;
							if($related_row.next().hasClass(CONTRACT.contract_services.add.child_row_class)) {
								//grandchild items. process these first
								var $gc_table = $related_row.next().find(CONTRACT.contract_services.add.grandchild_lineitems_table);
								grandchild_json = [];
								
								$gc_table.find("tbody tr").each(function() {
									var $gc_row = $(this);
									var grandchild_changed = false;
									
									var child_id = $related_row.find(CONTRACT.contract_services.edit.related_lineitem_id).val();
									var gchild_id = $gc_row.find(CONTRACT.contract_services.edit.related_lineitem_id).val();
									var gchild_device_id = $gc_row.find(CONTRACT.contract_services.add.related_lineitem_device_id_field).val();
									var gchild_device_description = $gc_row.find(CONTRACT.contract_services.add.related_lineitem_device_description_field).val();
									var gchild_device_part_number = $gc_row.find(CONTRACT.contract_services.add.related_lineitem_device_part_number_field).val();
									var gchild_service_id = $gc_row.find(CONTRACT.contract_services.add.related_lineitem_service_id_field).val();
									var gchild_default_osp_id = $gc_row.find(CONTRACT.contract_services.add.related_lineitem_default_osp_id_field).val();
									var gchild_quantity = 1;
									var gchild_unit_count = $gc_row.find(CONTRACT.contract_services.add.related_lineitem_unit_count_field).val();
									var gchild_onetime_cost = $gc_row.find(CONTRACT.contract_services.add.related_lineitem_onetime_cost_field).val();
									var gchild_recurring_cost = $gc_row.find(CONTRACT.contract_services.add.related_lineitem_recurring_cost_field).val();
									var gchild_start_date = $gc_row.find(CONTRACT.contract_services.edit.related_lineitem_start_date).val();
									var gchild_end_date = $gc_row.find(CONTRACT.contract_services.edit.related_lineitem_end_date).val();
									var gchild_operation = $gc_row.data("operation");
									
									if(!gchild_operation) {
										gchild_operation = "update";
									}
									
									if(gchild_operation == "unmap") {
										grandchild_changed = true;
									}
									
									if(!gchild_id) {
										gchild_device_id = $gc_row.find("select").val();
										gchild_default_osp_id = $gc_row.find("select option:selected").data("default-osp-id");
										gchild_device_description = $gc_row.find("select option:selected").data("device-description");
										child_changed = true;
										grandchild_changed = true;
										gchild_operation = "create";
									}
									
									if(!gchild_quantity || !gchild_onetime_cost || !gchild_recurring_cost) {
										child_validation_error_msg = "Please enter all required fields for the grandchild line item: " + gchild_device_description;
										child_validation_error = true;
										return false;
									}
									
									if(!gchild_unit_count) gchild_unit_count = null;
									
									var gchild_require_unit_count = CONTRACT.contract_services.get_device_require_unit_count(gchild_device_id);
									if(gchild_require_unit_count && !gchild_unit_count) {
										child_validation_error_msg = "Unit Count is required for the grandchild line item: " + gchild_device_description + ". Please enter all required fields for grandchild line items.";
										child_validation_error = true;
										return false;
									}			
									
									if(!grandchild_changed) {
										var parent_service = $.grep(existing_services, function(n) { return n.id == id; });
										if(parent_service && parent_service.length > 0){
											parent_service = parent_service[0];
											
											var existing_child_lineitems = parent_service.relatedLineItems;
											for(var i = 0; i < existing_child_lineitems.length; i++) {
												var existing_child_lineitem = existing_child_lineitems[i];
												
												var gchild_lineitem = $.grep(existing_child_lineitem.relatedLineItems, function(n) { return n.id == gchild_id; });
												if(gchild_lineitem && gchild_lineitem.length > 0) {
													gchild_lineitem = gchild_lineitem[0];
													
													var existing_gstart_date = UTIL.convert_dates_for_ui(gchild_lineitem.startDate);
													var existing_gend_date = UTIL.convert_dates_for_ui(gchild_lineitem.endDate);
													//validate if anything has changed
													//console.log("Unit Count - " + gchild_unit_count + " = " + gchild_lineitem.deviceUnitCount + "(" + (gchild_unit_count != gchild_lineitem.deviceUnitCount) + ") -- Onetime Cost: " + gchild_onetime_cost + " = " + gchild_lineitem.onetimeRevenue + "(" + (gchild_onetime_cost != gchild_lineitem.onetimeRevenue) + ") -- Recurring Cost: " + gchild_recurring_cost + " = " + gchild_lineitem.recurringRevenue + "(" + (gchild_recurring_cost != gchild_lineitem.recurringRevenue) + ") -- Start Date: " + gchild_start_date + " = " + existing_gstart_date + "(" + (gchild_start_date != existing_gstart_date) + ") -- End Date: " + gchild_end_date + " = " + existing_gend_date + "(" + (gchild_end_date != existing_gend_date) + ")");
													if(gchild_unit_count != gchild_lineitem.deviceUnitCount || gchild_onetime_cost != gchild_lineitem.onetimeRevenue || gchild_recurring_cost != gchild_lineitem.recurringRevenue || gchild_start_date != existing_gstart_date || gchild_end_date != existing_gend_date) {
														grandchild_changed = true;
													}	
												}	
											}
										}
									}
									
									if(!grandchild_changed) {
										var parent_service = $.grep(existing_services, function(n) { return n.id == id; });
										if(parent_service && parent_service.length > 0){
											parent_service = parent_service[0];
											
											var existing_child_lineitems = parent_service.relatedLineItems;
											for(var i = 0; i < existing_child_lineitems.length; i++) {
												var existing_child_lineitem = existing_child_lineitems[i];
												
												var gchild_lineitem = $.grep(existing_child_lineitem.relatedLineItems, function(n) { return n.id == gchild_id; });
												if(gchild_lineitem && gchild_lineitem.length > 0) {
													gchild_lineitem = gchild_lineitem[0];
													
													var existing_gstart_date = UTIL.convert_dates_for_ui(gchild_lineitem.startDate);
													var existing_gend_date = UTIL.convert_dates_for_ui(gchild_lineitem.endDate);
													//validate if anything has changed
													//console.log("Unit Count - " + gchild_unit_count + " = " + gchild_lineitem.deviceUnitCount + "(" + (gchild_unit_count != gchild_lineitem.deviceUnitCount) + ") -- Onetime Cost: " + gchild_onetime_cost + " = " + gchild_lineitem.onetimeRevenue + "(" + (gchild_onetime_cost != gchild_lineitem.onetimeRevenue) + ") -- Recurring Cost: " + gchild_recurring_cost + " = " + gchild_lineitem.recurringRevenue + "(" + (gchild_recurring_cost != gchild_lineitem.recurringRevenue) + ") -- Start Date: " + gchild_start_date + " = " + existing_gstart_date + "(" + (gchild_start_date != existing_gstart_date) + ") -- End Date: " + gchild_end_date + " = " + existing_gend_date + "(" + (gchild_end_date != existing_gend_date) + ")");
													if(gchild_unit_count != gchild_lineitem.deviceUnitCount || gchild_onetime_cost != gchild_lineitem.onetimeRevenue || gchild_recurring_cost != gchild_lineitem.recurringRevenue || gchild_start_date != existing_gstart_date || gchild_end_date != existing_gend_date) {
														grandchild_changed = true;
													}	
												}	
											}
										}
									}
									
									gchild_start_date = UTIL.convert_dates_for_server(gchild_start_date);
									gchild_end_date = UTIL.convert_dates_for_server(gchild_end_date);
									
									var gchild_json = {};
									
									if(gchild_operation == "delete") {
										gchild_json = {"id":gchild_id, "contractId":CONTRACT.contract_id, "startDate":gchild_start_date, "endDate":gchild_end_date, "operation":gchild_operation, "deviceId":gchild_device_id};
										grandchild_json.push(gchild_json);
										any_children_changed = true;
										child_changed = true;
									} else if(grandchild_changed) {
										gchild_json = { "id":gchild_id, "parentId":child_id, "serviceId":gchild_service_id, "operation":gchild_operation, "contractId":CONTRACT.contract_id, "contractGroupId":contract_group_id, "contractUpdateId":pcr_id, "quantity":gchild_quantity, "onetimeRevenue":gchild_onetime_cost, "recurringRevenue":gchild_recurring_cost, "startDate":gchild_start_date, "endDate":gchild_end_date, "note":null, "devicePartNumber":gchild_device_part_number, "deviceDescription":gchild_device_description, "deviceId":gchild_device_id, "deviceUnitCount":gchild_unit_count, "status":status, "locationId":location_id, "defaultOspId":gchild_default_osp_id };
										grandchild_json.push(gchild_json);
										any_children_changed = true;
										child_changed = true;
									}
								});
							}
							
							if(!$related_row.hasClass(CONTRACT.contract_services.add.child_row_class)) {
								var child_id = $related_row.find(CONTRACT.contract_services.edit.related_lineitem_id).val();
								var child_device_id = $related_row.find(CONTRACT.contract_services.add.related_lineitem_device_id_field).val();
								var child_device_description = $related_row.find(CONTRACT.contract_services.add.related_lineitem_device_description_field).val();
								var child_device_part_number = $related_row.find(CONTRACT.contract_services.add.related_lineitem_device_part_number_field).val();
								var child_service_id = $related_row.find(CONTRACT.contract_services.add.related_lineitem_service_id_field).val();
								var child_default_osp_id = $row.find(CONTRACT.contract_services.add.related_lineitem_default_osp_id_field).val();
								var child_quantity = 1;
								var child_unit_count = $related_row.find(CONTRACT.contract_services.add.related_lineitem_unit_count_field).val();
								var child_onetime_cost = $related_row.find(CONTRACT.contract_services.add.related_lineitem_onetime_cost_field).val();
								var child_recurring_cost = $related_row.find(CONTRACT.contract_services.add.related_lineitem_recurring_cost_field).val();
								var child_start_date = $related_row.find(CONTRACT.contract_services.edit.related_lineitem_start_date).val();
								var child_end_date = $related_row.find(CONTRACT.contract_services.edit.related_lineitem_end_date).val();
								var child_operation = $related_row.data("operation");
								
								if(!child_operation) {
									child_operation = "update";
								}
								
								if(child_operation == "unmap") {
									child_changed = true;
								}
								
								if(!child_id) {
									child_device_id = $related_row.find("select").val();
									child_default_osp_id = $related_row.find("select option:selected").data("default-osp-id");
									child_device_description = $related_row.find("select option:selected").data("device-description");
									child_changed = true;
									child_operation = "create";
								}
								
								if(!child_quantity || !child_onetime_cost || !child_recurring_cost) {
									child_validation_error_msg = "Please enter all required fields for the child line item: " + child_device_description;
									child_validation_error = true;
									return false;
								}
								
								if(!child_unit_count) child_unit_count = null;
								
								var child_require_unit_count = CONTRACT.contract_services.get_device_require_unit_count(child_device_id);
								if(child_require_unit_count && !child_unit_count) {
									child_validation_error_msg = "Unit Count is required for the child line item: " + child_device_description + ". Please enter all required fields for child line items.";
									child_validation_error = true;
									return false;
								}
								
								if(!child_changed) {
									var parent_service = $.grep(existing_services, function(n) { return n.id == id; });
									if(parent_service && parent_service.length > 0){
										parent_service = parent_service[0];
										
										var child_lineitem = $.grep(parent_service.relatedLineItems, function(n) { return n.id == child_id; });
										if(child_lineitem && child_lineitem.length > 0) {
											child_lineitem = child_lineitem[0];
											
											var existing_start_date = UTIL.convert_dates_for_ui(child_lineitem.startDate);
											var existing_end_date = UTIL.convert_dates_for_ui(child_lineitem.endDate);
											//validate if anything has changed
											//console.log("Unit Count - " + child_unit_count + " = " + child_lineitem.deviceUnitCount + "(" + (child_unit_count != child_lineitem.deviceUnitCount) + ") -- Onetime Cost: " + child_onetime_cost + " = " + child_lineitem.onetimeRevenue + "(" + (child_onetime_cost != child_lineitem.onetimeRevenue) + ") -- Recurring Cost: " + child_recurring_cost + " = " + child_lineitem.recurringRevenue + "(" + (child_recurring_cost != child_lineitem.recurringRevenue) + ") -- Start Date: " + child_start_date + " = " + existing_start_date + "(" + (child_start_date != existing_start_date) + ") -- End Date: " + child_end_date + " = " + existing_end_date + "(" + (child_end_date != existing_end_date) + ")");
											if(child_unit_count != child_lineitem.deviceUnitCount || child_onetime_cost != child_lineitem.onetimeRevenue || child_recurring_cost != child_lineitem.recurringRevenue || child_start_date != existing_start_date || child_end_date != existing_end_date) {
												child_changed = true;
											}
											
										}
										
									}
								}
								
								
								child_start_date = UTIL.convert_dates_for_server(child_start_date);
								child_end_date = UTIL.convert_dates_for_server(child_end_date);
								
								var child_json = {};
								
								if(child_operation == "delete") {
									child_json = {"id":child_id, "contractId":CONTRACT.contract_id, "startDate":child_start_date, "endDate":child_end_date, "operation":child_operation, "deviceId":child_device_id};
									related_lineitems.push(child_json);
									any_children_changed = true;
								} else if(child_changed) {
									child_json = { "id":child_id, "parentId":id, "serviceId":child_service_id, "operation":child_operation, "contractId":CONTRACT.contract_id, "contractGroupId":contract_group_id, "contractUpdateId":pcr_id, "quantity":child_quantity, "onetimeRevenue":child_onetime_cost, "recurringRevenue":child_recurring_cost, "startDate":child_start_date, "endDate":child_end_date, "note":null, "devicePartNumber":child_device_part_number, "deviceDescription":child_device_description, "deviceId":child_device_id, "deviceUnitCount":child_unit_count, "status":status, "locationId":location_id, "defaultOspId":child_default_osp_id };
									if(grandchild_json) {
										child_json["relatedLineItems"] = grandchild_json;
									}
									related_lineitems.push(child_json);
									any_children_changed = true;
								}
							}
						});
					}
					
					if(child_validation_error) {
						is_valid = false;
						error_msg = child_validation_error_msg;
						return false;
					}
					
					
					//validate
					if(!service_id || !one_time_cost || !recurring_cost || !start_date || !end_date) {
						error_msg = $(CONTRACT.general_validation_error).val();
						is_valid = false;
						return false;
					}
					
					if(require_unit_count && !device_unit_count && operation != "delete") {
						error_msg = "Unit Count is required for this device type.";
						is_valid = false;
						return false;
					}
					
					// validate start & end date
					var start_moment = moment(start_date, CONTRACT.date_selectors.moment_format);
					var end_moment = moment(end_date, CONTRACT.date_selectors.moment_format);
					
					// 1. validate start date & if end date is valid then validate it is not before the start date
					if ((!start_moment.isValid()) || ((end_date) && (!end_moment.isValid()))) {
						error_msg = $(CONTRACT.contract_start_date_validation_msg).val();
						is_valid = false;
						return false;
					} else if ((end_moment.isValid()) && (end_moment.isBefore(start_moment))) {
						error_msg = $(CONTRACT.contract_end_before_start_date_validation_msg).val();
						is_valid = false;
						return false;
					} 
					
					// 2. validate that service dates fall within contract date range
					if(start_moment.isBefore(contract_start_moment) || end_moment.isAfter(contract_end_moment)) {
						error_msg = $(CONTRACT.contract_services.add.contract_dates_validation_msg).val();
						is_valid = false;
						return false;
					}
					
					var changed = true;
					for(var j=0; j< existing_services.length; j++) {
						var record = existing_services[j];						
						if(record.id == id) {
							//found the object in our cache
							var existing_start_date = UTIL.convert_dates_for_ui(record.startDate);
							var existing_end_date = UTIL.convert_dates_for_ui(record.endDate);
							//console.log("ID: " + id + " start: " + (start_date == existing_start_date) + " end: " + (end_date == existing_end_date) + " one-time: " + (one_time_cost == record.onetimeRevenue) + " mrc: " + (recurring_cost == record.recurringRevenue) + "contract group: " + (contract_group_id == record.contractGroupId || (contract_group_id == "" && record.contractGroupId == null)) + " note: " + (notes == record.note || (notes == "" && record.note == null)) + " ci: " + (ci_name == record.deviceName || (ci_name == "" && record.deviceName == null)) + " service id: " + (record.serviceId == service_id) + " part number: " + (record.devicePartNumber == part_number) + " desc: " + (record.deviceDescription == part_description) + " unit count: " + (record.deviceUnitCount == device_unit_count || (device_unit_count == "" && record.deviceUnitCount == null)) + " status: " +  (record.status == status) + " location:  " + (record.locationId == location_id || (location_id == "" && record.locationId == null)));
							if(start_date == existing_start_date && end_date == existing_end_date && one_time_cost == record.onetimeRevenue && recurring_cost == record.recurringRevenue && (contract_group_id == record.contractGroupId || (contract_group_id == "" && record.contractGroupId == null)) && (notes == record.note || (notes == "" && record.note == null)) && (ci_name == record.deviceName || (ci_name == "" && record.deviceName == null)) && record.serviceId == service_id && record.devicePartNumber == part_number && record.deviceDescription == part_description && (record.deviceUnitCount == device_unit_count || (device_unit_count == "" && record.deviceUnitCount == null)) && record.status == status && (record.locationId == location_id || (location_id == "" && record.locationId == null))) {
								changed = false;
							}
							break;
						}
					}
					
					if(any_children_changed) {
						changed = true;
					}
					
					start_date = UTIL.convert_dates_for_server(start_date);
					end_date = UTIL.convert_dates_for_server(end_date);
					
					var json = {};
					if(operation == "delete") {
						json = {"id":id, "contractId":CONTRACT.contract_id, "startDate":start_date, "endDate":end_date, "operation":operation};
						services.push(json);
					} else if(changed) {
						json = {"id":id, "serviceId":service_id, "parentId":parent_id, "operation":operation, "contractId":CONTRACT.contract_id, contractUpdateId:pcr_id, "onetimeRevenue":one_time_cost, "recurringRevenue":recurring_cost, "startDate":start_date, "endDate":end_date, "note":notes, "devicePartNumber":part_number, "deviceDescription":part_description, "deviceName":ci_name, "contractGroupId":contract_group_id, "deviceUnitCount":device_unit_count, "deviceId":device_id, "status":status, "contractServiceSubscriptionId":contract_service_azure_id, "locationId":location_id, "relatedLineItems":related_lineitems };
						services.push(json);
					}
				});
				
				if(!is_valid) {
					UTIL.add_error_message_to_popup(CONTRACT.edit_contract_service_dialog, error_msg);
					return false;
				}
				
				UTIL.add_dialog_loader(CONTRACT.edit_contract_service_dialog);
				
				$.ajax ({
					url: PAGE_CONSTANTS.BASE_URL + "contractservices.json?batch",
					data: JSON.stringify(services),
					type: "POST",
					success:function(data) {
						UTIL.remove_dialog_loader(CONTRACT.edit_contract_service_dialog);
						var status = data.status;
						//we may want to loop through the indidivual responses and see if any errors occurred in adding the objects
						if(status == PAGE_CONSTANTS.OK_STS) {
							var success_msg = $(CONTRACT.contract_services.edit.contract_update_success_msg).val();
							UTIL.add_success_message_to_popup(CONTRACT.edit_contract_service_dialog, success_msg);
							$(CONTRACT.edit_contract_service_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").hide();
							$(CONTRACT.edit_contract_service_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button:contains('OK')").show();
							CONTRACT.contract_services.get_contract_services(CONTRACT.contract_id);
							$("." + CONTRACT.contract_services.edit.undo_delete_row_class).remove();
							$(CONTRACT.contract_services.edit.edit_contract_delete_all).hide();
							//CONTRACT.contract_services.setup_autocompletes();
						} else if (status == PAGE_CONSTANTS.ERRR_STS) {
							var error_msg = data.message;
							if(data.batchResults && data.batchResults.length > 0) {
								error_msg = data.batchResults[0].message;
							}
							UTIL.add_error_message_to_popup(CONTRACT.edit_contract_service_dialog, error_msg);
						}
					},
					error:function(jqXHR, textStatus, errorThrown) {
						UTIL.remove_dialog_loader(CONTRACT.edit_contract_service_dialog);
						var json = jqXHR.responseJSON;
						var message = "An error occurred. Please close this window and try to make your changes again.";
						if(json.status && json.message) {
							var status = json.status;
							if (status == PAGE_CONSTANTS.ERRR_STS) {
								message = json.message;
							}
						}
						UTIL.add_error_message_to_popup(CONTRACT.edit_contract_service_dialog, message);
					}
				});
			}
		},
		add: {
			contract_service_item_field: "#add-contract-service",
			contract_part_number_field: "#add-contract-service-part-number",
			contract_part_description_field: "#add-contract-service-part-description",
			contract_one_time_cost_field: "#add-contract-service-one-time-cost",
			contract_recurring_cost_field: "#add-contract-service-recurring-cost",
			contract_quantity_field: "#add-contract-service-quantity",
			contract_start_date_field: "#add-contract-service-start-date",
			contract_end_date_field: "#add-contract-service-end-date",
			contract_notes_field: "#add-contract-service-notes",
			contract_one_time_total_field: "#add-contract-service-one-time-cost-total",
			contract_recurring_total_field: "#add-contract-service-recurring-cost-total",
			contract_one_time_bill_date_field: "#add-contract-one-time-cost-bill-date",
			contract_pcr_field:"#add-contract-services-pcr",
			contract_location_field:"#add-contract-service-location",
			contract_no_pcr:"#add-contract-services-no-pcrs",
			contract_calculate_class: ".add-contract-service-calculate",
			contract_create_success_msg: "#new-contract-service-msg",
			contract_group_field:"#add-contract-service-group",
			contract_status_field:"#add-contract-service-status",
			contract_nrc_unit_cost_field:"#add-unit-contract-service-one-time-cost",
			contract_mrc_unit_cost_field:"#add-unit-contract-service-recurring-cost",
			nrc_unit_cost_total_display:"#total-nrc-cost-display",
			mrc_unit_cost_total_display:"#total-mrc-cost-display",
			add_unit_cost_field_class:".add-unit-cost-calculator-field",
			ci_name_container:"#ci-names-container",
			ci_name_clone:"#ci-name-clone",
			related_lineitems_container:"#add-related-devices-container",
			related_lineitems_table:"#add-related-devices",
			related_lineitems_msg:"#add-related-devices-msg",
			related_lineitem_enabled_checkbox:".child-device-enabled",
			related_lineitem_device_id_field:"input[name='child-device-id']",
			related_lineitem_device_description_field:"input[name='child-device-description']",
			related_lineitem_device_part_number_field:"input[name='child-device-part-number']",
			related_lineitem_quantity_field:"input[name='child-quantity']",
			related_lineitem_service_id_field:"input[name='child-service-id']",
			related_lineitem_default_osp_id_field:"input[name='child-default-osp-id']",
			related_lineitem_unit_count_field:"input[name='child-unit-count']",
			related_lineitem_onetime_cost_field:"input[name='child-onetime-cost']",
			related_lineitem_recurring_cost_field:"input[name='child-recurring-cost']",
			contract_ci_name_field:"input[name='add-contract-service-name']",
			grandchild_lineitems_table:".grandchild-lineitems-table",
			contract_group_class:".contract-group",
			child_row_class:"child-row",
			unit_price_calculator_btn:".uc-calculator-btn",
			unit_price_view_class:".unit-price-view",
			total_price_view_class:".total-price-view",
			unit_price_total_class:".unit-cost-total",
			unit_price_calculator_field:".unit-cost-calculator-field",
			unit_count_calculator_field:".unit-count-calculator-field",
			add_unit_price_view_class:".add-unit-price-view",
			add_total_price_view_class:".add-total-price-view",
			parent_lineitem_checkbox:".parent-checkbox",
			contract_device_unit_count_field:"#add-contract-service-unit-count",
			contract_dates_validation_msg:"#contract-service-contract-dates-msg",
			contract_device_validation_msg:"#contract-service-device-msg",
			contract_service_quantity_max_msg:"#validation-contract-service-quantity-max-msg",
			init:function() {
				CONTRACT.contract_services.add.bind_events();
			},
			bind_events:function() {
				$(document).on("input", CONTRACT.contract_services.add.contract_quantity_field, function() {
					CONTRACT.contract_services.add.setup_ci_name_fields();
				});
				
				$(document).on("click", CONTRACT.contract_services.add.related_lineitem_enabled_checkbox, function() {
					var $row = $(this).parents("tr");
					if(!$(this).prop("checked")) {
						$row.addClass("disabled-row");
						$row.find("input[type=text]").prop("disabled", true);
						$row.find(CONTRACT.contract_services.add.unit_price_calculator_btn).hide();
					} else {
						$row.removeClass("disabled-row");
						$row.find("input[type=text]").prop("disabled", false);
						$row.find(".keep-disabled").prop("disabled", true);
						$row.find(CONTRACT.contract_services.add.unit_price_calculator_btn).show();
					}
				});
				
				$(document).on("click", CONTRACT.contract_services.add.parent_lineitem_checkbox, function() {
					var id = $(this).data("id");
					var checked = $(this).prop("checked");
					
					if(checked) {
						$("tr[name='parent-id-" + id + "']").show();
					} else {
						$("tr[name='parent-id-" + id + "']").hide();
					}
				});
				
				$(document).on("click", CONTRACT.contract_services.add.unit_price_calculator_btn, function() {
					var $tr = $(this).parent().parent();
					var view = $(this).data("view");
					
					if(view == "total-cost") {
						$tr.find(CONTRACT.contract_services.add.total_price_view_class).hide();
						$tr.find(CONTRACT.contract_services.add.unit_price_view_class).show();
						$(this).data("view", "unit-cost");
						$(this).html("<i class=\"fa fa-calculator\"></i>Total");
					} else {
						$tr.find(CONTRACT.contract_services.add.total_price_view_class).show();
						$tr.find(CONTRACT.contract_services.add.unit_price_view_class).hide();
						$(this).data("view", "total-cost");
						$(this).html("<i class=\"fa fa-calculator\"></i>Unit");
					}
				});
				
				$(document).on("keyup", CONTRACT.contract_services.add.unit_price_calculator_field, function() {
					var $td = $(this).parents(CONTRACT.contract_services.add.unit_price_view_class).parent();
					var $tr = $td.parent();
					
					var unit_count = $tr.find(CONTRACT.contract_services.add.related_lineitem_unit_count_field).val();
					var unit_cost = $(this).val();
					
					var total = unit_cost * unit_count;
					$td.find(CONTRACT.contract_services.add.total_price_view_class).find("input").val(UTIL.convert_currency_for_server(total));
					$td.find(CONTRACT.contract_services.add.unit_price_total_class).html(UTIL.convert_currency_for_ui(total));
				});
				
				$(document).on("keyup", CONTRACT.contract_services.add.unit_count_calculator_field, function() {
					var $td = $(this).parent();
					var $tr = $td.parent();
					
					if($td.next().find(CONTRACT.contract_services.add.unit_price_calculator_btn).data("view") == "unit-cost") {
						var unit_count = $(this).val();
						
						var $nrc_td = $td.next().next();
						var $mrc_td = $nrc_td.next();
						
						var nrc_unit_cost = $nrc_td.find(CONTRACT.contract_services.add.unit_price_view_class).find("input").val();
						var nrc_total = nrc_unit_cost * unit_count;
						var mrc_unit_cost = $mrc_td.find(CONTRACT.contract_services.add.unit_price_view_class).find("input").val();
						var mrc_total = mrc_unit_cost * unit_count;
						
						$nrc_td.find(CONTRACT.contract_services.add.total_price_view_class).find("input").val(UTIL.convert_currency_for_server(nrc_total));
						$nrc_td.find(CONTRACT.contract_services.add.unit_price_total_class).html(UTIL.convert_currency_for_ui(nrc_total));
						$nrc_td.next().find(CONTRACT.contract_services.add.total_price_view_class).find("input").val(UTIL.convert_currency_for_server(mrc_total));
						$nrc_td.next().find(CONTRACT.contract_services.add.unit_price_total_class).html(UTIL.convert_currency_for_ui(mrc_total));
					}
				});
				
				$(CONTRACT.contract_services.add.add_unit_cost_field_class + ", "+ CONTRACT.contract_services.add.contract_device_unit_count_field).keyup(function() {
					CONTRACT.contract_services.add.calculate_unit_cost();
				});
			},
			calculate_unit_cost:function() {
				var unit_count = $(CONTRACT.contract_services.add.contract_device_unit_count_field).val();
				var nrc_unit_cost = $(CONTRACT.contract_services.add.contract_nrc_unit_cost_field).val();
				var mrc_unit_cost = $(CONTRACT.contract_services.add.contract_mrc_unit_cost_field).val();
				
				var nrc_total_unit_cost = nrc_unit_cost * unit_count;
				var mrc_total_unit_cost = mrc_unit_cost * unit_count;
				
				$(CONTRACT.contract_services.add.contract_one_time_cost_field).val(UTIL.convert_currency_for_server(nrc_total_unit_cost));
				$(CONTRACT.contract_services.add.contract_recurring_cost_field).val(UTIL.convert_currency_for_server(mrc_total_unit_cost));
				$(CONTRACT.contract_services.add.nrc_unit_cost_total_display).html(UTIL.convert_currency_for_ui(nrc_total_unit_cost));
				$(CONTRACT.contract_services.add.mrc_unit_cost_total_display).html(UTIL.convert_currency_for_ui(mrc_total_unit_cost));
				
			},
			reset_popup:function() {
				UTIL.clear_message_in_popup(CONTRACT.add_contract_service_dialog);
				$(CONTRACT.add_contract_service_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").show();
				$(CONTRACT.add_contract_service_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button:contains('Another')").hide();
				$(CONTRACT.add_contract_service_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button:contains('OK')").hide();
				
				var start_date = $(CONTRACT.date_selectors.current_viewing_start_date).val();
				var end_date = $(CONTRACT.date_selectors.contract_end_date).val();
				
				if(!start_date) start_date = $(CONTRACT.date_selectors.contract_start_date).val();
				
				$(CONTRACT.contract_services.add.contract_service_item_field).val("");
				$(CONTRACT.contract_services.add.contract_part_number_field).val("");
				$(CONTRACT.contract_services.add.contract_part_description_field).val("");
				$(CONTRACT.contract_services.add.contract_one_time_cost_field).val("");
				$(CONTRACT.contract_services.add.contract_recurring_cost_field).val("");
				$(CONTRACT.contract_services.add.contract_notes_field).val("");
				$(CONTRACT.contract_services.add.contract_pcr_field).val("#");
				$(CONTRACT.contract_services.add.contract_location_field).val("");
				if($(CONTRACT.contract_services.add.contract_pcr_field + " option").length > 1) {
					$(CONTRACT.contract_services.add.contract_pcr_field).show();
					$(CONTRACT.contract_services.add.contract_no_pcr).hide();
				} else {
					$(CONTRACT.contract_services.add.contract_pcr_field).hide();
					$(CONTRACT.contract_services.add.contract_no_pcr).show();
				}
				
				var $contract_group = $(CONTRACT.contract_services.add.contract_group_field);
				$contract_group.val("#");
				if($contract_group.find("option").length > 1) {
					$(CONTRACT.contract_services.add.contract_group_class).show();
				} else {
					$(CONTRACT.contract_services.add.contract_group_class).hide();
				}
				$(CONTRACT.contract_services.add.contract_status_field).val("active");
				
				$(CONTRACT.contract_services.add.contract_quantity_field).val(1); //set the default quantity to 1
				$(CONTRACT.contract_services.add.contract_device_unit_count_field).val(""); //set the default unit count to 1
				$(CONTRACT.contract_services.add.contract_start_date_field).val(start_date);
				$(CONTRACT.contract_services.add.contract_one_time_bill_date_field).html(CONTRACT.contract_services.add.convert_start_date_to_bill_date(start_date));
				$(CONTRACT.contract_services.add.contract_end_date_field).val(end_date);
				CONTRACT.contract_services.add.calculate_cost();
				
				$(CONTRACT.contract_services.add.related_lineitems_container).hide();
				$(CONTRACT.contract_services.add.related_lineitems_msg).hide();
			},
			convert_start_date_to_bill_date:function(date) {
				return moment(date, CONTRACT.date_selectors.moment_format).format("MMM YYYY");
			},
			calculate_cost:function() {
				var one_time = $(CONTRACT.contract_services.add.contract_one_time_cost_field).val();
				var recurring = $(CONTRACT.contract_services.add.contract_recurring_cost_field).val();
				var quantity = $(CONTRACT.contract_services.add.contract_quantity_field).val();
				var one_time_total = 0;
				var recurring_total = 0;
				
				if(!one_time) one_time = 0;
				if(!recurring) recurring = 0;
				
				one_time_total = one_time * quantity;
				recurring_total = recurring * quantity;
				
				$(CONTRACT.contract_services.add.contract_one_time_total_field).html(accounting.formatMoney(one_time_total));
				$(CONTRACT.contract_services.add.contract_recurring_total_field).html(accounting.formatMoney(recurring_total));
			},
			build_related_add_lineitems:function(devices) {
				var output = "";
				
				//filtered_device = CONTRACT.contract_services.filter_embedded_device(filtered_device);
				if(devices && devices.length > 0) {
					for(var i = 0; i < devices.length; i++) {
						var device = devices[i];
						
						var row_type = "";
						if(i % 2 == 0) {
							row_type = "odd";
						}
						output += CONTRACT.contract_services.add.build_related_add_lineitem_row(device, row_type, null);
						
						var related_devices = device.relatedDevices;
						if(related_devices != null && related_devices.length > 0) {
							var row_class = " class=\"child-row\"";;
							if(row_type == "odd") {
								row_class = " class=\"odd child-row\"";
							}
							output += "<tr" + row_class + " name=\"parent-id-" + device.id + "\" style=\"display:none;\"><td></td><td colspan=\"6\">";
							output += "<section class=\"table-wrapper\">";
							output += "<div class=\"table-header\">";
							output += "<div class=\"table-title\" style=\"font-size:0.9em;\"><i class=\"fas fa-link\"></i>Related Line Items</div>";
							output += "</div>";
							output += "<table class=\"grandchild-lineitems-table\">";
							output += "<thead><tr><th>&nbsp;</th><th>Device</th><th>Quantity</th><th>Unit Count</th><th>&nbsp;</th><th>NRC</th><th>MRC</th></tr></thead>";
							output += "<tbody>";
							
							for(var j = 0; j < related_devices.length; j++) {
								var related_device = related_devices[j];
								output += CONTRACT.contract_services.add.build_related_add_lineitem_row(related_device, row_type, device.id);
							}
							
							output += "</tbody></table></section>";
							output += "</td></tr>";
						}
					}
					$(CONTRACT.contract_services.add.related_lineitems_container).show();
					$(CONTRACT.contract_services.add.related_lineitems_msg).show();
				} else {
					$(CONTRACT.contract_services.add.related_lineitems_container).hide();
					$(CONTRACT.contract_services.add.related_lineitems_msg).hide();
				}
				
				$(CONTRACT.contract_services.add.related_lineitems_table + " tbody").html(output);
			},
			build_related_add_lineitem_row:function(device, row_type, parent_id) {
				var output = "";
				var device_id = device.id;
				var requires = (device.relationship == "required") ? true : false;
				var requires_unit_count = device.requireUnitCount;
				var row_class = "";
				if(row_type == "odd") {
					row_class = " class=\"odd\"";
				}
				var checked = "";
				var initial_disable = " disabled=\"disabled\"";
				var hidden = "";
				var total_displayed = " style=\"display:none;\"";
				var unit_displayed = " style=\"display:none;\"";
				if(requires) {
					checked = " checked=\"checked\" disabled=\"disabled\"";
					initial_disable = "";
				} else {
					hidden = " style=\"display:none;\"";
				}
				
				if(requires_unit_count) {
					unit_displayed = " style=\"display:inline-block;\"";
				} else {
					total_displayed = " style=\"display:inline-block;\"";
				}
				
				var child_name = "";
				var parent_checkbox_class = "";
				var col_width = " style=\"width:44%;\"";
				if(parent_id) {
					child_name = "";
				} else {
					parent_checkbox_class = " parent-checkbox";
				}
				
				if(requires) {
					output += "<tr" + row_class + child_name + ">";
				} else {
					if(row_type == "odd") {
						output += "<tr class=\"odd disabled-row\"" + child_name + ">";
					} else {
						output += "<tr class=\"disabled-row\"" + child_name + ">";
					}
				}
				
				output += "<td><input type=\"checkbox\"" + checked + " class=\"child-device-enabled" + parent_checkbox_class + "\" data-id=\"" + device_id + "\" /></td>";
				output += "<td" + col_width + ">" + device.description  + "<div style=\"font-size:0.8em;\">" + device.partNumber + "</span>";
				output += "<input type=\"hidden\" name=\"child-device-id\" value=\"" + device_id + "\" />";
				output += "<input type=\"hidden\" name=\"child-device-part-number\" value=\"" + device.partNumber + "\" />";
				output += "<input type=\"hidden\" name=\"child-device-description\" value=\"" + device.description + "\" />";
				output += "<input type=\"hidden\" name=\"child-service-id\" value=\"" + device.defaultOspId + "\" />";
				output += "<input type=\"hidden\" name=\"child-default-osp-id\" value=\"" + device.defaultOspId + "\" />";
				output += "</td>";
				if(requires_unit_count) {
					output += "<td><span class=\"field-symbol\"></span><span class=\"required-ind\"></span><input type=\"text\" size=\"3\" value=\"1\" name=\"child-quantity\" disabled=\"disabled\" class=\"integer keep-disabled\" /></td>";
					output += "<td><span class=\"field-symbol\"></span><span class=\"required-ind required\"></span><input type=\"text\" size=\"3\" class=\"integer unit-count-calculator-field\" name=\"child-unit-count\"" + initial_disable + " /></td>";
					output += "<td><a href=\"javascript:;\" data-view=\"unit-cost\" alt=\"Click to toggle between unit cost and total cost.\" title=\"Click to toggle between unit cost and total cost.\" class=\"uc-calculator-btn\"" + hidden + "><i class=\"fa fa-calculator\"></i>Total</a></td>";
				} else {
					output += "<td><span class=\"field-symbol\"></span><span class=\"required-ind required\"></span><input type=\"text\" size=\"3\" value=\"1\" class=\"integer\" name=\"child-quantity\"" + initial_disable + " /></td>";
					output += "<td><span class=\"field-symbol\"></span><span class=\"required-ind\"></span><input type=\"text\" size=\"3\" disabled=\"disabled\" name=\"child-unit-count\" class=\"keep-disabled integer unit-cost-calculator-field\" /></td>";
					output += "<td>&nbsp;</td>";
				}
				
				output += "<td>";
				output += "<div class=\"total-price-view\"" + total_displayed + "><span class=\"field-symbol\">$</span><span class=\"required-ind required\"></span><input type=\"text\" size=\"6\" class=\"currency\" name=\"child-onetime-cost\"" + initial_disable + "  /></div>";
				output += "<div class=\"unit-price-view\"" + unit_displayed + "><label>Unit Price</label><div><span class=\"field-symbol\">$</span><span class=\"required-ind required\"></span><input type=\"text\" size=\"7\" class=\"currency unit-cost-calculator-field\" name=\"child-onetime-unit-cost\"" + initial_disable + "  /></div><div class=\"unit-price-total-row\">Total: <span class=\"unit-cost-total\"></span></div></div>";
				output += "</td>";
				output += "<td>";
				output += "<div class=\"total-price-view\"" + total_displayed + "><span class=\"field-symbol\">$</span><span class=\"required-ind required\"></span><input type=\"text\" size=\"6\" class=\"currency\" name=\"child-recurring-cost\"" + initial_disable + "  /></div>";
				output += "<div class=\"unit-price-view\"" + unit_displayed + "><label>Unit Price</label><div><span class=\"field-symbol\">$</span><span class=\"required-ind required\"></span><input type=\"text\" size=\"7s\" class=\"currency unit-cost-calculator-field\" name=\"child-recurring-unit-cost\"" + initial_disable + "  /></div><div class=\"unit-price-total-row\">Total: <span class=\"unit-cost-total\"></span></div></div>";
				output += "</td>";
				output += "</tr>";
				return output;
			},
			setup_ci_name_fields:function() {
				var quantity = $(CONTRACT.contract_services.add.contract_quantity_field).val();
				$(CONTRACT.contract_services.add.ci_name_container).html("");
				
				for(var i=1; i <= quantity; i++) {
					var $clone = $(CONTRACT.contract_services.add.ci_name_clone).clone();
					$clone.attr("id", "").css("display","block");
					$clone.find("label").append(" #" + i);
					$(CONTRACT.contract_services.add.ci_name_container).append($clone);
				}
			},
			submit_services:function() {
				UTIL.clear_message_in_popup(CONTRACT.add_contract_service_dialog);
				
				var service_id = $(CONTRACT.contract_services.add.contract_service_item_field).val();
				var one_time_cost = $(CONTRACT.contract_services.add.contract_one_time_cost_field).val();
				var recurring_cost = $(CONTRACT.contract_services.add.contract_recurring_cost_field).val();
				var quantity = $(CONTRACT.contract_services.add.contract_quantity_field).val();
				var start_date = $(CONTRACT.contract_services.add.contract_start_date_field).val();
				var end_date = $(CONTRACT.contract_services.add.contract_end_date_field).val();
				var notes = $(CONTRACT.contract_services.add.contract_notes_field).val();
				var pcr_id = $(CONTRACT.contract_services.add.contract_pcr_field).val();
				var part_number = $(CONTRACT.contract_services.add.contract_part_number_field).val();
				var part_description = $(CONTRACT.contract_services.add.contract_part_description_field).val();
				var contract_group_id = $(CONTRACT.contract_services.add.contract_group_field).val();
				var device_unit_count = $(CONTRACT.contract_services.add.contract_device_unit_count_field).val();
				var status = $(CONTRACT.contract_services.add.contract_status_field).val();
				var location_id = $(CONTRACT.contract_services.add.contract_location_field).val();
				if(contract_group_id == "#") contract_group_id = "";
				var services = [];
				
				if(pcr_id == "#") pcr_id = "";
				var start_moment = moment(start_date, CONTRACT.date_selectors.moment_format);
				var end_moment = moment(end_date, CONTRACT.date_selectors.moment_format);
				start_date = UTIL.convert_dates_for_server(start_date);
				end_date = UTIL.convert_dates_for_server(end_date);
				
				//validate
				if(!service_id || !one_time_cost || !recurring_cost || !quantity || !start_date || !end_date) {
					UTIL.add_error_message_to_popup(CONTRACT.add_contract_service_dialog, $(CONTRACT.general_validation_error).val());
					return false;
				} else if(!part_number && !part_description) {
					UTIL.add_error_message_to_popup(CONTRACT.add_contract_service_dialog, $(CONTRACT.contract_services.edit.part_identifier_required_msg).val());
					return false;
				} 
				
				// validate start date & if end date is valid then validate it is not before the start date
				if ((!start_moment.isValid()) || ((end_date) && (!end_moment.isValid()))) {
					UTIL.add_error_message_to_popup(CONTRACT.add_contract_service_dialog,$(CONTRACT.contract_start_date_validation_msg).val());
					return false;
				} else if ((end_moment.isValid()) && (end_moment.isBefore(start_moment))) {
					UTIL.add_error_message_to_popup(CONTRACT.add_contract_service_dialog,$(CONTRACT.contract_end_before_start_date_validation_msg).val());
					return false;
				}
				
				if(quantity > 200) {
					UTIL.add_error_message_to_popup(CONTRACT.add_contract_service_dialog, $(CONTRACT.contract_services.add.contract_service_quantity_max_msg).val());
					return false;
				}

				//validate the device exists
				var device_id = CONTRACT.contract_services.get_device_id(part_number, part_description);
				if(!device_id) {
					UTIL.add_error_message_to_popup(CONTRACT.add_contract_service_dialog, $(CONTRACT.contract_services.add.contract_device_validation_msg).val());
					return false;
				}
				
				var require_unit_count = CONTRACT.contract_services.get_device_require_unit_count(device_id);
				if(require_unit_count && !device_unit_count) {
					UTIL.add_error_message_to_popup(CONTRACT.add_contract_service_dialog, "Unit Count is required for this device type.");
					return false;
				}
				
				//validate that service dates fall within contract date range
				var contract_start_moment = moment($(CONTRACT.date_selectors.contract_start_date).val(), CONTRACT.date_selectors.moment_format);
				var contract_end_moment = moment($(CONTRACT.date_selectors.contract_end_date).val(), CONTRACT.date_selectors.moment_format);
				if(start_moment.isBefore(contract_start_moment) || end_moment.isAfter(contract_end_moment)) {
					UTIL.add_error_message_to_popup(CONTRACT.add_contract_service_dialog, $(CONTRACT.contract_services.add.contract_dates_validation_msg).val());
					return false;
				}
				
				var ci_names = [];
				var $ci_names = $(CONTRACT.contract_services.add.ci_name_container).find(CONTRACT.contract_services.add.contract_ci_name_field).each(function() {
					ci_names.push($(this));
				});
				
				
				//related line items
				var related = [];
				var child_validation_error = false;
				var child_validation_error_msg = "";
				$(CONTRACT.contract_services.add.related_lineitems_table + " > tbody > tr").each(function() {
					var $row = $(this);
					
					if(!$row.hasClass(CONTRACT.contract_services.add.child_row_class)) {
						var checked = $row.find("input[type='checkbox']").prop("checked");
						
						if(checked) {
							var grandchild_json = null;
							if($row.next().hasClass(CONTRACT.contract_services.add.child_row_class)) {
								//grandchild items. process these first
								var $gc_table = $row.next().find(CONTRACT.contract_services.add.grandchild_lineitems_table);
								grandchild_json = [];
								
								$gc_table.find("tbody tr").each(function() {
									var $gc_row = $(this);
									var gc_checked = $gc_row.find("input[type='checkbox']").prop("checked");
									
									if(gc_checked) {
										var gchild_device_id = $gc_row.find(CONTRACT.contract_services.add.related_lineitem_device_id_field).val();
										var gchild_device_description = $gc_row.find(CONTRACT.contract_services.add.related_lineitem_device_description_field).val();
										var gchild_device_part_number = $gc_row.find(CONTRACT.contract_services.add.related_lineitem_device_part_number_field).val();
										var gchild_service_id = $gc_row.find(CONTRACT.contract_services.add.related_lineitem_service_id_field).val();
										var gchild_default_osp_id = $gc_row.find(CONTRACT.contract_services.add.related_lineitem_default_osp_id_field).val();
										var gchild_quantity = $gc_row.find(CONTRACT.contract_services.add.related_lineitem_quantity_field).val();
										var gchild_unit_count = $gc_row.find(CONTRACT.contract_services.add.related_lineitem_unit_count_field).val();
										var gchild_onetime_cost = $gc_row.find(CONTRACT.contract_services.add.related_lineitem_onetime_cost_field).val();
										var gchild_recurring_cost = $gc_row.find(CONTRACT.contract_services.add.related_lineitem_recurring_cost_field).val();
										
										if(!gchild_quantity || !gchild_onetime_cost || !gchild_recurring_cost) {
											child_validation_error_msg = "Please enter all required fields for grandchild line item: " + gchild_device_description + ".";
											child_validation_error = true;
											return false;
										}
										
										var gchild_require_unit_count = CONTRACT.contract_services.get_device_require_unit_count(child_device_id);
										if(gchild_require_unit_count && !gchild_unit_count) {
											child_validation_error_msg = "Unit Count is required for the grandchild line item: " + gchild_device_description + ". Please enter all required fields for grandchild line items.";
											child_validation_error = true;
											return false;
										}
										
										var gchild_json = { "operation":"create", "contractId":CONTRACT.contract_id, "contractGroupId":contract_group_id, "contractUpdateId":pcr_id, "onetimeRevenue":gchild_onetime_cost, "recurringRevenue":gchild_recurring_cost, "startDate":start_date, "endDate":end_date, "note":null, "devicePartNumber":gchild_device_part_number, "deviceDescription":gchild_device_description, "deviceId":gchild_device_id, "deviceUnitCount":gchild_unit_count, "status":status, "locationId":location_id, "defaultOspId":gchild_default_osp_id };
										
										for(var j=1; j <= gchild_quantity; j++) {
											grandchild_json.push(gchild_json);
										}
									}
								});
							}
							
							
							var child_device_id = $row.find(CONTRACT.contract_services.add.related_lineitem_device_id_field).val();
							var child_device_description = $row.find(CONTRACT.contract_services.add.related_lineitem_device_description_field).val();
							var child_device_part_number = $row.find(CONTRACT.contract_services.add.related_lineitem_device_part_number_field).val();
							var child_service_id = $row.find(CONTRACT.contract_services.add.related_lineitem_service_id_field).val();
							var child_default_osp_id = $row.find(CONTRACT.contract_services.add.related_lineitem_default_osp_id_field).val();
							var child_quantity = $row.find(CONTRACT.contract_services.add.related_lineitem_quantity_field).val();
							var child_unit_count = $row.find(CONTRACT.contract_services.add.related_lineitem_unit_count_field).val();
							var child_onetime_cost = $row.find(CONTRACT.contract_services.add.related_lineitem_onetime_cost_field).val();
							var child_recurring_cost = $row.find(CONTRACT.contract_services.add.related_lineitem_recurring_cost_field).val();
							
							if(!child_quantity || !child_onetime_cost || !child_recurring_cost) {
								child_validation_error_msg = "Please enter all required fields for child line item: " + child_device_description + ".";
								child_validation_error = true;
								return false;
							}
							
							var child_require_unit_count = CONTRACT.contract_services.get_device_require_unit_count(child_device_id);
							if(child_require_unit_count && !child_unit_count) {
								child_validation_error_msg = "Unit Count is required for the child line item: " + child_device_description + ". Please enter all required fields for child line items.";
								child_validation_error = true;
								return false;
							}
							
							var child_json = { "operation":"create", "contractId":CONTRACT.contract_id, "contractGroupId":contract_group_id, "contractUpdateId":pcr_id, "onetimeRevenue":child_onetime_cost, "recurringRevenue":child_recurring_cost, "startDate":start_date, "endDate":end_date, "note":null, "devicePartNumber":child_device_part_number, "deviceDescription":child_device_description, "deviceId":child_device_id, "deviceUnitCount":child_unit_count, "status":status, "locationId":location_id, "defaultOspId":child_default_osp_id };
							if(grandchild_json) {
								child_json["relatedLineItems"] = grandchild_json;
							}
							
							for(var k=1; k <= child_quantity; k++) {
								related.push(child_json);
							}
						}
					}
				});
				
				if(child_validation_error) {
					UTIL.add_error_message_to_popup(CONTRACT.add_contract_service_dialog, child_validation_error_msg);
					return false;
				}
				
				for(var i=1; i <= quantity; i++) {
					var json = {"serviceId":service_id, "operation":"create", "contractId":CONTRACT.contract_id, "contractGroupId":contract_group_id, "contractUpdateId":pcr_id, "onetimeRevenue":one_time_cost, "recurringRevenue":recurring_cost, "startDate":start_date, "endDate":end_date, "note":notes, "devicePartNumber":part_number, "deviceDescription":part_description, "deviceId":device_id, "deviceUnitCount":device_unit_count, "status":status, "locationId":location_id, relatedLineItems:related };
					
					if(ci_names && ci_names.length> 0) {
						var $ci_name = ci_names[i - 1];
						if($ci_name.val()) {
							json["deviceName"] = $ci_name.val();
						}
					}
					
					json = UTIL.remove_null_properties(json);
					services.push(json);
				}
				
				UTIL.add_dialog_loader(CONTRACT.add_contract_service_dialog);
				
				$.ajax ({
					url: PAGE_CONSTANTS.BASE_URL + "contractservices.json?batch",
					data: JSON.stringify(services),
					type: "POST",
					success:function(data) {
						UTIL.remove_dialog_loader(CONTRACT.add_contract_service_dialog);
						var status = data.status;
						if(status == PAGE_CONSTANTS.OK_STS) {	
							//we may want to loop through the indidivual responses and see if any errors occurred in adding the objects
							var success_msg = $(CONTRACT.contract_services.add.contract_create_success_msg).val();
							UTIL.add_success_message_to_popup(CONTRACT.add_contract_service_dialog, success_msg);
							$(CONTRACT.add_contract_service_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").hide();
							$(CONTRACT.add_contract_service_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button:contains('Add Another')").show();
							$(CONTRACT.add_contract_service_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button:contains('OK')").show();
							CONTRACT.contract_services.get_contract_services(CONTRACT.contract_id);
							//CONTRACT.contract_services.setup_autocompletes();
						} else if (status == PAGE_CONSTANTS.ERRR_STS) {
							var error_msg = data.message;
							if(data.batchResults && data.batchResults.length > 0) {
								error_msg = data.batchResults[0].message;
							}
							UTIL.add_error_message_to_popup(CONTRACT.add_contract_service_dialog, error_msg);
						}
					}
				});
			}
		},
		parent_map:{
			map_parent_link:".map-contract-service-to-parent-popup-link",
			map_parent_dialog:"#map-contract-service-to-parent-dialog",
			map_parent_ci_field:"#map-contract-service-parent",
			current_id:null,
			init:function() {
				CONTRACT.contract_services.parent_map.bind_events();
			},
			bind_events:function() {
				$(CONTRACT.contract_services.parent_map.map_parent_dialog).dialog({
					  autoOpen:false,
					  dialogClass:"contract-dialog",	
				      resizable:false,
				      width:700,
				      height:500,
				      modal:true,
				      title: "Map a Line Item",
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
				        	CONTRACT.contract_services.parent_map.submit_mapping();
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
				
				$(document).on("click", CONTRACT.contract_services.parent_map.map_parent_link, function() {
					var id = $(this).data("id");
					var device_id = $(this).data("device-id");
					(id == null) ? CONTRACT.contract_services.parent_map.current_id = null : CONTRACT.contract_services.parent_map.current_id = id;
					CONTRACT.contract_services.parent_map.reset_popup();
					CONTRACT.contract_services.parent_map.get_parent_options(device_id);
				});
			},
			reset_popup:function() {
				UTIL.clear_message_in_popup(CONTRACT.contract_services.parent_map.map_parent_dialog);
				
				$("#map-contract-service-parent").html("");
			},
			get_parent_options:function(device_id) {
				var view_month_url = "";
				var start_date = $(CONTRACT.date_selectors.current_viewing_start_date).val();
				if(start_date) view_month_url = "/" + moment(start_date, CONTRACT.date_selectors.moment_format).format("MM/YYYY");
				var contract_id = CONTRACT.contract_id;
				
				$.ajax ({
					url: PAGE_CONSTANTS.BASE_URL + "contractservices" + view_month_url + ".json?parents&cid=" + contract_id + "&did=" + device_id,
					type: "GET",
					success:function(data) {
						CONTRACT.contract_services.parent_map.build_dropdown(data);
					}
				});
			},
			build_dropdown:function(contract_services) {
				var options = "<option value=\"\"></option>";
				
				for(var i=0; i < contract_services.length; i++) {
					var contract_service = contract_services[i];
					var ci_name = "[no name]"; 
					if(contract_service.deviceName) {
						ci_name = contract_service.deviceName;
					}
					options += "<option value=\"" + contract_service.id + "\">" + ci_name + " -- " + contract_service.deviceDescription + " (" + contract_service.devicePartNumber + ")</option>";
				}
				
				$("#map-contract-service-parent").html(options);
			},
			submit_mapping:function() {
				UTIL.clear_message_in_popup(CONTRACT.contract_services.parent_map.map_parent_dialog);
				
				var parent_id = $(CONTRACT.contract_services.parent_map.map_parent_ci_field).val();
				var child_id = CONTRACT.contract_services.parent_map.current_id;
				
				if(!parent_id) {
					UTIL.add_error_message_to_popup(CONTRACT.contract_services.parent_map.map_parent_dialog, $(CONTRACT.general_validation_error).val());
					return false;
				}
				
				UTIL.add_dialog_loader(CONTRACT.contract_services.parent_map.map_parent_dialog);
				
				$.ajax ({
					url: PAGE_CONSTANTS.BASE_URL + "contractservices/map.json?parents&cid=" + child_id + "&pid=" + parent_id,
					data: null,
					type: "POST",
					success:function(data) {
						UTIL.remove_dialog_loader(CONTRACT.contract_services.parent_map.map_parent_dialog);
						var status = data.status;
						if(status == PAGE_CONSTANTS.OK_STS) {	
							//we may want to loop through the indidivual responses and see if any errors occurred in adding the objects
							//var success_msg = $(CONTRACT.contract_services.add.contract_create_success_msg).val();
							var success_msg = "Service successfully mapped!";
							UTIL.add_success_message_to_popup(CONTRACT.contract_services.parent_map.map_parent_dialog, success_msg);
							$(CONTRACT.contract_services.parent_map.map_parent_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").hide();
							$(CONTRACT.contract_services.parent_map.map_parent_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button:contains('Add Another')").show();
							$(CONTRACT.contract_services.parent_map.map_parent_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button:contains('OK')").show();
							CONTRACT.contract_services.get_contract_services(CONTRACT.contract_id);
						} else if (status == PAGE_CONSTANTS.ERRR_STS) {
							var error_msg = data.message;
							if(data.batchResults && data.batchResults.length > 0) {
								error_msg = data.batchResults[0].message;
							}
							UTIL.add_error_message_to_popup(CONTRACT.contract_services.parent_map.map_parent_dialog, error_msg);
						}
					}
				});
			}
		},
		get_contract_services:function(contract_id) {
			UTIL.add_table_loader(CONTRACT.contract_services.contract_services_table);
			CONTRACT.contract_services.changed_month_one_time_cost = 0;
			CONTRACT.contract_services.changed_month_recurring_cost = 0;
			
			var start_date = $(CONTRACT.date_selectors.current_viewing_start_date).val();
			var view_month_url = "";
			var view_group_url = "";
			var ci_view = "&civ=false";
			CONTRACT.contract_services.load_completed = false;
			if(start_date) view_month_url = "/" + moment(start_date, CONTRACT.date_selectors.moment_format).format("MM/YYYY");
			if(CONTRACT.contract_groups.current_view_id != null) view_group_url = "&cgid=" + CONTRACT.contract_groups.current_view_id;
			var status = "&sts=active";
			if(CONTRACT.contract_services.current_view == CONTRACT.contract_services.display_views.pending) {
				status = "&sts=pending";
			} else if(CONTRACT.contract_services.current_view == CONTRACT.contract_services.display_views.do_not_bill) {
				status = "&sts=donotbill";
			}
			
			if(CONTRACT.contract_services.current_view == CONTRACT.contract_services.display_views.ci) {
				$(CONTRACT.contract_services.ci_name_column).show();
				ci_view = "&civ=true";
			} else {
				$(CONTRACT.contract_services.ci_name_column).hide();
			}
			
			if(CONTRACT.contract_services.current_view != CONTRACT.contract_services.display_views.changed) {
				$.ajax ({
					url: PAGE_CONSTANTS.BASE_URL + "contractservices" + view_month_url + ".json?rollup&cid=" + contract_id + view_group_url + status + ci_view,
					type: "GET",
					success:function(data) {
						var contract_services = data.contractRollupRecords;
						CONTRACT.contract_services.current_month_cache = contract_services;
						CONTRACT.contract_services.load_completed = true;
						$(CONTRACT.contract_services.contract_services_table).find("tbody").html("");
						CONTRACT.contract_services.build_contract_services(contract_services, false);
						CONTRACT.contract_invoice.set_status(data.contractInvoice);
					}
				});
			} else {
				if(start_date) {
					$.ajax ({
						url: PAGE_CONSTANTS.BASE_URL + "contractservices" + view_month_url + ".json?summary&cid=" + contract_id + view_group_url + status,
						type: "GET",
						success:function(data) {
							$(CONTRACT.contract_services.contract_services_table).find("tbody").html("");
							CONTRACT.contract_services.setup_changed_view(data.previousMonth, data.removed, data.added, data.onetimeDifference, data.recurringDifference);
							CONTRACT.contract_invoice.set_status(data.contractInvoice);
						}
					});
				} else {
					$(CONTRACT.contract_services.contract_services_table).find("tbody").html("<tr><td colspan=\"9\" class=\"no-results\">Changed view is not available for the All Time timeframe. Please select a specific month above.</td></tr>");
				}
			}
		},
		setup_changed_view:function(prev_month_data, removed, added, onetime_difference, recurring_difference) {
			CONTRACT.contract_services.build_contract_services(added, false, true);
			CONTRACT.contract_services.build_contract_services(prev_month_data, true);
			CONTRACT.contract_services.add_deleted_rows(removed);
			CONTRACT.contract_services.add_pro_rows(added, "added");
			CONTRACT.contract_services.add_pro_rows(removed, "removed");
			CONTRACT.contract_services.add_difference_row(onetime_difference, recurring_difference);
		},
		add_pro_rows:function(contract_services, row_class) {
			var output = "";
			var recurring_total = CONTRACT.contract_services.current_month_recurring_cost;
			for(var i=0; i<contract_services.length; i++) {
				var contract_service = contract_services[i];
				if(contract_service.isProRatedAmount) {
					output += CONTRACT.contract_services.build_contract_service(contract_service, row_class, true);
					if(row_class == "added") {
						recurring_total += contract_service.recurringRevenue;
					} else if (row_class == "removed") {
						recurring_total -= contract_service.recurringRevenue;
					}
				}
			}
			CONTRACT.contract_services.current_month_recurring_cost = recurring_total;
			$(CONTRACT.contract_services.recurring_total).html(accounting.formatMoney(recurring_total));
			$(CONTRACT.contract_services.contract_services_table).find("tbody").append(output);
		},
		add_deleted_rows:function(contract_services) {
			var output = "";
			var one_time_total = CONTRACT.contract_services.current_month_one_time_cost;
			var recurring_total = CONTRACT.contract_services.current_month_recurring_cost;
			for(var i=0; i < contract_services.length; i++) {
				var record = contract_services[i];
				var end_moment = moment(UTIL.convert_dates_for_ui(record.endDate), CONTRACT.date_selectors.moment_format);
				var start_viewing_moment = moment($(CONTRACT.date_selectors.current_viewing_start_date).val(), CONTRACT.date_selectors.moment_format).subtract(1, "months");
				var end_viewing_moment = moment($(CONTRACT.date_selectors.current_viewing_end_date).val(), CONTRACT.date_selectors.moment_format).subtract(1, "months").endOf("month");
				
				//should we just allow all these through since we're getting them from the server?
				if(end_moment.isBetween(start_viewing_moment, end_viewing_moment) || end_moment.isSame(end_viewing_moment) || end_moment.isSame(start_viewing_moment)) {
					output += CONTRACT.contract_services.build_contract_service(record, "removed");
					//one_time_total -= record.onetimeRevenue;
					recurring_total -= record.recurringRevenue;
				}
			}
			$(CONTRACT.contract_services.contract_services_table).find("tbody").append(output);
			
			if(output != "") {
				CONTRACT.contract_services.current_month_one_time_cost = one_time_total;
				CONTRACT.contract_services.current_month_recurring_cost = recurring_total;
				
				$(CONTRACT.contract_services.one_time_total).html(accounting.formatMoney(one_time_total));
				$(CONTRACT.contract_services.recurring_total).html(accounting.formatMoney(recurring_total));
			}
		},
		add_difference_row:function(onetime_difference, recurring_difference) {
			var onetime_class = "";
			var recurring_class = "";
			var onetime_icon = "";
			var recurring_icon = "";
			
			if(onetime_difference > 0) {
				onetime_class = "up";
				onetime_icon = "<i class=\"fa fa-arrow-up\"></i>";
			} else if(onetime_difference < 0) {
				onetime_class = "down";
				onetime_icon = "<i class=\"fa fa-arrow-down\"></i>";
			}
			if(recurring_difference > 0) {
				recurring_class = "up";
				recurring_icon = "<i class=\"fa fa-arrow-up\"></i>";
			} else if(recurring_difference < 0) {
				recurring_class = "down";
				recurring_icon = "<i class=\"fa fa-arrow-down\"></i>";
			}
			
			var footer_output = "";
			footer_output += "<tr class=\"diff-row\">";
			footer_output += "<td  colspan=\"7\" class=\"right\">Difference from Previous Month</td>";
			footer_output += "<td class=\"right " + onetime_class + "\">" + onetime_icon + accounting.formatMoney(onetime_difference) + "</td>";
			footer_output += "<td class=\"right " + recurring_class + "\">" + recurring_icon + accounting.formatMoney(recurring_difference) + "</td>";
			footer_output += "</tr>";
		
			$(CONTRACT.contract_services.contract_services_table).find("tfoot").append(footer_output);
		},
		build_contract_services:function(contract_services, is_prev_month, is_changed_added_rows) {
			CONTRACT.contract_services.changed_collapsed_rows = "";
			var output = "";
			var changed_group_output = "";
			var unchanged_one_time_total = 0;
			var unchanged_recurring_total = 0;
			var one_time_total = 0;
			var recurring_total = 0;
			var start_viewing_moment = moment($(CONTRACT.date_selectors.current_viewing_start_date).val(), CONTRACT.date_selectors.moment_format);
			var end_viewing_moment = moment($(CONTRACT.date_selectors.current_viewing_end_date).val(), CONTRACT.date_selectors.moment_format);
			if(contract_services.length > 0) {
				for(var i=0;i<contract_services.length;i++) {
					var contract_service = contract_services[i];
					var one_time_cost = contract_service.onetimeRevenue;
					var recurring_cost = contract_service.recurringRevenue;
					
					//build out row data
					var output_holder = CONTRACT.contract_services.build_contract_service(contract_service,"");
					
					if(CONTRACT.contract_services.current_view == CONTRACT.contract_services.display_views.changed && CONTRACT.contract_services.current_row_class != "added" && $(CONTRACT.date_selectors.date_dropdown).val() != "#") {
						
					} else {
						output += output_holder;
					}
					
					if(CONTRACT.contract_services.current_view == CONTRACT.contract_services.display_views.changed && $(CONTRACT.date_selectors.date_dropdown).val() != "#" && is_prev_month) {
						CONTRACT.contract_services.changed_collapsed_rows += output_holder;
						unchanged_one_time_total += one_time_cost;
						unchanged_recurring_total += recurring_cost;
					}
					
					
					if(CONTRACT.contract_services.current_view == CONTRACT.contract_services.display_views.changed) {
						if(is_prev_month) {
							//CONTRACT.contract_services.changed_month_one_time_cost += one_time_cost;
							CONTRACT.contract_services.changed_month_recurring_cost += recurring_cost;
						} else if(CONTRACT.contract_services.current_row_class == "added") {
							CONTRACT.contract_services.changed_month_one_time_cost += one_time_cost;
							CONTRACT.contract_services.changed_month_recurring_cost += recurring_cost;
						}
					}
					
					one_time_total += one_time_cost;
					recurring_total += recurring_cost;
				}
				
				changed_group_output = "";
				if(CONTRACT.contract_services.current_view == CONTRACT.contract_services.display_views.changed && CONTRACT.contract_services.changed_collapsed_rows != "" && $(CONTRACT.date_selectors.date_dropdown).val() != "#" && is_prev_month) {
					changed_group_output += "<tr class=\"total-row\">";
					changed_group_output += "<td colspan=\"7\"><a href=\"javascript:;\" class=\"" + CONTRACT.contract_services.unchanged_activity_class + "\"><i class=\"fa fa-chevron-circle-right\"></i>Previous Month's MRC</a></td>";
					changed_group_output += "<td class=\"right\">" + accounting.formatMoney(unchanged_one_time_total) + "</td>";
					changed_group_output += "<td class=\"right\">" + accounting.formatMoney(unchanged_recurring_total) + "</td>";
					changed_group_output += "</tr>";
				}
				
				if(CONTRACT.contract_services.current_view == CONTRACT.contract_services.display_views.changed) {
					CONTRACT.contract_services.current_month_one_time_cost = CONTRACT.contract_services.changed_month_one_time_cost;
					CONTRACT.contract_services.current_month_recurring_cost = CONTRACT.contract_services.changed_month_recurring_cost;
				} else {
					CONTRACT.contract_services.current_month_one_time_cost = one_time_total;
					CONTRACT.contract_services.current_month_recurring_cost = recurring_total;
				}
				
				var footer_output = "";
				footer_output += "<tr class=\"total-row\">";
				if(CONTRACT.contract_services.current_view == CONTRACT.contract_services.display_views.ci) {
					footer_output += "<td colspan=\"7\">&nbsp;</td>";
				} else {
					footer_output += "<td colspan=\"6\">&nbsp;</td>";
				}
				footer_output += "<td class=\"right\">TOTAL</td>";
				footer_output += "<td class=\"right\" id=\"one-time-total\">" + accounting.formatMoney(CONTRACT.contract_services.current_month_one_time_cost) + "</td>";
				footer_output += "<td class=\"right\" id=\"recurring-total\">" + accounting.formatMoney(CONTRACT.contract_services.current_month_recurring_cost) + "</td>";
				footer_output += "</tr>";
			} else {
				if(!is_changed_added_rows) {
					output = "<tr><td colspan=\"10\" class=\"no-results\">" + $(CONTRACT.contract_services.contract_services_no_results).val() + "</td></tr>";
					footer_output = "";
				}
			}
			$(CONTRACT.contract_services.contract_services_table).find("tbody").prepend(changed_group_output + output);
			$(CONTRACT.contract_services.contract_services_table).find("tfoot").html(footer_output);
		},
		build_contract_service:function(contract_service, default_row_class, is_prorated, is_prev_month) {
			CONTRACT.contract_services.current_row_class = default_row_class;
			var name = contract_service.name;
			var start_date = UTIL.convert_dates_for_ui(contract_service.startDate);
			var end_date = UTIL.convert_dates_for_ui(contract_service.endDate);
			var one_time_cost = contract_service.onetimeRevenue;
			var recurring_cost = contract_service.recurringRevenue;
			var code = contract_service.devicePartNumber;
			var description = contract_service.deviceDescription;
			var start_moment = moment(start_date, CONTRACT.date_selectors.moment_format);
			var end_moment = moment(end_date, CONTRACT.date_selectors.moment_format);
			var start_viewing_moment = moment($(CONTRACT.date_selectors.current_viewing_start_date).val(), CONTRACT.date_selectors.moment_format);
			var end_viewing_moment = moment($(CONTRACT.date_selectors.current_viewing_end_date).val(), CONTRACT.date_selectors.moment_format);
			var quantity = contract_service.quantity;
			var changed_class = "";
			var icon = "";
			var message = "";
			if(code == null) code = "";
			if(description == null) description = "";
			if(quantity == null) quantity = "";
			
			if(start_moment.isBetween(start_viewing_moment, end_viewing_moment) || start_moment.isSame(start_viewing_moment) || (is_prorated && default_row_class == "added")) {
				CONTRACT.contract_services.current_row_class = "added";
				icon = "<i class=\"fa fa-plus-circle\"></i>";
				if(is_prorated) {
					message = " title=\"This service started last month at a prorated amount. This adjustment accounts for the amount changed last month versus this month.\"";
				} else {
					message = " title=\"This service STARTS this month.\"";
				}
			} else if((end_moment.isBetween(start_viewing_moment, end_viewing_moment) || end_moment.isSame(end_viewing_moment)) && !is_prorated) {
				CONTRACT.contract_services.current_row_class = "ended";
				icon = "<i class=\"fa fa-times-circle\"></i>";
				message = " title=\"This service ENDS this month.\"";
			} else if(default_row_class == "removed") {
				icon = "<i class=\"fa fa-times-circle\"></i>";
				if(is_prorated) {
					message = " title=\"This service ends this month at a prorated amount. This adjustment accounts for the amount changed last month versus this month.\"";
				} else {
					message = " title=\"This service ended LAST month.\"";
				}
			}
			
			if(CONTRACT.contract_services.current_view == CONTRACT.contract_services.display_views.changed && CONTRACT.contract_services.current_row_class != "added" && CONTRACT.contract_services.current_row_class != "removed" && $(CONTRACT.date_selectors.date_dropdown).val() != "#" && !is_prorated) {
				changed_class = " changed-collapsed";
			}
			
			var units = "";
			if(contract_service.unitCount > 0) {
				units = " <div style=\"font-size:0.8em;\">(" + contract_service.unitCount + " Units)</div>";
			}
			
			var output_holder = "";
			output_holder += "<tr class=\"" + CONTRACT.contract_services.current_row_class + changed_class + "\"" + message + ">";
			if(CONTRACT.contract_services.current_view == CONTRACT.contract_services.display_views.ci) {
				var ci_name = "";
				if(contract_service.rowType == "ContractService") {
					if(contract_service.deviceName) {
						ci_name = contract_service.deviceName;
					}
				}
				
				var device_id = contract_service.deviceId;
				var allowed_as_child = CONTRACT.contract_services.get_device_has_parent_in_contract(device_id);
				if(allowed_as_child) {
					ci_name += " <a href=\"javascript:;\" class=\"popup-link map-contract-service-to-parent-popup-link\" data-dialog=\"map-contract-service-to-parent-dialog\" style=\"font-size:0.8em;\" data-id=\"" + contract_service.id + "\" data-device-id=\"" + device_id + "\"><i class=\"fa fa-link\"></i>Map to Parent</a>";
				}
				output_holder += "<td>" + ci_name + "</td>";
			}
			
			if(contract_service.rowType == "ContractService") {
				output_holder += "<td><a href=\"javascript:;\" class=\"edit-contract-services-popup-link\" data-dialog=\"edit-contract-service-dialog\" data-id=\"" + contract_service.id + "\" data-service-id=\"" + contract_service.serviceId + "\" data-service-name=\"" + name + "\" data-start-date=\"" + start_date + "\" data-end-date=\"" + end_date + "\" data-device-id=\"" + contract_service.deviceId + "\">"  + icon + name + "</a></td>";
			} else if(contract_service.rowType == "ContractAdjustment") {
				var adjustment_type = CONTRACT.contract_adjustment.onetime;
				if(recurring_cost != null) {
					adjustment_type = CONTRACT.contract_adjustment.recurring;
				}
				output_holder += "<td><a href=\"javascript:;\" class=\"edit-contract-adjustment-popup-link\" data-dialog=\"edit-contract-adjustment-dialog\" data-id=\"" + contract_service.id + "\" data-type=\"" + adjustment_type + "\" data-start-date=\"" + start_date + "\" data-end-date=\"" + end_date + "\">"  + icon + name + "</a></td>";
			}
			
			output_holder += "<td>" + code + "</td>";
			output_holder += "<td>" + description + "</td>";
			output_holder += "<td>" + CONTRACT.contract_services.display_pcrs(contract_service.contractUpdates) + "</td>";
			output_holder += "<td class=\"center\">" + quantity + units+  "</td>";
			output_holder += "<td class=\"right\">" + start_date + "</td>";
			output_holder += "<td class=\"right\">" + end_date + "</td>";
			output_holder += "<td class=\"right\">" + accounting.formatMoney(one_time_cost) + "</td>";
			output_holder += "<td class=\"right\">" + accounting.formatMoney(recurring_cost) + "</td>";
			output_holder += "</tr>";
			
			return output_holder;
		},
		display_pcrs:function(pcrs) {
			var pcr_display = "";
			for(var k=0; k<pcrs.length; k++) {
				var pcr_name = pcrs[k].altId;
				if(pcr_display.indexOf(pcr_name) == -1) {
					if(pcr_display != "") pcr_display += ", ";
					pcr_display += pcr_name;
				}
			} 
			return pcr_display;
		}
	},
	contract_invoice:{
		is_billing_field:"#is-billing",
		is_billing_role:false,
		status_container:"#contract-invoice-status",
		statuses:{ active:"active", readyToInvoice:"readyToInvoice", invoiced:"invoiced" },
		status:"#current-status",
		status_loader:"#status-loader",
		update_status_link:".update-contract-invoice",
		current_status:null,
		init:function() {
			CONTRACT.contract_invoice.set_variables();
			CONTRACT.contract_invoice.bind_events();
		},
		set_variables:function() {
			if($(CONTRACT.contract_invoice.is_billing_field).val() == "true") CONTRACT.contract_invoice.is_billing_role = true;
		},
		bind_events:function() {
			$(document).on("click", CONTRACT.contract_invoice.update_status_link, function() {
				var $this = $(this);
				var id = $this.data("id");
				var start_date = $this.data("start-date");
				var end_date = $this.data("end-date");
				var status = $this.data("status");
				CONTRACT.contract_invoice.update_invoice(id, status, start_date, end_date);
			});
		},
		set_status:function(contract_invoice) {
			if(contract_invoice != null) {
				var status = CONTRACT.contract_invoice.build_status(contract_invoice);
				$(CONTRACT.contract_invoice.status).html(status);
				$(CONTRACT.contract_invoice.status).show();
			} else {
				$(CONTRACT.contract_invoice.status).hide();
			}
		},
		build_status:function(contract_invoice) {
			var output = "";
			var status = contract_invoice.status;
			var id = contract_invoice.id;
			var start_date = contract_invoice.startDate;
			var end_date = contract_invoice.endDate;
			
			if(moment(start_date, "YYYY-MM-DD").isValid()) {
				start_date = UTIL.convert_dates_for_ui(start_date);
				end_date = UTIL.convert_dates_for_ui(end_date);
			}
			
			if(status == CONTRACT.contract_invoice.statuses.active) {
				output = "<a href=\"javascript:;\" class=\"update-contract-invoice small-cta-btn\" data-status=\"readyToInvoice\" data-id=\"" + id + "\" data-start-date=\"" + start_date + "\" data-end-date=\"" + end_date + "\">Ready to Invoice?</a>";
			} else if(status == CONTRACT.contract_invoice.statuses.readyToInvoice) {
				output = "<i class=\"fa fa-check\"></i>Ready to Invoice";
				var new_status = "active";
				var text = "Undo";
				if(CONTRACT.contract_invoice.is_billing_role) {
					new_status = "invoiced";
					text = "Invoiced?";
				}
				output += "<a class=\"update-contract-invoice small-cta-btn\" href=\"javascript:;\" data-status=\"" + new_status + "\" data-id=\"" + id + "\" data-start-date=\"" + start_date + "\" data-end-date=\"" + end_date + "\">" + text + "</a>";
			} else if(status == CONTRACT.contract_invoice.statuses.invoiced) {
				output = "<i class=\"fa fa-lock\"></i>Invoiced";
				if(CONTRACT.contract_invoice.is_billing_role) {
					output += "<a class=\"update-contract-invoice small-cta-btn\" href=\"javascript:;\" data-status=\"active\" data-id=\"" + id + "\" data-start-date=\"" + start_date + "\" data-end-date=\"" + end_date + "\">Unlock</a>";
				}
			}
			
			CONTRACT.contract_invoice.current_status = status;
			CONTRACT.contract_invoice.toggle_page_locks(status);
			return output;
		},
		toggle_page_locks:function(status) {
			if(status == CONTRACT.contract_invoice.statuses.invoiced) {
				$(CONTRACT.contract_services.display_table).find(".table-links").find(".popup-link").hide();
				$(CONTRACT.contract_services.display_table).find("#locked-msg").show();
			} else {
				$(CONTRACT.contract_services.display_table).find(".table-links").find(".popup-link").show();
				$(CONTRACT.contract_services.display_table).find("#locked-msg").hide();
			}
		},
		update_invoice:function(id, status, start_date, end_date) {
			$(CONTRACT.contract_invoice.status).hide();
			$(CONTRACT.contract_invoice.status_loader).show();
			//UTIL.clear_msg_in_content_box(BILLING.billing_error);
			
			//$(BILLING.billing_loader).show();
			var contract_id = CONTRACT.contract_id;
			var server_start_date = UTIL.convert_dates_for_server(start_date);
			var server_end_date = UTIL.convert_dates_for_server(end_date);
			var json = { id:id, status:status, startDate:server_start_date, endDate:server_end_date, contractId:contract_id };
			
			$.ajax ({
				url: PAGE_CONSTANTS.BASE_URL + "contractinvoices.json",
				data: JSON.stringify(json),
				type: "PUT",
				success:function(data) {
					$(CONTRACT.contract_invoice.status_loader).hide();
					
					if(data.status == PAGE_CONSTANTS.OK_STS) {
						CONTRACT.contract_invoice.set_status(json);
						//$(BILLING.billing_success).show();
						//BILLING.update_rows(status);
						//setTimeout("$(BILLING.billing_success).fadeOut(1500);", 3000);
					} else {
						//UTIL.add_error_msg_to_content_box(BILLING.billing_error, data.message);
					}
				},
				error:function(jqXHR, textStatus, errorThrown) {
					$(CONTRACT.contract_invoice.status_loader).hide();
					/*$(BILLING.billing_loader).hide();
					$(BILLING.results_options).css("display","");
					var json = jqXHR.responseJSON;
					var message = $(msg_general_ajax_error).val();
					if(json && json.status && json.message) {
						var status = json.status;
						if (status == PAGE_CONSTANTS.ERRR_STS) {
							message = json.message;
						}
					}
					UTIL.add_error_msg_to_content_box(BILLING.billing_error, message);
					*/
				}
			});
		}
	},
	pcr:{
		current_pcr_id: null,
		pcr_table:"#pcrs",
		pcr_alt_id_field:"#pcr-alt-id",
		pcr_job_number_field:"#pcr-job-number",
		pcr_ticket_number_field:"#pcr-ticket-number",
		pcr_signed_date_field:"#pcr-signed-date",
		pcr_effective_date_field:"#pcr-effective-date",
		pcr_onetime_price_field:"#pcr-onetime-price",
		pcr_recurring_price_field:"#pcr-recurring-price",
		pcr_notes_field:"#pcr-notes",
		pcr_alt_id_display:".pcr-display-alt-id",
		pcr_job_number_display:".pcr-display-job-number",
		pcr_ticket_number_display:".pcr-display-ticket-number",
		pcr_signed_date_display:".pcr-display-signed-date",
		pcr_effective_date_display:".pcr-display-effective-date",
		pcr_onetime_price_display:".pcr-display-onetime-price",
		pcr_recurring_price_display:".pcr-display-recurring-price",
		pcr_note_display:"input[name='pcr-display-notes']",
		pcr_list_class:".pcr-list",
		pcr_no_results:"#no-pcrs-msg",
		pcr_create_success_msg: "#new-pcr-msg",
		pcr_update_success_msg: "#update-pcr-msg",
		pcr_related_contract_services: "#pcr-related-contract-services",
		init:function() {
			CONTRACT.pcr.bind_events();
			CONTRACT.pcr.get_pcrs();
			CONTRACT.pcr.delete_item.init();
		},
		bind_events:function() {
			$(document).on("click", ".update-pcr", function() {
		    	var id = $(this).data("id");
		    	(id == "add") ? CONTRACT.pcr.current_pcr_id = null : CONTRACT.pcr.current_pcr_id = id;
		    	CONTRACT.pcr.reset_fields();
	    		return false;
	    	});
		},
		load_related_contract_services:function() {
			var pcr_id = CONTRACT.pcr.current_pcr_id;
			
			$.ajax ({
				url: PAGE_CONSTANTS.BASE_URL + "contractservices/updated.json?cuid=" + pcr_id,
				type: "GET",
				success:function(data) {
					CONTRACT.pcr.build_related_contract_services(data);
				}
			});
		},
		build_related_contract_services:function(contract_services) {
			var output = "";
			
			if(contract_services.length > 0) {
				for(var i=0; i<contract_services.length; i++) {
					var obj = contract_services[i];
					output += "<tr>";
					output += "<td>" + obj.name + "</td>";
					output += "<td>" + UTIL.convert_dates_for_ui(obj.startDate) + "</td>";
					output += "<td>" + UTIL.convert_dates_for_ui(obj.endDate) + "</td>";
					//output += "<td><a href=\"javascript:;\" class=\"delete-pcr\">Remove from PCR</a></td>";
					output += "</tr>";
				}
			} else {
				output = "<td colspan=\"3\" class=\"no-results\">No items associated with this PCR.</td>";
			}
			
			$(CONTRACT.pcr.pcr_related_contract_services).find("tbody").html(output);
			$(CONTRACT.pcr.pcr_related_contract_services).show();
		},
		get_pcrs:function() {
			var contract_id = CONTRACT.contract_id;
			
			$.ajax ({
				url: PAGE_CONSTANTS.BASE_URL + "contractupdates.json?cid=" + contract_id,
				type: "GET",
				success:function(data) {
					CONTRACT.pcr.build_pcrs(data);
				}
			});
		},
		build_pcrs:function(pcrs) {
			CONTRACT.pcr.build_pcr_table(pcrs);
			CONTRACT.pcr.popuplate_pcr_dropdowns(pcrs);
		},
		build_pcr_table:function(pcrs) {
			var output = "";
			
			if(pcrs.length > 0) {
				for(var i=0; i<pcrs.length; i++) {
					var pcr = pcrs[i];
					var id = pcr.id;
					var job_number = pcr.jobNumber;
					var ticket_number = pcr.ticketNumber;
					var notes = pcr.note;
					if(job_number == null) job_number = "";
					if(ticket_number == null) ticket_number = "";
					if(notes == null) notes = "";
					var display_notes = notes;
					if(display_notes.length > 75) display_notes = display_notes.substring(0, 75) + "...";
					
					output += "<tr id=\"pcr-" + id + "\">";
					output += "<td>";
					output += "<a href=\"javascript:;\" class=\"popup-link update-pcr pcr-display-alt-id\" data-id=\"" + id + "\" data-dialog=\"pcr-dialog\">" + pcr.altId + "</a>";
					output += $("<input>").attr("type","hidden").attr("name","pcr-display-notes").val(notes).wrap("<span />").parent().html();
					output += "</td>";
					var file_path = "";
					if(pcr.filePath) {
						file_path = "<a href=\"javascript:;\" class=\"contract-download-link\"\" id=\"download-contract-link\" data-type=\"contractupdate\" data-id=\"" + id + "\">Download PCR PDF<i class=\"fa fa-download icon-right\"></i></a> <span style=\"margin:0 7px;\">|</span><span style=\"font-size:0.85em; font-style: italic;\"><a href=\"javascript:;\" class=\"popup-link contract-delete-link\"\" data-type=\"contractupdate\" data-dialog=\"delete-contract-doc-dialog\" data-id=\"" + id + "\"><i class=\"fa fa-minus-circle\"></i>Delete PDF</a></span>";
					} else {
						//file_path = "<a href=\"javascript:;\" class=\"popup-link contract-upload-link\" data-dialog=\"contract-upload-dialog\" id=\"contract-upload-btn\" data-id=\"" + id + "\" data-type=\"contractupdate\">Upload a PCR<i class=\"fa fa-upload icon-right\"></i></a>";
						file_path = "";
					}
					output += "<td>" + file_path + "</td>";
					output += "<td class=\"pcr-display-job-number\">" + job_number + "</td>";
					output += "<td class=\"pcr-display-ticket-number\">" + ticket_number + "</td>";
					output += "<td>" + $("<span>").attr("title",notes).html(display_notes).wrap("<span />").parent().html() + "</td>";
					output += "<td class=\"right pcr-display-signed-date\">" + UTIL.convert_dates_for_ui(pcr.signedDate) + "</td>";
					output += "<td class=\"right pcr-display-effective-date\">" + UTIL.convert_dates_for_ui(pcr.effectiveDate) + "</td>";
					output += "<td class=\"right pcr-display-onetime-price\">" + UTIL.convert_currency_for_ui(pcr.onetimePrice) + "</td>";
					output += "<td class=\"right pcr-display-recurring-price\">" + UTIL.convert_currency_for_ui(pcr.recurringPrice) + "</td>";
					output += "<td class=\"center\"><a href=\"javascript:;\" class=\"popup-link delete-pcr icon-link\" data-dialog=\"delete-pcr-dialog\" data-id=\"" + id + "\" data-name=\"" + pcr.altId + "\"><i class=\"fa fa-minus-circle\"></i>Delete</a></td>";
					output += "</tr>";
				}
			} else {
				output = "<tr><td colspan=\"3\" class=\"no-results\">" + $(CONTRACT.pcr.pcr_no_results).val() + "</td></tr>";
			}
			
			$(CONTRACT.pcr.pcr_table).find("tbody").html(output);
		},
		popuplate_pcr_dropdowns:function(pcrs) {
			var output = "<option value=\"#\">Select a PCR</option>";
			
			for(var i=0; i<pcrs.length; i++) {
				output += "<option value=\"" + pcrs[i].id + "\">" + pcrs[i].altId + "</option>";
			}
			
			$(CONTRACT.pcr.pcr_list_class).html(output);
		},
		reset_fields:function() {
			UTIL.clear_message_in_popup(CONTRACT.pcr_dialog);
			$(CONTRACT.pcr.pcr_related_contract_services).hide();
			
			var alt_id = "";
			var job_number = "";
			var ticket_number = "";
			var signed_date = "";
			var effective_date = "";
			var onetime_price = "";
			var recurring_price = "";
			var notes = "";
			
			if(CONTRACT.pcr.current_pcr_id != null) {
				var $row = $("#pcr-" + CONTRACT.pcr.current_pcr_id);
				alt_id = $row.find(CONTRACT.pcr.pcr_alt_id_display).html();
				job_number = $row.find(CONTRACT.pcr.pcr_job_number_display).html();
				ticket_number = $row.find(CONTRACT.pcr.pcr_ticket_number_display).html();
				signed_date = $row.find(CONTRACT.pcr.pcr_signed_date_display).html();
				effective_date = $row.find(CONTRACT.pcr.pcr_effective_date_display).html();
				notes = $row.find(CONTRACT.pcr.pcr_note_display).val();
				onetime_price = $row.find(CONTRACT.pcr.pcr_onetime_price_display).html();
				onetime_price = UTIL.convert_currency_for_server(onetime_price);
				recurring_price = $row.find(CONTRACT.pcr.pcr_recurring_price_display).html();
				recurring_price = UTIL.convert_currency_for_server(recurring_price);
				CONTRACT.pcr.load_related_contract_services();
			}
			
			$(CONTRACT.pcr.pcr_alt_id_field).val(alt_id);
			$(CONTRACT.pcr.pcr_job_number_field).val(job_number);
			$(CONTRACT.pcr.pcr_ticket_number_field).val(ticket_number);
			$(CONTRACT.pcr.pcr_signed_date_field).val(signed_date);
			$(CONTRACT.pcr.pcr_effective_date_field).val(effective_date);
			$(CONTRACT.pcr.pcr_onetime_price_field).val(onetime_price);
			$(CONTRACT.pcr.pcr_recurring_price_field).val(recurring_price);
			$(CONTRACT.pcr.pcr_notes_field).val(notes);
		},
		submit_pcr:function() {
			UTIL.clear_message_in_popup(CONTRACT.pcr_dialog);
			
			var pcr_alt_id = $(CONTRACT.pcr.pcr_alt_id_field).val();
			var job_number = $(CONTRACT.pcr.pcr_job_number_field).val();
			var ticket_number = $(CONTRACT.pcr.pcr_ticket_number_field).val();
			var signed_date = $(CONTRACT.pcr.pcr_signed_date_field).val();
			var effective_date = $(CONTRACT.pcr.pcr_effective_date_field).val();
			var onetime_price = $(CONTRACT.pcr.pcr_onetime_price_field).val();
			var recurring_price = $(CONTRACT.pcr.pcr_recurring_price_field).val();
			var notes = $(CONTRACT.pcr.pcr_notes_field).val();
			var contract_id = CONTRACT.contract_id;
			var ajax_type = "POST";
			
			signed_date = UTIL.convert_dates_for_server(signed_date);
			effective_date = UTIL.convert_dates_for_server(effective_date);
			onetime_price = UTIL.convert_currency_for_server(onetime_price);
			recurring_price = UTIL.convert_currency_for_server(recurring_price);
			
			//validate
			if(!pcr_alt_id || !signed_date) {
				UTIL.add_error_message_to_popup(CONTRACT.pcr_dialog, $(CONTRACT.general_validation_error).val());
				return false;
			}
			
			var json = { "altId":pcr_alt_id, "contractId":contract_id, "signedDate":signed_date, "note":notes, "jobNumber":job_number, "ticketNumber":ticket_number, "effectiveDate":effective_date, "onetimePrice":onetime_price, "recurringPrice":recurring_price };
			
			if(CONTRACT.pcr.current_pcr_id != null) {
				ajax_type = "PUT";
				json["id"] = CONTRACT.pcr.current_pcr_id;
				//json = { "id":CONTRACT.pcr.current_pcr_id, "contractId":contract_id, "altId":pcr_alt_id, "signedDate":signed_date, "note":notes, "jobNumber":job_number };
			}
			
			//prepare data before submit
			json = UTIL.remove_null_properties(json);
			
			UTIL.add_dialog_loader(CONTRACT.pcr_dialog);
			
			$.ajax ({
				url: PAGE_CONSTANTS.BASE_URL + "contractupdates.json",
				data: JSON.stringify(json),
				type: ajax_type,
				success:function(data) {
					UTIL.remove_dialog_loader(CONTRACT.pcr_dialog);
					
					var success_msg = $(CONTRACT.pcr.pcr_create_success_msg).val();
					if(CONTRACT.pcr.current_pcr_id != null) success_msg = $(CONTRACT.pcr.pcr_update_success_msg).val();
					UTIL.add_success_message_to_popup(CONTRACT.pcr_dialog, success_msg);
					$(CONTRACT.pcr_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").hide();
					$(CONTRACT.pcr_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button:contains('OK')").show();
					CONTRACT.pcr.get_pcrs();
				}
			});
		},
		delete_item:{
			delete_pcr_dialog:"#delete-pcr-dialog",
			delete_pcr_link:".delete-pcr",
			current_id:null,
			delete_pcr_name:"#delete-pcr-name",
			init:function() {
				CONTRACT.pcr.delete_item.bind_events();
			},
			bind_events:function() {
				$(CONTRACT.pcr.delete_item.delete_pcr_dialog).dialog({
					  autoOpen:false,
					  dialogClass:"delete-dialog",	
				      resizable:false,
				      width:720,
				      height:400,
				      modal:true,
				      title: "Delete a PCR",
				      open:function() {
				    	  $('.datepicker').datepicker('enable');
				    	  $(this).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").show();
					      $(this).closest(".ui-dialog").find(".ui-button:contains('OK')").hide();
					      UTIL.clear_message_in_popup(CONTRACT.pcr.delete_item.delete_pcr_dialog);
				      },
				      close:function() {
				    	  $('.datepicker').datepicker('disable');
				      },
				      buttons: {
				        "Delete":function() {
				        	CONTRACT.pcr.delete_item.submit_delete();
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
				
				$(document).on("click", CONTRACT.pcr.delete_item.delete_pcr_link, function() {
					CONTRACT.pcr.delete_item.current_id = $(this).data("id");
					$(CONTRACT.pcr.delete_item.delete_pcr_name).html($(this).data("name"));
				});
			},
			submit_delete:function() {
				UTIL.clear_message_in_popup(CONTRACT.pcr.delete_item.delete_pcr_dialog);
				UTIL.add_dialog_loader(CONTRACT.pcr.delete_item.delete_pcr_dialog);
				
				var url = PAGE_CONSTANTS.BASE_URL + "contractupdates/" + CONTRACT.pcr.delete_item.current_id;
				
				$.ajax ({
					url: url + ".json",
					type: "DELETE",
					success:function(data) {
						UTIL.remove_dialog_loader(CONTRACT.pcr.delete_item.delete_pcr_dialog);
						
						if(data.status == PAGE_CONSTANTS.OK_STS) {
							UTIL.add_success_message_to_popup(CONTRACT.pcr.delete_item.delete_pcr_dialog, "PCR has been successfully deleted.");
							$(CONTRACT.pcr.delete_item.delete_pcr_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").hide();
							$(CONTRACT.pcr.delete_item.delete_pcr_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button:contains('OK')").show();
							
							CONTRACT.pcr.get_pcrs();
							CONTRACT.contract_services.get_contract_services(CONTRACT.contract_id);
						} else {
							UTIL.add_error_message_to_popup(CONTRACT.pcr.delete_item.delete_pcr_dialog, data.message);
						}
					}
				});
			}
		}
	},
	sn_ci:{
		section:"#sn-cis",
		list:"#sn-ci-list",
		sync_link:"#osm-sync",
		optimal_url_field:"#osm-url",
		optimal_url:"https://optimal.service-now.com",
		init:function() {
			CONTRACT.sn_ci.bind_events();
			CONTRACT.sn_ci.read.init();
			if($(CONTRACT.sn_ci.optimal_url_field)) CONTRACT.sn_ci.optimal_url = $(CONTRACT.sn_ci.optimal_url_field).val();
		},
		bind_events:function() {
			$(CONTRACT.sn_ci.sync_link).click(function() {
				CONTRACT.sn_ci.read.refresh_sync();
			});
		},
		read:{
			msg_no_cis_found:"#no-cis-found-msg",
			msg_sn_integration_failed:"#sn-integration-failed-msg",
			msg_loading_cis:"#sn-loading-cis-msg",
			msg_syncing_cis:"#sn-syncing-cis-msg",
			init:function() {
				CONTRACT.sn_ci.read.get_cis();
			},
			bind_events:function() {
				
			},
			refresh_sync:function() {
				$(CONTRACT.sn_ci.list).html("<span class=\"no-results\"><img src=\"" + PAGE_CONSTANTS.LOADER_URL + "\" style=\"margin-right:5px;\"/>" + $(CONTRACT.sn_ci.read.msg_syncing_cis).val() + "</span>");
				
				$.ajax ({
					url: PAGE_CONSTANTS.BASE_URL + "servicenowcis.json?sync&cid=" + CONTRACT.contract_id + "&sid=" + CONTRACT.sn_sys_id,
					type: "GET",
					success:function(data) {
						CONTRACT.sn_ci.read.build_display(data);
					}
				});
			},
			get_cis:function() {
				$(CONTRACT.sn_ci.list).html("<span class=\"no-results\"><img src=\"" + PAGE_CONSTANTS.LOADER_URL + "\" style=\"margin-right:5px;\"/>" + $(CONTRACT.sn_ci.read.msg_loading_cis).val() + "</span>");
				
				$.ajax ({
					url: PAGE_CONSTANTS.BASE_URL + "servicenowcis.json?cid=" + CONTRACT.contract_id,
					type: "GET",
					success:function(data) {
						CONTRACT.sn_ci.read.build_display(data);
					}
				});
			},
			build_display:function(data) {
				var output = "";
				var chunk;
				var size = parseInt(data.length / 5);
				var remainder = parseInt(data.length % 5);
				
				if(data.length > 0) {
					while (data.length > 0) {
						var skip_chunk = false;
						if(data.length > 4) {
							chunk = data.splice(0, size + remainder);
							remainder = 0;
						} else {
							skip_chunk = true;
							chunk = data;
						}
						output += "<ul>";
						for(var i=0; i < chunk.length; i++) {
							var ci = chunk[i];
							var icon = "<i class=\"ci-status-icon fa fa-question-circle\"></i>";
							var matched = false;
							if(ci.contractServiceId) {
								matched = true;
								icon = "<i class=\"ci-status-icon fa fa-check-circle\"></i>";
							}
							if(matched) {
								output += "<li class=\"matched\">";
							} else {
								output += "<li>";
							}
							output += icon;
							output += "<span class=\"ci-name\">" + ci.name + "<a href=\"" + CONTRACT.sn_ci.optimal_url + "/nav_to.do?uri=cmdb_ci.do?sys_id=" + ci.serviceNowSysId + "\" target=\"_blank\"><i class=\"fa fa-external-link-square-alt icon-right\"></i></a></span>";
							output += "</li>";
						}
						output += "</ul>";
						if(skip_chunk) data = [];
					}
				} else {
					if(CONTRACT.sn_sys_id) {
						output = "<span class=\"no-results\">" + $(CONTRACT.sn_ci.read.msg_no_cis_found).val() + "</span>";
					} else {
						output = "<span class=\"no-results\">" + $(CONTRACT.sn_ci.read.msg_sn_integration_failed).val() + "</span>";
					}
				}
				
				$(CONTRACT.sn_ci.list).html(output);
				$(CONTRACT.sn_ci.list).css("height","");
				$(CONTRACT.sn_ci.list).height($(CONTRACT.sn_ci.list).height());
			}
		}
	}
};

$(document).ready(function() {
	CONTRACT.init();
});