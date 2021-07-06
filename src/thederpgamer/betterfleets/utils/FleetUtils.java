package thederpgamer.betterfleets.utils;

import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.elements.ShipManagerContainer;
import org.schema.game.common.controller.elements.beam.damageBeam.DamageBeamCollectionManager;
import org.schema.game.common.controller.elements.missile.MissileCollectionManager;
import org.schema.game.common.controller.elements.weapon.WeaponCollectionManager;
import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.fleet.FleetMember;

/**
 * Fleet utility functions.
 *
 * @author TheDerpGamer
 * @since 07/06/2021
 */
public class FleetUtils {

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
