package thederpgamer.betterfleets;

import api.common.GameClient;
import api.listener.Listener;
import api.listener.events.draw.RegisterWorldDrawersEvent;
import api.listener.events.input.MousePressEvent;
import api.mod.StarLoader;
import api.mod.StarMod;
import org.lwjgl.input.Keyboard;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.gamemap.GameMapDrawer;
import org.schema.game.common.data.fleet.Fleet;
import thederpgamer.betterfleets.gui.map.FleetGUIMapDrawer;
import thederpgamer.betterfleets.manager.ConfigManager;
import thederpgamer.betterfleets.manager.FleetGUIManager;
import thederpgamer.betterfleets.manager.LogManager;
import thederpgamer.betterfleets.utils.Inputs;

import java.util.ArrayList;

/**
 * BetterFleets mod main class.
 *
 * @author TheDerpGamer
 * @since 06/14/2021
 */
public class BetterFleets extends StarMod {

    //Instance
    private static BetterFleets instance;
    public static BetterFleets getInstance() {
        return instance;
    }
    public BetterFleets() {

    }
    public static void main(String[] args) {

    }

    //Data
    public FleetGUIMapDrawer fleetMapDrawer;

    @Override
    public void onEnable() {
        instance = this;
        ConfigManager.initialize(this);
        LogManager.initialize();
        registerListeners();
    }

    private void registerListeners() {
        StarLoader.registerListener(RegisterWorldDrawersEvent.class, new Listener<RegisterWorldDrawersEvent>() {
            @Override
            public void onEvent(RegisterWorldDrawersEvent event) {
                event.getModDrawables().add(fleetMapDrawer = new FleetGUIMapDrawer());
            }
        }, this);

        StarLoader.registerListener(MousePressEvent.class, new Listener<MousePressEvent>() {
            @Override
            public void onEvent(MousePressEvent event) {
                try {
                    GameMapDrawer mapDrawer = GameClient.getClientState().getWorldDrawer().getGameMapDrawer();
                    if(mapDrawer.isMapActive()) { //Check if the map is currently open
                        Vector3i selectedPos = mapDrawer.getGameMapPosition().get(new Vector3i());
                        //Sector sector = GameServer.getUniverse().getSector(selectedPos, true);
                        ArrayList<Fleet> clientFleets = new ArrayList<>(GameClient.getClientState().getFleetManager().getAvailableFleetsClient());
                        ArrayList<Fleet> sectorFleets = new ArrayList<>();
                        for(Fleet fleet : clientFleets) if(fleet.getFlagShip().getSector().equals(selectedPos)) sectorFleets.add(fleet);

                        if(event.getButton() == Inputs.MouseButtons.LEFT_MOUSE.id) {
                            if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                                for(Fleet fleet : sectorFleets) { //Todo: Highlight icons or display list of selected fleets
                                    if(FleetGUIManager.selectedFleets.contains(fleet)) {
                                        FleetGUIManager.selectedFleets.remove(fleet);
                                        LogManager.logDebug("Client removed fleet " + fleet.getName().trim() + " from selection.");
                                    } else {
                                        FleetGUIManager.selectedFleets.add(fleet);
                                        LogManager.logDebug("Client added fleet " + fleet.getName().trim() + " to selection.");
                                    }
                                    fleetMapDrawer.needsUpdate = true;
                                }
                            } else {
                                if(!sectorFleets.isEmpty()) {
                                    FleetGUIManager.selectedFleets.add(sectorFleets.get(0));
                                    LogManager.logDebug("Client added fleet " + sectorFleets.get(0).getName().trim() + " to selection.");
                                    fleetMapDrawer.needsUpdate = true;
                                }
                            }
                        } else if(event.getButton() == Inputs.MouseButtons.RIGHT_MOUSE.id) {
                            StringBuilder builder = new StringBuilder();
                            for(int i = 0; i < FleetGUIManager.selectedFleets.size(); i ++) {
                                builder.append(FleetGUIManager.selectedFleets.get(i));
                                if(i < FleetGUIManager.selectedFleets.size() - 1) builder.append(", ");
                            }
                            LogManager.logDebug("Client right-clicked on fleet map GUI with the following fleets selected:\n" + builder.toString());
                        }
                    }
                } catch(Exception exception) {
                    LogManager.logWarning("Encountered an exception while trying to add/remove fleets from the map fleet selection", exception);
                }
            }
        }, this);
    }
}
