package df.digitalfix_ms_catalog.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

// Resuelve el rol de negocio del usuario (ADMIN, SUPERVISOR, TECNICO, CLIENTE)
// consultando a ms-user, que es dueño de USUARIOS/ROLES. Equivale a cargar los
// roles de la BD en el backend de referencia, pero respetando el límite del
// microservicio: se reenvía el mismo JWT del usuario a GET /api/user/me.
//
// Se guarda en caché por usuario para no llamar a ms-user en cada petición; si
// ms-user no responde o el usuario no está registrado, no se agrega ningún rol
// y solo pasan las rutas que piden el scope, no las que piden un rol.
@Slf4j
@Service
public class UserRoleClient {

    private static final Duration ROLE_TTL = Duration.ofMinutes(5);
    private static final Duration MISSING_ROLE_TTL = Duration.ofMinutes(1);
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(2);
    // Holgado a propósito: la primera consulta tras levantar ms-user incluye
    // descargar el JWKS de Entra ID y abrir la conexión a Oracle (varios segundos).
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(8);

    private record CachedRole(Optional<String> role, Instant expiresAt) {
    }

    private final RestClient restClient;
    private final Map<String, CachedRole> cache = new ConcurrentHashMap<>();

    public UserRoleClient(@Value("${digitalfix.ms-user.url:http://localhost:8082}") String msUserUrl) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(CONNECT_TIMEOUT);
        requestFactory.setReadTimeout(READ_TIMEOUT);
        this.restClient = RestClient.builder()
                .baseUrl(msUserUrl)
                .requestFactory(requestFactory)
                .build();
    }

    public Optional<String> findRoleCode(String userEntraId, String bearerToken) {
        CachedRole cached = cache.get(userEntraId);
        if (cached != null && cached.expiresAt().isAfter(Instant.now())) {
            return cached.role();
        }

        Optional<String> role;
        try {
            role = fetchRoleCode(bearerToken);
        } catch (RestClientException e) {
            // Un fallo de ms-user (caído o lento al arrancar) no se guarda en caché:
            // la próxima petición vuelve a consultar, en vez de dejar al usuario
            // sin rol (y con 403) durante MISSING_ROLE_TTL.
            log.warn("No se pudo obtener el rol desde ms-user: {}", e.getMessage());
            return Optional.empty();
        }
        cache.put(userEntraId, new CachedRole(role, Instant.now().plus(role.isPresent() ? ROLE_TTL : MISSING_ROLE_TTL)));
        return role;
    }

    // Al cerrar sesión se olvida el rol: el próximo login lo vuelve a consultar.
    public void evict(String userEntraId) {
        cache.remove(userEntraId);
    }

    private Optional<String> fetchRoleCode(String bearerToken) {
        Map<?, ?> profile = restClient.get()
                .uri("/api/user/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
                .retrieve()
                .body(Map.class);
        Object code = profile != null ? profile.get("codigoRol") : null;
        return code != null ? Optional.of(code.toString().toUpperCase()) : Optional.empty();
    }
}
