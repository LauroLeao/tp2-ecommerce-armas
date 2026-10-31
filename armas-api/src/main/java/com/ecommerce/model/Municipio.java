package com.ecommerce.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Municipio extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, length = 7, unique = true)
    public String codigoIbge;

    @Column(nullable = false, length = 100)
    public String nome;

    @ManyToOne(optional = false)
    @JoinColumn(name = "estado_id", nullable = false)
    public Estado estado;
}
