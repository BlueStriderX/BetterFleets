package thederpgamer.betterfleets.gui.element.sprite;

import api.common.GameClient;
import api.common.GameCommon;
import com.bulletphysics.linearmath.Transform;
import org.lwjgl.opengl.GL11;
import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector3fTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.gamemap.entry.AbstractMapEntry;
import org.schema.game.client.view.effects.ConstantIndication;
import org.schema.game.client.view.effects.Indication;
import org.schema.game.client.view.gamemap.GameMapDrawer;
import org.schema.game.client.view.gui.shiphud.HudIndicatorOverlay;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.fleet.FleetMember;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.common.data.world.VoidSystem;
import org.schema.schine.graphicsengine.camera.Camera;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.SelectableSprite;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.graphicsengine.forms.font.FontLibrary;
import org.schema.schine.graphicsengine.forms.gui.GUIOverlay;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import thederpgamer.betterfleets.BetterFleets;
import thederpgamer.betterfleets.utils.SectorUtils;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sprite Indicator for fleets in the Tactical Map GUI.
 *
 * @author TheDerpGamer
 * @since 08/24/2021
 */
public class TacticalMapFleetIndicator extends AbstractMapEntry implements SelectableSprite {

    private static final Vector3f labelOffset = new Vector3f(-1800.0f, -670.0f, 490.0f);

    private final Fleet fleet;
    private Indication indication;
    private final Vector4f color = new Vector4f(0.3f, 0.8f, 0.2f, 0.8f);
    private final Vector3f pos = new Vector3f();
    private boolean drawIndication;
    private float selectDepth;

    private EntityIndicatorSprite indicatorSprite;
    private GUIOverlay sprite;
    private GUITextOverlay labelOverlay;

    private final Transform lastKnownTransform = new Transform();
    private double combinedMass;
    private final ConcurrentHashMap<FleetMember, Double> massMap = new ConcurrentHashMap<>();

    public TacticalMapFleetIndicator(Fleet fleet) {
        this.fleet = fleet;
    }

    public Fleet getFleet() {
        return fleet;
    }

    private HudIndicatorOverlay getHudOverlay() {
        return GameClient.getClientState().getWorldDrawer().getGuiDrawer().getHud().getIndicator();
    }

    public void drawSprite(Transform transform) {
        if(indicatorSprite == null || sprite == null) {
            indicatorSprite = new EntityIndicatorSprite(getFleet().getFlagShip().getLoaded());
            Sprite s = Controller.getResLoader().getSprite("map-sprites-8x2-c-gui-");
            s.setSelectedMultiSprite(SimpleTransformableSendableObject.EntityType.SHIP.mapSprite);
            sprite = new GUIOverlay(s, GameClient.getClientState());
            sprite.onInit();
            sprite.getSprite().setBillboard(true);
            sprite.getSprite().setDepthTest(false);
            sprite.getSprite().setBlend(false);
            sprite.getSprite().setFlip(true);
        }
        transform.basis.set(getCamera().lookAt(false).basis);
        transform.basis.invert();

        if(fleet.getFlagShip().isLoaded()) lastKnownTransform.set(fleet.getFlagShip().getLoaded().getWorldTransform());
        if(!getSector().equals(Objects.requireNonNull(getCurrentEntity()).getSector(new Vector3i()))) SectorUtils.transformToSector(lastKnownTransform, getCurrentEntity().getSector(new Vector3i()), getSector());
        sprite.getSprite().setTint(indicatorSprite.getColor());
        sprite.getTransform().set(lastKnownTransform);
        sprite.getTransform().basis.set(transform.basis);
        sprite.draw();
    }

    public void drawLabel(Transform transform) {
        if(labelOverlay == null) {
            (labelOverlay = new GUITextOverlay(32, 32, FontLibrary.FontSize.MEDIUM.getFont(), getHudOverlay().getState())).onInit();
            labelOverlay.getScale().y *= -1;
        }
        transform.basis.set(getCamera().lookAt(false).basis);
        transform.basis.invert();

        labelOverlay.setTextSimple(fleet.getName() + getFactionName() + " - " + StringTools.formatDistance(getDistance()) + "\nSize: " + fleet.getMembers().size() + "\nMass: " + StringTools.massFormat(combinedMass));
        labelOverlay.updateTextSize();
        if(fleet.getFlagShip().isLoaded()) lastKnownTransform.set(fleet.getFlagShip().getLoaded().getWorldTransform());
        if(!getSector().equals(Objects.requireNonNull(getCurrentEntity()).getSector(new Vector3i()))) SectorUtils.transformToSector(lastKnownTransform, getCurrentEntity().getSector(new Vector3i()), getSector());
        labelOverlay.getTransform().set(lastKnownTransform);
        labelOverlay.getTransform().basis.set(transform.basis);

        Vector3f upVector = GlUtil.getUpVector(new Vector3f(), labelOverlay.getTransform());
        upVector.scale(35.0f);
        labelOverlay.getTransform().origin.add(upVector);

        Vector3f rightVector = GlUtil.getRightVector(new Vector3f(), labelOverlay.getTransform());
        rightVector.scale(25.0f);
        labelOverlay.getTransform().origin.add(rightVector);

        labelOverlay.draw();
    }

    public void drawPath(Camera camera, float time, float sectorSize) {
        if(fleet.getCurrentMoveTarget() != null && ((fleet.getFlagShip().getFactionId() == -1 && GameClient.getClientPlayerState().getName().equals(fleet.getOwner())) || (GameClient.getClientPlayerState().getFactionId() == fleet.getFlagShip().getFactionId() && GameClient.getClientPlayerState().getFactionId() != -1))) {
            startDrawDottedLine(camera);
            if(fleet.getOwner().equals(GameClient.getClientPlayerState().getName().toLowerCase())) drawDottedLine(getSector(), fleet.getCurrentMoveTarget(), new Vector4f(0.1f,1.0f,0.5f,1.0f), time, sectorSize);
            else drawDottedLine(getSector(), fleet.getCurrentMoveTarget(), new Vector4f(0.0f,1.0f,1.0f,1.0f), time, sectorSize);
            endDrawDottedLine();
        }
    }

    @Override
    public void drawPoint(boolean colored, int filter, Vector3i selectedSector) {
        if(colored) {
            float alpha = 1.0f;
            if(!include(filter, selectedSector)) alpha = 0.1f;
            GlUtil.glColor4f(0.9f, 0.1f, 0.1f, alpha);
        }
        GL11.glBegin(GL11.GL_POINTS);
        GL11.glVertex3f(getPos().x, getPos().y, getPos().z);
        GL11.glEnd();
    }

    @Override
    public Indication getIndication(Vector3i system) {
        Vector3f indicatorPos = getPos();
        if(indication == null) {
            Transform transform = new Transform();
            transform.setIdentity();
            indication = new ConstantIndication(transform, fleet.getName() + getFactionName() + "\n" + StringTools.formatDistance(getDistance()));
        }
        indication.setText(fleet.getName() + getFactionName() + "\n" + StringTools.formatDistance(getDistance()));
        indication.getCurrentTransform().origin.set(indicatorPos.x - GameMapDrawer.halfsize, indicatorPos.y - GameMapDrawer.halfsize, indicatorPos.z - GameMapDrawer.halfsize);
        return indication;

    }

    private void startDrawDottedLine(Camera camera) {
        GlUtil.glDisable(GL11.GL_LIGHTING);
        GlUtil.glDisable(GL11.GL_TEXTURE_2D);
        GlUtil.glEnable(GL11.GL_COLOR_MATERIAL);
        GlUtil.glEnable(GL11.GL_BLEND);
        GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GlUtil.glMatrixMode(GL11.GL_PROJECTION);
        GlUtil.glPushMatrix();

        float aspect = (float) GLFrame.getWidth() / (float) GLFrame.getHeight();
        GlUtil.gluPerspective(Controller.projectionMatrix, (Float) EngineSettings.G_FOV.getCurrentState(), aspect, 10, 25000, true);
        GlUtil.glMatrixMode(GL11.GL_MODELVIEW);
        GlUtil.glPushMatrix();

        GlUtil.glLoadIdentity();
        camera.lookAt(true);
        GlUtil.translateModelview(-50.0f, -50.0f, -50.0f);
        GlUtil.glBegin(GL11.GL_LINES);
    }

    private void drawDottedLine(Vector3i from, Vector3i to, Vector4f color, float moving, float sectorSize) {
        Vector3f fromPx = new Vector3f();
        Vector3f toPx = new Vector3f();
        float sectorSizeHalf = sectorSize * 0.5f;

        fromPx.set((from.x) * sectorSize + sectorSizeHalf, (from.y) * sectorSize + sectorSizeHalf, (from.z) * sectorSize + sectorSizeHalf);
        toPx.set((to.x) * sectorSize + sectorSizeHalf, (to.y) * sectorSize + sectorSizeHalf, (to.z) * sectorSize + sectorSizeHalf);
        if(fromPx.equals(toPx)) return;
        Vector3f dir = new Vector3f();
        Vector3f dirN = new Vector3f();

        dir.sub(toPx, fromPx);

        dirN.set(dir);
        dirN.normalize();

        float len = dir.length();
        Vector3f a = new Vector3f();
        Vector3f b = new Vector3f();

        float dotedSize = Math.min(Math.max(2, len * 0.1f), 40);

        GlUtil.glColor4f(color);
        boolean first = true;
        float f = ((moving % 1.0f) * dotedSize * 2);
        for(; f < len; f += (dotedSize * 2)) {
            a.set(dirN);
            a.scale(f);
            if(first) {
                a.set(0,0,0);
                first = false;
            }
            b.set(dirN);

            if((f + dotedSize) >= len) {
                b.scale(len);
                f = len;
            } else b.scale(f + dotedSize);

            GL11.glVertex3f(fromPx.x + a.x, fromPx.y + a.y,fromPx.z + a.z);
            GL11.glVertex3f(fromPx.x + b.x, fromPx.y + b.y, fromPx.z + b.z);
        }
    }

    private void endDrawDottedLine() {
        GlUtil.glEnd();
        GlUtil.glPopMatrix();
        GlUtil.glColor4f(1, 1, 1, 1);

        GlUtil.glMatrixMode(GL11.GL_PROJECTION);
        GlUtil.glPopMatrix();
        GlUtil.glMatrixMode(GL11.GL_MODELVIEW);

    }

    public void updateStats() {
        combinedMass = 0;
        for(FleetMember member : fleet.getMembers()) {
            SegmentController segmentController = member.getLoaded();
            if(segmentController != null) {
                if(massMap.containsKey(member)) massMap.replace(member, (double) segmentController.getTotalPhysicalMass());
                else massMap.put(member, (double) segmentController.getTotalPhysicalMass());
            }
        }

        for(Double mass : massMap.values()) combinedMass += mass;
    }

    @Override
    public int getType() {
        return SimpleTransformableSendableObject.EntityType.SHIP.ordinal();
    }

    @Override
    public void setType(byte type) {

    }

    @Override
    public boolean include(int filter, Vector3i selectedSector) {
        return true;
    }

    @Override
    public Vector4f getColor() {
        return color;
    }

    @Override
    public float getScale(long time) {
        return 0.1f;
    }

    @Override
    public int getSubSprite(Sprite sprite) {
        return SimpleTransformableSendableObject.EntityType.SHIP.mapSprite;
    }

    @Override
    public boolean canDraw() {
        return true;
    }

    @Override
    public Vector3f getPos() {
        pos.set((getSector().x / VoidSystem.SYSTEM_SIZEf) * 100f, (getSector().y / VoidSystem.SYSTEM_SIZEf) * 100f, (getSector().z / VoidSystem.SYSTEM_SIZEf)*100f);
        return pos;
    }

    /**
     * @return the drawIndication
     */
    @Override
    public boolean isDrawIndication() {
        return drawIndication;
    }

    /**
     * @param drawIndication the drawIndication to set
     */
    @Override
    public void setDrawIndication(boolean drawIndication) {
        this.drawIndication = drawIndication;
    }

    @Override
    protected void decodeEntryImpl(DataInputStream stream) throws IOException {
    }

    @Override
    public void encodeEntryImpl(DataOutputStream buffer) throws IOException {

    }

    @Override
    public float getSelectionDepth() {
        return selectDepth;
    }

    @Override
    public void onSelect(float depth) {
        setDrawIndication(true);
        selectDepth = depth;
    }

    @Override
    public void onUnSelect() {
        setDrawIndication(true);
    }

    @Override
    public int hashCode() {
        return (int) fleet.dbid;
    }

    @Override
    public boolean isSelectable() {
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof TacticalMapFleetIndicator) {
            return ((TacticalMapFleetIndicator) obj).getEntityId() == getEntityId();
        }
        return false;
    }

    public Vector3i getSystem() {
        return VoidSystem.getContainingSystem(getSector(), new Vector3i());
    }

    private String getFactionName() {
        try {
            return " [" + GameCommon.getGameState().getFactionManager().getFaction(getFleet().getFlagShip().getFactionId()).getName() + "]";
        } catch(Exception ignored) { }
        return "";
    }

    public Vector3i getSector() {
        return fleet.getFlagShip().getSector();
    }

    public long getEntityId(){
        return fleet.dbid;
    }

    public float getDistance() {
        Vector3f currentPos = getCurrentEntityTransform().origin;
        Vector3f entityPos = getFleetTransform().origin;
        return Math.abs(Vector3fTools.distance(currentPos.x, currentPos.y, currentPos.z, entityPos.x, entityPos.y, entityPos.z));
    }

    private Transform getFleetTransform() {
        try {
            return fleet.getFlagShip().getLoaded().getWorldTransform();
        } catch(Exception ignored) { }
        return new Transform();
    }

    private Transform getCurrentEntityTransform() {
        SegmentController entity = getCurrentEntity();
        if(entity != null) return entity.getWorldTransform();
        else return new Transform();
    }

    private SegmentController getCurrentEntity() {
        if(GameClient.getCurrentControl() != null && GameClient.getCurrentControl() instanceof SegmentController) {
            return (SegmentController) GameClient.getCurrentControl();
        } else return null;
    }

    public static void changeLabelOffset(int x, int y, int z) {
        Vector3f dir = new Vector3f();
        int m = 0;
        if(z != 0) {
            m = z;
            z = 0;
            GlUtil.getForwardVector(dir, getCamera().getWorldTransform());
        }

        if(y != 0) {
            m = y;
            y = 0;
            GlUtil.getUpVector(dir, getCamera().getWorldTransform());
        }

        if(x != 0) {
            m = x;
            x = 0;
            GlUtil.getRightVector(dir, getCamera().getWorldTransform());
        }

        if(Math.abs(dir.x) >= Math.abs(dir.y) && Math.abs(dir.x) >= Math.abs(dir.z)) {
            if(dir.x >= 0) x = m;
            else x = -m;
        } else if(Math.abs(dir.y) >= Math.abs(dir.x) && Math.abs(dir.y) >= Math.abs(dir.z)) {
            if(dir.y >= 0) y = m;
            else y = -m;
        } else if(Math.abs(dir.z) >= Math.abs(dir.y) && Math.abs(dir.z) >= Math.abs(dir.x)) {
            if(dir.z >= 0) z = m;
            else z = -m;
        }
        labelOffset.add(new Vector3f(x, y, z));
    }

    private static Camera getCamera() {
        return BetterFleets.getInstance().tacticalMapDrawer.camera;
    }
}
