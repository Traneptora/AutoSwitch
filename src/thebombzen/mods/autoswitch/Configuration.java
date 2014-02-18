package thebombzen.mods.autoswitch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

import org.lwjgl.input.Keyboard;

import thebombzen.mods.thebombzenapi.ThebombzenAPI;
import thebombzen.mods.thebombzenapi.ThebombzenAPIConfigOption;
import thebombzen.mods.thebombzenapi.ThebombzenAPIConfiguration;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * This class oversees the configuration of AutoSwitch
 * This uses the basic properties as well as its own advanced configuration
 * @author thebombzen
 */
@SideOnly(Side.CLIENT)
public class Configuration extends ThebombzenAPIConfiguration<ConfigOption> {

	/**
	 * The current version of the configuration.
	 * 0: AS 4.0.0-4.2.0
	 * 1: AS 4.3.0
	 * 2: AS 4.3.1+  // I placed a big bug in the config. Had to bump the revision.
	 */
	public static final int CONFIG_VERSION = 2;
	
	public static final int FAST_STANDARD = 0;
	public static final int SLOW_STANDARD = 1;
	public static final int FAST_NONSTANDARD = 2;
	
	
	private static boolean doesSetContainBlock(Set<BlockItemIdentifier> set, Block block, int metadata){
		for (BlockItemIdentifier test : set) {
			if (test.contains(block, metadata)){
				return true;
			}
		}
		return false;
	}
	
	public static Configuration getConfiguration() {
		return (Configuration) AutoSwitch.instance.getConfiguration();
	}

	private Map<BlockItemIdentifier, Integer> customWeapons = new HashMap<BlockItemIdentifier, Integer>();
	private final String defaultConfig;

	private File extraConfigFile;
	private long extraConfigLastModified;
	private Set<BlockItemIdentifier> fortuneNoWorks = new HashSet<BlockItemIdentifier>();
	private Set<BlockItemIdentifier> fortuneWorks = new HashSet<BlockItemIdentifier>();
	private Set<BlockToolPair> notStandardBlocksAndTools = new HashSet<BlockToolPair>();
	private Set<BlockItemIdentifier> silkTouchNoWorks = new HashSet<BlockItemIdentifier>();
	private Set<BlockItemIdentifier> silkTouchWorks = new HashSet<BlockItemIdentifier>();

	private Set<BlockToolPair> standardBlocksAndTools = new HashSet<BlockToolPair>();

	public Configuration(AutoSwitch autoSwitch) {
		super(autoSwitch, ConfigOption.class);
		extraConfigFile = new File(ThebombzenAPI.sideSpecificUtilities.getMinecraftDirectory() + File.separator + "config" + File.separator + "AutoSwitch_Overrides.txt");
		File oldExtraConfigFile = new File(extraConfigFile.getParentFile(), "AutoSwitch_Overrides.cfg");
		StringBuilder builder = new StringBuilder();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(ThebombzenAPI.getResourceAsStream(autoSwitch, "AutoSwitch_Overrides.txt")));
			String line;
			while (null != (line = reader.readLine())){
				builder.append(line).append(ThebombzenAPI.newLine);
			}
			reader.close();
		} catch (IOException ioe){
			autoSwitch.throwException("Could not read default config!", ioe, true);
		} finally {
			defaultConfig = builder.toString();
		}
		try {
			PrintWriter w = new PrintWriter(new FileWriter(oldExtraConfigFile));
			w.println("The AutoSwitch overrides file has moved to AutoSwitch_Overrides.txt");
			w.close();
		} catch (IOException ioe){
			autoSwitch.throwException("Failed to fix redirect old config.", ioe, false);
		}
	}

	public int getCustomWeaponDamage(ItemStack itemstack) {
		
		if (itemstack == null) {
			return -1;
		}
		
		UniqueIdentifier id = GameRegistry.findUniqueIdentifierFor(itemstack.getItem());
		
		for (BlockItemIdentifier itemID : customWeapons.keySet()){
			if (itemID.contains(id.modId, id.name, itemstack.getItemDamage())){
				return customWeapons.get(itemID);
			}
		}
		
		return -1;
	}

	public File getExtraConfigFile() {
		return extraConfigFile;
	}

	public int getPulseKeyCode() {
		return Keyboard.getKeyIndex(getProperty(ConfigOption.PULSE_KEY));
	}
	
	public int getToggleKeyCode() {
		return Keyboard.getKeyIndex(getProperty(ConfigOption.TOGGLE_KEY));
	}

	public int getToolSelectionMode() {
		int toolSelectionMode = FAST_STANDARD;
		if (getProperty(ConfigOption.TOOL_SELECTION_MODE).equalsIgnoreCase(
				"Fast Standard")) {
			toolSelectionMode = FAST_STANDARD;
		} else if (getProperty(ConfigOption.TOOL_SELECTION_MODE)
				.equalsIgnoreCase("Slow Standard")) {
			toolSelectionMode = SLOW_STANDARD;
		} else if (getProperty(ConfigOption.TOOL_SELECTION_MODE)
				.equalsIgnoreCase("Fast Nonstandard")) {
			toolSelectionMode = FAST_NONSTANDARD;
		} else {
			setToolSelectionMode(FAST_STANDARD);
		}
		return toolSelectionMode;
	}

	public boolean isFortuneOverriddenToNotWork(Block block, int metadata) {
		return doesSetContainBlock(fortuneNoWorks, block, metadata);
	}

	public boolean isFortuneOverriddenToWork(Block block, int metadata) {
		return doesSetContainBlock(fortuneWorks, block, metadata);
	}

	public boolean isSilkTouchOverriddenToNotWork(Block block, int metadata) {
		return doesSetContainBlock(silkTouchNoWorks, block, metadata);
	}

	public boolean isSilkTouchOverriddenToWork(Block block, int metadata) {
		return doesSetContainBlock(silkTouchWorks, block, metadata);
	}

	public boolean isToolOverriddenAsNotStandardOnBlock(ItemStack tool, Block block, int metadata) {
		for (BlockToolPair pair : notStandardBlocksAndTools) {
			if (pair.getBlock().contains(block, metadata) && pair.getTool().contains(tool)){
				return true;
			}
		}
		return false;
	}

	public boolean isToolOverriddenAsStandardOnBlock(ItemStack tool, Block block, int metadata) {
		for (BlockToolPair pair : standardBlocksAndTools) {
			if (pair.getBlock().contains(block, metadata) && pair.getTool().contains(tool)){
				return true;
			}
		}
		return false;
	}

	@Override
	protected void loadProperties() throws IOException {
		super.loadProperties();
		AutoSwitch.instance.setToggleKeyCode(0, getToggleKeyCode());
		if (!extraConfigFile.exists()) {
			writeExtraConfig();
			parseConfig(defaultConfig);
			return;
		}
		StringBuilder sb = new StringBuilder();
		BufferedReader reader = new BufferedReader(new FileReader(extraConfigFile));
		String s;
		while (null != (s = reader.readLine())) {
			sb.append(s).append(ThebombzenAPI.newLine);
		}
		reader.close();
		parseConfig(sb.toString());
		extraConfigLastModified = getExtraConfigFile().lastModified();
	}

	protected void parseConfig(String config) {
		fortuneNoWorks.clear();
		fortuneWorks.clear();
		notStandardBlocksAndTools.clear();
		silkTouchNoWorks.clear();
		silkTouchWorks.clear();
		standardBlocksAndTools.clear();
		customWeapons.clear();
		Scanner s = new Scanner(config);
		s.useDelimiter(ThebombzenAPI.newLine);
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
				continue;
			}
			char first = line.charAt(0);
			switch (first) {
			case 'R':
			case 'r':
				String sub = line.substring(1);
				try {
					version = Integer.parseInt(sub);
				} catch (NumberFormatException nfe) {
					version = -1;
				}
				if (version != CONFIG_VERSION) {
					try {
						writeExtraConfig();
					} catch (IOException ioe) {
						mod.throwException("Could not write config file!", ioe,
								true);
					} finally {
						parseConfig(defaultConfig);
					}
					s.close();
					return;
				}
				break;
			case 'T':
			case 't':
				char second = line.charAt(1);
				if (second == '>') {
					silkTouchWorks.add(BlockItemIdentifier.parseBlockItemIdentifier(line.substring(2)));
				} else if (second == '<') {
					silkTouchNoWorks
							.add(BlockItemIdentifier.parseBlockItemIdentifier(line.substring(2)));
				} else {
					AutoSwitch.instance.forceDebug("Error on line: %s", line);
					continue;
				}
				break;
			case 'F':
			case 'f':
				second = line.charAt(1);
				if (second == '>') {
					fortuneWorks.add(BlockItemIdentifier.parseBlockItemIdentifier(line.substring(2)));
				} else if (second == '<') {
					fortuneNoWorks.add(BlockItemIdentifier.parseBlockItemIdentifier(line.substring(2)));
				} else {
					AutoSwitch.instance.forceDebug("Error on line: %s", line);
					continue;
				}
				break;
			case 'S':
				int indexP = line.indexOf('>');
				int indexM = line.indexOf('<');
				String toolSub = "";
				String blockSub = "";
				boolean plus = false;
				if (indexP != -1 && (indexM == -1 || indexP < indexM)) {
					toolSub = line.substring(1, indexP);
					blockSub = line.substring(indexP + 1);
					plus = true;
				} else if (indexM != -1 && (indexP == -1 || indexM < indexP)) {
					toolSub = line.substring(1, indexM);
					blockSub = line.substring(indexM + 1);
					plus = false;
				} else {
					AutoSwitch.instance.forceDebug("Error on line: %s", line);
					continue;
				}
				BlockItemIdentifier block = BlockItemIdentifier.parseBlockItemIdentifier(blockSub);
				BlockItemIdentifier tool = BlockItemIdentifier.parseBlockItemIdentifier(toolSub);
				(plus ? standardBlocksAndTools : notStandardBlocksAndTools).add(new BlockToolPair(block, tool));
				break;
			case 'W':
				indexP = line.lastIndexOf('>');
				if (indexP < 0 || indexP >= line.length() - 1) {
					AutoSwitch.instance.forceDebug("Error on line: %s", line);
					continue;
				}
				sub = line.substring(1, indexP);
				String damageString = line.substring(indexP);
				Integer damage = null;
				try {
					damage = ThebombzenAPI.parseInteger(damageString);
				} catch (NumberFormatException nfe) {
					AutoSwitch.instance.forceDebug("Error on line: %s", line);
					continue;
				}
				tool = BlockItemIdentifier.parseBlockItemIdentifier(sub);
				customWeapons.put(tool, damage);
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
		System.out.println(notStandardBlocksAndTools.toString());
	}

	@Override
	protected void setPropertyWithoutSave(ThebombzenAPIConfigOption option,
			String value) {
		super.setPropertyWithoutSave(option, value);
		if (option.equals(ConfigOption.TOGGLE_KEY)) {
			mod.setToggleKeyCode(0, Keyboard.getKeyIndex(value));
		}
	}

	public void setToolSelectionMode(int mode) {
		if (mode == FAST_STANDARD) {
			setProperty(ConfigOption.TOOL_SELECTION_MODE, "FAST STANDARD");
		} else if (mode == SLOW_STANDARD) {
			setProperty(ConfigOption.TOOL_SELECTION_MODE, "SLOW STANDARD");
		} else if (mode == FAST_NONSTANDARD) {
			setProperty(ConfigOption.TOOL_SELECTION_MODE, "FAST NONSTANDARD");
		} else {
			this.setToolSelectionMode(FAST_STANDARD);
		}
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
