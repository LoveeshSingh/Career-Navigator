package com.skillbridge.careernavigator.exception;

public class NlpExtractionException extends RuntimeException {
    
    public NlpExtractionException(String message) {
        super(message);
    }
    
    public NlpExtractionException(String message, Throwable cause) {
        super(message, cause);
    }
}
