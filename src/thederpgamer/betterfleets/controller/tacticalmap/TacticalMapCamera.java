package thederpgamer.betterfleets.controller.tacticalmap;

import api.common.GameClient;
import com.bulletphysics.linearmath.Transform;
import org.schema.game.common.controller.SegmentController;
import org.schema.schine.graphicsengine.camera.Camera;
import org.schema.schine.graphicsengine.camera.viewer.PositionableViewer;
import org.schema.schine.graphicsengine.core.Timer;
import thederpgamer.betterfleets.BetterFleets;
import javax.vecmath.Vector3f;

/**
 * <Description>
 *
 * @author TheDerpGamer
 * @since 07/12/2021
 */
public class TacticalMapCamera extends Camera {

    public Transform transform;

    public TacticalMapCamera() {
        super(GameClient.getClientState(), new PositionableViewer());
    }

    @Override
    public void reset() {
        super.reset();
        if(GameClient.getCurrentControl() != null && GameClient.getCurrentControl() instanceof SegmentController) {
            Transform defaultTransform = new Transform();
            if(transform == null) transform = (((SegmentController) GameClient.getCurrentControl()).getWorldTransform());
            defaultTransform.set(transform);
            defaultTransform.origin.add(new Vector3f(-BetterFleets.getInstance().tacticalMapDrawer.sectorSize, BetterFleets.getInstance().tacticalMapDrawer.sectorSize, -BetterFleets.getInstance().tacticalMapDrawer.sectorSize));
            setLookAlgorithm(new TacticalCameraLook(this, transform));
            getLookAlgorithm().lookTo(((SegmentController) GameClient.getCurrentControl()).getWorldTransform());
        }
    }

    @Override
    public void update(Timer timer, boolean server) {
        alwaysAllowWheelZoom = true;
        if(GameClient.getCurrentControl() != null && GameClient.getCurrentControl() instanceof SegmentController) {
            if(transform == null) transform = (((SegmentController) GameClient.getCurrentControl()).getWorldTransform());
            getLookAlgorithm().mouseRotate(server, (float)this.mouseState.dx / 1000.0F, (float)this.mouseState.dy / 1000.0F, 0.0F, this.getMouseSensibilityX(), this.getMouseSensibilityY(), 0.0F);
        }
    }
}
