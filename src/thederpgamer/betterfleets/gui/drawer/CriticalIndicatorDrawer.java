package thederpgamer.betterfleets.gui.drawer;

import api.common.GameClient;
import api.utils.draw.ModWorldDrawer;
import api.utils.textures.StarLoaderTexture;
import com.bulletphysics.linearmath.Transform;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;

import javax.vecmath.Vector3f;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <Description>
 *
 * @author TheDerpGamer
 * @version 1.0 - [03/09/2022]
 */
public class CriticalIndicatorDrawer extends ModWorldDrawer {

	private final ConcurrentHashMap<Integer, CriticalIndicatorOverlay> overlayMap = new ConcurrentHashMap<>();

	@Override
	public void onInit() {

	}

	@Override
	public void update(Timer timer) {
		for(Map.Entry<Integer, CriticalIndicatorOverlay> entry : overlayMap.entrySet()) {
			if(entry.getValue().getOpacity() > 0) {
				entry.getValue().draw();
				entry.getValue().setOpacity(entry.getValue().getOpacity() - 0.01f);
				Vector3f up = GlUtil.getUpVector(new Vector3f(), entry.getValue().getTransform());
				entry.getValue().getTransform().origin.add(up);
			} else overlayMap.remove(entry.getKey());
		}
	}

	@Override
	public void cleanUp() {

	}

	@Override
	public boolean isInvisible() {
		return false;
	}

	public void addOverlay(final Transform transform, final double damage, final int id) {
		StarLoaderTexture.runOnGraphicsThread(new Runnable() {
			@Override
			public void run() {
				if(!overlayMap.containsKey(id)) {
					CriticalIndicatorOverlay overlay = new CriticalIndicatorOverlay(GameClient.getClientState(), damage);
					overlay.onInit();
					overlay.setTransform(transform);
					overlay.setOpacity(1.0f);
					overlayMap.put(id, overlay);
				}
			}
		});
	}
}
