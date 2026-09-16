package com.deepblue.deepblue.exception;

public class BusinessRuleException
        extends RuntimeException {

    public BusinessRuleException(
            String message) {

        super(message);
    }
}
