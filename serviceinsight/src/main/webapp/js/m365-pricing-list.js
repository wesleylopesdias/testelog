var M365 = {
	init:function() {
		M365.set_variables();
		M365.bind_events();
		M365.read.init();
		M365.file_upload.init();
		M365.file_import.init();
	},
	set_variables:function() {
		
	},
	bind_events:function() {
		
	},
	read: {
		pricing_table:"#m365-pricing",
		init:function() {
			M365.read.bind_events();
			M365.read.get_microsoft_price_lists();
		},
		bind_events:function() {
			
		},
		get_microsoft_price_lists:function() {
			$.ajax ({
				url: PAGE_CONSTANTS.BASE_URL + "/settings/microsoftpricing.json",
				type: "GET",
				success:function(data) {
					M365.read.build_table(data);
				}
			});
		},
		build_table:function(price_lists) {
			var output = "";
			
			if(price_lists != null && price_lists.length > 0) {
				for(var i = 0; i < price_lists.length; i++) {
					output += M365.read.build_table_row(price_lists[i]);
				}
			} else {
				output += "<tr><td colspan=\"4\" class=\"no-results\">No Microsoft Pricing Found</td></tr>";
			}
			
			$(M365.read.pricing_table + " tbody").html(output);
		},
		build_table_row:function(price_list) {
			var output = "<tr>";
			output += "<td>" + price_list.month + "</td>";
			output += "<td>" + price_list.type + "</td>";
			output += "<td>" + price_list.productCount + "</td>";
			output += "<td></td>";
			output += "</tr>";
			return output;
		}
	},
	file_upload: {
		upload_dialog:"#upload-dialog",
		form:"#upload-form",
		form_target:"#upload-target",
		file_field: "#template-file",
		upload_date_field:"#upload-date",
		file_select_error_msg: "#file-select-file-error-msg",
		file_type_invalid_error_msg: "#file-type-invalid-error-msg",
		iframe_status:"#iframe-status",
		iframe_message:"#iframe-message",
		init:function() {
			M365.file_upload.bind_events();
		},
		bind_events:function() {
			$(M365.file_upload.upload_dialog).dialog({
				  autoOpen:false,
				  dialogClass:"upload-dialog",	
			      resizable:false,
			      width:720,
			      height:400,
			      modal:true,
			      title: "Upload a Spreadsheet",
			      open:function() {
			    	  $('.datepicker').datepicker('enable');
			    	  M365.file_upload.reset_popup();
			      },
			      close:function() {
			    	  $('.datepicker').datepicker('disable');
			      },
			      buttons: {
			        "Upload":function() {
			        	M365.file_upload.submit_file();
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
			
			$(M365.file_upload.form_target).load(function(){ 
				var iframe = $(M365.file_upload.form_target);
				var status = $(M365.file_upload.iframe_status, iframe.contents()).html();
				var message = $(M365.file_upload.iframe_message, iframe.contents()).html();
				M365.file_upload.complete_submit(status, message);
			});
		},
		reset_popup:function() {
			$(M365.file_upload.upload_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").show();
	    	$(M365.file_upload.upload_dialog).closest(".ui-dialog").find(".ui-button:contains('OK')").hide();
	    	UTIL.clear_message_in_popup(M365.file_upload.upload_dialog);
	    	$(M365.file_upload.file_field).val("");
	    	$(M365.file_upload.upload_date_field).val("");
		},
		submit_file:function() {
			UTIL.clear_message_in_popup(M365.file_upload.upload_dialog);
			
			var file = $(M365.file_upload.file_field).val();
			var date = $(M365.file_upload.upload_date_field).val();
			
			if(file == "") {
				var message = $(M365.file_upload.file_select_error_msg).val();
				UTIL.add_error_message_to_popup(M365.upload_dialog, message);
				return false;
			}
			
			if(!(/\.(xlsx)$/i).test(file)) {
				var message = $(M365.file_upload.file_type_invalid_error_msg).val();
				UTIL.add_error_message_to_popup(COSTS.upload_dialog, message);
				return false;
			}
			
			UTIL.add_dialog_loader(M365.file_upload.upload_dialog);
			$(M365.file_upload.form).submit();
		},
		complete_submit:function(status, message) {
			UTIL.remove_dialog_loader(M365.file_upload.upload_dialog);
			if(status == "success") {
				UTIL.add_success_message_to_popup(M365.file_upload.upload_dialog, message);
				$(M365.file_upload.upload_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").hide();
		    	$(M365.file_upload.upload_dialog).closest(".ui-dialog").find(".ui-button:contains('OK')").show();
		    	M365.read.get_microsoft_price_lists();
			} else {
				UTIL.add_error_message_to_popup(M365.file_upload.upload_dialog, message);
			}
		}
	},
	file_import: {
		import_dialog:"#import-dialog",
		form:"#import-form",
		init:function() {
			M365.file_import.bind_events();
		},
		bind_events:function() {
			$(M365.file_import.import_dialog).dialog({
				  autoOpen:false,
				  dialogClass:"upload-dialog",	
			      resizable:false,
			      width:720,
			      height:400,
			      modal:true,
			      title: "Import from PartnerCenter",
			      open:function() {
			    	  M365.file_import.reset_popup();
			      },
			      close:function() {
			      },
			      buttons: {
			        "Import":function() {
			        	M365.file_import.submit_file();
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
			$(M365.file_import.import_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").show();
	    	$(M365.file_import.import_dialog).closest(".ui-dialog").find(".ui-button:contains('OK')").hide();
	    	UTIL.clear_message_in_popup(M365.file_import.import_dialog);
		},
		submit_file:function() {
			UTIL.clear_message_in_popup(M365.file_import.import_dialog);
			UTIL.add_dialog_loader(M365.file_import.import_dialog);
			$(M365.file_import.form).submit();
		},
		complete_submit:function(status, message) {
			UTIL.remove_dialog_loader(M365.file_import.import_dialog);
			if(status == "success") {
				UTIL.add_success_message_to_popup(M365.file_import.import_dialog, message);
				$(M365.file_import.import_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").hide();
		    	$(M365.file_import.import_dialog).closest(".ui-dialog").find(".ui-button:contains('OK')").show();
		    	M365.read.get_microsoft_price_lists();
			} else {
				UTIL.add_error_message_to_popup(M365.file_import.import_dialog, message);
			}
		}
	},
};

$(document).ready(function() {
	M365.init();
});