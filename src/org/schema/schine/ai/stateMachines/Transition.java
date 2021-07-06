package org.schema.schine.ai.stateMachines;

/**
 * Modified version of Transition.
 *
 * @author Schema, TheDerpGamer
 * @since 07/06/2021
 */
public enum Transition {
    SEARCH_FOR_TARGET,
    NO_TARGET_FOUND,
    ENEMY_FIRE,
    RESTART,
    TARGET_AQUIRED,
    TARGET_OUT_OF_RANGE,
    STOP,
    TARGET_DESTROYED,
    IN_SHOOTING_POSITION,
    SHOOTING_COMPLETED,
    TARGET_IN_RANGE,
    MOVE_TO_SECTOR,
    ENEMY_PROXIMITY,
    MOVE,
    DISBAND,
    WAIT_COMPLETED,
    TARGET_SECTOR_REACHED,
    PLAN,
    HEALTH_LOW,
    PATH_FINISHED,
    ATTACK,
    ROAM,
    FOLLOW,
    RALLY,
    PATH_FAILED,
    CONDITION_SATISFIED,
    NEXT,
    BACK,
    TUTORIAL_FAILED,
    TUTORIAL_STOP,
    TUTORIAL_NO_SHIP_CREATED,
    TUTORIAL_CONDITION_BLOCK_EXISTS,
    TUTORIAL_CONDITION_CHEST_OPEN,
    TUTORIAL_CONDITION_IN_BUILD_MODE,
    TUTORIAL_CONDITION_IN_FLIGHT_MODE,
    TUTORIAL_CONDITION_IN_LAST_SHIP,
    TUTORIAL_CONDITION_IN_SHIP_UID,
    TUTORIAL_CONDITION_NPC_EXISTS,
    TUTORIAL_CONDITION_PRODUCTION_SET,
    TUTORIAL_CONDITION_WEAPON_PANEL_OPEN,
    TUTORIAL_RESTART,
    TUTORIAL_END,
    TUTORIAL_SKIP_PART,
    TUTORIAL_RESET_PART,
    TUTORIAL_SHOP_DISTANCE_LOST,
    SY_SPAWN_DONE,
    SY_ERROR,
    SY_CONVERSION_DONE,
    SY_LOADING_DONE,
    SY_DECONSTRUCTION_DONE_NO_DESIGN,
    SY_DECONSTRUCTION_DONE,
    SY_UNLOAD_DESIGN,
    SY_MOVING_TO_TEST_SITE_DONE,
    SY_BLOCK_TRANSACTION_FINISHED,
    SY_UNLOADING_DONE,
    SY_LOAD_NORMAL,
    SY_LOAD_DESIGN,
    SY_CREATE_DESIGN,
    SY_CONVERT_TO_BLUEPRINT,
    SY_CONVERT_BLUEPRINT_TO_DESIGN,
    SY_DECONSTRUCT,
    SY_DECONSTRUCT_RECYCLE,
    SY_CANCEL,
    SY_REPAIR_TO_DESIGN,
    SY_TEST_DESIGN,
    SY_SPAWN_DESIGN,
    SY_SPAWN_BLUEPRINT,
    SY_CONVERT,
    FLEET_EMPTY,
    DIALOG_AWNSER,
    DIALOG_HOOK,
    FLEET_MOVE_TO_FLAGSHIP_SECTOR,
    FLEET_IDLE_FORMATION,
    FLEET_DEFEND,
    FLEET_SENTRY_FORMATION,
    FLEET_SENTRY,
    FLEET_RECALL_CARRIER,
    FLEET_ATTACK,
    FLEET_FORMATION,
    FLEET_BREAKING,
    FLEET_GET_TO_MINING_POS,
    FLEET_FORMATION_REACHED,
    FLEET_MINE,
    FLEET_PATROL,
    FLEET_TRADE,
    FLEET_CLOAK,
    FLEET_UNCLOAK,
    FLEET_JAM,
    FLEET_UNJAM,
    //INSERTED CODE
    FLEET_ARTILLERY,
    FLEET_INTERCEPT,
    FLEET_SUPPORT,
    //
    FLEET_ACTION_DONE,
    NPCS_ABORT,
    NPCS_DONE,
    NPCS_EXPAND_TO_SYSTEM,
    NPCS_REPAIR_SYSTEM,
    NPCS_DO_MINING,
    NPCS_UPDATE_PRICES,
    NPCS_CREATE_FLEET,
    NPCS_CONSUME;

    private Transition() {
    }
}
