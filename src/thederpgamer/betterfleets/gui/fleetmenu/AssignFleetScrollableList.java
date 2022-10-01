package thederpgamer.betterfleets.gui.fleetmenu;

import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.fleet.FleetCommandTypes;
import org.schema.game.common.data.fleet.FleetMember;
import org.schema.schine.graphicsengine.forms.gui.GUIAncor;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
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
 * @version 1.0 - [05/01/2022]
 */
public class AssignFleetScrollableList extends ScrollableTableList<Fleet> {

	private final GUIElement anchor;
	private final FleetDeploymentData deploymentData;

	public AssignFleetScrollableList(InputState state, float width, float height, GUIElement anchor, FleetDeploymentData deploymentData) {
		super(state, width, height, anchor);
		this.deploymentData = deploymentData;
		this.anchor = anchor;
		this.anchor.attach(this);
	}

	@Override
	public void initColumns() {
		addColumn("Name", 12.0f, new Comparator<Fleet>() {
			@Override
			public int compare(Fleet o1, Fleet o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});

		addColumn("Health", 8.5f, new Comparator<Fleet>() {
			@Override
			public int compare(Fleet o1, Fleet o2) {
				return Float.compare(getAverageHealth(o1), getAverageHealth(o2));
			}
		});

		addColumn("Status", 10.0f, new Comparator<Fleet>() {
			@Override
			public int compare(Fleet o1, Fleet o2) {
				return getStatus(o1).compareTo(getStatus(o2));
			}
		});

		addTextFilter(new GUIListFilterText<Fleet>() {
			@Override
			public boolean isOk(String s, Fleet fleet) {
				return fleet.getName().toLowerCase().trim().contains(s.toLowerCase().trim());
			}
		}, ControllerElement.FilterRowStyle.LEFT);

		addDropdownFilter(new GUIListFilterDropdown<Fleet, FleetCommandTypes>(FleetCommandTypes.values()) {
			@Override
			public boolean isOk(FleetCommandTypes commandType, Fleet fleet) {
				return fleet.getMissionName().equals(commandType.getName());
			}
		}, new CreateGUIElementInterface<FleetCommandTypes>() {
			@Override
			public GUIElement create(FleetCommandTypes commandType) {
				GUIAncor anchor = new GUIAncor(getState(), 10.0F, 24.0F);
				GUITextOverlayTableDropDown dropDown;
				(dropDown = new GUITextOverlayTableDropDown(10, 10, getState())).setTextSimple(commandType.name());
				dropDown.setPos(4.0F, 4.0F, 0.0F);
				anchor.setUserPointer(commandType.name());
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
	protected Collection<Fleet> getElementList() {
		return FleetDeploymentManager.getAvailableFleets();
	}

	@Override
	public void updateListEntries(GUIElementList guiElementList, Set<Fleet> set) {
		guiElementList.deleteObservers();
		guiElementList.addObserver(this);
		for(Fleet fleet : set) {
			GUITextOverlayTable nameTextElement;
			(nameTextElement = new GUITextOverlayTable(10, 10, this.getState())).setTextSimple(fleet.getName());
			GUIClippedRow nameRowElement;
			(nameRowElement = new GUIClippedRow(this.getState())).attach(nameTextElement);

			GUITextOverlayTable healthTextElement;
			(healthTextElement = new GUITextOverlayTable(10, 10, this.getState())).setTextSimple(getAverageHealth(fleet));
			GUIClippedRow healthRowElement;
			(healthRowElement = new GUIClippedRow(this.getState())).attach(healthTextElement);

			GUITextOverlayTable statusTextElement;
			(statusTextElement = new GUITextOverlayTable(10, 10, this.getState())).setTextSimple(getStatus(fleet));
			GUIClippedRow statusRowElement;
			(statusRowElement = new GUIClippedRow(this.getState())).attach(statusTextElement);

			AssignFleetScrollableListRow listRow = new AssignFleetScrollableListRow(getState(), fleet, nameRowElement, healthRowElement, statusRowElement);
			listRow.onInit();
			guiElementList.addWithoutUpdate(listRow);
		}
		guiElementList.updateDim();
	}

	private float getAverageHealth(Fleet fleet) {
		float average = 0;
		for(FleetMember member : fleet.getMembers()) average += member.getShipPercent();
		return average / fleet.getMembers().size();
	}

	private String getStatus(Fleet fleet) {
		return fleet.getMissionName(); //Todo: Make this more specific
	}

	public class AssignFleetScrollableListRow extends ScrollableTableList<Fleet>.Row {

		public AssignFleetScrollableListRow(InputState state, Fleet fleet, GUIElement... elements) {
			super(state, fleet, elements);
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
	}
}