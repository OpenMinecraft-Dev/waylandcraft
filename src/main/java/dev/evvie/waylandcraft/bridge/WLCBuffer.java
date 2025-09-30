package dev.evvie.waylandcraft.bridge;

public class WLCBuffer {
	
	public final int width;
	public final int height;
	
	// SHM data pointer. Can be zero for non-shm buffers!
	public final long shmDataPtr;
	
	protected WLCBuffer(int width, int height, long shmDataPtr) {
		this.width = width;
		this.height = height;
		this.shmDataPtr = shmDataPtr;
	}
	
}
