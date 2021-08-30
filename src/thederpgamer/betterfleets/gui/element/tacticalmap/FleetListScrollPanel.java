package thederpgamer.betterfleets.gui.element.tacticalmap;

import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIListElement;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel;
import org.schema.schine.input.InputState;

/**
 * <Description>
 *
 * @author TheDerpGamer
 * @since 08/30/2021
 */
public class FleetListScrollPanel extends GUIScrollablePanel {

    public FleetMemberElementList list;

    public FleetListScrollPanel(GUIElement guiElement, FleetMemberElementList list, InputState inputState) {
        super(list.getWidth(), list.getHeight(), guiElement, inputState);
        this.list = list;
    }

    @Override
    public void draw() {
        if(!list.isCollapsed()) {
            super.draw();
            for(GUIListElement element : list.getList()) element.draw();
        } else {
            for(GUIListElement element : list.getList()) element.cleanUp();
        }
    }

    @Override
    public void checkMouseInside() {
        if(!list.isCollapsed()) super.checkMouseInside();
    }
}
