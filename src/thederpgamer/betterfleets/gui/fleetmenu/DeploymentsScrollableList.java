package thederpgamer.betterfleets.gui.fleetmenu;

import org.schema.game.client.view.gui.fleet.FleetPanel;
import org.schema.schine.graphicsengine.forms.gui.GUIAncor;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.GUIListElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.*;
import org.schema.schine.input.InputState;
import thederpgamer.betterfleets.data.fleet.FleetDeploymentData;
import thederpgamer.betterfleets.manager.ClientCacheManager;

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
	private final FleetPanel panel;
	public AddDeploymentDialog input;

	public DeploymentsScrollableList(InputState state, float width, float height, GUIElement anchor, FleetPanel panel) {
		super(state, width, height, anchor);
		this.anchor = anchor;
		this.panel = panel;
		anchor.attach(this);
	}

	private GUIHorizontalButtonTablePane redrawButtonPane(final FleetDeploymentData deploymentData, GUIAncor anchor) {
		GUIHorizontalButtonTablePane buttonPane = new GUIHorizontalButtonTablePane(getState(), 2, 1, anchor);
		buttonPane.onInit();

		return buttonPane;
	}

	@Override
	protected Collection<FleetDeploymentData> getElementList() {
		return ClientCacheManager.getFleetDeployments();
	}

	@Override
	public void initColumns() {
		addColumn("Status", 15.0f, new Comparator<FleetDeploymentData>() {
			@Override
			public int compare(FleetDeploymentData o1, FleetDeploymentData o2) {
				return o1.getStatus().toString().compareTo(o2.getStatus().toString());
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
				return deploymentData.getTaskType().equals(deploymentType);
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
			/*
			GUITextOverlayTable ownerTextElement;
			(ownerTextElement = new GUITextOverlayTable(10, 10, this.getState())).setTextSimple(sectorData.ownerName + "'s Build Sector");
			GUIClippedRow ownerRowElement;
			(ownerRowElement = new GUIClippedRow(this.getState())).attach(ownerTextElement);
			 */

			DeploymentsScrollableListRow listRow = new DeploymentsScrollableListRow(getState(), deploymentData);
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
			return input != null && input.isActive();
		}
	}
}