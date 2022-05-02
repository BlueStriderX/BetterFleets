package thederpgamer.betterfleets.gui.fleetmenu;

import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.*;
import org.schema.schine.input.InputState;
import thederpgamer.betterfleets.data.fleet.FleetDeploymentData;
import thederpgamer.betterfleets.manager.FleetDeploymentManager;

import java.util.Collection;
import java.util.Comparator;
import java.util.Set;

/**
 * <Description>
 *
 * @author Garret Reichenbach
 * @version 1.0 - [04/30/2022]
 */
public class DeploymentsScrollableList extends ScrollableTableList<FleetDeploymentData> {

	private final GUIElement anchor;
	public AddDeploymentDialog input;

	public DeploymentsScrollableList(InputState state, float width, float height, GUIElement anchor) {
		super(state, width, height, anchor);
		this.anchor = anchor;
		this.anchor.attach(this);
	}

	private GUIHorizontalButtonTablePane redrawButtonPane(final FleetDeploymentData deploymentData, GUIAncor anchor) {
		GUIHorizontalButtonTablePane buttonPane = new GUIHorizontalButtonTablePane(getState(), 2, 1, anchor);
		buttonPane.onInit();
		buttonPane.addButton(0, 0, "ASSIGN FLEET", GUIHorizontalArea.HButtonColor.BLUE, new GUICallback() {
			@Override
			public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
				if(mouseEvent.pressedLeftMouse() && getSelectedRow() != null && getSelectedRow().f != null) {
					(new AssignFleetDialog(DeploymentsScrollableList.this, getSelectedRow().f)).activate();
				}
			}

			@Override
			public boolean isOccluded() {
				return EditFleetDialog.homeStationDialog != null && EditFleetDialog.homeStationDialog.isActive();
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
		buttonPane.addButton(1, 0, "EDIT FLEET", GUIHorizontalArea.HButtonColor.ORANGE, new GUICallback() {
			@Override
			public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
				if(mouseEvent.pressedLeftMouse() && getSelectedRow() != null && getSelectedRow().f != null) {
					if(mouseEvent.pressedLeftMouse() && getSelectedRow() != null && getSelectedRow().f != null) {
						(new EditFleetDialog(DeploymentsScrollableList.this, getSelectedRow().f)).activate();
					}
				}
			}

			@Override
			public boolean isOccluded() {
				return EditFleetDialog.homeStationDialog != null && EditFleetDialog.homeStationDialog.isActive();
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
		return buttonPane;
	}

	@Override
	protected Collection<FleetDeploymentData> getElementList() {
		return FleetDeploymentManager.getFleetDeployments();
	}

	@Override
	public void initColumns() {
		addColumn("Status", 8.5f, new Comparator<FleetDeploymentData>() {
			@Override
			public int compare(FleetDeploymentData o1, FleetDeploymentData o2) {
				return o1.getStatus().toString().compareTo(o2.getStatus().toString());
			}
		});

		addColumn("Mission", 15.0f, new Comparator<FleetDeploymentData>() {
			@Override
			public int compare(FleetDeploymentData o1, FleetDeploymentData o2) {
				return o1.getDeploymentType().compareTo(o2.getDeploymentType());
			}
		});

		addDropdownFilter(new GUIListFilterDropdown<FleetDeploymentData, FleetDeploymentData.FleetDeploymentStatus>(FleetDeploymentData.FleetDeploymentStatus.values()) {
			@Override
			public boolean isOk(FleetDeploymentData.FleetDeploymentStatus statusType, FleetDeploymentData deploymentData) {
				return deploymentData.getStatus().equals(statusType);
			}
		}, new CreateGUIElementInterface<FleetDeploymentData.FleetDeploymentStatus>() {
			@Override
			public GUIElement create(FleetDeploymentData.FleetDeploymentStatus statusType) {
				GUIAncor anchor = new GUIAncor(getState(), 10.0F, 24.0F);
				GUITextOverlayTableDropDown dropDown;
				(dropDown = new GUITextOverlayTableDropDown(10, 10, getState())).setTextSimple(statusType.name());
				dropDown.setPos(4.0F, 4.0F, 0.0F);
				anchor.setUserPointer(statusType.name());
				anchor.attach(dropDown);
				return anchor;
			}

			@Override
			public GUIElement createNeutral() {
				GUIAncor anchor = new GUIAncor(getState(), 10.0F, 24.0F);
				GUITextOverlayTableDropDown dropDown;
				(dropDown = new GUITextOverlayTableDropDown(10, 10, getState())).setTextSimple("ANY");
				dropDown.setPos(4.0F, 4.0F, 0.0F);
				anchor.setUserPointer("ANY");
				anchor.attach(dropDown);
				return anchor;
			}
		}, ControllerElement.FilterRowStyle.LEFT);

		addDropdownFilter(new GUIListFilterDropdown<FleetDeploymentData, FleetDeploymentData.FleetDeploymentType>(FleetDeploymentData.FleetDeploymentType.values()) {
			@Override
			public boolean isOk(FleetDeploymentData.FleetDeploymentType deploymentType, FleetDeploymentData deploymentData) {
				return deploymentData.getDeploymentType().equals(deploymentType);
			}
		}, new CreateGUIElementInterface<FleetDeploymentData.FleetDeploymentType>() {
			@Override
			public GUIElement create(FleetDeploymentData.FleetDeploymentType deploymentType) {
				GUIAncor anchor = new GUIAncor(getState(), 10.0F, 24.0F);
				GUITextOverlayTableDropDown dropDown;
				(dropDown = new GUITextOverlayTableDropDown(10, 10, getState())).setTextSimple(deploymentType.name());
				dropDown.setPos(4.0F, 4.0F, 0.0F);
				anchor.setUserPointer(deploymentType.name());
				anchor.attach(dropDown);
				return anchor;
			}

			@Override
			public GUIElement createNeutral() {
				GUIAncor anchor = new GUIAncor(getState(), 10.0F, 24.0F);
				GUITextOverlayTableDropDown dropDown;
				(dropDown = new GUITextOverlayTableDropDown(10, 10, getState())).setTextSimple("ALL");
				dropDown.setPos(4.0F, 4.0F, 0.0F);
				anchor.setUserPointer("ALL");
				anchor.attach(dropDown);
				return anchor;
			}
		}, ControllerElement.FilterRowStyle.RIGHT);
	}

	@Override
	public void updateListEntries(GUIElementList guiElementList, Set<FleetDeploymentData> set) {
		guiElementList.deleteObservers();
		guiElementList.addObserver(this);
		for(FleetDeploymentData deploymentData : set) {
			GUITextOverlayTable statusTextElement;
			(statusTextElement = new GUITextOverlayTable(10, 10, this.getState())).setTextSimple(deploymentData.getStatus().name());
			GUIClippedRow statusRowElement;
			(statusRowElement = new GUIClippedRow(this.getState())).attach(statusTextElement);

			GUITextOverlayTable missionTextElement;
			(missionTextElement = new GUITextOverlayTable(10, 10, this.getState())).setTextSimple(deploymentData.getDeploymentType().name());
			GUIClippedRow missionRowElement;
			(missionRowElement = new GUIClippedRow(this.getState())).attach(missionTextElement);

			DeploymentsScrollableListRow listRow = new DeploymentsScrollableListRow(getState(), deploymentData, statusRowElement, missionRowElement);
			GUIAncor anchor = new GUIAncor(getState(), this.anchor.getWidth() - 28.0f, 28.0f);
			anchor.attach(redrawButtonPane(deploymentData, anchor));
			listRow.expanded = new GUIElementList(getState());
			listRow.expanded.add(new GUIListElement(anchor, getState()));
			listRow.expanded.attach(anchor);
			listRow.onInit();
			guiElementList.addWithoutUpdate(listRow);
		}
		guiElementList.updateDim();
	}

	public class DeploymentsScrollableListRow extends ScrollableTableList<FleetDeploymentData>.Row {

		public DeploymentsScrollableListRow(InputState state, FleetDeploymentData deploymentData, GUIElement... elements) {
			super(state, deploymentData, elements);
			this.highlightSelect = true;
			this.highlightSelectSimple = true;
			this.setAllwaysOneSelected(true);
		}

		@Override
		public void extended() {
			if(!isOccluded()) super.extended();
			else super.unexpend();
		}

		@Override
		public void collapsed() {
			if(!isOccluded()) super.collapsed();
			else super.extended();
		}

		@Override
		public boolean isOccluded() {
			return (input != null && input.isActive()) || (EditFleetDialog.homeStationDialog != null && EditFleetDialog.homeStationDialog.isActive());
		}
	}
}