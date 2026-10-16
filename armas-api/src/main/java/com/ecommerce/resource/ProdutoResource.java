package com.ecommerce.resource;

import com.ecommerce.dto.ProdutoRequestDTO;
import com.ecommerce.dto.ProdutoResponseDTO;
import com.ecommerce.dto.ProdutoImagemDTO;
import com.ecommerce.dto.PageResponse;
import com.ecommerce.service.ProdutoService;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/produtos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProdutoResource {

    private final ProdutoService service;

    public ProdutoResource(ProdutoService service) {
        this.service = service;
    }

    @GET
    public PageResponse<ProdutoResponseDTO> listar(
            @QueryParam("nome") String nome,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        if (page < 0 || pageSize < 1 || pageSize > 100) {
            throw new BadRequestException("Page deve ser >= 0 e pageSize deve estar entre 1 e 100");
        }
        return service.listar(nome, page, pageSize);
    }

    @GET
    @Path("/{id}")
    public ProdutoResponseDTO buscarPorId(@PathParam("id") Long id) {
        return service.buscarPorId(id);
    }

    @GET
    @Path("/{id}/imagem")
    @Produces({"image/png", "image/jpeg"})
    public Response buscarImagem(@PathParam("id") Long id) {
        ProdutoImagemDTO imagem = service.buscarImagem(id);
        return Response.ok(imagem.conteudo(), imagem.tipo())
                .header("X-Content-Type-Options", "nosniff")
                .header("Cache-Control", "no-cache")
                .build();
    }

    @POST
    public Response cadastrar(@Valid ProdutoRequestDTO produto) {
        return Response.status(Response.Status.CREATED)
                .entity(service.cadastrar(produto))
                .build();
    }

    @PUT
    @Path("/{id}")
    public ProdutoResponseDTO editar(@PathParam("id") Long id, @Valid ProdutoRequestDTO produto) {
        return service.editar(id, produto);
    }

    @DELETE
    @Path("/{id}")
    public Response excluir(@PathParam("id") Long id) {
        service.excluir(id);
        return Response.noContent().build();
    }
}
