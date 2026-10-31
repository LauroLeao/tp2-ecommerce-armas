package com.ecommerce.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "uk_cliente_email", columnNames = "email"),
        @UniqueConstraint(name = "uk_cliente_telefone", columnNames = "telefone"),
        @UniqueConstraint(name = "uk_cliente_cpf", columnNames = "cpf")
})
public class Cliente extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, length = 120)
    public String nome;

    @Column(nullable = false, length = 160)
    public String email;

    @Column(nullable = false, length = 20)
    public String telefone;

    @Column(nullable = false, length = 255)
    public String endereco;

    // Campos novos ficam opcionais na tabela para preservar cadastros antigos.
    // A API exige todos os dados estruturados nos cadastros novos e nas edicoes.
    @Column(length = 11)
    public String cpf;

    @Column(length = 8)
    public String cep;

    @Column(length = 120)
    public String logradouro;

    @Column(length = 20)
    public String numero;

    @Column(length = 120)
    public String complemento;

    @Column(length = 100)
    public String bairro;

    @ManyToOne
    @JoinColumn(name = "municipio_id")
    public Municipio municipio;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "foto_id", unique = true)
    public ClienteFoto foto;
}
