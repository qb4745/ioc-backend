package com.cambiaso.ioc.mapper;

import com.cambiaso.ioc.dto.response.UsuarioResponse;
import com.cambiaso.ioc.persistence.entity.AppUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    @Mapping(target = "id", source = "entity.id")
    @Mapping(target = "email", source = "entity.email")
    @Mapping(target = "fullName", expression = "java(getFullName(entity))")
    @Mapping(target = "plantaId", source = "entity.planta.id")
    @Mapping(target = "plantaCode", source = "entity.planta.code")
    @Mapping(target = "plantaName", source = "entity.planta.name")
    @Mapping(target = "centroCosto", source = "entity.centroCosto")
    @Mapping(target = "fechaContrato", source = "entity.fechaContrato")
    @Mapping(target = "isActive", source = "entity.active")
    @Mapping(target = "createdAt", source = "entity.createdAt")
    @Mapping(target = "updatedAt", source = "entity.updatedAt")
    @Mapping(target = "roles", source = "roles")
    UsuarioResponse toResponse(AppUser entity, List<String> roles);

    default String getFullName(AppUser u) {
        return Stream.of(u.getPrimerNombre(), u.getSegundoNombre(), u.getPrimerApellido(), u.getSegundoApellido())
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(" "));
    }
}
