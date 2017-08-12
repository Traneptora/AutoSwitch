package thebombzen.mods.autoswitch;

import java.util.Arrays;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thebombzen.mods.autoswitch.configuration.Configuration;
import thebombzen.mods.autoswitch.configuration.ToolSelectionMode;
import com.thebombzen.mods.thebombzenapi.ComparableTuple;
import com.thebombzen.mods.thebombzenapi.ThebombzenAPI;
import com.thebombzen.mods.thebombzenapi.ThebombzenAPIBaseMod;

/**
 * The main AutoSwitch mod
 * 
 * @author thebombzen
 */
@SideOnly(Side.CLIENT)
@Mod(modid = "autoswitch", name = "AutoSwitch", version = Constants.VERSION, dependencies = "required-after:thebombzenapi", guiFactory = "thebombzen.mods.autoswitch.configuration.ConfigGuiFactory", clientSideOnly = true, acceptedMinecraftVersions = "[1.9.4, 1.11.2]")
public class AutoSwitch extends ThebombzenAPIBaseMod {

	/**
	 * This is the default stage for TDAI. Used under normal conditions.
	 */
	public static final int STAGE_H0 = 0;
	/**
	 * This is the TDAI stage after AS has switched but before the event has been canceled.
	 */
	public static final int STAGE_SWITCHED = 1;
	/**
	 * This is the TDAI stage after AS has canceled the attack event.
	 */
	public static final int STAGE_CANCELED = 2;
	
	/**
	 * A convenience field storing the current Minecraft object
	 */
	public static final Minecraft mc = Minecraft.getMinecraft();

	/**
	 * The current TDAI attack stage
	 */
	private int entityAttackStage = STAGE_H0;

	/**
	 * The current TDAI entity we're tracking
	 */
	private EntityLivingBase entitySwitchedOn = null;

	/**
	 * The ThebombzenAPIConfiguration for AS
	 */
	private Configuration configuration;

	/**
	 * Was the mouse down last tick?
	 */
	private boolean prevMouseDown = false;
	
	/**
	 * Was the pulse key down last tick?
	 */
	private boolean prevPulse = false;
	
	/**
	 * This slot index contained the previous tool before switching
	 */
	private int prevtool = 0;
	
	/**
	 * Is the pulse key down?
	 */
	private boolean pulseOn = false;
	
	/**
	 * Should we switch back after switching?
	 */
	private boolean switchback = false;
	
	/**
	 * Is mcMMO treefeller currently being readied?
	 */
	private boolean treefellerOn = false;

	/**
	 * The instance field for AS
	 */
	@Instance("autoswitch")
	public static AutoSwitch instance;
	
	/**
	 * Used to detect if treefeller has been readied.
	 * If treefeller is activated and DETECT_TREEFELLER is on, then AS will switch to Slow Standard in the meantime.
	 * @param event The event containing the content of the chat message
	 */
	@SubscribeEvent
	public void clientChat(ClientChatReceivedEvent event){
		String text = event.getMessage().getUnformattedText();
		if (text.equals(configuration.getStringProperty(Configuration.TREEFELLER_READY_AXE))){
			treefellerOn = true;
		} else if (text.matches(configuration.getStringProperty(Configuration.TREEFELLER_READY_OTHER))) {
			treefellerOn = false;
		} else if (text.equals(configuration.getStringProperty(Configuration.TREEFELLER_LOWER_AXE))){
			treefellerOn = false;
		} else if (text.equals(configuration.getStringProperty(Configuration.TREEFELLER_WORNOFF))){
			treefellerOn = false;
		} else if (text.equals(configuration.getStringProperty(Configuration.TREEFELLER_AXE_SPLINTER))){
			treefellerOn = false;
		} else if (text.matches(configuration.getStringProperty(Configuration.TREEFELLER_TOO_TIRED))){
			treefellerOn = false;
		} else if (text.equals(configuration.getStringProperty(Configuration.TREEFELLER_SKULL_SPLITTER))){
			treefellerOn = false;
		}
	}
	
	/**
	 * This is our main client tick loop, used to run most AS functions, such as switching.
	 */
	@SubscribeEvent
	public void clientTick(ClientTickEvent event) {
		if (!event.phase.equals(Phase.START)) {
			return;
		}
		
		if (mc.world == null) {
			return;
		}
		
		if (ThebombzenAPI.hasWorldChanged()){
			treefellerOn = false;
		}

		// If we canceled the attack last tick, then go ahead and attack now.
		if (entityAttackStage == STAGE_CANCELED) {
			mc.player.swingArm(EnumHand.MAIN_HAND);;
			mc.playerController.attackEntity(mc.player, entitySwitchedOn);
			entityAttackStage = STAGE_H0;
			entitySwitchedOn = null;
			return;
		}

		pulseOn = ThebombzenAPI.isExtendedKeyDown(configuration.getKeyCodeProperty(Configuration.PULSE_KEY));
		boolean mouseDown =  ThebombzenAPI.isExtendedKeyDown(mc.gameSettings.keyBindAttack.getKeyCode());
		if (!mouseDown && prevMouseDown || mouseDown && pulseOn ^ prevPulse) {
			switchBack();
		}
		if (mouseDown && !prevMouseDown || mouseDown && pulseOn ^ prevPulse) {
			prevtool = mc.player.inventory.currentItem;
		}
		if (mouseDown) {
			if (mc.objectMouseOver != null
					&& mc.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK) {
				potentiallySwitchTools(mc.world, mc.objectMouseOver.getBlockPos());
			} else if (mc.objectMouseOver != null
					&& mc.objectMouseOver.typeOfHit == RayTraceResult.Type.ENTITY
					&& mc.objectMouseOver.entityHit instanceof EntityLivingBase) {
				potentiallySwitchWeapons((EntityLivingBase) mc.objectMouseOver.entityHit);
			}
		}
		prevMouseDown = mouseDown;
		prevPulse = pulseOn;
	}
	
	/**
	 * Send a string to the debug logger, but only if debugging is enabled.
	 * @param string The debug message to send
	 */
	public void debug(String string) {
		debug("%s", string);
	}
	
	/**
	 * Send a string to the debug logger, but only if debugging is enabled.
	 * Uses the same notation as String.format() or PrintStream.format()
	 * @param format A printf-style format string
	 * @param args The arguments for the format string
	 */
	public void debug(String format, Object... args) {
		if (configuration.getBooleanProperty(Configuration.DEBUG)) {
			forceDebug(format, args);
		}
	}
	
	/**
	 * Print an exception to the debug logger, but only if debugging is enabled.
	 * Prints a short info message, as well as the stack trace.
	 * @param exception the Throwable to print
	 */
	public void debugException(Throwable exception){
		if (configuration.getBooleanProperty(Configuration.DEBUG)){
			forceDebugException(exception);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Configuration getConfiguration() {
		return configuration;
	}

	@Override
	public String getDownloadLocationURLString() {
		return "http://is.gd/ThebombzensMods#AutoSwitch";
	}

	@Override
	public String getLongName() {
		return "AutoSwitch";
	}

	@Override
	public String getLongVersionString() {
		return "AutoSwitch, version " + Constants.VERSION + ", Minecraft " + Constants.MC_VERSION;
	}

	@Override
	public int getNumToggleKeys() {
		return 1;
	}

	@Override
	public String getShortName() {
		return "AS";
	}

	@Override
	protected String getToggleMessageString(int index, boolean enabled) {
		if (enabled) {
			return "AutoSwitch is now enabled.";
		} else {
			return "AutoSwitch is now disabled.";
		}
	}

	@Override
	protected String getVersionFileURLString() {
		return "https://dl.dropboxusercontent.com/u/51080973/Mods/AutoSwitch/ASVersion.txt";
	}

	@Override
	public void init1(FMLPreInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);
		configuration = new Configuration(this);
		FMLCommonHandler.instance().findContainerFor(this).getMetadata().authorList = Arrays.asList("Thebombzen");
	}

	/**
	 * Compares two tools using the AS Algorithm
	 * @param newItemStack The ItemStack containing the new tool
	 * @param oldItemStack The ItemStack containing the old tool
	 * @param world The relevant World object
	 * @param pos The position of the block to test inside the World object
	 * @return true if the new tool is better than the old one, false otherwise or if they are equal
	 */
	public boolean isToolBetter(ItemStack newItemStack, ItemStack oldItemStack,
			World world, BlockPos pos) {

		IBlockState blockState = world.getBlockState(pos);
		
		if (blockState.getBlock().isAir(blockState, world, pos)){
			debug("Not switching because air.");
			return false;
		}

		int newAdjustedBlockStr = Tests.getAdjustedBlockStr(Tests.getBlockStrength(newItemStack, world, pos));
		int oldAdjustedBlockStr = Tests.getAdjustedBlockStr(Tests.getBlockStrength(oldItemStack, world, pos));

		if (newAdjustedBlockStr == Integer.MIN_VALUE && oldAdjustedBlockStr == Integer.MIN_VALUE) {
			debug("Not switching because block is unbreakable by either item.");
			return false;
		}
		
		float newBlockStr = Tests.getBlockStrength(newItemStack, world, pos);
		float oldBlockStr = Tests.getBlockStrength(oldItemStack, world, pos);

		debug("newAdjustedBlockStr: %d, oldAdjustedBlockStr: %d", newAdjustedBlockStr, oldAdjustedBlockStr);
		
		ToolSelectionMode toolSelectionMode = configuration.getToolSelectionMode(blockState);
		
		debug("Tool Selection Mode: %s", toolSelectionMode.toString());
		
		ComparableTuple<Integer> newStandard = Tests.getToolStandardness(newItemStack, world, pos);
		ComparableTuple<Integer> oldStandard = Tests.getToolStandardness(oldItemStack, world, pos);
		ComparableTuple<Integer> newEffectiveness = Tests.getToolEffectiveness(newItemStack, world, pos);
		ComparableTuple<Integer> oldEffectiveness = Tests.getToolEffectiveness(oldItemStack, world, pos);
		
		debug("newStandard: %s, oldStandard: %s", newStandard.toString(), oldStandard.toString());

		boolean newDamageable = Tests.isItemStackDamageableOnBlock(
				newItemStack, world, pos);
		boolean oldDamageable = Tests.isItemStackDamageableOnBlock(
				oldItemStack, world, pos);
		
		int adjustedBlockStrComparison = new Integer(newAdjustedBlockStr).compareTo(oldAdjustedBlockStr);
		int blockStrComparison = Float.compare(newBlockStr, oldBlockStr);
		int standardComparison = newStandard.compareTo(oldStandard);
		int effectivenessComparison = newEffectiveness.compareTo(oldEffectiveness);
		boolean isNewStandard = newStandard.compareTo(Tests.standardThreshold) > 0;
		boolean isOldStandard = oldStandard.compareTo(Tests.standardThreshold) > 0;
		
		if (toolSelectionMode.isStandard() || configuration.getStandardToolOverrideState(newItemStack, blockState) != configuration.getStandardToolOverrideState(oldItemStack, blockState)) {
			if (standardComparison > 0) {
				debug("Switching because new item is more standard than old.");
				return true;
			} else if (standardComparison < 0) {
				debug("Not switching because old item is more standard than new.");
				return false;
			}
		} else {
			if (toolSelectionMode.isFast()) {
				if (adjustedBlockStrComparison > 0) {
					debug("Switching because new tool is stronger.");
					return true;
				} else if (adjustedBlockStrComparison < 0) {
					debug("Not switching because old tool is stronger.");
					return false;
				}
			} else { // This should never happen. Slow nonstandard isn't a thing.
				debug("Something went wrong. It appears Slow Nonstandard is on.");
			}
		}

		boolean silkWorks = Tests.doesSilkTouchWorkOnBlock(world, pos);
		boolean newHasSilk = EnchantmentHelper.getEnchantmentLevel(
				Enchantments.SILK_TOUCH, newItemStack) > 0;
		boolean oldHasSilk = EnchantmentHelper.getEnchantmentLevel(
				Enchantments.SILK_TOUCH, oldItemStack) > 0;
		
		if (configuration.shouldIgnoreSilkTouch(blockState)){
			debug("Ignoring Silk Touch.");
		} else {
			debug("silkWorks: %b, newHasSilk: %b, oldHasSilk: %b", silkWorks,
					newHasSilk, oldHasSilk);
			if (newHasSilk && !oldHasSilk) {
				if (silkWorks) {
					debug("Switching because new has silk touch and old doesn't, and new works.");
					return true;
				} else {
					if (isOldStandard) {
						debug("Not switching because new has silk touch and old doesn't, and old replaces new.");
						return false;
					} else if (!isNewStandard) {
						debug("Not switching because new has silk touch and old doesn't, and new is weak.");
						return false;
					}
				}
			} else if (oldHasSilk && !newHasSilk) {
				if (silkWorks) {
					debug("Not switching because old has silk touch and new doesn't, and old works.");
					return false;
				} else {
					if (isNewStandard) {
						debug("Switching because old has silk touch and new doesn't, and new replaces old.");
						return true;
					} else if (!isOldStandard) {
						debug("Switching because old has silk touch and new doesn't, and old is weak.");
						return true;
					}
				}
			}
		}
	

		boolean fortuneWorks = Tests.doesFortuneWorkOnBlock(world, pos);
		int newFortuneLevel = EnchantmentHelper.getEnchantmentLevel(
				Enchantments.FORTUNE, newItemStack);
		int oldFortuneLevel = EnchantmentHelper.getEnchantmentLevel(
				Enchantments.FORTUNE, oldItemStack);

		if (configuration.shouldIgnoreFortune(blockState)){
			debug("Ignoring Fortune.");
		} else {
			debug("fortuneWorks: %b, newFortuneLevel: %d, oldFortuneLevel: %d",
					fortuneWorks, newFortuneLevel, oldFortuneLevel);
			if (newFortuneLevel > oldFortuneLevel) {
				if (fortuneWorks) {
					debug("Switching because new fortune is more than old, and new works.");
					return true;
				} else {
					if (isOldStandard) {
						debug("Not switching because new fortune is more than old, and old replaces new.");
						return false;
					} else if (!isNewStandard) {
						debug("Not switching because new fortune is more than old, and new is weak.");
						return false;
					}
				}
			} else if (oldFortuneLevel > newFortuneLevel) {
				if (fortuneWorks) {
					debug("Not switching because old fortune is more than new, and old works.");
					return false;
				} else {
					if (isNewStandard) {
						debug("Switching because old fortune is more than new, and new replaces old.");
						return true;
					} else if (!isOldStandard) {
						debug("Switching because old fortune is more than new, and old is weak.");
						return true;
					}
				}
			}
		}
		
		if (toolSelectionMode.isStandard()) {
			if (effectivenessComparison > 0) {
				debug("Switching because new item is more effective than old.");
				return true;
			} else if (effectivenessComparison < 0) {
				debug("Not switching because old item is more effective than new.");
				return false;
			}
		}
		
		if (toolSelectionMode.isFast()) {
			if (adjustedBlockStrComparison > 0) {
				debug("Switching because new tool is stronger.");
				return true;
			} else if (adjustedBlockStrComparison < 0) {
				debug("Not switching because old tool is stronger.");
				return false;
			}
		} else {
			if (adjustedBlockStrComparison < 0) {
				debug("Switching because new tool is weaker.");
				return true;
			} else if (adjustedBlockStrComparison > 0) {
				debug("Not switching because old tool is weaker.");
				return false;
			}
		}

		Set<Enchantment> bothItemsEnchantments = Tests
				.getNonstandardNondamageEnchantmentsOnBothStacks(newItemStack,
						oldItemStack);

		for (Enchantment enchantment : bothItemsEnchantments) {
			int oldLevel = EnchantmentHelper.getEnchantmentLevel(
					enchantment, oldItemStack);
			int newLevel = EnchantmentHelper.getEnchantmentLevel(
					enchantment, newItemStack);
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

			int newUnbreakingLevel = EnchantmentHelper.getEnchantmentLevel(
					Enchantments.UNBREAKING, newItemStack);
			int oldUnbreakingLevel = EnchantmentHelper.getEnchantmentLevel(
					Enchantments.UNBREAKING, oldItemStack);

			if (newUnbreakingLevel > oldUnbreakingLevel) {
				debug("Switching because new unbreaking is more than old unbreaking.");
				return true;
			} else if (oldUnbreakingLevel > newUnbreakingLevel) {
				debug("Not switching because old unbreaking is more than new unbreaking.");
				return false;
			}
		}
		
		if (toolSelectionMode.isFast() && !(newDamageable && oldDamageable)) {
			if (blockStrComparison > 0) {
				debug("Switching because new tool is stronger.");
				return true;
			} else if (blockStrComparison < 0) {
				debug("Not switching because old tool is stronger.");
				return false;
			}
		} else {
			if (blockStrComparison < 0) {
				debug("Switching because new item is worse than old item and Slow Standard is on.");
				return true;
			} else if (blockStrComparison > 0) {
				debug("Not switching because new item is better than old item and Slow Standard is on.");
				return false;
			}
		}

		debug("Not switching because tools are equal.");
		return false;

	}

	/**
	 * Returns the current treefeller detection state.
	 * @return true if AS has detected that treefeller is on, false otherwise
	 */
	public boolean isTreefellerOn(){
		return treefellerOn;
	}

	/**
	 * Compares two weapons using the AS Algorithm
	 * @param newItemStack The ItemStack containing the new weapon
	 * @param oldItemStack The ItemStack containing the old weapon
	 * @param entityover The entity against which to test the weaposn
	 * @return true if the new weapon is better, false otherwise or if they are equal
	 */
	public boolean isWeaponBetter(ItemStack newItemStack,
			ItemStack oldItemStack, EntityLivingBase entityover) {
		
		int oldState = configuration.getWeaponOverrideState(oldItemStack, entityover);
		int newState = configuration.getWeaponOverrideState(newItemStack, entityover);
		
		if (newState == Configuration.OVERRIDDEN_NO && oldState != Configuration.OVERRIDDEN_NO){
			debug("Not switching because new is overridden no.");
			return false;
		} else if (newState != Configuration.OVERRIDDEN_NO && oldState == Configuration.OVERRIDDEN_NO){
			debug("Switching because old is overridden no.");
			return true;
		}
		if (newState == Configuration.OVERRIDDEN_YES && oldState != Configuration.OVERRIDDEN_YES){
			debug("Switching because new is ovverridden yes.");
			return true;
		} else if (oldState == Configuration.OVERRIDDEN_YES && newState != Configuration.OVERRIDDEN_YES){
			debug("Not switching because old is overridden yes.");
			return false;
		}
		
		boolean isPlayer = entityover instanceof EntityPlayer;
		double oldDamage = Tests.getFullItemStackDamage(oldItemStack, entityover);
		double newDamage = Tests.getFullItemStackDamage(newItemStack, entityover);

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

			int oldHits;
			int newHits;

			if (oldDamage == 0) {
				oldHits = Integer.MAX_VALUE;
			} else {
				oldHits = MathHelper.ceil(entityover.getMaxHealth()
						/ oldDamage);
			}

			if (newDamage == 0) {
				newHits = Integer.MAX_VALUE;
			} else {
				newHits = MathHelper.ceil(entityover.getMaxHealth()
						/ newDamage);
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

		int newLootingLevel = EnchantmentHelper.getEnchantmentLevel(
				Enchantments.LOOTING, newItemStack);
		int newFireAspectLevel = EnchantmentHelper.getEnchantmentLevel(
				Enchantments.FIRE_ASPECT, newItemStack);
		int newKnockbackLevel = EnchantmentHelper.getEnchantmentLevel(
				Enchantments.KNOCKBACK, newItemStack);
		int newUnbreakingLevel = EnchantmentHelper.getEnchantmentLevel(
				Enchantments.UNBREAKING, newItemStack);

		int oldLootingLevel = EnchantmentHelper.getEnchantmentLevel(
				Enchantments.LOOTING, oldItemStack);
		int oldFireAspectLevel = EnchantmentHelper.getEnchantmentLevel(
				Enchantments.FIRE_ASPECT, oldItemStack);
		int oldKnockbackLevel = EnchantmentHelper.getEnchantmentLevel(
				Enchantments.KNOCKBACK, oldItemStack);
		int oldUnbreakingLevel = EnchantmentHelper.getEnchantmentLevel(
				Enchantments.UNBREAKING, oldItemStack);

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

		Set<Enchantment> bothItemsEnchantments = Tests
				.getNonstandardNondamageEnchantmentsOnBothStacks(newItemStack,
						oldItemStack);

		for (Enchantment enchantment : bothItemsEnchantments) {
			int oldLevel = EnchantmentHelper.getEnchantmentLevel(
					enchantment, oldItemStack);
			int newLevel = EnchantmentHelper.getEnchantmentLevel(
					enchantment, newItemStack);
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

		if (Tests.isSword(newItemStack) && !Tests.isSword(oldItemStack)) {
			debug("Switching because new weapon is sword and old isn't.");
			return true;
		}
		if (Tests.isSword(oldItemStack) && !Tests.isSword(newItemStack)) {
			debug("Not switching because old weapon is sword and new isn't.");
			return false;
		}
		
		if (newDamage > oldDamage) {
			debug("Switching because new damage is more and all else is equal.");
			return true;
		} else if (newDamage < oldDamage) {
			debug("Not switching because old damage is more and all else is equal.");
			return false;
		}

		boolean newDamageable = Tests.isItemStackDamageable(newItemStack);
		boolean oldDamageable = Tests.isItemStackDamageable(oldItemStack);
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

		if (newItemStack.isEmpty() && !oldItemStack.isEmpty()) {
			debug("Switching because new tool is fist and old is useless.");
			return true;
		} else if (oldItemStack.isEmpty() && !newItemStack.isEmpty()) {
			debug("Not switching because old tool is fist and new is useless.");
			return false;
		}

		debug("Not switching because weapons are equal.");
		return false;
	}

	/**
	 * Initiates TDAI, or Tick-Delay Attack Interception.
	 * Because of bug MC-28289, AutoSwitch will intercept any attack and then switch weapons.
	 * It will cancel the attack and then wait exactly one tick to execute it.
	 * This allows the new weapon's damage to register.
	 */
	@SubscribeEvent
	public void onEntityAttack(AttackEntityEvent event) {
		if (!event.getEntity().world.isRemote) {
			return;
		}
		if (entityAttackStage == STAGE_SWITCHED
				&& entitySwitchedOn == event.getTarget()) {
			entityAttackStage = STAGE_CANCELED;
			event.setCanceled(true);
		} else if (entityAttackStage != STAGE_CANCELED) {
			entitySwitchedOn = null;
			entityAttackStage = STAGE_H0;
		}
	}

	/**
	 * Switch tools as long as all criteria hold.
	 * Criteria include various levels of enabled.
	 * @param world The relevant World object
	 * @param pos The position of the block within the World
	 * @return true if switching actually happened error-free, false otherwise
	 */
	public boolean potentiallySwitchTools(World world, BlockPos pos) {
		if (pulseOn == isToggleEnabled(Configuration.DEFAULT_ENABLED.getDefaultToggleIndex())
				|| mc.playerController.isInCreativeMode()
				&& !configuration
						.getBooleanProperty(Configuration.USE_IN_CREATIVE)
				|| mc.currentScreen != null || !configuration.getSingleMultiProperty(Configuration.BLOCKS)) {
			return false;
		}
		debug("====START====");
		debug(getLongVersionString());
		try {
			switchToBestTool(mc.world, pos);
			return true;
		} catch (Throwable e) {
			throwException("Error switching tools", e, false);
			return false;
		} finally {
			debug("====END====");
		}
	}

	/**
	 * Switch weapons as long as all criteria hold.
	 * Criteria include various levels of enabled.
	 * Also changes the state of the TDAI.
	 * @param entity The EntityLivingBase against which we're switching
	 * @return true if switching actually happened error-free, false otherwise
	 */
	public boolean potentiallySwitchWeapons(EntityLivingBase entity) {
		if (pulseOn == isToggleEnabled(Configuration.DEFAULT_ENABLED.getDefaultToggleIndex())
				|| mc.playerController.isInCreativeMode()
				&& !configuration
						.getBooleanProperty(Configuration.USE_IN_CREATIVE)
				|| mc.currentScreen != null || !configuration.getSingleMultiProperty(Configuration.MOBS)) {
			return false;
		}
		debug("====START====");
		debug(getLongVersionString());
		debug("Switching on an entity, %s", entity.toString());
		try {
			entitySwitchedOn = entity;
			entityAttackStage = STAGE_SWITCHED;
			switchToBestWeapon(mc.player, entity);
			return true;
		} catch (Throwable e) {
			throwException("Error switching weapons", e, false);
			return false;
		} finally {
			debug("====END====");
		}
	}

	/**
	 * Switch items back to the previous item, the one from before switching
	 */
	private void switchBack() {
		if (switchback) {
			mc.player.inventory.currentItem = prevtool;
			switchback = false;
			debug("Switching tools back to %d", prevtool);
		}
	}
	
	/**
	 * Returns a UniqueIdentifier for a particular item, accordinate to GameData
	 */
	public static ResourceLocation findUniqueIdentifierFor(Item item){
		return Item.REGISTRY.getNameForObject(item);
	}
	
	/**
	 * Returns a UniqueIdentifier for a particular block, according to GameData
	 */
	public static ResourceLocation findUniqueIdentifierFor(Block block){
		return Block.REGISTRY.getNameForObject(block);
	}

	/**
	 * If switching actually happens, go ahead and switch to the best tool.
	 * @param world The relevant World object
	 * @param pos The location of the block against which we're switching
	 */
	private void switchToBestTool(World world, BlockPos pos) {

		IBlockState state = world.getBlockState(pos);
		ResourceLocation location = findUniqueIdentifierFor(state.getBlock());

		String name = location.toString();

		debug("Testing vs block %s", name);
		String[] names = new String[9];
		for (int i = 0; i < 9; i++) {
			if (mc.player.inventory.mainInventory.get(i).isEmpty()) {
				names[i] = "Empty";
			} else {
				ResourceLocation itemLocation = findUniqueIdentifierFor(mc.player.inventory.mainInventory.get(i).getItem());
				names[i] = itemLocation.toString();
			}
			debug("Hotbar slot %d contains item %s", i, names[i]);
		}

		int currentBest = prevtool;

		debug("Block hardness is %f", Tests.getBlockHardness(world, pos));

		for (int i = 0; i < 9; i++) {

			if (i == currentBest) {
				continue;
			}

			debug("Checking if tool %d, which is %s, is better than %d, which is %s",
					i, names[i], currentBest, names[currentBest]);
			if (isToolBetter(mc.player.inventory.mainInventory.get(i),
					mc.player.inventory.mainInventory.get(currentBest), world,
					pos)) {
				debug("Changing possible best tool.");
				currentBest = i;
			}
		}
		debug("Current best is %d, which is %s", currentBest,
				names[currentBest]);
		switchToolsToN(currentBest);
		if (configuration.getSingleMultiProperty(Configuration.SWITCHBACK_BLOCKS)){
			switchback = true;
		} else {
			prevtool = currentBest;
		}
	}

	/**
	 * If switching actually happens, go ahead and switch to the best weapon.
	 * @param entity The entity against which we're switching
	 */
	private void switchToBestWeapon(EntityPlayer entityplayer,
			EntityLivingBase entityover) {

		String[] names = new String[9];
		for (int i = 0; i < 9; i++) {
			if (mc.player.inventory.mainInventory.get(i).isEmpty()) {
				names[i] = "Empty";
			} else {
				ResourceLocation itemLocation = findUniqueIdentifierFor(mc.player.inventory.mainInventory.get(i)
								.getItem());
				names[i] = itemLocation.toString();
			}
			debug("Hotbar slot %d contains item %s", i, names[i]);
		}

		int currentBest = prevtool;

		debug("Current item is %d", entityplayer.inventory.currentItem);
		debug("Setting possible best weapon to %d, which is %s", currentBest,
				names[currentBest]);

		for (int i = 0; i < 9; i++) {
			debug("Checking if weapon %d, which is %s, is better than %d, which is %s",
					i, names[i], currentBest, names[currentBest]);
			if (isWeaponBetter(mc.player.inventory.mainInventory.get(i), mc.player.inventory.mainInventory.get(currentBest), entityover)) {
				debug("Changing possible best weapon because weapon is better.");
				currentBest = i;
			}
		}
		switchToolsToN(currentBest);
		if (configuration.getSingleMultiProperty(Configuration.SWITCHBACK_MOBS)){
			switchback = true;
		} else {
			prevtool = currentBest;
		}
	}

	/**
	 * Actually switch tools to a given slot, and do the necessary storage and debug routines
	 * @param n The slot to which we must switch
	 */
	private void switchToolsToN(int n) {
		EntityPlayer entityplayer = mc.player;
		entityplayer.inventory.currentItem = n;
		String name;
		if (entityplayer.inventory.mainInventory.get(n).isEmpty()) {
			name = "Nothing";
		} else {
			ResourceLocation itemLocation = findUniqueIdentifierFor(entityplayer.inventory.mainInventory.get(n)
							.getItem());
			name = itemLocation.toString();
		}
		debug("Switching tools to %d, which is %s", n, name);
	}

}
