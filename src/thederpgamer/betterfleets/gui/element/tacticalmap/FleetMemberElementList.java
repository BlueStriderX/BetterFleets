package thederpgamer.betterfleets.gui.element.tacticalmap;

import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.fleet.FleetMember;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIEnterableList;
import org.schema.schine.graphicsengine.forms.gui.GUIListElement;
import org.schema.schine.input.InputState;
import thederpgamer.betterfleets.utils.LogManager;

/**
 * <Description>
 *
 * @author TheDerpGamer
 * @since 08/29/2021
 */
public class FleetMemberElementList extends GUIEnterableList {

    public final Fleet fleet;
    private float timer;

    public FleetMemberElementList(InputState inputState, Fleet fleet, GUIElement collapsedButton, GUIElement backButton) {
        super(inputState, collapsedButton, backButton);
        this.fleet = fleet;
    }

    @Override
    public void onInit() {
        super.onInit();
        updateMemberList();
    }

    @Override
    public void update(Timer time) {
        super.update(time);
        timer -= time.getDelta();
        if(timer <= 0 || fleet.getMembers().size() != list.size()) {
            updateMemberList();
            timer = 1000L;
        }
    }

    @Override
    public void draw() {
        super.draw();
        if(!isCollapsed()) {
            float pos = getList().getPos().y + 2.0f;
            for(GUIListElement element : list) {
                FleetMemberListElement memberElement = (FleetMemberListElement) element;
                memberElement.setPos(getList().getPos().x, pos, getList().getPos().z);
                memberElement.draw();
                pos += memberElement.getHeight() + 2.0f;
            }
        } else {
            for(GUIListElement element : list) {
                FleetMemberListElement memberElement = (FleetMemberListElement) element;
                memberElement.cleanUp();
            }
        }
    }

    @Override
    public void callback(GUIElement element, MouseEvent event) {
        super.callback(element, event);
        if(element.equals(collapsedButton)) {
            if(isCollapsed()) switchCollapsed(true);
        } else if(element.equals(backButton)) {
            if(!isCollapsed()) switchCollapsed(true);
        }
    }

    public void updateMemberList() {
        list.clear();
        for(FleetMember member : fleet.getMembers()) {
            try {
                FleetMemberListElement element = new FleetMemberListElement(getState(), member);
                element.onInit();
                list.add(element);
            } catch(Exception exception) {
                LogManager.logException("Something went wrong while initializing fleet member list pane", exception);
            }
        }
    }
}
