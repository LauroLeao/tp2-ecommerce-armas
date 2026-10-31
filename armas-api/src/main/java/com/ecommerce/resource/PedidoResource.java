package com.ecommerce.resource;

import com.ecommerce.dto.PedidoRequestDTO;
import com.ecommerce.dto.PedidoResponseDTO;
import com.ecommerce.dto.PedidoStatusRequestDTO;
import com.ecommerce.service.PedidoService;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import jakarta.annotation.security.RolesAllowed;

@Path("/pedidos")
@RolesAllowed("ADMIN")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PedidoResource {
    private final PedidoService service;

    public PedidoResource(PedidoService service) {
        this.service = service;
    }

    @GET
    public List<PedidoResponseDTO> listar(@QueryParam("clienteId") Long clienteId) {
        return service.listar(clienteId);
    }

    @GET
    @Path("/{id}")
    public PedidoResponseDTO buscar(@PathParam("id") Long id) {
        return service.buscar(id);
    }

    @POST
    public Response cadastrar(@Valid PedidoRequestDTO dados) {
        return Response.status(Response.Status.CREATED).entity(service.cadastrar(dados)).build();
    }

    @PUT
    @Path("/{id}/status")
    public PedidoResponseDTO alterarStatus(@PathParam("id") Long id, @Valid PedidoStatusRequestDTO dados) {
        return service.alterarStatus(id, dados.status());
    }
}
