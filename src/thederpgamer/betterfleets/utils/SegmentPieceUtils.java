package thederpgamer.betterfleets.utils;

import it.unimi.dsi.fastutil.longs.LongIterator;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.PositionControl;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ControlElementMapper;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.game.common.util.FastCopyLongOpenHashSet;

import java.util.ArrayList;

/**
 * [Description]
 *
 * @author TheDerpGamer (MrGoose#0027)
 */
public class SegmentPieceUtils {

	public static ArrayList<SegmentPiece> getControlledPiecesMatching(SegmentPiece segmentPiece, short type) {
		ArrayList<SegmentPiece> controlledPieces = new ArrayList<>();
		PositionControl control = segmentPiece.getSegmentController().getControlElementMap().getDirectControlledElements(type, segmentPiece.getAbsolutePos(new Vector3i()));
		if(control != null) {
			for(long l : control.getControlMap().toLongArray()) {
				SegmentPiece p = segmentPiece.getSegmentController().getSegmentBuffer().getPointUnsave(l);
				if(p != null && p.getType() == type) controlledPieces.add(p);
			}
		}
		return controlledPieces;
	}

	public static ArrayList<SegmentPiece> getControlledPieces(SegmentPiece segmentPiece) {
		ArrayList<SegmentPiece> controlledPieces = new ArrayList<>();
		ControlElementMapper controlElementMapper = segmentPiece.getSegmentController().getControlElementMap().getControllingMap();
		if(controlElementMapper.containsKey(segmentPiece.getAbsoluteIndex())) {
			for(FastCopyLongOpenHashSet longs : controlElementMapper.get(segmentPiece.getAbsoluteIndex()).values()) {
				LongIterator longIterator = longs.iterator();
				while(longIterator.hasNext()) {
					try {
						controlledPieces.add(segmentPiece.getSegmentController().getSegmentBuffer().getPointUnsave(longIterator.nextLong()));
					} catch(Exception exception) {
						exception.printStackTrace();
					}
				}
			}
		}
		return controlledPieces;
	}

	public static Inventory getInventory(SegmentPiece segmentPiece) {
		return ((ManagedSegmentController<?>) segmentPiece.getSegmentController()).getManagerContainer().getInventory(segmentPiece.getAbsoluteIndex());
	}
}
