package dev.evvie.waylandcraft.render;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import org.lwjgl.opengl.GL11;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.MemorySegment;
import java.nio.ByteBuffer;
import java.util.*;

public class RemoteWindowManager {
    private static Logger logger = LoggerFactory.getLogger(RemoteWindowManager.class);
    private static List<RemoteWindow> windows = new ArrayList<>();
    public static void handleUpdate(GameProfile profile, long handle, int x, int y, int w, int h, int windowWidth, int windowHeight, ByteBuffer data) {
        var win = windows.stream().filter(a -> a.profile.name().equals(profile.name())).filter(a -> a.handle == handle).findFirst();

        if (win.isEmpty()) {
            win = Optional.of(new RemoteWindow(profile, handle, new DynamicTexture("remotetexture-" + handle + "-" + profile.name(), windowWidth, windowHeight, false)));
            windows.add(win.get());
        }
        else {
            var winh = win.get();

            if (winh.texture.getPixels().getWidth() != windowWidth || winh.texture.getPixels().getHeight() != windowHeight) {
                winh.resize(windowWidth, windowHeight);
            }
        }

        RenderSystem.getDevice().createCommandEncoder().writeToTexture(win.get().texture.getTexture(), data, NativeImage.Format.RGBA, 0, 0, x, y, w, h);
    }

    public static void extractState(GuiGraphicsExtractor context, DeltaTracker tracker) {
        windows.forEach(w -> {
            context.blit(w.ident, 0, 0, 125 * w.texture.getPixels().getWidth() / w.texture.getPixels().getHeight(), 125, 0.0f, 1.0f, 0.0f, 1.0f);
        });
    }

    public static class RemoteWindow {
        public GameProfile profile; public long handle; public DynamicTexture texture;
        public Identifier ident;
        public RemoteWindow(GameProfile profile, long handle, DynamicTexture texture) {
            this.profile = profile;
            this.handle = handle;
            this.texture = texture;

            ident = Identifier.fromNamespaceAndPath("waylandcraft", "tmp-" + this.hashCode());
            Minecraft.getInstance().getTextureManager().register(ident, texture);
        }

        public void resize(int w, int h) {
            Minecraft.getInstance().getTextureManager().release(ident);
            texture = new DynamicTexture("remotetexture-" + handle + "-" + profile.name(), w, h, false);
            Minecraft.getInstance().getTextureManager().register(ident, texture);
        }
    }
}
