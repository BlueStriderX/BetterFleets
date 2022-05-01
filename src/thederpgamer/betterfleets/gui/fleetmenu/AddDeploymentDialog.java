package thederpgamer.betterfleets.gui.fleetmenu;

import api.utils.gui.GUIInputDialog;
import api.utils.gui.GUIInputDialogPanel;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationCallback;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITilePane;
import org.schema.schine.input.InputState;
import thederpgamer.betterfleets.data.fleet.FleetDeploymentData;

/**
 * <Description>
 *
 * @author Garret Reichenbach
 * @version 1.0 - [04/30/2022]
 */
public class AddDeploymentDialog extends GUIInputDialog {

	@Override
	public AddDeploymentPanel createPanel() {
		return new AddDeploymentPanel(getState(), this);
	}

	@Override
	public AddDeploymentPanel getInputPanel() {
		return (AddDeploymentPanel) super.getInputPanel();
	}

	@Override
	public void callback(GUIElement element, MouseEvent mouseEvent) {
		if(mouseEvent.pressedLeftMouse() && element.getUserPointer() != null) {
			switch((String) element.getUserPointer()) {
				case "OK":
					if(getInputPanel().deploymentType != null) {
						createNewDeployment(getInputPanel().deploymentType);
						deactivate();
					}
					break;
				case "X":
				case "CANCEL":
					deactivate();
					break;
			}
		}
	}

	private void createNewDeployment(FleetDeploymentData.FleetDeploymentType deploymentType) {

	}

	public static class AddDeploymentPanel extends GUIInputDialogPanel {

		private GUIScrollablePanel scrollPane;
		private GUITilePane<FleetDeploymentData.FleetDeploymentType> tilePane;
		public FleetDeploymentData.FleetDeploymentType deploymentType;

		public AddDeploymentPanel(InputState state, GUICallback callback) {
			super(state, "ADD_NEW_DEPLOYMENT", "Add new deployment", "", 400, 300, callback);
		}

		@Override
		public void onInit() {
			super.onInit();
			(scrollPane = new GUIScrollablePanel(getWidth(), getHeight(), getContent(), getState())).onInit();
			(tilePane = new GUITilePane<>(getState(), scrollPane.getContent(), 100, 150)).onInit();
			for(final FleetDeploymentData.FleetDeploymentType type : FleetDeploymentData.FleetDeploymentType.values()) {
				tilePane.addButtonTile(type.display, type.description, GUIHorizontalArea.HButtonColor.BLUE, new GUICallback() {
					@Override
					public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
						if(mouseEvent.pressedLeftMouse()) deploymentType = type;
					}

					@Override
					public boolean isOccluded() {
						return false;
					}
				}, new GUIActivationCallback() {
					@Override
					public boolean isVisible(InputState inputState) {
						return true;
					}

					@Override
					public boolean isActive(InputState inputState) {
						return true;
					}
				});
			}
			getContent().attach(scrollPane);
		}
	}
}
