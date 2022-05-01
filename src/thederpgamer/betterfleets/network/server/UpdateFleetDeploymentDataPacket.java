package thederpgamer.betterfleets.network.server;

import api.network.Packet;
import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import org.schema.game.common.data.player.PlayerState;
import thederpgamer.betterfleets.data.fleet.FleetDeploymentData;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <Description>
 *
 * @author Garret Reichenbach
 * @version 1.0 - [05/01/2022]
 */
public class UpdateFleetDeploymentDataPacket extends Packet {

	private ConcurrentHashMap<FleetDeploymentData, FleetDeploymentData.FleetDeploymentStatus> map;

	public UpdateFleetDeploymentDataPacket() {

	}

	public UpdateFleetDeploymentDataPacket(ConcurrentHashMap<FleetDeploymentData, FleetDeploymentData.FleetDeploymentStatus> map) {
		this.map = map;
	}

	@Override
	public void readPacketData(PacketReadBuffer packetReadBuffer) throws IOException {

	}

	@Override
	public void writePacketData(PacketWriteBuffer packetWriteBuffer) throws IOException {

	}

	@Override
	public void processPacketOnClient() {

	}

	@Override
	public void processPacketOnServer(PlayerState playerState) {

	}
}
