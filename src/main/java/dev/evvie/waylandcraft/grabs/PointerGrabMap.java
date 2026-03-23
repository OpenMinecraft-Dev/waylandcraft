package dev.evvie.waylandcraft.grabs;

import java.util.ArrayList;

import org.jetbrains.annotations.Nullable;

import dev.evvie.waylandcraft.bridge.WLCAbstractWindow;
import dev.evvie.waylandcraft.bridge.WLCSurface;
import net.minecraft.world.phys.Vec3;

public class PointerGrabMap {
	
	private ArrayList<PointerGrab> grabs = new ArrayList<PointerGrab>();
	
	public boolean isGrabActive() {
		return !grabs.isEmpty();
	}
	
	public boolean isGrabActive(int button) {
		return getGrab(button) != null;
	}
	
	public boolean isExclusiveGrabActive() {
		for(PointerGrab grab : grabs) {
			if(grab.exclusive()) return true;
		}
		
		return false;
	}
	
	private @Nullable PointerGrab getGrab(int button) {
		for(PointerGrab grab : grabs) {
			if(grab.button == button) return grab;
		}
		
		return null;
	}
	
	public void startImplicit(WLCAbstractWindow window, WLCSurface surface, int button) {
		if(isExclusiveGrabActive()) return;
		if(getGrab(button) != null) return;
		
		PointerGrab grab = new ImplicitGrab(window, surface, button);
		
		try {
			grab.init();
		} catch(GrabDroppedException e) {
			return;
		}
		
		grabs.add(grab);
	}
	
	public void startExclusive(PointerGrab grab) {
		if(isExclusiveGrabActive()) return;
		if(!grab.exclusive()) return;
		
		this.releaseAll();
		
		try {
			grab.init();
		} catch (GrabDroppedException e) {
			return;
		}
		grabs.add(grab);
	}
	
	public void moveWorld(Vec3 pos, Vec3 view, Vec3 up) {
		PointerGrab[] arr = grabs.toArray(PointerGrab[]::new);
		for(PointerGrab grab : arr) {
			try {
				grab.moveWorld(pos, view, up);
			} catch(GrabDroppedException e) {
				grabs.remove(grab);
			}
		}
	}
	
	public void hover(WLCAbstractWindow window, WLCSurface surface, double x, double y) {
		PointerGrab[] arr = grabs.toArray(PointerGrab[]::new);
		for(PointerGrab grab : arr) {
			try {
				grab.hover(window, surface, x, y);
			} catch(GrabDroppedException e) {
				grabs.remove(grab);
			}
		}
	}
	
	public void release(int button) {
		PointerGrab grab = getGrab(button);
		if(grab == null) return;
		
		try {
			grab.release();
		} catch (GrabDroppedException e) {
			// No handling necessary, grab always removed
		}
		
		grabs.remove(grab);
	}
	
	public void releaseAll() {
		for(PointerGrab grab : grabs) {
			try {
				grab.release();
			} catch (GrabDroppedException e) {
				// No handling necessary, grab always removed
			}
		}
		
		grabs.clear();
	}
	
}
