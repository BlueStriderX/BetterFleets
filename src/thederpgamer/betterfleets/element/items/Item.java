package thederpgamer.betterfleets.element.items;

import api.config.BlockConfig;
import org.schema.game.common.data.element.ElementCategory;
import org.schema.game.common.data.element.ElementInformation;
import thederpgamer.betterfleets.BetterFleets;
import thederpgamer.betterfleets.utils.ResourceManager;

/**
 * Abstract Item class.
 *
 * @author TheDerpGamer
 * @since 07/02/2021
 */
public abstract class Item {

    protected ElementInformation itemInfo;

    public Item(String name, ElementCategory category) {
        String internalName = name.toLowerCase().replace(" ", "-").trim();
        short textureId = (short) ResourceManager.getTexture(internalName).getTextureId();
        itemInfo = BlockConfig.newElement(BetterFleets.getInstance(), name, textureId);
        itemInfo.setBuildIconNum(textureId);
        itemInfo.setPlacable(false);
        itemInfo.setPhysical(false);
        BlockConfig.setElementCategory(itemInfo, category);
    }

    public final ElementInformation getItemInfo() {
        return itemInfo;
    }

    public final short getId() {
        return itemInfo.getId();
    }

    public abstract void initialize();
}
