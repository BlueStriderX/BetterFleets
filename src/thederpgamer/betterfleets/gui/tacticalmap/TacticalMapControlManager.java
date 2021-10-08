package thederpgamer.betterfleets.gui.tacticalmap;

import api.common.GameClient;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.schema.common.util.linAlg.Vector3fTools;
import org.schema.game.client.controller.manager.AbstractControlManager;
import org.schema.game.client.controller.manager.ingame.PlayerInteractionControlManager;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.server.data.ServerConfig;
import org.schema.schine.graphicsengine.camera.CameraMouseState;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.input.KeyboardMappings;
import thederpgamer.betterfleets.BetterFleets;
import thederpgamer.betterfleets.gui.element.sprite.TacticalMapEntityIndicator;
import thederpgamer.betterfleets.manager.ConfigManager;
import thederpgamer.betterfleets.utils.Inputs;

import javax.vecmath.Vector3f;
import java.nio.FloatBuffer;
import java.util.Map;

/**
 * <Description>
 *
 * @author TheDerpGamer
 * @since 07/12/2021
 */
public class TacticalMapControlManager extends AbstractControlManager {

    private final TacticalMapGUIDrawer guiDrawer;
    public float viewDistance;

    private FloatBuffer depthBuffer;

    public TacticalMapControlManager(TacticalMapGUIDrawer guiDrawer) {
        super(GameClient.getClientState());
        this.guiDrawer = guiDrawer;
        this.viewDistance = (float) ConfigManager.getMainConfig().getDouble("tactical-map-view-distance");
    }

    @Override
    public void onSwitch(boolean active) {
        guiDrawer.clearSelected();
        getInteractionManager().setActive(!active);
        getInteractionManager().getInShipControlManager().getShipControlManager().getShipExternalFlightController().suspend(active);
        getInteractionManager().getInShipControlManager().getShipControlManager().getSegmentBuildController().suspend(active);
        super.onSwitch(active);
    }

    @Override
    public void update(Timer timer) {
        CameraMouseState.setGrabbed(Mouse.isButtonDown(1));
        getInteractionManager().suspend(true);
        getInteractionManager().setActive(false);
        getInteractionManager().getBuildToolsManager().suspend(true);
        getInteractionManager().getInShipControlManager().getShipControlManager().getShipExternalFlightController().suspend(true);
        getInteractionManager().getInShipControlManager().getShipControlManager().getSegmentBuildController().suspend(true);
        handleInteraction(timer);
    }

    @Override
    public void handleMouseEvent(MouseEvent mouseEvent) {
        super.handleMouseEvent(mouseEvent);
    }

    @Override
    public void handleKeyEvent(KeyEventInterface keyEvent) {
        super.handleKeyEvent(keyEvent);
    }

    private void handleInteraction(Timer timer) {
        Vector3f movement = new Vector3f();
        int amount = 100;
        if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
            if(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) amount = 5000;
            else amount = 1000;
        }
        if(Keyboard.getEventKey() == Keyboard.KEY_LMENU && Keyboard.getEventKeyState()) BetterFleets.getInstance().tacticalMapDrawer.drawMovementPaths = !BetterFleets.getInstance().tacticalMapDrawer.drawMovementPaths;
        if(Keyboard.isKeyDown(Keyboard.KEY_X)) BetterFleets.getInstance().tacticalMapDrawer.camera.reset();
        if(Keyboard.isKeyDown(KeyboardMappings.FORWARD.getMapping())) movement.add(new Vector3f(0, 0, amount));
        if(Keyboard.isKeyDown(KeyboardMappings.BACKWARDS.getMapping())) movement.add(new Vector3f(0, 0, -amount));
        if(Keyboard.isKeyDown(KeyboardMappings.STRAFE_LEFT.getMapping())) movement.add(new Vector3f(amount, 0, 0));
        if(Keyboard.isKeyDown(KeyboardMappings.STRAFE_RIGHT.getMapping())) movement.add(new Vector3f(-amount, 0, 0));
        if(Keyboard.isKeyDown(KeyboardMappings.UP.getMapping())) movement.add(new Vector3f(0, amount, 0));
        if(Keyboard.isKeyDown(KeyboardMappings.DOWN.getMapping())) movement.add(new Vector3f(0, -amount, 0));
        movement.scale(timer.getDelta());
        move(movement);

        TacticalMapEntityIndicator selected = null;

        if(Mouse.getEventButtonState() && !Mouse.isGrabbed()) {
            for(Map.Entry<Integer, TacticalMapEntityIndicator> entry : guiDrawer.drawMap.entrySet()) {
                if(entry.getValue().getEntity().getFactionId() == 0 || GameClient.getClientPlayerState().getFactionId() == 0) continue;
                if(entry.getValue().getEntity().getFactionId() == GameClient.getClientPlayerState().getFactionId()) {
                    if(Mouse.getEventButton() == Inputs.MouseButtons.LEFT_MOUSE.id && entry.getValue().selected) {
                        selected = entry.getValue();
                        break;
                    }
                }
            }

            if(selected == null) { //Clicked on nothing, so clear selected and return
                guiDrawer.clearSelected();
                return;
            }

            if(!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) guiDrawer.clearSelected(); //Clear current before selecting in single add mode
        }
    }

    private void move(Vector3f movement) {
        Vector3f move = new Vector3f();
        Vector3f forward = new Vector3f(guiDrawer.camera.getForward());
        Vector3f up = new Vector3f(guiDrawer.camera.getUp());
        Vector3f right = new Vector3f(guiDrawer.camera.getRight());

        if(movement.x != 0) {
            right.scale(movement.x);
            move.add(right);
        }

        if(movement.y != 0) {
            up.scale(movement.y);
            move.add(up);
        }

        if(movement.z != 0) {
            forward.scale(movement.z);
            move.add(forward);
        }

        Vector3f newPos = new Vector3f(guiDrawer.camera.getWorldTransform().origin);
        newPos.add(move);
        if(getDistanceFromControl(newPos) < (int) ServerConfig.SECTOR_SIZE.getCurrentState() * ConfigManager.getMainConfig().getDouble("tactical-map-view-distance")) guiDrawer.camera.getWorldTransform().origin.set(newPos);
    }

    private float getDistanceFromControl(Vector3f newPos) {
        if(GameClient.getCurrentControl() != null && GameClient.getCurrentControl() instanceof SegmentController) {
            Vector3f controlPos = ((SegmentController) GameClient.getCurrentControl()).getWorldTransform().origin;
            return Math.abs(Vector3fTools.distance(newPos.x, newPos.y, newPos.z, controlPos.x, controlPos.y, controlPos.z));
        }
        return (int) ServerConfig.SECTOR_SIZE.getCurrentState();
    }

    private PlayerInteractionControlManager getInteractionManager() {
        return GameClient.getClientState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager();
    }
}
