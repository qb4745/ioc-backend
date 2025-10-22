package com.cambiaso.ioc.mapper;

import com.cambiaso.ioc.dto.response.RoleResponse;
import com.cambiaso.ioc.persistence.entity.Role;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    RoleResponse toResponse(Role entity);
}

