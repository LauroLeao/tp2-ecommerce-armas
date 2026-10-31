package com.ecommerce.resource;

import com.ecommerce.dto.FornecedorRequestDTO;
import com.ecommerce.dto.FornecedorResponseDTO;
import com.ecommerce.service.FornecedorService;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import jakarta.annotation.security.RolesAllowed;

@Path("/fornecedores")
@RolesAllowed("ADMIN")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class FornecedorResource {
    private final FornecedorService service;

    public FornecedorResource(FornecedorService service) {
        this.service = service;
    }

    @GET
    public List<FornecedorResponseDTO> listar(@QueryParam("marcaId") Long marcaId) {
        return service.listar(marcaId);
    }

    @GET
    @Path("/{id}")
    public FornecedorResponseDTO buscar(@PathParam("id") Long id) {
        return service.buscar(id);
    }

    @POST
    public Response cadastrar(@Valid FornecedorRequestDTO dados) {
        return Response.status(Response.Status.CREATED).entity(service.cadastrar(dados)).build();
    }

    @PUT
    @Path("/{id}")
    public FornecedorResponseDTO editar(@PathParam("id") Long id, @Valid FornecedorRequestDTO dados) {
        return service.editar(id, dados);
    }

    @DELETE
    @Path("/{id}")
    public Response excluir(@PathParam("id") Long id) {
        service.excluir(id);
        return Response.noContent().build();
    }
}
