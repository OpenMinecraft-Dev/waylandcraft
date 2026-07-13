package dev.evvie.waylandcraft.grabs;

import dev.evvie.waylandcraft.bridge.WLCAbstractWindow.SurfaceGeometry;
import dev.evvie.waylandcraft.bridge.WLCToplevel;
import dev.evvie.waylandcraft.displays.WindowDisplay;
import dev.evvie.waylandcraft.displays.WindowDisplay.DisplayHitResult;
import dev.evvie.waylandcraft.grabs.PointerGrabMap.ImplicitGrab;
import net.minecraft.world.phys.Vec3;

public class ResizeGrab extends PointerGrab {
	
	private final WindowDisplay window;
	private final WLCToplevel toplevel;
	private final Vec3 initialDisplayPos;
	
	private final Vec3 initialSurfaceLocal;
	
	private final ResizeEdges edges;
	private final SurfaceGeometry initialGeometry;
	
	private final int width;
	private final int height;
	
	public ResizeGrab(ImplicitGrab implicit, int edges) {
		super(implicit.button());
		this.window = implicit.window();
		this.toplevel = (WLCToplevel) window.window;
		this.initialSurfaceLocal = implicit.startSurfaceLocal();
		this.edges = ResizeEdges.forNumber(edges);
		this.initialGeometry = window.window.geometry;
		this.width = initialGeometry.width();
		this.height = initialGeometry.height();
		this.initialDisplayPos = window.origin();
	}
	
	@Override
	public void init() throws GrabDroppedException {
	}
	
	@Override
	public void release(boolean force) throws GrabDroppedException {
	}
	
	@Override
	public void moveWorld(Vec3 pos, Vec3 view, Vec3 up, float yRot, float xRot) throws GrabDroppedException {
		if(!window.isValid()) this.drop();
		
		DisplayHitResult hitResult = window.intersect(pos, view);
		if(hitResult == null) return;
		
		int horizontal = edges.horizontalMult();
		int vertical = edges.verticalMult();
		
		Vec3 surfLocalInitial = window.worldToLocal(initialDisplayPos);
		Vec3 diff = hitResult.surfaceLocalOrigin().subtract(surfLocalInitial).subtract(initialSurfaceLocal);
		int dx = (int) diff.x * horizontal;
		int dy = (int) diff.y * vertical;
		
		int nwidth = initialGeometry.width() + dx;
		int nheight = initialGeometry.height() + dy;
		
		if(nwidth == width && nheight == height) return;
		if(nwidth < 1 || nheight < 1 || nwidth > 10000 || nheight > 10000) return;
		
		window.moveOrigin(initialDisplayPos);
		if(horizontal < 0) {
			window.pivot = window.pivot.add(window.localX().scale(-dx));
		}
		if(vertical < 0) {
			window.pivot = window.pivot.add(window.localY().scale(-dy));
		}
		
		wlc.bridge.resizeToplevelInteractive(toplevel, nwidth, nheight);
	}
	
	private enum ResizeEdges {
		
		NONE, TOP, BOTTOM, LEFT, TOP_LEFT, BOTTOM_LEFT, RIGHT, TOP_RIGHT, BOTTOM_RIGHT;
		
		/* I know these are supposed to be bitmasks. Too bad! */
		
		public static ResizeEdges forNumber(int num) {
            return switch (num) {
                case 1 -> TOP;
                case 2 -> BOTTOM;
                case 4 -> LEFT;
                case 5 -> TOP_LEFT;
                case 6 -> BOTTOM_LEFT;
                case 8 -> RIGHT;
                case 9 -> TOP_RIGHT;
                case 10 -> BOTTOM_RIGHT;
                default -> NONE;
            };
		}
		
		public int horizontalMult() {
            return switch (this) {
                case RIGHT -> 1;
                case TOP_RIGHT -> 1;
                case BOTTOM_RIGHT -> 1;
                case LEFT -> -1;
                case TOP_LEFT -> -1;
                case BOTTOM_LEFT -> -1;
                default -> 0;
            };
		}
		
		public int verticalMult() {
            return switch (this) {
                case BOTTOM -> 1;
                case BOTTOM_RIGHT -> 1;
                case BOTTOM_LEFT -> 1;
                case TOP -> -1;
                case TOP_RIGHT -> -1;
                case TOP_LEFT -> -1;
                default -> 0;
            };
		}
		
	}
	
}
