package thederpgamer.betterfleets.systems.remotecontrol;

import api.common.GameServer;
import api.utils.game.module.util.SimpleDataStorageMCModule;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.schine.graphicsengine.core.Timer;
import thederpgamer.betterfleets.BetterFleets;
import thederpgamer.betterfleets.data.system.RemoteControlSystemData;
import thederpgamer.betterfleets.element.ElementManager;

import java.util.Map;

/**
 * <Description>
 *
 * @author TheDerpGamer
 * @version 1.0 - [03/07/2022]
 */
public class RemoteControlModule extends SimpleDataStorageMCModule {

	public static final float UPDATE_TIMER = 15.0f;
	private float timer;

	public RemoteControlModule(SegmentController segmentController, ManagerContainer<?> managerContainer) {
		super(segmentController, managerContainer, BetterFleets.getInstance(), ElementManager.getBlock("AI Remote Controller").getId());
		this.timer = UPDATE_TIMER;
		if(data == null || !(data instanceof RemoteControlSystemData)) data = new RemoteControlSystemData();
	}

	@Override
	public void handle(Timer timer) {
		if(this.timer <= 0) {
			RemoteControlSystemData systemData = getSystemData();
			for(Map.Entry<Long, String> entry : systemData.controllerMap.entrySet()) {
				SegmentController targetEntity = getTargetEntity(entry.getKey());
				if(targetEntity == null || targetEntity.getFactionId() != segmentController.getFactionId() || targetEntity.getFactionId() == FactionManager.ID_NEUTRAL || segmentController.getFactionId() == FactionManager.ID_NEUTRAL) {

				} else {

				}
			}
			this.timer = UPDATE_TIMER;
		} else this.timer -= timer.getDelta();
	}

	@Override
	public String getName() {
		return "Remote Controller Module";
	}

	private RemoteControlSystemData getSystemData() {
		if(data == null || !(data instanceof RemoteControlSystemData)) data = new RemoteControlSystemData();
		return (RemoteControlSystemData) data;
	}

	public SegmentController getTargetEntity(long controllerIndex) {
		return GameServer.getServerState().getSegmentControllersByName().get(getSystemData().controllerMap.get(controllerIndex));
	}

	public void setTargetEntity(long controllerIndex, SegmentController targetEntity) {
		getSystemData().controllerMap.remove(controllerIndex);
		getSystemData().controllerMap.put(controllerIndex, targetEntity.getName());
	}
}
