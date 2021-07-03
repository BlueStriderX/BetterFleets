package thederpgamer.betterfleets.systems.repairpastefabricator;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.data.element.ElementCollection;

/**
 * Element Collection Unit for Repair Paste Fabricator system.
 *
 * @author TheDerpGamer
 * @since 07/03/2021
 */
public class RepairPasteFabricatorUnit extends ElementCollection<RepairPasteFabricatorUnit, RepairPasteFabricatorCollectionManager, RepairPasteFabricatorElementManager> {

    @Override
    public ControllerManagerGUI createUnitGUI(GameClientState gameClientState, ControlBlockElementCollectionManager<?, ?, ?> supportCollectionManager, ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager) {
        return elementCollectionManager.getElementManager().getGUIUnitValues(this, elementCollectionManager, supportCollectionManager, effectCollectionManager);
    }
}
