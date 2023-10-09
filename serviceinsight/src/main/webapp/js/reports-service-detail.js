var REPORTS = {
	usage_data_table:"#report-usage-data-table",
	filter_criteria:".filter-criteria",
	report_month:"#report-month",
	report_service:"#report-service",
	report_device:"#report-device",
	sel_services:"#sel-services",
	sel_devices:"#sel-devices",
        services_list:"#span-services-list",
        devices_list:"#span-devices-list",
	run_report_btn:"#run-report-btn",
	report_section:".report-section",
	msg_error_service_required:"#msg-error-service-required",
	msg_error_device_required:"#msg-error-device-required",
	export_excel:"#download-excel",
	init:function() {
		REPORTS.bind_events();
		REPORTS.dates.init();
	},
	bind_events:function() {
		$(REPORTS.run_report_btn).click(function() {
			REPORTS.run_report();
		});
		$(REPORTS.export_excel).click(function() {
			var url = REPORTS.build_report_url(PAGE_CONSTANTS.FORMAT.excel);
			$(this).prop("href",url);
			return true;
		});
		$(REPORTS.sel_services).click(function() {
			$(REPORTS.services_list).show();
			$(REPORTS.devices_list).hide();
		});
		$(REPORTS.sel_devices).click(function() {
			$(REPORTS.devices_list).show();
			$(REPORTS.services_list).hide();
		});
	},
	dates:{
		format:"MM/YYYY",
		display_format:"MMM YYYY",
		init:function() {
			REPORTS.dates.bind_events();
			REPORTS.dates.setup_dates();
		},
		bind_events:function() {
			
		},
		setup_dates:function() {
			var output = "";
			
			var start_month_moment = moment().subtract(12, "months");
			var end_month_moment = moment();
			while(end_month_moment > start_month_moment) {
				output += "<option value=\"" + end_month_moment.startOf("month").format(REPORTS.dates.format) + "\">" + end_month_moment.format(REPORTS.dates.display_format); + "</option>";
				end_month_moment.subtract(1, "month");
			}

			$(REPORTS.report_month).html(output);
		}
	},
	build_report_url:function(report_format) {
		var url = PAGE_CONSTANTS.BASE_URL + "reports/servicedetails." + report_format + "?";
		
                var report_value = $("input[name='service_or_device']:checked").val();
		var month = $(REPORTS.report_month).val();
		var service = $(REPORTS.report_service).val();
		var device = $(REPORTS.report_device).val();
                
		if(month != "" && month != null) {
                    url += "svcdt=" + month;
                }
		if(report_value == "services" && service != "" && service != null) {
                    url += "&ospId=" + service;
		} else if(report_value == "devices" && device != "" && device != null) {
                    url += "&deviceId=" + device;
		}
		if(report_format == PAGE_CONSTANTS.FORMAT.excel) url += "&export";
		
		return url;
	},
	run_report:function() {
		//clear validation message
		UTIL.clear_msg_in_content_box($(REPORTS.filter_criteria));
                var report_value = $("input[name='service_or_device']:checked").val();
                if (report_value == "services") {
                    var service = $(REPORTS.report_service).val();
                    if (0 === service.length) {
                        UTIL.add_error_msg_to_content_box($(REPORTS.filter_criteria), $(REPORTS.msg_error_service_required).val());
                        return false;
                    }
                } else if (report_value == "devices") {
                    var device = $(REPORTS.report_device).val();
                    if (0 === device.length) {
                        UTIL.add_error_msg_to_content_box($(REPORTS.filter_criteria), $(REPORTS.msg_error_device_required).val());
                        return false;
                    }
                }
                
		var url = REPORTS.build_report_url(PAGE_CONSTANTS.FORMAT.json);
		
		//setup loaders
		UTIL.add_table_loader(REPORTS.usage_data_table, "Retrieving Report Data...");
		$(REPORTS.usage_data_table + " tfoot").html("");
		$(REPORTS.report_section).show();
		GLOBAL.set_nav_height();
		
		//ajax call
		$.ajax ({
			url: url,
			type: "GET",
			success:function(data) {
				REPORTS.build_usage_table(data);
				GLOBAL.set_nav_height();
			}
		});
	},
	build_usage_table:function(wrapper) {
            var records = wrapper.data;
            var total_onetime = 0;
            var total_recurring = 0;
            var output = "";
            var footer_output = "";
            var contract_base_url = PAGE_CONSTANTS.BASE_URL + "contracts";
            if(records && records.length > 0) {
                for(var i=0; i < records.length; i++) {
                    var icon = "";
                    var match_class = "";
                    var unit_count = "";
                    if(records[i].unitCount) {
                    	unit_count = "<div style=\"font-size:0.85em;\">(" + records[i].unitCount + " Units)</div>";
                    }
                    output += "<tr class=\"" + match_class + "\">";
                    output += "<td>" + records[i].customerName + "</td>";
                    output += "<td><a href=\"" + contract_base_url + "?jobnum=" + records[i].contractJobNumber + "&cid=" + records[i].customerId + "\">" + records[i].contractJobNumber + "</a></td>";
                    output += "<td>" + records[i].contractName + "</td>";
                    output += "<td>" + records[i].engagementManager + "</td>";
                    output += "<td>" + records[i].serviceName + "</td>";
                    output += "<td>" + records[i].deviceDescription + "</td>";
                    output += "<td>" + records[i].devicePartNumber + "</td>";
                    output += "<td class=\"right\">" + records[i].quantity + unit_count + "</td>";
                    output += "<td class=\"right\">" +  UTIL.convert_currency_for_ui(records[i].appliedOnetimeRevenue) +  "</td>";
                    total_onetime += records[i].appliedOnetimeRevenue;
                    output += "<td class=\"right\">" +  UTIL.convert_currency_for_ui(records[i].appliedRecurringRevenue) +  "</td>";
                    total_recurring += records[i].appliedRecurringRevenue;
                    output += "<td class=\"right\">" +  UTIL.convert_currency_for_ui(records[i].appliedOnetimeRevenue + records[i].appliedRecurringRevenue) +  "</td>";
                    output += "<td>" + records[i].startDate + "</td>";
                    output += "<td>" + records[i].endDate + "</td>";
                    output += "</tr>";
                }
                footer_output = "<tr class=\"total-row\"><td colspan=\"6\"></td><td></td><td class=\"right\">Total Revenue</td><td class=\"total-column\">" + accounting.formatMoney(total_onetime) + "</td><td class=\"total-column\">" + accounting.formatMoney(total_recurring) + "</td><td class=\"total-column\">" + accounting.formatMoney(total_onetime + total_recurring) + "</td><td colspan=\"2\"></td></tr>";
            } else {
                output = "<tr><td colspan=\"8\" class=\"no-results\">No results returned.</td></tr>";
            }
		
            $(REPORTS.usage_data_table + " tbody").html(output);
            $(REPORTS.usage_data_table + " tfoot").html(footer_output);
	}
};

$(document).ready(function() {
	REPORTS.init();
});