package thederpgamer.betterfleets.data.shipyard;

import org.schema.game.common.data.element.meta.VirtualBlueprintMetaItem;

/**
 * Wrapper class for shipyard designs.
 *
 * @author TheDerpGamer (MrGoose#0027)
 */
public class ShipyardDesignData {

	private final String UID;
	private final String name;
	private final int id;

	public ShipyardDesignData(VirtualBlueprintMetaItem virtualBlueprintMetaItem) {
		this.UID = virtualBlueprintMetaItem.UID;
		this.name = virtualBlueprintMetaItem.virtualName;
		this.id = virtualBlueprintMetaItem.getId();
	}

	public String getUID() {
		return UID;
	}

	public String getName() {
		return name;
	}

	public int getId() {
		return id;
	}
}
