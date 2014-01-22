package thebombzen.mods.thebombzenapi.client;

import java.io.File;

import net.minecraft.client.Minecraft;
import thebombzen.mods.thebombzenapi.CommonProxy;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {
	@Override
	public File getMinecraftFolder() {
		return Minecraft.getMinecraft().mcDataDir;
	}
}
