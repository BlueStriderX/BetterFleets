package thederpgamer.betterfleets.data;

import org.schema.game.common.controller.SegmentController;

import javax.annotation.Nullable;

/**
 * <Description>
 *
 * @author TheDerpGamer
 * @version 1.0 - [01/24/2022]
 */
public class TargetData {

    public SegmentController target;
    public int mode;
    public float distance;

    public TargetData(@Nullable SegmentController target, int mode, float distance) {
        this.target = target;
        this.mode = mode;
        this.distance = distance;
    }
}
