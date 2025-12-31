package dev.evvie.waylandcraft.bridge;

import org.jetbrains.annotations.Nullable;

public class WLCToplevel extends WLCAbstractWindow {
	
	@Nullable
	public String title;
	
	@Nullable
	public String appID;
	
	// Set to true when a toplevel requests to be minimized
	public boolean minimizeRequest = false;
	
	// Set to true when a toplevel requests to be maximized
	public boolean maximizeRequest = false;
	
	// Set to true when a toplevel requests to be unmaximized
	public boolean unmaximizeRequest = false;
	
	@Nullable
	public SurfaceGeometry maximizeRestoreGeometry = null;
	
	public WLCToplevel(long handle) {
		super(handle);
	}
	
}
