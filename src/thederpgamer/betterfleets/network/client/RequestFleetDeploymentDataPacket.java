package thederpgamer.betterfleets.network.client;

import api.network.Packet;
import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import api.network.packets.PacketUtil;
import org.schema.game.common.data.player.PlayerState;
import thederpgamer.betterfleets.manager.FleetDeploymentManager;
import thederpgamer.betterfleets.network.server.UpdateFleetDeploymentDataPacket;

import java.io.IOException;

/**
 * <Description>
 *
 * @author Garret Reichenbach
 * @version 1.0 - [05/01/2022]
 */
public class RequestFleetDeploymentDataPacket extends Packet {

	public RequestFleetDeploymentDataPacket() {

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
		PacketUtil.sendPacket(playerState, new UpdateFleetDeploymentDataPacket(FleetDeploymentManager.getPlayerDeployments(playerState)));
	}
}
