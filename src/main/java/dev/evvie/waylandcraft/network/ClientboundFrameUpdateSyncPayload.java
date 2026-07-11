package dev.evvie.waylandcraft.network;

import com.mojang.authlib.GameProfile;
import dev.evvie.waylandcraft.WaylandCraftCommon;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.nio.ByteBuffer;

public record ClientboundFrameUpdateSyncPayload(GameProfile profile, long windowHandle, int x, int y, int w, int h, ByteBuffer buffer, int windowWidth, int windowHeight) implements CustomPacketPayload {
    public static final Identifier FRAME_UPDATE_PAYLOAD_ID = Identifier.fromNamespaceAndPath(WaylandCraftCommon.MOD_ID, "frame_update_sync");

    public static final CustomPacketPayload.Type<ClientboundFrameUpdateSyncPayload> TYPE = new CustomPacketPayload.Type<>(FRAME_UPDATE_PAYLOAD_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundFrameUpdateSyncPayload> CODEC = StreamCodec.composite(ByteBufCodecs.GAME_PROFILE, ClientboundFrameUpdateSyncPayload::profile, ByteBufCodecs.LONG, ClientboundFrameUpdateSyncPayload::windowHandle, ByteBufCodecs.VAR_INT, ClientboundFrameUpdateSyncPayload::x, ByteBufCodecs.VAR_INT, ClientboundFrameUpdateSyncPayload::y, ByteBufCodecs.VAR_INT, ClientboundFrameUpdateSyncPayload::w, ByteBufCodecs.VAR_INT, ClientboundFrameUpdateSyncPayload::h, ByteBufCodecsExt.COMPRESSED_BYTE_BUFFER, ClientboundFrameUpdateSyncPayload::buffer, ByteBufCodecs.VAR_INT, ClientboundFrameUpdateSyncPayload::windowWidth, ByteBufCodecs.VAR_INT, ClientboundFrameUpdateSyncPayload::windowHeight, ClientboundFrameUpdateSyncPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}