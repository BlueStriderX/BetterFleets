package thederpgamer.betterfleets.gui.map;

import api.common.GameClient;
import api.utils.draw.ModWorldDrawer;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.input.InputState;

/**
 * Map drawer for Fleet GUI.
 *
 * @author TheDerpGamer
 * @since 06/28/2021
 */
public class FleetGUIMapDrawer extends ModWorldDrawer {

    public boolean needsUpdate;
    private SelectedFleetList selectedFleetList;

    @Override
    public void onInit() {
        (selectedFleetList = new SelectedFleetList(getState())).onInit();
    }

    @Override
    public void draw() {
        selectedFleetList.draw();
    }

    @Override
    public void update(Timer timer) {
        if(needsUpdate) {
            selectedFleetList.updateList();
            needsUpdate = false;
        }
    }

    @Override
    public void cleanUp() {
        selectedFleetList.cleanUp();
    }

    @Override
    public boolean isInvisible() {
        return false;
    }

    @Override
    public void postGameMapDraw() {

    }

    private InputState getState() {
        return GameClient.getClientState();
    }
}
