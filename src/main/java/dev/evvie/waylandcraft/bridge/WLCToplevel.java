package dev.evvie.waylandcraft.bridge;

import org.jetbrains.annotations.Nullable;

public class WLCToplevel {
	
	// Set to zero when this toplevel no longer exists
	private final long handle;
	
	@Nullable
	private WLCSurface surface;
	
	public WLCToplevel(long handle) {
		this.handle = handle;
	}
	
	protected long getHandle() {
		return this.handle;
	}
	
	protected void setSurface(WLCSurface surface) {
		this.surface = surface;
	}
	
}
