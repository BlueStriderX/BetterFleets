package thederpgamer.betterfleets.systems.repairpastefabricator;

import api.utils.game.module.CustomModuleUtils;
import org.schema.game.client.view.gui.structurecontrol.GUIKeyValueEntry;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ElementCollectionManager;
import org.schema.game.common.data.SegmentPiece;
import thederpgamer.betterfleets.element.ElementManager;
import thederpgamer.betterfleets.utils.ConfigManager;

/**
 * Element Collection Manager for Repair Paste Fabricator system.
 *
 * @author TheDerpGamer
 * @since 07/03/2021
 */
public class RepairPasteFabricatorCollectionManager extends ElementCollectionManager<RepairPasteFabricatorUnit, RepairPasteFabricatorCollectionManager, RepairPasteFabricatorElementManager> {

    public RepairPasteFabricatorCollectionManager(SegmentController segController, RepairPasteFabricatorElementManager elementManager) {
        super(ElementManager.getBlock("Repair Paste Fabricator").getId(), segController, elementManager);
        CustomModuleUtils.setCollectionManager(this, ElementManager.getBlock("Repair Paste Fabricator").getId());
    }

    @Override
    public int getMargin() {
        return 0;
    }

    @Override
    protected Class<RepairPasteFabricatorUnit> getType() {
        return RepairPasteFabricatorUnit.class;
    }

    @Override
    public boolean needsUpdate() {
        return false;
    }

    @Override
    public RepairPasteFabricatorUnit getInstance() {
        return new RepairPasteFabricatorUnit();
    }

    @Override
    protected void onChangedCollection() {

    }

    @Override
    protected void onFinishedCollection() {
        super.onFinishedCollection();
        //float prevMax = getElementManager().getRepairPasteCapacityMax(); Probably not needed for this.
        float size = getTotalSize();
        float capacityPerBlock = ConfigManager.getSystemConfig().getInt("repair-paste-capacity-per-block");
        //float regenPerBlock = ConfigManager.getSystemConfig().getInt("repair-paste-regen-per-block"); This should probably be done in the element manager instead.
        getElementManager().setRepairPasteCapacityMax(size * capacityPerBlock);
    }

    @Override
    public GUIKeyValueEntry[] getGUICollectionStats() {
        return new GUIKeyValueEntry[0];
    }

    @Override
    public String getModuleName() {
        return "Repair Paste Fabricator System";
    }

    @Override
    public float getSensorValue(SegmentPiece connected) {
        return Math.min(1f, getElementManager().getRepairPasteCapacity() / getElementManager().getRepairPasteCapacityMax());
    }

    @Override
    public CollectionShape requiredNeigborsPerBlock() {
        return CollectionShape.ALL_IN_ONE;
    }
}
