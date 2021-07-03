package thederpgamer.betterfleets.systems.repairpastefabricator;

import com.bulletphysics.linearmath.Transform;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.RailDockingListener;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.ShipManagerContainer;
import org.schema.game.common.controller.elements.UsableControllableSingleElementManager;
import org.schema.game.common.controller.elements.power.reactor.PowerConsumer;
import org.schema.game.common.controller.observer.DrawerObserver;
import org.schema.game.common.controller.rails.RailRelation;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import thederpgamer.betterfleets.utils.ConfigManager;

/**
 * Element Manager for Repair Paste Fabricator system.
 *
 * @author TheDerpGamer
 * @since 07/03/2021
 */
public class RepairPasteFabricatorElementManager extends UsableControllableSingleElementManager<RepairPasteFabricatorUnit, RepairPasteFabricatorCollectionManager, RepairPasteFabricatorElementManager> implements PowerConsumer, RailDockingListener {

    private float repairPasteCapacity = 1;
    private float repairPasteCapacityMax = 1;
    private float powered;

    public RepairPasteFabricatorElementManager(SegmentController segmentController, Class<RepairPasteFabricatorCollectionManager> clazz) {
        super(segmentController, clazz);
        if(!segmentController.isOnServer()) addObserver((DrawerObserver) segmentController.getState());
        //CustomModuleUtils.setElementManager(this, ElementManager.getBlock("Repair Paste Fabricator").getId(), ElementManager.getBlock("Repair Paste Fabricator").getId());
    }

    @Override
    public void onControllerChange() {

    }

    @Override
    public ControllerManagerGUI getGUIUnitValues(RepairPasteFabricatorUnit repairPasteFabricatorUnit, RepairPasteFabricatorCollectionManager repairPasteFabricatorCollectionManager, ControlBlockElementCollectionManager<?, ?, ?> controlBlockElementCollectionManager, ControlBlockElementCollectionManager<?, ?, ?> controlBlockElementCollectionManager1) {
        return ControllerManagerGUI.create((GameClientState) getState(), "Repair Paste Fabricator System", repairPasteFabricatorUnit);
    }

    @Override
    protected String getTag() {
        return "repairpastefabricator";
    }

    @Override
    public RepairPasteFabricatorCollectionManager getNewCollectionManager(SegmentPiece segmentPiece, Class<RepairPasteFabricatorCollectionManager> clazz) {
        return new RepairPasteFabricatorCollectionManager(getSegmentController(), this);
    }

    @Override
    protected void playSound(RepairPasteFabricatorUnit repairPasteFabricatorUnit, Transform transform) {

    }

    @Override
    public void handle(ControllerStateInterface controllerStateInterface, Timer timer) {

    }

    public float getRepairPasteCapacity() {
        return repairPasteCapacity;
    }

    public float getRepairPasteCapacityMax() {
        return repairPasteCapacityMax;
    }

    public void setRepairPasteCapacity(float repairPasteCapacity) {
        this.repairPasteCapacity = Math.min(repairPasteCapacity, repairPasteCapacityMax);
    }

    public void setRepairPasteCapacityMax(float repairPasteCapacityMax) {
        if(repairPasteCapacity > repairPasteCapacityMax) repairPasteCapacity = repairPasteCapacityMax;
        this.repairPasteCapacityMax = repairPasteCapacityMax;
    }

    @Override
    public void dockingChanged(SegmentController segmentController, boolean docked) {
        if(isOnServer()) {
            if(!docked) {
                SegmentController root = getSegmentController().railController.getRoot();
                if(root == getSegmentController() || !hasRepairSystem(segmentController)) setRepairPasteCapacity(0);
            }
        }
    }

    @Override
    public double getPowerConsumedPerSecondResting() {
        return ConfigManager.getSystemConfig().getInt("repair-paste-power-consumed-per-block-resting") * totalSize;
    }

    @Override
    public double getPowerConsumedPerSecondCharging() {
        return ConfigManager.getSystemConfig().getInt("repair-paste-power-consumed-per-block-charging") * totalSize;
    }

    @Override
    public boolean isPowerCharging(long l) {
        return repairPasteCapacity < repairPasteCapacityMax;
    }

    @Override
    public void setPowered(float powered) {
        this.powered = powered;
    }

    @Override
    public float getPowered() {
        return powered;
    }

    @Override
    public PowerConsumerCategory getPowerConsumerCategory() {
        return PowerConsumerCategory.SUPPORT_BEAMS;
    }

    @Override
    public void reloadFromReactor(double secTime, Timer timer, float tickTime, boolean charging, float resting) {

    }

    @Override
    public boolean isPowerConsumerActive() {
        return true;
    }

    @Override
    public String getName() {
        return "RepairPasteFabricatorElementManager";
    }

    @Override
    public void dischargeFully() {
        setRepairPasteCapacity(0);
    }

    public Tag toTag() {
        return new Tag(Tag.Type.STRUCT, "RepairPasteFabricatorSystem", new Tag[] {
                new Tag(Tag.Type.BYTE, "Version", 1),
                new Tag(Tag.Type.FLOAT, "RepairPasteCapacity", getRepairPasteCapacity()),
                new Tag(Tag.Type.FLOAT, "RepairPasteCapacityMax", getRepairPasteCapacityMax()),
                FinishTag.INST});
    }

    public void readFromTag(Tag tag) {
        Tag[] tagArray = tag.getStruct();
        setRepairPasteCapacity(tagArray[1].getFloat());
        setRepairPasteCapacityMax(tagArray[2].getFloat());
    }

    private boolean hasRepairSystem(SegmentController segmentController) {
        if(((ManagedSegmentController) segmentController).getManagerContainer() instanceof ShipManagerContainer && ((ShipManagerContainer) ((ManagedSegmentController) segmentController).getManagerContainer()).getRepair().getCollectionManagers().size() > 0) return true;
        if(!segmentController.railController.next.isEmpty()) {
            for(RailRelation relation : segmentController.railController.next) {
                if(hasRepairSystem(relation.docked.getSegmentController())) return true;
            }
        }
        return false;
    }
}
