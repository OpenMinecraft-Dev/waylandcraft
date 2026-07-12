package dev.evvie.waylandcraft.network.serverbound;

import dev.evvie.waylandcraft.WaylandCraftCommon;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ServerboundTitleUpdatePayload(long handle, String title) implements CustomPacketPayload {
    public static final Identifier TIILE_UPDATE_PAYLOAD_ID = Identifier.fromNamespaceAndPath(WaylandCraftCommon.MOD_ID, "title_update");
    public static final CustomPacketPayload.Type<ServerboundTitleUpdatePayload> TYPE = new CustomPacketPayload.Type<>(TIILE_UPDATE_PAYLOAD_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundTitleUpdatePayload> CODEC = StreamCodec.composite(ByteBufCodecs.LONG, ServerboundTitleUpdatePayload::handle, ByteBufCodecs.STRING_UTF8, ServerboundTitleUpdatePayload::title, ServerboundTitleUpdatePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
