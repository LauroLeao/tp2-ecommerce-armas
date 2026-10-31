package com.ecommerce.dto;

import com.ecommerce.model.Produto;

public record ProdutoResponseDTO(
        Long id,
        String nome,
        String descricao,
        Double preco,
        Integer estoque,
        Long categoriaId,
        String categoriaNome,
        Long marcaId,
        String marcaNome,
        Long modeloId,
        String modeloNome,
        Long fornecedorId,
        String fornecedorNome,
        String imagemUrl) {

    public static ProdutoResponseDTO from(Produto produto) {
        return new ProdutoResponseDTO(
                produto.id,
                produto.nome,
                produto.descricao,
                produto.preco,
                produto.estoque,
                produto.categoria.id,
                produto.categoria.nome,
                produto.modelo == null ? null : produto.modelo.marca.id,
                produto.modelo == null ? null : produto.modelo.marca.nome,
                produto.modelo == null ? null : produto.modelo.id,
                produto.modelo == null ? null : produto.modelo.nome,
                produto.fornecedor == null ? null : produto.fornecedor.id,
                produto.fornecedor == null ? null : produto.fornecedor.nome,
                produto.imagem == null ? null : "/produtos/" + produto.id + "/imagem?v=" + produto.imagem.getId());
    }
}
