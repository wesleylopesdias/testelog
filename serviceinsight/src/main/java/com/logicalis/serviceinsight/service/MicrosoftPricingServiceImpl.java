package com.logicalis.serviceinsight.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.transaction.annotation.Transactional;

import com.logicalis.serviceinsight.data.Device;
import com.logicalis.serviceinsight.data.Microsoft365SubscriptionConfig;
import com.logicalis.serviceinsight.data.MicrosoftM365ImportRecord;
import com.logicalis.serviceinsight.data.MicrosoftM365NCImportRecord;
import com.logicalis.serviceinsight.data.MicrosoftPriceList;
import com.logicalis.serviceinsight.data.MicrosoftPriceListM365NCProduct;
import com.logicalis.serviceinsight.data.MicrosoftPriceListM365Product;
import com.logicalis.serviceinsight.data.MicrosoftPriceListProduct;
import com.logicalis.serviceinsight.data.Service;
import com.logicalis.serviceinsight.data.Device.BillingPlan;
import com.logicalis.serviceinsight.data.Device.TermDuration;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.BOMInputStream;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Value;

@org.springframework.stereotype.Service
@Transactional(readOnly = false, rollbackFor = ServiceException.class)
public class MicrosoftPricingServiceImpl extends BaseServiceImpl implements MicrosoftPricingService {

    public static String DYNAMICS_365 = "Dynamics 365";
    @Value("${pricing.dynamics_365.enabled}")
    Boolean enableDynamics365;
	
    private static final DateTimeFormatter azureZulu7DigitsDateTimeFormat = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'"); // .9999999Z
    
	private static final String MICROSOFT_365_SUBSCRIPTION_CONFIG_BASE_QUERY = "select msc.id, msc.contract_id, msc.tenant_id, msc.support_type, msc.type, msc.support_flat_fee, msc.support_percent, "
			+	   "msc.active, msc.device_id, msc.service_id, d.description device_description, d.part_number device_part_number, s.name service_name "
			+	   " from microsoft_365_subscription_config msc "
			+	   " inner join device d on d.id = msc.device_id "
			+	   " inner join service s on s.id = msc.service_id ";
	private static final String MICROSOFT_365_PRICE_LIST_PRODUCTS_BASE_QUERY = "select mplp.id, mplp.microsoft_price_list_id, mplp.offer_name, mplp.offer_id, mplp.license_agreement_type, mplp.purchase, "
			+	   "mplp.secondary_license_type, mplp.end_customer_type, mplp.list_price, mplp.erp_price, mplp.material "
			+	   "from microsoft_price_list_product mplp ";
	private static final String MICROSOFT_365NC_PRICE_LIST_PRODUCTS_BASE_QUERY = "select mplncp.id, mplncp.microsoft_price_list_id, mplncp.product_title, mplncp.product_id, mplncp.sku_title, mplncp.sku_description, "
			+	   " mplncp.publisher, mplncp.term_duration, mplncp.billing_plan, mplncp.unit_price, mplncp.erp_price, mplncp.tags, mplncp.segment, mplncp.effective_start_date, mplncp.effective_end_date "
			+	   "from microsoft_price_list_nc_product mplncp ";
	
	public void importMicrosoftPriceList(File uploaded, Date month, MicrosoftPriceList.MicrosoftPriceListType type) throws ServiceException {
    	log.debug("importing O365 file with name [{}]", uploaded.getName());
        if (month == null) {
            DateTime currentMonth = new DateTime()
                    .withTime(0,0,0,0)
                    .dayOfMonth()
                    .withMinimumValue();
            log.debug("using current Month to import M365 NCE: [{}]", DateTimeFormat.forPattern("MM/yyyy").print(currentMonth));
            month = currentMonth.toDate();
        }
        try {
            if (!uploaded.getName().endsWith("xls") && !uploaded.getName().endsWith("xlsx") &&
                    !uploaded.getName().endsWith("zip")) {
                throw new IllegalArgumentException("Received file is not a zip file OR does not have a standard excel extension.");
            }
            
            if (uploaded.getName().endsWith("xls") || uploaded.getName().endsWith("xlsx")) {
                FileInputStream bis = new FileInputStream(uploaded);
                Workbook workbook;
                if (uploaded.getName().endsWith("xls")) {
                    workbook = new HSSFWorkbook(bis);
                } else {
                    workbook = new XSSFWorkbook(bis);
                }
                Sheet sheet = workbook.getSheetAt(0);
                Iterator<Row> rowIterator = sheet.rowIterator();
                if(MicrosoftPriceList.MicrosoftPriceListType.M365.equals(type)) {
                    processM365ProductImport(rowIterator, month);
                } else if(MicrosoftPriceList.MicrosoftPriceListType.M365NC.equals(type)) {
                    processM365NCProductImport(rowIterator, month);
                } else {
                    //TODO -- replace with message
                    throw new ServiceException("Unknown upload type");
                }
            } else if (uploaded.getName().endsWith("zip")) {
                log.debug("importing M365NC file with name [{}]", uploaded.getName());
                ZipInputStream zis = null;
                try {
                    zis = new ZipInputStream(new FileInputStream(uploaded));
                    ZipEntry zipEntry = zis.getNextEntry();
                    if (zipEntry != null) {
                        if (zipEntry.isDirectory()) {
                            log.debug("zipEntry folder: {}", zipEntry.getName());
                            // ahhh, not expecting that
                            throw new ServiceException("unexpected: folder found in ZIP file...");
                        } else {
                            log.debug("zipEntry file: {}", zipEntry.getName());
                            BOMInputStream bomis = new BOMInputStream(zis);
                            if (bomis.hasBOM()) {
                                log.info("Reading BOM in ZipInputSteam!");
                            }
                            final Reader reader = new InputStreamReader(bomis, "UTF-8");
                            final CSVParser parser = new CSVParser(reader, CSVFormat.EXCEL.withFirstRecordAsHeader());
                            int linesread = 0;
                            try {
                                linesread = processM365NCProductImport(parser, month);
                            } finally {
                                parser.close();
                                reader.close();
                            }
                            log.debug("Read [{}] lines", linesread);
                        }
                    }
                } finally {
                    try {
                        zis.closeEntry();
                        zis.close();
                    } catch(Exception onclose) {
                        log.info("unable to close inputstream: {}", onclose.getMessage());
                    }
                }
            }
        } catch (FileNotFoundException fnfe) {
            log.error("failed to import products", fnfe);
            throw new ServiceException("Error - file to import not found", fnfe);
        } catch (IOException ioe) {
            log.error("failed to import products", ioe);
            throw new ServiceException("IO Error - reading products", ioe);
        }
    }
	
	private void processM365ProductImport(Iterator<Row> rowIterator, Date month) throws ServiceException {
		List<MicrosoftM365ImportRecord> records = new ArrayList<MicrosoftM365ImportRecord>();
        int idx = 1;
        int counter = 0;
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            switch (row.getRowNum()) {
                case 0:
                	log.info("Case 0");
                    counter++;
                    break;
                default:
                	counter++;
                    
                	MicrosoftM365ImportRecord record = new MicrosoftM365ImportRecord();
                    Iterator<Cell> rowColumns = row.cellIterator();
                    while (rowColumns.hasNext()) {
                        Cell rowCell = rowColumns.next();
                        int columnIndex = rowCell.getColumnIndex();
                        try {
                            switch (columnIndex) {
                                case 0:
                                    record.setOperation(rowCell.getStringCellValue());
                                    break;
                                case 1:
                                    //skip -- valid from
                                    break;
                                case 2:
                                    //skip -- valid to
                                    break;
                                case 3:
                                	log.info(rowCell.getStringCellValue() + " at Row " + counter);
                                	record.setOfferName(rowCell.getStringCellValue());
                                    break;
                                case 4:
                                	record.setOfferId(rowCell.getStringCellValue());
                                    break;
                                case 5:
                                	record.setLicenseAgreementType(rowCell.getStringCellValue()); //corporate, gov, etc
                                    break;
                                case 6:
                                	record.setPurchase(rowCell.getStringCellValue());
                                    break;
                                case 7:
                                	record.setSecondaryLicenseType(rowCell.getStringCellValue());
                                    break;
                                case 8:
                                	record.setEndCustomerType(rowCell.getStringCellValue());
                                    break;
                                case 9:
                                	if(Cell.CELL_TYPE_NUMERIC == rowCell.getCellType()) {
                                		BigDecimal cost = new BigDecimal(rowCell.getNumericCellValue());
                                		record.setListPrice(cost);
                                	}
                                    break;
                                case 10:
                                	if(Cell.CELL_TYPE_NUMERIC == rowCell.getCellType()) {
                                		BigDecimal price = new BigDecimal(rowCell.getNumericCellValue());
                                		record.setErpPrice(price);
                                	}
                                    break;
                                case 11:
                                    record.setMaterial(rowCell.getStringCellValue());
                                    break;
                                default:
                                // foo
                            }
                        } catch (IllegalStateException e) {
                        	int currentColumn = columnIndex + 1;
                            throw new IllegalStateException(e.getMessage() + " at Row: " + counter + ", Column: " + (currentColumn + 1));
                        }
                    }
                    
                    //validate records
                    if(StringUtils.isEmpty(record.getOperation())) {
                    	throw new ServiceException("A/C/D/U is required for record at row [" + counter + "]");
                    }
                      
                    if(StringUtils.isEmpty(record.getOfferName())) {
                    	throw new ServiceException("Product Name is required for record at row [" + counter + "]");
                    }
                      
                    if(StringUtils.isEmpty(record.getOfferId())) {
                    	throw new ServiceException("Offer ID is required for record at row [" + counter + "]");
                    }
                   	                    
                    if(record.getListPrice() == null) {
                    	throw new ServiceException("List Price is required for record at row [" + counter + "]");
                    }
                      
                    if(record.getErpPrice() == null) {
                    	throw new ServiceException("ERP Price is required for record at row [" + counter + "]");
                    }
                    
                    if(record.getLicenseAgreementType() == null) {
                    	throw new ServiceException("License Agreement Type is required for record at row [" + counter + "]");
                    }
                        
                    records.add(record); 
            }
        }
        
        MicrosoftPriceList microsoftPriceList = new MicrosoftPriceList();
        microsoftPriceList.setMonth(month);
        microsoftPriceList.setType(MicrosoftPriceList.MicrosoftPriceListType.M365);
        Long microsoftPriceListId = saveMicrosoftPriceList(microsoftPriceList);
        
        int skip_count = 0;
        //process records
        for(MicrosoftM365ImportRecord rec: records) {
                if (skipD365Import(rec)) {
                    skip_count++;
                    continue;
                }
        	String operation = rec.getOperation();
        	String name = rec.getOfferName();
        	MicrosoftM365ImportRecord.Operation operationType = MicrosoftM365ImportRecord.Operation.valueOf(operation);
        	if(MicrosoftM365ImportRecord.Operation.ADD.equals(operationType) || MicrosoftM365ImportRecord.Operation.UNC.equals(operationType) || MicrosoftM365ImportRecord.Operation.CHG.equals(operationType)) {
        		MicrosoftPriceListM365Product newProduct = new MicrosoftPriceListM365Product();
        		newProduct.setOfferName(name);
        		newProduct.setOfferId(rec.getOfferId());
        		newProduct.setUnitPrice(rec.getListPrice());
        		newProduct.setErpPrice(rec.getErpPrice());
        		newProduct.setSegment(Device.Segment.valueOf(rec.getLicenseAgreementType().toLowerCase()));
        		newProduct.setPurchase(rec.getPurchase());
        		newProduct.setSecondaryLicenseType(rec.getSecondaryLicenseType());
        		newProduct.setEndCustomerType(rec.getEndCustomerType());
        		newProduct.setMaterial(rec.getMaterial());
        		newProduct.setMicrosoftPriceListId(microsoftPriceListId);
        		
        		log.info("Adding New Product [" + name + "]");
        		saveMicrosoftPriceListM365Product(newProduct);
        	}
        }
        log.info("enableDynamics365: [{}]; total O365/M365 skipped: [{}]", new Object[]{enableDynamics365, skip_count});
	}
	
	private void processM365NCProductImport(Iterator<Row> rowIterator, Date month) throws ServiceException {
		List<MicrosoftM365NCImportRecord> records = new ArrayList<MicrosoftM365NCImportRecord>();
        int idx = 1;
        int counter = 0;
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            switch (row.getRowNum()) {
                case 0:
                	log.info("Case 0");
                    counter++;
                    break;
                default:
                	counter++;
                    
                	MicrosoftM365NCImportRecord record = new MicrosoftM365NCImportRecord();
                    Iterator<Cell> rowColumns = row.cellIterator();
                    while (rowColumns.hasNext()) {
                        Cell rowCell = rowColumns.next();
                        int columnIndex = rowCell.getColumnIndex();
                        try {
                            switch (columnIndex) {
                                case 0:
                                    record.setProductTitle(rowCell.getStringCellValue());
                                    break;
                                case 1:
                                	record.setProductId(rowCell.getStringCellValue());
                                    break;
                                case 2:
                                	//record.setSkuId(rowCell.getStringCellValue());
                                    break;
                                case 3:
                                	record.setSkuTitle(rowCell.getStringCellValue());
                                    break;
                                case 4:
                                	record.setPublisher(rowCell.getStringCellValue());
                                    break;
                                case 5:
                                	record.setSkuDescription(rowCell.getStringCellValue());
                                    break;
                                case 6:
                                	//record.setUnitOfMeasure(rowCell.getStringCellValue());
                                    break;
                                case 7:
                                	record.setTermDuration(rowCell.getStringCellValue());
                                    break;
                                case 8:
                                	record.setBillingPlan(rowCell.getStringCellValue());
                                    break;
                                case 9:
                                	record.setMarket(rowCell.getStringCellValue());
                                    break;
                                case 10:
                                	record.setCurrency(rowCell.getStringCellValue());
                                    break;
                                case 11:
                                	if(Cell.CELL_TYPE_NUMERIC == rowCell.getCellType()) {
                                		BigDecimal cost = new BigDecimal(rowCell.getNumericCellValue());
                                		record.setUnitPrice(cost);
                                	}
                                    break;
                                case 12:
                                	//tier range min
                                    break;
                                case 13:
                                	//tier range max
                                    break;
                                case 14:
                                	record.setEffectiveStartDate(rowCell.getStringCellValue());
                                    break;
                                case 15:
                                	record.setEffectiveEndDate(rowCell.getStringCellValue());
                                    break;
                                case 16:
                                	record.setTags(rowCell.getStringCellValue());
                                    break;
                                case 17:
                                	if(Cell.CELL_TYPE_NUMERIC == rowCell.getCellType()) {
                                		BigDecimal price = new BigDecimal(rowCell.getNumericCellValue());
                                		record.setErpPrice(price);
                                	}
                                    break;
                                case 18:
                                    record.setSegment(rowCell.getStringCellValue());
                                    break;
                                default:
                                // foo
                            }
                        } catch (IllegalStateException e) {
                        	int currentColumn = columnIndex + 1;
                            throw new IllegalStateException(e.getMessage() + " at Row: " + counter + ", Column: " + (currentColumn + 1));
                        }
                    }
                    
                    //validate records
                    /*
                    if(StringUtils.isEmpty(record.getOfferName())) {
                    	throw new ServiceException("Product Name is required for record at row [" + counter + "]");
                    }
                      
                    if(StringUtils.isEmpty(record.getOfferId())) {
                    	throw new ServiceException("Offer ID is required for record at row [" + counter + "]");
                    }
                   	                    
                    if(record.getListPrice() == null) {
                    	throw new ServiceException("List Price is required for record at row [" + counter + "]");
                    }
                      
                    if(record.getErpPrice() == null) {
                    	throw new ServiceException("ERP Price is required for record at row [" + counter + "]");
                    }*/
                        
                    records.add(record); 
            }
        }
        
        MicrosoftPriceList microsoftPriceList = new MicrosoftPriceList();
        microsoftPriceList.setMonth(month);
        microsoftPriceList.setType(MicrosoftPriceList.MicrosoftPriceListType.M365NC);
        Long microsoftPriceListId = saveMicrosoftPriceList(microsoftPriceList);
        
        int skip_count = 0;
        //process records
        for(MicrosoftM365NCImportRecord rec: records) {
                if (skipD365Import(rec)) {
                    skip_count++;
                    continue;
                }
        	Device.Segment segment = Device.Segment.valueOf(rec.getSegment().toLowerCase());
        	Device.TermDuration termDuration = Device.TermDuration.valueOf(rec.getTermDuration().toUpperCase());
        	Device.BillingPlan billingPlan = Device.BillingPlan.valueOf(rec.getBillingPlan().toLowerCase());

        	MicrosoftPriceListM365NCProduct newProduct = new MicrosoftPriceListM365NCProduct();
        	newProduct.setProductTitle(rec.getProductTitle());
    		newProduct.setOfferName(rec.getSkuTitle());
    		newProduct.setOfferId(rec.getProductId());
    		newProduct.setSkuDescription(rec.getSkuDescription());
    		newProduct.setUnitPrice(rec.getUnitPrice());
    		newProduct.setErpPrice(rec.getErpPrice());
    		newProduct.setSegment(segment);
    		newProduct.setBillingPlan(billingPlan);
    		newProduct.setTermDuration(termDuration);
    		newProduct.setPublisher(rec.getPublisher());
    		newProduct.setTags(rec.getTags());
    		
    		DateTime effectiveStartDate = new DateTime();
    		DateTime effectiveEndDate = new DateTime();
    		newProduct.setEffectiveStartDate(effectiveStartDate.toDate());
    		newProduct.setEffectiveEndDate(effectiveEndDate.toDate());
    		newProduct.setMicrosoftPriceListId(microsoftPriceListId);
    		
    		saveMicrosoftPriceListM365NCProduct(newProduct);
    	}
        log.info("enableDynamics365: [{}]; total M365NC skipped: [{}]", new Object[]{enableDynamics365, skip_count});
	}
	
    private int processM365NCProductImport(CSVParser parser, Date month) throws ServiceException {
        
        List<MicrosoftM365NCImportRecord> records = new ArrayList<MicrosoftM365NCImportRecord>();
        log.debug("HEADER: {}", Arrays.toString(parser.getHeaderNames().toArray()));
        
        int linesread = 0;
        for (final CSVRecord csvrec : parser) {
            MicrosoftM365NCImportRecord record = new MicrosoftM365NCImportRecord();
            record.setProductTitle(csvrec.get("ProductTitle"));
            record.setProductId(csvrec.get("ProductId"));
            // record.setSkuId(csvrec.get("SkuId"));
            record.setSkuTitle(csvrec.get("SkuTitle"));
            record.setPublisher(csvrec.get("Publisher"));
            record.setSkuDescription(csvrec.get("SkuDescription"));
            // record.setUnitOfMeasure(csvrec.get("UnitOfMeasure"));
            record.setTermDuration(csvrec.get("TermDuration"));
            record.setBillingPlan(csvrec.get("BillingPlan"));
            record.setMarket(csvrec.get("Market"));
            record.setCurrency(csvrec.get("Currency"));
            BigDecimal cost = new BigDecimal(csvrec.get("UnitPrice"));
            record.setUnitPrice(cost);
            record.setEffectiveStartDate(csvrec.get("EffectiveStartDate"));
            record.setEffectiveEndDate(csvrec.get("EffectiveEndDate"));
            record.setTags(csvrec.get("Tags"));
            BigDecimal price = new BigDecimal(csvrec.get("ERP Price"));
            record.setErpPrice(price);
            record.setSegment(csvrec.get("Segment"));
            records.add(record);
            linesread++;
        }
        
        MicrosoftPriceList microsoftPriceList = new MicrosoftPriceList();
        microsoftPriceList.setMonth(month);
        microsoftPriceList.setType(MicrosoftPriceList.MicrosoftPriceListType.M365NC);
        Long microsoftPriceListId = saveMicrosoftPriceList(microsoftPriceList);
        
        int skip_count = 0;
        //process records
        for(MicrosoftM365NCImportRecord rec: records) {
            if (skipD365Import(rec)) {
                skip_count++;
                continue;
            }
            Device.Segment segment = Device.Segment.valueOf(rec.getSegment().toLowerCase());
            Device.TermDuration termDuration = Device.TermDuration.valueOf(rec.getTermDuration().toUpperCase());
            Device.BillingPlan billingPlan = Device.BillingPlan.valueOf(rec.getBillingPlan().toLowerCase());

            MicrosoftPriceListM365NCProduct newProduct = new MicrosoftPriceListM365NCProduct();
            newProduct.setProductTitle(rec.getProductTitle());
            newProduct.setOfferName(rec.getSkuTitle());
            newProduct.setOfferId(rec.getProductId());
            newProduct.setSkuDescription(rec.getSkuDescription());
            newProduct.setUnitPrice(rec.getUnitPrice());
            newProduct.setErpPrice(rec.getErpPrice());
            newProduct.setSegment(segment);
            newProduct.setBillingPlan(billingPlan);
            newProduct.setTermDuration(termDuration);
            newProduct.setPublisher(rec.getPublisher());
            newProduct.setTags(rec.getTags());

            DateTime effectiveStartDate = new DateTime();
            DateTime effectiveEndDate = new DateTime();
            newProduct.setEffectiveStartDate(effectiveStartDate.toDate());
            newProduct.setEffectiveEndDate(effectiveEndDate.toDate());
            newProduct.setMicrosoftPriceListId(microsoftPriceListId);

            saveMicrosoftPriceListM365NCProduct(newProduct);
    	}
        log.info("enableDynamics365: [{}]; total M365NC skipped: [{}]", new Object[]{enableDynamics365, skip_count});
        return linesread;
    }
	
	public Long saveMicrosoftPriceList(MicrosoftPriceList microsoftPriceList) throws ServiceException {
		//validate that there isn't already an entry for that month and type
		Integer count = jdbcTemplate.queryForObject("select count(*) from microsoft_price_list where month = ? and type = ?", Integer.class, new Object[] {microsoftPriceList.getMonth(), microsoftPriceList.getType().name()});
		if(count > 0) {
			throw new ServiceException("There is already a price list of that type for that month.");
		}
		
		try {
            SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
            jdbcInsert.withTableName("microsoft_price_list").usingGeneratedKeyColumns("id");
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("type", microsoftPriceList.getType());
            params.put("month", microsoftPriceList.getMonth());
            params.put("created", new Date());
            params.put("created_by", authenticatedUser());
            Number pk = jdbcInsert.executeAndReturnKey(new MapSqlParameterSource(params));
            return (Long) pk;
        } catch (Exception any) {
            log.warn("An error occurred inserting a Microsoft Price List", any);
            throw new ServiceException("An error occurred inserting a Microsoft Price List: " + any.getMessage());
        }
	}
	
	public Long saveMicrosoftPriceListM365Product(MicrosoftPriceListM365Product product) throws ServiceException {
		try {
            SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
            jdbcInsert.withTableName("microsoft_price_list_product").usingGeneratedKeyColumns("id");
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("offer_name", product.getOfferName());
            params.put("offer_id", product.getOfferId());
            params.put("microsoft_price_list_id", product.getMicrosoftPriceListId());
            params.put("license_agreement_type", product.getSegment());
            params.put("purchase", product.getPurchase());
            params.put("secondary_license_type", product.getSecondaryLicenseType());
            params.put("end_customer_type", product.getEndCustomerType());
            params.put("list_price", product.getUnitPrice());
            params.put("erp_price", product.getErpPrice());
            params.put("material", product.getMaterial());
            params.put("created", new Date());
            params.put("created_by", authenticatedUser());
            Number pk = jdbcInsert.executeAndReturnKey(new MapSqlParameterSource(params));
            
            return (Long) pk;
        } catch (Exception any) {
            log.warn("An error occurred inserting a Microsoft Price List", any);
            throw new ServiceException("An error occurred inserting a Microsoft Price List Product: " + any.getMessage());
        }
	}
	
	public Long saveMicrosoftPriceListM365NCProduct(MicrosoftPriceListM365NCProduct product) throws ServiceException {
		try {
            SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
            jdbcInsert.withTableName("microsoft_price_list_nc_product").usingGeneratedKeyColumns("id");
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("product_title", product.getProductTitle());
            params.put("sku_title", product.getOfferName());
            params.put("product_id", product.getOfferId());
            params.put("microsoft_price_list_id", product.getMicrosoftPriceListId());
            params.put("sku_description", product.getSkuDescription());
            params.put("publisher", product.getPublisher());
            params.put("term_duration", product.getTermDuration().name());
            params.put("billing_plan", product.getBillingPlan().name());
            params.put("unit_price", product.getUnitPrice());
            params.put("erp_price", product.getErpPrice());
            params.put("tags", product.getTags());
            params.put("segment", product.getSegment().name());
            params.put("effective_start_date", product.getEffectiveStartDate());
            params.put("effective_end_date", product.getEffectiveEndDate());
            params.put("created", new Date());
            params.put("created_by", authenticatedUser());
            Number pk = jdbcInsert.executeAndReturnKey(new MapSqlParameterSource(params));

            return (Long) pk;
        } catch (Exception any) {
            log.warn("An error occurred inserting a Microsoft Price List NC Product", any);
            throw new ServiceException("An error occurred inserting a Microsoft Price List NC Product: " + any.getMessage());
        }
	}
	
	public List<MicrosoftPriceList> getMicrosoftPriceLists() {
		String query = "select mpl.id, mpl.type, mpl.month from microsoft_price_list mpl order by month desc";
		List<MicrosoftPriceList> lists = jdbcTemplate.query(query,
                 new RowMapper<MicrosoftPriceList>() {
            @Override
            public MicrosoftPriceList mapRow(ResultSet rs, int i) throws SQLException {
                return new MicrosoftPriceList(
                		rs.getLong("id"),
                		MicrosoftPriceList.MicrosoftPriceListType.valueOf(rs.getString("type")),
                        rs.getDate("month"));
            }
        });
		
		for(MicrosoftPriceList list : lists) {
			Integer count = 0;
			if(MicrosoftPriceList.MicrosoftPriceListType.M365.equals(list.getType())) {
				count = jdbcTemplate.queryForObject("select count(*) from microsoft_price_list_product where microsoft_price_list_id = ?", Integer.class, list.getId());
			} else if(MicrosoftPriceList.MicrosoftPriceListType.M365NC.equals(list.getType())) {
				count = jdbcTemplate.queryForObject("select count(*) from microsoft_price_list_nc_product where microsoft_price_list_id = ?", Integer.class, list.getId());
			}
			
			list.setProductCount(count);
		}
		
		return lists;
	}
	
	public MicrosoftPriceList getMicrosoftPriceListForMonthOf(Date month, MicrosoftPriceList.MicrosoftPriceListType type) {
		String query = "select mpl.id, mpl.type, mpl.month from microsoft_price_list mpl where month = ?";
		List<MicrosoftPriceList> lists = jdbcTemplate.query(query, new Object[] {month},
                 new RowMapper<MicrosoftPriceList>() {
            @Override
            public MicrosoftPriceList mapRow(ResultSet rs, int i) throws SQLException {
                return new MicrosoftPriceList(
                		rs.getLong("id"),
                		MicrosoftPriceList.MicrosoftPriceListType.valueOf(rs.getString("type")),
                        rs.getDate("month"));
            }
        });
		
		MicrosoftPriceList list = null;
		Integer count = 0;
		if(lists != null && lists.size() > 0) {
			list = lists.get(0);
			List<? extends MicrosoftPriceListProduct> products = getMicrosoftPriceListProductByPriceListId(list.getId(), type);
			if(products != null) count = products.size();
			list.setProductCount(count);
		}
		
		return list;
	}
	
	public MicrosoftPriceList getLatestMicrosoftPriceList(MicrosoftPriceList.MicrosoftPriceListType type) {
		String query = "select mpl.id, mpl.type, mpl.month from microsoft_price_list mpl where type = ? order by month desc";
		List<MicrosoftPriceList> lists = jdbcTemplate.query(query, new Object[] {type.name()}, 
                 new RowMapper<MicrosoftPriceList>() {
            @Override
            public MicrosoftPriceList mapRow(ResultSet rs, int i) throws SQLException {
                return new MicrosoftPriceList(
                		rs.getLong("id"),
                		MicrosoftPriceList.MicrosoftPriceListType.valueOf(rs.getString("type")),
                        rs.getDate("month"));
            }
        });
		
		MicrosoftPriceList list = null;
		if(lists != null && lists.size() > 0) {
			list = lists.get(0);
		}
		
		List<? extends MicrosoftPriceListProduct> products = getMicrosoftPriceListProductByPriceListId(list.getId(), type);
		list.setProducts(products);
		Integer count = 0;
		if(products != null) count = products.size();
		list.setProductCount(count);
		
		return list;
	}
	
	public MicrosoftPriceListM365Product getMicrosoftPriceListProductByOfferId(Long microsoftPriceListId, String offerId) {
		String query = MICROSOFT_365_PRICE_LIST_PRODUCTS_BASE_QUERY
				+	   "where mplp.microsoft_price_list_id = ? and mplp.offer_id = ?";
		List<MicrosoftPriceListM365Product> lists = jdbcTemplate.query(query, new Object[] {microsoftPriceListId, offerId},
                 new RowMapper<MicrosoftPriceListM365Product>() {
            @Override
            public MicrosoftPriceListM365Product mapRow(ResultSet rs, int i) throws SQLException {
                return new MicrosoftPriceListM365Product(
                		rs.getLong("id"),
                		rs.getLong("microsoft_price_list_id"),
                		rs.getString("offer_name"),
                		rs.getString("offer_id"),
                		Device.Segment.valueOf(rs.getString("license_agreement_type")),
                		rs.getString("purchase"),
                		rs.getString("secondary_license_type"),
                		rs.getString("end_customer_type"),
                		rs.getBigDecimal("list_price"),
                		rs.getBigDecimal("erp_price"),
                        rs.getString("material"));
            }
        });
		
		if(lists != null && lists.size() > 0) {
			return lists.get(0);
		}
		
		return null;
	}
	
	public List<? extends MicrosoftPriceListProduct> getMicrosoftPriceListProductByPriceListId(Long microsoftPriceListId, MicrosoftPriceList.MicrosoftPriceListType type) {
		List<? extends MicrosoftPriceListProduct> products = new ArrayList<>();
		
		String query = "";
		if(MicrosoftPriceList.MicrosoftPriceListType.M365.equals(type)) {
			query = MICROSOFT_365_PRICE_LIST_PRODUCTS_BASE_QUERY
					+	   "where mplp.microsoft_price_list_id = ?";
			products = jdbcTemplate.query(query, new Object[] {microsoftPriceListId},
	                 new RowMapper<MicrosoftPriceListM365Product>() {
	            @Override
	            public MicrosoftPriceListM365Product mapRow(ResultSet rs, int i) throws SQLException {
	                return new MicrosoftPriceListM365Product(
	                		rs.getLong("id"),
	                		rs.getLong("microsoft_price_list_id"),
	                		rs.getString("offer_name"),
	                		rs.getString("offer_id"),
	                		Device.Segment.valueOf(rs.getString("license_agreement_type")),
	                		rs.getString("purchase"),
	                		rs.getString("secondary_license_type"),
	                		rs.getString("end_customer_type"),
	                		rs.getBigDecimal("list_price"),
	                		rs.getBigDecimal("erp_price"),
	                        rs.getString("material"));
	            }
	        });
		} else if(MicrosoftPriceList.MicrosoftPriceListType.M365NC.equals(type)) {
			query = MICROSOFT_365NC_PRICE_LIST_PRODUCTS_BASE_QUERY
					+	   "where mplncp.microsoft_price_list_id = ?";
			products = jdbcTemplate.query(query, new Object[] {microsoftPriceListId},
	                 new RowMapper<MicrosoftPriceListM365NCProduct>() {
	            @Override
	            public MicrosoftPriceListM365NCProduct mapRow(ResultSet rs, int i) throws SQLException {
	                return new MicrosoftPriceListM365NCProduct(
	                		rs.getLong("id"),
	                		rs.getLong("microsoft_price_list_id"),
	                		rs.getString("sku_title"),
	                		rs.getString("product_id"),
	                		Device.Segment.valueOf(rs.getString("segment")),
	                		rs.getBigDecimal("unit_price"),
	                		rs.getBigDecimal("erp_price"),
	                		rs.getString("product_title"),
	                		rs.getString("publisher"),
	                		rs.getString("sku_description"),
	                		Device.TermDuration.valueOf(rs.getString("term_duration")),
	                		Device.BillingPlan.valueOf(rs.getString("billing_plan")),
	                        rs.getString("tags"),
	                        rs.getDate("effective_start_date"),
	                        rs.getDate("effective_end_date"));
	            }
	        });
		}
		
		return products;
	}
	
	@Override
	public Microsoft365SubscriptionConfig getMicrosoft365SubscriptionConfig(Long microsoft365SubscriptionConfigId) {
		String query = MICROSOFT_365_SUBSCRIPTION_CONFIG_BASE_QUERY
				+	   "where msc.id = ?";
		List<Microsoft365SubscriptionConfig> lists = jdbcTemplate.query(query, new Object[] {microsoft365SubscriptionConfigId},
                 new RowMapper<Microsoft365SubscriptionConfig>() {
            @Override
            public Microsoft365SubscriptionConfig mapRow(ResultSet rs, int i) throws SQLException {
                return new Microsoft365SubscriptionConfig(
                		rs.getLong("id"),
                		rs.getLong("contract_id"),
                		rs.getLong("device_id"),
                		rs.getString("device_description"),
                		rs.getString("device_part_number"),
                		rs.getLong("service_id"),
                		rs.getString("service_name"),
                		rs.getString("tenant_id"),
                		Microsoft365SubscriptionConfig.Type.valueOf(rs.getString("type")),
                		Microsoft365SubscriptionConfig.SupportType.valueOf(rs.getString("support_type")),
                		rs.getBigDecimal("support_flat_fee"),
                		rs.getBigDecimal("support_percent"),
                        rs.getBoolean("active"));
            }
        });
		
		if(lists != null && lists.size() > 0) {
			return lists.get(0);
		}
		
		return null;
	}
	
	@Override
	public Microsoft365SubscriptionConfig getMicrosoft365SubscriptionConfigByActiveAndTenantId(String tenantId) {
		String query = MICROSOFT_365_SUBSCRIPTION_CONFIG_BASE_QUERY
				+	   "where msc.tenant_id = ?";
		List<Microsoft365SubscriptionConfig> lists = jdbcTemplate.query(query, new Object[] {tenantId},
                 new RowMapper<Microsoft365SubscriptionConfig>() {
            @Override
            public Microsoft365SubscriptionConfig mapRow(ResultSet rs, int i) throws SQLException {
                return new Microsoft365SubscriptionConfig(
                		rs.getLong("id"),
                		rs.getLong("contract_id"),
                		rs.getLong("device_id"),
                		rs.getString("device_description"),
                		rs.getString("device_part_number"),
                		rs.getLong("service_id"),
                		rs.getString("service_name"),
                		rs.getString("tenant_id"),
                		Microsoft365SubscriptionConfig.Type.valueOf(rs.getString("type")),
                		Microsoft365SubscriptionConfig.SupportType.valueOf(rs.getString("support_type")),
                		rs.getBigDecimal("support_flat_fee"),
                		rs.getBigDecimal("support_percent"),
                        rs.getBoolean("active"));
            }
        });
		
		if(lists != null && lists.size() > 0) {
			return lists.get(0);
		}
		
		return null;
	}
	
	@Override
	public List<Microsoft365SubscriptionConfig> getMicrosoft365SubscriptionConfigForContract(Long contractId) {
		String query = MICROSOFT_365_SUBSCRIPTION_CONFIG_BASE_QUERY
				+	   "where msc.contract_id = ?";
		List<Microsoft365SubscriptionConfig> lists = jdbcTemplate.query(query, new Object[] {contractId},
                 new RowMapper<Microsoft365SubscriptionConfig>() {
            @Override
            public Microsoft365SubscriptionConfig mapRow(ResultSet rs, int i) throws SQLException {
                return new Microsoft365SubscriptionConfig(
                		rs.getLong("id"),
                		rs.getLong("contract_id"),
                		rs.getLong("device_id"),
                		rs.getString("device_description"),
                		rs.getString("device_part_number"),
                		rs.getLong("service_id"),
                		rs.getString("service_name"),
                		rs.getString("tenant_id"),
                		Microsoft365SubscriptionConfig.Type.valueOf(rs.getString("type")),
                		Microsoft365SubscriptionConfig.SupportType.valueOf(rs.getString("support_type")),
                		rs.getBigDecimal("support_flat_fee"),
                		rs.getBigDecimal("support_percent"),
                        rs.getBoolean("active"));
            }
        });
		
		return lists;
	}
	
	@Override
	public Long saveMicrosoft365SubscriptionConfig(Microsoft365SubscriptionConfig config) throws ServiceException {
		try {
			if(config.getId() != null) {
				updateMicrosoft365SubscriptionConfig(config);
				return config.getId();
			}
			
			validateMicrosoft365SubscriptionConfig(config, Boolean.FALSE);
			
            SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
            jdbcInsert.withTableName("microsoft_365_subscription_config").usingGeneratedKeyColumns("id");
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("tenant_id", config.getTenantId());
            params.put("service_id", config.getServiceId());
            params.put("contract_id", config.getContractId());
            params.put("device_id", config.getDeviceId());
            params.put("type", config.getType());
            params.put("support_type", config.getSupportType());
            params.put("support_flat_fee", config.getFlatFee());
            params.put("support_percent", config.getPercent());
            params.put("active", config.getActive());
            params.put("created", new Date());
            params.put("created_by", authenticatedUser());
            Number pk = jdbcInsert.executeAndReturnKey(new MapSqlParameterSource(params));
            return (Long) pk;
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_m365_config_create", new Object[]{any.getMessage()}, LocaleContextHolder.getLocale()));
        }
	}
	
	@Override
	public void updateMicrosoft365SubscriptionConfig(Microsoft365SubscriptionConfig config) throws ServiceException {
		try {
			validateMicrosoft365SubscriptionConfig(config, Boolean.TRUE);
			
			Long configId = config.getId();
			if(configId == null) {
				throw new ServiceException(messageSource.getMessage("validation_error_m365_config_not_found", null, LocaleContextHolder.getLocale()));
			}
			Integer count = jdbcTemplate.queryForObject("select count(*) from microsoft_365_subscription_config where id = ?", Integer.class, configId);
	        if (!count.equals(1)) {
	            throw new ServiceException(messageSource.getMessage("validation_error_m365_config_not_found", new Object[]{configId}, LocaleContextHolder.getLocale()));
	        }
			
            int updated = jdbcTemplate.update("update microsoft_365_subscription_config set contract_id = ?, tenant_id = ?, support_type = ?, type = ?, support_flat_fee = ?, support_percent = ?, "
            		+ " active = ?, device_id = ?, service_id = ?,"
            		+ " updated = ?, updated_by = ?"
                    + " where id = ?",
                    new Object[]{config.getContractId(), config.getTenantId(), config.getSupportType().name(), config.getType().name(), config.getFlatFee(), config.getPercent(), config.getActive(), config.getDeviceId(), config.getServiceId(),
            				new DateTime().withZone(DateTimeZone.forID(TZID)).toDate(), authenticatedUser(), configId});
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_m365_config_update", new Object[]{config.getId(), any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
	}
	
	@Override
	public void deleteMicrosoft365SubscriptionConfig(Long configId) throws ServiceException {
		try {
			Integer count = jdbcTemplate.queryForObject("select count(*) from microsoft_365_subscription_config where id = ?", Integer.class, configId);
	        if (!count.equals(1)) {
	            throw new ServiceException(messageSource.getMessage("validation_error_m365_config_not_found", new Object[]{configId}, LocaleContextHolder.getLocale()));
	        }
			
            int deleted = jdbcTemplate.update("delete from microsoft_365_subscription_config where id = ?", new Object[]{configId});
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_m365_config_delete", new Object[]{configId, any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
	}
	
	private void validateMicrosoft365SubscriptionConfig(Microsoft365SubscriptionConfig config, Boolean isUpdate) throws ServiceException {
		if(config.getContractId() == null) {
			throw new ServiceException(messageSource.getMessage("validation_error_m365_config_contract_id_required", null, LocaleContextHolder.getLocale()));
		}
		
		if(StringUtils.isEmpty(config.getTenantId())) {
			throw new ServiceException(messageSource.getMessage("validation_error_m365_config_tenant_id_required", null, LocaleContextHolder.getLocale()));
		}
		
		Microsoft365SubscriptionConfig existingConfig = getMicrosoft365SubscriptionConfigByActiveAndTenantId(config.getTenantId());
		if(existingConfig != null) {
			if(isUpdate) {
				if(!existingConfig.getId().equals(config.getId())) {
					throw new ServiceException(messageSource.getMessage("validation_error_m365_config_active_subscription_exists", new Object[] {existingConfig.getContractId()}, LocaleContextHolder.getLocale()));
				}
			} else {
				throw new ServiceException(messageSource.getMessage("validation_error_m365_config_active_subscription_exists", new Object[] {existingConfig.getContractId()}, LocaleContextHolder.getLocale()));
			}
		}
		
		if(config.getServiceId() == null) {
			throw new ServiceException(messageSource.getMessage("validation_error_m365_config_service_id_required", null, LocaleContextHolder.getLocale()));
		}
		
		if(config.getDeviceId() == null) {
			throw new ServiceException(messageSource.getMessage("validation_error_m365_config_device_id_required", null, LocaleContextHolder.getLocale()));
		}
		
		if(config.getType() == null) {
			throw new ServiceException(messageSource.getMessage("validation_error_m365_config_subscription_type_required", null, LocaleContextHolder.getLocale()));
		}
		
		if(Microsoft365SubscriptionConfig.Type.M365.equals(config.getType())) {
			if(config.getSupportType() == null) {
				throw new ServiceException(messageSource.getMessage("validation_error_m365_config_support_type_required", null, LocaleContextHolder.getLocale()));
			}
			
			if(Microsoft365SubscriptionConfig.SupportType.flat.equals(config.getSupportType()) && config.getFlatFee() == null) {
				throw new ServiceException(messageSource.getMessage("validation_error_m365_config_support_amount_required", null, LocaleContextHolder.getLocale()));
			} else if(Microsoft365SubscriptionConfig.SupportType.percent.equals(config.getSupportType()) && config.getPercent() == null) {
				throw new ServiceException(messageSource.getMessage("validation_error_m365_config_support_amount_required", null, LocaleContextHolder.getLocale()));
			}
			
		}
		
	}

    private boolean skipD365Import(MicrosoftM365ImportRecord rec) {
        if (enableDynamics365) {
            return false;
        }
        boolean skip = false;
        if (rec.getOfferName() != null && rec.getOfferName().contains(DYNAMICS_365)) {
            skip = true;
        }
        return skip;
    }

    private boolean skipD365Import(MicrosoftM365NCImportRecord rec) {
        if (enableDynamics365) {
            return false;
        }
        boolean skip = false;
        if (rec.getProductTitle() != null && rec.getProductTitle().contains(DYNAMICS_365)) {
            skip = true;
        } else if (rec.getSkuTitle() != null && rec.getSkuTitle().contains(DYNAMICS_365)) {
            skip = true;
        }
        return skip;
    }
	
}
