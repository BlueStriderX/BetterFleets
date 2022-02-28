package thederpgamer.betterfleets.network.client;

import api.network.Packet;
import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import org.schema.game.common.data.player.PlayerState;
import thederpgamer.betterfleets.manager.CommandUpdateManager;

import java.io.IOException;

/**
 * <Description>
 * [CLIENT] -> [SERVER]
 *
 * @author TheDerpGamer
 * @version 1.0 - [01/24/2022]
 */
public class SendCommandPacket extends Packet {

    private int entityId;
    private int targetId;
    private int mode;
    private float distance;

    public SendCommandPacket() {

    }

    public SendCommandPacket(int entityId, int targetId, int mode, float distance) {
        this.entityId = entityId;
        this.targetId = targetId;
        this.mode = mode;
        this.distance = distance;
    }

    @Override
    public void readPacketData(PacketReadBuffer packetReadBuffer) throws IOException {
        entityId = packetReadBuffer.readInt();
        targetId = packetReadBuffer.readInt();
        mode = packetReadBuffer.readInt();
        distance = packetReadBuffer.readFloat();
    }

    @Override
    public void writePacketData(PacketWriteBuffer packetWriteBuffer) throws IOException {
        packetWriteBuffer.writeInt(entityId);
        packetWriteBuffer.writeInt(targetId);
        packetWriteBuffer.writeInt(mode);
        packetWriteBuffer.writeFloat(distance);
    }

    @Override
    public void processPacketOnClient() {

    }

    @Override
    public void processPacketOnServer(PlayerState playerState) {
        CommandUpdateManager.addCommand(entityId, targetId, mode, distance);
    }
}
