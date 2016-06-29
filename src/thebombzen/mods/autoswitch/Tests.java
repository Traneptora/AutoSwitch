package thebombzen.mods.autoswitch;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Enchantments;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thebombzen.mods.autoswitch.configuration.Configuration;
import thebombzen.mods.thebombzenapi.ComparableTuple;
import thebombzen.mods.thebombzenapi.FieldNotFoundException;
import thebombzen.mods.thebombzenapi.ThebombzenAPI;

@SideOnly(Side.CLIENT)
public final class Tests {
	
	private static final Minecraft mc = Minecraft.getMinecraft();
	
	private static ItemStack prevHeldItem = null;
	private static Random prevRandom = null;
	
	private static final String[] randomNames = {"rand", "field_73012_v", "v"};
	private static final String[] createStackedBlockNames = {"createStackedBlock", "i", "func_180643_i"};
	
	/**
	 * Anything strictly greater than this is considered to be "standard"
	 */
	public static final ComparableTuple<Integer> standardThreshold = new ComparableTuple<Integer>(0, 0);
	
	public static ItemStack createStackedBlock(Block block, IBlockState state) {
		return ThebombzenAPI.invokePrivateMethod(block, Block.class, createStackedBlockNames,
				new Class<?>[] { IBlockState.class }, state);
	}
	

	public static boolean doesFortuneWorkOnBlock(World world, BlockPos pos) {

		IBlockState blockState = world.getBlockState(pos);

		int state = AutoSwitch.instance.getConfiguration().getFortuneOverrideState(blockState);
		if (state == Configuration.OVERRIDDEN_NO){
			return false;
		} else if (state == Configuration.OVERRIDDEN_YES){
			return true;
		}

		Random maxRandom = new NotSoRandom(false);
		Random zeroRandom = new NotSoRandom(true);

		List<ItemStack> defaultMaxRandom;
		List<ItemStack> defaultZeroRandom;
		List<ItemStack> fortuneMaxRandom;
		List<ItemStack> fortuneZeroRandom;

		fakeRandomForWorld(world, maxRandom);
		defaultMaxRandom = blockState.getBlock().getDrops(world, pos, blockState, 0);
		fortuneMaxRandom = blockState.getBlock().getDrops(world, pos, blockState, 3);
		unFakeRandomForWorld(world);

		fakeRandomForWorld(world, zeroRandom);
		defaultZeroRandom = blockState.getBlock().getDrops(world, pos, blockState, 0);
		fortuneZeroRandom = blockState.getBlock().getDrops(world, pos, blockState, 3);
		unFakeRandomForWorld(world);

		if (!ThebombzenAPI.areItemStackCollectionsEqual(defaultMaxRandom,
				fortuneMaxRandom)
				|| !ThebombzenAPI.areItemStackCollectionsEqual(
						defaultZeroRandom, fortuneZeroRandom)) {
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean doesSilkTouchWorkOnBlock(World world, BlockPos pos) {

		IBlockState blockState = world.getBlockState(pos);

		int state = AutoSwitch.instance.getConfiguration().getSilkTouchOverrideState(blockState);
		
		if (state == Configuration.OVERRIDDEN_NO){
			return false;
		} else if (state == Configuration.OVERRIDDEN_YES){
			return true;
		}

		boolean silkHarvest = blockState.getBlock().canSilkHarvest(world, pos, blockState, mc.thePlayer);
		if (!silkHarvest){
			return false;
		}
		
		Random zeroRandom = new NotSoRandom(true);
		Random maxRandom = new NotSoRandom(false);
		
		ItemStack stackedBlock = createStackedBlock(blockState.getBlock(), blockState);
		List<ItemStack> stackedBlockList = Collections.singletonList(stackedBlock);
		
		List<ItemStack> defaultMaxRandom;
		List<ItemStack> defaultZeroRandom;
		
		fakeRandomForWorld(world, maxRandom);
		defaultMaxRandom = blockState.getBlock().getDrops(world, pos, blockState, 0);
		unFakeRandomForWorld(world);

		fakeRandomForWorld(world, zeroRandom);
		defaultZeroRandom = blockState.getBlock().getDrops(world, pos, blockState, 0);
		unFakeRandomForWorld(world);
		
		if (!ThebombzenAPI.areItemStackCollectionsEqual(stackedBlockList, defaultMaxRandom) || !ThebombzenAPI.areItemStackCollectionsEqual(stackedBlockList, defaultZeroRandom)){
			return true;
		} else {
			return false;
		}
	}

	private static void fakeItemForPlayer(ItemStack itemstack) {
		prevHeldItem = mc.thePlayer.inventory.mainInventory[mc.thePlayer.inventory.currentItem];
		mc.thePlayer.inventory.mainInventory[mc.thePlayer.inventory.currentItem] = itemstack;
		if (prevHeldItem != null) {
			mc.thePlayer.getAttributeMap().removeAttributeModifiers(prevHeldItem.getAttributeModifiers(EntityEquipmentSlot.MAINHAND));
		}
		if (itemstack != null) {
			mc.thePlayer.getAttributeMap().applyAttributeModifiers(itemstack.getAttributeModifiers(EntityEquipmentSlot.MAINHAND));
		}
	}

	private static void fakeRandomForWorld(World world, final Random random) {
		prevRandom = world.rand;
		for (String name : randomNames) {
			try {
				Field field = World.class.getDeclaredField(name);
				field.setAccessible(true);
				try {
					Field modifiersField = Field.class.getDeclaredField("modifiers");
				    modifiersField.setAccessible(true);
				    modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
					field.set(world, random);
					modifiersField.setInt(field, field.getModifiers() | Modifier.FINAL);
					return;
				} catch (Exception e) {
					throw new FieldNotFoundException("Error setting field", e);
				}
			} catch (NoSuchFieldException nsfe) {
				continue;
			}
		}
	}

	public static int getAdjustedBlockStr(double blockStr){
		if (blockStr <= 0){
			return Integer.MIN_VALUE;
		} else {
			return -MathHelper.ceiling_double_int(1D / blockStr);
		}
	}
	
	public static float getBlockHardness(World world, BlockPos pos) {
		IBlockState blockState = world.getBlockState(pos);
		if (blockState == null) {
			return 0;
		} else {
			return blockState.getBlock().getBlockHardness(blockState, world, pos);
		}
	}

	public static float getBlockStrength(ItemStack itemstack, World world, BlockPos pos) {
		IBlockState blockState = world.getBlockState(pos);
		fakeItemForPlayer(itemstack);
		float str = blockState.getBlock().getPlayerRelativeBlockHardness(blockState, mc.thePlayer, world, pos);
		unFakeItemForPlayer();
		return str;
	}
	
	public static float getEff(float str, ItemStack itemstack) {
		if (str <= 1.5F) {
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
	
	public static int getHarvestLevel(ItemStack itemstack, World world, BlockPos pos) {
		IBlockState blockState = world.getBlockState(pos);
		int state = AutoSwitch.instance.getConfiguration().getHarvestOverrideState(itemstack, blockState);
		if (state == Configuration.OVERRIDDEN_NO){
			return -2;
		} else if (state == Configuration.OVERRIDDEN_YES){
			return 2;
		}
		fakeItemForPlayer(null);
		boolean noTool = mc.thePlayer.canHarvestBlock(blockState);
		unFakeItemForPlayer();
		if (noTool){
			return 0;
		}
		fakeItemForPlayer(itemstack);
		boolean can = blockState.getBlock().canHarvestBlock(world, pos, mc.thePlayer);
		unFakeItemForPlayer();
		return can ? 1 : -1;
	}
	
	public static int getFullTinkersConstructItemStackDamage(ItemStack stack,
			EntityLivingBase entity) throws ClassNotFoundException {
		NBTTagCompound tags = stack.getTagCompound();
		NBTTagCompound toolTags = stack.getTagCompound().getCompoundTag(
				"InfiTool");
		int damage = toolTags.getInteger("Attack");
		boolean broken = toolTags.getBoolean("Broken");
		int durability = tags.getCompoundTag("InfiTool").getInteger("Damage");
		float stonebound = tags.getCompoundTag("InfiTool").getFloat("Shoddy");
		float stoneboundDamage = (float) Math.log(durability / 72f + 1) * -2
				* stonebound;
		int earlyModDamage = 0;
		Iterable<?> activeModifiers = ThebombzenAPI.getPrivateField(null,
				Class.forName("tconstruct.library.TConstructRegistry"),
				"activeModifiers");
		Class<?> activeToolModClass = Class
				.forName("tconstruct.library.ActiveToolMod");
		Class<?> toolCoreClass = Class
				.forName("tconstruct.library.tools.ToolCore");
		Class<?>[] attackModClasses = new Class<?>[] { int.class, int.class,
				toolCoreClass, NBTTagCompound.class, NBTTagCompound.class,
				ItemStack.class, EntityLivingBase.class, Entity.class };
		Item tool = stack.getItem();
		for (Object activeToolMod : activeModifiers) {
			earlyModDamage = ThebombzenAPI.invokePrivateMethod(activeToolMod,
					activeToolModClass, "baseAttackDamage", attackModClasses,
					earlyModDamage, damage, tool, tags, toolTags, stack,
					mc.thePlayer, entity);
		}
		damage += earlyModDamage;
		if (mc.thePlayer.isPotionActive(Potion.getPotionFromResourceLocation("strength"))) {
			damage += 3 << mc.thePlayer.getActivePotionEffect(Potion.getPotionFromResourceLocation("strength")).getAmplifier();
		}
		if (mc.thePlayer.isPotionActive(Potion.getPotionFromResourceLocation("weakness"))) {
			damage -= 2 << mc.thePlayer.getActivePotionEffect(Potion.getPotionFromResourceLocation("weakness")).getAmplifier();
		}
		float enchantDamage = 0;
		if (entity instanceof EntityLivingBase) {
			enchantDamage = EnchantmentHelper.getModifierForCreature(stack, entity.getCreatureAttribute());
		}
		damage += stoneboundDamage;
		if (damage < 1) {
			damage = 1;
		}
		if (mc.thePlayer.isSprinting()) {
			float lunge = ThebombzenAPI.invokePrivateMethod(tool,
					toolCoreClass, "chargeAttack", new Class<?>[] {});
			if (lunge > 1f) {
				damage *= lunge;
			}
		}
		int modDamage = 0;
		for (Object activeToolMod : activeModifiers) {
			modDamage = ThebombzenAPI.invokePrivateMethod(activeToolMod,
					activeToolModClass, "attackDamage", attackModClasses,
					modDamage, damage, tool, tags, toolTags, stack,
					mc.thePlayer, entity);
		}
		damage += modDamage;
		if (damage > 0 || enchantDamage > 0) {
			boolean criticalHit = mc.thePlayer.fallDistance > 0.0F
					&& !mc.thePlayer.onGround && !mc.thePlayer.isOnLadder()
					&& !mc.thePlayer.isInWater()
					&& !mc.thePlayer.isPotionActive(Potion.getPotionFromResourceLocation("blindness"))
					&& !mc.thePlayer.isRiding();
			for (Object activeToolMod : activeModifiers) {
				if (ThebombzenAPI.invokePrivateMethod(activeToolMod,
						activeToolModClass, "doesCriticalHit", new Class<?>[] {
								toolCoreClass, NBTTagCompound.class,
								NBTTagCompound.class, ItemStack.class,
								EntityLivingBase.class, Entity.class }, tool,
						tags, toolTags, stack, mc.thePlayer, entity))
					;
				criticalHit = true;
			}
			if (criticalHit) {
				damage *= 1.5F; // This is not actually accurate. It just makes
								// it fully objective. Also, this is how it is
								// in vanilla now.
			}
			damage += enchantDamage;
			float damageModifier = ThebombzenAPI.invokePrivateMethod(tool,
					toolCoreClass, "getDamageModifier", new Class<?>[] {});
			if (damageModifier != 1f) {
				damage *= damageModifier;
			}
			if (broken) {
				damage = 1;
			}
			return damage;
		} else {
			return 0;
		}
	}
	
	public static double getFullRegularItemStackDamage(ItemStack itemStack,
			EntityLivingBase entity) {
		fakeItemForPlayer(itemStack);
		double damage = AutoSwitch.instance.getConfiguration()
				.getCustomWeaponDamage(itemStack, entity);
		if (damage < 0) {
			damage = mc.thePlayer.getEntityAttribute(
					SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
		}
		// getEnchantmentModifierDamage or getEnchantmentModifierLiving
		double enchDamage = EnchantmentHelper.getModifierForCreature(itemStack, entity.getCreatureAttribute());

		if (damage > 0.0D || enchDamage > 0.0D) {
			boolean critical = mc.thePlayer.fallDistance > 0.0F
					&& !mc.thePlayer.onGround && !mc.thePlayer.isOnLadder()
					&& !mc.thePlayer.isInWater()
					&& !mc.thePlayer.isPotionActive(Potion.getPotionFromResourceLocation("blindness"))
					&& !mc.thePlayer.isRiding();

			if (critical && damage > 0) {

				damage *= 1.5D;
			}
			damage += enchDamage;
		}
		unFakeItemForPlayer();
		return damage;
	}
	
	public static double getFullItemStackDamage(ItemStack itemStack, EntityLivingBase entity){
		try {
			if (itemStack != null && itemStack.getTagCompound() != null && Loader.isModLoaded("TConstruct")){
				Class<?> clazz = Class.forName("tconstruct.library.tools.ToolCore");
				if (clazz.isAssignableFrom(itemStack.getItem().getClass())){
					return Tests.getFullTinkersConstructItemStackDamage(itemStack, entity);
				}
			}
		} catch (ClassNotFoundException e){
			AutoSwitch.instance.throwException("Error in Tinkers Construct Compatability", e, false);
		}
		return Tests.getFullRegularItemStackDamage(itemStack, entity);
	}
	
	public static Set<Enchantment> getNonstandardNondamageEnchantmentsOnBothStacks(
			ItemStack stack1, ItemStack stack2) {
		Set<Enchantment> bothItemsEnchantments = new HashSet<Enchantment>();

		if (stack1 != null) {
			bothItemsEnchantments.addAll(EnchantmentHelper.getEnchantments(
					stack1).keySet());
		}
		if (stack2 != null) {
			bothItemsEnchantments.addAll(EnchantmentHelper.getEnchantments(
					stack2).keySet());
		}

		List<Enchantment> standardEnchantments = new ArrayList<Enchantment>();
		Field[] fields = Enchantments.class.getFields();
		
		for (Field field : fields){
			if (field.getType().equals(Enchantment.class)){
				try {
					standardEnchantments.add((Enchantment)field.get(null));
				} catch (IllegalAccessException e) {
					// This should not happen.
					throw new FieldNotFoundException(e);
				}
			}
		}

		bothItemsEnchantments.removeAll(standardEnchantments);

		return bothItemsEnchantments;
	}
	
	private static int getToolOverrideStandardness(ItemStack itemstack, World world, BlockPos pos) {
		IBlockState blockState = world.getBlockState(pos);
		int state = AutoSwitch.instance.getConfiguration().getStandardToolOverrideState(itemstack, blockState);
		if (state == Configuration.OVERRIDDEN_NO){
			return -3;
		} else if (state == Configuration.OVERRIDDEN_YES){
			return 3;
		}
		return 0;
	}
	
	public static ComparableTuple<Integer> getToolStandardness(ItemStack itemstack, World world, BlockPos pos){
		int override = getToolOverrideStandardness(itemstack, world, pos);
		int harvest = getHarvestLevel(itemstack, world, pos);
		return new ComparableTuple<Integer>(override, harvest);
	}
	
	public static ComparableTuple<Integer> getToolEffectiveness(ItemStack itemStack, World world, BlockPos pos){
		int weakStrength = getToolSpeedEffectiveness(itemStack, world, pos);
		int forgeStandard = (itemStack != null && ForgeHooks.isToolEffective(world, pos, itemStack)) ? 1 : 0;
		return new ComparableTuple<Integer>(weakStrength, forgeStandard);
	}
	
	public static ComparableTuple<Integer> getToolDamageability(ItemStack itemStack, World world, BlockPos pos){
		int damageable = Tests.isItemStackDamageableOnBlock(itemStack, world, pos) ? -1 : 0;
		return new ComparableTuple<Integer>(damageable);
	}
	
	public static int getToolSpeedEffectiveness(ItemStack itemstack, World world, BlockPos pos){
		IBlockState blockState = world.getBlockState(pos);
		float hardness = Tests.getBlockHardness(world, pos);
		if (hardness <= 0F){
			return 0;
		}
		
		float blockStrForNull = Tests.getBlockStrength(null, world, pos);
		fakeItemForPlayer(null);
		boolean harvestable = blockState.getBlock().canHarvestBlock(world, pos, mc.thePlayer);
		unFakeItemForPlayer();
		
		float blockStr = Tests.getBlockStrength(itemstack, world, pos);
		fakeItemForPlayer(itemstack);
		boolean harvest = blockState.getBlock().canHarvestBlock(world, pos, mc.thePlayer);
		unFakeItemForPlayer();
		
		if (harvest && !harvestable){
			blockStr *= 0.3F;
		}
		
		if (blockStr > blockStrForNull * 1.5F){
			return 1;
		} else {
			return 0;
		}
	}

	public static boolean isItemStackDamageable(ItemStack itemstack) {
		return itemstack != null && itemstack.getItem().isDamageable();
	}

	public static boolean isItemStackDamageableOnBlock(ItemStack itemstack,
			World world, BlockPos pos) {
		IBlockState blockState = world.getBlockState(pos);
		int state = AutoSwitch.instance.getConfiguration().getDamageableOverrideState(itemstack, blockState);
		if (state == Configuration.OVERRIDDEN_NO){
			return false;
		} else if (state == Configuration.OVERRIDDEN_YES){
			return true;
		}
		if (!isItemStackDamageable(itemstack)) {
			return false;
		}
		return getBlockHardness(world, pos) != 0.0F;
	}

	public static boolean isSword(ItemStack itemstack) {
		if (itemstack == null){
			return false;
		}
		if (itemstack.getItem() instanceof ItemSword){
			return true;
		}
		String name = Item.REGISTRY.getNameForObject(itemstack.getItem()).toString();
		if (name.endsWith("sword")){
			return true;
		}
		return false;
	}

	private static void unFakeItemForPlayer() {
		ItemStack fakedStack = mc.thePlayer.inventory.mainInventory[mc.thePlayer.inventory.currentItem];
		mc.thePlayer.inventory.mainInventory[mc.thePlayer.inventory.currentItem] = prevHeldItem;
		if (fakedStack != null) {
			mc.thePlayer.getAttributeMap().removeAttributeModifiers(
					fakedStack.getAttributeModifiers(EntityEquipmentSlot.MAINHAND));
		}
		if (prevHeldItem != null) {
			mc.thePlayer.getAttributeMap().applyAttributeModifiers(
					prevHeldItem.getAttributeModifiers(EntityEquipmentSlot.MAINHAND));
		}
	}
	
	private static void unFakeRandomForWorld(World world) {
		for (String name : randomNames) {
			try {
				Field field = World.class.getDeclaredField(name);
				field.setAccessible(true);
				try {
					Field modifiersField = Field.class.getDeclaredField("modifiers");
				    modifiersField.setAccessible(true);
				    modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
					field.set(world, prevRandom);
					modifiersField.setInt(field, field.getModifiers() | Modifier.FINAL);
					return;
				} catch (Exception e) {
					throw new FieldNotFoundException("Error setting field", e);
				}
			} catch (NoSuchFieldException nsfe) {
				continue;
			}
		}
		prevRandom = null;
	}
	
	private Tests() {
		
	}
}
