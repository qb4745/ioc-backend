package com.cambiaso.ioc.dto.response;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public class UsuarioResponse {
    private long id;
    private String email;
    private String fullName;
    private Integer plantaId;
    private String plantaCode;
    private String plantaName;
    private String centroCosto;
    private LocalDate fechaContrato;
    private boolean isActive;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private List<String> roles;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Integer getPlantaId() { return plantaId; }
    public void setPlantaId(Integer plantaId) { this.plantaId = plantaId; }

    public String getPlantaCode() { return plantaCode; }
    public void setPlantaCode(String plantaCode) { this.plantaCode = plantaCode; }

    public String getPlantaName() { return plantaName; }
    public void setPlantaName(String plantaName) { this.plantaName = plantaName; }

    public String getCentroCosto() { return centroCosto; }
    public void setCentroCosto(String centroCosto) { this.centroCosto = centroCosto; }

    public LocalDate getFechaContrato() { return fechaContrato; }
    public void setFechaContrato(LocalDate fechaContrato) { this.fechaContrato = fechaContrato; }

    public boolean getIsActive() { return isActive; }
    public void setIsActive(boolean isActive) { this.isActive = isActive; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
}

