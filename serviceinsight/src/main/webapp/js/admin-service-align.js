var ALIGN = {
	align_dialog:"#service-align-dialog",
	align_device_id:"#align-device",
	align_service_id:"#align-service",
	align_password:"#align-password",
	general_validation_error:"#general-error-msg",
	init:function() {
		ALIGN.bind_events();
	},
	bind_events:function() {
		$(ALIGN.align_dialog).dialog({
			  autoOpen:false,
			  dialogClass:"contract-dialog",	
		      resizable:false,
		      width:700,
		      height:470,
		      modal:true,
		      title: "Re-Align Service",
		      open:function() {
		    	  $('.datepicker').datepicker('enable');
		    	  ALIGN.reset_popup();
		    	  $(this).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").show();
		    	  $(this).closest(".ui-dialog").find(".ui-button:contains('OK')").hide();
		      },
		      close:function() {
		    	  $('.datepicker').datepicker('disable');
		      },
		      buttons: {
		        "Save":function() {
		        	ALIGN.submit_align();
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
		UTIL.clear_message_in_popup(ALIGN.align_dialog);
		
		$(ALIGN.align_device_id).val("");
		$(ALIGN.align_service_id).val("");
		$(ALIGN.align_password).val("");
	},
	submit_align:function() {
		UTIL.clear_message_in_popup(ALIGN.align_dialog);
		
		var device_id = $(ALIGN.align_device_id).val();
		var service_id = $(ALIGN.align_service_id).val();
		var password = $(ALIGN.align_password).val();
		
		if(!device_id || !service_id || !password) {
			UTIL.add_error_message_to_popup(ALIGN.align_dialog, $(ALIGN.general_validation_error).val());
			return false;
		}
		
		var json = { deviceId:device_id, serviceId:service_id, password:password };
		
		json = UTIL.remove_null_properties(json);
		UTIL.add_dialog_loader(ALIGN.align_dialog);
		
		$.ajax ({
			url: PAGE_CONSTANTS.BASE_URL + "admin/servicealign.json",
			data: JSON.stringify(json),
			type: "POST",
			success:function(data) {
				UTIL.remove_dialog_loader(ALIGN.align_dialog);
				
				if(data.status == PAGE_CONSTANTS.OK_STS) {
					UTIL.add_success_message_to_popup(ALIGN.align_dialog, "Service Alignment successfully run!");
					$(ALIGN.align_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").hide();
					$(ALIGN.align_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button:contains('OK')").show();
				} else {
					UTIL.add_error_message_to_popup(ALIGN.align_dialog, data.message);
				}
			}
		});
	}
};

$(document).ready(function() {
	ALIGN.init();
});