package thederpgamer.betterfleets;

import api.common.GameClient;
import api.config.BlockConfig;
import api.listener.Listener;
import api.listener.events.block.SegmentPieceAddByMetadataEvent;
import api.listener.events.block.SegmentPieceAddEvent;
import api.listener.events.block.SegmentPieceKillEvent;
import api.listener.events.block.SegmentPieceRemoveEvent;
import api.listener.events.draw.RegisterWorldDrawersEvent;
import api.listener.events.gui.HudCreateEvent;
import api.listener.events.input.KeyPressEvent;
import api.listener.events.input.MousePressEvent;
import api.listener.events.register.ManagerContainerRegisterEvent;
import api.listener.events.register.RegisterAddonsEvent;
import api.mod.StarLoader;
import api.mod.StarMod;
import api.network.packets.PacketUtil;
import api.utils.game.module.ModManagerContainerModule;
import api.utils.registry.UniversalRegistry;
import org.apache.commons.io.IOUtils;
import org.lwjgl.input.Keyboard;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.gamemap.GameMapDrawer;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.fleet.FleetCommandTypes;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationCallback;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea;
import org.schema.schine.input.InputState;
import org.schema.schine.input.KeyboardMappings;
import thederpgamer.betterfleets.element.ElementManager;
import thederpgamer.betterfleets.element.blocks.systems.RepairPasteFabricator;
import thederpgamer.betterfleets.gui.drawer.CriticalIndicatorDrawer;
import thederpgamer.betterfleets.gui.hud.RepairPasteFabricatorHudOverlay;
import thederpgamer.betterfleets.gui.tacticalmap.TacticalMapGUIDrawer;
import thederpgamer.betterfleets.manager.*;
import thederpgamer.betterfleets.network.client.RequestFleetDeploymentDataPacket;
import thederpgamer.betterfleets.network.client.SendCommandPacket;
import thederpgamer.betterfleets.network.client.SendFleetDeploymentDataPacket;
import thederpgamer.betterfleets.network.server.SendCommandUpdatePacket;
import thederpgamer.betterfleets.network.server.UpdateFleetDeploymentDataPacket;
import thederpgamer.betterfleets.systems.RepairPasteFabricatorSystem;
import thederpgamer.betterfleets.systems.remotecontrol.RemoteControlAddOn;
import thederpgamer.betterfleets.utils.BlockIconUtils;
import thederpgamer.betterfleets.utils.DataUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * BetterFleets mod main class.
 *
 * @author TheDerpGamer
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
            "PlayerPanel",
            "MapControllerManager",
            "MapToolsPanel",
            "RepairBeamHandler",
            "FleetPanel",
            "FleetManager",
            "FleetFiniteStateMachine",
            "FleetState",
            "FleetStateType",
            "FleetCommandTypes",
            "Transition",
            "HudContextHelpManager",
            "ShipAIEntity"
    };
    public static Logger log;

    //GUI
    public char mapKey;
    public TacticalMapGUIDrawer tacticalMapDrawer;
    public RepairPasteFabricatorHudOverlay repairPasteHudOverlay;
    public CriticalIndicatorDrawer criticalIndicatorDrawer;
    public BlockIconUtils iconUtils;


    @Override
    public void onEnable() {
        instance = this;
        ConfigManager.initialize(this);
        initLogger();
        ResourceManager.loadResources(this);
        registerListeners();
        //registerFastListeners();
        registerPackets();
        CommandUpdateManager.initialize();
    }

    @Override
    public byte[] onClassTransform(String className, byte[] byteCode) {
        for(String name : overwriteClasses) if(className.endsWith(name)) return overwriteClass(className, byteCode);
        return super.onClassTransform(className, byteCode);
    }

    @Override
    public void onBlockConfigLoad(BlockConfig blockConfig) {
        //Systems
        ElementManager.addBlock(new RepairPasteFabricator());
        //ElementManager.addBlock(new AIRemoteController());

        ElementManager.initialize();
    }

    @Override
    public void onUniversalRegistryLoad() {
        UniversalRegistry.registerURV(UniversalRegistry.RegistryType.PLAYER_USABLE_ID, getSkeleton(), RemoteControlAddOn.UID);
    }

    private void initLogger() {
        String logFolderPath = DataUtils.getWorldDataPath() + "/logs";
        File logsFolder = new File(logFolderPath);
        if(!logsFolder.exists()) logsFolder.mkdirs();
        else {
            if(logsFolder.listFiles() != null && logsFolder.listFiles().length > 0) {
                File[] logFiles = new File[logsFolder.listFiles().length];
                int j = logFiles.length - 1;
                for(int i = 0; i < logFiles.length && j >= 0; i++) {
                    logFiles[i] = logsFolder.listFiles()[j];
                    j--;
                }

                for(File logFile : logFiles) {
                    String fileName = logFile.getName().replace(".txt", "");
                    int logNumber = Integer.parseInt(fileName.substring(fileName.indexOf("log") + 3)) + 1;
                    String newName = logFolderPath + "/log" + logNumber + ".txt";
                    if(logNumber < ConfigManager.getMainConfig().getInt("max-world-logs") - 1) logFile.renameTo(new File(newName));
                    else logFile.delete();
                }
            }
        }
        try {
            File newLogFile = new File(logFolderPath + "/log0.txt");
            if(newLogFile.exists()) newLogFile.delete();
            newLogFile.createNewFile();
            log = Logger.getLogger(newLogFile.getPath());
            FileHandler handler = new FileHandler(newLogFile.getPath());
            log.addHandler(handler);
            SimpleFormatter formatter = new SimpleFormatter();
            handler.setFormatter(formatter);
        } catch(IOException exception) {
            exception.printStackTrace();
        }
    }

    private void registerListeners() {
        StarLoader.registerListener(RegisterWorldDrawersEvent.class, new Listener<RegisterWorldDrawersEvent>() {
            @Override
            public void onEvent(RegisterWorldDrawersEvent event) {
                if(tacticalMapDrawer == null) {
                    event.getModDrawables().add(tacticalMapDrawer = new TacticalMapGUIDrawer());
                    mapKey = ConfigManager.getMainConfig().getString("tactical-map-toggle-key").charAt(0);
                }

                if(criticalIndicatorDrawer == null) event.getModDrawables().add(criticalIndicatorDrawer = new CriticalIndicatorDrawer());

                if(iconUtils == null) event.getModDrawables().add(iconUtils = new BlockIconUtils());
            }
        }, this);

        StarLoader.registerListener(RegisterAddonsEvent.class, new Listener<RegisterAddonsEvent>() {
            @Override
            public void onEvent(RegisterAddonsEvent event) {
                //event.addModule(new RemoteControlAddOn(event.getContainer()));
            }
        }, this);

        StarLoader.registerListener(ManagerContainerRegisterEvent.class, new Listener<ManagerContainerRegisterEvent>() {
            @Override
            public void onEvent(ManagerContainerRegisterEvent event) {
                event.addModMCModule(new RepairPasteFabricatorSystem(event.getSegmentController(), event.getContainer()));
                //event.addModMCModule(new RemoteControlModule(event.getSegmentController(), event.getContainer()));
            }
        }, this);

        StarLoader.registerListener(HudCreateEvent.class, new Listener<HudCreateEvent>() {
            @Override
            public void onEvent(HudCreateEvent event) {
                if(repairPasteHudOverlay == null) {
                    (repairPasteHudOverlay = new RepairPasteFabricatorHudOverlay(event.getInputState())).onInit();
                    event.addElement(repairPasteHudOverlay);
                }
            }
        }, this);

        StarLoader.registerListener(KeyPressEvent.class, new Listener<KeyPressEvent>() {
            @Override
            public void onEvent(KeyPressEvent event) {
                if(!GameClient.getClientState().getController().isChatActive() && GameClient.getClientState().getController().getPlayerInputs().isEmpty()) {
                    if(event.getChar() == mapKey && tacticalMapDrawer != null) tacticalMapDrawer.toggleDraw();
                    if(tacticalMapDrawer != null && tacticalMapDrawer.toggleDraw) {
                        if(KeyboardMappings.getEventKeyState(event.getRawEvent(), GameClient.getClientState())) {
                            if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
                                Controller.setCamera(tacticalMapDrawer.getDefaultCamera());
                                tacticalMapDrawer.controlManager.onSwitch(false);
                            }
                        }
                    }
                }
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
                                            FleetGUIManager.orderFleets(FleetCommandTypes.CALL_TO_CARRIER);
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

                                FleetGUIManager.getPanel().fleetActionsList.addButton(0, 12, "ACTIVATE TURRETS", GUIHorizontalArea.HButtonColor.YELLOW, new GUICallback() {
                                    @Override
                                    public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                                        if(mouseEvent.pressedLeftMouse()) {
                                            GameClient.getClientState().getController().queueUIAudio("0022_menu_ui - enter");
                                            FleetGUIManager.orderTurretsActivate();
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

                                FleetGUIManager.getPanel().fleetActionsList.addButton(0, 13, "DEACTIVATE TURRETS", GUIHorizontalArea.HButtonColor.YELLOW, new GUICallback() {
                                    @Override
                                    public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                                        if(mouseEvent.pressedLeftMouse()) {
                                            GameClient.getClientState().getController().queueUIAudio("0022_menu_ui - enter");
                                            FleetGUIManager.orderTurretsDeactivate();
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

                                FleetGUIManager.getPanel().fleetActionsList.addButton(0, 14, "TOGGLE JAMMING", GUIHorizontalArea.HButtonColor.YELLOW, new GUICallback() {
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

                                FleetGUIManager.getPanel().fleetActionsList.addButton(0, 15, "TOGGLE CLOAK", GUIHorizontalArea.HButtonColor.YELLOW, new GUICallback() {
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
                                FleetGUIManager.getPanel().fleetActionsList.moveToMouse(null);
                                FleetGUIManager.getPanel().fleetActionsList.draw();
                            } else FleetGUIManager.getPanel().updateFleetList();
                        }
                    }
                } catch(Exception exception) {
                    //LogManager.logWarning("Encountered an exception while trying to add/remove fleets from the map fleet selection", exception);
                }
            }
        }, this);

        /*
        StarLoader.registerListener(ShieldHitEvent.class, new Listener<ShieldHitEvent>() {
            @Override
            public void onEvent(ShieldHitEvent event) {
                SegmentController damager = (SegmentController) event.getShieldHit().damager;
                SegmentController damaged = event.getHitController();
                float damageBonus = EntityUtils.calculateFlankingBonus(damaged, damager);
                if(damageBonus > 0) {
                    double damage = event.getShieldHit().getDamage() * damageBonus;
                    event.setDamage(damage);
                    if(GameClient.getClientState() != null) {
                        if(PlayerUtils.getCurrentControl(GameClient.getClientPlayerState()).equals(damager)) {
                            AudioUtils.clientPlaySound("0022_spaceship user - laser impact with shields enabled synthetic hit small", 1, 1);
                        } else if(PlayerUtils.getCurrentControl(GameClient.getClientPlayerState()).equals(damaged)) {
                            GameClient.getClientState().getWorldDrawer().getGuiDrawer().getHud().notifyEffectHit(damaged, EffectElementManager.OffensiveEffects.PIERCING);
                            AudioUtils.clientPlaySound("0022_spaceship user - laser impact with shields disabled metallic hit small", 1, 1);
                        }
                    }
                }
            }
        }, this);
         */

        StarLoader.registerListener(SegmentPieceAddByMetadataEvent.class, new Listener<SegmentPieceAddByMetadataEvent>() {
            @Override
            public void onEvent(SegmentPieceAddByMetadataEvent event) {
                SegmentPiece segmentPiece = event.getAsSegmentPiece(new SegmentPiece());
                SegmentController segmentController = segmentPiece.getSegmentController();
                if(segmentController instanceof ManagedSegmentController<?>) {
                    ManagedSegmentController<?> entity = (ManagedSegmentController<?>) segmentController;
                    if(event.getType() == ElementManager.getBlock("Repair Paste Fabricator").getId()) {
                        ModManagerContainerModule module = entity.getManagerContainer().getModMCModule(event.getType());
                        if(module instanceof RepairPasteFabricatorSystem) {
                            RepairPasteFabricatorSystem repairPasteSystem = (RepairPasteFabricatorSystem) module;
                            repairPasteSystem.handlePlace(segmentPiece.getAbsoluteIndex(), segmentPiece.getOrientation());
                        }
                    }
                }
            }
        }, this);

        StarLoader.registerListener(SegmentPieceAddEvent.class, new Listener<SegmentPieceAddEvent>() {
            @Override
            public void onEvent(SegmentPieceAddEvent event) {
                SegmentPiece segmentPiece = event.getSegmentController().getSegmentBuffer().getPointUnsave(event.getAbsIndex());
                SegmentController segmentController = event.getSegmentController();
                if(segmentController instanceof ManagedSegmentController<?>) {
                    ManagedSegmentController<?> entity = (ManagedSegmentController<?>) segmentController;
                    if(event.getNewType() == ElementManager.getBlock("Repair Paste Fabricator").getId()) {
                        ModManagerContainerModule module = entity.getManagerContainer().getModMCModule(event.getNewType());
                        if(module instanceof RepairPasteFabricatorSystem) {
                            RepairPasteFabricatorSystem repairPasteSystem = (RepairPasteFabricatorSystem) module;
                            repairPasteSystem.handlePlace(segmentPiece.getAbsoluteIndex(), segmentPiece.getOrientation());
                        }
                    }
                }
            }
        }, this);

        StarLoader.registerListener(SegmentPieceRemoveEvent.class, new Listener<SegmentPieceRemoveEvent>() {
            @Override
            public void onEvent(SegmentPieceRemoveEvent event) {
                SegmentController segmentController = event.getSegment().getSegmentController();
                if(segmentController instanceof ManagedSegmentController<?>) {
                    ManagedSegmentController<?> entity = (ManagedSegmentController<?>) segmentController;
                    if(event.getType() == ElementManager.getBlock("Repair Paste Fabricator").getId()) {
                        ModManagerContainerModule module = entity.getManagerContainer().getModMCModule(event.getType());
                        if(module instanceof RepairPasteFabricatorSystem) {
                            RepairPasteFabricatorSystem repairPasteSystem = (RepairPasteFabricatorSystem) module;
                            repairPasteSystem.handleRemove(ElementCollection.getIndex(event.getX(), event.getY(), event.getZ()));
                        }
                    }
                }
            }
        }, this);

        StarLoader.registerListener(SegmentPieceKillEvent.class, new Listener<SegmentPieceKillEvent>() {
            @Override
            public void onEvent(SegmentPieceKillEvent event) {
                SegmentController segmentController = event.getController();
                if(segmentController instanceof ManagedSegmentController<?>) {
                    ManagedSegmentController<?> entity = (ManagedSegmentController<?>) segmentController;
                    if(event.getPiece().getType() == ElementManager.getBlock("Repair Paste Fabricator").getId()) {
                        ModManagerContainerModule module = entity.getManagerContainer().getModMCModule(event.getPiece().getType());
                        if(module instanceof RepairPasteFabricatorSystem) {
                            RepairPasteFabricatorSystem repairPasteSystem = (RepairPasteFabricatorSystem) module;
                            repairPasteSystem.handleRemove(event.getPiece().getAbsoluteIndex());
                        }
                    }
                }
            }
        }, this);

        /*
        StarLoader.registerListener(SegmentHitByProjectileEvent.class, new Listener<SegmentHitByProjectileEvent>() {
            @Override
            public void onEvent(SegmentHitByProjectileEvent event) {
                if(event.getShotHandler().hitSegController instanceof ManagedUsableSegmentController<?>) {
                    SegmentController damager = (SegmentController) event.getDamager();
                    ManagedUsableSegmentController<?> damaged = (ManagedUsableSegmentController<?>) event.getShotHandler().hitSegController;
                    ShieldAddOn shieldAddOn = ((ShieldContainerInterface) damaged.getManagerContainer()).getShieldAddOn();
                    if(shieldAddOn != null && shieldAddOn.getShields() <= 0) {
                        float damageBonus = EntityUtils.calculateFlankingBonus(damaged, damager);
                        if(damageBonus > 0) {
                            event.getShotHandler().initialDamage *= damageBonus;
                            //Todo: Display some sort of visual indicator
                        }
                    }
                }
            }
        }, this);
         */
        log.info("Registered listeners");
    }

    private void registerPackets() {
        //Client
        PacketUtil.registerPacket(SendCommandPacket.class);
        PacketUtil.registerPacket(SendFleetDeploymentDataPacket.class);
        PacketUtil.registerPacket(RequestFleetDeploymentDataPacket.class);

        //Server
        PacketUtil.registerPacket(SendCommandUpdatePacket.class);
        PacketUtil.registerPacket(UpdateFleetDeploymentDataPacket.class);
        log.info("Registered packets");
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
