var PS = {
	pricing_sheet_id:null,	
	contract_id:null,
	contract_id_field: "#contract-id",
	pricing_product_dialog:"#pricing-product-dialog",
	delete_pricing_product_dialog:"#delete-pricing-product-dialog",
	init:function() {
		PS.set_variables();
		PS.bind_events();
		PS.read.init();
		PS.modify.init();
		PS.products.init();
		PS.M365.init();
		PS.M365NC.init();
	},
	bind_events:function() {
		
	},
	set_variables:function() {
		PS.contract_id = $(PS.contract_id_field).val();
	},
	read:{
		status_container:"#pricing-sheet-status",
		status:"#current-pricing-status",
		status_loader:"#pricing-status-loader",
		init:function() {
			PS.read.bind_events();
			PS.read.get_pricing_sheet();
			PS.read.get_devices();
		},
		bind_events:function() {
			
		},
		get_pricing_sheet:function() {
			UTIL.add_table_loader($(PS.products.pricing_sheet_container));
			
			$.ajax ({
				url: PAGE_CONSTANTS.BASE_URL + "pricingsheets/contract/" + PS.contract_id + ".json",
				type: "GET",
				success:function(data) {
					PS.pricing_sheet_id = data.id;
					var products = [];
					if(data.products) {
						products = data.products;
						PS.products.read.products = products;
					}
					PS.products.read.build_products(products);
					//PS.read.set_status(data.active);
				}
			});
		},
		get_devices:function() {
			$.ajax ({
				url: PAGE_CONSTANTS.BASE_URL + "pricingsheets/devices/" + PS.contract_id + ".json",
				type: "GET",
				success:function(data) {
					PS.read.build_devices(data);
				}
			});
		},
		build_devices:function(devices) {
			var output = "<option value=\"\"></option>";
			
			for(var i=0; i < devices.length; i++) {
				var device = devices[i];
				output += "<option value=\"" + device.id + "\" data-default-osp-id=\"" + device.defaultOspId + "\">" + device.description + " (" + device.partNumber + ") </option>";
			}
			
			$(PS.products.modify.device_field).html(output);
		},
		set_status:function(sync_enabled) {
			var output = "";
			if(sync_enabled) {
				output = "<i class=\"fa fa-check\"></i>Available in OSM<a href=\"javascript:;\" class=\"small-cta-btn update-pricing-sheet-sync\" data-enable=\"false\">Disable</a>";
			} else {
				output = "<i class=\"fa fa-times\"></i>Not Available in OSM<a href=\"javascript:;\" class=\"small-cta-btn update-pricing-sheet-sync\" data-enable=\"true\">Enable</a>";
			}
			$(PS.read.status).html(output);
		}
	},
	modify:{
		update_sync_link:".update-pricing-sheet-sync",
		init:function() {
			PS.modify.bind_events();
		},
		bind_events:function() {
			$(document).on("click", PS.modify.update_sync_link, function() {
				var enabled = $(this).data("enable");
				PS.modify.update_status(enabled);
			});
		},
		update_status:function(active) {
			var json = { id:PS.pricing_sheet_id, contractId:PS.contract_id, active:active };
			json = UTIL.remove_null_properties(json);
			
			$(PS.read.status_loader).show();
			$(PS.read.status).hide();
			
			$.ajax ({
				url: PAGE_CONSTANTS.BASE_URL + "pricingsheets.json",
				data: JSON.stringify(json),
				type: "PUT",
				success:function(data) {
					if(data.status == PAGE_CONSTANTS.OK_STS) {
						$(PS.read.status_loader).hide();
						//PS.read.set_status(sync_enabled);
						$(PS.read.status).show();
					} else {
						
					}
				}
			});
		}
	},
	products:{
		pricing_sheet_container:"#pricing-sheet",
		init:function() {
			PS.products.read.init();
			PS.products.modify.init();
			PS.products.generate.init();
			PS.products.del.init();
		},
		bind_events:function() {
			
		},
		read:{
			products:[],
			standard_count:"#pricing-sheet-standard-count",
			o365_count:"#pricing-sheet-o365-count",
			m365_count:"#pricing-sheet-m365-count",
			m365nc_count:"#pricing-sheet-m365nc-count",
			tab_class:".pricing-sheet-tab",
			active_tab:"standard",
			add_product_success_msg:"#add-pricing-sheet-product-success-msg",
			init:function() {
				PS.products.read.bind_events();
			},
			bind_events:function() {
				$(PS.products.read.tab_class).click(function() {
					var type = $(this).data("type");
					PS.products.read.active_tab = type;
					PS.products.read.build_products(PS.products.read.products);
					
				});
			},
			build_products:function(products) {
				var output = "";
				var standard_count = 0;
				var o365_count = 0;
				var m365_count = 0;
				var m365nc_count = 0;
				var filtered_products = [];
				
				for(var i = 0; i < products.length; i++) {
					var product = products[i];
					var device_type = product.deviceType;
					
					if(device_type == "cspO365") {
						o365_count++;
					} else if (device_type == "M365") {
						m365_count++;
					} else if(device_type == "M365NC") {
						m365nc_count++;
					} else {
						standard_count++;
					}
					
					if(PS.products.read.active_tab == "M365" && device_type == "M365") {
						filtered_products.push(product);
					} else if(PS.products.read.active_tab == "O365" && device_type == "cspO365") {
						filtered_products.push(product);
					} else if(PS.products.read.active_tab == "M365NC" && device_type == "M365NC") {
						filtered_products.push(product);
					} else if(PS.products.read.active_tab == "standard" && device_type != "M365" && device_type != "cspO365") {
						filtered_products.push(product);
					}
				}
				
				if(filtered_products && filtered_products.length > 0) {
					for(var i = 0; i < filtered_products.length; i++) {
						var product = filtered_products[i];
						var service_name = product.serviceName;
						var device_type = product.deviceType;
						if(product.status == "error") {
							service_name = "<i class=\"fa  fa-exclamation-triangle\"></i>" + service_name;
						}
						
						
						
						output += "<tr>";
						if(device_type == "M365") {
							output += "<td><a href=\"javascript:;\" class=\"popup-link add-m365-product\" data-dialog=\"add-m365-pricing-product-dialog\" data-id=\"" + product.id + "\">" + service_name + "</a></td>";
						} else {
							output += "<td><a href=\"javascript:;\" class=\"popup-link update-pricing-products\" data-dialog=\"pricing-product-dialog\" data-id=\"" + product.id + "\">" + service_name + "</a></td>";
						}
						output += "<td>" + product.devicePartNumber + "</td>";
						output += "<td>" + product.deviceDescription + "</td>";
						output += "<td class=\"right\">" + UTIL.convert_currency_for_ui(product.onetimePrice) + "</td>";
						output += "<td class=\"right\">" + UTIL.convert_currency_for_ui(product.recurringPrice) + "</td>";
						output += "<td class=\"right\">" + UTIL.convert_currency_for_ui(product.removalPrice) + "</td>";
						output += "<td class=\"center\"><a href=\"javascript:;\" class=\"popup-link delete-pricing-product icon-link\" data-dialog=\"delete-pricing-product-dialog\" data-name=\"" + product.deviceDescription + " (" + product.devicePartNumber + ")\" data-id=\"" + product.id + "\"><i class=\"fa fa-minus-circle\"></i>Delete</a></td>"
						output += "</tr>";
					}
				} else {
					output = "<tr><td class=\"no-results\">There are no products in this pricing sheet.</td></tr>";
				}
				
				$(PS.products.pricing_sheet_container + " tbody").html(output);
				$(PS.products.read.standard_count).html(" (" + standard_count + ")");
				$(PS.products.read.o365_count).html(" (" + o365_count + ")");
				$(PS.products.read.m365_count).html(" (" + m365_count + ")");
				$(PS.products.read.m365nc_count).html(" (" + m365nc_count + ")");
			}
		},
		modify:{
			//service_field:"#pricing-product-service",
			device_field:"#pricing-product-device",
			device_part_number_field:"#pricing-product-part-number",
			device_description_field:"#pricing-product-description",
			device_display:"#pricing-product-device-display",
			onetime_price_field:"#pricing-product-onetime-price",
			recurring_price_field:"#pricing-product-recurring-price",
			removal_price_field:"#pricing-product-removal-price",
			manual_override_field:"#pricing-product-manual-override",
			popup_class:".update-pricing-products",
			product_already_exists_msg:"#validate-pricing-sheet-product-already-exists-msg",
			general_error_msg:"#general-error-msg",
			update_product_success_msg:"#update-pricing-sheet-product-success-msg",
			generate_status_msg:"#pricing-sheet-status-message",
			o365_products:[],
			current_id:null,
			current_status:null,
			init:function() {
				PS.products.modify.bind_events();
			},
			bind_events:function() {
				$(PS.pricing_product_dialog).dialog({
					  autoOpen:false,
					  dialogClass:"contract-dialog",	
				      resizable:false,
				      width:850,
				      height:500,
				      modal:true,
				      title: "Add/Edit a Pricing Sheet Product",
				      open:function() {
				    	  $('.datepicker').datepicker('enable');
				    	  $(this).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").show();
				    	  $(this).closest(".ui-dialog").find(".ui-button:contains('OK')").hide();
				    	  $(this).closest(".ui-dialog").find(".ui-button:contains('Add Another')").hide();
				      },
				      close:function() {
				    	  $('.datepicker').datepicker('disable');
				      },
				      buttons: {
				        "Save":function() {
				        	PS.products.modify.submit_product();
				        },
				        "Cancel":function() {
				            $(this).dialog("close");
				        },
				        "Add Another":function() {
				        	$(this).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").show();
					    	$(this).closest(".ui-dialog").find(".ui-button:contains('OK')").hide();
					    	$(this).closest(".ui-dialog").find(".ui-button:contains('Add Another')").hide();
				        	PS.products.modify.current_id = null;
				        	PS.products.modify.reset_popup();
				        },
				        "OK":function() {
				            $(this).dialog("close");
				        }
				      },
				      create:function () {
				    	  $(this).closest(".ui-dialog").find(".ui-button:contains('Cancel')").addClass("cancel");
				    	  $(this).closest(".ui-dialog").find(".ui-button:contains('OK')").hide();
				    	  $(this).closest(".ui-dialog").find(".ui-button:contains('Add Another')").hide();
				      }
				});
				
				$(document).on("click", PS.products.modify.popup_class, function() {
					var id = $(this).data("id");
					(id == "add") ? PS.products.modify.current_id = null : PS.products.modify.current_id = id;
					PS.products.modify.setup_autocompletes();
					PS.products.modify.reset_popup();
				});
				
				$(PS.products.modify.device_field).change(function() {
					var default_osp_id = $(this).find("option:selected").data("default-osp-id");
					if(!default_osp_id) default_osp_id = "";
					var id = $(PS.products.modify.service_field).find(".osp-id-" + default_osp_id).val();
					$(PS.products.modify.service_field).val(id);
				});
			},
			setup_autocompletes:function(type) {
				if(PS.products.modify.o365_products.length == 0) {
					var devices = CONTRACT.contract_services.devices;
					var o365_list = [];
					for(var i = 0; i < devices.length; i++) {
						var device = devices[i];
						if(device.deviceType == "cspO365") o365_list.push(device);
					}
					
					var codes = [], descriptions = [];
					for(var i = 0; i < o365_list.length; i++) {
						var code = o365_list[i].partNumber;
						var desc = o365_list[i].description;
						var alt_id = o365_list[i].altId;
						codes.push({ value:code, desc:desc, altId:alt_id });
						descriptions.push({ value:desc, desc:code, altId:alt_id });
					}
					
					PS.M365.create.bind_autocompletes(PS.products.modify.device_part_number_field, codes, PS.products.modify.device_description_field);
					PS.M365.create.bind_autocompletes(PS.products.modify.device_description_field, descriptions, PS.products.modify.device_part_number_field);
					
					PS.products.modify.o365_products = o365_list;
				}
			},
			bind_autocompletes:function(obj_id, codes, chained_field_id) {
				$(obj_id).autocomplete({ 
					source: codes,
					focus: function( event, ui ) {
				        $(obj_id).val(ui.item.value);
				        return false;
				      },
				      select: function( event, ui ) {
				        $(obj_id).val(ui.item.value);
				        $(chained_field_id).val(ui.item.desc);
				        
				        return false;
				      }
				}).data("uiAutocomplete")._renderItem = function( ul, item ) {
					return $("<li>").append("<a>" + item.value + "<span>" + item.desc + "</span></a>").appendTo(ul);
				};
			},
			reset_popup:function() {
				UTIL.clear_message_in_popup(PS.pricing_product_dialog);
				PS.products.modify.current_status = "active";
				
				//var service_id = "";
				var device_id = "";
				var device_part_number = "";
				var device_description = "";
				var onetime_price = "";
				var recurring_price = "";
				var error = false;
				var removal_price = "";
				var device_display = "";
				var auto_update = "true";
				
				if(PS.products.modify.current_id) {
					var product = $.grep(PS.products.read.products, function(n) { return n.id == PS.products.modify.current_id; });
					product = product[0];
				
					if(product) {
						//service_id = product.serviceId;
						device_id = product.deviceId;
						device_part_number = product.devicePartNumber;
						device_description = product.deviceDescription;
						onetime_price = product.onetimePrice;
						recurring_price = product.recurringPrice;
						device_display = product.deviceDescription + " (" + product.devicePartNumber + ")";
						if(product.manualOverride) {
							auto_update = "false";
						}
						removal_price = product.removalPrice;
						
						onetime_price = UTIL.convert_currency_for_server(onetime_price);
						recurring_price = UTIL.convert_currency_for_server(recurring_price);
						removal_price = UTIL.convert_currency_for_server(removal_price);
						
						if(product.status == "error") {
							$(PS.products.modify.generate_status_msg).html(product.statusMessage);
							PS.products.modify.current_status = "error";
							error = true;
						} else {
							$(PS.products.modify.generate_status_msg).html("");
						}
						
						$(PS.products.modify.device_part_number_field).prop("readonly",true);
						$(PS.products.modify.device_description_field).prop("readonly",true);
					}
				} else {
					auto_update = "false";
					$(PS.products.modify.manual_override_field).prop("readonly", true);
					$(PS.products.modify.device_part_number_field).prop("readonly",false);
					$(PS.products.modify.device_description_field).prop("readonly",false);
				}
				
				$(PS.products.modify.device_display).html(device_display);
				$(PS.products.modify.device_field).val(device_id);
				$(PS.products.modify.onetime_price_field).val(onetime_price);
				$(PS.products.modify.recurring_price_field).val(recurring_price);
				$(PS.products.modify.manual_override_field).val(auto_update);
				$(PS.products.modify.removal_price_field).val(removal_price);
				$(PS.products.modify.device_part_number_field).val(device_part_number);
				$(PS.products.modify.device_description_field).val(device_description);
				//$(PS.products.modify.service_field).val(service_id);
				if(error) {
					$(PS.products.modify.generate_status_msg).show();
				} else {
					$(PS.products.modify.generate_status_msg).hide();
				}
				
			},
			submit_product:function() {
				UTIL.clear_message_in_popup(PS.pricing_product_dialog);
				
				//var service_id = $(PS.products.modify.service_field).val();
				var device_id = $(PS.products.modify.device_field).val();
				var device_part_number = $(PS.products.modify.device_part_number_field).val();
				var device_description = $(PS.products.modify.device_description_field).val();
				var onetime_price = $(PS.products.modify.onetime_price_field).val();
				var recurring_price = $(PS.products.modify.recurring_price_field).val();
				var manual_override = "false";
				var removal_price = $(PS.products.modify.removal_price_field).val();
				//var removal_price = "0.00";
				var ajax_type = "POST";
				
				if($(PS.products.modify.manual_override_field).val() == "false") {
					manual_override = "true";
					PS.products.modify.current_status = "active";
				}
				
				if(!device_id) {
					var devices = PS.products.modify.o365_products;
					for(var i = 0; i < devices.length; i++) {
						var device = devices[i];
						if(device.partNumber == device_part_number && device.description == device_description) {
							device_id = device.id;
							break;
						}
					}
				}
				
				if(!device_id || !device_part_number || !device_description || !onetime_price || !recurring_price || !removal_price) {
					UTIL.add_error_message_to_popup(PS.pricing_product_dialog, $(PS.products.modify.general_error_msg).val());
					return false;
				}
				
				var exists = $.grep(PS.products.read.products, function(n) { return n.deviceId == device_id; });
				if(exists.length > 0 && PS.products.modify.current_id == null) {
					UTIL.add_error_message_to_popup(PS.pricing_product_dialog, $(PS.products.modify.product_already_exists_msg).val());
					return false;
				}
				
				
				onetime_price = UTIL.convert_currency_for_server(onetime_price);
				recurring_price = UTIL.convert_currency_for_server(recurring_price);
				removal_price = UTIL.convert_currency_for_server(removal_price);
				var json = { pricingSheetId:PS.pricing_sheet_id, deviceId:device_id, onetimePrice:onetime_price, recurringPrice:recurring_price, removalPrice:removal_price, status:PS.products.modify.current_status, manualOverride:manual_override };
				
				if(PS.products.modify.current_id) {
					json["id"] = PS.products.modify.current_id;
					ajax_type = "PUT";
				}
				
				json = UTIL.remove_null_properties(json);
				UTIL.add_dialog_loader(PS.pricing_product_dialog);
				
				$.ajax ({
					url: PAGE_CONSTANTS.BASE_URL + "pricingsheets/products.json",
					data: JSON.stringify(json),
					type: ajax_type,
					success:function(data) {
						UTIL.remove_dialog_loader(PS.pricing_product_dialog);
						
						if(data.status == PAGE_CONSTANTS.OK_STS) {
							$(PS.pricing_product_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").hide();
							$(PS.pricing_product_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button:contains('OK')").show();
							
							PS.read.get_pricing_sheet();
							
							if(PS.products.modify.current_id == null) {
								UTIL.add_success_message_to_popup(PS.pricing_product_dialog, $(PS.products.read.add_product_success_msg).val());
								$(PS.pricing_product_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button:contains('Add Another')").show();
							} else {
								UTIL.add_success_message_to_popup(PS.pricing_product_dialog, $(PS.products.modify.update_product_success_msg).val());
							}
						} else {
							UTIL.add_error_message_to_popup(PS.pricing_product_dialog, data.message);
						}
					}
				});
				
			}
		},
		generate:{
			generate_sheet_btn:"#generate-sheet",
			init:function() {
				PS.products.generate.bind_events();
			},
			bind_events:function() {
				$(PS.products.generate.generate_sheet_btn).click(function() {
					PS.products.generate.generate_products();
				});
			},
			generate_products:function() {
				UTIL.add_table_loader($(PS.products.pricing_sheet_container));
				
				var json = {};
				
				$.ajax ({
					url: PAGE_CONSTANTS.BASE_URL + "pricingsheets/products/generate/" + PS.contract_id + ".json",
					data: JSON.stringify(json),
					type: "POST",
					success:function(data) {
						if(data.status == PAGE_CONSTANTS.OK_STS) {
							PS.read.get_pricing_sheet();
						} else {
							UTIL.add_error_message_to_popup(PS.pricing_product_dialog, data.message);
						}
					}
				});
			}
		},
		del:{
			popup_class:".delete-pricing-product",
			name_field:"#delete-pricing-product-name",
			delete_product_success_msg:"#delete-pricing-sheet-product-success-msg",
			current_id:null,
			init:function() {
				PS.products.del.bind_events();
			},
			bind_events:function() {
				$(PS.delete_pricing_product_dialog).dialog({
					  autoOpen:false,
					  dialogClass:"delete-dialog",	
				      resizable:false,
				      width:720,
				      height:400,
				      modal:true,
				      title: "Delete a Pricing Sheet Product",
				      open:function() {
				    	  $('.datepicker').datepicker('enable');
				    	  $(this).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").show();
					      $(this).closest(".ui-dialog").find(".ui-button:contains('OK')").hide();
				      },
				      close:function() {
				    	  $('.datepicker').datepicker('disable');
				      },
				      buttons: {
				        "Delete":function() {
				        	PS.products.del.submit_delete();
				        },
				        "Cancel":function() {
				            $(this).dialog("close");
				        },
				        "OK":function() {
				            $(this).dialog("close");
				        }
				      },
				      create:function () {
				    	  $(this).closest(".ui-dialog").find(".ui-button:contains('Cancel')").addClass("cancel");
				    	  $(this).closest(".ui-dialog").find(".ui-button:contains('OK')").hide();
				      }
				});
				
				$(document).on("click", PS.products.del.popup_class, function() {
					var id = $(this).data("id");
					var name = $(this).data("name");
					(id == "add") ? PS.products.del.current_id = null : PS.products.del.current_id = id;
					PS.products.del.reset_popup(name);
				});
			},
			reset_popup:function(name) {
				UTIL.clear_message_in_popup(PS.delete_pricing_product_dialog);
				
				$(PS.products.del.name_field).html(name);
			},
			submit_delete:function() {
				UTIL.clear_message_in_popup(PS.delete_pricing_product_dialog);
				
				UTIL.add_dialog_loader(PS.delete_pricing_product_dialog);
				$.ajax ({
					url: PAGE_CONSTANTS.BASE_URL + "pricingsheets/products/" + PS.products.del.current_id + ".json",
					type: "DELETE",
					success:function(data) {
						UTIL.remove_dialog_loader(PS.delete_pricing_product_dialog);
						
						if(data.status == PAGE_CONSTANTS.OK_STS) {
							UTIL.add_success_message_to_popup(PS.delete_pricing_product_dialog, $(PS.products.del.delete_product_success_msg).val());
							$(PS.delete_pricing_product_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").hide();
							$(PS.delete_pricing_product_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button:contains('OK')").show();
							
							PS.read.get_pricing_sheet();
						} else {
							UTIL.add_error_message_to_popup(PS.delete_pricing_product_dialog, data.message);
						}
					}
				});
			}
		}
	},
	M365:{
		init:function() {
			PS.M365.bind_events();
			PS.M365.create.init();
			PS.M365.modify.init();
		},
		bind_events:function() {
			
		},
		create:{
			add_product_dialog:"#add-m365-pricing-product-dialog",
			part_number_field:"#m365-pricing-product-part-number",
			description_field:"#m365-pricing-product-description",
			discount_field:"#pricing-product-discount",
			unit_count_field:"#pricing-product-unit-count",
			erp_price:"#pricing-product-erp-price",
			discounted_price:"#pricing-product-discounted-price",
			add_m365_product_link:".add-m365-product",
			products:[],
			microsoft_price_list:null,
			current_id:null,
			current_alt_id:null,
			init:function() {
				PS.M365.create.bind_events();
				PS.M365.create.get_microsoft_price_list("M365");
			},
			bind_events:function() {
				$(PS.M365.create.add_product_dialog).dialog({
					  autoOpen:false,
					  dialogClass:"delete-dialog",	
				      resizable:false,
				      width:920,
				      height:600,
				      modal:true,
				      title: "Add an M365 Pricing Sheet Product",
				      open:function() {
				    	  $('.datepicker').datepicker('enable');
				    	  $(this).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").show();
					      $(this).closest(".ui-dialog").find(".ui-button:contains('OK')").hide();
				      },
				      close:function() {
				    	  $('.datepicker').datepicker('disable');
				      },
				      buttons: {
				        "Save":function() {
				        	PS.M365.create.submit_product();
				        },
				        "Cancel":function() {
				            $(this).dialog("close");
				        },
				        "OK":function() {
				            $(this).dialog("close");
				        }
				      },
				      create:function () {
				    	  $(this).closest(".ui-dialog").find(".ui-button:contains('Cancel')").addClass("cancel");
				    	  $(this).closest(".ui-dialog").find(".ui-button:contains('OK')").hide();
				      }
				});
				
				$(document).on("click", PS.M365.create.add_m365_product_link, function() {
					var id = $(this).data("id");
					(id == "add") ? PS.M365.create.current_id = null : PS.M365.create.current_id = id; 
					PS.M365.create.setup_autocompletes();
					PS.M365.create.reset_popup();
				});
				
				$(document).on("input", PS.M365.create.discount_field, function() {
					PS.M365.create.set_microsoft_price();
				});
			},
			reset_popup:function() {
				UTIL.clear_message_in_popup(PS.M365.create.add_product_dialog);
				
				var discount = "";
				var part_number = "";
				var description = "";
				var erp_price = "";
				var discounted_price = "";
				var device_alt_id = null;
				var unit_count = 0;
				
				if(PS.M365.create.current_id) {
					var product = $.grep(PS.products.read.products, function(n) { return n.id == PS.M365.create.current_id; });
					product = product[0];
				
					if(product) {
						//service_id = product.serviceId;
						discount = product.discount;
						part_number = product.devicePartNumber;
						description = product.deviceDescription;
						erp_price = product.erpPrice;
						discount = product.discount;
						device_alt_id = product.deviceAltId;
						unit_count = product.unitCount;
					}
					$(PS.M365.create.part_number_field).prop("readonly", true);
					$(PS.M365.create.description_field).prop("readonly", true);
				} else {
					$(PS.M365.create.part_number_field).prop("readonly", false);
					$(PS.M365.create.description_field).prop("readonly", false);
				}
				
				$(PS.M365.create.discount_field).val(discount);
				$(PS.M365.create.part_number_field).val(part_number);
				$(PS.M365.create.description_field).val(description);
				$(PS.M365.create.unit_count_field).val(unit_count);
				
				PS.M365.create.current_alt_id = device_alt_id;
				PS.M365.create.set_microsoft_price();
			},
			setup_autocompletes:function(type) {
				if(PS.M365.create.products.length == 0) {
					var devices = CONTRACT.contract_services.devices;
					var m365_list = [];
					for(var i = 0; i < devices.length; i++) {
						var device = devices[i];
						if(device.deviceType == "M365") m365_list.push(device);
					}
					
					var codes = [], descriptions = [];
					for(var i = 0; i < m365_list.length; i++) {
						var code = m365_list[i].partNumber;
						var desc = m365_list[i].description;
						var alt_id = m365_list[i].altId;
						codes.push({ value:code, desc:desc, altId:alt_id });
						descriptions.push({ value:desc, desc:code, altId:alt_id });
					}
					
					PS.M365.create.bind_autocompletes(PS.M365.create.part_number_field, codes, PS.M365.create.description_field);
					PS.M365.create.bind_autocompletes(PS.M365.create.description_field, descriptions, PS.M365.create.part_number_field);
					
					PS.M365.create.products = m365_list;
				}
			},
			bind_autocompletes:function(obj_id, codes, chained_field_id) {
				$(obj_id).autocomplete({ 
					source: codes,
					focus: function( event, ui ) {
				        $(obj_id).val(ui.item.value);
				        return false;
				      },
				      select: function( event, ui ) {
				        $(obj_id).val(ui.item.value);
				        $(chained_field_id).val(ui.item.desc);
				        PS.M365.create.current_alt_id = ui.item.altId;
				        PS.M365.create.set_microsoft_price();
				        
				        return false;
				      }
				}).data("uiAutocomplete")._renderItem = function( ul, item ) {
					return $("<li>").append("<a>" + item.value + "<span>" + item.desc + "</span></a>").appendTo(ul);
				};
			},
			get_microsoft_price_list:function(type) {
				$.ajax ({
					url: PAGE_CONSTANTS.BASE_URL + "pricingsheets/microsoft/latest.json?type=" + type,
					type: "GET",
					success:function(data) {
						PS.M365.create.microsoft_price_list = data;
					}
				});
			},
			set_microsoft_price:function() {
				var erp_price = "";
				var discounted_price = "";
				var discount = $(PS.M365.create.discount_field).val();
				if(PS.M365.create.current_alt_id) {
					var product = $.grep(PS.M365.create.microsoft_price_list.products, function(n) { return n.offerId == PS.M365.create.current_alt_id; });
					if(product && product.length > 0) {
						product = product[0];
						erp_price = product.erpPrice;
						if(!discount) discount = 0;
						discount = (100 - discount) / 100;
						discounted_price = erp_price * discount;
					}
				} else {
					
				}
				
				$(PS.M365.create.erp_price).html(UTIL.convert_currency_for_ui(erp_price));
				$(PS.M365.create.discounted_price).html(UTIL.convert_currency_for_ui(discounted_price));
			},
			submit_product:function() {
				UTIL.clear_message_in_popup(PS.M365.create.add_product_dialog);
				
				var part_number = $(PS.M365.create.part_number_field).val();
				var description = $(PS.M365.create.description_field).val();
				var discount = $(PS.M365.create.discount_field).val();
				var erp_price = $(PS.M365.create.erp_price).html();
				var recurring_price = $(PS.M365.create.discounted_price).html();
				var unit_count = $(PS.M365.create.unit_count_field).val();
				var manual_override = "false";
				var onetime_price = 0;
				var removal_price = 0;
				var ajax_type = "POST";
				
				
				if(!part_number || !description || !discount) {
					UTIL.add_error_message_to_popup(PS.M365.create.add_product_dialog, $(PS.products.modify.general_error_msg).val());
					return false;
				}
				
				var exists = $.grep(PS.products.read.products, function(n) { return n.deviceId == device_id; });
				if(exists.length > 0 && PS.products.modify.current_id == null) {
					UTIL.add_error_message_to_popup(PS.pricing_product_dialog, $(PS.products.modify.product_already_exists_msg).val());
					return false;
				}
				
				var device_id = null;
				var devices = PS.M365.create.products;
				for(var i = 0; i < devices.length; i++) {
					var device = devices[i];
					if(device.partNumber == part_number && device.description == description) {
						device_id = device.id;
						break;
					}
				}
				
				erp_price = UTIL.convert_currency_for_server(erp_price);
				recurring_price = UTIL.convert_currency_for_server(recurring_price);
				onetime_price = UTIL.convert_currency_for_server(onetime_price);
				removal_price = UTIL.convert_currency_for_server(removal_price);
				var json = { pricingSheetId:PS.pricing_sheet_id, deviceId:device_id, onetimePrice:onetime_price, recurringPrice:recurring_price, removalPrice:removal_price, status:"active", manualOverride:manual_override, erpPrice:erp_price, discount:discount, unitCount:unit_count };
				
				if(PS.M365.create.current_id) {
					json["id"] = PS.M365.create.current_id;
					ajax_type = "PUT";
				}
				
				json = UTIL.remove_null_properties(json);
				UTIL.add_dialog_loader(PS.M365.create.add_product_dialog);
				
				$.ajax ({
					url: PAGE_CONSTANTS.BASE_URL + "pricingsheets/products.json",
					data: JSON.stringify(json),
					type: ajax_type,
					success:function(data) {
						UTIL.remove_dialog_loader(PS.M365.create.add_product_dialog);
						
						if(data.status == PAGE_CONSTANTS.OK_STS) {
							$(PS.M365.create.add_product_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").hide();
							$(PS.M365.create.add_product_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button:contains('OK')").show();
							
							PS.read.get_pricing_sheet();
							
							if(PS.products.modify.current_id == null) {
								UTIL.add_success_message_to_popup(PS.M365.create.add_product_dialog, $(PS.products.read.add_product_success_msg).val());
								$(PS.M365.create.add_product_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button:contains('Add Another')").show();
							} else {
								UTIL.add_success_message_to_popup(PS.M365.create.add_product_dialog, $(PS.products.modify.update_product_success_msg).val());
							}
						} else {
							UTIL.add_error_message_to_popup(PS.M365.create.add_product_dialog, data.message);
						}
					}
				});
				
			}
		},
		modify:{
			init:function() {
				PS.M365.modify.bind_events();
			},
			bind_events:function() {
				
			}
		}
	},
	M365NC:{
		init:function() {
			PS.M365NC.bind_events();
			PS.M365NC.create.init();
			PS.M365NC.modify.init();
		},
		bind_events:function() {
			
		},
		create:{
			add_product_dialog:"#add-m365nc-pricing-product-dialog",
			part_number_field:"#m365nc-pricing-product-part-number",
			description_field:"#m365nc-pricing-product-description",
			discount_field:"#m365nc-pricing-product-discount",
			unit_count_field:"#m365nc-pricing-product-unit-count",
			erp_price:"#m365nc-pricing-product-erp-price",
			discounted_price:"#m365nc-pricing-product-discounted-price",
			term_duration:"#m365nc-pricing-product-term-duration",
			billing_plan:"#m365nc-pricing-product-billing-plan",
			segment:"#m365nc-pricing-product-segment",
			add_m365nc_product_link:".add-m365nc-product",
			products:[],
			microsoft_price_list:null,
			current_id:null,
			current_alt_id:null,
			current_product:null,
			init:function() {
				PS.M365NC.create.bind_events();
				PS.M365NC.create.get_microsoft_price_list("M365NC");
			},
			bind_events:function() {
				$(PS.M365NC.create.add_product_dialog).dialog({
					  autoOpen:false,
					  dialogClass:"delete-dialog",	
				      resizable:false,
				      width:920,
				      height:600,
				      modal:true,
				      title: "Add an M365 Pricing Sheet Product",
				      open:function() {
				    	  $('.datepicker').datepicker('enable');
				    	  $(this).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").show();
					      $(this).closest(".ui-dialog").find(".ui-button:contains('OK')").hide();
				      },
				      close:function() {
				    	  $('.datepicker').datepicker('disable');
				      },
				      buttons: {
				        "Save":function() {
				        	PS.M365NC.create.submit_product();
				        },
				        "Cancel":function() {
				            $(this).dialog("close");
				        },
				        "OK":function() {
				            $(this).dialog("close");
				        }
				      },
				      create:function () {
				    	  $(this).closest(".ui-dialog").find(".ui-button:contains('Cancel')").addClass("cancel");
				    	  $(this).closest(".ui-dialog").find(".ui-button:contains('OK')").hide();
				      }
				});
				
				$(document).on("click", PS.M365NC.create.add_m365nc_product_link, function() {
					var id = $(this).data("id");
					(id == "add") ? PS.M365NC.create.current_id = null : PS.M365NC.create.current_id = id; 
					PS.M365NC.create.setup_autocompletes();
					PS.M365NC.create.reset_popup();
				});
				
				$(document).on("input", PS.M365NC.create.discount_field, function() {
					var offer_id = null, termDuration = null, billingPlan = null, segment = null;
					if(PS.M365NC.create.current_product) {
						offer_id = PS.M365NC.create.current_product.offerId;
						term_duration = PS.M365NC.create.current_product.termDuration;
						billing_plan = PS.M365NC.create.current_product.billingPlan;
						segment = PS.M365NC.create.current_product.segment;
					}
					PS.M365NC.create.set_microsoft_price(offer_id, term_duration, billing_plan, segment);
				});
			},
			reset_popup:function() {
				UTIL.clear_message_in_popup(PS.M365NC.create.add_product_dialog);
				
				var discount = "";
				var part_number = "";
				var description = "";
				var erp_price = "";
				var discounted_price = "";
				var device_alt_id = null;
				var unit_count = 0;
				
				if(PS.M365NC.create.current_id) {
					var product = $.grep(PS.products.read.products, function(n) { return n.id == PS.M365NC.create.current_id; });
					product = product[0];
				
					if(product) {
						//service_id = product.serviceId;
						discount = product.discount;
						part_number = product.devicePartNumber;
						description = product.deviceDescription;
						erp_price = product.erpPrice;
						discount = product.discount;
						device_alt_id = product.deviceAltId;
						unit_count = product.unitCount;
					}
					$(PS.M365NC.create.part_number_field).prop("readonly", true);
					$(PS.M365NC.create.description_field).prop("readonly", true);
				} else {
					$(PS.M365NC.create.part_number_field).prop("readonly", false);
					$(PS.M365NC.create.description_field).prop("readonly", false);
				}
				
				$(PS.M365NC.create.discount_field).val(discount);
				$(PS.M365NC.create.part_number_field).val(part_number);
				$(PS.M365NC.create.description_field).val(description);
				$(PS.M365NC.create.unit_count_field).val(unit_count);
				
				PS.M365NC.create.current_alt_id = device_alt_id;
				PS.M365NC.create.set_microsoft_price();
			},
			setup_autocompletes:function(type) {
				if(PS.M365NC.create.products.length == 0) {
					var devices = CONTRACT.contract_services.devices;
					var m365_list = [];
					for(var i = 0; i < devices.length; i++) {
						var device = devices[i];
						if(device.deviceType == "M365NC") m365_list.push(device);
					}
					
					var codes = [], descriptions = [];
					for(var i = 0; i < m365_list.length; i++) {
						var code = m365_list[i].partNumber;
						var desc = m365_list[i].description;
						var alt_id = m365_list[i].altId;
						var segment = m365_list[i].segment;
						var term_duration = m365_list[i].termDuration;
						var billing_plan = m365_list[i].billingPlan;
						codes.push({ value:code, desc:desc, altId:alt_id, segment:segment, termDuration:term_duration, billingPlan:billing_plan });
						descriptions.push({ value:desc, desc:code, altId:alt_id, segment:segment, termDuration:term_duration, billingPlan:billing_plan });
					}
					
					PS.M365NC.create.bind_autocompletes(PS.M365NC.create.part_number_field, codes, PS.M365NC.create.description_field);
					PS.M365NC.create.bind_autocompletes(PS.M365NC.create.description_field, descriptions, PS.M365NC.create.part_number_field);
					
					PS.M365NC.create.products = m365_list;
				}
			},
			bind_autocompletes:function(obj_id, codes, chained_field_id) {
				$(obj_id).autocomplete({ 
					source: codes,
					focus: function( event, ui ) {
				        $(obj_id).val(ui.item.value);
				        return false;
				      },
				      select: function( event, ui ) {
				        $(obj_id).val(ui.item.value);
				        $(chained_field_id).val(ui.item.desc);
				        PS.M365NC.create.current_alt_id = ui.item.altId;
				        $(PS.M365NC.create.term_duration).html(ui.item.termDuration);
				        $(PS.M365NC.create.segment).html(ui.item.segment);
				        $(PS.M365NC.create.billing_plan).html(ui.item.billingPlan);
				        
				        PS.M365NC.create.current_product = { offerId:ui.item.altId, termDuration:ui.item.termDuration, segment:ui.item.segment, billingPlan:ui.item.billingPlan };
				        
				        PS.M365NC.create.set_microsoft_price(PS.M365NC.create.current_product.offerId, PS.M365NC.create.current_product.termDuration, PS.M365NC.create.current_product.billingPlan, PS.M365NC.create.current_product.segment);
				        
				        return false;
				      }
				}).data("uiAutocomplete")._renderItem = function( ul, item ) {
					return $("<li>").append("<a>" + item.value + "<span>" + item.desc + "</span></a>").appendTo(ul);
				};
			},
			get_microsoft_price_list:function(type) {
				$.ajax ({
					url: PAGE_CONSTANTS.BASE_URL + "pricingsheets/microsoft/latest.json?type=" + type,
					type: "GET",
					success:function(data) {
						PS.M365NC.create.microsoft_price_list = data;
					}
				});
			},
			set_microsoft_price:function(offer_id, term_duration, billing_plan, segment) {
				var erp_price = "";
				var discounted_price = "";
				var discount = $(PS.M365NC.create.discount_field).val();
				if(offer_id) {
					var product = $.grep(PS.M365NC.create.microsoft_price_list.products, function(n) { return n.offerId == offer_id && n.segment == segment && n.billingPlan == billing_plan && n.termDuration == term_duration; });
					if(product && product.length > 0) {
						product = product[0];
						erp_price = product.monthlyErpPrice;
						if(!discount) discount = 0;
						discount = (100 - discount) / 100;
						discounted_price = erp_price * discount;
					}
				}
				
				$(PS.M365NC.create.erp_price).html(UTIL.convert_currency_for_ui(erp_price));
				$(PS.M365NC.create.discounted_price).html(UTIL.convert_currency_for_ui(discounted_price));
			},
			submit_product:function() {
				UTIL.clear_message_in_popup(PS.M365NC.create.add_product_dialog);
				
				var part_number = $(PS.M365NC.create.part_number_field).val();
				var description = $(PS.M365NC.create.description_field).val();
				var discount = $(PS.M365NC.create.discount_field).val();
				var erp_price = $(PS.M365NC.create.erp_price).html();
				var discounted_price = $(PS.M365NC.create.discounted_price).html();
				var unit_count = $(PS.M365NC.create.unit_count_field).val();
				var manual_override = "false";
				var onetime_price = 0;
				var recurring_price = 0;
				var removal_price = 0;
				var ajax_type = "POST";
				var billing_plan = "";
				
				
				if(!part_number || !description || !discount) {
					UTIL.add_error_message_to_popup(PS.M365NC.create.add_product_dialog, $(PS.products.modify.general_error_msg).val());
					return false;
				}
				
				var exists = $.grep(PS.products.read.products, function(n) { return n.deviceId == device_id; });
				if(exists.length > 0 && PS.products.modify.current_id == null) {
					UTIL.add_error_message_to_popup(PS.pricing_product_dialog, $(PS.products.modify.product_already_exists_msg).val());
					return false;
				}
				
				var device_id = null;
				var devices = PS.M365NC.create.products;
				for(var i = 0; i < devices.length; i++) {
					var device = devices[i];
					if(device.partNumber == part_number && device.description == description) {
						device_id = device.id;
						billing_plan = device.billingPlan;
						break;
					}
				}
				
				if(billing_plan == "annual") {
					onetime_price = discounted_price;
				} else {
					recurring_price = discounted_price;
				}
				
				erp_price = UTIL.convert_currency_for_server(erp_price);
				recurring_price = UTIL.convert_currency_for_server(recurring_price);
				onetime_price = UTIL.convert_currency_for_server(onetime_price);
				removal_price = UTIL.convert_currency_for_server(removal_price);
				var json = { pricingSheetId:PS.pricing_sheet_id, deviceId:device_id, onetimePrice:onetime_price, recurringPrice:recurring_price, removalPrice:removal_price, status:"active", manualOverride:manual_override, erpPrice:erp_price, discount:discount, unitCount:unit_count };
				
				if(PS.M365NC.create.current_id) {
					json["id"] = PS.M365NC.create.current_id;
					ajax_type = "PUT";
				}
				
				json = UTIL.remove_null_properties(json);
				UTIL.add_dialog_loader(PS.M365NC.create.add_product_dialog);
				
				$.ajax ({
					url: PAGE_CONSTANTS.BASE_URL + "pricingsheets/products.json",
					data: JSON.stringify(json),
					type: ajax_type,
					success:function(data) {
						UTIL.remove_dialog_loader(PS.M365NC.create.add_product_dialog);
						
						if(data.status == PAGE_CONSTANTS.OK_STS) {
							$(PS.M365NC.create.add_product_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button").hide();
							$(PS.M365NC.create.add_product_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button:contains('OK')").show();
							
							PS.read.get_pricing_sheet();
							
							if(PS.products.modify.current_id == null) {
								UTIL.add_success_message_to_popup(PS.M365NC.create.add_product_dialog, $(PS.products.read.add_product_success_msg).val());
								$(PS.M365NC.create.add_product_dialog).closest(".ui-dialog").find(".ui-dialog-buttonpane .ui-button:contains('Add Another')").show();
							} else {
								UTIL.add_success_message_to_popup(PS.M365NC.create.add_product_dialog, $(PS.products.modify.update_product_success_msg).val());
							}
						} else {
							UTIL.add_error_message_to_popup(PS.M365NC.create.add_product_dialog, data.message);
						}
					}
				});
				
			}
		},
		modify:{
			init:function() {
				PS.M365NC.modify.bind_events();
			},
			bind_events:function() {
				
			}
		}
	}
};

$(document).ready(function() {
	PS.init();
});