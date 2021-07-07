package thederpgamer.betterfleets.utils;

import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.elements.ShipManagerContainer;
import org.schema.game.common.controller.elements.beam.damageBeam.DamageBeamCollectionManager;
import org.schema.game.common.controller.elements.missile.MissileCollectionManager;
import org.schema.game.common.controller.elements.weapon.WeaponCollectionManager;
import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.fleet.FleetCommandTypes;
import org.schema.game.common.data.fleet.FleetMember;

/**
 * Fleet utility functions.
 *
 * @author TheDerpGamer
 * @since 07/06/2021
 */
public class FleetUtils {

    public static FleetCommandTypes getCurrentCommand(Fleet fleet) {
        for(FleetMember member : fleet.getMembers()) {
            if(member.isLoaded()) {
                switch(member.command.toUpperCase()) {
                    case "IDLE": return FleetCommandTypes.IDLE;
                    case "IDLE - SENTRY": return FleetCommandTypes.SENTRY;
                    case "IDLE - FORMATION": return FleetCommandTypes.FLEET_IDLE_FORMATION;
                    case "MOVING": return FleetCommandTypes.MOVE_FLEET;
                    case "ATTACKING": return FleetCommandTypes.FLEET_ATTACK;
                    case "SENTRY": return FleetCommandTypes.SENTRY;
                    case "DEFENDING": return FleetCommandTypes.FLEET_DEFEND;
                    case "ARTILLERY": return FleetCommandTypes.ARTILLERY;
                    case "INTERCEPTING": return FleetCommandTypes.INTERCEPT;
                    case "SUPPORTING": return FleetCommandTypes.SUPPORT;
                    case "SENTRY - FORMATION": return FleetCommandTypes.SENTRY_FORMATION;
                    case "CALLBACK TO CARRIER": return FleetCommandTypes.CALL_TO_CARRIER;
                    case "MINING": return FleetCommandTypes.MINE_IN_SECTOR;
                    case "PATROLLING": return FleetCommandTypes.PATROL_FLEET;
                    case "TRADING": return FleetCommandTypes.TRADE_FLEET;
                    case "CLOAKING": return  FleetCommandTypes.CLOAK;
                    case "UNCLOAKING": return FleetCommandTypes.UNCLOAK;
                    case "JAMMING": return FleetCommandTypes.JAM;
                    case "STOP JAMMING": return FleetCommandTypes.UNJAM;
                }
            }
        }
        return FleetCommandTypes.IDLE;
    }

    public static boolean hasRepairBeams(Fleet fleet) {
        for(FleetMember member : fleet.getMembers()) {
            try {
                ShipManagerContainer managerContainer = ((Ship) member.getLoaded()).getManagerContainer();
                if(managerContainer.getRepair() != null && managerContainer.getRepair().hasAtLeastOneCoreUnit()) return true;
            } catch(Exception ignored) { }
        }
        return false;
    }

    public static int getAverageMaxFiringRange(Fleet fleet) {
        int count = 0;
        int total = 0;
        for(FleetMember member : fleet.getMembers()) {
            try {
                if(member.isLoaded()) {
                    ShipManagerContainer managerContainer = ((Ship) member.getLoaded()).getManagerContainer();

                    if(managerContainer.getWeapon() != null && managerContainer.getWeapon().hasAtLeastOneCoreUnit()) {
                        for(WeaponCollectionManager collectionManager : managerContainer.getWeapon().getCollectionManagers()) {
                            total += collectionManager.getWeaponDistance();
                            count ++;
                        }
                    }

                    if(managerContainer.getBeam() != null && managerContainer.getBeam().hasAtLeastOneCoreUnit()) {
                        for(DamageBeamCollectionManager collectionManager : managerContainer.getBeam().getCollectionManagers()) {
                            total += collectionManager.getWeaponDistance();
                            count ++;
                        }
                    }

                    if(managerContainer.getMissile() != null && managerContainer.getMissile().hasAtLeastOneCoreUnit()) {
                        for(MissileCollectionManager<?, ?, ?> collectionManager : managerContainer.getMissile().getCollectionManagers()) {
                            total += collectionManager.getWeaponDistance();
                            count ++;
                        }
                    }
                }
            } catch(Exception ignored) { }
        }
        if(total > 0 && count > 0) return total / count;
        else return 0;
    }
}
