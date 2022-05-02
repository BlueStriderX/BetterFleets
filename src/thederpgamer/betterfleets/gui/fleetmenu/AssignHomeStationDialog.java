package thederpgamer.betterfleets.gui.fleetmenu;

import api.utils.gui.GUIInputDialog;
import api.utils.gui.GUIInputDialogPanel;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDialogWindow;
import org.schema.schine.input.InputState;
import thederpgamer.betterfleets.data.fleet.FleetDeploymentData;

/**
 * <Description>
 *
 * @author Garret Reichenbach
 * @version 1.0 - [05/01/2022]
 */
public class AssignHomeStationDialog extends GUIInputDialog {

	private final FleetDeploymentData deploymentData;

	public AssignHomeStationDialog(FleetDeploymentData deploymentData) {
		this.deploymentData = deploymentData;
	}

	@Override
	public AssignHomeStationPanel createPanel() {
		return new AssignHomeStationPanel(getState(), this, deploymentData);
	}

	@Override
	public AssignHomeStationPanel getInputPanel() {
		return (AssignHomeStationPanel) super.getInputPanel();
	}

	@Override
	public void callback(GUIElement element, MouseEvent mouseEvent) {
		if(mouseEvent.pressedLeftMouse() && element.getUserPointer() != null) {
			switch((String) element.getUserPointer()) {
				case "OK":
					if(getInputPanel().stationList.getSelectedRow() != null && getInputPanel().stationList.getSelectedRow().f != null) {
						deploymentData.setHomeStation(getInputPanel().stationList.getSelectedRow().f);
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

	public static class AssignHomeStationPanel extends GUIInputDialogPanel {

		private final FleetDeploymentData deploymentData;
		private FactionStationList stationList;

		public AssignHomeStationPanel(InputState inputState, GUICallback guiCallback, FleetDeploymentData deploymentData) {
			super(inputState, "ASSIGN_HOME_STATION", "Assign Home Station", "", 800, 700, guiCallback);
			this.deploymentData = deploymentData;
		}

		@Override
		public void onInit() {
			super.onInit();
			(stationList = new FactionStationList(getState(), getWidth(), getHeight(), ((GUIDialogWindow) background).getMainContentPane().getContent(1), deploymentData)).onInit();
			((GUIDialogWindow) background).getMainContentPane().setTextBoxHeight(1, (int) stationList.getContentHeight());
		}
	}
}
