package com.ecommerce.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

@Entity
public class Produto extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, length = 120)
    public String nome;

    @Column(length = 1000)
    public String descricao;

    @Column(nullable = false)
    public Double preco;

    @Column(nullable = false)
    public Integer estoque;

    @ManyToOne(optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    public Categoria categoria;

    // Opcional na tabela para manter produtos criados antes do cadastro de modelos.
    @ManyToOne
    @JoinColumn(name = "modelo_id")
    public Modelo modelo;

    // Opcional para preservar produtos ja cadastrados antes desta relacao.
    @ManyToOne
    @JoinColumn(name = "fornecedor_id")
    public Fornecedor fornecedor;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "imagem_id", unique = true)
    public ProdutoImagem imagem;
}
