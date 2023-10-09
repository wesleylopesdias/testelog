var USERS = {
	add_user_popup:"#add-user-dialog",
	delete_user_popup:"#delete-user-dialog",
	disable_user_popup:"#disable-user-dialog",
	enable_user_popup:"#enable-user-dialog",
	users_table:"#users",
	users:null,
	users_msg_box:"#user-msg-box",
	current_id: null,
	current_username: null,
	current_tab:"active",
	current_user_field:"#current-user",
	init:function() {
		USERS.bind_events();
		USERS.read.init();
	},
	bind_events:function() {
		$(USERS.add_user_popup).dialog({
			  autoOpen:false,
			  dialogClass:"user-dialog",	
		      resizable:false,
		      width:740,
		      height:560,
		      modal:true,
		      title: "Users",
		      open:function() {
		    	  $(this).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").show();
		    	  $(this).closest(".ui-dialog").find(".ui-button:contains('OK')").hide();
		      },
		      buttons: {
		        "Save":function() {
		        	USERS.update_user.submit_user();
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
		
		$(USERS.delete_user_popup).dialog({
			  autoOpen:false,
			  dialogClass:"delete-dialog",	
		      resizable:false,
		      width:720,
		      height:400,
		      modal:true,
		      title: "Delete User",
		      open:function() {
		    	  $(this).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").show();
			      $(this).closest(".ui-dialog").find(".ui-button:contains('OK')").hide();
		      },
		      buttons: {
		        "Delete":function() {
		        	USERS.delete_user.submit_delete();
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
		
		$(USERS.disable_user_popup).dialog({
			  autoOpen:false,
			  dialogClass:"disable-dialog",	
		      resizable:false,
		      width:720,
		      height:400,
		      modal:true,
		      title: "Disable User",
		      open:function() {
		    	  $(this).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").show();
			      $(this).closest(".ui-dialog").find(".ui-button:contains('OK')").hide();
		      },
		      buttons: {
		        "Disable":function() {
		        	USERS.disable_user.submit_disable();
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

		$(USERS.enable_user_popup).dialog({
			  autoOpen:false,
			  dialogClass:"enable-dialog",	
		      resizable:false,
		      width:720,
		      height:400,
		      modal:true,
		      title: "Enable User",
		      open:function() {
		    	  $(this).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").show();
			      $(this).closest(".ui-dialog").find(".ui-button:contains('OK')").hide();
		      },
		      buttons: {
		        "Enable":function() {
		        	USERS.enable_user.submit_enable();
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
		
		$(document).on("click", ".update-user", function() {
	    	var id = $(this).data("id");
	    	(id == "add") ? USERS.current_id = null : USERS.current_id = id;
	    	USERS.update_user.reset_add_popup();
    		return false;
    	});
		
		$(document).on("click", ".delete-user", function(){
			USERS.current_id = $(this).data("id");
			USERS.current_username = $(this).data("username");
			USERS.delete_user.reset_delete_popup();
		});
		
		$(document).on("click", ".disable-user", function(){
			USERS.current_id = $(this).data("id");
			USERS.current_username = $(this).data("username");
			USERS.disable_user.reset_disable_popup();
		});
		
		$(document).on("click", ".enable-user", function(){
			USERS.current_id = $(this).data("id");
			USERS.current_username = $(this).data("username");
			USERS.enable_user.reset_enable_popup();
		});
	},
	read:{
		tab:".table-tab",
		disable_label:"#disable-user-label",
		enable_label:"#enable-user-label",
		delete_label:"#delete-user-label",
		init:function() {
			USERS.read.bind_events();
			USERS.read.load_user_list();
			$(USERS.update_user.user_profile_field).selectBox();
		},
		bind_events:function() {
			$(USERS.read.tab).click(function() {
				$(USERS.read.tab).removeClass("selected");
				$(this).addClass("selected");
				var type = $(this).data("view");
				USERS.current_tab = type;
				USERS.read.load_user_list();
			});
		},
		load_user_list:function() {
			USERS.read.get_users(USERS.current_tab);
		},
		get_users:function(type) {
			UTIL.add_table_loader(USERS.users_table);
			
			var enabled = "?enabled=true";
			if(type == "disabled") enabled = "?enabled=false";
			
			$.ajax ({
				url: PAGE_CONSTANTS.BASE_URL + "settings/users.json" + enabled,
				type: "GET",
				success:function(data) {
					USERS.users = data;
					USERS.read.build_users(data);
				}
			});
		},
		build_users:function(users) {
			var output = "";
			var url = PAGE_CONSTANTS.BASE_URL + "settings/users/";
			var disable_lbl = $(USERS.read.disable_label).val();
			var enable_lbl = $(USERS.read.enable_label).val();
			var delete_lbl = $(USERS.read.delete_label).val();
			var current_user = $(USERS.current_user_field).val();
			if(users && users.length > 0) {
				var count = 0;
				for(var i=0;i<users.length;i++) {
					count++;
					var username = users[i].username;
					var user_id = users[i].id;
					var title = users[i].title;
					if(!users[i].title) title = "";
					output += "<tr>";
					output += "<td>" + users[i].name + "</td>";
					output += "<td>";
					if ((USERS.current_tab=="active") && (username!=current_user)) {
						output += "<a href=\"javascript:;\" class=\"update-user popup-link\" data-dialog=\"add-user-dialog\" data-id=\"" + user_id + "\">" + username + "</a>";
					} else {
						output += username;
					}
					output += "</td>";
					output += "<td>" + title + "</td>";
					output += "<td class\"center\">";
					if (users[i].profile && users[i].profile.length>0) {
						var cnt = 0;
						for (var x=0;x<users[i].profile.length;x++) {
							if (cnt>0) {
								output += ", ";
							}
							output += USERS.read.get_role_text(users[i].profile[x]);
							cnt++;
						}
					} else {
						output += "No roles found";
					}
					output += "</td>";
					output += "<td class=\"right\">";
					if (username!=current_user) {
						if (USERS.current_tab=="active") {
							output += "   <a href=\"javascript:;\" class=\"disable-user popup-link\" data-dialog=\"disable-user-dialog\" data-username=\"" + username + "\" data-id=\"" + user_id + "\"><i class=\"fa fa-user-times\"></i>" + disable_lbl + "</a> ";
						} else {
							output += "   <a href=\"javascript:;\" class=\"enable-user popup-link\" data-dialog=\"enable-user-dialog\" data-username=\"" + username + "\" data-id=\"" + user_id + "\"><i class=\"fa fa-user\"></i>" + enable_lbl + "</a> ";
						}
					}
				    output += "</td></tr>";
				}
				
				if(count == 0) {
					output = "<tr><td colspan=\"5\" class=\"no-results\">No users found.</td></tr>";
				}
			} else {
				output = "<tr><td colspan=\"5\" class=\"no-results\">No users found.</td></tr>";
			}
			$(USERS.users_table).find("tbody").html(output);
			$(USERS.users_table).show();
		},
		get_role_text:function(role) {
			var role_text = role;
			$(USERS.update_user.user_template_roles_select).children("option").each(function(){
				if (role == (this.value)) {
					 role_text = this.text;
				}
			});
			return role_text;
		}
	},
	update_user: {
		name_field:"#name",
		user_name_field:"#username",
		user_title_field:"#title",
		user_password_field:"#password",
		user_profile_field:"#roles",
		user_template_roles_select:"#roles-template-select",
		add_user_msg:"#add-user-msg",
		update_user_msg:"#update-user-msg",
		general_error_msg:"#general-error-msg",
		username_error_msg:"#username-error-msg",
		password_error_msg:"#password-error-msg",
		profile_error_msg:"#profile-error-msg",
		add_class:".add",
		edit_class:".edit",
		reset_add_popup:function() {
			$(USERS.update_user.user_name_field).val("");
			$(USERS.update_user.name_field).val("");
			$(USERS.update_user.user_title_field).val("");
			$(USERS.update_user.user_password_field).val("");
			USERS.update_user.build_dropdown();
			if(USERS.current_id != null) {
				USERS.update_user.load_user();
				//$(PAGE_CONSTANTS.REQUIRED_CLASS.SELECTOR).hide();
				$(USERS.update_user.add_class).hide();
				$(USERS.update_user.edit_class).show();
				$(USERS.update_user.user_name_field).attr("disabled","disabled");
			} else {
				//$(PAGE_CONSTANTS.REQUIRED_CLASS.SELECTOR).show();
				$(USERS.update_user.add_class).show();
				$(USERS.update_user.edit_class).hide();
				$(USERS.update_user.user_name_field).removeAttr("disabled");
			}
			UTIL.clear_message_in_popup(USERS.add_user_popup);
		},
		load_user:function() {
			$.ajax ({
				url: PAGE_CONSTANTS.BASE_URL + "settings/users/" + USERS.current_id + ".json",
				type: "GET",
				success:function(data) {
					USERS.update_user.set_user(data);
				}
			});
		},
		set_user:function(data) {
			$(USERS.update_user.name_field).val(data.name);
			$(USERS.update_user.user_title_field).val(data.title);
			$(USERS.update_user.user_name_field).val(data.username);
			$(USERS.update_user.user_password_field).val(data.password);
			USERS.update_user.build_dropdown(data.profile);
		},
		build_dropdown: function(profile) {
			var output = "";
			var selected = false;
			$(USERS.update_user.user_template_roles_select).children("option").each(function(){
				selected = false;
				if (profile && profile.length>0) {
					for (var x=0; x<profile.length; x++) {
						if (profile[x] == (this.value)) {
							selected = true;
						}
					}
				}
				output += "<option value=\"" + this.value + "\"";
				if (selected) {
					output += " selected=\"" + selected  + "\"";
				}
				output +=  ">"+ this.text + "</option>";
			});
			$(USERS.update_user.user_profile_field).html(output);
			$(USERS.update_user.user_profile_field).selectBox("destroy");
			$(USERS.update_user.user_profile_field).selectBox();
		},
		submit_user:function() {
			UTIL.clear_message_in_popup(USERS.add_user_popup);
			
			var username = $(USERS.update_user.user_name_field).val();
			var name = $(USERS.update_user.name_field).val();
			var title = $(USERS.update_user.user_title_field).val();
			var password = $(USERS.update_user.user_password_field).val();
			var roles = [];
			$(USERS.update_user.user_profile_field).children("option").each(function(){
				if (this.selected) {
					roles.push(this.value);
				}
			});
			//validate
			if (USERS.current_username == null && (!username || !password || roles.length==0)) {
				var errMsg = "";
				var errCtr = 0;
				if (!username) {
					errMsg += $(USERS.update_user.username_error_msg).val();
					errCtr++;
				}
				if (!password) {
					if (errMsg) {
						errMsg += " and ";
					}
					errMsg += $(USERS.update_user.password_error_msg).val();
					errCtr++;
				}
				if (roles.length==0) {
					if (errMsg) {
						errMsg += " and ";
					}
					errMsg += $(USERS.update_user.profile_error_msg).val();
					errCtr++;
				}
				if (errCtr == 3) {
					errMsg = $(USERS.update_user.general_error_msg).val();
				}
				UTIL.add_error_message_to_popup(USERS.add_user_popup, errMsg);
				return false;
			} 
			
			if(username) {
				username = username.toLowerCase();
			}
			
			var ajax_type = "POST";
			if(USERS.current_id != null) {
				ajax_type = "PUT";
			}
			
			var json = { "id":USERS.current_id, "username":username, "password":password, "name":name, "title":title };
			json.profile = roles;
			
			UTIL.add_dialog_loader(USERS.add_user_popup);
			
			$.ajax ({
				url: PAGE_CONSTANTS.BASE_URL + "settings/users.json",
				data: JSON.stringify(json),
				type: ajax_type,
				success:function(data) {
					UTIL.remove_dialog_loader(USERS.add_user_popup);
					if(data.status == PAGE_CONSTANTS.OK_STS) {
						var success_msg = $(USERS.update_user.add_user_msg).val();
						if(USERS.current_username != null) success_msg = $(USERS.update_user.update_user_msg).val();
						UTIL.add_success_message_to_popup(USERS.add_user_popup, success_msg);
						$(USERS.add_user_popup).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").hide();
						$(USERS.add_user_popup).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button:contains('OK')").show();
						
						$(PAGE_CONSTANTS.REQUIRED_CLASS.SELECTOR).hide();
						$(USERS.update_user.add_class).hide();
						$(USERS.update_user.edit_class).show();
						
						USERS.read.load_user_list(USERS.current_tab);
					} else {
						UTIL.add_error_message_to_popup(USERS.add_user_popup,data.message);
					}
				}
			});
		}
	},
	delete_user:{
		username_display:"#delete-username",
		reset_delete_popup:function(){
			UTIL.clear_message_in_popup(USERS.delete_user_popup);
			$(USERS.delete_user.username_display).html(USERS.current_username);			
		},
		submit_delete:function(){
			UTIL.clear_message_in_popup(USERS.delete_user_popup);
			UTIL.add_dialog_loader(USERS.delete_user_popup);
			var url = PAGE_CONSTANTS.BASE_URL + "settings/users/" + USERS.current_id;
			$.ajax ({
				url: url + ".json",
				type: "DELETE",
				success:function(data) {
					UTIL.remove_dialog_loader(USERS.delete_user_popup);					
					if(data.status == PAGE_CONSTANTS.OK_STS) {
						UTIL.add_success_message_to_popup(USERS.delete_user_popup, data.message);
						$(USERS.delete_user_popup).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").hide();
						$(USERS.delete_user_popup).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button:contains('OK')").show();
						
						USERS.read.load_user_list(USERS.current_tab);
					} else {
						UTIL.add_error_message_to_popup(USERS.delete_user_popup, data.message);
					}
				}
			});			
		}
	},
	disable_user:{
		username_display:"#disable-username",
		reset_disable_popup:function(){
			UTIL.clear_message_in_popup(USERS.disable_user_popup);
			$(USERS.disable_user.username_display).html(USERS.current_username);			
		},
		submit_disable:function(){
			UTIL.clear_message_in_popup(USERS.disable_user_popup);
			UTIL.add_dialog_loader(USERS.disable_user_popup);
			var url = PAGE_CONSTANTS.BASE_URL + "settings/users/disable/" + USERS.current_id;
			$.ajax ({
				url: url + ".json",
				type: "PUT",
				success:function(data) {
					UTIL.remove_dialog_loader(USERS.disable_user_popup);
					
					if(data.status == PAGE_CONSTANTS.OK_STS) {
						UTIL.add_success_message_to_popup(USERS.disable_user_popup, data.message);
						$(USERS.disable_user_popup).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").hide();
						$(USERS.disable_user_popup).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button:contains('OK')").show();
						
						USERS.read.load_user_list(USERS.current_tab);
					} else {
						UTIL.add_error_message_to_popup(USERS.disable_user_popup, data.message);
					}
				}
			});			
		}
	},
	enable_user:{
		username_display:"#enable-username",
		reset_enable_popup:function(){
			UTIL.clear_message_in_popup(USERS.enable_user_popup);
			$(USERS.enable_user.username_display).html(USERS.current_username);			
		},
		submit_enable:function(){
			UTIL.clear_message_in_popup(USERS.enable_user_popup);
			UTIL.add_dialog_loader(USERS.enable_user_popup);
			var url = PAGE_CONSTANTS.BASE_URL + "settings/users/enable/" + USERS.current_id;
			$.ajax ({
				url: url + ".json",
				type: "PUT",
				success:function(data) {
					UTIL.remove_dialog_loader(USERS.enable_user_popup);
					
					if(data.status == PAGE_CONSTANTS.OK_STS) {
						UTIL.add_success_message_to_popup(USERS.enable_user_popup, data.message);
						$(USERS.enable_user_popup).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").hide();
						$(USERS.enable_user_popup).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button:contains('OK')").show();
						
						USERS.read.load_user_list(USERS.current_tab);
					} else {
						UTIL.add_error_message_to_popup(USERS.enable_user_popup, data.message);
					}
				}
			});			
		}
	}
};

$(document).ready(function() {
	USERS.init();
});