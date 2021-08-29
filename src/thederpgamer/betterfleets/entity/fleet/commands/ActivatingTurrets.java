package thederpgamer.betterfleets.entity.fleet.commands;

import org.schema.game.common.controller.Ship;
import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.fleet.FleetMember;
import org.schema.game.common.data.fleet.missions.machines.states.FleetState;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;

/**
 * <Description>
 *
 * @author TheDerpGamer
 * @since 08/28/2021
 */
public class ActivatingTurrets extends FleetState {

    public ActivatingTurrets(Fleet fleet) {
        super(fleet);
    }

    @Override
    public FleetStateType getType() {
        return FleetStateType.ACTIVATING_TURRETS;
    }

    @Override
    public boolean onExit() {
        return false;
    }

    @Override
    public boolean onUpdate() throws FSMException {
        if(getEntityState().getFlagShip() == null) stateTransition(Transition.FLEET_EMPTY);
        else {
            for(FleetMember member : getEntityState().getMembers()) {
                Ship ship = (Ship) member.getLoaded();
                if(ship != null) ship.railController.activateAllAIClient(true, true, true);
            }
        }
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
}
