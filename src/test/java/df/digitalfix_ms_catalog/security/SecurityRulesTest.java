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

// Solo se prueba la capa de seguridad: "controllers" apunta a una clase que no es controlador,
// así no se carga ninguno y una ruta que pasa la seguridad responde 404 (no hay handler) y una
// que no pasa, 401/403.
@WebMvcTest(controllers = SecurityRulesTest.class)
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
        mockMvc.perform(get("/api/catalog/repuestos")).andExpect(status().isUnauthorized());
    }

    @Test
    void supervisorConsultaPeroNoModificaElCatalogo() throws Exception {
        mockMvc.perform(as(get("/api/catalog/repuestos"), "SUPERVISOR")).andExpect(status().isNotFound());
        mockMvc.perform(as(post("/api/catalog/repuestos"), "SUPERVISOR")).andExpect(status().isForbidden());
    }

    @Test
    void clienteNoAccedeAlCatalogo() throws Exception {
        mockMvc.perform(as(get("/api/catalog/repuestos"), "CLIENTE")).andExpect(status().isForbidden());
    }

    @Test
    void adminYSuperadminAdministranElCatalogo() throws Exception {
        mockMvc.perform(as(post("/api/catalog/repuestos"), "ADMIN")).andExpect(status().isNotFound());
        mockMvc.perform(as(post("/api/catalog/repuestos"), "SUPERADMIN")).andExpect(status().isNotFound());
    }

    @Test
    void supervisorDescuentaYDevuelveStockAlOperarOrdenes() throws Exception {
        mockMvc.perform(as(post("/api/catalog/stock/salidas"), "SUPERVISOR")).andExpect(status().isNotFound());
        mockMvc.perform(as(post("/api/catalog/stock/devoluciones"), "SUPERVISOR")).andExpect(status().isNotFound());
    }

    @Test
    void supervisorNoRepoStockManualmente() throws Exception {
        mockMvc.perform(as(post("/api/catalog/repuestos/1/entradas"), "SUPERVISOR")).andExpect(status().isForbidden());
    }

    @Test
    void clienteYTecnicoNoMuevenStock() throws Exception {
        mockMvc.perform(as(post("/api/catalog/stock/salidas"), "CLIENTE")).andExpect(status().isForbidden());
        mockMvc.perform(as(post("/api/catalog/stock/salidas"), "TECNICO")).andExpect(status().isForbidden());
    }
}
