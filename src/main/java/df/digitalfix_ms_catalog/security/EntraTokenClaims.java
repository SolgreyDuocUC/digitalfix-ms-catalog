package df.digitalfix_ms_catalog.security;

import org.springframework.security.oauth2.jwt.Jwt;

// Lectura de los claims de identidad de Entra ID. Es una copia idéntica en
// ms-login, ms-user, ms-workorders y ms-catalog, para que todos los servicios
// identifiquen al usuario igual.
// Cubre tokens v1 (upn, unique_name) y v2 (preferred_username).
public final class EntraTokenClaims {

    private EntraTokenClaims() {
    }

    // "oid" es el identificador estable del usuario en todo el tenant ("sub"
    // cambia según la aplicación). Es el valor de USUARIOS.USER_ENTRA_ID.
    public static String userEntraId(Jwt jwt) {
        String oid = jwt.getClaimAsString("oid");
        return oid != null ? oid : jwt.getSubject();
    }

    public static String email(Jwt jwt) {
        for (String claim : new String[]{"preferred_username", "upn", "email", "unique_name"}) {
            String value = jwt.getClaimAsString(claim);
            if (value != null && value.contains("@")) {
                return value;
            }
        }
        return null;
    }

    public static String givenName(Jwt jwt) {
        String given = jwt.getClaimAsString("given_name");
        if (given != null) {
            return given;
        }
        String name = jwt.getClaimAsString("name");
        return name != null ? name.split(" ")[0] : null;
    }

    public static String familyName(Jwt jwt) {
        String family = jwt.getClaimAsString("family_name");
        if (family != null) {
            return family;
        }
        String name = jwt.getClaimAsString("name");
        int space = name != null ? name.indexOf(' ') : -1;
        return space > 0 ? name.substring(space + 1) : null;
    }
}
