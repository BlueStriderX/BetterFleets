package thederpgamer.betterfleets.listener;

import api.listener.fastevents.DamageBeamHitListener;
import org.schema.game.common.controller.BeamHandlerContainer;
import org.schema.game.common.controller.ManagedUsableSegmentController;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.damage.beam.DamageBeamHitHandlerSegmentController;
import org.schema.game.common.controller.elements.BeamState;
import org.schema.game.common.controller.elements.ShieldAddOn;
import org.schema.game.common.controller.elements.ShieldContainerInterface;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.world.Segment;
import org.schema.schine.graphicsengine.core.Timer;
import thederpgamer.betterfleets.utils.EntityUtils;

import javax.vecmath.Vector3f;
import java.util.Collection;

/**
 * <Description>
 *
 * @author TheDerpGamer
 * @version 1.0 - [03/08/2022]
 */
public class DamageBeamListener implements DamageBeamHitListener {

	@Override
	public void handle(BeamState beamState, int i, BeamHandlerContainer<?> beamHandlerContainer, SegmentPiece segmentPiece, Vector3f vector3f, Vector3f vector3f1, Timer timer, Collection<Segment> collection, DamageBeamHitHandlerSegmentController damageBeamHitHandlerSegmentController) {
		SegmentController damager = (SegmentController) beamHandlerContainer;
		SegmentController damaged = segmentPiece.getSegmentController();
		ShieldAddOn shieldAddOn = ((ShieldContainerInterface) ((ManagedUsableSegmentController<?>) damaged).getManagerContainer()).getShieldAddOn();
		if(shieldAddOn != null && shieldAddOn.getShields() <= 0 && !damaged.railController.isDockedAndExecuted()) {
			float damageBonus = EntityUtils.calculateFlankingBonus(damaged, damager);
			if(damageBonus > 0) {
				beamState.setPower(beamState.getPower() * damageBonus);
				//Todo: Display some sort of visual indicator
			}
		}
	}
}
