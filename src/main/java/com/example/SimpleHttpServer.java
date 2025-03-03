package com.example;

import fi.iki.elonen.NanoHTTPD;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import java.util.Map;

public class SimpleHttpServer extends NanoHTTPD {
    public SimpleHttpServer() throws IOException {
        super(8080);
        start(SOCKET_READ_TIMEOUT, false);
        System.out.println("Server started on port 8080");
    }

    @Override
    public Response serve(IHTTPSession session) {
        Map<String, String> params = session.getParms();
        String text = params.get("text");
        if (text == null) {
            return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain", "Text parameter is missing");
        }

        try {
            BufferedImage barcodeImage = generateQRCodeImage(text);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(barcodeImage, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            return newFixedLengthResponse(Response.Status.OK, "image/png", new ByteArrayInputStream(imageBytes), imageBytes.length);
        } catch (WriterException | IOException e) {
            e.printStackTrace();
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Error generating barcode");
        }
    }

    private BufferedImage generateQRCodeImage(String barcodeText) throws WriterException {
        BitMatrix bitMatrix = new MultiFormatWriter().encode(barcodeText, BarcodeFormat.QR_CODE, 200, 200);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    public static void main(String[] args) {
        try {
            new SimpleHttpServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
