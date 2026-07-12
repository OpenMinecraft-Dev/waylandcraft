package dev.evvie.waylandcraft.network.cllentbound;

import com.mojang.authlib.GameProfile;
import dev.evvie.waylandcraft.WaylandCraftCommon;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ClientboundDisplayUpdateSyncPayload(GameProfile profile, long handle, double pivotx, double pivoty, double pivotz, double normalx, double normaly, double normalz, double downx, double downy, double downz) implements CustomPacketPayload {
    public static final Identifier DISPLAY_UPDATE_SYNC_PAYLOAD_ID = Identifier.fromNamespaceAndPath(WaylandCraftCommon.MOD_ID, "display_update_sync");
    public static final CustomPacketPayload.Type<ClientboundDisplayUpdateSyncPayload> TYPE = new CustomPacketPayload.Type<>(DISPLAY_UPDATE_SYNC_PAYLOAD_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundDisplayUpdateSyncPayload> CODEC = StreamCodec.composite(ByteBufCodecs.GAME_PROFILE, ClientboundDisplayUpdateSyncPayload::profile, ByteBufCodecs.LONG, ClientboundDisplayUpdateSyncPayload::handle, ByteBufCodecs.DOUBLE, ClientboundDisplayUpdateSyncPayload::pivotx, ByteBufCodecs.DOUBLE, ClientboundDisplayUpdateSyncPayload::pivoty, ByteBufCodecs.DOUBLE, ClientboundDisplayUpdateSyncPayload::pivotz, ByteBufCodecs.DOUBLE, ClientboundDisplayUpdateSyncPayload::normalx, ByteBufCodecs.DOUBLE, ClientboundDisplayUpdateSyncPayload::normaly, ByteBufCodecs.DOUBLE, ClientboundDisplayUpdateSyncPayload::normalz, ByteBufCodecs.DOUBLE, ClientboundDisplayUpdateSyncPayload::downx, ByteBufCodecs.DOUBLE, ClientboundDisplayUpdateSyncPayload::downy, ByteBufCodecs.DOUBLE, ClientboundDisplayUpdateSyncPayload::downz, ClientboundDisplayUpdateSyncPayload::new);
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
