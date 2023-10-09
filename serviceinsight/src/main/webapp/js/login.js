var LOGIN = {
	login_btn:"#login-btn",
	reset_password_dialog:"#reset-password-dialog",
	reset_password_email_field:"#reset-password-email",
	reset_password_enter_email_msg:"#error-enter-email-msg",
	reset_password_enter_valid_email_msg:"#error-enter-valid-email-msg",
	reset_password_email_sent_msg:"#ok-email-sent-msg",
	init:function() {
		LOGIN.bind_events();
	},
	bind_events:function() {
		$(LOGIN.reset_password_dialog).dialog({
			  autoOpen:false,
			  dialogClass:"upload-dialog",	
		      resizable:false,
		      width:720,
		      height:340,
		      modal:true,
		      title: "Forgot Password",
		      open:function() {
		    	  $(this).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").show();
		    	  $(this).closest(".ui-dialog").find(".ui-button:contains('OK')").hide();
		    	  LOGIN.reset_popup();
		      },
		      close:function() {
		    	  $('.datepicker').datepicker('disable');
		      },
		      buttons: {
		        "Reset Password":function() {
		        	LOGIN.submit_reset();
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
		
		$(LOGIN.login_btn).click(function() {
			$(this).parents("form").submit();
		});
	},
	reset_popup:function() {
		UTIL.clear_message_in_popup(LOGIN.reset_password_dialog);
		
		$(LOGIN.reset_password_email_field).val("");
	},
	submit_reset:function() {
		UTIL.clear_message_in_popup(LOGIN.reset_password_dialog);
		
		var email = $(LOGIN.reset_password_email_field).val();
		
		if(!email) {
			UTIL.add_error_message_to_popup(LOGIN.reset_password_dialog, $(LOGIN.reset_password_enter_email_msg).val());
			return false;
		} else if(!VALIDATE.validate_email(email)) {
			UTIL.add_error_message_to_popup(LOGIN.reset_password_dialog, $(LOGIN.reset_password_enter_valid_email_msg).val());
			return false;
		}
		
		var json = { "username":email };
		UTIL.add_dialog_loader(LOGIN.reset_password_dialog);
		
		$.ajax ({
			url: PAGE_CONSTANTS.BASE_URL + "myaccount/resetpassword.json",
			data: JSON.stringify(json),
			type: "POST",
			success:function(data) {
				UTIL.remove_dialog_loader(LOGIN.reset_password_dialog);
				
				//check if it was successful or not
				if(data.status == PAGE_CONSTANTS.OK_STS) {
					var success_msg = $(LOGIN.reset_password_email_sent_msg).val();
					UTIL.add_success_message_to_popup(LOGIN.reset_password_dialog, success_msg);
					$(LOGIN.reset_password_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").hide();
					$(LOGIN.reset_password_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button:contains('OK')").show();
				} else {
					var error_msg = data.message;
					UTIL.add_error_message_to_popup(LOGIN.reset_password_dialog, error_msg);
				}
			}
		});
		
	}
};

$(document).ready(function() {
	LOGIN.init();
});