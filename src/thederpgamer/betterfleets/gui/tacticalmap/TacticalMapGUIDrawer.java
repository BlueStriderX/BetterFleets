package thederpgamer.betterfleets.gui.tacticalmap;

import api.common.GameClient;
import api.common.GameCommon;
import api.network.packets.PacketUtil;
import api.utils.draw.ModWorldDrawer;
import org.lwjgl.opengl.GL11;
import org.schema.common.util.ByteUtil;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.SegmentDrawer;
import org.schema.game.client.view.effects.Indication;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.player.faction.FactionRelation;
import org.schema.game.server.data.ServerConfig;
import org.schema.schine.graphicsengine.camera.Camera;
import org.schema.schine.graphicsengine.core.*;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationCallback;
import org.schema.schine.graphicsengine.forms.gui.GUIAncor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.input.InputState;
import thederpgamer.betterfleets.gui.element.GUIRightClickButtonPane;
import thederpgamer.betterfleets.gui.element.sprite.TacticalMapEntityIndicator;
import thederpgamer.betterfleets.manager.LogManager;
import thederpgamer.betterfleets.network.client.ClientRequestNearbyEntitiesPacket;

import javax.annotation.Nullable;
import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * World drawer for tactical map GUI.
 *
 * @author TheDerpGamer
 * @since 07/12/2021
 */
public class TacticalMapGUIDrawer extends ModWorldDrawer implements Drawable {

    private SegmentDrawer segmentDrawer;
    public TacticalMapControlManager controlManager;
    public TacticalMapCamera camera;
    public boolean toggleDraw;

    public final int sectorSize;
    public final float maxDrawDistance;
    public final Vector3f labelOffset;

    private boolean initialized;
    private boolean firstTime = true;

    public final ConcurrentHashMap<Integer, TacticalMapEntityIndicator> drawMap;
    public final ConcurrentLinkedQueue<SegmentController> selectedEntities = new ConcurrentLinkedQueue<>();

    private GUIRightClickButtonPane buttonPane;

    private FrameBufferObjects outlinesFBO;

    public boolean drawMovementPaths = false;
    public boolean drawTargetingPaths = true;

    private long updateTimer = 0;

    public TacticalMapGUIDrawer() {
        toggleDraw = false;
        initialized = false;
        sectorSize = (int) ServerConfig.SECTOR_SIZE.getCurrentState();
        maxDrawDistance = sectorSize * 4.0f;
        labelOffset = new Vector3f(0.0f, -20.0f, 0.0f);
        drawMap = new ConcurrentHashMap<>();
        outlinesFBO = new FrameBufferObjects("SELECTED_ENTITY_DRAWER", GLFrame.getWidth(), GLFrame.getHeight());
    }

    public void clearSelected() {
        ArrayList<SegmentController> temp = new ArrayList<>(selectedEntities);
        for(SegmentController i : temp) drawMap.get(i.getId()).onUnSelect();
    }

    public void toggleDraw() {
        if(!initialized) onInit();
        if(!(GameClient.getClientState().getPlayerInputs().isEmpty() || GameClient.getClientState().getController().isChatActive() || GameClient.getClientState().isInAnyStructureBuildMode() || GameClient.getClientState().isInFlightMode()) || GameClient.getClientState().getWorldDrawer().getGameMapDrawer().isMapActive()) {
            toggleDraw = false;
        } else toggleDraw = !toggleDraw;

        if(toggleDraw) {
            Controller.setCamera(camera);
            controlManager.onSwitch(true);
            if(firstTime) {
                camera.reset();
                firstTime = false;
            }
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
        camera.alwaysAllowWheelZoom = false;
        recreateButtonPane(null);
        initialized = true;
    }

    @Override
    public void draw() {
        if(!initialized) onInit();
        if(toggleDraw && Controller.getCamera() instanceof TacticalMapCamera) {
            //GlUtil.glEnable(GL11.GL_BLEND);
            //GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GameClient.getClientPlayerState().getNetworkObject().selectedEntityId.set(-1);
            drawGrid(-sectorSize, sectorSize);
            drawIndicators();
            drawOutlines();
            if(buttonPane.active) {
                GUIElement.enableOrthogonal();
                buttonPane.draw();
                GUIElement.disableOrthogonal();
            }
            //GlUtil.glDisable(GL11.GL_BLEND);
        } else cleanUp();
    }

    @Override
    public void update(Timer timer) {
        if(!toggleDraw || !(Controller.getCamera() instanceof TacticalMapCamera)) return;
        controlManager.update(timer);
        SegmentController currentEntity = getCurrentEntity();
        updateTimer --;
        for(TacticalMapEntityIndicator indicator : drawMap.values()) indicator.update(timer);
        if(updateTimer <= 0) {
            if(currentEntity != null) PacketUtil.sendPacketToServer(new ClientRequestNearbyEntitiesPacket(currentEntity));
            updateTimer = 500;
        }
    }

    @Override
    public void cleanUp() {

    }

    @Override
    public boolean isInvisible() {
        return false;
    }

    public void recreateButtonPane(@Nullable TacticalMapEntityIndicator indicator) {
        GUIAncor buttonPaneAnchor = new GUIAncor(GameClient.getClientState(), 150.0f, 300.0f);
        (buttonPane = new GUIRightClickButtonPane(GameClient.getClientState(), 1, 1, buttonPaneAnchor)).onInit();
        buttonPaneAnchor.attach(buttonPane);
        buttonPane.moveToMouse();

        if(indicator != null) {
            FactionRelation.RType relation = Objects.requireNonNull(GameCommon.getGameState()).getFactionManager().getRelation(Objects.requireNonNull(getCurrentEntity()).getFactionId(), indicator.getEntity().getFactionId());
            int currentFactionId = getCurrentEntity().getFactionId();
            int selectedFactionId = indicator.getEntity().getFactionId();
            int index = 0;

            buttonPane.addRow();
            buttonPane.addButton(0, index, "MOVE TO", GUIHorizontalArea.HButtonColor.PINK, new GUICallback() {
                @Override
                public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                    if(mouseEvent.pressedLeftMouse()) {
                        //Todo: Move to
                        buttonPane.active = false;
                        clearSelected();
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
            index ++;

            if(relation.equals(FactionRelation.RType.FRIEND)) {
                buttonPane.addRow();
                buttonPane.addButton(0, index, "DEFEND", GUIHorizontalArea.HButtonColor.GREEN, new GUICallback() {
                    @Override
                    public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                        if(mouseEvent.pressedLeftMouse()) {
                            //Todo: Defend target
                            buttonPane.active = false;
                            clearSelected();
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
                index ++;

                buttonPane.addRow();
                buttonPane.addButton(0, index, "REPAIR", GUIHorizontalArea.HButtonColor.GREEN, new GUICallback() {
                    @Override
                    public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                        if(mouseEvent.pressedLeftMouse()) {
                            //Todo: Repair target
                            buttonPane.active = false;
                            clearSelected();
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
                index ++;
            } else if(relation.equals(FactionRelation.RType.NEUTRAL)) {

            } else if(relation.equals(FactionRelation.RType.ENEMY)) {
                buttonPane.addRow();
                buttonPane.addButton(0, index, "ATTACK", GUIHorizontalArea.HButtonColor.RED, new GUICallback() {
                    @Override
                    public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                        if(mouseEvent.pressedLeftMouse()) {
                            //Todo: Attack target
                            buttonPane.active = false;
                            clearSelected();
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
                index ++;
            }

            buttonPane.addRow();
            buttonPane.addButton(0, index, "SCAN", GUIHorizontalArea.HButtonColor.ORANGE, new GUICallback() {
                @Override
                public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                    if(mouseEvent.pressedLeftMouse()) {
                        //Todo: Scan target
                        buttonPane.active = false;
                        clearSelected();
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
            index ++;
            buttonPane.moveToMouse();
        } else buttonPane.cleanUp();
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
        for(Map.Entry<Integer, TacticalMapEntityIndicator> entry : drawMap.entrySet()) {
            try {
                TacticalMapEntityIndicator indicator = entry.getValue();
                if(indicator.getDistance() < maxDrawDistance && indicator.getEntity() != null) {
                    Indication indication = indicator.getIndication(indicator.getSystem());
                    indicator.drawSprite(indication.getCurrentTransform());
                    indicator.drawLabel(indication.getCurrentTransform());
                    //if(drawMovementPaths && indicator.selected) indicator.drawMovementPath(camera);
                    if(drawTargetingPaths) indicator.drawTargetingPath(camera);
                    //if(drawTargetingPaths && indicator.selected) indicator.drawTargetingPath(camera);
                } else {
                    indicator.sprite.cleanUp();
                    indicator.labelOverlay.cleanUp();
                    drawMap.remove(entry.getKey());
                }
            } catch(Exception exception) {
                exception.printStackTrace();
                drawMap.remove(entry.getKey());
            }
        }
    }

    private void drawOutlines() {
        if((GameCommon.isClientConnectedToServer() || GameCommon.isOnSinglePlayer()) && GameClient.getClientState() != null) {
            for(Map.Entry<Integer, TacticalMapEntityIndicator> entry : drawMap.entrySet()) {
                try {
                    if(entry.getValue().selected || selectedEntities.contains(entry.getValue().getEntity())) {
                        SegmentController entity = (SegmentController) GameCommon.getGameObject(entry.getValue().getEntityId());
                        outlinesFBO.enable();
                        ShaderLibrary.cubeShader13SimpleWhite.loadWithoutUpdate();
                        GlUtil.updateShaderVector4f(ShaderLibrary.cubeShader13SimpleWhite, "col", entry.getValue().getColor());
                        ShaderLibrary.cubeShader13SimpleWhite.unloadWithoutExit();
                        int drawn = getSegmentDrawer().drawSegmentController(entity, ShaderLibrary.cubeShader13SimpleWhite);
                        outlinesFBO.disable();

                        if(drawn > 0) {
                            outlinesFBO.enable();
                            GlUtil.glDisable(GL11.GL_BLEND);
                            GlUtil.glDisable(GL11.GL_DEPTH_TEST);
                            GlUtil.glEnable(GL11.GL_BLEND);
                            GlUtil.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
                            getSegmentDrawer().drawElementCollectionsFromFrameBuffer(outlinesFBO, 0.5f);
                            GlUtil.glDisable(GL11.GL_BLEND);
                            outlinesFBO.disable();
                        }
                    }
                } catch(Exception exception) {
                    LogManager.logException("Something went wrong while trying to draw entity outlines", exception);
                }
                //if(true) {

                /*
                Vector3f pos = new Vector3f(entry.getValue().entityTransform.origin);
                Vector3f min = entry.getValue().getEntity().getBoundingBox().min;
                Vector3f max = entry.getValue().getEntity().getBoundingBox().max;
                Vector3f half = new Vector3f(SegmentData.SEG_HALF, SegmentData.SEG_HALF, SegmentData.SEG_HALF);
                Transform t = new Transform(entry.getValue().entityTransform);
                Vector3f p = new Vector3f();
                p.set(pos.x, pos.y, pos.z);
                p.x -= SegmentData.SEG_HALF;
                p.y -= SegmentData.SEG_HALF;
                p.z -= SegmentData.SEG_HALF;
                t.basis.transform(p);
                t.origin.add(p);
                Vector4f color = entry.getValue().getColor();
                min.sub(half);
                max.sub(half);
                DebugBoundingBox boundingBox = new DebugBoundingBox(min, max, color.x, color.y, color.z, 0.75f);
                boundingBox.LIFETIME = 200;
                DebugDrawer.boundingBoxes.add(boundingBox);
                DebugDrawer.drawBoundingBoxes();
                 */
            }
        }
    }

    private SegmentDrawer getSegmentDrawer() {
        return GameClient.getClientState().getWorldDrawer().getSegmentDrawer();
    }

    private SegmentController getCurrentEntity() {
        if(GameClient.getCurrentControl() != null && GameClient.getCurrentControl() instanceof SegmentController) {
            return (SegmentController) GameClient.getCurrentControl();
        } else return null;
    }
}