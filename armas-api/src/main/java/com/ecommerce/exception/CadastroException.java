package com.ecommerce.exception;

public class CadastroException extends RuntimeException {
    private final int status;

    public CadastroException(String mensagem, int status) {
        super(mensagem);
        this.status = status;
    }

    public int status() {
        return status;
    }
}
