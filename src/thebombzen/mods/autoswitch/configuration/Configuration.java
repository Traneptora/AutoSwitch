package thebombzen.mods.autoswitch.configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

import org.lwjgl.input.Keyboard;

import thebombzen.mods.autoswitch.AutoSwitch;
import thebombzen.mods.thebombzenapi.ThebombzenAPI;
import thebombzen.mods.thebombzenapi.configuration.ConfigFormatException;
import thebombzen.mods.thebombzenapi.configuration.ConfigOption;
import thebombzen.mods.thebombzenapi.configuration.SingleMultiBoolean;
import thebombzen.mods.thebombzenapi.configuration.ThebombzenAPIConfiguration;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * This class oversees the configuration of AutoSwitch
 * This uses the basic properties as well as its own advanced configuration
 * @author thebombzen
 */
@SideOnly(Side.CLIENT)
public class Configuration extends ThebombzenAPIConfiguration {

	/**
	 * The current version of the configuration.
	 * 0: AS 4.0.0-4.2.0
	 * 1: AS 4.3.0
	 * 2: AS 4.3.1-4.3.4  // I placed a big bug in the config. Had to bump the revision.
	 * 3: AS 4.4.0+ // More config configurability! :D
	 */
	public static final int CONFIG_VERSION = 3;
	
	public static final int OVERRIDDEN_YES = 1;
	public static final int OVERRIDDEN_NO = -1;
	public static final int NOT_OVERRIDDEN = 0;
	
	public static final ConfigOption TOGGLE_KEY = new ConfigOption(Keyboard.getKeyIndex("F10"), "TOGGLE_KEY", "Toggle Key",
			"This key toggles AutoSwitch.");
	public static final ConfigOption PULSE_KEY = new ConfigOption(Keyboard.getKeyIndex("V"), "PULSE_KEY", "Pulse Key",
			"This key temporarily toggles",
			"AutoSwitch while it's held down.");
	public static final ConfigOption DEFAULT_ENABLED = new ConfigOption(0, true, "DEFAULT_ENABED", "Enabled by default",
			"This option determines whether to",
			"enable AutoSwitch on new worlds",
			"and on worlds AutoSwitch hasn't",
			"been used on before.");
	public static final ConfigOption TOOL_SELECTION_MODE = new ConfigOption("FAST STANDARD", new String[]{"Fast Standard", "Slow Standard", "Fast Nonstandard"},"TOOL_SELECTION_MODE", "Tool Selection Mode",
			"FAST STANDARD picks the best standard tool,",
			"    where faster is better.",
			"SLOW STANDARD picks the best standard tool,",
			"    where slower is better.",
			"FAST NONSTANDARD picks the best tool,",
			"    ignoring what's standard.");
	public static final ConfigOption BLOCKS = new ConfigOption(SingleMultiBoolean.ALWAYS, "BLOCKS", "Use on blocks",
			"Use AutoSwitch when digging blocks.");
	public static final ConfigOption MOBS = new ConfigOption(SingleMultiBoolean.ALWAYS, "MOBS", "Use on mobs",
			"Use AutoSwitch when attacking mobs.");
	public static final ConfigOption SWITCHBACK_BLOCKS = new ConfigOption(SingleMultiBoolean.ALWAYS, "SWITCHBACK_BLOCKS", "Unswitch on blocks",
			"Switch back after digging a block.");
	public static final ConfigOption SWITCHBACK_MOBS = new ConfigOption(SingleMultiBoolean.ALWAYS, "SWITCHBACK_MOBS", "Unswitch on weapons",
			"Switch back after attacking a mob.");
	public static final ConfigOption DEBUG = new ConfigOption(false, "DEBUG", "Debug Logging",
			"Log debug output to",
			".minecraft/mods/AutoSwitch/DEBUG.txt");
	public static final ConfigOption USE_IN_CREATIVE = new ConfigOption(true, "USE_IN_CREATIVE", "Use in creative",
			"Use AutoSwitch when in creative mode");
	public static final ConfigOption TREEFELLER_COMPAT = new ConfigOption(false, "TREEFELLER_COMPAT", "Detect Tree Feller",
				"Automatically detect when",
				"mcMMO Tree Feller is activated",
				"and temporarily set the tool selection",
				"mode to SLOW STANDARD.");
	
	private static int doesYesNoSetContainBlock(Set<? extends BlockItemIdentifier> no, Set<? extends BlockItemIdentifier> yes, Block block, int metadata){
		if (doesSetContainBlock(no, block, metadata)){
			return OVERRIDDEN_NO;
		} else if (doesSetContainBlock(yes, block,  metadata)){
			return OVERRIDDEN_YES;
		} else {
			return NOT_OVERRIDDEN;
		}
	}
	
	private static boolean doesSetContainBlock(Set<? extends BlockItemIdentifier> set, Block block, int metadata){
		for (BlockItemIdentifier test : set) {
			if (test.contains(block, metadata)){
				return true;
			}
		}
		return false;
	}
	
	private static int doesYesNoSetContainToolAndBlock(Set<? extends BlockToolPair> no, Set<? extends BlockToolPair> yes, ItemStack tool, Block block, int metadata){
		if (doesSetContainToolAndBlock(no, tool, block, metadata)){
			return OVERRIDDEN_NO;
		} else if (doesSetContainToolAndBlock(yes, tool, block,  metadata)){
			return OVERRIDDEN_YES;
		} else {
			return NOT_OVERRIDDEN;
		}
	}

	private static boolean doesSetContainToolAndBlock(Set<? extends BlockToolPair> set, ItemStack tool, Block block, int metadata){
		for (BlockToolPair pair : set){
			if (pair.getBlock().contains(block, metadata) && pair.getTool().contains(tool)){
				return true;
			}
		}
		return false;
	}
	
	private Map<BlockItemIdentifier, Integer> customWeapons = new HashMap<BlockItemIdentifier, Integer>();
	private final String defaultConfig;

	private File extraConfigFile;
	private long extraConfigLastModified;
	private Set<BlockItemIdentifier> fortuneNoWorks = new HashSet<BlockItemIdentifier>();
	private Set<BlockItemIdentifier> fortuneWorks = new HashSet<BlockItemIdentifier>();
	private Set<BlockToolPair> standardBlocksAndTools = new HashSet<BlockToolPair>();
	private Set<BlockToolPair> notStandardBlocksAndTools = new HashSet<BlockToolPair>();
	private Set<BlockItemIdentifier> silkTouchNoWorks = new HashSet<BlockItemIdentifier>();
	private Set<BlockItemIdentifier> silkTouchWorks = new HashSet<BlockItemIdentifier>();
	private Set<BlockItemIdentifier> ignoreFortune = new HashSet<BlockItemIdentifier>();
	private Set<BlockItemIdentifier> ignoreSilkTouch = new HashSet<BlockItemIdentifier>();
	private Set<BlockItemIdentifier> fastStandardOverrides = new HashSet<BlockItemIdentifier>();
	private Set<BlockItemIdentifier> slowStandardOverrides = new HashSet<BlockItemIdentifier>();
	private Set<BlockItemIdentifier> fastNonStandardOverrides = new HashSet<BlockItemIdentifier>();
	private Set<BlockToolPair> harvestWorks = new HashSet<BlockToolPair>();
	private Set<BlockToolPair> harvestNoWorks = new HashSet<BlockToolPair>();
	private Set<BlockToolPair> damageableYes = new HashSet<BlockToolPair>();
	private Set<BlockToolPair> damageableNo = new HashSet<BlockToolPair>();

	public Configuration(AutoSwitch autoSwitch) {
		super(autoSwitch);
		extraConfigFile = new File(ThebombzenAPI.sideSpecificUtilities.getMinecraftDirectory() + File.separator + "config" + File.separator + "AutoSwitch_Overrides.txt");
		File oldExtraConfigFile = new File(extraConfigFile.getParentFile(), "AutoSwitch_Overrides.cfg");
		StringBuilder builder = new StringBuilder();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(ThebombzenAPI.getResourceAsStream(autoSwitch, "AutoSwitch_Overrides.txt")));
			String line;
			while (null != (line = reader.readLine())){
				builder.append(line).append(ThebombzenAPI.NEWLINE);
			}
			reader.close();
		} catch (IOException ioe){
			autoSwitch.throwException("Could not read default config!", ioe, true);
		} finally {
			defaultConfig = builder.toString();
		}
		if (oldExtraConfigFile.exists()){
			oldExtraConfigFile.delete();
		}
	}

	@Override
	public ConfigOption[] getAllOptions() {
		return new ConfigOption[]{TOGGLE_KEY, PULSE_KEY, DEFAULT_ENABLED, TOOL_SELECTION_MODE, BLOCKS, MOBS, SWITCHBACK_BLOCKS, SWITCHBACK_MOBS, DEBUG, USE_IN_CREATIVE, TREEFELLER_COMPAT};
	}

	public int getCustomWeaponDamage(ItemStack itemstack) {
		
		if (itemstack == null) {
			return -1;
		}
		
		for (BlockItemIdentifier itemID : customWeapons.keySet()){
			if (itemID.contains(itemstack)){
				return customWeapons.get(itemID);
			}
		}
		
		return -1;
	}

	public File getExtraConfigFile() {
		return extraConfigFile;
	}

	public ToolSelectionMode getToolSelectionMode(Block block, int metadata) {
		if (AutoSwitch.instance.isTreefellerOn() && getBooleanProperty(TREEFELLER_COMPAT)){
			return ToolSelectionMode.SLOW_STANDARD;
		}
		if (this.isSlowStandardOverridden(block, metadata)){
			return ToolSelectionMode.SLOW_STANDARD;
		} else if (this.isFastNonStandardOverridden(block, metadata)){
			return ToolSelectionMode.FAST_NONSTANDARD;
		} else if (this.isFastStandardOverridden(block, metadata)){
			return ToolSelectionMode.FAST_STANDARD;
		}
		return this.getDefaultToolSelectionMode();
	}
	
	public ToolSelectionMode getDefaultToolSelectionMode(){
		ToolSelectionMode mode = ToolSelectionMode.parse(getStringProperty(TOOL_SELECTION_MODE));
		if (mode == null){
			mode = ToolSelectionMode.FAST_STANDARD;
			setToolSelectionMode(mode);
		}
		return mode;
	}

	private boolean isFastNonStandardOverridden(Block block, int metadata){
		return doesSetContainBlock(fastNonStandardOverrides, block, metadata);
	}

	private boolean isFastStandardOverridden(Block block, int metadata){
		return doesSetContainBlock(fastStandardOverrides, block, metadata);
	}
	
	public int getFortuneOverrideState(Block block, int metadata){
		return doesYesNoSetContainBlock(fortuneNoWorks, fortuneWorks, block, metadata);
	}
	
	public boolean isSilkTouchOverriddenToWork(Block block, int metadata) {
		return doesSetContainBlock(silkTouchWorks, block, metadata);
	}
	
	public int getSilkTouchOverrideState(Block block, int metadata){
		return doesYesNoSetContainBlock(silkTouchNoWorks, silkTouchWorks, block, metadata);
	}
	
	private boolean isSlowStandardOverridden(Block block, int metadata){
		return doesSetContainBlock(slowStandardOverrides, block, metadata);
	}
	
	public int getStandardToolOverrideState(ItemStack tool, Block block, int metadata){
		return doesYesNoSetContainToolAndBlock(notStandardBlocksAndTools, standardBlocksAndTools, tool, block, metadata);
	}
	
	public int getHarvestOverrideState(ItemStack tool, Block block, int metadata){
		return doesYesNoSetContainToolAndBlock(harvestNoWorks, harvestWorks, tool, block, metadata);
	}
	
	public int getDamageableOverrideState(ItemStack tool, Block block, int metadata){
		return doesYesNoSetContainToolAndBlock(damageableNo, damageableYes, tool, block, metadata);
	}

	@Override
	protected void loadProperties() throws IOException {
		super.loadProperties();
		AutoSwitch.instance.setToggleKeyCode(DEFAULT_ENABLED.getDefaultToggleIndex(), getKeyCodeProperty(TOGGLE_KEY));
		if (!extraConfigFile.exists()) {
			writeExtraConfig();
			parseConfig(defaultConfig);
			return;
		}
		StringBuilder sb = new StringBuilder();
		BufferedReader reader = new BufferedReader(new FileReader(extraConfigFile));
		String s;
		while (null != (s = reader.readLine())) {
			sb.append(s).append(ThebombzenAPI.NEWLINE);
		}
		reader.close();
		parseConfig(sb.toString());
		extraConfigLastModified = getExtraConfigFile().lastModified();
	}
	
	private void parseBlockToolPairOverride(String line, Set<BlockToolPair> no, Set<BlockToolPair> yes){
		int indexGreaterThan = line.indexOf('>');
		int indexLessThan = line.indexOf('<');
		String toolSub = "";
		String blockSub = "";
		boolean plus = false;
		if (indexGreaterThan > 0 && indexLessThan < 0) {
			toolSub = line.substring(1, indexGreaterThan);
			blockSub = line.substring(indexGreaterThan + 1);
			plus = true;
		} else if (indexLessThan > 0 && indexGreaterThan < 0) {
			toolSub = line.substring(1, indexLessThan);
			blockSub = line.substring(indexLessThan + 1);
			plus = false;
		} else {
			AutoSwitch.instance.forceDebug("Error on line: %s", line);
			AutoSwitch.instance.forceDebug("Error caused by: Expected > or < but not both.");
			return;
		}
		try {
			BlockItemIdentifier block = BlockItemIdentifier.parseBlockItemIdentifier(blockSub);
			BlockItemIdentifier tool = BlockItemIdentifier.parseBlockItemIdentifier(toolSub);
			(plus ? yes : no).add(new BlockToolPair(block, tool));
		} catch (ConfigFormatException e){
			AutoSwitch.instance.forceDebug("Error on line: %s", line);
			AutoSwitch.instance.forceDebug("Error caused by: %s", e.toString());
			AutoSwitch.instance.debugException(e);
		}
	}

	protected void parseConfig(String config) {
		fortuneNoWorks.clear();
		fortuneWorks.clear();
		notStandardBlocksAndTools.clear();
		standardBlocksAndTools.clear();
		silkTouchNoWorks.clear();
		silkTouchWorks.clear();
		customWeapons.clear();
		ignoreFortune.clear();
		ignoreSilkTouch.clear();
		fastStandardOverrides.clear();
		fastNonStandardOverrides.clear();
		slowStandardOverrides.clear();
		harvestWorks.clear();
		harvestNoWorks.clear();
		damageableYes.clear();
		damageableNo.clear();
		Scanner s = new Scanner(config);
		s.useDelimiter(ThebombzenAPI.NEWLINE);
		int version = -1;
		while (s.hasNext()) {
			String line = s.next();
			int index = line.indexOf('#');
			if (index >= 0) {
				line = line.substring(0, index);
			}
			line = line.replaceAll("\\s", "");
			if (line.length() == 0) {
				continue;
			} else if (line.length() < 2) {
				AutoSwitch.instance.forceDebug("Error on line: %s", line);
				AutoSwitch.instance.forceDebug("Error caused by: Line is too short");
				continue;
			}
			char first = line.charAt(0);
			switch (first) {
			case 'R':
			case 'r':
				String sub = line.substring(1);
				try {
					version = ThebombzenAPI.parseInteger(sub);
				} catch (NumberFormatException nfe) {
					version = -1;
				}
				if (version != CONFIG_VERSION) {
					try {
						writeExtraConfig();
					} catch (IOException ioe) {
						mod.throwException("Could not write config file!", ioe,
								true);
					}
					parseConfig(defaultConfig);
					s.close();
					return;
				}
				break;
			case 'T':
			case 't':
				char second = line.charAt(1);
				if (second == '>') {
					try {
						silkTouchWorks.add(BlockItemIdentifier.parseBlockItemIdentifier(line.substring(2)));
					} catch (ConfigFormatException e){
						AutoSwitch.instance.forceDebug("Error on line: %s", line);
						AutoSwitch.instance.forceDebug("Error caused by: %s", e.toString());
						AutoSwitch.instance.debugException(e);
					}
				} else if (second == '<') {
					try {
						if (line.length() < 3){
							throw new ConfigFormatException("Line is too short");
						}
						if (line.charAt(2) == '<'){
							ignoreSilkTouch.add(BlockItemIdentifier.parseBlockItemIdentifier(line.substring(3)));
						} else {
							silkTouchNoWorks.add(BlockItemIdentifier.parseBlockItemIdentifier(line.substring(2)));
						}
						
					} catch (ConfigFormatException e){
						AutoSwitch.instance.forceDebug("Error on line: %s", line);
						AutoSwitch.instance.forceDebug("Error caused by: %s", e.toString());
						AutoSwitch.instance.debugException(e);
					}
							
				} else {
					AutoSwitch.instance.forceDebug("Error on line: %s", line);
					AutoSwitch.instance.forceDebug("Error caused by: Expected < or >. Found %c", second);
					continue;
				}
				break;
			case 'F':
			case 'f':
				second = line.charAt(1);
				if (second == '>') {
					try {
						fortuneWorks.add(BlockItemIdentifier.parseBlockItemIdentifier(line.substring(2)));
					} catch (ConfigFormatException e){
						AutoSwitch.instance.forceDebug("Error on line: %s", line);
						AutoSwitch.instance.forceDebug("Error caused by: %s", e.toString());
					}
				} else if (second == '<') {
					try {
						if (line.length() < 3){
							throw new ConfigFormatException("Line is too short");
						}
						if (line.charAt(2) == '<'){
							ignoreFortune.add(BlockItemIdentifier.parseBlockItemIdentifier(line.substring(3)));
						} else {
							fortuneNoWorks.add(BlockItemIdentifier.parseBlockItemIdentifier(line.substring(2)));
						}
					} catch (ConfigFormatException e){
						AutoSwitch.instance.forceDebug("Error on line: %s", line);
						AutoSwitch.instance.forceDebug("Error caused by: %s", e.toString());
						AutoSwitch.instance.debugException(e);
					}
				} else {
					AutoSwitch.instance.forceDebug("Error on line: %s", line);
					AutoSwitch.instance.forceDebug("Error caused by: Expected < or >. Found %c", second);
					continue;
				}
				break;
			case 'S':
			case 's':
				this.parseBlockToolPairOverride(line, notStandardBlocksAndTools, standardBlocksAndTools);
				break;
			case 'H':
			case 'h':
				this.parseBlockToolPairOverride(line, harvestNoWorks, harvestWorks);
				break;
			case 'D':
			case 'd':
				this.parseBlockToolPairOverride(line, damageableNo, damageableYes);
				break;
			case 'M':
			case 'm':
				int indexE = line.indexOf('=');
				if (indexE < 0 || indexE >= line.length() - 1) {
					AutoSwitch.instance.forceDebug("Error on line: %s", line);
					AutoSwitch.instance.forceDebug("Error caused by: Expected = in middle of line.");
					continue;
				}
				String blockSub = line.substring(1, indexE);
				String typeSub = line.substring(indexE + 1).toLowerCase();
				//System.out.println(blockSub + "=" + typeSub);
				try {
					BlockItemIdentifier block = BlockItemIdentifier.parseBlockItemIdentifier(blockSub);
					Set<BlockItemIdentifier> setToAdd;
					if (typeSub.contains("fast")){
						if (typeSub.contains("non")){
							setToAdd = fastNonStandardOverrides;
						} else {
							setToAdd = fastStandardOverrides;
						}
					} else if (typeSub.contains("slow")){
						setToAdd = slowStandardOverrides;
					} else {
						throw new ConfigFormatException("Invalid Tool Selection Mode.");
					}
					setToAdd.add(block);
				} catch (ConfigFormatException e){
					AutoSwitch.instance.forceDebug("Error on line: %s", line);
					AutoSwitch.instance.forceDebug("Error caused by: %s", e.toString());
					AutoSwitch.instance.debugException(e);
					continue;
				}
				break;
			case 'W':
			case 'w':
				indexE = line.lastIndexOf('=');
				if (indexE < 0 || indexE >= line.length() - 1) {
					AutoSwitch.instance.forceDebug("Error on line: %s", line);
					AutoSwitch.instance.forceDebug("Error caused by: Expected = in middle of line.");
					continue;
				}
				sub = line.substring(1, indexE);
				String damageString = line.substring(indexE + 1);
				Integer damage = null;
				try {
					damage = ThebombzenAPI.parseInteger(damageString);
				} catch (NumberFormatException nfe) {
					AutoSwitch.instance.forceDebug("Error on line: %s", line);
					AutoSwitch.instance.forceDebug("Error caused by: Invalid Number: %s", nfe.toString());
					continue;
				}
				try {
					BlockItemIdentifier tool = BlockItemIdentifier.parseBlockItemIdentifier(sub);
					customWeapons.put(tool, damage);
				} catch (ConfigFormatException e){
					AutoSwitch.instance.forceDebug("Error on line: %s", line);
					AutoSwitch.instance.forceDebug("Error caused by: %s", e.toString());
					AutoSwitch.instance.debugException(e);
				}
				break;
			default:
				AutoSwitch.instance.forceDebug("Error on line: %s", line);
				AutoSwitch.instance.forceDebug("Error caused by: Unrecognized first character.");
			}
		}
		if (version != CONFIG_VERSION) {
			try {
				writeExtraConfig();
			} catch (IOException ioe) {
				mod.throwException("Could not write config file!", ioe, true);
			} finally {
				parseConfig(defaultConfig);
			}
		}
		s.close();
		//System.out.println(notStandardBlocksAndTools.toString());
	}

	@Override
	protected void setPropertyWithoutSave(ConfigOption option,
			String value) {
		super.setPropertyWithoutSave(option, value);
		if (option.equals(TOGGLE_KEY)) {
			mod.setToggleKeyCode(0, Keyboard.getKeyIndex(value));
		}
	}

	public void setToolSelectionMode(ToolSelectionMode mode) {
		setProperty(TOOL_SELECTION_MODE, mode.toString());
	}

	public boolean shouldIgnoreFortune(Block block, int metadata){
		return doesSetContainBlock(ignoreFortune, block, metadata);
	}

	public boolean shouldIgnoreSilkTouch(Block block, int metadata){
		return doesSetContainBlock(ignoreSilkTouch, block, metadata);
	}
	
	@Override
	public boolean shouldRefreshConfig() {
		if (super.shouldRefreshConfig()) {
			return true;
		}
		if (extraConfigLastModified != getExtraConfigFile().lastModified()) {
			return true;
		} else {
			return false;
		}
	}

	private void writeExtraConfig() throws IOException {
		FileWriter writer = new FileWriter(extraConfigFile);
		writer.write(defaultConfig);
		writer.flush();
		writer.close();
	}
	
}
