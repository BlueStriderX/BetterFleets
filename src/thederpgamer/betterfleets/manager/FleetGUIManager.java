package thederpgamer.betterfleets.manager;

import com.bulletphysics.util.ObjectArrayList;
import org.schema.game.client.view.gui.mapgui.MapToolsPanel;
import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.fleet.FleetCommandTypes;
import java.util.List;

/**
 * Manages data pertaining to the fleet command menu.
 *
 * @author TheDerpGamer
 * @since 06/15/2021
 */
public class FleetGUIManager  {

    public static final List<Fleet> selectedFleets = new ObjectArrayList<>();
    public static FleetCommandTypes currentCommandType = FleetCommandTypes.IDLE;
    private static MapToolsPanel mapToolsPanel;

    public static void initializePanel(MapToolsPanel toolsPanel) {
        mapToolsPanel = toolsPanel;
    }

    public static MapToolsPanel getPanel() {
        return mapToolsPanel;
    }
}
