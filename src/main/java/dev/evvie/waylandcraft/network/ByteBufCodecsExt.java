package dev.evvie.waylandcraft.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.zip.Deflater;

public interface ByteBufCodecsExt {
    Logger logger = LoggerFactory.getLogger("Custom Codecs");
    private static ByteBuffer expandBuffer(ByteBuffer original, int newCapacity) {
        ByteBuffer expanded = ByteBuffer.allocateDirect(newCapacity);
        original.flip();
        expanded.put(original);
        return expanded;
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

    StreamCodec<ByteBuf, ByteBuffer> COMPRESSED_BYTE_BUFFER = new StreamCodec<>() {
        @Override
        public ByteBuffer decode(ByteBuf input) {
            try {
                var targetLength = input.readInt();
                var length = input.readInt();
                /*if (length > 0) {
                    logger.info("{} (rate {}) bytes of compressed frame", length, (1 - length / (double) targetLength) * 100.0);
                }*/
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

    StreamCodec<ByteBuf, ByteBuffer> BYTE_BUFFER = new StreamCodec<>() {
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
