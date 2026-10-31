package com.ecommerce.service.impl;

import com.ecommerce.dto.ProdutoRequestDTO;
import com.ecommerce.dto.ProdutoResponseDTO;
import com.ecommerce.dto.ProdutoImagemDTO;
import com.ecommerce.dto.PageResponse;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Categoria;
import com.ecommerce.model.Produto;
import com.ecommerce.model.ProdutoImagem;
import com.ecommerce.model.Modelo;
import com.ecommerce.model.Fornecedor;
import com.ecommerce.model.ItemPedido;
import com.ecommerce.exception.CadastroException;
import com.ecommerce.repository.CategoriaRepository;
import com.ecommerce.repository.ProdutoRepository;
import com.ecommerce.repository.ModeloRepository;
import com.ecommerce.repository.FornecedorRepository;
import com.ecommerce.service.ProdutoService;
import com.ecommerce.service.ProdutoImagemValidator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ProdutoServiceImpl implements ProdutoService {

    private final ProdutoRepository repository;
    private final CategoriaRepository categoriaRepository;
    private final ModeloRepository modeloRepository;
    private final FornecedorRepository fornecedorRepository;
    private final ProdutoImagemValidator imagemValidator;

    public ProdutoServiceImpl(ProdutoRepository repository, CategoriaRepository categoriaRepository,
            ModeloRepository modeloRepository, FornecedorRepository fornecedorRepository,
            ProdutoImagemValidator imagemValidator) {
        this.repository = repository;
        this.categoriaRepository = categoriaRepository;
        this.modeloRepository = modeloRepository;
        this.fornecedorRepository = fornecedorRepository;
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
        if (ItemPedido.count("produto.id", id) > 0) {
            throw new CadastroException("O produto aparece em pedidos. Preserve o historico de compras.", 409);
        }
        repository.delete(produto);
    }

    private Produto encontrarProduto(Long id) {
        return repository.findByIdOptional(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto nao encontrado"));
    }

    private void preencher(Produto produto, ProdutoRequestDTO dados) {
        Categoria categoria = categoriaRepository.findByIdOptional(dados.categoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria nao encontrada"));
        Modelo modelo = modeloRepository.findByIdOptional(dados.modeloId())
                .orElseThrow(() -> new ResourceNotFoundException("Modelo nao encontrado"));
        Fornecedor fornecedor = dados.fornecedorId() == null ? null
                : fornecedorRepository.findByIdOptional(dados.fornecedorId())
                        .orElseThrow(() -> new ResourceNotFoundException("Fornecedor nao encontrado"));
        if (fornecedor != null && !modelo.marca.id.equals(fornecedor.marca.id)) {
            throw new CadastroException("Modelo e fornecedor devem pertencer a mesma marca.", 400);
        }
        ProdutoImagem novaImagem = imagemValidator.validar(dados.imagemBase64(), dados.removerImagem());
        produto.nome = dados.nome();
        produto.descricao = dados.descricao();
        produto.preco = dados.preco();
        produto.estoque = dados.estoque();
        produto.categoria = categoria;
        produto.modelo = modelo;
        produto.fornecedor = fornecedor;
        if (dados.removerImagem()) {
            produto.imagem = null;
        } else if (novaImagem != null) {
            produto.imagem = novaImagem;
        }
    }
}
