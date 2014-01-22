package thebombzen.mods.thebombzenapi;

import java.io.File;

import net.minecraft.server.MinecraftServer;

public class CommonProxy {
	public File getMinecraftFolder() {
		return (File) ThebombzenAPI.callPrivateMethod(
				MinecraftServer.getServer(), MinecraftServer.class,
				new String[] { "getDataDirectory", "func_71238_n", "n" },
				new Class<?>[0]);
	}
}
