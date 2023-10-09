var REPORTS = {
	data_table_services:"#report-data-table-services",
	data_table_customers:"#report-data-table-customers",
	filter_criteria:".filter-criteria",
	report_date_range:"#report-date-range",
	report_custom_dates:"#report-custom-dates",
	report_start_date:"#report-start-date",
	report_end_date:"#report-end-date",
	run_report_btn:"#run-report-btn",
	report_section:".report-section",
	msg_error_dates_required:"#msg-error-dates-required",
	msg_error_dates_start_before_end:"#msg-error-start-before-end",
	msg_no_services_data:"#msg-no-services-data",
	msg_no_customer_quotes_data:"#msg-no-customer-quotes-data",
	export_excel_services:"#download-excel-services",
	export_excel_customers:"#download-excel-customers",
	dates_format:"MM/YYYY",
	init:function() {
		REPORTS.bind_events();
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
		
		$(REPORTS.export_excel_services).click(function() {
			var url = REPORTS.build_report_url(PAGE_CONSTANTS.FORMAT.excel);
			$(this).prop("href",url);
			return true;
		});
		
		$(REPORTS.export_excel_customers).click(function() {
			var url = REPORTS.build_report_url(PAGE_CONSTANTS.FORMAT.excel);
			$(this).prop("href",url);
			return true;
		});
	},
	build_report_url:function(report_format) {
		var url = PAGE_CONSTANTS.BASE_URL + "reports/pricingpipeline." + report_format + "?";
		
		var start_date = $(REPORTS.report_start_date).val();
		var end_date = $(REPORTS.report_end_date).val();
		
		if (report_format == PAGE_CONSTANTS.FORMAT.excel) url += "export";
		if (start_date && end_date) {
			url += "&sd=" + start_date + "&ed=" + end_date;
		}
		return url;
	},
	run_report:function() {
		// clear validation message
		UTIL.clear_msg_in_content_box($(REPORTS.filter_criteria));
		
		// get variables for validation
		var start_date = $(REPORTS.report_start_date).val();
		var end_date = $(REPORTS.report_end_date).val();
		
		if (start_date && end_date) {
			if (moment(start_date, REPORTS.dates_format).isAfter(moment(end_date, REPORTS.dates_format))) {
				UTIL.add_error_msg_to_content_box($(REPORTS.filter_criteria), $(REPORTS.msg_error_dates_start_before_end).val());
				return false;
			} 
		} else {
			UTIL.add_error_msg_to_content_box($(REPORTS.filter_criteria), $(REPORTS.msg_error_dates_required).val());
			return false;
		}
		
		var url = REPORTS.build_report_url(PAGE_CONSTANTS.FORMAT.json);
		
		// setup loaders
		UTIL.add_table_loader(REPORTS.data_table_services, "Retrieving Services Data...");
		UTIL.add_table_loader(REPORTS.data_table_customers, "Retrieving Customer Quotes Data...");
		$(REPORTS.report_section).show();
		GLOBAL.set_nav_height();
		
		console.log("url:[" + url + "]");
		
		// ajax call
		$.ajax ({
			url: url,
			type: "GET",
			success:function(data) {
				REPORTS.build_tables(data);
				GLOBAL.set_nav_height();
			}
		});			
	},
	build_tables:function(data) {
		var output = "";
		var service;
		var offering;
		var totalItems;
		var unitLabel;
		if (data.services.length==0) {
			output +="<tr><td>" + $(REPORTS.msg_no_services_data).val() + "</td></tr>";
		} else {
			for (var i=0; i < data.services.length; i++) {
				service = data.services[i].serviceOfferingName;
				offering = data.services[i].serviceName; 
				totalItems = data.services[i].totalItems;
				unitLabel = data.services[i].unitLabel;
				output += "<tr>";
				output += "<td>" + service + "</td>";
				output += "<td>" + offering + "</td>";
				output += "<td class=\"right\">" + UTIL.convert_number_for_ui(totalItems) + "</td>";
				output += "<td>" + unitLabel + "</td>";
				output += "</tr>";
			}
		}
		$(REPORTS.data_table_services + " tbody").html(output);
		
		output = "";
		var quoteNumber;
		var customerName;
		var targetCloseDate;
		var services;
		var pricingQuoteUrl = data.pricingBaseURL;
		var quoteHref;
		if (data.customerQuotes.length==0) {
			output +="<tr><td colspan=\"2\">" + $(REPORTS.msg_no_customer_quotes_data).val() + "</td></tr>";
		} else {
			for (var x=0; x < data.customerQuotes.length; x++) {
				quoteNumber = data.customerQuotes[x].quoteNumber;
				quoteHref = pricingQuoteUrl + data.customerQuotes[x].quoteId;
				customerName = data.customerQuotes[x].customerName;
				targetCloseDate = data.customerQuotes[x].closeDate;
				services = data.customerQuotes[x].services;
				totalItems = data.customerQuotes[x].totalItems;
				output += "<tr>";
				output += "<td class=\"valigntop\"><a href=\"" + quoteHref + "\" target=\"osp-pricing\">" + quoteNumber + "</a></td>";
				output += "<td class=\"valigntop\">" + customerName + "</td>";
				output += "<td class=\"valigntop\">" + targetCloseDate + "</td>";
				output += "<td class=\"valigntop\">" + services + "</td>";
				output += "<td class=\"right valigntop\">" + UTIL.convert_number_for_ui(totalItems) + "</td>";
				output += "</tr>";
			}
		}
		$(REPORTS.data_table_customers + " tbody").html(output);
	}
};

$(document).ready(function() {
	REPORTS.init();
});