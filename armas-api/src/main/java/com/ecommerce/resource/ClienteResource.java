package com.ecommerce.resource;

import com.ecommerce.dto.ClienteRequestDTO;
import com.ecommerce.dto.ClienteResponseDTO;
import com.ecommerce.service.ClienteService;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/clientes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ClienteResource {

    private final ClienteService service;

    public ClienteResource(ClienteService service) {
        this.service = service;
    }

    @GET
    public List<ClienteResponseDTO> listar() {
        return service.listar();
    }

    @GET
    @Path("/{id}")
    public ClienteResponseDTO buscarPorId(@PathParam("id") Long id) {
        return service.buscarPorId(id);
    }

    @POST
    public Response cadastrar(@Valid ClienteRequestDTO cliente) {
        return Response.status(Response.Status.CREATED)
                .entity(service.cadastrar(cliente))
                .build();
    }

    @PUT
    @Path("/{id}")
    public ClienteResponseDTO editar(@PathParam("id") Long id, @Valid ClienteRequestDTO cliente) {
        return service.editar(id, cliente);
    }

    @DELETE
    @Path("/{id}")
    public Response excluir(@PathParam("id") Long id) {
        service.excluir(id);
        return Response.noContent().build();
    }
}
