package dev.evvie.waylandcraft.network.cllentbound;

import com.mojang.authlib.GameProfile;
import dev.evvie.waylandcraft.WaylandCraftCommon;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ClientboundTitleUpdateSyncPayload(GameProfile profile, long handle, String title) implements CustomPacketPayload {
    public static final Identifier TITLE_UPDATE_PAYLOAD_ID = Identifier.fromNamespaceAndPath(WaylandCraftCommon.MOD_ID, "title_update_sync");
    public static final CustomPacketPayload.Type<ClientboundTitleUpdateSyncPayload> TYPE = new CustomPacketPayload.Type<>(TITLE_UPDATE_PAYLOAD_ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundTitleUpdateSyncPayload> CODEC = StreamCodec.composite(ByteBufCodecs.GAME_PROFILE, ClientboundTitleUpdateSyncPayload::profile, ByteBufCodecs.LONG, ClientboundTitleUpdateSyncPayload::handle, ByteBufCodecs.STRING_UTF8, ClientboundTitleUpdateSyncPayload::title, ClientboundTitleUpdateSyncPayload::new);


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
