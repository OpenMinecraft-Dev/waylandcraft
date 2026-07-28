package dev.evvie.waylandcraft.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.zip.Deflater;

public interface ByteBufCodecsExt {
    Logger logger = LoggerFactory.getLogger("Custom Codecs");
    private static ByteBuffer expandBuffer(ByteBuffer original, int newCapacity) {
        ByteBuffer expanded = ByteBuffer.allocateDirect(newCapacity);
        original.flip();
        expanded.put(original);
        return expanded;
    }
    static ByteBuffer compressToJpeg(ByteBuffer pixelBuffer, int width, int height, float quality) throws IOException {
        // 1. 将 ByteBuffer 转为 BufferedImage（ARGB → RGB，JPEG 无透明通道）
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int[] pixels = new int[width * height];

        // 读取所有像素（假设像素缓冲区为 ARGB 四个字节，顺序 A,R,G,B 或 R,G,B,A 取决于字节序）
        // 这里以常见的 ARGB 顺序（即 int 中最高字节为 Alpha）为例，直接复制为 int 数组
        ByteBuffer dup = pixelBuffer.duplicate(); // 不改变原指针
        IntBuffer intBuf = dup.asIntBuffer();
        intBuf.get(pixels);
        for (int i = 0; i < pixels.length; i++) {
            int c = pixels[i];
            int r = (c >> 24) & 0xFF; // 原来高位是 R
            int g = (c >> 16) & 0xFF;
            int b = (c >> 8) & 0xFF;
            int a = c & 0xFF;         // 低位是 A
            pixels[i] = (a << 24) | (r << 16) | (g << 8) | b; // 组装为 ARGB
        }
        image.setRGB(0, 0, width, height, pixels, 0, width);

        // 2. 创建 RGB 图像（去掉 Alpha）
        BufferedImage rgbImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        rgbImage.createGraphics().drawImage(image, 0, 0, null);
        image.flush(); // 释放 ARGB 图像

        // 3. JPEG 编码
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(quality);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
            writer.setOutput(ios);
            writer.write(null, new IIOImage(rgbImage, null, null), param);
        } finally {
            writer.dispose();
            rgbImage.flush();
        }

        // 4. 包装为 ByteBuffer（堆内存）
        return ByteBuffer.wrap(baos.toByteArray());
    }
    public static ByteBuffer decompressToDirect(ByteBuffer jpegBuffer) throws IOException {
        // 1. 读取 JPEG 字节
        byte[] jpegBytes;
        if (jpegBuffer.hasArray()) {
            int offset = jpegBuffer.arrayOffset() + jpegBuffer.position();
            int length = jpegBuffer.remaining();
            jpegBytes = new byte[length];
            System.arraycopy(jpegBuffer.array(), offset, jpegBytes, 0, length);
        } else {
            jpegBytes = new byte[jpegBuffer.remaining()];
            jpegBuffer.duplicate().get(jpegBytes);
        }

        // 2. 解码
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(jpegBytes));
        if (image == null) throw new IOException("Failed to decode JPEG");
        int width = image.getWidth();
        int height = image.getHeight();

        // 3. 转换为 ARGB 并获取像素
        BufferedImage argbImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = argbImage.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        image.flush();

        int[] pixels = new int[width * height];
        argbImage.getRGB(0, 0, width, height, pixels, 0, width);
        argbImage.flush();

        // 4. 转换为 RGBA
        for (int i = 0; i < pixels.length; i++) {
            int c = pixels[i];
            int a = (c >> 24) & 0xFF;
            int r = (c >> 16) & 0xFF;
            int gg = (c >> 8) & 0xFF;
            int b = c & 0xFF;
            pixels[i] = (r << 24) | (gg << 16) | (b << 8) | a;
        }

        // 5. 写入 Direct Buffer
        ByteBuffer directBuf = ByteBuffer.allocateDirect(width * height * 4);
        IntBuffer intView = directBuf.asIntBuffer();
        intView.put(pixels);
        directBuf.position(width * height * 4);
        return directBuf;
    }
    static ByteBuffer compress(ByteBuffer input) {
        Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION, false);
        try {
            deflater.setInput(input);
            deflater.finish();

            int maxOutput = input.remaining() + 32;
            ByteBuffer output = ByteBuffer.allocateDirect(maxOutput);

            while (!deflater.finished()) {
                if (!output.hasRemaining()) {
                    output = expandBuffer(output, output.capacity() * 2);
                }
                try {
                    int len = deflater.deflate(output);
                }
                catch (Exception e) {
                    output = expandBuffer(output, output.capacity() * 2);
                }
            }
            output.flip();
            return output;
        } finally {
            deflater.end();
        }
    }

    StreamCodec<ByteBuf, ByteBuffer> CLIENTCOMPRESS_FRAME = new StreamCodec<>() {
        @Override
        public ByteBuffer decode(ByteBuf input) {
            try {
                var targetLength = input.readInt();
                var length = input.readInt();
                if (length < 0) {
                    return null;
                }

                // var buf = ServerTempBuffer.request(length);
                var buf = ByteBuffer.allocate(length);
                input.readBytes(buf);
                return buf;
            }
            catch (Exception e) {
                // logger.warn("failed to receive data", e);
                return null;
            }
        }

        @Override
        public void encode(ByteBuf output, ByteBuffer value) {
            if (value.remaining() <= 0) {
                output.writeInt(-1);
                output.writeInt(-1);
                return;
            }
            output.writeInt(value.remaining());
            ByteBuffer temp = compress(value);
            try {
                int compressedLen = temp.remaining();
                output.writeInt(compressedLen);
                output.writeBytes(temp);
            } catch (Exception e) {
                logger.warn("compress fail", e);
            }
        }
    };

    StreamCodec<ByteBuf, ByteBuffer> COMPRESSED_FRAME = new StreamCodec<>() {
        @Override
        public ByteBuffer decode(ByteBuf input) {
            try {
                var length = input.readInt();
                var buf = ByteBuffer.allocateDirect(length);
                input.readBytes(buf);
                return buf;
            }
            catch (Exception e) {
                // logger.warn("failed to receive data", e);
                return null;
            }
        }

        @Override
        public void encode(ByteBuf output, ByteBuffer value) {
            if (value.remaining() <= 0) {
                output.writeInt(-1);
                return;
            }
            output.writeInt(value.remaining());
            output.writeBytes(value);
        }
    };
}
