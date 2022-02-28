package thederpgamer.betterfleets.network.server;

import api.common.GameCommon;
import api.network.Packet;
import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.player.PlayerState;
import thederpgamer.betterfleets.BetterFleets;
import thederpgamer.betterfleets.data.TargetData;
import thederpgamer.betterfleets.gui.element.sprite.TacticalMapEntityIndicator;
import thederpgamer.betterfleets.manager.CommandUpdateManager;

import java.io.IOException;

/**
 * <Description>
 * [SERVER] -> [CLIENT]
 *
 * @author TheDerpGamer
 * @version 1.0 - [01/25/2022]
 */
public class SendCommandUpdatePacket extends Packet {

    private int entityId;
    private int targetId;
    private int mode;
    private float distance;

    public SendCommandUpdatePacket() {

    }

    public SendCommandUpdatePacket(int entityId, int targetId, int mode, float distance) {
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
        TacticalMapEntityIndicator indicator = BetterFleets.getInstance().tacticalMapDrawer.drawMap.get(entityId);
        if(indicator != null) {
            if(GameCommon.getGameObject(targetId) instanceof SegmentController && mode != CommandUpdateManager.NONE) indicator.setCurrentTarget(new TargetData((SegmentController) GameCommon.getGameObject(targetId), mode, distance));
            else indicator.setCurrentTarget(null);
        }
    }

    @Override
    public void processPacketOnServer(PlayerState playerState) {

    }
}
