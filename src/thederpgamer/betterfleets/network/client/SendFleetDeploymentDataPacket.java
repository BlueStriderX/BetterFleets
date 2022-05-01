package thederpgamer.betterfleets.network.client;

import api.network.Packet;
import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import org.schema.game.common.data.player.PlayerState;
import thederpgamer.betterfleets.data.fleet.FleetDeploymentData;
import thederpgamer.betterfleets.manager.FleetDeploymentManager;

import java.io.IOException;

/**
 * <Description>
 *
 * @author Garret Reichenbach
 * @version 1.0 - [05/01/2022]
 */
public class SendFleetDeploymentDataPacket extends Packet {

	private FleetDeploymentData deploymentData;

	public SendFleetDeploymentDataPacket() {

	}

	public SendFleetDeploymentDataPacket(FleetDeploymentData deploymentData) {
		this.deploymentData = deploymentData;
	}

	@Override
	public void readPacketData(PacketReadBuffer packetReadBuffer) throws IOException {
		deploymentData = new FleetDeploymentData(packetReadBuffer);
	}

	@Override
	public void writePacketData(PacketWriteBuffer packetWriteBuffer) throws IOException {
		deploymentData.serialize(packetWriteBuffer);
	}

	@Override
	public void processPacketOnClient() {
		FleetDeploymentManager.updateFromServer(deploymentData);
	}

	@Override
	public void processPacketOnServer(PlayerState playerState) {
		FleetDeploymentManager.updateFromClient(deploymentData);
	}
}
