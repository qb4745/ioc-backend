package com.cambiaso.ioc;

import com.cambiaso.ioc.dto.response.RoleResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.fail;

public class SerializationSmokeTest {

    @Test
    void roleResponseSerializationDoesNotThrow() {
        RoleResponse resp = new RoleResponse();
        resp.setId(1);
        resp.setName("ADMIN");
        resp.setDescription("desc");
        resp.setUsersCount(2);
        resp.setPermissions(new ArrayList<>(java.util.List.of("READ", "WRITE")));

        ObjectMapper mapper = new ObjectMapper();
        try {
            String json = mapper.writeValueAsString(resp);
            System.out.println(json);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Serialization failed: " + t);
        }
    }
}

