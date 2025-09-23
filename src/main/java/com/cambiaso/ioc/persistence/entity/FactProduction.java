package com.cambiaso.ioc.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Entity
@Table(name = "fact_production")
public class FactProduction {

    @EmbeddedId
    private FactProductionId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maquina_fk", nullable = false)
    private DimMaquina maquina;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maquinista_fk")
    private DimMaquinista maquinista;

    @Column(name = "numero_log", nullable = false)
    private Long numeroLog;

    @Column(name = "hora_contabilizacion", nullable = false)
    private LocalTime horaContabilizacion;

    @Column(name = "fecha_notificacion", nullable = false)
    private LocalDate fechaNotificacion;

    @Column
    private Long documento;

    @Column(name = "material_sku", nullable = false)
    private Long materialSku;

    @Column(name = "material_descripcion")
    private String materialDescripcion;

    @Column(name = "numero_pallet")
    private Integer numeroPallet;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal cantidad;

    @Column(name = "peso_neto", nullable = false, precision = 18, scale = 4)
    private BigDecimal pesoNeto;

    @Column(length = 10)
    private String lista;

    @Column(name = "version_produccion", length = 50)
    private String versionProduccion;

    @Column(name = "centro_costos")
    private Long centroCostos;

    @Column(nullable = false, length = 10)
    private String turno;

    @Column(length = 10)
    private String jornada;

    @Column(name = "usuario_sap", length = 100)
    private String usuarioSap;

    @Column(length = 100)
    private String bodeguero;

    // Added to support parsing of "Status" column
    @Column(name = "status_origen", length = 10)
    private String statusOrigen;
}
