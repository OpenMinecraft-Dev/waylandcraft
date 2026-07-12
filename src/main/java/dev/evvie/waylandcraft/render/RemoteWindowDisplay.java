package dev.evvie.waylandcraft.render;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.evvie.waylandcraft.WaylandCraft;
import dev.evvie.waylandcraft.compat.IrisCompat;
import dev.evvie.waylandcraft.displays.AbstractWindowDisplay;
import dev.evvie.waylandcraft.displays.FramebufferRenderable;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

import static dev.evvie.waylandcraft.render.RenderUtils.*;

public class RemoteWindowDisplay extends AbstractWindowDisplay {
    private RemoteWindowManager.RemoteWindow window;
    public RemoteWindowDisplay(RemoteWindowManager.RemoteWindow window) {
        this.window = window;
    }
    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void updateGeometry() {
        setPixelScale(1.0f / WaylandCraft.instance.settings.getPixelsPerBlock());
        width = window.texture.getTexture().getWidth(0);
        height = window.texture.getTexture().getHeight(0);
        geometryX = 0;
        geometryY = 0;
    }

    @Override
    public void renderFramebuffer(PoseStack poseStack, SubmitNodeCollector collector, Vec3 origin, Vec3 spanX, Vec3 spanY) {
        if(IrisCompat.isShaderActive()) {
            collector.submitCustomGeometry(poseStack, RenderTypes.entityCutoutCull(window.ident), new RenderUtils.FramebufferRenderInstanceEntity(origin, spanX, spanY, ARGB.white(1.0f), OverlayTexture.NO_OVERLAY, LightCoordsUtil.FULL_BRIGHT, false));
            collector.submitCustomGeometry(poseStack, RenderTypes.entityCutoutCull(window.ident), new RenderUtils.FramebufferRenderInstanceEntity(origin, spanX, spanY, ARGB.black(1.0f), OverlayTexture.NO_OVERLAY, LightCoordsUtil.FULL_BRIGHT, true));
            return;
        }

        Function<Identifier, RenderType> renderType;

        // Front quad
        renderType = WINDOW_CUTOUT_ANTIALIAS;
        collector.submitCustomGeometry(poseStack, renderType.apply(window.ident), new RenderUtils.FramebufferRenderInstance(origin, spanX, spanY, false));

        // Back quad
        renderType = WINDOW_BACKGROUND_CUTOUT;
        collector.submitCustomGeometry(poseStack, renderType.apply(window.ident), new RenderUtils.FramebufferRenderInstance(origin, spanX, spanY, true));
    }

    @Override
    public @Nullable FramebufferRenderable getFramebuffer() {
        return new FramebufferRenderable() {
            @Override
            public int getXOff() {
                return 0;
            }

            @Override
            public int getYOff() {
                return 0;
            }

            @Override
            public int getWidth() {
                return window.texture.getTexture().getWidth(0);
            }

            @Override
            public int getHeight() {
                return window.texture.getTexture().getHeight(0);
            }
        };
    }
}
