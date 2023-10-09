var REPORTS = {
	chart:"#chart",
	data_table:"#report-data-table",
    cost_logging_tables:"#cost-logging-tables",
    customer_cost_logging_tables:"#customer-cost-logging-tables",
    forecasted_contracts_tables: "#forecasted-contracts-tables",
	filter_criteria:".filter-criteria",
	report_chart_type:"#report-chart-type",
	report_group:"#report-group",
	report_revenue_status:"#report-revenue-status",
	report_date_range:"#report-date-range",
	report_custom_dates:"#report-custom-dates",
	report_start_date:"#report-start-date",
	report_end_date:"#report-end-date",
	report_service:"#report-service",
	report_customer:"#report-customer",
	run_report_btn:"#run-report-btn",
	report_section:".report-section",
	table_tab:".table-tab",
	report_tab_section:".report-tab-section",
	service_tools_tab:"#service-tools-tab",
	direct_customer_tab:"#direct-customer-tab",
	forecast_tab:"#forecast-tab",
	msg_error_dates_required:"#msg-error-dates-required",
	msg_error_dates_start_before_end:"#msg-error-start-before-end",
    expense_category_link:"#expense-category-link",
	multi_select_hint:"#multi-select-hint",
	export_excel:"#download-excel",
	chart_types:{ column:"column", line:"line" },
	data_types:{ revenue:"Revenue", direct_labor:"Direct Labor Cost", indirect_labor:"Indirect Labor Cost", directc_cost:"Direct Customer Cost", service_cost:"Service Tools Cost", labor_tools_cost:"Labor Tools Cost", project_labor_cost:"Project Labor Cost", forecasted_revenue:"Forecasted Revenue" },
	codes:{ all_services:0, all_services_no_adjustments:99999 },
	chart_obj:null,
	init:function() {
		REPORTS.bind_events();
		REPORTS.dates.init();
		REPORTS.check_auto_load();
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
		
		$(REPORTS.report_group).change(function() {
			var selected = $(this).val();
			var $all = $(REPORTS.report_service + " option[value='0']");
			var $none = $(REPORTS.report_service + " option[value='99999']");
			var value = "0";
			var all_text = "";
			var none_text = "";
			if(selected) {
				$(REPORTS.report_service + " option").hide();
				$(REPORTS.report_service + " ." + selected).show();
				//all_text = "All " + selected + " Services (including Credits/Debits)";
				none_text = "All " + selected + " Services (excluding Credits/Debits)";
				$all.remove();
				value = "99999";
			} else {
				//all_text = "All Services (including Credits/Debits)";
				none_text = "All Services (excluding Credits/Debits)";
				$(REPORTS.report_service).prepend("<option value='0'>All Services (including Credits/Debits)</option>");
				$(REPORTS.report_service + " option").show();
			}
			
			//$all.text(all_text).show();
			$none.text(none_text).show();
			$(REPORTS.report_service).val(value);
		});
		
		$(REPORTS.export_excel).click(function() {
			var url = REPORTS.build_report_url(PAGE_CONSTANTS.FORMAT.excel);
			$(this).prop("href",url);
			return true;
		});
		
		$(REPORTS.report_chart_type).change(function() {
			if($(this).val() == REPORTS.chart_types.line) {
				$(REPORTS.report_service).prop("multiple", true);
				$(REPORTS.multi_select_hint).show();
			} else {
				$(REPORTS.report_service).prop("multiple", false);
				$(REPORTS.multi_select_hint).hide();
			}
		});
		
		$(REPORTS.table_tab).click(function() {
			$(REPORTS.table_tab).removeClass("selected");
			$(this).addClass("selected");
			var view = $(this).data("view");
			$(REPORTS.report_tab_section).hide();
			
			if(view == "service-tools") {
				$(REPORTS.cost_logging_tables).show();
			} else if(view == "direct-customer") {
				$(REPORTS.customer_cost_logging_tables).show();
			} else if(view == "forecast") {
				$(REPORTS.forecasted_contracts_tables).show();
			}
		});
	},
	check_auto_load:function() {
		var auto_load = UTIL.get_param_by_name("auto");
		if(auto_load) {
			var osp_id = UTIL.get_param_by_name("sid");
			if(osp_id) $(REPORTS.report_service).val(osp_id);
			
			var customer_id = UTIL.get_param_by_name("cid");
			if(customer_id) $(REPORTS.report_customer).val(customer_id);
			
			var year = UTIL.get_param_by_name("year");
			if(year) {
				var year_moment = moment(year, "YYYY");
				var start_date = year_moment.startOf("year").format(REPORTS.dates.format);
				var end_date = year_moment.endOf("year").format(REPORTS.dates.format);
				
				//$(REPORTS.report_date_range + " option").prop("selected", "");
				$(REPORTS.report_date_range + " option").each(function() {
					var $this = $(this);
					if($this.data("start-date") == start_date && $this.data("end-date") == end_date) {
						$this.prop("selected", "selected");
					} else {
						$this.prop("selected", "");
					}
				});
				
				$(REPORTS.report_start_date).val(start_date);
				$(REPORTS.report_end_date).val(end_date);
			}
			
			var invoiced_only = UTIL.get_param_by_name("invoiced");
			if(invoiced_only) {
				$(REPORTS.report_revenue_status).val(invoiced_only);
			} else {
				$(REPORTS.report_revenue_status).val("");
			}
			
			REPORTS.run_report();
		}
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
			output += "<option value=\"this-month\" data-start-date=\"" + moment().startOf("month").format(REPORTS.dates.format) + "\" data-end-date=\"" + moment().endOf("month").format(REPORTS.dates.format) + "\">This Month</option>";
			
			var current_month = moment().month();
			var quarter_start = "";
			var quarter_end = "";
			//calculate the current fiscal quarter
			if(current_month >= 0 && current_month <= 2) {
				quarter_start = moment().month(0).format(REPORTS.dates.format);
				quarter_end = moment().month(2).format(REPORTS.dates.format);
			} else if(current_month >= 3 && current_month <= 5) {
				quarter_start = moment().month(3).format(REPORTS.dates.format);
				quarter_end = moment().month(5).format(REPORTS.dates.format);
			} else if(current_month >= 6 && current_month <= 8) {
				quarter_start = moment().month(6).format(REPORTS.dates.format);
				quarter_end = moment().month(8).format(REPORTS.dates.format);
			} else if(current_month >= 9 && current_month <=11) {
				quarter_start = moment().month(9).format(REPORTS.dates.format);
				quarter_end = moment().month(11).format(REPORTS.dates.format);
			}
			
			output += "<option value=\"this-quarter\" data-start-date=\"" + quarter_start + "\" data-end-date=\"" + quarter_end + "\">This Calendar Quarter</option>";			
			output += "<option selected=\"selected\" value=\"year\" data-start-date=\"" + moment().startOf("year").format(REPORTS.dates.format) + "\" data-end-date=\"" + moment().endOf("year").format(REPORTS.dates.format) + "\">This Calendar Year</option>";
			output += "<option value=\"next-year\" data-start-date=\"" + moment().add(1,"year").startOf("year").format(REPORTS.dates.format) + "\" data-end-date=\"" + moment().add(1,"year").endOf("year").format(REPORTS.dates.format) + "\">Next Calendar Year</option>";
			
			var start_moment = moment("01/2012",REPORTS.dates.format);
			var end_moment = moment().subtract(1, "year");
			while(end_moment.year() > start_moment.year()) {
				output += "<option value=\"year\" data-start-date=\"" + end_moment.startOf("year").format(REPORTS.dates.format) + "\" data-end-date=\"" + end_moment.endOf("year").format(REPORTS.dates.format) + "\">" + end_moment.year() + " Calendar Year" + "</option>";
				//start_moment.add(1, "year");
				end_moment.subtract(1, "year");
			}
			
			current_month = moment().month();
			var fiscal_quarter_start = "";
			var fiscal_quarter_end = "";
			//calculate the current fiscal quarter
			if(current_month >= 2 && current_month <= 4) {
				fiscal_quarter_start = moment().month(2).format(REPORTS.dates.format);
				fiscal_quarter_end = moment().month(4).format(REPORTS.dates.format);
			} else if(current_month >= 5 && current_month <= 7) {
				fiscal_quarter_start = moment().month(5).format(REPORTS.dates.format);
				fiscal_quarter_end = moment().month(7).format(REPORTS.dates.format);
			} else if(current_month >= 8 && current_month <= 10) {
				fiscal_quarter_start = moment().month(8).format(REPORTS.dates.format);
				fiscal_quarter_end = moment().month(10).format(REPORTS.dates.format);
			} else if(current_month == 11 || current_month <= 1) {
				fiscal_quarter_start = moment().month(11).subtract(1,'year').format(REPORTS.dates.format);
				fiscal_quarter_end = moment().month(1).format(REPORTS.dates.format);
			}
			
			output += "<option value=\"this-quarter\" data-start-date=\"" + fiscal_quarter_start + "\" data-end-date=\"" + fiscal_quarter_end + "\">This Fiscal Quarter</option>";
			
			//fiscal years
			var fiscal_start_moment = moment();
			var fiscal_end_moment = moment();
			var month = moment().month();
			if(month > 1) {
				var fiscal_start_string = "03/" + moment().year();
				fiscal_start_moment = moment(fiscal_start_string, REPORTS.dates.format);
				fiscal_end_moment = moment(fiscal_start_string, REPORTS.dates.format).add(11,"month");
			} else {
				var fiscal_start_string = "03/" + moment().subtract(1,"year").year();
				fiscal_start_moment = moment(fiscal_start_string, REPORTS.dates.format);
				fiscal_end_moment = moment(fiscal_start_string, REPORTS.dates.format).add(11,"month");
			}
			
			output += "<option value=\"year\" data-start-date=\"" + fiscal_start_moment.format(REPORTS.dates.format) + "\" data-end-date=\"" + fiscal_end_moment.format(REPORTS.dates.format) + "\">This Fiscal Year</option>";
			output += "<option value=\"next-year\" data-start-date=\"" + fiscal_start_moment.add(1,"year").format(REPORTS.dates.format) + "\" data-end-date=\"" + fiscal_end_moment.add(1,"year").format(REPORTS.dates.format) + "\">Next Fiscal Year</option>";

			fiscal_start_moment.subtract(2, "year");
			fiscal_end_moment.subtract(2, "year");
			
			var fiscal_start_end_moment = moment("01/2012",REPORTS.dates.format);
			
			while(fiscal_end_moment.year() > fiscal_start_end_moment.year()) {
				output += "<option value=\"year\" data-start-date=\"" + fiscal_start_moment.format(REPORTS.dates.format) + "\" data-end-date=\"" + fiscal_end_moment.format(REPORTS.dates.format) + "\">" + fiscal_end_moment.year() + " Fiscal Year" + "</option>";
				fiscal_start_moment.subtract(1, "year");
				fiscal_end_moment.subtract(1, "year");
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
		var url = PAGE_CONSTANTS.BASE_URL + "reports/revenue." + report_format + "?";
		
		var group = $(REPORTS.report_group).val();
		var start_date = $(REPORTS.report_start_date).val();
		var end_date = $(REPORTS.report_end_date).val();
		var service = $(REPORTS.report_service).val();
		var customer = $(REPORTS.report_customer).val();
		var include_children = $(REPORTS.report_customer + " option:selected").data("parent");
		var chart_type = $(REPORTS.report_chart_type).val();
		var revenue_status = $(REPORTS.report_revenue_status).val();
		
		if(report_format == PAGE_CONSTANTS.FORMAT.excel) url += "export"
		if(start_date && end_date) {
			url += "&sd=" + start_date + "&ed=" + end_date;
		}
		if(group) url += "&bm=" + group;
		if(service != "" && service != null) {
			url += "&ospId=" + service;
		}
		if(customer) url += "&cid=" + customer;
		if(include_children) url += "&chldrn=true";
		if(chart_type == REPORTS.chart_types.line) url += "&profit=true";
		if(revenue_status == "invoiced") {
			url += "&invoiced=true";
		} else if(revenue_status == "forecast") {
			url += "&fc=true";
		}
		
		return url;
	},
	run_report:function() {
		//clear validation message
		UTIL.clear_msg_in_content_box($(REPORTS.filter_criteria));
		
		//get variables for validation
		var start_date = $(REPORTS.report_start_date).val();
		var end_date = $(REPORTS.report_end_date).val();
		var chart_type = $(REPORTS.report_chart_type).val();
		
		if(start_date && end_date) {
			if(moment(start_date, REPORTS.dates.format).isAfter(moment(end_date, REPORTS.dates.format))) {
				UTIL.add_error_msg_to_content_box($(REPORTS.filter_criteria), $(REPORTS.msg_error_dates_start_before_end).val());
				return false;
			}
		} else {
			UTIL.add_error_msg_to_content_box($(REPORTS.filter_criteria), $(REPORTS.msg_error_dates_required).val());
			return false;
		}
		
		var url = REPORTS.build_report_url(PAGE_CONSTANTS.FORMAT.json);
		
		//setup loaders
		var loader = "<div class=\"loader\"><img src=\"" + PAGE_CONSTANTS.LOADER_URL + "\" /><br/>Retreiving Chart Data...</div>";
		$(REPORTS.chart).html(loader);
		UTIL.add_table_loader(REPORTS.data_table, "Retrieving Chart Data...");
		$(REPORTS.report_section).show();
		GLOBAL.set_nav_height();
		
		//display correct info
		if(chart_type == REPORTS.chart_types.column) {
			$("." + REPORTS.chart_types.column).show();
			$("." + REPORTS.chart_types.line).hide();
		} else {
			$("." + REPORTS.chart_types.column).hide();
			$("." + REPORTS.chart_types.line).show();
		}
		$(REPORTS.data_table + " tfoot").html("");
		$(REPORTS.customer_cost_logging_tables).html("");
		$(REPORTS.cost_logging_tables).html("");
		$(REPORTS.forecasted_contracts_tables).html("");
		$(REPORTS.table_tab).hide();
		
		//ajax call
		$.ajax ({
			url: url,
			type: "GET",
			success:function(data) {
				var report_data = REPORTS.sort_data_for_chart(data);
				REPORTS.build_chart(report_data);
				REPORTS.build_table(report_data);
                REPORTS.build_cost_logging_tables(data);
                REPORTS.build_forecasted_contracts_tables(data);
				GLOBAL.set_nav_height();
			}
		});
	},
	sort_data_for_chart:function(data) {
		var series = [];
		var categories = [];
		var total_devices = [];
		var first_loop = true;
		var includes_all_services = false;
		
		for(var k in data) {
			var records = data[k];
			var revenue = [];
			var forecasted_revenue = [];
			var direct_labor = [];
			var directc_cost = [];
			var service_cost = [];
			var indirect_labor = [];
			var onboarding_labor = [];
			var labor_tools_cost = [];
			var profitability = [];
			var devices = [];
			var avg_prices = [];
			var margins = [];
			categories = [];
			devices = [];
			for(var i=0; i < records.length; i ++) {
				var record = records[i];
				categories.push(record.displayDate);
				revenue.push(record.revenue);
				forecasted_revenue.push(record.forecastedRevenue);
				direct_labor.push(record.laborCost);
				directc_cost.push(record.directCost);
				service_cost.push(record.serviceCost);
				labor_tools_cost.push(record.laborToolsCost);
				onboarding_labor.push(record.onboardingLaborCost);
				indirect_labor.push(record.indirectLaborProportionCost);
				profitability.push(record.profitability);
				devices.push(record.serviceCount);
				avg_prices.push(record.pricePerService);
				margins.push(record.margin);
				
				if(!includes_all_services) {
					if(first_loop) {
						total_devices.push(record.serviceCount);
					} else {
						if(k == REPORTS.codes.all_services || k == REPORTS.codes.all_services_no_adjustments) {
							total_devices[i] = record.serviceCount;
						} else {
							total_devices[i] = total_devices[i] + record.serviceCount;
						}
					}
				}
			}
			first_loop = false;
			if((k == REPORTS.codes.all_services || k == REPORTS.codes.all_services_no_adjustments) && !includes_all_services) includes_all_services = true;
			
			if($(REPORTS.report_chart_type).val() == REPORTS.chart_types.column) {
				series = [
					{
			            name:REPORTS.data_types.forecasted_revenue,
			            data:forecasted_revenue,
			            color:"#F9CBA7",
			            devices:devices,
			            margins:margins,
			            stack:"revenue"
		        	},
					{
			            name:REPORTS.data_types.revenue,
			            data:revenue,
			            color:"#f7a35c",
			            devices:devices,
			            margins:margins,
			            stack:"revenue"
		        	},
		        	{
			            name:REPORTS.data_types.directc_cost,
			            data:directc_cost,
			            color:"#d7bde2",
			            devices:devices,
			            margins:margins,
			            stack:"cost"
		        	},
		        	{
			            name:REPORTS.data_types.service_cost,
			            data:service_cost,
			            color:"#1d9d73",
			            devices:devices,
			            margins:margins,
			            stack:"cost"
		        	},
		        	{
			            name:REPORTS.data_types.labor_tools_cost,
			            data:labor_tools_cost,
			            color:"#f66",
			            devices:devices,
			            margins:margins,
			            stack:"cost"
		        	},
		        	{
			            name:REPORTS.data_types.project_labor_cost,
			            data:onboarding_labor,
			            color:"#FFE163",
			            devices:devices,
			            margins:margins,
			            stack:"cost"
		        	},
		        	{
			            name:REPORTS.data_types.indirect_labor,
			            data:indirect_labor,
			            color:"#8439dd",
			            devices:devices,
			            margins:margins,
			            stack:"cost"
		        	},
		        	{
			            name:REPORTS.data_types.direct_labor,
			            data:direct_labor,
			            color:"#69c",
			            devices:devices,
			            margins:margins,
			            stack:"cost"
		        	}
				];
			} else {
				//series = [ { name:$(REPORTS.report_service + " option:selected").text(), data:profitability, devices:devices } ];
				var name = $(REPORTS.report_service + " option[value='" + k + "']").html();
				series.push({ name:name, data:profitability, devices:devices, avg_prices:avg_prices, margins:margins });
			}
		}
		   
		return { categories:categories, series:series, devices:total_devices, avg_prices:avg_prices, margins:margins };
	},
	build_chart:function(data) {
		var chart_type = $(REPORTS.report_chart_type).val();
	    REPORTS.chart_obj = new Highcharts.Chart({
	        chart: {
	        	renderTo:'chart',
	            type: chart_type
	        },
	        title: {
	            text: 'Monthly Profitability Report'
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
	                text: 'Revenue vs Cost (in Dollars)'
	            	//text: 'Revenue (in Dollars)'
	            }
	        },
	        tooltip: {
	        	formatter: function() {
	                var points='<table class="tip"><caption style="font-size:10px; font-weight:bold; border-bottom:1px solid #ccc; margin-bottom:5px; padding-bottom:3px;"> '+this.x+'</caption><tbody>';
	                //loop each point in this.points
	                var index;
	                var cost_total = 0;
	                $.each(this.points,function(i,point){
	                	index = this.series.xAxis.categories.indexOf(point.x); // get index
	                	points+='<tr><td><span class="popup-legend-block" style="background-color: '+point.series.color+';"></span>'+point.series.name+':</td>'
	                          + '<td style="text-align: right"><b>'+UTIL.convert_currency_for_ui(point.y)+'</b></td></tr>';
	                    if(point.series.name == REPORTS.data_types.direct_labor ||
                                    point.series.name == REPORTS.data_types.indirect_labor ||
                                    point.series.name == REPORTS.data_types.directc_cost ||
                                    point.series.name == REPORTS.data_types.service_cost ||
                                    point.series.name == REPORTS.data_types.labor_tools_cost ||
                                    point.series.name == REPORTS.data_types.project_labor_cost) {
                                cost_total += point.y;
                            }
	                });
	                if(chart_type != REPORTS.chart_types.line) {
	                	points += '<tr><td style="color:#666;padding:0;">Total Cost:</td><td style="text-align:right;padding:0;"><b>'+UTIL.convert_currency_for_ui(cost_total)+'</b></td></tr>';
	                }
	                points += '<tr><td style="color:#666;padding:0;">Margin:</td><td style="text-align:right;padding:0;"><b>'+UTIL.convert_decimal_to_percent(data.margins[index], 2)+'%</b></td></tr>';
	                points += '<tr><td style="color:#666;padding:0;">Devices:</td><td style="text-align:right;padding:0;"><b>'+UTIL.convert_number_for_ui(data.devices[index])+'</b></td></tr>';
	                points += '<tr><td style="color:#666;padding:0;">Average Price:</td><td style="text-align:right;padding:0;"><b>'+UTIL.convert_currency_for_ui(data.avg_prices[index])+'</b></td></tr>';
	                points += '</tbody></table>';
	                return points;
	            },
	            shared: true,
	            useHTML: true
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
	                        	//var osp_id = data.service_ids[this.index];
	                        	//var date = $(REPORTS.report_date_range + " option:selected").data("month");
	                        	//var year = moment(date, REPORTS.dates.format).format("YYYY");
	                        	//window.location = "/si/reports/revenue?auto=true&sid=" + osp_id + "&year=" + year;
	                        	var url = "/si/reports/topservices?auto=true&month=" + this.category;
	                        	var invoiced_only = $(REPORTS.report_revenue_status).val();
	                        	if(invoiced_only) url += "&invoiced=true";
	                        	window.location = url;
	                        }
	                    }
	                }
	            }
	        },
	        series: data.series,
	        credits: { enabled:false }
	    });
	},
	build_table:function(data) {
		var output = "";
		var total = 0;
		var total_forecasted_revenue = 0;
		var total_directc_cost = 0;
		var total_service_cost = 0;
		var total_labor_tools_cost = 0;
		var total_onboarding_tools_cost = 0;
		var total_indirect_labor_cost = 0;
		var total_direct_labor_cost = 0;
		var total_cost = 0;
		var total_margin = 0;
		
		var profit_index = 0;
		if($(REPORTS.report_chart_type).val() == REPORTS.chart_types.column) {
			for(var i=0; i < data.categories.length; i++) {
				var date = data.categories[i];
				var revenue = 0;
				var cost = 0;
				var profit = 0;
				if($(REPORTS.report_chart_type).val() == REPORTS.chart_types.column) {
					var forecasted_revenue = data.series[0].data[i];
					revenue = data.series[1].data[i];
					var directc_cost = data.series[2].data[i];
					var service_cost = data.series[3].data[i];
					var labor_tools_cost = data.series[4].data[i];
					var onboarding_tools_cost = data.series[5].data[i];
					var indirect_labor_cost = data.series[6].data[i];
					var direct_labor_cost = data.series[7].data[i];
					var margin = data.series[1].margins[i];
					
					total_directc_cost += directc_cost;
					total_service_cost += service_cost;
					total_labor_tools_cost += labor_tools_cost;
					total_onboarding_tools_cost += onboarding_tools_cost;
					total_indirect_labor_cost += indirect_labor_cost;
					total_direct_labor_cost += direct_labor_cost;
					total_forecasted_revenue += forecasted_revenue;
					
					cost = direct_labor_cost + directc_cost + service_cost + indirect_labor_cost + labor_tools_cost + onboarding_tools_cost;
				} else {
					profit = data.series[profit_index].data[i];
				}
				var device_count = data.devices[i];
				output += "<tr>";
				output += "<td>" + date + "</td>";
				output += "<td class=\"right\">" + UTIL.convert_number_for_ui(device_count) + "</td>";
				output += "<td class=\"right\">" +  UTIL.convert_currency_for_ui(revenue) +  "</td>";
				output += "<td class=\"right\">" +  UTIL.convert_currency_for_ui(forecasted_revenue) +  "</td>";
				output += "<td class=\"right\">" +  UTIL.convert_currency_for_ui(directc_cost) +  "</td>";
				output += "<td class=\"right\">" +  UTIL.convert_currency_for_ui(service_cost) +  "</td>";
				output += "<td class=\"right\">" +  UTIL.convert_currency_for_ui(labor_tools_cost) +  "</td>";
				output += "<td class=\"right\">" +  UTIL.convert_currency_for_ui(onboarding_tools_cost) +  "</td>";
				output += "<td class=\"right\">" +  UTIL.convert_currency_for_ui(indirect_labor_cost) +  "</td>";
				output += "<td class=\"right\">" +  UTIL.convert_currency_for_ui(direct_labor_cost) +  "</td>";
				output += "<td class=\"right\">" +  UTIL.convert_currency_for_ui(cost) +  "</td>";
				output += "<td class=\"right\">" +  UTIL.convert_decimal_to_percent(margin, 2) +  "%</td>";
				total += revenue;
				output += "</tr>";
				
				total_cost += cost;
			}
			
			total_margin = 1 - (total_cost / total);
		} else {
			for(var k=0; k < data.series.length; k++) {
				var section_total = 0;
				output += "<tr><td colspan=\"4\" style=\"background-color:#666; color:#fff; font-weight:600;\">" + data.series[k].name + "</td></tr>";
				for(var i=0; i < data.categories.length; i++) {
					var date = data.categories[i];
					var profit = data.series[k].data[i];
					var device_count = data.series[k].devices[i];
					output += "<tr>";
					output += "<td>" + date + "</td>";
					output += "<td class=\"right\">" + UTIL.convert_number_for_ui(device_count) + "</td>";
					output += "<td class=\"right\">" +  UTIL.convert_currency_for_ui(profit) +  "</td>";
					total += profit;
					section_total += profit;
					output += "</tr>";
				}
				output += "<tr class=\"total-row\"><td></td><td class=\"right\">Service Total</td><td class=\"total-column\">" + accounting.formatMoney(section_total) + "</td></tr>";
			}
		}
		$(REPORTS.data_table + " tbody").html(output);
		
		var footer_output = "";
		if($(REPORTS.report_chart_type).val() == REPORTS.chart_types.column) {
			footer_output = "<tr class=\"total-row\"><td></td>";
			footer_output += "<td class=\"right\">TOTAL</td>";
			footer_output += "<td class=\"total-column\">" + accounting.formatMoney(total) + "</td>";
			footer_output += "<td class=\"total-column\">" + accounting.formatMoney(total_forecasted_revenue) + "</td>";
			footer_output += "<td class=\"total-column\">" + accounting.formatMoney(total_directc_cost) + "</td>";
			footer_output += "<td class=\"total-column\">" + accounting.formatMoney(total_service_cost) + "</td>";
			footer_output += "<td class=\"total-column\">" + accounting.formatMoney(total_labor_tools_cost) + "</td>";
			footer_output += "<td class=\"total-column\">" + accounting.formatMoney(total_onboarding_tools_cost) + "</td>";
			footer_output += "<td class=\"total-column\">" + accounting.formatMoney(total_indirect_labor_cost) + "</td>";
			footer_output += "<td class=\"total-column\">" + accounting.formatMoney(total_direct_labor_cost) + "</td>";
			footer_output += "<td class=\"total-column\">" + accounting.formatMoney(total_cost) + "</td>";
			footer_output += "<td class=\"total-column\">" + UTIL.convert_decimal_to_percent(total_margin, 2) + "%</td>";
			footer_output += "</tr>";
		} else {
			footer_output = "<tr class=\"total-row\"><td></td><td class=\"right\">TOTAL</td><td class=\"total-column\">" + accounting.formatMoney(total) + "</td></tr>";
		}
		//var footer_output = "<tr class=\"total-row\"><td></td><td></td><td class=\"right\">TOTAL</td><td class=\"total-column\">" + accounting.formatMoney(total) + "</td></tr>";
		$(REPORTS.data_table + " tfoot").html(footer_output);
	},
	build_cost_logging_tables:function(data) {
            var output = "";
            var chart_type = $(REPORTS.report_chart_type).val();
            if(chart_type == REPORTS.chart_types.line) {
                console.log("NOT processing cost logging for \"lines\" type");
                return;
            }
            /**
             * the data wrapper is an Object with Service IDs for properties. Because we are
             * ignoring the "lines" data (multiple Services, the data wrapper has one property
             * and that is either a single Service ID or it is "0", representing all Services
             */
            var data_key = Object.keys(data)[0]; // grab the only data Object key/property, whatever it is
            var records = data[data_key]; // grab the data Objetc value for the key
            if (records) {
                // find out if any months even have service tools costs
                var show_details = false;
                for(var i=0; i < records.length; i ++) {
                    var record = records[i];
                    if (record.serviceCost > 0.0) {
                        show_details = true;
                        break;
                    }
                }
                if (show_details) {
                    console.log("processing month(s) of Service Tools cost logging data");
                    output += "<div class=\"table-header\">";
                    //output += "<div class=\"table-title\"><span style=\"background-color:#1d9d73;\">Service Tools Cost Details</span></div>";
                    output += "</div>";
                    for(var i=0; i < records.length; i ++) {
                        var record = records[i];
                        output += "<div class=\"table-title\" style=\"padding-top:10px;\"><i class=\"fa fa-calendar\"></i>" + record.displayDate + "</div>";
                        output += "<table><thead><tr>";
                        output += "<th>Cost Category</th>";
                        output += "<th class=\"right\">Device Count</th>";
                        output += "<th class=\"right\">Cost Amount</th>";
                        output += "</tr></thead><tbody>";
                        var total_cost = 0.0;
                        for (var j=0; j < record.serviceCostDetails.length; j++) {
                            var costDetail = record.serviceCostDetails[j];
                            if (costDetail.formattedContributingCost > 0.0) {
                                total_cost += costDetail.formattedContributingCost;
                                output += "<tr>";
                                output += "<td><a target=\"_blank\" href=\"" + $(REPORTS.expense_category_link).val()+"?ad=" + record.displayDate + "&exid=" + costDetail.expenseCategoryId + "\">" + costDetail.expenseCategoryName + "</a></td>";
                                output += "<td class=\"right\">" + costDetail.contributingDeviceCount + "</td>";
                                output += "<td class=\"right\">" + accounting.formatMoney(costDetail.formattedContributingCost) + "</td></tr>";
                            }
                        }
                        output += "</tbody><tfoot>";
                        output += "<tr class=\"total-row\"><td></td>";
                        output += "<td class=\"right\">TOTAL</td>";
                        output += "<td class=\"total-column\">" + accounting.formatMoney(total_cost) + "</td></tr></tfoot></table>";
                    }
                    
                    $(REPORTS.table_tab).removeClass("selected");
        			$(REPORTS.service_tools_tab).addClass("selected");
        			$(REPORTS.service_tools_tab).show();
        			
        			$(REPORTS.report_tab_section).hide();
        			$(REPORTS.cost_logging_tables).show();
                }
                
                $(REPORTS.cost_logging_tables).html(output);
                // find out if any months even have direct costs
                show_details = false;
                for(var i=0; i < records.length; i ++) {
                    var record = records[i];
                    if (record.directCost > 0.0) {
                        show_details = true;
                        break;
                    }
                }
                //reset output
                output = ""; 
                if (show_details) {
                    console.log("processing month(s) of Customer Direct cost logging data");
                    output += "<div class=\"table-header\" style=\"margin-top:20px;\">";
                    //output += "<div class=\"table-title\"><span style=\"background-color:#d7bde2;\">Customer Direct Cost Details</span></div>";
                    output += "</div>";
                    for(var i=0; i < records.length; i ++) {
                        var record = records[i];
                        output += "<div class=\"table-title\" style=\"padding-top:10px;\"><i class=\"fa fa-calendar\"></i>" + record.displayDate + "</div>";
                        output += "<table><thead><tr>";
                        output += "<th>Customer</th>";
                        output += "<th>Cost Category</th>";
                        output += "<th class=\"right\">Device Count</th>";
                        output += "<th class=\"right\">Cost Amount</th>";
                        output += "</tr></thead><tbody>";
                        var total_cost = 0.0;
                        for (var j=0; j < record.directCostDetails.length; j++) {
                            var costDetail = record.directCostDetails[j];
                            if (costDetail.formattedContributingCost > 0.0) {
                                total_cost += costDetail.formattedContributingCost;
                                output += "<tr>";
                                output += "<td>" + costDetail.customerName + "</td>";
                                output += "<td><a target=\"_blank\" href=\"" + $(REPORTS.expense_category_link).val()+"?ad=" + record.displayDate + "&exid=" + costDetail.expenseCategoryId + "&custid=" + costDetail.customerId + "\">" + costDetail.expenseCategoryName + "</a></td>";
                                output += "<td class=\"right\">" + costDetail.contributingDeviceCount + "</td>";
                                output += "<td class=\"right\">" + accounting.formatMoney(costDetail.formattedContributingCost) + "</td></tr>";
                            }
                        }
                        output += "</tbody><tfoot>";
                        output += "<tr class=\"total-row\"><td></td><td></td>";
                        output += "<td class=\"right\">TOTAL</td>";
                        output += "<td class=\"total-column\">" + accounting.formatMoney(total_cost) + "</td></tr></tfoot></table>";
                    }
                    $(REPORTS.direct_customer_tab).show();
                }
                
                $(REPORTS.customer_cost_logging_tables).html(output);
            }
	},
	build_forecasted_contracts_tables:function(data) {
		var output = "";
		var chart_type = $(REPORTS.report_chart_type).val();
        if(chart_type == REPORTS.chart_types.line) {
            console.log("NOT processing cost logging for \"lines\" type");
            return;
        }
        
        var data_key = Object.keys(data)[0]; // grab the only data Object key/property, whatever it is
        var records = data[data_key]; // grab the data Objetc value for the key
        if (records) {
        	var show_details = false;
            for(var i=0; i < records.length; i ++) {
                var record = records[i];
                if (record.forecastedContracts.length > 0) {
                    show_details = true;
                    break;
                }
            }
            
            if(show_details) {
                output += "<div class=\"table-header\">";
                //output += "<div class=\"table-title\"><span style=\"background-color:#1d9d73;\">Forecasted Contracts</span></div>";
                output += "</div>";
                for(var i=0; i < records.length; i ++) {
                    var record = records[i];
                    output += "<div class=\"table-title\" style=\"padding-top:10px;\"><i class=\"fa fa-binoculars\"></i>" + record.displayDate + "</div>";
                    output += "<table><thead><tr>";
                    output += "<th>Customer</th>";
                    output += "<th>SOW ID</th>";
                    output += "<th>Job Number</th>";
                    output += "<th>Name</th>";
                    output += "<th>End Date</th>";
                    output += "<th>Renewal Status</th>";
                    output += "<th>Renewal Change</th>";
                    output += "<th class=\"right\">Forecasted MRC</th>";
                    output += "</tr></thead><tbody>";
                    var total_mrc = 0.0;
                    for (var j=0; j < record.forecastedContracts.length; j++) {
                        var contract = record.forecastedContracts[j];
                        var change = contract.renewalChange;
        				var change_display = "";
        				if(change < 0) {
        					change_display = Math.abs(change) + "% MRC Decrease";
        				} else if(change > 0) {
        					change_display = change + "% MRC Increase";
        				} else {
        					change_display = "No change";
        				}
                        
        				var contract_url = PAGE_CONSTANTS.BASE_URL + "contracts/" + contract.id;
                        output += "<tr>";
                        output += "<td>" + contract.customerName + "</td>";
                        output += "<td>" + contract.altId + "</td>";
                        output += "<td><a href=\"" + contract_url + "\" target=\"_blank\">" + contract.jobNumber + "</a></td>";
                        output += "<td>" + contract.name + "</td>";
                        output += "<td>" + UTIL.convert_dates_for_ui(contract.endDate) + "</td>";
                        output += "<td>" + contract.renewalStatusDisplay + "</td>";
                        output += "<td>" + change_display + "</td>";
                        output += "<td class=\"right\">" + accounting.formatMoney(contract.monthTotalRecurringRevenue) + "</td>";
                        output += "</tr>";
                        total_mrc += contract.monthTotalRecurringRevenue;
                    }
                    output += "</tbody><tfoot>";
                    output += "<tr class=\"total-row\"><td colspan=\"6\"></td>";
                    output += "<td class=\"right\">TOTAL</td>";
                    output += "<td class=\"total-column\">" + accounting.formatMoney(total_mrc) + "</td></tr></tfoot></table>";
                }
                
                $(REPORTS.table_tab).removeClass("selected");
    			$(REPORTS.forecast_tab).addClass("selected");
    			$(REPORTS.forecast_tab).show();
    			
    			$(REPORTS.report_tab_section).hide();
    			$(REPORTS.forecasted_contracts_tables).show();
            }
        	
        	$(REPORTS.forecasted_contracts_tables).html(output);
        }
	}
};

$(document).ready(function() {
	REPORTS.init();
});