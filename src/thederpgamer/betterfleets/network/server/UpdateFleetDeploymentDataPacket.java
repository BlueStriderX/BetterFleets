package thederpgamer.betterfleets.network.server;

import api.network.Packet;
import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import org.schema.game.common.data.player.PlayerState;
import thederpgamer.betterfleets.data.fleet.DeploymentStationData;
import thederpgamer.betterfleets.data.fleet.FleetDeploymentData;
import thederpgamer.betterfleets.manager.FleetDeploymentManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <Description>
 *
 * @author Garret Reichenbach
 * @version 1.0 - [05/01/2022]
 */
public class UpdateFleetDeploymentDataPacket extends Packet {

	private ConcurrentHashMap<FleetDeploymentData, FleetDeploymentData.FleetDeploymentStatus> map;
	private ArrayList<DeploymentStationData> stations;

	public UpdateFleetDeploymentDataPacket() {

	}

	public UpdateFleetDeploymentDataPacket(ConcurrentHashMap<FleetDeploymentData, FleetDeploymentData.FleetDeploymentStatus> map, ArrayList<DeploymentStationData> stations) {
		this.map = map;
		this.stations = stations;
	}

	@Override
	public void readPacketData(PacketReadBuffer packetReadBuffer) throws IOException {
		int mapSize = packetReadBuffer.readInt();
		if(mapSize > 0) {
			for(int i = 0; i < mapSize; i ++) map.put(new FleetDeploymentData(packetReadBuffer), FleetDeploymentData.FleetDeploymentStatus.values()[packetReadBuffer.readInt()]);
		}
		int listSize = packetReadBuffer.readInt();
		if(listSize > 0) {
			for(int i = 0; i < listSize; i ++) stations.add(new DeploymentStationData(packetReadBuffer));
		}
	}

	@Override
	public void writePacketData(PacketWriteBuffer packetWriteBuffer) throws IOException {
		packetWriteBuffer.writeInt(map.size());
		for(Map.Entry<FleetDeploymentData, FleetDeploymentData.FleetDeploymentStatus> entry : map.entrySet()) {
			entry.getKey().serialize(packetWriteBuffer);
			packetWriteBuffer.writeInt(entry.getValue().ordinal());
		}
		packetWriteBuffer.writeInt(stations.size());
		if(!stations.isEmpty()) {
			for(DeploymentStationData stationData : stations) stationData.serialize(packetWriteBuffer);
		}
	}

	@Override
	public void processPacketOnClient() {
		FleetDeploymentManager.updateAllFromServer(map, stations);
	}

	@Override
	public void processPacketOnServer(PlayerState playerState) {
	}
}
