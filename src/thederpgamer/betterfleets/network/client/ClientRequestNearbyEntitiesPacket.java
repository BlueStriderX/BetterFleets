package thederpgamer.betterfleets.network.client;

import api.common.GameCommon;
import api.common.GameServer;
import api.network.Packet;
import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import api.network.packets.PacketUtil;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import thederpgamer.betterfleets.network.server.ServerSendNearbyEntitiesPacket;

import java.io.IOException;
import java.util.ArrayList;

/**
 * <Description>
 *
 * @author TheDerpGamer
 * @since 08/29/2021
 */
public class ClientRequestNearbyEntitiesPacket extends Packet {

    public int currentEntity;

    public ClientRequestNearbyEntitiesPacket() {

    }

    public ClientRequestNearbyEntitiesPacket(SegmentController currentEntity) {
        this.currentEntity = currentEntity.getId();
    }

    @Override
    public void readPacketData(PacketReadBuffer packetReadBuffer) throws IOException {
        currentEntity = packetReadBuffer.readInt();
    }

    @Override
    public void writePacketData(PacketWriteBuffer packetWriteBuffer) throws IOException {
        packetWriteBuffer.writeInt(currentEntity);
    }

    @Override
    public void processPacketOnClient() {

    }

    @Override
    public void processPacketOnServer(PlayerState playerState) {
        ArrayList<Integer> entities = new ArrayList<>();
        SegmentController entity = (SegmentController) GameCommon.getGameObject(currentEntity);
        Sector sector = GameServer.getUniverse().getSector(entity.getSectorId());
        for(SimpleTransformableSendableObject<?> e : sector.getEntities()) {
            if(e instanceof SegmentController && !((SegmentController) e).isCloakedFor(entity) && !((SegmentController) e).isJammingFor(entity)) {
                if(((SegmentController) e).isInFleet()) entities.add(e.getId());
            }
        }
        PacketUtil.sendPacket(playerState, new ServerSendNearbyEntitiesPacket(entities));
    }
}
