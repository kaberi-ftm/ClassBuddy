package com.classbuddy.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class QRCodeGenerator {

    public static String generateToDataDir(String text, String fileNameNoExt) {
        Path dir = Path.of(DatabaseUtil.getDataDir(), "qrcodes");
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create QR directory", e);
        }

        Path out = dir.resolve(fileNameNoExt + ".png");
        writePng(text, out, 240, 240);
        return out.toString();
    }

    private static void writePng(String text, Path out, int width, int height) {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix matrix = writer.encode(text, BarcodeFormat.QR_CODE, width, height);
            MatrixToImageWriter.writeToPath(matrix, "PNG", out);
        } catch (WriterException | IOException e) {
            throw new RuntimeException("QR generation failed", e);
        }
    }
}
