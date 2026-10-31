package com.ecommerce.resource;

import com.ecommerce.dto.CadastroContaDTO;
import com.ecommerce.dto.ClienteRequestDTO;
import com.ecommerce.dto.ClienteResponseDTO;
import com.ecommerce.dto.ProdutoImagemDTO;
import com.ecommerce.dto.SessaoDTO;
import com.ecommerce.exception.CadastroException;
import com.ecommerce.model.Cliente;
import com.ecommerce.model.Usuario;
import com.ecommerce.service.ClienteService;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.vertx.http.runtime.security.FormAuthenticationMechanism;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Locale;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {
    private final SecurityIdentity identidade;
    private final ClienteService clientes;

    public AuthResource(SecurityIdentity identidade, ClienteService clientes) {
        this.identidade = identidade;
        this.clientes = clientes;
    }

    @POST
    @Path("/cadastro")
    @PermitAll
    @Transactional
    public Response cadastrar(@Valid CadastroContaDTO dados) {
        if (dados.cliente() == null) throw new BadRequestException("Informe os dados do cliente.");
        String email = dados.cliente().email().trim().toLowerCase(Locale.ROOT);
        if (Usuario.count("email", email) > 0) throw new CadastroException("Este e-mail ja possui uma conta.", 409);
        ClienteResponseDTO criado = clientes.cadastrar(dados.cliente());
        Usuario usuario = new Usuario();
        usuario.email = email;
        usuario.senhaHash = BcryptUtil.bcryptHash(dados.senha());
        usuario.perfil = "CLIENTE";
        usuario.cliente = Cliente.findById(criado.id());
        usuario.persist();
        return Response.status(Response.Status.CREATED).entity(criado).build();
    }

    @GET
    @Path("/me")
    @RolesAllowed({"ADMIN", "CLIENTE"})
    public SessaoDTO me() {
        Usuario usuario = usuarioAtual();
        Cliente cliente = usuario.cliente;
        return new SessaoDTO(usuario.email, usuario.perfil, cliente == null ? null : cliente.id,
                cliente == null ? "Administrador" : cliente.nome);
    }

    @GET
    @Path("/me/cliente")
    @RolesAllowed("CLIENTE")
    public ClienteResponseDTO meuCadastro() {
        return clientes.buscarPorId(clienteAtual().id);
    }

    @PUT
    @Path("/me/cliente")
    @RolesAllowed("CLIENTE")
    @Transactional
    public ClienteResponseDTO atualizarMeuCadastro(@Valid ClienteRequestDTO dados) {
        Usuario usuario = usuarioAtual();
        if (!usuario.email.equalsIgnoreCase(dados.email().trim())) {
            throw new BadRequestException("O e-mail da conta deve ser alterado pelo administrador.");
        }
        ClienteResponseDTO atualizado = clientes.editar(clienteAtual().id, dados);
        return atualizado;
    }

    @GET
    @Path("/me/foto")
    @RolesAllowed("CLIENTE")
    @Produces({"image/png", "image/jpeg"})
    public Response minhaFoto() {
        ProdutoImagemDTO foto = clientes.buscarFoto(clienteAtual().id);
        return Response.ok(foto.conteudo(), foto.tipo()).header("Cache-Control", "no-cache")
                .header("X-Content-Type-Options", "nosniff").build();
    }

    @POST
    @Path("/logout")
    @RolesAllowed({"ADMIN", "CLIENTE"})
    public Response sair() {
        FormAuthenticationMechanism.logout(identidade);
        return Response.noContent().build();
    }

    private Usuario usuarioAtual() {
        Usuario usuario = Usuario.find("email", identidade.getPrincipal().getName()).firstResult();
        if (usuario == null) throw new NotAuthorizedException("Conta nao encontrada.");
        return usuario;
    }

    private Cliente clienteAtual() {
        Cliente cliente = usuarioAtual().cliente;
        if (cliente == null) throw new ForbiddenException("Conta sem cadastro de cliente.");
        return cliente;
    }
}
