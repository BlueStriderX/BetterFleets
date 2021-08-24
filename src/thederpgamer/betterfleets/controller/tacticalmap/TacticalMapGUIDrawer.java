package thederpgamer.betterfleets.controller.tacticalmap;

import api.common.GameClient;
import api.common.GameServer;
import api.utils.draw.ModWorldDrawer;
import com.bulletphysics.linearmath.Transform;
import org.lwjgl.opengl.GL11;
import org.schema.common.util.ByteUtil;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.effects.Indication;
import org.schema.game.client.view.gui.shiphud.HudIndicatorOverlay;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.data.ServerConfig;
import org.schema.schine.graphicsengine.camera.Camera;
import org.schema.schine.graphicsengine.core.*;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.AbstractSceneNode;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.util.WorldToScreenConverter;
import thederpgamer.betterfleets.gui.element.sprite.TacticalMapFleetIndicator;

import javax.vecmath.Vector3f;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.FloatBuffer;
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

    private boolean initialized;
    private final ConcurrentHashMap<Long, TacticalMapFleetIndicator> drawMap;
    //private final HashMap<SegmentController, Sprite> entityMarkers;
    //private final HashMap<SegmentController, GUITextOverlay[]> entityLabels;

    public TacticalMapGUIDrawer() {
        toggleDraw = false;
        initialized = false;
        //entityMarkers = new HashMap<>();
        //entityLabels = new HashMap<>();
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
            //camera.reset();
            controlManager.onSwitch(true);
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
            //drawMarkers();
            //drawLabels();
        } else cleanUp();
    }

    @Override
    public void update(Timer timer) {
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
                    /*
                    if(!entityMarkers.containsKey(entity)) {
                        Sprite sprite = Controller.getResLoader().getSprite("map-sprites-8x2-c-gui-");
                        sprite.setBillboard(true);
                        sprite.setBlend(true);
                        sprite.setFlip(true);
                        entityMarkers.put(currentEntity, sprite);
                    }

                    if(!entityLabels.containsKey(entity)) {
                        entityLabels.put(currentEntity, new GUITextOverlay[] {
                                new GUITextOverlay(32, 32, FontLibrary.FontSize.MEDIUM.getFont(), getHudOverlay().getState()),
                                new GUITextOverlay(32, 32, FontLibrary.FontSize.MEDIUM.getFont(), getHudOverlay().getState())
                        });
                        entityLabels.get(currentEntity)[0].onInit();
                        entityLabels.get(currentEntity)[0].getScale().y *= -1;
                        entityLabels.get(currentEntity)[1].onInit();
                        entityLabels.get(currentEntity)[1].getScale().y *= -1;
                    }
                    */
                }
            }
            /*
            ArrayList<SegmentController> toRemove = new ArrayList<>();

            for(SegmentController entity : entityMarkers.keySet()) {
                if(!entity.isFullyLoaded() || entity.isJammingFor(currentEntity) || entity.isCloakedFor(currentEntity)) toRemove.add(entity);
            }

            for(SegmentController entity : entityLabels.keySet()) {
                if(!entity.isFullyLoaded() || entity.isJammingFor(currentEntity) || entity.isCloakedFor(currentEntity)) toRemove.add(entity);
            }

            for(SegmentController entity : toRemove) {
                entityMarkers.remove(entity);
                entityLabels.remove(entity);
            }
            */
        }
    }


    @Override
    public void cleanUp() {

    }

    @Override
    public boolean isInvisible() {
        return false;
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
            if(indicator.getDistance() < maxDrawDistance) {
                Indication indication = indicator.getIndication(indicator.getSystem());
                indicator.drawSprite(camera, indication.getCurrentTransform());
                getHudOverlay().drawString(indication, camera, false, maxDrawDistance, getWorldToScreenConverter(), labelOffset);
            } else drawMap.remove(entry.getKey());
        }
    }

    /*
    private void drawMarkers() {
        for(Map.Entry<SegmentController, Sprite> entry : entityMarkers.entrySet()) {
            entry.getValue().setTransform(entry.getKey().getWorldTransform());
            Sprite.draw3D(entry.getValue(), new EntityIndicatorSprite[] {new EntityIndicatorSprite(entry.getKey())}, camera);
        }
    }

    private void drawLabels() {
        if(getCurrentEntity() != null) {
            for(Map.Entry<SegmentController, GUITextOverlay[]> entry : entityLabels.entrySet()) {
                if(entityMarkers.containsKey(entry.getKey())) {
                    GlUtil.glPushMatrix();
                    Vector3f currentPos = getCurrentEntity().getWorldTransform().origin;
                    Vector3f entityPos = entry.getKey().getWorldTransform().origin;
                    float distance = Math.abs(Vector3fTools.distance(currentPos.x, currentPos.y, currentPos.z, entityPos.x, entityPos.y, entityPos.z));
                    if(entry.getKey().getFactionId() != 0) entry.getValue()[0].setTextSimple(entry.getKey().getName() + "[" +  entry.getKey().getFaction().getName() + "]");
                    else entry.getValue()[0].setTextSimple(entry.getKey().getName());
                    entry.getValue()[1].setTextSimple(StringTools.formatDistance(distance));

                    getHudOverlay().drawString();

                    Vector3i sector = entry.getKey().getSector(new Vector3i());
                    Vector3i relativeSector = new Vector3i(sector);
                    Vector3i currentSector = GameClient.getClientPlayerState().getCurrentSector();
                    relativeSector.sub(currentSector);

                    Vector3f playerPos = camera.getWorldTransform().origin;
                    Vector3f camPos = camera.getOffsetPos(new Vector3f());
                    Vector3f labelPos = getLabelPos(entityPos);

                    Transform transform = new Transform();
                    transform.setIdentity();
                    transform.set(getCurrentEntity().getClientTransform());
                    //transform.set(entityMarkers.get(entry.getKey()).getTransform());
                    //transform.origin.sub(new Vector3f(16, 16, 16));
                    //transform.set(getCurrentEntity().getWorldTransform());
                    //Vector3i var2 = this.getAbsolutePos(new Vector3i());
                    //Vector3f var3 = new Vector3f((float)(var2.x - 16), (float)(var2.y - 16), (float)(var2.z - 16));
                    //var1.basis.transform(var3);
                    //var1.origin.add(var3);

                    entry.getValue()[0].setTransform(transform);
                    entry.getValue()[0].getTransform().origin.x -= ((String) entry.getValue()[0].getText().get(0)).length() * 3;
                    entry.getValue()[0].getTransform().origin.y -= 15;

                    entry.getValue()[1].setTransform(transform);
                    entry.getValue()[1].getTransform().origin.x -= ((String) entry.getValue()[1].getText().get(0)).length() * 3;
                    entry.getValue()[1].getTransform().origin.y -= 30;

                    GlUtil.glLoadMatrix(Objects.requireNonNull(getBillboardMatrix(entry.getValue()[0])));
                    entry.getValue()[0].draw();
                    entry.getValue()[1].draw();
                    GlUtil.glPopMatrix();

                }
            }
        } else cleanUp();
    }
    */

    private Transform getShipTransform() {
        if(GameClient.getCurrentControl() != null && GameClient.getCurrentControl() instanceof SegmentController) {
            return ((SegmentController) GameClient.getCurrentControl()).getWorldTransform();
        } else return new Transform();
    }

    private SegmentController getCurrentEntity() {
        if(GameClient.getCurrentControl() != null && GameClient.getCurrentControl() instanceof SegmentController) {
            return (SegmentController) GameClient.getCurrentControl();
        } else return null;
    }

    private HudIndicatorOverlay getHudOverlay() {
        return GameClient.getClientState().getWorldDrawer().getGuiDrawer().getHud().getIndicator();
    }

    private WorldToScreenConverter getWorldToScreenConverter() {
        return GameClient.getClientState().getWorldDrawer().getGameMapDrawer().getWorldToScreenConverter();
    }

    private Vector3f getLabelPos(Vector3f entityPos) {
        return new Vector3f((entityPos.x / sectorSize) * 100f, (entityPos.y / sectorSize) * 100f, (entityPos.z / sectorSize)*100f);
    }

    private FloatBuffer getBillboardMatrix(GUITextOverlay textOverlay) {
        try {
            Method method = ((AbstractSceneNode) textOverlay).getClass().getMethod("getBillboardSphericalBeginMatrix");
            method.setAccessible(true);
            return (FloatBuffer) method.invoke(textOverlay);
        } catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
}
