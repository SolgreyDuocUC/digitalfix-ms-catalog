package df.digitalfix_ms_catalog.model.common;

import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * Normaliza texto de entrada antes de asignarlo a una entidad (subconjunto del sanitizador de
 * ms-workorders con lo que usa el catálogo).
 * <p>
 * Sanitizar no reemplaza a la validación: aquí sólo se limpia (espacios, caracteres de control,
 * etiquetas HTML); el formato y el largo los valida Bean Validation en cada entidad.
 */
public final class SanitizadorTexto {

    private static final Pattern ETIQUETAS_HTML = Pattern.compile("<[^>]*>");
    private static final Pattern SIGNOS_ANGULARES = Pattern.compile("[<>]");
    private static final Pattern CONTROL_SIN_SALTOS = Pattern.compile("[\\p{Cntrl}&&[^\\n\\t]]");
    private static final Pattern CONTROL_TODOS = Pattern.compile("\\p{Cntrl}");
    private static final Pattern ESPACIOS_HORIZONTALES = Pattern.compile("[ \\t\\u00A0]+");
    private static final Pattern SALTOS_MULTIPLES = Pattern.compile("\\n{3,}");

    private SanitizadorTexto() {
    }

    /** Texto de una sola línea (nombres, referencias). Devuelve {@code null} si queda vacío. */
    public static String texto(String valor) {
        if (valor == null) {
            return null;
        }
        String limpio = quitarMarcado(Normalizer.normalize(valor, Normalizer.Form.NFC));
        limpio = CONTROL_TODOS.matcher(limpio).replaceAll(" ");
        limpio = ESPACIOS_HORIZONTALES.matcher(limpio).replaceAll(" ").trim();
        return limpio.isEmpty() ? null : limpio;
    }

    /** Texto libre multilínea (descripciones). Conserva saltos de línea. */
    public static String textoMultilinea(String valor) {
        if (valor == null) {
            return null;
        }
        String limpio = quitarMarcado(Normalizer.normalize(valor, Normalizer.Form.NFC)
                .replace("\r\n", "\n").replace('\r', '\n'));
        limpio = CONTROL_SIN_SALTOS.matcher(limpio).replaceAll("");
        limpio = ESPACIOS_HORIZONTALES.matcher(limpio).replaceAll(" ");
        limpio = SALTOS_MULTIPLES.matcher(limpio).replaceAll("\n\n").strip();
        return limpio.isEmpty() ? null : limpio;
    }

    /** Texto de una línea con la primera letra en mayúscula ("cable thhn" → "Cable thhn"). */
    public static String capitalizarPrimeraLetra(String valor) {
        String limpio = texto(valor);
        if (limpio == null) {
            return null;
        }
        int primera = limpio.codePointAt(0);
        return new StringBuilder()
                .appendCodePoint(Character.toUpperCase(primera))
                .append(limpio.substring(Character.charCount(primera)))
                .toString();
    }

    private static String quitarMarcado(String valor) {
        String sinEtiquetas = ETIQUETAS_HTML.matcher(valor).replaceAll("");
        return SIGNOS_ANGULARES.matcher(sinEtiquetas).replaceAll("");
    }
}
