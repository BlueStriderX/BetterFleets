package thederpgamer.betterfleets.data.fleet;

import api.common.GameServer;
import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import org.schema.game.common.controller.SpaceStation;
import org.schema.game.common.controller.database.DatabaseEntry;
import org.schema.game.common.data.element.ElementKeyMap;
import thederpgamer.betterfleets.data.PersistentData;
import thederpgamer.betterfleets.manager.FleetDeploymentManager;
import thederpgamer.betterfleets.manager.LogManager;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <Description>
 *
 * @author Garret Reichenbach
 * @version 1.0 - [04/30/2022]
 */
public class DeploymentStationData implements PersistentData {

	private long stationId;

	private final ConcurrentHashMap<String, Boolean> assignedFleetMap = new ConcurrentHashMap<>();

	public DeploymentStationData(SpaceStation station) {
		stationId = station.getDbId();
	}

	public DeploymentStationData(PacketReadBuffer readBuffer) throws IOException {
		deserialize(readBuffer);
	}

	@Override
	public void deserialize(PacketReadBuffer readBuffer) throws IOException {
		stationId = readBuffer.readLong();
		int size = readBuffer.readInt();
		if(size > 0) {
			for(int i = 0; i < size; i ++) assignedFleetMap.put(readBuffer.readString(), readBuffer.readBoolean());
		}
	}

	@Override
	public void serialize(PacketWriteBuffer writeBuffer) throws IOException {
		writeBuffer.writeLong(stationId);
		writeBuffer.writeInt(assignedFleetMap.size());
		if(!assignedFleetMap.isEmpty()) {
			for(Map.Entry<String, Boolean> entry : assignedFleetMap.entrySet()) {
				writeBuffer.writeString(entry.getKey());
				writeBuffer.writeBoolean(entry.getValue());
			}
		}
	}

	public SpaceStation getStation() {
		try {
			DatabaseEntry entry = GameServer.getServerState().getDatabaseIndex().getTableManager().getEntityTable().getById(stationId);
			return (SpaceStation) GameServer.getServerState().getSegmentControllersByName().get(entry.realName);
		} catch(Exception exception) {
			LogManager.logException("Encountered an exception while trying to fetch station from station data", exception);
			return null;
		}
	}

	public boolean hasShipyard() {
		return getStation().getElementClassCountMap().get(ElementKeyMap.SHIPYARD_COMPUTER) > 0;
	}

	public ConcurrentHashMap<FleetDeploymentData, Boolean> getAssignedFleets() {
		ConcurrentHashMap<FleetDeploymentData, Boolean> map = new ConcurrentHashMap();
		for(Map.Entry<String, Boolean> entry : assignedFleetMap.entrySet()) {
			try {
				map.put(FleetDeploymentManager.getById(entry.getKey()), entry.getValue());
			} catch(NullPointerException exception) {
				assignedFleetMap.remove(entry.getKey());
			}
		}
		return map;
	}
}