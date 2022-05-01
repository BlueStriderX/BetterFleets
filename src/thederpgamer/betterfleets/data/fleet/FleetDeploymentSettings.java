package thederpgamer.betterfleets.data.fleet;

import api.common.GameClient;
import api.common.GameCommon;
import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import thederpgamer.betterfleets.data.PersistentData;

import java.io.IOException;

/**
 * <Description>
 *
 * @author Garret Reichenbach
 * @version 1.0 - [05/01/2022]
 */
public class FleetDeploymentSettings implements PersistentData {

	public boolean autoRepair = true;
	public long factionPermission = -1L;

	public FleetDeploymentSettings() {
		if(GameClient.getClientState() != null && GameClient.getClientPlayerState().getFactionId() > 0) {
			factionPermission = GameCommon.getGameState().getFactionManager().getFaction(GameClient.getClientPlayerState().getFactionId()).getRoles().getRoles()[0].role;
		}
	}

	public FleetDeploymentSettings(PacketReadBuffer readBuffer) throws IOException {
		deserialize(readBuffer);
	}

	@Override
	public void deserialize(PacketReadBuffer readBuffer) throws IOException {
		autoRepair = readBuffer.readBoolean();
		factionPermission = readBuffer.readLong();
	}

	@Override
	public void serialize(PacketWriteBuffer writeBuffer) throws IOException {
		writeBuffer.writeBoolean(autoRepair);
		writeBuffer.writeLong(factionPermission);
	}
}
