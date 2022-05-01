package thederpgamer.betterfleets.manager;

import api.common.GameClient;
import api.common.GameCommon;
import api.mod.ModSkeleton;
import api.mod.config.PersistentObjectUtil;
import api.network.packets.PacketUtil;
import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.player.PlayerState;
import thederpgamer.betterfleets.BetterFleets;
import thederpgamer.betterfleets.data.fleet.FleetDeploymentData;
import thederpgamer.betterfleets.network.client.RequestFleetDeploymentDataPacket;
import thederpgamer.betterfleets.network.client.SendFleetDeploymentDataPacket;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <Description>
 *
 * @author Garret Reichenbach
 * @version 1.0 - [04/30/2022]
 */
public class FleetDeploymentManager {

	private static final ArrayList<FleetDeploymentData> fleetDeployments = new ArrayList<>();

	public static void initialize() {
		if(GameCommon.isDedicatedServer() || GameCommon.isOnSinglePlayer()) {
			(new Timer()).scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					PersistentObjectUtil.save(BetterFleets.getInstance().getSkeleton());
				}
			}, 0, ConfigManager.getMainConfig().getConfigurableLong("auto-save-timer", 15000));
		}

		if(GameCommon.isClientConnectedToServer() || GameCommon.isOnSinglePlayer()) PacketUtil.sendPacketToServer(new RequestFleetDeploymentDataPacket());
	}

	public static ArrayList<FleetDeploymentData> getFleetDeployments() {
		return fleetDeployments;
	}

	public static void assignFleet(FleetDeploymentData deploymentData, Fleet fleet) {
		for(FleetDeploymentData data : fleetDeployments) {
			if(!data.getAssignedFleets().contains(fleet) || data.getStatus().equals(FleetDeploymentData.FleetDeploymentStatus.FINISHED)) data.getAssignedFleets().remove(fleet);
		}
		deploymentData.assignFleet(fleet);
		updateToServer(deploymentData);
	}

	public static void unAssignFleet(FleetDeploymentData deploymentData, Fleet fleet) {
		deploymentData.unAssignFleet(fleet);
		updateToServer(deploymentData);
	}

	public static ArrayList<Fleet> getAvailableFleets() {
		ArrayList<Fleet> fleets = new ArrayList<>();
		for(Fleet fleet : GameClient.getClientState().getFleetManager().getAvailableFleetsClient()) {
			for(FleetDeploymentData data : fleetDeployments) {
				if(!data.getAssignedFleets().contains(fleet) || data.getStatus().equals(FleetDeploymentData.FleetDeploymentStatus.FINISHED)) {
					if(data.getAssignedFleets().contains(fleet)) {
						if(data.getSettings(fleet).factionPermission != -1L && data.getSettings(fleet).factionPermission <= GameClient.getClientPlayerState().getFactionPermission()) fleets.add(fleet);
					} else fleets.add(fleet);
				}
			}
		}
		return fleets;
	}

	public static ConcurrentHashMap<FleetDeploymentData, FleetDeploymentData.FleetDeploymentStatus> getPlayerDeployments(PlayerState playerState) {
		ConcurrentHashMap<FleetDeploymentData, FleetDeploymentData.FleetDeploymentStatus> deployments = new ConcurrentHashMap<>();
		ModSkeleton instance = BetterFleets.getInstance().getSkeleton();
		for(Object object : PersistentObjectUtil.getObjects(instance, FleetDeploymentData.class)) {
			FleetDeploymentData deploymentData = (FleetDeploymentData) object;
			if(playerState.getFactionId() > 0 && deploymentData.getFaction() != null && deploymentData.getFaction().getIdFaction() == playerState.getFactionId()) {
				deployments.put(deploymentData, deploymentData.getStatus());
			}
		}
		return deployments;
	}

	public static void updateToServer(FleetDeploymentData deploymentData) {
		PacketUtil.sendPacketToServer(new SendFleetDeploymentDataPacket(deploymentData));
	}

	public static void updateFromClient(FleetDeploymentData deploymentData) {
		ModSkeleton instance = BetterFleets.getInstance().getSkeleton();
		FleetDeploymentData oldData = null;
		for(Object object : PersistentObjectUtil.getObjects(instance, FleetDeploymentData.class)) {
			FleetDeploymentData data = (FleetDeploymentData) object;
			if(data.getId().equals(deploymentData.getId())) {
				oldData = data;
				break;
			}
		}
		if(oldData != null) PersistentObjectUtil.removeObject(instance, oldData);
		PersistentObjectUtil.addObject(instance, deploymentData);
		PersistentObjectUtil.save(instance);
	}

	public static void updateFromServer(FleetDeploymentData deploymentData) {
		FleetDeploymentData oldData = null;
		for(FleetDeploymentData data : fleetDeployments) {
			if(data.getId().equals(deploymentData.getId())) {
				oldData = data;
				break;
			}
		}
		if(oldData != null) fleetDeployments.remove(oldData);
		fleetDeployments.add(deploymentData);
	}
}
