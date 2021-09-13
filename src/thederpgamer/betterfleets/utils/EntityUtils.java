package thederpgamer.betterfleets.utils;

import api.common.GameClient;
import org.schema.common.util.linAlg.Vector3fTools;
import org.schema.game.client.controller.manager.ingame.PlayerInteractionControlManager;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.BoundingBox;

import javax.vecmath.Vector3f;

/**
 * <Description>
 *
 * @author TheDerpGamer
 * @version 1.0 - [09/12/2021]
 */
public class EntityUtils {

    /**
     * Returns the closest entity within the specified range the client is looking at.
     * <p>If the client is currently piloting an entity, will add the distance from the entity's center to it's front.</p>
     * @return The entity the client is looking at, if one can be found.
     */
    public static SimpleTransformableSendableObject<?> getLookingAtWithSizeOffset(boolean respectFilters, float maxDistance, boolean selectClosest) {
        float distanceToFront = 0.0f;
        if(GameClient.getCurrentControl() instanceof SegmentController) {
            SegmentController entity = (SegmentController) GameClient.getCurrentControl();
            BoundingBox boundingBox = entity.getBoundingBox();

            Vector3f forward = GlUtil.getForwardVector(new Vector3f(), entity.getWorldTransform());
            Vector3f entityMax = new Vector3f(boundingBox.max);
            Vector3f entityCenter = boundingBox.getCenter(new Vector3f());

            entity.getWorldTransform().transform(entityMax);
            entity.getWorldTransform().transform(entityCenter);

            float distance;
            if(entityMax.lengthSquared() > entityCenter.lengthSquared()) distance = distance(entityMax, entityCenter);
            else distance = distance(entityCenter, entityMax);

            forward.scale(distance);
            distanceToFront += forward.z;
        }
        return PlayerInteractionControlManager.getLookingAt(GameClient.getClientState(), respectFilters, maxDistance + distanceToFront, false, 0.0f, selectClosest);
    }

    private static float distance(Vector3f vectorA, Vector3f vectorB) {
        return Vector3fTools.length(vectorA.x - vectorB.x, vectorA.y - vectorB.y, vectorA.z - vectorB.z);
    }
}
