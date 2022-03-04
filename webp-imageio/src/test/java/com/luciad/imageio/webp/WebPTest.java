package com.luciad.imageio.webp;

import org.junit.jupiter.api.Test;

import javax.imageio.*;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.*;
import java.util.Iterator;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class WebPTest {

    @Test
    public void testFindReaderByMimeType() {
        assertNotNull(
                findReader(ImageIO.getImageReadersByMIMEType("image/webp")),
                "Image reader was not registered"
        );
    }

    @Test
    public void testFindReaderByFormatName() {
        assertNotNull(
                findReader(ImageIO.getImageReadersByFormatName("webp")),
                "Image reader was not registered"
        );
    }

    @Test
    public void testFindReaderBySuffix() {
        assertNotNull(
                findReader(ImageIO.getImageReadersBySuffix("webp")),
                "Image reader was not registered"
        );
    }

    @Test
    public void testFindWriterByMimeType() {
        assertNotNull(
                findWriter(ImageIO.getImageWritersByMIMEType("image/webp")),
                "Image writer was not registered"
        );
    }

    @Test
    public void testFindWriterByFormatName() {
        assertNotNull(
                findWriter(ImageIO.getImageWritersByFormatName("webp")),
                "Image writer was not registered"
        );
    }

    @Test
    public void testFindWriterBySuffix() {
        assertNotNull(
                findWriter(ImageIO.getImageWritersBySuffix("webp")),
                "Image writer was not registered"
        );
    }

    @Test
    public void testDecompressLossy() throws IOException {
        byte[] webpData = readResource("lossy.webp");
        BufferedImage image = decompress(webpData);
        assertEquals(1024, image.getWidth());
        assertEquals(752, image.getHeight());
    }

    @Test
    public void testDecompressLossless() throws IOException {
        byte[] webpData = readResource("lossless.webp");
        BufferedImage image = decompress(webpData);
        assertEquals(400, image.getWidth());
        assertEquals(301, image.getHeight());
    }

    @Test
    public void testDecompressLossyAlpha() throws IOException {
        byte[] webpData = readResource("lossy_alpha.webp");
        BufferedImage image = decompress(webpData);
        assertEquals(400, image.getWidth());
        assertEquals(301, image.getHeight());
    }

    private byte[] readResource(String resource) throws IOException {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(resource);
        if (stream == null) {
            throw new FileNotFoundException("Could not load resource " + resource);
        }

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = stream.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            } finally {
                out.close();
            }
            return out.toByteArray();
        } finally {
            stream.close();
        }
    }

    private BufferedImage decompress(byte[] webp) throws IOException {
        ImageReader reader = getImageReader(webp);
        assertNotNull(reader);

        reader.setInput(new MemoryCacheImageInputStream(new ByteArrayInputStream(webp)));
        return reader.read(0);
    }

    @Test
    public void testCompress() throws IOException {
        BufferedImage image = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MemoryCacheImageOutputStream imageOut = new MemoryCacheImageOutputStream(out);

        ImageWriter writer = getImageWriter();
        writer.setOutput(imageOut);
        writer.write(image);

        imageOut.close();
        out.close();

        byte[] data = out.toByteArray();
        assertNotEquals(0, data.length);
        assertEquals('R', data[0] & 0xFF);
        assertEquals('I', data[1] & 0xFF);
        assertEquals('F', data[2] & 0xFF);
        assertEquals('F', data[3] & 0xFF);
    }

    @Test
    public void testRoundtrip() throws IOException {
        BufferedImage image = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        Random rng = new Random(42);
        int[] buffer = ((DataBufferInt) image.getData().getDataBuffer()).getData();
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = rng.nextInt();
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MemoryCacheImageOutputStream imageOut = new MemoryCacheImageOutputStream(out);

        ImageWriter writer = getImageWriter();
        writer.setOutput(imageOut);
        WebPWriteParam writeParam = (WebPWriteParam) writer.getDefaultWriteParam();
        writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        writeParam.setCompressionType("Lossless");
        writer.write(null, new IIOImage(image, null, null), writeParam);

        imageOut.close();
        out.close();

        byte[] webpData = out.toByteArray();

        ImageReader reader = getImageReader();
        reader.setInput(new MemoryCacheImageInputStream(new ByteArrayInputStream(webpData)));
        BufferedImage decodedImage = reader.read(0);

        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                assertEquals(image.getRGB(x, y), decodedImage.getRGB(x, y));
            }
        }
    }

    private ImageWriter getImageWriter() {
        return findWriter(ImageIO.getImageWritersByMIMEType("image/webp"));
    }

    private ImageWriter findWriter(Iterator<ImageWriter> writers) {
        ImageWriter writer = null;
        while (writers.hasNext()) {
            ImageWriter writerCandidate = writers.next();
            if (writerCandidate.getOriginatingProvider() instanceof WebPImageWriterSpi) {
                writer = writerCandidate;
                break;
            }
        }
        return writer;
    }

    private ImageReader getImageReader() {
        return findReader(ImageIO.getImageReadersByMIMEType("image/webp"));
    }

    private ImageReader getImageReader(byte[] data) {
        MemoryCacheImageInputStream stream = new MemoryCacheImageInputStream(new ByteArrayInputStream(data));
        return findReader(ImageIO.getImageReaders(stream));
    }

    private ImageReader findReader(Iterator<ImageReader> readers) {
        ImageReader reader = null;
        while (readers.hasNext()) {
            ImageReader readerCandidate = readers.next();
            if (readerCandidate.getOriginatingProvider() instanceof WebPImageReaderSpi) {
                reader = readerCandidate;
                break;
            }
        }
        return reader;
    }
}
