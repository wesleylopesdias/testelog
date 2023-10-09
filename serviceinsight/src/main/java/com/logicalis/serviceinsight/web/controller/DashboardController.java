package com.logicalis.serviceinsight.web.controller;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.Date;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.logicalis.serviceinsight.data.Dashboard;
import com.logicalis.serviceinsight.service.DashboardService;

@Controller
@RequestMapping("/dashboard")
public class DashboardController extends BaseController {
    
    @Autowired
    DashboardService dashboardService;

	@RequestMapping(method = RequestMethod.GET)
    public String dashboard(Model uiModel) {
        return "dashboard/index";
    }

    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/data", method = RequestMethod.GET, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public Dashboard queryForData(@RequestParam(value = "ed", required = false) @DateTimeFormat(pattern = "MM/yyyy") Date reqEndDate) {
        // determine start & end date range for last 6 months
        DateTime endDate = DateTime.now();
        if (reqEndDate!=null) {
            endDate = new DateTime(reqEndDate);
        }
        DateTime startDate = endDate.minusMonths(5).withDayOfMonth(1).withTime(0, 0, 0, 0);
        Dashboard dashboard = dashboardService.generateRevenueDashboard(startDate, endDate);
        log.debug(dashboard.toString());
        return (dashboard);
    }

}
