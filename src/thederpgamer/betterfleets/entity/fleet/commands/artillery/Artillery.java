package thederpgamer.betterfleets.entity.fleet.commands.artillery;

import api.common.GameCommon;
import api.common.GameServer;
import org.schema.common.util.linAlg.Vector3fTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.fleet.FleetCommandTypes;
import org.schema.game.common.data.fleet.FleetMember;
import org.schema.game.common.data.fleet.missions.machines.states.FleetState;
import org.schema.game.common.data.player.faction.FactionRelation;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.ai.program.common.TargetProgram;
import org.schema.game.server.ai.program.fleetcontrollable.states.FleetMovingToSector;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.ServerConfig;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;
import thederpgamer.betterfleets.utils.FleetUtils;
import thederpgamer.betterfleets.utils.LogManager;
import javax.vecmath.Vector3f;
import java.io.IOException;
import java.util.Objects;
import java.util.Random;

/**
 * Fleet artillery command state.
 *
 * @author TheDerpGamer
 * @since 07/06/2021
 */
public class Artillery extends FleetState {

    public Artillery(Fleet fleet) {
        super(fleet);
    }

    @Override
    public FleetStateType getType() {
        return FleetStateType.ARTILLERY;
    }

    @Override
    public boolean onExit() {
        return false;
    }

    @Override
    public boolean onUpdate() throws FSMException {
        moveToMaxRange();
        return false;
    }

    @Override
    public boolean onEnterFleetState() {
        try {
            restartAllLoaded();
        } catch(FSMException exception) {
            exception.printStackTrace();
        }
        return super.onEnterFleetState();
    }

    public void moveToMaxRange() throws FSMException {
        if(getEntityState().getFlagShip() == null) stateTransition(Transition.FLEET_EMPTY);
        else {
            int maxRange = FleetUtils.getAverageMaxFiringRange(getEntityState());
            if(getEntityState().getFlagShip().isLoaded()) {
                Ship flagShip = (Ship) getEntityState().getFlagShip().getLoaded();
                TargetProgram<?> targetProgram = (TargetProgram<?>) flagShip.getAiConfiguration().getAiEntityState().getCurrentProgram();
                if(targetProgram.getTarget() == null) {
                    SegmentController newTarget = findFurthestTargetWithinRange(maxRange);
                    if(newTarget != null) {
                        targetProgram.setTarget(newTarget);
                        moveToMaxRange();
                    } else getEntityState().sendFleetCommand(FleetCommandTypes.IDLE);
                } else {
                    Vector3i newSector = getFurthestSectorInRange(maxRange);
                    for(FleetMember member : getEntityState().getMembers()) {
                        if(member.isLoaded()) {
                            Ship ship = (Ship) member.getLoaded();
                            targetProgram = (TargetProgram<?>) ship.getAiConfiguration().getAiEntityState().getCurrentProgram();
                            if(member.getSector() != newSector && !isWithinRange(member.getSector(), newSector, GameServer.getUniverse().getSector(targetProgram.getTarget().getSectorId()).pos, 1500)) {
                                targetProgram.setSectorTarget(newSector);
                                if(!(ship.getAiConfiguration().getAiEntityState().getCurrentProgram().getMachine().getFsm().getCurrentState() instanceof FleetMovingToSector)) {
                                    ship.getAiConfiguration().getAiEntityState().getCurrentProgram().getMachine().getFsm().stateTransition(Transition.MOVE_TO_SECTOR);
                                    LogManager.logDebug("Fleet member " + ship.getName() + " is moving to sector " + newSector.toString() + " to get into artillery range.");
                                }
                            } else {
                                ship.getAiConfiguration().getAiEntityState().getCurrentProgram().getMachine().getFsm().stateTransition(Transition.SEARCH_FOR_TARGET);
                                LogManager.logDebug("Fleet member " + ship.getName() + " is in artillery range of target and engaging from sector " + member.getSector().toString() + ".");
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isWithinRange(Vector3i currentSector, Vector3i requiredSector, Vector3i targetSector, int range) {
        float requiredDistance = Math.abs(Vector3i.getDisatance(requiredSector, targetSector));
        float currentDistance = Math.abs(Vector3i.getDisatance(currentSector, targetSector));
        return Math.abs(requiredDistance - currentDistance) <= range;
    }

    private SegmentController findFurthestTargetWithinRange(float maxRange) {
        SegmentController furthestTarget = null;
        float furthestDistance = 0;
        for(FleetMember member : getEntityState().getMembers()) {
            if(member.isLoaded()) {
                Ship ship = (Ship) member.getLoaded();
                Sector sector = ((GameServerState) ship.getState()).getUniverse().getSector(ship.getSectorId());
                Vector3f memberPos = ship.getWorldTransform().origin;
                if(sector != null) {
                    for(SimpleTransformableSendableObject<?> object : sector.getEntities()) {
                        if(object instanceof SegmentController) {
                            SegmentController entity = (SegmentController) object;
                            if(Objects.requireNonNull(GameCommon.getGameState()).getFactionManager().getRelation(entity.getFactionId(), member.getFactionId()) == FactionRelation.RType.ENEMY) {
                                if(furthestTarget == null) furthestTarget = entity;
                                else {
                                    Vector3f targetPos = furthestTarget.getWorldTransform().origin;
                                    float distance = Math.abs(Vector3fTools.distance(memberPos.x, memberPos.y, memberPos.z, targetPos.x, targetPos.y, targetPos.z));
                                    if(distance > furthestDistance && distance <= maxRange) {
                                        furthestTarget = entity;
                                        furthestDistance = distance;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return furthestTarget;
    }

    private Vector3i getFurthestSectorInRange(float maxRange) {
        Vector3i furthestSector = new Vector3i();
        if(getEntityState().getFlagShip().isLoaded()) {
            int sectorSize = (int) ServerConfig.SECTOR_SIZE.getCurrentState();
            Ship flagShip = (Ship) getEntityState().getFlagShip().getLoaded();
            TargetProgram<?> targetProgram = (TargetProgram<?>) flagShip.getAiConfiguration().getAiEntityState().getCurrentProgram();
            Vector3i targetSector = GameServer.getUniverse().getSector(targetProgram.getTarget().getSectorId()).pos;
            Vector3f currentPos = flagShip.getWorldTransform().origin;
            Vector3f targetPos = targetProgram.getTarget().getWorldTransform().origin;
            int mul = 1;
            while(true) {
                Vector3f possiblePos = new Vector3f(targetPos.x + (sectorSize * mul), targetPos.y + (sectorSize *  mul), targetPos.z + (sectorSize * mul));
                float distance = Math.abs(Vector3fTools.distance(currentPos.x, currentPos.y, currentPos.z, possiblePos.x, possiblePos.y, possiblePos.z));
                if(distance - maxRange <= 1500) {
                    try {
                        furthestSector = getNearestAdjacentNeutralSector(targetSector);
                        mul ++;
                    } catch(IOException exception) {
                        exception.printStackTrace();
                    }
                } else return furthestSector;
            }
        }
        return furthestSector;
    }

    private Vector3i getNearestAdjacentNeutralSector(Vector3i pos) throws IOException {
        Vector3i currentPos = getRandomAdjacentSector(pos);
        int attemptCount = 0;
        while(containsEnemy(currentPos) && attemptCount <= 10) {
            currentPos = getRandomAdjacentSector(pos);
            attemptCount ++;
        }
        return currentPos;
    }

    private Vector3i getRandomAdjacentSector(Vector3i pos) {
        Random random = new Random();
        int xOffset = random.nextInt(1 - -1) + -1;
        int yOffset = random.nextInt(1 - -1) + -1;
        int zOffset = random.nextInt(1 - -1) + -1;
        return new Vector3i(pos.x + xOffset, pos.y + yOffset, pos.z + zOffset);
    }

    private boolean containsEnemy(Vector3i pos) throws IOException {
        Sector sector = GameServer.getUniverse().getSector(pos);
        for(SimpleTransformableSendableObject<?> object : sector.getEntities()) {
            if(Objects.requireNonNull(GameCommon.getGameState()).getFactionManager().getRelation(object.getFactionId(), getEntityState().getFlagShip().getFactionId()) == FactionRelation.RType.ENEMY) {
                return true;
            }
        }
        return false;
    }
}
