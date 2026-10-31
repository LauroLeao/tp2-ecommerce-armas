package com.ecommerce.resource;

import com.ecommerce.dto.MarcaRequestDTO;
import com.ecommerce.dto.MarcaResponseDTO;
import com.ecommerce.service.MarcaService;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import jakarta.annotation.security.RolesAllowed;

@Path("/marcas")
@RolesAllowed("ADMIN")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MarcaResource {
    private final MarcaService service;

    public MarcaResource(MarcaService service) {
        this.service = service;
    }

    @GET
    public List<MarcaResponseDTO> listar() {
        return service.listar();
    }

    @GET
    @Path("/{id}")
    public MarcaResponseDTO buscar(@PathParam("id") Long id) {
        return service.buscar(id);
    }

    @POST
    public Response cadastrar(@Valid MarcaRequestDTO dados) {
        return Response.status(Response.Status.CREATED).entity(service.cadastrar(dados)).build();
    }

    @PUT
    @Path("/{id}")
    public MarcaResponseDTO editar(@PathParam("id") Long id, @Valid MarcaRequestDTO dados) {
        return service.editar(id, dados);
    }

    @DELETE
    @Path("/{id}")
    public Response excluir(@PathParam("id") Long id) {
        service.excluir(id);
        return Response.noContent().build();
    }
}
