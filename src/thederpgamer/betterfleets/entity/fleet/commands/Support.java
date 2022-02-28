package thederpgamer.betterfleets.entity.fleet.commands;

import api.common.GameCommon;
import com.bulletphysics.linearmath.Transform;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.fleet.FleetMember;
import org.schema.game.common.data.fleet.missions.machines.states.FleetState;
import org.schema.game.common.data.player.faction.FactionRelation;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.ai.program.common.TargetProgram;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;
import thederpgamer.betterfleets.manager.ConfigManager;
import thederpgamer.betterfleets.systems.RepairPasteFabricatorSystem;
import thederpgamer.betterfleets.utils.EntityUtils;
import thederpgamer.betterfleets.utils.FleetUtils;

/**
 * Fleet support command state.
 *
 * @author TheDerpGamer
 * @since 07/06/2021
 */
public class Support extends FleetState {

    private float timer;

    public Support(Fleet fleet) {
        super(fleet);
        this.timer = ConfigManager.getMainConfig().getInt("fleet-command-update-interval");
    }

    @Override
    public FleetStateType getType() {
        return FleetStateType.SUPPORT;
    }

    @Override
    public boolean onExit() {
        return false;
    }

    @Override
    public boolean onUpdate() throws FSMException {
        if(timer <= 0) {
            runAction();
            timer = ConfigManager.getMainConfig().getInt("fleet-command-update-interval");
        } else timer --;
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

    public void runAction() throws FSMException {
        if(getEntityState().getFlagShip() == null) stateTransition(Transition.FLEET_EMPTY);
        else {
            for(FleetMember member : getEntityState().getMembers()) {
                if(member.isLoaded()) {
                    Ship ship = (Ship) member.getLoaded();
                    RepairPasteFabricatorSystem supportSystem = FleetUtils.getSupportSystem(ship);
                    if(supportSystem != null) {
                        SegmentController ally = getNearbyDamagedAlly(ship);
                        if(ally != null) {
                            TargetProgram<?> targetProgram = (TargetProgram<?>) ship.getAiConfiguration().getAiEntityState().getCurrentProgram();
                            targetProgram.setSpecificTargetId(ally.getId());
                            targetProgram.setTarget(ally);
                            //ShootingStateInterface currentState = (ShootingStateInterface) targetProgram.getOtherMachine("ATT").getFsm().getCurrentState();
                            ship.getNetworkObject().targetPosition.set(ally.getWorldTransform().origin);
                            targetProgram.suspend(false);
                        } else avoidEnemies(ship);
                    }
                }
            }
        }
    }

    private SegmentController getNearbyDamagedAlly(Ship ship) {
        SegmentController nearestAlly = null;
        try {
            Sector sector = ((GameServerState) ship.getState()).getUniverse().getSector(ship.getSectorId());
            for(SimpleTransformableSendableObject<?> object : sector.getEntities()) {
                if(object instanceof SegmentController) {
                    SegmentController segmentController = (SegmentController) object;
                    if(!segmentController.railController.isInAnyRailRelationWith(ship) && GameCommon.getGameState().getFactionManager().getRelation(segmentController.getFactionId(), ship.getFactionId()).equals(FactionRelation.RType.FRIEND)) {
                        boolean damageTaken = segmentController.getHpController().getHp() < segmentController.getHpController().getMaxHp();
                        if(damageTaken && (nearestAlly == null || EntityUtils.getDistance(segmentController, ship) < EntityUtils.getDistance(nearestAlly, ship))) nearestAlly = segmentController;
                    }
                }
            }
        } catch(Exception exception) {
            exception.printStackTrace();
        }
        return nearestAlly;
    }

    private void avoidEnemies(Ship ship) {
        SegmentController nearestEnemy = null;
        try {
            Sector sector = ((GameServerState) ship.getState()).getUniverse().getSector(ship.getSectorId());
            for(SimpleTransformableSendableObject<?> object : sector.getEntities()) {
                if(object instanceof SegmentController) {
                    SegmentController segmentController = (SegmentController) object;
                    if(!segmentController.railController.isInAnyRailRelationWith(ship) && GameCommon.getGameState().getFactionManager().getRelation(segmentController.getFactionId(), ship.getFactionId()).equals(FactionRelation.RType.ENEMY)) {
                        if(nearestEnemy == null || EntityUtils.getDistance(segmentController, ship) < EntityUtils.getDistance(nearestEnemy, ship)) nearestEnemy = segmentController;
                    }
                }
            }
        } catch(Exception exception) {
            exception.printStackTrace();
        }

        if(nearestEnemy != null) {
            Transform currentTransform = new Transform(ship.getWorldTransform());
            Transform enemyTransform = new Transform(nearestEnemy.getWorldTransform());
            currentTransform.origin.sub(enemyTransform.origin);
            currentTransform.origin.normalize();
            currentTransform.origin.scale(-1);
            EntityUtils.moveToPosition(ship, currentTransform);
        }
    }
}
