package com.ecommerce.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class CadastroExceptionMapper implements ExceptionMapper<CadastroException> {
    @Override
    public Response toResponse(CadastroException erro) {
        return Response.status(erro.status())
                .entity(new Mensagem(erro.getMessage()))
                .build();
    }

    private record Mensagem(String mensagem) {
    }
}
