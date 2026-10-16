package com.ecommerce.service.impl;

import com.ecommerce.dto.ProdutoRequestDTO;
import com.ecommerce.dto.ProdutoResponseDTO;
import com.ecommerce.dto.ProdutoImagemDTO;
import com.ecommerce.dto.PageResponse;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Categoria;
import com.ecommerce.model.Produto;
import com.ecommerce.model.ProdutoImagem;
import com.ecommerce.repository.CategoriaRepository;
import com.ecommerce.repository.ProdutoRepository;
import com.ecommerce.service.ProdutoService;
import com.ecommerce.service.ProdutoImagemValidator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ProdutoServiceImpl implements ProdutoService {

    private final ProdutoRepository repository;
    private final CategoriaRepository categoriaRepository;
    private final ProdutoImagemValidator imagemValidator;

    public ProdutoServiceImpl(ProdutoRepository repository, CategoriaRepository categoriaRepository,
            ProdutoImagemValidator imagemValidator) {
        this.repository = repository;
        this.categoriaRepository = categoriaRepository;
        this.imagemValidator = imagemValidator;
    }

    @Override
    public PageResponse<ProdutoResponseDTO> listar(String nome, int page, int pageSize) {
        return PageResponse.of(
                repository.buscarPorNome(nome, page, pageSize),
                page,
                pageSize,
                repository.contarPorNome(nome),
                ProdutoResponseDTO::from);
    }

    @Override
    public ProdutoResponseDTO buscarPorId(Long id) {
        return ProdutoResponseDTO.from(encontrarProduto(id));
    }

    @Override
    @Transactional
    public ProdutoImagemDTO buscarImagem(Long id) {
        ProdutoImagem imagem = encontrarProduto(id).imagem;
        if (imagem == null) {
            throw new ResourceNotFoundException("Imagem do produto nao encontrada");
        }
        return new ProdutoImagemDTO(imagem.conteudo, imagem.tipo);
    }

    @Override
    @Transactional
    public ProdutoResponseDTO cadastrar(ProdutoRequestDTO dados) {
        Produto produto = new Produto();
        preencher(produto, dados);
        repository.persist(produto);
        repository.flush();
        return ProdutoResponseDTO.from(produto);
    }

    @Override
    @Transactional
    public ProdutoResponseDTO editar(Long id, ProdutoRequestDTO dados) {
        Produto produto = encontrarProduto(id);
        preencher(produto, dados);
        repository.flush();
        return ProdutoResponseDTO.from(produto);
    }

    @Override
    @Transactional
    public void excluir(Long id) {
        Produto produto = encontrarProduto(id);
        repository.delete(produto);
    }

    private Produto encontrarProduto(Long id) {
        return repository.findByIdOptional(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto nao encontrado"));
    }

    private void preencher(Produto produto, ProdutoRequestDTO dados) {
        Categoria categoria = categoriaRepository.findByIdOptional(dados.categoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria nao encontrada"));
        ProdutoImagem novaImagem = imagemValidator.validar(dados.imagemBase64(), dados.removerImagem());
        produto.nome = dados.nome();
        produto.descricao = dados.descricao();
        produto.preco = dados.preco();
        produto.estoque = dados.estoque();
        produto.categoria = categoria;
        if (dados.removerImagem()) {
            produto.imagem = null;
        } else if (novaImagem != null) {
            produto.imagem = novaImagem;
        }
    }
}
