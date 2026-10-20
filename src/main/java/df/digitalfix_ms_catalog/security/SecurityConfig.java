package df.digitalfix_ms_catalog.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import java.util.Set;

// Misma validación del JWT y mismo modelo de roles que ms-login, ms-user y
// ms-workorders. El servicio aún no tiene endpoints: las reglas quedan listas
// para que la API nazca protegida.
//
// Validación del token de Entra ID:
//   - Firma con las claves públicas (JWKS) del tenant; vigencia ("exp", "nbf").
//   - Issuer del tenant (formato v2 o v1) y audience = client ID de la API.
//
// Autorización por rol según el caso (módulo Catálogo y pantalla /catalog):
//   - Consultar servicios, repuestos y stock: Admin y Supervisor.
//   - CRUD de servicios técnicos, tarifas y stock: Admin.
//   - SUPERADMIN hereda todos los roles (RoleHierarchy).
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final EntraJwtAuthenticationConverter jwtAuthenticationConverter;
    private final JsonSecurityErrorHandler jsonSecurityErrorHandler;

    @Value("${AZURE_TENANT_ID}")
    private String tenantId;

    @Value("${AZURE_CLIENT_ID}")
    private String clientId;

    @Value("${CORS_ALLOWED_ORIGINS:http://localhost:4200}")
    private List<String> corsAllowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth

                        /** SWAGGER Y PREFLIGHT CORS */
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        /** CATÁLOGO – consulta */
                        .requestMatchers(HttpMethod.GET, "/api/catalog/**").hasAnyRole("ADMIN", "SUPERVISOR")

                        /** CATÁLOGO – CRUD de servicios, tarifas y stock */
                        .requestMatchers("/api/catalog/**").hasRole("ADMIN")

                        /** RESTO */
                        .anyRequest().authenticated()
                )

                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder())
                                .jwtAuthenticationConverter(jwtAuthenticationConverter))
                        .authenticationEntryPoint(jsonSecurityErrorHandler)
                        .accessDeniedHandler(jsonSecurityErrorHandler))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jsonSecurityErrorHandler)
                        .accessDeniedHandler(jsonSecurityErrorHandler))
                .build();
    }

    // SUPERADMIN hereda los permisos de todos los roles del caso (misma
    // jerarquía que ms-login, ms-user y ms-workorders).
    @Bean
    static RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.withDefaultRolePrefix()
                .role("SUPERADMIN").implies("ADMIN", "SUPERVISOR", "TECNICO", "CLIENTE", "AUDITOR")
                .build();
    }

    // El frontend Angular corre en otro origen (ej. http://localhost:4200).
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(corsAllowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        String issuerV2 = "https://login.microsoftonline.com/" + tenantId + "/v2.0";
        String issuerV1 = "https://sts.windows.net/" + tenantId + "/";

        // Las claves de firma son las mismas para tokens v1 y v2 del tenant.
        NimbusJwtDecoder jwtDecoder = JwtDecoders.fromIssuerLocation(issuerV2);

        Set<String> allowedIssuers = Set.of(issuerV2, issuerV1);
        OAuth2TokenValidator<Jwt> issuerValidator = new JwtClaimValidator<Object>(
                JwtClaimNames.ISS, iss -> iss != null && allowedIssuers.contains(iss.toString()));

        OAuth2TokenValidator<Jwt> audienceValidator = new JwtClaimValidator<Object>(
                JwtClaimNames.AUD, aud -> {
                    if (aud instanceof String s) {
                        return s.equals(clientId) || s.equals("api://" + clientId);
                    }
                    if (aud instanceof List<?> list) {
                        return list.contains(clientId) || list.contains("api://" + clientId);
                    }
                    return false;
                });

        jwtDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefault(), issuerValidator, audienceValidator));
        return jwtDecoder;
    }
}
