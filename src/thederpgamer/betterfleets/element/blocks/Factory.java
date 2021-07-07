package thederpgamer.betterfleets.element.blocks;

import api.config.BlockConfig;
import org.schema.game.common.data.element.ElementCategory;
import org.schema.game.common.data.element.ElementInformation;
import thederpgamer.betterfleets.BetterFleets;

/**
 * Abstract Factory Block class.
 *
 * @author TheDerpGamer
 * @since 07/02/2021
 */
public abstract class Factory {

    protected ElementInformation blockInfo;

    public Factory(String name, ElementCategory category) {
        blockInfo = BlockConfig.newFactory(BetterFleets.getInstance(), name, new short[6]);
        BlockConfig.setElementCategory(blockInfo, category);
    }

    public final ElementInformation getBlockInfo() {
        return blockInfo;
    }

    public final short getId() {
        return blockInfo.getId();
    }

    public abstract void initialize();
}