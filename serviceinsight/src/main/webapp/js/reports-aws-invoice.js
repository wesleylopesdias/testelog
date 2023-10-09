var REPORTS = {
	usage_data_table:"#report-usage-data-table",
	report_date_range:"#report-date-range",
	run_report_btn:"#run-report-btn",
	report_section:".report-section",
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
			
			//var start_month_moment = moment().subtract(12, "months");
			var start_month_moment = moment("12/2017",REPORTS.dates.format);
			var end_month_moment = moment();
			while(end_month_moment > start_month_moment) {
				output += "<option value=\"year\" data-month=\"" + end_month_moment.startOf("month").format(REPORTS.dates.format) + "\">" + end_month_moment.format(REPORTS.dates.display_format); + "</option>";
				end_month_moment.subtract(1, "month");
			}

			$(REPORTS.report_date_range).html(output);
		}
	},
	build_report_url:function(report_format) {
		var url = PAGE_CONSTANTS.BASE_URL + "reports/awsinvoice." + report_format + "?";
		
		var month = $(REPORTS.report_date_range + " option:selected").data("month");
		
		url += "invd=" + month;
		if(report_format == PAGE_CONSTANTS.FORMAT.excel) url += "&export";
		
		return url;
	},
	run_report:function() {
		//clear validation message
		//UTIL.clear_msg_in_content_box($(REPORTS.filter_criteria));
		
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
				REPORTS.build_usage_table(data.usageLineItems);
				GLOBAL.set_nav_height();
			}
		});
	},
	build_usage_table:function(data) {
		var output = "";
		var footer_output = "";
		var aws_total = 0;
		var si_total = 0;
		if(data && data.length > 0) {
			for(var i=0; i < data.length; i++) {
				var record = data[i];
				var aws_customer_name = record.awsCustomerName;
				var suscription_name = record.awsSubscriptionName;
				var aws_monthly_total = record.awsMonthlyCost;
				var si_customer_name = record.siCustomerName;
				var si_onetime = record.siOnetimeRevenue;
				var si_recurring = record.siRecurringRevenue;
				var icon = "";
				var match_class = "";
				
				if(!aws_customer_name) aws_customer_name = "";
				if(!si_customer_name) si_customer_name = "";
				if(!si_onetime) si_onetime = 0;
				if(!si_recurring) si_recurring = 0;
				var si_monthly_total = si_onetime + si_recurring;
				
				if(si_customer_name && aws_customer_name) {
					icon = "<i class=\"fa fa-check-circle\"></i>";
					match_class = "match";
				}
				
				output += "<tr class=\"" + match_class + "\">";
				output += "<td>" + icon + "</td>";
				output += "<td>" + aws_customer_name + "</td>";
				output += "<td>" +  suscription_name +  "</td>";
				output += "<td class=\"right\">" +  UTIL.convert_currency_for_ui(aws_monthly_total) +  "</td>";
				output += "<td>&nbsp;</td>";
				output += "<td>" + si_customer_name + "</td>";
				output += "<td class=\"right\">" +  UTIL.convert_currency_for_ui(si_monthly_total) +  "</td>";
				output += "</tr>";
				
				aws_total += aws_monthly_total;
				si_total += si_monthly_total;
			}
			
			footer_output = "<tr class=\"total-row\"><td></td><td></td><td class=\"right\">AWS Total</td><td class=\"total-column\">" + accounting.formatMoney(aws_total) + "</td><td></td><td class=\"right\">SI Total</td><td class=\"total-column\">" + accounting.formatMoney(si_total) + "</td></tr>";
		} else {
			output = "<tr><td colspan=\"7\" class=\"no-results\">No results returned.</td></tr>";
		}
		
		$(REPORTS.usage_data_table + " tbody").html(output);
		$(REPORTS.usage_data_table + " tfoot").html(footer_output);
	}
};

$(document).ready(function() {
	REPORTS.init();
});