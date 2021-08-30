package thederpgamer.betterfleets.gui.element.tacticalmap;

import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.fleet.FleetMember;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.GUIEnterableList;
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
    private boolean firstDraw = true;

    public FleetMemberElementList(InputState inputState, Fleet fleet) {
        super(inputState);
        this.fleet = fleet;
    }

    @Override
    public void onInit() {
        super.onInit();
        updateMemberList();
    }

    @Override
    public void draw() {
        super.draw();
        firstDraw = false;
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

    public void updateMemberList() {
        list.clear();
        for(FleetMember member : fleet.getMembers()) {
            try {
                FleetMemberListElement element = new FleetMemberListElement(getState(), member);
                element.onInit();
                if(firstDraw) list.addWithoutUpdate(element);
                else list.add(element);
            } catch(Exception exception) {
                LogManager.logException("Something went wrong while initializing fleet list pane", exception);
            }
        }
    }
}
