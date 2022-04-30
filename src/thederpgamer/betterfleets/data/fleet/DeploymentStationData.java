package thederpgamer.betterfleets.data.fleet;

import api.common.GameServer;
import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import org.schema.game.common.controller.SpaceStation;
import org.schema.game.common.controller.database.DatabaseEntry;
import thederpgamer.betterfleets.data.PersistentData;
import thederpgamer.betterfleets.manager.LogManager;

import java.io.IOException;

/**
 * <Description>
 *
 * @author Garret Reichenbach
 * @version 1.0 - [04/30/2022]
 */
public class DeploymentStationData implements PersistentData {

	private long stationId;

	public DeploymentStationData(SpaceStation station) {
		stationId = station.getDbId();
	}

	public DeploymentStationData(PacketReadBuffer readBuffer) throws IOException {
		deserialize(readBuffer);
	}

	@Override
	public void deserialize(PacketReadBuffer readBuffer) throws IOException {
		stationId = readBuffer.readLong();
	}

	@Override
	public void serialize(PacketWriteBuffer writeBuffer) throws IOException {
		writeBuffer.writeLong(stationId);
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
}