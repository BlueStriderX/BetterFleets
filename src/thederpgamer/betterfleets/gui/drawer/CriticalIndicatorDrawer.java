package thederpgamer.betterfleets.gui.drawer;

import api.common.GameClient;
import api.utils.draw.ModWorldDrawer;
import com.bulletphysics.linearmath.Transform;
import org.schema.schine.graphicsengine.core.Timer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <Description>
 *
 * @author TheDerpGamer
 * @version 1.0 - [03/09/2022]
 */
public class CriticalIndicatorDrawer extends ModWorldDrawer {

	private final ConcurrentHashMap<CriticalIndicatorOverlay, Float> overlayMap = new ConcurrentHashMap<>();

	@Override
	public void onInit() {

	}

	@Override
	public void update(Timer timer) {
		for(Map.Entry<CriticalIndicatorOverlay, Float> entry : overlayMap.entrySet()) {
			if(entry.getValue() > 0) {
				entry.getKey().draw();
				entry.getKey().setOpacity(entry.getValue());
				entry.setValue(entry.getValue() - timer.getDelta());
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

	public void addOverlay(Transform transform, double damage) {
		CriticalIndicatorOverlay overlay = new CriticalIndicatorOverlay(GameClient.getClientState(), damage);
		overlay.onInit();
		overlay.setTransform(transform);
		overlay.setOpacity(100.0f);
		overlayMap.put(overlay, 100.0f);
	}
}
