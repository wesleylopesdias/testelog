var PAGE_CONSTANTS = {
    BASE_URL: "/si/",
    MSG_DIV: ".message-content",
    CNTNT_MSG:".content-msg",
    INLNE_LDR:".inline-loader",
    OK_STS: "OK",
    ERRR_STS: "ERROR",
    LOADER_URL:"/si/images/ajax-loader.gif",
    FORMAT:{ json:"json", excel:"xlsx" },
    RLTD_DT_ERRR: "RELATED_DATA_FOUND",
    REQUIRED_CLASS:{ VALUE:"required-ind", SELECTOR:".required-ind", REQUIRED:"required" }
};

var VALIDATE = {
	validate_email:function(email) {
		var re = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
    	return re.test(email);
	}
};

var UTIL = {
	add_success_msg_to_content_box:function(box_selector, message) {
		$(box_selector).find(PAGE_CONSTANTS.CNTNT_MSG).addClass("success-msg").html("<i class=\"fa fa-check-circle\"></i>" + message);
	},
	add_error_msg_to_content_box:function(box_selector, message) {
		$(box_selector).find(PAGE_CONSTANTS.CNTNT_MSG).addClass("error-msg").html("<i class=\"fa fa-times-circle\"></i>" + message);
	},
	clear_msg_in_content_box:function(box_selector) {
		$(box_selector).find(PAGE_CONSTANTS.CNTNT_MSG).removeClass("error-msg").removeClass("success-msg").removeClass("notice-msg").html("");
	},
	add_loader_to_content_box:function(box_selector) {
		$(box_selector).find(PAGE_CONSTANTS.INLNE_LDR).show();
	},
	remove_loader_from_content_box:function(box_selector) {
		$(box_selector).find(PAGE_CONSTANTS.INLNE_LDR).hide();
	},
	clear_message_in_popup:function(dialog_selector) {
		var $obj = $(dialog_selector);
		$obj.find(PAGE_CONSTANTS.MSG_DIV).removeClass("error-msg").removeClass("success-msg").removeClass("notice-msg").html("");
	},
	add_error_message_to_popup:function(dialog_selector, message) {
		var $obj = $(dialog_selector);
		$obj.find(PAGE_CONSTANTS.MSG_DIV).addClass("error-msg").html("<i class=\"fa fa-times-circle\"></i>" + message);
		$obj.scrollTop("0");
	},
	add_success_message_to_popup:function(dialog_selector, message) {
		var $obj = $(dialog_selector);
		$obj.find(PAGE_CONSTANTS.MSG_DIV).addClass("success-msg").html("<i class=\"fa fa-check-circle\"></i>" + message);
		$obj.scrollTop("0");
	},
	add_notice_message_to_popup:function(dialog_selector, message) {
		var $obj = $(dialog_selector);
		$obj.find(PAGE_CONSTANTS.MSG_DIV).addClass("notice-msg").html("<i class=\"fa fa-exclamation-circle\"></i>" + message);
	},
	create_message_for_related_data:function(general_message, related_message, related_data) {
		var output = general_message;
		output += "<div class=\"list-header\">" + related_message + "</div>";
		output += "<ul>";
		for(var i=0; i < related_data.length; i++) {
                    if (related_data[i].name) {
			output += "<li>Expense: " + related_data[i].name + "</li>";
                    } else if (related_data[i].deviceId) {
			output += "<li>Device-Cost-Category IDs: " + related_data[i].deviceId + " / " + related_data[i].expenseCategoryId + "</li>";
                    }
		}
		output += "</ul>";
		return output;
	},
	open_dialog:function(dialog_id) {
		$(dialog_id).dialog("open");
		//jquery ui defaults focus to the first link, so we blur that because it looks a bit odd
		$('.ui-dialog a').blur();
	},
	add_dialog_loader:function(dialog_selector) {
		var $obj = $(dialog_selector);
		$obj.find(".dialog-loader").show();
		$obj.find(".dialog-loader-overlay").show();
		$obj.parent().find(".ui-dialog-buttonpane").append("<div class=\"loader-button-overlay\"></div>");
	},
	remove_dialog_loader:function(dialog_selector) {
		var $obj = $(dialog_selector);
		$obj.find(".dialog-loader").hide();
		$obj.find(".dialog-loader-overlay").hide();
		$obj.parent().find(".loader-button-overlay").remove();
	},
	add_table_loader:function(table_selector, msg) {
		if(!msg) msg = "Loading...";
		var $table = $(table_selector);
		var rows = $table.find("thead tr th").length;
		$table.find("tbody").html("<tr><td colspan=\"" + rows + "\" class=\"no-results\"><img src=\"" + PAGE_CONSTANTS.LOADER_URL + "\" style=\"margin-right:5px;\"/>" + msg + "</td><tr>");
		$table.show();
	},
	add_chart_loader:function(chart_selector, msg) {
		if(!msg) msg = "Retreiving Chart Data...";
		var loader = "<div class=\"loader\"><img src=\"" + PAGE_CONSTANTS.LOADER_URL + "\" /><br/>" + msg + "</div>";
		$(chart_selector).html(loader);
		$(chart_selector).show();
	},
	convert_currency_for_server:function(value) {
		value = accounting.formatMoney(value);
		return value.replace(/,/g, '').replace(/\$/g, '');
	},
	convert_currency_for_ui:function(value) {
		return accounting.formatMoney(value);
	},
	convert_decimal_for_ui:function(value) {
		value = accounting.formatMoney(value);
		return value.replace(/,/g, '').replace(/\$/g, '');
	},
	convert_number_for_ui:function(value) {
		return accounting.formatNumber(value);
	},
	convert_dates_for_server:function(date) {
		if(!date) return date;
		return moment(date, "MM/DD/YYYY").format("MMDDYYYY");
	},
	convert_dates_for_ui:function(date) {
		if(!date) return "";
		return moment(date, "MMDDYYYY").format("MM/DD/YYYY");
	},
	convert_dates_for_ui_alt:function(date) {
		if(!date) return "";
		return moment(date, "MMDDYYYY").format("MM/DD/YYYY");
	},
	convert_percent_to_decimal:function(number) {
		return number / 100;
	},
	convert_decimal_to_percent:function(number, decimals) {
		if(decimals == null || decimals == undefined) decimals = 10;
		return (number * 100).toFixed(decimals);
	},
	round_with_decimals:function(number, decimals) {
		return Number(Math.round(number + 'e' + decimals) + 'e-' + decimals);
	},
	get_param_by_name:function(name) {
	    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
	    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
	        results = regex.exec(location.search);
	    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
	},
	remove_null_properties:function(post_data) {
		for (var key in post_data) {
		   if (post_data.hasOwnProperty(key)) {
			   if(post_data[key] == "" || post_data[key] == null) {
				   delete post_data[key];
			   }
		    }
		}
		return post_data;
	},
	prepare_data_for_display:function(get_data) {
		for(var i=0; i < get_data.length; i++) {
			var data = get_data[i];
			for (var key in data) {
			   if (data.hasOwnProperty(key)) {
				   if(data[key] == null) {
					   data[key] = "";
				   }
			    }
			}
		}
		return get_data;
	},
	flash_table_color:function($obj, color) {
	    var container = $obj.find("td");
	    if(!color) color = '#4cd271';
	    if(container.length) {
	        var originalColor = container.css('backgroundColor');
	        container.animate({
	            backgroundColor:color
	        },'normal','linear',function(){
	            $(this).animate({
	                backgroundColor:originalColor
	            });
	        });
	    }
	}
};

var GLOBAL = {
	is_admin_field:"#is-admin",
	is_admin:false,
	is_manager_field:"#is-manager",
	is_manager:false,
	logicalis_app_dropdown_trigger:".logicalis-links-arrow",
	logicalis_app_dropdown:".logicalis-app-nav .dropdown-menu",
	init:function() {
		GLOBAL.set_variables();
		GLOBAL.bind_events();
		GLOBAL.set_ajax_defaults();
		GLOBAL.set_nav_height();
		GLOBAL.set_highcharts_defaults();
	},
	set_variables:function() {
		var is_admin = $(GLOBAL.is_admin_field).val();
		if(is_admin) GLOBAL.is_admin = is_admin;
		var is_manager = $(GLOBAL.is_manager_field).val();
		if(is_manager) GLOBAL.is_manager = is_manager;
	},
	bind_events:function() {
		$(".dialog").on("focus", ".datepicker", function() {
			$(this).datepicker({changeYear: true});
		});
		$(".dialog-content .datepicker").datepicker("disable");
		
		$(".page-datepicker").datepicker({ changeYear: true });
		$(".month-datepicker").monthYearPicker();
		
		$(document).on("click", ".popup-link", function() {
	    	var id = $(this).data("dialog");
    		UTIL.open_dialog("#" + id);
    		return false;
    	});
		
		$(".dialog").on("input", ".currency", function() {
			this.value = this.value.replace(/[^0-9.]/g, '');
		}).on("blur", ".currency", function() {
			this.value = UTIL.convert_currency_for_server(this.value);
		});
		$(".dialog").on("input", ".decimal", function() {
			this.value = this.value.replace(/[^\d\.*]+/g, '');
		});
		$(".dialog").on("input", ".single-decimal", function() {
			var val = $(this).val();
			if(isNaN(val)){
			     val = val.replace(/[^0-9\.]/g,'');
			     if(val.split('.').length > 2) val = val.replace(/\.+$/,"");
			} else {
				if(val.split('.').length == 2 && val.split('.')[1].length > 1) {
			    	 val = val.substring(0, val.length - 1);
			     }
			}
			$(this).val(val);
		});
		$(".dialog").on("input", ".currency-negative", function() {
			this.value = this.value.replace(/[^0-9-.]/g, '');
		}).on("blur", ".currency-negative", function() {
			this.value = UTIL.convert_currency_for_server(this.value);
		});
		$(".dialog").on("input", ".integer", function() {
			this.value = this.value.replace(/[^0-9]/g, '');
		}).on("blur", ".currency", function() {
			this.value = UTIL.convert_currency_for_server(this.value);
		});
		$(".dialog").on("input propertychange", "textarea[maxlength]", function() {  
	        var max_length = $(this).attr("maxlength");  
	        if ($(this).val().length > max_length) {  
	            $(this).val($(this).val().substring(0, max_length));  
	        }  
	    });
		
		$(".dialog").on("input", ".decimal", function() {
			this.value = this.value.replace(/[^0-9.]/g, '');
		});
		
		$(window).resize(function() {
			GLOBAL.set_nav_height();
		});
		
		
		$(document).click(function() {
			if($(GLOBAL.logicalis_app_dropdown).hasClass("show")) {
				var $clicked = $(this);
				if(!$clicked.hasClass("logicalis-app-nav") && !$clicked.hasClass("dropdown-menu") && !$clicked.hasClass("logicalis-links-arrow")) {
					$(GLOBAL.logicalis_app_dropdown).removeClass("show");
				}
			}
		});
		
		$(GLOBAL.logicalis_app_dropdown_trigger).click(function() {
			$(GLOBAL.logicalis_app_dropdown).addClass("show");
			return false;
		});
	},
	set_ajax_defaults:function() {
	    var token = $("meta[name='_csrf']").attr("content");
	    var header = $("meta[name='_csrf_header']").attr("content");
	    $(document).ajaxSend(function(e, xhr, options) {
	        xhr.setRequestHeader(header, token);
	        xhr.setRequestHeader("Accept", "application/json");
            xhr.setRequestHeader("Content-Type", "application/json");
	    });
	    
	    $(document).ajaxComplete(function() {
	    	GLOBAL.set_nav_height();
	    });
	},
	set_nav_height:function() {
		var height = $(".page-wrapper").height() - 85;
		$(".main-nav").height(height);
	},
	set_highcharts_defaults:function() {
		if(typeof Highcharts != 'undefined') {
			Highcharts.setOptions({
			    lang: {
			        thousandsSep: ','
			    }
			});
		}
	}
};

$(document).ready(function() {
    GLOBAL.init();
});
