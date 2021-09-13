package thederpgamer.betterfleets.systems;

import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import api.utils.game.module.ModManagerContainerModule;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.schine.graphicsengine.core.Timer;
import thederpgamer.betterfleets.BetterFleets;
import thederpgamer.betterfleets.element.ElementManager;
import thederpgamer.betterfleets.manager.ConfigManager;
import java.io.IOException;

/**
 * ModuleContainerManager for Repair Paste Fabricator system.
 *
 * @author TheDerpGamer
 * @since 07/03/2021
 */
public class RepairPasteFabricatorSystem extends ModManagerContainerModule {

    private float repairPasteCapacity = 0f;
    private float repairPasteCapacityMax = 0f;
    private float timer;

    public RepairPasteFabricatorSystem(SegmentController ship, ManagerContainer<?> managerContainer) {
        super(ship, managerContainer, BetterFleets.getInstance(), ElementManager.getBlock("Repair Paste Fabricator").getId());
        this.timer = 10f;
    }

    @Override
    public void handle(Timer timer) {
        //updateGUI();
        this.timer = Math.max(0f, this.timer - 1);
        if(this.timer <= 0) {
            setRepairPasteCapacity(repairPasteCapacity + (ConfigManager.getSystemConfig().getInt("repair-paste-regen-per-block") * getSize()));
            this.timer = 10f;
        }
    }

    @Override
    public void handlePlace(long abs, byte orientation) {
        super.handlePlace(abs, orientation);
        setRepairPasteCapacityMax(repairPasteCapacityMax + ConfigManager.getSystemConfig().getInt("repair-paste-capacity-per-block"));
    }

    @Override
    public void handleRemove(long abs) {
        super.handleRemove(abs);
        setRepairPasteCapacityMax(repairPasteCapacityMax - ConfigManager.getSystemConfig().getInt("repair-paste-capacity-per-block"));
    }

    @Override
    public void onTagSerialize(PacketWriteBuffer packetWriteBuffer) throws IOException {

    }

    @Override
    public void onTagDeserialize(PacketReadBuffer packetReadBuffer) throws IOException {

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
        return "RepairPasteFabricatorElementManager";
    }

    @Override
    public void dischargeFully() {
        setRepairPasteCapacity(0);
    }

    public float getRepairPasteCapacity() {
        return repairPasteCapacity;
    }

    public float getRepairPasteCapacityMax() {
        return repairPasteCapacityMax;
    }

    public float getRepairPasteRegen() {
        return ConfigManager.getSystemConfig().getInt("repair-paste-regen-per-block") * getSize();
    }

    public void setRepairPasteCapacity(float repairPasteCapacity) {
        if(repairPasteCapacity < 0) repairPasteCapacity = 0;
        this.repairPasteCapacity = Math.min(repairPasteCapacity, repairPasteCapacityMax);
        try {
            BetterFleets.getInstance().repairPasteHudOverlay.updateText(segmentController, this.repairPasteCapacity, this.repairPasteCapacityMax);
        } catch(Exception ignored) { }
    }

    public void setRepairPasteCapacityMax(float repairPasteCapacityMax) {
        if(repairPasteCapacity > repairPasteCapacityMax) repairPasteCapacity = repairPasteCapacityMax;
        this.repairPasteCapacityMax = repairPasteCapacityMax;
    }
}
