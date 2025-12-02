package com.cambiaso.ioc.mapper;

import com.cambiaso.ioc.dto.response.RoleResponse;
import com.cambiaso.ioc.persistence.entity.Role;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    RoleResponse toResponse(Role entity);

    default RoleResponse toResponse(Role entity, long usersCount, List<String> permissions) {
        RoleResponse response = toResponse(entity);
        response.setUsersCount((int) usersCount);
        response.setPermissions(permissions);
        return response;
    }
}
