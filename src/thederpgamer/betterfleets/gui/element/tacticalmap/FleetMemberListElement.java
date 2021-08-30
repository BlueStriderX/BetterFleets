package thederpgamer.betterfleets.gui.element.tacticalmap;

import org.newdawn.slick.Color;
import org.schema.common.util.StringTools;
import org.schema.game.common.data.fleet.FleetMember;
import org.schema.schine.graphicsengine.forms.gui.GUIColoredRectangle;
import org.schema.schine.graphicsengine.forms.gui.GUIListElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.input.InputState;

import javax.vecmath.Vector4f;

/**
 * <Description>
 *
 * @author TheDerpGamer
 * @since 08/29/2021
 */
public class FleetMemberListElement extends GUIListElement {

    public static Vector4f VERY_LOW_HEALTH;
    public static Vector4f LOW_HEALTH;
    public static Vector4f MID_HEALTH;
    public static Vector4f HIGH_HEALTH;
    public static Vector4f MAX_HEALTH;

    public final FleetMember member;
    private float lastKnownHealth = -1.0f;

    private GUIColoredRectangle background;
    private GUITextOverlay labelOverlay;

    public FleetMemberListElement(InputState inputState, FleetMember member) {
        super(inputState);
        this.member = member;

        VERY_LOW_HEALTH = decode("0xB81111");
        LOW_HEALTH = decode("0xFFB030");
        MID_HEALTH = decode("0xFFDD30");
        HIGH_HEALTH = decode("0x32D420");
        MAX_HEALTH = decode("0x1BE39A");
    }

    @Override
    public void onInit() {
        super.onInit();
        background = new GUIColoredRectangle(getState(), 34.0f, 14.0f, new Vector4f());
        background.onInit();
        attach(background);
        labelOverlay = new GUITextOverlay(30, 10, getState());
        labelOverlay.onInit();
        background.attach(labelOverlay);
        labelOverlay.getPos().x -= 2.0f;
        labelOverlay.getPos().y -= 2.0f;
        updateDisplay();
    }

    public void updateDisplay() {
        if(!member.isLoaded()) {
            if(lastKnownHealth < 0.0f) {
                background.setColor(new Vector4f(0.5f, 0.5f, 0.5f, 0.5f));
                labelOverlay.setColor(new Vector4f(0.85f, 0.85f, 0.85f, 1.0f));
                labelOverlay.setTextSimple(member.getName() + " - ???HP");
            } else {
                background.setColor(getHealthColor(lastKnownHealth));
                labelOverlay.setColor(getHealthColor(lastKnownHealth));
                labelOverlay.setTextSimple(member.getName() + " - " + StringTools.formatPointZero(lastKnownHealth * 100) + "HP");
            }
        } else {
            lastKnownHealth = member.getShipPercent();
            background.setColor(getHealthColor(lastKnownHealth));
            labelOverlay.setColor(getHealthColor(lastKnownHealth));
            labelOverlay.setTextSimple(member.getName() + " - " + StringTools.formatPointZero(lastKnownHealth * 100) + "HP");
        }
    }

    public static Vector4f getHealthColor(float lastKnownHealth) {
        Vector4f healthColor = new Vector4f();
        if(lastKnownHealth <= 0.15f) healthColor.set(VERY_LOW_HEALTH);
        else if(lastKnownHealth <= 0.3f) healthColor.set(LOW_HEALTH);
        else if(lastKnownHealth <= 0.5f) healthColor.set(MID_HEALTH);
        else if(lastKnownHealth <= 0.75f) healthColor.set(HIGH_HEALTH);
        else if(lastKnownHealth <= 0.95f) healthColor.set(MAX_HEALTH);
        return healthColor;
    }

    public static Vector4f decode(String code) {
        Color color;
        if(code.startsWith("0x")) color = Color.decode(code);
        else color = Color.decode("0x" + code);
        return new Vector4f(color.r, color.b, color.g, 1.0f);
    }
}
