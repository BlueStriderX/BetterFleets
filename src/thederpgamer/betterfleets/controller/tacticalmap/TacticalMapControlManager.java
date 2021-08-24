package thederpgamer.betterfleets.controller.tacticalmap;

import api.common.GameClient;
import org.lwjgl.input.Mouse;
import org.schema.common.util.linAlg.Vector3fTools;
import org.schema.game.client.controller.manager.AbstractControlManager;
import org.schema.game.client.controller.manager.ingame.PlayerInteractionControlManager;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.server.data.ServerConfig;
import org.schema.schine.graphicsengine.camera.CameraMouseState;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.input.KeyEventInterface;
import javax.vecmath.Vector3f;

/**
 * <Description>
 *
 * @author TheDerpGamer
 * @since 07/12/2021
 */
public class TacticalMapControlManager extends AbstractControlManager {

    private TacticalMapGUIDrawer guiDrawer;

    public TacticalMapControlManager(TacticalMapGUIDrawer guiDrawer) {
        super(GameClient.getClientState());
        this.guiDrawer = guiDrawer;
    }

    @Override
    public void onSwitch(boolean active) {
        getInteractionManager().getInShipControlManager().getShipControlManager().getShipExternalFlightController().suspend(active);
        getInteractionManager().getInShipControlManager().getShipControlManager().getSegmentBuildController().suspend(active);
        getInteractionManager().setActive(!active);
        super.onSwitch(active);
    }

    @Override
    public void update(Timer timer) {
        CameraMouseState.setGrabbed(Mouse.isButtonDown(1));
        getInteractionManager().setActive(false);
        getInteractionManager().suspend(true);
        getInteractionManager().getBuildToolsManager().suspend(true);
        getInteractionManager().getInShipControlManager().getShipControlManager().getShipExternalFlightController().suspend(true);
        getInteractionManager().getInShipControlManager().getShipControlManager().getSegmentBuildController().suspend(true);
    }

    @Override
    public void handleMouseEvent(MouseEvent mouseEvent) {
        super.handleMouseEvent(mouseEvent);
    }

    @Override
    public void handleKeyEvent(KeyEventInterface keyEvent) {

    }

    public void move(int x, int y, int z) {
        Vector3f dir = new Vector3f();
        int m = 0;
        if(z != 0) {
            m = z;
            z = 0;
            GlUtil.getForwardVector(dir, guiDrawer.camera.getWorldTransform());
        }

        if(y != 0) {
            m = y;
            y = 0;
            GlUtil.getUpVector(dir, guiDrawer.camera.getWorldTransform());
        }

        if(x != 0) {
            m = x;
            x = 0;
            GlUtil.getRightVector(dir, guiDrawer.camera.getWorldTransform());
        }

        if(Math.abs(dir.x) >= Math.abs(dir.y) && Math.abs(dir.x) >= Math.abs(dir.z)) {
            if(dir.x >= 0) x = m;
            else x = -m;
        } else if(Math.abs(dir.y) >= Math.abs(dir.x) && Math.abs(dir.y) >= Math.abs(dir.z)) {
            if(dir.y >= 0) y = m;
            else y = -m;
        } else if(Math.abs(dir.z) >= Math.abs(dir.y) && Math.abs(dir.z) >= Math.abs(dir.x)) {
            if(dir.z >= 0) z = m;
            else z = -m;
        }
        Vector3f newPos = new Vector3f(guiDrawer.camera.getWorldTransform().origin);
        newPos.add(new Vector3f(x, y, z));
        if(getDistanceFromControl(newPos) < (int) ServerConfig.SECTOR_SIZE.getCurrentState()) guiDrawer.camera.getWorldTransform().origin.set(newPos);
    }

    private float getDistanceFromControl(Vector3f newPos) {
        if(GameClient.getCurrentControl() != null && GameClient.getCurrentControl() instanceof SegmentController) {
            Vector3f controlPos = ((SegmentController) GameClient.getCurrentControl()).getWorldTransform().origin;
            return Math.abs(Vector3fTools.distance(newPos.x, newPos.y, newPos.z, controlPos.x, controlPos.y, controlPos.z));
        }
        return (float) ServerConfig.SECTOR_SIZE.getCurrentState();
    }

    private PlayerInteractionControlManager getInteractionManager() {
        return GameClient.getClientState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager();
    }
}
