package com.logicalis.serviceinsight.web.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.logicalis.serviceinsight.data.ContractInvoice;
import com.logicalis.serviceinsight.data.User;
import com.logicalis.serviceinsight.service.UserDaoService;
import com.logicalis.serviceinsight.web.config.CloudBillingSecurityConfig.Role;

@Controller
@RequestMapping("/billing")
public class BillingController extends BaseController {

	@Autowired
	UserDaoService userDaoService;
	
	@RequestMapping(method = RequestMethod.GET)
    public String billingIndex(Model uiModel) {
		uiModel.addAttribute("invoiceStatuses", Arrays.asList(ContractInvoice.Status.values()));
		List<User> users = userDaoService.users(Boolean.TRUE);
		List<User> sdms = new ArrayList<User>();
		for(User user : users) {
			List<Role> profiles = user.getProfile();
			for(Role role : profiles) {
				if(Role.ROLE_SDM.equals(role)) {
					sdms.add(user);
					break;
				}
			}
		}
		
		uiModel.addAttribute("sdms", sdms);
        return "billing/index";
    }
	
}
