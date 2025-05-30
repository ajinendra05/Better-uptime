package com.devproject.dpinUptime.controller;


import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.RequestDispatcher;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        // Get error status and message
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        
        // Default values
        int statusCode = 500;
        String errorMessage = "Something went wrong";
        
        if (status != null) {
            statusCode = Integer.parseInt(status.toString());
        }
        
        if (message != null) {
            errorMessage = message.toString();
        }
        
        // Custom messages for common errors
        if (statusCode == HttpStatus.NOT_FOUND.value()) {
            errorMessage = "The page you're looking for doesn't exist";
        } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
            errorMessage = "You don't have permission to access this page";
        } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
            errorMessage = "Our server encountered an unexpected problem";
        }
        
        // Add attributes to model
        model.addAttribute("statusCode", statusCode);
        model.addAttribute("errorMessage", errorMessage);
        
        return "error";
    }
    
    public String getErrorPath() {
        return "/error";
    }
}
