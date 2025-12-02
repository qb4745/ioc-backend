package com.cambiaso.ioc.role.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "role")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String name; // Ejemplos: "Administrador", "Gerente"

    @ManyToMany(fetch = FetchType.LAZY)
    // ANÁLISIS: Define una relación Muchos-a-Muchos. 'FetchType.LAZY' es una optimización
    // crucial: los permisos de un rol solo se cargarán de la BD cuando se acceda a ellos explícitamente,
    // evitando consultas innecesarias.
    @JoinTable(
            name = "role_permission", // tabla de unión ahora en singular
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();
    // ANÁLISIS: Usamos un 'Set' en lugar de 'List' porque un rol no puede tener el mismo permiso dos veces.
}