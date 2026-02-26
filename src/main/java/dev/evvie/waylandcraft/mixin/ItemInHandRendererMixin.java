package dev.evvie.waylandcraft.mixin;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;

import dev.evvie.waylandcraft.item.WindowItem;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
	
	@Inject(method = "renderArmWithItem", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V"), cancellable = true)
	public void renderArmWithItem(
		AbstractClientPlayer player,
		float partialTicks,
		float yaw,
		InteractionHand interactionHand,
		float attack,
		ItemStack itemStack,
		float handHeight,
		PoseStack poseStack,
		MultiBufferSource multiBufferSource,
		int light,
		CallbackInfo info,
		@Local LocalRef<HumanoidArm> humanoidArm
	) {
		if(!itemStack.is(WindowItem.WINDOW)) return;
		info.cancel();
		
		poseStack.pushPose();
		
		shadow$renderOneHandedMap(poseStack, multiBufferSource, light, handHeight, humanoidArm.get(), attack, itemStack);
		
		poseStack.popPose();
	}
	
	@Redirect(method = "renderOneHandedMap", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderMap(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/item/ItemStack;)V"))
	public void renderMap(ItemInHandRenderer renderer, PoseStack poseStack, MultiBufferSource source, int light, ItemStack itemStack) {
		if(!itemStack.is(WindowItem.WINDOW)) {
			shadow$renderMap(poseStack, source, light, itemStack);
			return;
		}
		
		poseStack.translate(-0.5, -0.5, 0);
		
		Pose pose = poseStack.last();
		VertexConsumer buffer = source.getBuffer(RenderType.itemEntityTranslucentCull(MissingTextureAtlasSprite.getLocation()));
		Vector3f pos1 = pose.pose().transformPosition(0, 1, 0, new Vector3f());
		Vector3f pos2 = pose.pose().transformPosition(0, 0, 0, new Vector3f());
		Vector3f pos3 = pose.pose().transformPosition(1, 0, 0, new Vector3f());
		Vector3f pos4 = pose.pose().transformPosition(1, 1, 0, new Vector3f());
		
		Vector2f uv1 = new Vector2f(0, 0);
		Vector2f uv2 = new Vector2f(0, 1);
		Vector2f uv3 = new Vector2f(1, 1);
		Vector2f uv4 = new Vector2f(1, 0);
		
		Vector3f normal = pose.transformNormal(0, 0, 1, new Vector3f());
		
		int overlayCoords = OverlayTexture.NO_OVERLAY;
		
		// Front quad
		buffer.vertex(/* pos */ pos1.x, pos1.y, pos1.z, /* color */ 1, 1, 1, 1, /* uv */ uv1.x, uv1.y, /* overlay */ overlayCoords, /* uv2 */ light, /* normal */ normal.x, normal.y, normal.z);
		buffer.vertex(/* pos */ pos2.x, pos2.y, pos2.z, /* color */ 1, 1, 1, 1, /* uv */ uv2.x, uv2.y, /* overlay */ overlayCoords, /* uv2 */ light, /* normal */ normal.x, normal.y, normal.z);
		buffer.vertex(/* pos */ pos3.x, pos3.y, pos3.z, /* color */ 1, 1, 1, 1, /* uv */ uv3.x, uv3.y, /* overlay */ overlayCoords, /* uv2 */ light, /* normal */ normal.x, normal.y, normal.z);
		buffer.vertex(/* pos */ pos4.x, pos4.y, pos4.z, /* color */ 1, 1, 1, 1, /* uv */ uv4.x, uv4.y, /* overlay */ overlayCoords, /* uv2 */ light, /* normal */ normal.x, normal.y, normal.z);
	}
	
	@Shadow
	public abstract void shadow$renderOneHandedMap(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, float f, HumanoidArm humanoidArm, float g, ItemStack itemStack);
	
	@Shadow
	public abstract void shadow$renderMap(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, ItemStack itemStack);
	
}
