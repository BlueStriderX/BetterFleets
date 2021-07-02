package thederpgamer.betterfleets.utils;

import api.common.GameCommon;
import com.bulletphysics.util.ObjectArrayList;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.gui.mapgui.MapToolsPanel;
import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.fleet.FleetCommandTypes;
import org.schema.game.common.data.player.faction.FactionRelation;
import java.util.List;

/**
 * Manages data pertaining to the fleet command menu.
 *
 * @author TheDerpGamer
 * @since 06/15/2021
 */
public class FleetGUIManager  {

    public static final List<Fleet> selectedFleets = new ObjectArrayList<>();
    private static MapToolsPanel mapToolsPanel;

    public static void initializePanel(MapToolsPanel toolsPanel) {
        mapToolsPanel = toolsPanel;
    }

    public static MapToolsPanel getPanel() {
        return mapToolsPanel;
    }

    public static FactionRelation.RType getFleetRelation(Fleet fleetA, Fleet fleetB) {
        return GameCommon.getGameState().getFactionManager().getRelation(fleetA.getFlagShip().getFactionId(), fleetB.getFlagShip().getFactionId());
    }

    public static void orderFleets(FleetCommandTypes commandType) {
        for(Fleet fleet : selectedFleets) {
            if(commandType.args.length == 1) fleet.sendFleetCommand(commandType, mapToolsPanel.getMapPosition().get(new Vector3i()));
            else fleet.sendFleetCommand(commandType);
        }
        selectedFleets.clear();
        mapToolsPanel.updateFleetList();
    }

    public static void orderJamming() {
        for(Fleet fleet : selectedFleets) {
            FleetCommandTypes command = FleetCommandTypes.valueOf(fleet.getMissionName().toUpperCase());
            if(command.equals(FleetCommandTypes.JAM)) fleet.sendFleetCommand(FleetCommandTypes.UNJAM);
            else fleet.sendFleetCommand(FleetCommandTypes.JAM);
        }
        selectedFleets.clear();
        mapToolsPanel.updateFleetList();
    }

    public static void orderCloak() {
        for(Fleet fleet : selectedFleets) {
            FleetCommandTypes command = FleetCommandTypes.valueOf(fleet.getMissionName().toUpperCase());
            if(command.equals(FleetCommandTypes.CLOAK)) fleet.sendFleetCommand(FleetCommandTypes.UNCLOAK);
            else fleet.sendFleetCommand(FleetCommandTypes.CLOAK);
        }
        selectedFleets.clear();
        mapToolsPanel.updateFleetList();
    }

    public static void orderFormation() {
        for(Fleet fleet : selectedFleets) {
            FleetCommandTypes command = FleetCommandTypes.valueOf(fleet.getFlagShip().command.toUpperCase());
            if(command.equals(FleetCommandTypes.SENTRY)) fleet.sendFleetCommand(FleetCommandTypes.SENTRY_FORMATION);
            else if(command.equals(FleetCommandTypes.SENTRY_FORMATION)) fleet.sendFleetCommand(FleetCommandTypes.SENTRY);
            else if(command.equals(FleetCommandTypes.FLEET_IDLE_FORMATION)) fleet.sendFleetCommand(FleetCommandTypes.IDLE);
            else fleet.sendFleetCommand(FleetCommandTypes.FLEET_IDLE_FORMATION);
        }
    }
}
