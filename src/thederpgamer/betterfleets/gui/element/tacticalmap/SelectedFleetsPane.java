package thederpgamer.betterfleets.gui.element.tacticalmap;

import api.common.GameCommon;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.font.FontLibrary;
import org.schema.schine.graphicsengine.forms.gui.GUIColoredRectangle;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.GUIListElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.input.InputState;
import thederpgamer.betterfleets.BetterFleets;
import thederpgamer.betterfleets.gui.element.sprite.TacticalMapFleetIndicator;
import thederpgamer.betterfleets.utils.LogManager;

import javax.vecmath.Vector4f;
import java.util.Map;
import java.util.Objects;

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

    @Override
    public void draw() {
        super.draw();
        for(GUIListElement element : this) {
            FleetMemberElementList list = (FleetMemberElementList) element.getContent().getChilds().get(0);
            for(GUIListElement e : list.getList()) {
                FleetMemberListElement memberElement = (FleetMemberListElement) e;
                memberElement.updateDisplay();
                memberElement.labelOverlay.setPos(memberElement.getPos());
                memberElement.draw();
                memberElement.labelOverlay.draw();
            }
        }
    }

    public void updateFleetList() {
        clear();
        int index = 0;
        for(Map.Entry<Long, TacticalMapFleetIndicator> entry : BetterFleets.getInstance().tacticalMapDrawer.drawMap.entrySet()) {
            if(BetterFleets.getInstance().tacticalMapDrawer.selectedFleets.contains(entry.getKey())) {
                try {
                    String display = entry.getValue().getFleet().getName() + " - " + entry.getValue().getFleet().getMissionName();
                    if(entry.getValue().getFleet().getFlagShip().getFactionId() != 0) {
                        display = entry.getValue().getFleet().getName() + "[" + Objects.requireNonNull(GameCommon.getGameState()).getFactionManager().getFaction(entry.getValue().getFleet().getFlagShip().getFactionId()).getName() + "] - " + entry.getValue().getFleet().getMissionName();
                    }
                    GUITextOverlay[] buttons = createButtons(display);
                    Vector4f color = (index % 2 == 0) ? new Vector4f(0.1f, 0.3f, 0.5f, 0.65f) :  new Vector4f(0.1f, 0.4f, 0.6f, 0.65f);
                    FleetMemberElementList list = new FleetMemberElementList(getState(), entry.getValue().getFleet(), buttons[0], buttons[1]);
                    list.onInit();
                    GUIColoredRectangle entryBackground = new GUIColoredRectangle(getState(), buttons[0].getWidth() + 4.0f, buttons[0].getHeight() + 4.0f, color);
                    entryBackground.rounded = 6;
                    entryBackground.onInit();
                    entryBackground.attach(list);
                    GUIListElement listElement = new GUIListElement(entryBackground, getState());
                    listElement.onInit();
                    add(listElement);
                    entryBackground.setPos(listElement.getPos());
                    list.getPos().x += 2.0f;
                    list.getPos().y += 2.0f;;
                    index ++;
                } catch(Exception exception) {
                    LogManager.logException("Something went wrong while initializing fleet list pane", exception);
                }
            }
        }
    }

    private GUITextOverlay[] createButtons(String display) {
        GUITextOverlay[] buttons = new GUITextOverlay[2];

        buttons[0] = new GUITextOverlay(256 - 80, 30, getState());
        buttons[0].onInit();
        buttons[0].setFont(FontLibrary.FontSize.MEDIUM.getFont());
        buttons[0].setTextSimple("[+] " + display);

        buttons[1] = new GUITextOverlay(256 - 80, 30, getState());
        buttons[1].onInit();
        buttons[1].setFont(FontLibrary.FontSize.MEDIUM.getFont());
        buttons[1].setTextSimple("[-] " + display);

        return buttons;
    }
}
