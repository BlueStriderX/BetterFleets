package thederpgamer.betterfleets.controller.tacticalmap;

import api.common.GameClient;
import api.network.packets.PacketUtil;
import api.utils.draw.ModWorldDrawer;
import org.lwjgl.opengl.GL11;
import org.schema.common.util.ByteUtil;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.effects.Indication;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.fleet.FleetCommandTypes;
import org.schema.game.server.data.ServerConfig;
import org.schema.schine.graphicsengine.camera.Camera;
import org.schema.schine.graphicsengine.core.*;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea;
import org.schema.schine.input.InputState;
import thederpgamer.betterfleets.gui.element.GUIRightClickButtonPane;
import thederpgamer.betterfleets.gui.element.sprite.TacticalMapFleetIndicator;
import thederpgamer.betterfleets.gui.element.tacticalmap.SelectedFleetsPane;
import thederpgamer.betterfleets.network.client.ClientRequestNearbyEntitiesPacket;
import thederpgamer.betterfleets.utils.FleetUtils;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * World drawer for tactical map GUI.
 *
 * @author TheDerpGamer
 * @since 07/12/2021
 */
public class TacticalMapGUIDrawer extends ModWorldDrawer implements Drawable {

    public TacticalMapControlManager controlManager;
    public TacticalMapCamera camera;
    public boolean toggleDraw;

    public final int sectorSize;
    public final float maxDrawDistance;
    public final Vector3f labelOffset;

    private float time;
    private boolean initialized;
    public final ConcurrentHashMap<Long, TacticalMapFleetIndicator> drawMap;

    private float updateTimer;
    private boolean firstTime = true;

    public final ArrayList<Long> selectedFleets = new ArrayList<>();
    private GUIColoredRectangle fleetPanelBackground;
    private SelectedFleetsPane fleetSelectionList;
    private GUIRightClickButtonPane fleetActionsList;

    public TacticalMapGUIDrawer() {
        toggleDraw = false;
        initialized = false;
        sectorSize = (int) ServerConfig.SECTOR_SIZE.getCurrentState();
        maxDrawDistance = sectorSize * 4.0f;
        labelOffset = new Vector3f(0.0f, -20.0f, 0.0f);
        drawMap = new ConcurrentHashMap<>();
    }

    public void clearSelected() {
        ArrayList<Long> temp = new ArrayList<>(selectedFleets);
        for(Long l : temp) drawMap.get(l).onUnSelect();
    }

    public void toggleDraw() {
        if(!initialized) onInit();
        if(!(GameClient.getClientState().isInAnyStructureBuildMode() || GameClient.getClientState().isInFlightMode()) || GameClient.getClientState().getWorldDrawer().getGameMapDrawer().isMapActive()) {
            toggleDraw = false;
        } else toggleDraw = !toggleDraw;

        if(toggleDraw) {
            Controller.setCamera(camera);
            controlManager.onSwitch(true);
            if(firstTime) {
                camera.reset();
                firstTime = false;
            }
            updateIndicators();
        } else {
            Controller.setCamera(getDefaultCamera());
            controlManager.onSwitch(false);
        }
    }

    public Camera getDefaultCamera() {
        if(GameClient.getClientState().isInAnyStructureBuildMode()) return GameClient.getClientState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getInShipControlManager().getShipControlManager().getSegmentBuildController().getShipBuildCamera();
        else if(GameClient.getClientState().isInFlightMode()) return GameClient.getClientState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getInShipControlManager().getShipControlManager().getShipExternalFlightController().shipCamera;
        else return Controller.getCamera();
    }

    @Override
    public void onInit() {
        controlManager = new TacticalMapControlManager(this);
        camera = new TacticalMapCamera();
        camera.reset();
        camera.alwaysAllowWheelZoom = true;

        fleetPanelBackground = new GUIColoredRectangle(GameClient.getClientState(), GLFrame.getWidth() - 150.0f, 230.0f, new Vector4f(0.15f, 0.3f, 0.2f, 0.5f));
        fleetPanelBackground.rounded = 6;
        fleetPanelBackground.orientate(GUIElement.ORIENTATION_RIGHT | GUIElement.ORIENTATION_BOTTOM);

        GUIAncor fleetListAnchor = new GUIAncor(GameClient.getClientState(), (GLFrame.getWidth() / 2.0f) - 20.0f, 180.0f);
        fleetListAnchor.onInit();
        (fleetSelectionList = new SelectedFleetsPane(GameClient.getClientState())).onInit();
        fleetListAnchor.attach(fleetSelectionList);
        GUIScrollablePanel scrollablePanel = new GUIScrollablePanel(fleetListAnchor.getWidth(), fleetListAnchor.getHeight(), fleetListAnchor, GameClient.getClientState());
        scrollablePanel.setContent(fleetListAnchor);
        scrollablePanel.setScrollable(GUIScrollablePanel.SCROLLABLE_VERTICAL);
        scrollablePanel.onInit();
        fleetSelectionList.setScrollPane(scrollablePanel);
        fleetPanelBackground.attach(scrollablePanel);
        fleetSelectionList.setInside(true);
        fleetSelectionList.setPos(fleetPanelBackground.getPos().x, fleetPanelBackground.getPos().y, 0.0f);
        fleetPanelBackground.attach(scrollablePanel);

        GUIAncor fleetActionsAnchor = new GUIAncor(GameClient.getClientState(), (fleetPanelBackground.getWidth() / 2.0f) - 50.0f, 180.0f);
        fleetActionsAnchor.onInit();
        (fleetActionsList = new GUIRightClickButtonPane(GameClient.getClientState(), 2, 7, fleetActionsAnchor)).onInit();
        createActionsPane();
        fleetActionsAnchor.attach(fleetActionsList);
        fleetPanelBackground.attach(fleetActionsAnchor);
        fleetActionsList.setInside(true);
        fleetActionsList.setPos(GLFrame.getWidth() - fleetActionsList.getWidth() - 10.0f, GLFrame.getHeight() - fleetActionsList.getHeight() - 5.0f, 0.0f);

        initialized = true;
    }

    @Override
    public void draw() {
        if(toggleDraw && Controller.getCamera() instanceof TacticalMapCamera) {
            GlUtil.glEnable(GL11.GL_BLEND);
            GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            drawGrid(-sectorSize, sectorSize);
            drawIndicators();

            GlUtil.glDisable(GL11.GL_BLEND);
            GUIElement.enableOrthogonal();
            if(!selectedFleets.isEmpty()) {
                fleetSelectionList.setPos(fleetPanelBackground.getPos().x, fleetPanelBackground.getPos().y, 0.0f);
                fleetActionsList.setPos(GLFrame.getWidth() - fleetActionsList.getWidth() - 10.0f, GLFrame.getHeight() - fleetActionsList.getHeight() - 5.0f, 0.0f);
                fleetActionsList.active = true;

                fleetPanelBackground.draw();
                fleetSelectionList.draw();
                fleetActionsList.draw();
            }
            GUIElement.disableOrthogonal();
        } else cleanUp();
    }

    @Override
    public void update(Timer timer) {
        if(!toggleDraw || !(Controller.getCamera() instanceof TacticalMapCamera)) return;
        time += timer.getDelta();

        fleetSelectionList.update(timer);
        updateTimer -= timer.getDelta();
        if(updateTimer <= 0) updateIndicators();

        controlManager.update(timer);
        SegmentController currentEntity = getCurrentEntity();
        if(currentEntity != null) PacketUtil.sendPacketToServer(new ClientRequestNearbyEntitiesPacket(currentEntity));
    }


    @Override
    public void cleanUp() {
        fleetActionsList.active = false;
    }

    @Override
    public boolean isInvisible() {
        return false;
    }

    private void orderCommand(FleetCommandTypes commandType) {
        ArrayList<Long> temp = new ArrayList<>(selectedFleets);
        for(Long id : temp) {
            if(drawMap.containsKey(id)) {
                if(commandType.args != null && commandType.args.length == 1) drawMap.get(id).getFleet().sendFleetCommand(commandType, drawMap.get(id).getFleet().getFlagShipSector());
                else drawMap.get(id).getFleet().sendFleetCommand(commandType);
                drawMap.get(id).onUnSelect();
            }
        }
    }

    private void orderFormation() {
        for(Long id : selectedFleets) {
            if(drawMap.containsKey(id)) {
                Fleet fleet = drawMap.get(id).getFleet();
                FleetCommandTypes command = FleetUtils.getCurrentCommand(fleet);
                if(command.equals(FleetCommandTypes.SENTRY)) fleet.sendFleetCommand(FleetCommandTypes.SENTRY_FORMATION);
                else if(command.equals(FleetCommandTypes.SENTRY_FORMATION)) fleet.sendFleetCommand(FleetCommandTypes.SENTRY);
                else if(command.equals(FleetCommandTypes.FLEET_IDLE_FORMATION)) fleet.sendFleetCommand(FleetCommandTypes.IDLE);
                else fleet.sendFleetCommand(FleetCommandTypes.FLEET_IDLE_FORMATION);
                drawMap.get(id).onUnSelect();
            }
        }
    }

    private void orderJamming() {
        for(Long id : selectedFleets) {
            if(drawMap.containsKey(id)) {
                Fleet fleet = drawMap.get(id).getFleet();
                FleetCommandTypes command = FleetUtils.getCurrentCommand(fleet);
                if(command.equals(FleetCommandTypes.JAM)) fleet.sendFleetCommand(FleetCommandTypes.UNJAM);
                else fleet.sendFleetCommand(FleetCommandTypes.JAM);
                drawMap.get(id).onUnSelect();
            }
        }
    }

    private void orderCloaking() {
        for(Long id : selectedFleets) {
            if(drawMap.containsKey(id)) {
                Fleet fleet = drawMap.get(id).getFleet();
                FleetCommandTypes command = FleetUtils.getCurrentCommand(fleet);
                if(command.equals(FleetCommandTypes.CLOAK)) fleet.sendFleetCommand(FleetCommandTypes.UNCLOAK);
                else fleet.sendFleetCommand(FleetCommandTypes.CLOAK);
                drawMap.get(id).onUnSelect();
            }
        }
    }

    public void createActionsPane() {
            fleetActionsList.addButton(0, 0, "TOGGLE FORMATION", GUIHorizontalArea.HButtonColor.BLUE, new GUICallback() {
                @Override
                public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                    if(mouseEvent.pressedLeftMouse()) {
                        GameClient.getClientState().getController().queueUIAudio("0022_menu_ui - enter");
                        orderFormation();
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

            fleetActionsList.addButton(0, 1, "IDLE", GUIHorizontalArea.HButtonColor.BLUE, new GUICallback() {
                @Override
                public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                    if(mouseEvent.pressedLeftMouse()) {
                        GameClient.getClientState().getController().queueUIAudio("0022_menu_ui - enter");
                        orderCommand(FleetCommandTypes.IDLE);
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

            fleetActionsList.addButton(0, 2, "RECALL TO CARRIER", GUIHorizontalArea.HButtonColor.PINK, new GUICallback() {
                @Override
                public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                    if(mouseEvent.pressedLeftMouse()) {
                        GameClient.getClientState().getController().queueUIAudio("0022_menu_ui - enter");
                        orderCommand(FleetCommandTypes.CALL_TO_CARRIER);
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

            fleetActionsList.addButton(0, 3, "ATTACK CURRENT SECTOR", GUIHorizontalArea.HButtonColor.RED, new GUICallback() {
                @Override
                public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                    if(mouseEvent.pressedLeftMouse()) {
                        GameClient.getClientState().getController().queueUIAudio("0022_menu_ui - enter");
                        orderCommand(FleetCommandTypes.FLEET_ATTACK);
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

            fleetActionsList.addButton(0, 4, "ARTILLERY", GUIHorizontalArea.HButtonColor.RED, new GUICallback() {
                @Override
                public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                    if(mouseEvent.pressedLeftMouse()) {
                        GameClient.getClientState().getController().queueUIAudio("0022_menu_ui - enter");
                        orderCommand(FleetCommandTypes.ARTILLERY);
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

            fleetActionsList.addButton(0, 5, "DEFEND CURRENT SECTOR", GUIHorizontalArea.HButtonColor.GREEN, new GUICallback() {
                @Override
                public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                    if(mouseEvent.pressedLeftMouse()) {
                        GameClient.getClientState().getController().queueUIAudio("0022_menu_ui - enter");
                        orderCommand(FleetCommandTypes.FLEET_DEFEND);
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

            fleetActionsList.addButton(0, 6, "INTERCEPT", GUIHorizontalArea.HButtonColor.GREEN, new GUICallback() {
                @Override
                public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                    if(mouseEvent.pressedLeftMouse()) {
                        GameClient.getClientState().getController().queueUIAudio("0022_menu_ui - enter");
                        orderCommand(FleetCommandTypes.INTERCEPT);
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

            fleetActionsList.addButton(1, 0, "SENTRY", GUIHorizontalArea.HButtonColor.ORANGE, new GUICallback() {
                @Override
                public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                    if(mouseEvent.pressedLeftMouse()) {
                        GameClient.getClientState().getController().queueUIAudio("0022_menu_ui - enter");
                        orderCommand(FleetCommandTypes.SENTRY);
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

            fleetActionsList.addButton(1, 1, "SUPPORT", GUIHorizontalArea.HButtonColor.ORANGE, new GUICallback() {
                @Override
                public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                    if(mouseEvent.pressedLeftMouse()) {
                        GameClient.getClientState().getController().queueUIAudio("0022_menu_ui - enter");
                        orderCommand(FleetCommandTypes.SUPPORT);
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

            fleetActionsList.addButton(1, 2, "MINE", GUIHorizontalArea.HButtonColor.ORANGE, new GUICallback() {
                @Override
                public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                    if(mouseEvent.pressedLeftMouse()) {
                        GameClient.getClientState().getController().queueUIAudio("0022_menu_ui - enter");
                        orderCommand(FleetCommandTypes.MINE_IN_SECTOR);
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

            fleetActionsList.addButton(1, 3, "ACTIVATE TURRETS", GUIHorizontalArea.HButtonColor.YELLOW, new GUICallback() {
                @Override
                public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                    if(mouseEvent.pressedLeftMouse()) {
                        GameClient.getClientState().getController().queueUIAudio("0022_menu_ui - enter");
                        orderCommand(FleetCommandTypes.ACTIVATE_TURRETS);
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

            fleetActionsList.addButton(1, 4, "DEACTIVATE TURRETS", GUIHorizontalArea.HButtonColor.YELLOW, new GUICallback() {
                @Override
                public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                    if(mouseEvent.pressedLeftMouse()) {
                        GameClient.getClientState().getController().queueUIAudio("0022_menu_ui - enter");
                        orderCommand(FleetCommandTypes.DEACTIVATE_TURRETS);
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

            fleetActionsList.addButton(1, 5, "TOGGLE JAMMING", GUIHorizontalArea.HButtonColor.YELLOW, new GUICallback() {
                @Override
                public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                    if(mouseEvent.pressedLeftMouse()) {
                        GameClient.getClientState().getController().queueUIAudio("0022_menu_ui - enter");
                        orderJamming();
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

            fleetActionsList.addButton(1, 6, "TOGGLE CLOAK", GUIHorizontalArea.HButtonColor.YELLOW, new GUICallback() {
                @Override
                public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                    if(mouseEvent.pressedLeftMouse()) {
                        GameClient.getClientState().getController().queueUIAudio("0022_menu_ui - enter");
                        orderCloaking();
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
    }

    private void updateIndicators() {
        updateTimer = 1000;
        for(Map.Entry<Long, TacticalMapFleetIndicator> entry : drawMap.entrySet()) {
            TacticalMapFleetIndicator indicator = entry.getValue();
            if(indicator.getDistance() < maxDrawDistance && indicator.getFleet() != null && !indicator.getFleet().isEmpty()) indicator.updateStats();
            else drawMap.remove(entry.getKey());
        }
    }

    private void drawGrid(float start, float spacing) {
        GlUtil.glMatrixMode(GL11.GL_PROJECTION);
        GlUtil.glPushMatrix();

        float aspect = (float) GLFrame.getWidth() / (float) GLFrame.getHeight();
        GlUtil.gluPerspective(Controller.projectionMatrix, (Float) EngineSettings.G_FOV.getCurrentState(), aspect, 10, 25000, true);
        GlUtil.glMatrixMode(GL11.GL_MODELVIEW);
        Vector3i selectedPos = new Vector3i();

        selectedPos.x = ByteUtil.modU16(selectedPos.x);
        selectedPos.y = ByteUtil.modU16(selectedPos.y);
        selectedPos.z = ByteUtil.modU16(selectedPos.z);

        GlUtil.glBegin(GL11.GL_LINES);
        float size = spacing * 3;
        float end = (start + (1f / 3f) * size);
        float lineAlpha;
        float lineAlphaB;
        for(float i = 0; i < 3; i ++) {
            lineAlphaB = 1;
            lineAlpha = 1;

            if(i == 0) {
                lineAlpha = 0;
                lineAlphaB = 0.6f;
            } else if(i == 2) {
                lineAlpha = 0.6f;
                lineAlphaB = 0;
            }

            GlUtil.glColor4fForced(1, 1, 1, lineAlpha);
            GL11.glVertex3f(selectedPos.x * spacing, selectedPos.y * spacing, start);
            GlUtil.glColor4fForced(1, 1, 1, lineAlphaB);
            GL11.glVertex3f(selectedPos.x * spacing, selectedPos.y * spacing, end);

            GlUtil.glColor4fForced(1, 1, 1, lineAlpha);
            GL11.glVertex3f(start, selectedPos.y * spacing, selectedPos.z * spacing);
            GlUtil.glColor4fForced(1, 1, 1, lineAlphaB);
            GL11.glVertex3f(end, selectedPos.y * spacing, selectedPos.z * spacing);

            GlUtil.glColor4fForced(1, 1, 1, lineAlpha);
            GL11.glVertex3f(selectedPos.x * spacing, start, selectedPos.z * spacing);
            GlUtil.glColor4fForced(1, 1, 1, lineAlphaB);
            GL11.glVertex3f(selectedPos.x * spacing, end, selectedPos.z * spacing);

            GlUtil.glColor4fForced(1, 1, 1, lineAlpha);
            GL11.glVertex3f(selectedPos.x * spacing, (selectedPos.y + 1) * spacing, start);
            GlUtil.glColor4fForced(1, 1, 1, lineAlphaB);
            GL11.glVertex3f(selectedPos.x * spacing, (selectedPos.y + 1) * spacing, end);

            GlUtil.glColor4fForced(1, 1, 1, lineAlpha);
            GL11.glVertex3f(start, (selectedPos.y) * spacing, (selectedPos.z + 1) * spacing);
            GlUtil.glColor4fForced(1, 1, 1, lineAlphaB);
            GL11.glVertex3f(end, (selectedPos.y) * spacing, (selectedPos.z + 1) * spacing);

            GlUtil.glColor4fForced(1, 1, 1, lineAlpha);
            GL11.glVertex3f((selectedPos.x) * spacing, start, (selectedPos.z + 1) * spacing);
            GlUtil.glColor4fForced(1, 1, 1, lineAlphaB);
            GL11.glVertex3f((selectedPos.x) * spacing, end, (selectedPos.z + 1) * spacing);

            GlUtil.glColor4fForced(1, 1, 1, lineAlpha);
            GL11.glVertex3f((selectedPos.x + 1) * spacing, (selectedPos.y) * spacing, start);
            GlUtil.glColor4fForced(1, 1, 1, lineAlphaB);
            GL11.glVertex3f((selectedPos.x + 1) * spacing, (selectedPos.y) * spacing, end);

            GlUtil.glColor4fForced(1, 1, 1, lineAlpha);
            GL11.glVertex3f(start, (selectedPos.y + 1) * spacing, (selectedPos.z) * spacing);
            GlUtil.glColor4fForced(1, 1, 1, lineAlphaB);
            GL11.glVertex3f(end, (selectedPos.y + 1) * spacing, (selectedPos.z) * spacing);

            GlUtil.glColor4fForced(1, 1, 1, lineAlpha);
            GL11.glVertex3f((selectedPos.x + 1) * spacing, start, (selectedPos.z) * spacing);
            GlUtil.glColor4fForced(1, 1, 1, lineAlphaB);
            GL11.glVertex3f((selectedPos.x + 1) * spacing, end, (selectedPos.z) * spacing);

            GlUtil.glColor4fForced(1, 1, 1, lineAlpha);
            GL11.glVertex3f((selectedPos.x + 1) * spacing, (selectedPos.y + 1) * spacing, start);
            GlUtil.glColor4fForced(1, 1, 1, lineAlphaB);
            GL11.glVertex3f((selectedPos.x + 1) * spacing, (selectedPos.y + 1) * spacing, end);

            GlUtil.glColor4fForced(1, 1, 1, lineAlpha);
            GL11.glVertex3f(start, (selectedPos.y + 1) * spacing, (selectedPos.z + 1) * spacing);
            GlUtil.glColor4fForced(1, 1, 1, lineAlphaB);
            GL11.glVertex3f(end, (selectedPos.y + 1) * spacing, (selectedPos.z + 1) * spacing);

            GlUtil.glColor4fForced(1, 1, 1, lineAlpha);
            GL11.glVertex3f((selectedPos.x + 1) * spacing, start, (selectedPos.z + 1) * spacing);
            GlUtil.glColor4fForced(1, 1, 1, lineAlphaB);
            GL11.glVertex3f((selectedPos.x + 1) * spacing, end, (selectedPos.z + 1) * spacing);

            end += (1f / 3f) * size;
            start += (1f / 3f) * size;
        }
        GlUtil.glEnd();

        GlUtil.glMatrixMode(GL11.GL_PROJECTION);
        GlUtil.glPopMatrix();
        GlUtil.glMatrixMode(GL11.GL_MODELVIEW);
    }

    private void drawIndicators() {
        for(Map.Entry<Long, TacticalMapFleetIndicator> entry : drawMap.entrySet()) {
            TacticalMapFleetIndicator indicator = entry.getValue();
            if(indicator.getDistance() < maxDrawDistance && indicator.getFleet() != null && !indicator.getFleet().isEmpty()) {
                Indication indication = indicator.getIndication(indicator.getSystem());
                indicator.drawSprite(indication.getCurrentTransform());
                indicator.drawLabel(indication.getCurrentTransform());
                indicator.drawPath(camera, time, sectorSize);
            } else drawMap.remove(entry.getKey());
        }
    }

    private SegmentController getCurrentEntity() {
        if(GameClient.getCurrentControl() != null && GameClient.getCurrentControl() instanceof SegmentController) {
            return (SegmentController) GameClient.getCurrentControl();
        } else return null;
    }
}
