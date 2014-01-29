package thebombzen.mods.autoswitch;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.player.AttackEntityEvent;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import thebombzen.mods.thebombzenapi.ThebombzenAPI;
import thebombzen.mods.thebombzenapi.ThebombzenAPIBaseMod;
import thebombzen.mods.thebombzenapi.ThebombzenAPIConfiguration;
import thebombzen.mods.thebombzenapi.client.ThebombzenAPIConfigScreen;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@Mod(modid = "AutoSwitch", name = "AutoSwitch", version = "4.2.0", dependencies = "required-after:ThebombzenAPI")
public class AutoSwitch extends ThebombzenAPIBaseMod implements ITickHandler {

	public static final int STAGE_H0 = 0;
	public static final int STAGE_SWITCHED = 1;
	public static final int STAGE_CANCELED = 2;
	public static final Minecraft mc = Minecraft.getMinecraft();

	private int entityAttackStage = STAGE_H0;

	private EntityLivingBase entitySwitchedOn = null;

	private Configuration configuration;

	private ItemStack prevHeldItemStack;
	private Random prevRandom;
	private boolean prevMouseDown = false;
	private boolean prevPulse = false;
	private int prevtool = 0;
	private int prevWorld = 0;
	private boolean pulseOn = false;

	@Instance(value = "AutoSwitch")
	public static AutoSwitch instance;

	public AutoSwitch() {
		configuration = new Configuration(this);
	}

	@Override
	public void activeKeyPressed(int keyCode) {

	}

	public boolean canHarvestBlock(ItemStack itemstack, Block block,
			int metadata) {
		if (block == null) {
			return false;
		} else {
			fakeItemForPlayer(itemstack);
			boolean can = block.canHarvestBlock(mc.thePlayer, metadata);
			unFakeItemForPlayer();
			return can;
		}
	}

	private void clientTick() {

		if (mc.theWorld == null) {
			return;
		}

		if (entityAttackStage == STAGE_CANCELED) {
			mc.thePlayer.swingItem();
			mc.playerController.attackEntity(mc.thePlayer, entitySwitchedOn);
			entityAttackStage = STAGE_H0;
			entitySwitchedOn = null;
			return;
		}

		pulseOn = Keyboard.isKeyDown(configuration.getPulseKeyCode());

		int keyCode = mc.gameSettings.keyBindAttack.keyCode;
		boolean mouseDown = keyCode < 0 ? Mouse.isButtonDown(keyCode + 100)
				: Keyboard.isKeyDown(keyCode);
		if (!mouseDown && prevMouseDown || mouseDown && pulseOn ^ prevPulse) {
			switchBack();
		}
		if (mouseDown && !prevMouseDown || mouseDown && pulseOn ^ prevPulse) {
			prevtool = mc.thePlayer.inventory.currentItem;
		}
		if (mouseDown) {
			if (mc.objectMouseOver != null
					&& mc.objectMouseOver.typeOfHit == EnumMovingObjectType.TILE) {
				potentiallySwitchTools(mc.theWorld, mc.objectMouseOver.blockX,
						mc.objectMouseOver.blockY, mc.objectMouseOver.blockZ);
			} else if (mc.objectMouseOver != null
					&& mc.objectMouseOver.typeOfHit == EnumMovingObjectType.ENTITY
					&& mc.objectMouseOver.entityHit instanceof EntityLivingBase) {
				potentiallySwitchWeapons((EntityLivingBase) mc.objectMouseOver.entityHit);
			}
		}
		prevMouseDown = mouseDown;
		prevPulse = pulseOn;
		prevWorld = System.identityHashCode(mc.theWorld);
	}

	public int compareBlockStr(float newBlockStr, float oldBlockStr) {
		if (newBlockStr > oldBlockStr) {
			return 1;
		} else if (oldBlockStr > newBlockStr) {
			return -1;
		}
		return 0;
	}

	@Override
	public ThebombzenAPIConfigScreen createConfigScreen(GuiScreen base) {
		return new ConfigScreen(this, base, configuration);
	}

	private void debug(String string) {
		debug("%s", string);
	}

	private void debug(String format, Object... args) {
		if (configuration.getPropertyBoolean(ConfigOption.DEBUG)) {
			forceDebug(format, args);
		}
	}

	public boolean doesFortuneWorkOnBlock(World world, int x, int y, int z) {

		Block block = Block.blocksList[world.getBlockId(x, y, z)];
		int metadata = world.getBlockMetadata(x, y, z);

		if (block == null) {
			return false;
		}

		if (configuration.isFortuneOverriddenToNotWork(new BlockItemIdentifier(
				block.blockID, metadata))) {
			return false;
		} else if (configuration.isFortuneOverriddenToWork(new BlockItemIdentifier(
				block.blockID, metadata))) {
			return true;
		}

		Random maxRandom = new NotSoRandom(false);
		Random zeroRandom = new NotSoRandom(true);

		List<ItemStack> defaultMaxRandom;
		List<ItemStack> defaultZeroRandom;
		List<ItemStack> fortuneMaxRandom;
		List<ItemStack> fortuneZeroRandom;

		fakeRandomForWorld(world, maxRandom);
		defaultMaxRandom = block.getBlockDropped(world, x, y, z, metadata, 0);
		fortuneMaxRandom = block.getBlockDropped(world, x, y, z, metadata, 3);
		unFakeRandomForWorld(world);

		fakeRandomForWorld(world, zeroRandom);
		defaultZeroRandom = block.getBlockDropped(world, x, y, z, metadata, 0);
		fortuneZeroRandom = block.getBlockDropped(world, x, y, z, metadata, 3);
		unFakeRandomForWorld(world);

		if (!ThebombzenAPI.areItemStackCollectionsEqual(defaultMaxRandom,
				fortuneMaxRandom)
				|| !ThebombzenAPI.areItemStackCollectionsEqual(defaultZeroRandom,
						fortuneZeroRandom)) {
			return true;
		} else {
			return false;
		}
	}

	public boolean doesSilkTouchWorkOnBlock(World world, int x, int y, int z) {

		Block block = Block.blocksList[world.getBlockId(x, y, z)];
		int metadata = world.getBlockMetadata(x, y, z);

		if (block == null) {
			return false;
		}

		if (configuration.isSilkTouchOverriddenToNotWork(new BlockItemIdentifier(
				block.blockID, metadata))) {
			return false;
		} else if (configuration
				.isSilkTouchOverriddenToWork(new BlockItemIdentifier(block.blockID,
						metadata))) {
			return true;
		}

		Random maxRandom = new NotSoRandom(false);
		Random zeroRandom = new NotSoRandom(true);

		List<ItemStack> defaultMaxRandom;
		List<ItemStack> defaultZeroRandom;

		fakeRandomForWorld(world, maxRandom);
		defaultMaxRandom = block.getBlockDropped(world, x, y, z, metadata, 0);
		unFakeRandomForWorld(world);

		fakeRandomForWorld(world, zeroRandom);
		defaultZeroRandom = block.getBlockDropped(world, x, y, z, metadata, 0);
		unFakeRandomForWorld(world);

		ItemStack stackedBlock = (ItemStack) ThebombzenAPI.invokePrivateMethod(
				block, Block.class, new String[] { "createStackedBlock",
						"func_71880_c_", "c_" }, new Class<?>[] { int.class },
				metadata);

		List<ItemStack> stackedBlockList = Collections
				.singletonList(stackedBlock);

		if (block.canSilkHarvest(world, mc.thePlayer, x, y, z, metadata)
				&& (!ThebombzenAPI.areItemStackCollectionsEqual(stackedBlockList,
						defaultMaxRandom) || !ThebombzenAPI
						.areItemStackCollectionsEqual(stackedBlockList,
								defaultZeroRandom))) {
			return true;
		} else {
			return false;
		}
	}

	private void fakeItemForPlayer(ItemStack itemstack) {
		prevHeldItemStack = mc.thePlayer.inventory.mainInventory[mc.thePlayer.inventory.currentItem];
		mc.thePlayer.inventory.mainInventory[mc.thePlayer.inventory.currentItem] = itemstack;
		if (prevHeldItemStack != null) {
			mc.thePlayer.getAttributeMap().removeAttributeModifiers(
					prevHeldItemStack.getAttributeModifiers());
		}
		if (itemstack != null) {
			mc.thePlayer.getAttributeMap().applyAttributeModifiers(
					itemstack.getAttributeModifiers());
		}
	}

	private void fakeRandomForWorld(World world, Random random) {
		prevRandom = world.rand;
		world.rand = random;
	}

	public float getBlockHardness(World world, int x, int y, int z) {
		Block block = Block.blocksList[world.getBlockId(x, y, z)];
		if (block == null) {
			return 0;
		} else {
			return block.getBlockHardness(world, x, y, z);
		}
	}

	public float getBlockStrength(ItemStack itemstack, World world, int x,
			int y, int z) {
		Block block = Block.blocksList[world.getBlockId(x, y, z)];
		fakeItemForPlayer(itemstack);
		float str = block.getPlayerRelativeBlockHardness(mc.thePlayer, world,
				x, y, z);
		unFakeItemForPlayer();
		return str;
	}

	@Override
	public ThebombzenAPIConfiguration<?> getConfiguration() {
		return configuration;
	}

	public float getEff(float str, ItemStack itemstack) {
		if (str <= 1.0F) {
			return str;
		}
		fakeItemForPlayer(itemstack);
		float effLevel = EnchantmentHelper.getEfficiencyModifier(mc.thePlayer);
		unFakeItemForPlayer();
		if (effLevel == 0) {
			return str;
		}
		return str + effLevel * effLevel + 1;
	}

	public int getEnchantmentLevel(int i, ItemStack itemstack) {
		return EnchantmentHelper.getEnchantmentLevel(i, itemstack);
	}

	public float getEnchantmentModifierLiving(ItemStack itemstack,
			EntityLivingBase entityover) {
		fakeItemForPlayer(itemstack);
		float modifier = EnchantmentHelper.getEnchantmentModifierLiving(
				mc.thePlayer, entityover);
		unFakeItemForPlayer();
		return modifier;
	}

	@Override
	public String getLabel() {
		return "thebombzen.mods.autoswitch.AutoSwitch";
	}

	@Override
	public String getLongName() {
		return "AutoSwitch";
	}

	@Override
	public String getLongVersionString() {
		return "AutoSwitch v4.2.0 for Minecraft 1.6.4";
	}

	public Set<Enchantment> getNonstandardNondamageEnchantmentsOnBothStacks(
			ItemStack stack1, ItemStack stack2) {
		Set<Integer> bothItemsEnchantments = new HashSet<Integer>();
		Set<Enchantment> ret = new HashSet<Enchantment>();

		if (stack1 != null) {
			bothItemsEnchantments.addAll(EnchantmentHelper.getEnchantments(
					stack1).keySet());
		}
		if (stack2 != null) {
			bothItemsEnchantments.addAll(EnchantmentHelper.getEnchantments(
					stack2).keySet());
		}

		Iterator<Integer> bothItemsEnchantmentsIterator = bothItemsEnchantments
				.iterator();
		while (bothItemsEnchantmentsIterator.hasNext()) {
			Integer effectId = bothItemsEnchantmentsIterator.next();

			if (effectId == Enchantment.efficiency.effectId
					|| effectId == Enchantment.silkTouch.effectId
					|| effectId == Enchantment.fortune.effectId
					|| effectId == Enchantment.unbreaking.effectId
					|| effectId == Enchantment.looting.effectId
					|| effectId == Enchantment.knockback.effectId
					|| effectId == Enchantment.fireAspect.effectId) {
				continue;
			}

			if (Enchantment.enchantmentsList[effectId].getName().startsWith(
					"enchantment.damage.")) {
				continue;
			}

			ret.add(Enchantment.enchantmentsList[effectId]);

		}

		return ret;
	}

	@Override
	public int getNumActiveKeys() {
		return 0;
	}

	@Override
	public int getNumToggleKeys() {
		return 1;
	}

	@Override
	public String getShortName() {
		return "AS";
	}

	public float getStrVsBlock(ItemStack itemstack, Block block, int metadata) {
		return itemstack == null ? 1.0F : itemstack.getItem().getStrVsBlock(
				itemstack, block, metadata);
	}

	@Override
	protected String getToggleMessageString(int index, boolean enabled) {
		if (enabled) {
			return "AutoSwitch is now enabled.";
		} else {
			return "AutoSwitch is now disabled.";
		}
	}

	public int getToolStandardness(ItemStack itemstack, World world, int x,
			int y, int z) {
		if (itemstack == null) {
			return 0;
		}
		Block block = Block.blocksList[world.getBlockId(x, y, z)];
		int metadata = world.getBlockMetadata(x, y, z);
		if (configuration.isToolOverriddenAsNotStandardOnBlock(
				new BlockItemIdentifier(block.blockID, metadata), itemstack.itemID)) {
			return -2;
		} else if (configuration.isToolOverriddenAsStandardOnBlock(
				new BlockItemIdentifier(block.blockID, metadata), itemstack.itemID)) {
			return 2;
		}
		if (getStrVsBlock(itemstack, block, metadata) > 1.5F) {
			return 1;
		} else {
			if (isItemStackDamageableOnBlock(itemstack, world, x, y, z)) {
				return -1;
			} else {
				return 0;
			}
		}
	}

	@Override
	protected String getVersionFileURLString() {
		return "https://dl.dropboxusercontent.com/u/51080973/AutoSwitch/ASVersion.txt";
	}

	@Override
	public boolean hasConfigScreen() {
		return true;
	}

	public boolean instanceOfItemSword(ItemStack itemstack) {
		return itemstack != null && itemstack.getItem() instanceof ItemSword;
	}

	public boolean isItemStackDamageable(ItemStack itemstack) {
		if (itemstack == null) {
			return false;
		}
		return itemstack.getItem().isDamageable();
	}

	public boolean isItemStackDamageableOnBlock(ItemStack itemstack,
			World world, int x, int y, int z) {
		if (!isItemStackDamageable(itemstack)) {
			return false;
		}
		return getBlockHardness(world, x, y, z) > 0.0F;
	}

	public boolean isToolBetter(ItemStack newItemStack, ItemStack oldItemStack,
			World world, int x, int y, int z) {

		Block block = Block.blocksList[world.getBlockId(x, y, z)];

		if (block == null) {
			return false;
		}

		int metadata = world.getBlockMetadata(x, y, z);

		int newitemID = newItemStack == null ? 0 : newItemStack.itemID;
		int olditemID = oldItemStack == null ? 0 : oldItemStack.itemID;

		float newStr = getStrVsBlock(newItemStack, block, metadata);
		float oldStr = getStrVsBlock(oldItemStack, block, metadata);
		float newBlockStr = getBlockStrength(newItemStack, world, x, y, z);
		float oldBlockStr = getBlockStrength(oldItemStack, world, x, y, z);

		if (newBlockStr == 0.0F && oldBlockStr == 0.0F) {
			debug("Not switching because block is unbreakable by either item.");
			return false;
		}

		debug("newBlockStr: %f, oldBlockStr %f", newBlockStr, oldBlockStr);
		debug("New harvest: %b, old harvest: %b",
				canHarvestBlock(newItemStack, block, metadata),
				canHarvestBlock(oldItemStack, block, metadata));
		debug("newStrength: %f, oldStrength: %f", newStr, oldStr);

		float newEff = getEff(newStr, newItemStack);
		float oldEff = getEff(oldStr, oldItemStack);
		debug("newEff: %f, oldEff: %f", newEff, oldEff);

		if (canHarvestBlock(newItemStack, block, metadata)
				&& !canHarvestBlock(oldItemStack, block, metadata)) {
			debug("Switching because new can harvest and old can't.");
			return true;
		} else if (canHarvestBlock(oldItemStack, block, metadata)
				&& !canHarvestBlock(newItemStack, block, metadata)) {
			debug("Not switching because old can harvest and new can't.");
			return false;
		}

		int newStandard = getToolStandardness(newItemStack, world, x, y, z);
		int oldStandard = getToolStandardness(oldItemStack, world, x, y, z);
		debug("newStandard: %d, oldStandard: %d", newStandard, oldStandard);

		boolean newDamageable = isItemStackDamageableOnBlock(newItemStack,
				world, x, y, z);
		boolean oldDamageable = isItemStackDamageableOnBlock(oldItemStack,
				world, x, y, z);
		debug("newDamageable: %b, oldDamageable: %b", newDamageable,
				oldDamageable);

		debug("Comparison mode is %s",
				configuration.getProperty(ConfigOption.TOOL_SELECTION_MODE));

		if (configuration.getToolSelectionMode() == Configuration.FAST_STANDARD
				|| configuration.getToolSelectionMode() == Configuration.SLOW_STANDARD) {
			if (newStandard > oldStandard) {
				debug("Switching because new item is more standard than old.");
				return true;
			} else if (oldStandard > newStandard) {
				debug("Not switching because old item is more standard than new.");
				return false;
			} else {
				if (newStandard <= 0 && oldStandard <= 0) {
					if (newDamageable && !oldDamageable) {
						debug("Not switching because new tool is damageable and old isn't, and neither are standard.");
						return false;
					} else if (oldDamageable && !newDamageable) {
						debug("Switching because old tool is damageable and new isn't, and neither are standard.");
						return true;
					}
				}
			}
		}

		boolean silkWorks = doesSilkTouchWorkOnBlock(world, x, y, z);
		boolean newHasSilk = getEnchantmentLevel(
				Enchantment.silkTouch.effectId, newItemStack) > 0;
		boolean oldHasSilk = getEnchantmentLevel(
				Enchantment.silkTouch.effectId, oldItemStack) > 0;
		debug("silkWorks: %b, newHasSilk: %b, oldHasSilk: %b", silkWorks,
				newHasSilk, oldHasSilk);

		if (newHasSilk && !oldHasSilk) {
			if (silkWorks) {
				debug("Switching because new has silk touch and old doesn't, and new works.");
				return true;
			} else {
				if (oldStandard > 0) {
					debug("Not switching because new has silk touch and old doesn't, and old replaces new.");
					return false;
				} else if (newStandard <= 0) {
					debug("Not switching because new has silk touch and old doesn't, and new is weak.");
					return false;
				}
			}
		} else if (oldHasSilk && !newHasSilk) {
			if (silkWorks) {
				debug("Not switching because old has silk touch and new doesn't, and old works.");
				return false;
			} else {
				if (newStandard > 0) {
					debug("Switching because old has silk touch and new doesn't, and new replaces old.");
					return true;
				} else if (oldStandard <= 0) {
					debug("Switching because old has silk touch and new doesn't, and old is weak.");
					return true;
				}
			}
		}

		boolean fortuneWorks = doesFortuneWorkOnBlock(world, x, y, z);
		int newFortuneLevel = getEnchantmentLevel(Enchantment.fortune.effectId,
				newItemStack);
		int oldFortuneLevel = getEnchantmentLevel(Enchantment.fortune.effectId,
				oldItemStack);

		debug("fortuneWorks: %b, newFortuneLevel: %d, oldFortuneLevel: %d",
				fortuneWorks, newFortuneLevel, oldFortuneLevel);

		if (fortuneWorks) {
			if (newFortuneLevel > oldFortuneLevel) {
				debug("Switching because new fortune, %d, is more than old, %d.",
						newFortuneLevel, oldFortuneLevel);
				return true;
			} else if (oldFortuneLevel > newFortuneLevel) {
				debug("Not switching because old fortune, %d, is more than new, %d.",
						oldFortuneLevel, newFortuneLevel);
				return false;
			}
		}

		int comparison = compareBlockStr(newBlockStr, oldBlockStr);

		if (configuration.getToolSelectionMode() == Configuration.FAST_STANDARD) {
			if (comparison > 0) {
				debug("Switching because new tool is stronger.");
				return true;
			} else if (comparison < 0) {
				debug("Not switching because old tool is stronger.");
				return false;
			}
		} else if (configuration.getToolSelectionMode() == Configuration.SLOW_STANDARD) {
			if (comparison < 0) {
				debug("Switching because new item is worse than old item and SLOW STANDARD is on.");
				return true;
			} else if (comparison > 0) {
				debug("Not switching because new item is better than old item and SLOW STANDARD is on.");
				return false;
			}
		} else if (configuration.getToolSelectionMode() == Configuration.FAST_NONSTANDARD) {
			if (comparison > 0) {
				debug("Switching because new tool is stronger.");
				return true;
			} else if (comparison < 0) {
				debug("Not switching because old tool is stronger.");
				return false;
			}
		}

		Set<Enchantment> bothItemsEnchantments = getNonstandardNondamageEnchantmentsOnBothStacks(
				newItemStack, oldItemStack);

		for (Enchantment enchantment : bothItemsEnchantments) {
			int oldLevel = getEnchantmentLevel(enchantment.effectId,
					oldItemStack);
			int newLevel = getEnchantmentLevel(enchantment.effectId,
					newItemStack);
			if (newLevel > oldLevel) {
				debug("Switching because new %s level, %d, is more than old, %d.",
						enchantment.getName(), newLevel, oldLevel);
				return true;
			} else if (newLevel < oldLevel) {
				debug("Switching because old %s level, %d, is more than new, %d.",
						enchantment.getName(), oldLevel, newLevel);
				return false;
			}
		}

		if (newDamageable && !oldDamageable) {
			debug("Not switching because new tool is damageable and old isn't.");
			return false;
		} else if (oldDamageable && !newDamageable) {
			debug("Switching because old tool is damageable and new isn't.");
			return true;
		}

		if (newDamageable && oldDamageable) {

			if (newFortuneLevel > oldFortuneLevel) {
				debug("Not switching because new fortune is bad and items are damageable.");
				return false;
			} else if (oldFortuneLevel > newFortuneLevel) {
				debug("Switching because old fortune is bad and items are damageable.");
				return true;
			}

			if (newEff <= 1.5F && oldEff > 1.5F) {
				debug("Not switching because new item is wrong for the block and damageable, and old is right.");
				return false;
			} else if (oldEff <= 1.5F && newEff > 1.5F) {
				debug("Switching because old item is wrong for the block and damageable, and new is right.");
				return true;
			}

			int newUnbreakingLevel = EnchantmentHelper.getEnchantmentLevel(
					Enchantment.unbreaking.effectId, newItemStack);
			int oldUnbreakingLevel = EnchantmentHelper.getEnchantmentLevel(
					Enchantment.unbreaking.effectId, oldItemStack);

			if (newUnbreakingLevel > oldUnbreakingLevel) {
				debug("Switching because new unbreaking is more than old unbreaking.");
				return true;
			} else if (oldUnbreakingLevel > newUnbreakingLevel) {
				debug("Not switching because old unbreaking is more than new unbreaking.");
				return false;
			}
		}

		debug("Not switching because tools are equal.");
		return false;

	}

	public boolean isWeaponBetter(ItemStack newItemStack,
			ItemStack oldItemStack, EntityLivingBase entityover) {

		boolean isPlayer = entityover instanceof EntityPlayer;

		float oldDamage = configuration.getCustomWeaponDamage(oldItemStack);
		float newDamage = configuration.getCustomWeaponDamage(newItemStack);

		if (oldDamage == -1) {
			fakeItemForPlayer(oldItemStack);
			oldDamage = (float) mc.thePlayer.getEntityAttribute(
					SharedMonsterAttributes.attackDamage).getAttributeValue();
			unFakeItemForPlayer();
		}

		if (newDamage == -1) {
			fakeItemForPlayer(newItemStack);
			newDamage = (float) mc.thePlayer.getEntityAttribute(
					SharedMonsterAttributes.attackDamage).getAttributeValue();
			unFakeItemForPlayer();
		}

		oldDamage += getEnchantmentModifierLiving(oldItemStack, entityover);
		newDamage += getEnchantmentModifierLiving(newItemStack, entityover);

		debug("Old damage is %f, new damage is %f.", oldDamage, newDamage);

		if (isPlayer) {
			if (newDamage > oldDamage) {
				debug("Switching because new damage is more.");
				return true;
			} else if (newDamage < oldDamage) {
				debug("Not switching because old damage is more.");
				return false;
			}
		} else {
			int oldHits = ThebombzenAPI.ceil((double) entityover
					.getHealth() / (double) oldDamage);
			int newHits = ThebombzenAPI.ceil((double) entityover
					.getHealth() / (double) newDamage);

			if (oldHits < 0) {
				oldHits = Integer.MAX_VALUE;
			}
			if (newHits < 0) {
				newHits = Integer.MAX_VALUE;
			}

			debug("Old hits are %d, new hits are %d", oldHits, newHits);

			if (newHits < oldHits) {
				debug("Switching because new hits are fewer.");
				return true;
			} else if (newHits > oldHits) {
				debug("Not switching because old hits are fewer.");
				return false;
			}

		}

		int newLootingLevel = getEnchantmentLevel(Enchantment.looting.effectId,
				newItemStack);
		int newFireAspectLevel = getEnchantmentLevel(
				Enchantment.fireAspect.effectId, newItemStack);
		int newKnockbackLevel = getEnchantmentLevel(
				Enchantment.knockback.effectId, newItemStack);
		int newUnbreakingLevel = getEnchantmentLevel(
				Enchantment.unbreaking.effectId, newItemStack);

		int oldLootingLevel = getEnchantmentLevel(Enchantment.looting.effectId,
				oldItemStack);
		int oldFireAspectLevel = getEnchantmentLevel(
				Enchantment.fireAspect.effectId, oldItemStack);
		int oldKnockbackLevel = getEnchantmentLevel(
				Enchantment.knockback.effectId, oldItemStack);
		int oldUnbreakingLevel = getEnchantmentLevel(
				Enchantment.unbreaking.effectId, oldItemStack);

		if (!isPlayer) {
			if (newLootingLevel > oldLootingLevel) {
				debug("Switching because new looting, %d, is more than old, %d.",
						newLootingLevel, oldLootingLevel);
				return true;
			} else if (oldLootingLevel > newLootingLevel) {
				debug("Not switching because old looting, %d, is more than new, %d.",
						oldLootingLevel, newLootingLevel);
				return false;
			}
		}

		if (newFireAspectLevel > oldFireAspectLevel) {
			debug("Switching because new fire aspect, %d, is more than old, %d.",
					newFireAspectLevel, oldFireAspectLevel);
			return true;
		} else if (oldFireAspectLevel > newFireAspectLevel) {
			debug("Not switching because old fire aspect, %d, is more than new, %d.",
					oldFireAspectLevel, newFireAspectLevel);
			return false;
		}

		if (newKnockbackLevel > oldKnockbackLevel) {
			debug("Switching because new knockback, %d, is more than old, %d.",
					newKnockbackLevel, oldKnockbackLevel);
			return true;
		} else if (oldKnockbackLevel > newKnockbackLevel) {
			debug("Not switching because old knockback, %d, is more than new, %d.",
					oldKnockbackLevel, newKnockbackLevel);
			return false;
		}

		Set<Enchantment> bothItemsEnchantments = getNonstandardNondamageEnchantmentsOnBothStacks(
				newItemStack, oldItemStack);

		for (Enchantment enchantment : bothItemsEnchantments) {
			int oldLevel = getEnchantmentLevel(enchantment.effectId,
					oldItemStack);
			int newLevel = getEnchantmentLevel(enchantment.effectId,
					newItemStack);
			if (newLevel > oldLevel) {
				debug("Switching because new %s level, %d, is more than old, %d.",
						enchantment.getName(), newLevel, oldLevel);
				return true;
			} else if (newLevel < oldLevel) {
				debug("Switching because old %s level, %d, is more than new, %d.",
						enchantment.getName(), oldLevel, newLevel);
				return false;
			}
		}

		if (instanceOfItemSword(newItemStack)
				&& !instanceOfItemSword(oldItemStack)) {
			debug("Switching because new weapon is sword and old isn't.");
			return true;
		}
		if (instanceOfItemSword(oldItemStack)
				&& !instanceOfItemSword(newItemStack)) {
			debug("Not switching because old weapon is sword and new isn't.");
			return false;
		}

		boolean newDamageable = isItemStackDamageable(newItemStack);
		boolean oldDamageable = isItemStackDamageable(oldItemStack);
		debug("newDamageable: %b, oldDamageable: %b", newDamageable,
				oldDamageable);

		if (newDamageable && !oldDamageable) {
			debug("Not switching because new weapon is damageable and old isn't.");
			return false;
		}
		if (oldDamageable && !newDamageable) {
			debug("Switching because new weapon is not damageable and old is.");
			return true;
		}

		if (newDamageable && oldDamageable
				&& newUnbreakingLevel > oldUnbreakingLevel) {
			debug("Switching because new unbreaking, %d, is more than old, %d.",
					newUnbreakingLevel, oldUnbreakingLevel);
			return true;
		} else if (newDamageable && oldDamageable
				&& oldUnbreakingLevel > newUnbreakingLevel) {
			debug("Not switching because old unbreaking, %d, is more than new, %d.",
					oldUnbreakingLevel, newUnbreakingLevel);
			return false;
		}

		if (newDamage > oldDamage) {
			debug("Switching because new damage is more and all else is equal.");
			return true;
		} else if (newDamage < oldDamage) {
			debug("Not switching because old damage is more and all else is equal.");
			return false;
		}

		if (newItemStack == null && oldItemStack != null) {
			debug("Switching because new tool is fist and old is useless.");
			return true;
		} else if (oldItemStack == null && newItemStack != null) {
			debug("Not switching because old tool is fist and new is useless.");
			return false;
		}

		debug("Not switching because weapons are equal.");
		return false;
	}

	@EventHandler
	public void load(FMLInitializationEvent fmlie) {
		TickRegistry.registerTickHandler(this, Side.CLIENT);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@ForgeSubscribe
	public void onEntityAttack(AttackEntityEvent event) {
		if (!event.target.worldObj.isRemote) {
			return;
		}
		if (entityAttackStage == STAGE_SWITCHED
				&& entitySwitchedOn == event.target) {
			entityAttackStage = STAGE_CANCELED;
			event.setCanceled(true);
		} else if (entityAttackStage != STAGE_CANCELED) {
			entitySwitchedOn = null;
			entityAttackStage = STAGE_H0;
		}
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {

	}

	public boolean potentiallySwitchTools(World world, int x, int y, int z) {
		if (pulseOn == isToggleEnabled(0)
				|| mc.thePlayer.capabilities.isCreativeMode
				&& !configuration
						.getPropertyBoolean(ConfigOption.USE_IN_CREATIVE)
				|| mc.currentScreen != null || mc.isSingleplayer()
				&& !configuration.getPropertyBoolean(ConfigOption.BLOCKS_SP)
				|| !mc.isSingleplayer()
				&& !configuration.getPropertyBoolean(ConfigOption.BLOCKS_MP)) {
			return false;
		}
		debug("====================================================");
		debug(getLongVersionString());
		debug("Switching on block, x = %d, y = %d, z = %d", x, y, z);
		try {
			switchToBestTool(mc.theWorld, x, y, z);
			return true;
		} catch (Throwable e) {
			throwException("Error switching tools", e, false);
			return false;
		}
	}

	public boolean potentiallySwitchWeapons(EntityLivingBase entity) {
		if (pulseOn == isToggleEnabled(0)
				|| mc.thePlayer.capabilities.isCreativeMode
				&& !configuration
						.getPropertyBoolean(ConfigOption.USE_IN_CREATIVE)
				|| mc.currentScreen != null || mc.isSingleplayer()
				&& !configuration.getPropertyBoolean(ConfigOption.MOBS_SP)
				|| !mc.isSingleplayer()
				&& !configuration.getPropertyBoolean(ConfigOption.MOBS_MP)) {
			return false;
		}
		debug("====================================================");
		debug(getLongVersionString());
		debug("Switching on an entity, %s", entity.toString());
		try {
			entitySwitchedOn = entity;
			entityAttackStage = STAGE_SWITCHED;
			switchToBestWeapon(mc.thePlayer, entity);
			return true;
		} catch (Throwable e) {
			throwException("Error switching weapons", e, false);
			return false;
		}
	}

	@EventHandler
	@Override
	public void preInit(FMLPreInitializationEvent event) {
		super.preInit(event);
	}

	private void switchBack() {
		if (mc.thePlayer.inventory.currentItem != prevtool) {
			mc.thePlayer.inventory.currentItem = prevtool;
			debug("Switching tools back to %d", prevtool);
		}
	}

	private void switchToBestTool(World world, int x, int y, int z) {
		Block block = Block.blocksList[world.getBlockId(x, y, z)];
		debug("Testing vs block %s", block.getUnlocalizedName());
		String[] names = new String[9];
		for (int i = 0; i < 9; i++) {
			names[i] = mc.thePlayer.inventory.mainInventory[i] == null ? "Nothing"
					: mc.thePlayer.inventory.mainInventory[i]
							.getUnlocalizedName();
			debug("Hotbar slot %d contains item %s", i, names[i]);
		}

		int currentBest = prevtool;
		debug("Block hardness is %f", getBlockHardness(world, x, y, z));

		for (int i = 0; i < 9; i++) {
			if (i == currentBest) {
				continue;
			}
			debug("Checking if tool %d, which is %s, is better than %d, which is %s",
					i, names[i], currentBest, names[currentBest]);
			if (isToolBetter(mc.thePlayer.inventory.mainInventory[i],
					mc.thePlayer.inventory.mainInventory[currentBest], world,
					x, y, z)) {
				debug("Changing possible best tool.");
				currentBest = i;
			}
		}
		debug("Current best is %d, which is %s", currentBest,
				currentBest == -1 ? "Nothing" : names[currentBest]);
		switchToolsToN(currentBest == -1 ? mc.thePlayer.inventory.currentItem
				: currentBest);
	}

	private void switchToBestWeapon(EntityPlayer entityplayer,
			EntityLivingBase entityover) {
		ItemStack[] inventory = entityplayer.inventory.mainInventory;
		int currentBest = 0;
		while (currentBest < 9 && inventory[currentBest] == null) {
			currentBest++;
		}
		if (currentBest == 9)
			return;
		String[] names = new String[9];
		for (int i = 0; i < 9; i++) {
			String name = (inventory[i] == null) ? "Nothing" : inventory[i]
					.getUnlocalizedName();
			names[i] = name;
			debug("Hotbar slot %d contains item %s", i, name);
		}
		debug("Current item is %d", entityplayer.inventory.currentItem);
		debug("Setting possible best weapon to %d, which is %s", currentBest,
				names[currentBest]);
		for (int i = currentBest + 1; i < 9; i++) {
			debug("Checking if weapon %d, which is %s, is better than %d, which is %s",
					i, names[i], currentBest, names[currentBest]);
			if (isWeaponBetter(inventory[i], inventory[currentBest], entityover)) {
				debug("Changing possible best weapon because weapon is better.");
				currentBest = i;
			}
		}
		switchToolsToN(currentBest);
	}

	private void switchToolsToN(int n) {
		EntityPlayer entityplayer = mc.thePlayer;
		entityplayer.inventory.currentItem = n;
		String name = entityplayer.inventory.mainInventory[n] == null ? "Nothing"
				: entityplayer.inventory.mainInventory[n].getUnlocalizedName();
		debug("Switching tools to %d, which is %s", n, name);
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {

	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.CLIENT);
	}

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {
		clientTick();
	}

	private void unFakeItemForPlayer() {
		ItemStack fakedStack = mc.thePlayer.inventory.mainInventory[mc.thePlayer.inventory.currentItem];
		mc.thePlayer.inventory.mainInventory[mc.thePlayer.inventory.currentItem] = prevHeldItemStack;
		if (fakedStack != null) {
			mc.thePlayer.getAttributeMap().removeAttributeModifiers(
					fakedStack.getAttributeModifiers());
		}
		if (prevHeldItemStack != null) {
			mc.thePlayer.getAttributeMap().applyAttributeModifiers(
					prevHeldItemStack.getAttributeModifiers());
		}
	}

	private void unFakeRandomForWorld(World world) {
		world.rand = prevRandom;
		prevRandom = null;
	}

}