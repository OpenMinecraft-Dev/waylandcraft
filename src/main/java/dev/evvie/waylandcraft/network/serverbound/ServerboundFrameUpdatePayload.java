package dev.evvie.waylandcraft.network.serverbound;

import dev.evvie.waylandcraft.WaylandCraftCommon;
import dev.evvie.waylandcraft.network.ByteBufCodecsExt;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.nio.ByteBuffer;

public record ServerboundFrameUpdatePayload(long windowHandle, int x, int y, int w, int h, ByteBuffer buffer, int windowWidth, int windowHeight) implements CustomPacketPayload {
    public static final Identifier FRAME_UPDATE_PAYLOAD_ID = Identifier.fromNamespaceAndPath(WaylandCraftCommon.MOD_ID, "frame_update");

    public static final CustomPacketPayload.Type<ServerboundFrameUpdatePayload> TYPE = new CustomPacketPayload.Type<>(FRAME_UPDATE_PAYLOAD_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundFrameUpdatePayload> CODEC = StreamCodec.composite(ByteBufCodecs.LONG, ServerboundFrameUpdatePayload::windowHandle, ByteBufCodecs.VAR_INT, ServerboundFrameUpdatePayload::x, ByteBufCodecs.VAR_INT, ServerboundFrameUpdatePayload::y, ByteBufCodecs.VAR_INT, ServerboundFrameUpdatePayload::w, ByteBufCodecs.VAR_INT, ServerboundFrameUpdatePayload::h, ByteBufCodecsExt.CLIENTCOMPRESS_FRAME, ServerboundFrameUpdatePayload::buffer, ByteBufCodecs.VAR_INT, ServerboundFrameUpdatePayload::windowWidth, ByteBufCodecs.VAR_INT, ServerboundFrameUpdatePayload::windowHeight, ServerboundFrameUpdatePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
