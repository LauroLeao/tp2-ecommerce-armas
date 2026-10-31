package com.ecommerce.service.impl;

import com.ecommerce.dto.CategoriaRequestDTO;
import com.ecommerce.dto.CategoriaResponseDTO;
import com.ecommerce.model.Categoria;
import com.ecommerce.model.Produto;
import com.ecommerce.exception.CadastroException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CategoriaRepository;
import com.ecommerce.service.CategoriaService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
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
        verificarNome(nome, null);

        Categoria categoria = new Categoria();
        categoria.nome = nome;
        repository.persist(categoria);
        return CategoriaResponseDTO.from(categoria);
    }

    @Override
    @Transactional
    public CategoriaResponseDTO editar(Long id, CategoriaRequestDTO dados) {
        Categoria categoria = encontrar(id);
        String nome = dados.nome().trim();
        verificarNome(nome, id);
        categoria.nome = nome;
        repository.flush();
        return CategoriaResponseDTO.from(categoria);
    }

    @Override
    @Transactional
    public void excluir(Long id) {
        Categoria categoria = encontrar(id);
        if (Produto.count("categoria.id", id) > 0) {
            throw new CadastroException("A categoria possui produtos vinculados.", 409);
        }
        repository.delete(categoria);
    }

    private Categoria encontrar(Long id) {
        return repository.findByIdOptional(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria nao encontrada"));
    }

    private void verificarNome(String nome, Long idAtual) {
        long iguais = idAtual == null
                ? repository.count("lower(nome) = ?1", nome.toLowerCase(Locale.ROOT))
                : repository.count("lower(nome) = ?1 and id <> ?2", nome.toLowerCase(Locale.ROOT), idAtual);
        if (iguais > 0) throw new CadastroException("Essa categoria ja existe.", 409);
    }
}
