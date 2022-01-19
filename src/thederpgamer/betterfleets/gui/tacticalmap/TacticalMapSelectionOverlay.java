package thederpgamer.betterfleets.gui.tacticalmap;

import org.schema.game.common.controller.SegmentController;
import org.schema.schine.graphicsengine.forms.font.FontLibrary;
import org.schema.schine.graphicsengine.forms.gui.GUIAncor;
import org.schema.schine.graphicsengine.forms.gui.GUIListElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.input.InputState;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <Description>
 *
 * @author TheDerpGamer
 * @version 1.0 - [10/26/2021]
 */
public class TacticalMapSelectionOverlay extends GUIAncor {

    private TacticalMapGUIDrawer drawer;
    private MapEntityElementList selectedEntityList;
    private ConcurrentHashMap<Integer, String> selectedEntities;

    public TacticalMapSelectionOverlay(InputState inputState, TacticalMapGUIDrawer drawer) {
        super(inputState);
        this.drawer = drawer;
    }

    @Override
    public void onInit() {
        (selectedEntityList = new MapEntityElementList(getState())).onInit();
        attach(selectedEntityList);
        selectedEntities = new ConcurrentHashMap<>();
    }

    @Override
    public void draw() {
        super.draw();
    }

    public void addEntity(SegmentController entity) {
        if(!selectedEntities.containsKey(entity.getId())) {
            GUITextOverlay entityOverlay = new GUITextOverlay(30, 20, getState());
            entityOverlay.onInit();
            entityOverlay.setFont(FontLibrary.FontSize.SMALL.getFont());
            entityOverlay.setTextSimple(entity.getRealName());
            entityOverlay.setUserPointer(entity.getId());
            GUIListElement element = new GUIListElement(entityOverlay, getState());
            element.onInit();
            selectedEntityList.add(element);
            selectedEntities.put(entity.getId(), entity.getRealName());
        }
    }

    public void removeEntity(SegmentController entity) {
        if(selectedEntities.containsKey(entity.getId())) {
            ArrayList<GUIListElement> toRemove = new ArrayList<>();
            for(GUIListElement element : selectedEntityList) {
                if(element.getContent() instanceof GUITextOverlay && element.getContent().getUserPointer().equals(entity.getId())) {
                    toRemove.add(element);
                }
            }

            for(GUIListElement element : toRemove) {
                selectedEntities.remove((Integer) element.getContent().getUserPointer());
                selectedEntityList.remove(element);
            }
        }
    }

    public void removeAll() {
        selectedEntities.clear();
        selectedEntityList.clear();
    }
}
