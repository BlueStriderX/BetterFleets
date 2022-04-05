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

	private final Sprite sprite;
	private GUITextOverlay textOverlay;
	private final double damage;
	private float opacity;

	public CriticalIndicatorOverlay(InputState inputState, double damage) {
		super(inputState);
		this.sprite = ResourceManager.getSprite("critical-overlay-sprite");
		this.damage = damage;
		this.opacity = 1.0f;
	}

	@Override
	public void onInit() {
		super.onInit();
		attach(sprite);
		(textOverlay = new GUITextOverlay(32, 32, FontLibrary.FontSize.BIG, getState())).onInit();
		textOverlay.setTextSimple((int) damage);
		attach(textOverlay);
	}

	@Override
	public void draw() {
		//getTransform().basis.set(Controller.getCamera().lookAt(false).basis);
		//getTransform().basis.invert();
		super.draw();
		//textOverlay.draw();
		//sprite.draw();
	}

	public double getDamage() {
		return damage;
	}

	public float getOpacity() {
		return opacity;
	}

	public void setOpacity(float opacity) {
		this.opacity = opacity;
		sprite.getTint().w = opacity;
		textOverlay.getColor().a = opacity;
	}
}
