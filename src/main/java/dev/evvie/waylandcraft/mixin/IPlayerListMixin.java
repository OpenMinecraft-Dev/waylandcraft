package dev.evvie.waylandcraft.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(PlayerList.class)
public interface IPlayerListMixin {
    @Accessor("players")
    List<ServerPlayer> players();
}
