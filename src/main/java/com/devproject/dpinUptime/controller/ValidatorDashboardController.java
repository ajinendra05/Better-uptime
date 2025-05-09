package com.devproject.dpinUptime.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ValidatorDashboardController {
    @GetMapping("/validator/dashboard")
    public String validatorLogin() {
        return "validator-dashboard";
    }
}
