package com.ecommerce.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
public class ProdutoImagem extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @JdbcTypeCode(SqlTypes.LONGVARBINARY)
    @Column(nullable = false)
    public byte[] conteudo;

    @Column(nullable = false, length = 20)
    public String tipo;

    public Long getId() {
        return id;
    }
}
