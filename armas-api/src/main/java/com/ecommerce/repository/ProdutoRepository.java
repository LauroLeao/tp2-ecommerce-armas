package com.ecommerce.repository;

import com.ecommerce.model.Produto;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Locale;

@ApplicationScoped
public class ProdutoRepository implements PanacheRepository<Produto> {

    public List<Produto> buscarPorNome(String nome, int page, int pageSize) {
        return consultaPorNome(nome).page(Page.of(page, pageSize)).list();
    }

    public long contarPorNome(String nome) {
        return consultaPorNome(nome).count();
    }

    private PanacheQuery<Produto> consultaPorNome(String nome) {
        if (nome == null || nome.isBlank()) {
            return find("order by id");
        }
        return find("lower(nome) like ?1 order by id", "%" + nome.trim().toLowerCase(Locale.ROOT) + "%");
    }
}
