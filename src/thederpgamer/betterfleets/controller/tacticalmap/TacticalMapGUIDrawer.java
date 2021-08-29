package thederpgamer.betterfleets.controller.tacticalmap;

import api.common.GameClient;
import api.common.GameServer;
import api.utils.draw.ModWorldDrawer;
import org.lwjgl.opengl.GL11;
import org.schema.common.util.ByteUtil;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.effects.Indication;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.data.ServerConfig;
import org.schema.schine.graphicsengine.camera.Camera;
import org.schema.schine.graphicsengine.core.*;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import thederpgamer.betterfleets.gui.element.sprite.TacticalMapFleetIndicator;

import javax.vecmath.Vector3f;
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
    private final ConcurrentHashMap<Long, TacticalMapFleetIndicator> drawMap;

    private float updateTimer;

    public TacticalMapGUIDrawer() {
        toggleDraw = false;
        initialized = false;
        sectorSize = (int) ServerConfig.SECTOR_SIZE.getCurrentState();
        maxDrawDistance = sectorSize * 4.0f;
        labelOffset = new Vector3f(0.0f, -20.0f, 0.0f);
        drawMap = new ConcurrentHashMap<>();
    }

    public void toggleDraw() {
        if(!initialized) onInit();
        if(!(GameClient.getClientState().isInAnyStructureBuildMode() || GameClient.getClientState().isInFlightMode()) || GameClient.getClientState().getWorldDrawer().getGameMapDrawer().isMapActive()) {
            toggleDraw = false;
        } else toggleDraw = !toggleDraw;

        if(toggleDraw) {
            Controller.setCamera(camera);
            controlManager.onSwitch(true);
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
        camera.alwaysAllowWheelZoom = true;
        initialized = true;
    }

    @Override
    public void draw() {
        if(toggleDraw && Controller.getCamera() instanceof TacticalMapCamera) {
            GlUtil.glColor4fForced(1,1,1,1);
            GlUtil.glEnable(GL11.GL_BLEND);
            GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            drawGrid(-sectorSize, sectorSize);
            drawIndicators();
        } else cleanUp();
    }

    @Override
    public void update(Timer timer) {
        if(!toggleDraw || !(Controller.getCamera() instanceof TacticalMapCamera)) return;
        time += timer.getDelta();

        updateTimer -= timer.getDelta();
        if(updateTimer <= 0) updateIndicators();

        controlManager.update(timer);
        SegmentController currentEntity = getCurrentEntity();
        if(currentEntity != null) {
            Sector sector = GameServer.getUniverse().getSector(currentEntity.getSectorId());
            for(SimpleTransformableSendableObject<?> entity : sector.getEntities()) {
                if(entity instanceof SegmentController && !((SegmentController) entity).isCloakedFor(currentEntity) && !((SegmentController) entity).isJammingFor(currentEntity)) {
                    if(((SegmentController) entity).isInFleet()) {
                        long dbid = ((SegmentController) entity).getFleet().dbid;
                        if(!drawMap.containsKey(dbid)) drawMap.put(dbid, new TacticalMapFleetIndicator(((SegmentController) entity).getFleet()));
                        if(drawMap.get(dbid).getDistance() > maxDrawDistance) drawMap.remove(dbid);
                    }
                }
            }
        }
    }


    @Override
    public void cleanUp() {

    }

    @Override
    public boolean isInvisible() {
        return false;
    }

    private void updateIndicators() {
        updateTimer = 1000;
        for(Map.Entry<Long, TacticalMapFleetIndicator> entry : drawMap.entrySet()) {
            TacticalMapFleetIndicator indicator = entry.getValue();
            if(indicator.getDistance() < maxDrawDistance && indicator.getFleet() != null && GameServer.getServerState().getFleetManager().getByFleetDbId(indicator.getFleet().dbid) != null) indicator.updateStats();
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
        float end = start + (1f / 3f) * size;
        float lineAlpha = 0;
        float lineAlphaB = 0;
        for (float i = 0; i < 3; i++) {
            lineAlphaB = 1;
            lineAlpha = 1;

            if (i == 0) {
                lineAlpha = 0;
                lineAlphaB = 0.6f;
            } else if (i == 2) {
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
            if(indicator.getDistance() < maxDrawDistance && indicator.getFleet() != null && GameServer.getServerState().getFleetManager().getByFleetDbId(indicator.getFleet().dbid) != null && !indicator.getFleet().isEmpty()) {
                Indication indication = indicator.getIndication(indicator.getSystem());
                indicator.drawSprite(camera, indication.getCurrentTransform());
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
