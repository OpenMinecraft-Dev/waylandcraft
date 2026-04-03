package dev.evvie.waylandcraft.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import dev.evvie.waylandcraft.WaylandCraft;
import net.minecraft.client.Minecraft;

public class WaylandCraftSettingsManager {
	
	private final WaylandCraft wlc;
	
	private File settingsDir;
	private File keymapFile;
	
	private boolean createKeymap = false;
	
	public WaylandCraftSettingsManager(WaylandCraft wlc) {
		this.wlc = wlc;
		
		try {
			init();
		} catch(IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to read settings storage!");
		}
	}
	
	private void init() throws IOException {
		settingsDir = new File(Minecraft.getInstance().gameDirectory, "waylandcraft");
		if(!settingsDir.exists()) {
			settingsDir.mkdir();
		}
		else if(!settingsDir.isDirectory()) {
			throw new IOException("Waylandcraft settings directory exists but is not a directory");
		}
		
		keymapFile = new File(settingsDir, "keymap.txt");
		if(!keymapFile.exists()) {
			keymapFile.createNewFile();
			createKeymap = true;
		}
		else if(!keymapFile.isFile()) {
			throw new IOException("Waylandcraft keymap.txt exists but is not a file");
		}
		
		if(createKeymap) {
			String keymap = tryReadKeymapFromSystem();
			if(keymap == null) {
				// Use currently existing (probably default) keymap instead
				keymap = wlc.bridge.exportKeymap();
			}
			writeKeymap(keymap);
		}
		
		String keymap = readKeymap();
		if(!wlc.bridge.setKeymapFromStr(keymap)) {
			WaylandCraft.LOGGER.error("Failed to load keymap from file!");
		}
	}
	
	private String tryReadKeymapFromSystem() {
		// Try running xkbcli to get keymap
		String keymap = null;
		try {
			Process process = new ProcessBuilder("xkbcli", "dump-keymap").start();
			byte[] data = process.getInputStream().readAllBytes();
			keymap = new String(data);
			
			int exitCode = process.waitFor();
			if(exitCode != 0) {
				keymap = null;
				WaylandCraft.LOGGER.error("xkbcli exited with error " + exitCode);
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		if(keymap == null) {
			WaylandCraft.LOGGER.error("Failed to dump keymap using xkbcli");
		}
		return keymap;
	}
	
	private void writeKeymap(String keymap) throws IOException {
		FileOutputStream stream = new FileOutputStream(keymapFile);
		stream.write(keymap.getBytes());
		stream.close();
	}
	
	private String readKeymap() throws IOException {
		FileInputStream stream = new FileInputStream(keymapFile);
		byte[] data = stream.readAllBytes();
		String keymap = new String(data);
		stream.close();
		return keymap;
	}
	
}
