package com.ecommerce.resource;

import com.ecommerce.dto.CategoriaRequestDTO;
import com.ecommerce.dto.CategoriaResponseDTO;
import com.ecommerce.service.CategoriaService;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/categorias")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CategoriaResource {

    private final CategoriaService service;

    public CategoriaResource(CategoriaService service) {
        this.service = service;
    }

    @GET
    public List<CategoriaResponseDTO> listar() {
        return service.listar();
    }

    @POST
    public Response cadastrar(@Valid CategoriaRequestDTO dados) {
        return Response.status(Response.Status.CREATED)
                .entity(service.cadastrar(dados))
                .build();
    }
}
