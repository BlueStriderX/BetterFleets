package thederpgamer.betterfleets.gui.conversation;

import api.utils.gui.GUIInputDialog;
import api.utils.gui.GUIInputDialogPanel;
import org.schema.schine.graphicsengine.core.Drawable;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.input.InputState;

/**
 * <Description>
 *
 * @author TheDerpGamer
 * @version 1.0 - [09/12/2021]
 */
public class NPCConversationDialog implements Drawable {

    private NPCConversationInputDialog inputDialog;
    private boolean initialized = false;

    @Override
    public void onInit() {
        inputDialog = new NPCConversationInputDialog();
        initialized = true;
    }

    @Override
    public void draw() {
        if(!initialized) onInit();
    }

    @Override
    public void cleanUp() {
        if(initialized) {
            inputDialog.deactivate();
            getInputPanel().cleanUp();
        }
    }

    @Override
    public boolean isInvisible() {
        return false;
    }

    public NPCConversationInputDialog getInputDialog() {
        if(!initialized) onInit();
        return inputDialog;
    }

    public NPCConversationInputPanel getInputPanel() {
        if(!initialized) onInit();
        return (NPCConversationInputPanel) inputDialog.getInputPanel();
    }

    public static class NPCConversationInputDialog extends GUIInputDialog {

        @Override
        public NPCConversationInputPanel createPanel() {
            return new NPCConversationInputPanel(getState(), this);
        }

        @Override
        public void callback(GUIElement callingElement, MouseEvent mouseEvent) {
            if(!isOccluded()) {
                if(mouseEvent.pressedLeftMouse()) {
                    switch((String) callingElement.getUserPointer()) {
                        case "X":
                        case "CANCEL":
                            //Deactivate dialog
                            deactivate();
                            break;
                        case "OK":
                            //Do stuff
                            break;
                    }
                }
            }
        }
    }

    public static class NPCConversationInputPanel extends GUIInputDialogPanel {

        private boolean initialized = false;

        public NPCConversationInputPanel(InputState inputState, GUICallback guiCallback) {
            super(inputState, "NPC_Conversation_Input_Dialog", "", "", 800, 400, guiCallback);
        }

        @Override
        public void onInit() {
            super.onInit();
            initialized = true;
        }

        @Override
        public void draw() {
            if(!initialized) onInit();
            super.draw();
        }
    }
}
