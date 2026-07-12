package dev.evvie.waylandcraft.network;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.ForkJoinPool;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import dev.evvie.waylandcraft.WaylandCraftCommon;
import dev.evvie.waylandcraft.render.RemoteWindowManager;
import dev.evvie.waylandcraft.utils.IMyServerPlayer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class WaylandCraftNetworking {
	public static void register() {
		PayloadTypeRegistry.serverboundPlay().register(ServerboundGiveItemsPayload.TYPE, ServerboundGiveItemsPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(ServerboundAliveWindowsPayload.TYPE, ServerboundAliveWindowsPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ServerboundFrameUpdatePayload.TYPE, ServerboundFrameUpdatePayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ServerboundTitleUpdatePayload.TYPE, ServerboundTitleUpdatePayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ClientboundFrameUpdateSyncPayload.TYPE, ClientboundFrameUpdateSyncPayload.CODEC);

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

		ServerPlayNetworking.registerGlobalReceiver(ServerboundAliveWindowsPayload.TYPE, (payload, ctx) -> {
			IMyServerPlayer plr = (IMyServerPlayer) ctx.player();
			ArrayList<Long> handles = plr.getAliveWindows();
			handles.clear();
			
			for(long handle : payload.handles()) {
				handles.add(handle);
			}
		});
		
		ServerPlayNetworking.registerGlobalReceiver(ServerboundGiveItemsPayload.TYPE, WaylandCraftCommon.instance.serverItemManager::handleGiveItemsPayload);
        ServerPlayNetworking.registerGlobalReceiver(ServerboundTitleUpdatePayload.TYPE, ((payload, ctx) -> {
            System.out.println("window title updated to " + payload.title());
        }));

        ServerPlayNetworking.registerGlobalReceiver(ServerboundFrameUpdatePayload.TYPE, (payload, ctx) -> {
            if (payload.buffer() == null) {
                return;
            }

            ForkJoinPool.commonPool().execute(() -> {
                var packet = ctx.responseSender().createPacket(new ClientboundFrameUpdateSyncPayload(ctx.player().getGameProfile(), payload.windowHandle(), payload.x(), payload.y(), payload.w(), payload.h(), payload.buffer().rewind(), payload.windowWidth(), payload.windowHeight()));
                ctx.server().getPlayerList().broadcastAll(packet);
            });
        });
	}
}
