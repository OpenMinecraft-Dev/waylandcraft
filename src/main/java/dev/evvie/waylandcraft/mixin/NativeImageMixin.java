package dev.evvie.waylandcraft.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import com.mojang.blaze3d.platform.NativeImage;

@Mixin(NativeImage.class)
public interface NativeImageMixin {
	@Invoker(value = "<init>")
	public static NativeImage fromPtr(NativeImage.Format fmt, int width, int height, boolean useStbFree, long ptr) {
		return null;
	}
}
