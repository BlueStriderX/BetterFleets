package org.schema.game.client.controller.manager.ingame.map;

import api.common.GameClient;
import api.utils.StarRunnable;
import org.lwjgl.input.Keyboard;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.manager.AbstractControlManager;
import org.schema.game.client.controller.manager.ingame.PlayerInteractionControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.gamemap.entry.SelectableMapEntry;
import org.schema.game.client.data.gamemap.entry.TransformableEntityMapEntry;
import org.schema.game.client.view.gamemap.StarPosition;
import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.fleet.FleetMember;
import org.schema.schine.graphicsengine.camera.CameraMouseState;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.input.Mouse;
import thederpgamer.betterfleets.BetterFleets;
import thederpgamer.betterfleets.utils.FleetGUIManager;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Modified version of MapControllerManager.
 *
 * @author Schema, TheDerpGamer
 * @since 06/29/2021
 */
public class MapControllerManager extends AbstractControlManager {

    public static final HashSet<SelectableMapEntry> selected = new HashSet();
    long lastClick;

    public MapControllerManager(GameClientState var1) {
        super(var1);
    }

    public PlayerInteractionControlManager getInteractionManager() {
        return this.getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager();
    }

    private void removeFleet(Fleet fleet) {
        FleetGUIManager.selectedFleets.remove(fleet);
        fleet.getFlagShip().mapEntry.getColor().set(FleetGUIManager.getIconColor(fleet));
    }

    public void handleKeyEvent(KeyEventInterface keyEvent) {
        super.handleKeyEvent(keyEvent);
        if(KeyboardMappings.getEventKeyState(keyEvent, this.getState()) && KeyboardMappings.getEventKeySingle(keyEvent) == 88) {
            this.getState().getController().getClientChannel().getClientMapRequestManager().requestSystem(new Vector3i(0, 0, 0));
        }

        //INSERTED CODE
        if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE) || Keyboard.isKeyDown(Keyboard.KEY_M)) {
            for(Fleet fleet : FleetGUIManager.selectedFleets) {
                FleetGUIManager.selectedFleets.remove(fleet);
                fleet.getFlagShip().mapEntry.getColor().set(FleetGUIManager.getIconColor(fleet));
            }
            FleetGUIManager.selectedFleets.clear();
            FleetGUIManager.getPanel().updateFleetList();
        }
        //

        this.getState().getWorldDrawer().getGameMapDrawer().handleKeyEvent(keyEvent);
    }

    public void handleMouseEvent(MouseEvent mouseEvent) {
        //INSERTED CODE
        if(mouseEvent.state) {
            if(mouseEvent.pressedLeftMouse()) {
                if(!FleetGUIManager.getPanel().fleetActionsList.active) {
                    boolean doubleClick = System.currentTimeMillis() - lastClick < 300L;
                    if(!doubleClick) {
                        ArrayList<Fleet> toRemove = new ArrayList<>(FleetGUIManager.selectedFleets);
                        for(Fleet fleet : toRemove) removeFleet(fleet);
                        FleetGUIManager.selectedFleets.clear();
                        FleetGUIManager.getPanel().updateFleetList();
                    } else {
                        for(SelectableMapEntry selectableEntry : selected) {
                            System.err.println("[CLIENT][MAPMANAGER] clicked on " + selectableEntry);
                            if(selectableEntry instanceof TransformableEntityMapEntry) {
                                Vector3i var5 = this.getState().getWorldDrawer().getGameMapDrawer().getGameMapPosition().getCurrentSysPos();
                                TransformableEntityMapEntry var6 = (TransformableEntityMapEntry) selectableEntry;
                                this.getState().getWorldDrawer().getGameMapDrawer().getGameMapPosition().set((int) (var6.getPos().x / 6.25F) + (var5.x << 4), (int) (var6.getPos().y / 6.25F) + (var5.y << 4), (int) (var6.getPos().z / 6.25F) + (var5.z << 4), false);
                            } else if(selectableEntry instanceof StarPosition) {
                                this.getState().getWorldDrawer().getGameMapDrawer().getGameMapPosition().getCurrentSysPos();
                                StarPosition var7 = (StarPosition) selectableEntry;
                                this.getState().getWorldDrawer().getGameMapDrawer().getGameMapPosition().set(((int) var7.getPos().x << 4) + 8, ((int) var7.getPos().y << 4) + 8, ((int) var7.getPos().z << 4) + 8, false);
                            } else if(selectableEntry instanceof FleetMember.FleetMemberMapIndication) {
                                this.getState().getWorldDrawer().getGameMapDrawer().getGameMapPosition().getCurrentSysPos();
                                FleetMember.FleetMemberMapIndication var8 = (FleetMember.FleetMemberMapIndication) selectableEntry;
                                this.getState().getWorldDrawer().getGameMapDrawer().getGameMapPosition().set((int) (var8.getPos().x / 6.25F), (int) (var8.getPos().y / 6.25F), (int) (var8.getPos().z / 6.25F), false);
                            }
                        }
                    }
                } else {
                    new StarRunnable() {
                        @Override
                        public void run() {
                            ArrayList<Fleet> toRemove = new ArrayList<>(FleetGUIManager.selectedFleets);
                            for(Fleet f : toRemove) removeFleet(f);
                            FleetGUIManager.selectedFleets.clear();
                            FleetGUIManager.getPanel().updateFleetList();
                        }
                    }.runLater(BetterFleets.getInstance(), 3);
                }
            }
        }
        //
        super.handleMouseEvent(mouseEvent);
    }

    public void onSwitch(boolean active) {
        //INSERTED CODE
        Vector3i sector = GameClient.getClientPlayerState().getCurrentSector();
        if(sector.x >= 100000000 || sector.y >= 100000000 || sector.z >= 100000000) active = false; //Don't draw if outside universe to prevent visual glitches
        //
        if(active) {
            this.getState().getController().queueUIAudio("0022_menu_ui - swoosh scroll large");
            this.getState().getController().getClientChannel().getClientMapRequestManager().requestSystem(new Vector3i(0, 0, 0));
        } else this.getState().getController().queueUIAudio("0022_menu_ui - swoosh scroll small");

        this.getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getInShipControlManager().getShipControlManager().getShipExternalFlightController().suspend(active);
        this.getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getInShipControlManager().getShipControlManager().getSegmentBuildController().suspend(active);
        if(active) this.getState().getWorldDrawer().getGameMapDrawer().resetToCurrentSector();
        super.onSwitch(active);
    }

    public void update(Timer timer) {
        CameraMouseState.setGrabbed(Mouse.isButtonDown(1));
        this.getInteractionManager().suspend(true);
    }
}