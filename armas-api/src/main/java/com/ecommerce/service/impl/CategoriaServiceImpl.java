package com.ecommerce.service.impl;

import com.ecommerce.dto.CategoriaRequestDTO;
import com.ecommerce.dto.CategoriaResponseDTO;
import com.ecommerce.model.Categoria;
import com.ecommerce.repository.CategoriaRepository;
import com.ecommerce.service.CategoriaService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import java.util.List;
import java.util.Locale;

@ApplicationScoped
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository repository;

    public CategoriaServiceImpl(CategoriaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<CategoriaResponseDTO> listar() {
        return repository.find("order by nome").list().stream()
                .map(CategoriaResponseDTO::from)
                .toList();
    }

    @Override
    @Transactional
    public CategoriaResponseDTO cadastrar(CategoriaRequestDTO dados) {
        String nome = dados.nome().trim();
        if (repository.count("lower(nome) = ?1", nome.toLowerCase(Locale.ROOT)) > 0) {
            throw new WebApplicationException("Categoria ja cadastrada", 409);
        }

        Categoria categoria = new Categoria();
        categoria.nome = nome;
        repository.persist(categoria);
        return CategoriaResponseDTO.from(categoria);
    }
}
