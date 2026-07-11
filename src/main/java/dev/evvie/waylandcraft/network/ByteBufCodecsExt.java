package dev.evvie.waylandcraft.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

public interface ByteBufCodecsExt {
    Logger logger = LoggerFactory.getLogger("Custom Codecs");
    StreamCodec<ByteBuf, ByteBuffer> BYTEBUFFER = new StreamCodec<>() {
        @Override
        public ByteBuffer decode(ByteBuf input) {
            try {
                var length = input.readInt();
                logger.info("read {} bytes", length);
                var result = ByteBuffer.allocate(length); // WindowCopyBuffer.requestRead(length);
                input.readBytes(result);
                return result;
            }
            catch (Exception e) {
                logger.warn("failed to receive data", e);
                return null;
            }
        }

        @Override
        public void encode(ByteBuf output, ByteBuffer value) {
            output.writeInt(value.limit());
            output.writeBytes(value);
            logger.info("write {} bytes", value.limit());
        }
    };
}
