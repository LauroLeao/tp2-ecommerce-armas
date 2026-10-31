package com.ecommerce.exception;

public class ClienteFormException extends RuntimeException {
    private final String field;
    private final int status;

    public ClienteFormException(String field, String message, int status) {
        super(message);
        this.field = field;
        this.status = status;
    }

    public String field() {
        return field;
    }

    public int status() {
        return status;
    }
}
