package com.logicalis.serviceinsight.web.controller;

import com.logicalis.serviceinsight.dao.Expense;
import com.logicalis.serviceinsight.dao.ExpenseCategory;
import com.logicalis.serviceinsight.dao.ExpenseType;
import com.logicalis.serviceinsight.service.ContractDaoService;
import com.logicalis.serviceinsight.service.CostDaoService;
import com.logicalis.serviceinsight.service.CostDaoServiceImpl.ModelExpenseType;
import com.logicalis.serviceinsight.service.RelatedDataException;
import com.logicalis.serviceinsight.service.ServiceException;
import com.logicalis.serviceinsight.service.ServiceUsageService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.convert.ConversionService;
import org.springframework.format.annotation.DateTimeFormat;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author poneil
 */
@Controller
@RequestMapping("/expenses")
public class ExpenseController extends BaseController {

	private static final String FILE_PATH = "/tmp/";
	
    @Autowired
    ServiceUsageService serviceUsageService;
    @Autowired
    ContractDaoService contractDaoService;
    @Autowired
    ConversionService conversionService;
    @Autowired
    CostDaoService costDaoService;

    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public Expense expense(@PathVariable("id") Long id) throws ServiceException {
        return contractDaoService.expense(id);
    }
    
    @ResponseBody
    @ResponseStatus(CREATED)
    @RequestMapping(method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse saveExpense(@RequestBody Expense expense) {
        try {
            Long id = contractDaoService.saveExpense(expense);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_new_expense", new Object[]{id}, LocaleContextHolder.getLocale()));
        } catch (ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_new_expense", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch (Exception e) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_new_expense", null, LocaleContextHolder.getLocale()));
        }
    }

    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(method = RequestMethod.PUT, consumes = APPLICATION_JSON_VALUE, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse updateExpense(@RequestBody Expense expense) {
        try {
            contractDaoService.updateExpense(expense);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_update_expense", new Object[]{expense.getId()}, LocaleContextHolder.getLocale()));
        } catch (ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_update_expense", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch (Exception e) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_update_expense", null, LocaleContextHolder.getLocale()));
        }
    }

    @ResponseBody
    @ResponseStatus(CREATED)
    @Secured({"ROLE_ADMIN"})
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse deleteExpense(@PathVariable("id") Long id) {
        try {
            contractDaoService.deleteExpense(id);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_delete_expense", new Object[]{id}, LocaleContextHolder.getLocale()));
        } catch (ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_delete_expense", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch (Exception e) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_delete_expense", null, LocaleContextHolder.getLocale()));
        }
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String showExpense(Model uiModel, @PathVariable("id") Long id) throws ServiceException {
        uiModel.addAttribute("expense", contractDaoService.expense(id));
        return "expenses/show";
    }

    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public List<Expense> expenses() {
        return contractDaoService.expenses();
    }

    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(params = "cid", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public List<Expense> findExpensesForCustomer(@RequestParam(value = "cid", required = true) Long customerId) {
        return contractDaoService.findExpensesByCustomer(customerId);
    }

    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(params = {"cid","sd","ed"}, method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public List<Expense> findExpensesForCustomerAndPeriod(@RequestParam(value = "cid", required = false) Long customerId,
            @RequestParam(value = "sd", required = true) @DateTimeFormat(pattern = "MMddyyyy") Date startDate,
            @RequestParam(value = "ed", required = true) @DateTimeFormat(pattern = "MMddyyyy") Date endDate) {
        return contractDaoService.findExpensesByCustomerIdAndPeriod(customerId, startDate, endDate);
    }

    @RequestMapping(method = RequestMethod.GET)
    public String expenses(Model uiModel) {
        uiModel.addAttribute("expenses", contractDaoService.expenses());
        return "expenses/index";
    }
    
    
    /*
     * EXPENSE CATEGORIES
     * 
     */
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/categories/{id}", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public ExpenseCategory expenseCategory(@PathVariable("id") Integer id) throws ServiceException {
        return contractDaoService.expenseCategory(id);
    }

    @ResponseBody
    @ResponseStatus(CREATED)
    @RequestMapping(value = "/categories", method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse saveCostCategory(@RequestBody ExpenseCategory expenseCategory) {
        try {
            Integer id = contractDaoService.saveExpenseCategory(expenseCategory);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_new_expensecategory", new Object[]{id}, LocaleContextHolder.getLocale()));
        } catch (ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_new_expensecategory", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch (Exception e) {
        	e.printStackTrace();
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_new_expensecategory", null, LocaleContextHolder.getLocale()));
        }
    }

    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/categories", method = RequestMethod.PUT, consumes = APPLICATION_JSON_VALUE, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse updateExpenseCategory(@RequestBody ExpenseCategory expenseCategory) {
        try {
            contractDaoService.updateExpenseCategory(expenseCategory);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_update_expensecategory", new Object[]{expenseCategory.getId()}, LocaleContextHolder.getLocale()));
        } catch (ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_update_expensecategory", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch (Exception e) {
        	e.printStackTrace();
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_update_expensecategory", null, LocaleContextHolder.getLocale()));
        }
    }

    @ResponseBody
    @ResponseStatus(CREATED)
    @Secured({"ROLE_ADMIN"})
    @RequestMapping(value = "/categories/{id}", method = RequestMethod.DELETE, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse deleteExpenseCategory(@PathVariable("id") Integer id) {
        try {
            contractDaoService.deleteExpenseCategory(id);
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_delete_expensecategory", new Object[]{id}, LocaleContextHolder.getLocale()));
        } catch (ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_delete_expensecategory", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch (RelatedDataException ude) {
            return new APIResponse(APIResponse.Status.RELATED_DATA_FOUND, ude.getMessage(), ude.getRelatedData(), ude.getRelatedDataType());
        } catch (Exception e) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_error_general_delete_expensecategory", null, LocaleContextHolder.getLocale()));
        }
    }
    
    @RequestMapping(value = "/categories/{id}", method = RequestMethod.GET)
    public String showExpenseCategory(Model uiModel, @PathVariable("id") Integer id) throws ServiceException {
    	//TODO: Move this to expense controller?
        uiModel.addAttribute("expenseCategory", contractDaoService.expenseCategory(id));
        return "costCategories/show";
    }

    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/categories", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public List<ExpenseCategory> expenseCategories() {
    	List<ExpenseCategory> categories = contractDaoService.expenseCategories();
    	Collections.sort(categories, ExpenseCategory.ExpenseCategoryNameComparator);
        return categories;
    }
    
    
    
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/import", method = RequestMethod.POST)
    public String importExpenses(@RequestParam("templatefile") MultipartFile file, @RequestParam("importtype") ModelExpenseType expenseType, @RequestParam(value = "uploadexpensedate", required = false) @DateTimeFormat(pattern = "MM/yyyy") Date expenseDate, Model uiModel) {
    	StringBuffer response = new StringBuffer();
        String status = "error";
        String message = "";
    	if (!file.isEmpty()) {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmm").format(new DateTime().toDate());
            try {
                InputStream is = file.getInputStream();
                File uploaded = new File(FILE_PATH + "ImportCost_"+timestamp+"_"+file.getOriginalFilename());
                FileOutputStream fos = new FileOutputStream(uploaded);
                while (is.available() > 0) {
                    fos.write(is.read());
                }
                is.close();
                fos.close();
                log.debug("uploaded: " + uploaded.getAbsolutePath());
                
                if(expenseDate != null) {
                	DateTime expenseMonth = new DateTime(expenseDate);
                	expenseMonth.dayOfMonth().withMinimumValue();
                	expenseDate = expenseMonth.toDate();
                }
                
                costDaoService.importExpenses(uploaded, expenseType, expenseDate);
                
                status = "success";
                message = messageSource.getMessage("ui_ok_file_upload", null, LocaleContextHolder.getLocale());
            } catch (ServiceException ex) {
                log.debug("Showing exception from Expense Import", ex);
                message = ex.getMessage();
            } catch (Exception ex) {
                log.debug("Showing exception from Expense Import", ex);
                message = ex.getMessage();
            }
        } else {
            message = messageSource.getMessage("import_error_empty_file", null, LocaleContextHolder.getLocale());
        }
    	//we have to do a custom response here, since we are posting back to an iframe and parse from there
    	response.append("<span id=\"iframe-status\">").append(status).append("</span>").append("<span id=\"iframe-message\">").append(message).append("</span>");
    	return response.toString();
    }
  
}
