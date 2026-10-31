package com.ecommerce.service;

import com.ecommerce.dto.FornecedorRequestDTO;
import com.ecommerce.dto.FornecedorResponseDTO;
import com.ecommerce.exception.CadastroException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Fornecedor;
import com.ecommerce.repository.FornecedorRepository;
import com.ecommerce.model.Produto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Locale;

@ApplicationScoped
public class FornecedorService {
    private final FornecedorRepository fornecedores;
    private final MarcaService marcas;

    public FornecedorService(FornecedorRepository fornecedores, MarcaService marcas) {
        this.fornecedores = fornecedores;
        this.marcas = marcas;
    }

    public List<FornecedorResponseDTO> listar(Long marcaId) {
        List<Fornecedor> resultado = marcaId == null
                ? fornecedores.find("order by nome").list()
                : fornecedores.find("marca.id = ?1 order by nome", marcaId).list();
        return resultado.stream().map(FornecedorResponseDTO::from).toList();
    }

    public FornecedorResponseDTO buscar(Long id) {
        return FornecedorResponseDTO.from(encontrar(id));
    }

    @Transactional
    public FornecedorResponseDTO cadastrar(FornecedorRequestDTO dados) {
        Fornecedor fornecedor = new Fornecedor();
        preencher(fornecedor, dados);
        fornecedores.persist(fornecedor);
        fornecedores.flush();
        return FornecedorResponseDTO.from(fornecedor);
    }

    @Transactional
    public FornecedorResponseDTO editar(Long id, FornecedorRequestDTO dados) {
        Fornecedor fornecedor = encontrar(id);
        if (!fornecedor.marca.id.equals(dados.marcaId()) && Produto.count("fornecedor.id", id) > 0) {
            throw new CadastroException("Nao altere a marca de um fornecedor com produtos vinculados.", 409);
        }
        preencher(fornecedor, dados);
        fornecedores.flush();
        return FornecedorResponseDTO.from(fornecedor);
    }

    @Transactional
    public void excluir(Long id) {
        if (Produto.count("fornecedor.id", id) > 0) {
            throw new CadastroException("O fornecedor possui produtos vinculados.", 409);
        }
        fornecedores.delete(encontrar(id));
    }

    private Fornecedor encontrar(Long id) {
        return fornecedores.findByIdOptional(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fornecedor nao encontrado"));
    }

    private void preencher(Fornecedor fornecedor, FornecedorRequestDTO dados) {
        String email = dados.email().trim().toLowerCase(Locale.ROOT);
        long repetidos = fornecedor.id == null
                ? fornecedores.count("email", email)
                : fornecedores.count("email = ?1 and id <> ?2", email, fornecedor.id);
        if (repetidos > 0) {
            throw new CadastroException("Esse e-mail ja pertence a outro fornecedor.", 409);
        }
        fornecedor.nome = dados.nome().trim();
        fornecedor.email = email;
        fornecedor.telefone = dados.telefone().trim();
        fornecedor.marca = marcas.encontrar(dados.marcaId());
    }
}
