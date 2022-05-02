package thederpgamer.betterfleets.gui.fleetmenu;

import api.utils.gui.GUIInputDialog;
import api.utils.gui.GUIInputDialogPanel;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationCallback;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDialogWindow;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalButtonTablePane;
import org.schema.schine.input.InputState;
import thederpgamer.betterfleets.data.fleet.FleetDeploymentData;
import thederpgamer.betterfleets.manager.FleetDeploymentManager;

/**
 * <Description>
 *
 * @author Garret Reichenbach
 * @version 1.0 - [05/01/2022]
 */
public class EditFleetDialog extends GUIInputDialog {

	public static AssignHomeStationDialog homeStationDialog;
	private final DeploymentsScrollableList deploymentsList;
	private final FleetDeploymentData deploymentData;

	public EditFleetDialog(DeploymentsScrollableList deploymentsList, FleetDeploymentData deploymentData) {
		this.deploymentsList = deploymentsList;
		this.deploymentData = deploymentData;
	}

	@Override
	public EditFleetPanel createPanel() {
		return new EditFleetPanel(getState(), this, deploymentData);
	}

	@Override
	public EditFleetPanel getInputPanel() {
		return (EditFleetPanel) super.getInputPanel();
	}

	@Override
	public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
		if(mouseEvent.pressedLeftMouse() && guiElement.getUserPointer() != null) {
			switch((String) guiElement.getUserPointer()) {
				case "OK":
					FleetDeploymentManager.updateToServer(deploymentData);
					deploymentsList.flagDirty();
					deactivate();
					break;
				case "X":
				case "CANCEL":
					deactivate();
					break;
			}
		}
	}

	public static class EditFleetPanel extends GUIInputDialogPanel {

		public EditFleetScrollableList fleetList;
		private final FleetDeploymentData deploymentData;

		public EditFleetPanel(InputState inputState, GUICallback guiCallback, FleetDeploymentData deploymentData) {
			super(inputState, "ASSIGN_FLEET", "Assign Fleet", "", 800, 700, guiCallback);
			this.deploymentData = deploymentData;
		}

		@Override
		public void onInit() {
			super.onInit();
			GUIHorizontalButtonTablePane buttonPane = new GUIHorizontalButtonTablePane(getState(), 1, 1, ((GUIDialogWindow) background).getMainContentPane().getContent(0));
			buttonPane.onInit();
			buttonPane.addButton(0, 0, "SET HOME STATION", GUIHorizontalArea.HButtonColor.BLUE, new GUICallback() {
				@Override
				public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
					if(mouseEvent.pressedLeftMouse() && fleetList.getSelectedRow() != null && fleetList.getSelectedRow().f != null) {
						(homeStationDialog = new AssignHomeStationDialog(deploymentData)).activate();
					}
				}

				@Override
				public boolean isOccluded() {
					return homeStationDialog != null && homeStationDialog.isActive();
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
			((GUIDialogWindow) background).getMainContentPane().getContent(0).attach(buttonPane);
			((GUIDialogWindow) background).getMainContentPane().setTextBoxHeight(0, (int) buttonPane.getHeight());

			((GUIDialogWindow) background).getMainContentPane().addNewTextBox(30);
			(fleetList = new EditFleetScrollableList(getState(), getWidth(), getHeight(), ((GUIDialogWindow) background).getMainContentPane().getContent(1), deploymentData)).onInit();
			((GUIDialogWindow) background).getMainContentPane().setTextBoxHeight(1, (int) fleetList.getContentHeight());
		}
	}
}
