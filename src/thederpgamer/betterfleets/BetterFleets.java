package thederpgamer.betterfleets;

import api.common.GameClient;
import api.listener.Listener;
import api.listener.events.input.MousePressEvent;
import api.mod.StarLoader;
import api.mod.StarMod;
import org.apache.commons.io.IOUtils;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.gamemap.GameMapDrawer;
import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.fleet.FleetCommandTypes;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationCallback;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea;
import org.schema.schine.input.InputState;
import thederpgamer.betterfleets.manager.ConfigManager;
import thederpgamer.betterfleets.manager.FleetGUIManager;
import thederpgamer.betterfleets.manager.LogManager;
import thederpgamer.betterfleets.utils.MessageType;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * BetterFleets mod main class.
 *
 * @author TheDerpGamer
 * @since 06/14/2021
 */
public class BetterFleets extends StarMod {

    //Instance
    private static BetterFleets instance;
    public static BetterFleets getInstance() {
        return instance;
    }
    public BetterFleets() {

    }
    public static void main(String[] args) {

    }

    //Data
    private final String[] overwriteClasses = new String[] {
            "MapControllerManager",
            "MapToolsPanel"
    };

    @Override
    public void onEnable() {
        instance = this;
        ConfigManager.initialize(this);
        LogManager.initialize();
        registerListeners();
        LogManager.logMessage(MessageType.INFO, "Successfully loaded mod data.");
    }

    @Override
    public byte[] onClassTransform(String className, byte[] byteCode) {
        for(String name : overwriteClasses) if(className.endsWith(name)) return overwriteClass(className, byteCode);
        return super.onClassTransform(className, byteCode);
    }

    private void registerListeners() {
        StarLoader.registerListener(MousePressEvent.class, new Listener<MousePressEvent>() {
            @Override
            public void onEvent(MousePressEvent event) {
                try {
                    GameMapDrawer mapDrawer = GameClient.getClientState().getWorldDrawer().getGameMapDrawer();
                    if(mapDrawer.isMapActive()) { //Check if the map is currently open
                        Vector3i selectedPos = mapDrawer.getGameMapPosition().get(new Vector3i());
                        ArrayList<Fleet> clientFleets = new ArrayList<>(GameClient.getClientState().getFleetManager().getAvailableFleetsClient());
                        ArrayList<Fleet> sectorFleets = new ArrayList<>();
                        for(Fleet fleet : clientFleets) if(fleet.getFlagShip().getSector().equals(selectedPos)) sectorFleets.add(fleet);

                        /*
                        if(event.getRawEvent().pressedLeftMouse()) {
                            if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                                for(Fleet fleet : sectorFleets) {
                                    if(FleetGUIManager.selectedFleets.contains(fleet)) {
                                        FleetGUIManager.selectedFleets.remove(fleet);
                                        LogManager.logDebug("Client removed fleet " + fleet.getName().trim() + " from selection.");
                                        GameClient.getClientState().getController().queueUIAudio("0022_menu_ui - back");
                                    } else {
                                        FleetGUIManager.selectedFleets.add(fleet);
                                        LogManager.logDebug("Client added fleet " + fleet.getName().trim() + " to selection.");
                                        GameClient.getClientState().getController().queueUIAudio("0022_menu_ui - select 2");
                                    }
                                }
                            } else {
                                if(!sectorFleets.isEmpty()) {
                                    FleetGUIManager.selectedFleets.clear();
                                    FleetGUIManager.selectedFleets.add(sectorFleets.get(0));
                                    LogManager.logDebug("Client added fleet " + sectorFleets.get(0).getName().trim() + " to selection.");
                                    GameClient.getClientState().getController().queueUIAudio("0022_menu_ui - select 1");
                                }
                            }
                            FleetGUIManager.getPanel().updateFleetList();
                        } else if(event.getRawEvent().pressedRightMouse()) {
                         */
                        if(event.getRawEvent().pressedRightMouse()) {
                            StringBuilder builder = new StringBuilder();
                            for(int i = 0; i < FleetGUIManager.selectedFleets.size(); i ++) {
                                builder.append(FleetGUIManager.selectedFleets.get(i));
                                if(i < FleetGUIManager.selectedFleets.size() - 1) builder.append(", ");
                            }
                            LogManager.logDebug("Client right-clicked on fleet map GUI with the following fleets selected:\n" + builder.toString());

                            if(!FleetGUIManager.selectedFleets.isEmpty()) {
                                FleetGUIManager.getPanel().fleetActionsList.onInit();

                                FleetGUIManager.getPanel().fleetActionsList.addButton(0, 0, "TOGGLE FORMATION", GUIHorizontalArea.HButtonColor.BLUE, new GUICallback() {
                                    @Override
                                    public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                                        if(mouseEvent.pressedLeftMouse()) {
                                            GameClient.getClientState().getController().queueUIAudio("0022_menu_ui - enter");
                                            FleetGUIManager.orderFormation();
                                        }
                                    }

                                    @Override
                                    public boolean isOccluded() {
                                        return false;
                                    }
                                }, new GUIActivationCallback() {
                                    @Override
                                    public boolean isVisible(InputState inputState) {
                                        return true;
                                    }

                                    @Override
                                    public boolean isActive(InputState inputState) {
                                        return true;
                                    }
                                });

                                FleetGUIManager.getPanel().fleetActionsList.addButton(0, 1, "IDLE", GUIHorizontalArea.HButtonColor.BLUE, new GUICallback() {
                                    @Override
                                    public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                                        if(mouseEvent.pressedLeftMouse()) {
                                            GameClient.getClientState().getController().queueUIAudio("0022_menu_ui - enter");
                                            FleetGUIManager.orderFleets(FleetCommandTypes.IDLE);
                                        }
                                    }

                                    @Override
                                    public boolean isOccluded() {
                                        return false;
                                    }
                                }, new GUIActivationCallback() {
                                    @Override
                                    public boolean isVisible(InputState inputState) {
                                        return true;
                                    }

                                    @Override
                                    public boolean isActive(InputState inputState) {
                                        return true;
                                    }
                                });

                                FleetGUIManager.getPanel().fleetActionsList.addButton(0, 2, "MOVE", GUIHorizontalArea.HButtonColor.PINK, new GUICallback() {
                                    @Override
                                    public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                                        if(mouseEvent.pressedLeftMouse()) {
                                            GameClient.getClientState().getController().queueUIAudio("0022_menu_ui - enter");
                                            FleetGUIManager.orderFleets(FleetCommandTypes.MOVE_FLEET);
                                        }
                                    }

                                    @Override
                                    public boolean isOccluded() {
                                        return false;
                                    }
                                }, new GUIActivationCallback() {
                                    @Override
                                    public boolean isVisible(InputState inputState) {
                                        return true;
                                    }

                                    @Override
                                    public boolean isActive(InputState inputState) {
                                        return true;
                                    }
                                });

                                FleetGUIManager.getPanel().fleetActionsList.addButton(0, 3, "RECALL TO CARRIER", GUIHorizontalArea.HButtonColor.PINK, new GUICallback() {
                                    @Override
                                    public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                                        if(mouseEvent.pressedLeftMouse()) {
                                            GameClient.getClientState().getController().queueUIAudio("0022_menu_ui - enter");
                                            FleetGUIManager.orderFleets(FleetCommandTypes.IDLE);
                                        }
                                    }

                                    @Override
                                    public boolean isOccluded() {
                                        return false;
                                    }
                                }, new GUIActivationCallback() {
                                    @Override
                                    public boolean isVisible(InputState inputState) {
                                        return true;
                                    }

                                    @Override
                                    public boolean isActive(InputState inputState) {
                                        return true;
                                    }
                                });

                                FleetGUIManager.getPanel().fleetActionsList.addButton(0, 4, "PATROL", GUIHorizontalArea.HButtonColor.PINK, new GUICallback() {
                                    @Override
                                    public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                                        if(mouseEvent.pressedLeftMouse()) {
                                            GameClient.getClientState().getController().queueUIAudio("0022_menu_ui - enter");
                                            FleetGUIManager.orderFleets(FleetCommandTypes.PATROL_FLEET);
                                        }
                                    }

                                    @Override
                                    public boolean isOccluded() {
                                        return false;
                                    }
                                }, new GUIActivationCallback() {
                                    @Override
                                    public boolean isVisible(InputState inputState) {
                                        return true;
                                    }

                                    @Override
                                    public boolean isActive(InputState inputState) {
                                        return true;
                                    }
                                });

                                FleetGUIManager.getPanel().fleetActionsList.addButton(0, 5, "ATTACK", GUIHorizontalArea.HButtonColor.RED, new GUICallback() {
                                    @Override
                                    public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                                        if(mouseEvent.pressedLeftMouse()) {
                                            GameClient.getClientState().getController().queueUIAudio("0022_menu_ui - enter");
                                            FleetGUIManager.orderFleets(FleetCommandTypes.FLEET_ATTACK);
                                        }
                                    }

                                    @Override
                                    public boolean isOccluded() {
                                        return false;
                                    }
                                }, new GUIActivationCallback() {
                                    @Override
                                    public boolean isVisible(InputState inputState) {
                                        return true;
                                    }

                                    @Override
                                    public boolean isActive(InputState inputState) {
                                        return true;
                                    }
                                });

                                FleetGUIManager.getPanel().fleetActionsList.addButton(0, 6, "DEFEND", GUIHorizontalArea.HButtonColor.GREEN, new GUICallback() {
                                    @Override
                                    public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                                        if(mouseEvent.pressedLeftMouse()) {
                                            GameClient.getClientState().getController().queueUIAudio("0022_menu_ui - enter");
                                            FleetGUIManager.orderFleets(FleetCommandTypes.FLEET_DEFEND);
                                        }
                                    }

                                    @Override
                                    public boolean isOccluded() {
                                        return false;
                                    }
                                }, new GUIActivationCallback() {
                                    @Override
                                    public boolean isVisible(InputState inputState) {
                                        return true;
                                    }

                                    @Override
                                    public boolean isActive(InputState inputState) {
                                        return true;
                                    }
                                });

                                FleetGUIManager.getPanel().fleetActionsList.addButton(0, 7, "SENTRY", GUIHorizontalArea.HButtonColor.ORANGE, new GUICallback() {
                                    @Override
                                    public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                                        if(mouseEvent.pressedLeftMouse()) {
                                            GameClient.getClientState().getController().queueUIAudio("0022_menu_ui - enter");
                                            FleetGUIManager.orderFleets(FleetCommandTypes.SENTRY);
                                        }
                                    }

                                    @Override
                                    public boolean isOccluded() {
                                        return false;
                                    }
                                }, new GUIActivationCallback() {
                                    @Override
                                    public boolean isVisible(InputState inputState) {
                                        return true;
                                    }

                                    @Override
                                    public boolean isActive(InputState inputState) {
                                        return true;
                                    }
                                });

                                FleetGUIManager.getPanel().fleetActionsList.addButton(0, 8, "MINE", GUIHorizontalArea.HButtonColor.ORANGE, new GUICallback() {
                                    @Override
                                    public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                                        if(mouseEvent.pressedLeftMouse()) {
                                            GameClient.getClientState().getController().queueUIAudio("0022_menu_ui - enter");
                                            FleetGUIManager.orderFleets(FleetCommandTypes.MINE_IN_SECTOR);
                                        }
                                    }

                                    @Override
                                    public boolean isOccluded() {
                                        return false;
                                    }
                                }, new GUIActivationCallback() {
                                    @Override
                                    public boolean isVisible(InputState inputState) {
                                        return true;
                                    }

                                    @Override
                                    public boolean isActive(InputState inputState) {
                                        return true;
                                    }
                                });

                                FleetGUIManager.getPanel().fleetActionsList.addButton(0, 9, "TOGGLE JAMMING", GUIHorizontalArea.HButtonColor.YELLOW, new GUICallback() {
                                    @Override
                                    public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                                        if(mouseEvent.pressedLeftMouse()) {
                                            GameClient.getClientState().getController().queueUIAudio("0022_menu_ui - enter");
                                            FleetGUIManager.orderJamming();
                                        }
                                    }

                                    @Override
                                    public boolean isOccluded() {
                                        return false;
                                    }
                                }, new GUIActivationCallback() {
                                    @Override
                                    public boolean isVisible(InputState inputState) {
                                        return true;
                                    }

                                    @Override
                                    public boolean isActive(InputState inputState) {
                                        return true;
                                    }
                                });

                                FleetGUIManager.getPanel().fleetActionsList.addButton(0, 10, "TOGGLE CLOAK", GUIHorizontalArea.HButtonColor.YELLOW, new GUICallback() {
                                    @Override
                                    public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                                        if(mouseEvent.pressedLeftMouse()) {
                                            GameClient.getClientState().getController().queueUIAudio("0022_menu_ui - enter");
                                            FleetGUIManager.orderCloak();
                                        }
                                    }

                                    @Override
                                    public boolean isOccluded() {
                                        return false;
                                    }
                                }, new GUIActivationCallback() {
                                    @Override
                                    public boolean isVisible(InputState inputState) {
                                        return true;
                                    }

                                    @Override
                                    public boolean isActive(InputState inputState) {
                                        return true;
                                    }
                                });
                                FleetGUIManager.getPanel().fleetActionsList.moveToMouse();
                                FleetGUIManager.getPanel().fleetActionsList.draw();
                                FleetGUIManager.getPanel().fleetBox.draw();
                            } else FleetGUIManager.getPanel().updateFleetList();
                        }
                    }
                } catch(ArrayIndexOutOfBoundsException exception) {
                    //LogManager.logWarning("Encountered an exception while trying to add/remove fleets from the map fleet selection", exception);
                }
            }
        }, this);
    }

    private byte[] overwriteClass(String className, byte[] byteCode) {
        byte[] bytes = null;
        try {
            ZipInputStream file = new ZipInputStream(new FileInputStream(this.getSkeleton().getJarFile()));
            while(true) {
                ZipEntry nextEntry = file.getNextEntry();
                if(nextEntry == null) break;
                if(nextEntry.getName().endsWith(className + ".class")) bytes = IOUtils.toByteArray(file);
            }
            file.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
        if(bytes != null) return bytes;
        else return byteCode;
    }
}
