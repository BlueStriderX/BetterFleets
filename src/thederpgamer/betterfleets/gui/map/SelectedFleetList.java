package thederpgamer.betterfleets.gui.map;

import org.schema.game.common.data.fleet.Fleet;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.input.InputState;
import thederpgamer.betterfleets.manager.FleetGUIManager;

/**
 * Selected fleet list GUI element.
 *
 * @author TheDerpGamer
 * @since 06/14/2021
 */
public class SelectedFleetList extends GUIAncor {

    public GUIElementList elementList;

    public SelectedFleetList(InputState inputState) {
        super(inputState, 24, 24);
        elementList = new GUIElementList(inputState);
    }

    @Override
    public void onInit() {
        super.onInit();
        elementList.onInit();
        updateList();
        attach(elementList);
    }

    public void updateList() {
        elementList.clear();
        if(FleetGUIManager.selectedFleets.isEmpty()) cleanUp();
        else {
            for(Fleet selectedFleet : FleetGUIManager.selectedFleets) {
                GUITextOverlay selectedFleetOverlay = new GUITextOverlay(30, 10, getState());
                selectedFleetOverlay.onInit();
                selectedFleetOverlay.setTextSimple(selectedFleet.getName().trim());
                elementList.add(new GUIListElement(selectedFleetOverlay, getState()));
            }
        }
    }
}