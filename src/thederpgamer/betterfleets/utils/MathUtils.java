package thederpgamer.betterfleets.utils;

import javax.vecmath.Vector3f;

/**
 * <Description>
 *
 * @author TheDerpGamer
 * @since 08/28/2021
 */
public class MathUtils {

    public static Vector3f getRotationDiff(Vector3f lookVector, Vector3f dir) {
        Vector3f xPlane = new Vector3f(0.0f, lookVector.y, lookVector.z);
        Vector3f yPlane = new Vector3f(lookVector.x, 0.0f, lookVector.z);
        Vector3f zPlane = new Vector3f(lookVector.x, lookVector.y, 0.0f);
        Vector3f rotationDiff = new Vector3f();
        rotationDiff.x = (float) Math.toDegrees(dir.angle(xPlane));
        rotationDiff.y = (float) Math.toDegrees(dir.angle(yPlane));
        rotationDiff.z = (float) Math.toDegrees(dir.angle(zPlane));
        return rotationDiff;
    }
}
