package thebombzen.mods.autoswitch.configuration;

import thebombzen.mods.thebombzenapi.client.ThebombzenAPIConfigGuiFactory;

public class ConfigGuiFactory extends ThebombzenAPIConfigGuiFactory {
	public ConfigGuiFactory(){
		super(ConfigScreen.class);
	}
}
