package thebombzen.mods.autoswitch;

import thebombzen.mods.thebombzenapi.ThebombzenAPIConfigOption;

public enum ConfigOption implements ThebombzenAPIConfigOption {

	DEFAULT_ENABLED(0, BOOLEAN, "true", "Enabled by default",
		"This option determines whether to",
		"enable AutoSwitch on new worlds",
		"and on worlds AutoSwitch hasn't",
		"been used on before."),
	TOOL_SELECTION_MODE(-1, FINITE_STRING, "FAST STANDARD", "Tool Selection Mode",
		"FAST STANDARD picks the best standard tool,",
		"    where faster is better.",
		"SLOW STANDARD picks the best standard tool,",
		"    where slower is better.",
		"FAST NONSTANDARD picks the best tool,",
		"    ignoring what's standard."),
	BLOCKS_SP(-1, BOOLEAN, "true", "Use on blocks in SP",
		"This option determines whether to use",
		"AutoSwitch on blocks in singleplayer."),
	BLOCKS_MP(-1, BOOLEAN, "true", "Use on blocks in MP",
		"This option determines whether to use",
		"AutoSwitch on blocks in multiplayer."),
	MOBS_SP(-1, BOOLEAN, "true", "Use on entities in SP",
		"This option determines whether to use",
		"AutoSwitch on entities in singleplayer."),
	MOBS_MP(-1, BOOLEAN, "true", "Use on entities in MP",
		"This option determines whether to use",
		"AutoSwitch on entities in multiplayer."),
	TOGGLE_KEY(-1, KEY,	"F10", "AutoSwitch Toggle Key",
		"This key toggles AutoSwitch."),
	PULSE_KEY(-1, KEY, "V", "AutoSwitch Pulse Key",
		"This key temporarily toggles",
		"AutoSwitch while it's held down."),
	DEBUG(-1, BOOLEAN, "false", "Debug Logging",
		"Log debug output to",
		".minecraft/mods/AutoSwitch/DEBUG.txt"),
	USE_IN_CREATIVE(-1, BOOLEAN, "true", "Use in creative",
		"Use AutoSwitch when in creative mode");

	private static final String[] TOOL_SELECTION_MODE_OPTIONS = {
			"FAST STANDARD", "SLOW STANDARD", "FAST NONSTANDARD" };

	private int defaultToggleIndex;
	private String defaultValue;
	private String[] info;
	private int optionType;
	private String shortInfo;

	private ConfigOption(int defaultToggleIndex, int optionType,
			String defaultValue, String shortInfo, String... info) {
		this.optionType = optionType;
		this.defaultToggleIndex = defaultToggleIndex;
		this.defaultValue = defaultValue;
		this.shortInfo = shortInfo;
		this.info = info;
	}

	@Override
	public int getDefaultToggleIndex() {
		return this.defaultToggleIndex;
	}

	@Override
	public String getDefaultValue() {
		return defaultValue;
	}

	@Override
	public String[] getFiniteStringOptions() {
		if (optionType != FINITE_STRING) {
			throw new UnsupportedOperationException(
					"This isn't a finite String!");
		}
		if (this.equals(TOOL_SELECTION_MODE)) {
			return TOOL_SELECTION_MODE_OPTIONS;
		}
		return null;
	}

	@Override
	public String[] getInfo() {
		return info;
	}

	@Override
	public int getOptionType() {
		return optionType;
	}

	@Override
	public String getShortInfo() {
		return shortInfo;
	}

}
