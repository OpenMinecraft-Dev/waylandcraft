package dev.evvie.waylandcraft.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.evvie.waylandcraft.WaylandCraft;
import dev.evvie.waylandcraft.bridge.WLCToplevel;
import dev.evvie.waylandcraft.item.WindowItem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@Mixin(ItemFrameRenderer.class)
public class ItemFrameRendererMixin {
	
	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;renderStatic(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;IILcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/level/Level;I)V"))
	public void renderItem(ItemRenderer itemRenderer, ItemStack itemStack, ItemDisplayContext ctx, int light, int overlay, PoseStack poseStack, MultiBufferSource multiBufferSource, Level level, int itemFrameEntityId) {
		if(itemStack.is(WindowItem.WINDOW)) {
			WLCToplevel toplevel = WindowItem.getToplevel(itemStack);
			if(toplevel != null) {
				WaylandCraft.instance.windowInItemFrameRenderer.render(toplevel, poseStack, multiBufferSource);
				return;
			}
		}
		
		itemRenderer.renderStatic(itemStack, ctx, light, overlay, poseStack, multiBufferSource, level, itemFrameEntityId);
	}
	
	@Redirect(method = "getFrameModelResourceLoc", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"))
	public boolean redirectItemIsModelLoc(ItemStack itemStack, Item item) {
		if(itemStack.is(WindowItem.WINDOW) && WindowItem.getToplevel(itemStack) != null) return true;
		return itemStack.is(item);
	}

}
