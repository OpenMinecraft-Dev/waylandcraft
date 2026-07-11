package dev.evvie.waylandcraft.network;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

import dev.evvie.waylandcraft.WaylandCraftCommon;
import dev.evvie.waylandcraft.bridge.WaylandCraftBridge;
import dev.evvie.waylandcraft.utils.IMyServerPlayer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class WaylandCraftNetworking {
	
	public static void register() {
		PayloadTypeRegistry.serverboundPlay().register(ServerboundGiveItemsPayload.TYPE, ServerboundGiveItemsPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(ServerboundAliveWindowsPayload.TYPE, ServerboundAliveWindowsPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ServerboundFrameUpdatePayload.TYPE, ServerboundFrameUpdatePayload.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(ServerboundAliveWindowsPayload.TYPE, (payload, ctx) -> {
			IMyServerPlayer plr = (IMyServerPlayer) ctx.player();
			ArrayList<Long> handles = plr.getAliveWindows();
			handles.clear();
			
			for(long handle : payload.handles()) {
				handles.add(handle);
			}
		});
		
		ServerPlayNetworking.registerGlobalReceiver(ServerboundGiveItemsPayload.TYPE, WaylandCraftCommon.instance.serverItemManager::handleGiveItemsPayload);

        ServerPlayNetworking.registerGlobalReceiver(ServerboundFrameUpdatePayload.TYPE, (payload, ctx) -> {
            // System.out.println(payload);
            // System.out.println("reveiced window data of " + payload.windowHandle());

            /*Path path = Paths.get(String.format("%x-%dx%d@%dx%d.bin", payload.windowHandle(), payload.w(), payload.h(), payload.x(), payload.y()));

            if (payload.buffer().remaining() > 0) {
                try (FileChannel channel = FileChannel.open(path,
                        StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
                    channel.write(payload.buffer());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }*/
        });
	}
}
