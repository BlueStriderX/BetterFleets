package thederpgamer.betterfleets.data.shipyard;

import api.utils.other.HashList;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.elements.ManagerModuleCollection;
import org.schema.game.common.controller.elements.shipyard.ShipyardCollectionManager;
import org.schema.game.common.controller.elements.shipyard.ShipyardElementManager;
import org.schema.game.common.controller.elements.shipyard.ShipyardUnit;
import org.schema.game.common.data.MetaObjectState;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.meta.MetaObject;
import org.schema.game.common.data.element.meta.VirtualBlueprintMetaItem;
import org.schema.game.common.data.player.inventory.Inventory;
import thederpgamer.betterfleets.BetterFleets;
import thederpgamer.betterfleets.data.misc.ItemStack;
import thederpgamer.betterfleets.utils.SegmentPieceUtils;

import java.util.ArrayList;
import java.util.logging.Level;

/**
 * Wrapper class for a shipyard system.
 *
 * @author TheDerpGamer (MrGoose#0027)
 */
public class ShipyardData {

	private final ManagerModuleCollection<ShipyardUnit, ShipyardCollectionManager, ShipyardElementManager> shipyard;

	public ShipyardData(ManagerModuleCollection<ShipyardUnit, ShipyardCollectionManager, ShipyardElementManager> shipyard) {
		this.shipyard = shipyard;
	}

	public HashList<Inventory, ItemStack> getAvailableInventory() {
		HashList<Inventory, ItemStack> availableInventory = new HashList<>();
		//Add shipyard inventory
		for(ShipyardCollectionManager collectionManager : shipyard.getCollectionManagers()) {
			Inventory inventory = collectionManager.getInventory();
			for(int slot : inventory.getSlots()) {
				if(inventory.getSlot(slot) != null) availableInventory.add(inventory, new ItemStack(inventory.getSlot(slot)));
			}
		}

		//Add inventories of connected stashes
		for(ShipyardCollectionManager collectionManager : shipyard.getCollectionManagers()) {
			for(SegmentPiece segmentPiece : SegmentPieceUtils.getControlledPiecesMatching(collectionManager.getControllerElement(), ElementKeyMap.STASH_ELEMENT)) {
				Inventory inventory = SegmentPieceUtils.getInventory(segmentPiece);
				if(inventory != null) {
					for(int slot : inventory.getSlots()) {
						if(inventory.getSlot(slot) != null) availableInventory.add(inventory, new ItemStack(inventory.getSlot(slot)));
					}
				} else throw new NullPointerException("Stash inventory is null!");
			}
		}
		return availableInventory;
	}

	public ArrayList<ShipyardDesignData> getAvailableDesigns(ShipyardData shipyardData) {
		ArrayList<ShipyardDesignData> availableDesigns = new ArrayList<>();
		for(ShipyardCollectionManager collectionManager : shipyard.getCollectionManagers()) {
			for(int slot : collectionManager.getInventory().getSlots()) {
				if(collectionManager.getInventory().getSlot(slot) != null && collectionManager.getInventory().getSlot(slot).isMetaItem()) {
					try {
						MetaObject metaObject = ((MetaObjectState) collectionManager.getSegmentController().getState()).getMetaObjectManager().getObject(slot);
						if(metaObject instanceof VirtualBlueprintMetaItem) availableDesigns.add(new ShipyardDesignData((VirtualBlueprintMetaItem) metaObject));
					} catch(Exception exception) {
						BetterFleets.log.log(Level.WARNING, "Failed to get meta object for slot " + slot + " in shipyard " + collectionManager.getSegmentController().getName() + "!", exception);
					}
				}
			}
		}
		return availableDesigns;
	}

	public Vector3i getSector() {
		return shipyard.getElementManager().getSegmentController().getSector(new Vector3i());
	}
}
