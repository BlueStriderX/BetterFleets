package org.schema.game.client.view.gui.mapgui;

import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.manager.ingame.map.MapFilterEditDialog;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gamemap.GameMapDrawer;
import org.schema.game.client.view.gamemap.GameMapPosition;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.world.VoidSystem;
import org.schema.game.server.data.Galaxy;
import org.schema.game.server.data.admin.AdminCommands;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.StateParameterNotFoundException;
import org.schema.schine.graphicsengine.forms.font.FontLibrary;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.input.InputState;
import thederpgamer.betterfleets.gui.element.GUIMouseUpdatedButtonPane;
import thederpgamer.betterfleets.gui.map.SelectedFleetList;
import thederpgamer.betterfleets.manager.FleetGUIManager;
import javax.vecmath.Vector4f;

/**
 * Modified version of MapToolsPanel.
 *
 * @author Schema, TheDerpGamer
 * @since 06/28/2021
 */
public class MapToolsPanel extends GUIAncor {

    private Vector3i relPosTmp = new Vector3i();

    //INSERTED CODE
    private SelectedFleetList selectedFleetList;
    public GUIMouseUpdatedButtonPane fleetActionsList;
    public GUIColoredRectangle fleetBox;
    //

    public MapToolsPanel(InputState var1) {
        super(var1, 800.0F, 128.0F);
        //INSERTED CODE
        FleetGUIManager.initializePanel(this);
        //
        this.init();
    }

    public GameClientState getState() {
        return (GameClientState)super.getState();
    }

    public void update(Timer var1) {
        super.update(var1);
    }

    public GameMapPosition getMapPosition() {
        return this.getState().getWorldDrawer().getGameMapDrawer().getGameMapPosition();
    }

    public GameMapDrawer getMapDrawer() {
        return this.getState().getWorldDrawer().getGameMapDrawer();
    }

    public String getSystemInfo(VoidSystem var1) {
        String var4;
        if (var1 != null) {
            String var2;
            if (var1.getOwnerFaction() != 0 && var1.getOwnerUID() != null && this.getState().getFactionManager().existsFaction(var1.getOwnerFaction())) {
                Faction var3 = this.getState().getFactionManager().getFaction(var1.getOwnerFaction());
                var2 = " " + StringTools.format(Lng.ORG_SCHEMA_GAME_CLIENT_VIEW_GUI_MAPGUI_MAPTOOLSPANEL_17, new Object[]{var3.getName()});
            } else {
                var2 = "";
            }

            Vector3i var5 = Galaxy.getLocalCoordinatesFromSystem(var1.getPos(), this.relPosTmp);
            var4 = this.getState().getCurrentGalaxy().getName(var5) + " " + var1.getPos() + var2;
        } else {
            var4 = Lng.ORG_SCHEMA_GAME_CLIENT_VIEW_GUI_MAPGUI_MAPTOOLSPANEL_18;
        }

        return var4;
    }

    private void init() {
        GUIColoredRectangle var1;
        (var1 = new GUIColoredRectangle(this.getState(), this.getWidth(), this.getHeight(), new Vector4f(0.1F, 0.1F, 0.3F, 0.5F))).rounded = 6.0F;
        this.attach(var1);
        GUITextOverlay var2;
        (var2 = new GUITextOverlay(120, 20, this.getState())).setTextSimple(Lng.ORG_SCHEMA_GAME_CLIENT_VIEW_GUI_MAPGUI_MAPTOOLSPANEL_0);
        GUITextOverlay var3;
        (var3 = new GUITextOverlay(120, 20, this.getState())).setTextSimple(new Object() {
            public String toString() {
                VoidSystem var1 = MapToolsPanel.this.getState().getCurrentClientSystem();
                if (MapToolsPanel.this.getState().getPlayer().isInTutorial()) {
                    return "Own Position: [System Tutorial] [Sector: Tutorial]";
                } else if (MapToolsPanel.this.getState().getPlayer().isInPersonalSector()) {
                    return Lng.ORG_SCHEMA_GAME_CLIENT_VIEW_GUI_MAPGUI_MAPTOOLSPANEL_1;
                } else {
                    return MapToolsPanel.this.getState().getPlayer().isInTestSector() ? Lng.ORG_SCHEMA_GAME_CLIENT_VIEW_GUI_MAPGUI_MAPTOOLSPANEL_2 : StringTools.format(Lng.ORG_SCHEMA_GAME_CLIENT_VIEW_GUI_MAPGUI_MAPTOOLSPANEL_3, new Object[]{MapToolsPanel.this.getSystemInfo(var1), MapToolsPanel.this.getState().getPlayer().getCurrentSector().toString()});
                }
            }
        });
        GUITextButton var4;
        (var4 = new GUITextButton(this.getState(), 140, 18, Lng.ORG_SCHEMA_GAME_CLIENT_VIEW_GUI_MAPGUI_MAPTOOLSPANEL_4, new GUICallback() {
            public void callback(GUIElement var1, MouseEvent var2) {
                if (var2.pressedLeftMouse()) {
                    Vector3i var3 = MapToolsPanel.this.getState().getPlayer().getCurrentSector();
                    MapToolsPanel.this.getMapPosition().set(var3.x, var3.y, var3.z, false);
                }

            }

            public boolean isOccluded() {
                return false;
            }
        })).setTextPos(3, 0);
        GUITextOverlay var5;
        (var5 = new GUITextOverlay(120, 20, this.getState())).setTextSimple(new Object() {
            final Vector3i tmpPos = new Vector3i();

            public String toString() {
                if (MapToolsPanel.this.getState().getController() != null && MapToolsPanel.this.getState().getController().getClientChannel() != null) {
                    VoidSystem var1;
                    return (var1 = MapToolsPanel.this.getState().getController().getClientChannel().getGalaxyManagerClient().getSystemOnClient(MapToolsPanel.this.getMapPosition().get(this.tmpPos))) != null ? StringTools.format(Lng.ORG_SCHEMA_GAME_CLIENT_VIEW_GUI_MAPGUI_MAPTOOLSPANEL_5, new Object[]{MapToolsPanel.this.getSystemInfo(var1), this.tmpPos.toString()}) : Lng.ORG_SCHEMA_GAME_CLIENT_VIEW_GUI_MAPGUI_MAPTOOLSPANEL_20;
                } else {
                    return Lng.ORG_SCHEMA_GAME_CLIENT_VIEW_GUI_MAPGUI_MAPTOOLSPANEL_21;
                }
            }
        });
        GUITextButton var6;
        (var6 = new GUITextButton(this.getState(), 140, 18, Lng.ORG_SCHEMA_GAME_CLIENT_VIEW_GUI_MAPGUI_MAPTOOLSPANEL_6, new GUICallback() {
            public boolean isOccluded() {
                return false;
            }

            public void callback(GUIElement var1, MouseEvent var2) {
                if (var2.pressedLeftMouse()) {
                    MapToolsPanel.this.getState().getPlayer().getCurrentSector();
                    MapToolsPanel.this.getState().getController().getClientGameData().setWaypoint(MapToolsPanel.this.getMapPosition().get(new Vector3i()));
                }

            }
        })).setTextPos(3, 0);
        GUITextButton var7 = new GUITextButton(this.getState(), 140, 18, Lng.ORG_SCHEMA_GAME_CLIENT_VIEW_GUI_MAPGUI_MAPTOOLSPANEL_7, new GUICallback() {
            public boolean isOccluded() {
                return false;
            }

            public void callback(GUIElement var1, MouseEvent var2) {
                if (var2.pressedLeftMouse()) {
                    (new MapFilterEditDialog(MapToolsPanel.this.getState(), GameMapDrawer.filter, false)).activate();
                }

            }
        });
        var6.setTextPos(3, 0);
        GUITextButton var8 = new GUITextButton(this.getState(), 139, 18, new Object() {
            public String toString() {
                return StringTools.format(Lng.ORG_SCHEMA_GAME_CLIENT_VIEW_GUI_MAPGUI_MAPTOOLSPANEL_19, new Object[]{MapToolsPanel.this.getState().getController().getClientGameData().getWaypoint().toString()});
            }
        }, new GUICallback() {
            public boolean isOccluded() {
                return false;
            }

            public void callback(GUIElement var1, MouseEvent var2) {
                Vector3i var3;
                if (var2.pressedLeftMouse() && (var3 = MapToolsPanel.this.getState().getController().getClientGameData().getWaypoint()) != null) {
                    MapToolsPanel.this.getMapPosition().set(var3.x, var3.y, var3.z, false);
                }

            }
        }) {
            public void draw() {
                if (MapToolsPanel.this.getState().getController().getClientGameData().getWaypoint() != null) {
                    super.draw();
                }

            }
        };
        GUITextButton var9 = new GUITextButton(this.getState(), 300, 18, new Object() {
            public String toString() {
                return StringTools.format(Lng.ORG_SCHEMA_GAME_CLIENT_VIEW_GUI_MAPGUI_MAPTOOLSPANEL_8, new Object[]{MapToolsPanel.this.getMapPosition().get(new Vector3i()).toString()});
            }
        }, new GUICallback() {
            public boolean isOccluded() {
                return false;
            }

            public void callback(GUIElement var1, MouseEvent var2) {
                if (var2.pressedLeftMouse()) {
                    Vector3i var3 = MapToolsPanel.this.getMapPosition().get(new Vector3i());
                    MapToolsPanel.this.getState().getController().sendAdminCommand(AdminCommands.CHANGE_SECTOR, new Object[]{var3.x, var3.y, var3.z});
                }

            }
        }) {
            public void draw() {
                if (MapToolsPanel.this.getState().getPlayer().getNetworkObject().isAdminClient.get() && !MapToolsPanel.this.getState().getPlayer().isInTutorial()) {
                    super.draw();
                }

            }
        };
        GUITextButton var10 = new GUITextButton(this.getState(), 300, 18, new Object() {
            public String toString() {
                return StringTools.format(Lng.ORG_SCHEMA_GAME_CLIENT_VIEW_GUI_MAPGUI_MAPTOOLSPANEL_9, new Object[]{MapToolsPanel.this.getMapPosition().get(new Vector3i()).toString()});
            }
        }, new GUICallback() {
            public boolean isOccluded() {
                return false;
            }

            public void callback(GUIElement var1, MouseEvent var2) {
                if (var2.pressedLeftMouse()) {
                    Vector3i var3 = MapToolsPanel.this.getMapPosition().getCurrentSysPos();
                    MapToolsPanel.this.getState().getController().sendAdminCommand(AdminCommands.SCAN, new Object[]{var3.x, var3.y, var3.z});
                }

            }
        }) {
            public void draw() {
                if (MapToolsPanel.this.getState().getPlayer().getNetworkObject().isAdminClient.get() && !MapToolsPanel.this.getState().getPlayer().isInTutorial()) {
                    super.draw();
                }

            }
        };
        GUICheckBox var11 = new GUICheckBox(this.getState()) {
            protected void activate() throws StateParameterNotFoundException {
                GameMapDrawer.drawPlanetOrbits = true;
            }

            protected boolean isActivated() {
                return GameMapDrawer.drawPlanetOrbits;
            }

            protected void deactivate() throws StateParameterNotFoundException {
                GameMapDrawer.drawPlanetOrbits = false;
            }
        };
        GUITextOverlay var12;
        (var12 = new GUITextOverlay(120, 20, this.getState())).setTextSimple(Lng.ORG_SCHEMA_GAME_CLIENT_VIEW_GUI_MAPGUI_MAPTOOLSPANEL_10);
        GUICheckBox var13 = new GUICheckBox(this.getState()) {
            protected boolean isActivated() {
                return GameMapDrawer.drawAsteroidBeltOrbits;
            }

            protected void deactivate() throws StateParameterNotFoundException {
                GameMapDrawer.drawAsteroidBeltOrbits = false;
            }

            protected void activate() throws StateParameterNotFoundException {
                GameMapDrawer.drawAsteroidBeltOrbits = true;
            }
        };
        GUITextOverlay var14;
        (var14 = new GUITextOverlay(120, 20, this.getState())).setTextSimple(Lng.ORG_SCHEMA_GAME_CLIENT_VIEW_GUI_MAPGUI_MAPTOOLSPANEL_11);
        GUICheckBox var15 = new GUICheckBox(this.getState()) {
            protected boolean isActivated() {
                return GameMapDrawer.highlightOrbitSectors;
            }

            protected void deactivate() throws StateParameterNotFoundException {
                GameMapDrawer.highlightOrbitSectors = false;
            }

            protected void activate() throws StateParameterNotFoundException {
                GameMapDrawer.highlightOrbitSectors = true;
            }
        };
        GUITextOverlay var16;
        (var16 = new GUITextOverlay(120, 20, this.getState())).setTextSimple(Lng.ORG_SCHEMA_GAME_CLIENT_VIEW_GUI_MAPGUI_MAPTOOLSPANEL_12);
        GUICheckBox var17 = new GUICheckBox(this.getState()) {
            protected boolean isActivated() {
                return GameMapDrawer.drawFactionByRelation;
            }

            protected void deactivate() throws StateParameterNotFoundException {
                GameMapDrawer.drawFactionByRelation = false;
            }

            protected void activate() throws StateParameterNotFoundException {
                GameMapDrawer.drawFactionByRelation = true;
            }
        };
        GUITextOverlay var18;
        (var18 = new GUITextOverlay(120, 20, this.getState())).setTextSimple(Lng.ORG_SCHEMA_GAME_CLIENT_VIEW_GUI_MAPGUI_MAPTOOLSPANEL_13);
        GUICheckBox var19 = new GUICheckBox(this.getState()) {
            protected boolean isActivated() {
                return GameMapDrawer.drawFactionTerritory;
            }

            protected void deactivate() throws StateParameterNotFoundException {
                GameMapDrawer.drawFactionTerritory = false;
            }

            protected void activate() throws StateParameterNotFoundException {
                GameMapDrawer.drawFactionTerritory = true;
            }
        };
        GUITextOverlay var20;
        (var20 = new GUITextOverlay(120, 20, this.getState())).setTextSimple(Lng.ORG_SCHEMA_GAME_CLIENT_VIEW_GUI_MAPGUI_MAPTOOLSPANEL_14);
        GUICheckBox var21 = new GUICheckBox(this.getState()) {
            protected boolean isActivated() {
                return GameMapDrawer.drawWormHoles;
            }

            protected void deactivate() throws StateParameterNotFoundException {
                GameMapDrawer.drawWormHoles = false;
            }

            protected void activate() throws StateParameterNotFoundException {
                GameMapDrawer.drawWormHoles = true;
            }
        };
        GUITextOverlay var22;
        (var22 = new GUITextOverlay(120, 20, this.getState())).setTextSimple(Lng.ORG_SCHEMA_GAME_CLIENT_VIEW_GUI_MAPGUI_MAPTOOLSPANEL_15);
        GUICheckBox var23 = new GUICheckBox(this.getState()) {
            protected boolean isActivated() {
                return GameMapDrawer.drawWarpGates;
            }

            protected void deactivate() throws StateParameterNotFoundException {
                GameMapDrawer.drawWarpGates = false;
            }

            protected void activate() throws StateParameterNotFoundException {
                GameMapDrawer.drawWarpGates = true;
            }
        };
        GUITextOverlay var24;
        (var24 = new GUITextOverlay(120, 20, this.getState())).setTextSimple(Lng.ORG_SCHEMA_GAME_CLIENT_VIEW_GUI_MAPGUI_MAPTOOLSPANEL_16);
        var11.setScale(0.5F, 0.5F, 0.5F);
        var13.setScale(0.5F, 0.5F, 0.5F);
        var15.setScale(0.5F, 0.5F, 0.5F);
        var17.setScale(0.5F, 0.5F, 0.5F);
        var19.setScale(0.5F, 0.5F, 0.5F);
        var23.setScale(0.5F, 0.5F, 0.5F);
        var21.setScale(0.5F, 0.5F, 0.5F);
        var8.setTextPos(7, 0);
        var9.setPos(1.0F, -20.0F, 0.0F);
        var1.attach(var9);
        var10.setPos(1.0F + var9.getWidth() + 10.0F, -20.0F, 0.0F);
        var1.attach(var10);
        var2.setPos(1.0F, 0.0F, 0.0F);
        var1.attach(var2);
        var3.setPos(1.0F, 20.0F, 0.0F);
        var1.attach(var3);
        var4.setPos(521.0F, 20.0F, 0.0F);
        var1.attach(var4);
        var5.setPos(1.0F, 40.0F, 0.0F);
        var1.attach(var5);
        var6.setPos(521.0F, 40.0F, 0.0F);
        var1.attach(var6);
        var8.setPos(521.0F + var6.getWidth(), 40.0F, 0.0F);
        var1.attach(var8);
        var7.setPos(521.0F, 60.0F, 0.0F);
        var1.attach(var7);
        var11.setPos(1.0F, 60.0F, 0.0F);
        var1.attach(var11);
        var12.setPos(17.0F, 60.0F, 0.0F);
        var1.attach(var12);
        var13.setPos(1.0F, 80.0F, 0.0F);
        var1.attach(var13);
        var14.setPos(17.0F, 80.0F, 0.0F);
        var1.attach(var14);
        var15.setPos(1.0F, 100.0F, 0.0F);
        var1.attach(var15);
        var16.setPos(17.0F, 100.0F, 0.0F);
        var1.attach(var16);
        var19.setPos(301.0F, 60.0F, 0.0F);
        var1.attach(var19);
        var20.setPos(317.0F, 60.0F, 0.0F);
        var1.attach(var20);
        var17.setPos(301.0F, 80.0F, 0.0F);
        var1.attach(var17);
        var18.setPos(317.0F, 80.0F, 0.0F);
        var1.attach(var18);
        var21.setPos(301.0F, 100.0F, 0.0F);
        var1.attach(var21);
        var22.setPos(317.0F, 100.0F, 0.0F);
        var1.attach(var22);
        var23.setPos(601.0F, 100.0F, 0.0F);
        var1.attach(var23);
        var24.setPos(617.0F, 100.0F, 0.0F);
        var1.attach(var24);

        //INSERTED CODE
        fleetBox = new GUIColoredRectangle(getState(), 500.0f, 500.0f, new Vector4f(0.1f, 0.3f, 0.1f, 0.5f));
        fleetBox.rounded = 6.0f;
        fleetBox.onInit();
        fleetBox.setPos(0.0f, 300.0f, 0.0f);

        GUITextOverlay fleetBoxOverlay = new GUITextOverlay(120, 20, getState());
        fleetBoxOverlay.onInit();
        fleetBoxOverlay.setFont(FontLibrary.FontSize.BIG.getFont());
        fleetBoxOverlay.setTextSimple("Selected Fleets:");
        fleetBoxOverlay.setPos(50.0f, 280.0f, 0.0f);
        fleetBox.attach(fleetBoxOverlay);

        (selectedFleetList = new SelectedFleetList(getState())).onInit();
        selectedFleetList.setPos(50.0f, 250.0f, 0.0f);
        fleetBox.attach(selectedFleetList);
        attach(fleetBox);

        (fleetActionsList = new GUIMouseUpdatedButtonPane(getState(), 1, 1, this)).onInit();
        //
    }

    //INSERTED CODE
    public void updateFleetList() {
        selectedFleetList.updateList();
        fleetActionsList.cleanUp();
    }
    //
}