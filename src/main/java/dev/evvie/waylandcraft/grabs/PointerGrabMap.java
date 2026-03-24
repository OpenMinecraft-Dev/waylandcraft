package dev.evvie.waylandcraft.grabs;

import java.util.HashSet;

import dev.evvie.waylandcraft.WaylandCraft;
import dev.evvie.waylandcraft.WindowDisplay;
import dev.evvie.waylandcraft.WindowDisplay.DisplayHitResult;
import dev.evvie.waylandcraft.bridge.WLCAbstractWindow;
import dev.evvie.waylandcraft.bridge.WLCSurface;
import net.minecraft.world.phys.Vec3;

public class PointerGrabMap {
	
	private WaylandCraft wlc;
	private PointerGrab exclusiveGrab = null;
	private ImplicitGrab implicitGrab = null;
	
	public PointerGrabMap(WaylandCraft wlc) {
		this.wlc = wlc;
	}
	
	public boolean isGrabActive() {
		return implicitGrab != null || exclusiveGrab != null;
	}
	
	public boolean isExclusiveGrabActive() {
		return exclusiveGrab != null;
	}
	
	public boolean isGrabActive(int button) {
		return (exclusiveGrab != null && exclusiveGrab.button == button) || (implicitGrab != null && implicitGrab.pressedButtons.contains(button));
	}
	
	// Start implicit pointer grab on a surface. Surface MUST have active pointer focus!
	public void startImplicit(WindowDisplay window, WLCSurface surface, int button) {
		if(isExclusiveGrabActive()) return;
		
		if(implicitGrab == null) implicitGrab = new ImplicitGrab(window, surface);
		if(implicitGrab.pressedButtons.contains(button)) return;
		
		implicitGrab.pressedButtons.add(button);
		wlc.bridge.sendButton(0x110 + button, 1);
	}
	
	public void startExclusive(PointerGrab grab) {
		if(isExclusiveGrabActive()) return;
		
		this.releaseImplicit();
		
		try {
			grab.init();
		} catch (GrabDroppedException e) {
			return;
		}
		
		exclusiveGrab = grab;
	}
	
	public void moveWorld(Vec3 pos, Vec3 view, Vec3 up) {
		if(exclusiveGrab != null) {
			try {
				exclusiveGrab.moveWorld(pos, view, up);
			} catch(GrabDroppedException e) {
				exclusiveGrab = null;
			}
			
			return;
		}
		
		if(implicitGrab == null) return;
		
		DisplayHitResult hitResult = implicitGrab.window.intersect(pos, view);
		if(hitResult == null) return;
		
		Vec3 relativeCoords = hitResult.surfaceLocalOrigin.subtract(implicitGrab.surface.xSubpos, implicitGrab.surface.ySubpos, 0);
		wlc.bridge.sendMotion(relativeCoords.x, relativeCoords.y);
	}
	
	public void hover(WLCAbstractWindow window, WLCSurface surface, double x, double y) {
		if(exclusiveGrab != null) {
			try {
				exclusiveGrab.hover(window, surface, x, y);
			} catch(GrabDroppedException e) {
				exclusiveGrab = null;
			}
		}
	}
	
	public void release(int button) {
		if(exclusiveGrab != null && exclusiveGrab.button == button) {
			try {
				exclusiveGrab.release();
			} catch (GrabDroppedException e) {
				// No handling necessary, grab always removed
			}
			exclusiveGrab = null;
			return;
		}
		
		if(implicitGrab == null) return;
		
		if(implicitGrab.pressedButtons.contains(button)) {
			wlc.bridge.sendButton(0x110 + button, 0);
			implicitGrab.pressedButtons.remove(button);
		}
		
		if(implicitGrab.pressedButtons.isEmpty()) implicitGrab = null;
	}
	
	private void releaseImplicit() {
		if(implicitGrab == null) return;
		
		implicitGrab.pressedButtons.forEach((button) -> wlc.bridge.sendButton(0x110 + button, 0));
		implicitGrab = null;
	}
	
	public void releaseAll() {
		this.releaseImplicit();
		
		if(exclusiveGrab == null) return;
		
		try {
			exclusiveGrab.release();
		} catch (GrabDroppedException e) {
			// No handling necessary, grab always removed
		}
		exclusiveGrab = null;
	}
	
	// Not a real pointer grab, just a way to represent active button presses on a WindowDisplay
	private static class ImplicitGrab {
		
		public final WindowDisplay window;
		public final WLCSurface surface;
		public HashSet<Integer> pressedButtons = new HashSet<Integer>();
		
		public ImplicitGrab(WindowDisplay window, WLCSurface surface) {
			this.window = window;
			this.surface = surface;
		}
		
	}
	
}
