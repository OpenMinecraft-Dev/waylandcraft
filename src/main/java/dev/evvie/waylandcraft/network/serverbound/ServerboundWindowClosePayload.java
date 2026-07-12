package dev.evvie.waylandcraft.network.serverbound;

import dev.evvie.waylandcraft.WaylandCraftCommon;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ServerboundWindowClosePayload(long handle) implements CustomPacketPayload {
    public static final Identifier WINDOW_CLOSE_PAYLOAD_ID = Identifier.fromNamespaceAndPath(WaylandCraftCommon.MOD_ID, "window_close");
    public static final CustomPacketPayload.Type<ServerboundWindowClosePayload> TYPE = new CustomPacketPayload.Type<>(WINDOW_CLOSE_PAYLOAD_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundWindowClosePayload> CODEC = StreamCodec.composite(ByteBufCodecs.LONG, ServerboundWindowClosePayload::handle, ServerboundWindowClosePayload::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
