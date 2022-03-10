package thederpgamer.betterfleets.systems.remotecontrol;

import api.listener.events.systems.ReactorRecalibrateEvent;
import api.utils.addon.SimpleAddOn;
import org.schema.game.common.controller.elements.ManagerContainer;
import thederpgamer.betterfleets.BetterFleets;
import thederpgamer.betterfleets.element.ElementManager;

/**
 * <Description>
 *
 * @author TheDerpGamer
 * @version 1.0 - [03/07/2022]
 */
public class RemoteControlAddOn extends SimpleAddOn {

	public static final String UID = "AI_Remote_Controller";

	private boolean playerUsable = false;

	public RemoteControlAddOn(ManagerContainer<?> managerContainer) {
		super(managerContainer, ElementManager.getBlock("AI Remote Controller").getId(), BetterFleets.getInstance(), UID);
		onReactorRecalibrate(null);
	}

	@Override
	public void onReactorRecalibrate(ReactorRecalibrateEvent event) {
		try {

		} catch(Exception ignored) {

		}
	}

	@Override
	public float getChargeRateFull() {
		return 0;
	}

	@Override
	public double getPowerConsumedPerSecondResting() {
		return 0;
	}

	@Override
	public double getPowerConsumedPerSecondCharging() {
		return 0;
	}

	@Override
	public float getDuration() {
		return 0;
	}

	@Override
	public boolean onExecuteServer() {
		return true;
	}

	@Override
	public boolean onExecuteClient() {
		return true;
	}

	@Override
	public void onActive() {

	}

	@Override
	public void onInactive() {

	}

	@Override
	public boolean isPlayerUsable() {
		return playerUsable;
	}


	@Override
	public String getName() {
		return "AI Remote Controller";
	}
}
