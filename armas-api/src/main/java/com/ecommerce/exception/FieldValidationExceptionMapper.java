package com.ecommerce.exception;

import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.Map;

@Provider
public class FieldValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {
    @Override
    public Response toResponse(ConstraintViolationException exception) {
        var errors = exception.getConstraintViolations().stream()
                .map(violation -> {
                    String field = "formulario";
                    for (var node : violation.getPropertyPath()) {
                        if (node.getName() != null) {
                            field = node.getName();
                        }
                    }
                    return Map.of("field", field, "message", violation.getMessage());
                })
                .toList();
        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of("errors", errors))
                .build();
    }
}
