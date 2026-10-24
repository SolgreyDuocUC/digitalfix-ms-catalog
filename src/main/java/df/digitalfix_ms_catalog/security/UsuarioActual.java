package df.digitalfix_ms_catalog.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

// Quién hace la petición, para registrarlo en MOVIMIENTO_STOCK.USUARIO. Se prefiere el correo
// (legible en una auditoría); si el token no lo trae, el OID de Entra ID.
public final class UsuarioActual {

    private UsuarioActual() {
    }

    public static String identificador() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken token) {
            String email = EntraTokenClaims.email(token.getToken());
            return email != null ? email : EntraTokenClaims.userEntraId(token.getToken());
        }
        return auth != null ? auth.getName() : null;
    }
}
