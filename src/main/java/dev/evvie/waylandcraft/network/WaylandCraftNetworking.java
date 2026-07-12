package dev.evvie.waylandcraft.network;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.ForkJoinPool;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import dev.evvie.waylandcraft.WaylandCraftCommon;
import dev.evvie.waylandcraft.mixin.IPlayerListMixin;
import dev.evvie.waylandcraft.network.cllentbound.ClientboundDisplayUpdateSyncPayload;
import dev.evvie.waylandcraft.network.cllentbound.ClientboundFrameUpdateSyncPayload;
import dev.evvie.waylandcraft.network.cllentbound.ClientboundTitleUpdateSyncPayload;
import dev.evvie.waylandcraft.network.cllentbound.ClientboundWindowCloseSyncPayload;
import dev.evvie.waylandcraft.network.serverbound.*;
import dev.evvie.waylandcraft.render.RemoteWindowManager;
import dev.evvie.waylandcraft.utils.IMyServerPlayer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.phys.Vec3;

public class WaylandCraftNetworking {
	public static void register() {
		PayloadTypeRegistry.serverboundPlay().register(ServerboundGiveItemsPayload.TYPE, ServerboundGiveItemsPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(ServerboundAliveWindowsPayload.TYPE, ServerboundAliveWindowsPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ServerboundFrameUpdatePayload.TYPE, ServerboundFrameUpdatePayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ServerboundTitleUpdatePayload.TYPE, ServerboundTitleUpdatePayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ServerboundWindowClosePayload.TYPE, ServerboundWindowClosePayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ServerboundDisplayUpdatePayload.TYPE, ServerboundDisplayUpdatePayload.CODEC);

        PayloadTypeRegistry.clientboundPlay().register(ClientboundFrameUpdateSyncPayload.TYPE, ClientboundFrameUpdateSyncPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ClientboundTitleUpdateSyncPayload.TYPE, ClientboundTitleUpdateSyncPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ClientboundWindowCloseSyncPayload.TYPE, ClientboundWindowCloseSyncPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ClientboundDisplayUpdateSyncPayload.TYPE, ClientboundDisplayUpdateSyncPayload.CODEC);

        ClientPlayNetworking.registerGlobalReceiver(ClientboundFrameUpdateSyncPayload.TYPE, (payload, ctx) -> {
            if (payload.buffer() != null) {
                var dir = ByteBuffer.allocateDirect(payload.w() * payload.h() * 4);
                try (Inflater def = new Inflater(false)) {
                    def.setInput(payload.buffer().rewind());
                    def.inflate(dir);
                } catch (DataFormatException e) {
                    throw new RuntimeException(e);
                }

                RemoteWindowManager.handleUpdate(payload.profile(), payload.windowHandle(), payload.x(), payload.y(), payload.w(), payload.h(), payload.windowWidth(), payload.windowHeight(), dir.rewind());
            }
        });
        ClientPlayNetworking.registerGlobalReceiver(ClientboundTitleUpdateSyncPayload.TYPE, (payload, _) -> RemoteWindowManager.handleTitleUpdate(payload.profile(), payload.handle(), payload.title()));
        ClientPlayNetworking.registerGlobalReceiver(ClientboundWindowCloseSyncPayload.TYPE, (payload, _) -> RemoteWindowManager.handleWindowClose(payload.profile(), payload.handle()));
        ClientPlayNetworking.registerGlobalReceiver(ClientboundDisplayUpdateSyncPayload.TYPE, (payload, _) -> RemoteWindowManager.handleDisplay(payload.profile(), payload.handle(), new Vec3(payload.pivotx(), payload.pivoty(), payload.pivotz()), new Vec3(payload.normalx(), payload.normaly(), payload.normalz()), new Vec3(payload.downx(), payload.downy(), payload.downz())));

		ServerPlayNetworking.registerGlobalReceiver(ServerboundAliveWindowsPayload.TYPE, (payload, ctx) -> {
			IMyServerPlayer plr = (IMyServerPlayer) ctx.player();
			ArrayList<Long> handles = plr.getAliveWindows();
			handles.clear();
			
			for(long handle : payload.handles()) {
				handles.add(handle);
			}
		});
		
		ServerPlayNetworking.registerGlobalReceiver(ServerboundGiveItemsPayload.TYPE, WaylandCraftCommon.instance.serverItemManager::handleGiveItemsPayload);
        ServerPlayNetworking.registerGlobalReceiver(ServerboundTitleUpdatePayload.TYPE, ((payload, ctx) -> broadcastAllExcept(ctx.server().getPlayerList(), ctx.player(), new ClientboundTitleUpdateSyncPayload(ctx.player().getGameProfile(), payload.handle(), payload.title()))));

        ServerPlayNetworking.registerGlobalReceiver(ServerboundFrameUpdatePayload.TYPE, (payload, ctx) -> {
            if (payload.buffer() == null) {
                return;
            }

            ForkJoinPool.commonPool().execute(() -> broadcastAllExcept(ctx.server().getPlayerList(), ctx.player(), new ClientboundFrameUpdateSyncPayload(ctx.player().getGameProfile(), payload.windowHandle(), payload.x(), payload.y(), payload.w(), payload.h(), payload.buffer().rewind(), payload.windowWidth(), payload.windowHeight())));
        });

        ServerPlayNetworking.registerGlobalReceiver(ServerboundWindowClosePayload.TYPE, (payload, ctx) -> broadcastAllExcept(ctx.server().getPlayerList(), ctx.player(), new ClientboundWindowCloseSyncPayload(ctx.player().getGameProfile(), payload.handle())));

        ServerPlayNetworking.registerGlobalReceiver(ServerboundDisplayUpdatePayload.TYPE, (payload, ctx) -> {
            broadcastAllExcept(ctx.server().getPlayerList(), ctx.player(), new ClientboundDisplayUpdateSyncPayload(ctx.player().getGameProfile(), payload.handle(), payload.pivotx(), payload.pivoty(), payload.pivotz(), payload.normalx(), payload.normaly(), payload.normalz(), payload.downx(), payload.downy(), payload.downz()));
        });
	}

    public static void broadcastAllExcept(PlayerList list, ServerPlayer player, CustomPacketPayload payload) {
        var l = (IPlayerListMixin) list;
        for (var p : l.players()) {
            if (p != player) {
                ServerPlayNetworking.send(p, payload);
            }
        }
    }
}
