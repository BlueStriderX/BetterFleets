package thederpgamer.betterfleets.manager;

import api.common.GameCommon;
import api.common.GameServer;
import api.network.packets.PacketUtil;
import api.utils.StarRunnable;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.linearmath.Transform;
import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.SpaceStation;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.common.data.player.faction.FactionRelation;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.ai.SegmentControllerAIEntity;
import org.schema.game.server.ai.ShipAIEntity;
import org.schema.game.server.ai.program.common.TargetProgram;
import thederpgamer.betterfleets.BetterFleets;
import thederpgamer.betterfleets.data.TargetData;
import thederpgamer.betterfleets.network.client.SendCommandPacket;
import thederpgamer.betterfleets.network.server.SendCommandUpdatePacket;
import thederpgamer.betterfleets.utils.EntityUtils;

import javax.vecmath.Vector3f;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <Description>
 *
 * @author TheDerpGamer
 * @version 1.0 - [01/24/2022]
 */
public class CommandUpdateManager {

    public static final int NONE = 0;
    public static final int MOVE = 1;
    public static final int ATTACK = 2;
    public static final int DEFEND = 3;
    public static final int ESCORT = 4;

    private static final float MAX_DISTANCE_DIFF_RANGE = 10f; //Don't bother changing position if target distance is within range of current distance
    private static final long COMMAND_UPDATE_INTERVAL = 100L;
    private static final ConcurrentHashMap<SegmentController, TargetData> targetMap = new ConcurrentHashMap<>();

    public static void initialize() {
        new StarRunnable() {
            @Override
            public void run() {
                for(Map.Entry<SegmentController, TargetData> entry : targetMap.entrySet()) {
                    try {
                        if(GameCommon.getGameObject(entry.getKey().getId()) instanceof Ship) {
                            SegmentControllerAIEntity<?> aiEntity = getAIEntity(entry.getKey());
                            if(aiEntity != null && !entry.getKey().isConrolledByActivePlayer() && !entry.getKey().isDocked()) {
                                if(entry.getValue() == null || entry.getValue().mode == NONE) holdPosition((ShipAIEntity) aiEntity);
                                else if(GameCommon.getGameObject(entry.getValue().target.getId()) instanceof SegmentController && entry.getValue().target.getId() != entry.getKey().getId()) {
                                    float currentDistance = EntityUtils.getDistance(entry.getKey(), entry.getValue().target);
                                    if(Math.abs(currentDistance - entry.getValue().distance) > 300 || entry.getValue().mode == MOVE) updatePosition(entry.getKey(), entry.getValue()); //Change position if too far away
                                    else {
                                        if(aiEntity.getCurrentProgram().isSuspended()) aiEntity.getCurrentProgram().suspend(false);
                                        switch(entry.getValue().mode) {
                                            case ATTACK:
                                                ((TargetProgram<?>) aiEntity.getCurrentProgram()).setTarget(entry.getValue().target);
                                                LogManager.logDebug(entry.getKey().getRealName() + " attacking " + entry.getValue().target.getRealName() + " at " + StringTools.formatDistance(currentDistance) + " [" + StringTools.formatDistance(entry.getValue().distance) + "]");
                                                break;
                                            case DEFEND:
                                                LogManager.logDebug(entry.getKey().getRealName() + " defending " + entry.getValue().target.getRealName() + " at " + StringTools.formatDistance(currentDistance) + " [" + StringTools.formatDistance(entry.getValue().distance) + "]");
                                            case ESCORT:
                                                LogManager.logDebug(entry.getKey().getRealName() + " escorting " + entry.getValue().target.getRealName() + " at " + StringTools.formatDistance(currentDistance) + " [" + StringTools.formatDistance(entry.getValue().distance) + "]");
                                                float closestDistance = 20000;
                                                SegmentController closestEnemy = null;
                                                for(SimpleTransformableSendableObject<?> object : GameServer.getServerState().getUniverse().getSector(entry.getKey().getSectorId()).getEntities()) {
                                                    if(object instanceof SegmentController && !((SegmentController) object).isDocked()) {
                                                        SegmentController target = (SegmentController) object;
                                                        if(GameCommon.getGameState().getFactionManager().getRelation(entry.getKey().getFactionId(), target.getFactionId()).equals(FactionRelation.RType.ENEMY) || target.getFactionId() == FactionManager.PIRATES_ID) {
                                                            if(!target.isConrolledByActivePlayer() && getAIEntity(target) != null && ((TargetProgram<?>) getAIEntity(target).getCurrentProgram()).getTarget().equals(entry.getValue().target)) {
                                                                closestEnemy = target; //Prioritize attackers
                                                                break;
                                                            }
                                                            float tempDistance = EntityUtils.getDistance(entry.getKey(), target);
                                                            if(tempDistance < closestDistance && Math.abs(EntityUtils.getDistance(entry.getValue().target, target) - tempDistance) < 300) { //Only engage if enemy is close to entity and protection target
                                                                closestDistance = EntityUtils.getDistance(entry.getKey(), target);
                                                                closestEnemy = target;
                                                            }
                                                        }
                                                    }
                                                }
                                                if(closestEnemy != null) {
                                                    ((TargetProgram<?>) aiEntity.getCurrentProgram()).setTarget(closestEnemy);
                                                    LogManager.logDebug(entry.getKey().getRealName() + " intercepting " + closestEnemy.getRealName() + " at " + StringTools.formatDistance(currentDistance) + " [" + StringTools.formatDistance(entry.getValue().distance) + "]");
                                                }
                                                break;
                                        }
                                        targetMap.remove(entry.getKey());
                                    }
                                }
                            }
                        }
                    } catch(Exception exception) {
                        exception.printStackTrace();
                    }
                }
            }
        }.runTimer(BetterFleets.getInstance(), COMMAND_UPDATE_INTERVAL);
    }

    public static void addCommand(int entityId, int targetId, int mode, float distance) {
        if(GameServer.getServerState() != null) {
            Ship entity = (Ship) GameCommon.getGameObject(entityId);
            targetMap.remove(entity);
            if(mode == NONE) targetMap.put(entity, new TargetData(null, NONE, 0));
            else targetMap.put(entity, new TargetData((SegmentController) GameCommon.getGameObject(targetId), mode, distance));
        } else PacketUtil.sendPacketToServer(new SendCommandPacket(entityId, targetId, mode, distance));
    }

    private static void updatePosition(SegmentController entity, TargetData targetData) {
        SegmentControllerAIEntity<?> aiEntity = getAIEntity(entity);
        if(aiEntity != null && !entity.isConrolledByActivePlayer()) {
            try {
                if(aiEntity instanceof ShipAIEntity) {
                    if(aiEntity.getCurrentProgram().isSuspended()) aiEntity.getCurrentProgram().suspend(false);
                    if(entity.getSectorId() != targetData.target.getSectorId()) ((TargetProgram<?>) aiEntity.getCurrentProgram()).setSectorTarget(targetData.target.getSector(new Vector3i()));
                    else {
                        Vector3f moveVector = new Vector3f();
                        Vector3f targetPos = new Vector3f(targetData.target.getWorldTransform().origin);
                        Transform transform = new Transform(entity.getWorldTransform());
                        targetPos.scale(0.9f);
                        Vector3f distance = new Vector3f(transform.origin);
                        distance.sub(targetPos);
                        distance.absolute();
                        if(distance.length() < MAX_DISTANCE_DIFF_RANGE) {
                            holdPosition((ShipAIEntity) aiEntity);
                            targetData.target = null;
                            targetData.mode = NONE;
                            sendCommandUpdate(entity.getId(), targetData, entity.getSector(new Vector3i()));
                            //targetMap.remove(entity);
                       } else {
                            moveVector.cross(transform.origin, targetPos);
                            //((ShipAIEntity) aiEntity).orientate(GameServer.getServerState().getController().getTimer(), Quat4fTools.getNewQuat(moveVector.x, moveVector.y, moveVector.z, 0.0F));
                            ((Ship) entity).getNetworkObject().orientationDir.set(0,0,0,0);
                            ((Ship) entity).getNetworkObject().targetPosition.set(targetPos);
                            ((Ship) entity).getNetworkObject().moveDir.set(moveVector);
                            ((ShipAIEntity) aiEntity).moveTo(GameServer.getServerState().getController().getTimer(), moveVector, true);
                            //aiEntity.getCurrentProgram().getMachine().setState(new ShipMovingToSector(aiEntity.getCurrentProgram().getEntityState()));
                            //((TargetProgram<?>) aiEntity.getCurrentProgram()).setSectorTarget(targetData.target.getSector(new Vector3i()));
                            //((ShipMovingToSector) aiEntity.getCurrentProgram().getState()).setMovingDir(moveVector);
                            //sendCommandUpdate(entity.getId(), new TargetData(), entity.getSector(new Vector3i()));
                        }
                    }
                }
            } catch(Exception ignored) { }
        }
        targetMap.remove(entity);
    }

    public static SegmentControllerAIEntity<?> getAIEntity(SegmentController entity) {
        switch(entity.getType()) {
            case SHIP: return ((Ship) entity).getAiConfiguration().getAiEntityState();
            case SPACE_STATION: return ((SpaceStation) entity).getAiConfiguration().getAiEntityState();
            default: return null; //Only support Ship or Station AIs
        }
    }

    private static void holdPosition(ShipAIEntity aiEntity) {
        RigidBody body = (RigidBody) aiEntity.getEntity().getPhysicsDataContainer().getObject();
        Vector3f linearVelocity = body.getLinearVelocity(new Vector3f());
        linearVelocity.scale(0.3f);
        if(linearVelocity.length() < 1f) linearVelocity.set(0, 0, 0);
        body.setLinearVelocity(linearVelocity);
        targetMap.remove(aiEntity.getEntity());
    }

    private static void sendCommandUpdate(int entityId, TargetData targetData, Vector3i sector) {
        for(PlayerState playerState : GameServer.getServerState().getPlayerStatesByName().values()) {
            Vector3i playerSector = new Vector3i(playerState.getCurrentSector());
            playerSector.sub(sector);
            playerSector.absolute();
            if(playerSector.length() < ConfigManager.getMainConfig().getConfigurableFloat("tactical-map-view-distance", 1.2f)) {
                //Check if client is within range
                if(targetData.target == null || targetData.mode == NONE) PacketUtil.sendPacket(playerState, new SendCommandUpdatePacket(entityId, 0, NONE, 0));
                else PacketUtil.sendPacket(playerState, new SendCommandUpdatePacket(entityId, targetData.target.getId(), targetData.mode, targetData.distance));
            }
        }
    }
}
