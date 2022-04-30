package thederpgamer.betterfleets.listener;

import api.listener.fastevents.CannonProjectileHitListener;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.damage.projectile.ProjectileController;
import org.schema.game.common.controller.damage.projectile.ProjectileHandlerSegmentController;
import org.schema.game.common.controller.damage.projectile.ProjectileParticleContainer;
import org.schema.game.common.data.physics.CubeRayCastResult;

import javax.vecmath.Vector3f;

/**
 * <Description>
 *
 * @author TheDerpGamer
 * @version 1.0 - [03/08/2022]
 */
public class CannonListener implements CannonProjectileHitListener {

	@Override
	public ProjectileController.ProjectileHandleState handle(Damager damager, ProjectileController projectileController, Vector3f vector3f, Vector3f vector3f1, ProjectileParticleContainer projectileParticleContainer, int i, CubeRayCastResult cubeRayCastResult, ProjectileHandlerSegmentController projectileHandlerSegmentController) {
		return null;
	}

	@Override
	public ProjectileController.ProjectileHandleState handleBefore(Damager damager, ProjectileController projectileController, Vector3f vector3f, Vector3f vector3f1, ProjectileParticleContainer projectileParticleContainer, int i, CubeRayCastResult cubeRayCastResult, ProjectileHandlerSegmentController projectileHandlerSegmentController) {
		return null;
	}

	@Override
	public ProjectileController.ProjectileHandleState handleAfterIfNotStopped(Damager damager, ProjectileController projectileController, Vector3f vector3f, Vector3f vector3f1, ProjectileParticleContainer projectileParticleContainer, int i, CubeRayCastResult cubeRayCastResult, ProjectileHandlerSegmentController projectileHandlerSegmentController) {
		return null;
	}

	@Override
	public void handleAfterAlways(Damager damager, ProjectileController projectileController, Vector3f vector3f, Vector3f vector3f1, ProjectileParticleContainer projectileParticleContainer, int i, CubeRayCastResult cubeRayCastResult, ProjectileHandlerSegmentController projectileHandlerSegmentController) {

	}
}
