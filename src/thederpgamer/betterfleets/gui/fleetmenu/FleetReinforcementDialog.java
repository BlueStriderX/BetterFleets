package thederpgamer.betterfleets.gui.fleetmenu;

import api.utils.gui.GUIInputDialog;
import api.utils.gui.GUIInputDialogPanel;
import org.schema.game.client.view.gui.catalog.newcatalog.TypeRowItem;
import org.schema.game.common.controller.observer.DrawerObservable;
import org.schema.game.common.controller.observer.DrawerObserver;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.*;
import org.schema.schine.input.InputState;
import thederpgamer.betterfleets.data.fleet.FleetDeploymentData;
import thederpgamer.betterfleets.data.misc.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;
import java.util.Set;

/**
 * [Description]
 *
 * @author TheDerpGamer (MrGoose#0027)
 */
public class FleetReinforcementDialog extends GUIInputDialog {

	private static FleetDeploymentData deploymentData;
	private static ArrayList<TypeRowItem> reinforcementItems;

	public FleetReinforcementDialog(FleetDeploymentData data, ArrayList<ItemStack> needed) {
		deploymentData = data;
		reinforcementItems = convertItemStacksToTypeRowItems(needed);
	}

	private ArrayList<TypeRowItem> convertItemStacksToTypeRowItems(ArrayList<ItemStack> needed) {
		ArrayList<TypeRowItem> items = new ArrayList<>();
		for(ItemStack itemStack : needed) items.add(new TypeRowItem(itemStack.getInfo(), itemStack.getId(), getState()));
		return items;
	}

	@Override
	public FleetReinforcementPanel createPanel() {
		return new FleetReinforcementPanel(getState(), this);
	}

	@Override
	public FleetReinforcementPanel getInputPanel() {
		return (FleetReinforcementPanel) super.getInputPanel();
	}

	@Override
	public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
		if(mouseEvent.pressedLeftMouse() && guiElement.getUserPointer() != null) {
			switch((String) guiElement.getUserPointer()) {
				case "OK":

					break;
				case "X":
				case "CANCEL":
					deactivate();
					break;
			}
		}
	}

	public static class FleetReinforcementPanel extends GUIInputDialogPanel {

		public FleetReinforcementPanel(InputState inputState, GUICallback guiCallback) {
			super(inputState, "FLEET_REINFORCEMENTS", "Resources Required", "", 800, 650, guiCallback);
		}

		@Override
		public void onInit() {
			super.onInit();
			FleetReinforcementItemScrollableList scrollableList = new FleetReinforcementItemScrollableList(getState(), this);
			scrollableList.onInit();
			getContent().attach(scrollableList);
		}
	}

	public static class FleetReinforcementItemScrollableList extends ScrollableTableList<TypeRowItem> implements DrawerObserver {

		public FleetReinforcementItemScrollableList(InputState inputState, GUIElement guiElement) {
			super(inputState, 800, 650, guiElement);

		}

		@Override
		public void update(DrawerObservable drawerObservable, Object o, Object o1) {
			flagDirty(); //Todo: Update list based on resource changes
		}

		@Override
		public void initColumns() {
			addColumn("Type", 3.0f, new Comparator<TypeRowItem>() {
				@Override
				public int compare(TypeRowItem o1, TypeRowItem o2) {
					return o1.info.name.compareToIgnoreCase(o2.info.name);
				}
			});

			addFixedWidthColumn(Lng.str("Provided"), 64, new Comparator<TypeRowItem>() {
				@Override
				public int compare(TypeRowItem o1, TypeRowItem o2) {
					return o1.getProgress() - o2.getProgress();
				}
			});

			addFixedWidthColumn(Lng.str("Goal"), 64, new Comparator<TypeRowItem>() {
				@Override
				public int compare(TypeRowItem o1, TypeRowItem o2) {
					return o1.getGoal() - o2.getGoal();
				}
			});

			addFixedWidthColumn(Lng.str("Progress"), 100, new Comparator<TypeRowItem>() {
				@Override
				public int compare(TypeRowItem o1, TypeRowItem o2) {
					return Float.compare(o1.getPercent(), o2.getPercent());
				}
			});

			/*
			addFixedWidthColumn(Lng.str("Options"), 100, new Comparator<TypeRowItem>() {
				@Override
				public int compare(TypeRowItem o1, TypeRowItem o2) {
					return 0;
				}
			});
			 */

			addTextFilter(new GUIListFilterText<TypeRowItem>() {

				@Override
				public boolean isOk(String input, TypeRowItem listElement) {
					return listElement.info.getName().toLowerCase(Locale.ENGLISH).contains(input.toLowerCase(Locale.ENGLISH));
				}
			}, Lng.str("SEARCH BY TYPE"), ControllerElement.FilterRowStyle.LEFT);

			addDropdownFilter(new GUIListFilterDropdown<TypeRowItem, String>("UNFILLED", "FILLED") {
				@Override
				public boolean isOk(String type, TypeRowItem item) {
					return (item.getProgress() == item.getGoal() && type.equals("FILLED")) || (item.getProgress() < item.getGoal() && type.equals("UNFILLED"));
				}
			}, new CreateGUIElementInterface<String>() {
				@Override
				public GUIElement create(String type) {
					GUIAncor anchor = new GUIAncor(getState(), 10.0F, 24.0F);
					GUITextOverlayTableDropDown dropDown;
					(dropDown = new GUITextOverlayTableDropDown(10, 10, getState())).setTextSimple(type);
					dropDown.setPos(4.0F, 4.0F, 0.0F);
					anchor.setUserPointer(type);
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
		protected ArrayList<TypeRowItem> getElementList() {
			return reinforcementItems;
		}

		@Override
		public void updateListEntries(GUIElementList guiElementList, Set<TypeRowItem> set) {
			guiElementList.deleteObservers();
			guiElementList.addObserver(this);
			for(TypeRowItem item : set) {
				GUITextOverlayTable nameTextElement;
				(nameTextElement = new GUITextOverlayTable(10, 10, this.getState())).setTextSimple(item.info.getName());
				GUIClippedRow nameRowElement;
				(nameRowElement = new GUIClippedRow(this.getState())).attach(nameTextElement);

				GUITextOverlayTable providedTextElement;
				(providedTextElement = new GUITextOverlayTable(10, 10, this.getState())).setTextSimple(item.getProgress());
				GUIClippedRow providedRowElement;
				(providedRowElement = new GUIClippedRow(this.getState())).attach(providedTextElement);

				GUITextOverlayTable goalTextElement;
				(goalTextElement = new GUITextOverlayTable(10, 10, this.getState())).setTextSimple(item.getGoal());
				GUIClippedRow goalRowElement;
				(goalRowElement = new GUIClippedRow(this.getState())).attach(goalTextElement);

				GUITextOverlayTable progressTextElement;
				(progressTextElement = new GUITextOverlayTable(10, 10, this.getState())).setTextSimple(item.getPercent() + "%");
				GUIClippedRow progressRowElement;
				(progressRowElement = new GUIClippedRow(this.getState())).attach(progressTextElement);

				FleetReinforcementItemScrollableListRow listRow = new FleetReinforcementItemScrollableListRow(getState(), item, nameRowElement, providedRowElement, goalRowElement, progressRowElement);
				listRow.onInit();
				guiElementList.addWithoutUpdate(listRow);
			}
			guiElementList.updateDim();
		}

		public class FleetReinforcementItemScrollableListRow extends ScrollableTableList<TypeRowItem>.Row {

			public FleetReinforcementItemScrollableListRow(InputState inputState, TypeRowItem typeRowItem, GUIElement... guiElements) {
				super(inputState, typeRowItem, guiElements);
			}
		}
	}
}
