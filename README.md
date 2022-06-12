

# Forked repository

This contains only pre-compiled binaries

# Changes
- 

# Supported platforms

- windows (32, 64 bit)
- linux (64 bit)
- mac (64 bit, arm)

--------------

# Description

[Java Image I/O](http://docs.oracle.com/javase/7/docs/api/javax/imageio/package-summary.html) reader and writer for the
[Google WebP](https://developers.google.com/speed/webp/) image format.

# License

webp-imageio is distributed under the [Apache Software License](https://www.apache.org/licenses/LICENSE-2.0) version
2.0.

# Usage

- Add dependency `com.github.usefulness:webp-imageio` to your application
- The WebP reader and writer can be used like any other Image I/O reader and writer.

## Decoding

WebP images can be decoded using default settings as follows.

```
BufferedImage image = ImageIO.read(new File("input.webp"));
```

To customize the WebP decoder settings you need to create instances of ImageReader and WebPReadParam.

```
// Obtain a WebP ImageReader instance
ImageReader reader = ImageIO.getImageReadersByMIMEType("image/webp").next();

// Configure decoding parameters
WebPReadParam readParam = new WebPReadParam();
readParam.setBypassFiltering(true);

// Configure the input on the ImageReader
reader.setInput(new FileImageInputStream(new File("input.webp")));

// Decode the image
BufferedImage image = reader.read(0, readParam);
```

## Encoding

Encoding is done in a similar way to decoding.

You can either use the Image I/O convenience methods to encode using default settings.

```
// Obtain an image to encode from somewhere
BufferedImage image = ImageIO.read(new File("input.png"));

// Encode it as webp using default settings
ImageIO.write(image, "webp", new File("output.webp"));
```

Or you can create an instance of ImageWriter and WebPWriteParam to use custom settings.

```
// Obtain an image to encode from somewhere
BufferedImage image = ImageIO.read(new File("input.png"));

// Obtain a WebP ImageWriter instance
ImageWriter writer = ImageIO.getImageWritersByMIMEType("image/webp").next();

// Configure encoding parameters
WebPWriteParam writeParam = new WebPWriteParam(writer.getLocale());
writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
writeParam.setCompressionType(p.getCompressionTypes()[WebPWriteParam.LOSSLESS_COMPRESSION]);

// Configure the output on the ImageWriter
writer.setOutput(new FileImageOutputStream(new File("output.webp")));

// Encode
writer.write(null, new IIOImage(image, null, null), writeParam);
```
