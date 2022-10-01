package thederpgamer.betterfleets.network.server;

import api.network.Packet;
import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import org.schema.game.common.data.player.PlayerState;
import thederpgamer.betterfleets.data.fleet.StatusQueueEntry;

import java.io.IOException;

/**
 * [Description]
 *
 * @author TheDerpGamer (MrGoose#0027)
 */
public class FleetStatusUpdatePacket extends Packet {

	private StatusQueueEntry statusQueueEntry;

	public FleetStatusUpdatePacket() {

	}

	public FleetStatusUpdatePacket(StatusQueueEntry statusQueueEntry) {
		this.statusQueueEntry = statusQueueEntry;
	}

	@Override
	public void readPacketData(PacketReadBuffer packetReadBuffer) throws IOException {
		statusQueueEntry = new StatusQueueEntry(packetReadBuffer);
	}

	@Override
	public void writePacketData(PacketWriteBuffer packetWriteBuffer) throws IOException {
		statusQueueEntry.serialize(packetWriteBuffer);
	}

	@Override
	public void processPacketOnClient() {

	}

	@Override
	public void processPacketOnServer(PlayerState playerState) {

	}
}
