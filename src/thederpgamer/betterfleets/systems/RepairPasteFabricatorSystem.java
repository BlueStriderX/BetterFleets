package thederpgamer.betterfleets.systems;

import api.utils.game.module.util.SimpleDataStorageMCModule;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.schine.graphicsengine.core.Timer;
import thederpgamer.betterfleets.BetterFleets;
import thederpgamer.betterfleets.data.system.RepairPasteSystemData;
import thederpgamer.betterfleets.element.ElementManager;
import thederpgamer.betterfleets.manager.ConfigManager;

/**
 * ModuleContainerManager for Repair Paste Fabricator system.
 *
 * @author TheDerpGamer
 * @since 07/03/2021
 */
public class RepairPasteFabricatorSystem extends SimpleDataStorageMCModule {

    public static final float UPDATE_TIMER = 1000.0f;
    private float timer;

    public RepairPasteFabricatorSystem(SegmentController ship, ManagerContainer<?> managerContainer) {
        super(ship, managerContainer, BetterFleets.getInstance(), ElementManager.getBlock("Repair Paste Fabricator").getId());
        this.timer = UPDATE_TIMER;
        if(data == null || !(data instanceof RepairPasteSystemData)) data = new RepairPasteSystemData();
    }

    @Override
    public void handle(Timer timer) {
        super.handle(timer);
        if(this.timer <= 0) {
            updateSystemData();
            this.timer = UPDATE_TIMER;
        } else this.timer --;
    }

    @Override
    public void handlePlace(long absIndex, byte orientation) {
        super.handlePlace(absIndex, orientation);
        updateSystemData();
    }

    @Override
    public void handleRemove(long absIndex) {
       super.handleRemove(absIndex);
       updateSystemData();
    }

    @Override
    public double getPowerConsumedPerSecondResting() {
        return ConfigManager.getSystemConfig().getInt("repair-paste-power-consumed-per-block-resting") * getSize();
    }

    @Override
    public double getPowerConsumedPerSecondCharging() {
        return ConfigManager.getSystemConfig().getInt("repair-paste-power-consumed-per-block-charging") * getSize();
    }

    @Override
    public String getName() {
        return "Repair Paste Fabricator";
    }

    @Override
    public void dischargeFully() {
        setRepairPasteCapacity(0);
    }

    private RepairPasteSystemData getSystemData() {
        if(data == null || !(data instanceof RepairPasteSystemData)) data = new RepairPasteSystemData();
        return (RepairPasteSystemData) data;
    }

    public float getRepairPasteCapacity() {
        return getSystemData().repairPasteCapacity;
    }

    public float getRepairPasteCapacityMax() {
        return getSystemData().repairPasteCapacityMax;
    }

    public float getRepairPasteRegen() {
        return ConfigManager.getSystemConfig().getInt("repair-paste-regen-per-block") * getSize();
    }

    public void setRepairPasteCapacity(float repairPasteCapacity) {
        if(repairPasteCapacity < 0) repairPasteCapacity = 0;
        getSystemData().repairPasteCapacity = Math.min(repairPasteCapacity, getRepairPasteCapacityMax());
        try {
            BetterFleets.getInstance().repairPasteHudOverlay.updateText(segmentController, getRepairPasteCapacity(), getRepairPasteCapacityMax());
        } catch(Exception exception) {
            exception.printStackTrace();
        }
    }

    public void setRepairPasteCapacityMax(float repairPasteCapacityMax) {
        if(getRepairPasteCapacity() > repairPasteCapacityMax) setRepairPasteCapacity(getRepairPasteCapacityMax());
        getSystemData().repairPasteCapacityMax = repairPasteCapacityMax;
    }

    public void updateSystemData() {
        setRepairPasteCapacityMax(ConfigManager.getSystemConfig().getInt("repair-paste-capacity-per-block") * getSize());
        setRepairPasteCapacity(getRepairPasteCapacity() + getRepairPasteRegen());
        flagUpdatedData();
    }
}
