package com.cambiaso.ioc;

import com.cambiaso.ioc.dto.response.RoleResponse;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;

public class MvcSerializationTest {

    @Test
    void mappingConverterSerializesPage() {
        RoleResponse resp = new RoleResponse();
        resp.setId(1);
        resp.setName("ADMIN");
        resp.setDescription("desc");
        resp.setUsersCount(2);
        resp.setPermissions(new java.util.ArrayList<>(List.of("READ", "WRITE")));

        PageImpl<RoleResponse> page = new PageImpl<>(List.of(resp), PageRequest.of(0, 1), 1);

        MappingJackson2HttpMessageConverter conv = new MappingJackson2HttpMessageConverter(Jackson2ObjectMapperBuilder.json().build());
        MockHttpOutputMessage out = new MockHttpOutputMessage();
        try {
            conv.write(page, MediaType.APPLICATION_JSON, out);
            String body = out.getBodyAsString();
            System.out.println(body);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Serialization via converter failed: " + t);
        }
    }
}
