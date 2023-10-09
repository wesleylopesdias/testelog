var REPORTS = {
	data_table:"#report-data-table",
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
			var start_month_moment = moment("12/2015",REPORTS.dates.format);
			var end_month_moment = moment();
			while(end_month_moment > start_month_moment) {
				output += "<option value=\"year\" data-month=\"" + end_month_moment.startOf("month").format(REPORTS.dates.format) + "\">" + end_month_moment.format(REPORTS.dates.display_format); + "</option>";
				end_month_moment.subtract(1, "month");
			}

			$(REPORTS.report_date_range).html(output);
		}
	},
	build_report_url:function(report_format) {
		var url = PAGE_CONSTANTS.BASE_URL + "reports/laborbreakdown." + report_format + "?";
		
		var month = $(REPORTS.report_date_range + " option:selected").data("month");
		
		url += "month=" + month;
		if(report_format == PAGE_CONSTANTS.FORMAT.excel) url += "&export";
		
		return url;
	},
	run_report:function() {
		//clear validation message
		//UTIL.clear_msg_in_content_box($(REPORTS.filter_criteria));
		
		var url = REPORTS.build_report_url(PAGE_CONSTANTS.FORMAT.json);
		
		//setup loaders
		UTIL.add_table_loader(REPORTS.data_table, "Retrieving Chart Data...");
		$(REPORTS.report_section).show();
		GLOBAL.set_nav_height();
		
		//ajax call
		$.ajax ({
			url: url,
			type: "GET",
			success:function(data) {
				REPORTS.build_table(data);
				GLOBAL.set_nav_height();
			}
		});
	},
	set_chart_height:function(records) {
		var height = 500;
		if(records) height = 25 * records;
		if(height < 500) height = 500;
		$(REPORTS.chart).height(height);
		
		GLOBAL.set_nav_height();
	},
	build_table:function(data) {
		var output = "";
		var footer_output = "";
		var total_base = 0;
		var total_addl = 0;
		var total = 0;
		if(data && data.length > 0) {
			for(var i=0; i < data.length; i++) {
				var record = data[i];
				var tier_name = record.tierName;
				var tier_code = record.tierCode;
				var tier_base_rate = record.tierRate;
				var tier_base_rate_total = record.tierTotal;
				var tier_addl_rate = record.tierAddlRate;
				var tier_addl_rate_total = record.tierAddlTotal;
				var tier_overall_total = tier_base_rate_total + tier_addl_rate_total;
				
				output += "<tr>";
				output += "<td>" + tier_code + "</td>";
				output += "<td class=\"right\">" +  UTIL.convert_currency_for_ui(tier_base_rate) +  "</td>";
				output += "<td class=\"right\">" +  UTIL.convert_currency_for_ui(tier_addl_rate) +  "</td>";
				output += "<td class=\"right\">" +  UTIL.convert_currency_for_ui(tier_base_rate_total) +  "</td>";
				output += "<td class=\"right\">" +  UTIL.convert_currency_for_ui(tier_addl_rate_total) +  "</td>";
				output += "<td class=\"right\">" +  UTIL.convert_currency_for_ui(tier_overall_total) +  "</td>";
				output += "</tr>";
				
				total_base += tier_base_rate_total;
				total_addl += tier_addl_rate_total;
				total += tier_overall_total;
			}
			
			footer_output = "<tr class=\"total-row\"><td colspan=\"2\"></td><td class=\"right\">TOTAL</td><td class=\"total-column\">" + accounting.formatMoney(total_base) + "</td><td class=\"total-column\">" + accounting.formatMoney(total_addl) + "</td><td class=\"total-column\">" + accounting.formatMoney(total) + "</td></tr>";
		} else {
			output = "<tr><td colspan=\"6\" class=\"no-results\">No results returned.</td></tr>";
		}
		
		$(REPORTS.data_table + " tbody").html(output);
		$(REPORTS.data_table + " tfoot").html(footer_output);
	}
};

$(document).ready(function() {
	REPORTS.init();
});