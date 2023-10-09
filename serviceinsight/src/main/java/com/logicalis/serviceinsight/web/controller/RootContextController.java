package com.logicalis.serviceinsight.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 *
 * @author poneil
 */
@Controller
@RequestMapping("/")
public class RootContextController {

    private final Logger log = LoggerFactory.getLogger(RootContextController.class);

    @RequestMapping(method = RequestMethod.GET)
    public String root() {
        return "redirect:/dashboard";
    }
}
