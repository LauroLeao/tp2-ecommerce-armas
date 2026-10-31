package com.ecommerce.service;

import com.ecommerce.dto.ModeloRequestDTO;
import com.ecommerce.dto.ModeloResponseDTO;
import com.ecommerce.exception.CadastroException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Modelo;
import com.ecommerce.model.Produto;
import com.ecommerce.repository.ModeloRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Locale;

@ApplicationScoped
public class ModeloService {
    private final ModeloRepository modelos;
    private final MarcaService marcas;

    public ModeloService(ModeloRepository modelos, MarcaService marcas) {
        this.modelos = modelos;
        this.marcas = marcas;
    }

    public List<ModeloResponseDTO> listar(Long marcaId) {
        List<Modelo> resultado = marcaId == null
                ? modelos.find("order by marca.nome, nome").list()
                : modelos.find("marca.id = ?1 order by nome", marcaId).list();
        return resultado.stream().map(ModeloResponseDTO::from).toList();
    }

    public ModeloResponseDTO buscar(Long id) {
        return ModeloResponseDTO.from(encontrar(id));
    }

    @Transactional
    public ModeloResponseDTO cadastrar(ModeloRequestDTO dados) {
        verificarNome(dados.nome().trim(), dados.marcaId(), null);
        Modelo modelo = new Modelo();
        preencher(modelo, dados);
        modelos.persist(modelo);
        modelos.flush();
        return ModeloResponseDTO.from(modelo);
    }

    @Transactional
    public ModeloResponseDTO editar(Long id, ModeloRequestDTO dados) {
        Modelo modelo = encontrar(id);
        if (!modelo.marca.id.equals(dados.marcaId()) && Produto.count("modelo.id", id) > 0) {
            throw new CadastroException("Nao altere a marca de um modelo com produtos vinculados.", 409);
        }
        verificarNome(dados.nome().trim(), dados.marcaId(), id);
        preencher(modelo, dados);
        modelos.flush();
        return ModeloResponseDTO.from(modelo);
    }

    @Transactional
    public void excluir(Long id) {
        Modelo modelo = encontrar(id);
        if (Produto.count("modelo.id", id) > 0) {
            throw new CadastroException("O modelo possui produtos vinculados.", 409);
        }
        modelos.delete(modelo);
    }

    private Modelo encontrar(Long id) {
        return modelos.findByIdOptional(id)
                .orElseThrow(() -> new ResourceNotFoundException("Modelo nao encontrado"));
    }

    private void preencher(Modelo modelo, ModeloRequestDTO dados) {
        modelo.nome = dados.nome().trim();
        modelo.marca = marcas.encontrar(dados.marcaId());
    }

    private void verificarNome(String nome, Long marcaId, Long idAtual) {
        long repetidos = idAtual == null
                ? modelos.count("marca.id = ?1 and lower(nome) = ?2", marcaId, nome.toLowerCase(Locale.ROOT))
                : modelos.count("marca.id = ?1 and lower(nome) = ?2 and id <> ?3", marcaId,
                        nome.toLowerCase(Locale.ROOT), idAtual);
        if (repetidos > 0) {
            throw new CadastroException("Esse modelo ja existe para a marca selecionada.", 409);
        }
    }
}
