package com.thebombzen.mods.autoswitch.configuration;

import com.thebombzen.mods.thebombzenapi.client.ThebombzenAPIConfigGuiFactory;

public class ConfigGuiFactory extends ThebombzenAPIConfigGuiFactory {
	public ConfigGuiFactory(){
		super(ConfigScreen.class);
	}
}
