package com.cambiaso.ioc.role.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data // ANÁLISIS: Genera automáticamente getters, setters, equals, hashCode y toString.
@NoArgsConstructor // ANÁLISIS: Genera un constructor sin argumentos, requerido por JPA.
@AllArgsConstructor // ANÁLISIS: Genera un constructor con todos los argumentos, útil para tests.
@Entity // ANÁLISIS: Marca esta clase como una entidad JPA, lo que significa que se mapeará a una tabla.
@Table(name = "permission") // ANÁLISIS: Especifica que el nombre de la tabla en la BD es "permission".
public class Permission {

    @Id // ANÁLISIS: Marca este campo como la clave primaria de la tabla.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // ANÁLISIS: Le dice a Hibernate que la BD se encargará de generar el ID (usando SERIAL de Postgres).
    private Integer id;

    @Column(unique = true, nullable = false)
    // ANÁLISIS: Mapea este campo a una columna. 'unique=true' crea una restricción de unicidad
    // y 'nullable=false' la hace obligatoria (NOT NULL).
    private String name; // Ejemplos: "CREATE_USER", "VIEW_DASHBOARD"
}