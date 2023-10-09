package com.logicalis.serviceinsight.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Transactional;

import com.logicalis.serviceinsight.dao.AssetItem;
import com.logicalis.serviceinsight.dao.CostItem;
import com.logicalis.serviceinsight.dao.CostItem.CostFraction;
import com.logicalis.serviceinsight.dao.Expense;
import com.logicalis.serviceinsight.dao.ExpenseCategory;
import com.logicalis.serviceinsight.dao.AssetItem.AssetCostFraction;
import com.logicalis.serviceinsight.dao.DeviceExpenseCategory;
import com.logicalis.serviceinsight.dao.Location;
import com.logicalis.serviceinsight.dao.ServiceExpenseCategory;
import com.logicalis.serviceinsight.dao.UnitCost;
import com.logicalis.serviceinsight.data.AssetImportRecordHolder;
import com.logicalis.serviceinsight.data.Contract;
import com.logicalis.serviceinsight.data.CostAllocationLineItem;
import com.logicalis.serviceinsight.data.CostImportRecordHolder;
import com.logicalis.serviceinsight.data.Customer;
import com.logicalis.serviceinsight.data.Device;
import com.logicalis.serviceinsight.data.MonthlyCostImportRecordHolder;
import com.logicalis.serviceinsight.data.MonthlyCostImportRecordHolder.ServiceFraction;
import com.logicalis.serviceinsight.data.Service;
import com.logicalis.serviceinsight.data.UnallocatedExpense;

import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Months;
import org.joda.time.format.DateTimeFormat;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

@Transactional(readOnly = false, rollbackFor = ServiceException.class)
@org.springframework.stereotype.Service
public class CostDaoServiceImpl extends BaseServiceImpl implements CostDaoService {

    public enum ModelExpenseType {
        asset, cost, labor, spla
    }
    
    @Autowired
    ApplicationDataDaoService applicationDataDaoService;
    @Autowired
    ContractDaoService contractDaoService;
    @Lazy
    @Autowired
    CostService costService;
    
    @Override
    public ExpenseCategory expenseCategoryByName(String name, String parentName) throws ServiceException {
        Integer count = 0;
        Long parentId = null;
        if(parentName != null) {
        	Integer parentCount = jdbcTemplate.queryForObject("select count(*) from expense_category where name = ? and parent_id is null", Integer.class, parentName);
        	if(parentCount > 0) {
        		parentId = jdbcTemplate.queryForObject("select id from expense_category where name = ? and parent_id is null", Long.class, parentName);
        		count = jdbcTemplate.queryForObject("select count(*) from expense_category where name = ? and parent_id = ?", Integer.class, new Object[]{name, parentId});
        	} else {
        		throw new ServiceException(messageSource.getMessage("expensecategory_not_found_for_name", new Object[]{parentName}, LocaleContextHolder.getLocale()));
        	}
        } else {
        	count = jdbcTemplate.queryForObject("select count(*) from expense_category where name = ?", Integer.class, name);
        }
        if (!count.equals(1)) {
            throw new ServiceException(messageSource.getMessage("expensecategory_not_found_for_name", new Object[]{name}, LocaleContextHolder.getLocale()));
        }
        String query = "select ec.id, ec.name, ec.description, ec.target_utilization, ec.units, ec.labor_split, ec.created, ec.created_by,"
                + " ec.updated, ec.updated_by, ecp.id p_id, ecp.name p_name, ecp.description p_description, ecp.target_utilization p_target_utilization, ecp.units p_units,"
                + " ecp.labor_split p_labor_split, ecp.created p_created, ecp.created_by p_created_by, ecp.updated p_updated, ecp.updated_by p_updated_by"
                + " from expense_category ec"
                + " left outer join expense_category ecp on ec.parent_id = ecp.id"
                + " where ec.name = ?";
        if(parentId != null) {
        	query += " and ec.parent_id = ?";
        }
        Object[] params = new Object[]{name};
        if(parentId != null) {
        	params = new Object[]{name, parentId};
        }
        return jdbcTemplate.queryForObject(query, params,
                new RowMapper<ExpenseCategory>() {
            @Override
            public ExpenseCategory mapRow(ResultSet rs, int i) throws SQLException {
                ExpenseCategory expenseCategory = new ExpenseCategory(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getBigDecimal("target_utilization"),
                        rs.getString("units"),
                        rs.getBigDecimal("labor_split"));

                Integer parentId = rs.getInt("p_id");
                if (parentId > 0) {
                    ExpenseCategory parent = new ExpenseCategory(
                            rs.getInt("p_id"),
                            rs.getString("p_name"),
                            rs.getString("p_description"),
                            rs.getBigDecimal("p_target_utilization"),
                            rs.getString("p_units"),
                            rs.getBigDecimal("p_labor_split"));
                    expenseCategory.setParent(parent);
                }
                expenseCategory.setCreated(rs.getTimestamp("created"));
                expenseCategory.setCreatedBy(rs.getString("created_by"));
                expenseCategory.setUpdated(rs.getTimestamp("updated"));
                expenseCategory.setUpdatedBy(rs.getString("updated_by"));
                return expenseCategory;
            }
        });
    }

    @Override
    public void importExpenses(File excel, ModelExpenseType expenseType, Date expenseDate) throws ServiceException {
        try {
            FileInputStream bis = new FileInputStream(excel);
            Workbook workbook;
            if (excel.getName().endsWith("xls")) {
                workbook = new HSSFWorkbook(bis);
            } else if (excel.getName().endsWith("xlsx")) {
                workbook = new XSSFWorkbook(bis);
            } else {
                throw new IllegalArgumentException("Received file does not have a standard excel extension.");
            }

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.rowIterator();
            boolean emptyRow = false;
            DataFormatter poiFmt = new DataFormatter();
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

            if (ModelExpenseType.asset.equals(expenseType)) {
                importAssets(rowIterator, emptyRow, poiFmt);
            } else if (ModelExpenseType.cost.equals(expenseType)) {
                //importCosts(rowIterator, emptyRow, poiFmt);
            	importInvoiceCosts(rowIterator, emptyRow, poiFmt, evaluator, expenseDate);
            }

        } catch (FileNotFoundException fnfe) {
            log.error("failed to import service definition", fnfe);
            throw new ServiceException("Error - file to import not found", fnfe);
        } catch (IOException ioe) {
            log.error("failed to import service definition", ioe);
            throw new ServiceException("IO Error - reading service definition", ioe);
        }
    }

    private class ExpenseGroupParts {
    	private String parent;
    	private String child;
    	
		public String getParent() {
			return parent;
		}
		public void setParent(String parent) {
			this.parent = parent;
		}
		public String getChild() {
			return child;
		}
		public void setChild(String child) {
			this.child = child;
		}
    }
    
    private class ExpenseGroupImport {
    	private String group;
    	private BigDecimal percent;
    	
		public String getGroup() {
			return group;
		}
		public void setGroup(String group) {
			this.group = group;
		}
		public BigDecimal getPercent() {
			return percent;
		}
		public void setPercent(BigDecimal percent) {
			this.percent = percent;
		}
    }
    
    private List<ExpenseGroupImport> splitGroupDataColumn(String categories) {
    	List<ExpenseGroupImport> expenseGroups = new ArrayList<ExpenseGroupImport>();
    	//if(categories.indexOf(",") != -1) {
    		String[] parts = categories.split(", ");
    		for(String part : parts) {
    			if(part.indexOf("%") != -1) {
    				String[] subparts = part.split("%:");
    				BigDecimal percent = new BigDecimal(subparts[0].trim());
    				String group = subparts[1].trim();
    				
    				ExpenseGroupImport expenseGroup = new ExpenseGroupImport();
    				expenseGroup.setGroup(group);
    				expenseGroup.setPercent(percent);
        			expenseGroups.add(expenseGroup);
    			}
    		}
    	//}
    	return expenseGroups;
    }
    
    private ExpenseGroupParts splitGroupForParent(String category) {
    	ExpenseGroupParts groupParts = new ExpenseGroupParts();
    	String parent = null;
    	String child = category;
    	
    	if(category.indexOf(">") != -1) {
    		String[] parts = category.split(">");
    		parent = parts[0].trim();
    		child = parts[1].trim();
    	}
    	
    	groupParts.setParent(parent);
    	groupParts.setChild(child);
    	return groupParts;
    }
    
    private void importInvoiceCosts(Iterator<Row> rowIterator, boolean emptyRow, DataFormatter poiFmt, FormulaEvaluator evaluator, Date expenseDate) throws ServiceException {
        int counter = 0;
        List<MonthlyCostImportRecordHolder> toinsert = new ArrayList<MonthlyCostImportRecordHolder>();
        while (rowIterator.hasNext()) {
        	MonthlyCostImportRecordHolder rec;
            Row row = rowIterator.next();
            int emptyRowCounter = 0;
            switch (row.getRowNum()) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                    counter++;
                    // header
                    break;
                default:
                    emptyRow = true;
                    for (Cell c : row) {
                        if (c.getCellType() != Cell.CELL_TYPE_BLANK) {
                            emptyRow = false;
                        } else {
                        	emptyRowCounter++;
                        }
                    }
                    //if (!emptyRow) {
                    if (emptyRowCounter < 20) {
                        // read cost records
                        counter++;
                        rec = new MonthlyCostImportRecordHolder();
                        Iterator<Cell> rowColumns = row.cellIterator();
                        while (rowColumns.hasNext()) {
                            Cell rowCell = rowColumns.next();
                            int columnIndex = rowCell.getColumnIndex();
                            try {
                                switch (columnIndex) {
                                    case 0:
                                        rec.setThirdParty(rowCell.getStringCellValue());
                                        break;
                                    case 1:
                                        rec.setCc(rowCell.getStringCellValue());
                                        break;
                                    case 2:
                                        rec.setApprover(rowCell.getStringCellValue());
                                        break;
                                    case 3:
                                    	if(Cell.CELL_TYPE_NUMERIC == rowCell.getCellType()) {
                                    		rec.setAmount(new BigDecimal(rowCell.getNumericCellValue()));
                                    	} else if(rowCell.getCellType() == Cell.CELL_TYPE_FORMULA) {
                                    		evaluator.evaluateFormulaCell(rowCell);
                                    		if(Cell.CELL_TYPE_NUMERIC == rowCell.getCachedFormulaResultType()) {
                                    			rec.setAmount(new BigDecimal(rowCell.getNumericCellValue()));
                                    		}
                                    	} else {
                                    		rec.setAmount(new BigDecimal(0));
                                    	}
                                        break;
                                    case 4:
                                    	if(Cell.CELL_TYPE_NUMERIC == rowCell.getCellType()) {
                                    		rec.setInvoiceNumber(String.valueOf(rowCell.getNumericCellValue()));
                                    	} else if (Cell.CELL_TYPE_STRING == rowCell.getCellType()) {
                                    		rec.setInvoiceNumber(rowCell.getStringCellValue());
                                    	}
                                        break;
                                    case 5:
                                        rec.setPoNumber(rowCell.getStringCellValue());
                                        break;
                                    case 6:
                                    	rec.setAmountType(rowCell.getStringCellValue());
                                        break;
                                    case 7:
                                        rec.setDescription(rowCell.getStringCellValue());
                                        break;
                                    case 8:
                                    	rec.setCategory(rowCell.getStringCellValue());
                                        break;
                                    case 9:
                                    	rec.setCustomerColumn(rowCell.getStringCellValue());
                                        break;
                                    case 10:
                                        rec.setSiCategoryColumn(rowCell.getStringCellValue());
                                        break;
                                    case 11:
                                        rec.setOspServiceColumn(rowCell.getStringCellValue());
                                        break;
                                    case 12:
                                        rec.setCostSubType(rowCell.getStringCellValue());
                                        break;
                                    case 13:
                                        rec.setNotes(rowCell.getStringCellValue());
                                        break;
                                    default:
                                    // foo
                                }
                            } catch (IllegalStateException e) {
                            	int currentColumn = columnIndex + 1;
                                throw new IllegalStateException(e.getMessage() + " at Row: " + counter + ", Column: " + (currentColumn + 1));
                            }
                        }
                        
                        BigDecimal amount = rec.getAmount();
                        String category = rec.getCategory();
                        if(amount != null && !amount.equals(BigDecimal.ZERO) && 
                        		(MonthlyCostImportRecordHolder.CATEGORY_TYPE_MS_CLIENT_SPECIFIC.equals(category) || MonthlyCostImportRecordHolder.CATEGORY_TYPE_MS_GENERAL.equals(category) ||
                        		 MonthlyCostImportRecordHolder.CATEGORY_TYPE_CLOUD_CLIENT_SPECIFIC.equals(category) || MonthlyCostImportRecordHolder.CATEGORY_TYPE_CLOUD_GENERAL.equals(category))) {
	                        if(!StringUtils.isBlank(rec.getSiCategoryColumn())) {
	                        	List<ExpenseGroupImport> groups = splitGroupDataColumn(rec.getSiCategoryColumn());
	                        	if(groups != null && groups.size() > 0) {
	                        		for(ExpenseGroupImport group : groups) {
	                        			CostFraction costFraction = new CostFraction();
	        	                        ExpenseGroupParts categories = splitGroupForParent(group.getGroup());
	        	                        ExpenseCategory expenseCategory = expenseCategoryByName(categories.getChild(), categories.getParent());
	        	                        if (expenseCategory == null) {
	        	                            throw new ServiceException(messageSource.getMessage("expensecategory_not_found_for_name", new Object[]{group.getGroup(), counter}, LocaleContextHolder.getLocale()));
	        	                        } else {
	        	                        	costFraction.setExpenseCategory(expenseCategory);
	        	                        	costFraction.setFraction(group.getPercent());
	        	                        	rec.validateCostFraction(costFraction, counter, messageSource, LocaleContextHolder.getLocale());
	        	                            rec.addCostFraction(costFraction);
	        	                        }
	                        		}
	                        		
	                        		String costSubTypeStr = rec.getCostSubType();
	                        		if(!StringUtils.isEmpty(costSubTypeStr)) {
	                	            	costSubTypeStr = costSubTypeStr.toLowerCase();
	                	            	try {
	                	            		CostItem.CostSubType costSubType = CostItem.CostSubType.valueOf(costSubTypeStr);
	                	            	} catch (Exception e) {
	                	            		throw new ServiceException(messageSource.getMessage("import_validation_error_cost_subtype_invalid", new Object[]{counter}, LocaleContextHolder.getLocale()));
	                	            	}
	                	            }
	                        	} else {
	                        		throw new ServiceException(messageSource.getMessage("import_validation_error_cost_fraction_required", new Object[]{counter}, LocaleContextHolder.getLocale()));
	                        	}
	                        	
	                        	rec.setUnallocatedExpense(Boolean.FALSE);
	                        	rec.validateCostFractions(rec.getCostFractions(), counter, messageSource, LocaleContextHolder.getLocale());
	                        } else if (!StringUtils.isBlank(rec.getOspServiceColumn())) {
	                        	if(MonthlyCostImportRecordHolder.CATEGORY_TYPE_MS_CLIENT_SPECIFIC.equals(category) || MonthlyCostImportRecordHolder.CATEGORY_TYPE_CLOUD_CLIENT_SPECIFIC.equals(category)) {
	                        		throw new ServiceException(messageSource.getMessage("import_validation_error_service_customer", new Object[]{counter}, LocaleContextHolder.getLocale()));
	                        	}
	                        	
	                        	List<ExpenseGroupImport> groups = splitGroupDataColumn(rec.getOspServiceColumn());
	                        	if(groups != null && groups.size() > 0) {
	                        		for(ExpenseGroupImport group : groups) {
	                        			ServiceFraction serviceFraction = new ServiceFraction();
	        	                        Service service = applicationDataDaoService.findActiveServiceByName(group.getGroup());
	        	                        if (service == null) {
	        	                            throw new ServiceException(messageSource.getMessage("import_service_not_found_for_name", new Object[]{group.getGroup(), counter}, LocaleContextHolder.getLocale()));
	        	                        } else {
	        	                        	Long ospId = new Long(service.getOspId());
	        	                        	serviceFraction.setOspId(ospId);
	        	                        	serviceFraction.setFraction(group.getPercent());
	        	                        	rec.validateServiceFraction(serviceFraction, group.getGroup(), counter, messageSource, LocaleContextHolder.getLocale());
	        	                            rec.addServiceFraction(serviceFraction);
	        	                        }
	                        		}
	                        	} else {
	                        		throw new ServiceException(messageSource.getMessage("import_validation_error_cost_fraction_required", new Object[]{counter}, LocaleContextHolder.getLocale()));
	                        	}
	                        	
	                        	rec.setUnallocatedExpense(Boolean.TRUE);
	                        } else {
	                        	throw new ServiceException(messageSource.getMessage("import_validation_error_cost_fraction_required", new Object[]{counter}, LocaleContextHolder.getLocale()));
	                        }
	                        
	                        if(MonthlyCostImportRecordHolder.CATEGORY_TYPE_MS_CLIENT_SPECIFIC.equals(category) || MonthlyCostImportRecordHolder.CATEGORY_TYPE_CLOUD_CLIENT_SPECIFIC.equals(category)) {
		                        String customerColumn = rec.getCustomerColumn();
		                        if (!StringUtils.isBlank(customerColumn)) {
		                        	List<ExpenseGroupImport> groups = splitGroupDataColumn(customerColumn);
		                        	BigDecimal originalAmount = rec.getAmount();
		                        	if(groups != null && groups.size() > 0) {
		                        		BigDecimal total = new BigDecimal(0);
		                        		for(ExpenseGroupImport group : groups) {
		                        			total = total.add(group.getPercent());
		                        		}
		                        		
		                        		//add up to 100%?
		                        		if(!total.setScale(2, RoundingMode.HALF_UP).equals(new BigDecimal(100).setScale(2, RoundingMode.HALF_UP))) {
		                        			throw new ServiceException(messageSource.getMessage("import_validation_error_customer_fraction_percent_total", new Object[]{counter}, LocaleContextHolder.getLocale()));
		                        		}
		                        		
	                        			for(ExpenseGroupImport group : groups) {
	                        				Customer customer = null;
	                        				ExpenseGroupParts customerContract = splitGroupForParent(group.getGroup());
	                        				String customerName = customerContract.getParent();
	                        				if(customerName != null && customerName.contains("','")) customerName = customerName.replaceAll("','", ",");
	                        				String jobNumber = customerContract.getChild();
	                        				
	                        				if(StringUtils.isBlank(customerName) || StringUtils.isBlank(jobNumber)) {
	                        					throw new ServiceException(messageSource.getMessage("import_validation_error_customer_and_job_number_required", new Object[]{counter}, LocaleContextHolder.getLocale()));
	                        				}
	                        				
	    		                            List<Customer> customers = contractDaoService.findCustomerByName(customerName);

	    		                            if (customers == null || customers.size() < 1) {
	    		                                throw new ServiceException(messageSource.getMessage("import_customer_not_found_for_name",
	    		                                        new Object[]{group.getGroup(), counter}, LocaleContextHolder.getLocale()));
	    		                            } else if (customers.size() > 1) {
	    		                                throw new ServiceException(messageSource.getMessage("import_duplicate_customer_found_for_name",
	    		                                        new Object[]{group.getGroup(), counter}, LocaleContextHolder.getLocale()));
	    		                            } else {
	    		                                customer = customers.get(0);
	    		                            }
	    		                            
	    		                            if (customer == null) {
	    		                                throw new ServiceException(messageSource.getMessage("import_customer_not_found_for_name",
	    		                                        new Object[]{customerName, counter}, LocaleContextHolder.getLocale()));
	    		                            }
	    		                            
	    		                            
	    		                            
	    		                            if (!StringUtils.isBlank(jobNumber)) {
		    		                            if("spread".equals(jobNumber.toLowerCase())) {
		    		                            	//if they use spread, we'll go find the active contracts and spread it across them for the user
		    		                            	List<Contract> contracts = contractDaoService.findContractsByCustomerId(customer.getId(), Boolean.FALSE);
		    		                            	if(contracts != null && contracts.size() > 0) {
		    		                            		BigDecimal spreadPercent = group.getPercent();
		    		                            		
		    		                            		//whatever percent they've given to it, we divide that by the number of contracts
		    		                            		spreadPercent = spreadPercent.divide(new BigDecimal(contracts.size()), 2, RoundingMode.HALF_UP);
		    		                            		BigDecimal percentSplit = spreadPercent.divide(new BigDecimal(100));
				    		                            BigDecimal amountSplit = originalAmount.multiply(percentSplit).setScale(2, RoundingMode.HALF_UP);
				    		                            
		    		                            		for(Contract contract: contracts) {
		    		                            			MonthlyCostImportRecordHolder newRec = new MonthlyCostImportRecordHolder(rec);
				    		                            	newRec.setCustomer(customer);
				    		                            	newRec.setContract(contract);
					    		                            newRec.setAmount(amountSplit);
					    		                            toinsert.add(newRec);
		    		                            		}
		    		                            	} else {
		    		                            		throw new ServiceException(messageSource.getMessage("import_costs_no_contracts_found_for_customer",
		    		                                            new Object[]{customer.getName(), counter}, LocaleContextHolder.getLocale()));
		    		                            	}
		    		                            	
		    		                            } else {
		    		                            	//if they've entered specific job numbers for customers
		    		                            	MonthlyCostImportRecordHolder newRec = new MonthlyCostImportRecordHolder(rec);
		    		                            	newRec.setCustomer(customer);
		    		                            	if (!StringUtils.isBlank(jobNumber)) {
			    		                                Contract contract = contractDaoService.findContractByJobNumberAndCompanyId(jobNumber, customer.getId());
			    		                                if (contract == null) {
			    		                                    throw new ServiceException(messageSource.getMessage("import_contract_not_found_for_job_number_customer",
			    		                                            new Object[]{customer.getName(), jobNumber, counter}, LocaleContextHolder.getLocale()));
			    		                                } else {
			    		                                    newRec.setContract(contract);
			    		                                }
			    		                            }
		    		                            	
		    		                            	BigDecimal percentSplit = group.getPercent().divide(new BigDecimal(100));
			    		                            BigDecimal amountSplit = originalAmount.multiply(percentSplit).setScale(2, RoundingMode.HALF_UP);
			    		                            newRec.setAmount(amountSplit);
			    		                            
			    		                            //if there is a customer, we split and add the record here. otherwise, we add it outside this loop
			    		                            toinsert.add(newRec);
		    		                            }
	    		                            } else {
	    		                            	throw new ServiceException(messageSource.getMessage("import_job_number_for_contract_required",
    		                                            new Object[]{customer.getId(), counter}, LocaleContextHolder.getLocale()));
	    		                            }
		                        		}
	                        			break;
		                        	}
		                        }
	                        }
	                        
	                        //rec.validate(counter, messageSource, LocaleContextHolder.getLocale());
	
	                        log.info(rec.toString());
	                        toinsert.add(rec);
                        }
                    } else {
                        log.debug("empty row...");
                    }
            }
            //if (emptyRow) {
            if (emptyRowCounter > 18) {
            	log.info("Leaving loop");
                break; // get out of row loop
            }
        }
        int idx = 1;
        for (MonthlyCostImportRecordHolder rec : toinsert) {
        	log.info("About to import record: " + rec.toString());
        	
        	if(!rec.getUnallocatedExpense()) {
	            CostItem costItem = new CostItem();
	            Expense expense = new Expense();
	
	            String costItemName = rec.getThirdParty() + " - " + rec.getDescription();
	            costItem.setName(costItemName);
	            
	            String invoiceNumber = "";
	            String poNumber = "";
	            String notes = "";
	            
	            if(rec.getInvoiceNumber() != null) invoiceNumber = rec.getInvoiceNumber();
	            if(rec.getPoNumber() != null) poNumber = rec.getPoNumber();
	            if(rec.getNotes() != null) notes = rec.getNotes();
	            
	            String costItemDescription = "Inv#: " + invoiceNumber + ", PO#: " + poNumber + ", Notes: " + notes;
	            costItem.setDescription(costItemDescription);
	            BigDecimal costItemAmount = rec.getAmount();
	            costItem.setAmount(costItemAmount);
	            costItem.setApplied(expenseDate);
	            costItem.setCostType(CostItem.CostType.general);
	            costItem.setCostFractions(rec.getCostFractions());
	            if (rec.getCustomer() != null) {
	                costItem.setCustomerId(rec.getCustomer().getId());
	                
	                if (rec.getContract() != null) {
	                    costItem.setContractId(rec.getContract().getId());
	                }
	            }
	            
	            String costSubTypeStr = rec.getCostSubType();
	            if(!StringUtils.isEmpty(costSubTypeStr)) {
	            	costSubTypeStr = costSubTypeStr.toLowerCase();
	            	
	            	//we validate this sub type is valid earlier in the import
	            	CostItem.CostSubType costSubType = CostItem.CostSubType.valueOf(costSubTypeStr);
            		costItem.setCostSubType(costSubType);
            		
            		//if they include sub type, then we'll set the type to depreciated
            		costItem.setCostType(CostItem.CostType.depreciated);
	            }
	            
	            //set expense record
	            expense.setExpenseType(Expense.ExpenseType.cost);
	            expense.setName(costItemName);
	            expense.setDescription(costItemDescription);
	            expense.setAmount(costItemAmount);
	            expense.setQuantity(1);
	            costItem.setExpense(expense);
	
	            //save cost record
	            try {
	                Long ciid = contractDaoService.saveCostItem(costItem);
	            } catch (ServiceException se) {
	                throw new ServiceException(messageSource.getMessage("import_costs_save_failed",
	                        new Object[]{se.getMessage(), idx}, LocaleContextHolder.getLocale()));
	            }
        	} else {
        		UnallocatedExpense ue = new UnallocatedExpense();
        		String costItemName = rec.getThirdParty() + " - " + rec.getDescription();
        		ue.setName(costItemName);
	            
	            String invoiceNumber = "";
	            String poNumber = "";
	            String notes = "";
	            
	            if(rec.getInvoiceNumber() != null) invoiceNumber = rec.getInvoiceNumber();
	            if(rec.getPoNumber() != null) poNumber = rec.getPoNumber();
	            if(rec.getNotes() != null) notes = rec.getNotes();
	            
	            String UnallocatedExpenseDescription = "Inv#: " + invoiceNumber + ", PO#: " + poNumber + ", Notes: " + notes;
	            ue.setDescription(UnallocatedExpenseDescription);
	            ue.setMonth(expenseDate);
	            ue.setPoNumber(poNumber);
	            
	            BigDecimal costItemAmount = rec.getAmount();
	            for(ServiceFraction serviceFraction: rec.getServiceFractions()) {
	            	BigDecimal fractionAmount = costItemAmount.multiply(serviceFraction.getFraction()).divide(new BigDecimal(100));
	            	ue.setOspId(serviceFraction.getOspId());
	            	ue.setAmount(fractionAmount);
	            	
	            	try {
	            		saveUnallocatedExpense(ue);
	            	} catch (ServiceException se) {
	            		throw new ServiceException(messageSource.getMessage("import_costs_save_failed",
		                        new Object[]{se.getMessage(), idx}, LocaleContextHolder.getLocale()));
	            	}
	            }
        	}
            
            idx++;
        }
    }


    private void importAssets(Iterator<Row> rowIterator, boolean emptyRow, DataFormatter poiFmt) throws ServiceException {
        int counter = 0;
        List<AssetImportRecordHolder> toinsert = new ArrayList<AssetImportRecordHolder>();
        while (rowIterator.hasNext()) {
            AssetImportRecordHolder rec;
            Row row = rowIterator.next();
            switch (row.getRowNum()) {
                case 0:
                    counter++;
                    // header
                    break;
                default:
                    emptyRow = true;
                    for (Cell c : row) {
                        if (c.getCellType() != Cell.CELL_TYPE_BLANK) {
                            emptyRow = false;
                        }
                    }
                    if (!emptyRow) {
                        // read asset records
                        counter++;
                        rec = new AssetImportRecordHolder();
                        Iterator<Cell> rowColumns = row.cellIterator();
                        while (rowColumns.hasNext()) {
                            Cell rowCell = rowColumns.next();
                            int columnIndex = rowCell.getColumnIndex();
                            try {
                                switch (columnIndex) {
                                    case 0:
                                        rec.setLocationName(rowCell.getStringCellValue());
                                        break;
                                    case 1:
                                        rec.setName(rowCell.getStringCellValue());
                                        break;
                                    case 2:
                                        rec.setDescription(rowCell.getStringCellValue());
                                        break;
                                    case 3:
                                        rec.setAmount(new BigDecimal(rowCell.getNumericCellValue()));
                                        break;
                                    case 4:
                                        rec.setAcquired(rowCell.getDateCellValue());
                                        break;
                                    case 5:
                                        rec.setDisposal(rowCell.getDateCellValue());
                                        break;
                                    case 6:
                                        rec.setLife(getIntFromDouble(rowCell.getNumericCellValue()));
                                        break;
                                    case 7:
                                        rowCell.setCellType(Cell.CELL_TYPE_STRING); // force String input for expected integer value
                                        if (StringUtils.isNotBlank(rowCell.getStringCellValue())) {
                                            rec.setAssetNumber(rowCell.getStringCellValue());
                                        }
                                        break;
                                    case 8:
                                        rowCell.setCellType(Cell.CELL_TYPE_STRING); // force String input for expected integer value
                                        if (StringUtils.isNotBlank(rowCell.getStringCellValue())) {
                                            rec.setSku(rowCell.getStringCellValue());
                                        }
                                        break;
                                    case 9:
                                        rec.setCostFractionCategoryOne(rowCell.getStringCellValue());
                                        break;
                                    case 10:
                                        rec.setCostFractionUnitOne(getIntFromDouble(rowCell.getNumericCellValue()));
                                        break;
                                    case 11:
                                        rec.setCostFractionPercentOne(convertNumericToPercent(new BigDecimal(rowCell.getNumericCellValue())));
                                        break;
                                    case 12:
                                        rec.setCostFractionCategoryTwo(rowCell.getStringCellValue());
                                        break;
                                    case 13:
                                        rec.setCostFractionUnitTwo(getIntFromDouble(rowCell.getNumericCellValue()));
                                        break;
                                    case 14:
                                        rec.setCostFractionPercentTwo(convertNumericToPercent(new BigDecimal(rowCell.getNumericCellValue())));
                                        break;
                                    case 15:
                                        rec.setCostFractionCategoryThree(rowCell.getStringCellValue());
                                        break;
                                    case 16:
                                        rec.setCostFractionUnitThree(getIntFromDouble(rowCell.getNumericCellValue()));
                                        break;
                                    case 17:
                                        rec.setCostFractionPercentThree(convertNumericToPercent(new BigDecimal(rowCell.getNumericCellValue())));
                                        break;
                                    case 18:
                                        rec.setCostFractionCategoryFour(rowCell.getStringCellValue());
                                        break;
                                    case 19:
                                        rec.setCostFractionUnitFour(getIntFromDouble(rowCell.getNumericCellValue()));
                                        break;
                                    case 20:
                                        rec.setCostFractionPercentFour(convertNumericToPercent(new BigDecimal(rowCell.getNumericCellValue())));
                                        break;
                                    case 21:
                                        rec.setCostFractionCategoryFive(rowCell.getStringCellValue());
                                        break;
                                    case 22:
                                        rec.setCostFractionUnitFive(getIntFromDouble(rowCell.getNumericCellValue()));
                                        break;
                                    case 23:
                                        rec.setCostFractionPercentFive(convertNumericToPercent(new BigDecimal(rowCell.getNumericCellValue())));
                                        break;
                                    case 24:
                                        rec.setCustomerName(rowCell.getStringCellValue());
                                        break;
                                    case 25:
                                        rec.setContractJobNumber(rowCell.getStringCellValue());
                                        break;
                                    default:
                                    // foo
                                }
                            } catch (IllegalStateException e) {
                                throw new IllegalStateException(e.getMessage() + " at Row: " + counter + ", Column: " + columnIndex);
                            }
                        }

                        rec.validate(counter, messageSource, LocaleContextHolder.getLocale());
                        String customerName = rec.getCustomerName();
                        if (!StringUtils.isBlank(customerName)) {
                            Customer customer = null;
                            List<Customer> customers = contractDaoService.findCustomerByName(rec.getCustomerName());
                            if (customers == null || customers.size() < 1) {
                                throw new ServiceException(messageSource.getMessage("import_customer_not_found_for_name",
                                        new Object[]{rec.getCustomerName(), counter}, LocaleContextHolder.getLocale()));
                            } else if (customers.size() > 1) {
                                throw new ServiceException(messageSource.getMessage("import_duplicate_customer_found_for_name",
                                        new Object[]{rec.getCustomerName(), counter}, LocaleContextHolder.getLocale()));
                            } else {
                                customer = customers.get(0);
                            }
                            rec.setCustomer(customer);


                            String contractJobNumber = rec.getContractJobNumber();
                            if (!StringUtils.isBlank(contractJobNumber)) {
                                Contract contract = contractDaoService.findContractByJobNumberAndCompanyId(contractJobNumber, customer.getId());
                                if (contract == null) {
                                    throw new ServiceException(messageSource.getMessage("import_contract_not_found_for_job_number_customer",
                                            new Object[]{customer.getId(), contractJobNumber, counter}, LocaleContextHolder.getLocale()));
                                } else {
                                    rec.setContract(contract);
                                }
                            }
                        }

                        String locationName = rec.getLocationName();
                        if (!StringUtils.isBlank(locationName)) {
                            Location location = applicationDataDaoService.findLocationByName(locationName);
                            if (location == null) {
                                throw new ServiceException(messageSource.getMessage("import_location_not_found_for_name", new Object[]{locationName, counter}, LocaleContextHolder.getLocale()));
                            } else {
                                rec.setLocation(location);
                            }
                        }

                        AssetCostFraction aifOne = new AssetCostFraction();
                        ExpenseGroupParts categoriesOne = splitGroupForParent(rec.getCostFractionCategoryOne());
                        ExpenseCategory expenseCategoryOne = expenseCategoryByName(categoriesOne.getChild(), categoriesOne.getParent());
                        if (expenseCategoryOne == null) {
                            throw new ServiceException(messageSource.getMessage("expensecategory_not_found_for_name", new Object[]{rec.getCostFractionCategoryOne(), counter}, LocaleContextHolder.getLocale()));
                        } else {
                            aifOne.setExpenseCategory(expenseCategoryOne);
                            aifOne.setFraction(rec.getCostFractionPercentOne());
                            aifOne.setQuantity(rec.getCostFractionUnitOne());
                            rec.addCostFraction(aifOne);
                        }

                        String costFractionCategoryTwo = rec.getCostFractionCategoryTwo();
                        if (!StringUtils.isBlank(costFractionCategoryTwo)) {
                            AssetCostFraction aifTwo = new AssetCostFraction();
                            ExpenseGroupParts categoriesTwo = splitGroupForParent(rec.getCostFractionCategoryTwo());
                            ExpenseCategory expenseCategoryTwo = expenseCategoryByName(categoriesTwo.getChild(), categoriesTwo.getParent());
                            if (expenseCategoryTwo == null) {
                                throw new ServiceException(messageSource.getMessage("expensecategory_not_found_for_name", new Object[]{costFractionCategoryTwo, counter}, LocaleContextHolder.getLocale()));
                            } else {
                                aifTwo.setExpenseCategory(expenseCategoryTwo);
                                aifTwo.setFraction(rec.getCostFractionPercentTwo());
                                aifTwo.setQuantity(rec.getCostFractionUnitTwo());
                                rec.validateCostFraction(aifTwo, counter, messageSource, LocaleContextHolder.getLocale());
                                rec.addCostFraction(aifTwo);
                            }
                        }

                        String costFractionCategoryThree = rec.getCostFractionCategoryThree();
                        if (!StringUtils.isBlank(costFractionCategoryThree)) {
                            AssetCostFraction aifThree = new AssetCostFraction();
                            ExpenseGroupParts categoriesThree = splitGroupForParent(rec.getCostFractionCategoryThree());
                            ExpenseCategory expenseCategoryThree = expenseCategoryByName(categoriesThree.getChild(), categoriesThree.getParent());
                            if (expenseCategoryThree == null) {
                                throw new ServiceException(messageSource.getMessage("expensecategory_not_found_for_name", new Object[]{costFractionCategoryThree, counter}, LocaleContextHolder.getLocale()));
                            } else {
                                aifThree.setExpenseCategory(expenseCategoryThree);
                                aifThree.setFraction(rec.getCostFractionPercentThree());
                                aifThree.setQuantity(rec.getCostFractionUnitThree());
                                rec.validateCostFraction(aifThree, counter, messageSource, LocaleContextHolder.getLocale());
                                rec.addCostFraction(aifThree);
                            }
                        }

                        String costFractionCategoryFour = rec.getCostFractionCategoryFour();
                        if (!StringUtils.isBlank(costFractionCategoryFour)) {
                            AssetCostFraction aifFour = new AssetCostFraction();
                            ExpenseGroupParts categoriesFour = splitGroupForParent(rec.getCostFractionCategoryFour());
                            ExpenseCategory expenseCategoryFour = expenseCategoryByName(categoriesFour.getChild(), categoriesFour.getParent());
                            if (expenseCategoryFour == null) {
                                throw new ServiceException(messageSource.getMessage("expensecategory_not_found_for_name", new Object[]{costFractionCategoryFour, counter}, LocaleContextHolder.getLocale()));
                            } else {
                                aifFour.setExpenseCategory(expenseCategoryFour);
                                aifFour.setFraction(rec.getCostFractionPercentFour());
                                aifFour.setQuantity(rec.getCostFractionUnitFour());
                                rec.validateCostFraction(aifFour, counter, messageSource, LocaleContextHolder.getLocale());
                                rec.addCostFraction(aifFour);
                            }
                        }

                        String costFractionCategoryFive = rec.getCostFractionCategoryFive();
                        if (!StringUtils.isBlank(costFractionCategoryFive)) {
                            AssetCostFraction aifFive = new AssetCostFraction();
                            ExpenseGroupParts categoriesFive = splitGroupForParent(rec.getCostFractionCategoryFive());
                            ExpenseCategory expenseCategoryFive = expenseCategoryByName(categoriesFive.getChild(), categoriesFive.getParent());
                            if (expenseCategoryFive == null) {
                                throw new ServiceException(messageSource.getMessage("expensecategory_not_found_for_name", new Object[]{costFractionCategoryFive, counter}, LocaleContextHolder.getLocale()));
                            } else {
                                aifFive.setExpenseCategory(expenseCategoryFive);
                                aifFive.setFraction(rec.getCostFractionPercentFive());
                                aifFive.setQuantity(rec.getCostFractionUnitFive());
                                rec.validateCostFraction(aifFive, counter, messageSource, LocaleContextHolder.getLocale());
                                rec.addCostFraction(aifFive);
                            }
                        }


                        log.info(rec.toString());
                        toinsert.add(rec);
                    } else {
                        log.debug("empty row...");
                    }
            }
            if (emptyRow) {
                break; // get out of row loop
            }
        }
        int idx = 1;
        for (AssetImportRecordHolder rec : toinsert) {
            AssetItem assetItem = new AssetItem();
            Expense expense = new Expense();

            String assetItemName = rec.getName();
            assetItem.setName(assetItemName);
            String assetItemDescription = rec.getDescription();
            assetItem.setDescription(assetItemDescription);
            BigDecimal assetItemAmount = rec.getAmount();
            assetItem.setAmount(assetItemAmount);
            assetItem.setAcquired(rec.getAcquired());
            assetItem.setDisposal(rec.getDisposal());
            assetItem.setLife(rec.getLife());
            assetItem.setSku(rec.getSku());
            assetItem.setPartNumber(rec.getAssetNumber());
            assetItem.setAssetCostFractions(rec.getCostFractions());
            if (rec.getCustomer() != null) {
                assetItem.setCustomerId(rec.getCustomer().getId());

                if (rec.getContract() != null) {
                    assetItem.setContractId(rec.getContract().getId());
                }
            }
            if (rec.getLocation() != null) {
                assetItem.setLocationId(rec.getLocation().getId());
                expense.setLocationId(rec.getLocation().getId());
            }

            //set expense record
            expense.setExpenseType(Expense.ExpenseType.asset);
            expense.setName(assetItemName);
            expense.setDescription(assetItemDescription);
            expense.setAmount(assetItemAmount);
            expense.setQuantity(1);
            assetItem.setExpense(expense);

            //save asset record
            try {
                Long aiid = contractDaoService.saveAssetItem(assetItem);
            } catch (ServiceException se) {
                throw new ServiceException(messageSource.getMessage("import_costs_save_failed",
                        new Object[]{se.getMessage(), idx}, LocaleContextHolder.getLocale()));
            }
            idx++;
        }
    }

    private Integer getIntFromDouble(Double cellValue) {
        Integer intValue = null;
        Double doubleValue = cellValue;
        if (doubleValue != null) {
            intValue = doubleValue.intValue();
        }
        return intValue;
    }

    private BigDecimal convertNumericToPercent(BigDecimal base) {
        return base.multiply(new BigDecimal(100));
    }

    @Override
    public List<UnitCost> unitCosts() {
        try {
            return jdbcTemplate.query("select * from unit_cost"
                    + " order by expense_category_id asc, applied_date asc", new RowMapper<UnitCost>() {
                @Override
                public UnitCost mapRow(ResultSet rs, int i) throws SQLException {
                    return new UnitCost(rs.getLong("id"),
                            (rs.getLong("customer_id") == 0 ? null : rs.getLong("customer_id")),
                            rs.getInt("expense_category_id"),
                            rs.getBigDecimal("total_cost"),
                            rs.getBigDecimal("total_labor"),
                            rs.getInt("device_total_units"),
                            new DateTime(rs.getDate("applied_date")));
                }
            });
        } catch (Exception ignore) {
        }
        return null;
    }
    
    @Override
    public UnitCost customCostByExpenseCategoryAndDate(Long customerId, Integer expenseCategoryId, DateTime appliedDate, List<Map<String, String>> costTypes) {

        DateTime startDate = appliedDate
                .withZone(DateTimeZone.forID(TZID))
                .withDayOfMonth(1)
                .withTimeAtStartOfDay();
        DateTime endDate = startDate
                .plusMonths(1)
                .minusDays(1)
                .withTime(23, 59, 59, 999);
        Integer deviceTotalUnits = 0;
        try {
            deviceTotalUnits = costService.deviceTotalDeviceCountWithExpenseCategory(startDate, endDate, null, customerId, null, expenseCategoryId, null);
        } catch(ServiceException se) {
            log.debug("error calculating deviceTotalUnits", se);
            // the method doesn't ever throw that exception...
        }
        
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("expenseCategoryId", expenseCategoryId);
        params.put("leftDate", startDate.toDate());
        params.put("rightDate", endDate.toDate());
        // we need to find the total cost based on limited CostItem.CostType/SubType records
        String query = "select sum(ci.amount * cif.cost_fraction / 100) as 'Cost'"
                + " from cost_item ci"
                + " inner join cost_item_fraction cif on ci.id = cif.cost_item_id";
        if (customerId != null) {
            params.put("customerId", customerId);
            query += " inner join customer cu on ci.customer_id = cu.id";
        }
        query += " inner join expense_category exp on exp.id = cif.expense_category_id"
                + " left outer join expense_category pexp on exp.parent_id = pexp.id"
                + " where ci.applied between :leftDate and :rightDate"
                + " and exp.id = :expenseCategoryId";
        if (customerId != null) {
            query += " and cu.id = :customerId";
        } else {
            query += " and ci.customer_id is null";
        }
        if (costTypes != null && !costTypes.isEmpty()) {
            for (Map<String, String> member : costTypes) {
                String costType = member.get("CostType");
                String costSubType = member.get("CostSubType");
                query += " and NOT (ci.cost_type = '" + costType + "' AND ci.cost_subtype = '" + costSubType + "')";
            }
        }
        BigDecimal totalCost = namedJdbcTemplate.queryForObject(query, params, BigDecimal.class);
        if (deviceTotalUnits < 1 || totalCost == null) {
            return null;
        }
        return new UnitCost(expenseCategoryId, customerId, deviceTotalUnits, totalCost, startDate);
    }

    @Override
    public Long saveUnitCost(UnitCost uc) throws ServiceException {
        if (uc.getId() != null) {
            updateUnitCost(uc);
            return uc.getId();
        }
        try {
            SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
            jdbcInsert.withTableName("unit_cost").usingGeneratedKeyColumns("id");
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("customer_id", uc.getCustomerId());
            params.put("expense_category_id", uc.getExpenseCategoryId());
            params.put("total_cost", uc.getTotalCost());
            params.put("total_labor", uc.getTotalLabor());
            params.put("device_total_units", uc.getDeviceTotalUnits());
            params.put("applied_date", uc.getAppliedDate().toDate());
            Number pk = jdbcInsert.executeAndReturnKey(new MapSqlParameterSource(params));
            return (Long) pk;
        } catch (Exception ignore) {
            log.warn("An error occurred inserting a UnitCost", ignore);
        }
        return null;
    }

    @Override
    public void updateUnitCost(UnitCost uc) throws ServiceException {
        if (uc.getId() == null) {
            throw new ServiceException(messageSource.getMessage("validation_error_unitcost_id", null, LocaleContextHolder.getLocale()));
        }
        try {
            int result = jdbcTemplate.update("update unit_cost set customer_id = ?, expense_category_id = ?, total_cost = ?, total_labor = ?, device_total_units = ?, applied_date = ? where id = ?",
                    new Object[]{uc.getCustomerId(), uc.getExpenseCategoryId(), uc.getTotalCost(), uc.getTotalLabor(), uc.getDeviceTotalUnits(), uc.getAppliedDate().toDate(), uc.getId()});
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_unitcost_update",
                    new Object[]{uc.getId(), any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    }

    @Override
    public List<UnitCost> allUnitCostByExpenseCategory(Integer expenseCategoryId) {
        try {
            return jdbcTemplate.query("select * from unit_cost where expense_category_id = ?", new RowMapper<UnitCost>() {
                @Override
                public UnitCost mapRow(ResultSet rs, int i) throws SQLException {
                    return new UnitCost(rs.getLong("id"),
                            (rs.getLong("customer_id") == 0 ? null : rs.getLong("customer_id")),
                            rs.getInt("expense_category_id"),
                            rs.getBigDecimal("total_cost"),
                            rs.getBigDecimal("total_labor"),
                            rs.getInt("device_total_units"),
                            new DateTime(rs.getDate("applied_date")));
                }
            }, new Object[]{expenseCategoryId});
        } catch (Exception ignore) {
        }
        return null;
    }
    
    @Override
    public List<UnitCost> unitCostByExpenseCategory(Long customerId, Integer expenseCategoryId) {
        String query = "select * from unit_cost"
                    + " where expense_category_id = ?";
        if (customerId != null && customerId > 0) {
            query += " and customer_id = ?";
        } else {
            query += " and customer_id is null";
        }
        try {
            return jdbcTemplate.query(query, new RowMapper<UnitCost>() {
                @Override
                public UnitCost mapRow(ResultSet rs, int i) throws SQLException {
                    return new UnitCost(rs.getLong("id"),
                            (rs.getLong("customer_id") == 0 ? null : rs.getLong("customer_id")),
                            rs.getInt("expense_category_id"),
                            rs.getBigDecimal("total_cost"),
                            rs.getBigDecimal("total_labor"),
                            rs.getInt("device_total_units"),
                            new DateTime(rs.getDate("applied_date")));
                }
            }, new Object[]{expenseCategoryId, customerId});
        } catch (Exception ignore) {
        }
        return null;
    }

    @Override
    public UnitCost unitCostByExpenseCategoryAndDate(Long customerId, Integer expenseCategoryId, DateTime costDate) {

        if (costDate == null) {
            throw new IllegalArgumentException("A costDate is required");
        }
        if (expenseCategoryId == null) {
            throw new IllegalArgumentException("Expense Category ID is required");
        }
        DateTime appliedDate = costDate
                .withZone(DateTimeZone.forID(TZID))
                .withDayOfMonth(1) // default is beginning of year...
                .withTimeAtStartOfDay();
        
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("appliedDate", appliedDate.toDate());
        String query = "select *"
                + " from unit_cost uc"
                + " where uc.expense_category_id = :expenseCategoryId"
                + " and uc.applied_date = :appliedDate";
        if (customerId != null && customerId > 0) {
            query += " and uc.customer_id = :customerId";
            params.put("customerId", customerId);
            } else {
            query += " and uc.customer_id is null";
        }
        params.put("expenseCategoryId", expenseCategoryId);
        UnitCost unitCost = null;
        try {
            unitCost = namedJdbcTemplate.queryForObject(query, params, new RowMapper<UnitCost>() {
                @Override
                public UnitCost mapRow(ResultSet rs, int i) throws SQLException {
                    return new UnitCost(
                    rs.getLong("id"),
                    (rs.getLong("customer_id") == 0 ? null : rs.getLong("customer_id")),
                    rs.getInt("expense_category_id"),
                    rs.getBigDecimal("total_cost"),
                    rs.getBigDecimal("total_labor"),
                    rs.getInt("device_total_units"),
                    new DateTime(rs.getDate("applied_date")));
                }
            });
        } catch (EmptyResultDataAccessException ignore) {
        }
        return unitCost;
    }
    
    @Override
    public Map<String, UnitCost> unitCostByExpenseCategoryAndDateRange(Long customerId, Integer expenseCategoryId, DateTime startDate, DateTime endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("start date and end date are required");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("start date must be before end date");
        }
        Map<String, UnitCost> results = new TreeMap<String, UnitCost>(new Comparator<String>() {

            @Override
            public int compare(String o1, String o2) {
                DateTime d1 = DateTimeFormat.forPattern("MM/yyyy").parseDateTime(o1);
                DateTime d2 = DateTimeFormat.forPattern("MM/yyyy").parseDateTime(o2);
                return d1.compareTo(d2);
            }
        });
        while (Months.monthsBetween(startDate, endDate).getMonths() >= 0) {
            String key = DateTimeFormat.forPattern("MM/yyyy").print(startDate);
            UnitCost value = unitCostByExpenseCategoryAndDate(customerId, expenseCategoryId, startDate);
            results.put(key, value);
            startDate = startDate.plusMonths(1);
        }
        return results;
    }

    /**
     * @deprecated no longer use the service_expense_category table
     * @param ospId
     * @return 
     */
    @Override
    public List<ServiceExpenseCategory> serviceExpenseCategories(Long ospId) {
        return jdbcTemplate.query("select svc.id service_id, svc.osp_id, svc.business_model, sec.expense_category_id, sec.quantity"
                + " from service svc left outer join service_expense_category sec on svc.osp_id = sec.osp_id"
                + " where svc.osp_id = ? and svc.active = true order by sec.expense_category_id",
                new Object[]{ospId}, new RowMapper<ServiceExpenseCategory>() {
            @Override
            public ServiceExpenseCategory mapRow(ResultSet rs, int i) throws SQLException {
                return new ServiceExpenseCategory(
                        rs.getInt("expense_category_id"),
                        rs.getLong("osp_id"),
                        rs.getString("business_model"),
                        rs.getLong("service_id"),
                        rs.getInt("quantity"));
            }
        });
    }
    
    @Override
    public void saveOrUpdateDeviceCostMappings(Long deviceId, List<DeviceExpenseCategory> costMappings) throws ServiceException {
        
        List<DeviceExpenseCategory> affectedCostMappings = new ArrayList<DeviceExpenseCategory>();
        List<DeviceExpenseCategory> existingCostMappings = applicationDataDaoService.findCostMappingsForDevice(deviceId);
        String query = "delete from device_expense_category where device_id = ?";
        try {
            jdbcTemplate.update(query, deviceId);
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_device_cost_mapping_delete", new Object[]{deviceId, any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
        
        costMappings = uniqueListOf(costMappings);
        if (costMappings != null && costMappings.size() > 0) {
            List<Object[]> inserts = new ArrayList<Object[]>();
            String update = "insert into device_expense_category (device_id, expense_category_id, quantity, allocation_category) values (?, ?, ?, ?)";
            for (DeviceExpenseCategory costMapping : costMappings) {
                // validation
                if (costMapping.getDeviceId() == null) continue;
                if (!deviceId.equals(costMapping.getDeviceId())) {
                    throw new ServiceException(messageSource.getMessage("jdbc_error_device_cost_mapping_mismatch", new Object[]{deviceId}, LocaleContextHolder.getLocale()));
                }
                if (costMapping.getExpenseCategoryId() == null) continue;
                if (costMapping.getQuantity() == null || costMapping.getQuantity() <= 1) costMapping.setQuantity(1);
                // end validation
                inserts.add(new Object[]{deviceId, costMapping.getExpenseCategoryId(), costMapping.getQuantity(), costMapping.getAllocationCategory()});
                if (!existingCostMappings.contains(costMapping)) {
                    affectedCostMappings.add(costMapping); // a new mapping
                }
            }
            jdbcTemplate.batchUpdate(update, inserts);
        }
        for (DeviceExpenseCategory costMapping : existingCostMappings) {
            if (!costMappings.contains(costMapping)) {
                affectedCostMappings.add(costMapping); // a removed mapping
            }
        }
        // modify service counts on NEW and REMOVED mappings
        for (DeviceExpenseCategory affectedCostMapping : affectedCostMappings) {
            log.debug("recalculating unit cost for affected expense category [{}]", affectedCostMapping.getExpenseCategoryId());
            List<UnitCost> affectedUnitCosts = allUnitCostByExpenseCategory(affectedCostMapping.getExpenseCategoryId());
            if(affectedUnitCosts != null) {
	            for (UnitCost uc : affectedUnitCosts) {
	                Integer deviceTotalCount = costService.deviceTotalDeviceCountWithExpenseCategory(
	                        uc.getAppliedDate(), uc.getAppliedDate().plusMonths(1).minusDays(1).withTime(23, 59, 59, 999),
	                        null, uc.getCustomerId(), null, affectedCostMapping.getExpenseCategoryId(), null);
	                uc.setDeviceTotalUnits(deviceTotalCount);
	                updateUnitCost(uc);
	            }
            }
        }
    }
    
    private <T> List<T> uniqueListOf(List<T> objects) {
        if (objects != null && objects.size() > 0) {
            Set<T> uniqueSet = new HashSet<T>();
            for (T object : objects) {
                uniqueSet.add(object);
            }
            return new ArrayList<T>(uniqueSet);
        }
        return new ArrayList<T>();
    }

    private void validateServiceExpenseCategories(List<ServiceExpenseCategory> data) throws ServiceException {
        Long serviceId = -1L;
        for (ServiceExpenseCategory item : data) {
            Integer count = 0;
            if (item.getExpenseCategoryId() != null) {
                count = jdbcTemplate.queryForObject("select count(*) from expense_category where id = ?", Integer.class, item.getExpenseCategoryId());
                if (!count.equals(1)) {
                    throw new ServiceException(messageSource.getMessage("expensecategory_not_found_for_id", new Object[]{item.getExpenseCategoryId()}, LocaleContextHolder.getLocale()));
                }
            }
            if (serviceId == -1) {
                count = jdbcTemplate.queryForObject("select count(*) from service where id = ?", Integer.class, item.getServiceOfferingId());
                if (!count.equals(1)) {
                    throw new ServiceException(messageSource.getMessage("service_not_found_for_id", new Object[]{item.getServiceOfferingId()}, LocaleContextHolder.getLocale()));
                }
                serviceId = item.getServiceOfferingId();
            } else if (!serviceId.equals(item.getServiceOfferingId())) {
                throw new ServiceException(messageSource.getMessage("service_ids_collection_mismatch", new Object[]{serviceId}, LocaleContextHolder.getLocale()));
            }
            if ((item.getQuantity() == null || item.getQuantity() == 0) && item.getExpenseCategoryId() != null) {
                throw new ServiceException(messageSource.getMessage("validation_error_service_expense_category_quantity", null, LocaleContextHolder.getLocale()));
            }
        }
    }

    private void validateDeviceExpenseCategories(List<DeviceExpenseCategory> data) throws ServiceException {
        Long deviceId = -1L;
        for (DeviceExpenseCategory item : data) {
            Integer count = 0;
            if (item.getExpenseCategoryId() != null) {
                count = jdbcTemplate.queryForObject("select count(*) from expense_category where id = ?", Integer.class, item.getExpenseCategoryId());
                if (!count.equals(1)) {
                    throw new ServiceException(messageSource.getMessage("expensecategory_not_found_for_id", new Object[]{item.getExpenseCategoryId()}, LocaleContextHolder.getLocale()));
                }
            }
            if (deviceId == -1) {
                count = jdbcTemplate.queryForObject("select count(*) from device where id = ?", Integer.class, item.getDeviceId());
                if (!count.equals(1)) {
                    throw new ServiceException(messageSource.getMessage("device_not_found_for_id", new Object[]{item.getDeviceId()}, LocaleContextHolder.getLocale()));
                }
                deviceId = item.getDeviceId();
            } else if (!deviceId.equals(item.getDeviceId())) {
                throw new ServiceException(messageSource.getMessage("device_ids_collection_mismatch", new Object[]{deviceId}, LocaleContextHolder.getLocale()));
            }
            if ((item.getQuantity() == null || item.getQuantity() == 0) && item.getExpenseCategoryId() != null) {
                throw new ServiceException(messageSource.getMessage("validation_error_device_expense_category_quantity", null, LocaleContextHolder.getLocale()));
            }
        }
    }
    
    public List<Service> servicesForExpenseCategory(Integer expenseCategoryId) throws ServiceException {
    	return jdbcTemplate.query("select distinct svc.osp_id, svc.name service_name, svc.business_model business_model"
                + " from service svc inner join service_expense_category sec on svc.osp_id = sec.osp_id"
                + " where sec.expense_category_id = ? order by svc.name",
                new Object[]{expenseCategoryId}, new RowMapper<Service>() {
            @Override
            public Service mapRow(ResultSet rs, int i) throws SQLException {
                return new Service(
                		null,
                		null,
                		rs.getString("osp_id"),
                        null,
                        rs.getString("service_name"),
                        rs.getString("business_model"));
            }
        });
    }
    
    @Override
    public List<Device> devicesForExpenseCategory(Integer expenseCategoryId) throws ServiceException {
    	return jdbcTemplate.query("select distinct d.id, d.part_number, d.description"
                + " from device d inner join device_expense_category dcat on d.id = dcat.device_id"
                + " where dcat.expense_category_id = ? order by d.part_number",
                new Object[]{expenseCategoryId}, new RowMapper<Device>() {
            @Override
            public Device mapRow(ResultSet rs, int i) throws SQLException {
                return new Device(
                		rs.getLong("id"),
                		rs.getString("part_number"),
                		rs.getString("description"));
            }
        });
    }
    
    
    private static final String UNALLOCATED_EXPENSE_BASE_QUERY = "select ua.id, ua.alt_id, ua.name, ua.description, ua.amount, ua.quantity, ua.osp_id, s.name service_name, ua.vendor, "
    		+ " ua.po_number, ua.cost_allocation_ref_id, ua.month from unallocated_expense ua"
    		+ " inner join service s on ua.osp_id = s.osp_id";
    private static final String UNALLOCATED_EXPENSE_POST_QUERY = " group by ua.id";
    
    @Override
    public UnallocatedExpense unallocatedExpense(Long id) throws ServiceException {
        Integer count = jdbcTemplate.queryForObject("select count(*) from unallocated_expense where id = ?", Integer.class, id);
        if (!count.equals(1)) {
            throw new ServiceException(messageSource.getMessage("unallocated_expense_not_found_for_id", new Object[]{id}, LocaleContextHolder.getLocale()));
        }
        
        List<UnallocatedExpense> ues = jdbcTemplate.query(UNALLOCATED_EXPENSE_BASE_QUERY + " where ua.id = ?" + UNALLOCATED_EXPENSE_POST_QUERY, new Object[]{id}, new UnallocatedExpenseRowMapper());
        if(ues != null && ues.size() > 0) {
        	return ues.get(0);
        } else {
        	return null;
        }
    }
    
    @Override
    public Long saveUnallocatedExpense(UnallocatedExpense ue) throws ServiceException {
    	if(ue.getId() != null) {
    		updateUnallocatedExpense(ue);
    	}
    	
    	validateUnallocatedExpense(ue);
    	
        try {
            SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
            jdbcInsert.withTableName("unallocated_expense").usingGeneratedKeyColumns("id");
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("alt_id", ue.getAltId());
            params.put("name", ue.getName());
            params.put("description", ue.getDescription());
            params.put("amount", ue.getAmount());
            params.put("quantity", 1);
            params.put("osp_id", ue.getOspId());
            params.put("vendor", ue.getVendor());
            params.put("po_number", ue.getPoNumber());
            params.put("cost_allocation_ref_id", ue.getCostAllocationId());
            params.put("month", ue.getMonth());
            params.put("created", new Date());
            params.put("created_by", authenticatedUser());
            Number pk = jdbcInsert.executeAndReturnKey(new MapSqlParameterSource(params));
            return (Long) pk;
        } catch (Exception ignore) {
            log.warn("An error occurred inserting a UnallocatedCost", ignore);
        }
        return null;
    }
    
    public void updateUnallocatedExpense(UnallocatedExpense ue) throws ServiceException {
    	Integer count = jdbcTemplate.queryForObject("select count(*) from unallocated_expense where id = ?", Integer.class, ue.getId());
        if (!count.equals(1)) {
            throw new ServiceException(messageSource.getMessage("unallocated_expense_not_found_for_id", new Object[]{ue.getId()}, LocaleContextHolder.getLocale()));
        }
    	
    	validateUnallocatedExpense(ue);
    	
    	try {
            int updated = jdbcTemplate.update("update unallocated_expense set alt_id = ?,"
                    + " name = ?, description = ?, amount = ?, osp_id = ?, vendor = ?,"
                    + " po_number = ?, cost_allocation_ref_id = ?, month = ?, updated_by = ?, updated = ?"
                    + " where id = ?",
                    new Object[]{ue.getAltId(), ue.getName(), ue.getDescription(), ue.getAmount(), ue.getOspId(), ue.getVendor(), ue.getPoNumber(),
                    		ue.getCostAllocationId(), ue.getMonth(), authenticatedUser(), new DateTime().withZone(DateTimeZone.forID(TZID)).toDate(),
                ue.getId()});
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_contract_adjustment_update", new Object[]{ue.getId(), any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    }
    
    private void validateUnallocatedExpense(UnallocatedExpense ue) throws ServiceException {
    	if(StringUtils.isEmpty(ue.getName())) {
    		throw new ServiceException(messageSource.getMessage("validation_error_expense_name_required", null, LocaleContextHolder.getLocale()));
    	}
    	
    	if(ue.getOspId() == null) {
    		throw new ServiceException(messageSource.getMessage("validation_error_osp_service_required", null, LocaleContextHolder.getLocale()));
    	}
    	
    	if(ue.getAmount() == null) {
    		throw new ServiceException(messageSource.getMessage("validation_error_expense_amount_required", null, LocaleContextHolder.getLocale()));
    	}
    	
    	if(ue.getMonth() == null) {
    		throw new ServiceException(messageSource.getMessage("validation_error_expense_date_required", null, LocaleContextHolder.getLocale()));
    	}
    }
    
    public void deleteUnallocatedExpense(Long id) throws ServiceException {
    	Integer count = jdbcTemplate.queryForObject("select count(*) from unallocated_expense where id = ?", Integer.class, id);
        if (!count.equals(1)) {
            throw new ServiceException(messageSource.getMessage("contractservice_not_found_for_id", new Object[]{id}, LocaleContextHolder.getLocale()));
        }
        
        try {
            int deleted = jdbcTemplate.update("delete from unallocated_expense where id = ?", id);
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_contractservice_update", new Object[]{id, any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    }
    
    @Override
	public List<UnallocatedExpense> unallocatedExpenses() throws ServiceException {
		String query = UNALLOCATED_EXPENSE_BASE_QUERY + UNALLOCATED_EXPENSE_POST_QUERY;
        List<UnallocatedExpense> lineItems = jdbcTemplate.query(query, new UnallocatedExpenseRowMapper());
        return lineItems;
	}
    
    @Override
	public List<UnallocatedExpense> unallocatedExpensesForMonth(Date month) throws ServiceException {
		String query = UNALLOCATED_EXPENSE_BASE_QUERY + " where ua.month = ?" + UNALLOCATED_EXPENSE_POST_QUERY;
        List<UnallocatedExpense> lineItems = jdbcTemplate.query(query, new Object[] {month}, new UnallocatedExpenseRowMapper());
        return lineItems;
	}
    
    @Override
    public List<UnallocatedExpense> unallocatedExpensesForPeriod(Date startDate, Date endDate) throws ServiceException {
    	String query = UNALLOCATED_EXPENSE_BASE_QUERY + " where ua.month between ? and ?" + UNALLOCATED_EXPENSE_POST_QUERY;
        List<UnallocatedExpense> lineItems = jdbcTemplate.query(query, new Object[] {startDate, endDate}, new UnallocatedExpenseRowMapper());
        return lineItems;
    }
    
    @Override
    public List<UnallocatedExpense> unallocatedExpensesForCostAllocation(Long costAllocationId) throws ServiceException {
    	String query = UNALLOCATED_EXPENSE_BASE_QUERY + " where ua.cost_allocation_ref_id = ?" + UNALLOCATED_EXPENSE_POST_QUERY;
        List<UnallocatedExpense> lineItems = jdbcTemplate.query(query, new Object[] {costAllocationId}, new UnallocatedExpenseRowMapper());
        return lineItems;
    }
    
    private class UnallocatedExpenseRowMapper implements RowMapper {
    	public UnallocatedExpense mapRow(ResultSet rs, int i) throws SQLException {
    		UnallocatedExpense device = new UnallocatedExpense();
            device.setId(rs.getLong("id"));
            device.setAltId(rs.getString("alt_id"));
            device.setName(rs.getString("name"));
            device.setDescription(rs.getString("description"));
            device.setAmount(rs.getBigDecimal("amount"));
            device.setQuantity(rs.getInt("quantity"));
            device.setOspId(rs.getLong("osp_id"));
            device.setServiceName(rs.getString("service_name"));
            device.setVendor(rs.getString("vendor"));
            device.setPoNumber(rs.getString("po_number"));
            device.setCostAllocationId((rs.getLong("cost_allocation_ref_id") == 0) ? null : rs.getLong("cost_allocation_ref_id"));
            device.setMonth(rs.getDate("month"));
            return device;
        }
    }
}
