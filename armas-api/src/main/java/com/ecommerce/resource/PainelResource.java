package com.ecommerce.resource;

import com.ecommerce.dto.PedidoResponseDTO;
import com.ecommerce.model.Cliente;
import com.ecommerce.model.Pedido;
import com.ecommerce.model.Produto;
import com.ecommerce.model.StatusPedido;
import com.ecommerce.model.Usuario;
import com.ecommerce.service.PedidoService;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("/painel")
@Produces(MediaType.APPLICATION_JSON)
public class PainelResource {
    private final SecurityIdentity identidade;
    private final PedidoService pedidos;

    public PainelResource(SecurityIdentity identidade, PedidoService pedidos) {
        this.identidade = identidade;
        this.pedidos = pedidos;
    }

    public record ResumoAdmin(long produtos, long semEstoque, long clientes, long pedidosPendentes,
                              long pedidosConfirmados) {}
    public record ResumoCliente(long pedidos, long pendentes, List<PedidoResponseDTO> recentes) {}

    @GET
    @Path("/admin")
    @RolesAllowed("ADMIN")
    public ResumoAdmin admin() {
        return new ResumoAdmin(Produto.count(), Produto.count("estoque <= 0"), Cliente.count(),
                Pedido.count("status", StatusPedido.PENDENTE),
                Pedido.count("status", StatusPedido.CONFIRMADO));
    }

    @GET
    @Path("/cliente")
    @RolesAllowed("CLIENTE")
    public ResumoCliente cliente() {
        Usuario usuario = Usuario.find("email", identidade.getPrincipal().getName()).firstResult();
        if (usuario == null || usuario.cliente == null) throw new jakarta.ws.rs.ForbiddenException();
        List<PedidoResponseDTO> todos = pedidos.listar(usuario.cliente.id);
        long pendentes = todos.stream().filter(p -> p.status() == StatusPedido.PENDENTE).count();
        return new ResumoCliente(todos.size(), pendentes, todos.stream().limit(5).toList());
    }
}
