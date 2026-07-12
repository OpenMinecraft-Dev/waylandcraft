package dev.evvie.waylandcraft.render;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.evvie.waylandcraft.displays.AbstractWindowDisplay;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public static void handleTitleUpdate(GameProfile profile, long handle, String name) {
        windows.stream().filter(a -> a.profile.name().equals(profile.name())).filter(a -> a.handle == handle).findFirst().ifPresent(a -> a.title = name);
    }

    public static void handleWindowClose(GameProfile profile, long handle) {
       var l = windows.stream().filter(a -> a.profile.name().equals(profile.name())).filter(a -> a.handle == handle).findFirst();
        l.ifPresent(remoteWindow -> windows.remove(remoteWindow));
    }

    public static void handleDisplay(GameProfile profile, long handle, Vec3 pivot, Vec3 normal, Vec3 down) {
        windows.stream().filter(a -> a.profile.name().equals(profile.name())).filter(a -> a.handle == handle).findFirst().ifPresent(a -> {
            a.display.pivot = pivot;
            a.display.normal = normal;
            a.display.down = down;
        });
    }

    public static void renderOverlay(GuiGraphicsExtractor context, DeltaTracker tracker) {
        /*windows.forEach(w -> {
            // context.blit(w.ident, 0, 30, 200 * w.texture.getPixels().getWidth() / w.texture.getPixels().getHeight(), 200 + 30,0.0f, 1.0f, 0.0f, 1.0f);
            context.text(Minecraft.getInstance().font, w.title, 0, 0, 0xffffffff);
        });*/
    }

    public static void renderWorld(LevelRenderContext ctx) {
        windows.forEach(a -> {
            // a.display.pivot = Minecraft.getInstance().player.position().add(-2, 0, -2);
            a.display.render(ctx);
        });
    }

    public static class RemoteWindow {
        public GameProfile profile; public long handle; public DynamicTexture texture;
        public Identifier ident;
        public String title = "";
        public AbstractWindowDisplay display;
        public RemoteWindow(GameProfile profile, long handle, DynamicTexture texture) {
            this.profile = profile;
            this.handle = handle;
            this.texture = texture;

            ident = Identifier.fromNamespaceAndPath("waylandcraft", "tmp-" + this.hashCode());
            Minecraft.getInstance().getTextureManager().register(ident, texture);

            display = new RemoteWindowDisplay(this);
        }

        public void resize(int w, int h) {
            Minecraft.getInstance().getTextureManager().release(ident);
            texture = new DynamicTexture("remotetexture-" + handle + "-" + profile.name(), w, h, false);
            Minecraft.getInstance().getTextureManager().register(ident, texture);
        }
    }
}
