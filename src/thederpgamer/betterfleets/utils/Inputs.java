package thederpgamer.betterfleets.utils;

/**
 * <Description>
 *
 * @author TheDerpGamer
 * @since 06/14/2021
 */
public class Inputs {

    public enum MouseButtons {
        NONE(-1),
        LEFT_MOUSE(0),
        RIGHT_MOUSE(1);

        public int id;

        MouseButtons(int id) {
            this.id = id;
        }

        public MouseButtons getFromId(int id) {
            for(MouseButtons mouseButton : values()) {
                if(mouseButton.id == id) return mouseButton;
            }
            return NONE;
        }
    }
}
