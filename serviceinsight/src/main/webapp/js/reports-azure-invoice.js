var REPORTS = {
	usage_data_table:"#report-usage-data-table",
	license_data_table:"#report-license-data-table",
	onetime_data_table:"#report-onetime-data-table",
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
		var url = PAGE_CONSTANTS.BASE_URL + "reports/azureinvoice." + report_format + "?";
		
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
		UTIL.add_table_loader(REPORTS.license_data_table, "Retrieving Report Data...");
		UTIL.add_table_loader(REPORTS.onetime_data_table, "Retrieving Report Data...");
		$(REPORTS.license_data_table + " tfoot").html("");
		$(REPORTS.onetime_data_table + " tfoot").html("");
		$(REPORTS.usage_data_table + " tfoot").html("");
		$(REPORTS.report_section).show();
		GLOBAL.set_nav_height();
		
		//ajax call
		$.ajax ({
			url: url,
			type: "GET",
			success:function(data) {
				REPORTS.build_usage_table(data.usageLineItems);
				REPORTS.build_license_table(data.licenseLineItems);
				REPORTS.build_onetime_table(data.onetimeLineItems);
				GLOBAL.set_nav_height();
			}
		});
	},
	build_usage_table:function(data) {
		var output = "";
		var footer_output = "";
		var azure_total = 0;
		var si_total = 0;
		if(data && data.length > 0) {
			for(var i=0; i < data.length; i++) {
				var record = data[i];
				var azure_customer_name = record.azureCustomerName;
				var suscription_name = record.azureSubscriptionName;
				var azure_monthly_total = record.azureMonthlyCost;
				var si_customer_name = record.siCustomerName;
				var si_onetime = record.siOnetimeRevenue;
				var si_recurring = record.siRecurringRevenue;
				var icon = "";
				var match_class = "";
				
				if(!azure_customer_name) azure_customer_name = "";
				if(!si_customer_name) si_customer_name = "";
				if(!si_onetime) si_onetime = 0;
				if(!si_recurring) si_recurring = 0;
				var si_monthly_total = si_onetime + si_recurring;
				
				if(si_customer_name && azure_customer_name) {
					icon = "<i class=\"fa fa-check-circle\"></i>";
					match_class = "match";
				}
				
				output += "<tr class=\"" + match_class + "\">";
				output += "<td>" + icon + "</td>";
				output += "<td>" + azure_customer_name + "</td>";
				output += "<td>" +  suscription_name +  "</td>";
				output += "<td class=\"right\">" +  UTIL.convert_currency_for_ui(azure_monthly_total) +  "</td>";
				output += "<td>&nbsp;</td>";
				output += "<td>" + si_customer_name + "</td>";
				output += "<td class=\"right\">" +  UTIL.convert_currency_for_ui(si_monthly_total) +  "</td>";
				output += "</tr>";
				
				azure_total += azure_monthly_total;
				si_total += si_monthly_total;
			}
			
			footer_output = "<tr class=\"total-row\"><td></td><td></td><td class=\"right\">Azure Total</td><td class=\"total-column\">" + accounting.formatMoney(azure_total) + "</td><td></td><td class=\"right\">SI Total</td><td class=\"total-column\">" + accounting.formatMoney(si_total) + "</td></tr>";
		} else {
			output = "<tr><td colspan=\"7\" class=\"no-results\">No results returned.</td></tr>";
		}
		
		$(REPORTS.usage_data_table + " tbody").html(output);
		$(REPORTS.usage_data_table + " tfoot").html(footer_output);
	},
	build_license_table:function(data) {
		var output = "";
		var footer_output = "";
		var azure_total = 0;
		var si_total = 0;
		if(data) {
			//for(var i=0; i < data.length; i++) {
			var count = 0;
			for(var customer in data) {
				var record = data[customer];
				var customer_name = customer;
				var row_class = "odd";
				if(count % 2 == 0) row_class = "even"; 
				
				output += "<tr class=\"" + row_class + " title-row\"><td colspan=\"9\">" + customer_name + "</td></tr>";
				
				var si_customer_total = 0;
				var azure_customer_total = 0;
				for(var k=0; k < record.length; k++) {
					var lineitem = record[k];
					var azure_offer_name = lineitem.azureOfferName;
					var azure_quantity = lineitem.azureQuantity;
					var azure_monthly_total = lineitem.azureMonthlyCost;
					var azure_monthly_total_display = "";
					var si_device_description = lineitem.siDeviceDescription;
					var si_part_number = lineitem.siDevicePartNumber;
					var si_unit_count = lineitem.siDeviceUnitCount;
					var si_quantity = lineitem.siQuantity;
					var si_monthly_total = (lineitem.siOnetimeRevenue + lineitem.siRecurringRevenue);
					var si_monthly_total_display = "";
					var icon = "";
					var match_class = "";
					
					if(!azure_offer_name) azure_offer_name = "";
					if(!azure_quantity) azure_quantity = "";
					
					if(!azure_monthly_total) {
						azure_monthly_total_display = "";
					} else {
						azure_monthly_total_display = UTIL.convert_currency_for_ui(azure_monthly_total);
					}
					if(!si_device_description) si_device_description = "";
					if(!si_part_number) si_part_number = "";
					if(!si_quantity) {
						si_quantity = "";
					} else {
						if(si_unit_count) {
							/*
							if(si_quantity == 1) {
								si_quantity = si_unit_count + " Units";
							} else {
								si_quantity += " <span style=\"font-size:0.85em;\">(" + si_unit_count + " Units)</span>";
							}*/
							if(si_unit_count) {
								si_quantity = si_unit_count;
							}
						}
					}
					if(!si_monthly_total) {
						si_monthly_total_display = "";
					} else {
						si_monthly_total_display = UTIL.convert_currency_for_ui(si_monthly_total);
					}
					if(azure_offer_name && si_device_description) {
						icon = "<i class=\"fa fa-check-circle\"></i>";
						match_class = " match";
					}
					
					output += "<tr class=\"" + row_class + match_class + "\">";
					output += "<td class=\"right\">" + icon + "</td>";
					output += "<td>" +  azure_offer_name +  "</td>";
					output += "<td class=\"right\">" +  azure_quantity +  "</td>";
					output += "<td class=\"right\">" + azure_monthly_total_display  +  "</td>";
					output += "<td>&nbsp;</td>";
					output += "<td>" +  si_device_description +  "</td>";
					output += "<td>" +  si_part_number +  "</td>";
					output += "<td class=\"right\">" + si_quantity + "</td>";
					output += "<td class=\"right\">" + si_monthly_total_display  +  "</td>";
					output += "</tr>";
					
					si_customer_total += si_monthly_total;
					si_total += si_monthly_total;
					azure_customer_total += azure_monthly_total;
					azure_total += azure_monthly_total;
				}
				
				output += "<tr class=\"" + row_class + " subtotal-row\"><td></td><td colspan=\"2\" class=\"right\">Azure Total</td><td class=\"total-column\">" + accounting.formatMoney(azure_customer_total) + "</td><td></td><td class=\"right\" colspan=\"3\">SI Total</td><td class=\"total-column\">" + accounting.formatMoney(si_customer_total) + "</td></tr>";
				count++;
			}
			
			footer_output = "<tr class=\"total-row\"><td></td><td class=\"right\" colspan=\"2\">Azure Total</td><td class=\"total-column\">" + accounting.formatMoney(azure_total) + "</td><td></td><td class=\"right\" colspan=\"3\">SI Total</td><td class=\"total-column\">" + accounting.formatMoney(si_total) + "</td></tr>";
		} else {
			output = "<tr><td colspan=\"6\" class=\"no-results\">No results returned.</td></tr>";
		}
		
		$(REPORTS.license_data_table + " tbody").html(output);
		$(REPORTS.license_data_table + " tfoot").html(footer_output);
	},
	build_onetime_table:function(data) {
		var output = "";
		var footer_output = "";
		var azure_total = 0;
		var si_total = 0;
		if(data) {
			//for(var i=0; i < data.length; i++) {
			var count = 0;
			for(var customer in data) {
				var record = data[customer];
				var customer_name = customer;
				var row_class = "odd";
				if(count % 2 == 0) row_class = "even"; 
				
				output += "<tr class=\"" + row_class + " title-row\"><td colspan=\"9\">" + customer_name + "</td></tr>";
				
				var si_customer_total = 0;
				var azure_customer_total = 0;
				for(var k=0; k < record.length; k++) {
					var lineitem = record[k];
					var azure_offer_name = lineitem.azureOfferName;
					var azure_quantity = lineitem.azureQuantity;
					var azure_item_total = lineitem.customerTotal;
					var azure_item_total_display = "";
					var si_device_description = lineitem.siDeviceDescription;
					var si_part_number = lineitem.siDevicePartNumber;
					var si_unit_count = lineitem.siDeviceUnitCount;
					var si_quantity = lineitem.siQuantity;
					var si_monthly_total = (lineitem.siOnetimeRevenue + lineitem.siRecurringRevenue);
					var si_monthly_total_display = "";
					var icon = "";
					var match_class = "";
					
					if(!azure_offer_name) azure_offer_name = "";
					if(!azure_quantity) azure_quantity = "";
					
					if(!azure_item_total) {
						azure_item_total_display = "";
					} else {
						azure_item_total_display = UTIL.convert_currency_for_ui(azure_item_total);
					}
					if(!si_device_description) si_device_description = "";
					if(!si_part_number) si_part_number = "";
					if(!si_quantity) {
						si_quantity = "";
					} else {
						if(si_unit_count) {
							/*
							if(si_quantity == 1) {
								si_quantity = si_unit_count + " Units";
							} else {
								si_quantity += " <span style=\"font-size:0.85em;\">(" + si_unit_count + " Units)</span>";
							}*/
							if(si_unit_count) {
								si_quantity = si_unit_count;
							}
						}
					}
					if(!si_monthly_total) {
						si_monthly_total_display = "";
					} else {
						si_monthly_total_display = UTIL.convert_currency_for_ui(si_monthly_total);
					}
					if(azure_offer_name && si_device_description) {
						icon = "<i class=\"fa fa-check-circle\"></i>";
						match_class = " match";
					}
					
					output += "<tr class=\"" + row_class + match_class + "\">";
					output += "<td class=\"right\">" + icon + "</td>";
					output += "<td>" +  azure_offer_name +  "</td>";
					output += "<td class=\"right\">" +  azure_quantity +  "</td>";
					output += "<td class=\"right\">" + azure_item_total_display  +  "</td>";
					output += "<td>&nbsp;</td>";
					output += "<td>" +  si_device_description +  "</td>";
					output += "<td>" +  si_part_number +  "</td>";
					output += "<td class=\"right\">" + si_quantity + "</td>";
					output += "<td class=\"right\">" + si_monthly_total_display  +  "</td>";
					output += "</tr>";
					
					si_customer_total += si_monthly_total;
					si_total += si_monthly_total;
					azure_customer_total += azure_item_total;
					azure_total += azure_item_total;
				}
				
				output += "<tr class=\"" + row_class + " subtotal-row\"><td></td><td colspan=\"2\" class=\"right\">Azure Total</td><td class=\"total-column\">" + accounting.formatMoney(azure_customer_total) + "</td><td></td><td class=\"right\" colspan=\"3\">SI Total</td><td class=\"total-column\">" + accounting.formatMoney(si_customer_total) + "</td></tr>";
				count++;
			}
			
			footer_output = "<tr class=\"total-row\"><td></td><td class=\"right\" colspan=\"2\">Azure Total</td><td class=\"total-column\">" + accounting.formatMoney(azure_total) + "</td><td></td><td class=\"right\" colspan=\"3\">SI Total</td><td class=\"total-column\">" + accounting.formatMoney(si_total) + "</td></tr>";
		} else {
			output = "<tr><td colspan=\"6\" class=\"no-results\">No results returned.</td></tr>";
		}
		
		$(REPORTS.onetime_data_table + " tbody").html(output);
		$(REPORTS.onetime_data_table + " tfoot").html(footer_output);
	}
};

$(document).ready(function() {
	REPORTS.init();
});