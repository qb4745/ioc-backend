package com.cambiaso.ioc.mapper;

import com.cambiaso.ioc.dto.response.PermissionResponse;
import com.cambiaso.ioc.persistence.entity.Permission;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

class PermissionMapperTest {

    private final PermissionMapper mapper = Mappers.getMapper(PermissionMapper.class);

    @Test
    void toResponse_mapsBasicFields() {
        Permission p = new Permission();
        p.setId(7);
        p.setName("READ_REPORTS");
        p.setDescription("Puede leer reportes");

        PermissionResponse dto = mapper.toResponse(p);

        assertNotNull(dto);
        assertEquals(7, dto.getId());
        assertEquals("READ_REPORTS", dto.getName());
        assertEquals("Puede leer reportes", dto.getDescription());
    }
}

