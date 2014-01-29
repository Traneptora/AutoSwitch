package thebombzen.mods.autoswitch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.lwjgl.input.Keyboard;

import thebombzen.mods.thebombzenapi.ThebombzenAPI;
import thebombzen.mods.thebombzenapi.ThebombzenAPIConfigOption;
import thebombzen.mods.thebombzenapi.ThebombzenAPIConfiguration;

public class Configuration extends ThebombzenAPIConfiguration<ConfigOption> {

	public static final int CONFIG_VERSION = 0;
	public static final int FAST_NONSTANDARD = 2;
	public static final int FAST_STANDARD = 0;
	public static final int SLOW_STANDARD = 1;
	private Map<Integer, Integer> customWeapons = new HashMap<Integer, Integer>();
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
		extraConfigFile = new File(new File(
				ThebombzenAPI.proxy.getMinecraftDirectory(), "config"),
				"AutoSwitch_Overrides.cfg");
		StringBuilder dcb = new StringBuilder();
		dcb.append(
				"# Use this file to override whether AutoSwitch thinks silk touch or fortune work on a block")
				.append(ThebombzenAPI.newLine);
		dcb.append("# to override what AutoSwitch considers a standard tool")
				.append(ThebombzenAPI.newLine);
		dcb.append("# or to provide weapon overrides to AutoSwitch").append(
				ThebombzenAPI.newLine);
		dcb.append("").append(ThebombzenAPI.newLine);
		dcb.append("# Lines beginning with # are ignored").append(
				ThebombzenAPI.newLine);
		dcb.append("").append(ThebombzenAPI.newLine);
		dcb.append("# Config version number:").append(ThebombzenAPI.newLine);
		dcb.append(
				"# If this is not found or does not match the current number, AutoSwitch will replace your config with the default one.")
				.append(ThebombzenAPI.newLine);
		dcb.append("R0").append(ThebombzenAPI.newLine);
		dcb.append("").append(ThebombzenAPI.newLine);
		dcb.append("# ====== HOW TO SPECIFY BLOCKS ======").append(
				ThebombzenAPI.newLine);
		dcb.append("# To specify a block, use one of five forms:").append(
				ThebombzenAPI.newLine);
		dcb.append("").append(ThebombzenAPI.newLine);
		dcb.append("# IID, as in I5. This is all blocks with the specified ID.")
				.append(ThebombzenAPI.newLine);
		dcb.append(
				"# IID:Damage, as in I35:0. This is all blocks with the specified ID and damage value.")
				.append(ThebombzenAPI.newLine);
		dcb.append(
				"# MID, as in M5. This is all blocks that have the same material as the number after the M. M5 has all blocks that have the same material as the block with ID 5 (planks), or all wooden blocks.")
				.append(ThebombzenAPI.newLine);
		dcb.append(
				"# CID, as in C12. This is all blocks that have the same behavior as the number after the C. C12 has all blocks that have the same behavior as the block with ID 12 (sand), or all gravity-affected blocks.")
				.append(ThebombzenAPI.newLine);
		dcb.append(
				"# VID, as in V278. This is all blocks that can be quickly dug by the item after the V. V278 has all blocks that a diamond pickaxe (ID 278) can dig quickly.")
				.append(ThebombzenAPI.newLine);
		dcb.append("").append(ThebombzenAPI.newLine);
		dcb.append(
				"# Note that when specifying a block with C, if the block has no special behavior such as Cobblestone or Endstone, this will get all blocks.")
				.append(ThebombzenAPI.newLine);
		dcb.append("").append(ThebombzenAPI.newLine);
		dcb.append("# ====== HOW TO SPECIFY ITEMS =======").append(
				ThebombzenAPI.newLine);
		dcb.append("# To specify an item, use one of three forms:").append(
				ThebombzenAPI.newLine);
		dcb.append("").append(ThebombzenAPI.newLine);
		dcb.append(
				"# IID as in I278. This is all items with the specified ID (I278 gives all diamond pickaxes)")
				.append(ThebombzenAPI.newLine);
		dcb.append(
				"# DID as in D278. This is all items with the same behavior as the item after the D. D278 is all items that have the same behavior as a diamond pickaxe (ID 278) which means all pickaxes.")
				.append(ThebombzenAPI.newLine);
		dcb.append(
				"# BID as in B17. This is all items that can quickly dig the block after the B. B17 is all items that can quickly cut logs (ID 17). ")
				.append(ThebombzenAPI.newLine);
		dcb.append("").append(ThebombzenAPI.newLine);
		dcb.append(
				"# Note that specifying a weapon with B is supported but rather useless: I and D are recommended for weapons.")
				.append(ThebombzenAPI.newLine);
		dcb.append("").append(ThebombzenAPI.newLine);
		dcb.append("# ========= SILK TOUCH =============").append(
				ThebombzenAPI.newLine);
		dcb.append(
				"# AutoSwitch normally calculates if silk touch works on a block")
				.append(ThebombzenAPI.newLine);
		dcb.append(
				"# To tell AutoSwitch that silk touch works on a block, add the line ")
				.append(ThebombzenAPI.newLine);
		dcb.append("# T + block").append(ThebombzenAPI.newLine);
		dcb.append(
				"# Similarly, to tell AutoSwitch that silk touch does not work on a block, add the line")
				.append(ThebombzenAPI.newLine);
		dcb.append("# T - block").append(ThebombzenAPI.newLine);
		dcb.append("# For example, use").append(ThebombzenAPI.newLine);
		dcb.append("# T - I1").append(ThebombzenAPI.newLine);
		dcb.append(
				"# to tell AutoSwitch that Silk Touch does not work on stone")
				.append(ThebombzenAPI.newLine);
		dcb.append("").append(ThebombzenAPI.newLine);
		dcb.append("# Place silk touch overrides here").append(
				ThebombzenAPI.newLine);
		dcb.append("T - I1").append(ThebombzenAPI.newLine);
		dcb.append("T - I13").append(ThebombzenAPI.newLine);
		dcb.append("").append(ThebombzenAPI.newLine);
		dcb.append("# ========= FORTUNE ================").append(
				ThebombzenAPI.newLine);
		dcb.append(
				"# AutoSwitch normally calculates if fortune works on a block")
				.append(ThebombzenAPI.newLine);
		dcb.append(
				"# To tell AutoSwitch that fortune works on a block, add the line ")
				.append(ThebombzenAPI.newLine);
		dcb.append("# F + block").append(ThebombzenAPI.newLine);
		dcb.append(
				"# Similarly, to tell AutoSwitch that fortune does not work on a block, add the line")
				.append(ThebombzenAPI.newLine);
		dcb.append("# F - block").append(ThebombzenAPI.newLine);
		dcb.append("# For example, use").append(ThebombzenAPI.newLine);
		dcb.append("# F + M1").append(ThebombzenAPI.newLine);
		dcb.append(
				"# to tell AutoSwitch that fortune works on all blocks that have the same material as stone.")
				.append(ThebombzenAPI.newLine);
		dcb.append("").append(ThebombzenAPI.newLine);
		dcb.append("# Place fortune overrides here").append(
				ThebombzenAPI.newLine);
		dcb.append("F + C59").append(ThebombzenAPI.newLine);
		dcb.append("").append(ThebombzenAPI.newLine);
		dcb.append("# ========= STANDARD TOOLS ==========").append(
				ThebombzenAPI.newLine);
		dcb.append(
				"# AutoSwitch normally calculates if a tool is standard on a block")
				.append(ThebombzenAPI.newLine);
		dcb.append(
				"# To tell AutoSwitch that a specified tool is standard on the specified blocks, use")
				.append(ThebombzenAPI.newLine);
		dcb.append("# S tool + block").append(ThebombzenAPI.newLine);
		dcb.append(
				"# Similarly, to tell AutoSwitch that a tool is not standard on the specified blocks, use")
				.append(ThebombzenAPI.newLine);
		dcb.append("# S tool - block").append(ThebombzenAPI.newLine);
		dcb.append(
				"# For example, to tell AutoSwitch that all items that behave like shears")
				.append(ThebombzenAPI.newLine);
		dcb.append("# are standard tools on blocks that behave like vines, use")
				.append(ThebombzenAPI.newLine);
		dcb.append("# S D359 + C106").append(ThebombzenAPI.newLine);
		dcb.append("").append(ThebombzenAPI.newLine);
		dcb.append("# Place standard tool overrides here").append(
				ThebombzenAPI.newLine);
		dcb.append("S D359 + C106").append(ThebombzenAPI.newLine);
		dcb.append("S D359 + C31").append(ThebombzenAPI.newLine);
		dcb.append("S D293 + C59").append(ThebombzenAPI.newLine);
		dcb.append("S D1270 + C59").append(ThebombzenAPI.newLine);
		dcb.append("S D1270 + C18").append(ThebombzenAPI.newLine);
		dcb.append("").append(ThebombzenAPI.newLine);
		dcb.append("# ======== WEAPON OVERRIDES =========").append(
				ThebombzenAPI.newLine);
		dcb.append(
				"# AutoSwitch normally calculates how much damage a weapon does, in half-hearts")
				.append(ThebombzenAPI.newLine);
		dcb.append(
				"# To tell AutoSwitch that a specified item really does a different amount of damage, use")
				.append(ThebombzenAPI.newLine);
		dcb.append("# W weapon + damage").append(ThebombzenAPI.newLine);
		dcb.append("# For example, use").append(ThebombzenAPI.newLine);
		dcb.append("# W I30233 + 10").append(ThebombzenAPI.newLine);
		dcb.append(
				"# to tell AutoSwitch that the IndustrialCraft2 Chainsaw (ID 30233)")
				.append(ThebombzenAPI.newLine);
		dcb.append("# really does 10 half-hearts of damage.").append(
				ThebombzenAPI.newLine);
		dcb.append("").append(ThebombzenAPI.newLine);
		dcb.append("W I30233 + 10").append(ThebombzenAPI.newLine);
		dcb.append("W I30479 + 12").append(ThebombzenAPI.newLine);
		dcb.append("W I30148 + 20").append(ThebombzenAPI.newLine);
		dcb.append("").append(ThebombzenAPI.newLine);
		dcb.append("").append(ThebombzenAPI.newLine);

		defaultConfig = dcb.toString();
	}

	private Set<BlockItemIdentifier> getAllBlocksInSet(String info) {
		Set<BlockItemIdentifier> ret = new HashSet<BlockItemIdentifier>();
		if (info.length() < 2) {
			System.err.println("Info is too short!");
			return ret;
		}
		char type = info.charAt(0);
		Scanner scanner = new Scanner(info.substring(1));
		int id;
		int meta = -1;
		if (scanner.hasNextInt()) {
			id = scanner.nextInt();
		} else {
			System.err.println("Error with info: " + info);
			scanner.close();
			return ret;
		}
		if (scanner.hasNextInt()) {
			meta = scanner.nextInt();
		}
		scanner.close();
		if (type == 'I') {
			if (id >= Block.blocksList.length || id < 0) {
				System.err.println("Invalid ID:" + id);
				return ret;
			}
			Block block = Block.blocksList[id];
			if (block == null) {
				return ret;
			}
			ret.add(new BlockItemIdentifier(id, meta));
		} else if (type == 'M') {
			if (id >= Block.blocksList.length || id < 0) {
				System.err.println("Invalid ID:" + id);
				return ret;
			}
			Block block = Block.blocksList[id];
			if (block == null) {
				return ret;
			}
			for (int i = 0; i < Block.blocksList.length; i++) {
				Block testBlock = Block.blocksList[i];
				if (testBlock == null) {
					continue;
				}
				if (testBlock.blockMaterial.equals(block.blockMaterial)) {
					ret.add(new BlockItemIdentifier(i, -1));
				}
			}
		} else if (type == 'C') {
			if (id >= Block.blocksList.length || id < 0) {
				System.err.println("Invalid ID:" + id);
				return ret;
			}
			Block block = Block.blocksList[id];
			if (block == null) {
				return ret;
			}
			for (int i = 0; i < Block.blocksList.length; i++) {
				Block testBlock = Block.blocksList[i];
				if (testBlock == null) {
					continue;
				}
				if (block.getClass().isAssignableFrom(testBlock.getClass())) {
					ret.add(new BlockItemIdentifier(i, -1));
				}
			}
		} else if (type == 'V') {
			if (id >= Item.itemsList.length || id < 0) {
				System.err.println("Invalid ID:" + id);
				return ret;
			}
			Item item = Item.itemsList[id];
			if (item == null) {
				return ret;
			}
			for (int i = 0; i < Block.blocksList.length; i++) {
				Block testBlock = Block.blocksList[i];
				if (testBlock == null) {
					continue;
				}
				if (item.getStrVsBlock(new ItemStack(item), testBlock) > 1.5F) {
					ret.add(new BlockItemIdentifier(i, -1));
				}
			}
		} else {
			System.err.println("Unrecognized option: " + type);
		}

		return ret;
	}

	private Set<Integer> getAllToolsInSet(String info) {
		Set<Integer> ret = new HashSet<Integer>();
		if (info.length() < 2) {
			System.err.println("Info is too short!");
			return ret;
		}
		char type = info.charAt(0);
		Scanner scanner = new Scanner(info.substring(1));
		int id;
		if (scanner.hasNextInt()) {
			id = scanner.nextInt();
		} else {
			System.err.println("Error with info: " + info);
			scanner.close();
			return ret;
		}
		scanner.close();
		if (type == 'I') {
			if (id >= Item.itemsList.length || id < 0) {
				System.err.println("Invalid ID: " + id);
				return ret;
			}
			Item item = Item.itemsList[id];
			if (item == null) {
				return ret;
			}
			ret.add(id);
		} else if (type == 'D') {
			if (id >= Item.itemsList.length || id < 0) {
				System.err.println("Invalid ID: " + id);
				return ret;
			}
			Item item = Item.itemsList[id];
			if (item == null) {
				return ret;
			}
			for (int i = 0; i < Item.itemsList.length; i++) {
				Item testItem = Item.itemsList[i];
				if (testItem == null) {
					continue;
				}
				if (item.getClass().isAssignableFrom(testItem.getClass())) {
					ret.add(i);
				}
			}
		} else if (type == 'B') {
			if (id >= Block.blocksList.length || id < 0) {
				System.err.println("Invalid ID:" + id);
				return ret;
			}
			Block block = Block.blocksList[id];
			if (block == null) {
				return ret;
			}
			for (int i = 0; i < Item.itemsList.length; i++) {
				Item testItem = Item.itemsList[i];
				if (testItem == null) {
					continue;
				}
				if (testItem.getStrVsBlock(new ItemStack(testItem), block) > 1.5F) {
					ret.add(i);
				}
			}
		} else {
			System.err.println("Unrecognized Option: " + info);
		}
		return ret;
	}

	public int getCustomWeaponDamage(ItemStack itemstack) {
		if (itemstack == null) {
			return -1;
		}
		if (customWeapons.containsKey(itemstack.itemID)) {
			return customWeapons.get(itemstack.itemID);
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

	public boolean isFortuneOverriddenToNotWork(BlockItemIdentifier pair) {
		for (BlockItemIdentifier test : fortuneNoWorks) {
			if (test.includes(pair)) {
				return true;
			}
		}
		return false;
	}

	public boolean isFortuneOverriddenToWork(BlockItemIdentifier pair) {
		for (BlockItemIdentifier test : fortuneWorks) {
			if (test.includes(pair)) {
				return true;
			}
		}
		return false;
	}

	public boolean isSilkTouchOverriddenToNotWork(BlockItemIdentifier pair) {
		for (BlockItemIdentifier test : silkTouchNoWorks) {
			if (test.includes(pair)) {
				return true;
			}
		}
		return false;
	}

	public boolean isSilkTouchOverriddenToWork(BlockItemIdentifier pair) {
		for (BlockItemIdentifier test : silkTouchWorks) {
			if (test.includes(pair)) {
				return true;
			}
		}
		return false;
	}

	public boolean isToolOverriddenAsNotStandardOnBlock(BlockItemIdentifier block,
			int tool) {
		for (BlockToolPair pair : notStandardBlocksAndTools) {
			if (pair.getTool() == tool && pair.getBlock().includes(block)) {
				return true;
			}
		}
		return false;
	}

	public boolean isToolOverriddenAsStandardOnBlock(BlockItemIdentifier block,
			int tool) {
		for (BlockToolPair pair : standardBlocksAndTools) {
			if (pair.getTool() == tool && pair.getBlock().includes(block)) {
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
		BufferedReader reader = new BufferedReader(new FileReader(
				extraConfigFile));
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
				System.err.println("Error on line: " + line);
				continue;
			}
			char first = line.charAt(0);
			if (first == 'R') {
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
			} else if (first == 'T') {
				char second = line.charAt(1);
				if (second == '+') {
					silkTouchWorks.addAll(getAllBlocksInSet(line.substring(2)));
				} else if (second == '-') {
					silkTouchNoWorks
							.addAll(getAllBlocksInSet(line.substring(2)));
				} else {
					System.err.println("Error on line: " + line);
					continue;
				}
			} else if (first == 'F') {
				char second = line.charAt(1);
				if (second == '+') {
					fortuneWorks.addAll(getAllBlocksInSet(line.substring(2)));
				} else if (second == '-') {
					fortuneNoWorks.addAll(getAllBlocksInSet(line.substring(2)));
				} else {
					System.err.println("Error on line: " + line);
					continue;
				}
			} else if (first == 'S') {
				int indexP = line.indexOf('+');
				int indexM = line.indexOf('-');
				String toolSub = "";
				String blockSub = "";
				boolean plus = false;
				if (indexP != -1) {
					if (indexM != -1 || indexP >= line.length() - 1) {
						System.err.println("Error on line: " + line);
						continue;
					} else {
						toolSub = line.substring(1, indexP);
						blockSub = line.substring(indexP + 1);
						plus = true;
					}
				} else if (indexM != -1) {
					if (indexM >= line.length() - 1) {
						System.err.println("Error on line: " + line);
						continue;
					} else {
						toolSub = line.substring(1, indexM);
						blockSub = line.substring(indexM + 1);
						plus = false;
					}
				}
				if (plus) {
					Set<BlockItemIdentifier> blockSet = getAllBlocksInSet(blockSub);
					Set<Integer> toolSet = getAllToolsInSet(toolSub);
					for (BlockItemIdentifier block : getAllBlocksInSet(blockSub)) {
						for (Integer tool : toolSet) {
							standardBlocksAndTools.add(new BlockToolPair(block,
									tool));
						}
					}
				} else {
					Set<BlockItemIdentifier> blockSet = getAllBlocksInSet(blockSub);
					Set<Integer> toolSet = getAllToolsInSet(toolSub);
					for (BlockItemIdentifier block : getAllBlocksInSet(blockSub)) {
						for (Integer tool : toolSet) {
							notStandardBlocksAndTools.add(new BlockToolPair(
									block, tool));
						}
					}
				}
			} else if (first == 'W') {
				int indexP = line.indexOf('+');
				if (indexP < 0 || indexP >= line.length() - 1) {
					System.err.println("Error on line: " + line);
					continue;
				}
				String sub = line.substring(1, indexP);
				String damageString = line.substring(indexP);
				Integer damage = null;
				try {
					damage = Integer.valueOf(damageString);
				} catch (NumberFormatException nfe) {
					System.err.println("Error on line: " + line);
					continue;
				}
				Set<Integer> tools = getAllToolsInSet(sub);
				for (Integer tool : tools) {
					customWeapons.put(tool, damage);
				}
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
