var REPORTS = {
	chart:"#chart",
	data_table:"#report-data-table",
	filter_criteria:".filter-criteria",
	report_start_date:"#report-start-date",
	report_date_range:"#report-date-range",
	report_status:"#report-status",
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
				renewal_date = $option.data("renewal-date");
				$(REPORTS.report_custom_dates).hide();
			}
			$(REPORTS.report_start_date).val(renewal_date);
			//$(REPORTS.report_end_date).val(end_date);
		});
		
		$(REPORTS.export_excel).click(function() {
			var url = REPORTS.build_report_url(PAGE_CONSTANTS.FORMAT.excel);
			$(this).prop("href",url);
			return true;
		});
	},
	dates:{
		format:"MM/DD/YYYY",
		nrc_header:"#nrc-header",
		mrc_header:"#mrc-header",
		init:function() {
			REPORTS.dates.bind_events();
			REPORTS.dates.setup_dates();
			REPORTS.dates.set_headers();
		},
		bind_events:function() {
			
		},
		setup_dates:function() {
			var output = "";
			output += "<option value=\"this-month\" data-renewal-date=\"" + moment().subtract(90, "days").format(REPORTS.dates.format) + "\">Last 90 Days</option>";
			output += "<option value=\"this-month\" data-renewal-date=\"" + moment().subtract(60, "days").format(REPORTS.dates.format) + "\">Last 60 Days</option>";
			output += "<option value=\"this-month\" data-renewal-date=\"" + moment().subtract(30, "days").format(REPORTS.dates.format) + "\">Last 30 Days</option>";
			output += "<option value=\"this-month\" data-renewal-date=\"" + moment().add(30, "days").format(REPORTS.dates.format) + "\">Next 30 Days</option>";
			output += "<option value=\"this-month\" data-renewal-date=\"" + moment().add(60, "days").format(REPORTS.dates.format) + "\">Next 60 Days</option>";
			output += "<option selected=\"selected\" value=\"this-month\" data-renewal-date=\"" + moment().add(90, "days").format(REPORTS.dates.format) + "\">Next 90 Days</option>";
			output += "<option value=\"this-month\" data-renewal-date=\"" + moment().add(120, "days").format(REPORTS.dates.format) + "\">Next 120 Days</option>";
			output += "<option value=\"this-month\" data-renewal-date=\"" + moment().add(6, "months").format(REPORTS.dates.format) + "\">Next 6 Months</option>";
			output += "<option value=\"this-month\" data-renewal-date=\"" + moment().add(9, "months").format(REPORTS.dates.format) + "\">Next 9 Months</option>";
			output += "<option value=\"this-month\" data-renewal-date=\"" + moment().add(12, "months").format(REPORTS.dates.format) + "\">Next 12 Months</option>";
			output += "<option value=\"this-month\" data-renewal-date=\"" + moment().add(18, "months").format(REPORTS.dates.format) + "\">Next 18 Months</option>";
			output += "<option value=\"this-month\" data-renewal-date=\"" + moment().add(2, "years").format(REPORTS.dates.format) + "\">Next 2 Years</option>";
			output += "<option value=\"this-month\" data-renewal-date=\"" + moment().add(3, "years").format(REPORTS.dates.format) + "\">Next 3 Years</option>";
			output += "<option value=\"this-month\" data-renewal-date=\"" + moment().add(4, "years").format(REPORTS.dates.format) + "\">Next 4 Years</option>";
			output += "<option value=\"this-month\" data-renewal-date=\"" + moment().add(5, "years").format(REPORTS.dates.format) + "\">Next 5 Years</option>";
			$(REPORTS.report_date_range).html(output);
			
			//set the initial dates
			var $option = $(REPORTS.report_date_range + " option:selected");
			var start_date = $option.data("start-date");
			var end_date = $option.data("end-date");
			$(REPORTS.report_start_date).val(start_date);
			$(REPORTS.report_end_date).val(end_date);
		},
		set_headers:function() {
			var moment_month = moment().format("MMM 'YY");
			
			$(REPORTS.dates.nrc_header + ", " + REPORTS.dates.mrc_header).html("(" + moment_month + ")");
		}
	},
	build_report_url:function(report_format) {
		var url = PAGE_CONSTANTS.BASE_URL + "reports/renewals." + report_format + "?";
		var $option = $(REPORTS.report_date_range + " option:selected");
		var renewal_date = $option.data("renewal-date");
		var customer = $(REPORTS.report_customer).val();
		var status = $(REPORTS.report_status).val();
		
		if(report_format == PAGE_CONSTANTS.FORMAT.excel) url += "export"
		if(renewal_date) {
			url += "&rd=" + renewal_date;
		}
		if(status) {
			url += "&sts=" + status;
		}
		if(customer) url += "&cid=" + customer;
		
		return url;
	},
	run_report:function() {
		//clear validation message
		UTIL.clear_msg_in_content_box($(REPORTS.filter_criteria));
		$(REPORTS.results_msg).hide();
		
		//get variables for validation
		var renewal_date = $(REPORTS.report_start_date).val();
		/*
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
		}*/
		
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
				REPORTS.build_table(data);
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
				var contract = data[i];
				var customer = "";
				var sdm = "";
				var ae = "";
				var epe = "";
				var bsc = "";
				var renewal_status = "";
				var renewal_change = "";
				if(contract.customerId) customer = contract.customerName;
				if(contract.accountExecutive) ae = contract.accountExecutive.userName;
				if(contract.enterpriseProgramExecutive) epe = contract.enterpriseProgramExecutive.userName;
				if(contract.renewalStatus) {
					renewal_status = contract.renewalStatusDisplay;
					var change = contract.renewalChange;
					if(change > 0) {
						renewal_change = change + "% Increase";
					} else if(change < 0) {
						renewal_change = change + "% Decrease";
					} else {
						renewal_change = "No Change";
					}
				}
				
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
				
				var contract_url = PAGE_CONSTANTS.BASE_URL + "contracts/" + contract.id;
				output += "<tr>";
				output += "<td>" + customer + "</td>";
				output += "<td>" + contract.altId + "</td>";
				output += "<td>" + contract.name + "</td>";
				output += "<td><a href=\"" + contract_url + "\" target=\"_blank\">" + contract.jobNumber + "</a></td>";
				output += "<td>" + ae + "</td>";
				output += "<td>" + epe + "</td>";
				output += "<td>" + sdm + "</td>";
				output += "<td>" + bsc + "</td>";
				output += "<td>" + UTIL.convert_dates_for_ui(contract.serviceStartDate) +  "</td>";
				output += "<td>" + UTIL.convert_dates_for_ui(contract.endDate) +  "</td>";
				output += "<td>" + renewal_status + "</td>";
				output += "<td>" + renewal_change + "</td>";
				output += "<td class=\"right\">" + UTIL.convert_currency_for_ui(contract.monthTotalOnetimeRevenue) + "</td>";
				output += "<td class=\"right\">" + UTIL.convert_currency_for_ui(contract.monthTotalRecurringRevenue) + "</td>";
				output += "</tr>";
			}
		} else {
			output = "<tr><td class=\"no-results\" colspan=\"12\">No results returned.</td></tr>";
		}
		
		$(REPORTS.data_table + " tbody").html(output);
	}
};

$(document).ready(function() {
	REPORTS.init();
});