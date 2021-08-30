package thederpgamer.betterfleets.gui.element.tacticalmap;

import org.newdawn.slick.Color;
import org.newdawn.slick.font.effects.OutlineEffect;
import org.schema.common.util.StringTools;
import org.schema.game.common.data.fleet.FleetMember;
import org.schema.schine.graphicsengine.forms.font.FontLibrary;
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

    private GUITextOverlay labelOverlay;
    private GUITextOverlay healthOverlay;

    public FleetMemberListElement(InputState inputState, FleetMember member) {
        super(inputState);
        this.member = member;
        this.content = new GUIColoredRectangle(getState(), 34.0f, 14.0f, new Vector4f());

        VERY_LOW_HEALTH = decode("0xB81111");
        LOW_HEALTH = decode("0xFFB030");
        MID_HEALTH = decode("0xFFDD30");
        HIGH_HEALTH = decode("0x32D420");
        MAX_HEALTH = decode("0x3BE392");
    }

    @Override
    public void onInit() {
        super.onInit();

        labelOverlay = new GUITextOverlay(30, 10, getState());
        labelOverlay.onInit();
        labelOverlay.setFont(FontLibrary.FontSize.SMALL.getFont());
        labelOverlay.setLimitTextDraw(100);
        content.attach(labelOverlay);

        healthOverlay = new GUITextOverlay(30, 10, getState());
        healthOverlay.onInit();
        healthOverlay.setFont(FontLibrary.FontSize.MEDIUM.getFont());
        content.attach(healthOverlay);

        ((GUIColoredRectangle) content).rounded = 3;
        updateDisplay();
    }

    public void updateDisplay() {
        if(!member.isLoaded()) {
            if(lastKnownHealth < 0.0f) ((GUIColoredRectangle) content).setColor(new Vector4f(0.5f, 0.5f, 0.5f, 0.5f));
            else ((GUIColoredRectangle) content).setColor(getHealthColor(lastKnownHealth));
        } else {
            lastKnownHealth = member.getShipPercent();
            healthOverlay.setColor(getHealthColor(lastKnownHealth));
            ((GUIColoredRectangle) content).setColor(getHealthColor(lastKnownHealth));
        }
        healthOverlay.setTextSimple(StringTools.formatPointZero(lastKnownHealth * 100) + "%");
        labelOverlay.setTextSimple(member.getName());
        labelOverlay.getPos().x = (content.getPos().x * 5) + 5.0f;
        healthOverlay.getPos().x = 500.0f - healthOverlay.getWidth() - 5.0f;
        ((GUIColoredRectangle) content).setHeight(labelOverlay.getHeight() + 10.0f);
        content.setInside(true);
        labelOverlay.setInside(true);
        healthOverlay.setInside(true);

        healthOverlay.setFont(FontLibrary.FontSize.MEDIUM.getFont());
        healthOverlay.getFont().getEffects().add(new OutlineEffect(2, java.awt.Color.decode(getFontOutlineColor(lastKnownHealth))));
    }

    public static Vector4f getHealthColor(float lastKnownHealth) {
        Vector4f healthColor = new Vector4f();
        if(lastKnownHealth >= 0.95f) healthColor.set(MAX_HEALTH);
        else if(lastKnownHealth >= 0.75f) healthColor.set(HIGH_HEALTH);
        else if(lastKnownHealth >= 0.5f) healthColor.set(MID_HEALTH);
        else if(lastKnownHealth >= 0.3f) healthColor.set(LOW_HEALTH);
        else if(lastKnownHealth >= 0.15f) healthColor.set(VERY_LOW_HEALTH);
        return healthColor;
    }

    public static String getFontOutlineColor(float lastKnownHealth) {
        if(lastKnownHealth >= 0.95f) return "0x3BE392";
        else if(lastKnownHealth >= 0.75f) return"0x32D40";
        else if(lastKnownHealth >= 0.5f)  return "0xFFDD30";
        else if(lastKnownHealth >= 0.3f) return "0xFFB030";
        else if(lastKnownHealth >= 0.15f) return "0xB81111";
        else return "0xFFFFFF";
    }

    public static Vector4f decode(String code) {
        Color color;
        if(code.startsWith("0x")) color = Color.decode(code);
        else color = Color.decode("0x" + code);
        return new Vector4f(color.r, color.b, color.g, 1.0f);
    }
}
