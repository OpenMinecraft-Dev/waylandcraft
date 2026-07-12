package dev.evvie.waylandcraft.network.serverbound;

import dev.evvie.waylandcraft.WaylandCraftCommon;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ServerboundDisplayUpdatePayload(long handle, double pivotx, double pivoty, double pivotz, double normalx, double normaly, double normalz, double downx, double downy, double downz) implements CustomPacketPayload {
    public static final Identifier DISPLAY_UPDATE_PAYLOAD_ID = Identifier.fromNamespaceAndPath(WaylandCraftCommon.MOD_ID, "display_update");
    public static final CustomPacketPayload.Type<ServerboundDisplayUpdatePayload> TYPE = new CustomPacketPayload.Type<>(DISPLAY_UPDATE_PAYLOAD_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundDisplayUpdatePayload> CODEC = StreamCodec.composite(ByteBufCodecs.LONG, ServerboundDisplayUpdatePayload::handle, ByteBufCodecs.DOUBLE, ServerboundDisplayUpdatePayload::pivotx, ByteBufCodecs.DOUBLE, ServerboundDisplayUpdatePayload::pivoty, ByteBufCodecs.DOUBLE, ServerboundDisplayUpdatePayload::pivotz, ByteBufCodecs.DOUBLE, ServerboundDisplayUpdatePayload::normalx, ByteBufCodecs.DOUBLE, ServerboundDisplayUpdatePayload::normaly, ByteBufCodecs.DOUBLE, ServerboundDisplayUpdatePayload::normalz, ByteBufCodecs.DOUBLE, ServerboundDisplayUpdatePayload::downx, ByteBufCodecs.DOUBLE, ServerboundDisplayUpdatePayload::downy, ByteBufCodecs.DOUBLE, ServerboundDisplayUpdatePayload::downz, ServerboundDisplayUpdatePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
