package thederpgamer.betterfleets.gui.drawer;

import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.graphicsengine.forms.font.FontLibrary;
import org.schema.schine.graphicsengine.forms.gui.GUIAncor;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.input.InputState;
import thederpgamer.betterfleets.manager.ResourceManager;

/**
 * <Description>
 *
 * @author TheDerpGamer
 * @version 1.0 - [03/09/2022]
 */
public class CriticalIndicatorOverlay extends GUIAncor {

	private GUITextOverlay textOverlay;
	private Sprite spriteOverlay;
	private final double damage;

	public CriticalIndicatorOverlay(InputState inputState, double damage) {
		super(inputState);
		this.damage = damage;
	}

	@Override
	public void onInit() {
		super.onInit();
		(spriteOverlay = ResourceManager.getSprite("critical-overlay-sprite")).onInit();
		attach(spriteOverlay);
		(textOverlay = new GUITextOverlay(32, 32, FontLibrary.FontSize.BIG, getState())).onInit();
		attach(textOverlay);
	}

	public double getDamage() {
		return damage;
	}

	public void setOpacity(float opacity) {
		opacity /= 100;
		spriteOverlay.getTint().w = opacity;
		textOverlay.getColor().a = opacity;
	}
}
