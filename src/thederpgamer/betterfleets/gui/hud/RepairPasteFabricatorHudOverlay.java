package thederpgamer.betterfleets.gui.hud;

import api.common.GameClient;
import org.schema.common.util.StringTools;
import org.schema.game.client.view.BuildModeDrawer;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.forms.font.FontLibrary;
import org.schema.schine.graphicsengine.forms.gui.GUIAncor;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.input.InputState;
import thederpgamer.betterfleets.element.ElementManager;

/**
 * <Description>
 *
 * @author TheDerpGamer
 * @since 07/03/2021
 */
public class RepairPasteFabricatorHudOverlay extends GUIAncor {

    private GUITextOverlay textOverlay;

    public RepairPasteFabricatorHudOverlay(InputState state) {
        super(state);
    }

    @Override
    public void onInit() {
        super.onInit();
        (textOverlay = new GUITextOverlay(32, 32, FontLibrary.FontSize.MEDIUM, getState())).onInit();
        attach(textOverlay);
    }

    public void updateText(SegmentController segmentController, float current, float max) {
        if(GameClient.getClientState() != null && segmentController.isFullyLoadedWithDock()) {
            try {
                if(max > 0) {
                    if(GameClient.getClientState().isInFlightMode() && segmentController.getSegmentBuffer().getPointUnsave(segmentController.getSlotAssignment().getAsIndex(GameClient.getClientPlayerState().getCurrentShipControllerSlot())).getType() == ElementKeyMap.REPAIR_CONTROLLER_ID) {
                        textOverlay.setTextSimple(StringTools.formatPointZero(current) + " / " + StringTools.formatPointZero(max));
                        setTextPos(1);
                    } else if(BuildModeDrawer.currentPiece.getType() == ElementManager.getBlock("Repair Paste Fabricator").getId() && !GameClient.getClientState().isInFlightMode()) {
                        textOverlay.setTextSimple("Repair Paste Fabricator: " + StringTools.formatPointZero(current) + " / " + StringTools.formatPointZero(max));
                        setTextPos(2);
                    } else textOverlay.setTextSimple("");
                }
            } catch(Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    private void setTextPos(int mode) {
        if(mode == 1) textOverlay.setPos((float) (GLFrame.getWidth() / 2 + 10), ((float) GLFrame.getHeight() / 2 + 100), 0);
        else if(mode == 2) textOverlay.setPos((float) GLFrame.getWidth() / 2 + 10, GameClient.getClientState().getWorldDrawer().getGuiDrawer().getHud().getHelpManager().getMouseYHeight() + 377, 0);
    }
}
