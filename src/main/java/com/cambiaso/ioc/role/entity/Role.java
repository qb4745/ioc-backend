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
@Table(name = "roles")
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
            name = "role_permissions", // ANÁLISIS: Especifica el nombre de la tabla de unión.
            joinColumns = @JoinColumn(name = "role_id"), // ANÁLISIS: La columna FK que apunta a esta entidad (Role).
            inverseJoinColumns = @JoinColumn(name = "permission_id") // ANÁLISIS: La columna FK que apunta a la otra entidad (Permission).
    )
    private Set<Permission> permissions = new HashSet<>();
    // ANÁLISIS: Usamos un 'Set' en lugar de 'List' porque un rol no puede tener el mismo permiso dos veces.
}