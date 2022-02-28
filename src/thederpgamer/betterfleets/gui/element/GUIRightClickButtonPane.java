package thederpgamer.betterfleets.gui.element;

import org.lwjgl.input.Mouse;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalButtonTablePane;
import org.schema.schine.graphicsengine.util.WorldToScreenConverter;
import org.schema.schine.input.InputState;
import thederpgamer.betterfleets.gui.element.sprite.TacticalMapEntityIndicator;

import javax.annotation.Nullable;
import javax.vecmath.Vector3f;

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

    public void moveToMouse(@Nullable TacticalMapEntityIndicator indicator) {
        if(active) cleanUp();
        try {
            if(indicator == null)  setPos(Mouse.getX() - 250, Mouse.getY() * -1, 0.0f);
            else {
                WorldToScreenConverter converter = new WorldToScreenConverter();
                Vector3f middle = converter.getMiddleOfScreen(new Vector3f());
                setPos(middle.x - (getWidth() / 2), middle.y + 50, 0.0f);
            }
            active = true;
        } catch(Exception ignored) { }
    }

    public void clearButtons() {
        super.onInit();
    }
}
