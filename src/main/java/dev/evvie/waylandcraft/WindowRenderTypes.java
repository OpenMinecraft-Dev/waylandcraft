package dev.evvie.waylandcraft;

import java.util.function.Function;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;

import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

public class WindowRenderTypes {
	
	public static RenderType windowItem(int texture) {
		return DummyRenderType.WINDOW_ITEM.apply(texture);
	}
	
	/* This whole subclass dummy is necessary to access the RenderType.CompositeState class */
	private static class DummyRenderType extends RenderType {
		
		public DummyRenderType(String string, VertexFormat vertexFormat, Mode mode, int i, boolean bl, boolean bl2, Runnable runnable, Runnable runnable2) {
			super(string, vertexFormat, mode, i, bl, bl2, runnable, runnable2);
			throw new IllegalStateException("DummyRenderType constructor called");
		}
		
		public static Function<Integer, RenderType> WINDOW_ITEM = Util.memoize(DummyRenderType::windowItem);
		public static final RenderStateShard.ShaderStateShard RENDERTYPE_WINDOW_ITEM = new RenderStateShard.ShaderStateShard(RenderUtils::getRendertypeWindowItem);
		
		private static RenderType windowItem(int texture) {
			RenderType.CompositeState compositeState = RenderType.CompositeState.builder()
					.setShaderState(RENDERTYPE_WINDOW_ITEM)
					.setTextureState(new TextureIdShard(texture))
					.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
					.setOutputState(ITEM_ENTITY_TARGET)
					.setLightmapState(NO_LIGHTMAP)
					.setOverlayState(NO_OVERLAY)
					.setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
					.createCompositeState(true);
			return create("wlc_window_item", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, RenderType.TRANSIENT_BUFFER_SIZE, true, true, compositeState);
		}
		
		private static class TextureIdShard extends RenderStateShard.EmptyTextureStateShard {
			
			public TextureIdShard(int texture) {
				super(() -> {
					RenderSystem.setShaderTexture(0, texture);
				}, () -> {});
			}
			
		}
		
	}
	
}
