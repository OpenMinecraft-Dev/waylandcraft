package dev.evvie.waylandcraft.network;

import java.nio.ByteBuffer;

public class ServerTempBuffer {
    private static final ThreadLocal<ByteBuffer> buffer = new ThreadLocal<>();

    public static ByteBuffer request(int size) {
        if (buffer.get() == null) {
            buffer.set(ByteBuffer.allocateDirect(size));
            return buffer.get();
        }

        if (buffer.get().capacity() < size) {
            buffer.set(ByteBuffer.allocateDirect(size));
            return buffer.get();
        }

        buffer.get().clear();
        buffer.get().limit(size);
        return buffer.get();
    }
}
