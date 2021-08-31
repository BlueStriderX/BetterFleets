package thederpgamer.betterfleets.gui.element.sprite;

import api.common.GameClient;
import api.common.GameCommon;
import api.utils.game.SegmentControllerUtils;
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
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.SpaceStation;
import org.schema.game.common.data.SimpleGameObject;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.common.data.player.faction.FactionRelation;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.common.data.world.VoidSystem;
import org.schema.game.server.ai.SegmentControllerAIEntity;
import org.schema.game.server.ai.program.common.TargetProgram;
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
import thederpgamer.betterfleets.controller.tacticalmap.TacticalMapGUIDrawer;
import thederpgamer.betterfleets.utils.LogManager;
import thederpgamer.betterfleets.utils.ResourceManager;
import thederpgamer.betterfleets.utils.SectorUtils;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

/**
 * Sprite Indicator for entities in the Tactical Map GUI.
 *
 * @author TheDerpGamer
 * @since 08/24/2021
 */
public class TacticalMapEntityIndicator extends AbstractMapEntry implements SelectableSprite {

    private static final Vector3f labelOffset = new Vector3f(-1800.0f, -670.0f, 490.0f);

    private final SegmentController entity;
    private Indication indication;
    private final Vector3f pos = new Vector3f();
    private boolean drawIndication;
    private float selectDepth;

    private EntityIndicatorSubSprite indicatorSprite;
    public GUIOverlay sprite;
    public GUITextOverlay labelOverlay;

    public final Transform entityTransform = new Transform();
    public boolean selected = false;
    public boolean needsUpdate = true;

    public TacticalMapEntityIndicator(SegmentController entity) {
        this.entity = entity;
    }

    public SegmentController getEntity() {
        return entity;
    }

    private HudIndicatorOverlay getHudOverlay() {
        return GameClient.getClientState().getWorldDrawer().getGuiDrawer().getHud().getIndicator();
    }

    private Sprite getDisplaySprite(SegmentController playerEntity) {
        int entityFaction = entity.getFactionId();
        int playerFactionId = playerEntity.getFactionId();
        FactionRelation.RType relation = Objects.requireNonNull(GameCommon.getGameState()).getFactionManager().getRelation(entityFaction, playerFactionId);
        if((entity.isJammingFor(playerEntity) || entity.isCloakedFor(playerEntity)) && !relation.equals(FactionRelation.RType.FRIEND)) return ResourceManager.getSprite("entity-indicator-unknown");
        else {
            try {
                String prefix = "entity-indicator-";
                String suffix = (selected) ? relation.getName().toLowerCase().trim() + "-selected" : "-" + relation.getName().toLowerCase().trim();
                String spriteName = entity.getType().getName().toLowerCase().replace(" ", "-").replace("_", "-").trim();
                if(entity.getType().equals(SimpleTransformableSendableObject.EntityType.SPACE_STATION)) {
                    if(entity.getFactionId() == FactionManager.PIRATES_ID) {
                        if(!selected) return ResourceManager.getSprite("entity-indicator-pirate-station");
                        else throw new IllegalStateException("Pirate stations should never be selectable!");
                    } else if(entity.getFactionId() == FactionManager.TRAIDING_GUILD_ID) {
                        if(!selected) return ResourceManager.getSprite("entity-indicator-trade-station");
                        else throw new IllegalStateException("Trade stations should never be selectable!");
                    }
                } else if(entity.getType().equals(SimpleTransformableSendableObject.EntityType.SHOP)) {
                    if(!selected) return ResourceManager.getSprite("entity-indicator-shop");
                    else throw new IllegalStateException("Shops should never be selectable!");
                }
                Sprite sprite = ResourceManager.getSprite(prefix + spriteName + suffix);
                if(sprite == null) {
                    LogManager.logWarning("Failed to find correct map sprite for entity \"" + entity.getName() + "\". Reverting to default sprite...", null);
                    return ResourceManager.getSprite("entity-indicator-unknown");
                } else return sprite;
            } catch(Exception exception) {
                LogManager.logException("Encountered an exception while trying to pick a map sprite for entity \"" + entity.getName() + "\"", exception);
                return ResourceManager.getSprite("entity-indicator-unknown");
            }
        }
    }

    public void drawSprite(Transform transform, SegmentController playerEntity) {
        if(indicatorSprite == null || sprite == null || needsUpdate) {
            indicatorSprite = new EntityIndicatorSubSprite(getEntity());
            needsUpdate = false;
            sprite = new GUIOverlay(getDisplaySprite(playerEntity), GameClient.getClientState());
            sprite.onInit();
            sprite.getSprite().setBillboard(true);
            sprite.getSprite().setDepthTest(false);
            sprite.getSprite().setBlend(true);
            sprite.getSprite().setFlip(true);
            sprite.getSprite().setScale(new Vector3f(1.15f, 1.15f, 1.15f));
            sprite.setUserPointer(entity.getId());
        }
        transform.basis.set(getCamera().lookAt(false).basis);
        transform.basis.invert();


        if(entity.isCloakedFor(playerEntity) && entityTransform.equals(entity.getWorldTransform())) entityTransform.set(randomizeTransform(entity.getWorldTransform()));
        else entityTransform.set(entity.getWorldTransform());
        if(!getSector().equals(Objects.requireNonNull(getCurrentEntity()).getSector(new Vector3i()))) SectorUtils.transformToSector(entityTransform, getCurrentEntity().getSector(new Vector3i()), getSector());
        sprite.getTransform().set(entityTransform);
        sprite.getTransform().basis.set(transform.basis);
        sprite.draw();
    }

    public void drawLabel(Transform transform, SegmentController playerEntity) {
        if(labelOverlay == null) {
            (labelOverlay = new GUITextOverlay(32, 32, FontLibrary.FontSize.MEDIUM.getFont(), getHudOverlay().getState())).onInit();
            labelOverlay.getScale().y *= -1;
        }
        transform.basis.set(getCamera().lookAt(false).basis);
        transform.basis.invert();

        labelOverlay.setTextSimple(getEntityDisplay(playerEntity));
        labelOverlay.updateTextSize();
        if(entity.isCloakedFor(playerEntity) && entityTransform.equals(entity.getWorldTransform())) entityTransform.set(randomizeTransform(entity.getWorldTransform()));
        else entityTransform.set(entity.getWorldTransform());
        if(!getSector().equals(Objects.requireNonNull(getCurrentEntity()).getSector(new Vector3i()))) SectorUtils.transformToSector(entityTransform, getCurrentEntity().getSector(new Vector3i()), getSector());
        labelOverlay.getTransform().set(entityTransform);
        labelOverlay.getTransform().basis.set(transform.basis);

        Vector3f upVector = GlUtil.getUpVector(new Vector3f(), labelOverlay.getTransform());
        upVector.scale((labelOverlay.getText().size() * 10) + 20.0f);
        labelOverlay.getTransform().origin.add(upVector);

        Vector3f rightVector = GlUtil.getRightVector(new Vector3f(), labelOverlay.getTransform());
        rightVector.scale(25.0f);
        labelOverlay.getTransform().origin.add(rightVector);

        labelOverlay.draw();
    }

    private SegmentController getCurrentTarget() {
        SegmentControllerAIEntity<?> aiEntity = getAIEntity();
        if(aiEntity != null) {
            SimpleGameObject obj = ((TargetProgram<?>) aiEntity.getCurrentProgram()).getTarget();
            if(obj instanceof SegmentController) return (SegmentController) obj;
        }
        return null;
    }

    public void drawTargetingPath(Camera camera, float speed) {
        SegmentController currentTarget = getCurrentTarget();
        if(currentTarget != null) {
            Vector3f start = new Vector3f(entityTransform.origin);
            Vector3f end = currentTarget.getWorldTransform().origin;
            if(end.length() != 0 && Math.abs(Vector3fTools.sub(start, end).length()) > 1.0f) {
                startDrawDottedLine(camera);
                drawDottedLine(start, end, getPathColor(), speed);
                endDrawDottedLine();
            }
        }
    }

    public void drawMovementPath(Camera camera, float speed) {
        Vector3f start = new Vector3f(entityTransform.origin);
        Vector3f end = entity.getLinearVelocity(new Vector3f()); //This doesn't seem right...
        if(end.length() != 0 && Math.abs(Vector3fTools.sub(start, end).length()) > 1.0f) {
            startDrawDottedLine(camera);
            Vector4f pathColor = new Vector4f(Color.CYAN.getColorComponents(new float[4]));
            pathColor.w = 1.0f;
            drawDottedLine(start, end, pathColor, speed);
            endDrawDottedLine();
        }
    }

    private Vector4f getPathColor() {
        SegmentController currentTarget = getCurrentTarget();
        if(currentTarget != null) {
            int entityFaction = entity.getFactionId();
            int targetFaction = currentTarget.getFactionId();
            if(!(entityFaction == -1 && targetFaction == -1)) {
                FactionRelation.RType relation = Objects.requireNonNull(GameCommon.getGameState()).getFactionManager().getRelation(entityFaction, targetFaction);
                return new Vector4f(relation.defaultColor.x, relation.defaultColor.y, relation.defaultColor.z, 1.0f);
            }
        }
        return new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public SegmentControllerAIEntity<?> getAIEntity() {
        switch(entity.getType()) {
            case SHIP: return ((Ship) entity).getAiConfiguration().getAiEntityState();
            case SPACE_STATION: return ((SpaceStation) entity).getAiConfiguration().getAiEntityState();
            default: return null; //Only support Ship or Station AIs
        }
    }

    private Transform randomizeTransform(Transform transform) {
        Transform randomizedTransform = new Transform(transform);
        Random random = new Random();
        Vector3f randomVector = new Vector3f((float) random.nextInt(150), (float) random.nextInt(150), (float) random.nextInt(150));
        randomizedTransform.origin.add(randomVector);
        return randomizedTransform;
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
            indication = new ConstantIndication(transform, getEntityDisplay((SegmentController) GameClient.getCurrentControl()));
        }
        indication.setText(getEntityDisplay((SegmentController) GameClient.getCurrentControl()));
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

    /**
     * Draws a dotted line between the specified points and scales it by the sector size.
     * Use for drawing lines on a sector-wide scale.
     * @param from The point to start at
     * @param to The point to end at
     * @param color The line's color
     * @param speed The speed to move the line at
     * @param sectorSize The world's sector size
     */
    public void drawDottedLine(Vector3f from, Vector3f to, Vector4f color, float speed, float sectorSize) {
        Vector3f fromPx = new Vector3f();
        Vector3f toPx = new Vector3f();
        float sectorSizeHalf = sectorSize * 0.5f;
        fromPx.set((from.x) * sectorSize + sectorSizeHalf, (from.y) * sectorSize + sectorSizeHalf, (from.z) * sectorSize + sectorSizeHalf);
        toPx.set((to.x) * sectorSize + sectorSizeHalf, (to.y) * sectorSize + sectorSizeHalf, (to.z) * sectorSize + sectorSizeHalf);
        if(!fromPx.equals(toPx)) drawDottedLine(fromPx, toPx, color, speed);
    }

    /**
     * Draws a dotted line between the specified points. Use for drawing lines in local scale.
     * @param from The point to start at
     * @param to The point to end at
     * @param color The line's color
     * @param speed The speed to move the line at
     */
    public void drawDottedLine(Vector3f from, Vector3f to, Vector4f color, float speed) {
        Vector3f dir = new Vector3f();
        Vector3f dirN = new Vector3f();

        dir.sub(to, from);
        dirN.set(dir);
        dirN.normalize();

        float len = dir.length();
        Vector3f a = new Vector3f();
        Vector3f b = new Vector3f();
        float dottedSize = Math.min(Math.max(2, len * 0.1f), 40);

        GlUtil.glColor4f(color);
        boolean first = true;
        float f = ((speed % 1.0f) * dottedSize * 2);
        for(; f < len; f += (dottedSize * 2)) {
            a.set(dirN);
            a.scale(f);
            if(first) {
                a.set(0,0,0);
                first = false;
            }
            b.set(dirN);

            if((f + dottedSize) >= len) {
                b.scale(len);
                f = len;
            } else b.scale(f + dottedSize);

            GL11.glVertex3f(from.x + a.x, from.y + a.y,from.z + a.z);
            GL11.glVertex3f(from.x + b.x, from.y + b.y, from.z + b.z);
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

    public String distortString(String s) {
        char[] chars = s.toCharArray();
        Random random = new Random();
        for(int i = 0; i < chars.length; i ++) {
            int r = random.nextInt(2);
            if(r == 0) chars[i] = StringTools.randomString(1).charAt(0);
        }
        return new String(chars);
    }

    private String getEntityDisplay(SegmentController playerEntity) {
        StringBuilder builder = new StringBuilder();
        if(entity.isJammingFor(playerEntity) || entity.isCloakedFor(playerEntity)) builder.append(distortString(entity.getName()));
        else builder.append(entity.getName());
        builder.append("\n");
        ArrayList<PlayerState> attachedPlayers = SegmentControllerUtils.getAttachedPlayers(entity);
        if(!attachedPlayers.isEmpty() && !entity.isJammingFor(playerEntity) && !entity.isCloakedFor(playerEntity)) {
            builder.append(" <").append(attachedPlayers.get(0).getName());
            if(attachedPlayers.size() > 1) builder.append(" + ").append(attachedPlayers.size() - 1).append(" others");
            builder.append(">\n");
        }
        if(entity.getFactionId() > 0) {
            if(entity.isJammingFor(playerEntity) || entity.isCloakedFor(playerEntity)) builder.append("[").append(distortString(entity.getFaction().getName())).append("]\n");
            else builder.append("[").append(entity.getFaction().getName()).append("]\n");
        }
        if(entity.isJammingFor(playerEntity) || entity.isCloakedFor(playerEntity)) builder.append("??? mass\n");
        else builder.append(StringTools.massFormat(entity.getTotalPhysicalMass())).append(" mass\n");
        if(!entity.equals(getCurrentEntity())) {
            if(entity.isJammingFor(playerEntity) || entity.isCloakedFor(playerEntity)) builder.append("???km");
            else builder.append(StringTools.formatDistance(getDistance()));
        }
        return builder.toString().trim();
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
        return sprite.getSprite().getTint();
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
        selected = true;
        needsUpdate = true;
        BetterFleets.getInstance().tacticalMapDrawer.selectedEntities.add(entity);
    }

    @Override
    public void onUnSelect() {
        setDrawIndication(true);
        selected = false;
        needsUpdate = true;
        BetterFleets.getInstance().tacticalMapDrawer.selectedEntities.remove(entity);
    }

    @Override
    public int hashCode() {
        return getEntityId();
    }

    @Override
    public boolean isSelectable() {
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof TacticalMapEntityIndicator) {
            return ((TacticalMapEntityIndicator) obj).getEntityId() == getEntityId();
        }
        return false;
    }

    public Vector3i getSystem() {
        return VoidSystem.getContainingSystem(getSector(), new Vector3i());
    }

    public Vector3i getSector() {
        return entity.getSector(new Vector3i());
    }

    public int getEntityId(){
        return entity.getId();
    }

    public float getDistance() {
        Vector3f currentPos = getCurrentEntityTransform().origin;
        Vector3f entityPos = entity.getWorldTransform().origin;
        return Math.abs(Vector3fTools.distance(currentPos.x, currentPos.y, currentPos.z, entityPos.x, entityPos.y, entityPos.z));
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

    private TacticalMapGUIDrawer getDrawer() {
        return BetterFleets.getInstance().tacticalMapDrawer;
    }
}
