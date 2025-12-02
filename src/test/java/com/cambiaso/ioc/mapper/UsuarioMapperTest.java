package com.cambiaso.ioc.mapper;

import com.cambiaso.ioc.dto.response.UsuarioResponse;
import com.cambiaso.ioc.persistence.entity.AppUser;
import com.cambiaso.ioc.persistence.entity.Planta;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioMapperTest {

    private final UsuarioMapper mapper = Mappers.getMapper(UsuarioMapper.class);

    @Test
    void toResponse_buildsFullNameAndMapsFields() {
        AppUser u = new AppUser();
        u.setId(10L);
        u.setEmail("ana@example.com");
        u.setPrimerNombre("Ana");
        u.setSegundoNombre(null); // should be omitted
        u.setPrimerApellido("Pérez");
        u.setSegundoApellido(""); // should be omitted
        Planta p = new Planta();
        p.setId(5);
        p.setCode("PL-01");
        p.setName("Planta Norte");
        u.setPlanta(p);
        u.setCentroCosto("CC-99");
        u.setFechaContrato(LocalDate.of(2023, 6, 1));
        u.setActive(true);
        OffsetDateTime now = OffsetDateTime.now();
        u.setCreatedAt(now);
        u.setUpdatedAt(now);

        List<String> roles = Arrays.asList("ADMIN", "GERENTE");

        UsuarioResponse dto = mapper.toResponse(u, roles);

        assertNotNull(dto);
        assertEquals(10L, dto.getId());
        assertEquals("ana@example.com", dto.getEmail());
        assertEquals("Ana Pérez", dto.getFullName());
        assertEquals(5, dto.getPlantaId());
        assertEquals("PL-01", dto.getPlantaCode());
        assertEquals("Planta Norte", dto.getPlantaName());
        assertEquals("CC-99", dto.getCentroCosto());
        assertEquals(LocalDate.of(2023, 6, 1), dto.getFechaContrato());
        assertTrue(dto.getIsActive());
        assertEquals(now, dto.getCreatedAt());
        assertEquals(now, dto.getUpdatedAt());
        assertEquals(roles, dto.getRoles());
    }

    @Test
    void toResponse_handlesNullPlantaAndEmptyNames() {
        AppUser u = new AppUser();
        u.setId(11L);
        u.setEmail("b@example.com");
        u.setPrimerNombre("  "); // trimmed empty
        u.setSegundoNombre(null);
        u.setPrimerApellido(null);
        u.setSegundoApellido("  ");
        u.setPlanta(null); // null planta should map to null fields
        u.setActive(false);

        UsuarioResponse dto = mapper.toResponse(u, List.of());

        assertNotNull(dto);
        assertNull(dto.getPlantaId());
        assertNull(dto.getPlantaCode());
        assertNull(dto.getPlantaName());
        assertEquals("", dto.getFullName());
        assertFalse(dto.getIsActive());
        assertNotNull(dto.getRoles());
        assertTrue(dto.getRoles().isEmpty());
    }
}

