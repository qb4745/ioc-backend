package com.cambiaso.ioc.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class FactProductionId implements Serializable {

    @Column(name = "id", insertable = false, updatable = false)
    private Long id;

    @Column(name = "fecha_contabilizacion", nullable = false)
    private LocalDate fechaContabilizacion;
}
