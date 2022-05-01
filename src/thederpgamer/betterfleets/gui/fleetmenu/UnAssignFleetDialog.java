package thederpgamer.betterfleets.gui.fleetmenu;

import api.utils.gui.GUIInputDialog;
import api.utils.gui.GUIInputDialogPanel;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.input.InputState;
import thederpgamer.betterfleets.data.fleet.FleetDeploymentData;
import thederpgamer.betterfleets.manager.FleetDeploymentManager;

/**
 * <Description>
 *
 * @author Garret Reichenbach
 * @version 1.0 - [05/01/2022]
 */
public class UnAssignFleetDialog extends GUIInputDialog {

	private final DeploymentsScrollableList deploymentsList;
	private final FleetDeploymentData deploymentData;

	public UnAssignFleetDialog(DeploymentsScrollableList deploymentsList, FleetDeploymentData deploymentData) {
		this.deploymentsList = deploymentsList;
		this.deploymentData = deploymentData;
	}

	@Override
	public AssignFleetPanel createPanel() {
		return new AssignFleetPanel(getState(), this, deploymentData);
	}

	@Override
	public AssignFleetPanel getInputPanel() {
		return (AssignFleetPanel) super.getInputPanel();
	}

	@Override
	public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
		if(mouseEvent.pressedLeftMouse() && guiElement.getUserPointer() != null) {
			switch((String) guiElement.getUserPointer()) {
				case "OK":
					if(getInputPanel().fleetList.getSelectedRow() != null && getInputPanel().fleetList.getSelectedRow().f != null) {
						FleetDeploymentManager.unAssignFleet(deploymentData, getInputPanel().fleetList.getSelectedRow().f);
						deploymentsList.flagDirty();
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

	public static class AssignFleetPanel extends GUIInputDialogPanel {

		public UnAssignFleetScrollableList fleetList;
		private final FleetDeploymentData deploymentData;

		public AssignFleetPanel(InputState inputState, GUICallback guiCallback, FleetDeploymentData deploymentData) {
			super(inputState, "ASSIGN_FLEET", "Assign Fleet", "", 400, 300, guiCallback);
			this.deploymentData = deploymentData;
		}

		@Override
		public void onInit() {
			super.onInit();
			(fleetList = new UnAssignFleetScrollableList(getState(), getWidth(), getHeight(), getContent(), deploymentData)).onInit();
			getContent().attach(fleetList);
		}
	}
}
