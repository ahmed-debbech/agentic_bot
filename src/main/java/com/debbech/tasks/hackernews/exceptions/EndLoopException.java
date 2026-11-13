package com.debbech.tasks.hackernews.exceptions;

public class EndLoopException extends RuntimeException {
    public EndLoopException(String message) {
        super(message);
    }
}
