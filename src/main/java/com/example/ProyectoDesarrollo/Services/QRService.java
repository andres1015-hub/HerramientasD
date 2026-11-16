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
import java.util.HashMap;
import java.util.Map;

@Service
public class QRService {

    private static final int MIN_DIMENSION = 50;
    private static final int MAX_DIMENSION = 1000;
    private static final int MAX_CONTENIDO = 4096;

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
