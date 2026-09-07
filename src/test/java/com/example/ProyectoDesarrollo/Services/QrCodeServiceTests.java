package com.example.ProyectoDesarrollo.Services;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QrCodeServiceTests {

    @Test
    void generatesPngQrCode() throws Exception {
        byte[] png = new QrCodeService().generatePng("INVENTARIO-QR|codigo=TEST-01", 180);

        assertTrue(png.length > 500);
        assertArrayEquals(new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47}, new byte[]{png[0], png[1], png[2], png[3]});
    }
}
