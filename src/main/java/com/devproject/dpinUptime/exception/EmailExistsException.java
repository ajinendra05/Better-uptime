package com.devproject.dpinUptime.exception;


// Custom Exception
public class EmailExistsException extends Exception {
    public EmailExistsException(String message) {
        super(message);
    }
}