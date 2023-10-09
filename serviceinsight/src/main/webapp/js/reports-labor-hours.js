var REPORTS = {
	chart:"#chart",
	data_table:"#report-data-table",
	filter_criteria:".filter-criteria",
	report_date_range:"#report-date-range",
	report_custom_dates:"#report-custom-dates",
	report_start_date:"#report-start-date",
	report_end_date:"#report-end-date",
	report_service:"#report-service",
	report_customer:"#report-customer",
	run_report_btn:"#run-report-btn",
	report_section:".report-section",
	msg_error_dates_required:"#msg-error-dates-required",
	msg_error_dates_start_before_end:"#msg-error-start-before-end",
	results_msg:"#results-msg",
	export_excel:"#download-excel",
	record_limit:30000,
	init:function() {
		REPORTS.bind_events();
		REPORTS.dates.init();
	},
	bind_events:function() {
		$(REPORTS.run_report_btn).click(function() {
			REPORTS.run_report();
		});
		
		$(REPORTS.report_date_range).change(function() {
			var selected = $(this).val();
			var start_date = "";
			var end_date = "";
			if(selected == "custom") {
				$(REPORTS.report_custom_dates).show();
			} else {
				var $option = $(REPORTS.report_date_range + " option:selected");
				start_date = $option.data("start-date");
				end_date = $option.data("end-date");
				$(REPORTS.report_custom_dates).hide();
			}
			$(REPORTS.report_start_date).val(start_date);
			$(REPORTS.report_end_date).val(end_date);
		});
		
		$(REPORTS.export_excel).click(function() {
			var url = REPORTS.build_report_url(PAGE_CONSTANTS.FORMAT.excel);
			$(this).prop("href",url);
			return true;
		});
	},
	dates:{
		format:"MM/YYYY",
		init:function() {
			REPORTS.dates.bind_events();
			REPORTS.dates.setup_dates();
		},
		bind_events:function() {
			
		},
		setup_dates:function() {
			var output = "";
			//output += "<option value=\"\">All Time</option>";
			output += "<option selected=\"selected\" value=\"this-month\" data-start-date=\"" + moment().startOf("month").format(REPORTS.dates.format) + "\" data-end-date=\"" + moment().endOf("month").format(REPORTS.dates.format) + "\">This Month</option>";
			
			var start_moment = moment().startOf("year").subtract(1, "year");
			var now = moment().subtract(1, "months");
			var end_moment = moment().subtract(1, "months");
			
			while(now > start_moment) {
				output += "<option value=\"year\" data-start-date=\"" + now.startOf("month").format(REPORTS.dates.format) + "\" data-end-date=\"" + now.endOf("month").format(REPORTS.dates.format) + "\">" + now.format("MM/YYYY"); + "</option>";
				now.subtract(1, "months");
			}
			
			//years
			output += "<option value=\"year\" data-start-date=\"" + moment().startOf("year").format(REPORTS.dates.format) + "\" data-end-date=\"" + moment().endOf("year").format(REPORTS.dates.format) + "\">This Year</option>";
			end_moment = moment("01/2016",REPORTS.dates.format);
			now = moment().subtract(1, "year");
			while(end_moment.year() <= now.year()) {
				output += "<option value=\"year\" data-start-date=\"" + now.startOf("year").format(REPORTS.dates.format) + "\" data-end-date=\"" + now.endOf("year").format(REPORTS.dates.format) + "\">" + now.year(); + "</option>";
				now.subtract(1, "year");
			}

			output += "<option value=\"custom\">Custom Dates</option>";
			$(REPORTS.report_date_range).html(output);
			
			//set the initial dates
			var $option = $(REPORTS.report_date_range + " option:selected");
			var start_date = $option.data("start-date");
			var end_date = $option.data("end-date");
			$(REPORTS.report_start_date).val(start_date);
			$(REPORTS.report_end_date).val(end_date);
		}
	},
	build_report_url:function(report_format) {
		var url = PAGE_CONSTANTS.BASE_URL + "reports/laborhours." + report_format + "?";
		
		var start_date = $(REPORTS.report_start_date).val();
		var end_date = $(REPORTS.report_end_date).val();
		var service = $(REPORTS.report_service).val();
		var customer = $(REPORTS.report_customer).val();
		var include_children = $(REPORTS.report_customer + " option:selected").data("parent");
		
		if(report_format == PAGE_CONSTANTS.FORMAT.excel) url += "export"
		if(start_date && end_date) {
			url += "&sd=" + start_date + "&ed=" + end_date;
		}
		if(service != "") {
			url += "&ospId=" + service;
		}
		if(customer) url += "&cid=" + customer;
		if(include_children) url += "&chldrn=true";
		if(REPORTS.record_limit) url += "&lim=" + REPORTS.record_limit;
		
		return url;
	},
	run_report:function() {
		//clear validation message
		UTIL.clear_msg_in_content_box($(REPORTS.filter_criteria));
		$(REPORTS.results_msg).hide();
		
		//get variables for validation
		var start_date = $(REPORTS.report_start_date).val();
		var end_date = $(REPORTS.report_end_date).val();
		var service = $(REPORTS.report_service).val();
		var customer = $(REPORTS.report_customer).val();
		var include_children = $(REPORTS.report_customer + " option:selected").data("parent");
		
		if(start_date && end_date) {
			if(moment(start_date, REPORTS.dates.format).isAfter(moment(end_date, REPORTS.dates.format))) {
				UTIL.add_error_msg_to_content_box($(REPORTS.filter_criteria), $(REPORTS.msg_error_dates_start_before_end).val());
				return false;
			} else if(moment(start_date, REPORTS.dates.format).add(1, 'months').isBefore(moment(end_date, REPORTS.dates.format)) && !service && !customer) {
				UTIL.add_error_msg_to_content_box($(REPORTS.filter_criteria), "Date Ranges cannot be greater than 2 months if you do not select a customer or service.");
				return false;
			} else if(moment(start_date, REPORTS.dates.format).add(1, 'months').isBefore(moment(end_date, REPORTS.dates.format)) && include_children) {
				UTIL.add_error_msg_to_content_box($(REPORTS.filter_criteria), "Date Ranges cannot be greater than 2 months if you select a customer including their child companies.");
				return false;
			}
		} else {
			UTIL.add_error_msg_to_content_box($(REPORTS.filter_criteria), $(REPORTS.msg_error_dates_required).val());
			return false;
		}
		
		var url = REPORTS.build_report_url(PAGE_CONSTANTS.FORMAT.json);
		
		//setup loaders
		UTIL.add_table_loader(REPORTS.data_table, "Retrieving Chart Data...");
		$(REPORTS.report_section).show();
		GLOBAL.set_nav_height();
		
		//display correct info
		$(REPORTS.data_table + " tfoot").html("");
		
		//ajax call
		$.ajax ({
			url: url,
			type: "GET",
			success:function(data) {
				console.log("data");
				if(data.resultCount > REPORTS.record_limit) {
					$(REPORTS.results_msg).show();
					$(REPORTS.data_table + " tbody").html("");
				} else {
					REPORTS.build_table(data.genericData);
					GLOBAL.set_nav_height();
				}
			},
			error:function(jqXHR, textStatus, errorThrown) {
				var json = $.parseJSON(jqXHR.responseText);
				var message = "An error occurred. Please adjust your filter criteria and try again. If the problem persists, contact the Service Insight administrator.";
				if(json && json.message) {
					message = json.message;
				}
				$(REPORTS.data_table + " tbody").html("");
				UTIL.add_error_msg_to_content_box($(REPORTS.filter_criteria), message);
			}
		});
	},
	build_table:function(data) {
		var output = "";
		var total_hours = 0;
		var is_manager = false;
		if(GLOBAL && GLOBAL.is_manager) is_manager = GLOBAL.is_manager;
		var colspan = 6;
		if(is_manager) colspan = 8;
		
		if(data && data.length > 0) {
			for(var i=0; i < data.length; i++) {
				var record = data[i];
				var hours = record.hours;
				var service = "";
				var customer = "";
				var worker = "";
				var date = "";
				var task = "";
				var tier_name = "";
				var tier_code = "";
				var ticket = "";
				if(record.customerName) customer = record.customerName;
				if(record.serviceName) service = record.serviceName; 
				if(record.worker) worker = record.worker; 
				//if(record.workDate) date = moment(record.workDate, "YYYY-MM-DD").format("MM/YYYY");
				if(record.workDate) date = UTIL.convert_dates_for_ui(record.workDate);
				if(record.taskDescription) task = record.taskDescription;
				if(record.ticket) ticket = record.ticket;
				if(record.tierName) tier_name = record.tierName;
				if(record.tierCode) tier_code = record.tierCode;
				
				output += "<tr>";
				output += "<td>" + customer + "</td>";
				output += "<td>" + worker + "</td>";
				output += "<td>" + service + "</td>";
				output += "<td>" + ticket + "</td>";
				output += "<td>" + task + "</td>";
				if(is_manager) {
					output += "<td>" + tier_name + "</td>";
					output += "<td>" + tier_code + "</td>";
				}
				output += "<td>" + date +  "</td>";
				output += "<td class=\"right\">" + hours + "</td>";
				output += "</tr>";
				total_hours += hours;
			}
		} else {
			output = "<tr><td class=\"no-results\" colspan=\"" + colspan + "\">No results returned.</td></tr>";
		}
		
		$(REPORTS.data_table + " tbody").html(output);
		
		var footer_output = "<tr class=\"total-row\"><td colspan=\"" + colspan + "\" class=\"right\">TOTAL</td><td class=\"total-column\">" + Number(Math.round(total_hours+'e3')+'e-3') + "</td></tr>";
		$(REPORTS.data_table + " tfoot").html(footer_output);
	}
};

$(document).ready(function() {
	REPORTS.init();
});