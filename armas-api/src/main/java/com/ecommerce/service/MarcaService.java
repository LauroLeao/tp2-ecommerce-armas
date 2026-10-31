package com.ecommerce.service;

import com.ecommerce.dto.MarcaRequestDTO;
import com.ecommerce.dto.MarcaResponseDTO;
import com.ecommerce.exception.CadastroException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Marca;
import com.ecommerce.repository.FornecedorRepository;
import com.ecommerce.repository.MarcaRepository;
import com.ecommerce.repository.ModeloRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Locale;

@ApplicationScoped
public class MarcaService {
    private final MarcaRepository marcas;
    private final ModeloRepository modelos;
    private final FornecedorRepository fornecedores;

    public MarcaService(MarcaRepository marcas, ModeloRepository modelos, FornecedorRepository fornecedores) {
        this.marcas = marcas;
        this.modelos = modelos;
        this.fornecedores = fornecedores;
    }

    public List<MarcaResponseDTO> listar() {
        return marcas.find("order by nome").list().stream().map(MarcaResponseDTO::from).toList();
    }

    public MarcaResponseDTO buscar(Long id) {
        return MarcaResponseDTO.from(encontrar(id));
    }

    @Transactional
    public MarcaResponseDTO cadastrar(MarcaRequestDTO dados) {
        String nome = dados.nome().trim();
        verificarNome(nome, null);
        Marca marca = new Marca();
        marca.nome = nome;
        marcas.persist(marca);
        marcas.flush();
        return MarcaResponseDTO.from(marca);
    }

    @Transactional
    public MarcaResponseDTO editar(Long id, MarcaRequestDTO dados) {
        Marca marca = encontrar(id);
        String nome = dados.nome().trim();
        verificarNome(nome, id);
        marca.nome = nome;
        marcas.flush();
        return MarcaResponseDTO.from(marca);
    }

    @Transactional
    public void excluir(Long id) {
        Marca marca = encontrar(id);
        if (modelos.count("marca.id", id) > 0 || fornecedores.count("marca.id", id) > 0) {
            throw new CadastroException("A marca possui modelos ou fornecedores vinculados.", 409);
        }
        marcas.delete(marca);
    }

    public Marca encontrar(Long id) {
        return marcas.findByIdOptional(id)
                .orElseThrow(() -> new ResourceNotFoundException("Marca nao encontrada"));
    }

    private void verificarNome(String nome, Long idAtual) {
        long repetidas = idAtual == null
                ? marcas.count("lower(nome) = ?1", nome.toLowerCase(Locale.ROOT))
                : marcas.count("lower(nome) = ?1 and id <> ?2", nome.toLowerCase(Locale.ROOT), idAtual);
        if (repetidas > 0) {
            throw new CadastroException("Essa marca ja esta cadastrada.", 409);
        }
    }
}
