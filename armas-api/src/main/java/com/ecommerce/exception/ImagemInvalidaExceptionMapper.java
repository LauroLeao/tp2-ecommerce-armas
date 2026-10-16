package com.ecommerce.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.Map;

@Provider
public class ImagemInvalidaExceptionMapper implements ExceptionMapper<ImagemInvalidaException> {

    @Override
    public Response toResponse(ImagemInvalidaException exception) {
        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of("mensagem", exception.getMessage()))
                .build();
    }
}
