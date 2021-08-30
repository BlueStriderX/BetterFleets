package thederpgamer.betterfleets.network.server;

import api.common.GameCommon;
import api.network.Packet;
import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.player.PlayerState;
import thederpgamer.betterfleets.BetterFleets;
import thederpgamer.betterfleets.gui.element.sprite.TacticalMapFleetIndicator;

import java.io.IOException;
import java.util.ArrayList;

/**
 * <Description>
 *
 * @author TheDerpGamer
 * @since 08/29/2021
 */
public class ServerSendNearbyEntitiesPacket extends Packet {

    public ArrayList<Integer> entities;

    public ServerSendNearbyEntitiesPacket() {

    }

    public ServerSendNearbyEntitiesPacket(ArrayList<Integer> entities) {
        this.entities = entities;
    }

    @Override
    public void readPacketData(PacketReadBuffer packetReadBuffer) throws IOException {
        entities = packetReadBuffer.readIntList();
    }

    @Override
    public void writePacketData(PacketWriteBuffer packetWriteBuffer) throws IOException {
        packetWriteBuffer.writeIntList(entities);
    }

    @Override
    public void processPacketOnClient() {
        for(int id : entities) {
            SegmentController entity = (SegmentController) GameCommon.getGameObject(id);
            if(entity.isInFleet()) {
                long dbid = entity.getFleet().dbid;
                if(!BetterFleets.getInstance().tacticalMapDrawer.drawMap.containsKey(dbid)) BetterFleets.getInstance().tacticalMapDrawer.drawMap.put(dbid, new TacticalMapFleetIndicator(entity.getFleet()));
                if(BetterFleets.getInstance().tacticalMapDrawer.drawMap.get(dbid).getDistance() > BetterFleets.getInstance().tacticalMapDrawer.maxDrawDistance) BetterFleets.getInstance().tacticalMapDrawer.drawMap.remove(dbid);
            }
        }
    }

    @Override
    public void processPacketOnServer(PlayerState playerState) {

    }
}
