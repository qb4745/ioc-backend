package com.cambiaso.ioc.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class UsuarioUpdateRequest {

    @NotBlank
    private String primerNombre;

    private String segundoNombre;

    @NotBlank
    private String primerApellido;

    private String segundoApellido;

    private Integer plantaId; // nullable to unset

    @Size(max = 50)
    private String centroCosto;

    private LocalDate fechaContrato;

    private Boolean isActive;

    public String getPrimerNombre() { return primerNombre; }
    public void setPrimerNombre(String primerNombre) { this.primerNombre = primerNombre; }

    public String getSegundoNombre() { return segundoNombre; }
    public void setSegundoNombre(String segundoNombre) { this.segundoNombre = segundoNombre; }

    public String getPrimerApellido() { return primerApellido; }
    public void setPrimerApellido(String primerApellido) { this.primerApellido = primerApellido; }

    public String getSegundoApellido() { return segundoApellido; }
    public void setSegundoApellido(String segundoApellido) { this.segundoApellido = segundoApellido; }

    public Integer getPlantaId() { return plantaId; }
    public void setPlantaId(Integer plantaId) { this.plantaId = plantaId; }

    public String getCentroCosto() { return centroCosto; }
    public void setCentroCosto(String centroCosto) { this.centroCosto = centroCosto; }

    public LocalDate getFechaContrato() { return fechaContrato; }
    public void setFechaContrato(LocalDate fechaContrato) { this.fechaContrato = fechaContrato; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}

