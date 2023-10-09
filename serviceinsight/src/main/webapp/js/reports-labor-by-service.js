var REPORTS = {
	chart:"#chart",
	sub_chart_link:"#sub-chart-link",
	sub_chart_container:"#sub-chart-container",
	sub_chart_left:"#sub-chart-left",
	sub_chart_right:"#sub-chart-right",
	sub_table_container:"#sub-table-container",
	sub_table_left:"#sub-table-left",
	sub_table_right:"#sub-table-right",
	filter_criteria:".filter-criteria",
	data_table:"#report-data-table",
	report_date_range:"#report-date-range",
	report_custom_dates:"#report-custom-dates",
	report_start_date:"#report-start-date",
	report_end_date:"#report-end-date",
	run_report_btn:"#run-report-btn",
	report_section:".report-section",
	msg_error_dates_required:"#msg-error-dates-required",
	msg_error_dates_start_before_end:"#msg-error-start-before-end",
	export_excel:"#download-excel",
	table_class:".data-table",
	tab_class:".table-tab",
	init:function() {
		REPORTS.bind_events();
		REPORTS.dates.init();
	},
	bind_events:function() {
		$(REPORTS.run_report_btn).click(function() {
			REPORTS.run_report(REPORTS.chart, REPORTS.data_table, "Labor Cost by Service");
			
			if($(REPORTS.sub_chart_container).is(":visible")) {
				REPORTS.run_report(REPORTS.sub_chart_left, REPORTS.sub_table_left, "Uncategorized Labor Cost by Customer", "customer");
				REPORTS.run_report(REPORTS.sub_chart_right, REPORTS.sub_table_right, "Uncategorized Labor Cost by Task", "task");
			}
		});
		
		$(REPORTS.sub_chart_link).click(function() {
			$(REPORTS.sub_chart_container).show();
			$(REPORTS.sub_table_container).show();
			
			REPORTS.run_report(REPORTS.sub_chart_left, REPORTS.sub_table_left, "Uncategorized Labor Cost by Customer", "customer");
			REPORTS.run_report(REPORTS.sub_chart_right, REPORTS.sub_table_right, "Uncategorized Labor Cost by Task", "task");
			
			$(this).remove();
		});
		
		$(REPORTS.export_excel).click(function() {
			var url = REPORTS.build_report_url(PAGE_CONSTANTS.FORMAT.excel);
			$(this).prop("href",url);
			return true;
		});
		
		$(REPORTS.tab_class).click(function() {
			var table = $(this).data("view");
			$(REPORTS.table_class).hide();
			$("#" + table).show();
			
			$(REPORTS.tab_class).removeClass("selected");
			$(this).addClass("selected");
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
		},
		setup_dates:function() {
			var output = "";
			
			var start_month_moment = moment("12/2015",REPORTS.dates.format);
			var end_month_moment = moment();
			while(end_month_moment > start_month_moment) {
				output += "<option value=\"year\" data-start-date=\"" + end_month_moment.startOf("month").format(REPORTS.dates.format) + "\" data-end-date=\"" + end_month_moment.endOf("month").format(REPORTS.dates.format) + "\">" + end_month_moment.format(REPORTS.dates.display_format); + "</option>";
				end_month_moment.subtract(1, "month");
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
	build_report_url:function(report_format, report_type) {
		var url = PAGE_CONSTANTS.BASE_URL + "reports/laborbyservice." + report_format + "?";
		
		var start_date = $(REPORTS.report_start_date).val();
		var end_date = $(REPORTS.report_end_date).val();
		
		url += "sd=" + start_date + "&ed=" + end_date;
		if(report_type) url += "&type=" + report_type;
		if(report_format == PAGE_CONSTANTS.FORMAT.excel) url += "&export";
		
		return url;
	},
	run_report:function(chart_container, table_container, title, report_type) {
		//clear validation message
		UTIL.clear_msg_in_content_box($(REPORTS.filter_criteria));
		
		var is_sub_report = false;
		if(report_type) is_sub_report = true;
		
		var url = REPORTS.build_report_url(PAGE_CONSTANTS.FORMAT.json, report_type);
		var start_date = $(REPORTS.report_start_date).val();
		var end_date = $(REPORTS.report_end_date).val();
		
		if(start_date && end_date) {
			if(moment(start_date, REPORTS.dates.format).isAfter(moment(end_date, REPORTS.dates.format))) {
				UTIL.add_error_msg_to_content_box($(REPORTS.filter_criteria), $(REPORTS.msg_error_dates_start_before_end).val());
				return false;
			}
		} else {
			UTIL.add_error_msg_to_content_box($(REPORTS.filter_criteria), $(REPORTS.msg_error_dates_required).val());
			return false;
		}
		
		//setup loaders
		var loader = "<div class=\"loader\"><img src=\"" + PAGE_CONSTANTS.LOADER_URL + "\" /><br/>Retrieving Chart Data...</div>";
		$(chart_container).html(loader);
		if($(table_container).is(":visible")) UTIL.add_table_loader(table_container, "Retrieving Chart Data...");
		$(REPORTS.report_section).show();
		GLOBAL.set_nav_height();
		
		//ajax call
		$.ajax ({
			url: url,
			type: "GET",
			success:function(data) {
				var report_data = REPORTS.sort_data_for_chart(data, title);
				REPORTS.build_chart(chart_container, report_data);
				REPORTS.build_table(table_container, report_data);
				
				$(REPORTS.sub_chart_link).show();
			}
		});
	},
	sort_data_for_chart:function(data, title) {
		var cost = [];
		var total = 0;
		for(var i=0; i < data.length; i ++) {
			var record = data[i];
			for(var key in record) {
				cost.push({name:key, cost:record[key]});
				total += record[key];
			}
		}
		
		for(var i=0; i < cost.length; i++) {
			var record = cost[i];
			var percent = (record.cost / total) * 100;
			percent = (Math.round(percent + "e+2")  + "e-2");
			cost[i]["y"] = parseFloat(percent);
		}
		
		var wrapper = {
				series: [{
					name: 'Labor Cost',
					colorByPoint: true,
					data: cost
				}],
				title:title
		};
		
		return wrapper;
	},
	build_chart:function(container, data) {
		$(container).highcharts({
	        chart: {
	            type: 'pie'
	        },
	        title: {
	            text: data.title
	        },
	        plotOptions: {
	            pie: {
	                allowPointSelect: true,
	                cursor: 'pointer',
	                dataLabels: {
	                    enabled: true,
	                    format: '<b>{point.name}</b>: {point.y:.2f} %',
	                    style: {
	                        color: (Highcharts.theme && Highcharts.theme.contrastTextColor) || 'black'
	                    }
	                }
	            }
	        },
	        tooltip: {
	            headerFormat: '<span style="font-size:10px">{point.key}</span><table>',
	            pointFormatter: function() {
	            	var series = this.series.chart.series, // get all series 
	                index = this.series.xData.indexOf(this.x), // get index
	                str = '';
	            	
	            	$.each(series, function(i, s) { 
	            		str += '<tr><td style="padding:0;">Monthly Percentage: </td>' + '<td style="padding:0; text-align:right;"><b>' + s.data[index].y + '%</b></td></tr>';
	            		str += '<tr><td style="padding:0;">Monthly Actual: </td>' + '<td style="padding:0; text-align:right;"><b>' + UTIL.convert_currency_for_ui(s.data[index].cost) + '</b></td></tr>';
	                });
	            	return str;
	            },
	            footerFormat: '</table>',
	            useHTML: true
	        },
	        series: data.series,
	        credits: {
	            enabled: false
	        }
	    });
	},
	build_table:function(table_container, data) {
		var output = "";
		var total_cost = 0;
		
		for(var i=0; i < data.series[0].data.length; i++) {
			var record = data.series[0].data[i];
			var cost = record.cost;
			output += "<tr>";
			output += "<td>" + record.name + "</td>";
			output += "<td>&nbsp;</td>";
			output += "<td class=\"right\">" +  record.y +  "%</td>";
			output += "<td class=\"right\">" +  UTIL.convert_currency_for_ui(cost) +  "</td>";
			output += "</tr>";
			total_cost += cost;
		}
		$(table_container + " tbody").html(output);
		
		var footer_output = "<tr class=\"total-row\"><td></td><td></td><td class=\"right\">TOTAL</td><td class=\"total-column\">" + accounting.formatMoney(total_cost) + "</td></tr>";
		$(table_container + " tfoot").html(footer_output);
		
		GLOBAL.set_nav_height();
	}
};

$(document).ready(function() {
	REPORTS.init();
});