package org.schema.game.common.data.fleet.missions.machines;

import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.fleet.missions.machines.states.*;
import org.schema.schine.ai.MachineProgram;
import org.schema.schine.ai.stateMachines.FiniteStateMachine;
import org.schema.schine.ai.stateMachines.Message;
import org.schema.schine.ai.stateMachines.State;
import org.schema.schine.ai.stateMachines.Transition;
import thederpgamer.betterfleets.entity.fleet.commands.Artillery;
import thederpgamer.betterfleets.entity.fleet.commands.Intercept;
import thederpgamer.betterfleets.entity.fleet.commands.Support;

/**
 * Modified version of FleetFiniteStateMachine.
 *
 * @author Schema, TheDerpGamer
 * @since 07/06/2021
 */
public class FleetFiniteStateMachine extends FiniteStateMachine<FleetFiniteStateMachineFactory>{

    private Idle idle;
    private FormationingIdle formationingIdle;
    private FormationingSentry formatoningSentry;
    private Moving moving;
    private Attacking attacking;
    private Defending defending;
    private SentryIdle sentry;
    private CallbackToCarrier recall;
    private MiningAsteroids mining;
    private Patrolling patrolling;
    private Trading trading;

    //INSERTED CODE
    private Artillery artillery;
    private Intercept intercept;
    private Support support;
    //

    private Jamming jamming;
    private UnJamming unjamming;
    private Cloaking cloaking;
    private UnCloaking uncloaking;

    @Override
    public Fleet getObj() {
        return (Fleet) super.getObj();
    }


    public FleetFiniteStateMachine(Fleet obj,
                                   MachineProgram<?> program, FleetFiniteStateMachineFactory parameter) {
        super(obj, program, parameter);

        Fleet gObj = getObj();


    }

    @Override
    public void createFSM(FleetFiniteStateMachineFactory parameter) {
        parameter.createMachine(this);

        Fleet gObj = getObj();
        idle = new Idle(gObj);
        formationingIdle = new FormationingIdle(gObj);
        formatoningSentry = new FormationingSentry(gObj);
        moving = new Moving(gObj);
        attacking = new Attacking(gObj);
        defending = new Defending(gObj);
        sentry = new SentryIdle(gObj);
        recall = new CallbackToCarrier(gObj);
        mining = new MiningAsteroids(gObj);
        patrolling = new Patrolling(gObj);
        trading = new Trading(gObj);

        //INSERTED CODE
        artillery = new Artillery(gObj);
        intercept = new Intercept(gObj);
        support = new Support(gObj);
        //
        cloaking = new Cloaking(gObj);
        uncloaking = new UnCloaking(gObj);
        jamming = new Jamming(gObj);
        unjamming = new UnJamming(gObj);

        addState(idle);
        addState(formationingIdle);
        addState(formatoningSentry);
        addState(moving);
        addState(attacking);
        addState(defending);
        addState(sentry);
        addState(recall);
        addState(mining);
        addState(patrolling);
        addState(trading);

        //INSERTED CODE
        addState(artillery);
        addState(intercept);
        addState(support);
        //

        moving.addTransition(Transition.TARGET_SECTOR_REACHED, idle);
        patrolling.addTransition(Transition.TARGET_SECTOR_REACHED, idle);
        trading.addTransition(Transition.TARGET_SECTOR_REACHED, idle);


        setStartingState(idle);
    }
    private void addState(State s){
        s.addTransition(Transition.FLEET_IDLE_FORMATION, formationingIdle);
        s.addTransition(Transition.FLEET_SENTRY_FORMATION, formatoningSentry);
        s.addTransition(Transition.FLEET_ATTACK, attacking);
        s.addTransition(Transition.FLEET_DEFEND, defending);
        s.addTransition(Transition.RESTART, idle);
        s.addTransition(Transition.FLEET_EMPTY, idle);
        s.addTransition(Transition.MOVE_TO_SECTOR, moving);
        s.addTransition(Transition.FLEET_PATROL, patrolling);
        s.addTransition(Transition.FLEET_TRADE, trading);
        s.addTransition(Transition.FLEET_SENTRY, sentry);
        s.addTransition(Transition.FLEET_RECALL_CARRIER, recall);
        s.addTransition(Transition.FLEET_MINE, mining);

        //INSERTED CODE
        s.addTransition(Transition.FLEET_ARTILLERY, artillery);
        s.addTransition(Transition.FLEET_INTERCEPT, intercept);
        s.addTransition(Transition.FLEET_SUPPORT, support);
        //

        s.addTransition(Transition.FLEET_CLOAK, cloaking);
        s.addTransition(Transition.FLEET_UNCLOAK, uncloaking);
        s.addTransition(Transition.FLEET_JAM, jamming);
        s.addTransition(Transition.FLEET_UNJAM, unjamming);

        cloaking.addTransition(Transition.FLEET_ACTION_DONE, s);
        uncloaking.addTransition(Transition.FLEET_ACTION_DONE, s);
        jamming.addTransition(Transition.FLEET_ACTION_DONE, s);
        unjamming.addTransition(Transition.FLEET_ACTION_DONE, s);
    }
    @Override
    public void onMsg(Message message) {
        // TODO Auto-generated method stub

    }
}