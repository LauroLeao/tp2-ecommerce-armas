package com.ecommerce.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.Map;

@Provider
public class ClienteFormExceptionMapper implements ExceptionMapper<ClienteFormException> {
    @Override
    public Response toResponse(ClienteFormException exception) {
        return Response.status(exception.status())
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of("field", exception.field(), "message", exception.getMessage()))
                .build();
    }
}
