package thederpgamer.betterfleets;

import api.common.GameClient;
import api.config.BlockConfig;
import api.listener.Listener;
import api.listener.events.gui.HudCreateEvent;
import api.listener.events.input.MousePressEvent;
import api.listener.events.register.ManagerContainerRegisterEvent;
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
import org.schema.schine.resource.ResourceLoader;
import thederpgamer.betterfleets.element.ElementManager;
import thederpgamer.betterfleets.element.blocks.systems.RepairPasteFabricator;
import thederpgamer.betterfleets.gui.hud.RepairPasteFabricatorHudOverlay;
import thederpgamer.betterfleets.systems.RepairPasteFabricatorSystem;
import thederpgamer.betterfleets.utils.*;
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
            "MapToolsPanel",
            "RepairBeamHandler",
            "FleetManager",
            "FleetFiniteStateMachine",
            "FleetState",
            "FleetStateType",
            "FleetCommandTypes",
            "Transition",
            "HudContextHelpManager"
    };

    //Hud Elements
    public RepairPasteFabricatorHudOverlay repairPasteHudOverlay;

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

    @Override
    public void onResourceLoad(ResourceLoader loader) {
        ResourceManager.loadResources(this, loader);
    }

    @Override
    public void onBlockConfigLoad(BlockConfig blockConfig) {
        //Systems
        ElementManager.addBlock(new RepairPasteFabricator());

        ElementManager.initialize();
    }

    private void registerListeners() {
        StarLoader.registerListener(ManagerContainerRegisterEvent.class, new Listener<ManagerContainerRegisterEvent>() {
            @Override
            public void onEvent(ManagerContainerRegisterEvent event) {
                event.addModMCModule(new RepairPasteFabricatorSystem(event.getSegmentController(), event.getContainer()));
            }
        }, this);

        StarLoader.registerListener(HudCreateEvent.class, new Listener<HudCreateEvent>() {
            @Override
            public void onEvent(HudCreateEvent event) {
                event.addElement(repairPasteHudOverlay = new RepairPasteFabricatorHudOverlay(event.getInputState()));
            }
        }, this);

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

                                FleetGUIManager.getPanel().fleetActionsList.addButton(0, 6, "ARTILLERY", GUIHorizontalArea.HButtonColor.RED, new GUICallback() {
                                    @Override
                                    public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                                        if(mouseEvent.pressedLeftMouse()) {
                                            GameClient.getClientState().getController().queueUIAudio("0022_menu_ui - enter");
                                            FleetGUIManager.orderFleets(FleetCommandTypes.ARTILLERY);
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


                                FleetGUIManager.getPanel().fleetActionsList.addButton(0, 7, "DEFEND", GUIHorizontalArea.HButtonColor.GREEN, new GUICallback() {
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

                                FleetGUIManager.getPanel().fleetActionsList.addButton(0, 8, "INTERCEPT", GUIHorizontalArea.HButtonColor.GREEN, new GUICallback() {
                                    @Override
                                    public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                                        if(mouseEvent.pressedLeftMouse()) {
                                            GameClient.getClientState().getController().queueUIAudio("0022_menu_ui - enter");
                                            FleetGUIManager.orderFleets(FleetCommandTypes.INTERCEPT);
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

                                FleetGUIManager.getPanel().fleetActionsList.addButton(0, 9, "SENTRY", GUIHorizontalArea.HButtonColor.ORANGE, new GUICallback() {
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

                                FleetGUIManager.getPanel().fleetActionsList.addButton(0, 10, "SUPPORT", GUIHorizontalArea.HButtonColor.ORANGE, new GUICallback() {
                                    @Override
                                    public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                                        if(mouseEvent.pressedLeftMouse()) {
                                            GameClient.getClientState().getController().queueUIAudio("0022_menu_ui - enter");
                                            FleetGUIManager.orderFleets(FleetCommandTypes.SUPPORT);
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

                                FleetGUIManager.getPanel().fleetActionsList.addButton(0, 11, "MINE", GUIHorizontalArea.HButtonColor.ORANGE, new GUICallback() {
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

                                FleetGUIManager.getPanel().fleetActionsList.addButton(0, 12, "TOGGLE JAMMING", GUIHorizontalArea.HButtonColor.YELLOW, new GUICallback() {
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

                                FleetGUIManager.getPanel().fleetActionsList.addButton(0, 13, "TOGGLE CLOAK", GUIHorizontalArea.HButtonColor.YELLOW, new GUICallback() {
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
