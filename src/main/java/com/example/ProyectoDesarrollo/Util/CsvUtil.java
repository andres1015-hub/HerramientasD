package com.example.ProyectoDesarrollo.Util;

import java.nio.charset.StandardCharsets;
import java.util.List;

/** Generación de CSV compatible con Excel (UTF-8 con BOM) y protegido contra inyección de fórmulas. */
public final class CsvUtil {

    private CsvUtil() {
    }

    public static String fila(Object... valores) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < valores.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(celda(valores[i]));
        }
        return sb.append("\r\n").toString();
    }

    public static byte[] aBytes(List<String> filas) {
        String texto = "﻿" + String.join("", filas);
        return texto.getBytes(StandardCharsets.UTF_8);
    }

    private static String celda(Object valor) {
        if (valor == null) {
            return "";
        }
        String texto = String.valueOf(valor);
        // Solo los textos pueden contener fórmulas; los números se escriben tal cual.
        if (valor instanceof CharSequence && !texto.isEmpty() && "=+-@\t\r".indexOf(texto.charAt(0)) >= 0) {
            texto = "'" + texto;
        }
        if (texto.contains(",") || texto.contains("\"") || texto.contains("\n") || texto.contains("\r")) {
            texto = "\"" + texto.replace("\"", "\"\"") + "\"";
        }
        return texto;
    }
}
