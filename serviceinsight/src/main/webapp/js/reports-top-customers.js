var REPORTS = {
	chart:"#chart",
	data_table:"#report-data-table",
	report_group:"#report-group",
	report_date_range:"#report-date-range",
	report_customer_status:"#report-customer-status",
	report_revenue_status:"#report-revenue-status",
	run_report_btn:"#run-report-btn",
	report_section:".report-section",
	export_excel:"#download-excel",
	data_types:{ revenue:"Revenue", direct_labor:"Direct Labor Cost", indirect_labor:"Indirect Labor Cost", directc_cost:"Direct Customer Cost", service_cost:"Service Tools Cost", labor_tools_cost:"Labor Tools Cost", project_labor_cost:"Project Labor Cost" },
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
			
			var start_month_moment = moment("12/2014",REPORTS.dates.format);
			var end_month_moment = moment();
			while(end_month_moment > start_month_moment) {
				output += "<option value=\"year\" data-month=\"" + end_month_moment.startOf("month").format(REPORTS.dates.format) + "\">" + end_month_moment.format(REPORTS.dates.display_format); + "</option>";
				end_month_moment.subtract(1, "month");
			}

			$(REPORTS.report_date_range).html(output);
		}
	},
	build_report_url:function(report_format) {
		var url = PAGE_CONSTANTS.BASE_URL + "reports/topcustomers." + report_format + "?";
		
		var month = $(REPORTS.report_date_range + " option:selected").data("month");
		var status = $(REPORTS.report_customer_status).val();
		var revenue_status = $(REPORTS.report_revenue_status).val();
                var include_children = $("input[name='chldrn']:checked").val();
		url += "month=" + month;
		if(report_format == PAGE_CONSTANTS.FORMAT.excel) url += "&export";
		if(status != "") url += "&archived=" + status;
		if(revenue_status) url += "&invoiced=true";
		url += "&chldrn=" + include_children;
		
		return url;
	},
	run_report:function() {
		//clear validation message
		//UTIL.clear_msg_in_content_box($(REPORTS.filter_criteria));
		
		var url = REPORTS.build_report_url(PAGE_CONSTANTS.FORMAT.json);
		
		//setup loaders
		var loader = "<div class=\"loader\"><img src=\"" + PAGE_CONSTANTS.LOADER_URL + "\" /><br/>Retrieving Data...</div>";
		$(REPORTS.chart).html(loader);
		UTIL.add_table_loader(REPORTS.data_table, "Retrieving Data...");
		$(REPORTS.report_section).show();
		GLOBAL.set_nav_height();
		
		//ajax call
		$.ajax ({
			url: url,
			type: "GET",
			success:function(data) {
				var report_data = REPORTS.sort_data_for_chart(data);
				REPORTS.build_chart(report_data);
				REPORTS.build_table(report_data);
				GLOBAL.set_nav_height();
			}
		});
	},
	sort_data_for_chart:function(data) {
		var customers = [];
		var sdms = [];
		var revenue = [];
		var direct_labor_cost = [];
		var indirect_labor_cost = [];
		var onboarding_labor_cost = [];
		var labor_tools_cost = [];
		var directc_cost = [];
		var service_cost = [];
		var devices = [];
		var avg_prices = [];
		var margins = [];
		
		for(var i=0; i < data.length; i ++) {
			var record = data[i];
			customers.push(record.customerName);
			sdms.push(record.serviceDeliveryManager);
			revenue.push(record.data[0].revenue);
			direct_labor_cost.push(record.data[0].laborCost);
			indirect_labor_cost.push(record.data[0].indirectLaborProportionCost);
			onboarding_labor_cost.push(record.data[0].onboardingLaborCost);
			labor_tools_cost.push(record.data[0].laborToolsCost);
			directc_cost.push(record.data[0].directCost);
			service_cost.push(record.data[0].serviceCost);
			devices.push(record.data[0].serviceCount);
			avg_prices.push(record.data[0].pricePerService);
			margins.push(record.data[0].margin);
		}
		var series = [
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
	            data:onboarding_labor_cost,
	            color:"#FFE163",
	            devices:devices,
	            margins:margins,
	            stack:"cost"
        	},
        	{
	            name:REPORTS.data_types.indirect_labor,
	            data:indirect_labor_cost,
	            color:"#8439dd",
	            devices:devices,
	            margins:margins,
	            stack:"cost"
        	},
        	{
	            name:REPORTS.data_types.direct_labor,
	            data:direct_labor_cost,
	            color:"#69c",
	            devices:devices,
	            margins:margins,
	            stack:"cost"
        	}
		];
		return { categories:customers, series:series, devices:devices, avg_prices:avg_prices, margins:margins, sdms:sdms };
	},
	set_chart_height:function(records) {
		var height = 500;
		if(records) height = 25 * records;
		if(height < 500) height = 500;
		$(REPORTS.chart).height(height);
		
		GLOBAL.set_nav_height();
	},
	build_chart:function(data) {
		REPORTS.set_chart_height(data.categories.length);
		
		$(REPORTS.chart).highcharts({
	        chart: {
	            type: 'bar'
	        },
	        title: {
	            text: 'Top Customers'
	        },
	        subtitle: {
	            text: 'For ' + $(REPORTS.report_date_range + ' option:selected').text()
	        },
	        xAxis: {
	            categories:data.categories,
	            title: {
	                text: null
	            }
	        },
	        yAxis: {
	            min: 0,
	            title: {
	                text: 'Revenue',
	                align: 'high'
	            },
	            labels: {
	                overflow: 'justify',
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
	                    if(point.series.name != REPORTS.data_types.revenue) cost_total += point.y;
	                });
	                points += '<tr><td style="color:#666;padding:0;">Total Cost:</td><td style="text-align:right;padding:0;"><b>'+UTIL.convert_currency_for_ui(cost_total)+'</b></td></tr>';
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
	            bar: {
	                stacking: 'normal'
	            }
	        },
	        series:data.series,
	        credits: {
	            enabled: false
	        }
	    });
	},
	build_table:function(data) {
		var output = "";
		var total = 0;
		var total_directc_cost = 0;
		var total_service_cost = 0;
		var total_labor_tools_cost = 0;
		var total_onboarding_tools_cost = 0;
		var total_indirect_labor_cost = 0;
		var total_direct_labor_cost = 0;
		var total_cost = 0;
		
		for(var i=0; i < data.categories.length; i++) {
			var date = data.categories[i];
			var sdm = data.sdms[i];
			var value = data.series[0].data[i];
			var directc_cost = data.series[1].data[i];
			var service_cost = data.series[2].data[i];
			var labor_tools_cost = data.series[3].data[i];
			var onboarding_labor_cost = data.series[4].data[i];
			var indirect_labor_cost = data.series[5].data[i];
			var direct_labor_cost = data.series[6].data[i];
			var margin = data.series[0].margins[i];
			
			var cost = direct_labor_cost + directc_cost + service_cost + indirect_labor_cost + labor_tools_cost + onboarding_labor_cost;
			//var cost = direct_labor_cost + indirect_labor_cost + labor_tools_cost;
			var device_count = data.devices[i];
			output += "<tr>";
			output += "<td>" + date + "</td>";
			output += "<td>" + sdm + "</td>";
			output += "<td class=\"right\">" + UTIL.convert_number_for_ui(device_count) + "</td>";
			output += "<td class=\"right\">" + UTIL.convert_currency_for_ui(directc_cost) + "</td>";
			output += "<td class=\"right\">" + UTIL.convert_currency_for_ui(service_cost) + "</td>";
			output += "<td class=\"right\">" + UTIL.convert_currency_for_ui(labor_tools_cost) + "</td>";
			output += "<td class=\"right\">" + UTIL.convert_currency_for_ui(onboarding_labor_cost) + "</td>";
			output += "<td class=\"right\">" + UTIL.convert_currency_for_ui(indirect_labor_cost) + "</td>";
			output += "<td class=\"right\">" + UTIL.convert_currency_for_ui(direct_labor_cost) + "</td>";
			output += "<td class=\"right\">" + UTIL.convert_currency_for_ui(value) + "</td>";
			output += "<td class=\"right\">" + UTIL.convert_currency_for_ui(cost) + "</td>";
			output += "<td class=\"right\">" + UTIL.convert_decimal_to_percent(margin, 2) + "%</td>";
			output += "</tr>";
			
			total += value;
			total_directc_cost += directc_cost;
			total_service_cost += service_cost;
			total_labor_tools_cost += labor_tools_cost;
			total_onboarding_tools_cost += onboarding_labor_cost;
			total_indirect_labor_cost += indirect_labor_cost;
			total_direct_labor_cost += direct_labor_cost;
			total_cost += cost;
		}
		$(REPORTS.data_table + " tbody").html(output);
		
		//var footer_output = "<tr class=\"total-row\"><td></td><td></td><td class=\"right\">TOTAL</td><td class=\"total-column\">" + accounting.formatMoney(total) + "</td><td class=\"total-column\">" + accounting.formatMoney(total_cost) + "</td></tr>";
		var footer_output = "<tr class=\"total-row\"><td></td><td></td>";
		footer_output += "<td class=\"right\">TOTAL</td>";
		footer_output += "<td class=\"total-column\">" + accounting.formatMoney(total) + "</td>";
		footer_output += "<td class=\"total-column\">" + accounting.formatMoney(total_directc_cost) + "</td>";
		footer_output += "<td class=\"total-column\">" + accounting.formatMoney(total_service_cost) + "</td>";
		footer_output += "<td class=\"total-column\">" + accounting.formatMoney(total_labor_tools_cost) + "</td>";
		footer_output += "<td class=\"total-column\">" + accounting.formatMoney(total_onboarding_tools_cost) + "</td>";
		footer_output += "<td class=\"total-column\">" + accounting.formatMoney(total_indirect_labor_cost) + "</td>";
		footer_output += "<td class=\"total-column\">" + accounting.formatMoney(total_direct_labor_cost) + "</td>";
		footer_output += "<td class=\"total-column\">" + accounting.formatMoney(total_cost) + "</td>";
		footer_output += "<td class=\"total-column\">&nbsp;</td></tr>";
		
		$(REPORTS.data_table + " tfoot").html(footer_output);
	}
};

$(document).ready(function() {
	REPORTS.init();
});