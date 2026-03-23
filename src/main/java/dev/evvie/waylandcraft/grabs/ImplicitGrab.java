package dev.evvie.waylandcraft.grabs;

import dev.evvie.waylandcraft.WaylandCraft;
import dev.evvie.waylandcraft.WindowDisplay;
import dev.evvie.waylandcraft.WindowDisplay.DisplayHitResult;
import dev.evvie.waylandcraft.bridge.WLCAbstractWindow;
import dev.evvie.waylandcraft.bridge.WLCSurface;
import net.minecraft.world.phys.Vec3;

public class ImplicitGrab extends PointerGrab {
	
	public final WLCSurface surface;
	public final WLCAbstractWindow window;
	
	public ImplicitGrab(WLCAbstractWindow window, WLCSurface surface, int button) {
		super(button);
		this.window = window;
		this.surface = surface;
	}
	
	@Override
	public boolean exclusive() {
		return false;
	}
	
	private void checkValid() throws GrabDroppedException {
		if(!window.isAlive() || !surface.isAlive()) {
			this.drop();
		}
	}
	
	public void init() throws GrabDroppedException {
		this.checkValid();
		
		// 0x110 is BTN_LEFT, see linux/input-event-codes.h
		wlc.bridge.sendButton(0x110 + button, 1);
	}
	
	@Override
	public void release() throws GrabDroppedException {
		this.checkValid();
		
		// 0x110 is BTN_LEFT, see linux/input-event-codes.h
		wlc.bridge.sendButton(0x110 + button, 0);
	}
	
	@Override
	public void moveWorld(Vec3 pos, Vec3 view, Vec3 up) throws GrabDroppedException {
		this.checkValid();
		
		WindowDisplay display = WaylandCraft.instance.getDisplay(window);
		if(display == null) return;
		
		DisplayHitResult hitResult = display.intersect(pos, view);
		if(hitResult == null) return;
		
		Vec3 relativeCoords = hitResult.surfaceLocalOrigin.subtract(surface.xSubpos, surface.ySubpos, 0);
		wlc.bridge.sendMotion(relativeCoords.x, relativeCoords.y);
	}
	
	@Override
	public void hover(WLCAbstractWindow window, WLCSurface surface, double x, double y) throws GrabDroppedException {
	}
	
}
