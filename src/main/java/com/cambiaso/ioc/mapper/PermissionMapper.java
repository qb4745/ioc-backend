package com.cambiaso.ioc.mapper;

import com.cambiaso.ioc.dto.response.PermissionResponse;
import com.cambiaso.ioc.persistence.entity.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    PermissionResponse toResponse(Permission entity);
}

