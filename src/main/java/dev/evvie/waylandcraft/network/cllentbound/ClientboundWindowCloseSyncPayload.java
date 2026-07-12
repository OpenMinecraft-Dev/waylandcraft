package dev.evvie.waylandcraft.network.cllentbound;

import com.mojang.authlib.GameProfile;
import dev.evvie.waylandcraft.WaylandCraftCommon;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ClientboundWindowCloseSyncPayload(GameProfile profile, long handle) implements CustomPacketPayload {
    public static final Identifier WINDOW_CLOSE_SYNC_PAYLOAD_ID = Identifier.fromNamespaceAndPath(WaylandCraftCommon.MOD_ID, "window_close_sync");
    public static final CustomPacketPayload.Type<ClientboundWindowCloseSyncPayload> TYPE = new CustomPacketPayload.Type<>(WINDOW_CLOSE_SYNC_PAYLOAD_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundWindowCloseSyncPayload> CODEC = StreamCodec.composite(ByteBufCodecs.GAME_PROFILE, ClientboundWindowCloseSyncPayload::profile, ByteBufCodecs.LONG, ClientboundWindowCloseSyncPayload::handle, ClientboundWindowCloseSyncPayload::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
