package com.ecommerce.repository;

import com.ecommerce.model.Pedido;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class PedidoRepository implements PanacheRepository<Pedido> {
    public List<Pedido> listar(Long clienteId) {
        return clienteId == null
                ? find("order by id desc").list()
                : find("cliente.id = ?1 order by id desc", clienteId).list();
    }
}
