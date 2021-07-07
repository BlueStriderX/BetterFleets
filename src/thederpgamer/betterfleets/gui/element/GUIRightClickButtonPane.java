package thederpgamer.betterfleets.gui.element;

import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalButtonTablePane;
import org.schema.schine.input.InputState;
import org.schema.schine.input.Mouse;

/**
 * ButtonTablePane that appears next to the mouse on right click.
 *
 * @author TheDerpGamer
 * @since 06/28/2021
 */
public class GUIRightClickButtonPane extends GUIHorizontalButtonTablePane {

    public boolean active = false;

    public GUIRightClickButtonPane(InputState inputState, int columns, int rows, GUIElement parent) {
        super(inputState, columns, rows, parent);
    }

    @Override
    public void draw() {
        if(active) super.draw();
        else cleanUp();
    }

    @Override
    public void cleanUp() {
        super.cleanUp();
        active = false;
    }

    public void moveToMouse() {
        try {
            setPos(Mouse.getX() - 250, Mouse.getY() * -1, 0.0f);
            active = true;
        } catch(Exception ignored) {
            cleanUp();
        }
    }
}
