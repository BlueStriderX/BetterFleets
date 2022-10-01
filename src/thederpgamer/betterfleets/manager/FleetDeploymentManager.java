package thederpgamer.betterfleets.manager;

import api.common.GameClient;
import api.common.GameCommon;
import api.common.GameServer;
import api.mod.ModSkeleton;
import api.mod.config.PersistentObjectUtil;
import api.network.packets.PacketUtil;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SpaceStation;
import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.fleet.FleetCommandTypes;
import org.schema.game.common.data.fleet.FleetMember;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.ai.ShipAIEntity;
import org.schema.game.server.ai.program.common.TargetProgram;
import org.schema.game.server.data.PlayerNotFountException;
import thederpgamer.betterfleets.BetterFleets;
import thederpgamer.betterfleets.data.fleet.DeploymentStationData;
import thederpgamer.betterfleets.data.fleet.FleetDeploymentData;
import thederpgamer.betterfleets.data.fleet.StatusQueueEntry;
import thederpgamer.betterfleets.data.misc.ItemStack;
import thederpgamer.betterfleets.data.shipyard.ShipyardData;
import thederpgamer.betterfleets.network.client.RequestFleetDeploymentDataPacket;
import thederpgamer.betterfleets.network.client.SendFleetDeploymentDataPacket;
import thederpgamer.betterfleets.network.server.FleetStatusUpdatePacket;
import thederpgamer.betterfleets.utils.FleetUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * <Description>
 *
 * @author TheDerpGamer (MrGoose#0027)
 */
public class FleetDeploymentManager {

	private static final ArrayList<FleetDeploymentData> fleetDeployments = new ArrayList<>();
	private static final ArrayList<DeploymentStationData> availableStations = new ArrayList<>();
	private static final ConcurrentHashMap<Fleet, StatusQueueEntry> statusQueue = new ConcurrentHashMap<>();

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

	public static void addNewDeployment(FleetDeploymentData.FleetDeploymentType deploymentType) {
		Faction playerFaction = GameCommon.getGameState().getFactionManager().getFaction(GameClient.getClientPlayerState().getFactionId());
		fleetDeployments.add(new FleetDeploymentData(deploymentType, playerFaction.getHomeSector(), playerFaction));
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

	public static ArrayList<DeploymentStationData> getAvailableStations() {
		PacketUtil.sendPacketToServer(new RequestFleetDeploymentDataPacket());
		return availableStations;
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

	public static FleetDeploymentData getById(String id) {
		for(FleetDeploymentData data : getFleetDeployments()) {
			if(data.getId().equals(id)) return data;
		}
		return null;
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

	public static void updateAllFromServer(ConcurrentHashMap<FleetDeploymentData, FleetDeploymentData.FleetDeploymentStatus> map, ArrayList<DeploymentStationData> stations) {
		for(FleetDeploymentData deploymentData : map.keySet()) updateFromServer(deploymentData);
		availableStations.clear();
		availableStations.addAll(stations);
	}

	public static ArrayList<DeploymentStationData> getDeploymentStations(int factionId) {
		ArrayList<DeploymentStationData> stationData = new ArrayList<>();
		Faction faction = GameCommon.getGameState().getFactionManager().getFaction(factionId);
		try {
			for(Vector3i sector : faction.lastSystemSectors) {
				for(SimpleTransformableSendableObject<?> object : GameServer.getServerState().getUniverse().getSector(sector).getEntities()) {
					if(object.getType().equals(SimpleTransformableSendableObject.EntityType.SPACE_STATION) && object.getFactionId() == factionId) stationData.add(new DeploymentStationData((SpaceStation) object));
				}
			}
		} catch(IOException exception) {
			BetterFleets.log.log(Level.WARNING, "Failed to get deployable stations", exception);
		}
		return stationData;
	}

	/**
	 * Runs through the fleet status queue and applies any changes / updates.
	 */
	public static void runQueue() {
		for(Map.Entry<Fleet, StatusQueueEntry> entry : statusQueue.entrySet()) {
			Vector3i sector = Vector3i.parseVector3i(entry.getKey().getFlagShipSector());
			boolean updateClient = false;
			switch(entry.getValue().getStatus()) {
				case IDLE:
					statusQueue.remove(entry.getKey());
					updateClient = true;
					break;
				case MOVING:
					if(entry.getKey().getCurrentMoveTarget() == null || entry.getKey().getCurrentMoveTarget().equals(sector)) {
						entry.getValue().setStatus(StatusQueueEntry.Status.IDLE);
						statusQueue.remove(entry.getKey());
						updateClient = true;
					}
					break;
				case ENGAGING:
					FleetCommandTypes command = FleetUtils.getCurrentCommand(entry.getKey());
					switch(command) {
						case SENTRY:
						case SUPPORT:
						case FLEET_ATTACK:
						case FLEET_DEFEND:
						case ARTILLERY:
						case SENTRY_FORMATION:
						case INTERCEPT:
						case PATROL_FLEET:
							try {
								Sector s = GameServer.getServerState().getUniverse().getSector(sector);
								//Check for enemies in sector
								for(SimpleTransformableSendableObject<?> object : s.getEntities()) {
									if(object.getFactionId() != entry.getKey().getFlagShip().getFactionId() && object.getFactionId() != 0) {
										Faction factionA = GameServer.getServerState().getFactionManager().getFaction(entry.getKey().getFlagShip().getFactionId());
										Faction factionB = GameServer.getServerState().getFactionManager().getFaction(object.getFactionId());
										if(factionA.getEnemies().contains(factionB)) {
											entry.getValue().setStatus(StatusQueueEntry.Status.ENGAGING);
											statusQueue.remove(entry.getKey());
											updateClient = true;
											break;
										}
									}
								}

								//Check ai targets
								for(FleetMember member : entry.getKey().getMembers()) {
									if(member.getLoaded() != null) {
										ShipAIEntity aiEntity = (ShipAIEntity) CommandUpdateManager.getAIEntity(member.getLoaded());
										if(aiEntity != null) {
											TargetProgram<?> targetProgram = (TargetProgram<?>) aiEntity.getCurrentProgram();
											if(targetProgram.getTarget() != null) {
												entry.getValue().setStatus(StatusQueueEntry.Status.ENGAGING);
												statusQueue.remove(entry.getKey());
												updateClient = true;
												break;
											}
										}
									}
								}
							} catch(IOException exception) {
								BetterFleets.log.log(Level.WARNING, "Failed to get sector", exception);
							}
							break;
						default:
							break;
					}
					break;
				case
			}
			if(updateClient) {
				//Send update packet to fleet owner
				try {
					PacketUtil.sendPacket(GameServer.getServerState().getPlayerFromName(entry.getKey().getOwner()), new FleetStatusUpdatePacket(entry.getValue()));
				} catch(PlayerNotFountException ignored) {
					BetterFleets.log.info("Player \"" + entry.getKey().getOwner() + "\" not online, skipping update");
				}
			}
		}
	}

	/**
	 * Attempts to reinforce a fleet using the nearest available shipyard.
	 * <p>The shipyard will attempt to build the specified ships, and will order the required resources to be delivered.</p>
	 * <p>If the resources are not available, will return a list of what's needed.</p>
	 *
	 * @param deploymentData The deployment data to reinforce
	 * @param fleet The fleet to reinforce
	 * @return A list of resources needed to reinforce the fleet, or an empty list if no additional resources were needed
	 */
	public static ArrayList<ItemStack> reinforceFleet(FleetDeploymentData deploymentData, Fleet fleet) {
		ArrayList<ItemStack> resourcesNeeded = new ArrayList<>();
		//Get the nearest shipyard
		ShipyardData nearestShipyard = getNearestShipyard(fleet, false); //Todo: Some sort of permissions system for allied shipyards
		if(nearestShipyard != null) {
			//If fleet is not at the shipyard, move it there
			Vector3i fleetPosition = Vector3i.parseVector3i(fleet.getFlagShipSector());
			if(!fleetPosition.equals(nearestShipyard.getSector())) {
				fleet.setCurrentMoveTarget(nearestShipyard.getSector());
				//Add to queue that checks if the fleet has arrived. Once it has, run this method again
				statusQueue.put(fleet, new StatusQueueEntry(StatusQueueEntry.Status.MOVING_TO_SHIPYARD, fleet));
			}
			//If fleet is at the shipyard, repair it
			//If no repairs are needed, reinforce it
			//If no reinforcements are needed, return
		}
		return resourcesNeeded;
	}

	/**
	 * Attempts to fetch the first shipyard nearest to the specified fleet.
	 * <p>If no shipyard is found, will return null.</p>
	 *
	 * @param fleet The fleet to find a shipyard for
	 * @param includeAllied If allied shipyards should be included in the search
	 * @return The nearest shipyard, or null if none were found
	 */
	public static ShipyardData getNearestShipyard(Fleet fleet, boolean includeAllied) {
		try {
			Sector sector = GameServer.getServerState().getUniverse().getSector(Vector3i.parseVector3i(fleet.getFlagShipSector()));
			for(SimpleTransformableSendableObject<?> object : sector.getEntities()) {
				if(object.getType().equals(SimpleTransformableSendableObject.EntityType.SPACE_STATION)) {
					SpaceStation station = (SpaceStation) object;
					if(station.getManagerContainer().getShipyard() != null) {
						Faction fleetFaction = GameServer.getServerState().getFactionManager().getFaction(fleet.getFlagShip().getFactionId());
						Faction stationFaction = GameServer.getServerState().getFactionManager().getFaction(station.getFactionId());
						if(stationFaction.equals(fleetFaction) || (includeAllied && fleetFaction.getFriends().contains(stationFaction))) return new ShipyardData(station.getManagerContainer().getShipyard());
					}
				}
			}
		} catch(Exception exception) {
			BetterFleets.log.log(Level.WARNING, "Failed to get nearest shipyard", exception);
		}
		return null;
	}
}
