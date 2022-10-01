package thederpgamer.betterfleets.data.misc;

import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.inventory.InventorySlot;

/**
 * Simple ItemStack class for storing item data.
 *
 * @author TheDerpGamer (MrGoose#0027)
 */
public class ItemStack {

	private final short id;
	private int amount;

	public ItemStack(short id, int amount) {
		this.id = id;
		this.amount = amount;
	}

	public ItemStack(InventorySlot slot) {
		this(slot.getType(), slot.count());
	}

	public short getId() {
		return id;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public ElementInformation getInfo() {
		return ElementKeyMap.getInfo(id);
	}

	public String serialize() {
		return id + ":" + amount;
	}

	public static ItemStack deserialize(String string) {
		String[] split = string.split(":");
		return new ItemStack(Short.parseShort(split[0]), Integer.parseInt(split[1]));
	}
}
