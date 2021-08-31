package thederpgamer.betterfleets.gui.element.sprite;

import api.common.GameClient;
import api.common.GameCommon;
import org.schema.common.util.linAlg.Vector3fTools;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.player.faction.FactionRelation;
import org.schema.schine.graphicsengine.forms.PositionableSubColorSprite;
import org.schema.schine.graphicsengine.forms.Sprite;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

/**
 * <Description>
 *
 * @author TheDerpGamer
 * @since 08/24/2021
 */
public class EntityIndicatorSubSprite implements PositionableSubColorSprite {

    private final SegmentController entity;

    public EntityIndicatorSubSprite(SegmentController entity) {
        this.entity = entity;
    }

    @Override
    public Vector4f getColor() {
        try {
            FactionRelation.RType rType = GameCommon.getGameState().getFactionManager().getRelation(entity.getFactionId(), getCurrentEntity().getFactionId());
            return new Vector4f(rType.defaultColor.x, rType.defaultColor.y, rType.defaultColor.z, 1.0f);
        } catch (Exception ignored) { }
        return new Vector4f(1, 1, 1, 0);
    }

    @Override
    public float getScale(long l) {
        if(getCurrentEntity() != null) {
            Vector3f currentPos = getCurrentEntity().getWorldTransform().origin;
            Vector3f entityPos = entity.getWorldTransform().origin;
            float distance = Math.abs(Vector3fTools.distance(currentPos.x, currentPos.y, currentPos.z, entityPos.x, entityPos.y, entityPos.z));
            if(distance < 500) return 1.0f;
            else if(distance < 1000) return 0.8f;
            else if(distance < 3000) return 0.6f;
            else if(distance < 5000) return 0.4f;
            else if(distance < 10000) return 0.2f;
        }
        return 0.0f;
    }

    @Override
    public int getSubSprite(Sprite sprite) {
        return entity.getType().mapSprite;
    }

    @Override
    public boolean canDraw() {
        return true;
    }

    @Override
    public Vector3f getPos() {
        return entity.getWorldTransform().origin;
    }

    public float getAlpha() {
        if(getCurrentEntity() != null) {
            Vector3f currentPos = getCurrentEntity().getWorldTransform().origin;
            Vector3f entityPos = entity.getWorldTransform().origin;
            float distance = Math.abs(Vector3fTools.distance(currentPos.x, currentPos.y, currentPos.z, entityPos.x, entityPos.y, entityPos.z));
            if(distance < 500) return 1.0f;
            else if(distance < 1000) return 0.8f;
            else if(distance < 3000) return 0.6f;
            else if(distance < 5000) return 0.4f;
            else if(distance < 10000) return 0.2f;
        }
        return 0f;
    }

    private SegmentController getCurrentEntity() {
        if(GameClient.getCurrentControl() != null && GameClient.getCurrentControl() instanceof SegmentController) {
            return (SegmentController) GameClient.getCurrentControl();
        } else return null;
    }
}