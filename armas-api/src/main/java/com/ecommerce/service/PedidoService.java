package com.ecommerce.service;

import com.ecommerce.dto.PedidoRequestDTO;
import com.ecommerce.dto.PedidoResponseDTO;
import com.ecommerce.exception.CadastroException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Cliente;
import com.ecommerce.model.ItemPedido;
import com.ecommerce.model.Pedido;
import com.ecommerce.model.Produto;
import com.ecommerce.model.StatusPedido;
import com.ecommerce.repository.PedidoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;

@ApplicationScoped
public class PedidoService {
    private final PedidoRepository pedidos;

    @Inject
    EntityManager entityManager;

    public PedidoService(PedidoRepository pedidos) {
        this.pedidos = pedidos;
    }

    @Transactional
    public List<PedidoResponseDTO> listar(Long clienteId) {
        return pedidos.listar(clienteId).stream().map(PedidoResponseDTO::from).toList();
    }

    @Transactional
    public PedidoResponseDTO buscar(Long id) {
        return PedidoResponseDTO.from(encontrar(id));
    }

    @Transactional
    public PedidoResponseDTO cadastrar(PedidoRequestDTO dados) {
        Cliente cliente = Cliente.findById(dados.clienteId());
        if (cliente == null) throw new ResourceNotFoundException("Cliente nao encontrado");
        Pedido pedido = new Pedido();
        pedido.cliente = cliente;
        pedido.criadoEm = Instant.now();
        var produtosUsados = new HashSet<Long>();
        for (var itemDados : dados.itens()) {
            if (!produtosUsados.add(itemDados.produtoId())) {
                throw new CadastroException("Inclua cada produto uma vez e ajuste sua quantidade.", 400);
            }
            Produto produto = Produto.findById(itemDados.produtoId());
            if (produto == null) throw new ResourceNotFoundException("Produto nao encontrado");
            ItemPedido item = new ItemPedido();
            item.pedido = pedido;
            item.produto = produto;
            item.nomeProduto = produto.nome;
            item.quantidade = itemDados.quantidade();
            item.precoUnitario = BigDecimal.valueOf(produto.preco).setScale(2, RoundingMode.HALF_UP);
            pedido.itens.add(item);
            pedido.total = pedido.total.add(item.precoUnitario.multiply(BigDecimal.valueOf(item.quantidade)));
        }
        pedidos.persist(pedido);
        pedidos.flush();
        return PedidoResponseDTO.from(pedido);
    }

    @Transactional
    public PedidoResponseDTO alterarStatus(Long id, StatusPedido novoStatus) {
        Pedido pedido = encontrar(id);
        if (novoStatus == StatusPedido.PENDENTE || pedido.status == StatusPedido.CANCELADO) {
            throw new CadastroException("Este pedido nao pode voltar ao estado pendente ou sair do cancelamento.", 409);
        }
        if (pedido.status == novoStatus) return PedidoResponseDTO.from(pedido);
        if (novoStatus == StatusPedido.CONFIRMADO) {
            for (ItemPedido item : pedido.itens) {
                Produto produto = entityManager.find(Produto.class, item.produto.id, LockModeType.PESSIMISTIC_WRITE);
                if (produto.estoque < item.quantidade) {
                    throw new CadastroException("Estoque insuficiente para " + item.nomeProduto + ".", 409);
                }
                produto.estoque -= item.quantidade;
            }
        } else if (pedido.status == StatusPedido.CONFIRMADO && novoStatus == StatusPedido.CANCELADO) {
            for (ItemPedido item : pedido.itens) {
                Produto produto = entityManager.find(Produto.class, item.produto.id, LockModeType.PESSIMISTIC_WRITE);
                produto.estoque += item.quantidade;
            }
        }
        pedido.status = novoStatus;
        pedidos.flush();
        return PedidoResponseDTO.from(pedido);
    }

    private Pedido encontrar(Long id) {
        return pedidos.findByIdOptional(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido nao encontrado"));
    }
}
