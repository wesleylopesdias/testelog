package com.logicalis.serviceinsight.web.view;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.springframework.context.MessageSource;

import com.logicalis.serviceinsight.data.Contract;
import com.logicalis.serviceinsight.data.ContractAdjustment;
import com.logicalis.serviceinsight.data.ContractServiceChangedConsolidatedWrapper;
import com.logicalis.serviceinsight.data.ContractUpdate;
import com.logicalis.serviceinsight.data.Customer;
import com.logicalis.serviceinsight.data.Device;
import com.logicalis.serviceinsight.data.Service;
import com.logicalis.serviceinsight.representation.SDMCustomerExportWrapper;
import com.logicalis.serviceinsight.service.ApplicationDataDaoService;
import com.logicalis.serviceinsight.service.ContractDaoService;

public class SDMCustomerExportDocumentBuilder extends BaseDocumentBuilder {

	private MessageSource messageSource;
    private Locale locale;
    private XSSFSheet sheet1;
    static final String TITLE_SHEET1 = "SDM Export";
    static final int HEADER_FACTOR = 3 * 256;
    private ContractDaoService contractDaoService;
    private ApplicationDataDaoService applicationDataDaoService;
    
    private static final String DEVICE_KEY_RESOURCES = "Resources";
    private static final String DEVICE_KEY_BACKUP_VAULTING = "Backup & Vaulting";
    private static final String DEVICE_KEY_SERVER = "Server";
    private static final String DEVICE_KEY_VDC = "VDC";
    private static final String DEVICE_KEY_NETWORK = "Network";
    private static final String DEVICE_KEY_LICENSES = "Licenses";
    private static final String DEVICE_KEY_MANAGEMENT = "Management";
    private static final String DEVICE_KEY_DR = "Disaster Recovery";
    private static final String DEVICE_KEY_OTHER = "Other";
    
    public SDMCustomerExportDocumentBuilder(MessageSource messageSource,
            Locale locale, ContractDaoService contractDaoService, ApplicationDataDaoService applicationDataDaoService) {
        this.messageSource = messageSource;
        this.locale = locale;
        this.contractDaoService = contractDaoService;
        this.applicationDataDaoService = applicationDataDaoService;
        workbook = new XSSFWorkbook();
        sheet1 = workbook.createSheet(TITLE_SHEET1);
        sheet1.setDefaultColumnWidth(30);
    }

    public XSSFWorkbook buildSDMCustomerExportSpreadsheet(SDMCustomerExportWrapper wrapper) {

        writeCustomerInfo(wrapper.getCustomer());
        writeContractInfo(wrapper.getContract());
        Integer rowidx = writeLineItems(8, wrapper);
        return this.workbook;
    }
	
    private void writeCustomerInfo(Customer customer) {
        CellStyle style = headerTableHeaderCellStyle();

        XSSFRow header = sheet1.createRow(1);
        header.setHeightInPoints((short) 30);

        String customerTitle = messageSource.getMessage("label_customer_title", null, locale);
        header.createCell(0).setCellValue(customerTitle);
        header.getCell(0).setCellStyle(style);

        header.createCell(1).setCellValue(customer.getName());
        header.getCell(1).setCellStyle(style);
        
        sheet1.setColumnWidth(0, HEADER_FACTOR * customerTitle.length());
        sheet1.setColumnWidth(1, HEADER_FACTOR * customer.getName().length());
    }

    private void writeContractInfo(Contract contract) {
        CellStyle style = headerTableBodyCellStyle();

        // SOW ID
        writeContractInfoRow(style, 2, messageSource.getMessage("ui_col_contract_id", null, locale), contract.getAltId());

        // SOW Signed Date
        writeContractInfoRow(style, 3, messageSource.getMessage("ui_col_signed_date", null, locale),
                DateTimeFormat.forPattern("MM/dd/yyyy").print(new DateTime(contract.getSignedDate())));

        // SOW Start Date
        writeContractInfoRow(style, 4, messageSource.getMessage("ui_col_start_date", null, locale),
                DateTimeFormat.forPattern("MM/dd/yyyy").print(new DateTime(contract.getStartDate())));

        // SOW End Date
        writeContractInfoRow(style, 5, messageSource.getMessage("ui_col_end_date", null, locale),
                DateTimeFormat.forPattern("MM/dd/yyyy").print(new DateTime(contract.getEndDate())));
    }
    
    private void writeContractInfoRow(CellStyle style, int rowidx, String label, String value) {
        XSSFRow row = sheet1.createRow(rowidx);
        row.setHeightInPoints((short) 16);
        
        int colidx = 0;
        row.createCell(colidx).setCellValue(label);
        row.getCell(colidx++).setCellStyle(style);

        row.createCell(colidx).setCellValue(value);
        row.getCell(colidx++).setCellStyle(style);
    }
    
    private Integer writeLineItemHeader(Integer rowidx, boolean isdetail) {
        
        // header row
        CellStyle style = headerRowCellStyle();
        XSSFRow header = sheet1.createRow(rowidx);
        header.setHeightInPoints((short) 16);

        int colidx = 0;
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_ci_name", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_part_description", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_service_quantity", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        /*
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_start_date", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_end_date", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        */
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_one_time_cost", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_mrc", null, locale));
        header.getCell(colidx).setCellStyle(style);
        
        return rowidx;
    }
    
    private Integer writeLineItems(Integer rowidx, SDMCustomerExportWrapper wrapper) {
        // baseline row
        CellStyle baselineStyle = cellStyle();
        CellStyle baselineCurrencyStyle = currencyStyle();
        XSSFRow row = sheet1.createRow(rowidx);
        
        for(Service service: wrapper.getServices()) {
        	try {
    			Device device = applicationDataDaoService.device(service.getDeviceId());
    			if(Device.DeviceType.businessService.equals(device.getDeviceType())) {
    				continue;
    			} else if(service.getParentId() == null) {
            		// header row
    				rowidx++;
    				rowidx = writeBlankRow(baselineStyle, rowidx);
    				rowidx++;
                    rowidx = writeLineItemHeader(rowidx, false);
            		rowidx++;
            		rowidx = writeContractRow(ciNameCellStyle(), cellStyle(), dateStyle(), currencyStyle(), rowidx, service);
            		
            		Map<String, List<Service>> serviceGroupings = new LinkedHashMap<String, List<Service>>();
            		//we prepopulate these so they sort in a specific order
            		serviceGroupings.put(DEVICE_KEY_RESOURCES, new ArrayList<Service>());
            		serviceGroupings.put(DEVICE_KEY_BACKUP_VAULTING, new ArrayList<Service>());
            		serviceGroupings.put(DEVICE_KEY_DR, new ArrayList<Service>());
            		serviceGroupings.put(DEVICE_KEY_MANAGEMENT, new ArrayList<Service>());
            		serviceGroupings.put(DEVICE_KEY_LICENSES, new ArrayList<Service>());
            		serviceGroupings.put(DEVICE_KEY_OTHER, new ArrayList<Service>());
            		
                    if(service.getRelatedLineItems() != null && service.getRelatedLineItems().size() > 0) {
                    	for(Service childService: service.getRelatedLineItems()) {
                    		serviceGroupings = addServiceToGrouping(serviceGroupings, childService);
                    		if(childService.getRelatedLineItems() != null && childService.getRelatedLineItems().size() > 0) {
                            	for(Service grandChildService: childService.getRelatedLineItems()) {
                            		serviceGroupings = addServiceToGrouping(serviceGroupings, grandChildService);
                                }
                        	}
                    	}
                    }
                    
                    BigDecimal ciOnetimePrice = service.getOnetimeRevenue();
    				BigDecimal ciRecurringPrice = service.getRecurringRevenue();
                    
                    Iterator it = serviceGroupings.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry)it.next();
                        String key = (String) pair.getKey();
                    	
                    	BigDecimal onetimePrice = new BigDecimal(0);
                    	BigDecimal recurringPrice = new BigDecimal(0);
                        List<Service> services = (List<Service>) pair.getValue();
                    	
                        if(services != null && services.size() > 0) {
                        	rowidx++;
                        	rowidx = writeBlankRow(cellStyle(), rowidx);
                        	rowidx++;
                        	rowidx = writeChildLineItemHeader(rowidx, key);
                        	
	                        for(Service childService: services) {
	                        	rowidx++;
	                        	rowidx = writeChildContractRow(ciNameCellStyle(), cellStyle(), dateStyle(), currencyStyle(), rowidx, childService);
	                        	
	                        	onetimePrice = onetimePrice.add(childService.getOnetimeRevenue());
	                        	recurringPrice = recurringPrice.add(childService.getRecurringRevenue());
	                        }
	                        
	                        rowidx++;
	                        rowidx = writeChildLineItemSubTotal(rowidx, onetimePrice, recurringPrice);
	                        
	                        ciOnetimePrice = ciOnetimePrice.add(onetimePrice);
	                        ciRecurringPrice = ciRecurringPrice.add(recurringPrice);
                        }
                        
                        it.remove(); // avoids a ConcurrentModificationException
                    }
            		
            		rowidx++;
            		rowidx = writeLineItemTotal(rowidx, ciOnetimePrice, ciRecurringPrice);
            	}
        	} catch (Exception e) {
        		log.error(e.getMessage());
        	}
        }
        
        if(wrapper.getAdjustments() != null && wrapper.getAdjustments().size() > 0) {
        	BigDecimal onetimeAdjustmentTotal = new BigDecimal(0);
        	BigDecimal recurringAdjustmentTotal = new BigDecimal(0);
        	
        	rowidx++;
			rowidx = writeBlankRow(baselineStyle, rowidx);
			rowidx++;
            rowidx = writeLineItemHeader(rowidx, false);
        	for(ContractAdjustment adjustment: wrapper.getAdjustments()) {
        		rowidx++;
        		rowidx = writeAdjustmentRow(ciNameCellStyle(), cellStyle(), dateStyle(), currencyStyle(), rowidx, adjustment);
        		if("onetime".equals(adjustment.getAdjustmentType())) {
        			onetimeAdjustmentTotal = onetimeAdjustmentTotal.add(adjustment.getAdjustment());
                } else {
                	recurringAdjustmentTotal = recurringAdjustmentTotal.add(adjustment.getAdjustment());
                }
	        }
        	
        	rowidx++;
    		rowidx = writeLineItemTotal(rowidx, onetimeAdjustmentTotal, recurringAdjustmentTotal);
        }
        
        return rowidx;
    }
    
    
    private Integer writeContractRow(CellStyle firstCellStyle, CellStyle style, CellStyle dateStyle, CellStyle currencyStyle, Integer rowidx, Service service) {
    	XSSFRow row = sheet1.createRow(rowidx);
        row.setHeightInPoints((short) 16);
        row.setRowStyle(style);
        
        int colidx = 0;
        row.createCell(colidx).setCellValue(service.getDeviceName());
        row.getCell(colidx++).setCellStyle(firstCellStyle);
        row.createCell(colidx).setCellValue(service.getDeviceDescription());
        row.getCell(colidx++).setCellStyle(style);
        
        Integer quantity = 1;
        if(service.getDeviceUnitCount() != null) quantity = service.getDeviceUnitCount();
        row.createCell(colidx).setCellValue(quantity);
        row.getCell(colidx++).setCellStyle(style);
        
        /*
        row.createCell(colidx).setCellValue(service.getStartDate());
        row.getCell(colidx++).setCellStyle(dateStyle);
        row.createCell(colidx).setCellValue(service.getEndDate());
        row.getCell(colidx++).setCellStyle(dateStyle);
        */
        
        BigDecimal onetimeRevenue = service.getFormattedOnetimeRevenue();
        BigDecimal recurringRevenue = service.getFormattedRecurringRevenue();		
        row.createCell(colidx).setCellValue(onetimeRevenue.doubleValue());
        row.getCell(colidx++).setCellStyle(currencyStyle);
        row.createCell(colidx).setCellValue(recurringRevenue.doubleValue());
        row.getCell(colidx).setCellStyle(currencyStyle);
        
        return rowidx;
    }
    
    private Integer writeAdjustmentRow(CellStyle firstCellStyle, CellStyle style, CellStyle dateStyle, CellStyle currencyStyle, Integer rowidx, ContractAdjustment adjustment) {
    	XSSFRow row = sheet1.createRow(rowidx);
        row.setHeightInPoints((short) 16);
        row.setRowStyle(style);
        
        int colidx = 0;
        row.createCell(colidx).setCellValue("");
        row.getCell(colidx++).setCellStyle(firstCellStyle);
        row.createCell(colidx).setCellValue("Contract Adjustment");
        row.getCell(colidx++).setCellStyle(style);
        
        Integer quantity = 1;
        row.createCell(colidx).setCellValue(quantity);
        row.getCell(colidx++).setCellStyle(style);
        
        /*
        row.createCell(colidx).setCellValue(adjustment.getStartDate());
        row.getCell(colidx++).setCellStyle(dateStyle);
        row.createCell(colidx).setCellValue(adjustment.getEndDate());
        row.getCell(colidx++).setCellStyle(dateStyle);
        */
        
        BigDecimal onetimeRevenue = new BigDecimal(0);
        BigDecimal recurringRevenue = new BigDecimal(0);
        
        if("onetime".equals(adjustment.getAdjustmentType())) {
        	onetimeRevenue = adjustment.getAdjustment();
        } else {
        	recurringRevenue = adjustment.getAdjustment();
        }
        
        row.createCell(colidx).setCellValue(onetimeRevenue.doubleValue());
        row.getCell(colidx++).setCellStyle(currencyStyle);
        row.createCell(colidx).setCellValue(recurringRevenue.doubleValue());
        row.getCell(colidx).setCellStyle(currencyStyle);
        
        return rowidx;
    }
    
    private Map<String, List<Service>> addServiceToGrouping(Map<String, List<Service>> serviceGroupings, Service service) {
    	try {
			Device device = applicationDataDaoService.device(service.getDeviceId());
			String deviceTypeKey = DEVICE_KEY_OTHER;
			Device.DeviceType deviceType = null;
			if(device.getDeviceType() != null) {
				deviceType = device.getDeviceType();
				
				deviceTypeKey = deviceType.getDescription();
				
				if(deviceType.equals(Device.DeviceType.backupStorage) || deviceType.equals(Device.DeviceType.vaultingStorage) || deviceType.equals(Device.DeviceType.backup) || deviceType.equals(Device.DeviceType.vaulting)) {
					deviceTypeKey = DEVICE_KEY_BACKUP_VAULTING;
				}
				
				if(deviceType.equals(Device.DeviceType.compute) || deviceType.equals(Device.DeviceType.memory) || deviceType.equals(Device.DeviceType.storage)) {
					deviceTypeKey = DEVICE_KEY_RESOURCES;
				}
				
				if(deviceType.equals(Device.DeviceType.spla) || deviceType.equals(Device.DeviceType.software)) {
					deviceTypeKey = DEVICE_KEY_LICENSES;
				}
				
				if(deviceType.equals(Device.DeviceType.drCompute) || deviceType.equals(Device.DeviceType.drMemory) || deviceType.equals(Device.DeviceType.drStorage) || deviceType.equals(Device.DeviceType.dr)) {
					deviceTypeKey = DEVICE_KEY_DR;
				}
			}
			
			if(serviceGroupings.get(deviceTypeKey) == null) {
				serviceGroupings.put(deviceTypeKey, new ArrayList<Service>());
			}
			serviceGroupings.get(deviceTypeKey).add(service);
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
    	return serviceGroupings;
    }
    
    private Integer writeChildLineItemHeader(Integer rowidx, String key) {
        CellStyle style = subHeaderRowCellStyle();
        XSSFRow header = sheet1.createRow(rowidx);
        header.setHeightInPoints((short) 16);

        int colidx = 0;
        header.createCell(colidx).setCellValue("");
        header.getCell(colidx++).setCellStyle(null);
        header.createCell(colidx).setCellValue(key);
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue("");
        header.getCell(colidx++).setCellStyle(style);
        /*
        header.createCell(colidx).setCellValue("");
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue("");
        header.getCell(colidx++).setCellStyle(style);
        */
        header.createCell(colidx).setCellValue("");
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue("");
        header.getCell(colidx).setCellStyle(style);
        
        return rowidx;
    }
    
    private Integer writeChildLineItemSubTotal(Integer rowidx, BigDecimal onetimePrice, BigDecimal recurringPrice) {
        CellStyle style = subTotalCellStyle();
        CellStyle currencyStyle = subTotalCellCurrencyStyle();
        XSSFRow header = sheet1.createRow(rowidx);
        header.setHeightInPoints((short) 16);

        int colidx = 0;
        header.createCell(colidx).setCellValue("");
        header.getCell(colidx++).setCellStyle(null);
        header.createCell(colidx).setCellValue("");
        header.getCell(colidx++).setCellStyle(style);
        /*
        header.createCell(colidx).setCellValue("");
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue("");
        header.getCell(colidx++).setCellStyle(style);
        */
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_subtotal", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(onetimePrice.doubleValue());
        header.getCell(colidx++).setCellStyle(currencyStyle);
        header.createCell(colidx).setCellValue(recurringPrice.doubleValue());
        header.getCell(colidx).setCellStyle(currencyStyle);
        
        return rowidx;
    }
    
    private Integer writeLineItemTotal(Integer rowidx, BigDecimal onetimePrice, BigDecimal recurringPrice) {
        CellStyle style = totalCellStyle();
        CellStyle currencyStyle = totalCellCurrencyStyle();
        XSSFRow header = sheet1.createRow(rowidx);
        header.setHeightInPoints((short) 16);

        int colidx = 0;
        header.createCell(colidx).setCellValue("");
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue("");
        header.getCell(colidx++).setCellStyle(style);
        /*
        header.createCell(colidx).setCellValue("");
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue("");
        header.getCell(colidx++).setCellStyle(style);
        */
        header.createCell(colidx).setCellValue(messageSource.getMessage("ui_col_total_caps", null, locale));
        header.getCell(colidx++).setCellStyle(style);
        header.createCell(colidx).setCellValue(onetimePrice.doubleValue());
        header.getCell(colidx++).setCellStyle(currencyStyle);
        header.createCell(colidx).setCellValue(recurringPrice.doubleValue());
        header.getCell(colidx).setCellStyle(currencyStyle);
        
        return rowidx;
    }
    
    private Integer writeBlankRow(CellStyle style, Integer rowidx) {
    	XSSFRow row = sheet1.createRow(rowidx);
        row.setHeightInPoints((short) 16);
        row.setRowStyle(style);
        
        int colidx = 0;
        row.createCell(colidx).setCellValue("");
        row.getCell(colidx).setCellStyle(style);
        
        return rowidx;
    }
    
    private Integer writeChildContractRow(CellStyle firstCellStyle, CellStyle style, CellStyle dateStyle, CellStyle currencyStyle, Integer rowidx, Service service) {
    	XSSFRow row = sheet1.createRow(rowidx);
        row.setHeightInPoints((short) 16);
        row.setRowStyle(style);
        
        int colidx = 0;
        row.createCell(colidx).setCellValue("");
        row.getCell(colidx++).setCellStyle(firstCellStyle);
        row.createCell(colidx).setCellValue(service.getDeviceDescription());
        row.getCell(colidx++).setCellStyle(style);
        
        Integer quantity = 1;
        if(service.getDeviceUnitCount() != null) quantity = service.getDeviceUnitCount();
        row.createCell(colidx).setCellValue(quantity);
        row.getCell(colidx++).setCellStyle(style);
        
        /*
        row.createCell(colidx).setCellValue(service.getStartDate());
        row.getCell(colidx++).setCellStyle(dateStyle);
        row.createCell(colidx).setCellValue(service.getEndDate());
        row.getCell(colidx++).setCellStyle(dateStyle);
        */
        
        BigDecimal onetimeRevenue = service.getFormattedOnetimeRevenue();
        BigDecimal recurringRevenue = service.getFormattedRecurringRevenue();		
        row.createCell(colidx).setCellValue(onetimeRevenue.doubleValue());
        row.getCell(colidx++).setCellStyle(currencyStyle);
        row.createCell(colidx).setCellValue(recurringRevenue.doubleValue());
        row.getCell(colidx).setCellStyle(currencyStyle);
        
        return rowidx;
    }
    
    protected CellStyle headerTableHeaderCellStyle() {
		CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName("Calibri");
        style.setFillForegroundColor(HSSFColor.GREY_80_PERCENT.index);
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        font.setColor(HSSFColor.WHITE.index);
        font.setFontHeightInPoints((short) 20);
        style.setFont(font);
        return style;
	}
	
	protected CellStyle headerTableBodyCellStyle() {
		CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName("Calibri");
        style.setFillForegroundColor(HSSFColor.GREY_40_PERCENT.index);
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        font.setColor(HSSFColor.WHITE.index);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        return style;
	}
	
	protected CellStyle headerCellStyle() {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName("Calibri");
        style.setFillForegroundColor(HSSFColor.GREY_80_PERCENT.index);
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        font.setColor(HSSFColor.WHITE.index);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        return style;
    }
    
    private CellStyle headerRowCellStyle() {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName("Calibri");
        style.setFillForegroundColor(HSSFColor.GREY_80_PERCENT.index);
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        font.setColor(HSSFColor.WHITE.index);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        return style;
    }
    
    private CellStyle subHeaderRowCellStyle() {
    	CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName("Calibri");
        style.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        font.setColor(HSSFColor.BLACK.index);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        return style;
    }
    
    private CellStyle subTotalCellStyle() {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName("Calibri");
        style.setAlignment(CellStyle.ALIGN_RIGHT);
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        font.setColor(HSSFColor.BLACK.index);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        return style;
    }
    
    private CellStyle subTotalCellCurrencyStyle() {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName("Calibri");
        style.setDataFormat((short) 8);
        style.setAlignment(CellStyle.ALIGN_RIGHT);
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        font.setColor(HSSFColor.BLACK.index);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        return style;
    }
    
    private CellStyle titleCellStyle() {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName("Calibri");
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        font.setFontHeightInPoints((short) 16);
        style.setFont(font);
        return style;
    }

    private CellStyle totalCellStyle() {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName("Calibri");
        style.setFillForegroundColor(HSSFColor.PALE_BLUE.index);
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        style.setAlignment(CellStyle.ALIGN_RIGHT);
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        font.setColor(HSSFColor.BLACK.index);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        return style;
    }
    
    private CellStyle totalCellCurrencyStyle() {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName("Calibri");
        style.setFillForegroundColor(HSSFColor.PALE_BLUE.index);
        style.setDataFormat((short) 8);
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        style.setAlignment(CellStyle.ALIGN_RIGHT);
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        font.setColor(HSSFColor.BLACK.index);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        return style;
    }
    
    private CellStyle ciNameCellStyle() {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName("Calibri");
        font.setColor(HSSFColor.BLACK.index);
        font.setFontHeightInPoints((short) 12);
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        style.setFont(font);
        return style;
    }
    
    private CellStyle cellStyle() {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName("Calibri");
        font.setColor(HSSFColor.BLACK.index);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        return style;
    }
    
    private CellStyle dateStyle() {
        CellStyle style = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        Font font = workbook.createFont();
        font.setFontName("Calibri");
        font.setColor(HSSFColor.BLACK.index);
        style.setAlignment(CellStyle.ALIGN_RIGHT);
        style.setDataFormat(createHelper.createDataFormat().getFormat("mm/dd/yyyy"));
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        return style;
    }
    
    private CellStyle currencyStyle() {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName("Calibri");
        font.setColor(HSSFColor.BLACK.index);
        style.setAlignment(CellStyle.ALIGN_RIGHT);
        style.setDataFormat((short) 8);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        return style;
    }
    
}
