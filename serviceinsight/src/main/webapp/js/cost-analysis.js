var COST_ANALYSIS = {
	moment_format:"MM/YYYY",
        
	init:function() {
		COST_ANALYSIS.set_variables();
		COST_ANALYSIS.bind_events();
		COST_ANALYSIS.view.init();
		COST_ANALYSIS.load.init();
	},
	set_variables:function() {
	},
	bind_events:function() {
	},
	view: {
		view_date_min:"01/2012",
		view_date_max:"12/2024",
		date_dropdown:"#current-viewing-date",
		date_viewing_section:".costs-viewing",
		current_viewing_input_date:"#current-viewing-input-date",
		current_view_prev_month:"#current-view-prev-month",
		current_view_prev_month_link:"#current-view-prev-month-link",
		current_view_next_month:"#current-view-next-month",
		current_view_next_month_link:"#current-view-next-month-link",
                cost_summary_link:"#cost-summary-link",
                cost_summary_table_div:"#cost-summary-table-div",
                cost_customer_link:"#cost-customer-link",
                cost_customer_table_div:"#cost-customer-table-div",
                cost_expense_category_link:"#cost-expense-category-link",
                cost_expense_category_table_div:"#cost-expense-category-table-div",
		switch_date_class:"switch-date",
		expense_type_filter_column:"#expense-type-filter-column",
                last_cost_view:"summary",
                
		init:function() {
			COST_ANALYSIS.view.bind_events();
			COST_ANALYSIS.view.build_dropdown();
			COST_ANALYSIS.view.build_date_links();
		},
		set_summary_view:function() {
                        $link_view = $(COST_ANALYSIS.view.cost_summary_link);
                        $link_view.css("background-color","#ccc");
                        $(COST_ANALYSIS.load.costs_summary_table).show();
                        $(COST_ANALYSIS.load.chart_container).show();
                        $(COST_ANALYSIS.view.cost_customer_link).css("background-color","");
                        $(COST_ANALYSIS.load.costs_customer_table).hide();
                        $(COST_ANALYSIS.view.cost_expense_category_link).css("background-color","");
                        $(COST_ANALYSIS.load.costs_expense_category_table).hide();
		},
		set_customer_view:function() {
                        $link_view = $(COST_ANALYSIS.view.cost_customer_link);
                        $link_view.css("background-color","#ccc");
                        $(COST_ANALYSIS.view.cost_summary_link).css("background-color","");
                        $(COST_ANALYSIS.load.costs_summary_table).hide();
                        $(COST_ANALYSIS.load.chart_container).hide();
                        $(COST_ANALYSIS.load.costs_customer_table).show();
                        $(COST_ANALYSIS.view.cost_expense_category_link).css("background-color","");
                        $(COST_ANALYSIS.load.costs_expense_category_table).hide();
		},
		set_expense_category_view:function() {
                        $link_view = $(COST_ANALYSIS.view.cost_expense_category_link);
                        $link_view.css("background-color","#ccc");
                        $(COST_ANALYSIS.view.cost_summary_link).css("background-color","");
                        $(COST_ANALYSIS.load.costs_summary_table).hide();
                        $(COST_ANALYSIS.load.chart_container).hide();
                        $(COST_ANALYSIS.view.cost_customer_link).css("background-color","");
                        $(COST_ANALYSIS.load.costs_customer_table).hide();
                        $(COST_ANALYSIS.load.costs_expense_category_table).show();
		},
		bind_events:function() {
			$(COST_ANALYSIS.view.date_dropdown).change(function() {
				var $option = $(this).find(":selected");
				COST_ANALYSIS.view.switch_date($option.val());
			});
			$(document).on("click", "." + COST_ANALYSIS.view.switch_date_class, function() {
				var $option = $(this);
				$(COST_ANALYSIS.view.date_dropdown).val($option.data("value"));
                                $(COST_ANALYSIS.view.date_dropdown).selectBox('value',$option.data("value"));
				COST_ANALYSIS.view.switch_date($option.data("value"));
			});
			Highcharts.setOptions({
			    lang: {
			        numericSymbols: null //otherwise by default ['k', 'M', 'G', 'T', 'P', 'E']
			    }
			});
                        $(document).on("click", COST_ANALYSIS.view.cost_summary_link, function() {
                            COST_ANALYSIS.view.last_cost_view = "summary";
                            COST_ANALYSIS.view.set_summary_view();
			});
                        $(document).on("click", COST_ANALYSIS.view.cost_customer_link, function() {
                            COST_ANALYSIS.view.last_cost_view = "customer";
                            COST_ANALYSIS.view.set_customer_view();
			});
                        $(document).on("click", COST_ANALYSIS.view.cost_expense_category_link, function() {
                            COST_ANALYSIS.view.last_cost_view = "expense_category";
                            COST_ANALYSIS.view.set_expense_category_view();
			});
		},
		switch_date:function(date_value) {
			$(COST_ANALYSIS.view.current_viewing_input_date).val(date_value);
			COST_ANALYSIS.load.get_costs();
			COST_ANALYSIS.view.build_date_links();
                        if (COST_ANALYSIS.view.last_cost_view == "summary") {
                            COST_ANALYSIS.view.set_summary_view();
                        } else if (COST_ANALYSIS.view.last_cost_view == "customer") {
                            COST_ANALYSIS.view.set_customer_view();
                        } else if (COST_ANALYSIS.view.last_cost_view == "expense_category") {
                            COST_ANALYSIS.view.set_expense_category_view();
                        }
		},
		build_date_links:function() {
			var $option = $(COST_ANALYSIS.view.date_dropdown + " :selected");
			var prev_label = $option.prev().html();
			var prev_value = $option.prev().val();
			var next_label = $option.next().html();
			var next_value = $option.next().val();
			
			var prev_link = "";
			var next_link = "";
			if(prev_label != undefined) prev_link = COST_ANALYSIS.view.build_date_link(prev_label, prev_value, true);
			if(next_label != undefined) next_link = COST_ANALYSIS.view.build_date_link(next_label, next_value, false);
			$(COST_ANALYSIS.view.current_view_prev_month).html(prev_link);
			$(COST_ANALYSIS.view.current_view_next_month).html(next_link);
		},
		build_date_link:function(month_name, month_value, prev) {
			var prev_icon = "<i class=\"fa fa-angle-double-left\"></i>";
			var next_icon = "<i class=\"fa fa-angle-double-right\"></i>";
                        var link_id = (prev) ? "current-view-prev-month-link" : "current-view-next-month-link";
			var output = "<a id=\"" + link_id + "\" class=\"" + COST_ANALYSIS.view.switch_date_class + "\" href=\"javascript:;\" data-label=\"" + month_name + "\" data-value=\"" + month_value + "\">";
			(prev) ? output += prev_icon + month_name : output += month_name + next_icon;
			output += "</a>";
			return output;
		},
		build_dropdown:function() {
			var output = "";
			var type = "month";
			var display_format = "MMM YYYY";
			var start_moment = moment(COST_ANALYSIS.view.view_date_min, COST_ANALYSIS.moment_format).startOf(type);
			var end_moment = moment(COST_ANALYSIS.view.view_date_max, COST_ANALYSIS.moment_format).endOf(type);
			var now = moment().startOf(type);
			while(start_moment < end_moment) {
				var selected = "";
				var month_of = start_moment.startOf(type).format(COST_ANALYSIS.moment_format);
				//set the month to the current month, as that is the page load default
				if(month_of == now.format(COST_ANALYSIS.moment_format)) {
					selected = " selected=\"selected\"";
				}
				output += "<option value=\"" + month_of + "\"" + selected + ">" + start_moment.format(display_format) + "</option>";
				start_moment.add(1, type).startOf(type);
			}
			$(COST_ANALYSIS.view.date_dropdown).html(output);
			
			//destroy a previous version if it exists
			$(COST_ANALYSIS.view.date_dropdown).selectBox("destroy");
			
			//initiate the fancy looking dropdown
			$(COST_ANALYSIS.view.date_dropdown).selectBox();
		}
	},
	load: {
		costs_summary_table:"#cost-summary-table-div",
		costs_customer_table:"#cost-customer-table-div",
		costs_expense_category_table:"#cost-expense-category-table-div",
                chart_container:"#chart",
                
		init:function() {
			COST_ANALYSIS.load.get_costs();
		},
		get_costs:function() {
			
                        var selected_option = $(COST_ANALYSIS.view.date_dropdown + " :selected").val();
			var params = "?month=" + selected_option;
			UTIL.add_table_loader(COST_ANALYSIS.load.costs_summary_table);
			UTIL.add_table_loader(COST_ANALYSIS.load.costs_customer_table);
			UTIL.add_table_loader(COST_ANALYSIS.load.costs_expense_category_table);
			
                        $.ajax ({
                                url: PAGE_CONSTANTS.BASE_URL + "costs/analysisData.json" + params,
                                type: "GET",
                                dataType: "json",
                                accepts: {
                                    text: "application/json"
                                },
                                success:function(data) {
                                        COST_ANALYSIS.load.build_cost_summary_table(data.summaryRecords);
                                        COST_ANALYSIS.load.build_cost_customer_table(data.customerRecords);
                                        COST_ANALYSIS.load.build_cost_expense_category_table(data.expenseCategoryRecords);
                                }
                        });
		},
		build_cost_summary_table:function(costs) {
			var output = "";
                        var header_output = "";
			var footer_output = "";
			var prev_total_amount = 0;
			var current_total_amount = 0;
			var next_total_amount = 0;
                        
                        header_output += "<tr>";
                        header_output += "<th>Type</th>";
                        header_output += "<th>Subtype</th>";
                        var $prev_date = moment($(COST_ANALYSIS.view.current_view_prev_month_link).data("value"), COST_ANALYSIS.moment_format);
                        header_output += "<th>" + $prev_date.format("MMM") + "</th>";
                        var $curr_date = moment($(COST_ANALYSIS.view.date_dropdown + " :selected").val(), COST_ANALYSIS.moment_format);
                        header_output += "<th>" + $curr_date.format("MMM") + "</th>";
                        var $next_date = moment($(COST_ANALYSIS.view.current_view_next_month_link).data("value"), COST_ANALYSIS.moment_format);
                        header_output += "<th>" + $next_date.format("MMM") + "</th>";
                        
                        for (i=0; i < costs.length; i++) {
                            output += "<tr>";
                            output += "<td>" + costs[i].costItemCostSubType.typeLabel + "</td>";
                            output += "<td>" + (costs[i].costItemCostSubType.subtypeLabel ? costs[i].costItemCostSubType.subtypeLabel : "") + "</td>";
                            output += "<td>" + accounting.formatMoney(costs[i].previous) + "</td>";
                            prev_total_amount += costs[i].previous;
                            output += "<td>" + accounting.formatMoney(costs[i].current) + "</td>";
                            current_total_amount += costs[i].current;
                            output += "<td>" + accounting.formatMoney(costs[i].next) + "</td>";
                            next_total_amount += costs[i].next;
                            output += "</tr>";
                        }
                        
                        footer_output += "<tr class=\"total-row\">";
                        footer_output += "<td colspan=\"2\" class=\"right\">TOTAL</td>";
                        footer_output += "<td>" + accounting.formatMoney(prev_total_amount) + "</td>";
                        footer_output += "<td>" + accounting.formatMoney(current_total_amount) + "</td>";
                        footer_output += "<td>" + accounting.formatMoney(next_total_amount) + "</td>";
                        footer_output += "</tr>";
			
			$(COST_ANALYSIS.load.costs_summary_table + " thead").html(header_output);
			$(COST_ANALYSIS.load.costs_summary_table + " tbody").html(output);
			$(COST_ANALYSIS.load.costs_summary_table + " tfoot").html(footer_output);
                        
                        COST_ANALYSIS.load.build_costs_summary_chart(costs, [$prev_date.format("MMM"), $curr_date.format("MMM"), $next_date.format("MMM")]);
		},
		build_cost_customer_table:function(costs) {
			var output = "";
                        var header_output = "";
			var footer_output = "";
			var prev_total_amount = 0;
			var current_total_amount = 0;
			var next_total_amount = 0;
                        
			var is_admin = false;
			if(GLOBAL && GLOBAL.is_admin) is_admin = GLOBAL.is_admin;
                        
                        header_output += "<tr>";
                        header_output += "<th>Type</th>";
                        header_output += "<th>Subtype</th>";
                        header_output += "<th>Customer</th>";
                        var $prev_date = moment($(COST_ANALYSIS.view.current_view_prev_month_link).data("value"), COST_ANALYSIS.moment_format);
                        header_output += "<th>" + $prev_date.format("MMM") + "</th>";
                        var $curr_date = moment($(COST_ANALYSIS.view.date_dropdown + " :selected").val(), COST_ANALYSIS.moment_format);
                        header_output += "<th>" + $curr_date.format("MMM") + "</th>";
                        var $next_date = moment($(COST_ANALYSIS.view.current_view_next_month_link).data("value"), COST_ANALYSIS.moment_format);
                        header_output += "<th>" + $next_date.format("MMM") + "</th>";
                        
                        for (i=0; i < costs.length; i++) {
                            output += "<tr>";
                            output += "<td>" + costs[i].costItemCostSubType.typeLabel + "</td>";
                            output += "<td>" + (costs[i].costItemCostSubType.subtypeLabel ? costs[i].costItemCostSubType.subtypeLabel : "") + "</td>";
                            output += "<td>" + (costs[i].customer ? costs[i].customer : "") + "</td>";
                            output += "<td>" + accounting.formatMoney(costs[i].previous) + "</td>";
                            prev_total_amount += costs[i].previous;
                            output += "<td>" + accounting.formatMoney(costs[i].current) + "</td>";
                            current_total_amount += costs[i].current;
                            output += "<td>" + accounting.formatMoney(costs[i].next) + "</td>";
                            next_total_amount += costs[i].next;
                            output += "</tr>";
                        }
                        
                        footer_output += "<tr class=\"total-row\">";
                        footer_output += "<td colspan=\"3\" class=\"right\">TOTAL</td>";
                        footer_output += "<td>" + accounting.formatMoney(prev_total_amount) + "</td>";
                        footer_output += "<td>" + accounting.formatMoney(current_total_amount) + "</td>";
                        footer_output += "<td>" + accounting.formatMoney(next_total_amount) + "</td>";
                        footer_output += "</tr>";
			
			$(COST_ANALYSIS.load.costs_customer_table + " thead").html(header_output);
			$(COST_ANALYSIS.load.costs_customer_table + " tbody").html(output);
			$(COST_ANALYSIS.load.costs_customer_table + " tfoot").html(footer_output);
		},
		build_cost_expense_category_table:function(costs) {
			var output = "";
                        var header_output = "";
			var footer_output = "";
			var prev_total_amount = 0;
			var current_total_amount = 0;
			var next_total_amount = 0;
                        
			var is_admin = false;
			if(GLOBAL && GLOBAL.is_admin) is_admin = GLOBAL.is_admin;
                        
                        header_output += "<tr>";
                        header_output += "<th>Type</th>";
                        header_output += "<th>Subtype</th>";
                        header_output += "<th>Cost Category</th>";
                        var $prev_date = moment($(COST_ANALYSIS.view.current_view_prev_month_link).data("value"), COST_ANALYSIS.moment_format);
                        header_output += "<th>" + $prev_date.format("MMM") + "</th>";
                        var $curr_date = moment($(COST_ANALYSIS.view.date_dropdown + " :selected").val(), COST_ANALYSIS.moment_format);
                        header_output += "<th>" + $curr_date.format("MMM") + "</th>";
                        var $next_date = moment($(COST_ANALYSIS.view.current_view_next_month_link).data("value"), COST_ANALYSIS.moment_format);
                        header_output += "<th>" + $next_date.format("MMM") + "</th>";
                        
                        for (i=0; i < costs.length; i++) {
                            output += "<tr>";
                            output += "<td>" + costs[i].costItemCostSubType.typeLabel + "</td>";
                            output += "<td>" + (costs[i].costItemCostSubType.subtypeLabel ? costs[i].costItemCostSubType.subtypeLabel : "") + "</td>";
                            output += "<td>" + (costs[i].expenseCategoryName ? costs[i].expenseCategoryName : "") + "</td>";
                            output += "<td>" + accounting.formatMoney(costs[i].previous) + "</td>";
                            prev_total_amount += costs[i].previous;
                            output += "<td>" + accounting.formatMoney(costs[i].current) + "</td>";
                            current_total_amount += costs[i].current;
                            output += "<td>" + accounting.formatMoney(costs[i].next) + "</td>";
                            next_total_amount += costs[i].next;
                            output += "</tr>";
                        }
                        
                        footer_output += "<tr class=\"total-row\">";
                        footer_output += "<td colspan=\"3\" class=\"right\">TOTAL</td>";
                        footer_output += "<td>" + accounting.formatMoney(prev_total_amount) + "</td>";
                        footer_output += "<td>" + accounting.formatMoney(current_total_amount) + "</td>";
                        footer_output += "<td>" + accounting.formatMoney(next_total_amount) + "</td>";
                        footer_output += "</tr>";
			
			$(COST_ANALYSIS.load.costs_expense_category_table + " thead").html(header_output);
			$(COST_ANALYSIS.load.costs_expense_category_table + " tbody").html(output);
			$(COST_ANALYSIS.load.costs_expense_category_table + " tfoot").html(footer_output);
		},
		build_costs_summary_chart:function(costs_instance, dateRange) {
                        var dataSeries = [];
                        for (i=0; i < costs_instance.length; i++) {
                            var name_field = (costs_instance[i].costItemCostSubType.subtypeLabel ? costs_instance[i].costItemCostSubType.typeLabel + " - " + costs_instance[i].costItemCostSubType.subtypeLabel : costs_instance[i].costItemCostSubType.typeLabel);
                            var data_field = [costs_instance[i].previous, costs_instance[i].current, costs_instance[i].next];
                            dataSeries.push({name:name_field, data:data_field});
                        }
			$(COST_ANALYSIS.load.chart_container).highcharts({
		        title: {
		            text:null,
                            align:"left"
		        },
		        xAxis: {
		            categories:dateRange
		        },
		        yAxis: {
		            title: {
		                text:"Costs"
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
		            }],
                        min:0
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
                            enabled:true,
		            layout:"vertical",
		            align:"center",
		            verticalAlign:"bottom",
		            borderWidth:0
		        },
		        series:dataSeries,
		        credits: { enabled:false }
		    });
		}
	}
};

$(document).ready(function() {
	COST_ANALYSIS.init();
});