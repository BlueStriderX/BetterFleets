package thederpgamer.betterfleets.systems.repairpastefabricator;

import api.common.GameClient;
import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import api.utils.game.module.ModManagerContainerModule;
import org.schema.common.util.StringTools;
import org.schema.game.client.view.BuildModeDrawer;
import org.schema.game.client.view.gui.shiphud.newhud.HudContextHelperContainer;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.ContextFilter;
import thederpgamer.betterfleets.BetterFleets;
import thederpgamer.betterfleets.element.ElementManager;
import thederpgamer.betterfleets.utils.ConfigManager;

import java.io.IOException;

/**
 * ModuleContainerManager for Repair Paste Fabricator system.
 *
 * @author TheDerpGamer
 * @since 07/03/2021
 */
public class RepairPasteFabricatorModuleContainer extends ModManagerContainerModule {

    private float repairPasteCapacity = 0f;
    private float repairPasteCapacityMax = 0f;
    private float timer;

    public RepairPasteFabricatorModuleContainer(SegmentController ship, ManagerContainer<?> managerContainer) {
        super(ship, managerContainer, BetterFleets.getInstance(), ElementManager.getBlock("Repair Paste Fabricator").getId());
        this.timer = 1000f;
    }

    @Override
    public void handle(Timer timer) {
        this.timer = Math.max(0f, this.timer - 1);
        if(this.timer <= 0) {
            setRepairPasteCapacity(repairPasteCapacity + (ConfigManager.getSystemConfig().getInt("repair-paste-regen-per-block") * getSize()));
            this.timer = 1000f;
        }
        updateGUI();
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
    }

    public void setRepairPasteCapacityMax(float repairPasteCapacityMax) {
        if(repairPasteCapacity > repairPasteCapacityMax) repairPasteCapacity = repairPasteCapacityMax;
        this.repairPasteCapacityMax = repairPasteCapacityMax;
    }

    private void updateGUI() {
        if(BuildModeDrawer.currentPiece.getType() == ElementManager.getBlock("Repair Paste Fabricator").getId()) {
            GameClient.getClientState().getWorldDrawer().getGuiDrawer().getHud().getHelpManager().addInfo(HudContextHelperContainer.Hos.MOUSE, ContextFilter.NORMAL, getHudText());
        }
    }

    private String getHudText() {
        return "Repair Paste Fabricator:\n  Size: " + getSize() +
                "\n   Capacity: " + StringTools.formatPointZero(repairPasteCapacity) + " / " +
                StringTools.formatPointZero(repairPasteCapacity) + "\n  Generation: " + getRepairPasteRegen()  + "/s";
    }
}
