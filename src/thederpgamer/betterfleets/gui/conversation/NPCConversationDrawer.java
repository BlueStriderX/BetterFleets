package thederpgamer.betterfleets.gui.conversation;

import api.common.GameClient;
import api.utils.draw.ModWorldDrawer;
import org.schema.game.client.view.gui.shiphud.newhud.HudContextHelpManager;
import org.schema.game.client.view.gui.shiphud.newhud.HudContextHelperContainer;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.ContextFilter;
import org.schema.schine.input.KeyboardMappings;
import thederpgamer.betterfleets.utils.EntityUtils;

/**
 * <Description>
 *
 * @author TheDerpGamer
 * @version 1.0 - [09/12/2021]
 */
public class NPCConversationDrawer extends ModWorldDrawer {

    public static final float UPDATE_TIMER = 15.0f;
    public static final float MAX_INTERACTION_DISTANCE = 100.0f;

    public NPCConversationDialog dialog;

    private float updateTimer = UPDATE_TIMER;
    private boolean initialized = false;
    private boolean active = false;

    private HudContextHelperContainer helperContainer;

    @Override
    public void onInit() {
        (dialog = new NPCConversationDialog()).onInit();
        initialized = true;
    }

    @Override
    public void draw() {
        if(!initialized) onInit();
        if(active) {
            if(helperContainer != null && GameClient.getClientState().isInFlightMode()) {
                getHudHelpManager().addHelper(helperContainer);
            }
        }
    }

    @Override
    public void update(Timer timer) {
        updateTimer --;
        if(updateTimer <= 0) {
            updateInteraction();
            updateTimer = UPDATE_TIMER;
        }
    }

    @Override
    public void cleanUp() {
        dialog.cleanUp();
        active = false;
    }

    @Override
    public boolean isInvisible() {
        return false;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    public HudContextHelpManager getHudHelpManager() {
        return GameClient.getClientState().getWorldDrawer().getGuiDrawer().getHud().getHelpManager();
    }

    private void updateInteraction() {
        if(GameClient.getClientState().isInFlightMode() && !active) {
            SimpleTransformableSendableObject<?> lookingAt = EntityUtils.getLookingAtWithSizeOffset(true, MAX_INTERACTION_DISTANCE, false);
            if(lookingAt instanceof SegmentController && lookingAt.isNPCFactionControlledAI()) {
                helperContainer = getHudHelpManager().addHelper(KeyboardMappings.ACTIVATE, "Communicate", HudContextHelperContainer.Hos.MOUSE, ContextFilter.NORMAL);
                
            } else {
                helperContainer = null;
            }
        }
    }
}
