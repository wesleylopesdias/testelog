var REPORTS = {
	chart:"#chart",
	data_table:"#report-data-table",
	devices_table:"#report-devices-table",
	filter_criteria:".filter-criteria",
	expense_category:"#expense-category",
	report_customer:"#report-customer",
        push_applied_date:"#push-applied-date",
        push_id:"#push-id",
        push_custid:"#push-custid",
	report_date_range:"#report-date-range",
	report_custom_dates:"#report-custom-dates",
	report_start_date:"#report-start-date",
	report_end_date:"#report-end-date",
	run_report_btn:"#run-report-btn",
	expense_category_display:"#current-category",
	customer_display:"#current-customer",
	month_display:"#current-month",
	report_section:".report-section",
	total_services_display:"#total-services",
	total_devices_display:"#total-devices",
	service_unit_count_display:"#service-unit-cost",
	device_unit_count_display:"#device-unit-cost",
	loader:"#report-loader",
	export_excel:"#download-excel",
	codes:{ all_services:0, all_services_no_adjustments:99999 },
	chart_obj:null,
	init:function() {
		REPORTS.bind_events();
		REPORTS.dates.init();
                
                /**
                 * When the /reports/expensecategories page is requested, it can contain the request
                 * parameters for actually retrieving the requested data for the month and expense category
                 */
                var pushId = $(REPORTS.push_id).val();
                var pushCustId = $(REPORTS.push_custid).val();
                var pushAppliedDate = $(REPORTS.push_applied_date).val();
                if (pushId && pushAppliedDate) {
                    $(REPORTS.expense_category + " option[value='"+pushId+"']").prop('selected', true);
                    $(REPORTS.report_date_range + " option").each(function() {
                        var $this = $(this);
                        if($this.data("end-date") == pushAppliedDate) {
                                $this.prop("selected", true);
                        }
                    });
                    if (pushCustId) {
                        $(REPORTS.report_customer + " option[value='"+pushCustId+"']").prop('selected', true);
                    }
                    REPORTS.run_report();
                }
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
			
			var start_moment = moment().startOf("year").subtract(1, "years");
			var now = moment().subtract(1, "months");
			/*
			while(start_moment < moment()) {
				output += "<option value=\"year\" data-start-date=\"" + start_moment.startOf("month").format(REPORTS.dates.format) + "\" data-end-date=\"" + start_moment.endOf("month").format(REPORTS.dates.format) + "\">" + start_moment.format("MM/YYYY"); + "</option>";
				start_moment.add(1, "months");
				//end_moment.add(1, "year");
			}*/
			while(now > start_moment) {
				output += "<option value=\"year\" data-start-date=\"" + now.startOf("month").format(REPORTS.dates.format) + "\" data-end-date=\"" + now.endOf("month").format(REPORTS.dates.format) + "\">" + now.format("MM/YYYY"); + "</option>";
				now.subtract(1, "months");
			}

			//output += "<option value=\"custom\">Custom Dates</option>";
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
		var url = PAGE_CONSTANTS.BASE_URL + "reports/expensecategories." + report_format + "?";
		
		var category = $(REPORTS.expense_category).val();
		var customer = $(REPORTS.report_customer).val();
		var end_date = $(REPORTS.report_date_range + " option:selected").data("end-date");
		var month_display = "";
		var category_display = "";
                var customer_display = "";
		
		if(report_format == PAGE_CONSTANTS.FORMAT.excel) url += "export"
		if(end_date) {
			url += "&ad=" + end_date;
			month_display = end_date;
			$(REPORTS.month_display).html(month_display);
		}
		if(customer) {
			url += "&custid=" + customer;
			customer_display = $(REPORTS.report_customer + " option:selected").html();
			$(REPORTS.customer_display).html(customer_display);
		}
		if(category) {
			url += "&exid=" + category;
			category_display = $(REPORTS.expense_category + " option:selected").html();
			url += "&exn=" + category_display;
			$(REPORTS.expense_category_display).html(category_display);
		}
		
		return url;
	},
	run_report:function() {
		//clear validation message
		UTIL.clear_msg_in_content_box($(REPORTS.filter_criteria));
		
		var url = REPORTS.build_report_url(PAGE_CONSTANTS.FORMAT.json);
		
		//setup loaders
		$(REPORTS.report_section).hide();
		$(REPORTS.loader).show();
		$(REPORTS.data_table + " tfoot").html("");
		
		//ajax call
		$.ajax ({
			url: url,
			type: "GET",
			success:function(data) {
				$(REPORTS.loader).hide();
				var unit_cost_details = data.unitCostDetails;
				var table_data = REPORTS.sort_data_for_table(unit_cost_details.assets, unit_cost_details.costs, unit_cost_details.labor);
				var total_devices = null;
				if(unit_cost_details.unitCost && unit_cost_details.unitCost.serviceTotalUnits) {
					total_devices = unit_cost_details.unitCost.serviceTotalUnits;
				}
				REPORTS.set_summary_info(unit_cost_details.unitCost);
				REPORTS.build_table(table_data);
				REPORTS.build_devices_table(data.associatedDevices);
				var chart_data = REPORTS.sort_data_for_chart(data.previousMonths);
				REPORTS.build_chart("chart-one", "Devices per Month", chart_data.devices);
				REPORTS.build_chart("chart-two", "Device Unit Cost per Month", chart_data.costs);
				$(REPORTS.report_section).show();
				$(window).resize();
				GLOBAL.set_nav_height();
			}
		});
	},
	sort_data_for_table:function(assets, costs, labor) {
		var data = [];
		
		for(var i=0; i < assets.length; i++) {
			var asset = assets[i];
			data.push({ type:"Asset", name:asset.description, amount:asset.depreciation, date:asset.acquiredDateShort });
		}
		
		for(var i=0; i < costs.length; i++) {
			var cost = costs[i];
			data.push({ type:"Expense", name:cost.description, amount:cost.amount, date:cost.appliedDateShort });
		}
		
		for(var i=0; i < labor.length; i++) {
			var labor_record = labor[i];
			data.push({ type:"Labor", name:labor_record.worker, amount:labor_record.laborFormatted, date:labor_record.workDateShort });
		}
		
		data.sort(function(a,b){
			  return new Date(a.date) - new Date(b.date);
		});
		
		return data;
	},
	sort_data_for_chart:function(months) {
		var devices = [];
		var unit_costs = [];
		var dates = [];
		
		for(var key in months) {
			var month = months[key];
			var unit_cost = 0;
			var device_count = 0;
			if(month != null) {
				unit_cost = UTIL.round_with_decimals((month.totalCost + month.totalLabor) / month.deviceTotalUnits, 2);
				device_count = month.deviceTotalUnits;
			}
			devices.push(device_count);
			unit_costs.push(unit_cost);
			dates.push(key);
		}
		
		return { devices: { title:"Units per Month", categories:dates, series: [{ name:"Devices", data:devices }] }, costs: { title:"Unit Cost per Month", categories:dates, series: [{ name:"Unit Cost", data:unit_costs }] } };
	},
	build_chart:function(container_id, name, data) {
	    REPORTS.chart_obj = new Highcharts.Chart({
	        chart: {
	        	renderTo:container_id,
	            type: 'line'
	        },
	        title: {
	            text: name
	        },
	        subtitle: {
	            text: ''
	        },
	        xAxis: {
	            categories: data.categories,
	            crosshair: true
	        },
	        yAxis: {
	            min: 0,
	            title: {
	                text: data.title
	            }
	        },
	        plotOptions: {
	            column: {
	                pointPadding: 0.2,
	                borderWidth: 0,
	                stacking: 'normal'
	            },
	            series: {
	                cursor: 'pointer',
	                point: {
	                    events: {
	                        click: function () {
	                        	var month = this.category;
	                        	$(REPORTS.report_date_range + " option:selected").prop("selected", false);
	                        	$(REPORTS.report_date_range + " option").each(function() {
	                        		var $this = $(this);
	                        		if($this.data("end-date") == month) {
	                        			$this.prop("selected", true);
	                        		}
	                        	});
	                        	REPORTS.run_report();
	                        }
	                    }
	                }
	            }
	        },
	        series: data.series,
	        credits: { enabled:false }
	    });
	},
	set_summary_info:function(unit_cost) {
		var total_device_count = 0;
		var calc_device_unit_cost = 0;
		if(unit_cost != null) {
			total_device_count = unit_cost.deviceTotalUnits;
			calc_device_unit_cost = (unit_cost.totalCost + unit_cost.totalLabor) / total_device_count;
		}
		$(REPORTS.total_devices_display).html(total_device_count);
		$(REPORTS.device_unit_count_display).html(UTIL.convert_currency_for_ui(calc_device_unit_cost));
	},
	build_table:function(data) {
		var output = "";
		var footer_output = "";
		var total = 0;
		
		if(data && data.length > 0) {
			for(var i=0; i < data.length; i++) {
				var record = data[i];
				
				output += "<tr>";
				output += "<td>" + record.type + "</td>";
				output += "<td class=\"center\">" + UTIL.convert_dates_for_ui(record.date) + "</td>";
				output += "<td>" + record.name + "</td>";
				output += "<td class=\"right\">" +  UTIL.convert_currency_for_ui(record.amount) +  "</td>";
				total += record.amount;
				output += "</tr>";
			}
			
			footer_output = "<tr class=\"total-row\"><td></td><td></td><td class=\"right\">TOTAL</td><td class=\"total-column\">" + UTIL.convert_currency_for_ui(total) + "</td></tr>";
		} else {
			output = "<tr><td class=\"no-results\" colspan=\"4\">No results returned.</td></tr>";
		} 
		
		$(REPORTS.data_table + " tbody").html(output);
		$(REPORTS.data_table + " tfoot").html(footer_output);
	},
	build_devices_table:function(devices) {
		var output = "";
		
		if(devices && devices.length > 0) {
			for(var i=0; i < devices.length; i++) {
				var device = devices[i];
				
				output += "<tr>";
				output += "<td>" + device.partNumber + "</td>";
				output += "<td>" + device.description + "</td>";
				output += "</tr>";
			}
		} else {
			output = "<tr><td class=\"no-results\">No Services are Associated with this Cost Category.</td></tr>";
		}
		
		$(REPORTS.devices_table + " tbody").html(output);
	}
};

$(document).ready(function() {
	REPORTS.init();
});
