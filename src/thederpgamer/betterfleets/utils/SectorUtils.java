package thederpgamer.betterfleets.utils;

import com.bulletphysics.linearmath.Transform;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.server.data.ServerConfig;

/**
 * <Description>
 *
 * @author TheDerpGamer
 * @since 08/28/2021
 */
public class SectorUtils {

    public static void transformToSector(Transform transform, Vector3i currentSector, Vector3i targetSector) {
        int sectorSize = (int) ServerConfig.SECTOR_SIZE.getCurrentState();
        Vector3i diff = new Vector3i(currentSector);
        diff.sub(targetSector);
        if(diff.x > 0) transform.origin.x += sectorSize * diff.x;
        else if(diff.x < 0) transform.origin.x -= sectorSize * diff.x;
        if(diff.y > 0) transform.origin.y += sectorSize * diff.y;
        else if(diff.y < 0) transform.origin.y -= sectorSize * diff.y;
        if(diff.z > 0) transform.origin.z += sectorSize * diff.z;
        else if(diff.z < 0) transform.origin.z -= sectorSize * diff.z;
    }
}
