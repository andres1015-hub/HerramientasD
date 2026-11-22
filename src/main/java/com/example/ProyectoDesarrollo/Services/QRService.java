package com.example.ProyectoDesarrollo.Services;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class QRService {

    public static final int LONGITUD_MAXIMA_CODIGO = 100;
    private static final int MAX_CONTENIDO = 1000;
    private static final int MIN_DIMENSION = 100;
    private static final int MAX_DIMENSION = 1000;
    private static final Pattern RUTA_VER = Pattern.compile("/qr/ver/([^/?#\\s]+)");

    /**
     * Obtiene el código de un QR a partir de lo que se leyó: el código tal cual,
     * una URL del tipo {@code /qr/ver/{codigo}} o una URL con parámetro
     * {@code codigo} / {@code codigoQR}. Devuelve cadena vacía si no hay contenido.
     */
    public String extraerCodigo(String texto) {
        String limpio = texto == null ? "" : texto.trim();
        if (limpio.isEmpty()) {
            return "";
        }
        try {
            URI uri = URI.create(limpio.replace(" ", "%20"));
            if (uri.getScheme() != null && uri.getRawPath() != null) {
                Matcher ruta = RUTA_VER.matcher(uri.getRawPath());
                if (ruta.find()) {
                    return decodificar(ruta.group(1));
                }
                String consulta = uri.getRawQuery();
                if (consulta != null) {
                    for (String par : consulta.split("&")) {
                        int igual = par.indexOf('=');
                        String clave = igual < 0 ? par : par.substring(0, igual);
                        String valor = igual < 0 ? "" : decodificar(par.substring(igual + 1)).trim();
                        if ((clave.equals("codigo") || clave.equals("codigoQR")) && !valor.isEmpty()) {
                            return valor;
                        }
                    }
                }
            }
        } catch (IllegalArgumentException ignorada) {
            // No era una URL válida: se usa el texto tal cual.
        }
        Matcher coincidencia = RUTA_VER.matcher(limpio);
        if (coincidencia.find()) {
            return decodificar(coincidencia.group(1));
        }
        return limpio;
    }

    private String decodificar(String valor) {
        try {
            return URLDecoder.decode(valor.replace("+", "%2B"), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            return valor;
        }
    }

    public byte[] generarPNG(String contenido, int ancho, int alto) {
        if (contenido == null || contenido.isBlank()) {
            throw new IllegalArgumentException("El contenido del QR es obligatorio");
        }
        if (contenido.length() > MAX_CONTENIDO) {
            throw new IllegalArgumentException("El contenido del QR es demasiado grande");
        }
        if (ancho < MIN_DIMENSION || ancho > MAX_DIMENSION
                || alto < MIN_DIMENSION || alto > MAX_DIMENSION) {
            throw new IllegalArgumentException("Las dimensiones del QR no son validas");
        }

        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 1);

            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(contenido, BarcodeFormat.QR_CODE, ancho, alto, hints);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return out.toByteArray();

        } catch (WriterException | IOException e) {
            throw new IllegalStateException("Error generando QR", e);
        }
    }
}
