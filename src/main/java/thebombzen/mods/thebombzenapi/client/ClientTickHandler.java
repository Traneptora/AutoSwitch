package thebombzen.mods.thebombzenapi.client;

import java.util.EnumSet;

import thebombzen.mods.thebombzenapi.ThebombzenAPI;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class ClientTickHandler implements ITickHandler {

	@Override
	public String getLabel() {
		return "thebombzen.mods.thebombzenapi.client.ClientTickHandler";
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
		if (type.contains(TickType.CLIENT)) {
			ThebombzenAPI.instance.clientTick();
		}
	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.CLIENT);
	}

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {

	}

}
