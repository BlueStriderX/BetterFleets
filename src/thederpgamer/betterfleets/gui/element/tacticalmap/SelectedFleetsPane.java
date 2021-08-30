package thederpgamer.betterfleets.gui.element.tacticalmap;

import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.GUIListElement;
import org.schema.schine.input.InputState;
import thederpgamer.betterfleets.BetterFleets;
import thederpgamer.betterfleets.gui.element.sprite.TacticalMapFleetIndicator;

import java.util.Map;

/**
 * <Description>
 *
 * @author TheDerpGamer
 * @since 08/29/2021
 */
public class SelectedFleetsPane extends GUIElementList {

    private float timer;

    public SelectedFleetsPane(InputState inputState) {
        super(inputState);
    }

    @Override
    public void onInit() {
        super.onInit();
        updateFleetList();
    }


    @Override
    public void update(Timer time) {
        super.update(time);
        timer -= time.getDelta();
        if(timer <= 0 || BetterFleets.getInstance().tacticalMapDrawer.selectedFleets.size() != size()) {
            updateFleetList();
            timer = 1000L;
        }
    }

    public void updateFleetList() {
        clear();
        for(Map.Entry<Long, TacticalMapFleetIndicator> entry : BetterFleets.getInstance().tacticalMapDrawer.drawMap.entrySet()) {
            if(BetterFleets.getInstance().tacticalMapDrawer.selectedFleets.contains(entry.getKey())) {
                FleetMemberElementList list = new FleetMemberElementList(getState(), entry.getValue().getFleet());
                list.onInit();
                GUIListElement listElement = new GUIListElement(list, getState());
                listElement.onInit();
                add(listElement);
            }
        }
    }
}
