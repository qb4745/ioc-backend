package com.cambiaso.ioc.dto.request;

import com.cambiaso.ioc.validation.UniqueEmail;
import com.cambiaso.ioc.validation.ValidSupabaseUUID;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class UsuarioCreateRequest {

    @NotBlank
    @Email
    @UniqueEmail
    private String email;

    // NEW: Password field for automatic Supabase user creation
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    // DEPRECATED: Kept for backward compatibility during migration
    @Deprecated
    @ValidSupabaseUUID
    private UUID supabaseUserId;

    @NotBlank
    private String primerNombre;

    private String segundoNombre;

    @NotBlank
    private String primerApellido;

    private String segundoApellido;

    private Integer plantaId;

    @Size(max = 50)
    private String centroCosto;

    private LocalDate fechaContrato;

    private List<String> roles;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public UUID getSupabaseUserId() { return supabaseUserId; }
    public void setSupabaseUserId(UUID supabaseUserId) { this.supabaseUserId = supabaseUserId; }

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

    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
}
