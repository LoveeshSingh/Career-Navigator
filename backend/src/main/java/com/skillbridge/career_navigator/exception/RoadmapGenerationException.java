package com.skillbridge.career_navigator.exception;

public class RoadmapGenerationException extends RuntimeException {
    public RoadmapGenerationException(String message) {
        super(message);
    }

    public RoadmapGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
