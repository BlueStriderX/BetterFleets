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
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.common.data.world.VoidSystem;
import org.schema.schine.graphicsengine.camera.Camera;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.SelectableSprite;
import org.schema.schine.graphicsengine.forms.Sprite;
import thederpgamer.betterfleets.BetterFleets;
import thederpgamer.betterfleets.controller.tacticalmap.TacticalMapGUIDrawer;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Sprite Indicator for fleets in the Tactical Map GUI.
 *
 * @author TheDerpGamer
 * @since 08/24/2021
 */
public class TacticalMapFleetIndicator extends AbstractMapEntry implements SelectableSprite {

    private EntityIndicatorSprite indicatorSprite;
    private Sprite sprite;
    private final Fleet fleet;
    private Indication indication;
    private Vector4f color = new Vector4f(0.3f, 0.8f, 0.2f, 0.8f);
    private Vector3f pos = new Vector3f();
    private boolean drawIndication;
    private float selectDepth;

    public TacticalMapFleetIndicator(Fleet fleet) {
        this.fleet = fleet;
    }

    public Fleet getFleet() {
        return fleet;
    }

    private TacticalMapGUIDrawer getTacticalMapDrawer() {
        return BetterFleets.getInstance().tacticalMapDrawer;
    }

    public void drawSprite(Camera camera, Transform transform) {
        if(indicatorSprite == null || sprite == null) {
            indicatorSprite = new EntityIndicatorSprite(getFleet().getFlagShip().getLoaded());
            sprite = Controller.getResLoader().getSprite("map-sprites-8x2-c-gui-");
            sprite.setBillboard(true);
            sprite.setBlend(true);
            sprite.setFlip(true);
        }
        sprite.setTransform(transform);
        Sprite.draw3D(sprite, new EntityIndicatorSprite[]{indicatorSprite}, camera);
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
        //return getFleet().getFlagShip().mapEntry.getSubSprite(sprite);
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
        //setDrawIndication(false);
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
        if(getFleet().getFlagShip().getFactionId() != -1) {
            return " [" + GameCommon.getGameState().getFactionManager().getFaction(getFleet().getFlagShip().getFactionId()).getName() + "]";
        } else return "";
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
}
