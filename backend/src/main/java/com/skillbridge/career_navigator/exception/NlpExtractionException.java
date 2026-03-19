package com.skillbridge.career_navigator.exception;

public class NlpExtractionException extends RuntimeException {
    
    public NlpExtractionException(String message) {
        super(message);
    }
    
    public NlpExtractionException(String message, Throwable cause) {
        super(message, cause);
    }
}
