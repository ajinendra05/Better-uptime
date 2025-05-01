package com.devproject.dpinUptime.exception;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailExistsException.class)
    public String handleEmailExistsException(EmailExistsException ex, Model model) {
        model.addAttribute("error", ex.getMessage());
        return "register";
    }
}

