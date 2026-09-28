package com.acme.salary.exception;

public class DomainException extends RuntimeException {
    public DomainException(String message) {
        super(message);
    }
}
