package dev.evvie.waylandcraft.render;

import dev.evvie.waylandcraft.bridge.WLCSurface;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class WindowCopyBuffer {
    private static final Map<WLCSurface, ByteBuffer> buffers = new HashMap<>();

    public static ByteBuffer request(WLCSurface surface, int size) {
        if (buffers.get(surface) == null) {
            buffers.put(surface, ByteBuffer.allocateDirect(size));
            return buffers.get(surface);
        }

        if (buffers.get(surface).capacity() < size) {
            buffers.put(surface, ByteBuffer.allocateDirect(size));
            return buffers.get(surface);
        }

        buffers.get(surface).clear();
        buffers.get(surface).limit(size);
        return buffers.get(surface);
    }
}
