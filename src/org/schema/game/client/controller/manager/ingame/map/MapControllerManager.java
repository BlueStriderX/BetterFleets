package org.schema.game.client.controller.manager.ingame.map;

import api.common.GameClient;
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
import org.schema.game.common.data.world.VoidSystem;
import org.schema.schine.graphicsengine.camera.CameraMouseState;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.input.Mouse;
import thederpgamer.betterfleets.manager.FleetGUIManager;
import thederpgamer.betterfleets.manager.LogManager;

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

    public void handleKeyEvent(KeyEventInterface var1) {
        super.handleKeyEvent(var1);
        if (KeyboardMappings.getEventKeyState(var1, this.getState()) && KeyboardMappings.getEventKeySingle(var1) == 88) {
            this.getState().getController().getClientChannel().getClientMapRequestManager().requestSystem(new Vector3i(0, 0, 0));
        }

        this.getState().getWorldDrawer().getGameMapDrawer().handleKeyEvent(var1);
    }

    public void handleMouseEvent(MouseEvent var1) {
        //INSERTED CODE
        if(getState().getController().getPlayerInputs().isEmpty() && var1.state) {
            ArrayList<Fleet> clientFleets = new ArrayList<>(GameClient.getClientState().getFleetManager().getAvailableFleetsClient());
            if(var1.pressedLeftMouse()) {
                boolean doubleClick = System.currentTimeMillis() - lastClick < 300L;
                for(SelectableMapEntry selectableEntry : selected) {
                    System.err.println("[CLIENT][MAPMANAGER] clicked on " + selectableEntry);
                    if(doubleClick) {
                        if(selectableEntry instanceof TransformableEntityMapEntry) {
                            Vector3i var5 = this.getState().getWorldDrawer().getGameMapDrawer().getGameMapPosition().getCurrentSysPos();
                            TransformableEntityMapEntry var6 = (TransformableEntityMapEntry) selectableEntry;
                            this.getState().getWorldDrawer().getGameMapDrawer().getGameMapPosition().set((int)(var6.getPos().x / 6.25F) + (var5.x << 4), (int)(var6.getPos().y / 6.25F) + (var5.y << 4), (int)(var6.getPos().z / 6.25F) + (var5.z << 4), false);
                        } else if(selectableEntry instanceof StarPosition) {
                            this.getState().getWorldDrawer().getGameMapDrawer().getGameMapPosition().getCurrentSysPos();
                            StarPosition var7 = (StarPosition) selectableEntry;
                            this.getState().getWorldDrawer().getGameMapDrawer().getGameMapPosition().set(((int)var7.getPos().x << 4) + 8, ((int)var7.getPos().y << 4) + 8, ((int)var7.getPos().z << 4) + 8, false);
                        } else if(selectableEntry instanceof FleetMember.FleetMemberMapIndication) {
                            this.getState().getWorldDrawer().getGameMapDrawer().getGameMapPosition().getCurrentSysPos();
                            FleetMember.FleetMemberMapIndication var8 = (FleetMember.FleetMemberMapIndication) selectableEntry;
                            this.getState().getWorldDrawer().getGameMapDrawer().getGameMapPosition().set((int)(var8.getPos().x / 6.25F), (int)(var8.getPos().y / 6.25F), (int)(var8.getPos().z / 6.25F), false);
                        }
                    } else {
                        if(selectableEntry instanceof FleetMember.FleetMemberMapIndication) {
                            getState().getWorldDrawer().getGameMapDrawer().getGameMapPosition().getCurrentSysPos();
                            FleetMember.FleetMemberMapIndication fleetIcon = (FleetMember.FleetMemberMapIndication) selectableEntry;
                            Vector3i sectorPos = new Vector3i((int) (fleetIcon.getPos().x / (100f / VoidSystem.SYSTEM_SIZEf)), (int) (fleetIcon.getPos().y / (100f / VoidSystem.SYSTEM_SIZEf)), (int) (fleetIcon.getPos().z / (100f / VoidSystem.SYSTEM_SIZEf)));
                            ArrayList<Fleet> sectorFleets = new ArrayList<>();
                            for(Fleet clientFleet : clientFleets) if(clientFleet.getFlagShip().getSector().equals(sectorPos)) sectorFleets.add(clientFleet);

                            if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                                for(Fleet fleet : sectorFleets) {
                                    if(FleetGUIManager.selectedFleets.contains(fleet)) {
                                        FleetGUIManager.selectedFleets.remove(fleet);
                                        ((FleetMember.FleetMemberMapIndication) selectableEntry).getColor().set(0.3f, 0.8f, 0.2f, 0.8f);
                                        LogManager.logDebug("Client removed fleet " + fleet.getName().trim() + " from selection.");
                                        GameClient.getClientState().getController().queueUIAudio("0022_menu_ui - back");
                                    } else {
                                        FleetGUIManager.selectedFleets.add(fleet);
                                        ((FleetMember.FleetMemberMapIndication) selectableEntry).getColor().set(1.4f, 1.2f, 1.3f, 0.8f);
                                        LogManager.logDebug("Client added fleet " + fleet.getName().trim() + " to selection.");
                                        GameClient.getClientState().getController().queueUIAudio("0022_menu_ui - select 2");
                                    }
                                }
                            } else {
                                ((FleetMember.FleetMemberMapIndication) selectableEntry).getColor().set(0.3f, 0.8f, 0.2f, 0.8f);
                                if(!sectorFleets.isEmpty()) {
                                    FleetGUIManager.selectedFleets.clear();
                                    FleetGUIManager.selectedFleets.add(sectorFleets.get(0));
                                    ((FleetMember.FleetMemberMapIndication) selectableEntry).getColor().set(1.4f, 1.2f, 1.3f, 0.8f);
                                    LogManager.logDebug("Client added fleet " + sectorFleets.get(0).getName().trim() + " to selection.");
                                    GameClient.getClientState().getController().queueUIAudio("0022_menu_ui - select 1");
                                }
                            }
                            FleetGUIManager.getPanel().updateFleetList();
                        }
                    }
                }
            }
        }

        /*
        if (this.getState().getController().getPlayerInputs().isEmpty() && var1.state && var1.button == 0) {
            boolean var2 = System.currentTimeMillis() - this.lastClick < 300L;

            SelectableMapEntry var4;
            for(Iterator var3 = selected.iterator(); var3.hasNext(); System.err.println("[CLIENT][MAPMANAGER] clicked on " + var4)) {
                var4 = (SelectableMapEntry)var3.next();
                if (var2) {
                    if (var4 instanceof TransformableEntityMapEntry) {
                        Vector3i var5 = this.getState().getWorldDrawer().getGameMapDrawer().getGameMapPosition().getCurrentSysPos();
                        TransformableEntityMapEntry var6 = (TransformableEntityMapEntry)var4;
                        this.getState().getWorldDrawer().getGameMapDrawer().getGameMapPosition().set((int)(var6.getPos().x / 6.25F) + (var5.x << 4), (int)(var6.getPos().y / 6.25F) + (var5.y << 4), (int)(var6.getPos().z / 6.25F) + (var5.z << 4), false);
                    } else if (var4 instanceof StarPosition) {
                        this.getState().getWorldDrawer().getGameMapDrawer().getGameMapPosition().getCurrentSysPos();
                        StarPosition var7 = (StarPosition)var4;
                        this.getState().getWorldDrawer().getGameMapDrawer().getGameMapPosition().set(((int)var7.getPos().x << 4) + 8, ((int)var7.getPos().y << 4) + 8, ((int)var7.getPos().z << 4) + 8, false);
                    } else if (var4 instanceof FleetMemberMapIndication) {
                        this.getState().getWorldDrawer().getGameMapDrawer().getGameMapPosition().getCurrentSysPos();
                        FleetMemberMapIndication var8 = (FleetMemberMapIndication)var4;
                        //INSERTED CODE
                        Vector3i newPos = new Vector3i((int) (var8.getPos().x / 6.25F), (int) (var8.getPos().y / 6.25F), (int) (var8.getPos().z / 6.25F));
                        //this.getState().getWorldDrawer().getGameMapDrawer().getGameMapPosition().set((int)(var8.getPos().x / 6.25F), (int)(var8.getPos().y / 6.25F), (int)(var8.getPos().z / 6.25F), false);
                        //
                    }
                }
            }

            this.lastClick = System.currentTimeMillis();
        }

         */
        //
        super.handleMouseEvent(var1);
    }

    public void onSwitch(boolean var1) {
        if (var1) {
            this.getState().getController().queueUIAudio("0022_menu_ui - swoosh scroll large");
            this.getState().getController().getClientChannel().getClientMapRequestManager().requestSystem(new Vector3i(0, 0, 0));
        } else {
            this.getState().getController().queueUIAudio("0022_menu_ui - swoosh scroll small");
        }

        this.getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getInShipControlManager().getShipControlManager().getShipExternalFlightController().suspend(var1);
        this.getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getInShipControlManager().getShipControlManager().getSegmentBuildController().suspend(var1);
        if (var1) {
            this.getState().getWorldDrawer().getGameMapDrawer().resetToCurrentSector();
        }

        super.onSwitch(var1);
    }

    public void update(Timer var1) {
        CameraMouseState.setGrabbed(Mouse.isButtonDown(1));
        this.getInteractionManager().suspend(true);
    }
}