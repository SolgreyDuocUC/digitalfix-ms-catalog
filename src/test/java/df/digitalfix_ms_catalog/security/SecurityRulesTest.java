package df.digitalfix_ms_catalog.security;

import df.digitalfix_ms_catalog.service.UserRoleClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// ms-catalog aún no tiene endpoints: una ruta que pasa la seguridad responde
// 404 (no existe el handler) y una que no pasa, 401/403.
@WebMvcTest
@Import({SecurityConfig.class, EntraJwtAuthenticationConverter.class, JsonSecurityErrorHandler.class})
@TestPropertySource(properties = {"AZURE_TENANT_ID=test-tenant", "AZURE_CLIENT_ID=test-client"})
class SecurityRulesTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;
    @MockitoBean
    private UserRoleClient userRoleClient;

    private MockHttpServletRequestBuilder as(MockHttpServletRequestBuilder request, String role) {
        return request.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_" + role)));
    }

    @Test
    void sinTokenResponde401() throws Exception {
        mockMvc.perform(get("/api/catalog/services")).andExpect(status().isUnauthorized());
    }

    @Test
    void supervisorConsultaPeroNoModificaElCatalogo() throws Exception {
        mockMvc.perform(as(get("/api/catalog/services"), "SUPERVISOR")).andExpect(status().isNotFound());
        mockMvc.perform(as(post("/api/catalog/services"), "SUPERVISOR")).andExpect(status().isForbidden());
    }

    @Test
    void clienteNoAccedeAlCatalogo() throws Exception {
        mockMvc.perform(as(get("/api/catalog/services"), "CLIENTE")).andExpect(status().isForbidden());
    }

    @Test
    void adminYSuperadminAdministranElCatalogo() throws Exception {
        mockMvc.perform(as(post("/api/catalog/services"), "ADMIN")).andExpect(status().isNotFound());
        mockMvc.perform(as(post("/api/catalog/services"), "SUPERADMIN")).andExpect(status().isNotFound());
    }
}
