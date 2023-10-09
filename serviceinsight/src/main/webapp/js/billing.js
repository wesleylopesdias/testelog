var BILLING = {
	is_billing_field:"#is-billing",
	is_billing_role:false,
	customer_field:"#customer",
	billing_search_date_field: "#billing-search-date",
	engagement_manager_field: "#engagement-manager",
	report_format_field:"#report-format",
	show_detail_field:"#show-detail",
	invoice_status_field:"#invoice-status",
	get_report_btn: "#get-report",
	results_container:"#billing-results",
	results_table:"#results-table",
	results_options:"#results-options",
	options_menu_btn:".menu-btn",
	date_range:"#date-range",
	select_all:"#select-all",
	billing_status:".billing-status",
	billing_loader:"#billing-loader",
	billing_success:"#billing-success-msg",
	billing_error:"#billing-error-msg",
	msg_checkbox_required:"#msg-validation-checkbox-required",
	msg_general_ajax_error:"#msg-general-ajax-error",
	is_first_row:true,
	moment_format: "MM/YYYY",
	init:function() {
		BILLING.set_variables();
		BILLING.bind_events();
		BILLING.setup_autocomplete();
		BILLING.setup_date_dropdown();
	},
	set_variables:function() {
		if($(BILLING.is_billing_field).val() == "true") BILLING.is_billing_role = true;
	},
	bind_events:function() {
		$(BILLING.get_report_btn).click(function() {
			var date = $(BILLING.billing_search_date_field).val();
			var customer = $(BILLING.customer_field).val();
			var engagement_manager = $(BILLING.engagement_manager_field).val();
			var report_format = $(BILLING.report_format_field).val();
			var show_detail = $(BILLING.show_detail_field).val();
			var invoice_status = $(BILLING.invoice_status_field).val();
			var hide_detail = false;
			var report_type = ".xlsx?rollupByQueryExport";
			if(report_format == "json") report_type = ".json?rollupByQuery";
			
			customer = "&customer=" + customer;
			engagement_manager = "&manager=" + engagement_manager;
			if(show_detail == "false") {
				hide_detail = true;
			}
			show_detail = "&showdetail=" + show_detail;
			
			if(invoice_status) {
				invoice_status = "&invsts=" + invoice_status; 
			} else {
				invoice_status = "";
			}
			
			var url = PAGE_CONSTANTS.BASE_URL + "contractservices/" + date + report_type + customer + engagement_manager + show_detail + invoice_status;
			
			if(report_format == "json") {
				BILLING.get_table_data(url, hide_detail);
				return false;
			} else {
				BILLING.get_excel_export($(this), url);
			}
		});
		
		$(BILLING.select_all).click(function() {
			var checked = false;
			if(this.checked) checked = true;
			$(BILLING.results_container).find("input[type='checkbox']").prop("checked",checked);
		});
		
		$(BILLING.results_options + " a").click(function() {
			$(BILLING.results_options).css("display","none");
			var status = $(this).data("status");
			BILLING.update_invoices(status);
		});
	},
	setup_date_dropdown:function() {
		var options = "";
		var start_moment = moment("04/01/2015", "MM/DD/YYYY");
		var end_moment = moment().add(2,"months");
		var now = moment();
		while(start_moment < end_moment) {
			var selected = "";
			var month_year = start_moment.format(BILLING.moment_format);
			//set the month to the current month, as that is the page load default
			if(month_year == now.format(BILLING.moment_format)) {
				selected = " selected=\"selected\"";
			}
			options += "<option value=\"" + month_year + "\" " + selected + ">" + start_moment.format("MMM YYYY") + "</option>";
			start_moment.add(1, 'months').startOf("month");
		}
		
		$(BILLING.billing_search_date_field).html(options);
	},
	setup_autocomplete:function() {
		$.ajax ({
			url: PAGE_CONSTANTS.BASE_URL + "customers.json?si=true",
			type: "GET",
			success:function(data) {
				var customers = [];
				for(var i=0; i<data.length; i++) {
					var code = data[i].name;
					customers.push({value: code});
				}
				//setup autocomplete fields
				BILLING.bind_autocompletes(BILLING.customer_field, customers);
			}
		});
	},
	bind_autocompletes:function(obj_id, customers) {
		$(obj_id).autocomplete({ source: customers });
	},
	get_excel_export:function($btn, url) {
		$btn.attr("href",url);
		return true;
	},
	get_table_data:function(url, hide_detail) {
		$(BILLING.results_container).show();
		UTIL.add_table_loader(BILLING.results_table);
		
		$.ajax ({
			url: url,
			type: "GET",
			success:function(data) {
				BILLING.build_table(data, hide_detail);
			}
		});
	},
	build_table:function(data, hide_detail) {
		var output = "";
		if(data.length > 0) {
			for(var i=0; i < data.length; i++) {
				var row = data[i];
				if(i == 0) $(BILLING.date_range).html(row.billingPeriod);
				var row_class = "even";
				if (i % 2 != 0) row_class = "odd";
				output += BILLING.build_result_rows(row, row_class, hide_detail);
			}
			$(BILLING.options_menu_btn).show();
		} else {
			output = "<tr><td colspan=\"13\" class=\"no-results\">No results returned.</td></tr>";
			$(BILLING.options_menu_btn).hide();
		}
		
		$(BILLING.select_all).prop("checked", false);
		$(BILLING.results_table + " tbody").html(output);
	},
	build_result_rows:function(row, row_class, hide_detail) {
		var output = "";
		var customer_name = row.customer.name;
		//var eng_mgr = row.contract.engagementManager;
		var job_number = row.contract.jobNumber;
		var contract_id = row.contract.id;
		var contract_invoice = row.contractInvoice;
		//if(eng_mgr == null) eng_mgr = "";
		if(job_number == null) job_number = "";
		var sdms = row.contract.serviceDeliveryManagers;
		var sdm_display = "";
		if(sdms != null && sdms.length > 0) {
			sdms.forEach(sdm => {
				if(sdm_display != "") sdm_display += ", ";
				sdm_display += sdm.userName;
			});
		}
		
		BILLING.is_first_row = true;
			
		if(row.previousMonth.length > 0 || row.previousMonthAdjustments.length > 0) {
			output += BILLING.build_previous_month_row(customer_name, sdm_display, job_number, row.formmattedPreviousMonthTotalOnetime, row.formattedPreviousMonthTotalRecurring, row_class, contract_invoice, contract_id, hide_detail);
		} else {
			BILLING.is_first_row = false;
		} 
			
		if(!hide_detail) {
			for(var k=0; k < row.removed.length; k++) {
				var changed_row = row.removed[k];
				output += BILLING.build_row(customer_name, sdm_display, job_number, changed_row, row_class, "removed", contract_invoice);
			}
			
			for(var k=0; k < row.removedAdjustments.length; k++) {
				var changed_adjustment = row.removedAdjustments[k];
				output += BILLING.build_adjustment_row(customer_name, sdm_display, job_number, changed_adjustment, row_class, "removed", contract_invoice);
			}
			
			for(var k=0; k < row.added.length; k++) {
				var changed_row = row.added[k];
				output += BILLING.build_row(customer_name, sdm_display, job_number, changed_row, row_class, "added", contract_invoice);
			}
			
			for(var k=0; k < row.addedAdjustments.length; k++) {
				var changed_adjustment = row.addedAdjustments[k];
				output += BILLING.build_adjustment_row(customer_name, sdm_display, job_number, changed_adjustment, row_class, "added", contract_invoice);
			}
		} else if(hide_detail && !BILLING.is_first_row) {
			output += BILLING.build_first_row_without_detail(customer_name, sdm_display, job_number, row_class, contract_invoice, contract_id);
			BILLING.is_first_row = false;
		}
		
		output += BILLING.build_total_row(row.formattedTotalOnetime, row.formattedTotalRecurring, row_class, contract_invoice);
		
		return output;
	},
	build_previous_month_row:function(customer_name, eng_mgr, job_number, onetime_revenue, recurring_revenue, row_class, contract_invoice, contract_id, hide_detail) {
		var output = "<tr class=\"" + row_class + "\">";
		var checkbox = BILLING.build_action_checkbox(contract_invoice);
		output += "<td>" + checkbox+ "</td>";
        output += "<td>" + customer_name + "</td>";
        output += "<td>" + eng_mgr + "</td>";
        output += "<td><a href=\"" + PAGE_CONSTANTS.BASE_URL + "contracts/" + contract_id +  "\">" + job_number + "</a></td>";
        if(hide_detail) {
        	output += "<td colspan=\"3\">Current Month's Totals (w/o Detail)</td>";
        	output += "<td>&nbsp;</td>";
        	output += "<td>&nbsp;</td>";
        	output += "<td>&nbsp;</td>";
        	output += "<td>&nbsp;</td>";
        	output += "<td>&nbsp;</td>";
        } else {
	        output += "<td>Previous Month MRC</td>";
	        output += "<td>Billing Amount from Previous Month</td>";
	        output += "<td></td>";
	        output += "<td class=\"right\"></td>";
	        output += "<td class=\"right\"></td>";
	        output += "<td class=\"right\"></td>";
	        output += "<td class=\"right\">" + UTIL.convert_currency_for_ui(onetime_revenue) + "</td>";
	        output += "<td class=\"right\">" + UTIL.convert_currency_for_ui(recurring_revenue) + "</td>";
        }
        output += "<td></td>";
		output += "</tr>";
		return output;
	},
	build_first_row_without_detail:function(customer_name, eng_mgr, job_number, row_class, contract_invoice, contract_id) {
		var output = "<tr class=\"" + row_class + "\">";
		var checkbox = BILLING.build_action_checkbox(contract_invoice);
		output += "<td>" + checkbox+ "</td>";
        output += "<td>" + customer_name + "</td>";
        output += "<td>" + eng_mgr + "</td>";
        output += "<td><a href=\"" + PAGE_CONSTANTS.BASE_URL + "contracts/" + contract_id +  "\">" + job_number + "</a></td>";
        output += "<td colspan=\"3\">Current Month's Totals (w/o Detail)</td>";
        output += "<td>&nbsp;</td>";
        output += "<td>&nbsp;</td>";
        output += "<td>&nbsp;</td>";
        output += "<td>&nbsp;</td>";
        output += "<td>&nbsp;</td>";
        output += "<td></td>";
		output += "</tr>";
		return output;
	},
	build_row:function(customer_name, eng_mgr, job_number, row, row_class, add_remove_class, contract_invoice) {
		var output = "<tr class=\"" + row_class + " " + add_remove_class + "\">";
		var checkbox = "";
		if(!BILLING.is_first_row) {
			checkbox = BILLING.build_action_checkbox(contract_invoice);
			BILLING.is_first_row = true;
		} else {
			customer_name = "", eng_mgr = "", job_number = "";
		}
		output += "<td>" + checkbox + "</td>";
        output += "<td>" + customer_name + "</td>";
        output += "<td>" + eng_mgr + "</td>";
        output += "<td>" + job_number + "</td>";
        output += "<td>" + row.name + "</td>";
        output += "<td>" + row.deviceDescription + "</td>";
        output += "<td>" + BILLING.build_pcr(row.contractUpdates) + "</td>";
        output += "<td class=\"right\">" + row.quantity + "</td>";
        output += "<td class=\"right\">" + UTIL.convert_dates_for_ui(row.startDate); + "</td>";
        output += "<td class=\"right\">" + UTIL.convert_dates_for_ui(row.endDate); + "</td>";
        output += "<td class=\"right\">" + UTIL.convert_currency_for_ui(row.formattedOnetimeRevenue) + "</td>";
        output += "<td class=\"right\">" + UTIL.convert_currency_for_ui(row.formattedRecurringRevenue) + "</td>";
        output += "<td></td>";
		output += "</tr>";
		return output;
	},
	build_adjustment_row:function(customer_name, eng_mgr, job_number, adjustment, row_class, add_remove_class, contract_invoice) {
		var output = "<tr class=\"" + row_class + " " + add_remove_class + "\">";
		var checkbox = "";
		if(!BILLING.is_first_row) {
			checkbox = BILLING.build_action_checkbox(contract_invoice);
			BILLING.is_first_row = true;
		} else {
			customer_name = "", eng_mgr = "", job_number = "";
		}
		output += "<td>" + checkbox + "</td>";
        output += "<td>" + customer_name + "</td>";
        output += "<td>" + eng_mgr + "</td>";
        output += "<td>" + job_number + "</td>";
        output += "<td>Contract Adjustment</td>";
        output += "<td></td>";
        output += "<td>" + BILLING.build_pcr(adjustment.contractUpdates) + "</td>";
        output += "<td class=\"right\"></td>";
        output += "<td class=\"right\">" + UTIL.convert_dates_for_ui(adjustment.startDate); + "</td>";
        output += "<td class=\"right\">" + UTIL.convert_dates_for_ui(adjustment.endDate); + "</td>";
        
        var amount = "";
        if(adjustment.adjustmentType == "onetime") amount = UTIL.convert_currency_for_ui(adjustment.formattedAdjustment);
        output += "<td class=\"right\">" + amount + "</td>";
        if(adjustment.adjustmentType == "recurring") {
        	amount = UTIL.convert_currency_for_ui(adjustment.formattedAdjustment);
        } else {
        	amount = "";
        }
        output += "<td class=\"right\">" + amount + "</td>";
        output += "<td></td>";
		output += "</tr>";
		return output;
	},
	build_total_row:function(onetime_revenue, recurring_revenue, row_class, contract_invoice) {
		var output = "<tr class=\"" + row_class + " billing-total-row\">";
		var status = "";
		if(contract_invoice) status = BILLING.build_status(contract_invoice.status)
		output += "<td></td>";
        output += "<td class=\"billing-status\" colspan=\"2\">" + status + "</td>";
        output += "<td colspan=\"6\" style=\"vertical-align:middle;\"><div style=\"height:1px; border-bottom:1px solid #ccc; margin-bottom:3px; vertical-align:bottom;\"></div></td>";
        output += "<td class=\"right\">Total</td>";
        output += "<td class=\"right\">" + UTIL.convert_currency_for_ui(onetime_revenue) + "</td>";
        output += "<td class=\"right\">" + UTIL.convert_currency_for_ui(recurring_revenue) + "</td>";
        output += "<td class=\"right\">" + UTIL.convert_currency_for_ui(onetime_revenue + recurring_revenue) + "</td>";
		output += "</tr>";
		return output;
	},
	build_pcr:function(pcrs) {
		var output = "";
		
		for(var i = 0; i < pcrs.length; i++) {
			if(i > 0) output += ", ";
			output += pcrs[i].altId;
		}
		
		return output;
	},
	build_action_checkbox:function(contract_invoice) {
		var output = "";
		if(contract_invoice && ((!BILLING.is_billing_role && contract_invoice.status != "invoiced") || BILLING.is_billing_role)) {
			output = "<input type=\"checkbox\" value=\"" + contract_invoice.id + "\" name=\"billing-action\" data-start-date=\"" + UTIL.convert_dates_for_ui(contract_invoice.startDate) + "\" data-end-date=\"" + UTIL.convert_dates_for_ui(contract_invoice.endDate) + "\" data-contract-id=\"" + contract_invoice.contractId + "\" />";
		}
		return output;
	},
	build_status:function(status) {
		var output = "";
		if(status == "readyToInvoice") {
			output = "<i class=\"fa fa-check\"></i>Ready To Invoice";
		} else if(status == "invoiced") {
			output = "<i class=\"fa fa-lock\"></i>Invoiced";
		}
		return output;
	},
	update_invoices:function(status) {
		$(BILLING.billing_success).hide();
		UTIL.clear_msg_in_content_box(BILLING.billing_error);
		
		//validate
		if($(BILLING.results_table + " tbody").find("input[type='checkbox']:checked").length == 0) {
			UTIL.add_error_msg_to_content_box(BILLING.billing_error, $(BILLING.msg_checkbox_required).val());
			$(BILLING.results_options).css("display","");
			return false;
		}
		
		$(BILLING.billing_loader).show();
		var json = [];
		$(BILLING.results_table + " tbody").find("input[type='checkbox']:checked").each(function() {
			var id = $(this).val();
			var start_date = $(this).data("start-date");
			var end_date = $(this).data("end-date");
			var contract_id = $(this).data("contract-id");
			start_date = UTIL.convert_dates_for_server(start_date);
			end_date = UTIL.convert_dates_for_server(end_date);
			
			json.push({ id:id, status:status, startDate:start_date, endDate:end_date, contractId:contract_id, operation:"update" });
		});
		
		$.ajax ({
			url: PAGE_CONSTANTS.BASE_URL + "contractinvoices.json?batch",
			data: JSON.stringify(json),
			type: "POST",
			success:function(data) {
				$(BILLING.billing_loader).hide();
				$(BILLING.results_options).css("display","");
				
				if(data.status == PAGE_CONSTANTS.OK_STS) {
					$(BILLING.billing_success).show();
					BILLING.update_rows(status);
					setTimeout("$(BILLING.billing_success).fadeOut(1500);", 3000);
				} else {
					UTIL.add_error_msg_to_content_box(BILLING.billing_error, data.message);
				}
			},
			error:function(jqXHR, textStatus, errorThrown) {
				$(BILLING.billing_loader).hide();
				$(BILLING.results_options).css("display","");
				var json = jqXHR.responseJSON;
				var message = $(msg_general_ajax_error).val();
				if(json && json.status && json.message) {
					var status = json.status;
					if (status == PAGE_CONSTANTS.ERRR_STS) {
						message = json.message;
					}
				}
				UTIL.add_error_msg_to_content_box(BILLING.billing_error, message);
			}
		});
	},
	update_rows:function(status) {
		var status_ui = BILLING.build_status(status);
		$(BILLING.results_table + " tbody").find("input[type='checkbox']:checked").each(function() {
			$(this).prop("checked", false);
			var $tr = $(this).parents("tr");
			var found = false;
			UTIL.flash_table_color($tr);
			do {
			    $tr = $tr.next();
			    UTIL.flash_table_color($tr);
			    if($tr.find(BILLING.billing_status).length > 0) found = true;
			}
			while (!found);
			
			$tr.find(BILLING.billing_status).html(status_ui);
		});
		$(BILLING.select_all).prop("checked", false);
	}
};

$(document).ready(function() {
	BILLING.init();
});