package dev.evvie.waylandcraft.bridge;

import java.util.OptionalInt;

import org.jetbrains.annotations.Nullable;

/* Surface render object
 * 
 * Not persistent! Only meant for rendering!
 * These surfaces create a singly linked list through nextChild.
 * A WLCSurface object can represent any wayland surface and does not persistently represent the same one.
 */
public class WLCSurface {
	
	// Current protocol id
	protected int id = -1;
	
	@Nullable
	private WLCBuffer buffer = null;
	
	// Either a child of this surface or one of its siblings
	@Nullable
	private WLCSurface nextChild = null;
	
	// Current protocol id of the parent
	protected OptionalInt parentId = OptionalInt.empty();
	
	@Nullable
	private WLCSurface parent = null;
	
	// X and Y offsets relative to parent coords
	private int xoff = 0;
	private int yoff = 0;
	
	// Total calculated offsets
	public int xSubpos = 0;
	public int ySubpos = 0;
	
	// Total depth
	public int depth = 0;
	
	protected WLCSurface() {
	}
	
	protected void setNextChild(WLCSurface surface) {
		this.nextChild = surface;
	}
	
	protected void setBuffer(WLCBuffer buffer) {
		this.buffer = buffer;
	}
	
	protected void setXOff(int xoff) {
		this.xoff = xoff;
	}

	protected void setYOff(int yoff) {
		this.yoff = yoff;
	}
	
	@Nullable
	public WLCBuffer getBuffer() {
		return this.buffer;
	}
	
	@Nullable
	public WLCSurface getParent() {
		return this.parent;
	}
	
	@Nullable
	public WLCSurface getNextChild() {
		return this.nextChild;
	}
	
	public int getSubOffsetX() {
		return this.xoff;
	}

	public int getSubOffsetY() {
		return this.yoff;
	}
	
}
