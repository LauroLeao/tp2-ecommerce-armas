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
                produto.imagem == null ? null : "/produtos/" + produto.id + "/imagem?v=" + produto.imagem.getId());
    }
}
