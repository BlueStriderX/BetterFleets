package thederpgamer.betterfleets.gui.fleetmenu;

import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.input.InputState;
import thederpgamer.betterfleets.data.fleet.DeploymentStationData;
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
public class FactionStationList extends ScrollableTableList<DeploymentStationData> {

	private final GUIElement anchor;
	private final FleetDeploymentData deploymentData;

	public FactionStationList(InputState state, float width, float height, GUIElement anchor, FleetDeploymentData deploymentData) {
		super(state, width, height, anchor);
		this.deploymentData = deploymentData;
		this.anchor = anchor;
		this.anchor.attach(this);
	}

	@Override
	public void initColumns() {
		addColumn("Name", 12.0f, new Comparator<DeploymentStationData>() {
			@Override
			public int compare(DeploymentStationData o1, DeploymentStationData o2) {
				return o1.getStation().getName().compareTo(o2.getStation().getName());
			}
		});

		addColumn("Has Shipyard", 7.5f, new Comparator<DeploymentStationData>() {
			@Override
			public int compare(DeploymentStationData o1, DeploymentStationData o2) {
				return Boolean.compare(o1.hasShipyard(), o2.hasShipyard());
			}
		});
	}

	@Override
	protected Collection<DeploymentStationData> getElementList() {
		return FleetDeploymentManager.getAvailableStations();
	}

	@Override
	public void updateListEntries(GUIElementList guiElementList, Set<DeploymentStationData> set) {
		guiElementList.deleteObservers();
		guiElementList.addObserver(this);
		for(DeploymentStationData stationData : set) {
			GUITextOverlayTable nameTextElement;
			(nameTextElement = new GUITextOverlayTable(10, 10, this.getState())).setTextSimple(stationData.getStation().getName());
			GUIClippedRow nameRowElement;
			(nameRowElement = new GUIClippedRow(this.getState())).attach(nameTextElement);

			GUITextOverlayTable hasShipyardTextElement;
			(hasShipyardTextElement = new GUITextOverlayTable(10, 10, this.getState())).setTextSimple(stationData.getStation().getName());
			GUIClippedRow hasShipyardRowElement;
			(hasShipyardRowElement = new GUIClippedRow(this.getState())).attach(hasShipyardTextElement);

			FactionStationListRow listRow = new FactionStationListRow(getState(), stationData, nameRowElement, hasShipyardRowElement);
			listRow.onInit();
			guiElementList.addWithoutUpdate(listRow);
		}
		guiElementList.updateDim();
	}

	public class FactionStationListRow extends ScrollableTableList<DeploymentStationData>.Row {

		public FactionStationListRow(InputState state, DeploymentStationData stationData, GUIElement... elements) {
			super(state, stationData, elements);
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
