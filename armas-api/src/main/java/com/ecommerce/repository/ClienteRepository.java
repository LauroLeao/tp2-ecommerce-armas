package com.ecommerce.repository;

import com.ecommerce.model.Cliente;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Locale;

@ApplicationScoped
public class ClienteRepository implements PanacheRepository<Cliente> {
    public List<Cliente> sugerirPorNome(String nome) {
        String termo = nome.trim().toLowerCase(Locale.ROOT);
        return find("lower(nome) like ?1 order by nome", "%" + termo + "%")
                .page(Page.ofSize(10))
                .list();
    }

    public boolean existeOutroCom(String campo, String valor, Long idAtual) {
        return count(campo + " = ?1 and id <> ?2", valor, idAtual == null ? -1L : idAtual) > 0;
    }
}
