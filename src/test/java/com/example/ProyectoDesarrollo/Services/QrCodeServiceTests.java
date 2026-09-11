package com.example.ProyectoDesarrollo.Services;

import org.junit.jupiter.api.Test;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class QrCodeServiceTests {

    @Test
    void generatesPngQrCode() throws Exception {
        String content = "INVENTARIO-QR|codigo=TEST-01|nombre=Escáner 日本語";
        byte[] png = new QrCodeService().generatePng(content, 180);

        assertArrayEquals(new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47}, new byte[]{png[0], png[1], png[2], png[3]});
        var image = ImageIO.read(new ByteArrayInputStream(png));
        assertNotNull(image);
        assertEquals(180, image.getWidth());
        assertEquals(180, image.getHeight());
        var bitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image)));
        assertEquals(content, new MultiFormatReader().decode(bitmap).getText());
    }
}
