package thederpgamer.betterfleets.manager;

import thederpgamer.betterfleets.data.fleet.FleetDeploymentData;

import java.util.ArrayList;

/**
 * <Description>
 *
 * @author Garret Reichenbach
 * @version 1.0 - [04/30/2022]
 */
public class ClientCacheManager {

	private static final ArrayList<FleetDeploymentData> fleetDeployments = new ArrayList<>();

	public static ArrayList<FleetDeploymentData> getFleetDeployments() {
		return fleetDeployments;
	}
}
