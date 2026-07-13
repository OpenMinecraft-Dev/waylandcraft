package dev.evvie.waylandcraft.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.pipeline.RenderPipeline;

import dev.evvie.waylandcraft.WaylandCraft;
import dev.evvie.waylandcraft.WaylandCraftCommon;
import dev.evvie.waylandcraft.utils.CursorShape;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

@Mixin(Gui.class)
public class GuiMixin {
	
	private static final Identifier TLBR_DIAGONAL_CROSSHAIR = Identifier.fromNamespaceAndPath(WaylandCraftCommon.MOD_ID, "crosshair/tlbr_diagonal");
	private static final Identifier TRBL_DIAGONAL_CROSSHAIR = Identifier.fromNamespaceAndPath(WaylandCraftCommon.MOD_ID, "crosshair/trbl_diagonal");
	private static final Identifier LEFT_RIGHT_CROSSHAIR = Identifier.fromNamespaceAndPath(WaylandCraftCommon.MOD_ID, "crosshair/left_right");
	private static final Identifier TOP_BOTTOM_CROSSHAIR = Identifier.fromNamespaceAndPath(WaylandCraftCommon.MOD_ID, "crosshair/top_bottom");
	
	private static final Identifier HELP_CROSSHAIR = Identifier.fromNamespaceAndPath(WaylandCraftCommon.MOD_ID, "crosshair/help");
	private static final Identifier MOVE_CROSSHAIR = Identifier.fromNamespaceAndPath(WaylandCraftCommon.MOD_ID, "crosshair/move");
	private static final Identifier POINTER_CROSSHAIR = Identifier.fromNamespaceAndPath(WaylandCraftCommon.MOD_ID, "crosshair/pointer");
	private static final Identifier TEXT_CROSSHAIR = Identifier.fromNamespaceAndPath(WaylandCraftCommon.MOD_ID, "crosshair/text");
	private static final Identifier VTEXT_CROSSHAIR = Identifier.fromNamespaceAndPath(WaylandCraftCommon.MOD_ID, "crosshair/vtext");
	private static final Identifier WAIT_CROSSHAIR = Identifier.fromNamespaceAndPath(WaylandCraftCommon.MOD_ID, "crosshair/wait");
	private static final Identifier ZOOM_IN_CROSSHAIR = Identifier.fromNamespaceAndPath(WaylandCraftCommon.MOD_ID, "crosshair/zoom_in");
	private static final Identifier ZOOM_OUT_CROSSHAIR = Identifier.fromNamespaceAndPath(WaylandCraftCommon.MOD_ID, "crosshair/zoom_out");
	
	@Redirect(method = "extractCrosshair", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V", ordinal = 0))
	public void crosshairBlitSprite(GuiGraphicsExtractor context, RenderPipeline pipeline, Identifier original, int x, int y, int width, int height) {
		CursorShape cursor = WaylandCraft.instance.cursorShape;
		Identifier crosshair = crosshairForCursor(cursor);
		if(crosshair == null) crosshair = original;
		
		context.blitSprite(pipeline, crosshair, x, y, width, height);
	}
	
	@Inject(method = "extractCrosshair", at = @At("HEAD"), cancellable = true)
	public void crosshairExtractCancel(GuiGraphicsExtractor context, DeltaTracker tracker, CallbackInfo info) {
		if(WaylandCraft.instance.cursorShape == CursorShape.HIDE) info.cancel();
	}
	
	private @Nullable Identifier crosshairForCursor(@Nullable CursorShape cursor) {
		if(cursor == null) return null;

        return switch (cursor) {
            case HIDE -> null;
            case DEFAULT -> null;
            case HELP -> HELP_CROSSHAIR;
            case POINTER -> POINTER_CROSSHAIR;
            case WAIT -> WAIT_CROSSHAIR;
            case TEXT -> TEXT_CROSSHAIR;
            case VERTICAL_TEXT -> VTEXT_CROSSHAIR;
            case E_RESIZE -> LEFT_RIGHT_CROSSHAIR;
            case N_RESIZE -> TOP_BOTTOM_CROSSHAIR;
            case NE_RESIZE -> TRBL_DIAGONAL_CROSSHAIR;
            case NW_RESIZE -> TLBR_DIAGONAL_CROSSHAIR;
            case S_RESIZE -> TOP_BOTTOM_CROSSHAIR;
            case SE_RESIZE -> TLBR_DIAGONAL_CROSSHAIR;
            case SW_RESIZE -> TRBL_DIAGONAL_CROSSHAIR;
            case W_RESIZE -> LEFT_RIGHT_CROSSHAIR;
            case EW_RESIZE -> LEFT_RIGHT_CROSSHAIR;
            case NS_RESIZE -> TOP_BOTTOM_CROSSHAIR;
            case NESW_RESIZE -> TRBL_DIAGONAL_CROSSHAIR;
            case NWSE_RESIZE -> TLBR_DIAGONAL_CROSSHAIR;
            case COL_RESIZE -> LEFT_RIGHT_CROSSHAIR;
            case ROW_RESIZE -> TOP_BOTTOM_CROSSHAIR;
            case ZOOM_IN -> ZOOM_IN_CROSSHAIR;
            case ZOOM_OUT -> ZOOM_OUT_CROSSHAIR;
            case ALL_RESIZE -> MOVE_CROSSHAIR;
            default -> null;
        };
	}
	
}
