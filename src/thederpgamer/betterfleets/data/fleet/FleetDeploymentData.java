package thederpgamer.betterfleets.data.fleet;

import api.common.GameCommon;
import api.common.GameServer;
import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.SpaceStation;
import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.player.faction.Faction;
import thederpgamer.betterfleets.data.PersistentData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <Description>
 *
 * @author Garret Reichenbach
 * @version 1.0 - [04/30/2022]
 */
public class FleetDeploymentData implements PersistentData {

	public enum FleetDeploymentType {
		MOVE("Move", "Fleet will move to the specified sector.\n- Sector: The sector to move to\n- Auto Engage: Whether to automatically engage enemies or avoid to avoid contact", Vector3i.class, Boolean.class),
		ATTACK("Attack", "Fleet will attack enemies in the specified sector.\n- Sector: The sector to attack\n- Engagement Range: How far away should the fleet try to stay from the target", Vector3i.class, Float.class),
		DEFEND("Defend", "Fleet will defend friendly entities in the specified sector and not pursue retreating enemies.\n- Sector: The sector to defend", Vector3i.class),
		GUARD("Guard", "Fleet will follow another fleet and guard them from any aggressors.\n- Fleet: The fleet to guard", Long.class),
		SCOUT("Scout", "Fleet will scout another sector and report back results while attempting to avoid direct engagement with any enemies.\n- Sector: The sector to scout\n- Use stealth: If the fleet should use any available stealth systems such as jamming or cloaking", Vector3i.class, Boolean.class),
		MINE("Mine", "Fleet will mine target resources in the specified sector and avoid hostiles.\n- Sector: The sector to mine in\n- Target Resources: The specific resources to mine, defaults to all", Short[].class),
		SALVAGE("Salvage", "Fleet will salvage debris in the specified sector and avoid hostiles. Each ship has a small chance to recover a piece of battle data from the wreck, which can be used to reconstruct enemy designs and locate their weak points.\n- Sector: The sector to salvage in", Vector3i.class),
		//TRANSPORT("Transport", "Fleet will transport items from "), Might need to do some weird inventory block fuckery for this
		PATROL("Patrol", "Fleet will patrol along a specified path and and report on anything interesting they find.\n- Sectors: The sectors to scout\n- Repeat: Starts again after completing the path\n- Engage hostiles: If enabled, will pause pathing and attack hostiles if encountered\n- Notify others: Will notify other nearby friendlies");

		public final String display;
		public final String description;
		public final Class<?>[] fields;

		FleetDeploymentType(String display, String description, Class<?>... fields) {
			this.display = display;
			this.description = description;
			this.fields = fields;
		}
	}

	private String id;
	private Vector3i homeSector;
	private DeploymentStationData homeStation;
	private int factionId;
	private final ConcurrentHashMap<Long, FleetDeploymentSettings> assignedFleets = new ConcurrentHashMap<>();
	private FleetDeploymentType taskType;
	private FleetDeploymentStatus status;

	public FleetDeploymentData(Vector3i homeSector, DeploymentStationData homeStation, Faction faction) {
		this.homeSector = homeSector;
		this.homeStation = homeStation;
		this.factionId = faction.getIdFaction();
		this.id = UUID.randomUUID().toString();
	}

	public FleetDeploymentData(PacketReadBuffer readBuffer) throws IOException {
		deserialize(readBuffer);
	}

	public FleetDeploymentData(Vector3i homeSector, Faction faction) {
		this(homeSector, getFactionHomebaseData(faction), faction);
	}

	public String getId() {
		return id;
	}

	public Vector3i getHomeSector() {
		return homeSector;
	}

	public void setHomeSector(Vector3i homeSector) {
		this.homeSector = homeSector;
	}

	public DeploymentStationData getHomeStation() {
		return homeStation;
	}

	public void setHomeStation(DeploymentStationData homeStation) {
		this.homeStation = homeStation;
	}

	public Faction getFaction() {
		return GameCommon.getGameState().getFactionManager().getFaction(factionId);
	}

	public ArrayList<Fleet> getAssignedFleets() {
		ArrayList<Fleet> fleets = new ArrayList<>();
		for(long id : assignedFleets.keySet()) fleets.add(GameServer.getServerState().getFleetManager().getByFleetDbId(id));
		return fleets;
	}

	public FleetDeploymentSettings getSettings(Fleet fleet) {
		return assignedFleets.get(fleet.dbid);
	}

	public void assignFleet(Fleet fleet) {
		assignedFleets.put(fleet.dbid, new FleetDeploymentSettings());
	}

	public void unAssignFleet(Fleet fleet) {
		assignedFleets.remove(fleet.dbid);
	}

	public FleetDeploymentType getTaskType() {
		return taskType;
	}

	public FleetDeploymentStatus getStatus() {
		return status;
	}

	@Override
	public void deserialize(PacketReadBuffer readBuffer) throws IOException {
		id = readBuffer.readString();
		factionId = readBuffer.readInt();
		homeSector = readBuffer.readVector();
		if(readBuffer.readBoolean()) homeStation = new DeploymentStationData(readBuffer);
		int fleetCount = readBuffer.readInt();
		if(fleetCount > 0) {
			for(int i = 0; i < fleetCount; i ++) assignedFleets.put(readBuffer.readLong(), new FleetDeploymentSettings(readBuffer));
		}
		taskType = FleetDeploymentType.values()[readBuffer.readInt()];
		status = FleetDeploymentStatus.values()[readBuffer.readInt()];
	}

	@Override
	public void serialize(PacketWriteBuffer writeBuffer) throws IOException {
		writeBuffer.writeString(id);
		writeBuffer.writeInt(factionId);
		writeBuffer.writeVector(homeSector);
		if(homeStation != null) {
			writeBuffer.writeBoolean(true);
			homeStation.serialize(writeBuffer);
		} else writeBuffer.writeBoolean(false);
		writeBuffer.writeInt(assignedFleets.size());
		if(!assignedFleets.isEmpty()) {
			for(Map.Entry<Long, FleetDeploymentSettings> entry : assignedFleets.entrySet()) {
				writeBuffer.writeLong(entry.getKey());
				entry.getValue().serialize(writeBuffer);
			}
		}
		writeBuffer.writeInt(taskType.ordinal());
		writeBuffer.writeInt(status.ordinal());
	}

	public static DeploymentStationData getFactionHomebaseData(Faction faction) {
		SegmentController segmentController = GameServer.getServerState().getSegmentControllersByName().get(faction.getHomebaseRealName());
		if(segmentController instanceof SpaceStation) return new DeploymentStationData((SpaceStation) segmentController);
		else return null;
	}

	/**
	 * <Description>
	 *
	 * @author Garret Reichenbach
	 * @version 1.0 - [04/30/2022]
	 */
	public enum FleetDeploymentStatus {
		IDLE,
		ACTIVE,
		FINISHED
	}
}