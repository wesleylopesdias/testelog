var RESET = {
	reset_password_box:"#reset-password-box",
	update_password_box:"#update-password-box",
	old_password_field:"#old-password",
	new_password_field:"#new-password",
	confirm_password_field:"#confirm-password",
	token:null,
	strength_indicator:".password-strength",
	reset_password_btn:"#reset-password-btn",
	update_password_btn:"#update-password-btn",
	required_fields_error:"#general-error-msg",
	too_short_error:"#too-short-error-msg",
	no_match_error:"#no-match-error-msg",
    init:function() {
    	RESET.bind_events();
    },
    bind_events:function() {
    	var $input = $(RESET.new_password_field);
		var $output = $(RESET.strength_indicator);
		$.passy.requirements.length.min = 7;
		$.passy.strength.HIGH;

		var feedback = [
		    { color:"#ebaf46", text:"poor" },
		    { color:"#ebea54", text:"okay" },
		    { color:"#4cd271", text:"good" },
		    { color:"#4cd271", text:"strong" }
		];

		$input.passy(function(strength,valid) {
			$output.text(feedback[strength].text);
	        $output.css("background-color", feedback[strength].color);
	        if(feedback[strength].text == "strong") $output.prepend("<i class=\"fa fa-check-circle\"></i>");
	        $output.css("visibility","visible");
		});
		
		$(RESET.reset_password_btn).click(function() {
			RESET.reset_password.submit_reset_password();
			return false;
		});
		
		$(RESET.update_password_btn).click(function() {
			RESET.update_password.submit_update_password();
			return false;
		});
		
		RESET.token = UTIL.get_param_by_name("token");
    },
    reset_password: {
    	submit_reset_password:function() {
    		UTIL.clear_msg_in_content_box(RESET.reset_password_box);
    		
    		var new_password = $(RESET.new_password_field).val();
    		var confirm_password = $(RESET.confirm_password_field).val();
    		
    		if(!new_password || !confirm_password) {
    			UTIL.add_error_msg_to_content_box(RESET.reset_password_box, $(RESET.required_fields_error).val());
    			return false;
    		} else if(new_password.length < 7) {
    			UTIL.add_error_msg_to_content_box(RESET.reset_password_box, $(RESET.too_short_error).val());
    			return false;
    		} else if(new_password != confirm_password) {
    			UTIL.add_error_msg_to_content_box(RESET.reset_password_box, $(RESET.no_match_error).val());
    			return false;
    		}
    		
    		var json = { "token":RESET.token, "newPassword":new_password };
    		
    		UTIL.add_loader_to_content_box(RESET.reset_password_box);
    		$(RESET.reset_password_btn).prop("disabled",true);
    		
    		$.ajax ({
    			url: PAGE_CONSTANTS.BASE_URL + "myaccount/updatepassword.json",
    			data: JSON.stringify(json),
    			type: "PUT",
    			success:function(data) {
    				
    				if(data.status == PAGE_CONSTANTS.OK_STS) {
    					UTIL.add_success_msg_to_content_box(RESET.reset_password_box, data.message);
    					$(RESET.new_password_field).attr("disabled",true);
    		    		$(RESET.confirm_password_field).attr("disabled",true);
    				} else {
    					UTIL.add_error_msg_to_content_box(RESET.reset_password_box, data.message);
    				}
    				
    				UTIL.remove_loader_from_content_box(RESET.reset_password_box);
    			}
    		});
    	}
    },
    update_password: {
    	submit_update_password:function() {
    		UTIL.clear_msg_in_content_box(RESET.update_password_box);
    		
    		var old_password = $(RESET.old_password_field).val();
    		var new_password = $(RESET.new_password_field).val();
    		var confirm_password = $(RESET.confirm_password_field).val();
    		
    		if(!old_password || !new_password || !confirm_password) {
    			UTIL.add_error_msg_to_content_box(RESET.update_password_box, $(RESET.required_fields_error).val());
    			return false;
    		} else if(new_password.length < 7) {
    			UTIL.add_error_msg_to_content_box(RESET.update_password_box, $(RESET.too_short_error).val());
    			return false;
    		} else if(new_password != confirm_password) {
    			UTIL.add_error_msg_to_content_box(RESET.update_password_box, $(RESET.no_match_error).val());
    			return false;
    		}
    		
    		var json = { "oldPassword":old_password, "newPassword":new_password };
    		
    		UTIL.add_loader_to_content_box(RESET.update_password_box);
    		$(RESET.update_password_btn).prop("disabled",true);
    		
    		$.ajax ({
    			url: PAGE_CONSTANTS.BASE_URL + "myaccount/updatepassword.json",
    			data: JSON.stringify(json),
    			type: "PUT",
    			success:function(data) {
    				if(data.status == PAGE_CONSTANTS.OK_STS) {
	    				UTIL.add_success_msg_to_content_box(RESET.update_password_box, data.message);
	    				
	    				$(RESET.old_password_field).val("");
	    	    		$(RESET.new_password_field).val("");
	    	    		$(RESET.confirm_password_field).val("");
	    	    		$(RESET.strength_indicator).css("visibility","hidden");
    				} else {
    					UTIL.add_error_msg_to_content_box(RESET.update_password_box, data.message);
    				}
    				
    				UTIL.remove_loader_from_content_box(RESET.update_password_box);
    				$(RESET.update_password_btn).prop("disabled",false);
    			}
    		});
    		
    	}
    }
    
};

$(document).ready(function() {
	RESET.init();
});