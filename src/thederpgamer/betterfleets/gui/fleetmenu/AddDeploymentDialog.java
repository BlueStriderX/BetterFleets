package thederpgamer.betterfleets.gui.fleetmenu;

import api.utils.gui.GUIInputDialog;
import api.utils.gui.GUIInputDialogPanel;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.input.InputState;

/**
 * <Description>
 *
 * @author Garret Reichenbach
 * @version 1.0 - [04/30/2022]
 */
public class AddDeploymentDialog extends GUIInputDialog {

	public AddDeploymentDialog() {

	}

	@Override
	public AddDeploymentPanel createPanel() {
		return new AddDeploymentPanel(getState(), this);
	}

	@Override
	public void callback(GUIElement element, MouseEvent mouseEvent) {

	}

	public static class AddDeploymentPanel extends GUIInputDialogPanel {

		public AddDeploymentPanel(InputState state, GUICallback callback) {
			super(state, "ADD_NEW_DEPLOYMENT", "Add new deployment", "", 400, 300, callback);
		}
	}
}
