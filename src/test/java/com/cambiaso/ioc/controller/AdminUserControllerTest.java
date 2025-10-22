package com.cambiaso.ioc.controller;

import com.cambiaso.ioc.controller.admin.AdminUserController;
import com.cambiaso.ioc.dto.request.UsuarioCreateRequest;
import com.cambiaso.ioc.dto.request.UsuarioUpdateRequest;
import com.cambiaso.ioc.dto.response.UsuarioResponse;
import com.cambiaso.ioc.service.UserAdminService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminUserControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UserAdminService userAdminService;

    @BeforeEach
    void setup() {
        AdminUserController controller = new AdminUserController(userAdminService);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.setConstraintValidatorFactory(new ConstraintValidatorFactory() {
            @Override
            public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> key) {
                if (key == null) return null;
                // detect UniqueEmailValidator by simple name to avoid reflective class checks that may NPE
                if (key.getSimpleName().equals("UniqueEmailValidator") || key.getName().contains("UniqueEmailValidator")) {
                    // return a lenient validator that always returns true for tests
                    //noinspection unchecked
                    return (T) new ConstraintValidator<com.cambiaso.ioc.validation.UniqueEmail, String>() {
                        @Override
                        public void initialize(com.cambiaso.ioc.validation.UniqueEmail constraintAnnotation) { }

                        @Override
                        public boolean isValid(String value, jakarta.validation.ConstraintValidatorContext context) {
                            return true;
                        }
                    };
                }
                try {
                    return key.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void releaseInstance(ConstraintValidator<?, ?> instance) {
                // no-op
            }
        });
        validator.afterPropertiesSet();

        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setValidator(validator)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void search_returns200() throws Exception {
        UsuarioResponse u = new UsuarioResponse();
        u.setId(1);
        when(userAdminService.search(eq("q"), eq(null), eq(null), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(u)));

        mockMvc.perform(get("/api/admin/users").param("search", "q"))
                .andExpect(status().isOk());
    }

    @Test
    void getById_returns200() throws Exception {
        UsuarioResponse u = new UsuarioResponse();
        u.setId(2);
        when(userAdminService.getById(2L)).thenReturn(u);

        mockMvc.perform(get("/api/admin/users/2"))
                .andExpect(status().isOk());
    }

    @Test
    void create_returns200() throws Exception {
        UsuarioCreateRequest req = new UsuarioCreateRequest();
        req.setEmail("a@b.com");
        req.setPrimerNombre("X");
        req.setPrimerApellido("Y");
        req.setSupabaseUserId(java.util.UUID.randomUUID());
        UsuarioResponse resp = new UsuarioResponse(); resp.setId(3);
        when(userAdminService.create(any(UsuarioCreateRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/admin/users").contentType(APPLICATION_JSON).content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void update_returns200() throws Exception {
        UsuarioUpdateRequest req = new UsuarioUpdateRequest();
        req.setPrimerNombre("N");
        req.setPrimerApellido("P");
        UsuarioResponse resp = new UsuarioResponse(); resp.setId(4);
        when(userAdminService.update(eq(4L), any(UsuarioUpdateRequest.class))).thenReturn(resp);

        mockMvc.perform(put("/api/admin/users/4").contentType(APPLICATION_JSON).content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void delete_returns204() throws Exception {
        doNothing().when(userAdminService).delete(5L);
        mockMvc.perform(delete("/api/admin/users/5"))
                .andExpect(status().isNoContent());
    }
}
