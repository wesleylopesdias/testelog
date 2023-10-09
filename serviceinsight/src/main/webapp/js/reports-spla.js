var REPORTS = {
	spla_data_div:"#report-spla-data-div",
	report_date_range:"#report-date-range",
	report_customer:"#report-customer",
	report_vendor:"#report-vendor",
	report_spla_filter:"#report-spla-filter",
	report_spla_device_filter:"#report-spla-device-filter",
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
			var start_month_moment = moment("12/2016",REPORTS.dates.format);
			var end_month_moment = moment();
			while(end_month_moment > start_month_moment) {
				output += "<option value=\"year\" data-month=\"" + end_month_moment.startOf("month").format(REPORTS.dates.format) + "\">" + end_month_moment.format(REPORTS.dates.display_format); + "</option>";
				end_month_moment.subtract(1, "month");
			}

			$(REPORTS.report_date_range).html(output);
		}
	},
	build_report_url:function(report_format) {
		var url = PAGE_CONSTANTS.BASE_URL + "reports/splareport." + report_format + "?";
		
		var month = $(REPORTS.report_date_range + " option:selected").data("month");
                var customer = $(REPORTS.report_customer).val();
                var vendor = $(REPORTS.report_vendor).val();
                var spla_filter = $(REPORTS.report_spla_filter).val();
                var spla_device_filter = $(REPORTS.report_spla_device_filter).val();
		
		url += "monthof=" + month;
                if(customer) url += "&cid=" + customer;
                if(vendor) url += "&vendor=" + vendor;
                if(spla_filter) url += "&splaid=" + spla_filter;
                if(spla_device_filter) url += "&spladevid=" + spla_device_filter;
		if(report_format == PAGE_CONSTANTS.FORMAT.excel) url += "&export";
		
		return url;
	},
	run_report:function() {
		//clear validation message
		//UTIL.clear_msg_in_content_box($(REPORTS.filter_criteria));
		
		var url = REPORTS.build_report_url(PAGE_CONSTANTS.FORMAT.json);
		
		//setup loaders
		UTIL.add_chart_loader(REPORTS.spla_data_div, "Retrieving Report Data...");
		$(REPORTS.report_section).show();
		GLOBAL.set_nav_height();
		
		//ajax call
		$.ajax ({
			url: url,
			type: "GET",
			success:function(data) {
				REPORTS.build_spla_table(data);
				GLOBAL.set_nav_height();
			}
		});
	},
	build_spla_table:function(data) {
		var output = "";
		var spla_total = 0;
		var si_total = 0;
		if(data) {
                    spla_total = data.totalCost;
                    si_total = data.totalRevenue;
                    var customers = data.customers;
                    var splaSummaries = data.splasummaries;
                    if(customers && customers.length > 0) {
                        for(var i=0; i < customers.length; i++) {
                            var customer = customers[i];
                            var customerName = customer.customer;
                            var contracts = customer.contracts;
                            var spla_customer_total = customer.totalCost;
                            var si_customer_total = customer.totalRevenue;
                            if(contracts && contracts.length > 0) {
                                output += "<div style=\"margin-bottom:15px;\"><b>"+customerName+"</b>";
                                for(var j=0; j < contracts.length; j++) {
                                    var contract = contracts[j];
                                    var spla_contract_total = contract.totalCost;
                                    var si_contract_total = contract.totalRevenue;
                                    output += "<div class=\"row\">";
                                    output += "<div><a target=\"_blank\" href=\""+PAGE_CONSTANTS.BASE_URL+"contracts/"+contract.contractId+"\">"+contract.contractName+"</a></div>";
                                    // costs...
                                    output += "<div class=\"column-2\">";
                                    var costs = contract.costs;
                                    if(costs && costs.length > 0) {
                                        output += "<table class=\"small-table\">";
                                        output += "<thead><tr>";
                                        output += "<th>Part Number</th><th>SPLA</th><th style=\"text-align:center;\">Quantity</th><th>Cost</th>";
                                        output += "</tr></thead><tbody>";
                                        for(var k=0; k < costs.length; k++) {
                                            var cost = costs[k];
                                            output += "<tr>";
                                            output += "<td>"+cost.partNumber+"</td><td>"+cost.spla+"</td><td style=\"text-align:center;\">"+cost.quantity+"</td><td>"+UTIL.convert_currency_for_ui(cost.amount)+"</td>";
                                            output += "</tr>";
                                        }
                                        output += "</tbody></table>";
                                    }
                                    output += "</div>";
                                    // revenues...
                                    output += "<div class=\"column-2\">";
                                    var revenues = contract.revenues;
                                    if(revenues && revenues.length > 0) {
                                        output += "<table class=\"small-table\">";
                                        output += "<thead><tr>";
                                        output += "<th>Service / Product</th><th>Part Number</th><th>SI Revenue</th>";
                                        output += "</tr></thead><tbody>";
                                        for(var k=0; k < revenues.length; k++) {
                                            var revenue = revenues[k];
                                            output += "<tr>";
                                            output += "<td>"+revenue.service+"</td><td>"+revenue.partNumber+"</td><td>"+UTIL.convert_currency_for_ui(revenue.formattedTotalRevenue)+"</td>";
                                            output += "</tr>";
                                        }
                                        output += "</tbody></table>";
                                    }
                                    output += "</div>";
                                    output += "<div class=\"column-2\" style=\"margin-top:5px;text-align:right;\">Contract Cost Total: "+UTIL.convert_currency_for_ui(spla_contract_total)+"</div>";
                                    output += "<div class=\"column-2\" style=\"margin-top:5px;text-align:right;\">SI Contract Total Revenue: "+UTIL.convert_currency_for_ui(si_contract_total)+"</div>";
                                    output += "</div>"; // outer row...
                                }
                                output += "<div class=\"column-2\" style=\"margin-top:5px;text-align:right;\">Customer Cost Total: "+UTIL.convert_currency_for_ui(spla_customer_total)+"</div>";
                                output += "<div class=\"column-2\" style=\"margin-top:5px;text-align:right;\">SI Customer Total Revenue: "+UTIL.convert_currency_for_ui(si_customer_total)+"</div>";
                                output += "</div>";
                            }
                        }
                        output += "<div class=\"column-2\" style=\"margin-top:5px;text-align:right;\"><b>SPLA Cost Total: "+UTIL.convert_currency_for_ui(spla_total)+"</b></div>";
                        output += "<div class=\"column-2\" style=\"margin-top:5px;text-align:right;\"><b>SI Total Revenue: "+UTIL.convert_currency_for_ui(si_total)+"</b></div>";
                        output += "</div>";
                    } else {
                        output = "<tr><td colspan=\"6\" class=\"no-results\">No results returned.</td></tr>";
                    }
                    if (splaSummaries && splaSummaries.length > 0) {
                        output += "<div id=\"spla_summary\" style=\"margin-top:20px;\">";
                        output += "<span>SPLA Total Summary</span><p/>"
                        output += "<table class=\"small-table\">";
                        output += "<thead><tr>";
                        output += "<th width=\"50%\">SPLA Cost Item</th><th style=\"text-align:center;\">Total Quantity</th><th>Total Cost</th>";
                        output += "</tr></thead><tbody>";
                        for(var i=0; i < splaSummaries.length; i++) {
                            var splaSummary = splaSummaries[i];
                            output += "<tr>";
                            output += "<td>"+splaSummary.name+"</td><td style=\"text-align:center;\">"+splaSummary.quantity+"</td><td>"+UTIL.convert_currency_for_ui(splaSummary.cost)+"</td>";
                            output += "</tr>";
                        }
                        output += "</tbody></table>";
                        output += "</div>"
                    }
		}
		
		$(REPORTS.spla_data_div).html(output);
	}
};

$(document).ready(function() {
	REPORTS.init();
});