package com.thebombzen.mods.autoswitch.configuration;

public enum ToolSelectionMode {
	FAST_STANDARD("Fast Standard"),
	SLOW_STANDARD("Slow Standard"),
	FAST_NONSTANDARD("Fast Nonstandard");
	
	public static ToolSelectionMode parse(String info){
		String line = info.replaceAll("\\s", "").replace("_", "").toLowerCase();
		if (line.equals("faststandard")){
			return FAST_STANDARD;
		} else if (line.equals("slowstandard")){
			return SLOW_STANDARD;
		} else if (line.equals("fastnonstandard")){
			return FAST_NONSTANDARD;
		} else {
			return null;
		}
	}
	
	private String name;
	
	private ToolSelectionMode(String name){
		this.name = name;
	}
	
	public boolean isFast(){
		return this != SLOW_STANDARD;
	}
	
	public boolean isStandard(){
		return this != FAST_NONSTANDARD;
	}
	
	@Override
	public String toString(){
		return name;
	}
}
