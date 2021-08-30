package thederpgamer.betterfleets.utils;

import api.common.GameClient;
import api.common.GameCommon;
import com.bulletphysics.util.ObjectArrayList;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.gui.mapgui.MapToolsPanel;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.fleet.FleetCommandTypes;
import org.schema.game.common.data.fleet.FleetMember;
import org.schema.game.common.data.player.faction.FactionRelation;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
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

    public static Vector4f getIconColor(Fleet fleet) {
        int playerFactionId = GameClient.getClientPlayerState().getFactionId();
        int fleetFactionId = fleet.getFlagShip().getFactionId();
        if(selectedFleets.contains(fleet)) return new Vector4f(1.0f, 1.0f, 0.0f, 1.0f);
        else {
            Vector4f color = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
            try {
                Vector3f c = GameCommon.getGameState().getFactionManager().getRelation(playerFactionId, fleetFactionId).defaultColor;
                color.set(c.x, c.y, c.z, 1.0f);
            } catch(Exception ignored) { }
            return color;
        }
    }

    public static void orderFleets(FleetCommandTypes commandType) {
        for(Fleet fleet : selectedFleets) {
            if(commandType.args.length == 1) fleet.sendFleetCommand(commandType, mapToolsPanel.getMapPosition().get(new Vector3i()));
            else fleet.sendFleetCommand(commandType);
            fleet.getFlagShip().mapEntry.getColor().set(getIconColor(fleet));
        }
        selectedFleets.clear();
        mapToolsPanel.updateFleetList();
    }

    public static void orderJamming() {
        for(Fleet fleet : selectedFleets) {
            FleetCommandTypes command = FleetUtils.getCurrentCommand(fleet);
            if(command.equals(FleetCommandTypes.JAM)) fleet.sendFleetCommand(FleetCommandTypes.UNJAM);
            else fleet.sendFleetCommand(FleetCommandTypes.JAM);
            fleet.getFlagShip().mapEntry.getColor().set(getIconColor(fleet));
        }
        selectedFleets.clear();
        mapToolsPanel.updateFleetList();
    }

    public static void orderCloak() {
        for(Fleet fleet : selectedFleets) {
            FleetCommandTypes command = FleetUtils.getCurrentCommand(fleet);
            if(command.equals(FleetCommandTypes.CLOAK)) fleet.sendFleetCommand(FleetCommandTypes.UNCLOAK);
            else fleet.sendFleetCommand(FleetCommandTypes.CLOAK);
            fleet.getFlagShip().mapEntry.getColor().set(getIconColor(fleet));
        }
        selectedFleets.clear();
        mapToolsPanel.updateFleetList();
    }

    public static void orderFormation() {
        for(Fleet fleet : selectedFleets) {
            FleetCommandTypes command = FleetUtils.getCurrentCommand(fleet);
            if(command.equals(FleetCommandTypes.SENTRY)) fleet.sendFleetCommand(FleetCommandTypes.SENTRY_FORMATION);
            else if(command.equals(FleetCommandTypes.SENTRY_FORMATION)) fleet.sendFleetCommand(FleetCommandTypes.SENTRY);
            else if(command.equals(FleetCommandTypes.FLEET_IDLE_FORMATION)) fleet.sendFleetCommand(FleetCommandTypes.IDLE);
            else fleet.sendFleetCommand(FleetCommandTypes.FLEET_IDLE_FORMATION);
            fleet.getFlagShip().mapEntry.getColor().set(getIconColor(fleet));
        }
        selectedFleets.clear();
        mapToolsPanel.updateFleetList();
    }

    public static void orderTurretsActivate() {
        for(Fleet fleet : selectedFleets) {
            for(FleetMember member : fleet.getMembers()) {
                Ship ship = (Ship) member.getLoaded();
                if(ship != null) ship.railController.activateAllAIClient(true, true, true);
            }
        }
        selectedFleets.clear();
        mapToolsPanel.updateFleetList();
    }

    public static void orderTurretsDeactivate() {
        for(Fleet fleet : selectedFleets) {
            for(FleetMember member : fleet.getMembers()) {
                Ship ship = (Ship) member.getLoaded();
                if(ship != null) ship.railController.activateAllAIClient(false, true, false);
            }
        }
        selectedFleets.clear();
        mapToolsPanel.updateFleetList();
    }
}
