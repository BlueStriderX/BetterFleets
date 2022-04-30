package thederpgamer.betterfleets.gui.element;

import org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea;
import org.schema.schine.graphicsengine.forms.gui.newgui.settingsnew.GUICheckBoxTextPairNew;
import org.schema.schine.input.InputState;

/**
 * <Description>
 *
 * @author Garret Reichenbach
 * @version 1.0 - [04/30/2022]
 */
public class GUIHorizontalCheckBoxArea extends GUIHorizontalArea {

	private final GUICheckBoxTextPairNew checkBox;

	public GUIHorizontalCheckBoxArea(InputState state, GUICheckBoxTextPairNew checkBox) {
		super(state, HButtonType.TEXT_FIELD, 10);
		this.checkBox = checkBox;
	}

	@Override
	public void onInit() {
		super.onInit();
		GUIScrollablePanel panel = new GUIScrollablePanel(getWidth(), getHeight(), this, getState());
		panel.setScrollable(0);
		panel.setLeftRightClipOnly = true;
		panel.setContent(checkBox);
		attach(panel);
	}
}