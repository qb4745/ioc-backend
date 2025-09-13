package com.cambiaso.ioc.user.entity;

import com.cambiaso.ioc.role.entity.Role;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id // ANÁLISIS: La clave primaria es un UUID.
    // OJO: NO usamos @GeneratedValue. El ID será proporcionado por Supabase Auth
    // cuando un usuario se registre. Nuestra aplicación recibirá este ID y lo usará
    // para insertar el registro en esta tabla.
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "full_name") // ANÁLISIS: Mapea el campo 'fullName' a la columna 'full_name' en la BD.
    private String fullName;

    @ManyToOne(fetch = FetchType.LAZY)
    // ANÁLISIS: Define una relación Muchos-a-Uno (muchos usuarios pueden tener un rol).
    // 'FetchType.LAZY' es de nuevo una buena práctica para el rendimiento.
    @JoinColumn(name = "role_id") // ANÁLISIS: Especifica que la columna 'role_id' en la tabla 'users' es la clave foránea.
    private Role role;
}