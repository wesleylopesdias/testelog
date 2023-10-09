var DASH = {
	stat_number_class:".stat-bottom",
	init:function() {
		DASH.bind_events();
		DASH.chart.init();
		DASH.customers.init();
		DASH.contracts.init();
		DASH.services.init();
		DASH.dates.init();
		DASH.get_data(moment().format(DASH.dates.date_server_format));
	},
	bind_events:function() {
		
	},
	dates:{
		date_class:".current-month",
		date_display_format:"MMM 'YY",
		date_dropdown_display_format:"MMM YYYY",
		date_server_format:"MM/YYYY",
		current_date_field:"#currently-viewing",
		init:function() {
			DASH.dates.bind_events();
			DASH.dates.setup_date_dropdown();
			DASH.dates.set_dates(moment().format(DASH.dates.date_display_format));
		},
		bind_events:function() {
			$(DASH.dates.current_date_field).change(function() {
				var date = $(this).val();
				DASH.get_data(date);
				DASH.dates.set_dates(moment(date, DASH.dates.date_server_format).format(DASH.dates.date_display_format));
			});
		},
		setup_date_dropdown:function() {
			var output = "";
			
			var start_month_moment = moment("12/2014",DASH.dates.date_server_format);
			var end_month_moment = moment();
			while(end_month_moment > start_month_moment) {
				output += "<option value=\"" + end_month_moment.startOf("month").format(DASH.dates.date_server_format) + "\">" + end_month_moment.format(DASH.dates.date_dropdown_display_format); + "</option>";
				end_month_moment.subtract(1, "month");
			}

			$(DASH.dates.current_date_field).html(output);
			
			$(DASH.dates.current_date_field).selectBox();
		},
		set_dates:function(date) {
			$(DASH.dates.date_class).html(date);
		}
	},
	get_data:function(date) {
		//setup loaders
		UTIL.add_table_loader($(DASH.customers.container), "Loading Top Customers...");
		UTIL.add_table_loader($(DASH.contracts.container), "Loading Recents SOWs...");
		UTIL.add_table_loader($(DASH.services.top_ms_services_container), "Loading Top MS Services...");
		UTIL.add_table_loader($(DASH.services.top_cloud_services_container), "Loading Top Cloud Services...");
		UTIL.add_chart_loader(DASH.chart.chart_container);
		$(DASH.stat_number_class).html("--");
		
		if(date) date = "?ed=" + date;
		
		//ajax call to get the dashboard data
		$.ajax ({
			url: PAGE_CONSTANTS.BASE_URL + "dashboard/data.json" + date,
			type: "GET",
			success:function(data) {
				//build out page
				DASH.contracts.build_contracts(data.contractStats.contracts);
				DASH.contracts.set_tiles(data.contractStats.msContractCount, data.contractStats.cloudContractCount, data.contractStats.msCloudContractCount)
				DASH.services.set_tiles(data.serviceStats.msCurrentMonthRevenue, data.serviceStats.cloudCurrentMonthRevenue);
				DASH.services.build_top_services_table("ms", data.serviceStats.topManagedServices);
				DASH.services.build_top_services_table("cloud", data.serviceStats.topCloudServices);
				DASH.customers.build_customers_table(data.customerStats.customers);
				DASH.chart.build_chart(data.serviceStats.chartData);
			}
		});
	},
	customers:{
		container:"#top-customers",
		init:function() {
			DASH.customers.bind_events();
		},
		bind_events:function() {
			
		},
		build_customers_table:function(customers) {
			var output = "";
			if(customers && customers.length > 0) {
				for(var i=0; i < customers.length; i++) {
					var customer = customers[i];
					output += "<tr>";
					output += "<td>" + customer.customerName + "</td>";
					output += "<td class=\"center\"><a href=\"/si/contracts?cid=" + customer.id + "\">" + customer.contractCount + "</a></td>";
					output += "<td class=\"right\">" + UTIL.convert_currency_for_ui(customer.currentMonthTotalRecurringRevenue) + "</td>";
			        output += "</tr>";
				}
			} else {
				output = "<td colspan=\"5\" class=\"no-results\">No recent customers were returned.</td>";
			}
			$(DASH.customers.container + " tbody").html(output);
		}
	},
	contracts:{
		container:"#recent-sows",
		ms_contract_tile:"#tile-ms-contract-count",
		cloud_contract_tile:"#tile-cloud-contract-count",
		ms_cloud_contract_tile:"#tile-ms-cloud-contract-count",
		init:function() {
			DASH.contracts.bind_events();
		},
		bind_events:function() {
			
		},
		build_contracts:function(contracts) {
			var output = "";
			if(contracts && contracts.length > 0) {
				for(var i=0; i < contracts.length; i++) {
					var contract = contracts[i];
					output += "<tr>";
					output += "<td>" + contract.customerName + "</td>";
					output += "<td class=\"center\"><a href=\"/si/contracts/" + contract.id + "\">" + contract.jobNumber + "</a></td>";
					output += "<td class=\"center\">" + contract.type + "</td>";
					output += "<td class=\"right\">" + UTIL.convert_dates_for_ui(contract.startDate) + "</td>";
					output += "<td class=\"right\">" + UTIL.convert_currency_for_ui(contract.currentMonthTotalRecurringRevenue) + "</td>";
			        output += "</tr>";
				}
			} else {
				output = "<td colspan=\"5\" class=\"no-results\">No recent SOWs were returned.</td>";
			}
			$(DASH.contracts.container + " tbody").html(output);
		},
		set_tiles:function(ms_count, cloud_count, ms_cloud_count) {
			$(DASH.contracts.ms_contract_tile).html(ms_count);
			$(DASH.contracts.cloud_contract_tile).html(cloud_count);
			$(DASH.contracts.ms_cloud_contract_tile).html(ms_cloud_count);
		}
	},
	services:{
		ms_revenue_tile:"#tile-ms-revenue",
		cloud_revenue_tile:"#tile-cloud-revenue",
		top_ms_services_container:"#top-ms-services",
		top_cloud_services_container:"#top-cloud-services",
		init:function() {
			DASH.services.bind_events();
		},
		bind_events:function() {
			
		},
		build_top_services_table:function(type, services) {
			var output = "";
			if(services && services.length > 0) {
				for(var i=0; i < services.length; i++) {
					var service = services[i];
					output += "<tr>";
					output += "<td>" + service.serviceName + "</td>";
					output += "<td class=\"right\">" + UTIL.convert_currency_for_ui(service.currentMonthTotalRecurringRevenue) + "</td>";
			        output += "</tr>";
				}
			} else {
				output = "<td colspan=\"2\" class=\"no-results\">No results returned.</td>";
			}
			
			var table_selector = DASH.services.top_ms_services_container;
			if(type == "cloud") {
				table_selector = DASH.services.top_cloud_services_container;
			}
			
			$(table_selector + " tbody").html(output);
		},
		set_tiles:function(ms_revenue, cloud_revenue) {
			$(DASH.services.ms_revenue_tile).html(UTIL.convert_currency_for_ui(ms_revenue));
			$(DASH.services.cloud_revenue_tile).html(UTIL.convert_currency_for_ui(cloud_revenue));
		}
	},
	chart:{
		chart_container:"#chart",
		init:function() {
			DASH.chart.bind_events();
		},
		bind_events:function() {
			Highcharts.setOptions({
			    lang: {
			        numericSymbols: null //otherwise by default ['k', 'M', 'G', 'T', 'P', 'E']
			    }
			});
		},
		get_chart_data:function() {
			//some ajax metho
		},
		build_chart:function(data) {
			$(DASH.chart.chart_container).highcharts({
		        title: {
		            text:"Monthly Revenue"
		        },
		        subtitle: {
		            text:"(for the past 6 months)"
		        },
		        xAxis: {
		            categories:data.dateRange,
		        },
		        yAxis: {
		            title: {
		                text:"Total Revenue"
		            },
		            labels:{
		            	//rotation: -45
		            	formatter: function () {
		                    return '$' + this.axis.defaultLabelFormatter.call(this);
		                }
		            },
		            plotLines: [{
		                value: 0,
		                width: 1,
		                color:"#808080"
		            }]
		        },
		        tooltip: {
		            headerFormat:'<span style="font-size:10px">{point.key}</span><table>',
		            pointFormat:'<tr><td style="color:{series.color};padding:0">{series.name}: </td>' +
		                '<td style="padding:0; text-align:right;"><b>${point.y}</b></td></tr>',
		            footerFormat:'</table>',
		            shared:true,
		            useHTML:true
		        },
		        legend: {
		            layout:"vertical",
		            align:"center",
		            verticalAlign:"bottom",
		            borderWidth:0
		        },
		        series:data.series,
		        credits: { enabled:false }
		    });
		}
	}
};

$(document).ready(function() {
	DASH.init();
});

