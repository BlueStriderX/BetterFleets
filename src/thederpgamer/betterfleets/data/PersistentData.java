package thederpgamer.betterfleets.data;

import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;

import java.io.IOException;

/**
 * Base interface for persistent mod data.
 *
 * @author TheDerpGamer
 * @version 1.0 - [09/13/2021]
 */
public interface PersistentData {

	void deserialize(PacketReadBuffer readBuffer) throws IOException;
	void serialize(PacketWriteBuffer writeBuffer) throws IOException;
}
