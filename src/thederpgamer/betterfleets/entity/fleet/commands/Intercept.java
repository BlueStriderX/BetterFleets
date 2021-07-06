package thederpgamer.betterfleets.entity.fleet.commands;

import api.common.GameCommon;
import api.common.GameServer;
import org.schema.common.util.linAlg.Vector3fTools;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.SpaceStation;
import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.fleet.FleetCommandTypes;
import org.schema.game.common.data.fleet.FleetMember;
import org.schema.game.common.data.fleet.missions.machines.states.FleetState;
import org.schema.game.common.data.player.faction.FactionRelation;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.ai.program.common.TargetProgram;
import org.schema.game.server.ai.program.fleetcontrollable.states.FleetSeachingForTarget;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;
import thederpgamer.betterfleets.utils.ConfigManager;

import javax.vecmath.Vector3f;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Fleet blockade command state.
 *
 * @author TheDerpGamer
 * @since 07/06/2021
 */
public class Intercept extends FleetState {

    private float timer;

    public Intercept(Fleet fleet) {
        super(fleet);
        this.timer = ConfigManager.getMainConfig().getInt("fleet-command-update-interval");
    }

    @Override
    public FleetStateType getType() {
        return FleetStateType.INTERCEPTING;
    }

    @Override
    public boolean onExit() {
        return false;
    }

    @Override
    public boolean onUpdate() throws FSMException {
        if(timer <= 0) {
            interceptEnemies();
            timer = ConfigManager.getMainConfig().getInt("fleet-command-update-interval");
        }
        else timer --;
        return false;
    }

    public void interceptEnemies() throws FSMException {
        if(getEntityState().getFlagShip() == null) stateTransition(Transition.FLEET_EMPTY);
        else {
            if(getEntityState().getFlagShip().isLoaded()) {
                TargetProgram<?> targetProgram = (TargetProgram<?>) ((Ship) getEntityState().getFlagShip().getLoaded()).getAiConfiguration().getAiEntityState().getCurrentProgram();
                if(targetProgram.getTarget() == null) {
                    ArrayList<SegmentController> friendlyEntities = new ArrayList<>();
                    ArrayList<SegmentController> enemyEntities = new ArrayList<>();
                    for(FleetMember member : getEntityState().getMembers()) {
                        try {
                            Sector sector = GameServer.getUniverse().getSector(member.getSector());
                            for(SimpleTransformableSendableObject<?> object : sector.getEntities()) {
                                if(object instanceof SegmentController) {
                                    FactionRelation.RType relation = GameCommon.getGameState().getFactionManager().getRelation(member.getFactionId(), object.getFactionId());
                                    if(relation.equals(FactionRelation.RType.FRIEND)) friendlyEntities.add((SegmentController) object);
                                    if(relation.equals(FactionRelation.RType.ENEMY)) enemyEntities.add((SegmentController) object);
                                    //Todo: Add entities in adjacent sectors
                                }
                            }
                        } catch(IOException exception) {
                            exception.printStackTrace();
                        }
                    }
                    if(enemyEntities.isEmpty()) getEntityState().sendFleetCommand(FleetCommandTypes.IDLE);
                    else {
                        if(getEntityState().getFlagShip().isLoaded()) {
                            Ship flagShip = (Ship) getEntityState().getFlagShip().getLoaded();
                            Vector3f flagShipPos = flagShip.getWorldTransform().origin;
                            if(friendlyEntities.isEmpty()) {
                                if(!(flagShip.getAiConfiguration().getAiEntityState().getCurrentProgram().getMachine().getFsm().getCurrentState() instanceof FleetSeachingForTarget)) {
                                    flagShip.getAiConfiguration().getAiEntityState().getCurrentProgram().getMachine().getFsm().stateTransition(Transition.SEARCH_FOR_TARGET);
                                }
                            } else {
                                SegmentController selectedEnemy = enemyEntities.get(0);
                                float closestDistance = 10000f;
                                for(SegmentController enemy : enemyEntities) {
                                    SegmentController nearestTarget = getNearestTarget(enemy, friendlyEntities);
                                    if(nearestTarget != null) {
                                        Vector3f enemyPos = selectedEnemy.getWorldTransform().origin;
                                        float distance = Math.abs(Vector3fTools.distance(enemyPos.x, enemyPos.y, enemyPos.z, flagShipPos.x, flagShipPos.y, flagShipPos.z));
                                        if(distance < closestDistance) {
                                            closestDistance = distance;
                                            selectedEnemy = enemy;
                                        }
                                    }
                                }
                                for(FleetMember member : getEntityState().getMembers()) {
                                    if(member.isLoaded()) {
                                        Ship ship = (Ship) member.getLoaded();
                                        targetProgram = (TargetProgram<?>) ship.getAiConfiguration().getAiEntityState().getCurrentProgram();
                                        targetProgram.setTarget(selectedEnemy);
                                        if(!(ship.getAiConfiguration().getAiEntityState().getCurrentProgram().getMachine().getFsm().getCurrentState() instanceof FleetSeachingForTarget)) {
                                            ship.getAiConfiguration().getAiEntityState().getCurrentProgram().getMachine().getFsm().stateTransition(Transition.SEARCH_FOR_TARGET);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private SegmentController getNearestTarget(SegmentController attacker, ArrayList<SegmentController> nearbyTargets) {
        SegmentController nearestTarget = null;
        if(attacker instanceof Ship) {
            Vector3f attackerPos = attacker.getWorldTransform().origin;
            float nearestDistance = 10000f;
            TargetProgram<?> targetProgram = (TargetProgram<?>) ((Ship) attacker).getAiConfiguration().getAiEntityState().getCurrentProgram();
            for(SegmentController target : nearbyTargets) {
                if(targetProgram.getTarget() != null) {
                    if(nearestTarget == null) nearestTarget = (SegmentController) targetProgram.getTarget();
                    else {
                        Vector3f targetPos = target.getWorldTransform().origin;
                        float distance = Math.abs(Vector3fTools.distance(targetPos.x, targetPos.y, targetPos.z, attackerPos.x, attackerPos.y, attackerPos.z));
                        if(distance < nearestDistance) {
                            nearestDistance = distance;
                            nearestTarget = target;
                        }
                    }
                } else {
                    Vector3f targetPos = target.getWorldTransform().origin;
                    float distance = Math.abs(Vector3fTools.distance(targetPos.x, targetPos.y, targetPos.z, attackerPos.x, attackerPos.y, attackerPos.z));
                    if(distance < nearestDistance) {
                        nearestDistance = distance;
                        nearestTarget = target;
                    }
                }
            }
        } else {
            Vector3f attackerPos = attacker.getWorldTransform().origin;
            float nearestDistance = 10000f;
            TargetProgram<?> targetProgram = (TargetProgram<?>) ((SpaceStation) attacker).getAiConfiguration().getAiEntityState().getCurrentProgram();
            for(SegmentController target : nearbyTargets) {
                if(targetProgram.getTarget() != null) {
                    if(nearestTarget == null) nearestTarget = (SegmentController) targetProgram.getTarget();
                    else {
                        Vector3f targetPos = target.getWorldTransform().origin;
                        float distance = Math.abs(Vector3fTools.distance(targetPos.x, targetPos.y, targetPos.z, attackerPos.x, attackerPos.y, attackerPos.z));
                        if(distance < nearestDistance) {
                            nearestDistance = distance;
                            nearestTarget = target;
                        }
                    }
                } else {
                    Vector3f targetPos = target.getWorldTransform().origin;
                    float distance = Math.abs(Vector3fTools.distance(targetPos.x, targetPos.y, targetPos.z, attackerPos.x, attackerPos.y, attackerPos.z));
                    if(distance < nearestDistance) {
                        nearestDistance = distance;
                        nearestTarget = target;
                    }
                }
            }
        }
        return nearestTarget;
    }
}
