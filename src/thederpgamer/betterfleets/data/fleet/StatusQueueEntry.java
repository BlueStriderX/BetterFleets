package thederpgamer.betterfleets.data.fleet;

import api.common.GameClient;
import api.common.GameServer;
import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import org.schema.game.common.data.fleet.Fleet;
import thederpgamer.betterfleets.data.PersistentData;

import java.io.IOException;

/**
 * [Description]
 *
 * @author TheDerpGamer (MrGoose#0027)
 */
public class StatusQueueEntry implements PersistentData {

	private Status status;
	private Fleet fleet;
	private long startTime;

	public StatusQueueEntry(Status status, Fleet fleet) {
		this.status = status;
		this.fleet = fleet;
		this.startTime = System.currentTimeMillis();
	}

	public StatusQueueEntry(PacketReadBuffer packetReadBuffer) throws IOException {
		deserialize(packetReadBuffer);
	}

	@Override
	public void deserialize(PacketReadBuffer readBuffer) throws IOException {
		status = Status.values()[readBuffer.readInt()];
		if(GameServer.getServerState() != null) fleet = GameServer.getServerState().getFleetManager().getByFleetDbId(readBuffer.readLong());
		else if(GameClient.getClientState() != null) fleet = GameClient.getClientState().getFleetManager().getByFleetDbId(readBuffer.readLong());
		startTime = readBuffer.readLong();
	}

	@Override
	public void serialize(PacketWriteBuffer writeBuffer) throws IOException {
		writeBuffer.writeInt(status.ordinal());
		writeBuffer.writeLong(fleet.dbid);
		writeBuffer.writeLong(startTime);
	}

	public long getTime() {
		return System.currentTimeMillis() - startTime;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public Fleet getFleet() {
		return fleet;
	}

	public enum Status {
		IDLE,
		MOVING,
		ENGAGING,
		DEFENDING,
		DOCKED,
		DOCKING,
		UNDOCKING,
		UNDOCKED,
		DESTROYED,
		REPAIRING,
		REINFORCING,
		MOVING_TO_SHIPYARD,
		WAITING_FOR_SHIPYARD
	}
}
