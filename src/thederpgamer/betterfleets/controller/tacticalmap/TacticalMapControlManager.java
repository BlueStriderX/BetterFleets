package thederpgamer.betterfleets.controller.tacticalmap;

import api.common.GameClient;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.schema.common.util.linAlg.Vector3fTools;
import org.schema.game.client.controller.manager.AbstractControlManager;
import org.schema.game.client.controller.manager.ingame.PlayerInteractionControlManager;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.server.data.ServerConfig;
import org.schema.schine.graphicsengine.camera.CameraMouseState;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.input.KeyboardMappings;

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
        super.onSwitch(active);
    }

    @Override
    public void update(Timer timer) {
        CameraMouseState.setGrabbed(Mouse.isButtonDown(1));
        getInteractionManager().suspend(true);
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
        if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
            Controller.setCamera(guiDrawer.getDefaultCamera());
            onSwitch(false);
            return;
        }

        Vector3f movement = new Vector3f();
        int amount = 5;
        if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
            if(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) amount = 100;
            else amount = 50;
        }
        if(Keyboard.isKeyDown(KeyboardMappings.FORWARD.getMapping())) movement.add(new Vector3f(0, 0, amount));
        if(Keyboard.isKeyDown(KeyboardMappings.BACKWARDS.getMapping())) movement.add(new Vector3f(0, 0, -amount));
        if(Keyboard.isKeyDown(KeyboardMappings.STRAFE_LEFT.getMapping())) movement.add(new Vector3f(-amount, 0, 0));
        if(Keyboard.isKeyDown(KeyboardMappings.STRAFE_RIGHT.getMapping())) movement.add(new Vector3f(amount, 0, 0));
        if(Keyboard.isKeyDown(KeyboardMappings.UP.getMapping())) movement.add(new Vector3f(0, amount, 0));
        if(Keyboard.isKeyDown(KeyboardMappings.DOWN.getMapping())) movement.add(new Vector3f(0, -amount, 0));
        movement.scale(timer.getDelta());
        move(movement);
    }

    private void move(Vector3f movement) {
        Vector3f dir = new Vector3f();
        Vector3f m = new Vector3f(0, 0, 0);
        if(movement.z != 0) {
            m.z = movement.z;
            movement.z = 0;
            GlUtil.getForwardVector(dir, guiDrawer.camera.getWorldTransform());
        }

        if(movement.y != 0) {
            m.y = movement.y;
            movement.y = 0;
            GlUtil.getUpVector(dir, guiDrawer.camera.getWorldTransform());
        }

        if(movement.x != 0) {
            m.x = movement.x;
            movement.x = 0;
            GlUtil.getRightVector(dir, guiDrawer.camera.getWorldTransform());
        }

        if(Math.abs(dir.x) >= Math.abs(dir.y) && Math.abs(dir.x) >= Math.abs(dir.z)) {
            if(dir.x >= 0) movement.x = m.x;
            else movement.x = -m.x;
        } else if(Math.abs(dir.y) >= Math.abs(dir.x) && Math.abs(dir.y) >= Math.abs(dir.z)) {
            if(dir.y >= 0) movement.y = m.y;
            else movement.y = -m.y;
        } else if(Math.abs(dir.z) >= Math.abs(dir.y) && Math.abs(dir.z) >= Math.abs(dir.x)) {
            if(dir.z >= 0) movement.z = m.z;
            else movement.z = -m.z;
        }
        Vector3f newPos = new Vector3f(guiDrawer.camera.getWorldTransform().origin);
        newPos.add(movement);
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
